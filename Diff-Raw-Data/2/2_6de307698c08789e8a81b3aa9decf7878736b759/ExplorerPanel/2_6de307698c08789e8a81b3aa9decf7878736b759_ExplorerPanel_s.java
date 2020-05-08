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
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.tree.*;
 import javax.swing.event.*;
 
 import org.wings.*;
 
 /**
  * TODO: documentation
  *
  * @author Holger Engels
  * @author Andreas Gruener
  * @author Armin Haaf
  * @version $Revision$
  */
 public class ExplorerPanel
     extends SPanel
 {
     private final DirTableModel dirTableModel = new DirTableModel();
 
     private final STable dirTable = new STable(dirTableModel);
 
     public ExplorerPanel(String dir) {
         try {
             java.net.URL templateURL = getClass()
                 .getResource("/explorer/Explorer.thtml");
             // you can of course directly give files here.
             STemplateLayout layout = new STemplateLayout( templateURL );
             setLayout(new STemplateLayout(templateURL));
         }
        catch ( java.io.IOException e ) {
             setLayout(new SFlowLayout());
         }
 
 	add(createTree(dir), "DirTree");
 	add(createTable(), "FileTable");
 	add(createUpload(), "UploadForm");
 	add(createDeleteButton(), "DeleteButton");
     }
 
     /**
      *
      */
     private void deleteFiles() {
         int selected[] = dirTable.getSelectedRows();
 
         for (int i=0; i<selected.length; i++)
             dirTableModel.getFileAt(selected[i]).delete();
 
         dirTableModel.reset();
     }
 
     protected SComponent createTable() {
         dirTable.setSelectionMode(STable.MULTIPLE_SELECTION);
 
         // timestamp and filesize are displayed with special renderers
 
 	dirTable.setDefaultRenderer(Date.class, new DateTableCellRenderer());
 	dirTable.setDefaultRenderer(Long.class, new SizeTableCellRenderer());
 
 	return dirTable; 
     }
 
     protected SComponent createDeleteButton() {
         SButton delete = new SButton("delete selected");
         delete.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 deleteFiles();
             } 
         });
 
         return delete;
     }
 
     protected SComponent createUpload() {
         SForm p = new SForm();
         p.setEncodingType("multipart/form-data");
 
         final SFileChooser chooser = new SFileChooser();
         p.add(chooser);
 
         SButton submit = new SButton("upload");
         submit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 writeFile(chooser.getSelectedFile(), chooser.getFilename());
             }
         });
         p.add(submit);
 
         return p;
     }
 
     /**
      *
      */
     private void writeFile(File file, String fileName) {
         try {
             FileInputStream fin = new FileInputStream(file);
             FileOutputStream fout =
                 new FileOutputStream(dirTableModel.getDirectory().getAbsolutePath() +
                                      File.separator + fileName);
             int val;
 
             while ((val = fin.read()) != -1)
                 fout.write(val);
 
             fin.close();
             fout.close();
 
             dirTableModel.reset();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     protected SComponent createTree(String dir) {
 
         STree explorerTree = new STree(createModel(dir));
 
         // wenn ein Verzeichnis selektiert wird, wird die Tabelle
         // aktualisiert
         explorerTree.addTreeSelectionListener(new TreeSelectionListener() {
             public void valueChanged(TreeSelectionEvent e) {
 
                 TreePath tpath = e.getNewLeadSelectionPath();
                 DefaultMutableTreeNode selectedNode =
                     (DefaultMutableTreeNode)tpath.getLastPathComponent();
 
                 dirTableModel.setDirectory((File)selectedNode.getUserObject());
             } 
         });
 
         explorerTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
         return explorerTree;
     }
 
     protected TreeModel createModel(String dir) {
         DefaultMutableTreeNode root = new DefaultMutableTreeNode(new File(dir));
         appendDirTree(root);
 	return new DefaultTreeModel(root);
     }
 
     /**
      * Build the directory tree. For simplicity a static model.
      */
     private void appendDirTree(DefaultMutableTreeNode root) {
         File entries[] = ((File)root.getUserObject()).listFiles();
         if (entries == null)
             return;
 
         for ( int i=0; i<entries.length; i++) {
             DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(entries[i]);
 
             if (entries[i].isDirectory()) {
                 root.add(nextNode);
                 appendDirTree(nextNode);
             }
         }
 
         return;
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
