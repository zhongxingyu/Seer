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
         public StringBuffer generateCode()
         {
         	StringBuffer output = new StringBuffer();
         	output.append("import java.util.ArrayList;\n");
         	
         	output.append("public class STL {\n");
         	output.append("public static void main(String[] args) {\n");
        	output.append("_smartestFunction_main();\n}\n");
             for (ASTNode function : this.getChildren())
             {
                     output.append(function.generateCode());
             }
         	
         	output.append("}");
         	
         	System.out.println("CODE OUTPUT --------------------------\n" + output);
         	
                 return null;
         }
         
 }
