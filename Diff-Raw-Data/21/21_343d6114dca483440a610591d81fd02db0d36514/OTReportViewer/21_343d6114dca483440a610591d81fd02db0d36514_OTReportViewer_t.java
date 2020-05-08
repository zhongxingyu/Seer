 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01741
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
  */
 
 /*
  * Last modification information:
 * $Revision: 1.2 $
 * $Date: 2005-07-07 16:52:08 $
 * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.view.OTFrame;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerListener;
 import org.concord.framework.util.SimpleTreeNode;
 import org.concord.otrunk.OTStateRoot;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.user.OTReferenceMap;
 import org.concord.otrunk.user.OTTemplateDatabase;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.xml.Exporter;
 import org.concord.otrunk.xml.XMLDatabase;
 import org.concord.otrunk.VirtualFolder;
 import org.concord.view.SimpleTreeModel;
 import org.concord.view.SwingUserMessageHandler;
 
 
 /**
  * OTReportViewer
  * Class name and description
  *
  *
  * @author shengyao<p>
  *
  */
 public class OTReportViewer extends JFrame
 	implements TreeSelectionListener, OTFrameManager,
 		OTViewContainerListener
 {
     public final static int NO_USER_MODE = 0;
     public final static int SINGLE_USER_MODE = 1;        
     public final static int MULTIPLE_USER_MODE = 2;
     
     private static OTrunkImpl otrunk;
 	private static OTViewFactory otViewFactory;
 	
 	protected int userMode = 2;
 	
 	final JFileChooser fc = new JFileChooser();
 	
 	/**
 	 * users in the otml file
 	 */
 	private Vector users = new Vector();
 	private URL usersURL = null;
 	private File usersDirectory;
 	private int numberOfUsers;
 	
 	JTree rootTree = new JTree();
 	
 	OTUserObject currentUser = null;
 	URL currentURL = null;
 	
 	OTViewContainerPanel bodyPanel;
 	JTree folderTreeArea;
 	SimpleTreeModel folderTreeModel;
 	JTree dataTreeArea;
 	SimpleTreeModel dataTreeModel;
 	JSplitPane splitPane;
 	
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
 	
 	boolean showTree = false;
     private AbstractAction saveUserDataAsAction;
     private AbstractAction saveUserDataAction;
     private AbstractAction debugAction;
     private AbstractAction newUserDataAction;
     private AbstractAction loadUserDataAction;
     private AbstractAction loadAction;
     private AbstractAction saveAction;
 	
     public static void setOTViewFactory(OTViewFactory factory)
 	{
 		otViewFactory = factory;
 	}
 		
 	public OTReportViewer(boolean showTree)
 	{
 		super("OTrunk Viewer");
 		this.showTree = showTree;
 
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		
 		addWindowListener( new WindowAdapter() {
 		    public void windowClosing(WindowEvent e)
 		    {
 		        ((OTReportViewer)e.getSource()).exit();
 		        
 		    }			
 		});				
 	}
 	
 	public void setUserMode(int mode)
 	{
 	    userMode = mode;
 	}
 	
 	public void updateTreePane()
 	{
 		if(userMode == MULTIPLE_USER_MODE) {
 			VirtualFolder vFolder = new VirtualFolder();
 			
 			VirtualFolder userRoots = new VirtualFolder();
 			userRoots.setName("User Roots");
 			//System.out.println("start update tree pane: " + numberOfUsers);
 			if(numberOfUsers > 0) {
 				Vector users = otrunk.getUsers();
 				for(int i = 0; i < numberOfUsers; i++) {
 					try {
 						OTObject root = otrunk.getRoot();
 						OTUser userObject = (OTUser) users.elementAt(i);
 						System.out.println("user " + i + ": " + userObject.getName());
 						
 					    OTObject userPfObject = 
 					    	otrunk.getUserRuntimeObject(root, userObject);
 						userRoots.addVirtualChild(userPfObject);
 					} catch(Exception exp) {
 						exp.printStackTrace();
 					}
 
 				}
 				//System.out.println("userroots: " + userRoots.getChildCount());
 				vFolder.addVirtualChild(userRoots);
 				//System.out.println("vFolder: " + vFolder.getChildCount());
 				vFolder.setName("System");
 				OTFolderNode folderNode = new OTFolderNode(vFolder);
 				//System.out.println("OTFolderNode: " + folderNode.getChildCount());
 				folderTreeModel.setRoot(folderNode);
 				//System.out.println("folder tree model root: " + folderTreeModel.getRoot());
 				folderTreeArea = new JTree(folderTreeModel);
 		        folderTreeArea.setEditable(true);
 		        folderTreeArea.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		        folderTreeArea.addTreeSelectionListener(this);
 				//System.out.println("folder tree area: " + folderTreeArea.getRowCount());
 
 				/*Hashtable usersHT = otrunk.getUserTemplateDatabases();
 				Enumeration userNames = usersHT.keys();
 				//folderTreeArea.removeAll();
 				try {
 					while(userNames.hasMoreElements()) {
 						OTObject root = otrunk.getRoot();
 						OTTemplateDatabase tempDB = 
 							(OTTemplateDatabase) usersHT.get(userNames.nextElement());
 						System.out.println(tempDB.toString());
 						
 						OTStateRoot stateRoot = 
 							(OTStateRoot)otrunk.getRootObject(tempDB);
 						System.out.println(stateRoot.toString());
 						
 					    OTObjectMap userMap = stateRoot.getUserMap();
 					    Vector keys = userMap.getObjectKeys();
 					    OTReferenceMap refMap = 
 					    	(OTReferenceMap)userMap.getObject((String)keys.get(0));
 					    
 					    OTUserObject userObject = refMap.getUser();
 					    OTObject userPfObject = 
 					    	otrunk.getUserRuntimeObject(root, userObject);
 					    tempDB.close();
 
 						userRoots.addVirtualChild(userPfObject);
 						System.out.println("             " + userObject.getName());
 					}
 					vFolder.addVirtualChild(userRoots);
 					vFolder.setName("System");
 					OTFolderNode folderNode = new OTFolderNode(vFolder);
 					folderTreeModel.setRoot(folderNode);
 					folderTreeArea = new JTree(folderTreeModel);
 				} catch (Exception exp) {
 					exp.printStackTrace();
 				}
 				*/
 
 			}	
 			JScrollPane folderTreeScrollPane = new JScrollPane(folderTreeArea);
 			
 			if(splitPane == null){
 			    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 			    		folderTreeScrollPane, bodyPanel);
 			} else {
 			    splitPane.setLeftComponent(folderTreeScrollPane);
 			    splitPane.setRightComponent(bodyPanel);
 			}
 			
 			splitPane.setOneTouchExpandable(true);
 			splitPane.setDividerLocation(200);
 			
 		} else {
 			
 			Dimension minimumSize = new Dimension(100, 50);
 		    JComponent leftComponent = null;
 	        folderTreeArea = new JTree(folderTreeModel);
 	        folderTreeArea.setEditable(true);
 	        folderTreeArea.addTreeSelectionListener(this);
 	        
 	        JScrollPane folderTreeScrollPane = new JScrollPane(folderTreeArea);
 	
 	        if(System.getProperty("otrunk.view.debug","").equals("true")){
 		        //			ViewFactory.getComponent(root);
 		        
 		        dataTreeArea = new JTree(dataTreeModel);
 		        dataTreeArea.setEditable(true);
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
 		    
 			if(splitPane == null){
 			    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 			            leftComponent, bodyPanel);
 			} else {
 			    splitPane.setLeftComponent(leftComponent);
 			}
 			splitPane.setOneTouchExpandable(true);
 			splitPane.setDividerLocation(200);
 		}
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
 			        ClassLoader cl = OTReportViewer.class.getClassLoader();
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
 			if(splitPane != null) getContentPane().add(splitPane);
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
         
     }
 
 	private void loadUserDataFile(File file)
 	{
 	    currentUserFile = file;
 	    try {
 	        loadUserDataURL(file.toURL());
 	    } catch(Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 
 	private void loadUserDataURL(URL url)
 		throws Exception
 	{
 	    userDataDB = new XMLDatabase(url);
 	    //System.out.println("User root " + userDataDB.getRoot());
 	    otrunk.registerUserDataDatabase(userDataDB);
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
 				new Object [] {new SwingUserMessageHandler(this)});
 			
 		OTViewFactory myViewFactory = 
 		    (OTViewFactory)otrunk.getService(OTViewFactory.class);
 		if(myViewFactory != null) {
 		    otViewFactory = myViewFactory;
 		}
 		
 		bodyPanel.setOTViewFactory(otViewFactory);
 		
 		currentURL = url;
 
 		reloadWindow();
 	}
 	
 	private void reloadWindow() throws Exception
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
 			break;
 		case MULTIPLE_USER_MODE:
 			root = otrunk.getRoot();
 		    /*if(userDataDB == null) {
 		        // no user file has been started yet
 		        overrideShowTree = true;
 		        root = otrunk.getFirstObjectNoUserData();
 		    } else {
 		        OTObject otRoot = otrunk.getRoot();
 			    root = otrunk.getUserRuntimeObject(otRoot, currentUser);
 		    }*/
 			updateTreePane();
 			break;
 		}
 
 		/*
 		if(showTree && !overrideShowTree) {
 			//DefaultMutableTreeNode top =
 	        //    new DefaultMutableTreeNode(usersDirectory.getName());
 			//if(numberOfUsers > 0) {
 				//Hashtable usersName
 			//}
 			
 
 			OTDataObject rootDataObject = otrunk.getRootDataObject();
 			dataTreeModel.setRoot(new OTDataObjectNode("root", 
 					otrunk.getRootDataObject(), otrunk));
 			
 			folderTreeModel.setRoot(new OTFolderNode(root));
 		}
 		
 		bodyPanel.setCurrentObject(root, null);
 		
 		if(showTree && !overrideShowTree) {
 			folderTreeModel.fireTreeStructureChanged((SimpleTreeNode)folderTreeModel.getRoot());
 			dataTreeModel.fireTreeStructureChanged((SimpleTreeNode)dataTreeModel.getRoot());
 		}
 		*/
 		Frame frame = (Frame)SwingUtilities.getRoot(this);
 		
 		switch(userMode) {
 		case NO_USER_MODE:
 		    frame.setTitle("CCPortfolio: " + currentURL.toString());
 		    break;
 		case SINGLE_USER_MODE:
 			if(currentUserFile != null) {
 			    frame.setTitle("CCPortfolio: " + currentUserFile.toString());
 			} else  if(userDataDB != null){
 			    frame.setTitle("CCPortfolio: Untitled");
 			} else {
 			    frame.setTitle("CCPortfolio");
 			}
 			break;
 		case MULTIPLE_USER_MODE:
 		    frame.setTitle("CCPortfolio: " + currentURL.toString());
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
 	
 	public OTUserObject createUser(String name)
 		throws Exception
 	{
 	    OTUserObject user = (OTUserObject)otrunk.createObject(OTUserObject.class); 
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
 	
 	public int getUserMode() {
 		return userMode;
 	}
 	
 	public static void main(String [] args)
 	{
 		OTReportViewer viewer = new OTReportViewer(true);
 
 		if(Boolean.getBoolean("otrunk.view.multiple_user")) {
 			viewer.setUserMode(OTReportViewer.MULTIPLE_USER_MODE);
 		}		
 		
 		//System.out.println("user mode: " + viewer.getUserMode());
 
 		viewer.initArgs(args);				
 	}
 	
 	class ExitAction extends AbstractAction
 	{
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
 		//System.out.print("selection changed to ");
 		if (event.getSource() == folderTreeArea) {
 			if(folderTreeArea.getSelectionRows()[0] < 2) return;
 			OTFolderNode node = (OTFolderNode)
 			folderTreeArea.getLastSelectedPathComponent();
 			//System.out.println(node.getChildCount());
 
 			if (node == null) return;
 
 			OTObject pfObject = node.getPfObject();
 			System.out.println("pfObject " + pfObject.getName());
 
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
 			
 			JTextArea textArea = new JTextArea(resourceValue.toString());
 			
 			splitPane.setRightComponent(textArea);
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
 		    
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        if(!checkForUnsavedUserData()) {
 		            // the user canceled the operation
 		            return;
 		        }
 		        
 		        // call some new method for creating a new un-saved user state
 		        // this should set the currentUserFile to null, so the save check prompts
 		        // for a file name
 		        newAnonUserData();
 		    }
 		    
 		};
 		newUserDataAction.putValue(Action.NAME, "New");			
 
 		loadUserDataAction = new AbstractAction(){
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        if(!checkForUnsavedUserData()) {
 		            // the user canceled the operation
 		            //return;
 		        }
 		        
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTReportViewer.this);
 		   
 		        if(userMode == MULTIPLE_USER_MODE) {
 		        	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			        int returnVal = fc.showOpenDialog(frame);
 			        if (returnVal == JFileChooser.APPROVE_OPTION) {
 			            usersDirectory = fc.getSelectedFile();
 			            if(usersDirectory == null) return;
 			            
 			            File[] files = usersDirectory.listFiles();
 		            	//System.out.println("files in the directory:");
 		            	numberOfUsers = 0;
 			            for(int i = 0; i < files.length; i++) {
 			            	if(isFileAcceptable(files[i])) {
 				            	//System.out.println(files[i].getName());
 			            		loadUserDataFile(files[i]);
 			            		numberOfUsers ++;
 			            	}
 			            }
 			        }
 			        try {
 				        reloadWindow();			        	
 			        } catch (Exception exp) {
 			        	exp.printStackTrace();
 			        }
 		        } else {
 			        FileDialog dialog = new FileDialog(frame, "Open", FileDialog.LOAD);
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
 			        System.out.println("load file name: " + fileName);	
 			        loadUserDataFile(new File(fileName));					
 		        }
 		    }
 		};
 		loadUserDataAction.putValue(Action.NAME, "Open...");			
 		    
 		saveUserDataAction = new AbstractAction(){
 		    
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
 		    
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTReportViewer.this);
 		        FileDialog dialog = new FileDialog(frame, "Save As", FileDialog.SAVE);
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
 		                setTitle("CCPortfolio: " + currentUserFile.toString());
 		            } catch(Exception e){
 		                e.printStackTrace();
 		            }	                    	
 		        }				
 		    }
 		};
 		saveUserDataAsAction.putValue(Action.NAME, "Save As...");
 		
 		loadAction = new AbstractAction(){
 		    
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTReportViewer.this);
 		        
 		        FileDialog dialog = new FileDialog(frame, "Open", FileDialog.LOAD);
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
 		    }
 		    
 		};
 		loadAction.putValue(Action.NAME, "Open Authored Content...");			
 		    
 		saveAction = new AbstractAction(){
 		    
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
 		    
 		    /* (non-Javadoc)
 		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		     */
 		    public void actionPerformed(ActionEvent arg0)
 		    {
 		        Frame frame = (Frame)SwingUtilities.getRoot(OTReportViewer.this);
 		        FileDialog dialog = new FileDialog(frame, "Save As", FileDialog.SAVE);
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
 
 		debugAction = new AbstractAction(){
 		    public void actionPerformed(ActionEvent e)
 		    {
 		        Object source = e.getSource();
 		        if(((JCheckBoxMenuItem)source).isSelected()){
 		            System.setProperty("otrunk.view.debug","true");
 		        } else {
 		            System.setProperty("otrunk.view.debug","false");
 		        }
 		        
 		        updateTreePane();
 		        SwingUtilities.invokeLater(new Runnable(){
 		            public void run()
 		            {
 				        updateMenuBar();		                
 		            }
 		        });
 		    }		
 		};
 		debugAction.putValue(Action.NAME, "Debug Mode");
 		
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
 		    fileMenu.add(newUserDataAction);
 
 		    fileMenu.add(loadUserDataAction);
 		    
 		    fileMenu.add(saveUserDataAction);
 		    
 		    fileMenu.add(saveUserDataAsAction);
 		} else if(userMode == MULTIPLE_USER_MODE) {
 		    fileMenu.add(loadUserDataAction);			
 		}
 		
 		if(Boolean.getBoolean("otrunk.view.debug")) {
 		    fileMenu.add(loadAction);
 		    
 		    fileMenu.add(saveAction);
 		    
 		    fileMenu.add(saveAsAction);
 		}
 		
 		JCheckBoxMenuItem debugItem = new JCheckBoxMenuItem(debugAction);
 		debugItem.setSelected(Boolean.getBoolean("otrunk.view.debug"));
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
 			otrunk.setCreationDb(userDataDB);
 			OTStateRoot stateRoot = (OTStateRoot)otrunk.createObject(OTStateRoot.class);
 			userDataDB.setRoot(stateRoot.getGlobalId());
 			stateRoot.setFormatVersionString("1.0");		
 			userDataDB.setDirty(false);
 			
 		    
 		    
 		    OTUserObject userObject = createUser("anon_single_user");
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
 		    
 			otrunk.close();
 		} catch (Exception exp) {
 			exp.printStackTrace();
 			// exit anyhow 
 		}
 		System.exit(0);
 		return true;
 	}
 	
 	private boolean isFileAcceptable(File file) {
 		if(file.isDirectory())
 			return false;
 		String ext = getExtension(file);
 		if(ext == null)
 			return false;
 		if(ext.equalsIgnoreCase("otml"))
 			return true;
 		return false;
 	}
 	
     private static String getExtension(File f) {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');
 
         if (i > 0 &&  i < s.length() - 1) {
             ext = s.substring(i+1).toLowerCase();
         }
         return ext;
     }
 }  //  @jve:decl-index=0:visual-constraint="10,10"
