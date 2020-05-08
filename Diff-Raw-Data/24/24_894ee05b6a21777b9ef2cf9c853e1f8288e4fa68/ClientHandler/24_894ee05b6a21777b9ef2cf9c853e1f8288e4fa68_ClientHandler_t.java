 package org.kari.call;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.rmi.RemoteException;
 
 import org.apache.log4j.Logger;
 import org.kari.call.event.Call;
 import org.kari.call.event.Result;
 
 /**
  * Handles communication with one socket in client side
  *
  * @author kari
  */
 public final class ClientHandler extends Handler {
     private static final Logger LOG = Logger.getLogger(CallConstants.BASE_PKG + ".client_handler");
     
    private Object mLastSessionId;
    

     public ClientHandler(
             Socket pSocket, 
             CallClient pClient) 
         throws IOException 
     {
         super(pSocket, 
                 pClient.getIOFactory(), 
                 pClient.isCounterEnabled(),
                 pClient.isReuseObjectStream(),
                 pClient.getCallCompressThreshold());
     }
     
    public Object getLastSessionId() {
        return mLastSessionId;
    }

    public void setLastSessionId(Object pLastSessionId) {
        mLastSessionId = pLastSessionId;
    }

     /**
      * Invoke call, retrying call is upto caller
      * 
      * @throws RetryCallException if retryable call error occurred
      */
     public Object invoke(Call pCall) 
         throws 
             RetryCallException,
             Throwable 
     {
         Result result;
 
         boolean acked = false;
         if (mCounterEnabled) {
             mCountOut.markCount();
             mCountIn.markCount();
         }
         try {
             resetByteOut();
             pCall.send(this, mOut);
             
             // handle ack
             Result ack = readResult();
             if (ack.getType() == CallType.ACK_CALL_RECEIVED) {
                 acked = true;
                 result = readResult();
             } else {
                 // error if not ack
                 ack.getResult();
                 result = null;
             }
         } catch (Throwable e) {
             // suicide; failed write/read socket or invalid result type received
             // thus input is already in invalid state
             // => However, should differentiate phase; if call was sent
             //    succesfully to server, then server may have actually handled it.
             //    In that case transparent "retry" is not possible
             kill();
             if (acked) {
                 throw new RemoteException("Failed to access server", e);
             } else {
                 throw new RetryCallException("Retry call", e);
             }
         } finally {
             if (mCounterEnabled) {
                 if (TRACE) LOG.info("out=" + mCountOut.getMarkSize() + ", in=" + mCountIn.getMarkSize());
                 mCounter.add(mCountOut.getCount(), mCountIn.getCount());
             }
             
             if (!mRunning) {
                 free();
             }
         }
         
         return result.getResult();
     }
 
     /**
      * Read single result from server
      */
     private Result readResult() 
         throws IOException,
             RemoteException, 
             Exception 
     {
         int code = mIn.read();
         CallType type = CallType.resolve(code);
         Result result = (Result)type.create();
         result.receive(this, mIn);
         return result;
     }
 }
 
