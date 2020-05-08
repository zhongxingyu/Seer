 package fhbrs.ooka.ateam.components.simple;
 
 import fhbrs.ooka.ateam.components.AbstractComponent;
 
 public class SimpleComponent extends AbstractComponent {
 	public SimpleComponent() {
 		super("Simple Component", 1, "Doesn't do very much else than saying, that it was started and stopped.");
 	}
 	
 	@Override
 	public void start() throws Exception {
 		super.start();
 		System.out.println(getName() + " was started successfully.");
 	}
 
 	@Override
 	public void stop() throws Exception {
 		super.stop();
 		System.out.println(getName() + " was stopped successfully.");
 	}
 
 }
