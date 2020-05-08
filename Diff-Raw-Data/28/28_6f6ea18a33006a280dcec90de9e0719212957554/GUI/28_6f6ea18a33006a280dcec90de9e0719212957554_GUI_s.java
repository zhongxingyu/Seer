 package edu.mst.distopsysproj.gui;
 
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import edu.mst.distopsysproj.person.Location;
 import edu.mst.distopsysproj.person.Person;
 import edu.mst.distopsysproj.util.Bridge;
 
 public class GUI extends Agent {
 	private static final long serialVersionUID = -8484372606650826462L;
 	
 	private BridgeCrossingFrame frame;
 	private Bridge bridge;
 
 	@Override
 	protected void setup() {
 		frame = new BridgeCrossingFrame(Person.persons);
 		bridge = Bridge.getInstance();
 		addBehaviour(new UpdateGUIBehavior());
 	}
 	
 	@Override
 	protected void takeDown() {
 		frame.dispose();
 		System.out.println("Bye! " + getAID().getName() + " is shutting down!");
 	}
 	
 	private class UpdateGUIBehavior extends CyclicBehaviour {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void action() {
			System.out.println("GUI will execute and remove the queue's first element");
 			String[] data = bridge.pollBridge();
 			if(data != null){			
 				frame.setCircleToPosition(data[0], Location.valueOf(data[1]));
 			}
 		}	
 	}
 }
