package netproj.skeleton;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Logger;
 
 // Is something like this what you meant by pinger?
 public class Pinger extends Device{
 	private Packet prevpkt;
 	
 	public void sendPacket(Packet pkt, int gate) {
 		Link link;
 		synchronized (links) {
 			link = links.get(gate);
 		}
		link.sendPacket(this, pkt);
 		// Keep track of the packet we sent, so when we get a reply we know it's a reply to this packet.
 		prevpkt = pkt;
 	}
 	
 	public void processPacket(Packet pkt) {
 		// If this device isn't the intended recipient, we don't do anything.
		if (this.getAddress() == pkt.dest) {
 			// The return packet switches the dest and source.
			Packet retpkt = Packet(pkt.dest, pkt.source, pkt.size); 
 
 			// If the packet was a reply to the previous packet we sent, we don't need to reply
 			if (retpkt != prevpkt) {
 				// Broadcast the return packet, which sends it on all links, so we know 
 				// it will reach the original sender.
 				this.broadcastPacket(retpkt);
 			}
 		}
 	}
 	
 }
