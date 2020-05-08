 package swp_compiler_ss13.fuc.ast;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.parser.SymbolTable;
 
 /**
  * BlockNode implementation
  * 
  * @author "Frank Zechert, Danny Maasch"
  * @version 1
  */
 public class BlockNodeImpl extends ASTNodeImpl implements BlockNode {
 
 	/**
 	 * The list of declaration nodes
 	 */
 	private List<DeclarationNode> declarationNodes;
 
 	/**
 	 * The list of statement nodes
 	 */
 	private List<StatementNode> statementNodes;
 
 	/**
 	 * The symbol table of the block scope
 	 */
 	private SymbolTable symbolTable;
 
 	/**
 	 * The logger
 	 */
 	private static Logger logger = Logger.getLogger(BlockNodeImpl.class);
 
 	/**
 	 * Create a new BlockNode
 	 */
 	public BlockNodeImpl() {
 		this.declarationNodes = new LinkedList<>();
 		this.statementNodes = new LinkedList<>();
 	}
 
 	@Override
 	public ASTNodeType getNodeType() {
 		return ASTNodeType.BlockNode;
 	}
 
 	@Override
 	public Integer getNumberOfNodes() {
 		int nodes = 1 + this.getNumberOfDeclarations();
 		for (StatementNode stn : this.statementNodes) {
 			nodes += stn.getNumberOfNodes();
 		}
 		return nodes;
 	}
 
 	@Override
 	public List<ASTNode> getChildren() {
 		List<ASTNode> children = new LinkedList<>();
 		children.addAll(this.declarationNodes);
 		children.addAll(this.statementNodes);
 		return children;
 	}
 
 	@Override
 	public void addDeclaration(DeclarationNode declaration) {
 		if (declaration == null) {
 			logger.error("The argument declaration can not be null!");
 			throw new IllegalArgumentException("The argument declaration can not be null!");
 		}
 		this.declarationNodes.add(declaration);
 	}
 
 	@Override
 	public void addStatement(StatementNode statement) {
 		if (statement == null) {
 			logger.error("The argument statement can not be null!");
 			throw new IllegalArgumentException("The argument statement can not be null!");
 		}
 		this.statementNodes.add(statement);
 	}
 
 	@Override
 	public List<DeclarationNode> getDeclarationList() {
 		return this.declarationNodes;
 	}
 
 	@Override
 	public List<StatementNode> getStatementList() {
 		return this.statementNodes;
 	}
 
 	@Override
 	public Iterator<DeclarationNode> getDeclarationIterator() {
 		return this.declarationNodes.iterator();
 	}
 
 	@Override
 	public Iterator<StatementNode> getStatementIterator() {
 		return this.statementNodes.iterator();
 	}
 
 	@Override
 	public Integer getNumberOfDeclarations() {
 		return this.declarationNodes.size();
 	}
 
 	@Override
 	public Integer getNumberOfStatements() {
 		return this.statementNodes.size();
 	}
 
 	@Override
 	public SymbolTable getSymbolTable() {
 		if (this.symbolTable == null) {
 			logger.warn("Returning null as the symbol table!");
 		}
 		return this.symbolTable;
 	}
 
 	@Override
 	public void setSymbolTable(SymbolTable symbolTable) {
		if (symbolTable == null) {
 			logger.error("The argument symbolTable can not be null!");
 			throw new IllegalArgumentException("The argument symbolTable can not be null!");
 		}
 		this.symbolTable = symbolTable;
 	}
 
 }
