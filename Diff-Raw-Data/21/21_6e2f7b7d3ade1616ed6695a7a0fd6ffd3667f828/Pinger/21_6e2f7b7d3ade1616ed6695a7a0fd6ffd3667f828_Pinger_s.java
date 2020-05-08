 package netproj.skeleton;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Logger;
 
 // Is something like this what you meant by pinger?
public class Pinger extends Device{
 	public void processMessage(Message msg) {
		if (this.getAddress() == msg.dest) {
			Link lnk;
 			// The return message switches the dest and source.
			Message retmsg = Message(msg.dest, msg.source, msg.size); 
 
 			// Find the link the message was sent from.
			for (Link link : links) {
 				List<Device> devices = link.getDevices();
 				for (Device device : devices) {
 					/* 
 					 * If the source of the message is connected to this link, then we can use
 					 * this link to send the return message.
 					 */
					if (device.getAddress() == msg.source) {
 						lnk = link;
 					}
 				}
 			}
 			
 			// Send the return message on a link that leads back to the original sender.
 			lnk.sendMessage(this, retmsg);
 		}
 	}
 	
 }
