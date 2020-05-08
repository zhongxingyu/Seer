 package edu.wheaton.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JTextField;
 
 public class NewSimScreenFinishListener implements ActionListener {
 	
 	private JTextField name;
 	private JTextField width;
 	private JTextField height;
 	private Manager sm;
 	
 	public NewSimScreenFinishListener(JTextField name, JTextField width, JTextField height, 
 									  Manager sm){
 		this.name = name;
 		this.width = width;
 		this.height = height;
 		this.sm = sm;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		//TODO The simulation as a whole will be created in here
 		String nameString = getName();
 		int heightInt = 0;
 		int widthInt = 0;
 		try {
 			heightInt = Integer.parseInt(height.getText());
 			widthInt = Integer.parseInt(width.getText());
 			sm.setFacade(widthInt, heightInt);
 			sm.updateGUIManager(nameString, widthInt, heightInt);
 		} catch(java.lang.NumberFormatException nfe) { 
			System.err.println("Invalid input passed to NewSimScreenFinishListener");
 		}
 		Screen upload = sm.getScreen("Edit Simulation");
 		sm.update(upload);
 		sm.loadScreen(upload);
 	}
 	public String getName(){
 		return name.getText();
 	}
 	public int getHeight(){
 		return Integer.parseInt(height.getText());
 	}
 	public int getWidth(){
 		return Integer.parseInt(width.getText());
 	}
 }
