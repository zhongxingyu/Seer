 import java.util.ArrayList;
 
 public class ProgramNode extends ASTNode
 {
         
         public ProgramNode(ArrayList<ASTNode> functionList, int yyline, int yycol)
         {
                 super(yyline, yycol);
                 if (functionList != null)
                         this.children.addAll(functionList);
         }
	public void semanticCheck()
         {      
                 for (ASTNode function : this.children)
                 {
                         function.semanticCheck();
                 }
                if (this.children.getChildCount() == 0 || !((FunctionNode)this.children.getChildAt(0)).getIdentifier().equalsIgnoreCase("main"))
                 {
                         throw new Exception("No function called main defined");
                 }
 
         }
         public String generateCode()
         {
                 return "";
         }
         
 }
