 package edu.wheaton.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
 import edu.wheaton.simulator.gui.screen.NewSimulationScreen;
 import edu.wheaton.simulator.gui.screen.SetupScreen;
 import edu.wheaton.simulator.gui.screen.StatDisplayScreen;
 import edu.wheaton.simulator.gui.screen.TitleScreen;
 import edu.wheaton.simulator.gui.screen.ViewSimScreen;
 import edu.wheaton.simulator.simulation.Simulator;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.Loader;
 import edu.wheaton.simulator.statistics.Saver;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class SimulatorGuiManager {
 
 	private ScreenManager sm;
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
 
 	public SimulatorGuiManager(Display d) {
 		canSpawn = true;
 		gridPanel = new GridPanel(this);
 		load("New Simulation",10, 10);
 		sm = new ScreenManager(d);
 		sm.putScreen("Title", new TitleScreen(this));
 		sm.putScreen("New Simulation", new NewSimulationScreen(this));
 		sm.putScreen("Edit Fields", new EditFieldScreen(this));
 		sm.putScreen("Edit Entities", new EditEntityScreen(this));
 		sm.putScreen("View Simulation", new ViewSimScreen(this));
 		sm.putScreen("Statistics", new StatDisplayScreen(this));
 		sm.putScreen("Grid Setup", new SetupScreen(this));
 
 		sm.getDisplay().setJMenuBar(makeMenuBar());
 		se = new SimulationEnder();
 		loader = new Loader();
 		statMan = StatisticsManager.getInstance();
 
 		hasStarted = false;
 		gpo = new GridPanelObserver(gridPanel);
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
 
 
 
 	private JMenuBar makeMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 
 		JMenu fileMenu = makeFileMenu(this);
 		//JMenu editMenu = makeEditMenu(sm);
 		JMenu helpMenu = makeHelpMenu(sm);
 
 		menuBar.add(fileMenu);
 		//menuBar.add(editMenu);
 		menuBar.add(helpMenu);
 		return menuBar;
 	}
 
 	private JMenu makeFileMenu(final SimulatorGuiManager guiManager) {
 		JMenu menu = Gui.makeMenu("File");
 
 		menu.add(Gui.makeMenuItem("New Simulation", 
 				new GeneralButtonListener("New Simulation",guiManager.sm)));
 
 		menu.add(Gui.makeMenuItem("Save Simulation", 
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String fileName = JOptionPane.showInputDialog("Please enter file name: ");
 				save(fileName);
 			}
 
 		}
 				));
 
 		menu.add(Gui.makeMenuItem("Load Simulation", 
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				load();
 			}
 		}
 				));
 
 		menu.add(Gui.makeMenuItem("Exit",new ActionListener(){ 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				guiManager.setRunning(false);
 				System.exit(0);
 			}
 		}));
 
 		return menu;
 	}
 
 	private JMenu makeEditMenu(final ScreenManager sm) {
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
 
 	public void setName(String name) {
 		getSim().setName(name);
 	}
 
 	public Integer getSleepPeriod() {
 		return getSim().getSleepPeriod();
 	}
 
 	public void setSleepPeriod(int n) {
 		getSim().setSleepPeriod(n);
 	}
 
	public Map<String, String> getSimGridFieldMap() {
 		return getSim().getGlobalFieldMap();
 	}
 }
