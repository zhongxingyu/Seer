 package se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.broadcast;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import se.miun.mediasense.disseminationlayer.communication.Message;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.NodeID;
 
 public abstract class BroadcastMessage extends Message {
 	
 	//Definition of the region the broadcast is responsible for
 	private NodeID startKey;
 	private NodeID endKey;
 	
 	public BroadcastMessage(String from, String to,NodeID startKey,NodeID endKey,int internalType) {
 		super(from,to,Message.BROADCAST);
 		this.startKey = startKey;
 		this.endKey = endKey;
 	}
 	
 	public NodeID getStartKey() {
 		return startKey;
 	}
 
 	public NodeID getEndKey() {
 		return endKey;
 	}
 
 	protected String toString(String text) {
 		return "BROADCAST:" + startKey.toString() + " -> " + endKey.toString() + " | " + extractMessage().toString();
 	}
 	
 	//Extract unicast message
 	public abstract Message extractMessage();
 	
 	//Clone with new addresses to easily forward messages
 	public abstract BroadcastMessage cloneWithNewAddresses(String from,String to,NodeID startKey,NodeID endKey);
 
 	//Return packet size for statistic
 	public int getDataAmount() {
 		return super.getDataAmount() + 2 * NodeID.ADDRESS_SIZE;
 	}
 	
 	//Abstract methods for serialization
 	@Override
 	public void serializeMessage(ObjectOutputStream oos) {
 		try {
 			oos.writeInt(MAGIC_WORD_BROADCAST);
			oos.writeByte(getType());
 			oos.write(startKey.getID());
 			oos.write(endKey.getID());
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Message deserializeMessage(ObjectInputStream ois,String fromIp,String toIp) {
 		int type;
 		byte[] startKeyArray;
 		byte[] endKeyArray;
 		NodeID startKey;		
 		NodeID endKey;
 		
 		//Read type information
 		try {
 			type = ois.readByte();
 			startKeyArray = new byte[NodeID.ADDRESS_SIZE];
 			endKeyArray = new byte[NodeID.ADDRESS_SIZE];
 
 			ois.readFully(startKeyArray, 0, NodeID.ADDRESS_SIZE);
 			ois.readFully(endKeyArray, 0, NodeID.ADDRESS_SIZE);
 			
 			startKey = new NodeID(startKeyArray);
 			endKey = new NodeID(endKeyArray);
 		}
 		catch (IOException e) {
 			return null;
 		}
 		
 		//Mapping types <-> classes
 		switch(type) {
 		case Message.KEEPALIVE: return KeepAliveBroadcastMessage.deserializeMessage(ois,fromIp,toIp,startKey,endKey);
 		case Message.NODE_SUSPICIOUS: return NodeSuspiciousBroadcastMessage.deserializeMessage(ois,fromIp,toIp,startKey,endKey);
 		case Message.NODE_JOIN_NOTIFY: return NotifyJoinBroadcastMessage.deserializeMessage(ois,fromIp,toIp,startKey,endKey);
 		case Message.NODE_LEAVE_NOTIFY: return NotifyLeaveBroadcastMessage.deserializeMessage(ois,fromIp,toIp,startKey,endKey);
 		default: return null;
 		}
 	}
 }
