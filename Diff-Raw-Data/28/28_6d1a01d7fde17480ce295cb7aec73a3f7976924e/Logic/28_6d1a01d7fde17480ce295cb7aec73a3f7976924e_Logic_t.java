 package AP2DX;
 
 import java.lang.Thread;
 import java.util.ArrayList;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * This class defines the Logic engine of each module. It calls the
  * ``processLogic'' module of a class, to think of a reaction.
  */
 public class Logic extends Thread {
 	/** The base class, in which we can run the componentLogic */
 	private AP2DXBase base;
 
 	/**
 	 * The constructor. For now it only takes moduleID, but i'm sure it will
 	 * need some more things later
 	 */
 	public Logic(AP2DXBase base) {
 		this.base = base;
 	}
 
 	/**
 	 * Where the magic happens. This will be on all the time, calling
 	 * componentLogic to find a reaction
 	 */
 	public void run() {
 		while (true) {
 			Message message = null;
 			// Get the message from the base
 			try {
 				message = base.getReceiveQueue().take();
 			} catch (Exception e) {
 				e.printStackTrace();
 				base.logger
 						.severe("Error in Logic.run, attempted to retrieve item out base.receiveQueue");
 			}
 			// run componentLogic with the message
 			ArrayList<Message> actions = base.componentLogic(message);
 			if (!actions.isEmpty()) {
 				for (Message action : actions) {
 					try {
 						ConnectionHandler connection = base
 								.getSendConnection(action
 										.getDestinationModuleId());
 						connection.sendMessage(action);
 					} catch (Exception e) {
 						e.printStackTrace();
 						base.logger
 								.severe("Error in Logic.run, attempted get the connection of action: "
 										+ action);
 					}
 				}
 			}
            else
                break;
 		}
 	}
 }
