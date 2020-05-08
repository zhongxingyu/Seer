 import java.util.List;
 import java.util.ArrayList;
 import java.io.File;
 import java.util.Scanner;
 
 /**
  * Responsible for loading the word data bank and also for retrieving a random word.
  * 
  * @author Josh Gillham
  * @version 9-23-12
  */
 public class Dictionary{
     /** The smallest allowed word in the dictionary. */
     static public final int MIN_WORDLENGTH= 3;
     /** The largest allowed word in the dictionary. */
     static public final int LARGEST_WORD= 12;
     /** Contains a list of array lists where there will be an inner list for each word length. */
    static List< ArrayList<String> > dataBank= new ArrayList< ArrayList<String> >(LARGEST_WORD);
     /** I need the extra code because the Java static constructors were not working properly. */
     static public boolean constructed= false;
     {
         staticConstructor();
     }
     /**
      * Adds one inner list for each word length.
      */
     static private void staticConstructor(){
         if( !constructed ) {
             constructed= true;
             // Add one list for each length including 1
             for( int i= 0; i < LARGEST_WORD; ++i ) {
                 dataBank.add( new ArrayList<String>(1000) );
             }
         }
     }
     
     /**
      * Used to ensure word lengths meet the proper size.
      */
     static public boolean checkWordLength( int length ) {
         return length >= MIN_WORDLENGTH && length <= dataBank.size();
     }
     
     /**
      * Adds a word into the dictionary data bank. Each word is sorted by
      *  length into the databanks inner lists.
      *  
      * @arg word is the new word to add.
      * 
      * @throws NullPointerException when word is null.
      * @throws IllegalArgumentException when word is empty.
      * @throws IllegalArgumentException when word is smaller than MIN_WORDLENGTH or larger than MAX_WORDLENGTH.
      */
     static public void depositWord( String word ) {
         staticConstructor();
         if( word == null )
             throw new NullPointerException();
         if( word.isEmpty() )
             throw new IllegalArgumentException();
         if( checkWordLength( word.length() ) )
             dataBank.get( word.length() - 1 ).add( word );
     }
     
     
     
     /**
      * Loads the dictionary file.
      * 
      * @arg file the dictionary file to load.
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
         
         // Read in each word line by line
         File src= new File( file );
         Scanner input= new Scanner( src );
         while( input.hasNext() ) {
             depositWord( input.next() );
         }
     }
     
     /**
      * Finds a random word from the dictionary.
      * 
      * Preconditions:
      *  Word data bank must have at least one word.
      * 
      * @arg length is the length of the word to find.
      * 
      * @return the random word.
      * @return null if the length is not allowed in the dictionary.
      * 
      * @throw IllegalArgumentException when the length is less than MIN_WORDLENGTH 
      *   or greater than MAX_WORDLENGTH
      */
     static public String getWord( int length )throws java.util.NoSuchElementException {
         staticConstructor();
         if( !checkWordLength( length ) )
             throw new IllegalArgumentException();
         List<String> list= dataBank.get( length - 1 );
         if( list.size() == 0 )
             throw new java.util.NoSuchElementException();
         
             
         if( !checkWordLength( length ) )
             return null;
         // Get a random word
         int randomIndex= (int)( Math.random() * list.size() );
         return list.get( randomIndex );
     }
         
 }
