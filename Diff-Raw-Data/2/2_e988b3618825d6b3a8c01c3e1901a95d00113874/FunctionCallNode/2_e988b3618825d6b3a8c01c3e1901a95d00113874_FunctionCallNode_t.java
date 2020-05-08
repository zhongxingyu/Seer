 /**
  * Implements semantic checking and output code generation
  * of function call statements
  * Example: int sum (int a, int b){
  *				int c = a + b;
  *				return c;          
  *          } 
  *          sum(a, b, d); // number of parameters are not consistent with function definition
  *          avg(a, b); //use of undefined function
  * 			
  */
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class FunctionCallNode extends ASTNode
 {
 
 	private String functionName;
 	private int length;
 	private HashMap<String, FunctionSymbolTableEntry> hashMap = Parser.functionSymbolsTable;
 	private FunctionSymbolTableEntry functionSymbolTableEntry;
 	private ArrayList<String> arrayList;
 	
 	FunctionCallNode(String functionName, ASTNode optionalFactorList, int yyline, int yycolumn)
 	{
 		super(yyline, yycolumn);
 		if(optionalFactorList!=null)
 		{
 			this.addChild(optionalFactorList);
 			this.setLength(optionalFactorList.getChildCount());
 		}
 		this.setFunctionName(functionName);
 		
 	}
 	
 	public void checkSemantics() throws Exception 
 	{
 			if (this.getChildCount() > 0)
 				this.getChildAt(0).checkSemantics();
 
 			if(!hashMap.containsKey(functionName.toLowerCase()))
 				throw new Exception("Function does not Exist: " + this.getYyline() + ":" + 
 						this.getYycolumn()+". Please declare the function first."); 
 				
 			else 
 			{
 				functionSymbolTableEntry = hashMap.get(functionName.toLowerCase());
 				
 				if(!(functionSymbolTableEntry.getParamTypes().size() == this.getLength()))
 					throw new Exception("Number of Parameters not consistent with function declaration: " + this.getYyline() + ":" + 
 							this.getYycolumn());
 				
 				else 
 				{
 					
 					arrayList  = functionSymbolTableEntry.getParamTypes();
 					for(int i = 0; i < arrayList.size(); i++)
 					{
 						if(arrayList.get(i).equalsIgnoreCase("float") & this.getChildAt(0).getChildAt(i).getType().equalsIgnoreCase("int"))
 							continue;
 						
 						else if(!arrayList.get(i).equalsIgnoreCase(this.getChildAt(0).getChildAt(i).getType()))
 						{
 							throw new Exception ("Parameter is of Incompatible Type: " + this.getYyline() + ":" + 
 									this.getYycolumn()+" See function definition.");
 							
 							
 						}
 					}
 				}
 				
 			}
 			this.setType(functionSymbolTableEntry.getReturnType());
 	}
 
 	
 	public StringBuffer generateCode() 
 	{
 			StringBuffer output = new StringBuffer();
 			output.append(functionSymbolTableEntry.getJavaID());
 			output.append("( ");
 			if (getLength() > 0)
 			{
 				output.append(this.getChildAt(0).generateCode());
 			}
			output.append(" )");
 			return output;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public void setLength(int length) {
 		this.length = length;
 	}
 
 	public String getFunctionName() {
 		return functionName;
 	}
 
 	public void setFunctionName(String functionName) {
 		this.functionName = functionName;
 	}
 	
 
 }
 
