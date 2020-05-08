 // !!! more constructors, but read about factories first
 package sounds;
 import java.util.*;
 
 /**
 * Represents a group of chords.
 */
 public class ChordCollection implements Iterable<Chord>{
 	
 	//===================================================================
     //                      Instance Variables
     //===================================================================
 	
 	/**
 	* List that holds the collection of chords. 
 	*/
 	private List<Chord> chords;
 	
 	/**
 	* Number of chords in the collection of chords. 
 	*/
 	private int size;
 	
 	//===================================================================
     //                        Constructors
     //===================================================================
 	
 	/**
 	* Builds a ChordCollection object using an arbitrary number of Chord objects.
     * @param newNotes Note objects that will be added to the NoteCollection.
 	*/
 	public ChordCollection(Chord... newChords){
 	    chords = new ArrayList<Chord>(Arrays.asList(newChords));
 	    size   = chords.size();
 	}
 	
 	public ChordCollection(List<Chord> newChordList){
 	    chords = newChordList;
 	    size   = chords.size();
 	}
 	
 	/**
    	* Copy constructor. Use this to make copies of ChordCollection objects. 
     * @param newChordGroup The ChordCollection object to be copied.
     */
 	public ChordCollection(ChordCollection newChordCollection){
 		this(newChordCollection.getChords());
 	}
 	
 	//===================================================================
     //                            Methods
     //===================================================================
 	/**
 	* Builds a NoteCollectionIterator object, used to iterate through the Note objects.
 	* @return NoteCollectionIterator object.  
 	*/
 	@Override
 	public Iterator<Chord> iterator() {return new ChordCollectionIterator();}
 	
 	//===================================================================
     //                            Setters
     //===================================================================
 	
 	public void setChords(List<Chord> newChords){
 		chords = newChords;
 		size   = chords.size();
 	}
 	
 	//===================================================================
     //                            Getters 
     //===================================================================
 	
 	public List<Chord> getChords(){
 		return chords;
 	}
 	
 	public int getSize(){
 		return size;
 	}
 	
 	//===================================================================
     //                       Special Getters
     //===================================================================
 	
 	
 	/**
 	* Builds a String with the names of the chords in the chords list.
 	* @return Names of the chords as a String.
 	*/
     public String getChordsAsString(){
         List<String> noteList = new ArrayList<String>();
         for (Chord e : chords){
         	noteList.add(e.getName());
         }
         return Arrays.toString(noteList.toArray(new String[noteList.size()]));
     }
     
     /**
     * Builds a String with the values of the object.
     * @return The state of the ChordGroup as a String.
     */
     @Override
     public String toString(){
     	return "chords: " + getChordsAsString() + " size: " + size;
     }
     
    //===================================================================
     //                         Inner Classes
     //===================================================================
     
     /**
     * Iterator class used to iterate through the Chord objects. Implementing Iterator
     * makes a ChordCollection object support for in loops for(Chord n : NoteCollection).
     */
     private class ChordCollectionIterator implements Iterator<Chord>{
         private int current = 0;
         
         /**
         * Is there an item in the collection next to this one?
         */
         @Override
         public boolean hasNext() {return current < size;}
         
         /**
         * Not supported.
         */
         @Override
         public void remove() {throw new UnsupportedOperationException();}
         
         /**
         * Returns the current item, iterates to the next one.
         */
         @Override
         public Chord next(){
             if (!hasNext()) throw new NoSuchElementException();
         	return chords.get(current++);
         }
     }
 }
