 package util.ast.node;
 
 import back_end.Visitor;
 
 /**
  * Represents the ArgumentExpressionList production. An ArgumentsNode contains a
  * list of IdNodes which specify the arguments for the function, method, or
  * constructor.
  * 
  * @author ben
  * 
  */
 public class ArgumentsNode extends ExpressionNode {
 	
 	protected ExpressionNode expNode;
	protected ExpressionNode argNode;
 
 	/**
 	 * 
 	 * @param args
 	 *            - a (possibly empty) subtree of additional ArgumentsNodes.
 	 * @param exp
 	 *            - a (not empty) expression representing one actual parameter
 	 *            to a function.
 	 *            
 	 *  Deepest node in the tree is the first argument
 	 */
	public ArgumentsNode(ExpressionNode args, ExpressionNode exp) {
 		super();
 		if (exp == null) {
 			throw new UnsupportedOperationException(
 					"Attempted to create an ArgumentsNode with a null ExpressionNode!");
 		}
 		this.expNode = exp;
 		this.argNode = args;
 		this.addChild(args);
 		this.addChild(exp);
 		ArgumentsNode.LOGGER.fine("Constructing ArgumentsNode");
 	}
 
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 	
	public ExpressionNode getArgumentsNode() {
 		return this.argNode;
 	}
 	
 	public ExpressionNode getExpressionNode() {
 		return this.expNode;
 	}
 
 	@Override
 	public String getName() {
 		return id + "-ArgumentsNode. newscope: " + isNewScope();
 	}
 
 	@Override
 	public int visitorTest(Visitor v) {
 		// TODO Auto-generated method stub
 		return 41;
 	}
 
 }
