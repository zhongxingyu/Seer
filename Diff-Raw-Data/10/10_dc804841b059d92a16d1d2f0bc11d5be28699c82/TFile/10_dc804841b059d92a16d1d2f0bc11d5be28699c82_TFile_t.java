 //=============================================================================
 // TFile.java
 //=============================================================================
 
 //===============================
 // Imports
 //===============================
import java.io.File;
 
 
 public class TFile
 {
     //===============================================================
     //      Constants
     //===============================================================
 
 
     //===============================================================
     //      Instance Variables
     //===============================================================
     private File   file;
     private String fileName;
     private String fileExtension;
 
     //===============================================================
     //      Constructors
     //===============================================================
 
     public TFile(File file, String name, String extension)
     {
         this.file          = file;
         fileName      = name;
         fileExtension = extension;
     }
 
     //===============================================================
     //      Methods
     //===============================================================
     
     public void moveFile(String path)
     {
 
     }
 
     public boolean renameFile(String name)
     {
         setFileName(name);
         
         File renameFile = new File(name);
         return file.renameTo(renameFile);
     }
 
 
 
     //===============================================================
     //      getters / setters
     //===============================================================
 
     //=================================
     //      file
     //=================================
 
    public File getFile()
     {
         return file;
     }
 
     public void setFile(File file)
     {
         this.file = file;
     }
 
     //=================================
     //      fileName
     //=================================
 
    public String getFileName()
     {
         return fileName;
     }
 
    public void setFileName(String name)
     {
         fileName = name;
     }
 
     //=================================
     //      fileExtension
     //=================================
 
     public String getExtension()
     {
         return fileName;
     }
 
 
     public void setExtension(String extension)
     {
         fileExtension = extension;
     }
     
     //===============================================================
     //      conditionals
     //===============================================================
 
     public boolean isMovie() { return false; }
     public boolean isShow()  { return false; }
 
 
 
 
 
 }
