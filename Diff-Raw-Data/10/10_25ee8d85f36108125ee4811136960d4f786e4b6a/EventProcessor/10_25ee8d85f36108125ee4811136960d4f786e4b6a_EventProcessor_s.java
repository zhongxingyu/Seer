 package dgu.bufsizing.control;
 
 import dgu.bufsizing.BottleneckLink;
 import dgu.bufsizing.DemoGUI;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 
 /**
  * Process event capture packets.
  * @author David Underhill
  */
 public class EventProcessor extends Thread {
     /** default event capture port */
     public static final int DEFAULT_EVCAP_PORT = 27033;
     
     /** maximum length of a datagram */
     private static final int MAX_PACKET_LEN = 1500;
     
     /** number of queues in the header */
     private static final int NUM_QUEUES = 8;
     
     /** the port to listen on for event capture packets */
     private int port;
     
     /** socket to use for listening */
     private DatagramSocket dsocket;
     
     public EventProcessor( int port ) {
         this.port = port;
         
         /* establish a socket for the stats port */
         try {
             dsocket = new DatagramSocket(port);
         } catch( SocketException e ) {
             System.err.println( "Error: UDP socket setup failed for Event Processor Thread: " + e.getMessage() );
             System.exit( 1 );
             return;
         }
     }
     
     /**
      * Listens for new event capture packets and processes them with the 
      * assumption that they are for the first bottleneck on the first router.
      */
     public void run() {
         byte[] buf = new byte[MAX_PACKET_LEN];
         DatagramPacket packet = new DatagramPacket(buf, buf.length);
 
         /* listen for updates until the end of time */
         while (true) {
             try {
                 packet.setLength(buf.length);
                 dsocket.receive(packet);
 
                 /* extract the stats (assume router 0) */
                 EventProcessor.handleEventCapPacket( 0, buf );
             } catch( IOException e ) {
                 System.err.println( "Error: UDP stats receive failed: " + e.getMessage() );
                 System.exit( 1 );
             }
         }
     }
     
     /** defines event type codes */
     private static enum EventType {
         TYPE_TS((byte)0),
         TYPE_ARRIVE((byte)1),
         TYPE_DEPART((byte)2),
         TYPE_DROP((byte)3);
         
         EventType( final byte t ) { 
             type = t;
         }
         public final byte type;
     }
     
     /** reads byte i to i+3 to form an int */
     private static int extractInt( byte[] buf, int i ) {
         // convert to signed ints, clearing any bits set due to sign extension
         int a = buf[i]   & 0x000000FF;
         int b = buf[i+1] & 0x000000FF;
         int c = buf[i+2] & 0x000000FF;
         int d = buf[i+3] & 0x000000FF;
         
         // create the int
         return a<<24 | b<<16 | c<<8 | d;
     }
     
     /** reads byte i to i+3 to form a long (use for unsigned ints which may use the MSB) */
     private static long extractUintAsLong( byte[] buf, int i ) {
         long ret = 0;
         
         // convert to signed ints, clearing any bits set due to sign extension
         int a = buf[i]   & 0x000000FF;
         int b = buf[i+1] & 0x000000FF;
         int c = buf[i+2] & 0x000000FF;
         int d = buf[i+3] & 0x000000FF;
         
         // create the int
         ret = ((long)a)<<24 | b<<16 | c<<8 | d;
         
         // clear any sign extended bits
         return ret & 0x00000000FFFFFFFFL;
     }
     
     private static long extractTimestamp( byte[] buf, int i ) {
         long upper = extractUintAsLong(buf, i);
         long lower = extractUintAsLong(buf, i+4);
         return upper<<32L | lower;
     }
     
     private static final void debug_println( String s ) {
         //System.err.println( s );
     }
     
     /**
      * Processes a buffer containing an event capture packet.
      * @param routerIndex  index of the router the data belongs to
      * @param buf          datagram containing an event capture payload
      */
     public static void handleEventCapPacket( int routerIndex, byte[] buf ) {
         // always assume first bottleneck for now
         BottleneckLink b = DemoGUI.me.demo.getRouters().get(routerIndex).getBottleneckLinkAt(0);
         
         // start processing at byte 1 (byte 0 isn't too interesting)
         int index = 1;
         int num_events = buf[index] & 0xFF; /* cast to an int so we properly interpret values > 127 */
         index += 1;
         
         // skip the sequence number
         debug_println( "seq = " + extractInt(buf, index) );
         index += 4;
         
         // get the timestamp before the queue data
         long timestamp_8ns = extractTimestamp( buf, 70 );
         if( !b.prepareForUpdate( timestamp_8ns ) ) {
             debug_println( "old timestamp (ignoring) " + timestamp_8ns );
             return; // old, out-of-order packet
         }
         else
             debug_println( "got new timestamp " + timestamp_8ns );
         
         // get queue occupancy data
         for( int i=0; i<NUM_QUEUES; i++ ) {
             // update the queue with its new absolute value
            if( i == 2 ) // only handle NF2C1 for now
                b.setOccupancy( timestamp_8ns, 8 * extractInt(buf, index) );
             index += 4;
             
             //skip size in packets
             index += 4;
         }
         
         // already got the timestamp; keep going
         index += 8;
         
         // process each event
         long timestamp_adjusted_8ns = timestamp_8ns;
         for( int i=0; i<num_events; i++ ) {            
             int type = (buf[index] & 0xC0) >> 6;
             debug_println( "  got type = " + Integer.toHexString(type) );
             
             if( type == EventType.TYPE_TS.type ) {
                 timestamp_8ns = extractTimestamp( buf, index );
                 index += 8;
                 debug_println( "    got timestamp " + timestamp_8ns );
             }
             else {
                 // determine the # of bytes involved and the offset
                 int val = extractInt( buf, index );
                 //System.err.println( "    got bytes for shorty: " + Integer.toHexString(val) );
                 int queue_id = (val & 0x38000000) >> 27;           
                 int plen_bytes = ((val & 0x07F80000) >> 19) * 8 - 8; /* - 8 to not include NetFPGA overhead */
                 timestamp_adjusted_8ns = timestamp_8ns + (val & 0x0007FFFF);
                 index += 4;
                 
                 debug_println( "     got short event " + type + " (" + plen_bytes + "B) at timestamp " + timestamp_adjusted_8ns + " for queue " + queue_id );
                 if( queue_id != 2 ) {
                     // only pay attention to NF2C1 for now
                     debug_println( "    ignoring event for queue " + queue_id );
                     continue;
                 }
                 
                 if( type == EventType.TYPE_ARRIVE.type )
                     b.arrival( timestamp_adjusted_8ns, plen_bytes );
                if( type == EventType.TYPE_ARRIVE.type )
                     b.departure( timestamp_adjusted_8ns, plen_bytes );
                 else
                     b.dropped( timestamp_adjusted_8ns, plen_bytes );
             }
         }
         
         // refresh instantaneous readings over the interval from the previous
         //  update to the time of the last event in this update
         b.refreshInstantaneousValues( timestamp_adjusted_8ns );
     }
 }
