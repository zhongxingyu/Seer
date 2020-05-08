 package ru.nsu.ccfit.resync.pref;
 
/*
  * Immutable class to store preference
  * 
  * @author Gleb Kanterov <gleb@kanterov.ru>
  */
 public class Preference {
 
     private final String bundle;
     private final String key;
     private final String value;
 
     private Preference(String bundle, String key, String value) {
         this.bundle = bundle;
         this.key = key;
         this.value = value;
     }
 
     public static Preference newInstance(String bundle, String key, String value) {
         return new Preference(bundle, key, value);
     }
 
     public String getBundle() {
         return bundle;
     }
 
     public String getKey() {
         return key;
     }
 
     public String getValue() {
         return value;
     }
 
     @Override
     public String toString() {
         return "Preference [bundle=" + bundle + ", key=" + key + ", value=" + value + "]";
     }
 
 }
