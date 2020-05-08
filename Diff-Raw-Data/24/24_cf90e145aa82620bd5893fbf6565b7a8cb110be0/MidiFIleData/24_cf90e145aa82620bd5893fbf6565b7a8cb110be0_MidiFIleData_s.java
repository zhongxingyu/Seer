 package midiParse;
 
 import java.util.HashMap;
 
 public class MidiFIleData {
 	/*averageNote duration expressed in seconds, songLength
 	expressed in minutes*/
 	private double BPM, averageNoteDuration, songLength;
 	private Note highestNote, lowestNote;
 	private HashMap<Note, Integer> noteFrequencies;
 }
