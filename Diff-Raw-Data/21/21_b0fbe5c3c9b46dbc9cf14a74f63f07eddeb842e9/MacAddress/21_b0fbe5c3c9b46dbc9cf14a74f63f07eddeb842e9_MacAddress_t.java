 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 // MAC address class
 
 // the class maintains two mac arrays
 // macArray[] = 6 bytes  
 // macArrayRev[] = macArray bits reversed (bytes not reversed)
 
 public class MacAddress {
 
 	// class level variables
 	private byte[] macArray = new byte[6];		// byte[0] is MSB of address
 	private byte[] macArrayRev = new byte[6];	// bits reversed (bytes not reversed)
 	
 	/*----------------------------------------------------------------------------------------*/
 	// no arg constructor creates random mac address
 	public MacAddress() {
 		
 		setMac(getRandomdMac());				// setup a random mac address
 		reverseBits();							
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// constructor accepts hex string in form "00:00:00:00:00:00"
 	public MacAddress(String hexStr) {
 		
 		setHexMac(hexStr);
 		reverseBits();
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// constructor accepts 6 byte array
 	// any six bytes is validMac
 	public MacAddress(byte[] arrayIn) {
 		
 		if(arrayIn.length != 6)
 			System.out.print("need six bytes array for mac address ");					// bad mac throw exception
 		else {
 			macArray = arrayIn.clone();
 			reverseBits();
 		}	
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// set new hex mac address 
 	public void setHexMac(String hexStr) {
 		
 		// regex for hex string validation
 		// six groups separated by ':'
 		// each group is 2 digit hex
 		// upper or lower case
 		
 		hexStr = hexStr.trim();															// trim spaces
 		
 		if(hexStr.length() != 17) {
 			{System.out.print("bad MAC\n");}											// bad mac throw exception
 			return;
 		}
 		
 		Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");		// matching pattern
 		Matcher macMatcher = macPattern.matcher(hexStr);								// pattern to be matched
 
 		if(macMatcher.find()) {															// valid mac string
 			String[] tokens = hexStr.split(":");										// split string into array
 			for(int i = 0; i < 6; i++) {			
 				macArray[i] = (byte)Integer.parseInt(tokens[i],16);						// convert hex to byte
 			}
 			reverseBits();
 		}
 		else
 			{System.out.print("bad MAC\n");}// bad mac throw exception
 	}
 
 	/*----------------------------------------------------------------------------------------*/
 	// any six bytes is validMac
 	public void setMac(byte[] arrayIn) {
 		
 		if(arrayIn.length != 6)
 			System.out.print("need six bytes array for mac address ");
 		else {
 			macArray = arrayIn.clone();
 			reverseBits();
 		}	
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// reverses the bits in each macAddress byte and stores result in macArrayRev[]
 	private void reverseBits() {
 		
 		for(int i = 0; i < 6; i++) 
 			macArrayRev[i] = (byte) (Integer.reverse((int) macArray[i]) >>> 24);
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return hex formated MAC address string
 	public String toHexString() {
 		
 		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", macArray[0],macArray[1],macArray[2],macArray[3],macArray[4],macArray[5]);
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return hex formated MAC address string with bits reversed
 	public String toHexStringRev() {
 		
 		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", macArrayRev[0],macArrayRev[1],macArrayRev[2],macArrayRev[3],macArrayRev[4],macArrayRev[5]);
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return binary formated address string
 	public String toBinString() {
 
 		String s = "";
 		
 		for(int i = 0; i < 6; i++)
 			s += String.format("%8s", Integer.toBinaryString(macArray[i] & 255)).replace(' ','0') + " ";
 		
 		return s;
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return binary formated address string with bits reversed
 	public String toBinStringRev() {
 		
 		String s = "";
 		
 		for(int i = 0; i < 6; i++)
 			s += String.format("%8s", Integer.toBinaryString(macArrayRev[i] & 255)).replace(' ','0') + " ";
 
 		return s;
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return true if byte arrays are equal
 	public boolean equals(byte[] a) {
 		
		return Arrays.equals(a, macArray);
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return true if byte arrays in are equal in a and this
 	public boolean equals(MacAddress a) {
 		
		return Arrays.equals(a.macArray, macArray);
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return an array with 6 random bytes
 	public static byte[] getRandomdMac() {
 		
 		byte randomMac[] = new byte[6];
 		
 		for(int i = 0; i < 6; i++) 
 			randomMac[i] = (byte)(Math.random() * 256) ;
 		
 		return randomMac;
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return this mac address array
 	public byte[] getMacArray() {
 	
 		return macArray.clone();
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return this mac address arrayRev
 	public byte[] getMacArrayRev() {
 	
 		return macArrayRev.clone();
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// return a copy of this class 
 	public MacAddress clone() {
 	
 		// return this;
 		// return this.clone();
 		return new MacAddress(macArray);
 	}
 	/*----------------------------------------------------------------------------------------*/
 }
