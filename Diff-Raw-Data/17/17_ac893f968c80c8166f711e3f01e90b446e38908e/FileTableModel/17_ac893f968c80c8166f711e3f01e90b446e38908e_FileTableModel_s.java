 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of the wingS demo (http://wings.mercatis.de).
  *
  * The wingS demo is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package explorer;
 
 import java.io.*;
 import java.util.*;
 import java.text.*;
 
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 /**
  * TODO: documentation
  *
  * @author Rene Thol
  * @version $Revision$
  */
 public class FileTableModel
     extends AbstractTableModel
 {
     private static FilenameFilter DEFAULT_FILENAMEFILTER =
         new FilenameFilter() {
             public boolean accept(File d, String name) {
                 return true;
             }
         };
 
     private File directory = null;
 
     private FilenameFilter filenameFilter = DEFAULT_FILENAMEFILTER;
 
     private String[] filenames = {};
 
     private final HashMap filenameCache = new HashMap();
 
     public FileTableModel() {
     }
 
     public FileTableModel(File dir) {
         setDirectory(dir);
     }
 
     public void setDirectory(File d) {
         setDirectory(d, null);
     }
 
     public String getColumnName(int column) {
         switch(column) {
         case 0:
             return "Name";
         case 1:
             return "Modified";
         case 2:
             return "Size";
         }
 
         return "";
     }
 
     public boolean isCellEditable(int row, int column) {
         if (column == 0)
             return true;
         else
             return false;
     }
 
     public File getDirectory() { return directory; }
 
     public void setDirectory(File d, FilenameFilter f) {
         if ( d==null || !d.isDirectory() ) {
             throw new IllegalArgumentException("no dir " + d);
         }
 
         if ( f!=null )
             filenameFilter = f;
 
         directory = d;
         filenames = directory.list(filenameFilter);
 
         fireTableStructureChanged();
     }
 
     protected void reset() {
         setDirectory(directory, filenameFilter);
     }
 
     public Object getValueAt(int row, int col) {
 
         switch ( col ) {
         case 0:
             return filenames[row];
         case 1:
             return new Date(getFile(filenames[row]).lastModified());
         case 2:
             return new Long(getFile(filenames[row]).length());
         }
 
         return null;
     }
 
     public void setValueAt(Object aValue, int row, int column) {
         if (column == 0) {
             setFileName(row, (String)aValue);
 
             fireTableChanged(new TableModelEvent(this, row, row, column));
         }
     }
 
     public void setFileName(int row, String name) {
        File oldFile = getFile(filenames[row]);
        File newFile = new File(oldFile.getParent() + File.separator + name);

        filenames[row] = name;

        oldFile.renameTo(newFile);
     }
 
     public File getFile(String filename) {
         File f = (File)filenameCache.get(filename);
 
         if ( f==null ) {
             f = new File(directory.getAbsolutePath() + File.separator + filename);
             filenameCache.put(filename, f);
         }
 
         return f;
     }
 
     public File getFileAt(int row) {
         return (File)filenameCache.get(filenames[row]);
     }
 
     public int getRowCount() {
         return filenames.length;
     }
 
     public int getColumnCount() {
         return 3;
     }
 
     public Class getColumnClass(int col) {
         switch ( col ) {
         case 2:
             return Long.class;
         default:
             return String.class;
         }
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
