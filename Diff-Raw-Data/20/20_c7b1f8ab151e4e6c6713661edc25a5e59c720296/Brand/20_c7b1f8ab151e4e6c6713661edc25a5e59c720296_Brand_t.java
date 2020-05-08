 package com.abudko.reseller.huuto.query.enumeration;
 
 import static com.abudko.reseller.huuto.query.enumeration.Category.LENKKARIT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.SADEHAALARI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.TALVIHAALARI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.TALVIHOUSUT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.TALVIKENGAT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.TALVITAKKI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSIHAALARI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSIHOUSUT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSIKENGAT;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VALIKAUSITAKKI;
 import static com.abudko.reseller.huuto.query.enumeration.Category.VILLAHAALARI;
 
 import java.util.Arrays;
 import java.util.List;
 
 public enum Brand {
 
     NO_BRAND("", "Не важно", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSITAKKI, TALVIKENGAT, VALIKAUSIKENGAT,
             VALIKAUSIHOUSUT, SADEHAALARI, LENKKARIT, VALIKAUSIHAALARI, VILLAHAALARI), //
     ABEKO("Abeko", "Abeko", TALVIHAALARI), //
     ADIDAS("Adidas", "Adidas", LENKKARIT), //
     ALTITUDE("Altitude", "Altitude", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     ASICS("Asics", "Asics", LENKKARIT), //
     BOGI("Bogi", "Bogi", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI, VILLAHAALARI), //
     CELAVI("Celavi", "Celavi", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     CIFAF("Ciraf", "Ciraf", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI, TALVIKENGAT, VALIKAUSIKENGAT), //
     COLORKIDS("Color", "Color Kids", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     EVEREST("Everest", "Everest", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, TALVIKENGAT,
             VALIKAUSIKENGAT, VALIKAUSITAKKI, VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     ECCO("Ecco", "Ecco", TALVIKENGAT, VALIKAUSIKENGAT), //
    DISNEY("Disney", "Disney", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
            VALIKAUSIHOUSUT, SADEHAALARI), //
     DIDRIKSONS("Didriksons", "Didriksons", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     GEGGAMOJA("Geggamoja", "Geggamoja", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
                     VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     GREENHILL("Green", "Greenhill", VILLAHAALARI), //
     JONATHAN("Jonathan", "Jonathan", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     HALTI("Halti", "Halti", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSITAKKI, VALIKAUSIHOUSUT, VALIKAUSIHAALARI,
             SADEHAALARI, TALVIKENGAT, VALIKAUSIKENGAT), //
     HM("H&M", "H&M", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI, VILLAHAALARI), //
     HULABALU("Hulabalu", "Hulabalu", TALVIHAALARI), //
     HUPPA("Huppa", "Huppa", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     ICEPEAK("Ice", "Icepeak", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     KANGAROOS("Kangaroos", "Kangaroos", TALVIKENGAT, VALIKAUSIKENGAT), //
     KAPPAHL("Kappahl", "Kappahl", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     KAVAT("Kavat", "Kavat", TALVIKENGAT, VALIKAUSIKENGAT), //
     KIVAT("Kivat", "Kivat", VILLAHAALARI), //
     KUOMA("Kuoma", "Kuoma", TALVIKENGAT, VALIKAUSIKENGAT), //
     LASSIE("Lassie", "Lassie", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     LASSIETEC("Lassietec", "Lassietec", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, TALVIKENGAT,
             VALIKAUSIKENGAT, VALIKAUSITAKKI, VALIKAUSIHOUSUT, SADEHAALARI), //
     LEGO("Lego", "Lego", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     LENNE("Lenne", "Lenne", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     LINDBERG("Lindberg", "Lindberg", TALVIHAALARI), //
     LINDEX("Lindex", "Lindex", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     LUHTA("Luhta", "Luhta", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     MCKINLEY("Mckinley", "Mckinley", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     METOO("Me Too", "Me Too", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
                     VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     MOTION("Motion", "Motion's", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     MYNIMO("Mynimo", "Mynimo", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     MOLO("Molo", "Molo Kids", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     NAMEIT("Name", "Name It", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI, TALVIKENGAT, VALIKAUSIKENGAT, VILLAHAALARI), //
     NIKE("Nike", "Nike", LENKKARIT), //
     PEUHU("Peuhu", "Peuhu", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI, VALIKAUSIHOUSUT,
             SADEHAALARI), //
     POLARN("Polarn", "Polarn o Pyret", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     PUMA("Puma", "Puma", LENKKARIT), //
     RACOON("Racoon", "Racoon", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     REIMA("Reima", "Reima", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, TALVIKENGAT, VALIKAUSIKENGAT,
             VALIKAUSITAKKI, VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     REIMATEC("Reimatec", "Reimatec", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, TALVIKENGAT,
             VALIKAUSIKENGAT, VALIKAUSITAKKI, VALIKAUSIHOUSUT, SADEHAALARI), //
     REMU("Remu", "Travalle Remu", TALVIHAALARI, TALVITAKKI, TALVIHOUSUT, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     RUKKA("Rukka", "Rukka", TALVITAKKI, VALIKAUSITAKKI, VALIKAUSIHOUSUT, SADEHAALARI), //
     SUPERFIT("Superfit", "Superfit", TALVIKENGAT, VALIKAUSIKENGAT), //
     TICKET("Ticket", "Ticket to Heaven", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI, VILLAHAALARI), //
     TIMBERLAND("Timberland", "Timberland", TALVIKENGAT, VALIKAUSIKENGAT), //
     TUTTA("Tutta", "Tutta", VILLAHAALARI), //
     TRAVALLE("Travalle", "Travalle", TALVIHAALARI, TALVIHOUSUT, TALVITAKKI, VALIKAUSIHAALARI, VALIKAUSITAKKI,
             VALIKAUSIHOUSUT, SADEHAALARI), //
     UMBRO("Umbro", "Umbro", LENKKARIT), //
     VIKING("Viking", "Viking", LENKKARIT, TALVIKENGAT, VALIKAUSIKENGAT), //
     VINCENT("Vincent", "Vincent", TALVIKENGAT, VALIKAUSIKENGAT); //
 
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
