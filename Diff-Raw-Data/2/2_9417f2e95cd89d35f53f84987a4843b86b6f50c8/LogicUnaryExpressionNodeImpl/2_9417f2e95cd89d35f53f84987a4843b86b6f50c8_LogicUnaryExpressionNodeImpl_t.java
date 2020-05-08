 package swp_compiler_ss13.fuc.ast;
 
 import swp_compiler_ss13.common.ast.nodes.unary.ArithmeticUnaryExpressionNode;
 
 /**
  * LogicUnaryExpressionNode implementation
  * 
  * @author "Frank Zechert, Danny Maasch"
  * @version 1
  */
public class LogicUnaryExpressionNodeImpl extends UnaryExpressionNodeImpl implements LogicUnaryExpressionNode {
 
 	@Override
 	public ASTNodeType getNodeType() {
 		return ASTNodeType.LogicUnaryExpressionNode;
 	}
 
 }
