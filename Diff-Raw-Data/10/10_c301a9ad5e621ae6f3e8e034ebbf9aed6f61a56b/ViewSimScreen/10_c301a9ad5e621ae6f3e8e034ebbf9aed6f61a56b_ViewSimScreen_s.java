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
 import edu.wheaton.simulator.simulation.Simulator;
 
 public class ViewSimScreen extends Screen {
 
 	private static final long serialVersionUID = -6872689283286800861L;
 
 	private JComboBox agentComboBox;
 
 	private JComboBox layerComboBox;
 
 	private String[] entities;
 
 	private JPanel layerPanelAgents;
 
 	private JPanel layerPanelLayers;
 
 	private GridBagConstraints c;
 
 	private boolean canSpawn;
 
 	private final EntityScreen entitiesScreen;
 
 	private final Screen globalFieldScreen;
 
 	private final Screen optionsScreen;
 	
 	public ViewSimScreen(final SimulatorGuiManager gm) {
 		super(gm);
 		entities = new String[0];
 		setSpawn(false);
 		this.setLayout(new GridBagLayout());
 		((GridBagLayout)this.getLayout()).columnWeights = new double[]{0, 1};
 
 		JLabel agents = new JLabel("Agents", SwingConstants.CENTER);
 		agentComboBox = Gui.makeComboBox(null, new MaxSize(200, 50));
 
 		JLabel layers = new JLabel("Fields", SwingConstants.CENTER);
 		layerComboBox = Gui.makeComboBox(null, new MaxSize(200, 50));
 
 		final JColorChooser colorTool = Gui.makeColorChooser();
 
 		JButton apply = Gui.makeButton("Apply", PrefSize.NULL,
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						Simulator.getInstance().displayLayer(layerComboBox.getSelectedItem().toString(), colorTool.getColor());
 						gm.getGridPanel().repaint();
 					}
 				});
 
 		JButton clear = Gui.makeButton("Clear", PrefSize.NULL,
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
						gm.getGridPanel().setLayers(true);
						gm.getGridPanel().repaint();
 					}
 				});
 
 		layerPanelAgents = Gui.makePanel(BoxLayoutAxis.LINE_AXIS, null, null);
 		layerPanelAgents.add(agents);
 		layerPanelAgents.add(agentComboBox);
 
 		layerPanelLayers = Gui.makePanel(BoxLayoutAxis.LINE_AXIS, null, null);
 		layerPanelLayers.add(layers);
 		layerPanelLayers.add(layerComboBox);
 
 		JPanel layerPanelButtons = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,
 				null, null);
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
 
 		final JTabbedPane tabs = new JTabbedPane();
 		tabs.setMaximumSize(new Dimension(550, 550));
 		entitiesScreen = new EntityScreen(gm);
 		globalFieldScreen = new FieldScreen(gm);
 		optionsScreen = new SetupScreen(gm);
 		tabs.addTab("Agent", entitiesScreen);
 		tabs.addTab("Layers", upperLayerPanel);
 		tabs.addTab("Global Fields", globalFieldScreen);
 		tabs.addTab("Options", optionsScreen);
 		tabs.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				if (tabs.getSelectedComponent().toString() == "Agent")
 					canSpawn = true;
 				else {
 					canSpawn = false;
 				}
 				entitiesScreen.load();
 				globalFieldScreen.load();
 				optionsScreen.load();
 			}
 		});
 
 		gm.getGridPanel().addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent me) {
 				if (getGuiManager().canSimSpawn()) {
 					int standardSize = Math.min(gm.getGridPanel().getWidth()
 							/ gm.getSimGridWidth(), gm.getGridPanel()
 							.getHeight() / gm.getSimGridHeight());
 
 					int x = me.getX() / standardSize;
 					int y = me.getY() / standardSize;
 					if (canSpawn) {
 						if (gm.getSimAgent(x, y) == null) {
 							gm.addAgent(entitiesScreen.getList()
 									.getSelectedValue().toString(), x, y);
 						} else {
 							gm.removeSimAgent(x, y);
 						}
 					}
 					gm.getGridPanel().repaint();
 				}
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 			}
 		});
 
 		JPanel layerPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS, null, null);
 		layerPanel.setAlignmentY(CENTER_ALIGNMENT);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.NONE;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridheight = 2;
 		c.weightx = 0;
 		c.insets = new Insets(5, 5, 5, 5);
 		this.add(tabs, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.ipadx = 600;
 		c.ipady = 600;
 		c.gridwidth = 2;
 		c.weighty = 1;
 		c.weightx = 1;
 		c.insets = new Insets(5, 5, 5, 5);
 		this.add(gm.getGridPanel(), c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 2;
 		this.add(makeButtonPanel(), c);
 
 		this.setVisible(true);
 	}
 
 	private JPanel makeButtonPanel() {
 		// TODO most of these will become tabs, adding temporarily for
 		// navigation purposes
 		ScreenManager sm = getScreenManager();
 		JPanel buttonPanel = Gui.makePanel((LayoutManager) null, new MaxSize(
 				500, 50), PrefSize.NULL, makeStartButton(), Gui.makeButton(
 				"Pause", null, new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						getGuiManager().pauseSim();
 						canSpawn = true;
 					}
 				}), Gui.makeButton("Statistics", null,
 				new GeneralButtonListener("Statistics", sm)));
 		return buttonPanel;
 	}
 
 	public void setSpawn(boolean canSpawn) {
 		this.canSpawn = canSpawn;
 	}
 
 	private JButton makeStartButton() {
 		JButton b = Gui.makeButton("Start/Resume", null, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				SimulatorGuiManager gm = getGuiManager();

				gm.getGridPanel().repaint();
 				canSpawn = false;

 				gm.getGridPanel().repaint();	

 				gm.startSim();
 				
 			}
 		});
 		return b;
 	}
 
 	@Override
 	public void load() {
 		entitiesScreen.load();
 		globalFieldScreen.load();
 		// TODO when New Simulation is pressed on the menu bar, we need to
 		// reset the number of
 		// prototypes
 		entities = Simulator.prototypeNames().toArray(entities);
 		agentComboBox = new JComboBox(entities);
 		agentComboBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 
 				// To ensure type safety with the "String" combo box, we need
 				// to convert the objects to strings.
 				Object[] tempObjList = Simulator
 						.getPrototype(
 								agentComboBox.getSelectedItem().toString())
 						.getCustomFieldMap().keySet().toArray();
 				String[] tempStringList = new String[tempObjList.length];
 				for (int i = 0; i < tempObjList.length; i++) {
 					tempStringList[i] = tempObjList[i].toString();
 				}
 
 				layerComboBox = new JComboBox(tempStringList);
 				layerComboBox.setMaximumSize(new Dimension(200, 50));
 				layerPanelLayers.remove(1);
 				layerPanelLayers.add(layerComboBox);
 				validate();
 				repaint();
 			}
 		});
 		if (entities.length != 0) {
 
 			// To ensure type safety with the "String" combo box, we need to
 			// convert the objects to strings.
 			Object[] tempObjList = Simulator
 					.getPrototype(agentComboBox.getSelectedItem().toString())
 					.getCustomFieldMap().keySet().toArray();
 			String[] tempStringList = new String[tempObjList.length];
 			for (int i = 0; i < tempObjList.length; i++) {
 				tempStringList[i] = tempObjList[i].toString();
 			}
 
 			layerComboBox = new JComboBox(tempStringList);
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
