 package no.rmz.chordguesser;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import org.mockito.runners.MockitoJUnit44Runner;
 
 @RunWith(MockitoJUnit44Runner.class)
 public final class MidiMessageDecoderTest {
 
    
     private @Mock NoteListener noteListener;
 
     /**
      * Test of getHighNibble method, of class MidiMessageDecoder.
      */
     @Test
     public void testGetHighNibble() {
         final byte b = (byte) 0xAB;
         final byte expResult = 0xA;
         final byte result = MidiMessageDecoder.getHighNibble(b);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getLowNibble method, of class MidiMessageDecoder.
      */
     @Test
     public void testGetLowNibble() {
         final byte b = (byte) 0xAB;
         final byte expResult = 0xB;
         final byte result = MidiMessageDecoder.getLowNibble(b);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getCmdFromByte method, of class MidiMessageDecoder.
      */
     @Test
     public void testGetNoteOnCmdFromByte() {
         byte b = (byte) 0x9E;
         final MidiCmd expResult = MidiCmd.NOTE_ON;
         final MidiCmd result = MidiMessageDecoder.getCmdFromByte(b);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getCmdFromByte method, of class MidiMessageDecoder.
      */
     @Test
     public void testGetNoteOffCmdFromByte() {
         byte b = (byte) 0x8E;
         final MidiCmd expResult = MidiCmd.NOTE_OFF;
         final MidiCmd result = MidiMessageDecoder.getCmdFromByte(b);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getMidiChannelFromByte method, of class MidiMessageDecoder.
      */
     @Test
     public void testGetMidiChannelFromByte() {
         final byte b = (byte) 0x8E;
         final int expResult = (byte) 0xE;
         final int result = MidiMessageDecoder.getMidiChannelFromByte(b);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getCmdFromByte method, of class MidiMessageDecoder.
      */
     @Test
     public void testGetCmdFromByte() {
         final byte b = (byte) 0x90;
         final MidiCmd expResult = MidiCmd.NOTE_ON;
         final MidiCmd result = MidiMessageDecoder.getCmdFromByte(b);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of decode method, of class MidiMessageDecoder.
      */
     @Test
     public void testDecodeMiddleCToneOn() {
         // XXXX Don't use 60, say what it means.
         final byte[] m = new byte[]{(byte) 0x90, (byte) 60, (byte) 60};
         final long timestamp = 4711L;
         final MidiMessageDecoder instance = new MidiMessageDecoder(noteListener);
         instance.decode(m, timestamp);
         verify(noteListener).noteOn(60);
     }
 
     /**
      * Test of decode method, of class MidiMessageDecoder.
      */
     @Test
     public void testDecodeAllTonesCToneOn() {
         for (int tone = 0; tone < 128; tone++) {
             final byte[] m = new byte[]{ScaleAndChordGeneration.MIDI_TONE_DOWN_CNAN_ZERO, (byte) tone, (byte) 60};
             final long timestamp = 4711L;
             noteListener = mock(NoteListener.class);
             final MidiMessageDecoder instance = new MidiMessageDecoder(noteListener);
             instance.decode(m, timestamp);
             verify(noteListener).noteOn(tone);
         }
     }
 
    
 
     /**
      * This is an exploratory test, when the exploration is done it probably
      * doesn't even belong in this class.
      */
     @Test
     public void testCMajorChordDetection() {
         byte[][] cMajorChordArpeggiated =
                 // XXX Replae 60 withsome constant.
                midiToneDownSequenceForTones(new byte[]{
                     ScaleAndChordGeneration.MIDDLE_C_MIDI_ENCODING,
                     ScaleAndChordGeneration.MIDDLE_C_MIDI_ENCODING + 4,
                     ScaleAndChordGeneration.MIDDLE_C_MIDI_ENCODING + 7});
 
         final MidiMessageDecoder instance = new MidiMessageDecoder(noteListener);
         ScaleAndChordGeneration.inject(instance, cMajorChordArpeggiated);
         verify(noteListener).noteOn(ScaleAndChordGeneration.MIDDLE_C_MIDI_ENCODING);
         verify(noteListener).noteOn(ScaleAndChordGeneration.MIDDLE_C_MIDI_ENCODING + 4);
         verify(noteListener).noteOn(ScaleAndChordGeneration.MIDDLE_C_MIDI_ENCODING + 7);
     }
 }
