 package sla.util;
 
 public class ShortUrlUtil {
 
 	private final static int LENGTH_OF_URL = 6;
 
 	private final static String[] elements = { "P","I","k","j","u","E","z","K","g",
 		"2","6","a","m","s","1","B","G","o","Q","X","l","c","y","v","N",
 		"7","S","h","U","f","R","O","L","M","Y","4","e","F","8","V","T","C",
 		"3","x","i","9","A","p","b","n","D","d","5","W","q","w","r","J","H","Z","t" }; //random generated
 
 	public static String convert(long id) {
 		String shortUrl = "";
 		int l = elements.length;
 		if (id < l  && id >= 0) {
 			shortUrl = elements[(int) (id)];
 		} else if (id >= l) {
 			long mod = 0;
 			long multiplier = 0;
 			boolean determinedTheLength = false;
 			for (int i = LENGTH_OF_URL; i >= 0; i--) {
 				multiplier = (long) (id / Math.pow(l, i));
 				if (multiplier > 0 && id >= l) {
 					shortUrl += elements[(int) multiplier];
 					determinedTheLength = true;
 				} else if (determinedTheLength && multiplier == 0) {
 					shortUrl += elements[0];
 				} else if (id < l) {
 					shortUrl += elements[(int) mod];
 				}
 				mod = (long) (id % Math.pow(l, i));
 				id = mod;
 			}
 		}
 		int d = LENGTH_OF_URL - shortUrl.length();
 		return shortUrl;
 	}
 	public static long revert(String shortUrl){
 		long result=0;
 		int l=elements.length;
 		for(int i=0;i<shortUrl.length();i++){
 			char nowChar=shortUrl.charAt(i);
 			result+=(long) (getIndexOfElement(nowChar)*Math.pow(l, shortUrl.length()-1-i));
 		}
 		return result;
 	}
 	public static String complicatedConvert(long id){ 
 		int moduloN=elements.length;
 		String idToString=String.valueOf(id);
 		String reverseString="";
 		for(int i=idToString.length()-1;i>-1;i--){
 			reverseString+=idToString.charAt(i);
 		}
 		long reverseLong=Long.valueOf(reverseString);
 		long n= id/3+id/3+reverseLong/3;
 		int modulo=((int)n)%moduloN;
 	
 		return convert(id)+elements[modulo];
 	}
 	public static long complicatedRevert(String shortUrl){
 		String withOutModulo="";
 		for(int i=0;i<shortUrl.length()-1;i++){
 			withOutModulo+=String.valueOf(shortUrl.charAt(i));
 		}
 		return revert(withOutModulo);
 	}
 	public static void main(String[] args){
 		for(int i=2740000;i<2743000;i++){
 			System.out.println(complicatedConvert((long)i)+","+complicatedRevert(complicatedConvert((long)i)));
 		}
 		
 		
 	}
 	public static long getIndexOfElement(char c){
 		String element=String.valueOf(c);
 		switch(element){
 		case "P": return 0;
 		case "I": return 1;
 		case "k": return 2;
 		case "j": return 3;
 		case "u": return 4;
 		case "E": return 5;
 		case "z": return 6;
 		case "K": return 7;
 		case "g": return 8;
 		case "2": return 9;
 		case "6": return 10;
 		case "a": return 11;
 		case "m": return 12;
 		case "s": return 13;
 		case "1": return 14;
 		case "B": return 15;
 		case "G": return 16;
 		case "o": return 17;
 		case "Q": return 18;
 		case "X": return 19;
 		case "l": return 20;
 		case "c": return 21;
 		case "y": return 22;
 		case "v": return 23;
 		case "N": return 24;
 		case "7": return 25;
 		case "S": return 26;
 		case "h": return 27;
 		case "U": return 28;
 		case "f": return 29;
 		case "R": return 30;
 		case "O": return 31;
 		case "L": return 32;
 		case "M": return 33;
 		case "Y": return 34;
 		case "4": return 35;
 		case "e": return 36;
 		case "F": return 37;
 		case "8": return 38;
 		case "V": return 39;
 		case "T": return 40;
 		case "C": return 41;
 		case "3": return 42;
 		case "x": return 43;
 		case "i": return 44;
 		case "9": return 45;
 		case "A": return 46;
 		case "p": return 47;
 		case "b": return 48;
 		case "n": return 49;
 		case "D": return 50;
 		case "d": return 51;
 		case "5": return 52;
 		case "W": return 53;
 		case "q": return 54;
 		case "w": return 55;
 		case "r": return 56;
 		case "J": return 57;
 		case "H": return 58;
 		case "Z": return 59;
 		case "t": return 60;
 		default : return -1;
 		}
 	}
 }
