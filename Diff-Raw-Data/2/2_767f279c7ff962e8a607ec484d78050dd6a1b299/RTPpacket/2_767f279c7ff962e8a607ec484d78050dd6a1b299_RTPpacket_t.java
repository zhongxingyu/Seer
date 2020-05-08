 package talkingnet.net.rtp;
 
 /**
  * @author Alexander Oblovatniy <oblovatniy@gmail.com>
  * <br/>Original:
  * @see
  * http://code.google.com/p/mynpr/source/browse/trunk/mynpr/src/com/webeclubbin/mynpr/RTPpacket.java
  */
 public class RTPpacket {
 
     public static int HEADER_SIZE = 12;
     
     public byte[] header;
     public int payloadSize;
     public byte[] payload;
     
     public int version = 2;
     public int padding = 0;
     public int extension = 0;
     public int CSRCcount = 0;
     public int marker = 0;
     public int SSRC = 0;
     public int payloadType;
     public int sequenceNumber;
     public int timeStamp;
 
     /**
      * RTP packet from raw data
      */
     public RTPpacket(int PType, int Framenb, int Time, byte[] data, int data_length) {
         sequenceNumber = Framenb;
         timeStamp      = Time;
         payloadType    = PType;
         payloadSize   = data_length;
 
         createAndFillHeader();
         
         payload = new byte[payloadSize];
         
         System.arraycopy(data, 0, payload, 0, data_length);
     }
 
     private void createAndFillHeader() {
         header = new byte[HEADER_SIZE];
 
         header[0] = (byte) (version << 6);
         header[0] = (byte) (header[0] | padding << 5);
         header[0] = (byte) (header[0] | extension << 4);
         header[0] = (byte) (header[0] | CSRCcount);
         header[1] = (byte) (header[1] | marker << 7);
         header[1] = (byte) (header[1] | payloadType);
         header[2] = (byte) (sequenceNumber >> 8);
         header[3] = (byte) (sequenceNumber & 0xFF);
         header[4] = (byte) (timeStamp >> 24);
         header[5] = (byte) (timeStamp >> 16);
         header[6] = (byte) (timeStamp >> 8);
         header[7] = (byte) (timeStamp & 0xFF);
         header[8] = (byte) (SSRC >> 24);
         header[9] = (byte) (SSRC >> 16);
         header[10] = (byte) (SSRC >> 8);
         header[11] = (byte) (SSRC & 0xFF);
     }
     
     /**
      * RTP packet from UDP data
      */
     public RTPpacket(byte[] packet, int packet_size) {        
         if (packet_size < HEADER_SIZE) {
             return;
         }
         
         restoreHeader(packet);
         restorePayload(packet, packet_size);
         restoreHeaderChangingFields();
     }
     
     private void restoreHeader(byte[] packet) {
         header = new byte[HEADER_SIZE];
         System.arraycopy(packet, 0, header, 0, HEADER_SIZE);
     }
     
     private void restorePayload(byte[] packet, int packet_size) {
         payloadSize = packet_size - HEADER_SIZE;
         payload = new byte[payloadSize];
        System.arraycopy(packet, HEADER_SIZE, payload, 0, payloadSize);
     }
     
     private void restoreHeaderChangingFields(){
         payloadType = header[1] & 127;
         sequenceNumber = unsignedInt(header[3]) + 256 * unsignedInt(header[2]);
         timeStamp = unsignedInt(header[7]) + 256 * unsignedInt(header[6]) + 65536 * unsignedInt(header[5]) + 16777216 * unsignedInt(header[4]);
     }
 
     /**
      * Puts payload to data bitstream
      * @param data Bitstream to fill
      * @return Length of payload
      */
     public int getPayload(byte[] data) {
         System.arraycopy(payload, 0, data, 0, payloadSize);
         return (payloadSize);
     }
 
     /**
      * @return The length of the payload
      */
     public int getPayloadLength() {
         return (payloadSize);
     }
 
     /**
      * @return The total length of the RTP packet
      */
     public int getLength() {
         return (payloadSize + HEADER_SIZE);
     }
 
     /**
      * Puts header and payload to packet bitstream
      * @param packet Bitstream to fill
      * @return Total length of the RTP packet
      */
     public int getPacket(byte[] packet) {        
         System.arraycopy(header, 0, packet, 0, HEADER_SIZE);
         System.arraycopy(payload, 0, packet, HEADER_SIZE, payloadSize);
 
         return getLength();
     }
 
     /**
      * @return Packet's timestamp
      */
     public int getTimestamp() {
         return (timeStamp);
     }
 
     /**
      * @return Packet's number in sequence
      */
     public int getSequenceNumber() {
         return (sequenceNumber);
     }
 
     /**
      * @return Integer value of payload type
      */
     public int getPayloadType() {
         return (payloadType);
     }
 
     /**
      * Print headers without the SSRC
      */    
     public void printHeader() {
         final String TAG = "printheader ";
 
         for (int i = 0; i < (HEADER_SIZE - 4); i++) {
             for (int j = 7; j >= 0; j--) {
                 if (((1 << j) & header[i]) != 0) {
                     System.out.println(TAG + "1");
                 } else {
                     System.out.println(TAG + "0");
                 }
             }
         }
     }
 
     /**
      * @param nb 8-bit integer
      * @return The unsigned value of nb
      */
     private static int unsignedInt(int nb) {
         if (nb >= 0) {
             return (nb);
         } else {
             return (256 + nb);
         }
     }
 }
