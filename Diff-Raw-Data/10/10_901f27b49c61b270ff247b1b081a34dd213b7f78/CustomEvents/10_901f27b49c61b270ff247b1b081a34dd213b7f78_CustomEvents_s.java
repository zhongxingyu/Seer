 package com.stormdev.minigamez.editor;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 
 public class CustomEvents extends JPanel implements ActionListener {
 	private static final long serialVersionUID = -7360871852330458524L;
 	private ArrayList<String> vals = new ArrayList<String>();
 	WindowArea window = null;
 	JTextField newEvtName = new JTextField(16);
 	JButton add = new JButton("Add");
 	public CustomEvents(WindowArea area){
 		this.window = area;
 		this.setLayout(new GridLayout(0,2));
 		this.add.addActionListener(this);
 		draw();
 	}
 	public void setEvt(String name){
 		Boolean contains = false;
 		for(String eName:this.vals){
 			if(eName.equalsIgnoreCase(name)){
 				contains = true;
 			}
 		}
 		ArrayList<String> official = new GameEventList(this.window).getOfficial();
 		for(String eName:official){
 			if(eName.equalsIgnoreCase(name)){
 				this.window.popUpMsg("Already in default event list!", "ERROR");
 				contains = true;
 			}
 		}
 		if(!contains){
 			this.vals.add(name);
 		}
 		return;
 	}
 	public ArrayList<String> getEvts(){
 		return this.vals;
 	}
 	public void removeLocation(String name){
 		Boolean contains = false;
 		for(String eName:this.vals){
 			if(eName.equalsIgnoreCase(name)){
 				name = eName;
 				contains = true;
 			}
 		}
 		if(contains){
 			this.vals.remove(name);
 		}
 		return;
 	}
 	public void draw(){
 		this.clear();
 		for(String eName:this.vals){
 			JButton removeIt = new JButton("Remove");
 			final String locName = eName;
 			removeIt.setAction(new Action(){
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					removeLocation(locName);
 					draw();
 				}
 				@Override
 				public void addPropertyChangeListener(
 						PropertyChangeListener arg0) {
 				}
 				@Override
 				public Object getValue(String arg0) {
 					return null;
 				}
 				@Override
 				public boolean isEnabled() {
 					return true;
 				}
 				@Override
 				public void putValue(String arg0, Object arg1) {
 				}
 				@Override
 				public void removePropertyChangeListener(
 						PropertyChangeListener arg0) {
 				}
 				@Override
 				public void setEnabled(boolean arg0) {
 				}});
 			removeIt.setText("Remove");
 		this.add(new JLabel(eName)); this.add(removeIt);
 		}
 		this.add(newEvtName); this.add(add);
 		window.refresh();
 	}
 	public void clear(){
 		Component[] comps = this.getComponents().clone();
 		for(Component comp:comps){
 			this.remove(comp);
 		}
 		return;
 	}
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		Object source = event.getSource();
 		if(source == add){
 			String toAdd = newEvtName.getText();
 			if(toAdd == null || toAdd == "" || toAdd.length() < 1){
 				window.popUpMsg("Please select a event name to add!", "ERROR");
 				System.out.println("No Name! Aborted!");
 				return;
 			}
 			if(toAdd.contains(" ")){
 				window.popUpMsg("Event name cannot contain spaces!", "ERROR");
 				System.out.println("Space in name! Aborted!");
 				return;
 			}
 			setEvt(toAdd);
 			newEvtName.setText("");
 			draw();
 			return;
 		}
 	}
 	
 	
 
 }
