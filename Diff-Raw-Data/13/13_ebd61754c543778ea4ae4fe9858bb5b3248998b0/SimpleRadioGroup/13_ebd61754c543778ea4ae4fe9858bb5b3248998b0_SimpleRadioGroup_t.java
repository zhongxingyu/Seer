 package simpleui;
 /*
  * Author: Aaron Nech
  * Project: SJGF
  * Description: Radio Button Group class. These manage groups of radio buttons
  * 
  */

 import gameabstract.InputEvent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SimpleRadioGroup extends SimpleUIComponent {
 	private SimpleRadio selected;
 	private List<SimpleRadio> members;
 	
 	//constructs a new radio group
 	public SimpleRadioGroup() {
 		selected = null;
 		members = new ArrayList<SimpleRadio>();
 	}
 	
 	//adds a radio button to the group
 	public void add(SimpleRadio button) {
 		button.setGroup(this);
 		members.add(button);
 	}

 	//adds a radio button to the group and subscribes it
 	public void add(SimpleRadio button, SimpleUIListener listener,
 			SimpleEventType action, String methodName) {
 		button.subscribe(listener, action, methodName);
 		button.setGroup(this);
 		members.add(button);
 	}

 	//selects a specific radio button in the group
 	public void select(SimpleRadio radio) {
 		if(members.contains(radio)) {
 			selected = radio;
 			for(SimpleRadio button : members)
 				button.set(false);
 			selected.set(true);
 		}
 	}

 	//selects a radio button in the group by name
 	public void select(String name) {
 		for(SimpleRadio button : members) {
 			if(button.name.equals(name)) {
 				selected = button;
 				for(SimpleRadio others : members)
 					others.set(false);
 				selected.set(true);
 			}
 		}
 	}

 	//reacts to input events dished out by the ui manager
 	public boolean react(InputEvent event) {
 		boolean hitOne = false;
 		for(SimpleRadio r : members) {
 			if(r.react(event))
 				hitOne = true;
 		}
 		return hitOne;
 	}
 
 	//gets the radio buttons in this group
 	public List<SimpleRadio> getRadios() {
 		return members;
 	}
 	
 }
