 package org.jcoderz.mp3.intern.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.util.Arrays;
 
 import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.jaudiotagger.audio.mp3.MPEGFrameHeader;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.id3.ID3v23Tag;
 import org.jcoderz.commons.util.FileUtils;
 import org.jcoderz.commons.util.IoUtil;
 import org.jcoderz.mp3.intern.TestUtil;
 import org.junit.Test;
 
 public class Mp3UtilTest
 {
     private static final int HEADER_START = 33;
 
     @Test
     public void testCalcAudioFramesSha1 () throws Exception
     {
         final File targetDir 
             = new File(TestUtil.getTargetBasePath(), "sha1-rename-test").getCanonicalFile();
         
         targetDir.mkdirs();
         
         FileUtils.copy(new File(TestUtil.getMp3BasePath(), "unsorted/PeterLicht/Lieder vom Ende des Kapitalismus/08 - Hallo hallo (Dies ist der Tag).mp3"), targetDir);
 
         File testFile = new File(targetDir, "08 - Hallo hallo (Dies ist der Tag).mp3");
         final String ref = Mp3Util.calcAudioFramesSha1(testFile);
         assertEquals(ref, Mp3Util.calcAudioFramesSha1(testFile));
         final MP3File mp3File = new MP3File(testFile);
         mp3File.setID3v2Tag(new ID3v23Tag());
         mp3File.getID3v2Tag().setField(FieldKey.ALBUM, "CHANGED");
         mp3File.save();
         assertEquals("Different hash after tag change.", ref, Mp3Util.calcAudioFramesSha1(testFile));
         
         File testRef = new File(targetDir, "error.mp3");
         
         copyError(testFile, testRef, 112); // in id3 tag
         assertEquals("Different hash after tag change.", ref, Mp3Util.calcAudioFramesSha1(testRef));
         
         copyError(testFile, testRef, 212); // in song data
         assertFalse("Same hash after song change.", ref.equals(Mp3Util.calcAudioFramesSha1(testRef)));
     }
 
     @Test
     public void testCalcAudioFramesSha1Protected () throws Exception
     {
         final File targetDir 
             = new File(TestUtil.getTargetBasePath(), "sha1-rename-test").getCanonicalFile();
         
         targetDir.mkdirs();
         
         FileUtils.copy(new File(TestUtil.getMp3BasePath(), "unsorted/PeterLicht/Lieder vom Ende des Kapitalismus/bar.mp3"), targetDir);
 
         File testFile = new File(targetDir, "bar.mp3");
         final String ref = Mp3Util.calcAudioFramesSha1(testFile);
         assertEquals(ref, Mp3Util.calcAudioFramesSha1(testFile));
         final MP3File mp3File = new MP3File(testFile);
         mp3File.setID3v2Tag(new ID3v23Tag());
         mp3File.getID3v2Tag().setField(FieldKey.ALBUM, "CHANGED");
         mp3File.save();
         assertEquals("Different hash after tag change.", ref, Mp3Util.calcAudioFramesSha1(testFile));
         
         File testRef = new File(targetDir, "error.mp3");
         
         copyError(testFile, testRef, 112); // in id3 tag
         assertEquals("Different hash after tag change.", ref, Mp3Util.calcAudioFramesSha1(testRef));
         
         copyError(testFile, testRef, 212); // in song data
         assertFalse("Same hash after song change.", ref.equals(Mp3Util.calcAudioFramesSha1(testRef)));
     }
     
     
     private void copyError(File in, File out, int errorPos) throws IOException
     {
         FileInputStream is = null;
         FileOutputStream os = null;
         try
         {
             is = new FileInputStream(in);
             os = new FileOutputStream(out);
             int pos = 0;
             int read = is.read();
             while (read >= 0)
             {
                 if (pos == errorPos)
                 {
                     os.write(read ^ 0xFF);
                 }
                 else
                 {
                     os.write(read);
                 }
                 pos++;
                 read = is.read();
             }
         }
         finally
         {
             IoUtil.close(is);
             IoUtil.close(os);
         }
     }
     
     @Test
     public void testSimpleStream() throws InvalidAudioFrameException
     {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         
         for (int i = 0; i < HEADER_START; i++)
         {
             os.write(5);
         }
         
         os.write(0xFF); // see http://www.id3.org/mp3Frame
         os.write(0xFA); // MPEG-1 Layer 3 CRC Protected 
         os.write(0x18); // 32 kbit/s / 32KHz / No Padding / No Privacy
         os.write(0xC0); // Mono / no emphasis / 'normal'
         
         int crcPos = os.size();
         os.write(0x7F); // CRC
         os.write(0x7F); // CRC
         
         // 144 Bytes
         int audioStart = os.size();
         for (int i = 6; i < 144; i++)
         {
             os.write(6);
         }
 
         for (int f = 0; f < 3; f++)
         {
             os.write(0xFF); // see http://www.id3.org/mp3Frame
             os.write(0xFA); // MPEG-1 Layer 3 CRC Protected 
             os.write(0x18); // 32 kbit/s / 32KHz / No Padding / No Privacy
             os.write(0xC0); // Mono / no emphasis / 'normal'
             
             os.write(0x7F); // CRC
             os.write(0x7F); // CRC
             
             // 144 Bytes
             for (int i = 6; i < 144; i++)
             {
                 os.write(6);
             }
         }
         int audioEnd = os.size();
         os.write(99); // dummy
         
         final byte[] testData = os.toByteArray();
         
         String sha 
             = calcSha1(testData); 
         
         
         MPEGFrameHeader header 
             = MPEGFrameHeader.parseMPEGHeader(
                 ByteBuffer.wrap(Arrays.copyOfRange(testData, HEADER_START, HEADER_START + MPEGFrameHeader.HEADER_SIZE)));
         
         assertEquals("Header should indicate a protected frame.", true, header.isProtected());
         assertEquals("Header should indicate a other frame length.", 144, header.getFrameLength());
         
         testData[crcPos] = 99;
         testData[crcPos + 1] = 99;
         assertEquals(
             "CRC should nor be part od the sha1", sha, 
             calcSha1(testData));
         
         testData[audioStart]++;
         assertFalse("Start audio not part of sha1?", sha.equals(calcSha1(testData)));
         testData[audioStart]--;
         assertEquals("Testcase issue!", sha, calcSha1(testData));
         testData[audioEnd - 1]++;
         assertFalse("End audio not part of sha1?", sha.equals(calcSha1(testData)));
         testData[audioEnd - 1]--;
         assertEquals("Testcase issue!", sha, calcSha1(testData));
         testData[audioEnd]--;
         assertEquals("Change after audio breaks sha1!", sha, calcSha1(testData));
         assertEquals("Reference sha1 changed!", "3846730730C4AE78F5B0F08700E1E77147651916", sha);
         
     }
 
     private String calcSha1 (final byte[] testData)
     {
         return Mp3Util.calcAudioFramesSha1(Channels.newChannel(new ByteArrayInputStream(testData)));
     }
     
 }    
