 import checkers.latticetainting.quals.Level;
 
 
 public class Simple {
     
 
     public static void main(String args[]) {
 
         @Level("Public")
        int c = 100;
 
         // @Level("Private")
         // int d = 100;
 
         // // Not OK
         // @Level("Public")
         // int e = Simple.testVal();
 
         // // Assignment OK
         // d = c;
         
         // // Assignment NOT OK
         // c = d;
     }
 
     public static int testVal() {
	//        @Level("Private")
         int notSecret = 1000;
         return notSecret;
     }
 
 
 
     // public static int declassify(@Level("Private") int privateVal){
     //     @SuppressWarnings("safe")
     //     @Level("Public")
     //     int publicVal = privateVal;
         
     //     return publicVal;
     // }  
 
 
 
 }
