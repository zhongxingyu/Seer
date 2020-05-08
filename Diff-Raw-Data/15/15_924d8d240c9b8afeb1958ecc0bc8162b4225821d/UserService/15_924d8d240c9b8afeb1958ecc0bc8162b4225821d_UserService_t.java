 package com.robonobo.core.service;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.Errot;
 import com.robonobo.common.serialization.*;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.config.RobonoboConfig;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
 import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;
 import com.robonobo.core.api.proto.CoreApi.UserMsg;
 import com.robonobo.core.metadata.*;
 import com.robonobo.core.metadata.AbstractMetadataService.RequestFetchOrder;
 import com.robonobo.core.wang.RobonoboWangConfig;
 
 /**
  * Managers users (me and my friends) and associated playlists. We pull everything down via http on startup (and update
  * it periodically); nothing is persisted locally
  */
 public class UserService extends AbstractService {
 	EventService events;
 	TaskService tasks;
 	DbService db;
 	StreamService streams;
 	DownloadService downloads;
 	AbstractMetadataService metadata;
 	PlaylistService playlists;
 	private User me;
 	private UserConfig myUserCfg;
 	/**
 	 * We keep users and playlists in a hashmap, and look them up on demand. This is because they are being updated
 	 * asynchronously, and so if we kept pointers, they'd go out of date.
 	 */
 	private Map<String, User> usersByEmail = Collections.synchronizedMap(new HashMap<String, User>());
 	private Map<Long, User> usersById = Collections.synchronizedMap(new HashMap<Long, User>());
 	private ScheduledFuture<?> updateTask;
 	private ReentrantLock startupLock = new ReentrantLock();
 	private Condition startupCondition = startupLock.newCondition();
 	private boolean started = false;
 
 	public UserService() {
 		addHardDependency("core.db");
 		addHardDependency("core.metadata");
 		addHardDependency("core.streams");
 		addHardDependency("core.storage");
 		addHardDependency("core.tasks");
 		addHardDependency("core.tracks");
 		addHardDependency("core.playlists");
 	}
 
 	@Override
 	public void startup() throws Exception {
 		events = rbnb.getEventService();
 		tasks = rbnb.getTaskService();
 		db = rbnb.getDbService();
 		streams = rbnb.getStreamService();
 		downloads = rbnb.getDownloadService();
 		metadata = rbnb.getMetadataService();
 		playlists = rbnb.getPlaylistService();
 		started = true;
 		startupLock.lock();
 		try {
 			startupCondition.signalAll();
 		} finally {
 			startupLock.unlock();
 		}
 	}
 
 	public String getName() {
 		return "User Service";
 	}
 
 	public String getProvides() {
 		return "core.users";
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		if (updateTask != null)
 			updateTask.cancel(true);
 	}
 
 	public void checkAllUsersUpdate() {
 		resetUpdateTask();
 		tasks.runTask(new UpdateTask());
 	}
 
 	private void resetUpdateTask() {
 		if (updateTask != null)
 			updateTask.cancel(false);
 		int updateFreq = rbnb.getConfig().getUserUpdateFrequency();
 		updateTask = rbnb.getExecutor().scheduleAtFixedRate(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				tasks.runTask(new UpdateTask());
 			}
 		}, updateFreq, updateFreq, TimeUnit.SECONDS);
 	}
 
 	/**
 	 * This will return immediately (or as soon as the service is started) - to see the result, add a LoginListener
 	 * before you call this
 	 */
 	public void login(String email, String password) {
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
 		log.info("Attempting login as user " + email);
 		metadata.fetchUserForLogin(email, password, new LoginHandler(email, password));
 	}
 
 	class LoginHandler implements UserCallback {
 		String email;
 		String password;
 
 		public LoginHandler(String email, String password) {
 			this.email = email;
 			this.password = password;
 		}
 
 		@Override
 		public void success(User u) {
 			log.info("Login as " + email + " successful");
 			resetUpdateTask();
 			metadata.setCredentials(email, password);
 			RobonoboConfig rbnbCfg = rbnb.getConfig();
 			rbnbCfg.setMetadataUsername(email);
 			rbnbCfg.setMetadataPassword(password);
 			RobonoboWangConfig wangCfg = (RobonoboWangConfig) rbnb.getConfig("wang");
 			wangCfg.setAccountEmail(email);
 			wangCfg.setAccountPwd(password);
 			rbnb.saveConfig();
 			// Reload all our users
 			synchronized (UserService.this) {
 				usersByEmail.clear();
 				usersById.clear();
 				usersByEmail.put(email, u);
 				usersById.put(u.getUserId(), u);
 				u.setPassword(password);
 				me = u;
 			}
 			events.fireLoginSucceeded(u);
 			events.fireUserChanged(u);
 			if(rbnb.getMina().isConnectedToSupernode()) {
 				rbnb.setStatus(RobonoboStatus.Connected);
 				events.fireStatusChanged();
 			}
 				
 			metadata.fetchUserConfig(me.getUserId(), new UsrCfgUpdater(null));
 			// Tell our metadata service to load things serially so we get playlists loading one at a time rather than a
 			// big pause then all loading at once
 			metadata.setFetchOrder(RequestFetchOrder.Serial);
 			if (rbnbCfg.isAgoric())
 				rbnb.getWangService().loggedIn();
			// If we have any playlists, fetch them now (fetchFriends() will be called when they're done)
			if(me.getPlaylistIds().size() > 0)
				playlists.refreshMyPlaylists(me);
			else
				fetchFriends();
 		}
 
 		@Override
 		public void error(long userId, Exception e) {
 			if (e instanceof UnauthorizedException) {
 				log.error("Login failed");
 				events.fireLoginFailed("Login failed");
 			} else {
 				log.error("Caught exception logging in", e);
 				events.fireLoginFailed("Server error");
 			}
 		}
 	}
 
 	public void saveUserConfigItem(String itemName, String itemValue) {
 		log.debug("Saving user config");
 		UserConfig cfg = new UserConfig();
 		cfg.setUserId(me.getUserId());
 		cfg.getItems().put(itemName, itemValue);
 		metadata.updateUserConfig(cfg, new UserConfigCallback() {
 			public void success(UserConfig uc) {
 				synchronized (UserService.this) {
 					myUserCfg = uc;
 				}
 			}
 
 			public void error(long userId, Exception e) {
 				log.error("Error updating user config", e);
 			}
 		});
 	}
 
 	public void addFriends(final Collection<String> emails) {
 		metadata.addFriends(emails, new GeneralCallback() {
 			public void success() {
 				log.info("Successfully sent friend request to "+emails.size()+" friends");
 			}
 			
 			public void error(Exception e) {
 				log.error("Error adding friends", e);
 			}
 		});
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
 
 	public void refreshMyUserConfig(UserConfigCallback handler) {
 		metadata.fetchUserConfig(me.getUserId(), handler);
 	}
 
 	public synchronized User getUser(String email) {
 		return usersByEmail.get(email);
 	}
 
 	public synchronized User getUser(long userId) {
 		return usersById.get(userId);
 	}
 
 	void fetchFriends() {
 		if (me.getFriendIds().size() > 0)
 			tasks.runTask(new FriendFetchTask(me));
 		// else awwwwww...
 	}
 
 	void playlistCreated(Playlist newP) {
 		synchronized (this) {
 			me.getPlaylistIds().add(newP.getPlaylistId());
 		}
 		events.fireUserChanged(me);
 	}
 
 	void playlistDeleted(Playlist p) {
 		synchronized (this) {
 			if (!me.getPlaylistIds().contains(p.getPlaylistId())) {
 				log.error("User service asked to delete playlist " + p.getPlaylistId() + ", but I am not an owner");
 				return;
 			}
 			me.getPlaylistIds().remove(p.getPlaylistId());
 		}
 		events.fireUserChanged(me);
 	}
 
 	void playlistShared(Playlist p, Collection<Long> friendIds) {
 		long plId = p.getPlaylistId();
 		List<User> friends = new ArrayList<User>();
 		synchronized (this) {
 			for (Long fid : friendIds) {
 				User friend = usersById.get(fid);
 				if (friend == null) {
 					log.error("Playlist shared with friend id " + fid + " but no such user exists");
 					continue;
 				}
 				friend.getPlaylistIds().add(plId);
 				friends.add(friend);
 			}
 		}
 		for (User friend : friends) {
 			events.fireUserChanged(friend);
 		}
 	}
 
 	class UsrCfgUpdater implements UserConfigCallback {
 		UserConfigCallback onwardHandler;
 
 		public UsrCfgUpdater(UserConfigCallback onwardHandler) {
 			this.onwardHandler = onwardHandler;
 		}
 
 		@Override
 		public void success(UserConfig uc) {
 			log.debug("Got new user config");
 			myUserCfg = uc;
 			events.fireUserConfigChanged(uc);
 			if (onwardHandler != null)
 				onwardHandler.success(uc);
 		}
 
 		@Override
 		public void error(long userId, Exception e) {
 			log.error("Error fetching my user config", e);
 			if (onwardHandler != null)
 				onwardHandler.error(userId, e);
 		}
 	}
 
 	class FriendFetchTask extends Task implements UserCallback {
 		User u;
 		int usersDone = 0;
 		Set<Long> playlistIds = new HashSet<Long>();
 		Set<Long> newPlIds = new HashSet<Long>();
 
 		public FriendFetchTask(User u) {
 			title = "Updating friend details";
 			this.u = u;
 		}
 
 		@Override
 		public void runTask() throws Exception {
 			log.info("Fetching friends");
 			update();
 			metadata.fetchUsers(u.getFriendIds(), this);
 		}
 
 		@Override
 		public void success(User u) {
 			synchronized (this) {
 				usersByEmail.put(u.getEmail(), u);
 				usersById.put(u.getUserId(), u);
 			}
 			// NB we can check here to see if any playlists have been deleted, but with this whole asynchronous malarky
 			// it's very tough to tell that a playlist isn't needed at all any more - we just live with any deleted
 			// playlists floating around
 			events.fireUserChanged(u);
 			// Get all the users before fetching the playlists, so that if there are shared playlists they all get
 			// updated
 			playlistIds.addAll(u.getPlaylistIds());
 			usersDone++;
 			update();
 		}
 
 		@Override
 		public void error(long userId, Exception e) {
 			usersDone++;
 			update();
 			log.error("Caught exception fetching user id " + userId, e);
 		}
 
 		private void update() {
 			if (usersDone == u.getFriendIds().size()) {
 				statusText = "Done.";
 				completion = 1f;
 				// Now we've fetched all the users, we can fetch their playlists
 				if (playlistIds.size() > 0)
 					playlists.refreshFriendPlaylists(playlistIds);
 			} else {
 				statusText = "Fetching friend " + (usersDone + 1) + " of " + u.getFriendIds().size();
 				completion = ((float) usersDone) / u.getFriendIds().size();
 			}
 			fireUpdated();
 		}
 	}
 
 	class UpdateTask extends Task {
 		public UpdateTask() {
 			title = "Fetching updated user details";
 		}
 
 		public void runTask() throws Exception {
 			if (me == null) {
 				return;
 			}
 			log.info("Running user update");
 			metadata.fetchUser(me.getUserId(), new UserCallback() {
 				public void success(User u) {
 					log.debug("Successfully retrieved my user details");
 					completion = 1f;
 					statusText = "Done.";
 					fireUpdated();
 					synchronized (UserService.this) {
 						me = u;
 					}
 					events.fireUserChanged(u);
 					tasks.runTask(new FriendFetchTask(u));
 				}
 
 				public void error(long userId, Exception e) {
 					log.error("Error fetching my updated user", e);
 				}
 			});
 		}
 	}
 }
