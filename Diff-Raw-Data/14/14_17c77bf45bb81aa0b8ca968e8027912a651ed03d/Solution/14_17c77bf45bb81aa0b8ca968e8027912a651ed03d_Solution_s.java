 //wild card matching
 public class Solution {
     public boolean isMatch(String s, String p) {
 		// Start typing your Java solution below
 		// DO NOT write main() function
 		if(s == null && p == null)
 			return true;
 		if(s == null)
 			return p.isEmpty();
 		if(p == null)
 			return s.isEmpty();
 		if(s.isEmpty() && p.isEmpty())
 			return true;
		if(s.isEmpty() || p.isEmpty())
 			return false;
 
 		if(p.charAt(0) == '*'){
 			for(int i =0; i<=s.length();++i){
 				if(isMatch(s.substring(i), p.substring(1)))
 					return true;
 			}
 			return false;
 		}
		if(p.charAt(0) == '?')
			return isMatch(s.substring(1), p.substring(1));
		return (p.charAt(0) == s.charAt(0)) && isMatch(s.substring(1), p.substring(1));
 	}
 }
