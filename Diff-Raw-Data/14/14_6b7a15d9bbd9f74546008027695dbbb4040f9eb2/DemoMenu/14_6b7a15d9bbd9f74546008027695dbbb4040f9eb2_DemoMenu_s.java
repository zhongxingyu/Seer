 package edu.wheaton.simulator.demo;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.simulation.GUIToAgentFacade;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 import edu.wheaton.simulator.statistics.GridObserver;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class DemoMenu {
 
 	private JFrame frame;
 	private JPanel startScreen;
 	private JPanel simulationScreen;
 	private JPanel statsScreen;
 	private JTextField nameField;
 	private JColorChooser colorTool;
 	private DemoGridPanel grid;
 	private GUIToAgentFacade facade;
 	private StatisticsManager statsManager;
 	private GridObserver observer;
 	private boolean isRunning;
 	private int turnCount;
 	private HashSet<Prototype> prototypes;
 	private JButton finishButton;
 
 	public DemoMenu() {
 		//initialize instance variables
 		facade = new GUIToAgentFacade(10, 10);
 		statsManager = new StatisticsManager();
 		observer = new GridObserver(statsManager);
 		isRunning = false;
 		turnCount = 0;
 		prototypes = new HashSet<Prototype>();
 		frame = new JFrame("Simulator");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(1000, 800);
 		frame.setLocation(300, 200);
 
 		//create panel for opening screen
 		startScreen = new JPanel();
 		startScreen.setLayout(new BorderLayout());
 		JLabel startLabel = new JLabel("Starting Information");
 		startLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		startLabel.setHorizontalAlignment(JLabel.CENTER);
 		startScreen.add(startLabel, BorderLayout.NORTH);
 		JPanel startMainPanel = new JPanel();
 		startMainPanel.setLayout(new BoxLayout(startMainPanel, BoxLayout.Y_AXIS));
 		JPanel startNamePanel = new JPanel();
 		JLabel startNameLabel = new JLabel("Agent name");
 		nameField = new JTextField(25);
 		startNamePanel.add(startNameLabel);
 		startNamePanel.add(nameField);
 		startMainPanel.add(startNamePanel);
 		colorTool = new JColorChooser();
 		startMainPanel.add(colorTool);
 		startScreen.add(startMainPanel, BorderLayout.CENTER);
 		JButton startNextButton = new JButton("Next");
 		//addActionListener to move to next screen, create the agent, add it to the grid
 		startNextButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						facade.createPrototype(nameField.getText(), facade.getGrid(), colorTool.getColor());
 						//add behavior to that prototype
 						facade.getPrototype(nameField.getText()).addTrigger(
 								new Trigger("move right", 
 										1, 
 										new Expression("1==1"), 
 										new Expression("move('this', this.x + 1, this.y)"))
 								);
 						prototypes.add(facade.getPrototype(nameField.getText()));
 						facade.spawnAgent(nameField.getText(), 0, 4);
 						frame.setContentPane(simulationScreen);
 						frame.setVisible(true);
 						grid.clearAgents(grid.getGraphics());
 						grid.agentPaint(grid.getGraphics());
 						//TODO try and get it to paint immediately, before start button is called. 
 						//is it automatically repainting after these methods are called and overwriting them?
 					}
 				}
 				);
 		startScreen.add(startNextButton, BorderLayout.SOUTH);
 
 		//create panel for simulation screen
 		simulationScreen = new JPanel();
 		simulationScreen.setLayout(new BorderLayout());
 		JLabel simLabel = new JLabel("View Simulation");
 		simLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		simulationScreen.add(simLabel, BorderLayout.NORTH);
 		grid = new DemoGridPanel(facade);
 		simulationScreen.add(grid, BorderLayout.CENTER);
 		JPanel simButtonPanel = new JPanel();
 		JButton startButton = new JButton("Start");
 		startButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						isRunning = true;
 						runSim();
 					}
 				}
 				);
 		JButton pauseButton = new JButton("Pause");
 		pauseButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						isRunning = false;
 					}
 				}
 				);
 		finishButton = new JButton("Finish");
 		//TODO addActionListener to move to next screen; should be active only when simulation ends
 		finishButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						frame.setContentPane(statsScreen);
 						frame.setVisible(true);
 						//loadStats(); //??
 					}
 				}
 				);
 		finishButton.setEnabled(false);
 		simButtonPanel.add(pauseButton);
 		simButtonPanel.add(startButton);
 		simButtonPanel.add(finishButton);
 		simulationScreen.add(simButtonPanel, BorderLayout.SOUTH);
 
 		//create panel for stats screen
 		statsScreen = new JPanel();
 		statsScreen.setLayout(new BorderLayout());
 		JLabel statsLabel = new JLabel("Statistics");
 		statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		statsScreen.add(statsLabel, BorderLayout.NORTH);
 		JPanel statsMainPanel = new JPanel();
 		statsMainPanel.setLayout(new BoxLayout(statsMainPanel, BoxLayout.Y_AXIS));
 		//TODO label + display for # of moves, label + display for # of seconds (was there something else?)
 		JButton quitButton = new JButton("Quit");
 		quitButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						//close simulation
 						frame.setVisible(false);
 						frame.dispose();
 					}
 				}
 				);
 		statsScreen.add(quitButton);
 
 		frame.setContentPane(startScreen);
 		frame.setVisible(true);
 	}
 
 	private void runSim() {
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while(isRunning) {
 					try {
 						facade.updateEntities();
 					} catch (SimulationPauseException e) {
 						isRunning = false;
 						JOptionPane.showMessageDialog(null, e.getMessage());
 					}
 
 					SwingUtilities.invokeLater(
 							new Thread (new Runnable() {
 								@Override
 								public void run() {
 //									grid.clearAgents(grid.getGraphics());
 //									grid.agentPaint(grid.getGraphics());
 									grid.repaint();
 								}
 							}));
 					turnCount++;
 					observer.recordSimulationStep(facade.getGrid(), turnCount, prototypes);
 					System.out.println(turnCount);
 					if (turnCount >= 9) {
 						isRunning = false;
 						finishButton.setEnabled(true);
 					}
 					try {
 						System.out.println("Sleep!");
 						Thread.sleep(500);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 
 	}
 }
