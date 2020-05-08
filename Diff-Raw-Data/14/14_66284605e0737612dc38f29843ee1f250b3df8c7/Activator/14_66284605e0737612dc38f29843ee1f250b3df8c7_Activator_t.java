 package de.tud.kitchen.template;
 
 import java.net.SocketException;
 import java.util.Date;
 
 import com.illposed.osc.OSCListener;
 import com.illposed.osc.OSCMessage;
 import com.illposed.osc.OSCPort;
 import com.illposed.osc.OSCPortIn;
 
 import de.tud.kitchen.api.Kitchen;
 import de.tud.kitchen.api.event.EventPublisher;
 import de.tud.kitchen.api.event.acc.AccelerometerEvent;
 import de.tud.kitchen.api.module.KitchenModuleActivator;
 
 public class Activator extends KitchenModuleActivator {
 	OSCPortIn port;
 	EventPublisher<AccelerometerEvent> publisher;
 
 	@Override
 	public void start(Kitchen kitchen) {
 		System.out.println("bundle started " + OSCPort.defaultSCOSCPort());
 		try {
 			port = new OSCPortIn(57110);
 			System.out.println("assigned Port");
 			publisher = kitchen.getEventPublisher(AccelerometerEvent.class);
 			port.addListener("/wax/101", new parametricOscListener("101"));
 			port.addListener("/wax/102", new parametricOscListener("102"));
 			port.addListener("/wax/103", new parametricOscListener("103"));
 			port.addListener("/wax/104", new parametricOscListener("104"));
 			port.startListening();
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void stop() {
 		port.stopListening();
 		port.close();
 	}
 
 	private class parametricOscListener implements OSCListener {
 		String source;
		private long lastTime;
 
 		public parametricOscListener(String source) {
 			super();
 			this.source = source;
 		}
 
 		@Override
 		public void acceptMessage(Date arg0, OSCMessage arg1) {
			long time = ((Date) arg1.getArguments()[4]).getTime();
			if (time == 0) 
				time = lastTime+20;
			lastTime=time;
 			final AccelerometerEvent<Float> event = new AccelerometerEvent<Float>(source,
					time, 
 					(Float) arg1.getArguments()[0], 
 					(Float) arg1.getArguments()[1],
 					(Float) arg1.getArguments()[2]);
 			publisher.publish(event);
 		}
 
 	}
 }
