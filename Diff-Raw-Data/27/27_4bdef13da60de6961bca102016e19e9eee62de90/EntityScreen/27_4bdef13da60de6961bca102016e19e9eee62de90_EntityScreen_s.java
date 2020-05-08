 /**
  * EntityScreen
  * 
  * Class representing the screen that manages user interactions 
  * pertaining to grid entities, including triggers and appearance.
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
 
 public class EntityScreen extends Screen {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8471925846048875713L;
 
 	public EntityScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Entities");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel panel = new JPanel();
 		JButton backButton = new JButton("Back");
 		backButton.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				});
 		JButton editButton = new JButton("Edit");
 		editButton.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Entities")); 
 					} 
 				});
 		this.add(label, BorderLayout.NORTH);
 		panel.add(backButton);
 		panel.add(editButton);
 		this.add(panel);
 	}
 
 
 	public void addComponents(JPanel panel) {
 		// TODO Auto-generated method stub
 
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
