 import java.util.List;
 import java.util.Map;
 
 
 abstract class RoomRegistrySkel extends RoomRegistry implements ObjectImpl {
 	
 	protected String key;
 	
 	public RoomRegistrySkel() {
 		
 	    Address addr = ORB.instance().address();
 	    ObjectReference ior = new ObjectReference ("IDL:Account:1.0", addr);
 	    objectReference (ior);
 	    ORB.instance().registerObjectImpl(ior.stringify(),this);
 	}
 	
 	public RoomRegistrySkel(String key) {
 	    Address addr = ORB.instance().address();
 	    ObjectReference ior = new ObjectReference ("IDL:Account:1.0", addr);
 	    ior.updateIor(key);
 	    this.key = key;
 	    objectReference (ior);
 	    ORB.instance().registerObjectImpl(key,this);
 	}
 
 	public void invoke(ServerRequest req) {
 		boolean a = dispatch(req);
 		assert (a):"dispatch Error";
 	}
 	
 	protected boolean dispatch(ServerRequest req) {
 		
 		/**
 		 * TODO: implementar as operacoes
 		 */
 		if (req.opname().equals("newRoom")) {
 			String name = req.getString();
 			//Adiciono o nome no xml de reply
 			ChatRoom chatroom = newRoom(name);
 			
 			req.putObjectReferenceReply(chatroom.objectReference().getXmlReference());
 
 			return true;
 		}else if (req.opname().equals("findRoom")) {
 			String name = req.getString();
 			ChatRoom chatroom = findRoom(name);
 			if (chatroom == null) {
 				//echo("room ["+name+"] nao existe!");
 				req.putNullObjectReferenceReply();
 			}else{
 				req.putObjectReferenceReply(chatroom.objectReference().getXmlReference());
 			}
 			
 			return true;
 		}else if (req.opname().equals("getRooms")) {
 			Map rooms = getRooms();
 			req.putSequenceReferenceReply(rooms);
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
	public String getKey(){
 		return key;
 	}
 	
 	
 }
