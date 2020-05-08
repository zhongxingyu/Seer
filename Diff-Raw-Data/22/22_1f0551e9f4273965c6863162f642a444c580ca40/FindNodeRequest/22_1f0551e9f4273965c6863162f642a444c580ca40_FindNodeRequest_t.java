 package kademlia.messages;
 
 import kademlia.Identifier;
 
 public class FindNodeRequest extends FindRequest {
 	protected Identifier targetIdentifier;
 	final protected String command = "FIND_NODE";
	@Override
	public Identifier getTargetIdentifier() {
		return targetIdentifier;
	}
 }
