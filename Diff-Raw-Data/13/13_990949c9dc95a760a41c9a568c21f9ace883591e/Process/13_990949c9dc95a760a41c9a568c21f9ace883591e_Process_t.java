 import java.util.*;
 
 public class Process{
 	
 	// Used as default pool (equal temperament 12 semi-tones):
 	private List<String> et12Pool   = Arrays.asList("Ab", "A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G");
 	private int          et12Length = 12;
 	
 	// ---------------------------------------------------------------------------------------------
 	// stepCount: returns the distance between two notes using an input pool of type List
 	// Note, Note, optional: List or String[] -> int or String, String, optional: List or String[]-> int
 	// !!! Throw error when note is not found in pool
 	// ---------------------------------------------------------------------------------------------
 	// stepCount: Note, Note, List -> int 
 	public int stepCount(Note note1, Note note2, List<String> pool){
 	    int result = pool.indexOf(note2.getName()) - pool.indexOf(note1.getName());
 		if (result > 0){
 			return result;
 		}
 		else{
 			return result + et12Length;
 		}
 	}
 	// stepCount: Note, Note -> int
 	public int stepCount(Note note1, Note note2){
 		return stepCount(note1, note2, et12Pool);
 	}
 	// stepCount: Note, Note, String[] -> int
 	public int stepCount(Note note1, Note note2, String[] pool){
 		return stepCount(note1, note2, new ArrayList<String>(Arrays.asList(pool)));
 	}
 	// stepCount: String, String -> int
 	public int stepCount(String note1, String note2){
 		return stepCount(new Note(note1), new Note(note2), et12Pool);
 	}
 	// stepCount: String, String, List -> int
 	public int stepCount(String note1, String note2, List<String> pool){
 		return stepCount(new Note(note1), new Note(note2), pool);
 	}
 	// stepCount: String, String, String[] -> int
 	public int stepCount(String note1, String note2, String[] pool){
 		return stepCount(new Note(note1), new Note(note2), new ArrayList<String>(Arrays.asList(pool)));
 	}
 	
 	// ---------------------------------------------------------------------------------------------
 	// scalize: returns a Scale object derived from the formula applied to the pool with note as root,
 	// default pool is et12Pool. 
 	// Note or String, int[], optional: List or String[] -> Scale
 	// !!! throw errors: note was not found in pool
 	// ---------------------------------------------------------------------------------------------
 	// scalize: Note, int[], List -> Scale
 	public Scale scalize(Note note, int[] formula, List<String> pool){
 	    List<Note> noteList = new ArrayList<Note>();
 	    int currentIndex = pool.indexOf(note.getName());
 	    for (int e : formula){
 	    	noteList.add(new Note(pool.get(currentIndex)));
 	    	currentIndex = (currentIndex + e) % pool.size();
 	    }
 	    return new Scale(noteList);  
 	}
 	// scalize: Note, int[], String[] -> Scale
 	public Scale scalize(Note note, int[] formula, String[] pool){
 	    return scalize(note, formula, new ArrayList<String>(Arrays.asList(pool)));
 	}
	// scalize: Note, int[] -> Scale
 	public Scale scalize(Note note, int[] formula){
 	    return scalize(note, formula, et12Pool); 
 	}
	// scalize: String, int[], List -> Scale
 	public Scale scalize(String note, int[] formula, List<String> pool){
 		return scalize(new Note(note), formula, pool);
 	}
	// scalize: String, int[], String[] -> Scale
     public Scale scalize(String note, int[] formula, String[] pool){
 		return scalize(new Note(note), formula, new ArrayList<String>(Arrays.asList(pool)));
 	}
	// scalize: String, int[] -> Scale
 	public Scale scalize(String note, int[] formula){
 		return scalize(new Note(note), formula, et12Pool);
 	}
 	
 }
