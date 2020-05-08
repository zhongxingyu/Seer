 package com.robonobo.core.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.common.serialization.SerializationException;
 import com.robonobo.common.serialization.SerializationManager;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.config.MetadataServerConfig;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
 import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;
 import com.robonobo.core.api.proto.CoreApi.UserMsg;
 
 /**
  * Managers users (me and my friends) and associated playlists. We pull everything down via http on startup (and update
  * it periodically); nothing is persisted locally
  */
 @SuppressWarnings("unchecked")
 public class UserService extends AbstractService {
 	Log log = LogFactory.getLog(getClass());
 	private User me;
 	private UserConfig myUserCfg;
 	MetadataServerConfig msc;
 	/**
 	 * We keep users and playlists in a hashmap, and look them up on demand. This is because they are being updated
 	 * asynchronously, and so if we kept pointers, they'd go out of date.
 	 */
 	private Map<String, User> usersByEmail = Collections.synchronizedMap(new HashMap<String, User>());
 	private Map<Long, User> usersById = Collections.synchronizedMap(new HashMap<Long, User>());
 	private Map<Long, Playlist> playlists = Collections.synchronizedMap(new HashMap<Long, Playlist>());
 	private Map<String, Long> myPlaylistIdsByTitle = Collections.synchronizedMap(new HashMap<String, Long>());
 	private ScheduledFuture updateScheduler;
 	private ReentrantLock userUpdateLock = new ReentrantLock();
 	private ReentrantLock startupLock = new ReentrantLock();
 	private Condition startupCondition = startupLock.newCondition();
 	private boolean started = false;
 
 	public UserService() {
 		addHardDependency("core.metadata");
 		addHardDependency("core.storage");
 		addHardDependency("core.tasks");
 	}
 
 	@Override
 	public void startup() throws Exception {
 		int updateFreq = rbnb.getConfig().getUserUpdateFrequency();
 		updateScheduler = rbnb.getExecutor().scheduleAtFixedRate(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				rbnb.getTaskService().runTask(new UpdateTask());
 			}
 		}, updateFreq, updateFreq, TimeUnit.SECONDS);
 		started = true;
 		startupLock.lock();
 		try {
 			startupCondition.signalAll();
 		} finally {
 			startupLock.unlock();
 		}
 	}
 
 	public String getName() {
 		return "User service";
 	}
 
 	public String getProvides() {
 		return "core.users";
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		if (updateScheduler != null)
 			updateScheduler.cancel(true);
 	}
 
 	public void checkAllUsersUpdate() {
 		rbnb.getTaskService().runTask(new UpdateTask());
 	}
 
 	public void login(final String email, final String password) throws IOException, SerializationException {
 		// We get called immediately here, which might be before we've started... wait, if so
 		if (!started) {
 			startupLock.lock();
 			try {
 				try {
 					log.debug("Waiting to login until user service is started");
 					startupCondition.await();
 				} catch (InterruptedException e) {
 					return;
 				}
 			} finally {
 				startupLock.unlock();
 			}
 		}
 		MetadataServerConfig msc = new MetadataServerConfig(rbnb.getConfig().getMetadataServerUrl());
 		log.info("Attempting login as user " + email);
 		try {
 			UserMsg.Builder ub = UserMsg.newBuilder();
 			// If the details are wrong, this will chuck UnauthorizedException
 			SerializationManager sm = rbnb.getSerializationManager();
 			sm.setCreds(email, password);
 			sm.getObjectFromUrl(ub, msc.getUserUrl(email));
 			User tryUser = new User(ub.build());
 			tryUser.setPassword(password);
 			rbnb.getConfig().setMetadataServerUsername(email);
 			rbnb.getConfig().setMetadataServerPassword(password);
 			rbnb.saveConfig();
 			// Reload everything again
 			usersByEmail.clear();
 			usersById.clear();
 			playlists.clear();
 			myPlaylistIdsByTitle.clear();
 			usersByEmail.put(email, tryUser);
 			usersById.put(tryUser.getUserId(), tryUser);
 			this.msc = msc;
 			me = tryUser;
 			log.info("Login as " + email + " successful");
 			rbnb.getExecutor().execute(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					rbnb.getEventService().fireLoggedIn();
 					synchronized (UserService.this) {
 						usersByEmail.put(me.getEmail(), me);
 						usersById.put(me.getUserId(), me);
 					}
 					rbnb.getEventService().fireUserChanged(me);
 					rbnb.getTaskService().runTask(new InitialFetchTask());
 				}
 			});
			if (rbnb.getMina() != null & rbnb.getMina().isConnectedToSupernode()) {
 				rbnb.setStatus(RobonoboStatus.Connected);
 				rbnb.getEventService().fireStatusChanged();
 			}
 		} catch (IOException e) {
 			log.error("Caught exception logging in", e);
 			throw e;
 		} catch (SerializationException e) {
 			log.error("Caught exception logging in", e);
 			throw e;
 		}
 	}
 
 	public void saveUserConfigItem(String itemName, String itemValue) {
 		if (me == null) {
 			log.error("Error: tried to save user config, but I am not logged in");
 			return;
 		}
 		UserConfig cfg = new UserConfig();
 		cfg.setUserId(me.getUserId());
 		cfg.getItems().put(itemName, itemValue);
 		try {
 			rbnb.getSerializationManager().putObjectToUrl(cfg.toMsg(), msc.getUserConfigUrl(me.getUserId()));
 		} catch (IOException e) {
 			log.error("Errot saving user config", e);
 		}
 	}
 
 	public void requestTopUp() throws IOException {
 		if (me != null) {
 			try {
 				rbnb.getSerializationManager().hitUrl(msc.getTopUpUrl());
 			} catch (IOException e) {
 				log.error("Error requesting topup", e);
 				throw e;
 			}
 		}
 	}
 
 	public boolean isLoggedIn() {
 		return me != null;
 	}
 
 	public User getMyUser() {
 		if (me == null)
 			return null;
 		return getUser(me.getEmail());
 	}
 
 	public UserConfig getMyUserConfig() {
 		return myUserCfg;
 	}
 
 	public UserConfig refreshMyUserConfig() {
 		UserConfigMsg.Builder b = UserConfigMsg.newBuilder();
 		try {
 			rbnb.getSerializationManager().getObjectFromUrl(b, msc.getUserConfigUrl(me.getUserId()));
 		} catch (Exception e) {
 			log.error("Caught exception fetching user config", e);
 			return null;
 		}
 		myUserCfg = new UserConfig(b.build());
 		rbnb.getEventService().fireUserConfigChanged(myUserCfg);
 		return myUserCfg;
 	}
 
 	public MetadataServerConfig getMsc() {
 		return msc;
 	}
 
 	public void updateMyUser(User u) throws IOException {
 		if (u.getEmail() != me.getEmail()) {
 			throw new SeekInnerCalmException();
 		}
 		userUpdateLock.lock();
 		try {
 			rbnb.getSerializationManager().putObjectToUrl(u.toMsg(false), msc.getUserUrl(u.getEmail()));
 			synchronized (this) {
 				me = u;
 				usersByEmail.put(u.getEmail(), u);
 				usersById.put(u.getUserId(), u);
 			}
 		} finally {
 			userUpdateLock.unlock();
 		}
 		rbnb.getEventService().fireUserChanged(u);
 	}
 
 	/**
 	 * Returns the playlist with the playlist id set
 	 */
 	public Playlist addOrUpdatePlaylist(Playlist newP) throws IOException, RobonoboException {
 		String playlistUrl = msc.getPlaylistUrl(newP.getPlaylistId());
 		if (newP.getPlaylistId() <= 0) {
 			// New playlist - the server will send it back with the playlist id set
 			PlaylistMsg.Builder bldr = PlaylistMsg.newBuilder();
 			rbnb.getSerializationManager().putObjectToUrl(newP.toMsg(), playlistUrl, bldr);
 			Playlist updatedP = new Playlist(bldr.build());
 			// Grab a new copy of my user, it'll have the new playlist in it
 			checkUserUpdate(me, false);
 			// Fire this playlist as being updated
 			rbnb.getEventService().firePlaylistChanged(updatedP);
 			return updatedP;
 		} else {
 			rbnb.getSerializationManager().putObjectToUrl(newP.toMsg(), playlistUrl);
 			rbnb.getEventService().firePlaylistChanged(newP);
 			return newP;
 		}
 	}
 
 	public void nukePlaylist(Playlist pl) throws IOException, RobonoboException {
 		rbnb.getSerializationManager().deleteObjectAtUrl(msc.getPlaylistUrl(pl.getPlaylistId()));
 		myPlaylistIdsByTitle.remove(pl.getTitle());
 		// Get a new copy of my user without this playlist
 		checkUserUpdate(me, false);
 		// If we were sharing this with any of our friends, update it so that we
 		// are no longer registered as an owner
 		boolean sharing = false;
 		synchronized (this) {
 			for (Long friendId : me.getFriendIds()) {
 				User friend = getUser(friendId);
 				if (friend.getPlaylistIds().contains(pl.getPlaylistId())) {
 					sharing = true;
 					break;
 				}
 			}
 		}
 		if (sharing) {
 			try {
 				checkPlaylistUpdate(pl.getPlaylistId());
 			} catch (RobonoboException e) {
 				log.error("Error checking updated playlist after delete", e);
 			}
 		}
 	}
 
 	public void sharePlaylist(Playlist p, Set<Long> friendIds, Set<String> emails) throws IOException,
 			RobonoboException {
 		rbnb.getSerializationManager().hitUrl(msc.getSharePlaylistUrl(p.getPlaylistId(), friendIds, emails));
 		List<User> friends = new ArrayList<User>(friendIds.size());
 		for (Long friendId : friendIds) {
 			friends.add(getUser(friendId));
 		}
 		checkUserUpdate(friends);
 	}
 
 	public void postFacebookUpdate(long playlistId, String msg) throws IOException {
 		rbnb.getSerializationManager().hitUrl(msc.getFacebookPlaylistUrl(playlistId, msg));
 	}
 
 	public void postTwitterUpdate(long playlistId, String msg) throws IOException {
 		rbnb.getSerializationManager().hitUrl(msc.getTwitterPlaylistUrl(playlistId, msg));
 	}
 
 	public synchronized User getUser(String email) {
 		return usersByEmail.get(email);
 	}
 
 	public synchronized User getUser(long userId) {
 		return usersById.get(userId);
 	}
 
 	public synchronized Playlist getExistingPlaylist(long playlistId) {
 		return playlists.get(playlistId);
 	}
 
 	public Playlist getOrFetchPlaylist(long plId) {
 		Playlist p = getExistingPlaylist(plId);
 		if (p != null)
 			return p;
 		try {
 			p = getUpdatedPlaylist(plId);
 		} catch (Exception e) {
 			log.error("Error fetching playlist with pId " + plId, e);
 			return null;
 		}
 		synchronized (this) {
 			playlists.put(plId, p);
 		}
 		return p;
 	}
 
 	public synchronized Playlist getMyPlaylistByTitle(String title) {
 		Long plId = myPlaylistIdsByTitle.get(title);
 		if (plId == null)
 			return null;
 		return getExistingPlaylist(plId);
 	}
 
 	private User getUpdatedUser(long userId) throws IOException, SerializationException {
 		UserMsg.Builder ub = UserMsg.newBuilder();
 		rbnb.getSerializationManager().getObjectFromUrl(ub, msc.getUserUrl(userId));
 		return new User(ub.build());
 	}
 
 	private Playlist getUpdatedPlaylist(long playlistId) throws IOException, SerializationException {
 		String playlistUrl = msc.getPlaylistUrl(playlistId);
 		PlaylistMsg.Builder pb = PlaylistMsg.newBuilder();
 		rbnb.getSerializationManager().getObjectFromUrl(pb, playlistUrl);
 		return new Playlist(pb.build());
 	}
 
 	public void checkPlaylistUpdate(long playlistId) throws IOException, RobonoboException {
 		Playlist currentP = playlists.get(playlistId);
 		final Playlist updatedP = getUpdatedPlaylist(playlistId);
 		PlaylistConfig pc = rbnb.getDbService().getPlaylistConfig(playlistId);
 		if (currentP == null || currentP.getUpdated() == null || updatedP.getUpdated().after(currentP.getUpdated())) {
 			// Make sure we have copies of all streams
 			for (String sid : updatedP.getStreamIds()) {
 				rbnb.getMetadataService().getStream(sid);
 			}
 			playlists.put(playlistId, updatedP);
 			if (me.getPlaylistIds().contains(updatedP.getPlaylistId())) {
 				if (currentP != null)
 					myPlaylistIdsByTitle.remove(currentP.getTitle());
 				myPlaylistIdsByTitle.put(updatedP.getTitle(), updatedP.getPlaylistId());
 			}
 			rbnb.getEventService().firePlaylistChanged(updatedP);
 		}
 		if (((pc != null) && "true".equalsIgnoreCase(pc.getItem("autoDownload")))) {
 			for (String streamId : updatedP.getStreamIds()) {
 				Track t = rbnb.getTrackService().getTrack(streamId);
 				if (t instanceof CloudTrack)
 					rbnb.getDownloadService().addDownload(streamId);
 			}
 		}
 		if (pc != null && "true".equalsIgnoreCase(pc.getItem("iTunesExport"))) {
 			final List<User> contUsers = new ArrayList<User>();
 			synchronized (this) {
 				for (User u : usersById.values()) {
 					if (u.getPlaylistIds().contains(playlistId))
 						contUsers.add(u);
 				}
 			}
 			// Update itunes in another thread
 			getRobonobo().getExecutor().execute(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					for (User u : contUsers) {
 						getRobonobo().getITunesService().syncPlaylist(u, updatedP);
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Update things that need to be updated on playlists containing this track we're now sharing
 	 */
 	public void checkPlaylistsForNewShare(SharedTrack sh) {
 		// Currently, just sync to itunes if necessary
 		final Map<User, List<Playlist>> contPls = new HashMap<User, List<Playlist>>();
 		synchronized (this) {
 			for (User u : usersById.values()) {
 				for (Long plId : u.getPlaylistIds()) {
 					PlaylistConfig pc = getRobonobo().getDbService().getPlaylistConfig(plId);
 					if ("true".equalsIgnoreCase(pc.getItem("iTunesExport"))) {
 						if (!contPls.containsKey(u))
 							contPls.put(u, new ArrayList<Playlist>());
 						contPls.get(u).add(getExistingPlaylist(plId));
 					}
 				}
 			}
 		}
 		// Come out of sync and do this in another thread, it'll take a while
 		getRobonobo().getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				for (User u : contPls.keySet()) {
 					for (Playlist p : contPls.get(u)) {
 						getRobonobo().getITunesService().syncPlaylist(u, p);
 					}
 				}
 			}
 		});
 	}
 
 	private void lookupNewUser(long userId) throws RobonoboException, IOException {
 		User u;
 		try {
 			u = getUpdatedUser(userId);
 		} catch (Exception e) {
 			throw new RobonoboException(e);
 		}
 		if (u == null)
 			return;
 		synchronized (this) {
 			usersByEmail.put(u.getEmail(), u);
 			usersById.put(u.getUserId(), u);
 		}
 		rbnb.getEventService().fireUserChanged(u);
 		for (Long playlistId : u.getPlaylistIds()) {
 			// For shared playlists, don't hit the server again if we already have it - but fire the playlist
 			// changed event so it's added to the friend tree
 			// for this user
 			Playlist p = getExistingPlaylist(playlistId);
 			if (p == null)
 				checkPlaylistUpdate(playlistId);
 			else
 				rbnb.getEventService().firePlaylistChanged(p);
 		}
 		for (long friendId : u.getFriendIds()) {
 			if (!usersById.containsKey(friendId)) {
 				lookupNewUser(userId);
 			}
 		}
 	}
 
 	private void checkUserUpdate(User u, boolean cascade) throws IOException, RobonoboException {
 		User newU = getUpdatedUser(u.getUserId());
 		boolean isUpdated = (newU.getUpdated() == null && u.getUpdated() == null)
 				|| newU.getUpdated().after(u.getUpdated());
 		if (isUpdated) {
 			synchronized (this) {
 				if (newU.equals(me)) {
 					newU.setPassword(me.getPassword());
 					me = newU;
 				}
 				usersByEmail.put(newU.getEmail(), newU);
 				usersById.put(newU.getUserId(), newU);
 			}
 			rbnb.getEventService().fireUserChanged(newU);
 		}
 		if (cascade) {
 			for (Long playlistId : newU.getPlaylistIds()) {
 				checkPlaylistUpdate(playlistId);
 			}
 		}
 		if (isUpdated) {
 			for (long friendId : newU.getFriendIds()) {
 				if (!usersById.containsKey(friendId))
 					lookupNewUser(friendId);
 			}
 		}
 	}
 
 	/**
 	 * This updates all the users before updating the playlists, so if they share playlists they all catch the changes
 	 * 
 	 * @throws RobonoboException
 	 * @throws IOException
 	 */
 	private void checkUserUpdate(Collection<User> users) throws IOException, RobonoboException {
 		Set<Long> playlistsToCheck = new HashSet<Long>();
 		for (User u : users) {
 			User newU = getUpdatedUser(u.getUserId());
 			if ((newU.getUpdated() == null && u.getUpdated() == null) || newU.getUpdated().after(u.getUpdated())) {
 				for (long friendId : newU.getFriendIds()) {
 					if (!usersById.containsKey(friendId))
 						lookupNewUser(friendId);
 				}
 				synchronized (UserService.this) {
 					usersByEmail.put(newU.getEmail(), newU);
 					usersById.put(newU.getUserId(), newU);
 				}
 				rbnb.getEventService().fireUserChanged(newU);
 			}
 			for (Long playlistId : newU.getPlaylistIds()) {
 				playlistsToCheck.add(playlistId);
 			}
 		}
 		for (Long playlistId : playlistsToCheck) {
 			checkPlaylistUpdate(playlistId);
 		}
 	}
 
 	class InitialFetchTask extends Task {
 		public InitialFetchTask() {
 			title = "Fetching friends and playlists";
 		}
 
 		public void doRun() throws Exception {
 			log.info("Fetching initial user & playlist info");
 			Set<Long> playlistIds = me.getPlaylistIds();
 			Set<Long> friendIds = me.getFriendIds();
 			// Have to estimate a percentage for completion, so we assume that everyone has as many playlists as we do
 			int psPerU = playlistIds.size();
 			if (psPerU == 0)
 				psPerU = 1;
 			int totalSteps = ((friendIds.size() + 1) * psPerU) + 1;
 			int stepsDone = 0;
 			int playlistsDone = 0;
 			for (long pId : playlistIds) {
 				statusText = "Fetching playlist " + (playlistsDone + 1) + " of " + playlistIds.size();
 				fireUpdated();
 				checkPlaylistUpdate(pId);
 				stepsDone++;
 				playlistsDone++;
 				completion = (float) stepsDone / totalSteps;
 			}
 			int friendsDone = 0;
 			for (long uId : friendIds) {
 				statusText = "Fetching friend " + (friendsDone + 1) + " of " + friendIds.size();
 				fireUpdated();
 				lookupNewUser(uId);
 				friendsDone++;
 				stepsDone += psPerU;
 				completion = (float) stepsDone / totalSteps;
 			}
 			statusText = "Fetching my user config";
 			fireUpdated();
 			refreshMyUserConfig();
 			statusText = "Done.";
 			completion = 1f;
 			fireUpdated();
 			rbnb.getEventService().fireAllUsersAndPlaylistsUpdated();
 		}
 	}
 
 	class UpdateTask extends Task {
 		public UpdateTask() {
 			title = "Updating friends and playlists";
 		}
 
 		public void doRun() throws Exception {
 			if (me == null) {
 				return;
 			}
 			log.info("Fetching updated info for all users & playlists");
 			// Copy out users so we can iterate over them safely
 			User[] uArr = new User[usersByEmail.size()];
 			usersByEmail.values().toArray(uArr);
 			for (int i = 0; i < uArr.length; i++) {
 				if (cancelRequested) {
 					cancelled = true;
 					fireUpdated();
 					return;
 				}
 				User u = uArr[i];
 				completion = (float) i / uArr.length;
 				statusText = "Fetching user " + u.getEmail();
 				fireUpdated();
 				if (me.equals(u)) {
 					// This is me - check to see if we're currently updating my
 					// user, and if we are, don't bother to check for updates -
 					// just leave it until next time
 					if (userUpdateLock.isLocked())
 						continue;
 				}
 				checkUserUpdate(u, true);
 			}
 			statusText = "Done.";
 			completion = 1f;
 			fireUpdated();
 			rbnb.getEventService().fireAllUsersAndPlaylistsUpdated();
 		}
 	}
 }
