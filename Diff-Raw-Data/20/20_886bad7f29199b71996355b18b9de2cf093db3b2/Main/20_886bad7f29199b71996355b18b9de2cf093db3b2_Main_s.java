 package easy.caesar.code;
 
 
 public class Main {
     public static void main (String[] args){
        CaesarCode exampleOne = new CaesarCode("уфнёиц", 4);
        CaesarCode exampleTwo = new CaesarCode("привет", 4);
 
         exampleOne.decrypt();
         exampleTwo.encrypt();
     }
 }
