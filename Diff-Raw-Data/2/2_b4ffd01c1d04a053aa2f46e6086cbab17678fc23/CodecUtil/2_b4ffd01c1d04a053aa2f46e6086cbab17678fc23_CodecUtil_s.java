 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.io.HexDump;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 
 /**
  * 
  * 
  * @author wangmaolin
  * @since 0.1.0
  */
 public class CodecUtil {
 
 	/**
 	 * Prints a pretty debug output of byte content.
 	 * 
 	 * @param log
 	 * @param raw
 	 */
 	public static final void debugRaw(Logger log, byte[] raw) {
 
 		if (log == null)
 			return;
 		if (raw == null)
 			return;
 
 		if (log.isDebugEnabled()) {
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			// use hex dump to output
 			try {
 				HexDump.dump(raw, 0, os, 0);
 				String s = os.toString("UTF-8");
 				log.trace("bytes (hex) ({} bytes)\n{}\n", raw.length, s);
 			} catch (Throwable e) {
 				log.warn("Failed to dump hex raw bytes");
 			} finally {
 				try {
 					if (os != null)
 						os.close();
 				} catch (Throwable t) {
 					// mute
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Returns a nice hex dump representation of the source byte.
 	 * 
 	 * @param src
 	 * @return
 	 */
 	public static final String hexDumpAsString(byte[] src) {
 		return hexDumpAsString(src, "UTF-8");
 	}
 
 	/**
 	 * Returns a nice hex dump representation of the source byte. 
 	 * 
 	 * @param src
 	 * @param encoding the encoding to use.
 	 * @return
 	 */
 	public static final String hexDumpAsString(byte[] src, String charset) {
 
 		// print it
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		// use hex dump to output
 		try {
 			HexDump.dump(src, 0, os, 0);
 			String s = os.toString(charset);
 			return s;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return null;
 		} catch (IllegalArgumentException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		} finally {
 			try {
 				if (os != null) os.close();
 			} catch (Throwable t) {
 				// mute error
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param log
 	 * @param message
 	 * @param maxHexDumpLength
 	 */
 	public static final void hexDumpForLogging(Logger log, Object message,
 			int maxHexDumpLength) {
 		
 		// do nothing if not IoBuffer
 		if (!(message instanceof IoBuffer))
 			return;
 		
 		IoBuffer buffer = (IoBuffer)message;
 		
 		hexDumpForLogging(log, buffer, maxHexDumpLength);
 		
 
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param log
 	 * @param message
 	 * @param maxHexDumpLength
 	 */
 	public static final void hexDumpForLogging(Logger log, byte[] buf,
 			int maxHexDumpLength) {
 		
 		// do nothing if no need to print.
 		if (maxHexDumpLength <= 0)
 			return;
 
 		// number of available bytes to read.
 		int remaining = buf.length;
 		// the actual length to read.
 		int partLength = remaining < maxHexDumpLength ? remaining
 				: maxHexDumpLength;
 
 		try {
 			// copy the target bytes to print
 			byte[] part = new byte[partLength];
 			System.arraycopy(buf, 0, part, 0, partLength);
			int omitted = partLength - remaining;
 			
 			String hexDump = CodecUtil.hexDumpAsString(part);
 
 			// use hex dump to output
 			if (log.isTraceEnabled()) {
 				log.trace("Raw bytes: (Printing {} of {} bytes, {} omitted)\n{}",
 						new Object[] { partLength, remaining, omitted, hexDump });
 			}
 
 		} finally {
 		}
 
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param log
 	 * @param message
 	 * @param maxHexDumpLength
 	 */
 	public static final void hexDumpForLogging(Logger log, IoBuffer buffer,
 			int maxHexDumpLength) {
 		
 		// do nothing if no need to print.
 		if (maxHexDumpLength <= 0)
 			return;
 
 		// print the buffer
 		if (!buffer.hasRemaining())
 			return;
 
 		// remember the position
 		int position = buffer.position();
 		// number of available bytes to read.
 		int remaining = buffer.remaining();
 		// the actual length to read.
 		int partLength = remaining < maxHexDumpLength ? remaining
 				: maxHexDumpLength;
 
 		try {
 			// copy the target bytes to print
 			byte[] part = new byte[partLength];
 			buffer.get(part);
 			int omitted = buffer.remaining();
 			
 			String hexDump = CodecUtil.hexDumpAsString(part);
 
 			// use hex dump to output
 			if (log.isTraceEnabled()) {
 				log.trace("Raw bytes: (Printing {} of {} bytes, {} omitted)\n{}",
 						new Object[] { partLength, remaining, omitted, hexDump });
 			}
 
 		} finally {
 			// must reset the position after reading from IoBuffer!
 			buffer.position(position);
 		}
 
 	}
 
 }
