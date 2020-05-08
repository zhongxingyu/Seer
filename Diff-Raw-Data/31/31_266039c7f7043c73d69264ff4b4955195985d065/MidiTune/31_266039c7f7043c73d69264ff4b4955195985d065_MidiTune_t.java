 package pipes.sound;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiEvent;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.ShortMessage;
 import javax.sound.midi.Track;
 
 import pipes.model.Pitch;
 
 public class MidiTune {
 	private static final int MIDI_OCTAVE = 12;
 	private static final int MELODY_TRACK = 0;
 	private static final int TENOR_TRACK = 1;
 	private static final int BASS_TRACK = 2;
 	
 	public Sequence sequence() {
 		return sequence;
 	}
 
 	public void appendNote(Pitch p, int ticks) {
 		try {
 			MidiEvent onEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, MELODY_TRACK, getMidiCode(p), 100), endTime);
 			mainTrack.add(onEvent);
 
 			endTime += ticks;
 	
 			MidiEvent offEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, MELODY_TRACK, getMidiCode(p), 100), endTime);
 			mainTrack.add(offEvent);
 		} catch (InvalidMidiDataException imde) {
 
 		}
 	}

	/**
	 * Adds a duration of musical "rest", or silence, during which no melody note is played.
	 */
	public void appendRest(int ticks) {
		endTime += ticks;
	}
 	
 	public void engageDrones() {
 		try {
 			MidiEvent tenorOnEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, TENOR_TRACK, getMidiCode(Pitch.LOW_A)-MIDI_OCTAVE, 60), 0);
 			MidiEvent bassOnEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, BASS_TRACK, getMidiCode(Pitch.LOW_A)-MIDI_OCTAVE*2, 60), 0);
 			mainTrack.add(bassOnEvent);
 			mainTrack.add(tenorOnEvent);
 		} catch (InvalidMidiDataException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public MidiTune(int ticksPerQuarter) {
 		try {
 			sequence = new Sequence(Sequence.PPQ, ticksPerQuarter);
 			mainTrack = sequence.createTrack();
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, MELODY_TRACK, 111, 0), 0));
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, TENOR_TRACK, 111, 0), 0));
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, BASS_TRACK, 111, 0), 0));
 			
 			// Controller 73 = Attack Time
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, MELODY_TRACK, 73, 0), 0));
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, TENOR_TRACK, 73, 0), 0));
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, BASS_TRACK, 73, 0), 0));
 			
 			// Controller 75 = Decay Time
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, MELODY_TRACK, 75, 0), 0));
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, TENOR_TRACK, 75, 0), 0));
 			mainTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, BASS_TRACK, 75, 0), 0));
 		} catch (InvalidMidiDataException imde) {
 
 		}
 	}
 
 	private int getMidiCode(Pitch p) {
 		switch (p) {
 			case A: return 69;
 			case G: return 67;
 			case F: return 66;
 			case E: return 64;
 			case D: return 62;
 			case C: return 61;
 			case B: return 59;
 			case LOW_A: return 57;
 			case LOW_G: return 55;
 			default: return 0;
 		}
 	}
 
 	private Track mainTrack;
 	private Sequence sequence;
 
 	// Time in Ticks
 	private long endTime;
 }
