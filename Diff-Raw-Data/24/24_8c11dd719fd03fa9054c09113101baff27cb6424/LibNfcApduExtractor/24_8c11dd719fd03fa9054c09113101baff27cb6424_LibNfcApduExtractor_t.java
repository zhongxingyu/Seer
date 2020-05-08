 package ch.compass.gonzoproxy.relay.io.extractor;
 
 import java.util.ArrayList;
 
 import ch.compass.gonzoproxy.model.ForwardingType;
 import ch.compass.gonzoproxy.model.Packet;
 import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
 import ch.compass.gonzoproxy.utils.ByteArraysUtils;
 
 public class LibNfcApduExtractor implements ApduExtractor {
 
 	private static final char EOC = '\n';
 
	//TODO
	private static final char DELIMITER = '#';
	private static final String DELIMITERR = "#";
 
 
 	public byte[] extractPacketsToQueue(byte[] buffer, RelayDataHandler relayDataHandler,
 			int readBytes, ForwardingType forwardingType) {
		ArrayList<Integer> indices = getDelimiterIndices(buffer, DELIMITER);
//		ArrayList<Integer> indices = ByteArraysUtils.getDelimiterIndices(buffer,
//								DELIMITERR.getBytes());
 		int startIndex = 0;
 		int endIndex = 0;
 
 		for (int i = 0; i < indices.size() - 1; i++) {
 			startIndex = indices.get(i);
 			endIndex = indices.get(i + 1);
 			int size = endIndex - startIndex;
 			byte[] plainpacket = ByteArraysUtils.trim(buffer, startIndex, size);
 			Packet packet = splitPacket(plainpacket);
 			packet.setType(forwardingType);
 			relayDataHandler.offer(packet);
 		}
 
 		byte[] singlePacket = ByteArraysUtils.trim(buffer, endIndex, readBytes - endIndex);
 
 		if (packetIsComplete(singlePacket)) {
 			Packet apdu = splitPacket(singlePacket);
 			apdu.setType(forwardingType);
 			relayDataHandler.offer(apdu);
 			return new byte[0];
 		} else {
 			return singlePacket;
 		}
 	}
 
 	private boolean packetIsComplete(byte[] singlePacket) {
 		return singlePacket[singlePacket.length - 1] == EOC;
 	}
 
 	private Packet splitPacket(byte[] rawApdu) {
 		int size = getPacketSize(rawApdu);
 		byte[] preamble = getApduPreamble(rawApdu, size);
 		byte[] plainApdu = getPlainPacket(rawApdu, size);
 		byte[] trailer = getPacketTrailer(rawApdu, size);
 		Packet newApdu = new Packet();
 		newApdu.setPreamble(preamble);
 		newApdu.setOriginalPacketData(plainApdu);
 		newApdu.setTrailer(trailer);
 		newApdu.setSize(size);
 		return newApdu;
 	}
 
 	private byte[] getPacketTrailer(byte[] plainPacket, int size) {
 		for (int i = 0; i < plainPacket.length; i++) {
 			if (plainPacket[i] == ':') {
 				int endOfPlainPacket = i + 3 * size + 1;
 				return ByteArraysUtils.trim(plainPacket, endOfPlainPacket, plainPacket.length
 						- endOfPlainPacket);
 			}
 		}
 		return null;
 	}
 
 	private byte[] getPlainPacket(byte[] plainPacket, int size) {
 		for (int i = 0; i < plainPacket.length; i++) {
 			if (plainPacket[i] == ':') {
 				return ByteArraysUtils.trim(plainPacket, i + 2, size * 3 - 1);
 			}
 		}
 		return plainPacket;
 	}
 
 	private byte[] getApduPreamble(byte[] plainPacket, int size) {
 		for (int i = 0; i < plainPacket.length; i++) {
 			if (plainPacket[i] == ':') {
 				return ByteArraysUtils.trim(plainPacket, 0, i + 2);
 			}
 		}
 		return plainPacket;
 	}
 
 	private int getPacketSize(byte[] plainPacket) {
 		int value = 0;
 		byte[] size = new byte[4];
 		for (int i = 0; i < plainPacket.length; i++) {
 			if (plainPacket[i] == ' ') {
 				size[0] = plainPacket[i + 1];
 				size[1] = plainPacket[i + 2];
 				size[2] = plainPacket[i + 3];
 				size[3] = plainPacket[i + 4];
 				value = Integer.parseInt(new String(size), 16);
 				return value;
 			}
 		}
 		return value;
 	}
 
 	@Override
 	public String getName() {
 		return "libnfc";
 	}
	
	//TODO
	public static ArrayList<Integer> getDelimiterIndices(byte[] buffer,
			char delimiter) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] == delimiter)
				indices.add(i);

		}
		return indices;
	}
 }
