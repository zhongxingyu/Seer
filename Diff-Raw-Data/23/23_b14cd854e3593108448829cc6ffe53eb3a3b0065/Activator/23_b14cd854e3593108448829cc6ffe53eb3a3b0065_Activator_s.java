 package de.tud.kitchen.osc;
 
 import java.io.IOException;
 import java.net.SocketException;
 
 import de.tud.kitchen.api.Kitchen;
 import de.tud.kitchen.api.event.EventConsumer;
 import de.tud.kitchen.api.event.furniture.DoorEvent;
 import de.tud.kitchen.api.module.KitchenModuleActivator;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceInfo;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.illposed.osc.OSCListener;
 import com.illposed.osc.OSCMessage;
 import com.illposed.osc.OSCPortIn;
 
 public class Activator extends KitchenModuleActivator {
 	public final static String REMOTE_TYPE = "_tk_kitchen._tcp.local.";
 
 	private OSCPortIn port;
 	private JmDNS jmdns;

 	@Override
 	public void start(Kitchen kitchen) {
 		System.out.println("Starting Android Bundle");

 		// ----- start jmdns and register service for android ----- //
 		try {
			ServiceInfo pairservice = ServiceInfo.create(REMOTE_TYPE, "KitchenAndroidOSCReceiver", 3333,
 					"TK SmartKitchen Project, Android OSC Receiver");
 			jmdns.registerService(pairservice);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// ----- start osc and register listener for data reception ----- //
 		try {
			port = new OSCPortIn(3333);
 			OSCListener listener = new OSCListener() {
 				public void acceptMessage(java.util.Date time, OSCMessage message) {
 					Object[] sensor_data = message.getArguments();
 					System.out.println("OSC: " + sensor_data[0] + " | " + sensor_data[1] + " | " + sensor_data[2] + "  ||  "
 							+ sensor_data[3]);
 				}
 			};
 			port.addListener("/android/101", listener);
 			port.startListening();
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void stop() {
 		System.out.println("Stopping Bundle");
 
 		port.stopListening();
 		port.close();
 
 		try {
 			jmdns.unregisterAllServices();
 			jmdns.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
