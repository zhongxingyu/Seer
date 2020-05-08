 package org.sergut.diceroller.ui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import org.sergut.diceroller.DiceRoller;
 import org.sergut.diceroller.IllegalDiceExpressionException;
 import org.sergut.diceroller.savageworlds.SavageWorldsDamageCounter;
 import org.sergut.diceroller.savageworlds.SavageWorldsSimulationJob;
 import org.sergut.diceroller.savageworlds.SavageWorldsSimulationResult;
 import org.sergut.diceroller.savageworlds.SavageWorldsSimulator;
 
 public class SavageWorldsFrame extends JFrame {
 
     private static final long serialVersionUID = 12872389749812789L;
     
     private static final int FIELD_LENGTH = 4;
     
     // TODO: develop a general reusable way of creating labels and fields for dice
     //   based on a text description, maybe partially based on roll20 syntax
     
     private final String[] attackDice = {"d4", "d6", "d8", "d10", "d12"};
 
     private JComboBox attackDiceCombobox = new JComboBox(attackDice);
 
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
 
     private WildCardChoicePanel attackerWildCardPanel = new WildCardChoicePanel("Attacker");
     private WildCardChoicePanel defenderWildCardPanel = new WildCardChoicePanel("Defender");
     
     private CheckPanel defenderShakenPanel = new CheckPanel("Defender already shaken?");
     
     public SavageWorldsFrame() {
 	setButtonBehaviours();
 	JPanel westPane = getWestPanel();
 	JPanel eastPane = getEastPanel();	
 	JPanel northPane = new JPanel();	
 	northPane.setLayout(new GridLayout(1,0));
 	northPane.add(westPane);
 	northPane.add(eastPane);
 	JPanel southPane = getSouthPanel();	
 	this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 	this.add(northPane);
 	this.add(southPane);
 	this.pack();
 	this.setTitle("Savage Worlds: Kill chances");
 	this.setLocation(100, 100);
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
 	enemyPane.add(defenderWildCardPanel);
 	enemyPane.add(defenderShakenPanel);
 	result.setLayout(new GridLayout(0,1));
 	result.add(damagePane);
 	result.add(enemyPane);
 	return result;
     }
     
     private JPanel getWestPanel() {
 	JPanel result = new JPanel();
 	result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
 	JPanel attackDiePanel = new JPanel();
 	attackDiePanel.add(new JLabel("Attack die: "));
 	attackDiePanel.add(attackDiceCombobox);
 	result.add(attackDiePanel);
 	result.add(attackerWildCardPanel);
 	// TODO: more options
 	//   - 2 weapons
 	//      - two-fisted
 	//      - ambidextrous
 	//   - trademark weapon
 	//   - gang-up bonus ____
 	//   - magic bonus   ____
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
 
     private void setButtonBehaviours() {
 	calculateButton.addActionListener(new ActionListener() {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		runSimulation();
 	    }});
     }
 
     private void runSimulation() {
 	try {
 	    SavageWorldsSimulationJob job = new SavageWorldsSimulationJob();
 	    job.attackDice = collectAttackDice();
 	    job.damageDice = collectDamageDice();
 	    job.attackerWildCard = attackerWildCardPanel.isWildCard();
 	    job.defenderParry = getParry();
 	    job.defenderToughness = getToughness();
 	    job.defenderShaken = defenderShakenPanel.isChecked();
 	    job.defenderWildCard = defenderWildCardPanel.isWildCard;
 	    job.maxIterations = getMaxIterations();
 	    SavageWorldsSimulationResult result = (new SavageWorldsSimulator()).simulate(job);
 	    SavageWorldsDamageCounter damageCounter = result.getResult("Normal, body");
 	    if (defenderWildCardPanel.isWildCard()) { 
 		showResultsForWildCard(damageCounter);
 	    } else {
 		showResultsForExtra(damageCounter);
 	    }
 	} catch (IllegalDiceExpressionException ex) {
 	    String s = "Invalid expression: " + ex.getExpression();
 	    ex.printStackTrace();
 	    JOptionPane.showMessageDialog(this, s, "Invalid expression", JOptionPane.ERROR_MESSAGE);
 	}
     }
 
     private void showResultsForWildCard(SavageWorldsDamageCounter damageCounter) {
 	int wound1Ratio = DiceRoller.getSimpleRate(damageCounter.wound1, damageCounter.getTotalRolls());
 	int wound2Ratio = DiceRoller.getSimpleRate(damageCounter.wound2, damageCounter.getTotalRolls());
 	int wound3Ratio = DiceRoller.getSimpleRate(damageCounter.wound3, damageCounter.getTotalRolls());
 	int wound4Ratio = DiceRoller.getSimpleRate(damageCounter.wound4m, damageCounter.getTotalRolls());
 	int shakenRatio = DiceRoller.getSimpleRate(damageCounter.shaken, damageCounter.getTotalRolls());
 	String s = "Shaken  ratio: " + shakenRatio + "% \n"
 	    	+  "1 wound  ratio: " + wound1Ratio + "% \n" 
 	    	+  "2 wounds ratio: " + wound2Ratio + "% \n" 
 	    	+  "3 wounds ratio: " + wound3Ratio + "% \n" 
 	    	+  "4+ wounds ratio: " + wound4Ratio + "% "; 
 	System.out.println(damageCounter);
 	JOptionPane.showMessageDialog(this, s, "Result", JOptionPane.INFORMATION_MESSAGE);
     }
 
     private void showResultsForExtra(SavageWorldsDamageCounter damageCounter) {
 	int killRatio = DiceRoller.getSimpleRate(damageCounter.getWounds(), damageCounter.getTotalRolls());
 	int shakenRatio = DiceRoller.getSimpleRate(damageCounter.shaken, damageCounter.getTotalRolls());
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
 	if (attackerWildCardPanel.isWildCard()) {
 	    result += ",1d6!";
 	}
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
     * A panel to choose whether someone in an extra or a wild card
      */
     private class WildCardChoicePanel extends JPanel {
 	private static final long serialVersionUID = 111111L;
 	private boolean isWildCard = false;
 	
 	public WildCardChoicePanel(String title) {
 	    this.setLayout(new FlowLayout());
 	    JLabel label = new JLabel(title);
 	    this.add(label);
 	    ButtonGroup buttonGroup = new ButtonGroup();
 	    JRadioButton extraButton = new JRadioButton("Extra", true);
 	    buttonGroup.add(extraButton);
 	    this.add(extraButton);
 	    extraButton.addActionListener(new ActionListener() {
 		@Override public void actionPerformed(ActionEvent e) {
 		    isWildCard = false;
 		}});
 	    JRadioButton wildCardButton = new JRadioButton("Wild Card", false);
 	    buttonGroup.add(wildCardButton);
 	    this.add(wildCardButton);
 	    wildCardButton.addActionListener(new ActionListener() {
 		@Override public void actionPerformed(ActionEvent e) {
 		    isWildCard = true;
 		}});
 	}
 	
 	public boolean isWildCard() {
 	    return isWildCard;
 	}
     }
     
     /**
      * A panel to check or uncheck some property
      */
     private class CheckPanel extends JPanel {
 	private static final long serialVersionUID = 111113L;
 	private JCheckBox box = new JCheckBox();
 	
 	public CheckPanel(String text) {
 	    this.setLayout(new FlowLayout());
 	    this.add(new JLabel(text));    
 	    this.add(box);
 	}
 	
 	public boolean isChecked() {
 	    return box.isSelected();
 	}
     }
     
     
     public static void main(String... args) {
 	SavageWorldsFrame frame = new SavageWorldsFrame();
 	frame.setVisible(true);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
     }
 }
