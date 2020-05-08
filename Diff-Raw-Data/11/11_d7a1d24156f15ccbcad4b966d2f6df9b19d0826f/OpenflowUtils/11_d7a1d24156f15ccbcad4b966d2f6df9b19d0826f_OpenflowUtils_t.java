 package ru.spbstu.telematics.flowgen.utils;
 
 
 import java.util.Set;
 
 public class OpenflowUtils {
 
 	public static final int MIN_PORT = 1;
 	public static final int MAX_PORT = 255;
 
 	public static final int DPID_BYTES = 8;
 	public static final int MAC_BYTES = 6;
 
 	public static final String DPID_DELIMITER = ":";
 	public static final String MAC_DELIMITER = ":";
 	public static final String PORTS_DELIMITER = ",";
 
 
 	public static boolean validateDpid(String dpid) {
         return validateHexByteString(dpid, DPID_BYTES, DPID_DELIMITER);
 	}
 
 	public static boolean validateMac(String mac) {
 		return validateHexByteString(mac, MAC_BYTES, MAC_DELIMITER);
 	}
 
 	public static boolean validateHexByteString(String string, int bytesNumber, String delimiter) {
 		final int HEX_BYTE_LENGTH = 2;
 
 		if (StringUtils.isNullOrEmpty(string)) {
 			return false;
 		}
 
 		if (string.startsWith(delimiter) || string.endsWith(delimiter)) {
 			return false;
 		}
 
 		String[] bytes = string.split(delimiter);
 		if (bytes.length != bytesNumber) {
 			return false;
 		}
 
 		for (int i = 0; i < bytesNumber; i++) {
 			if(bytes[i].length() != HEX_BYTE_LENGTH) {
 				return false;
 			}
 			for (int j = 0; j < HEX_BYTE_LENGTH; j++) {
 				if (!StringUtils.isHexDigit(bytes[i].charAt(j))) {
 					return false;
 				}
 			}
 
 		}
 
 		return true;
 	}
 
 	public static boolean validatePortsSet(Set<Integer> ports) {
 		if (ports.isEmpty()) {
 			return false;
 		}
 
 		for(Integer port : ports) {
			if (!validatePortNumber(port)) {
 				return false;
 			}
 		}
 
 		return true;
 
 	}
 
 	public static boolean validatePortNumber(int port) {
 		return port >= MIN_PORT && port <= MAX_PORT;
 
 	}
 }
