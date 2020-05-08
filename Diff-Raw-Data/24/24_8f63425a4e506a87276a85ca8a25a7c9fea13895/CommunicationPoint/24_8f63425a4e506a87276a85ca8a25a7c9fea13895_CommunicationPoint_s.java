 package communication;
 
 import communication.api.Message;
 import communication.api.Serializer;
 
 import hydna.ntnu.student.api.HydnaApi;
 import hydna.ntnu.student.listener.api.HydnaListener;
 
 /**
  * Simple class for communication through the Hydna service API
  */
 public abstract class CommunicationPoint {
 
 	protected HydnaApi hydnaSvc;
 	protected HydnaListener listener;
 	protected String id;
 	protected String type;
 	protected String location;
 	protected boolean registered = false;
 
 	
 	/**
 	 * Set id, location and type before calling this
 	 */
 	protected void setUp() {
 		this.listener = new HydnaListener() {
 			
 			@Override
 			public void systemMessage(String msg) {
 				System.out.println("got sysmsg: "+msg);
 			}
 			
 			@Override
 			public void signalRecieved(String msg) {
 				System.out.println("got signal: "+msg);
 			}
 			
 			@Override
 			public void messageRecieved(String msg) {
 				System.out.println(msg);
 				Message m = Serializer.deserialize(msg);
 				handleMessage(m);
 			}
 		};
 		hydnaSvc.registerListener(this.listener);
 		hydnaSvc.stayConnected(true);
 		hydnaSvc.connectChannel("ttm3-access-control.hydna.net/"+this.location, "rwe");
 		registerCommunicationPoint();
 	}
 	
 	protected abstract void handleMessage(Message msg);
 	
 	protected abstract void registerCommunicationPoint();
 	
 	public String getId() {
 		return this.id;
 	}
 	
 }
