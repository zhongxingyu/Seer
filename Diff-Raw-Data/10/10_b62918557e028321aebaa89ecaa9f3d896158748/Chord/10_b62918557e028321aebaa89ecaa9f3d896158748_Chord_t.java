 import java.util.*;
 
 public class Chord extends NoteGroup{
     
 	//===================================================================
     //                       Constructors
     //===================================================================
 	
     // NoteList, String -> Chord
     public Chord(List<Note> newNotes, String newName){
         super(newNotes, newName);
     }
     // NoteList -> Chord
     public Chord(List<Note> newNotes){    
         super(newNotes);
     }
     // NoteArray, String -> Chord
     public Chord(Note[] newNotes, String newName){
         super(new ArrayList<Note>(Arrays.asList(newNotes)), newName);
     }
     // NoteArray -> Chord
     public Chord(Note[] newNotes){
         super(new ArrayList<Note>(Arrays.asList(newNotes)));
     }
     // StringArray, String -> Chord
     public Chord(String[] newNotes, String newName){
     	super(newNotes, newName);
     }
     // StringArray -> Chord
     public Chord(String[] newNotes){
     	super(newNotes);
     }
     //Copy constructor
     // Chord -> Chord
     public Chord(Chord newChord){
     	super(newChord);
     }
     
     //===================================================================
     //                          Special Setters
     //===================================================================
     
     // Inversions
    // invert(): Rotates the order of the notes. 1 puts the first note of the list in the last spot of the list, 2
    // puts the first two notes of the list in the last spots of the list, and so on. Negative parameter does the 
    // opposite, putting n items from the last spots of the list to the front.
     public void invert(int n){
     	rotateNotes(n);
     }
    // invert(): No argument case, default value is 1
     public void invert(){
     	rotateNotes(1);
     }
     
 }
