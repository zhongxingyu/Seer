 import org.junit.*;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
 * Created by IntelliJ IDEA.
 * User: 8thlight
 * Date: 2/20/12
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
 public class IntegrationTest {
     Server testServer;
     int portNumber=8083;
 
     @Before
     public void setUp()
     {
         testServer = new Server(portNumber);
         testServer.start();
     }
 
     @After
     public void tearDown(){
         testServer.kill();
 
     }
 
     private Socket getTestSocket()
     throws IOException{
         return new Socket("localhost",portNumber);
     }
 
     private int readSocket(Socket testSocket)
     throws IOException{
         InputStream s=testSocket.getInputStream();
         return s.read();
     }
 
     @Test
     public void responds() throws IOException{
         Socket testSocket=getTestSocket();
         testSocket.getOutputStream().write("abc\n\ndef\ngh\n".getBytes());
         int socketResponse=readSocket(testSocket);
         assertTrue(socketResponse>0);
         testSocket.close();
     }
 
 
     @Test
     public void respondsToTwoRequests()
     throws IOException{
             Socket firstSocket=getTestSocket();
             firstSocket.getOutputStream().write("ijk\n\n".getBytes());
             int firstResponse=readSocket(firstSocket);
             assertTrue(firstResponse>0);
             firstSocket.close();
 
             Socket secondSocket=getTestSocket();
             secondSocket.getOutputStream().write("peace in the middle east\n\n".getBytes());
             int secondResponse=readSocket(secondSocket);
             assertTrue(secondResponse>0);
             secondSocket.close();
     }
     
     @Test
     public void timeoutOnEmptyRequest()
     throws IOException{
         Socket emptyInputSocket=getTestSocket();
         BufferedReader reader=new BufferedReader(new InputStreamReader(emptyInputSocket.getInputStream()));
 
         String status=reader.readLine();
         assertTrue(status.indexOf("408")>0);
         emptyInputSocket.close();
     }
 
     @Test
    public void badRequestResponseWithBadSyntax()
    throws IOException{
        Socket malformedRequestSocket=getTestSocket();
        malformedRequestSocket.getOutputStream().write("HTTP/1.1 whatever i don't care\n\n".getBytes());
        
        BufferedReader reader=new BufferedReader(new InputStreamReader(malformedRequestSocket.getInputStream()));

        String status=reader.readLine();
        assertTrue(status.indexOf("400")>0);
        malformedRequestSocket.close();
    }

    @Test
     public void timeoutOnGarbageRequest()
     throws IOException{
 
         Socket garbageInputSocket=getTestSocket();
         garbageInputSocket.getOutputStream().write("global warming sucks".getBytes());
         BufferedReader reader=new BufferedReader(new InputStreamReader(garbageInputSocket.getInputStream()));
         String status=reader.readLine();
         assertTrue(status.indexOf("408")>0);
         garbageInputSocket.close();
 
     }
 
 }
 
