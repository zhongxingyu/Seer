 package com.powersurgepub.twodue.disk;
 
   import com.powersurgepub.psdatalib.txbio.*;
   import com.powersurgepub.psdatalib.tabdelim.*;
   import com.powersurgepub.psdatalib.psdata.*;
   import com.powersurgepub.psdatalib.template.*;
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.regcodes.*;
   import com.powersurgepub.twodue.data.*;
   import java.io.*;
   import java.text.*;
 
 /**
    A parent class for a Two Due disk file or folder.<p>
   
    This code is copyright (c) 2003 by Herb Bowie.
    All rights reserved. <p>
   
    Version History: <ul><li>
        </ul>
   
    @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
            herb@powersurgepub.com</a>)<br>
            of PowerSurge Publishing 
            (<a href="http://www.powersurgepub.com">
            www.powersurgepub.com</a>)
   
    @version 2003/12/21 - Originally written.
  */
 public abstract class TwoDueDiskStore {
   
   public static final String  FILE          = "file";
   public static final String  PRIMARY       = "primary";
   public static final String  TEMPLATE      = "template";
   public static final String  PUBLISH_WHEN  = "publishwhen";
   public static final String  START_TIME    = "starttime";
   public static final String  VIEW_INDEX    = "viewindex";
   public static final String  SYNC_FOLDER   = "syncfolder";
   public static final String  AUTO_SYNC     = "autosync";
   public static final String  DELETE_UNSYNCED = "deleteunsynced";
   public static final String  BACKUP_FOLDER = "backupfolder";
   
   public static final String  TO_DO_FILE_NAME = "twodue";
   
   public static final String  FILE_EXT      = "tdu";
   public static final String  FILE_EXT_OLD  = "txt";
   public static final String  NEW_EXT       = "tmp";
   public static final String  BACKUP_EXT    = "bak";
   public static final String  FILE_EXT_XML  = "xml";
   
   public static final String  XML_DOC_TAG   = "twodue";
   public static final String  HEADER_TAG    = "header";
   public static final String  ITEM_TAG      = "item";
   
   public static final String  SPLIT_TAGS_FILE_NAME = "twodue_split.tab";
   
   // public static final String  TWO_DUE_FILE_CREATOR = "2Due";
   // public static final String  TWO_DUE_FILE_CREATOR_HEX = "32447565";
   
   /** The location of this disk store. */
   protected File            file;
   
   /** Is this the primary disk store for the current user? */
   private boolean           primary = false;
   
   /** Should we forget about this file? */
   protected boolean         forgettable = false;
   
   /** The location of the file of To Do Items. */
   protected File            toDoFile;
   
   /** The tab-delimited file object. */
   protected TabDelimFile    toDoTDF;
   
   /** A temporary name for the output to do  file. */
   protected File            newToDoFile;
   
   /** The tab-delimited file object for a new output file. */
   protected TabDelimFile    newToDoTDF;
   
   /** The name used for the prior version of the to do file. */
   protected File            backupToDoFile;
   
   /** The name used for the to do file with one record for each tag. */
   protected File            splitTagsFile = null;
   
   /** The tab-delimited file object for a to do file with one rec per tag. */
   protected TabDelimFile    splitTagsTDF = null;
   
   /** The location of the XML file. */
   protected File            toDoFileXML = null;
   
   /** The XML equivalent of the output TDF file. */
   protected XMLRecordWriter toDoRecsXMLOut = null;
   
   /** The XML equivalent of the input TDF file. */
   protected XMLParser3      toDoRecsXMLIn = null;
   
   /** The location of the disk directory. */
   protected File            diskDirFile;
   
   /** The tab-delimited representation of the disk directory. */
   protected TabDelimFile    diskDirTDF;
   
   /** The location of the views stored on disk. */
   protected File            diskViewsFile;
   
   /** The tab-delimited representation of the list of views. */
   protected TabDelimFile    diskViewsTDF;
   
   /** Indicates whether this to do file has been opened. */
   protected boolean         fileNowOpen = false;
   
   /** Indicates whether the archive for this to do file is now open. */
   protected boolean         processingArchive = false;
   
   /** A Web Publishing template for these to do items. */
   protected File            templateFile;
   
   /** A companion archive file for closed items. */
   protected File            archiveFile;
   
   /** A temporary name for a new output archive file. */
   protected File            archiveNewFile;
   
   /** The name used for the prior version of the archive file. */
   protected File            archiveBackupFile;
   
   /** A companion archive tab-delimited file for closed items. */
   protected TabDelimFile    archiveTDF;
   
   /** A Web template file for publishing the archive file. */
   protected File            archiveTemplateFile;
   
   /** A Web template for publishing the archive file. */
   protected Template        archiveTemplate;
   
   /** The first or only Web page generated from the Archive publication. */
   protected File            archivePublishedPage;
   
   /** The last folder used to store a backup for this two due list. */
   protected File            backupFolder;
   
   /** A sub-folder for update logs. */
   protected File            updateLogFolder;
   
   protected Template        template;
   
   /** The selection and sequence settings for this file. */
   protected ItemComparator  comp = new ItemComparator();
   
   /** A pointer to the selection and sequence settings for this file. */
   protected int             viewIndex = 0;
   
   /** An indicator of how often the file should be published to the Web. */
   protected int             publishWhen = 0;
   
   public static final int     PUBLISH_ON_CLOSE = 1;
   public static final int     PUBLISH_ON_SAVE  = 2;
   
   /** The folder last used for the Sync Folder function. */
   protected String          syncFolder = "";
   
   protected boolean         autoSync = false;
   
   protected boolean         deleteUnsynced = false;
   
   /** Default start time for new items created within this data store. */
   protected TimeOfDay       defaultStartTime = new TimeOfDay();
   
   protected Logger          log = new Logger();
   
   /**
     Indicates whether this is an instance of TwoDueUnknown.
    
     @return true if this is an instance of TwoDueUnknown.
    */
   public abstract boolean isUnknown();
   
   /**
     Indicates whether this is an instance of TwoDueFile.
    
     @return true if this is an instance of TwoDueFile.
    */
   public abstract boolean isAFile();
   
   /**
     Indicates whether this is an instance of TwoDueFolder.
    
     @return true if this is an instance of TwoDueFolder.
    */
   public abstract boolean isAFolder();
   
   /**
     Returns a record definition for a TwoDue disk store, 
     in com.powersurgepub.psdata.RecordDefinition format.
    
     @return A record format definition for a Two Due disk store.
    */
   public static RecordDefinition getRecDef() {
     RecordDefinition recDef = new RecordDefinition();
     recDef.addColumn (FILE);
     recDef.addColumn (PRIMARY);
     ItemComparator.addColumnNames (recDef);
     recDef.addColumn (TEMPLATE);
     recDef.addColumn (PUBLISH_WHEN);
     recDef.addColumn (START_TIME);
     recDef.addColumn (VIEW_INDEX);
     recDef.addColumn (SYNC_FOLDER);
     recDef.addColumn (AUTO_SYNC);
     recDef.addColumn (DELETE_UNSYNCED);
     recDef.addColumn (BACKUP_FOLDER);
     return recDef;
   }
   
   /**
     Sets multiple fields based on contents of passed record.
   
     @param  storeRec  A data record containing multiple fields.
    */
   public void setMultiple (DataRecord storeRec) {
     
     // Indication of current primary disk file should not be assumed
     // based on last saved state of this file.
     // if (! isPrimary()) {
     //   setPrimary (storeRec.getFieldAsBoolean (PRIMARY));
     // }
 
     comp.setSortFields (
         storeRec.getFieldData (ItemComparator.SHOW),
         storeRec.getFieldData (ItemComparator.FIELD1),
         storeRec.getFieldData (ItemComparator.SEQ1),
         storeRec.getFieldData (ItemComparator.FIELD2),
         storeRec.getFieldData (ItemComparator.SEQ2),
         storeRec.getFieldData (ItemComparator.FIELD3),
         storeRec.getFieldData (ItemComparator.SEQ3),
         storeRec.getFieldData (ItemComparator.FIELD4),
         storeRec.getFieldData (ItemComparator.SEQ4),
         storeRec.getFieldData (ItemComparator.FIELD5),
         storeRec.getFieldData (ItemComparator.SEQ5),
         storeRec.getFieldData (ItemComparator.UNDATED)
       );
     String viewIndexString = storeRec.getFieldData (VIEW_INDEX);
     if (viewIndexString != null
         && viewIndexString.length() > 0) {
       setViewIndex (viewIndexString);
     }
     setTemplate (new File (storeRec.getFieldData (TEMPLATE)));
     setPublishWhen (Integer.parseInt (storeRec.getFieldData (PUBLISH_WHEN)));
     setDefaultStartTime (storeRec.getFieldData (START_TIME));
     setSyncFolder (storeRec.getFieldData (SYNC_FOLDER));
     setAutoSync (storeRec.getFieldData (AUTO_SYNC));
     setDeleteUnsynced (storeRec.getFieldData (DELETE_UNSYNCED));
     setBackupFolder (storeRec.getFieldData (BACKUP_FOLDER));
 
   } // end method
   
   /*
   public void update (TwoDueDiskStore store) {
     if (file.equals (store.getFile())) {
       setPrimary (store.isPrimary());
       setComparator (store.getComparator());
       setTemplate (store.getTemplate());
       setPublishWhen (store.getPublishWhen());
       setDefaultStartTime (store.getDefaultStartTimeAsString());
     }
   }
   */
   
   /**
     Return this object, formatted as a DataRecord.
    
     @param recDef Record Definition to be used in building the record. 
     */
   public DataRecord getDataRec (RecordDefinition recDef) {
     // Create a date formatter
     DataRecord nextRec = new DataRecord();
     nextRec.addField (recDef, getFile().toString());
     nextRec.addField (recDef, String.valueOf (isPrimary ()));
     nextRec.addField (recDef, comp.getSelectString());
     for (int i = 0; i < 5; i++) {
       nextRec.addField (recDef, comp.getSortField (i));
       nextRec.addField (recDef, comp.getSortSeq (i));
     }
     nextRec.addField (recDef, comp.getUndatedString());
     nextRec.addField (recDef, getTemplateString());
     nextRec.addField (recDef, String.valueOf (getPublishWhen ()));
     nextRec.addField (recDef, getDefaultStartTimeAsString());
     nextRec.addField (recDef, getViewIndexAsString());
     nextRec.addField (recDef, getSyncFolder());
     nextRec.addField (recDef, getAutoSyncAsString());
     nextRec.addField (recDef, getDeleteUnsyncedAsString());
     nextRec.addField (recDef, getBackupFolderAsString());
     return nextRec;
   }
   
   public File getFile() {
     return file;
   }
   
   public void setPrimary (String primary) {
     setPrimary (Boolean.getBoolean (primary));
   }
   
   public void setPrimary (boolean primary) {
     this.primary = primary;
   }
   
   public boolean isPrimary () {
     return primary;
   }
   
   public void setForgettable (boolean forgettable) {
     this.forgettable = forgettable;
   }
   
   public boolean isForgettable () {
     return forgettable;
   }
   
   /**
     Return an abbreviated path to the disk store, to visually
     identify it to the user. 
    
     @return A string identifying the disk store to the user. 
    */
   public abstract String getShortPath();
   
   /**
     Return full path to file or folder.
    
     @return Path to file or folder.
    */
   public String getPath () {
     if (isUnknown()) {
       return ("???");
     } else {
       return file.getPath();
     }
   }
   
   public boolean isToDoFileValidInput () {
     return (toDoFile != null
         && (! isUnknown())
         && toDoFile.exists()
         && toDoFile.isFile()
         && toDoFile.canRead());
   }
   
   public boolean isToDoFileValidOutput () {
     return (toDoFile != null
         && (! isUnknown()));
   }
   
   public boolean isInvalid () {
     return (! isValid());
   }
   
   public boolean isValid() {
     boolean ok = isAFolder();
     if (ok) {
       if (file.exists() && file.canRead()) {
         // still ok
       } else {
         ok = false;
       }
     }
     return ok;
   }
   
   /**
     Returns the file of To Do Items.
    
     @return The file of To Do Items.
    */
   public File getToDoFile() {
     return toDoFile;
   }
   
   /**
     Returns the tab-delimited file of To Do Items.
    
     @return The tab-delimited file of To Do Items.
    */
   public TabDelimFile getToDoTDF() {
     return toDoTDF;
   }
   
   public File getNewToDoFile() {
     if (toDoFile == null) {
       return null;
     }
     if (newToDoFile == null) {
       FileName fileName = new FileName (toDoFile);
       newToDoFile 
           = new File (fileName.getPath(), fileName.replaceExt (NEW_EXT));
     }
     return newToDoFile;
   }
   
   /**
     Returns the tab-delimited file for output To Do Items.
    
     @return The tab-delimited file for output To Do Items.
    */
   public TabDelimFile getNewToDoTDF() {
     File f = getNewToDoFile();
     if (newToDoTDF == null && f != null) {
       newToDoTDF = new TabDelimFile (f);
       newToDoTDF.setLog (log);
     } 
     return newToDoTDF;
   }
   
   /**
    Get the tab delimited file to contain one record per tag. 
   
    @return A tab delimited file with one record per tag. 
   */
   public TabDelimFile getSplitTagsTDF() {
     if (toDoFile == null) {
       return null;
     } else {
       splitTagsFile 
           = new File (toDoFile.getParentFile(), SPLIT_TAGS_FILE_NAME);
       splitTagsTDF = new TabDelimFile (splitTagsFile);
       splitTagsTDF.setLog (log);
       return splitTagsTDF;
     }
   }
   
   public File getBackupToDoFile() {
     if (toDoFile == null) {
       return null;
     }
     if (backupToDoFile == null) {
       FileName fileName = new FileName (toDoFile);
       backupToDoFile 
           = new File (fileName.getPath(), fileName.replaceExt (BACKUP_EXT));
     }
     return backupToDoFile;
   }
   
   public XMLRecordWriter getToDoRecsXML() {
     if (toDoFileXML == null) {
       return null;
     }
     if (toDoRecsXMLOut == null) {
       constructToDoRecsXML();
     }
     return toDoRecsXMLOut;
   }
   
   public boolean isDiskDirFileValidInput () {
     return (diskDirFile != null
         && (isAFolder())
         && diskDirFile.exists()
         && diskDirFile.isFile()
         && diskDirFile.canRead());
   }
   
   /**
     Returns the tab-delimited file of disk stores.
    
     @return The tab-delimited file of disk stores.
    */
   public TabDelimFile getDiskDirTDF() {
     return diskDirTDF;
   }
   
   public boolean isDiskViewsFileValidInput () {
     return (diskViewsFile != null
         && (isAFolder())
         && diskViewsFile.exists()
         && diskViewsFile.isFile()
         && diskViewsFile.canRead());
   }
   
   public void setBackupFolder(String backupFolderStr) {
     backupFolder = new File (backupFolderStr);
   }
   
   public void setBackupFolder(File backupFolder) {
     this.backupFolder = backupFolder;
   }
   
   public File getBackupFolder() {
     return backupFolder;
   }
   
   public String getBackupFolderAsString() {
     if (backupFolder != null) {
       return backupFolder.getPath();
     } else {
       return "";
     }
   }
   
   
   
   /**
     Returns the tab-delimited file of disk stores.
    
     @return The tab-delimited file of disk stores.
    */
   public TabDelimFile getDiskViewsTDF() {
     return diskViewsTDF;
   }
   
   public void setLog (Logger log) {
     this.log = log;
     if (toDoTDF != null) {
       toDoTDF.setLog (log);
     }
   }
   
   /**
    Populate the list of items from this disk store. 
   
    @param items The list of items to be populated. 
    @param dateFormatter The date formatter to be used for input data.
    @throws RegistrationException If the user is not registered, and the number
                                  of items exceeds the demo limit. 
    @throws IOException           If there are problems reading from disk. 
   */
   public void populate ( // IDListHeader header, 
       ToDoItems items, DateFormat dateFormatter) 
       throws RegistrationException, IOException {
     try {
       if (processingArchive) {
         items.addAll (dateFormatter, archiveTDF);
         Logger.getShared().recordEvent(LogEvent.NORMAL, 
             "Loaded Two Due data from Archive TDF file " + archiveTDF.getFileName(), 
             false);
       }
       else
       if (toDoFileXML != null
           && toDoFileXML.exists()
           && toDoFileXML.canRead()
           && toDoRecsXMLIn != null) {
         toDoRecsXMLIn.openForInput(getRecDef());
         DataRecord diskStoreRec = toDoRecsXMLIn.nextRecordIn();
         setMultiple(diskStoreRec);
         items.addAll (dateFormatter, toDoRecsXMLIn);
         Logger.getShared().recordEvent(LogEvent.NORMAL, 
             "Loaded Two Due data from XML file " + toDoFileXML.getPath(), 
             false); 
       } else {
         items.addAll (dateFormatter, toDoTDF);
         Logger.getShared().recordEvent(LogEvent.NORMAL, 
             "Loaded Two Due data from TDF file " + toDoTDF.getFileName(), 
             false);
       }
     } catch (RegistrationException regExc) {
       setFileNowOpen (true);
       throw regExc;
     }
     setFileNowOpen (true);
   }
   
   /**
    Populate the passed list of to do items from the passed file. 
   
    @param items The list of to do items to be populated. 
    @param dateFormatter The date formatter to use. 
    @param inFile The input file to use. 
    @throws RegistrationException If we tried to exceed the demo limit. 
    @throws IOException If we had an disk error. 
   */
   public void populate (ToDoItems items, DateFormat dateFormatter, File inFile)
       throws RegistrationException, IOException {
     if (inFile != null
         && inFile.exists()
         && inFile.canRead()) {
       FileName inFileName = new FileName(inFile);
       if (inFileName.getExt().equals(FILE_EXT_XML)) {
         // Populate from XML File
         XMLParser3 inXML = constructToDoRecsXMLIn (inFile);
         inXML.openForInput(getRecDef());
         DataRecord diskStoreRec = inXML.nextRecordIn();
         setMultiple(diskStoreRec);
         items.addAll (dateFormatter, inXML);
         Logger.getShared().recordEvent(LogEvent.NORMAL, 
             "Loaded Two Due data from XML file " + inFile.getPath(), 
             false); 
       } else {
         TabDelimFile inTDF = new TabDelimFile (inFile);
         inTDF.setLog (log);
         items.addAll (dateFormatter, inTDF);
         Logger.getShared().recordEvent(LogEvent.NORMAL, 
             "Loaded Two Due data from TDF file " + inTDF.getFileName(), 
             false);
       }
     } else {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Failed to populate Two Due data", false);
     }
   }
   
   public void backupRenameArchive () {
     archiveFile = backupRename (archiveFile);
     constructArchiveTDF();
   }
   
   public void save (ToDoItems items)
       throws RegistrationException, IOException {
     if (processingArchive) {
       items.getAll (archiveTDF);
     } else {
       items.getAll (getNewToDoTDF());
       toDoFile = backupRename (getToDoFile());
       /** Mac OS no longer uses type or creator codes. 
       XOS xos = XOS.getShared();
       boolean typeOK = xos.setFileTypeText (getToDoFile());
       try {
         int creator = Integer.parseInt (TWO_DUE_FILE_CREATOR_HEX, 16);
         boolean creatorOK = xos.setFileCreator (file, creator);
       } catch (NumberFormatException e) {
         System.out.println ("Exception caught while converting file creator code");
       } */
       constructToDoTDF();
       XMLRecordWriter xmlWriter = getToDoRecsXML();
       saveToXML(items, xmlWriter);
     }
     setFileNowOpen (true);
   }
   
   public void saveToXML (ToDoItems items, XMLRecordWriter xmlWriter) 
       throws RegistrationException,
              IOException {
     RecordDefinition headerDef = getRecDef();
     xmlWriter.openForOutput(headerDef);
     xmlWriter.setRecTag(HEADER_TAG);
     xmlWriter.nextRecordOut(getDataRec(headerDef));
     // System.out.println("TwoDueDiskStore.saveToXML entity encoding? " 
     //     + String.valueOf(xmlWriter.getXMLEntityEncoding())
     //     + " for " + xmlWriter.toString());
     // System.out.println("TwoDueDiskStore.saveToXML XML Entity Encoder: "
     //     + xmlWriter.getXMLEntityEncoder().toString());
     items.getAll(xmlWriter);
   }
   
   public void constructToDoTDF() {
     toDoTDF = new TabDelimFile (toDoFile);
     toDoTDF.setLog (log);
   }
   
   protected void constructToDoRecsXML() {
     toDoRecsXMLOut = constructToDoRecsXMLOut (toDoFileXML);
     toDoRecsXMLIn = constructToDoRecsXMLIn (toDoFileXML);
   }
   
   public XMLRecordWriter constructToDoRecsXMLOut (File file) {
     XMLRecordWriter xmlOut = new XMLRecordWriter (file);
     xmlOut.setDocTag(XML_DOC_TAG);
     xmlOut.setXmlEntityEncoding(true);
     return xmlOut;
   }
   
   public XMLParser3 constructToDoRecsXMLIn (File file) {
     XMLParser3 xmlIn = new XMLParser3(file);
     xmlIn.setXmlEntityDecoding(true);
     return xmlIn;
   }
   
   public void constructArchiveTDF() {
     archiveTDF = new TabDelimFile (archiveFile);
     archiveTDF.setLog (log);
   }
   
   /**
     Backs up the latest current copy of the file before renaming the new file
     to its permanent name. If currFile exists and contains some data, then
     it will be renamed to oldFile. The newFile will then be renamed to the
     currFile name. 
    
     @param currFile File containing latest data before save.
    */
   public static File backupRename (File currFile) {
     FileName fileName = new FileName (currFile);
     File newFile = new File (fileName.getPath(), fileName.replaceExt (NEW_EXT)); 
     File oldFile = new File (fileName.getPath(), fileName.replaceExt (BACKUP_EXT));
     File currFile2 = new File (fileName.getPath(), fileName.replaceExt (FILE_EXT));
     if (currFile.exists()
         && currFile.length() > 0) {
       if (oldFile.exists()) {
         oldFile.delete();
       }
       currFile.renameTo (oldFile);
     }
     newFile.renameTo (currFile2);
     return currFile2;
   }
   
   public boolean isFileNowOpen () {
     return fileNowOpen;
   }
   
   private void setFileNowOpen (boolean fileNowOpen) {
     this.fileNowOpen = fileNowOpen;
   }
   
   public void setProcessingArchive (boolean processingArchive) {
     this.processingArchive = processingArchive;
   }
   
   public boolean getProcessingArchive () {
     return processingArchive;
   }
   
   public boolean templateIsAvailable () {
     return (template != null);
   }
   
   public String getTemplateString () {
     if (templateFile == null) {
       return "";
     } else {
       return templateFile.toString();
     }
   }
   
   public File getTemplate () {
     return templateFile;
   }
   
   public void setTemplate (File templateFile) {
     this.templateFile = templateFile;
   }
   
   /**
     Uses the template file already identified to attempt to publish 
     the supplied data source as a Web page.
    
     @return File handle for the first, or only, Web page created.
    
     @param source The data to be published, using the template
                   previously identified. 
    */
   public File publish (DataSource source) 
       throws IOException {
     template = new Template (log);
     template.openTemplate (templateFile);
     template.openData (source, getShortPath());
     template.generateOutput();
     return new File(template.getTextFileOut().getDestination());
   }
   
   /*
   public boolean comparatorIsAvailable () {
     return (comp != null);
   }
    */
   
   public void resetComparator() {
     comp.setSortFields (ItemSelector.SHOW_ALL_STR,
         ToDoItem.COLUMN_DISPLAY [ToDoItem.STATUS],      ItemComparator.ASCENDING,
         ToDoItem.COLUMN_DISPLAY [ToDoItem.DUE_DATE],    ItemComparator.ASCENDING,
         ToDoItem.COLUMN_DISPLAY [ToDoItem.PRIORITY],    ItemComparator.ASCENDING,
         ToDoItem.COLUMN_DISPLAY [ToDoItem.ASSIGNED_TO], ItemComparator.ASCENDING,
         ToDoItem.COLUMN_DISPLAY [ToDoItem.SEQUENCE],    ItemComparator.ASCENDING,
         ItemComparator.LOW);
   }
   
   public void setCompareOption (String compareOption) {
     comp.set (compareOption);
   }
   
   public void setComparator (ItemComparator comp) {
     if (comp == null) {
       System.out.println("TwoDueDiskStore.setComparator to null ignored");
     } else {
       this.comp = comp;
     }
   }
   
   public ItemComparator getComparator () {
     return comp;
   }
   
   public String getCompareString () {
     return comp.toString();
   }
   
   public void setViewIndex (String viewIndexString) {
 
     try {
      int viewIndex = Integer.parseInt (viewIndexString);
      setViewIndex (viewIndex);
     } catch (NumberFormatException e) {
       System.out.println ("TwoDueDiskStore.setViewIndex NumberFormatException:");
       System.out.println ("  input = " + viewIndexString);
     }
   }
   
   /**
     Set the index pointing to the view to be used by this file. 
    
     @param viewIndex Index pointing to the view to be used by this file.
    */
   public void setViewIndex (int viewIndex) {
 
     this.viewIndex = viewIndex;
   }
   
   public String getViewIndexAsString () {
 
     return String.valueOf (getViewIndex());
   }
   
   /**
     Get the index pointing to the view to be used by this file.
    
     @return Index pointing to the view to be used by this file.
    */
   public int getViewIndex () {
 
     return viewIndex;
   }
   
   /*
   public void setComparator (ItemComparator comp) {
     this.comp = comp;
   }
    */
   
   /*
   public void setComparator (String bundle) {
     comp.set (bundle);
   }
    */
   
   public void setSelectOption (String selectOption) {
     comp.setSelectOption (selectOption);
   }
   
   public ItemSelector getSelector () {
     if (comp == null) {
       comp = new ItemComparator();
     } 
     return comp.getSelector();
 
   }
   
   public String getSelectString() {
     return comp.getSelectString();
   }
   
   public int getPublishWhen () {
     return publishWhen;
   }
   
   public void setPublishWhen (int publishWhen) {
     if (publishWhen >= 0
         && publishWhen <= 2) {
       this.publishWhen = publishWhen;
     }
   }
   
   public boolean isArchiveFileValidInput () {
     return (archiveFile != null
         && (isAFolder())
         && archiveFile.exists()
         && archiveFile.isFile()
         && archiveFile.canRead());
   }
   
   /**
     Indicates whether an Archive file is available for input or output. 
    
     @return True if an archive file is available, at least for output.
    */
   public boolean archiveIsAvailable () {
     return (archiveTDF != null);
   }
   
   public File getArchiveFile () {
     return archiveFile;
   }
   
   public File getArchiveNewFile () {
     return archiveNewFile;
   }
   
   public File getArchiveBackupFile () {
     return archiveBackupFile;
   }
   
   public TabDelimFile getArchiveTDF () {
     return archiveTDF;
   }
   
   public File getUpdateLogFolder () {
     return updateLogFolder;
   }
   
   public boolean archiveTemplateIsAvailable () {
     return (archiveTemplateFile != null
         && archiveTemplateFile.exists()
         && archiveTemplateFile.isFile()
         && archiveTemplateFile.canRead());
   }
   
   public File getArchiveTemplateFile () {
     return archiveTemplateFile;
   }
   
   /**
     Uses the template file already identified to attempt to publish 
     the archive as a Web page.
    
     @return File handle for the first, or only, Web page created.
    */
   public File publishArchive () 
       throws IOException {
     archivePublishedPage = null;
     if (archiveIsAvailable()
         && archiveTemplateIsAvailable()) {
       template = new Template (log);
       template.openTemplate (archiveTemplateFile);
       template.openData (archiveTDF, archiveFile.getName());
       template.generateOutput();
       archivePublishedPage 
           = new File(template.getTextFileOut().getDestination());
     } 
     return archivePublishedPage;
   }
   
   /**
     Return flag indicating whether a published archive Web page is available.
    
     @return True if published archive Web page is available, 
             False otherwise.
    */
   public boolean isArchivePublishedPageAvailable () {
     return (archivePublishedPage != null);
   }
   
   /**
     Return first or only page generated by Archive publication.
    
     @return First or only Web page generated by archive publication.
    */
   public File getArchivePublishedPage () {
     return archivePublishedPage;
   }
   
   /**
     Sets default start time.
    
     @param defaultStartTime Default start time for new items
    */
   public void setDefaultStartTime (String defaultStartTime) {
     this.defaultStartTime.set (defaultStartTime);
   }
   
   /**
     Gets the default start time for this data store. 
   
     @return Default start time for this data store.
    */
   public TimeOfDay getDefaultStartTime () {
     return defaultStartTime;
   }
 
   /**
     Gets the default start time for this data store. 
   
     @return Default start time for this data store  formatted as a string.
    */
   public String getDefaultStartTimeAsString () {
     return defaultStartTime.toString();
   }
   
   
   public void setSyncFolder (String syncFolder) {
     this.syncFolder = syncFolder;
   }
   
   public String getSyncFolder () {
     return syncFolder;
   }
   
   public void setAutoSync (boolean autoSync) {
     this.autoSync = autoSync;
   }
   
   public void setAutoSync (String autoSyncStr) {
     char a = 'y';
     if (autoSyncStr.length() > 0) {
       a = autoSyncStr.charAt(0);
     }
     autoSync = (a == 'y' || a == 'Y' || a == 't' || a == 'T');
   }
   
   public boolean getAutoSync() {
     return autoSync;
   }
   
   public String getAutoSyncAsString() {
     if (autoSync) {
       return "Yes";
     } else {
       return "No";
     }
   }
   
   public void setDeleteUnsynced (boolean deleteUnsynced) {
     this.deleteUnsynced = deleteUnsynced;
   }
   
   public void setDeleteUnsynced (String deleteUnsyncedStr) {
     char d = 'y';
     if (deleteUnsyncedStr.length() > 0) {
       d = deleteUnsyncedStr.charAt(0);
     }
     deleteUnsynced = (d == 'y' || d == 'Y' || d == 't' || d == 'T');
   }
   
   public boolean getDeleteUnsynced() {
     return deleteUnsynced;
   }
   
   public String getDeleteUnsyncedAsString() {
     if (deleteUnsynced) {
       return "Yes";
     } else {
       return "No";
     }
   }
   
   
   /**
     Two disk stores are considered equal if they point to the same
     location on disk.
    
     @return True if the two disk store instances point to the same file
             or folder as their data source.
    
    */
   public boolean equals (TwoDueDiskStore store2) {
     boolean same = (file.equals (store2.getFile()));
     
     return (file.equals (store2.getFile()));
   }
   
   /**
     This disk store is considered equal to the passed File if they both point 
     to the same location on disk.
    
     @return True if this disk store points
     to the same disk location as the passed File.
    
    */
   public boolean equals (File file2) {
     // System.out.println ("TwoDueDiskStore equals input " + file2.toString());
     // System.out.println ("  " + file.toString());
     // System.out.println ("  " + toDoFile.toString());
     // System.out.println ("    Equal? " + String.valueOf(file.equals (file2)
     //     || toDoFile.equals (file2)));
     return (file.equals (file2)
         || toDoFile.equals (file2));
   }
   
   public int compareTo (TwoDueDiskStore store2) {
     return file.compareTo (store2.getFile());
   }
   
   public String toString() {
     if (file == null) {
       return "??? (Unknown)";
     } else {
       return file.toString();
     }
   }
   
   public void display(String header) {
     System.out.println (header);
     System.out.println ("TwoDueDiskStore " + toString());
     if (isUnknown()) {
       System.out.println ("  Disk location unknown");
     }
     if (isAFile()) {
       System.out.println ("  Disk location is a single file");
     }
     if (isAFolder()) {
       System.out.println ("  Disk location is a folder");
       System.out.println ("  To Do File is " + toDoFile.toString());
       System.out.println ("  To Do TDF  is " + toDoTDF.toString());
       System.out.println ("  Archive File is " + archiveFile.toString());
       System.out.println ("  Archive TDF  is " + archiveTDF.toString());
     }
     // if (comparatorIsAvailable()) {
       // System.out.println ("  Comparator is " + comp.toString());
     // } else {
     //   System.out.println ("  No comparator defined");
     // }
   }
   
 }
