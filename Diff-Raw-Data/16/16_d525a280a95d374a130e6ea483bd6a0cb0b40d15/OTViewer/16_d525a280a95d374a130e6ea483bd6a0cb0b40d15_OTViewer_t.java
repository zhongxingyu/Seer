 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.35 $
 * $Date: 2006-02-01 21:39:18 $
 * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.view.OTFrame;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerListener;
 import org.concord.framework.util.SimpleTreeNode;
 import org.concord.otrunk.OTMLToXHTMLConverter;
 import org.concord.otrunk.OTObjectServiceImpl;
 import org.concord.otrunk.OTStateRoot;
 import org.concord.otrunk.OTUserListService;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.xml.Exporter;
 import org.concord.otrunk.xml.XMLDatabase;
 import org.concord.swing.CCFileDialog;
 import org.concord.swing.CCFilenameFilter;
 import org.concord.swing.MostRecentFileDialog;
 import org.concord.swing.StreamRecord;
 import org.concord.swing.StreamRecordView;
 import org.concord.swing.util.Util;
 import org.concord.view.SimpleTreeModel;
 import org.concord.view.SwingUserMessageHandler;
 
 
 /**
  * OTViewer
  * Class name and description
  *
  * Date created: Dec 14, 2004
  *
  * @author scott<p>
  *
  */
 public class OTViewer extends JFrame
 	implements TreeSelectionListener, OTFrameManager,
 		OTViewContainerListener
 {
     /**
      * first version of this class
      */
     private static final long serialVersionUID = 1L;
     
     public final static String DEBUG_PROP = "otrunk.view.debug";
     public final static String TITLE_PROP = "otrunk.view.frame_title";
     public final static String SINGLE_USER_PROP = "otrunk.view.single_user";
     public final static String NO_USER_PROP = "otrunk.view.no_user";
         
     public final static int NO_USER_MODE = 0;
     public final static int SINGLE_USER_MODE = 1;        
     public final static int MULTIPLE_USER_MODE = 2;
     
     private static OTrunkImpl otrunk;
 	private static OTViewFactory otViewFactory;
 	
 	protected int userMode = 0;
 		
 	OTUserObject currentUser = null;
 	URL currentURL = null;
 	
     String baseFrameTitle = "OTrunk Viewer";
     
 	OTViewContainerPanel bodyPanel;
 	JTree folderTreeArea;
 	SimpleTreeModel folderTreeModel;
 	JTree dataTreeArea;
 	SimpleTreeModel dataTreeModel;
 	JSplitPane splitPane;
 	
     JFrame consoleFrame;
     
 	//Temp, to close the window
 	AbstractAction exitAction;
 	AbstractAction saveAsAction;
 	
 	JMenuBar menuBar;  
 	XMLDatabase xmlDB;
 	XMLDatabase userDataDB;
 	File currentAuthoredFile = null;
 	File currentUserFile = null;	
 	
 	Hashtable otContainers = new Hashtable();
 	
 	String startupMessage = "";
 	
 	boolean justStarted = true;
 	
 	boolean showTree = false;
     private AbstractAction saveUserDataAsAction;
     private AbstractAction saveUserDataAction;
     private AbstractAction debugAction;
     private AbstractAction showConsoleAction;
     private AbstractAction newUserDataAction;
     private AbstractAction loadUserDataAction;
     private AbstractAction loadAction;
     private AbstractAction saveAction;
     private AbstractAction exportImageAction;
     private AbstractAction exportHiResImageAction;
     private AbstractAction exportToHtmlAction;
 	
 	private JDialog commDialog;
 	
 	private boolean firstOpen = true;
     
     public static void setOTViewFactory(OTViewFactory factory)
 	{
 		otViewFactory = factory;
 	}
 		
 	public OTViewer(boolean showTree)
 	{
         super();
         
 		this.showTree = showTree;
         
         try {
             // this overrides the default base frame title
             String title = System.getProperty(TITLE_PROP,null);
             if(title != null) {
                 baseFrameTitle = title;
             }
         } catch (Throwable t) {
             // do nothing, just use the default title
         }
 
         setTitle(baseFrameTitle);
         
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		
 		addWindowListener( new WindowAdapter() {
 		    public void windowClosing(WindowEvent e)
 		    {
 		        ((OTViewer)e.getSource()).exit();
 		        
 		    }			
 		});				
 	
         consoleFrame = new JFrame("Console");
         StreamRecord record = new StreamRecord(10000);
         StreamRecordView view = new StreamRecordView(record);
         consoleFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         System.setOut((PrintStream) view.addOutputStream(System.out, "Console"));
         System.setErr((PrintStream) view.addOutputStream(System.err, System.out));
 
         consoleFrame.getContentPane().add(view);
         consoleFrame.setSize(800, 600);
         
 		commDialog = new JDialog(this, true);
 	}
 	
 	public void setUserMode(int mode)
 	{
 	    userMode = mode;
 	}
 	
 	public void updateTreePane()
 	{
 		Dimension minimumSize = new Dimension(100, 50);
         folderTreeArea = new JTree(folderTreeModel);
 
         // we are just disabling this however if we want to 
         // use this tree for authoring, or for managing student
         // created objects this will need to be some form of option
         folderTreeArea.setEditable(false);
         folderTreeArea.addTreeSelectionListener(this);
         
         JComponent leftComponent = null;
 
         JScrollPane folderTreeScrollPane = new JScrollPane(folderTreeArea);
 
         if(System.getProperty(DEBUG_PROP,"").equals("true")){
 	        //			ViewFactory.getComponent(root);
 	        
 	        dataTreeArea = new JTree(dataTreeModel);
 	        dataTreeArea.setEditable(false);
 	        dataTreeArea.addTreeSelectionListener(this);
 	        
 	        JScrollPane dataTreeScrollPane = new JScrollPane(dataTreeArea);
 	        
 	        JTabbedPane tabbedPane = new JTabbedPane();
 	        tabbedPane.add("Folders", folderTreeScrollPane);
 	        tabbedPane.add("Resources", dataTreeScrollPane);
 
 			//	Provide minimum sizes for the two components in the split pane
 			folderTreeScrollPane.setMinimumSize(minimumSize);
 			dataTreeScrollPane.setMinimumSize(minimumSize);
 			tabbedPane.setMinimumSize(minimumSize);			    
 
 	        leftComponent = tabbedPane;
 	    } else {
 	        leftComponent = folderTreeScrollPane;
 	    }
 	    
 		if(splitPane == null) {
 		    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 		            leftComponent, bodyPanel);
 		} else {
 		    splitPane.setLeftComponent(leftComponent);
 		}
 
 	    splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(200);
 
 	}
 	
 	public void initArgs(String [] args)	
 	{
 		if(args.length > 0) {
 			String urlStr = null;
 			
 			if(args[0].equals("-f")) {
 				if(args.length > 1) {
 					File inFile = new File(args[1]);
 					try {
 						URL url = inFile.toURL();
 						urlStr = url.toString();
 					} catch (Exception e) {
 						e.printStackTrace();
 						urlStr = null;
 					}
 				}
 			} else if(args[0].equals("-r")) {
 			    if(args.length > 1) {
 			        ClassLoader cl = OTViewer.class.getClassLoader();
 			        URL url = cl.getResource(args[1]);
 			        urlStr = url.toString();
 			    }
 			} else {
 				urlStr = args[0];
 			}
 			
 			init(urlStr);
 		} else {
 			init(null);
 		}
 
 	}
 	
 	public void init(String url)
 	{
 	    createActions();
 	    
 		updateMenuBar();
 				
 		setJMenuBar(menuBar);
 
 		bodyPanel = new OTViewContainerPanel(this, null);
 		
 		bodyPanel.addViewContainerListener(this);
 		
 		if(showTree) {
 		
 			dataTreeModel = new SimpleTreeModel();
 			
 			folderTreeModel = new SimpleTreeModel();		
 			
 			updateTreePane();
 						
 			getContentPane().add(splitPane);
 		} else {
 			getContentPane().add(bodyPanel);
 		}
 		
         SwingUtilities.invokeLater(new Runnable() {
             public void run() 
             {
 
             	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                 if(screenSize.width < 1000 || screenSize.height < 700) {
                     setVisible(true);
                     int state = getExtendedState();
                     
                     // Set the maximized bits
                     state |= Frame.MAXIMIZED_BOTH;
                     
                     // Maximize the frame
                     setExtendedState(state);
                 } else {
                     setBounds(100, 100, 875, 600);
                     setVisible(true);
                 }
                 
             }            
         });
 
         if(url != null) {
         	try {
         		loadURL(new URL(url));
         	} catch(Exception e) {
         		// FIXME: this should popup a dialog
         		System.err.println("Can't load url");
         		e.printStackTrace();
         		return;
         	}
         } 
         
         if(userMode == SINGLE_USER_MODE) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     instructionPanel();              
                 }
             });
         }
     }
 
 	private void loadUserDataFile(File file)
 	{
 	    currentUserFile = file;
 	    try {
 	        loadUserDataURL(file.toURL(), file.getName());
 	    } catch(Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 
 	private void loadUserDataURL(URL url, String name)
 		throws Exception
 	{
 	    userDataDB = new XMLDatabase(url);
         otrunk.setCreationDb(userDataDB);
         currentUser = otrunk.registerUserDataDatabase(userDataDB, name);
         
 	    reloadWindow();
 	}
 	
 	private void loadFile(File file)
 	{
 		currentAuthoredFile = file;
 		try {
 			loadURL(currentAuthoredFile.toURL());
 		} catch(Exception e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	private void loadURL(URL url)
 		throws Exception
 	{	    	    
 		xmlDB = new XMLDatabase(url, System.err);
 		
 		otrunk = new OTrunkImpl(xmlDB,
 				new Object [] {new SwingUserMessageHandler(this), new OTUserListService()});
 			
 		OTViewService viewService = 
 		    (OTViewService)otrunk.getService(OTViewService.class);
         
         OTViewFactory myViewFactory = null;
         if(viewService != null) {
             myViewFactory = viewService.getViewFactory(otrunk);
         }
 
 		if(myViewFactory != null) {
 		    otViewFactory = myViewFactory;
 		}
 		
 		bodyPanel.setOTViewFactory(otViewFactory);
 		
 		currentURL = url;
 
 		reloadWindow();
 	}
 	
 	private void reloadWindow()
 		throws Exception
 	{		
 		OTObject root = null;
 		boolean overrideShowTree = false;
 		
 		switch(userMode){
 		case NO_USER_MODE:
 		    root = otrunk.getRoot();
 			break;
 		case SINGLE_USER_MODE:
 		    if(userDataDB == null) {
 		        // no user file has been started yet
 		        overrideShowTree = true;
 		        root = otrunk.getFirstObjectNoUserData();
 		    } else {
 		        OTObject otRoot = otrunk.getRoot();
 			    root = otrunk.getUserRuntimeObject(otRoot, currentUser);			    		        
 		    }
 		}
 
 		if(showTree && !overrideShowTree) {
 		    OTDataObject rootDataObject = otrunk.getRootDataObject();
 			dataTreeModel.setRoot(new OTDataObjectNode("root", 
 					rootDataObject, otrunk));
 			
 			folderTreeModel.setRoot(new OTFolderNode(root));
 		}
 		
 		bodyPanel.setCurrentObject(root, null);
 		
 		if(showTree && !overrideShowTree) {
 			folderTreeModel.fireTreeStructureChanged((SimpleTreeNode)folderTreeModel.getRoot());
 			dataTreeModel.fireTreeStructureChanged((SimpleTreeNode)dataTreeModel.getRoot());
 		}
 		
 		Frame frame = (Frame)SwingUtilities.getRoot(this);
 		
 		switch(userMode) {
 		case NO_USER_MODE:
 		    frame.setTitle(baseFrameTitle + ": " + currentURL.toString());
 		    break;
 		case SINGLE_USER_MODE:
 			if(currentUserFile != null) {
 			    frame.setTitle(baseFrameTitle + ": " + currentUserFile.toString());
 			} else  if(userDataDB != null){
 			    frame.setTitle(baseFrameTitle + ": Untitled");
 			} else {
 			    frame.setTitle(baseFrameTitle);
 			}
 			break;
 		}
 		
 		saveUserDataAction.setEnabled(userDataDB != null);
 		saveUserDataAsAction.setEnabled(userDataDB != null);		
 	}
 		
 	public void reload()
 		throws Exception
 	{
 	    loadURL(currentURL);
 	}
 	
 	public OTUserObject createUser(String name, OTObjectService objService)
 		throws Exception
 	{
 	    OTUserObject user = (OTUserObject)objService.createObject(OTUserObject.class); 
 	    user.setName(name);
 	    return user;
 	}
 
 	
 	public void setCurrentUser(OTUserObject userObject)
 	{
 	    OTUserObject oldUser = currentUser;
 	    currentUser = userObject;
 	    if(!currentUser.equals(oldUser)) {
 	        try {
 	    		OTObject root = otrunk.getRoot();
 	    		if(currentUser != null) {
 	    		    root = otrunk.getUserRuntimeObject(root, currentUser);			    
 	    		}
 	    		
 	    		if(showTree) {
 	    			folderTreeModel.setRoot(new OTFolderNode(root));
 	    		}
 	    		
 	    		bodyPanel.setCurrentObject(root, null);
 
 	    		if(showTree) {
 	    			folderTreeModel.fireTreeStructureChanged((SimpleTreeNode)folderTreeModel.getRoot());
 	    			dataTreeModel.fireTreeStructureChanged((SimpleTreeNode)dataTreeModel.getRoot());
 	    		}
 	    		
 	    		userDataDB.setDirty(false);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	    }
 
 	}
 	
 	public static void main(String [] args)
 	{
         System.setProperty("apple.laf.useScreenMenuBar","true");
 
 		OTViewer viewer = new OTViewer(true);
 
 		if(Boolean.getBoolean(SINGLE_USER_PROP)) {
 			viewer.setUserMode(OTViewer.SINGLE_USER_MODE);
 		} else if(Boolean.getBoolean(NO_USER_PROP)) {
 			viewer.setUserMode(OTViewer.NO_USER_MODE);
 		}
 
 		viewer.initArgs(args);				
 	}
 	
 	class ExitAction extends AbstractAction
 	{
 		/**
          * nothing to serialize here.
          */
         private static final long serialVersionUID = 1L;
 
         public ExitAction()
 		{
 			super("Exit");			
 		}
 		
 		public void actionPerformed(ActionEvent e) 
 		{
 		    // If this suceeds then the VM will exit so
 		    // the window will get disposed		    
 		    exit();
 	    }
 	}
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.view.OTViewContainerListener#currentObjectChanged(org.concord.framework.otrunk.view.OTViewContainer)
      */
     public void currentObjectChanged(OTViewContainer container)
     {
         // TODO Auto-generated method stub
         OTObject currentObject = container.getCurrentObject();
         if(folderTreeArea != null) {
 			OTFolderNode node = (OTFolderNode)folderTreeArea.getLastSelectedPathComponent();
 			if(node == null) return;
             if(node.getPfObject() != currentObject) {
                 folderTreeArea.setSelectionPath(null);
             }
         }
     }
 	
 	/* (non-Javadoc)
 	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
 	 */
 	public void valueChanged(TreeSelectionEvent event) 
 	{	
 		if (event.getSource() == folderTreeArea) {
 			OTFolderNode node = (OTFolderNode)
 			folderTreeArea.getLastSelectedPathComponent();
 
 			if (node == null) return;
 
 			OTObject pfObject = node.getPfObject();
 
 			bodyPanel.setCurrentObject(pfObject, null);
 			
 			if(splitPane.getRightComponent() != bodyPanel){
 			    splitPane.setRightComponent(bodyPanel);
 			}
 		} else if (event.getSource() == dataTreeArea) {
 			SimpleTreeNode node = (SimpleTreeNode)
 				dataTreeArea.getLastSelectedPathComponent();
 			Object resourceValue = null;
 			if(node != null) {
 				resourceValue = node.getObject();				
 				if(resourceValue == null) {
 					resourceValue = "null resource value";
 				}
 			} else {
 				resourceValue = "no selected data object";
 			}
 			
             JComponent nodeView = null;
             if(resourceValue instanceof OTDataObject) {
                 nodeView = new OTDataObjectView((OTDataObject)resourceValue);
             } else {
                 nodeView = new JTextArea(resourceValue.toString());
             }
 			JScrollPane scrollPane = new JScrollPane(nodeView);
             
 			splitPane.setRightComponent(scrollPane);
 		}
 	}
 	
 	public void setFrameObject(OTObject otObject, OTFrame otFrame)
 	{
 		// look up view container with the frame.
 		OTViewContainerPanel otContainer = (OTViewContainerPanel)otContainers.get(otFrame.getGlobalId());
 		
 		if(otContainer == null) {
 			JFrame jFrame = new JFrame(otFrame.getTitle());
 
 			otContainer = new OTViewContainerPanel(this, jFrame);
 
 			otContainer.setOTViewFactory(otViewFactory);
 			
 			jFrame.getContentPane().setLayout(new BorderLayout());
 
 			jFrame.getContentPane().add(otContainer, BorderLayout.CENTER);
 			jFrame.setSize(otFrame.getWidth(), otFrame.getHeight());
 			
 			otContainers.put(otFrame.getGlobalId(), otContainer);
 		}
 		
 		// call setCurrentObject on that view container with a null
 		// frame
 		otContainer.setCurrentObject(otObject, null);
 		otContainer.showFrame();
 	}
 	
 	public void createActions()
 	{
 		newUserDataAction = new AbstractAction(){
 		    
 		    /**
              * nothing to serialize here
              */
             private static final long serialVersionUID = 1L;
 
             /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		    	createNewUser();
 		    }
 		    
 		};
 		newUserDataAction.putValue(Action.NAME, "New");			
 
 		loadUserDataAction = new AbstractAction(){
 		    /**
              * nothing to serialize here.  Just the parent class.
              */
             private static final long serialVersionUID = 1L;
 
             /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		    	openUserData();
 		    }
 		    
 		};
 		loadUserDataAction.putValue(Action.NAME, "Open...");		
 		
 		exportToHtmlAction = new AbstractAction() {
 			/**
              * nothing to serialize here
              */
             private static final long serialVersionUID = 1L;
 
             public void actionPerformed(ActionEvent arg0) {
 				File fileToSave = getReportFile();
                 OTMLToXHTMLConverter otxc = 
                 	new OTMLToXHTMLConverter(otViewFactory, bodyPanel);
                 otxc.setXHTMLParams(fileToSave, 800, 600);
                 
                 (new Thread(otxc)).start();
 			}
 		};
 		exportToHtmlAction.putValue(Action.NAME, "Export to html...");
 		exportToHtmlAction.setEnabled(false);
 		
 
 		    
 		saveUserDataAction = new AbstractAction(){
 		    
 		    /**
              * Nothing to serialize here
              */
             private static final long serialVersionUID = 1L;
 
             /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        if(currentUserFile == null || !currentUserFile.exists()){
 		            saveUserDataAsAction.actionPerformed(arg0);
 		            return;
 		        }
 		        
 		        if(currentUserFile.exists()){
 		            try {
 		                Exporter.export(currentUserFile, userDataDB.getRoot(), userDataDB);
 		                userDataDB.setDirty(false);
 		            } catch(Exception e){
 		                e.printStackTrace();
 		            }	                    	
 		        }
 		    }
 		};
 		saveUserDataAction.putValue(Action.NAME, "Save");					    
 		    		    
 		saveUserDataAsAction = new AbstractAction(){
 		    
 		    /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
             /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTViewer.this);
 		        CCFileDialog dialog = new CCFileDialog(frame, "Save As", CCFileDialog.SAVE);
 		        CCFilenameFilter filenameFilter = new CCFilenameFilter("otml");
 		        dialog.setFilenameFilter(filenameFilter);
 		        if(currentUserFile != null) {
 		            dialog.setDirectory(currentUserFile.getParentFile().getAbsolutePath());
 		            dialog.setFile(currentUserFile.getName());
 		        }
 		        dialog.show();
 		        
 		        String fileName = dialog.getFile();
 		        if(fileName == null) {
 		            return;
 		        }
 		        
 		        fileName = dialog.getDirectory() + fileName;
 		        currentUserFile = new File(fileName);
 		        
 		        if(!fileName.toLowerCase().endsWith(".otml")){
 		            currentUserFile = new File(currentUserFile.getAbsolutePath()+".otml");
 		        }
 		        if(!currentUserFile.exists() || checkForReplace(currentUserFile)){
 		            try {
 		                Exporter.export(currentUserFile, userDataDB.getRoot(), userDataDB);
 		                userDataDB.setDirty(false);
 		                setTitle(baseFrameTitle + ": " + currentUserFile.toString());
 		            } catch(Exception e){
 		                e.printStackTrace();
 		            }	                    	
 		        }				
 		    }
 		};
 		saveUserDataAsAction.putValue(Action.NAME, "Save As...");
 		
 		loadAction = new AbstractAction(){
 		    
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTViewer.this);
 		        
 		        CCFileDialog dialog = new CCFileDialog(frame, "Open", CCFileDialog.LOAD);
 		        CCFilenameFilter filenameFilter = new CCFilenameFilter("otml");
 		        dialog.setFilenameFilter(filenameFilter);
 		        if(currentAuthoredFile != null) {
 		            dialog.setDirectory(currentAuthoredFile.getParentFile().getAbsolutePath());
 		            dialog.setFile(currentAuthoredFile.getName());
 		        }
 		        dialog.show();
 		        
 		        String fileName = dialog.getFile();
 		        if(fileName == null) {
 		            return;
 		        }
 		        
 		        fileName = dialog.getDirectory() + fileName;
 		        System.out.println("load file name: " + fileName);
 		        loadFile(new File(fileName));					
 				exportToHtmlAction.setEnabled(true);
 		    }
 		    
 		};
 		loadAction.putValue(Action.NAME, "Open Authored Content...");			
 		    
 		saveAction = new AbstractAction(){
 		    
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        if(currentAuthoredFile == null){
 		            saveAsAction.actionPerformed(arg0);
 		            return;
 		        }
 		        
 		        if(!currentAuthoredFile.exists() || checkForReplace(currentAuthoredFile)){
 		            try {
 		                Exporter.export(currentAuthoredFile, xmlDB.getRoot(), xmlDB);
 		            } catch(Exception e){
 		                e.printStackTrace();
 		            }	                    	
 		        }
 		    }
 		    
 		};
 		saveAction.putValue(Action.NAME, "Save Authored Content...");			
 		    
 		saveAsAction = new AbstractAction(){
 		    
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTViewer.this);
 		        CCFileDialog dialog = new CCFileDialog(frame, "Save As", CCFileDialog.SAVE);
 		        CCFilenameFilter filenameFilter = new CCFilenameFilter("otml");
 		        dialog.setFilenameFilter(filenameFilter);
 		        if(currentAuthoredFile != null) {
 		            dialog.setDirectory(currentAuthoredFile.getParentFile().getAbsolutePath());
 		            dialog.setFile(currentAuthoredFile.getName());
 		        }
 		        dialog.show();
 		        
 		        String fileName = dialog.getFile();
 		        if(fileName == null) {
 		            return;
 		        }
 		        
 		        fileName = dialog.getDirectory() + fileName;
 		        currentAuthoredFile = new File(fileName);
 		        
 		        if(!fileName.toLowerCase().endsWith(".otml")){
 		            currentAuthoredFile = new File(currentAuthoredFile.getAbsolutePath()+".otml");
 		        }
 		        if(!currentAuthoredFile.exists() || checkForReplace(currentAuthoredFile)){
 		            try {
 		                Exporter.export(currentAuthoredFile, xmlDB.getRoot(), xmlDB);
 		            } catch(Exception e){
 		                e.printStackTrace();
 		            }	                    	
 		        }
 		        
 		        frame.setTitle(fileName);
 		    }
 		};
 		saveAsAction.putValue(Action.NAME, "Save Authored Content As...");			
 
         exportImageAction = new AbstractAction(){
             
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 
             public void actionPerformed(ActionEvent e)
             {
                 // this introduces a dependency on concord Swing project
                 // instead there needs to be a way to added these actions through
                 // the xml 
                 Component currentComp = bodyPanel.getCurrentComponent();
                 if(currentComp instanceof JScrollPane) {
                     currentComp = ((JScrollPane)currentComp).getViewport().getView();
                 }
                 Util.makeScreenShot(currentComp);
             }            
         };
         exportImageAction.putValue(Action.NAME, "Export Image...");          
         
         exportHiResImageAction = new AbstractAction(){
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 
             public void actionPerformed(ActionEvent e)
             {
                 Component currentComp = bodyPanel.getCurrentComponent();
                 if(currentComp instanceof JScrollPane) {
                     currentComp = ((JScrollPane)currentComp).getViewport().getView();
                 }
                 Util.makeScreenShot(currentComp, 2, 2);
             }                        
         };
         exportHiResImageAction.putValue(Action.NAME, "Export Hi Res Image...");          
         
 		debugAction = new AbstractAction(){
             
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 
 		    public void actionPerformed(ActionEvent e)
 		    {
 		        Object source = e.getSource();
 		        if(((JCheckBoxMenuItem)source).isSelected()){
 		            System.setProperty(DEBUG_PROP,"true");
 		        } else {
 		            System.setProperty(DEBUG_PROP,"false");
 		        }
 		        
 		        updateTreePane();
 		        SwingUtilities.invokeLater(new Runnable(){
 		            public void run()
 		            {
 				        updateMenuBar();		                
 		            }
 		        });
 				exportToHtmlAction.setEnabled(true);
 		    }		
 		};
 		debugAction.putValue(Action.NAME, "Debug Mode");
 		
         showConsoleAction = new AbstractAction(){
             
             /**
              * nothing to serizile here
              */
             private static final long serialVersionUID = 1L;
 
 
             public void actionPerformed(ActionEvent e)
             {
                 if(consoleFrame != null) {
                     consoleFrame.setVisible(true);
                 }
             }       
         };
         showConsoleAction.putValue(Action.NAME, "Show Console");
 
         exitAction = new ExitAction();
     
 	}
 	
 	/**
 	 * @return Returns the menuBar.
 	 */
 	public JMenuBar updateMenuBar()
 	{
 		/////////////////////////////////////////////////
 	    JMenu fileMenu = null;
 		if (menuBar == null){
 			menuBar = new JMenuBar();
 			fileMenu = new JMenu("File");
 			menuBar.add(fileMenu);			
 		} else {
 		    fileMenu = menuBar.getMenu(0);
 		    fileMenu.removeAll();
 		}
				
 		if(userMode == SINGLE_USER_MODE) {
            fileMenu.setEnabled(!justStarted);

            fileMenu.add(newUserDataAction);
 
 		    fileMenu.add(loadUserDataAction);
 		    
 		    fileMenu.add(saveUserDataAction);
 		    
 		    fileMenu.add(saveUserDataAsAction);
 		}
 		
 		if(Boolean.getBoolean(DEBUG_PROP)) {
 		    fileMenu.add(loadAction);
 		    
 		    fileMenu.add(saveAction);
 		    
 		    fileMenu.add(saveAsAction);
 		}
 	
         if(Boolean.getBoolean("otrunk.view.export_image")) {
             fileMenu.add(exportImageAction);
             
             fileMenu.add(exportHiResImageAction);
         }
         
 	    fileMenu.add(exportToHtmlAction);
 
         fileMenu.add(showConsoleAction);
         
 		JCheckBoxMenuItem debugItem = new JCheckBoxMenuItem(debugAction);
 		debugItem.setSelected(Boolean.getBoolean(DEBUG_PROP));
 		fileMenu.add(debugItem);
 		
 		fileMenu.add(exitAction);
 		
 		return menuBar;
 	}
 
 	boolean checkForReplace(File file){
         if(file == null || !file.exists()) return false;
         final Object[] options = { "Yes", "No" };
         return javax.swing.JOptionPane.showOptionDialog(null,
                   "The file '" + file.getName() +
                   "' already exists.  " +
                   "Replace existing file?",
                   "Warning",
                   javax.swing.JOptionPane.YES_NO_OPTION,
                   javax.swing.JOptionPane.WARNING_MESSAGE,
                   null,
                   options,
                   options[1]) == javax.swing.JOptionPane.YES_OPTION;
 
     }				
 	
 	/**
 	 * Checks if the user has unsaved work.  If they do then it prompts them to 
 	 * confirm what they are doing.  If they cancel then it returns false.
 	 *   
 	 * @return
 	 */
 	public boolean checkForUnsavedUserData()
 	{
 	    if(currentUser != null && userDataDB != null)
 	    {
 	        if(userDataDB.isDirty()) {
 	            // show dialog message telling them they haven't
 	            // saved their work
 	            // FIXME
 	            String options [] = {"Don't Save", "Cancel", "Save"};
 	            int chosenOption = JOptionPane.showOptionDialog(this, "Save Changes?", "Save Changes?",
 	                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
 	                    null, options, options[2]);
 	            switch(chosenOption) {
 	            case 0:
 	                System.err.println("Not saving work");
 	                break;
 	            case 1:
 	                System.err.println("Canceling close");
 	                return false;
 	            case 2:
 	                System.err.println("Saving work");
 	                saveUserDataAction.actionPerformed(null);
 	                break;
 	            }
 	        }			        
 	    }
 	    
 	    return true;
 	}
 
 	/**
 	 * This does not check for unsaved user data
 	 *
 	 */
 	public void newAnonUserData()
 	{
         // call some new method for creating a new un-saved user state
         // this should set the currentUserFile to null, so the save check prompts
         // for a file name
 		try {
 		    // need to make a brand new stateDB
 			userDataDB = new XMLDatabase();
 			//System.out.println("otrunk: " + otrunk + " userDatabase: " + userDataDB);
 			OTObjectService objService = otrunk.createObjectService(userDataDB);
 
 			OTStateRoot stateRoot = (OTStateRoot)objService.createObject(OTStateRoot.class);
 			userDataDB.setRoot(stateRoot.getGlobalId());
 			stateRoot.setFormatVersionString("1.0");		
 			userDataDB.setDirty(false);
 					    		    
 		    OTUserObject userObject = createUser("anon_single_user", objService);
             
             otrunk.initUserObjectService((OTObjectServiceImpl)objService, userObject, stateRoot);
             
 		    setCurrentUser(userObject);
 		    
 		    currentUserFile = null;
 		    
 			reloadWindow();
 
 		} catch (Exception e) {
 		    e.printStackTrace();
 		}
 
 	}
 	public boolean exit()
 	{
 		try {
 		    if(!checkForUnsavedUserData()) {
 		        // the user canceled the operation
 		        return false;
 		    }
 		    
 			if(otrunk != null) otrunk.close();
 		} catch (Exception exp) {
 			exp.printStackTrace();
 			// exit anyhow 
 		}
 		System.exit(0);
 		return true;
 	}
 	
 	public File getReportFile(){
 		File fileToSave;
 		CCFileDialog dialog = new CCFileDialog(new Frame(), "Save As html", CCFileDialog.SAVE);
 		CCFilenameFilter filenameFilter = new CCFilenameFilter("html");
 		dialog.setFilenameFilter(filenameFilter);
 		dialog.setMode(CCFileDialog.SAVE);
 		dialog.show();
 
 		String filestr = dialog.getDirectory() + dialog.getFile();
 
 		if(filestr == null) {
 			return null;
 		}
 		else {
 			fileToSave = new File(filestr);
             
 			if(!fileToSave.getName().toLowerCase().endsWith(".html")){
 				fileToSave = new File(fileToSave.getAbsolutePath()+".html");
             }
 
             if(!fileToSave.exists() || checkForReplace(fileToSave)){
             	return fileToSave;
             }
 		}
 
         return null;
 	}
 	
 	public void createNewUser() {
         if(!checkForUnsavedUserData()) {
             // the user canceled the operation
             return;
         }
         
         // call some new method for creating a new un-saved user state
         // this should set the currentUserFile to null, so the save check prompts
         // for a file name
         newAnonUserData();
 		exportToHtmlAction.setEnabled(true);
 		firstOpen = false;
 	}
 	
 	public void openUserData() {
         if(!checkForUnsavedUserData()) {
             // the user canceled the operation
             return;
         }
         
         Frame frame = (Frame)SwingUtilities.getRoot(OTViewer.this);
         
         MostRecentFileDialog mrfd = new MostRecentFileDialog("org.concord.otviewer.openotml");
         mrfd.setFilenameFilter("otml");
         
         int retval = mrfd.showOpenDialog(frame);
         
         File file = null;
         if(retval == MostRecentFileDialog.APPROVE_OPTION) {
         	file = mrfd.getSelectedFile();
         }
         
         if(file != null && file.exists()) {
         	loadUserDataFile(file);
         	exportToHtmlAction.setEnabled(true);
         	firstOpen = false;
         } 
 	}
 	
 	public void instructionPanel() {
 		commDialog.setResizable(false);
 		
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.WHITE);
 		panel.setLayout(null);
 		
 		JLabel lNew = new JLabel("Click the \"New\" button to create a new portfolio:");
 		JLabel lOpen = new JLabel("Click the \"Open\" button to open a saved portfolio:");
 		JButton bNew = new JButton("New");
 		JButton bOpen = new JButton("Open");
 		
 		panel.add(lNew);
 		panel.add(lOpen);
 		panel.add(bNew);
 		panel.add(bOpen);
 		
 		lNew.setLocation(50, 100);
 		lOpen.setLocation(50, 150);
 		bNew.setLocation(400, 100);
 		bOpen.setLocation(400, 150);
 		
 		lNew.setSize(340, 30);
 		lOpen.setSize(340, 30);
 		bNew.setSize(70, 25);
 		bOpen.setSize(70, 25);
 		
 		bNew.setOpaque(false);
 		bOpen.setOpaque(false);
 
 		bNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				commDialog.setVisible(false);
 				createNewUser();
 			}
 		});
 		
 		bOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				commDialog.setVisible(false);
 				openUserData();
 			}			
 		});
 		
 		commDialog.getContentPane().add(panel);
 		commDialog.setBounds(200, 200, 500, 300);
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 			    commDialog.show();
 			    justStarted = false;
 			    updateMenuBar();
 			}
 		});
 	}
 }  //  @jve:decl-index=0:visual-constraint="10,10"
 
 class HtmlFileFilter extends javax.swing.filechooser.FileFilter{
     public boolean accept(File f){
         if(f == null) return false;
         if (f.isDirectory())  return true;
 
         return (f.getName().toLowerCase().endsWith(".html"));
     }
     public String getDescription(){
         return "HTML files";
     }
     
 }  //  @jve:decl-index=0:visual-constraint="10,10"
