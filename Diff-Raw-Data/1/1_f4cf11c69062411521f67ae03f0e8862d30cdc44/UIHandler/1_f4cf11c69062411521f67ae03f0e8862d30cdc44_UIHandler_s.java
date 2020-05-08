 package org.noip.staticattic.GUI;
 
 import java.util.ArrayList;
 
 import org.noip.staticattic.GUI.Events.GUIEvent;
 
 public class UIHandler implements Runnable {
 
 	private ArrayList<GUIEvent> events = new ArrayList<GUIEvent>();
 	
 	@Override
 	public void run() {
 		
 		for (GUIEvent e: events) {
 			
 			if (e.getDelay() <= 0) {
 				
 				e.execute();
 				events.remove(e);
 				
 			} else {
 				
 				e.setDelay(e.getDelay()-20);
 				
 			}
 			
 		}
 		
 	}
 	
 	public void addToQueue(GUIEvent event) {
 		
 		events.add(event);
 		
 	}
 
 }
