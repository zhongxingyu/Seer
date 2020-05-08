 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 
 abstract class ChatRoomSkel extends ChatRoom implements ObjectImpl {
 	
 	protected String key;
 	
 	public ChatRoomSkel() {
 		
 	    Address addr = ORB.instance().address();
 	    ObjectReference ior = new ObjectReference ("IDL:Account:1.0", addr);
 	    objectReference (ior);
 	    key = ior.stringify();
 	    ORB.instance().registerObjectImpl(ior.stringify(),this);
 	}
 	
 	public ChatRoomSkel(String key) {
 		
 	    Address addr = ORB.instance().address();
 	    ObjectReference ior = new ObjectReference ("IDL:Account:1.0", addr);
 	    this.key = key;
 	    ior.updateIor(key);
 	    objectReference(ior);
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
 		if (req.opname().equals("getName")) {
 			String name = this.getName();
 			//Adiciono o nome no xml de reply
 			req.putStringReply(name);
 			
 			return true;
 		}else if (req.opname().equals("getUsers")) {
 			 Map users = getUsers();
 			
 			req.putSequenceReferenceReply(users);
 			
 			return true;
 		}else if (req.opname().equals("join")) {
 			String name = req.getString();
 			
 			String ior = req.getObjectReferenceFromSequence();
 			String object_host = req.getObjectHost();
 			String object_port = req.getObjectPort();
 			
 			//Criar o stub para o ChatUser
 			echo("obtendo o stub para o ChatUser ("+ior+") -> "+name);
 			ChatUser chatuser = null;
 			
 			ObjectReference chatuser_ref = new ObjectReference (ior, object_host, object_port);
 			
 			chatuser = new ChatUserStub(chatuser_ref);
 			
 			boolean result = join(name, chatuser);
 			req.putBooleanReply(result);
 			
 			return result;
 		}else if (req.opname().equals("leave")) {
 			String name = req.getString();
 			boolean result = leave(name);
 			req.putBooleanReply(result);
 			
 			return result;
 		}else if (req.opname().equals("send")) {
 			List sequence = req.getSequenceString();
 			String name = (String) sequence.get(0);
 			String message = (String) sequence.get(1);
 			req.putStringReply("send to "+name+" -> "+message);
 			send(name, message);
 			return true;
 		}
 		
 		return false;
 	}
 	
	protected String getKey(){
 		return key;
 	}
 
 }
