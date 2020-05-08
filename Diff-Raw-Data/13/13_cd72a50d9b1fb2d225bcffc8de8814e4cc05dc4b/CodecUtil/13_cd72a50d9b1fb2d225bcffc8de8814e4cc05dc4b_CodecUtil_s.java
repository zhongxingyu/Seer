 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.io.ByteArrayOutputStream;
 
 import org.apache.commons.io.HexDump;
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
 
 		if (log == null) return;
 		if (raw == null) return;
 		
 		if (log.isDebugEnabled()) {
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			// use hex dump to output
 			try {
 				HexDump.dump(raw, 0, os, 0);
 				String s = os.toString("UTF-8");
				log.debug("bytes (hex) ({} bytes) {}\n", raw.length, s);
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
 
 }
