 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db.xmlutil;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.jbundle.base.db.BaseDatabase;
 import org.jbundle.base.db.BaseTable;
 import org.jbundle.base.db.KeyArea;
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.SQLParams;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.field.CounterField;
 import org.jbundle.base.field.DateTimeField;
 import org.jbundle.base.field.ObjectField;
 import org.jbundle.base.field.ReferenceField;
 import org.jbundle.base.field.StringField;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.Debug;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.thread.BaseProcess;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.model.main.db.DatabaseInfoModel;
 import org.jbundle.model.DBException;
 import org.jbundle.model.RecordOwnerParent;
 import org.jbundle.model.Task;
 import org.jbundle.model.util.Util;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.buff.VectorBuffer;
 import org.jbundle.thin.base.thread.AutoTask;
 import org.jbundle.thin.base.util.Application;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 
 /**
  * This utility is used to import and export to XML file(s).
  * The import utility is very flexible, including the capability to import multi-level
  * records including sub-records and fields referencing main records.
  * <p>This class also has the capability to be run alone as a utility for imports only.
  * If you want a standalone utility for exports, use the class "ExportRecordsToXMLProcess."
  * <pre>
  * The params to pass in are:
  * filename - the name of the target XML file to import.
  * import - the name of the target XML file to import.
  * export - the name of the target XML file to export.
  * overwritedups - true if you want to overwrite duplicate records in import.
  * </pre>
  */
 public class XmlInOut extends BaseProcess
 {
     /**
      * The default archive directory.
      */
     public static String DEFAULT_ARCHIVE_DIR = "data/archive";
     
     protected String archiveRoot = null;
     
     protected boolean m_bOverwriteDups = false;
 
     /**
      * This stand-alone method is to be used only when the bootstrap (menu) files need to be imported.
      * You probably want to comment out the check security line in Application. (= NORMAL_RETURN)
      * Typical usage:
      * java org.jbundle.base.db.xmlutil.XmlInOut allFiles=true loadInitialData=false \
      *  archiveDir=/home/don/workspace/workspace/tourapp/res/data/data/src/main/webapp/com/tourapp/res/data/initial_data/current_initial_data/
      */
     public static void main(String[] args)
     {
         String[] argsDefault = {};
 //        String[] argsDefault = {"local=Jdbc", "remote=Jdbc", "table=Jdbc", "databaseproduct=cloudscape"};
         if ((args == null) || (args.length == 0))
             args = argsDefault;
         Map<String,Object> properties = new Hashtable<String,Object>();
         Util.parseArgs(properties, args);
         if (properties.get(DBParams.LOCAL) == null)
             properties.put(DBParams.LOCAL, DBParams.JDBC);
         if (properties.get(DBParams.REMOTE) == null)
             properties.put(DBParams.REMOTE, DBParams.JDBC);
         if (properties.get(DBParams.TABLE) == null)
             properties.put(DBParams.TABLE, DBParams.JDBC);
         String archiveRoot = (String)properties.get("archiveDir");
         if (archiveRoot == null)
         	archiveRoot = "./" + DEFAULT_ARCHIVE_DIR;
         String recordClass = "org.jbundle.main.user.db.UserGroup";
         if (properties.get(DBParams.RECORD) != null)
             recordClass = properties.get(DBParams.RECORD).toString();
 
         Environment env = new Environment(properties);
         Application app = new MainApplication(env, properties, null);
         Task task = new AutoTask(app, null, null);
         XmlInOut test = new XmlInOut(task, null, null);
         if ((archiveRoot.endsWith("/")) || (archiveRoot.endsWith(File.separator)))
             archiveRoot = archiveRoot.substring(0, archiveRoot.length() - 1);
         test.archiveRoot = archiveRoot;
         boolean bAllFiles = false;
         if (DBConstants.TRUE.equalsIgnoreCase((String)properties.get("allFiles")))
             bAllFiles = true;
         if (bAllFiles)
             test.importArchives(archiveRoot, null);    // Import the entire archive
         else
         {
             Record record = Record.makeRecordFromClassName(recordClass, test);
             String archiveFile = "/main_user/org/jbundle/main/user/db/UserGroup.xml";
             if (properties.get("archiveFile") != null)
             	archiveFile = properties.get("archiveFile").toString();
             if (!test.importXML(record.getTable(), archiveRoot + archiveFile, null))     // Import the menu file.
                 System.exit(1);
 //            Record record = new org.jbundle.main.db.Menus(recordOwner);
 //            test.importXML(record, "archive/main/db/Menus.xml");     // Import the menu file.
             record.free();
         }
         test.free();
         test = null;
         Environment.getEnvironment(null).free();
         System.exit(0);
     }
     /**
      * Initialization.
      */
     public XmlInOut()
     {
         super();
     }
     /**
      * Initialization.
      * @param taskParent Optional task param used to get parent's properties, etc.
      * @param recordMain Optional main record.
      * @param properties Optional properties object (note you can add properties later).
      */
     public XmlInOut(RecordOwnerParent taskParent, Record recordMain, Map<String,Object> properties)
     {
         this();
         this.init(taskParent, recordMain, properties);
     }
     /**
      * Initialization.
      * @param taskParent Optional task param used to get parent's properties, etc.
      * @param recordMain Optional main record.
      * @param properties Optional properties object (note you can add properties later).
      */
     public void init(RecordOwnerParent taskParent, Record recordMain, Map<String,Object> properties)
     {
         super.init(taskParent, recordMain, properties);
     }
     /**
      * Free this object.
      */
     public void free()
     {
         super.free();
     }
     /**
      * Run this process given the parameters passed in on initialization.
      */
     public void run()
     {
         Record record = this.getMainRecord();
         String strFileName = this.getProperty("filename");
         String strImport = this.getProperty("import");
         if (strFileName == null)
             strFileName = strImport;
         String strExport = this.getProperty("export");
         if (strFileName == null)
             strFileName = strExport;
         m_bOverwriteDups = false;
         if ((DBConstants.TRUE.equalsIgnoreCase(this.getProperty("overwritedups")))
             || (DBConstants.YES.equalsIgnoreCase(this.getProperty("overwritedups"))))
                 m_bOverwriteDups = true;
         boolean bSuccess = false;
         if (strImport != null)
             bSuccess = this.importXML(record.getTable(), strFileName, null);
         else
             bSuccess = this.exportXML(record.getTable(), strFileName);
         if (!bSuccess)
             System.exit(1);
     }
     /**
      * Export this table.
      * @record The record to export.
      * @strFileName The destination filename (deleted the old copy if this file exists).
      */
     public boolean exportXML(BaseTable table, String strFileName)
     {
     	Record record = table.getRecord();
         boolean bSuccess = true;
         File file = new File(strFileName);
         if (file.exists())
             file.delete();      // Delete if it exists
         else
         {
             String strPath = file.getParent();
             File fileDir = new File(strPath);
             fileDir.mkdirs();
         }
 
         XmlInOut.enableAllBehaviors(record, false, true); // Disable file behaviors
 
         Document doc = XmlUtilities.exportFileToDoc(table);
         
         try   {
             OutputStream fileout = new FileOutputStream(strFileName);
             Writer out = new OutputStreamWriter(fileout, XmlUtilities.XML_ENCODING);   //, MIME2Java.convert("UTF-8"));
 
             Utility.convertDOMToXML(doc, out);
 
             out.close();
             fileout.close();
         } catch (Exception ex)  {
             ex.printStackTrace();
             bSuccess = false;
         }
         XmlInOut.enableAllBehaviors(record, true, true);
         return bSuccess;
     }
     /**
      * Import all the archive files.
      * @param strArchiveDir The root archive directory to import (if null, use the fileDir).
      * @param fileDir The root directory to import archives from (if null, use the strArchive string).
      */
     public void importArchives(String strArchiveDir, File fileDir)
     {
         if (fileDir == null)
         {
             if (!strArchiveDir.contains(","))
                 fileDir = new File(strArchiveDir);
             else
             {   // Comma delimited file list
                 StringTokenizer st = new StringTokenizer(strArchiveDir, ", \t\n\r\f");
                 while (st.hasMoreTokens()) {
                     archiveRoot = st.nextToken();
                     archiveRoot = archiveRoot.replace(" ", "");
                     if (archiveRoot.endsWith(File.separator))
                         archiveRoot = archiveRoot.substring(0, archiveRoot.length() - 1);
                     this.importArchives(archiveRoot, null);
                 }
                 return;
             }
         }
         else
             strArchiveDir = fileDir.getName();
         Utility.getLogger().info(strArchiveDir);
         File[] fileList = fileDir.listFiles();
         for (int i = 0; i < fileList.length; i++)
         {
             if (fileList[i].isDirectory())
             {
                 this.importArchives(strArchiveDir, fileList[i]);
             }
             else
             {
                 this.importThisArchive(fileList[i]);
             }
         }
     }
     /**
      * Import this archive file.
      * @param strDatabaseName The name of the database this file is in.
      * @param file The XML file to import.
      */
     public void importThisArchive(File file)
     {
         String filename = file.getName();
         int iIndex = filename.lastIndexOf('.');
         if ((iIndex == -1)
             || (!filename.substring(iIndex).equalsIgnoreCase(".XML")))
                 return;   // This is not an XML file
         filename = filename.substring(0, iIndex);
         Record record = null;
         String className = file.getPath();
         className = className.substring(0, className.lastIndexOf('.'));
         int iStartClass = className.toUpperCase().indexOf(archiveRoot.toUpperCase()) + archiveRoot.length() + 1;
         className = className.substring(iStartClass);
         char chFileSeparator = System.getProperty("file.separator").charAt(0);
         className = className.replace(chFileSeparator, '.');
         String databaseName = className.substring(0, className.indexOf('.'));
         className = className.substring(className.indexOf('.') + 1);
         //x strClassName = Constants.ROOT_PACKAGE + strClassName;
         String strRecordClassName = className.substring(className.lastIndexOf('.') + 1);
         int iDatabaseType = DBConstants.SHARED_DATA;
         if ((databaseName != null) && (databaseName.length() > 0))
         {
             if (databaseName.endsWith(BaseDatabase.USER_SUFFIX))
             {
                 databaseName = databaseName.substring(0, databaseName.length() - BaseDatabase.USER_SUFFIX.length());
                 iDatabaseType = DBConstants.USER_DATA;
             }
             else if (databaseName.endsWith(BaseDatabase.SHARED_SUFFIX))
                 databaseName = databaseName.substring(0, databaseName.length() - BaseDatabase.SHARED_SUFFIX.length());
         }
         if (!"DatabaseInfo".equalsIgnoreCase(strRecordClassName))
         {
         	BaseDatabase database = null;
             if ((databaseName != null) && (databaseName.length() > 0))
             	if (databaseName.contains("_"))	// Alternate database name
             		database = (BaseDatabase)((BaseApplication)this.getTask().getApplication()).getDatabase(databaseName, iDatabaseType, null);
             if (database == null)
             	record = Record.makeRecordFromClassName(className, this);
             else
             {
                 record = Record.makeRecordFromClassName(className, this, false, false);
                 /*BaseTable table =*/ database.makeTable(record);	// Force a table from this alternate database
                 record.init(this);
             }
         }
         else
         {
             className = DatabaseInfoModel.THICK_CLASS;
             record = Record.makeRecordFromClassName(className, this, false, true);
             ((DatabaseInfoModel)record).setDatabaseName(databaseName);
             record.init(this);
         } 
         if (record == null)
         {
         	if (Boolean.TRUE.toString().equalsIgnoreCase(this.getProperty("SkipIfNoRecord")))
         	{
         		System.out.println("Skipping record " + filename);
         		return;
         	}
             record = new XmlRecord();
             ((XmlRecord)record).setTableName(filename);
             ((XmlRecord)record).setDatabaseName(databaseName);
             ((XmlRecord)record).setDatabaseType(DBConstants.REMOTE);
             record.init(this);
         }
 //if (record.getDatabaseType() != DBConstants.TABLE)
 //    return;
         databaseName = record.getDatabaseName();
         Utility.getLogger().info("Record: " + filename + "  Database: " + databaseName);// + "  Path: " + fileDir.getPath());
         if (!this.importXML(record.getTable(), file.getPath(), null))
             System.exit(1);
         record.free();
     }
     /**
      * Parses this XML text and place it in new records.
      * @param record The record to place this parsed XML into (If this is null, the file name is supplied in the XML).
      * @param filename The XML file to parse.
      */
     public boolean importXML(BaseTable table, String filename, InputStream inStream)
     {
     	Record record = table.getRecord();
         XmlInOut.enableAllBehaviors(record, false, false);
         DocumentBuilder db = Utility.getDocumentBuilder();
         DocumentBuilder stringdb = Utility.getDocumentBuilder();
         // Parse the input file
         Document doc = null;
         try {
             synchronized (db)
             {
                 if (inStream != null)
                     doc = db.parse(inStream);
                 else
                     doc = db.parse(new File(filename));
             }
         } catch (FileNotFoundException e) {
         	Utility.getLogger().warning("Table input file not found: " + filename);
             return false;   // Error
         } catch (SAXException e) {
             Debug.print(e);
             return false;   // Error
         } catch (IOException e) {
             Debug.print(e);
             return false;   // Error
         } catch (Exception e) {
             Debug.print(e);
             return false;   // Error
         }
 
         try {
             String databaseName = null;
             int iEndDB = filename.lastIndexOf(System.getProperty("file.separator", "/"));
             if (iEndDB == -1)
                 iEndDB = filename.lastIndexOf('/');
             if (iEndDB != -1)
             {
                 int iStartDB = filename.lastIndexOf(System.getProperty("file.separator", "/"), iEndDB - 1);
                 if (iStartDB == -1)
                     iStartDB = filename.lastIndexOf('/', iEndDB - 1);
                 iStartDB++;     // Start
                 databaseName = filename.substring(iStartDB, iEndDB);
             }
 
             if (record == null)
                 this.parseXSL();    // Parse the XSL/DTD and create the tables and structures
 
             Element elParent = doc.getDocumentElement();
             RecordParser parser = new RecordParser(elParent);
             while (parser.next(stringdb) != null)
             {
                 Node elChild = parser.getChildNode();
                 String strName = parser.getName();
                 this.parseRecord(stringdb, (Element)elChild, strName, databaseName, table, null, false);
             }
         } catch (Throwable t) {
             t.printStackTrace();
             return false;   // Error
         }
         XmlInOut.enableAllBehaviors(record, true, true);
         return true;    // Success
     }
     /**
      * Find or create this record and then add these elements.
      * Note: If the MainHasReferenceField is true, you must return the bookmark of this new record.
      * @param stringdb The documentbuilder (so you have access to an XML string parser).
      * @param elParent The (file) parent element to read thru.
      * @param strTagName The tag name of this (file) element (ie., the file name).
      * @param strDatabase The name of the current database.
      * @param record The record to add these record elements to (if null, I will create this record).
      * @param recMain The main record if I am parsing a sub-file referencing another file's field element.
      * @param bIsMainHasReferenceField The main record doesn't have a reference to my main field.
      * @return This new record.
      */
     public Record parseRecord(DocumentBuilder stringdb, Element elParent, String strTagName, String strDatabase, BaseTable table, BaseTable tableMain, boolean bIsMainHasReferenceField)
     {
     	Record record = null;
     	if (table != null)
     		record = table.getRecord();
         if (record == null)
         {
             record = this.getRecord(strTagName);
 	        if (record == null)
 	        {   // Not found, create it!
 	            record = new XmlRecord();
 	            ((XmlRecord)record).setTableName(strTagName);
 	            if (strDatabase != null)
 	                ((XmlRecord)record).setDatabaseName(strDatabase);
 	            ((XmlRecord)record).setDatabaseType(DBConstants.REMOTE);
 	            record.init(this);
 	            Record recMain = null;
 	            if (tableMain != null)
 	            	recMain = tableMain.getRecord();
 	            if (recMain != null)    // If this is a sub-record
 	                if (!bIsMainHasReferenceField)  // And the main record doesn't have a reference to my key
 	            {
 	                ReferenceField fld = new ReferenceField(record, recMain.getTableNames(false) + "ID", DBConstants.DEFAULT_FIELD_LENGTH, null, null);
 	                KeyArea keyArea = record.makeIndex(DBConstants.NOT_UNIQUE, recMain.getTableNames(false) + "ID");
 	                keyArea.addKeyField(fld, DBConstants.ASCENDING);
 	            }
 	            RecordParser parser = new RecordParser(elParent);
 	            while (parser.next(stringdb) != null)
 	            {   // First, add the fields to the record
 	                parser.findField(record, true);     // This will create if not found
 	            }
 	        }
 	        table = record.getTable();
         }
         // Now its time to move the data to the fields and write the record.
         RecordParser parser = new RecordParser(elParent);
         boolean bFirstTime = true;
         while (parser.next(stringdb) != null)
         {
             Node elChild = parser.getChildNode();
             String strName = parser.getName();
             String strValue = parser.getValue();
             boolean bIsRecord = parser.isRecord();
             if (bFirstTime)
             {   // First time thru
                 bFirstTime = false;
                 try   {
                     table.addNew();
     	            Record recMain = null;
     	            if (tableMain != null)
     	            	recMain = tableMain.getRecord();
                     if (recMain != null)
                     {   // If this is a sub-file, be sure to update the reference field.
                         BaseField field = record.getField(recMain.getTableNames(false) + "ID");
                         if (field != null)
                             if (field instanceof ReferenceField)
                                 if (!bIsMainHasReferenceField)
                         {   // If the main record has to get me, but doesn't have my key, save it's key
                             if (recMain.getEditMode() == DBConstants.EDIT_ADD)
                             {   // Main record not added yet, add it now
                                 recMain = this.updateRecord(tableMain, true);   // And return the record written
                                 tableMain.edit();
                             }
                             ((ReferenceField)field).setReference(recMain);  // Reference the main record.
                         }
                     }
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 }
             }
             BaseField field = parser.findField(record, true);
             if (bIsRecord)
             {   // If this field directly references a record, you have to write the sub-record first.
                 boolean bIsReferenceField = (parser.getReferenceName() != null);
                 Record recNew = this.parseRecord(stringdb, (Element)elChild, strName, strDatabase, null, table, bIsReferenceField);
                 if (bIsReferenceField) if (recNew != null)
                 {
                     ((ReferenceField)field).setReference(recNew);
                     field = null; // Don't set it twice
                 }
             }
             if (field != null)
             {   // Set the data
                 if (field instanceof ObjectField)
                    XmlUtilities.decodeFieldData(field, strValue);
                 else if (field instanceof DateTimeField)
                     XmlUtilities.decodeDateTime((DateTimeField)field, strValue);
                 else
                     field.setString(strValue);
             }
         }
 
         record = this.updateRecord(table, bIsMainHasReferenceField);
         return record;
     }
     /**
      * Update this record.
      * Note: This is complicated.
      * @param record The record to update.
      * @param bIsMainHasReferenceField The main record doesn't have a reference to my main field.
      * @return The updated record (re-reads the record).
      */
     public Record updateRecord(BaseTable table, boolean bIsMainHasReferenceField)
     {
     	Record record = table.getRecord();
         Object objCounterData = null;
         if (record != null)
         {   // Field updating done, write the record to disk
             boolean bAutoSeqEnabled = true;
             try   {
                 if (record.getEditMode() == DBConstants.EDIT_ADD)
                 {
                     if (record.getCounterField() != null) if (((CounterField)record.getCounterField()).isModified())
                     {
                         bAutoSeqEnabled = false;
                         record.getTable().setProperty(SQLParams.AUTO_SEQUENCE_ENABLED, DBConstants.FALSE);        // Disable autosequence
                         objCounterData = record.getCounterField().getData();
                     }
                     try   {
                         table.add(record);
                     } catch (DBException ex)    {
                         if ((ex.getErrorCode() == DBConstants.DUPLICATE_KEY)
                             && (m_bOverwriteDups)
                                 && ((record.getCounterField() == null) || (!bAutoSeqEnabled)))
                         {
                             BaseBuffer buff = new VectorBuffer(new Vector<Object>()); // pend(don) ouch, serious performance hit.
                             buff.fieldsToBuffer(record);
                             record.setKeyArea(DBConstants.MAIN_KEY_AREA);
                             if (table.seek("="))
                             {   // Always
                             	table.remove();
                             	table.addNew();
                                 buff.bufferToFields(record, DBConstants.DONT_DISPLAY, DBConstants.READ_MOVE);
                                 table.add(record);
                             }
                             buff.free();
                         }
                         else
                         	ex.printStackTrace();
                     }
                     if (bIsMainHasReferenceField) if (bAutoSeqEnabled)
                     {   // Need to return the new record
                         table.setHandle(table.getLastModified(DBConstants.DATA_SOURCE_HANDLE), DBConstants.DATA_SOURCE_HANDLE); // Get this record, so I can get the bookmark
                         bIsMainHasReferenceField = false; // So seek won't be done at end
                     }
                 }
                 else
                 	table.set(record);
             } catch (DBException ex)    {
                 if (!bIsMainHasReferenceField)
                     ex.printStackTrace();
             } finally {
                 if (record != null) if (!bAutoSeqEnabled)
                     record.getTable().setProperty(SQLParams.AUTO_SEQUENCE_ENABLED, DBConstants.TRUE);     // Re-enable autosequence
             }
         }
         if (bIsMainHasReferenceField)
         {
             try   {
                 if (objCounterData != null)
                     record.getCounterField().setData(objCounterData);
                 table.seek("="); // Get this record, so I can get the bookmark
             } catch (DBException ex)    {
                 ex.printStackTrace();
             }
         }
         return record;
     }
     /**
      * Enable or disable all the behaviors.
      * @param record The target record.
      * @param bEnableRecordBehaviors Enable/disable all the record behaviors.
      * @param bEnableFieldBehaviors Enable/disable all the field behaviors.
      */
     public static void enableAllBehaviors(Record record, boolean bEnableRecordBehaviors, boolean bEnableFieldBehaviors)
     {
         if (record == null)
             return;
         record.setEnableListeners(bEnableRecordBehaviors);   // Disable all file behaviors
         for (int iFieldSeq = 0; iFieldSeq < record.getFieldCount(); iFieldSeq++)
         {
             BaseField field = record.getField(iFieldSeq);
             field.setEnableListeners(bEnableFieldBehaviors);
         }
     }
     /**
      * Parse the XSL and create the records.
      * @return false means there was no XSL/DTD
      */
     public boolean parseXSL()
     {
         return true;
     }
     /**
      * Encapsulate the function of reading through an XML file dom element.
      * Usually, you use this class by creating a RecordParser around the record element and
      * then call next to change the child pointer to the next record in the XML dom object.
      * <pre>
      * RecordParser parser = new RecordParser(elParent);
      * while (parser.next(stringdb) != null)
      * {
      *    Node elChild = parser.getChildNode();
      *  String strName = parser.getName();
      *  this.parseRecord(stringdb, (Element)elChild, strName, strDatabase, record, null, false);
      *  }
      * </pre>
      */
     protected class RecordParser extends Object
     {
         /**
          * The file element parent.
          */
         protected Element m_elParent = null;
         /**
          * The name of the current field element.
          */
         protected String m_strName = null;
         /**
          * The value of the current field element.
          */
         protected String m_strValue = null;
         /**
          * The node of the current field element.
          */
         protected Node m_elChild = null;
         /**
          * The current (logical) field in the record.
          */
         protected int m_iElementCount = 0;
         /**
          * The current physical element in the parent node (includes white-space elements).
          */
         protected int m_irec = 0;
 
         /**
          * Constructor.
          * @param elParent The file (parent) element.
          */
         public RecordParser(Element elParent)
         {
             super();
             m_elParent = elParent;
             m_iElementCount = 0;
             m_irec = 0;
         }
         /**
          * Get the name of the current field element.
          * @return The name.
          */
         public String getName()
         {
             return m_strName;
         }
         /**
          * Get the value of the current field element.
          * @return The value of this element.
          */
         public String getValue()
         {
             return m_strValue;
         }
         /**
          * Get the child node of the current field element.
          * @return The child node.
          */
         public Node getChildNode()
         {
             return m_elChild;
         }
         /**
          * Find the current field in this record.
          * (and optionally create the field and add to the record if it wasn't found.
          * @param record The record to find the field for this tag.
          * @param bCreateIfNotFound If not found, add a StringField with this name to the record.
          * @return The field for this tag (or null if not found).
          */
         public BaseField findField(Record record, boolean bCreateIfNotFound)    //Node elChild, Record record, Record recMain, String strTagName, String strData, int iElementCount, int iParseType)
         {
             boolean bIsRecord = this.isRecord();
             String strTagName = this.getName();
             BaseField field = record.getField(strTagName);
             if (field == null)
                 if (bCreateIfNotFound)
                     if (record instanceof XmlRecord)
             {
                 if (m_elChild.getNodeType() == Node.ATTRIBUTE_NODE)
                 {
                     field = (BaseField)record.getCounterField();
                     field.setFieldName(strTagName);     // Now I know the counter's field name.
                 }
                 if (field == null) if (!bIsRecord)
                     field = new StringField(record, strTagName, 32000, null, null);
             }
             if (field == null)
                 if (bIsRecord)
                     if (this.getReferenceName() != null)
             {
                 strTagName = this.getReferenceName();
                 field = record.getField(strTagName);
                 if (field == null)
                     field = new ReferenceField(record, strTagName, DBConstants.DEFAULT_FIELD_LENGTH, null, null);
             }
             return field;
         }
         /**
          * Move to the next field element in this document and return this if not eof.
          * @param stringdb The document builder (in case you need to parse an XML field.
          * @return this if a next element or attribute was moved to (null if EOF).
          */
         public RecordParser next(DocumentBuilder stringdb)
         {
             if (m_iElementCount == 0)
             {   // First check for an attribute (only on the first element)
                 NamedNodeMap attrList = m_elParent.getAttributes();
                 if (attrList != null)
                 {
                     for (int irec = 0; irec < attrList.getLength(); irec++)
                     {
                         Node elChild = attrList.item(irec);
                         if (elChild instanceof Attr)
                         {
                             m_strName = ((Attr)elChild).getName();
                             m_strValue = ((Attr)elChild).getValue();
                             m_iElementCount++;
                             m_elChild = elChild;
                             return this;
                         }
                     }
                 }
             }
             while (m_irec < m_elParent.getChildNodes().getLength())
             {
                 Node elChild = m_elParent.getChildNodes().item(m_irec);
                 m_irec++;
                 if (elChild instanceof Element)
                 {
                     m_strName = ((Element)elChild).getTagName();
                     
                     m_iElementCount++;
 
                     m_strValue = null;
                     if (!this.isXMLElement((Element)elChild))
                     {
                         Text text = (Text)elChild.getFirstChild();
                         if (text != null)
                             m_strValue = text.getData();
                     }
                     else
                     {
                         m_strValue = null;
                         try   {
                             synchronized (stringdb)
                             {
                                 Document fieldDoc = stringdb.newDocument();
                                 Node node = null;
                                 node = elChild;
     /*
                                 NodeList nodeList = elChild.getChildNodes();
                                 for (int i = 0; i < nodeList.getLength(); i++)
                                 {
                                     if (nodeList.item(i) instanceof Element)
                                     {
                                         node = nodeList.item(i);
                                         break;
                                     }
                                 }
     */
                                 if (node != null)
                                 {
                                     node = fieldDoc.importNode(node, true);
                                     fieldDoc.appendChild(node);
                                     StringWriter out = new StringWriter();  //, MIME2Java.convert("UTF-8"));
                                     try {
                                         // Use a Transformer for output
                                         TransformerFactory tFactory = TransformerFactory.newInstance();
                                         Transformer transformer = tFactory.newTransformer();
     
                                         DOMSource source = new DOMSource(fieldDoc);
                                         StreamResult result = new StreamResult(out);
                                         transformer.transform(source, result);
                                     } catch (TransformerConfigurationException tce) {
                                         // Error generated by the parser
                                         tce.printStackTrace();
                                     } catch (TransformerException te) {
                                         // Error generated by the parser
                                         te.printStackTrace();
                                     }
     
                                     out.close();
                                     m_strValue = out.toString();
                                     if ((m_strValue.length() > 5)
                                         && (m_strValue.substring(0, 5).equalsIgnoreCase(XmlUtilities.XML_LEAD_LINE.substring(0, 5))))
                                     { // Always
                                         int iStartIndex = m_strValue.indexOf('>');
                                         if (iStartIndex != -1) if (iStartIndex < m_strValue.length())
                                             iStartIndex = m_strValue.indexOf('>', iStartIndex + 1); // <XYZfield type="XML">
                                         if (iStartIndex != -1)
                                             iStartIndex++;
                                         if (iStartIndex != -1) if (iStartIndex < m_strValue.length()) if (m_strValue.charAt(iStartIndex) == '\n')
                                             iStartIndex++;
                                         if (iStartIndex != -1) if (iStartIndex < m_strValue.length()) if (m_strValue.charAt(iStartIndex) == '\r')
                                             iStartIndex++;
                                         if (iStartIndex != -1) if (iStartIndex < m_strValue.length()) if (m_strValue.charAt(iStartIndex) == '\n')
                                             iStartIndex++;
                                         int iLastIndex = m_strValue.lastIndexOf('<');
                                         if (iLastIndex != -1)
                                             iLastIndex--;
                                         if (iLastIndex > 0) if (m_strValue.charAt(iLastIndex) == '\n')
                                             iLastIndex--;
                                         if (iLastIndex > 0) if (m_strValue.charAt(iLastIndex) == '\r')
                                             iLastIndex--;
                                         if (iLastIndex > 0) if (m_strValue.charAt(iLastIndex) == '\n')
                                             iLastIndex--;
                                         if (iStartIndex != -1) if (iLastIndex + 1 >= iStartIndex) 
                                             m_strValue = m_strValue.substring(iStartIndex, iLastIndex + 1);
                                     }
                                 }
                             }
                         } catch (Exception ex)  {
                             ex.printStackTrace();
                         }
                     }
                     m_iElementCount++;
                     m_elChild = elChild;
                     return this;
                 }
             }
             return null;    // EOF
         }
         /**
          * Is this a record?
          * If the first child has children, then this is a record.
          * This is typically used to create a sub-file and connect it to this field.
          * @return true if this element is a record element.
          */
         public boolean isRecord()
         {
             if (m_elChild instanceof Element)
             {
                 if (this.isXMLElement((Element)m_elChild))
                     return false;
                 for (int irec = 0; irec < ((Element)m_elChild).getChildNodes().getLength() ; irec++)
                 {
                     Node elChild = ((Element)m_elChild).getChildNodes().item(irec);
                     if (elChild instanceof Element)
                         return true;
                 }
             }
             return false;
         }
         /**
          * Is this element an XML Type?
          * Is this element a field which contains XML.
          * You can tell by looking for the type=XML attribute.
          * @param elChild The element to check.
          * @return true if this is an XML element.
          */
         public boolean isXMLElement(Element elChild)
         {
             NamedNodeMap attrList = elChild.getAttributes();
             if (attrList != null)
             {
                 for (int iAttr = 0; iAttr < attrList.getLength() ; iAttr++)
                 {
                     Node elAttr = attrList.item(iAttr);
                     if (elAttr instanceof Attr)
                     {
                         if (XmlUtilities.TYPE_PARAM.equalsIgnoreCase(((Attr)elAttr).getName()))
                             if (XmlUtilities.XML_VALUE.equalsIgnoreCase(((Attr)elAttr).getValue()))
                                 return true;    // Children are an XML tree, skip
                     }
                 }
             }
             return false; // No
         }
         /**
          * Is this a reference to a record?
          * If this element has an attribute, then it is a reference field.
          * @return The attribute name of this element (or null if none).
          */
         public String getReferenceName()
         {
             if (m_elChild instanceof Element)
             {
                 NamedNodeMap attrList = ((Element)m_elChild).getAttributes();
                 if (attrList == null)
                     return null;
                 for (int irec = 0; irec < attrList.getLength() ; irec++)
                 {
                     Node elChild = attrList.item(irec);
                     if (elChild instanceof Attr)
                         return ((Attr)elChild).getName();
                 }
             }
             return null;
         }
     }
 }
