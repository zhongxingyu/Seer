 /*
  License:
 
  blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
  (http://www.eclipse.org/legal/epl-v10.html)
 
 
  Distribution:
 
  Repository - https://github.com/lempel/blueprint-sdk.git
  Blog - http://lempel.egloos.com
  */
 
 package blueprint.sdk.util;
 
 import java.net.Inet4Address;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 
 /**
  * IP address related utility
  * 
  * @author Simon Lee
  * @since 2013. 8. 5.
  */
 public class IpUtil {
 	/**
 	 * See if given address is private or not
 	 * 
 	 * @param addr
 	 * @return true : private network
 	 */
 	public static boolean isPrivateIp(InetAddress addr) {
 		boolean result = false;
 
 		if (addr instanceof Inet4Address) {
 			result = isPrivateIp((Inet4Address) addr);
 		} else if (addr instanceof Inet6Address) {
 			result = isPrivateIp((Inet6Address) addr);
 		}
 
 		return result;
 	}
 
 	/**
 	 * See if given address is private or not
 	 * 
 	 * @param addr
 	 *            IPv4 address
 	 * @return true : private network
 	 */
 	public static boolean isPrivateIp(Inet4Address addr) {
 		boolean result = false;
 
 		byte[] address = addr.getAddress();
 
 		if (address[0] == 10) {
 			result = true;
		} else if (address[0] == 169 && address[1] == 254) {
 			result = true;
		} else if (address[0] == 172 && (address[1] >= 16 && address[1] <= 31)) {
 			result = true;
		} else if (address[0] == 192 && address[1] == 168) {
 			result = true;
 		}
 
 		return result;
 	}
 
 	/**
 	 * See if given address is private or not
 	 * 
 	 * @param addr
 	 *            IPv6 address
 	 * @return true : private network
 	 */
 	public static boolean isPrivateIp(Inet6Address addr) {
 		boolean result = false;
 
 		byte[] address = addr.getAddress();
		
		if (address[0] == 0xfc && address[0] == 0x00) {
 			result = true;
		} else if (address[0] == 0xfe && address[0] == 0x80) {
 			result = true;
 		}
 
 		return result;
 	}
 
 	/**
 	 * See if given address is loopback or not
 	 * 
 	 * @param addr
 	 * @return true : private network
 	 */
 	public static boolean isLoopbackIp(InetAddress addr) {
 		boolean result = false;
 
 		if (addr instanceof Inet4Address) {
 			result = isLoopbackIp((Inet4Address) addr);
 		} else if (addr instanceof Inet6Address) {
 			result = isLoopbackIp((Inet6Address) addr);
 		}
 
 		return result;
 	}
 
 	/**
 	 * See if given address is loopback or not
 	 * 
 	 * @param addr
 	 *            IPv4 address
 	 * @return true : private network
 	 */
 	public static boolean isLoopbackIp(Inet4Address addr) {
 		boolean result = false;
 
 		byte[] address = addr.getAddress();
 
		if (address[0] == 127) {
 			result = true;
 		}
 
 		return result;
 	}
 
 	/**
 	 * See if given address is loopback or not
 	 * 
 	 * @param addr
 	 *            IPv6 address
 	 * @return true : private network
 	 */
 	public static boolean isLoopbackIp(Inet6Address addr) {
 		boolean result = false;
 
 		byte[] address = addr.getAddress();
 
 		boolean allZero = true;
 		for (byte abyte : address) {
 			if (abyte != 0) {
 				allZero = false;
 				break;
 			}
 		}
 
 		if (allZero && address[address.length - 1] == 1) {
 			result = true;
 		}
 
 		return result;
 	}
 
 	/**
 	 * See if given address is public or not
 	 * 
 	 * @param addr
 	 * @return true : public address
 	 */
 	public static boolean isPublicIp(InetAddress addr) {
 		return !(isPrivateIp(addr) || isLoopbackIp(addr));
 	}
 }
