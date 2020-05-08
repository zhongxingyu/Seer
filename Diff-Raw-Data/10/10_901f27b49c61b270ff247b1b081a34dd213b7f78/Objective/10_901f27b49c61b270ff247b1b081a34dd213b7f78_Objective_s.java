 package com.stormdev.minigamez.editor;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 public class Objective extends JPanel implements ActionListener{
 	private static final long serialVersionUID = -8722850909010143667L;
 	WindowArea window = null;
 	GameEventList event = null;
 	ObjectiveActions actions = null;
 	JPanel values = new JPanel();
 	JButton save = new JButton("Save!");
 	JLabel when = new JLabel("When:");
 	JLabel DO = new JLabel("Do:");
 	ObjectiveManager manager = null;
     public Objective(WindowArea area, ObjectiveManager manager){
     	this.window = area;
     	event = new GameEventList(area);
     	actions = new ObjectiveActions(area);
     	event.addActionListener(this);
     	actions.addActionListener(this);
     	event.draw();
     	actions.draw();
     	this.manager = manager;
     	this.save = new JButton("Save!");
     	this.save.addActionListener(this.manager);
     }
     public String getEvent(){
     	return (String)this.event.getSelectedItem();
     }
     public String getAction(){
     	return (String)this.actions.getSelectedItem();
     }
     public HashMap<String, Object> getActionValues(){
 		HashMap<String, Object> vals = new HashMap<String, Object>();
 		Component[] comps = values.getComponents();
 		for(Component comp:comps){
 			if(comp.getName() == null || comp.getName().length() < 1){
 			}
 			else{
 			if(comp.getName().toLowerCase().contains("actionarg")){
 				if(comp instanceof JTextField){
 					if(comp.getName().toLowerCase().contains("t:int")){
 						int val = 1;
 						try {
 							val = Integer.parseInt(((JTextField) comp).getText());
 						} catch (NumberFormatException e) {
 							this.window.popUpMsg("Field should contain an integer!", "ERROR");
 							return null;
 						}
 						vals.put(comp.getName(), val);
 						((JTextField) comp).setText("");
 					}
 					else if(comp.getName().toLowerCase().contains("t:num")){
 						double val = 1;
 						try {
 							val = Double.parseDouble(((JTextField) comp).getText());
 						} catch (NumberFormatException e) {
 							this.window.popUpMsg("Field should contain a number!", "ERROR");
 							return null;
 						}
 						vals.put(comp.getName(), val);
 						((JTextField) comp).setText("");
 					}
 					else if(comp.getName().toLowerCase().contains("t:chance")){
 						double val = 1;
 						try {
 							val = Double.parseDouble(((JTextField) comp).getText());
 						} catch (NumberFormatException e) {
 							this.window.popUpMsg("Field should contain a number!", "ERROR");
 							return null;
 						}
 						if(val > 100 || val <= 0){
 							this.window.popUpMsg("Field should contain a number between 0.1 and 100!", "ERROR");
 							return null;
 						}
 						vals.put(comp.getName(), val);
 						((JTextField) comp).setText("");
 					}
 					else{
 					vals.put(comp.getName(), ((JTextField)comp).getText());
 					((JTextField) comp).setText("");
 					}
 				}
 				if(comp instanceof OptionList){
 					if(((String)((OptionList) comp).getSelectedItem()) == null || ((String)((OptionList) comp).getSelectedItem()).length() < 1){
 						this.window.popUpMsg("Option field cannot be empty!", "ERROR");
 						return null;
 					}
 					vals.put(comp.getName(), ((OptionList)comp).getSelectedItem());
 				}
 				if(comp instanceof LocationsList){
 					vals.put(comp.getName(), ((LocationsList)comp).getSelectedItem());
 				}
 			}
 			}
 		}
 		return vals;
     }
     public HashMap<String, Object> getEventValues(){
 		HashMap<String, Object> vals = new HashMap<String, Object>();
 		Component[] comps = values.getComponents();
 		for(Component comp:comps){
 			if(comp.getName() == null || comp.getName().length() < 1){
 			}
 			else{
 			if(comp.getName().toLowerCase().contains("eventarg")){
 				if(comp instanceof JTextField){
 					if(comp.getName().toLowerCase().contains("t:int")){
 						int val = 1;
 						try {
 							val = Integer.parseInt(((JTextField) comp).getText());
 						} catch (NumberFormatException e) {
 							this.window.popUpMsg("Field should contain an integer!", "ERROR");
 							return null;
 						}
 						vals.put(comp.getName(), val);
 						((JTextField) comp).setText("");
 					}
 					else{
 					vals.put(comp.getName(), ((JTextField)comp).getText());
 					((JTextField) comp).setText("");
 					}
 				}
 				if(comp instanceof OptionList){
 					vals.put(comp.getName(), ((OptionList)comp).getSelectedItem());
 				}
 				if(comp instanceof LocationsList){
 					vals.put(comp.getName(), ((LocationsList)comp).getSelectedItem());
 				}
 			}
 			}
 		}
 		return vals;
     }
     public JButton getSaveButton(){
     	return this.save;
     }
     public void onLoad(){
     	this.event.calculate();
     	this.event.draw();
     	return;
     }
     public void draw(){
     	clearPanel(values);
     	this.add(when);
     	this.add(event);
     	this.add(DO);
     	this.add(actions);
     	//adjust values tab
     	if(getEvent().equalsIgnoreCase("numPlayersLeft")){
     		JTextField pLeft = new JTextField(2);
     		pLeft.setText("2");
     		pLeft.setName("eventarg0t:int");
     		values.add(new JLabel("Left:"));
     		values.add(pLeft);
     	}
     	if(getEvent().equalsIgnoreCase("playerArriveAtLocation")){
     		LocationsList loc = new LocationsList(this.window);
     		loc.setName("eventarg0");
     		values.add(new JLabel("Location(arrived):"));
     		values.add(loc);
     	}
     	if(getEvent().equalsIgnoreCase("reachXPoints")){
     		JTextField pts = new JTextField(3);
     		pts.setName("eventarg0t:int");
     		values.add(new JLabel("Pts reached:"));
     		values.add(pts);
     	}
     	if(getEvent().equalsIgnoreCase("teamMateArriveAtLocation")){
     		if(!this.window.useTeams.isSelected()){
     			this.window.popUpMsg("This event doesn't function with teams disabled!", "WARNING");
     		}
     		LocationsList loc = new LocationsList(this.window);
     		loc.setName("eventarg0");
     		OptionList team = new OptionList(this.window);
     		team.setVals(this.window.getTeams());
     		team.setName("eventarg1");
     		team.draw();
     		values.add(new JLabel("Location(arrived):"));
     		values.add(loc);
     		values.add(new JLabel("Team:"));
     		values.add(team);
     	}
     	if(getAction().equalsIgnoreCase("sendMessage")){
     		JTextField toSend = new JTextField(16);
     		toSend.setName("actionarg0");
     		OptionList to = new OptionList(window);
     		ArrayList<String> recipients = new ArrayList<String>();
     		recipients.add("player");
     		recipients.add("team");
     		to.setVals(recipients);
     		to.setName("actionarg1");
     		to.draw();
     		values.add(new JLabel("Msg:"));
     		values.add(toSend);
     		values.add(new JLabel("To:"));
     		values.add(to);
     	}
     	if(getAction().equalsIgnoreCase("broadcastMessage")){
     		JTextField toSend = new JTextField(16);
     		toSend.setName("actionarg0");
     		values.add(new JLabel("Msg:"));
     		values.add(toSend);
     	}
     	if(getAction().equalsIgnoreCase("incrementPoints")){
     		JTextField pts = new JTextField(3);
     		OptionList who = new OptionList(this.window);
     		ArrayList<String> vals = new ArrayList<String>();
     		vals.add("invloved");
     		vals.add("opposition");
     		vals.add("team");
     		vals.add("rivalTeam");
     		who.setVals(vals);
     		who.draw();
     		pts.setText("1");
     		pts.setName("actionarg0t:int");
     		who.setName("actionarg1");
     		values.add(new JLabel("Pts to increment:"));
     		values.add(pts);
     		values.add(new JLabel("Who to increment:"));
     		values.add(who);
     	}
     	if(getAction().equalsIgnoreCase("teleportTo")){
     		LocationsList loc = new LocationsList(this.window);
     		loc.setName("actionarg0");
     		values.add(new JLabel("Location(tp to):"));
     		values.add(loc);
     	}
     	if(getAction().equalsIgnoreCase("spawnMob")){
     		LocationsList loc = new LocationsList(this.window);
     		OptionList mob = new OptionList(this.window);
     		mob.setVals(Mobs.getMobs());
     		mob.draw();
     		JTextField amount = new JTextField(3);
     		amount.setText("1");
     		loc.setName("actionarg0");
     		mob.setName("actionarg1");
     		amount.setName("actionarg2t:int");
     		values.add(new JLabel("Location(Spawn at):"));
     		values.add(loc);
     		values.add(new JLabel("Mob(To spawn):"));
     		values.add(mob);
     		values.add(new JLabel("Amount(To spawn):"));
     		values.add(amount);
     	}
     	if(getAction().equalsIgnoreCase("fireEvent")){
     		GameEventList list = new GameEventList(this.window);
     		list.draw();
     		list.setName("actionarg0");
     		values.add(new JLabel("Event to fire:"));
     		values.add(list);
     	}
     	if(getAction().equalsIgnoreCase("chance")){
     		JTextField chance = new JTextField(3);
     		chance.setName("actionarg0t:chance");
     		ArrayList<String> cEvnts = window.customEvents.getEvts();
     		cEvnts.add("Nothing");
     		OptionList list = new OptionList(this.window);
     		list.setVals(cEvnts);
     		list.draw();
     		list.setName("actionarg1");
     		OptionList rlist = new OptionList(this.window);
     		@SuppressWarnings("unchecked")
 			ArrayList<String> reserveEvts = (ArrayList<String>) cEvnts.clone();
     		rlist.setVals(reserveEvts);
     		rlist.draw();
     		rlist.setSelectedItem("Nothing");
     		rlist.setName("actionarg2");
     		values.add(new JLabel("Chance (/100):"));
     		values.add(chance);
     		values.add(new JLabel("Action if chance succeed(custom event):"));
     		values.add(list);
     		values.add(new JLabel("Action if chance failed (custom event):"));
     		values.add(rlist);
     	}
     	if(getAction().equalsIgnoreCase("setRespawnLoc")){
     		LocationsList loc = new LocationsList(this.window);
     		loc.setName("actionarg0");
     		values.add(new JLabel("Location(respawn):"));
     		values.add(loc);
     	}
     	this.add(values);
     	this.add(this.save);
     	this.save.addActionListener(this.manager);
     	window.refresh();
     }
     public void clearPanel(JPanel panel){
     	panel.removeAll();
     	return;
     }
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 	    draw();
 	}
     
 	
 
 }
