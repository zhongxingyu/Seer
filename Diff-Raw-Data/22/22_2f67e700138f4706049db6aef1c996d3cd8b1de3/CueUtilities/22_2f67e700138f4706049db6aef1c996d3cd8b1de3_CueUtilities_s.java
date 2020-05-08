 /**
  * 
  */
 package org.opf_labs.arc_cd.cue;
 
import java.util.Arrays;
 import java.util.Iterator;
import java.util.List;
 
 import org.apache.commons.io.FilenameUtils;
 import org.opf_labs.arc_cd.cdrdao.toc.AudioTrack;
 import org.opf_labs.arc_cd.cdrdao.toc.TocItemRecord;
 import org.opf_labs.arc_cd.collection.CdItemRecord;
 import org.opf_labs.arc_cd.collection.CdTrack;
 import org.opf_labs.audio.CueSheet;
 import org.opf_labs.audio.FileData;
 import org.opf_labs.audio.Index;
 import org.opf_labs.audio.Position;
 import org.opf_labs.audio.TrackData;
 
 import com.google.common.base.Preconditions;
 
 /**
  * @author carl
  *
  */
 public final class CueUtilities {
 	public static final String TIME_DELIMETER = ":";
 	public static final String TOC_START_TIME = "0";
 	public static final int HUNDRETHS_PER_SECOND = 100;
 	public static final int SECONDS_PER_MINUTE = 60;
 	public static final int FRAMES_PER_SECOND = 75;
 	private static final int MINUTES_INDEX = 0;
 	private static final int SECONDS_INDEX = 1;
 	private static final int HUNDRETHS_INDEX = 2;
 
 	private CueUtilities() {
 		throw new AssertionError("Should not be in " + this.getClass().getName() + " default constructor.");
 	}
 	
 	public static Position timeToPosition(final String time) {
 		Preconditions.checkNotNull(time, "time == null");
 		Preconditions.checkArgument(!time.isEmpty(), "time.isEmpty()");
 		int minutes = 0;
 		int seconds = 0;
 		int frames = 0;
 		if (!time.equals(TOC_START_TIME)) {
 			String[] timeParts = time.split(TIME_DELIMETER);
 			if (timeParts.length != 3) throw new AssertionError("Expecting 3 part time string with " + TIME_DELIMETER + " delimeter: mm:ss:hh");
 			frames = hundrethsToFrames(timeParts[HUNDRETHS_INDEX]);
 			seconds = Integer.parseInt(timeParts[SECONDS_INDEX]);
 			if (frames >= FRAMES_PER_SECOND) {
 				seconds+=(frames / FRAMES_PER_SECOND);
 				frames = frames % FRAMES_PER_SECOND;
 			}
 			minutes = Integer.parseInt(timeParts[MINUTES_INDEX]);
 			if (seconds >= SECONDS_PER_MINUTE) {
 				minutes+=(seconds / SECONDS_PER_MINUTE);
 				seconds = seconds % SECONDS_PER_MINUTE;
 			}
 		}
 		return new Position(minutes, seconds, frames);
 	}
 	
 	public static int hundrethsToFrames(final String hundreths) {
 		Preconditions.checkNotNull(hundreths, "hundreths == null");
 		Preconditions.checkArgument(!hundreths.isEmpty(), "hundreths.isEmpty()");
 		float fltHundreths = Float.parseFloat(hundreths);
 		return Math.round((fltHundreths * FRAMES_PER_SECOND) / HUNDRETHS_PER_SECOND);
 	}
 	
 	public static CueSheet cueFromTocAndInfo(final TocItemRecord toc, final CdItemRecord info) {
 		Preconditions.checkNotNull(toc, "toc == null");
 		Preconditions.checkNotNull(info, "info == null");
 		Preconditions.checkState(toc.getTracks().size() == info.getTracks().size(), "TocItemRecord and CdItemRecord have different number of tracks.");
 
 		CueSheet cue = cueSheetFromInfo(info);
 		FileData fileData = new FileData(cue);
 		cue.getFileData().add(fileData);
 
 		Iterator<CdTrack> infoTracks = info.getTracks().iterator();
 		Iterator<AudioTrack> tocTracks = toc.getTracks().iterator();
 		while (infoTracks.hasNext() && tocTracks.hasNext()) {
 			AudioTrack audioTrack = tocTracks.next();
 			CdTrack infoTrack = infoTracks.next();
 			if (fileData.getFile() == null) {
 				fileData.setFile(FilenameUtils.getName(audioTrack.getFile()));
 			}
 			TrackData cueTrack = trackDataFromTocAndInfoTracks(fileData, audioTrack, infoTrack);
 			fileData.getTrackData().add(cueTrack);
 		}
 
 		return cue;
 	}
 	
 	private static CueSheet cueSheetFromInfo(CdItemRecord info) {
 		CueSheet cue = new CueSheet();
 		cue.setPerformer(info.getAlbumArtist());
 		cue.setTitle(info.getTitle());
 		return cue;
 	}
 	
 	private static TrackData trackDataFromTocAndInfoTracks(final FileData parent, final AudioTrack tocTrack, final CdTrack infoTrack) {
 		TrackData cueTrack = new TrackData(parent, tocTrack.getNumber(), "AUDIO");
 		if (!tocTrack.getIsrc().isEmpty()) {
 			cueTrack.setIsrcCode(tocTrack.getIsrc());
 		}
 		cueTrack.setTitle(infoTrack.getTitle());
 		if (!infoTrack.getArtist().isEmpty() && !infoTrack.getArtist().equals(CdTrack.DEFAULT_ARTIST)) {
 			cueTrack.setPerformer(infoTrack.getArtist());
 		}
 		Index index = new Index(1, CueUtilities.timeToPosition(tocTrack.getStart()));
 		cueTrack.getIndices().add(index);
 		return cueTrack;
 	}
 }
