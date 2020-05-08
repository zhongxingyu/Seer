 import java.util.ArrayList;
 
 public class FunctionNode extends ASTNode
 {
         private ArrayList<ASTNode> paramList;
         private ArrayList<ASTNode> stmtList;
         private String rtrnType;
         private IDNode identifier;
 
         public FunctionNode(String returnType, IDNode id, ArrayList<ASTNode> parameterList, ArrayList<ASTNode> statementList, int yyline, int yycol)
         {
                 super(yyline, yycol);
                 rtrnType = returnType;
                 paramList = parameterList;
                 stmtList = statementList;
                 identifier = id;
         }
 	public void checkSemantics() throws Exception
         {
                identifier.checkSemantics();
                
                 if (paramList != null)
                 {
                         for (ASTNode param : paramList)
                         {
                                 param.checkSemantics();
                         }
                 }
                 if (stmtList != null)
                 {
                         for (ASTNode statement : stmtList)
                         {
                                 statement.checkSemantics();
                         }
                 }
                 if (Parser.functionSymbolsTable.containsKey(identifier.getName().toLowerCase()))
                 {
                         throw new Exception("Line " + this.getYyline() +
                                 ": Function " + identifier.getName().toLowerCase() +
                                 " is already defined");
                 }
                 else
                 {
                         String javaID = "_smartestFunction_" + identifier.getName();
                         ArrayList<String> parameterList = new ArrayList<String>();
                         for (ASTNode param : paramList)
                         {
                                 parameterList.add(param.getType());
                         }
                         Parser.functionSymbolsTable.put(identifier.getName().toLowerCase(), new FunctionSymbolTableEntry(identifier.getName(), javaID, rtrnType, parameterList));
                 }
                 setType(rtrnType);
         }
 	
 		public String getIdentifier()
 		{
 			return identifier.getName();
 		}
 	
         public String generateCode()
         {
                 return "";
         }
         
 }
