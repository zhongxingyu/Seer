 package client;
 
 public class StringAsciiConversion {
 	/**
 	 * Converts each individual character in an String in to ASCII Code.
 	 */
 	public static String toAscii(String str){
 		char[] charArray = str.toCharArray();
 		int length = charArray.length;
 		String asciiString = "";
 		for (int i = 0; i < length-1; i++){
 			String a = "" + (int)charArray[i] + "a";
 			asciiString += a;
 		}
		asciiString += "" + (int)charArray[length-1];
 		return asciiString;
 	}
 	
 	/**
 	 * Converts each individual ASCII Code into String.
 	 */
 	public static String asciiToString(String str){
 		String[] asciiArray = str.split("a");
 		String text = "";
 		for (String s: asciiArray){
 			int i = Integer.parseInt(s);
 			char c = (char) i;
 			text += c;
 		}
 		return text;
 	}
 }
