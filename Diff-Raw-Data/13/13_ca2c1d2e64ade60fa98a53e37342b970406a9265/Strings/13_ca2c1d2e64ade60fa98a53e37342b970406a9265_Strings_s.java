 public class Strings {
 
 	public static void main(String[] args) {
 		String str = new String("A string");
 		String s = "A string";
 
 		// if (str.equals(s)) {
 		// 	System.out.println("A");
 		// } else {
 		// 	System.out.println("B");
 		// }
 
 		// for (int i=0; i<str.length(); i++) {
 		// 	System.out.println(str.charAt(i));
 		// }
 
 		// for (int i=0; i<str.length()-1; i++) {
 		// 	System.out.println(str.substring(i, i+2));
 		// }
 		// if (contains("baseball","ball")) {
 		// 	System.out.println("Yes");
 		// } else {
 		// 	System.out.println("No");
 		// }
 
 		// System.out.println(count("people","e"));
 
 		// if (hasAnA("B")) {
 			// System.out.println("Yes.");
 		// } else{
 			// System.out.println("No.");
 		// }
 		 System.out.println(isAbcdarian("abcdef"));
 
 	}
 
 	public static boolean isAbcdarian(String str){
 		for (int i=0; i<str.length(); i++) {
 			if (str.charAt(i) < str.charAt(i+1)) {
 				return true;
 			}else{
 				return false;
 			}
 			
 		}
 		return true;
 	}
 
 	public static boolean isDupledrome(String s){
 		for (int i = 0; i<s.length(); i+2) {
 			 if((s.charAt(i) != s.charAt(i + 1)){
 			 	return false;
 			 }
 			return true;
 		}
 	}
 
 	public static boolean captainCrunch(String s){
 		for (int i=0; i<s.lenght; i++) {
			charAt(s) = s+13
 		}
 	}
 
 	public static int count(String s, String t){
 		int amount = 0;
 
 		for (int i=0; i < s.length(); i++) {
 			if (t.equals(s.substring(i, i +1))) {
 				amount++;
 			}
 		}
 		return amount;
 	}
 
 	public static boolean hasAnA(String s){
 		for (int i = 0; i<s.length(); i++) {
 			if (s.charAt(i)=='a' || s.charAt(i) =='A') {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// public static boolean is Palindrome(String s){
 	// 	String reverese = new String();
 	// 	for (int i = ;;) {
 	// 		if () {
 				
 	// 		}
 	// 	}
 	// }
 
 
 	public static boolean contains(String s, String t){
 		for (int i=0; i<s.length()-t.length()+1; i++) {
 			if (s.substring(i, i+t.length()).equals(t)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 }
