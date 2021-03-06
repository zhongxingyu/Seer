 package org.jaudiotagger.tag.id3;
 
 import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.tag.id3.framebody.*;
 import org.jaudiotagger.tag.TagOptionSingleton;
 import org.jaudiotagger.audio.mp3.MP3File;
 
 import java.io.File;
 
 /**
  * Test Itunes problems
  */
 public class ItunesTest extends AbstractTestCase
 {
     private static final int FRAME_SIZE = 2049;
 
     private static final int STRING_LENGTH_WITH_NULL = 12;
     private static final int STRING_LENGTH_WITHOUT_NULL = 11;
     private static final int TERMINATOR_LENGTH = 1;
     private static final String SECOND_VALUE = "test";
    private static final String EMPTY_VALUE = "";

     /** This tests that we work out that the frame is not unsynced and read the frame size as a normal integer
      *  using an integral algorithm
      * @throws Exception
      */
     public void testv24TagWithNonSyncSafeFrame() throws Exception
     {
         File testFile = AbstractTestCase.copyAudioToTmp("Issue96-1.id3","testV1.mp3");
 
         MP3File mp3File = new MP3File(testFile);
 
         ID3v24Frame v24frame = (ID3v24Frame) mp3File.getID3v2Tag().getFrame(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE);
         assertNotNull(v24frame);
         FrameBodyAPIC fb = (FrameBodyAPIC) v24frame.getBody();
         assertEquals(FRAME_SIZE,fb.getSize());
     }
 
     /** This tests that we work out that the frame is unsynced and read the frame size correctly  and convert to intger
      * this is what most (non-itunes applications do)
      * @throws Exception
      */
     public void testv24TagWithSyncSafeFrame() throws Exception
     {
         File testFile = AbstractTestCase.copyAudioToTmp("Issue96-2.id3","testV1.mp3");
 
         MP3File mp3File = new MP3File(testFile);
 
         ID3v24Frame v24frame = (ID3v24Frame) mp3File.getID3v2Tag().getFrame(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE);
         assertNotNull(v24frame);
         FrameBodyAPIC fb = (FrameBodyAPIC) v24frame.getBody();
         assertEquals(FRAME_SIZE,fb.getSize());
     }
 
     /**
      * test can read string with spurious null at end, and can retrieve string without this if we want
      * to. Can then write to file without null if options set correctly, can add multiple values
      * @throws Exception
      */
     public void testCanIgnoreSpuriousNullCharacters() throws Exception
     {
         File testFile = AbstractTestCase.copyAudioToTmp("Issue92.id3","testV1.mp3");
 
         MP3File mp3File = new MP3File(testFile);
 
         ID3v24Frame v24frame = (ID3v24Frame) mp3File.getID3v2Tag().getFrame(ID3v24Frames.FRAME_ID_ARTIST);
         assertNotNull(v24frame);
         FrameBodyTPE1 fb = (FrameBodyTPE1) v24frame.getBody();
         assertEquals(STRING_LENGTH_WITH_NULL,fb.getText().length());
         assertEquals(STRING_LENGTH_WITHOUT_NULL,fb.getFirstTextValue().length());
 
         //Null remains
         TagOptionSingleton.getInstance().setRemoveTrailingTerminatorOnWrite(false);
         mp3File.save();
         mp3File = new MP3File(testFile);
         v24frame = (ID3v24Frame) mp3File.getID3v2Tag().getFrame(ID3v24Frames.FRAME_ID_ARTIST);
         fb = (FrameBodyTPE1) v24frame.getBody();
         assertEquals(STRING_LENGTH_WITH_NULL,fb.getText().length());
         assertEquals(STRING_LENGTH_WITHOUT_NULL,fb.getFirstTextValue().length());
 
         //Null removed
         TagOptionSingleton.getInstance().setRemoveTrailingTerminatorOnWrite(true);
         mp3File.save();
         mp3File = new MP3File(testFile);
         v24frame = (ID3v24Frame) mp3File.getID3v2Tag().getFrame(ID3v24Frames.FRAME_ID_ARTIST);
         fb = (FrameBodyTPE1) v24frame.getBody();
         assertEquals(STRING_LENGTH_WITHOUT_NULL,fb.getText().length());
         assertEquals(STRING_LENGTH_WITHOUT_NULL,fb.getFirstTextValue().length());
         assertEquals(1,fb.getNumberOfValues());
 
         //Adding additional values
         fb.addTextValue(SECOND_VALUE);
         assertEquals(2,fb.getNumberOfValues());
         assertEquals(SECOND_VALUE,fb.getValueAtIndex(1));
         mp3File.save();
 
         mp3File = new MP3File(testFile);
         v24frame = (ID3v24Frame) mp3File.getID3v2Tag().getFrame(ID3v24Frames.FRAME_ID_ARTIST);
         fb = (FrameBodyTPE1) v24frame.getBody();
         assertEquals(2,fb.getNumberOfValues());
         assertEquals(STRING_LENGTH_WITHOUT_NULL + TERMINATOR_LENGTH + SECOND_VALUE.length(),fb.getText().length());
         assertEquals(STRING_LENGTH_WITHOUT_NULL,fb.getFirstTextValue().length());
         assertEquals(SECOND_VALUE,fb.getValueAtIndex(1));
     }

    /** Check can handle empty value when splitting strings into a list */
    public void testCanReadEmptyString() throws Exception
    {
        File testFile = AbstractTestCase.copyAudioToTmp("Issue92-2.id3","testV1.mp3");
        MP3File mp3File = new MP3File(testFile);


        ID3v23Frame v23frame = (ID3v23Frame) mp3File.getID3v2Tag().getFrame(ID3v23Frames.FRAME_ID_V3_COMMENT);
        assertNotNull(v23frame);
        FrameBodyCOMM fb = (FrameBodyCOMM) v23frame.getBody();
        assertEquals(EMPTY_VALUE,fb.getText());       
    }
 }
