 package mnet;
 import mstructs.*;
 public class Constants {
 	static Dictionary hardwareTypes = new Dictionary("./constants/hardwareTypes"),
 			     		DHCPOptions = new Dictionary("./constants/DHCPOptions");
 	
	public final static byte[]  MAGICCOOKIE = new byte[]{99, (byte) 130, 83, 99};
	public final static byte    BOOTREQUEST = 1, BOOTREPLY = 2;
	public final static byte   DHCPDISCOVER = 1, DHCPOFFER = 2, DHCPREQUEST = 3, DHCPDECLINE = 4,
				                    DHCPACK = 5,  DHCPNACK = 6, DHCPRELEASE = 7,  DHCPINFORM = 8;
 }
