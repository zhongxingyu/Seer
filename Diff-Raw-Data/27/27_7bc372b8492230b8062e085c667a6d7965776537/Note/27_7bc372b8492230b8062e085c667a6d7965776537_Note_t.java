 package sound;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Note implements Playable 
 {
     private final NoteType noteBase; 
     private final Accidental accidental; 
     private final int octavesAboveMiddleC; 
     private RatNum noteLength; 
     
     public Note(NoteType noteBase, int octaves, RatNum noteLength)
     {
         this.noteBase = noteBase; 
         this.octavesAboveMiddleC = octaves; 
         this.noteLength = noteLength; 
         this.accidental = Accidental.ABSENT; 
     }
     
     public Note(NoteType noteBase, int octaves, RatNum noteLength, Accidental accidental)
     {
         this.noteBase = noteBase; 
         this.accidental = accidental;
         this.octavesAboveMiddleC = octaves; 
         this.noteLength = noteLength; 
     }
     
     public NoteType getBaseNoteType()
     {
         return noteBase;
     }
     
     public Accidental getAccidental()
     {
         return accidental;
     }
     
     public int getOctavesAboveMiddleC()
     {
         return octavesAboveMiddleC;
     }
     
     public void setNoteLength(RatNum noteLength)
     {
         this.noteLength = noteLength; 
     }
     
     @Override
     public String toString()
     {
         return "Note(" + noteBase.toString() + " " + octavesAboveMiddleC + " " + noteLength.toString() + " " + accidental.toString() + ") ";
     }
     
     public List<SequencePlayerNote> play(int startTicks, int numTicks, RatNum defaultNoteLength) {
     	List<SequencePlayerNote> sequencePlayerNotes = new ArrayList<SequencePlayerNote> ();
     	int numer = this.noteLength.getNumer();
     	int denom = this.noteLength.getDenom();
     	int defaultDenom = defaultNoteLength.getDenom(); 
     	int defaultNum = defaultNoteLength.getNumer(); 
     	int ticks = (numer * 4 * numTicks * defaultNum)/(denom * defaultDenom);
     	
     	Pitch notePitch;
     	switch (noteBase) {
     	case C:
     		notePitch = new Pitch('C');
     		break;
     	case D:
     		notePitch = new Pitch('D');
     		break;
     	case E:
     		notePitch = new Pitch('E');
     		break;
     	case F:
     		notePitch = new Pitch('F');
     		break;
     	case G:
     		notePitch = new Pitch('G');
     		break;
     	case A:
     		notePitch = new Pitch('A');
     		break;
     	case B:
     		notePitch = new Pitch('B');
     		break;
     	default:
     		throw new RuntimeException("Illegal note");
     		
     	}
     	int numTranspose = Pitch.OCTAVE * octavesAboveMiddleC;
     	
     	switch (accidental) {
     	case SHARP:
     		numTranspose += 1;
     		break;
     	case DOUBLESHARP:
     		numTranspose += 2;
     		break;
     	case FLAT:
     		numTranspose -= 1;
     		break;
     	case DOUBLEFLAT:
     		numTranspose -= 2;
     		break;
     	case NATURAL:
     	case ABSENT:
     		break;
     	default:
     		throw new IllegalArgumentException("Illegal accidental");
     	}
     	
     	sequencePlayerNotes.add(new SequencePlayerNote(notePitch.transpose(numTranspose), startTicks, ticks));
 		return sequencePlayerNotes;
     }
     
     public RatNum getLength() {
     	return this.noteLength;
     }
 }
