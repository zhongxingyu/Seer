 /*
  * Copyright (C) 2013 Felix Wiemuth
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package nipgm.translations;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import static nipgm.util.Util.*;
 
 /**
  * Provide text strings for the application from a selected translation file.
  *
  * @author Felix Wiemuth
  */
 public class Translator implements Texts {
 
     private static int COMMENT_BEGIN = '#';
     private static int ASSIGN = '=';
     private File baseDirectory;
     private Map<String, String> data = new HashMap<>();
 
     /**
      * Create a translator.
      *
      * @param baseDirectory The directory where the translation files are
      * stored.
      */
     public Translator(File baseDirectory) {
         this.baseDirectory = baseDirectory;
     }
 
     /**
      * Get the files of the translation file directory. The names of these files
      * represent the available translations.
      *
      * @return
      */
     public File[] getAvailableTranslations() {
         return baseDirectory.listFiles();
     }
 
     /**
      * Load the texts of the translation file 'file'.
      *
      * @param file
      */
     public void loadTranslation(File file) {
         if (!file.isFile()) {
             err("File '" + file.toString() + "' does not exist!");
             return;
         }
         data = new HashMap<>();
         BufferedReader reader;
         try {
             reader = getReader(file);
         } catch (FileNotFoundException ex) {
             err(ex.getMessage());
             return;
         }
         String line;
        int lineCount = 0;
         while (true) {
             try {
                 line = reader.readLine();
             } catch (IOException e) {
                 err("I/O error: " + e.getMessage());
                 break;
             }
            lineCount++;
             if (line == null) {
                 break;
             }
            addItem(line, lineCount);
         }
         try {
             reader.close();
         } catch (IOException ex) {
             err(ex);
         }
     }
 
    private void addItem(String raw, int lineNumber) {
         String line; //line without comment
         try {
             line = raw.substring(0, raw.indexOf(COMMENT_BEGIN));
         } catch (IndexOutOfBoundsException e) {
             line = raw;
         }
         if (line.isEmpty()) {
             return;
         }
         int pos = line.indexOf(ASSIGN);
         String key;
         String value;
         try {
             key = line.substring(0, pos);
             value = line.substring(pos + 1);
         } catch (IndexOutOfBoundsException ex) {
            warn("Skipping incorrect line " + lineNumber + " in translation file");
             return;
         }
         try {
             if (data.containsKey(key)) {
                 warn("Translation file includes key '" + key + "' multiple times!");
             }
             data.put(key, value);
         } catch (IllegalArgumentException ex) {
             warn("Cannot use translation with key '" + key + "': " + ex);
         }
     }
 
     /**
      * Get the text from the currently loaded translation corresponding to
      * 'key'. If there is no text belonging to 'key', "NULL" is returned.
      *
      * @param key
      * @return
      */
     @Override
     public String get(String key) {
         String res = data.get(key);
         if (res == null) {
             warn("The requested text from the translation file with key '"
                     + key + "' is not available.");
             return "NULL";
         }
         return res;
     }
 }
