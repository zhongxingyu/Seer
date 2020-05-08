 package edu.wheaton.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Map;
 
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import com.google.common.collect.ImmutableMap;
 
 import net.sourceforge.jeval.EvaluationException;
 
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.gui.screen.EditEntityScreen;
 import edu.wheaton.simulator.gui.screen.EditFieldScreen;
 import edu.wheaton.simulator.gui.screen.EntityScreen;
 import edu.wheaton.simulator.gui.screen.FieldScreen;
 import edu.wheaton.simulator.gui.screen.NewSimulationScreen;
 import edu.wheaton.simulator.gui.screen.SetupScreen;
 import edu.wheaton.simulator.gui.screen.SpawningScreen;
 import edu.wheaton.simulator.gui.screen.StatDisplayScreen;
 import edu.wheaton.simulator.gui.screen.TitleScreen;
 import edu.wheaton.simulator.gui.screen.ViewSimScreen1;
 import edu.wheaton.simulator.simulation.Simulator;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.Loader;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class SimulatorGuiManager {
 
 	private ScreenManager sm;
 	private SimulationEnder se;
 	private StatisticsManager statMan;
 	private Simulator simulator;
 	private boolean simulationIsRunning;
 	private ArrayList<SpawnCondition> spawnConditions;
	private int stepCount;
	private long startTime;
 	private boolean canSpawn;
 	private GridPanel gridPanel;
 	private Loader loader;
 	private boolean hasStarted;
 	private JFileChooser fc;
 
 	public SimulatorGuiManager(Display d) {
 		spawnConditions = new ArrayList<SpawnCondition>();
		startTime = 0;
 		canSpawn = true;
 		initSim("New Simulation",10, 10);
 		gridPanel = new GridPanel(this);
 		sm = new ScreenManager(d);
 		sm.putScreen("Title", new TitleScreen(this));
 		sm.putScreen("New Simulation", new NewSimulationScreen(this));
 		sm.putScreen("Fields", new FieldScreen(this));
 		sm.putScreen("Edit Fields", new EditFieldScreen(this));
 		sm.putScreen("Entities", new EntityScreen(this));
 		sm.putScreen("Edit Entities", new EditEntityScreen(this));
 		sm.putScreen("Spawning", new SpawningScreen(this));
 		sm.putScreen("View Simulation", new ViewSimScreen1(this));
 		sm.putScreen("Statistics", new StatDisplayScreen(this));
 		sm.putScreen("Grid Setup", new SetupScreen(this));
 
 		sm.getDisplay().setJMenuBar(makeMenuBar());
 		se = new SimulationEnder();
 		loader = new Loader();
 		statMan = StatisticsManager.getInstance();
 
 		hasStarted = false;
 
 		fc = new JFileChooser();
 	}
 
 	public SimulatorGuiManager(){
 		this(new Display());
 	}
 
 	public ScreenManager getScreenManager(){
 		return sm;
 	}
 
 	public GridPanel getGridPanel(){
 		return gridPanel;
 	}
 
 	public void initSim(String name,int x, int y) {
 		System.out.println("Reset prototypes");
 		simulator = new Simulator(name, x, y, se);
 		if(gridPanel != null)
 			gridPanel.setGrid(getSimGrid());
 	}
 
 	private Simulator getSim() {
 		return simulator;
 	}
 
 
 	public Grid getSimGrid(){
 		return getSim().getGrid();
 	}
 
 	public Map<String,String> getSimGridFieldMap(){
 		return getSimGrid().getFieldMap();
 	}
 
 	public Field getSimGlobalField(String name){
 		return getSim().getGlobalField(name);
 	}
 
 	public void addSimGlobalField(String name, String value){
 		getSim().addGlobalField(name, value);
 	}
 
 	public void removeSimGlobalField(String name){
 		getSim().removeGlobalField(name);
 	}
 
 	private SimulationEnder getSimEnder() {
 		return se;
 	}
 
 	public void setSimStepLimit(int maxSteps){
 		getSimEnder().setStepLimit(maxSteps);
 	}
 
 	public int getSimStepLimit(){
 		return getSimEnder().getStepLimit();
 	}
 
 	public void setSimPopLimit(String typeName, int maxPop){
 		getSimEnder().setPopLimit(typeName, maxPop);
 	}
 
 	public ImmutableMap<String, Integer> getSimPopLimits(){
 		return getSimEnder().getPopLimits();
 	}
 
 	public void removeSimPopLimit(String typeName){
 		getSimEnder().removePopLimit(typeName);
 	}
 
 	public StatisticsManager getStatManager(){
 		return statMan;
 	}
 
 	public String getSimName(){
 		return getSim().getName();
 	}
 
 	public void updateGuiManager(String nos, int width, int height){
 		getSim().setName(nos);
 		resizeSimGrid(width, height);
 	}
 
 	public boolean isSimRunning() {
 		return simulationIsRunning;
 	}
 
 	public void setSimRunning(boolean b) {
 		simulationIsRunning = b;
 	}
 
 	public void setSimStarted(boolean b) {
 		hasStarted = b;
 	}
 
 	public boolean hasSimStarted() {
 		return hasStarted;
 	}
 
 	public ArrayList<SpawnCondition> getSimSpawnConditions() { 
 		return spawnConditions; 
 	}
 
 	public int getSimGridHeight(){
 		return getSim().getGrid().getHeight();
 	}
 
 	public void resizeSimGrid(int width,int height){
 		getSim().resizeGrid(width, height);
 	}
 
 	public int getSimGridWidth(){
 		return getSim().getGrid().getWidth();
 	}
 
 	public void setSimLayerExtremes() throws EvaluationException{
 		getSim().setLayerExtremes();
 	}
 
 	public Agent getSimAgent(int x, int y){
 		return getSim().getAgent(x, y);
 	}
 
 	public void removeSimAgent(int x, int y){
 		getSim().removeAgent(x, y);
 	}
 
 	public void initSampleSims(){
 		getSim().initSamples();
 	}
 
 	public void initGameOfLifeSim(){
 		getSim().initGameOfLife();
 	}
 
 	public void initRockPaperScissorsSim(){
 		getSim().initRockPaperScissors();
 	}
 
 	public void setSimLinearUpdate(){
 		getSim().setLinearUpdate();
 	}
 
 	public void setSimAtomicUpdate(){
 		getSim().setAtomicUpdate();
 	}
 
 	public void setSimPriorityUpdate(int a, int b){
 		getSim().setPriorityUpdate(a, b);
 	}
 
 	public String getCurrentSimUpdater(){
 		return getSim().currentUpdater();
 	}
 
 	public void pauseSim(){
 		setSimRunning(false);
 		canSpawn = true;
 		simulator.pause();
 	}
 
 	public boolean canSimSpawn() {
 		return canSpawn;
 	}
 
	public void initSimStartTime(){
		startTime = System.currentTimeMillis();
	}

 	public boolean spiralSpawnSimAgent(String prototypeName, int x, int y){
 		return getSim().spiralSpawn(prototypeName, x, y);
 	}
 
 	public boolean spiralSpawnSimAgent(String string) {
 		return getSim().spiralSpawn(string);
 	}
 
 	public boolean horizontalSpawnSimAgent(String prototypeName, int x) {
 		return getSim().horizontalSpawn(prototypeName, x);
 	}
 
 	public boolean verticalSpawnSimAgent(String name, int y) {
 		return getSim().verticalSpawn(name, y);
 	}
 
 	public void loadSim() {
 		final JFileChooser fc = new JFileChooser();
 
 		int returnVal = fc.showOpenDialog(null);
 		String fileName = "";
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			fileName = fc.getSelectedFile().getName();
 		}
 
 		loader.loadSimulation(fileName);
 	}
 
 	public void startSim(){
 		setSimRunning(true);
 		setSimStarted(true);
 		canSpawn = false;
 		simulator.resume();
 
 	}
 
 
 
 	private JMenuBar makeMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 
 		JMenu fileMenu = makeFileMenu(this);
 		JMenu editMenu = makeEditMenu(sm);
 		JMenu helpMenu = makeHelpMenu(sm);
 
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		menuBar.add(helpMenu);
 		return menuBar;
 	}
 
 	private static JMenu makeFileMenu(final SimulatorGuiManager guiManager) {
 		JMenu menu = Gui.makeMenu("File");
 
 		menu.add(Gui.makeMenuItem("New Simulation", 
 				new GeneralButtonListener("New Simulation",guiManager.sm)));
 		menu.add(Gui.makeMenuItem("Exit",new ActionListener(){ 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				guiManager.setSimRunning(false);
 				System.exit(0);
 			}
 		}));
 
 		return menu;
 	}
 
 	private static JMenu makeEditMenu(final ScreenManager sm) {
 		JMenu menu = Gui.makeMenu("Edit");
 
 
 		menu.add(Gui.makeMenuItem("Edit Global Fields", 
 				new GeneralButtonListener("Fields",sm)));
 
 		return menu;
 	}
 
 	private static JMenu makeHelpMenu(final ScreenManager sm) {
 		JMenu menu = Gui.makeMenu("Help");
 
 		menu.add(Gui.makeMenuItem("About",new ActionListener(){ 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(sm.getDisplay(),
 						"Wheaton College. Software Development 2013.",
 						"About",JOptionPane.PLAIN_MESSAGE);
 			}
 		}));
 		menu.add(Gui.makeMenuItem("Help Contents",new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(sm.getDisplay(),
 						"Wheaton College. Software Development 2013.\n Help Contents",
 						"Help Contents",JOptionPane.PLAIN_MESSAGE);
 			}
 		}));
 
 		return menu;
 	}
 }
