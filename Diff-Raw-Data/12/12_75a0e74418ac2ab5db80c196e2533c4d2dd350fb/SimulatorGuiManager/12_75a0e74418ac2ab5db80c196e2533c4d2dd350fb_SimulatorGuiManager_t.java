 package edu.wheaton.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import edu.wheaton.simulator.gui.screen.EditEntityScreen;
 import edu.wheaton.simulator.gui.screen.EditFieldScreen;
 import edu.wheaton.simulator.gui.screen.EntityScreen;
 import edu.wheaton.simulator.gui.screen.FieldScreen;
 import edu.wheaton.simulator.gui.screen.NewSimulationScreen;
 import edu.wheaton.simulator.gui.screen.SetupScreen;
 import edu.wheaton.simulator.gui.screen.SpawningScreen;
 import edu.wheaton.simulator.gui.screen.StatDisplayScreen;
 import edu.wheaton.simulator.gui.screen.TitleScreen;
 import edu.wheaton.simulator.gui.screen.ViewSimScreen;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 import edu.wheaton.simulator.simulation.Simulator;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class SimulatorGuiManager {
 	
 	private ScreenManager sm;
 	
 	private SimulationEnder se;
 	
 	private StatisticsManager statMan;
 	
 	private Simulator simulator;
 	
 	private boolean simulationIsRunning;
 	
 	private ArrayList<SpawnCondition> spawnConditions;
 	
 	public int stepCount;
 	public long startTime;
 	public boolean canSpawn;
 	
 	//for determining when components should be disabled while running a sim.
 	private boolean hasStarted;
 	
 	private GridPanel gridPanel;
 
 	public SimulatorGuiManager(Display d) {
 		spawnConditions = new ArrayList<SpawnCondition>();
 		startTime = 0;
 		canSpawn = true;
 		setSim("New Simulation",10, 10);
 		sm = new ScreenManager(d);
 		sm.putScreen("Title", new TitleScreen(this));
 		sm.putScreen("New Simulation", new NewSimulationScreen(this));
 		sm.putScreen("Fields", new FieldScreen(this));
 		sm.putScreen("Edit Fields", new EditFieldScreen(this));
 		sm.putScreen("Entities", new EntityScreen(this));
 		sm.putScreen("Edit Entities", new EditEntityScreen(this));
 		sm.putScreen("Spawning", new SpawningScreen(this));
 		sm.putScreen("View Simulation", new ViewSimScreen(this));
 		sm.putScreen("Statistics", new StatDisplayScreen(this));
 		sm.putScreen("Grid Setup", new SetupScreen(this));
 		
 		sm.getDisplay().setJMenuBar(makeMenuBar());
 		se = new SimulationEnder();
 		statMan = StatisticsManager.getInstance();
 	}
 
 	public SimulatorGuiManager(){
 		this(new Display());
 	}
 	
 	public ScreenManager getScreenManager(){
 		return sm;
 	}
 	 
 	public void setSim(String name,int x, int y) {
 		simulator = new Simulator(name,x, y);
 	}
 	
 	 
 	public Simulator getSim() {
 		return simulator;
 	}
 	
 	 
 	public SimulationEnder getEnder() {
 		return se;
 	}
 	
 	public StatisticsManager getStatManager(){
 		return statMan;
 	}
 	
 	public String getSimName(){
 		return getSim().getName();
 	}
 	 
 	public void updateGUIManager(String nos, int width, int height){
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
 
 	public ArrayList<SpawnCondition> getSpawnConditions() { 
 		return spawnConditions; 
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
 				guiManager.setRunning(false);
 				System.exit(0);
 			}
 		}));
 		
 		return menu;
 	}
 	
 	private static JMenu makeEditMenu(final ScreenManager sm) {
 		JMenu menu = Gui.makeMenu("Edit");
 		
 		menu.add(Gui.makeMenuItem("Edit Entities", 
 				new GeneralButtonListener("Entities",sm)));
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
 	
 	public int getGridHeight(){
 		return getSim().getGrid().getHeight();
 	}
 	
 	public void resizeGrid(int width,int height){
 		getSim().resizeGrid(width, height);
 	}
 	
 	public int getGridWidth(){
 		return getSim().getGrid().getWidth();
 	}
 	
 	public void pauseSim(){
 		setRunning(false);
 		canSpawn = true;
 	}
 	
 	public void startSim(){
 		setRunning(true);
 		setStarted(true);
 		canSpawn = false;
 		System.out.println("StepLimit = " + getEnder().getStepLimit());
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while(isRunning()) {
 					Simulator sim = getSim();
 					try {
 						sim.updateEntities();
 					} catch (SimulationPauseException e) {
 						setRunning(false);
 						JOptionPane.showMessageDialog(null, e.getMessage());
 						break;
 					}
 					long currentTime = System.currentTimeMillis();
 					//gridRec.recordSimulationStep(gm.getFacade().getGrid(), stepCount, Prototype.getPrototypes());
 					//gridRec.updateTime(currentTime, currentTime - startTime);
 					startTime = currentTime;
 					stepCount++;
 					boolean shouldEnd = getEnder().evaluate(stepCount, 
 							sim.getGrid());
 					System.out.println("shouldEnd = " + shouldEnd);
 					if (shouldEnd) {
 						setRunning(false);
 					}
 
 					SwingUtilities.invokeLater(
 						new Thread (new Runnable() {
 							@Override
 							public void run() {
 								gridPanel.repaint();
 							}
 						}));
 
 					System.out.println(stepCount);
 					try {
 						System.out.println("Sleep!");
 						Thread.sleep(500);
 					} catch (InterruptedException e) {
 						System.err.println("ViewSimScreen.java: 'Thread.sleep(500)' was interrupted");
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 	}
 }
