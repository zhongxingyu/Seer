 package applicationLogic;
 
 import java.util.List;
 
 /**
  * Supports Inproceedings for now.
  */
 public class Generate {
     
     public static String identifier(String author, int year, String title) {
         String toReturn = "";
        String auth = author.toLowerCase().trim();
         
         if (auth.length() < 6) {
             toReturn += auth;
         } else {
             toReturn += auth.substring(0, 6);
         }
         
         toReturn += year;
         toReturn += "-";
         
         String tit = title.toLowerCase().trim();
         
         if (tit.length() < 6) {
             toReturn += tit;
         } else {
             toReturn += tit.substring(0, 6);
         }
         
         return toReturn;
     }
     
     public static boolean isUnique(List<String> citekeys, String citekey) {
         for (String string : citekeys) {
             if (citekey.equals(string)) {
                 return false;
             }
         }
         return true;
     }
 }
