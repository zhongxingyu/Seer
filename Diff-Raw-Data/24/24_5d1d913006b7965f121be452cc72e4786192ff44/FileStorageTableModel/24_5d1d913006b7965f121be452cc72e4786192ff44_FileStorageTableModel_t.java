 package com.scurab.java.ftpleechergui.model;
 
 import com.scurab.java.ftpleechergui.Application;
 import com.scurab.java.ftpleechergui.TextUtils;
import org.apache.commons.net.ftp.FTPFile;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
 
 public class FileStorageTableModel extends AbstractTableModel {
 
     private File mRoot;
 
     private File[] mData;
 
     private ImageIcon mFolderIcon = new ImageIcon();
 
     public FileStorageTableModel() {
         this(null);
     }
 
     public FileStorageTableModel(File root) {
 
         try {
             mFolderIcon.setImage(ImageIO.read(getClass().getResourceAsStream("/assets/folder.png")));
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         mRoot = root;
         mData = getDirs(mRoot);
     }
 
     private File[] getDirs(File root) {
         if (root == null) {
             return File.listRoots();
         } else {
             ArrayList<File> data = new ArrayList<File>(Arrays.asList(root.listFiles(new FileFilter() {
                 @Override
                 public boolean accept(File pathname) {
                     return pathname.isDirectory();
                 }
             })));
            Collections.sort(data, mComparator);
             data.add(0, root.getParentFile());
             return data.toArray(new File[data.size()]);
         }
     }
 
     @Override
     public int getRowCount() {
         return mData.length;
     }
 
     @Override
     public int getColumnCount() {
         return 2;
     }
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         if (columnIndex == 0) {
             return ImageIcon.class;
         } else {
             return String.class;
         }
     }
 
     @Override
     public String getColumnName(int column) {
         if (column == 0) {
             return "";
         } else if (column == 1) {
             return Application.getLabels().getString("Dir");
         } else {
             return super.getColumnName(column);
         }
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         if ((rowIndex == 0 && mRoot != null) || mData[rowIndex] == null) {
             if (columnIndex == 1) {
                 return "..";
             } else {
                 return null;
             }
         } else {
             if (columnIndex == 1) {
                 File f = getItem(rowIndex);
                 String name = TextUtils.nz(f.getName(), f.toString());
                 return name;
             } else {
                 return mFolderIcon;
             }
         }
     }
 
     public File getItem(int rowIndex) {
         return mData[rowIndex];
     }
 
     public void setRoot(File r) throws Exception {
         try {
             mData = getDirs(r);
             mRoot = r;
             fireTableDataChanged();
         } catch (Exception e) {
             throw new Exception("Unable to open dir/drive: " + r.getAbsolutePath());
         }
     }

    /**
     * Help class for sorting
     */
    private Comparator<? super File> mComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
    };
 }
