 // Copyright (C) 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.tools.tcpproxy;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Array;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import junit.framework.TestCase;
 import net.grinder.common.Logger;
 import net.grinder.common.LoggerStubFactory;
 import net.grinder.testutility.CallData;
 import net.grinder.util.StreamCopier;
 import net.grinder.util.TerminalColour;
 
 
 /**
  * Unit test case for {@link PortForwarderTCPProxyEngine}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestPortForwarderTCPProxyEngine extends TestCase {
 
   private final MyFilterStubFactory m_requestFilterStubFactory =
     new MyFilterStubFactory();
   private final TCPProxyFilter m_requestFilter =
     m_requestFilterStubFactory.getFilter();
 
   private final MyFilterStubFactory m_responseFilterStubFactory =
     new MyFilterStubFactory();
   private final TCPProxyFilter m_responseFilter =
     m_responseFilterStubFactory.getFilter();
 
   private final LoggerStubFactory m_loggerStubFactory =
     new LoggerStubFactory();
   private final Logger m_logger = m_loggerStubFactory.getLogger();
   private int m_localPort;
 
   protected void setUp() throws Exception {
     final ServerSocket serverSocket = new ServerSocket(0);
     m_localPort = serverSocket.getLocalPort();
     serverSocket.close();
   }
 
   public void testBadLocalPort() throws Exception {
     final ConnectionDetails badConnectionDetails =
       new ConnectionDetails(new EndPoint("from", 111),
                             new EndPoint("unknownhost", 222),
                             false);
 
     try {
       new PortForwarderTCPProxyEngine(m_requestFilter,
                                       m_responseFilter,
                                       m_logger,
                                       badConnectionDetails,
                                       false,
                                       1000);
       fail("Expected UnknownHostException");
     }
     catch (UnknownHostException e) {
     }
   }
 
   public void testTimeOut() throws Exception {
 
     final ConnectionDetails connectionDetails =
       new ConnectionDetails(new EndPoint("localhost", m_localPort),
                             new EndPoint("wherever", 9999),
                             false);
 
     final TCPProxyEngine engine =
       new PortForwarderTCPProxyEngine(m_requestFilter,
                                       m_responseFilter,
                                       m_logger,
                                       connectionDetails,
                                       false,
                                       10);
 
     m_loggerStubFactory.resetCallHistory();
 
     // If this ends up spinning its probably because
     // some other test has not terminated all of its filter
     // threads correctly.
     engine.run();
 
     final CallData callData = m_loggerStubFactory.getCallData();
     assertEquals("error", callData.getMethodName());
     final Object[] parameters = callData.getParameters();
     assertEquals(1, parameters.length);
     assertEquals("Listen time out", parameters[0]);
 
     m_loggerStubFactory.assertNoMoreCalls();
   }
 
   private void engineTests(AbstractTCPProxyEngine engine) throws Exception {
 
     final Thread engineThread = new Thread(engine, "Run engine");
     engineThread.start();
 
     final Socket clientSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientSocket.getOutputStream(), true);
 
     final String message =
       "This is some stuff\r\nWe expect to be echoed.\u00ff\u00fe";
     clientWriter.print(message);
     clientWriter.flush();
 
     final InputStream clientInputStream = clientSocket.getInputStream();
 
     while (clientInputStream.available() <= 0) {
       Thread.sleep(10);
     }
 
     final ByteArrayOutputStream response = new ByteArrayOutputStream();
 
     // Don't use a StreamCopier because it will block reading the
     // input stream.
     final byte[] buffer = new byte[100];
 
     while (clientInputStream.available() > 0) {
       final int bytesRead = clientInputStream.read(buffer, 0, buffer.length);
       response.write(buffer, 0, bytesRead);
     }
 
     assertEquals(message, response.toString());
 
     clientSocket.close();
 
    // TODO - resolve race.
    Thread.sleep(100);

     engine.stop();
     engineThread.join();
 
     m_requestFilterStubFactory.assertSuccess("connectionOpened",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertSuccess("connectionClosed",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertSuccess("stop");
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.assertSuccess("connectionOpened",
                                              ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_responseFilterStubFactory.assertSuccess("connectionClosed",
                                              ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess("stop");
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertNoMoreCalls();
 
   }
 
   public void testEngine() throws Exception {
 
     final AcceptSingleConnectionAndEcho echoer =
       new AcceptSingleConnectionAndEcho();
 
     final EndPoint localEndPoint = new EndPoint("localhost", m_localPort);
 
     final ConnectionDetails connectionDetails =
       new ConnectionDetails(localEndPoint,
                             echoer.getEndPoint(),
                             false);
 
     // Set the filters not to randomly generate output.
     m_requestFilterStubFactory.setResult(null);
     m_responseFilterStubFactory.setResult(null);
 
     final AbstractTCPProxyEngine engine =
       new PortForwarderTCPProxyEngine(m_requestFilter,
                                       m_responseFilter,
                                       m_logger,
                                       connectionDetails,
                                       false,
                                       100000);
 
     m_responseFilterStubFactory.assertNoMoreCalls();
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     assertEquals(localEndPoint, engine.getListenEndPoint());
     assertNotNull(engine.getSocketFactory());
     assertEquals(m_requestFilter, engine.getRequestFilter());
     assertEquals(m_responseFilter, engine.getResponseFilter());
     assertEquals("", engine.getRequestColour());
     assertEquals("", engine.getResponseColour());
 
     m_loggerStubFactory.resetCallHistory();
     m_requestFilterStubFactory.resetCallHistory();
     m_responseFilterStubFactory.resetCallHistory();
 
     engineTests(engine);
   }
 
   public void testColourEngine() throws Exception {
 
     final AcceptSingleConnectionAndEcho echoer =
       new AcceptSingleConnectionAndEcho();
 
     final EndPoint localEndPoint = new EndPoint("localhost", m_localPort);
 
     final ConnectionDetails connectionDetails =
       new ConnectionDetails(localEndPoint,
                             echoer.getEndPoint(),
                             false);
 
     // Set the filters not to randomly generate output.
     m_requestFilterStubFactory.setResult(null);
     m_responseFilterStubFactory.setResult(null);
 
     final AbstractTCPProxyEngine engine =
       new PortForwarderTCPProxyEngine(m_requestFilter,
                                       m_responseFilter,
                                       m_logger,
                                       connectionDetails,
                                       true,
                                       100000);
 
     m_responseFilterStubFactory.assertNoMoreCalls();
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     assertEquals(localEndPoint, engine.getListenEndPoint());
     assertNotNull(engine.getSocketFactory());
     assertEquals(m_requestFilter, engine.getRequestFilter());
     assertEquals(m_responseFilter, engine.getResponseFilter());
     assertEquals(TerminalColour.RED, engine.getRequestColour());
     assertEquals(TerminalColour.BLUE, engine.getResponseColour());
 
     m_loggerStubFactory.resetCallHistory();
     m_requestFilterStubFactory.resetCallHistory();
     m_responseFilterStubFactory.resetCallHistory();
 
     engineTests(engine);
   }
 
   private final class AcceptSingleConnectionAndEcho implements Runnable {
     private final ServerSocket m_serverSocket;
 
     public AcceptSingleConnectionAndEcho() throws IOException {
       m_serverSocket = new ServerSocket(0);
       new Thread(this, getClass().getName()).start();
     }
 
     public EndPoint getEndPoint() {
       return EndPoint.serverEndPoint(m_serverSocket);
     }
 
     public void run() {
       try {
         final Socket socket = m_serverSocket.accept();
 
         new StreamCopier(1000, true).copy(socket.getInputStream(),
                                           socket.getOutputStream());
       }
       catch (IOException e) {
         fail("Got a " + e.getMessage());
       }
     }
   }
 }
