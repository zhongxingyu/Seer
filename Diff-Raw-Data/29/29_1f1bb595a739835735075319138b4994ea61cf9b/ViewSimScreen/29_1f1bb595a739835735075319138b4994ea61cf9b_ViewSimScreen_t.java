 /**
  * ViewSimScreen
  * 
  * Class representing the screen that displays the grid as
  * the simulation runs.
  * 
  * @author Willy McHie and Ian Walling
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 import edu.wheaton.simulator.statistics.GridObserver;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class ViewSimScreen extends Screen {
 
 	private JPanel gridPanel;
 
 	private int height;
 
 	private int width;
 
 	private ScreenManager sm;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6872689283286800861L;
 
 	private GridPanel grid;
 
 	private int stepCount;
 
 	public ViewSimScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		this.sm = sm;
 		stepCount = 0;
 		JLabel label = new JLabel("View Simulation", SwingConstants.CENTER);
 		JPanel panel = new JPanel();
 		panel.setMaximumSize(new Dimension(500, 50));
 		gridPanel = new JPanel();
 		JButton pauseButton = new JButton("Pause");
 		JButton backButton = new JButton("Back");
 		JButton startButton = new JButton("Start/Resume");
 
 		backButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				}
 				);
 		pauseButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.setRunning(false);
 					}
 				}
 				);
 
 		startButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						ArrayList<SpawnCondition> conditions = sm.getSpawnConditions();
 						for (SpawnCondition condition: conditions) {
 							condition.addToGrid(sm.getFacade());
 						}
 						sm.setRunning(true);
 						sm.setStarted(true);
 						runSim();
 					}
 				}
 				);
 
 		grid = new GridPanel(sm);
 		panel.add(startButton);
 		panel.add(pauseButton);
 		panel.add(backButton);
 		this.add(label, BorderLayout.NORTH);
 		this.add(panel, BorderLayout.SOUTH);
 		this.add(grid, BorderLayout.CENTER);
 		this.setVisible(true);	
 
 	}
 	private void runSim() {
 		System.out.println(sm.getEnder().getStepLimit());
 		//program loop yay!
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				GridObserver gridObs = new GridObserver(new StatisticsManager());
 				while(sm.isRunning()) {
 					try {
 						sm.getFacade().updateEntities();
 					} catch (SimulationPauseException e) {
 						sm.setRunning(false);
 						JOptionPane.showMessageDialog(null, e.getMessage());
 					}
 					gridObs.recordSimulationStep(sm.getFacade().getGrid(), stepCount, Prototype.getPrototypes());
 					sm.setRunning(!(sm.getEnder().evaluate(stepCount, 
 							sm.getFacade().getGrid())));
 
 					SwingUtilities.invokeLater(
 							new Thread (new Runnable() {
 								@Override
 								public void run() {
 									grid.repaint();
 //									grid.clearAgents(grid.getGraphics());
 //									grid.agentPaint(grid.getGraphics());
 								}
 							}));
 					stepCount++;
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
 	private void paint(){
 		grid.paint(grid.getGraphics());
 		grid.agentPaint(grid.getGraphics());
 	}
 
 	@Override
 	public void load() {
 		paint();
 	}
 }
