 package se.miun.mediasense.disseminationlayer.communication;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import se.miun.mediasense.addinlayer.extensions.publishsubscribe.EndSubscribeMessage;
 import se.miun.mediasense.addinlayer.extensions.publishsubscribe.NotifySubscribersMessage;
 import se.miun.mediasense.addinlayer.extensions.publishsubscribe.StartSubscribeMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.broadcast.BroadcastMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.CheckPredecessorMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.CheckPredecessorResponseMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.CheckSuccessorMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.CheckSuccessorResponseMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.FindPredecessorMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.FindPredecessorResponseMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.JoinAckMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.JoinBusyMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.JoinMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.JoinResponseMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.NodeSuspiciousMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.NotifyJoinMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.NotifyLeaveMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.RegisterMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.RegisterResponseMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.ResolveMessage;
 import se.miun.mediasense.disseminationlayer.lookupservice.distributed.messages.unicast.ResolveResponseMessage;
 
 public abstract class Message {
 
 	//Magic words
 	public final static int MAGIC_WORD_UNICAST = 0xcafebabe;
 	public final static int MAGIC_WORD_BROADCAST = 0xdeadbeef;
 	
 	//Message types	
 	public final static int UNKNOWN = 0;
 	public final static int GET = 1;
 	public final static int SET = 2;
 	public final static int NOTIFY = 3;
 	public final static int STARTSUBSCRIBE = 6;
 	public final static int ENDSUBSCRIBE = 7;
 	//public final static int TRANSFER = 8;
 	public final static int NOTIFYSUBSCRIBERS = 9;
 	public final static int ACK = 10;
 
 	//DHT messages
 	public final static int BROADCAST = 17;
 
 	public final static int REGISTER = 4;
 	public final static int REGISTER_RESPONSE = 30;
 
 	public final static int RESOLVE = 5;
 	public final static int RESOLVE_RESPONSE = 15;
 	
 	public final static int JOIN = 11;
 	public final static int JOIN_RESPONSE = 12;
 	public final static int JOIN_ACK = 22;
 	public final static int JOIN_BUSY = 23;
 	public final static int JOIN_FINALIZE = 24;
 	public final static int DUPLICATE_NODE_ID = 16;
 	
 	public final static int KEEPALIVE = 13;
 	//public final static int KEEPALIVE_RESPONSE = 14; DOES NOT EXIST ANY LONGER
 	
 	public final static int NODE_JOIN_NOTIFY = 18;
 	public final static int NODE_LEAVE_NOTIFY = 19;
 	
 	public final static int NODE_SUSPICIOUS = 29;
 	
 	public final static int FIND_PREDECESSOR = 20;
 	public final static int FIND_PREDECESSOR_RESPONSE = 21;
 
 	public final static int CHECK_PREDECESSOR = 25;
 	public final static int CHECK_PREDECESSOR_RESPONSE = 26;
 
 	public final static int CHECK_SUCCESSOR = 27;
 	public final static int CHECK_SUCCESSOR_RESPONSE = 28;
 
 	//These were public first, the so called I-LIKE-TO-SOLVE-TRICKY-PROBLEMS-IN-THE-MIDDLE-OF-THE-NIGHT design pattern
 	//Changed to something better (someone owns me a beer for that)
 	private int type = Message.UNKNOWN;
 	private String fromIp = "";
 	private String toIp = "";
 	
 	public String getFromIp() {
 		return fromIp;
 	}
 	
 	public String getToIp() {
 		return toIp;
 	}
 	
 	public int getType() {
 		return type;
 	}
 	
 	public Message(String from,String to,int type) {
 		this.fromIp = from;
 		this.toIp = to;
 		this.type = type;
 	}
 	
 	public String toString() {
 		//Return type as number
 		return toString(new Integer(type).toString());
 	}
 	
 	protected String toString(String msgType) {
 		//Return message info
 		return "MSG: type: " + msgType + " - from: (" + fromIp + ") - to: (" + toIp + ")"; 
 	}
 	
 	public int getDataAmount() {
 		//2 x ip-address + 1 type
 		return 4 + 4 + 1;
 	}
 	
 	//Abstract methods for serialization
 	public void serializeMessage(ObjectOutputStream oos) {
 		try {
 			oos.writeInt(MAGIC_WORD_UNICAST);
 			oos.writeByte(type);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Message deserializeMessage(ObjectInputStream ois,String fromIp,String toIp) {
 		int type;
 		int magic_word;
 		
 		//Read magic word
 		try {
 			magic_word = ois.readInt();
 		}
 		catch (IOException e) {
 			return null;
 		}
 		
 		if(magic_word == MAGIC_WORD_UNICAST) {
 			//Read type information
 			try {
 				type = ois.readByte();
 			}
 			catch (IOException e) {
 				return null;
 			}
 
 			//Mapping types <-> classes
 			switch(type) {
 			case Message.GET: return GetMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.SET: return SetMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.NOTIFY: return NotifyMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.STARTSUBSCRIBE: return StartSubscribeMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.ENDSUBSCRIBE: return EndSubscribeMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.NOTIFYSUBSCRIBERS: return NotifySubscribersMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.REGISTER: return RegisterMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.REGISTER_RESPONSE: return RegisterResponseMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.RESOLVE: return ResolveMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.RESOLVE_RESPONSE: return ResolveResponseMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.JOIN: return JoinMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.JOIN_RESPONSE: return JoinResponseMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.JOIN_BUSY: return JoinBusyMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.JOIN_ACK: return JoinAckMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.NODE_JOIN_NOTIFY: return NotifyJoinMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.NODE_LEAVE_NOTIFY: return NotifyLeaveMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.NODE_SUSPICIOUS: return NodeSuspiciousMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.FIND_PREDECESSOR: return FindPredecessorMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.FIND_PREDECESSOR_RESPONSE: return FindPredecessorResponseMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.CHECK_PREDECESSOR: return CheckPredecessorMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.CHECK_PREDECESSOR_RESPONSE: return CheckPredecessorResponseMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.CHECK_SUCCESSOR: return CheckSuccessorMessage.deserializeMessage(ois,fromIp,toIp);
 			case Message.CHECK_SUCCESSOR_RESPONSE: return CheckSuccessorResponseMessage.deserializeMessage(ois,fromIp,toIp);
 			default: return null;
 			}
 		}
 		else if(magic_word == MAGIC_WORD_BROADCAST) {
 			//Forward to broadcast message
 			return BroadcastMessage.deserializeMessage(ois, fromIp, toIp);
 		}
 		else {
 			//Invalid data received
 			return null;
 		}
 	}
 }
