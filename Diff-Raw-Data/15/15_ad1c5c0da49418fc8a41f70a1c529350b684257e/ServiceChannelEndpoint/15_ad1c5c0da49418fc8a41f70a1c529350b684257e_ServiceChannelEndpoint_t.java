 package edu.washington.cs.oneswarm.f2f.servicesharing;
 
 import java.util.Hashtable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.gudy.azureus2.core3.util.DirectByteBuffer;
 
 import com.aelitis.azureus.core.peermanager.messaging.MessageException;
 
 import edu.washington.cs.oneswarm.f2f.messaging.OSF2FChannelDataMsg;
 import edu.washington.cs.oneswarm.f2f.messaging.OSF2FHashSearch;
 import edu.washington.cs.oneswarm.f2f.messaging.OSF2FHashSearchResp;
 import edu.washington.cs.oneswarm.f2f.messaging.OSF2FMessage;
 import edu.washington.cs.oneswarm.f2f.network.FriendConnection;
 import edu.washington.cs.oneswarm.f2f.network.OverlayEndpoint;
 import edu.washington.cs.oneswarm.f2f.network.OverlayTransport;
 
 /**
  * This class represents one Friend connection channel used for multiplexed
  * service channels.
  * Functionality extends from {@code OverlayEndpoint}, the additional
  * functionality from
  * this class is that received data is forwarded to the aggregate service
  * connection, and
  * outstanding sent data is tracked for congestion control across channels.
  * 
  * @author willscott
  * 
  */
 public class ServiceChannelEndpoint extends OverlayEndpoint {
     public final static Logger logger = Logger.getLogger(ServiceChannelEndpoint.class.getName());
     private static final byte ss = 0;
     protected AbstractServiceConnection serviceAggregator;
     protected final Hashtable<SequenceNumber, DirectByteBuffer> sentMessages;
     private int outstandingBytes;
 
     public ServiceChannelEndpoint(AbstractServiceConnection aggregator,
             FriendConnection connection, OSF2FHashSearch search, OSF2FHashSearchResp response,
             boolean outgoing) {
         super(connection, response.getPathID(), 0, search, response, outgoing);
         logger.info("Service Channel Endpoint Created.");
         this.serviceAggregator = aggregator;
 
         this.sentMessages = new Hashtable<SequenceNumber, DirectByteBuffer>();
         this.outstandingBytes = 0;
 
         this.started = true;
         friendConnection.isReadyForWrite(new OverlayTransport.WriteQueueWaiter() {
             @Override
             public void readyForWrite() {
                 logger.info("friend connection marked ready for write.");
                 serviceAggregator.channelReady(ServiceChannelEndpoint.this);
             }
         });
     }
 
     @Override
     public void start() {
     }
 
     @Override
     public boolean isStarted() {
         return friendConnection.isHandshakeReceived();
     }
 
     @Override
     protected void destroyBufferedMessages() {
         // No buffered messages to destroy.
         for (DirectByteBuffer b : this.sentMessages.values()) {
             b.returnToPool();
         }
         this.sentMessages.clear();
         this.outstandingBytes = 0;
     }
 
     @Override
     public void cleanup() {
         serviceAggregator.removeChannel(this);
     };
 
     @Override
     protected void handleDelayedOverlayMessage(OSF2FChannelDataMsg msg) {
         if (logger.isLoggable(Level.FINEST)) {
             logger.finest("incoming message: " + msg.getDescription());
         }
 
         if (closed) {
             return;
         }
         if (!started) {
             start();
         }
         logger.fine("Service channel msg recieved.");
         // We need to create a new message here and transfer the payload over so
         // the buffer won't be returned while the packet is in the queue.
         try {
           OSF2FServiceDataMsg newMessage = OSF2FServiceDataMsg.fromChannelMessage(msg);
            logger.fine("Received msg with sequence number " + newMessage.getSequenceNumber());
             serviceAggregator.writeMessageToServiceBuffer(newMessage);
         } catch(MessageException m) {
             return;
         }
     }
 
     public void writeMessage(SequenceNumber num, DirectByteBuffer buffer) {
         this.sentMessages.put(num, buffer);
         this.outstandingBytes += buffer.remaining(ss);
         OSF2FServiceDataMsg msg = new OSF2FServiceDataMsg(OSF2FMessage.CURRENT_VERSION, channelId,
                 num.getNum(), buffer);
         long totalWritten = buffer.remaining(DirectByteBuffer.SS_MSG);
        logger.fine("Wrote msg to network with sequence number " + num.getNum());
        super.writeMessage(msg);
         bytesOut += totalWritten;
     }
 
     public int getOutstanding() {
         return this.outstandingBytes;
     }
 
     public DirectByteBuffer getMessage(SequenceNumber num) {
         return this.sentMessages.get(num);
     }
 }
