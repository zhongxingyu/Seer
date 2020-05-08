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
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 import edu.wheaton.simulator.statistics.GridRecorder;
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
 		JPanel layerPanel = new JPanel();
 		layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.Y_AXIS));
//		JLabel agents = new JLabel("Agents", SwingConstants.CENTER);
//		JComboBox agentComboBox = new JComboBox();
//		JLabel layers = new JLabel("Layers", SwingConstants.CENTER);
//		JComboBox layerComboBox = new JComboBox();
 		
 		
 		//TODO add layer elements
 		//set Layout
 		//objects for layers:
 		// - combobox(es) for choosing field, colorchooser to pick primary filter color, 
 		//   labels for these, "apply" button, "clear" button
 		
		gridPanel = new JPanel();
 		grid = new GridPanel(sm);
		
 		this.add(label, BorderLayout.NORTH);
		this.add(makeButtonPanel(), BorderLayout.SOUTH);
 		this.add(grid, BorderLayout.CENTER);
 		this.setVisible(true);	
 	}
 	
	private JPanel makeButtonPanel(){
		JPanel buttonPanel = new JPanel();
		buttonPanel.setMaximumSize(new Dimension(500, 50));
		buttonPanel.add(makeStartButton());
		buttonPanel.add(makePauseButton());
		buttonPanel.add(makeBackButton());
		return buttonPanel;
	}
	
 	private JButton makeBackButton(){
 		JButton b = new JButton("Back");
 		b.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				}
 				);
 		return b;
 	}
 
 	private JButton makePauseButton(){
 		JButton b = new JButton("Pause");
 		b.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.setRunning(false);
 					}
 				}
 				);
 		return b;
 	}
 
 	private JButton makeStartButton(){
 		JButton b = new JButton("Start/Resume");
 		b.addActionListener(
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
 		return b;
 	}
 	
 	private void runSim() {
 		System.out.println(sm.getEnder().getStepLimit());
 		//program loop yay!
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				GridRecorder gridObs = new GridRecorder(new StatisticsManager());
 				while(sm.isRunning()) {
 					try {
 						sm.getFacade().updateEntities();
 					} catch (SimulationPauseException e) {
 						sm.setRunning(false);
 						JOptionPane.showMessageDialog(null, e.getMessage());
 					}
 					gridObs.recordSimulationStep(sm.getFacade().getGrid(), stepCount, Prototype.getPrototypes());
 					stepCount++;
 					sm.setRunning(!(sm.getEnder().evaluate(stepCount, 
 							sm.getFacade().getGrid())));
 
 					SwingUtilities.invokeLater(
 							new Thread (new Runnable() {
 								@Override
 								public void run() {
 									grid.repaint();
 									grid.agentPaint(grid.getGraphics());
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
 
 	@Override
 	public void load() {
 		grid.repaint();
 	}
 }
