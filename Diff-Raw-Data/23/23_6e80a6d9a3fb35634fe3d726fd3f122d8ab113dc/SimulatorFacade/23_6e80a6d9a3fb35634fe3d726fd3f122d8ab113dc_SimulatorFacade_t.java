 package edu.wheaton.simulator.gui;
 
 import java.awt.Color;
 import java.io.File;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JFileChooser;
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.simulation.Simulator;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class SimulatorFacade {
 
 	public static SimulatorFacade getInstance(){
 		if(gm==null)
 			gm = new SimulatorFacade();
 		return gm;
 	}
 
 	public static Expression makeExpression(String str){
 		return new Expression(str);
 	}
 	
 	public GridPanel getGridPanel(){
 		return gridPanel;
 	}
 
 	public void load(String name,int x, int y) {
 		simulator = Simulator.getInstance();
 		simulator.load(name, x,y,se);
 		simulator.addGridObserver(gpo);
 
 	}
 
 	public Field getGlobalField(String name){
 		return simulator.getGlobalField(name);
 	}
 
 	public void addGlobalField(String name, String value){
 		simulator.addGlobalField(name, value);
 	}
 
 	public void removeGlobalField(String name){
 		simulator.removeGlobalField(name);
 	}
 
 	public void setStepLimit(int maxSteps){
 		se.setStepLimit(maxSteps);
 	}
 
 	public Integer getStepLimit(){
 		return se.getStepLimit();
 	}
 
 	public void setPopLimit(String typeName, int maxPop){
 		se.setPopLimit(typeName, maxPop);
 	}
 
 	public ImmutableMap<String, Integer> getPopLimits(){
 		return se.getPopLimits();
 	}
 
 	public void removePopLimit(String typeName){
 		se.removePopLimit(typeName);
 	}
 
 	public String getSimName(){
 		return simulator.getName();
 	}
 
 	public void updateGuiManager(String nos, int width, int height){
 		simulator.setName(nos);
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
 		return simulator.getHeight();
 	}
 
 	public void resizeGrid(int width,int height){
 		simulator.resizeGrid(width, height);
 	}
 
 	public Integer getGridWidth(){
 		return simulator.getWidth();
 	}
 
 	public Agent getAgent(int x, int y){
 		return simulator.getAgent(x, y);
 	}
 
 	public Set<String> getPrototypeNames(){
 		return Simulator.prototypeNames();
 	}
 
 	public void removeAgent(int x, int y){
 		simulator.removeAgent(x, y);
 	}
 
 	public void initSampleAgents(){
 		simulator.initSamples();
 	}
 
 	public void initGameOfLife(){
 		simulator.initGameOfLife();
 	}
 
 	public void initRockPaperScissors(){
 		simulator.initRockPaperScissors();
 	}
 
 	public void setLinearUpdate(){
 		simulator.setLinearUpdate();
 	}
 
 	public void setAtomicUpdate(){
 		simulator.setAtomicUpdate();
 	}
 
 	public void setPriorityUpdate(int a, int b){
 		simulator.setPriorityUpdate(a, b);
 	}
 
 	public String getCurrentUpdater(){
 		return simulator.currentUpdater();
 	}
 
 	public void pause(){
 		setRunning(false);
 		simulator.pause();
 	}
 
 	public void addAgent(String prototypeName, int x, int y){
 		simulator.addAgent(prototypeName, x, y);
 	}
 
 	public void addAgentRandom(String prototypeName){
 		simulator.addAgent(prototypeName);
 	}
 
 	public void clearGrid() {
 		simulator.clearAll();
 	}
 
 	public void fillGrid(String prototypeName) {
 		simulator.fillAll(prototypeName);
 	}
 
 	public void save() {
 		//how to deal with possibility of simulation name being different from selected file name?
 		int returnVal = fc.showSaveDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			simulator.saveToFile(fc.getSelectedFile(), se);
 		}
 		
 	}
 
 	public void load() {
 		int returnVal = fc.showOpenDialog(null);
 		File file = null;
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = fc.getSelectedFile();
 			simulator.loadFromFile(file);
 			
 		}
 	}
 
 	public void saveAgent(String agentName) {
 		//how to deal with possibility of agent name being different from selected file name?
 		int returnVal = fc.showSaveDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			simulator.savePrototypeToFile(Prototype.getPrototype(agentName));
 		}
 	}
 
 	public void importAgent() {
 		//TODO need different fc because different directories/file extensions?
 		int returnVal = fc.showOpenDialog(null);
 		File file = null;
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = fc.getSelectedFile();
 			simulator.loadPrototypeFromFile(file);
 		}
 
 	}
 
 	public void start(){
 		setRunning(true);
 		setStarted(true);
 		simulator.play();
 	}
 
 	public void setName(String name) {
 		simulator.setName(name);
 	}
 
 	public Integer getSleepPeriod() {
 		return simulator.getSleepPeriod();
 	}
 
 	public void setSleepPeriod(int n) {
 		simulator.setSleepPeriod(n);
 	}
 
 	public Map<String, String> getGlobalFieldMap() {
 		return simulator.getGlobalFieldMap();
 	}
 
 	public Prototype getPrototype(String string) {
 		return Simulator.getPrototype(string);
 	}
 
	public void displayLayer(String fieldName, Color color) {
		simulator.displayLayer(fieldName, color);
 	}
 
 	public void clearLayer(){
 		simulator.clearLayer();
 	}
 	
 	public void createPrototype(String text,Color color,
 			byte[] generateBytes) {
 		Simulator.createPrototype(text, color, generateBytes);
 	}
 
 	public int[] getPopVsTime(String selectedItem) {
 		return statMan.getPopVsTime(selectedItem);
 	}
 
 	public double[] getAvgFieldValue(String protName, String fieldName) {
 		return statMan.getAvgFieldValue(protName, fieldName);
 	}
 
 	public double getAvgLifespan(String protName) {
 		return statMan.getAvgLifespan(protName);
 	}
 	
 	private static SimulatorFacade gm;
 	private SimulationEnder se;
 	private StatisticsManager statMan;
 	private Simulator simulator;
 	private boolean simulationIsRunning;
 	private GridPanel gridPanel;
 	private GridPanelObserver gpo;
 	private boolean hasStarted;
 	private JFileChooser fc;
 
 	private SimulatorFacade() {
 		gridPanel = new GridPanel(this);
 		load("New Simulation",10, 10);
 		se = new SimulationEnder();
 		statMan = StatisticsManager.getInstance();
 
 		hasStarted = false;
 		gpo = new GridPanelObserver(gridPanel);
 		simulator.addGridObserver(gpo);
 		fc = new JFileChooser();
 	}
 }
