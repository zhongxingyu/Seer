 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db;
 
 /**
  * @(#)BaseTable.java 1.16 95/12/14 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import java.io.InputStream;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import org.jbundle.base.db.event.FileListener;
 import org.jbundle.base.db.grid.DataRecord;
 import org.jbundle.base.db.xmlutil.XmlInOut;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.field.BaseListener;
 import org.jbundle.base.field.CounterField;
 import org.jbundle.base.field.event.FieldListener;
 import org.jbundle.base.message.record.RecordMessageConstants;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.RecordOwner;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.Environment;
 import org.jbundle.model.DBException;
 import org.jbundle.model.db.Field;
 import org.jbundle.model.db.Rec;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.Converter;
 import org.jbundle.thin.base.db.FieldInfo;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.FieldTable;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.buff.VectorBuffer;
 import org.jbundle.thin.base.db.buff.str.StrBuffer;
 
 
 /**
  * Contains all the records in this table.
  * <p>(ie., If the base record is of the "Animal" class,
  * this table contains all the "Animal" records; "Cat"s,
  * "Dog"s, etc.)<p>
  * BaseTable - Used for navigation (one for each class).<p>
  *  <b>I Attempt to conform to the ListIterator Interface.</b><p>
  * Table Class: <br>
  * Next - next object in this table <br>
  * Seek - find this record using this key <br>
  * setKey - set the order for sequential access/seek <br>
  * getCurrentRecord - Get the current record<br>
  * Methods to override:<br />
  * nextRecord - Return the next record in this table and MoveFields
  * - null = EOF<br />
  * addNew()   - Create/Clear the Base record<br />
  * edit() - Lock the Base record<br />
  * add()    - Add the Base record<br />
  * set()    - Update the Base record<br />
  * remove()   - Delete the Base record <p />
  * NOTE: The Record Class can be either a Record of a Table or a Query<br />
  * based on one or more tables <p />
  * The BaseTable class MUST be overridden to add functionality <br />
  * MemoryTable - In-memory table (No database engine required!) <br />
  * JdbcTable - Converts to/from JDBC calls.
  * <p />There are several types of tables:<br />
  * LOCAL - Local tables - Read only tables that contain static information (usually small).<br />
  * REMOTE - Remote tables - Tables contain live data.<br />
  * VECTOR - Vector tables - In-memory tables.<br />
  * UNSHARABLE - Unsharable memory tables - In-memory tables that arn't shared between apps.
  */
 public abstract class BaseTable extends FieldTable
 {
     /**
      * The item separator for a full handle.
      */
     public static final char HANDLE_SEPARATOR = ':';
     /**
      * The database parent.
      */
     protected BaseDatabase m_database = null; // Parent
     /**
      * Normal/Invalid/EOF/BOF.
      */
     protected int m_iRecordStatus = DBConstants.RECORD_INVALID;
     /**
      * A serializable object that contains the current object's source.<br />
      *  <table><primary key> - for JDBC database tables.<br />
      *  <objectID> - for Remote tables (Corba/RMI).<br />
      *  null - for Vector tables (Not persistent).
      */
     protected Object m_objectID = null;     // Object's source unique ID
     /**
      * Table properties.<br />
      * Turn AUTOSEQUENCE property off to disable the database's autosequence (ie., for importing).
      */
     protected Map<String,Object> m_properties = null;
 
     /**
      * Constructor.
      */
     public BaseTable()
     {
         super();
     }
     /**
      * Constructor.
      * @param database The database to add this table to.
      * @param record The record to connect to this table.
      */
     public BaseTable(BaseDatabase database, Record record)
     {
         this();
         this.init(database, record);
     }
     /**
      * Init this table.
      * Add this table to the database and hook this table to the record.
      * NOTE: For linked tables, only the last table on the chain should be added to the database.
      * @param database The database to add this table to.
      * @param record The record to connect to this table.
      */
     public void init(BaseDatabase database, Record record)
     {
         m_database = database;
         if (m_database != null)
             m_database.addTable(this);
         super.init(record);
         m_objectID = null;
         m_bIsOpen = false;
         m_iRecordStatus = DBConstants.RECORD_INVALID;
     }
     /**
      * Free this table object.
      * Don't call this directly, freeing the record will free the table correctly.
      * You never know, another table may have been added to the table chain.
      * First, closes this table, then removes me from the database.
      */
     public void free()
     {
         int iOldEditMode = 0;
         if (m_record != null) // First, release the record for this table
         {
             iOldEditMode = this.getRecord().getEditMode();
             this.getRecord().setEditMode(iOldEditMode | DBConstants.EDIT_CLOSE_IN_FREE);    // This is a cludge... signals tables that this is the last close()!
         }
         this.close();
         if (m_record != null) // First, release the record for this table
             this.getRecord().setEditMode(iOldEditMode);   // This is a cludge... signals tables that this is the last close()!
 
         super.free(); // Set the record's table reference to null.
 
         if (m_database != null)
             m_database.removeTable(this);
         m_database = null;
         m_dataSource = null;
         m_objectID = null;
     }
     /**
      * Not used here - overriden in PassThruTable.
      * @param table Table to add to my table list.
      * @see PassThruTable
      */
     public void addTable(BaseTable table)
     {
     }
     /**
      * Remove this table from this table list.
      * @param table The table to remove.
      * Not used here - overriden in PassThruTable.
      * @see PassThruTable
      */
     public boolean removeTable(BaseTable table)
     {
         return false;
     }
     /**
      * Adding a file listener to the chain.
      * This just gives the table an ability to respond to listeners being added.
      * @param record TODO
      * @param listener The filter or listener to add to the chain.
      */
     public void addListener(Record record, FileListener listener)
     {
         record.doAddListener(listener);
         boolean bOldState = listener.setEnabledListener(false);      // To disable recursive forever loop!
         BaseListener nextListener = listener.setNextListener(null);     // Make sure this is the ONLY listener in the chain to get this call
         if (record.getEditMode() == Constants.EDIT_ADD)
         {
             boolean[] rgbModified = this.getRecord().getModified();
             listener.doNewRecord(DBConstants.DISPLAY);
             this.getRecord().setModified(rgbModified);   // Restore since doNew should not change modified fields.
         }
         else if ((record.getEditMode() == Constants.EDIT_IN_PROGRESS) || (record.getEditMode() == Constants.EDIT_CURRENT))
             listener.doValidRecord(DBConstants.DISPLAY);
         listener.setNextListener(nextListener);
         listener.setEnabledListener(bOldState);   // Renable the listener to eliminate echos
     }
     /**
      * Adding a field listener to a field.
      * This just gives the table an ability to respond to listeners being added.
      * @param listener The listener to add to the chain.
      */
     public void addListener(FieldListener listener, BaseField field)
     {
         field.doAddListener(listener);
     }
     /**
      * Set up/do the local criteria.
      * This is only here to accommodate local file systems that can't handle
      * REMOTE criteria. All you have to do here to handle the remote criteria
      * locally is to call: return record.handleRemoteCriteria(xx, yy).
      */
     public boolean doLocalCriteria(StringBuffer strFilter, boolean bIncludeFileName, Vector<BaseField> vParamList)
     {   // Default BaseListener
         if (this.isTable())   // For tables, do the remote criteria now
             return this.getRecord().handleRemoteCriteria(strFilter, bIncludeFileName, vParamList);  // If can't handle remote
         else
             return true;    // Record okay, don't skip it
     }
     /**
      * Called when a change is the record status is about to happen/has happened.
      * <p />NOTE: This is where the notification message is sent after an ADD, DELETE, or UPDATE.
      * @param field If the change is due to a field, pass the field.
      * @param iChangeType The type of change.
      * @param bDisplayOption If true display the fields on the screen.
      * @return An error code.
      */
     public int doRecordChange(FieldInfo field, int iChangeType, boolean bDisplayOption)     // init this field override for other value
     {
         return DBConstants.NORMAL_RETURN;
     }
     /**
      * Move the data source buffer to all the fields.
      *  <p /><pre>
      *  Make sure you do the following steps:
      *  1) Move the data fields to the correct record data fields, with mode DBConstants.READ_MOVE.
      *  2) Save the data source or set to null, so I can cache the source if necessary.
      *  3) Save the objectID if it is not an Integer type, so I can serialize the source of this object.
      *  </pre>
      * @return The error code.
      * @exception DBException File exception.
      */
     public int dataToFields(Rec record) throws DBException
     {
         return super.dataToFields(record);
     }
     /**
      * Move the output buffer to this field.
      * You must override this.
      * Warning: Check to make sure the field.isSelected() before moving data!
      * @param field The field to move the current data to.
      * @return The error code (From field.setData()).
      * @exception DBException File exception.
      */
     public int dataToField(Field field) throws DBException
     {
         throw new DatabaseException("You must override dataToField");
     }
     /**
      * Move all the fields to the output buffer.
      * @exception DBException File exception.
      */
     public void fieldsToData(Rec record) throws DBException
     {
         super.fieldsToData(record);
     }
     /**
      * Move this Field's data to the record area (Override this for non-standard buffers).
      * Warning: Check to make sure the field.isSelected() before moving data!
      * @param field The field to move the current data from.
      * @exception DBException File exception.
      */
     public void fieldToData(Field field) throws DBException
     {
         throw new DatabaseException("You must override fieldToData");
     }
     /**
      * Get the current table target.
      * This is usually used to keep track of the current table in Move(s).
      * (ie., If this is a table for the "Animal" record, m_CurrentRecord
      * could be an "Cat" table object).
      * @return The current table target.
      */
     public BaseTable getCurrentTable()
     {
         return this;        // Current table
     }
     /**
      * Get the table's database.
      * @return The database.
      */
     public BaseDatabase getDatabase()
     {
         return m_database;
     }
     /**
      * Get the main (base) record for this table.
      * Same as getFieldList, but casts the class to Record.
      * @return The record.
      */
     public Record getRecord()
     {
         return (Record)super.getRecord();
     }
     /**
      * Get the count of records accessed so far.
      * @return The record count if you can supply an accurate count, else -1.
      */
     public int getRecordCount()
     {
         return DBConstants.UNKNOWN_RECORD_COUNT;
     }
     /**
      * Return this table drive type for the getObjectSource call. (Override this method)
      * @return java.lang.String The source type.
      */
     public String getSourceType()
     {
         return this.getClass().getName().toString();
     }
     /**
      * Is this table open?
      * <p />Note: m_bIsOpen is a member variable of FieldTable.
      * @return true if the table is open.
      */
     public boolean isOpen()
     {
         return m_bIsOpen;
     }
     /**
      * Is this a table (or SQL) type of "Table"?
      * This flag determines when you hit the end of a query. If set, every record is compared
      * against the last record in the query. If not, the only EOF determines the end (ie., a SQL query).
      * Override this and return false where the DB returns the complete query.
      */
     public boolean isTable()
     {
         return true;
     }
     /**
      * Create a representation of the current record and optionally cache all the data fields.
      * <p>Use the setDataRecord call to make this record current again
      * @param bCacheData boolean Cache the data?
      * @param iFieldTypes The types of fields to cache (see BaseBuffer).
      * @return DataRecord The information needed to recreate this record.
      */
     public DataRecord getDataRecord(boolean bCacheData, int iFieldsTypes)
     {
         if ((this.getCurrentTable().getRecord().getEditMode() != Constants.EDIT_IN_PROGRESS)
             && (this.getCurrentTable().getRecord().getEditMode() != Constants.EDIT_CURRENT))
                 return null;
         DataRecord dataRecord = new DataRecord(null);
         try   {
             dataRecord.setHandle(this.getHandle(DBConstants.BOOKMARK_HANDLE), DBConstants.BOOKMARK_HANDLE);
             dataRecord.setHandle(this.getHandle(DBConstants.DATA_SOURCE_HANDLE), DBConstants.DATA_SOURCE_HANDLE);
             dataRecord.setHandle(this.getHandle(DBConstants.OBJECT_ID_HANDLE), DBConstants.OBJECT_ID_HANDLE);
             dataRecord.setHandle(this.getHandle(DBConstants.OBJECT_SOURCE_HANDLE), DBConstants.OBJECT_SOURCE_HANDLE);
         } catch (DBException ex)    {
             dataRecord.free();
             dataRecord = null;
         }
         if (dataRecord != null) if (bCacheData)
         {
             BaseBuffer buffer = new VectorBuffer(null, iFieldsTypes);
             buffer.fieldsToBuffer(this.getCurrentTable().getRecord(), iFieldsTypes);
             dataRecord.setBuffer(buffer);
         }
         return dataRecord;
     }
     /**
      * Make the record represented by this DataRecord current.
      * @param dataRecord tour.db.DataRecord The datarecord to try to recreate.
      * @return true if successful.
      */
     public boolean setDataRecord(DataRecord dataRecord)
     {
         m_objectID = null;
         m_dataSource = null;
     
         boolean bFound = false;
         if (dataRecord.getBuffer() == null)
         {
             this.getCurrentTable().getRecord().setEditMode(Constants.EDIT_NONE);    // Make sure read doesn't use current record.
         }
         else
         {
             m_iRecordStatus = DBConstants.RECORD_NORMAL;
             this.getCurrentTable().getRecord().setEditMode(Constants.EDIT_CURRENT);     // Data Current, not locked
             dataRecord.getBuffer().bufferToFields(this.getCurrentTable().getRecord(), DBConstants.DONT_DISPLAY, DBConstants.READ_MOVE);
             this.getCurrentTable().getRecord().handleValidRecord();   // Display Fields
             bFound = true;
             m_objectID = dataRecord.getHandle(DBConstants.OBJECT_ID_HANDLE);
             m_dataSource = dataRecord.getHandle(DBConstants.DATA_SOURCE_HANDLE);
         } 
         try   {
             if (!bFound) if (dataRecord.getHandle(DBConstants.DATA_SOURCE_HANDLE) != null)
                 bFound = (this.setHandle(dataRecord.getHandle(DBConstants.DATA_SOURCE_HANDLE), DBConstants.DATA_SOURCE_HANDLE) != null);
             } catch (DBException ex)    {
                 return false; // Not found
             }   
     
         try   {
             if (!bFound) if (dataRecord.getHandle(DBConstants.BOOKMARK_HANDLE) != null)
                 bFound = (this.setHandle(dataRecord.getHandle(DBConstants.BOOKMARK_HANDLE), DBConstants.BOOKMARK_HANDLE) != null);
             } catch (DBException ex)    {
                 return false; // Not found
             }   
     
         try   {
             if (!bFound) if (dataRecord.getHandle(DBConstants.OBJECT_ID_HANDLE) != null)
                 bFound = (this.setHandle(dataRecord.getHandle(DBConstants.OBJECT_ID_HANDLE), DBConstants.OBJECT_ID_HANDLE) != null);
             } catch (DBException ex)    {
                 return false; // Not found
             }
     
         return bFound;  // Return
     }
     /**
      * Requery the table.
      * <p>NOTE: You do not need to Open to do a seek or addNew.
      * The record pointer is positioned before the first record at BOF.
      * @exception DBException
      */
     public void open() throws DBException
     {
         if (this.isOpen())
             return;         // Ignore if already open
         m_bIsOpen = false;
         this.getRecord().handleInitialKey();        // Set up the smaller key
         this.getRecord().handleEndKey();            // Set up the larger key
         this.doOpen();      // Now do the physical open in sub class.
         m_bIsOpen = true;
         m_iRecordStatus = DBConstants.RECORD_INVALID | DBConstants.RECORD_AT_BOF;
         if ((this.getRecord().getOpenMode() & DBConstants.OPEN_SUPPRESS_MESSAGES) == 0)
             this.getRecord().handleRecordChange(DBConstants.AFTER_REQUERY_TYPE);    // Notify listeners that a new table will be built
     }
     /**
      * Close this table.
      */
     public void close()
     {
         m_bIsOpen = false;
         m_iRecordStatus = DBConstants.RECORD_INVALID;
     }
     /**
      * Create/Clear the current object (Always called from the record class).
      * @exception DBException File exception.
      */
     public void addNew() throws DBException
     {
         this.getRecord().setEditMode(Constants.EDIT_NONE);
         m_objectID = null;
         m_dataSource = null;
         try   {
             this.doAddNew();
             m_iRecordStatus = DBConstants.RECORD_NEW;
         } catch (DBException ex)   {
             throw ex;
         }
         this.getRecord().setEditMode(Constants.EDIT_ADD);   // I do this before the handleNewRecord behaviors do anything
         this.getRecord().handleNewRecord();   // Display Fields
     }
     /**
      * Add this new record (Always called from the record class).
      * @param fieldList The record to add.
      * @exception DBException File exception.
      */
     public void add(Rec fieldList) throws DBException
     {
         Record record = (Record)fieldList;
         int iErrorCode = DBConstants.NORMAL_RETURN;
         if ((record.getOpenMode() & DBConstants.OPEN_READ_ONLY) != 0)
             throw new DatabaseException(DBConstants.ERROR_READ_ONLY);
         boolean[] rgbSaveEnabled = null;
         try   {
             if (record.getEditMode() != Constants.EDIT_ADD)
             {
                 if (record.getEditMode() == Constants.EDIT_IN_PROGRESS)
                     if ((record.getOpenMode() & DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY) == DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY)
                 { // Special case - Add with refresh/update on add
                     iErrorCode = record.handleRecordChange(DBConstants.ADD_TYPE);
                     if (iErrorCode != DBConstants.NORMAL_RETURN)
                         throw new DatabaseException(iErrorCode);
                     rgbSaveEnabled = record.setEnableListeners(false);      // Don't trigger any UPDATE listeners
                     if (record.isModified())
                         this.set(record);
                     record.setEnableListeners(rgbSaveEnabled);
                     rgbSaveEnabled = null;
                     iErrorCode = record.handleRecordChange(DBConstants.AFTER_ADD_TYPE);
                     if (iErrorCode != DBConstants.NORMAL_RETURN)
                         throw new DatabaseException(iErrorCode);
                     return;
                 }
                 throw new DatabaseException(DBConstants.INVALID_RECORD);
             }
             CounterField fldCounter = (CounterField)record.getCounterField();
             if (fldCounter != null)
                 if (!DBConstants.FALSE.equalsIgnoreCase(this.getProperty(SQLParams.AUTO_SEQUENCE_ENABLED))) // Auto seq is enabled:
             {       // Just in case you had some data in the counter (it will be cleared).
                 boolean[] rgbEnabled = fldCounter.setEnableListeners(false);
                 fldCounter.setData(null);
                 fldCounter.setEnableListeners(rgbEnabled);
             }
             iErrorCode = record.handleRecordChange(DBConstants.ADD_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
             if (record.getEditMode() != Constants.EDIT_ADD)
             {   // It is possible that the add behavior wrote and refreshed the record, in that case, I need to update.
                 if ((record.getEditMode() == Constants.EDIT_IN_PROGRESS) || (record.getEditMode() == Constants.EDIT_CURRENT))
                 { // Special case - Add with refresh/update on add
                     rgbSaveEnabled = record.setEnableListeners(false);      // Don't trigger any UPDATE listeners
                     if (record.isModified())
                         this.set(record);
                     record.setEnableListeners(rgbSaveEnabled);
                     rgbSaveEnabled = null;
                     iErrorCode = record.handleRecordChange(DBConstants.AFTER_ADD_TYPE);
                     if (iErrorCode != DBConstants.NORMAL_RETURN)
                         throw new DatabaseException(iErrorCode);
                     return;
                 }
                 throw new DatabaseException(DBConstants.INVALID_RECORD);
             }
             this.fieldsToData(record);
             this.setLastModHint(record);
             this.doAdd(record);
             m_iRecordStatus |= DBConstants.RECORD_INVALID;
         } catch (DBException ex)    {
             if (rgbSaveEnabled != null)
                 record.setEnableListeners(rgbSaveEnabled);
             if (ex.getErrorCode() == DBConstants.DUPLICATE_KEY)
             {   // If duplicate key, don't invalidate the record.
                 iErrorCode = this.getRecord().handleErrorReturn(DBConstants.AFTER_ADD_TYPE, ex.getErrorCode()); // Give the listeners a chance to fix the error.
                 if (iErrorCode == DBConstants.NORMAL_RETURN)
                     return;
             }
 //x            else
 //x                record.setEditMode(Constants.EDIT_NONE);
             throw ex;
         }
         iErrorCode = record.handleRecordChange(DBConstants.AFTER_ADD_TYPE);
         if (iErrorCode != DBConstants.NORMAL_RETURN)
             throw new DatabaseException(iErrorCode);
         record.setEditMode(Constants.EDIT_NONE);    // Unknown status after an add
         if ((record.getOpenMode() & DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY) == DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY)
             return;   // Will need these to refresh record
         m_objectID = null;
         m_dataSource = null;
     }
     /**
      * Lock the current record.
      * This method responds differently depending on what open mode the record is in:
      * OPEN_DONT_LOCK - A physical lock is not done. This is usually where deadlocks are possible
      * (such as screens) and where transactions are in use (and locks are not needed).
      * OPEN_LOCK_ON_EDIT - Holds a lock until an update or close. (Update crucial data, or hold records for processing)
      * Returns false is someone already has a lock on this record.
      * OPEN_WAIT_FOR_LOCK - Don't return from edit until you get a lock. (ie., Add to the total).
      * Returns false if someone has a hard lock or time runs out.
      * @return true if successful, false is lock failed.
      * @exception DBException FILE_NOT_OPEN
      * @exception DBException INVALID_RECORD - Record not current.
      */
     public int edit() throws DBException
     {
     	int iErrorCode = DBConstants.NORMAL_RETURN;
         if ((this.getRecord().getOpenMode() & DBConstants.OPEN_READ_ONLY) != 0)
             return DBConstants.NORMAL_RETURN;        // I Don't throw any exception unless they try to Update!
         try   {
             if (this.getRecord().getEditMode() == Constants.EDIT_IN_PROGRESS)
                 return DBConstants.NORMAL_RETURN;        // Okay, Already locked
             if (this.getRecord().getEditMode() != Constants.EDIT_CURRENT)
                 throw new DatabaseException(DBConstants.INVALID_RECORD);
             iErrorCode = this.getRecord().handleRecordChange(DBConstants.LOCK_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
             iErrorCode = this.doEdit();
             if ((iErrorCode == DBConstants.RECORD_CHANGED) || ((this.getRecord().getOpenMode() & DBConstants.OPEN_WAIT_FOR_LOCK_STRATEGY) == DBConstants.OPEN_WAIT_FOR_LOCK_STRATEGY))
             {   // If record has changed, or I using the wait strategy (which means I don't know if the record was updated between read and edit)
             	if ((this.getRecord().getOpenMode() & DBConstants.OPEN_ERROR_ON_DIRTY_LOCK_TYPE) == 0)
             	{	// Typically you don't want to return an error, you want to refresh the data (unless this is slave code)
             		iErrorCode = this.getRecord().refreshToCurrent(DBConstants.AFTER_UPDATE_TYPE, false);
             	}
             }
             if (iErrorCode == DBConstants.NORMAL_RETURN)
                 this.getRecord().setEditMode(Constants.EDIT_IN_PROGRESS);
         } catch (DBException ex)    {
             throw ex;
         }
         return iErrorCode;
     }
     /**
      * Update this record (Always called from the record class).
      * @param fieldList The record to add.
      * @exception DBException File exception.
      */
     public void set(Rec fieldList) throws DBException
     {
         Record record = (Record)fieldList;
         int iErrorCode = DBConstants.NORMAL_RETURN;
         if ((record.getOpenMode() & DBConstants.OPEN_READ_ONLY) != 0)
             throw new DatabaseException(DBConstants.ERROR_READ_ONLY);
         if ((record.getOpenMode() & DBConstants.OPEN_APPEND_ONLY) != 0)
             throw new DatabaseException(DBConstants.ERROR_APPEND_ONLY);
         try   {
             if (record.getEditMode() != Constants.EDIT_IN_PROGRESS)
                 throw new DatabaseException(DBConstants.RECORD_NOT_LOCKED);
             iErrorCode = record.handleRecordChange(DBConstants.UPDATE_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
             this.fieldsToData(record);
             this.doSet(record);
             m_iRecordStatus |= DBConstants.RECORD_INVALID;
         } catch (DBException ex)    {
             iErrorCode = this.getRecord().handleErrorReturn(DBConstants.AFTER_UPDATE_TYPE, ex.getErrorCode()); // Give the listeners a chance to fix the error.
             // Note: I DO NOT do: record.setEditMode(Constants.EDIT_NONE);  to give the client a chance to recover and rewrite this record.
             if ((iErrorCode != DBConstants.NORMAL_RETURN) && (iErrorCode != DBConstants.RECORD_CHANGED))
                 throw ex;
             // If the error was fixed, continue
         }
         record.setEditMode(Constants.EDIT_NONE);    // Unknown status after an update
         iErrorCode = record.handleRecordChange(DBConstants.AFTER_UPDATE_TYPE);  // Remember, these listeners may refresh the record
         if (iErrorCode != DBConstants.NORMAL_RETURN)
             throw new DatabaseException(iErrorCode);
         m_objectID = null;
         m_dataSource = null;
     }
     /**
      * Delete this record (Always called from the record class).
      * @exception DBException File exception.
      */
     public void remove() throws DBException
     {
         if ((this.getRecord().getOpenMode() & DBConstants.OPEN_READ_ONLY) != 0)
             throw new DatabaseException(DBConstants.ERROR_READ_ONLY);
         if ((this.getRecord().getOpenMode() & DBConstants.OPEN_APPEND_ONLY) != 0)
             throw new DatabaseException(DBConstants.ERROR_APPEND_ONLY);
         try   {
             if ((this.getRecord().getEditMode() != Constants.EDIT_IN_PROGRESS)
                 && (this.getRecord().getEditMode() != Constants.EDIT_CURRENT))
                     throw new DatabaseException(DBConstants.INVALID_RECORD);
             int iErrorCode = this.getRecord().handleRecordChange(DBConstants.DELETE_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
             if ((this.getRecord().getEditMode() == Constants.EDIT_IN_PROGRESS)
                 || (this.getRecord().getEditMode() == Constants.EDIT_CURRENT))  // I know I just checked for this, but it possible the the listener changed the record state
                     this.doRemove();
             m_iRecordStatus |= DBConstants.RECORD_INVALID;
         } catch (DBException ex)    {
             this.getRecord().setEditMode(Constants.EDIT_NONE);  // Unknown status after a delete
             throw ex;
         }
         this.getRecord().setEditMode(Constants.EDIT_NONE);  // Unknown status after a delete
         int iErrorCode = this.getRecord().handleRecordChange(DBConstants.AFTER_DELETE_TYPE);
         if (iErrorCode != DBConstants.NORMAL_RETURN)
             throw new DatabaseException(iErrorCode);
         m_objectID = null;
         m_dataSource = null;
     }
     /**
      * Move the position of the record +1.
      */
     public final Object next() throws DBException
     {
         return this.move(DBConstants.NEXT_RECORD);
     }
     /**
      * Move the position of the record.
      * @param iRelPosition - Relative position positive or negative or FIRST_RECORD/LAST_RECORD.
      * @return NORMAL_RETURN - The following are NOT mutually exclusive
      * @return RECORD_INVALID
      * @return RECORD_AT_BOF
      * @return RECORD_AT_EOF
      * @return RECORD_EMPTY (same as RECORD_AT_BOF | RECORD_AT_EOF)
      * @exception FILE_NOT_OPEN.
      * @exception INVALID_RECORD - Record position is not current or move past EOF or BOF.
      */
     public FieldList move(int iRelPosition) throws DBException
     {
         if ((m_iRecordStatus & DBConstants.RECORD_NEXT_PENDING) == DBConstants.RECORD_NEXT_PENDING)
             if (iRelPosition == DBConstants.NEXT_RECORD)
         {
             m_iRecordStatus = DBConstants.RECORD_NORMAL;
             this.getRecord().handleValidRecord(); // Display Fields
             int iErrorCode = this.getRecord().handleRecordChange(DBConstants.MOVE_NEXT_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
             return this.getCurrentTable().getRecord();  // Special case - last call was hasNext()
         }
         if ((m_iRecordStatus & DBConstants.RECORD_PREVIOUS_PENDING) == DBConstants.RECORD_PREVIOUS_PENDING)
             if (iRelPosition == DBConstants.PREVIOUS_RECORD)
         {
             m_iRecordStatus = DBConstants.RECORD_NORMAL;
             this.getRecord().handleValidRecord(); // Display Fields
             int iErrorCode = this.getRecord().handleRecordChange(DBConstants.MOVE_NEXT_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
             return this.getCurrentTable().getRecord();  // Special case - last call was hasNext()
         }
         Record record = null;
         Object[] rgobjEnabledFields = null;
         try   {
             this.getRecord().setEditMode(Constants.EDIT_NONE);
 
             if (!this.isOpen())
                 this.open();    // Cool! It opens automatically.
             this.getRecord().setEditMode(Constants.EDIT_NONE);
 
             if (this.isTable())
             {
                 if (iRelPosition == DBConstants.NEXT_RECORD)
                     if (m_iRecordStatus == (DBConstants.RECORD_INVALID | DBConstants.RECORD_AT_BOF))
                         iRelPosition = DBConstants.FIRST_RECORD;    // After open, read first
                 if (iRelPosition == DBConstants.PREVIOUS_RECORD)
                     if (m_iRecordStatus == (DBConstants.RECORD_INVALID | DBConstants.RECORD_AT_BOF))
                         iRelPosition = DBConstants.LAST_RECORD;     // After open, read last
                 if ((iRelPosition == DBConstants.FIRST_RECORD) || (iRelPosition == DBConstants.LAST_RECORD))
                 {   // If this is a table, it can't handle starting and ending keys.. do it manually
                     KeyArea keyArea = this.getRecord().getKeyArea(-1);
                     if (keyArea != null) if (iRelPosition == DBConstants.FIRST_RECORD)
                     {
                         if (keyArea.isModified(DBConstants.START_SELECT_KEY))
                         {
                             keyArea.reverseKeyBuffer(null, DBConstants.START_SELECT_KEY);
                             boolean bFound =  this.getRecord().seek(">=");
                             if (bFound)
                                 record = this.getRecord();
                             else
                             {
                                 record = null;
                                 m_iRecordStatus |= DBConstants.RECORD_AT_EOF;
                             }
                             if (bFound) if (this.isTable())
                                 bFound = !this.isEOF();   // Do end criteria check
                             if (!bFound)
                             {
                                 record = null;
                                 m_iRecordStatus |= DBConstants.RECORD_AT_EOF;
                             }
                             return record;
                         }
                     }
                     else //   if (iRelPosition == DBConstants.LAST_RECORD)
                     {
                         if (keyArea.isModified(DBConstants.END_SELECT_KEY))
                         {
                             keyArea.reverseKeyBuffer(null, DBConstants.END_SELECT_KEY);
                             boolean bFound =  this.getRecord().seek("<=");
                             if (bFound)
                                 record = this.getRecord();
                             else
                             {
                                 record = null;
                                 m_iRecordStatus |= DBConstants.RECORD_AT_BOF;
                             }
                             if (bFound) if (this.isTable())
                                 bFound = !this.isBOF();   // Do end criteria check
                             if (!bFound)
                             {
                                 record = null;
                                 m_iRecordStatus |= DBConstants.RECORD_AT_BOF;
                             }
                             return record;
                         }
                     }
                 }
             }
 
             m_iRecordStatus = DBConstants.RECORD_INVALID;
             m_iRecordStatus = this.doMove(iRelPosition);    // Do the physical move next
 
             if (m_iRecordStatus == DBConstants.RECORD_NORMAL)
             {
                 rgobjEnabledFields = this.getRecord().setEnableFieldListeners(false);
                 this.dataToFields(this.getRecord());
                 this.getRecord().setEnableFieldListeners(rgobjEnabledFields);
             }
             if (this.isTable())
             {
                 if (this.isBOF())
                     m_iRecordStatus |= DBConstants.RECORD_AT_BOF; // At BOF
                 if (this.isEOF())
                     m_iRecordStatus |= DBConstants.RECORD_AT_EOF; // At EOF
             }
             if ((m_iRecordStatus & DBConstants.RECORD_AT_BOF) == DBConstants.RECORD_AT_BOF)
             {
                 this.getRecord().handleRecordChange(DBConstants.SELECT_EOF_TYPE); // Notify listeners that EOF was hit (Reading backward or readnext on an empty file)
                 record = null;
                 m_objectID = null;
                 m_dataSource = null;
                 return record;
             }
             if ((m_iRecordStatus & DBConstants.RECORD_AT_EOF) == DBConstants.RECORD_AT_EOF)
             {
                 this.getRecord().handleRecordChange(DBConstants.SELECT_EOF_TYPE); // Notify listeners that EOF was hit
                 record = null;
                 m_objectID = null;
                 m_dataSource = null;
                 return record;
             }
             this.getRecord().setEditMode(Constants.EDIT_CURRENT);
 
             // Special test for local criteria
             if (this.getRecord().handleLocalCriteria(null, false, null) == false)
             { // This record didn't pass the test, get the next one that matches
                 if ((iRelPosition > 0) || (iRelPosition == DBConstants.FIRST_RECORD))
                 {
                     if (iRelPosition == DBConstants.FIRST_RECORD)
                         iRelPosition = +1;
                     return this.move(iRelPosition);
                 }
                 if (iRelPosition == DBConstants.LAST_RECORD)
                     iRelPosition = -1;
                 return this.move(iRelPosition);
             }
             record = this.getRecord();  // m_CurrentRecord
 
         // Table will update m_dbEditMode to dbEditCurrent if found.
         } catch (DBException ex)    {
             if (rgobjEnabledFields != null)
                 this.getRecord().setEnableFieldListeners(rgobjEnabledFields);
             if (ex.getErrorCode() == DBConstants.INVALID_RECORD)
                 record = null;  // Special case (empty recordset)
             else
                 throw ex;
         }
         if (record != null)
         {
             this.getRecord().handleValidRecord(); // Display Fields
             int iErrorCode = this.getRecord().handleRecordChange(DBConstants.MOVE_NEXT_TYPE);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 throw new DatabaseException(iErrorCode);
         }
         else
         {
             this.getRecord().handleNewRecord();     // Display Fields
         }
         return record;
     }
     /**
      * Move the position of the record to this record location.
      * Not implemented for a BaseTable, must use GridTable.
      * Be careful, if a record at a row is deleted, this method will return a new
      * (empty) record, so you need to check the record status before updating it.
      * @param iRelPosition - Absolute position of the record to retrieve.
      * @return The record at this location (or null if not found).
      * @exception DBException File exception.
      */
     public Object get(int iPosition) throws DBException
     {
         throw new DatabaseException("Can't do a positioned 'get' on a BaseTable - must use GridTable");
     }
     /**
      * Read the record that matches this record's current key.
      * <p>NOTE: You do not need to Open to do a seek or addNew.
      * But, if you have an active query, it is no longer valid.
      * WARNING: This method ignores selection behaviors.
      * @param strSeekSign - Seek sign:
      *  <pre>
      *  null/"=" - Look for the first match.
      *  "==" - Look for an exact match (On non-unique keys, must match primary key also).
      *  ">" - Look for the first record greater than this.
      *  ">=" - Look for the first record greater than or equal to this.
      *  "<" - Look for the first record less than to this.
      *  "<=" - Look for the first record less than or equal to this.
      *  </pre>
      * @return true if successful, false if not found.
      * @exception FILE_NOT_OPEN.
      * @exception KEY_NOT_FOUND - The key was not found on read.
      */
     public boolean seek(String strSeekSign) throws DBException
     {
         boolean bSuccess = false;
         m_objectID = null;
         m_dataSource = null;
         m_bIsOpen = false;
         if (strSeekSign == null)
             strSeekSign = DBConstants.EQUALS;
         bSuccess = this.doSeek(strSeekSign);
         m_bIsOpen = true;
         if (bSuccess)
         {
             m_iRecordStatus = DBConstants.RECORD_NORMAL;
             this.getRecord().setEditMode(Constants.EDIT_CURRENT);
             Object[] rgobjEnabledFields = null;
             try {
                 rgobjEnabledFields = this.getRecord().setEnableFieldListeners(false);
                 this.dataToFields(this.getRecord());
             } catch (DBException ex)    {
                 throw ex;
             } finally {
                 this.getRecord().setEnableFieldListeners(rgobjEnabledFields);
             }
             this.getRecord().handleValidRecord(); // Display Fields
         }
         else
         {
             m_iRecordStatus = DBConstants.RECORD_INVALID | DBConstants.RECORD_AT_BOF | DBConstants.RECORD_AT_EOF;
             this.getRecord().handleNewRecord();     //? Display Fields (Should leave record in an indeterminate state!)
             this.getRecord().setEditMode(Constants.EDIT_NONE);  // Unknown status
         }
         return bSuccess;
     }
     /**
      * Reposition to this record Using this bookmark.
      * @param Object bookmark Bookmark.
      * @param int iHandleType Type of handle (see getHandle).
      * @exception FILE_NOT_OPEN.
      * @return record if found/null - record not found.
      */
     public FieldList setHandle(Object bookmark, int iHandleType) throws DBException
     {
         if ((iHandleType == DBConstants.BOOKMARK_HANDLE) && (bookmark instanceof String))     // It is okay to pass in a string, but convert it first!
             if ((this.getRecord().getCounterField() != null) || (this.getRecord().getKeyArea(DBConstants.MAIN_KEY_AREA).getKeyFields() > 1))
         {   // As a convenience convert a string bookmark to the proper bookmark
             try   {
             	bookmark = Converter.stripNonNumber(bookmark.toString());
                 bookmark = new Integer((String)bookmark);
             } catch (NumberFormatException ex)  {
                 bookmark = new StrBuffer((String)bookmark);
             }
         }
         if ((this.getRecord().getEditMode() == Constants.EDIT_IN_PROGRESS)
             || (this.getRecord().getEditMode() == Constants.EDIT_CURRENT))
         {
             Object oldBookmark = this.getHandle(iHandleType);
             if (bookmark != null) if (bookmark.equals(oldBookmark)) if (!this.getRecord().isModified(false))
                 return this.getRecord();        // This is already your current record!
         }
 
         m_objectID = null;      // No current record
         m_dataSource = null;
         if (iHandleType == DBConstants.FULL_OBJECT_HANDLE)
         {
             if (!(bookmark instanceof String))
                 return null;
             int iIndex = ((String)bookmark).lastIndexOf(HANDLE_SEPARATOR);
             if ((iIndex == -1) || (iIndex == ((String)bookmark).length() - 1))
                 return null;
             String strRecord = ((String)bookmark).substring(0, iIndex);
             int iRecordIndex = strRecord.lastIndexOf(HANDLE_SEPARATOR, iIndex - 1);
             int iDBIndex = strRecord.lastIndexOf(HANDLE_SEPARATOR, iRecordIndex - 1);
             if (!strRecord.substring(iRecordIndex + 1, iIndex).equalsIgnoreCase(this.getRecord().getTableNames(false)))
                 if (!strRecord.substring(iDBIndex + 1, iRecordIndex).equalsIgnoreCase(this.getDatabase().getDatabaseName(false)))
                     return null;    // Not the correct record or database
             bookmark = ((String)bookmark).substring(iIndex + 1);
             iHandleType = DBConstants.OBJECT_ID_HANDLE;
         }
         m_bIsOpen = false;
         boolean bSuccess = this.doSetHandle(bookmark, iHandleType);
         m_bIsOpen = true;
         if (bSuccess)
         {
             m_iRecordStatus = DBConstants.RECORD_NORMAL;
             this.getRecord().setEditMode(Constants.EDIT_CURRENT);
             Object[] rgobjEnabledFields = null;
             try {
                 rgobjEnabledFields = this.getRecord().setEnableFieldListeners(false);
                 this.dataToFields(this.getRecord());
             } catch (DBException ex)    {
                 throw ex;
             } finally {
                 this.getRecord().setEnableFieldListeners(rgobjEnabledFields);
             }
             this.getRecord().handleValidRecord(); // Display Fields
             return this.getRecord();
         }
         else
         {
             m_iRecordStatus = DBConstants.RECORD_INVALID | DBConstants.RECORD_AT_BOF | DBConstants.RECORD_AT_EOF;
             this.getRecord().handleNewRecord();     //? Display Fields (Should leave record in an indeterminate state!)
             this.getRecord().setEditMode(Constants.EDIT_NONE);  // Unknown status
             return null;
         }
     }
     /**
      * Reposition to this record using this bookmark.
      * @param bookmark The handle to use to position the record.
      * @param iHandleType The type of handle (DATA_SOURCE/OBJECT_ID,OBJECT_SOURCE,BOOKMARK).
      * @return  - true - record found/false - record not found
      * @exception FILE_NOT_OPEN.
      * @exception DBException File exception.
      */
     public boolean doSetHandle(Object bookmark, int iHandleType) throws DBException
     {
         switch (iHandleType)
         {
         case DBConstants.DATA_SOURCE_HANDLE:
             m_dataSource = bookmark;
             return true;    // Success
         case DBConstants.OBJECT_ID_HANDLE:
             m_objectID = bookmark;      // Override this to make it work
             return true;    // Success
         case DBConstants.OBJECT_SOURCE_HANDLE:
         case DBConstants.FULL_OBJECT_HANDLE:
                         // Override to handle this case.
             return true;    // Success?
         case DBConstants.BOOKMARK_HANDLE:
         default:
             this.getRecord().getKeyArea(DBConstants.MAIN_KEY_AREA).reverseBookmark(bookmark, DBConstants.FILE_KEY_AREA);
 
             String strCurrentOrder = this.getRecord().getKeyArea().getKeyName();
             this.getRecord().setKeyArea(DBConstants.MAIN_KEY_AREA);
 
             m_bIsOpen = false;
             boolean bSuccess = this.doSeek("=");
             m_bIsOpen = true;
 
             this.getRecord().setKeyArea(strCurrentOrder);
             return bSuccess;
         }
     }
     /**
      * Get a unique object that can be used to reposition to this record.
      * <p>NOTE: UpdateAndEdit - There is special case that must be handled by getBookmark:
      * If a NEW record was just updated, the bookmark must reflect the new field.
      * In some SQL systems, you can do the call "GetLastModifiedBookmark."
      *  Update the new/current record, re-read it, and Edit.
      *  This is a special utility method, for new records. First, you update the record,
      *  then you immediately read and lock (edit) the record. Most DBMSs have a special
      *  routine to do this with a small performance hit. (Override this to implement the
      *  system specific method, or just leave this default code).<p>
      * NOTE: In most cases, you should override and use the db specific technique for getting
      *  the bookmark of the last added record.
      * @param int iHandleType 
      *  BOOKMARK_HANDLE - long ID for access to native (DB) objects<br />
      *  OBJECT_ID_HANDLE - object ID of persistent object (remote or local)<br />
      *  DATA_SOURCE_HANDLE - pointer to the physical object (persistent or not)<br />
      *  OBJECT_SOURCE_HANDLE - source of this object (RMI Server or JDBC path)<br />
      *  FULL_OBJECT_HANDLE - full path to this object ObjectSource + ObjectID.
      * @exception FILE_NOT_OPEN.
      * @exception INVALID_RECORD - There is no current record.
      */
     public Object getHandle(int iHandleType) throws DBException   
     {
         switch (iHandleType)
         {
         case DBConstants.OBJECT_SOURCE_HANDLE:  // Usually override to handle this type
         case DBConstants.FULL_OBJECT_HANDLE:
             String strSource = this.getSourceType() + HANDLE_SEPARATOR + this.getRecord().getDatabaseName() + HANDLE_SEPARATOR + this.getRecord().getTableNames(false);
             try   {
                 if (iHandleType == DBConstants.FULL_OBJECT_HANDLE)
                 {
                     if (this.getHandle(DBConstants.OBJECT_ID_HANDLE) == null)
                         strSource = null;
                     else
                         strSource += HANDLE_SEPARATOR + this.getHandle(DBConstants.OBJECT_ID_HANDLE).toString();
                 }
             } catch (DBException ex)    {
             }   
             return strSource;
 
         case DBConstants.DATA_SOURCE_HANDLE:
             return m_dataSource;
 
         case DBConstants.OBJECT_ID_HANDLE:
             return m_objectID;
 
         default:
         case DBConstants.BOOKMARK_HANDLE:
             return this.getRecord().getKeyArea(DBConstants.MAIN_KEY_AREA).setupBookmark(DBConstants.FILE_KEY_AREA);
         } 
     }
     /**
      * Get the Handle to the last modified or added record.
      * This uses some very inefficient (and incorrect) code... override if possible.
      * NOTE: There is a huge concurrency problem with this logic if another person adds
      * a record after you, you get the their (wrong) record, which is why you need to
      * provide a concrete implementation when you override this method.
      * @param iHandleType The handle type.
      * @return The bookmark.
      */
     public Object getLastModified(int iHandleType)
     {
         Object varBookmark = null;
         boolean[] rgbEnabled = null;
         int iOrigOrder = DBConstants.MAIN_KEY_AREA;
         boolean bOrigDirection = DBConstants.ASCENDING;
         Record record = this.getCurrentTable().getRecord();
         try   {
         // Save current order and ascending/descending
             iOrigOrder = record.getDefaultOrder();
             bOrigDirection = record.getKeyArea(DBConstants.MAIN_KEY_AREA).getKeyField(DBConstants.MAIN_KEY_FIELD).getKeyOrder();
         // Set reverse order, descending
             record.setKeyArea(DBConstants.MAIN_KEY_AREA);
             record.getKeyArea(DBConstants.MAIN_KEY_AREA).getKeyField(DBConstants.MAIN_KEY_FIELD).setKeyOrder(DBConstants.DESCENDING);
         // Read the highest key in the record
             rgbEnabled = record.setEnableListeners(false);
             this.close();
             int iCount = 0;
             Record newRecord = null;    // Special case - Updated, must move updated key to field (Last in Recordset).
             while ((newRecord = (Record)this.move(DBConstants.NEXT_RECORD)) != null)
             {   // Go thru the last 10 record to see if this is it
                 if ((m_iLastModType > -1) && (m_iLastModType < record.getFieldCount() + DBConstants.MAIN_FIELD))
                 {
                     Object objCompareData = newRecord.getField(m_iLastModType).getData();
                     if ((m_objLastModHint == objCompareData) || (m_objLastModHint.equals(objCompareData)))
                         break;      // This is the right one
                 }
                 else
                     break;
                 iCount++;
                 if (iCount >= 10)
                 {
                     Utility.getLogger().warning("Error (BaseTable/getLastModified): Get last not found");
                     newRecord = null;
                     break;  // Error
                 }
             }
             if (newRecord == null)
                 newRecord = (Record)this.move(DBConstants.FIRST_RECORD);    // Guessed wrong, just use the last record
             varBookmark = this.getHandle(iHandleType);
         } catch (DBException e)   {
             varBookmark = null;
         } finally {
             // Restore current order and ascending/descending
             record.setKeyArea(iOrigOrder);
             record.getKeyArea(DBConstants.MAIN_KEY_AREA).getKeyField(DBConstants.MAIN_KEY_FIELD).setKeyOrder(bOrigDirection);
             record.setEnableListeners(rgbEnabled);
         }
         return varBookmark;
     }
     /**
      * Create a new empty table using the definition in the record.
      * @exception DBException Open errors passed from SQL.
      * @return true if successful.
      */
     public boolean create() throws DBException
     {
         return false;
     }
     /**
      * Create a new empty table using the definition in the record.
      * @exception DBException Open errors passed from SQL.
      * @return true if successful.
      */
     public boolean loadInitialData() throws DBException
     {
     	BaseTable table = this;
         Record record = table.getRecord();
         while (((record.getDatabaseType() & DBConstants.SHARED_TABLE) != 0) && ((record.getDatabaseType() & DBConstants.BASE_TABLE_CLASS) == 0))
         {
         	String tableName = record.getTableNames(false);
         	Class<?> className = record.getClass().getSuperclass();
         	record = Record.makeRecordFromClassName(className.getName(), record.getRecordOwner());
         	record.setTableNames(tableName);
         	table = record.getTable();
         }
         if (record.getTable() instanceof PassThruTable)
         	table = this.getPhysicalTable((PassThruTable)record.getTable(), record);
         int iOpenMode = record.getOpenMode();
         record.setOpenMode(DBConstants.OPEN_NORMAL);	// Possible read-only
         
         int iCount = record.getFieldCount();
         boolean[] brgCurrentSelection = new boolean[iCount];
         for (int i = 0; i < iCount; i++)
         {
             brgCurrentSelection[i] = record.getField(i).isSelected();
             record.getField(i).setSelected(true);
         }
         BaseBuffer buffer = new VectorBuffer(null);
         buffer.fieldsToBuffer(record);
 
         org.jbundle.base.db.xmlutil.XmlInOut xml = new org.jbundle.base.db.xmlutil.XmlInOut(null, null, null);
 
         String filename = record.getArchiveFilename(true);
         String defaultFilename = record.getArchiveFilename(false);
         InputStream inputStream = null;
         if (Record.findRecordOwner(record) != null)
             if (Record.findRecordOwner(record).getTask() != null)
                 inputStream = Record.findRecordOwner(record).getTask().getInputStream(filename);
 
         boolean bSuccess = false;
         try {
         	bSuccess = xml.importXML(table, filename, inputStream);    // Data file read successfully
             if (!bSuccess)
                 if (!defaultFilename.equals(filename))
             {
                 if (Record.findRecordOwner(record) != null)
                     if (Record.findRecordOwner(record).getTask() != null)
                         inputStream = Record.findRecordOwner(record).getTask().getInputStream(defaultFilename);
                 bSuccess = xml.importXML(table, defaultFilename, inputStream);
             }
             if (!bSuccess)   
                Utility.getLogger().warning("No initial data for: " + record.getRecordName());
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
         	xml.free();
         }
         XmlInOut.enableAllBehaviors(record, false, false);
         buffer.bufferToFields(record, DBConstants.DISPLAY, DBConstants.SCREEN_MOVE);
         XmlInOut.enableAllBehaviors(record, true, true);
         for (int i = 0; i < iCount; i++)
         {
             record.getField(i).setSelected(brgCurrentSelection[i]);
         }
         record.setOpenMode(iOpenMode);
         if (record != this.getRecord())
         	record.free();	// If this was a base record.
         return bSuccess;    // Success
     }
     /**
      * Dig down and get the physical table for this record.
      * @param table
      * @param record
      * @return
      */
     private BaseTable getPhysicalTable(PassThruTable table, Record record)
     {
     	BaseTable altTable = table.getNextTable();
 		if (altTable instanceof PassThruTable)
 		{
 			BaseTable physicalTable = getPhysicalTable((PassThruTable)altTable, record);
 			if (physicalTable != null)
 				if (physicalTable != altTable)
 					return physicalTable;
 		}
 		else
 			if (altTable.getDatabase().getDatabaseName(true).equals(record.getTable().getDatabase().getDatabaseName(true)))
 				return altTable;
     	
     	Iterator<BaseTable> tables = table.getTables();
     	while (tables.hasNext())
     	{
     		altTable = tables.next();
     		if (altTable instanceof PassThruTable)
     		{
     			BaseTable physicalTable = getPhysicalTable((PassThruTable)altTable, record);
     			if (physicalTable != null)
     				if (physicalTable != altTable)
     					return physicalTable;
     		}
     		else 
     			if (altTable.getDatabase().getDatabaseName(true).equals(record.getTable().getDatabase().getDatabaseName(true)))
     				return altTable;
     	}
     	return table;
     }
     /**
      * User defined hint to find the last modified record.
      */
     protected Object m_objLastModHint = null;
     /**
      * Used in conjunction with the lastmodhint (usually the field number of the mod hint field; -1 - means the mod hint is undefined).
      */
     protected int m_iLastModType = -1;
     /**
      * Optionally set a hint to be used to find the last modified record.
      * This method is only called immediately before adding the physical record to the table.
      * The default logic saves the first field it finds that is modified,
      * since this is the field that would have triped the add logic.
      * NOTE: This is a last resort, try to override getLastModified and use a DB Specific call.
      */
     public void setLastModHint(Record record)
     {
         m_objLastModHint = null;
         for (m_iLastModType = DBConstants.MAIN_FIELD; m_iLastModType < record.getFieldCount() + DBConstants.MAIN_FIELD; m_iLastModType++)
         {
             BaseField field = record.getField(m_iLastModType);
             if (field.isModified()) if (!(field instanceof CounterField))
             {
                 m_objLastModHint = field.getData();
                 return;   // iFieldSeq = field
             }
         }
         m_iLastModType = -1;    // Not found
     }
     /**
      * Do the physical Open on this table.
      * @exception DBException File exception.
      */
     public void doOpen() throws DBException
     {
         m_bIsOpen = true;
     }
     /**
      * Create/Clear the current object (Always called from the record class).
      * @exception DBException File exception.
      */
     public abstract void doAddNew() throws DBException;
     /**
      * Delete this record (Always called from the record class).
      * Always override this method.
      * @exception DBException File exception.
      */
     public abstract void doRemove() throws DBException;
     /**
      * Lock the current record.
      * This method responds differently depending on what open mode the record is in:
      * OPEN_DONT_LOCK - A physical lock is not done. This is usually where deadlocks are possible
      * (such as screens) and where transactions are in use (and locks are not needed).
      * OPEN_LOCK_ON_EDIT - Holds a lock until an update or close. (Update crucial data, or hold records for processing)
      * Returns false is someone already has a lock on this record.
      * OPEN_WAIT_FOR_LOCK - Don't return from edit until you get a lock. (ie., Add to the total).
      * Returns false if someone has a hard lock or time runs out.
      * @return true if successful, false is lock failed.
      * @exception DBException FILE_NOT_OPEN
      * @exception DBException INVALID_RECORD - Record not current.
      */
     public abstract int doEdit() throws DBException;
     /**
      * Move the position of the record.
      * @param iRelPosition The relative order to move records.
      * @return NORMAL_RETURN - The following are NOT mutually exclusive
      * @return RECORD_INVALID
      * @return RECORD_AT_BOF
      * @return RECORD_AT_EOF
      * @return RECORD_EMPTY (same as RECORD_AT_BOF | RECORD_AT_EOF)
      * @exception DBException File exception.
      */
     public abstract int doMove(int iRelPosition) throws DBException;
     /**
      * Read the record that matches this record's current key.
      * @param strSeekSign The seek sign (defaults to '=').
      * @return true if successful.
      *  Remember to update m_dataSource if dataToFields needs to move from the data object.
      * @exception DBException File exception.
      */
     public abstract boolean doSeek(String strSeekSign) throws DBException;
     /**
      * Add this record (Always called from the record class).
      * @param record The record to add.
      * @exception DBException File exception.
      */
     public abstract void doAdd(Record record) throws DBException;
     /**
      * Update this record (Always called from the record class).
      * @param record The record to update.
      * @exception DBException File exception.
      */
     public abstract void doSet(Record record) throws DBException;
     /**
      * Is the last record in the file?
      * @return false if file position is at last record.
      */
     public boolean hasPrevious() throws DBException
     {
         return this.doHasPrevious();
     }
     /**
      * Is the first record in the file?
      * @return false if file position is at first record.
      */
     public boolean doHasPrevious()
     {
         if ((m_iRecordStatus & DBConstants.RECORD_AT_BOF) != 0)
             return false; // Already at BOF (can't be one before)
         if ((m_iRecordStatus & DBConstants.RECORD_PREVIOUS_PENDING) != 0)
             return true;    // Already one waiting
         boolean bAtBOF = true;
         try   {
             FieldList record = this.move(DBConstants.PREVIOUS_RECORD);
             if (record == null)
                 bAtBOF = true;
             else if ((m_iRecordStatus & DBConstants.RECORD_AT_BOF) != 0)
                 bAtBOF = true;
             else if (this.isTable())
                 bAtBOF = this.isBOF();
             else
                 bAtBOF = false;     // Valid record!
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         if (bAtBOF)
             return false; // Does not have a next record
         m_iRecordStatus |= DBConstants.RECORD_PREVIOUS_PENDING;     // If next call is a moveNext(), return unchanged!
         return true;        // Yes, a next record exists.
     }
     /**
      * Is the record at the Start of the file?
      * @return true if file position is one before the first record.
      */
     public boolean isBOF()
     {
         boolean bFlag = ((m_iRecordStatus & DBConstants.RECORD_AT_BOF) != 0);
         if (this.isTable())
         {   // If this is a table, it can't handle starting and ending keys.. do it manually
             if (bFlag)
                 return bFlag;
             if (this.getRecord().getKeyArea(-1).isModified(DBConstants.START_SELECT_KEY))
             {
                 if (!bFlag)
                     bFlag = this.getRecord().checkParams(DBConstants.START_SELECT_KEY);
                 if (bFlag)
                     m_iRecordStatus |= DBConstants.RECORD_AT_BOF; // At BOF
             }
         }
         return bFlag;
     }
     /**
      * Is the last record in the file?
      * @return false if file position is at last record.
      */
     public boolean hasNext() throws DBException
     {
         return this.doHasNext();
     }
     /**
      * Is the last record in the file?
      * @return false if file position is at last record.
      */
     public boolean doHasNext() throws DBException
     {
         if ((m_iRecordStatus & DBConstants.RECORD_AT_EOF) != 0)
             return false; // Already at EOF, can't be one after
         if ((m_iRecordStatus & DBConstants.RECORD_NEXT_PENDING) != 0)
             return true;    // Already one waiting
         boolean bAtEOF = true;
         if (!this.isOpen())
             this.open();    // Make sure any listeners are called before disabling.
         Object[] rgobjEnabledFields = this.getRecord().setEnableNonFilter(null, false, false, false, false, true);
         FieldList record = null;
         try {
             record = this.move(DBConstants.NEXT_RECORD);
             if (record == null)
                 bAtEOF = true;
             else if ((m_iRecordStatus & DBConstants.RECORD_AT_EOF) != 0)
                 bAtEOF = true;
             else if (this.isTable())
                 bAtEOF = this.isEOF();
             else
                 bAtEOF = false;     // Valid record!
         } catch (DBException ex) {
             throw ex;
         } finally {
             this.getRecord().setEnableNonFilter(rgobjEnabledFields, false, false, false, bAtEOF, true);
         }
         if (bAtEOF)
             return false; // Does not have a next record
         m_iRecordStatus |= DBConstants.RECORD_NEXT_PENDING;     // If next call is a moveNext(), return unchanged!
         return true;        // Yes, a next record exists.
     }
     /**
      * Is the record at the End of the file?
      * @return true if file position is one after the last record.
      */
     public boolean isEOF()
     {
         boolean bFlag = ((m_iRecordStatus & DBConstants.RECORD_AT_EOF) != 0);
         if (this.isTable())
         {   // If this is a table, it can't handle starting and ending keys.. do it manually
             if (bFlag)
                 return bFlag;
             if (this.getRecord().getKeyArea(-1).isModified(DBConstants.END_SELECT_KEY))
             {
                 if (!bFlag)
                     bFlag = this.getRecord().checkParams(DBConstants.END_SELECT_KEY);
                 if (bFlag)
                     m_iRecordStatus |= DBConstants.RECORD_AT_EOF; // At EOF
             }
         }
         return bFlag;
     }
     /**
      * Do I lock or unlock this record or do I not call the lock/unlock method for this type?
      * @param record The optional record that I will be locking/unlocking.
      * @param iTrxType
      * @param bModeLock True if I want to lock, false for unlock
      * @return True if I should call the lock/unlock method
      */
     public boolean lockOnDBTrxType(Record record, int iTrxType, boolean bModeLock)
     {
         if (record == null)
             record = this.getRecord();
 
         boolean bCallLock = false;
         if (record != null)
         {
             if (bModeLock)
             {   // If I'm locking
                 if ((record.getOpenMode() & DBConstants.OPEN_LOCK_ON_EDIT_STRATEGY) != 0)
                     bCallLock = true;  // Usually lock record
                 if (iTrxType == DBConstants.LOCK_TYPE)
                 {
                     if ((record.getOpenMode() & DBConstants.OPEN_WAIT_FOR_LOCK_STRATEGY) == DBConstants.OPEN_WAIT_FOR_LOCK_STRATEGY)
                         bCallLock = true;
                     else
                         bCallLock = false;   // I don't lock otherwise
                 }
             }
             else
             {   // If I'm unlocking
                 if ((record.getOpenMode() & DBConstants.OPEN_DONT_CHANGE_CURRENT_LOCK_TYPE) == DBConstants.OPEN_DONT_CHANGE_CURRENT_LOCK_TYPE)
                     bCallLock = false;   // I don't unlock if this is set
                 else
                 {
                     if ((record.getOpenMode() & DBConstants.OPEN_LOCK_ON_EDIT_STRATEGY) != 0)
                         bCallLock = true;  // Usually unlock record
                     if (iTrxType == DBConstants.UPDATE_TYPE)
                     {
                         if ((record.getOpenMode() & DBConstants.OPEN_MERGE_ON_LOCK_STRATEGY) == DBConstants.OPEN_MERGE_ON_LOCK_STRATEGY)
                             bCallLock = true;
                         else
                             bCallLock = false;   // I don't lock otherwise
                     }
                 }
             }
         }
         return bCallLock;    // By default, I do lock on this type
     }
     /**
      * Utility method to lock the current record using the lock manager.
      * If doEdit() doesn't have native locking, just call this method.
      * @return Error
      * @throws DBException
      */
     public int lockCurrentRecord() throws DBException
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
         Record record = this.getRecord();
         Object bookmark = record.getHandle(DBConstants.BOOKMARK_HANDLE);
         if (bookmark != null)
         {   // Always
             String strUserName = null;
             BaseApplication app = null;
             org.jbundle.model.Task task = null;
             if (record.getRecordOwner() != null)
                 task = record.getRecordOwner().getTask();
             if (task != null)
                 app = (BaseApplication)record.getRecordOwner().getTask().getApplication();
             if (app != null)
                 strUserName = app.getProperty(DBParams.USER_NAME);
             Environment env = null;
             if (app != null)
                 env = app.getEnvironment();
             else
                 env = Environment.getEnvironment(null);
             iErrorCode = env.getLockManager().lockThisRecord(task, record.getDatabaseName(), record.getTableNames(false), bookmark, strUserName, record.getOpenMode());
         }
         return iErrorCode;
     }
     /**
      * Unlock this record if it is locked.
      * @param record The record to unlock.
      * @param The bookmark to unlock (all if null).
      * @return true if successful (it is usually okay to ignore this return).
      */
     public boolean unlockIfLocked(Record record, Object bookmark) throws DBException
     {
         if (record != null)
         {   // unlock record
             BaseApplication app = null;
             org.jbundle.model.Task task = null;
             if (record.getRecordOwner() != null)
                 task = record.getRecordOwner().getTask();
             if (task != null)
                 app = (BaseApplication)record.getRecordOwner().getTask().getApplication();
             Environment env = null;
             if (app != null)
                 env = app.getEnvironment();
             else
                 env = Environment.getEnvironment(null);
             return env.getLockManager().unlockThisRecord(task, record.getDatabaseName(), record.getTableNames(false), bookmark);
         }
         return true;
     }
     /**
      * Get this property for this table.
      * NOTE: This is only for this table, if you want global properties, use getDatabase().getProperty(xx);
      * @param strProperty The property key.
      * @return The property value.
      */
     public String getProperty(String strProperty)
     {
         String strValue = null;
         if (m_properties != null)
             strValue = (String)m_properties.get(strProperty);
         if (strValue == null)
         	if (this != this.getRecord().getTable())
         		return this.getRecord().getTable().getProperty(strProperty);
         return strValue;
     }
     /**
      * set this property for this table.
      * @param strProperty The property key.
      * @param strValue The property value.
      */
     public void setProperty(String strProperty, String strValue)
     {
         if (m_properties == null)
             m_properties = new Hashtable<String,Object>();
         if (strValue != null)
             m_properties.put(strProperty, strValue);
         else
             m_properties.remove(strProperty);
     }
     /**
      * Get this property for this table.
      * @param strProperty The property key.
      * @return The property value.
      */
     public Map<String, Object> getProperties()
     {
         if (this.getRecord() != null)
             if (((this.getRecord().getDatabaseType() & DBConstants.MAPPED) != 0)
                 || ((this.getRecord().getDatabaseType() & DBConstants.REMOTE_MEMORY) != 0))
         {
             if (m_properties == null)
                 m_properties = new Hashtable<String,Object>();
             m_properties.put(RecordMessageConstants.TABLE_NAME, this.getRecord().getTableNames(false)); // Don't call setProperty since ClientTable does a remote call
         }
         return m_properties;
     }
     /**
      * Check out this error, if it says table not found, create a new table.
      * @param ex error thrown.
      * @return true if file was not found and was successfully created.
      * @exception DBException Converts and returns SQLExceptions, or if no rows updated, throws INVALID_RECORD.
      */
     public boolean createIfNotFoundError(DBException ex)
         throws DBException
     {
         if (ex.getErrorCode() == DBConstants.FILE_NOT_FOUND)
             if (this.getRecord() != null)
                 if ((this.getRecord().getOpenMode() & DBConstants.OPEN_DONT_CREATE) == 0)
         {
             boolean loadInitialData = false;
             boolean useTemporaryFilename = false;
             if (this.getDatabase() != null)
             {
                 if (DBConstants.TRUE.equalsIgnoreCase(this.getDatabase().getProperty(DBConstants.LOAD_INITIAL_DATA)))
                     if ((this.getDatabase().getDatabaseOwner() == null) || (!DBConstants.FALSE.equalsIgnoreCase(this.getDatabase().getDatabaseOwner().getProperty(DBConstants.LOAD_INITIAL_DATA))))   // Global switch
                         loadInitialData = true;
                 if (DBConstants.TRUE.equalsIgnoreCase(this.getDatabase().getProperty(SQLParams.RENAME_TABLE_SUPPORT)))
                     useTemporaryFilename = true;
             }
 
             if (this.getRecord().isQueryRecord())
             {	// If this is a query, try to create all the tables of the query
             	for (int i = 0; i < this.getRecord().getRecordlistCount(); i++)
             	{
             		Record record = this.getRecord().getRecordlistAt(i);
             		try {	// TODO - I Should load into a temp table (see below)
             		if (!record.getTable().create())
             			return false;
             		} catch (DBException e) {
             			continue;	// Ignore if it already exists
             		}
             		if (loadInitialData)
                     {
                     	RecordOwner recordOwner = record.getRecordOwner();
                     	if (recordOwner == null)
                     		record.setRecordOwner(this.getRecord().getRecordOwner());
                         record.getTable().loadInitialData();
                     	if (recordOwner == null)
                     		record.setRecordOwner(recordOwner);
                     }
             	}
                 return true;   // Success!
             }
             String tableName = null;
             boolean bSuccess = true;
             try {
                 if (useTemporaryFilename)
                     if (loadInitialData)
                 {
                     tableName = this.getRecord().getTableNames(false);
                    this.getRecord().setTableNames(tableName + "_temp");
                 }
                 bSuccess = this.create();
                 if (bSuccess)
                     if (loadInitialData)
                 {
                     this.loadInitialData();
                     if (useTemporaryFilename)
                    	this.renameTable(tableName + "_temp", tableName);
                 }
             } catch (DBException e) {
                 // Exception usually means another thread is creating and loading the data for this table... just wait for it to finish.
                 if (tableName != null)
                     this.getRecord().setTableNames(tableName);
                 int oldOpenMode = this.getRecord().getOpenMode();
                 this.getRecord().setOpenMode(oldOpenMode | DBConstants.OPEN_DONT_CREATE);
                 int oldKeyArea = this.getRecord().getDefaultOrder();
                 int i = 0;
                 for (; i < 120; i++)    // 120 Seconds max.
                 {
                     try {
                         this.getRecord().setKeyArea(0);
                         this.doSeek(DBConstants.EQUALS);  // This will throw an exception if there is no table
                         break;  // Normal open = done loadingInitialData.
                     } catch (DBException e2) {
                         // Ignore this error, continue until the table exists
                     } finally {
                         this.getRecord().setKeyArea(oldKeyArea);                        
                     }
                     synchronized(this)
                     {
                         try {
                             this.wait(1000);
                         } catch (InterruptedException e1) {
                             e1.printStackTrace();
                         }
                     }
                 }
                 if (i == 20)
                 {
                 	oldOpenMode = oldOpenMode | DBConstants.OPEN_DONT_CREATE;	// Give up
                     e.printStackTrace();
                 }
                 this.getRecord().setOpenMode(oldOpenMode);
             } finally {
                 if (tableName != null)
                     this.getRecord().setTableNames(tableName);
             }
             return bSuccess;
         }
         return false;
     }
     /**
      * Rename this table
      * @param tableName
      * @param newTableName
      * @return
      */
     public boolean renameTable(String tableName, String newTableName) throws DBException
     {
         return false;	// Override this
     }
 }
