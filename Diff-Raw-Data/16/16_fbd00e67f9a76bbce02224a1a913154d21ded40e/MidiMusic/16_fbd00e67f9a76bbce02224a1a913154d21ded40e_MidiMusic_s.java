 package music;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.Sequencer;
 
 /**
  * Musical objects which can be played.
  * 
  * @author Unto Created 10.7.2013
  * 
  */
 public class MidiMusic {
 
 	// ATTRIBUTES ---------------------------------------------------------
 
 	private String fileName;
 	private Sequence midiSequence;
 	private Sequencer midiSequencer;
 
 	// CONSTRUCTOR ---------------------------------------------------------
 
 	/**
 	 * Creates MidiMusic-object.
 	 * 
 	 * @param fileName	Where the midi's name and where it is located.
 	 */
 	public MidiMusic(String fileName) {
 		this.fileName = fileName;
 		// Let's try to create our midiSequence
 		try {
 			this.midiSequence = MidiSystem.getSequence(new File(this.fileName));
 		} catch (InvalidMidiDataException e) {
 			System.err.println("Couldn't find create a midisequence!");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("IOException whilst creating midisequence!");
 			e.printStackTrace();
 		}
 		// Now let's try and set-up our midiSequencer
 		try {
 			this.midiSequencer = MidiSystem.getSequencer();
			this.midiSequencer.setSequence(this.midiSequence);
 		} catch (MidiUnavailableException e) {
 			System.err.println("Problems whilst setting up sequencer!");
 			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			System.err.println("Midi was invalid!");
			e.printStackTrace();
		}
 
 	}
 
 	// METHODS ---------------------------------------------------
 
 	/**
 	 * @return Returns the length of a Midi-sequence in ticks.
 	 */
 	public long getSequenceLength() {
 		return this.midiSequence.getTickLength();
 	}
 
 	/**
 	 * Starts playing the music-file from the given position.
 	 * 
 	 * @param startPosition	 Playback's starting tick-position.
 	 * @throws MidiUnavailableException
 	 */
 	public void startMusic(long startPosition) throws MidiUnavailableException {
 		this.midiSequencer.open();
 		this.midiSequencer.setTickPosition(startPosition);
 		this.midiSequencer.start();
 	}
 
 	/**
 	 * Stops playing the music.
 	 */
 	public void stopMusic() {
 		this.midiSequencer.stop();
 		this.midiSequencer.close();
 	}
 
 	/**
 	 * Pauses the music and returns where the song was paused.
 	 * 
 	 * @return Returns the tick-position where the song was paused.
 	 */
 	public long pauseMusic() {
 		this.midiSequencer.stop();
 		long pausePosition = this.midiSequencer.getTickPosition();
 		return pausePosition;
 	}
 
 	/**
 	 * Sets how many times the song loops.
 	 * 
 	 * @param loopCount	How many times the song loops. If loopCount is negative, song
 	 *            will loop continuously.
 	 */
 	public void setLoopCount(int loopCount) {
 		if (loopCount < 0) {
 			this.midiSequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
 		} else {
 			this.midiSequencer.setLoopCount(loopCount);
 		}
 	}
 
 	/**
 	 * Changes where the music's loop starts.
 	 * 
 	 * @param loopStartPoint	The tick where music's loop starts.
 	 */
 	public void setLoopStart(long loopStartPoint) {
 		this.midiSequencer.setLoopStartPoint(loopStartPoint);
 	}
 
 	/**
 	 * Changes where the music's loop ends.
 	 * 
 	 * @param loopEndPoint	The tick where music's loop ends.
 	 */
 	public void setLoopEnd(long loopEndPoint) {
 		this.midiSequencer.setLoopEndPoint(loopEndPoint);
 	}
 
 }
