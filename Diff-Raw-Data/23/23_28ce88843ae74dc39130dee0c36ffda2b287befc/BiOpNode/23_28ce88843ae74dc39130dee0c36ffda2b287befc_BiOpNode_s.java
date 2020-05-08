 package util.ast.node;
 
 import java.util.ArrayList;
 
 /**
  * A node representing the Binary Operator terminals in a parse tree.
  * 
  * Note: while not prescribed here, it is assumed that all classes that inherit from BiOpNode
  * have a method evaluate() that returns the evaluation of the expression node. The type of this
  * method should be determined by the type of the node (i.e. multiplicativeExpressionNodes return
  * a number type, and booleanExpressionNodes return a boolean type).
  * 
  * @author sam
  *
  */
 public abstract class BiOpNode extends ExpressionNode {
 	
 	public static enum OpType{
 		MINUS, PLUS, DIVIDE, TIMES, MOD
 	}
 	
 	protected OpType opType;
 	
 	public BiOpNode(OpType type, Node left, Node right) {
 		this.children = new ArrayList<Node>();
		this.children.add(left);
		this.children.add(right);
 		this.opType = type;	
 	}
 	
 	public OpType getOpType() {
 		return opType;
 	}
 	
 	public void setOpType(OpType type) {
 		this.opType = type;
 	}
 
 	@Override
 	public String getName() {
 		return opType.toString() + "<" + this.getTypeName() + ">";
 	}
 
 }
