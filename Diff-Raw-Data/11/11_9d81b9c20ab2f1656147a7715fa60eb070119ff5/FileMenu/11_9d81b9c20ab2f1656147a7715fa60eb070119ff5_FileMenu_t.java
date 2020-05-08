 /**
  * Copyright (C) 2000 Maynard Demmon, maynard@organic.com
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
  
 package com.organic.maynard.outliner;
 
 import java.io.*;
 import java.util.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 public class FileMenu extends AbstractOutlinerMenu implements ActionListener {
 	public static final String FILE_NEW = "New";
 	public static final String FILE_OPEN = "Open...";
 	public static final String FILE_OPEN_RECENT = "Open Recent";
 	public static final String FILE_SAVE = "Save";
 	public static final String FILE_SAVE_ALL = "Save All";
 	public static final String FILE_SAVE_AS = "Save As...";
 	public static final String FILE_REVERT = "Revert";
 	public static final String FILE_CLOSE = "Close";
 	public static final String FILE_CLOSE_ALL = "Close All";
 	public static final String FILE_QUIT = "Quit";
 
 	public JMenuItem FILE_NEW_ITEM = new JMenuItem(FILE_NEW);
 	public JMenuItem FILE_OPEN_ITEM = new JMenuItem(FILE_OPEN);
 	public RecentFilesList FILE_OPEN_RECENT_MENU;
 	// Seperator	
 	public JMenuItem FILE_SAVE_ITEM = new JMenuItem(FILE_SAVE);
 	public JMenuItem FILE_SAVE_ALL_ITEM = new JMenuItem(FILE_SAVE_ALL);
 	public JMenuItem FILE_SAVE_AS_ITEM = new JMenuItem(FILE_SAVE_AS);
 	public JMenuItem FILE_REVERT_ITEM = new JMenuItem(FILE_REVERT);
 	// Seperator	
 	public JMenuItem FILE_CLOSE_ITEM = new JMenuItem(FILE_CLOSE);
 	public JMenuItem FILE_CLOSE_ALL_ITEM = new JMenuItem(FILE_CLOSE_ALL);
 	// Seperator	
 	public JMenuItem FILE_QUIT_ITEM = new JMenuItem(FILE_QUIT);
 	
 	// The Constructors
 	public FileMenu() {
 		super("File");
 
 		FILE_NEW_ITEM.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK, false));
 		FILE_NEW_ITEM.addActionListener(this);
 		add(FILE_NEW_ITEM);
 
 		FILE_OPEN_ITEM.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK, false));
 		FILE_OPEN_ITEM.addActionListener(this);
 		add(FILE_OPEN_ITEM);
 
 		FILE_OPEN_RECENT_MENU = new RecentFilesList(FILE_OPEN_RECENT,Outliner.getMostRecentDocumentTouched());
 		add(FILE_OPEN_RECENT_MENU);
 
 		insertSeparator(3);
 
 		FILE_SAVE_ITEM.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK, false));
 		FILE_SAVE_ITEM.addActionListener(this);
 		FILE_SAVE_ITEM.setEnabled(false);
 		add(FILE_SAVE_ITEM);
 
 		FILE_SAVE_ALL_ITEM.addActionListener(this);
 		FILE_SAVE_ALL_ITEM.setEnabled(false);
 		add(FILE_SAVE_ALL_ITEM);
 		
 
 		FILE_SAVE_AS_ITEM.addActionListener(this);
 		FILE_SAVE_AS_ITEM.setEnabled(false);
 		add(FILE_SAVE_AS_ITEM);
 
 		FILE_REVERT_ITEM.addActionListener(this);
 		FILE_REVERT_ITEM.setEnabled(false);
 		add(FILE_REVERT_ITEM);
 
 		insertSeparator(8);
 
 		FILE_CLOSE_ITEM.setAccelerator(KeyStroke.getKeyStroke('W', Event.CTRL_MASK, false));
 		FILE_CLOSE_ITEM.addActionListener(this);
 		FILE_CLOSE_ITEM.setEnabled(false);
 		add(FILE_CLOSE_ITEM);
 
 		FILE_CLOSE_ALL_ITEM.addActionListener(this);
 		FILE_CLOSE_ALL_ITEM.setEnabled(false);
 		add(FILE_CLOSE_ALL_ITEM);
 
 		insertSeparator(11);
 
 		FILE_QUIT_ITEM.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK, false));
 		FILE_QUIT_ITEM.addActionListener(this);
 		add(FILE_QUIT_ITEM);
 	}
 
 
 	// ActionListener Interface
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(FILE_NEW)) {
 			newOutlinerDocument();
 		} else if (e.getActionCommand().equals(FILE_SAVE)) {
 			saveOutlinerDocument(Outliner.getMostRecentDocumentTouched());
 		} else if (e.getActionCommand().equals(FILE_SAVE_ALL)) {
 			saveAllOutlinerDocuments();
 		} else if (e.getActionCommand().equals(FILE_SAVE_AS)) {
 			saveAsOutlinerDocument(Outliner.getMostRecentDocumentTouched());
 		} else if (e.getActionCommand().equals(FILE_REVERT)) {
 			revertOutlinerDocument(Outliner.getMostRecentDocumentTouched());
 		} else if (e.getActionCommand().equals(FILE_OPEN)) {
 			openOutlinerDocument(Outliner.getMostRecentDocumentTouched());
 		} else if (e.getActionCommand().equals(FILE_CLOSE)) {
 			closeOutlinerDocument(Outliner.getMostRecentDocumentTouched());
 		} else if (e.getActionCommand().equals(FILE_CLOSE_ALL)) {
 			closeAllOutlinerDocuments();
 		} else if (e.getActionCommand().equals(FILE_QUIT)) {
 			quit();
 		}
 	}
 
 	// File Menu Methods
 	public static void quit() {
 		if (!closeAllOutlinerDocuments()) {
 			return;
 		}
 
 		// Hide Desktop
 		Outliner.outliner.setVisible(false);
 		Outliner.outliner.dispose();
 		
 		// Save config and quit
 		Preferences.saveConfigFile(Outliner.CONFIG_FILE);
 		RecentFilesList.saveConfigFile(Outliner.RECENT_FILES_FILE);
 		System.exit(0);
 		
 	}
 	
 	protected static void closeOutlinerDocument(OutlinerDocument document) {
 		OutlinerWindowMonitor.closeInternalFrame(document);
 	}
 
 	protected static boolean closeAllOutlinerDocuments() {
 		for (int i = Outliner.openDocumentCount() - 1; i >= 0; i--) {
 			if (!OutlinerWindowMonitor.closeInternalFrame(Outliner.getDocument(i))) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	protected static void newOutlinerDocument() {
 		new OutlinerDocument("");
 	}
 
 	protected static void saveOutlinerDocument(OutlinerDocument document) {
 		if (!document.getFileName().equals("")) {
 			saveFile(document.getFileName(),document,false);
 		} else {
 			saveAsOutlinerDocument(document);
 		}
 	}
 
 	protected static void saveAllOutlinerDocuments() {
 		for (int i = 0; i < Outliner.openDocumentCount(); i++) {
 			OutlinerDocument doc = Outliner.getDocument(i);
 			if (doc.isFileModified()) {
 				saveOutlinerDocument(doc);
 			}
 		}
 	}
 
 	protected static void saveAsOutlinerDocument(OutlinerDocument document) {
 		// Setup comboBoxes from settings
 		Outliner.chooser.setAccessory(Outliner.saveAccessory);
 		Outliner.lineEndComboBox.setSelectedItem(document.settings.lineEnd.cur);
 		Outliner.encodingComboBox.setSelectedItem(document.settings.saveEncoding.cur);
 		Outliner.saveFormatComboBox.setSelectedItem(document.settings.saveFormat.cur);
 
 		int option = Outliner.chooser.showSaveDialog(document);
 		if (option == JFileChooser.APPROVE_OPTION) {
 			String filename = Outliner.chooser.getSelectedFile().getPath();
 			if (!Outliner.isFileNameUnique(filename) && (!filename.equals(document.getFileName()))) {
 				JOptionPane.showMessageDialog(document, "The file: " + filename + " is already in use.");
 				return;
 			}
 			
 			// Pull Preference Values from the file chooser
 			String lineEnd = (String) Outliner.lineEndComboBox.getSelectedItem();
 			String encoding = (String) Outliner.encodingComboBox.getSelectedItem();
 			String fileFormat = (String) Outliner.saveFormatComboBox.getSelectedItem();
 
 			// Update the document settings
 			document.settings.lineEnd.def = lineEnd;
 			document.settings.lineEnd.cur = lineEnd;
 			document.settings.lineEnd.tmp = lineEnd;
 			document.settings.saveEncoding.def = encoding;
 			document.settings.saveEncoding.cur = encoding;
 			document.settings.saveEncoding.tmp = encoding;
 			document.settings.saveFormat.def = fileFormat;
 			document.settings.saveFormat.cur = fileFormat;
 			document.settings.saveFormat.tmp = fileFormat;
 			
 			saveFile(filename,document,true);
 		}
 	}
 	
 	protected static void openOutlinerDocument(OutlinerDocument document) {
 		// Setup comboBoxes from global settings
 		Outliner.chooser.setAccessory(Outliner.openAccessory);
 		Outliner.lineEndComboBox.setSelectedItem(Preferences.LINE_END.cur);
 		Outliner.openEncodingComboBox.setSelectedItem(Preferences.OPEN_ENCODING.cur);
 		Outliner.openFormatComboBox.setSelectedItem(Preferences.OPEN_FORMAT.cur);
 		
 		int option = Outliner.chooser.showOpenDialog(document);
 		if (option == JFileChooser.APPROVE_OPTION) {
 			String filename = Outliner.chooser.getSelectedFile().getPath();
 			if (!Outliner.isFileNameUnique(filename)) {
 				JOptionPane.showMessageDialog(document, "The file: " + filename + " is already open.");
 				return;
 			}
 			
 			// Pull Preference Values from the file chooser
 			String encoding = (String) Outliner.openEncodingComboBox.getSelectedItem();
 			String fileFormat = (String) Outliner.openFormatComboBox.getSelectedItem();
 
 			DocumentInfo docInfo = new DocumentInfo();
 			docInfo.setPath(filename);
 			docInfo.setEncodingType(encoding);
 			docInfo.setFileFormat(fileFormat);
 			
 			openFile(docInfo);
 		}
 	}
 
 	protected static void revertOutlinerDocument(OutlinerDocument document) {
 		int result = JOptionPane.showConfirmDialog(document, "Revert File? All Changes will be lost.","",JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			revertFile(document.getFileName(),document);
 		} else if (result == JOptionPane.NO_OPTION) {
 			return;
 		}
 	}
 
 	protected static void saveFile(String filename, OutlinerDocument document, boolean saveAs) {
 		// Get the file format object
 		String fileFormatName = document.settings.saveFormat.cur;
 		SaveFileFormat saveFileFormat = Outliner.fileFormatManager.getSaveFormat(fileFormatName);
 		if (saveFileFormat == null) {
 			JOptionPane.showMessageDialog(document, "An error occurred. Could not save file: " + Outliner.chooser.getSelectedFile().getPath() + " because I couldn't retrieve the file format: " + fileFormatName);
 			return;
 		}
 		
 		// Save the file
 		DocumentInfo docInfo = new DocumentInfo();
 		docInfo.setPath(filename);
 		docInfo.setEncodingType(document.settings.saveEncoding.cur);
 		docInfo.setLineEnding(document.settings.lineEnd.cur);
 		docInfo.setFileFormat(document.settings.saveFormat.cur);
 		Rectangle r = document.getNormalBounds();
 		docInfo.setWindowTop(r.y);
 		docInfo.setWindowLeft(r.x);
 		docInfo.setWindowBottom(r.y + r.height);
 		docInfo.setWindowRight(r.x + r.width);
 		int index = document.tree.visibleNodes.indexOf(document.panel.layout.getNodeToDrawFrom()) + 1;
 		docInfo.setVerticalScrollState(index);
 		docInfo.getExpandedNodes().clear();
 		for (int i = 0; i < document.tree.visibleNodes.size(); i++) {
 			Node node = (Node) document.tree.visibleNodes.get(i);
 			if (node.isExpanded()) {
 				docInfo.addExpandedNodeNum(i);
 			}
 		}
 					
 		boolean success = saveFileFormat.save(document.tree, docInfo);
 		if (!success) {
 			JOptionPane.showMessageDialog(document, "An error occurred. Could not save file: " + Outliner.chooser.getSelectedFile().getPath());
 			return;
 		}
 
 		// Stop collecting text edits into the current undoable.
 		UndoableEdit.freezeUndoEdit(document.tree.getEditingNode());
 		
 		// Update the Recent File List
 		if (saveAs && !document.getFileName().equals(filename)) {
 			RecentFilesList.addFileNameToList(docInfo);
 		} else {
 			RecentFilesList.updateFileNameInList(filename, docInfo);
 		}
 
 		document.setFileName(filename);
 		document.setTitle(filename);
 		document.setFileModified(false);
 
 		// Update the Window Menu
 		WindowMenu.updateWindow(document);
 	}
 		
 	protected static void openFile(DocumentInfo docInfo) {
 		String filename = docInfo.getPath();
 		String encoding = docInfo.getEncodingType();
 		String fileFormat = docInfo.getFileFormat();
 		
 		// Get the file format object
 		OpenFileFormat openFileFormat = Outliner.fileFormatManager.getOpenFormat(fileFormat);
 		if (openFileFormat == null) {
 			JOptionPane.showMessageDialog(Outliner.outliner, "An error occurred. Could not open file: " + filename + " because I couldn't retrieve the file format: " + fileFormat);
 			return;
 		}
 		
 		// Load the file
 		TreeContext tree = new TreeContext();
 
 		boolean success = openFileFormat.open(tree, docInfo);
 		if (!success) {
 			RecentFilesList.removeFileNameFromList(filename);
 			return;
 		}
 		
 		// Create a new document
 		OutlinerDocument newDoc = new OutlinerDocument(filename);
 		tree.doc = newDoc;
 		newDoc.tree = tree;
 		
 		// Set bounds
 		if (newDoc.isMaximum()) {
 			newDoc.setNormalBounds(new Rectangle(docInfo.getWindowLeft(), docInfo.getWindowTop(), docInfo.getWidth(), docInfo.getHeight()));
 		} else {
 			newDoc.setBounds(docInfo.getWindowLeft(), docInfo.getWindowTop(), docInfo.getWidth(), docInfo.getHeight());
 		}
 		
 		newDoc.settings.syncPrefs();
 		newDoc.settings.saveEncoding.def = encoding;
 		newDoc.settings.saveEncoding.cur = encoding;
 		newDoc.settings.saveEncoding.tmp = encoding;
 		newDoc.settings.saveFormat.def = fileFormat;
 		newDoc.settings.saveFormat.cur = fileFormat;
 		newDoc.settings.saveFormat.tmp = fileFormat;
 		newDoc.settings.useDocumentSettings = true;
 		
 		// Clear current selection
 		tree.clearSelection();
 		
 		// Clear the VisibleNodeCache
 		tree.visibleNodes.clear();
 		
 		// Insert nodes into the VisibleNodes Cache
 		for (int i = 0; i < tree.rootNode.numOfChildren(); i++) {
 			tree.addNode(tree.rootNode.getChild(i));
 		}
 		
 		// Update the menuBar
 		newDoc.setFileName(filename);
 		newDoc.setFileModified(false);
 		newDoc.setTitle(filename);
 
 		// Move it to the bottom of the recent files list
 		RecentFilesList.updateFileNameInList(filename, docInfo);
 
 		// Expand Nodes
 		Vector expandedNodes = docInfo.getExpandedNodes();
 		for (int i = 0; i < expandedNodes.size(); i++) {
 			int nodeNum = ((Integer) expandedNodes.elementAt(i)).intValue();
 			try {
 				Node node = (Node) newDoc.tree.visibleNodes.get(nodeNum);
 				node.setExpanded(true);
 			} catch (Exception e) {
 				break;
 			}
 		}
 		
 		// Record the current location
 		Node firstVisibleNode;
 		int index = -1;
 		try {
 			index = docInfo.getVerticalScrollState() - 1;
 			firstVisibleNode = (Node) tree.visibleNodes.get(index);
 		} catch (IndexOutOfBoundsException e) {
 			index = 0;
 			firstVisibleNode = (Node) tree.visibleNodes.get(0);
 		}
 		
 		tree.setEditingNode(firstVisibleNode);
 		tree.setCursorPosition(0);
 		tree.setComponentFocus(outlineLayoutManager.TEXT);
 		
 		// Redraw
 		outlineLayoutManager layout = newDoc.panel.layout;
 		layout.setNodeToDrawFrom(firstVisibleNode,index);
 		//layout.draw(firstVisibleNode, outlineLayoutManager.TEXT);
 		layout.draw();
 	}
 
 	protected static void revertFile(String filename, OutlinerDocument document) {
 		// Get the file format object
 		String fileFormatName = document.settings.saveFormat.cur;
 		OpenFileFormat openFileFormat = Outliner.fileFormatManager.getOpenFormat(fileFormatName);
 		if (openFileFormat == null) {
 			JOptionPane.showMessageDialog(document, "An error occurred. Could not revert file: " + filename + " because I couldn't retrieve the file format: " + fileFormatName);
 			return;
 		}
 		
 		// Load the file
 		TreeContext tree = new TreeContext();
 		
 		DocumentInfo docInfo = new DocumentInfo();
 		docInfo.setPath(filename);
 		docInfo.setEncodingType(document.settings.saveEncoding.cur);
 
 		boolean success = openFileFormat.open(tree, docInfo);
 		if (!success) {
 			RecentFilesList.removeFileNameFromList(filename); // Not really sure this is appropriate.
 			return;
 		}
 		
 		// Swap in the new tree
 		tree.doc = document;
 		document.tree = tree;
 		
 		// Clear the UndoQueue
 		document.undoQueue.clear();
 		
 		// Clear current selection
 		tree.clearSelection();
 		
 		// Clear the VisibleNodeCache
 		tree.visibleNodes.clear();
 		
 		// Insert nodes into the VisibleNodes Cache
 		for (int i = 0; i < tree.rootNode.numOfChildren(); i++) {
 			tree.addNode(tree.rootNode.getChild(i));
 		}
 		
 		// Update the menuBar
 		document.setFileName(filename);
 		document.setFileModified(false);
 		
 		// Record the current location
 		tree.setEditingNode((Node) tree.visibleNodes.get(0));
 		tree.setCursorPosition(0);
 		tree.setComponentFocus(outlineLayoutManager.TEXT);
 		
 		// Redraw
 		outlineLayoutManager layout = document.panel.layout;
 		layout.setNodeToDrawFrom((Node) tree.visibleNodes.get(0), 0);
 		layout.draw((Node) tree.visibleNodes.get(0), outlineLayoutManager.TEXT);
 	}
 }
