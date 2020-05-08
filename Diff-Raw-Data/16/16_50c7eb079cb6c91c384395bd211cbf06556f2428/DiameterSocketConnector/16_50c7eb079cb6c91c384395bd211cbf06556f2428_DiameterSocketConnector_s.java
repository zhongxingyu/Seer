 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.diameter.bio;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.cipango.diameter.AVP;
 import org.cipango.diameter.AVPList;
 import org.cipango.diameter.Dictionary;
 import org.cipango.diameter.Factory;
 import org.cipango.diameter.ResultCode;
 import org.cipango.diameter.base.Common;
 import org.cipango.diameter.io.Codecs;
 import org.cipango.diameter.node.AbstractDiameterConnector;
 import org.cipango.diameter.node.DiameterAnswer;
 import org.cipango.diameter.node.DiameterConnection;
 import org.cipango.diameter.node.DiameterMessage;
 import org.cipango.diameter.node.Peer;
 import org.eclipse.jetty.io.Buffer;
 import org.eclipse.jetty.io.ByteArrayBuffer;
 import org.eclipse.jetty.io.EofException;
 import org.eclipse.jetty.io.bio.SocketEndPoint;
 import org.eclipse.jetty.util.log.Log;
 
 /**
  * TCP Diameter Connector using BIO
  */
 public class DiameterSocketConnector extends AbstractDiameterConnector
 {
 	public static final int DEFAULT_PORT = 3868;
 	
 	private ServerSocket _serverSocket;
 		
 	public void open() throws IOException
 	{
 		if (_serverSocket == null || _serverSocket.isClosed())
 			_serverSocket = newServerSocket(); 
 	}
 	
 	public void close() throws IOException
 	{
 		if (_serverSocket != null)
 			_serverSocket.close();
 		_serverSocket = null;
 	}
 	
 	public Object getTransport()
 	{
 		return _serverSocket;
 	}
 	
 	protected ServerSocket newServerSocket() throws IOException
 	{
 		ServerSocket ss = new ServerSocket();
 		if (getHost() == null)
 			ss.bind(new InetSocketAddress(getPort()));
 		else
 			ss.bind(new InetSocketAddress(getHost(), getPort()));
 		return ss;
 	}
 	
 	public void accept(int acceptorID) throws IOException, InterruptedException
 	{
 		Socket socket = _serverSocket.accept();
 		Connection connection = new Connection(socket);
 		new Thread(connection, "Connection-" + acceptorID).start();
 	}
 	
 	public DiameterConnection getConnection(Peer peer) throws IOException
 	{
 		int port = peer.getPort();
 		if (port == 0)
 			port = DEFAULT_PORT;
 		
 		Socket socket;
 		
 		if (peer.getAddress() != null)
 			socket = new Socket(peer.getAddress(), port, getLocalAddress(), 0);
 		else 
 			socket = new Socket(peer.getHost(), port, getLocalAddress(), 0);
 		
 		Connection connection = new Connection(socket);
 		connection.setPeer(peer);
 		
 		new Thread(connection, "Connection-" + peer.getHost()).start();
 
 		return connection;
 	}
 	
 	public int getLocalPort()
 	{
 		if (_serverSocket == null || _serverSocket.isClosed())
 			return -1;
 		return _serverSocket.getLocalPort();
 	}
 	
 	public InetAddress getLocalAddress()
 	{
 		if (_serverSocket == null || _serverSocket.isClosed())
 			return null;
 		return _serverSocket.getInetAddress();
 	}
 
 	@Override
 	protected int getDefaultPort()
 	{
 		return DEFAULT_PORT;
 	}
 		
 	public class Connection extends SocketEndPoint implements Runnable, DiameterConnection
 	{
 		private Peer _peer;
 		
 		public Connection(Socket socket) throws IOException
 		{
 			super(socket);
 		}
 		
 		public void setPeer(Peer peer)
 		{
 			_peer = peer;
 		}
 		
 		public Peer getPeer()
 		{
 			return _peer;
 		}
 		
 		public void stop()
 		{
 			try { close(); } catch (IOException e) { Log.ignore(e); }
 		}
 		
 		public void write(DiameterMessage message) throws IOException
 		{
 			Buffer buffer = getBuffer(getMessageBufferSize());
 			buffer = Codecs.__message.encode(buffer, message);
 			
 			flush(buffer);
 			returnBuffer(buffer);
 			
 			if (getNode().isStatsOn())
 				_messagesSent.incrementAndGet();
 			
 			if (_listener != null)
 				_listener.messageSent(message, this);
 		}
 		
 		public void run()
 		{
 			try
 			{
 				Buffer fb = new ByteArrayBuffer(4);
 				
 				while (isStarted() && !isClosed())
 				{
 					fb.clear();
 					int read = fill(fb);
 					
 					if (read == -1)
 						throw new EofException();
 					
 					int length = 
 						(fb.peek(1) & 0xff) << 16
 						| (fb.peek(2) & 0xff) << 8
 						| (fb.peek(3) & 0xff);
 					
 					Buffer b = new ByteArrayBuffer(length);
 
 					b.put(fb);
					read = fill(b);

					if (read == -1)
						throw new EofException();
 					
 					DiameterMessage message = Codecs.__message.decode(b);
 					message.setConnection(this);
 					message.setNode(getNode());
 					
 					if (getNode().isStatsOn())
 						_messagesReceived.incrementAndGet();
 					
 					// TODO move the following code at a better place. Need to be done before _listener.messageReceived(message, this);
 					if (!message.isRequest())
 					{
 						int code;
 						int vendorId = Common.IETF_VENDOR_ID;
 						
 						AVP<Integer> avp = message.getAVPs().get(Common.RESULT_CODE);
 						if (avp != null)
 						{
 							code = avp.getValue();
 						}
 						else
 						{
 							AVPList expRc = message.get(Common.EXPERIMENTAL_RESULT);
 							code = expRc.getValue(Common.EXPERIMENTAL_RESULT_CODE);
 							vendorId = expRc.getValue(Common.VENDOR_ID);
 						}
 						
 						ResultCode rc = Dictionary.getInstance().getResultCode(vendorId, code);
 						if (rc == null)
 							rc = Factory.newResultCode(vendorId, code, "Unknown");
 						
 						((DiameterAnswer) message).setResultCode(rc);
 					}
 					
 					if (_listener != null)
 						_listener.messageReceived(message, this);
 					
 					getNode().receive(message);
 				}
 			}
 			catch (EofException e)
 			{
 				Log.debug(e);
 				try { close(); } catch (IOException e2) { Log.ignore(e2); }
 			}
 			catch (IOException e)
 			{
 				Log.debug(e); // TODO
 				try { close(); } catch (IOException e2) { Log.ignore(e2); }
 			}
 			catch (Throwable t)
 			{
 				Log.warn("handle failed", t);
 				try { close(); } catch (IOException e2) { Log.ignore(e2); }
 			}
 			finally
 			{
 				if (_peer != null)
 					_peer.peerDisc(this);
 			}
 		}
 	}
 
 }
