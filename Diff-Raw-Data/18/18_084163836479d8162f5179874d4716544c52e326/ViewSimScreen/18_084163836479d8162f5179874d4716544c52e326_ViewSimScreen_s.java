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
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
import edu.wheaton.simulator.gui.BoxLayoutAxis;
 import edu.wheaton.simulator.gui.GeneralButtonListener;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.MaxSize;
 import edu.wheaton.simulator.gui.PrefSize;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorFacade;
 
 public class ViewSimScreen extends Screen {
 
 	private static final long serialVersionUID = -6872689283286800861L;
 
 
 
 	private GridBagConstraints c;
 
 	private boolean canSpawn;
 
 	private final EntityScreen entitiesScreen;
 
 	private final Screen layerScreen;
 
 	private final Screen globalFieldScreen;
 
 	private final Screen optionsScreen;
 
 	public ViewSimScreen(final SimulatorFacade gm) {
 		super(gm);
 		setSpawn(false);
 		this.setLayout(new GridBagLayout());
 		((GridBagLayout)this.getLayout()).columnWeights = new double[]{0, 1};
 
 		final JTabbedPane tabs = new JTabbedPane();
 		tabs.setMaximumSize(new Dimension(550, 550));
 		entitiesScreen = new EntityScreen(gm);
 		layerScreen = new LayerScreen(gm);
 		globalFieldScreen = new FieldScreen(gm);
 		optionsScreen = new SetupScreen(gm);
 		tabs.addTab("Agent", entitiesScreen);
 		tabs.addTab("Layers", layerScreen);
 		tabs.addTab("Global Fields", globalFieldScreen);
 		tabs.addTab("Options", optionsScreen);
 		tabs.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				if (tabs.getSelectedComponent().toString() == "Agent")
 					setSpawn(true);
 				else {
 					setSpawn(false);
 				}
 				entitiesScreen.load();
 				layerScreen.load();
 				globalFieldScreen.load();
 				optionsScreen.load();
 			}
 		});
 
 		gm.getGridPanel().addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent me) {
 				if (getGuiManager().canSpawn()) {
 					int standardSize = Math.min(gm.getGridPanel().getWidth()
 							/ gm.getGridWidth(), gm.getGridPanel()
 							.getHeight() / gm.getGridHeight());
 
 					int x = me.getX() / standardSize;
 					int y = me.getY() / standardSize;
 					if (canSpawn) {
 						if (gm.getAgent(x, y) == null) {
 							gm.addAgent(entitiesScreen.getList()
 									.getSelectedValue().toString(), x, y);
 						} else {
 							gm.removeAgent(x, y);
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
 		ScreenManager sm = getScreenManager();
 		JPanel buttonPanel = Gui.makePanel((LayoutManager) null, new MaxSize(
 				500, 50), PrefSize.NULL, makeStartButton(), Gui.makeButton(
 						"Pause", null, new ActionListener() {
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								getGuiManager().pause();
 								canSpawn = true;
 							}
 						}), Gui.makeButton("Statistics", null,
 								new GeneralButtonListener("Statistics", sm)),
 								Gui.makeButton("Clear Agents", null,
 										new ActionListener() {
 									@Override
 									public void actionPerformed(ActionEvent e) {
 										getGuiManager().clearGrid();
 									}
 								}), Gui.makeButton("Fill Grid", null,
 										new ActionListener() {
 									@Override
 									//TODO make sure that this is disabled when the user shouldn't be able to press it
 									public void actionPerformed(ActionEvent e) {
 										//adding fill grid here
 										String agentName = entitiesScreen.getList().getSelectedValue().toString();
 										if (agentName != null)
 											getGuiManager().fillGrid(agentName);
 									}
 								})
 				);
 		return buttonPanel;
 	}
 
 	public void setSpawn(boolean canSpawn) {
 		this.canSpawn = canSpawn;
 	}
 
 	private JButton makeStartButton() {
 		JButton b = Gui.makeButton("Start/Resume", null, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				SimulatorFacade gm = getGuiManager();
 				canSpawn = false;
 				gm.getGridPanel().repaint();	
 				gm.start();
 
 			}
 		});
 		return b;
 	}
 
 	@Override
 	public void load() {
 		entitiesScreen.load();
 		layerScreen.load();
 		globalFieldScreen.load();
 		optionsScreen.load();
 
 		validate();
 		getGuiManager().getGridPanel().repaint();
 	}
 }
