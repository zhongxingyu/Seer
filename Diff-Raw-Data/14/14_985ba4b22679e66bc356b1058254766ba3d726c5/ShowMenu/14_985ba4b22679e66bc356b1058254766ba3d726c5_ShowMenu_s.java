 package de.kit.irobot.util;
 
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 import lejos.util.TextMenu;
 
 public class ShowMenu extends BaseBehaviour {
 	
 	private boolean controlRequested = false;
 	
 	
 	public ShowMenu(Config config, BehaviourController controller) {
 		super(config, controller);
 	}
 	
 	
 	public void executeAction() {
 		kill();
 		show();
 	}
 	
 	public static void show() {
 		LCD.clear();
 		
 		State[] states = State.values();
 		String[] menuItems = new String[states.length];
 		for (int i = 0; i < states.length; i++) {
 			menuItems[i] = states[i].toString();
 		}
 		TextMenu menu = new TextMenu(menuItems, 1, "Dummer Toaster 5");
 		int selectionIndex = menu.select();
 		
 		if (selectionIndex < 0) {
 			System.exit(0);
 		}
 		else {
 			LCD.clear();
 			State state = State.values()[selectionIndex];
 			int duration = 3;
 			for(int i = 0; i < duration; i++) {
 				LCD.drawString("Timer: "+ (duration - i), 0, 0);
 				ThreadUtil.sleep(1000);
 			}
 			LCD.clear();
 			state.start();
 		}
 	}
 	
 	public boolean executeTakeControl() {
 		if (Button.ESCAPE.isPressed()) {
 			controlRequested = true;
 		}
 		return controlRequested;
 	}
 	
 }
