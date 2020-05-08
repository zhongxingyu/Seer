 package midiParse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class MidiFileData {
 	/*averageNote duration expressed in seconds, songLength
 	expressed in minutes*/
 	private double BPM, averageNoteDuration, songLength;
 	private long totalNumOfNotes;
 	private Note highestNote, lowestNote, longestNote, shortestNote;
 	private HashMap<String, Long> noteFrequencies;
 	
 	public MidiFileData(){
 		BPM = 0;
 		averageNoteDuration = 0;
 		songLength = 0;
 		totalNumOfNotes = 0;
 		Note floorNote = new Note(NoteName.C, 0);
 		floorNote.setDuration(Double.MAX_VALUE);
 		Note celingNote = new Note(NoteName.C, Integer.MAX_VALUE);
 		celingNote.setDuration(Double.MIN_VALUE);
 		highestNote = floorNote;
 		lowestNote = celingNote;
 		longestNote = floorNote;
 		shortestNote = celingNote;
 		noteFrequencies = new HashMap<String, Long>();
 	}
 	
 	public void setBPM(double bpm) {
 		this.BPM = bpm;
 	}
 	
 	public double getBPM() {
 		return BPM;
 	}
 	
 	public void setSongLength(double length) {
 		this.songLength = length;
 	}
 	
 	public double getSongLength() {
 		return songLength;
 	}
 	
 	public double getAverageNoteDuration() {
 		return averageNoteDuration;
 	}
 
 	public long getTotalNumOfNotes() {
 		return totalNumOfNotes;
 	}
 
 	public Note getHighestNote() {
 		return highestNote;
 	}
 
 	public Note getLowestNote() {
 		return lowestNote;
 	}
 	
 	public Note getShortestNote() {
 		return shortestNote;
 	}
 	
 	public Note getLongestNote() {
 		return longestNote;
 	}
 	
 	public String[] getMostFrequentNotes() {
 		ArrayList<String> notes = new ArrayList<String>();
 		long highestFrequency = 0;
 		
 		Iterator<String> iterator = noteFrequencies.keySet().iterator();
 		
 		while(iterator.hasNext()) {
 			String key = iterator.next();
 			
 			long currentNoteFrequency = noteFrequencies.get(key);
 			
 			if(highestFrequency < currentNoteFrequency) {
 				notes.clear();
 				notes.add(key);
 			}
 			else if(highestFrequency == currentNoteFrequency)
 				notes.add(key);
 		}
 		
 		String[] toReturn = new String[notes.size()];
 		notes.toArray(toReturn);
 		
 		return toReturn;
 	}
 
 	public HashMap<String, Long> getNoteFrequencies() {
 		return noteFrequencies;
 	}
 
 	public void addNote(Note note) {
		if(highestNote.compareTo(note) > 0)
 			highestNote = note;
		else if(lowestNote.compareTo(note) < 0)
 			lowestNote = note;
 		
 		if(note.getDuration() > longestNote.getDuration())
 			longestNote = note;
 		else if(note.getDuration() < shortestNote.getDuration())
 			shortestNote = note;
 		
 		totalNumOfNotes++;
 		averageNoteDuration = songLength / totalNumOfNotes;
 		
 		long currentFrequency = (noteFrequencies.containsKey(note.toString()))? noteFrequencies.get(note.toString()) : 0;
 		
 		noteFrequencies.put(note.toString(), currentFrequency + 1);
 	}
 }
