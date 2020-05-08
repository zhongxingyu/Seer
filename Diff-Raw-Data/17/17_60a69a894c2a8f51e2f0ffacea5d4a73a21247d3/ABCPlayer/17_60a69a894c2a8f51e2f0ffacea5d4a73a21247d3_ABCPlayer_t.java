 package player;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 
 import lexer.Header;
 
 import abcmusic.ABCmusic;
 import abcmusic.Chord;
 import abcmusic.MajorSection;
 import abcmusic.Measure;
 import abcmusic.Music;
 import abcmusic.Note;
 import abcmusic.Rest;
 import abcmusic.Section;
 import abcmusic.Tuplet;
 import abcmusic.Voice;
 import sound.Pitch;
 import sound.SequencePlayer;
 
 public class ABCPlayer implements ABCmusic.Visitor<SequencePlayer>{
     // Our main player for the music
 	private SequencePlayer player;
 	private int ticksPerQuarterNote;
 	private Header header;
 	private int lastTick = 0;
 	
 	/**
 	 * Construct a new ABCPlayer with the correct number of ticks per quarter note,
 	 * beats per minute, and a valid parsed header
 	 * 
 	 * @param ticks Number of ticks per quarter note. Must be a non-negative integer
 	 * @param bpm Number of beats (quarter notes) per minute. Must be non-negative integer
 	 * @param head A parsed representation of the header
 	 */
 	public ABCPlayer(int ticks, int bpm, Header head){
 		try {
			ticksPerQuarterNote = ticks*128; // This is to get around the NOTE_ON / NOTE_OFF in different voices problem, as per Josh's suggestion.
 			header = head;
			player = new SequencePlayer(ticksPerQuarterNote, bpm);
 		} catch (MidiUnavailableException e) {
 	        e.printStackTrace();
 	    } catch (InvalidMidiDataException e) {
 	        e.printStackTrace();
 	    }
 	}
 	
 	public SequencePlayer on(Music music){
 		// Process every voice in the music, one at a time 
         for (int i=0;i<music.size;i++) {
             abcPlayer(music.getVoice(i));
         }
         return player;
     }
     public SequencePlayer on(Voice voice) {
     	// Process all the major sections in the voice one by one.
         for (int i=0;i<voice.size;i++) {
             abcPlayer(voice.getMajorSection(i));
         }
         // Reset tick after every voice
         lastTick = 0;
         return player;
     }
     public SequencePlayer on(MajorSection majorsection) {
         for (int i=0;i<majorsection.size;i++) {
             abcPlayer(majorsection.getSection(i));
         }
         return player;
     }
     public SequencePlayer on(Section section) {
         for (int i=0;i<section.size;i++) {
             abcPlayer(section.getMeasure(i));
         }
         return player;
     }
     public SequencePlayer on(Measure measure) {
         for (int i=0;i<measure.size;i++) {
             abcPlayer(measure.getElements(i));
         }
         return player;
     }
     public SequencePlayer on(Chord c) {
     	int noteLengthInTicks = getNoteLengthInTicks(c);
     	    	
         for (int i=0;i<c.size;i++) {
         	Note note = c.getNote(i);
         	Pitch pitch = createPitchFromNote(note);
        	player.addNote(pitch.toMidiNote(), lastTick, noteLengthInTicks-1);
         }
         lastTick = lastTick+noteLengthInTicks;
         return player;
     }
     
     public SequencePlayer on(Tuplet tuplet) {
     	int noteLengthInTicks = getNoteLengthInTicks(tuplet);
     	
         for (int i=0;i<tuplet.size;i++){ 
         	ABCmusic elem = tuplet.getElement(i);
         	if(elem instanceof Note)
         		processNoteWithinTuplet((Note)elem, noteLengthInTicks);
         	else if(elem instanceof Chord)
         		processChordWithinTuplet((Chord)elem, noteLengthInTicks);
         }
         return player;
     }
     
     public SequencePlayer on(Note note) {
     	Pitch pitch = createPitchFromNote(note);
     
     	int noteLengthInTicks = getNoteLengthInTicks(note);
    	player.addNote(pitch.toMidiNote(), lastTick, noteLengthInTicks-1);
     	lastTick = lastTick+noteLengthInTicks;
     		
         return player;
     }
     
     public SequencePlayer on(Rest rest) {
     	// Increment the tick counter (without adding any notes) by the length of the rest.
     	lastTick = lastTick + getNoteLengthInTicks(rest);
         
     	return player;
     }
     
     public SequencePlayer abcPlayer(ABCmusic music){
         return music.accept(this);
     }
     
     /**
      * Instantiate a pitch from a passed-in note
      * 
      * @param note The note which we are converting to a pitch
      * @return The resulting pitch (transposed to the correct octave, and with the correct accidental)
      */
     
     private Pitch createPitchFromNote(Note note){
     	
     	Pitch pitch = new Pitch(note.value).transpose(note.octave*Pitch.OCTAVE);
     	if (note.getHasAccidental())
     		pitch = pitch.transpose(note.getAccidental());
     	else
     		pitch = pitch.transpose(header.getAccidental(note.value));
     	return pitch;
     }
     
     /**
      * Helper method that calculates the note length for a given ABCmusic element - either
      * a note, a tuplet, a rest or a chord
      * 
      * @param elem Either a note, a tuplet, a rest or a chord to calculate note length
      * @return an integer representing the length of a single note within elem
      */
     private int getNoteLengthInTicks(ABCmusic elem){
     	// First we calculate the noteLength, which is simply the unscaled length of a note times the default note length
     	Rational noteLength;
     	
     	if(elem instanceof Note)
     		noteLength = ((Note)elem).getLength().times(header.getDefaultNoteLength());
     	else if(elem instanceof Tuplet)
     		noteLength = ((Tuplet)elem).getNoteLength().times(header.getDefaultNoteLength());
     	else if(elem instanceof Chord)
     		noteLength = ((Chord)elem).getLength().times(header.getDefaultNoteLength());
     	else if(elem instanceof Rest)
     		noteLength = ((Rest)elem).getLength().times(header.getDefaultNoteLength());
     	else
     		throw new RuntimeException("Parameter to getNoteLengthInTicks must be one of [Note, Tuplet, Chord, Rest]");
     	
     	// Now we convert our noteLength into ticks by scaling it by 4*ticksPerQuarterNote (which is ticks / full note)
     	return (4*ticksPerQuarterNote)*noteLength.num/noteLength.den;
     }
     /**
      * Helper method used to process a note inside a tuplet
      * 
      * @param note The note to be processed (from the tuplet)
      * @param noteLengthInTicks Length of the note within the tuplet
      */
     private void processNoteWithinTuplet(Note note, int noteLengthInTicks){
     	
     	Pitch pitch = createPitchFromNote(note);
    	player.addNote(pitch.toMidiNote(), lastTick, noteLengthInTicks-1);
     	lastTick = lastTick+noteLengthInTicks;
     }
     
     /**
      * Helper method used to process a chord inside a tuplet
      * 
      * @param chord The chord to be processed (from the tuplet)
      * @param noteLengthInTicks Length of a single note within the chord to be processed
      */
     private void processChordWithinTuplet(Chord chord, int noteLengthInTicks){
     	
     	for (int i=0;i<chord.size;i++) {
         	Note note = chord.getNote(i);
         	Pitch pitch = createPitchFromNote(note);
        	player.addNote(pitch.toMidiNote(), lastTick, noteLengthInTicks-1);
         }
         lastTick = lastTick+noteLengthInTicks;
     }
 
 }
