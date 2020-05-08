 package com.robonobo.core.service;
 
 import static com.robonobo.common.util.TimeUtil.*;
 
import java.io.IOException;
 import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
 
 import com.robonobo.common.concurrent.Batcher;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.TimeUtil;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.config.MetadataServerConfig;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.proto.CoreApi.LibraryMsg;
 
 public class LibraryService extends AbstractService implements UserPlaylistListener {
 	/** secs */
 	static final int SEND_UPDATE_TO_SERVER_DELAY = 30;
 	/** secs */
 	static final int FIRE_UI_EVENT_DELAY = 10;
 	private AddBatcher addB;
 	private DelBatcher delB;
 	private Map<Long, Library> libs = new HashMap<Long, Library>();
 	boolean updateTaskRunning = false;
 
 	public LibraryService() {
 		addHardDependency("core.metadata");
 		addHardDependency("core.users");
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
 		addB = new AddBatcher();
 		delB = new DelBatcher();
 		rbnb.getEventService().addUserPlaylistListener(this);
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
 
 	@Override
 	public void loggedIn() {
 		// Do nothing
 	}
 
 	@Override
 	public void allUsersAndPlaylistsUpdated() {
 		if (!updateTaskRunning)
 			rbnb.getTaskService().runTask(new LibrariesUpdateTask());
 	}
 
 	@Override
 	public void userChanged(User u) {
 		// Do nothing
 	}
 
 	@Override
 	public void playlistChanged(Playlist p) {
 		// Do nothing
 	}
 
 	@Override
 	public void userConfigChanged(UserConfig cfg) {
 		// Do nothing
 	}
 
 	class LibrariesUpdateTask extends Task {
 		public LibrariesUpdateTask() {
 			title = "Updating friend libraries";
 		}
 
 		@Override
 		public void runTask() throws Exception {
 			updateTaskRunning = true;
 			try {
 				Set<Long> friendIds = new HashSet<Long>(rbnb.getUsersService().getMyUser().getFriendIds());
 				// Clean out any ex-friends - your tunes sucked anyway! :-P
 				synchronized (LibraryService.this) {
 					Iterator<Long> it = libs.keySet().iterator();
 					while (it.hasNext()) {
 						long libUid = it.next();
 						if (!friendIds.contains(libUid))
 							it.remove();
 					}
 				}
 				int friendsDone = 0;
 				for (long friendId : friendIds) {
 					completion = (float) friendsDone / friendIds.size();
 					User u = rbnb.getUsersService().getUser(friendId);
 					Library cLib;
 					Set<String> newSidsForUi = new HashSet<String>();
 					synchronized (LibraryService.this) {
 						cLib = libs.get(friendId);
 						if (cLib == null) {
 							cLib = rbnb.getDbService().getLibrary(friendId);
 							if (cLib != null) {
 								// First time through - Looked up the user's library from the db
 								// Take note of our current tracks in the user's library so we can pass these up to the
 								// ui - but remove any tracks we don't have looked-up streams for - we take care of those later
 								Map<String, Date> unknownSids = rbnb.getDbService().getUnknownStreamsInLibrary(friendId);
 								for (String uSid : unknownSids.keySet()) {
 									cLib.getTracks().remove(uSid);
 								}
 								newSidsForUi.addAll(cLib.getTracks().keySet());
 								libs.put(friendId, cLib);
 							}
 						}
 					}
 					statusText = "Fetching library for " + u.getEmail();
 					fireUpdated();
 					LibraryMsg.Builder b = LibraryMsg.newBuilder();
 					MetadataServerConfig msc = rbnb.getUsersService().getMsc();
 					Date lastUpdated = (cLib == null) ? null : cLib.getLastUpdated();
 					try {
 						rbnb.getSerializationManager().getObjectFromUrl(b, msc.getLibraryUrl(friendId, lastUpdated));
 					} catch (Exception e) {
 						log.error("Error getting library", e);
 					}
 					lastUpdated = now();
 					Library nLib = new Library(b.build());
 					Map<String, Date> newTrax = nLib.getTracks();
 					log.debug("Received updated library for " + u.getEmail() + ": " + newTrax.size() + " new tracks");
 					if (newTrax.size() > 0) {
 						// Save this library in our db first as it's quite likely the user will quit while we're looking
 						// up streams
 						rbnb.getDbService().addTracksToLibrary(friendId, newTrax);
 						// Create a library for the user
 						synchronized (LibraryService.this) {
 							if (cLib == null) {
 								cLib = new Library();
 								cLib.setUserId(friendId);
 								cLib.setLastUpdated(lastUpdated);
 								libs.put(friendId, cLib);
 							} else
 								cLib.setLastUpdated(lastUpdated);
 						}
 					}
 					Map<String, Date> unknownTracks = rbnb.getDbService().getUnknownStreamsInLibrary(friendId);
 					if(unknownTracks.size() > 0) {
 						log.debug("Looking up "+unknownTracks.size()+" unknown streams in library for "+u.getEmail());
 						Date lastFiredEvent = now();
 						for(String sid : unknownTracks.keySet()) {
 							// If this is a stream we haven't seen before, this next method will result in a blocking
 							// call to the metadata server
 							Stream s = rbnb.getMetadataService().getStream(sid);
 							if (s == null) {
 								if(rbnb.getStatus() == RobonoboStatus.Stopping)
 									return;
 							} else
 								newSidsForUi.add(sid);
 							if (newSidsForUi.size() > 0 && msElapsedSince(lastFiredEvent) > (FIRE_UI_EVENT_DELAY * 1000)) {
 								for (String newSid : newSidsForUi) {
 									cLib.getTracks().put(newSid, unknownTracks.get(newSid));
 								}
 								rbnb.getEventService().fireLibraryChanged(cLib, newSidsForUi);
 								// Create a new one rather than clear the old one as it might have been passed into another thread and used somewhere
 								newSidsForUi = new HashSet<String>();
 								lastFiredEvent = now();
 							}
 
 						}
 					}
 					if(newSidsForUi.size() > 0) {
 						for (String newSid : newSidsForUi) {
 							cLib.getTracks().put(newSid, unknownTracks.get(newSid));
 						}
 						rbnb.getEventService().fireLibraryChanged(cLib, newSidsForUi);
 					}						
 				}
 				statusText = "Done.";
 				completion = 1f;
 				fireUpdated();
 			} finally {
 				updateTaskRunning = false;
 			}
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
 			User me = rbnb.getUsersService().getMyUser();
 			MetadataServerConfig msc = rbnb.getUsersService().getMsc();
 			rbnb.getSerializationManager().putObjectToUrl(lib.toMsg(), msc.getLibraryAddUrl(me.getUserId()));
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
 			User me = rbnb.getUsersService().getMyUser();
 			MetadataServerConfig msc = rbnb.getUsersService().getMsc();
 			rbnb.getSerializationManager().putObjectToUrl(lib.toMsg(), msc.getLibraryDelUrl(me.getUserId()));
 		}
 	}
 }
