 import java.util.Set;
 import java.util.TreeSet;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.File;
 import java.util.Scanner;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Responsible for loading the word data bank and also for retrieving a random word.
  * 
  * @author Josh Gillham
  * @version 9-23-12
  */
 public class Dictionary{
     /** Holds a copy of the Dictionary class. */
     static private Dictionary instance= null;
     
     /**
      * Creates a new instance of Dictionary if it doesnot exist otherwise it returns the last copy.
      * 
      * Post Conditions:
      *  -Ensures an instance of Dictionary exists
      * 
      * @return the new instance.
      */
     static public Dictionary getInstance( ) {
         if( instance == null )
            instance= new Dictionary( );
        return instance;
     }
     
     /**
      * Deletes the dictionary instance.
      * 
      * Post Conditions:
      *  -Ensures an instance of Dictionary does NOT exist
      */
     static public void dispose() {
         instance= null;
     }
     
     /** Contains a list of array lists where there will be an inner list for each word length. */
     private Map< Integer, List<String> > dataBank= new TreeMap< Integer, List<String> >();
     
     /**
      * Used to ensure word lengths meet the proper size.
      * 
      * @arg length the lenth of the word to check.
      * 
      * @return true if the word length refers to a list words.
      */
     public boolean checkWordLength( int length ) {
         return dataBank.get( Integer.valueOf( length ) ) != null;
     }
     
     /**
      * Adds a word into the dictionary data bank. Each word is sorted by
      *  length into the databanks inner lists.
      *  
      * Post Conditions:
      *  -Dictionary has at least one word.
      * 
      * @arg word is the new word to add.
      * 
      * @throws NullPointerException when word is null.
      * @throws IllegalArgumentException when word is empty.
      */
     public void depositWord( String word ) {
         if( word == null )
             throw new NullPointerException();
         if( word.isEmpty() )
             throw new IllegalArgumentException();
         List< String > set= this.getSet( Integer.valueOf( word.length() ) );
         if( set != null ){
             set.add( word );
         } else {
             set= new ArrayList< String >( 1000 );
             set.add( word );
             dataBank.put( Integer.valueOf( word.length() ), set );
         }
     }
     
     /**
      * Loads the dictionary file.
      * 
      * @arg file the dictionary file to load.
      * 
      * Post Conditions:
      *  -Ensures a instance of Dictionary exists.
      *  -Dictionary now has a data bank of the words from file.
      * 
      * @throws NullPointerException when file is null.
      * @throws IllegalArgumentException when file is empty.
      * @throws another error when file is bad.
      */
     static public void load( String file ) throws NullPointerException, java.io.FileNotFoundException {
         if( file == null )
             throw new NullPointerException();
             
         if( file.isEmpty() )
             throw new IllegalArgumentException();
         // Get a copy of dictionary.
         Dictionary instance= getInstance();
         // Read in each word line by line
         File src= new File( file );
         Scanner input= new Scanner( src );
         while( input.hasNext() ) {
             instance.depositWord( input.next() );
         }
     }
     
     /**
      * Finds a random word from the dictionary.
      * 
      * @arg length is the length of the word to find.
      * 
      * @return the random word OR null if the length is not in the dictionary or the word list is empty.
      */
     public String getWord( int length ) {
         List< String > set= this.getSet( length );
         if( set == null ||  set.size() == 0 ){
             return null;
         } else {
             // Get a random word
             int randomIndex= (int)( Math.random() * set.size() );
             return set.get( randomIndex );
         }
     }
     
     /**
      * Accesses a set from the dictionary.
      * 
      * @arg length is the length of the word to find.
      * 
      * @return the set of words OR null if the length is not in the dictionary.
      */
     public List< String > getSet( int length ) {
         return dataBank.get( Integer.valueOf( length ) );
     }
         
 }
