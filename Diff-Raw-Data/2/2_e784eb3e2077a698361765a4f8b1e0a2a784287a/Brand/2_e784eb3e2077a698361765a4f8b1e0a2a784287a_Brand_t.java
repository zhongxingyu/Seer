 package com.abudko.reseller.huuto.query.enumeration;
 
 import static com.abudko.reseller.huuto.query.enumeration.Category.LENKKARIT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.TALVIHAALARI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.TALVIKENGAT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSIHAALARI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSIHOUSUT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSITAKKI;
 
 import java.util.Arrays;
 import java.util.List;
 
 public enum Brand {
 
     NO_BRAND("", "Не важно", TALVIHAALARI, VALIKAUSITAKKI, TALVIKENGAT, VALIKAUSIHOUSUT, LENKKARIT, VALIKAUSIHAALARI), //
     ADIDAS("Adidas", "Adidas", LENKKARIT), //
     ALTITUDE("Altitude", "Altitude", TALVIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     ASICS("Asics", "Asics", LENKKARIT), //
     BOGI("Bogi", "Bogi", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     CIFAF("Ciraf", "Ciraf", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     ECCO("Ecco", "Ecco", TALVIKENGAT), //
     DIDRIKSONS("Didriksons", "Didriksons", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     JONATHAN("Jonathan", "Jonathan", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
    HALTI("Halti", "Halti", VALIKAUSITAKKI, VALIKAUSIHOUSUT, TALVIKENGAT), //
     HM("H&M", "H&M", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     KAPPAHL("Kappahl", "Kappahl", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     KAVAT("Kavat", "Kavat", TALVIKENGAT), //
     KUOMA("Kuoma", "Kuoma", TALVIKENGAT), //
     LASSIE("Lassie", "Lassie", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     LASSIETEC("Lassietec", "Lassietec", TALVIHAALARI, VALIKAUSIHAALARI, TALVIKENGAT, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     LENNE("Lenne", "Lenne", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     MOTION("Motion", "Motion's", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     NIKE("Nike", "Nike", LENKKARIT), //
     PEUHU("Peuhu", "Peuhu", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     POLARN("Polarn", "Polarn o Pyret", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     PUMA("Puma", "Puma", LENKKARIT), //
     RACOON("Racoon", "Racoon", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     REIMA("Reima", "Reima", TALVIHAALARI, VALIKAUSIHAALARI, TALVIKENGAT, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     REIMATEC("Reimatec", "Reimatec", TALVIHAALARI, VALIKAUSIHAALARI, TALVIKENGAT, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     REMU("Remu", "Travalle Remu", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     SUPERFIT("Superfit", "Superfit", TALVIKENGAT), //
     TICKET("Ticket", "Ticket to Heaven", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     TIMBERLAND("Timberland", "Timberland", TALVIKENGAT), //
     TRAVALLE("Travalle", "Travalle", TALVIHAALARI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT), //
     VIKING("Viking", "Viking", LENKKARIT, TALVIKENGAT); //
 
     private String parseName;
 
     private String fullName;
 
     private Category[] categories;
 
     private static final List<Character> END_CHARS = Arrays.asList(' ', '.', ',');
 
     private Brand(String parseName, String fullName, Category... categories) {
         this.parseName = parseName;
         this.fullName = fullName;
         this.categories = categories;
     }
 
     public String getParseName() {
         return parseName;
     }
 
     public String getFullName() {
         return fullName;
     }
 
     public Category[] getCategories() {
         return categories;
     }
 
     public static Brand getBrandFrom(String string) {
         Brand[] brands = Brand.values();
         for (Brand brand : brands) {
             if (Brand.NO_BRAND.equals(brand)) {
                 continue;
             }
             if (brandStrictlyMatches(string, brand)) {
                 return brand;
             }
         }
 
         return null;
     }
 
     private static boolean brandStrictlyMatches(String string, Brand brand) {
         String brandStr = brand.getParseName().toLowerCase();
         int index = string.toLowerCase().indexOf(brand.getParseName().toLowerCase());
         if (index >= 0) {
             if (index + brandStr.length() < string.length()) {
                 char c = string.charAt(index + brandStr.length());
                 return END_CHARS.contains(c);
             }
             return true;
         }
 
         return false;
     }
 }
