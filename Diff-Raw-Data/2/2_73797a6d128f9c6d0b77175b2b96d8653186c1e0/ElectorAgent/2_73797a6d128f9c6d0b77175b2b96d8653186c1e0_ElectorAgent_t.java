 package br.ufla.dcc.mac.backbone.packet;
 
 import br.ufla.dcc.grubix.simulator.Address;
 import br.ufla.dcc.grubix.simulator.NodeId;
 import br.ufla.dcc.grubix.simulator.node.Node;
 
 public abstract class ElectorAgent extends MACAgent {
 
 	public ElectorAgent(Address sender, NodeId receiver, double signalStrength, int hopsEquality) {
 		super(sender, receiver, PacketType.DATA, signalStrength, hopsEquality);
 	}
 
 	public abstract double evaluate(Node node);
 
 	public boolean isEqualSinceHop(int hop) {
		return hop + this.getHopsEquality() > this.getHops();
 	}
 }
