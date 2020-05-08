 package sound;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Tuplet implements Playable 
 {
     private final TupletType type; 
     private final List<Note> notes; 
     
     public Tuplet(TupletType type, List<Note> notes)
     {
         this.type = type; 
         this.notes = new ArrayList<Note>(notes);
     }
     
     public List<Note> getNotes()
     {
         return new ArrayList<Note>(notes);
     }
     
     public TupletType getType()
     {
         return type; 
     }
     
     public String toString() {
         String s = "Tuplet(" ;
         for (Note n : this.notes)
             s = s + n.toString() + " ";
         s = s.trim() + ")";
         return s;   
     }
 
 	public List<SequencePlayerNote> play(int startTicks, int numTicks, RatNum defaultNoteLength) {
 		List<SequencePlayerNote> sequencePlayerNotes = new ArrayList<SequencePlayerNote> ();
 		int ticks = startTicks;
 		for (Note note:notes) {
 			sequencePlayerNotes.addAll(note.play(ticks, numTicks, defaultNoteLength));
			ticks += (numTicks * note.getLength().getNumer())/
			        (note.getLength().getDenom());
 		}
 		return sequencePlayerNotes;
 	}
 	
 	public RatNum getLength() {
 		RatNum tupletLength = new RatNum(0,1);
 		for (Note note: notes) {
 			int numer1 = tupletLength.getNumer();
 			int denom1 = tupletLength.getDenom();
 			int numer2 = note.getLength().getNumer();
 			int denom2 = note.getLength().getDenom();
 			tupletLength = new RatNum((numer1*denom2 + numer2*denom1), denom1*denom2);
 		}
 		return tupletLength;
 	}
 }
