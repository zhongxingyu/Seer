 
 
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * The test class testWordCanidates.
  *
  * @author  Josh Gillham
  * @version 10-10-12
  */
 public class testWordCanidates {
     List< String > testWordList;
     String onlyWord= "cant";
     @Before
     public void setup() {
         testWordList= new LinkedList< String >();
         testWordList.add( onlyWord );
     }
    @Test( expected= IllegalArgumentException.class )
     public void testConstructor_nullWord() {
         new WordCanidates( null, testWordList );
     }
    @Test( expected= IllegalArgumentException.class )
     public void testConstructor_nullList() {
         new WordCanidates( "adfs", null );
     }
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_emptyList() {
         new WordCanidates( "adfs", new LinkedList< String >() );
     }
     @Test
     public void testCount() {
         WordCanidates instance= new WordCanidates( "adfs", testWordList );
         assertEquals( testWordList.size(), instance.count() );
         
         testWordList.add( "tester" );
         instance= new WordCanidates( "adfs", testWordList );
         assertEquals( testWordList.size(), instance.count() );
         
         testWordList.add( "confidence" );
         instance= new WordCanidates( "adfs", testWordList );
         assertEquals( testWordList.size(), instance.count() );
     }
     @Test
     public void testGetRandomCanidate() {
         WordCanidates instance= new WordCanidates( "adfs", testWordList );
         assertTrue( testWordList.contains( instance.getRandomCanidate() ) );
     }
     public void utilTestResults( java.util.Map< String, String[] > expectedResults, 
                                  java.util.Map< String, List< String > > actualResults ){
         System.out.println( "actualResults: " + actualResults );
         System.out.println( "expectedResults: " + expectedResults );
         assertEquals( expectedResults.size(), actualResults.size() );
         int j=0;
         List< String > subList;
         Iterator< String > k= expectedResults.keySet().iterator();
         while( k.hasNext() ){
             String key= k.next();
             String[] eArray= expectedResults.get( key );
             assertNotNull( eArray );
             List< String > aArray= actualResults.get( key );
             assertNotNull( aArray );
             
             assertEquals( eArray.length, aArray.size() );
             int e= 0;
             Iterator< String > a= aArray.iterator();
             while( a.hasNext() ){
                 String expected= eArray[ e++ ];
                 String actual= a.next();
                 System.out.println( "actual: "+actual );
                 System.out.println( "expected: "+expected );
                 assertEquals( expected, actual );
             }
             
         }
     }
     @Test
     public void testsubDivide() {
         System.out.println( "testsubDivide" );
         List< String > wordList= new LinkedList< String >();
         wordList.add( "cant" );
         wordList.add( "went" );
         wordList.add( "capa" );
         java.util.Map< String, String[] > expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "----", new String[]{ "went" } );
         expectedResult.put( "-a--", new String[]{ "cant" } );
         expectedResult.put( "-a-a", new String[]{ "capa" } );
         WordCanidates instance= new WordCanidates( "----", wordList );
         java.util.Map< String, List< String > > mappedLists= instance.subDivide( 'a' );
         
         utilTestResults( expectedResult, mappedLists );
     }
     @Test
     public void testsubDivide2() {
         System.out.println( "testsubDivide2" );
         int[] resultingSizes={ 2, 1 };
         List< String > wordList= new LinkedList< String >();
         wordList.add( "casa" );
         wordList.add( "went" );
         wordList.add( "capa" );
         java.util.Map< String, String[] > expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "----", new String[]{ "went" } );
         expectedResult.put( "-a-a", new String[]{ "casa", "capa" } );
         
         WordCanidates instance= new WordCanidates( "----", wordList );
         java.util.Map< String, List< String > > mappedLists= instance.subDivide( 'a' );
         
         utilTestResults( expectedResult, mappedLists );
     }
     @Test
     public void testsubDivide_Twice() {
         System.out.println( "testsubDivide_Twice" );
         List< String > wordList= new LinkedList< String >();
         wordList.add( "casa" );
         wordList.add( "went" );
         wordList.add( "capa" );
         wordList.add( "cata" );
         java.util.Map< String, String[] > expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "----", new String[]{ "went" } );
         expectedResult.put( "-a-a", new String[]{ "casa", "capa", "cata" } );
         
         System.out.println( "testsubDivide_Twice first subDivide()" );
         
         WordCanidates instance= new WordCanidates( "----", wordList );
         java.util.Map< String, List< String > > mappedLists= instance.subDivide( 'a' );
         
         utilTestResults( expectedResult, mappedLists );
         System.out.println( "testsubDivide_Twice second subDivide()" );
         List< String > subList;
         assertNotNull( (subList= mappedLists.get( "-a-a" ) ) );
         
         System.out.println( "testsubDivide_Twice second subList: " + subList );
         
         expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "-a-a", new String[]{ "casa", "capa" } );
         expectedResult.put( "-ata", new String[]{ "cata" } );
         
         
         WordCanidates instance2= new WordCanidates( "-a-a", subList );
         mappedLists= instance2.subDivide( 't' );
         
         utilTestResults( expectedResult, mappedLists );
         
     }
     @Test
     public void testsubDivide_ThreeTimes() {
         System.out.println( "testsubDivide_ThreeTimes" );
         List< String > wordList= new LinkedList< String >();
         wordList.add( "pasa" );
         wordList.add( "went" );
         wordList.add( "capa" );
         wordList.add( "cata" );
         java.util.Map< String, String[] > expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "----", new String[]{ "went" } );
         expectedResult.put( "-a-a", new String[]{ "pasa", "capa", "cata" } );
         
         System.out.println( "testsubDivide_ThreeTimes first subDivide()" );
         
         WordCanidates instance= new WordCanidates( "----", wordList );
         java.util.Map< String, List< String > > mappedLists= instance.subDivide( 'a' );
         
         utilTestResults( expectedResult, mappedLists );
         
         System.out.println( "testsubDivide_ThreeTimes second subDivide()" );
         List< String > subList;
         assertNotNull( (subList= mappedLists.get( "-a-a" ) ) );
         
         System.out.println( "testsubDivide_ThreeTimes second subList: " + subList );
         
         expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "-a-a", new String[]{ "pasa", "capa" } );
         expectedResult.put( "-ata", new String[]{ "cata" } );
         
         
         WordCanidates instance2= new WordCanidates( "-a-a", subList );
         mappedLists= instance2.subDivide( 't' );
         
         utilTestResults( expectedResult, mappedLists );
         
         System.out.println( "testsubDivide_ThreeTimes third subDivide()" );
         assertNotNull( (subList= mappedLists.get( "-a-a" ) ) );
         
         System.out.println( "testsubDivide_ThreeTimes third subList: " + subList );
         
         expectedResult= new java.util.TreeMap< String, String[] >();
         expectedResult.put( "ca-a", new String[]{ "capa" } );
         expectedResult.put( "-a-a", new String[]{ "pasa" } );
         
         
         WordCanidates instance3= new WordCanidates( "-a-a", subList );
         mappedLists= instance3.subDivide( 'c' );
         
         utilTestResults( expectedResult, mappedLists );
         
     }
 }
