 package com.elaxys.android.websocket;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.security.KeyManagementException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Random;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import javax.net.SocketFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.Header;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.message.BasicLineParser;
 
 import android.net.Uri;
 import android.os.Handler;
 import android.util.Base64;
 import android.util.Log;
 
 
 /**
  * Web Socket Client (RFC6455)
  */
 public class Client {
     
     /**
      * Configuration parameters
      */
     public static class Config {
         /** Server URI in the format "ws[s]://host[:port][/path]" */
         public String  mURI;
         /** Size of the transmission queue in number of messages */
         public int     mQueueSize;
         /** Connection timeout in milliseconds */
         public int     mConnTimeout;
         /** Retry interval in milliseconds */
         public int     mRetryInterval;
         /** Maximum size of received packet PAYLOAD (0=unlimited) */
         public int     mMaxRxSize;
         /** True to respond to PINGS, false sends the PING frame to the application */
         public boolean mRespondPing;
         /** Checks server certificate on SSL connection */
         public boolean mServerCert;
         /** Log tag used for debugging. If null no log is generated */
         public String mLogTag;
         /** Default constructor */
         public Config() {
             mURI            = "ws://www.example.com/path";
             mQueueSize      = 10;
             mConnTimeout    = 5000;
             mRetryInterval  = 1000;
             mMaxRxSize      = 128*1024;
             mRespondPing    = true;
             mServerCert     = false;
             mLogTag         = "WSCLIENT";
         }
         /** Copy constructor */
         public Config(Config config) {
             mURI            = config.mURI;
             mQueueSize      = config.mQueueSize;
             mConnTimeout    = config.mConnTimeout;
             mRetryInterval  = config.mRetryInterval;
             mMaxRxSize      = config.mMaxRxSize;
             mRespondPing    = config.mRespondPing;
             mServerCert     = config.mServerCert;
             mLogTag         = config.mLogTag;
         }
     }
     
     /**
      * Interface to listen to generated events
      */
     public interface Listener {
         /**
          * Called when reception thread is started.
          */
         public void onClientStart();
         /**
          * Called when reception thread is about to connect with the server.
          */
         public void onClientConnect();
         /**
          * Called when connection and handshake with the server
          * completed successfully.
          */
         public void onClientConnected();
         /**
          * Called when any error occurs, including the closing of
          * the connection.
          * @param msg Error message
          */
         public void onClientError(String msg);
         /**
          * Called when a BINARY frame is received from the server
          * @param type Frame type. Possible values are F_BINARY* and F_PING
          * @param data Frame PAYLOAD byte array
          */
         public void onClientRecv(int type, byte[] data);
         /**
          * Called when a TEXT frame is received from the server
          * @param type Frame type. Possible values are F_TEXT*
          * @param data Frame PAYLOAD string
          */
         public void onClientRecv(int type, String data);
         /**
          * Called when a frame removed is from the transmission queue
          * and sent to server.
          * @param fid  Frame id.
          */
         public void onClientSent(int fid);
         /**
          * Called when the client is stopped
          */
         public void onClientStop();
     }
     
     /**
      * Statistics Data.
      * Contains communication statistics collected during the
      * communication.
      */
     public static class Stats {
         /**
          * Total number of received frames, including control frames.
          */
         public int mRxFrames;
         /**
          * Total number of bytes received.
          */
         public int mRxBytes;
         /**
          * Total number of PAYLOAD bytes received.
          */
         public int mRxData;
         /**
          * Total number of transmitted frames, including control frames.
          */
         public int mTxFrames;
         /**
          * Total number of transmitted bytes.
          */
         public int mTxBytes;
         /**
          * Total number of transmitted PAYLOAD bytes.
          */
         public int mTxData;
         /**
          * Current number of messages in transmission queue
          */
         public int mInQueue;
     }
     
     
     /**
      * Exception generated on some API calls
      */
     public static class Error extends IOException {
         private static final long serialVersionUID = 1L;
         public Error(String msg) {
             super(msg);
         }
     }
     
     
     /**
      * Internal exception generated on any detected frame error
      */
     private static class ErrorInternal extends IOException {
         private static final long serialVersionUID = 1L;
         public ErrorInternal(String msg) {
             super(msg);
         }
     }
     
     /**
      * Type for received messages sent to handler
      */
     private static class RxMsg {
         int     mType;
         byte[]  mBytes;
         String  mText;
     }
     
     /**
      * Type for messages inserted in transmission queue
      */
     private static class TxMsg {
         int     mID;
         int     mOpcode;
         int     mHeadsize;
         byte[]  mFrame;
     }
 
     /**
      * Type for error events
      */
     private static class ErrorEvent {
     	int		mCode;
     	String	mMsg;
     	
     	public ErrorEvent(int code, String msg) {
     		mCode = code;
     		mMsg  = msg;
     	}
     }
 
     
     /** Client Version String */
     public static final String VERSION = "0.9.0";
     
     /** Connection error */
     public static final int E_CONNECT	= 1;
     /** SSL error */
     public static final int E_SSL    	= 2;
     /** Input/Output error */
     public static final int E_IO		= 3;
     /** Server handshake error */
     public static final int E_HANDSHAKE = 4;
     /** WebSocket protocol error */
     public static final int E_PROTOCOL	= 5;
     
     /** Client is stopped */
     public static final int ST_STOPPED      = 0;
     /** Client is started by not connected yet */
     public static final int ST_STARTED      = 1;
     /** Client is connected to server */
     public static final int ST_CONNECTED    = 2;    
 
     /** Frame Types */
     /** Single TEXT frame of a NOT fragmented message */
     public static final int F_TEXT          = 1;
     /** First TEXT frame of a fragmented message */
     public static final int F_TEXT_FIRST    = 2;
     /** Next TEXT frame of a fragmented message */
     public static final int F_TEXT_NEXT     = 3;
     /** Last TEXT frame of a fragmented message */
     public static final int F_TEXT_LAST     = 4;
     /** Single BINARY frame of a NOT fragmented message */
     public static final int F_BINARY        = 5;
     /** First BINARY frame of a fragmented message */
     public static final int F_BINARY_FIRST  = 6;
     /** Next BINARY frame of a fragmented message */
     public static final int F_BINARY_NEXT   = 7;
     /** Last BINARY frame of a fragmented message */
     public static final int F_BINARY_LAST   = 8;
     /** PING frame with BINARY PAYLOAD */
     public static final int F_PING          = 9;
     /** PONG frame with BINARY PAYLOAD */
     public static final int F_PONG          = 10;
     
     /** Private Data */
     private Config      mConfig;
     private Handler     mHandler;
     private Logger      mLog;
     private String      mHost;
     private int         mPort;
     private boolean     mSSL;
     private String      mPath;
     private String      mOrigin;
     private Socket      mSocket;
     private Thread                  mRxThread = null;
     private Thread                  mTxThread = null;
     private BufferedInputStream     mSocketIn;
     private OutputStream            mSocketOut;
     private volatile int            mStatus;    
     private volatile boolean        mRxRun;
     private volatile boolean        mTxRun;
     private boolean                 mFIN;
     private boolean                 mMasked;
     private int                     mOpcode;
     private int                     mContinuation = -1;
     private long                    mLength;
     private int                     mHeadSize;
     private int                     mNextFrameID = 1;
     private byte[]                  mMask = new byte[4];
     private Random mRandom          = new Random();
     private ArrayBlockingQueue<TxMsg> mTxQueue;
     private volatile Stats          mStats = new Stats();
     
     // WebSocket Frame
     private static final int LENGTH1        = 126;
     private static final int LENGTH2        = 127;
     
     private static final int MASK_FIN       = 0x80;
     private static final int MASK_RSV       = 0x70;
     private static final int MASK_OP        = 0x0F;
     private static final int MASK_MASKED    = 0x80;
     private static final int MASK_LENGTH    = 0x7F;
     private static final int OP_CONT        = 0x00;
     private static final int OP_TEXT        = 0x01;
     private static final int OP_BINARY      = 0x02;
     private static final int OP_CLOSE       = 0x08;
     private static final int OP_PING        = 0x09;
     private static final int OP_PONG        = 0x0A;
     private static final boolean USE_MASK   = true;
     // Magic string for calculating keys.
     private static final String MAGIC       = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
     /** Event codes sent to Handler */
     private static final int EV_START       = 1;
     private static final int EV_CONNECT     = 2;
     private static final int EV_CONNECTED   = 3;
     private static final int EV_RECV        = 4;
     private static final int EV_SENT        = 5;
     private static final int EV_ERROR       = 6;
     private static final int EV_STOP        = 7;
     /** Fragmentation */
     private static final int FRAG_NONE      = 0;
     private static final int FRAG_FIRST     = 1;
     private static final int FRAG_NEXT      = 2;
     private static final int FRAG_LAST      = 3;
     /** Opcode types */
     private static final int[] OPCODES = {
         1,              // 0x00 - OP_CONT
         1,              // 0x01 - OP_TEXT
         1,              // 0x02 - OP_BINARY
         0,0,0,0,0,      //        INVALID
         1,              // 0x08 - OP_CLOSE
         1,              // 0x09 - OP_PING
         1,              // 0x0A - OP_PONG
     };
     /** Handshake related */
     private static final int HANDSHAKE_TIMEOUT = 5000;
     private static final int MAX_HEADER_LINE   = 512;
  
     
     /**
      * Constructor
      * @param config Reference to configuration object
      * @throws Error 
      */
     public Client(Config config, Listener listener) throws Error {
         
         mConfig  = new Config(config);
         mHandler = new ListenerHandler(listener);
         // Creates logger
         if (mConfig.mLogTag == null) {
             mLog = new NullLogger();
         }
         else {
             mLog = new DefaultLogger(mConfig.mLogTag);
         }
         // Parse server URI
         Uri uri = Uri.parse(config.mURI);
         String scheme = uri.getScheme();
         if (scheme == null) {
             throw new Error("Invalid URI");
         }
         String originScheme = "http";
         if (scheme.equals("wss") || scheme.equals("https")) {
             mSSL = true;
             originScheme = "https";
         }
         mHost = uri.getHost();
         if (mHost == null) {
             throw new Error("Invalid URI");
         }
         // Get server port
         mPort = uri.getPort();
         if (mPort == -1) {
             if (mSSL) {
                 mPort = 443;
             }
             else {
                 mPort = 80;
             }
         }
         mPath = uri.getPath();
         Uri.Builder uriOrigin = new Uri.Builder();
         uriOrigin.scheme(originScheme);
         uriOrigin.authority(mHost);
         mOrigin = uriOrigin.toString();
         mTxQueue = new ArrayBlockingQueue<TxMsg>(mConfig.mQueueSize);
         clearStats();
         mStatus = ST_STOPPED;
     }
 
     
     /**
      * Starts the client
      * The client will try to connect to the server until stopped.
      */
     public void start() {
         if (mStatus != ST_STOPPED) {
             return;
         }
         // Starts RxThread
         mRxRun = true;
         mRxThread = new RxThread();
         mRxThread.start();
     }
    
     
     /**
      * Stops the client.
      * After all messages in the transmission queue are sent,
      * a CLOSE frame is sent to the server and the client is stopped.
      */
     public void stop() {
         // If already stopped, ignore.
         if (mStatus == ST_STOPPED) {
             return;
         }
         // If connected, sends CLOSE frame.
         // The transmission thread will stop the client after this frame is sent.
         // We do not wait to receive the server CLOSE frame response.
         if (mStatus == ST_CONNECTED) {
             mRxRun = false;
             sendFrame(OP_CLOSE, FRAG_NONE, new byte[0]);
             return;
         }
         stopRxThread();
         stopTxThread();
     }
    
     
     /**
      * Returns the current client status 
      * @return current status (ST_*)
      */
     public int getStatus() {
         return mStatus;
     }
 
     
     /**
      * Get statistics data
      * @param stats object to be updated
      */
     public void getStats(Stats stats) {
         stats.mRxFrames = mStats.mRxFrames;
         stats.mRxData   = mStats.mRxData;
         stats.mRxBytes  = mStats.mRxBytes;
         stats.mTxFrames = mStats.mTxFrames;
         stats.mTxData   = mStats.mTxData;
         stats.mTxBytes  = mStats.mTxBytes;
         stats.mInQueue  = mTxQueue.size();
     }
     
     
     /**
      * Clear transmission queue
      */
     public void clearTx() {
         mTxQueue.clear();
     }
    
   
     /**
      * Clears Statistics data
      */
     public void clearStats() {
         mStats.mRxFrames = 0;
         mStats.mRxBytes = 0;
         mStats.mRxData = 0;
         mStats.mTxFrames = 0;
         mStats.mTxBytes = 0;
         mStats.mTxData = 0;
     }
     
     
     /**
      * Inserts TEXT message in transmission queue
      * @param data String with message PAYLOAD
      * @return frame id or null if transmission queue is full.
      * @throws Error
      */
     public Integer send(String data) throws Error {
         return sendFrame(OP_TEXT, FRAG_NONE, encodeString(data));
     }
     
    
     /**
      * Inserts BINARY message in transmission queue
      * @param data Byte array with message PAYLOAD
      * @return frame id or null if queue is full.
      * @throws Error
      */
     public Integer send(byte[] data) throws Error {
         return sendFrame(OP_BINARY, FRAG_NONE, data);
     }
    
   
     /**
      * Inserts PING frame in transmission queue
      * @param data Byte array with PAYLOAD (up to 125 bytes)
      * @return frame id or null if queue is full.
      * @throws Error 
      */
     public Integer ping(byte[] payload) throws Error {
         if (payload.length >= LENGTH1) {
             throw new Error("Payload MUST be less than " + LENGTH1);
         }
         return sendFrame(OP_PING, FRAG_NONE, payload);
     }
     
     
     /**
      * Inserts PONG frame in transmission queue
      * @param payload Byte array with PAYLOAD (up to 125 bytes)
      * @return frame id or null if queue is full.
      * @throws Error 
      */
     public Integer pong(byte[] payload) throws Error {
         if (payload.length >= LENGTH1) {
             throw new Error("Payload MUST be less than " + LENGTH1);
         }
         return sendFrame(OP_PONG, FRAG_NONE, payload);
     }
 
    
     /**
      * Insert first fragment of TEXT message in transmission queue
      * @param data String with frame PAYLOAD
      * @return frame id or null if queue is full.
      * @throws Error
      */
     public Integer sendFirst(String data) throws Error {
         return sendFrame(OP_TEXT, FRAG_FIRST, encodeString(data));
     }
  
     
     /**
      * Inserts next fragment of TEXT message in transmission queue
      * @param data String with frame PAYLOAD
      * @return frame id or null if queue is full.
      * @throws Error
      */
     public Integer sendNext(String data) throws Error {
         return sendFrame(OP_TEXT, FRAG_NEXT, encodeString(data));
     }
 
     
     /**
      * Inserts last fragment of TEXT message in transmission queue
      * @param data String with frame PAYLOAD
      * @return frame id or null if queue is full.
      * @throws ErrorInternal 
      */
     public Integer sendLast(String data) throws Error {
         return sendFrame(OP_TEXT, FRAG_LAST, encodeString(data));
     }
 
     
     /**
      * Insert first fragment of BINARY message in transmission queue
      * @param payload Byte array with frame PAYLOAD
      * @return frame id or null if queue is full.
      * @throws Error 
      */
     public Integer sendFirst(byte[] payload) throws Error {
         return sendFrame(OP_BINARY, FRAG_FIRST, payload);
     }
     
     
     /**
      * Inserts next fragment of BINARY message in transmission queue
      * @param payload Byte array with frame PAYLOAD
      * @return frame id or null if queue is full.
      * @throws Error 
      */
     public Integer sendNext(byte[] payload) throws Error {
         return sendFrame(OP_BINARY, FRAG_NEXT, payload);
     }
 
     
     /**
      * Inserts last fragment of BINARY message in transmission queue
      * @param data Byte array with frame PAYLOAD
      * @return frame id or null if queue is full.
      * @throws Error 
      */
     public Integer sendLast(byte[] payload) throws Error {
         return sendFrame(OP_BINARY, FRAG_LAST, payload);
     }
     
    
     /**
      * Stops reception thread
      */
     private void stopRxThread() {
         mRxRun = false;
         if (mRxThread == null) {
             return;
         }
         // Interrupt reception thread (when sleep)
         // Closes socket to force thread IO exception
         mRxThread.interrupt();
         try {
             if (mSocket != null) {
                 if (!mSSL) {
                     mSocket.shutdownInput();
                     mSocket.shutdownOutput();
                 }
                 else {
                     mSocket.close();
                 }
             }
         } catch (IOException e) {}
     }
 
     
     /**
      * Stops transmission thread
      */
     private void stopTxThread() {
         
         mTxRun = false;
         if (mTxThread != null) {
             mTxThread.interrupt();
         }
     }
    
     
     /**************************************************************************
      * Connection/Reception Thread
      */
     private class RxThread extends Thread {
         
         public RxThread() {
             super("WSCLIENT.RxThread");
         }
         
         @Override
         public void run() {
             mStatus = ST_STARTED;
             long delay   = 0;
             mLog.debug("RxThread started");
             sendEvent(EV_START, null);
             // Main Read Loop: try to connect and read frames
             while (mRxRun) {
                 // Delay before trying to connect. The first time the delay is 0.
                 try {
                     Thread.sleep(delay);
                 } catch (InterruptedException e) {
                     break;
                 }
                 delay = mConfig.mRetryInterval;
                 sendEvent(EV_CONNECT, null);
                 // Try to connect with server and if error, retry.
                 if (!connect()) {
                     continue;
                 }
                 // Do handshake with server and if error, retry
                 if (!doHandshake()) {
                     continue;
                 }
                 // Starts the writer thread.
                 if (mTxThread == null) {
                     mTxThread = new TxThread();
                     mTxThread.start();
                 }
                 mStatus = ST_CONNECTED;
                 sendEvent(EV_CONNECTED, null);
                 // Process received frames and returns on any error.
                 processFrames();
                 // Stops transmission thread
                 mLog.debug("Stopping TxThread");
                 mTxRun = false;
                 if (mTxThread != null) {
                     mTxThread.interrupt();
                 }
                 // Continue to retry
             }
             // This thread is stopping.
             mRxThread = null;
             mStatus = ST_STOPPED;
             sendEvent(EV_STOP, null);
             mLog.debug("RxThread stopped");   
         }
         
         
         /**
          * Connects to server and creates streams
          * @return true if OK, false otherwise
          */
         private boolean connect() {
             // Closes previous connection if any
             if (mSocket != null) {
                 try {
                     mSocket.close();
                 } catch (IOException e) {}
             }
             // Try to connect with server
             try {
                 mLog.debug("RxThread connecting with: %s", mConfig.mURI);
                 // Creates normal socket
                 if (!mSSL) {
                     mSocket = new Socket();
                 }
                 // Connects SSL socket
                 else {
                     SSLContext sctx = createSSLContext();
                     if (sctx == null) {
                         mLog.error("RxThread creating SSLContext");
                         return false;
                     }
                     SocketFactory socketFactory = sctx.getSocketFactory();
                     mSocket = socketFactory.createSocket();
                 }
                 // Try to connect with server
                 InetSocketAddress address = new InetSocketAddress(mHost, mPort);
                 mSocket.connect(address, mConfig.mConnTimeout);
                 // Creates streams to read and write to the socket
                 mSocketIn  = new BufferedInputStream(mSocket.getInputStream(), 8*1024);
                 mSocketOut = mSocket.getOutputStream();
             } catch (UnknownHostException e) {
                 sendEvent(EV_ERROR, e.getMessage());
                 return false;
             } catch (IOException e) {
                 mLog.error("RxThread ERROR: IOException: %s", e.getMessage());
                 sendEvent(EV_ERROR, e.getMessage());
                 return false;
             }
             mLog.debug("RxThread connected OK");
             return true;
         }
         
        
         /**
          * Sends WebSocket handshake to server and checks response
          * @return true if OK, false otherwise
          */
         private boolean doHandshake() {
             StringBuilder req;
             String seckey;
         
             mLog.debug("Sending handshake request");
             seckey = createSecKey();
             // Sends HTTP upgrade request
             req = new StringBuilder();
             req.append("GET " + mPath + " HTTP/1.1\r\n");
             req.append("Upgrade: websocket\r\n");
             req.append("Connection: Upgrade\r\n");
             req.append("Host: " + mHost + "\r\n");
             req.append("Origin: " + mOrigin + "\r\n");
             req.append("Sec-WebSocket-Key: " + seckey + "\r\n");
             req.append("Sec-WebSocket-Version: 13\r\n");
             req.append("\r\n");
             byte[] data = req.toString().getBytes();
             try {
                 mSocketOut.write(data);
             } catch (IOException e) {
                 sendEvent(EV_ERROR, e.getMessage());
                 return false;
             }
             // Sets socket timeout to wait for handshake
             try {
                 mSocket.setSoTimeout(HANDSHAKE_TIMEOUT);
             } catch (SocketException e) {
                 sendEvent(EV_ERROR, e.getMessage());
                 return false;
             }
             // Reads response lines from server
             String[] Responses = new String[10];
             int nlines = 0;
             while (true) {
                 String line = readLine(mSocketIn);
                 if (line == null) {
                     return false;
                 }
                 // Empty line is response terminator.
                 if (line.length() == 0) {
                     break;
                 }
                 // Saves line in array
                 if (nlines < Responses.length) {
                     Responses[nlines++] = line;
                 }
             }
             if (nlines < 1) {
                 sendEvent(EV_ERROR, "Empty response from server");
                 return false;
             }
             // Parses status line
             StatusLine status = BasicLineParser.parseStatusLine(Responses[0], new BasicLineParser());
             if (status.getStatusCode() != HttpStatus.SC_SWITCHING_PROTOCOLS) {
                 sendEvent(EV_ERROR, String.format("Server response status: %d", status.getStatusCode()));
                 return false;
             }
             // Parse headers
             boolean found = false;
             for (int line = 1; line < nlines; line++) {
                 Header header = BasicLineParser.parseHeader(Responses[line], new BasicLineParser());
                 if (header.getName().equals("Sec-WebSocket-Accept")) {
                     String accept = header.getValue();
                     String calc = calcAccept(seckey);
                     if (!accept.equals(calc)) {
                         sendEvent(EV_ERROR, "Server accept key is invalid");
                         return false;
                     }
                     found = true;
                 }
             }
             if (!found) {
                 sendEvent(EV_ERROR, "Server accept header not found");
                 return false;
             }
             // Remove socket timeout
             try {
                 mSocket.setSoTimeout(0);
             } catch (SocketException e) {
                 sendEvent(EV_ERROR, e.getMessage());
                 return false;
             }
             mLog.debug("Handshake OK");
             return true;
         }
       
         
         /**
          * Process received frames and returns on any error
          */
         private void processFrames() {
             boolean text = false;
             
             while (mRxRun) {
                 byte[] payload = null;
                 // Read next frame
                 try {
                     payload = readFrame();
                 } catch (IOException e) {
                     sendEvent(EV_ERROR, e.getMessage());
                     return;
                 }
                 // Update statistics
                 mStats.mRxFrames++;
                 mStats.mRxData += payload.length;
                 mStats.mRxBytes += (mHeadSize + payload.length);
                 mLog.debug("Rx frame: %x", mOpcode);
                 // Prepare message to send to Handler
                 RxMsg m = new RxMsg();
                 m.mBytes = payload;
                 // Process each frame type
                 switch (mOpcode) {
                 case OP_CONT:
                     if (mContinuation == OP_TEXT) {
                         text = true;
                         if (mFIN) {
                             m.mType = F_TEXT_LAST;
                             mContinuation = -1;
                         }
                         else {
                             m.mType = F_TEXT_NEXT;
                         }
                         break;
                     }
                     if (mContinuation == OP_BINARY) {
                         if (mFIN) {
                             m.mType = F_BINARY_LAST;
                             mContinuation = -1;
                         }
                         else {
                             m.mType = F_BINARY_NEXT;
                         }
                         break;
                     }
                     sendEvent(EV_ERROR, "Received Unexpected OP_CONT Frame");
                     return;
                 case OP_TEXT:
                     if (mContinuation >= 0) {
                         sendEvent(EV_ERROR, "Received OP_TEXT Frame instead of fragment");
                         return;
                     }
                     text = true;
                     if (mFIN) {
                         m.mType = F_TEXT;
                     }
                     else {
                         m.mType = F_TEXT_FIRST;
                         mContinuation = OP_TEXT;
                     }
                     break;
                 case OP_BINARY:
                     if (mContinuation >= 0) {
                         sendEvent(EV_ERROR, "Received OP_BINARY Frame instead of fragment");
                         return;
                     }
                     if (mFIN) {
                         m.mType = F_BINARY;
                     }
                     else {
                         m.mType = F_BINARY_FIRST;
                         mContinuation = OP_BINARY;
                     }
                     break;
                 case OP_CLOSE:
                     // Echo CLOSE frame to server
                     // The client will stop by transmission thread after this frame was sent.
                     sendFrame(OP_CLOSE, FRAG_NONE, payload);
                     sendEvent(EV_ERROR, "Closed by Server");
                     break;
                 case OP_PING:
                     if (payload.length >= LENGTH1) {
                         sendEvent(EV_ERROR, "Received PING with payload >=" + LENGTH1) ;
                         return;
                     }
                     // Sends PONG if configured to do so.
                     if (mConfig.mRespondPing) { 
                         sendFrame(OP_PONG, FRAG_NONE, payload);
                         mLog.debug("Sent PONG");
                         continue;
                     }
                     m.mType = F_PING;
                     break;
                 case OP_PONG:
                     if (payload.length >= LENGTH1) {
                         sendEvent(EV_ERROR, "Received PONG with payload >=" + LENGTH1) ;
                         return;
                     }
                     m.mType = F_PONG;
                     break;
                 }
                 // Decodes the PAYLOAD of TEXT frames
                 if (text) {
                     try {
                         m.mText = decodeString(payload);
                     } catch (ErrorInternal e) {
                         sendEvent(EV_ERROR, e.getMessage());
                         return;
                     }
                 }
                 sendEvent(EV_RECV, m);
             }
         }
     }
     
     
     /**
      * Reads complete frame from input
      * @return PAYLOAD of received frame 
      * @throws IOException
      */
     private byte[] readFrame() throws IOException {
  
         readOpcode();
         readLength();
         readMask();
         return readPayload();
     }
   
    
     /**
      * Blocks reading for frame start byte
      * @throws IOException for any error
      */
     private void readOpcode() throws IOException {
         int data;
         
         data = mSocketIn.read();
         if (data < 0) {
             throw new ErrorInternal("Connection Closed");
         }
         if ((data & MASK_RSV) != 0) {
             throw new ErrorInternal("Received RSV different from zero");
         }
         mFIN = (data & MASK_FIN) == MASK_FIN;
         mOpcode = data & MASK_OP;
         if (mOpcode >= OPCODES.length || OPCODES[mOpcode] == 0) {
             throw new ErrorInternal("Received Invalid OPCODE");
         }
     }
     
    
     /**
      * Blocks reading the frame length
      * The frame length can have from 1 to 9 bytes
      * @throws IOException for any error
      */
     private void readLength() throws IOException {
         int mlen;
    
         // Reads mask + length byte
         mlen = mSocketIn.read();
         if (mlen < 0) {
             mLog.debug("readLength EOF");
             throw new ErrorInternal("Connection Closed");
         }
         mMasked = (mlen & MASK_MASKED) == MASK_MASKED;
         mLength = (mlen & MASK_LENGTH);
         mHeadSize = 2;
         
         if (mLength < 126) {
             return;
         }
         if (mLength == 126)  {
             byte[] len = new byte[2];
             readBytes(mSocketIn, len);
             mLength = 0;
             for (int pos = 0; pos < 2; pos++) {
                 mLength = (mLength << 8) + (len[pos] & 0xFF);
             }
             mHeadSize += 2;
             return;
         }
         // Length = 127
         byte[] len = new byte[8];
         readBytes(mSocketIn, len);
         mLength = 0;
         for (int pos = 0; pos < 8; pos++) {
             mLength = (mLength << 8) + (len[pos] & 0xFF);
         }
         mHeadSize += 8;
     }
 
   
     /**
      * Blocks reading the frame mask (if applicable)
      * @throws IOException
      */
     private void readMask() throws IOException {
         if (!mMasked) {
             return;
         }
         mHeadSize += mMask.length;
         readBytes(mSocketIn, mMask);
     }
     
    
     /**
      * Blocks reading the frame pay load
      * @return byte array with frame pay load
      * @throws IOException
      */
     private byte[] readPayload() throws IOException {
         
         if (mConfig.mMaxRxSize != 0 && mLength > mConfig.mMaxRxSize) {
             throw new ErrorInternal("Received Frame size greater than maximum");
         }
         byte[] payload = new byte[(int)mLength];
         readBytes(mSocketIn, payload);
         return payload;
     }
     
     
     /**************************************************************************
      * Client Transmission Thread
      */
     private class TxThread extends Thread {
             
         public TxThread() {
             super("WSCLIENT.TxThread");
         }
         
         @Override
         public void run() {
             mLog.debug("TxThread started");
             TxMsg txmsg;
             mTxRun = true;
             while (mTxRun) {
                 // Blocks waiting for next frame to send
                 try {
                    txmsg = mTxQueue.take();
                 } catch (InterruptedException e1) {
                     mLog.debug("TxThread interrupted");
                     break;
                 }
                 // Sends frame
                 try {
                     mSocketOut.write(txmsg.mFrame, 0, txmsg.mFrame.length);
                     mSocketOut.flush();
                     mStats.mTxFrames++;
                     mStats.mTxBytes += txmsg.mFrame.length; 
                     mStats.mTxData  += (txmsg.mFrame.length - txmsg.mHeadsize);
                     // If close frame sent, STOPS the client
                     if (txmsg.mOpcode == OP_CLOSE) {
                         stopRxThread();
                         break;
                     }
                     sendEvent(EV_SENT, txmsg);
                 } catch (IOException e) {
                     break;
                 }
             }
             mTxThread = null;
             mLog.debug("TxThread: stopped");
         }
     }
 
   
     /**
      * Builds and inserts frame into transmission queue
      * @param opcode Frame opcode (OP_*)
      * @param frag   Fragmentation type  (FRAG_*)
      * @param payload Byte array with pay load
      * @return Frame id if frame inserted or null if queue is full.
      */
     private Integer sendFrame(int opcode, int frag, byte[] payload) {
         int paylen;
         int headsize;
         int masksize;
         int next;
         int fid;
         byte[] frame;
         byte[] mask;
   
         // Calculates the header size and allocates frame buffer
         paylen = payload.length;
         if (paylen < 126) {
             headsize = 2;
         }
         else if (paylen <= 0xFFFF) {
             headsize = 4;
         }
         else {
             headsize = 10;
         }
         masksize = 0;
         if (USE_MASK) {
             masksize = 4;
         }
         frame = new byte[headsize + masksize + paylen];
         // Sets the FIN bit and opcode of the frame
         switch (frag) {
         case FRAG_NONE:
             frame[0] = (byte)(MASK_FIN | opcode);
             break;
         case FRAG_FIRST:
             frame[0] = (byte)opcode;
             break;
         case FRAG_NEXT:
             frame[0] = (byte)OP_CONT;
             break;
         case FRAG_LAST:
             frame[0] = (byte)(MASK_FIN | OP_CONT);
             break;
         }
         if (USE_MASK) {
             frame[1] = (byte)MASK_MASKED;
         }
         else {
             frame[1] = 0;
         }
         if (headsize == 2) {
             frame[1] |= (byte)paylen;
             next = 2;
         }
         else if (headsize == 4) {
             frame[1] |= 126;
             frame[2] = (byte)(paylen >> 8);
             frame[3] = (byte)paylen;
             next = 4;
         }
         else {
             frame[1] |= 127;
             frame[2] = 0;
             frame[3] = 0;
             frame[4] = 0;
             frame[5] = 0;
             frame[6] = (byte)(paylen >> 24);
             frame[7] = (byte)(paylen >> 16);
             frame[8] = (byte)(paylen >> 8);
             frame[9] = (byte)(paylen);
             next = 10;
         }
         // Generates random mask
         if (USE_MASK) {
             mask = new byte[4];
             mRandom.nextBytes(mask);
             System.arraycopy(mask, 0, frame, next, mask.length);
             next += 4;
         }
         // Copy pay load to frame and masks it
         System.arraycopy(payload, 0, frame, next, paylen);
         if (USE_MASK) {
             maskPayload(frame, next, mask);
         }
         // Inserts frame in transmission queue
         fid = mNextFrameID++;
         TxMsg txmsg = new TxMsg();
         txmsg.mOpcode = opcode;
         txmsg.mID = fid;
         txmsg.mHeadsize = headsize + masksize;
         txmsg.mFrame = frame;
         if (mTxQueue.offer(txmsg)) {
             return fid;
         }
         return null;
     }
    
     
     /**
      * Creates an SSL Context which could check or not the server certificate,
      * depending on configuration option.
      * @return SSLContext to use or null if error
      */
     private SSLContext createSSLContext() {
         SSLContext context = null;
         try {
             context = SSLContext.getInstance("TLS");
         } catch (NoSuchAlgorithmException e) {
             sendEvent(EV_ERROR, e.getMessage());
             return null;
         }
         TrustManager[] trustManagers;
         if (mConfig.mServerCert) {
             trustManagers = new TrustManager[0];
         }
         else {
             trustManagers = new X509TrustManager[]{
                 new X509TrustManager() {
                     public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                     public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                     public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                 }
             };
         }
         try {
             context.init(null, trustManagers, new SecureRandom());
         } catch (KeyManagementException e) {
             sendEvent(EV_ERROR, e.getMessage());
         }
         return context;
     }
    
     
     /**
      * Masks frame pay load
      * @param frame  Buffer with frame to mask
      * @param pos    Position in the frame where the payload starts
      * @param mask   Byte array with mask
      */
     private static void maskPayload(byte[] frame, int pos, byte[] mask) {
         int idx;
 
         for (idx = 0; idx < frame.length - pos; idx++) {
             frame[pos + idx] = (byte)(frame[pos + idx] ^ mask[idx % 4]);
         }
     }
    
    
     /**
      * Creates accept key string for handshake
      * @return String with secret BASE64 encoded
      */
     private String createSecKey() {
         
         byte[] secret = new byte[16];
         mRandom.nextBytes(secret);
         return Base64.encodeToString(secret, Base64.DEFAULT).trim();
     }
 
    
     /**
      * Calculates the accept key for specified secret
      * @param secret String with secret key
      * @return String with accept key
      */
     private String calcAccept(String secret) {
         String concat;
         MessageDigest md;
         byte[] sha1;
     
         concat = secret + MAGIC;
         try {
             md = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             return "";
         }
         md.update(concat.getBytes(), 0, concat.length());
         sha1 = md.digest();
         return Base64.encodeToString(sha1, Base64.DEFAULT).trim();
     }
 
    
     /**
      * Decodes String from UTF-8 byte array
      * @param data Byte array to decode
      * @return decoded string
      * @throws ErrorInternal 
      */
     private static String decodeString(byte[] data) throws ErrorInternal {
         try {
             return new String(data, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new ErrorInternal(e.getMessage());
         }
     }
    
     
     /**
      * Encodes String into UTF-8 byte array 
      * @param data String to encode
      * @return byte array or null if error
      * @throws Error 
      */
     private static byte[] encodeString(String data) throws Error {
         try {
             return data.getBytes("UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new Error(e.getMessage());
         }
     }
    
     
     /**
      * Read all the specified number of bytes from the input stream
      * @param buffer Buffer to read
      * @return number of bytes read or null if error
      * @throws IOException 
      */
     private void readBytes(InputStream is, byte[] buffer) throws IOException {
         int size = buffer.length;
         int nread = 0;
         int bytes;
         
         while (nread < size) {
             bytes = is.read(buffer, nread, size - nread);
             if (bytes <= 0) {
                 throw new ErrorInternal("Connection Closed");
             }
             nread += bytes;
         }
     }
   
     
     /**
      * Reads next line terminated by "\r\n" from stream
      * @param stream InputStream to read from
      * @return String with line or null if error or EOF
      * @throws IOException
      */
     private String readLine(InputStream stream) {
         int readChar;
         int len = 0;
         StringBuilder line = new StringBuilder();
         
         while (true) {
             try {
                 readChar = stream.read();
             } catch (SocketTimeoutException e) {
                 sendEvent(EV_ERROR, "Reception Timeout");
                 return null;
             } catch (IOException e) {
                 sendEvent(EV_ERROR, e.getMessage());
                 return null;
             }
             if (readChar == -1) {
                 sendEvent(EV_ERROR, "Connection Closed");
                 return null;
             }
             if (readChar == '\r') {
                 continue;
             }
             if (readChar == '\n') {
                 return line.toString();
             }
             if (len >= MAX_HEADER_LINE) {
                 sendEvent(EV_ERROR, "Max header line recv");
                 return null;
             }
             line.append((char)readChar);
             len++;
         }
     }
 
     
     /**
      * Sends event to handler
      * @param what Event code
      * @param obj  Optional event data
      */
     private void sendEvent(int what, Object obj) {
   
         if (what == EV_ERROR) {
             mLog.error((String)obj);
         }
         mHandler.sendMessage(mHandler.obtainMessage(what, obj));
     }
    
     
     /**
      * Handler to process events in user thread
      */
     private static class ListenerHandler extends Handler {
         private Listener mListener; 
         
         public ListenerHandler(Listener listener) {
             super();
             mListener = listener;
         }
     
         public void handleMessage(android.os.Message msg) {
             super.handleMessage(msg);
             switch (msg.what) {
             case EV_START:
                 mListener.onClientStart();
                 break;
             case EV_CONNECT:
                 mListener.onClientConnect();
                 break;
             case EV_CONNECTED:
                 mListener.onClientConnected();
                 break;
             case EV_RECV:
                 RxMsg rx = (RxMsg)msg.obj;
                 if (rx.mType == OP_TEXT) {
                     mListener.onClientRecv(rx.mType, rx.mText);
                 }
                 else {
                     mListener.onClientRecv(rx.mType, rx.mBytes);
                 }
                 break;
             case EV_SENT:
                 TxMsg tx = (TxMsg)msg.obj;
                 mListener.onClientSent(tx.mID);
                 break;
             case EV_ERROR:
                 mListener.onClientError((String)msg.obj);
                 break;
             case EV_STOP:
                 mListener.onClientStop();
                 break;
             }
         }
     }
   
     
     /**
      * Internal Logger interface
      */
     private interface Logger {
         void debug(String format, Object... args);
         void error(String format, Object... args);
     }
    
     
     /**
      * Logger which does not generate any logs
      */
     private static class NullLogger implements Logger {
         @Override
         public void debug(String format, Object... args) {}
         @Override
         public void error(String format, Object... args) {}
     }
    
     
     /**
      * Logger which uses standard Android log facility
      */
     private static class DefaultLogger implements Logger {
         private String mTag;
         
         public DefaultLogger(String tag) {
             mTag = tag;
         }
 
         @Override
         public void debug(String format, Object... args) {
             Log.println(Log.DEBUG, mTag, String.format(format, args));
         }
 
         @Override
         public void error(String format, Object... args) {
             Log.println(Log.ERROR, mTag, String.format(format, args));
         }
     }
 }


