 package project.efg.client.impl.gui;
 
 /**
  * $Id: SynopticKeyTreeMain.java,v 1.1.1.1 2007/08/01 19:11:15 kasiedu Exp $
  * $Name:  $
  * 
  * Copyright (c) 2003  University of Massachusetts Boston
  *
  * Authors: Jacob K Asiedu
  *
  * This file is part of the UMB Electronic Field Guide.
  * UMB Electronic Field Guide is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2, or
  * (at your option) any later version.
  *
  * UMB Electronic Field Guide is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the UMB Electronic Field Guide; see the file COPYING.
  * If not, write to:
  * Free Software Foundation, Inc.
  * 59 Temple Place, Suite 330
  * Boston, MA 02111-1307
  * USA
  */
 /**
  * A temporary object used in some of the stack operations Should be extended to
  * implement equals and hashcode if it is used as part of a Collection.
  */
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 
 import project.efg.client.factory.gui.GUIFactory;
 import project.efg.client.factory.nogui.NoGUIFactory;
 import project.efg.client.interfaces.gui.CheckListener;
 import project.efg.client.interfaces.gui.DataManipulatorInterface;
 import project.efg.client.interfaces.gui.SynopticKeyTreeInterface;
 import project.efg.client.utils.gui.HelpEFG2ItemListener;
 import project.efg.client.utils.gui.PreferencesListener;
 import project.efg.client.utils.nogui.WorkspaceResources;
 import project.efg.util.interfaces.EFGImportConstants;
 import project.efg.util.utils.DBObject;
 
 /**
  * SynopticKeyTreeMain.java
  * 
  * 
  * Created: Sat Feb 18 16:41:14 2006
  * 
  * @author <a href="mailto:kasiedu@cs.umb.edu">Jacob K Asiedu</a>
  * @version 1.0
  */
 public class SynopticKeyTreeMain extends JDialog {
 
 
 	static final long serialVersionUID = 1;
 
 	DataManipulatorInterface deleteManipulator;
 
 	DataManipulatorInterface updateManipulator;
 
 	DataManipulatorInterface editManipulator;
 
 	private SynopticKeyTreeInterface tree;
 
 	// handle the metadata menu
 	final JMenuItem editMetadataMenu = new JMenuItem(EFGImportConstants.EFGProperties
 			.getProperty("SynopticKeyTreeMain.editMetadataBtn"));
 	final JMenuItem editTitleMenu = new JMenuItem(EFGImportConstants.EFGProperties
 			.getProperty("SynopticKeyTreeMain.updateBtn"));
 
 	final JMenuItem deleteMenu = new JMenuItem(EFGImportConstants.EFGProperties
 			.getProperty("SynopticKeyTreeMain.deleteBtn"));
 	final JMenuItem doneMenu = new JMenuItem(EFGImportConstants.EFGProperties
 			.getProperty("SynopticKeyTreeMain.doneBtn"));
 	final JPopupMenu popup = new JPopupMenu();
 
 	JFrame parentFrame;
 
 	private DBObject dbObject;
 
 	private String bkgdImageName;
 	private String isLinux = "windowsflavor";
 	
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(SynopticKeyTreeMain.class);
 		} catch (Exception ee) {
 		}
 	}
 
 	public SynopticKeyTreeMain(JFrame frame, boolean modal, DBObject db) {
 		this(frame, "", modal, db,null);
 	}
 
 	public SynopticKeyTreeMain(JFrame frame, String title, boolean modal,
 			DBObject dbObject, String bkgdImageName) {
 		super(frame, title, modal);
 		this.bkgdImageName= bkgdImageName;
 		this.parentFrame = frame;
 		this.isLinux = EFGImportConstants.EFGProperties.getProperty("efg2.system.os","windowsflavor");
 			setSize(new Dimension(400, 400));
 	
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				close();
 			}
 		});
 		this.dbObject = dbObject;
 	
 		String message = "";
 		try {
 	
 			this.tree = GUIFactory.getSynopticKeyTree(
 					this.dbObject, frame);
 		
 			this.tree.setRootVisible(false);
 			this.tree.setToolTipText("");
 			this.tree.addMouseListener(new EditMouseListener(this, this
 					.getEditManipulator()));
 	
 		} catch (Exception ee) {
 			ee.printStackTrace();
 			message = ee.getMessage();
 			log.error(message);
 			JOptionPane.showMessageDialog(this.parentFrame, message, "Error Message",
 					JOptionPane.ERROR_MESSAGE);
 		}
 		this.addMenus();
 	
 	
 	
 		JPanel jp = addPanel();
 		JScrollPane treePane = new JScrollPane();
 		(treePane.getViewport()).setOpaque(false);
 		treePane.getViewport().setView(jp);
 		
 		this.getContentPane().setLayout(new BorderLayout());
 		this.getContentPane().add(treePane, BorderLayout.CENTER);
 
 		this.setLocationRelativeTo(frame);
 	} // SynoptcKeyTreeMain constructor
 
 	public SynopticKeyTreeMain(DBObject db, JFrame frame, String mainTableName) {
 		this(frame, "", true, db,null);
 	}
 	private void addMenus(){
 		JMenu fileMenu = new JMenu("File");
 		JMenu helpMenu = new JMenu("Help");
 		if(this.isLinux.equalsIgnoreCase("islinuxflavor")){
             String property = 
             	EFGImportConstants.EFGProperties.getProperty(
             			"efg.data.last.file",null);
  
             if(property != null) {
             	String[] properties = {property};
             	properties = WorkspaceResources.convertURIToString(properties);
             	if(properties != null) {
             		property = properties[0];
             	}
             }
             else{
             	property = ".";
             }
 			//if it is a linux like thing show new import
 			final JMenuItem newLinuxMenu = new JMenuItem(EFGImportConstants.EFGProperties
 					.getProperty("new.linux.menu"));
 		
 			newLinuxMenu.addActionListener(
 					new NoDragDropHandler(
 							this.tree,
 							this.parentFrame,
 							property,
 							EFGImportConstants.EFGProperties.getProperty(
 									"efg.file.csv.message")));
 				
 			fileMenu.add(newLinuxMenu);	
 			fileMenu.addSeparator();
 		}
 
 		
 		//FileChooser
 		
 		editMetadataMenu.addActionListener(new DataManipulatorListener(this,
 				this.getEditManipulator()));
 		editMetadataMenu.setToolTipText(EFGImportConstants.EFGProperties
 				.getProperty("SynopticKeyTreeMain.editMetadataBtn.tooltip"));
 		fileMenu.add(editMetadataMenu);
 	
 		
 		
 		editTitleMenu.addActionListener(new DataManipulatorListener(this, this
 				.getUpdateManipulator()));
 		editTitleMenu.setToolTipText(EFGImportConstants.EFGProperties
 				.getProperty("SynopticKeyTreeMain.updateBtn.tooltip"));
 		fileMenu.add(editTitleMenu);
 		
 	
 		deleteMenu.addActionListener(new DataManipulatorListener(this, this
 				.getDeleteManipulator()));
 		deleteMenu.setToolTipText(EFGImportConstants.EFGProperties
 				.getProperty("SynopticKeyTreeMain.deleteBtn.tooltip"));
 		fileMenu.add(deleteMenu);
 	
 		
 		JMenu checkMenu = new JMenu("Check Data For Errors");
 		//sub menus
 		String propItem = 
 			EFGImportConstants.EFGProperties.getProperty(
 					"SynopticKeyTreeMain.checkimagesmenuitem","Check Images"
 					);
 		JMenuItem checkMediaMenu = new JMenuItem(propItem);
 		propItem = 
 			EFGImportConstants.EFGProperties.getProperty(
 					"SynopticKeyTreeMain.checkimagesmenuitem.tooltip",
 					"verify image files"
 					);
 		checkMediaMenu.setToolTipText(propItem);
 		checkMediaMenu.addActionListener(
 				new CheckListener(
 				this.dbObject,
 				this.tree, 
 				EFGImportConstants.MEDIARESOURCE));
 		
 		
 		checkMenu.add(checkMediaMenu);
 		JMenuItem checkIllegalCharactersMenu = new JMenuItem("Check IllegalCharacters");
 		checkIllegalCharactersMenu.setToolTipText("Check if illegal characters exists in data");
 		checkIllegalCharactersMenu.addActionListener(
 				new CheckListener(
 				this.dbObject,
 				this.tree, 
 				EFGImportConstants.ILLEGALCHARACTER_STRING));
 		//checkMenu.add(checkIllegalCharactersMenu);
 		fileMenu.addSeparator();
 		fileMenu.add(checkMenu);
 		fileMenu.addSeparator();
 		
 		JMenuItem preferencesMenu = 
 			new JMenuItem("Change/View Preferences");
 		preferencesMenu.addActionListener(new PreferencesListener(parentFrame, false, false));
 		fileMenu.add(preferencesMenu);
 		fileMenu.addSeparator();
 		
 		doneMenu.addActionListener(new DoneListener(this));
 		doneMenu.setToolTipText(EFGImportConstants.EFGProperties
 				.getProperty("SynopticKeyTreeMain.doneBtn.tooltip"));
 		fileMenu.add(doneMenu);
 	
 		
 		
 		
 		//JMenuItem closeMenu = new JMenuItem("Close");
 		JMenuItem helpItem = new JMenuItem("Help Contents");
 		helpItem.addActionListener(new HelpEFG2ItemListener(EFGImportConstants.KEYTREE_DEPLOY_HELP));
 		helpMenu.add(helpItem);
 		
 
 		JMenuBar mBar = new JMenuBar();
 		mBar.add(fileMenu);
 		mBar.add(helpMenu);
 	
 	
 		this.setJMenuBar(mBar);
 		
 		//add pop up menu
 		this.createPopUp();
 		
 	}
 	/**
 	 * @return
 	 */
 	private DataManipulatorInterface getEditManipulator() {
 		if (this.editManipulator == null) {
 			this.editManipulator = NoGUIFactory.getDataManipulatorInstance(
 					this.tree, EFGImportConstants.EFGProperties
 							.getProperty("editMetadataClass"));
 		}
 		return this.editManipulator;
 	}
 
 	public DataManipulatorInterface getUpdateManipulator() {
 		if (this.updateManipulator == null) {
 			this.updateManipulator =  NoGUIFactory.getDataManipulatorInstance(
 					this.tree, EFGImportConstants.EFGProperties
 							.getProperty("updateDataClass"));
 		}
 		return this.updateManipulator;
 	}
 
 	public DataManipulatorInterface getDeleteManipulator() {
 		if (this.deleteManipulator == null) {
 			this.deleteManipulator = NoGUIFactory.getDataManipulatorInstance(
 					this.tree, EFGImportConstants.EFGProperties
 							.getProperty("deleteDataClass"));
 		}
 		return this.deleteManipulator;
 	}
 
 	public void close() {
 
 		this.dispose();
 	}
 
 	private JPanel addPanel() {
 		
 		
 		JPanel iPanel = null;
 			//new ImagePanel(this.bkgdImageName);
 		
 		
 		if(this.isLinux.equalsIgnoreCase("islinuxflavor")){
 			iPanel = new JPanel();
 			
 		}
 		else{
 			iPanel = new ImagePanel(this.bkgdImageName);
 		}
 		iPanel.setLayout(new BorderLayout());
 		iPanel.add(this.tree,BorderLayout.CENTER);
 		iPanel.setBackground(Color.white);
 	
 		
 		this.tree.setOpaque(false);
 		iPanel.setOpaque(true);
 		return iPanel;
 	}
 
 
 
 
 	public void createPopUp() {
 
 		JMenuItem menuItem = new JMenuItem(this.editMetadataMenu.getText());
 		menuItem.addActionListener(new DataManipulatorListener(this, this
 				.getEditManipulator()));
 		this.popup.add(menuItem);
 
 		menuItem = new JMenuItem(this.editTitleMenu.getText());
 		menuItem.addActionListener(new DataManipulatorListener(this, this
 				.getUpdateManipulator()));
 		this.popup.add(menuItem);
 
 		menuItem = new JMenuItem(this.deleteMenu.getText());
 		menuItem.addActionListener(new DataManipulatorListener(this, this
 				.getDeleteManipulator()));
 		this.popup.add(menuItem);
 
 	}
 	/**
 	 * @param interface1
 	 */
 	public void processNode(DataManipulatorInterface manipulator) {
 
 		manipulator.processNode();
 	}
 
 	/**
 	 * 
 	 * @author kasiedu Edit Metadata table
 	 */
 	class DataManipulatorListener implements ActionListener {
 		private SynopticKeyTreeMain treeBrowser;
 
 		private DataManipulatorInterface manipulator;
 
 		public DataManipulatorListener(SynopticKeyTreeMain treeBrowser,
 				DataManipulatorInterface manipulator) {
 			this.treeBrowser = treeBrowser;
 			this.manipulator = manipulator;
 			
 		}
 
 		public void actionPerformed(ActionEvent evt) {
 			
 			this.treeBrowser.processNode(this.manipulator);
 
 		}
 	}
 
 	/**
 	 * 
 	 * @author kasiedu Done with changes
 	 */
 	class DoneListener implements ActionListener {
 		private SynopticKeyTreeMain treeBrowser;
 
 		public DoneListener(SynopticKeyTreeMain treeBrowser) {
 			this.treeBrowser = treeBrowser;
 			
 		}
 
 		public void actionPerformed(ActionEvent evt) {
 			this.treeBrowser.close();
 		}
 	}
 
 	class EditMouseListener extends MouseAdapter {
 
 		private SynopticKeyTreeMain treeBrowser;
 
 		private DataManipulatorInterface manipulator;
 
 		public EditMouseListener(SynopticKeyTreeMain treeBrowser,
 				DataManipulatorInterface manipulator) {
 			this.treeBrowser = treeBrowser;
 			this.manipulator = manipulator;
 		}
 
 		public void mousePressed(MouseEvent e) {
 			int selRow = tree.getRowForLocation(e.getX(), e.getY());
 
 			if (selRow != -1) {
 				if (e.getClickCount() == 2) {
 					
 					this.treeBrowser.processNode(this.manipulator);
 				}
 			}
 			if (e.isPopupTrigger()) {
 				showPopUp(e);
 			}
 		}
 
 		private void showPopUp(MouseEvent e) {
 			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
 
 			if (path != null) {
 
 				tree.getSelectionModel().setSelectionPath(path);
 				
 
				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				showPopUp(e);
 			}
 		}
 
 	}
 	/**
 	 * @author kasiedu
 	 *
 	 */
 	
 	class NoDragDropHandler implements ActionListener{
 		
 
 			private String previousFileLocation;
 			private String title;
 			private JFrame parent;
 			
 			private JTree tree;
 			/**
 			 * 
 			 */
 			public NoDragDropHandler(JTree tree,
 					JFrame parent,
 					String previousFileLocation, 
 					String title) {
 				this.tree = tree;
 				this.previousFileLocation = previousFileLocation;
 				this.title = title;
 				this.parent = parent;
 			}
 			/* (non-Javadoc)
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent e) {
 	            JFileChooser chooser = new JFileChooser();
 	            chooser.setFileHidingEnabled(false);
 	            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 	            chooser.setMultiSelectionEnabled(true);
 	            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
 	            chooser.setDialogTitle(this.title);
 	            chooser.setCurrentDirectory(new File(this.previousFileLocation));
 	            if (
 	                chooser.showOpenDialog(
 	                		this.parent)
 	                == JFileChooser.APPROVE_OPTION
 	                ) {
 	                File[] files = chooser.getSelectedFiles();
 	            	if(files != null && files.length > 0){
 	            		log.debug("Number Files Selected: " + files.length);
 	                		                	
 	            		List data = convertFilesToList(files);
 	            	
 	            			EFGImportConstants.EFGProperties.setProperty(
 	                				"efg.data.last.file",
 	                				WorkspaceResources.convertFileNameToURLString(
 	                						files[0].getParentFile().getAbsolutePath()));
 	            			HandleDataImport.handleImport((SynopticKeyTreeInterface)this.tree, data);
 	            	
 	             	}
 	            	else{
 	            		log.debug("No Files Selected");
 	            	}
 	            }
 				
 			}
 			/**
 			 * @param files
 			 * @return
 			 */
 			private List convertFilesToList(File[] files) {
 				List list = new ArrayList(files.length);
 				for (int i = 0; i < files.length; i++) {
 					log.debug("Adding File: " + files[i].getAbsolutePath() );
 					list.add(files[i]);
 				}
 				return list;
 			}
 	}
 
 
 } // SynopticKeyTreeMain
