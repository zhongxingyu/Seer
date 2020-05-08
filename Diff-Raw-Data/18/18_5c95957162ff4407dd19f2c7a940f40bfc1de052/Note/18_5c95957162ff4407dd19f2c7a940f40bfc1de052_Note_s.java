 package midiParse;
 
 public class Note implements Comparable<Note>{
 	private final NoteName noteName;
 	private final int octave;
 	private double duration;
 	
 	Note(NoteName noteName) {
 		this.noteName = noteName;
 		this.octave = 0;
 	}
 	
 	Note(NoteName noteName, int octave) {
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
			int noteCompareVal = 0;
 			
 			if(noteCompareVal > 0)
				comparedValue = 1;
			else if(noteCompareVal < 0)
 				comparedValue = -1;
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
