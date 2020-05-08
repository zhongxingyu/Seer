 package com.timboe.rpsrts.enumerators;
 
 public class Pass {
 	public static String GetPass() {
		return s("1PxE5B7SE4EsEwG");
	}
	
	private static String s(String s){
		String ss = "";
        for (int S = 0; S < s.length(); ++S) {
            char c = s.charAt(S);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            else if  (c >= '5' && c <= '9') c -= 5;
            else if  (c >= '0' && c <= '4') c += 5;
            ss += c;
        }
        return ss;
 	}
 }
