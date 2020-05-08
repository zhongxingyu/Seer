 package smartest;
 
 import java.util.ArrayList;
 
 public class FunctionNode extends ASTNode
 {
         private ArrayList<ASTNode> paramList;
         private ArrayList<ASTNode> stmtList;
         private String rtrnType;
 
         public FunctionNode(String returnType, String id, ArrayList<ASTNode> parameterList, ArrayList<ASTNode> statementList, int yyline, int yycol)
         {
                 super(yyline, yycol);
                 rtrnType = returnType;
                 paramList = parameterList;
                 stmtList = statementList;
         }
 	public void semanticCheck()
         {
                 if (paramList != null)
                 {
                        for (param : paramList)
                         {
                                 param.semanticCheck();
                         }
                 }
                 if (stmtList != null)
                 {
                        for (statement : stmtList)
                         {
                                 statement.semanticCheck();
                         }
                 }
                 if (Parser.functionSymbolsTable.contains(id.toLowerCase()))
                 {
                         throw new Exception("Line " + this.getYyline() +
                                 ": Function " + id.toLowerCase() +
                                 " is already defined");
                 }
                 else
                 {
                         String javaID = Parser.getNewFunctionID();
                        ArrayList<String> paramList = new ArrayList<String>();
                        for (param : paramList)
                         {
                                paramList.append(param.getType());
                         }
 
                         Parser.functionSymbolsTable.put(id.toLowerCase(), new FunctionSymbolTableEntry(id, javaID, paramList));
                 }
                 setType(returnType);
         }
         
 }
