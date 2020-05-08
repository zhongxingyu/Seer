 package midiParse;
 
 import java.util.HashMap;
 
 public class MidiFIleData {
 	/*averageNote duration expressed in seconds, songLength
 	expressed in minutes*/
 	private double BPM, averageNoteDuration, songLength;
	private long totalNumOfNotes;
 	private Note highestNote, lowestNote;
 	private HashMap<Note, Integer> noteFrequencies;
	
	public void setBPM(double bpm) {
		this.BPM = bpm;
	}
	
	public double getBPM() {
		return BPM;
	}
	
 }
