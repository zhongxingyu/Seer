 package my.triviagame.xmcd;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import com.sun.tools.javac.util.Pair;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import my.triviagame.dal.AlbumRow;
 import my.triviagame.dal.TrackRow;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.text.WordUtils;
 
 /**
  * Work with a single XMCD file.
  */
 public class XmcdDisc {
     
     public static final short INVALID_YEAR = Short.MIN_VALUE;
 
     public final AlbumRow albumRow;
     public final List<TrackRow> trackRows;
     
     /**
      * Initializes an {@link XmcdDisc} from the contents of an xmcd file.
      * Parses the entire file with one pass.
      *
      * @param contents all the contents of an xmcd file
      * @param freedbGenre the FreeDB genre (corresponds with the directory name)
      */
     public static XmcdDisc fromXmcdFile(String contents, FreedbGenre freedbGenre)
             throws XmcdFormatException, XmcdMissingInformationException {
         AlbumRow albumRow = new AlbumRow();
         albumRow.freedbGenre = (byte)freedbGenre.ordinal();
         Matcher m = XmcdRegEx.SIGNATURE.matcher(contents);
         validateXmcdSignature(m);
         List<Integer> trackFrameOffsets = parseTrackOffsets(m);
         short totalLenSec = parseDiscLength(m, albumRow);
         parseDiscRevision(m, albumRow);
         parseDiscId(m, albumRow);
         parseDiscTitle(m, albumRow);
         parseDiscYear(m, albumRow);
         parseDiscGenre(m, albumRow);
         List<TrackRow> trackRows = parseTrackTitles(m, albumRow, trackFrameOffsets, totalLenSec);
         return new XmcdDisc(albumRow, trackRows);
     }
     
     public XmcdDisc(AlbumRow albumRow, List<TrackRow> trackRows) {
         this.albumRow = albumRow;
         this.trackRows = Collections.unmodifiableList(trackRows);
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof XmcdDisc)) {
             return false;
         }
         XmcdDisc other = (XmcdDisc) obj;
         return (Objects.equal(albumRow, other.albumRow) &&
                 Objects.equal(trackRows, other.trackRows));
     }
 
     @Override
     public int hashCode() {
         return albumRow.hashCode();
     }
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 
     private static void validateXmcdSignature(Matcher m)
             throws XmcdFormatException, NumberFormatException, XmcdMissingInformationException {
         m.usePattern(XmcdRegEx.SIGNATURE);
         if (!m.lookingAt()) {
             throw new XmcdFormatException("Missing xmcd signature");
         }
     }
 
     private static List<Integer> parseTrackOffsets(Matcher m)
             throws NumberFormatException, XmcdMissingInformationException {
         // Find track offsets
         m.usePattern(XmcdRegEx.TRACK_FRAME_OFFSETS_START);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing track frame offsets");
         }
         List<Integer> trackFrameOffsets = Lists.newArrayList();
         m.usePattern(XmcdRegEx.TRACK_FRAME_OFFSET);
         int regionStart = m.regionStart();
         while (m.find()) {
             if (m.group(1) == null) {
                 // Found list terminator
                 break;
             }
             trackFrameOffsets.add(Integer.parseInt(m.group(1)));
             regionStart = m.end();
         }
         // Reset the region in case we left the previous loop after failing to find the list terminator.
         // This happens in some malformed xmcd files. Finding the terminator fails and the matcher's state is affected
         // such that the region required resetting.
         m.region(regionStart, m.regionEnd());
         return trackFrameOffsets;
     }
 
     private static short parseDiscLength(Matcher m, AlbumRow albumRow)
             throws XmcdMissingInformationException, NumberFormatException {
         // Find disc length
         m.usePattern(XmcdRegEx.DISC_LENGTH);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing disc length");
         }
         return Short.parseShort(m.group(1));
     }
 
     private static void parseDiscRevision(Matcher m, AlbumRow albumRow) throws NumberFormatException {
         // Find revision
         m.usePattern(XmcdRegEx.REVISION);
         if (m.find()) {
             albumRow.revision = Integer.parseInt(m.group(1));
         }
         // If no match found for revision, retain the default value
     }
 
     private static void parseDiscId(Matcher m, AlbumRow albumRow)
             throws XmcdMissingInformationException, NumberFormatException {
         // Find disc ID
         m.usePattern(XmcdRegEx.DISC_ID);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing FreeDB ID");
         }
         // Parsing as long is required because the FreeDB ID as an 8-nibble string may be parsed as a high unsigned int.
         // Casting back to int is assumed to be safe because the FreeDB ID is 8 nibbles.
         albumRow.freedbId = (int)(Long.parseLong(m.group(1), 16));
     }
 
     private static void parseDiscTitle(Matcher m, AlbumRow albumRow)
             throws XmcdMissingInformationException, XmcdException {
         // Find disc title
         m.usePattern(XmcdRegEx.DISC_TITLE);
         List<String> titleLines = Lists.newLinkedList();
         while (m.find()) {
             titleLines.add(m.group(1));
         }
         if (titleLines.isEmpty()) {
             throw new XmcdMissingInformationException("Missing disc title");
         }
         String title = Joiner.on(" ").join(titleLines);
         String[] titleParts = title.split(" / ", 2);
         albumRow.artistName = WordUtils.capitalize(titleParts[0]);
         if (titleParts.length > 1) {
             // Album title is specified
             albumRow.title = WordUtils.capitalize(titleParts[1]);
         } else {
             // Unspecified title, album is assumed to be titled after the artist
             albumRow.title = albumRow.artistName;
         }
     }
 
     private static void parseDiscYear(Matcher m, AlbumRow albumRow)
             throws XmcdMissingInformationException, NumberFormatException {
         // Find disc year
         m.usePattern(XmcdRegEx.YEAR);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing year");
         }
         if (m.group(1) == null) {
             albumRow.year = INVALID_YEAR;
         } else {
             albumRow.year = Short.parseShort(m.group(1));
         }
     }
 
     private static void parseDiscGenre(Matcher m, AlbumRow albumRow) {
         // Find disc genre
         m.usePattern(XmcdRegEx.GENRE);
         if (!m.find()) {
             // Use the FreeDB genre (converted to titlecase, e.g. "Pop")
            albumRow.freeTextGenre = WordUtils.capitalize(FreedbGenre.values()[albumRow.genreId].name().toLowerCase());
         } else {
             albumRow.freeTextGenre = WordUtils.capitalize(m.group(1));
         }
     }
 
     private static List<TrackRow> parseTrackTitles(Matcher m, AlbumRow albumRow, List<Integer> trackFrameOffsets,
             short totalLenSec)
             throws XmcdFormatException, NumberFormatException, XmcdMissingInformationException {
         // Find track numbers and titles
         m.usePattern(XmcdRegEx.TRACK_TITLE);
         List<Pair<Byte, String>> trackTitles = Lists.newArrayList();
         byte prevNum = Byte.MIN_VALUE;
         while (m.find()) {
             byte trackNum = Byte.parseByte(m.group(1));
             String trackText = m.group(2);
             if (trackNum == prevNum) {
                 // Coalesce with previous record
                 String prevText = trackTitles.get(trackTitles.size() - 1).snd;
                 trackText = prevText + " " + trackText;
                 trackTitles.set(trackTitles.size() - 1, Pair.of(trackNum, trackText));
             } else {
                 trackTitles.add(Pair.of(trackNum, trackText));
             }
             prevNum = trackNum;
         }
         if (trackTitles.isEmpty()) {
             throw new XmcdMissingInformationException("Missing track titles");
         }
         
         // Build tracks out of numbers, titles and lengths
         List<Short> lengths =
                 FrameOffsetsToTrackLengthsSeconds.toTrackLengthsSeconds(trackFrameOffsets, totalLenSec);
         if (trackTitles.size() > lengths.size()) {
             throw new XmcdFormatException(
                     String.format("%d tracks but only %d frame offsets", trackTitles.size(), trackFrameOffsets.size()));
         }
         List<TrackRow> trackRows = Lists.newArrayListWithCapacity(trackTitles.size());
         for (Pair<Byte, String> track : trackTitles) {
             TrackRow trackRow = new TrackRow();
             trackRow.trackNum = track.fst;
             trackRow.lenInSec = lengths.get(track.fst);
             String[] titleParts = track.snd.split(" / ", 2);
             if (titleParts.length > 1) {
                 // Track has a specified artist
                 trackRow.artistName = WordUtils.capitalize(titleParts[0]);
                 trackRow.title = WordUtils.capitalize(titleParts[1]);
             } else {
                 // Track artist is the same as the album artist
                 trackRow.artistName = albumRow.artistName;
                 trackRow.title = WordUtils.capitalize(titleParts[0]);
             }
             trackRows.add(trackRow);
         }
         return trackRows;
     }
 }
