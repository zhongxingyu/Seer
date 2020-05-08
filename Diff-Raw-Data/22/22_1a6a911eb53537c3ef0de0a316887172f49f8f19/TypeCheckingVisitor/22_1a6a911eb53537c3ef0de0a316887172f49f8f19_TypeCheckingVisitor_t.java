 package back_end;
 
 import util.ast.node.AdditiveExpressionNode;
 import util.ast.node.BiOpNode;
 import util.ast.node.CastExpressionNode;
 import util.ast.node.ConstantNode;
 import util.ast.node.EqualityExpressionNode;
 import util.ast.node.ExpressionNode;
 import util.ast.node.IdNode;
 import util.ast.node.LogicalExpressionNode;
 import util.ast.node.LogicalTermNode;
 import util.ast.node.MockExpressionNode;
 import util.ast.node.MockNode;
 import util.ast.node.MultiplicativeExpressionNode;
 import util.ast.node.Node;
 import util.ast.node.ParametersNode;
 import util.ast.node.PostfixExpressionNode;
 import util.ast.node.PrimaryExpressionNode;
 import util.ast.node.RelationalExpressionNode;
 import util.ast.node.UnOpNode;
 import util.ast.node.UnaryExpressionNode;
 
 public class TypeCheckingVisitor extends Visitor{
 	
 	@Override
 	public void visit(AdditiveExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking bi op node");
 	}
 	
 	@Override
 	public void visit(BiOpNode node){
 		//node specific type checking operations here
 		System.out.println("type checking bi op node");
 	}
 	
 	@Override
 	public void visit(CastExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking cast exp node");
 	}
 	
 	@Override
 	public void visit(ConstantNode node){
 		//node specific type checking operations here
 		System.out.println("type checking constant node");
 	}
 	
 	@Override
 	public void visit(EqualityExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking bi op node");
 	}
 	
 	@Override
 	public void visit(ExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking expression node");
 	}
 	
 	@Override
 	public void visit(IdNode node){
 		//node specific type checking operations here
 		System.out.println("type checking id node");
 	}
 	
 	@Override
 	public void visit(LogicalExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking id node");
 	}
 	
 	@Override
 	public void visit(LogicalTermNode node){
 		//node specific type checking operations here
 		System.out.println("type checking id node");
 	}
 	
 	@Override
 	public void visit(MockNode node){
 		//node specific type checking operations here
		System.out.println("type checking mock node");
 	}
 	
 	@Override
 	public void visit(MockExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking id node");
 	}
 	
 	@Override
 	public void visit(MultiplicativeExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking mult expr node");
 	}
 	
 	@Override
 	public void visit(Node node){
 		//node specific type checking operations here
 		System.out.println("type checking Node node");
 	}
 	
 	@Override
 	public void visit(ParametersNode node){
 		//node specific type checking operations here
 		System.out.println("type checking parameters node");
 	}
 	
 	@Override
 	public void visit(PostfixExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking postfix expr node");
 	}
 	
 	@Override
 	public void visit(PrimaryExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking primary exp node");
 	}
 	
 	@Override
 	public void visit(RelationalExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking id node");
 	}
 	
 	@Override
 	public void visit(UnaryExpressionNode node){
 		//node specific type checking operations here
 		System.out.println("type checking unary exp node");
 	}
 	
 	@Override
 	public void visit(UnOpNode node){
 		//node specific type checking operations here
 		System.out.println("type checking unary op node");
 	}
 	
 }
