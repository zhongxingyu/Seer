  /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.style.CSSAttributeSet;
 import org.wings.style.CSSProperty;
 import org.wings.style.CSSSelector;
 import org.wings.style.CSSStyleSheet;
 import org.wings.table.SDefaultTableColumnModel;
 import org.wings.table.STableCellEditor;
 import org.wings.table.STableCellRenderer;
 import org.wings.table.STableColumn;
 import org.wings.table.STableColumnModel;
 import javax.swing.event.CellEditorListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import java.awt.*;
 import java.util.EventObject;
 import java.util.HashMap;
 
 
 /**
  * Displays information contained in a {@link TableModel} object.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class STable extends SComponent
         implements TableModelListener, Scrollable, CellEditorListener, LowLevelEventListener {
 
     /**
      * Apache jakarta commons logger
      */
     private final static Log log = LogFactory.getLog(STable.class);
 
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int NO_SELECTION = SListSelectionModel.NO_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int SINGLE_SELECTION = SListSelectionModel.SINGLE_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int SINGLE_INTERVAL_SELECTION = SListSelectionModel.SINGLE_INTERVAL_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int MULTIPLE_SELECTION = SListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int MULTIPLE_INTERVAL_SELECTION = SListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
 
 
     /**
      * <p>the table model.</p>
      */
     protected TableModel model;
 
     /**
      * <p>the selection model.</p>
      */
     protected SListSelectionModel selectionModel;
 
     /**
      * <p>The default renderer is used if no other renderer is set for the
      * content of a cell.</p>
      */
     protected STableCellRenderer defaultRenderer;
 
     /**
      * <p>The <code>headerRenderer</code> is used to render the header line.</p>
      */
     protected STableCellRenderer headerRenderer;
 
     /**
      * <p>A special cell renderer, that displays the control used to select
      * a table row.</p><p>Ususally, this would be some checkbox. The plaf is the
      * last instance to decide this.</p>
      */
     protected STableCellRenderer rowSelectionRenderer;
 
     /**
      * <p>In this <code>Map</code>, the renderers for the different
      * classes of cell content are stored.</p><p>The class is treated
      * as key, the renderer as the value.</p>
      */
     protected final HashMap renderer = new HashMap();
 
     protected boolean editable = true;
 
     /**
      * <p>If editing, this is the <code>SComponent</code> that is handling the editing.
      */
     transient protected SComponent editorComp;
 
     /**
      * <p>The object that overwrites the screen real estate occupied by the
      * current cell and allows the user to change those contents.</p>
      */
     transient protected STableCellEditor cellEditor;
 
     /**
      * <p>Identifies the column of the cell being edited.</p>
      */
     transient protected int editingColumn = -1;
 
     /**
      * <p>Identifies the row of the cell being edited.</p>
      */
     transient protected int editingRow = -1;
 
     /**
      * <p>In this <code>Map</code>, the <code>STableCellEditor</code>s for the different
      * classes of cell content are stored.</p><p>The class is treated
      * as key, the <code>STableCellEditor</code> as the value.</p>
      */
     protected final HashMap editors = new HashMap();
 
     /**
      * <p>Determines whether the header is visible or not.</p><p>By
      * default the header is visible.</p> <p><em>CAVEAT:</em>The
      * header is not (yet) implemented like in Swing. But maybe
      * someday.  So you can disable it if you like. TODO.</p>
      */
     protected boolean headerVisible = true;
 
     /**
      * <p>Determines if horizontal lines in the table should be
      * painted.</p><p>This is off by default.</p>
      */
     protected boolean showHorizontalLines = false;
 
     /**
      * <p>Determines if vertical lines in the table should be
      * painted.</p><p>This is off by default.</p>
      */
     protected boolean showVerticalLines = false;
 
     protected SDimension intercellSpacing;
 
     protected SDimension intercellPadding = new SDimension("1", "1");
 
     protected Rectangle viewport;
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     protected boolean epochCheckEnabled = true;
 
     /**
      * The column model holds state information about the columns of the table.
      */
     protected STableColumnModel columnModel;
 
     /**
      * If true, the column model is autorebuild from the table model.
      */
     private boolean autoCreateColumnsFromModel;
 
 
     /**
      * A Pseudo CSS selector addressing the header row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.CSSSelector, org.wings.style.CSSProperty, String)}
      */
     public static final CSSSelector.Pseudo SELECTOR_HEADER = new CSSSelector.Pseudo("HEADER");
 
     /**
      * A Pseudo CSS selector addressing the selected row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.CSSSelector, org.wings.style.CSSProperty, String)}
      */
     public static final CSSSelector.Pseudo SELECTOR_SELECTION = new CSSSelector.Pseudo("SELECTION");
 
     /**
      * A Pseudo CSS selector addressing the regular odd row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.CSSSelector, org.wings.style.CSSProperty, String)}
      */
     public static final CSSSelector.Pseudo SELECTOR_ODD_ROWS = new CSSSelector.Pseudo("ODD_ROWS");
 
     /**
      * A Pseudo CSS selector addressing the regular even row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.CSSSelector, org.wings.style.CSSProperty, String)}
      */
     public static final CSSSelector.Pseudo SELECTOR_EVEN_ROWS = new CSSSelector.Pseudo("EVEN_ROWS");
 
     /**
      * A Pseudo CSS selector addressing the regular even row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.CSSSelector, org.wings.style.CSSProperty, String)}
      */
     public static final CSSSelector.Pseudo SELECTOR_NUMBERING_COLUMN = new CSSSelector.Pseudo("NUMBERING_COLUMN");
 
     /**
      * <p>Creates a new <code>STable</code>.</p>
      */
     public STable() {
         this(null);
     }
 
     /**
      * <p>Creates a new <code>STable</code>.</p>
      *
      * @param tm the <code>TableModel</code> for the table's contents.
      */
     public STable(TableModel tm) {
         this(tm, null);
     }
 
     public STable(TableModel model, STableColumnModel columnModel) {
         setSelectionModel(new SDefaultListSelectionModel());
         createDefaultEditors();
 
        if (model == null)
            this.model = createDefaultDataModel();
        else
            this.model = model;

         if (columnModel == null) {
            this.columnModel = createDefaultColumnModel();
            createDefaultColumnsFromModel();
             autoCreateColumnsFromModel = true;
         }
        else
            this.columnModel = columnModel;
     }
 
     /**
      * <p>Sets the model of the table.</p>
      *
      * @param tm the <code>TableModel</code> to set.
      */
     public void setModel(TableModel tm) {
         if (tm == null)
             throw new IllegalArgumentException("Cannot set a null TableModel");
 
         if (this.model != tm) {
             if (model != null)
                 model.removeTableModelListener(this);
 
             model = tm;
             model.addTableModelListener(this);
 
             tableChanged(new TableModelEvent(tm, TableModelEvent.HEADER_ROW));
         }
     }
 
     /**
      * <p>returns the model of the table</p>
      */
     public TableModel getModel() {
         return model;
     }
 
     public int getColumnCount() {
         return model.getColumnCount();
     }
 
     public String getColumnName(int col) {
         return model.getColumnName(col);
     }
 
     public Class getColumnClass(int col) {
         return model.getColumnClass(col);
     }
 
     /**
      * Convienece method / Swing compatiblity to <code>model.getRowCount()</code>
      * @return
      */
     public int getRowCount() {
         return model.getRowCount();
     }
 
     /**
      * Define an optional CSS class which should be applied additionally to the passed row num.
      * Override this method, if you want to give rows different attributes.
      * E.g. for displaying an alternating background color for rows.
      *
      * @return the style of a specific row number.
      */
     public String getRowStyle(int row) {
         return null;
     }
 
     public Object getValueAt(int row, int column) {
         return model.getValueAt(row, column);
     }
 
     public void setValueAt(Object v, int row, int column) {
         model.setValueAt(v, row, column);
     }
 
     public int convertColumnIndexToModel(int viewColumnIndex) {
         return viewColumnIndex;
     }
 
     /**
      * Adds the row from <i>index0</i> to <i>index0</i> inclusive to the current selection.
      */
     public void addRowSelectionInterval(int index0, int index1) {
         selectionModel.addSelectionInterval(index0, index1);
     }
 
     public void setParent(SContainer p) {
         super.setParent(p);
 
         if (getCellRendererPane() != null)
             getCellRendererPane().setParent(p);
 
         if (editorComp != null)
             editorComp.setParent(p);
     }
 
     protected void setParentFrame(SFrame f) {
         super.setParentFrame(f);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParentFrame(f);
     }
 
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
 
         // delay events...
         getSelectionModel().setDelayEvents(true);
         getSelectionModel().setValueIsAdjusting(true);
 
         for (int i = 0; i < values.length; i++) {
             String value = values[i];
             if (value.length() > 1) {
 
                 char modus = value.charAt(0);
                 value = value.substring(1);
 
                 int colonIndex = value.indexOf(':');
                 if (colonIndex < 0)
                     continue; // maybe next value fits ...
 
                 try {
 
                     int row = Integer.parseInt(value.substring(0, colonIndex));
                     int col = Integer.parseInt(value.substring(colonIndex + 1));
                     // editor event
                     switch (modus) {
                         case 'e':
                             editCellAt(row, col, null);
                             break;
                         case 't':
                             if (getSelectionModel().isSelectedIndex(row))
                                 getSelectionModel().removeSelectionInterval(row, row);
                             else
                                 getSelectionModel().addSelectionInterval(row, row);
                             break;
                         case 's':
                             getSelectionModel().addSelectionInterval(row, row);
                             break;
                         case 'd':
                             getSelectionModel().removeSelectionInterval(row, row);
                             break;
                     }
                 } catch (NumberFormatException ex) {
                     log.warn("Number formatting exception due parsing of component name", ex);
                 }
             }
         }
 
         getSelectionModel().setValueIsAdjusting(false);
         getSelectionModel().setDelayEvents(false);
         SForm.addArmedComponent(this);
 
     }
 
     private SCellRendererPane cellRendererPane = new SCellRendererPane();
 
     public SCellRendererPane getCellRendererPane() {
         return cellRendererPane;
     }
 
     public void setDefaultRenderer(STableCellRenderer r) {
         defaultRenderer = r;
     }
 
     public STableCellRenderer getDefaultRenderer() {
         return defaultRenderer;
     }
 
     public void setDefaultRenderer(Class columnClass, STableCellRenderer r) {
         renderer.remove(columnClass);
         if (renderer != null)
             renderer.put(columnClass, r);
     }
 
     public STableCellRenderer getDefaultRenderer(Class columnClass) {
         if (columnClass == null) {
             return defaultRenderer;
         } else {
             Object r = renderer.get(columnClass);
             if (r != null) {
                 return (STableCellRenderer) r;
             } else {
                 return getDefaultRenderer(columnClass.getSuperclass());
             }
         }
     }
 
     /**
      * The renderer component responsible for rendering the table's header cell.
      * @param headerCellRenderer
      */
     public void setHeaderRenderer(STableCellRenderer headerCellRenderer) {
         headerRenderer = headerCellRenderer;
     }
 
     /**
      * The renderer component responsible for rendering the table's header cell.
      * @return The renderer component for the header row
      */
     public STableCellRenderer getHeaderRenderer() {
         return headerRenderer;
     }
 
     /**
      * The cell renderer used to render a special selection column needed in cases clicks on table
      * cell cannot be distinguished as 'edit' or 'selection' click.
      * @return The table cell renderer used to render the selection column
      */
     public STableCellRenderer getRowSelectionRenderer() {
         return rowSelectionRenderer;
     }
 
     /**
      * The cell renderer used to render a special selection column needed in cases clicks on table
      * cell cannot be distinguished as 'edit' or 'selection' click.
      * @param rowSelectionRenderer The table cell renderer used to render the selection column
      */
     public void setRowSelectionRenderer(STableCellRenderer rowSelectionRenderer) {
         this.rowSelectionRenderer = rowSelectionRenderer;
     }
 
     /**
      * Returns the cell renderer for the given table cell.
      * @param row Table row
      * @param column Table column
      * @return The cell renderer for the given table cell.
      */
     public STableCellRenderer getCellRenderer( int row, int column ) {
         if ( getColumnModel() == null || getColumnModel().getColumn(column) == null)
             return getDefaultRenderer(getColumnClass(column));
 
         STableCellRenderer renderer = getColumnModel().getColumn( column ).getCellRenderer();
         if ( renderer == null )
             renderer = getDefaultRenderer( getColumnClass( getColumnModel().getColumn( column ).getModelIndex() ) );
 
         return renderer;
     }
 
     public SComponent prepareRenderer(STableCellRenderer r, int row, int col) {
         return r.getTableCellRendererComponent(this,
                 model.getValueAt(row, col),
                 isRowSelected(row),
                 row, col);
     }
 
     /**
      * Prepares and returns the renderer to render the column header
      * @param col Column number to render. Starts with <code>0</code>. May be <code>-1</code> for row selection column.
      * @return The renderer to render the column header
      */
     public SComponent prepareHeaderRenderer( int col ) {
         Object headerValue = col >= 0 ? model.getColumnName(col) : null;
         if ( getColumnModel() != null && getColumnModel().getColumn( col ) != null)
             headerValue = getColumnModel().getColumn( col ).getHeaderValue();
         return headerRenderer.getTableCellRendererComponent( this, headerValue, false, -1, col );
     }
 
     public boolean isEditable() {
         return editable;
     }
 
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
 
     /**
      * Set a default editor to be used if no editor has been set in
      * a TableColumn. If no editing is required in a table, or a
      * particular column in a table, use the isCellEditable()
      * method in the TableModel interface to ensure that the
      * STable will not start an editor in these columns.
      * If editor is null, remove the default editor for this
      * column class.
      *
      * @see TableModel#isCellEditable
      * @see #getDefaultEditor
      * @see #setDefaultRenderer
      */
     public void setDefaultEditor(Class columnClass, STableCellEditor r) {
         editors.remove(columnClass);
         if (editors != null)
             editors.put(columnClass, r);
     }
 
     /*
      * Returns the editor to be used when no editor has been set in
      * a TableColumn. During the editing of cells the editor is fetched from
      * a Map of entries according to the class of the cells in the column. If
      * there is no entry for this <I>columnClass</I> the method returns
      * the entry for the most specific superclass. The STable installs entries
      * for <I>Object</I>, <I>Number</I> and <I>Boolean</I> all which can be modified
      * or replaced.
      *
      * @see     #setDefaultEditor
      * @see     #getColumnClass
      */
     public STableCellEditor getDefaultEditor(Class columnClass) {
         if (columnClass == null) {
             return null;
         } else {
             Object r = editors.get(columnClass);
             if (r != null) {
                 return (STableCellEditor) r;
             } else {
                 return getDefaultEditor(columnClass.getSuperclass());
             }
         }
     }
 
     //
     // Editing Support
     //
 
     /**
      * Programmatically starts editing the cell at <I>row</I> and
      * <I>column</I>, if the cell is editable.
      *
      * @param row    the row to be edited
      * @param column the column to be edited
      * @return false if for any reason the cell cannot be edited.
      * @throws IllegalArgumentException If <I>row</I> or <I>column</I>
      *                                  are not in the valid range
      */
     public boolean editCellAt(int row, int column) {
         return editCellAt(row, column, null);
     }
 
     /**
      * Programmatically starts editing the cell at <I>row</I> and
      * <I>column</I>, if the cell is editable.
      * To prevent the STable from editing a particular table, column or
      * cell value, return false from the isCellEditable() method in the
      * TableModel interface.
      *
      * @param row    the row to be edited
      * @param column the column to be edited
      * @param e      event to pass into
      *               shouldSelectCell
      * @return false if for any reason the cell cannot be edited.
      * @throws IllegalArgumentException If <I>row</I> or <I>column</I>
      *                                  are not in the valid range
      */
     public boolean editCellAt(int row, int column, EventObject e) {
         if (isEditing()) {
             // Try to stop the current editor
             if (cellEditor != null) {
                 boolean stopped = cellEditor.stopCellEditing();
                 if (!stopped)
                     return false;       // The current editor not resigning
             }
         }
 
         if (!isCellEditable(row, column))
             return false;
 
         STableCellEditor editor = getCellEditor(row, column);
         if (editor != null) {
             // set up editor environment and make it possible for the editor, to
             // stop/cancel editing on preparation
             editor.addCellEditorListener(this);
             setCellEditor(editor);
             setEditingRow(row);
             setEditingColumn(column);
 
             // prepare editor
             editorComp = prepareEditor(editor, row, column);
 
             if (editor.isCellEditable(e) && editor.shouldSelectCell(e)) {
                 return true;
             } else {
                 setValueAt(editor.getCellEditorValue(), row, column);
                 removeEditor();
             } // end of else
 
         }
         return false;
     }
 
     /**
      * Returns true if the cell at <I>row</I> and <I>column</I>
      * is editable.  Otherwise, setValueAt() on the cell will not change
      * the value of that cell.
      *
      * @param row the row whose value is to be looked up
      * @param col the column whose value is to be looked up
      * @return true if the cell is editable.
      * @see #setValueAt
      */
     public boolean isCellEditable(int row, int col) {
         if (col >= getColumnCount() || row == -1)
             return false;
         else
             return getModel().isCellEditable(row, col);
     }
 
     /**
      * Returns  true is the table is editing a cell.
      *
      * @return true is the table is editing a cell
      * @see #editingColumn
      * @see #editingRow
      */
     public boolean isEditing() {
         return (cellEditor == null) ? false : true;
     }
 
     /**
      * If the receiver is currently editing this will return the Component
      * that was returned from the CellEditor.
      *
      * @return SComponent handling editing session
      */
     public SComponent getEditorComponent() {
         return editorComp;
     }
 
     /**
      * This returns the index of the editing column.
      *
      * @return the index of the column being edited
      * @see #editingRow
      */
     public int getEditingColumn() {
         return editingColumn;
     }
 
     /**
      * Returns the index of the editing row.
      *
      * @return the index of the row being edited
      * @see #editingColumn
      */
     public int getEditingRow() {
         return editingRow;
     }
 
     /**
      * Return the cellEditor.
      *
      * @return the STableCellEditor that does the editing
      * @see #cellEditor
      */
     public STableCellEditor getCellEditor() {
         return cellEditor;
     }
 
     /**
      * Set the cellEditor variable.
      *
      * @param anEditor the STableCellEditor that does the editing
      * @see #cellEditor
      */
     protected void setCellEditor(STableCellEditor anEditor) {
         cellEditor = anEditor;
     }
 
     /**
      * Set the editingColumn variable.
      *
      * @see #editingColumn
      */
     public void setEditingColumn(int aColumn) {
         int oldEditingColumn = editingColumn;
         editingColumn = aColumn;
         if (editingColumn != oldEditingColumn)
             reload(ReloadManager.STATE);
     }
 
     /**
      * Set the editingRow variable.
      *
      * @see #editingRow
      */
     public void setEditingRow(int aRow) {
         int oldEditingRow = editingRow;
         editingRow = aRow;
         if (editingRow != oldEditingRow)
             reload(ReloadManager.STATE);
     }
 
     /**
      * Return an appropriate editor for the cell specified by this row and
      * column. If the TableColumn for this column has a non-null editor, return that.
      * If not, find the class of the data in this column (using getColumnClass())
      * and return the default editor for this type of data.
      *
      * @param row    the row of the cell to edit, where 0 is the first
      * @param column the column of the cell to edit, where 0 is the first
      */
     public STableCellEditor getCellEditor( int row, int column ) {
         if ( getColumnModel() == null )
             return getDefaultEditor(getColumnClass(column));
 
         STableColumn tableColumn = getColumnModel().getColumn( column );
         STableCellEditor editor = tableColumn.getCellEditor();
         if ( editor == null )
             editor = getDefaultEditor( getColumnClass( tableColumn.getModelIndex() ) );
         return editor;
     }
 
     /**
      * Prepares the specified editor using the value at the specified cell.
      *
      * @param editor the TableCellEditor to set up
      * @param row    the row of the cell to edit, where 0 is the first
      * @param col    the column of the cell to edit, where 0 is the first
      */
     protected SComponent prepareEditor(STableCellEditor editor, int row, int col) {
         return editor.getTableCellEditorComponent(this,
                 model.getValueAt(row, col),
                 isRowSelected(row), // true?
                 row, col);
     }
 
     /**
      * Discard the editor object and return the real estate it used to
      * cell rendering.
      */
     public void removeEditor() {
         STableCellEditor editor = getCellEditor();
         if (editor != null) {
             editor.removeCellEditorListener(this);
             //remove(editorComp);
             setCellEditor(null);
             setEditingColumn(-1);
             setEditingRow(-1);
             if (editorComp != null) {
                 editorComp.setParent(null);
             } // end of if ()
             editorComp = null;
         }
     }
 
 
     //
     // Implementing the CellEditorListener interface
     //
 
     /**
      * Invoked when editing is finished. The changes are saved and the
      * editor object is discarded.
      *
      * @see CellEditorListener
      */
     public void editingStopped(ChangeEvent e) {
         // Take in the new value
         STableCellEditor editor = getCellEditor();
         if (editor != null) {
             Object value = editor.getCellEditorValue();
             setValueAt(value, editingRow, editingColumn);
             removeEditor();
             reload(ReloadManager.STATE);
         }
     }
 
     /**
      * Invoked when editing is canceled. The editor object is discarded
      * and the cell is rendered once again.
      *
      * @see CellEditorListener
      */
     public void editingCanceled(ChangeEvent e) {
         removeEditor();
         reload(ReloadManager.STATE);
     }
 
     /**
      * Creates default cell editors for Objects, numbers, and boolean values.
      */
     protected void createDefaultEditors() {
         editors.clear();
 
         // Objects
         STextField textField = new STextField();
         setDefaultEditor(Object.class, new SDefaultCellEditor(textField));
         setDefaultEditor(Number.class, new SDefaultCellEditor(textField));
 
         // Numbers
         //STextField rightAlignedTextField = new STextField();
         //rightAlignedTextField.setHorizontalAlignment(STextField.RIGHT);
         //setDefaultEditor(Number.class, new SDefaultCellEditor(rightAlignedTextField));
 
         // Booleans
         SCheckBox centeredCheckBox = new SCheckBox();
         //centeredCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
         setDefaultEditor(Boolean.class, new SDefaultCellEditor(centeredCheckBox));
     }
 
 
     public SListSelectionModel getSelectionModel() {
         return selectionModel;
     }
 
     /**
      * Sets the row selection model for this table to <code>model</code>.
      *
      * @param model the new selection model
      * @throws IllegalArgumentException if <code>model</code>
      *                                  is <code>null</code>
      * @see #getSelectionModel
      */
     public void setSelectionModel(SListSelectionModel model) {
         if (getSelectionModel() != null) {
             removeSelectionListener(reloadOnSelectionChangeListener);
         }
 
         if (model == null) {
             throw new IllegalArgumentException("cannot set a null SListSelectionModel");
         }
         selectionModel = model;
 
         addSelectionListener(reloadOnSelectionChangeListener);
     }
 
 
     public int getSelectedRowCount() {
         int result = 0;
         for (int i = getSelectionModel().getMinSelectionIndex();
              i <= getSelectionModel().getMaxSelectionIndex(); i++) {
             if (getSelectionModel().isSelectedIndex(i))
                 result++;
         }
 
         return result;
     }
 
 
     public int getSelectedRow() {
         return getSelectionModel().getMinSelectionIndex();
     }
 
     public int[] getSelectedRows() {
         int[] result = new int[getSelectedRowCount()];
 
         int index = 0;
         for (int i = getSelectionModel().getMinSelectionIndex();
              i <= getSelectionModel().getMaxSelectionIndex(); i++) {
             if (getSelectionModel().isSelectedIndex(i))
                 result[index++] = i;
         }
 
         return result;
     }
 
     /**
      * Deselects all selected columns and rows.
      */
     public void clearSelection() {
         if (!getSelectionModel().isSelectionEmpty()) {
             getSelectionModel().clearSelection();
             reload(ReloadManager.STATE);
         }
     }
 
 
     public boolean isRowSelected(int row) {
         return getSelectionModel().isSelectedIndex(row);
     }
 
     /**
      * Sets the selection mode. Use one of the following values:
      * <UL>
      * <LI> {@link #NO_SELECTION}
      * <LI> {@link javax.swing.ListSelectionModel#SINGLE_SELECTION} or
      * {@link #SINGLE_SELECTION}
      * <LI> {@link javax.swing.ListSelectionModel#SINGLE_INTERVAL_SELECTION} or
      * {@link #SINGLE_INTERVAL_SELECTION}
      * <LI> {@link javax.swing.ListSelectionModel#MULTIPLE_INTERVAL_SELECTION} or
      * {@link #MULTIPLE_SELECTION}
      * </UL>
      */
     public void setSelectionMode(int s) {
         getSelectionModel().setSelectionMode(s);
     }
 
     /**
      * @return <UL>
      *         <LI> {@link #NO_SELECTION}
      *         <LI> {@link javax.swing.ListSelectionModel#SINGLE_SELECTION} or
      *         {@link #SINGLE_SELECTION}
      *         <LI> {@link javax.swing.ListSelectionModel#SINGLE_INTERVAL_SELECTION} or
      *         {@link #SINGLE_INTERVAL_SELECTION}
      *         <LI> {@link javax.swing.ListSelectionModel#MULTIPLE_INTERVAL_SELECTION} or
      *         {@link #MULTIPLE_SELECTION}
      *         </UL>
      */
     public int getSelectionMode() {
         return getSelectionModel().getSelectionMode();
     }
 
 
     public void addSelectionListener(ListSelectionListener listener) {
         getSelectionModel().addListSelectionListener(listener);
     }
 
 
     public void removeSelectionListener(ListSelectionListener listener) {
         getSelectionModel().removeListSelectionListener(listener);
     }
 
     public void fireIntermediateEvents() {
         getSelectionModel().fireDelayedIntermediateEvents();
     }
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
         // fire selection events...
         getSelectionModel().fireDelayedFinalEvents();
     }
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     public boolean isEpochCheckEnabled() {
         return epochCheckEnabled;
     }
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     public void setEpochCheckEnabled(boolean epochCheckEnabled) {
         this.epochCheckEnabled = epochCheckEnabled;
     }
 
     public void tableChanged(TableModelEvent e) {
         // kill active editors
         editingCanceled(null);
 
         if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
             // The whole thing changed
             clearSelection();
             if (getAutoCreateColumnsFromModel())
                 createDefaultColumnsFromModel();
         } else {
             switch (e.getType()) {
                 case TableModelEvent.INSERT:
                     if (e.getFirstRow() >= 0)
                         getSelectionModel().insertIndexInterval(e.getFirstRow(),
                                 e.getLastRow(), true);
                     break;
 
                 case TableModelEvent.DELETE:
                     if (e.getFirstRow() >= 0)
                         getSelectionModel().removeIndexInterval(e.getFirstRow(),
                                 e.getLastRow());
                     break;
             }
         }
         reload(ReloadManager.STATE);
     }
 
     /**
      * Return the background color.
      *
      * @return the background color
      */
     public Color getSelectionBackground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTION) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTION));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setSelectionBackground(Color color) {
         setAttribute(SELECTOR_SELECTION, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the foreground color.
      *
      * @return the foreground color
      */
     public Color getSelectionForeground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTION) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTION));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setSelectionForeground(Color color) {
         setAttribute(SELECTOR_SELECTION, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     public void setSelectionFont(SFont font) {
         setAttributes(SELECTOR_SELECTION, CSSStyleSheet.getAttributes(font));
     }
 
     /**
      * Return the font.
      *
      * @return the font
      */
     public SFont getSelectionFont() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTION) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTION));
     }
 
     /**
      * Return the background color.
      *
      * @return the background color
      */
     public Color getHeaderBackground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_HEADER) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(SELECTOR_HEADER));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setHeaderBackground(Color color) {
         setAttribute(SELECTOR_HEADER, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the foreground color.
      *
      * @return the foreground color
      */
     public Color getHeaderForeground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_HEADER) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(SELECTOR_HEADER));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setHeaderForeground(Color color) {
         setAttribute(SELECTOR_HEADER, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     public void setHeaderFont(SFont font) {
         setAttributes(SELECTOR_HEADER, CSSStyleSheet.getAttributes(font));
     }
 
     /**
      * Return the font.
      *
      * @return the font
      */
     public SFont getHeaderFont() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_HEADER) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(SELECTOR_HEADER));
     }
 
     public void setHeaderVisible(boolean hv) {
         boolean oldHeaderVisible = headerVisible;
         headerVisible = hv;
         if (oldHeaderVisible != headerVisible)
             reload(ReloadManager.STATE);
     }
 
     public boolean isHeaderVisible() {
         return headerVisible;
     }
 
     public void setShowGrid(boolean b) {
         setShowHorizontalLines(b);
         setShowVerticalLines(b);
     }
 
     public void setShowHorizontalLines(boolean b) {
         boolean oldShowHorizontalLines = showHorizontalLines;
         showHorizontalLines = b;
         if (showHorizontalLines != oldShowHorizontalLines)
             reload(ReloadManager.STATE);
     }
 
     public boolean getShowHorizontalLines() {
         return showHorizontalLines;
     }
 
     public void setShowVerticalLines(boolean b) {
         boolean oldShowVerticalLines = showVerticalLines;
         showVerticalLines = b;
         if (showVerticalLines != oldShowVerticalLines)
             reload(ReloadManager.STATE);
     }
 
     public boolean getShowVerticalLines() {
         return showVerticalLines;
     }
 
     /*
      * Implementiert das cellspacing Attribut des HTML Tables. Da dieses
      * nur eindimensional ist, wird nur der width Wert der Dimension in
      * den HTML Code uebernommen.
      */
     public void setIntercellSpacing(SDimension d) {
         SDimension oldIntercellSpacing = intercellSpacing;
         intercellSpacing = d;
         if ((intercellSpacing == null && oldIntercellSpacing != null) ||
                 intercellSpacing != null && !intercellSpacing.equals(oldIntercellSpacing))
             reload(ReloadManager.STATE);
     }
 
     public SDimension getIntercellSpacing() {
         return intercellSpacing;
     }
 
     /*
      * Implementiert das cellpadding Attribut des HTML Tables. Da dieses
      * nur eindimensional ist, wird nur der width Wert der Dimension in
      * den HTML Code uebernommen.
      */
 
     public void setIntercellPadding(SDimension d) {
         SDimension oldIntercellPadding = intercellPadding;
         intercellPadding = d;
         if ((intercellPadding == null && oldIntercellPadding != null) ||
                 intercellPadding != null && !intercellPadding.equals(oldIntercellPadding))
             reload(ReloadManager.STATE);
     }
 
     public SDimension getIntercellPadding() {
         return intercellPadding;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getEditParameter(int row, int col) {
         return "e" + row + ":" + col;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getToggleSelectionParameter(int row, int col) {
         return "t" + row + ":" + col;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getSelectionParameter(int row, int col) {
         return "s" + row + ":" + col;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getDeselectionParameter(int row, int col) {
         return "d" + row + ":" + col;
     }
 
     /**
      * Returns the maximum size of this table.
      *
      * @return maximum size
      */
     public Rectangle getScrollableViewportSize() {
         return new Rectangle(0, 0, getColumnCount(), getRowCount());
     }
 
     /*
      * Setzt den anzuzeigenden Teil
      */
     public void setViewportSize(Rectangle d) {
         if (isDifferent(viewport, d)) {
             viewport = d;
             reload(ReloadManager.STATE);
         }
     }
 
     public Rectangle getViewportSize() {
         return viewport;
     }
 
     public Dimension getPreferredExtent() {
         return null;
     }
 
     /**
      * if selection changes, we have to reload code...
      */
     protected final ListSelectionListener reloadOnSelectionChangeListener =
             new ListSelectionListener() {
                 public void valueChanged(ListSelectionEvent e) {
                     reload(ReloadManager.STATE);
                 }
             };
 
     public void setSelectedRow(int selectedIndex) {
         getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
     }
 
 
     /**
      * Sets this table's <code>autoCreateColumnsFromModel</code> flag.
      * This method calls <code>createDefaultColumnsFromModel</code> if
      * <code>autoCreateColumnsFromModel</code> changes from false to true.
      *
      * @param   autoCreateColumnsFromModel   true if <code>JTable</code> should automatically create columns
      * @see     #getAutoCreateColumnsFromModel
      * @see     #createDefaultColumnsFromModel
      */
     public void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel) {
         if (this.autoCreateColumnsFromModel != autoCreateColumnsFromModel) {
             this.autoCreateColumnsFromModel = autoCreateColumnsFromModel;
             if (autoCreateColumnsFromModel) {
                 createDefaultColumnsFromModel();
             }
         }
     }
 
     /**
      * Determines whether the table will create default columns from the model.
      * If true, <code>setModel</code> will clear any existing columns and
      * create new columns from the new model.  Also, if the event in
      * the <code>tableChanged</code> notification specifies that the
      * entire table changed, then the columns will be rebuilt.
      * The default is true.
      *
      * @return  the autoCreateColumnsFromModel of the table
      * @see     #setAutoCreateColumnsFromModel
      * @see     #createDefaultColumnsFromModel
      */
     public boolean getAutoCreateColumnsFromModel() {
         return autoCreateColumnsFromModel;
     }
 
     /**
      * Returns a <code>STableColumnModel</code> that contains information
      * about all columns  of this table.
      *
      * @return  the object that provides the column state of the table
      * @see     #setColumnModel
      */
     public STableColumnModel getColumnModel() {
         return columnModel;
     }
 
 
     /**
      * Sets the model holding information about the columns for this table.
      *
      * @param   newColumnModel        the new data source for this table
      * @see     #getColumnModel
      */
     public void setColumnModel(STableColumnModel newColumnModel) {
         if (newColumnModel == null)
             throw new IllegalArgumentException("Column model must not be null");
 
         if (columnModel != newColumnModel) {
             this.columnModel = newColumnModel;
             reload(ReloadManager.STATE);
         }
     }
 
     /**
      * Creates the default columns of the table from the table model.
      */
     public void createDefaultColumnsFromModel() {
         TableModel tm = getModel();
 
         if (tm != null) {
             STableColumnModel columnModel = getColumnModel();
             while (columnModel.getColumnCount() > 0)
                 columnModel.removeColumn(columnModel.getColumn(0));
 
             for ( int i = 0; i < tm.getColumnCount(); i++ ) {
                 STableColumn column = new STableColumn( i );
                 String columnName = tm.getColumnName( i );
                 column.setHeaderValue( columnName );
                 this.columnModel.addColumn( column );
             }
         }
     }
 
     /**
      * Returns the default column model object, which is
      * a <code>SDefaultTableColumnModel</code>.
      * A subclass can override this method to return a different column model object.
      *
      * @return the default column model object
      */
     protected STableColumnModel createDefaultColumnModel() {
         return new SDefaultTableColumnModel();
     }
 
     /**
      * Returns a default table model object.
      * Subclasses can override this method to return a different table model objects
      *
      * @return the default table model object
      * @see javax.swing.table.DefaultTableModel
      */
     protected TableModel createDefaultDataModel() {
         return new DefaultTableModel();
     }
 
 }
