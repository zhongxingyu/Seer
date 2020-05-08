 /*
  * Kajona Language File Editor Core
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 
 package de.mulchprod.kajona.languageeditor.core.textfile;
 
 import de.mulchprod.kajona.languageeditor.core.config.Configuration;
 import de.mulchprod.kajona.languageeditor.core.config.Configuration.ConfigNotSetException;
 import de.mulchprod.kajona.languageeditor.core.logger.LELogger;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textentry.EntryNotSetException;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textfile.KeyNotFoundException;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textfile.KeyNotUniqueException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author sidler
  */
 public class LanguageFileSet implements ILanguageFileSet {
 
     private String module;
     private String modulePart;
 
     private HashMap<String, Textfile>fileMap = new HashMap<String, Textfile>();
 
 
     public LanguageFileSet(Textfile file) {
         this.module = file.getModule();
         this.modulePart = file.getModulepart();
         this.addTextfile(file);
     }
 
     public void createNewFileForNewLanguage(String newLanguage) {
 
         //check, if target language is not yet exisiting
         if(this.getListOfLanguages().contains(newLanguage)) {
             LELogger.getInstance().logInfo("Language "+newLanguage+" for fileSet "+this.module+"/"+this.modulePart+" already existing!");
             return;
         }
 
 
         //use the first one in the set as the master-file
         if(!fileMap.values().isEmpty()) {
             //Create a new textfile
             Textfile newFile = new Textfile();
 
             Textfile masterFile = fileMap.values().iterator().next();
 
             newFile.setModule(masterFile.getModule());
             newFile.setModulepart(masterFile.getModulepart());
 
             newFile.setLanguage(newLanguage);
             newFile.setSourcePath(masterFile.getSourcePath().replace(masterFile.getLanguage()+".php", newLanguage+".php"));
             newFile.setFilename(masterFile.getFilename().replace(masterFile.getLanguage()+".php", newLanguage+".php"));
             LELogger.getInstance().logInfo("Created new file with props: \n"+newFile.toString());
 
             //copy keys
             for(Textentry masterEntry  : masterFile.getTextEntries().values()) {
                 try {
                     newFile.setEntryToInsert(masterEntry.getReadableKey());
                 } catch (KeyNotUniqueException ex) {
                 } catch (EntryNotSetException ex) {
                 }
             }
 
             this.addTextfile(newFile);
         }
     }
 
     public boolean needToSave() {
         for(Textfile file : fileMap.values())
             if(file.isFileHasChanged())
                 return true;
 
         return false;
     }
 
     public ArrayList<String> getListOfLanguages() {
         ArrayList<String> languageList = new ArrayList<String>();
         for(Textfile file: fileMap.values())
             if(!languageList.contains(file.getLanguage()))
                 languageList.add(file.getLanguage());
 
         return languageList;
     }
 
     public ArrayList<String> getAllKeys() {
         ArrayList<String> returnList = new ArrayList<String>();
         
         for(Textfile file: fileMap.values()) {
             for(Textentry entry : file.getTextEntries().values())
                 if(!returnList.contains(entry.getReadableKey()))
                     returnList.add(entry.getReadableKey());
         }
 
         Collections.sort(returnList);
         return returnList;
     }
 
     public boolean isKeyValidForAllLanguages(String keyToTest) {
         boolean bitFound = true;
         for(Textfile file: fileMap.values()) {
             boolean bitFoundInFile = false;
             for(Textentry entry : file.getTextEntries().values()) {
                 if(entry.getReadableKey().equals(keyToTest) && entry.getReadableValue().length() >= Configuration.getInstance().getMinStringLength()) {
                     bitFoundInFile = true;
                     break;
                 }
             }
             bitFound = bitFound && bitFoundInFile;
         }
         
         return bitFound;
     }
 
     public boolean updateValue(String key, String newValue, String language) {
         Textfile fileToUpdate = null;
         try {
             for (Textfile file : fileMap.values()) {
                 if(file.getLanguage().equals(language)) {
                     fileToUpdate = file;
                     if(file.setValue(key, newValue)) {
                         return true;
                     }
                 }
             }
         } catch (KeyNotFoundException ex) {
             try {
                 //create a new key
                 LELogger.getInstance().logInfo(ex.getMessage());
                 LELogger.getInstance().logInfo("creating new key/value pair");
                 fileToUpdate.setEntryToInsert(key);
                 fileToUpdate.setValue(key, newValue);
                 
             } catch (Exception ex1) {
                 LELogger.getInstance().logInfo(ex1.getMessage());
             }
             
         }
         return false;
     }
 
     public String getValueForKey(String key, String language) {
         try {
             for (Textfile file : fileMap.values()) {
                 if(file.getLanguage().equals(language))
                     return file.getValue(key);
             }
         } catch (KeyNotFoundException ex) {
             LELogger.getInstance().logInfo(ex.getMessage());
         }
         return "";
     }
     
     public String getFilenameForLanguage(String language) {
         for (Textfile file : fileMap.values()) {
             if(file.getLanguage().equals(language))
                 try {
                 return file.getSourcePath().replace(Configuration.getInstance().getKajonaProjectPath(), "");
             } catch (ConfigNotSetException ex) {
                 LELogger.getInstance().logInfo(ex.getMessage());
             }
         }
         return "";
     }
 
     public boolean updateKeyValue(String oldKey, String newKey) throws KeyNotUniqueException {
        for (Textfile file : fileMap.values()) {
            try {
                 file.setKeyValue(oldKey, newKey);
            } catch (KeyNotFoundException ex) {
                LELogger.getInstance().logInfo(ex.getMessage());
             }
         }
        return true;
     }
 
     public boolean createKeyValue(String keyToCreate) throws KeyNotUniqueException {
         try {
             for (Textfile file : fileMap.values()) {
                 file.setEntryToInsert(keyToCreate);
             }
              return true;
         } catch (EntryNotSetException ex) {
             LELogger.getInstance().logInfo(ex.getMessage());
         }
         
         return false;
     }
 
     public boolean deleteKeyValue(String keyToDelete) {
         
         for (Textfile file : fileMap.values()) {
             try {
                 file.setEntryToDelete(keyToDelete);
             } catch (KeyNotFoundException ex) {
                 LELogger.getInstance().logInfo(ex.getMessage());
             }
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "LanguageFileSet: "+module+"/"+modulePart+"\n"+
                 "  nrOfFiles included: "+this.fileMap.size();
     }
 
     public void addTextfile(Textfile file) {
         this.fileMap.put(file.getLanguage(), file);
     }
 
     public HashMap<String, Textfile> getFileMap() {
         return fileMap;
     }
 
     public String getModule() {
         return module;
     }
 
     public String getModulePart() {
         return modulePart;
     }
 
     @Override
     public String createNewValidKey(String keyToCheck) {
         if(!this.getAllKeys().contains(keyToCheck))
             return keyToCheck;
 
         keyToCheck += "_1";
         LELogger.getInstance().logInfo("Key already existing, shifted to "+keyToCheck);
         return this.createNewValidKey(keyToCheck);
     }
 
 
 }
