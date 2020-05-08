 /**
  * 
  */
 package org.opf_labs.arc_cd.collection;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Preconditions;
 
 
 /**
  * @author carl
  */
 public final class CdItemRecord {
 	private static final String DEFAULT_STRING_VALUE = "UNKNOWN";
 	public static final CdItemRecord DEFAULT_ITEM = new CdItemRecord();
 	public static final String VARIOUS = "Various Artists";
 	private static final String OPEN_DELIM = "\\[";
 	private static final String CLOSE_DELIM = "\\]";
 	public static final String ALBUM_TITLE = "AlbumTitle";
 	public static final String ALBUM_ARTIST = "AlbumArtist";
 	public static final String TRACK = "Track";
 	public static final String TRACK_ARTIST = "TrackArtist";
	private static final String GRABBER = "(.+)$";
 	private static final Pattern ALBUM_TITLE_PATTERN = Pattern.compile("^" + OPEN_DELIM + ALBUM_TITLE + CLOSE_DELIM + GRABBER);
 	private static final Pattern ALBUM_ARTIST_PATTERN = Pattern.compile("^" + OPEN_DELIM + ALBUM_ARTIST + CLOSE_DELIM + GRABBER);
 	private static final Pattern TRACK_TITLE_PATTERN = Pattern.compile("^" + OPEN_DELIM + TRACK + CLOSE_DELIM + GRABBER);
 	private static final Pattern TRACK_ARTIST_PATTERN = Pattern.compile("^" + OPEN_DELIM + TRACK_ARTIST + CLOSE_DELIM + GRABBER);
 
 	private static Logger LOGGER = Logger.getLogger(CdItemRecord.class); 
 
 	final String title;
 	final String albumArtist;
 	final boolean isCompilation;
 	final List<CdTrack> tracks;
 	final boolean isArchived;
 	
 	private CdItemRecord() {
 		this(DEFAULT_STRING_VALUE, DEFAULT_STRING_VALUE, new ArrayList<CdTrack>());
 	}
 	
 	private CdItemRecord(final String title, final String artist, final List<CdTrack> tracks) {
 		this(title, artist, tracks, false);
 	}
 
 	private CdItemRecord(final String title, final String artist, final List<CdTrack> tracks, final boolean isArchived) {
 		this.title = title;
 		this.albumArtist = (artist == null || artist.isEmpty()) ? VARIOUS : artist;
 		this.isCompilation = (this.albumArtist == VARIOUS);
 		this.tracks = new ArrayList<>(tracks);
 		this.isArchived = isArchived;
 	}
 	
 	/**
 	 * 
 	 * @return the item title, the title of the cd
 	 */
 	public String getTitle() {
 		return this.title;
 	}
 	
 	/**
 	 * 
 	 * @return the album artist for the CD item
 	 */
 	public String getAlbumArtist() {
 		return this.albumArtist;
 	}
 	
 	/**
 	 * 
 	 * @return a java.util.List of Track items, one for each track on the CD
 	 */
 	public List<CdTrack> getTracks() {
 		return this.tracks;
 	}
 	
 	/**
 	 * @return the default instance of a CdItemRecord
 	 */
 	public static CdItemRecord defaultItem() {
 		return DEFAULT_ITEM;
 	}
 
 	/**
 	 * Factory method that instantiates the 
 	 * @param itemFile
 	 * @return a CdItemRecord populated from file
 	 * @throws FileNotFoundException when the item file can't be found
 	 */
 	public static CdItemRecord fromInfoFile(File itemFile) throws FileNotFoundException {
 		Preconditions.checkNotNull(itemFile, "itemFile is null");
 		Builder builder = new Builder();
 		try (BufferedReader br = new BufferedReader(new FileReader(itemFile))) {
 			builder = Builder.fromBufferedReader(br);
 		} catch (IOException excep1) {
 			LOGGER.info("Problem closing item file:" + itemFile.getAbsolutePath());
 		}
 		return builder.build();
 	}
 	
 	/**
 	 * Simple builder for the immutable CdItemRecord objects.
 	 * 
 	 * @author  <a href="mailto:carl@openplanetsfoundation.org">Carl Wilson</a>.</p>
 	 *          <a href="https://github.com/carlwilson">carlwilson AT github</a>.</p>
 	 * @version 0.1
 	 * 
 	 * Created 29 Sep 2013:20:47:47
 	 */
 	@SuppressWarnings({"javadoc", "hiding"})
 	public static final class Builder {
 		private String title = DEFAULT_STRING_VALUE;
 		private String albumArtist = DEFAULT_STRING_VALUE;
 		boolean isCompilation = false;
 		List<CdTrack> tracks = new ArrayList<>();
 		boolean isArchived = false;
 		
 		/**	creates a bare bones, default instance */
 		public Builder() {
 			// just the defaults required
 		}
 		
 		/**
 		 * Creates an instance with param title 
 		 * @param title the title of the CD
 		 */
 		public Builder(final String title) {
 			this.title = title;
 		}
 		
 		public Builder title(final String title) {
 			this.title = title;
 			return this;
 		}
 		
 		public Builder artist(final String artist) {
 			this.albumArtist = artist;
 			return this;
 		}
 		
 		public Builder tracks(final List<CdTrack> tracks) {
 			this.tracks = tracks;
 			return this;
 		}
 		
 		public Builder track(final CdTrack track) {
 			this.tracks.add(track);
 			return this;
 		}
 		
 		public Builder archived(final boolean archived) {
 			this.isArchived = archived;
 			return this;
 		}
 		
 		public static Builder fromBufferedReader(BufferedReader reader) {
 			Builder builder = new Builder();
 			String line;
 			try {
 				while ((line = reader.readLine()) != null) {
 					builder.updateFromInfoLine(line.trim());
 				}
 			} catch (IOException excep) {
 				// Going to return what we can
 				LOGGER.warn("Problem reading line from reader");
 			}
 			return builder;
 		}
 		
 		private Builder updateFromInfoLine(final String infoLine) {
			Matcher trackMatcher = TRACK_TITLE_PATTERN.matcher(infoLine);
			if (trackMatcher.matches()) {
				this.tracks.add(CdTrack.fromValues(trackMatcher.group(1)));
				return this;
			}
			Matcher albumTitleMatcher = ALBUM_TITLE_PATTERN.matcher(infoLine);
			if (albumTitleMatcher.matches()) {
				this.title = albumTitleMatcher.group(1);
				return this;
			}
			Matcher albumArtistMatcher = ALBUM_ARTIST_PATTERN.matcher(infoLine);
			if (albumArtistMatcher.matches()) {
				this.albumArtist = albumArtistMatcher.group(1);
				return this;
 			}
 			return this;
 		}
 		
 		public CdItemRecord build() {
 			return new CdItemRecord(this.title, this.albumArtist, this.tracks, this.isArchived);
 		}
 	}
 }
