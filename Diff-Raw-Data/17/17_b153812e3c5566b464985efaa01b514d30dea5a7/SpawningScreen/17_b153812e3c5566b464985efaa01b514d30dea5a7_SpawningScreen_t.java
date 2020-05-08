 /**
  * SpawningScreen
  * 
  * Class representing the screen that allows to determine spawning conditions,
  * or spawn new entities during a simulation.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 public class SpawningScreen extends Screen {
 	//TODO how do we handle if spawn are set, and then the grid is made smaller,
 	//     and some of the spawns are now out of bounds? delete those fields?
 
 	private String[] entities;
 
 	private ArrayList<JComboBox> entityTypes;
 
 	private ArrayList<JComboBox> spawnPatterns;
 
 	//TODO temporary placeholder
 	private String[] spawnOptions = {"Clustered", "Horizontal", "Vertical", "Random"};
 
 	private ArrayList<JTextField> xLocs;
 
 	private ArrayList<JTextField> yLocs;
 
 	private ArrayList<JTextField> numbers;
 
 	private ArrayList<JButton> deleteButtons;
 
 	private ArrayList<JPanel> subPanels;
 
 	private JButton addSpawnButton;
 
 	private JPanel listPanel;
 
 	private Component glue;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6312784326472662829L;
 
 	public SpawningScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		entities = new String[0];
 		JLabel label = new JLabel("Spawning");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		listPanel = new JPanel();
 		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
 		JPanel labelsPanel = new JPanel();
 		labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.X_AXIS));
 
 		//TODO mess with sizes of labels to line up with components
 		JLabel entityLabel = new JLabel("Entity Type");
 		entityLabel.setPreferredSize(new Dimension(200, 30));
		
 		JLabel patternLabel = new JLabel("Spawn Pattern");
 		patternLabel.setPreferredSize(new Dimension(270, 30));
		
 		JLabel xLabel = new JLabel("x Loc.");
 		xLabel.setPreferredSize(new Dimension(100, 30));
		
 		JLabel yLabel = new JLabel("Y Loc.");
 		yLabel.setPreferredSize(new Dimension(100, 30));
		
 		JLabel numberLabel = new JLabel("Number");
		numberLabel.setPreferredSize(new Dimension(290, 30));
		
		labelsPanel.add(Box.createHorizontalGlue());
		labelsPanel.add(Box.createHorizontalGlue());
 		labelsPanel.add(Box.createHorizontalGlue());
 		labelsPanel.add(entityLabel);
 		entityLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		labelsPanel.add(Box.createHorizontalGlue());
 		labelsPanel.add(patternLabel);
 		patternLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		labelsPanel.add(Box.createHorizontalGlue());
 		labelsPanel.add(xLabel);
 		labelsPanel.add(yLabel);
 		labelsPanel.add(numberLabel);
 		labelsPanel.add(Box.createHorizontalGlue());
		
 		mainPanel.add(labelsPanel);
 		mainPanel.add(listPanel);
		
 		labelsPanel.setAlignmentX(CENTER_ALIGNMENT);
 
 		entityTypes = new ArrayList<JComboBox>();
 		spawnPatterns = new ArrayList<JComboBox>();
 		xLocs = new ArrayList<JTextField>();
 		yLocs = new ArrayList<JTextField>();
 		numbers = new ArrayList<JTextField>();
 		deleteButtons = new ArrayList<JButton>();
 		subPanels = new ArrayList<JPanel>();
 
 		addSpawnButton = new JButton("Add Spawn");
 		addSpawnButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				addSpawn();
 			}
 		});
 		listPanel.add(addSpawnButton);
 		addSpawnButton.setAlignmentX(CENTER_ALIGNMENT);
 		glue = Box.createVerticalGlue();
 		listPanel.add(glue);
 
 		JPanel buttonPanel = new JPanel();
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				});
 		JButton finishButton = new JButton("Finish");
 		finishButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						try{
 							for (int i = 0; i < xLocs.size(); i++) {
 								if (Integer.parseInt(xLocs.get(i).getText()) < 0 ||
 										Integer.parseInt(yLocs.get(i).getText()) < 0 ||
 										Integer.parseInt(numbers.get(i).getText()) < 0) {
 									throw new Exception("Coordinates and numbers must be integers greater at least 0");
 								}
 							}
 							ArrayList<SpawnCondition> conditions = sm.getSpawnConditions();
 							conditions.clear();
 							for (int i = 0; i < entityTypes.size(); i++) {
 								SpawnCondition condition = new SpawnCondition(sm.getFacade().getPrototype(
 										((String) entityTypes.get(i).getSelectedItem())),
 										Integer.parseInt(xLocs.get(i).getText()), 
 										Integer.parseInt(yLocs.get(i).getText()), 
 										Integer.parseInt(numbers.get(i).getText()),
 										(String) spawnPatterns.get(i).getSelectedItem());
 								sm.getSpawnConditions().add(condition);
 							}
 							sm.update(sm.getScreen("Edit Simulation"));
 						}
 						catch (NumberFormatException excep) {
 							JOptionPane.showMessageDialog(null,
 									"Coordinates and numbers must be integers greater than 0");
 							excep.printStackTrace();
 						}
 						catch (Exception excep) {
 							JOptionPane.showMessageDialog(null, excep.getMessage());
 						}
 					}
 				});
 		buttonPanel.add(cancelButton);
 		buttonPanel.add(finishButton);
 		this.add(label, BorderLayout.NORTH);
 		this.add(mainPanel, BorderLayout.CENTER);
 		this.add(buttonPanel, BorderLayout.SOUTH);
 	}
 
 	public void reset() {
 		entityTypes.clear();
 		spawnPatterns.clear();
 		xLocs.clear();
 		yLocs.clear();
 		numbers.clear();
 		subPanels.clear();
 		deleteButtons.clear();
 		listPanel.removeAll();
 	}
 
 	@Override
 	public void load() {
 		reset();
 		entities = sm.getFacade().prototypeNames().toArray(entities);
 		ArrayList<SpawnCondition> spawnConditions = sm.getSpawnConditions(); 
 
 		for (int i = 0; i < spawnConditions.size(); i++) { 
 			addSpawn();
 			entityTypes.get(i).setSelectedItem(spawnConditions.get(i).prototype.getName());
 			spawnPatterns.get(i).setSelectedItem(spawnConditions.get(i).pattern);
 			xLocs.get(i).setText(spawnConditions.get(i).x + "");
 			yLocs.get(i).setText(spawnConditions.get(i).y + "");
 			numbers.get(i).setText(spawnConditions.get(i).number + "");
 		}
 		if (spawnConditions.size() == 0) {
 			addSpawn();
 		}
 		validate();
 	}
 
 	private void addSpawn() {
 		JPanel newPanel = new JPanel();
 		newPanel.setLayout(
 				new BoxLayout(newPanel, 
 						BoxLayout.X_AXIS)
 				);
 		JComboBox newBox = new JComboBox(entities);
 		newBox.setMaximumSize(new Dimension(250, 30));
 		entityTypes.add(newBox);
 		JComboBox newSpawnType = new JComboBox(spawnOptions);
 		newSpawnType.setMaximumSize(new Dimension(250, 30));
 		spawnPatterns.add(newSpawnType);
 		newSpawnType.addItemListener(new PatternListener(spawnPatterns.indexOf(newSpawnType)));
 		JTextField newXLoc = new JTextField(10);
 		newXLoc.setMaximumSize(new Dimension(100, 30));
 		newXLoc.setText("0");
 		xLocs.add(newXLoc);
 		JTextField newYLoc = new JTextField(10);
 		newYLoc.setMaximumSize(new Dimension(100, 30));
 		newYLoc.setText("0");
 		yLocs.add(newYLoc);
 		JTextField newNumber = new JTextField(10);
 		newNumber.setMaximumSize(new Dimension(100, 30));
 		newNumber.setText("1");
 		numbers.add(newNumber);
 		JButton newButton = new JButton("Delete");
 		newButton.addActionListener(new DeleteListener());
 		deleteButtons.add(newButton);
 		newButton.setActionCommand(
 				deleteButtons.indexOf(newButton) + ""
 				);
 		newPanel.add(newBox);
 		newPanel.add(newSpawnType);
 		newPanel.add(newXLoc);
 		newPanel.add(newYLoc);
 		newPanel.add(newNumber);
 		newPanel.add(newButton);
 		subPanels.add(newPanel);
 		listPanel.add(newPanel);
 		listPanel.add(addSpawnButton);
 		listPanel.add(glue);
 		listPanel.validate();
 		repaint();	
 	}
 
 	private void deleteSpawn(int n) {
 		entityTypes.remove(n);
 		((PatternListener)spawnPatterns.get(n).getItemListeners()[0]).setNum(n);
 		spawnPatterns.remove(n);
 		xLocs.remove(n);
 		yLocs.remove(n);
 		numbers.remove(n);
 		deleteButtons.remove(n);
 		for (int i = n; i < deleteButtons.size(); i++) {
 			deleteButtons.get(i).setActionCommand(i + "");
 			((PatternListener)spawnPatterns.get(i).getItemListeners()[0]).setNum(i);
 		}
 		listPanel.remove(subPanels.get(n));
 		subPanels.remove(n);
 		listPanel.validate();
 		repaint();
 	}
 
 	private class DeleteListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e){
 			String action = e.getActionCommand();
 			deleteSpawn(Integer.parseInt(action));
 		}
 	}
 
 	private class PatternListener implements ItemListener {
 
 		private int n;
 
 		public PatternListener(int n) {
 			this.n = n;
 		}
 		
 		public void setNum(int n) {
 			this.n = n;
 		}
 		
 		@Override
 		public void itemStateChanged(ItemEvent e) {
 			if (((String)e.getItem()).equals("Random")) {
 				xLocs.get(n).setEnabled(false);
 				yLocs.get(n).setEnabled(false);
 			}
 			else {
 				xLocs.get(n).setEnabled(true);
 				yLocs.get(n).setEnabled(true);
 			}
 			repaint();
 		}
 
 
 	}
 
 }
