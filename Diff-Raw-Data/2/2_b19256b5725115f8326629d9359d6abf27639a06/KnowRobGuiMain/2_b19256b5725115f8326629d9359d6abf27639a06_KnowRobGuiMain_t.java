 /*
  *    Copyright (C) 2012
  *      ATR Intelligent Robotics and Communication Laboratories, Japan
  *
  *    Permission is hereby granted, free of charge, to any person obtaining a copy
  *    of this software and associated documentation files (the "Software"), to deal
  *    in the Software without restriction, including without limitation the rights
  *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  *    of the Software, and to permit persons to whom the Software is furnished to do so,
  *    subject to the following conditions:
  *
  *    The above copyright notice and this permission notice shall be included in all
  *    copies or substantial portions of the Software.
  *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  *    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
  *    PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  *    HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  *    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  *    SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package jp.atr.unr.pf.gui;
 
 import java.awt.Color;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import processing.core.PApplet;
 import ros.Ros;
 import edu.tum.cs.ias.knowrob.prolog.PrologInterface;
 import edu.tum.cs.ias.knowrob.prolog.PrologQueryUtils;
 
 
 /**
  * Main class of the teleoperation interface combining knowledge from
  * RoboEarth with execution based on the UNR-Platform.
  * 
  * @author Moritz Tenorth, tenorth@atr.jp
  *
  */
 public class KnowRobGuiMain extends PApplet implements MouseListener, MouseMotionListener  {
 
 	private static final long serialVersionUID = 3248733082317739051L;
 	private static Ros ros;
 	protected String mapOwlFile = "";
 	protected String recipeOwlFile = "";
 
 	protected KnowRobGuiApplet gui;
 	
 //	protected UnrExecutive executive=null;
 	private String recipeClass;
 	private String recipeURL = null;
 	
 	private String mapInstance;
 	private String mapURL = null;
 	
 	
 	/**
 	 * Initialization: start Prolog engine and set up the GUI.
 	 */
 	public void setup () {
 		
 		size(1250, 750, P2D);
 		frameRate(10);
 		background(40);
 		
 		//initLocalProlog("prolog/init.pl");
		PrologInterface.initJPLProlog("mod_vis");
 		new jpl.Query("use_module(library('knowrob_coordinates'))").oneSolution();
 		new jpl.Query("use_module(library('comp_similarity'))").oneSolution();
 		
 		if (this.frame != null)
 		{
 		    this.frame.setTitle("UNR teleoperation console");
 		    this.frame.setBackground(new Color(40, 40, 40));
 		    this.frame.setResizable(true);
 		}
 		
 		gui = new KnowRobGuiApplet();
 		gui.setFrame(this.frame);
 		gui.setOperatorMain(this);
 		gui.init();
 		gui.setBounds(0, 0, 1250,750);
 		this.add(gui);
 
 		// ROS initialization only needed for communication visualization
 //		initRos();
 	}
 	
 	public void draw() {
 		background(40);
 	}
 
 
 	/**
 	 * Thread-safe ROS initialization
 	 */
 	protected static void initRos() {
 
 		ros = Ros.getInstance();
 
 		if(!ros.isInitialized()) {
 			ros.init("knowrob_re_client");
 		}
 	}
 	
 //	
 //	/** 
 //	 * Start the UNR-PF execution engine and connect to the platform. 
 //	 */
 //	public void startExecutionEngine() {
 //
 //		try {
 //			
 //			executive = new UnrExecutive();
 //
 //			vis = new PlanVisActionVisualizer(gui.planvis);
 //			executive.setVis(vis);
 //			executive.setNotificationListener(gui);
 //		
 //			executive.initUnrInterface();
 //			
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //	}
 //	
 //	/**
 //	 * Stop the UNR-PF engine and disconnect from the platform.
 //	 */
 //	public void stopExecutionEngine() {
 //		try {
 //			executive.resetUnrInterface();
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //	}
 //
 //	/**
 //	 * Execute the current recipe (as stored in the recipeClass field) 
 //	 * using the UNR Platform.
 //	 */
 //	public void executeRecipeClass() {
 //		
 //		try {
 //			
 //			// synchronize Prolog knowledge base with current recipe editor state
 //			gui.planvis.getCurrTask().setSaveToProlog(true);
 //			gui.planvis.getCurrTask().writeToProlog();
 //			
 //			if(executive != null)
 //				executive.executeRecipeClass(gui.planvis.getCurrTask().getIRI());
 //			
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 
 
 	/**
 	 * Set the internal field storing the current map OWL file, and load this OWL file
 	 * into the Prolog engine and the visualization.
 	 * 
 	 * @param mapOwlFile File name to be loaded
 	 * @param url URL of the map in RoboEarth, or null if not applicable
 	 * @param streetName 
 	 * @param streetNumber 
 	 * @param floorNumber 
 	 * @param roomNumber 
 	 */
 	public void setMapOwlFile(String mapOwlFile, String url, String roomNumber, String floorNumber, String streetNumber, String streetName) {
 		
 		this.mapOwlFile = mapOwlFile;
 		gui.map_forms.loadInputFile(mapOwlFile);
 		this.mapInstance = PrologInterface.removeSingleQuotes(PrologQueryUtils.getSemanticMapInstance(roomNumber, floorNumber, streetNumber, streetName));
 		
 		this.mapURL = url;
 	}
 
 
 
 	/**
 	 * Set the internal field storing the current recipe OWL file, and load this OWL file
 	 * into the Prolog engine and the visualization.
 	 * 
 	 * @param recipeOwlFile File name to be loaded
 	 * @param url URL of the action recipe in RoboEarth, or null if not applicable
 	 */
 	public void setRecipeOwlFile(String recipeOwlFile, String command, String url) {
 		
 		PrologQueryUtils.parseOwlFile(recipeOwlFile);
 
 		this.recipeOwlFile = recipeOwlFile;
 		this.recipeClass = PrologInterface.removeSingleQuotes(PrologQueryUtils.readRecipeClass(command));
 		this.recipeURL = url;
 		
 		gui.planvis.loadPrologPlan(recipeClass);
 
 		gui.planvis.drawActionsTreeLayout();
 		gui.planvis.redraw();
 	}
 	
 	/** 
 	 * Get the file name of the OWL file describing the currently loaded action recipe
 	 * 
 	 * @return File name of the recipe OWL file
 	 */
 	public String getRecipeOwlFile() {
 		return recipeOwlFile;
 	}
 
 	public String getRecipeURL() {
 		return recipeURL;
 	}
 
 	public void setRecipeURL(String recipeURL) {
 		this.recipeURL = recipeURL;
 	}
 	
 	/** 
 	 * Get the file name of the OWL file describing the currently loaded semantic map
 	 * 
 	 * @return File name of the map OWL file
 	 */
 	public String getMapOwlFile() {
 		return mapOwlFile;
 	}
 
 	public String getMapURL() {
 		return mapURL;
 	}
 
 	public void setMapURL(String mapURL) {
 		this.mapURL = mapURL;
 	}
 
 	public String getMapInstance() {
 		return mapInstance;
 	}
 
 	public void setMapInstance(String mapInstance) {
 		this.mapInstance = mapInstance;
 	}
 
 	
 	
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "jp.atr.unr.pf.gui.KnowRobGuiMain" });
 	}
 }
