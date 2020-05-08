 package midiParse;
 
 public class Note implements Comparable<Note>{
 	private final NoteName noteName;
 	private final int octave;
 	private double duration;
 	
 	Note(NoteName noteName) {
 		this.noteName = noteName;
 		this.octave = 0;
 	}
 	
 	public Note(NoteName noteName, int octave) {
 		this.noteName = noteName;
 		this.octave = octave;
 	}
 	
 	public NoteName note() {
 		return noteName;
 	}
 	
 	public int getOctave() {
  		return octave;
 	}
 	
 	public double getDuration() {
 		return duration;
 	}
 
 	public void setDuration(double duration) {
 		this.duration = duration;
 	}
 	
 	public int getDistance() {
 		return octave * (Notes.getNoteIndex(noteName) + 1);
 	}
 	
 	public Note getMidNote(Note n) {
 		double distance = 0.0;
 		
 		distance += Math.pow((this.noteName.getIndex() - n.noteName.getIndex()), 2);
 		distance += Math.pow((this.octave - n.octave), 2);
 		
 		long noteOctave = (long)distance;
 		long hops = Math.round((distance-noteOctave)*10);
 		
 		int noteNameIndex = (this.compareTo(n) <= 0) ? this.noteName.getIndex() : n.noteName.getIndex();
 		
		int realIndex = (int) (((noteNameIndex + hops) % 12)-1);
 		noteOctave = noteOctave + (hops/12);
 		
 		return new Note(Notes.getNoteName(realIndex), (int)noteOctave);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		boolean isEqual = false;
 		
 		if(obj instanceof Note)
 		{
 			Note o = (Note)obj;
 			if(octave == o.getOctave() && noteName.equals(o.note()))
 				isEqual = true;
 		}
 		
 		return isEqual;
 	}
 
 	@Override
 	public int compareTo(Note o) {
 		int comparedValue = 0;
 		
 		if(this.octave > o.octave) {
 			comparedValue = 1;
 		}
 		else if(this.octave < o.octave) {
 			comparedValue = -1;
 		} 
 		else {
 			int noteCompareVal = this.noteName.compareNote(o.noteName);
 			
 			if(noteCompareVal > 0)
 				comparedValue = -1;
 			else if(noteCompareVal < 0)
 				comparedValue = 1;
 			else
 				comparedValue = 0;
 		}
 		
 		return comparedValue;
 	}
 	
 	@Override
 	public String toString() {
 		return noteName.name() + "" + octave;
 	}
 }
