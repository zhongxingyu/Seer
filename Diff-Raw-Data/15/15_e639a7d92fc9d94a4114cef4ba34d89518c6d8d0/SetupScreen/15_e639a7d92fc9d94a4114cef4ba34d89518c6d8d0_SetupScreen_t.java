 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.entity.PrototypeID;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 
 //TODO commented code for adding operators to ending conditions :
 //     see if it should stay for future use or just be deleted
 //TODO handle "invalid" input
 public class SetupScreen extends Screen {
 
 	private JTextField nameField;
 
 	private JTextField width;
 
 	private JTextField height;
 
 	private JTextField timeField;
 
 	private String[] agentNames;
 
 	private ArrayList<JComboBox> agentTypes;
 
 	private ArrayList<JTextField> values;
 
 	//private String[] opNames = {">=", "=", "<="};
 
 	//private ArrayList<JComboBox> operations;
 
 	private ArrayList<JButton> deleteButtons;
 
 	private ArrayList<JPanel> subPanels;
 
 	private JPanel conListPanel;
 
 	private JButton addConditionButton;
 
 	private Component glue;
 
 	private static final long serialVersionUID = -8347080877399964861L;
 
 	public SetupScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		agentNames = new String[0];
 		JLabel label = new JLabel("Simulation Setup");
 		label.setPreferredSize(new Dimension(300, 150));
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		this.add(label, BorderLayout.NORTH);
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		mainPanel.setAlignmentX(CENTER_ALIGNMENT);
 		JLabel nameLabel = new JLabel ("Name: ");
 		nameLabel.setMaximumSize(new Dimension(100, 40));
 		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		nameField = new JTextField(sm.getGUIname(), 25);
 		nameField.setMaximumSize(new Dimension(400, 30));
 		JLabel widthLabel = new JLabel("Width: ");
 		widthLabel.setMaximumSize(new Dimension(100, 40));
 		widthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		width = new JTextField(sm.getGUIwidth()+"", 10);
 		width.setMaximumSize(new Dimension(80, 30));
 		JLabel heightLabel = new JLabel("Height: ");
 		heightLabel.setMaximumSize(new Dimension(210, 40));
 		heightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		height = new JTextField(sm.getGUIheight()+"", 0);
 		height.setMaximumSize(new Dimension(80, 30));
 		JPanel panel1 = new JPanel();
 		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
 		panel1.add(nameLabel);
 		panel1.add(nameField);
 		JPanel panel2 = new JPanel();
 		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
 		panel2.add(heightLabel);
 		panel2.add(height);
 		panel2.add(widthLabel);
 		panel2.add(width);
 
 		JPanel conMainPanel = new JPanel();
 		JPanel conBodyPanel = new JPanel();
 		JPanel timePanel = new JPanel();
 		JPanel conLabelsPanel = new JPanel();
 		conListPanel = new JPanel();
 
 		JLabel endingLabel = new JLabel("Ending Conditions");
 		endingLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		endingLabel.setPreferredSize(new Dimension(300, 100));
 		JLabel timeLabel = new JLabel("Time limit: ");
 		JLabel agentTypeLabel = new JLabel("Agent Type");
 		agentTypeLabel.setPreferredSize(new Dimension(200, 30));
 		JLabel valueLabel = new JLabel("Boundary Value");
 		valueLabel.setPreferredSize(new Dimension(400, 30));
 		//JLabel operationLabel = new JLabel("Operation");
 		//operationLabel.setPreferredSize(new Dimension(350, 30));
 		timeField = new JTextField(15);
 		timeField.setMaximumSize(new Dimension(200, 40));
 		agentTypes = new ArrayList<JComboBox>();
 		values = new ArrayList<JTextField>();
 		//operations = new ArrayList<JComboBox>();
 		deleteButtons = new ArrayList<JButton>();
 		subPanels = new ArrayList<JPanel>();
 		addConditionButton = new JButton("Add Field");
 		addConditionButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						addCondition();
 					}
 				});
 		glue = Box.createVerticalGlue();
 		addCondition();
 		//TODO line up components
 		conMainPanel.setLayout(
 				new BorderLayout());
 		conBodyPanel.setLayout(
 				new BoxLayout(conBodyPanel, BoxLayout.Y_AXIS)
 				);
 		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
 		conLabelsPanel.setLayout(
 				new BoxLayout(conLabelsPanel, BoxLayout.X_AXIS)
 				);
 		conListPanel.setLayout(
 				new BoxLayout(conListPanel, BoxLayout.Y_AXIS)
 				);
 		subPanels.get(0).setLayout(
 				new BoxLayout(subPanels.get(0), BoxLayout.X_AXIS)
 				);
 		timePanel.add(timeLabel);
 		timePanel.add(timeField);
 		timePanel.setAlignmentX(CENTER_ALIGNMENT);
 		conMainPanel.add(endingLabel, BorderLayout.NORTH);
 		conLabelsPanel.add(Box.createHorizontalGlue());
 		conLabelsPanel.add(agentTypeLabel);
 		agentTypeLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		agentTypeLabel.setAlignmentX(LEFT_ALIGNMENT);
 		//conLabelsPanel.add(operationLabel);
 		//operationLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		conLabelsPanel.add(valueLabel);
 		conLabelsPanel.add(Box.createHorizontalGlue());
 		valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		subPanels.get(0).add(agentTypes.get(0));
 		//subPanels.get(0).add(operations.get(0));
 		subPanels.get(0).add(values.get(0));
 		subPanels.get(0).add(deleteButtons.get(0));
 		conListPanel.add(subPanels.get(0));
 		conListPanel.add(addConditionButton);
 		conListPanel.add(glue);
 		conBodyPanel.add(timePanel);
 		conBodyPanel.add(conLabelsPanel);
 		conBodyPanel.add(conListPanel);
 		conMainPanel.add(conBodyPanel, BorderLayout.CENTER);
 
 		JPanel buttonPanel = new JPanel();
 		JButton finishButton = new JButton("Finish");
 		finishButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.updateGUIManager(nameField.getText(), Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
 						JPanel[][] grid = new JPanel[GUIManager.getGridWidth()][GUIManager.getGridHeight()];
 						for (int j = 0; j < GUIManager.getGridWidth(); j++){
 							for (int i = 0; i < GUIManager.getGridHeight(); i++) {
 								grid[i][j] = new JPanel();
 								grid[i][j].setOpaque(false);
 								grid[i][j].setBorder(BorderFactory.createEtchedBorder());
 							}	
 						}
 						for (int i = 0; i < values.size(); i++) {
 							sm.getEnder().setPopLimit(
 									sm.getFacade().getPrototype(
 											(String)(agentTypes.get(i).getSelectedItem())
 											).getPrototypeID(), 
 											Integer.parseInt(values.get(i).getText())
 									);
 						}
 
 						sm.update(sm.getScreen("Edit Simulation"));
 					}
 				}
 				);
 		JButton backButton = new JButton("Back");
 		backButton.addActionListener(
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation"));
 					}
 				}
 				);
 		buttonPanel.add(backButton);
 		buttonPanel.add(finishButton);
 		mainPanel.add(panel1);
 		mainPanel.add(panel2);
 		JPanel uberPanel = new JPanel();
 		uberPanel.setLayout(new BoxLayout(uberPanel, BoxLayout.Y_AXIS));
 		uberPanel.add(mainPanel);
 		uberPanel.add(conMainPanel);
 		this.add(uberPanel, BorderLayout.CENTER);
 		this.add(buttonPanel, BorderLayout.SOUTH);
 	}
 
 	@Override
 	public void load() {
 		reset();
 		nameField.setText(sm.getGUIname());
 		width.setText(sm.getGUIwidth() + "");
 		height.setText(sm.getGUIheight() + "");
 
 		SimulationEnder se = sm.getEnder();
 		Set<String> agents = sm.getFacade().prototypeNames();
 		agentNames = agents.toArray(agentNames);
 		timeField.setText(se.getStepLimit() + "");
 		ImmutableMap<PrototypeID, Integer> popLimits = se.getPopLimits();
 		int i = 0;
 		for (PrototypeID p : popLimits.keySet()) {
 			addCondition();
 			for (String s : agentNames) {
 				//this is pretty hackish: there should be a way to get the name
 				//from the ID
 				//TODO see if that can be added
 				if (sm.getFacade().getPrototype(s).getPrototypeID().equals(p)) {
 					agentTypes.get(i).setSelectedItem(s);
 				}
 			}
 			values.get(i).setText(popLimits.get(p) + "");
 		}
 		if (popLimits.size() == 0) {
 			addCondition();
 		}
 	}
 
 
 	private void addCondition() {
 		JPanel newPanel = new JPanel();
 		newPanel.setLayout(
 				new BoxLayout(newPanel, 
 						BoxLayout.X_AXIS)
 				);
 		JComboBox newBox = new JComboBox(agentNames);
 		newBox.setMaximumSize(new Dimension(300, 40));
 		agentTypes.add(newBox);
 		//		JComboBox newOps = new JComboBox(opNames);
 		//		newOps.setMaximumSize(new Dimension(200, 40));
 		//		operations.add(newOps);
 		JTextField newValue = new JTextField(25);
 		newValue.setMaximumSize(new Dimension(300, 40));
 		values.add(newValue);
 		JButton newButton = new JButton("Delete");
 		newButton.addActionListener(new DeleteListener());
 		deleteButtons.add(newButton);
 		newButton.setActionCommand(deleteButtons.indexOf(newButton) + "");
 		newPanel.add(newBox);
 		//newPanel.add(newOps);
 		newPanel.add(newValue);
 		newPanel.add(newButton);
 		subPanels.add(newPanel);
 		conListPanel.add(newPanel);
 		conListPanel.add(addConditionButton);
 		conListPanel.add(glue);
 		conListPanel.validate();
 		repaint();	
 	}
 
 	private void reset() {
 		nameField.setText("");
 		width.setText("");
 		height.setText("");
 		conListPanel.removeAll();
 		agentTypes.clear();
 		values.clear();
 		//operations.clear();
 		deleteButtons.clear();
 		subPanels.clear();
 	}
 
 	private class DeleteListener implements ActionListener {
 
		@Override
 		public void actionPerformed(ActionEvent e){
 			int n = Integer.parseInt(e.getActionCommand());
 			String str = (String) agentTypes.get(n).getSelectedItem();
 			sm.getEnder().removePopLimit(
 					sm.getFacade().getPrototype(str).getPrototypeID());
 			conListPanel.remove(subPanels.get(n));
 			agentTypes.remove(n);
 			values.remove(n);
 			//operations.remove(n);
 			deleteButtons.remove(n);
 			subPanels.remove(n);
 			conListPanel.validate();
 			repaint();
 		}
 
 	}
 }
