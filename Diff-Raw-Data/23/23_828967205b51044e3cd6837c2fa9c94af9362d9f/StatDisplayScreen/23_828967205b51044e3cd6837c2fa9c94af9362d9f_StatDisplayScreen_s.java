 package edu.wheaton.simulator.gui.screen;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorFacade;
 
 public class StatDisplayScreen extends Screen {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static String DISPLAY_AVG_FIELD_VALUE_STR = "Average field value"; 
 	private static String DISPLAY_AVG_LIFESPAN_STR = "Average lifespan"; 
 	private static String DISPLAY_POP_OVER_TIME_STR = "Population over time";
 
 	
 	/**
 	 * Source of simulation statistics. 
 	 */
 	//private StatisticsManager statMan; 
 
 	private JPanel displayPanel; 
 
 	private JComboBox prototypes; 
 
 	private JComboBox displayTypes; 
 
 	private JComboBox fields; 
 
 	private SimulatorFacade gm; 
 	
 	/**
 	 * Constructor. 
 	 * Make the screen. 
 	 * @param gm ScreenManager. 
 	 */
 	public StatDisplayScreen(SimulatorFacade gm) {
 		super(gm);
 		this.gm = gm;
 		//Setup GridBagLayout & demensions.
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{69, 81, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 
 		//Setup displayPanel -- The panel which displays the graph
 		displayPanel = getDisplayPanel();
 		//Setup displayPanel's GridBagConstraints
 		GridBagConstraints gbc_displayPanel = new GridBagConstraints();
 		gbc_displayPanel.gridwidth = 5;
 		gbc_displayPanel.insets = new Insets(20, 30, 20, 30);
 		gbc_displayPanel.fill = GridBagConstraints.BOTH;
 		gbc_displayPanel.gridx = 0;
 		gbc_displayPanel.gridy = 0;
 		add(displayPanel, gbc_displayPanel);
 
 		//Setup agentList -- The ComboBox which lists possible categories of agents to view.
 		prototypes = new JComboBox();
 		GridBagConstraints gbc_agentList = new GridBagConstraints();
 		//Setup agentList's GridBagConstraints
 		gbc_agentList.insets = new Insets(0, 30, 10, 5);
 		gbc_agentList.fill = GridBagConstraints.HORIZONTAL;
 		gbc_agentList.gridx = 0;
 		gbc_agentList.gridy = 1;
 		add(prototypes, gbc_agentList);
 		setPrototypeListener(prototypes);
 
 		//Setup displayList -- The ComboBox which lists possible ways of viewing the selected population.
 		displayTypes = new JComboBox();
 		GridBagConstraints gbc_displayList = new GridBagConstraints();
 		//Setup displayList's GridBagConstraints
 		gbc_displayList.insets = new Insets(0, -5, 10, 5);
 		gbc_displayList.fill = GridBagConstraints.HORIZONTAL;
 		gbc_displayList.gridx = 1;
 		gbc_displayList.gridy = 1;
 		add(displayTypes, gbc_displayList);
 		setDisplayTypeListener(displayTypes);
 
 		//Setup fieldList -- The ComboBox which lists possible fields to track. 
 		fields = new JComboBox();
 		GridBagConstraints gbc_fieldList = new GridBagConstraints();
 		//Setup fieldList's GridBagConstraints
 		gbc_fieldList.insets = new Insets(0, -5, 10, 30);
 		gbc_fieldList.fill = GridBagConstraints.HORIZONTAL;
 		gbc_fieldList.gridx = 2;
 		gbc_fieldList.gridy = 1;
 		add(fields, gbc_fieldList);
 		setDisplayTypeListener(fields);
 
 		//Setup the backButton. 
 		JButton backButton = new JButton("Back");
 		GridBagConstraints gbc_backButton = new GridBagConstraints();
 		gbc_backButton.gridwidth = 2;
 		//Setup backButton's GridBagConstraints
 		gbc_backButton.insets = new Insets(-5, -30, 6, 30);
 		gbc_backButton.gridx = 3;
 		gbc_backButton.gridy = 1;
 		add(backButton, gbc_backButton);
 		setBackButtonListener(backButton);
 	}
 
 	/**
 	 * To be executed when a prototype has been selected. 
 	 */
 	private void onPrototypeSelected() { 
 		String selectedPrototype = (String) prototypes.getSelectedItem();
 		fields.removeAllItems();
 		for (String fieldName : gm.getPrototype(selectedPrototype).getCustomFieldMap().keySet()) { 
 			fields.addItem(fieldName);
 		}
 		onDisplayTypeSelected();
 	}
 	
 	/**
 	 * To be executed when a display type has been selected. 
 	 */
 	private void onDisplayTypeSelected() { 
 		String selectedDisplayType = (String) displayTypes.getSelectedItem();
 		if (selectedDisplayType != null && 
 				selectedDisplayType.equals(DISPLAY_AVG_FIELD_VALUE_STR)) { 
 			onFieldSelected();
 		} else { 
 			makeDisplayPanelPaint();
 		}
 	}
 
 	private void makeDisplayPanelPaint() { 
 		new Thread() {
 			@Override
 			public void run() {
 				displayPanel.paint(displayPanel.getGraphics());
 			}
 		}.run();
 	}
 
 	/**
 	 * To be executed when a field has been selected. 
 	 */
 	private void onFieldSelected() { 
 		String selectedDisplayType = (String) displayTypes.getSelectedItem();
 		if (selectedDisplayType != null &&
 				selectedDisplayType.equals(DISPLAY_AVG_FIELD_VALUE_STR)) { 
 				makeDisplayPanelPaint();
 		}
 	}
 
	private void setBackButtonListener(JButton backButton) { 
 		backButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ScreenManager sm = Gui.getScreenManager();
 				Screen toDisplay = sm.getScreen("View Simulation");
 				ScreenManager.loadScreen(toDisplay);
 				sm.update(toDisplay);
 			}
 		});
 	}
 
 	private void setPrototypeListener(JComboBox protBox) { 
 		protBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onPrototypeSelected();
 			}
 		});
 	}
 	
 	private void setDisplayTypeListener(JComboBox catBox) { 
 		catBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onDisplayTypeSelected();
 			}
 		});
 	}
 	
 	private void setFieldListener(JComboBox fieldBox) {
 		fieldBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onFieldSelected();
 			}
 		});
 	}
 
 	@Override
 	public void load() {
 		prototypes.removeAllItems();
 		displayTypes.removeAllItems();
 		fields.removeAllItems();
 		for (String name : gm.getPrototypeNames()) { 
 			prototypes.addItem(name);
 		}
 		displayTypes.addItem(DISPLAY_AVG_FIELD_VALUE_STR);
 		displayTypes.addItem(DISPLAY_AVG_LIFESPAN_STR);
 		displayTypes.addItem(DISPLAY_POP_OVER_TIME_STR);
 	}
 
 	private JPanel getDisplayPanel() { 
 		return new JPanel() { 
 			
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -4375342368896479163L;
 
 			@Override
 			public void paint(Graphics g) {
 				super.paint(g);
 				String selectedDisplayType = (String) displayTypes.getSelectedItem();
 				int width = getWidth() -1; 
 				int height = getHeight() -1;
 				g.setColor(Color.BLACK);
 				g.fillRect(0, 0, width, height);
 				
 				if (selectedDisplayType == null) 
 					return; 
 				else if (selectedDisplayType.equals(DISPLAY_AVG_LIFESPAN_STR)) 
 					paintLife(g, width, height);
 				else if (selectedDisplayType.equals(DISPLAY_POP_OVER_TIME_STR))
 					paintPop(g, width, height);
 				else if (selectedDisplayType.equals(DISPLAY_AVG_FIELD_VALUE_STR))
 					paintField(g, width, height);
 			}
 			
 			private void paintPop(Graphics g, int width, int height) { 
 				int[] pops = gm.getPopVsTime((String) prototypes.getSelectedItem());
 				if (pops.length < 1)
 					return; 
 				
 				int[] extremes = getHighLowIndex(pops);
 				double max = pops[extremes[0]];
 				double min = 0;
 				
 				int lastX = 0; 
 				int lastY = getAppropriateY(pops[0], max, min, height);
 
 				g.setColor(Color.GREEN);
 				for (int index = 1; index < pops.length; index++) { 
 					int currentX = getAppropriateX(index, pops.length-1, width);
 					int currentY = getAppropriateY(pops[index], max, min, height);
 					g.drawLine(lastX, lastY, currentX, currentY);
 					lastX = currentX;
 					lastY = currentY;
 				}
 			}
 			
 			private void paintField(Graphics g, int width, int height) { 
 				String protName = (String) prototypes.getSelectedItem();
 				String fieldName = (String) fields.getSelectedItem();
 				double[] avgValues = gm.getAvgFieldValue(protName, fieldName);
 				if (avgValues.length < 1)
 					return; 
 				
 				int[] extremes = getHighLowIndex(avgValues);
 				double maxYValue = avgValues[extremes[0]];
 				double minYValue = avgValues[extremes[1]];
 				
 				int zeroY = getAppropriateY(0, maxYValue, minYValue, height);
 				g.setColor(Color.WHITE);
 				g.drawLine(0, zeroY, width, zeroY);
 				
 				int lastX = 0; 
 				int lastY = getAppropriateY(avgValues[0], maxYValue, minYValue, height);
 
 				g.setColor(Color.RED);
 				for (int index = 1; index < avgValues.length; index++) { 
 					int currentX = getAppropriateX(index, avgValues.length-1, width);
 					int currentY = getAppropriateY(avgValues[index], maxYValue, minYValue, height);
 					g.drawLine(lastX, lastY, currentX, currentY);
 					lastX = currentX;
 					lastY = currentY;
 				}
 			}
 			
 			private void paintLife(Graphics g, int width, int height) { 
 				String protName = (String) prototypes.getSelectedItem();
 				double avgLifespan = gm.getAvgLifespan(protName);
 				g.setColor(Color.CYAN);
 				g.drawString("" + avgLifespan, width/2, height/2);
 			}
 
 			private int getAppropriateY(double currentValue, double maxValue, double minValue, int height) { 
 				return (int)(height - ((currentValue - minValue)/(maxValue - minValue)) * height); 
 			}
 			
 			private int getAppropriateX(double currentIndex, double maxIndex, double width) { 
 				return (int) ((currentIndex / maxIndex) * width);
 			}
 			
 			private int[] getHighLowIndex(double[] values) { 
 				if (values.length == 0) 
 					return new int[] {-1, -1};
 				int maxIndex = 0;
 				int minIndex = 0;
 				for (int index = 0; index < values.length; index++) { 
 					double currentValue = values[index]; 
 					if (currentValue > values[maxIndex])
 						maxIndex = index; 
 					else if (currentValue < values[minIndex])
 						minIndex = index; 
 				}
 				return new int[] {maxIndex, minIndex}; 
 			}
 			
 			private int[] getHighLowIndex(int[] values) { 
 				if (values.length == 0) 
 					return new int[] {-1, -1};
 				int maxIndex = 0;
 				int minIndex = 0;
 				for (int index = 0; index < values.length; index++) { 
 					double currentValue = values[index]; 
 					if (currentValue > values[maxIndex])
 						maxIndex = index; 
 					else if (currentValue < values[minIndex])
 						minIndex = index; 
 				}
 				return new int[] {maxIndex, minIndex}; 
 			}
 		};
 	}
 }
