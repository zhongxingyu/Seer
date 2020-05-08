 import java.util.List;
 import java.util.LinkedList;
 import java.util.Iterator;
 
 /**
  * Allows the game to have a partial set of words from the main dictionary.
  * 
  * @author Josh Gillham 
  * @version 10-9-12
  */
 public class WordCanidates  {
     /** Holds a list of potiential word canidates. */
     private List< String > wordCanidates;
     /** Holds a string the same length of gameWord but with dashes in places where no player has guessed. */
     private StringBuilder statusWord= new StringBuilder();
     
     /**
      * Initializes class variables.
      * 
      * @arg wordList is the list to use.
      * 
      * @throws IllegalArgumentException when wordList.size() == 0.
      * @throws IllegalArgumentException when the length of the status word does not equal the length of
      *  the first word in the word list.
     * @throws NullPointerException when statusWord or wordList are null.
      */
     public WordCanidates( String statusWord, List<String> wordList ) {
        if( wordList == null )
            throw new NullPointerException();
        if( wordList == null )
            throw new NullPointerException();
         if( wordList.size() == 0 )
             throw new IllegalArgumentException();
         if( statusWord.length() != wordList.get( 0 ).length() )
             throw new IllegalArgumentException();
         wordCanidates= new LinkedList< String >( wordList );
         this.statusWord= new StringBuilder( statusWord );
     }
     
     /**
      * Accesses the remaining word canidates.
      * 
      * @return the number of canidates.
      */
     public int count() {
         return wordCanidates.size();
     }
     /**
      * Divides word canidates into smaller lists that correspond to status word
      *  patterns i.e. --a---.
      *  
      * Example:
      *  --a--- List1
      *  -a--a- List2
      *  ------ List3
      *  
      * @arg letter is the letter to divide the lists by.
      * 
      * @return maps status word patterns to lists of words with those patterns.
      */
     public java.util.Map< String, List< String > > subDivide( char letter ) {
         int length= wordCanidates.get( 0 ).length();
         StringBuilder basePattern= this.statusWord;
         java.util.Map< String, List< String > > subLists= new java.util.HashMap< String, List< String > >(24);
         Iterator< String > i= wordCanidates.iterator();
         List< String > othersList= new java.util.ArrayList< String >(500);
         subLists.put( basePattern.toString(), othersList );
         while( i.hasNext() ) {
             String word= i.next();
             StringBuilder pattern= new StringBuilder( basePattern );
             int start= -1;
             int temp;
             while( ( temp= word.indexOf( letter, start + 1 ) ) > -1 ){
                 start= temp;
                 pattern.setCharAt( start, letter );
             }
             if( start != -1 ) {
                 List< String > subList= subLists.get( pattern.toString() );
                 if( subList == null ) {
                     subList= new java.util.ArrayList< String >(500);
                     subLists.put( pattern.toString(), subList );
                 }
                 subList.add( word );
             } else 
                 othersList.add( word );
                 
         }
         return subLists;
     }
     
     /**
      * Retrieves a random word from the list.
      * 
      * @return the word
      */
     public String getRandomCanidate() {
         System.out.println( "wordCanidates.size(): " + wordCanidates.size() );
         int rand= (int)(Math.random() * wordCanidates.size());
         System.out.println( "rand: " + rand );
         return wordCanidates.get( rand );
     }
 }
 
