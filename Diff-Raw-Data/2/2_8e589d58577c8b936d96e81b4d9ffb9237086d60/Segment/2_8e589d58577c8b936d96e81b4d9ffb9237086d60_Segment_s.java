 package nl.vu.cs.cn.tcp.segment;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import nl.vu.cs.cn.IP;
 import nl.vu.cs.cn.tcp.TransmissionControlBlock;
 import nl.vu.cs.cn.tcp.Util;
 
 /**
  * A TCP segment which contains a header and a data part.
  */
 public class Segment {
 
     /**
      * Masks for control bits.
      * 1  = 000001
      * 2  = 000010
      * 4  = 000100
      * 8  = 001000
      * 16 = 010000
      * 32 = 100000
      *
      * Using bitwise operators it is easy to set the control bits using the logical OR '|' operation.
      */
     public static final short URG_MASK = 32;
     public static final short ACK_MASK = 16;
     public static final short PSH_MASK = 8;
     public static final short RST_MASK = 4;
     public static final short SYN_MASK = 2;
     public static final short FIN_MASK = 1;
 
     /**
      * Data offset: The number of 32 bit words in the TCP Header.  This indicates where
      * the data begins. We have 5 words of 32 bits:
      * - Source + destination port
      * - Seq num
      * - Ack num
      * - Data offset + reserved area + control bits + window
      * - Checksum + Urgent pointer
      */
     private static final short DATA_OFFSET = 5;
 
     private static final short HEADER_SIZE = DATA_OFFSET * 4;
 
     private IP.IpAddress sourceAddr;
     private IP.IpAddress destinationAddr;
     private short sourcePort;
     private short destinationPort;
 
     private long seq;    // segment sequence number
     private long ack;    // segment acknowledgement number
     private short wnd;  // segment window
 
     private int len = -1;    // segment length data + syn + fin
 
     // Note: we omit variables urgent pointer and precendence value because
     // those are unsupported in this implementation.
 
     private byte[] data;
     private boolean isUrg, isAck, isPsh, isRst, isSyn, isFin;
 
     private short checksum;
     private boolean validChecksum;
 
     /**
      * Create a new segment
      * @param sourceAddr
      * @param destinationAddr
      * @param sourcePort
      * @param destinationPort
      * @param seq
      */
     protected Segment(IP.IpAddress sourceAddr, IP.IpAddress destinationAddr, short sourcePort, short destinationPort, long seq, short wnd) {
         this(sourceAddr, destinationAddr, sourcePort, destinationPort, seq, wnd, -1);
     }
 
     /**
      * Create a new segment and set isAck to true
      * @param sourceAddr
      * @param destinationAddr
      * @param sourcePort
      * @param destinationPort
      * @param seq
      * @param ack
      */
     protected Segment(IP.IpAddress sourceAddr, IP.IpAddress destinationAddr, short sourcePort, short destinationPort, long seq, short wnd, long ack) {
         this.sourceAddr = sourceAddr;
         this.destinationAddr = destinationAddr;
         this.sourcePort = sourcePort;
         this.destinationPort = destinationPort;
         this.seq = seq % Integer.MAX_VALUE;
         this.wnd = wnd;
 
         if(ack > -1){
             this.ack = ack % Integer.MAX_VALUE;
             isAck = true;
         }
 
         // implementation specific:
 
         // PUSH is set on all segments, RESET is not supported and set to false
         isPsh = true;
         isRst = false;
     }
 
     public Segment(byte[] packet, int sourceAddr, int destinationAddr){
         this.sourceAddr = IP.IpAddress.getAddress(sourceAddr);
         this.destinationAddr = IP.IpAddress.getAddress(destinationAddr);
 
         ByteBuffer bb = ByteBuffer.wrap(packet);
 
         sourcePort = bb.getShort();
         destinationPort = bb.getShort();
         seq = bb.getInt();
         ack = bb.getInt();
 
         /*
          * bits contains data offset, reserved area, and control bits.
          * It looks like 0101 0000 0011 1111 (if all flags would be true,
          * and the data offset would be 5). We ignore the data offset for now,
          * and read out the control bits using the logical and operation.
          */
         short bits = bb.getShort();
         setControlBits(bits);
 
         wnd = bb.getShort();
         int checksumPosition = bb.position();
         checksum = bb.getShort();
 
         // ignore urgent pointer, never used in this implementation
         bb.getShort();
 
         // options never used in this implementation, so data comes next (if there is data)
         if(bb.remaining() > 0){
             data = new byte[bb.remaining()];
             bb.get(data);
         }
 
         // check if checksum is correct
         bb.putShort(checksumPosition, (short)0);
 
         short expectedChecksum = Util.calculateChecksum(bb, this.sourceAddr, this.destinationAddr, bb.capacity());
         validChecksum = (expectedChecksum == checksum);
     }
 
     /**
      * Set the control bits for this segment. Use the control masks
      * with the logical OR operation to set multiple bits. Note that
      * this overwrites the current control bits values.
      *
      * @param bits
      */
     public void setControlBits(short bits){
         isUrg = (URG_MASK & bits) != 0;
         isAck = (ACK_MASK & bits) != 0;
         isPsh = (PSH_MASK & bits) != 0;
         isRst = (RST_MASK & bits) != 0;
         isSyn = (SYN_MASK & bits) != 0;
         isFin = (FIN_MASK & bits) != 0;
     }
 
     public IP.IpAddress getSourceAddr(){
         return sourceAddr;
     }
 
     public short getSourcePort() {
         return sourcePort;
     }
 
     public IP.IpAddress getDestinationAddr(){
         return destinationAddr;
     }
 
     public short getDestinationPort() {
         return destinationPort;
     }
 
     public long getSeq() {
         return seq;
     }
 
     public long getLastSeq() {
        return (seq + len - 1) % Integer.MAX_VALUE;
     }
 
     public long getAck() {
         return ack;
     }
 
     /**
      * Get the length of the segment, which is the length of the data plus
      * the length of control bits that take up space (SYN and FIN)
      * @return
      */
     public int getLen() {
         if(len == -1){
             len = getDataLength();
             if(isSyn) len += 1;
             if(isFin) len += 1;
         }
         return len;
     }
 
     public byte[] getData(){
         return data;
     }
 
     /**
      * Copy bytes from data (starting at offset, maximum of either len or max data size)
      * into the data field.
      * @param data
      * @param offset
      * @param len
      * @return number of bytes copied into the data field
      */
     public int setData(byte[] data, int offset, int len){
         len = Math.min(TransmissionControlBlock.MAX_SEGMENT_SIZE - HEADER_SIZE, len);
         this.data = new byte[len];
         System.arraycopy(data, offset, this.data, 0, len);
         return len;
     }
 
     /**
      * Copy bytes from data into the data field. This is the same as calling
      * setData(data, 0, data.length).
      * @param data
      * @return
      */
     public int setData(byte[] data){
         return setData(data, 0, data.length);
     }
 
     public int getDataLength(){
         return (data == null) ? 0 : data.length;
     }
 
     public boolean isUrg() {
         return isUrg;
     }
 
     public void setIsUrg(boolean urg) {
         isUrg = urg;
     }
 
     public boolean isAck() {
         return isAck;
     }
 
     public void setIsAck(boolean ack) {
         isAck = ack;
     }
 
     public boolean isPsh() {
         return isPsh;
     }
 
     public void setIsPsh(boolean psh) {
         isPsh = psh;
     }
 
     public boolean isRst() {
         return isRst;
     }
 
     public void setIsRst(boolean rst) {
         isRst = rst;
     }
 
     public boolean isSyn() {
         return isSyn;
     }
 
     public void setIsSyn(boolean syn) {
         isSyn = syn;
     }
 
     public boolean isFin() {
         return isFin;
     }
 
     public void setIsFin(boolean fin) {
         isFin = fin;
     }
 
     public boolean hasValidChecksum(){
         return validChecksum;
     }
 
     public byte[] encode(){
         int capacity = HEADER_SIZE; // capacity in bytes
         if(getDataLength() > 0){
             capacity += data.length;
         }
 
         ByteBuffer bb = ByteBuffer.allocate(capacity);
         bb.putShort(sourcePort);
         bb.putShort(destinationPort);
         bb.putInt((int)seq);
         bb.putInt((int)ack);
 
         /*
          * Create bits for data offset, reserved area, and control bits.
          *
          * Start with DATA_OFFSET, which looks like 0000 0000 0000 0101 (assuming
          * the value DATA_OFFSET equals 5). The data offset field is 4 bits long,
          * so shift to the left by twelve. This results in 0101 0000 0000 0000
          */
         short bits = DATA_OFFSET << 12;
 
         /*
          * Set the control bits using the logical operator
          */
         bits |= (isUrg ? URG_MASK : 0);
         bits |= (isAck ? ACK_MASK : 0);
         bits |= (isPsh ? PSH_MASK : 0);
         bits |= (isRst ? RST_MASK : 0);
         bits |= (isSyn ? SYN_MASK : 0);
         bits |= (isFin ? FIN_MASK : 0);
 
         /*
          * Assume all flags where set to true. Then the bits look like this:
          * 0101 0000 0011 1111. Because the bit masks are shorts, and thus have
          * length 16, the bit string looks exactly as we need it. Also, the reserved
          * area will always stay 0.
          */
 
         bb.putShort(bits);
         bb.putShort(wnd);
 
         /*
          * Store current position so we can inject the checksum later on.
          * Also, insert 16 zero's that we'll later replace with the actual
          * checksum.
          */
         int checksumPosition = bb.position();
         bb.putShort((short) 0);
 
         /*
          * Set urgent pointer; this will be ignored if urg is not set,
          * which is the case in this implementation.
          */
         bb.putShort((short) 0);
 
         if(getDataLength() > 0){
             bb.put(data);
         }
 
         checksum = Util.calculateChecksum(bb, sourceAddr, destinationAddr, bb.capacity());
         bb.putShort(checksumPosition, checksum);
 
         return bb.array();
     }
 
     @Override
     public boolean equals(Object o) {
         if(!(o instanceof Segment)){
             return false;
         }
         
         Segment segment = (Segment) o;
         return segment.sourceAddr.getAddress() == sourceAddr.getAddress() &&
                 segment.sourcePort == sourcePort &&
                 segment.destinationAddr.getAddress() == destinationAddr.getAddress() &&
                 segment.destinationPort == destinationPort &&
                 segment.seq == seq && segment.ack == ack && segment.wnd == wnd &&
                 segment.isAck == isAck && segment.isSyn == isSyn && segment.isFin == isFin &&
                 segment.isRst == isRst && segment.isPsh == isPsh &&
                 Arrays.equals(segment.data, data);
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(" | SEQ: ").append(seq);
         if(isAck) sb.append(" | ACK: ").append(ack);
         sb.append(" | ");
         if(isUrg) sb.append("URG, ");
         if(isAck) sb.append("ACK, ");
         if(isPsh) sb.append("PSH, ");
         if(isRst) sb.append("RST, ");
         if(isSyn) sb.append("SYN, ");
         if(isFin) sb.append("FIN ");
         if(getDataLength() > 0) try {
             sb.append(" | [").append(new String(data, "UTF-8")).append("]");
         } catch (UnsupportedEncodingException e) {
             sb.append(" | [").append(new String(data)).append("]");
         }
 
         return sb.toString();
     }
 }
