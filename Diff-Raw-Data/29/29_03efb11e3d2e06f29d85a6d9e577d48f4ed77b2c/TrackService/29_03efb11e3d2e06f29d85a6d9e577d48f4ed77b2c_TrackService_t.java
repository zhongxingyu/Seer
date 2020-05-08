 package com.robonobo.core.service;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.AudioPlayer.Status;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.model.Track.PlaybackStatus;
 import com.robonobo.mina.external.MinaControl;
 
 /**
  * First point of call to get information about a track, whether we're sharing it, downloading it or it just exists in
  * the cloud.
  * 
  * Get shares and downloads from here rather than Share/Download service as they will then include info on whether
  * they're playing, and their transfer speeds
  * 
  * @author macavity
  * 
  */
 public class TrackService extends AbstractService implements TransferSpeedListener {
 	Log log = LogFactory.getLog(getClass());
 	private ShareService share;
 	private DownloadService download;
 	private MetadataService metadata;
 	private PlaybackService playback;
 	private EventService event;
 	private MinaControl mina;
 	protected Map<String, TransferSpeed> transferSpeeds = null;
 	private String currentPlayingStreamId = null;
 	private boolean allSharesStarted;
	private boolean started = false;
 
 	public TrackService() {
 		addHardDependency("core.shares");
 		addHardDependency("core.downloads");
 	}
 
 	public String getName() {
 		return "Track Service";
 	}
 
 	public String getProvides() {
 		return "core.tracks";
 	}
 
 	@Override
 	public void startup() throws Exception {
 		share = rbnb.getShareService();
 		download = rbnb.getDownloadService();
 		metadata = rbnb.getMetadataService();
 		playback = rbnb.getPlaybackService();
 		event = rbnb.getEventService();
 		mina = rbnb.getMina();
 		// Fork off a thread to start shares, we might have a lot... do this here rather than in shareservice as we
 		// start after them and we need to be present
 		// to fire allSharesStarted
 		log.debug("Spawning thread to start shares");
 		getRobonobo().getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				share.startAllShares();
 				allSharesStarted = true;
 				event.fireAllTracksLoaded();
 			}
 		});
 		event.addTransferSpeedListener(this);
		started  = true;
 	}
 
 	@Override
 	public void newTransferSpeeds(Map<String, TransferSpeed> speedsByStream, Map<String, TransferSpeed> speedsByNode) {
 		// We fire the updated event for all streams that were in the
 		// previous set of speeds and this set, so they get reset to 0
 		Set<String> changedStreamIds = new HashSet<String>();
 		synchronized (this) {
 			if (transferSpeeds != null)
 				changedStreamIds.addAll(transferSpeeds.keySet());
 			transferSpeeds = speedsByStream;
 			changedStreamIds.addAll(transferSpeeds.keySet());
 		}
 		if (changedStreamIds.size() > 0)
 			event.fireTracksUpdated(changedStreamIds);
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 	}
 
 	/**
 	 * Don't hang onto the object you get returned from here - implement TrackListener, and look it up every time
 	 * instead. That way we keep ram usage down and you always have the correct status (tracks start off as CloudTracks,
 	 * then become DownloadingTracks, then SharedTracks, plus they get played, then stopped, etc)
 	 */
 	public Track getTrack(String streamId) {
		// If we haven't started yet, just wait til we have
		while(!started) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new SeekInnerCalmException(e);
			}
		}
			
 		Track t;
 		// Are we sharing this track?
 		t = share.getShare(streamId);
 		if (t == null) {
 			// Are we downloading this track?
 			t = download.getDownload(streamId);
 			if (t == null) {
 				// Nope - it's in the cloud
 				Stream s = metadata.getStream(streamId);
 				if (s == null)
 					return null;
 				t = new CloudTrack(s, mina.numSources(streamId));
 			}
 		}
 		// Set playback status (might be already there if downloading, if necessary override it)
 		String playingSid = playback.getCurrentStreamId();
 		if (playingSid != null && playingSid.equals(streamId)) {
 			switch (playback.getStatus()) {
 			case Starting:
 				t.setPlaybackStatus(PlaybackStatus.Starting);
 				break;
 			case Playing:
 				t.setPlaybackStatus(PlaybackStatus.Playing);
 				break;
 			case Paused:
 				t.setPlaybackStatus(PlaybackStatus.Paused);
 				break;
 			}
 		}
 		// Set transfer speeds
 		synchronized (this) {
 			if (transferSpeeds != null && transferSpeeds.containsKey(streamId)) {
 				TransferSpeed ts = transferSpeeds.get(streamId);
 				t.setRates(ts.getDownload(), ts.getUpload());
 			}
 		}
 		return t;
 	}
 
 	public void notifyPlayingTrackChange(String newPlayingStreamId) {
 		if (currentPlayingStreamId != null)
 			event.fireTrackUpdated(currentPlayingStreamId);
 		if (newPlayingStreamId != null)
 			event.fireTrackUpdated(newPlayingStreamId);
 		currentPlayingStreamId = newPlayingStreamId;
 	}
 
 	public boolean haveAllSharesStarted() {
 		return allSharesStarted;
 	}
 }
