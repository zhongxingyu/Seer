 package com.robonobo.core.service;
 
 import java.net.*;
 import java.util.*;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.serialization.TemporarilyUnavailableException;
 import com.robonobo.common.serialization.UnauthorizedException;
 import com.robonobo.common.util.CodeUtil;
 import com.robonobo.core.api.RobonoboStatus;
 import com.robonobo.core.api.Task;
 import com.robonobo.core.api.config.RobonoboConfig;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.metadata.*;
 import com.robonobo.core.metadata.AbstractMetadataService.RequestFetchOrder;
 import com.robonobo.core.wang.RobonoboWangConfig;
 
 /** Managers users (me and my friends) and associated playlists. We pull everything down via http on startup (and update
  * it periodically); nothing is persisted locally */
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
 	/** We keep users and playlists in a hashmap, and look them up on demand. This is because they are being updated
 	 * asynchronously, and so if we kept pointers, they'd go out of date. */
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
 
 	/** This will return immediately (or as soon as the service is started) - to see the result, add a LoginListener
 	 * before you call this */
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
 			// Clear our last-fetch comments
 			rbnb.getCommentService().lastFetched.clear();
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
 			if (rbnb.getMina().isConnectedToSupernode()) {
 				rbnb.setStatus(RobonoboStatus.Connected);
 				events.fireStatusChanged();
 			}
 			metadata.fetchUserConfig(me.getUserId(), new UserConfigUpdater(null));
 			// Tell our metadata service to load things serially so we get playlists loading one at a time rather than a
 			// big pause then all loading at once
 			metadata.setFetchOrder(RequestFetchOrder.Serial);
 			if (rbnbCfg.isAgoric())
 				rbnb.getWangService().loggedIn();
 			// If we have any playlists, fetch them now (fetchFriends() will be called when they're done)
 			playlists.clearPlaylists();
			if (me.getPlaylistIds().size() > 0)
				playlists.refreshMyPlaylists(me);
			else
				fetchFriends();
 			rbnb.getShareService().startFetchingComments();
 		}
 
 		@Override
 		public void error(long userId, Exception e) {
 			if (e instanceof UnauthorizedException) {
 				log.error("Login failed");
 				events.fireLoginFailed("Login failed");
 			} else if (e instanceof TemporarilyUnavailableException) {
 				log.error("Login server temporarily unavailable");
 				events.fireLoginFailed("Down for maintenance, please wait a few minutes");
 			} else if (e instanceof UnknownHostException || e instanceof NoRouteToHostException || e instanceof SocketException) {
 				log.error("Cannot connect to midas server " + rbnb.getConfig().getMidasUrl() + " - caught " + CodeUtil.shortClassName(e.getClass()));
 				events.fireLoginFailed("Cannot connect to server");
 			} else {
 				log.error("Caught exception logging in", e);
 				events.fireLoginFailed("Server error");
 			}
 		}
 	}
 
 	public void saveUserConfigItem(final String itemName, final String itemValue) {
 		log.debug("Saving user config");
 		UserConfig cfg = new UserConfig();
 		cfg.setUserId(me.getUserId());
 		cfg.putItem(itemName, itemValue);
 		metadata.updateUserConfig(cfg, new UserConfigCallback() {
 			public void success(UserConfig uc) {
 				synchronized (UserService.this) {
 					if(myUserCfg == null)
 						myUserCfg = uc;
 					else
 						myUserCfg.putItem(itemName, itemValue);
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
 				log.info("Successfully sent friend request to " + emails.size() + " friends");
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
 		return me;
 	}
 
 	public UserConfig getMyUserConfig() {
 		return myUserCfg;
 	}
 
 	public void refreshMyUserConfig(UserConfigCallback callback) {
 		metadata.fetchUserConfig(me.getUserId(), new UserConfigUpdater(callback));
 	}
 
 	/** Polls the server for our user config every minute for ten mins - we're expecting an update */
 	public void watchMyUserConfig() {
 		int minsToWatch = 10;
 		Runnable r = new CatchingRunnable() {
 			public void doRun() throws Exception {
 				refreshMyUserConfig(new UserConfigUpdater(null));
 			}
 		};
 		for (int i = 1; i <= minsToWatch; i++) {
 			rbnb.getExecutor().schedule(r, i, TimeUnit.MINUTES);
 		}
 	}
 
 	public synchronized User getKnownUser(String email) {
 		return usersByEmail.get(email);
 	}
 
 	public synchronized User getKnownUser(long userId) {
 		return usersById.get(userId);
 	}
 
 	public void getOrFetchUser(long userId, final UserCallback cb) {
 		User u;
 		synchronized (this) {
 			u = usersById.get(userId);
 		}
 		if (u != null) {
 			cb.success(u);
 			return;
 		}
 		metadata.fetchUser(userId, new UserCallback() {
 			@Override
 			public void success(User u) {
 				synchronized (UserService.this) {
 					usersById.put(u.getUserId(), u);
 				}
 				cb.success(u);
 			}
 
 			@Override
 			public void error(long userId, Exception e) {
 				cb.error(userId, e);
 			}
 		});
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
 
 	class UserConfigUpdater implements UserConfigCallback {
 		UserConfigCallback onwardHandler;
 
 		public UserConfigUpdater(UserConfigCallback onwardHandler) {
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
 		User myUser;
 		int usersDone = 0;
 		Set<Long> playlistIds = new HashSet<Long>();
 		Set<Long> newPlIds = new HashSet<Long>();
 
 		public FriendFetchTask(User myUser) {
 			title = "Updating friend details";
 			this.myUser = myUser;
 		}
 
 		@Override
 		public void runTask() throws Exception {
 			log.info("Fetching friends");
 			update();
 			metadata.fetchUsers(myUser.getFriendIds(), this);
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
 			for (Long plId : u.getPlaylistIds()) {
 				// Any playlists shared with both me and my friends, don't fetch them twice
 				if (!myUser.getPlaylistIds().contains(plId))
 					playlistIds.add(plId);
 			}
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
 			if (usersDone == myUser.getFriendIds().size()) {
 				statusText = "Done.";
 				completion = 1f;
 				// Now we've fetched all the users, we can fetch their playlists
 				Set<Long> myPlIds = new HashSet<Long>();
 				Set<Long> myFriendIds = new HashSet<Long>();
 				synchronized (UserService.this) {
 					myPlIds.addAll(me.getPlaylistIds());
 					myFriendIds.addAll(me.getFriendIds());
 				}
 				// First, if any of our own playlists are shared with our friends, fire them as updated now that the
 				// friend is loaded
 				plLoop: for (Long plId : myPlIds) {
 					Playlist p = playlists.getExistingPlaylist(plId);
 					for (Long ownerId : p.getOwnerIds()) {
 						if (myFriendIds.contains(ownerId)) {
 							events.firePlaylistChanged(p);
 							continue plLoop;
 						}
 					}
 				}
 				if (playlistIds.size() > 0)
 					playlists.refreshFriendPlaylists(playlistIds);
 				else
 					rbnb.getLibraryService().updateFriendLibraries();
 			} else {
 				statusText = "Fetching friend " + (usersDone + 1) + " of " + myUser.getFriendIds().size();
 				completion = ((float) usersDone) / myUser.getFriendIds().size();
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
 					if (me.getPlaylistIds().size() > 0)
 						playlists.refreshMyPlaylists(me);
 					else
 						fetchFriends();
 				}
 
 				public void error(long userId, Exception e) {
 					log.error("Error fetching my updated user", e);
 				}
 			});
 		}
 	}
 }
