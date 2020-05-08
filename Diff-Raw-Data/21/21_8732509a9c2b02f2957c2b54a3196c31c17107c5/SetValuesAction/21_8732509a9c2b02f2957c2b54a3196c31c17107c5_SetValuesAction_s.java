 package de.hpi_web.cloudSim.profiling.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class SetValuesAction implements ActionListener{
 
 	private StartAction start;
 	private Gui parent;
 	
 	public SetValuesAction(StartAction start, Gui parent) {
 		super();
 		this.parent = parent;
 		this.start = start;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		start.setDelay(Integer.parseInt(parent.getTextField_2().getText()));
 		start.setUpperThreshold(Integer.parseInt(parent.getTextField().getText()));
 		start.setLowerThreshold(Integer.parseInt(parent.getTextField_1().getText()));
 	}
 
 }
