 package com.JTFTP;
 
 import java.util.*;
 import java.io.*;
 
 /**
  * This class provides a fixed length buffer in which we can save short's and strings.
  */
 public class Buffer {
 
 	private byte buffer[];
	private short offset;
 
 	/**
 	 * Creates an empty buffer with length bytes capacity (including separators of strings).
 	 */
 	public Buffer(int length) {
 		buffer = new byte[length];
 		reset();
 	}
 
 	/**
 	 * Creates a buffer from byte array.
 	 */
 	public Buffer(byte[] buffer) {
 		this.buffer = buffer;
 	}
 
 	/**
 	 * Deletes the content of buffer.
 	 */
 	public void reset() {
 		Arrays.fill(buffer, (byte)0);
 		offset = 0;
 	}
 
 	/**
 	 * Save the string data into buffer.
 	 * @param data is the string to save.
 	 * @throws UnsupportedEncodingException if US-ASCII encoding is not supported.
 	 * @throws ArrayIndexOutOfBoundsException if data not fit in the buffer.
 	 */
 	public void addString(String data) throws UnsupportedEncodingException, ArrayIndexOutOfBoundsException {
 		boolean status;
 		byte[] tmpByteArray;
 
 		tmpByteArray = data.getBytes("US-ASCII");
 		if(buffer.length < offset + tmpByteArray.length + 1) {
 			throw new ArrayIndexOutOfBoundsException("The string not fit in the buffer.");
 		}
 
 		System.arraycopy(tmpByteArray, 0, buffer, offset, tmpByteArray.length);
 
 		offset += tmpByteArray.length;
 		buffer[offset] = 0;
 
 		offset++;
 	}
 
 	/**
 	 * Gets a string of the buffer.
 	 * @throws UnsupportedEncodingException if US-ASCII encoding is not supported.
 	 * @return the next string of the buffer.
 	 * @throws ArrayIndexOutOfBoundsException if there is no string to read in this buffer.
 	 */
 	public String getString() throws UnsupportedEncodingException, ArrayIndexOutOfBoundsException {
 		int tmpOffset = offset;
 		byte[] tmpByteArray = new byte[buffer.length-tmpOffset];
 		int i = 0;
 
 		while(tmpOffset < buffer.length && buffer[tmpOffset] != 0) {
 			tmpByteArray[i] = buffer[offset];
 
 			i++;
 			tmpOffset++;
 		}
 
 		if(tmpOffset == buffer.length) {
 			throw new ArrayIndexOutOfBoundsException("There is no string to read in this buffer.");
 		}
 
 		offset = tmpOffset + 1;
 
 		return new String(tmpByteArray, "US-ASCII");
 	}
 
 	/**
 	 * Save a short into the buffer.
 	 * @param data is the integer to save.
 	 * @throws ArrayIndexOutOfBoundsException if data not fit in the buffer.
 	 */
 	public void addShort(short data) throws ArrayIndexOutOfBoundsException {
 		if(buffer.length < offset + 2) {
 			throw new ArrayIndexOutOfBoundsException("The short not fit in the buffer.");
 		}
 		buffer[offset] = (byte) ((data & 0xFF00) >> 8);
 		offset++;
 		buffer[offset] = (byte) (data & 0x00FF);
 		offset++;
 	}
 
 	/**
 	 * Gets a short of the buffer.
 	 * @return the next short of the buffer.
 	 * @throws ArrayIndexOutOfBoundsException if there aren't 2 or more bytes to read. 
 	 */
 	public short getShort() throws ArrayIndexOutOfBoundsException {
 		if(buffer.length < offset + 2) {
 			throw new ArrayIndexOutOfBoundsException("Threre are less than 2 bytes to read and short can't be obtained.");
 		}
 		return (short) (buffer[offset++] << 8 | buffer[offset++]);
 	}
 
 	/**
 	 * Gets buffer's content.
 	 * @return byte array with buffer's content.
 	 */
 	public byte[] dumpBuffer() {
 		return buffer;
 	}
 
 	/**
 	 * Set buffer as content of Buffer and reset the offset.
 	 * @param buffer is a byte array with the new content of Buffer.
 	 */
 	public void setBuffer(byte[] buffer) {
 		this.buffer = buffer;
 		offset = 0;
 	}
 
 	/**
 	 * Prints min(max(howMuch, 0), offset) bytes of buffer.
 	 * @param howMuch indicates how many bytes have to print.
 	 */
 	public void printBuffer(int howMuch, boolean toString) {
 		int bytesToPrint = (howMuch > 0) ? howMuch : 0;
 		bytesToPrint = (bytesToPrint < offset) ? bytesToPrint : offset;
 		for (int i = 0; i < bytesToPrint; i++) {
 			System.out.print(buffer[i] + ", ");
 		}
 	}
 
 	/**
 	 * Copy the content of buffer and reset the offset.
 	 * @param buffer is the Buffer to copy.
 	 */
 	public void copyBuffer(Buffer buffer) {
 		this.buffer = buffer.dumpBuffer();
 		offset = 0;
 	}
 }
