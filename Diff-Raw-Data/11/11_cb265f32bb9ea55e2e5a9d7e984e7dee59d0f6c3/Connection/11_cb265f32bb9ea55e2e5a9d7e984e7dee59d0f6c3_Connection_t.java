 package hydna;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.UnresolvedAddressException;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  *  This class is used internally by the Channel class.
  *  A user of the library should not create an instance of this class.
  */
 public class Connection implements Runnable {
 	private static final int MAX_REDIRECT_ATTEMPTS = 5;
 	private static Map<String, Connection> m_availableConnections = new HashMap<String, Connection>();
 	private static Lock m_connectionMutex = new ReentrantLock();
 	
 	private Lock m_channelRefMutex = new ReentrantLock();
 	private Lock m_destroyingMutex = new ReentrantLock();
 	private Lock m_closingMutex = new ReentrantLock();
 	private Lock m_openChannelsMutex = new ReentrantLock();
 	private Lock m_openWaitMutex = new ReentrantLock();
 	private Lock m_pendingMutex = new ReentrantLock();
 	private Lock m_listeningMutex = new ReentrantLock();
 	
 	private boolean m_connecting = false;
 	private boolean m_connected = false;
 	private boolean m_handshaked = false;
 	private boolean m_destroying = false;
 	private boolean m_closing = false;
 	private boolean m_listening = false;
 	
 	private String m_host;
 	private short m_port;
 	private String m_auth = "";
 	private int m_attempt = 0;
 	
 	private SocketChannel m_socketChannel;
 	private Socket m_socket;
 	private DataOutputStream m_outStream;
 	private BufferedReader m_inStreamReader;
 	
 	private Map<Integer, OpenRequest> m_pendingOpenRequests = new HashMap<Integer, OpenRequest>();
 	private Map<Integer, Channel> m_openChannels = new HashMap<Integer, Channel>();
 	private Map<Integer, Queue<OpenRequest>> m_openWaitQueue = new HashMap<Integer, Queue<OpenRequest>>();
 	
 	private int m_channelRefCount = 0;
 	
 	private Thread m_listeningThread;
 	
 	public static boolean m_followRedirects = true;
 	
 	/**
      *  Return an available connection or create a new one.
      *
      *  @param host The host associated with the connection.
      *  @param port The port associated with the connection.
      *  @return The connection.
      */
 	public static Connection getConnection(String host, short port, String auth) {
 		Connection connection;
         String ports = Short.toString(port);
         String key = host + ports + auth;
       
         m_connectionMutex.lock();
         if (m_availableConnections.containsKey(key)) {
             connection = m_availableConnections.get(key);
         } else {
             connection = new Connection(host, port, auth);
             m_availableConnections.put(key, connection);
         }
         m_connectionMutex.unlock();
 
         return connection;
 	}
 	
 	/**
      *  Initializes a new Channel instance.
      *
      *  @param host The host the connection should connect to.
      *  @param port The port the connection should connect to.
      */
 	public Connection(String host, short port, String auth) {
 		m_host = host;
 		m_port = port;
 		m_auth = auth;
 	}
 	
 	/**
      *  Returns the handshake status of the connection.
      *
      *  @return True if the connection has handshaked.
      */
 	public boolean hasHandShaked() {
 		return m_handshaked;
 	}
 	
 	/**
      * Method to keep track of the number of channels that is associated 
      * with this connection instance.
      */
 	public void allocChannel() {
 		m_channelRefMutex.lock();
         m_channelRefCount++;
         m_channelRefMutex.unlock();
         
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Allocating a new channel, channel ref count is " + m_channelRefCount);
         }
 	}
 	
 	/**
      *  Decrease the reference count.
      *
      *  @param addr The channel to dealloc.
      */
 	public void deallocChannel(int ch) {
 		if (HydnaDebug.HYDNADEBUG) {
 			DebugHelper.debugPrint("Connection", ch, "Deallocating a channel");
 		}
 		
         m_destroyingMutex.lock();
         m_closingMutex.lock();
         if (!m_destroying && !m_closing) {
             m_closingMutex.unlock();
             m_destroyingMutex.unlock();
 
             m_openChannelsMutex.lock();
             m_openChannels.remove(ch);
             
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", ch, "Size of openSteams is now " + m_openChannels.size());
         	}
             m_openChannelsMutex.unlock();
         } else  {
             m_closingMutex.unlock();
             m_destroyingMutex.unlock();
         }
       
         m_channelRefMutex.lock();
         --m_channelRefCount;
         m_channelRefMutex.unlock();
 
         checkRefCount();
 	}
 	
 	/**
      *  Check if there are any more references to the connection.
      */
 	private void checkRefCount() {
 		m_channelRefMutex.lock();
         if (m_channelRefCount == 0) {
             m_channelRefMutex.unlock();
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", 0, "No more refs, destroy connection");
             }
             
             m_destroyingMutex.lock();
             m_closingMutex.lock();
             if (!m_destroying && !m_closing) {
                 m_closingMutex.unlock();
                 m_destroyingMutex.unlock();
                 destroy(new ChannelError("", 0x0));
             } else {
                 m_closingMutex.unlock();
                 m_destroyingMutex.unlock();
             }
         } else {
             m_channelRefMutex.unlock();
         }
 	}
 	
 	/**
      *  Request to open a channel.
      *
      *  @param request The request to open the channel.
      *  @return True if request went well, else false.
      */
 	public boolean requestOpen(OpenRequest request) {
 		int chcomp = request.getChannelId();
         Queue<OpenRequest> queue;
 
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", chcomp, "A channel is trying to send a new open request");
         }
 
         m_openChannelsMutex.lock();
         if (m_openChannels.containsKey(chcomp)) {
             m_openChannelsMutex.unlock();
             
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", chcomp, "The channel was already open, cancel the open request");
             }
             
             return false;
         }
         m_openChannelsMutex.unlock();
 
         m_pendingMutex.lock();
         if (m_pendingOpenRequests.containsKey(chcomp)) {
             m_pendingMutex.unlock();
 
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", chcomp, "A open request is waiting to be sent, queue up the new open request");
             }
             
             m_openWaitMutex.lock();
             queue = m_openWaitQueue.get(chcomp);
         
             if (queue == null) {
             	queue = new LinkedList<OpenRequest>();
                 m_openWaitQueue.put(chcomp, queue);
             } 
         
             queue.add(request);
             m_openWaitMutex.unlock();
         } else if (!m_handshaked) {
         	if (HydnaDebug.HYDNADEBUG) {
         		DebugHelper.debugPrint("Connection", chcomp, "No connection, queue up the new open request");
         	}
             
         	m_pendingOpenRequests.put(chcomp, request);
             m_pendingMutex.unlock();
             
             if (!m_connecting) {
                 m_connecting = true;
                 connectConnection(m_host, m_port, m_auth);
             }
         } else {
         	m_pendingOpenRequests.put(chcomp, request);
             m_pendingMutex.unlock();
 
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", chcomp, "Already connected, sending the new open request");
             }
 
             writeBytes(request.getFrame());
             request.setSent(true);
         }
       
         return m_connected;
 	}
 	
 	/**
      *  Try to cancel an open request. Returns true on success else
      *  false.
      *
      *  @param request The request to cancel.
      *  @return True if the request was canceled.
      */
 	public boolean cancelOpen(OpenRequest request) {
 		int channelcomp = request.getChannelId();
         Queue<OpenRequest> queue;
         Queue<OpenRequest> tmp = new LinkedList<OpenRequest>();
         boolean found = false;
       
         if (request.isSent()) {
             return false;
         }
       
         m_openWaitMutex.lock();
         queue = m_openWaitQueue.get(channelcomp);
       
         m_pendingMutex.lock();
         if (m_pendingOpenRequests.containsKey(channelcomp)) {
             m_pendingOpenRequests.remove(channelcomp);
         
             if (queue != null && queue.size() > 0)  {
                 m_pendingOpenRequests.put(channelcomp, queue.poll());
             }
 
             m_pendingMutex.unlock();
             m_openWaitMutex.unlock();
             return true;
         }
         m_pendingMutex.unlock();
       
         // Should not happen...
         if (queue == null) {
             m_openWaitMutex.unlock();
             return false;
         }
       
         while (!queue.isEmpty() && !found) {
             OpenRequest r = queue.poll();
             
             if (r == request) {
                 found = true;
             } else {
                 tmp.add(r);
             }
         }
 
         while(!tmp.isEmpty()) {
             OpenRequest r = tmp.poll();
             queue.add(r);
         }
         m_openWaitMutex.unlock();
       
         return found;
 	}
 	
 	/**
      *  Connect the connection.
      *
      *  @param host The host to connect to.
      *  @param port The port to connect to.
      */
 	private void connectConnection(String host, int port, String auth) {
 		++m_attempt;
 		
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Connecting, attempt " + m_attempt);
         }
         
         try {
         	SocketAddress address = new InetSocketAddress(host, port);
             m_socketChannel = SocketChannel.open(address);
             m_socket = m_socketChannel.socket();
             
         	try {
         		m_socket.setTcpNoDelay(true);
         	} catch (SocketException e) {
             	System.err.println("WARNING: Could not set TCP_NODELAY");
             }
         	
         	m_outStream = new DataOutputStream(m_socket.getOutputStream());
         	m_inStreamReader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
         	
         	if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", 0, "Connected, sending HTTP upgrade request");
             }
         	
         	m_connected = true;
         	
         	connectHandler(auth);
         } catch (UnresolvedAddressException e) {
         	destroy(new ChannelError("The host \"" + host + "\" could not be resolved"));
         } catch (IOException e) {
         	destroy(new ChannelError("Could not connect to the host \"" + host + "\" on the port " + port));
         }
 	}
 	
 	/**
      *  Send HTTP upgrade request.
      */
 	private void connectHandler(String auth) {
         boolean success = false;
 
     	try {
            m_outStream.writeBytes("GET /" + auth + " HTTP/1.1\r\n" +
            					   "Connection: upgrade\r\n" +
            					   "Upgrade: winksock/1\r\n" +
            					   "Host: " + m_host + "\r\n" +
             					   "X-Follow-Redirects: ");
             
             if (m_followRedirects) {
             	m_outStream.writeBytes("yes");
             } else {
             	m_outStream.writeBytes("no");
             }
             
            m_outStream.writeBytes("\r\n\r\n");
             success = true;
     	} catch (IOException e) {
     		success = false;
     	}
 
         if (!success) {
             destroy(new ChannelError("Could not send upgrade request"));
         } else {
             handshakeHandler();
         }
 	}
 	
 	/**
      *  Handle the Handshake response frame.
      */
 	private void handshakeHandler() {
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Incoming upgrade response");
         }
         
         boolean fieldsLeft = true;
         boolean gotResponse = false;
         boolean gotRedirect = false;
         String location = "";
         
         while (fieldsLeft) {
         	String line;
         	
         	try {
         		line = m_inStreamReader.readLine();
         		if (line.length() == 0) {
         			fieldsLeft = false;
         		}
             } catch (IOException e) {
             	destroy(new ChannelError("Server responded with bad handshake"));
                 return;
             }
         	
         	if (fieldsLeft) {
         		// First line i a response, all others are fields
         		if (!gotResponse) {
         			int code = 0;
         			int pos1, pos2;
         			
         			// Take the response code from "HTTP/1.1 101 Switching Protocols"
         			pos1 = line.indexOf(" ");
         			if (pos1 != -1) {
         				pos2 = line.indexOf(" ", pos1 + 1);
         				
         				if (pos2 != -1) {
         					try {
         		            	code = Integer.parseInt(line.substring(pos1 + 1, pos2));
         		            } catch (NumberFormatException e) {
         		            	destroy(new ChannelError("Could not read the status from the response \"" + line + "\""));
         		            }
         				}
         			}
         			
         			switch (code) {
         			case 101:
         				// Everything is ok, continue.
         				break;
         			case 300:
         			case 301:
         			case 302:
         			case 303:
         			case 304:
         				if (!m_followRedirects) {
         					destroy(new ChannelError("Bad handshake (HTTP-redirection disabled)"));
         					return;
         				}
         				
         				if (m_attempt > MAX_REDIRECT_ATTEMPTS) {
         					destroy(new ChannelError("Bad handshake (Too manu redirect attemps)"));
         					return;
         				}
         				
         				gotRedirect = true;
         				break;
         			default:
         				destroy(new ChannelError("Server responded with bad HTTP response code, " + code));
         			}
         			
         			gotResponse = true;
         		} else {
         			line = line.toLowerCase();
         			int pos;
 
         			if (gotRedirect) {
         				pos = line.indexOf("location: ");
         				if (pos != -1) {
         					location = line.substring(10);
         				}
         			} else {
         				pos = line.indexOf("upgrade: ");
 		    			if (pos != -1) {
 		    				String header = line.substring(9);
 		    				if (!header.equals("winksock/1")) {
 		    					destroy(new ChannelError("Bad protocol version: " + header));
 		    					return;
 		    				}
 		    			}
         			}
         		}
         	}
         }
 
         if (gotRedirect) {
         	m_connected = false;
         	
         	if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", 0, "Redirected to location: " + location);
             }
         	
         	URL url = URL.parse(location);
         	
         	if (!url.getProtocol().equals("http")) {
         		if (!url.getProtocol().equals("https")) {
         			destroy(new ChannelError("The protocol HTTPS is not supported"));
         		} else {
         			destroy(new ChannelError("Unknown protocol, " + url.getProtocol()));
         		}
         	}
         	
         	if (!url.getError().equals("")) {
         		destroy(new ChannelError(url.getError()));
         	}
         	
         	connectConnection(url.getHost(), url.getPort(), url.getPath());
         	return;
         }
         
         m_handshaked = true;
         m_connecting = false;
 
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Handshake done on connection");
         }
 
         for (OpenRequest request : m_pendingOpenRequests.values()) {
             writeBytes(request.getFrame());
 
             if (m_connected) {
                 request.setSent(true);
                 if (HydnaDebug.HYDNADEBUG) {
                 	DebugHelper.debugPrint("Connection", request.getChannelId(), "Open request sent");
                 }
             } else {
                 return;
             }
         }
 
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Creating a new thread for frame listening");
         }
 
         try {
         	m_listeningThread = new Thread(this);
         	m_listeningThread.start();
         } catch (IllegalThreadStateException e) {
             destroy(new ChannelError("Could not create a new thread for frame listening"));
             return;
         }
 	}
 	
 	/**
      * The method that is called in the new thread.
      * Listens for incoming frames.
      */
 	public void run() {
 		receiveHandler();
 	}
 	
 	/**
      *  Handles all incoming data.
      */
 	public void receiveHandler() {
 		int size;
         int headerSize = Frame.HEADER_SIZE;
         int ch;
         int op;
         int flag;
 
         ByteBuffer header = ByteBuffer.allocate(headerSize);
         header.order(ByteOrder.BIG_ENDIAN);
         ByteBuffer payload;
 
         int offset = 0;
         int n = 1;
 
         m_listeningMutex.lock();
         m_listening = true;
         m_listeningMutex.unlock();
 
         for (;;) {
             try {
             	while(offset < headerSize && n >= 0) {
             		n = m_socketChannel.read(header);
                     offset += n;
                 }
             } catch (Exception e) {
             	n = -1;
             }
 
             if (n <= 0) {
                 m_listeningMutex.lock();
                 if (m_listening) {
                     m_listeningMutex.unlock();
                     destroy(new ChannelError("Could not read from the connection"));
                 } else {
                 	m_listeningMutex.unlock();
                 }
                 break;
             }
             
             header.flip();
 
             size = (int)header.getShort() & 0xFFFF;
             payload = ByteBuffer.allocate(size - headerSize);
             payload.order(ByteOrder.BIG_ENDIAN);
 
             try {
             	while(offset < size && n >= 0) {
             		n = m_socketChannel.read(payload);
                     offset += n;
                 }
             } catch (Exception e) {
             	n = -1;
             }
 
             if (n <= 0) {
                 m_listeningMutex.lock();
                 if (m_listening) {
                     m_listeningMutex.unlock();
                     destroy(new ChannelError("Could not read from the connection"));
                 } else {
                 	m_listeningMutex.unlock();
                 }
                 break;
             }
 
             payload.flip();
             
             ch = header.getInt();
             byte of = header.get();
             op   = of >> 3 & 3;
             flag = of & 7;
 
             switch (op) {
 
                 case Frame.OPEN:
                 	if (HydnaDebug.HYDNADEBUG) {
                 		DebugHelper.debugPrint("Connection", ch, "Received open response");
                 	}
                     processOpenFrame(ch, flag, payload);
                     break;
 
                 case Frame.DATA:
                 	if (HydnaDebug.HYDNADEBUG) {
                 		DebugHelper.debugPrint("Connection", ch, "Received data");
                 	}
                     processDataFrame(ch, flag, payload);
                     break;
 
                 case Frame.SIGNAL:
                 	if (HydnaDebug.HYDNADEBUG) {
                 		DebugHelper.debugPrint("Connection", ch, "Received signal");
                 	}
                     processSignalFrame(ch, flag, payload);
                     break;
             }
 
             offset = 0;
             n = 1;
             header.clear();
         }
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Listening thread exited");
         }
 	}
 	
 	/**
      *  Process an open frame.
      *
      *  @param addr The address that should receive the open frame.
      *  @param errcode The error code of the open frame.
      *  @param payload The content of the open frame.
      */
 	private void processOpenFrame(int ch, int errcode, ByteBuffer payload) {
 		OpenRequest request;
         Channel channel;
         int respch = 0;
         String message = "";
         
         m_pendingMutex.lock();
         request = m_pendingOpenRequests.get(ch);
         m_pendingMutex.unlock();
 
         if (request == null) {
             destroy(new ChannelError("The server sent a invalid open frame"));
             return;
         }
 
         channel = request.getChannel();
 
         if (errcode == Frame.OPEN_ALLOW) {
             respch = ch;
             
             if (payload != null) {
             	Charset charset = Charset.forName("US-ASCII");
             	CharsetDecoder decoder = charset.newDecoder();
             	
                 try {
 					message = decoder.decode(payload).toString();
 				} catch (CharacterCodingException e) {}
             }
         } else if (errcode == Frame.OPEN_REDIRECT) {
             if (payload == null || payload.capacity() < 4) {
                 destroy(new ChannelError("Expected redirect channel from the server"));
                 return;
             }
 
             respch = payload.getInt();
 
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection",     ch, "Redirected from " + ch);
             	DebugHelper.debugPrint("Connection", respch, "             to " + respch);
             }
             
             if (payload != null && payload.capacity() > 4) {
             	Charset charset = Charset.forName("US-ASCII");
             	CharsetDecoder decoder = charset.newDecoder();
             	
                 try {
                 	payload.position(4);
 					message = decoder.decode(payload).toString();
 				} catch (CharacterCodingException e) {}
             }
         } else {
             m_pendingMutex.lock();
             m_pendingOpenRequests.remove(ch);
             m_pendingMutex.unlock();
 
             String m = "";
             if (payload != null && payload.capacity() > 0) {
             	Charset charset = Charset.forName("US-ASCII");
             	CharsetDecoder decoder = charset.newDecoder();
                 try {
 					m = decoder.decode(payload).toString();
 				} catch (CharacterCodingException e) {}
             }
 
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", ch, "The server rejected the open request, errorcode " + errcode);
             }
 
             ChannelError error = ChannelError.fromOpenError(errcode, m);
             channel.destroy(error);
             return;
         }
 
         m_openChannelsMutex.lock();
         if (m_openChannels.containsKey(respch)) {
             m_openChannelsMutex.unlock();
             destroy(new ChannelError("Server redirected to open channel"));
             return;
         }
 
         m_openChannels.put(respch, channel);
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", respch, "A new channel was added");
         	DebugHelper.debugPrint("Connection", respch, "The size of openChannels is now " + m_openChannels.size());
         }
         m_openChannelsMutex.unlock();
 
         channel.openSuccess(respch, message);
 
         m_openWaitMutex.lock();
         m_pendingMutex.lock();
         if (m_openWaitQueue.containsKey(ch)) {
             Queue<OpenRequest> queue = m_openWaitQueue.get(ch);
             
             if (queue != null)
             {
                 // Destroy all pending request IF response wasn't a 
                 // redirected channel.
                 if (respch == ch) {
                     m_pendingOpenRequests.remove(ch);
 
                     ChannelError error = new ChannelError("Channel already open");
 
                     while (!queue.isEmpty()) {
                         request = queue.poll();
                         request.getChannel().destroy(error);
                     }
 
                     return;
                 }
 
                 request = queue.poll();
                 m_pendingOpenRequests.put(ch, request);
 
                 if (queue.isEmpty()) {
                     m_openWaitQueue.remove(ch);
                 }
 
                 writeBytes(request.getFrame());
                 request.setSent(true);
             }
         } else {
             m_pendingOpenRequests.remove(ch);
         }
         m_pendingMutex.unlock();
         m_openWaitMutex.unlock();
 	}
 	
 	/**
      *  Process a data frame.
      *
      *  @param addr The address that should receive the data.
      *  @param priority The priority of the data.
      *  @param payload The content of the data.
      */
 	private void processDataFrame(int ch, int priority, ByteBuffer payload) {
 		Channel channel = null;
         ChannelData data;
         
         m_openChannelsMutex.lock();
         if (m_openChannels.containsKey(ch))
             channel = m_openChannels.get(ch);
         m_openChannelsMutex.unlock();
 
         if (channel == null) {
             destroy(new ChannelError("No channel was available to take care of the data received"));
             return;
         }
 
         if (payload == null || payload.capacity() == 0) {
             destroy(new ChannelError("Zero data frame received"));
             return;
         }
 
         data = new ChannelData(priority, payload);
         channel.addData(data);
 	}
 	
 	/**
      *  Process a signal frame.
      *
      *  @param channel The channel that should receive the signal.
      *  @param flag The flag of the signal.
      *  @param payload The content of the signal.
      *  @return False is something went wrong.
      */
 	private boolean processSignalFrame(Channel channel, int flag, ByteBuffer payload) {
 		ChannelSignal signal;
 
         if (flag != Frame.SIG_EMIT) {
             String m = "";
             if (payload != null && payload.capacity() > 0) {
             	Charset charset = Charset.forName("US-ASCII");
             	CharsetDecoder decoder = charset.newDecoder();
             	
                 try {
 					m = decoder.decode(payload).toString();
 				} catch (CharacterCodingException e) {}
             }
             ChannelError error = new ChannelError("", 0x0);
             
             if (flag != Frame.SIG_END) {
                 error = ChannelError.fromSigError(flag, m);
             }
 
             channel.destroy(error);
             return false;
         }
 
         if (channel == null)
             return false;
 
         signal = new ChannelSignal(flag, payload);
         channel.addSignal(signal);
         return true;
 	}
 	
 	/**
      *  Process a signal frame.
      *
      *  @param addr The address that should receive the signal.
      *  @param flag The flag of the signal.
      *  @param payload The content of the signal.
      */
 	private void processSignalFrame(int ch, int flag, ByteBuffer payload) {
 		if (ch == 0) {
             m_openChannelsMutex.lock();
             boolean destroying = false;
             int size = payload.capacity();
 
             if (flag != Frame.SIG_EMIT || payload == null || size == 0) {
                 destroying = true;
 
                 m_closingMutex.lock();
                 m_closing = true;
                 m_closingMutex.unlock();
             }
             
             Iterator<Channel> it = m_openChannels.values().iterator();
             while (it.hasNext()) {
             	Channel channel = it.next();
             	ByteBuffer payloadCopy = ByteBuffer.allocate(size);
             	payloadCopy.put(payload);
             	payloadCopy.flip();
             	payload.rewind();
 
                 if (!destroying && channel == null) {
                     destroying = true;
 
                     m_closingMutex.lock();
                     m_closing = true;
                     m_closingMutex.unlock();
                 }
 
                 if (!processSignalFrame(channel, flag, payloadCopy)) {
                     it.remove();
                 }
             }
 
             m_openChannelsMutex.unlock();
 
             if (destroying) {
                 m_closingMutex.lock();
                 m_closing = false;
                 m_closingMutex.unlock();
 
                 checkRefCount();
             }
         } else {
             m_openChannelsMutex.lock();
             Channel channel = null;
 
             if (m_openChannels.containsKey(ch))
                 channel = m_openChannels.get(ch);
             m_openChannelsMutex.unlock();
             
             if (channel == null) {
                 destroy(new ChannelError("Received unknown channel"));
                 return;
             }
             
             if (flag != Frame.SIG_EMIT && !channel.isClosing()) {
             	Frame frame = new Frame(ch, Frame.SIGNAL, Frame.SIG_END, payload);
 				writeBytes(frame);
 				
 				return;
 			}
 
             processSignalFrame(channel, flag, payload);
         }
 	}
 	
 	/**
      *  Destroy the connection.
      *
      *  @error The cause of the destroy.
      */
 	private void destroy(ChannelError error) {
 		m_destroyingMutex.lock();
         m_destroying = true;
         m_destroyingMutex.unlock();
 
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Destroying connection because: " + error.getMessage());
         }
 
         m_pendingMutex.lock();
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Destroying pendingOpenRequests of size " + m_pendingOpenRequests.size());
         }
         
         for (OpenRequest request : m_pendingOpenRequests.values()) {
         	if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Connection", request.getChannelId(), "Destroying channel");
             }
 			request.getChannel().destroy(error);
 		}
         m_pendingOpenRequests.clear();
         m_pendingMutex.unlock();
 
         m_openWaitMutex.lock();
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Destroying waitQueue of size " + m_openWaitQueue.size());
         }
         for (Queue<OpenRequest> queue : m_openWaitQueue.values()) {
             while(queue != null && !queue.isEmpty()) {
                 queue.poll().getChannel().destroy(error);
             }
         }
         m_openWaitQueue.clear();
         m_openWaitMutex.unlock();
         
         m_openChannelsMutex.lock();
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Destroying openChannels of size " + m_openChannels.size());
         }
         for (Channel channel : m_openChannels.values()) {
         	if (HydnaDebug.HYDNADEBUG) {
         		DebugHelper.debugPrint("Connection", channel.getChannel(), "Destroying channel");
         	}
             channel.destroy(error);
         }				
         m_openChannels.clear();
         m_openChannelsMutex.unlock();
 
         if (m_connected) {
         	if (HydnaDebug.HYDNADEBUG) {
         		DebugHelper.debugPrint("Connection", 0, "Closing connection");
         	}
             m_listeningMutex.lock();
             m_listening = false;
             m_listeningMutex.unlock();
             
             try {
 				m_socketChannel.close();
 			} catch (IOException e) {}
 			
             m_connected = false;
             m_handshaked = false;
         }
         String ports = Short.toString(m_port);
         String key = m_host + ports + m_auth;
 
         m_connectionMutex.lock();
         if (m_availableConnections.containsKey(key)) {
             m_availableConnections.remove(key);
         }
         m_connectionMutex.unlock();
 
         if (HydnaDebug.HYDNADEBUG) {
         	DebugHelper.debugPrint("Connection", 0, "Destroying connection done");
         }
         
         m_destroyingMutex.lock();
         m_destroying = false;
         m_destroyingMutex.unlock();
 	}
 	
 	/**
      *  Writes a frame to the connection.
      *
      *  @param frame The frame to be sent.
      *  @return True if the frame was sent.
      */
 	public boolean writeBytes(Frame frame) {
 		if (m_handshaked) {
             int n = -1;
             ByteBuffer data = frame.getData();
             int size = data.capacity();
             int offset = 0;
 
             try {
             	while(offset < size) {
                     n = m_socketChannel.write(data);
                     offset += n;
                 }
             } catch (Exception e) {
             	n = -1;
             }
 
             if (n <= 0) {
                 destroy(new ChannelError("Could not write to the connection"));
                 return false;
             }
             return true;
         }
         return false;
 	}
 }
