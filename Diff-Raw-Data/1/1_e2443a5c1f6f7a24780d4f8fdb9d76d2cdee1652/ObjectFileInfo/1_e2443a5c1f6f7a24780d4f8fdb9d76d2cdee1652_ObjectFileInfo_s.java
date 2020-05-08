 package org.doframework;
 
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 
 /**
  * This class offers information so that a DependentObjectHandler knows how to create an object, either a
  * "reference" object or a "scratch" object.<p/>
  *
  * 
  *
  */
 public class ObjectFileInfo
 {
     /**
      * Like "Customer" or "Invoice" or whatever type of object you are saving
      */
     private String objectType;
 
     /**
      * The primary key of the object to create.
      * Note, in the case of a request to create a "scratch" object, the pk will not be representative of what
      * is in the file. Instead, you need to substitute the provided PK with the one you've provided in the file.
      *
      * If you use the utilitye method DOF.
      */
     private String pk;
 
     /**
      * Like "xml"
      */
     private String fileType;
 
     /**
      * Like "customers/Customer.15.xml". Note, the fileToLoad indicates the PK for reference objects. For
      * scratch objects, the name part is simply used to distinguish flavors of scratch objects.
      */
     private String fileToLoad;
 
     private boolean scratchMode;
 
 
     /**
      * This is the original PK -- which differs from the "PK" in scratch mode
      */
     private String originalPk;
 
     private String fileContents;
     private static ScratchPkProvider defaultScratchPrimaryKeyProvider;
 
     {
         String defaultScratchPrimaryKeyProviderClassName = HandlerMappings.getDefaultScratchPrimaryKeyProviderClassName();
         if (defaultScratchPrimaryKeyProviderClassName != null &&
                 defaultScratchPrimaryKeyProviderClassName.length() > 0)
         {
             try
             {
                 Class<? extends ScratchPkProvider> scratchClass =
                         (Class<? extends ScratchPkProvider>) Class.forName(defaultScratchPrimaryKeyProviderClassName);
                 defaultScratchPrimaryKeyProvider = scratchClass.newInstance();
             }
             catch (Throwable e)
             {
                 throw new RuntimeException(e);
             }
         }
     }
 
     public ObjectFileInfo(String objectType, String pk, String fileType)
     {
         this.objectType = objectType;
         this.originalPk = pk;
         this.pk = pk; // this will change if a scratch object
         this.fileType = fileType;
     }
 
     public ObjectFileInfo()
     {
     }
 
     public String toString()
     {
         return "{" + getFileToLoad() + ", " + getObjectType() + ", " + getPk() + ", " + getFileType() + "}";
     }
 
     /**
      * This returns the contents of the file to create the object. For creation of a "reference" object, this
      * string is the exact same as the file contents. In the case of creating a scratch object, scratch object
      * primary keys are replaced
      *
      * @return The contents of the file to create object.
      */
     public String getFileContentsAsString()
     {
         if (fileContents == null)
         {
             String originalFileContents = DOF.getResourceAsString(getFileToLoad());
             if (isScratchMode())
             {
                 fileContents = swapOutPkWithScratchValue(originalFileContents);
             }
             else
             {
                 fileContents = originalFileContents;
             }
         }
         return fileContents;
     }
 
     /**
      * This method is called when the file contents need to processed for "scratch" mode.
      * @param originalFileContents
      * @return The original file contents with the scratch PK
      */
     private String swapOutPkWithScratchValue(String originalFileContents)
     {
         String patternForScratchPK = HandlerMappings.getPatternForScratchPK();
         DependentObjectHandler dofHandler = DOF.getHandlerForObject(objectType, fileType);
         String scratchPk;
         if (dofHandler instanceof ScratchPkProvider)
         {
             scratchPk = ((ScratchPkProvider)dofHandler).getScratchPk();
         }
         else if (defaultScratchPrimaryKeyProvider != null)
         {
             scratchPk = defaultScratchPrimaryKeyProvider.getScratchPk();
         }
         else
         {
             scratchPk = System.currentTimeMillis() + "";
         }
         // Non regexp replacement!
         pk = scratchPk;
         if (originalFileContents.indexOf(patternForScratchPK) == -1)
         {
             throw new RuntimeException("Error trying to fill in scratch primary key: Could not find pattern "
                                        + patternForScratchPK
                                        + " inside of file "
                                        + originalFileContents);
         }
         return originalFileContents.replace(patternForScratchPK, scratchPk);
     }
 
     public InputStream getFileContentsAsInputStream()
     {
         InputStream result = new ByteArrayInputStream(getFileContentsAsString().getBytes());
         return result;
     }
 
 
     public boolean isScratchMode()
     {
         return scratchMode;
     }
 
     public void setScratchMode(boolean scratchMode)
     {
         this.scratchMode = scratchMode;
     }
 
     public String getOriginalPk()
     {
         return originalPk;
     }
 
     public void setOriginalPk(String originalPk)
     {
         this.originalPk = originalPk;
     }
 
     public String getObjectType()
     {
         return objectType;
     }
 
     public String getPk()
     {
         return pk;
     }
 
     public String getFileType()
     {
         return fileType;
     }
 
     /**
      * This is the passed in path of the file to load. Note, you MUST NOT
      * use this to retrieve the file data yourself, as for scratch objects
      * the PK needs to get swapped. So call getFileContentsAsInputStream()
      * or getFileContentsAsString() for the contents of the file.
      * @return path of the file to load, which may be inside the classpath or
      * relative to the DOF_DIR environment variable.
      */
     public String getFileToLoad()
     {
         return fileToLoad;
     }
 
     public void setFileToLoad(String fileToLoad)
     {
         this.fileToLoad = fileToLoad;
     }
 }
