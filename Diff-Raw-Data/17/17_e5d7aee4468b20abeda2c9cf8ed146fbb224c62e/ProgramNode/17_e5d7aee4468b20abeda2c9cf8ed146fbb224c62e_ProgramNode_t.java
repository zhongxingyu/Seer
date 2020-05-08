 package smartest;
 
 import java.util.ArrayList;
 
 /**
  * The top-level AST Node that implements semantic checking and target code
  * generation of an STL program. The children are nodes representing all
  * function definitions for this program.
  * 
  * @author Daniel Walker
  */
 public class ProgramNode extends ASTNode {
 
     /**
      * Instantiates a new program node.
      * 
      * @param functionList
      *            A list of ASTNode's representing the function definitions of
      *            this program
      * @param yyline
      *            the line in the source file
      * @param yycol
      *            the column in the source file
      */
     public ProgramNode(ArrayList<ASTNode> functionList, int yyline, int yycol) {
         super(yyline, yycol);
         if (functionList != null)
             this.getChildren().addAll(functionList);
     }
 
    /**
     * Checks semantics of Program. Ensures there is a main function with an
     * empty parameter list defined.
      * 
      * @see ASTNode#checkSemantics()
      */
     public void checkSemantics() throws Exception {
         for (ASTNode function : this.getChildren()) {
             function.checkSemantics();
         }
         if (!Parser.functionSymbolsTable.containsKey("main")) {
             throw new Exception(this.getYyline() + ":" + this.getYycolumn()
                     + ": No function called main defined");
         }
        FunctionSymbolTableEntry symbol_entry = Parser.functionSymbolsTable
                .get("main");
        if (symbol_entry.getParamTypes() != null
                && symbol_entry.getParamTypes().size() != 0) {
            throw new Exception(
                    this.getYyline()
                            + ":"
                            + this.getYycolumn()
                            + ": Main function must be defined with empty parameter list");
        }
     }
 
     /**
      * The top level target code generation. The target Java class is given the
      * same name as the input source file. The target main function calls the
      * main function defined in the source.
      * 
      * The target code is printed to standard out to be piped into successive
      * compiler modules.
      * 
      * @param file_name
      *            the name, no extension, of the input source file
      */
     public void generateCode(String file_name) {
         StringBuffer output = new StringBuffer();
         output.append("import smartest.*;\nimport java.util.ArrayList;\n");
 
         output.append("public class " + file_name + " {\n");
         output.append("public static void main(String[] args) {\n");
         output.append("_smartestFunction_"
                 + Parser.functionSymbolsTable.get("main").getID() + "();\n}\n");
         for (ASTNode function : this.getChildren()) {
             output.append(function.generateCode());
         }
 
         output.append("}");
 
         System.out.println(output);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ASTNode#generateCode()
      */
     public StringBuffer generateCode() {
         generateCode("STL");
         return null;
     }
 
 }
