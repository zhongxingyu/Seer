 package edu.wheaton.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 
 public class SetupScreen extends Screen {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8347080877399964861L;
 
 	public SetupScreen(final ScreenManager sm) {
 		super(sm);
 		JButton backButton = new JButton("Back");
 		backButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation"));
 					}
 				}
 		);
 		this.add(backButton);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void sendInfo() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
