 package com.osdiab.patient_organizer;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * Author: odiab
  * Date: 8/9/13
  * Time: 1:10 AM
  * Describes changes made during organization
  */
 public class UpdateData {
    private List<File> newPatients;
    private File folderUsed;
 
     /**
      * Constructor to inscribe update data
      * @param folderUsed Folder used
      * @param newPatients New patients in folder
      */
    public UpdateData(File folderUsed, List<File> newPatients)
     {
         this.folderUsed = folderUsed;
         this.newPatients = newPatients;
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
