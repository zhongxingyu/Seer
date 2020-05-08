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
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
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
 import javax.swing.SwingConstants;
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
 
 public class ViewSimScreen extends Screen {
 
 	private static final long serialVersionUID = -6872689283286800861L;
 
 	private JComboBox agentComboBox;
 	private JComboBox layerComboBox;
 
 	private String[] entities;
 
 	private JPanel layerPanelAgents;
 	private JPanel layerPanelLayers;
 
 	public ViewSimScreen(final SimulatorGuiManager gm) {
 		super(gm);
 		
 		entities = new String[0];
 		this.setLayout(new BorderLayout());
 		//gridRec = new SimulationRecorder(sm.getStatManager());
 
 		agentComboBox = Gui.makeComboBox(null,new MaxSize(200,50));
 
 		layerComboBox = Gui.makeComboBox(null, new MaxSize(200,50));
 
 		final JColorChooser colorTool = Gui.makeColorChooser();
 
 		layerPanelAgents = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null,
 				new JLabel("Agents", SwingConstants.CENTER),agentComboBox);
 
 		layerPanelLayers = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null,
 				new JLabel("Layers", SwingConstants.CENTER),layerComboBox);
 
 		JPanel layerPanelButtons = Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null,
 			Gui.makeButton("Apply",null,
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						Simulator.newLayer(layerComboBox.getSelectedItem().toString(), colorTool.getColor());
 						try {
 							gm.setSimLayerExtremes();
 						} catch (EvaluationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						gm.getGridPanel().setLayers(true);
 						gm.getGridPanel().repaint();
 					}
 				}
 			),
 			Gui.makeButton("Clear",null,
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						gm.getGridPanel().setLayers(false);
 						gm.getGridPanel().repaint();
 					} 
 				}
 			)
 		);
 
 		JPanel upperLayerPanel = Gui.makePanel(BoxLayoutAxis.PAGE_AXIS, null, null,
 				layerPanelAgents,layerPanelLayers,layerPanelButtons);
 		upperLayerPanel.setAlignmentX(LEFT_ALIGNMENT);
 
 		gm.getGridPanel().setAlignmentY(CENTER_ALIGNMENT);
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
 
 		JPanel layerPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS,null,null,
 				upperLayerPanel,Gui.makeColorChooserPanel(colorTool));
 		layerPanel.setAlignmentY(CENTER_ALIGNMENT);
 
 
 		this.add(
 			new JLabel("View Simulation", SwingConstants.CENTER), BorderLayout.NORTH
 		);
 		this.add(makeButtonPanel(), BorderLayout.SOUTH);
 		this.add(Gui.makePanel(BoxLayoutAxis.LINE_AXIS,null,null,
 				layerPanel,gm.getGridPanel()), BorderLayout.CENTER);
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
 			Gui.makeButton("Entities",null, new GeneralButtonListener("Entities",sm)),
 			Gui.makeButton("Global Fields",null, new GeneralButtonListener("Fields",sm)),
 			Gui.makeButton("Setup options",null, new GeneralButtonListener("Grid Setup",sm)),
 			Gui.makeButton("Statistics",null, new GeneralButtonListener("Statistics",sm)));
 		return buttonPanel;
 	}
 
 	private JButton makeStartButton(){
 		JButton b = Gui.makeButton("Start/Resume",null,new ActionListener() {
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
 //				if (stepCount == 0)
 //					;//sm.getStatManager().setStartTime(startTime);
 				gm.startSim();
 			}
 		});
 		return b;
 	}
 
 	@Override
 	public void load() {
 		entities = Simulator.prototypeNames().toArray(entities);
 		agentComboBox = new JComboBox(entities);
 		agentComboBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if(agentComboBox.getItemAt(0)!=null)
 					layerComboBox = new JComboBox(Simulator.getPrototype
 							(agentComboBox.getSelectedItem().toString())
 							.getCustomFieldMap().keySet().toArray());
 				else
 					layerComboBox = new JComboBox();
 				layerComboBox.setMaximumSize(new Dimension(200, 50));
 				layerPanelLayers.remove(1);
 				layerPanelLayers.add(layerComboBox);
 				validate();
 				repaint();
 			}
 		});
 		if(entities.length != 0){
 			if(agentComboBox.getItemAt(0)!=null)
 				layerComboBox = new JComboBox(Simulator.getPrototype
 						(agentComboBox.getItemAt(0).toString())
 						.getCustomFieldMap().keySet().toArray());
 			else
 				layerComboBox = new JComboBox();
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
