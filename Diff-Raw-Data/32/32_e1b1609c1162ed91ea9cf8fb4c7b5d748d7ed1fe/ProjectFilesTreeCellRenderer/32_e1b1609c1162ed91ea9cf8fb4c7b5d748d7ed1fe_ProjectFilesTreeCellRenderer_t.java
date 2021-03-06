 package com.jakeapp.gui.swing.controls;
 
import com.jakeapp.gui.swing.helpers.JakeMainHelper;

 import javax.swing.tree.TreeCellRenderer;
 import javax.swing.*;
 import javax.swing.filechooser.FileSystemView;
 import java.awt.*;
 import java.io.File;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Chris
  * Date: 30-Dec-2008
  * Time: 15:24:24
  * To change this template use File | Settings | File Templates.
  */
 public class ProjectFilesTreeCellRenderer implements TreeCellRenderer {
     @Override
     public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // Apparently doesn't work on Crapple's Mac OS. Sweet.
        // FileSystemView fsv = FileSystemView.getFileSystemView();

         File file = (File) value;
 
         JLabel label = new JLabel();
 
        Icon icon = JakeMainHelper.getFileIcon(file);
 
         label.setIcon(icon);
         label.setText(file.getName());
 
         return label;
     }
 }
