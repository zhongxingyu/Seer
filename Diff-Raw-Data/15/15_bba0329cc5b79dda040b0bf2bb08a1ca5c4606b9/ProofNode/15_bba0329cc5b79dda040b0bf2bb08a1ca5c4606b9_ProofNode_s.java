 
 public class ProofNode extends Node<Token> {
 
 	public ProofNode(Token token) {
 		super(token);
 	}
 	
 	public ProofNode(Token token, Node<Token> l , Node<Token> r) {
 		super(token, l, r);
 	}
 	
 	public void linkTo(ProofNode leftNode, ProofNode rightNode) {
 		this.left = leftNode;
 		this.right = rightNode;
 	}
 	
 	public String toString() {
		return "Node : " + this.data.getValue();
 	}
 	
 }
