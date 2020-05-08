 package netproj.hosts;
 
 import java.util.ArrayList;
 import netproj.skeleton.Device;
 import netproj.skeleton.Packet;
 import netproj.skeleton.Link;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Logger;
 
 // Is something like this what you meant by pinger?
 public class Pinger extends Device{
 	private Packet prevpkt;
	private ArrayList<Link> links;
 
 	public Pinger(int inputBuffSize, int outputBuffSize, int address) {
 		super(inputBuffSize, outputBuffSize, address);
		links = new ArrayList<Link>();
 	}
 	
 	@Override
 	public void sendPacket(Packet pkt, int gate) {
 		Link link;
 		synchronized (links) {
 			link = links.get(gate);
 		}
 		link.sendMessage(this, pkt);
 		// Keep track of the packet we sent, so when we get a reply we know it's a reply to this packet.
 		prevpkt = pkt;
 	}
	
 	@Override
 	public void processPacket(Packet pkt) {
 		// If this device isn't the intended recipient, we don't do anything.
 		if (this.getAddress() == pkt.getDestAddress()) {
 			// The return packet switches the dest and source.
 			Packet retpkt = new Packet(pkt.getDestAddress(), pkt.getSourceAddress(), pkt.getSizeInBits()); 
 
 			// If the packet was a reply to the previous packet we sent, we don't need to reply
 			if (retpkt != prevpkt) {
 				// Broadcast the return packet, which sends it on all links, so we know 
 				// it will reach the original sender.
 				this.broadcastPacket(retpkt);
 			}
 		}
 	}
 	
 }
