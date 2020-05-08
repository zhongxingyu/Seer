 package zinara.ast.instructions;
 
import zinara.ast.expression.Expression;

 public class Assignation extends Instruction{
     private String id;
     private Expression expr;
 
     public Assignation(String name, Expression ex){
 	this.id = name;
 	this.expr = ex;
     }
 
     public String getName(){
 	return this.id;
     }
 
     public Expression getExpression(){
 	return this.expr;
     }
 }
