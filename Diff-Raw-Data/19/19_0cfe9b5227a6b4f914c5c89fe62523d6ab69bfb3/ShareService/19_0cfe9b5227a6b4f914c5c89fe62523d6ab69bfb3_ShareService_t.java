 package com.robonobo.core.service;
 
 import static com.robonobo.common.util.FileUtil.*;
 import static com.robonobo.common.util.TimeUtil.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.Errot;
 import com.robonobo.common.pageio.buffer.FilePageBuffer;
 import com.robonobo.common.util.FileUtil;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
 import com.robonobo.core.api.model.SharedTrack.ShareStatus;
 import com.robonobo.core.storage.StorageService;
 import com.robonobo.mina.external.MinaControl;
 import com.robonobo.mina.external.buffer.PageBuffer;
 import com.robonobo.spi.FormatSupportProvider;
 
 /**
  * Responsible for translating mina Broadcasts to robonobo Shares
  * 
  * @author macavity
  */
 public class ShareService extends AbstractService {
 	/** Seconds */
 	// public static final int WATCHDIR_CHECK_FREQ = 60 * 10;
 	public static final int WATCHDIR_CHECK_FREQ = 30;
 	public static final int WATCHDIR_INITIAL_WAIT = 30;
 
 	Log log = LogFactory.getLog(getClass());
 	DbService db;
 	EventService event;
 	UserService users;
 	StorageService storage;
 	StreamService streams;
 	PlaybackService playback;
 	MinaControl mina;
 	private Set<String> shareStreamIds;
 	private ScheduledFuture<?> watchDirTask;
 	private boolean watchDirRunning = false;
 
 	public ShareService() {
 		addHardDependency("core.mina");
 		addHardDependency("core.streams");
 		addHardDependency("core.storage");
 	}
 
 	@Override
 	public void startup() throws Exception {
 		db = rbnb.getDbService();
 		event = rbnb.getEventService();
 		users = rbnb.getUserService();
 		storage = rbnb.getStorageService();
 		streams = rbnb.getStreamService();
 		playback = rbnb.getPlaybackService();
 		mina = rbnb.getMina();
 		// Keep track of our stream ids, everything else loaded on-demand from the db
 		shareStreamIds = db.getShares();
 		watchDirTask = getRobonobo().getExecutor().scheduleWithFixedDelay(new WatchDirChecker(), WATCHDIR_INITIAL_WAIT,
 				WATCHDIR_CHECK_FREQ, TimeUnit.SECONDS);
 	}
 
 	@Override
 	public void shutdown() {
 		// Don't specifically stop our shares, the mina shutdown will stop them
 		watchDirTask.cancel(true);
 	}
 
 	/**
 	 * NB The stream referenced by stream id must already have been put into streamservice
 	 * @param streamId
 	 * @param dataFile
 	 * @throws RobonoboException
 	 */
 	public void addShare(Stream s, File dataFile) throws RobonoboException {
 		String streamId = s.getStreamId();
 		log.info("Adding share for id " + streamId + " at " + dataFile.getAbsolutePath());
 		SharedTrack sh = db.getShare(streamId);
 		if (sh != null) {
 			log.info("Not adding share for id " + streamId + " - sharing already");
 			return;
 		}
 		sh = new SharedTrack(s, dataFile, ShareStatus.Sharing);
 		try {
 			PageBuffer pb = storage.createPageBufForShare(sh.getStream(), sh.getFile(), true);
 			FormatSupportProvider fsp = rbnb.getFormatService().getFormatSupportProvider(s.getMimeType());
 			if (fsp == null)
 				throw new IOException("No FSP available for the mimeType " + s.getMimeType());
 			log.debug("Paginating " + dataFile.getAbsolutePath());
 			fsp.paginate(dataFile, pb);
 		} catch (IOException e) {
 			throw new RobonoboException(e);
 		}
 		sh.setDateAdded(now());
 		db.putShare(sh);
 		synchronized (this) {
 			shareStreamIds.add(s.getStreamId());
 		}
 		startShare(streamId);
 		rbnb.getLibraryService().addToLibrary(streamId);
 		event.fireTrackUpdated(s.getStreamId());
 		event.fireMyLibraryUpdated();
 		rbnb.getPlaylistService().checkPlaylistsForNewShare(sh);
 	}
 
 	private File fileForFinishedDownload(Stream s) {
 		String artist = s.getAttrValue("artist");
 		if (artist == null)
 			artist = "Unknown Artist";
 		String album = s.getAttrValue("album");
 		if (album == null)
 			album = "Unknown Album";
 		String sep = File.separator;
 		File targetDir = new File(rbnb.getConfig().getFinishedDownloadsDirectory() + sep + makeFileNameSafe(artist)
 				+ sep + makeFileNameSafe(album));
 		targetDir.mkdirs();
 		String fileExt = rbnb.getFormatService().getFormatSupportProvider(s.getMimeType()).getDefaultFileExtension();
 		return new File(targetDir, makeFileNameSafe(s.getTitle()) + "." + fileExt);
 	}
 
 	public void addShareFromCompletedDownload(DownloadingTrack d) throws RobonoboException {
 		Stream s = d.getStream();
 		log.debug("Adding share for completed download " + s.getStreamId());
 		if (d.getDownloadStatus() != DownloadStatus.Finished) {
 			throw new Errot();
 		}
 		File curFile = d.getFile();
 		File shareFile = fileForFinishedDownload(s);
 		// Copy the file rather than move, as windoze won't allow us to move it while we're
 		// accessing it (unix does it just fine *while* allowing us to keep reading from the file descriptor...)
 		try {
 			FileUtil.copyFile(curFile, shareFile);
 		} catch (IOException e) {
 			throw new RobonoboException(e);
 		}
 		curFile.deleteOnExit();
 		SharedTrack sh = new SharedTrack(s, shareFile, ShareStatus.Sharing);
 		sh.setDateAdded(now());
 		db.putShare(sh);
 		synchronized (this) {
 			shareStreamIds.add(s.getStreamId());
 		}
 		try {
 			rbnb.getStorageService().createPageBufForShare(s, shareFile, false);
 		} catch (IOException e) {
 			throw new RobonoboException(e);
 		}
 		startShare(s.getStreamId());
 		event.fireTrackUpdated(s.getStreamId());
 		rbnb.getPlaylistService().checkPlaylistsForNewShare(sh);
 	}
 
 	public void deleteShare(String streamId) {
 		log.info("Deleting share for stream " + streamId);
 		playback.stopIfCurrentlyPlaying(streamId);
 		SharedTrack share = db.getShare(streamId);
 		if (share == null)
 			return;
 		stopShare(streamId);
 		db.deleteShare(streamId);
 		storage.nukePageBuf(streamId);
 		synchronized (this) {
 			shareStreamIds.remove(streamId);
 		}
 		rbnb.getLibraryService().delFromLibrary(streamId);
 		event.fireTrackUpdated(streamId);
		event.fireMyLibraryUpdated();
 	}
 
 	private void startShare(String streamId) throws RobonoboException {
 		mina.startBroadcast(streamId);
 	}
 
 	private void stopShare(String streamId) {
 		mina.stopBroadcast(streamId);
 	}
 
 	public String getName() {
 		return "Share service";
 	}
 
 	public String getProvides() {
 		return "core.shares";
 	}
 
 	public SharedTrack getShare(String streamId) {
 		synchronized (this) {
 			if (!shareStreamIds.contains(streamId))
 				return null;
 		}
 		return db.getShare(streamId);
 	}
 
 	public Collection<SharedTrack> getSharesByPattern(String searchPattern) {
 		Collection<SharedTrack> shares = db.getSharesByPattern(searchPattern);
 		return shares;
 	}
 
 	public SharedTrack getShareByFilePath(String filePath) {
 		return db.getShareByFilePath(filePath);
 	}
 
 	public void checkWatchDir(File watchDir) throws RobonoboException {
 		synchronized (this) {
 			if (watchDirRunning)
 				return;
 			watchDirRunning = true;
 		}
 		try {
 			log.debug("Checking watch dir " + watchDir.getAbsolutePath());
 			for (File itemInWatchDir : watchDir.listFiles()) {
 				if (!db.haveCheckedFile(itemInWatchDir)) {
 					List<File> mp3z = FileUtil.getFilesWithinPath(itemInWatchDir, "mp3");
 					for (File mp3File : mp3z) {
 						log.debug("Adding share from file " + mp3File.getAbsolutePath());
 						SharedTrack sh = getShareByFilePath(mp3File.getAbsolutePath());
 						if (sh == null) {
 							Stream s;
 							try {
 								s = getRobonobo().getFormatService().getStreamForFile(mp3File);
 								streams.putStream(s);
 								addShare(s, mp3File);
 							} catch (Exception e) {
 								log.error("Error adding file " + mp3File.getAbsolutePath() + ": " + e.getMessage());
 								continue;
 							}
 						}
 					}
 					db.notifyFileChecked(itemInWatchDir);
 				}
 			}
 		} finally {
 			synchronized (this) {
 				watchDirRunning = false;
 			}
 		}
 	}
 
 	void startAllShares() throws IOException, RobonoboException {
 		log.debug("Start Share thread running");
 		// Copy out our stream ids so we can iterate while adding new shares
 		String[] arr;
 		synchronized (this) {
 			arr = new String[shareStreamIds.size()];
 			shareStreamIds.toArray(arr);
 		}
 		Set<String> shareSids = new HashSet<String>();
 		for (String sid : arr) {
 			// We don't cache the page buffer unless we need it (there could be 10^4+), just look it up to make sure
 			// it's kosher
 			FilePageBuffer pb = storage.getPageBuf(sid);
 			if (pb == null) {
 				// Errot
 				log.error("Found null pagebuf when starting share for " + sid + " - deleting share");
 				db.deleteShare(sid);
 				continue;
 			}
 			// If the file for this share doesn't exist, don't start this share but keep the reference around - might be
 			// a removable drive that will come back later
 			File file = pb.getFile();
 			if (!file.exists() || !file.canRead()) {
 				log.error("Could not find or read from file " + file.getAbsolutePath() + " for stream " + sid
 						+ " - not starting share");
 				synchronized (this) {
 					shareStreamIds.remove(sid);
 				}
 				continue;
 			}
 			shareSids.add(sid);
 		}
 		getRobonobo().getMina().startBroadcasts(shareSids);
 		log.debug("Start Share thread finished: started " + shareSids.size() + " shares");
 	}
 
 	private class WatchDirChecker extends CatchingRunnable {
 		public WatchDirChecker() {
 		}
 
 		public void doRun() throws Exception {
 			List<File> watchDirs = db.getWatchDirs();
 			for (File watchDir : watchDirs) {
 				checkWatchDir(watchDir);
 			}
 		}
 	}
 }
