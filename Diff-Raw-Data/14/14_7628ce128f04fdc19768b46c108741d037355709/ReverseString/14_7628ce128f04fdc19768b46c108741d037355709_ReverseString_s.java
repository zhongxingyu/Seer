 class ReverseString {
 
  String reverseString( String s ) {
     String result = "";
     for (int i=0;  i < s.length();  ++i) {
       result += s.charAt(s.length()-i-1);
       }
     return result;
     }
 
  String reverseString_bug_A( String s ) {
     String result = "";
     for (int i=0;  i > s.length();  ++i) {
       result += s.charAt(s.length()-i);
       }
     return result;
     }
 
  String reverseString_bug_B( String s ) {
     String result = "";
     for (int i=0;  i < s.length();  ++i) {
       result += s.charAt(i);
       }
     return result;
     }
 
  String reverseString_bug_C( String s ) {
     String result = "";
     result += s.charAt( s.length() -1 );
     for (int i=1;  i < s.length();  ++i) {
       result += s.charAt(s.length()-i);
       }
     return result;
     }
