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
 
 
 import java.awt.PopupMenu;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 
 import de.tuilmenau.ics.fog.IController;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.eclipse.GraphViewer;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.EditorAWT;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.EditorInput;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.SelectionProvider;
 import de.tuilmenau.ics.fog.eclipse.ui.menu.MenuCreator;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.AbstractRoutingGraphLink;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.simulated.RoutingServiceAddress;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.graph.util.Pair;
 
 public class HRGViewer extends EditorAWT implements Observer, IController, IEvent
 {
 	protected HRMController mHRMController = null;
 	private SelectionProvider selectionCache = null;
 	private MenuCreator menuCreator = null;
 
 	/**
 	 * Stores the time stamp of the last GUI update
 	 */
 	private Double mTimestampLastGUIUpdate =  new Double(0);
 
     /**
      * Stores the simulation time for the next GUI update.
      */
     private double mTimeNextGUIUpdate = 0;
 
     /**
      * Stores the GraphViewer reference
      */
     private GraphViewer<RoutingServiceAddress, RoutingServiceLink> mGraphViewer = null;
     
 	public HRGViewer()
 	{
 	}
 	
 	@Override
 	public void createPartControl(Composite parent)
 	{
 		super.createPartControl(parent);
 
 		selectionCache = new SelectionProvider(getDisplay());
 		getSite().setSelectionProvider(selectionCache);
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
 		
 		mGraphViewer = new GraphViewer<RoutingServiceAddress,RoutingServiceLink>(this);
 		mGraphViewer.init((RoutableGraph)mHRMController.getHRGForGraphViewer());
 		setView(mGraphViewer.getComponent());
 		
 		// register this GUI at the corresponding HRMController
 		if (mHRMController != null){
 			mHRMController.registerHRGGUI(this);
 		}
 	}
 
 	@Override
 	public Object getAdapter(Class pFilter)
 	{
 		if (getClass().equals(pFilter)) {
 			return this;
 		}
 		
 		Object res = super.getAdapter(pFilter);
 		
 		if(res == null) {
 			res = Platform.getAdapterManager().getAdapter(this, pFilter);
 			
 			if(res == null)	res = Platform.getAdapterManager().getAdapter(mHRMController, pFilter);
 		}
 		
 		return res;
 	}
 
 	@Override
 	public void selected(Object selection, boolean pByDefaultButton, int clickCount)
 	{
 		// default: select whole object represented in the view
 		if(selection == null) selection = mHRMController;
 
 		Logging.trace(this, "Selected (" + selection.getClass().getSimpleName() + "): " + selection);
 		if(selection instanceof HRMID){
 			HRMID tSelectedHRMID = (HRMID)selection;
 			
 			Logging.trace(this, "   ..related links:");
 			Collection<AbstractRoutingGraphLink> tLinks = mHRMController.getHRGForGraphViewer().getOutEdges(tSelectedHRMID);
 			int i = 0;
 			for(AbstractRoutingGraphLink tLink : tLinks){
				RoutingEntry tLinkRouteEntry = (RoutingEntry) tLink.getRoute().getFirst();
 				Pair<HRMID> tEndPoints = mHRMController.getHRGForGraphViewer().getEndpoints(tLink);
 				Logging.trace(this, "     ..[" + i + "]: " + tEndPoints.getFirst() + " out to " + tEndPoints.getSecond() + " <== " + tLink);
				Logging.trace(this, "       ..cause: " + tLinkRouteEntry.getCause());
 				i++;
 			}
 			tLinks = mHRMController.getHRGForGraphViewer().getInEdges(tSelectedHRMID);
 			i = 0;
 			for(AbstractRoutingGraphLink tLink : tLinks){
				RoutingEntry tLinkRouteEntry = (RoutingEntry) tLink.getRoute().getFirst();
 				Pair<HRMID> tEndPoints = mHRMController.getHRGForGraphViewer().getEndpoints(tLink);
				Logging.trace(this, "     ..[" + i + "]: " + tEndPoints.getSecond() + " in from " + tEndPoints.getFirst() + " <== " + tLink);
				Logging.trace(this, "       ..cause: " + tLinkRouteEntry.getCause());
 				i++;
 			}
 		}
 		
 		if(pByDefaultButton) {
 			if(selection != null) {
 				selectionCache.announceSelection(selection);
 			}
 
 			// start default action?
 			if(clickCount >= 2) {
 				if(menuCreator == null) {
 					menuCreator = new MenuCreator(getSite());
 				}
 				
 				//
 				// Get and run default action
 				//
 				ActionListener action = menuCreator.getDefaultAction(selection);
 				if(action != null) {
 					action.actionPerformed(null);
 				} else {
 					Logging.warn(this, "No default action for " +selection);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Recursive version for creating the context menu.
 	 * Required by special case where a Node is cased to a Host.
 	 * 
 	 * @param selection Selected object for which the context menu should be created (null, if no object is selected)
 	 * @param popup Popup menu from previous recursive level
 	 * @return Result popup menu (might be same reference as passed in be <code>popup</code>)
 	 */
 	@Override
 	public void fillContextMenu(Object selection, PopupMenu popup)
 	{
 		if(menuCreator == null) {
 			menuCreator = new MenuCreator(getSite());
 		}
 		
 		menuCreator.fillMenu(selection, popup);
 		
 		if(selection == null) {
 			// No special element within graph selected.
 			// But we would like to show the menu for the
 			// overall item shown in the graph, too.
 			if(popup.getItemCount() > 0) {
 				popup.addSeparator();
 			}
 			
 			menuCreator.fillMenu(mHRMController, popup);
 		}
 		
 		//
 		// Special case Node -> Show entry for Host, too!
 		//
 		if(selection instanceof Node) {
 			if(popup.getItemCount() > 0) {
 				popup.addSeparator();
 			}
 			
 			menuCreator.fillMenu((Node)selection, popup);
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
 
 		//startGUIUpdateTimer();
 	}
 	
 	/**
 	 * Starts the timer for the "update GUI" event.
 	 * If the timer is already started nothing is done.
 	 */
 	private synchronized void startGUIUpdateTimer()
 	{
 		// is a GUI update already planned?
 		if (mTimeNextGUIUpdate == 0){
 			// when was the last GUI update? is the time period okay for a new update? -> determine a timeout for a new GUI update
 			double tNow = mHRMController.getSimulationTime();
 			double tTimeout = mTimestampLastGUIUpdate.longValue() + HRMConfig.DebugOutput.GUI_NODE_HRG_DISPLAY_UPDATE_INTERVAL;
 
 			if ((mTimestampLastGUIUpdate.doubleValue() == 0) || (tNow > tTimeout)){
 				mTimeNextGUIUpdate = tNow;
 
 				// register next trigger
 				mHRMController.getAS().getTimeBase().scheduleIn(0, this);
 			}else{
 				mTimeNextGUIUpdate = tTimeout;
 			
 				// register next trigger
 				mHRMController.getAS().getTimeBase().scheduleIn(tTimeout - tNow, this);
 			}
 
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
 		mGraphViewer.update(null,  null);
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{		
 		return "HRG viewer" + (mHRMController != null ? "@" + mHRMController.getNodeGUIName() : "");
 	}
 }
