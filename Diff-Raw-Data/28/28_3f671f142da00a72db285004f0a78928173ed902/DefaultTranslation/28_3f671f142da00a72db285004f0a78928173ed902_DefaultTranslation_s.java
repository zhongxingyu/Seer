 package de.aidger.utils;
 
 /**
  * Contains the default language
  * 
 * @author Phil
  */
 public class DefaultTranslation extends Translation {
 
     public DefaultTranslation(String path) {
         // TODO specify the id's for languages
         super(path, "de");
     }
 
     @Override
     public String get(String id) {
         return bundle.getString(id);
     }
 }
