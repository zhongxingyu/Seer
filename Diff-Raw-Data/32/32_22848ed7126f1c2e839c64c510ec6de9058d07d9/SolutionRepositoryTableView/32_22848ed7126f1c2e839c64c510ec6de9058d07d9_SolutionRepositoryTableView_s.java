 /*
  * Copyright 2008 Pentaho Corporation.  All rights reserved.
  * This software was developed by Pentaho Corporation and is provided under the terms
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use
  * this file except in compliance with the license. If you need a copy of the license,
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
  * the license for the specific language governing your rights and limitations.
  */
 package org.pentaho.mondrian.publish;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreeNode;
 
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 
 public class SolutionRepositoryTableView extends JScrollPane {
   private static final long serialVersionUID = -807169611634721626L;
   
   Document solutionRepositoryDocument = null;
   boolean showFoldersOnly = true;
   String filters[] = null;
   String clientFilters[] = null;
   String selectedPath = null;
   String selectedFolder = null;
   String baseURL = null;
   String serverUserId = null;
   String serverPassword = null;
   JTable repositoryTable = null;
   SolutionRepositorySelectionCallback callback = null;
   RepositoryHelper repositoryHelper = RepositoryHelper.getInstance();
   
   class MyTableModel extends AbstractTableModel {
 
     private static final long serialVersionUID = 2384550372283413169L;
 
     private String[] columnNames = { "Name", "Type", "Date Modified" };
 
     public int getColumnCount() {
       return columnNames.length;
     }
 
     public int getRowCount() {
       // using selected path / folder find the node in the tree
       // and then the row count is that # of children for the node
       return repositoryHelper.getNumChildrenForPath(selectedFolder, clientFilters);
     }
 
     public String getColumnName(int col) {
       return columnNames[col];
     }
 
     public Object getValueAt(int row, int col) {
       // we just care about the row (it's the child # in the tree)
       return getChildName(row, col);
     }
 
     public Class getColumnClass(int c) {
       return getValueAt(0, c).getClass();
     }
   }
 
   public SolutionRepositoryTableView(Document solutionRepositoryDocument, boolean showFoldersOnly, String baseURL, String[] filters, String serverUserId, String serverPassword, final SolutionRepositorySelectionCallback callback) throws Exception {
     this.showFoldersOnly = showFoldersOnly;
     this.filters = filters;
     this.baseURL = baseURL;
     this.serverUserId = serverUserId;
     this.serverPassword = serverPassword;
     this.callback = callback;
     this.solutionRepositoryDocument = solutionRepositoryDocument;
     repositoryHelper.buildRepositoryTree(solutionRepositoryDocument, clientFilters, showFoldersOnly);
 
     final DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
     treeRenderer.setLeafIcon(new ImageIcon(SolutionRepositoryTree.class.getResource("/images/file.gif")));
 
     final Color defaultPanelBackground = new Color(247, 247, 247);
 
     DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
       public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (column == 0) {
           if (repositoryHelper.isFolder(repositoryHelper.getNodeForSelection((String) value, selectedFolder))) {
             component.setIcon(treeRenderer.getClosedIcon());
           } else {
             component.setIcon(treeRenderer.getLeafIcon());
           }
           if (!isSelected) {
             component.setBackground(defaultPanelBackground);
           }
         } else {
           component.setIcon(null);
           if (!isSelected) {
             component.setBackground(Color.white);
           }
         }
         return component;
       }
     };
 
     repositoryTable = new JTable();
     repositoryTable.setShowHorizontalLines(false);
     repositoryTable.setShowVerticalLines(false);
     repositoryTable.setModel(new MyTableModel());
     repositoryTable.setIntercellSpacing(new Dimension(0, 0));
 
     setViewportView(repositoryTable);
     repositoryTable.addMouseListener(new MouseListener() {
 
       public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() > 1) {
           String possibleSelectedFolder = selectedFolder + "/" + (String) repositoryTable.getValueAt(repositoryTable.getSelectedRow(), repositoryTable.getSelectedColumn());
           DefaultMutableTreeNode nodeForSelection = repositoryHelper.getNodeForSelection((String) repositoryTable.getValueAt(repositoryTable.getSelectedRow(), repositoryTable.getSelectedColumn()), selectedFolder);
           if (repositoryHelper.isFolder(nodeForSelection)) {
             repositoryTable.setModel(new MyTableModel());
             selectedFolder = possibleSelectedFolder;
             callback.repositoryItemSelected(possibleSelectedFolder);
           }
         }
       }
 
       public void mouseEntered(MouseEvent e) {
 
       }
 
       public void mouseExited(MouseEvent e) {
 
       }
 
       public void mousePressed(MouseEvent e) {
 
       }
 
       public void mouseReleased(MouseEvent e) {
 
       }
 
     });
     repositoryTable.setDefaultRenderer(String.class, renderer);
   }
 
   public void setSelectedPath(String path) {
     if (StringUtils.isEmpty(path)) {
       path = "/";
     }
    selectedPath = path;
     selectedFolder = path;
     StringTokenizer st = new StringTokenizer(path, "/");
     DefaultMutableTreeNode node = repositoryHelper.getRootNode();
     List<TreeNode> pathList = new ArrayList<TreeNode>();
     while (st.hasMoreTokens()) {
       String token = st.nextToken();
       DefaultMutableTreeNode child = null;
       boolean found = false;
       for (int i = 0; i < node.getChildCount(); i++) {
         child = (DefaultMutableTreeNode) node.getChildAt(i);
         if (child.getUserObject().equals(token)) {
           found = true;
           break;
         }
       }
       if (found) {
         node = child;
         if (child != null) {
           // if the child is a folder
           if (repositoryHelper.isFolder(child)) {
             pathList.add(child);
           }
         }
       }
     }
     if (pathList.size() > 0) {
       selectedFolder = "";
       for (int i = 0; i < pathList.size(); i++) {
         selectedFolder += "/" + pathList.get(i);
       }
     }
   }
 
   public String getSelectedPath() {
     return selectedPath;
   }
 
   public String[] getClientFilters() {
     return clientFilters;
   }
 
   public void setClientFilters(String clientFilters[]) {
     this.clientFilters = clientFilters;
     // fire tree rebuilding exercise obeying the clientFilter
     repositoryHelper.buildRepositoryTree(solutionRepositoryDocument, clientFilters, showFoldersOnly);
     setSelectedPath(selectedPath);
   }
 
   public void refresh(boolean fetchRepositoryDocument) throws Exception {
     if (fetchRepositoryDocument) {
       solutionRepositoryDocument = repositoryHelper.getRepositoryDocument(baseURL, filters, serverUserId, serverPassword);
     }
     repositoryHelper.buildRepositoryTree(solutionRepositoryDocument, clientFilters, showFoldersOnly);
     repositoryTable.setModel(new MyTableModel());
   }
 
   public String getChildName(int index, int column) {
     StringTokenizer st = new StringTokenizer(selectedFolder, "/");
     DefaultMutableTreeNode node = repositoryHelper.getRootNode();
     while (st.hasMoreTokens()) {
       String token = st.nextToken();
       DefaultMutableTreeNode child = null;
       boolean found = false;
       for (int i = 0; node != null && i < node.getChildCount(); i++) {
         child = (DefaultMutableTreeNode) node.getChildAt(i);
         if (child.getUserObject().equals(token)) {
           found = true;
           break;
         }
       }
       if (found) {
         node = child;
       }
     }
     if (node != null) {
       if (column == 0) {
         return node.getChildAt(index).toString();
       } else if (column == 1) {
         if (repositoryHelper.isFolder(node.getChildAt(index))) {
           return "File Folder";
         }
       } else if (column == 2) {
         if (repositoryHelper.hasDate(node.getChildAt(index))) {
           return repositoryHelper.getDate(node.getChildAt(index));
         }
       }
     }
     return "";
   }
 
   public void levelUp() {
     if (selectedFolder.lastIndexOf("/") == -1) {
       // we're at root
       return;
     }
     selectedFolder = selectedFolder.substring(0, selectedFolder.lastIndexOf("/"));
     repositoryTable.setModel(new MyTableModel());
   }
 
   public String getSelectedFolder() {
     return selectedFolder;
   }
 
   public void setSelectedFolder(String selectedFolder) {
     this.selectedFolder = selectedFolder;
   }
 
   public RepositoryHelper getRepositoryHelper() {
     return repositoryHelper;
   }
 
 }
