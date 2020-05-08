 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-04-21
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.gui;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.table.AbstractTableModel;
 
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class replaces FilePanelDataModel. The data for each row is now
  * contained in an instance of FileData (or one of its subclasses, like
  * {@link wjhk.jupload2.filedata.PictureFileData}). This allow easy add of new
  * functionalites, during upload, by adding attributes or methods to these
  * classes, or create new ones. <BR>
  * Some ides of improvements :
  * <UL>
  * <LI> Compression of picture before Upload (see
  * {@link wjhk.jupload2.filedata.PictureFileData})
  * <LI> Could be XML validation before sending to the server
  * <LI> Up to your imagination...
  * </UL>
  */
 class FilePanelDataModel2 extends AbstractTableModel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1473262424494858913L;
 
     /**
      * The default colum indices of the columns, as displayed by the applet.
      * Index of the column "Filename"
      */
     public static final int COLINDEX_NAME = 0;
 
     /**
      * The default colum indices of the columns, as displayed by the applet.
      * Index of the column "Filename"
      */
     public static final int COLINDEX_SIZE = 1;
 
     /**
      * The default colum indices of the columns, as displayed by the applet.
      * Index of the column "Filesize"
      */
     public static final int COLINDEX_DIRECTORY = 2;
 
     /**
      * The default colum indices of the columns, as displayed by the applet.
      * Index of the column "Last modified"
      */
     public static final int COLINDEX_MODIFIED = 3;
 
     /**
      * The default colum indices of the columns, as displayed by the applet.
      * Index of the column "Readable"
      */
     public static final int COLINDEX_READABLE = 4;
 
     /**
      * The uploadPolicy contains all current parameter, including the
      * FileDataParam
      */
     private UploadPolicy uploadPolicy = null;
 
     /**
      * The column names, as displayed on the applet. They are not real final
      * values, as they are translated: we need to have an uploadPolicy for this
      * translation, and the uploadPolicy is 'given' to the constructor.
      */
     private String COL_NAME = null;
 
     private String COL_SIZE = null;
 
     private String COL_DIRECTORY = null;
 
     private String COL_MODIFIED = null;
 
     private String COL_READABLE = null;
 
     protected String[] columnNames = null;
 
     protected int[] columnSize = null;
 
     protected Class[] columnClasses = null;
 
     /**
      * This Vector contains all FileData.
      */
     private Vector<FileData> rows = new Vector<FileData>();
 
     /**
      * 
      */
     public FilePanelDataModel2(UploadPolicy uploadPolicy) {
         // Property initialization is done ... for each property. Nothing to do
         // here.
         super();
         //
         this.uploadPolicy = uploadPolicy;
 
         // Initialization for column name, type and size.
         this.COL_NAME = uploadPolicy.getString("colName");
         this.COL_SIZE = uploadPolicy.getString("colSize");
         this.COL_DIRECTORY = uploadPolicy.getString("colDirectory");
         this.COL_MODIFIED = uploadPolicy.getString("colModified");
         this.COL_READABLE = uploadPolicy.getString("colReadable");
 
         this.columnNames = new String[] {
                 this.COL_NAME, this.COL_SIZE, this.COL_DIRECTORY,
                 this.COL_MODIFIED, this.COL_READABLE
         };
 
         this.columnSize = new int[] {
                 150, 75, 199, 130, 75
         };
 
         this.columnClasses = new Class[] {
                 String.class, Long.class, String.class, Date.class,
                 Boolean.class
         };
     }
 
     /**
      * Does this table contain this file ?
      * 
      * @param file : the file that could be contained...
      * @return true if the table contains this file.
      */
     public boolean contains(File file) {
         Iterator<FileData> i = this.rows.iterator();
         while (i.hasNext()) {
             if (file.equals(i.next().getFile())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Add a file to the panel (at the end of the list)
      * 
      * @param file
      * @param root
      */
     public void addFile(File file, File root) {
         if (contains(file)) {
             this.uploadPolicy.displayWarn("File " + file.getName()
                     + " already exists");
         } else {
             // We first call the upload policy, to get :
             // - The correct fileData instance (for instance the
             // PictureUploadPolicy returns a PictureFileData)
             // - The reference to this newly FileData, or null if an error
             // occurs (for instance: invalid file content,
             // according to the current upload policy).
             FileData df = this.uploadPolicy.createFileData(file, root);
             if (df != null) {
                 // The file is Ok, let's add it.
                 this.rows.add(df);
                 fireTableDataChanged();
             }
         }
     }
 
     /**
      * Ask for the file contained at specified row number.
      * 
      * @param row The row number
      * @return The return instance of File.
      */
     public File getFileAt(int row) {
         return this.rows.get(row).getFile();
     }
 
     /**
      * Ask for the file contained at specified row number.
      * 
      * @param row The row number
      * @return The return instance of File.
      */
     public FileData getFileDataAt(int row) {
         try {
             return this.rows.get(row);
         } catch (ArrayIndexOutOfBoundsException e) {
             // Nothing to do
         }
         return null;
     }
 
     /**
      * Remove a specified row.
      * 
      * @param row The row to remove.
      */
     public void removeRow(int row) {
         this.rows.remove(row);
         fireTableDataChanged();
     }
 
     /**
      * Removes fileData from the current list. There should be only one.
      * 
      * @param fileData
      */
     public void removeRow(FileData fileData) {
         Iterator<FileData> i = this.rows.iterator();
         FileData item;
         while (i.hasNext()) {
             item = i.next();
             if (item.getFile().equals(fileData.getFile())) {
                 this.rows.removeElement(item);
                 fireTableDataChanged();
                 break;
             }
         }
     }
 
     /** @see javax.swing.table.TableModel#getColumnCount() */
     public int getColumnCount() {
         return this.columnNames.length;
     }
 
     /** @see javax.swing.table.TableModel#getRowCount() */
     public int getRowCount() {
         return this.rows.size();
     }
 
     /**
      * Always return false here : no editable cell.
      * 
      * @see javax.swing.table.TableModel#isCellEditable(int, int)
      */
     @Override
     public boolean isCellEditable(@SuppressWarnings("unused")
     int arg0, @SuppressWarnings("unused")
     int arg1) {
         // No editable columns.
         return false;
     }
 
     /**
      * Sort the rows, according to one column.
      * 
      * @param col The index of the column to sort
      * @param ascending true if ascending, false if descending.
      */
     @SuppressWarnings("unchecked")
     public void sortColumn(int col, boolean ascending) {
         Collections.sort(this.rows, new ColumnComparator(col, ascending));
         fireTableDataChanged();
     }
 
     /**
      * Return true if this column can be sorted.
      * 
      * @param col The index of the column which can sortable or not.
      * @return true if the column can be sorted. false otherwise.
      */
     public boolean isSortable(int col) {
         return (Boolean.class != getColumnClass(col));
     }
 
     /**
      * @see javax.swing.table.TableModel#getColumnClass(int)
      */
     @SuppressWarnings("unchecked")
     @Override
     public Class getColumnClass(int arg0) {
         return this.columnClasses[arg0];
     }
 
     /**
      * @see javax.swing.table.TableModel#getValueAt(int, int)
      */
     public Object getValueAt(int row, int col) {
         FileData fileData = getFileDataAt(row);
         if (null == fileData) {
            this.uploadPolicy.displayWarn("Row index out of bounds "
                     + this.getClass().getName() + ": " + row);
             return null;
         }
         String colName = getColumnName(col);
         // Don't know if it will be useful, but the switch below allows the
         // column
         // to be in any order.
         if (colName.equals(this.COL_NAME)) {
             return fileData.getFileName();
         } else if (colName.equals(this.COL_SIZE)) {
             return new Long(fileData.getFileLength());
         } else if (colName.equals(this.COL_DIRECTORY)) {
             return fileData.getDirectory();
         } else if (colName.equals(this.COL_MODIFIED)) {
             return fileData.getLastModified();
         } else if (colName.equals(this.COL_READABLE)) {
             return new Boolean(fileData.canRead());
         } else {
             this.uploadPolicy.displayErr("Unknown column in "
                     + this.getClass().getName() + ": " + colName);
             return null;
         }
     }
 
     /**
      * This method doesn't do anything : no changeable values.
      * 
      * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
      */
     @Override
     public void setValueAt(@SuppressWarnings("unused")
     Object arg0, @SuppressWarnings("unused")
     int arg1, @SuppressWarnings("unused")
     int arg2) {
         this.uploadPolicy.displayWarn(this.getClass().getName()
                 + ".setValueAt: no action");
     }
 
     /**
      * @see javax.swing.table.TableModel#getColumnName(int)
      */
     @Override
     public String getColumnName(int arg0) {
         return this.columnNames[arg0];
     }
 
     /**
      * Retrieves the default colum size of a column.
      * 
      * @param col The index of the column to query.
      * @return the default size of the requested column.
      */
     public int getColumnSize(int col) {
         return this.columnSize[col];
     }
 }
