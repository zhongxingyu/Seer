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
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.datatransfer.*;
 import java.io.*;
 import java.util.*;
 
 import javax.swing.*;
 
 import org.xml.sax.*;
 
 import com.organic.maynard.util.*;
 import com.organic.maynard.io.FileTools;
 
 // WebFile
 import javax.swing.filechooser.*;
 import com.yearahead.io.*;
 
 public class Outliner extends JFrame implements ClipboardOwner, GUITreeComponent {
 	
 	// Constants
 	private static final String A_TITLE = "title";
 	
 	public static final String USER_OUTLINER_DIR = "outliner";
 	
 	// Directory setup
 	public static String GRAPHICS_DIR = "graphics" + System.getProperty("file.separator");
 	public static String PREFS_DIR = "prefs" + System.getProperty("file.separator");
 	public static String USER_PREFS_DIR = PREFS_DIR;
 
 	// Find out if we've got a home directory to work with for user preferences, if
 	// not then we use the prefs dir as usual.
 	static {
 		String userhome = System.getProperty("user.home");
 		if ((userhome != null) && !userhome.equals("")) {
 			USER_PREFS_DIR = userhome + System.getProperty("file.separator") + USER_OUTLINER_DIR + System.getProperty("file.separator");
 		}
 	}
 
 	// These prefs should be under the users prefs dir, or if no user prefs dir exists then
 	// they should be under the apps prefs dir.
 	public static String MACROS_DIR = USER_PREFS_DIR + "macros" + System.getProperty("file.separator");
 	public static String MACROS_FILE = USER_PREFS_DIR + "macros.txt";
 	public static String CONFIG_FILE = USER_PREFS_DIR + "config.txt";
 	public static String RECENT_FILES_FILE = USER_PREFS_DIR + "recent_files.txt";
 
 	// Make the directories in case they don't exist.
 	static {
 		boolean isCreated = false;
 		
 		File prefsFile = new File(PREFS_DIR);
 		isCreated = prefsFile.mkdirs();
 		if (isCreated) {
 			System.out.println("Created Preferences Directory: " + prefsFile.getPath());
 		}
 		
 		File userPrefsFile = new File(USER_PREFS_DIR);
 		isCreated = userPrefsFile.mkdirs();
 		if (isCreated) {
 			System.out.println("Created User Preferences Directory: " + userPrefsFile.getPath());
 		}
 		
		// Create macros directory it it doesn't exist, and copy over macros from installation directory.
 		File macrosFile = new File(MACROS_DIR);
 		isCreated = macrosFile.mkdirs();
 		if (isCreated) {
 			System.out.println("Created Macros Directory: " + macrosFile.getPath());
 			try {
 				FileTools.copy(new File(PREFS_DIR + "macros"), macrosFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Copy over macros.txt from installation directory if it doesn't exist in the user's home directory.
		File userMacrosFile = new File(MACROS_FILE);
		if (!userMacrosFile.exists()) {
			System.out.println("Copying over macros config file: " + userMacrosFile.getPath());
			try {
				FileTools.copy(new File(PREFS_DIR + "macros.txt"), userMacrosFile);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	// These dirs should always be under the apps prefs dir.
 	public static String MACRO_CLASSES_FILE = PREFS_DIR + "macro_classes.txt";
 	public static String ENCODINGS_FILE = PREFS_DIR + "encodings.txt";
 	public static String FILE_FORMATS_FILE = PREFS_DIR + "file_formats.txt";
 	public static String GUI_TREE_FILE = PREFS_DIR + "gui_tree.xml";
 	
 	
 	// Command Parser
 	public static final String COMMAND_PARSER_SEPARATOR = "|";
 	public static final String COMMAND_SET = "set";
 	public static final String COMMAND_MACRO_CLASS = "macro_class";
 	public static final String COMMAND_MACRO = "macro";
 	public static final String COMMAND_FILE_FORMAT = "file_format";
 	public static final CommandParser PARSER = new CommandParser(COMMAND_PARSER_SEPARATOR);
 	
 	
 	// GUI Objects
 	public static FindReplaceFrame findReplace = null;
 	public static MacroManagerFrame macroManager = null;
 	public static MacroPopupMenu macroPopup = null;
 	public static FileFormatManager fileFormatManager = null;
 
 
 	// GUI Settings
 	static final int MIN_WIDTH = 450;
 	static final int MIN_HEIGHT = 450;
  
  	static final int INITIAL_WIDTH = 600;
 	static final int INITIAL_HEIGHT = 600;
 	
 	public static Outliner outliner = null;
 	public static OutlinerDesktop desktop = new OutlinerDesktop();
 	public static JScrollPane jsp;
 	public static OutlinerDesktopMenuBar menuBar;
 	public static OutlinerFileChooser chooser = null;
 	public static DocumentStatistics statistics = new DocumentStatistics();
 
 	static {
 		jsp = new JScrollPane(desktop,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		jsp.addComponentListener(new DesktopScrollPaneComponentListener());
 	}
 
 
 	// GUITreeComponent interface	
 	private String id = null;
 	public String getGUITreeComponentID() {return this.id;}
 	public void setGUITreeComponentID(String id) {this.id = id;}
 
 	public void startSetup(AttributeList atts) {
 		outliner = this;	
 
 		setTitle(atts.getValue(A_TITLE));
 
 		// Load Preferences
 		PARSER.addCommand(new SetPrefCommand(COMMAND_SET,2));
 		PARSER.addCommand(new LoadMacroClassCommand(COMMAND_MACRO_CLASS,2));
 		PARSER.addCommand(new LoadMacroCommand(COMMAND_MACRO,2));
 		PARSER.addCommand(new LoadFileFormatClassCommand(COMMAND_FILE_FORMAT,2));
 
 		System.out.println("Loading Encoding Types...");
 		loadPrefsFile(PARSER,ENCODINGS_FILE);
 		System.out.println("Done Loading Encoding Types.");
 
 		// Setup the FileFormatManager
 		fileFormatManager = new FileFormatManager();
 		
 		System.out.println("Loading File Formats...");
 		loadPrefsFile(PARSER,FILE_FORMATS_FILE);
 		System.out.println("Done Loading File Formats.");
 
 		// Setup the Desktop
 		addComponentListener(new WindowSizeManager(INITIAL_WIDTH,INITIAL_HEIGHT,MIN_WIDTH,MIN_HEIGHT));
 		addWindowListener(
 			new WindowAdapter() {
 				public void windowClosing(WindowEvent e) {
 					QuitMenuItem item = (QuitMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.QUIT_MENU_ITEM);
 					item.quit();
 				}
 			}
 		);
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 
 		setContentPane(jsp);
 
 		// Set Frame Icon
 		ImageIcon icon = new ImageIcon(GRAPHICS_DIR + "frame_icon.gif");
 		setIconImage(icon.getImage());
 	}
 	
 	public void endSetup(AttributeList atts) {
 
 		// Setup the FindReplace Frame
 		findReplace = new FindReplaceFrame();
 		
 		// Setup the MacroManager and the MacroPopupMenu
 		macroManager = new MacroManagerFrame();
 		loadPrefsFile(PARSER,MACRO_CLASSES_FILE);
 		
 		macroPopup = new MacroPopupMenu();
 		
 		System.out.println("Loading Macros...");
 		loadPrefsFile(PARSER,MACROS_FILE);
 		System.out.println("Done Loading Macros.");
 		
 		// WebFile
 		// Note the outliner will have to be restarted if user switches
 		// file system preferences since the chooser is created just once.
 		// There seems to be a bug where the WEB_FILE_SYSTEM will not
 		// work once the native file system is set, otherwise you could
 		// just call setFileSystemView() on the file chooser.
 		FileSystemView fsv = null;
 		if (Preferences.getPreferenceBoolean(Preferences.WEB_FILE_SYSTEM).cur) {
 			// if you have authentication enabled on your web server,
 			// pass non-null user/password
 			fsv = new WebFileSystemView(Preferences.getPreferenceString(Preferences.WEB_FILE_URL).cur,
 										Preferences.getPreferenceString(Preferences.WEB_FILE_USER).cur,
 										Preferences.getPreferenceString(Preferences.WEB_FILE_PASSWORD).cur);
 		}
 
 		// Setup the File Chooser
 		chooser = new OutlinerFileChooser(fsv);	
 
 
 		// Set the Desktop Color
 		jsp.getViewport().setBackground(Preferences.getPreferenceColor(Preferences.DESKTOP_BACKGROUND_COLOR).cur);
 		desktop.setBackground(Preferences.getPreferenceColor(Preferences.DESKTOP_BACKGROUND_COLOR).cur);
 
 		setVisible(true);
 
 		// Create a Document. This must come after visiblity otherwise the window won't be activated.
 		if (Preferences.getPreferenceBoolean(Preferences.NEW_DOC_ON_STARTUP).cur) {
 			new OutlinerDocument("");
 		}
 	}
 		
 	public static void main(String args[]) {
 		//UIManager.put("ScrollBarUI", "com.organic.maynard.outliner.OutlinerScrollBarUI");
 
 		GUITreeLoader loader = new GUITreeLoader();
 		boolean success = loader.load(Outliner.GUI_TREE_FILE);
 		if (!success) {
 			System.out.println("GUI Loading Error: exiting.");
 			System.exit(0);
 		}
 
 		// See if a file to open was provided at the command line.
 		String filepath = null;
 		try {
 			filepath = args[0];
 			if (filepath != null) {
 				String extension = filepath.substring(filepath.lastIndexOf(".") + 1,filepath.length());
 				String fileFormat = Outliner.fileFormatManager.getOpenFileFormatNameForExtension(extension);
 	
 				DocumentInfo docInfo = new DocumentInfo();
 				docInfo.setPath(filepath);
 				docInfo.setEncodingType(Preferences.getPreferenceString(Preferences.OPEN_ENCODING).cur);
 				docInfo.setFileFormat(fileFormat);
 				
 				FileMenu.openFile(docInfo);
 			}
 		} catch (ArrayIndexOutOfBoundsException e) {}
 		
 		// For Debug Purposes
 		if (Preferences.getPreferenceBoolean(Preferences.PRINT_ENVIRONMENT).cur) {
 			Properties properties = System.getProperties();
 			Enumeration names = properties.propertyNames();
 			while (names.hasMoreElements()) {
 				String name = (String) names.nextElement();
 				System.out.println(name + " : " + properties.getProperty(name));
 			}
 		}
 	}
 	
 	// Utility Methods
 	public static boolean isWindows() {
 		String osName = System.getProperty("os.name");
 		if (osName.toLowerCase().startsWith("win")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	
 	// Open Document Repository
 	private static Vector openDocuments = new Vector();
 	private static OutlinerDocument mostRecentDocumentTouched = null;
 
 	public static OutlinerDocument getMostRecentDocumentTouched() {
 		return mostRecentDocumentTouched;
 	}
 	
 	public static void setMostRecentDocumentTouched(OutlinerDocument doc) {
 		mostRecentDocumentTouched = doc;
 		
 		if(mostRecentDocumentTouched != null) {
 			Outliner.menuBar.windowMenu.selectWindow(doc);
 			FindReplaceFrame.enableButtons();
 		} else {
 			FindReplaceFrame.disableButtons();
 			
 			updateSaveMenuItem();
 			updateSaveAllMenuItem();
 			
 			UndoQueue.updateMenuBar(doc);
 			EditMenu.updateEditMenu(doc);
 			
 			OutlineMenu.updateOutlineMenu(doc);
 			SearchMenu.updateSearchMenu(doc);
 			WindowMenu.updateWindowMenu();
 		}
 	}
 	
 	public static void addDocument(OutlinerDocument document) {
 		openDocuments.addElement(document);
 
 		// Add it to the WindowMenu
 		WindowMenu.addWindow(document);
 		
 		// Update the close menu item
 		CloseFileMenuItem closeItem = (CloseFileMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_MENU_ITEM);
 		CloseAllFileMenuItem closeAllItem = (CloseAllFileMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_ALL_MENU_ITEM);
 		
 		closeItem.setEnabled(true);
 		closeAllItem.setEnabled(true);
 	}
 	
 	public static OutlinerDocument getDocument(int i) {
 		return (OutlinerDocument) openDocuments.elementAt(i);
 	}
 	
 	public static OutlinerDocument getDocument(String filename) {
 		for (int i = 0; i < openDocuments.size(); i++) {
 			OutlinerDocument doc = getDocument(i);
 			if (filename.equals(doc.getFileName())) {
 				return doc;
 			}
 		}
 		return null;	
 	}
 	
 	public static void removeDocument(OutlinerDocument document) {
 		// Remove the document from the window menus
 		WindowMenu.removeWindow(document);
 		
 		openDocuments.removeElement(document);
 		
 		IS_CURRENT_DOCUMENT: if (mostRecentDocumentTouched == document) {
 			if (openDocumentCount() > 0) {
 				for (int i = openDocumentCount() - 1; i >= 0; i--) {
 					OutlinerDocument newDoc = getDocument(i);
 					if (!newDoc.isIcon()) {
 						Outliner.menuBar.windowMenu.changeToWindow(newDoc);
 						break IS_CURRENT_DOCUMENT;
 					}
 				}
 			}
 			setMostRecentDocumentTouched(null);
 			
 		}
 		
 		// Update the Save All Menu Item
 		Outliner.updateSaveMenuItem();
 		Outliner.updateSaveAllMenuItem();
 	}
 	
 	public static int openDocumentCount() {
 		return openDocuments.size();
 	}
 	
 	public static boolean isFileNameUnique(String filename) {
 		// For windows we need to normalize the case.
 		if (isWindows()) {
 			for (int i = 0; i < openDocuments.size(); i++) {
 				if (filename.equalsIgnoreCase(getDocument(i).getFileName())) {
 					return false;
 				}
 			}		
 		} else {
 			for (int i = 0; i < openDocuments.size(); i++) {
 				if (filename.equals(getDocument(i).getFileName())) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 
 	// File Opening and Saving
 	// This method should probably get moved into the FileMenu class if possible.
 	public static void updateSaveMenuItem() {
 		JMenuItem saveItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_MENU_ITEM);
 		JMenuItem saveAsItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_AS_MENU_ITEM);
 		JMenuItem revertItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.REVERT_MENU_ITEM);
 		JMenuItem closeItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_MENU_ITEM);
 		JMenuItem closeAllItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_ALL_MENU_ITEM);
 	
 		if (getMostRecentDocumentTouched() == null) {
 			saveItem.setEnabled(false);
 			saveAsItem.setEnabled(false);
 			revertItem.setEnabled(false);
 			closeItem.setEnabled(false);
 			closeAllItem.setEnabled(false);
 		} else if (getMostRecentDocumentTouched().getFileName().equals("")) {
 			saveItem.setEnabled(true);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(false);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		} else if (getMostRecentDocumentTouched().isFileModified()) {
 			saveItem.setEnabled(true);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		} else {
 			saveItem.setEnabled(false);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(false);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		}
 	}
 	
 	public static void updateSaveAllMenuItem() {
 		boolean enabledState = false;
 		for (int i = 0; i < Outliner.openDocumentCount(); i++) {
 			OutlinerDocument doc = Outliner.getDocument(i);
 			if (doc.isFileModified() || doc.getFileName().equals("")) {
 				enabledState = true;
 				break;
 			}
 		}
 
 		JMenuItem saveAllItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_ALL_MENU_ITEM);
 		saveAllItem.setEnabled(enabledState);
 	}
 
 
 	// ClipboardOwner Interface
 	public static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 	
 	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
 	
 	
 	// Misc Methods
 	public static void loadPrefsFile(CommandParser parser, String filename) {
 		CommandQueue commandQueue = new CommandQueue(25);
 		commandQueue.loadFromFile(filename);
 		
 		while (commandQueue.getSize() > 0) {
 			try {
 				parser.parse((String) commandQueue.getNext());
 			} catch (UnknownCommandException uce) {
 				System.out.println("Unknown Command");
 			}
 		}	
 	}
 
 	public static void redrawAllOpenDocuments() {
 		for (int i = 0; i < openDocuments.size(); i++) {
 			OutlinerDocument doc = getDocument(i);
 			doc.panel.layout.draw();
 			doc.panel.layout.setFocus(doc.tree.getEditingNode(),doc.tree.getComponentFocus());
 		}		
 	}
 }
