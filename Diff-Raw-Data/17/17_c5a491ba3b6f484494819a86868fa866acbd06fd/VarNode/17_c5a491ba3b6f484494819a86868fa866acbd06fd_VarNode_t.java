 package tree;
 
 import symboltable.SymbolTable;
 
 public class VarNode extends ExprNode {
 
 	/**Variable**/
 	private String variable;
 	private ProgTree parent;
 	
 	/**
 	 * Build a variable node
 	 * @param variable name of the variable
 	 */
 	public VarNode(ProgTree parent, String variable) {
 		this.parent = parent;
 		this.variable = variable;
 	}
 	
 	/**
 	 * Get the variable
 	 * @return variable the variable
 	 */
 	public String getVariable() {
 		return variable;
 	}
 
 	/**
 	 * Set a new variable
 	 * @param variable new variable
 	 */
 	public void setVariable(String variable) {
 		this.variable = variable;
 	}
 
 	public void parse() {
 		if (!SymbolTable.getInstance().getProgram(this.parent.getName()).containsVariable(this.variable))
 			SymbolTable.getInstance().addConst(this.variable);
 	}
 	
 	@Override
 	public String codeC() {	
		return this.variable;
 	}
 	
 	public String display(){		
 		return "|-"+ this.variable;
 	}
 
 }
