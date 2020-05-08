 package edu.berkeley.grippus.fs;
 
 import edu.berkeley.grippus.server.NodeRef;
 
 public class DPermission implements Permission {
 	private static final long serialVersionUID = 1L;
 	private final NodeRef owner;
 	public DPermission(NodeRef owner) {
 		this.owner = owner;
 	}

	public NodeRef getOwner() {
		return owner;
	}
 }
