 package dk.clausreimer.zmag.wordrank;
 
 import org.junit.Rule;
 import org.junit.Test;
 
 import org.junit.Assert;
 import org.junit.rules.ExpectedException;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 
 
 public class IOUtilTest {
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Test
     public void testRead() {
         InputStreamMock stream;
         byte[] expected;
         byte[] result;
 
         expected = "Hello".getBytes();
         stream = new InputStreamMock(new ByteArrayInputStream(expected));
 
         result = IOUtil.read(stream).getBytes();
 
         Assert.assertArrayEquals(expected, result);
         Assert.assertEquals(true, stream.isClosed());
     }
 
     @Test
     public void testStreamIsClosedOnError() {
         InputStreamMock stream;
         boolean wordRankExceptionThrown;
 
         stream = new InputStreamMock(new ByteArrayInputStream("Hello".getBytes()));
         stream.setRaiseReadException(true);
         wordRankExceptionThrown = false;
 
         try {
             IOUtil.read(stream);
         } catch (WordRankException x) {
             wordRankExceptionThrown = true;
         }
 
         Assert.assertEquals(true, wordRankExceptionThrown);
         Assert.assertEquals(true, stream.isClosed());
     }
 
     @Test
     public void testCloseIsExceptionSafe() {
         InputStreamMock stream;
 
         stream = new InputStreamMock(new ByteArrayInputStream("Hello".getBytes()));
         stream.setRaiseCloseException(true);
 
         IOUtil.read(stream);
 
         Assert.assertEquals(false, stream.isClosed());
     }
 
     @Test
     public void testCloseNullNoExceptionThrown() {
         IOUtil.close(null);
     }
 
     @Test
     public void testReadResource() {
         String text;
 
         text = IOUtil.read("dk/clausreimer/zmag/wordrank/IOUtil.ReadResourceTest.txt");
         Assert.assertEquals("Hello World", text.trim());
     }
 
     @Test
     public void testExceptionThrownWhenResourceNotFound() {
         thrown.expect(WordRankException.class);
        thrown.expectMessage("resource dk/clausreimer/zmag/wordrank/IOUtil.NoFile.txt not found");
 
        IOUtil.read("dk/clausreimer/zmag/wordrank/IOUtil.NoFile.txt");
     }
 
     @Test
     public void testReadFile() {
         File file;
         String text;
 
         file = new File("src/test/resources/dk/clausreimer/zmag/wordrank/IOUtil.ReadFileTest.txt");
         text = IOUtil.read(file);
 
         Assert.assertEquals("Hello World", text.trim());
     }
 
     @Test
     public void testExceptionThrownWhenFileNotFound() {
         thrown.expect(WordRankException.class);
        thrown.expectMessage("C:\\git-hub\\zmag\\src\\test\\resources\\dk\\clausreimer\\zmag\\wordrank\\IOUtil.ReadNoFile.txt");
 
        IOUtil.read(new File("src/test/resources/dk/clausreimer/zmag/wordrank/IOUtil.ReadNoFile.txt"));
     }
 }
