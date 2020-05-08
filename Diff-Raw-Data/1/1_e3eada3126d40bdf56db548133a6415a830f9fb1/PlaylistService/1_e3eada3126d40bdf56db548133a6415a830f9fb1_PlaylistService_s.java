 package com.robonobo.core.service;
 
 import java.io.IOException;
 import java.util.*;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.Errot;
 import com.robonobo.core.Platform;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.Task;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.metadata.*;
 import com.robonobo.core.metadata.AbstractMetadataService.RequestFetchOrder;
 
 public class PlaylistService extends AbstractService {
 	Map<Long, Playlist> playlists = new HashMap<Long, Playlist>();
 	Map<String, Long> myPlaylistIdsByTitle = new HashMap<String, Long>();
 	Set<Long> forceUpdatePlaylists = new HashSet<Long>();
 	TaskService tasks;
 	DbService db;
 	AbstractMetadataService metadata;
 	EventService events;
 	StreamService streams;
 	TrackService tracks;
 
 	public PlaylistService() {
 		addHardDependency("core.db");
 		addHardDependency("core.metadata");
 		addHardDependency("core.event");
 		addHardDependency("core.tasks");
 		addHardDependency("core.streams");
 		addHardDependency("core.tracks");
 	}
 
 	@Override
 	public String getName() {
 		return "Playlist Service";
 	}
 
 	@Override
 	public String getProvides() {
 		return "core.playlists";
 	}
 
 	@Override
 	public void startup() throws Exception {
 		tasks = rbnb.getTaskService();
 		db = rbnb.getDbService();
 		metadata = rbnb.getMetadataService();
 		events = rbnb.getEventService();
 		streams = rbnb.getStreamService();
 		tracks = rbnb.getTrackService();
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 	}
 
 	public void clearPlaylists() {
 		synchronized (this) {
 			playlists.clear();
 			myPlaylistIdsByTitle.clear();
 		}
 	}
 	
 	public void refreshMyPlaylists(User me) {
 		tasks.runTask(new RefreshMyPlaylistsTask(me.getPlaylistIds()));
 	}
 
 	/** We've loaded friends - now get their playlists */
 	public void refreshFriendPlaylists(Set<Long> plIds) {
 		if (plIds.size() > 0)
 			tasks.runTask(new RefreshFriendPlaylistsTask(plIds));
 	}
 
 	abstract class PlaylistFetcher implements PlaylistCallback {
 		Set<Long> waitingForPlaylists = new HashSet<Long>();
 		Set<String> waitingForStreams = new HashSet<String>();
 		Set<Long> plIds;
 		int streamsToFetch;
 
 		public PlaylistFetcher(Set<Long> plIds) {
 			this.plIds = plIds;
 			waitingForPlaylists.addAll(plIds);
 		}
 
 		@Override
 		public void success(Playlist p) {
 			long plId = p.getPlaylistId();
 			boolean mine = rbnb.getUserService().getMyUser().getPlaylistIds().contains(p.getPlaylistId());
 			synchronized (PlaylistService.this) {
 				Playlist oldP = playlists.get(plId);
 				if (oldP == null || p.getUpdated().after(oldP.getUpdated())) {
 					playlists.put(plId, p);
 					if (mine) {
 						if (oldP != null && !oldP.getTitle().equals(p.getTitle()))
 							myPlaylistIdsByTitle.remove(oldP.getTitle());
 						myPlaylistIdsByTitle.put(p.getTitle(), plId);
 					}
 					waitingForStreams.addAll(p.getStreamIds());
 				}
 			}
 			playlistUpdated(plId);
 		}
 
 		@Override
 		public void error(long plId, Exception ex) {
 			log.error("Got error fetching playlist id " + plId, ex);
 			playlistUpdated(plId);
 		}
 
 		private void playlistUpdated(long upPlId) {
 			waitingForPlaylists.remove(upPlId);
 			if (waitingForPlaylists.size() == 0) {
 				// We've got all our playlists - now get the streams (if any)
 				streamsToFetch = waitingForStreams.size();
 				onStreamUpdate(0, streamsToFetch);
 				if (streamsToFetch > 0) {
 					for (Long plId : plIds) {
 						Playlist p;
 						synchronized (PlaylistService.this) {
 							p = playlists.get(plId);
 						}
 						if (p != null)
 							streams.fetchStreams(p.getStreamIds(), new StreamFetcher(p, this));
 					}
 				}
 			}
 		}
 
 		void gotStream(String sid) {
 			waitingForStreams.remove(sid);
 			int streamsLeft = waitingForStreams.size();
 			int streamsDone = streamsToFetch - streamsLeft;
 			onStreamUpdate(streamsDone, streamsToFetch);
 		}
 
 		void onStreamUpdate(int done, int total) {
 			// Default does nothing
 		}
 	}
 
 	// We make sure we get streamhandler callbacks so that we can know when we're done
 	abstract class PlaylistsRefreshTask extends Task {
 		PlaylistFetcher fetcher;
 		private Set<Long> plIds;
 
 		public PlaylistsRefreshTask(Set<Long> plIds) {
 			this.plIds = plIds;
 			title = titleName();
 			fetcher = new PlaylistFetcher(plIds) {
 				void onStreamUpdate(int done, int total) {
 					if (done == total) {
 						completion = 1f;
 						statusText = "Done.";
 					} else {
 						completion = ((float) done) / total;
 						statusText = "Fetching details of playlist track " + (done + 1) + " of " + total;
 					}
 					fireUpdated();
 					if (completion == 1)
 						onCompletion();
 				}
 			};
 		}
 
 		abstract String titleName();
 
 		abstract void onCompletion();
 
 		@Override
 		public void runTask() throws Exception {
 			statusText = "Fetching playlist details";
 			fireUpdated();
 			metadata.fetchPlaylists(plIds, fetcher);
 		}
 	}
 
 	class RefreshMyPlaylistsTask extends PlaylistsRefreshTask {
 		public RefreshMyPlaylistsTask(Set<Long> plIds) {
 			super(plIds);
 		}
 
 		String titleName() {
 			return "Refreshing my playlists";
 		}
 
 		void onCompletion() {
 			// Now we're done with my playlists, we can load our friends
 			rbnb.getUserService().fetchFriends();
 		}
 	}
 
 	class RefreshFriendPlaylistsTask extends PlaylistsRefreshTask {
 		public RefreshFriendPlaylistsTask(Set<Long> plIds) {
 			super(plIds);
 		}
 
 		String titleName() {
 			return "Refreshing friends' playlists";
 		}
 
 		void onCompletion() {
 			// Now we've done all users and playlists - tell our metadata service to load stuff in parallel now to avoid
 			// requests getting stuck behind all our friends' libraries loading
 			metadata.setFetchOrder(RequestFetchOrder.Parallel);
 			rbnb.getLibraryService().updateLibraries();
 		}
 	}
 
 	class StreamFetcher implements StreamCallback {
 		Playlist p;
 		Set<String> waitingForSids = new HashSet<String>();
 		PlaylistFetcher pFetcher;
 
 		public StreamFetcher(Playlist p, PlaylistFetcher task) {
 			this.p = p;
 			waitingForSids.addAll(p.getStreamIds());
 			this.pFetcher = task;
 		}
 
 		@Override
 		public void success(Stream s) {
 			update(s.getStreamId());
 		}
 
 		@Override
 		public void error(String streamId, Exception ex) {
 			log.error("Exception fetching stream " + streamId, ex);
 			update(streamId);
 		}
 
 		private void update(String sid) {
 			waitingForSids.remove(sid);
 			if (waitingForSids.size() == 0) {
 				// We've loaded all our streams
 				events.firePlaylistChanged(p);
 				downloadTracksIfNecessary(p);
 				syncITunesIfNecessary(p);
 			}
 			if (pFetcher != null)
 				pFetcher.gotStream(sid);
 		}
 	}
 
 	public void checkPlaylistUpdate(long plId) {
 		Set<Long> plIds = new HashSet<Long>();
 		plIds.add(plId);
 		PlaylistFetcher fetcher = new PlaylistFetcher(plIds) {
 		};
 		metadata.fetchPlaylist(plId, fetcher);
 	}
 
 	public void updatePlaylist(Playlist p) {
 		log.debug("Updating playlist " + p.getPlaylistId());
 		synchronized (this) {
 			Playlist oldP = playlists.get(p.getPlaylistId());
 			if (oldP != null && myPlaylistIdsByTitle.containsKey(oldP.getTitle()) && !oldP.getTitle().equals(p.getTitle())) {
 				myPlaylistIdsByTitle.remove(oldP.getTitle());
 				myPlaylistIdsByTitle.put(p.getTitle(), p.getPlaylistId());
 			}
 			playlists.put(p.getPlaylistId(), p);
 		}
 		db.markAllAsSeen(p);
 		metadata.updatePlaylist(p, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.info("Updated playlist id " + newP.getPlaylistId() + " successfully");
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error updating playlist id " + playlistId, ex);
 			}
 		});
 	}
 
 	/** The handler will be called back with the updated playlist, which will have a playlist id set, or else with an
 	 * errot */
 	public void createPlaylist(Playlist p, final PlaylistCallback handler) {
 		log.debug("Creating new playlist with title " + p.getTitle());
 		metadata.updatePlaylist(p, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.debug("Successfully created new playlist id " + newP.getPlaylistId());
 				synchronized (PlaylistService.this) {
 					playlists.put(newP.getPlaylistId(), newP);
 					myPlaylistIdsByTitle.put(newP.getTitle(), newP.getPlaylistId());
 				}
 				db.markAllAsSeen(newP);
 				// Fire our user as updated first, then the playlist
 				rbnb.getUserService().playlistCreated(newP);
 				events.firePlaylistChanged(newP);
 				if (handler != null)
 					handler.success(newP);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error creating playlist", ex);
 				if (handler != null)
 					handler.error(playlistId, ex);
 			}
 		});
 	}
 
 	/** Update things that need to be updated on playlists containing this track we're now sharing */
 	public void checkPlaylistsForNewShare(SharedTrack sh) {
 		// Currently, just sync to itunes
 		final List<Playlist> affectedPs = new ArrayList<Playlist>();
 		synchronized (this) {
 			for (Playlist p : playlists.values()) {
 				if (p.getStreamIds().contains(sh.getStream().getStreamId()))
 					affectedPs.add(p);
 			}
 		}
 		if (affectedPs.size() > 0 && Platform.getPlatform().iTunesAvailable()) {
 			// Update itunes in another thread
 			rbnb.getExecutor().execute(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					for (Playlist p : affectedPs) {
 						syncITunesIfNecessary(p);
 					}
 				}
 			});
 		}
 	}
 
 	private void syncITunesIfNecessary(Playlist p) {
 		try {
 			PlaylistConfig pc = getRobonobo().getDbService().getPlaylistConfig(p.getPlaylistId());
 			if ("true".equalsIgnoreCase(pc.getItem("iTunesExport"))) {
 				for (Long ownerId : p.getOwnerIds()) {
 					User owner = rbnb.getUserService().getUser(ownerId);
 					rbnb.getITunesService().syncPlaylist(owner, p);
 				}
 			}
 		} catch (IOException e) {
 			log.error("Error syncing playlist id " + p.getPlaylistId() + " to itunes");
 		}
 	}
 
 	private void downloadTracksIfNecessary(Playlist p) {
 		PlaylistConfig pc = db.getPlaylistConfig(p.getPlaylistId());
 		if (((pc != null) && "true".equalsIgnoreCase(pc.getItem("autoDownload")))) {
 			for (String sid : p.getStreamIds()) {
 				Track t = tracks.getTrack(sid);
 				try {
 					if (t instanceof CloudTrack)
 						rbnb.getDownloadService().addDownload(sid);
 				} catch (RobonoboException e) {
 					log.error("Error auto-downloading stream " + sid, e);
 				}
 			}
 		}
 	}
 
 	public synchronized Playlist getExistingPlaylist(long playlistId) {
 		return playlists.get(playlistId);
 	}
 
 	public void getOrFetchPlaylist(long playlistId, final PlaylistCallback handler) {
 		Playlist p = getExistingPlaylist(playlistId);
 		if (p != null) {
 			handler.success(p);
 			return;
 		}
 		metadata.fetchPlaylist(playlistId, new PlaylistCallback() {
 			public void success(Playlist p) {
 				synchronized (PlaylistService.this) {
 					playlists.put(p.getPlaylistId(), p);
 				}
 				handler.success(p);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				handler.error(playlistId, ex);
 			}
 		});
 	}
 
 	public synchronized Playlist getMyPlaylistByTitle(String title) {
 		Long plId = myPlaylistIdsByTitle.get(title);
 		if (plId == null)
 			return null;
 		return getExistingPlaylist(plId);
 	}
 
 	public void deletePlaylist(Playlist p) {
 		final long plId = p.getPlaylistId();
 		log.debug("Deleting playlist " + plId);
 		User me = rbnb.getUserService().getMyUser();
 		boolean firePlaylistUpdate = false;
 		synchronized (this) {
 			p = playlists.get(plId);
 			if (p == null)
 				throw new Errot();
 			playlists.get(plId).getOwnerIds().remove(me.getUserId());
 			if (p.getOwnerIds().size() > 0)
 				firePlaylistUpdate = true;
 			else
 				playlists.remove(plId);
 		}
 		rbnb.getUserService().playlistDeleted(p);
 		if (firePlaylistUpdate)
 			events.firePlaylistChanged(p);
 		metadata.deletePlaylist(p, new PlaylistCallback() {
 			public void success(Playlist p) {
 				log.debug("Successfully deleted playlist " + plId);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error deleting playlist " + playlistId, ex);
 			}
 		});
 	}
 
 	public void sharePlaylist(final Playlist p, final Set<Long> friendIds, Set<String> emails) throws IOException, RobonoboException {
 		metadata.sharePlaylist(p, friendIds, emails, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.debug("Successfully shared playlist " + p.getPlaylistId());
 				synchronized (PlaylistService.this) {
 					playlists.put(newP.getPlaylistId(), newP);
 				}
 				rbnb.getUserService().playlistShared(p, friendIds);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error sharing playlist " + playlistId, ex);
 			}
 		});
 	}
 
 	public void postPlaylistUpdateToService(final String service, final long playlistId, String msg) {
 		log.debug("Posting playlist update for playlist " + playlistId + " to service " + service);
 		metadata.postPlaylistUpdateToService(service, playlistId, msg, new PlaylistCallback() {
 			public void success(Playlist isnull) {
 				log.debug("Successfully posted playlist update for playlist " + playlistId + " to service " + service);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error posting playlist update for playlist " + playlistId + " to service " + service, ex);
 			}
 		});
 	}
 }
