 package org.uav.simulation;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.graphstream.graph.Edge;
 import org.graphstream.graph.Node;
 import org.graphstream.graph.implementations.DefaultGraph;
 import org.graphstream.stream.file.FileSinkImages;
 import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
 import org.graphstream.stream.file.FileSinkImages.OutputType;
 import org.graphstream.stream.file.FileSinkImages.Quality;
 import org.graphstream.stream.file.FileSinkImages.RendererType;
 import org.graphstream.stream.file.FileSinkImages.Resolution;
 import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
 import org.uav.autopilot.AutopilotFixedWingOutOfSimulationAreaStrategy;
 import org.uav.autopilot.Destination;
 import org.uav.AbstractUAV;
 import org.uav.UAVBasicConnectedCoverage;
 import org.uav.UAVBasicPheromoneMovement;
 import org.uav.UAVBasicPheromoneWithRepulsion;
 import org.uav.UAVRandomDestination;
 import org.uav.UAVRandomMovement;
 import org.uav.aco.AbstractACO;
 import org.uav.aco.CentralizedACO;
 import org.uav.aco.DistributedACO;
 import org.uav.area.AbstractSimulationArea;
 import org.uav.area.RectangularSimulationArea;
 import org.uav.graph.UAVGraph;
 import org.uav.graph.UAVGraphRepresentationTwoNodes;
 import org.uav.metrics.MetricAvgNbScans;
 import org.uav.metrics.MetricGiantConnectedComponent;
 import org.uav.metrics.MetricNbCellsOlderThanThreshold;
 import org.uav.metrics.MetricNbConnectedComponents;
 import org.uav.metrics.MetricNbNeverScannedCells;
 import org.uav.metrics.MetricNbScannedCells;
 import org.uav.metrics.MetricNbUAVConnectedToBase;
 import org.uav.metrics.MetricStdNbScans;
 import org.uav.metrics.MetricsManager;
 import org.uav.status.Heading;
 import org.uav.status.Position;
 import org.uav.status.Speed;
 import org.uav.status.Status;
 import org.uav.status.TurnRate;
 import org.uav.wireless.HomogeneousPropagationModel;
 import org.uav.wireless.AbstractPropagationModel;
 
 /**
  * @author Julien Schleich
  * The main class to launch a simulation
  * Even though using the simulation batch class is recommended 
  */
 
 public class Simulation {
     private boolean display = true;
     private boolean screenshot = false;
 
     /**
      * Main graph
      */
     protected DefaultGraph g;
 
     /**
      * UAV parameters
      */
     public String uavType;
     protected int nbUAV;
     public UAVGraph uavGraph;
     public AbstractUAV[] u; 
     public UAVGraphRepresentationTwoNodes[] u_g;
 
     /// Speed
     protected int maxSpeed = 10;
     protected int minSpeed = 0;
     protected int maxAccel = 1;
     protected int maxDecel = 1;
     protected int initSpeed = 0;
 
     /// Heading
     protected double maxHeadingChange = 0.1;
     protected double initHeading = Math.PI/2;
 
     /// Future
     protected int localDecisionFrequency = 10;
     protected int nbTimestepsInTheFuture = localDecisionFrequency;
 
     /**
      * Simulation area
      */
     protected AbstractSimulationArea area;
     protected int maxX = 2000;
     protected int maxY = 1000;
     protected int granularity = 20;
 
     protected int baseX = 1000;
     protected int baseY = 0;
 
     /**
      * Simulation
      */
     public Random rand;
     public long seed;
 
     protected int maxTimesteps = 5000;
     protected int warmingSteps = 500;
     protected int bigCpt = 0;
 
     /// ACO parameters
     protected double evaporationSpeed = 0.01;
     protected int evaporationFreq = 2;
 
     /// Statistics
     protected boolean useScanTimes = true;
     protected boolean useLastUAVId = true;
     protected int tooOld = 20;
     protected int statsFreq = 100;
 
     /**
      * Wireless
      */
     protected AbstractPropagationModel propa;
 
     /**
      * Ant Colony Optimization
      */
     protected AbstractACO aco;
 
     /**
      * Statistics
      */
     protected MetricsManager stats;
     public String statsFile;
 
 
     /**
      * Initialisation methods
      */
     public void initGraphs(){
 	/** Main graph */
 	g = new DefaultGraph("uavNetwork");
 	/** UAV graph */
 	uavGraph = new UAVGraph(this);
     }
 
     public void initDisplay(){
 	if (display) {
 	    g.setAttribute("ui.stylesheet", "url('style.css')");
 	    g.addAttribute("ui.quality");
 	    g.addAttribute("ui.antialias");
 	    g.display(false);
	    Viewer v = g.display();
	    View view = v.getDefaultView();
	    view.resizeFrame(1280,720);
	    //view.setViewCenter(440000, 2503000, 0);
	    //view.setViewPercent(0.25);
 	    double minValue = 2.0;
 	    double maxValue = 4.0;
 	    Random rand = new Random(seed);
 	    for (Edge e: g.getEdgeSet()){
 		double value = (rand.nextDouble()*(maxValue/minValue)) 
 			+ minValue;
 		e.addAttribute("attribute", value);
 	    }
 
 	}
     }
 
     public FileSinkImages initScreenshotModule() throws IOException{
 	if (this.screenshot){
 	    OutputPolicy outputPolicy = OutputPolicy.NONE;
 	    String prefix = "ph_";
 	    OutputType type = OutputType.PNG;
 	    Resolution resolution = Resolutions.HD720;
 	    FileSinkImages fsi = 
 		    new FileSinkImages(prefix, type, resolution, outputPolicy );
 	    fsi.setStyleSheet("url('style.css')");
 	    fsi.setQuality(Quality.HIGH);
 	    fsi.setRenderer(RendererType.SCALA);
	    /* fsi.addLogo("/Users/bobdoulz/dev/coverage/src/banniere.png", 
		    280, 620);*/
 	    g.addSink(fsi);
 	    fsi.begin(prefix);
 	    return fsi;
 	}
 	else return null;
     }
 
     public void initSimulationArea(){
 	area = new RectangularSimulationArea(g, getMaxX(), 
 		getMaxY(), getGranularity()); 
 	((RectangularSimulationArea)area).setBaseX(baseX);
 	((RectangularSimulationArea)area).setBaseY(baseY);
 	area.generateEnvironment();
     }
 
     public void initStatisticsModule() {
 	stats = new MetricsManager(this);
 	stats.addMetric(new MetricNbScannedCells(this));
 	stats.addMetric(new MetricNbNeverScannedCells(this));
 	stats.addMetric(new MetricNbCellsOlderThanThreshold(this));
 	stats.addMetric(new MetricAvgNbScans(this));
 	stats.addMetric(new MetricStdNbScans(this));
 	stats.addMetric(new MetricNbConnectedComponents(this));
 	stats.addMetric(new MetricGiantConnectedComponent(this));
 	stats.addMetric(new MetricNbUAVConnectedToBase(this));
 	stats.addTitleLineToFile();
     }
 
     public void initializeUAVs(){
 	u = new AbstractUAV[getNbUAV()];
 	u_g = new UAVGraphRepresentationTwoNodes[getNbUAV()];
 	for (int i=0; i < getNbUAV(); i++)
 	    addUAV(i);
     }
 
     public void initializeACO(){
 	if (	(this.uavType == "UAVBasicConnectedCoverage") ||
 		(this.uavType == "UAVRandomMovement") ||
 		(this.uavType == "UAVRandomDestination") ||
 		(this.uavType == "UAVBasicPheromoneMovement") ||
		(this.uavType == "UAVBasicPheromoneWithRepulsion")){
 	    aco = new CentralizedACO(this);
 	}
 
 	if (this.uavType == "UAVDistributedPheromoneMovement") {
 	    aco = new DistributedACO(this);
 	}
 
 	for (int i=0; i < getNbUAV(); i++){
 	    if (this.uavType == "UAVBasicConnectedCoverage"){
 		UAVBasicConnectedCoverage uav = (UAVBasicConnectedCoverage)u[i];
 		uav.setAco(aco);
 	    }
 	    if (this.uavType == "UAVBasicPheromoneMovement"){
 		UAVBasicPheromoneMovement uav = (UAVBasicPheromoneMovement)u[i];
 		uav.setAco(aco);
 	    }
	    if (this.uavType == "UAVBasicPheromoneWithRepulsion"){
 		UAVBasicPheromoneWithRepulsion uav = 
 			(UAVBasicPheromoneWithRepulsion)u[i];
 		uav.setAco(aco);
 	    }
 	    /** To activate when the related class are finished 
 	    if (this.uavType == "UAVDistributedPheromoneMovement"){
 		UAVDistributedPheromoneMovement uav = 
 		(UAVDistributedPheromoneMovement)u[i];
 		uav.setAco(aco);
 	    }
 	     */
 	}
 
 
     }
 
     public void runSimulation() throws InterruptedException, IOException{
 	/**
 	 * Initialization
 	 */
 
 	/** Random number */
 	rand = new Random(this.seed);
 	/** Initialization of the different graphs */
 	initGraphs();
 	/** Initialization of the display (if needed) */
 	initDisplay();
 	/** Initialization of the screenshot module (if needed) */
 	FileSinkImages fsi = initScreenshotModule();
 	/** Building of the simulation area */
 	initSimulationArea();
 	/** Initialization of the statistic module */
 	initStatisticsModule();
 	/** Wireless module */
 	propa = new HomogeneousPropagationModel();
 	/** Initialization of the UAVs */
 	initializeUAVs();
 	/** Initialize Ant Colony Optimization (ACO) module */
 	initializeACO();
 
 	/**
 	 * Main simulation loop
 	 */
 	boolean alive = true;
 	//System.out.println(stats.getMetricsNames());
 	while (alive){
 	    /** Moving UAVs */
 	    for (int i=0; i<nbUAV; i++){
 		u[i].doSimulationStep();
 	    }
 
 	    /** ACO Part */
 	    if (bigCpt>warmingSteps){
 		for (int i=0; i<nbUAV; i++){
 		    aco.dropPheromone(
 			    null, 
 			    u[i].getAuto().getCurrentStatus().getCurrentPosition(), 
 			    "pheromone", 
 			    u[i].getUavId());
 		}
 		if (bigCpt % evaporationFreq == 0){
 		    aco.evaporation();
 		}
 	    }
 
 	    /** Refreshing the display */
 	    refreshNodesOnGraph();
 	    refreshLinksOnGraph();
 	    area.refreshDisplay();
 
 	    /** For display purpose the processing is slowed */
 	    if (display){
 		Thread.sleep(40);
 	    }
 
 	    /** Statistics */		
 	    if (bigCpt>warmingSteps){
 		if (bigCpt % statsFreq == 0){
 		    String str = stats.appendCurrentValuesToFile();
 		    //System.out.println(str);
 		}
 	    }
 
 	    /** For screenshot purpose */
 	    if (screenshot)
 		if (fsi != null)
 		    fsi.outputNewImage();
 
 	    /** Counters */
 	    bigCpt ++;
 	    if (bigCpt == maxTimesteps)
 		alive = false;
 	}
 	/** For screenshot purpose */
 	if (fsi != null)
 	    fsi.end();
 	/** Closes the stats file */ 
 	stats.closeFile();
     }
 
 
     /*********************************************
      * 
      * Graph Representation methods 
      * 
      ********************************************/
     /**
      * This method deals with adding a UAV
      * @param id the identifier of the UAV
      */
     public void addUAV(int id){
 	/** Initial position */
 	Position initPos = new Position(getBaseX(), getBaseY());
 	/** Initial status */
 	Status initStatus = new Status(
 		initPos, 
 		new Heading(getInitHeading(), getMaxHeadingChange()), 
 		new Speed(getInitSpeed(), getMinSpeed(), getMaxSpeed(), 
 			getMaxAccel(), getMaxDecel()), 
			new TurnRate());
 	/** The autopilot */
 	double destX, destY;
 	destX = getRand().nextDouble()*getMaxX();
 	destY = getRand().nextDouble()*getMaxY();
 	AutopilotFixedWingOutOfSimulationAreaStrategy auto = 
 		new AutopilotFixedWingOutOfSimulationAreaStrategy(
 			new Destination(destX, destY, g), 
 			initStatus, area);
 
 	/** Local decision frequency (in simulation steps) */
 	AbstractUAV.setDecisionFrequency(getLocalDecisionFrequency());
 
 	/** The actual UAV instance creation */
 	if (this.uavType == "UAVBasicPheromoneMovement")
 	    u[id] = new UAVBasicPheromoneMovement(auto, id, this.aco);
	if (this.uavType == "UAVBasicPheromoneWithRepulsion")
 	    u[id] = new UAVBasicPheromoneWithRepulsion(auto, id, 
 		    this.uavGraph, this.aco);
 	/** To activate when the related classes are finished 
 	if (this.uavType == "UAVDistributedPheromoneMovement")
 	    u[id] = new UAVDistributedPheromoneMovement(auto, id, 
 	    this.aco, 100, 200);
 	 */
 	if (this.uavType == "UAVRandomMovement")
 	    u[id] = new UAVRandomMovement(auto, id);
 	if (this.uavType == "UAVBasicConnectedCoverage") 
 	    u[id] = new UAVBasicConnectedCoverage(auto, id, this.uavGraph, 
 		    this.g.getNode("base"), this.aco, 10, 
 		    getNbTimestepsInTheFuture());
 	if (this.uavType == "UAVRandomDestination")
 	    u[id] = new UAVRandomDestination(auto, id);
 	if (this.uavType == "UAVRandomMovement")
 	    u[id] = new UAVRandomMovement(auto, id);
 
 	u[id].setSeed(getSeed());
 
 	/** The graph representation of the UAV */
 	u_g[id] = new UAVGraphRepresentationTwoNodes(g, u[id].getUavId());
 	u_g[id].getFront().setAttribute("ui.label", u[id].getUavId());
 
 	/** The initial position of the UAVs */
 	Node n = this.uavGraph.addNode(u[id]);
 	n.setAttribute("x", getBaseX());
 	n.setAttribute("y", getBaseY());
     }
 
     /**
      * Triggers the refresh on the display of the UAV 
      */
     public void refreshNodesOnGraph(){
 	for (int i=0; i < getNbUAV(); i++){
 	    /** Main graph */
 	    u_g[i].refreshPositionOnGraph(u[i].getAuto().getCurrentStatus());
 	    /** Uav graph */
 	    Node n = this.uavGraph.getUavGraph().getNode("uav"+i);
 	    n.setAttribute(
 		    "x", 
 		    u[i].getAuto().getCurrentStatus().getCurrentPosition().getX());
 	    n.setAttribute(
 		    "y", 
 		    u[i].getAuto().getCurrentStatus().getCurrentPosition().getY());
 	}
     }
 
     /**
      * Refresh the links between UAVs depending on the propagation model
      */
     public void refreshLinksOnGraph(){
 	Node base = this.area.getG().getNode("base");
 	Node baseUavGraph = this.uavGraph.getUavGraph().getNode("base");
 	for (int i=0; i < getNbUAV(); i++){
 	    Node me = u_g[i].getMainNode();  
 	    int myId = u_g[i].getId();
 	    Node meUavGraph = this.uavGraph.getUavGraph().getNode("uav"+myId);
 	    /** We check if the uav is in sight of the base */
 	    if (HomogeneousPropagationModel.areCloseEnoughToDecode(me, base)){
 		if ( (!me.hasEdgeToward(base)) || (!base.hasEdgeToward(me)) ) {
 		    /** Update the simulation graph */
 		    Edge e = 
 			    this.area.getG().addEdge(me.getId()+"_"+base.getId(), 
 				    me, base);
 		    e.addAttribute("ui.class", "neighbor");	
 		    /** Update the uav graph */
 		    this.uavGraph.addEdge(
 			    meUavGraph.getId()+"_"+baseUavGraph.getId(), 
 			    meUavGraph, baseUavGraph);
 		}
 	    }
 	    else{
 		if (this.area.getG().getEdge(
 			me.getId()+"_"+base.getId()) != null){
 		    /** Update the simulation graph */
 		    this.area.getG().removeEdge(me, base);
 		    /** Update the uav graph */
 		    this.uavGraph.removeEdge(meUavGraph, baseUavGraph);
 		}
 	    }
 
 	    /** We check all the other UAV except us */
 	    for (int j = 0; j< getNbUAV(); j++){
 		if (myId != j){
 		    Node neighbor = this.area.getG().getNode(j+"_m");
 		    Node neighborUavGraph = 
 			    this.uavGraph.getUavGraph().getNode("uav"+j);
 		    /** If neighbor is close enough, create edge if needed */
 		    if (HomogeneousPropagationModel.areCloseEnoughToDecode(
 			    me, neighbor)){
 			if (!me.hasEdgeToward(neighbor)){
 			    /** Update the simulation graph */
 			    Edge e = this.area.getG().addEdge(
 				    me.getId()+"_"+neighbor.getId(),
 				    me, neighbor);
 			    e.addAttribute("ui.class", "neighbor");
 			    /** Update the uav graph */
 			    this.uavGraph.addEdge(
 				    meUavGraph.getId()+"_"+neighborUavGraph.getId(), 
 				    meUavGraph, neighborUavGraph);
 			}
 		    }
 		    /** If neighbor is NOT close enough, delete existing edges */
 		    else{
 			if (this.area.getG().getEdge(
 				me.getId()+"_"+neighbor.getId()) != null){
 			    /** Update the simulation graph */
 			    this.area.getG().removeEdge(me, neighbor);
 			    /** Update the uav graph */
 			    this.uavGraph.removeEdge(
 				    meUavGraph, neighborUavGraph);
 			}
 			if (this.area.getG().getEdge(
 				neighbor.getId()+"_"+me.getId()) != null){
 			    /** Update the simulation graph */
 			    this.area.getG().removeEdge(neighbor, me);
 			    /** Update the uav graph */
 			    this.uavGraph.removeEdge(
 				    neighborUavGraph, meUavGraph);
 			}
 		    }
 		}
 	    }
 	}
     }
 
 
 
     /*********************************************
      * 
      * Getters / Setters
      * 
      ********************************************/
     /**
      * @return the warmingSteps
      */
     public int getWarmingSteps() {
 	return warmingSteps;
     }
 
     /**
      * @param warmingSteps the warmingSteps to set
      */
     public void setWarmingSteps(int warmingSteps) {
 	this.warmingSteps = warmingSteps;
     }
 
 
 
     /**
      * @return the display
      */
     public boolean isDisplay() {
 	return display;
     }
 
 
     /**
      * @return the screenshot
      */
     public boolean isScreenshot() {
 	return screenshot;
     }
 
 
     /**
      * @return the g
      */
     public DefaultGraph getG() {
 	return g;
     }
 
 
     /**
      * @return the nbUAV
      */
     public int getNbUAV() {
 	return nbUAV;
     }
 
 
     /**
      * @return the maxSpeed
      */
     public int getMaxSpeed() {
 	return maxSpeed;
     }
 
 
     /**
      * @return the minSpeed
      */
     public int getMinSpeed() {
 	return minSpeed;
     }
 
 
     /**
      * @return the maxAccel
      */
     public int getMaxAccel() {
 	return maxAccel;
     }
 
 
     /**
      * @return the maxDecel
      */
     public int getMaxDecel() {
 	return maxDecel;
     }
 
 
     /**
      * @return the initSpeed
      */
     public int getInitSpeed() {
 	return initSpeed;
     }
 
 
     /**
      * @return the maxHeadingChange
      */
     public double getMaxHeadingChange() {
 	return maxHeadingChange;
     }
 
 
     /**
      * @return the initHeading
      */
     public double getInitHeading() {
 	return initHeading;
     }
 
 
     /**
      * @return the localDecisionFrequency
      */
     public int getLocalDecisionFrequency() {
 	return localDecisionFrequency;
     }
 
 
     /**
      * @return the area
      */
     public AbstractSimulationArea getArea() {
 	return area;
     }
 
 
     /**
      * @return the maxX
      */
     public int getMaxX() {
 	return maxX;
     }
 
 
     /**
      * @return the maxY
      */
     public int getMaxY() {
 	return maxY;
     }
 
 
     /**
      * @return the granularity
      */
     public int getGranularity() {
 	return granularity;
     }
 
 
     /**
      * @return the baseX
      */
     public int getBaseX() {
 	return baseX;
     }
 
 
     /**
      * @return the baseY
      */
     public int getBaseY() {
 	return baseY;
     }
 
 
     /**
      * @return the rand
      */
     public Random getRand() {
 	return rand;
     }
 
 
     /**
      * @return the maxTimesteps
      */
     public int getMaxTimesteps() {
 	return maxTimesteps;
     }
 
 
     /**
      * @return the bigCpt
      */
     public int getBigCpt() {
 	return bigCpt;
     }
 
 
     /**
      * @return the evaporationSpeed
      */
     public double getEvaporationSpeed() {
 	return evaporationSpeed;
     }
 
 
     /**
      * @return the evaporationFreq
      */
     public int getEvaporationFreq() {
 	return evaporationFreq;
     }
 
 
     /**
      * @return the tooOld
      */
     public int getTooOld() {
 	return tooOld;
     }
 
 
     /**
      * @return the statsFreq
      */
     public int getStatsFreq() {
 	return statsFreq;
     }
 
 
     /**
      * @return the propa
      */
     public AbstractPropagationModel getPropa() {
 	return propa;
     }
 
 
     /**
      * @return the aco
      */
     public AbstractACO getAco() {
 	return aco;
     }
 
 
     /**
      * @param display the display to set
      */
     public void setDisplay(boolean display) {
 	this.display = display;
     }
 
 
     /**
      * @param screenshot the screenshot to set
      */
     public void setScreenshot(boolean screenshot) {
 	this.screenshot = screenshot;
     }
 
 
     /**
      * @param g the g to set
      */
     public void setG(DefaultGraph g) {
 	this.g = g;
     }
 
 
     /**
      * @param nbUAV the nbUAV to set
      */
     public void setNbUAV(int nbUAV) {
 	this.nbUAV = nbUAV;
     }
 
 
     /**
      * @param maxSpeed the maxSpeed to set
      */
     public void setMaxSpeed(int maxSpeed) {
 	this.maxSpeed = maxSpeed;
     }
 
 
     /**
      * @param minSpeed the minSpeed to set
      */
     public void setMinSpeed(int minSpeed) {
 	this.minSpeed = minSpeed;
     }
 
 
     /**
      * @param maxAccel the maxAccel to set
      */
     public void setMaxAccel(int maxAccel) {
 	this.maxAccel = maxAccel;
     }
 
 
     /**
      * @param maxDecel the maxDecel to set
      */
     public void setMaxDecel(int maxDecel) {
 	this.maxDecel = maxDecel;
     }
 
 
     /**
      * @param initSpeed the initSpeed to set
      */
     public void setInitSpeed(int initSpeed) {
 	this.initSpeed = initSpeed;
     }
 
 
     /**
      * @param maxHeadingChange the maxHeadingChange to set
      */
     public void setMaxHeadingChange(double maxHeadingChange) {
 	this.maxHeadingChange = maxHeadingChange;
     }
 
 
     /**
      * @param initHeading the initHeading to set
      */
     public void setInitHeading(double initHeading) {
 	this.initHeading = initHeading;
     }
 
 
     /**
      * @param localDecisionFrequency the localDecisionFrequency to set
      */
     public void setLocalDecisionFrequency(int localDecisionFrequency) {
 	this.localDecisionFrequency = localDecisionFrequency;
     }
 
 
     /**
      * @param area the area to set
      */
     public void setArea(AbstractSimulationArea area) {
 	this.area = area;
     }
 
 
     /**
      * @param maxX the maxX to set
      */
     public void setMaxX(int maxX) {
 	this.maxX = maxX;
     }
 
 
     /**
      * @param maxY the maxY to set
      */
     public void setMaxY(int maxY) {
 	this.maxY = maxY;
     }
 
 
     /**
      * @param granularity the granularity to set
      */
     public void setGranularity(int granularity) {
 	this.granularity = granularity;
     }
 
 
     /**
      * @param baseX the baseX to set
      */
     public void setBaseX(int baseX) {
 	this.baseX = baseX;
     }
 
 
     /**
      * @param baseY the baseY to set
      */
     public void setBaseY(int baseY) {
 	this.baseY = baseY;
     }
 
 
     /**
      * @param rand the rand to set
      */
     public void setRand(Random rand) {
 	this.rand = rand;
     }
 
 
     /**
      * @param maxTimesteps the maxTimesteps to set
      */
     public void setMaxTimesteps(int maxTimesteps) {
 	this.maxTimesteps = maxTimesteps;
     }
 
 
     /**
      * @param bigCpt the bigCpt to set
      */
     public void setBigCpt(int bigCpt) {
 	this.bigCpt = bigCpt;
     }
 
 
     /**
      * @param evaporationSpeed the evaporationSpeed to set
      */
     public void setEvaporationSpeed(double evaporationSpeed) {
 	this.evaporationSpeed = evaporationSpeed;
     }
 
 
     /**
      * @param evaporationFreq the evaporationFreq to set
      */
     public void setEvaporationFreq(int evaporationFreq) {
 	this.evaporationFreq = evaporationFreq;
     }
 
 
     /**
      * @param tooOld the tooOld to set
      */
     public void setTooOld(int tooOld) {
 	this.tooOld = tooOld;
     }
 
 
     /**
      * @param statsFreq the statsFreq to set
      */
     public void setStatsFreq(int statsFreq) {
 	this.statsFreq = statsFreq;
     }
 
 
     /**
      * @param propa the propa to set
      */
     public void setPropa(AbstractPropagationModel propa) {
 	this.propa = propa;
     }
 
 
     /**
      * @param aco the aco to set
      */
     public void setAco(AbstractACO aco) {
 	this.aco = aco;
     }
 
     /**
      * @return the useScanTimes
      */
     public boolean isUseScanTimes() {
 	return useScanTimes;
     }
 
     /**
      * @return the useLastUAVId
      */
     public boolean isUseLastUAVId() {
 	return useLastUAVId;
     }
 
     /**
      * @param useScanTimes the useScanTimes to set
      */
     public void setUseScanTimes(boolean useScanTimes) {
 	this.useScanTimes = useScanTimes;
     }
 
     /**
      * @param useLastUAVId the useLastUAVId to set
      */
     public void setUseLastUAVId(boolean useLastUAVId) {
 	this.useLastUAVId = useLastUAVId;
     }
 
     /**
      * @return the seed
      */
     public long getSeed() {
 	return seed;
     }
 
     /**
      * @param seed the seed to set
      */
     public void setSeed(long seed) {
 	this.seed = seed;
     }
 
     /**
      * @return the nbTimestepsInTheFuture
      */
     public int getNbTimestepsInTheFuture() {
 	return nbTimestepsInTheFuture;
     }
 
     /**
      * @param nbTimestepsInTheFuture the nbTimestepsInTheFuture to set
      */
     public void setNbTimestepsInTheFuture(int nbTimestepsInTheFuture) {
 	this.nbTimestepsInTheFuture = nbTimestepsInTheFuture;
     }
 
 }
