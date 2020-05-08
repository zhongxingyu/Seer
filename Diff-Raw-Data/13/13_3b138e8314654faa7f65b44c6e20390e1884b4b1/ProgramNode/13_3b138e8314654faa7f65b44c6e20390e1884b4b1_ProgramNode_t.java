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
                if (! Parser.functionSymbolsTable.containsKey("main") )
                 {
                        throw new Exception(this.getYyline() + ":" + this.getYycolumn() + ": No function called main defined");
                 }
 
         }
         public String generateCode()
         {
                 return "";
         }
         
 }
