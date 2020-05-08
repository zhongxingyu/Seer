 package fi.helsinki.biblex.Importer;
 
 import fi.helsinki.biblex.domain.BibTexEntry;
 import fi.helsinki.biblex.storage.Storage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author jzse
  */
 public class Importer {
 
     protected Storage storage;
 
     public Importer(Storage s) {
         storage = s;
     }
 
     /**
      * Import .bib file to the database.
      */
     public void importFromFile(String path) {
         String fileAsString = "";
         try {
             Scanner reader = new Scanner(new File(path));
             while (reader.hasNextLine()) {
                 String line = reader.nextLine();
                 fileAsString += line;
             }
         } catch (Exception e) {
             System.err.println("Failed to read the file. Exception " + e + ".");
         }
 
         ArrayList<String> entries = removeEmptyStringsFromArray(fileAsString.trim().split("@"));
         for (String string : entries) {
             try {
                 BibTexEntry entry = parseOneBibtexEntry(string);
                 if(storage.get(entry.getName()) == null) 
                     storage.add(entry); 
                 
             } catch (Exception ex) {
                 Logger.getLogger(Importer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * Parse one reference block into BibTeXEntry.
      */
     private BibTexEntry parseOneBibtexEntry(String entry) {
         
         //Separate style name from rest of the block
         String[] styleAndRest = entry.trim().split("[{]", 2);
         String style = styleAndRest[0];
         String rest = styleAndRest[1];
         
         //Separate name from rest of the block
        String[] nameAndRest = rest.trim().split("[^\\w*]", 2);
         String name = nameAndRest[0];
         rest = nameAndRest[1];
                 
         /*Now rest of the block should only contain fields and their values,
         so separate them from each other. */
        String[] fieldsAndValues = rest.trim().split("[^}|^=|^\\w|^ä][\\s*]");
         ArrayList<String> fieldAndValueArrayList = removeEmptyStringsFromArray(fieldsAndValues);  
        BibTexEntry bibEntry = new BibTexEntry(name, style);
             
         for (String string : fieldAndValueArrayList) {
             String[] nameAndValue = string.trim().split("[\\s+][=][\\s+]");
             String fieldName = nameAndValue[0];
             String value = nameAndValue[1].replaceAll("[{|}]", "");
                      
             bibEntry.put(fieldName, value);
         }
         return bibEntry;
     }
 
     private ArrayList removeEmptyStringsFromArray(String[] array) {
         ArrayList<String> returnArray = new ArrayList();
         for (String string : array) {
             if (!string.trim().isEmpty()) 
                 returnArray.add(string);
         }
         return returnArray;
     }
 }
