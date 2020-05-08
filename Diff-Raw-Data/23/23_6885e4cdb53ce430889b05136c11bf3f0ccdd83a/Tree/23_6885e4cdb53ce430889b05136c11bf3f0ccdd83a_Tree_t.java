 package sim.structures;
 
 import java.awt.Rectangle;
 import java.util.Vector;
 
 import sim.gui.elements.GuiTree;
 
 /**
  * An implementation of a tree structure. Its graphical element is the GuiTree.
  */
 public class Tree {
 
 	/**
 	 * The Traversal enum is used to specify which traversal-rule to use
 	 * on this tree when accessing elements.
 	 */
 	public enum Traversal{
 		PREORDER,
 		INORDER,
 		POSTORDER,
 		BREADTHFIRST
 	}
 	private int maxCluster = 2;
 	private TreeNode root; 
 	private GuiTree gui;
 
 	private Traversal traversal = Traversal.INORDER;
 
 	//GETTERS AND SETTERS
 	/**
 	 * The Traversal enum is used to specify which traversal-rule to use
 	 * on this tree when accessing elements.
 	 * @return The current traversal rule.
 	 */
 	public Traversal getTraversal() {
 		return traversal;
 	}
 	/**
 	 * The Traversal enum is used to specify which traversal-rule to use
 	 * on this tree when accessing elements.
 	 * @param The new traversal rule for this tree.
 	 */
 	public void setTraversal(Traversal traversal) {
 		this.traversal = traversal;
 		setIndices();
 	}
 	/**
 	 * The depth of a node is the number of nodes between it and the root.
 	 * @return The maximum depth of any node in the tree.
 	 */
 	public int getMaxDepth() {
 		return findMaxDepth(root, 0);
 	}
	/**
	 * The height of a node is the number of direct descendants.
	 * @param The node from which the height will be calculated
	 * 
	 * @return The height of the specified node.
	 */
 	public int getHeight(TreeNode t){
 		if(t==null) return 0;
 		return findMaxDepth(t, 0)-t.getDepth();
 	}
	/**
	 * Clustersize is the .
	 * 
	 * @return The height of the specified node.
	 */
 	public int getMaxCluster() {
 		return maxCluster;
 	}
 	public void setMaxCluster(int maxCluster) {
 		this.maxCluster = maxCluster;
 		rebuildTree();
 	}
 	public TreeNode getRoot() {
 		return root;
 	}	
 	public void setRoot(TreeNode root) {
 		this.root = root;
 	}
 	public String getValueAt(int index){
 		String s = (String)elementAt(index).getValue();
 		return s;
 	}
 	public void setValueAt(int index, Object value){
 		TreeNode n = elementAt(index);
 		if(n!=null)
 			n.setValue(value);
 
 		gui.repaint();
 	}
 	public GuiTree getGuiElement() {
 		return gui;
 	}
 	public void setGuiElement(GuiTree gui) {
 		this.gui = gui;
 	}
 	//CONSTRUCTORS
 	/**
 	 * Constructor.
 	 * @param bounds - the dimensions of the graphical element.
 	 * @param 
 	 */
 	public Tree(Rectangle bounds){
 		root = null;	
 		gui = new GuiTree(bounds, this);
 	}
 	protected Tree(){
 		root = null;	
 	}
 	//TREE METHODS
 	/**
 	 * Rebuilds the tree by adding breadth-first.
 	 * @param values The values to build the tree from.
 	 */
 	public void rebuildTree(){
 		if(root == null) return;
 		Vector<TreeNode> nodes = getAllNodes(new Vector<TreeNode>(), root);
 		setRoot(new TreeNode(nodes.remove(0).getValue().toString(), null));
 		for(TreeNode n : nodes)
 			addBreadthFirst(n.getValue().toString());
 
 		setIndices();
 	}
 	public void swapNodes(TreeNode a, TreeNode b){
 		Object o = a.getValue().toString();
 		a.setValue(b.getValue().toString());
 		b.setValue(o);
 	}
 	public void setIndices(){
 		Vector<TreeNode> nodes = getAllNodes(new Vector<TreeNode>(), root);
 		if(nodes != null){
 			int count = 0;
 			for(TreeNode n : nodes){
 				if(n!=null){
 					n.setCurrentIndex(count);
 					count++;
 				}
 			}
 		}
 	}
 	private int findMaxDepth(TreeNode t, int max){
 		if(t==null) return max;
 		if(t.getDepth()>max) max = t.getDepth();
 		for(Tree.TreeNode q : t.getChildren())
 		{
 			if(q!=null)
 				max = findMaxDepth(q, max);
 		}
 		return max;
 	}
 	//ADD AND INSERT METHODS
 	public void addBreadthFirst(String value){
 		gui.stopAnimation();
 		Vector<TreeNode> nodeQueue = new Vector<TreeNode>();
 		TreeNode n = root;
 		if(n == null){
 			root = new TreeNode(value, null);
 			gui.repaint();
 			return;
 		}
 		nodeQueue = getAllNodes(nodeQueue,root);
 		for(TreeNode node : nodeQueue){
 			if(node.getNumberOfChildren()<maxCluster){
 				addChildAt(node.getCurrentIndex(), value);
 				break;
 			}
 		}
 		setIndices();
 
 		gui.startAnimation();
 		gui.repaint();
 	}
 	public void addChildAt(int index, Object value){
 		gui.stopAnimation();
 		
 		TreeNode n = root;
 		if(n == null){
 			root = new TreeNode(value, null);
 			gui.repaint();
 			return;
 		}
 		TreeNode element = elementAt(index);
 		if(element == null) return;
 		TreeNode newnode = new TreeNode(value, element);
 		if(element.getNumberOfChildren()<maxCluster){
 			element.addChild(newnode);
 		}
 		else return;
 		setIndices();
 		newnode.setAdded(true);
 		gui.setChanged(newnode);
 
 
 		Vector<TreeNode> changePath = new Vector<TreeNode>();
 		changePath = getAllNodes(new Vector<TreeNode>(), root);
 
 		for(int i = 0; i<changePath.size(); i++){
 			if(changePath.get(i).getCurrentIndex()>index){
 				changePath.remove(i);
 				i--;
 			}
 		}
 		gui.setPathToChanged(changePath);
 		gui.startAnimation();
 		gui.repaint();
 	}
 	public String removeAt(int index){
 		gui.stopAnimation();
 		setIndices();
 		TreeNode element = elementAt(index);
 
 		if(element == null) return null;
 			element.setRemoved(true);
 			element.setAdded(false);
 			gui.setChanged(element);
 
 
 			Vector<TreeNode> changePath = new Vector<TreeNode>();
 			changePath = getAllNodes(new Vector<TreeNode>(), root);
 
 			for(int i = 0; i<changePath.size(); i++){
 				if(changePath.get(i).getCurrentIndex()>index){
 					changePath.remove(i);
 					i--;
 				}
 			}
 			gui.setPathToChanged(changePath);
 			gui.startAnimation();
 			gui.repaint();
 			
 			return element.getValue().toString();
 
 	}
 	public void insertAt(int index,Object value, boolean after){
 		gui.stopAnimation();
 		setIndices();
 
 		TreeNode n = root;
 		if(n == null){
 			root = new TreeNode(value, null);
 			gui.repaint();
 			return;
 		}
 		TreeNode element = elementAt(index);
 		if(element == null) return;
 		TreeNode newnode = null;
 		if(after){
 			newnode = new TreeNode(value, element);
 
 			if(element.getNumberOfChildren()==maxCluster){
 				element.getChild(0).setParent(newnode);
 				newnode.addChild(element.getChild(0));
 				element.removeChild(element.getChild(0));
 				element.addChild(newnode);
 				newnode.setParent(element);
 			}
 			else element.addChild(newnode);
 		}
 		else{
 			newnode = new TreeNode(value, element.getParent());
 			if(element == root) 
 				setRoot(newnode);
 			else if(element.getParent() != null){
 				element.getParent().getChildren().set(element.getParent().getChildren().indexOf(element), newnode);
 			}
 			element.setParent(newnode);
 			newnode.addChild(element);
 		}
 		setIndices();
 		newnode.setAdded(true);
 		gui.setChanged(newnode);
 
 
 		Vector<TreeNode> changePath = new Vector<TreeNode>();
 		changePath = getAllNodes(new Vector<TreeNode>(), root);
 
 		for(int i = 0; i<changePath.size(); i++){
 			if(changePath.get(i).getCurrentIndex()>index){
 				changePath.remove(i);
 				i--;
 			}
 		}
 		gui.setPathToChanged(changePath);
 		gui.startAnimation();
 		gui.repaint();
 		}
 
 	// GET METHODS
 	private int currentIndex = 0;
 	private TreeNode currentNode = null;
 
 	public Vector<TreeNode> getAllNodes(Vector<TreeNode> nodes, TreeNode n){
 		if(n==null) return null;
 		switch(traversal){
 		case INORDER:
 			if(n.getNumberOfChildren()>0)
 				getAllNodes(nodes, n.getChild(0));
 
 			nodes.add(n);
 
 			if(n.getNumberOfChildren()>1)
 				getAllNodes(nodes, n.getChild(1));
 			break;
 		case PREORDER:
 			nodes.add(n);
 
 			for(TreeNode t : n.getChildren())
 				if(t!=null)
 					getAllNodes(nodes, t);
 			
 			break;
 		case POSTORDER:
 			for(TreeNode t : n.getChildren())
 				if(t!=null)
 					getAllNodes(nodes, t);
 
 			nodes.add(n);
 			break;
 		case BREADTHFIRST:
 			nodes.add(n);
 			Vector<TreeNode> tobeadded = new Vector<TreeNode>();
 			for(TreeNode ch : n.getChildren())
 				if(ch != null)
 					tobeadded.add(ch);
 			while(tobeadded.size()>0){
 				nodes.add(tobeadded.firstElement());
 				
 				for(TreeNode ch : tobeadded.firstElement().getChildren())
 					if(ch != null)
 						tobeadded.add(ch);
 				
 				tobeadded.remove(tobeadded.firstElement());
 			}
 			break;
 		}
 		return nodes;
 	}
 	public TreeNode elementAt(int index){
 		currentNode = null;
 		currentIndex = 0;
 		if(root != null)
 			switch(traversal){
 			case INORDER:
 				inOrderElementAt(root, index);
 				break;
 			case PREORDER:
 				preOrderElementAt(root, index);
 				break;
 			case POSTORDER:
 				postOrderElementAt(root, index);
 				break;
 			case BREADTHFIRST:
 				breadthFirstElementAt(index);
 				break;
 			}
 		return currentNode;
 	}
 	private void breadthFirstElementAt(int index){
 		if(getRoot()==null) return;
 		currentNode = getRoot();
 		Vector<TreeNode> added = new Vector<TreeNode>();
 		Vector<TreeNode> tobeadded = new Vector<TreeNode>();
 		added.add(currentNode);
 		
 		for(TreeNode ch : currentNode.getChildren())
 			if(ch != null)
 				tobeadded.add(ch);
 		
 		while(tobeadded.size()>0){
 			added.add(tobeadded.firstElement());
 			for(TreeNode ch : tobeadded.firstElement().getChildren())
 				if(ch != null)
 					tobeadded.add(ch);
 			
 			tobeadded.remove(tobeadded.firstElement());
 		}
 		if(index <added.size())
 		currentNode = added.elementAt(index);
 	}
 	private void inOrderElementAt(TreeNode n, int index){
 		if(n.getNumberOfChildren()>0)
 			inOrderElementAt(n.getChild(0), index);		
 
 		if(index == currentIndex) currentNode = n;
 		currentIndex++;
 		if(n.getNumberOfChildren()>1)
 			inOrderElementAt(n.getChild(1), index);
 	}
 	private void preOrderElementAt(TreeNode n, int index){
 
 		if(index == currentIndex) currentNode = n;
 		currentIndex++;
 		if(n.getNumberOfChildren()>0){
 			for(TreeNode ch : n.getChildren())
 				if(ch!=null){
 					preOrderElementAt(ch, index);
 				}
 		}
 	}
 	private void postOrderElementAt(TreeNode n, int index){
 
 		for(TreeNode ch : n.getChildren()){
 			if(ch!=null)
 			postOrderElementAt(ch, index);
 		}		
 		if(index == currentIndex) currentNode = n;
 		currentIndex++;
 	}
 	public class TreeNode{
 		private TreeNode parent;		
 		private Vector<TreeNode> children;
 		private Object value;
 		private int currentIndex;
 		private boolean added;
 		private boolean removed;
 		public boolean isAdded() {
 			return added;
 		}
 		public void setAdded(boolean added) {
 			this.added = added;
 		}
 		public int getCurrentIndex() {
 			return currentIndex;
 		}
 		public void setCurrentIndex(int currentIndex) {
 			this.currentIndex = currentIndex;
 		}
 		public int getNumberOfChildren(){
 			int count = 0;
 			for(TreeNode child: children)
 				if(child!=null) count ++;
 			return count;
 		}
 		public TreeNode getChild(int num){
 			if(num>maxCluster) return null;
 			int count = 0;
 			for(TreeNode child:getChildren())
 				if(child!=null){
 
 					if(count == num)
 						return child;
 
 					count++;
 				}
 			return null;
 
 		}
 		public boolean isRemoved() {
 			return removed;
 		}
 		public void setRemoved(boolean removed) {
 			this.removed = removed;
 		}
 		public TreeNode getParent() {
 			return parent;
 		}
 		public void setParent(TreeNode parent) {
 			this.parent = parent;
 		}
 		public boolean isLeaf() {
 			return !( getNumberOfChildren()>0);
 		}
 		public int getDepth() {
 			if(getParent() == null) return 0;
 			TreeNode n = getParent();
 			int depth = 0;
 			while(n!=null){
 				n =	n.getParent();
 				depth++;
 			}
 			return depth;
 		}
 		public int getDegree(){
 			return getNumberOfChildren();
 		}
 		public Vector<TreeNode> getChildren(){
 			return children;
 		}
 		public void setChildren(Vector<TreeNode> children) {
 			this.children = children;
 		}
 		public void setValue(Object value) {
 			this.value = value;
 		}
 		public Object getValue(){
 			return value;
 		}
 		public void insert(Object value){
 			TreeNode n = new TreeNode(value, this);
 			n.setAdded(true);
 			addChild(n);
 		}
 		public void addChild(TreeNode child){
 			for(int i = 0; i<children.size(); i++)
 				if(children.get(i)==null){
 					children.set(i,child);	
 					return;
 				}
 		}
 		public void removeChild(TreeNode node){
 			if(children.indexOf(node)>=0)
 				children.set(children.indexOf(node), null);
 		}
 		public TreeNode(Object value, TreeNode parent){
 			this.value = value;
 			this.parent = parent;
 			children = new Vector<TreeNode>();
 			children.setSize(maxCluster);
 		}
 	}
 }
