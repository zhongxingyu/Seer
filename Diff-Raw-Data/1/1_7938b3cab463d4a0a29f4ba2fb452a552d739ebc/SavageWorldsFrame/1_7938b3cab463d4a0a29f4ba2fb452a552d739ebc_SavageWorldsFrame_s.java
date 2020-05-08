 package org.sergut.diceroller.ui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import org.sergut.diceroller.DiceRoller;
 import org.sergut.diceroller.IllegalDiceExpressionException;
 
 public class SavageWorldsFrame extends JFrame {
 
     private static final long serialVersionUID = 12872389749812789L;
     
     private static final int FIELD_LENGTH = 4;
     
     // TODO: develop a general reusable way of creating labels and fields for dice
     //   based on a text description, maybe partially based on roll20 syntax
     
     private final String[] attackDice = {"d4", "d6", "d8", "d10", "d12"};
 
     private JComboBox attackDiceCombobox = new JComboBox(attackDice);
 
     private JLabel attackD4Label = new JLabel(" d4");
     private JLabel attackD6Label = new JLabel(" d6");
     private JLabel attackD8Label = new JLabel(" d8");
     private JLabel attackD10Label = new JLabel("d10");
     private JLabel attackD12Label = new JLabel("d12");
 
     private JTextField attackD4Field = new JTextField(FIELD_LENGTH);
     private JTextField attackD6Field = new JTextField(FIELD_LENGTH);
     private JTextField attackD8Field = new JTextField(FIELD_LENGTH);
     private JTextField attackD10Field = new JTextField(FIELD_LENGTH);
     private JTextField attackD12Field = new JTextField(FIELD_LENGTH);
 
     private JLabel damageD4Label = new JLabel(" d4");
     private JLabel damageD6Label = new JLabel(" d6");
     private JLabel damageD8Label = new JLabel(" d8");
     private JLabel damageD10Label = new JLabel("d10");
     private JLabel damageD12Label = new JLabel("d12");
 
     private JTextField damageD4Field = new JTextField(FIELD_LENGTH);
     private JTextField damageD6Field = new JTextField(FIELD_LENGTH);
     private JTextField damageD8Field = new JTextField(FIELD_LENGTH);
     private JTextField damageD10Field = new JTextField(FIELD_LENGTH);
     private JTextField damageD12Field = new JTextField(FIELD_LENGTH);
 
     private JLabel parryLabel = new JLabel("Parry / Diff.");
     private JTextField parryField = new JTextField(FIELD_LENGTH);
     private JLabel toughnessLabel = new JLabel("Toughness");
     private JTextField toughnessField = new JTextField(FIELD_LENGTH);
 
     private static final int INITIAL_MAX_ROLLS = 1000000;
     private JLabel iterationsLabel = new JLabel("Rolls");
     private JTextField iterationsField = new JTextField("  " + INITIAL_MAX_ROLLS);
 
     private JButton calculateButton = new JButton("Calculate!");
 
     private boolean isWildCard = false;
     
     public SavageWorldsFrame() {
 	setButtonBehaviours();
 	JPanel westPane = getWestPanel();
 	JPanel eastPane = getEastPanel();	
 	JPanel northPane = new JPanel();	
 	northPane.setLayout(new GridLayout(1,0));
 	northPane.add(westPane);
 	northPane.add(eastPane);
 	JPanel southPane = getSouthPanel();	
 	this.setLayout(new GridLayout(0,1));
 	this.add(northPane);
 	this.pack();
 	this.add(southPane);
 	this.pack();
 	this.setTitle("Savage Worlds: Kill chances");
 	this.setLocation(100, 100);
 	//this.setResizable(false);
     }
     
     private JPanel getEastPanel() {
 	JPanel result = new JPanel();
 	JPanel damagePane = new JPanel();
 	damagePane.setLayout(new GridLayout(0,1));
 	damagePane.add(new JLabel("Damage dice"));
 	damagePane.add(packLabelAndTextField(damageD4Label, damageD4Field), BorderLayout.CENTER);
 	damagePane.add(packLabelAndTextField(damageD6Label, damageD6Field), BorderLayout.CENTER);
 	damagePane.add(packLabelAndTextField(damageD8Label, damageD8Field), BorderLayout.CENTER);
 	damagePane.add(packLabelAndTextField(damageD10Label, damageD10Field), BorderLayout.CENTER);
 	damagePane.add(packLabelAndTextField(damageD12Label, damageD12Field), BorderLayout.CENTER);
 	JPanel enemyPane = new JPanel();
 	enemyPane.setLayout(new GridLayout(0,1));
 	JPanel parryPane = new JPanel();
 	parryPane.setLayout(new FlowLayout());
 	parryPane.add(parryLabel);
 	parryPane.add(parryField);
 	enemyPane.add(parryPane);
 	JPanel toughnessPane = new JPanel();
 	toughnessPane.setLayout(new FlowLayout());
 	toughnessPane.add(toughnessLabel);
 	toughnessPane.add(toughnessField);
 	enemyPane.add(toughnessPane);
 	enemyPane.add(getWildCardRadioButtons());
 	result.setLayout(new GridLayout(0,1));
 	result.add(damagePane);
 	result.add(enemyPane);
 	return result;
     }
     
     private JPanel getWestPanel() {
 	JPanel result = new JPanel();
 	result.add(attackDiceCombobox);
 	// TODO: all the options of wild attack, double attack, etc
 //	JPanel attackPane = new JPanel();
 //	attackPane.setLayout(new GridLayout(0,1));
 //	attackPane.add(new JLabel("Attack dice"));
 //	attackPane.add(packLabelAndTextField(attackD4Label, attackD4Field), BorderLayout.CENTER);
 //	attackPane.add(packLabelAndTextField(attackD6Label, attackD6Field), BorderLayout.CENTER);
 //	attackPane.add(packLabelAndTextField(attackD8Label, attackD8Field), BorderLayout.CENTER);
 //	attackPane.add(packLabelAndTextField(attackD10Label, attackD10Field), BorderLayout.CENTER);
 //	attackPane.add(packLabelAndTextField(attackD12Label, attackD12Field), BorderLayout.CENTER);
 //	result.setLayout(new GridLayout(0,2));
 //	result.add(attackPane);
 	return result;
     }
 
     private JPanel getSouthPanel() {
 	JPanel result = new JPanel();
 	result.setLayout(new GridLayout(0,1));
 	JPanel iterationsPane = new JPanel();
 	iterationsPane.setLayout(new FlowLayout());
 	iterationsPane.add(iterationsLabel);
 	iterationsPane.add(iterationsField);
 	result.add(iterationsPane);
 	result.add(calculateButton);
 	return result;
     }
 
     private JPanel packLabelAndTextField(JLabel label, JTextField field) {
 	JPanel result = new JPanel();
 	result.setLayout(new FlowLayout());
 	result.add(label);
 	result.add(field);
 	return result;
     }
 
     private JPanel getWildCardRadioButtons() {
 	JPanel resultPane = new JPanel();
 	resultPane.setLayout(new FlowLayout());
 	ButtonGroup buttonGroup = new ButtonGroup();
 	JRadioButton extraButton = new JRadioButton("Extra", true);
 	buttonGroup.add(extraButton);
 	resultPane.add(extraButton);
 	extraButton.addActionListener(new ActionListener() {
 	    @Override public void actionPerformed(ActionEvent e) {
 		isWildCard = false;
 	    }});
 	JRadioButton wildCardButton = new JRadioButton("Wild Card", false);
 	buttonGroup.add(wildCardButton);
 	resultPane.add(wildCardButton);
 	wildCardButton.addActionListener(new ActionListener() {
 	    @Override public void actionPerformed(ActionEvent e) {
 		isWildCard = true;
 	    }});
 	return resultPane;
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
 	    String attackDice = collectAttackDice();
 	    String damageDice = collectDamageDice();
 	    System.out.println(attackDice + "   " + damageDice);
 	    int maxRolls = getMaxIterations();
 	    int parry = getParry();
 	    int toughness = getToughness();
 	    DiceRoller diceRoller = new DiceRoller();
 	    DamageCounter damageCounter = new DamageCounter();
 	    for (int i = 0; i < maxRolls; ++i) {
 		String damageDiceCopy = new String(damageDice);
 		int attack = diceRoller.rollDice(attackDice);
 		if (attack >= parry + 4) {
 		    damageDiceCopy += "+1d6!";
 		} else if (attack < parry) {
 		    damageDiceCopy = "0";
 		}
 		int damage = diceRoller.rollDice(damageDiceCopy);
 		int success = damage - toughness;
 		if (success >= 16) {
 		    damageCounter.wound4m++;
 		} else if (success >= 12) {
 		    damageCounter.wound3++;
 		} else if (success >= 8) {
 		    damageCounter.wound2++;
 		} else if (success >= 4) {
 		    damageCounter.wound1++;
 		} else if (success >= 0) {
 		    damageCounter.shaken++;
 		} else {
 		   damageCounter.nothing++; 
 		}
 	    }
 	    if (isWildCard) 
 		showResultsForWildCard(damageCounter, maxRolls);
 	    else
 		showResultsForExtra(damageCounter, maxRolls);
 	} catch (IllegalDiceExpressionException ex) {
 	    String s = "Invalid expression: " + ex.getExpression();
 	    ex.printStackTrace();
 	    JOptionPane.showMessageDialog(this, s, "Invalid expression", JOptionPane.ERROR_MESSAGE);
 	}
     }
 
     private void showResultsForWildCard(DamageCounter damageCounter, int maxRolls) {
 	int wound1Ratio = DiceRoller.getSimpleRate(damageCounter.wound1, maxRolls);
 	int wound2Ratio = DiceRoller.getSimpleRate(damageCounter.wound2, maxRolls);
 	int wound3Ratio = DiceRoller.getSimpleRate(damageCounter.wound3, maxRolls);
 	int wound4Ratio = DiceRoller.getSimpleRate(damageCounter.wound4m, maxRolls);
 	int shakenRatio = DiceRoller.getSimpleRate(damageCounter.shaken, maxRolls);
 	String s = "Shaken  ratio: " + shakenRatio + "% \n"
 	    	+  "1 wound  ratio: " + wound1Ratio + "% \n" 
 	    	+  "2 wounds ratio: " + wound2Ratio + "% \n" 
 	    	+  "3 wounds ratio: " + wound3Ratio + "% \n" 
 	    	+  "4+ wounds ratio: " + wound4Ratio + "% "; 
 	System.out.println(damageCounter);
 	JOptionPane.showMessageDialog(this, s, "Result", JOptionPane.INFORMATION_MESSAGE);
     }
 
     private void showResultsForExtra(DamageCounter damageCounter, int maxRolls) {
 	int killRatio = DiceRoller.getSimpleRate(damageCounter.getWounds(), maxRolls);
 	int shakenRatio = DiceRoller.getSimpleRate(damageCounter.shaken, maxRolls);
 	String s = "Kill ratio: " + killRatio + "%  Shaken ratio: " + shakenRatio + "%";
 	System.out.println(damageCounter);
 	JOptionPane.showMessageDialog(this, s, "Result", JOptionPane.INFORMATION_MESSAGE);
     }
 
     private int getParry() {
 	int result = parseTextFieldAsInteger(parryField);
 	return result;
     }
 
     private int getToughness() {
 	int result = parseTextFieldAsInteger(toughnessField);
 	return result;
     }
 
     private int getMaxIterations() {
 	int result = parseTextFieldAsInteger(iterationsField);
 	return result;
     }
 
     private String collectAttackDice() {
 	String result = "b[" + (String) attackDiceCombobox.getSelectedItem() + "!";
 	// TODO: add ,1d6! if wildcard
	result = result.substring(0, result.length()-1); // Remove trailing ","
 	return result + "]";
     }
 
     private String collectDamageDice() {
 	String result = "";
 	int n;
 	n = parseTextFieldAsInteger(damageD4Field);
 	result += n + "d4!+";
 	n = parseTextFieldAsInteger(damageD6Field);
 	result += n + "d6!+";
 	n = parseTextFieldAsInteger(damageD8Field);
 	result += n + "d8!+";
 	n = parseTextFieldAsInteger(damageD10Field);
 	result += n + "d10!+";
 	n = parseTextFieldAsInteger(damageD12Field);
 	result += n + "d12!+";
 	return result.substring(0, result.length()-1); // Remove trailing "+"
     }
 
     private int parseTextFieldAsInteger(JTextField field) {
 	String content = field.getText().trim();
 	if ("".equals(content)) 
 	    return 0;
 	else
 	    return Integer.parseInt(content);
     }
     
     /**
      * A simple counter for several damage-related concepts in Savage Worlds
      * 
      * @author sergut
      */
     private class DamageCounter {
 	int nothing = 0;
 	int shaken  = 0;
 	int wound1  = 0;
 	int wound2  = 0;
 	int wound3  = 0; 
 	int wound4m = 0; // 4 or more
 	@Override public String toString() {
 	    return "Nothing:" + nothing + 
 	    	", Shaken:" + shaken +
 	    	", 1 wound:" + wound1 +
 	    	", 2 wounds: " + wound2 +
 	    	", 3 wounds: " + wound3 +
 	    	", 4 wounds or more:" + wound4m;
 	}
 	// convenience
 	public int getWounds() {
 	    return wound1 + wound2 + wound3 + wound4m;
 	}
     }
 }
