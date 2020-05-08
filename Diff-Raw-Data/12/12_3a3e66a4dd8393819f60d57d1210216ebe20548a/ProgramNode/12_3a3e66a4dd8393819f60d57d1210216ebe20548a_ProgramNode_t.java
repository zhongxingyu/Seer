 import java.util.ArrayList;
 
 public class ProgramNode extends ASTNode
 {
         
         public ProgramNode(ArrayList<ASTNode> functionList, int yyline, int yycol)
         {
                 super(yyline, yycol);
                 if (functionList != null)
                         this.children.addAll(functionList);
         }
	public void checkSemantics() throws Exception
         {      
                 for (ASTNode function : this.children)
                 {
                         function.semanticCheck();
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
