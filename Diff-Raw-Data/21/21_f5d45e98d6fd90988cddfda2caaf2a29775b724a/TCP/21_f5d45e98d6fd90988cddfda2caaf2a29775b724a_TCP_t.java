 package nl.vu.cs.cn;
 
 import android.util.Log;
 
 import java.io.IOException;
 
 import nl.vu.cs.cn.IP.IpAddress;
 import nl.vu.cs.cn.tcp.segment.RetransmissionSegment;
 import nl.vu.cs.cn.tcp.segment.Segment;
 import nl.vu.cs.cn.tcp.segment.SegmentHandler;
 import nl.vu.cs.cn.tcp.segment.SegmentReceiver;
 import nl.vu.cs.cn.tcp.TransmissionControlBlock;
 import nl.vu.cs.cn.tcp.segment.SegmentUtil;
 
 /**
  * This class represents a TCP stack.
  */
 public class TCP {
 
     private static final short CLIENT_LOCAL_PORT = 3110;    // local port used by client
 
     private String TAG = "TCP";
 
    // protected ip stack and tcb so test cases can use it
     protected IP ip;
    protected TransmissionControlBlock tcb;
 
     private boolean sendIssued;
 
     private SegmentReceiver segmentReceiver;
     private SegmentHandler segmentHandler;
 
 
     /**
      * Constructs a TCP stack for the given virtual address.
      * The virtual address for this TCP stack is then
      * 192.168.1.address.
      *
      * @param address The last octet of the virtual IP address 1-254.
      * @throws IOException if the IP stack fails to initialize.
      */
     public TCP(int address) throws IOException {
         ip = new IP(address);
     }
 
     /**
      * @return a new client socket for this stack
      */
     public Socket socket() {
         TAG += " [client]";
         tcb = new TransmissionControlBlock(ip, false);
 
         initSegmentReceiver();
 
         return new Socket();
     }
 
     /**
      * @return a new server socket for this stack bound to the given port
      * @param port the port to bind the socket to.
      */
     public Socket socket(int port) {
         TAG += " [server]";
         tcb = new TransmissionControlBlock(ip, true);
 
         initSegmentReceiver();
 
         return new Socket(port);
     }
 
     /**
      * Create a segment handler, and start new thread to receive messages
      */
     private void initSegmentReceiver(){
         segmentHandler = new SegmentHandler(tcb, ip);
         segmentReceiver = new SegmentReceiver(segmentHandler, ip);
         segmentReceiver.run();
     }
 
     /**
      * ??
      * @param buf
      * @param offset
      * @param len
      * @return
      */
     private int send(byte[] buf, int offset, int len) {
         switch(tcb.getState()){
             case CLOSED:
                 Log.e(TAG, "Error in send(): connection does not exist");
                 return -1;
             case LISTEN:
                 Log.e(TAG, "Error in send(): switching from passive to active open is not supported");
                 return -1;
             case SYN_SENT:
             case SYN_RECEIVED:
                 // Queue data for transmission after entering ESTABLISHED state
                 int bytesQueued = tcb.queueDataForTransmission(buf, offset, len);
                 if(bytesQueued < len){
                     // this will (should) never happen. Adding data to queue always succeeds
                     Log.e(TAG, "Error in send(): insufficient resources");
                     return -1;
                 }
                 Log.v(TAG, "send(): data queued for transmission after entering ESTABLISHED");
                 // TODO: should we already return, or wait for ACK?
                 return bytesQueued;
             case ESTABLISHED:
             case CLOSE_WAIT:
                 // segmentize buffer and send it with a piggybacked ACK
                 // TODO: implement
                 Log.v(TAG, "send(): buffer segmentized and piggybacked with ACK");
                 sendIssued = true;
                 // TODO: don't forget return here
             default:
                 Log.e(TAG, "Error in send(): connection closing");
                 return -1;
         }
     }
 
     /**
      * ??
      * @param buf
      * @param offset
      * @param maxlen
      * @return
      */
     private int receive(byte[] buf, int offset, int maxlen) {
         switch(tcb.getState()){
             case CLOSED:
                 Log.e(TAG, "Error in receive(): connection does not exist");
                 return -1;
             case LISTEN:
             case SYN_SENT:
             case SYN_RECEIVED:
                 Log.v(TAG, "receive(): call queued until state is ESTABLISHED");
                 // TODO: Queue receive for processing after entering ESTABLISHED state
                 // e.g. block until state == established, and process receive
                 // TODO: don't forget return here.
             case ESTABLISHED:
             case FIN_WAIT_1:
             case FIN_WAIT_2:
                 if(!tcb.hasDataToProcess()){
                     Log.v(TAG, "receive(): call queued until segments arrive");
                     // TODO: Queue receive for processing after segments come in
                     // e.g. block until new data is received
                 }
                 Log.v(TAG, "receive(): returning data from processing queue");
                 return tcb.getDataToProcess(buf, offset, maxlen);
             case CLOSE_WAIT:
                 if(!tcb.hasDataToProcess()){
                     Log.e(TAG, "Error in receive(): connection closing");
                     return -1;
                 }
                 Log.v(TAG, "receive(): returning data from processing queue");
                 return tcb.getDataToProcess(buf, offset, maxlen);
             default:
                 Log.e(TAG, "Error in receive(): connection closing");
                 return -1;
         }
     }
 
     /**
      * Get the state the TCP stack is currently in.
      * @return
      */
     protected TransmissionControlBlock.State getState(){
         return tcb.getState();
     }
 
     /**
      * This class represents a TCP socket.
      *
      */
     public class Socket {
 
         /**
          * Construct a client socket and setup a transmission control block with
          * local socket information.
          */
         private Socket() {
             tcb.setLocalSocketInfo(ip.getLocalAddress(), CLIENT_LOCAL_PORT);
         }
 
         /**
          * Construct a server socket bound to the given local port and setup
          * transmission control block with a local socket information.
          *
          * @param port the local port to use
          */
         private Socket(int port) {
             tcb.setLocalSocketInfo(ip.getLocalAddress(), (short)port);
         }
 
         /**
          * Connect this socket to the specified destination and port.
          *
          * @param dst the destination to connect to
          * @param port the port to connect to
          * @return true if the connect succeeded.
          */
         public boolean connect(IpAddress dst, int port) {
             if(tcb.getState() == TransmissionControlBlock.State.LISTEN){
                 Log.e(TAG, "Error in connect(): switching from passive to active open is not supported");
                 return false;
             } else if(tcb.getState() != TransmissionControlBlock.State.CLOSED){
                 Log.e(TAG, "Error in connect(): connection already exists");
                 return false;
             } else if(dst == null || port == 0){
                 Log.e(TAG, "Error in connect(): foreign socket unspecified");
                 return false;
             } else if(tcb.hasForeignSocketInfo()){
                 Log.e(TAG, "Error in connect(): insufficient resources (connect() was already called before)");
                 return false;
             }
 
             // at this point we are sure we are in the CLOSED state
 
             tcb.setForeignSocketInfo(dst, (short)port);
 
             // sending SYN until entering SYN SENT state should be synchronized
             synchronized(tcb) {
                 // send SYN packet <SEQ=ISS><CTL=SYN>
                 int iss = tcb.getInitialSendSequenceNumber();
                 Segment segment = SegmentUtil.getSYNPacket(tcb, iss);
                 IP.Packet packet = IPUtil.getPacket(segment);
                 try {
                     Log.v(TAG, "Sending: " + segment.toString());
                     ip.ip_send(packet);
                 } catch (IOException e) {
                     Log.e(TAG, "Error while sending SYN", e);
                     return false;
                 }
 
                 tcb.addToRetransmissionQueue(new RetransmissionSegment(segment));
 
                 tcb.setSendUnacknowledged(iss);
                 tcb.advanceSendNext(segment.getLen());
 
                 tcb.enterState(TransmissionControlBlock.State.SYN_SENT);
             }
 
             // Wait for either the ESTABLISHED state (success) or the CLOSED state (error)
             Log.v(TAG, "connect(): waiting until state becomes ESTABLISHED or CLOSED");
             tcb.waitForStates(TransmissionControlBlock.State.ESTABLISHED,
                     TransmissionControlBlock.State.CLOSED);
 
             return tcb.getState() == TransmissionControlBlock.State.ESTABLISHED;
         }
 
         /**
          * Accept a connection on this socket.
          * This call blocks until a connection is made.
          */
         public void accept() {
             tcb.enterState(TransmissionControlBlock.State.LISTEN);
 
             Log.v(TAG, "accept(): waiting until state becomes ESTABLISHED");
             tcb.waitForStates(TransmissionControlBlock.State.ESTABLISHED);
         }
 
         /**
          * Reads bytes from the socket into the buffer.
          * This call is not required to return maxlen bytes
          * every time it returns.
          *
          * @param buf the buffer to read into
          * @param offset the offset to begin reading data into
          * @param maxlen the maximum number of bytes to read
          * @return the number of bytes read, or -1 if an error occurs.
          */
         public int read(byte[] buf, int offset, int maxlen) {
             return receive(buf, offset, maxlen);
         }
 
         /**
          * Writes to the socket from the buffer.
          *
          * @param buf the buffer to
          * @param offset the offset to begin writing data from
          * @param len the number of bytes to write
          * @return the number of bytes written or -1 if an error occurs.
          */
         public int write(byte[] buf, int offset, int len) {
             return send(buf, offset, len);
         }
 
         /**
          * Closes the connection for this socket.
          * Blocks until the connection is closed.
          *
          * @return true unless no connection was open.
          */
         public boolean close() {
             switch(tcb.getState()){
                 case CLOSED:
                     Log.e(TAG, "Error in close(): connection does not exist");
                     return false;
                 case LISTEN:
                     // TODO: Any outstanding RECEIVEs are returned with "error:  closing" responses
                     tcb.enterState(TransmissionControlBlock.State.CLOSED);
                     return true;
                 case SYN_SENT:
                     // TODO: return "error:  closing" responses to any queued SENDs, or RECEIVEs
                     tcb.enterState(TransmissionControlBlock.State.CLOSED);
                     return true;
                 case SYN_RECEIVED:
                     // TODO: this probably needs concurrency protection
                     if(!sendIssued && !tcb.hasDataToTransmit()){
                         // TODO: send FIN
                         tcb.enterState(TransmissionControlBlock.State.FIN_WAIT_1);
                     }
 
                     // TODO: Queue close for processing after entering ESTABLISHED state
                     // e.g. block until state == established, and process close
                     return true;
                 case ESTABLISHED:
                     // TODO: Queue this close until all preceding SENDs have been segmentized,
                     // then form a FIN segment and send it (e.g wait until ... something)
                     tcb.enterState(TransmissionControlBlock.State.FIN_WAIT_1);
                     return true;
                 case FIN_WAIT_1:
                 case FIN_WAIT_2:
                     // TODO: according assignment this should also return true
                     Log.e(TAG, "Error in close(): connection closing");
                     return false;
                 case CLOSE_WAIT:
                     // TODO: Queue this request until all preceding SENDs have been segmentized;
                     // then send a FIN segment, enter CLOSING state.
                     // TODO: don't forget the return here
                 default:
                     // TODO: according to assignment this should return true
                     Log.e(TAG, "Error in close(): connection closing");
                     return false;
             }
         }
     }
 
 }
