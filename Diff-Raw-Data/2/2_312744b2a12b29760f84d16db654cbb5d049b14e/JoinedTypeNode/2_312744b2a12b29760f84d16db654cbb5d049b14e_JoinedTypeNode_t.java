 package org.caesarj.compiler.typesys.join;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.typesys.CaesarTypeSystemException;
 import org.caesarj.compiler.typesys.graphsorter.GraphSorter2;
 import org.caesarj.compiler.typesys.input.InputTypeNode;
 import org.caesarj.compiler.typesys.java.JavaQualifiedName;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 
 /**
  * Joined Caesar type node
  * 		- contains information about class relationships
  * 		- nesting, parents, furtherbounds, mixin list 
  *		- computes on-demand
  *	 
  * @author vaidas
  */
 public class JoinedTypeNode {
 	/* qualified class name */
 	protected JavaQualifiedName qualifiedName;
 	
 	/* owner graph */
 	protected JoinedTypeGraph graph;
 	
 	/* outer type node (can be null) */
 	protected JoinedTypeNode outer;
 	
 	/* flag if inputNode is determined */
 	protected boolean bInputNode = false;
 	
 	/* corresponding input type node (computed on-demand) */
 	protected InputTypeNode inputNode = null;
 	
 	/* all class mixins, including implicit (computed on-demand) */
 	protected List<JoinedTypeNode> allMixins = null;
 	
 	/* all explicit (declared) class mixins (computed on-demand) */
 	protected List<JoinedTypeNode> implMixins = null;
 	
 	/* all mixins with the same name: furtherbounds and itself (computed on-demand) */
 	protected List<JoinedTypeNode> ownMixins = null;
 	
 	/* explicitly declared parents for the type node */
 	protected List<JoinedTypeNode> declParents = null;
 	
 	/* declared parents of the type node and its futherbounds */
 	protected List<JoinedTypeNode> directParents = null;
 	
 	/* all direct and indirect parents of the type node (computed on-demand) */
 	protected List<JoinedTypeNode> allParents = null;
 	
 	/* names of declared inner classes (computed on-demand) */
 	protected List<String> declInnerNames = null;
 	
 	/* type nodes of declared inner classes (computed on-demand) */
 	protected List<JoinedTypeNode> declInners = null;
 	
 	/* names of all inner classes, including implicit ones (computed on-demand) */
 	protected List<String> allInnerNames = null;
 	
 	/* type nodes of all inner classes, including implicit ones (computed on-demand) */
 	protected List<JoinedTypeNode> allInners = null;
 	
 	/**
 	 *	Constructor  
 	 */
 	public JoinedTypeNode(JavaQualifiedName qualifiedName, JoinedTypeGraph graph) {
 		this.qualifiedName = qualifiedName;
 		this.graph = graph;
 		if (!qualifiedName.getOuterPrefix().equals("")) {
 			this.outer = graph.getNodeByName(
 					new JavaQualifiedName(qualifiedName.getOuterQualifiedName()));
 		}
 	}
 	
 	/**
 	 *	Get qualified name 
 	 */
 	public JavaQualifiedName getQualifiedName() {
 		return qualifiedName;
 	}
 	
 	/**
 	 *	Get class identifier (short name) 
 	 */
 	public String getIdent() {
 		return qualifiedName.getIdent();
 	}
 	
 	/**
 	 *	Get outer node (can be null) 
 	 */
 	public JoinedTypeNode getOuter() {
 		return outer;
 	}
 	
 	/**
 	 * Finds a class with given name in the context of the outer of the class
 	 * Finds also Caesar classes from global scope
 	 * 
 	 * @param name		short class name
 	 * @return			resolved class name, null if not found
 	 */
 	protected JoinedTypeNode lookupClassInCtx(String name) {
 		if (outer == null) {
 			JavaQualifiedName resolvedName = getInputNode().resolveType(name);
 			if (graph.getInputNode(resolvedName) == null) {
 				return null;
 			}
 			return graph.getNodeByName(resolvedName);
 		}
 		else {
 			JoinedTypeNode inner = outer.findInner(name);
 			if (inner != null) 
 				return inner;
 			return outer.lookupClassInCtx(name);
 		}
 	}
 	
 	/**
 	 * Find inner class with given name, also implicit
 	 * 
 	 * @param name		short class name
 	 * @return			type node of the inner class (null if not found)
 	 */
 	public JoinedTypeNode findInner(String name) {
 		for (String inner : getAllInnerNames()) {
 			if (inner.equals(name)) {
 				return createInnerForName(inner);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Get the list of the names of all inner classes
 	 */
 	public List<String> getAllInnerNames() {
 		if (allInnerNames == null) {
 			allInnerNames = new ArrayList<String>();
 			/* collect inners from all inherited mixins */
 			for (JoinedTypeNode mixin : getImplMixins()) {
 				for (String inner : mixin.getDeclInnerNames()) {
 					if (!allInnerNames.contains(inner)) {
 						allInnerNames.add(inner);
 					}
 				}
 			}
 		}
 		return allInnerNames;		
 	}
 	
 	/**
 	 * Get the type nodes of all inner classes
 	 */
 	public List<JoinedTypeNode> getAllInners() {
 		if (allInners == null) {
 			allInners = new ArrayList<JoinedTypeNode>();
 			for (String name: getAllInnerNames()) {
 				allInners.add(createInnerForName(name));
 			}
 		}
 		return allInners;
 	}
 	
 	/**
 	 * Get the type nodes of all inner classes
 	 */
 	public List<JoinedTypeNode> getDeclInners() {
 		if (declInners == null) {
 			declInners = new ArrayList<JoinedTypeNode>();
 			for (String name: getDeclInnerNames()) {
 				declInners.add(createInnerForName(name));
 			}
 		}
 		return declInners;
 	}
 	
 	/**
 	 * Create the type node for the inner class with given name 
 	 * or returns existing node
 	 */
 	protected JoinedTypeNode createInnerForName(String name) {
 		return graph.getNodeByName(new JavaQualifiedName(getQualifiedName() + "$" + name));
 	}
 	
 	/**
 	 * Get corresponding input node 
 	 */
 	public InputTypeNode getInputNode() {
 		if (!bInputNode) {
 			inputNode = graph.getInputNode(qualifiedName);
 			bInputNode = true;
 		}
 		return inputNode;
 	}
 	
 	/**
 	 * Get token reference for error reporting.
 	 * If the class is implicit, the token reference of its outer is used  
 	 */
 	public TokenReference getTokenRef() {
 		if (isDeclared()) {
 			return getInputNode().getTokenRef();
 		}
 		else if (getOuter() != null) {
 			return getOuter().getTokenRef();
 		}
 		else {
 			return TokenReference.NO_REF;
 		}
 	}
 	
 	/**
 	 * Is class declared? 
 	 */
 	public boolean isDeclared() {
 		return (getInputNode() != null);
 	}
 	
 	/**
 	 * Get all mixins that have declarations 
 	 */
 	public List<JoinedTypeNode> getImplMixins() {
 		if (implMixins == null) {
 			/* filters only declared mixins from the list of all mixins */
 			implMixins = new ArrayList<JoinedTypeNode>();
 			for (JoinedTypeNode mixin : getAllMixins()) {
 				if (mixin.isDeclared()) {
 					implMixins.add(mixin);
 				}
 			}			
 		}
 		return implMixins;
 	}
 	
 	/**
 	 * Get all mixins, declared as well as implicit 
 	 */
 	public List<JoinedTypeNode> getAllMixins() {
 		if (allMixins == null) {
 			allMixins = new ArrayList<JoinedTypeNode>();
 			allMixins.addAll(getOwnMixins());
 			for (JoinedTypeNode parent : getAllParents()) {
 				List<JoinedTypeNode> parentMixins = parent.getOwnMixins();
 				allMixins.addAll(parentMixins);
 			}			
 		}
 		return allMixins;
 	}
 	
 	/**
 	 * Get all mixins with the same name 
 	 */
 	public List<JoinedTypeNode> getOwnMixins() {
 		if (ownMixins == null) {
 			ownMixins = new ArrayList<JoinedTypeNode>();
 			if (outer == null) {
 				/* for top level classes only itself */
 				ownMixins.add(this);
 			}
 			else {
 				String ident = qualifiedName.getIdent();
 				/* collect classes having the same name as current node
 				   in all mixins of outer */
 				for (JoinedTypeNode mixin : outer.getAllMixins()) {
 					JoinedTypeNode inner = mixin.findInner(ident);
 					if (inner != null) {
 						ownMixins.add(inner);
 					}
 				}
 			}
 		}
 		return ownMixins;
 	}
 	
 	/**
 	 *	Get all parent classes 
 	 */
 	public List<JoinedTypeNode> getAllParents() {
 		if (allParents == null) {
 			try {
 				/* sort the graph of transitive parent relationships 
 				 * the sorting uses getDirectParents as input */
 				TypeNodeParentSorter sorter = new TypeNodeParentSorter(this);
 				allParents = sorter.getSortedTypeNodes();
 				/* remove self */
 				allParents.remove(0);
 			}
 			catch (GraphSorter2.CycleFoundException e) {
 				/* circular parent declarations detected */
 				graph.getCompiler().reportTrouble(
 						new PositionedError(getTokenRef(), KjcMessages.CLASS_CIRCULARITY,
 				                e.getNodeName()));
 				throw new CaesarTypeSystemException();
 			}
 			/* avoid inheritance from own outer classes */
 			if (containsOwnOuters(allParents)) {
 				graph.getCompiler().reportTrouble(
 						new PositionedError(getTokenRef(), CaesarMessages.CANNOT_INHERIT_FROM_OWN_OUTER,
 				                getQualifiedName().toString()));
 				throw new CaesarTypeSystemException();
 			}
 		}
 		return allParents;
 	}
 	
 	/**
 	 *	Check if the given list of nodes contains the outers of the class
 	 */
 	protected boolean containsOwnOuters(List<JoinedTypeNode> nodes) {
 		for (JoinedTypeNode n : getOuterChain()) {
 			if (nodes.contains(n)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Get the outer path from the top level class to itself (inclusive) 
 	 */
 	public List<JoinedTypeNode> getOuterChain() {
 		List<JoinedTypeNode> lst = new ArrayList<JoinedTypeNode>();
 		JoinedTypeNode n = this;
 		while (n != null) {
 			lst.add(0, n);
 			n = n.getOuter();
 		}
 		return lst;
 	}
 	
 	/**
 	 * returns the type of this Node in context of the node n.
 	 * B extends A ->
 	 * A.A in context of B is B.A
 	 */
 	public JoinedTypeNode getTypeInContextOf(JoinedTypeNode n) {
 	    // top level class, no redefinition possible
 	    if (getOuter() == null || n == null)
 	        return this;
 	    
 	    List<JoinedTypeNode> l1 = getOuterChain();
 	    List<JoinedTypeNode> l2 = n.getOuterChain();
 	    
 	    JoinedTypeNode thisOuterChain[] = l1.toArray(new JoinedTypeNode[l1.size()]);
 	    JoinedTypeNode contextOuterChain[] = l2.toArray(new JoinedTypeNode[l2.size()]);
 	    
 	    // check subtype relations
 
 	    // s = max(thisOuterChain.length-1, contextOuterChain.length)
 	    int s = thisOuterChain.length - 1; 
 	    if (s > contextOuterChain.length)
 	        s = contextOuterChain.length;
 	    
 	    for (int j = s-1; j >= 0; j--) {
 	    	JoinedTypeNode t = contextOuterChain[j];
 	    	for (int i = j+1; i < thisOuterChain.length; i++) {
 		    	t = t.findInner(thisOuterChain[i].getQualifiedName().getIdent());
 		    	if (t == null) {
 		    		break;
 		        }
 		    }
 	    	if (t != null && t.isFurtherbindingOf(this)) {
 	    		return t;
 	    	}
 	    }
 	    return null;
     }
 	
 	/**
 	 * Checks if it is a further binding of the given node
 	 */
 	public boolean isFurtherbindingOf(JoinedTypeNode n) {
 	    if (this == n) {
 	        return true;
         }
 	    
 	    for (JoinedTypeNode mixin : getOwnMixins()) {
             if (mixin == n)
                 return true;
         }
 	    
 	    return false;
     }
 	
 	/**
 	 * Get directly declared parents of the node or its furtherbounds 
 	 */
 	public List<JoinedTypeNode> getDirectParents() {
 		if (directParents == null) {
 			directParents = new ArrayList<JoinedTypeNode>();
 			/* collect all declared parents from itself and futherbounds */
 			for (JoinedTypeNode mixin : getOwnMixins()) {
 				for (JoinedTypeNode parent : mixin.getDeclParents()) {
 					JoinedTypeNode resParent = parent;
 					if (outer != null) {
 						/* compute the parent type node in the context of the class */
 						resParent = parent.getTypeInContextOf(outer);
 						if (resParent == null) {
 							graph.getCompiler().reportTrouble(
 									new PositionedError(getTokenRef(), CaesarMessages.CANNOT_INHERIT_FROM_CCLASS,
 											parent.getQualifiedName()));
 							throw new CaesarTypeSystemException();							
 						}
 					}
 					if (!directParents.contains(resParent)) {
 						directParents.add(resParent);
 					}
 				}
 			}
 		}
 		return directParents;
 	}
 	
 	/**
 	 * Get declared parents of the node 
 	 */
 	public List<JoinedTypeNode> getDeclParents() {
 		if (declParents == null) {
 			declParents = new ArrayList<JoinedTypeNode>();
 			if (isDeclared()) {
 				/* get parents from the input node and import to the current context */
 				for (String parentIdent : getInputNode().getDeclaredParents()) {
 					JoinedTypeNode parent = lookupClassInCtx(parentIdent);
 					if (parent == null) {
 						graph.getCompiler().reportTrouble(
 								new PositionedError(getTokenRef(), CaesarMessages.CCLASS_UNKNOWN,
 										parentIdent));
 						throw new CaesarTypeSystemException();						
 					}
 					if (outer != null) {
 						/* import parent to the own context */
 						parent = parent.getTypeInContextOf(outer);	
 						if (parent == null) {
 							graph.getCompiler().reportTrouble(
 									new PositionedError(getTokenRef(), CaesarMessages.CANNOT_INHERIT_FROM_CCLASS,
											parentIdent));
 							throw new CaesarTypeSystemException();							
 						}
 					}
 					declParents.add(parent);
 				}
 			}
 		}
 		return declParents;	
 	}
 	
 	/**
 	 * Get the names of declared inner classes 
 	 */
 	public List<String> getDeclInnerNames() {
 		if (declInnerNames == null) {
 			declInnerNames = new ArrayList<String>();
 			if (isDeclared()) {
 				/* retrieve inners from the input node */
 				declInnerNames.addAll(getInputNode().getDeclaredInners());
 			}
 		}
 		return declInnerNames;	
 	}
 	
 	/**
 	 * Convert to string (for debugging purposes)
 	 */
 	public String toString() {
 		return getQualifiedName().toString();
 	}
 }
