 
 public class BinaryUtil {
 	public static final String pad(String s){
 		if(s.length() > 32){
 			throw new RuntimeException("Attempt to pad out long string that is too many bits: " + s);
 		} else return String.format("%32s", s).replace(" ", "0");
 	}
 	
 	public static final String pad8(String s){
 		if(s.length() > 8){
 			throw new RuntimeException("Attempt to pad out String that is greater than 8 bits: " + s);
		} else return String.format("%8s", s).replace(" ", "0");
 		
 	}
 }
