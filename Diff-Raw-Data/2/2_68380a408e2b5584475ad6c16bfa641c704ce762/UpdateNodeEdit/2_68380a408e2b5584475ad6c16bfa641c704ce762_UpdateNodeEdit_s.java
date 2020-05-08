 package de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits;
 
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 
 public class UpdateNodeEdit extends Edit {
 	private final int index;
 	private final Node node;
 
 	public UpdateNodeEdit(Session session, int index, Node node) {
 		super(session);
 		this.index = index;
 		this.node = node;
 	}
 
 	@Override
 	public void perform() {
		session.getNodeModel();
 		session.notifyChangeListerners(Session.MODEL_CHANGE);
 	}
 }
