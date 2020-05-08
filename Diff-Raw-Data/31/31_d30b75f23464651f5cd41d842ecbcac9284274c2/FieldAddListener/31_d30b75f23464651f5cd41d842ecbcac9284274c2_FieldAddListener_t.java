 package edu.wheaton.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class FieldAddListener implements ActionListener{
 
 	private Manager sm;
 	
 	public FieldAddListener(Manager sm){
 		this.sm = sm;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		boolean editing = FieldScreen.getEditing();
 		if(editing)
 			editing = false;
 		((EditFieldScreen) (sm.getScreen("Edit Fields"))).load(
 				sm.getFacade().getGrid());
 		sm.update(sm.getScreen("Edit Fields"));
 	}
 }
