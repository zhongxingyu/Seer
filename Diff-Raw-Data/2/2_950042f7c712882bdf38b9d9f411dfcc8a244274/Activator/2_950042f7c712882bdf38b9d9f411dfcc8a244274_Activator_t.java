 package de.tud.kitchen.taggingGui;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import de.tud.kitchen.api.Kitchen;
 import de.tud.kitchen.api.event.EventPublisher;
 import de.tud.kitchen.api.module.KitchenModuleActivator;
 
 public class Activator extends KitchenModuleActivator {
 
 	EventPublisher<taggingEvent> publisher;
 	gui mygui;
 	
 	@Override
 	public void start(Kitchen kitchen) {
 		publisher = kitchen.getEventPublisher(taggingEvent.class);
 		mygui = new gui(publisher);
 	}
 
 	@Override
 	public void stop() {
 		mygui.close();		
 	}
 	
 	
 }
