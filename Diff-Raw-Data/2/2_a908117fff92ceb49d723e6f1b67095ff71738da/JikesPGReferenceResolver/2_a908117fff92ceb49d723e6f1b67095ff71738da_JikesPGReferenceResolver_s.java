 package org.jikespg.uide.editor;
 
 import java.util.*;
 
 import org.eclipse.uide.editor.ReferenceResolver;
 import org.eclipse.uide.parser.IParseController;
 
 import lpg.lpgjavaruntime.*;
 
 //import jikesPG.safari.parser.Ast.*;
 import org.jikespg.uide.parser.ASTUtils;
 import org.jikespg.uide.parser.JikesPGParser.*;
 
 public class JikesPGReferenceResolver extends ReferenceResolver {
 
 	public JikesPGReferenceResolver () {
 		super();
 
 		// If you want to stipulate at construction time the
 		// node types that are legal link source or target types,
 		// you can do that here ...
 
 	}
 	
 	
 	/**
 	 * Get the target for a given source node in the AST represented by a
 	 * given Parse Controller.
 	 */
 	public Object getLinkTarget(Object node, IParseController parseController)
 	{
		if (!(node instanceof ASTNode)) return null;
 		
 		JikesPG ast = (JikesPG) parseController.getCurrentAst();
 		final ASTNode def= ASTUtils.findDefOf((IASTNodeToken) node, (JikesPG) ast);
 		
     	return def;
 	}
 	
 	// For JikesPG, any AST node can be treated as the source for
 	// a hyperlink.  Links may only be found for some times of source
 	// node, but inappropriate types are not filtered out here.
 	public boolean hasSuitableLinkSourceType(Object node) {
 		return (node instanceof ASTNode);
 	}
 
 
 	/**
 	 * Get the text associated with a given node for use in a link
 	 * from (or to) that node
 	 */
 	public String getLinkText(Object node) {
 		if (node instanceof ASTNode) {
 			// This seems to be the traditional implementation
 			return ((ASTNode)node).getLeftIToken().toString();
 		} else {
 			System.err.println("JikesPGLinkMapper.getLinkText:  be advised:  given object is not an ASTNode");
 			return node.getClass().toString();
 		}
 	}
 
 
 }
