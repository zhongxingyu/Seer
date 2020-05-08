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
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.makeThreadSafe;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 
 import static org.junit.Assert.assertArrayEquals;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 
 import org.junit.Test;
 
 /**
  * <p>
  * Unit tests for the class {@link com.se.devenvy.net.TcpClient TcpClient}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class TcpClientTest
 {
     /**
      * An instance of the class being unit tested.
      */
     private MockTcpClient fTestObject;
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient#dispose() dispose()}.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test
     public void dispose() throws IOException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
 
         // Dictate correct results.
         mockSocket.close();
         replay(mockSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpClient(mockSocket);
 
         // Perform test.
         fTestObject.dispose();
 
         // Verify test results.
         verify(mockSocket);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient.Heartbeat#run() run()}.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      * @throws InterruptedException Thrown if this test is interrupted.
      */
     @Test
     public void heartbeatRun() throws IOException, InterruptedException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
         OutputStream mockOutputStream = createMock(OutputStream.class);
 
         // Initialise test environment.
         fTestObject = new MockTcpClient(mockSocket);
         fTestObject.setHeartbeatInterval(500);
 
         // Dictate correct behaviour.
         expect(mockSocket.isConnected()).andReturn(true).anyTimes();
         expect(mockSocket.isClosed()).andReturn(false).anyTimes();
         expect(mockSocket.getOutputStream()).andReturn(mockOutputStream).anyTimes();
         expect(mockSocket.getRemoteSocketAddress()).andReturn(null).anyTimes();
         mockSocket.close();
         makeThreadSafe(mockSocket, true);
         replay(mockSocket);
 
         // Dictate expected results.
         mockOutputStream.write(fTestObject.getHeartbeatData());
         expectLastCall().times(2);
         replay(mockOutputStream);
 
         // Perform test.
         fTestObject.maintainHeartbeat(true);
         Thread.sleep(1250);
         fTestObject.dispose();
 
         // Verify test results.
         verify(mockOutputStream);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient.Heartbeat#run() run()} with the special condition that the connection has been closed
      * remotely.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      * @throws InterruptedException Thrown if this test is interrupted.
      */
     @Test
     public void heartbeatRunClosedRemotely() throws IOException, InterruptedException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
         OutputStream mockOutputStream = createMock(OutputStream.class);
 
         // Dictate correct behaviour.
         mockOutputStream.write((byte[]) anyObject());
         expectLastCall().andThrow(new IOException());
         expect(mockSocket.isConnected()).andReturn(true).anyTimes();
         expect(mockSocket.isClosed()).andReturn(false).anyTimes();
         expect(mockSocket.getOutputStream()).andReturn(mockOutputStream).anyTimes();
         expect(mockSocket.getRemoteSocketAddress()).andReturn(null).anyTimes();
         replay(mockOutputStream);
 
         // Dictate expected results.
         mockSocket.close();
         makeThreadSafe(mockSocket, true);
         replay(mockSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpClient(mockSocket);
         fTestObject.setHeartbeatInterval(500);
 
         // Perform test.
         fTestObject.maintainHeartbeat(true);
         Thread.sleep(1000);
 
         // Verify test results.
         verify(mockSocket);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient#receiveData() receiveData()}.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test
     public void receiveData() throws IOException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
         InputStream inputStream = new ByteArrayInputStream(new byte[] {'X', 'Y', 'Z'});
 
         // Dictate correct behaviour.
         expect(mockSocket.getInputStream()).andReturn(inputStream).anyTimes();
         expect(mockSocket.isClosed()).andReturn(false);
         expect(mockSocket.isClosed()).andReturn(true);
         replay(mockSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpClient(mockSocket);
 
         // Perform test.
         fTestObject.receiveData();
 
         // Verify test results.
         byte[] expected = new byte[1024];
         expected[0] = 'X';
         expected[1] = 'Y';
         expected[2] = 'Z';
         assertArrayEquals(expected, fTestObject.getReceivedData());
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient#receiveData() receiveData()} with the special condition that the connection has been
      * closed remotely.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test
     public void receiveDataClosedRemotely() throws IOException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
         InputStream mockInputStream = createMock(InputStream.class);
 
         // Dictate correct behaviour.
         expect(mockInputStream.read((byte[]) anyObject())).andReturn(-1).anyTimes();
         expect(mockSocket.getInputStream()).andReturn(mockInputStream).anyTimes();
         expect(mockSocket.getRemoteSocketAddress()).andReturn(null).anyTimes();
         replay(mockInputStream);
 
         // Dictate expected results.
         mockSocket.close();
         replay(mockSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpClient(mockSocket);
 
         // Perform test.
         fTestObject.receiveData();
 
         // Verify test results.
         verify(mockSocket);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient#receiveData() receiveData()} with the special condition that the connection has been
      * closed locally.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test
     public void receiveDataClosedLocally() throws IOException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
         InputStream mockInputStream = createMock(InputStream.class);
 
        // Initialise test environment.
        fTestObject = new MockTcpClient(mockSocket);

         // Dictate correct behaviour.
         expect(mockInputStream.read((byte[]) anyObject())).andThrow(new SocketException("Socket closed")).anyTimes();
         expect(mockSocket.getInputStream()).andReturn(mockInputStream).anyTimes();
         expect(mockSocket.getRemoteSocketAddress()).andReturn(null).anyTimes();
        replay(mockInputStream);
 
        // Dictate expected results.
        mockSocket.close();
        replay(mockSocket);
 
         // Perform test.
         fTestObject.receiveData();
 
         // Verify test results.
         verify(mockSocket);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.devenvy.net.TcpClient#receiveData() receiveData()} with the special condition that a
      * {@link java.net.SocketException SocketException} other than "Socket closed" occurs.
      * </p>
      * 
      * @throws IOException Thrown if an I/O error occurs.
      */
     @Test(expected = SocketException.class)
     public void receiveDataOtherSocketException() throws IOException
     {
         // Create dependencies.
         Socket mockSocket = createMock(Socket.class);
         InputStream mockInputStream = createMock(InputStream.class);
 
         // Dictate correct behaviour.
         expect(mockInputStream.read((byte[]) anyObject())).andThrow(new SocketException("Unknown")).anyTimes();
         expect(mockSocket.getInputStream()).andReturn(mockInputStream).anyTimes();
         replay(mockInputStream);
 
         // Dictate expected results.
         mockSocket.close();
         replay(mockSocket);
 
         // Initialise test environment.
         fTestObject = new MockTcpClient(mockSocket);
 
         // Perform test.
         fTestObject.receiveData();
     }
 }
