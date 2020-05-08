 import java.util.ArrayList;
 
 public class StatementsNode extends ASTNode
 {
         public StatementsNode(int yyline, int yycol)
         {
                 super(yyline, yycol);
         }
 	public void checkSemantics() throws Exception
         {
                 for (ASTNode statement : this.getChildren())
                 {
                         statement.checkSemantics();
                 }
                 /*
                  * Check to make sure function has a return statement that 
                  * is guarenteed to be executed (if it is not a void function)
                  * and does not have any unreachable code
                  *
                  * The type of the return statement is verified in ReturnNode
                  */
                 
                 boolean hasReturn = false;
                 for (ASTNode statement : this.getChildren())
                 {
                         if (hasReturn == true)
                         {
                                 throw new Exception("Line: " + this.getYyline() + ": Unreachable code!");
                         }
                        if (statement.getType().equalsIgnoreCase("return"))
                         {
                                 hasReturn = true;
                         }
                 }
                 if (hasReturn == true)
                 {
                         setType("return");
                 }
         }
 	
         public String generateCode()
         {
                 return "";
         }
         
 }
