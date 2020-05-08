 package clusteringAlgoritm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import midiParse.Note;
 import midiParse.NoteName;
 
 public class MidiFileData {
 	private final Pattern noteRegex = Pattern.compile("([a-zA-z]*)(\\d*)");
 	private final int MAX_OCTAVE = 12;
 	private final int THIRTY_MIN_IN_SEC = 1800;
 	/*averageNote duration expressed in seconds, songLength
 	expressed in minutes*/
 	private String fileName;
 	protected double BPM;
 	protected double averageNoteDuration;
 	protected double songLength;
 	protected long totalNumOfNotes;
 	protected Note highestNote, lowestNote, longestNote, shortestNote;
 	private HashMap<String, Long> noteFrequencies;
 	private boolean visited = false;
 	
 	public boolean isVisited() {
 		return visited;
 	}
 
 	public void setVisited(boolean visited) {
 		this.visited = visited;
 	}
 
 	public MidiFileData(){
 		BPM = 0;
 		averageNoteDuration = 0;
 		songLength = 0;
 		totalNumOfNotes = 0;
 		highestNote = new Note(NoteName.C, 0);
 		lowestNote = new Note(NoteName.B, MAX_OCTAVE);
 		longestNote = new Note(NoteName.C, 0);
 		longestNote.setDuration(0);
 		shortestNote = new Note(NoteName.C, 0);
 		shortestNote.setDuration(THIRTY_MIN_IN_SEC);
 		noteFrequencies = new HashMap<String, Long>();
 	}
 	
 	public void setAverageNoteDuration(double averageNoteDuration) {
 		this.averageNoteDuration = averageNoteDuration;
 	}
 
 	public void setTotalNumOfNotes(long totalNumOfNotes) {
 		this.totalNumOfNotes = totalNumOfNotes;
 	}
 
 	public void setHighestNote(Note highestNote) {
 		this.highestNote = highestNote;
 	}
 
 	public void setLowestNote(Note lowestNote) {
 		this.lowestNote = lowestNote;
 	}
 
 	public void setLongestNote(Note longestNote) {
 		this.longestNote = longestNote;
 	}
 
 	public void setShortestNote(Note shortestNote) {
 		this.shortestNote = shortestNote;
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
 	
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String songTitle) {
 		this.fileName = songTitle;
 	}
 
 	//Returns the highest, most frequent note
 	public Note getMostFrequentNote() {
 		ArrayList<Note> notes = new ArrayList<Note>();
 		long highestFrequency = 0;
 		
 		Iterator<String> iterator = noteFrequencies.keySet().iterator();
 		
 		while(iterator.hasNext()) {
 			String key = iterator.next();
 			
 			long currentNoteFrequency = noteFrequencies.get(key);
 			
 			if(highestFrequency < currentNoteFrequency) {
 				notes.clear();
 				Matcher m = noteRegex.matcher(key);
 				m.find();
 				NoteName note = NoteName.valueOf(m.group(1));
 				int octave = Integer.parseInt(m.group(2));
 				notes.add(new Note(note,octave));
 			}
 			else if(highestFrequency == currentNoteFrequency){
 				NoteName note = NoteName.valueOf((String.valueOf(key.charAt(0))));
 				int octave = Integer.parseInt(key.substring(1, key.length()));
 				notes.add(new Note(note,octave));
 				notes.add(new Note(note, octave));
 			}
 		}
 		
 		Note toReturn = new Note(NoteName.C, -1);
 		
 		for(Note n : notes){
 			if(n.compareTo(toReturn) > 0)
 				toReturn = n;
 		}
 		
 		return toReturn;
 	}
 	
 	public Note getLeastFrequentNote(){
 		ArrayList<Note> notes = new ArrayList<Note>();
 		long lowestFrequency = Long.MAX_VALUE;
 		
 		Iterator<String> iterator = noteFrequencies.keySet().iterator();
 		
 		while(iterator.hasNext()) {
 			String key = iterator.next();
 			
 			long currentNoteFrequency = noteFrequencies.get(key);
 			
 			if(lowestFrequency > currentNoteFrequency) {
 				notes.clear();
 				NoteName note = NoteName.valueOf((String.valueOf(key.charAt(0))));
 				int octave = Integer.parseInt(key.substring(1, key.length()));
 				notes.add(new Note(note,octave));
 			}
 			else if(lowestFrequency == currentNoteFrequency){
 				NoteName note = NoteName.valueOf((String.valueOf(key.charAt(0))));
 				int octave = Integer.parseInt(key.substring(1, key.length()));
 				notes.add(new Note(note,octave));
 				notes.add(new Note(note, octave));
 			}
 		}
 		
 		Note toReturn = new Note(NoteName.C, -1);
 		
 		for(Note n : notes){
 			if(n.compareTo(toReturn)< 0)
 				toReturn = n;
 		}
 		
 		return toReturn;
 	}
 
 	public HashMap<String, Long> getNoteFrequencies() {
 		return noteFrequencies;
 	}
 
 	public void addNote(Note note) {
 		if(note.getDuration() > 0){			
 			if(highestNote.compareTo(note) < 0)
 				highestNote = note;
 			else if(lowestNote.compareTo(note) > 0 && note.compareTo(new Note(NoteName.Cs, 0)) > 0)
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
 	
 	public double getDistance(MidiFileData d) {
 		double result = 0.0;
 		
 		result += Math.pow((this.BPM - d.BPM), 2);
 		result += Math.pow((this.averageNoteDuration - d.averageNoteDuration), 2);
 		result += Math.pow((this.songLength - d.songLength), 2);
 		result += Math.pow((this.totalNumOfNotes - d.totalNumOfNotes), 2);
 		result += Math.pow((this.longestNote.getDuration() - d.longestNote.getDuration()), 2);
 		result += Math.pow((this.shortestNote.getDuration() - d.shortestNote.getDuration()), 2);
 		result += Math.pow((this.highestNote.getDuration() - d.highestNote.getDuration()), 2);
 		result += Math.pow((this.lowestNote.getDuration() - d.lowestNote.getDuration()), 2);
 		
 		//Skipping most frequent notes, might be a good idea to get the lowestMostFrequent, etc.
 		
 		//result += Math.pow((this.getMostFrequentNotes(), b)
 		
 		
 		result += Math.pow((this.highestNote.getDistance() - d.getHighestNote().getDistance()), 2);
 		result += Math.pow((this.lowestNote.getDistance() - d.getLowestNote().getDistance()), 2);
 		result += Math.pow((this.longestNote.getDistance() - d.longestNote.getDistance()), 2);
 		result += Math.pow((this.shortestNote.getDistance() - d.shortestNote.getDistance()), 2);
 		
 		return Math.abs(Math.sqrt(result));
 		
 	}
 	
 }
