 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db.util;
 
 /**
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import java.util.Iterator;
 
 import org.jbundle.base.db.BaseDatabase;
 import org.jbundle.base.db.BaseTable;
 import org.jbundle.base.db.DatabaseException;
 import org.jbundle.base.db.KeyArea;
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.filter.SubFileFilter;
 import org.jbundle.base.db.shared.BaseSharedTable;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.model.DBException;
 import org.jbundle.model.db.Field;
 import org.jbundle.model.db.Rec;
 import org.jbundle.thin.base.db.FieldInfo;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.buff.VectorBuffer;
 
 
 /**
  * Hierarchical Table - A mirror table that retrieves data from the concrete db, then from the base db.
  */
 public class HierarchicalTable extends BaseSharedTable
 {
 
     /**
      * RecordList Constructor.
      */
     public HierarchicalTable()
     {
         super();
     }
     /**
      * RecordList Constructor.
      */
     public HierarchicalTable(BaseDatabase database, Record record)
     {
         this();
         this.init(database, record);
     }
     /**
      * init.
      */
     public void init(BaseDatabase database, Record record)
     {
         BaseTable table = record.getTable();
         
         super.init(database, record);
 
         this.addTable(table);   // Be careful, the next table is on the table list
         this.setCurrentTable(table);
     }
     /**
      * Free.
      */
     public void free()
     {
         super.free();
     }
     /**
      * This is usually called from thin code, so make sure you return the CURRENT record.
      * @return The record from the current table.
      */
     public Record getRecord()
     {
         return this.getNextTable().getRecord(); // Always use the main db's record
     }
     /**
      * Do the physical Open on this table (requery the table).
      * @exception DBException File exception.
      */
     public void open() throws DBException
     {
         super.open();
         this.getRecord().setEditMode(DBConstants.EDIT_NONE);    // Being careful
         Iterator<BaseTable> iterator = this.getTables();
         while (iterator.hasNext())
         {
             BaseTable table = iterator.next();
             if ((table != null) && (table != this.getNextTable()))
             {
                 this.syncRecordToBase(table.getRecord(), this.getRecord()); // Note: I am syncing the base to the alt here
                 table.open();
             }
         }
     }
     /**
      * Close the recordset.
      */
     public void close()
     {
         super.close();
     }
 
     
 //--------------------------------------------------------------------------
     
     
     /**
      * Is this a table (or SQL) type of "Table"?
      * This flag determines when you hit the end of a query. If set, every record is compared
      * against the last record in the query. If not, the only EOF determines the end (ie., a SQL query).
      * Override this and return false where the DB returns the complete query.
      */
     public boolean isTable()
     {
         return false;   // I am not a table
     }
     /**
      * Create/Clear the current object (Always called from the record class).
      * @exception DBException File exception.
      */
     public void addNew() throws DBException
     {
         //Record recMain = this.getRecord();
         this.setCurrentTable(this.getNextTable());  // Only for main table
         super.addNew();     // Only need to init the main record
     }
     /**
      * Add this record (Always called from the record class).
      * @exception DBException File exception.
      */
     public void add(Rec fieldList) throws DBException
     {
         if (this.getCurrentTable() != this.getNextTable())
            throw new DatabaseException("Can't add new table in alternate table");  // Never
         super.add(fieldList);     // Only need to add the main record
     }
     /**
      * doRecordChange Method.
      * If this is an update or add type, grab the bookmark.
      * @param field The field that is changing.
      * @param iChangeType The type of change.
      * @param bDisplayOption not used here.
      */
     public int doRecordChange(FieldInfo field, int iChangeType, boolean bDisplayOption)
     {
         int iErrorCode = super.doRecordChange(field, iChangeType, bDisplayOption);
         return iErrorCode;
     }
     /**
      * Lock the current record.
      * This method responds differently depending on what open mode the record is in:
      * OPEN_DONT_LOCK - A physical lock is not done. This is usually where deadlocks are possible
      * (such as screens) and where transactions are in use (and locks are not needed).
      * OPEN_LOCK_ON_EDIT - Holds a lock until an update or close. (Update crucial data, or hold records for processing)
      * Returns false is someone alreay has a lock on this record.
      * OPEN_WAIT_FOR_LOCK - Don't return from edit until you get a lock. (ie., Add to the total).
      * Returns false if someone has a hard lock or time runs out.
      * @return true if successful, false is lock failed.
      * @exception DBException FILE_NOT_OPEN
      * @exception DBException INVALID_RECORD - Record not current.
      */
     public int edit() throws DBException
     {
         if (this.getCurrentTable() != this.getNextTable())
             throw new DatabaseException("Can't lock record in alternate table");  // Never
         SubFileFilter listener = (SubFileFilter)this.getRecord().getListener(SubFileFilter.class, false);
         boolean state = true;
         if (listener != null)
         	if (listener.getMainRecord().getTable().getCurrentTable() != listener.getMainRecord().getTable())
         		state = listener.setEnabledListener(false);		// This handles a very obscure case: if this is a sub of a record in another table, can't lock main
         int iErrorCode = DBConstants.NORMAL_RETURN;
         try {
 	        iErrorCode = super.edit();
         } catch (DBException e) {
 	        throw e;
         } finally {
         	if (listener != null)
         		listener.setEnabledListener(state);
         }
         return iErrorCode;
     }
     /**
      * Update this record (Always called from the record class).
      * @exception DBException File exception.
      */
     public void set(Rec fieldList) throws DBException
     {
         if (this.getCurrentTable() != this.getNextTable())
             throw new DatabaseException("Can't update record in alternate table");  // Never
         super.set(fieldList);     // Only need to update the main record
     }
     /**
      * Update this record (Always called from the record class).
      * @exception DBException File exception.
      */
     public void remove() throws DBException
     {
         if (this.getCurrentTable() != this.getNextTable())
             throw new DatabaseException("Can't remove record in alternate table");  // Never
         super.remove();     // Only need to update the main record
     }
     /**
      * Is the first record in the file?
      * @return false if file position is at first record.
      */
     public boolean hasPrevious()
     {
         return this.doHasPrevious();
     }
     /**
      * Is there another record (is this not the last one)?
      */
     public boolean hasNext() throws DBException
     {
         return this.doHasNext();
     }
     /**
      * Move the position of the record.
      * @exception DBException File exception.
      */
     public FieldList move(int iRelPosition) throws DBException
     {
         if (this.getRecord() != this.getCurrentTable().getRecord())
         {
         	if (this.getRecord().getEditMode() == DBConstants.EDIT_CURRENT)
         	{
 	        	this.getRecord().getKeyArea(DBConstants.MAIN_KEY_AREA).reverseKeyBuffer(null, DBConstants.TEMP_KEY_AREA);
 	        	this.getRecord().getKeyArea().reverseKeyBuffer(null, DBConstants.TEMP_KEY_AREA);	// Restore key (since this is current, it is guaranteed not to be next)
         	}
         }
     	Record recLastCurrent = this.getCurrentTable().getRecord();
         FieldList record = this.moveThruMultipleTables(iRelPosition);
     	if (this.getRecord() != record)
         {
             if (this.getRecord() == recLastCurrent)
             {
             	this.getRecord().getKeyArea().setupKeyBuffer(null, DBConstants.TEMP_KEY_AREA);	// Save this for compare later
 	        	this.getRecord().getKeyArea(DBConstants.MAIN_KEY_AREA).reverseKeyBuffer(null, DBConstants.TEMP_KEY_AREA);
             }
             this.syncRecordToBase(this.getRecord(), (Record)record);
         }
     	else
     	{
             if (this.getRecord() != recLastCurrent)
             {	// The record was lost when I synced it
             	Object bookmark = this.getRecord().getHandle(DBConstants.BOOKMARK_HANDLE);
             	this.getRecord().setHandle(bookmark, DBConstants.BOOKMARK_HANDLE);
             }
     	}
         return (record == null) ? null : this.getRecord();
     }
     /**
      * Read the record that matches this record's current key.<p>
      * @exception DBException File exception.
      */
     public boolean seek(String strSeekSign) throws DBException
     {
         Record recMain = this.getRecord();
         BaseBuffer vector = new VectorBuffer(null);
         KeyArea keyMain = recMain.getKeyArea(recMain.getDefaultOrder());
         keyMain.setupKeyBuffer(vector, DBConstants.FILE_KEY_AREA);
         this.setCurrentTable(this.getNextTable());
         
         boolean bSuccess = super.seek(strSeekSign);
         if (!bSuccess)
         {   // Not found in the main table, try the other tables
             Iterator<BaseTable> iterator = this.getTables();
             while (iterator.hasNext())
             {
                 BaseTable table = iterator.next();
                 if ((table != null) && (table != this.getNextTable()))
                 {
                     Record recAlt = table.getRecord();
                     recAlt.setKeyArea(recMain.getDefaultOrder());
                     KeyArea keyAlt = recAlt.getKeyArea(recMain.getDefaultOrder());
                     vector.resetPosition();
                     keyAlt.reverseKeyBuffer(vector, DBConstants.FILE_KEY_AREA);     // Move these keys back to the record
 
                     bSuccess = recAlt.seek(strSeekSign);
                     if (bSuccess)
                     {
                         this.syncRecordToBase(recMain, recAlt);
                         this.setCurrentTable(table);
                         break;
                     }
                 }
             }
         }
         vector.free();
         return bSuccess;
     }
     /**
      * Get the ObjectIDHandle to the last modified or added record.
      * This uses some very inefficient code... override if possible.
      */
     public Object getLastModified(int iHandleType)
     {
         Object bookmark = super.getLastModified(iHandleType);
         return bookmark;
     }
     /**
      * Reposition to this record Using this bookmark.
      * @exception DBException File exception.
      */
     public FieldList setHandle(Object bookmark, int iHandleType) throws DBException
     {
         Record recMain = this.getRecord();
         this.setCurrentTable(this.getNextTable());
         FieldList record = super.setHandle(bookmark, iHandleType);
         
         if (record == null)
         {   // Not found in the main table, try the other tables
             Iterator<BaseTable> iterator = this.getTables();
             while (iterator.hasNext())
             {
                 BaseTable table = iterator.next();
                 if ((table != null) && (table != this.getNextTable()))
                 {
                     Record recAlt = table.getRecord();
                     record = recAlt.setHandle(bookmark, iHandleType);
                     if (record != null)
                     {
                         this.syncRecordToBase(recMain, recAlt);
                         this.setCurrentTable(table);
                         break;
                     }
                 }
             }
         }
         return record;
     }
     /**
      * Get a unique key that can be used to reposition to this record.
      * @exception DBException File exception.
      */
     public Object getHandle(int iHandleType) throws DBException
     {
         Object bookmark = this.getCurrentTable().getHandle(iHandleType);
         return bookmark;
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
      * @exception DBException File exception.
      */
     public void fieldToData(Field field) throws DBException
     {
         super.fieldToData(field);
     } 
     /**
      * Move the data source buffer to all the fields.<p>
      *  Make sure you do the following steps:
      *  1) Move the data fields to the correct record data fields.
      *  2) Save the data source or set to null, so I can cache the source if necessary.
      *  3) Save the objectID if it is not an Integer type, so I can serialize the source of this object.
      * @exception DBException File exception.
      */
     public int dataToFields(Rec record) throws DBException
     {
         return super.dataToFields(record);
     }
     /**
      * Move the output buffer to this field (Override this to use non-standard buffers).
      * Warning: Check to make sure the field.isSelected() before moving data!
      * @param field The field to move the data to.
      * @return The error code (From field.setData()).
      * @exception DBException File exception.
      */
     public int dataToField(Field field) throws DBException
     {
         return super.dataToField(field);
     } 
 }
