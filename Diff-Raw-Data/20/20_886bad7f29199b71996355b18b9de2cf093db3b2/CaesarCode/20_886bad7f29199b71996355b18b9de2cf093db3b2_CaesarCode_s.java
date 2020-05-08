 package easy.caesar.code;
 
 
 public class CaesarCode {
     private int step;
     private String originalString; //originalString string
     private String resultString = (""); //resultString string
     private char buf; //buffer for one char
     private static String alphabet = ("абвгдеёжзиклмнопрстуфхцчшщъыьэюя"); //alphabet
     private static int lng = 33; //length of alphabet
     private int pos; // position of char in alphabet
 
     public CaesarCode(String userEncryptedString, int userStep){
         originalString = userEncryptedString;
         step = userStep;
     }
 
     public void encrypt(){
         originalString = originalString.toLowerCase(); //convert originalString string to lower case
         for(int i = 0; i < originalString.length(); i++){
             buf = originalString.charAt(i);
            pos = alphabet.indexOf(buf) + step;
            if(pos > lng){
                pos = pos - lng;
            }
             resultString = resultString.concat(alphabet.substring(pos, pos+1));
         }
         System.out.println("Encrypted string: "+ resultString);
     }
 
     public void decrypt(){
         originalString = originalString.toLowerCase(); //convert originalString string to lower case
         for(int i = 0; i < originalString.length(); i++){
             buf = originalString.charAt(i);
            pos = alphabet.indexOf(buf) - step;
            if(pos < 0){
                pos = lng - pos;
            }
             resultString = resultString.concat(alphabet.substring(pos, pos+1));
         }
         System.out.println("Decrypted string: "+ resultString);
     }
 }
