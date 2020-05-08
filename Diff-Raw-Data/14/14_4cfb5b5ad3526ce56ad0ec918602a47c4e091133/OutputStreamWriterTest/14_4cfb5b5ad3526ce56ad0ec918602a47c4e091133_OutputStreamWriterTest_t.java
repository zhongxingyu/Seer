 package ua.in.dmitry404.writers;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayOutputStream;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Dmitriy Butakov
  */
 public class OutputStreamWriterTest {
     OutputStreamWriter streamWriter;
     ByteArrayOutputStream stream;
 
     @Before
     public void setUp() {
         stream = new ByteArrayOutputStream();
         streamWriter = new OutputStreamWriter(stream);
     }
 
     @Test
     public void testSuccessMessage() throws WriterException {
        String expectedMessage = "OK\n";
 
         streamWriter.success();
 
         assertArrayEquals(expectedMessage.getBytes(), stream.toByteArray());
     }
 
     @Test
     public void testErrorMessage() throws WriterException {
        String expectedMessage = "ERROR\n";
 
         streamWriter.error();
 
         assertArrayEquals(expectedMessage.getBytes(), stream.toByteArray());
     }
 
     @Test
     public void testInformationMessage() throws WriterException {
         String informationMessage = "test info string";
 
         streamWriter.info(informationMessage);
 
         assertArrayEquals(informationMessage.getBytes(), stream.toByteArray());
 
     }
 }
