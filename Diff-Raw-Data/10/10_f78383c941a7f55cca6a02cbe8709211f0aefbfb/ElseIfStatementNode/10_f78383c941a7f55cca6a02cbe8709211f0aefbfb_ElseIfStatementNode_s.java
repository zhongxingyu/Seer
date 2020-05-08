 package util.ast.node;
<<<<<<< HEAD
 /**
  * An ElseIfStatementNode represents an else-if statement
  * and contains the statement if the condition is true, and
  * the statement if the condition is true.
  * 
  * @author ktran
  *
  */
=======

import back_end.Visitor;

>>>>>>> 61ea79bc26dedb49f6db4d7392679feb44d650cf
 public class ElseIfStatementNode extends SelectionStatementNode {
 
 	protected StatementNode ifCondTrue;
 	protected StatementNode ifCondFalse;
 
 	public ElseIfStatementNode(ExpressionNode condition,
 			StatementListNode checkNext, ElseIfStatementNode ifCondFalse) {
 		super(condition);
 		this.addChild(ifCondTrue);
 		this.addChild(checkNext);
 		this.addChild(ifCondFalse);
 	}
 
 	@Override
 	public String getName() {
 		return "ElseIfStatementNode";
 	}
 	
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 }
