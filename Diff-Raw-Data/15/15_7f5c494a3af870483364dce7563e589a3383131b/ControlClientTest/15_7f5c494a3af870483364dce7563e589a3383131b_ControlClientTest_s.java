 package cpsc471.ftp.client.control;
 
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import static org.mockito.Mockito.*;
 import static org.mockito.Mockito.when;
 
 /**
  * Test for the control client
  */
 @Test
 public class ControlClientTest {
 
     private ControlClientImpl client;
     private Socket socket;
 
     private ByteArrayOutputStream outputStream;
     private ByteArrayInputStream inputStream;
 
     @BeforeMethod
     public void setUp() throws Exception {
 
         // mock the socket
         socket = mock(Socket.class);
 
         // when socket method calls, return test objects
         // this allows for complete control over the internal socket and state
         outputStream = new ByteArrayOutputStream();
         when(socket.getOutputStream()).thenReturn(outputStream);
 
         inputStream = new ByteArrayInputStream("".getBytes());
         when(socket.getInputStream()).thenReturn(inputStream);
 
         client = new ControlClientImpl();
         client.setSocket(socket);
     }
 
     /**
      * Instantiate client with invalid domain name
      */
     @Test(expectedExceptions = UnknownHostException.class)
     public void testInvalidDomainName() throws Exception {
 
         new ControlClientImpl("@%$!", (short)12);
     }
 
     /**
      * Valid domain name
      */
     public void testValidDomainName() throws Exception {
 
         // todo find a better way to test
         // these values are important. This test actually establishes
         // a socket with Google. There must be a better way to do this.
         // new ControlClientImpl("google.com", (short)80);
     }
 
     /**
      * Execute the ls command
      */
     public void testLs() throws Exception {
 
         client.ls();
         // ls should be written to wire
         Assert.assertEquals(outputStream.toByteArray(), "ls\n".getBytes(),
                 "\"ls\" wasn't written to socket");
     }
 
     /**
      * Invoke quit
      */
     public void testQuit() throws Exception {
 
         // should close socket
         client.quit();
 
         verify(socket, times(1)).close();
     }
 
     /**
      * Test basic put, with a file that exists
      */
     public void testPutFileExists() throws Exception {
 
         String fileName = "fileToUpload";
         File mockFile = mock(File.class);
         when(mockFile.exists()).thenReturn(true);
         when(mockFile.getName()).thenReturn(fileName);
 
         client.put(mockFile);
 
         Assert.assertEquals(
                 outputStream.toByteArray(),
                 ("put\n" + fileName + "\n").getBytes(),
                 "\"put\" command improperly written to socket"
         );
     }
 
     /**
      * Attempt to put a file that does not exist
      */
     @Test(expectedExceptions = FileNotFoundException.class)
     public void testPutFileDoesNotExist() throws Exception {
 
         File mockFile = mock(File.class);
         when(mockFile.exists()).thenReturn(false);
 
         client.put(mockFile); // should throw an exception
     }
 
     /**
      * Successful get request
      * @throws Exception
      */
     public void testGetFileExists() throws Exception {
 
         String fileName = "serverFile";
         client.get(fileName);
 
         Assert.assertEquals(
                outputStream.toByteArray(),
                ("get\n" + fileName + "\n").getBytes(),
                 "\"get\" command improperly written to socket"
         );
     }
 
     /**
      * Request a file not found on the server
      * @throws Exception
      */
     @Test(expectedExceptions = FileNotFoundException.class)
     public void testGetFileDoesNotExist() throws Exception {
 
         // fake a "not found" server response
         inputStream = new ByteArrayInputStream("not found".getBytes());
         when(socket.getInputStream()).thenReturn(inputStream);
         client.setSocket(socket);
 
         client.get("serverFile"); // should throw exception
     }
 }
