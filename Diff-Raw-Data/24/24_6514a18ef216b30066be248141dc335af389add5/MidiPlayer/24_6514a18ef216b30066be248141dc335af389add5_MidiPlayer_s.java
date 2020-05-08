 package jku.se.tetris.sound;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Sequencer;
 
 public class MidiPlayer {
 	private static Sequencer backgroundSequencer;
 
 	static {
 		try {
 			InputStream audioStream = MidiPlayer.class.getResourceAsStream("Tetris_Background.mid");
 			// --
 			backgroundSequencer = MidiSystem.getSequencer();
 			backgroundSequencer.open();
 			backgroundSequencer.setSequence(MidiSystem.getSequence(audioStream));
 			backgroundSequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
 
 		} catch (InvalidMidiDataException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (MidiUnavailableException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void startBackgroundMusic() {
		if (backgroundSequencer != null) {
 			backgroundSequencer.start();
 		}
 	}
 
 	public static void stopBackgroundMusic() {
		if (backgroundSequencer != null) {
 			backgroundSequencer.stop();
 		}
 	}
 
 	public static void teardown() {
 		if (backgroundSequencer != null) {
 			backgroundSequencer.close();
 		}
 	}
 }
