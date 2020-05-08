 package com.carr.mitchell.zyxba_gui.listeners;
 
 import com.carr.mitchell.zyxba_gui.GUI;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 
 /**
  * Handles MenuItem events
  * */
 public class ButtonListener implements ActionListener {
 
 	private final GUI parent;
 
 	/**
	 * @param parent GUI to accept this class's output.
 	 */
 	public ButtonListener(GUI gui) {
 		
 		this.parent = gui;
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 
 		Object obj = ae.getSource();
 
 		if (obj instanceof JMenuItem) {
 
 			JMenuItem jbt = (JMenuItem) obj;
 			String command = jbt.getText();
 
 			if (command.equals(GUI.openCsv)) {
 				OpenListener ol = new OpenListener(parent);
 				ol.actionPerformed(ae);
 				if (parent.getCSV() != null) {
 					parent.readCSV();
 				}
 			} else {
 				ExportListener el = new ExportListener(parent);
 				el.actionPerformed(ae);
 			}
 
 		}
 
 	}
 
 }
