package dbc.opensearch.components.tools.tests;
 /** \brief UnitTest for FileFilter */
 
 import static org.junit.Assert.*;
 import org.junit.*;
 
 import dbc.opensearch.components.datadock.FileFilter;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  *
  */
 public class FileFilterTest {
 
     FileFilter ff;
     static String dir = ".shouldnotbeaccepted"; 
     static File dummy = null;
 
     /**
      * Before each test we construct a dummy directory path and a
      * clean FileFilter instance
      */
     @Before public void SetUp() {
         dummy = new File( dir );
         dummy.mkdir();
         ff = new FileFilter();
     }
 
     /**
      * After each test the dummy directory is removed
      */
     @After public void TearDown() {
         try{
             (new File( dir )).delete();
         }catch( Exception e ){}
     }
 
     /**
      * Files or dirs beginning with a '.' should not be accepted
      */
     @Test public void testDotFileOrDirNotAccepted() {
         assertFalse( ff.accept( dummy, dir ) );
     }
 
     /**
      * Files not beginning with a '.' should be accepted
      */
     @Test public void testNonDotFileOrDirAccepted(){
         assertTrue( ff.accept( dummy, "arbitraryname" ) );
     }
 
     /**
      * directories should not be accepted
      */
     @Test public void testDirsNotAccepted()
     {
         new File( dummy, dir).mkdir();
         assertFalse( ff.accept( dummy, dir ) );
     }
 
     /**
      * if dir- or filename is null, java.io.File must throw
      */
     @Test(expected = NullPointerException.class) 
     public void testNullValueForFilenameShouldFail(){
         assertFalse( ff.accept( new File( "idontexist" ), null ) );
     }
 
 }
