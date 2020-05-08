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
  * Visitor class for generating Java source.
  * 
  * This is the third (and final) walk performed after construction of the AST from source.
  * CodeGeneratingVisitor generates a massive String representing the translated Hog program.
  * 
  * 
  * @author kurry
  *
  */
 public class CodeGeneratingVisitor implements Visitor{
 	
 	
 	@Override
 	public void visit(BiOpNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(ConstantNode node){
 		
 		System.out.println(node.getType());
 		
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(ExpressionNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(IdNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(MockNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(MockExpressionNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(Node node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(ParametersNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(PrimaryExpressionNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(RelationalExpressionNode node){
 		//node specific code generation operations here
 	}
 	
 	@Override
 	public void visit(UnOpNode node){
 		//node specific code generation operations here
 	}
 
 	@Override
 	public void visit(ArgumentsNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(CatchesNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(DerivedTypeNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(ElseIfStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(ElseStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(ExceptionTypeNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(FunctionNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(GuardingStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(IfElseStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(IterationStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(JumpStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(PostfixExpressionNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(PrimitiveTypeNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(ProgramNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(SectionNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(SectionTypeNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(SelectionStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(StatementListNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(StatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(SwitchStatementNode node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void visit(TypeNode node) {
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
