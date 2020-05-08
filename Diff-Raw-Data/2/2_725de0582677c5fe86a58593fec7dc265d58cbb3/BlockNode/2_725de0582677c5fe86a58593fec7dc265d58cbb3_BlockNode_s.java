 package swp_compiler_ss13.common.ast.nodes.marynary;
 
 import java.util.Iterator;
 import java.util.List;
 
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.parser.SymbolTable;
 
 /**
  * The node to represent a block. A block consists of n declarations followed by
  * m statements. m, n >= 0.
  * 
  * @author "Frank Zechert"
  * @version 1
  */
 public interface BlockNode extends StatementNode
 {
 	/**
 	 * Add a declaration to the end of the current list of declarations.
 	 * 
 	 * @param declaration
 	 *            The declaration to add.
 	 */
 	public void addDeclaration(DeclarationNode declaration);
 
 	/**
 	 * Add a statement to the end of the current list of statements.
 	 * 
 	 * @param statement
 	 *            The statement to add.
 	 */
 	public void addStatement(StatementNode statement);
 
 	/**
 	 * Get a list of declarations of this block.
 	 * 
 	 * @return the list of declarations.
 	 */
 	public List<DeclarationNode> getDeclarationList();
 
 	/**
 	 * Get the list of statements of this block.
 	 * 
 	 * @return the list of statements.
 	 */
 	public List<StatementNode> getStatementList();
 
 	/**
 	 * Get an iterator to iterate over the declarations of this block.
 	 * 
 	 * @return the declaration iterator.
 	 */
 	public Iterator<DeclarationNode> getDeclarationIterator();
 
 	/**
 	 * Get an iterator to iterate over the statements of this block.
 	 * 
 	 * @return the statement iterator.
 	 */
	public Iterable<StatementNode> getStatementIterator();
 
 	/**
 	 * Get the number of declarations in this block.
 	 * 
 	 * @return the number of declarations.
 	 */
 	public Integer getNumberOfDeclarations();
 
 	/**
 	 * Get the number of statements in this block
 	 * 
 	 * @return the number of statements.
 	 */
 	public Integer getNumberOfStatements();
 
 	/**
 	 * Get the symbol table that belongs to the scope of this block.
 	 * 
 	 * @return the symbol table of this block's scope.
 	 */
 	public SymbolTable getSymbolTable();
 
 	/**
 	 * Set the symbol table that belongs to the scope of this block.
 	 * 
 	 * @param symbolTable
 	 *            the symbol table of this block's scope
 	 */
 	public void setSymbolTable(SymbolTable symbolTable);
 }
