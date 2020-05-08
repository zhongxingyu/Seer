 package fs;
 
 import org.junit.Test;
 import play.test.UnitTest;
 
 import java.io.File;
 
 public class LocalFileTest extends UnitTest {
     @Test
     public void shouldKnowItsRelativePath() {
         File f = new File("/some/long/complete/path/file.txt");
         LocalFile lf = new LocalFile("/some/long/complete", f);
        assertEquals(lf.getRelativePath(),"path/file.txttttttt");
     }
 
     @Test
     public void shouldReturnComplePathAsFileObjectDoes() {
         File f = new File("/some/long/complete/path/file.txt");
         LocalFile lf = new LocalFile("/some/long/complete", f);
         assertEquals(lf.getPath(), "/some/long/complete/path/file.txt");
     }
 }
