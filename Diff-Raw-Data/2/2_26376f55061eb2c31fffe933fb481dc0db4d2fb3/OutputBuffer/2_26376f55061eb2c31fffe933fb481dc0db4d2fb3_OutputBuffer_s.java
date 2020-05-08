 package jagex.runescape.io;
 
 import jagex.runescape.io.packet.Packet;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 
 /**
  * OutputBuffer.java
  * @author Ryley M. Kimmel <ryley.kimmel@live.com>
  * @version 1.0
  * Aug 14, 2012
  */
 public final class OutputBuffer extends Packet {
 
     /**
      * Constructs a new fixed raw output buffer.
      * 
      * @param buffer A specific payload buffer to use as the internal buffer.
      */
     public OutputBuffer(ChannelBuffer buffer) {
 	super(buffer);
     }
 
     /**
      * Constructs a new fixed output buffer
      * 
      * @param opCode The operation code which is used to associate the data piece with it's handler.
      * @param buffer A specific payload buffer to use as the internal buffer.
      */
     public OutputBuffer(int opCode, ChannelBuffer buffer) {
 	super(opCode, buffer);
     }
 
     /**
      * Constructs a new output buffer.
      * 
      * @param opCode The operation code which is used to associate the data piece with it's handler.
      * @param type The type of packet. This marks the additions needed and 
      * the type of recognition variables which need to be also 
      * attributed towards an outgoing buffer.
      * @param buffer A specific payload buffer to use as the internal buffer.
      */
     public OutputBuffer(int opCode, Type type, ChannelBuffer buffer) {
 	super(opCode, type, buffer);
     }
 
     /**
      * Constructs a new output buffer.
      * 
      * @param opCode The operation code which is used to associate the data piece with it's handler.
      * @param type The type of packet. This marks the additions needed and 
      * the type of recognition variables which need to be also 
      * attributed towards an outgoing buffer.
      */
     public OutputBuffer(int opCode, Type type) {
 	super(opCode, type);
     }
 
     /**
      * Constructs a new output buffer with a dynamic buffer.
      * 
      * @param opCode The operation code which is used to associate the data piece with it's handler.
      */
     public OutputBuffer(int opCode) {
 	super(opCode);
     }
 
     /**
      * Constructs a new headless fixed output buffer.
      */
     public OutputBuffer() {
 	super(-1);
     }
 
     /**
      * Writes one <code>byte</code> to the buffer.
      * 
      * @param b The byte's value.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeByte(final int b) {
 	buffer.writeByte(b);
 	return this;
     }
 
     /**
      * Writes numerous <code>byte</code>s to the buffer.
      * 
      * @param b The array of bytes to write.
      * @return This writer's instance, for chaining.
      * @throws IllegalArgumentException should the length of <tt>b</tt> be less than <tt>1</tt>.
      */
     public OutputBuffer writeBytes(final byte... b) {
 	if (b.length < 1) {
 	    throw new IllegalArgumentException("Must write at least one value.");
 	}
 	buffer.writeBytes(b);
 	return this;
     }
 
     /**
      * Transfers the specified source array's data to this buffer 
      * starting at the current writerIndex and increases the writerIndex 
      * by the number of the transferred bytes (= length).
      * 
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeBytes(final byte[] src, final int srcIndex, final int length) {
 	buffer.writeBytes(src, srcIndex, length);
 	return this;
     }
 
     /**
      * Writes one <code>byte</code> special type <tt>A</tt> to the buffer.
      * 
      * @param b The value of the byte.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeByteA(final int b) {
 	return writeByte(b + 128);
     }
 
     /**
      * Writes one <code>byte</code> special type <tt>C</tt> to the buffer.
      * 
      * @param b The value of the byte.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeByteC(final int b) {
 	return writeByte(- b);
     }
 
     /**
      * Writes one <code>byte</code> special type <tt>S</tt> to the buffer.
      * 
      * @param b The value of the byte.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeByteS(final int b) {
 	return writeByte(128 - b);
     }
 
     /**
      * Writes one <code>short</code> to the buffer.
      * 
      * @param s The short's value.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeShort(final int s) {
 	buffer.writeShort(s);
 	return this;
     }
 
     /**
      * Writes numerous <code>short</code>s to the buffer.
      * 
      * @param s The array of shorts to write.
      * @return This writer's instance, for chaining.
      * @throws IllegalArgumentException should the length of <tt>v</tt> be less than <tt>1</tt>.
      */
     public OutputBuffer writeShort(final int... s) {
 	if (s.length < 1) {
 	    throw new IllegalArgumentException("Must write at least one value.");
 	}
 	for (final int i : s) {
 	    writeShort(i);
 	}
 	return this;
     }
 
     /**
      * Writes one <code>int</code> to the buffer.
      * 
      * @param i The integer's value.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeInt(final int i) {
 	buffer.writeInt(i);
 	return this;
     }
 
     /**
      * Writes numerous <code>int</code>s to the buffer.
      * 
      * @param v The array of integers to write.
      * @return This writer's instance, for chaining.
      * @throws IllegalArgumentException should the length of <tt>v</tt> be less than <tt>1</tt>.
      */
     public OutputBuffer writeInt(final int... v) {
 	if (v.length < 1) {
 	    throw new IllegalArgumentException("Must write at least one value.");
 	}
 	for (final int i : v) {
 	    writeInt(i);
 	}
 	return this;
     }
 
     /**
      * Writes one <code>long</code> to the buffer.
      * 
      * @param l The long's value.
      * @return This writer's instance, for chaining.
      */
     public OutputBuffer writeLong(final long l) {
 	buffer.writeLong(l);
 	return this;
     }
 
     /**
      * Writes numerous <code>long</code>s to the buffer.
      * 
     * @param b The array of longs to write.
      * @return This writer's instance, for chaining.
      * @throws IllegalArgumentException should the length of <tt>v</vv> be less than <tt>1</tt>.
      */
     public OutputBuffer writeLong(final long... l) {
 	if (l.length < 1) {
 	    throw new IllegalArgumentException("Must write at least one value.");
 	}
 	for (final long i : l) {
 	    writeLong(i);
 	}
 	return this;
     }
 
 }
