 package ie.wombat.ha.nic.xbee;
 
 
 import ie.wombat.ha.ByteFormatUtils;
 import ie.wombat.ha.DebugUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 
 import org.apache.log4j.Logger;
 
 /**
  * Various static utility methods related to the XBee NIC.
  * 
  * @author joe
  *
  */
 public class XBeeUtil implements XBeeConstants {
 	private static Logger log = Logger.getLogger(XBeeUtil.class);
 	
 	/**
 	 * From XBee API frame payload (starting with the API frame type byte)
 	 * construct an encoded API frame suitable for transmission to the XBee
 	 * UART. This involves prefixing a Start-of-Frame delimiter (0x7E), 
 	 * packet length and suffixing a checksum. In addition to this any byte values
 	 * of 0x7E (Start-of-Frame), 0x7D (Escape), 0x11 (Xon), 0x13 (Xoff) 
 	 * after the Start-of-Frame delimiter must be escaped. See chapter 9
 	 * of the XBee manual for details.
 	 * @param payload
 	 * @return
 	 */
 	public static byte[] encodeAPIFrame (byte[] payload, int payloadLen) {
 
 		// Annoyingly methods in use here through checked IOException
 		// even though the chances of this happening are close to 0.
 		// Therefore enclose the entire method in a try/catch.
 
 		try {
 			log.debug("encodeAPIFrame(): inlen="
 					+ payload.length
 					+ " "
 					+ DebugUtils
 							.formatXBeeAPIFrame(payload, 0, payloadLen));
 
 			ByteArrayOutputStream xbeeOut = new ByteArrayOutputStream();
 
 			xbeeOut.write(START_OF_FRAME);
 			XBeeUtil.writeEscapedByte(payloadLen >> 8, xbeeOut);
 			XBeeUtil.writeEscapedByte(payloadLen & 0xFF, xbeeOut);
 
 			// Checksum
 			int cs = 0;
 
 			for (int i = 0; i < payloadLen; i++) {
 				XBeeUtil.writeEscapedByte(payload[i], xbeeOut);
 				cs += payload[i];
 			}
 
 			cs = 0xff - cs;
 
 			// Send checksum
 			XBeeUtil.writeEscapedByte(cs, xbeeOut);
 
 			byte[] apipacket = xbeeOut.toByteArray();
 			log.debug("encodeAPIPacket(): outlen="
 					+ apipacket.length
 					+ " "
 					+ DebugUtils.formatXBeeAPIFrame(apipacket, 0,
 							apipacket.length));
 
 			return apipacket;
 		} catch (IOException e) {
 			// this should *never* happen
 			log.error("encodeAPIPacket(): unexpected IOException");
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	
 	public static byte[] decodeAPIFrame(byte[] encpacket, int encpacketLen) {
 
 		try {
 			ByteArrayInputStream xbeeIn = new ByteArrayInputStream(encpacket,
 					0, encpacketLen);
 			int b = xbeeIn.read();
 
 			if (b != START_OF_FRAME) {
 				log.error("decodeAPIPacket(): Expecting START_OF_PACKET");
 				return null;
 			}
 
 			int payloadLen;
 
 			payloadLen = XBeeUtil.readEscapedByte(xbeeIn) << 8;
 			payloadLen |= XBeeUtil.readEscapedByte(xbeeIn);
 
 			byte[] payload = new byte[payloadLen];
 
 			// Checksum
 			int cs = 0;
 
 			for (int i = 0; i < payloadLen; i++) {
 				b = XBeeUtil.readEscapedByte(xbeeIn);
 				payload[i] = (byte) b;
 				cs += b;
 			}
 
 			// Read checksum
 			cs += XBeeUtil.readEscapedByte(xbeeIn);
 			cs &= 0xff;
 			if (cs != 0xff) {
 				log.error("decodeAPIFrame(): checksum fail");
 				// throw new IOException ("XBee packet checksum");
 				return null;
 			}
 
 			return payload;
 
 		} catch (IOException e) {
 			// this should *never* happen
 			log.error("encodeAPIFrame(): unexpected IOException");
 			e.printStackTrace();
 			return null;
 		}
 
 	}
 	
 
 	
 	public static final int readEscapedByte (InputStream in) throws IOException {
 		int b = in.read();
 		if ( b == -1) {
 			throw new IOException ("Timeout");
 		}
 		return (b == ESCAPE ? (in.read()^0x20) : b);
 	}
 	
 	public static final void writeEscapedByte (int b, OutputStream out) throws IOException {
 		//System.err.print("[" + formatHexByte(b) + "] ");
 		switch (b) {
 		case START_OF_FRAME:
 		case XON:
 		case XOFF:
 			out.write(ESCAPE);
 			out.write(b ^ 0x20);
 			break;
 		default:
 			out.write(b);
 		}	
 	}
 	public static final void writeEscapedByte (byte b, OutputStream out) throws IOException {
 		writeEscapedByte(b & 0xFF, out);
 	}
 	
 	/**
 	 * Write escaped byte to byte[] array
 	 * @param b
 	 * @param buf
 	 * @param ptr
 	 * @return
 	 * @throws IOException
 	 */
 	public static final int writeEscapedByte (byte b, byte[] buf, int ptr) throws IOException {
 		switch (b) {
 		case START_OF_FRAME:
 		case XON:
 		case XOFF:
 			buf[ptr++] = ESCAPE;
 			buf[ptr++] = (byte)(b ^ 0x20);
 			break;
 		default:
 			buf[ptr++] = b;
 		}	
 		return ptr;
 	}
 	
 	public static void writeAPIFrameToStream (byte[] packet, OutputStream out) throws IOException {
 		writeAPIFrameToStream(packet, packet.length, out);
 	}
 	
 	public static void writeAPIFrameToStream (byte[] packet, int packetLen, OutputStream out) throws IOException {
 
 		// API packet Start-of-frame delimiter
 		out.write(0x7E);
 		
 		// API packet (unescaped) length
 		writeEscapedByte(packetLen>>8, out);
 		writeEscapedByte(packetLen & 0xff, out);
 		
 		// Escaped data
 		int cs = 0;
 		for (int i = 0; i < packetLen; i++) {
 			writeEscapedByte(packet[i],out);
 			cs += packet[i];
 		}
 		
 		// Checksum (XBee doc chapter 9)
 		cs = 0xff - (cs&0xff);
 		writeEscapedByte(cs,out);
 	}
 	
 	public static int readAPIFrameFromStream (InputStream xbeeIn, byte[] packet) throws IOException {
 		int b, i;
 		
 		i = 0;
 		while ( (b = xbeeIn.read()) != START_OF_FRAME ) {
			log.trace("Expecting START_OF_FRAME, skipping " + ByteFormatUtils.formatHexByte(b));
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 			}
 			if (++i > 150) {
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 				}
 				throw new IOException ("Start of packet delimiter not found after reading " + i + " bytes");
 			}
 		}
 		if (i > 0) {
 			log.warn("readAPIFrameFromStream(): warning skipped " + i + " bytes to get to START_OF_FRAME");
 		}
 	
 		int packetLen;
 		packetLen = XBeeUtil.readEscapedByte(xbeeIn) << 8;
 		packetLen |= XBeeUtil.readEscapedByte(xbeeIn);
 		
 		if (packetLen > 255) {
 			throw new IOException ("XBee packet too long, max 255, got " + packetLen);
 		}
 		//byte[] packet = new byte[packetLen];
 		
 		int cs=0;
 		for (i = 0; i < packetLen; i++) {
 			b = XBeeUtil.readEscapedByte(xbeeIn);
 			packet[i] = (byte)b;
 			cs += b;
 		}
 		
 		// Read checksum
 		cs += XBeeUtil.readEscapedByte(xbeeIn);
 		cs &= 0xff;
 		if ( cs != 0xff ) {
 			throw new IOException ("XBee API frame checksum fail: csCalc=0x"
 					+ Integer.toHexString(cs)
 					+ " (should be 0xff) data="
 					+ ByteFormatUtils.byteArrayToString(packet,0,packetLen)
 					);
 		}
 		
 		return packetLen;
 		
 	}
 	
 	
 	public static void displayZDOResponse (Writer w, int clusterId, byte[] packet, int start) throws IOException {
 		int i;
 		switch (clusterId) {
 		
 		case 0x8031:
 			w.write ("NeighborTable: ");
 			w.write (" status=" + ByteFormatUtils.formatHexByte(packet[start]));
 			w.write (" eEntriesTotal=" + ByteFormatUtils.formatHexByte(packet[start+1]));
 			w.write (" offset=" + ByteFormatUtils.formatHexByte(packet[start+2]));
 			w.write (" nEntries=" + ByteFormatUtils.formatHexByte(packet[start+3]));
 			w.write ("\n");
 			for (i = 0; i < packet[start+3]; i++) {
 				
 				w.write (" PAN64=");
 				DebugUtils.writeAddr64LSBF(w, packet, start+4+i*22);
 				
 				w.write (" Addr64=");
 				DebugUtils.writeAddr64LSBF(w, packet, start+4+i*22+8);
 				
 				w.write (" Addr16=");
 				DebugUtils.writeAddr16LSBF(w, packet, start+4+i*22+16);
 				
 				switch ((int)packet[start+4+i*22+18] & 0x03) {
 				case 0:
 					w.write (" Coordinator");
 					break;
 				case 1:
 					w.write (" Router");
 					break;
 				case 2:
 					w.write (" EndDevice");
 					break;
 				case 3:
 					w.write (" TypeUnknown");
 					break;
 	
 				}
 				
 				switch ( ((int)packet[start+4+i*22+18] >> 4) & 0x07) {
 				case 0:
 					w.write (" Parent");
 					break;
 				case 1:
 					w.write (" Child");
 					break;
 				case 2:
 					w.write (" Sibling");
 					break;
 				case 3:
 					w.write (" UnknownRelation");
 					break;
 				case 4:
 					w.write (" PrevChild");	
 					break;
 				}
 				w.write ("\n");
 			}
 			break;
 		
 		// Routing table response
 		case 0x8032:
 			w.write ("RoutingTables: ");
 			w.write (" status=" + ByteFormatUtils.formatHexByte(packet[start]));
 			w.write (" eEntriesTotal=" + ByteFormatUtils.formatHexByte(packet[start+1]));
 			w.write (" offset=" + ByteFormatUtils.formatHexByte(packet[start+2]));
 			w.write (" nEntries=" + ByteFormatUtils.formatHexByte(packet[start+3]));
 			
 			for (i = 0; i < packet[start+3]; i++) {
 				DebugUtils.writeAddr64LSBF (w, packet, start+4+i*5);
 				
 				w.write ("" + get16LSBFirst(packet, start+4+i*5));
 				switch ((int)packet[start+4+i*5+2] & 0x07) {
 				case 0:
 					w.write (" Active");
 					break;
 				case 1:
 					w.write (" DiscoveryUnderway");
 					break;
 				case 2: 
 					w.write (" DiscoveryFailed");
 					break;
 				case 3:
 					w.write (" Inactive");
 					break;
 				case 4:
 					w.write (" ValidationUnderway");
 					break;
 				}
 				w.write (" nextHopAddr=" + get16LSBFirst(packet, start+4+i*5+3));
 				w.write ("\n");
 				break;
 			}
 		}
 	}
 	
 	public static void writeData (Writer w, byte[] packet, int start, int len) throws IOException {
 		for (int i = start; i < (start+len); i++) {
 			w.write (" " + ByteFormatUtils.formatHexByte(packet[i]));
 		}
 	}
 	
 	public static final int get16LSBFirst (byte[] packet, int offset) {
 		return ((int)packet[offset] & 0xff) | ((int)packet[offset+1]<<8) & 0xffff;
 	}	
 	public static final int get16MSBFirst (byte[] packet, int offset) {
 		return (((int)packet[offset]) << 8 | (int)packet[offset+1] & 0xff) & 0xffff;
 	}
 	public static final int get32LSBFirst (byte[] packet, int offset) {
 		return (packet[offset]&0xff) 
 				| (packet[offset+1]&0xff)<<8 
 				| (packet[offset+2]&0xff)<<16 
 				| (packet[offset+3]&0xff)<<24;  
 	}	
 	public static final String getTxStatusDescription (int status) {
 		switch (status) {
 		case 0x00: return "SUCCESS";
 		case 0x01: return "MAC_ACK_FAIL";
 		case 0x02: return "CCA_FAIL";
 		case 0x15: return "INVALID_DST_EP";
 		case 0x21: return "NWK_ACK_FAIL";
 		case 0x22: return "NOT_JOINED";
 		case 0x23: return "SELF_ADDR";
 		case 0x24: return "ADDR_NOT_FOUND";
 		case 0x25: return "ROUTE_NOT_FOUND";
 		default: return "OTHER_0X" + Integer.toHexString(status);
 		
 		
 		}
 	}
 	
 }
