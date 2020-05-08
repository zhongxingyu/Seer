 package com.osdiab.patient_organizer;
 
 import java.io.File;
 import java.util.List;
import java.util.Map;
 
 /**
  * Author: odiab
  * Date: 8/9/13
  * Time: 1:10 AM
  * Describes changes made during organization
  */
 public class UpdateData {
    private final Map<File, Exception> errors;
    private final List<File> newPatients;
    private final File folderUsed;
 
     /**
      * Constructor to inscribe update data
      * @param folderUsed Folder used
      * @param newPatients New patients in folder
      */
    public UpdateData(File folderUsed, List<File> newPatients, Map<File, Exception> errors)
     {
         this.folderUsed = folderUsed;
         this.newPatients = newPatients;
        this.errors = errors;
    }

    /**
     * Get errors in the operation
     * @return new patients
     */
    public Map<File, Exception> getErrors() {
        return errors;
     }
 
     /**
      * Get new patients for this folder
      * @return new patients
      */
     public List<File> getNewPatients()
     {
         return newPatients;
     }
 
     /**
      * Get folder
      * @return folder
      */
     public File getFolderUsed()
     {
         return folderUsed;
     }
 }
