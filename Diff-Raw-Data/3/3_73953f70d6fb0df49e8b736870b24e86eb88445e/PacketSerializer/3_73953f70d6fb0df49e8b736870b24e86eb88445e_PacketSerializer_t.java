 /*
  * Copyright 2010-2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package com.googlecode.jcimd;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * <p>
  * Serializes/deserializes CIMD packets in the following format:
  * </p>
  * <pre>
  *   HEADER         Parameter List             TRAILER
  * &lt;STX&gt;ZZ:NNN&lt;TAB&gt;PPP:Parameter value&lt;TAB&gt;...CC&lt;ETX&gt;
  *   ^   ^  ^   ^  ^                           ^  ^
  *   |   |  |   |  |                           |  |
  *   |   |  |   |  |                           |  +-- (end of text)
  *   |   |  |   |  |                           +-- check sum (two bytes)
  *   |   |  |   |  + parameter code (three bytes)
  *   |   |  |   |
  *   |   |  |   +-- delimiter
  *   |   |  |
  *   |   |  +-- packet number (three bytes)
  *   |   +-- operation code (two bytes)
  *   +-- (start of text)
  * </pre>
  * <p>
  * The packet sequence number is generated using an optional
  * {@link PacketSequenceNumberGenerator}. If none is specified, then all packets
  * are expected to have sequence numbers. Otherwise, an exception is thrown.
  * <p>
  * The &lt;STX&gt;, &lt;TAB&gt;, &quot;:&quot; (COLON), and &lt;ETX&gt; bytes are
  * <em>not</em> part of the {@link Packet packet bean}. Instead, it is added upon
  * serialization by this class.
  * <p>
  * To protect against buffer overflow, this class uses a {@link #setMaxMessageSize(int)
  * maximum message size} which defaults to 4096 (1024 * 4) bytes.
  * 
  * @author Lorenzo Dee
  *
  * @see #setSequenceNumberGenerator(PacketSequenceNumberGenerator)
  */
 public class PacketSerializer {
 
 	public static final byte STX = 0x02;
 	public static final byte TAB = '\t'; // ASCII value is 9
 	public static final byte COLON = ':'; // ASCII value is 58
 	public static final byte ETX = 0x03;
 	public static final byte NUL = 0x00;
 	public static final int END_OF_STREAM = -1;
 
 	private static final int DEFAULT_MAX_SIZE = 1024 * 4;
 
 	private final Log logger;
 
 	private final boolean useChecksum; 
 	private int maxMessageSize = DEFAULT_MAX_SIZE;
 
 	private PacketSequenceNumberGenerator sequenceNumberGenerator; 
 
 	/**
 	 * Constructs a serializer that uses and expects a two-byte checksum.
 	 */
 	public PacketSerializer() {
 		this(null, true);
 	}
 
 	/**
 	 * Constructs a serializer with the given name that uses
 	 * and expects a two-byte checksum.
 	 * @param name name of this serializer (used in logging)
 	 */
 	public PacketSerializer(String name) {
 		this(name, true);
 	}
 
 	/**
 	 * Constructs a serializer with the given name that will use
 	 * two-byte checksum based on flag.
 	 * @param name name of this serializer (used in logging)
 	 * @param useChecksum flag to indicate to use checksum
 	 */
 	public PacketSerializer(String name, boolean useChecksum) {
 		if (name != null) {
 			this.logger = LogFactory.getLog(
 					this.getClass().getName() + "." + name);
 		} else {
 			this.logger = LogFactory.getLog(this.getClass());
 		}
 		this.useChecksum = useChecksum;
 	}
 
 	public int getMaxMessageSize() {
 		return maxMessageSize;
 	}
 
 	public void setMaxMessageSize(int maxMessageSize) {
 		if (maxMessageSize <= 0) {
 			throw new IllegalArgumentException(
 					"maxMessageSize must be greater than zero");
 		}
 		this.maxMessageSize = maxMessageSize;
 	}
 
 	public PacketSequenceNumberGenerator getSequenceNumberGenerator() {
 		return sequenceNumberGenerator;
 	}
 
 	public void setSequenceNumberGenerator(
 			PacketSequenceNumberGenerator sequenceNumberGenerator) {
 		this.sequenceNumberGenerator = sequenceNumberGenerator;
 	}
 
 	public void serialize(Packet packet, OutputStream outputStream)
 			throws IOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Sending " + packet);
 		}
 		byte[] bytes = serializeToByteArray(packet);
 		outputStream.write(bytes);
 		if (this.useChecksum) {
 			int checkSum = calculateCheckSum(bytes);
 			AsciiUtils.writeIntAsHexAsciiBytes(checkSum, outputStream, 2);
 		}
 		outputStream.write(ETX);
 	}
 
 	private byte[] serializeToByteArray(Packet packet) throws IOException {
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		outputStream.write(STX);
 		AsciiUtils.writeIntAsAsciiBytes(
 				packet.getOperationCode(), outputStream, 2);
 		outputStream.write(COLON);
 		Integer sequenceNumber = packet.getSequenceNumber();
 		if (sequenceNumber == null) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("No sequence number in packet, generating one...");
 			}
 			if (this.sequenceNumberGenerator != null) {
 				sequenceNumber = this.sequenceNumberGenerator.nextSequence();
 				if (logger.isTraceEnabled()) {
 					logger.trace("Generated " + sequenceNumber + " as sequence number");
 				}
 			} else {
 				String message = "No sequence number generator. " +
 						"Please see PacketSerializer#setSequenceNumberGenerator(" +
 						"PacketSequenceNumberGenerator)";
 				logger.error(message);
 				throw new IOException(message);
 			}
 		}
 		AsciiUtils.writeIntAsAsciiBytes(
 				sequenceNumber, outputStream, 3);
 		outputStream.write(TAB);
 		for (Parameter parameter : packet.getParameters()) {
 			AsciiUtils.writeIntAsAsciiBytes(
 					parameter.getNumber(), outputStream, 3);
 			outputStream.write(COLON);
 			AsciiUtils.writeStringAsAsciiBytes(
 					parameter.getValue(), outputStream);
 			outputStream.write(TAB);
 		}
 		return outputStream.toByteArray();
 	}
 
 	/**
 	 * Calculates the check sum of the given bytes.
 	 *
 	 * @param bytes the array from which a check sum is calculated
 	 * @return the check sum
 	 */
 	private int calculateCheckSum(byte[] bytes) {
 		return calculateCheckSum(bytes, 0, bytes.length);
 	}
 
 	/**
 	 * Calculates the check sum of the given range of bytes.
 	 *
 	 * @param bytes the array from which a check sum is calculated
 	 * @param from the initial index of the range to be copied, inclusive
 	 * @param to the final index of the range to be copied, exclusive.
 	 *     (This index may lie outside the array.)
 	 * @return the check sum
 	 */
 	private int calculateCheckSum(byte[] bytes, int from, int to) {
 		int sum = 0;
 		for (int i = from; i < to; i++) {
 			sum += bytes[i];
 			sum &= 0xFF;
 		}
 		return sum;
 	}
 
 	public Packet deserialize(InputStream inputStream) throws IOException {
 		ByteArrayOutputStream temp = new ByteArrayOutputStream();
 		int b;
 		while ((b = inputStream.read()) != END_OF_STREAM) {
 			// Any data transmitted between packets SHALL be ignored.
 			if (b == STX) {
 				temp.write(b);
 				break;
 			}
 		}
 		if (b != STX) {
 			//throw new SoftEndOfStreamException();
 			throw new IOException(
 				"End of stream reached and still no <STX> byte");
 		}
 		// Read the input stream until ETX
 		while ((b = inputStream.read()) != END_OF_STREAM) {
 			temp.write(b);
 			if (b == ETX) {
 				break;
 			}
 			if (temp.size() >= getMaxMessageSize()) {
 				// Protect from buffer overflow
 				throw new IOException(
 						"Buffer overflow reached at " + temp.size()
 						+ " byte(s) and still no <ETX> byte");
 			}
 		}
 		if (b != ETX) {
 			throw new IOException(
 					"End of stream reached and still no <ETX> byte");
 		}
 
 		// Parse contents of "temp" (it contains the entire CIMD message
 		// including STX and ETX bytes).
 		byte bytes[] = temp.toByteArray();
 
 		if (logger.isTraceEnabled()) {
 			logger.trace("Received " + bytes.length + " byte(s)");
 		}
 
 		if (useChecksum) {
 			// Read two (2) bytes, just before the ETX byte.
 			StringBuilder buffer = new StringBuilder(2);
 			buffer.append((char) bytes[bytes.length - 3]);
 			buffer.append((char) bytes[bytes.length - 2]);
 			try {
 				int checksum = Integer.valueOf(buffer.toString(), 16);
 				int expectedChecksum = calculateCheckSum(bytes, 0, bytes.length - 3);
 				if (checksum != expectedChecksum) {
 					throw new IOException(
 							"Checksum error: expecting " + expectedChecksum
 							+ " but got " + checksum);
 				}
 			} catch (NumberFormatException e) {
 				throw new IOException(
 						"Checksum error: expecting HEX digits, but got " + buffer);
 			}
 		}
 
 		// Deserialize bytes, minus STX, CC (check sum), and ETX.
		int end = useChecksum ? bytes.length - 3 : bytes.length -1;
		Packet packet = deserializeFromByteArray(bytes, 1, end);
 		if (logger.isDebugEnabled()) {
 			logger.debug("Received " + packet);
 		}
 		return packet;
 	}
 
 	private Packet deserializeFromByteArray(
 			byte[] bytes, int from, int to) throws IOException {
 		StringBuilder buffer = new StringBuilder();
 		int i = from;
 
 		// Read the operation code and packet number
 		i = readToBufferUntil(bytes, i, to, 2, buffer, COLON);
 		int operationCode = Integer.valueOf(buffer.toString());
 		buffer.setLength(0);
 		i = readToBufferUntil(bytes, i, to, -1, buffer, TAB);
 		int sequenceNumber = Integer.valueOf(buffer.toString());
 		buffer.setLength(0);
 
 		// Read the parameters
 		List<Parameter> parameters = new LinkedList<Parameter>();
 		while (i < to) {
 			i = readToBufferUntil(bytes, i, to, 3, buffer, COLON);
 			int parameterType = Integer.valueOf(buffer.toString());
 			buffer.setLength(0);
 			i = readToBufferUntil(bytes, i, to, -1, buffer, TAB);
 			String parameterValue = buffer.toString();
 			buffer.setLength(0);
 			parameters.add(new Parameter(parameterType, parameterValue));
 		}
 
 		return new Packet(operationCode, sequenceNumber,
 				parameters.toArray(new Parameter[0]));
 	}
 
 	/**
 	 * Reads bytes and appends to buffer until the delimiter is reached,
 	 * or the <em>to</em> is reached, or a reserved characters is reached.
 	 * The reserved characters 0x00 (NUL), 0x02 (STX), 0x03 (ETX), 0x09
 	 * (TAB) are not allowed in any parameter.
 	 *
 	 * @param bytes the array of bytes to read
 	 * @param from the initial index of the range to be read, inclusive
 	 * @param to the final index of the range to be read, exclusive.
 	 *     (This index may lie outside the array.)
 	 * @param maxOffset the maximum offset that can be read before reaching
 	 *     delimiter. If this offset is exceeded, and no delimiter was
 	 *     reached, an exception will be thrown.
 	 * @param buffer the buffer to append to
 	 * @param delimiter the delimiter to reach
 	 * @return the index (between <em>from</em> and <em>to</em>)
 	 *     when the delimiter was reached
 	 * @throws IOException if a reserved character was reached, and it is
 	 *     not the expected <em>delimiter</em>.
 	 */
 	private int readToBufferUntil(
 			byte[] bytes, int from, int to, int maxOffset,
 			StringBuilder buffer, byte delimiter)
 	throws IOException {
 		int i = from;
 		while ((i < to) && (bytes[i] != delimiter)
 				// The reserved characters 0x00 (NUL), 0x02 (STX),
 				// 0x03 (ETX), 0x09 (TAB) are not allowed in any parameter
 				&& (bytes[i] != NUL) && (bytes[i] != STX)
 				&& (bytes[i] != ETX) && (bytes[i] != TAB)) {
 			buffer.append((char) bytes[i]);
 			i++;
 			if ((maxOffset > 0) && ((i - from) > maxOffset)) {
 				throw new IOException(
 						"Expecting 0x" + Integer.toHexString(delimiter)
 						+ " within " + maxOffset + " byte(s), " +
 								"but got 0x" + Integer.toHexString(bytes[i - 1]));
 			}
 		}
 		if (bytes[i] != delimiter) {
 			throw new IOException(
 					"Expecting 0x" + Integer.toHexString(delimiter)
 					+ " but got 0x" + Integer.toHexString(bytes[i]));
 		} else {
 			i++;
 		}
 		return i;
 	}
 
 }
