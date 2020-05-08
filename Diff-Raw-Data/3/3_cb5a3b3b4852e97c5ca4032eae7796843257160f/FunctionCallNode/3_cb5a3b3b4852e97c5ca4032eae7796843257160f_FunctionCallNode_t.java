 package smartest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * Implements semantic checking and output code generation of function call
 * statements.
 * <br />
  * Example:
  * 
  * <pre>
  * int sum (int a, int b) {
  *      int c = a + b;
  *      return c;          
  * } 
  * 
  * sum(a, b, d); %% number of parameters are not consistent with function definition
  * avg(a, b); %% use of undefined function
  * </pre>
  * 
  * @author Harpreet
  */
 public class FunctionCallNode extends ASTNode {
 
     /** The function name. */
     private String functionName;
 
     /** The length of the parameter list. */
     private int length;
 
     /** The function symbol table. */
     private HashMap<String, FunctionSymbolTableEntry> hashMap = Parser.functionSymbolsTable;
 
     /** The function symbol table entry for this function. */
     private FunctionSymbolTableEntry functionSymbolTableEntry;
 
     /** The list of parameters. */
     private ArrayList<String> arrayList;
 
     /**
      * Instantiates a new function call node.
      * 
      * @param functionName
      *            the function name
      * @param optionalFactorList
      *            a factor list or null
      * @param yyline
      *            the corresponding line in the input file
      * @param yycolumn
      *            the corresponding column in the input file
      */
     FunctionCallNode(String functionName, ASTNode optionalFactorList,
             int yyline, int yycolumn) {
         super(yyline, yycolumn);
         if (optionalFactorList != null) {
             this.addChild(optionalFactorList);
             this.setLength(optionalFactorList.getChildCount());
         }
         this.setFunctionName(functionName);
 
     }
 
     /**
      * Ensures the function has a definition matching the number and types of
      * the parameters of this function call.
      * 
      * @see ASTNode#checkSemantics()
      */
     public void checkSemantics() throws Exception {
         if (this.getChildCount() > 0)
             this.getChildAt(0).checkSemantics();
 
         if (!hashMap.containsKey(functionName.toLowerCase()))
             throw new Exception("Function " + functionName
                     + "  does not Exist: " + this.getYyline() + ":"
                     + this.getYycolumn()
                     + ". Please declare the function first.");
 
         else {
             functionSymbolTableEntry = hashMap.get(functionName.toLowerCase());
 
             if (!(functionSymbolTableEntry.getParamTypes().size() == this
                     .getLength()))
                 throw new Exception(
                         "Number of Parameters not consistent with function declaration: "
                                 + this.getYyline() + ":" + this.getYycolumn());
 
             else {
 
                 arrayList = functionSymbolTableEntry.getParamTypes();
                 for (int i = 0; i < arrayList.size(); i++) {
                     if (arrayList.get(i).equalsIgnoreCase("float")
                             && this.getChildAt(0).getChildAt(i).getType()
                                     .equalsIgnoreCase("int"))
                         continue;
 
                     else if (!arrayList.get(i).equalsIgnoreCase(
                             this.getChildAt(0).getChildAt(i).getType())) {
                         throw new Exception(
                                 "Parameter is of Incompatible Type: "
                                         + this.getYyline() + ":"
                                         + this.getYycolumn()
                                         + " See function definition.");
 
                     }
                 }
             }
 
         }
         this.setType(functionSymbolTableEntry.getReturnType());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ASTNode#generateCode()
      */
     public StringBuffer generateCode() {
         StringBuffer output = new StringBuffer();
         output.append(functionSymbolTableEntry.getJavaID());
         output.append("( ");
         if (getLength() > 0) {
             output.append(this.getChildAt(0).generateCode());
         }
         output.append(" )");
         return output;
 
     }
 
     /**
      * Gets the length of the parameter list
      * 
      * @return the length
      */
     public int getLength() {
         return length;
     }
 
     /**
      * Sets the length of the parameter list
      * 
      * @param length
      *            the new length
      */
     public void setLength(int length) {
         this.length = length;
     }
 
     /**
      * Gets the function name.
      * 
      * @return the function name
      */
     public String getFunctionName() {
         return functionName;
     }
 
     /**
      * Sets the function name.
      * 
      * @param functionName
      *            the new function name
      */
     public void setFunctionName(String functionName) {
         this.functionName = functionName;
     }
 
 }
