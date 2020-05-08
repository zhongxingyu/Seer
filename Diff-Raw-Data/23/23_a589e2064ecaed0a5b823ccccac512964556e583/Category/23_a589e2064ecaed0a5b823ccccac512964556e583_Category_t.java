 package com.abudko.reseller.huuto.query.enumeration;
 
 
 public enum Category {
 
    TALVIHAALARI("talvihaalari%20OR%20toppahaalari", "Зим. комбинезоны", ""),
     
    TALVIHOUSUT("talvihousut%20OR%20toppahousut", "Зим. штаны", ""),
     
    TALVITAKKI("talvitakki%20OR%20toppatakki", "Зим. куртки", ""),
 
     VALIKAUSIHAALARI("v%E4likausihaalari", "Деми комбинезоны", ""),
 
     VALIKAUSIHOUSUT("v%E4likausihousut", "Деми штаны", ""),
     
     VALIKAUSITAKKI("v%E4likausitakki", "Деми куртки", ""),
 
     TALVIKENGAT("talvikeng%E4t", "Зим. обувь", "728-736#736"),
     
     LENKKARIT("lenkkarit", "Кроссовки", "729-737#737");
 
     private String value;
 
     private String id;
 
     private String label;
 
     private Category(String value, String label, String id) {
         this.value = value;
         this.label = label;
         this.id = id;
     }
 
     public String getValue() {
         return this.value;
     }
 
     public String getLabel() {
         return this.label;
     }
 
     public String getId() {
         return this.id;
     }
 }
