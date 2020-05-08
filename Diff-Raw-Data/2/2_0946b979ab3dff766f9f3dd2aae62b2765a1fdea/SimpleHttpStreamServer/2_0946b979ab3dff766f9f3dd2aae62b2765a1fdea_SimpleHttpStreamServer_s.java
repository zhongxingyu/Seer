 package cryptocast.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
import static cryptocast.util.ByteStringUtils.str2bytes;
 
 public class SimpleHttpStreamServer implements Runnable {
     InputStream in;
     ServerSocket sock;
     private String contentType;
 
     public SimpleHttpStreamServer(InputStream in, SocketAddress addr, String contentType) 
                         throws IOException {
         this.in = in;
         this.contentType = contentType;
         sock = new ServerSocket();
         sock.bind(addr);
     }
 
     @Override
     public void run() {
         for (;;) {
             Socket clientSock;
             try {
                 clientSock = sock.accept();
             } catch (Exception e) {
                 e.printStackTrace();
                 return;
             }
             try {
                 BufferedReader clientIn = new BufferedReader(
                         new InputStreamReader(clientSock.getInputStream()));
                 while (clientIn.readLine().length() > 1) {
                     // fetch all request headers
                 }
                 OutputStream clientOut = clientSock.getOutputStream();
                 clientOut.write(getChunkedResponseHeader());
                 sendChunked(clientOut, in);
                 clientSock.close();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (InterruptedException e) {
                 return;
             }
         }
     }
     
     private void sendChunked(OutputStream out, InputStream in) 
                 throws InterruptedException, IOException {
         int recv;
         byte[] buffer = new byte[0x400000];
         while ((recv = in.read(buffer)) >= 0) {
             if (recv == 0) {
                 Thread.sleep(10); 
                 continue; 
             }
             out.write((Integer.toHexString(recv) + "\r\n").getBytes());
             out.write(buffer, 0, recv);
             out.write("\r\n".getBytes());
             if (Thread.interrupted()) {
                 throw new InterruptedException();
             }
         }
         out.write("0\r\n\r\n".getBytes());
     }
 
     private byte[] getChunkedResponseHeader() {
         return str2bytes(
              "HTTP/1.1 200 OK\r\n" +
              "Transfer-Encoding: chunked\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "Connection: keep-alive\r\n" +
              "\r\n"
         );
     }
 }
