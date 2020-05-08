 
 public class Tree {
 
 	private TreeNode root;
 	private boolean linked;
 	
 	public Tree(){
 		this(null);
 	}
 	
 	public Tree(TreeNode root){
 		setRoot(root);
 	}
 	
 	public void add(int i){
 		add(new TreeNode(i));
 	}
 	
 	public void add(TreeNode node){
 		if(root == null)
 			setRoot(node);
 		TreeNode current = root;
 		TreeNode side = null;
 		do{
 			if(current.getValue().compareTo(node.getValue()) >= 0){
 				side = current.getLeft();
 			}
 			else{
 				side = current.getRight();
 			}
 			if(side == null)
 				break;
 			current = side;
 		}while(side != null);
 		if(current.getValue().compareTo(node.getValue()) >= 0){
 			System.out.println(node + " added to the left");
 			current.setLeft(node);
 		}
 		else{
 			System.out.println(node + " added to the right");
 			current.setRight(node);
 		}
 	}
 	
 	public String toString(){
 		if(!linked)
 			return orderedToString(root);
 		return linkedToString();
 	}
 	
 	public String orderedToString(){
 		return orderedToString(root);
 	}
 	
 	/**
 	 * Returns a ordered string representation of the tree
 	 * @param root
 	 * @return
 	 */
 	public String orderedToString(TreeNode root){
 		
 		if(root == null)
 			return "";
 		String s = "";
 		s += orderedToString(root.getLeft());
 		s += root.toString();
 		s += orderedToString(root.getRight());
 		return s;
 		
 	}
 	
 	public String preorderToString(){
 		return preorderToString(root);
 	}
 	
 	/**
 	 * Returns a pre order string representation of the tree
 	 * @param root
 	 * @return
 	 */
 	public String preorderToString(TreeNode root){
 		
 		if(root == null)
 			return "";
 		String s = "";
 		s += root.toString();
 		s += preorderToString(root.getLeft());
 		s += preorderToString(root.getRight());
 		return s;
 		
 	}
 	
 	public String postorderToString(){
 		return postorderToString(root);
 	}
 	
 	/**
 	 * Returns a post order string representation of the tree
 	 * @param root
 	 * @return
 	 */
 	public String postorderToString(TreeNode root){
 		
 		if(root == null)
 			return "";
 		String s = "";
 		s += postorderToString(root.getLeft());
 		s += postorderToString(root.getRight());
 		s += root.toString();
 		return s;
 		
 	}
 	
 	/**
 	 * @return the root
 	 */
 	public TreeNode getRoot() {
 		return root;
 	}
 
 	/**
 	 * @param root the root to set
 	 */
 	public void setRoot(TreeNode root) {
 		this.root = root;
 	}
 	
 	/**
 	 * Starts the conversion of the tree into a linked list
 	 */
 	public void convert(){
 		root = convert(root);
 		linked = true;
 	}
 	
 	/**
 	 * Recursively converts the Tree into a linked list that is ordered
 	 * @param subRoot
 	 * @return
 	 */
 	public TreeNode convert(TreeNode subRoot){
 		if(subRoot == null)
 			return null;
 		if(subRoot.getLeft() == null && subRoot.getRight() == null){
 			subRoot = setHead(subRoot);
 			return subRoot;
 		}
 		
 		TreeNode leftHead = convert(subRoot.getLeft());
 		TreeNode tempRight = subRoot.getRight();
 		if(leftHead == null){
 			leftHead = setHead(subRoot);
 		}
 		
 		TreeNode rightHead = convert(tempRight);
 		if(rightHead == null)
 			rightHead = setHead(subRoot);
 		
 		subRoot = setHead(subRoot);
 		link(leftHead, subRoot);
 		link(leftHead, rightHead);
 		
		//FIX
 		return leftHead;
 	}
 	
 	/**
 	 * Sets the giving TreeNode as the "head" of a linked list by
 	 * setting its references to itself.
 	 * @param head -- node to be treated as the head of a Linked List
 	 * @return a reference to the same node
 	 */
 	private TreeNode setHead(TreeNode head) {
 		
		head.setRight(head);
 		head.setLeft(head);
 		
 		return head;
 	}
 	
 	/**
 	 * Returns the node at the farthest right of the roo
 	 * @param root node
 	 * @return
 	 */
 	private TreeNode getFarthestRight(TreeNode root) {
 		TreeNode curr = root.getRight();
 		while(curr.getRight() != null){
 			curr = curr.getRight();
 		}
 		return curr;
 	}
 	
 	/**
 	 * Links two linked lists together
 	 * @param firstHead -- the head of the first linked list
 	 * @param secondHead -- the head of the second linked list to be linked to
 	 * the end of the first
 	 * @return the firstHead after linked to the second list
 	 */
 	private TreeNode link(TreeNode firstHead, TreeNode secondHead){
 		
 		if(firstHead == secondHead)
 			return setHead(firstHead);
 		
 		TreeNode firstTail = firstHead.getLeft();
 		if(firstTail == null)
 			firstTail = getFarthestRight(firstHead);
 		TreeNode secondTail = secondHead.getLeft();
 		if(secondTail == null)
 			secondTail = getFarthestRight(secondHead);
 		firstTail.setRight(secondHead);
 		secondHead.setLeft(firstTail);
 		
 		firstHead.setLeft(secondTail);
 		secondTail.setRight(firstHead);
 		
 		return firstHead;
 	}
 
 	/**
 	 * The toString after the tree is converted to a linked list
 	 * @return
 	 */
 	private String linkedToString() {
 		String s = "";
 		TreeNode curr = root.getRight();
 		s += root.toString();
 		while(curr != root){
 			s += curr.toString();
 			curr = curr.getRight();
 		}
 		return s;
 	}
 	
 }
