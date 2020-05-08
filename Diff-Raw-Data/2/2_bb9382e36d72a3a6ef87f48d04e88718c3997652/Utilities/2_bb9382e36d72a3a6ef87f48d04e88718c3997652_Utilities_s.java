 package bt.Utils;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.ServerSocket;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Map;
 
 import bt.Model.Bittorrent;
 import bt.Model.TrackerRefresher;
 
 /**
  * A pure utilities class filled of static methods for specific tasks.
  * 
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci
  *
  */
 public class Utilities {
 	
 	public static final int MAX_PIECE_LENGTH = 16384;
 	public static final int MAX_TIMEOUT = 120000;
 	
 	/**
 	 * Returns a byte stream from the given file.
 	 * @param file
 	 * @return byte[] File Bytes
 	 */
 	public static byte[] getBytesFromFile(File file) {
 		ArrayList<Byte> bytes = new ArrayList<Byte>();
 		DataInputStream dis;
 		try {
 			// open file
 			dis = new DataInputStream(new FileInputStream(file));
 			int b;
 			// read bytes
 			while(true) {
 				b = dis.read();
 				if(b == -1) break;
 				else bytes.add((byte)b);
 			}
 			// close the file stream
 			dis.close();
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 		// translate into byte[]
 		int listLength = bytes.size();
 		byte[] bytesArray = new byte[listLength];
 		for(int i = 0; i < listLength; ++i) {
 			bytesArray[i] = bytes.get(i);
 		}
 
 		return bytesArray;
 	}
 	
 	/**
 	 * Returns a String representation of a ByteBuffer.
 	 * @param bb ByteBuffer to be converted to a string
 	 * @return String message
 	 */
 	public static String getStringFromByteBuffer(ByteBuffer bb) {
 		StringBuilder message = new StringBuilder();
 		int bytes;
 		while(true) {
 			try {
 				bytes = bb.get();
 				// format the product of two bytes and a bitwise AND with 0xFF
 				message.append("\\x"+String.format("%02x", bytes&0xff));
 			} catch (Exception e) {
 				break;
 			}
 		}
 		return message.toString();
 	}
 	
 	/**
 	 * It will encode the info_hash to a URL parameter recursively.
 	 * @param infoHash The sha hash of the torrent file.
 	 * @return String encoded info_hash URL
 	 */
 	public static String encodeInfoHashToURL(String infoHash) {
 		String encodedURL = "";
 		if(infoHash.length() == 0) return infoHash;
 		char ch = infoHash.charAt(0);
 		if(ch == 'x') 
 			encodedURL += "%"+encodeInfoHashToURL(infoHash.substring(1));
 		else if(ch == '\\')
 			encodedURL += encodeInfoHashToURL(infoHash.substring(1));
 		else
 			encodedURL += ch+encodeInfoHashToURL(infoHash.substring(1));
 		return encodedURL;
 	}
 	
 	/**
 	 * Generates a random 20 character string as a peerID.
 	 * @return String peer ID
 	 */
 	public static String generateID() {
 		StringBuilder generatedID = new StringBuilder();
 		char nextChar;
 		for(int i = 0 ; i < 20; ++i) {
 			// create a random character between 65 - 90 ASCII
 			nextChar = (char)(65 + (int)(Math.random()*25));
 			generatedID.append(nextChar);
 		}
 		System.out.println("Random ID is (in bytes): "+generatedID.toString());
 		
 		return generatedID.toString();
 	}
 	
 	/**
 	 * Returns a peer list from the ByteBuffer return.
 	 * @param map A collection of peers.
 	 * @return decoded peer list
 	 */
 	 public static String[] decodeCompressedPeers(Map map) {
 	        ByteBuffer peers = (ByteBuffer)map.get(ByteBuffer.wrap("peers".getBytes()));
 	        ArrayList<String> peerURLs = new ArrayList<String>();
 	        try {
 	            while (true) {
 	                String ip = String.format("%d.%d.%d.%d",
 	                    peers.get() & 0xff,
 	                    peers.get() & 0xff,
 	                    peers.get() & 0xff,
 	                    peers.get() & 0xff);
 	                int port = peers.get() * 256 + peers.get();
 	                peerURLs.add(ip + ":" + port);
 	            }
 	        } catch (Exception e) {
 	        }
 	        return peerURLs.toArray(new String[peerURLs.size()]);
 	  }
 	 
 	 /**
 	  * Returns the updated interval requested by tracker
 	  * @param map: the interval key
 	  * @return the integer value of interval
 	  */
 	 public static int decodeInterval(Map map) {
 		 int interval = -1;
 		 try {
 			 interval = (int)map.get(ByteBuffer.wrap("interval".getBytes()));
 		 } catch (Exception e) {
 			 System.err.println("Tracker interval = -1, because not received.");
 		 }
 		 return interval;		 
 	 }
 	 
 	 /**
 	  * Returns the updated min_interval requested by tracker
 	  * @param map: the min_interval key
 	  * @return the integer value of min_interval
 	  */
 	 public static int decodeMinInterval(Map map) {
 		 int min_interval = -1;
 		 try {
 			 min_interval = (int)map.get(ByteBuffer.wrap("min_interval".getBytes()));
 		 } catch (Exception e) {
			 System.err.println("Tracker min_interval = -1, because not received.");
 		 }
 		 return min_interval;		 
 	 }
 	 
 	 /**
 	  * Terminates the client.
 	  */
 	 public static void callClose() {
 		 try {
 			 
 			 Bittorrent.getInstance().stopServer();
 			 Bittorrent.getInstance().disposePeers();
 			 System.out.println("\n -- Client Terminated -- ");
 			 System.out.println("\t> Peers disposed");
 			 System.out.println("\t> Tracker Notified");
 			 System.exit(0);
 		 } catch (Exception e) { /* this should never happen */	 } 
 	 }
 	 
 	 /**
 	  * Splits the IPv4 and port from address.
 	  * @param address The A string representation of the ip address
 	  * @return String IPv4 address
 	  */
 	 public static String getIPFromString(String address) {
 		 String ipAddress = null;
 		 int separator = address.indexOf(':');
 		 if(separator != -1) {
 			 ipAddress = address.substring(0, separator);
 		 }
 		 return ipAddress;
 	 }
 	 
 	 /**
 	  * Splits the port section of a String IPv4:port String.
 	  * @param address
 	  * @return integer value of port string
 	  */
 	 public static int getPortFromString(String address) {
 		 int port = -1;
 		 int separator = address.indexOf(':');
 		 if(separator != -1) {
 			 port = Integer.parseInt(address.substring(separator+1));
 		 }
 		 return port;
 	 }
 	 
 	 /**
 	  * Concatenates two byte arrays
 	  * @param a byte array to be prepended
 	  * @param b byte array to be appended
 	  * @return single concatenated byte array
 	  */
 	 public byte[] byteConcat(byte[] a, byte[] b) {
 		   byte[] C= new byte[a.length + b.length];
 		   System.arraycopy(a, 0, C, 0, a.length);
 		   System.arraycopy(b, 0, C, a.length, b.length);
 		   return C;
 	 }
 	  
 	 /**
 	  * Returns a byte[] from the info_hash ByteBuffer for simplicity.
 	  * @param bb ByteBuffer containing the hash code
 	  * @return byte[] info_hash 20 bytes
 	  */
 	 public static byte[] getHashBytes(ByteBuffer bb) {
 		 bb.position(0);
 		 byte[] array = new byte[20];
 		 bb.get(array);
 		 return array;
 	 }
 	 
 	 /**
 	  * It tests two arrays of bytes for equality.
 	  * @param a byte array to be checked for equality
 	  * @param b byte array to be checked for equality
 	  * @return boolean True if match, false otherwise.
 	  */
 	 public static boolean matchBytes(byte[] a, byte[] b) {
 		 if(a.length != b.length) return false;
 		 else {
 			 for(int i = 0; i < a.length; ++i) {
 				 if(a[i] != b[i]) return false;
 			 }
 			 return true;
 		 }
 	 }
 	 
 	 /**
 	  * Extracts the info hash from the handshake repsonse.
 	  * @param response the response sent by the peer to our handshake
 	  * @return info_hash
 	  */
 	 public static byte[] getInfoHashFromHandShakeResponse(byte[] response) {
 		 byte[] info_hash = new byte[20];
 		 int offset = 28;
 		 for(int i = 0; i < 20; ++i) {
 			 info_hash[i] = response[offset];
 			 ++offset;
 		 }
 		 return info_hash;
 	 }
 	 
 	 /**
 	  * Returns the index of the next piece to be requested.
 	  * @param completed
 	  * @return index of a piece which is needed.
 	  */
 	 public static int getNeededPiece(boolean[] completed) {
 
 		 for(int i = 0; i < completed.length; ++i) {
 			 if(!completed[i]) return i;
 		 }
 		 return -1; // no more pieces are needed.
 	 }
 	 
 	 /**
 	  * Converts a bitfield into a string.
 	  * @param boolean[] bits
 	  * @return String bitfield string representation.
 	  */
 	 public static String bitFieldToString(boolean[] bits) {
 		 String bitfield = "";
 		 for(int i = 0; i < bits.length; ++i) {
 			 if(bits[i]) {
 				 bitfield += "1";
 			 } else {
 				 bitfield += "0";
 			 }
 		 }
 		 return bitfield;
 		 
 	 }
 }
