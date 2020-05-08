 package hydna;
 
 import java.nio.ByteBuffer;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  *  This class is used as an interface to the library.
  *  A user of the library should use an instance of this class
  *  to communicate with a server.
  */
 public class Channel {
 	private int m_ch = 0;
 	private String m_message = "";
 	
 	private Connection m_connection = null;
 	private boolean m_connected = false;
 	private boolean m_closing = false;
 	private Frame m_pendingClose;
 	
 	private boolean m_readable = false;
 	private boolean m_writable = false;
 	private boolean m_emitable = false;
 	
 	private ChannelError m_error = new ChannelError("", 0x0);
 	
 	private int m_mode;
 	private OpenRequest m_openRequest = null;
 	
 	private Queue<ChannelData> m_dataQueue = new LinkedList<ChannelData>();
 	private Queue<ChannelSignal> m_signalQueue = new LinkedList<ChannelSignal>();
 	
 	private Lock m_dataMutex = new ReentrantLock();
 	private Lock m_signalMutex = new ReentrantLock();
 	private Lock m_connectMutex = new ReentrantLock();
 	
 	/**
      *  Initializes a new Channel instance
      */
 	public Channel() {}
 	
 	public boolean getFollowRedirects() {
 		return Connection.m_followRedirects;
 	}
 	
 	public void setFollowRedirects(boolean value) {
 		Connection.m_followRedirects = value;
 	}
 	
 	/**
      *  Checks the connected state for this Channel instance.
      *
      *  @return The connected state.
      */
 	public boolean isConnected() {
 		m_connectMutex.lock();
 		boolean result = m_connected;
 		m_connectMutex.unlock();
 		return result;
 	}
 	
 	/**
      *  Checks the closing state for this Channel instance.
      *
      *  @return The closing state.
      */
 	public boolean isClosing() {
 		m_connectMutex.lock();
 		boolean result = m_closing;
 		m_connectMutex.unlock();
 		return result;
 	}
 	
 	/**
      *  Checks if the channel is readable.
      *
      *  @return True if channel is readable.
      */
 	public boolean isReadable() {
 		m_connectMutex.lock();
         boolean result = m_connected && m_readable;
         m_connectMutex.unlock();
         return result;
 	}
 	
 	/**
      *  Checks if the channel is writable.
      *
      *  @return True if channel is writable.
      */
 	public boolean isWritable() {
 		m_connectMutex.lock();
         boolean result = m_connected && m_writable;
         m_connectMutex.unlock();
         return result;
 	}
 	
 	/**
      *  Checks if the channel can emit signals.
      *
      *  @return True if channel has signal support.
      */
 	public boolean hasSignalSupport() {
 		m_connectMutex.lock();
         boolean result = m_connected && m_emitable;
         m_connectMutex.unlock();
         return result;
 	}
 	
 	/**
      *  Returns the channel that this instance listen to.
      *
      *  @return The channel.
      */
 	public int getChannel() {
 		m_connectMutex.lock();
         int result = m_ch;
         m_connectMutex.unlock();
         return result;
 	}
 	
 	/**
      *  Returns the message received when connected.
      *
      *  @return The welcome message.
      */
 	public String getMessage() {
 		m_connectMutex.lock();
         String result = m_message;
         m_connectMutex.unlock();
         return result;
 	}
 	
 	/**
      *  Resets the error.
      *  
      *  Connects the channel to the specified channel. If the connection fails 
      *  immediately, an exception is thrown.
      *
      *  @param expr The channel to connect to,
      *  @param mode The mode in which to open the channel.
      */
 	public void connect(String expr, int mode) throws ChannelError {
 		connect(expr, mode, null);
 	}
 	
 	/**
      *  Resets the error.
      *  
      *  Connects the channel to the specified channel. If the connection fails 
      *  immediately, an exception is thrown.
      *
      *  @param expr The channel to connect to,
      *  @param mode The mode in which to open the channel.
      *  @param token An optional token.
      */
 	public void connect(String expr, int mode, ByteBuffer token) throws ChannelError {
 		Frame frame;
         OpenRequest request;
       
         m_connectMutex.lock();
         if (m_connection != null) {
             m_connectMutex.unlock();
             throw new ChannelError("Already connected");
         }
         m_connectMutex.unlock();
 
         if (mode == 0x04 ||
                 mode < ChannelMode.READ || 
                 mode > ChannelMode.READWRITEEMIT) {
             throw new ChannelError("Invalid channel mode");
         }
       
         m_mode = mode;
       
         m_readable = ((m_mode & ChannelMode.READ) == ChannelMode.READ);
         m_writable = ((m_mode & ChannelMode.WRITE) == ChannelMode.WRITE);
         m_emitable = ((m_mode & ChannelMode.EMIT) == ChannelMode.EMIT);
 
         URL url = URL.parse(expr);
         String tokens = "";
         String chs = "";
         int ch;
         
         // Host can be on the form "http://auth@localhost:80/x00112233?token"
         
         
     	if (!url.getProtocol().equals("http")) {
     		if (url.getProtocol().equals("https")) {
     			throw new Error("The protocol HTTPS is not supported");
     		} else {
     			throw new Error("Unknown protocol, " + url.getProtocol());
     		}
     	}
         
         if (!url.getError().equals("")) {
             throw new Error(url.getError());
         }
        
         chs = url.getPath();
 
        // Take out the channel
 
        if (chs.length() > 0) {
             try {
            	ch = Integer.parseInt(chs, 16);
             } catch (NumberFormatException e) {
            	throw new ChannelError("Could not read the channel \"" + chs + "\"");
             }
         } else {
            ch = 1;
         }
        
         tokens = url.getToken();
         m_ch = ch;
 
         m_connection = Connection.getConnection(url.getHost(), url.getPort(), url.getAuth());
       
         // Ref count
         m_connection.allocChannel();
 
         if (token != null || tokens == "") {
             frame = new Frame(m_ch, Frame.OPEN, mode, token);
         } else {
             frame = new Frame(m_ch, Frame.OPEN, mode, ByteBuffer.wrap(tokens.getBytes()));
         }
       
         request = new OpenRequest(this, m_ch, frame);
 
         m_error = new ChannelError("", 0x0);
       
         if (!m_connection.requestOpen(request)) {
             checkForChannelError();
             throw new ChannelError("Channel already open");
         }
 
         m_openRequest = request;
 	}
 	
 	/**
      *  Sends data to the channel.
      *
      *  @param data The data to write to the channel.
      *  @param priority The priority of the data.
      */
 	public void writeBytes(ByteBuffer data, int priority) throws ChannelError {
 		boolean result;
 
         m_connectMutex.lock();
         if (!m_connected || m_connection == null) {
             m_connectMutex.unlock();
             checkForChannelError();
             throw new ChannelError("Channel is not connected");
         }
         m_connectMutex.unlock();
 
         if (!m_writable) {
             throw new ChannelError("Channel is not writable");
         }
       
         if (priority > 3 || priority == 0) {
             throw new ChannelError("Priority must be between 1 - 3");
         }
 
         Frame frame = new Frame(m_ch, Frame.DATA, priority,
                                 data);
       
         m_connectMutex.lock();
         Connection connection = m_connection;
         m_connectMutex.unlock();
         result = connection.writeBytes(frame);
 
         if (!result)
             checkForChannelError();
 	}
 	
 	/**
      *  Sends data to the channel.
      *
      *  @param data The data to write to the channel.
      */
 	public void writeBytes(ByteBuffer data) throws ChannelError {
 		writeBytes(data, 1);
 	}
 	
 	/**
      *  Sends string data to the channel.
      *
      *  @param value The string to be sent.
      */
 	public void writeString(String value) throws ChannelError {
 		writeBytes(ByteBuffer.wrap(value.getBytes()));
 	}
 	
 	/**
      *  Sends data signal to the channel.
      *
      *  @param data The data to write to the channel.
      *  @param type The type of the signal.
      */
 	public void emitBytes(ByteBuffer data) throws ChannelError {
 		boolean result;
 
         m_connectMutex.lock();
         if (!m_connected || m_connection == null) {
             m_connectMutex.unlock();
             checkForChannelError();
             throw new ChannelError("Channel is not connected.");
         }
         m_connectMutex.unlock();
 
         if (!m_emitable) {
             throw new ChannelError("You do not have permission to send signals");
         }
 
         Frame frame = new Frame(m_ch, Frame.SIGNAL, Frame.SIG_EMIT,
                             data);
 
         m_connectMutex.lock();
         Connection connection = m_connection;
         m_connectMutex.unlock();
         result = connection.writeBytes(frame);
 
         if (!result)
             checkForChannelError();
 	}
 	
 	/**
      *  Sends a string signal to the channel.
      *
      *  @param value The string to be sent.
      *  @param type The type of the signal.
      */
 	public void emitString(String value) throws ChannelError {
 		emitBytes(ByteBuffer.wrap(value.getBytes()));
 	}
 	
 	/**
      *  Closes the Channel instance.
      */
 	public void close() {
 		m_connectMutex.lock();
         if (m_connection == null || m_closing) {
             m_connectMutex.unlock();
             return;
         }
         
         m_closing = true;
         m_readable = false;
         m_writable = false;
         m_emitable = false;
       
         if (m_openRequest != null && m_connection.cancelOpen(m_openRequest)) {
         	// Open request hasn't been posted yet, which means that it's
             // safe to destroy channel immediately.
         	
         	m_openRequest = null;
         	m_connectMutex.unlock();
         	
         	ChannelError error = new ChannelError("", 0x0);
         	destroy(error);
         	return;
         }
         
         Frame frame = new Frame(m_ch, Frame.SIGNAL, Frame.SIG_END);
         
         if (m_openRequest != null) {
         	// Open request is not responded to yet. Wait to send ENDSIG until	
             // we get an OPENRESP.
         	
         	m_pendingClose = frame;
         	m_connectMutex.unlock();
         } else {
         	m_connectMutex.unlock();
         	
         	if (HydnaDebug.HYDNADEBUG) {
         		DebugHelper.debugPrint("Channel", m_ch, "Sending close signal");
 			}
         	
         	m_connectMutex.lock();
         	Connection connection = m_connection;
         	m_connectMutex.unlock();
         	connection.writeBytes(frame);
         	
         }
 	}
 	
 	/**
      *  Checks if some error has occurred in the channel
      *  and throws an exception if that is the case.
      */
 	public void checkForChannelError() throws ChannelError {
 		m_connectMutex.lock();
         if (m_error.getCode() != 0x0) {
             m_connectMutex.unlock();
             throw m_error;
         } else {
             m_connectMutex.unlock();
         }
 	}
 	
 	/**
      *  Add data to the data queue.
      *
      *  @param data The data to add to queue.
      */
 	protected void addData(ChannelData data) {
 		m_dataMutex.lock();
 		m_dataQueue.add(data);
 		m_dataMutex.unlock();
 	}
 	
 	/**
      *  Pop the next data in the data queue.
      *
      *  @return The data that was removed from the queue,
      *          or NULL if the queue was empty.
      */
 	public ChannelData popData() {
 		m_dataMutex.lock();
         ChannelData data = m_dataQueue.poll();
         m_dataMutex.unlock();
         
         return data;
 	}
 	
 	/**
      *  Checks if the signal queue is empty.
      *
      *  @return True if the queue is empty.
      */
 	public boolean isDataEmpty() {
 		m_dataMutex.lock();
         boolean result = m_dataQueue.isEmpty();
         m_dataMutex.unlock();
         
         return result;
 	}
 	
 	/**
      *  Add signals to the signal queue.
      *
      *  @param signal The signal to add to the queue.
      */
 	protected void addSignal(ChannelSignal signal) {
 		m_signalMutex.lock();
 		m_signalQueue.add(signal);
 		m_signalMutex.unlock();
 	}
 	
 	/**
      *  Pop the next signal in the signal queue.
      *
      *  @return The signal that was removed from the queue,
      *          or NULL if the queue was empty.
      */
 	public ChannelSignal popSignal() {
 		m_signalMutex.lock();
 		ChannelSignal signal = m_signalQueue.poll();
 		m_signalMutex.unlock();
 		
 		return signal;
 	}
 	
 	/**
      *  Checks if the signal queue is empty.
      *
      *  @return True is the queue is empty.
      */
 	public boolean isSignalEmpty() {
 		m_signalMutex.lock();
 		boolean result = m_signalQueue.isEmpty();
 		m_signalMutex.unlock();
 		
 		return result;
 	}
 	
 	/**
      *  Internal callback for open success.
      *  Used by the Connection class.
      *
      *  @param respch The response channel.
      */
 	protected void openSuccess(int respch, String message) {
 		m_connectMutex.lock();
 		int origch = m_ch;
 		Frame frame;
 		
 		m_openRequest = null;
         m_ch = respch;
         m_connected = true;
         m_message = message;
       
         if (m_pendingClose != null) {
         	frame = m_pendingClose;
         	m_pendingClose = null;
             m_connectMutex.unlock();
             
             if (origch != respch) {
             	// channel is changed. We need to change the channel of the
                 //frame before sending to server.
             	
             	frame.setChannel(respch);
 			}
             
             if (HydnaDebug.HYDNADEBUG) {
             	DebugHelper.debugPrint("Channel", m_ch, "Sending close signal");
 			}
             
 			m_connectMutex.lock();
 			Connection connection = m_connection;
 			m_connectMutex.unlock();
 			connection.writeBytes(frame);
         } else {
             m_connectMutex.unlock();
         }
 	}
 	
 	/**
      *  Internally destroy channel.
      *
      *  @param error The cause of the destroy.
      */
 	protected void destroy(ChannelError error) {
 		m_connectMutex.lock();
 		Connection connection = m_connection;
 		boolean connected = m_connected;
 		int ch = m_ch;
 
 		m_ch = 0;
 		m_connected = false;
         m_writable = false;
         m_readable = false;
         m_pendingClose = null;
         m_closing = false;
         m_openRequest = null;
         m_connection = null;
       
         if (connection != null) {
             connection.deallocChannel(connected ? ch : 0);
         }
         
         m_error = error;
 
         m_connectMutex.unlock();
 	}
 }
