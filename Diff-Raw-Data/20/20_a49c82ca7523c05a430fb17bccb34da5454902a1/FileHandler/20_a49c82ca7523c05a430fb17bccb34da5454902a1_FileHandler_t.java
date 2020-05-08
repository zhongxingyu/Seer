 package net.daboross.bukkitdev.playerdata;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 
 /**
  * This class provides Functions to read and write to and from Files.
  */
 final class FileHandler {
 
     /**
      * Writes a file with text lines to a given File.
      *
      * @param file This is the File to write to.
      * @param lines This is the text to put in a file.
      * @return True if successful, False otherwise.
      */
     protected static boolean WriteFile(File file, ArrayList<String> lines) {
         Bukkit.getServer().getLogger().log(Level.FINEST, "Player Data File Handler: Writing New File: {0}", file.getAbsolutePath());
         if (lines == null || file == null) {
             return false;
         }
         if (file.canWrite() || !file.exists()) {
             try {
                 file.getParentFile().mkdirs();
                 if (!file.exists()) {
                     file.createNewFile();
                 }
                FileWriter fw = new FileWriter(file);
                BufferedWriter bf = new BufferedWriter(fw);
                for (int i = 0; i < lines.size(); i++) {
                     bf.write(lines.get(i));
                     bf.newLine();
                 }
                try {
                    bf.close();
                } catch (IOException ex) {
                }
                try {
                    fw.close();
                } catch (IOException ex) {
                }
                 return true;
             } catch (Exception e) {
             }
         }
         return false;
     }
 
     /**
      * Reads a given file into an array list of strings
      *
      * @param file This is the file to read from
      * @return The text in the file, or null if it doesn't exist.
      */
     protected static ArrayList<String> ReadFile(File file) {
         ArrayList<String> lines = new ArrayList<String>();
         if (file.canRead()) {
             FileReader fr = null;
             BufferedReader bf = null;
             try {
                 fr = new FileReader(file);
                 bf = new BufferedReader(fr);
                 while (true) {
                     String line = bf.readLine();
                     if (line == null) {
                         break;
                     }
                     lines.add(line);
                 }
             } catch (Exception e) {
             }
             try {
                 bf.close();
             } catch (Exception e) {
             }
             try {
                 fr.close();
             } catch (Exception e) {
             }
         }
         return lines;
     }
}
