 /*
     This file is part of Dev Envy.
 
     Dev Envy is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     Dev Envy is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with Dev Envy. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.devenvy.test.net;
 
 import static org.easymock.EasyMock.anyObject;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.makeThreadSafe;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import org.junit.Test;
 
 import com.se.devenvy.net.TcpServer;
 import com.se.devenvy.test.mocks.SleepingMockTcpServer;
 
 /**
  * <p>
  * Unit tests for the class {@link com.se.devenvy.net.TcpServer TcpServer}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class TcpServerTest
 {
     /**
      * An instance of the class being unit tested.
      */
     private TcpServer fTestObject;
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpServer#dispose() dispose()}.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      * @throws InterruptedException Thrown if this thread is interrupted while waiting for the <code>TcpServer</code> to be disposed.
      */
     @Test
     public void dispose() throws IOException, InterruptedException
     {
         // Create dependencies.
         ServerSocket mockServerSocket = createMock(ServerSocket.class);
 
         // Dictate correct results.
         mockServerSocket.close();
         replay(mockServerSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpServer(mockServerSocket);
 
         // Perform test.
         fTestObject.dispose();
 
         // Verify test results.
         verify(mockServerSocket);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpServer#run() run()}.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      * 
      * @throws InterruptedException Thrown if this test is interrupted.
      */
     @Test
     public void run() throws IOException, InterruptedException
     {
         // Create dependencies.
         ServerSocket mockServerSocket = createMock(ServerSocket.class);
         Socket mockSocket = createMock(Socket.class);
         InputStream mockInputStream = createMock(InputStream.class);
 
         // Dictate correct behaviour.
         expect(mockServerSocket.isClosed()).andReturn(false);
         expect(mockServerSocket.isClosed()).andReturn(true);
         expect(mockServerSocket.accept()).andReturn(mockSocket).anyTimes();
         expect(mockSocket.getRemoteSocketAddress()).andReturn(null).anyTimes();
         expect(mockSocket.getInputStream()).andReturn(mockInputStream).anyTimes();
         mockServerSocket.close();
         replay(mockServerSocket);
 
         // Dictate expected results.
         expect(mockSocket.isConnected()).andReturn(true);
         expect(mockSocket.isClosed()).andReturn(false);
         expect(mockInputStream.read((byte[]) anyObject())).andReturn(0);
         expect(mockSocket.isConnected()).andReturn(false);
         makeThreadSafe(mockSocket, true);
         replay(mockSocket, mockInputStream);
 
         // Initialise test environment.
         fTestObject = new MockTcpServer(mockServerSocket);
 
         // Perform test.
         fTestObject.run();
         Thread.sleep(500);
         fTestObject.dispose();
 
         // Verify test results.
         verify(mockSocket);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpServer#run() run()} with the special condition that an error occurred during an attempt to
      * accept a new connection.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test
     public void runAcceptFailure() throws IOException
     {
         // Create dependencies.
         ServerSocket mockServerSocket = createMock(ServerSocket.class);
 
         // Dictate correct behaviour.
         expect(mockServerSocket.isClosed()).andReturn(false);
         expect(mockServerSocket.accept()).andThrow(new SocketException(""));
 
         // Dictate expected results.
         mockServerSocket.close();
         expect(mockServerSocket.isClosed()).andReturn(true);
         replay(mockServerSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpServer(mockServerSocket);
 
         // Perform test.
         fTestObject.run();
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpServer#run() run()} with the special condition that the {@link java.net.ServeSocket
      * ServerSocket} closed during an attempt to accept a new connection.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test
     public void runServerSocketClosedDuringAccept() throws IOException
     {
         // Create dependencies.
         ServerSocket mockServerSocket = createMock(ServerSocket.class);
 
         // Dictate correct behaviour.
         expect(mockServerSocket.isClosed()).andReturn(false);
         expect(mockServerSocket.accept()).andThrow(new SocketException("Socket closed"));
         expect(mockServerSocket.isClosed()).andReturn(true);
         replay(mockServerSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpServer(mockServerSocket);
 
         // Perform test.
         fTestObject.run();
     }
 }
