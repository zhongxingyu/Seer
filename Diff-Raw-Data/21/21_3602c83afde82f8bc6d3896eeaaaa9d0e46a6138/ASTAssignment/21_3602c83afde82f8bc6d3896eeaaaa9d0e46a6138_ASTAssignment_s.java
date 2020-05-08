 package Interpreter;
 
 public class ASTAssignment extends SimpleNode implements VizParserTreeConstants{
   private String name;
   public ASTAssignment(int id) {
     super(id);
   }
 
   public ASTAssignment(VizParser p, int id) {
     super(p, id);
   }
   
   public String getCode()
   {
   	String code = jjtGetChild(0).getCode() + " = ";
   	code += jjtGetChild(1).getCode() + ";";
  	
   	return code;
   }
 
   public void setName(String name)
   {
   	this.name = name;
   }
   
   public String getName()
   {
   	return this.name;
   }
 
   /** Accept the visitor. **/
   public Object jjtAccept(VizParserVisitor visitor, Object data) {
     return visitor.visit(this, data);
   }
   
   /*************EVERYTHING BELOW HERE IS USED BY RANDOMIZINGVISITOR************/
   /**
    * rhs can be Op, Num, or Var node
    */
   public static ASTAssignment createAssignment(ASTVar lhs, Node rhs)
   {
 	  ASTAssignment assign = new ASTAssignment(JJTASSIGNMENT);
 	  
 	  assign.addChild(lhs, 0);
 	  
 	  if (lhs.getName() == null)
 		  throw new AssumptionFailedException();
 	  
 	  assign.setName(lhs.getName());
 	  
 	  
 	  assign.addChild(ASTExpression.createExpWithChild(rhs), 1);
 	  
 	  return assign;
   }
}
