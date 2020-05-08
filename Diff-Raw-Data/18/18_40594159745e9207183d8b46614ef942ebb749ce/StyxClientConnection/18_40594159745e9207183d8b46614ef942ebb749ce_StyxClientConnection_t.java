 package com.v2soft.styxlib.library;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.nio.channels.SocketChannel;
 import java.security.InvalidParameterException;
 import java.util.LinkedList;
 import java.util.concurrent.TimeoutException;
 
 import com.v2soft.styxlib.library.core.Messenger;
 import com.v2soft.styxlib.library.core.Messenger.StyxMessengerListener;
 import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
 import com.v2soft.styxlib.library.exceptions.StyxException;
 import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
 import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
 import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
 import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
 import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
 import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
 import com.v2soft.styxlib.library.messages.base.StyxMessage;
 import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
 import com.v2soft.styxlib.library.messages.base.enums.MessageType;
 import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
 
 /**
  * Styx client conection
  * @author V.Shcryabets<vshcryabets@gmail.com>
  *
  */
 public class StyxClientConnection 
 implements Closeable, StyxMessengerListener {
     //---------------------------------------------------------------------------
     // Constants
     //---------------------------------------------------------------------------
     public static final String PROTOCOL = "9P2000";
     public static final int DEFAULT_TIMEOUT = 10000;
     //---------------------------------------------------------------------------
     // Class fields
     //---------------------------------------------------------------------------
     private StyxFile mRoot;
     private InetAddress mAddress;
     private String mUserName;
     private String mPassword;
     private String mMountPoint;
     private int mPort;
     private int mTimeout = DEFAULT_TIMEOUT;
     private boolean mSSL;
     private boolean mNeedAuth;
     private boolean isConnected, isAttached;
     private Messenger mMessenger;
     private int mIOBufSize = 8192;
     private long mAuthFID = StyxMessage.NOFID;
     private StyxQID mAuthQID;
     private StyxQID mQID;
     private long mFID = StyxMessage.NOFID;
     private ActiveFids mActiveFids = new ActiveFids();
 
     public StyxClientConnection() {
         this(null, 0, false, null, null);
     }
 
     public StyxClientConnection(InetAddress address, int port, boolean ssl)
     {
         this(address, port, ssl, null, null);
         setConnected(false);
     }
 
     public StyxClientConnection(InetAddress address, int port, boolean ssl, String username, String password) {
         mAddress = address;
         mPort = port;
         mSSL = ssl;
         mUserName = username;
         mPassword = password;
     }
 
     public boolean connect() 
             throws IOException, StyxException, InterruptedException, TimeoutException {
         return connect(mAddress, mPort, mSSL, mUserName, mPassword);
     }
 
     public boolean connect(InetAddress address, int port, boolean ssl)
             throws IOException, StyxException, InterruptedException, TimeoutException {
         return connect(address, port, ssl, null, null);
     }
 
     /**
      * Connect to server with specified parameters
      * @param address server name
      * @param port server port
      * @param ssl use SSL
      * @param username user name
      * @param password password 
      * @return true if connected
      * @throws IOException
      * @throws StyxException
      * @throws TimeoutException 
      */
     public boolean connect(InetAddress address, int port, boolean ssl, String username, String password)
             throws IOException, StyxException, InterruptedException, TimeoutException {
         mAddress = address;
         mPort = port;
         mSSL = ssl;
         mUserName = username;
         mPassword = password;
         mMountPoint = "/";
         mNeedAuth = (username != null);
         SocketAddress sa = new InetSocketAddress(address, port);
         SocketChannel channel = SocketChannel.open(sa);
         channel.configureBlocking(true);
         Socket socket = channel.socket();
         socket.setSoTimeout(mTimeout);
         mMessenger = new Messenger(channel, mIOBufSize, this);
 
         sendVersionMessage();
         setConnected(socket.isConnected());
         return socket.isConnected();
     }
 
     public StyxFile getRoot() throws StyxException, InterruptedException, TimeoutException, IOException
     {
         if (mRoot == null)
             mRoot = new StyxFile(this,mFID);
         return mRoot;
     }
 
     public Messenger getMessenger() {
         return mMessenger;
     }		
 
     public ActiveFids getActiveFids()
     {
         return mActiveFids;
     }
 
     public long getIOBufSize()
     {
         return mIOBufSize;
     }
 
     public InetAddress getAddress()
     {
         return mAddress;
     }
 
     public String getUserName()
     {
         return mUserName;
     }
 
     public String getPassword()
     {
         return mPassword;
     }
 
     public String getMountPoint()
     {
         return mMountPoint;
     }
 
     public int getPort()
     {
         return mPort;
     }
 
     public boolean isSSL()
     {
         return mSSL;
     }
 
     public boolean isNeedAuth()
     {
         return mNeedAuth;
     }
 
     /**
      * 
      * @return FID of root folder
      */
     public long getFID()
     {
         return mFID;
     }
 
     public StyxQID getQID()
     {
         return mQID;
     }
 
     public long getAuthFID()
     {
         return mAuthFID;
     }
 
     public StyxQID getAuthQID()
     {
         return mAuthQID;
     }
 
     /**
      * Send TClunk message (release FID)
      * @param fid
      * @throws InterruptedException
      * @throws StyxException
      * @throws TimeoutException
      * @throws IOException 
      */
     public void clunk(long fid) 
             throws InterruptedException, StyxException, TimeoutException, IOException {
         final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, fid);
         mMessenger.send(tClunk);
     }
 
     /**
      * Send TRemove message for specefied FID, note that FID will be released even if remove failed.
      * @param fid FID of the file that should be removed
      * @throws TimeoutException 
      * @throws InterruptedException 
      * @throws StyxErrorMessageException 
      * @throws IOException 
      */
     public void remove(long fid) throws InterruptedException, TimeoutException, StyxErrorMessageException, IOException {
         StyxTMessageFID tRemove = new StyxTMessageFID(MessageType.Tremove, MessageType.Rremove, fid);
         mMessenger.send(tRemove);
         StyxMessage rMessage = tRemove.waitForAnswer(mTimeout);
         StyxErrorMessageException.doException(rMessage);
     }    
     /**
      * Restart session with server
      * @throws InterruptedException
      * @throws StyxException
      * @throws IOException
      * @throws TimeoutException
      */
     public void sendVersionMessage()
             throws InterruptedException, StyxException, IOException, TimeoutException {
         // release atached FID
         if ( mFID != StyxMessage.NOFID ) {
             try {
                 clunk(mFID);
             } catch (Exception e) {
                 throw new IOException(e);
             }
             mFID = StyxMessage.NOFID;
         }
 
         StyxTVersionMessage tVersion = new StyxTVersionMessage(mIOBufSize,PROTOCOL);
         mMessenger.send(tVersion);
 
         StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
         StyxErrorMessageException.doException(rMessage);
         StyxRVersionMessage rVersion = (StyxRVersionMessage)rMessage;
         if (rVersion.getMaxPacketSize() < mIOBufSize)
             mIOBufSize = (int)rVersion.getMaxPacketSize();
         mActiveFids.clean();
         if (isNeedAuth())
             sendAuthMessage();
         else
             sendAttachMessage();
     }
 
     private void sendAuthMessage()
             throws InterruptedException, StyxException, IOException, TimeoutException {
         mAuthFID = getActiveFids().getFreeFid();
 
         StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID);
         tAuth.setUserName(getUserName());
         tAuth.setMountPoint(getMountPoint());
         mMessenger.send(tAuth);
 
         StyxMessage rMessage = tAuth.waitForAnswer(mTimeout);
         StyxErrorMessageException.doException(rMessage);
         StyxRAuthMessage rAuth = (StyxRAuthMessage) rMessage;
         mAuthQID = rAuth.getQID();
 
         // TODO uncomment later
         //        StyxOutputStream output = new StyxOutputStream((new StyxFile(this, 
         //                ((StyxTAuthMessage)tMessage).getAuthFID())).openForWrite());
         //        output.writeString(getPassword());
         //        output.flush();
 
         sendAttachMessage();
     }
 
     private void sendAttachMessage()
             throws InterruptedException, StyxException, TimeoutException, IOException {
         mFID = getActiveFids().getFreeFid();
         StyxTAttachMessage tAttach = new StyxTAttachMessage(getFID(), getAuthFID(),
                 getUserName(),
                 getMountPoint());
         mMessenger.send(tAttach);
 
         StyxMessage rMessage = tAttach.waitForAnswer(mTimeout);
         StyxErrorMessageException.doException(rMessage);
         StyxRAttachMessage rAttach = (StyxRAttachMessage) rMessage;
         mQID = rAttach.getQID();
         setAttached(true);
     }
 
     @Override
     public void close() throws IOException {
         if ( mMessenger != null ) {
             mMessenger.close();
             mMessenger = null;
         }
     }
 
     public class ActiveFids
     {
         private LinkedList<Long> mAvailableFids = new LinkedList<Long>();
         private long mLastFid = 0L;
 
         /**
          * @return Return free FID
          */
         protected long getFreeFid() {
             synchronized (mAvailableFids) {
                 if (!mAvailableFids.isEmpty())
                     return mAvailableFids.poll();
                 mLastFid++;
                 if(mLastFid > Consts.MAXUNINT)
                     mLastFid = 0;
                 return mLastFid;
             }
         }
 
         protected boolean releaseFid(long fid) {
             synchronized (mAvailableFids) {
                 if (fid == StyxMessage.NOFID)
                     return false;
                 if ( mAvailableFids.contains(fid)) {
                     throw new InvalidParameterException(
                             String.format("Something goes wrong, this FID(%d) already has been released", fid));
                 }
                 return mAvailableFids.add(fid);
             }
         }
         protected void clean() {
             mAvailableFids.clear();
             mLastFid = 0;
         }
     }
     //-------------------------------------------------------------------------------------
     // Getters
     //-------------------------------------------------------------------------------------
     public int getTimeout() {return mTimeout;}
     public boolean isAttached() {return isAttached;}
     //-------------------------------------------------------------------------------------
     // Setters
     //-------------------------------------------------------------------------------------
     public void setTimeout(int mTimeout) {
         this.mTimeout = mTimeout;
     }
     public void setAttached(boolean isAttached) {
         this.isAttached = isAttached;
     }
     public boolean isConnected() {
         return isConnected;
     }
 
     public void setConnected(boolean isConnected) {
         this.isConnected = isConnected;
     }
     public void setAddressPort(InetAddress address, int port) {
         mAddress = address;
         mPort = port;
     }
     //-------------------------------------------------------------------------------------
     // Messenger listener
     //-------------------------------------------------------------------------------------
     @Override
     public void onSocketDisconected() {
         setConnected(false);
     }
 
     @Override
     public void onTrashReceived() {
         //something goes wrong, we should restart protocol
         setAttached(false);
         try {
             sendVersionMessage();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void onFIDReleased(long fid) {
         mActiveFids.releaseFid(fid);
     }
 }
