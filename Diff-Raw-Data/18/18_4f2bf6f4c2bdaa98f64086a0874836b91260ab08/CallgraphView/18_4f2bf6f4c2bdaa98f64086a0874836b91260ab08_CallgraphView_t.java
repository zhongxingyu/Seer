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
 
 import java.util.HashMap;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.draw2d.parts.ScrollableThumbnail;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.linuxtools.callgraph.core.PluginConstants;
 import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
 import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;
 import org.eclipse.linuxtools.callgraph.core.SystemTapView;
 import org.eclipse.linuxtools.callgraph.graphlisteners.AutoScrollSelectionListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
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
 
 /**
  *	The SystemTap View for displaying output of the 'stap' command, and acts
  *	as a container for any graph to be rendered. Any buttons/controls/actions
  *	necessary to the smooth running of SystemTap could be placed here.
  */
 public class CallgraphView extends SystemTapView {
 
 	private StapGraphParser parser;
 
 
 	private Action view_treeview;
 	private Action view_radialview;
 	private  Action view_aggregateview;
 	private  Action view_levelview;	
 	private  Action view_refresh;	
 	private  Action animation_slow;
 	private  Action animation_fast;
 	private  Action mode_collapsednodes;
 	private  Action markers_next; 
 	private  Action markers_previous;
 	private  Action limits; 
 	private  Action goto_next;
 	private  Action goto_previous;
 	private  Action goto_last;
 	private  Action play;
 	ImageDescriptor playImage= ImageDescriptor.createFromImage(
 			new Image(Display.getCurrent(), CallGraphConstants.getPluginLocation() + "icons/perform.png")); //$NON-NLS-1$
 	ImageDescriptor pauseImage= ImageDescriptor.createFromImage(
 			new Image(Display.getCurrent(), CallGraphConstants.getPluginLocation() + "icons/pause.gif")); //$NON-NLS-1$
 	
 	private  IMenuManager menu;
 	private  IMenuManager gotoMenu;
 	private  IMenuManager view;
 	private  IMenuManager animation;
 	private  IMenuManager markers; //Unused
 	public  IToolBarManager mgr;
 	
 	private  Composite graphComp;
 	private  Composite treeComp;
 	
 	private  StapGraph g;
 	private  int treeSize = 200;
 
 
 	
 	/**
 	 * Initializes the view by creating composites (if necessary) and canvases
 	 * Calls loadData(), and calls finishLoad() if not in realTime mode (otherwise
 	 * it is up to the user-defined update methods to finish loading).
 	 * 
 	 * @return status
 	 * 
 	 */
 	public IStatus initializeView(Display targetDisplay, IProgressMonitor monitor) {
 		
 		Display disp = targetDisplay;
 		if (disp == null)
 			disp = Display.getCurrent();
 		if (disp == null)
 			disp = Display.getDefault();
 
 		treeSize = 200;
 		makeTreeComp(treeSize);
 		makeGraphComp();
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
 		Image image = new Image(disp, CallGraphConstants.getPluginLocation()+"icons/up.gif"); //$NON-NLS-1$
 		Button up = new Button(papaCanvas, SWT.PUSH);
 		GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
 		buttonData.widthHint = 150;
 		buttonData.heightHint = 20;
 		up.setData(buttonData);
 		up.setImage(image);
 		up.setToolTipText(Messages.getString("CallgraphView.ThumbNailUp")); //$NON-NLS-1$
 		
 		
 		//Add thumb canvas
 		Canvas thumbCanvas = new Canvas(papaCanvas, SWT.NONE);
 		
 		
 		//Add second button
 		image = new Image(disp, CallGraphConstants.getPluginLocation()+"icons/down.gif"); //$NON-NLS-1$
 		Button down = new Button(papaCanvas, SWT.PUSH);
 		buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
 		buttonData.widthHint = 150;
 		buttonData.heightHint = 0;
 		down.setData(buttonData);
 		down.setImage(image);
 		down.setToolTipText(Messages.getString("CallgraphView.ThumbNailDown")); //$NON-NLS-1$
 
 		
 		//Initialize graph
 		g = new StapGraph(graphComp, SWT.BORDER, treeComp, papaCanvas, this);
 		g.setLayoutData(new GridData(masterComposite.getBounds().width,Display.getCurrent().getBounds().height - treeSize));
 
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
 
 		loadData(monitor);
 		if (!parser.isRealTime())
 			return finishLoad(monitor);
 			
 		finishLoad(monitor);
 		
 
 		return Status.OK_STATUS;
 	}
 	
 	/**
 	 * Load data.
 	 * @param mon -- Progress monitor.
 	 * @return
 	 */
 	private IStatus loadData(IProgressMonitor mon) {
 		IProgressMonitor monitor = mon;
 		//Dummy node, set start time
 		if (g.getNodeData(0) == null) {
 			g.loadData(SWT.NONE, 0, StapGraph.CONSTANT_TOP_NODE_NAME, 
 					1, 1, -1, false, ""); //$NON-NLS-1$
 		}
 		g.setStartTime(parser.startTime);
 		g.setEndTime(parser.endingTimeInNS);
 
 		
 		/*
 		 *                Load graph data
 		 */
 		boolean marked = false;
 		String msg = ""; //$NON-NLS-1$
 	    for (int id_parent : parser.serialMap.keySet()) {
 	    	if (g.getNodeData(id_parent) == null) {
 				if (parser.markedMap.get(id_parent) != null) {
 					marked = true;
 					msg = parser.markedMap.remove(id_parent);
 				}
 	    		g.loadData(SWT.NONE, id_parent, parser.serialMap.get(id_parent), parser.timeMap.get(id_parent),
 	    				1, 0, marked, msg);
 	    	}
 	    	
 			for (int id_child : parser.outNeighbours.get(id_parent)) {
 				if (g.getNodeData(id_child) != null)
 					continue;
 				if (monitor.isCanceled()) {
 					return Status.CANCEL_STATUS;
 				}
 				
 				marked = false;
 				msg = ""; //$NON-NLS-1$
 				if (parser.markedMap.get(id_child) != null) {
 					marked = true;
 					msg = parser.markedMap.remove(id_child);
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
 	    
 	    monitor.worked(1);
 	    if (parser.markedMap.size() > 0) {
 	    	//Still some markers left
 	    	for (int key : parser.markedMap.keySet()) {
 	    		g.insertMessage(key, parser.markedMap.get(key));
 	    	}
 	    	
 	    	//Erase the remaining nodes, just in case
 	    	parser.markedMap.clear();
 	    }
 	    
 	    
 	    if (g.aggregateTime == null)
 	    	g.aggregateTime = new HashMap<String, Long>();
 		if (g.aggregateCount == null)
 	    	g.aggregateCount = new HashMap<String, Integer>();	    
 	    
 	    g.aggregateCount.putAll(parser.countMap);
 	    g.aggregateTime.putAll(parser.aggregateTimeMap);
 	    g.setLastFunctionCalled(parser.lastFunctionCalled);
 
 	    
 	    //Finish off by collapsing nodes, initializing the tree and setting options
 	    g.recursivelyCollapseAllChildrenOfNode(g.getTopNode());
 	    monitor.worked(1);
 		setGraphOptions(true);
 	    g.initializeTree();
 
 	    return Status.OK_STATUS;
 	}
 	
 	/**
 	 * Completes the loading process by calculating aggregate data.
 	 * 
 	 * @param monitor
 	 * @return
 	 */
 	private IStatus finishLoad(IProgressMonitor monitor) {
 
 		if (g.aggregateCount == null)
 	    	g.aggregateCount = new HashMap<String, Integer>();
 	    
 	    g.aggregateCount.putAll(parser.countMap);
 	    
 	    if (g.aggregateTime == null)
 	    	g.aggregateTime = new HashMap<String, Long>();
 	    g.aggregateTime.putAll(parser.aggregateTimeMap);
 
 	    //Set total time
 	    if (parser.totalTime != -1)
 	    	g.setTotalTime(parser.totalTime);
 		
 	    //-------------Finish initializations
 	    //Generate data for collapsed nodes
 		if (monitor.isCanceled()) {
 			return Status.CANCEL_STATUS;
 		}
 	    g.initializeTree();
 	    
 
 		if (monitor.isCanceled()) {
 			return Status.CANCEL_STATUS;
 		}
 	    g.setCallOrderList(parser.callOrderList);
 	    
 	    g.setProject(parser.project);
 	    
 	    this.initializePartControl();
 		return Status.OK_STATUS;
 	}
 	
 	
 	/**
 	 * Enable or Disable the graph options
 	 * @param visible
 	 */
 	public  void setGraphOptions (boolean visible){
 		play.setEnabled(visible);
 		save_file.setEnabled(visible);
 		view_treeview.setEnabled(visible);
 		view_radialview.setEnabled(visible);
 		view_aggregateview.setEnabled(visible);
 		view_levelview.setEnabled(visible);
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
 
 	
 	
 	public  void makeTreeComp(int treeSize) {
 		if (treeComp != null && !treeComp.isDisposed()) {
 			treeComp.dispose();
 		}
 		
 		treeComp = new Composite(this.masterComposite, SWT.NONE);
 		GridData treegd = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
 		treegd.widthHint = treeSize;
 		treeComp.setLayout(new FillLayout());
 		treeComp.setLayoutData(treegd);
 	}
 	
 	public  void makeGraphComp() {
 		if (graphComp != null && !graphComp.isDisposed()) {
 			graphComp.dispose();
 		}
 		graphComp = new Composite(this.masterComposite, SWT.NONE);
 		GridData graphgd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		GridLayout gl = new GridLayout(2, false);
 		gl.horizontalSpacing=0;
 		gl.verticalSpacing=0;
 		
 		graphComp.setLayout(gl);
 		graphComp.setLayoutData(graphgd);
 	}
 
 
 	/**
 	 * This must be executed before a Graph is displayed
 	 */
 	private void initializePartControl(){
 		setGraphOptions(true);
 		if (graphComp == null)
 			return;
 		graphComp.setParent(masterComposite);
 		
 		if (treeComp != null)
 			treeComp.setParent(masterComposite);
 
 		graphComp.setSize(masterComposite.getSize().x ,masterComposite.getSize().y);
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
 
 		// LOAD ALL ACTIONS
 		createActions();
 		
 		//MENU FOR SYSTEMTAP BUTTONS
 		mgr = getViewSite().getActionBars().getToolBarManager();
 		
 		
 		//MENU FOR SYSTEMTAP GRAPH OPTIONS
 		menu = getViewSite().getActionBars().getMenuManager();
 		
 		// ADD OPTIONS TO THE GRAPH MENU
 		addFileMenu();
 		view = new MenuManager(Messages.getString("CallgraphView.1")); //$NON-NLS-1$
 		animation = new MenuManager(Messages.getString("CallgraphView.2")); //$NON-NLS-1$
 		markers = new MenuManager(Messages.getString("CallgraphView.6")); //$NON-NLS-1$
 		gotoMenu = new MenuManager(Messages.getString("CallgraphView.9")); //$NON-NLS-1$
 		menu.add(view);
 //		menu.add(animation);	
 		menu.add(gotoMenu);
 		addErrorMenu();
 		addHelpMenu();
 		
 		view.add(view_treeview);
 		view.add(view_radialview);
 		view.add(view_aggregateview);
 		view.add(view_levelview);
 		view.add(getView_refresh());
 		view.add(mode_collapsednodes);
 		view.add(limits);
 		
 		
 		gotoMenu.add(play);
 		gotoMenu.add(goto_previous);
 		gotoMenu.add(goto_next);
 		gotoMenu.add(goto_last);
 		
 		addKillButton();
 		mgr.add(play);
 		mgr.add(view_radialview);
 		mgr.add(view_treeview);
 		mgr.add(view_levelview);
 		mgr.add(view_aggregateview);
 		mgr.add(mode_collapsednodes);
 		
 //		help.add(help_about);
 		
 		markers.add(markers_next);
 		markers.add(markers_previous);
 		
 		animation.add(animation_slow);
 		animation.add(animation_fast);
 //		menu.add(markers);
 
 		setGraphOptions(false);
 	}
 
 
 	
 	public void createViewActions() {
 		//Set drawmode to tree view
 		view_treeview = new Action(Messages.getString("CallgraphView.16")){ //$NON-NLS-1$
 			public void run() {
 				g.draw(StapGraph.CONSTANT_DRAWMODE_TREE, g.getAnimationMode(), 
 						g.getRootVisibleNodeNumber());
 				g.scrollTo(g.getNode(g.getRootVisibleNodeNumber()).getLocation().x
 						- g.getBounds().width / 2, g.getNode(
 						g.getRootVisibleNodeNumber()).getLocation().y);
 				if (play != null)
 					play.setEnabled(true);
 			}
 		};
 		ImageDescriptor treeImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), CallGraphConstants.getPluginLocation() + "icons/tree_view.gif")); //$NON-NLS-1$
 		view_treeview.setImageDescriptor(treeImage);
 		
 		
 		//Set drawmode to radial view
 		view_radialview = new Action(Messages.getString("CallgraphView.17")){ //$NON-NLS-1$
 			public void run(){
 				g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, g.getAnimationMode(),
 						g.getRootVisibleNodeNumber());
 				if (play != null)
 					play.setEnabled(true);
 			}
 		};
 		ImageDescriptor d = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.getPluginLocation() + "/icons/radial_view.gif")); //$NON-NLS-1$
 		view_radialview.setImageDescriptor(d);
 
 		
 		//Set drawmode to aggregate view
 		view_aggregateview = new Action(Messages.getString("CallgraphView.18")){ //$NON-NLS-1$
 			public void run(){
 				g.draw(StapGraph.CONSTANT_DRAWMODE_AGGREGATE, g.getAnimationMode(), 
 						g.getRootVisibleNodeNumber());
 				if (play != null)
 					play.setEnabled(false);
 			}
 		};
 		ImageDescriptor aggregateImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.getPluginLocation() + "/icons/view_aggregateview.gif")); //$NON-NLS-1$
 		view_aggregateview.setImageDescriptor(aggregateImage);
 		
 		
 		//Set drawmode to level view
 		view_levelview = new Action(Messages.getString("CallgraphView.19")){ //$NON-NLS-1$
 			public void run(){
 				g.draw(StapGraph.CONSTANT_DRAWMODE_LEVEL, g.getAnimationMode(), 
 						g.getRootVisibleNodeNumber());
 				if (play != null)
 					play.setEnabled(true);
 			}
 		};
 		ImageDescriptor levelImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.getPluginLocation() + "/icons/showchild_mode.gif")); //$NON-NLS-1$
 		view_levelview.setImageDescriptor(levelImage);
 		
 		
 		setView_refresh(new Action(Messages.getString("CallgraphView.Reset")){ //$NON-NLS-1$
 			public void run(){
 				g.reset();
 			}
 		});
 		ImageDescriptor refreshImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), 
 						CallGraphConstants.getPluginLocation() + "/icons/nav_refresh.gif")); //$NON-NLS-1$
 		getView_refresh().setImageDescriptor(refreshImage);
 		
 		
 	}
 	
 
 	/**
 	 * Populates Animate menu.
 	 */
 	public void createAnimateActions() {
 		//Set animation mode to slow
 		animation_slow = new Action(Messages.getString("CallgraphView.20"), Action.AS_RADIO_BUTTON){ //$NON-NLS-1$
 			public void run(){
 				g.setAnimationMode(StapGraph.CONSTANT_ANIMATION_SLOW);
 				this.setChecked(true);
 				animation_slow.setChecked(true);
 				animation_fast.setChecked(false);
 			}
 		};
 		
 		animation_slow.setChecked(true);
 		
 		//Set animation mode to fast
 		animation_fast = new Action(Messages.getString("CallgraphView.22"), Action.AS_RADIO_BUTTON){ //$NON-NLS-1$
 			public void run(){
 				g.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
 				animation_slow.setChecked(false);
 				animation_fast.setChecked(true);
 			}
 		};
 		
 		//Toggle collapse mode
 		mode_collapsednodes = new Action(Messages.getString("CallgraphView.24"), Action.AS_CHECK_BOX){ //$NON-NLS-1$
 			public void run(){
 				
 				if (g.isCollapseMode()) {
 					g.setCollapseMode(false);
 					g.draw(g.getRootVisibleNodeNumber());
 				}
 				else {
 					g.setCollapseMode(true);
 					g.draw(g.getRootVisibleNodeNumber());
 				}
 			}
 		};
 		
 		ImageDescriptor newImage = ImageDescriptor.createFromImage(
 				new Image(Display.getCurrent(), CallGraphConstants.getPluginLocation() + "icons/mode_collapsednodes.gif")); //$NON-NLS-1$
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
 				limit.setSelection(g.getMaxNodes());
 				limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				
 				Label bufferLabel = new Label(sh, SWT.NONE);
 				bufferLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				bufferLabel.setText(Messages.getString("CallgraphView.MaxDepth")); //$NON-NLS-1$
 				buffer = new Spinner(sh, SWT.BORDER);
 				buffer.setMaximum(5000);
 				buffer.setSelection(g.getLevelBuffer());
 				buffer.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				
 				Button set_limit = new Button(sh, SWT.PUSH);
 				set_limit.setText(Messages.getString("CallgraphView.SetValues")); //$NON-NLS-1$
 				set_limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
 				set_limit.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						boolean redraw = false;
 						if (limit.getSelection() > 0 && buffer.getSelection() > 0) {
 							g.setMaxNodes(limit.getSelection());
 							g.setLevelBuffer(buffer.getSelection());
 							
 							if (g.changeLevelLimits(g.getLevelOfNode(g.getRootVisibleNodeNumber()))) {
 								SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
 										Messages.getString("CallgraphView.BufferTooHigh"), Messages.getString("CallgraphView.BufferTooHigh"),  //$NON-NLS-1$ //$NON-NLS-2$
 										Messages.getString("CallgraphView.BufferMessage1") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage2") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage3") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage4") + g.getLevelBuffer() + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage5") + PluginConstants.NEW_LINE + PluginConstants.NEW_LINE +   //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage6") + //$NON-NLS-1$
 										Messages.getString("CallgraphView.BufferMessage7")); //$NON-NLS-1$
 								mess.schedule();
 							}
 							
 							redraw = true;
 						}
 						sh.dispose();
 						
 						if (redraw)
 							g.draw();
 					}
 					
 				});
 
 				
 				sh.open();			}
 		};
 
 	}
 
 /**
  * Convenience method for creating all the various actions
  */
 	public void createActions() {
 		createViewActions();
 		createAnimateActions();
 		createMarkerActions();		
 		createMovementActions();
 
 		mode_collapsednodes.setChecked(true);
 		
 	}
 	
 	public void createMovementActions() {
 		goto_next = new Action(Messages.getString("CallgraphView.Next")) { //$NON-NLS-1$
 			public void run() {
 				g.drawNextNode();
 			}
 		};
 		
 		goto_previous = new Action(Messages.getString("CallgraphView.Previous")) { //$NON-NLS-1$
 			public void run() {
 				if (g.isCollapseMode()) {
 					g.setCollapseMode(false);
 				}
 				int toDraw = g.getPreviousCalledNode(g.getRootVisibleNodeNumber());
 				if (toDraw != -1)
 					g.draw(toDraw);
 			}
 		};
 		
 		goto_last = new Action(Messages.getString("CallgraphView.Last")) { //$NON-NLS-1$
 			public void run() {
 				if (g.isCollapseMode())
 					g.setCollapseMode(false);
 				g.draw(g.getLastFunctionCalled());
 			}
 		};
 		
 		play = new Action(Messages.getString("CallgraphView.0")) { //$NON-NLS-1$
 			public void run() {
 				if (g.getDrawMode() != StapGraph.CONSTANT_DRAWMODE_AGGREGATE) {
 					g.play();
 					togglePlayImage();
 				}
 			}
 		};
 		play.setImageDescriptor(playImage);
 	}
 	
 	/**
 	 * Toggles the play/pause image
 	 * @param play
 	 */
 	protected void togglePlayImage() {
 		if (play.getToolTipText() == Messages.getString("CallgraphView.3")) { //$NON-NLS-1$
 			play.setImageDescriptor(playImage);
 			play.setToolTipText(Messages.getString("CallgraphView.0")); //$NON-NLS-1$
 		}
 		else {
 			play.setImageDescriptor(pauseImage);
 			play.setToolTipText(Messages.getString("CallgraphView.3")); //$NON-NLS-1$
 		}
 	}
 	
 	public void createMarkerActions() {
 		markers_next = new Action(Messages.getString("CallgraphView.nextMarker")) { //$NON-NLS-1$
 			public void run() {
 				g.draw(g.getNextMarkedNode());
 			}
 		};
 		
 		markers_previous = new Action(Messages.getString("CallgraphView.previousMarker")) { //$NON-NLS-1$
 			public void run() {
 				g.draw(g.getPreviousMarkedNode());
 			}
 		};
 	}
 	
 	@Override
 	protected boolean createOpenAction() {
 		//Opens from specified location
 		open_file = new Action(Messages.getString("CallgraphView.7")){ //$NON-NLS-1$
 			public void run(){
 				try {
 				FileDialog dialog = new FileDialog(new Shell(), SWT.DEFAULT);
 				String filePath =  dialog.open();
 				if (filePath != null){
 					StapGraphParser new_parser = new StapGraphParser();
 					new_parser.setSourcePath(filePath);
 						new_parser.setViewID(CallGraphConstants.viewID);
 					new_parser.schedule();					
 				}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		};	
 		return true;
 	}
 
 
 	@Override
 	protected boolean createOpenDefaultAction() {
 		//Opens from the default location
 		open_default = new Action(Messages.getString("CallgraphView.11")){ //$NON-NLS-1$
 			public void run(){
 				try {
 				StapGraphParser new_parser = new StapGraphParser();
 				new_parser.setViewID(CallGraphConstants.viewID);
 				new_parser.schedule();					
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		
 		return true;
 	}
 	
 	@Override
 	public boolean setParser(SystemTapParser newParser) {
 		if (newParser instanceof StapGraphParser) {
 			parser = (StapGraphParser) newParser;
 			return true;
 		}
 		return false;
 		
 	}
 
 	@Override
 	public void setViewID() {
 		viewID = "org.eclipse.linuxtools.callgraph.callgraphview";		 //$NON-NLS-1$
 	}
 
 	
 
 	public  Action getAnimation_slow() {
 		return animation_slow;
 	}
 
 	public  void setAnimation_slow(Action animation_slow) {
 		this.animation_slow = animation_slow;
 	}
 
 	public  Action getAnimation_fast() {
 		return animation_fast;
 	}
 
 	public  void setAnimation_fast(Action animation_fast) {
 		this.animation_fast = animation_fast;
 	}
 
 	public  IMenuManager getAnimation() {
 		return animation;
 	}
 
 	public  void setAnimation(IMenuManager animation) {
 		this.animation = animation;
 	}
 
 	public  Action getMode_collapsednodes() {
 		return mode_collapsednodes;
 	}
 
 	public  void setMode_collapsednodes(Action mode_collapsednodes) {
 		this.mode_collapsednodes = mode_collapsednodes;
 	}
 
 	public  void setView_refresh(Action view_refresh) {
 		this.view_refresh = view_refresh;
 	}
 
 	public  Action getView_refresh() {
 		return view_refresh;
 	}
 
 	public  Action getGoto_next() {
 		return goto_next;
 	}
 
 	public  void setGoto_next(Action gotoNext) {
 		goto_next = gotoNext;
 	}
 
 	public  Action getGoto_previous() {
 		return goto_previous;
 	}
 
 	public  void setGoto_parent(Action gotoParent) {
 		goto_previous = gotoParent;
 	}
 
 	public  Action getGoto_last() {
 		return goto_last;
 	}
 
 	public  void setGoto_last(Action gotoLast) {
 		goto_last = gotoLast;
 	}
 
 	public  Action getView_treeview() {
 		return view_treeview;
 	}
 
 	public  void setView_treeview(Action viewTreeview) {
 		view_treeview = viewTreeview;
 	}
 
 	public  Action getView_radialview() {
 		return view_radialview;
 	}
 
 	public  void setView_radialview(Action viewRadialview) {
 		view_radialview = viewRadialview;
 	}
 
 	public  Action getView_aggregateview() {
 		return view_aggregateview;
 	}
 
 	public  void setView_aggregateview(Action viewAggregateview) {
 		view_aggregateview = viewAggregateview;
 	}
 
 	public  Action getView_levelview() {
 		return view_levelview;
 	}
 
 	public  void setView_levelview(Action viewlevelview) {
 		view_levelview = viewlevelview;
 	}
 
 	public  void setGoto_previous(Action gotoPrevious) {
 		goto_previous = gotoPrevious;
 	}
 	
 	public  Action getPlay() {
 		return play;
 	}
 	
 	public  StapGraph getGraph() {
 		return g;
 	}
 	
 	@Override
 	public void setFocus() {
 	}
 
 
 	@Override
 	public void updateMethod() {
 		IProgressMonitor m = new NullProgressMonitor();
		m.beginTask("Updating callgraph", 4); //$NON-NLS-1$
 		
 		loadData(m);
 		m.worked(1);
 		if (parser.totalTime > 0) {
 			finishLoad(m);
 		}
 		m.worked(1);
 		
 		g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, StapGraph.CONSTANT_ANIMATION_SLOW, g.getFirstUsefulNode());
 	}
 
 	@Override
 	public SystemTapParser getParser() {
 		return parser;
 	}
 
 
 }
