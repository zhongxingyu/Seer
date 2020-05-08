 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.db.client;
 
 import java.util.Vector;
 
 import org.jbundle.model.DBException;
 import org.jbundle.model.RemoteException;
 import org.jbundle.model.db.Rec;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.KeyAreaInfo;
 import org.jbundle.thin.base.remote.RemoteTable;
 import org.jbundle.thin.base.util.ThinUtil;
 
 
 /**
  * An RemoteFieldTable is a link to this remote table.
  */
 public class RemoteFieldTable extends VectorFieldTable
 {
     /**
      * A reference to the remote table.
      */
     protected RemoteTable m_tableRemote = null;
     /**
      * The object to synchronize calls on.
      */
     protected Object m_syncObject = null;
     /**
      * The current record position.
      */
     protected int m_iCurrentRecord = -1;
     /**
      * The number of records accessed so far.
      */
     protected int m_iRecordsAccessed = -1;
     /**
      * The bookmark of the last added record (if supported by the remote session).
      */
     protected Object m_objLastModBookmark = null;
 
     /**
      * Constructor.
      */
     public RemoteFieldTable()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The record to handle.
      */
     public RemoteFieldTable(FieldList record)
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
     public RemoteFieldTable(FieldList record, RemoteTable tableRemote, Object syncObject)
     {
         this();
         this.init(record, tableRemote, syncObject);
     }
     /**
      * Constructor.
      * @param record The record to handle.
      * @param tableRemote The remote table.
      * @param server The remote server (only used for synchronization).
      */
     public void init(Rec record, RemoteTable tableRemote, Object syncObject)
     {
         super.init(record);
         this.setRemoteTable(tableRemote, syncObject);
     }
     /**
      * I'm done with the model, free the resources.
      */
     public void free()
     {
         try   {
             if (m_tableRemote != null)
                 m_tableRemote.freeRemoteSession();
         } catch (RemoteException ex) {
             ex.printStackTrace();
         }
         m_tableRemote = null;
         super.free();
     }
     /**
      * Reset the current position and open the file.
      */
     public void open() throws DBException
     {
         m_objLastModBookmark = null;
         try   {
     // FROM is automatic, since the remote BaseRecord is exactly the same as this one
     // ORDER BY
             String strKeyArea = this.getRecord().getKeyName();
     // ASC / DESCending
             KeyAreaInfo keyArea = this.getRecord().getKeyArea(-1);
             boolean bKeyOrder = Constants.ASCENDING;
             if (keyArea != null)
                 bKeyOrder = keyArea.getKeyOrder();
     // Open mode
             int iOpenMode = this.getRecord().getOpenMode();
     // SELECT (fields to select - all)
             String strFields = null;
     // WHERE aaa >=
             Object objInitialKey = null;
     // WHERE aaa <=
             Object objEndKey = null;
     // WHERE XYZ
             byte[] byFilter = null;
 
             m_tableRemote.open(strKeyArea, iOpenMode, bKeyOrder, strFields, objInitialKey, objEndKey, byFilter);
             m_iCurrentRecord = -1;
             m_iRecordsAccessed = 0;
             super.open();
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Close this table.
      * In this implementation this does nothing, because the remote open code always closes first.
      */
     public void close()
     {
         m_iCurrentRecord = -1;
         m_iRecordsAccessed = 0;
         m_objLastModBookmark = null;
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
         try   {
             m_objLastModBookmark = m_tableRemote.add(this.getDataSource(), record.getOpenMode());
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
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
         try   {
             int iOpenMode = this.getRecord().getOpenMode();  // It is common to change this before open
             return m_tableRemote.edit(iOpenMode);    // Only call if edit is supported by remote db
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Set the current record to this (new) record.
      * NOTE: In the thin model, it is not necessary to call edit() before set.
      * @exception Throws an exception if there is no current record.
      */
     public void doSet(Rec record) throws DBException
     {
         try   {
             m_objLastModBookmark = null;
             m_tableRemote.set(this.getDataSource(), record.getOpenMode());
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Remove the current record.
      */
     public void doRemove() throws DBException
     {
         try   {
             m_objLastModBookmark = null;
             m_tableRemote.remove(null, this.getRecord().getOpenMode());
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Does this list have a next record?
      * @return true if there is a next record to read.
      */
     public boolean hasNext() throws DBException
     {
         if (m_iCurrentRecord >= m_iRecordsAccessed)
             return true;
         Object record = this.next();
         if (record == null)
             return false;
         else
         {
             m_iRecordsAccessed--;   // Offically this record has not been accessed
             return true;
         }
     }
     /**
      * Get the next record (return a null at EOF).
      * Note: Remember to set the data source before returning a NORMAL_RETURN.
      * @param iRelPosition The relative records to move.
      * @return A record status (NORMAL_RETURN means the move was successful).
      */
     public int doMove(int iRelPosition) throws DBException
     {
         try   {
             if (m_iCurrentRecord == m_iRecordsAccessed)
             {   // Impossible to be at the record count, unless hasNext has moved to the next record
                 m_iRecordsAccessed++;
                 if (this.getDataSource() != null) // Always
                     return Constants.NORMAL_RETURN;   // This is already the current record
             }
             Object objData = m_tableRemote.doMove(iRelPosition, 1);
             if (objData instanceof Vector)
             {
                 this.setDataSource(objData);
 
                 m_iCurrentRecord++;
                 m_iRecordsAccessed++;
                 return Constants.NORMAL_RETURN;   // Normal return
             }
             else if (objData instanceof Number)
                 return ((Number)objData).intValue();    // Usually EOF
             else
                 return Constants.ERROR_RETURN;  // Never
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Does this list have a previous record?
      */
     public boolean hasPrevious() throws DBException
     {
         if (m_iCurrentRecord >= m_iRecordsAccessed)
             return true;
         Object record = this.previous();
         if (record == null)
             return false;
         else
         {
             m_iRecordsAccessed--;   // Offically this record has not been accessed
             return true;
         }
     }
     /**
      * Read the record that matches this record's current key.
      * @param strSeekSign The seek sign (defaults to '=').
      * @return true if successful.
      *  Remember to set m_dataSource if dataToFields needs to move from the data object.
      * @exception DBException File exception.
      */
     public boolean doSeek(String strSeekSign) throws DBException
     {
         m_objLastModBookmark = null;
         String strKeyArea = this.getRecord().getKeyName();
         if (strKeyArea == null)
             strKeyArea = Constants.PRIMARY_KEY;
         int iOpenMode = this.getRecord().getOpenMode();
 
         Object objKeyData = null;
         KeyAreaInfo keyArea = this.getRecord().getKeyArea(-1);
         if (keyArea != null)
         {
             if (keyArea.getKeyFields() > 1)
             {
                 objKeyData = new Vector<Object>();
                 for (int i = 0; i < keyArea.getKeyFields(); i++)
                 {
                     ((Vector)objKeyData).add(keyArea.getField(i));
                 }
             }
             else
                 objKeyData = keyArea.getField(Constants.MAIN_KEY_FIELD).getData();
         }
         else
             objKeyData = this.getRecord().getField(Constants.MAIN_FIELD).getData();
         try   {
             Object objData = m_tableRemote.seek(strSeekSign, iOpenMode, strKeyArea, null, objKeyData);
             if (objData instanceof Vector)
             {
                 this.setDataSource(objData);
                 return true;    // Success
             }
             else if (objData instanceof Boolean)
                 return ((Boolean)objData).booleanValue();
             else
                 return false; // Never
         } catch (RemoteException ex) {
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Returns an attribute value for the cell at columnIndex and rowIndex.
      */
     public Object doGet(int iRowIndex) throws DBException
     {
         try   {
             m_objLastModBookmark = null;
             return m_tableRemote.get(iRowIndex, 1);
         } catch (RemoteException ex) {
             ex.printStackTrace();
             throw new DBException(ex.getMessage());
         }
     }
     /**
      * Get the Handle to the last modified or added record.
      * This uses some very inefficient (and incorrect) code... override if possible.
      * NOTE: There is a huge concurrency problem with this logic if another person adds
      * a record after you, you get the their (wrong) record, which is why you need to
      * provide a solid implementation when you override this method.
      * @param iHandleType The handle type.
      * @return The bookmark.
      */
     public Object getLastModified(int iHandleType) throws DBException
     {
         if (m_objLastModBookmark != null)
             return m_objLastModBookmark;    // Override this to supply last modified.
         else
         {
             try   {
                 return m_tableRemote.getLastModified(iHandleType);
             } catch (RemoteException ex) {
                 ex.printStackTrace();
                 throw new DBException(ex.getMessage());
             }            
         }
     }
     /**
      * Set the remote table reference.
      */
     public void setRemoteTable(RemoteTable tableRemote, Object syncObject)
     {
         if (syncObject != null)
             tableRemote = new SyncRemoteTable(tableRemote, syncObject);   // Synchronize all calls
         m_tableRemote = tableRemote;
         m_syncObject = syncObject;
     }
     /**
      * Get the remote table reference.
      * If you want the remote table session, call this method with Remote.class.
      * @classType The base class I'm looking for (If null, return the next table on the chain) 
      * @return The remote table reference.
      */
     public RemoteTable getRemoteTableType(Class<?> classType)
     {
         return ThinUtil.getRemoteTableType(m_tableRemote, classType);
     }
     /**
      * This is just a convenience method to get the server for this APPLICATION, so I can synchronize the remote access.
      */
     public Object getServer()
     {
         return m_syncObject;
     }
 }
