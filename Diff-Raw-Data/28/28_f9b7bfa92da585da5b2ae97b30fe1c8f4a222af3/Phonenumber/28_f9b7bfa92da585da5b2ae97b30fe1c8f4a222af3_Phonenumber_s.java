 package dk.drb.blacktiger.util;
 
 import org.springframework.util.Assert;
 
 /**
  *
  * @author michael
  */
public class Phonenumber {
 
     
    private Phonenumber() {
     }
     
     public static String normalize(String number) {
         Assert.hasLength(number, "Number is null or empty");
         
         if(!number.startsWith("+")) {
             number = "+" + number;
         }
         return number;
     }
     
     
 }
