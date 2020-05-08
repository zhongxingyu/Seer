 /**
  * EditEntityScreen
  * 
  * Class representing the screen that allows users to edit properties of
  * grid entities, including triggers and appearance.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.*;
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.simulation.GUIToAgentFacade;
 
 public class EditEntityScreen extends Screen {
 
 	private static final long serialVersionUID = 4021299442173260142L;
 
 	private Boolean editing;
 
 	private Prototype agent;
 
 	private JPanel cards;
 	//private JTabbedPane tabs;
 
 	private String currentCard;
 
 	private JPanel generalPanel;
 
 	private JTextField nameField;
 
 	private JColorChooser colorTool;
 
 	private ArrayList<JTextField> fieldNames;
 
 	private ArrayList<JTextField> fieldValues;
 
 	//private ArrayList<JComboBox> fieldTypes;
 
 	//private String[] typeNames = { "Integer", "Double", "String", "Boolean" };
 
 	private ArrayList<JButton> fieldDeleteButtons;
 
 	private ArrayList<JPanel> fieldSubPanels;
 
 	private JButton addFieldButton;
 
 	private JToggleButton[][] buttons;
 
 	private JPanel fieldListPanel;
 
 	private ArrayList<JTextField> triggerNames;
 
 	private ArrayList<JTextField> triggerPriorities;
 
 	private ArrayList<JTextField> triggerConditions;
 
 	private ArrayList<JTextField> triggerResults;
 
 	private ArrayList<JButton> triggerDeleteButtons;
 
 	private ArrayList<JPanel> triggerSubPanels;
 
 	private JButton addTriggerButton;
 
 	private JPanel triggerListPanel;
 
 	private HashSet<Integer> removedFields;
 
 	private HashSet<Integer> removedTriggers;
 
 	private JButton nextButton;
 
 	private JButton previousButton;
 
 	private JButton finishButton;
 
 	private GridBagConstraints c;
 

	//TODO addField/Trigger buttons don't work, some polishing to do on next/previous buttons
 	public EditEntityScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 
 		removedFields = new HashSet<Integer>();
 		removedTriggers = new HashSet<Integer>();
 
 		JLabel nameLabel = new JLabel("Agent Name: ");
 		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		nameField = new JTextField(25);
 		nameField.setMaximumSize(new Dimension(400, 40));
 
 		colorTool = GuiUtility.makeColorChooser();
 
 		buttons = new JToggleButton[7][7];
 
 		fieldNames = new ArrayList<JTextField>();
 		fieldValues = new ArrayList<JTextField>();
 
 		//fieldTypes = new ArrayList<JComboBox>();
 
 		fieldSubPanels = new ArrayList<JPanel>();
 		triggerSubPanels = new ArrayList<JPanel>();
 
 		triggerNames = new ArrayList<JTextField>();
 		triggerPriorities = new ArrayList<JTextField>();
 		triggerConditions = new ArrayList<JTextField>();
 		triggerResults = new ArrayList<JTextField>();
 
 		fieldDeleteButtons = new ArrayList<JButton>();
 		triggerDeleteButtons = new ArrayList<JButton>();
 
 		currentCard = "General";
 
 		fieldListPanel = GuiUtility.makePanel(BoxLayoutAxis.Y_AXIS,MaxSize.NULL,PrefSize.NULL);
 
 		triggerListPanel = GuiUtility.makePanel(BoxLayoutAxis.Y_AXIS,MaxSize.NULL,PrefSize.NULL);
 
 		cards = new JPanel(new CardLayout());
 
 		generalPanel = new JPanel(new GridBagLayout());
 		//generalPanel = GuiUtility.makePanel(BoxLayoutAxis.PAGE_AXIS,MaxSize.NULL,PrefSize.NULL);
 
 		final EditEntityScreen xThis = this;
 		addFieldButton = GuiUtility.makeButton("Add Field",new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				xThis.addField();
 			}
 		});
 
 		addTriggerButton = GuiUtility.makeButton("Add Trigger",new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				xThis.addTrigger();
 			}
 		});
 
 		JPanel iconPanel = new JPanel();
 		iconPanel.setLayout(new GridLayout(7, 7));
 		iconPanel.setMinimumSize(new Dimension(500, 500));
 		iconPanel.setAlignmentX(RIGHT_ALIGNMENT);
 
 		initIconDesignObject(iconPanel);
 
		//serialization not yet implemented
		//JButton loadIconButton = new JButton("Load icon");


 		JLabel generalLabel = GuiUtility.makeLabel("General Info",new PrefSize(300,80),HorizontalAlignment.CENTER);
 
 		//JPanel mainPanel = GuiUtility.makePanel(BoxLayoutAxis.X_AXIS,MaxSize.NULL,PrefSize.NULL);
 
 		JPanel colorPanel = GuiUtility.makeColorChooserPanel(colorTool);
 		Dimension maxSize = colorPanel.getMaximumSize();
 		maxSize.height += 50;
 		colorPanel.setMaximumSize(maxSize);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.PAGE_START;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 4;
 		generalPanel.add(generalLabel, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		generalPanel.add(nameLabel, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		generalPanel.add(nameField, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1.0;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		generalPanel.add(colorPanel, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1.0;
 		c.gridx = 2;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		generalPanel.add(iconPanel, c);
		//generalPanel.add(loadIconButton);
 
 		//JLabel fieldTypeLabel = new JLabel("Field Type");
 
 		addField();
 
 		// TODO make sure components line up
 
 		fieldSubPanels.get(0).setLayout(
 				new BoxLayout(fieldSubPanels.get(0), BoxLayout.X_AXIS));
 		fieldSubPanels.get(0).add(fieldNames.get(0));
 		//fieldSubPanels.get(0).add(fieldTypes.get(0));
 		fieldSubPanels.get(0).add(fieldValues.get(0));
 		fieldSubPanels.get(0).add(fieldDeleteButtons.get(0));
 
 		//fieldTypeLabel.setHorizontalAlignment(SwingConstants.LEFT);
 
 		//fieldLabelsPanel.add(fieldTypeLabel);
 
 		fieldListPanel.add(fieldSubPanels.get(0));
 		fieldListPanel.add(addFieldButton);
 		fieldListPanel.add(Box.createVerticalGlue());
 
 
 		// fieldSubPanels.get(0).setAlignmentY(TOP_ALIGNMENT);
 
 		addTrigger();
 
 		triggerSubPanels.get(0).setLayout(
 				new BoxLayout(triggerSubPanels.get(0), BoxLayout.X_AXIS));
 		triggerSubPanels.get(0).add(triggerNames.get(0));
 		triggerSubPanels.get(0).add(triggerPriorities.get(0));
 		triggerSubPanels.get(0).add(triggerConditions.get(0));
 		triggerSubPanels.get(0).add(triggerResults.get(0));
 		triggerSubPanels.get(0).add(triggerDeleteButtons.get(0));
 		triggerSubPanels.get(0).setAlignmentX(CENTER_ALIGNMENT);
 
 		triggerListPanel.add(triggerSubPanels.get(0));
 		triggerListPanel.add(addTriggerButton);
 		triggerListPanel.add(Box.createVerticalGlue());
 
 		cards.add(generalPanel, "General");
 		cards.add(makeFieldMainPanel(fieldListPanel), "Fields");
 		cards.add(makeTriggerMainPanel(triggerListPanel), "Triggers");
 
 		JPanel lowerPanel = new JPanel();
 
 		lowerPanel.add(GuiUtility.makeButton("Cancel",new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sm.update(sm.getScreen("Entities"));
 				xThis.reset();
 			}
 		}));
 
 		finishButton = GuiUtility.makeButton("Finish",new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (xThis.sendInfo()) {
 					sm.update(sm.getScreen("View Simulation"));
 					xThis.reset();
 				}
 			}
 		});
 		lowerPanel.add(finishButton);
 		previousButton = GuiUtility.makeButton("Previous",new PreviousListener());
 		lowerPanel.add(previousButton);
 		nextButton = GuiUtility.makeButton("Next",new NextListener());
 		lowerPanel.add(nextButton);
 
 		this.add(new JLabel("Edit Entities",SwingConstants.CENTER), BorderLayout.NORTH);
 		this.add(cards, BorderLayout.CENTER);
 		this.add(lowerPanel, BorderLayout.SOUTH);
 
 	}
 
 	private void initIconDesignObject(JPanel iconPanel){
 		//Creates the icon design object.
 		for (int i = 0; i < 7; i++) {
 			for (int j = 0; j < 7; j++) {
 				buttons[i][j] = new JToggleButton();
 				buttons[i][j].setOpaque(true);
 				buttons[i][j].setBackground(Color.WHITE);
 				buttons[i][j].setActionCommand(i + "" + j);
 				//When the button is pushed it switches colors.
 				buttons[i][j].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						String str = ae.getActionCommand();
 						JToggleButton jb = buttons[Integer.parseInt(str
 								.charAt(0) + "")][Integer.parseInt(str
 										.charAt(1) + "")];
 						if (jb.getBackground().equals(Color.WHITE)) {
 							jb.setBackground(Color.BLACK);
 							jb.setForeground(Color.BLACK);
 						}
 						else {
 							jb.setBackground(Color.WHITE);
 							jb.setForeground(Color.WHITE);
 						}
 					}
 				});
 				iconPanel.add(buttons[i][j]);
 			}
 		}
 	}
 
 	private static JPanel makeTriggerMainPanel(JPanel triggerListPanel){
 
 
 		JLabel triggerNameLabel = GuiUtility.makeLabel("Trigger Name",new PrefSize(130,30),HorizontalAlignment.LEFT);
 
 		JLabel triggerPriorityLabel = GuiUtility.makeLabel("Trigger Priority",new PrefSize(180,30),null);
 
 		JLabel triggerConditionLabel = GuiUtility.makeLabel("Trigger Condition",new PrefSize(300,30),null);
 
 		JLabel triggerResultLabel = GuiUtility.makeLabel("Trigger Result",new PrefSize(300,30),null);
 
 		JPanel triggerLabelsPanel = GuiUtility.makePanel(BoxLayoutAxis.X_AXIS,MaxSize.NULL,PrefSize.NULL);
 
 		triggerLabelsPanel.add(Box.createHorizontalGlue());
 		triggerLabelsPanel.add(triggerNameLabel);
 		triggerLabelsPanel.add(triggerPriorityLabel);
 		triggerLabelsPanel.add(triggerConditionLabel);
 		triggerLabelsPanel.add(triggerResultLabel);
 		triggerLabelsPanel.add(Box.createHorizontalGlue());
 		triggerLabelsPanel.setAlignmentX(CENTER_ALIGNMENT);
 
 
 		JLabel triggerLabel = GuiUtility.makeLabel("Trigger Info",new PrefSize(300,100),HorizontalAlignment.CENTER);
 
 		JPanel triggerBodyPanel = GuiUtility.makePanel(BoxLayoutAxis.Y_AXIS,MaxSize.NULL,PrefSize.NULL);
 		triggerBodyPanel.add(triggerLabelsPanel);
 		triggerBodyPanel.add(triggerListPanel);
 
 		JPanel triggerMainPanel = GuiUtility.makePanel(new BorderLayout(),MaxSize.NULL,PrefSize.NULL);
 
 		triggerMainPanel.add(triggerLabel, BorderLayout.NORTH);
 		triggerMainPanel.add(triggerBodyPanel, BorderLayout.CENTER);
 		return triggerMainPanel;
 	}
 
 	private static JPanel makeFieldMainPanel(JPanel fieldListPanel){
 
 		JPanel fieldMainPanel = GuiUtility.makePanel(new GridBagLayout(),MaxSize.NULL,PrefSize.NULL);
 
 
 		JLabel fieldLabel = GuiUtility.makeLabel("Field Info",new PrefSize(300,100),HorizontalAlignment.CENTER);
 		
 		JLabel fieldNameLabel = GuiUtility.makeLabel("Field Name",new PrefSize(350,30),HorizontalAlignment.LEFT);
 		fieldNameLabel.setAlignmentX(LEFT_ALIGNMENT);
 		JLabel fieldValueLabel = GuiUtility.makeLabel("Field Initial Value",new PrefSize(400,30),HorizontalAlignment.CENTER);
 		fieldValueLabel.setAlignmentX(LEFT_ALIGNMENT);
 
 		
 		GridBagConstraints constraint = new GridBagConstraints();
 		//constraint.fill = GridBagConstraints.HORIZONTAL;
 		constraint.gridx = 0;
 		constraint.gridy = 0;
 		constraint.gridwidth = 2;
 		fieldMainPanel.add(fieldLabel, constraint);
 		
 		constraint = new GridBagConstraints();
 		//constraint.fill = GridBagConstraints.HORIZONTAL;
 		constraint.gridx = 0;
 		constraint.gridy = 1;
 		fieldMainPanel.add(fieldNameLabel, constraint);
 
 		constraint = new GridBagConstraints();
 		//constraint.fill = GridBagConstraints.HORIZONTAL;
 		constraint.gridx = 1;
 		constraint.gridy = 1;
 		fieldMainPanel.add(fieldValueLabel, constraint);
 
 		constraint = new GridBagConstraints();
 		//constraint.fill = GridBagConstraints.HORIZONTAL;
 		constraint.gridx = 0;
 		constraint.gridy = 2;
 		constraint.gridwidth = 3;
 		constraint.weighty = 1.0;
 		constraint.anchor = GridBagConstraints.PAGE_START;
 		fieldMainPanel.add(fieldListPanel, constraint);
 		
 		return fieldMainPanel;
 	}
 
 	public void load(String str) {
 		reset();
 		sm.getFacade();
 		agent = GUIToAgentFacade.getPrototype(str);
 		nameField.setText(agent.getName());
 		colorTool.setColor(agent.getColor());
 
 		byte[] designBytes = agent.getDesign();
 		byte byter = Byte.parseByte("0000001", 2);
 		for (int i = 0; i < 7; i++) {
 			for (int j = 0; j < 7; j++) {
 				if ((designBytes[i] & (byter << j)) != Byte.parseByte("0000000", 2)) {
 					buttons[i][6-j].doClick();
 				}
 			}
 		}
 
 		Map<String, String> fields = agent.getCustomFieldMap();
 		int i = 0;
 		for (String s : fields.keySet()) {
 			addField();
 			fieldNames.get(i).setText(s);
 			fieldValues.get(i).setText(fields.get(s));
 			i++;
 		}
 		List<Trigger> triggers = agent.getTriggers();
 		int j = 0;
 		for (Trigger t : triggers) {
 			addTrigger();
 			triggerNames.get(j).setText(t.getName());
 			triggerConditions.get(j).setText(t.getConditions().toString());
 			triggerResults.get(j).setText(t.getBehavior().toString());
 			triggerPriorities.get(j).setText(t.getPriority() +"");
 			j++;
 		}
 	}
 
 	public void reset() {
 		agent = null;
 		currentCard = "General";
 		((CardLayout)cards.getLayout()).first(cards);
 		nameField.setText("");
 		colorTool.setColor(Color.WHITE);
 		for (int x = 0; x < 7; x++) {
 			for (int y = 0; y < 7; y++) {
 				buttons[x][y].setSelected(false);
 				buttons[x][y].setBackground(Color.WHITE);
 			}
 		}
 		fieldNames.clear();
 		//fieldTypes.clear();
 		fieldValues.clear();
 		fieldDeleteButtons.clear();
 		fieldSubPanels.clear();
 		removedFields.clear();
 		fieldListPanel.removeAll();
 		fieldListPanel.add(addFieldButton);
 		triggerNames.clear();
 		triggerPriorities.clear();
 		triggerConditions.clear();
 		triggerResults.clear();
 		triggerDeleteButtons.clear();
 		triggerSubPanels.clear();
 		removedTriggers.clear();
 		triggerListPanel.removeAll();
 		triggerListPanel.add(addTriggerButton);
 		previousButton.setEnabled(false);
 		nextButton.setEnabled(true);
 		finishButton.setEnabled(false);
 	}
 
 	public boolean sendInfo() {
 		sendGeneralInfo();
 		sendFieldInfo();
 		return sendTriggerInfo();
 	}
 
 	public void sendGeneralInfo(){
 		if (!editing) {
 			sm.getFacade();
 			GUIToAgentFacade.createPrototype(nameField.getText(),
 					sm.getFacade().getGrid(), colorTool.getColor(),	generateBytes());
 			sm.getFacade();
 			agent = GUIToAgentFacade.getPrototype(nameField.getText());
 		}
 		else {
 			agent.setPrototypeName(agent.getName(),
 					nameField.getText());
 			agent.setColor(colorTool.getColor());
 			agent.setDesign(generateBytes());
 			Prototype.addPrototype(agent);
 		}
 	}
 
 	public void sendFieldInfo(){
 		try{
 			for (int i = 0; i < fieldNames.size(); i++) {
 				if (!removedFields.contains(i)) {
 					if (fieldNames.get(i).getText().equals("")
 							|| fieldValues.get(i).getText().equals("")) {
 						throw new Exception("All fields must have input");
 					}
 				}
 			}
 
 			for (int i = 0; i < fieldNames.size(); i++) {
 				if (removedFields.contains(i)) {
 					if (agent.hasField(fieldNames.get(i).getText()))
 						agent.removeField(fieldNames.get(i).toString());
 				} else {
 					if (agent.hasField(fieldNames.get(i).getText())) {
 						agent.updateField(fieldNames.get(i).getText(),
 								fieldValues.get(i).getText());
 					} else
 						try {
 							agent.addField(fieldNames.get(i).getText(),
 									fieldValues.get(i).getText());
 						} catch (ElementAlreadyContainedException e) {
 							e.printStackTrace();
 						}
 				}
 			}
 		}
 		catch (Exception e) {
 			JOptionPane.showMessageDialog(null, e.getMessage());
 		} 
 	}
 
 	public boolean sendTriggerInfo(){
 		boolean toReturn = false;
 		try{
 			for (int j = 0; j < triggerNames.size(); j++) {
 				if (!removedTriggers.contains(j)) {
 					if (triggerNames.get(j).getText().equals("")
 							|| triggerConditions.get(j).getText().equals("")
 							|| triggerResults.get(j).getText().equals("")) {
 						throw new Exception("All fields must have input");
 					}
 				}
 				if (Integer.parseInt(triggerPriorities.get(j).getText()) < 0) {
 					throw new Exception("Priority must be greater than 0");
 				}
 			}
 
 			for (int i = 0; i < triggerNames.size(); i++) {
 				if (removedTriggers.contains(i)) {
 					if (agent.hasTrigger(triggerNames.get(i).getText()))
 						agent.removeTrigger(triggerNames.get(i).getText());
 				} else {
 					if (agent.hasTrigger(triggerNames.get(i).getText()))
 						agent.updateTrigger(triggerNames.get(i).getText(),
 								generateTrigger(i));
 					else
 						agent.addTrigger(generateTrigger(i));
 				}
 			}
 			toReturn = true;
 		}
 		catch (NumberFormatException e) {
 			JOptionPane.showMessageDialog(null,
 					"Priorities field must be an integer greater than 0.");
 			e.printStackTrace();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, e.getMessage());
 		} 
 
 		return toReturn;
 	}
 
 	public void setEditing(Boolean b) {
 		editing = b;
 	}
 
 	private void addField() {
 		JPanel newPanel = GuiUtility.makePanel(BoxLayoutAxis.X_AXIS,null,null);
 		JTextField newName = GuiUtility.makeTextField(null,25,new MaxSize(300,40),null);
 		fieldNames.add(newName);
 		//		JComboBox newType = new JComboBox(typeNames);
 		//		newType.setMaximumSize(new Dimension(200, 40));
 		//		fieldTypes.add(newType);
 		JTextField newValue = GuiUtility.makeTextField(null,25,new MaxSize(300,40),null);
 		fieldValues.add(newValue);
 		JButton newButton = GuiUtility.makeButton("Delete",new DeleteFieldListener());
 		fieldDeleteButtons.add(newButton);
 		newButton.setActionCommand(fieldDeleteButtons.indexOf(newButton) + "");
 		newPanel.add(newName);
 		//		newPanel.add(newType);
 		newPanel.add(newValue);
 		newPanel.add(newButton);
 		fieldSubPanels.add(newPanel);
 		fieldListPanel.add(newPanel);
 		fieldListPanel.add(addFieldButton);
 		fieldListPanel.add(Box.createVerticalGlue());
 		validate();
 		repaint();
 	}
 
 	private void addTrigger() {
 		JPanel newPanel = GuiUtility.makePanel(BoxLayoutAxis.X_AXIS,null,null);
 		JTextField newName = GuiUtility.makeTextField(null,25,new MaxSize(200,40),null);
 		triggerNames.add(newName);
 		JTextField newPriority = GuiUtility.makeTextField(null,15,new MaxSize(150,40),null);
 		triggerPriorities.add(newPriority);
 		JTextField newCondition = GuiUtility.makeTextField(null,50,new MaxSize(300,40),null);
 		triggerConditions.add(newCondition);
 		JTextField newResult = GuiUtility.makeTextField(null,50,new MaxSize(300,40),null);
 		triggerResults.add(newResult);
 		JButton newButton = GuiUtility.makeButton("Delete",new DeleteTriggerListener());
 		triggerDeleteButtons.add(newButton);
 		newButton.setActionCommand(triggerDeleteButtons.indexOf(newButton)
 				+ "");
 		newPanel.add(newName);
 		newPanel.add(newPriority);
 		newPanel.add(newCondition);
 		newPanel.add(newResult);
 		newPanel.add(newButton);
 		triggerSubPanels.add(newPanel);
 		triggerListPanel.add(newPanel);
 		triggerListPanel.add(addTriggerButton);
 		triggerListPanel.add(Box.createVerticalGlue());
 		repaint();
 	}
 
 	private byte[] generateBytes() {
 		String str = "";
 		byte[] toReturn = new byte[7];
 		for (int column = 0; column < 7; column++) {
 			for (int row = 0; row < 7; row++) {
 				if (buttons[column][row].getBackground().equals(Color.BLACK)) {
 					str += "1";
 				} else {
 					str += "0";
 				}
 			}
 			str += ":";
 		}
 		str = str.substring(0, str.lastIndexOf(':'));
 		String[] byteStr = str.split(":");
 		for (int i = 0; i < 7; i++) {
 			toReturn[i] = Byte.parseByte(byteStr[i], 2);
 		}
 
 		return toReturn;
 	}
 
 	private Trigger generateTrigger(int i) {
 		return new Trigger(triggerNames.get(i).getText(),
 				Integer.parseInt(triggerPriorities.get(i).getText()),
 				new Expression(triggerConditions.get(i).getText()),
 				new Expression(triggerResults.get(i).getText()));
 	}
 
 	private class DeleteFieldListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			removedFields.add(Integer.parseInt(e.getActionCommand()));
 			fieldListPanel.remove(fieldSubPanels.get(Integer.parseInt(e
 					.getActionCommand())));
 			validate();
 			repaint();
 		}
 	}
 
 	private class DeleteTriggerListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			removedTriggers.add(Integer.parseInt(e.getActionCommand()));
 			triggerListPanel.remove(triggerSubPanels.get(Integer.parseInt(
 					e.getActionCommand()))
 					);
 			validate();
 			repaint();
 		}
 	}
 
 	private class NextListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			CardLayout c1 = (CardLayout)cards.getLayout();
 			if (currentCard == "General"){
 				sendGeneralInfo();
 				previousButton.setEnabled(true);
 				c1.next(cards);
 				currentCard = "Fields";
 			}
 			else if (currentCard == "Fields"){
 				sendFieldInfo();
 				c1.next(cards);
 				nextButton.setEnabled(false);
 				finishButton.setEnabled(true);
 				currentCard = "Triggers";
 			}
 		}
 	}
 
 	private class PreviousListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			CardLayout c1 = (CardLayout)cards.getLayout();
 			if (currentCard == "Fields"){
 				//sendFieldInfo();
 				previousButton.setEnabled(false);
 				c1.previous(cards);
 				currentCard = "General";
 			}
 			else if (currentCard == "Triggers"){
 				//sendTriggerInfo();
 				c1.previous(cards);
 				nextButton.setEnabled(true);
 				currentCard = "Fields";
 			}
 		}
 	}
 
 	@Override
 	public void load() {
 		reset();
 		addField();
 		addTrigger();
 	}
 
 }
