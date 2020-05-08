 package com.heralli.ekonsehan;
 
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.JLabel;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Color;
 
 public class MultipleChoiceQuestionView extends JPanel {
 
 	MultipleChoiceQuestion q;
 	
 	/**
 	 * Create the panel.
 	 */
 	public MultipleChoiceQuestionView(MultipleChoiceQuestion q) {
 		setBackground(Color.CYAN);
 		this.q = q;
 		setLayout(null);
 		
 		JTextPane txtpnQuestiontext = new JTextPane();
 		txtpnQuestiontext.setFont(new Font("Arial", Font.PLAIN, 46));
 		txtpnQuestiontext.setText("questionText");
 		txtpnQuestiontext.setBounds(30, 12, 541, 196);
 		add(txtpnQuestiontext);
 		
 		JLabel lblChoicea = new JLabel("ChoiceA");
 		lblChoicea.setBounds(30, 251, 205, 15);
 		add(lblChoicea);
 		
 		JLabel lblChoiceb = new JLabel("ChoiceB");
 		lblChoiceb.setBounds(366, 251, 205, 15);
 		add(lblChoiceb);
 		
 		JLabel lblChoicec = new JLabel("ChoiceC");
 		lblChoicec.setBounds(30, 330, 205, 15);
 		add(lblChoicec);
 		
 		JLabel lblChoiced = new JLabel("ChoiceD");
 		lblChoiced.setBounds(366, 330, 205, 15);
 		add(lblChoiced);
 		
 		txtpnQuestiontext.setText(q.getQuestion());
 		lblChoicea.setText(q.getChoice(0));
 		lblChoiceb.setText(q.getChoice(1));
 		lblChoicec.setText(q.getChoice(2));
 		lblChoiced.setText(q.getChoice(3));
 		
 		this.setPreferredSize(new Dimension(600, 400));
		this.setMinimumSize( new Dimension(600, 400));
		this.setMaximumSize(new Dimension(600, 400));
 	}
 }
