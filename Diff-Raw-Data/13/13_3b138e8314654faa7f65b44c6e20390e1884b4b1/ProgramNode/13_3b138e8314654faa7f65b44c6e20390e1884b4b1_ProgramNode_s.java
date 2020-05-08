 import java.util.ArrayList;
 
 public class ProgramNode extends ASTNode
 {
         
         public ProgramNode(ArrayList<ASTNode> functionList, int yyline, int yycol)
         {
                 super(yyline, yycol);
                 if (functionList != null)
                         this.getChildren().addAll(functionList);
         }
 	public void checkSemantics() throws Exception
         {      
                 for (ASTNode function : this.getChildren())
                 {
                         function.checkSemantics();
                 }
                if (this.getChildCount() == 0 || !((FunctionNode)this.getChildAt(0)).getIdentifier().equalsIgnoreCase("main"))
                 {
                        throw new Exception("No function called main defined");
                 }
 
         }
         public String generateCode()
         {
                 return "";
         }
         
 }
