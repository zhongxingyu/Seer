 package uk.ed.inf.tree;
 
 import java.util.Iterator;
 
 
 public final class GeneralTree<T extends ITreeNode<T>> implements ITree<T> {
 	private final T rootNode;
 	private final LCACalculator<T> lcaCalc;
 	
 	public GeneralTree(T rootNode){
 		if(rootNode == null) throw new NullPointerException("root node cannot be null");
 		
 		this.rootNode = rootNode;
 		this.lcaCalc = new LCACalculator<T>(rootNode);
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ed.inf.graph.impl.ITree#getRootNode()
 	 */
 	public T getRootNode(){
 		return this.rootNode;
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ed.inf.graph.impl.ITree#containsNode(T)
 	 */
 	public boolean containsNode(T testNode){
 		return this.containsNode(testNode.getIndex());
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ed.inf.graph.impl.ITree#constainsNode(int)
 	 */
 	public boolean containsNode(int testIndex){
 		Iterator<T> iter = this.levelOrderIterator();
 		boolean retVal = false;
 		while(iter.hasNext() && retVal == false){
 			T node = iter.next();
 			if(node.getIndex() == testIndex){
 				retVal = true;
 			}
 		}
 		return retVal;
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ed.inf.graph.impl.ITree#get(int)
 	 */
 	public T get(int testIndex){
 		Iterator<T> iter = this.levelOrderIterator();
 		T retVal = null;
 		while(iter.hasNext() && retVal == null){
 			T node = iter.next();
 			if(node.getIndex() == testIndex){
 				retVal = node;
 			}
 		}
 		return retVal;
 	}
 	
 	public T getLowestCommonAncestor(final T thisNode, final T thatNode){
 		if(!thisNode.getRoot().equals(this.rootNode) || !thatNode.getRoot().equals(this.rootNode))
 			throw new IllegalArgumentException("Noth nodes must belong to the same this tree");
 		
 		this.lcaCalc.findLowestCommonAncestor(thisNode, thatNode);
 		return this.lcaCalc.getLCANode();
 	}
 	
 	public Iterator<T> levelOrderIterator(){
 		return new LevelOrderTreeIterator<T>(this.rootNode);
 	}
 	
 	/**
 	 * Tests if <code>testNode</code> is a descendant of <code>startNode</code>. This means traversing
 	 * from the <code>startNode</code> until it finds the <code>testNode</code>.
 	 * @param startNode the node to start from, can be null.
 	 * @param testNode the node to be tested, can be null.
 	 * @return <code>true</code> if <code>testNode</code> is an descendant of <code>startNode</code>,
 	 *  <code>false</code> otherwise.
 	 */
 	public boolean isDescendant(T startNode, T testNode){
 		boolean retVal = false;
		Iterator<T> iter = this.levelOrderIterator();
 		while(iter.hasNext() && retVal == false){
 			T node = iter.next();
 			if(node.equals(testNode)){
 				retVal = true;
 			}
 		}
 		return retVal;
 	}
 	
 	public int size(){
 		Iterator<T> iter = this.levelOrderIterator();
 		int cnt = 0;
 		while(iter.hasNext()){
 			iter.next();
 			cnt++;
 		}
 		return cnt;
 	}
 
 	public boolean isAncestor(T startNode, T testNode) {
 		boolean retVal = false;
 		Iterator<T> iter = new AncestorTreeIterator<T>(startNode);
 		while(iter.hasNext() && retVal == false){
 			T node = iter.next();
 			if(node.equals(testNode)){
 				retVal = true;
 			}
 		}
 		return retVal;
 	}
 
 	public ITreeWalker<T> levelOrderTreeWalker(ITreeNodeAction<T> visitorAction) {
 		return new LevelOrderTreeWalker<T>(this.rootNode, visitorAction);
 	}
 }
