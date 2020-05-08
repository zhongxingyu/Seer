 package org.sergut.diceroller.ui;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.sergut.diceroller.DiceRoller;
 
 /**
  * A simple frame to roll a set of dice as described by a String with roll20
  * syntax.
  * 
  * @author sergut
  * 
  */
 public class SimpleDieRollFrame extends JFrame {
 
 	private static final long serialVersionUID = 128723849817789L;
 
 	private JButton rollButton = new JButton("Roll!");
 
 	private JLabel diceLabel = new JLabel("Dice expression");
	private JTextField diceField = new JTextField("                     ");
 
 	public SimpleDieRollFrame() {
 		JPanel dicePane = new JPanel();
 		dicePane.setLayout(new FlowLayout());
 		dicePane.add(diceLabel);
 		dicePane.add(diceField);
 
 		JPanel mainPane = new JPanel();
 		mainPane.setLayout(new GridLayout(0, 1));
 		mainPane.add(dicePane);
 		mainPane.add(rollButton);
 
 		setButtonBehaviours();
 
 		this.add(mainPane);
 		this.pack();
 		this.setResizable(false);
 		this.setTitle("Simple dice roller");
 	}
 
 	private void setButtonBehaviours() {
 		rollButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				rollDice();
 			}
 		});
 	}
 
 	private void rollDice() {
 		try {
 			String dice = diceField.getText().trim();
 			DiceRoller diceRoller = new DiceRoller();
 			int result = diceRoller.rollDice(dice);
 			String s = "The result is " + result + ".";
 			JOptionPane.showMessageDialog(this, s, "Result", JOptionPane.INFORMATION_MESSAGE);
 		} catch (NumberFormatException ex) {
 			String s = "There is an error with one of the numbers";
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, s, "Invalid number",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}

 }
