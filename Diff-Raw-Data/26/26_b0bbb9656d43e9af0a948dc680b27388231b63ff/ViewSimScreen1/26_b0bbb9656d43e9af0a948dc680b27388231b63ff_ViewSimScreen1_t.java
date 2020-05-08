 /**
  * ViewSimScreen
  * 
  * Class representing the screen that displays the grid as
  * the simulation runs.
  * 
  * @author Willy McHie and Ian Walling
  * Wheaton College, CSCI 335, Spring 2013
  */
 package edu.wheaton.simulator.gui.screen;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.sourceforge.jeval.EvaluationException;
 
 import edu.wheaton.simulator.gui.BoxLayoutAxis;
 import edu.wheaton.simulator.gui.GeneralButtonListener;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.MaxSize;
 import edu.wheaton.simulator.gui.PrefSize;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorGuiManager;
 import edu.wheaton.simulator.gui.SpawnCondition;
 import edu.wheaton.simulator.simulation.Simulator;
 
 public class ViewSimScreen1 extends Screen {
 
 	private static final long serialVersionUID = -6872689283286800861L;
 
 	private JComboBox agentComboBox;
 
 	private JComboBox layerComboBox;
 
 	private String[] entities;
 
 	private JPanel layerPanelAgents;
 
 	private JPanel layerPanelLayers;
 
 	private GridBagConstraints c;
 	
 	private final Screen entitiesScreen;
 
 	public ViewSimScreen1(final SimulatorGuiManager gm) {
 		super(gm);
 		entities = new String[0];
 		this.setLayout(new GridBagLayout());
 		JLabel label = new JLabel("View Simulation", SwingConstants.CENTER);
 
 		JLabel agents = new JLabel("Agents", SwingConstants.CENTER);
 		agentComboBox = Gui.makeComboBox(null,new MaxSize(200,50));
 
 		JLabel layers = new JLabel("Layers", SwingConstants.CENTER);
 		layerComboBox = Gui.makeComboBox(null, new MaxSize(200,50));
 
 		final JColorChooser colorTool = Gui.makeColorChooser();
 
 		JButton apply = Gui.makeButton("Apply", PrefSize.NULL,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				Simulator.newLayer(layerComboBox.getSelectedItem().toString(), colorTool.getColor());
 				try {
 					gm.setSimLayerExtremes();
 				} catch (EvaluationException e) {
 					e.printStackTrace();
 				}
 				gm.getGridPanel().setLayers(true);
 				gm.getGridPanel().repaint();
 			} 
 		}
 				);
 
 		JButton clear = Gui.makeButton("Clear", PrefSize.NULL, 
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				gm.getGridPanel().setLayers(true);
 				gm.getGridPanel().repaint();
 			} 
 		}
 				);
 
 		layerPanelAgents = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null);
 		layerPanelAgents.add(agents);
 		layerPanelAgents.add(agentComboBox);
 
 		layerPanelLayers = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null);
 		layerPanelLayers.add(layers);
 		layerPanelLayers.add(layerComboBox);
 
 		JPanel layerPanelButtons = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null);
 		layerPanelButtons.add(apply);
 		layerPanelButtons.add(clear);
 
 		JPanel colorPanel = Gui.makeColorChooserPanel(colorTool);
 
 		JPanel upperLayerPanel = new JPanel();
 		upperLayerPanel.setLayout(new GridBagLayout());
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy = 0;
 		upperLayerPanel.add(layerPanelAgents, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy = 1;
 		upperLayerPanel.add(layerPanelLayers, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy = 2;
 		upperLayerPanel.add(layerPanelButtons, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy = 3;
 		upperLayerPanel.add(colorPanel, c);
 		upperLayerPanel.setAlignmentX(LEFT_ALIGNMENT);
 
 
 
 		JTabbedPane tabs = new JTabbedPane();
 		entitiesScreen = gm.getScreenManager().getScreen("Entities");
 		tabs.add("Agent", entitiesScreen);
 		tabs.addChangeListener(new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				entitiesScreen.load();
 			}
 			
 		});
 		tabs.add("Layers", upperLayerPanel);
 
 		gm.getGridPanel().addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent me) {
 				if(getGuiManager().canSimSpawn()){
 					int standardSize = Math.min(
 							gm.getGridPanel().getWidth()/gm.getSimGridWidth(),
 							gm.getGridPanel().getHeight()/gm.getSimGridHeight()
 					);
 					
 					int x = me.getX()/standardSize;
 					int y = me.getY()/standardSize;
 					
 					if(gm.getSimAgent(x,y) == null)
 						gm.spiralSpawnSimAgent(agentComboBox.getSelectedItem().toString(), x, y);
 					else
 						gm.removeSimAgent(x,y);
 					gm.getGridPanel().repaint();
 				}
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {}
 		});
 
 		JPanel layerPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS,null,null);
 		layerPanel.setAlignmentY(CENTER_ALIGNMENT);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.PAGE_START;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		this.add(label, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 2;
 		c.weighty = 1;
 		c.weightx = .5;
 		c.insets = new Insets(1,1,1,1);
 		this.add(tabs, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.ipadx = 600;
 		c.ipady = 600;
 		c.gridwidth = 2;
 		c.weighty = 1;
 		c.weightx = .7;
 		c.insets = new Insets(1,1,1,1);
 		this.add(gm.getGridPanel(), c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		this.add(makeButtonPanel(), c);
 
 		this.setVisible(true);	
 	}
 
 	private JPanel makeButtonPanel(){
 		//TODO most of these will become tabs, adding temporarily for navigation purposes
 		ScreenManager sm = getScreenManager();
 		JPanel buttonPanel = Gui.makePanel((LayoutManager)null,new MaxSize(500,50),PrefSize.NULL,
 			makeStartButton(),
 			Gui.makeButton("Pause",null,new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					getGuiManager().pauseSim();
 				}
 			}),
 			//Gui.makeButton("Entities",null, new GeneralButtonListener("Entities",sm)),
 			Gui.makeButton("Global Fields",null, new GeneralButtonListener("Fields",sm)),
 			Gui.makeButton("Setup options",null, new GeneralButtonListener("Grid Setup",sm)),
 			Gui.makeButton("Statistics",null, new GeneralButtonListener("Statistics",sm)));
 		return buttonPanel;
 	}
 
 	private JButton makeStartButton(){
 		JButton b = Gui.makeButton("Start/Resume", null, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				SimulatorGuiManager gm = getGuiManager();
 				if (!gm.hasSimStarted()) {
 					for (SpawnCondition condition: gm.getSimSpawnConditions()) {
 						condition.addToGrid(gm);
 					}
 				}
 				gm.getGridPanel().repaint();
 				gm.initSimStartTime();
 				//grid.repaint();
 				//gm.setSimRunning(true);
 				//gm.setSimStarted(true);
 				//canSpawn = false;
 				gm.startSim();
 			}
 		});
 		return b;
 	}
 /*
 	private void runSim() {
 		System.out.println("StepLimit = " + gm.getSimStepLimit());
 		//program loop yay!
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while(gm.isSimRunning()) {
 					try {
 						gm.updateEntities();
 					} catch (SimulationPauseException e) {
 						gm.setSimRunning(false);
 						JOptionPane.showMessageDialog(null, e.getMessage());
 						break;
 					}
 					boolean shouldEnd = gm.getSimEnder().evaluate(stepCount, 
 							gm.getSimGrid());
 					System.out.println("shouldEnd = " + shouldEnd);
 					if (shouldEnd) {
 						gm.setSimRunning(false);
 					}
 
 					SwingUtilities.invokeLater(
 							new Thread (new Runnable() {
 								@Override
 								public void run() {
 									grid.repaint();
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
 */
 	@Override
 	public void load() {
 		entitiesScreen.load();
		//TODO when New Simulation is pressed on the menu bar, we need to reset the number of 
		//prototypes 
 		entities = Simulator.prototypeNames().toArray(entities);
 		agentComboBox = new JComboBox(entities);
 		agentComboBox.addItemListener(
 				new ItemListener() {
 					@Override
 					public void itemStateChanged(ItemEvent e) {
 						layerComboBox = new JComboBox(Simulator.getPrototype
 								(agentComboBox.getSelectedItem().toString())
 								.getCustomFieldMap().keySet().toArray());
 						layerComboBox.setMaximumSize(new Dimension(200, 50));
 						layerPanelLayers.remove(1);
 						layerPanelLayers.add(layerComboBox);
 						validate();
 						repaint();
 					}
 				}
 				);
		System.out.println(entities.length);
 		if(entities.length != 0){
 			layerComboBox = new JComboBox(Simulator.getPrototype
 					(agentComboBox.getItemAt(0).toString())
 					.getCustomFieldMap().keySet().toArray());
 			layerComboBox.setMaximumSize(new Dimension(200, 50));
 			layerPanelLayers.remove(1);
 			layerPanelLayers.add(layerComboBox);
 		}
 		agentComboBox.setMaximumSize(new Dimension(200, 50));
 		layerPanelAgents.remove(1);
 		layerPanelAgents.add(agentComboBox);
 		validate();
 		getGuiManager().getGridPanel().repaint();
 
 	}
 }
