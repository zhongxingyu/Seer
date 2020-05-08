 package easy.caesar.code;
 
 
 public class Main {
     public static void main (String[] args){
        CaesarCode exampleOne = new CaesarCode("врд", 4);
        CaesarCode exampleTwo = new CaesarCode("яма", 4);
 
         exampleOne.decrypt();
         exampleTwo.encrypt();
     }
 }
