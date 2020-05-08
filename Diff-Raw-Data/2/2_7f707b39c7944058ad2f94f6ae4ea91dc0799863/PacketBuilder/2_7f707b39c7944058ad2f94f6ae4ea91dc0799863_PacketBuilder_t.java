 package jagex.runescape.io.packet;
 
 import jagex.runescape.io.packet.Packet.Type;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 
 /**
  * PacketBuilder.java
  * @author Ryley M. Kimmel <ryley.kimmel@live.com>
  * @version 1.0
  * Aug 14, 2012
  */
 public final class PacketBuilder {
 
     /** An array of bit masks. The element {@code n} is equal to {@code 2<sup>n</sup> - 1}. */
     private static final int[] BIT_MASK = new int[32];
 
     /**
      * Initializes the {@link #BIT_MASK} array.
      */
     static {
 	for (int i = 0; i < BIT_MASK.length; ++i) {
 	    BIT_MASK[i] = (1 << i) - 1;
 	}
     }
 
     /** the current bit index */
     private int bitIndex;
 
     /** The operation code */
     private final int opCode;
 
     /** The buffer */
     private final ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
 
     /** The packet type */
     private final Type type;
 
     /**
      * Creates a new fixed packet
      * 
      * @param opCode The operation code which is used to associate the data piece with it's handler.
      */
     public PacketBuilder(final int opCode) {
 	this(opCode, Type.FIXED);
     }
 
     /**
      * Creates a new packet builder for a packet
      * 
      * @param opCode The operation code which is used to associate the data piece with it's handler.
      * @param type The type of packet. This marks the additions needed and 
      * the type of recognition variables which need to be also 
      * attributed towards an outgoing buffer.
      */
     public PacketBuilder(final int opCode, final Type type) {
 	this.opCode = opCode;
 	this.type = type;
     }
 
     /**
     * Puts a number of bits into the buffer
      * 
      * @param numBits The number of bits to put into the buffer.
      * @param value The value.
      */
     public void writeBits(int numBits, final int value) {
 	int bytePos = bitIndex >> 3;
 	int bitOffset = 8 - (bitIndex & 7);
 	bitIndex += numBits;
 
 	int requiredSpace = (bytePos - buffer.writerIndex()) + 1;
 	requiredSpace += (numBits + 7) / 8;
 	buffer.ensureWritableBytes(requiredSpace);
 
 	for (; numBits > bitOffset; bitOffset = 8) {
 	    int tmp = buffer.getByte(bytePos);
 	    tmp &= ~BIT_MASK[bitOffset];
 	    tmp |= (value >> (numBits - bitOffset)) & BIT_MASK[bitOffset];
 	    buffer.setByte(bytePos++, tmp);
 	    numBits -= bitOffset;
 	}
 	if (numBits == bitOffset) {
 	    int tmp = buffer.getByte(bytePos);
 	    tmp &= ~BIT_MASK[bitOffset];
 	    tmp |= value & BIT_MASK[bitOffset];
 	    buffer.setByte(bytePos, tmp);
 	} else {
 	    int tmp = buffer.getByte(bytePos);
 	    tmp &= ~(BIT_MASK[numBits] << (bitOffset - numBits));
 	    tmp |= (value & BIT_MASK[numBits]) << (bitOffset - numBits);
 	    buffer.setByte(bytePos, tmp);
 	}
     }
 
     /**
      * Switches this builder's mode to the bit access mode.
      */
     public void switchToBitAccess() {
 	bitIndex = buffer.writerIndex() * 8;
     }
 
     /**
      * Switches this builder's mode to the byte access mode.
      */
     public void switchToByteAccess() {
 	buffer.writerIndex((bitIndex + 7) / 8);
     }
 
     /**
      * Gets the current length of the builder's buffer.
      */
     public int getLength() {
 	return buffer.writerIndex();
     }
 
     /**
      * Creates a {@link Packet} based on the current contents of this builder.
      * 
      * @return The packet
      */
     public Packet createPacket() {
 	return new Packet(opCode, type, buffer);
     }
 
 }
