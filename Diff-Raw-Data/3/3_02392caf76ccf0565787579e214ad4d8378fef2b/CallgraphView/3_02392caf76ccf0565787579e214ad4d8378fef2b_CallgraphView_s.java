 /*******************************************************************************
  * Copyright (c) 2009 Red Hat, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Red Hat - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.linuxtools.callgraph;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.draw2d.parts.ScrollableThumbnail;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.linuxtools.callgraph.core.PluginConstants;
 import org.eclipse.linuxtools.callgraph.core.SystemTapErrorHandler;
 import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
 import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;
 import org.eclipse.linuxtools.callgraph.core.SystemTapView;
 import org.eclipse.linuxtools.callgraph.graphlisteners.AutoScrollSelectionListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 /**
  *	The SystemTap View for displaying output of the 'stap' command, and acts
  *	as a container for any graph to be rendered. Any buttons/controls/actions
  *	necessary to the smooth running of SystemTap could be placed here.
  */
 public class CallgraphView extends SystemTapView {
 	private static final String NEW_LINE = Messages.getString("CallgraphView.3"); //$NON-NLS-1$
 
 
 	private static StapGraphParser parser;
 
 
 	private static Action open_callgraph;
 	private static Action save_callgraph;
 	private static Action open_default;
 	private static Action error_errorLog;
 	private static Action error_deleteError;
 	private static Action view_treeview;
 	private static Action view_radialview;
 	private static Action view_aggregateview;
 	private static Action view_boxview;	
 	private static Action view_refresh;	
 	private static Action animation_slow;
 	private static Action animation_fast;
 	private static Action mode_collapsednodes;
 	private static Action markers_next; 
 	private static Action markers_previous;
 	private static Action limits; 
 	private static Action goto_next;
 	private static Action goto_previous;
 	private static Action goto_last;
 	
 	private static IMenuManager menu;
 	private static IMenuManager gotoMenu;
 	private static IMenuManager file;
 	private static IMenuManager errors;
 	private static IMenuManager view;
 	private static IMenuManager animation;
 	private static IMenuManager markers; //Unused
 	private static IMenuManager help;
 	@SuppressWarnings("unused")
 	private static Action help_about;
 	private static Action help_version;
 	public static IToolBarManager mgr;
 	
 	private static Composite graphComp;
 	private static Composite treeComp;
 	
 	private static StapGraph graph;
 	
 	private static StapGraph g;
 	private static int treeSize = 200;
 	
 
 
 	
 	public IStatus initialize(Display targetDisplay, IProgressMonitor monitor) {
 
 		Display disp = Display.getCurrent();
 		if (disp == null)
 			disp = Display.getDefault();
 		
 		
 		//-------------Initialize shell, menu
 		treeSize = 200;
 
 		
 		//OPEN UP THE SYSTEMTAPVIEW IF IT IS NOT ALREADY OPEN
 		CallgraphView.forceDisplay();
 		
 		Composite treeComp = CallgraphView.makeTreeComp(treeSize);
 		Composite graphComp = CallgraphView.makeGraphComp();
 		graphComp.setBackgroundMode(SWT.INHERIT_FORCE);
 		
 		
 		//Create papa canvas
 		Canvas papaCanvas = new Canvas(graphComp, SWT.BORDER);
 		GridLayout papaLayout = new GridLayout(1, true);
 		papaLayout.horizontalSpacing=0;
 		papaLayout.verticalSpacing=0;
 		papaLayout.marginHeight=0;
 		papaLayout.marginWidth=0;
 		papaCanvas.setLayout(papaLayout);
 		GridData papaGD = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
 		papaGD.widthHint=160;
 		papaCanvas.setLayoutData(papaGD);
 		
 		
 		//Add first button
 		Image image = new Image(disp, CallGraphConstants.PLUGIN_LOCATION+"icons/up.gif"); //$NON-NLS-1$
 		Button up = new Button(papaCanvas, SWT.PUSH);
 		GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
 		buttonData.widthHint = 150;
 		buttonData.heightHint = 20;
 		up.setData(buttonData);
 		up.setImage(image);
 		
 		
 		//Add thumb canvas
 		Canvas thumbCanvas = new Canvas(papaCanvas, SWT.NONE);
 		
 		
 		//Add second button
 		image = new Image(disp, CallGraphConstants.PLUGIN_LOCATION+"icons/down.gif"); //$NON-NLS-1$
 		Button down = new Button(papaCanvas, SWT.PUSH);
 		buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
 		buttonData.widthHint = 150;
 		buttonData.heightHint = 0;
 		down.setData(buttonData);
 		down.setImage(image);
 
 		
 		//Initialize graph
 		g = new StapGraph(graphComp, SWT.BORDER, treeComp, papaCanvas);
 		g.setLayoutData(new GridData(targetDisplay.getPrimaryMonitor().getBounds().width - 200,
 					targetDisplay.getPrimaryMonitor().getBounds().height - 200));
 
 		up.addSelectionListener(new AutoScrollSelectionListener(
 				AutoScrollSelectionListener.AutoScroll_up, g));
 		down.addSelectionListener(new AutoScrollSelectionListener(
 				AutoScrollSelectionListener.AutoScroll_down, g));
 		
 		
 		//Initialize thumbnail
 		GridData thumbGD = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
 		thumbGD.widthHint=160;
 		thumbCanvas.setLayoutData(thumbGD);
 		LightweightSystem lws = new LightweightSystem(thumbCanvas);
 		ScrollableThumbnail thumb = new ScrollableThumbnail(g.getViewport());
 		thumb.setSource(g.getContents());
 		lws.setContents(thumb);
 
 		//-------------Load graph data
 		g.loadData(SWT.NONE, 0, StapGraph.CONSTANT_TOP_NODE_NAME, 1, 1, -1, false, ""); //$NON-NLS-1$
 		boolean marked = false;
 		String msg = ""; //$NON-NLS-1$
 		
 		
 	    for (int id_parent : parser.serialMap.keySet()) {
 	    	if (g.getNodeData(id_parent) == null) {
 				if (parser.markedMap.get(id_parent) != null) {
 					marked = true;
 					msg = parser.markedMap.get(id_parent);
 				}
 	    		g.loadData(SWT.NONE, id_parent, parser.serialMap.get(id_parent), parser.timeMap.get(id_parent),
 	    				1, 0, marked, msg);
 	    	}
 	    	
 			for (int id_child : parser.outNeighbours.get(id_parent)) {
 				
 				if (monitor.isCanceled()) {
 					return Status.CANCEL_STATUS;
 				}
 				
 				marked = false;
 				msg = ""; //$NON-NLS-1$
 				if (parser.markedMap.get(id_child) != null) {
 					marked = true;
 					msg = parser.markedMap.get(id_child);
 				}
 				if (id_child != -1) {
 					if (parser.timeMap.get(id_child) == null){						
 						g.loadData(SWT.NONE, id_child, parser.serialMap
 								.get(id_child), parser.timeMap.get(0),
 								1, id_parent, marked,msg);
 					}else{
 						g.loadData(SWT.NONE, id_child, parser.serialMap
 								.get(id_child), parser.timeMap.get(id_child),
 								1, id_parent, marked,msg);
 					}
 				}
 			}
 			
 		}
 
 	    
 	    g.aggregateCount = parser.countMap;
 	    g.aggregateTime = parser.cumulativeTimeMap;
 
 	    //Set total time
 	    g.setTotalTime(parser.totalTime);
 	    
 	    //-------------Finish initializations
 	    //Generate data for collapsed nodes
 	    g.recursivelyCollapseAllChildrenOfNode(g.getTopNode());
 		if (monitor.isCanceled()) {
 			return Status.CANCEL_STATUS;
 		}
 	    g.initializeTree();
 	    g.setLastFunctionCalled(parser.lastFunctionCalled);
 	    
 
 		if (monitor.isCanceled()) {
 			return Status.CANCEL_STATUS;
 		}
 	    g.setFocus();
 	    g.setCallOrderList(parser.callOrderList);
 	    
 	    g.setProject(parser.project);
 	    
 	    CallgraphView.setValues(graphComp, treeComp, g, parser);
 	    CallgraphView.createPartControl();
 	    g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, StapGraph.CONSTANT_ANIMATION_SLOW,
 		g.getFirstUsefulNode());
 		return Status.OK_STATUS;
 	}
 	
 	
 	public static void testFunction() {
 		if (masterComposite != null && !masterComposite.isDisposed())
 			masterComposite.dispose();
 	}
 	
 	public static void setValues(Composite graphC, Composite treeC, StapGraph g, StapGraphParser p){
 		treeComp = treeC;
 		graphComp = graphC;
 		graph = g;
 		parser = p;
 	}
 	
 	
 	/**
 	 * Enable or Disable the graph options
 	 * @param visible
 	 */
 	public static void setGraphOptions (boolean visible){
 		save_callgraph.setEnabled(visible);
 		view_treeview.setEnabled(visible);
 		view_radialview.setEnabled(visible);
 		view_aggregateview.setEnabled(visible);
 		view_boxview.setEnabled(visible);
 		view_refresh.setEnabled(visible);
 		limits.setEnabled(visible);
 		
 		markers_next.setEnabled(visible);
 		markers_previous.setEnabled(visible);
 		
 		animation_slow.setEnabled(visible);
 		animation_fast.setEnabled(visible);
 		mode_collapsednodes.setEnabled(visible);
 		
 		goto_next.setEnabled(visible);
 		goto_previous.setEnabled(visible);
 		goto_last.setEnabled(visible);
 	}
 	
 	
 	
 	public static void firstTimeRefresh(){
 
 		graphComp.setSize(masterComposite.getSize().x ,masterComposite.getSize().y);
 	}
 	
 	
 	public static Composite makeTreeComp(int treeSize) {
 		if (treeComp != null && !treeComp.isDisposed()) {
 			return treeComp;
 		}
 		
 		Composite treeComp = new Composite(CallgraphView.masterComposite, SWT.NONE);
 		GridData treegd = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
 		treegd.widthHint = treeSize;
 		treeComp.setLayout(new FillLayout());
 		treeComp.setLayoutData(treegd);
 		return treeComp; 
 	}
 	
 	public static Composite makeGraphComp() {
 //		if (graphComp != null && !graphComp.isDisposed()) {
 //			return graphComp;
 //		}
 		if (graphComp != null)
 			graphComp.dispose();
 		Composite graphComp = new Composite(CallgraphView.masterComposite, SWT.NONE);
 		GridData graphgd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		GridLayout gl = new GridLayout(2, false);
 		gl.horizontalSpacing=0;
 		gl.verticalSpacing=0;
 		
 		graphComp.setLayout(gl);
 		graphComp.setLayoutData(graphgd);
 		return graphComp;
 	}
 
 	
 //	
 //	public static void disposeAll() {
 //		if (graphComp != null) {
 //			graphComp.setVisible(false);
 //			GridData gd = (GridData) graphComp.getLayoutData();
 //			gd.exclude = true;
 //			graphComp.setLayoutData(gd);
 //			graphComp.dispose();
 //		}
 //		if (treeComp != null) {
 //			treeComp.setVisible(false);
 //			GridData gd = (GridData) treeComp.getLayoutData();
 //			gd.exclude = true;
 //			treeComp.setLayoutData(gd);
 //			treeComp.dispose();
 //		}
 //	}
 
 	/**
 	 * This must be executed before a Graph is displayed
 	 */
 	public static void createPartControl(){
 		
 		
 		setGraphOptions(true);
 		cleanViewer();
 		
 		
 		
 		graphComp.setParent(masterComposite);
 		
 		if (treeComp != null)
 			treeComp.setParent(masterComposite);
 
 		if (graph == null) {
 			createViewer(masterComposite);
 		}
 		
 		//MAXIMIZE THE SYSTEMTAP VIEW WHEN RENDERING A GRAPH
 		firstTimeRefresh();
 	}
 	
 	
 
 	/**
 	 * This is a callback that will allow us to create the viewer and
 	 * initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		if (masterComposite != null)
 			masterComposite.dispose();
 		masterComposite = parent;
 		GridLayout layout = new GridLayout(2, false);
 		layout.horizontalSpacing=0;
 		GridData gd = new GridData(100, 100);
 
 		parent.setLayout(layout);
 		parent.setLayoutData(gd);
 
 		//CREATE THE TEXT VIEWER
 		if (graph == null)
 			createViewer(parent);
 
 		// LOAD ALL ACTIONS
 		createActions();
 		
 		//MENU FOR SYSTEMTAP BUTTONS
 		mgr = getViewSite().getActionBars().getToolBarManager();
 		
 		
 		//MENU FOR SYSTEMTAP GRAPH OPTIONS
 		menu = getViewSite().getActionBars().getMenuManager();
 		
 		// ADD OPTIONS TO THE GRAPH MENU
 		file = new MenuManager(Messages.getString("CallgraphView.0")); //$NON-NLS-1$
 		view = new MenuManager(Messages.getString("CallgraphView.1")); //$NON-NLS-1$
 		errors = new MenuManager(Messages.getString("CallgraphView.Errors")); //$NON-NLS-1$
 		animation = new MenuManager(Messages.getString("CallgraphView.2")); //$NON-NLS-1$
 		help = new MenuManager(Messages.getString("CallgraphView.5")); //$NON-NLS-1$
 		markers = new MenuManager(Messages.getString("CallgraphView.6")); //$NON-NLS-1$
 		gotoMenu = new MenuManager(Messages.getString("CallgraphView.9")); //$NON-NLS-1$
 		
 
 		
 		menu.add(file);
 		menu.add(view);
 //		menu.add(animation);
 		menu.add(gotoMenu);
 		menu.add(errors);
 		menu.add(help);
 		
 		
 		file.add(open_callgraph);
 		file.add(open_default);
 		file.add(save_callgraph);
 		
 		
 		errors.add(error_errorLog);
 		errors.add(error_deleteError);
 		
 		
 		view.add(view_treeview);
 		view.add(view_radialview);
 		view.add(view_aggregateview);
 		view.add(view_boxview);
 		view.add(getView_refresh());
 		view.add(mode_collapsednodes);
 		view.add(limits);
 		
 		
 		gotoMenu.add(goto_previous);
 		gotoMenu.add(goto_next);
 		gotoMenu.add(goto_last);
 		
 		
 		mgr.add(view_radialview);
 		mgr.add(view_treeview);
 		mgr.add(view_boxview);
 		mgr.add(view_aggregateview);
 		mgr.add(getView_refresh());
 		mgr.add(mode_collapsednodes);
 		
 //		help.add(help_about);
 		help.add(help_version);
 		
 		markers.add(markers_next);
 		markers.add(markers_previous);
 		
 		animation.add(animation_slow);
 		animation.add(animation_fast);
 //		menu.add(markers);
 
 		setGraphOptions(false);
 		
 		// Colouring helper variable
 		setView(this);
 	}
 
 	/**
 	 * Force the CallgraphView to initialize
 	 */
 	public static void forceDisplay(){
 		try {
 			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 			window.getActivePage().showView("org.eclipse.linuxtools.callgraph.callgraphview").setFocus(); //$NON-NLS-1$
 		} catch (PartInitException e2) {
 			e2.printStackTrace();
 		}
 		
 	}
 
 	
 	
 	/**
 	 * Populates the file menu
 	 */
 	public void createFileActions() {
 		//Opens from some location in your program
 		open_callgraph = new Action(Messages.getString("CallgraphView.7")){ //$NON-NLS-1$
 			public void run(){
 				FileDialog dialog = new FileDialog(new Shell(), SWT.DEFAULT);
 				String filePath =  dialog.open();
 				if (filePath != null){
 					StapGraphParser new_parser = new StapGraphParser();
 					new_parser.setFile(filePath);
 					new_parser.schedule();					
 				}
 			}
 		};
 		
 		//Opens from the default location
 		open_default = new Action(Messages.getString("CallgraphView.11")){ //$NON-NLS-1$
 			public void run(){
 				StapGraphParser new_parser = new StapGraphParser();
 				new_parser.schedule();					
 			}
 		};
 		
 		
 		//Save callgraph.out
 		save_callgraph = new Action(Messages.getString("CallgraphView.8")){ //$NON-NLS-1$
 			public void run(){
 				Shell sh = new Shell();
 				FileDialog dialog = new FileDialog(sh, SWT.SAVE);
 				String filePath = dialog.open();
 				
 				if (filePath != null) {
 					parser.saveData(filePath);
 				}
 			}
 		};
 		
 	}
 	
 	public void createHelpActions() {
 		help_version = new Action(Messages.getString("CallgraphView.13")) {  //$NON-NLS-1$
 			public void run() {
 			Runtime rt = Runtime.getRuntime();
 			try {
 				Process pr = rt.exec("stap -V"); //$NON-NLS-1$
 				BufferedReader buf = new BufferedReader(new InputStreamReader(pr
 						.getErrorStream()));
 				String line = ""; //$NON-NLS-1$
 				String message = ""; //$NON-NLS-1$
 				
 				while ((line = buf.readLine()) != null) {
 					message += line + NEW_LINE; //$NON-NLS-1$
 				}
 				
 				try {
 					pr.waitFor();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				
 				Shell sh = new Shell();
 				
 				MessageDialog.openInformation(sh, Messages.getString("CallgraphView.SystemTapVersionBox"), message); //$NON-NLS-1$
 					
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		};
 		
 		help_about = new Action(Messages.getString("CallgraphView.4")) { //$NON-NLS-1$
 			public void run() {
 				Display disp = Display.getCurrent();
 				if (disp == null){
 					disp = Display.getDefault();
 				}
 
 				
 				Shell sh = new Shell(disp, SWT.MIN | SWT.MAX);
 				sh.setSize(425, 540);
 				GridLayout gl = new GridLayout(1, true);
 				sh.setLayout(gl);
 
 				sh.setText(""); //$NON-NLS-1$
 				
 				Image img = new Image(disp, PluginConstants.PLUGIN_LOCATION+"systemtap.png"); //$NON-NLS-1$
 				Composite cmp = new Composite(sh, sh.getStyle());
 				cmp.setLayout(gl);
 				GridData data = new GridData(415,100);
 				cmp.setLayoutData(data);
 				cmp.setBackgroundImage(img);
 
 				Composite c = new Composite(sh, sh.getStyle());
 				c.setLayout(gl);
 				GridData gd = new GridData(415,400);
 				c.setLayoutData(gd);
 				c.setLocation(0,300);
 				StyledText viewer = new StyledText(c, SWT.READ_ONLY | SWT.MULTI
 						| SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);		
 				
 				GridData viewerGD = new GridData(SWT.FILL, SWT.FILL, true, true);
 				viewer.setLayoutData(viewerGD);
 				Font font = new Font(sh.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
 				viewer.setFont(font);
 				viewer.setText(
 						 "" + //$NON-NLS-1$
 						 "" + //$NON-NLS-1$
 						 "" + //$NON-NLS-1$
 						 "" +  //$NON-NLS-1$
 						 "" + //$NON-NLS-1$
 						 "" + //$NON-NLS-1$
 						 
 						 "" + //$NON-NLS-1$
 //						 
 //						 Messages.getString("LaunchAbout.9") + //$NON-NLS-1$
 //						 Messages.getString("LaunchAbout.10") + //$NON-NLS-1$
 						 
 						 "" + //$NON-NLS-1$
 						 "" + //$NON-NLS-1$
 						 "" + //$NON-NLS-1$
 						 
 //						 Messages.getString("LaunchAbout.14") + //$NON-NLS-1$
 //						 Messages.getString("LaunchAbout.15") + //$NON-NLS-1$
 //						 Messages.getString("LaunchAbout.16") + //$NON-NLS-1$
 						 
 						 "" + //$NON-NLS-1$
 						 
 //						 Messages.getString("LaunchAbout.18") + //$NON-NLS-1$
 //						 Messages.getString("LaunchAbout.19") + //$NON-NLS-1$
 						 
 						 "" + //$NON-NLS-1$
 						 "" //$NON-NLS-1$
 						);
 
 
 				
 				sh.open();		
 			}
 		};
 	}
 	
 	/**
 	 * Populates the Errors menu
 	 */
 	public void createErrorActions() {
 
 		error_errorLog = new Action(Messages.getString("CallgraphView.OpenLog")) { //$NON-NLS-1$
 			public void run() {
 				boolean error = false;
 				File log = new File(PluginConstants.DEFAULT_OUTPUT + "Error.log"); //$NON-NLS-1$
 				BufferedReader buff;
 				try {
 					buff = new BufferedReader(new FileReader(log));
 				String logText = ""; //$NON-NLS-1$
 				String line;
 				
 				while ((line = buff.readLine()) != null) {
 					logText+=line + PluginConstants.NEW_LINE;
 				}
 				
 				Shell sh = new Shell(SWT.BORDER | SWT.TITLE);
 				
 				sh.setText(Messages.getString("CallgraphView.15")); //$NON-NLS-1$
 				sh.setLayout(new FillLayout());
 				sh.setSize(600,600);
 				
 				StyledText txt = new StyledText(sh, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
 				
 				txt.setText(logText);
 
 				sh.setText(Messages.getString("CallgraphView.21")); //$NON-NLS-1$
 				
 				sh.open();
 				txt.setTopIndex(txt.getLineCount());
 
 				
 				} catch (FileNotFoundException e) {
 					error = true;
 				} catch (IOException e) {
 					error = true;
 				} finally {
 					if (error) {
 						SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
 							Messages.getString("CallgraphView.ErrorMessageName"), //$NON-NLS-1$
 							Messages.getString("CallgraphView.ErrorMessageTitle"), //$NON-NLS-1$
 							Messages.getString("CallgraphView.ErrorMessageBody") + //$NON-NLS-1$
 							Messages.getString("CallgraphView.ErrorMessageBody2")); //$NON-NLS-1$
 						mess.schedule();
 					}
 				}
 				
 			}
 		};
 		
 		
 		error_deleteError = new Action(Messages.getString("CallgraphView.ClearLog")) { //$NON-NLS-1$
 			public void run() {
 					if (!MessageDialog.openConfirm(new Shell(), Messages.getString("CallgraphView.DeleteLogsTitle"),  //$NON-NLS-1$
 							Messages.getString("CallgraphView.DeleteLogsMessage") + //$NON-NLS-1$
 							Messages.getString("CallgraphView.DeleteLogsMessage2"))) //$NON-NLS-1$
 						return;
 					
 					SystemTapErrorHandler.delete();
 			}
 		};
 		
 		
 	}
 	
 	public void createViewActions() {
 		//Set drawmode to tree view
 		view_treeview = new Action(Messages.getString("CallgraphView.16")){ //$NON-NLS-1$
 			public void run() {
 				graph.draw(StapGraph.CONSTANT_DRAWMODE_TREE, graph.getAnimationMode(), 
 						graph.getRootVisibleNodeNumber());
 				graph.scrollTo(graph.getNode(graph.getRootVisibleNodeNumber()).getLocation().x
 						- graph.getBounds().width / 2, graph.getNode(
 						graph.getRootVisibleNodeNumber()).getLocation().y);
 			}
 		};
 		ImageDescriptor treeImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), CallGraphConstants.PLUGIN_LOCATION + "icons/tree_view.gif")); //$NON-NLS-1$
 		view_treeview.setImageDescriptor(treeImage);
 		
 		
 		//Set drawmode to radial view
 		view_radialview = new Action(Messages.getString("CallgraphView.17")){ //$NON-NLS-1$
 			public void run(){
 				graph.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, graph.getAnimationMode(),
 						graph.getRootVisibleNodeNumber());
 
 			}
 		};
 		ImageDescriptor d = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.PLUGIN_LOCATION + "/icons/radial_view.gif")); //$NON-NLS-1$
 		view_radialview.setImageDescriptor(d);
 
 		
 		//Set drawmode to aggregate view
 		view_aggregateview = new Action(Messages.getString("CallgraphView.18")){ //$NON-NLS-1$
 			public void run(){
 				graph.draw(StapGraph.CONSTANT_DRAWMODE_AGGREGATE, graph.getAnimationMode(), 
 						graph.getRootVisibleNodeNumber());
 
 			}
 		};
 		ImageDescriptor aggregateImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.PLUGIN_LOCATION + "/icons/view_aggregateview.gif")); //$NON-NLS-1$
 		view_aggregateview.setImageDescriptor(aggregateImage);
 		
 		
 		//Set drawmode to box view
 		view_boxview = new Action(Messages.getString("CallgraphView.19")){ //$NON-NLS-1$
 			public void run(){
 				graph.draw(StapGraph.CONSTANT_DRAWMODE_BOX, graph.getAnimationMode(), 
 						graph.getRootVisibleNodeNumber());
 			}
 		};
 		ImageDescriptor boxImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.PLUGIN_LOCATION + "/icons/showchild_mode.gif")); //$NON-NLS-1$
 		view_boxview.setImageDescriptor(boxImage);
 		
 		
 		setView_refresh(new Action(Messages.getString("CallgraphView.Reset")){ //$NON-NLS-1$
 			public void run(){
 				graph.reset();
 			}
 		});
 		ImageDescriptor refreshImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.PLUGIN_LOCATION + "/icons/nav_refresh.gif")); //$NON-NLS-1$
 		getView_refresh().setImageDescriptor(refreshImage);
 		
 		
 	}
 	
 
 	/**
 	 * Populates Animate menu.
 	 */
 	public void createAnimateActions() {
 		//Set animation mode to slow
 		animation_slow = new Action(Messages.getString("CallgraphView.20"), Action.AS_RADIO_BUTTON){ //$NON-NLS-1$
 			public void run(){
 				graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_SLOW);
 				this.setChecked(true);
 				animation_slow.setChecked(true);
 				animation_fast.setChecked(false);
 			}
 		};
 		
 		animation_slow.setChecked(true);
 		
 		//Set animation mode to fast
 		animation_fast = new Action(Messages.getString("CallgraphView.22"), Action.AS_RADIO_BUTTON){ //$NON-NLS-1$
 			public void run(){
 				graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
 				animation_slow.setChecked(false);
 				animation_fast.setChecked(true);
 			}
 		};
 		
 		//Toggle collapse mode
 		mode_collapsednodes = new Action(Messages.getString("CallgraphView.24"), Action.AS_CHECK_BOX){ //$NON-NLS-1$
 			public void run(){
 				
 				if (graph.isCollapseMode()) {
 					graph.setCollapseMode(false);
 					graph.draw(graph.getRootVisibleNodeNumber());
 				}
 				else {
 					graph.setCollapseMode(true);
 					graph.draw(graph.getRootVisibleNodeNumber());
 				}
 			}
 		};
 		
 		ImageDescriptor newImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), CallGraphConstants.PLUGIN_LOCATION + "icons/mode_collapsednodes.gif")); //$NON-NLS-1$
 		mode_collapsednodes.setImageDescriptor(newImage);
 		
 		limits = new Action(Messages.getString("CallgraphView.SetLimits"), Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
 			private Spinner limit;
 			private Spinner buffer;
 			private Shell sh;
 			public void run() {
 				sh = new Shell();
 				sh.setLayout(new GridLayout());
 				sh.setSize(150, 200);
 				Label limitLabel = new Label(sh, SWT.NONE);
 				limitLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				limitLabel.setText(Messages.getString("CallgraphView.MaxNodes")); //$NON-NLS-1$
 				limit = new Spinner(sh, SWT.BORDER);
 				limit.setMaximum(5000);
 				limit.setSelection(graph.getMaxNodes());
 				limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				
 				Label bufferLabel = new Label(sh, SWT.NONE);
 				bufferLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				bufferLabel.setText(Messages.getString("CallgraphView.MaxDepth")); //$NON-NLS-1$
 				buffer = new Spinner(sh, SWT.BORDER);
 				buffer.setMaximum(5000);
 				buffer.setSelection(graph.getLevelBuffer());
 				buffer.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				
 				Button set_limit = new Button(sh, SWT.PUSH);
 				set_limit.setText(Messages.getString("CallgraphView.SetValues")); //$NON-NLS-1$
 				set_limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				set_limit.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						boolean redraw = false;
 						if (limit.getSelection() > 0 && buffer.getSelection() > 0) {
 							graph.setMaxNodes(limit.getSelection());
 							graph.setLevelBuffer(buffer.getSelection());
 							
 							if (graph.changeLevelLimits(graph.getLevelOfNode(graph.getRootVisibleNodeNumber()))) {
 								SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
 										Messages.getString("CallgraphView.BufferTooHigh"), Messages.getString("CallgraphView.BufferTooHigh"),  //$NON-NLS-1$ //$NON-NLS-2$
 										Messages.getString("CallgraphView.BufferMessage1") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage2") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage3") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage4") + graph.getLevelBuffer() + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage5") + PluginConstants.NEW_LINE + PluginConstants.NEW_LINE +   //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage6") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage7")); //$NON-NLS-1$
 								mess.schedule();
 							}
 							
 							redraw = true;
 						}
 						sh.dispose();
 						
 						if (redraw)
 							graph.draw();
 					}
 					
 				});
 
 				
 				sh.open();			}
 		};
 
 	}
 
 /**
  * Creates actions by calling the relevant functions
  */
 	public void createActions() {
 		createFileActions();
 		createHelpActions();
 		createErrorActions();
 		createViewActions();
 		createAnimateActions();
 		createMarkerActions();		
 		createMovementActions();
 
 		mode_collapsednodes.setChecked(true);
 		
 	}
 	
 	public void createMovementActions() {
 		goto_next = new Action(Messages.getString("CallgraphView.Next")) { //$NON-NLS-1$
 			public void run() {
 				if (graph.isCollapseMode()) {
 					graph.setCollapseMode(false);
 				}
 				int toDraw = graph.getNextCalledNode(graph.getRootVisibleNodeNumber());
 				if (toDraw != -1)
 					graph.draw(toDraw);
 			}
 		};
 		
 		goto_previous = new Action(Messages.getString("CallgraphView.Previous")) { //$NON-NLS-1$
 			public void run() {
 				if (graph.isCollapseMode()) {
 					graph.setCollapseMode(false);
 				}
 				int toDraw = graph.getPreviousCalledNode(graph.getRootVisibleNodeNumber());
 				if (toDraw != -1)
 					graph.draw(toDraw);
 			}
 		};
 		
 		goto_last = new Action(Messages.getString("CallgraphView.Last")) { //$NON-NLS-1$
 			public void run() {
 				if (graph.isCollapseMode())
 					graph.setCollapseMode(false);
 				graph.draw(graph.getLastFunctionCalled());
 			}
 		};
 	}
 	
 	public void createMarkerActions() {
 		markers_next = new Action(Messages.getString("CallgraphView.nextMarker")) { //$NON-NLS-1$
 			public void run() {
 				graph.draw(graph.getNextMarkedNode());
 			}
 		};
 		
 		markers_previous = new Action(Messages.getString("CallgraphView.previousMarker")) { //$NON-NLS-1$
 			public void run() {
 				graph.draw(graph.getPreviousMarkedNode());
 			}
 		};
 	}
 	
 	
 	public static void disposeGraph() {
 		if (graphComp != null && !graphComp.isDisposed())
 			graphComp.dispose();
 		if (treeComp != null && !treeComp.isDisposed())
 			treeComp.dispose();
 		disposeView();
 		CallgraphView.setGraphOptions(false);
 		//Force a redraw (.redraw() .update() not working)
 		CallgraphView.maximizeOrRefresh(false);
 	}
 
 	//METHODS USED MOSTLY IN TESTING
 	public static Action getAnimation_slow() {
 		return animation_slow;
 	}
 
 	public static void setAnimation_slow(Action animation_slow) {
 		CallgraphView.animation_slow = animation_slow;
 	}
 
 	public static Action getAnimation_fast() {
 		return animation_fast;
 	}
 
 	public static void setAnimation_fast(Action animation_fast) {
 		CallgraphView.animation_fast = animation_fast;
 	}
 
 	public static IMenuManager getAnimation() {
 		return animation;
 	}
 
 	public static void setAnimation(IMenuManager animation) {
 		CallgraphView.animation = animation;
 	}
 
 	public static Action getMode_collapsednodes() {
 		return mode_collapsednodes;
 	}
 
 	public static void setMode_collapsednodes(Action mode_collapsednodes) {
 		CallgraphView.mode_collapsednodes = mode_collapsednodes;
 	}
 
 	public static void setView_refresh(Action view_refresh) {
 		CallgraphView.view_refresh = view_refresh;
 	}
 
 	public static Action getView_refresh() {
 		return view_refresh;
 	}
 
 	public static Action getGoto_next() {
 		return goto_next;
 	}
 
 	public static void setGoto_next(Action gotoNext) {
 		goto_next = gotoNext;
 	}
 
 	public static Action getGoto_previous() {
 		return goto_previous;
 	}
 
 	public static void setGoto_parent(Action gotoParent) {
 		goto_previous = gotoParent;
 	}
 
 	public static Action getGoto_last() {
 		return goto_last;
 	}
 
 	public static void setGoto_last(Action gotoLast) {
 		goto_last = gotoLast;
 	}
 
 	public static Action getOpen_callgraph() {
 		return open_callgraph;
 	}
 
 	public static void setOpen_callgraph(Action openCallgraph) {
 		open_callgraph = openCallgraph;
 	}
 
 	public static Action getSave_callgraph() {
 		return save_callgraph;
 	}
 
 	public static void setSave_callgraph(Action saveCallgraph) {
 		save_callgraph = saveCallgraph;
 	}
 
 	public static Action getError_errorLog() {
 		return error_errorLog;
 	}
 
 	public static void setError_errorLog(Action errorErrorLog) {
 		error_errorLog = errorErrorLog;
 	}
 
 	public static Action getError_deleteError() {
 		return error_deleteError;
 	}
 
 	public static void setError_deleteError(Action errorDeleteError) {
 		error_deleteError = errorDeleteError;
 	}
 
 	public static Action getView_treeview() {
 		return view_treeview;
 	}
 
 	public static void setView_treeview(Action viewTreeview) {
 		view_treeview = viewTreeview;
 	}
 
 	public static Action getView_radialview() {
 		return view_radialview;
 	}
 
 	public static void setView_radialview(Action viewRadialview) {
 		view_radialview = viewRadialview;
 	}
 
 	public static Action getView_aggregateview() {
 		return view_aggregateview;
 	}
 
 	public static void setView_aggregateview(Action viewAggregateview) {
 		view_aggregateview = viewAggregateview;
 	}
 
 	public static Action getView_boxview() {
 		return view_boxview;
 	}
 
 	public static void setView_boxview(Action viewBoxview) {
 		view_boxview = viewBoxview;
 	}
 
 	public static Action getHelp_version() {
 		return help_version;
 	}
 
 	public static void setHelp_version(Action helpVersion) {
 		help_version = helpVersion;
 	}
 
 	public static void setGoto_previous(Action gotoPrevious) {
 		goto_previous = gotoPrevious;
 	}
 	
 	public static StapGraph getGraph() {
 		return graph;
 	}
 	
 	public boolean setParser(SystemTapParser newParser) {
 		if (newParser instanceof StapGraphParser) {
 			parser = (StapGraphParser) newParser;
 			return true;
 		}
 		return false;
 		
 	}
 
 }
