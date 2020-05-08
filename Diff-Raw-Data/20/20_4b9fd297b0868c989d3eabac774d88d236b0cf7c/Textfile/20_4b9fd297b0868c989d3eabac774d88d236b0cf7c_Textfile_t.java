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
 
 import de.mulchprod.kajona.languageeditor.core.CoreBaseException;
 import de.mulchprod.kajona.languageeditor.core.logger.LELogger;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textentry.EntryNotSetException;
 import java.io.File;
 import java.util.TreeMap;
 
 /**
  * This class represents a single textfile.
  * A textfile contains the key value pairs of a given module / element in a single language.
  * The k-v-pairs themselfs are represented by instances of Textentry
  * @author sidler
  */
 public class Textfile implements ITextfile {
     private String module;
     private String language;
     private String sourcePath;
     private String filename;
     private String modulepart;
     private String area;
 
     private File fileObject;
 
     private boolean fileHasChanged = false;
 
 
     private TreeMap<String, Textentry> textEntries = new TreeMap<String, Textentry>();
     private TreeMap<String, Textentry> nonEditableTextEntries = new TreeMap<String, Textentry>();
 
 
     public void setUpTextfileFromFileobject() throws FileNotSetException {
         if(fileObject == null)
             throw new FileNotSetException();
 
         String rawfilename = fileObject.getName();
         rawfilename = rawfilename.replace(".php", "");
         String[] filenameparts = rawfilename.split("_");
        
        if(filenameparts.length == 3) {
 
            String foldername = fileObject.getPath().replace("\\", "/").replace("/"+fileObject.getName(), "");
            String rawModulepart = foldername.substring(foldername.lastIndexOf("modul_")+6);
 
            this.setLanguage(filenameparts[2]);
            this.setModule(rawModulepart);
            this.setModulepart(filenameparts[1]);
            this.setSourcePath(fileObject.getPath());
            this.setFilename(fileObject.getName());
        
        }
 
     }
 
     private String beautifyKey(String key) {
         return replaceChars(key);
     }
 
     public static String replaceChars(String key) {
         //beautify string
         key = key.trim().replace(" ", "_");
         key = key.replaceAll("[^a-zA-Z0-9_\\-]", "-");
 
         //System.out.println(key);
         return key;
     }
 
     /**
      * Updates the key of a textentry.
      * Searches the Map of entries in order to find the matching key.
      * Markes the file as dirty if the entry to update was found.
      *
      * @param oldReadableKey
      * @param newReadableKey
      * @return
      * @throws de.mulchprod.kajona.languageeditor.core.textfile.Textfile.KeyNotFoundException
      * @throws de.mulchprod.kajona.languageeditor.core.textfile.Textfile.KeyNotUniqueException
      */
     public boolean setKeyValue(String oldReadableKey, String newReadableKey) throws KeyNotFoundException, KeyNotUniqueException {
         LELogger.getInstance().logInfo("Textfile.setKeyValue: "+oldReadableKey+" to "+newReadableKey+" --> "+this.filename);
         Textentry entryToUpdate = null;
         Textentry entryWithNewKey = null;
         
         for(Textentry tempEntry : textEntries.values()) {
             if(tempEntry.getReadableKey().equals(oldReadableKey)) {
                 entryToUpdate = tempEntry;
             }
             if(tempEntry.getReadableKey().equals(newReadableKey)) {
                 entryWithNewKey = tempEntry;
             }
         }
 
         if(entryToUpdate == null)
             throw new KeyNotFoundException("Key to update >"+oldReadableKey+"< not found");
         
         if(entryWithNewKey != null)
             throw new KeyNotUniqueException("Key >"+newReadableKey+"< already existing");
 
 
         newReadableKey = beautifyKey(newReadableKey);
 
         //if we reached up till here, update!
         entryToUpdate.setReadableKey(newReadableKey);
 
         fileHasChanged = true;
         return true;
     }
 
     public String getValue(String key) throws KeyNotFoundException {
         for(Textentry tempEntry : textEntries.values()) {
             if(tempEntry.getReadableKey().equals(key)) {
                 return tempEntry.getReadableValue();
             }
         }
         throw new KeyNotFoundException("Key to update >"+key+"< not found");
     }
 
 
     /**
      * Sets the value of a single texentries.
      * Marks the file as dirty if the value differs from the value before.
      *
      * @param readableKey
      * @param newValue
      * @return
      * @throws de.mulchprod.kajona.languageeditor.core.textfile.Textfile.KeyNotFoundException
      */
     public boolean setValue(String readableKey, String newValue) throws KeyNotFoundException {
         LELogger.getInstance().logInfo("Textfile.setValue: Key: "+readableKey+" --> "+this.filename);
         Textentry entryToUpdate = null;
         for(Textentry tempEntry : textEntries.values()) {
             if(tempEntry.getReadableKey().equals(readableKey)) {
                 entryToUpdate = tempEntry;
             }
         }
 
         if(entryToUpdate == null)
             throw new KeyNotFoundException("Key to update >"+readableKey+"< not found");
 
         //if we reached up till here, update!
         if(!entryToUpdate.getReadableValue().equals(newValue)) {
             entryToUpdate.setReadableValue(newValue);
             fileHasChanged = true;
         }
         return true;
     }
 
     public boolean setEntryToDelete(String keyToDelete) throws KeyNotFoundException {
         LELogger.getInstance().logInfo("Textfile.setEntryToDelete: "+keyToDelete+" --> "+this.filename);
         for(Textentry tempEntry : textEntries.values()) {
             if(tempEntry.getReadableKey().equals(keyToDelete)) {
                 textEntries.remove(tempEntry.getReadableKey());
                 LELogger.getInstance().logInfo("removed "+keyToDelete);
                 tempEntry = null;
                 break;
             }
         }
 
         fileHasChanged = true;
         return true;
     }
 
     public boolean setEntryToInsert(String newKey) throws KeyNotUniqueException, EntryNotSetException {
         LELogger.getInstance().logInfo("Textfile.setEntryToInsert: "+newKey+" --> "+this.filename);
         newKey = beautifyKey(newKey);
         
         Textentry entryToUpdate = null;
         for(Textentry tempEntry : textEntries.values()) {
             if(tempEntry.getReadableKey().equals(newKey)) {
                 entryToUpdate = tempEntry;
             }
         }
 
         if(entryToUpdate != null)
             throw new KeyNotUniqueException("Key to create >"+newKey+"< aready existing");
 
         //create a new row. therefore build a complete row in order to pass it to the entry-class
         Textentry newEntry = new Textentry();
         newEntry.setReadableKey(newKey);
         this.addTextEntry(newEntry);
 
         fileHasChanged = true;
         return true;
     }
 
     @Override
     public String toString() {
         return " single file: "+filename+" \n " +
                "   module: "+module+" language: "+language+" modulepart: "+modulepart+"\n"+
                "   sourcepath: "+sourcePath;
     }
 
     public void addTextEntry(Textentry entry) {
         if(entry.isIsEditableEntry())
             textEntries.put(entry.getReadableKey(), entry);
         else
             nonEditableTextEntries.put(entry.getReadableKey(), entry);
     }
 
     
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public String getModule() {
         return module;
     }
 
     public void setModule(String module) {
         this.module = module;
     }
 
     public String getSourcePath() {
         return sourcePath;
     }
 
     public void setSourcePath(String sourcePath) {
         this.sourcePath = sourcePath;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public String getModulepart() {
         return modulepart;
     }
 
     public void setModulepart(String modulepart) {
         this.modulepart = modulepart;
     }
 
     public File getFileObject() {
         return fileObject;
     }
 
     public void setFileObject(File fileObject) {
         this.fileObject = fileObject;
     }
 
     public TreeMap<String, Textentry> getTextEntries() {
         return textEntries;
     }
 
     public String getArea() {
         return area;
     }
 
     public void setArea(String area) {
         this.area = area;
     }
 
     public boolean isFileHasChanged() {
         return fileHasChanged;
     }
 
     public void setFileHasChanged(boolean fileHasChanged) {
         this.fileHasChanged = fileHasChanged;
     }
 
     public TreeMap<String, Textentry> getNonEditableTextEntries() {
         return nonEditableTextEntries;
     }
 
     public void setNonEditableTextEntries(TreeMap<String, Textentry> nonEditableTextEntries) {
         this.nonEditableTextEntries = nonEditableTextEntries;
     }
 
     public class FileNotSetException extends CoreBaseException {
     }
 
     public class KeyNotUniqueException extends CoreBaseException {
         public KeyNotUniqueException(String s) { super(s); }
     }
 
     public class KeyNotFoundException extends CoreBaseException {
         public KeyNotFoundException(String s) { super(s); }
     }
 }
