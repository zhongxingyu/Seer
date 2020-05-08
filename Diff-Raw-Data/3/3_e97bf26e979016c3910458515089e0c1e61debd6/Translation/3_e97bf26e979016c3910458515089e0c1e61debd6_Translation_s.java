 package de.aidger.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.Locale;
 import java.util.PropertyResourceBundle;
 import java.util.Vector;
 
 import de.unistuttgart.iste.se.adohive.util.tuple.Pair;
 
 /**
  * Sets the translation of the program. If Strings of the current language can't
  * be found, it will fall back to the default language.
  * 
  * @author aidGer Team
  */
 public class Translation {
 
     /**
      * The file name of the translation.
      */
     protected String filePath;
 
     /**
      * The bundle holding the translation.
      */
     protected static PropertyResourceBundle bundle = null;
 
     /**
      * Initializes this class. Resolves the file path of the translation files.
      * 
      * @param path
      *            The path of the aidGer settings.
      * @param language
      *            The language to which the program will be translated.
      */
     public Translation(String path, String language) {
         /* Create path if necessary */
         filePath = path + "lang/";
         File languagePath = new File(filePath);
         if ((!languagePath.exists() || !languagePath.isDirectory())
                 && !languagePath.mkdirs()) {
            System.err.println("Konnte Verzeichnis für Übersetzung nicht "
                    + "erstellen");
         }
 
         /* Load the language file */
         try {
             File inputFile = new File(filePath + language + ".properties");
             FileInputStream inputStream = new FileInputStream(inputFile);
             bundle = new PropertyResourceBundle(inputStream);
             inputStream.close();
         } catch (Exception e) {
         }
     }
 
     /**
      * Returns the translation of the specified string from the bundle.
      * 
      * @param id
      *            The string to translate.
      * @return The translated string or return the id.
      */
     public static String _(String id) {
         try {
             return bundle.getString(id);
         } catch (Exception e) {
             /*
              * If the string is not translated in the bundle, or the bundle
              * cannot be found, just return the original string.
              */
             return id;
         }
     }
 
     /**
      * Get a list of all languages installed on the system. The format is 0 =>
      * short, 1 => long language name.
      * 
      * @return The list of all installed languages
      */
     public List<Pair<String, String>> getLanguages() {
         List<Pair<String, String>> list = new Vector<Pair<String, String>>();
         /* Add English as standard language */
         list.add(new Pair<String, String>("en", new Locale("en")
                 .getDisplayLanguage()));
 
         /* Search all files in the lang directory and add them */
         File dir = new File(filePath);
         File[] files = dir.listFiles();
         if (files == null) {
             return list;
         }
         for (File file : files) {
             String filename = file.getName();
             int idx = filename.indexOf(".properties");
             if (idx > -1) {
                 String lang = filename.substring(0, idx);
                 Locale loc = new Locale(lang);
                 list.add(new Pair<String, String>(lang, loc
                         .getDisplayLanguage()));
             }
         }
 
         return list;
     }
 }
