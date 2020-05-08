 package org.sergut.diceroller.ui;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.sergut.diceroller.DiceRoller;
 
 public class SimpleMontecarloFrame extends JFrame {
 
     private static final long serialVersionUID = 12872389749817789L;
     
     private static final int INITIAL_MAX_ROLLS = 1000000;
     private JLabel iterationsLabel = new JLabel("Rolls");
     private JTextField iterationsField = new JTextField("  " + INITIAL_MAX_ROLLS);
 
     private JButton calculateButton = new JButton("Calculate!");
 
     private JLabel diceLabel = new JLabel("Dice expression");
    private JTextField diceField = new JTextField("                     ");
    private JTextField targetField = new JTextField("    ");
 
     private String[] operators = {">=", ">", "<=", "<", "="};
 
     private JComboBox operatorCombobox = new JComboBox(operators);
     
     public SimpleMontecarloFrame() {
 	JPanel dicePane = new JPanel();
 	dicePane.setLayout(new FlowLayout());
 	dicePane.add(diceLabel);
 	dicePane.add(diceField);
 	dicePane.add(operatorCombobox);
 	dicePane.add(targetField);
 	
 	JPanel iterationsPane = new JPanel();
 	iterationsPane.setLayout(new FlowLayout());
 	iterationsPane.add(iterationsLabel);
 	iterationsPane.add(iterationsField);	
 
 	JPanel mainPane = new JPanel();
 	mainPane.setLayout(new GridLayout(0,1));
 	mainPane.add(dicePane);
 	mainPane.add(iterationsPane);
 	mainPane.add(calculateButton);
 
 	setButtonBehaviours();
 
 	this.add(mainPane);
 	this.pack();
 	this.setResizable(false);
 	this.setTitle("Simple dice simulator");
     }
     
     private void setButtonBehaviours() {
 	calculateButton.addActionListener(new ActionListener() {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		simulateDice();
 	    }});
     }
 
     private void simulateDice() {
 	try {
 	    String dice = diceField.getText().trim();
 	    int maxRolls = Integer.parseInt(iterationsField.getText().trim());
 	    DiceRoller diceRoller = new DiceRoller();
 	    String operator = (String) operatorCombobox.getSelectedItem();
 	    int target = Integer.parseInt(targetField.getText().trim());
 	    int success = 0;
 	    for (int i = 0; i < maxRolls; ++i) {
 		int result = diceRoller.rollDice(dice);
 		// FIXME: do this smartly with an enum
 		if (">".equals(operator)) {
 		    if (result > target) 
 			success++;
 		} else if (">=".equals(operator)) {
 		    if (result >= target) 
 			success++;
 		} else if ("=".equals(operator)) {
 		    if (result == target) 
 			success++;		    
 		} else if ("<=".equals(operator)) {
 		    if (result <= target) 
 			success++;
 		} else if ("<".equals(operator)) {
 		    if (result < target)  
 			success++;
 		} else {
 		    throw new IllegalStateException("Invalid operator: '" + operator + "'");
 		}
 	    }
 	    showResultsForExtra(success, maxRolls);
 	} catch (NumberFormatException ex) {
 	    String s = "There is an error with one of the numbers";
 	    ex.printStackTrace();
 	    JOptionPane.showMessageDialog(this, s, "Invalid number", JOptionPane.ERROR_MESSAGE);
 	}
     }
 
     private void showResultsForExtra(int success, int maxRolls) {
 	int ratio = DiceRoller.getSimpleRate(success, maxRolls);
 	String s = "Success ratio: " + ratio + "%";
 	JOptionPane.showMessageDialog(this, s, "Result", JOptionPane.INFORMATION_MESSAGE);
     }
     
 }
