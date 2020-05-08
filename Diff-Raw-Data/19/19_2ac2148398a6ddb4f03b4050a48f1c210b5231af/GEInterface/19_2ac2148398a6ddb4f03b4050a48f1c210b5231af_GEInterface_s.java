 package org.eclipse.stem.ui.ge;
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/ 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.stem.core.common.DublinCore;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProvider;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapterFactory;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProvider;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapter;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapterFactory;
 import org.eclipse.stem.definitions.adapters.spatial.geo.PlatformLatLongDataProvider;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong.Segment;
 import org.eclipse.stem.definitions.nodes.impl.RegionImpl;
 import org.eclipse.stem.data.geography.GeographicNames;
 import org.eclipse.stem.jobs.preferences.PreferenceConstants;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.jobs.simulation.ISimulationListener;
 import org.eclipse.stem.jobs.simulation.ISimulationManagerListener;
 import org.eclipse.stem.jobs.simulation.SimulationEvent;
 import org.eclipse.stem.jobs.simulation.SimulationManager;
 import org.eclipse.stem.jobs.simulation.SimulationManagerEvent;
 import org.eclipse.stem.jobs.simulation.SimulationState;
 import org.eclipse.stem.ui.ge.kml.KmlDisplaySelection;
 import org.eclipse.stem.ui.ge.kml.StemKml;
 import org.eclipse.stem.ui.ge.views.GEPreferencePage;
 import org.eclipse.stem.ui.ge.views.GEView;
 import org.eclipse.stem.ui.views.geographic.map.GeographicSelectionElements;
 
 
 
 /**
  * This is the STEM half of the interface between 
  * STEM and GoogleEarth.
  * 
  * It listens for Simulations to be added or deleted and maintains a 
  * list of simulations.  When a simulation becomes active it then 
  * listens for Simulation events.   
  * <p>
  * When a simulation cycle completes, we are notified and 
  * it will take the STEM data and call StemKml to convert 
  * it to KML and either log it to a set of files for later display 
  * or send it directly to GoogleEarth.
  * <p>
  * The overall process flow is the following:
  * <pre>
  *   <b>simulationsChanged:</b> Notified when a new Simulation is added
  *   or removed.  If added then setActive() is called to add it to 
  *   the list of Simulations being handled.
  *   
  *   <b>simulationChanged:</b> Notified when a cycle ends. It calls 
  *   the processCycle method.
  *   
  *   <b>processCycle</b>Main process when a processing cycle ends.
  *     
  *     <b>buildMap()</b> called on 1st cycle to build a map of  
  *     GEData instances that contains the geographic description 
  *     of each county/areaid
  *     
  *     It then uses the PreferencePage option to decide how to 
  *     comunicate with GoogleEarth.  
  *     
  *      <b>storeSEIR()</b> This updates the GEData instances with the 
  *      relative disease statistics.
  *      
  *      <b>generateKML()</b> This sends all the information to the 
  *      GEJob instance where a KML file for this cydle is written.
  *      
  * </pre>
  *    
  */
 
 public class GEInterface 
            implements ISimulationListener, ISimulationManagerListener{
 		
 	 /**
      * template for the KML filename written on the 
      * Nth cycle for type T
      */
     private static final String controlTemplate = "control_&N_&T.kml";
     
     
 	/**
 	 * List of active simulations keyed by Name
 	 * 
 	 * The list is updated by SimulationManager events
 	 * that add or remove simulations.
 	 */
 	private Map<String,GESimEntry> activeSimulations = null;
 	
 	
 	/**
 	 * map of geographic data keyed by area id
 	 */
 	private Map<String, GEData> stemMap = null;
 	
 	/**
 	 * GEView handles all the gui ui for us
 	 */
 	private GEView geView = null;
 	
 	/**
 	 * This will contain the simulation id when that simulation 
 	 * is sending its KML to GoogleEarth.  When it is filled
 	 * in any subsequent requests to log and view will only do logging.
 	 * It is cleared when the simulation is removed.
 	 */
 	private String activeView = null;
 	
 	/**
 	 * when set to a node name then the displayMap method
 	 * will only display that node.
 	 */
 	private String displayNode = null;
 	/**
 	 * Cycle being processed.
 	 */
 	//private int cycle = 0;
 	/**
 	 * This map is used to collect nodes that have been 
 	 * displayed by previous requests so that this can 
 	 * be an accumulative display.  
 	 */
 	private Map<String,GEData> stemMapNode = null;
 	/**
 	 * default Aspect to be displayed for simulations.
 	 * Set by GEView context menu to override 
 	 * the defaultAspect specified in Aspect.properties
 	 */
 	private Aspect defaultAspect = null;
 	
 	/** 
 	 * place to store an error message so the caller can get it.
 	 */
 	private String errorMessage = null;
 	/**
 	 * when user requests a manual display of map
 	 * set this true 
 	 */
 	private boolean manualRequest = false;
 	
 	/**
 	 * Reference to the display module that 
 	 * displays the info based on a selection by
 	 * the mapview view 
 	 */
 	private final KmlDisplaySelection displaySelection = 
 		new KmlDisplaySelection();
 	/**
 	 * remember if we have isssued a displayArea cmd
 	 * so we can clean it up.
 	 */
 	private boolean didDisplayArea = false;
 	/**
 	 * 
 	 * Constructor. 
 	 * 
 	 * - setup to listen for SimulationManager events
 	 * - check if existing simulations.
 	 * @param geView  GUI class that is invoking us.
 	 */
 	public GEInterface(GEView geView) {
 		
 		GELog.debug(this,"constructor: ");
 		
 		this.geView = geView;
 		Aspect.setup();
 		activeSimulations = new HashMap<String,GESimEntry>(1);
 		
 		
 		
 	}
 
 	/**
 	 * called by GEView to do any initialization 
 	 * after we have setup the GUI environment
 	 *
 	 * It will startup the servlet web server if possible
 	 * 
 	 * @return true if initialization is successful
 	 * 
 	 */
     public boolean init() {
     	boolean ok = true;
     	boolean okKML = true;
     	SimulationManager simulationManager = SimulationManager.getManager();
 		simulationManager.addSimulationManagerListener(this);
 		List<ISimulation>list = simulationManager.getActiveSimulations();
 		boolean autolog = GEPreferencePage.isAutoLogging();
 		for (ISimulation sim: list) {
 			String name = getSimulationId(sim);
 		    GELog.debug(this,"simulationAdded: "+name+" "+sim);			
 			setActive(sim,autolog);			
 		}
     	// check for valid web server
 		String url = StemKml.initWebServer();
 		GELog.debug(this,"Web server URL: "+url);
 		if (url == null ) {
 			setErrorMessage("Unable to access WebServer");
 			ok = false;
 		} else {
 			okKML = StemKml.verifyGE();			
 			// handle undefined KML
 			if (! okKML) {	
 				setErrorMessage("GoogleEarth KML files are not defined");
 				
 			}
 		}
 		if (!ok || !okKML) {
 			// check if preferences will work with no web 
 			// server or no KNL file definition
 			ok = checkPreferences(url,okKML);
 			if (!ok) {
 				GELog.debug(getErrorMessage());
 			}
 		}
 		
 		return ok;
 		
 	}
 
     /**
      * verify (and change) the preferences based on the 
      * environment.  
      * 
      * If a webserver is not available or if googleearth 
      * has not been installed then it will limit the 
      * communication methods to not use a webserve or 
      * not try to launch the kml file.
      * 
      * @param url URL for web server or null
      * @param okGE false if googleEarth not installed
      * @return false if not OK and changes had to be made
      */
     private boolean checkPreferences(String url,boolean okGE) {
     	boolean result = false;
     	int method = GEPreferencePage.getMethod();
     	int newMethod = -1;
 		switch (method) {				
 			case GEPreferencePage.M_LOG_ONLY:
 				// this method alway works
 				result = true;
 		        break;
 			case GEPreferencePage.M_LOG_SERVLET:
 				// this method will not work if no server
 				result = false; 
 				
 				if ( url == null && okGE) {
 					newMethod = GEPreferencePage.M_AUTO_LAUNCH;
 					errorMessage = "No WebServer: Changed to directly launch KML";
 					
 				} else {
 					newMethod = GEPreferencePage.M_LOG_ONLY;
 					errorMessage = "KML is not recognized, Changed to only Log files";
 				}					
 				
 		        break;
 			case GEPreferencePage.M_ASYNC_SERVLET:
 //				 this method will not work if no server
 								
 				if ( url == null && okGE) {
 					newMethod = GEPreferencePage.M_AUTO_LAUNCH;
 					errorMessage = "No WebServer: Changed to directly launch KML";
 					
 				} else {
 					newMethod = GEPreferencePage.M_LOG_ONLY;
 					errorMessage = "KML is not recognized, Changed to only Log files";
 				}					
 				
 		        break;
 			case GEPreferencePage.M_AUTO_LAUNCH:
                  //	 this method will not work if KML is unknown
 				  if (! okGE) {
 					  newMethod = GEPreferencePage.M_LOG_ONLY;
 						errorMessage = "KML is not recognized, Changed to only Log files";
 				  }
 		  		break;	
 			case GEPreferencePage.M_MANUAL_DISPLAY:
 				// this method is always OK
 				break;
 
 		    default:
 		        	
 		}
 		if (newMethod != -1) {
 			GEPreferencePage.setMethod(newMethod);
 			result = false;  // tell about change
 		}
     	return result;
     }
 	/**
 	 * We get here because we are listening for SimulationsChanged 
 	 * events from the Simulation Manager and one occured. 
 	 * We will check for a Simulation being removed and if so check if 
 	 * it is one of our active simulation and if so remove it.
 	 * If a simulation is being added, it will be added to the list of
 	 * active simulations.
 	 * 	 
 	 */
 	public void simulationsChanged(SimulationManagerEvent event) {
 		GELog.debug(this.getClass(), "simulationsChanged: ");
 		// GELog.memory();
 		if (event.getSimulationsRemoved().length > 0) {
 			for (ISimulation sim : event.getSimulationsRemoved()) {
 
 				String id = getSimulationId(sim);
 				GELog.debug(this.getClass(), "simulationRemoved: " + id + " "
 						+ sim);
 
 				GESimEntry existing = activeSimulations.get(id);
 				if (existing != null) {
 					boolean active = isActiveView(sim);
 					setActive(sim, false);
 					activeSimulations.remove(id);
 					if (active) {
 						stopGE();
 					}
 					GELog.debug(this, "Active simulation removed: " + id);
 				}
 			}
 		} else if (event.getSimulationsAdded().length > 0) {
 			boolean autolog = GEPreferencePage.isAutoLogging();
 			for (ISimulation sim : event.getSimulationsAdded()) {
 				String name = getSimulationId(sim);
 				GELog.debug(this, "simulationAdded: " + name + " " + sim);
 				GESimEntry existing = activeSimulations.get(name);
 				if (existing == null) {
 					boolean ok = setActive(sim, autolog);
 					if (!ok) {
 						if (!autolog) {
 							geView.showMessage(errorMessage);
 						} 
 					}
 				}
 
 			}
 		}
 	} // simulationsChanged
     
 	/**
 	 * Called by GEView.dispose()
 	 * 
 	 * 
 	 */
 	public void dispose() {
 		SimulationManager simulationManager = SimulationManager.getManager();
 		List<ISimulation> list =simulationManager.getActiveSimulations();
 		for (ISimulation sim:list) {
 			sim.removeSimulationListener(this);
 		}
 		simulationManager.removeListener(this);
 		if (activeView != null) {
 			// GE is talking to us
 			stopGE();
 			// wait a bit before we continue 
 			// because this will kill the webserver 
 			// that GoogleEarth is talking to.
 			try {
 				Thread.sleep(5000);
 			} catch (Exception e) {
 				// ignore
 			}
 		}
 	}
     /**
 	 * Listener for events that happen in a specific Simulation.
 	 * 
 	 * <code>
 	 * This gets called for the following event states
 	 *    SimulationState: RUNNING
 	 *                     COMPLETED_CYCLE
 	 *                     PAUSED  
 	 * We are interested in Completed_Cycle 
 	 * 
 	 * @param event
 	 *            the event that records what happened to the manager.
 	 */
 	
 	public void simulationChanged(SimulationEvent event) {
 		
 		
 		try {
 			ISimulation simulation = event.getSimulation();
 			SimulationState state = event.getSimulationState();
 			String id = getSimulationId(simulation);
 			GESimEntry existing = activeSimulations.get(id);
             Sequencer seq = simulation.getScenario().getSequencer();
 			          
             
 			if (state == SimulationState.COMPLETED_CYCLE) {				
 				if (existing == null ) {
 					GELog.debug(this,"Simulation "+id+" not active for GE.");
 					return;
 				}
 				//existing.cycle++;
 				int cycle = seq.getCycle();
 				GELog.debug(this,id+" State="+state+" cycle="+cycle); 
 				processCycle(simulation,state);
 				geView.refresh();
 			} else {
 				GELog.debug(this,id+" State="+state); 
 			}
 
 		} catch (Exception e) {
 			GELog.error("simulationChanged: Failure ", e);
 		}
 	}
 	
 	/**
 	 * Handle the simulation event that just occured
 	 * This is where the real processing starts.
 	 * If we haven't already, we build a map of the 
 	 * geographical information.
 	 * Then determine if we want to do anything on this cycle and 
 	 * determine how we want to comunicate with GE.  
 	 * Next we run thru the scenario nodes and pick up
 	 * the current disease state for each node.  
 	 * Finally all of the information is sent to a 
 	 * background method that builds the KML file that is
 	 * sent to GoogleEarth.   
 	 *  
 	 * @param simulation that event is for
 	 * @param state  Current state being processed
 	 * 
 	 */
 	private void processCycle(ISimulation simulation, SimulationState state ) {
 		
 		String kmlfn = null;		
 		boolean doit = false;
 		int nth = GEPreferencePage.getWhichCycle();
 		
 		
 			
 		String id = getSimulationId(simulation);
 		GESimEntry existing = activeSimulations.get(id);
 		if (existing == null) {
 			GELog.debug(this,"Simulation "+id+" not active for GE.");
 			return;
 		}
 		
 		
 		int method = existing.getMethod();
 		String entryFolder = existing.getLogFolder();
 		Scenario scenario = simulation.getScenario();
 		Sequencer seq = scenario.getSequencer();
 //		 get cycle from simulation
 		int cycle = seq.getCycle();
 		if (GELog.memoryCheck(0.8) ) {
 			GELog.debug(this,"Process for cycle "+
 					cycle+" skipped. Memory low");
 			return;
 		}		
 		//if we haven't yet, then build geo map
 		if ((stemMap = existing.getStemMap()) == null ) {
 			if (! GELog.memoryCheck(0.8) ) {
 				stemMap = buildMap(simulation);
 			}
 			if (stemMap == null) {
 				GELog.debug(this.getClass(),"Unable to build stemMap");
 				return;
 			}
 			doit = true;
 			existing.setStemMap(stemMap);			
 		}
 		
 		StemKml stemKml = existing.getStemKml();
 		if (stemKml == null) {
 			stemKml = new StemKml();
 			existing.setStemKml(stemKml);			
 			stemKml.init();
 		}
 	    // if ManualDisplay and no user request
 		if (method == GEPreferencePage.M_MANUAL_DISPLAY &&
 				! manualRequest ) {
 			return;
 		}
         
 		if (cycle == 0 || nth == 1 ) {
 			doit = true;
 		} else if (nth > 1 && cycle%nth == 0 ) {
 			doit = true;
 		}
 
 		// Get aspect that we want view of
 		Aspect aspect = getAspectToDisplay(simulation);
 		if (manualRequest) 
 			doit = true;
 		// decide how we want to process it
 		if (doit) {	
 			try {				
 				boolean launch = false;	
 				boolean rename = false;
 				switch (method) {				
 					case GEPreferencePage.M_LOG_ONLY:
 						// log the file but no launch or netlink						
 						if (entryFolder != null) {
 							kmlfn = StemKml.getControlFN(entryFolder,
 									controlTemplate,aspect, cycle);
 						} else {
 							doit = false;
 						}
 				        break;
 					case GEPreferencePage.M_LOG_SERVLET:
 						// log file and launch a netlink to display
 						if ( manualRequest ) {
 							doit = true;							
 							if (activeView == null) {									
 								launch = true;
 								kmlfn = StemKml.getControlFile();
 							} else {
 								kmlfn = StemKml.getControlFN(entryFolder,
 										controlTemplate,aspect, cycle);	
 							}
 						} else if (! id.equals(activeView)) { 
 							doit = false;
 							GELog.debug(this,"Unable to display id "+id);
 						} else if (entryFolder != null) {
 							kmlfn = StemKml.getControlFN(entryFolder,
 									controlTemplate,aspect, cycle);							
 						} else {
 							doit = false;
 							GELog.debug(this,"Unable to display. Invalid folder");
 						}
 				        break;
 					case GEPreferencePage.M_ASYNC_SERVLET:						
 
 						if ( manualRequest ) {
 							if (activeView != null) {
 								doit = false;
 								GELog.debug(this,"Unable to do manual display");
 							} else {
 								launch = true;
 								kmlfn = StemKml.getControlFile();
 							}
 						} else {
 							if (! id.equals(activeView)) { 						
 							   doit = false;
 							   GELog.debug(this,"Not an active Simulation id: "+id+" activeView: "+
 									   activeView);
 							} else {
 								kmlfn = StemKml.getControlFile();
 								rename = true;
 							}
 						}
 				        break;
 					case GEPreferencePage.M_AUTO_LAUNCH:
 				  		// build control.kml and launch it
 						launch = true;
 						kmlfn = StemKml.getControlFile();
 						if ( ! manualRequest && (! id.equals(activeView)) ) {							
 							doit = false;
 							GELog.debug(this,"Invalid. id: "+id+
 									          " activeView: "+activeView);
 						} 
 				  		break;	
 					case GEPreferencePage.M_MANUAL_DISPLAY:
 						if (manualRequest) {
 							GELog.debug(this,"Do manual display");
 							launch = true;
 							kmlfn = StemKml.getControlFile();
 							doit = true;
 							
 						} else {
 							doit = false;
 						}
 						break;
 	
 				    default:
 				        	
 				}
 				manualRequest = false;
 				if (doit) {
 					//	 store the current SEIR data into GEData
 					storeAspects(simulation, stemMap);
 					
 					// generate the KML file
 					GELog.debug(this.getClass(),"GenerateKML for id: "+
 							id + " aspect "+aspect.toString());
 				
 					generateKML(kmlfn, stemKml,stemMap, aspect, cycle,launch,rename);
 					
 					
 				}
 			} catch (Exception e) {				
 				GELog.error("processCycle(): Failure creating KML ",e);
 				activeSimulations.remove(id);
 			}
 		}
 		
 		//GELog.debug(this.getClass(),"Done. cycle="+cycle);
 	}
 	
 	
 	/**
 	 *  called by GEView to display the current map for 
 	 *  a specific admin area.
 	 * 
 	 * @param adminFile  File that contains desired data
 	 * @param search   String that is matched against area id
 	 * @param clear    If true then clear the existing map
 	 * @return  true if able to display map or false if could not.
 	 */	
 	public boolean displayArea( String adminFile,String search, boolean clear) {
 		displayNode = search;
 		didDisplayArea = true;
 		errorMessage = null;
 		boolean result = true;
 		try {
             if (clear) 
             	stemMapNode = null;
             
 			GELog.debug(this,"displayMap() "+adminFile+" search: "+search);
 			if (adminFile == null) {				
 				errorMessage = "AdminFile argument is null";
 				GELog.debug(this,errorMessage);
 				return false;
 			}
 			String fn = StemKml.getDisplayAreaFile();
 			
 			StemKml stemKml = new StemKml();				
 			stemKml.init();				
 			// this will create GEData instances with LatLong data
             // The LatLong comes from file rather than simulation
 			Map<String,GEData> stemMap = buildMap(adminFile,search);
             if (stemMap.size() == 0) {
             	errorMessage = "Search argument "+search+
             	       " did not match any admin area name.";
             	return false;
             }
 			if (stemMapNode == null) {
 				stemMapNode = stemMap;
 			} else {
 				stemMapNode.putAll(stemMap);
 			}
 
 //			 Get aspect that we want view of
 			Aspect aspect = Aspect.getAspect("S");  
 			// generate the KML file	
 			boolean rename = false;
             int cycle = 0;
 			boolean launch = true;				
 			generateKML(fn,stemKml,stemMapNode, aspect,
 					cycle,launch,rename);
 
 		} catch (RuntimeException e) {
 			result = false;
 			GELog.error("DisplayArea failure", e);
 		}
 
 		displayNode = null;
 		return result;
 	}
 	/**
 	 *  called by GEView to display the specified area
 	 *  when a Selection event is received from mapview
 	 * 
 	 * @param region The Identifiable describing the admin area that 
 	 *               was selected.
 	 * @param gse  reference to the data about the selection
 	 * 
 	 * @return  true if able to display map or false if could not.
 	 */	
 	public boolean displaySelection( RegionImpl region,GeographicSelectionElements gse) {
         didDisplayArea = true;
 		errorMessage = null;
 		boolean result = true;
 		try {           
 			
 			String fn = StemKml.getDisplayAreaFile();
 			
 			StemKml stemKml = new StemKml();				
 			stemKml.init();				
 			
 			Map<String,GEData> stemMap = buildSelectionMap(region,gse);
            if (stemMap == null | stemMap.size() == 0) {
            	
             	return false;
             }
 			if (stemMapNode == null) {
 				stemMapNode = stemMap;
 			} else {
 				//stemMapNode.putAll(stemMap);
 				stemMapNode = stemMap; // clear???
 			}
 
 //			 Aspect not used
 			Aspect aspect = null;  
 			// generate the KML file
 			displaySelection.generatePolygons(fn, stemMapNode,aspect);
 
 //			 launch the file for GoogleEarth			  
 			StemKml.launch(fn);
 			GELog.debug(this,"GoogleEarth Map  submitted");
             
 		} catch (RuntimeException e) {
 			result = false;
 			GELog.error("DisplayArea failure", e);
 		}
 
 		displayNode = null;
 		return result;
 	}
 	/**
 	 *  called by GEView to display the current map 
 	 * This version sets a switch to cause 
 	 * a display on the next cycle if simulation or
 	 * now if paused. 
 	 * 
 	 * 
 	 * @param simulation The currently selected Simulation
 	 * @return  true if able to display map or false if could not.
 	 */
 	public boolean displayMap(ISimulation simulation) {
 		boolean result = true;
 		SimulationState state =   simulation.getSimulationState();
 		GELog.debug(this,"DisplayMap request");
 		manualRequest = true;
 		if (state != SimulationState.RUNNING) {	
 			// process it if not running.
 			processCycle(simulation,simulation.getSimulationState());
 		}
 	    // if it was running it will do it at end of cycle
 		return result;
 	}
 	
 
 	/**
 	 *  called by GEView to display the KML files on GE
 	 *  via the Servlet API
 	 *  
 	 * @param folder 
 	 * @return  true if able to display map or false if could not.
 	 */
 	public boolean runGE(String folder) {
 		boolean result = true;
 		GELog.debug(this,"GoogleEarth run request");
 		if (activeView == null) {	
 	        
 	        activeView = "runGE";
 	        
 	        StemKml.setWaitForKML(false);
 	        StemKml.launchNetworkLink(folder, null);
 		} else {
 			errorMessage = "GoogleEarth is busy";
 			GELog.debug(this,errorMessage);
 			result = false;
 			
 		}
 		return result;
 	}
 	/**
 	 *  called by GEView to start the GoogleEarth 
 	 *  application if not already started
 	 * 
 	 * 
 	 * 
 	 * @return  true if able to display map or false if could not.
 	 */
 	public boolean startGE() {
 		boolean result = true;	
 		GELog.debug(this,"GoogleEarth Start request");
 	    StemKml.launchGE();
 		
 		return result;
 	}
 	/**
 	 *  called by GEView when the GoogleEarth 
 	 *  application is being restarted.
 	 * 
 	 * 
 	 * 
 	 * @return  true if able to reset or false if could not.
 	 */
 	public boolean restartGE() {
 		boolean result = true;	
 		GELog.debug(this,"GoogleEarth Restart request");
 	    StemKml.launchGE();
 	    int method = 0;
 		
 		// TODO 
 	    // reestablish the communication environment.
 	    // If a networklink was active, reestablish it.
 	    if (activeView != null)  {
 	    	GESimEntry existing = activeSimulations.get(activeView);
 			if (existing != null) {
 				method = existing.getMethod();
 				ISimulation sim = existing.getSimulation();
 				if (sim.getSimulationState() == SimulationState.RUNNING){
 					errorMessage = 
 						"Simulation should be paused while restarting GoogleEarth";
 					return false;
 						
 				}
 				String folder = existing.getLogFolder();
 			    if (method == GEPreferencePage.M_ASYNC_SERVLET ||
 			    	method == GEPreferencePage.M_LOG_SERVLET)	{        
 			    	StemKml.launchNetworkLink(folder, null);
 			    }
 	        }
 		}
 		return result;
 	}
 	/**
 	 *  called by GEView to stop the current
 	 *  display the KML files on GE
 	 *  via the Servlet API
 	 * 
 	 * 
 	 * @return  true 
 	 */
 	public boolean stopGE() {
 		boolean result = true;	
 		GELog.debug(this,"GoogleEarth Stop request");
 		//StemKml.launchGE();
 		if (didDisplayArea) {
 			StemKml.clearDisplayArea();
 		}
 		activeView = null;
 		// for some reason it appears that sometimes 
 		// it takes to writing of an empty control file
 		// to stop the current NetworkLink so do it again
 		StemKml.launchGE();
 		return result;
 	}
 	/**
 	 * Schedule the background job that will build the .kml file 
 	 * that will be sent to GoogleEarth 
 	 * 
 	 * @param fn       Fully qualified Filename where we write the kml
 	 * @param stemKml  Object containing info about where and
 	 *                 how to generate the KML.
 	 * @param stemMap  Map of Polygons with SEIR data and PopDensity
 	 * 
 	 * @param aspect   SEIR Type to be displayed
 	 * @param cycle    current cycle number
 	 * @param launch   true if file should be sent to GoogleEarth
 	 * @param rename   true if file should be written then renamed
 	 * @return reference to the file containing the KML
 	 */	
 	
 	private StemKml generateKML(String fn,StemKml stemKml, Map<String,GEData> stemMap,
 			Aspect aspect,long cycle,boolean launch,boolean rename) {	
 		try {
 			GEJob job = new GEJob("Generate KML");
 			job.generateKML(fn,stemKml, stemMap, aspect, cycle,launch,rename);
 			
 		} catch (Exception e) {			
 			e.printStackTrace();
 		}
 		return stemKml;
 	}
 	
 	
 	/**
 	 * obtain the current aspect values and store them in the 
 	 * GEData instance
 	 * 
 	 * @param sim
 	 * @param stemMap
 	 */
 	
 	private void storeAspects(ISimulation sim,Map<String,GEData> stemMap) {
 		
 		Scenario scenario = sim.getScenario();		
 		//		 We get the decorators from the scenario ...			
 		Graph g = scenario.getCanonicalGraph();
		Decorator[] decorators = (Decorator[]) (g.getDecorators().toArray());
 		int err1ctr = 0;  // no latlong data
 		int err2ctr = 0;  // no aspect data
 		for (Decorator decorator : decorators) {
 	
 			Object[] labels = decorator.getLabelsToUpdate().toArray();
 			for (Object element: labels){
 				
 				if ( element instanceof NodeLabel) {
 					NodeLabel nodeLabel = (NodeLabel)element;
 					Node node = nodeLabel.getNode();
 					
 					String key = node.getDublinCore().getTitle();
 					
 					GEData data = stemMap.get(key);
 					
 					if (data == null) {	  // filtered previously	
 						continue;
 					}
 					boolean keep = filter(node,key,data);
 					if (!keep) 
 						continue;
 					//if (nodeLabel instanceof SEIRLabelImpl) {
 						// TODO we need a way to get PopDensity
 						//SEIRLabelImpl label = (SEIRLabelImpl)nodeLabel;
 						//DiseaseModelState state = (DiseaseModelState)label.getDiseaseModelState();							
 					//	popDensity =50*Math.random(); //state.getPopulationDensity();
 					//	data.setPopDensity(popDensity);
 					//}
 					RelativeValueProviderAdapter rvp = (RelativeValueProviderAdapter) RelativeValueProviderAdapterFactory.INSTANCE
 					.adapt(element, RelativeValueProvider.class);
 					rvp.setTarget((Notifier)element);
 					
 					// Does the label have relative values?
 					
 					if (rvp != null) {	
 						Map<String,Double> map = data.getValueMap();
 						for (Iterator propertyIter = 
 							rvp.getProperties().iterator(); propertyIter.hasNext();) {
 							ItemPropertyDescriptor property = 
 								(ItemPropertyDescriptor) propertyIter.next();
 
 							String p = property.getDisplayName(property);
 							double v = rvp.getRelativeValue(property);
 							// TODO debug insert of Infectious value
 //							if (key.startsWith("M") && p.equals("I")) {
 //								v = Math.random();
 //							}
 							// if we are keeping track of this aspect
 							if (Aspect.exists(p)) {
 								map.put(p,v);
 							}
 						} // for each property
 						//  remove debug code
 						// To debug this then make the infectious value random
 //						if (key.startsWith("Middlesex") && key.contains("MA")) {							
 //							GELog.debug(this,key+" Aspects: "+data.getValuesAsString());
 //							//GELog.debug(this,"BBox:"+data.getBBox());
 //						}
 //						if (key.contains("G220010") ) {							
 //							GELog.debug(this,key+" Aspects: "+data.getValuesAsString());
 //							//GELog.debug(this,"BBox:"+data.getBBox());
 //						}
 					} else {
 						//if (err2ctr == 0) 
 						//GELog.debug(this,"Node: "+key+" has no aspect data");
 						err2ctr++;
 					}
 
 				}
 			}  // for
 		}
 		
 		if (err1ctr > 0) 
 			GELog.debug(this,"Nodes with no LatLong data. count="+err1ctr);
 		if (err2ctr > 0)
 		    GELog.debug(this,"Nodes with no aspect values. count="+err2ctr);
 	}
 	
 	/**
 	 * Build a StemMap instance for the specified Simulation
 	 * 
 	 * The StemMap is a HashMap containing GEData objects 
 	 * keyed by areaid.  The location object contains the 
 	 * geographic borders for the areaid.
 	 * 
 	 * @param simulation that will be shown
 	 * @return Map Key is node, element is GEData instance
 	 * 
 	 */
 	
 	private Map<String,GEData> buildMap(ISimulation simulation) {
 		try {
 			stemMap = new HashMap<String,GEData>();
 			
 			
 			Scenario scenario = simulation.getScenario();
 			//String title = scenario.produceTitle();
 			//DublinCore dc = scenario.getDublinCore();
 			//GELog.debug(this,"buildMap(): title="+title);
 			//GELog.debug(this,"buildMap(): DublinCore="+dc);
 			
 			
 			//		 We get the graph...
 			final Graph canonicalGraph = scenario.getCanonicalGraph();
 			//GELog.debug(this,"buildMap(): canonicalGraph="+canonicalGraph);
 			if (canonicalGraph == null) {
 				GELog.debug(this,"buildMap(): canonicalGraph is null");
 				return null;
 			}
 			// ...and can now iterate through the nodes. Note that the method
 			// "getNodes" returns a map indexed by the URI of each of the nodes,
 			// thus we need to get the values of the map to have an actual list to
 			// iterator over.				
 			int errctr = 0;
 			for (final Iterator nodeIter = canonicalGraph.getNodes().values()
 					.iterator(); nodeIter.hasNext();) {
 				final Node node = (Node) nodeIter.next();
 			    String nodeTitle = node.getDublinCore().getTitle();
 			    //GELog.debug(this, node+" : "+ nodeTitle);
 			    boolean keep = filter(node,nodeTitle,null);
 			    if (! keep) 
 			    	continue;
 			   
                 // The node may or may not have lat/long data associated with
 				// it, if it doesn't the LatLong that's returned will be empty.
 				final LatLong latLongData = getLatLong(node);
 				if (latLongData != null && latLongData.size() > 0) {
 
 				    GEData data = new GEData(nodeTitle,latLongData);
 				    stemMap.put(nodeTitle,data);
 							  
 				} else {					
 //					if (errctr <= 2) {
 //					  GELog.debug(this,"buildMap(): No spacial data\n"+
 //							  "Name="+nodeTitle+" Node="+node);
 //					}
 					errctr++;
 				}
 				
 			} // for each node
 			
 			if (errctr > 0)
 				   GELog.debug(this,"Total buildmap errors = "+errctr);	
 			
 		} catch (Throwable e) {
 			
 			GELog.error("Failure building area map", e);
 			stemMap = null;
 		}
 		
 		
 		return stemMap;	
 	}
 	/**
 	 * Filter out nodes that we are not interested in 
 	 * Note: The important filtering is done in the 
 	 * filter method in StemKml.  This just affects gathering of 
 	 * aspect data .
 	 * 	
 	 * @param node
 	 * @param nodeTitle
 	 * @paran data GEData instance for the node
 	 * @return true if we should keep the node, false to ignore it
 	 */
 	boolean filter(Node node,String nodeTitle, GEData data) {
 //		 if (GEPreferencePage.isTest()) {
 //				if (nodeTitle.indexOf(" US-")< 0 ) {					
 //					return false;
 //				}
 //		 }
 //				// ignore alaska
 //		if (nodeTitle.indexOf(" US-AK")> 0 ) {					
 //					return false;
 //		}
 ////				 ignore hawaii
 //		if (nodeTitle.indexOf(" US-HI")> 0 ) {					
 //					return false;
 //		
 		// filter out AdminArea 0 because they "may" be container entries
 		int adminarea = getAreaType(node);
 		if (adminarea <= 0) {
 			GELog.debug(this,"Skip AdminArea 0: "+node);
 			return false;
 		}
 		// debug stuff
 		if (displayNode != null) {
 			displayNode = displayNode.toLowerCase();			
 			if (nodeTitle.toLowerCase().contains(displayNode) ) {
 				GELog.debug(this,"Display: "+nodeTitle);
 				return true;
 			} else {
 				//GELog.debug(this,"Do not Display: "+nodeTitle);
 				return false;
 			}
 			
 		}
 		return true;
 	}
 
 	/**
 	 * Get the id of the area from the DublinCore instance
 	 * @param region Node representing an AdminArea
 	 * @return 3 Char ISO id of Country
 	 */
 
 	private String getAreaID(Node region) {
 		final DublinCore dc = region.getDublinCore();
 		final String dcIdentifier = dc.getIdentifier();
 
 		final String isoKey = dcIdentifier.substring(dcIdentifier
 				.lastIndexOf("/")+1);
 	    return isoKey;
 	}
 	/**
 	 * Get the name of the area from the DublinCore instance
 	 * @param region Node representing an AdminArea
 	 * @return Name of the AdminArea
 	 */
 
 	private String getAreaName(Node region) {
 		final DublinCore dc = region.getDublinCore();
 		final String dcIdentifier = dc.getIdentifier();
 
 		final String isoKey = dcIdentifier.substring(dcIdentifier
 				.lastIndexOf("/")+1);
 		// Did we get it?
		if (isoKey != null && isoKey != "") {
 			// Yes
 			final String geographicName = 
 				GeographicNames.getName(isoKey);
 			return geographicName;
 //			final StringBuilder sb= new StringBuilder(geographicName);
 //			sb.append(" (");
 //			sb.append(isoKey);
 //			sb.append(")");
 //			return sb.toString();
 		} else {
 			return dc.getTitle();
 		}
 	}
 	
 	/**
 	 * Get the type of the Admin area from the DublinCore instance
 	 * @param region
 	 * @return 0 1 or 2 (-1 if problem)
 	 */
 
 	private int getAreaType(Node node) {
 		int areatype = -1;
 		final DublinCore dc = node.getDublinCore();
 		final String spatial = dc.getSpatial();
     
 		if (spatial != null) {
 			String mapid = spatial.substring(spatial.lastIndexOf("/") + 1);
 			// Did we get it?
			if (mapid != null && mapid != "") {
 				//   XXX_0_MAP.xml#XXX
 				mapid = mapid.substring(0, mapid.indexOf("#"));
 				mapid = mapid.substring(4, 5);
 				areatype = Integer.parseInt(mapid);
 			}
 		}
 		return areatype;
 	}
 	/**
 	 * Build a StemMap instance for a specified Spatial location
 	 * based on a Selection event from MapView
 	 * 
 	 * The StemMap is a HashMap containing GEData objects 
 	 * keyed by areaid.  The location object contains the 
 	 * geographic borders for the areaid.
 	 * 
 	 * @param region The Identifiable describing the admin area that 
 	 *               was selected.
 	 * @param gse  Instace of GeographicSelectionElements that was
 	 *             passed from MapView when mouseup occurred.
 	 * 
 	 * @return Map Key is node, element is GEData instance
 	 * 
 	 */
 	
 	private Map<String, GEData> buildSelectionMap(RegionImpl region,
 			                         GeographicSelectionElements gse) {
 		
 		try {
 			
 			DublinCore dc = region.getDublinCore();
 			String spatial = dc.getSpatial();
 			
             String uriString = spatial.replace("stemspatial:","");
             
 			stemMap = new HashMap<String, GEData>();
 			PlatformLatLongDataProvider provider = new PlatformLatLongDataProvider();
 
 			URI uriNode = URI.createURI(uriString);                
 		    LatLong latLong = provider.getLatLong(uriNode);
 		 // get id of the area
 			String id = getAreaID(region);
 			String name = getAreaName(region);
 	    	GEData data = new GEData(id,name, latLong);	    	// Use S just to get it to work
 	    	data.setValue(Aspect.getAspect("S"), 01.0);
 	    	String description = formatDescription(region,gse);
 	    	data.setDescription(description);
 	    	double longitude = gse.getLongitude();
 	    	double latitude = gse.getLatitude();
 	    	GELog.debug(longitude+":"+latitude);
 	    	//double[] center = data.getBBoxCenter();
 	    	//GELog.debug(center[0]+":"+center[1]);
 	    	// set the thumbtack on the point clicked by user
 	    	data.setPoint(longitude, latitude);
 	    	
 	    	stemMap.put(id, data);
 			
 		} catch (Throwable e) {
 			GELog.error("Failure building area map", e);
 			stemMap = null;
 			errorMessage = "Exception while building Map";
 		}
 
 		return stemMap;
 	}
 	/**
 	 * Build a StemMap instance for the specified Simulation
 	 * 
 	 * The StemMap is a HashMap containing GEData objects 
 	 * keyed by areaid.  The location object contains the 
 	 * geographic borders for the areaid.
 	 * 
 	 * @param adminFile file name to use in search
 	 * @param search string to look for 
 	 * @return Map Key is node, element is GEData instance
 	 * 
 	 */
 	
 	private Map<String, GEData> buildMap(String adminFile, String search) {
 		
 		String baseURI = 
 			"platform:/plugin/org.eclipse.stem.geography/" + 
 			"resources/data/geo/country/";
 		
 		try {
             String uriString = baseURI+adminFile;
 			stemMap = new HashMap<String, GEData>();
 			PlatformLatLongDataProvider provider = new PlatformLatLongDataProvider();
 
 			final URI uri = URI.createURI(uriString);
 			final Collection<String> ids = provider.getIds(uri);
 			if (ids.size() == 0) {
 				errorMessage = "Invalid Country code specified";
 				return stemMap;
 			}
 			String match = search.toLowerCase();
 			for (String id : ids) {
 				if (id.toLowerCase().contains(match)) {
                     URI uriNode = URI.createURI(uriString + "#" + id);                
 				    LatLong latLong = provider.getLatLong(uriNode);
 				    if (latLong != null && latLong.size() > 0) {
 
 				    	GEData data = new GEData(id, latLong);
 				    	// Use S just to get it to work
 				    	data.setValue(Aspect.getAspect("S"), 1.0);
 				    	stemMap.put(id, data);
 				    } else {
 				    	GELog.debug(this, "No spacial data" + " Name=" + id);
 				    }
 				}
 
 			} // for each id
 			if (ids.size() == 0) {
 				errorMessage = "No Matching internal Administration area found";
 			}
 		} catch (Throwable e) {
 
 			GELog.error("Failure building area map", e);
 			stemMap = null;
 			errorMessage = "Exception while building Map";
 		}
 
 		return stemMap;
 	}
 	
 	/**
 	 * Create a formated display to use for description 
 	 * <pre>
 	 *   S:   25.3% 
 	 *   I:   10.2%
 	 *   ..
 	 *   R:    0.0%
 	 * </pre>
 	 * @return formatted description
 	 */
 	private String formatDescription(RegionImpl region,
                              GeographicSelectionElements gse) {
 		StringBuffer sb = new StringBuffer(128);
 		Map<String,Double> relValues = getRelValues(region);
 		Set<String> aspects = relValues.keySet();
 		sb.append("&lt;pre&gt;");
 		sb.append(getAreaID(region));
 		sb.append(" &lt;br&gt;");
 		for(String aspect:aspects) {			
 			Double relValue = relValues.get(aspect)*100;
 			// format as "     S: 15.1%"
 			String result= String.format("%8s: %5.1f%%",aspect,relValue);
 			sb.append(result);
 			// add the html formatting 
 			sb.append(" &lt;br&gt;");
 		}
 		sb.append("&lt;pre&gt;");
 		return sb.toString();
 	}
 	
 	/**
 	 * Get relative values from the Graph.
 	 * We are given the RegionImpl for the Admin Area.
 	 * We will get the lable from it that contains 
 	 * relative values and put then in a Map with 
 	 * the value name as the key.
 	 * 
 	 * @param region
 	 * @return Map containing <name:Value>
 	 */
 	private Map<String,Double> getRelValues(RegionImpl region) {
 		Map<String,Double> map = new HashMap<String, Double>();
 		
 		List<NodeLabel> nodes = region.getLabels();
 		for(NodeLabel node:nodes) {		 
 			RelativeValueProvider rvp = 
 				(RelativeValueProvider) RelativeValueProviderAdapterFactory.INSTANCE
 					.adapt(node, RelativeValueProvider.class);
 			// Does the label have relative values?
 			if (rvp != null) {					
 				for (Iterator propertyIter = 
 					  rvp.getProperties().iterator(); propertyIter.hasNext();) {
 					ItemPropertyDescriptor property = 
 						(ItemPropertyDescriptor) propertyIter.next();			
 					String name = property.getDisplayName(property);					
 					Double value = rvp.getRelativeValue(property);
 				    map.put(name, value);								
 			    } // for				
 			}			
 		} // for
         return map;
 	}
 	/**
 	 * Get geographical border of the node. We'd like to have access to lat/long
 	 * data of the node if there is any, to do that we use a factory to create
 	 * an adapter that knows about the inner details of nodes (it really knows
 	 * about all Identifiables) and implements the LatLongProvider interface.
 	 * 
 	 * @param node
 	 * @return border of the node espressed as an array of long/latitude points.
 	 */
 	LatLong getLatLong(final Node node) {		
 
 		// The node may or may not have lat/long data associated with
 		// it, if it doesn't the list that's returned will be empty.
 
 		try {
 			final LatLongProviderAdapter latLongProvider = 
 				(LatLongProviderAdapter) LatLongProviderAdapterFactory.INSTANCE
 			    .adapt(node, LatLongProvider.class);
 			latLongProvider.setTarget(node);
 			return latLongProvider.getLatLong();
 		} catch (RuntimeException e) {
 			GELog.debug(this,e.getMessage());
 			return null;
 		}
 	}
 	
 	
     /**
 	 * Check if the specified simulation is actively 
 	 * being listened to. A simulation is active if
 	 * it is listed in the map of active simulations
 	 * and can therefore be manually displayed.
 	 * 
 	 * @param sim
 	 * @return <code>true</code> if active, <code>false</code> in not active or non existent
 	 */
     public boolean  isActive(ISimulation sim) {
     	
         String name = getSimulationId(sim);		
 		GESimEntry existing = activeSimulations.get(name);
 		return (existing == null) ? false : existing.active;
     }
     
     /**
 	 * Check if the specified simulation is the simulation
 	 * being viewed by GoogleEarth
 	 * 
 	 * @param sim
 	 * @return true if activeView
 	 */
     public boolean  isActiveView(ISimulation sim) {
     	
         String name = getSimulationId(sim);		
 		return name.equals(activeView);
     }
 	/**
 	 * Set the logging for the specified simulation.
 	 * This creates the GESimEntry which is used to collect 
 	 * information about the ISimulation instance to used 
 	 * to build KML files.
 	 * 
 	 * @param sim  Simulation instance being activated 
 	 *             (or deactivated) 
 	 * @param active  set true to write file, 
 	 *                set false to bypass write
 	 * @return true if ok, false in failed with
 	 *           errorMessage
 	 */
 	public boolean setActive(ISimulation sim, boolean active) {
 		
 		String name = getSimulationId(sim);		
 		GESimEntry existing = activeSimulations.get(name);
 		GELog.debug(this.getClass(),"setActive: "+name+"  "+active);
 		if (existing != null) {
 			// if already known about
 			if (existing.active == active) {
 				// no change in state do nothing
 				return true;
 			} else 	{
 				existing.active = active;
 				if (!existing.active) {	
 //					 do anything if deactivated????
 					if (name.equals(activeView))
 						activeView = null;				  
 				}  
 			}			
 		} else {
 			// new entry			
 			existing = new GESimEntry(name,sim);
 			existing.active = active;
 			activeSimulations.put(name, existing);
 			existing.setAspectToDisplay(GEPreferencePage.getAspect());
 			
 			//Handl this based on communication method
 			if (active) {
 				String entryFolder = existing.getLogFolder();
 				int method = GEPreferencePage.getMethod();
 				
 				switch (method) {				
 				case GEPreferencePage.M_LOG_ONLY:
 					// log the file but no launch or netlink
 					if (geView != null) {
 						entryFolder = GEPreferencePage.getFolder();
 						entryFolder = geView.verifyFolder(entryFolder);
 						existing.setLogFolder(entryFolder);
 						existing.setMethod(method);
 					} 
 					sim.addSimulationListener(this);
 					break;
 				case GEPreferencePage.M_LOG_SERVLET:
 					// log file and launch a netlink to display				
 					if (geView != null) {
 						entryFolder = GEPreferencePage.getFolder();
 						entryFolder = geView.verifyFolder(entryFolder);
 						existing.setLogFolder(entryFolder);
 					} 
 					if (entryFolder != null) {					
 						if (activeView == null) {
 							StemKml.setWaitForKML(true);
 							StemKml.launchNetworkLink(entryFolder,null);
 							activeView = name;
 							sim.addSimulationListener(this);
 							existing.setMethod(method);
 						} else {
 							errorMessage ="Simulation "+
 								activeView+
 								" already active on GoogleEarth.";
 							GELog.debug(this,errorMessage);
 							existing.active = false;
 							return false;
 						}
 					} 
 					break;
 				case GEPreferencePage.M_ASYNC_SERVLET:
 
 					// write control.kml and 
 					// launch netlink to display control.kml
 					// every n seconds
 					String kmlfn = StemKml.getControlFile();
 					File f = new File(kmlfn);
 					String parent = f.getParent()+File.separator;
 					kmlfn = f.getName();
 					kmlfn = parent+ kmlfn.replace(".","_Current.");
 					if (activeView == null) {
 						StemKml.setWaitForKML(true);
 						StemKml.launchNetworkLink(entryFolder,kmlfn);
 						activeView = name;
 						sim.addSimulationListener(this);
 						existing.setMethod(method);
 					} else {
 						errorMessage ="Simulation "+
 							activeView+
 							" already active on GoogleEarth.";
 						GELog.debug(this,errorMessage);
 						existing.active = false;
 						return false;
 					}
 
 					break;
 				case GEPreferencePage.M_AUTO_LAUNCH:
 					if (activeView == null ) {					
 						activeView = name;
 						sim.addSimulationListener(this);
 						existing.setMethod(method);
 					} else {
 						errorMessage ="Simulation "+
 								activeView+
 						" already active on GoogleEarth.";
 						GELog.debug(this,errorMessage);
 						existing.active = false;
 						sim.addSimulationListener(this);
 						return false;
 					}
 					
 					break;	
 				case GEPreferencePage.M_MANUAL_DISPLAY:
 					sim.addSimulationListener(this);
 					existing.setMethod(method);
 					break;
 
 				default:
 
 				}// switch
 			}
 		}
 		return true;		
 	}
 	/**
 	 * Set the SEIR value that wwe want displayed.
 	 * 
 	 * @param sim  Simulation instance
 	 * @param aspect the sEIRValue to set
 	 */
 	public void setAspectToDisplay(ISimulation sim, Aspect aspect) {
 		if (sim == null) {
 			setDefaultAspect(aspect);
 		} else {
 			String name = getSimulationId(sim);
 			GESimEntry existing = activeSimulations.get(name);
 
 			if (existing != null) {
 				// if already known about
 				existing.setAspectToDisplay(aspect);
 			} else {
 				setDefaultAspect(aspect);
 			}
 		}
 	}
 	/**
 	 * Get the SEIR value to be displayed from user.
 	 * 
 	 * @param sim
 	 *            Simulation instance
 	 * @return the sEIRValue to display
 	 */
 	public Aspect getAspectToDisplay(ISimulation sim) {
         String name = getSimulationId(sim);		
 		GESimEntry existing = activeSimulations.get(name);
 		Aspect value = null;
 		if (existing != null) {
 			// if already known about
 			value = existing.getAspectToDisplay();
 			
 		} else {
 			value = getDefaultAspect();
 		}
 		return value;
 	}
 	/**
 	 * display status.
 	 * 
 	 * This will display on the console a lot of 
 	 * information about the environment. 
 	 * Currently it displays the status on the console.
 	 * At some point the important status may be 
 	 * placed into a dialog box and displayed.
 	 *The main purpose of this is to have a way to 
 	 *display interesting stuff. 
 	 * It can be used as a problem reporting aid 
 	 * since one can take the output and use it in the 
 	 * problem description.  So it is important that information
 	 * that is needed in diagnosing problems with the 
 	 * STEM-GE interface be displayed here.
 	 *
 	 *
 	 *	 
 	 */
 	public void displayStatus() {
 		boolean saveDebug = GELog.DEBUG;
 		GELog.DEBUG = true;
 		// display Preferences
 		GELog.debug(this,GEPreferencePage.display());
 		final Preferences preferences = 
 			org.eclipse.stem.jobs.Activator.getDefault()
 				.getPluginPreferences();
 		boolean simulationSleep = preferences
 				.getBoolean(PreferenceConstants.SIMULATION_SLEEP_BOOLEAN);
 	    // display an important delay value.  GE needs it   
 		int simulationSleepSeconds = preferences
 				.getInt(PreferenceConstants.SIMULATION_SLEEP_MILLISECONDS_INTEGER) * 1000;
 		GELog.debug(this,"SIMULATION_SLEEP: "
 				            +simulationSleep);
 		GELog.debug(this,"SIMULATION_SLEEP_SECONDS: "
 				            +simulationSleepSeconds);
 		GELog.debug(this,"activeView: "+activeView);
 		if (activeSimulations.size() == 0) {
 			GELog.debug(this,"No active Simulations");
 
 		} else {
 			GELog.debug(this,"Active Simulations: "+activeSimulations.size());
 			for (Map.Entry entry : activeSimulations.entrySet()) {
 				GESimEntry geSim = (GESimEntry)entry.getValue();
 				String name = (String)entry.getKey();
 				ISimulation sim = geSim.getSimulation();
 				try {
 					GELog.debug(this,"  Simulation: "+							
 							" "+geSim.toString());
 					
 					GELog.debug(this,"  State: "+sim.getSimulationState());
 							
 					GESimEntry existing = activeSimulations.get(name);
 					GELog.debug(this,"  GESimEntry: "+existing);		
 					Scenario scenario = sim.getScenario();
 					GELog.debug(this,"  Scenario: "+scenario);
 					DublinCore dc = scenario.getDublinCore();
 					GELog.debug(this,"  Scenario DublinCore: "+dc);
 					Sequencer sequencer = scenario.getSequencer();
 					GELog.debug(this,"  Sequencer: "+sequencer);
 					Model model = scenario.getModel();
 					GELog.debug(this,"  Model: "+model);
 					//GELog.debug(this,"  LongLat: "+displayLatLong(sim));
 					//GELog.debug(this,"  Aspect: "+displayAspect(sim));
 					
 				} catch (Throwable t) {
 					GELog.error("Display Status failure", t);
 
 				}
 			}
 		}
 		GELog.DEBUG = saveDebug;
 	}
 	/**
 	 * display the LatLong data.
 	 * This is mostly for debug usage.
 	 * 
 	 * 
 	 * @param simulation that will be shown
 	 * @return display string of latlong data
 	 * 
 	 */
 	
 	@SuppressWarnings("unused")
 	private String displayLatLong(ISimulation simulation) {
 		final StringBuilder sb = new StringBuilder();
 		int counter = 0;
 		try {
 			sb.append("LatLong data: ");			
 			Scenario scenario = simulation.getScenario();
 			
 			//		 We get the graph...
 			final Graph canonicalGraph = scenario.getCanonicalGraph();
 			GELog.debug(this,"displayLatLong(): canonicalGraph="+canonicalGraph);
 			if (canonicalGraph == null) {
 				GELog.debug(this,"displayLatLong(): canonicalGraph is null");
 				return null;
 			}
 			// ...and can now iterate through the nodes. Note that the method
 			// "getNodes" returns a map indexed by the URI of each of the nodes,
 			// thus we need to get the values of the map to have an actual list to
 			// iterator over.				
 			for (final Iterator nodeIter = canonicalGraph.getNodes().values()
 					.iterator(); nodeIter.hasNext();) {
 				counter++;
 				if (counter>100) {
 					sb.append("\n Limit of 100 reached");
 					break;
 				}
 				final Node node = (Node) nodeIter.next();
 			    String nodeTitle = node.getDublinCore().getTitle();
 			    sb.append("\n"+"Node: "+nodeTitle);
 			    final LatLongProviderAdapter latLongProvider = (LatLongProviderAdapter) LatLongProviderAdapterFactory.INSTANCE
 				             .adapt(node, LatLongProvider.class);
 			   latLongProvider.setTarget(node);
 			   
                 // The node may or may not have lat/long data associated with
 				// it, if it doesn't the list that's returned will be empty.
 				final LatLong latLongData = latLongProvider.getLatLong();
 				if (latLongData != null && latLongData.size() > 0) {
 					//GELog.debug(this,"displayLatLong(): Node="+nodeTitle);
                     //	Now we just go through each segment and create a string.
 					for (Segment segment : latLongData.getSegments()) {
 						String data = createStringOfLatLongData(segment);
 						sb.append("LatLong: "+data);
 					} // for
 				    							  
 				} else {
 					GELog.debug(this,"buildMap()(): No spacial data for Node="+nodeTitle);
 				}
 				
 			} // for each node
 			
 				
 			
 		} catch (Throwable e) {
 			
 			GELog.error("Failure building area map", e);
 			
 		}
 		return sb.toString();	
 	}
 	
 	/**
 	 * display the Aspect data.
 	 * This is mostly for debug
 	 * 
 	 * @param simulation that will be shown
 	 * @return Display string containing info about SEIR valued
 	 * 
 	 */
 
 	@SuppressWarnings("unused")
 	private String displayAspect(ISimulation simulation) {
 		final StringBuilder sb = new StringBuilder();
 		try {
             int counter = 0;
 			Scenario scenario = simulation.getScenario();		
 					
 			//		 We get the decorators from the scenario ...			
 
			Decorator[] decorators = (Decorator[]) (scenario.getCanonicalGraph()
					.getDecorators().toArray());
 			for (Decorator decorator : decorators) {
 				GELog.debug(this,"decorator: "+decorator.toString());
 				
 				Object[] labels = decorator.getLabelsToUpdate().toArray();
 				for (Object element: labels){
 					counter++;
 					if (counter>100) {
 						sb.append("\n Limit of 100 reached");
 						break;
 					}
 					if ( element instanceof NodeLabel) {
 						NodeLabel nodeLabel = (NodeLabel)element;
 						Node node = nodeLabel.getNode();
 						String title = node.getDublinCore().getTitle();
                         sb.append("\nNode: "+title+" ");
 						RelativeValueProviderAdapter rvp = (RelativeValueProviderAdapter) RelativeValueProviderAdapterFactory.INSTANCE
 						.adapt(element, RelativeValueProvider.class);
 						rvp.setTarget((Notifier)element);
 						
 						// Does the label have relative values?
 						if (rvp != null) {							
 							for (Iterator propertyIter = rvp.getProperties().iterator(); propertyIter
 							.hasNext();) {
 								ItemPropertyDescriptor property = (ItemPropertyDescriptor) propertyIter
 								.next();
 								sb.append(property.getDisplayName(property));
 								sb.append(": ");
 
 								sb.append(String.format("%.2f", rvp.getRelativeValue(property)));
 
 								if (propertyIter.hasNext()) {
 									sb.append(", ");
 								}
 							} // for each property
                             
 						} // if
 						
 					}
 				}  // for
 			}
 
 
 		} catch (Throwable e) {
 			GELog.error("Failure getting SEIR data", e);
 		}
 		return sb.toString();	
 
 	}
 	/** 
 	 * Get the ID to use for getting the GESimEntry instance
 	 * @param simulation
 	 * @return "SIM[sequenceNumber"]"
 	 */
 	public static String getSimulationId(ISimulation simulation) {
 		if (simulation != null)
 		  return "SIM["+
 		      Integer.toString(simulation.getSequenceNumber())+
 		    		  "]";
 		else 
 		  return null;
 	}
 	
 	/**
 	 * Display the latlong data from a LatLong segment
 	 * A segment is a line or polygon.
 	 * 
 	 * @param segment
 	 * @return a string of the lat/long points
 	 */
 	private static String createStringOfLatLongData(final Segment segment) {
 		final StringBuilder sb = new StringBuilder();
 		final int size = segment.size();
 		for (int i = 0; i < size; i++) {
 			sb.append("(");
 			sb.append(segment.latitude(i));
 			sb.append(", ");
 			sb.append(segment.longitude(i));
 			sb.append(")");
 		} // for each lat/long pair
 		return sb.toString();
 	} // createStringOfLatLongData
 
 
 
 
 	/**
 	 * @return the defaultAspect
 	 */
 	public Aspect getDefaultAspect() {
 		if (defaultAspect == null) 
 			defaultAspect = Aspect.getDefault();
 		return defaultAspect;
 	}
 
 
 
 
 	/**
 	 * @param defaultAspect the defaultAspect to set
 	 */
 	public void setDefaultAspect(Aspect defaultAspect) {
 		this.defaultAspect = defaultAspect;
 	}
 
 
 
 	/**
 	 * Set an error message to display
 	 * @param msg 
 	 * 
 	 */
 	public void setErrorMessage(String msg) {
 		errorMessage = msg;
 	}
 	/**
 	 * @return the errorMessage
 	 */
 	public String getErrorMessage() {
 		if (errorMessage == null) 
 			return "";
 		else 
 			return errorMessage;
 	}
 
 	/**
 	 * @param sim Simulation that is being processed.
 	 * @return the cycle
 	 */
 	public int getCycle(ISimulation sim) {
 		Scenario scenario = sim.getScenario();		
 		Sequencer seq = scenario.getSequencer();
 		int cycle = seq.getCycle();
 		return cycle;
 	}
 	
 	
 } // class GEInterface
 	
 
