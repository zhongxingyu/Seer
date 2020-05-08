 package javabat.string2;
 
 public class MixString {
 
 	public String mixString(String a, String b) {
 
 		String shorter, longer = "";
 
 		if (a.length() < b.length()) {
 			shorter = a;
 			longer = b;
 		} else {
 			shorter = b;
 			longer = a;
 		}
 
 		StringBuilder sb = new StringBuilder();
 
 		for (int i = 0; i < shorter.length(); i++) {
 			sb.append(a.charAt(i));
 			sb.append(b.charAt(i));
 		}
 
 		sb.append(longer.substring(shorter.length()));
 		return sb.toString();
 	}
 }
