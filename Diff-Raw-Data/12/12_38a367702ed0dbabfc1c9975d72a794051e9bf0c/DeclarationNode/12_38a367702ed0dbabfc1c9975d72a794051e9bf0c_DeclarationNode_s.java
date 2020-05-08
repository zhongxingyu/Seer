 /**
  * Represents a declaration statement
  * This is where identifiers are inserted into the symbols tables
  * @author Aiman
  */
 public class DeclarationNode extends ASTNode {
 
 	
 	private String declaredType;
 	private IDNode idNode;
 	
 	/**
 	 * Instantiates a DeclarationNode that has been invoked by this grammar:
 	 * type ID
 	 * Example: int x
 	 *  @param typeNode the type of the identifier (e.g. int)
 	 *  @param idNode the identifier name node (e.g. x)
 	 *  @param yyline the line where this operator was found in the source code
 	 *  @param yycolumn the column where this operator was found in the source code
 	 */
 	public DeclarationNode(String idType, ASTNode idNode, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(idNode);
 		this.declaredType = idType;
 		this.idNode = (IDNode)idNode;
 		
 		
 	}
 
 	/**
 	 * Declaration semantics:
 	 * Verify that the declared name has not been declared before
 	 * Inserts the symbol into the symbols table
 	 * @throws Exception
 	 */
 	@Override
 	public void checkSemantics() throws Exception {
 
 		// Verify that this variable has not been already declared before
 		if (Parser.symbolsTable.containsKey(this.getIdNode().getName()))
 		{
 			throw new Exception("Line " + this.getYyline() + ":" + this.getYycolumn() + ": variable " + this.getIdNode().getName() +
								"has already been declared at line " + Parser.symbolsTable.get(this.getIdNode().getName())[2]);
 		}
 		
 		// Now, insert this new ID into symbols table, and generate a new valid target variable name for it
 		String varName = "_smartestVar_" + this.getIdNode().getName();
 		
 		
 		if (Parser.DEBUG)
 		{
 			System.out.println("** Inserting new variable into symbols table:");
 			System.out.println("Type:" + declaredType.toLowerCase());
 			System.out.println("Var Name: " + varName);
 			System.out.println("Line Number: " + ""+this.getYyline());		
 		}
 		
 		
 		String[] data = { this.getDeclaredType().toLowerCase(),varName, ""+this.getYyline() }; 
 		
 		
 		
 		Parser.symbolsTable.put(this.getIdNode().getName(), data);
 		
 		
 		this.setType(this.getDeclaredType());
 		
 		
 		this.getChildAt(0).checkSemantics();
 
 	}
 
 	@Override
 	public String generateCode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IDNode getIdNode() {
 		return idNode;
 	}
 
 	public void setIdNode(IDNode idNode) {
 		this.idNode = idNode;
 	}
 
 	/**
 	 * @return the declaredType
 	 */
 	public String getDeclaredType() {
 		return declaredType;
 	}
 
 	/**
 	 * @param declaredType the declaredType to set
 	 */
 	public void setDeclaredType(String declaredType) {
 		this.declaredType = declaredType;
 	}
 
 	
 
 	
 
 }
