 /**
  * Portions copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
  * Portions copyright (C) 2002  Stan Krute <Stan@StanKrute.com>
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 // we're part of this
 package com.organic.maynard.outliner;
 
 // we use these
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import org.xml.sax.*;
 import com.organic.maynard.util.string.StringTools;
 import com.organic.maynard.util.string.Replace;
 import com.organic.maynard.util.string.StanStringTools ;
 import com.organic.maynard.util.vector.StanVectorTools ;
 
 
public class RecentFilesList extends JMenu implements ActionListener, GUITreeComponent, JoeReturnCodes {
 
 	// Constants
 	private static final String A_TEXT = "text";
 	
 	private static final String TRUNC_STRING = GUITreeLoader.reg.getText("trunc_string");
 	
 	// display mode constants [srk]
 	// ordering
 	private static final int CHRONO_ORDER = 0 ;
 	private static final int ALFA_ORDER = 1 ;
 	private static final int ASCII_ORDER = 2 ;
 	// name form
 	private static final int FULL_PATHNAME = 0 ;
 	private static final int TRUNC_PATHNAME = 1 ;
 	private static final int JUST_FILENAME = 2 ;
 	// direction
 	private static final int TOP_TO_BOTTOM = 0 ;
 	private static final int BOTTOM_TO_TOP = 1 ;
 	
 	// presence in frameInfoList
 	private static final int NOT_THERE = -1 ;
 	
 	// Static Fields
 	private static Vector frameInfoList = null;
 	/*  TBD  
 	 * [srk] move frameInfoList into its own class for cleanth/power/flexibility
 	
 	 * 	its job is state maintenance for ALL internal frames opened in JOE
 	 *	not just std. outline docs, which go up on recent files list,
 	 *	but modeless dialogs and help doc outlines while they're open
 	 *	
 	 *	that will let it restore ALL were-open frames after a crash
 	 *	or at startup if desired
 	 *	
 	 */
 	
 	private static TreeSet alfaAsciiTree = null ; // [srk] for alfa/ascii ordering, we store filename/pathname strings here
 	
 	private static int currentDisplayOrdering = -1 ; // [srk] we start with these values to force a menu population
 	private static int currentDisplayNameForm = -1 ;
 	private static int currentDisplayDirection = -1 ;
 	private static int currentRecentFilesListSize = 0 ;
 	
 	// The Constructors
 	public RecentFilesList() {}
 	
 	// Static Accessors
 	static Vector getFrameInfoList() {
 		return frameInfoList;
 	}
 	
 	static int getSizeOfFrameInfoList() {
 		return frameInfoList.size();
 	}
 	
 	static void setFrameInfoList(Vector list) {
 		frameInfoList = list;
 	}
 	
 	static void addDocumentInfo(DocumentInfo docInfo) {
 		frameInfoList.add(docInfo);
 	}
 
 	// GUITreeComponent interface
	private String id = null;
	public String getGUITreeComponentID() {return this.id;}
	public void setGUITreeComponentID(String id) {this.id = id;}
	
 	public void startSetup(AttributeList atts) {
 		// this lets us test what we read from file
 		Vector testVector = new Vector() ;
 
 		// get the menu's title and set it
 		String title = atts.getValue(A_TEXT);
 		setText(title);
 		
 		// we start out disabled
 		setEnabled(false);
 
 		// Add us to our parent menu.
 		JMenu menu = (JMenu) GUITreeLoader.elementStack.get(GUITreeLoader.elementStack.size() - 2);
 		menu.add(this);
 		
 		// grab some preferences
 		Preferences prefs = (Preferences) GUITreeLoader.reg.get(GUITreeComponentRegistry.PREFERENCES);
 		
 		// set size of Recent Files list
 		currentRecentFilesListSize = ((PreferenceInt)prefs.getPreference(Preferences.RECENT_FILES_LIST_SIZE)).cur;
 
 		// Try to load the frameInfoList from disk
 		// TBD [srk] rename RECENT_FILES_FILE to FRAME_INFO_LIST_FILE
 		Object obj = ReadObjectFromFile(Outliner.RECENT_FILES_FILE);
 		
 		// we need to make sure we have a Vector
 		
 		// we used to use an ArrayList for frameInfoList,
 		// but now we use a Vector 
 		
 		// anything else is just ignored
 		
 		// if we were able to read something ...
 		if (obj != null) {
 			
 			// if we have a Vector ...
 			if (testVector.getClass().isInstance(obj)) {
 				
 				// set the var
 				frameInfoList = (Vector)obj ;
 				
 				// remove any duplicate entries
 				// saves youngest, removes oldest
 				StanVectorTools.removeDupesHeadside(frameInfoList) ;
 				
 			} // end if we have a Vector
 			
 			// else we don't have a Vector
 			else { 
 				// start a fresh list
 				frameInfoList = null ;
 			} // end else we don't have a vector
 
 		// else we couldn't read anything
 		} else {
 			frameInfoList = null; 
 		} //end else
 		
 		int filSize = ((PreferenceInt)prefs.getPreference(Preferences.FRAME_INFO_LIST_SIZE)).def ;
 		
 		// testing wipeout frameInfoList = null ;
 		
 		// if frameInfoList is null ...
 		if (frameInfoList == null) {
 			// start a new frameInfoList
 			frameInfoList = new Vector();
 		// else we're non-null
 		} else {
 			// if we're too large ...
 			if (frameInfoList.size() > filSize) {
 				
 				// trim back
 				StanVectorTools.trimSizeSaveTail(frameInfoList, filSize) ;
 				
 			} // end if we're too large
 			
 		} // end if-else
 		
 		// apply our display settings
 		// this also populates the menu correctly
 		applyDisplaySettings() ;
 		
 		
 	} // end methor startSetup
 	
 	// call on our UI panel to apply the latest display options
 	private void applyDisplaySettings () {
 		
 		// grab ahold of our prefs panel
 		PreferencesPanelRecentFiles prefsPanel = (PreferencesPanelRecentFiles) GUITreeLoader.reg.get(GUITreeComponentRegistry.PREFERENCES_PANEL_RECENT_FILES);
 
 		// have it apply its current settings
 		prefsPanel.applyCurrentToApplication() ;
 		
 	} // end method applyDisplaySettings
 
 	
 	// set display options -- adjust menu and treeset structures as necessary
 	// called from PreferencesPanelRecentFiles.applyCurrentToApplication
 	void setDisplayOptions (int ordering, int nameForm, int direction) {
 		
 		// we are lazy, only work on change
 		boolean change = (currentDisplayOrdering != ordering)
 			|| (currentDisplayNameForm !=nameForm)
 			|| (currentDisplayDirection != direction) ;
 		
 		// if there's change ...
 		if (change) {
 			// store the new values
 			currentDisplayOrdering = ordering ;
 			currentDisplayNameForm = nameForm ;
 			currentDisplayDirection = direction ;
 			
 			// sync up the tree set
 			syncTreeSet() ;
 						
 			// sync up the menu items
 			syncMenuItems() ;
 			
 		} // end if we have a change
 		
 	} // end method setDisplayOptions
 	
 	
 	// ensure that the treeset contains the necessary info
 	private void syncTreeSet () {
 		
 		// if we don't have a treeset, leave
 		if ( ! ensureAlfaAsciiTree() ) {
 			return ;
 		} // end if
 		
 		// empty it out
 		// note that syncTreeSet 
 		// ALWAYS clears the tree if it exists
 		alfaAsciiTree.clear() ;
 		
 		// if this ordering does not use the tree ...
 		if ( (currentDisplayOrdering != ALFA_ORDER)  
 			&&  (currentDisplayOrdering != ASCII_ORDER) ) {
 			// leave
 			return ;
 		} // end if
 		
 		// if frameInfoList is empty ...
 		int frameInfoListSize = frameInfoList.size() ;
 		if (frameInfoListSize == 0) {
 			// leave
 			return ;
 		} // end if
 				
 		// if Recent Files size is zero, leave
 		int recentFilesListSize = Preferences.getPreferenceInt(Preferences.RECENT_FILES_LIST_SIZE).cur ;
 		if (recentFilesListSize == 0) {
 			return;
 		} // end if
 
 		// set up some local vars
 		DocumentInfo docInfo = null ;
 		StrungDocumentInfo strungDocInfo = null ;
 		
 		// let's go mine some data
 
 		// until we get to the bottom of frameInfoList or hit RECENT_FILES_LIST_SIZE ...
 		for (int i = frameInfoListSize - 1, j = 0; (i >= 0) && (j < recentFilesListSize); i--) {
 			
 			// grab docInfo from frameInfoList
 			docInfo = (DocumentInfo) frameInfoList.get(i) ;
 			
 			// if we're a Help file, try the next frame
 			if (docInfo.isHelpFile()) {
 				continue ;
 			} // end if
 			
 			// switch on name form
 			switch (currentDisplayNameForm) {
 				
 			case FULL_PATHNAME: 
 			default:
 				// package the docInfo up with the full pathname
 				strungDocInfo= new StrungDocumentInfo(docInfo.getPath(), docInfo) ;
 				break ;
 				
 			case TRUNC_PATHNAME:
 				// package the docInfo up with a truncated pathname
 				strungDocInfo= new StrungDocumentInfo(
 					StanStringTools.getTruncatedPathName(docInfo.getPath(),TRUNC_STRING),
 					docInfo) ; 
 				break ;
 				
 			case JUST_FILENAME:
 				// package the docInfo up with the filename
 				strungDocInfo= new StrungDocumentInfo(
 					StanStringTools.getFileNameFromPathName(docInfo.getPath()),
 					docInfo) ;
 				break ;
 			
 			} // end switch on name form	
 			
 			// set ascii/alfa switch
 			// we ignore case for alphabetical order, heed it for ASCII order
 			strungDocInfo.setIgnoreCase(currentDisplayOrdering == ALFA_ORDER) ;
 			
 			// add item to the alfaAscii tree
 			alfaAsciiTree.add(strungDocInfo) ;
 			
 			// up the Recent Files list counter
 			j++ ;
 			
 		} // end for
 		
 	} // end method syncTreeSet
 
 		
 	// ensure that this menu contains the proper set of menu items
 	private void syncMenuItems () {
 		// local vars
 		StrungDocumentInfo sdi = null ;
 		DocumentInfo docInfo = null ;
 		
 		// start with a clean slate by removing all menu items
 		removeAll() ;
 		
 		// [srk] this removal does NOT shrink the
 		// displayed width of the menu back to 
 		// a small number
 		// perhaps this is a bug in Sun's JMenu code
 		// looking for a workaround or fix
 		
 		// if frameInfoList is empty, leave
 		int frameInfoListSize = frameInfoList.size() ;
 		if (frameInfoListSize == 0) {
 			return ;
 		} // end if
 		
 		// if Recent Files size is zero, leave
 		int recentFilesListSize = Preferences.getPreferenceInt(Preferences.RECENT_FILES_LIST_SIZE).cur ;
 		if (recentFilesListSize == 0) {
 			return;
 		} // end if
 
 		// switch on ordering
 		switch (currentDisplayOrdering) {
 			
 		case CHRONO_ORDER:
 		default:
 			
 			
 			// we send these out in reverse order
 			reverseDisplayDirection() ;
 			
 			for (int j= 0, i = frameInfoListSize - 1; (i >= 0) && (j < recentFilesListSize); i--) {
 
 				// grab docInfo from frameInfoList
 				docInfo = (DocumentInfo) frameInfoList.get(i) ;
 				
 				if (docInfo == null) {
 					continue ;
 				}
 				
 				// if we're a Help file, try the next frame
 				if (docInfo.isHelpFile()) {
 					continue ;
 				} // end if
 	
 				// okay, we're to be shown -- do it
 				addMenuItemForFileToMenu(docInfo);
 	
 				// up the Recent Files list counter
 				j++ ;
 
 			} // end for
 
 			// go back to our entry direction state
 			reverseDisplayDirection() ;
 
 			break ; // case CHRONO_ORDER, default
 			
 		case ALFA_ORDER:
 		case ASCII_ORDER:
 			// add each item in alfaAscii tree order to the menu
 			// note that we don't check whether item's to be shown on menu
 			// that's because only showing items get added to the tree 
 			for (Iterator iter = alfaAsciiTree.iterator(); iter.hasNext();) {
 				sdi = (StrungDocumentInfo) iter.next();
 				addMenuItemForFileToMenu(sdi.getDocumentInfo());
 			} // end for
 			
 			break ; // case ALFA_ORDER, ASCII_ORDER
 			
 		} // end switch on currentDisplayOrdering	
 		
 	} // end method syncMenuItems
		

	// in case we need to do something at the end of setup
	public void endSetup(AttributeList atts) {}

 
 	// add a menu item for a file to the menu
 	private void addMenuItemForFileToMenu(DocumentInfo docInfo) {
 		// local vars
 		RecentFilesListItem menuItem = null ;
 		StrungDocumentInfo strungItem = null ;
 		
 		// switch on nameform to create a menu item
 		switch (currentDisplayNameForm) {
 			
 		case FULL_PATHNAME: 
 		default:
 			// create a menu item
 			menuItem = new RecentFilesListItem(docInfo.getPath(), docInfo);
 			break ;
 			
 		case TRUNC_PATHNAME:
 			// create a menu item
 			menuItem = new RecentFilesListItem(
 				StanStringTools.getTruncatedPathName(docInfo.getPath(),TRUNC_STRING), 
 				docInfo);
 			break ;
 		
 		case JUST_FILENAME:
 			// create a menu item
 			menuItem = new RecentFilesListItem(
 				StanStringTools.getFileNameFromPathName(docInfo.getPath()), 
 				docInfo);
 			break ;
 		
 		} // end switch on nameform
 		
 		// tell the menu item to listen to its menu
 		menuItem.addActionListener(this);
 		
 		// switch out on menu's item orientation
 		switch (currentDisplayDirection) {
 			
 		case TOP_TO_BOTTOM:
 		default:
 			// append the menu item to the menu
 			add(menuItem);
 			break ;
 			
 		case BOTTOM_TO_TOP:
 			// prepend the menu item to the menu
 			insert(menuItem,0);
 			break ;
 			
 		}  // end switch on direction
 		
 		// since we've got at least one item on our menu, we're enabled
 		setEnabled(true);
 
 	} // end method addMenuItemForFileToMenu
 
 	// make sure we have an alfaAsciiTree
 	// it's needed for alfa/ascii ordering
 	private static boolean ensureAlfaAsciiTree () {
 		// local var
 		TreeSet test = new TreeSet() ;
 		
 		// if we're pointin' to a treeset, we're cool
 		if ( (alfaAsciiTree != null)  &&  (test.getClass().isInstance(alfaAsciiTree)) ) {
 			return true ;
 		} // end if
 		
 		// if not, let's try to create one
 		alfaAsciiTree = new TreeSet() ;
 		
 		// return a result by running the same test
 		return ( (alfaAsciiTree != null)  &&  (test.getClass().isInstance(alfaAsciiTree)) ) ;
 		
 	} // end method ensureAlfaAsciiTree
 
 	// Static methods
 	
 	// this method is called by outsiders when a file is open or imported
 	// they're asking RFL to add the doc to the Recent Files List
 	
 	// if the file is not in frameInfoList, it gets added to it
 	// if the file is already in frameInfoList, it get moves to the tail of the list
 	
 	// we then sync up treeset and the menu
 	static void addFileNameToList(DocumentInfo docInfo) {
 		// local vars
 		String filename = docInfo.getPath();
 		
 		// check to see if this file is in frameInfoList 
 		int position = filePositionInFrameInfoList (filename) ;
 		
 		// if it's not in frameInfoList ...
 		if (position == NOT_THERE) {
 			
 			// if the frameInfoList is too long ...
 			if (frameInfoList.size() >= Preferences.getPreferenceInt(Preferences.FRAME_INFO_LIST_SIZE).cur) {
 				
 				// remove the oldest item
 				frameInfoList.remove(0) ;
 				// removeOldestFileNameFromFrameInfoList() ;
 			
 			} // end if the frameInfoList is too long
 			
 			// add the file's docInfo to frameInfoList
 			frameInfoList.add(docInfo);
 			
 		// else it IS in frameInfoList 
 		} else {
 			// update its doc info
 			frameInfoList.set(position, docInfo) ;
 			
 			// move that to the top of the list
 			StanVectorTools.moveElementToTail(frameInfoList, position) ;
 		} // end else
 
 		RecentFilesList menu = (RecentFilesList) GUITreeLoader.reg.get(GUITreeComponentRegistry.RECENT_FILE_MENU);
 
 		// sync up the tree set
 		menu.syncTreeSet() ;
 					
 		// sync up the menu items
 		menu.syncMenuItems() ;
 			
 	} // end method addFileNameToList
 
 
 	// this method is called by outsiders when a file is to be removed from Recent Files List
 	
 	// if the file is not in frameInfoList, we do nothing
 	// if the file is in frameInfoList, we remove it, then sync up the treeset and the menu items
 	
 	// we then sync up treeset and the menu
 	public static void removeFileNameFromList(DocumentInfo docInfo) {
 		// local vars
 		String filename = docInfo.getPath();
 		
 		// check to see if this file is in frameInfoList 
 		int position = filePositionInFrameInfoList (filename) ;
 		
 		// if it's not in frameInfoList ...
 		if (position == NOT_THERE) {
 			
 			// leave
 			return ;
 			
 		} //end if
 		
 		// okay, it's in frameInfoList
 		
 		// move it to the tail of the list
 		StanVectorTools.moveElementToTail(frameInfoList, position) ;
 		
 		// cut off the tail
 		frameInfoList.setSize(frameInfoList.size() - 1) ;
 		
 		// grab the RFL menu
 		RecentFilesList menu = (RecentFilesList) GUITreeLoader.reg.get(GUITreeComponentRegistry.RECENT_FILE_MENU);
 
 		// sync up the tree set
 		menu.syncTreeSet() ;
 					
 		// sync up the menu items
 		menu.syncMenuItems() ;
 			
 	} // end method removeFileNameFromList
 
 
 	// called by outsiders when the size of the recent files list may have been changed
 	static void syncSize() {
 		 
 		// grab a handle to this menu
 		RecentFilesList menu = (RecentFilesList) GUITreeLoader.reg.get(GUITreeComponentRegistry.RECENT_FILE_MENU);
 		
 		// grab the size that's been set
 		int sizeSet = Preferences.getPreferenceInt(Preferences.RECENT_FILES_LIST_SIZE).cur ;
 		
 		// if it's different than our stored setting ...
 		if (sizeSet != menu.currentRecentFilesListSize) {
 			
 			// store new setting
 			menu.currentRecentFilesListSize = sizeSet ;
 			
 			// sync up the tree set
 			menu.syncTreeSet() ;
 						
 			// sync up the menu items
 			menu.syncMenuItems() ;
 			
 		} // end if it's different than our stored setting
 		
 	} // end method syncSize
 
 
 	// is a file in frameInfoList ?  
 	// if it is, returns position in list
 	// if it is not, returns NOT_THERE (-1)
 	private static int filePositionInFrameInfoList(String pathname) {
 		
 		// for each item in frameInfoList ...
 		for (int i = 0; i < frameInfoList.size(); i++) {
 			
 			// does its path match pathname ?
 			if (pathname.equals(((DocumentInfo) frameInfoList.get(i)).getPath())) {
 				
 				// it does
 				// return position in frameInfoList
 				return i;
 				
 			} // end if
 			
 		} // end for each item in frameInfoList
 		
 		// if we get here, no match, we be unique
 		return NOT_THERE;
 		
 	} // end method isFileNameUnique
 	
 
 	// given a pathname, get docInfo from frameInfoList
 	public static DocumentInfo getDocumentInfo(String pathname) {
 		
 		// for each element in frameInfoList
 		for (int i = 0, limit = frameInfoList.size(); i < limit; i++) {
 			
 			// get it as docInfo
 			DocumentInfo docInfo = (DocumentInfo) frameInfoList.get(i);
 			
 			// if it's nothing
 			if (docInfo == null) {
 				// next element
 				continue ;
 			} // end if
 			
 			// it's not nothing, check for a match
 			// if we have a match
 			if (pathname.equals(docInfo.getPath())) {
 				
 				// return the match's docInfo
 				return docInfo;
 			} // end if we have a match
 		} // end for each element in frameInfoList
 		
 		// if we get here, there's no match
 		return null;
 		
 	} // end method
 
 	
 	// Config File
 	public static void saveConfigFile(String filename) {
 		
 		// write out frameInfoList 
 		writeObjectToFile(frameInfoList, filename);
 		
 	} // end method saveConfigFile
 
 	// ActionListener Interface
 	public void actionPerformed(ActionEvent e) {
 		DocumentInfo docInfo = ((RecentFilesListItem) e.getSource()).getDocumentInfo();
 		String filename = docInfo.getPath();
 		if (!Outliner.isFileNameUnique(filename)) {
 			String msg = GUITreeLoader.reg.getText("message_file_already_open");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, filename);
 			
 			JOptionPane.showMessageDialog(Outliner.outliner, msg);
 			return;
 		}
 
 		// TEMP: get protocol from protocolName
 		String protocolName = docInfo.getProtocolName();
 		FileProtocol protocol = null;
 		if (protocolName == null || protocolName.equals("")) {
 			protocol = Outliner.fileProtocolManager.getDefault();
 		} else {
 			protocol = Outliner.fileProtocolManager.getProtocol(protocolName);
 		}
 
 		// Open or Import the file, as is appropriate
 		if (! docInfo.isImported()) {
 			FileMenu.openFile(docInfo, protocol);
 		} else {
 			FileMenu.importFile(docInfo,protocol);
 		} // end if-else
 		
 	}
 	
 	
 	// Utility Functions
 	// TODO: these should be added to io in com.organic.maynard.jar
 	public static boolean writeObjectToFile(Object obj, String filename) {
 		try {
 			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filename));
 			stream.writeObject(obj);
 			stream.close();
 			return true;
 		} catch (IOException e) {
 			System.out.println("Exception: " + e.getMessage());
 			return false;
 		}
 	}
 
 	public static Object ReadObjectFromFile(String filename) {
 		Object obj = null;
 		
 		try {
 			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(filename));
 			obj = stream.readObject();
 			stream.close();
 			
 		} catch (OptionalDataException ode) {
 			System.out.println("Exception: " + ode);
 			
 		} catch (ClassNotFoundException cnfe) {
 			System.out.println("Exception: " + cnfe);
 			
 		} catch (FileNotFoundException fnfe) {
 			System.out.println("Exception: " + fnfe);
 			
 		} catch (StreamCorruptedException sce) {
 			System.out.println("Exception: " + sce);
 					
 		} catch (IOException ioe) {
 			System.out.println("Exception: " + ioe);
 					
 		}
 		
 		return obj;
 	}
 	
 	
 		private void reverseDisplayDirection () {
 		
 			// so we temporarily change the direction of the menu
 			switch (currentDisplayDirection){
 				
 			case TOP_TO_BOTTOM:
 				currentDisplayDirection = BOTTOM_TO_TOP ;
 				break ;
 				
 			case BOTTOM_TO_TOP:
 				currentDisplayDirection = TOP_TO_BOTTOM ;
 				break ;
 				
 			default:
 				break ;
 			} // end switch
 		} // end method reverseDisplayDirection
 			
 
 }
