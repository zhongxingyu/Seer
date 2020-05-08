 package sshaclient;
 
 public class Constants {
 	public static final int defaultPort = 6666;
 	public static final String hostName = "127.0.0.1";
 	//public static final String hostName = "83.248.107.3";
 	public static final String name = "Sebbe";
 	
 	public static final int nbrOfPlayer = 4;
 	
	public static final int globalSleep = 1;
 	
 	public static String getItem(String str, int pos) {
 		
 		String[] splitString = str.split(" ");
 		
 		if(pos < splitString.length) {
 			return splitString[pos];
 		} else {
 			return "nada";
 		}
 	}
 }
