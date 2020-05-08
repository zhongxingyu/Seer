 package util.ast.node;
 
 import java.util.ArrayList;
 
 /**
  * A node representing the Unary Operator terminals in a parse tree.
  * 
  * @author sam
  *
  */
 public abstract class UnOpNode extends ExpressionNode {
 	
 	public static enum OpType {
 		UMINUS, NOT, RETURN, INCR, DECR
 	}
 	
 	protected OpType opType;
 	
 	public UnOpNode(Node child, OpType opType) {
 		this.children = new ArrayList<Node>();
		this.children.add(child);
 		this.opType = opType;
 	}
 	
 	public OpType getOpType() {
 		return opType;
 	}
 
 	@Override
 	public String getName() {
 		return opType.toString() + "<" + this.getTypeName() + ">";
 	}
 
 }
