 package hypeerweb.visitors;
 
 import hypeerweb.Node;
 
 public class SendVisitor implements VisitorInterface{
 	protected int targetWebId;
 	
 	/**
 	 * Creates a new Send operation visitor
 	 * @param targetWebId the Node's WebID that we are searching for
 	 */
 	public SendVisitor(int targetWebId){
 		this.targetWebId = targetWebId;
 	}
 	
 	public SendVisitor(){
 		targetWebId = 0;
 	}
 
 	@Override
 	public void visit(Node n) {
		if (n.getWebId() == targetWebId)
 			performTargetOperation(n);
		else{
			performIntermediateOperation(n);
			Node next = n.getCloserNode(targetWebId);
			if (next != null)
				next.accept(this);
 		}
 	}
 	
 	protected void performTargetOperation(Node node){}
 	
 	protected void performIntermediateOperation(Node node){}
 }
