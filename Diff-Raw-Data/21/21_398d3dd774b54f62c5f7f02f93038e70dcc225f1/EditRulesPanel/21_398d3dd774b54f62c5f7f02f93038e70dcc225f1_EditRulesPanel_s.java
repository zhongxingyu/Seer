 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 
 public class EditRulesPanel extends JPanel {
 
     private Player player;
 
     private JLabel editRulesLabel;
     
     private StrategyPanel strategyPanel;
 
     private NewRulePanel newRulePanel;
 
     private DeleteRulePanel deleteRulePanel;
     
     public EditRulesPanel(Player player) {
 	super();
 	this.player = player;
 
 	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 	editRulesLabel = new JLabel();
 	editRulesLabel.setFont(Config.LABEL_FONT);
 	editRulesLabel.setText(player.getID() + " Player's Rules");
 	editRulesLabel.setAlignmentY(Component.TOP_ALIGNMENT);
 	editRulesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 	add(editRulesLabel);
 
 	strategyPanel = new StrategyPanel();
 	strategyPanel.setRuleText(player.getRuleString());
 	add(strategyPanel);
 	
 	newRulePanel = new NewRulePanel();
 	add(newRulePanel);
 
 	add(Box.createVerticalStrut(15));
 	deleteRulePanel = new DeleteRulePanel();
 	add(deleteRulePanel);
     }
 
     public void updateStrategyPanel() {
 	strategyPanel.setRuleText(player.getRuleString());
     }
 
     /**
      * Internal class - New Rule Panel
      *
      * Holds the configuration panels and buttons for adding new rules
      */
 
     private class NewRulePanel extends JPanel implements ActionListener {
 
 	private JPanel addRuleButtonsPanel;
 	private JButton addRuleOneB;
 	private JButton addRuleTwoB;
 
 	private JPanel ruleFormatsPanel;
     
 	private RuleFormatOnePanel rfOnePanel;
 
 	private RuleFormatTwoPanel rfTwoPanel;
 
 	public NewRulePanel() {
 	    super();
 
 	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 	    rfOnePanel = new RuleFormatOnePanel();
 	    rfOnePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 	    add(rfOnePanel);
 
 	    add(Box.createVerticalStrut(5));
 	    addRuleOneB = new JButton("Add");
 	    addRuleOneB.addActionListener(this);
 	    add(addRuleOneB);
 
 	    add(Box.createVerticalStrut(15));
 	    rfTwoPanel = new RuleFormatTwoPanel();
 	    rfTwoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 	    add(rfTwoPanel);
 
 	    add(Box.createVerticalStrut(5));
 	    addRuleTwoB = new JButton("Add");
 	    addRuleTwoB.addActionListener(this);
 	    add(addRuleTwoB);
 	}
 
 	/**
 	 * Handle a press of either "Add" button
 	 */
 	public void actionPerformed(ActionEvent e) {
 	    if(e.getSource() == addRuleOneB) {
 		if(!rfOnePanel.validatePastNumMoves()) {
 		    JOptionPane.showMessageDialog(getParent(), "Please enter past number of moves, or mark the \"all\" checkbox");
 		} else if (!rfOnePanel.validateFutureNumMoves()) {
 		    JOptionPane.showMessageDialog(getParent(), "Please enter future number of moves, or mark the \"all\" checkbox");
 		} else {
 		    player.createRule0(rfOnePanel.getRuleString());
 		    updateStrategyPanel();
 		    /**
 		    if(player.createRule0(rfOnePanel.getRuleString())) {
 			updateStrategyPanel();
 		    } else { //The rule set is already full
 			JOptionPane.showMessageDialog(getParent(), player.getID() + " Player's Ruleset is full");
 		    }
 		    */
 		}
 	    }
 	    if(e.getSource() == addRuleTwoB) {
 		player.createRule1(rfTwoPanel.getRuleString());
 		updateStrategyPanel();
 	    }
 	}
 
 	/**
 	 * Allows configuration of the long-format rule our agents use
 	 *
 	 * Contains a number of drop-down menus and NumericTextFields which
 	 * are the variables that make up the rule
 	 */
 
 	private class RuleFormatOnePanel extends JPanel {
 
 	    private JComboBox<Integer> priorityNumBox;
 	    private JComboBox<String> ifUntilBox;
 	    private JComboBox<String> playerBox;
 	    private JComboBox<String> pastMoveTypeBox;
 	    private JComboBox<String> atLeastUnderBox;
 	    private JComboBox<String> pastMovePercentBox;
 	    
 	    private JCheckBox allPastMovesCB;
 	    private NumericTextField numPastMoves;
 
 	    private JComboBox<String> futureMoveTypeBox;
 	    
 	    private JCheckBox allFutureMovesCB;
 	    private NumericTextField numFutureMoves;
 
 	    private JLabel moveIsLabel = new JLabel("move is");
 	    private JLabel ofThePastLabel = new JLabel("of the past");
 	    private JLabel movesThenLabel = new JLabel("move(s) then queue");
 	    private JLabel forLabel = new JLabel("for");
 	    private JLabel futureMovesLabel = new JLabel("future move(s)");
 
 	    public RuleFormatOnePanel() {
 		super();
 	    
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		Integer[] priorityNumList = {1,2,3,4,5,6,7,8,9,10};
 		String[] ifUntilList = {"If", "Until"};
 		String[] playerList = {"Your", "Opp."};
 		String[] atLeastUnderList = {"At least", "Under"};
 		String[] percentageList = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
 		JPanel priorityPanel = new JPanel();
 		priorityPanel.setLayout(new FlowLayout());
 		JLabel priorityLabel = new JLabel("Priority");
 		priorityPanel.add(priorityLabel);
 		priorityNumBox = new JComboBox<Integer>(priorityNumList);
 		priorityPanel.add(priorityNumBox);
 
 		JPanel panelOne = new JPanel();
 		panelOne.setLayout(new FlowLayout());
 		ifUntilBox = new JComboBox<String>(ifUntilList);
 		panelOne.add(ifUntilBox);
 		playerBox = new JComboBox<String>(playerList);
 		panelOne.add(playerBox);
 		panelOne.add(moveIsLabel);
 		
 		JPanel panelTwo = new JPanel();
 		panelTwo.setLayout(new FlowLayout());
 		pastMoveTypeBox = new JComboBox<String>(Config.MOVE_LIST);
 		panelTwo.add(pastMoveTypeBox);
 		atLeastUnderBox = new JComboBox<String>(atLeastUnderList);
 		panelTwo.add(atLeastUnderBox);
 		pastMovePercentBox = new JComboBox<String>(percentageList);
 		panelTwo.add(pastMovePercentBox);
 		panelTwo.add(new JLabel("%"));
 
 		JPanel panelThree = new JPanel();
 		panelThree.add(ofThePastLabel);
 		panelThree.setLayout(new FlowLayout());
 		numPastMoves = new NumericTextField(3);
 		panelThree.add(numPastMoves);
 		panelThree.add(new JLabel("OR"));
 		allPastMovesCB = new JCheckBox("all");
 		panelThree.add(allPastMovesCB);
 
 		JPanel panelFour = new JPanel();
 		panelFour.add(movesThenLabel);
 		futureMoveTypeBox = new JComboBox<String>(Config.MOVE_LIST);
 		panelFour.add(futureMoveTypeBox);
 
 		JPanel panelFive = new JPanel();
 		panelFive.add(forLabel);
 		panelFive.setLayout(new FlowLayout());
 		numFutureMoves = new NumericTextField(3);
 		panelFive.add(numFutureMoves);
 		panelFive.add(new JLabel("OR"));
 		allFutureMovesCB = new JCheckBox("all");
 		panelFive.add(allFutureMovesCB);
 		panelFive.add(futureMovesLabel);
 
 		add(priorityPanel);
 		add(Box.createVerticalGlue());
 		add(panelOne);
 		add(Box.createVerticalGlue());
 		add(panelTwo);
 		add(Box.createVerticalGlue());
 		add(panelThree);
 		add(Box.createVerticalGlue());
 		add(panelFour);
 		add(Box.createVerticalGlue());
 		add(panelFive);
 	    }
 
 	    public boolean validatePastNumMoves() {
 		if(!allPastMovesCB.isSelected()) {
 		    if(numPastMoves.getText().equals("")) {
 			return false;
 		    }
 		    return true;
 		}
 		return true;
 	    }
 
 	    public boolean validateFutureNumMoves() {
 		if(!allFutureMovesCB.isSelected()) {
 		    if(numFutureMoves.getText().equals("")) {
 			return false;
 		    }
 		    return true;
 		}
 		return true;
 	    }
 
 	    /**
 	     * Returns the variables which define rule type 1, with each variable delimited
 	     * by the '|' character
 	     */
 
 	    public String getRuleString() {
 		return((int)priorityNumBox.getSelectedItem() + "|" +
 		       (String)ifUntilBox.getSelectedItem() + "|" +
 		       (String)playerBox.getSelectedItem() + "|" + 
 		       (String)pastMoveTypeBox.getSelectedItem() + "|" +
 		       (String)atLeastUnderBox.getSelectedItem() + "|" +
 		       (String)pastMovePercentBox.getSelectedItem() + "|" +
 		       (allPastMovesCB.isSelected() ? "-1" : numPastMoves.getText()) + "|" +
 		       (String)futureMoveTypeBox.getSelectedItem() + "|" +
 		       (allFutureMovesCB.isSelected() ? "-1" : numFutureMoves.getText()));
 	    }
 	}
 
 	/**
 	 * Format for the short-form rule (always [move-type])
 	 */
 	
 	private class RuleFormatTwoPanel extends JPanel{
 	
 	    private JComboBox<Integer> priorityNumBox;
 
 	    private JComboBox<String> moveTypeBox;
 
 	    public RuleFormatTwoPanel() {
 		super();
 
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 	    
 		Integer[] priorityNumList = {1,2,3,4,5,6,7,8,9,10};
 		JPanel priorityPanel = new JPanel();
 		priorityPanel.setLayout(new FlowLayout());
 		JLabel priorityLabel = new JLabel("Priority");
 		priorityPanel.add(priorityLabel);
 		priorityNumBox = new JComboBox<Integer>(priorityNumList);
 		priorityPanel.add(priorityNumBox);
 	    
 		add(priorityPanel);
 	    
 		JPanel alwaysMovePanel = new JPanel();
 		alwaysMovePanel.setLayout(new FlowLayout());
 		JLabel alwaysLabel = new JLabel("Always");
 		alwaysMovePanel.add(alwaysLabel);
 		moveTypeBox = new JComboBox<String>(Config.MOVE_LIST);
 		alwaysMovePanel.add(moveTypeBox);
 	    
 		add(alwaysMovePanel);
 	    }
 	
 	    public String getRuleString() {
 		return((int)priorityNumBox.getSelectedItem() + "|" +
 		       (String)moveTypeBox.getSelectedItem());
 	    }
 	}
     }
 
     /**
      * Internal class - Delete Rule Panel
      *
      * Numeric Text Field for the number of the rule to be deleted, and delete button
      */
 
     private class DeleteRulePanel extends JPanel implements ActionListener{
 
 	private JLabel deleteLabel;
 	private NumericTextField deleteIndex;
 
 	private JButton deleteButton;
 
 	public DeleteRulePanel() {
 	    super();
 
 	    setLayout(new FlowLayout());
 	    setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 	    deleteLabel = new JLabel("Delete Rule Number");
 	    add(deleteLabel);
 	
 	    deleteIndex = new NumericTextField(2);
 	    add(deleteIndex);
 	
 	    add(Box.createHorizontalStrut(10));
 	    deleteButton = new JButton("Delete");
 	    deleteButton.addActionListener(this);
 	    add(deleteButton);
 	}
 	/**
 	 * Returns the index of the rule to be deleted
 	 *
 	 * Must subtract one from the user-inputted index to match the actual rule index
 	 */
 	private int ruleIndexToDelete() {
 	    return Integer.parseInt(deleteIndex.getText()) - 1;
 	}
 
 	/**
 	 * Handle a press of the delete button
 	 */
 	public void actionPerformed(ActionEvent e) {
 	    if(e.getSource() == deleteButton) {
 		if(deleteIndex.getText().equals("")) {
 		    JOptionPane.showMessageDialog(getParent(), "Please enter the number of the rule to be deleted");
 		} else {
 		    player.removeRule(ruleIndexToDelete());
 		    updateStrategyPanel();
 		    //} else { //Deletion was unsuccessful, the index was out of range
 		//	JOptionPane.showMessageDialog(getParent(), "This rule number is not present in the rule list");
 		    //  }
 		}
 	    }
 	}
 
     }
 }
