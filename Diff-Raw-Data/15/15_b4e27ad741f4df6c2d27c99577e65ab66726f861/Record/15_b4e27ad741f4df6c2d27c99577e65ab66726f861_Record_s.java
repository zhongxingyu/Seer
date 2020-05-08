 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db;
 
 /**
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.ListResourceBundle;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import org.jbundle.base.db.event.ClearFieldReferenceOnCloseHandler;
 import org.jbundle.base.db.event.FileListener;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.field.BaseListener;
 import org.jbundle.base.field.CounterField;
 import org.jbundle.base.field.IntegerField;
 import org.jbundle.base.field.ListenerOwner;
 import org.jbundle.base.field.ReferenceField;
 import org.jbundle.base.field.StringField;
 import org.jbundle.base.field.event.FieldDataScratchHandler;
 import org.jbundle.base.field.event.FieldListener;
 import org.jbundle.base.field.event.ReadSecondaryHandler;
 import org.jbundle.base.message.record.GridRecordMessageFilter;
 import org.jbundle.base.message.record.RecordMessage;
 import org.jbundle.base.message.record.RecordMessageFilter;
 import org.jbundle.base.message.record.RecordMessageHeader;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.MenuConstants;
 import org.jbundle.base.model.RecordOwner;
 import org.jbundle.base.model.ResourceConstants;
 import org.jbundle.base.model.ScreenConstants;
 import org.jbundle.base.model.ScreenModel;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.model.App;
 import org.jbundle.model.DBException;
 import org.jbundle.model.RecordOwnerParent;
 import org.jbundle.model.Task;
 import org.jbundle.model.db.DatabaseOwner;
 import org.jbundle.model.db.Field;
 import org.jbundle.model.db.Rec;
 import org.jbundle.model.message.MessageManager;
 import org.jbundle.model.screen.BaseAppletReference;
 import org.jbundle.model.screen.ComponentParent;
 import org.jbundle.model.screen.ScreenLoc;
 import org.jbundle.model.screen.ScreenParent;
 import org.jbundle.model.util.Constant;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.FieldInfo;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.FieldTable;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.buff.VectorBuffer;
 import org.jbundle.thin.base.message.BaseMessage;
 import org.jbundle.thin.base.message.BaseMessageFilter;
 import org.jbundle.thin.base.message.JMessageListener;
 import org.jbundle.thin.base.remote.LocalTask;
 import org.jbundle.thin.base.remote.RemoteException;
 import org.jbundle.thin.base.remote.RemoteTable;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.main.db.DatabaseInfo;
 import org.jbundle.util.osgi.BundleConstants;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 /**
  * Record - Pure public recordset object.
  * <p />    Base object for all recordsets and queries.<p />
  * <pre>
  * The record class is roughly based on the collection interface:
  * <p>Record Class: <br>
  * getTable <br>
  * getObjectID <br>
  * addNew - New empty record
  * add - Add new record
  * edit - lock current record
  * set - Update record
  * delete - Delete record
  * Table class:
  *  move(iRelPos) - Moves relative to the current position.
  *  seek() - Seeks a record using this key.
  *  getHandle - Gets a unique record identifier. (Must be serializable).
  *  setHandle - Seeks a record using this unique key.
  *  getEditMode() - Get status of this record.
  * </pre>
  */
 public class Record extends FieldList
     implements ListenerOwner
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
      * This is the first listener in the record listener chain.
      */
     protected FileListener m_listener = null;   // Linked list of file behaviors
     /**
      * List of all Dependent screens (doesn't include m_recordOwner).
      */
     protected Vector<ScreenParent> m_depScreens = null;
     /**
      * Auto-display fields on change?
      */
     protected boolean m_bDisplayOption = true;
     /**
      * Don't set record messages to your client?
      */
     protected boolean m_bSupressRemoteMessages = false;
 
     /**
      * Constructor.
      */
     public Record()
     {
         super();
     }
     /**
      * Constructor.
      * @param recordOwner The recordowner to add this record to.
      */
     public Record(RecordOwner recordOwner)
     {
         this();
         this.init(recordOwner);
     }
     /**
      * Constructor.
      * @param recordOwner The recordowner to add this record to.
      */
     public void init(Object recordOwner)
     {   // This is just for safety (if the FieldList method is called.
         this.init((RecordOwner)recordOwner);
     }
     /**
      * Constructor.
      * @param recordOwner The recordowner to add this record to.
      */
     public void init(RecordOwner recordOwner)
     {
         m_dbEditMode = Constants.EDIT_NONE;
         m_listener = null;
         m_bDisplayOption = true;
 
         m_depScreens = null;        // List of all Dependent screens (doesn't include m_recordOwner)
         m_recordOwner = recordOwner;
         this.getTable();    // This will set up a table using the recordowner if it exists.
 
         if (recordOwner != null)
             recordOwner.addRecord(this, false);
 
         super.init(recordOwner);    // Set up the fields.
         this.selectFields();
         if ((this.getDatabaseType() & DBConstants.BASE_TABLE_CLASS) != 0)
         {
             if ((this.getDatabaseType() & DBConstants.SHARED_TABLE) != 0)  // But not shared (don't need multitable).
             {
                 if ((this.getMasterSlave() & RecordOwner.MASTER) != 0)   // Slaves don't need this helper class.
                 {
                     if ((this.getRecordOwner() == null)
                         || ((this.getRecordOwner().getMasterSlave() & RecordOwner.MASTER) != 0))
                             new org.jbundle.base.db.shared.SharedBaseRecordTable(null, this);    // Special - base class of a shared table
                 }
             }
             else
                 new org.jbundle.base.db.shared.MultiTable(null, this);     // Special - base class - Add the overriding classes.
         }
         if (this.getTable() != null)
             if (this.getTable().getDatabase() != null)
                 if (this.getTable().getDatabase().getDatabaseOwner() != null)
                 if (this.getTable().getDatabase().getDatabaseOwner().getEnvironment() != null)
                 if ((DBConstants.YES.equalsIgnoreCase(((Environment)this.getTable().getDatabase().getDatabaseOwner().getEnvironment()).getProperty(DBParams.LOG_TRXS))) || (DBConstants.TRUE.equalsIgnoreCase(((Environment)this.getTable().getDatabase().getDatabaseOwner().getEnvironment()).getProperty(DBParams.LOG_TRXS))))
             if ((this.getMasterSlave() & RecordOwner.SLAVE) != 0)   // Slaves do the logging.
                 if ((this.getDatabaseType() & DBConstants.DONT_LOG_TRX) == 0)
         {
             if ((this.getRecordOwner() == null)
                 || ((this.getRecordOwner().getMasterSlave() & RecordOwner.SLAVE) != 0))
                     new org.jbundle.base.db.util.log.MessageLogTable(null, this);    // Special - base class of a shared table
         }
         m_iDefaultOrder = Constants.MAIN_KEY_AREA;      // Key Area to use on next operation
         this.addListeners();
     }
     /**
      * Free.
      */
     public void free()
     {
         this.handleRecordChange(DBConstants.BEFORE_FREE_TYPE);
         
         if (m_depScreens != null)
         {
             while (m_depScreens.size() > 0)
             {
                 ComponentParent screen = m_depScreens.elementAt(0);
                 screen = screen.getRootScreen();
                 screen.free();
             }
             m_depScreens.removeAllElements();
             m_depScreens = null;
         }
 
         m_dbEditMode |= DBConstants.EDIT_CLOSE_IN_FREE;   // This is a cludge... signals tables that this is the last close()!
         this.close();
 
         if (m_recordOwner != null)
             ((RecordOwner)m_recordOwner).removeRecord(this); // Take this query off the document's list
         m_recordOwner = null;
 
         while (m_listener != null)
         {
             this.removeListener(m_listener, true);    // free all the behaviors
         }
         m_dbEditMode &= (~DBConstants.EDIT_CLOSE_IN_FREE);
 
         super.free(); // Free fields, free table.
     }
     /**
      * Make an exact copy of this record.
      * Note This new record does not have a recordowner, so be sure to set one.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone() throws CloneNotSupportedException
     {
         return this.clone(false);
     }
     /**
      * Make an exact copy of this record.
      * Note This new record does not have a recordowner, so be sure to set one.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone(boolean bCloneListeners) throws CloneNotSupportedException
     {
         Record record = null; // DO NOT call super, as super will not call my method init(recordowner)
         try {
             Class<?> c = this.getClass();
             record = (Record)c.newInstance();
             RecordOwner recordOwner = Record.findRecordOwner(this);
             record.init(recordOwner);   // This is strange, but this way, the cloned record has access to all recordowner's objects (ie., Environment).
             if (recordOwner != null)        // Now, disconnect this record from the owner, so the caller can set this.
                 recordOwner.removeRecord(record);
             else
                 if (this.getTable() != null)
                     if (this.getTable().getDatabase() != null)
             {   // Since this is a clone, and you didn't give me a record owner, I'll use the same database as this record
                 BaseDatabase database = this.getTable().getDatabase();
                 record.setTable(database.makeTable(record));    // This record uses the same database.
             }
         }
         catch (Exception ex) {
             record = null;
         }
         if (bCloneListeners)
             this.matchListeners(record, true, true, false, true, true);
         return record;
     }
     /**
      * Set up all the fields for this record.
      */
     public void setupFields()
     {
         FieldInfo field = null;
         for (int iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq < 256; iFieldSeq++)
         {
             field = this.setupField(iFieldSeq);     // Allocate this Field (may be overidden)
             if (field == null)
                 break;                              // End of fields
         }
     }
     /**
      * Set up this field for this record.
      * Override this method (or add the fields manually).
      * @param iFieldSeq The field to set up and add to this record.
      * @return The new field.
      */
     public BaseField setupField(int iFieldSeq)
     {
         return null;
     }   // This return is required
     /**
      * Set up all the key areas for this record.
      */
     public void setupKeys()
     {
         KeyArea keyArea = null;
         for (int iKeyArea = DBConstants.MAIN_KEY_FIELD; iKeyArea < 64; iKeyArea++)
         {
             keyArea = this.setupKey(iKeyArea);      // Allocate this Key (overidden in file class)
             if (keyArea == null)
                 break;                              // End of Keys
         }
     }
     /**
      * Set up this key area for this record.
      * Override this method (or add the key areas manually).
      * @param iKeyArea The key area to set up and add to this record.
      * @return The new key area.
      */
     public KeyArea setupKey(int iKeyArea)
     {
         return null;
     }   // Must be overidden
     /**
      * Clear all the fields and leave record in an indeterminate state.
      * This method is frequently used to prepare a record for a seek command. This prevents
      * the Edit command from being called because the record was changed. (Edit usually called the database).
      * @param bDisplayOption Display the new field values.
      */
     public void initRecord(boolean bDisplayOption)  //, boolean bDisplayOption)
     {
         this.handleNewRecord(bDisplayOption);
         this.setEditMode(Constants.EDIT_NONE);  // No Status
     }
     /**
      * Select the fields for this query.
      * Override this to select other than all the records.
      */
     public void selectFields()
     {
         // By default, all fields are selected
     }
     /**
      * Select/Deselect all the fields in this using SQL format.
      * ie., "AgencyName, AgencyAddress, SubTable.SubFieldName, AgencyZipCode".
      */
     public void setSelected(String strFields)
     {
         String SELECT = "SELECT ";
         int iLastPlus = strFields.length();
         while ((iLastPlus > 1) && (strFields.charAt(iLastPlus-1) == ' '))
         {
             iLastPlus--;        // Strip Trailing space
         }
         int iStart = 0;
         while ((iLastPlus > iStart + 1) && (strFields.charAt(iStart) == ' '))
         {
             iStart++;       // Strip Leading space
         }
         if (iLastPlus > iStart+7) if (strFields.substring(iStart, iStart+7).equalsIgnoreCase(SELECT))
             iStart += SELECT.length();  // Leading "SELECT "
         if (strFields.substring(iStart, iLastPlus).equals("*"))
             this.setSelected(true);   // Select all
         else
         {   // Select the fields
             this.setSelected(false);    // Deselect all to start
             int iEnd = 0;
             while (iEnd < iLastPlus)
             {
                 iEnd = strFields.indexOf(',', iStart);  // Next comma
                 if (iEnd == -1)
                     iEnd = iLastPlus;
                 String strField = strFields.substring(iStart, iEnd);    // Next selected field
                 BaseField field = this.getField(strField);
                 if (field != null)
                     field.setSelected(true);    // Select this field
                 iStart = iEnd + 1;
                 if (iEnd < iLastPlus) if (strFields.charAt(iStart) == ' ')
                     iStart++; // Skip the space
             }
         }
     }
     /**
      * Select/Deselect all the fields in this record.
      * @param bSelected Set selected if true, deselect all if false.
      */
     public void setSelected(boolean bSelected)
     {
         int iCount = this.getFieldCount();
         for (int i = 0; i < iCount; i++)
         {
             this.getField(i).setSelected(bSelected);
         }
     }
     /**
      * Override this to add record listeners and filters.
      */
     public void addListeners()
     {
         if ((this.getMasterSlave() & RecordOwner.MASTER) != 0)    // Don't add in slave
             this.addMasterListeners();
         if ((this.getMasterSlave() & RecordOwner.SLAVE) != 0)     // Do add in slave
             this.addSlaveListeners();
     }
     /**
      * Override this to add record listeners and filters.
      */
     public void addMasterListeners()
     {
     }
     /**
      * Override this to add record listeners and filters.
      */
     public void addSlaveListeners()
     {
     }
     /**
      * Override this to add record listeners and filters to every screen where this is the main record.
      * @param screen The screen these listeners will be in.
      */
     public void addScreenListeners(RecordOwner screen)
     {
     }
     /**
      * Set the next listener in the listener chain.
      * Note: You can pass the full class name, or the short class name or (preferably) the class.
      * @param strListenerClass The name of the class I'm looking for.
      * @return The first listener of this class or null if no match.
      */
     public void setListener(FileListener listener)
     {
         m_listener = listener;
     }
     /**
      * Add a listener to the chain.
      * @param theBehavior Listener or Filter to add to this record - calls doAddListener.
      */
     public void addListener(BaseListener listener)
     {
         listener.setNextListener(null);  // Just being safe
         if (this.getTable() != null)
             this.getTable().addListener(this, (FileListener)listener);   // Give the table a chance to clone this or whatever.
         else
             this.doAddListener((FileListener)listener);
     }
     /**
      * Internal method to add a listener to the end of the chain.
      * Sets the listener's owner to this.
      * @param theBehavior Listener or Filter to add to this record.
      */
     public void doAddListener(BaseListener listener)
     {
         if (m_listener != null)
             m_listener.doAddListener((FileListener)listener);
         else
             m_listener = (FileListener)listener;
         boolean bOldState = listener.setEnabledListener(false);   // To disable recursive forever loop!
         listener.setOwner(this);
         listener.setEnabledListener(bOldState);        // Renable the listener to eliminate echos
     }
     /**
      * Remove a listener from the chain.
      * @param theBehavior Listener or Filter to add to this record.
      * @param bFreeBehavior Free the behavior.
      */
     public void removeListener(BaseListener theBehavior, boolean bFreeBehavior)
     {
         if (m_listener != null)
         {
             if (m_listener == theBehavior)
             {
                 m_listener = (FileListener)theBehavior.getNextListener();
                 theBehavior.unlink(bFreeBehavior);      // remove theBehavior from the linked list
             }
             else
                 m_listener.removeListener(theBehavior, bFreeBehavior);
         }
     }   // Callers should remember to free this listener now
     public static final boolean[] ALL_TRUE = new boolean[0];
     /**
      * Get the current status (enabled/disabled) for all the listeners.
      * @return a array of all the listener statuses.
      * @see setEnableListener.
      */
     public boolean[] setEnableListeners(boolean flag)
     {
         int iPosition = 0;
         boolean rgbEnabled[] = ALL_TRUE;
         FileListener fileBehavior = this.getListener();
         while (fileBehavior != null)
         {
             if (!fileBehavior.isEnabledListener())
             {   // This one was disabled, set it.
                 if (iPosition >= rgbEnabled.length)
                 {       // Increase the size
                     boolean[] rgbEnabledNew = new boolean[iPosition + 8];
                     for (int i = 0; i < rgbEnabledNew.length; i++)
                     {
                         if (i < rgbEnabled.length)
                             rgbEnabledNew[i] = rgbEnabled[i];
                         else
                             rgbEnabledNew[i] = true;
                     }
                     rgbEnabled = rgbEnabledNew;
                 }
                 rgbEnabled[iPosition] = false;  // This listener was disabled
             }
             iPosition++;
             fileBehavior.setEnabledListener(flag);
             fileBehavior = (FileListener)fileBehavior.getNextListener();
         }
         return rgbEnabled;
     }
     /**
      * Set the current status (enabled/disabled) for all the listeners.
      * @param rgbEnabled an array of all the listener statuses.
      * @see setEnableListener.
      */
     public void setEnableListeners(boolean[] rgbEnabled)
     {
         if (rgbEnabled == null)
             rgbEnabled = ALL_TRUE;
         int iPosition = 0;
         FileListener fileBehavior = this.getListener();
         while (fileBehavior != null)
         {
             if (iPosition < rgbEnabled.length)
                 fileBehavior.setEnabledListener(rgbEnabled[iPosition]);
             else
                 fileBehavior.setEnabledListener(true);
             iPosition++;
             fileBehavior = (FileListener)fileBehavior.getNextListener();
         }
     }
     /**
      * Enable or Disable non-filter listeners for this record.
      */
     public Object[] setEnableNonFilter(Object[] rgobjEnable, boolean bHasNext, boolean bBreak, boolean bAfterRequery, boolean bSelectEOF, boolean bFieldListeners)
     {
         boolean bEnable = (rgobjEnable == null) ? false : true;
         if (bFieldListeners)
         {
             if (rgobjEnable == null)
                 rgobjEnable = this.setEnableFieldListeners(bEnable);
             else
                 this.setEnableFieldListeners(rgobjEnable);
         }
         else
         {
             if (rgobjEnable == null)
                 rgobjEnable = new Object[0];
         }
         FileListener listener = this.getListener();
         int iCount = this.getFieldCount();
         while (listener != null)
         {
             if (!(listener instanceof org.jbundle.base.db.filter.FileFilter))
             {
                 if (!bEnable)
                 {
                     rgobjEnable = Utility.growArray(rgobjEnable, iCount + 1, 8);
                     if (listener.isEnabledListener())
                         rgobjEnable[iCount] = Boolean.TRUE;
                     else
                         rgobjEnable[iCount] = Boolean.FALSE;
                     listener.setEnabledListener(bEnable);
                 }
                 else
                 {   // Enable
                     boolean bEnableThis = true;
                     if (iCount < rgobjEnable.length)
                         if (rgobjEnable[iCount] != null)
                             bEnableThis = ((Boolean)rgobjEnable[iCount]).booleanValue();
                     listener.setEnabledListener(bEnableThis);
                 }
                 iCount++;
             }
             listener = (FileListener)listener.getNextListener();
         }
         if (bEnable)
         {
             if (bAfterRequery)
                 this.handleRecordChange(null, DBConstants.AFTER_REQUERY_TYPE, true);
             if (bBreak)
                 this.handleRecordChange(null, DBConstants.CONTROL_BREAK_TYPE, true);
             if (bHasNext)
             {
                 this.handleValidRecord(true);
                 this.handleRecordChange(null, DBConstants.MOVE_NEXT_TYPE, true);
             }
             if (bSelectEOF)
                 this.handleRecordChange(null, DBConstants.SELECT_EOF_TYPE, true);
             if (this.getTable().getCurrentTable().getRecord() != this)
             {   // Special logic - match listeners on current record (if shared)
                 boolean bCloneListeners = false;
                 boolean bMatchEnabledState = true;
                 boolean bSyncReferenceFields = false;
                 boolean bMatchSelectedState = true;
                 this.matchListeners(this.getTable().getCurrentTable().getRecord(), bCloneListeners, bMatchEnabledState, bSyncReferenceFields, bMatchSelectedState, true);
             }
         }
             
         return rgobjEnable;
     }
     /**
      * Enable or Disable all the field listeners and return the original state.
      * @param bEnable Enable or disable.
      * @return The original state of the listeners.
      */
     public Object[] setEnableFieldListeners(boolean bEnable)
     {
         int iFieldCount = this.getFieldCount();
         Object[] rgobjEnabledFields = new Object[iFieldCount];
         for (int i = 0; i < iFieldCount; i++)
         {
             BaseField field = this.getField(i);
             rgobjEnabledFields[i] = field.setEnableListeners(bEnable);
         }
         return rgobjEnabledFields;
     }
     /**
      * Enable all the field listeners in this record according to this map.
      * @param rgbEnabledFields The field listeners (in order) to enable/disable.
      */
     public void setEnableFieldListeners(Object[] rgobjEnabledFields)
     {
         for (int i = 0; i < this.getFieldCount(); i++)
         {
             this.getField(i).setEnableListeners((boolean[])rgobjEnabledFields[i]);
         }
     }
     /**
      * Clone the record behaviors of this table and add them to newTable.
      * @param record The record to clone listeners from.
      * @param bCloneListeners Clone listeners that do not exist?
      * @param bMatchEnabledState If source listener is disabled, disable the new (or existing) listener.
      * @param bSyncReferenceFields Make sure any reference fields are referencing the same record.
      * @param bMatchOpenMode Match the open mode
      */
     public void matchListeners(Record record, boolean bCloneListeners, boolean bMatchEnabledState, boolean bSyncReferenceFields, boolean bMatchSelectedState, boolean bMatchOpenMode)
     {
         FileListener listener = this.getListener();
         // Don't add the listeners that already exist in the target record (from the record's addListeners() method).
         FileListener recordListener = record.getListener();
         while ((listener != null) && (recordListener != null))
         {   // Get through the first listeners (The ones added in init in the addListeners() method)
             if (!recordListener.getClass().getName().equals(listener.getClass().getName()))
                 break;  // Not the same... STOP. and start looking for matching listeners.
             if (bMatchEnabledState)
                 recordListener.setEnabledListener(listener.isEnabledListener());
             listener = (FileListener)listener.getNextListener();    // Skip to the next one in the chain.
             recordListener = (FileListener)recordListener.getNextListener();
         }
         while (listener != null)
         {   // Clone all the file behaviors (that want to be cloned)
             int iBehaviorCount = 0;
             FileListener thisListener = this.getListener(listener.getClass());
             while ((thisListener != null) && (thisListener != listener))
             {   // There are more than one, count which one it is.
                 iBehaviorCount++;
                 thisListener = (FileListener)thisListener.getListener(listener.getClass());
             }
             FileListener newBehavior = null;
             newBehavior = record.getListener(listener.getClass());
             while (iBehaviorCount > 0)
             {   // Get the correct one
                 iBehaviorCount--;
                 if (newBehavior != null)
                     newBehavior = (FileListener)newBehavior.getListener(listener.getClass());
             }
             if ((bCloneListeners) && (newBehavior == null))
             {
                 try   {
                     newBehavior = (FileListener)listener.clone(); // Clone the file behaviors
                 } catch (CloneNotSupportedException ex)   {
                     newBehavior = null;
                 }
                 if (newBehavior != null)
                     if (newBehavior.getOwner() == null)
                         record.addListener(newBehavior);        // Add them to the new query
             }
             if ((bMatchEnabledState) && (newBehavior != null))
                 newBehavior.setEnabledListener(listener.isEnabledListener());
             listener = (FileListener)listener.getNextListener();            // Do the next one in the chain
         }
         if ((bSyncReferenceFields) || (bMatchEnabledState) || (bCloneListeners) || (bMatchSelectedState))
         {
             for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
             {
                 BaseField field = this.getField(iFieldSeq);
                 BaseField fieldRecord = record.getField(iFieldSeq);
                 if ((fieldRecord == null) || (field.getClass() != fieldRecord.getClass()))
                     continue;
                 field.matchListeners(fieldRecord, bCloneListeners, bMatchEnabledState, bSyncReferenceFields, bMatchSelectedState);
             }
         }
         if (bMatchOpenMode)
             record.setOpenMode(this.getOpenMode());
     }
     /**
      * Are all the behaviors enabled or disabled?
      * @return false if All the listeners are disabled.
      */
     public boolean isAllListenersEnabled()
     {
         return (this.getNextEnabledListener() != null);
     }
     /**
      * Get the next enabled listener on the chain.
      * @return The next enabled listener.
      */
     public BaseListener getNextEnabledListener()
     {
         if (m_listener != null)
         {
             if (m_listener.isEnabled())
                 return m_listener;
             else
                 return m_listener.getNextEnabledListener();
         }
         else
             return null;
     }
     /**
      * Get the listener.
      * @return The first listener on the listener chain.
      */
     public FileListener getListener()
     {
         return m_listener;
     }
     /**
      * Get the listener with this identifier.
      * @return The first listener of this class or null if no match.
      */
     public FileListener getListener(Object strBehaviorClass)
     {
         return this.getListener(strBehaviorClass, true);   // By default need exact match
     }
     /**
      * Get the listener with this class identifier.
      * Note: You can pass the full class name, or the short class name or (preferably) the class.
      * @param strListenerClass The name of the class I'm looking for.
      * @return The first listener of this class or null if no match.
      */
     public FileListener getListener(Object strBehaviorClass, boolean bExactMatch)
     {
         FileListener listener = m_listener;
         if (listener == null)
             return null;
         if (!bExactMatch)
         {
             try {
                 if (strBehaviorClass instanceof String)
                     strBehaviorClass = Class.forName((String)strBehaviorClass);
                 if (((Class<?>)strBehaviorClass).isAssignableFrom(listener.getClass()))
                     return listener;
                 else
                     return (FileListener)listener.getListener(strBehaviorClass, bExactMatch);
             } catch (ClassNotFoundException ex) {
                 ex.printStackTrace();
                 return null;
             }
         }
         if (strBehaviorClass instanceof String)
         {
             String strClass = listener.getClass().getName();
             if (((String)strBehaviorClass).indexOf('.') == -1)
             {   // If identifier is not fully qualified, strip the package from the class
                 if (strClass.lastIndexOf('.') != -1)
                     strClass = strClass.substring(strClass.lastIndexOf('.') + 1);
             }
             if (strClass.equalsIgnoreCase((String)strBehaviorClass))
                 return listener;
         }
         else if (listener.getClass().equals(strBehaviorClass))
             return listener;
         return (FileListener)listener.getListener(strBehaviorClass, bExactMatch);
     }
     /**
      * Override this to Setup all the records for this query.
      * Only used for querys and abstract-record queries.
      * Actually adds records not tables, but the records aren't physically
      * added here, the record's tables are added to my table.
      * @param The recordOwner to pass to the records that are added.
      * @see addTable
      */
     public void addTables(RecordOwner recordOwner)
     {
 // ie.,   this.addTable(new SpecificRecord(null));
     }
     /**
      * Add a (merge) record to this multi-record.
      */
     public void addTable(Record record)
     {
         this.getTable().addTable(record.getTable());
     }
     /**
      * Get the record type from the field that specifies the record type.
      * (Override this).
      * @return The record type (as an object).
      */
     public BaseField getSharedRecordTypeKey()
     {
         if (this.getFieldCount() >= 2)  // Wild guess (typically the second field)
             if (this.getField(DBConstants.MAIN_FIELD + 1) instanceof IntegerField)
                 return this.getField(DBConstants.MAIN_FIELD + 1);
         return null;
     }
     /**
      * Get the shared record that goes with this key.
      * (Always override this).
      * @param objKey The value that specifies the record type.
      * @return The correct (new) record for this type (or null if none).
      */
     public Record createSharedRecord(Object objKey, RecordOwner recordOwner)
     {
         if (objKey instanceof Integer)
         {
 //            int iRecordType = ((Integer)objKey).intValue();
             // Add code here (in overidden code)
             // if (iRecordType == ReptileTypeField.LIZARD)
             //    return new Lizard(recordOwner);
         }
         return null;
     }
     /**
      * See if you are past the select range.
      * This is a utility for non-Sql queries where you have to test the EOF/BOF.
      * @param The temp key area to test against the current field values.
      * @return true if past this range.
      */
     public boolean checkParams(int iAreaDesc)
     {
         KeyArea pIndex = this.getKeyArea(-1);
         int lastField = pIndex.getKeyFields() + DBConstants.MAIN_FIELD - 1;
         for (int keyFieldSeq = DBConstants.MAIN_FIELD; keyFieldSeq <= lastField; keyFieldSeq++)
         {
             KeyField pKeyField = pIndex.getKeyField(keyFieldSeq);
             BaseField pField = pKeyField.getField(DBConstants.MAIN_FIELD);
             BaseField pParamField = pKeyField.getField(iAreaDesc);
             int bCompare = pField.compareTo(pParamField);
             if (iAreaDesc == DBConstants.END_SELECT_KEY) if (bCompare > 0)
                 return true;    // Past range
             if (iAreaDesc == DBConstants.START_SELECT_KEY) if (bCompare < 0)
                 return true;    // Past range
         }
         return false; // Within the range!
     }
     /**
      * Utility routine to add quotes to a string if the string contains a space.
      * @param strTableNames The table name to add quotes to if there is a space in the name.
      * @param bAddQuotes Add the quotes?
      * @return The new quoted table name.
      */
     public static final String formatTableNames(String strTableNames, boolean bAddQuotes)
     {
         if (bAddQuotes)
             if (strTableNames.indexOf(' ') != -1)
                 strTableNames = BaseField.addQuotes(strTableNames, DBConstants.SQL_START_QUOTE,  DBConstants.SQL_END_QUOTE);    // Spaces in name, quotes required
         return strTableNames;
     }
     /**
      * Return the record at this location (for QueryRecords).
      * @param i The sequence.
      * @return The record as this location.
      */
     public Record getRecordlistAt(int i)
     {
         return this;
     }
     /**
      * GetRecordlistCount.
      * @return The number of records under this record (for queryrecords).
      */
     public int getRecordlistCount() 
     {
         return 1;
     }
     /**
      * Get an actual Record to add/edit/etc...
      * Usually used in QueryRecords.
      * @return The physical record.
      */
     public Record getBaseRecord()
     {
         return this;
     }
     /**
      * By default display all the fields on change?
      * @return The display option.
      */
     public boolean getDisplayOption()
     {
         return m_bDisplayOption;
     }
     /**
      * Get the image for Buttons and Bitmaps and drag cursors.
      * (Previously getBitmap)
      * @return The name of the bitmap for this record.
      */
     public String getBitmap()
     {
         return null;
     }
     /**
      * Get the Database Name.
      * Always override this method.
      * @return The database name.
      */
     public String getDatabaseName()
     {
         return DEFAULT_DB_NAME;   // ****Override this****
     }
     /**
      * Get the database type.
      * Always override this method.
      * @return The database type (LOCAL/REMOTE/SCREEN/etc).
      */
     public int getDatabaseType()
     {
         return super.getDatabaseType();
     }
     /**
      * Is this a new - refreshed record?
      * @return true if true.
      */
     public boolean isRefreshedRecord()
     {
         return ((m_dbEditMode & DBConstants.EDIT_REFRESHED) != 0);
     }
     /**
      * Is record currently being freed?
      * @return true if true.
      */
     public boolean isInFree()
     {
         return ((m_dbEditMode & DBConstants.EDIT_CLOSE_IN_FREE) != 0);
     }
     /**
      * Is this a client or a server. Get the client/server flag.
      * @return The client/server flag.
      */
     public int getMasterSlave()
     {
         int iRecordOwnerMasterSlave = 0;    // None by default
         int iDBMasterSlave = RecordOwner.MASTER | RecordOwner.SLAVE;    // Both by default
         if (this.getRecordOwner() != null)
             iRecordOwnerMasterSlave = this.getRecordOwner().getMasterSlave();
         if (m_table != null)
         {
             if (this.getTable() != null)
                 if (this.getTable().getCurrentTable() != null)
                     if (this.getTable().getCurrentTable().getDatabase() != null)
                         iDBMasterSlave = this.getTable().getCurrentTable().getDatabase().getMasterSlave();
             if (iDBMasterSlave == RecordOwner.SLAVE)
                 if (this.getRecordOwner() == null)
                     iDBMasterSlave = RecordOwner.MASTER | RecordOwner.SLAVE;    // The only records that don't have an owner in a slave are user-added.
         }
         return iRecordOwnerMasterSlave | iDBMasterSlave;
     }
     /**
      * Get the table for this record.
      * This is the same as getFieldTable, but casts the class up.
      * @return The table for this record.
      */
     public BaseTable getTable()
     {
         BaseTable table = (BaseTable)super.getTable();
         if (table == null)    // It's possible that m_table was set in an overriding init method.
         {
             DatabaseOwner databaseOwner = null;
             if (this.getRecordOwner() != null)
                 databaseOwner = this.getRecordOwner().getDatabaseOwner();
             if (databaseOwner != null)
             {
                 BaseDatabase database = (BaseDatabase)databaseOwner.getDatabase(this.getDatabaseName(), this.getDatabaseType(), null);
                 m_table = database.makeTable(this);
             }
         }
         return (BaseTable)super.getTable();
     }
     /**
      * Get this field in the record.
      * Same as getFieldInfo, but casts the field to BaseField.
      * @param iFieldSeq The sequence of the field in the record.
      * @return The field.
      */
     public BaseField getField(int iFieldSeq)
     {
         return (BaseField)super.getField(iFieldSeq);
     }
     /**
      * Get this field in the record.
      * @param The field name (If this is a queryrecord, you can pass Filename.fieldname).
      * @return The field.
      */
     public BaseField getField(String strFieldName)    // Lookup this field
     {
         int iDotIndex = strFieldName.indexOf('.');
         if (iDotIndex != -1)    // Is Field formated as: RecordName.FieldName?
             return this.getField(strFieldName.substring(0, iDotIndex), strFieldName.substring(iDotIndex+1, strFieldName.length()));
         else
             return (BaseField)super.getField(strFieldName);
     }
     /**
      * Get this field in the table/record.
      * @param strTableName the name of the table this field is in.
      * @param iFieldSeq The sequence of the field in the record.
      * @return The field.
      */
     public BaseField getField(String strTableName, int iFieldSeq)   // Lookup this field
     {
         if (this.getRecord(strTableName) != this)
             return null;
         return this.getField(iFieldSeq);
     }
     /**
      * Get this field in the table/record.
      * @param strTableName the name of the table this field is in (for query records).
      * @param The field name.
      * @return The field.
      */
     public BaseField getField(String strTableName, String strFieldName)     // Lookup this field
     {
         if (this.getRecord(strTableName) != this)
             return null;
         return this.getField(strFieldName);
     }
     /**
      * Get the field sequence for this field.
      * @param fieldName
      * @return
      */
     public int getFieldSeq(String fieldName)
     {
         for (int i = 0; i < this.getFieldCount(); i++)
         {
             if (fieldName.equals(this.getField(i).getFieldName()))
                 return i;
         }
         return -1;
     }
     /**
      * Get the field in the record that is a reference to this record.
      * The field returned must be a ReferenceField, in a key area, and must override getReferenceRecordName().
      * @param record The record to check.
      * @return The field which is used to reference this record or null if none.
      */
     public ReferenceField getReferenceField(Record record)
     {
         String strRecordName = null;
         if (record != null)
             strRecordName = record.getTableNames(false);
         ReferenceField fldBestGuess = null;
         for (int i = 0; i < this.getKeyAreaCount(); i++)
         {
             KeyArea keyArea = this.getKeyArea(i);
             BaseField fieldFirst = keyArea.getKeyField(DBConstants.MAIN_KEY_FIELD).getField(DBConstants.FILE_KEY_AREA);
             if (fieldFirst instanceof ReferenceField)
             {
                 if (fldBestGuess == null)
                     fldBestGuess = (ReferenceField)fieldFirst;
                 else if (((ReferenceField)fieldFirst).getReferenceRecordName().toString().equals(strRecordName))
                     fldBestGuess = (ReferenceField)fieldFirst;
             }
         }
         return fldBestGuess;
     }
     /**
      * Get the field that references this record (from another record).
      * The field returned must be a ReferenceField, in a key area, and must override getReferenceRecordName().
      * @param record The record to check.
      * @return The field which is used to reference this record or null if none.
      */
     public ReferenceField getReferringField()
     {
     	ClearFieldReferenceOnCloseHandler listener = (ClearFieldReferenceOnCloseHandler)this.getListener(ClearFieldReferenceOnCloseHandler.class, true);
     	while (listener != null)
     	{
     	BaseField field = listener.getField();
     	if (field instanceof ReferenceField)
     		if (((ReferenceField)field).getReferenceRecord(null, false) == this)
     			return (ReferenceField)field;
     	}
     	return null;
     }
     /**
      * Get the default key index.
      * @return The current default key area.
      */
     public KeyArea getKeyArea()
     {
         return this.getKeyArea(-1);
     }
     /**
      * Get this key area.
      * @param iKeySeq The key area to set.
      * @return The key area.
      */
     public KeyArea getKeyArea(int iKeySeq)
     {
         KeyArea keyArea = (KeyArea)super.getKeyArea(iKeySeq);
         if (keyArea == null)
             if (this != this.getRecordlistAt(0))
                 return this.getRecordlistAt(0).getKeyArea(iKeySeq);
         return keyArea;
     }
     /**
      * Set the default key order.
      * <p />Note: This only sets the order for the next query,
      * you must close(), then open() to see the new key order!
      * The actual recordset key order is set right before open().
      * @param int iKeyArea the current index.
      * @return The new default key area.
      */
     public KeyArea setKeyArea(int iKeyArea)
     {
         KeyArea keyArea = this.getKeyArea(iKeyArea);
         if (keyArea == null)
             return keyArea;
         return this.setKeyArea(keyArea.getKeyName());
     }
     /**
      * Set the default key order.
      * @param String strKeyName the current index.
      * @return The new default key area (null if not found).
      */
     public KeyArea setKeyArea(String strKeyName)
     {
         return (KeyArea)super.setKeyArea(strKeyName);
     }
     /**
      * Set the default key order.
      * @param String strKeyName the current index.
      * @return The new default key area (null if not found).
      */
     public KeyArea setKeyArea(BaseField fldFirstFieldInKey)
     {
         for (int i = 0; i < this.getKeyAreaCount(); i++)
         {
             KeyArea keyArea = this.getKeyArea(i);
             BaseField fieldFirst = keyArea.getKeyField(DBConstants.MAIN_KEY_FIELD).getField(DBConstants.FILE_KEY_AREA);
             if (fieldFirst == fldFirstFieldInKey)
             {
                 return this.setKeyArea(i);
             }
         }
         return null;    // Not found
     }
     /**
      * Set the default key order.
      * @param String strKeyName the current index.
      * @return The new default key area (null if not found).
      */
     public KeyArea getKeyArea(String strKeyName)
     {
         return (KeyArea)super.setKeyArea(strKeyName);
     }
     /**
      * Get the default key index for grid screens.
      * The default key area for grid screens is the first non-unique key that is a string.
      * Override this to supply a different key area.
      * @return The key area to use for screens and popups.
      */
     public String getDefaultScreenKeyArea()
     {
         for (int i = DBConstants.MAIN_KEY_AREA; i < this.getKeyAreaCount(); i++)
         {
             if (this.getKeyArea(i).getUniqueKeyCode() == DBConstants.NOT_UNIQUE)
                 if (this.getKeyArea(i).getKeyField(DBConstants.MAIN_KEY_FIELD).getField(DBConstants.FILE_KEY_AREA) instanceof StringField)
                     return this.getKeyArea(i).getKeyName();
         }
         return null;      // Return the current key area
     }
     /**
      * Get the default display field for this record (for popups and lookups).
      * @return The sequence of the field that should be displayed.
      */
     public String getDefaultDisplayFieldName()
     {
         int iSeq = this.getDefaultDisplayFieldSeq();
         if (iSeq == -1)
             return null;
         return this.getField(iSeq).getFieldName();
     }
     /**
      * Get the default display field for this record (for popups and lookups).
      * @return The sequence of the field that should be displayed.
      */
     public int getDefaultDisplayFieldSeq()
     {
         String iKeyOrder = this.getDefaultScreenKeyArea();
         if (iKeyOrder == null)
         {
             for (int i = 0; i < this.getFieldCount(); i++)
             {
             	if (this.getField(i).isHidden())
             		continue;
                 if (this.getField(i) instanceof StringField)
                     return i;
             }
             return DBConstants.MAIN_FIELD + 1;
         }
         for (int i = 0; i < this.getFieldCount(); i++)
         {
             if (this.getField(i) == this.getKeyArea(iKeyOrder).getField(DBConstants.FILE_KEY_AREA))
                 return i;
         }
         return DBConstants.MAIN_FIELD + 1;
     }
     /**
      * Get the record with this file name.
      * This is more usefull in the queryrecord.
      * @return This if match.
      */
     public Record getRecord(String strFileName)
     {
         boolean bAddQuotes = false;
         if (strFileName.length() > 0) if (strFileName.charAt(0) == '\"')
             bAddQuotes = true;
         if (this.getTableNames(bAddQuotes).equals(strFileName))
             return this;
         return null;
     }
     /**
      * Get the record count.
      * @return The records in this table (For querys and multi-tables).
      */
     public int getRecordCount()
     {
         return this.getTable().getRecordCount();
     }
     /**
      * What a record is called.
      * <p>(ie., for the AgencyTypeTable, one record is called an "Agency Type")
      * @return The record name.
      */
     public String getRecordName()
     {
         return this.getTableNames(false);
     } 
     /**
      * Make this the main record for screen.
      */
     public void setRecordOwner(RecordOwner recordOwner)
     {
         m_recordOwner = recordOwner;
     }
     /**
      * Get the recordowner.
      * @return The recordowner.
      */
     public RecordOwner getRecordOwner()
     {
         return (RecordOwner)m_recordOwner;
     }
     public static RecordOwner findRecordOwner(Record record)
     {
         return record.findRecordOwner();    // Get rid of this
     }
     /**
      * Get a recordowner from this record.
      * This method does a deep search using the listeners and the database connections to find a recordowner.
      * @param record
      * @return
      */
     public RecordOwner findRecordOwner()
     {
         RecordOwner recordOwner = this.getRecordOwner();
         if (recordOwner instanceof org.jbundle.base.db.shared.FakeRecordOwner)
             recordOwner = null; 
         BaseListener listener = this.getListener();
         while ((recordOwner == null) && (listener != null))
         {
             BaseListener listenerDep = listener.getDependentListener();
             if (listenerDep != null)
                 if (listenerDep.getListenerOwner() instanceof RecordOwner)
                     recordOwner = (RecordOwner)listenerDep.getListenerOwner();
             listener = listener.getNextListener();
         }
         if (recordOwner == null)
             if (this.getTable() != null)
                 if (this.getTable().getDatabase() != null)
                     if (this.getTable().getDatabase().getDatabaseOwner() instanceof Application)
         {
             App app = (App)this.getTable().getDatabase().getDatabaseOwner();
             if (app.getSystemRecordOwner() == null) // This should be okay... get the system recordowner.
                 app = ((Environment)this.getTable().getDatabase().getDatabaseOwner().getEnvironment()).getDefaultApplication();
             if (app != null)
             {
                 if (app.getSystemRecordOwner() instanceof RecordOwner)
                     recordOwner = (RecordOwner)app.getSystemRecordOwner();
                 else
                 {
                     Environment env = (Environment)this.getTable().getDatabase().getDatabaseOwner().getEnvironment();
                     for (int i = env.getApplicationCount() - 1; i >= 0; i--)
                     {
                         app = env.getApplication(i);
                         if (app instanceof MainApplication)
                             if (app.getSystemRecordOwner() instanceof RecordOwner)
                                 recordOwner = (RecordOwner)app.getSystemRecordOwner();
                     }
                 }
             }
         }
         return recordOwner;
     }
     /**
      * Setup the SQL Key Filter.
      * (ie., 'WHERE ID=10')
      * @param seekSign The seek sign.
      * @param bAddOnlyMods Add only the keys which have modified?
      * @param bIncludeFileName Include the file name in the string?
      * @param bUseCurrentValues Use current values?
      * @param vParamList The parameter list.
      * @param bForceUniqueKey If params must be unique, if they aren't, add the unique key to the end.
      * @param bIncludeTempFields  If true, include any temporary key fields that have been added to the end if this keyarea
      * @param iAreaDesc The key area to select.
      * @return The select string.
      */
     public String addSelectParams(String seekSign, int areaDesc, boolean bAddOnlyMods, boolean bIncludeFileName, boolean bUseCurrentValues, Vector<BaseField> vParamList, boolean bForceUniqueKey, boolean bIncludeTempFields)
     {
         String sFilter = DBConstants.BLANK;
         KeyArea keyArea = this.getKeyArea(-1);
         if (keyArea != null)        // Leave orderby blank if no index specified
             sFilter = keyArea.addSelectParams(seekSign, areaDesc, bAddOnlyMods, bIncludeFileName, bUseCurrentValues, vParamList, bForceUniqueKey, bIncludeTempFields);
         return sFilter;
     }
     /**
      * Setup the SQL Sort String.
      * (ie., (ORDER BY) 'AgencyName, AgencyNo').
      * @param bIncludeFileName If true, include the filename with the fieldname in the string.
      * @param bForceUniqueKey If params must be unique, if they aren't, add the unique key to the end.
      * @return The SQL sort string.
      * @see KeyArea
      */
     public String addSortParams(boolean bIncludeFileName, boolean bForceUniqueKey)
     {
         String strSort = DBConstants.BLANK;
         KeyArea keyArea = this.getKeyArea(-1);
         if (keyArea != null)        // Leave orderby blank if no index specified
             strSort = keyArea.addSortParams(bIncludeFileName, bForceUniqueKey);
         return strSort;
     }
     /**
      * Are all the fields selected?
      * @return true if all selected.
      */
     public boolean isAllSelected()
     {
         boolean bAllSelected = true;
         for (int iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= this.getFieldCount() + DBConstants.MAIN_FIELD - 1; iFieldSeq++)
         {
             if(this.getField(iFieldSeq).isSelected() == false)
                 bAllSelected = false;
         }
         return bAllSelected;
     }
     /**
      * Get the SQL field string for this table.
      *  <p>(ie., (SELECT) 'EmployeeID,EmpName,"Emp Last",Salary')
      *  (ie., (UPDATE) 'EmployeeID=5,EmpName='Fred',"Emp Last"='Flintstone',Salary=null).
      * @param iType The sql query type.
      * @param bUseCurrentValues If true, use the current field value, otherwise, use '?'.
      * @return The SQL field list.
      */
     public String getSQLFields(int iType, boolean bUseCurrentValues)
     {
         String strFields = DBConstants.BLANK;
         boolean bAllSelected = this.isAllSelected();
         boolean bIsQueryRecord = this.isQueryRecord();
         if (iType != DBConstants.SQL_SELECT_TYPE)
             bAllSelected = false;
         if (bAllSelected == true)
             strFields = " *";
         else
         {
             for (int iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= this.getFieldCount() + DBConstants.MAIN_FIELD - 1; iFieldSeq++)
             {
                 BaseField field = this.getField(iFieldSeq);
                 if (field.getSkipSQLParam(iType))
                     continue; // Skip this param
                 if (strFields.length() > 0)
                     strFields += ",";
                 String strCompare = null;
                 if (bUseCurrentValues == false)
                     strCompare = "?";
                 if (iType != DBConstants.SQL_INSERT_VALUE_TYPE)
                     strFields += " " + field.getFieldName(true, bIsQueryRecord);    // Full name if QueryRecord
                 else    // kInsertValueType
                     strFields += field.getSQLFilter("", strCompare, false);   // Full name if QueryRecord
                 if (iType == DBConstants.SQL_UPDATE_TYPE)
                     strFields += field.getSQLFilter("=", strCompare, false);    // Full name if QueryRecord
             }
         }
         return strFields;
     }
     /**
      * Get the SQL 'Insert' string.
      * INSERT INTO table(field1, field2) VALUES('value1', 'value2');
      * @param bUseCurrentValues If true, use the current field value, otherwise, use '?'.
      * @return The SQL insert string.
      */
     public String getSQLInsert(boolean bUseCurrentValues)
     {
         String strRecordset = this.getBaseRecord().makeTableNames(false);
         String strFields = this.getBaseRecord().getSQLFields(DBConstants.SQL_INSERT_TABLE_TYPE, bUseCurrentValues);
         String strValues = this.getBaseRecord().getSQLFields(DBConstants.SQL_INSERT_VALUE_TYPE, bUseCurrentValues);
 
         strRecordset = "INSERT INTO " + strRecordset + "(" + strFields + ") VALUES (" + strValues + ")";
 
         return strRecordset;
     }
     /**
      * Get the SQL SELECT string.
      * @param bUseCurrentValues If true, use the current field value, otherwise, use '?'.
      * @param vParamList The parameter list.
      * @return The SQL select string.
      */
     public String getSQLQuery(boolean bUseCurrentValues, Vector<BaseField> vParamList)
     {
         String strRecordset = this.makeTableNames(false);
         String strFields = this.getSQLFields(DBConstants.SQL_SELECT_TYPE, bUseCurrentValues);
         boolean bIsQueryRecord = this.isQueryRecord();
         String strSortParams = this.addSortParams(bIsQueryRecord, true);
 
         this.handleInitialKey();        // Set up the smaller key
         String strStartRange = this.addSelectParams(">=", DBConstants.START_SELECT_KEY, true, bIsQueryRecord, bUseCurrentValues, vParamList, true, false);   // Add only if changed
         this.handleEndKey();            // Set up the larger key
         String strEndRange = this.addSelectParams("<=", DBConstants.END_SELECT_KEY, true, bIsQueryRecord, bUseCurrentValues, vParamList, true, false);   // Add only if changed
         String strWhere = DBConstants.BLANK;
         if (strStartRange.length() == 0)
             strWhere = strEndRange;
         else
         {
             if (strEndRange.length() == 0)
                 strWhere = strStartRange;
             else
                 strWhere = strStartRange + " AND " + strEndRange;
         }
         //      Next, get the recordset filter
             StringBuffer strbFilter = new StringBuffer();
             this.handleRemoteCriteria(strbFilter, bIsQueryRecord, vParamList);  // Add any selection criteria (from behaviors)
             if (strbFilter.length() > 0)
             {
                 if (strWhere.length() == 0)
                     strWhere = strbFilter.toString();
                 else
                     strWhere += " AND (" + strbFilter.toString() + ")";
             }
 
         if (strWhere.length() > 0)
             strWhere = " WHERE " + strWhere;
         strRecordset = "SELECT" + strFields + " FROM " + strRecordset + strWhere + strSortParams;
         return strRecordset;
     }
     /**
      * Get the SQL 'Seek' string.
      * @param bUseCurrentValues If true, use the current field value, otherwise, use '?'.
      * @param vParamList The parameter list.
      * @return The SQL select string.
      */
     public String getSQLSeek(String strSeekSign, boolean bUseCurrentValues, Vector<BaseField> vParamList)
     {
         boolean bIsQueryRecord = this.isQueryRecord();
         String strRecordset = this.makeTableNames(false);
         String strFields = this.getSQLFields(DBConstants.SQL_SELECT_TYPE, bUseCurrentValues);
         String strSortParams = this.addSortParams(bIsQueryRecord, false);
 
         KeyArea keyArea = this.getKeyArea(-1);  // Current index
         keyArea.setupKeyBuffer(null, DBConstants.TEMP_KEY_AREA);        // Move params
         String sFilter = keyArea.addSelectParams(strSeekSign, DBConstants.TEMP_KEY_AREA, false, bIsQueryRecord, bUseCurrentValues, vParamList, false, false);  // Always add!?
         if (sFilter.length() > 0)
         {
             if (strRecordset.indexOf(" WHERE ") == -1)
                 sFilter = " WHERE " + sFilter;
             else
                 sFilter = " AND " + sFilter;
         }
 
         strRecordset = "SELECT" + strFields + " FROM " + strRecordset + sFilter + strSortParams;
 
         return strRecordset;
     }
     /**
      * Get the SQL 'Update' string.
      * UPDATE table SET field1 = 'value1', field2 = 'value2' WHERE key = 'value'
      * @param bUseCurrentValues If true, use the current field value, otherwise, use '?'.
      * @param vParamList The parameter list.
      * @return The SQL select string.
      * @return null if nothing to update.
      */
     public String getSQLUpdate(boolean bUseCurrentValues)
     {
         String strRecordset = this.getBaseRecord().makeTableNames(false);
 
         KeyArea keyArea = this.getBaseRecord().getKeyArea(0); // Primary index
         boolean bUseCurrentKeyValues = bUseCurrentValues ? true : keyArea.isNull(DBConstants.TEMP_KEY_AREA, true);
         boolean bIsQueryRecord = this.getBaseRecord().isQueryRecord();
         String sFilter = keyArea.addSelectParams("=", DBConstants.TEMP_KEY_AREA, false, bIsQueryRecord, bUseCurrentKeyValues, null, true, true);   // Always add!?
         if (sFilter.length() > 0)
             sFilter = " WHERE " + sFilter;
         String strSetValues = this.getBaseRecord().getSQLFields(DBConstants.SQL_UPDATE_TYPE, bUseCurrentValues);
         if (strSetValues.length() == 0)
             return null;    // No fields to update
 
         strRecordset = "UPDATE " + strRecordset + " SET " + strSetValues + sFilter;
 
         return strRecordset;
     }
     /**
      * Get the SQL 'Delete' string.
      * DELETE table WHERE key=value;
      * @param bUseCurrentValues   If true, insert field values, if false, insert '?'
      * @return The SQL delete string.
      */
     public String getSQLDelete(boolean bUseCurrentValues)
     {
         String strRecordset = this.getBaseRecord().makeTableNames(false);
 
         KeyArea keyArea = this.getKeyArea(0); // Primary index
         boolean bIsQueryRecord = this.isQueryRecord();
         boolean bUseCurrentKeyValues = bUseCurrentValues ? true : keyArea.isNull(DBConstants.TEMP_KEY_AREA, false);
         String sFilter = "?";
         sFilter = keyArea.addSelectParams("=", DBConstants.TEMP_KEY_AREA, false, bIsQueryRecord, bUseCurrentKeyValues, null, true, false);    // Always add!?
         if (sFilter.length() > 0)
             sFilter = " WHERE " + sFilter;
 
         strRecordset = "DELETE FROM " + strRecordset + sFilter;
 
         return strRecordset;
     }
     /**
      * Get the name of this table.
      * Override this to supply the name of the table.
      * Note: This is almost always overidden (except for mapped files)
      * @param bAddQuotes if the table name contains spaces, add quotes.
      * @return The name of this table.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         String strTableNames = super.getTableNames(bAddQuotes);
         if (strTableNames == null)
             strTableNames = Constants.BLANK;
         return strTableNames;
     }
     /**
      * The name of this table in a sql query.<p>
      * Override this to supply the name of a built-in query.
      * @param bAddQuotes if the table name contains spaces, add quotes.
      * @return The name of this table.
      */
     public String makeTableNames(boolean bAddQuotes)
     {
         return this.getTableNames(bAddQuotes);
     } 
     /**
      * Called when a error happens on a file operation, return the errorcode, or fix the problem.
      * @param iChangeType The type of change that occurred.
      * @param iErrorCode The error code encountered on the file operation.
      * @return The new error code, or return the original code if you can't fix it.
      */
     public int handleErrorReturn(int iChangeType, int iErrorCode)        // init this field override for other value
     {
         BaseListener nextListener = this.getNextEnabledListener();
         while (nextListener != null)
         {
             boolean bOldState = nextListener.setEnabledListener(false);  // Don't allow it to be called again
             iErrorCode = ((FileListener)nextListener).doErrorReturn(iChangeType, iErrorCode);
             nextListener.setEnabledListener(bOldState);
             nextListener = nextListener.getNextEnabledListener();
         }
         iErrorCode = this.doErrorReturn(iChangeType, iErrorCode);
         return iErrorCode;
     }
     /**
      * Called when a error happens on a file operation, return the errorcode, or fix the problem.
      * @param iChangeType The type of change that occurred.
      * @param iErrorCode The error code encountered on the file operation.
      * @return The new error code, or return the original code if you can't fix it.
      */
     public int doErrorReturn(int iChangeType, int iErrorCode)
     {   // Default, return the same errorCode
         return iErrorCode;
     }
     /**
      * The initial key position is in this record... Save it!
      */
     public void handleInitialKey()                  // init this field override for other value
     {
         KeyArea keyArea = this.getKeyArea(-1);
         if (keyArea == null)
             return;
         BaseBuffer buffer = new VectorBuffer(null);
         boolean[] rgbModified = keyArea.getModified();
         boolean[] rgbNullable = keyArea.setNullable(true);
         keyArea.setupKeyBuffer(buffer, DBConstants.FILE_KEY_AREA);
         keyArea.zeroKeyFields(DBConstants.START_SELECT_KEY);        // Zero out the key fields
         BaseListener  nextListener = this.getNextEnabledListener();
         if (nextListener != null)
             ((FileListener)nextListener).doInitialKey();
         else
             this.doInitialKey();
         keyArea.setNullable(rgbNullable);
         keyArea.setModified(rgbModified);
         keyArea.reverseKeyBuffer(buffer, DBConstants.FILE_KEY_AREA);
     }
     /**
      * The initial key position is in this record... Save it!
      */
     public void doInitialKey()                  // init this field override for other value
     {   // Save the current key values in the param buffers
         KeyArea keyArea = this.getKeyArea(-1);
         keyArea.setupKeyBuffer(null, DBConstants.START_SELECT_KEY);
     }
     /**
      * The end key position is in this record... Save it!
      */
     public void handleEndKey()
     {
         KeyArea keyArea = this.getKeyArea(-1);
         if (keyArea == null)
             return;
         BaseBuffer buffer = new VectorBuffer(null);
         boolean[] rgbModified = keyArea.getModified();
         boolean[] rgbNullable = keyArea.setNullable(true);
         keyArea.setupKeyBuffer(buffer, DBConstants.FILE_KEY_AREA);
         keyArea.zeroKeyFields(DBConstants.END_SELECT_KEY);      // Set the key fields to a large value
         BaseListener  nextListener = this.getNextEnabledListener();
         if (nextListener != null)
             ((FileListener)nextListener).doEndKey();
         else
             this.doEndKey();
         keyArea.setNullable(rgbNullable);
         keyArea.setModified(rgbModified);
         keyArea.reverseKeyBuffer(buffer, DBConstants.FILE_KEY_AREA);
     }
     /**
      * The end key position is in this record... Save it!
      */
     public void doEndKey()
     {   // Save the current key values in the param buffers
         KeyArea keyArea = this.getKeyArea(-1);
         keyArea.setupKeyBuffer(null, DBConstants.END_SELECT_KEY);
     }
     /**
      * Check to see if this record should be skipped.
      * Generally, you use a remote criteria.
      * @param strFilter The current SQL WHERE string.
      * @param bIncludeFileName Include the Filename.fieldName in the string.
      * @param vParamList The list of params.
      * @return true if the criteria passes.
      * @return false if the criteria fails, and returns without checking further.
      */
     public boolean handleLocalCriteria(StringBuffer strFilter, boolean bIncludeFileName, Vector<BaseField> vParamList)
     {
         BaseListener  nextListener = this.getNextEnabledListener();
         boolean bDontSkip = true;
         if (nextListener != null)
             bDontSkip = ((FileListener)nextListener).doLocalCriteria(strFilter, bIncludeFileName, vParamList);
         else
             bDontSkip = this.doLocalCriteria(strFilter, bIncludeFileName, vParamList);
         if (bDontSkip == false)
             return bDontSkip; // skip it
         return this.getTable().doLocalCriteria(strFilter, bIncludeFileName, vParamList);    // Give the table a shot at it
     }
     /**
      * Set up/do the local criteria.
      * Generally, you use a remote criteria.
      * @param strFilter The current SQL WHERE string.
      * @param bIncludeFileName Include the Filename.fieldName in the string.
      * @param vParamList The list of params.
      * @return true if the criteria passes.
      * @return false if the criteria fails, and returns without checking further.
      */
     public boolean doLocalCriteria(StringBuffer strFilter, boolean bIncludeFileName, Vector<BaseField> vParamList)
     {   // Default BaseListener
         return true;        // Don't skip (default)
     }
     /**
      * Check to see if this record should be skipped.
      * @param strFilter The current SQL WHERE string.
      * @param bIncludeFileName Include the Filename.fieldName in the string.
      * @param vParamList The list of params.
      * @return true if the criteria passes.
      * @return false if the criteria fails, and returns without checking further.
      */
     public boolean handleRemoteCriteria(StringBuffer strFilter, boolean bIncludeFileName, Vector<BaseField> vParamList)
     {
         BaseListener  nextListener = this.getNextEnabledListener();
         if (nextListener != null)
             return ((FileListener)nextListener).doRemoteCriteria(strFilter, bIncludeFileName, vParamList);
         else
             return this.doRemoteCriteria(strFilter, bIncludeFileName, vParamList);
     }
     /**
      * Set up/do the remote criteria.
      * @param strFilter The current SQL WHERE string.
      * @param bIncludeFileName Include the Filename.fieldName in the string.
      * @param vParamList The list of params.
      * @return true if the criteria passes.
      * @return false if the criteria fails, and returns without checking further.
      */
     public boolean doRemoteCriteria(StringBuffer strFilter, boolean bIncludeFileName, Vector<BaseField> vParamList)
     {   // Default BaseListener
         return true;            // Default to... don't skip record
     }
     /**
      * Called when a new blank record is required for the table/query.
      * Set this field back to the original value.
      */
     public void handleNewRecord()   // init this field override for other value
     {
         this.handleNewRecord(this.getDisplayOption());
     }
     /**
      * Called when a new blank record is required for the table/query.
      * Set this field back to the original value.
      * This method calls doNewRecord for all the listeners, and handleFieldChange(init) for all the fields.
      * @param bDisplayOption Display the new values if true.
      */
     public void handleNewRecord(boolean bDisplayOption)     // init this field override for other value
     {
         int iFieldSeq;
         BaseField field = null;
         int iLastField = this.getFieldCount() + DBConstants.MAIN_FIELD - 1;   // Are these lines really needed?
 
         for (iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= iLastField; ++iFieldSeq)
         {
             this.getField(iFieldSeq).setModified(false);        // At this point, no changes have been done
         }
         for (iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= iLastField; ++iFieldSeq)
         {
             field = this.getField(iFieldSeq);
             if (!field.isModified())  // If it hasn't been modified by another listener...
             {
                 boolean[] rgbEnabled = field.setEnableListeners(false);
                 field.initField(bDisplayOption);                // Set the field to it's initial value
                 field.setEnableListeners(rgbEnabled);
             }
             field.setModified(false); // At this point, no changes have been done
         }
 
         BaseListener nextListener = (FileListener)this.getNextEnabledListener();
         while (nextListener != null)
         {
             boolean bOldState = nextListener.setEnabledListener(false);  // Don't allow it to be called again
             ((FileListener)nextListener).doNewRecord(bDisplayOption);
             nextListener.setEnabledListener(bOldState);
             nextListener = nextListener.getNextEnabledListener();
         }
         this.doNewRecord(bDisplayOption);
 
         for (iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= iLastField; ++iFieldSeq)
         {
             field = this.getField(iFieldSeq);
             field.handleFieldChanged(bDisplayOption, DBConstants.INIT_MOVE);        // Do BaseField Changed Behaviors
         }
         for (iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= iLastField; ++iFieldSeq)
         {
             field = this.getField(iFieldSeq);
             if (this.getEditMode() == DBConstants.EDIT_ADD) // Should be (who know, someone may have written it already)
                 field.setModified(false);   // At this point, no changes have been done
             if (bDisplayOption)         // BaseField was displayed in BaseField.setData
                 field.displayField();   // Display this field
         }
     }
     /**
      * Called when a new blank record is required for the table/query.
      * @param bDisplayOption If true display the fields on the screen.
      */
     public void doNewRecord(boolean bDisplayOption)         // init this field override for other value
     {   // So what... there's a new record
         return;
     }
     /**
      * Called when a valid record is read from the table/query.
      */
     public void handleValidRecord()                 // init this field override for other value
     {
         this.handleValidRecord(this.getDisplayOption());
     }
     /**
      * Called when a valid record is read from the table/query.
      * This method calls doValidRecord for all the listeners, and handleFieldChange(read) for all the fields.
      * @param bDisplayOption Display the fields on the screen?
      */
     public void handleValidRecord(boolean bDisplayOption)               // init this field override for other value
     {
         // Now, Notify all the fields that they have new data 
         int iFieldSeq;
         BaseField field = null;
         int iLastField = this.getFieldCount() + DBConstants.MAIN_FIELD - 1;   // Are these lines really needed?
 
         for (iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= iLastField; ++iFieldSeq)
         {
             field = this.getField(iFieldSeq);
             field.handleFieldChanged(bDisplayOption, DBConstants.READ_MOVE);        // Do BaseField Changed Behaviors
         }
         BaseListener nextListener = this.getNextEnabledListener();
         while (nextListener != null)
         {
             boolean bOldState = nextListener.setEnabledListener(false);  // Don't allow it to be called again
             ((FileListener)nextListener).doValidRecord(bDisplayOption);
             nextListener.setEnabledListener(bOldState);
             nextListener = nextListener.getNextEnabledListener();
         }
         this.doValidRecord(bDisplayOption);
         
         for (iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq <= iLastField; ++iFieldSeq)
         {
             field = this.getField(iFieldSeq);
             field.setModified(false);   // At this point, no changes have been done
             if (bDisplayOption)
                 field.displayField();   // Display this field
         }
     }
     /**
      * Called when a valid record is read from the table/query.
      * @param bDisplayOption If true display the fields on the screen.
      */
     public void doValidRecord(boolean bDisplayOption)               // init this field override for other value
     {   // So what... there's a valid record
         return;
     }
     /**
      * Called when a change is the record status is about to happen/has happened.
      * @param iChangeType The type of change.
      * @return An error code.
      */
     public int handleRecordChange(int iChangeType)      // init this field override for other value
     {
         return this.handleRecordChange(null, iChangeType, this.getDisplayOption());
     }
     /**
      * Called when a change is the record status is about to happen/has happened.
      * <p />NOTE: This is where the notification message is sent after an ADD, DELETE, or UPDATE.
      * @param field If the change is due to a field, pass the field.
      * @param iChangeType The type of change.
      * @param bDisplayOption If true display the fields on the screen.
      * @return An error code.
      */
     public int handleRecordChange(FieldInfo field, int iChangeType, boolean bDisplayOption)     // init this field override for other value
     {
         if (iChangeType == DBConstants.FIELD_CHANGED_TYPE)
         { // If a field changed, only pass the first time through
             if (this.getEditMode() == Constants.EDIT_NONE)
                 return DBConstants.NORMAL_RETURN; // No status = no action
             if (this.getEditMode() == Constants.EDIT_IN_PROGRESS) // No status = no action
                 return DBConstants.NORMAL_RETURN; // Already locked
             if (this.getEditMode() == Constants.EDIT_ADD) // No status = no action
                 if ((this.getOpenMode() & DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY) != DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY)
                     return DBConstants.NORMAL_RETURN; // Don't do this listener on an Add unless OPEN_REFRESH_AND_LOCK_ON_CHANGE.
             // NOTE: fall through if ((this.GetEditMode() == Constants.EDIT_CURRENT)    // No status = no action
         }
         // Do file listeners
         int iErrorCode = DBConstants.NORMAL_RETURN;
         if (m_table != null)
             iErrorCode = this.getTable().doRecordChange(field, iChangeType, bDisplayOption);    // Give the table a shot at it
         if (iErrorCode != DBConstants.NORMAL_RETURN)
             return iErrorCode;
         BaseListener nextListener = this.getNextEnabledListener();
         while (nextListener != null)
         {
             int iOldEditMode = this.getEditMode();
             boolean bOldState = nextListener.setEnabledListener(false);  // Don't allow it to be called again
             iErrorCode = ((FileListener)nextListener).doRecordChange(field, iChangeType, bDisplayOption);
             nextListener.setEnabledListener(bOldState);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 break;
             if (iOldEditMode != this.getEditMode())
                 if (this.getEditMode() == DBConstants.EDIT_IN_PROGRESS)
                     if (iOldEditMode != DBConstants.EDIT_IN_PROGRESS)
                         this.handleRecordChange(DBConstants.LOCK_TYPE); // If the record was auto-locked, make sure the listeners know.
             nextListener = nextListener.getNextEnabledListener();
         }
         if (iErrorCode == DBConstants.NORMAL_RETURN)
             iErrorCode = this.doRecordChange(field, iChangeType, bDisplayOption);
         // Messaging
         if ((iChangeType == DBConstants.AFTER_UPDATE_TYPE)
             || (iChangeType == DBConstants.AFTER_ADD_TYPE)
             || (iChangeType == DBConstants.AFTER_DELETE_TYPE))
         { // If the record was updated, notify any other copies of this same record
             try   {
                 Object dsBookmark = this.getHandle(DBConstants.BOOKMARK_HANDLE);
                 if (iChangeType == DBConstants.AFTER_ADD_TYPE)
                     dsBookmark = this.getLastModified(DBConstants.BOOKMARK_HANDLE);
                 if (dsBookmark != null)
                     if (this.getTable() != null)
                         if ((this.getDatabaseType() & DBConstants.UNSHAREABLE_MEMORY) != DBConstants.UNSHAREABLE_MEMORY)    // Don't send any messages for unshared tables
                 {
                     Map<String,Object> mapHints = null;
                     if (iChangeType == DBConstants.AFTER_ADD_TYPE)
                     {   // For new record, need some hints to tell the filter if it needs to add this record.
                         for (int iKeyArea = 1; iKeyArea < this.getKeyAreaCount(); iKeyArea++)
                         {   // Add any keys that have reference fields.
                             KeyArea keyArea = this.getKeyArea(iKeyArea);
                             BaseField fieldOne = keyArea.getField(0);
                             if (fieldOne instanceof ReferenceField)
                             {
                                 String strKey = keyArea.getKeyName();
                                 Object dsData = fieldOne.getData();
                                 if (dsData != null)
                                 {
                                     if (mapHints == null)
                                         mapHints = new Hashtable<String,Object>();
                                     mapHints.put(strKey, dsData);
                                 }
                             }
                         }
                     }
                     BaseMessage message = new RecordMessage(new RecordMessageHeader(this, dsBookmark, this, iChangeType, mapHints));
                     if (this.getRecordOwner() != null)
                         if (this.getRecordOwner().getTask() instanceof LocalTask)
                     {
                         RemoteTask messageTask = ((LocalTask)this.getRecordOwner().getTask()).getRemoteMessageTask();
                         if (this.getSupressRemoteMessages())
                             message.setProcessedByClientSession(messageTask);   // Don't echo back up to my server.
                     }
                     else
                     {
                         if (DBConstants.TRUE.equals(this.getTable().getCurrentTable().getProperty(DBParams.SUPRESSREMOTEDBMESSAGES)))
                             message.setProcessedByServer(true); // Don't send this message down to the server.
                     }
                     if ((DBConstants.REMOTE != (this.getDatabaseType() & DBConstants.TABLE_TYPE_MASK))   // Track remotely for remote files only
                         && (DBConstants.REMOTE_MEMORY != (this.getDatabaseType() & DBConstants.TABLE_TYPE_MASK)))
                             message.setProcessedByServer(true); // Don't send this message down to the server.
                     BaseDatabase database = this.getTable().getCurrentTable().getDatabase();
                     if (DBConstants.FALSE.equals(database.getProperty(DBParams.MESSAGES_TO_REMOTE)))
                         message.setProcessedByServer(true); // Don't send this message down to the server.
                     if (this.getTask() != null)
                         if (this.getTask().getApplication() instanceof Application)	// Always
                         if (((Application)this.getTask().getApplication()).getMessageManager() != null)
                         	((Application)this.getTask().getApplication()).getMessageManager().sendMessage(message);  // Have database notify everyone else
                 }
             } catch (DBException ex)    {
                 ex.printStackTrace();
             }
         }
         return iErrorCode;
     }
     /**
      * Called when a change to the record status is about to happen/has happened.
      * <p />NOTE: This is where the special logic to refresh the record/create the key on change exists.
      * @param field If the change is due to a field, pass the field.
      * @param iChangeType The type of change.
      * @param bDisplayOption If true display the fields on the screen.
      * @return An error code.
      */
     public int doRecordChange(Field field, int iChangeType, boolean bDisplayOption)
     {   // So what... there's a new record
         if (iChangeType == DBConstants.FIELD_CHANGED_TYPE)
         { // When the first field changes, lock the record!
             Utility.getLogger().info(this.getTableNames(false) + " Record field changed");
             if ((this.getOpenMode() & DBConstants.OPEN_LOCK_ON_CHANGE_STRATEGY) == DBConstants.OPEN_LOCK_ON_CHANGE_STRATEGY)
             {
                 Task task = null;
                 if (this.getRecordOwner() != null)
                     task = this.getRecordOwner().getTask();
                 //if (task == null)
                 //    task = BaseApplet.getSharedInstance();
                 Object[] rgobjEnabledFields = null;
                 try   {
                     if (this.getEditMode() == Constants.EDIT_ADD) // No status = no action
                         if ((this.getOpenMode() & DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY) == DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY)
                     {   // Special case - Write/lock on addNew()
                         if ((field != null)
                             && (field.isVirtual()))
                                 return DBConstants.NORMAL_RETURN;   // Don't acknowledge a virtual field change
                         if ((field instanceof CounterField)
                             || (this.getCounterField() == null))
                                 return DBConstants.NORMAL_RETURN;   // Don't acknowledge a counter field change or a non auto file
                         this.handleRecordChange(DBConstants.REFRESH_TYPE);     // Tell listeners I'm going to refresh the record
                         rgobjEnabledFields = this.setEnableNonFilter(null, false, false, false, false, false);
                         boolean[] rgbModified = this.getModified();
                         this.add();
                         Object varBookmark = this.getLastModified(DBConstants.DATA_SOURCE_HANDLE);
                         this.getTable().setHandle(varBookmark, DBConstants.DATA_SOURCE_HANDLE);
                         this.edit();        // Lock this record
                         this.setEditMode(this.getEditMode() | DBConstants.EDIT_REFRESHED);  // Special Mode
                         this.setModified(rgbModified);  // This is required, so any file listeners looking for a modified field on add will see this modified field
                         this.setEnableNonFilter(rgobjEnabledFields, false, false, false, false, false);   // Re-enable all file behaviors
                         rgobjEnabledFields = null;
                         return this.handleRecordChange(DBConstants.AFTER_REFRESH_TYPE);     // Tell listeners I refreshed the record
                     }
                     int iErrorCode = this.edit();        // Lock this record
                     if (iErrorCode != DBConstants.NORMAL_RETURN)
                     {
                         if (iErrorCode == DBConstants.ERROR_RETURN)
                         {
                             String strError = "Record locked";
                             if (task != null)
                             {
                                 strError = ((BaseApplication)task.getApplication()).getResources(ResourceConstants.ERROR_RESOURCE, true).getString(strError);
                                 return task.setLastError(strError);
                             }
                         }
                         return iErrorCode;  // Error
                     }
                 }
                 catch(DBException ex)  {
                     if (task != null)
                     {
                         String RECORD_LOCKED_BY_MESSAGE = "Record locked by ";
                         String strError = ex.getMessage();
                         if ((strError != null) && (strError.startsWith(RECORD_LOCKED_BY_MESSAGE)))
                             strError = ((BaseApplication)task.getApplication()).getResources(ResourceConstants.ERROR_RESOURCE, true).getString(strError.substring(0, RECORD_LOCKED_BY_MESSAGE.length() - 1)) + strError.substring(RECORD_LOCKED_BY_MESSAGE.length() - 1);
                         else
                             strError = ((BaseApplication)task.getApplication()).getResources(ResourceConstants.ERROR_RESOURCE, true).getString(strError);
                         return task.setLastError(strError);
                     }
                     return ex.getErrorCode();
                 } finally {
                     if (rgobjEnabledFields != null)
                         this.setEnableNonFilter(rgobjEnabledFields, false, false, false, false, false);   // Re-enable all file behaviors
                 }
             }
         }
         return DBConstants.NORMAL_RETURN;
     }
     /**
      * Have any fields Changed?
      * @return true if any fields have changed.
      */
     public boolean isModified()
     {
         return this.isModified(false);
     }
     /**
      * Have any fields Changed?
      * @param bNonKeyOnly If we are talking about non current key fields only.
      * @return true if any fields have changed.
      */
     public boolean isModified(boolean bNonKeyOnly)
     {
         int fieldCount = this.getFieldCount();  // BaseField Count
         for (int fieldSeq = DBConstants.MAIN_FIELD; fieldSeq < fieldCount+DBConstants.MAIN_FIELD; fieldSeq++)
         {
             BaseField field = this.getField(fieldSeq);
             if (field.isModified()) if (!field.isVirtual()) // If not public and has been modified...
             {
                 boolean bSkip = false;
                 if (bNonKeyOnly)
                 {
                     KeyArea index = this.getKeyArea(-1);
                     for (int i = DBConstants.MAIN_KEY_FIELD; i < index.getKeyFields(); i++)
                     {
                         if (field == index.getField(i))
                         {
                             bSkip = true; // Skip this one
                             break;
                         }
                     }
                 }
                 if (!bSkip)
                     return true;            // A field has been changed
             }
         }
         return false;   // No Changes YET
     }
     /**
      * Get the field's modified status
      * @param bNonKeyOnly If we are talking about non current key fields only.
      * @return true if any fields have changed.
      */
     public boolean[] getModified()
     {
         int iFieldCount = this.getFieldCount();  // BaseField Count
         boolean[] rgbModified = new boolean[iFieldCount];
         for (int iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq < iFieldCount + DBConstants.MAIN_FIELD; iFieldSeq++)
         {
             BaseField field = this.getField(iFieldSeq);
             rgbModified[iFieldSeq] = field.isModified();
         }
         return rgbModified;
     }
     /**
      * Restore the field's modified status to this.
      * @param bNonKeyOnly If we are talking about non current key fields only.
      * @return true if any fields have changed.
      */
     public void setModified(boolean[] rgbModified)
     {
         int iFieldCount = this.getFieldCount();  // BaseField Count
         for (int iFieldSeq = DBConstants.MAIN_FIELD; iFieldSeq < iFieldCount + DBConstants.MAIN_FIELD; iFieldSeq++)
         {
             BaseField field = this.getField(iFieldSeq);
             if (iFieldSeq < rgbModified.length)
                 field.setModified(rgbModified[iFieldSeq]);
         }
     }
     /**
      * Are there any null fields which can't be null?
      * @return true If a non-nullable field is null.
      * @return false If the fields are okay.
      */
     public boolean isNull()
     {   // Return true if all non null fields have data in them
         int fieldCount = this.getFieldCount();  // BaseField Count
         for (int fieldSeq = DBConstants.MAIN_FIELD; fieldSeq < fieldCount+DBConstants.MAIN_FIELD; fieldSeq++)
         {
             BaseField field = this.getField(fieldSeq);
             if ((!field.isNullable()) && (field.isNull()))
                 return true;            // This field can't be null!!!
         }
         return false;   // All fields okay
     }
     /**
      * Is this record's table open?
      * @return true If the table is open.
      */
     public boolean isOpen()
     {
         if (this.getTable() == null)
             return false;
         return this.getTable().isOpen();
     }
     /**
      * Is this record a table (or a query stmt)?
      * This class is a table.
      * @return true If this is a queryrecord.
      */
     public boolean isQueryRecord()
     {
         return false;
     }
     /**
      * Make a new key index.
      * @param bUnique True if this is a unique key.
      * @param The keyName.
      * @return The new KeyArea.
      */
     public KeyArea makeIndex(int bUnique, String strKeyName)
     {
         return new KeyArea(this, bUnique, strKeyName);
     }
     /**
      * Create a default document for file maintenance or file display.
      * Usually overidden in the file's record class.
      * @param itsLocation The location of the screen in the parentScreen (usually null here).
      * @param parentScreen The parent screen.
      * @param iDocMode The type of screen to create (MAINT/DISPLAY/SELECT/MENU/etc).
      * @return The new screen.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent screenParent, int iDocMode, Map<String, Object> properties)  // Standard file maint for this record (returns new record)
     {   // This is almost always overidden!
         if (m_recordOwner != null) if (m_recordOwner instanceof ScreenParent)
             return (ScreenParent)m_recordOwner;
         ScreenParent screen = null;
         if ((iDocMode & ScreenConstants.MAINT_MODE) == ScreenConstants.MAINT_MODE)
             screen = Record.makeNewScreen(ScreenModel.SCREEN, itsLocation, screenParent, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.DISPLAY_MODE) == ScreenConstants.DISPLAY_MODE)
             screen = Record.makeNewScreen(ScreenModel.GRID_SCREEN, itsLocation, screenParent, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.SELECT_MODE) == ScreenConstants.SELECT_MODE)
             screen = Record.makeNewScreen(ScreenModel.GRID_SCREEN, itsLocation, screenParent, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MENU_MODE) == ScreenConstants.MENU_MODE)
             screen = Record.makeNewScreen(ScreenModel.BASE_MENU_SCREEN, itsLocation, screenParent, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else
             screen = Record.makeNewScreen(ScreenModel.GRID_SCREEN, itsLocation, screenParent, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         return (ScreenParent)screen;
     }
     /**
      * Clone, read same record, and create screen.
      * DONT OVERRIDE THIS METHOD!
      * @param itsLocation The location of the screen in the parentScreen (usually null here).
      * @param parentScreen The parent screen.
      * @param iDocMode The type of screen to create (MAINT/DISPLAY/SELECT/MENU/etc).
      * @param bCloneThisQuery If true, use a clone of this record, if false use this record.
      * @param bReadCurrentRecord Read the currently selected record for the new query?
      * @param bUseBaseTable Use the base table for the new query (usually true).
      * @param bLinkGridToQuery Link the new query to this record?
      * @return The new screen.
      */
     public final ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parent, int iDocMode, boolean bCloneThisQuery, boolean bReadCurrentRecord, boolean bUseBaseTable, boolean bLinkGridToQuery, Map<String,Object> properties)
     {
         Record recordNew = this;
         if (bCloneThisQuery)
         {
             boolean bUseConcreteClass = true;
             if ((recordNew.getDatabaseType() & DBConstants.BASE_TABLE_CLASS) == DBConstants.BASE_TABLE_CLASS)
                 if ((iDocMode & ScreenConstants.DISPLAY_MODE) == ScreenConstants.DISPLAY_MODE)
                     bUseConcreteClass = false;  // Typically for displays, you want to use the abstract class
             if (bUseBaseTable)
             {
                 recordNew = recordNew.getBaseRecord();
                 if (recordNew.getTable() != null)
                     if (bUseConcreteClass)
                         if (recordNew.getTable().getCurrentTable() != null)
                             recordNew = recordNew.getTable().getCurrentTable().getRecord().getBaseRecord();
             }
             try   {
                 recordNew = (Record)recordNew.clone();
             } catch (CloneNotSupportedException ex)   {
                 return null;
             }
             Record recordCurrent = this;
             if (bUseBaseTable)
             {
                 if (recordCurrent.getTable() != null)
                     if (bUseConcreteClass)
                         if (recordCurrent.getTable().getCurrentTable() != null)
                             recordCurrent = recordCurrent.getTable().getCurrentTable().getRecord();
             }
             if ((bReadCurrentRecord)
                 && ((recordCurrent.getEditMode() == Constants.EDIT_CURRENT) || (recordCurrent.getEditMode() == Constants.EDIT_IN_PROGRESS)))
             {
                 if (bUseBaseTable)
                     recordCurrent = recordCurrent.getBaseRecord();
                 boolean bRefreshIfChanged = true;
                 if (parent != null)
                     if (this.getRecordOwner() != null)   // BasePanel
                         if (this.getRecordOwner().getParentRecordOwner() == parent)
                             bRefreshIfChanged = false;  // Since the screen (and record) will be closed, only need to write record
                 recordNew.readSameRecord(recordCurrent, true, bRefreshIfChanged);  // Write if it is changed
             }
             else if ((bReadCurrentRecord)
                 && (recordCurrent.getEditMode() == Constants.EDIT_ADD))
             {
                 if (bUseBaseTable)
                     recordCurrent = recordCurrent.getBaseRecord();
                 try   {
                     recordNew.addNew();
                     recordNew.moveFields(recordCurrent, null, true, DBConstants.INIT_MOVE, false, false, false);
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 }
             }
             else
             {
                 try   {
                     recordNew.addNew();
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 }
             }
         }
         if (parent != null)
         {
             if (this.getRecordOwner() != null)
                 if (this.getRecordOwner().getParentRecordOwner() == parent)
             {
                 this.getRecordOwner().free();    // Warning, this also closes "this" record.
                 bLinkGridToQuery = false;   // Can't link to a closed query.
             }
         }
         if (properties != null)
         {
             Task task = parent.getTask();
             task.setProperties(properties);
         }
         BaseAppletReference applet = null;
         if (parent.getTask() instanceof BaseAppletReference)
         	applet = (BaseAppletReference)parent.getTask();
         Object oldCursor = null;
         if (applet != null)
         	oldCursor = applet.setStatus(Constants.WAIT, applet, null);
         ScreenParent screenNew = recordNew.makeScreen(itsLocation, parent, iDocMode, properties);
         if (applet != null)
             applet.setStatus(0, applet, oldCursor);
         if (screenNew == null)
         {
             if (bCloneThisQuery) if (recordNew != null)
             {
                 recordNew.free();
                 recordNew = null;
             }
             return null;
         }
         ScreenParent screenCurrent = null;
         if (this.getRecordOwner() instanceof ScreenParent)
             screenCurrent = (ScreenParent)this.getRecordOwner();
         if (screenCurrent == null)
             bLinkGridToQuery = false;
         if (bLinkGridToQuery)
         {
             if ((iDocMode & ScreenConstants.MAINT_MODE) != 0)
             {   // Building a form
                 if (/*(screenCurrent instanceof GridScreen) &&*/ (((ScreenParent)screenCurrent).getMainRecord() == this))
                     screenCurrent.setSelectQuery(recordNew, true);   // When user selects, reads from this file!
                 else
                     screenNew.setSelectQuery(this, false);         // When user reads or updates, reads from this file!
             }
             else
             {   // Building a display/select screen
                 boolean bSuccess = screenNew.setSelectQuery(this, true);          // When user selects, reads from this file!
                 if (!bSuccess)
                     if ((iDocMode & ScreenConstants.DETAIL_MODE) != 0)
                 {   // Linking to a detail screen
                     recordNew = (Record)screenNew.getHeaderRecord();
                     screenCurrent.setSelectQuery(recordNew, true);   // When user selects, reads from this file!
                 }
                 this.addDependentScreen(screenNew);        // When this closes, closes dependent screen
             }
         }
         return screenNew;
     }
     /**
      * Make a screen window and put the screen with this class name into it.
      * @param componentType The class of the new screen.
      * @param itsLocation The location of the new screen.
      * @param screenParent The parent of the new screen.
      * @param iDisplayFieldDesc Display the field desc?
      * @param mainRecord The main record
      * @param initScreen Call the screen init method?
      * @return The new screen.
      */
     public static ScreenParent makeNewScreen(String componentType, ScreenLoc itsLocation, RecordOwnerParent screenParent, int iDisplayFieldDesc, Map<String, Object> properties, Rec mainRecord, boolean initScreen)
     {
         String screenClass = null;
         if (!componentType.contains("."))
             screenClass = ScreenModel.BASE_PACKAGE + componentType;
         else if (componentType.startsWith("."))
             screenClass = DBConstants.ROOT_PACKAGE + componentType.substring(1);
         else
             screenClass = componentType;
         ScreenParent screen = (ScreenParent)ClassServiceUtility.getClassService().makeObjectFromClassName(screenClass);
         if (screen == null)
         {
             Utility.getLogger().warning("Screen component not found " + componentType);
             screen = (ScreenParent)ClassServiceUtility.getClassService().makeObjectFromClassName(ScreenModel.BASE_PACKAGE + ScreenModel.SCREEN);
         }
         if (screen != null)
         {
             BaseAppletReference applet = null;
             if (screenParent.getTask() instanceof BaseAppletReference)
                 applet = (BaseAppletReference)screenParent.getTask();
             if (initScreen)
             {
                 Object oldCursor = null;
                 if (applet != null)
                     oldCursor = applet.setStatus(Constant.WAIT, applet, null);
                 Map<String,Object> screenProperties = new HashMap<String,Object>();
                 if (properties != null)
                     screenProperties.putAll(properties);
                 screenProperties.put(ScreenModel.DISPLAY, iDisplayFieldDesc);
                 if (itsLocation != null)
                     screenProperties.put(ScreenModel.LOCATION, itsLocation);
 
                 //if (((iDisplayFieldDesc & ScreenConstants.DETAIL_MODE) == ScreenConstants.DETAIL_MODE) && (screen instanceof DetailGridScreen))
                 //    ((DetailGridScreen)screen).init((Record)mainRecord, null, (ScreenLocation)itsLocation, (BasePanel)screenParent, null, iDisplayFieldDesc, properties);
                 //else
                     screen.init(screenParent, mainRecord, screenProperties);
 
                 if (applet != null)
                     applet.setStatus(0, applet, oldCursor);
             }
         }
         return screen;
     }
     /**
      * Convert the command to the screen document type.
      * @param strCommand The command text.
      * @param The standard document type (MAINT/DISPLAY/SELECT/MENU/etc).
      */
     public int commandToDocType(String strCommand)  // Standard file maint for this record (returns new record)
     {
         int iDocMode = ScreenConstants.MAINT_MODE;
         if ((MenuConstants.FORM.equalsIgnoreCase(strCommand))
             || (MenuConstants.FORMLINK.equalsIgnoreCase(strCommand)))
                 iDocMode = ScreenConstants.MAINT_MODE;
         else if ((MenuConstants.LOOKUP.equalsIgnoreCase(strCommand))
             || (MenuConstants.LOOKUPCLONE.equalsIgnoreCase(strCommand)))
                 iDocMode = ScreenConstants.DISPLAY_MODE;
         else if (MenuConstants.FORMDETAIL.equalsIgnoreCase(strCommand))
             iDocMode = ScreenConstants.DISPLAY_MODE | 64;
         else if (MenuConstants.POST.equalsIgnoreCase(strCommand))
             iDocMode = ScreenConstants.DISPLAY_MODE | 128;
         else if (MenuConstants.MENUREC.equalsIgnoreCase(strCommand))
             iDocMode = ScreenConstants.MENU_MODE;
         return iDocMode;
     }
     /**
      * Read the record in this BaseTable.
      * <p/>Note: If the recordCurrent has an edit in progress, it is not updated first,
      * so you should update the current record if you want the new record to show the updates.
      * <br/>Note: Be careful of concurrency issues... Use OnSelectHandler to update from one screen
      * to another.
      * @param currentRecord record that has the current record that I need to read.
      * @return true if successful.
      */
     public boolean readSameRecord(Record recordCurrent, boolean bWriteIfChanged, boolean bRefreshIfChanged)
     { // Read the same record as the supplied BaseTable
         try   {
             Record record = null;
             Object bookmark = null;
             if (recordCurrent != null)
                 bookmark = recordCurrent.getHandle(DBConstants.DATA_SOURCE_HANDLE);
             if (bookmark != null)
             {
                 if (recordCurrent.getEditMode() == Constants.EDIT_IN_PROGRESS)
                     if (recordCurrent.isModified())
                 {
                     if (bRefreshIfChanged)
                         recordCurrent.writeAndRefresh();    // Update before reading other
                     else if (bWriteIfChanged)
                         recordCurrent.set();    // Update before reading other
                 }
                 record = this.setHandle(bookmark, DBConstants.DATA_SOURCE_HANDLE);
             }
             if (record != null)
                 return true;
             this.addNew();  // Not found, start with a NEW record!
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         return false; // Not found
     }
     /**
      * Add another screen dependent on this record.
      * If this record is closed, so is the dependent screen.
      * @param screen Dependent screen to add.
      */
     public void addDependentScreen(ScreenParent screen)
     {
         if (m_depScreens == null)
             m_depScreens = new Vector<ScreenParent>();
         m_depScreens.addElement(screen);
         screen.setDependentQuery(this);
     }
     /**
      * Remove a dependent screen.
      * @param screen The screen to remove.
      */
     public void removeDependentScreen(ScreenParent screen)
     {
         if (m_depScreens == null)
             return;
         if (m_depScreens.contains(screen))
             m_depScreens.removeElement(screen);
         screen.setDependentQuery(null);
     }
     /**
      * By default display all the fields on change?
      * @param bDisplayOption Set default display mode.
      */
     public void setDisplayOption(boolean bDisplayOption)
     {
         m_bDisplayOption = bDisplayOption;
     }
     /**
      * Open this table (requery the table).
      * <p />NOTE: This is a table method, it is included here in Record for convenience!!!
      * @exception DBException File exception.
      */
     public void open() throws DBException
     {
         this.getTable().open();
     }
     /**
      * Close this table.
      * <p />NOTE: This is a table method, it is included here in Record for convience!!!
      */
     public void close()
     {
         if (m_table != null)    // Do not use getTable() as it will build a new table!
             this.getTable().close();
     }
     /**
      * Create a new empty record.
      * <p />Leave the current position unchanged, and
      * clear this record's fields to their initial values.
      * @exception DBException FILE_NOT_OPEN.
      */
     public void addNew() throws DBException
     {
         this.getTable().addNew();           // Clear the fields
     }
     /**
      * Add this record.
      * <p />For new records:<break/>
      * The new record is added to the table,
      * sometimes in the correct order, sometimes at the end of the table.
      * Leaves the current position unchanged.<p>
      * @exception DBException FILE_NOT_OPEN.
      * @exception DBException DUPLICATE_KEY.
      * @exception DBException RECORD_NOT_LOCKED - Record must be locked to Update an existing record.
      * @exception DBException INVALID_RECORD - Record not current.
      */
     public void add() throws DBException
     {
         this.getTable().add(this);
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
         return this.getTable().edit();
     }
     /**
      * Update this record.
      * For updated records:<p />
      *  This record is updated in the table. It is
      *  sometimes in the correct order, sometimes at the same location in the table.
      *  The current position is unknown, but is usually pointing to this record.
      * @exception DBException FILE_NOT_OPEN.
      * @exception DBException DUPLICATE_KEY.
      * @exception DBException RECORD_NOT_LOCKED - Record must be locked to Update an existing record.
      * @exception DBException INVALID_RECORD - Record not current.
      */
     public void set() throws DBException
     {
         this.getTable().set(this);
     }
     /**
      * Delete the current object.
      * @exception DBException   FILE_NOT_OPEN.
      * @exception DBException RECORD_NOT_LOCKED - Record must be locked to Update an existing record.
      * @exception DBException INVALID_RECORD - Record not current.
      */
     public void remove() throws DBException
     {
         this.getTable().remove();
     }
     /**
      * Move the position of the record relative to the current position.
      * @param iRelPosition - Relative position positive or negative or FIRST_RECORD/LAST_RECORD.
      * @exception DBException FILE_NOT_OPEN File is not open.
      * @exception DBException INVALID_RECORD - Record position is not current or move past EOF or BOF.
      * NOTE: This is a table method, it is included here in Record for convience!!!
      */
     public FieldList move(int iRelPosition) throws DBException
     {
         return this.getTable().move(iRelPosition);
     }
     public final Record moveFirst() throws DBException      {
         return (Record)this.move(DBConstants.FIRST_RECORD);   }
     public final Record moveLast() throws DBException   {
         return (Record)this.move(DBConstants.LAST_RECORD);  }
     public final Record next() throws DBException   {
         return (Record)this.move(DBConstants.NEXT_RECORD);  }
     public final Record previous() throws DBException   {
         return (Record)this.move(DBConstants.PREVIOUS_RECORD);  }
     /**
      * Is there another record before this one (is this not the first one)?
      * @return true If there is a previous record.
      */
     public boolean hasPrevious() throws DBException
     {
         return this.getTable().hasPrevious();
     }
     /**
      * Is there another record (is this not the last one)?
      * @return true If there is a next record.
      */
     public boolean hasNext() throws DBException
     {
         return this.getTable().hasNext();
     }
     /**
      * Read the record that matches this record's current key.
      * <p />NOTE: This is a table method, it is included here in Record for convience!!!
      * @exception DBException FILE_NOT_OPEN;
      * @exception DBException KEY_NOT_FOUND - The key was not found on read.
      */
     public boolean seek(String strSeekSign) throws DBException
     {
         return this.getTable().seek(strSeekSign);
     }
     /**
      * Get the DATA_SOURCE_HANDLE reference to the last modified or added record.
      * @param iHandleType The type of handle to get.
      * @return The bookmark, or null if no current record.
      */
     public Object getLastModified(int iHandleType)
     {   // So what... there's a new record
         return this.getTable().getLastModified(iHandleType);
     }
     /**
      * Get a unique key that can be used to reposition to this record.
      * <p />NOTE: This is a table method, it is included here in Record for convience!!!
      * @param iHandleType The type of handle to retrieve.
      * @return The handle to the current record.
      * @exception DBException File exception.
      */
     public Object getHandle(int iHandleType) throws DBException
     {
         return this.getTable().getHandle(iHandleType);
     }
     /**
      * Reposition to this record Using this bookmark.
      * <br>NOTE: This is a table method, it is included here in Record for convience!!!
      * @param bookmark The handle to use to position the record.
      * @param iHandleType The type of handle bookmark is.
      * @exception DBException FILE_NOT_OPEN.
      * @return  <code>valid record</code> - record found;
      *  <code>null</code> - record not found
      */
     public Record setHandle(Object bookmark, int iHandleType) throws DBException
     {
         return (Record)this.getTable().setHandle(bookmark, iHandleType);
     }
     /**
      * Create and initialize the record that has this class name.
      * @param strClassName Full class name.
      * @param recordOwner The recordowner to add this record to.
      * @return The new record.
      */
     public static Record makeRecordFromClassName(String strClassName, RecordOwner recordOwner)
     {
         return Record.makeRecordFromClassName(strClassName, recordOwner, true, true);
     }
     /**
      * Create and initialize the record that has this class name.
      * @param className Full class name.
      * @param recordOwner The recordowner to add this record to.
      * @param bErrorIfNotFound Display an error if not found.
      * @return The new record.
      */
     public static Record makeRecordFromClassName(String className, RecordOwner recordOwner, boolean bInitRecord, boolean bErrorIfNotFound)
     {
         Record record = null;
         try {
             record = (Record)ClassServiceUtility.getClassService().makeObjectFromClassName(className, null, bErrorIfNotFound);
         } catch (RuntimeException ex) {
         	if (className.startsWith("."))
         		if (recordOwner != null)
         			if (!recordOwner.getClass().getName().startsWith(BundleConstants.ROOT_PACKAGE))
 			{
 				String packageName = Utility.getPackageName(recordOwner.getClass().getName());
 				int domainEnd = packageName.indexOf(".");
 				if (domainEnd != -1)
 					domainEnd = packageName.indexOf(".", domainEnd + 1);
 				if (domainEnd != -1)
 				{
 					className = ClassServiceUtility.getFullClassName(packageName.substring(0, domainEnd), null, className);
 					ex = null;
 					try {
 						record = (Record)ClassServiceUtility.getClassService().makeObjectFromClassName(className, null, bErrorIfNotFound);
 			        } catch (RuntimeException e) {
 			        	ex = e;
 			        }
 				}
 				if (ex != null)
 					recordOwner.getTask().setLastError(ex.getMessage());
 			}
         }
         if (bInitRecord)
             if (record != null)
                 record.init(recordOwner);
         return record;
     }
     /**
      * Get the autosequence field if it exists.
      * @return The counterfield or null.
      */
     public FieldInfo getCounterField()
     {
         KeyArea keyArea = this.getKeyArea(DBConstants.MAIN_KEY_AREA);
         if (keyArea != null)
         {
             BaseField fldID = keyArea.getKeyField(DBConstants.MAIN_KEY_FIELD).getField(DBConstants.FILE_KEY_AREA);
             if (fldID instanceof CounterField)
                 return (CounterField)fldID;
         }
         return null;    // None
     }
     /**
      * Does this fieldlist have a primary key that is auto-sequence.
      * This can be turned off (in thick) by setting table.setProperty(AUTO_SEQUENCE_KEY, FALSE),
      * which is primarily used to restore an archive with changing the autosequenced values.
      * Note: Thin FieldList's ALWAYS have auto-sequence keys.
      * @return True if autosequence key.
      */
     public boolean isAutoSequence()
     {
         if (this.getCounterField() != null)
             if (!DBConstants.FALSE.equalsIgnoreCase(this.getTable().getProperty(SQLParams.AUTO_SEQUENCE_ENABLED)))
                 return super.isAutoSequence();  // Yes, this record has an autosequence key that is enabled.
         return false;
     }
     /**
      * Move the fields from the source record, using this translation table.
      * This is a utility method, used mostly for edi/xml/corba table translation.
      * @param srcRecord The Source record.
      * @param resource The property file to use to tranlate source field to dest field
      * if NAME_TRANSLATION = move all same name.
      * if null, move the identical sequence number.
      * @return true if the fields were moved.
      */
     public static final ResourceBundle MOVE_BY_NAME = new ListResourceBundle()
     {
         public Object[][] getContents()
         {
             return null;
         }
     };
     /**
      * Copy all the fields from one record to another.
      * @param recSource
      * @param resource
      * @param bDisplayOption
      * @param iMoveMode
      * @param bAllowFieldChange
      * @param bOnlyModifiedFields
      * @param bMoveModifiedState
      * @return true if successful
      */
     public boolean moveFields(Record recSource, ResourceBundle resource, boolean bDisplayOption, int iMoveMode, boolean bAllowFieldChange, boolean bOnlyModifiedFields, boolean bMoveModifiedState)
     {
         boolean bFieldsMoved = false;
         for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
         {
             BaseField fldDest = this.getField(iFieldSeq);
             BaseField fldSource = null;
             try   {
                 if (resource == Record.MOVE_BY_NAME)
                 {
                     String strSourceField = fldDest.getFieldName();
                     fldSource = recSource.getField(strSourceField);
                 }
                 else if (resource != null)
                 {
                     String strSourceField = resource.getString(fldDest.getFieldName());    // Lookup source field name from dest
                     if ((strSourceField != null) && (strSourceField.length() > 0))
                         fldSource = recSource.getField(strSourceField);
                 }
                 else // if (resource == null)
                 {
                     fldSource = recSource.getField(iFieldSeq);
                 }
             } catch (MissingResourceException ex) {
                 fldSource = null;
             }
             if (fldSource != null)
             {
                 if ((!bOnlyModifiedFields) || (fldSource.isModified()))
                 {
                     boolean[] rgbEnabled = null;
                     if (bAllowFieldChange == false)    // Don't enable a disabled field listener
                         rgbEnabled = fldDest.setEnableListeners(bAllowFieldChange);                  // Don't call the HandleFieldChanged beh until ALL inited!
                     
                     FieldDataScratchHandler listenerScratchData = null;
                     if ((bAllowFieldChange == false) && (bMoveModifiedState))
                     	if (iMoveMode == DBConstants.READ_MOVE)	// Very obscure - Make sure this fake read move does not change the scratch area
                     		if (fldDest.getNextValidListener(iMoveMode) instanceof FieldDataScratchHandler)	// Always enable would have to be true since listeners have been disabled
                     			(listenerScratchData = ((FieldDataScratchHandler)fldDest.getNextValidListener(iMoveMode))).setAlwaysEnabled(false);
 
                     fldDest.moveFieldToThis(fldSource, bDisplayOption, iMoveMode);
                     
                     if (listenerScratchData != null)
                     	listenerScratchData.setAlwaysEnabled(true);
                     
                     if (rgbEnabled != null)
                         fldDest.setEnableListeners(rgbEnabled);
                     if (bAllowFieldChange == false)
                         fldDest.setModified(false); // At this point, no changes have been done
                     bFieldsMoved = true;
                 }
                 if ((bAllowFieldChange) || (bMoveModifiedState))
                     fldDest.setModified(fldSource.isModified()); // If you allow field change set if source is modified.
             }
         }
         return bFieldsMoved;
     }
     /**
      * Set up a listener to notify when an external change is made to the current record.
      * @param listener The listener to set to the new filter. If null, use the record's recordowner.
      * @param bTrackMultipleRecord Use a GridRecordMessageFilter to watch for multiple records.
      * @param bAllowEchos Allow this record to be notified of changes (usually for remotes hooked to grid tables).
      * @param bReceiveAllAdds If true, receive all add notifications, otherwise just receive the adds on secondary reads.
      * @return The new filter.
      */
     public BaseMessageFilter setupRecordListener(JMessageListener listener, boolean bTrackMultipleRecords, boolean bAllowEchos)
     {
         boolean bReceiveAllAdds = false;
         if (((this.getDatabaseType() & DBConstants.TABLE_MASK) == DBConstants.TABLE)
             || ((this.getDatabaseType() & DBConstants.TABLE_MASK) == DBConstants.LOCAL)
             || ((this.getDatabaseType() & DBConstants.TABLE_MASK) == DBConstants.REMOTE_MEMORY))
                 bReceiveAllAdds = true;    // The volume of changes is negligable and there is rarely a secondary, so listen for all changes
         return this.setupRecordListener(listener, bTrackMultipleRecords, bAllowEchos, bReceiveAllAdds);
     }
     /**
      * Set up a listener to notify when an external change is made to the current record.
      * @param listener The listener to set to the new filter. If null, use the record's recordowner.
      * @param bTrackMultipleRecord Use a GridRecordMessageFilter to watch for multiple records.
      * @param bAllowEchos Allow this record to be notified of changes (usually for remotes hooked to grid tables).
      * @param bReceiveAllAdds If true, receive all add notifications, otherwise just receive the adds on secondary reads.
      * @return The new filter.
      */
     public BaseMessageFilter setupRecordListener(JMessageListener listener, boolean bTrackMultipleRecords, boolean bAllowEchos, boolean bReceiveAllAdds)
     {
         BaseMessageFilter filter = null;
         MessageManager messageManager = null;
         if (listener == null)
             listener = this.getRecordOwner();
         if (listener != null)
             if (this.getRecordOwner() != null)
                 if (this.getRecordOwner().getTask() != null)
                     messageManager = ((Application)this.getRecordOwner().getTask().getApplication()).getMessageManager();
         if (messageManager != null)
         {
             Object source = null;
             if (bAllowEchos)
                 source = DBConstants.BLANK;     // Non-null object that will never be a source
             if (!bTrackMultipleRecords)
                 filter = new RecordMessageFilter(this, source);
             else
                 filter = new GridRecordMessageFilter(this, source, bReceiveAllAdds);
             filter.addMessageListener(listener);
             messageManager.addMessageFilter(filter);
         }
         return filter;
     }
     /**
      * Get the task for this field list.
      * This is a convience method which calls this.getRecordOwner().getTask() in the thick model.
      * @return The task which contains this fieldList's recordowner Guaranteed to to non-null.
      */
     public Task getTask()
     {
         if (this.getRecordOwner() != null)
             return this.getRecordOwner().getTask();
         Task task = super.getTask();   // Null
         if (task == null)
         {   // Look a little harder
        	if (this.getTable().getDatabase().getDatabaseOwner() != null)
        		if (((Environment)this.getTable().getDatabase().getDatabaseOwner().getEnvironment()).getDefaultApplication() != null)
            task = ((BaseApplication)((Environment)this.getTable().getDatabase().getDatabaseOwner().getEnvironment()).getDefaultApplication()).getMainTask();
         }
         return task;
     }
     /**
      * Don't set record messages to your client?
      * @param boolean bSupressRemoteMessages If set, don't send record messages to your client.
      */
     public void setSupressRemoteMessages(boolean bSupressRemoteMessages)
     {
         m_bSupressRemoteMessages = bSupressRemoteMessages;
     }
     /**
      * Don't set record messages to your client?
      * @return boolean bSupressRemoteMessages If set, don't send record messages to your client.
      */
     public boolean getSupressRemoteMessages()
     {
         return m_bSupressRemoteMessages;
     }
     /**
     * Optimize the query by only selecting the fields which are being displayed.
     */
     public void selectScreenFields()
     {
         if (this.isOpen())
             return;   // Can't select after it's open
 //x        this.setSelected(false);
 //        for (Enumeration e = m_SFieldList.elements() ; e.hasMoreElements() ;)
         {   // This should only be called for Imaged GridScreens (Child windows would be deleted by now if Component)
 //            ScreenField sField = (ScreenField)e.nextElement();
 //            if (sField.getConverter() != null) if (sField.getConverter().getField() != null)
 //                ((BaseField)sField.getConverter().getField()).setSelected(true);
         }
         Record recordBase = this.getBaseRecord();     // Get an actual Record to add/edit/etc...
         if (recordBase != null)
         {   // Make sure the main key area's keys are selected
             for (int iIndex = DBConstants.MAIN_FIELD; iIndex < recordBase.getFieldCount(); iIndex++)
             {
                 boolean bSelect = false;    // By default
                 BaseField field = recordBase.getField(iIndex);
                 if (field.isVirtual())
                     continue;   // Who cares if a virtual field is selected or not? (actually, I sometimes need to auto-cache virtuals).
                 if (field.getComponent(0) != null)
                     bSelect = true;        // If you are linked to a screen field, you should be selected
                 else
                 {
                     FieldListener listener = field.getListener();
                     while (listener != null)
                     {
                         if (listener.respondsToMode(DBConstants.READ_MOVE))
                         {
                             if ((!field.isVirtual())
                                 || (listener instanceof ReadSecondaryHandler))
                                     bSelect = true;
                         }
                         listener = (FieldListener)listener.getNextListener(); // Next listener in chain
                     }
                 }
                 field.setSelected(bSelect); // Select/deselect field
             }
             KeyArea keyArea = recordBase.getKeyArea(DBConstants.MAIN_KEY_AREA);
             int iLastIndex = keyArea.getKeyFields() + DBConstants.MAIN_FIELD;
             for (int iIndexSeq = DBConstants.MAIN_FIELD; iIndexSeq < iLastIndex; iIndexSeq++)
             {   // Make sure the index key is selected!
                 keyArea.getField(iIndexSeq).setSelected(true);
             }
         }
         this.selectFields();      // This will do nothing, unless there is a specific selection method
     }
     /**
      * Write this record and re-read if (if it has been modified).
      * @return the bookmark.
      */
     public Object writeAndRefresh() throws DBException
     {
         return this.writeAndRefresh(true);
     }
     /**
      * Write this record and re-read if (if it has been modified).
      * @return the bookmark.
      */
     public Object writeAndRefresh(boolean bRefreshDataIfNoMods) throws DBException
     {
         if (!this.isModified())
         {
             if ((this.getEditMode() == DBConstants.EDIT_IN_PROGRESS)
                     || (this.getEditMode() == DBConstants.EDIT_CURRENT))
             {
                 boolean bLocked = (this.getEditMode() == DBConstants.EDIT_IN_PROGRESS);
                 Object bookmark = this.getHandle(DBConstants.BOOKMARK_HANDLE);
                 if (bRefreshDataIfNoMods)
                 {
                     this.setHandle(bookmark, DBConstants.BOOKMARK_HANDLE);
                     if (bLocked)
                         this.edit();
                 }
                 return bookmark;
             }
             else
                 return null;
         }
         Object bookmark = null;
         if ((this.getEditMode() == DBConstants.EDIT_IN_PROGRESS)
             || (this.getEditMode() == DBConstants.EDIT_CURRENT))
         {
             if (this.getEditMode() == DBConstants.EDIT_CURRENT)
             {    // HACK - It should have been locked already... especially if there were any changes
                 BaseBuffer buffer = null;
                 if (this.isModified())
                 {
                     buffer = new VectorBuffer(null, BaseBuffer.ALL_FIELDS | BaseBuffer.MODIFIED_ONLY);
                     buffer.fieldsToBuffer(this);
                 }
                 this.edit();
                 if (buffer != null)
                 {
                     boolean[] rgListeners = this.setEnableListeners(false);
                     Object[] rgFieldListeners = this.setEnableFieldListeners(false);
                     buffer.bufferToFields(this, DBConstants.DONT_DISPLAY, DBConstants.READ_MOVE);
                     buffer.free();
                     this.setEnableFieldListeners(rgFieldListeners);
                     this.setEnableListeners(rgListeners);
                 }
             }
             bookmark = this.getHandle(DBConstants.BOOKMARK_HANDLE);
             int iOldOpenMode = this.getOpenMode();
             this.setOpenMode(iOldOpenMode | DBConstants.OPEN_DONT_CHANGE_CURRENT_LOCK_TYPE);    // Don't relinquish my lock
             this.set();
             this.setHandle(bookmark, DBConstants.BOOKMARK_HANDLE);
             this.setOpenMode(iOldOpenMode);
             this.edit();
         }
         else if (this.getEditMode() == DBConstants.EDIT_ADD)
         {
             this.add();
             bookmark = this.getLastModified(DBConstants.BOOKMARK_HANDLE);
             this.setHandle(bookmark, DBConstants.BOOKMARK_HANDLE);
             this.edit();
         }
         return bookmark;
     }
     /**
      * Refresh this record to the current data.
      * @param iChangeType Optional change type if a message was received saying this changed.
      * @param bWarningIfChanged If anything has changed in this record, return a RECORD_CHANGED warning.
      * @return error
      */
     public int refreshToCurrent(int iChangeType, boolean bWarningIfChanged)
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
     	try {
 	        int iHandleType = DBConstants.BOOKMARK_HANDLE;
 	
 	        if ((this.getEditMode() == Constants.EDIT_CURRENT)
 	            || (this.getEditMode() == Constants.EDIT_IN_PROGRESS))
 	        {
 	            Object bookmark = this.getHandle(iHandleType);
 	            if (bookmark != null)
 	            {
 	                Object[] rgobjEnabledFields = this.setEnableFieldListeners(false);
 	                boolean[] rgbEnabled = this.setEnableListeners(false);
 	                int iOldOpenMode = this.getOpenMode();
 	                this.setOpenMode(DBConstants.OPEN_DONT_CHANGE_CURRENT_LOCK_TYPE | (iOldOpenMode & DBConstants.LOCK_TYPE_MASK));	// Straight read - no cache (keep lock settings)
                     
                     // First step - backup the current data
                     int iFieldsTypes = BaseBuffer.ALL_FIELDS;
                     BaseBuffer buffer = new VectorBuffer(null, iFieldsTypes);
                     buffer.fieldsToBuffer(this, iFieldsTypes);
 
                     boolean rgbModified[] = null;
                     if ((iChangeType != DBConstants.AFTER_DELETE_TYPE)
 	                    && (this.isModified()))
 	                {
                         boolean bLockRecord = this.getEditMode() == DBConstants.EDIT_IN_PROGRESS;
                         // Second step, save which fields have been modified.
                         rgbModified = this.getModified();
                         // Third step - refresh the record
                         this.addNew();
                         Record newRecord = this.setHandle(bookmark, iHandleType);
                         // Forth step - Move my changed fields to the refreshed record
                         if (newRecord == null)
                             this.addNew();
                         else
                         {
                             if (bLockRecord)
                                 this.edit();    // If I haven't changed any data, just refresh it.
                             buffer.resetPosition();
                             for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
                             {
                                 Object dataScreen = buffer.getNextData();
                                 if (rgbModified[iFieldSeq])
                                 {   // If I modified it
                                     BaseField field = this.getField(iFieldSeq);
                                     if ((field.isVirtual()) && (field.isModified()))
                                         continue;   // Special case - some field change modified a virtual field, leave it alone
                                     Object dataUpdated = field.getData();
                                     if (((dataUpdated != null) && (!dataUpdated.equals(dataScreen)))
                                         || ((dataUpdated == null) && (dataScreen != dataUpdated)))
                                     {   // And it is different from the current data, merge data
                                         field.mergeData(dataScreen);    // This will set field modified to true
                                         if (field.isVirtual())
                                             field.setData(dataScreen);  // This will set just modified to false
                                         if (bWarningIfChanged)
                                             iErrorCode = DBConstants.RECORD_CHANGED;
                                     }
                                 }
                             }
                         }
 	                }
 	                else
 	                {
 	                    this.addNew();    // Clear current bookmark
 	                    if (iChangeType != DBConstants.AFTER_DELETE_TYPE)
 	                        this.setHandle(bookmark, iHandleType);
 	                }
 	                this.setEnableListeners(rgbEnabled);
 	                this.setEnableFieldListeners(rgobjEnabledFields);
                     // Fifth step: If what is currently on the screen is different from the new merged data, re-call the handleFieldChanged (SCREEN_MOVE)
 	                int iDBMasterSlave = 0;
 	                if ((this.getMasterSlave() & RecordOwner.MASTER) == 0)
 	                {
                         iDBMasterSlave = this.getTable().getCurrentTable().getDatabase().getMasterSlave();
                         this.getTable().getCurrentTable().getDatabase().setMasterSlave(RecordOwner.MASTER | RecordOwner.SLAVE);
 	                }
 	                boolean bAnyChanged = this.checkAndHandleFieldChanges(buffer, rgbModified, true);
 	                if (iDBMasterSlave != 0)
 	                	this.getTable().getCurrentTable().getDatabase().setMasterSlave(iDBMasterSlave);
                     this.setOpenMode(iOldOpenMode);     // Some field listeners need to know I'm refreshing, so I wait until checkAndHandleFieldChanges is done.
                     if (bWarningIfChanged)
                         if (bAnyChanged)
                             iErrorCode = DBConstants.RECORD_CHANGED;
 	            }
 	        }
     	} catch (DBException ex) {
     		ex.printStackTrace();
     	}
 		return iErrorCode;
     }
     /**
      * Compare the current state with the way the record was before and call any fieldchange listeners.
      * @param buffer
      * @param rgbModified
      * @return
      */
     public boolean checkAndHandleFieldChanges(BaseBuffer buffer, boolean[] rgbModified, boolean bRestoreVirtualFields)
     {
         boolean bAnyChanged = false;
         // Fifth step: If what is currently on the screen is different from the new merged data, re-call the handleFieldChanged (SCREEN_MOVE)
         buffer.resetPosition();
         for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
         {
             BaseField field = this.getField(iFieldSeq);
             Object dataScreen = buffer.getNextData();
             Object dataUpdated = field.getData();
             if (((dataUpdated != null) && (!dataUpdated.equals(dataScreen)))
                 || ((dataUpdated == null) && (dataScreen != dataUpdated))
                 || (field.isModified()))
             {   // I compare rather than check isModified due to the virtual fields
                 if ((this.getField(iFieldSeq).isVirtual())
                     && (!this.getField(iFieldSeq).isModified())
                         && (this.getField(iFieldSeq).getData() == this.getField(iFieldSeq).getDefault()))
                 {   // Virtual fields that were not changed, need to be returned to their former value
                     if (bRestoreVirtualFields)
                     {
                         boolean[] rgbEnabled = this.getField(iFieldSeq).setEnableListeners(false);
                         this.getField(iFieldSeq).setData(dataScreen);
                         this.getField(iFieldSeq).setModified(false);
                         this.getField(iFieldSeq).setEnableListeners(rgbEnabled);
                     }
                 }
                 else
                 {
                     int iMoveMode = field.isModified() ? DBConstants.SCREEN_MOVE : DBConstants.READ_MOVE;   // If I changed it, SCREEN_MOVE, if data changed, READ_MOVE
                     if (rgbModified != null)
                         if (rgbModified[iFieldSeq])
                         {
                             iMoveMode = DBConstants.SCREEN_MOVE;    // Special case if I changed it, and the someone else changed it to the same thing, hit screen_move one more time.
                             if (field.isVirtual())
                                 if (((dataUpdated != null) && (dataUpdated.equals(dataScreen)))
                                         || ((dataUpdated == null) && (dataScreen == dataUpdated)))
                                     if (!field.isJustModified())  // Extra special case - Since a changed virtual field will always be modified, see if it actually changed;
                                         iMoveMode = DBConstants.READ_MOVE;  // Extra special case - If not, see if someone else modified it
                         }
                     this.getField(iFieldSeq).handleFieldChanged(DBConstants.DISPLAY, iMoveMode);
                     bAnyChanged = true;
                 }
             }
         }
         return bAnyChanged;
     }
     /**
      * Get the referenced record's ID given the code.
      * This method converts the code to the record ID by reading the secondary key of the referenced record.
      * @param strCode The code to convert (ie., "EMAIL_TYPE")
      * @return int The ID of the referenced record (or 0 if not found).
      */
     public int getIDFromCode(String strCode)
     {
         int iID = 0;
         try   {
             iID = Integer.parseInt(strCode);    // Special case - if an integer, just convert it.
         } catch (NumberFormatException ex)  {
             iID = 0;
         }
         if (iID == 0)
         {
             int iTargetKeyArea = this.getCodeKeyArea();   // Key 0 cannot be the code key
             if (iTargetKeyArea != 0)
             {
                 KeyArea keyArea = this.getKeyArea(iTargetKeyArea);
                 int iOldKeyArea = this.getDefaultOrder();
                 this.setKeyArea(iTargetKeyArea);
                 BaseField field = keyArea.getField(0);
                 field.setString(strCode);
                 try   {
                     if (this.seek(null))
                     {
                         iID = (int)this.getField(DBConstants.MAIN_FIELD).getValue();
                     }
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 } finally {
                     this.setKeyArea(iOldKeyArea);
                 }
             }
         }
         return iID;
     }
     /**
      * Get the code key area.
      * @return The code key area (or 0 if not found).
      */
     public int getCodeKeyArea()
     {
         int iTargetKeyArea = 0;   // Key 0 cannot be the code key
         for (int i = this.getKeyAreaCount() - 1; i >= 0 ; i--)
         {
             KeyArea keyArea = this.getKeyArea(i);
             if (keyArea.getUniqueKeyCode() == DBConstants.SECONDARY_KEY)
                 iTargetKeyArea = i;
             if (iTargetKeyArea == 0)
                 if (keyArea.getUniqueKeyCode() == DBConstants.NOT_UNIQUE)
                     if (keyArea.getField(0).getDataClass() == String.class)
                         iTargetKeyArea = i; // Best guess if no secondary
         }
         return iTargetKeyArea;
     }
     /**
      * Does this fieldlist have a primary key that is auto-sequence.
      * This can be turned off (in thick) by setting table.setProperty(AUTO_SEQUENCE_KEY, FALSE),
      * which is primarily used to restore an archive with changing the autosequenced values.
      * In this is is okay to set this, although usually only set by system functions.
      * @param bIsAutoSequence True if autosequence key.
      */
     public boolean setAutoSequence(boolean bIsAutoSequence)
     {
         boolean bOldAutoSequence = super.setAutoSequence(bIsAutoSequence);
         String strMode = DBConstants.FALSE;
         if (bIsAutoSequence)
             strMode = DBConstants.TRUE;
         this.getTable().getCurrentTable().setProperty(SQLParams.AUTO_SEQUENCE_ENABLED, strMode);        // Enable/Disable autosequence
         return bOldAutoSequence;
     }
     /**
      * Utility to copy this entire table to this thin table.
      * @param fieldList
      * @return true If successful
      */
     public boolean populateThinTable(FieldList fieldList, boolean bApplyMappedFilter)
     {
         Record record = this;
         if (bApplyMappedFilter)
             record = this.applyMappedFilter();
         if (record == null)
             return false;
 
         FieldTable table = fieldList.getTable();
         boolean bAutoSequence = fieldList.isAutoSequence();
         boolean[] rgListeners = null;
         Object[] rgFieldListeners = null;
         int iOldOpenMode = fieldList.getOpenMode();
         fieldList.setOpenMode(DBConstants.OPEN_NORMAL);
         try   {
             fieldList.setAutoSequence(false);  // Disable autoseq temporarily
             if (fieldList instanceof Record)
             {
                 rgListeners = ((Record)fieldList).setEnableListeners(false);
                 rgFieldListeners = ((Record)fieldList).setEnableFieldListeners(false);
             }
             while (record.hasNext())
             {
                 record.next();
                 table.addNew();
                 if (bApplyMappedFilter)
                     this.moveDataToThin(record, fieldList);
                 else
                     this.copyAllFields(record, fieldList);
                 table.add(fieldList);
             }
         } catch (DBException ex)    {
             ex.printStackTrace();
         } finally {
             fieldList.setAutoSequence(bAutoSequence);
             if (rgListeners != null)
                 ((Record)fieldList).setEnableListeners(rgListeners);
             if (rgFieldListeners != null)
                 ((Record)fieldList).setEnableFieldListeners(rgFieldListeners);
             fieldList.setOpenMode(iOldOpenMode);
         }
         
         if (bApplyMappedFilter)
             this.freeMappedRecord(record);
         return true;
     }
     /**
      * Create the filter and apply it to this/a record and record the record to traverse.
      * @return The record to traverse (with filters applied).
      */
     public Record applyMappedFilter()
     {
         return this;    // Override this!
     }
     /**
      * Move the data in this record to the thin version.
      * This is the one to override if you want to move different fields.
      * @param fieldList
      */
     public void moveDataToThin(Record record, FieldList fieldList)
     {
         this.copyAllFields(record, fieldList);
     }
     /**
      * Copy the data in this record to the thin version.
      * @param fieldList
      */
     public final void copyAllFields(Record record, FieldList fieldList)
     {
         for (int i = 0; i < fieldList.getFieldCount(); i++)
         {
             FieldInfo fieldInfo = fieldList.getField(i);
             BaseField field = record.getField(i);
             this.moveFieldToThin(fieldInfo, field, record);
         }
     }
     /**
      * Move the data in this record to the thin version.
      * @param fieldInfo The destination thin field.
      * @param field The source field (or null to auto-find)
      * @param record The source record (or null if field supplied)
      */
     public void moveFieldToThin(FieldInfo fieldInfo, BaseField field, Record record)
     {
         if ((field == null) || (!field.getFieldName().equals(fieldInfo.getFieldName())))
         {
             field = null;
             if (record != null)
                 field = record.getField(fieldInfo.getFieldName());
         }
         if (field != null)
         {
             if (field.getDataClass() == fieldInfo.getDataClass())
                 fieldInfo.setData(field.getData());
             else
                 fieldInfo.setString(field.toString());
         }
     }
     /**
      * Free this mapped record.
      * @param record The record to free.
      */
     public void freeMappedRecord(Record record)
     {
         // Override this
     }
     /**
      * Get the starting ID for this table.
      * Override this for different behavior.
      * @return The starting id
      */
     public int getStartingID()
     {
         if (this.getTable() != null)
             if (this.getTable().getDatabase() != null)
                 return this.getTable().getDatabase().getStartingID();
         return super.getStartingID();   // Never (default)
     }
     /**
      * Get the starting ID for this table.
      * Override this for different behavior.
      * @return The starting id
      */
     public int getEndingID()
     {
         if (this.getTable() != null)
             if (this.getTable().getDatabase() != null)
                 return this.getTable().getDatabase().getEndingID();
         return super.getEndingID();   // Never (default)
     }
     /**
      * Get the archive file path for this record.
      * @param bPhysicalName Using the physical name of the database.
      * @return The archive filename.
      */
     public String getArchiveFilename(boolean bPhysicalName)
     {
         String strFileSeparator = System.getProperty("file.separator");
         char chFileSeparator = '/';
         if ((strFileSeparator != null) && (strFileSeparator.length() == 1))
             chFileSeparator = strFileSeparator.charAt(0);
         String strArchiveFolder = null;
         if (this.getTable().getDatabase() != null)
         {
         	if ((this.getDatabaseType() & DBConstants.USER_DATA) != 0)
     			strArchiveFolder = this.getTable().getDatabase().getProperty(DBConstants.USER_ARCHIVE_FOLDER);
         	else
     			strArchiveFolder = this.getTable().getDatabase().getProperty(DBConstants.SHARED_ARCHIVE_FOLDER);
     		if (strArchiveFolder == null)
     			strArchiveFolder = this.getTable().getDatabase().getProperty(DBConstants.ARCHIVE_FOLDER);
         }
         if ((strArchiveFolder == null) || (strArchiveFolder.length() == 0))
             if (this.getRecordOwner() != null)
         {
             if ((this.getDatabaseType() & DBConstants.USER_DATA) != 0)
                 strArchiveFolder = this.getRecordOwner().getProperty(DBConstants.USER_ARCHIVE_FOLDER);
             else
                 strArchiveFolder = this.getRecordOwner().getProperty(DBConstants.SHARED_ARCHIVE_FOLDER);
             if (strArchiveFolder == null)
                 strArchiveFolder = this.getRecordOwner().getProperty(DBConstants.ARCHIVE_FOLDER);
         }
         if ((strArchiveFolder == null) || (strArchiveFolder.length() == 0))
             strArchiveFolder = DBConstants.DEFAULT_ARCHIVE_FOLDER;
         String tableDomain = this.getClass().getName();
         if ((tableDomain.indexOf('.') != -1)
         	&& (tableDomain.substring(tableDomain.indexOf('.') + 1).indexOf('.') != -1))
         		tableDomain = tableDomain.substring(0, tableDomain.indexOf('.') + tableDomain.substring(tableDomain.indexOf('.') + 1).indexOf('.') + 2);	// Include trailing .
     		else
     			tableDomain = DBConstants.BLANK;
         strArchiveFolder = Utility.replace(strArchiveFolder, "{domain}", tableDomain.replace('.', chFileSeparator));
         String strPackage = this.getClass().getName();
         if (strPackage.endsWith(DatabaseInfo.DATABASE_INFO_FILE))
             strPackage = DatabaseInfo.DATABASE_INFO_FILE;
         String strDBName = this.getTable().getDatabase().getDatabaseName(bPhysicalName);
         if (bPhysicalName == false)
         {
             if ((this.getDatabaseType() & DBConstants.TABLE_DATA_TYPE_MASK) == DBConstants.USER_DATA)
                 strDBName = strDBName + BaseDatabase.USER_SUFFIX;
             else if ((this.getDatabaseType() & DBConstants.TABLE_DATA_TYPE_MASK) == DBConstants.SHARED_DATA)
                 strDBName = strDBName + BaseDatabase.SHARED_SUFFIX;            
         }
         String strFilename = Utility.addToPath(Utility.addToPath(strArchiveFolder, strDBName), strPackage.replace('.', chFileSeparator) + ".xml");
         return strFilename;
     }
     /**
      * Do a remote command.
      * @param strCommand
      * @param properties
      * @return
      */
     public Object handleRemoteCommand(String strCommand, Map<String, Object> properties)
     	throws DBException, RemoteException
     {
     	return this.handleRemoteCommand(strCommand, properties, true, true, true);
     }
     /**
      * Do a remote command.
      * This method simplifies the task of calling a remote method.
      * Instead of having to override the session, all you have to do is override
      * doRemoteCommand in your record and handleRemoteCommand will call the remote version of the record.
      * @param strCommand
      * @param properties
      * @return
      */
     public Object handleRemoteCommand(String strCommand, Map<String, Object> properties, boolean bWriteAndRefresh, boolean bDontCallIfLocal, boolean bCloneServerRecord)
     	throws DBException, RemoteException
     {
     	RemoteTable remoteTask = (RemoteTable)this.getTable().getRemoteTableType(org.jbundle.model.Remote.class);
     	if (bWriteAndRefresh)
     		this.writeAndRefresh();
     	if (remoteTask == null)
     	{
     		if (bDontCallIfLocal)
     			return Boolean.FALSE;
     		else
     			return this.doRemoteCommand(strCommand, properties);
     	}
     	if (bCloneServerRecord)
     	{
     		if (properties == null)
     			properties = new HashMap<String, Object>();
     		properties.put(MenuConstants.CLONE, Boolean.TRUE);
     		properties.put(DBParams.RECORD, this.getClass().getName());
     	}
 		return remoteTask.doRemoteAction(strCommand, properties);
     }
     /**
      * Do a remote command (Override this with your code).
      * @param strCommand The command to execute
      * @param properties The properties
      * @return Boolean.FALSE if the command was not handled, anything else if it was.
      */
     public Object doRemoteCommand(String strCommand, Map<String, Object> properties)
     {
     	return Boolean.FALSE;   // Override this to handle this command
     }
 }
