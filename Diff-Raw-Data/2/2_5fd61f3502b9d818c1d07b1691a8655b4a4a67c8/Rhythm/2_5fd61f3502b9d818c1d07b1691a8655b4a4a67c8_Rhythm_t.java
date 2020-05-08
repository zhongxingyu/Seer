 package com.adagio.structures;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.adagio.language.figures.Figure;
 import com.adagio.language.musicnotes.AbsoluteMusicNote;
 import com.adagio.language.rhythm.RhythmComponent;
 import com.adagio.language.times.Time;
 import com.adagio.structures.instruments.Instrument;
 
 public class Rhythm {
 
 	private List<RhythmComponent> components;
 	
 	public Rhythm(){
 		components = new ArrayList<RhythmComponent>();
 	}
 	
 	public Rhythm(List<RhythmComponent> components){
 		this.components = components;
 	}
 	
 	
 	/**
 	 * For a chord given as a list of absolute music notes, return the assignation of the
 	 * positional components of the rhythm
 	 * @param aNotes
 	 * @return A Map: key (component position) value (list of notes that assigned to that component)
 	 */
 	@SuppressWarnings("rawtypes")
 	public Map<Integer, List<AbsoluteMusicNote>> assignNotes(List<AbsoluteMusicNote> aNotes, Instrument instrument){
 		Map<Integer, List<AbsoluteMusicNote>> assignation = new HashMap<Integer, List<AbsoluteMusicNote>>(); 
 		Map<Integer, AbsoluteMusicNote> remainNotes = new HashMap<Integer, AbsoluteMusicNote>();
 		List<AbsoluteMusicNote> componentNotes = null;
 		List<AbsoluteMusicNote> aNotes2 = new ArrayList<AbsoluteMusicNote>();
 		AbsoluteMusicNote aNote = null;
 		AbsoluteMusicNote aNoteIncreased = null;
 		int position = -1;
 		int noteIndex = 0;
 		int optionalIndex = -1;
 		int emptiestKey = 0;
 		int octaveIncrement = 0;
 		
 		//Clone the original notes
 		for(AbsoluteMusicNote current: aNotes){
 			aNotes2.add(current.clone());
 		}
 		
 		//TODO test this block
 		//If there is to much notes in the chord, we try to delete the optionals
 		optionalIndex = hasOptional(aNotes2);
 		while(aNotes2.size() > numOfPositionalComponents() && optionalIndex != -1){
 			aNotes2.remove(optionalIndex);
 			optionalIndex = hasOptional(aNotes2);
 		}
 		
 		//Copy the list of notes as a HashMap
 		for(int i=0; i < aNotes2.size(); i++){
 			remainNotes.put(i, aNotes2.get(i).clone());
 		}
 		
 		//Assign Note1 ... NoteN to the chord
 		for(RhythmComponent current: components){
 			position = current.getNote().getNotePosition();
 			//If is a positional component
 			if(position > 0){
 				noteIndex = (position-1)%aNotes.size();
 				octaveIncrement = (position-1)/aNotes.size();
 				componentNotes = new ArrayList<AbsoluteMusicNote>();
 				
 				//If the note is repeated, increase the octave
 				aNote = aNotes2.get(noteIndex);
 				aNoteIncreased = aNote.clone();
 				aNoteIncreased.increaseOctave(octaveIncrement);
 				if(instrument.belong(aNoteIncreased)){
 					aNote = aNoteIncreased;
 				}
 				
 				componentNotes.add(aNote);
 				assignation.put(position, componentNotes);
 				remainNotes.remove(noteIndex);
 			}
 		}
 		
 		Map.Entry x = null;
 		Iterator<Entry<Integer, AbsoluteMusicNote>> it;
 		it = remainNotes.entrySet().iterator();
 
 		//Assign the rest of the notes
 		while (it.hasNext()) {
 			x = (Map.Entry) it.next();
 			aNote = ((AbsoluteMusicNote) x.getValue()).clone();
 			emptiestKey = emptiest(assignation);
 			assignation.get(emptiestKey).add(aNote);
 		}
 		
 		
 		return assignation;
 	}
 	
 	public List<List<AbsoluteMusicNote>> apply(List<List<AbsoluteMusicNote>> chordsAsLists, Instrument instrument, Time time, AbsoluteMusicNote relative){
 		List<List<AbsoluteMusicNote>> voices = new ArrayList<List<AbsoluteMusicNote>>();
 		List<Map<Integer, List<AbsoluteMusicNote>>> assignations = new ArrayList<Map<Integer, List<AbsoluteMusicNote>>>();
 		List<AbsoluteMusicNote> voice, notesForComponent;
 		double initTime, finalTime, silencesHeadDuration, noteDuration, timeDuration;
 		int position, chordIndex;
 		AbsoluteMusicNote currentRelative = relative.clone();
 
 		timeDuration = time.duration();
 
 		//Obtains the assignation of the Components for each chord
 		// (C E G) --> Note1 (C G), Note2(E)
 		// (C E G D) --> Note1 (C, G) Note2(E D)
 		for(List<AbsoluteMusicNote> current: chordsAsLists){
 			assignations.add(assignNotes(current, instrument));
 		}
 
 		for(RhythmComponent currentComponent: components){
 			initTime = currentComponent.getInitTime();
 			finalTime = currentComponent.getFinalTime();
 			silencesHeadDuration = initTime*timeDuration;
 			noteDuration = (finalTime - initTime)*timeDuration;
 			position = currentComponent.getNote().getNotePosition();
 
 			//If the component is positional
 			if(position != -1){
 				//selects the chord associated with the component
 				chordIndex = selectChordIndex(currentComponent, chordsAsLists.size());
 				notesForComponent = assignations.get(chordIndex).get(position);
 			}
 			//If the component is absolute
 			else{
 				//generates the note 
 				//TODO check that the relative is correctly applied
 				AbsoluteMusicNote aRhythmNote = currentComponent.getNote().getMusicNote().toAbsoluteMusicNote(currentRelative);
 				notesForComponent = new ArrayList<AbsoluteMusicNote>();
 				notesForComponent.add(aRhythmNote);
 			}
 			
 			for(AbsoluteMusicNote currentNote: notesForComponent){
 
 				//We add silences in the head of the voice
 				//in order to set correctly the note in the bar
 				voice = new ArrayList<AbsoluteMusicNote>();
 				voice = genLigaturedNotes(AbsoluteMusicNote.genSilence(), silencesHeadDuration);
 
 				//We add a the note (or succession of ligatured notes)
 				voice.addAll(genLigaturedNotes(currentNote, noteDuration));
 				voices.add(voice);
 				
 				currentRelative = currentNote.clone();
 			}
 		}
 
 		//We add a ghost-voice with a full-bar silence
 		voice = genLigaturedNotes(AbsoluteMusicNote.genSilence(), timeDuration);
 		voices.add(voice);
 
 		return voices;
 	}
 	
 	/**
 	 * Obtain the key of the element of the Map that has the emptiest List of
 	 * Absolute music notes
 	 * @param assignation The Map
 	 * @return A number that is the index of the emptiest element
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public int emptiest( Map<Integer, List<AbsoluteMusicNote>> assignation){
 		int key = -1;
 		List<AbsoluteMusicNote> aNotes = null;
 		int smaller = Integer.MAX_VALUE;
 		
 		Map.Entry x = null;
 		Iterator<Entry<Integer, List<AbsoluteMusicNote>>> it;
 		it = assignation.entrySet().iterator();
 
 		//For each note unsigned
 		while (it.hasNext()) {
 			x = (Map.Entry) it.next();
 			aNotes = (List<AbsoluteMusicNote>) x.getValue();
 			if(aNotes.size() < smaller){
 				smaller = aNotes.size();
				key = (Integer) x.getKey(); 
 			}
 		}
 		return key;
 	}
 	
 	/**
 	 * Calculate the number of positional components in the rhythm
 	 * @return An integer number that represents the num of positional components
 	 */
 	private int numOfPositionalComponents(){
 		int position = 0;
 		Set<Integer> diffItems = new TreeSet<Integer>();
 		
 		for(RhythmComponent current: components){
 			position = current.getNote().getNotePosition();
 			if(position > 0){
 				diffItems.add(position);
 			}
 		}
 		return diffItems.size();
 	}
 	
 	/**
 	 * Checks if some note of the list is optional and return his position
 	 * @param aNotes List of AbsoluteMusicNote
 	 * @return An index that represents the position in the vector of the
 	 * first optional occurrence. -1 if there is no optional note
 	 */
 	private int hasOptional(List<AbsoluteMusicNote> aNotes ){
 		for(int i = 0; i < aNotes.size(); i++){
 			if(aNotes.get(i).isOptional()){
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Given a component and the number of chords that we have, select the index
 	 * of the chord associated with the component
 	 * @param component
 	 * @param numOfChords
 	 * @return
 	 */
 	private int selectChordIndex (RhythmComponent component, int numOfChords) {
 
 		double factor = 1.0/(double)numOfChords;
 		double initTime = component.getInitTime();
 
 		for(int i = 0; i < numOfChords; i++){
 
 			if((initTime >= (factor*i)) && (initTime < (factor*(i+1)))){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Generates a succession of ligated AbsoluteMusicNote(s) until reach (if is possible)
 	 * the duration specified 
 	 * @param note Note to play in the succession
 	 * @param duration Duration to achieve (4 whole note, 2 half note, ...)
 	 * @return A list of notes with the flag "ligatured" set to true (except the last one)
 	 */
 	public static List<AbsoluteMusicNote> genLigaturedNotes(AbsoluteMusicNote note, double duration){
 		List<AbsoluteMusicNote> ligaturedNotes = new ArrayList<AbsoluteMusicNote>();
 		Figure closer = new Figure();
 		double currentDuration = duration;
 		AbsoluteMusicNote aNote = new AbsoluteMusicNote(note);
 		Duration noteDuration;
 
 		while(closer != null){
 			closer = Figure.closerFigure(currentDuration);
 			if(closer != null){
 				noteDuration = new Duration(closer);
 				aNote.setDuration(noteDuration);
 				ligaturedNotes.add(aNote.clone());
 				currentDuration -= closer.duration();
 			}
 		}
 		
 		for(int i = 0; i < ligaturedNotes.size(); i++){
 			if(i != ligaturedNotes.size()-1){
 				ligaturedNotes.get(i).setLigatured(true);
 			}
 			else{
 				ligaturedNotes.get(i).setLigatured(false);
 			}
 		}
 
 		return ligaturedNotes;
 	}
 	
 	@Override
 	public String toString(){
 		String composition = "";
 		for(RhythmComponent current: components){
 			composition += "[" + current.toString() + "] ";
 		}
 		return composition;
 	}
 }
