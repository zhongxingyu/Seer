 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.screen.view.swing.grid;
 
 import java.io.FileFilter;
 
 import org.jbundle.base.db.DatabaseException;
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.event.FileListener;
 import org.jbundle.base.db.event.FreeOnFreeHandler;
 import org.jbundle.base.field.ShortField;
 import org.jbundle.base.field.event.SortOrderHandler;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.screen.model.BaseScreen;
 import org.jbundle.base.screen.model.GridScreen;
 import org.jbundle.base.screen.model.SButtonBox;
 import org.jbundle.base.screen.model.ScreenField;
 import org.jbundle.base.screen.model.ToolScreen;
 import org.jbundle.base.screen.view.swing.VScreenField;
 import org.jbundle.model.DBException;
 import org.jbundle.model.db.Convert;
import org.jbundle.thin.base.db.Converter;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.screen.grid.ThinTableModel;
 
 
 /**
  * GridTableModel - Supplies data for the grid screen.
  *  NOTE - This is also used for Popups.
  */
 public class GridTableModel extends ThinTableModel
 {
     private static final long serialVersionUID = 1L;
 
     /**
      * The screen that is displaying this model.
      */
     protected BaseScreen m_gridScreen = null;
     /**
      * The table event handler.
      */
     protected TableModelHandler m_tableModelBehavior = null;
     /**
      * Is this the first open?
      */
     protected boolean m_bFirstOpen = true;
 
     /**
      * Constructor.
      */
     public GridTableModel()
     {
         super();
     }
     /**
      * Constructor.
      * @param gridScreen The screen that is dipslaying this model.
      */
     public GridTableModel(BaseScreen gridScreen)
     {
         this();
         this.init(gridScreen);
     }
     /**
      * Constructor.
      * @param gridScreen The screen that is displaying this model.
      */
     public void init(BaseScreen gridScreen)
     {
         super.init(gridScreen.getMainRecord().getTable());
         m_gridScreen = gridScreen;
         m_gridScreen.getMainRecord().addListener(m_tableModelBehavior = new TableModelHandler(this)); // Add this listener
         m_bFirstOpen = true;
         if (m_gridScreen.getMainRecord().isOpen())
             m_bFirstOpen = false;   // Open already
     }
     /**
      * Free the resources.
      */
     public void free()
     {
         if (m_tableModelBehavior != null)
             m_gridScreen.getMainRecord().removeListener(m_tableModelBehavior, true);        // Remove and free the table model listener
         m_tableModelBehavior = null;
         super.free();
         m_gridScreen = null;
     }
     /**
      * Get this column's class.
      * Get it from the base field.
      * @param iColumnIndex The column to return.
      * @return The class of this column.
      */
     public Class<?> getColumnClass(int iColumnIndex)
     {
         return m_gridScreen.getSField(iColumnIndex).getScreenFieldView().getStateClass();
     }
     /**
      * Set the value at the field at the column.
      * Since this is only used by the restore current record method,
      * pass dont_display and read_move when you set the data.
      * This is NOT a TableModel override, this is my method.
      * @param iColumnIndex The column to set.
      * @param value The value to set at this location.
      * @return True if the value at this cell changed.
      */
     public boolean setColumnValue(int iColumnIndex, Object value, boolean bDisplay, int iMoveMode)
     {
         Object dataBefore = m_gridScreen.getSField(iColumnIndex).getScreenFieldView().getFieldState();
         m_gridScreen.getSField(iColumnIndex).getScreenFieldView().setFieldState(value, bDisplay, iMoveMode);
         Object dataAfter = m_gridScreen.getSField(iColumnIndex).getScreenFieldView().getFieldState();
         if (dataBefore == null)
             return (dataAfter != null);
         else
             return (!dataBefore.equals(dataAfter));
     }
     /**
      * Get the value of the field at the column.
      * This is NOT a TableModel override, this is my method.
      * @param iColumnIndex The column to set.
      * @param iEditMode The edit mode.
      * @return The value at this location.
      */
     public Object getColumnValue(int iColumnIndex, int iEditMode)
     {
         if (iEditMode == DBConstants.EDIT_NONE)
             if (m_gridScreen.getSField(iColumnIndex) instanceof SButtonBox)
                 return null;    // Signal the grid to disable this control.
         return m_gridScreen.getSField(iColumnIndex).getScreenFieldView().getFieldState();
     }
     /**
      * Get the number of columns in the physical table.
      * @return The column count.
      */
     public int getColumnCount()
     {
         return m_gridScreen.getSFieldCount();
     }
     /**
      * Returns the field at columnIndex.
      * @param iColumnIndex The column to set.
      * @return The converter at this location.
      */
     public Convert getFieldInfo(int iColumnIndex)
     {
         return m_gridScreen.getSField(iColumnIndex).getConverter();
     }
     /**
      * Is this cell editable?
      * @param iRowIndex The row to set.
      * @param iColumnIndex The column to set.
      * @return True if this cell is editable.
      */
     public boolean isCellEditable(int iRowIndex, int iColumnIndex)
     {
         if (iColumnIndex < m_gridScreen.getNavCount())
             return true;    // Must return true for form button to work
         boolean bIsEditable = super.isCellEditable(iRowIndex, iColumnIndex);
         if (!bIsEditable)
             return bIsEditable;     // Deleted record
         return ((VScreenField)((ScreenField)m_gridScreen.getSField(iColumnIndex)).getScreenFieldView()).isCellEditable(null);    // From Editor interface
     }
     /**
      * Add one extra blank record for appending?
      * @return true if appending is on.
      */
     public boolean isAppending()
     {
         if (m_gridScreen != null)
             return m_gridScreen.getAppending();
         return super.isAppending();
     }
     /**
      * User selected a new row.
      * From the ListSelectionListener interface.
      * @param source The source data.
      * @param iStartRow The start row.
      * @param iEndRow The end row.
      * @param iSelectType The select type.
      * @return true if the selection changed?
      */
     public boolean selectionChanged(Object source, int iStartRow, int iEndRow, int iSelectType)
     {
         int iSelectedRow = this.getSelectedRow();
         boolean bChanged = super.selectionChanged(source, iStartRow, iEndRow, iSelectType);
         if (iSelectedRow != this.getSelectedRow())
             if (m_gridScreen != null)   // Could have been freed.
         { // Selection changed
             int iErrorCode = DBConstants.NORMAL_RETURN;
             FieldList fieldList = null;
             Record record = m_gridScreen.getMainRecord();
             iSelectedRow = this.getSelectedRow();
             if (iSelectedRow != -1)
                 fieldList = this.makeRowCurrent(iSelectedRow, false);
             if (fieldList == null)
                 iSelectedRow = -1;      // No record = de-select
             if (iSelectedRow == -1)
                 iErrorCode = record.handleRecordChange(DBConstants.DESELECT_TYPE);  // Record DeSelected!!!
             else
                 iErrorCode = record.handleRecordChange(DBConstants.SELECT_TYPE);    // Record selected!!!
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 ;   // DO Something?
         }
         return bChanged;
     }
     /**
      * Get the array of changed values for the current row.
      * I use a standard field buffer and append all the screen items which
      * are not included in the field.
      * @param iRowIndex The row to get the data for.
      * @return The array of data for the currently locked row.
      */
     public BaseBuffer cacheCurrentLockedData(int iRowIndex, FieldList fieldList)
     {
         BaseBuffer buffer = super.cacheCurrentLockedData(iRowIndex, fieldList);
         m_brgCurrentLockedDataMods = ((Record)fieldList).getModified();
         return buffer;
     }
     /**
      * Restore this fieldlist with items from the input cache.
      * I use a standard field buffer and append all the screen items which
      * are not included in the field.
      * @param fieldList The record to fill with the data.
      */
     public void restoreCurrentRecord(FieldList fieldList)
     {
         super.restoreCurrentRecord(fieldList);
         ((Record)fieldList).setModified(m_brgCurrentLockedDataMods);
     }
     /**
      * Data for the currently locked record (Each array location is a column).
      */
     protected boolean[] m_brgCurrentLockedDataMods = null;
     /**
      * Update the currently updated record if the row is different from this row.
      * @param iRowIndex Row to read... If different from current record, update current. If -1, update and don't read.
      */
     public void updateIfNewRow(int iRowIndex)
         throws DBException
     {
         try {
             super.updateIfNewRow(iRowIndex);
         } catch (DBException ex)    {
             org.jbundle.model.Task task = m_gridScreen.getTask();
             if (task != null)
             {
             	String strError = ex.getMessage();
             	if ((strError == null) || (strError.length() == 0))
             		if (ex instanceof DatabaseException)
             			strError = ((DatabaseException)ex).getMessage(task);
                 task.setLastError(strError);
                 task.setStatusText(strError, DBConstants.WARNING);
             }
             // Don't throw the error (I set the message already).
         }
     }
     /**
      * The underlying query changed, reset the model.
      */
     public void resetTheModel()
     {
         super.resetTheModel();
         if (!m_bFirstOpen)
             this.fireTableDataChanged();        // I'm different now, repaint me
         m_bFirstOpen = false;
     }
     /**
      * Is the main record modified?
      * @return True if the record has changed.
      */
     public boolean isRecordChanged()
     {
         Record record = m_gridScreen.getMainRecord();
         return record.getTable().getCurrentTable().getRecord().isModified();  // Is this record modified?
     }
     /**
      * Sort this table by this column.
      * @param iColumn The column to sort by.
      * @param If true, sort ascending.
      * @return true if successful.
      */
     public boolean sortByColumn(int iColumn, boolean bOrder)
     {
         if (m_gridScreen != null)
         {
             if (m_gridScreen.getConverter() == null)
             {   // If there is no sorter, add a default one
                 Record record = m_gridScreen.getMainRecord();
                 if (record != null)
                 {
                     boolean bAddDefaultSorter = true;
                     FileListener listener = record.getListener();
                     while (listener != null)
                     {
                         if (listener instanceof FileFilter)
                             bAddDefaultSorter = false;      // Too difficult to figure out sort order
                         listener = (FileListener)listener.getNextListener();
                     }
                     if (bAddDefaultSorter)
                         if (m_gridScreen instanceof GridScreen) // Always
                     {
                         ShortField tempField = new ShortField(null, "SortOrder", DBConstants.DEFAULT_FIELD_LENGTH, "Sort Order", null);
                         SortOrderHandler sortListener = new SortOrderHandler((GridScreen)m_gridScreen);
                         if ((record.getDatabaseType() & DBConstants.TABLE_TYPE_MASK) == DBConstants.TABLE)
                             sortListener.setCreateSortOrder(true);  // On simple tables, allow user to re-sort on any key
                         tempField.addListener(sortListener);
                         record.addListener(new FreeOnFreeHandler(tempField));
                     }
                 }
             }
             if (m_gridScreen.getConverter() != null)
             {
                 Convert converter = m_gridScreen.getConverter();
                 if (iColumn > 0)
                 {
                     if (!bOrder)
                         iColumn = -iColumn;     // Negative means descending
                     return (converter.setValue(iColumn) == DBConstants.NORMAL_RETURN);
                 }
             }
         }
         return super.sortByColumn(iColumn, bOrder);
     }
     /**
      * Convert the table column to the 1 based field column
      * @param iViewColumn
      * @return The field column.
      */
     public int columnToFieldColumn(int iViewColumn)
     {
         Convert lastConverter = null;
         int iFieldColumn = 1;
         for (int iColumnIndex = 0; iColumnIndex < iViewColumn; iColumnIndex++)
         {
             Convert converter = this.getFieldInfo(iColumnIndex);
             if ((converter == null) || (converter == lastConverter))
                 continue;
             iFieldColumn++;
             lastConverter = converter;
         }
         return iFieldColumn;
     }
     /**
      * Convert the 1 based field column to the table column
      * @param iFieldColumn
      * @return The field column.
      */
     public int columnToViewColumn(int iFieldColumn)
     {   // Note: this is overidden in GridTable Model
         Convert lastConverter = null;
         int iActualColumn = 0;
         int iToolscreens = 0;
         for (int iColumnIndex = 0; iColumnIndex < this.getColumnCount(); iColumnIndex++)
         {
             if (m_gridScreen.getSField(iColumnIndex) instanceof ToolScreen)
                 iToolscreens++;
             Convert converter = this.getFieldInfo(iColumnIndex);
             if ((converter == null) || (converter == lastConverter))
                 continue;
             iActualColumn++;
             if (iActualColumn >= iFieldColumn)
                 return iColumnIndex - iToolscreens;
             lastConverter = converter;
         }
         return iFieldColumn - iToolscreens;    //Never
     }
 }
