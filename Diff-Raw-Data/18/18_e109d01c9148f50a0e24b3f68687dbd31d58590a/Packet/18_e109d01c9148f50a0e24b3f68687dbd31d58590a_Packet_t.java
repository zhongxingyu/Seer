 package hu.edudroid.sniffer;
 
 import hu.edudroid.tcp_utils.TCPIPUtils;
 
 import java.net.DatagramPacket;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 
 /**
  * @author lajthabalazs
  */
 public class Packet {
 	private static final byte TCP = 6;
 	static final byte UDP = 17;
 	private static final int MIN_IP_HEADER_SIZE = 20;
 	private static final int UDP_HEADER_SIZE = 8;
 	private final byte ZERO = 0;
 	public byte[] sourceIp = new byte[4];
 	public int sourcePort; 
 	public byte[] destIp = new byte[4];
 	long destAddress;
 	public byte protocol;
 	public int destPort;
 	public int packetLength;
 	public int ipHeaderLength;
 	public int transportHeaderLength;
 	public boolean hasIpOptions = false;
 	public short version;
 	public short ihl;
 	public byte[] data; // A reference to data
 	public int dataOffset; // Start of the packet payload in the data array
 	public int dataLength; // Length of the packet payload
 	
 	public Packet(DatagramPacket packet, InetSocketAddress localAddress) {
 		dataLength = packet.getLength();
 		data = new byte[dataLength];
 		version = 4;
 		ihl = 5;
 		packetLength = dataLength + MIN_IP_HEADER_SIZE + UDP_HEADER_SIZE;
 		protocol = UDP;
 		sourceIp = packet.getAddress().getAddress();
 		sourcePort = packet.getPort();
 		destIp = localAddress.getAddress().getAddress();
 		destPort = localAddress.getPort();
 	}
 
 	public Packet(ByteBuffer buffer, int packetStart, int lastData) {
 		// If there isn't a whole ip header, return
 		if (packetStart + MIN_IP_HEADER_SIZE > lastData) {
 			throw new IllegalArgumentException("Not enough bytes in stream");
 		}
 		
 		System.arraycopy(buffer.array(), packetStart + 12, sourceIp, 0, 4);
 		System.arraycopy(buffer.array(), packetStart + 16, destIp, 0, 4);
 		version = (short)((buffer.array()[0] & 0xF0) >> 4);
 		ihl = (short)((buffer.array()[0] & 0x0F));
 		packetLength = TCPIPUtils.toIntUnsigned(buffer.array()[packetStart + 2], buffer.array()[packetStart + 3]);
 		if (packetStart + packetLength > lastData) {
 			throw new IllegalArgumentException("Not enough bytes in stream");
 		}
 		ipHeaderLength = ihl * 4;
 		sourcePort = TCPIPUtils.toIntUnsigned(buffer.array()[packetStart + ipHeaderLength + 1], buffer.array()[packetStart + ipHeaderLength]);
 		destPort =  TCPIPUtils.toIntUnsigned(buffer.array()[packetStart + ipHeaderLength + 2], buffer.array()[packetStart + ipHeaderLength + 3]);
 		destAddress = TCPIPUtils.getLongFromAddress(destIp, destPort);
 		protocol = buffer.array()[packetStart + 9];
 		if (protocol == UDP) {
 			transportHeaderLength = UDP_HEADER_SIZE;
 		} else if (protocol == TCP) {
 			transportHeaderLength = TCPIPUtils.toIntUnsigned(ZERO, buffer.array()[packetStart + ipHeaderLength + 12]);
 		}
 		dataLength = packetLength - (ipHeaderLength + transportHeaderLength);
 		dataOffset = packetStart + ipHeaderLength + transportHeaderLength;
 		data = new byte[dataLength];
 		System.arraycopy(buffer.array(), dataOffset, data, 0, dataLength);
 	}
 	
 	@Override
 	public String toString() {
 		String ret = version + "(" + (protocol == UDP?"UDP":(protocol == TCP?"TCP":protocol)) + ") > " + TCPIPUtils.ipAddressToString(sourceIp, 0) + ":" + sourcePort;
 		ret = ret + " -> " + TCPIPUtils.ipAddressToString(destIp, 0) + ":" + destPort;
 		ret = ret + " length : " + packetLength;
 		return ret;
 	}
 	
 	public static int IPChecksum(byte[] header) {
 		int sum = 0;
 		int length = header.length;
 		int i = 0;
 		while (length > 1) {
 			// Calculating the sum of byte pairs
 			sum += (((header[i] << 8) & 0xFF00) | ((header[i + 1]) & 0xFF));
 			i += 2;
 			length -= 2;
 		}
 
 		// if length is odd, padd with 0's from right
 		if (length > 0) {
 			sum += (header[i] << 8 & 0xFF00);
 		}
 		int carry = (0xFFFF0000 & sum) >> 16; // Carry
 		if (carry > 0) {
 			sum = sum & 0xFFFF;
 			sum += carry;
 		}
 		return (~sum) & 0xFFFF;
 	}
 	
	public int UDPChecksum(byte[] ret) {
 		int sum = 0;
 		int length = ret.length;
 		int i = 0;
 		int carry = 0;
 		
 		//Pseudo header
		sum += (((sourceIp[0] << 8) & 0xFF00) | ((sourceIp[1]) & 0xFF));
		sum += (((sourceIp[2] << 8) & 0xFF00) | ((sourceIp[3]) & 0xFF));
		sum += (((destIp[0] << 8) & 0xFF00) | ((destIp[1]) & 0xFF));
		sum += (((destIp[2] << 8) & 0xFF00) | ((destIp[3]) & 0xFF));
		sum += (0x00FF & UDP);
		sum += (0xFFFF & ret.length);
 		carry = (0xFFFF0000 & sum) >> 16; //Carry
 	    if(carry > 0){
 	    	sum = sum & 0xFFFF;
 	    	sum += carry;
 	    }
 		
 		while(length > 1){
 	    	//Calculating the sum of byte pairs
 	    	sum += (((ret[i] << 8) & 0xFF00) | ((ret[i+1]) & 0xFF));
 	    	i += 2;
 	    	length -= 2;
 	    }
		//if length is odd, use padding with 0's from right
 		if(length > 0){
 			sum += (ret[i] << 8 & 0xFF00);
 		}
 		carry = (0xFFFF0000 & sum) >> 16; //Carry
 	    if(carry > 0){
 	    	sum = sum & 0xFFFF;
 	    	sum += carry;
 	    }
 	    
 	    return (~sum) & 0xFFFF;
 	}
 	
 	public byte[] toByteArray() {
 		byte[] ret = new byte[packetLength];
 		ret[0] = 69;// TCPIPUtils.toByte(version, ihl);
 		ret[1] = 0; // DSCP, ECN
 		System.arraycopy(TCPIPUtils.toTwoBytes(packetLength), 0, ret, 2, 2); // Total length
 		System.arraycopy(TCPIPUtils.toTwoBytes(0), 0, ret, 4, 2); // Identification
 		ret[6] = 0; // Flags, Fragment offset part 1
 		ret[7] = 0; // Flags, Fragment offset part 2
 		ret[8] = 64; // TTL
 		ret[9] = protocol;
 		System.arraycopy(TCPIPUtils.toTwoBytes(0), 0, ret, 4, 2); // 0's for Header checksum calculation
 		System.arraycopy(sourceIp, 0, ret, 12, 2);
 		System.arraycopy(destIp, 0, ret, 12, 4);
 		byte[] header = new byte[ipHeaderLength];
 		System.arraycopy(ret,0,header,0,ipHeaderLength);
 		System.arraycopy(TCPIPUtils.toTwoBytes(IPChecksum(header)), 0, ret, 4, 2); // Header checksum
 		// No options
 		if (protocol == UDP) {
 			System.arraycopy(TCPIPUtils.toTwoBytes(sourcePort), 0, ret, 20, 2); // Source port
 			System.arraycopy(TCPIPUtils.toTwoBytes(destPort), 0, ret, 22, 2); // Destination port
 			System.arraycopy(TCPIPUtils.toTwoBytes(dataLength + 8), 0, ret, 24, 2); // Data + header length
 			System.arraycopy(TCPIPUtils.toTwoBytes(0), 0, ret, 26, 2); // 0's for UDP checksum calculation
 			System.arraycopy(data, 0, ret, 28, dataLength);
 			System.arraycopy(TCPIPUtils.toTwoBytes(UDPChecksum(ret)), 0, ret, 26, 2); // UDP checksum
 		}
 		return ret;
 	}
 }
