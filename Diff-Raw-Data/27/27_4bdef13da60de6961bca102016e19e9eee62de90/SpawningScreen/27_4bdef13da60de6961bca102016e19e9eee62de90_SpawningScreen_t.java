 /**
  * SpawningScreen
  * 
  * Class representing the screen that allows to determine spawning conditions,
  * or spawn new entities during a simulation.
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
 
 public class SpawningScreen extends Screen {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6312784326472662829L;
 
 	public SpawningScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Spawning");
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
