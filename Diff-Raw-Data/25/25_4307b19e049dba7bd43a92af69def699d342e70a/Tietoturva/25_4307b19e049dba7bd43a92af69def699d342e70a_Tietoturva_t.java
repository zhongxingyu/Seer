 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.viitearto;
 
 import java.util.HashMap;
 
 /**
  *
  * @author kennyhei
  */
 public class Tietoturva {
     
     private static HashMap<String, String> virheet;
     
     public Tietoturva() {
         virheet = new HashMap<String, String>();
         virheet.put("yearError", null);
         virheet.put("authorError", null);
         virheet.put("titleError", null);
         virheet.put("publisherError", null);
        virheet.clear();
     }
 
     public String estaCrossSiteScripting(String mjono) {
         mjono = mjono.replace("<", "&lt;");
         mjono = mjono.replace(">", "&gt;");
         return mjono;
     }
     
     public void lisaaVirhe(String virheenLaatu, String virhe) {
         virheet.put(virheenLaatu, virhe);
     }
     
     public String getYearError() {
         return virheet.get("yearError");
     }
     
     public String getAuthorError() {
         return virheet.get("authorError");
     }
     
     public String getTitleError() {
         return virheet.get("titleError");
     }
     
     public String getPublisherError() {
         return virheet.get("publisherError");
     }
 
     public void nollaaVirheet() {
         virheet.clear();
     }
     
     public boolean onkoVirheita() {
         return !virheet.isEmpty();
     }
 }
