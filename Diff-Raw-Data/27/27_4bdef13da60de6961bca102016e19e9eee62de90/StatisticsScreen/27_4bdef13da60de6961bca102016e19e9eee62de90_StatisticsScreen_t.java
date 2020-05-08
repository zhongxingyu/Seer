 /**
  * StatisticsScreen
  * 
  * Class representing the screen that allows users to view statistics.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 public class StatisticsScreen extends Screen {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 714636604315959167L;
 
 	public StatisticsScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Statistics");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel panel = new JPanel();
 		JButton finishButton = new JButton("Finish");
 		finishButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				});
 		this.add(label, BorderLayout.NORTH);
 		panel.add(finishButton);
 		this.add(panel);
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void sendInfo() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
