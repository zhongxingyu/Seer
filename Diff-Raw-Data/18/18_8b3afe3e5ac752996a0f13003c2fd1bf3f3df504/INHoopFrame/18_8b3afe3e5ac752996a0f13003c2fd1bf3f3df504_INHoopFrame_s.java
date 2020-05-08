 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  * This source set uses code written for other various graduate 
  * courses and is part of a larger research effort in the field of
  * interactive narrative (IN). Courses that have contribute
  * to this source base are:
  * 
  * 	05-834 Applied Machine Learning
  *  11-719 Computational Models of Discourse Analysis
  *  11-741 Information Retrieval
  * 
  */
 
 package edu.cmu.cs.in.hoop;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.border.Border;
 
 import org.apache.hadoop.util.VersionInfo;
 
 import edu.cmu.cs.in.INHoopMessageHandler;
 import edu.cmu.cs.in.base.INFileManager;
 import edu.cmu.cs.in.base.INLink;
//import edu.cmu.cs.in.controls.INJFeatureList;
 import edu.cmu.cs.in.controls.base.INJFrame;
 import edu.cmu.cs.in.controls.INScatterPlot;
 import edu.cmu.cs.in.network.INSocketServerBase;
 import edu.cmu.cs.in.stats.INStatistics;
 
import java.awt.Container;
//import java.util.Arrays;
//import java.util.List;
//import javax.swing.JButton;
import org.jdesktop.swingx.MultiSplitLayout;
//import org.jdesktop.swingx.MultiSplitLayout.Leaf;
//import org.jdesktop.swingx.MultiSplitLayout.Divider;
//import org.jdesktop.swingx.MultiSplitLayout.Split;
import org.jdesktop.swingx.MultiSplitPane;

 /** 
  * @author vvelsen
  *
  */
 public class INHoopFrame extends INJFrame implements ActionListener
 {
 	private static final long serialVersionUID = 2788834485599638868L;
 	
 	JMenuBar menuBar;
     JDesktopPane desktop;
     JInternalFrame toolPalette;
     JCheckBoxMenuItem showToolPaletteMenuItem;
     
     static final private String PREVIOUS = "previous";
     static final private String UP = "up";
     static final private String NEXT = "next";
 
     static final Integer DOCLAYER = new Integer(5);
     static final Integer TOOLLAYER = new Integer(6);
     static final Integer HELPLAYER = new Integer(7);
 
     static final String ABOUTMSG = "Hoop is an interactive text exploration tool written with the express\n purpose of understanding narrative structures in written form.";
 	    
 	private INSocketServerBase server=null;
 	private INHoopMessageHandler handler=null;
 	private INStatistics stats=null;
 	private INScatterPlot plotter=null; 		
 	private INHoopJobList jobListWindow=null;
 	private INHoopStopWordEditor stopListEditor=null;
 	private INHoopVocabularyEditor vocabularyEditor=null;	
 	private	INHoopStatusBar statusbar=null;
 	private	INHoopEditor hoopEditor=null;	
 	private INHoopConsole console=null;
 	
 	private INHoopTabDraggable left=null;
 	private INHoopTabDraggable right=null;
 	private INHoopTabDraggable center=null;
 	private INHoopTabDraggable bottom=null;
 		
 	/**
 	 *
 	 */	
     public INHoopFrame() 
     {
         super ("Hoop");
         
 		this.setTitle ("Hoop");
                 
         final int inset=50;
         Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
         setBounds (inset,inset,screenSize.width-inset*2, screenSize.height-inset*2);
 	    
 	    loadImageIcons ();
 		
 		INLink.fManager=new INFileManager ();
 		INLink.posFiles=new ArrayList<String> ();
                 
         buildContent();
         buildMenus();
         
         this.addWindowListener(new WindowAdapter() 
         {
         	public void windowClosing(WindowEvent e) 
         	{
         		quit();
         	}
         });                             
     }
 	/**
 	 *
 	 */    
     private void loadImageIcons ()
     {
     	debug ("loadImageIcons ()");
     	
     	java.net.URL imgURL=null;
     	String loadPath="";
     
 	    imgURL=getClass().getResource("/assets/images/machine.png");
 	    if (imgURL!=null) 
 	    {
 	        INLink.icon=new ImageIcon(imgURL,"Machine");
 	    } 
 	    else 
 	    	debug ("Unable to load image ("+imgURL+") icon from jar");
 	    
 	    java.net.URL linkURL=getClass().getResource("/assets/images/link.jpg");
 	    if (linkURL!=null) 
 	    {
 	        INLink.linkIcon=new ImageIcon(linkURL,"Link");
 	    } 
 	    else 
 	    	debug ("Unable to load image ("+linkURL+") icon from jar");
 	    
 	    java.net.URL unlinkURL=getClass().getResource("/assets/images/broken.jpg");
 	    if (unlinkURL!=null) 
 	    {
 	        INLink.unlinkIcon=new ImageIcon(unlinkURL,"Unlink");
 	    } 
 	    else 
 	    	debug ("Unable to load image ("+unlinkURL+") icon from jar");	    	
     	
     	INLink.imageIcons=new ImageIcon [INLink.imgURLs.length];
     	
     	for (int i=0;i<INLink.imgURLs.length;i++)
     	{
     		loadPath="/assets/images/"+INLink.imgURLs [i];
     	    imgURL=getClass().getResource(loadPath);
 
     	    if (imgURL!=null) 
     	    {
     	    	INLink.imageIcons [i]=new ImageIcon(imgURL,INLink.imgURLs [i]);
     	    	debug ("Loaded: " + loadPath);
     	    } 
     	    else 
     	    	debug ("Unable to load image ("+loadPath+") icon from jar");    		
     	}
     }
 	/**
 	 *
 	 */	
     protected void buildMenus() 
     {
         menuBar = new JMenuBar();
         menuBar.setOpaque(true);
         
         JMenu file = buildFileMenu();
         JMenu edit = buildEditMenu();
         JMenu views = buildViewsMenu();
         JMenu tools = buildToolsMenu();
         JMenu speed = buildSpeedMenu();
         JMenu help = buildHelpMenu();
         
         menuBar.add(file);
         menuBar.add(edit);
         menuBar.add(tools);
         menuBar.add(views);
         menuBar.add(speed);
         menuBar.add(help);
         
         setJMenuBar(menuBar);	
     }
 	/**
 	 *
 	 */	
     protected JMenu buildFileMenu() 
     {
     	JMenu file = new JMenu("File");
     	JMenuItem newWin = new JMenuItem("New");
     	JMenuItem open = new JMenuItem("Open");
     	JMenuItem quit = new JMenuItem("Quit");
 
     	newWin.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			// Fill in later
     		}
     	});
 
     	open.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			// Fill in later
     		}
     	});
 
     	quit.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			quit();
     		}
     	});
 
     	file.add(newWin);
     	file.add(open);
     	file.addSeparator();
     	file.add(quit);
     	return file;
     }
 	/**
 	 *
 	 */	
     protected JMenu buildEditMenu() 
     {
     	JMenu edit = new JMenu("Edit");
     	JMenuItem undo = new JMenuItem("Undo");
     	JMenuItem copy = new JMenuItem("Copy");
     	JMenuItem cut = new JMenuItem("Cut");
     	JMenuItem paste = new JMenuItem("Paste");
     	JMenuItem prefs = new JMenuItem("Preferences...");
 
     	undo.setEnabled(false);
     	copy.setEnabled(false);
     	cut.setEnabled(false);
     	paste.setEnabled(false);
 
     	prefs.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			openPrefsWindow();
     		}
     	});
 
     	edit.add(undo);
     	edit.addSeparator();
     	edit.add(cut);
     	edit.add(copy);
     	edit.add(paste);
     	edit.addSeparator();
     	edit.add(prefs);
     	return edit;
     }
 	/**
 	 *
 	 */	
     protected JMenu buildViewsMenu() 
     {
     	JMenu views = new JMenu("Views");
 
     	JMenuItem documentItem = new JMenuItem("Document Viewer");
 
     	documentItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     	    	JInternalFrame viewer = new INHoopDocumentViewer();
     	    	desktop.add (viewer, DOCLAYER);
     	    	try 
     	    	{ 
     	    		viewer.setVisible(true);
     	    		viewer.setSelected(true); 
     	    	} 
     	    	catch (java.beans.PropertyVetoException e2) 
     	    	{
     	    		
     	    	}
     		}
     	});
     	
     	JMenuItem documentListItem=new JMenuItem("Document Set Viewer");    	
     	
     	documentListItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     	    	JInternalFrame viewer=new INHoopDocumentList();
     	    	desktop.add (viewer, DOCLAYER);
     	    	try 
     	    	{ 
     	    		viewer.setVisible(true);
     	    		viewer.setSelected(true); 
     	    	} 
     	    	catch (java.beans.PropertyVetoException e2) 
     	    	{
     	    		
     	    	}
     		}
     	});    	
     	
     	JMenuItem consoleItem=new JMenuItem("Console Output");    	
     	
     	consoleItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     	    	console=new INHoopConsole();
     	    	desktop.add (console, DOCLAYER);
     	    	try 
     	    	{ 
     	    		console.setVisible(true);
     	    		console.setSelected(true); 
     	    	} 
     	    	catch (java.beans.PropertyVetoException e2) 
     	    	{
     	    		
     	    	}
     		}
     	});    	    	
     	
     	views.add (documentItem);
     	views.add (documentListItem);
     	views.add (consoleItem);
     	
     	return (views);
     }
 	/**
 	 *
 	 */	
     protected JMenu buildToolsMenu() 
     {
     	JMenu tools = new JMenu("Tools");
 
     	JMenuItem searchItem = new JMenuItem("Search");
     	JMenuItem clusterItem = new JMenuItem("Cluster Manager");
     	JMenuItem experimentItem = new JMenuItem("Experimenter");
     	JMenuItem reporterItem = new JMenuItem("Reporter");
     	JMenuItem hoopEditorItem = new JMenuItem("Hoop Editor");    	
     	JMenuItem jobListItem = new JMenuItem("Hadoop Jobs");
     	JMenuItem stopWordItem = new JMenuItem("Stopword Editor");
     	JMenuItem vocabularyItem = new JMenuItem("Vocabulary Editor");
 
     	searchItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showSearchWindow();
     		}
     	});
    	
     	clusterItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showClusterWindow();
     		}
     	});
    	
     	experimentItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showExperimentWindow();
     		}
     	});
     	
     	reporterItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showReporterWindow();
     		}
     	});    	
     	
     	hoopEditorItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showHoopEditor();
     		}
     	});     	
     	
     	jobListItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showJobList();
     		}
     	});    	
     	
     	stopWordItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showStopWordEditor();
     		}
     	});     	    	
 
     	vocabularyItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			showVocabularyEditor();
     		}
     	});      	
     	
     	tools.add (searchItem);
     	tools.add (clusterItem);
     	tools.add (experimentItem);
     	tools.add (reporterItem);
     	tools.add (hoopEditorItem);
     	tools.add (jobListItem);
     	tools.add (stopWordItem);
     	tools.add (vocabularyItem);
     	
     	return (tools);
     }    
 	/**
 	 *
 	 */	
     protected JMenu buildSpeedMenu() 
     {
         JMenu speed = new JMenu("Drag");
 
         JRadioButtonMenuItem live = new JRadioButtonMenuItem("Live");
         JRadioButtonMenuItem outline = new JRadioButtonMenuItem("Outline");
         JRadioButtonMenuItem slow = new JRadioButtonMenuItem("Old and Slow");
 
         ButtonGroup group = new ButtonGroup();
 
         group.add(live);
         group.add(outline);
         group.add(slow);
 
         live.setSelected(true);
 
         slow.addActionListener(new ActionListener()
         {
         	public void actionPerformed(ActionEvent e)
         	{
         		// for right now I'm saying if you set the mode
         		// to something other than a specified mode
         		// it will revert to the old way
         		// This is mostly for comparison's sake
         		desktop.setDragMode(-1);}});
 
         		live.addActionListener(new ActionListener()
         		{
         			public void actionPerformed(ActionEvent e)
         			{
         				desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
         			}
         		});
       
         		outline.addActionListener(new ActionListener()
         		{
         			public void actionPerformed(ActionEvent e)
         			{
                          desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
         			}
         		});
       
         speed.add(live);
         speed.add(outline);
         speed.add(slow);
         return speed;
     }
 	/**
 	 *
 	 */	
     protected JMenu buildHelpMenu() 
     {
     	JMenu help = new JMenu("Help");
         JMenuItem about = new JMenuItem("About Hoop ...");
         JMenuItem openHelp = new JMenuItem("Open Help Window");
 
         about.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent e) 
         	{
         		showAboutBox();
         	}
         });
 
         openHelp.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent e) 
         	{
         		openHelpWindow();
         	}
         });
 
         help.add(about);
         help.add(openHelp);
 
         return help;
     }
 	/**
 	 *
 	 */	
     protected void buildContent() 
     {    	                
 		Border padding = BorderFactory.createEmptyBorder(2,0,0,0);
 		Border blackborder=BorderFactory.createLineBorder(Color.black);
 		//Border redborder=BorderFactory.createLineBorder(Color.red);		
 						
 		getContentPane().setLayout(new BoxLayout (getContentPane(),BoxLayout.Y_AXIS));
 				
 		Box centerBox = new Box (BoxLayout.X_AXIS);
 		centerBox.setMinimumSize(new Dimension (50,25));
 		centerBox.setPreferredSize(new Dimension (5000,5000));
 		centerBox.setMaximumSize(new Dimension (5000,5000));		
 						
 		// Add it all together ...
 		
 		JTabbedPane leftTabs = new JTabbedPane();
 		leftTabs.setFont(new Font("Dialog", 1, 10));
 										
 		JTabbedPane centerTabs = new JTabbedPane();
 		centerTabs.setFont(new Font("Dialog", 1, 10));		
 
 		desktop=new JDesktopPane();
 		desktop.setBackground(new Color (160,160,160));
 		
 		centerTabs.addTab("Desktop",null,desktop,"Temp desktop");
 				
 		JSplitPane centerPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftTabs,centerTabs);
 		centerPane.setResizeWeight(0.5);
 		centerPane.setDividerLocation(250);
 		centerPane.setOneTouchExpandable(true);
 		centerPane.setContinuousLayout(true);
 		
 		centerBox.add(centerPane);
 				
 		// Add a plotter ...
 		
 		plotter=new INScatterPlot ();
 		plotter.setMinimumSize(new Dimension (200,150));
 		plotter.setPreferredSize(new Dimension (5000,150));
 		plotter.setMaximumSize(new Dimension (5000,150));		
 		
 		Box statsBox = new Box (BoxLayout.X_AXIS);
 		//statsBox.setBorder(redborder);					
 		statsBox.setMinimumSize(new Dimension (20,110));
 		statsBox.setPreferredSize(new Dimension (5000,110));
 		statsBox.setMaximumSize(new Dimension (5000,110));	
 				
 		statsBox.add (plotter);						
 				
 		JTextArea rawStats=new JTextArea ();
 		rawStats.setBorder(blackborder);
 		rawStats.setEditable (false);
 		rawStats.setFont(new Font("Dialog", 1, 10));
 		rawStats.setMinimumSize(new Dimension (50,60));
 		rawStats.setPreferredSize(new Dimension (5000,60));
 		rawStats.setMaximumSize(new Dimension (5000,60));
 				
 		JTabbedPane tabbedPane = new JTabbedPane();
 		tabbedPane.setFont(new Font("Dialog", 1, 10));
 		
 		statsBox.setBorder(padding);
 		tabbedPane.addTab("Plots & Graphs",null,statsBox,"Visual representations of statistics");
 				
 		rawStats.setBorder(padding);
 		tabbedPane.addTab("Raw Statistics",null,rawStats,"Raw statistical data");
 				
 		// Finally add your vanilla default standard status bar ...
 						
 		statusbar=new INHoopStatusBar ();
 		statusbar.setBorder(blackborder);
 		statusbar.setMinimumSize(new Dimension (50,22));
 		statusbar.setPreferredSize(new Dimension (5000,22));
 		statusbar.setMaximumSize(new Dimension (5000,22));		
 		
 		//getContentPane ().add (centerBox);
 		//getContentPane ().add (tabbedPane);
 		
 		JToolBar toolBar = new JToolBar("Still draggable");
         addButtons(toolBar);
         toolBar.setMinimumSize(new Dimension (50,24));
         toolBar.setPreferredSize(new Dimension (5000,24));
         toolBar.setMaximumSize(new Dimension (5000,24));	
         
         JSeparator sep1=new JSeparator(SwingConstants.HORIZONTAL);
         sep1.setMinimumSize(new Dimension (50,5));
         sep1.setMaximumSize(new Dimension (5000,5));
         
         getContentPane ().add(sep1);
         
         getContentPane ().add (toolBar);
         
         JSeparator sep2=new JSeparator(SwingConstants.HORIZONTAL);
         sep2.setMinimumSize(new Dimension (50,5));
         sep2.setMaximumSize(new Dimension (5000,5));
 
         getContentPane ().add(sep2);
 		
 		left=new INHoopTabDraggable ();		
 		right=new INHoopTabDraggable ();		
 		center=new INHoopTabDraggable ();		
 		bottom=new INHoopTabDraggable ();
 		
         String layoutDef = "(ROW weight=1.0 (LEAF name=left weight=0.2) (COLUMN weight=0.6 (LEAF name=middle weight=0.8) (LEAF name=bottom weight=0.2)) (LEAF name=right weight=0.2))";
         MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
 
         MultiSplitPane multiSplitPane = new MultiSplitPane();
         multiSplitPane.setDividerSize(5);
         multiSplitPane.getMultiSplitLayout().setModel(modelRoot);
        	multiSplitPane.add(left, "left");
        	multiSplitPane.add(right, "right");
         multiSplitPane.add(center, "middle");
         multiSplitPane.add(bottom, "bottom");
 
        	Container cp = this.getContentPane();
        	cp.add(multiSplitPane, BorderLayout.CENTER);
        	
        	getContentPane ().add (statusbar);
 				
 		handler=new INHoopMessageHandler (stats,plotter,rawStats,null);
 		
 		server=new INSocketServerBase ();
 		server.setLocalPort (INLink.monitorPort);
 		server.setMessageHandler (handler);
 				
 		server.runServer ();
 		
 		debug ("Ready for input");        
 		
 		addView ("Package Explorer",new JPanel (),left);
 		addView ("Navigator",new JPanel (),left);
 		
 		addView ("Search",new JPanel (),bottom);
 		
 		addView ("Problems",new JPanel (),right);		
 		addView ("Console",new JPanel (),right);
     }
 	/**
 	 *
 	 */    
     protected void addButtons(JToolBar toolBar) 
     {
         JButton button = null;
 
         //first button
         button = makeNavigationButton("Back24", 
         							  PREVIOUS,
                                       "Back to previous something-or-other",
                                       "Previous",
                                       INLink.imageIcons [44]);        
         
         toolBar.add(button);
         
         //first button
         button = makeNavigationButton("Back24", 
         							  PREVIOUS,
                                       "Back to previous something-or-other",
                                       "Previous",
                                       INLink.imageIcons [16]);
         toolBar.add(button);
 
         //second button
         button = makeNavigationButton("Up24", 
         							  UP,
                                       "Up to something-or-other",
                                       "Up",
                                       INLink.imageIcons [55]);
         toolBar.add(button);
 
         //third button
         button = makeNavigationButton("Forward24", 
         							  NEXT,
                                       "Forward to something-or-other",
                                       "Next",
                                       INLink.imageIcons [56]);
         toolBar.add (button);
         
         JSeparator sep=new JSeparator(SwingConstants.VERTICAL);
         sep.setMinimumSize(new Dimension (5,5));
         sep.setMaximumSize(new Dimension (5,50));
         
         toolBar.add(sep);
         
         //third button
         button = makeNavigationButton("Forward24", 
         							  NEXT,
                                       "Forward to something-or-other",
                                       "Next",
                                       INLink.imageIcons [2]);
         toolBar.add (button);
         
         //third button
         button = makeNavigationButton("Forward24", 
         							  NEXT,
                                       "Forward to something-or-other",
                                       "Next",
                                       INLink.imageIcons [69]);
         toolBar.add (button);        
     }  
 	/**
 	 *
 	 */    
     protected JButton makeNavigationButton(String imageName,
     									   String actionCommand,
     									   String toolTipText,
     									   String altText,
     									   ImageIcon icon) 
     {
     	//Create and initialize the button.
     	JButton button = new JButton();
     	button.setBorder (null);
     	button.setMinimumSize(new Dimension (26,22));
     	button.setPreferredSize(new Dimension (50,22));
     	button.setActionCommand(actionCommand);
     	button.setToolTipText(toolTipText);
     	button.addActionListener(this);
     	button.setIcon(icon);
     	button.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
     	//button.setText(altText);
 
     	return button;
     }    
 	/**
 	 *
 	 */
     private void addView (String aTitle,JPanel aContent,JTabbedPane aPane)
     {
     	debug ("addView ("+aTitle+")");
     	
     	aPane.addTab(aTitle,INLink.imageIcons [5],aContent,"New Panel");
     	int index=aPane.indexOfComponent (aContent);
     	INHoopTabPane pane=new INHoopTabPane (aPane);
     	aPane.setTabComponentAt(index,pane);
     	pane.update();
     }
 	/**
 	 *
 	 */	
     public void quit() 
     {
         System.exit(0);
     }
 	/**
 	 *
 	 */	
     public void openHelpWindow() 
     {
     	JInternalFrame help = new INHoopHelp();
     	desktop.add(help, HELPLAYER);
     	try 
     	{ 
     		help.setVisible(true);
     		help.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
     		
     	}
     }
 	/**
 	 *
 	 */	
     public void showAboutBox() 
     {
         JOptionPane.showMessageDialog(this, ABOUTMSG+"\n\nHoop version "+INHoopVersion.version+" (compiled on Hadoop "+VersionInfo.getVersion()+", running on port "+INLink.monitorPort+")");
     }
 	/**
 	 *
 	 */	
     public void openPrefsWindow() 
     {
         //MetalworksPrefs dialog = new MetalworksPrefs(this);
         //dialog.show();
     }
 	/**
 	 *
 	 */	
     public void showSearchWindow() 
     {
     	debug ("showSearchWindow ()");
     	
     	INHoopSearch doc=new INHoopSearch();
     	desktop.add (doc,DOCLAYER);
     
     	try 
     	{ 
     		doc.setVisible(true);
     		doc.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
     		
     	}
     }
 	/**
 	 *
 	 */	
     public void showClusterWindow() 
     {
     	debug ("showClusterWindow ()");
    	
     	INHoopCluster doc=new INHoopCluster();
     	desktop.add (doc,DOCLAYER);
     	   	
     	try 
     	{ 
     		doc.setVisible(true);
     		doc.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
     		
     	}
     }
 	/**
 	 *
 	 */	
     public void showExperimentWindow () 
     {
     	debug ("showExperimentWindow ()");
   	
     	INHoopExperimenter doc=new INHoopExperimenter();
     	desktop.add (doc,DOCLAYER);
     	  	
     	try 
     	{ 
     		doc.setVisible(true);
     		doc.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
    		
     	}
     }    
 	/**
 	 *
 	 */	    
     public void showJobList ()
     {
     	debug ("showJobList ()");
     	     	
     	jobListWindow=new INHoopJobList();
     	desktop.add (jobListWindow,DOCLAYER);
    	  	
     	try 
     	{ 
     		jobListWindow.setVisible(true);
     		jobListWindow.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
   		
     	}    	
     }
 	/**
 	 *
 	 */	    
     public void showStopWordEditor ()
     {
     	debug ("showStopWordEditor ()");
    	     	
     	stopListEditor=new INHoopStopWordEditor();
     	desktop.add (stopListEditor,DOCLAYER);
   	  	
     	try 
     	{ 
     		stopListEditor.setVisible(true);
     		stopListEditor.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
 
     	}    	
     }
 	/**
 	 *
 	 */	    
     public void showVocabularyEditor ()
     {	
    		debug ("showVocabularyEditor ()");
   	     	
    		vocabularyEditor=new INHoopVocabularyEditor();
    		desktop.add (vocabularyEditor,DOCLAYER);
  	  	
    		try 
    		{ 
    			vocabularyEditor.setVisible(true);
    			vocabularyEditor.setSelected(true); 
    		} 
    		catch (java.beans.PropertyVetoException e2) 
    		{
    			
    		}    	
     }    
 	/**
 	 *
 	 */	    
     public void showHoopEditor ()
     {
     	debug ("showHoopEditor ()");
   	     	
     	hoopEditor=new INHoopEditor();
     	desktop.add (hoopEditor,DOCLAYER);
  	  	
     	try 
     	{ 
     		hoopEditor.setVisible(true);
     		hoopEditor.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
 		
     	}    	
     }   
 	/**
 	 *
 	 */	
     public void showReporterWindow () 
     {
     	debug ("showReporterWindow ()");
  	
     	INHoopReporter doc=new INHoopReporter();
     	desktop.add (doc,DOCLAYER);
    	  	
     	try 
     	{ 
     		doc.setVisible(true);
     		doc.setSelected(true); 
     	} 
     	catch (java.beans.PropertyVetoException e2) 
     	{
   		
     	}
     }      
 	/**
 	 *
 	 */	
 	@Override
 	public void actionPerformed (ActionEvent event) 
 	{
 		debug("actionPerformed ()");
 	}  
 }
