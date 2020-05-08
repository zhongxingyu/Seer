 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.ui.eclipse.editors;
 
 import java.text.Collator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.MenuDetectEvent;
 import org.eclipse.swt.events.MenuDetectListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.EditorInput;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMRoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterMember;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComChannelPacketMetaData;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Coordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.CoordinatorAsClusterMember;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 /**
  * The HRM viewer, which depicts all information from an HRM controller.
  * 
  */
 public class HRMViewer extends EditorPart implements Observer, Runnable, IEvent
 {
 	private static boolean HRM_VIEWER_DEBUGGING = HRMConfig.DebugOutput.GUI_SHOW_VIEWER_STEPS;
 	private static boolean HRM_VIEWER_SHOW_SINGLE_ENTITY_CLUSTERING_CONTROLS = true;
 	private static boolean HRM_VIEWER_SHOW_SINGLE_ENTITY_ELECTION_CONTROLS = true;
 	private static boolean HRM_VIEWER_SHOW_ALWAYS_ALL_CLUSTERS = true;
 	
 	private static final boolean GUI_SHOW_COLORED_BACKGROUND_FOR_CONTROL_ENTITIES = true;
 
 	private HRMController mHRMController = null;
     private Composite mShell = null;
     private ScrolledComposite mScroller = null;
     private Composite mContainer = null;
     private Display mDisplay = null;
     private Composite mContainerRoutingTable = null;
 	private Composite mContainerHRMID2L2ADDRTable = null;
 	
 	private boolean mShowClusterMembers = false;
 	private boolean mShowCoordinatorAsClusterMembers = false;
 	
     /**
      * Stores the simulation time for the next GUI update.
      */
     private double mTimeNextGUIUpdate = 0;
     
 	public HRMViewer()
 	{
 		
 	}
 	
 	/**
 	 * Resets all parts of the EditorPart
 	 */
 	private void destroyPartControl()
 	{
 		mContainer.dispose();
 
 		//HINT: don't dispose the mScroller object here, this would lead to GUI display problems
 		
 		mShell.redraw();
 	}
 	
 	/**
 	 * Assigns a hide/show menu for a given composite object
 	 * 
 	 * @param pComposite the composite for which the context menu should be available
 	 */
 	private void assignContextMenu(final Composite pComposite)
 	{
 		pComposite.addMenuDetectListener(new MenuDetectListener()
 		{
 			@Override
 			public void menuDetected(MenuDetectEvent pEvent)
 			{
 				/**
 				 * Create the context menu
 				 */
 				Menu tMenu = new Menu(pComposite);
 				MenuItem tMenuItem = new MenuItem(tMenu, SWT.NONE);
 				if (mShowClusterMembers){
 					tMenuItem.setText("Hide cluster members");
 				}else{
 					tMenuItem.setText("Show cluster members");
 				}
 				tMenuItem.addSelectionListener(new SelectionListener() {
 					public void widgetDefaultSelected(SelectionEvent pEvent)
 					{
 						mShowClusterMembers = !mShowClusterMembers;
 						startGUIUpdateTimer();
 					}
 					public void widgetSelected(SelectionEvent pEvent)
 					{
 						mShowClusterMembers = !mShowClusterMembers;
 						startGUIUpdateTimer();
 					}
 				});
 				MenuItem tMenuItem1 = new MenuItem(tMenu, SWT.NONE);
				if (mShowClusterMembers){
 					tMenuItem1.setText("Hide coordinators as cluster members");
 				}else{
 					tMenuItem1.setText("Show coordinators as cluster members");
 				}
 				tMenuItem1.addSelectionListener(new SelectionListener() {
 					public void widgetDefaultSelected(SelectionEvent pEvent)
 					{
 						mShowCoordinatorAsClusterMembers = !mShowCoordinatorAsClusterMembers;
 						startGUIUpdateTimer();
 					}
 					public void widgetSelected(SelectionEvent pEvent)
 					{
 						mShowCoordinatorAsClusterMembers = !mShowCoordinatorAsClusterMembers;
 						startGUIUpdateTimer();
 					}
 				});
 				MenuItem tMenuItem2 = new MenuItem(tMenu, SWT.NONE);
 				if (Coordinator.USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 					tMenuItem2.setText("Deactivate coordinator announcements");
 				}else{
 					tMenuItem2.setText("Activate coordinator announcements");
 				}
 				tMenuItem2.addSelectionListener(new SelectionListener() {
 					public void widgetDefaultSelected(SelectionEvent pEvent)
 					{
 						Coordinator.USER_CTRL_COORDINATOR_ANNOUNCEMENTS = !Coordinator.USER_CTRL_COORDINATOR_ANNOUNCEMENTS;
 						startGUIUpdateTimer();
 					}
 					public void widgetSelected(SelectionEvent pEvent)
 					{
 						Coordinator.USER_CTRL_COORDINATOR_ANNOUNCEMENTS = !Coordinator.USER_CTRL_COORDINATOR_ANNOUNCEMENTS;
 						startGUIUpdateTimer();
 					}
 				});
 				pComposite.setMenu(tMenu);
 			}
 		});
 	}
 	
 	/**
 	 * Creates all needed parts of the EditorPart.
 	 * 
 	 * @param pParent the parent shell
 	 */
 	@Override
 	public void createPartControl(Composite pParent)
 	{
 		// get the HRS instance
 		HRMRoutingService tHRS = mHRMController.getHRS();
 
 		mShell = pParent;
 		mDisplay = pParent.getDisplay();
 		mShell.setLayout(new FillLayout());
 		if (mScroller == null){
 			mScroller = new ScrolledComposite(mShell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		}
 		mContainer = new Composite(mScroller, SWT.NONE);
 		mScroller.setContent(mContainer);
 		GridLayout tLayout = new GridLayout(1, true);
 		mContainer.setLayout(tLayout);
 		
 		if (HRM_VIEWER_DEBUGGING){
 			Logging.log(this, "Found clusters: " + mHRMController.getAllClusters().size());
 			Logging.log(this, "Found coordinators: " + mHRMController.getAllCoordinators().size());
 		}
 
 		/**
 		 * GUI part 0: list clusters
 		 */
 		for (Cluster tCluster: mHRMController.getAllClusters()) {
 			// show only those cluster which also have a coordinator
 			if((HRM_VIEWER_SHOW_ALWAYS_ALL_CLUSTERS) || (tCluster.hasLocalCoordinator())){
 				// print info. about cluster
 				printClusterMember(tCluster);
 			}
 		}
 
 		/**
 		 * GUI part: list cluster members
 		 */
 		if (mShowClusterMembers){
 			for(ClusterMember tClusterMemeber : mHRMController.getAllClusterMembers()){
 				if (!(tClusterMemeber instanceof Cluster)){
 					// print info. about cluster
 					printClusterMember(tClusterMemeber);
 				}
 			}
 		}
 		
 		/**
 		 * GUI part: list coordinator as cluster members
 		 */
 		if(mShowCoordinatorAsClusterMembers){
 			for(CoordinatorAsClusterMember tCoordinatorAsClusterMember : mHRMController.getAllCoordinatorAsClusterMemebers()){
 				// print info. about cluster
 				printClusterMember(tCoordinatorAsClusterMember);
 			}
 		}
 		/**
 		 * GUI part 1: list coordinators
 		 */
 		for (Coordinator tCoordinator: mHRMController.getAllCoordinators()) {
 			// print info. about cluster
 			printCoordinator(tCoordinator);
 		}
 
 		/**
 		 * GUI part 2: table of known mappings from HRMID to L2Addresses
 		 */
 		// create the headline
 		StyledText tSignaturesLabel4 = new StyledText(mContainer, SWT.BORDER);
 		tSignaturesLabel4.setText("Mappings from HRMID to L2Address - Node " + mHRMController.getNodeGUIName());
 		tSignaturesLabel4.setForeground(new Color(mShell.getDisplay(), 0, 0, 0));
 		tSignaturesLabel4.setBackground(new Color(mShell.getDisplay(), 222, 222, 222));
 	    StyleRange style4 = new StyleRange();
 	    style4.start = 0;
 	    style4.length = tSignaturesLabel4.getText().length();
 	    style4.fontStyle = SWT.BOLD;
 	    tSignaturesLabel4.setStyleRange(style4);
 	    
 	    // create the GUI container
 	    mContainerHRMID2L2ADDRTable = new Composite(mContainer, SWT.NONE);
 	    GridData tLayoutDataMappingTable = new GridData(SWT.FILL, SWT.FILL, true, true);
 	    tLayoutDataMappingTable.horizontalSpan = 1;
 	    mContainerHRMID2L2ADDRTable.setLayoutData(tLayoutDataMappingTable); 
 	    
 	    // create the table
 		final Table tTableMappingTable = new Table(mContainerHRMID2L2ADDRTable, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		tTableMappingTable.setHeaderVisible(true);
 		tTableMappingTable.setLinesVisible(true);
 		
 		// create the columns and define the texts for the header row
 		// col. 0
 		TableColumn tTableHRMID = new TableColumn(tTableMappingTable, SWT.NONE, 0);
 		tTableHRMID.setText("HRMID");
 		// col. 1
 		TableColumn tTableL2Addr = new TableColumn(tTableMappingTable, SWT.NONE, 1);
 		tTableL2Addr.setText("L2 address");
 		
 		HashMap<HRMID, L2Address> tHRMIDToL2AddressMapping = tHRS.getHRMIDToL2AddressMapping();
 		if (HRM_VIEWER_DEBUGGING){
 			Logging.log(this, "Found " + tHRMIDToL2AddressMapping.keySet().size() + " HRMID-to-L2Address mappings");
 		}
 
 		if ((tHRMIDToL2AddressMapping != null) && (!tHRMIDToL2AddressMapping.isEmpty())) {
 			int tRowNumber = 0;
 			for(HRMID tAddr : tHRMIDToL2AddressMapping.keySet()) {
 				L2Address tL2Addr = tHRMIDToL2AddressMapping.get(tAddr);
 				
 				// create the table row
 				TableItem tTableRow = new TableItem(tTableMappingTable, SWT.NONE, tRowNumber);
 				
 				/**
 				 * Column 0: the neighbor name
 				 */
 				tTableRow.setText(0, tAddr.toString());
 
 				/**
 				 * Column 1: route 
 				 */
 				tTableRow.setText(1, tL2Addr.toString());
 
 				tRowNumber++;
 			}
 		}
 		
 		TableColumn[] columns4 = tTableMappingTable.getColumns();
 		for (int k = 0; k < columns4.length; k++){
 			columns4[k].pack();
 		}
 		tTableMappingTable.setLayoutData(new GridData(GridData.FILL_BOTH));//SWT.FILL, SWT.TOP, true, true, 1, 1));
 
 		// create the container layout
 		TableColumnLayout tLayoutMappingTable = new TableColumnLayout();
 		mContainerHRMID2L2ADDRTable.setLayout(tLayoutMappingTable);
 		// assign each column a layout weight
 		tLayoutMappingTable.setColumnData(tTableHRMID, new ColumnWeightData(3));
 		tLayoutMappingTable.setColumnData(tTableL2Addr, new ColumnWeightData(3));
 
 		/**
 		 * GUI part 2: HRM routing table of the node
 		 */
 		// create the headline
 		StyledText tSignaturesLabel2 = new StyledText(mContainer, SWT.BORDER);
 		tSignaturesLabel2.setText("HRM Routing Table - Node " + mHRMController.getNodeGUIName());
 		tSignaturesLabel2.setForeground(new Color(mShell.getDisplay(), 0, 0, 0));
 		tSignaturesLabel2.setBackground(new Color(mShell.getDisplay(), 222, 222, 222));
 	    StyleRange style3 = new StyleRange();
 	    style3.start = 0;
 	    style3.length = tSignaturesLabel2.getText().length();
 	    style3.fontStyle = SWT.BOLD;
 	    tSignaturesLabel2.setStyleRange(style3);
 	    
 	    // create the GUI container
 	    mContainerRoutingTable = new Composite(mContainer, SWT.NONE);
 	    GridData tLayoutDataRoutingTable = new GridData(SWT.FILL, SWT.FILL, true, true);
 	    tLayoutDataRoutingTable.horizontalSpan = 1;
 	    mContainerRoutingTable.setLayoutData(tLayoutDataRoutingTable); 
 	    
 	    // create the table
 		final Table tTableRoutingTable = new Table(mContainerRoutingTable, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		tTableRoutingTable.setHeaderVisible(true);
 		tTableRoutingTable.setLinesVisible(true);
 		
 		// create the columns and define the texts for the header row
 		// col. 0
 		TableColumn tTableColDest = new TableColumn(tTableRoutingTable, SWT.NONE, 0);
 		tTableColDest.setText("Dest.");
 		// col. 1
 		TableColumn tTableColNext = new TableColumn(tTableRoutingTable, SWT.NONE, 1);
 		tTableColNext.setText("Next hop");
 		// col. 2
 		TableColumn tTableColHops = new TableColumn(tTableRoutingTable, SWT.NONE, 2);
 		tTableColHops.setText("Hops");
 		// col. 3
 		TableColumn tTableColUtil = new TableColumn(tTableRoutingTable, SWT.NONE, 3);
 		tTableColUtil.setText("Util. [%]");
 		// col. 4
 		TableColumn tTableColDelay = new TableColumn(tTableRoutingTable, SWT.NONE, 4);
 		tTableColDelay.setText("MinDelay [ms]");
 		// col. 5
 		TableColumn tTableColDR = new TableColumn(tTableRoutingTable, SWT.NONE, 5);
 		tTableColDR.setText("MaxDR [Kb/s]");
 		// col. 6
 		TableColumn tTableColLoop = new TableColumn(tTableRoutingTable, SWT.NONE, 6);
 		tTableColLoop.setText("Loopback?");
 		// col. 7
 		TableColumn tTableColDirectNeighbor = new TableColumn(tTableRoutingTable, SWT.NONE, 7);
 		tTableColDirectNeighbor.setText("Route to neighbor");
 		
 		LinkedList<RoutingEntry> tRoutingTable = tHRS.routingTable();
 		if (HRM_VIEWER_DEBUGGING){
 			Logging.log(this, "Found " + tRoutingTable.size() + " entries in the local routing table");
 		}
 			
 		if ((tRoutingTable != null) && (!tRoutingTable.isEmpty())) {
 			int tRowNumber = 0;
 			for(RoutingEntry tEntry : tRoutingTable) {
 				if ((HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES) || (!tEntry.getDest().isRelativeAddress())){
 					// create the table row
 					TableItem tTableRow = new TableItem(tTableRoutingTable, SWT.NONE, tRowNumber);
 					
 					/**
 					 * Column 0: destination
 					 */
 					tTableRow.setText(0, tEntry.getDest() != null ? tEntry.getDest().toString() : "");
 	
 					/**
 					 * Column 1: next hop 
 					 */
 					if (tEntry.getNextHop() != null) {
 						tTableRow.setText(1, tEntry.getNextHop().toString());
 					}else{
 						tTableRow.setText(1, "??");
 					}
 					
 					/**
 					 * Column 2: hop costs
 					 */
 					if (tEntry.getHopCount() != RoutingEntry.NO_HOP_COSTS){
 						tTableRow.setText(2, Integer.toString(tEntry.getHopCount()));
 					}else{
 						tTableRow.setText(2, "none");
 					}
 					
 					/**
 					 * Column 3:  utilization
 					 */
 					if (tEntry.getUtilization() != RoutingEntry.NO_UTILIZATION){
 						tTableRow.setText(3,  Float.toString(tEntry.getUtilization() * 100));
 					}else{
 						tTableRow.setText(3, "N/A");
 					}
 					
 					/**
 					 * Column 4: min. delay
 					 */
 					if (tEntry.getMinDelay() != RoutingEntry.NO_DELAY){					
 						tTableRow.setText(4, Long.toString(tEntry.getMinDelay()));
 					}else{
 						tTableRow.setText(4, "none");
 					}
 					
 					/**
 					 * Column 5: max. data rate
 					 */
 					if (tEntry.getMaxDataRate() != RoutingEntry.INFINITE_DATARATE){
 						tTableRow.setText(5, Long.toString(tEntry.getMaxDataRate()));				
 					}else{
 						tTableRow.setText(5, "inf.");
 					}
 					
 					/**
 					 * Column 6: loopback?
 					 */
 					if (tEntry.isLocalLoop()){
 						tTableRow.setText(6, "yes");				
 					}else{
 						tTableRow.setText(6, "no");
 					}
 	
 					/**
 					 * Column 7: direct neighbor?
 					 */
 					if (tEntry.isRouteToDirectNeighbor()){
 						tTableRow.setText(7, "yes");				
 					}else{
 						tTableRow.setText(7, "no");
 					}
 	
 					tRowNumber++;
 				}
 			}
 		}
 		
 		TableColumn[] columns2 = tTableRoutingTable.getColumns();
 		for (int k = 0; k < columns2.length; k++){
 			columns2[k].pack();
 		}
 		tTableRoutingTable.setLayoutData(new GridData(GridData.FILL_BOTH));//SWT.FILL, SWT.TOP, true, true, 1, 1));
 		
 		// create the container layout
 		TableColumnLayout tLayoutRoutingTable2 = new TableColumnLayout();
 		mContainerRoutingTable.setLayout(tLayoutRoutingTable2);
 		// assign each column a layout weight
 		tLayoutRoutingTable2.setColumnData(tTableColDest, new ColumnWeightData(3));
 		tLayoutRoutingTable2.setColumnData(tTableColNext, new ColumnWeightData(3));
 		tLayoutRoutingTable2.setColumnData(tTableColHops, new ColumnWeightData(1));
 		tLayoutRoutingTable2.setColumnData(tTableColUtil, new ColumnWeightData(1));
 		tLayoutRoutingTable2.setColumnData(tTableColDelay, new ColumnWeightData(1));
 		tLayoutRoutingTable2.setColumnData(tTableColDR, new ColumnWeightData(1));
 		tLayoutRoutingTable2.setColumnData(tTableColLoop, new ColumnWeightData(1));
 		tLayoutRoutingTable2.setColumnData(tTableColDirectNeighbor, new ColumnWeightData(1));		
 		
 		/**
 		 * Add a listener to allow re-sorting of the table based on the destination per table row
 		 */
 		tTableColDest.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				// sort column 2
 		        TableItem[] tAllRows = tTableRoutingTable.getItems();
 		        Collator collator = Collator.getInstance(Locale.getDefault());
 		        
 		        for (int i = 1; i < tAllRows.length; i++) {
 		        	String value1 = tAllRows[i].getText(1);
 		          
 		        	for (int j = 0; j < i; j++) {
 		        		String value2 = tAllRows[j].getText(1);
 		            
 		        		if (collator.compare(value1, value2) < 0) {
 							// copy table row data
 							String[] tRowData = { tAllRows[i].getText(0), tAllRows[i].getText(1) };
 							  
 							// delete table row "i"
 							tAllRows[i].dispose();
 							  
 							// create new table row
 							TableItem tRow = new TableItem(tTableRoutingTable, SWT.NONE, j);
 							tRow.setText(tRowData);
 							  
 							// update data of table rows
 							tAllRows = tTableRoutingTable.getItems();
 							  
 							break;
 		        		}
 		        	}
 		        }
 			}
 	    });
 		
 		// arrange the GUI content in order to full the entire space
         mContainer.setSize(mContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
         mContainerRoutingTable.setSize(mContainerRoutingTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
         mContainerHRMID2L2ADDRTable.setSize(mContainerHRMID2L2ADDRTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		/**
 		 * Context menu
 		 */
         assignContextMenu(mScroller);
         assignContextMenu(mContainer);
         assignContextMenu(mContainerRoutingTable);
         assignContextMenu(mContainerHRMID2L2ADDRTable);
 	}
 
 	/**
 	 * Draws GUI elements for depicting coordinator information.
 	 * 
 	 * @param pCoordinator selected coordinator 
 	 */
 	private void printCoordinator(Coordinator pCoordinator)
 	{
 		if (HRM_VIEWER_DEBUGGING)
 			Logging.log(this, "Printing coordinator \"" + pCoordinator.toString() +"\"");
 
 		/**
 		 * GUI part 0: name of the coordinator 
 		 */
 		printNAME(pCoordinator);
 
 		/**
 		 * GUI part 1: tool box 
 		 */
 		if(pCoordinator != null) {
 			ToolBar tToolbar = new ToolBar(mContainer, SWT.NONE);
 
 			if (HRM_VIEWER_SHOW_SINGLE_ENTITY_CLUSTERING_CONTROLS){
 				if(!pCoordinator.getHierarchyLevel().isHighest()) {
 					if ((pCoordinator.getCluster().getElector() != null) && (pCoordinator.getCluster().getElector().isWinner()) && (!pCoordinator.isClustered())){
 					    ToolItem toolItem3 = new ToolItem(tToolbar, SWT.PUSH);
 					    toolItem3.setText("[Create cluster]");
 					    toolItem3.addListener(SWT.Selection, new ListenerClusterHierarchy(this, pCoordinator));
 					}
 				}
 			}
 
 		    ToolItem toolItem4 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem4.setText("[Create all level " + pCoordinator.getHierarchyLevel().getValue() + " clusters]");
 		    toolItem4.addListener(SWT.Selection, new ListenerClusterHierarchyLevel(this, pCoordinator));
 		    
 		    tToolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		}
 		
 		/**
 		 * GUI part 2: table about CEPs 
 		 */
 		printComChannels(pCoordinator);
 			
 		Label separator = new Label (mContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
 		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		separator.setVisible(true);
 		
 	}
 	
 	private void printComChannels(final ControlEntity pControlEntity)
 	{
 		final Table tTable = new Table(mContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		
 		/**
 		 * The table header
 		 */
 		TableColumn tColumnCoordinator = new TableColumn(tTable, SWT.NONE, 0);
 		tColumnCoordinator.setText("Peer");
 		
 		TableColumn tColumnCEP = new TableColumn(tTable, SWT.NONE, 1);
 		tColumnCEP.setText("Peer Node");
 		
 		TableColumn tColumnTargetCovered = new TableColumn(tTable, SWT.NONE, 2);
 		tColumnTargetCovered.setText("Active link");
 		
 		TableColumn tColumnPeerPriority = new TableColumn(tTable, SWT.NONE, 3);
 		tColumnPeerPriority.setText("Peer Priority");
 		
 		TableColumn tColumnNegotiator = new TableColumn(tTable, SWT.NONE, 4);
 		tColumnNegotiator.setText("Peer End Point");
 		
 		TableColumn tColumnRoute = new TableColumn(tTable, SWT.NONE, 5);
 		tColumnRoute.setText("Route to peer");
 		
 		TableColumn tColumnDirection = new TableColumn(tTable, SWT.NONE, 6);
 		tColumnDirection.setText("Connected");
 
 		TableColumn tColumnSendPackets = new TableColumn(tTable, SWT.NONE, 7);
 		tColumnSendPackets.setText("Sent packets");
 
 		TableColumn tColumnReceivedPackets = new TableColumn(tTable, SWT.NONE, 8);
 		tColumnReceivedPackets.setText("Received packets");
 
 		tTable.setHeaderVisible(true);
 		tTable.setLinesVisible(true);
 		
 		/**
 		 * The table content
 		 */
 		LinkedList<ComChannel> tComChannels = pControlEntity.getComChannels();
 		if(pControlEntity instanceof Coordinator){
 			Coordinator tCoordinator = (Coordinator)pControlEntity;
 			
 			tComChannels = tCoordinator.getClusterMembershipComChannels();
 		}
 		
 		int j = 0;		
 		if (HRM_VIEWER_DEBUGGING)
 			Logging.log(this, "Amount of participating CEPs is " + tComChannels.size());
 		for(ComChannel tComChannel : tComChannels) {
 			if (HRM_VIEWER_DEBUGGING)
 				Logging.log(this, "Updating table item number " + j);
 			
 			// table row
 			TableItem tRow = null;
 			
 			// get reference to already existing table row
 			if (tTable.getItemCount() > j) {
 				tRow = tTable.getItem(j);
 			}				
 			
 			// create a table row if necessary
 			if (tRow == null){
 				tRow = new TableItem(tTable, SWT.NONE, j);
 			}
 			
 			/**
 			 * Column 0: peer
 			 */			
 			tRow.setText(0, ((tComChannel.getPeer() != null) ? tComChannel.getPeer().toString() : tComChannel.getRemoteClusterName().toString()));
 
 			/**
 			 * Column 1: peer node
 			 */
 			tRow.setText(1, (tComChannel.getPeerL2Address() != null ? tComChannel.getPeerL2Address().toString() : "??"));
 
 			/**
 			 * Column 2: active link 
 			 */
 			tRow.setText(2, tComChannel.getLinking() ? "yes" : "no");
 			
 			/**
 			 * Column 3:  
 			 */
 			tRow.setText(3, Float.toString(tComChannel.getPeerPriority().getValue()));
 			
 			/**
 			 * Column 4:  
 			 */
 			if (tComChannel.getRemoteClusterName() != null){
 				if ((pControlEntity.getHierarchyLevel().isHigherLevel()) && (pControlEntity instanceof Cluster)){
 					ClusterName tRemoteClusterName = tComChannel.getRemoteClusterName();
 					int tPeerLevel = tRemoteClusterName.getHierarchyLevel().getValue() - 1 /* the connected remote entity is always one level below because it is reported with an increment */;
 					tRow.setText(4, "Coordinator" + tRemoteClusterName.getGUICoordinatorID() + "@" + (tPeerLevel) + "(Cluster" + tRemoteClusterName.getGUIClusterID() + "@" + tPeerLevel + ")");
 				}else{
 					tRow.setText(4, tComChannel.getRemoteClusterName().toString());
 				}
 			}else{
 				tRow.setText(4, "??");
 			}
 			
 			/**
 			 * Column 5:  
 			 */
 			Route tRoute = null;
 			Name tTarget = null;
 			try {
 				tTarget = tComChannel.getPeerL2Address();
 				if(tTarget != null) {
 					tRoute = mHRMController.getHRS().getRoute(tTarget, new Description(), null);
 				}
 			} catch (RoutingException tExc) {
 				Logging.err(this, "Unable to compute route to " + tTarget, tExc);
 			} catch (RequirementsException tExc) {
 				Logging.err(this, "Unable to fulfill requirements for route calculation to " + tTarget, tExc);
 			}			
 			if (tRoute != null){
 				tRow.setText(5, tRoute.toString());
 			}else{
 				tRow.setText(5, "??");
 			}
 			
 			/**
 			 * Column 6:  
 			 */
 			tRow.setText(6, tComChannel.getDirection().toString());
 
 			/**
 			 * Column 7:  
 			 */
 			tRow.setText(7, Integer.toString(tComChannel.countSentPackets()));
 
 			/**
 			 * Column 8:  
 			 */
 			tRow.setText(8, Integer.toString(tComChannel.countReceivedPackets()));
 
 			j++;
 		}
 		
 		TableColumn[] cols = tTable.getColumns();
 		for(int k=0; k < cols.length; k++) cols[k].pack();
 		tTable.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		
 		/**
 		 * The table context menu
 		 */
 		final LinkedList<ComChannel> tfComChannels = tComChannels;
 		tTable.addMenuDetectListener(new MenuDetectListener()
 		{
 			@Override
 			public void menuDetected(MenuDetectEvent pEvent)
 			{
 				final int tSelectedIndex = tTable.getSelectionIndex();
 				// was there a row selected?
 				if (tSelectedIndex != -1){
 					// identify which row was clicked.
 					TableItem tSelectedRow = tTable.getItem(tSelectedIndex);
 					tSelectedRow.getData();
 				
 					//Logging.log(this, "Context menu for comm. channels of entity: " + pControlEntity + ", index: " + tSelectedIndex + ", row data: " + tSelectedRow);
 					
 					/**
 					 * Create the context menu
 					 */
 					Menu tMenu = new Menu(tTable);
 					MenuItem tMenuItem = new MenuItem(tMenu, SWT.NONE);
 					tMenuItem.setText("Show packet I/O");
 					tMenuItem.addSelectionListener(new SelectionListener() {
 						public void widgetDefaultSelected(SelectionEvent pEvent)
 						{
 							//Logging.log(this, "Default selected: " + pEvent);
 							showPackets(tfComChannels.get(tSelectedIndex));
 						}
 						public void widgetSelected(SelectionEvent pEvent)
 						{
 							//Logging.log(this, "Widget selected: " + pEvent);
 							showPackets(tfComChannels.get(tSelectedIndex));
 						}
 					});
 					tTable.setMenu(tMenu);
 				}
 			}
 		});
 	}
 	
 	private void showPackets(ComChannel pComChannel)
 	{
 		Logging.log(this, "Packet I/O for: " + pComChannel);
 		LinkedList<ComChannelPacketMetaData> tPacketsMetaData = pComChannel.getPacketsStorage();
 		int i = 0;
 		for (ComChannelPacketMetaData tPacketMetaData: tPacketsMetaData){
 			Logging.log(this, "     ..[" + i + "] (" + (tPacketMetaData.wasSent() ? "S" : "R") + "): " + tPacketMetaData.getPacket());
 			i++;
 		}		
 	}
 
 	private void printNAME(ControlEntity pEntity)
 	{
 		StyledText tClusterLabel = new StyledText(mContainer, SWT.BORDER);;
 		tClusterLabel.setForeground(new Color(mShell.getDisplay(), 0, 0, 0));
 		boolean tClusterHeadWithoutCoordinator = false;
 		if (GUI_SHOW_COLORED_BACKGROUND_FOR_CONTROL_ENTITIES){
 			boolean tBackgroundSet = false;
 			if (pEntity instanceof Cluster){
 				Cluster tCluster =(Cluster) pEntity;
 				if ((tCluster.getElector() != null) && (tCluster.getElector().isCoordinatorValid())){
 					if(tCluster.hasLocalCoordinator()){
 						tClusterLabel.setBackground(new Color(mShell.getDisplay(), 111, 222, 111));
 					}else{
 						tClusterHeadWithoutCoordinator = true;
 						tClusterLabel.setBackground(new Color(mShell.getDisplay(), 111, 222, 222));
 					}
 					tBackgroundSet = true;
 				}
 			}
 			if (pEntity instanceof Coordinator){
 				Coordinator tCoordinator =(Coordinator) pEntity;
 				if ((tCoordinator.isClustered()) || (tCoordinator.getHierarchyLevel().isHighest())){
 					tClusterLabel.setBackground(new Color(mShell.getDisplay(), 111, 222, 111));
 					tBackgroundSet = true;
 				}
 			}
 			if (!tBackgroundSet){
 				tClusterLabel.setBackground(new Color(mShell.getDisplay(), 222, 111, 111));
 			}
 		}else{
 			tClusterLabel.setBackground(new Color(mShell.getDisplay(), 222, 222, 222));
 		}
 		if (pEntity instanceof Cluster){
 			Cluster tCluster = (Cluster) pEntity;
 			tClusterLabel.setText(pEntity.toString() + "  Priority=" + pEntity.getPriority().getValue() + "  UniqueID=" + tCluster.getClusterID() + " Election=" + tCluster.getElector().getElectionStateStr() + (tClusterHeadWithoutCoordinator ? "   (inactive cluster)" : ""));
 		}else{
 			tClusterLabel.setText(pEntity.toString() + "  Priority=" + pEntity.getPriority().getValue());
 		}
 	    StyleRange style1 = new StyleRange();
 	    style1.start = 0;
 	    style1.length = tClusterLabel.getText().length();
 	    style1.fontStyle = SWT.BOLD;
 	    tClusterLabel.setStyleRange(style1);
 	}
 	
 	/**
 	 * Draws GUI elements for depicting cluster information.
 	 * 
 	 * @param pCluster selected cluster 
 	 */
 	private void printClusterMember(ClusterMember pClusterMember)
 	{
 		// on which hierarchy level are we?
 		int tHierarchyLevel = pClusterMember.getHierarchyLevel().getValue();
 
 		if (HRM_VIEWER_DEBUGGING)
 			Logging.log(this, "Printing cluster (member) \"" + pClusterMember.toString() +"\"");
 
 		/**
 		 * GUI part 0: name of the cluster 
 		 */
 		printNAME(pClusterMember);
 		
 		/**
 		 * GUI part 1: tool box 
 		 */
 		if(pClusterMember != null) {
 			ToolBar tToolbar = new ToolBar(mContainer, SWT.NONE);
 
 			if (HRM_VIEWER_SHOW_SINGLE_ENTITY_ELECTION_CONTROLS){
 				if ((pClusterMember.getElector() != null) && (!pClusterMember.getElector().isCoordinatorValid())){
 					ToolItem toolItem1 = new ToolItem(tToolbar, SWT.PUSH);
 				    toolItem1.setText("[Elect coordinator]");
 				    toolItem1.addListener(SWT.Selection, new ListenerElectCoordinator(this, pClusterMember));
 				}
 			}
 
 			ToolItem toolItem2 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem2.setText("[Elect all level " + tHierarchyLevel + " coordinators]");
 		    toolItem2.addListener(SWT.Selection, new ListenerElectHierarchyLevelCoordinators(this, pClusterMember));
 		    
 		    tToolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		}
 		
 		/**
 		 * GUI part 2: table about CEPs 
 		 */
 		printComChannels(pClusterMember);
 	
 		Label separator = new Label (mContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
 		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		separator.setVisible(true);
 	}
 
 	@Override
 	public void init(IEditorSite pSite, IEditorInput pInput) throws PartInitException
 	{
 		setSite(pSite);
 		setInput(pInput);
 		
 		// get selected object to show in editor
 		Object tInputObject;
 		if(pInput instanceof EditorInput) {
 			tInputObject = ((EditorInput) pInput).getObj();
 		} else {
 			tInputObject = null;
 		}
 		if (HRM_VIEWER_DEBUGGING)
 			Logging.log(this, "Initiating HRM viewer " + tInputObject + " (class=" + tInputObject.getClass() +")");
 		
 		if(tInputObject != null) {
 			// update title of editor
 			setTitle(tInputObject.toString());
 
 			if(tInputObject instanceof HRMController) {
 				mHRMController = (HRMController) tInputObject;				
 			} else {
 				throw new PartInitException("Invalid input object " +tInputObject +". Bus expected.");
 			}
 			
 			// update name of editor part
 			setPartName(toString());
 			
 		} else {
 			throw new PartInitException("No input for editor.");
 		}
 		
 		// register this GUI at the corresponding HRMController
 		if (mHRMController != null){
 			mHRMController.registerGUI(this);
 		}
 	}
 	
 	/**
 	 * overloaded dispose() function for unregistering from the HRMController instance
 	 */
 	public void dispose()
 	{
 		// unregister this GUI at the corresponding HRMController
 		if (mHRMController != null){
 			mHRMController.unregisterGUI(this);
 		}
 		
 		// call the original implementation
 		super.dispose();
 	}
 
 	@Override
 	public void doSave(IProgressMonitor arg0)
 	{
 	}
 
 	@Override
 	public void doSaveAs()
 	{
 	}
 
 	@Override
 	public boolean isDirty()
 	{
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed()
 	{
 		return false;
 	}
 
 	@Override
 	public void setFocus()
 	{
 	}
 
 	@Override
 	public Object getAdapter(Class required)
 	{
 		if(getClass().equals(required)) return this;
 		
 		Object res = super.getAdapter(required);
 		
 		if(res == null) {
 			res = Platform.getAdapterManager().getAdapter(this, required);
 			
 			if(res == null)	res = Platform.getAdapterManager().getAdapter(mHRMController, required);
 		}
 		
 		return res;
 	}
 
 	/**
 	 * Thread main function, which is used if an asynchronous GUI update is needed.
 	 * In this case, the GUI update has to be delayed in order to do it within the main GUI thread.
 	 */
 	@Override
 	public void run()
 	{
 		resetGUI();
 	}
 
 	/**
 	 * Resets the GUI and updates everything in this EditorPart
 	 */
 	private void resetGUI()
 	{
 		if(!mDisplay.isDisposed()) {
 			if(Thread.currentThread() != mDisplay.getThread()) {
 				//switches to different thread
 				mDisplay.asyncExec(this);
 			} else {
 				Point tOldScrollPosition = mScroller.getOrigin();
 				
 				destroyPartControl();
 				
 				createPartControl(mShell);
 				
 				mScroller.setOrigin(tOldScrollPosition);
 			}
 		}
 	}
 	
 	/**
 	 * Function for receiving notifications about changes in the corresponding HRMController instance
 	 */
 	@Override
 	public void update(Observable pSource, Object pReason)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Got notification from " + pSource + " because of \"" + pReason + "\"");
 		}
 
 		startGUIUpdateTimer();
 	}
 	
 	/**
 	 * Starts the timer for the "update GUI" event.
 	 * If the timer is already started nothing is done.
 	 */
 	private void startGUIUpdateTimer()
 	{
 		if (mTimeNextGUIUpdate == 0){
 			// determine the time when a "share phase" has to be started 
 			mTimeNextGUIUpdate = mHRMController.getSimulationTime() + HRMConfig.DebugOutput.GUI_NODE_DISPLAY_UPDATE_INTERVAL;
 
 			// register next trigger
 			mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.DebugOutput.GUI_NODE_DISPLAY_UPDATE_INTERVAL, this);
 		}else{
 			// timer is already started, we ignore the repeated request
 		}
 	}
 	
 	/**
 	 * This function is called when the event is fired by the main event system.
 	 */
 	@Override
 	public void fire()
 	{
 		// reset stored GUI update time
 		mTimeNextGUIUpdate = 0;
 		
 		// trigger GUI update
 		resetGUI();
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{		
 		return "HRM viewer" + (mHRMController != null ? "@" + mHRMController.getNodeGUIName() : "");
 	}
 	
 	/**
 	 * Listener for electing coordinator for this cluster.
 	 */
 	private class ListenerElectCoordinator implements Listener
 	{
 		private ClusterMember ClusterMember = null;
 		private HRMViewer mHRMViewer = null;
 		
 		private ListenerElectCoordinator(HRMViewer pHRMViewer, ClusterMember pClusterMember)
 		{
 			super();
 			ClusterMember = pClusterMember;
 			mHRMViewer = pHRMViewer;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			if (HRM_VIEWER_DEBUGGING){
 				Logging.log(this, "GUI-TRIGGER: Starting election for " + ClusterMember);
 			}
 			
 			// start the election for the selected cluster
 			ClusterMember.getElector().startElection();
 		}
 	
 		public String toString()
 		{
 			return mHRMViewer.toString() + "@" + getClass().getSimpleName(); 
 		}
 	}
 
 	/**
 	 * Listener for electing coordinators for all clusters on this hierarchy level. 
 	 */
 	private class ListenerElectHierarchyLevelCoordinators implements Listener
 	{
 		private ClusterMember mClusterMember = null;
 		private HRMViewer mHRMViewer = null;
 
 		private ListenerElectHierarchyLevelCoordinators(HRMViewer pHRMViewer, ClusterMember pClusterMember)
 		{
 			super();
 			mClusterMember = pClusterMember;
 			mHRMViewer = pHRMViewer;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			// get the hierarchy level of the selected cluster
 			HierarchyLevel tLocalClusterLevel = mClusterMember.getHierarchyLevel();
 			
 			// iterate over all HRMControllers
 			for(HRMController tHRMController : HRMController.getALLHRMControllers()) {
 				// iterate over all clusters from the current HRMController
 				for (Cluster tCluster: tHRMController.getAllClusters())
 				{
 					// check the hierarchy of the found cluster
 					if (tLocalClusterLevel.equals(tCluster.getHierarchyLevel())){
 						if (HRM_VIEWER_DEBUGGING){
 							Logging.log(this, "GUI-TRIGGER: Starting election for " + tCluster);
 						}
 						
 						// start the election for the found cluster
 						tCluster.getElector().startElection();
 					}
 				}
 			}
 		}
 		
 		public String toString()
 		{
 			return mHRMViewer.toString() + "@" + getClass().getSimpleName(); 
 		}
 	}
 	
 	/**
 	 * Listener for clustering the network on a defined hierarchy level. 
 	 */
 	private class ListenerClusterHierarchyLevel implements Listener
 	{
 		private Coordinator mCoordinator = null;
 		private HRMViewer mHRMViewer = null;
 		
 		private ListenerClusterHierarchyLevel(HRMViewer pHRMViewer, Coordinator pCoordinator)
 		{
 			super();
 			mCoordinator = pCoordinator;
 			mHRMViewer = pHRMViewer;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			// get the hierarchy level of the selected cluster
 			HierarchyLevel tLocalClusterLevel = mCoordinator.getHierarchyLevel();
 			
 			// check if we are at the max. hierarchy height
 			if(!tLocalClusterLevel.isHighest()) {
 
 				// iterate over all HRMControllers
 				for(HRMController tHRMController : HRMController.getALLHRMControllers()) {
 					tHRMController.cluster(new HierarchyLevel(this, tLocalClusterLevel.getValue() + 1));
 				}
 			}else{
 				Logging.err(this, "Maximum hierarchy height " + (tLocalClusterLevel.getValue()) + " is already reached.");
 			}
 		}
 		
 		public String toString()
 		{
 			return mHRMViewer.toString() + "@" + getClass().getSimpleName(); 
 		}
 	}
 	
 	/**
 	 * Listener for clustering the network, including the current cluster's coordinator and its siblings. 
 	 */
 	private class ListenerClusterHierarchy implements Listener
 	{
 		private Coordinator mCoordinator = null;
 		private HRMViewer mHRMViewer = null;
 		
 		private ListenerClusterHierarchy(HRMViewer pHRMViewer, Coordinator pCoordinator)
 		{
 			super();
 			mCoordinator = pCoordinator;
 			mHRMViewer = pHRMViewer;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			// check if we are at the max. hierarchy height
 			if(!mCoordinator.getHierarchyLevel().isHighest()) {
 				if (mCoordinator != null){
 					if (HRM_VIEWER_DEBUGGING){
 						Logging.log(this, "GUI-TRIGGER: Starting clustering for coordinator " + mCoordinator);
 					}
 					
 					// start the clustering of the selected cluster's coordinator and its neighbors
 					mHRMController.cluster(new HierarchyLevel(this, mCoordinator.getHierarchyLevel().getValue() + 1));
 				}else{
 					Logging.err(this, "Coordinator is invalid, skipping clustering request");
 				}
 			}else{
 				Logging.err(this, "Maximum hierarchy height " + (mCoordinator.getHierarchyLevel().getValue()) + " is already reached.");
 			}
 		}		
 		
 		public String toString()
 		{
 			return mHRMViewer.toString() + "@" + getClass().getSimpleName(); 
 		}
 	}	
 }
