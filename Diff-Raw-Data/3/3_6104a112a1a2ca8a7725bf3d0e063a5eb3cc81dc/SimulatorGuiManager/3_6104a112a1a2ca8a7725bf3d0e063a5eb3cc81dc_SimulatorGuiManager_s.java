 package edu.wheaton.simulator.gui;
 
 import java.awt.Color;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JFileChooser;
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.simulation.Simulator;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.Loader;
 import edu.wheaton.simulator.statistics.Saver;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class SimulatorGuiManager {
 
 	private static SimulatorGuiManager gm;
 	
 	private SimulationEnder se;
 	private StatisticsManager statMan;
 	private Simulator simulator;
 	private boolean simulationIsRunning;
 	private boolean canSpawn;
 	private GridPanel gridPanel;
 	private GridPanelObserver gpo;
 	private Loader loader;
 	private Saver saver;
 	private boolean hasStarted;
 	private JFileChooser fc;
 
 	private SimulatorGuiManager() {
 		canSpawn = true;
 		gridPanel = new GridPanel(this);
 		load("New Simulation",10, 10);
 		se = new SimulationEnder();
 		loader = new Loader();
 		statMan = StatisticsManager.getInstance();
 
 		hasStarted = false;
 		gpo = new GridPanelObserver(gridPanel);
 		fc = new JFileChooser();
 	}
 	
 	public static SimulatorGuiManager getInstance(){
 		if(gm==null)
 			gm = new SimulatorGuiManager();
 		return gm;
 	}
 
 	public GridPanel getGridPanel(){
 		return gridPanel;
 	}
 
 	public void load(String name,int x, int y) {
 		simulator = Simulator.getInstance();
 		simulator.load(name, x,y,se);
 		simulator.addGridObserver(gpo);
 
 	}
 
 	private Simulator getSim() {
 		return simulator;
 	}
 
 	public Field getGlobalField(String name){
 		return getSim().getGlobalField(name);
 	}
 
 	public void addGlobalField(String name, String value){
 		getSim().addGlobalField(name, value);
 	}
 
 	public void removeGlobalField(String name){
 		getSim().removeGlobalField(name);
 	}
 
 	private SimulationEnder getEnder() {
 		return se;
 	}
 
 	public void setStepLimit(int maxSteps){
 		getEnder().setStepLimit(maxSteps);
 	}
 
 	public Integer getStepLimit(){
 		return getEnder().getStepLimit();
 	}
 
 	public void setPopLimit(String typeName, int maxPop){
 		getEnder().setPopLimit(typeName, maxPop);
 	}
 
 	public ImmutableMap<String, Integer> getPopLimits(){
 		return getEnder().getPopLimits();
 	}
 
 	public void removePopLimit(String typeName){
 		getEnder().removePopLimit(typeName);
 	}
 
 	public StatisticsManager getStatManager(){
 		return statMan;
 	}
 
 	public String getSimName(){
 		return getSim().getName();
 	}
 
 	public void updateGuiManager(String nos, int width, int height){
 		getSim().setName(nos);
 		resizeGrid(width, height);
 	}
 
 	public boolean isRunning() {
 		return simulationIsRunning;
 	}
 
 	public void setRunning(boolean b) {
 		simulationIsRunning = b;
 	}
 
 	public void setStarted(boolean b) {
 		hasStarted = b;
 	}
 
 	public boolean hasStarted() {
 		return hasStarted;
 	}
 
 	public Integer getGridHeight(){
 		return getSim().getHeight();
 	}
 
 	public void resizeGrid(int width,int height){
 		getSim().resizeGrid(width, height);
 	}
 
 	public Integer getGridWidth(){
 		return getSim().getWidth();
 	}
 
 	public Agent getAgent(int x, int y){
 		return getSim().getAgent(x, y);
 	}
 
 	public Set<String> getPrototypeNames(){
 		return Simulator.prototypeNames();
 	}
 	
 	public void removeAgent(int x, int y){
 		getSim().removeAgent(x, y);
 	}
 
 	public void initSampleAgents(){
 		getSim().initSamples();
 	}
 
 	public void initGameOfLife(){
 		getSim().initGameOfLife();
 	}
 
 	public void initRockPaperScissors(){
 		getSim().initRockPaperScissors();
 	}
 
 	public void setLinearUpdate(){
 		getSim().setLinearUpdate();
 	}
 
 	public void setAtomicUpdate(){
 		getSim().setAtomicUpdate();
 	}
 
 	public void setPriorityUpdate(int a, int b){
 		getSim().setPriorityUpdate(a, b);
 	}
 
 	public String getCurrentUpdater(){
 		return getSim().currentUpdater();
 	}
 
 	public void pause(){
 		setRunning(false);
 		canSpawn = true;
 		simulator.pause();
 	}
 
 	public boolean canSpawn() {
 		return canSpawn;
 	}
 
 	public boolean addAgent(String prototypeName, int x, int y){
 		return getSim().addAgent(prototypeName, x, y);
 	}
 
 	public void load() {
 		int returnVal = fc.showOpenDialog(null);
 		String fileName = "";
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			fileName = fc.getSelectedFile().getName();
 			//TODO make new simulator
 			//initSim(fileName, x, y);
 			//this should eventually be statMan.loadSim(fileName), once that actually gets written
 			//loader.loadSimulation(fileName);
 		}
 
 
 	}
 
 	public void save(String fileName) {
 		//TODO get statistics team to provide a 'saveSim(String fileName)' method
 		//statMan.saveSimulation(fileName);
 	}
 
 	public void start(){
 		setRunning(true);
 		setStarted(true);
 		canSpawn = false;
 		simulator.play();
 	}
 
 	public void setName(String name) {
 		getSim().setName(name);
 	}
 
 	public Integer getSleepPeriod() {
 		return getSim().getSleepPeriod();
 	}
 
 	public void setSleepPeriod(int n) {
 		getSim().setSleepPeriod(n);
 	}
 
 	public Map<String, String> getGlobalFieldMap() {
 		return getSim().getGlobalFieldMap();
 	}
 
 	public Prototype getPrototype(String string) {
		return getSim().getPrototype(string);
 	}
 
 	public void displayLayer(String string, Color color) {
 		getSim().displayLayer(string, color);
 	}
 
 	public void createPrototype(String text, Grid grid, Color color,
 			byte[] generateBytes) {
 		Simulator.createPrototype(text, grid, color, generateBytes);
 	}
 
 	public void createPrototype(String text, Object object, Color color,
 			byte[] generateBytes) {
 		// TODO Auto-generated method stub
 		
 	}
 }
