 package com.github.donaldmunro.sysvqueue;
 
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 
 import com.sun.jna.NativeLong;
 import com.sun.jna.Platform;
 import com.sun.jna.Structure;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /*
 This file is part of SysVMsgQueue.
 
     SysVMsgQueue is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SysVMsgQueue is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SysVMsgQueue.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * Unix Sys V queues for Java programs.
  * Note: Uses Java Native Access (JNA) <@see https://github.com/twall/jna> to make API calls.
  * Requires jna.jar in the classpath.
  * @author Donald Munro
  **/
 public class JSysVMsgQueue
 //========================
 {
    final static Logger logger = LoggerFactory.getLogger(JSysVMsgQueue.class);
    final static int IPC_CREAT = 01000;
    final static int IPC_EXCL  = 02000;
    final static int IPC_NOWAIT = 04000;
    final static int MSG_NOERROR  = 010000;
    final static int IPC_RMID = 0;
    final static int IPC_STAT = 2;
    final static int EEXIST  = 17;
 
    int mKey =-1, mId =-1, mLastErrNo =0;
    String mLastErrorMessage = "";
 
    public interface LibC extends Library
    //========================================
    {
         LibC INSTANCE = (LibC)
             Native.loadLibrary(("c"), LibC.class);
 
         int msgget(int key, int msgflg);
         int msgsnd(int qid, Structure message, int size, int flags);
         int msgrcv(int qid, final Structure msgp, int size, NativeLong type,
                    int flags);
         int msgctl(int id, int cmd, Structure buf);
         int strerror_r(int errnum, ByteBuffer buf, int buflen);
     }
 
    public interface LibMsgCtl extends Library
    //========================================
    {
         LibMsgCtl INSTANCE = (LibMsgCtl) ((Platform.is64Bit())
                 ? Native.loadLibrary(("native/lib/libmsgctl-amd64.so"), LibMsgCtl.class)
                 : Native.loadLibrary(("native/lib/libmsgctl-i586.so"), LibMsgCtl.class));
 
         long msgqSize(int qid);
    }
 
    /**
     * Construct a SysVMsgQueue instance for a message queue with key specified by parameter <i>key</i>.
     * @param key - The SysV message queue key.
     */
    public JSysVMsgQueue(final int key)
    //----------------------------------
    {
       mKey = key;
    }
 
    /**
     * Optionally create and/or open a message queue. Queue will be created with permissions of 0666.
     * @param isCreate If <i>true</i> queue will be created if it does not exist.
     * @return <i>true</i> if queue was successfully opened or created.
     */
    public boolean open(final boolean isCreate)
    //------------------------------------------
    {
       return this.open(0666, isCreate);
    }
 
    /**
     * Optionally create and/or open a message queue.
     * @param permissions - Queue permissions
     * @param isCreate If <i>true</i> queue will be created if it does not exist.
     * @return <i>true</i> if queue was successfully opened or created.
     */
    public boolean open(final int permissions, final boolean isCreate)
    //----------------------------------------------------------------
    {
       //Pointer errno = NativeLibrary.getInstance("c").getFunction("errno");
       int flags = IPC_EXCL | permissions;
       if (isCreate)
          flags |= IPC_CREAT;
       mId = LibC.INSTANCE.msgget(mKey, flags);
       if (mId < 0)
          mLastErrNo = Native.getLastError();
       else
          mLastErrNo = 0;
       if ( (mId == -1) && (isCreate) && (mLastErrNo == EEXIST) )
       {
          flags = IPC_EXCL | permissions;
          mId = LibC.INSTANCE.msgget(mKey, flags);
          if (mId < 0)
             mLastErrNo = Native.getLastError();
          else
             mLastErrNo = 0;
       }
       _setLastErrorMessage(mLastErrNo);
       return (mId >= 0);
    }
 
    /**
     * Create a message queue. Queue will be created with permissions of 0666.
     * @return <i>true</i> if queue was successfully created.
     */
    public boolean create()
    //---------------------
    {
       return this.create(0666);
    }
 
    /**
     * Create a message queue.
     * @param permissions Queue permissions
     * @return <i>true</i> if queue was successfully created.
     */
    public boolean create(final int permissions)
    //------------------------------------------------
    {
       int flags = IPC_EXCL | IPC_CREAT | permissions;
       mId = LibC.INSTANCE.msgget(mKey, flags);
       mLastErrNo = Native.getLastError();
       _setLastErrorMessage(mLastErrNo);
       return (mId >= 0);
    }
 
    /**
     * Place a message on this queue. The message will be sent synchronously ie the call will
     * not return until the message has been sent ie the send will wait until the message queue
     * is no longer full if it was full when the send was invoked.
     * @param type Message type. See msgsnd documentation ie man msgsnd
     * @param message The message bytes.
     * @return <i>true</i> if message was sent.
     */
    public boolean send(final long type, final byte[] message)
    //--------------------------------------------------------
    {
       return this.send(type, message, true);
    }
 
    /**
     * Place a message on this queue.
     * @param type Message type. See msgsnd documentation ie man msgsnd
     * @param message The message bytes.
     * @param isWait If <i>true</i> then send message synchronously ie the call will
     * not return until the message has been sent ie the send will wait until the message queue
     * is no longer full if it was full when the send was invoked.. If <i>false</i> then call returns 
     * immediately with an error.
     * @return <i>true</i> if message was sent.
     */
    public boolean send(final long type, final byte[] message, final boolean isWait)
    //-----------------------------------------------------------------------------
    {
       MsgBuf msg = new MsgBuf();
       msg.type = new NativeLong(type);
       msg.message = message;
       int flags = 0;
       if (! isWait)
          flags = IPC_NOWAIT;
       int ret = LibC.INSTANCE.msgsnd(mId, msg, message.length, flags);
       mLastErrNo = Native.getLastError();
       _setLastErrorMessage(mLastErrNo);
       return  (ret != -1);
    }
 
    /**
     * Receive a message from this queue. This method will block until a message specified by <i>type</i>
     * is available.
     * @param type The type of message to receive. See msgrcv docs ie man msgrcv
     * @param messageLen The maximum length of the message to receive. If the message length
     * exceeds this length the message will be truncated.
     * @return An array of bytes containing the message or null if an error occurred.
     */
    public byte[] receive(final long type, final int messageLen)
    //----------------------------------------------------------
    {
       return this.receive(type, messageLen, (byte)0, true);
    }
 
    /**
     * Receive a message from this queue. This method will block until a message specified by <i>type</i>
     * is available.
     * @param type The type of message to receive. See msgrcv docs ie man msgrcv
     * @param messageLen The maximum length of the message to receive. If the message length
     * exceeds this length the message will be truncated.
     * @param fillchar Prefill the return array with this byte.
     * @return An array of bytes containing the message or null if an error occurred.
     */
    public byte[] receive(final long type, final int messageLen,
                          final byte fillchar)
    //----------------------------------------------------------
    {
       return this.receive(type, messageLen, fillchar, true);
    }
 
    /**
     * Receive a message from this queue.
     * @param type The type of message to receive. See msgrcv docs ie man msgrcv
     * @param messageLen The maximum length of the message to receive. If the message length
     * exceeds this length the message will be truncated.
     * @param fillchar Prefill the return array with this byte.
     * @param isWait If <i>true</i> then this method will block until a message specified by <i>type</i>
     * is available. If false then it will return immediately if there are no messages available.
     * @return An array of bytes containing the message or null if an error occurred.
     */
    public byte[] receive(final long type, final int messageLen,
                          final byte fillchar, final boolean isWait)
    //--------------------------------------------------------------
    {
       MsgBuf msg = new MsgBuf();
       msg.type = new NativeLong(type);
       byte[] buffer = new byte[messageLen];
       Arrays.fill(buffer, fillchar);
       msg.message = buffer;
       int flags = MSG_NOERROR;
       if (! isWait)
          flags |= IPC_NOWAIT;
       int ret = LibC.INSTANCE.msgrcv(mId, msg, messageLen, new NativeLong(type),
                                      flags);
       mLastErrNo = Native.getLastError();
       _setLastErrorMessage(mLastErrNo);
       if (ret == -1)
          return null;
       return Arrays.copyOf(msg.message, messageLen);
    }
 
    ExecutorService mAsyncPool = null;
 
    private Map<QueueReceivable, AsyncReceiveRecord> mAsyncMap = null;
 
    /**
     * Asynchronously receive a message from this queue. The receive call will be done in a thread
     * and the caller notified by the <b>received()</b> method in the callback class.
     * @param type The type of message to receive. See msgrcv docs ie man msgrcv
     * @param messageLen The maximum length of the message to receive. If the message length
     * exceeds this length the message will be truncated.
     * @param callback A callback class that implements the QueueReceivable interface. 
     */
    public void asyncReceive(final long type, final int messageLen,
                             final QueueReceivable callback)
    //------------------------------------------------------------------------------
    {
       this.asyncReceive(type, messageLen, (byte) 0, callback);
    }
 
    /**
     * Asynchronously receive a message from this queue. The receive call will be done in a thread
     * and the caller notified by the <b>received()</b> method in the callback class.
     * @param type The type of message to receive. See msgrcv docs ie man msgrcv
     * @param messageLen The maximum length of the message to receive. If the message length
     * exceeds this length the message will be truncated.
     * @param fillchar Prefill the return array with this byte.
     * @param callback A callback class that implements the QueueReceivable interface. 
     */
    public void asyncReceive(final long type, final int messageLen,
                             final byte fillchar, final QueueReceivable callback)
    //------------------------------------------------------------------------------
    {
       if (callback == null)
       {
          logger.error("PosixMsgQueue.asyncReceive: callback null");
          mLastErrNo = 65535;
          mLastErrorMessage = "PosixMsgQueue.asyncReceive: callback null";
          return;
       }
       if (mAsyncPool == null)
       {
          mAsyncPool = Executors.newCachedThreadPool(new ThreadFactory()
          //============================================================
          {
             public Thread newThread(Runnable r)
             //----------------------------------
             {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("asyncReceive");
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
 
             }
          });
          mAsyncMap = new HashMap<QueueReceivable, AsyncReceiveRecord>();
       }
       else
       {
          if (mAsyncMap.containsKey(callback))
             return;
       }
       final JSysVMsgQueue me = this;
       AsyncReceiveThread t = new AsyncReceiveThread(type, messageLen, fillchar,
                                                     me, callback);
       Future f = mAsyncPool.submit(t);
       mAsyncMap.put(callback, new AsyncReceiveRecord(f, t));
    }
 
    /**
     * Cancel a previous call to asyncReceive.
     * @param callback The callback class used in the asyncReceive call.
     * @return <i>true</i> if call was cancelled.
     */
    public boolean cancelAsyncReceive(final QueueReceivable callback)
    //------------------------------------------------------------
    {
       boolean isCancelled = false;
       if (callback == null)
       {
          logger.error("PosixMsgQueue.cancelAsyncReceive: callback null");
          return false;
       }
       if (mAsyncMap != null)
       {
          AsyncReceiveRecord arr = mAsyncMap.get(callback);
          if (arr != null)
          {
             AsyncReceiveThread t = arr.thread;
             t.setTerminated();
             if (size() <= 0)
             {
                byte[] empty = new byte[1];
                empty[0] = 0;
                send(t.getType(), empty, true);
             }
             Future f = arr.future;
             if ( (! f.isDone()) && (! f.isCancelled()) )
             {
                 isCancelled = f.cancel(true);
                 if (isCancelled)
                    mAsyncMap.remove(callback);
             }
             else
                isCancelled = true;
          }
       }
       return isCancelled;
    }
 
    /**
     * Delete this message queue.
     * @return 
     */
    public boolean delete()
    //---------------------
    {
       int ret = LibC.INSTANCE.msgctl(mId, IPC_RMID, null);
       mLastErrNo = Native.getLastError();
       _setLastErrorMessage(mLastErrNo);
       return  (ret != -1);
    }
 
 //   public long size()
 //   //----------------
 //   {
 //      MsgInfo info = new MsgInfo();
 //      int ret = LibC.INSTANCE.msgctl(mId, IPC_STAT, info);
 //      mLastErrNo = Native.getLastError();
 //      _setLastErrorMessage(mLastErrNo);
 //      return info.size;
 //   }
 
    private boolean mIsMsgCtlAvail = true;
 
    /**
     * Return the size of this queue.
     * Note: Depends on native/lib/libmsgctl-*.so due to difficulty in defining the variable size 
     * struct used by msgctl in JNA.
     * @return The size of the message queue or -1 if an error occurred.
     */
    public long size()
    //----------------
    {
       if (! mIsMsgCtlAvail)
          return -1;
       try
       {
          return LibMsgCtl.INSTANCE.msgqSize(mId);
       }
       catch (UnsatisfiedLinkError e)
       {
          logger.error("Could not find native/lib/libmsgctl-{arch}.so. " +
                        "size() method will not be available", e);
          mIsMsgCtlAvail = false;
          return -1;
       }
    }
 
    /**
     * @return This queues id.
     */
    public int getId() { return mId; }
 
    /**
     * @return This queues key.
     */
    public int getKey() { return mKey; }
 
    /**
     * @return The last error number (errno) from the last SysV queue API call.
     */
    public int getLastErrNo() { return mLastErrNo; }
 
    /**
     * @return The last error message (errno) from the last SysV queue API call.
     */
    public String getLastErrorMessage() { return mLastErrorMessage; }
 
    @Override
    public String toString()
    //----------------------
    {
       return "SysVMsgQueue[ Key = " + mKey + ", Id = " + mId + " Last Error " +
               mLastErrNo + ":" + mLastErrorMessage + "]";
    }
 
    private void _setLastErrorMessage(int errno)
    //-----------------------------------------
    {
       if (errno == 0)
       {
          mLastErrorMessage = "";
          return;
       }
       byte as[] = new byte[4096];
       ByteBuffer errbuf = ByteBuffer.wrap(as);
       int err = LibC.INSTANCE.strerror_r(errno, errbuf, 4096);
       if (err >= 0)
          mLastErrorMessage = errbuf.asCharBuffer().toString().replace('\0', ' ').trim();
       else
          mLastErrorMessage = Integer.toString(errno) + " (" + err + ")";
    }
 
    /**
     * Interface used for callback by asyncReceive methods.
     */
    public interface QueueReceivable
    //===============================
    {
       /**
        * The callback method. When reporting an error the message will be null and the receivedType
        * will be the errno for the API call.
        * @param queue The SysVMsgQueue instance
        * @param type The message type specified in the asyncReceive method
        * @param receivedType The type of the message received. If an error occurs this will 
        * equal the errno for the API call.
        * @param message The message bytes. Will be null if an error occurred.
        */
       public void received(JSysVMsgQueue queue, long type, long receivedType,
                            byte[] message);
    }
 
    class AsyncReceiveRecord
    //======================
    {
       public Future future;
       public AsyncReceiveThread thread;
 
       public AsyncReceiveRecord(Future future, AsyncReceiveThread thread)
       {
          this.future = future;
          this.thread = thread;
       }
    }
 
    class AsyncReceiveThread implements Runnable
    //==========================================
    {
       volatile boolean isTerminated = false;
       long type;
       int messageLen;
       byte fillchar;      
       JSysVMsgQueue queue;
       QueueReceivable callback;
       
       public AsyncReceiveThread(long type, int messageLen, byte fillchar,
                                 JSysVMsgQueue queue, QueueReceivable callback)
       //----------------------------------------------------------------------
       {
          this.type = type;
          this.messageLen = messageLen;
          this.fillchar = fillchar;
          this.queue = queue;
          this.callback = callback;
       }
       
       public void setTerminated() { this.isTerminated = true; }
 
       public long getType() { return type; }
 
       public void run()
       //---------------
       {
          MsgBuf msg = new MsgBuf();
          byte[] buffer = new byte[messageLen];
          while (! isTerminated)
          {
             try
             {
                if (isTerminated)
                   break;
                Arrays.fill(buffer, fillchar);
                msg.type = new NativeLong(type);
                msg.message = buffer;
                int flags = MSG_NOERROR;
                int ret = LibC.INSTANCE.msgrcv(mId, msg, messageLen, new NativeLong(type),
                                               flags); 
                if (ret == -1)
                {
                   int errno = Native.getLastError();
                   callback.received(queue, type, errno, null);
                   mLastErrNo = errno;
                   _setLastErrorMessage(errno);
                }
                else
                   callback.received(queue, type, msg.type.longValue(),
                                     Arrays.copyOf(msg.message, messageLen));
             }
             catch (Exception e)
             {
                logger.error("", e);
                continue;
             }
          }
          System.out.println("AsyncReceiveThread done");
       }
       
    }
 
    public static class MsgBuf extends Structure
    //==========================================
    {
       public NativeLong type;
       public byte[] message;
    }
 
 // Implementation specific eg Linux has bits/msq.h
 // #if __WORDSIZE == 32 unsigned long int __unused1; #endif
 // Using a native library with a size method instead to implement size().
 //   public static class MsgInfo  extends Structure
 //   //=============================================
 //   {
 //      public int key;       /* Key supplied to msgget(2) */
 //      public int uid;         /* Effective UID of owner */
 //      public int gid;         /* Effective GID of owner */
 //      public int cuid;        /* Effective UID of creator */
 //      public int cgid;        /* Effective GID of creator */
 //      public short mode;        /* Permissions */
 //      public short seq;
 //      public long lastSendTime;    /* Time of last msgsnd(2) */
 //      public long lastReceiveTime;    /* Time of last msgrcv(2) */
 //      public long lastChangeTime;    /* Time of last change */
 //      public long sizeInBytes; /* Current number of bytes in  queue (non-standard) */
 //      public long size;     /* Current number of messages in queue */
 //      public long maxBytes;   /* Maximum number of bytes allowed in queue */
 //      public int lastSendPid;    /* PID of last msgsnd(2) */
 //      public int lastReceivePid;    /* PID of last msgrcv(2) */
 //   };
 
    public static void main(String[] args)
    {
       JSysVMsgQueue queue = new JSysVMsgQueue(1024);
       if (! queue.open(true))
       {
          System.err.println("Open (create) failed " + queue.getLastErrNo() +
                             ": " + queue.getLastErrorMessage());
          return;
       }
 
       System.out.println("Size " + queue.size());
 
       String s = "Hello world !!!!";
       byte[] message;
       try { message = s.getBytes("UTF-8"); } catch (Exception e) { message = s.getBytes(); }
 //  zero terminated string
 //      try
 //      {
 //         message = Native.toByteArray(s, "UTF-8");
 //      }
 //      catch (Exception e)
 //      {
 //         message = Native.toByteArray(s);
 //      }
       if (queue.send(1L, message))
       {
          System.out.println("Size " + queue.size());
 
          int len = message.length;
          message = queue.receive(1, len, (byte)32, true);
          if (message != null)
          {
             s = new String(message);
             System.out.println("Received " + s);
          }
          else
             System.err.println("Error in receive "+ queue.getLastErrNo() +
                                ": " + queue.getLastErrorMessage());
          System.out.println("Size " + queue.size());
       }
       else
          System.err.println("Error in send "+ queue.getLastErrNo() +
                                ": " + queue.getLastErrorMessage());
 
       QueueReceivable asyncCallback = new QueueReceivable()
       {
          public void received(JSysVMsgQueue queue, long type, long receivedType,
                               byte[] message)
          {
             System.out.println("Async callback received: " + new String(message).trim());
             System.out.println("Size " + queue.size());
          }
       };
       queue.asyncReceive(1L, 4096, (byte)32, asyncCallback);
       for (int i=0; i<10; i++)
       {
          s = "Message " + i;
          try { message = s.getBytes("UTF-8"); } catch (Exception e) { message = s.getBytes(); }
          queue.send(1L, message);
       }
       try { Thread.sleep(5000); } catch (Exception e) {}
       queue.cancelAsyncReceive(asyncCallback);
       try { Thread.sleep(500); } catch (Exception e) {}
       System.out.println("Queue delete: " + queue.delete());
    }
 }
