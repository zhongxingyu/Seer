 package kademlia.messages;
 
import kademlia.Identifier;
 import kademlia.Key;
 
 public class FindValueRequest extends FindRequest {
 	Key key;
 	final protected String command = "FIND_VALUE";
	@Override
	public Identifier getTargetIdentifier() {
		return key.getIdentifier();
	}
 	
 }
