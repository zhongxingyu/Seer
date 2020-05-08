 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
 * Created with IntelliJ IDEA.
 * User: twer
 * Date: 9/13/12
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
 public class LibraryTest {
     private ByteArrayOutputStream outContent;
     private ByteArrayOutputStream errContent;
     private Library library;

    public LibraryTest(){
        library = new Library();
    }
     @Before
     public void setUpStreams() {
         errContent = new ByteArrayOutputStream();
         outContent = new ByteArrayOutputStream() ;
         System.setOut(new PrintStream(outContent));
         System.setErr(new PrintStream(errContent));
     }
     @Test
     public void testShowBookList(){
         library.showBookList();
         assertThat(outContent.toString(), is("Thinking in Java\nPro Git\nC++ Primer\n"));
 
 
     }
     @Test
     public void testBorrowBook() {
      assertThat(library.borrowBook("Thinking in Java"), is("Thank You! Enjoy the book!"));
 
     }
     @After
     public void cleanUpStreams() {
         System.setOut(null);
         System.setErr(null);
     }
 }
