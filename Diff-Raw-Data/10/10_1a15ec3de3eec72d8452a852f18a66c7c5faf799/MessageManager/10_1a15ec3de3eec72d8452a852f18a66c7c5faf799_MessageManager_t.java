 package twpx.core;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class MessageManager {
 	
 	public static Map<Integer, Protocol> protocols = new HashMap<Integer, Protocol>();
 	
 	// todo
 	public static void send(TWPConnection con, Message message, boolean withHeader) throws IOException {
 		
 		if (withHeader) {
 			con.writeMagicBytes();
 			con.writeProtocolId(message.getProtocolId());
 		}
 		
 		con.writeMessageId(message.getId());
 		
		// writeGeneric
 		Iterator<TWPParameter> iterator = message.getParameters().iterator();
 		while (iterator.hasNext()) {
 			TWPParameter parameter = iterator.next();
 			if (parameter.type == "string") {
 				con.writeString((String) message.get(parameter.name));
 			}
 			if (parameter.type == "int") {
 				con.writeInteger((Integer) message.get(parameter.name));
 			}
 		}
 		
 		con.writeEndOfMessage();
 	}
 	
 	// todo
 	public static Message receive(TWPConnection con, boolean withHeader, int protocolId) throws IOException {
 		int protocol;
 
 		if (withHeader) {
 			con.readMagicBytes();
 			protocol = con.readProtocolId(); // todo
 		} else {
 			protocol = protocolId; // t
 		}
 		
 		int id = con.readMessageId();
 		
 		Message message = MessageManager.protocols.get(protocol).getMessages().get(id); // clone?
 		
		// readGeneric
 		Iterator<TWPParameter> iterator = message.getParameters().iterator();
 		while (iterator.hasNext()) {
 			TWPParameter parameter = iterator.next();
 			if (parameter.type == "string") {
 				message.set(parameter.name, con.readString());
 			}
 			if (parameter.type == "int") {
 				message.set(parameter.name, con.readInteger());
 			}
 		}
 		
 		con.readEndOfMessage();
 		
 		return message;
 	}
 
 	public static void register(Protocol protocol) {
 		protocols.put(new Integer(protocol.getId()), protocol);
 	}
 }
