 package de.tud.kitchen.doorGui;
 
 import de.tud.kitchen.api.Kitchen;
 import de.tud.kitchen.api.event.EventConsumer;
 import de.tud.kitchen.api.event.furniture.DoorEvent;
 import de.tud.kitchen.api.module.KitchenModuleActivator;
 
 public class Activator extends KitchenModuleActivator {
 
 	Demo Gui;
 	
 	@Override
 	public void start(Kitchen kitchen) {
 		kitchen.registerEventConsumer(new myEventConsumer());
 		Gui = new Demo();
 	}
 
 	@Override
 	public void stop() {
 		Gui = null;
 		
 	}
 	
	class myEventConsumer extends EventConsumer {
 		
 		public void handleDoorEvent (DoorEvent e){
 			Gui.updateGui(e);
 		}
 		
 	}
 }
