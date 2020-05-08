 package de.aidger.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.PropertyResourceBundle;
 
 /**
  * Sets the translation of the program. If Strings of the current language can't
  * be found, it will fall back to the default language.
  * 
 * @author Philipp Pirrung
  */
 public class Translation {
 
     /**
      * The file name of the translation.
      */
     protected String filePath;
 
     /**
      * The bundle holding the translation.
      */
     protected PropertyResourceBundle bundle = null;
 
     /**
      * The default translation of this program.
      */
     private DefaultTranslation defaultTranslation = null;
 
     /**
      * The file path to the aidGer folder.
      */
     protected String path;
 
     /**
      * Initializes this class. Resolves the file path of the translation files.
      * 
      * @param path
      *            The path of the aidGer settings.
      * @param language
      *            The language to which the program will be translated.
      */
     public Translation(String path, String language) {
         filePath = path + "lang/";
         File languagePath = new File(filePath);
         if ((!languagePath.exists() || !languagePath.isDirectory())
                 && !languagePath.mkdirs()) {
             System.err.println("Konnte Verzeichnis für bersetzung nicht "
                     + "erstellen");
         }
         filePath = filePath + language + ".properties";
         this.path = path;
         initialize();
     }
 
     /**
      * Creates a ResourceBundle of the language file.
      */
     public void initialize() {
         try {
             File inputFile = new File(filePath);
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
      * @return The translated string.
      */
     public String get(String id) {
         try {
             return bundle.getString(id);
         } catch (Exception e) {
             /*
              * If the string is not translated in the bundle, or the bundle
              * cannot be found, fall back to the default translation.
              */
            defaultTranslation = new DefaultTranslation(path);
             return defaultTranslation.get(id);
         }
     }
 
 }
