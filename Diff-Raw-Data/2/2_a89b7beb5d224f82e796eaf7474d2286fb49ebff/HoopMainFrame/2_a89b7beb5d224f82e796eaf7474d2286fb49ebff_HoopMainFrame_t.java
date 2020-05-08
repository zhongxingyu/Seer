 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.hoop;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import com.mxgraph.model.mxCell;
 import com.mxgraph.view.mxGraph;
 
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.base.io.HoopMessageReceiver;
 import edu.cmu.cs.in.controls.HoopComponentSnapshot;
 import edu.cmu.cs.in.controls.HoopControlTools;
 import edu.cmu.cs.in.controls.HoopJFileChooser;
 import edu.cmu.cs.in.controls.HoopSentenceWall;
 import edu.cmu.cs.in.controls.base.HoopJDialog;
 import edu.cmu.cs.in.controls.base.HoopViewInterface;
 import edu.cmu.cs.in.controls.dialogs.HoopCleanProjectDialog;
 import edu.cmu.cs.in.controls.dialogs.HoopGenericNameDialog;
 import edu.cmu.cs.in.controls.dialogs.HoopGenericProgressdialog;
 import edu.cmu.cs.in.controls.dialogs.HoopEnvironmentInspector;
 import edu.cmu.cs.in.controls.dialogs.HoopPreferencesDialog;
 import edu.cmu.cs.in.hoop.builder.HoopAppBuilder;
 import edu.cmu.cs.in.hoop.editor.HoopEditorToolBar;
 import edu.cmu.cs.in.hoop.execute.HoopExecute;
 import edu.cmu.cs.in.hoop.execute.HoopExecuteExceptionHandler;
 import edu.cmu.cs.in.hoop.execute.HoopExecuteInEditor;
 import edu.cmu.cs.in.hoop.execute.HoopExecuteProgressPanel;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.task.HoopStart;
 import edu.cmu.cs.in.hoop.project.HoopGraphFile;
 import edu.cmu.cs.in.hoop.project.HoopProject;
 import edu.cmu.cs.in.hoop.project.export.HoopProjectExportInterface;
 import edu.cmu.cs.in.hoop.project.export.HoopProjectZipExport;
 import edu.cmu.cs.in.hoop.properties.HoopPropertyPanel;
 import edu.cmu.cs.in.hoop.properties.HoopVisualProperties;
 import edu.cmu.cs.in.hoop.visualizers.HoopBackingDBInspector;
 import edu.cmu.cs.in.hoop.visualizers.HoopCluster;
 import edu.cmu.cs.in.hoop.visualizers.HoopParseTreeViewer;
 
 /** 
  *
  */
 public class HoopMainFrame extends HoopMultiViewFrame implements ActionListener, HoopMessageReceiver
 {
 	private static final long serialVersionUID = -1L;
 		    
 	private HoopConsole console=null;
 	private Component compReference=null;
 		
 	private ArrayList <HoopProjectExportInterface> exportPlugins=null;
 			
 	/**
 	 *
 	 */	
     public HoopMainFrame() 
     {                
     	setClassName ("HoopMainFrame");
     	debug ("HoopMainFrame ()");
     	
     	this.setIconImage (HoopLink.getImageByName("hoop.png").getImage());
     	
     	compReference=this;
     	HoopLink.runner=new HoopExecuteInEditor ();
     	
     	exportPlugins=new ArrayList<HoopProjectExportInterface> ();
     	
     	buildExportPlugins ();
     	    	    	    
         buildMenus();       
         
         addButtons (this.getToolBar());
                         
         startEditor ();
     }
     /**
      * 
      */
     protected void buildExportPlugins ()
     {
     	debug ("buildExportPlugins ()");
     	
     	exportPlugins.add(new HoopProjectZipExport ());
     }
     /**
      * 
      */
     /*
     private void updateProjectViews ()
     {
     	debug ("updateProjectViews ()");
     	
     	HoopProjectPanel projWindow=(HoopProjectPanel) HoopLink.getWindow("Project");
     	if (projWindow!=null)
     	{
     		projWindow.updateContents();
     	}
     }
     */
 	/**
 	 *
 	 */	
     protected void buildMenus() 
     {
         JMenuBar mBar = getMainMenuBar();
         mBar.setOpaque(true);
         
         JMenu file = buildFileMenu();
         JMenu edit = buildEditMenu();
         JMenu views = buildViewsMenu();
         JMenu project = buildProjectMenu();
         JMenu run = buildRunMenu();
         JMenu tools = buildToolsMenu();
         JMenu help = buildHelpMenu();
         
         mBar.add(file);
         mBar.add(edit);
         mBar.add(tools);
         mBar.add(project);
         mBar.add(run);
         mBar.add(views);
         mBar.add(help);        
     }
 	/**
 	 *
 	 */	
     protected JMenu buildFileMenu() 
     {
     	JMenu file = new JMenu("File");
     	
     	//>------------------------------------------------------
     	
     	JMenuItem newFile = new JMenuItem("New Project");
     	JMenuItem open = new JMenuItem("Open Project");
     	
     	newFile.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			newProject ();
     		}
     	});
 
     	open.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			openProject ();    			
     		}
     	});    	
     	
     	//>------------------------------------------------------
     	
     	JMenuItem save = new JMenuItem("Save",HoopLink.getImageByName("save.gif"));
     	
     	JMenuItem saveas = new JMenuItem("Save As ...",HoopLink.getImageByName("saveas.gif"));
     	
     	JMenuItem saveall = new JMenuItem("Save All",HoopLink.getImageByName("save.gif"));
     	saveall.setEnabled(false);
     	
     	JMenuItem revert = new JMenuItem("Revert",HoopLink.getImageByName("undo.gif"));
     	revert.setEnabled(false);
     	
     	save.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Save ...");
     		
     			if ( projectSave () )
     				JOptionPane.showMessageDialog(compReference, "Project saved");
     			else
     				JOptionPane.showMessageDialog(compReference, "Error saving project");
     		}
     	});
     	
     	saveas.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("SaveAs ...");
     		        		
     			if ( projectSaveAs () )
     				JOptionPane.showMessageDialog(compReference, "Project saved");
     			else
     				JOptionPane.showMessageDialog(compReference, "Error saving project");
     		}
     	});
     	
     	saveall.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("SaveAll ...");
 
     		}
     	});
     	
     	revert.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Revert ...");
 
     		}
     	});    	
     	
     	//>------------------------------------------------------    	
     	
     	JMenuItem imp = new JMenuItem("Import ...");
     	//imp.setEnabled(false);
     	
     	imp.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			importFiles ();
     		}
     	});    	
     	    	
     	JMenu exportMenu=new JMenu ("Export Project");
     	
     	//exp.add(exportMenu);
     	
     	for (int i=0;i<exportPlugins.size();i++)
     	{
     		HoopProjectExportInterface aPlugin=exportPlugins.get(i);
     		
     		JMenuItem aPluginItem = new JMenuItem(aPlugin.getDescription());
     		
     		exportMenu.add(aPluginItem);
     	}
     	
     	/*
     	exp.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     		
     		}
     	});
     	*/    	
     	
     	//>------------------------------------------------------    	
     	
     	JMenuItem props = new JMenuItem("Properties");
     	props.setEnabled(false);
     	
     	//>------------------------------------------------------
     	
     	JMenuItem quit = new JMenuItem("Exit");
     	    	
     	quit.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			quit();
     		}
     	});
     	
     	//>------------------------------------------------------
 
     	file.add(newFile);
     	file.add(open);
     	file.addSeparator();
     	file.add(save);
     	file.add(saveas);
     	file.add(saveall);
     	file.add(revert);
     	file.addSeparator();
     	file.add(imp);
     	file.add(exportMenu);
     	file.addSeparator();
     	file.add(props);   
     	file.addSeparator();    	
     	file.add(quit);
     	
     	return (file);
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
     	
     	return (edit);
     }
 	/**
 	 *
 	 */	
     protected JMenu buildViewsMenu() 
     {
     	JMenu views = new JMenu("Window");
 
     	/*
     	JMenuItem documentItem = new JMenuItem("Document Viewer");
 
     	documentItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Document",new HoopDocumentViewer(),"right");
     		}
     	});
     	*/
     	
     	JMenuItem parseTreeItem=new JMenuItem("Parse Tree Viewer");    	
     	
     	parseTreeItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Parse Tree Viewer",new HoopParseTreeViewer(),"center");
     		}
     	});     	
     	
     	JMenuItem documentListItem=new JMenuItem("Document Set Viewer");    	
     	
     	documentListItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Document List",new HoopDocumentList(),"right");
     		}
     	});    	
     	
     	JMenuItem consoleItem=new JMenuItem("Console Output");    	
     	
     	consoleItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     	    	console=new HoopConsole();
     	    	
     	    	addView ("Console",console,HoopLink.bottom);    	    	
     		}
     	});
     	
     	JMenuItem errorItem=new JMenuItem("Error Output");    	
     	
     	errorItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{    	    
     	    	addView ("Errors",new HoopErrorPanel(),"bottom");
     		}
     	});    	
     	
     	/*
     	JMenuItem plotterItem=new JMenuItem("Main Data Plotter");    	
     	
     	plotterItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     	    	plotter=new HoopScatterPlot ();
     	    	
     	    	addView ("Plotter",plotter,HoopLink.bottom);    	    	
     		}
     	});
     	*/        	
     	
     	JMenuItem propertiesItem=new JMenuItem("Properties");    	
     	
     	propertiesItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{    			    		    		
     			HoopPropertyPanel propPanel=(HoopPropertyPanel) HoopLink.getWindow("Properties");
     			
     			if (propPanel==null)
     				propPanel=new HoopPropertyPanel();
     	    	
     	    	addView ("Properties",propPanel,HoopLink.right);
     	    	
     	    	// Rebuild the property panel
     	    	
     	    	ArrayList <HoopBase> hoops=HoopLink.hoopGraphManager.getHoopList ();
     	    	
     	    	if (hoops!=null)
     	    	{
     	    		for (int t=0;t<hoops.size();t++)
     	    		{
     	    			HoopBase aHoop=hoops.get(t);
     	    			
     	    			HoopPropertyPanel.popupPropertyPanel (aHoop);
     	    		}
     	    	}
     	    	else
     	    		debug ("Error: no list of hoops found in graph manager");
     		}
     	});
     	
     	JMenuItem statsItem=new JMenuItem("Statistics");    	
     	
     	statsItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			HoopStatisticsPanel statsPanel=new HoopStatisticsPanel ();
     	    	
     	    	addView ("Statistics",statsPanel,HoopLink.bottom);    	    	
     		}
     	});
     	
     	JMenuItem sWallItem=new JMenuItem("Sentence Wall");    	
     	
     	sWallItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			HoopSentenceWall sWallPanel=new HoopSentenceWall ();
     	    	
     	    	addView ("Sentence Wall",sWallPanel,HoopLink.right);    	    	
     		}
     	});
     	
     	JMenuItem sTextViewItem=new JMenuItem("Text Viewer");
     	
     	sTextViewItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			HoopTextViewer sTextViewPanel=new HoopTextViewer ();
     	    	
     	    	addView ("Text Viewer",sTextViewPanel,HoopLink.center);    	    	
     		}
     	});  
     	
     	JMenuItem dbViewItem=new JMenuItem("Backing Database Viewer");
     	
     	dbViewItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			HoopBackingDBInspector dbViewPanel=new HoopBackingDBInspector ();
     	    	
     	    	addView ("Backing Database Viewer",dbViewPanel,HoopLink.center);    	    	
     		}
     	});      	
     	
     	
     	JMenuItem executeViewItem=new JMenuItem("Execution Monitor");
     	
     	executeViewItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			HoopExecuteProgressPanel executionMonitor=(HoopExecuteProgressPanel) HoopLink.getWindow("Execution Monitor");
     			
     			if (executionMonitor==null)
     			{
     				executionMonitor=new HoopExecuteProgressPanel ();
     				addView ("Execution Monitor",executionMonitor,HoopLink.bottom);
     			}
     			
     			HoopLink.popWindow("Execution Monitor");
     		}
     	});    	
     		    	    	    	
     	views.add (parseTreeItem);
     	views.add (sTextViewItem);
     	//views.add (documentItem);
     	views.add (documentListItem);
     	views.add (consoleItem);
     	views.add (errorItem);
     	views.add (statsItem);
     	//views.add (plotterItem);
     	views.add (new JSeparator());
     	views.add (propertiesItem);
     	views.add (sWallItem);
     	views.add (dbViewItem);
     	views.add(executeViewItem);
 
     	views.add (new JSeparator());
     	
     	for (int i=0;i<HoopLink.windowsPlugins.size();i++)
     	{
     		HoopViewInterface aView=HoopLink.windowsPlugins.get(i);
     		
     		JMenuItem aViewItem=new JMenuItem(aView.getDescription());
     		
     		views.add (aViewItem);
     		
     		aViewItem.addActionListener(new ActionListener() 
         	{
         		public void actionPerformed(ActionEvent e) 
         		{
         		   	for (int t=0;t<HoopLink.windowsPlugins.size();t++)
         	    	{
         	    		HoopViewInterface aView=HoopLink.windowsPlugins.get(t);
         	    	
         	    		addView (aView.getDescription(),aView.getPanel(),HoopLink.center);
         	    	}	
         		}
         	});   
     	}
     	
     	return (views);
     }
 	/**
 	 *
 	 */	
     protected JMenu buildToolsMenu() 
     {
     	JMenu tools = new JMenu("Tools");
 
     	JMenuItem searchItem = new JMenuItem("Search");    	
     	JMenuItem clusterItem = new JMenuItem("Cluster Monitor");
     	JMenuItem experimentItem = new JMenuItem("Experimenter");
     	JMenuItem reporterItem = new JMenuItem("Reporter");
     	JMenuItem hoopEditorItem = new JMenuItem("Hoop Editor");    	
     	JMenuItem jobListItem = new JMenuItem("Hadoop Jobs");
     	//JMenuItem stopWordItem = new JMenuItem("Stopword Editor");
     	JMenuItem vocabularyItem = new JMenuItem("Vocabulary Editor");
     	JMenuItem opSpaceItem = new JMenuItem("Narrative Opportunity Space Visualizer");
     	//JMenuItem hexMapItem = new JMenuItem("Hexagon Map");
     	JMenuItem sentItem = new JMenuItem("Sentinet Panel");
 
     	searchItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Search",new HoopSearch (),HoopLink.center);
     		}
     	});
     	   	
     	clusterItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Cluster Monitor",new HoopCluster (),HoopLink.center);
     		}
     	});
    	
     	experimentItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Experimenter",new HoopExperimenter (),HoopLink.center);
     		}
     	});
     	
     	reporterItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Reporter",new HoopReporter (),HoopLink.bottom);
     		}
     	});    	
     	
     	hoopEditorItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{    			    			
     			startEditor ();
     		}
     	});     	
     	
     	jobListItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Hadoop Jobs",new HoopJobList (),HoopLink.right);
     		}
     	});    	
     	
     	/*
     	stopWordItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Stop Words",new HoopStopWordEditor (),HoopLink.left);
     		}
     	});
     	*/     	    	
 
     	vocabularyItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Vocabulary",new HoopVocabularyEditor (),HoopLink.left);
     		}
     	});      	
     	
     	sentItem.addActionListener (new ActionListener ()
     	{    		    		
     		public void actionPerformed(ActionEvent e) 
     		{
     			//addView ("Sentinet",new HoopSentinetPanel (),HoopLink.center);
     		}    		
     	});
     	
     	/*
     	opSpaceItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Opportunity Space",new HoopOpportunitySpace (),HoopLink.center);
     		}
     	});
     	*/
     	
     	/*
     	hexMapItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			addView ("Hexagon Map",new HoopJava3DJPanel (),HoopLink.center);
     		}
     	});
     	*/
     	    	    	
     	tools.add (searchItem);
     	tools.add (clusterItem);
     	tools.add (experimentItem);
     	tools.add (reporterItem);
     	tools.add (hoopEditorItem);
     	tools.add (jobListItem);
     	//tools.add (stopWordItem);
     	tools.add (vocabularyItem);
     	tools.add (opSpaceItem);
     	tools.add (sentItem);
     	//tools.add (hexMapItem);
     	
     	return (tools);
     }    
 	/**
 	 *
 	 */	
     protected JMenu buildHelpMenu() 
     {
     	JMenu help = new JMenu("Help");
     	JMenuItem env = new JMenuItem("Inspect Environment");
         JMenuItem about = new JMenuItem("About Hoop ...");
         JMenuItem openHelp = new JMenuItem("Open Help Window");
         
         about.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent e) 
         	{
         		showAboutBox();
         	}
         });
         
         env.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent e) 
         	{
                 HoopEnvironmentInspector envDialog=new HoopEnvironmentInspector (HoopLink.mainFrame,true);                
                 envDialog.setVisible (true);
                 envDialog.fillPropList();
         	}
         });        
 
         openHelp.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent e) 
         	{        		
         		addView ("Help",new HoopHelp (),HoopLink.left);        		
         	}
         });
         
         help.add(about);
         help.add (env);
         help.add(openHelp);
 
         return help;
     }
 	/**
 	 *
 	 */	
     protected JMenu buildProjectMenu() 
     {
     	JMenu project = new JMenu("Project");
     	JMenuItem buildItem = new JMenuItem("Build");
     	JMenuItem buildAppItem = new JMenuItem("Build Application");
     	JMenuItem cleanItem = new JMenuItem("Clean");
     	JMenuItem propertiesItem = new JMenuItem("Properties");
 
     	buildItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			buildProject ();
     		}
     	});
 
     	buildAppItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			buildApplication ();
     		}
     	});    	
     	
     	cleanItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			// Fill in later
     			cleanProject ();
     		}
     	});
 
     	propertiesItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			// Fill in later
     		}
     	});
 
     	project.add(buildItem);
     	project.add(buildAppItem);
     	project.add(cleanItem);
     	project.addSeparator();
     	project.add(propertiesItem);
    	
     	return (project);
     }    
 	/**
 	 *
 	 */	
     protected JMenu buildRunMenu() 
     {
     	JMenu runMenu = new JMenu("Run");
     	
     	JMenuItem runOnceItem = new JMenuItem("Run");
     	runOnceItem.setIcon(HoopLink.getImageByName("run.png"));
     	
     	JMenuItem runClusterItem = new JMenuItem("Run on Cluster");
     	runClusterItem.setIcon(HoopLink.getImageByName("run-cluster.png"));
     	
     	/*
     	JMenuItem runNTimesItem = new JMenuItem("Run N Times");
     	runNTimesItem.setIcon(HoopLink.getImageByName("run-n.png"));
     	
     	JMenuItem runForeverItem = new JMenuItem("Run Until Stopped");
     	runForeverItem.setIcon(HoopLink.getImageByName("run-forever.png"));
     	*/
     	    	
     	runOnceItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Run ...");
     			
     			HoopExecuteProgressPanel executionMonitor=(HoopExecuteProgressPanel) HoopLink.getWindow("Execution Monitor");
     			if (executionMonitor==null)
     			{
     				executionMonitor=new HoopExecuteProgressPanel ();
     				addView ("Execution Monitor",executionMonitor,HoopLink.bottom);
     			}
     			
     			HoopLink.popWindow("Execution Monitor");
     			
     			HoopLink.runner.setRoot(HoopLink.hoopGraphManager.getRoot());
     			HoopLink.runner.setLoopCount(1);
     			HoopLink.runner.setLocation (HoopExecute.LOCAL);
     			    			
     			Thread runner=new Thread (HoopLink.runner);
     			runner.setUncaughtExceptionHandler(new HoopExecuteExceptionHandler ());    			
     			runner.start();
     		}
     	});
     	
     	runClusterItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Run on Cluster ...");
     			
     			HoopExecuteProgressPanel executionMonitor=(HoopExecuteProgressPanel) HoopLink.getWindow("Execution Monitor");
     			if (executionMonitor==null)
     			{
     				executionMonitor=new HoopExecuteProgressPanel ();
     				addView ("Execution Monitor",executionMonitor,HoopLink.bottom);
     			}
     			
     			HoopLink.popWindow("Execution Monitor");
     			
     			HoopCluster clusterMonitor=(HoopCluster) HoopLink.getWindow("Cluster Monitor");
     			if (clusterMonitor==null)
     			{
     				clusterMonitor=new HoopCluster ();
     				addView ("Cluster Monitor",clusterMonitor,HoopLink.center);
     			}
     			
     			HoopLink.popWindow("Cluster Monitor");
     			
     			HoopLink.runner.setRoot(HoopLink.hoopGraphManager.getRoot());
     			HoopLink.runner.setLoopCount(1);
     			HoopLink.runner.setLocation (HoopExecute.CLUSTER);
     			    			
     			Thread runner=new Thread (HoopLink.runner);
     			runner.setUncaughtExceptionHandler(new HoopExecuteExceptionHandler ());    			
     			runner.start();
     		}
     	});    	
     	
     	/*
     	runNTimesItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Run N Times ...");
     			
     			runtime.setRoot(HoopLink.hoopGraphManager.getRoot());
     			runtime.setLoopCount(10);
     			
     			Thread runner=new Thread (runtime);
     			runner.setUncaughtExceptionHandler(new HoopExecuteExceptionHandler ());    			
     			runner.start();   			
     		}
     	});
     	*/
     	
     	/*
     	runForeverItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Run Forever ...");
     			    			
     			runtime.setRoot(HoopLink.hoopGraphManager.getRoot());
     			runtime.setLoopCount(-1);
     			
     			Thread runner=new Thread (runtime);
     			runner.setUncaughtExceptionHandler(new HoopExecuteExceptionHandler ());    			
     			runner.start();   			
     		}
     	});
     	*/ 
     	
     	JMenuItem debugItem = new JMenuItem("Debug");
     	debugItem.setIcon(HoopLink.getImageByName("debug.png"));
 
     	debugItem.addActionListener(new ActionListener() 
     	{
     		public void actionPerformed(ActionEvent e) 
     		{
     			debug ("Debug ...");
     		}
     	});
 
        runMenu.add (runOnceItem);
        runMenu.add (runClusterItem);
        //runMenu.add (runNTimesItem);
        //runMenu.add (runForeverItem);
        runMenu.add (new JSeparator());
        runMenu.add (debugItem);
 
        return runMenu;
     }    
 	/**
 	 *
 	 */    
     protected void addButtons(JToolBar toolBar) 
     {
     	debug ("addButtons ()");
     	
         JButton runButton = HoopControlTools.makeNavigationButton ("run","Run",HoopLink.getImageByName("run.png"));
         runButton.addActionListener((HoopExecuteInEditor) HoopLink.runner);
         
         JButton runClusterButton = HoopControlTools.makeNavigationButton ("runCluster","Run on Cluster",HoopLink.getImageByName("run-cluster.png"));
         runClusterButton.addActionListener((HoopExecuteInEditor) HoopLink.runner);        
         
         JButton debugButton = HoopControlTools.makeNavigationButton ("debug","Debug",HoopLink.getImageByName("debug.png"));
         debugButton.addActionListener((HoopExecuteInEditor) HoopLink.runner);
         
         /*
         JButton runNButton = HoopControlTools.makeNavigationButton ("runN","Run N Times",HoopLink.getImageByName("run-n.png"));
         runNButton.addActionListener(runtime);
         
         JButton runForeverButton = HoopControlTools.makeNavigationButton ("runForever","Run Forever",HoopLink.getImageByName("run-forever.png"));
         runForeverButton.addActionListener(runtime);
         */
         
         JSeparator sep=new JSeparator(SwingConstants.VERTICAL);
         sep.setMinimumSize(new Dimension (5,5));
         sep.setMaximumSize(new Dimension (5,50));
         
         JButton stopButton = HoopControlTools.makeNavigationButton ("Stop","Stop",HoopLink.getImageByName("run-stopped.png"));
         stopButton.addActionListener((HoopExecuteInEditor) HoopLink.runner);
         
         toolBar.add (runButton);
         toolBar.add (Box.createRigidArea(new Dimension(2,0)));
         
         toolBar.add (runClusterButton);
         toolBar.add (Box.createRigidArea(new Dimension(2,0)));
         
         toolBar.add (debugButton);        
         toolBar.add (Box.createRigidArea(new Dimension(2,0)));
                         
         /*
         toolBar.add (runNButton);
         toolBar.add (Box.createRigidArea(new Dimension(2,0)));
         
         toolBar.add (runForeverButton);
         */
 
         toolBar.add (sep);
         toolBar.add (stopButton);                                
     }     
 	/**
 	 *
 	 */	
     public void openPrefsWindow() 
     {
     	debug ("openPrefsWindow ()");
     	
     	HoopPreferencesDialog prefs=new HoopPreferencesDialog (HoopLink.mainFrame,false);
    		prefs.setVisible (true);
     }
 	/**
 	 *
 	 */	
 	@Override
 	public void handleIncomingData(String data) 
 	{
 		debug ("handleIncomingData ()");
 	}
 	/**
 	 *
 	 */	
 	@Override
 	public void handleConnectionClosed() 
 	{
 		debug ("handleConnectionClosed ()");
 		
 	}
 	/**
 	 * Currently we have a method that creates a hardcoded perspective that corresponds
 	 * to the default perspective associated with graph editing. It makes a number of
 	 * assumptions on what panels the user would like to use but at this point it seems
 	 * there is enough space for all the panes and options required 
 	 */
 	private void startEditor ()
 	{
 		debug ("startEditor ()");
 		
 	    addView ("Text Viewer",new HoopTextViewer (),HoopLink.center);
 	    
 	    addView ("Document List",new HoopDocumentList(),"right");
 					
 		if (HoopLink.getWindow("Console")==null)
 		{
 			console=new HoopConsole();    	
 			addView ("Console",console,HoopLink.bottom);
 		}	
 		
 		/*
 		HoopExecuteProgressPanel executionMonitor=(HoopExecuteProgressPanel) HoopLink.getWindow("Execution Monitor");
 		if (executionMonitor==null)
 		{
 			executionMonitor=new HoopExecuteProgressPanel ();
 			addView ("Execution Monitor",executionMonitor,HoopLink.bottom);
 		}
 		*/		
 		
 		/*
 		HoopDialogConsole userIO=(HoopDialogConsole) HoopLink.getWindow("User Dialog");
 		if (userIO==null)
 		{
 			HoopLink.addView ("User Dialog",new HoopDialogConsole (),HoopLink.bottom);
 			userIO=(HoopDialogConsole) HoopLink.getWindow("User Dialog");
 		}
 		*/					
 		
     	HoopProjectPanel projectPanel=(HoopProjectPanel) HoopLink.getWindow("Project Explorer");
     	if (projectPanel==null)
     	{
     		projectPanel=new HoopProjectPanel ();
     		addView ("Project Explorer",projectPanel,HoopLink.left);
     	}	
     	
 		HoopTreeList hoopList=(HoopTreeList) HoopLink.getWindow("HoopList");
 		if (hoopList==null)
 		{
 			hoopList=new HoopTreeList ();
 			addView ("Hoop List",hoopList,HoopLink.left);
 		}
 		
 		HoopPropertyPanel propPanel=(HoopPropertyPanel) HoopLink.getWindow("Properties");
     	if (propPanel==null)
     	{
     		propPanel=new HoopPropertyPanel();
     		addView ("Properties",propPanel,HoopLink.right);
     	}	
 		
 		HoopGraphEditor editor=(HoopGraphEditor) HoopLink.getWindow("Hoop Editor");		
 		if (editor==null)
 		{
 			editor=new HoopGraphEditor ();
 			addView ("Hoop Editor",editor,HoopLink.center);
 		}	
 			    			
 		newProjectInternal (null);
 		
 		//HoopLink.menuBar.create(editor);
 		
 		if (HoopLink.toolEditorBar==null)
 		{
 			HoopLink.toolEditorBar=new HoopEditorToolBar ();
 		
 			HoopLink.toolBoxContainer.add (HoopLink.toolEditorBar,1);    		
 			HoopLink.toolEditorBar.create(editor,JToolBar.HORIZONTAL);
 		}	
 		
 		projectPanel.updateContents();
 	}
 	/**
 	 * 
 	 */
 	private void newProjectInternal (String aURI)
 	{
 		debug ("newProjectInternal ()");
 		
 		HoopLink.project=new HoopProject ();
 		HoopLink.project.newProject (aURI);
 		
 		HoopGraphEditor editor=(HoopGraphEditor) HoopLink.getWindow("Hoop Editor");
 		if (editor==null)
 		{
 			alert ("Error no graph editor available to create new start node");
 			return;
 		}
 		
 		mxGraph graph=editor.getGraph ();
 		
 		Object parent=graph.getDefaultParent();
 		
 		graph.getModel().beginUpdate();
 		
 		try
 		{
 			HoopStart startNode=new HoopStart ();
 			
 			HoopLink.hoopGraphManager.addHoop (startNode);
 			
 			HoopVisualProperties vizProps=startNode.getVisualProperties();
 			
 			mxCell graphObject=(mxCell) graph.insertVertex (parent, 
 															startNode.getClassName(),
 															startNode,
 															20,
 															20,
 															vizProps.getWidth(),
 															vizProps.getHeight());
 			
 			graphObject.setValue(startNode.getHoopID());			
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 		
 		HoopLink.project.resetChanged();
 	}
 	/**
 	 * 
 	 */
 	public void showErrorWindow ()
 	{
 		debug ("showErrorWindow ()");
 		
 		HoopErrorPanel test=(HoopErrorPanel) HoopLink.getWindow("Errors");
 		
 		if (test==null)
 		{
 			addView ("Errors",new HoopErrorPanel(),"bottom");
 			test=(HoopErrorPanel) HoopLink.getWindow("Errors");
 		}	
 		
 		HoopLink.popWindow ("Errors");
 	}
 	/**
 	 * 
 	 */
 	private Boolean newProject ()
 	{
 		debug ("newProject ()");
 		
 		if (HoopLink.project!=null)
 		{
 			debug ("We already have an open project!");
 			
 			if (HoopLink.project.hasChanged()==true)
 			{			
 				debug ("Existing project has changed!");
 				
 				Object[] options = {"Yes","No","Cancel"};
 				int n = JOptionPane.showOptionDialog (compReference,
 														"You already have a project open, save and close this project first?",
 														"Hoop Info Panel",
 														JOptionPane.YES_NO_CANCEL_OPTION,
 														JOptionPane.QUESTION_MESSAGE,
 														null,
 														options,
 														options[2]);
            	
 				if (n==0)
 				{          	
 					debug ("Saving project ...");
            		
 					startWaitCursor ();
            		
 					HoopLink.project.save();
            		
 					HoopComponentSnapshot.saveScreenShot(this,HoopLink.project.getBasePath()+"/preview.png");
            		
 					endWaitCursor ();
 				}
            	
 				if (n==2)
 				{
 					debug ("Aborting creating new project");
 					return (false);
 				}
 			}	
 		}
 				
 		HoopJFileChooser fc = new HoopJFileChooser();
 		
 		FileNameExtensionFilter filter=new FileNameExtensionFilter ("Target Directories", "Directories");
 		fc.setFileFilter(filter);    			
 		fc.setFileSelectionMode(HoopJFileChooser.DIRECTORIES_ONLY);
 		
 		int returnVal=fc.showSaveDialog (compReference);
 
 		if (returnVal==HoopJFileChooser.APPROVE_OPTION) 
 		{
 			/*
 			Object[] options = {"Yes","No","Cancel"};
            	int n = JOptionPane.showOptionDialog (compReference,
            										  "Loading a saved set will override any existing selections, do you want to continue?",
            										  "Hoop Info Panel",
            										  JOptionPane.YES_NO_CANCEL_OPTION,
            										  JOptionPane.QUESTION_MESSAGE,
            										  null,
            										  options,
            										  options[2]);
            	
            	if (n==0)
            	{          	
         		HoopPropertyPanel propPanel=(HoopPropertyPanel) HoopLink.getWindow("Properties");
             	if (propPanel!=null)
             	{
             		propPanel.reset();
             	}           		
            		
            		File file = fc.getSelectedFile();
 
            		debug ("Creating in directory: " + file.getAbsolutePath() + " ...");
                    	           		           		
            		newProjectInternal (file.getAbsolutePath());
            	}
            	*/
 			
        		File file = fc.getSelectedFile();
 
        		debug ("Creating in directory: " + file.getAbsolutePath() + " ...");
                	           		           		
        		newProjectInternal (file.getAbsolutePath());			
        		
        		HoopLink.project.save ();
 		} 
 		else 
 		{
 			debug ("Open command cancelled by user.");
 			return (false);
 		}		
 		
 		//updateProjectViews ();
 		refreshProjectPane ();
 		
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private Boolean openProject ()
 	{
 		debug ("openProject");
 				
 		if (HoopLink.project!=null)
 		{
 			if (HoopLink.project.isEmpty()==false)
 			{
 					debug ("We already have an open project!");
 			
 					if (HoopLink.project.hasChanged()==true)
 					{
 						Object[] options = {"Yes","No","Cancel"};
 						int n = JOptionPane.showOptionDialog (compReference,
 																"You already have a project open, save and close this project first?",
 																"Hoop Info Panel",
            									  		  	  	JOptionPane.YES_NO_CANCEL_OPTION,
            									  		  	  	JOptionPane.QUESTION_MESSAGE,
            									  		  	  	null,
            									  		  	  	options,
            									  		  	  	options[2]);
            	
 						if (n==0)
 						{          	
 							debug ("Saving project ...");
            		
 							if (HoopLink.project.getVirginFile()==true)
 							{
 								if (projectSaveAs ()==false)
 									return (false);
 							}
 							else
 							{
 								try
 								{
 									startWaitCursor ();
 				           		
 									HoopLink.project.save();
 				           		
 									HoopComponentSnapshot.saveScreenShot(this,HoopLink.project.getBasePath()+"/preview.png");
 								}
 								finally
 								{
 									endWaitCursor ();
 								}
 							}
 						}
            	
 						if (n==2)
 						{
 							debug ("Aborting creating new project");
 							return (false);
 						}
 					}	
 			}		
 		}    			
 				
 		/*
 		FileNameExtensionFilter filter=new FileNameExtensionFilter (".hprj project files", "hprj");
 		fc.setFileFilter(filter); 
 		fc.setFileSelectionMode (HoopJFileChooser.FILES_ONLY);
 		*/
 		
 		HoopJFileChooser fc = new HoopJFileChooser();
 		
 		FileNameExtensionFilter filter=new FileNameExtensionFilter ("Target Directories", "Directories");
 		fc.setFileFilter(filter);    			
 		fc.setFileSelectionMode(HoopJFileChooser.DIRECTORIES_ONLY);    			
 		
 		int returnVal=fc.showOpenDialog (compReference);
 
 		if (returnVal==HoopJFileChooser.APPROVE_OPTION) 
 		{   	
 			HoopPropertyPanel propPanel=(HoopPropertyPanel) HoopLink.getWindow("Properties");
 	    	if (propPanel!=null)
 	    	{
 	    		propPanel.reset();
 	    	}			
 	    	
 	    	HoopDocumentList docList=(HoopDocumentList) HoopLink.getWindow("Document List");
 			if (docList!=null)
 			{
 				docList.reset ();
 			}	    	
 			
            	File file = fc.getSelectedFile();
 
            	debug ("Loading: " + file.getAbsolutePath() + " ...");
                           	
        		startWaitCursor ();
            		
        		HoopJDialog waiter=new HoopJDialog (HoopJDialog.NONE,HoopLink.mainFrame,false,"Please wait, loading ...");
        		waiter.setVisible (true);
        		
            	try
            	{
            		HoopLink.project=new HoopProject (); // Blatantly whipe it
            		HoopLink.project.load(file.getAbsolutePath());           		
            	}
            	finally
            	{
 
            	}	
 
        		endWaitCursor ();
        		waiter.setVisible (false);
            	
            	// Do a ton of housekeeping here ...
            		
            	HoopGraphEditor win=(HoopGraphEditor) HoopLink.getWindow("Hoop Editor");
            		    	           		    	           		
            	if (win==null)
            	{
            		win=new HoopGraphEditor ();
            		addView ("Hoop Editor",win,HoopLink.center);
            	}
 
            	HoopGraphFile graphFile=(HoopGraphFile) HoopLink.project.getFileByClass ("HoopGraphFile");
            	if (graphFile!=null)
            	{
                	win.reset();
            		win.instantiateFromFile(graphFile);
            	}
            	else
            	{
            		alert ("Unable to find graph file in project");
            		
            		return (false);
            	}
 		} 
 		else 
 		{
 			debug ("Open command cancelled by user.");
 		}
 		
 		refreshProjectPane ();
 
 		HoopDocumentList docList=(HoopDocumentList) HoopLink.getWindow("Document List");
 
 		if (docList!=null)
 			docList.updateContents();
 
 		//updateAllWindows ();
 
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private Boolean projectSave ()
 	{
 		debug ("projectSave ()");
 				
 		HoopProject proj=HoopLink.project;
 		
 		if (proj==null)
 		{
 			debug ("Internal error: no project available");
 			return (false);
 		}
 		
 		if (proj.getVirginFile()==true)
 		{    
 			try
 			{
 				startWaitCursor ();
 			
 				if (projectSaveAs ()==false)
 				{
 					endWaitCursor ();
 					return (false);
 				}
 			}
 			finally
 			{
 				endWaitCursor ();
 			}
 		}
 		else
 		{
 			proj.save();
 		}			
 		
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private Boolean projectSaveAs ()
 	{
 		debug ("projectSaveAs ()");
 				
 		HoopProject proj=HoopLink.project;
 		
 		if (proj==null)
 		{
 			debug ("Internal error: no project available");
 			return (false);
 		}
 		
 		HoopJFileChooser fc = new HoopJFileChooser();
 		
 		FileNameExtensionFilter filter=new FileNameExtensionFilter ("Target Directories", "Directories");
 		fc.setFileFilter(filter);    			
 		fc.setFileSelectionMode(HoopJFileChooser.DIRECTORIES_ONLY);
 			
 		int returnVal=fc.showSaveDialog (compReference);
 
 		if (returnVal==HoopJFileChooser.APPROVE_OPTION) 
 		{
 	       	File file = fc.getSelectedFile();
 
 	       	debug ("Creating in directory: " + file.getAbsolutePath() + " ...");
 	                
 	       	File testFile=new File (file.getAbsolutePath()+"/.hprj");
 	       	if (testFile.exists()==true)
 	       	{
 	       		alert ("Error: a project already exists in that location");
 	       		return (false);
 	       	}
 	       	else
 	       	{        	       	
 	       		HoopLink.project.setFileURI(file.getAbsolutePath()+"/.hprj");
 	           		        	       	        	       	
 	           	try
 	           	{
 	           		startWaitCursor ();
 	           		
 	           		proj.save();
 	           	}
 	           	finally
 	           	{
 	           		endWaitCursor ();
 	           	}
 	       	}	
 		} 
 		else 
 		{
 			debug ("Open command cancelled by user.");
 			return (false);
 		}	    			
 		
 		return (true);
 	}
 	/**
 	 * http://www.richjavablog.com/2012/03/display-file-copy-progress-in-swing.html
 	 */
 	private void importFiles ()
 	{
 		debug ("importFiles ()");
 						
 		HoopGenericNameDialog directoryName=null;
 		HoopProject proj=HoopLink.project;
 			
 		if (proj==null)
 		{
 			debug ("Internal error: no project available");
 			return;
 		}
 		
 		if (proj.getVirginFile()==true)
 		{
 			alert ("Please save your project first");
 			return;
 		}
 		
 		HoopJFileChooser fc = new HoopJFileChooser();
 		fc.setMultiSelectionEnabled (true);
 							
 		int returnVal=fc.showOpenDialog (compReference);
 
 		if (returnVal==HoopJFileChooser.APPROVE_OPTION) 
 		{
 			directoryName=new HoopGenericNameDialog (HoopGenericNameDialog.DIRECTORY,this,true);
			directoryName.setDescription ("Please provide the subdirectory under which you want to import your files. This folder will appear under your project's system directory.");
 			directoryName.setChosenName ("import");
 			directoryName.setVisible (true);
 			
 			if (directoryName.getAnswer()==false)
 			{
 				return;
 			}
 			
 			String targetDir=directoryName.getChosenName ();
 						
 			File files []=fc.getSelectedFiles();
 			
 			String dirCreator=proj.getBasePath()+"/system/"+targetDir;
 			
 			HoopLink.fManager.createDirectory(dirCreator);
 			
 			HoopGenericProgressdialog copyProcess=new HoopGenericProgressdialog (this.getContentPane());
 			copyProcess.copyFiles(fc.getCurrentDirectory ().getAbsolutePath(),
 								  dirCreator,
 								  files);						
 		}
 		
 		refreshProjectPane ();
 	}
 	/**
 	 * 
 	 */
 	private void refreshProjectPane ()
 	{
 		debug ("refreshProjectPane ()");
 		
 		HoopProjectPanel projectPane=(HoopProjectPanel) HoopLink.getWindow("Project Explorer");
        	if (projectPane!=null)
        	{
        		projectPane.refresh();
        	}
        	
 		HoopExecuteProgressPanel executionMonitor=(HoopExecuteProgressPanel) HoopLink.getWindow("Execution Monitor");
 		if (executionMonitor!=null)
 		{
 			executionMonitor.reset ();
 		}       	
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
 		debug ("actionPerformed ()");
 	}
 	/**
 	 * 
 	 */
 	private void buildProject ()
 	{
 		debug ("buildProject ()");
 	
 		if (HoopLink.project.getVirginFile()==true)
 		{
 			alert ("Please save your project first");
 			return;
 		}
 		
 		HoopLink.fManager.createDirectory(HoopLink.project.getBasePath()+"/bin");
 	}
 	/**
 	 * 
 	 */
 	private void cleanProject ()
 	{
 		debug ("cleanProject ()");
 		
 		if (HoopLink.project.getVirginFile()==true)
 		{
 			alert ("Please save your project first");
 			return;
 		}
 		
 		HoopCleanProjectDialog cleanConfig=new HoopCleanProjectDialog (this,true);
 		cleanConfig.setVisible(true);
 		
 	    if (cleanConfig.getAnswer()) 
 	    {
 	    	//debug ("The answer stored in CustomDialog is 'true' (i.e. user clicked yes button.)");
 	    	HoopLink.project.clean (cleanConfig.getCleanDocuments(),
 	    							cleanConfig.getCleanBuildOutput(),
 	    							cleanConfig.getCleanTempFiles());
 	    }
 	    else 
 	    {
 	    	//debug ("The answer stored in CustomDialog is 'false' (i.e. user clicked no button.)");
 		}							
 	}
 	/**
 	 * 
 	 */
 	private void buildApplication ()
 	{
 		debug ("buildApplication ()");
 		
 		if (HoopLink.project.getVirginFile()==true)
 		{
 			alert ("Please save your project first");
 			return;
 		}
 		
 		HoopAppBuilder builder=new HoopAppBuilder (this,true);
 		builder.setVisible(true);
 		
 	    if (builder.getAnswer()) 
 	    {
 
 	    }
 	    else 
 	    {
 	    	//debug ("The answer stored in CustomDialog is 'false' (i.e. user clicked no button.)");
 		}							
 	}	
 }
