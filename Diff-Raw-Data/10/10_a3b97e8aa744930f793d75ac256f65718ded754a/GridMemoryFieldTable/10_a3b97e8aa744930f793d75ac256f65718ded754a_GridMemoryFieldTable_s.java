 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.db.mem;
 
 import org.jbundle.model.DBException;
 import org.jbundle.model.db.Rec;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.FieldTable;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.mem.base.PTable;
 import org.jbundle.thin.base.db.mem.base.PhysicalDatabaseParent;
 import org.jbundle.thin.base.db.util.ArrayCache;
 
 /**
 * An MemoryFieldTable is a link to a memory-based raw data (PTable) table.
 * Genurally, you want to use this class rather than MemoryFieldTable
  * as this class adds the capability to handle the get() command.
  */
 public class GridMemoryFieldTable extends MemoryFieldTable
 {
     /**
      * If this current index is the value, the current index is undefined.
      */
     public static final int NO_INDEX = -2;
     /**
      * The grid record cache (caches the BaseBuffers).
      */
     protected ArrayCache m_arrayCache = null;
     /**
      * The current location in a move query.
      */
     protected int m_iPhysicalIndex = NO_INDEX;
     /**
      * The last read record index;
      */
     protected int m_iLogicalIndex = NO_INDEX;
     /**
      * The last read record index;
      */
     protected int m_iEndOfFileIndex = NO_INDEX;
 
     /**
      * Constructor.
      */
     public GridMemoryFieldTable()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The record to handle.
      */
     public GridMemoryFieldTable(FieldList record)
     {
         this();
         this.init(record, null, null);
     }
     /**
      * Constructor.
      * @param record The record to handle.
      * @param tableRemote The remote table.
      * @param server The remote server (only used for synchronization).
      */
     public GridMemoryFieldTable(FieldList record, PTable tableRemote, PhysicalDatabaseParent dbOwner)
     {
         this();
         this.init(record, tableRemote, dbOwner);
     }
     /**
      * Constructor.
      * @param record The record to handle.
      * @param tableRemote The remote table.
      * @param server The remote server (only used for synchronization).
      */
     public void init(Rec record, PTable tableRemote, PhysicalDatabaseParent dbOwner)
     {
         super.init(record, tableRemote, dbOwner);
         m_iPhysicalIndex = NO_INDEX;
         m_iLogicalIndex = NO_INDEX;
     }
     /**
      * I'm done with the model, free the resources.
      */
     public void free()
     {
         super.free();
     }
     /**
      * Reset the current position and open the file.
      */
     public void open() throws DBException
     {
         super.open();
         m_iPhysicalIndex = -1;  // A open resets the current query to before the first position.
         m_iLogicalIndex = -1;
         m_iEndOfFileIndex = NO_INDEX; // Unknown
     }
     /**
      * Close this table.
      * In this implementation this does nothing, because the remote open code always closes first.
      */
     public void close()
     {
         super.close();
     }
     /**
      * Add this record to this table.
      * Add this record to the table and set the current edit mode to NONE.
      * @param record The record to add.
      * @throws Exception
      */
     public void doAdd(Rec record) throws DBException
     {
         super.doAdd(record);
         if (m_arrayCache != null)
             if (m_iEndOfFileIndex != NO_INDEX)
         {
             m_iEndOfFileIndex++;
             m_arrayCache.set(m_iEndOfFileIndex, this.getDataSource());  // Show the current record as deleted.
         }
     }
     /**
      * Lock the current record (Always called from the record class).
      * NOTE: For a remote table it is not necessary to call edit, as edit will
      * be called automatically by the set() call.
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
     public int doEdit() throws DBException
     {
         return super.doEdit();  // Only call if edit is supported by remote db
     }
     /**
      * Set the current record to this (new) record.
      * NOTE: In the thin model, it is not necessary to call edit() before set.
      * @exception Throws an exception if there is no current record.
      */
     public void doSet(Rec record) throws DBException
     {
         super.doSet(record);
         if (m_arrayCache != null)
             if (m_iLogicalIndex != NO_INDEX)
             {
                 m_arrayCache.set(m_iLogicalIndex, this.getDataSource());    // Show the current record as deleted.
             }
     }
     /**
      * Remove the current record.
      */
     public void doRemove() throws DBException
     {
         super.doRemove();
         if (m_arrayCache != null)
             if (m_iLogicalIndex != -1)
                 m_arrayCache.set(m_iLogicalIndex, null);    // Show the current record as deleted.
     }
     /**
      * Get the next record (return a null at EOF).
      * Note: Remember to set the data source before returning a NORMAL_RETURN.
      * @param iRelPosition The relative records to move.
      * @return A record status (NORMAL_RETURN means the move was successful).
      */
     public int doMove(int iRelPosition) throws DBException
     {
         int iRecordStatus = super.doMove(iRelPosition);
         if ((iRecordStatus == Constants.NORMAL_RETURN)
             || (iRecordStatus == FieldTable.DELETED_RECORD.intValue()))   // Deleted record
         {
             if (m_arrayCache != null)
                 if (m_iPhysicalIndex != NO_INDEX)
             {
                 m_iPhysicalIndex = m_iPhysicalIndex + iRelPosition;
                 Object data = null;
                 if (iRecordStatus == Constants.NORMAL_RETURN)
                     data = this.getDataSource();
                 m_arrayCache.set(m_iPhysicalIndex, data); // Update the current record.
             }
         }
         else
         {
             if (iRelPosition > 0)
             {
                 m_iEndOfFileIndex = m_iPhysicalIndex;
                 m_iPhysicalIndex = NO_INDEX;
             }
             else
                 if (m_arrayCache != null)
                     if (m_iPhysicalIndex != NO_INDEX)
                         m_arrayCache.set(m_iPhysicalIndex, null); // Show the current record as deleted.
         }
         return iRecordStatus;
     }
     /**
      * Read the record that matches this record's current key.
      * @param strSeekSign The seek sign (defaults to '=').
      * @return true if successful.
      * Remember to set m_dataSource if dataToFields needs to move from the data object.
      * @exception DBException File exception.
      */
     public boolean doSeek(String strSeekSign) throws DBException
     {
         m_iPhysicalIndex = NO_INDEX;    // A seek messes up the current query.
         m_iLogicalIndex = NO_INDEX;
         return super.doSeek(strSeekSign);
     }
     /**
      * Returns the record at this absolute row position.
      * Be careful, if a record at a row is deleted, this method will return a new
      * (empty) record, so you need to check the record status before updating it.
      * @param iRelPosition Absolute position of the record to retrieve.
      * @return The record at this location (or an Integer with the record status or null if not found).
      * @exception DBException File exception.
      */
     public Object doGet(int iRowIndex) throws DBException
     {
         if (m_arrayCache == null)
             m_arrayCache = new ArrayCache(-1);
         int iIncrement = -1;
         Object data = m_arrayCache.get(iRowIndex);
         if (data == null)
         {
             if (m_iEndOfFileIndex != NO_INDEX)
                 if (iRowIndex > m_iEndOfFileIndex)
                     return null;    // Past EOF
             int iStart = m_arrayCache.getStartIndex();
             int iEnd = m_arrayCache.getEndIndex();
             for (; iStart < iEnd; iStart++)
             {
                 if (m_arrayCache.get(iStart) != null)
                     break;
             }
             for (; iStart < iEnd; iEnd--)
             {
                 if (m_arrayCache.get(iEnd) != null)
                     break;
             }
             if ((iRowIndex >= iStart) && (iRowIndex <= iEnd))
                 return FieldTable.DELETED_RECORD; // Deleted record
             if (iStart <= iEnd)
                 if (iRowIndex > iEnd)
             {
                 iIncrement = iStart;
                 iStart = iEnd;
                 iEnd = iIncrement;
                 iIncrement = +1;
             }
             data = m_arrayCache.get(iStart);
             if (data == null)
             {
                 this.close();
                 m_iPhysicalIndex = -1;  // Starting from the beginning
                 iIncrement = +1;
             }
             else
             {
                 this.setDataSource(null);
                 this.getPTable().saveCurrentKeys(this, (BaseBuffer)data, false);
                 m_iPhysicalIndex = iStart;  // Starting from the beginning
             }
             for (int i = m_iPhysicalIndex; i != iRowIndex; i = i + iIncrement)
             {
                 if (this.move(iIncrement) == null)  // Moveto automatically caches the records as they are read
                     break;
             }
             data = m_arrayCache.get(iRowIndex);
         }
         if (data != null)
         {
             this.getPTable().saveCurrentKeys(this, (BaseBuffer)this.getDataSource(), false);
             m_iLogicalIndex = iRowIndex;
         }
         else
             if (iIncrement == -1)
                 data = FieldTable.DELETED_RECORD;
         return data;
     }
 }
