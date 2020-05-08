 package org.tmatesoft.jlibc;
 
 public interface Memory {
 
 	Pointer pointer(int address);
 
 	byte getByte(int address);
 
 	void setByte(int address, byte value);
 
 	short getShort(int address);
 
 	void setShort(int address, short value);
 
 	int getInt(int address);
 
 	void setInt(int address, int value);
 
 	long getLong(int address);
 
 	void setLong(int address, long value);
 
 	boolean getBoolean(int address);
 
 	void setBoolean(int address, boolean value);
 
 	short getUnsignedByte(int address);
 
 	void setUnsignedByte(int address, short value);
 
 	int getUnsignedShort(int address);
 
 	void setUnsignedShort(int address, int value);
 
 	long getUnsignedInt(int address);
 
 	void setUnsignedInt(int address, long value);
 
 	class Pointer implements Memory {
 
 		private final Memory memory;
 		private int address;
 
 		public Pointer(final Memory memory, final int address) {
 			this.memory = memory;
 			this.address = address;
 		}
 
 		public final int address() {
 			return address;
 		}
 
 		public final void move(final int offset) {
 			address += offset;
 		}
 
 		public final void move(final Pointer pointer) {
			address += pointer.address;
 		}
 
 		@Override
 		public final Pointer pointer(final int offset) {
 			return memory.pointer(address + offset);
 		}
 
 		@Override
 		public final byte getByte(final int offset) {
 			return memory.getByte(address + offset);
 		}
 
 		@Override
 		public final void setByte(final int offset, final byte value) {
 			memory.setByte(address + offset, value);
 		}
 
 		@Override
 		public final short getShort(final int offset) {
 			return memory.getShort(address + offset);
 		}
 
 		@Override
 		public final void setShort(final int offset, final short value) {
 			memory.setShort(address + offset, value);
 		}
 
 		@Override
 		public final int getInt(final int offset) {
 			return memory.getInt(address + offset);
 		}
 
 		@Override
 		public final void setInt(final int offset, final int value) {
 			memory.setInt(address + offset, value);
 		}
 
 		@Override
 		public long getLong(final int offset) {
 			return memory.getLong(address + offset);
 		}
 
 		@Override
 		public final void setLong(final int offset, final long value) {
 			memory.setLong(address + offset, value);
 		}
 
 		@Override
 		public final boolean getBoolean(final int offset) {
 			return getBoolean(address + offset);
 		}
 
 		@Override
 		public final void setBoolean(final int offset, final boolean value) {
 			setBoolean(address + offset, value);
 		}
 
 		@Override
 		public final short getUnsignedByte(final int offset) {
 			return memory.getUnsignedByte(address + offset);
 		}
 
 		@Override
 		public final void setUnsignedByte(final int offset, final short value) {
 			memory.setUnsignedByte(address + offset, value);
 		}
 
 		@Override
 		public final int getUnsignedShort(final int offset) {
 			return memory.getUnsignedShort(address + offset);
 		}
 
 		@Override
 		public final void setUnsignedShort(final int offset, final int value) {
 			memory.setUnsignedShort(address + offset, value);
 		}
 
 		@Override
 		public final long getUnsignedInt(final int offset) {
 			return memory.getUnsignedInt(address + offset);
 		}
 
 		@Override
 		public final void setUnsignedInt(final int offset, final long value) {
 			memory.setUnsignedInt(address + offset, value);
 		}
 
 	}
 
 	class ByteArray implements Memory {
 
 		private final byte[] bytes;
 
 		public ByteArray(final byte[] bytes) {
 			this.bytes = bytes;
 		}
 
 		public ByteArray(final int size) {
 			this(new byte[size]);
 		}
 
 		@Override
 		public final Pointer pointer(final int address) {
 			return new Pointer(this, address);
 		}
 
 		@Override
 		public final byte getByte(final int address) {
 			return bytes[address];
 		}
 
 		@Override
 		public final void setByte(final int address, final byte value) {
 			bytes[address] = value;
 		}
 
 		@Override
 		public final short getShort(final int address) {
 			return Utils.getShort(bytes, address);
 		}
 
 		@Override
 		public final void setShort(final int address, final short value) {
 			Utils.putShort(bytes, address, value);
 		}
 
 		@Override
 		public final int getInt(final int address) {
 			return Utils.getInt(bytes, address);
 		}
 
 		@Override
 		public final void setInt(final int address, final int value) {
 			Utils.putInt(bytes, address, value);
 		}
 
 		@Override
 		public final long getLong(final int address) {
 			return Utils.getLong(bytes, address);
 		}
 
 		@Override
 		public final void setLong(final int address, final long value) {
 			Utils.putLong(bytes, address, value);
 		}
 
 		@Override
 		public final boolean getBoolean(final int address) {
 			return Utils.getBoolean(bytes, address);
 		}
 
 		@Override
 		public final void setBoolean(final int address, final boolean value) {
 			Utils.putBoolean(bytes, address, value);
 		}
 
 		@Override
 		public final short getUnsignedByte(final int address) {
 			return Utils.toUnsignedByte(getByte(address));
 		}
 
 		@Override
 		public final void setUnsignedByte(final int address, final short value) {
 			setByte(address, Utils.fromUnsignedByte(value));
 		}
 
 		@Override
 		public final int getUnsignedShort(final int address) {
 			return Utils.toUnsignedShort(getShort(address));
 		}
 
 		@Override
 		public final void setUnsignedShort(final int address, final int value) {
 			setShort(address, Utils.fromUnsignedShort(value));
 		}
 
 		@Override
 		public final long getUnsignedInt(final int address) {
 			return Utils.toUnsignedInt(getInt(address));
 		}
 
 		@Override
 		public final void setUnsignedInt(final int address, final long value) {
 			setInt(address, Utils.fromUnsignedInt(value));
 		}
 
 	}
 
 	final class Utils {
 
 		private Utils() {
 		}
 
 		public static final int U_BYTE = 0xFF;
 		public static final long U_BYTE_L = 0xFFL;
 		public static final int U_SHORT = 0xFFFF;
 		public static final long U_INT = 0xFFFFFFFFL;
 
 		public static short toUnsignedByte(final short value) {
 			return (short) (U_BYTE & value);
 		}
 
 		public static int toUnsignedShort(final int value) {
 			return U_SHORT & value;
 		}
 
 		public static long toUnsignedInt(final long value) {
 			return U_INT & value;
 		}
 
 		public static byte fromUnsignedByte(final short value) {
 			return (byte) value;
 		}
 
 		public static short fromUnsignedShort(final int value) {
 			return (short) value;
 		}
 
 		public static int fromUnsignedInt(final long value) {
 			return (int) value;
 		}
 
 		/*
 		 * Methods for unpacking primitive values from byte arrays starting at
 		 * given offsets.
 		 */
 
 		public static boolean getBoolean(final byte[] b, final int off) {
 			return b[off] != 0;
 		}
 
 		public static char getChar(final byte[] b, final int off) {
 			return (char) (((b[off + 1] & U_BYTE)) | ((b[off] & U_BYTE) << 8));
 		}
 
 		public static short getShort(final byte[] b, final int off) {
 			return (short) (((b[off + 1] & U_BYTE)) | ((b[off] & U_BYTE) << 8));
 		}
 
 		public static int getInt(final byte[] b, final int off) {
 			return ((b[off + 3] & U_BYTE)) | ((b[off + 2] & U_BYTE) << 8)
 					| ((b[off + 1] & U_BYTE) << 16) | ((b[off] & U_BYTE) << 24);
 		}
 
 		public static float getFloat(final byte[] b, final int off) {
 			final int i = ((b[off + 3] & U_BYTE))
 					| ((b[off + 2] & U_BYTE) << 8)
 					| ((b[off + 1] & U_BYTE) << 16) + ((b[off] & U_BYTE) << 24);
 			return Float.intBitsToFloat(i);
 		}
 
 		public static long getLong(final byte[] b, final int off) {
 			return ((b[off + 7] & U_BYTE_L)) | ((b[off + 6] & U_BYTE_L) << 8)
 					| ((b[off + 5] & U_BYTE_L) << 16)
 					+ ((b[off + 4] & U_BYTE_L) << 24)
 					| ((b[off + 3] & U_BYTE_L) << 32)
 					| ((b[off + 2] & U_BYTE_L) << 40)
 					+ ((b[off + 1] & U_BYTE_L) << 48)
 					| ((b[off] & U_BYTE_L) << 56);
 		}
 
 		public static double getDouble(final byte[] b, final int off) {
 			final long j = ((b[off + 7] & U_BYTE_L))
 					| ((b[off + 6] & U_BYTE_L) << 8)
 					| ((b[off + 5] & U_BYTE_L) << 16)
 					+ ((b[off + 4] & U_BYTE_L) << 24)
 					| ((b[off + 3] & U_BYTE_L) << 32)
 					| ((b[off + 2] & U_BYTE_L) << 40)
 					+ ((b[off + 1] & U_BYTE_L) << 48)
 					| ((b[off] & U_BYTE_L) << 56);
 			return Double.longBitsToDouble(j);
 		}
 
 		/*
 		 * Methods for packing primitive values into byte arrays starting at
 		 * given offsets.
 		 */
 
 		public static void putBoolean(final byte[] b, final int off,
 				final boolean val) {
 			b[off] = (byte) (val ? 1 : 0);
 		}
 
 		public static void putChar(final byte[] b, final int off, final char val) {
 			b[off + 1] = (byte) (val);
 			b[off] = (byte) (val >>> 8);
 		}
 
 		public static void putShort(final byte[] b, final int off,
 				final short val) {
 			b[off + 1] = (byte) (val);
 			b[off] = (byte) (val >>> 8);
 		}
 
 		public static void putInt(final byte[] b, final int off, final int val) {
 			b[off + 3] = (byte) (val);
 			b[off + 2] = (byte) (val >>> 8);
 			b[off + 1] = (byte) (val >>> 16);
 			b[off] = (byte) (val >>> 24);
 		}
 
 		public static void putFloat(final byte[] b, final int off,
 				final float val) {
 			final int i = Float.floatToIntBits(val);
 			b[off + 3] = (byte) (i);
 			b[off + 2] = (byte) (i >>> 8);
 			b[off + 1] = (byte) (i >>> 16);
 			b[off] = (byte) (i >>> 24);
 		}
 
 		public static void putLong(final byte[] b, final int off, final long val) {
 			b[off + 7] = (byte) (val);
 			b[off + 6] = (byte) (val >>> 8);
 			b[off + 5] = (byte) (val >>> 16);
 			b[off + 4] = (byte) (val >>> 24);
 			b[off + 3] = (byte) (val >>> 32);
 			b[off + 2] = (byte) (val >>> 40);
 			b[off + 1] = (byte) (val >>> 48);
 			b[off] = (byte) (val >>> 56);
 		}
 
 		public static void putDouble(final byte[] b, final int off,
 				final double val) {
 			final long j = Double.doubleToLongBits(val);
 			b[off + 7] = (byte) (j);
 			b[off + 6] = (byte) (j >>> 8);
 			b[off + 5] = (byte) (j >>> 16);
 			b[off + 4] = (byte) (j >>> 24);
 			b[off + 3] = (byte) (j >>> 32);
 			b[off + 2] = (byte) (j >>> 40);
 			b[off + 1] = (byte) (j >>> 48);
 			b[off] = (byte) (j >>> 56);
 		}
 
 	}
 }
