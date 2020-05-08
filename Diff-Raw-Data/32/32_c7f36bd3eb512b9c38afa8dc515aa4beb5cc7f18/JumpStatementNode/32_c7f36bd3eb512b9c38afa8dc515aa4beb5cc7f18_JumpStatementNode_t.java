 package util.ast.node;
 
 import back_end.Visitor;
 
 /**
  * A JumpStatementNode represents either a Continue, Break, or Return construct,
  * and returns have expression statements.
  * 
  * 
  * @author ben
  * 
  */
 public class JumpStatementNode extends StatementNode {
 
 	public static enum JumpType {
 		CONTINUE, BREAK, RETURN;
 	}
 
 	protected JumpType jumpType;
 	protected StatementNode expression;
 
 	public JumpStatementNode(JumpType jumpType, StatementNode e) {
 		this(jumpType);
 		if (jumpType != JumpType.RETURN) {
 			throw new UnsupportedOperationException(
 					"Tried to create a JumpStatement node with " + jumpType
 							+ "jump type, and expression node " + e
 							+ ". But there shouldn't be an expression node.");
 		}
		this.expression = e;
 		this.addChild(e);
 	}
 
 	public JumpStatementNode(JumpType jumpType) {
 		this.jumpType = jumpType;
 	}
 
 	public JumpType getJumpType() {
 		return jumpType;
 	}
 
 	public StatementNode getExpressionNode() {
 		return expression;
 	}
 
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 	@Override
 	public String getName() {
 		return id + "-JumpStatement: " + jumpType.toString();
 	}
 
 	@Override
 	public int visitorTest(Visitor v) {
 		return 0;
 	}
 }
