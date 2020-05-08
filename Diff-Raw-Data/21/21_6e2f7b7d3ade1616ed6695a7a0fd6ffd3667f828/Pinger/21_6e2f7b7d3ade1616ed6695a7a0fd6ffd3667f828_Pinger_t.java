 package netproj.skeleton;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Logger;
 
 // Is something like this what you meant by pinger?
public class Pinger extends Device {
	public Pinger(int inputBuffSize, int address) {
		super(inputBuffSize, address);
		// TODO Auto-generated constructor stub
	}

 	public void processMessage(Message msg) {
		if (this.getAddress() == msg.getDestAddress()) {
			Link lnk = null;
 			// The return message switches the dest and source.
			Message retmsg = new Message(msg.getDestAddress(), msg.getSourceAddress(), msg.getSizeInBits()); 
 
 			// Find the link the message was sent from.
			for (Link link : getLinks()) {
 				List<Device> devices = link.getDevices();
 				for (Device device : devices) {
 					/* 
 					 * If the source of the message is connected to this link, then we can use
 					 * this link to send the return message.
 					 */
					if (device.getAddress() == msg.getSourceAddress()) {
 						lnk = link;
 					}
 				}
 			}
 			
 			// Send the return message on a link that leads back to the original sender.
 			lnk.sendMessage(this, retmsg);
 		}
 	}
 	
 }
