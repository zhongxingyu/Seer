 package mnet;
 import mstructs.*;
 public class Constants {
 	static Dictionary hardwareTypes = new Dictionary("./constants/hardwareTypes"),
 			     		DHCPOptions = new Dictionary("./constants/DHCPOptions");
 	
	public final static byte   BOOTREQUEST = 1, BOOTREPLY = 2;
	public final static byte[] magicCookie = new byte[]{99, (byte) 130, 83, 99};
 }
