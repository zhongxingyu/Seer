 package org.yajul.net;
 
 import junit.framework.TestCase;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yajul.io.StreamHelper;
 import org.yajul.util.Copier;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Test SocketListener
  * <br/>
  * Created by IntelliJ IDEA.
  * User: josh
  * Date: 6/28/11
  * Time: 8:09 AM
  */
 public class SocketServerTest extends TestCase {
     private static final Logger log = LoggerFactory.getLogger(SocketServerTest.class);
 
     private ExecutorService executor;
     private int port = 17777;
 
     public SocketServerTest() {
         executor = Executors.newCachedThreadPool();
     }
 
     private class EchoTask extends AbstractClientTask {
         protected EchoTask(ClientConnection connection) {
             super(connection);
         }
 
         public void runClient() throws IOException {
             InputStream in = getConnection().getInputStream();
             BufferedReader inbuf = new BufferedReader(new InputStreamReader(in));
             OutputStream out = getConnection().getOutputStream();
             PrintWriter outbuf = new PrintWriter(new OutputStreamWriter(out));
             //noinspection InfiniteLoopStatement
             for (; ; ) {
                 log.info("Reading...");
                 String line = inbuf.readLine();
                 if (line == null) {
                     log.info("Done.");
                     break;
                 }
                 log.info("line=" + line);
                 outbuf.println("Echo: " + line);
                 outbuf.flush();
             }
         }
     }
 
     public void testServerSocket() throws IOException {
         SocketListener echoServer = new SocketListener(InetAddress.getLocalHost(),
                 port, executor, new SingleClientTaskFactory() {
             @Override
             public ClientTask createClientTask(ClientConnection clientConnection) {
                 return new EchoTask(clientConnection);
             }
         });
         echoServer.start();
 
         Socket client = new Socket(InetAddress.getLocalHost(), port);
         InputStream in = client.getInputStream();
         BufferedReader inbuf = new BufferedReader(new InputStreamReader(in));
         OutputStream out = client.getOutputStream();
         PrintWriter outbuf = new PrintWriter(new OutputStreamWriter(out));
 
         outbuf.println("test 1");
         outbuf.flush();
 
         String line = inbuf.readLine();
         log.info("line=" + line);
         client.close();
 
         echoServer.shutdown();
 
     }
 
     private class OutputTask extends AbstractClientTask implements Copier.Callback {
         private OutputStream outputStream;
 
         private OutputTask(ClientConnection connection,OutputStream outputStream) {
             super(connection);
         }
 
         public void runClient() throws Exception {
             // Copy the stream from the client to the output stream.
             InputStream fromClient = getConnection().getInputStream();
            Copier.copy(fromClient,outputStream,1024,Copier.UNLIMITED,this);
             getConnection().close();
         }
 
         public void startOfStream() {
         }
 
         public boolean beforeWrite(byte[] buf, int length, int total) {
             return true;
         }
 
         public boolean beforeWrite(char[] buf, int length, int total) {
             return true;
         }
 
         public void endOfStream(int total) {
             log.info("endOfStream() : total = " + total);
             StreamHelper.closeNoThrow(outputStream);
         }
     }
 
     private class IncomingTask extends AbstractClientTask {
         private InputStream inputStream;
 
         protected IncomingTask(ClientConnection connection,InputStream inputStream) {
                 super(connection);
             this.inputStream = inputStream;
         }
 
         public void runClient() throws Exception {
             OutputStream toClient = getConnection().getOutputStream();
             if (inputStream != null)
                 Copier.copy(inputStream,toClient,1024,Copier.UNLIMITED,Copier.NO_CALLBACK);
         }
     }
 
     public void testNetcat() throws Exception {
 
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         final InputStream in = null;
 
         SocketListener server = new SocketListener(InetAddress.getLocalHost(),
                 port, executor, new ClientTaskFactory() {
             public List<ClientTask> createClientTasks(ClientConnection clientConnection) {
 
                 List<ClientTask> tasks = new ArrayList<ClientTask>();
                 // client->server stream
                 tasks.add(new OutputTask(clientConnection,out));
                 // server->client stream
                 tasks.add(new IncomingTask(clientConnection,in));
                 return tasks;
             }
         });
 
         server.start();
 
 
         Thread.sleep(1000);
 
         // Make a client that connects and writes some stuff.
         Socket socket = new Socket(InetAddress.getLocalHost(),port);
         OutputStream clientOut = socket.getOutputStream();
         PrintStream ps = new PrintStream(clientOut);
 
        ps.println("hello there!");
         ps.flush();
 
         ps.close();
 
         log.debug("Closing client socket...");
         socket.close();
 
         Thread.sleep(1000);
 
 
         String received = out.toString();
 
         assertEquals("hello there!",received);
 
         server.shutdown();
     }
 }
