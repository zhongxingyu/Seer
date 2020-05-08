 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.lang.NullPointerException;
 
 
 /**
  * This unit test will check the integrity of the Dictionary class.
  * 
  * @author  Josh Gillham
  * @version 10-16-12
  */
 public class testDictionary {
     /** Holds a list of test words. */
     java.util.List< String > testWords= new java.util.LinkedList< String >();
     /** Holds a copy of Dictionary. */
     Dictionary dictionary= Dictionary.getInstance();
     /** The file of the dictionary. */
     String dictionaryFile= "testwords.txt";
     
     /**
      * Setup word list.
      */
     public testDictionary () throws java.io.FileNotFoundException {
         testWords.add( "cat" );
         testWords.add( "hat" );
         
         loadDictionary();
         
     }
     
     private final void loadDictionary() {
         try{
             Dictionary.dispose();
             Dictionary.load( dictionaryFile );
             dictionary= Dictionary.getInstance();
         } catch( Exception e ) {
             e.printStackTrace();
             dictionary= null;
         }
     }
     
     /**
      * Test the load with a null. The result should be an error
      */
     @Test( expected= java.lang.NullPointerException.class )
     public void testLoad_Null() throws java.io.FileNotFoundException {
         Dictionary.load( null );
     }
     
     /**
      * Test the load with an empty path. The result should be an error
      */
     @Test( expected= IllegalArgumentException.class )
     public void testLoad_Empty() throws java.io.FileNotFoundException {
         Dictionary.load( "" );
     }
     
     /**
      * Test the load.
      */
     @Test
     public void testLoad() throws java.io.FileNotFoundException {
         loadDictionary();
         assertEquals( testWords, dictionary.getSet( 3 ) );
     }
     
     /**
      * Test getWord with bad lengths. The result should a null.
      */
     @Test
     public void testGetWord_TooSmall() {
         assertNull( dictionary.getWord( 2 ) );
     }
     
     /**
      * Test getWord with bad lengths. The result should a null.
      */
     @Test
     public void testGetWord_TooLarge() {
         assertNull( dictionary.getWord( 6 ) );
     }
     
     /**
      * I had a problem with it giving me the file name instead of a word.
      */
     @Test
     public void testGetWords_NotTheFileName() {
         String word= dictionary.getWord( 3 );
         if( dictionaryFile.equalsIgnoreCase( word ) )
             fail( "Word is the file name." );
     }
     
     /**
      * 
      */
     @Test
     public void testGetWords() {
         String word= dictionary.getWord( 3 );
         assertNotNull( word );
     }
     
     /**
      * 
      */
     @Test
     public void testGetWords_CorrectWords() {
         String word= dictionary.getWord( 3 );
         System.out.println( "testGetWords_CorrectWords word: "+ word );
        assertTrue( testWords.contains( testWords ) );
     }
     
     @Test
     public void testDepositWord() {
         dictionary.depositWord( "maneater" );
     }
     
     @Test
     public void testDepositWord_Multiple() {
         Dictionary.dispose();
         dictionary= Dictionary.getInstance();
         java.util.List< String > wordList= new java.util.LinkedList< String >();
         wordList.add( "cat" );
         dictionary.depositWord( "cat" );
         assertEquals( wordList, dictionary.getSet( 3 ) );
         wordList.add( "rat" );
         dictionary.depositWord( "rat" );
         assertEquals( wordList, dictionary.getSet( 3 ) );
         wordList.add( "mat" );
         dictionary.depositWord( "mat" );
         assertEquals( wordList, dictionary.getSet( 3 ) );
         wordList.add( "cow" );
         dictionary.depositWord( "cow" );
         assertEquals( wordList, dictionary.getSet( 3 ) );
         Dictionary.dispose();
     }
     
     @Test( expected= IllegalArgumentException.class )
     public void testDepositWord_Empty() {
         dictionary.depositWord( "" );
     }
     @Test( expected= NullPointerException.class )
     public void testDepositWord_Null() {
         dictionary.depositWord( null );
     }
     
     @Test
     public void testGetSet() {
         System.out.println( "testGetSet" );
         assertNotNull( dictionary.getSet( 3 ) );
         assertNotNull( dictionary.getSet( 5 ) );
     }
     
     @Test
     public void testGetSet_NotThere() {
         assertNull( dictionary.getSet( 1 ) );
         assertNull( dictionary.getSet( 2 ) );
         assertNull( dictionary.getSet( 4 ) );
         assertNull( dictionary.getSet( 6 ) );
     }
     
     @Test
     public void testCheckWordLength_Bad() {
         assertFalse( dictionary.checkWordLength( 1 ) );
         assertFalse( dictionary.checkWordLength( 2 ) );
         assertFalse( dictionary.checkWordLength( 4 ) );
         assertFalse( dictionary.checkWordLength( 6 ) );
     }
     
     @Test
     public void testCheckWordLength() {
         assertTrue( dictionary.checkWordLength( 3 ) );
         assertTrue( dictionary.checkWordLength( 5 ) );
     }
 }
