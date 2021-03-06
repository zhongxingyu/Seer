 package org.jaudiotagger.tag.mp4;
 
 import org.jaudiotagger.AbstractTestCase;
 import org.jaudiotagger.tag.*;
 import org.jaudiotagger.tag.reference.GenreTypes;
 import org.jaudiotagger.tag.mp4.Mp4FieldKey;
 import org.jaudiotagger.tag.mp4.Mp4Tag;
 import org.jaudiotagger.tag.mp4.atom.Mp4ContentTypeValue;
 import org.jaudiotagger.tag.mp4.atom.Mp4RatingValue;
 import org.jaudiotagger.tag.mp4.field.*;
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.audio.mp4.Mp4AudioHeader;
 import org.jaudiotagger.audio.mp4.atom.Mp4EsdsBox;
 import org.jaudiotagger.audio.exceptions.CannotReadException;
 
 import java.io.File;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.List;
 import java.awt.image.BufferedImage;
 import java.nio.charset.Charset;
 import java.nio.ByteBuffer;
 
 import junit.framework.TestCase;
 
 import javax.imageio.ImageIO;
 
 /**
  */
 public class M4aReadTagTest extends TestCase
 {
     /**
      * Test to read all metadata from an Apple iTunes encoded m4a file
      */
     public void testReadFile()
     {
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test.m4a");
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //AudioInfo
             //Time in seconds
             assertEquals(241, f.getAudioHeader().getTrackLength());
             assertEquals(44100, f.getAudioHeader().getSampleRateAsNumber());
             assertEquals(new String("2"), f.getAudioHeader().getChannels());
             assertEquals(128, f.getAudioHeader().getBitRateAsNumber());
 
             //MPEG Specific
             Mp4AudioHeader audioheader = (Mp4AudioHeader) f.getAudioHeader();
             assertEquals(Mp4EsdsBox.Kind.MPEG4_AUDIO, audioheader.getKind());
             assertEquals(Mp4EsdsBox.AudioProfile.LOW_COMPLEXITY, audioheader.getProfile());
 
             //Ease of use methods for common fields
             assertEquals("Artist", tag.getFirstArtist());
             assertEquals("Album", tag.getFirstAlbum());
             assertEquals("title", tag.getFirstTitle());
             assertEquals("comments", tag.getFirstComment());
             assertEquals("1971", tag.getFirstYear());
             assertEquals("1/10", tag.getFirstTrack());
 
             //Although using custom genre this call works this out and gets correct value
             assertEquals("Genre", tag.getFirstGenre());
 
             //Lookup by generickey
             assertEquals("Artist", tag.getFirst(TagFieldKey.ARTIST));
             assertEquals("Album", tag.getFirst(TagFieldKey.ALBUM));
             assertEquals("title", tag.getFirst(TagFieldKey.TITLE));
             assertEquals("comments", tag.getFirst(TagFieldKey.COMMENT));
             assertEquals("1971", tag.getFirst(TagFieldKey.YEAR));
             assertEquals("1/10", tag.getFirst(TagFieldKey.TRACK));
             assertEquals("1/10", tag.getFirst(TagFieldKey.DISC_NO));
             assertEquals("composer", tag.getFirst(TagFieldKey.COMPOSER));
             assertEquals("Sortartist", tag.getFirst(TagFieldKey.ARTIST_SORT));
             assertEquals("lyrics", tag.getFirst(TagFieldKey.LYRICS));
             assertEquals("199", tag.getFirst(TagFieldKey.BPM));
             assertEquals("Albumartist", tag.getFirst(TagFieldKey.ALBUM_ARTIST));
             assertEquals("Sortalbumartist", tag.getFirst(TagFieldKey.ALBUM_ARTIST_SORT));
             assertEquals("Sortalbum", tag.getFirst(TagFieldKey.ALBUM_SORT));
             assertEquals("GROUping", tag.getFirst(TagFieldKey.GROUPING));
             assertEquals("Sortcomposer", tag.getFirst(TagFieldKey.COMPOSER_SORT));
             assertEquals("sorttitle", tag.getFirst(TagFieldKey.TITLE_SORT));
             assertEquals("1", tag.getFirst(TagFieldKey.IS_COMPILATION));
             assertEquals("iTunes v7.4.3.1, QuickTime 7.2", tag.getFirst(TagFieldKey.ENCODER));
             assertEquals("66027994-edcf-9d89-bec8-0d30077d888c", tag.getFirst(TagFieldKey.MUSICIP_ID));
             assertEquals("e785f700-c1aa-4943-bcee-87dd316a2c30", tag.getFirst(TagFieldKey.MUSICBRAINZ_TRACK_ID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", tag.getFirst(TagFieldKey.MUSICBRAINZ_ARTISTID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", tag.getFirst(TagFieldKey.MUSICBRAINZ_RELEASEARTISTID));
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", tag.getFirst(TagFieldKey.MUSICBRAINZ_RELEASEID));
 
             //Cast to format specific tag
             Mp4Tag mp4tag = (Mp4Tag) tag;
 
             //Lookup by mp4 key
             assertEquals("Artist", mp4tag.getFirst(Mp4FieldKey.ARTIST));
             assertEquals("Album", mp4tag.getFirst(Mp4FieldKey.ALBUM));
             assertEquals("title", mp4tag.getFirst(Mp4FieldKey.TITLE));
             assertEquals("comments", mp4tag.getFirst(Mp4FieldKey.COMMENT));
             assertEquals("1971", mp4tag.getFirst(Mp4FieldKey.DAY));
 
             //Not sure why there are 4 values, only understand 2nd and third
             assertEquals("1/10", mp4tag.getFirst(Mp4FieldKey.TRACK));
             assertEquals("1/10", ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getContent());
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(0));
             assertEquals(new Short("1"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(1));
             assertEquals(new Short("10"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(2));
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(3));
             assertEquals(new Short("1"), ((Mp4TrackField) mp4tag.getFirstField(Mp4FieldKey.TRACK)).getTrackNo());
             assertEquals(new Short("10"), ((Mp4TrackField) mp4tag.getFirstField(Mp4FieldKey.TRACK)).getTrackTotal());
 
             //Not sure why there are 4 values, only understand 2nd and third
             assertEquals("1/10", mp4tag.getFirst(Mp4FieldKey.DISCNUMBER));
             assertEquals("1/10", ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.DISCNUMBER).get(0)).getContent());
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getNumbers().get(0));
             assertEquals(new Short("1"), ((Mp4TagTextNumberField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getNumbers().get(1));
             assertEquals(new Short("10"), ((Mp4TagTextNumberField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getNumbers().get(2));
             assertEquals(new Short("1"), ((Mp4DiscNoField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getDiscNo());
             assertEquals(new Short("10"), ((Mp4DiscNoField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getDiscTotal());
 
             assertEquals("composer", mp4tag.getFirst(Mp4FieldKey.COMPOSER));
             assertEquals("Sortartist", mp4tag.getFirst(Mp4FieldKey.ARTIST_SORT));
             assertEquals("lyrics", mp4tag.getFirst(Mp4FieldKey.LYRICS));
             assertEquals("199", mp4tag.getFirst(Mp4FieldKey.BPM));
             assertEquals("Albumartist", mp4tag.getFirst(Mp4FieldKey.ALBUM_ARTIST));
             assertEquals("Sortalbumartist", mp4tag.getFirst(Mp4FieldKey.ALBUM_ARTIST_SORT));
             assertEquals("Sortalbum", mp4tag.getFirst(Mp4FieldKey.ALBUM_SORT));
             assertEquals("GROUping", mp4tag.getFirst(Mp4FieldKey.GROUPING));
             assertEquals("Sortcomposer", mp4tag.getFirst(Mp4FieldKey.COMPOSER_SORT));
             assertEquals("sorttitle", mp4tag.getFirst(Mp4FieldKey.TITLE_SORT));
             assertEquals("1", mp4tag.getFirst(Mp4FieldKey.COMPILATION));
             assertEquals("66027994-edcf-9d89-bec8-0d30077d888c", mp4tag.getFirst(Mp4FieldKey.MUSICIP_PUID));
             assertEquals("e785f700-c1aa-4943-bcee-87dd316a2c30", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_TRACKID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ARTISTID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ALBUMARTISTID));
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ALBUMID));
 
             Mp4TagReverseDnsField rvs = (Mp4TagReverseDnsField) mp4tag.getFirstField(Mp4FieldKey.MUSICBRAINZ_ALBUMID);
             assertEquals("com.apple.iTunes", rvs.getIssuer());
             assertEquals("MusicBrainz Album Id", rvs.getDescriptor());
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", rvs.getContent());
 
             //Lookup by mp4key (no generic key mapping for these yet)
             assertEquals(" 000002C0 00000298 00004210 00002FD5 0001CB31 0001CB48 0000750D 00007C4A 000291A8 00029191", mp4tag.getFirst(Mp4FieldKey.ITUNES_NORM));
             assertEquals(" 00000000 00000840 000000E4 0000000000A29EDC 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000", mp4tag.getFirst(Mp4FieldKey.ITUNES_SMPB));
             assertEquals("0", mp4tag.getFirst(Mp4FieldKey.PART_OF_GAPLESS_ALBUM));
             assertEquals("iTunes v7.4.3.1, QuickTime 7.2", mp4tag.getFirst(Mp4FieldKey.ENCODER));
             assertEquals("sortshow", mp4tag.getFirst(Mp4FieldKey.SHOW_SORT));
             assertEquals("show", mp4tag.getFirst(Mp4FieldKey.SHOW));
             assertEquals("Genre", mp4tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
             assertEquals(String.valueOf(Mp4RatingValue.EXPLICIT.getId()), mp4tag.getFirst(Mp4FieldKey.RATING));
             assertEquals(String.valueOf(Mp4ContentTypeValue.BOOKLET.getId()), mp4tag.getFirst(Mp4FieldKey.CONTENT_TYPE));
             List coverart = mp4tag.get(Mp4FieldKey.ARTWORK);
             //Should be one image
             assertEquals(1, coverart.size());
 
 
             Mp4TagCoverField coverArtField = (Mp4TagCoverField) coverart.get(0);
             //Check type jpeg
             assertEquals(Mp4FieldType.COVERART_JPEG, coverArtField.getFieldType());
             //Just check jpeg signature
             assertEquals(0xff, coverArtField.getData()[0] & 0xff);
             assertEquals(0xd8, coverArtField.getData()[1] & 0xff);
             assertEquals(0xff, coverArtField.getData()[2] & 0xff);
             assertEquals(0xe0, coverArtField.getData()[3] & 0xff);
             //Recreate the image
             BufferedImage bi = ImageIO.read(ImageIO
                     .createImageInputStream(new ByteArrayInputStream(coverArtField.getData())));
             assertNotNull(bi);
 
         }
         catch (Exception e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     /**
      * Test to read all metadata from an Apple iTunes encoded m4a file , this tests a few items that could not
      * be tested with first test. Namely genre picked from list, and png item instead of jpg
      * <p/>
      * TODO:Although selected genre from a list still seems to be using a custom genre
      */
     public void testReadFile2()
     {
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test2.m4a");
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //AudioInfo
             //Time in seconds
             assertEquals(241, f.getAudioHeader().getTrackLength());
             assertEquals(44100, f.getAudioHeader().getSampleRateAsNumber());
 
             //MPEG Specific
             Mp4AudioHeader audioheader = (Mp4AudioHeader) f.getAudioHeader();
             assertEquals(Mp4EsdsBox.Kind.MPEG4_AUDIO, audioheader.getKind());
             assertEquals(Mp4EsdsBox.AudioProfile.LOW_COMPLEXITY, audioheader.getProfile());
 
             //Ease of use methods for common fields
             assertEquals("Artist\u01fft", tag.getFirstArtist());
             assertEquals("Album", tag.getFirstAlbum());
             assertEquals("title", tag.getFirstTitle());
             assertEquals("comments", tag.getFirstComment());
             assertEquals("1971", tag.getFirstYear());
             assertEquals("1/10", tag.getFirstTrack());
 
             //Althjough using cusotm genre this call works this out and gets correct value
             assertEquals("Religious", tag.getFirstGenre());
 
             //Lookup by generickey
             assertEquals("Artist\u01fft", tag.getFirst(TagFieldKey.ARTIST));
             assertEquals("Album", tag.getFirst(TagFieldKey.ALBUM));
             assertEquals("title", tag.getFirst(TagFieldKey.TITLE));
             assertEquals("comments", tag.getFirst(TagFieldKey.COMMENT));
             assertEquals("1971", tag.getFirst(TagFieldKey.YEAR));
             assertEquals("1/10", tag.getFirst(TagFieldKey.TRACK));
             assertEquals("1/10", tag.getFirst(TagFieldKey.DISC_NO));
             assertEquals("composer", tag.getFirst(TagFieldKey.COMPOSER));
             assertEquals("Sortartist", tag.getFirst(TagFieldKey.ARTIST_SORT));
             assertEquals("lyrics", tag.getFirst(TagFieldKey.LYRICS));
             assertEquals("199", tag.getFirst(TagFieldKey.BPM));
             assertEquals("Albumartist", tag.getFirst(TagFieldKey.ALBUM_ARTIST));
             assertEquals("Sortalbumartist", tag.getFirst(TagFieldKey.ALBUM_ARTIST_SORT));
             assertEquals("Sortalbum", tag.getFirst(TagFieldKey.ALBUM_SORT));
             assertEquals("GROUping", tag.getFirst(TagFieldKey.GROUPING));
             assertEquals("Sortcomposer", tag.getFirst(TagFieldKey.COMPOSER_SORT));
             assertEquals("sorttitle", tag.getFirst(TagFieldKey.TITLE_SORT));
             assertEquals("1", tag.getFirst(TagFieldKey.IS_COMPILATION));
             assertEquals("66027994-edcf-9d89-bec8-0d30077d888c", tag.getFirst(TagFieldKey.MUSICIP_ID));
             assertEquals("e785f700-c1aa-4943-bcee-87dd316a2c30", tag.getFirst(TagFieldKey.MUSICBRAINZ_TRACK_ID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", tag.getFirst(TagFieldKey.MUSICBRAINZ_ARTISTID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", tag.getFirst(TagFieldKey.MUSICBRAINZ_RELEASEARTISTID));
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", tag.getFirst(TagFieldKey.MUSICBRAINZ_RELEASEID));
 
             //Cast to format specific tag
             Mp4Tag mp4tag = (Mp4Tag) tag;
 
             //Lookup by mp4 key
             assertEquals("Artist\u01fft", mp4tag.getFirst(Mp4FieldKey.ARTIST));
             assertEquals("Album", mp4tag.getFirst(Mp4FieldKey.ALBUM));
             assertEquals("title", mp4tag.getFirst(Mp4FieldKey.TITLE));
             assertEquals("comments", mp4tag.getFirst(Mp4FieldKey.COMMENT));
             assertEquals("1971", mp4tag.getFirst(Mp4FieldKey.DAY));
             //Not sure why there are 4 values, only understand 2nd and third
             assertEquals("1/10", mp4tag.getFirst(Mp4FieldKey.TRACK));
             assertEquals("1/10", ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getContent());
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(0));
             assertEquals(new Short("1"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(1));
             assertEquals(new Short("10"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(2));
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(3));
 
             //Not sure why there are 4 values, only understand 2nd and third
             assertEquals("1/10", mp4tag.getFirst(Mp4FieldKey.DISCNUMBER));
             assertEquals("1/10", ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.DISCNUMBER).get(0)).getContent());
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.DISCNUMBER).get(0)).getNumbers().get(0));
             assertEquals(new Short("1"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.DISCNUMBER).get(0)).getNumbers().get(1));
             assertEquals(new Short("10"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.DISCNUMBER).get(0)).getNumbers().get(2));
 
             assertEquals("composer", mp4tag.getFirst(Mp4FieldKey.COMPOSER));
             assertEquals("Sortartist", mp4tag.getFirst(Mp4FieldKey.ARTIST_SORT));
             assertEquals("lyrics", mp4tag.getFirst(Mp4FieldKey.LYRICS));
             assertEquals("199", mp4tag.getFirst(Mp4FieldKey.BPM));
             assertEquals("Albumartist", mp4tag.getFirst(Mp4FieldKey.ALBUM_ARTIST));
             assertEquals("Sortalbumartist", mp4tag.getFirst(Mp4FieldKey.ALBUM_ARTIST_SORT));
             assertEquals("Sortalbum", mp4tag.getFirst(Mp4FieldKey.ALBUM_SORT));
             assertEquals("GROUping", mp4tag.getFirst(Mp4FieldKey.GROUPING));
             assertEquals("Sortcomposer", mp4tag.getFirst(Mp4FieldKey.COMPOSER_SORT));
             assertEquals("sorttitle", mp4tag.getFirst(Mp4FieldKey.TITLE_SORT));
             assertEquals("1", mp4tag.getFirst(Mp4FieldKey.COMPILATION));
             assertEquals("66027994-edcf-9d89-bec8-0d30077d888c", mp4tag.getFirst(Mp4FieldKey.MUSICIP_PUID));
             assertEquals("e785f700-c1aa-4943-bcee-87dd316a2c30", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_TRACKID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ARTISTID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ALBUMARTISTID));
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ALBUMID));
 
             //Lookup by mp4key (no generic key mapping for these yet)
             assertEquals("iTunes v7.4.3.1, QuickTime 7.2", mp4tag.getFirst(Mp4FieldKey.ENCODER));
             assertEquals("sortshow", mp4tag.getFirst(Mp4FieldKey.SHOW_SORT));
             assertEquals("show", mp4tag.getFirst(Mp4FieldKey.SHOW));
             assertEquals("Religious", mp4tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
             assertEquals("1", mp4tag.getFirst(Mp4FieldKey.PART_OF_GAPLESS_ALBUM));
             assertEquals(" 000002C0 00000298 00004210 00002FD5 0001CB31 0001CB48 0000750D 00007C4A 000291A8 00029191", mp4tag.getFirst(Mp4FieldKey.ITUNES_NORM));
             assertEquals(" 00000000 00000840 000000E4 0000000000A29EDC 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000", mp4tag.getFirst(Mp4FieldKey.ITUNES_SMPB));
 
             List coverart = mp4tag.get(Mp4FieldKey.ARTWORK);
             //Should be one image
             assertEquals(3, coverart.size());
 
             //Check 1st field
             Mp4TagCoverField coverArtField = (Mp4TagCoverField) coverart.get(0);
             //Check type png
             assertEquals(Mp4FieldType.COVERART_PNG, coverArtField.getFieldType());
             //Just check png signature
             assertEquals(0x89, coverArtField.getData()[0] & 0xff);
             assertEquals(0x50, coverArtField.getData()[1] & 0xff);
             assertEquals(0x4E, coverArtField.getData()[2] & 0xff);
             assertEquals(0x47, coverArtField.getData()[3] & 0xff);
 
             //Recreate the image
             BufferedImage bi = ImageIO.read(ImageIO
                     .createImageInputStream(new ByteArrayInputStream(coverArtField.getData())));
             assertNotNull(bi);
 
             //Check 2nd field
             coverArtField = (Mp4TagCoverField) coverart.get(1);
             //Check type png
             assertEquals(Mp4FieldType.COVERART_PNG, coverArtField.getFieldType());
             //Just check png signature
             assertEquals(0x89, coverArtField.getData()[0] & 0xff);
             assertEquals(0x50, coverArtField.getData()[1] & 0xff);
             assertEquals(0x4E, coverArtField.getData()[2] & 0xff);
             assertEquals(0x47, coverArtField.getData()[3] & 0xff);
 
             //Recreate the image
             bi = ImageIO.read(ImageIO
                     .createImageInputStream(new ByteArrayInputStream(coverArtField.getData())));
             assertNotNull(bi);
 
             //Check 3rd Field
             coverArtField = (Mp4TagCoverField) coverart.get(2);
             //Check type jpeg
             assertEquals(Mp4FieldType.COVERART_JPEG, coverArtField.getFieldType());
             //Just check jpeg signature
             assertEquals(0xff, coverArtField.getData()[0] & 0xff);
             assertEquals(0xd8, coverArtField.getData()[1] & 0xff);
             assertEquals(0xff, coverArtField.getData()[2] & 0xff);
             assertEquals(0xe0, coverArtField.getData()[3] & 0xff);
             //Recreate the image
             bi = ImageIO.read(ImageIO
                     .createImageInputStream(new ByteArrayInputStream(coverArtField.getData())));
             assertNotNull(bi);
 
         }
         catch (Exception e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     /**
      * Test to read all metadata from an Apple iTunes encoded m4a file which doesnt have a meta free atom
      */
     public void testReadFileWithNoMetaFreeAtom()
     {
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test3.m4a");
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //AudioInfo
             //Time in seconds
             assertEquals(241, f.getAudioHeader().getTrackLength());
             assertEquals(44100, f.getAudioHeader().getSampleRateAsNumber());
 
             //MPEG Specific
             Mp4AudioHeader audioheader = (Mp4AudioHeader) f.getAudioHeader();
             assertEquals(Mp4EsdsBox.Kind.MPEG4_AUDIO, audioheader.getKind());
             assertEquals(Mp4EsdsBox.AudioProfile.LOW_COMPLEXITY, audioheader.getProfile());
 
             //Ease of use methods for common fields
             assertEquals("Artist", tag.getFirstArtist());
             assertEquals("Album", tag.getFirstAlbum());
             assertEquals("title", tag.getFirstTitle());
             assertEquals("comments", tag.getFirstComment());
             assertEquals("1971", tag.getFirstYear());
             assertEquals("1/10", tag.getFirstTrack());
 
             //Althjough using cusotm genre this call works this out and gets correct value
             assertEquals("Genre", tag.getFirstGenre());
 
             //Lookup by generickey
             assertEquals("Artist", tag.getFirst(TagFieldKey.ARTIST));
             assertEquals("Album", tag.getFirst(TagFieldKey.ALBUM));
             assertEquals("title", tag.getFirst(TagFieldKey.TITLE));
             assertEquals("comments", tag.getFirst(TagFieldKey.COMMENT));
             assertEquals("1971", tag.getFirst(TagFieldKey.YEAR));
             assertEquals("1/10", tag.getFirst(TagFieldKey.TRACK));
             assertEquals("1/10", tag.getFirst(TagFieldKey.DISC_NO));
             assertEquals("composer", tag.getFirst(TagFieldKey.COMPOSER));
             assertEquals("Sortartist", tag.getFirst(TagFieldKey.ARTIST_SORT));
             assertEquals("lyrics", tag.getFirst(TagFieldKey.LYRICS));
             assertEquals("199", tag.getFirst(TagFieldKey.BPM));
             assertEquals("Albumartist", tag.getFirst(TagFieldKey.ALBUM_ARTIST));
             assertEquals("Sortalbumartist", tag.getFirst(TagFieldKey.ALBUM_ARTIST_SORT));
             assertEquals("Sortalbum", tag.getFirst(TagFieldKey.ALBUM_SORT));
             assertEquals("GROUping", tag.getFirst(TagFieldKey.GROUPING));
             assertEquals("Sortcomposer", tag.getFirst(TagFieldKey.COMPOSER_SORT));
             assertEquals("sorttitle", tag.getFirst(TagFieldKey.TITLE_SORT));
             assertEquals("1", tag.getFirst(TagFieldKey.IS_COMPILATION));
             assertEquals("66027994-edcf-9d89-bec8-0d30077d888c", tag.getFirst(TagFieldKey.MUSICIP_ID));
             assertEquals("e785f700-c1aa-4943-bcee-87dd316a2c30", tag.getFirst(TagFieldKey.MUSICBRAINZ_TRACK_ID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", tag.getFirst(TagFieldKey.MUSICBRAINZ_ARTISTID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", tag.getFirst(TagFieldKey.MUSICBRAINZ_RELEASEARTISTID));
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", tag.getFirst(TagFieldKey.MUSICBRAINZ_RELEASEID));
 
             //Cast to format specific tag
             Mp4Tag mp4tag = (Mp4Tag) tag;
 
             //Lookup by mp4 key
             assertEquals("Artist", mp4tag.getFirst(Mp4FieldKey.ARTIST));
             assertEquals("Album", mp4tag.getFirst(Mp4FieldKey.ALBUM));
             assertEquals("title", mp4tag.getFirst(Mp4FieldKey.TITLE));
             assertEquals("comments", mp4tag.getFirst(Mp4FieldKey.COMMENT));
             assertEquals("1971", mp4tag.getFirst(Mp4FieldKey.DAY));
             //Not sure why there are 4 values, only understand 2nd and third
             assertEquals("1/10", mp4tag.getFirst(Mp4FieldKey.TRACK));
             assertEquals("1/10", ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getContent());
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(0));
             assertEquals(new Short("1"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(1));
             assertEquals(new Short("10"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(2));
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.TRACK).get(0)).getNumbers().get(3));
             assertEquals(new Short("1"), ((Mp4TrackField) mp4tag.getFirstField(Mp4FieldKey.TRACK)).getTrackNo());
             assertEquals(new Short("10"), ((Mp4TrackField) mp4tag.getFirstField(Mp4FieldKey.TRACK)).getTrackTotal());
 
             //Not sure why there are 4 values, only understand 2nd and third
             assertEquals("1/10", mp4tag.getFirst(Mp4FieldKey.DISCNUMBER));
             assertEquals("1/10", ((Mp4TagTextNumberField) mp4tag.get(Mp4FieldKey.DISCNUMBER).get(0)).getContent());
             assertEquals(new Short("0"), ((Mp4TagTextNumberField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getNumbers().get(0));
             assertEquals(new Short("1"), ((Mp4TagTextNumberField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getNumbers().get(1));
             assertEquals(new Short("10"), ((Mp4TagTextNumberField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getNumbers().get(2));
             assertEquals(new Short("1"), ((Mp4DiscNoField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getDiscNo());
             assertEquals(new Short("10"), ((Mp4DiscNoField) mp4tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getDiscTotal());
 
             assertEquals("composer", mp4tag.getFirst(Mp4FieldKey.COMPOSER));
             assertEquals("Sortartist", mp4tag.getFirst(Mp4FieldKey.ARTIST_SORT));
             assertEquals("lyrics", mp4tag.getFirst(Mp4FieldKey.LYRICS));
             assertEquals("199", mp4tag.getFirst(Mp4FieldKey.BPM));
             assertEquals("Albumartist", mp4tag.getFirst(Mp4FieldKey.ALBUM_ARTIST));
             assertEquals("Sortalbumartist", mp4tag.getFirst(Mp4FieldKey.ALBUM_ARTIST_SORT));
             assertEquals("Sortalbum", mp4tag.getFirst(Mp4FieldKey.ALBUM_SORT));
             assertEquals("GROUping", mp4tag.getFirst(Mp4FieldKey.GROUPING));
             assertEquals("Sortcomposer", mp4tag.getFirst(Mp4FieldKey.COMPOSER_SORT));
             assertEquals("sorttitle", mp4tag.getFirst(Mp4FieldKey.TITLE_SORT));
             assertEquals("1", mp4tag.getFirst(Mp4FieldKey.COMPILATION));
             assertEquals("66027994-edcf-9d89-bec8-0d30077d888c", mp4tag.getFirst(Mp4FieldKey.MUSICIP_PUID));
             assertEquals("e785f700-c1aa-4943-bcee-87dd316a2c30", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_TRACKID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ARTISTID));
             assertEquals("989a13f6-b58c-4559-b09e-76ae0adb94ed", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ALBUMARTISTID));
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", mp4tag.getFirst(Mp4FieldKey.MUSICBRAINZ_ALBUMID));
 
             Mp4TagReverseDnsField rvs = (Mp4TagReverseDnsField) mp4tag.getFirstField(Mp4FieldKey.MUSICBRAINZ_ALBUMID);
             assertEquals("com.apple.iTunes", rvs.getIssuer());
             assertEquals("MusicBrainz Album Id", rvs.getDescriptor());
             assertEquals("19c6f0f6-3d6d-4b02-88c7-ffb559d52be6", rvs.getContent());
 
             //Lookup by mp4key (no generic key mapping for these yet)
             assertEquals(" 000002C0 00000298 00004210 00002FD5 0001CB31 0001CB48 0000750D 00007C4A 000291A8 00029191", mp4tag.getFirst(Mp4FieldKey.ITUNES_NORM));
             assertEquals(" 00000000 00000840 000000E4 0000000000A29EDC 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000", mp4tag.getFirst(Mp4FieldKey.ITUNES_SMPB));
             assertEquals("0", mp4tag.getFirst(Mp4FieldKey.PART_OF_GAPLESS_ALBUM));
             assertEquals("iTunes v7.4.3.1, QuickTime 7.2", mp4tag.getFirst(Mp4FieldKey.ENCODER));
             assertEquals("sortshow", mp4tag.getFirst(Mp4FieldKey.SHOW_SORT));
             assertEquals("show", mp4tag.getFirst(Mp4FieldKey.SHOW));
             assertEquals("Genre", mp4tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
             assertEquals(String.valueOf(Mp4RatingValue.EXPLICIT.getId()), mp4tag.getFirst(Mp4FieldKey.RATING));
             assertEquals(String.valueOf(Mp4ContentTypeValue.BOOKLET.getId()), mp4tag.getFirst(Mp4FieldKey.CONTENT_TYPE));
             List coverart = mp4tag.get(Mp4FieldKey.ARTWORK);
 
             //Should be one image
             assertEquals(1, coverart.size());
 
 
             Mp4TagCoverField coverArtField = (Mp4TagCoverField) coverart.get(0);
             //Check type jpeg
             assertEquals(Mp4FieldType.COVERART_JPEG, coverArtField.getFieldType());
             //Just check jpeg signature
             assertEquals(0xff, coverArtField.getData()[0] & 0xff);
             assertEquals(0xd8, coverArtField.getData()[1] & 0xff);
             assertEquals(0xff, coverArtField.getData()[2] & 0xff);
             assertEquals(0xe0, coverArtField.getData()[3] & 0xff);
             //Recreate the image
             BufferedImage bi = ImageIO.read(ImageIO
                     .createImageInputStream(new ByteArrayInputStream(coverArtField.getData())));
             assertNotNull(bi);
 
         }
         catch (Exception e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     public void testDetectVideo()
     {
         File orig = new File("testdata", "test7.mp4");
         if (!orig.isFile())
         {
             return;
         }
 
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test7.mp4");
             AudioFile f = AudioFileIO.read(testFile);
         }
         catch (Exception e)
         {
             e.printStackTrace(System.err);
             exceptionCaught = e;
         }
         assertNotNull(exceptionCaught);
         assertTrue(exceptionCaught instanceof CannotReadException);
     }
 
     /**
      * testing reading of header with low bit rate and mono channels
      */
     public void testMonoLowbitRateReadFile()
     {
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test5.m4a");
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //AudioInfo
             //Time in seconds
             assertEquals(14, f.getAudioHeader().getTrackLength());
             assertEquals(44100, f.getAudioHeader().getSampleRateAsNumber());
             assertEquals(new String("1"), f.getAudioHeader().getChannels());
             assertEquals(64, f.getAudioHeader().getBitRateAsNumber());
 
             //MPEG Specific
             Mp4AudioHeader audioheader = (Mp4AudioHeader) f.getAudioHeader();
             assertEquals(Mp4EsdsBox.Kind.MPEG4_AUDIO, audioheader.getKind());
             assertEquals(Mp4EsdsBox.AudioProfile.LOW_COMPLEXITY, audioheader.getProfile());
         }
         catch (Exception e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
 
     }
 
     /**
      * Test Issue #156:Handling of known fields with invalid fieldtypes
      *
      * @throws Exception
      */
     public void testIssue156() throws Exception
     {
         Exception exceptionCaught = null;
         try
         {
             File orig = new File("testdata", "test13.m4a");
             if (!orig.isFile())
             {
                 return;
             }
             File testFile = AbstractTestCase.copyAudioToTmp("test13.m4a");
 
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //AudioInfo
             //Time in seconds
             assertEquals(219, f.getAudioHeader().getTrackLength());
             assertEquals(44100, f.getAudioHeader().getSampleRateAsNumber());
 
             //MPEG Specific
             Mp4AudioHeader audioheader = (Mp4AudioHeader) f.getAudioHeader();
             assertEquals(Mp4EsdsBox.Kind.MPEG4_AUDIO, audioheader.getKind());
             assertEquals(Mp4EsdsBox.AudioProfile.LOW_COMPLEXITY, audioheader.getProfile());
 
             //These shouldn't be any values for these. because they have invalid fieldtype of 15 instead of 21
             assertEquals("", tag.getFirst(TagFieldKey.BPM));
             assertEquals("", tag.getFirst(TagFieldKey.IS_COMPILATION));
 
         }
         catch (IOException e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
 
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test3.m4a", new File("testIssue156.m4a"));
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //Allow calling getFirst() on binary fields, although value actually currently makes not much sense
             assertEquals("jpeg:8445bytes", tag.getFirst(TagFieldKey.COVER_ART));
 
         }
         catch (IOException e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     public void testIssue163()
     {
         Exception exceptionCaught = null;
         try
         {
             //Charset Testing UTf8
             String copyright_symbol = "\u00A9";
             ByteBuffer bb = Charset.forName("UTF-8").encode(copyright_symbol);
             bb.rewind();
             System.out.println("utf8 bb size is:" + bb.limit());
             {
                 System.out.println("utf8 byte value is " + (bb.get(0) & 0xFF));
                 System.out.println("utf8 byte value is " + (bb.get(1) & 0xFF));
             }
 
             bb = Charset.forName("ISO-8859-1").encode(copyright_symbol);
             bb.rewind();
             System.out.println("ISO-8859-1 bb size is:" + bb.limit());
             {
                 System.out.println("ISO-8859-1 byte value is " + (bb.get(0) & 0xFF));
             }
 
             File orig = new File("testdata", "unable_to_read.m4a");
             if (!orig.isFile())
             {
                 return;
             }
             File testFile = AbstractTestCase.copyAudioToTmp("unable_to_read.m4a");
 
             AudioFile f = AudioFileIO.read(testFile);
             Tag tag = f.getTag();
 
             tag.getFirstAlbum();
             tag.getFirstArtist();
             tag.getFirstComment();
             tag.getFirstGenre();
             tag.getFirstTitle();
             tag.getFirstTrack();
             tag.getFirstYear();
             System.out.println(tag);
         }
         catch (Exception e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     public void testGenre()
     {
         Exception exceptionCaught = null;
         try
         {
             assertNull(GenreTypes.getInstanceOf().getIdForValue("fred"));
             Mp4GenreField.isValidGenre("fred");
         }
         catch (Exception e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     /**
      * Test reading filers tagged with monkeymedia, which has custom image tags and additiona mm tags
      *
      * @throws Exception
      */
     public void testIssue168() throws Exception
     {
         Exception exceptionCaught = null;
         try
         {
             File orig = new File("testdata", "test14.m4a");
             if (!orig.isFile())
             {
                 return;
             }
             File testFile = AbstractTestCase.copyAudioToTmp("test14.m4a");
            
             AudioFile f = AudioFileIO.read(testFile);
             Mp4Tag tag = (Mp4Tag)f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             //AudioInfo
             //Time in seconds
             assertEquals(241, f.getAudioHeader().getTrackLength());
             assertEquals(44100, f.getAudioHeader().getSampleRateAsNumber());
 
             //MPEG Specific
             Mp4AudioHeader audioheader = (Mp4AudioHeader) f.getAudioHeader();
             assertEquals(Mp4EsdsBox.Kind.MPEG4_AUDIO, audioheader.getKind());
             assertEquals(Mp4EsdsBox.AudioProfile.LOW_COMPLEXITY, audioheader.getProfile());
             assertEquals(1,tag.get(Mp4NonStandardFieldKey.AAPR.getFieldName()).size());
             assertNotNull(tag.getFirst(Mp4NonStandardFieldKey.AAPR.getFieldName()));
             assertEquals("AApr",tag.getFirstField(Mp4NonStandardFieldKey.AAPR.getFieldName()).getId());
             //Make a change and save
             tag.setTitle("NEWTITLE\u00A9\u01ff");      //test UTF8 encoding
             tag.set(tag.createTagField(Mp4FieldKey.CONTENT_TYPE,Mp4ContentTypeValue.TV_SHOW.getIdAsString()));
             f.commit();
 
             f = AudioFileIO.read(testFile);
             tag = (Mp4Tag)f.getTag();
 
             assertEquals("AApr",tag.getFirstField(Mp4NonStandardFieldKey.AAPR.getFieldName()).getId());
             assertEquals("NEWTITLE\u00A9\u01ff",tag.getFirstTitle());
             assertEquals(Mp4ContentTypeValue.TV_SHOW.getIdAsString(),tag.getFirst(Mp4FieldKey.CONTENT_TYPE));                       
             assertEquals(1,tag.get(Mp4NonStandardFieldKey.AAPR.getFieldName()).size());
             assertNotNull(tag.getFirst(Mp4NonStandardFieldKey.AAPR.getFieldName()));
 
             //Can we read all the other customfields  (that do follow convention)
             System.out.println(tag.toString());
             assertEquals("lyricist",tag.getFirst(Mp4FieldKey.LYRICIST));
             assertEquals("70",tag.getFirst(Mp4FieldKey.SCORE));
             assertEquals("conductor",tag.getFirst(Mp4FieldKey.CONDUCTOR));
             assertEquals("original artist",tag.getFirst(Mp4FieldKey.ORIGINAL_ARTIST));
             assertEquals("original album title",tag.getFirst(Mp4FieldKey.ORIGINAL_ALBUM_TITLE));
             assertEquals("involved people",tag.getFirst(Mp4FieldKey.INVOLVED_PEOPLE));
             assertEquals("Slow",tag.getFirst(Mp4FieldKey.TEMPO));
             assertEquals("Mellow",tag.getFirst(Mp4FieldKey.MOOD));
             assertEquals("Dinner",tag.getFirst(Mp4FieldKey.OCCASION));
             assertEquals("Very good copy",tag.getFirst(Mp4FieldKey.QUALITY));
             assertEquals("custom1",tag.getFirst(Mp4FieldKey.CUSTOM_1));
             assertEquals("custom2",tag.getFirst(Mp4FieldKey.CUSTOM_2));
             assertEquals("custom3",tag.getFirst(Mp4FieldKey.CUSTOM_3));
             assertEquals("custom4",tag.getFirst(Mp4FieldKey.CUSTOM_4));
             assertEquals("custom5",tag.getFirst(Mp4FieldKey.CUSTOM_5));
         }
         catch (IOException e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
 
     }
 
     /** Tests reading of winamp encoded files, that contain additional scene tracks */
      public void testIssue182() throws Exception
     {
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test16.m4a");
 
             AudioFile f = AudioFileIO.read(testFile);
             Mp4Tag tag = (Mp4Tag)f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
             assertEquals("Suerte",tag.getFirstArtist());
             assertEquals("Kogani",tag.getFirstTitle());
 
         }
         catch (IOException e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 
     /** Test reading mp4 file
      *
      * @throws Exception
      */
     public void testIssue198()throws Exception
     {
         File orig = new File("testdata", "test27.m4a");
         if (!orig.isFile())
         {
             return;
         }
 
         Exception exceptionCaught = null;
         try
         {
             File testFile = AbstractTestCase.copyAudioToTmp("test27.m4a");
 
             AudioFile f = AudioFileIO.read(testFile);
             Mp4Tag tag = (Mp4Tag)f.getTag();
 
             System.out.println(f.getAudioHeader());
             System.out.println(tag);
 
         }
         catch (IOException e)
         {
             e.printStackTrace();
             exceptionCaught = e;
         }
         assertNull(exceptionCaught);
     }
 }
