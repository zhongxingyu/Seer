 package easy.caesar.code;
 
 
 public class Main {
     public static void main (String[] args){
        CaesarCode exampleOne = new CaesarCode("Фэзыя йз зьи ахлш пвёнлш чугрщцкфнлш дцосн, жг еютзм ъгб.", 3);
        CaesarCode exampleTwo = new CaesarCode("Съешь же ещё этих мягких французских булок, да выпей чаю.", 3);
 
         exampleOne.decrypt();
         exampleTwo.encrypt();
     }
 }
