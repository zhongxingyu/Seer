 package sim.structures;
 
 import java.awt.Rectangle;
 import java.util.Vector;
 
 import sim.gui.elements.GuiTree;
 
 
 public class Tree {
 
 	/**
 	 * @param args
 	 */
 	public enum Traversal{
 		PREORDER,
 		INORDER,
 		POSTORDER
 	}
 	GuiTree gui;
 	public int getMaxDepth() {
 		return findMaxDepth(root, 0);
 	}
 	public int getMaxCluster() {
 		return maxCluster;
 	}
 	public void setMaxCluster(int maxCluster) {
 		this.maxCluster = maxCluster;
 		rebuildTree();
 	}
 	int maxCluster = 2;
 	
 	TreeNode root; 
 	
 	public TreeNode getRoot() {
 		return root;
 	}
 	public void setRoot(TreeNode root) {
 		this.root = root;
 	}
 	public GuiTree getGuiElement() {
 		return gui;
 	}
 	public Tree(Rectangle bounds,boolean animated){
 		root = new TreeNode("root", null);	
 		gui = new GuiTree(bounds, this, animated);
 	}
 	protected Tree(){
 		root = new TreeNode("root", null);	
 	}
 	
 	public void rebuildTree(){
 		Vector<TreeNode> nodes = getAllNodes(new Vector<TreeNode>(), root);
 		nodes.remove(root);
 		root = new TreeNode("root", null);
 		//nodes.remove(root);
 		for(TreeNode n : nodes)
 			addBreadthFirst(n.getValue().toString());
 	}
 	public Vector<TreeNode> getAllNodes(Vector<TreeNode> nodes, TreeNode n){
 		nodes.add(n);
 		for(TreeNode t : n.getChildren())
 			getAllNodes(nodes, t);
 		return nodes;
 	}
 	
 	public void swapNodes(TreeNode a, TreeNode b){
 		Object o = a.getValue().toString();
 		a.setValue(b.getValue().toString());
 		b.setValue(o);
 	}
 	public void addBreadthFirst(String value){
 		Vector<TreeNode> nodeQueue = new Vector<TreeNode>();
 		TreeNode n = root;
 		nodeQueue.addAll(n.getChildren());
 		while(n.getChildren().size()==maxCluster && nodeQueue.size()>0){
 			nodeQueue.addAll(n.getChildren());
 			n = nodeQueue.remove(0);
 		}
 		n.insert(value);
 	}
 	// Get element methods
 	private int currentIndex = 0;
 	private TreeNode currentNode = null;
 	private Traversal traversal = Traversal.INORDER;
 	public Traversal getTraversal() {
 		return traversal;
 	}
 	public void setTraversal(Traversal traversal) {
 		this.traversal = traversal;
 	}
 	public void addChildAt(int index, Object value){
 		TreeNode element = elementAt(index);
 		TreeNode newnode = new TreeNode(value, element);
 //FAEN TA GIT
 		if(element.getChildren().size()<maxCluster){
 			element.getChildren().add(newnode);
 		}
 		else addBreadthFirst((String)value);
 		gui.repaint();
 	}
 	public void insertAt(int index,Object value){
 		TreeNode element = elementAt(index);
 		TreeNode newnode = new TreeNode(value, element);
 
 		if(element.getChildren().size()>0){
 			element.getChildren().get(0).setParent(newnode);
 			newnode.getChildren().add(element.getChildren().get(0));
 			element.getChildren().remove(0);
 			element.getChildren().add(0, newnode);
 			}
 		else
			element.insert(newnode.getValue());
 		gui.repaint();
 	}
 	public TreeNode elementAt(int index){
 		currentNode = null;
 		currentIndex = 0;
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
 		}
 		return currentNode;
 	}
 	private void inOrderElementAt(TreeNode n, int index){
 		if(n.getChildren().size()>0)
 		inOrderElementAt(n.getChildren().elementAt(0), index);		
 		currentIndex++;
 		if(index == currentIndex) currentNode = n;
 		
 		if(n.getChildren().size()>1)
 			inOrderElementAt(n.getChildren().elementAt(1), index);
 	}
 	private void preOrderElementAt(TreeNode n, int index){
 		currentIndex++;
 		if(index == currentIndex) currentNode = n;
 		
 		for(int i= 0; i<n.getChildren().size()-1; i++){
 					preOrderElementAt(n.getChildren().elementAt(i), index);
 		}		
 		if(n.getChildren().size()>0)
 			preOrderElementAt(n.getChildren().elementAt(n.getChildren().size()-1), index);
 	}
 	private void postOrderElementAt(TreeNode n, int index){
 
 		for(int i= 0; i<n.getChildren().size()-1; i++){
 					postOrderElementAt(n.getChildren().elementAt(i), index);
 		}		
 		if(n.getChildren().size()>0)
 			postOrderElementAt(n.getChildren().elementAt(n.getChildren().size()-1), index);
 		
 		currentIndex++;
 		if(index == currentIndex) currentNode = n;
 	}
 	public int findMaxCluster(TreeNode t, int max){
 		
 		if(t.getChildren().size()>max) max = t.getChildren().size();
 		for(Tree.TreeNode q : t.getChildren())
 		{
 			max = findMaxCluster(q, max);
 		}
 		return max;
 	}
 	public int getHeight(TreeNode t){
 		if(t==null) return 0;
 		return findMaxDepth(t, 0)-t.getDepth();
 	}
 	public int findMaxDepth(TreeNode t, int max){
 		if(t==null) return max;
 		if(t.getDepth()>max) max = t.getDepth();
 		for(Tree.TreeNode q : t.getChildren())
 		{
 			max = findMaxDepth(q, max);
 		}
 		return max;
 	}
 	public class TreeNode{
 		private TreeNode parent;
 		private boolean added;
 		private boolean removed;
 		public boolean isAdded() {
 			return added;
 		}
 		public void setAdded(boolean added) {
 			this.added = added;
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
 		private Vector<TreeNode> children;
 		private Object value;
 		
 		public boolean isLeaf() {
 			return !(children.size()>0);
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
 			return children.size();
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
 			addSubTree(n);
 		}
 		public void addSubTree(TreeNode root){
 			children.add(root);	
 		}
 		public TreeNode(Object value, TreeNode parent){
 			this.value = value;
 			this.parent = parent;
 			children = new Vector<TreeNode>();
 		}
 	}
 }
