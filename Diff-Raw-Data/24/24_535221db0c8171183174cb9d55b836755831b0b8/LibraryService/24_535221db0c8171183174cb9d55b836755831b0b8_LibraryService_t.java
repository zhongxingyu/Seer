 package com.robonobo.core.service;
 
 import static com.robonobo.common.util.TimeUtil.*;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 
 import javax.swing.JSpinner.DateEditor;
 
 import com.robonobo.common.concurrent.Batcher;
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.proto.CoreApi.LibraryMsg;
 import com.robonobo.core.metadata.*;
 
 public class LibraryService extends AbstractService {
 	/** secs */
 	static final int SEND_UPDATE_TO_SERVER_DELAY = 30;
 	/** secs */
 	static final int FIRE_UI_EVENT_DELAY = 10;
 	private AddBatcher addB;
 	private DelBatcher delB;
 	private Map<Long, Library> libs = new HashMap<Long, Library>();
 	Set<Long> stillFetchingLibs = new HashSet<Long>();
 	UserService users;
 	AbstractMetadataService metadata;
 	TaskService tasks;
 	EventService events;
 	StreamService streams;
 
 	public LibraryService() {
 		addHardDependency("core.metadata");
 		addHardDependency("core.users");
 		addHardDependency("core.tasks");
 		addHardDependency("core.event");
 		addHardDependency("core.streams");
 	}
 
 	@Override
 	public String getName() {
 		return "Library service";
 	}
 
 	@Override
 	public String getProvides() {
 		return "core.library";
 	}
 
 	@Override
 	public void startup() throws Exception {
 		metadata = rbnb.getMetadataService();
 		users = rbnb.getUserService();
 		tasks = rbnb.getTaskService();
 		events = rbnb.getEventService();
 		streams = rbnb.getStreamService();
 		addB = new AddBatcher();
 		delB = new DelBatcher();
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		// We might have some pending library updates - do them now
 		addB.run();
 		delB.run();
 	}
 
 	public synchronized Library getLibrary(long userId) {
 		return libs.get(userId);
 	}
 
 	public void addToLibrary(String streamId) {
 		addB.add(new LibraryTrack(streamId, now()));
 	}
 
 	public void delFromLibrary(String streamId) {
 		delB.add(streamId);
 	}
 
 	void updateLibraries() {
 		if (stillFetchingLibs.size() == 0)
 			rbnb.getExecutor().execute(new LibrariesUpdater());
 	}
 
 	class LibrariesUpdater extends CatchingRunnable {
 		public void doRun() throws Exception {
 			Set<Long> friendIds = new HashSet<Long>(rbnb.getUserService().getMyUser().getFriendIds());
 			// Clean out any ex-friends - your tunes sucked anyway! :-P
 			synchronized (LibraryService.this) {
 				Iterator<Long> it = libs.keySet().iterator();
 				while (it.hasNext()) {
 					long libUid = it.next();
 					if (!friendIds.contains(libUid))
 						it.remove();
 				}
 			}
 			for (long friendId : friendIds) {
 				User friend = users.getUser(friendId);
 				Library cLib;
 				Set<String> sidsForUi = new HashSet<String>();
 				synchronized (LibraryService.this) {
 					cLib = libs.get(friendId);
 					if (cLib == null) {
 						cLib = rbnb.getDbService().getLibrary(friendId);
 						if (cLib != null) {
 							// First time through - Looked up the user's library from the db
 							// Take note of our current tracks in the user's library so we can pass these up to the
 							// ui - but remove any tracks we don't have looked-up streams for - we take care of
 							// those in the update pFetcher
 							Map<String, Date> unknownSids = rbnb.getDbService().getUnknownStreamsInLibrary(friendId);
 							for (String uSid : unknownSids.keySet()) {
 								cLib.getTracks().remove(uSid);
 							}
 							sidsForUi.addAll(cLib.getTracks().keySet());
 							libs.put(friendId, cLib);
 						}
 					}
 				}
 				// Punt up the sids we do have to the UI
 				if (cLib != null && sidsForUi.size() > 0)
 					events.fireLibraryChanged(cLib, sidsForUi);
 				Date lastUpdated = (cLib == null) ? null : cLib.getLastUpdated();
 				tasks.runTask(new LibraryUpdateTask(friend, cLib, lastUpdated));
 			}
 		}
 	}
 
 	class LibraryUpdateTask extends Task implements LibraryHandler {
 		private Date lastUpdate;
 		private Library cLib;
 		private User friend;
 		private Set<String> waitingForStreams;
 		private int streamsToFetch;
 
 		public LibraryUpdateTask(User friend, Library cLib, Date lastUpdate) {
 			title = "Fetching library for " + friend.getEmail();
 			this.lastUpdate = lastUpdate;
 			this.cLib = cLib;
 			this.friend = friend;
 			stillFetchingLibs.add(friend.getUserId());
 		}
 
 		public void runTask() throws Exception {
 			statusText = "Retrieving library details";
 			fireUpdated();
 			metadata.fetchLibrary(friend.getUserId(), lastUpdate, this);
 		}
 
 		public void success(Library nLib) {
 			long friendId = friend.getUserId();
 			Map<String, Date> newTrax = nLib.getTracks();
 			log.debug("Received updated library for " + friend.getEmail() + ": " + newTrax.size() + " new tracks");
 			if (newTrax.size() > 0) {
 				// Save this library in our db first as it's quite likely the user will quit while we're looking
 				// up streams
 				rbnb.getDbService().addTracksToLibrary(friendId, newTrax);
 				// Create a library for the user
 				synchronized (LibraryService.this) {
 					if (cLib == null) {
 						cLib = new Library();
 						cLib.setUserId(friendId);
 						cLib.setLastUpdated(lastUpdate);
 						libs.put(friendId, cLib);
 					} else
 						cLib.setLastUpdated(lastUpdate);
 				}
 			}
 			// Now we've updated the db this will have all the sids for which we don't have streams
 			Map<String, Date> unknownTracks = rbnb.getDbService().getUnknownStreamsInLibrary(friendId);
 			// Any of these new tracks we know about from other sources, punt them up to the ui
 			Set<String> newSidsForUi = new HashSet<String>();
 			for (Entry<String, Date> entry : newTrax.entrySet()) {
 				String sid = entry.getKey();
 				Date dateAdded = entry.getValue();
 				if (!unknownTracks.containsKey(sid)) {
 					cLib.getTracks().put(sid, dateAdded);
 					newSidsForUi.add(sid);
 				}
 			}
 			if (newSidsForUi.size() > 0)
 				events.fireLibraryChanged(cLib, newSidsForUi);
 			if (unknownTracks.size() == 0) {
 				statusText = "Done.";
 				completion = 1f;
 				stillFetchingLibs.remove(friendId);
 				fireUpdated();
 				return;
 			}
 			// Get the rest of our streams
			waitingForStreams = new HashSet<String>();
 			waitingForStreams.addAll(unknownTracks.keySet());
 			streamsToFetch = waitingForStreams.size();
 			statusText = "Fetching track 1 of " + streamsToFetch;
 			fireUpdated();
 			streams.fetchStreams(unknownTracks.keySet(), new StreamFetcher(nLib, unknownTracks, this));
 		}
 
 		public void error(long userId, Exception e) {
 			log.error("Failed to retrieve library for user " + userId, e);
 			completion = 1f;
 			statusText = "An error occurred";
 			stillFetchingLibs.remove(userId);
 			fireUpdated();
 		}
 
 		void streamUpdated(String sid) {
 			waitingForStreams.remove(sid);
 			int streamsDone = streamsToFetch - waitingForStreams.size();
 			if (streamsDone == streamsToFetch) {
 				completion = 1f;
 				stillFetchingLibs.remove(friend.getUserId());
 				statusText = "Done.";
 			} else {
 				completion = ((float) streamsDone) / streamsToFetch;
 				statusText = "Fetching track "+(streamsDone+1)+" of "+streamsToFetch;
 			}
 			fireUpdated();
 		}
 	}
 
 	class StreamFetcher extends Batcher<String> implements StreamHandler {
 		Library lib;
 		Map<String, Date> streamsToFetch;
 		LibraryUpdateTask task;
 
 		public StreamFetcher(Library lib, Map<String, Date> streamsToFetch, LibraryUpdateTask task) {
 			super(FIRE_UI_EVENT_DELAY * 1000, rbnb.getExecutor());
 			this.lib = lib;
 			this.streamsToFetch = streamsToFetch;
 			this.task = task;
 		}
 
 		public void success(Stream s) {
			String sid = s.getStreamId();
			add(sid);
			task.streamUpdated(sid);
 		}
 
 		public void error(String streamId, Exception ex) {
 			log.error("Error fetching stream " + streamId, ex);
 			task.streamUpdated(streamId);
 		}
 
 		protected void runBatch(Collection<String> sids) throws Exception {
 			events.fireLibraryChanged(lib, sids);
 		}
 	}
 
 	class LibraryTrack {
 		String streamId;
 		Date dateAdded;
 
 		public LibraryTrack(String streamId, Date dateAdded) {
 			this.streamId = streamId;
 			this.dateAdded = dateAdded;
 		}
 	}
 
 	class AddBatcher extends Batcher<LibraryTrack> {
 		public AddBatcher() {
 			super(SEND_UPDATE_TO_SERVER_DELAY * 1000, rbnb.getExecutor());
 		}
 
 		@Override
 		protected void runBatch(Collection<LibraryTrack> tracks) throws Exception {
 			if (tracks.size() == 0)
 				return;
 			Library lib = new Library();
 			for (LibraryTrack t : tracks) {
 				lib.getTracks().put(t.streamId, t.dateAdded);
 			}
 			User me = rbnb.getUserService().getMyUser();
 			metadata.addToLibrary(me.getUserId(), lib, new LibraryHandler() {
 				public void success(Library l) {
 					log.debug("Successfully added tracks to my library");
 				}
 				
 				public void error(long userId, Exception e) {
 					log.error("Error adding to my library", e);
 				}
 			});
 		}
 	}
 
 	class DelBatcher extends Batcher<String> {
 		public DelBatcher() {
 			super(SEND_UPDATE_TO_SERVER_DELAY * 1000, rbnb.getExecutor());
 		}
 
 		@Override
 		protected void runBatch(Collection<String> streamIds) throws Exception {
 			if (streamIds.size() == 0)
 				return;
 			Library lib = new Library();
 			for (String sid : streamIds) {
 				lib.getTracks().put(sid, null);
 			}
 			User me = rbnb.getUserService().getMyUser();
 			metadata.deleteFromLibrary(me.getUserId(), lib, new LibraryHandler() {
 				public void success(Library l) {
 					log.debug("Successfully removed tracks from my library");
 				}
 				
 				public void error(long userId, Exception e) {
 					log.error("Error deleting from my library", e);
 				}
 			});
 		}
 	}
 }
