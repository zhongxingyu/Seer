 /**
  * 
  */
 package back_end;
 
import util.ast.AbstractSyntaxTree;
 import util.ast.node.ArgumentsNode;
 import util.ast.node.BiOpNode;
 import util.ast.node.CatchesNode;
 import util.ast.node.ConstantNode;
 import util.ast.node.DerivedTypeNode;
 import util.ast.node.DictTypeNode;
 import util.ast.node.ElseIfStatementNode;
 import util.ast.node.ElseStatementNode;
 import util.ast.node.ExceptionTypeNode;
 import util.ast.node.ExpressionNode;
 import util.ast.node.FunctionNode;
 import util.ast.node.GuardingStatementNode;
 import util.ast.node.IdNode;
 import util.ast.node.IfElseStatementNode;
 import util.ast.node.IterationStatementNode;
 import util.ast.node.JumpStatementNode;
 import util.ast.node.MockExpressionNode;
 import util.ast.node.MockNode;
 import util.ast.node.Node;
 import util.ast.node.ParametersNode;
 import util.ast.node.PostfixExpressionNode;
 import util.ast.node.PrimaryExpressionNode;
 import util.ast.node.PrimitiveTypeNode;
 import util.ast.node.ProgramNode;
 import util.ast.node.RelationalExpressionNode;
 import util.ast.node.ReservedWordTypeNode;
 import util.ast.node.SectionNode;
 import util.ast.node.SectionTypeNode;
 import util.ast.node.SelectionStatementNode;
 import util.ast.node.StatementListNode;
 import util.ast.node.StatementNode;
 import util.ast.node.SwitchStatementNode;
 import util.ast.node.TypeNode;
 import util.ast.node.UnOpNode;
 
 /**
  * Visitor class for error checking.
  * 
  * This is the second walk performed after construction of the AST from source.
  * 
  * Performs the following validations:
  * - no dead code (statements after a return statement in the same basic block)
  * - no break/continue statements outside of iteration loops
  * - non-void functions have adequate number of return statements
  * - no case/default statements outside of immediate switch statement
  * 
  * @author sam
  *
  */
 public class ErrorCheckingVisitor implements Visitor {
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ArgumentsNode)
 	 */
 	@Override
 	public void visit(ArgumentsNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.BiOpNode)
 	 */
 	@Override
 	public void visit(BiOpNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.CatchesNode)
 	 */
 	@Override
 	public void visit(CatchesNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ConstantNode)
 	 */
 	@Override
 	public void visit(ConstantNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.DerivedTypeNode)
 	 */
 	@Override
 	public void visit(DerivedTypeNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ElseIfStatementNode)
 	 */
 	@Override
 	public void visit(ElseIfStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ElseStatementNode)
 	 */
 	@Override
 	public void visit(ElseStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ExceptionTypeNode)
 	 */
 	@Override
 	public void visit(ExceptionTypeNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ExpressionNode)
 	 */
 	@Override
 	public void visit(ExpressionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.FunctionNode)
 	 */
 	@Override
 	public void visit(FunctionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.GuardingStatementNode)
 	 */
 	@Override
 	public void visit(GuardingStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.IdNode)
 	 */
 	@Override
 	public void visit(IdNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.IfElseStatementNode)
 	 */
 	@Override
 	public void visit(IfElseStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.IterationStatementNode)
 	 */
 	@Override
 	public void visit(IterationStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.JumpStatementNode)
 	 */
 	@Override
 	public void visit(JumpStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.MockExpressionNode)
 	 */
 	@Override
 	public void visit(MockExpressionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.MockNode)
 	 */
 	@Override
 	public void visit(MockNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.Node)
 	 */
 	@Override
 	public void visit(Node node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ParametersNode)
 	 */
 	@Override
 	public void visit(ParametersNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.PostfixExpressionNode)
 	 */
 	@Override
 	public void visit(PostfixExpressionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.PrimaryExpressionNode)
 	 */
 	@Override
 	public void visit(PrimaryExpressionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.PrimitiveTypeNode)
 	 */
 	@Override
 	public void visit(PrimitiveTypeNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.ProgramNode)
 	 */
 	@Override
 	public void visit(ProgramNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.RelationalExpressionNode)
 	 */
 	@Override
 	public void visit(RelationalExpressionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.SectionNode)
 	 */
 	@Override
 	public void visit(SectionNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.SectionTypeNode)
 	 */
 	@Override
 	public void visit(SectionTypeNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.SelectionStatementNode)
 	 */
 	@Override
 	public void visit(SelectionStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.StatementListNode)
 	 */
 	@Override
 	public void visit(StatementListNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.StatementNode)
 	 */
 	@Override
 	public void visit(StatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.SwitchStatementNode)
 	 */
 	@Override
 	public void visit(SwitchStatementNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.TypeNode)
 	 */
 	@Override
 	public void visit(TypeNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see back_end.Visitor#visit(util.ast.node.UnOpNode)
 	 */
 	@Override
 	public void visit(UnOpNode node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void visit(DictTypeNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(ReservedWordTypeNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
	public void walk(AbstractSyntaxTree tree) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
