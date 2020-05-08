 package br.ufal.cideei.visitors;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jface.text.ITextSelection;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class SelectionNodesVisitor is used to compute the ASTNodes contained in an ITextSelection.
  */
 public class SelectionNodesVisitor extends ASTVisitor {
 
 	/** The text selection. */
 	private ITextSelection textSelection;
 	
 	/** The nodes will be added to this Set as the visitor visits the nodes*/
 	private Set<ASTNode> nodes = new HashSet<ASTNode>();
 
 	/**
 	 * Instantiates a new selection nodes visitor.
 	 */
 	@SuppressWarnings("unused")
 	private SelectionNodesVisitor() {
 	}
 
 	/**
 	 * Instantiates a new selection nodes visitor.
 	 *
 	 * @param textSelection the text selection
 	 */
 	public SelectionNodesVisitor(ITextSelection textSelection) {
 		this.textSelection = textSelection;
 	}
 	
 	/**
 	 * Gets the nodes. 
 	 *
 	 * @return the nodes
 	 */
 	public Set<ASTNode> getNodes(){
 		return nodes;
 	}
 
 	/**
 	 * Populates the {@link #nodes} Set with the ASTNodes. 
 	 * Use {@link #getNodes()} to retrive the nodes after accepting this visitor to an ASTNode
 	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
 	 */
 	public void preVisit(ASTNode node) {
 		super.preVisit(node);
		System.out.println(node);
 		if (node.getStartPosition() >= textSelection.getOffset() && 
 				(node.getStartPosition() + node.getLength()) <= textSelection.getOffset() + textSelection.getLength()) {
 			nodes.add(node);
 		}
 	}
 }
