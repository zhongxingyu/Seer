 package com.mymed.model.core.wrappers.cassandra.api07;
 
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 
 /**
  * Convenience class to convert to and from {@link ByteBuffer} for the most used
  * data types.
  * 
  * @author Milo Casagrande
  * 
  */
 public final class MConverter {
 
 	/**
 	 * The default charset to use
 	 */
 	private static final Charset CHARSET = Charset.forName("UTF-8");
 
 	/**
 	 * The size of a long in bytes
 	 */
 	private static final int LONG_SIZE = Long.SIZE / 8;
 
 	/**
 	 * The size of a double in bytes
 	 */
 	private static final int DOUBLE_SIZE = Double.SIZE / 8;
 
 	/**
 	 * The size of an int in bytes
 	 */
 	private static final int INT_SIZE = Integer.SIZE / 8;
 
 	/**
 	 * The size of a byte in bytes
 	 */
 	private static final int BYTE_SIZE = Byte.SIZE / 8;
 
 	/**
 	 * Private constructor to avoid warnings since class is all static, or we
 	 * should implement a singleton
 	 */
 	private MConverter() {
 	};
 
 	/**
 	 * Convert a string into a {@link ByteBuffer}
 	 * 
 	 * @param string
 	 *            the string to convert
 	 * @return the string converted into a {@link ByteBuffer}
 	 * @throws InternalBackEndException
 	 *             if the string is null, or when a wrong encoding is used
 	 */
 	public static ByteBuffer stringToByteBuffer(final String string) throws InternalBackEndException {
 
 		if (string == null) {
 			throw new InternalBackEndException("You need to provide a non-null value");
 		}
 
 		final CharBuffer charBuffer = CharBuffer.wrap(string);
 		ByteBuffer returnBuffer = null;
 
 		try {
 			returnBuffer = CHARSET.newEncoder().encode(charBuffer);
 		} catch (final CharacterCodingException ex) {
 			throw new InternalBackEndException("Wrong encoding used in the message");
 		}
 
 		return returnBuffer;
 	}
 
 	/**
 	 * Convert a list of strings in a list with the strings converted into
 	 * {@link ByteBuffer}
 	 * 
 	 * @param list
 	 *            the list with the strings to convert
 	 * @return the list with the strings converted
 	 * @throws InternalBackEndException
 	 *             if one of the strings is null, or when a wrong encoding is
 	 *             used
 	 */
 	public static List<ByteBuffer> stringToByteBuffer(final List<String> list) throws InternalBackEndException {
 
 		final List<ByteBuffer> result = new ArrayList<ByteBuffer>(list.size());
 
 		for (final String string : list) {
 			result.add(stringToByteBuffer(string));
 		}
 
 		return result;
 	}
 
 	/**
 	 * Convert a {@link ByteBuffer} back into a string
 	 * 
 	 * @param buffer
 	 *            the {@link ByteBuffer} to convert
 	 * @return the string
 	 * @throws InternalBackEndException
 	 *             if the buffer is null, or when a wrong encoding is used
 	 */
 	public static String byteBufferToString(final ByteBuffer buffer) throws InternalBackEndException {
 
 		if (buffer == null) {
 			throw new InternalBackEndException("You need to provide a non-null value");
 		}
 
 		String returnString = null;
 
 		try {
 			final CharBuffer charBuf = CHARSET.newDecoder().decode(buffer);
 			returnString = charBuf.toString();
 		} catch (final CharacterCodingException ex) {
 			throw new InternalBackEndException("Wrong encoding used in the message");
 		}
 
 		return returnString;
 	}
 
 	/**
 	 * Convert an integer value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the integer to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer intToByteBuffer(final int value) {
 
 		final ByteBuffer buffer = ByteBuffer.allocate(INT_SIZE);
 
 		buffer.clear();
 		buffer.putInt(value);
 		buffer.compact();
 
 		return buffer;
 	}
 
 	/**
 	 * Convert an integer value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the integer to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer intToByteBuffer(final Integer value) {
 		return intToByteBuffer(value.intValue());
 	}
 
 	/**
 	 * Convert a {@link ByteBuffer} into an integer
 	 * <p>
 	 * No assumptions are made on the value stored in the ByteBuffer, it has to
 	 * be an integer
 	 * 
 	 * @param buffer
 	 *            the ByteBuffer that holds the integer value
 	 * @return the integer value
 	 */
 	public static int byteBufferToInt(final ByteBuffer buffer) {
 
 		buffer.compact();
 		buffer.clear();
 
 		return buffer.getInt();
 	}
 
 	/**
 	 * Convert a long value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the long to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer longToByteBuffer(final long value) {
 
 		final ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE);
 
 		buffer.clear();
 		buffer.putLong(value);
 		buffer.compact();
 
 		return buffer;
 	}
 
 	/**
 	 * Convert a long value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the long to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer longToByteBuffer(final Long value) {
 		return longToByteBuffer(value.longValue());
 	}
 
 	/**
 	 * Convert a {@link ByteBuffer} into an long
 	 * <p>
 	 * No assumptions are made on the value stored in the ByteBuffer, it has to
 	 * be a long
 	 * 
 	 * @param buffer
 	 *            the ByteBuffer that holds the long value
 	 * @return the long value
 	 */
 	public static long byteBufferToLong(final ByteBuffer buffer) {
 
 		buffer.compact();
 		buffer.clear();
 
 		return buffer.getLong();
 	}
 
 	/**
 	 * Convert a byte value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the byte value to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer byteToByteBuffer(final byte value) {
 
 		final ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE);
 
 		buffer.clear();
 		buffer.put(value);
 		buffer.compact();
 
 		return buffer;
 	}
 
 	/**
 	 * Convert a byte value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the byte value to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer byteToByteBuffer(final Byte value) {
 		return byteToByteBuffer(value.byteValue());
 	}
 
 	/**
 	 * Convert a {@link ByteBuffer} into a byte
 	 * <p>
 	 * No assumptions are made on the value stored in the ByteBuffer, it has to
 	 * be a byte
 	 * 
 	 * @param buffer
 	 *            the ByteBuffer that holds the byte value
 	 * @return the byte value
 	 */
 	public static byte byteBufferToByte(final ByteBuffer buffer) {
 
 		buffer.compact();
 		buffer.clear();
 
 		return buffer.get();
 	}
 
 	/**
 	 * Convert a double value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the double to convert
 	 * @return the ByteBuffer value
 	 */
	public static ByteBuffer doubleToByteBuffer(final double value) {
 
 		final ByteBuffer buffer = ByteBuffer.allocate(DOUBLE_SIZE);
 
 		buffer.clear();
 		buffer.putDouble(value);
 		buffer.compact();
 
 		return buffer;
 	}
 
 	/**
 	 * Convert a double value into a {@link ByteBuffer}
 	 * 
 	 * @param value
 	 *            the double to convert
 	 * @return the ByteBuffer value
 	 */
 	public static ByteBuffer doubleToByteBuffer(final Double value) {
 		return doubleToByteBuffer(value.doubleValue());
 	}
 
 	/**
 	 * Convert a {@link ByteBuffer} into an double
 	 * <p>
 	 * No assumptions are made on the value stored in the ByteBuffer, it has to
 	 * be a double
 	 * 
 	 * @param buffer
 	 *            the ByteBuffer that holds the double value
 	 * @return the long value
 	 */
 	public static double byteBufferToDouble(final ByteBuffer buffer) {
 
 		buffer.compact();
 		buffer.clear();
 
 		return buffer.getDouble();
 	}
 }
