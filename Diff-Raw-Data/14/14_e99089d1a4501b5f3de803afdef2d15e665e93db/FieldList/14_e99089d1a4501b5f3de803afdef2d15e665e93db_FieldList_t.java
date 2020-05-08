 package org.jbundle.thin.base.db;
 
 /**
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import org.jbundle.model.DBException;
 import org.jbundle.model.RecordOwnerParent;
 import org.jbundle.model.Task;
 import org.jbundle.model.db.Field;
 import org.jbundle.model.db.Key;
 import org.jbundle.model.db.Rec;
 import org.jbundle.model.db.Table;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.buff.VectorBuffer;
import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 
 /**
  * FieldList - Collection of field information.
  * This is the thin version of a record.
  */
 public class FieldList extends Object
     implements Rec, Serializable
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
      * The starting field number -1.
      */
     public static final int kRecordLastField = -1;
     /**
      * This is the "Record" that contains all the Fields in this class.
      */
     protected Vector<Field> m_vFieldInfo = null;   // List of individual fields
     /**
      * Key Area to use on next operation.
      * Note: -1 means use the remote object's default order.
      */
     protected int m_iDefaultOrder = -1;
     /**
      * This is the "Table" that contains all the key areas in this class.
      */
     protected Vector<Key> m_vKeyAreaList = null;
     /**
      * The table for this record.
      */
     protected transient FieldTable m_table = null;
     /**
      * Current open mode (readonly/append/etc).
      */
     private int m_dbOpenMode = Constants.OPEN_NORMAL;
     /**
      * Used by Record to ascertain status.
      */
     protected int m_dbEditMode = Constants.EDIT_NONE;
     /**
      * True, the primary key field is autosequence.
      */
     protected boolean m_bIsAutoSequence = true;
     /**
      * The recordowner that owns this record.
      */
     protected Object m_recordOwner = null;
 
     /**
      * Constructor.
      */
     public FieldList()
     {
         super();
     }
     /**
      * Constructor.
      */
     public FieldList(Object recordOwner)
     {
         this();
         this.init(recordOwner);
     }
     /**
      * init.
      */
     public void init(Object recordOwner)
     {
         m_recordOwner = recordOwner;
         if (m_dbOpenMode == Constants.OPEN_NORMAL)  // In case it was changed in contructor() and init was called after
             m_dbOpenMode = Constants.OPEN_NORMAL;
         if (this.getTask() != null)
             m_dbOpenMode = m_dbOpenMode | this.getTask().getDefaultLockType(this.getDatabaseType());
         m_vFieldInfo = new Vector<Field>();
         m_vKeyAreaList = new Vector<Key>();
 
         this.setupFields();         // Add this FieldList's fields
         this.setupKeys();       // Set up the key areas
         m_dbEditMode = Constants.EDIT_NONE;
         m_bIsAutoSequence = true;
     }
     /**
      * Free.
      */
     public void free()
     {
         if (propertyChange != null)
         {
             PropertyChangeListener[] propertyChangeListeners = propertyChange.getPropertyChangeListeners();
             for (PropertyChangeListener propertyChangeListener : propertyChangeListeners)
             {
                 propertyChange.removePropertyChangeListener(propertyChangeListener);
             }
         }
         for (int i = m_vKeyAreaList.size() - 1 ; i >= 0 ;i--)
         {
             KeyAreaInfo keyArea = (KeyAreaInfo)m_vKeyAreaList.elementAt(i);
             keyArea.free();
         } 
         m_vKeyAreaList.removeAllElements();
         m_vKeyAreaList = null;
 
         for (int i = m_vFieldInfo.size() - 1 ; i >= 0 ;i--)
         {
             FieldInfo field = (FieldInfo)m_vFieldInfo.elementAt(i);
             field.free();
         } 
         m_vFieldInfo.removeAllElements();
         m_vFieldInfo = null;
 
         if (m_table != null)
         {
             m_table.setRecord(null);    // Don't try to free me
             m_table.free();
             m_table = null;
         }
     }
     /**
      * Make an exact copy of this FieldList.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone() throws CloneNotSupportedException
     {
         FieldList fieldList = null;
         try {
             Class<?> c = this.getClass();
             fieldList = (FieldList)c.newInstance();
             fieldList.init(null);   // This is strange, but otherwise this doesn't call right
         }
         catch (Exception e) {
             fieldList = null;
         }
         return fieldList;
     }
     /**
      * Get the "record" owner.
      */
     public Object getOwner()
     {
         return m_recordOwner;
     }
     /**
      * Get the "record" owner.
      */
     public void setOwner(Object recordOwner)
     {
         m_recordOwner = recordOwner;
     }
     /**
      * The table name (rarely used.. only in MAPPED tables).
      */
     protected String m_tableName = null;
     /**
      * Get the name of this table.
      * Override this to supply the name of the table.
      * Note: This is almost always overidden (except for mapped files)
      * @param bAddQuotes if the table name contains spaces, add quotes.
      * @return The name of this table.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return m_tableName;
     }
     /**
      * Set the table name.
      */
     public void setTableNames(String tableName)
     {
         m_tableName = tableName;
     }
     /**
      * Get the remote class name.
      * Just remove thin from this class name!.
      * @return The full remote class name.
      */
     public String getRemoteClassName()
     {
         String strClassName = this.getClass().getName().toString();
         int iThinPos = strClassName.indexOf(Constants.THIN_SUBPACKAGE);
         return strClassName.substring(0, iThinPos) + strClassName.substring(iThinPos + Constants.THIN_SUBPACKAGE.length());
     }
     /**
      *  Get the Database Name.
      * @return The database name. This is supplied in the overidden class.
      */
     public /*?abstract*/ String getDatabaseName()
     {
         return null;    // Override this!
     }
     /**
      *  Is this a local (vs remote) file?.
      * @return The database type.
      * LOCAL - Read-only local (or copy of remote) table.
      * REMOTE - Remote table.
      * SCREEN - Screen table.
      * VECTOR - Vector database where same records share one table
      * UNSHAREABLE_VECTOR - Vector database where all tables are new
      */
     public /**?abstract*/ int getDatabaseType()
     {
         return 0;   // Override this!
     }
     /**
      * Get the table for this record.
      * @return The table for this record.
      */
     public FieldTable getTable()
     {
         return m_table;
     }
     /**
      * Set the table for this record.
      * @param table The table for this record.
      */
     public void setTable(Table table)
     {
         m_table = (FieldTable)table;
     }
     /**
      * Set up all the fields for this record.
      * Override this to add the fields.
      */
     public void setupFields()
     {
     }
     /**
      * Set up all the key areas for this record.
      * Override this to add the key areas.
      */
     public void setupKeys()
     {
     }
     /**
      * Add this field to this record.
      * @param field A field to add to the end of the current fieldlist.
      */
     public void addField(Field field)
     {
         m_vFieldInfo.addElement(field);
     }
     /**
      * Number of Fields in this record.
      * @return The current field count.
      */
     public int size()
     {
         return this.getFieldCount();
     }
     /**
      * Get rid of this method.
      * @return The current field count.
      */
     public int getFieldCount()
     {
         if (m_vFieldInfo != null)
             return m_vFieldInfo.size();
         return 0;
     }
     /**
      * Get this field in the record.
      * @param iFieldSeq The field position to retrieve.
      * @return The field at this position (Or null if past end of records).
      */
     public FieldInfo getField(int iFieldSeq)
     {
         iFieldSeq -= Constants.MAIN_FIELD; // Zero based index
         try   {
             return (FieldInfo)m_vFieldInfo.elementAt(iFieldSeq);
         } catch (ArrayIndexOutOfBoundsException ex)   {
         }
         return null;    // Not found
     }
     /**
      * Get this field in the record.
      * @param strFieldName The field name to find.
      * @return The field at this position (Or null if past end of records).
      */
     public FieldInfo getField(String strFieldName)    // Lookup this field
     {
         boolean bAddQuotes = false;
         if (strFieldName != null) if (strFieldName.length() > 0) if (strFieldName.charAt(0) == '\"')
             bAddQuotes = true;
         for (int i = 0; i < this.getFieldCount(); i++)
         {
             FieldInfo field = this.getField(i);
             if (field.getFieldName(bAddQuotes, false).toString().equalsIgnoreCase(strFieldName))        // Don't add quotes on compare
                 return field;
         }
         return null;        // Not found
     }
     /**
      * Remove this field from this record.
      * @param field Field to remove.
      * @return true if removed.
      */
     public boolean removeField(Field field)
     {
         if (m_vFieldInfo == null)
             return false;
         return m_vFieldInfo.removeElement(field);
     }
     /**
      * Convert this record to a string.
      * @return A string of all the fields.
      */
     public String toString()
     {
         String string = Constants.BLANK;
         for (int iFieldSeq = Constants.MAIN_FIELD; iFieldSeq <= this.getFieldCount() + Constants.MAIN_FIELD - 1; iFieldSeq++) {
             FieldInfo field = this.getField(iFieldSeq);
             string += field.toString() + '\n';
         }
         string += '\n';
         return string;
     }
     /**
      * Reset all the fields to their default value.
      * <p />NOTE: After executing, this records edit mode is set to NONE.
      * @param bDisplay The the new values (on work in the thick model).
      * @throws Exception An exception on initing a field.
      */
     public void initRecord(boolean bDisplay) throws DBException
     {
         int iFieldCount = this.getFieldCount();      // Number of fields to read in
         for (int iFieldSeq = Constants.MAIN_FIELD; iFieldSeq < iFieldCount + Constants.MAIN_FIELD; iFieldSeq++)
         {
             FieldInfo field = this.getField(iFieldSeq);
             field.initField(bDisplay);
         }
         this.setEditMode(Constants.EDIT_NONE);
     }
     /**
      * Called each time a field changed.
      * This method replaces behaviors in the thick model. Just override this method and look for the
      * target field and do the listener associated with a change of value.
      * Note: The thin version (this one) keeps a flag for changes, the thick version surveys the fields.
      * @param field The field that changed.
      * @param iChangeType The type of change (See record for the list).
      * @param iDisplayOtion The display option.
      * @return The error code.
      */
     public int doRecordChange(Field field, int iChangeType, boolean bDisplayOption)
     {
         if (field != null)
             if (iChangeType == Constants.SCREEN_MOVE)
                 this.firePropertyChange(field.getFieldName(), null, field.getData());
         return Constants.NORMAL_RETURN;
     }
     /**
      * The addPropertyChangeListener method was generated to support the propertyChange field.
      * @param listener The propery change listener to add to my listeners.
      */
     public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener)
     {
         if (propertyChange == null)
             propertyChange = new java.beans.PropertyChangeSupport(this);
         propertyChange.addPropertyChangeListener(listener);
     }
     /**
      * The firePropertyChange method was generated to support the propertyChange field.
      * @param propertyName The property name.
      * @param oldValue The old value.
      * @param newValue The new value.
      */
     public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
     {
         if (propertyChange != null)
             propertyChange.firePropertyChange(propertyName, oldValue, newValue);
     }
     /**
      * The removePropertyChangeListener method was generated to support the propertyChange field.
      * @param listener The propery change listener to remove from my listeners.
      */
     public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener)
     {
         if (propertyChange != null)
             propertyChange.removePropertyChangeListener(listener);
     }
     /**
      * Propery change support.
      */
     protected transient java.beans.PropertyChangeSupport propertyChange = null;
     /**
      * Have any fields Changed?
      * Note: The thin version (this one) keeps a flag for changes, the thick version surveys the fields.
      * @return true if any fields have changed.
      */
     public boolean isModified()
     {
         for (int iFieldSeq = 0; iFieldSeq < m_vFieldInfo.size(); iFieldSeq++)
         {
             if (this.getField(iFieldSeq).isModified())
                 return true;
         }
         return false;
     }
     /**
      * Get the current edit mode for this record.
      * @return The edit mode.
      * <pre>
      * EDIT_NONE - Unknown
      * EDIT_ADD - New (unwritten) record.
      * EDIT_IN_PROGRESS - Current record in Edit mode (locked).
      * EDIT_CURRENT - Current record (read from table).
      * </pre>
      */
     public int getEditMode()
     {
         return (m_dbEditMode & Constants.EDIT_MASK);
     }
     /**
      * Set the current edit mode for this record.
      * @param dbEditMode The edit mode.
      * @return Old edit mode.
      * @see getEditMode.
      */
     public int setEditMode(int dbEditMode)
     {
         int oldEditMode = m_dbEditMode;
         m_dbEditMode = dbEditMode;
         return oldEditMode;
     }
     /**
      * Set the current open mode for this record.
      * Set this before you call Open.
      * @param dbOpenMode The current open mode.
      * @return The edit mode.
      * @see getOpenMode
      */
     public int setOpenMode(int dbOpenMode)
     {
         int oldOpenMode = m_dbOpenMode;
         m_dbOpenMode = dbOpenMode;
         return oldOpenMode;
     }
     /**
      * Get the current open mode for this record.
      * @return The current open mode.
      * <pre>
      * dbOpenNone - Lock on edit, allow deletes and changes.
      * dbOpenReadOnly - No deletes and changes.
      * dbOpenAppendOnly - No deletes and changes, adds only.
      * dbRefreshAndLockOnChange - If the record is new, refreshes the fields before the first field change. (For counter fields).
      * </pre>
      */
     public int getOpenMode()
     {
         return m_dbOpenMode;
     }
     /**
      * The resource bundle that contains the values for this record's fields.
      */
     protected ResourceBundle[] m_menuResourceBundle = null;
     /**
      * Get the string that matches this key.
      * This method traverses the class hierarchy for a matching string that matches this key value.
      * If the resource bundle hasn't been read yet, reads the bundle from the .res package.
      * @param strResource The key.
      * @return The value (returns the key if no value is found).
      */
     public String getString(String strResource)
     {
         String strResult = null;
         if (m_menuResourceBundle == null)
         {
             m_menuResourceBundle = new ResourceBundle[10];
             Class<?> classResource = this.getClass();
            Locale locale = Locale.getDefault();
             for (int i = 0; i < 10; i++)
             {
                 m_menuResourceBundle[i] = null;
                 if (classResource != null)
                 {   // First time only
                    String resourceClassName = classResource.getName();
                    resourceClassName = Converter.convertClassName(resourceClassName, Constants.RES_SUBPACKAGE) + "Resources";
                     try   {
                    	ClassLoader classLoader = this.getClass().getClassLoader();
                        m_menuResourceBundle[i] = ClassServiceUtility.getClassService().getResourceBundle(resourceClassName, locale, classLoader);
                        		//xResourceBundle.getBundle(strResourceClassName, currentLocale);
                     } catch (MissingResourceException ex) {
                         m_menuResourceBundle[i] = null;
                     }
                     classResource = classResource.getSuperclass();
                     if (classResource != null)
                         if ((classResource.getName().equals(Constants.ROOT_PACKAGE + "base.db.Record"))
                             || (classResource.getName().equals(FieldList.class.getName())))
                                 classResource = null; // Stop at record class
                 }
             }
 
         }
         for (int i = 0; i < 10; i++)
         {
             if (m_menuResourceBundle[i] != null)
             {
                 strResult = null;
                 try   {
                     strResult = m_menuResourceBundle[i].getString(strResource);
                 } catch (MissingResourceException ex) {
                     strResult = null;
                 }
                 if ((strResult != null) && (strResult.length() > 0))
                     return strResult; // Found, return value
             }
         }
         return strResource;   // Not found, return original key.
     }
     /**
      * Get the task for this field list.
      * This is a convience method which calls this.getRecordOwner().getTask() in the thick model.
      * Note: This is always the shared instance of BaseApplet for the thin model.
      * @return The task which contains this fieldList's recordowner.
      */
     public Task getTask()
     {
     	if (this.getOwner() instanceof RecordOwnerParent)
     		return ((RecordOwnerParent)this.getOwner()).getTask();  // In the thin model, your task is always the applet
     	return null;
     }
     /**
      * Get the name of the current key.
      * Note: This is a convience method, the current KeyArea's name is returned.
      * @return The key's name.
      */
     public String getKeyName()
     {
         if (this.getKeyArea(-1) != null)
             return this.getKeyArea(-1).getKeyName();
         return null;
     }
     /**
      * Get the name of the current key.
      * Note: This is a convience method, this is the same as setKeyArea (with no return).
      * @param strKeyName The key's name.
      */
     public void setKeyName(String strKeyName)
     {
         this.setKeyArea(strKeyName);
     }
     /**
      * Set the default key order.
      * @param String strKeyName the current index.
      * @return The new default key area (null if not found).
      */
     public KeyAreaInfo setKeyArea(String strKeyName)
     {
         KeyAreaInfo keyArea = null;
         for (m_iDefaultOrder = Constants.MAIN_KEY_AREA; m_iDefaultOrder < this.getKeyAreaCount() - Constants.MAIN_KEY_AREA; m_iDefaultOrder++)
         {
             keyArea = this.getKeyArea(m_iDefaultOrder);
             if (keyArea.getKeyName().equals(strKeyName))
                 return keyArea;     // Found key area
         }
         if (Constants.PRIMARY_KEY.equals(strKeyName))
         {
             m_iDefaultOrder = Constants.MAIN_KEY_AREA;  // Set to default.
             return this.getKeyArea(m_iDefaultOrder);
         }
         m_iDefaultOrder = Constants.MAIN_KEY_AREA;  // Not found!!! Set to default.
         return null;
     }
     /**
      * Add this key area to this record.
      * @param keyArea KeyArea to add.
      */
     public void addKeyArea(Key keyArea)
     {
         m_vKeyAreaList.addElement(keyArea);
     }
     /**
      * Remove this key area from this record.
      * @param keyArea KeyArea to remove.
      * @return true if successful.
      */
     public boolean removeKeyArea(Key keyArea)
     {
         return m_vKeyAreaList.removeElement(keyArea);
     }
     /**
      * Get the key order.
      * @return The current Key order.
      */
     public int getDefaultOrder()
     {
         return m_iDefaultOrder;
     }
     /**
      * Get this key area.
      * @param iKeySeq The key area to set.
      * @return The key area.
      */
     public KeyAreaInfo getKeyArea(int iKeySeq)
     {
         if (iKeySeq == -1)
             iKeySeq = this.getDefaultOrder();
         else
             iKeySeq -= Constants.MAIN_KEY_AREA;
         try   {
             if (m_vKeyAreaList != null)
                 if ((iKeySeq >= 0) && (iKeySeq < m_vKeyAreaList.size()))
                     return (KeyAreaInfo)m_vKeyAreaList.elementAt(iKeySeq);
         } catch (java.lang.ArrayIndexOutOfBoundsException e)    {
         }
         return null;    // Not found
     }
     /**
      * The number of key areas.
      * @return Key area count.
      */
     public int getKeyAreaCount()
     {
         return m_vKeyAreaList.size();
     } // Number of Key Areas for this file
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
      * Does this fieldlist have a primary key that is auto-sequence.
      * This can be turned off (in thick) by setting table.setProperty(AUTO_SEQUENCE_KEY, FALSE),
      * which is primarily used to restore an archive with changing the autosequenced values.
      * @return True if autosequence key.
      */
     public boolean isAutoSequence()
     {
         return m_bIsAutoSequence;
     }
     /**
      * Does this fieldlist have a primary key that is auto-sequence.
      * This can be turned off (in thick) by setting table.setProperty(AUTO_SEQUENCE_KEY, FALSE),
      * which is primarily used to restore an archive with changing the autosequenced values.
      * In this is is okay to set this, although usually only set by system functions.
      * @param bIsAutoSequence True if autosequence key.
      * @return Old autosequence value.
      */
     public boolean setAutoSequence(boolean bIsAutoSequence)
     {
         boolean bOldAutoSequence = m_bIsAutoSequence;
         m_bIsAutoSequence = bIsAutoSequence;
         return bOldAutoSequence;
     }
     /**
      * Get the autosequence field if it exists.
      * @return The counterfield or null.
      */
     public FieldInfo getCounterField()
     {
         return this.getField(Constants.MAIN_FIELD);
     }
     /**
      * Refresh this record to the current data.
      * @param iChangeType Optional change type if a message was received saying this changed.
      * @param bWarningIfChanged If anything has changed in this record, return a RECORD_CHANGED warning.
      * @return error
      */
     public int refreshToCurrent(int iChangeType, boolean bWarningIfChanged)
     {
         try {
             if ((this.getEditMode() == Constants.EDIT_CURRENT)
                 || (this.getEditMode() == Constants.EDIT_IN_PROGRESS))
             {
                 Object bookmark = this.getCounterField().getData();
                 if (bookmark != null)
                 {
                     int iOldOpenMode = this.getOpenMode();
                     this.setOpenMode(Constants.OPEN_REFRESH_TO_CURRENT | (iOldOpenMode/* & Constants.LOCK_TYPE_MASK*/)); // This is only trigger server listeners for changes
                     
                     // First step - backup the current data
                     int iFieldsTypes = BaseBuffer.ALL_FIELDS;
                     BaseBuffer buffer = new VectorBuffer(null, iFieldsTypes);
                     buffer.fieldsToBuffer(this, iFieldsTypes);
 
                     boolean rgbModified[] = null;
                     if ((iChangeType != Constants.AFTER_DELETE_TYPE)
                         && (this.isModified()))
                     {
                         boolean bLockRecord = this.getEditMode() == Constants.EDIT_IN_PROGRESS;
                         // Second step, save which fields have been modified.
                         rgbModified = new boolean[this.getFieldCount()];
                         for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
                         {
                             rgbModified[iFieldSeq] = this.getField(iFieldSeq).isModified();
                         }
                         // Third step - refresh the record
                         String strOldKey = this.getKeyName();
                         this.setKeyName(Constants.PRIMARY_KEY);
                         this.getCounterField().setData(bookmark);
                         boolean bSuccess = this.getTable().seek(null);
                         this.setKeyName(strOldKey);
                         // Forth step - Move the changed fields to the refreshed record
                         if (!bSuccess)
                             this.getTable().addNew();
                         else
                         {
                             if (bLockRecord)
                                 this.getTable().edit();    // If I haven't changed any data, just refresh it.
                             buffer.resetPosition();
                             for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
                             {
                                 Object dataScreen = buffer.getNextData();
                                 if (rgbModified[iFieldSeq])
                                 {
                                     FieldInfo field = this.getField(iFieldSeq);
                                     Object dataUpdated = field.getData();
                                     if (((dataUpdated != null) && (!dataUpdated.equals(dataScreen)))
                                         || ((dataUpdated == null) && (dataScreen != dataUpdated)))
                                             field.setData(dataScreen);
                                 }
                             }
                         }
                     }
                     else
                     {
                         this.getTable().addNew();    // Clear current bookmark
                         if (iChangeType != Constants.AFTER_DELETE_TYPE)
                         {
                             String strOldKey = this.getKeyName();
                             this.setKeyName(Constants.PRIMARY_KEY);
                             this.getCounterField().setData(bookmark);
                             this.getTable().seek(null);
                             this.setKeyName(strOldKey);
                         }
                     }
                     this.setOpenMode(iOldOpenMode);
                     buffer.resetPosition();
                     for (int iFieldSeq = 0; iFieldSeq < this.getFieldCount(); iFieldSeq++)
                     {
                         FieldInfo field = this.getField(iFieldSeq);
                         boolean bIsModified = false;
                         if (rgbModified != null)
                             if (rgbModified[iFieldSeq] == false)
                                 bIsModified = field.isModified();   // If it wasn't modified until now, flag it.
 
                         Object dataScreen = buffer.getNextData();
                         Object dataUpdated = field.getData();
                         if (((dataUpdated != null) && (!dataUpdated.equals(dataScreen)))
                             || ((dataUpdated == null) && (dataScreen != dataUpdated)))
                                 bIsModified = true;     // Changed somewhere else, better call fieldChanged just in case I read secondary or something.
                         if (bIsModified)
                         {
                             if ((this.getField(iFieldSeq).isVirtual())
                                 && (!this.getField(iFieldSeq).isModified())
                                     && (this.getField(iFieldSeq).getData() == this.getField(iFieldSeq).getDefault()))
                             {   // Virtual fields that were not changed, need to be returned to their former value
                                 this.getField(iFieldSeq).setData(dataScreen);
                                 this.getField(iFieldSeq).setModified(false);
                             }
 //                            else
 //                                this.getFieldInfo(iFieldSeq).handleFieldChanged(true, Constants.SCREEN_MOVE); // Typical
                         }
                     }
                 }
             }
         } catch (DBException ex) {
             ex.printStackTrace();
         }
         return Constants.NORMAL_RETURN;
     }
     /**
      * Get the starting ID for this table.
      * Override this for different behavior.
      * @return The starting id
      */
     public int getStartingID()
     {
         return 1;   // default
     }
     /**
      * Get the ending ID for this table.
      * Override this for different behavior.
      * @return The starting id
      */
     public int getEndingID()
     {
         return Integer.MAX_VALUE;   // default
     }
 }
