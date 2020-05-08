 package questions;
 
 /** Model of the Questions game. */
 public class Game {
 
 	/** The root of the tree of knowledge. */
 	public Node root;
 
 	/** Current node in the decision tree. */
 	private Node currentNode;
 
 	/** A new Game has two questions and three leaves. */
 	public Game() {
 		root = new Node("Does it have a motor?");
 		root.setLeft(new Node("Does it store information?", new Node(
 				"a hard drive"), new Node("a car")));
 		root.setRight(new Node("a giraffe"));
 	}
 
 	/** True if the current node is a leaf. */
 	public boolean atLeaf() {

 	}
 
 	/**
 	 * Updates the current node to the left or right child, respectively, if
 	 * answer is true or false.
 	 */
 	public void descend(boolean answer) {
 		if (answer) {
 			currentNode = currentNode.getLeft();
 		} else {
 			currentNode = currentNode.getRight();
 		}
 	}
 
 	/** Returns the current node. */
 	public Node getCurrentNode() {
 		return currentNode;
 	}
 
 	/** Returns the question asked of the user for learning after losing a game. */
 	public String getLearningQuestion(String correct) {
		// TODO You have to write this.
 	}
 
 	/** Returns the questions asked at the current node. */
 	public String getQuestion() {
		// TODO You have to write this.
 	}
 
 	/** Returns the root of this game's decision tree. */
 	public Node getRoot() {
 		return root;
 	}
 
 	/**
 	 * Replaces the key of the current node (a leaf) with question and gives it
 	 * two children. The left child's key is correct. The right child's key is
 	 * this node's old key.
 	 * 
 	 * This is a "delegate" method that simply calls the version from Node.
 	 */
 	public void learn(String correct, String question) {
		learn(correct, question);
 	}
 
 	/** Resets the current node to be the root of the decision tree. */
 	public void reset() {
 		currentNode = root;
 	}
 
 	@Override
 	public String toString() {
 		return root.toString();
 	}
 
 }
