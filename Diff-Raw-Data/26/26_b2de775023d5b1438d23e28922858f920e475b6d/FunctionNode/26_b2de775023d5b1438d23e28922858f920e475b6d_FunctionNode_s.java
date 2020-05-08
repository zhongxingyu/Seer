 package smartest;
 
 import java.util.ArrayList;
 
 public class FunctionNode extends ASTNode
 {
         private ArrayList<ASTNode> paramList;
         private ArrayList<ASTNode> stmtList;
 
        public FunctionNode(String id, ArrayList<ASTNode> parameterList, ArrayList<ASTNode> statementList, int yycol)
         {
                 super(yyline, yycol);
                 paramList = parameterList;
                 stmtList = statementList;
         }
 	public void semanticCheck()
         {
                for (param : paramList)
                 {
                        param.semanticCheck();
                 }
                for (statement : stmtList)
                 {
                        statement.semanticCheck();
                 }
                 if (Parser.getFunctionSymbolTable().contains(id.toLowerCase()))
                 {
                         throw new Exception("Line " + this.getYyline() +
                                 ": Function " + id.toLowerCase() +
                                 " is already defined");
                 }
         }
         
 }
