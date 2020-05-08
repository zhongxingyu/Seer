 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // PrioritySearchTree.java
 // Since: Dec 3, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.canvas;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 /**
  * Priority search tree for efficient 2D search
  * 
  * @author leo
  * 
  */
 public class PrioritySearchTree<E> implements Iterable<E> {
 
 	public class Node {
 		public E elem;
 		public int x;
 		public int y;
 		public int splitX;
 		public Node left;
 		public Node right;
 
 		public Node(E node, int x, int y) {
 			if (node == null)
 				throw new NullPointerException("node cannot be null");
 			this.elem = node;
 			this.x = x;
 			this.y = y;
 		}
 
 		public void swap(Node n) {
 			// swap x
 			int tmpX = this.x;
 			this.x = n.x;
 			n.x = tmpX;
 
 			// swap y
 			int tmpY = this.y;
 			this.y = n.y;
 			n.y = tmpY;
 
 			// swap node
 			E tmpNode = this.elem;
 			this.elem = n.elem;
 			n.elem = tmpNode;
 		}
 
 		public void replaceWith(Node n) {
 			this.x = n.x;
 			this.y = n.y;
 			this.elem = n.elem;
 		}
 	}
 
 	private Node root = null;
 	private int lowerBoundOfX = 0;
 	private int upperBoundOfX = Integer.MAX_VALUE;
 	private int lowerBoundOfY = 0;
 	private int uppperBoundOfY = Integer.MAX_VALUE;
 	private int nodeCount = 0;
 
 	public PrioritySearchTree() {
 
 	}
 
 	public PrioritySearchTree(int lowerBoundOfX, int upperBoundOfX, int lowerBoundOfY, int upperBoundOfY) {
 		this.lowerBoundOfX = lowerBoundOfX;
 		this.upperBoundOfX = upperBoundOfX;
 		this.lowerBoundOfY = lowerBoundOfY;
 		this.uppperBoundOfY = upperBoundOfY;
 	}
 
 	public static interface Visitor<E> {
 		public void visit(E visit);
 	}
 
 	private class DFSIterator implements Iterator<E> {
 
 		private Stack<Node> nodeStack = new Stack<Node>();
 
 		public DFSIterator(Node root) {
 			if (root != null)
 				nodeStack.push(root);
 		}
 
 		public boolean hasNext() {
 			return !nodeStack.isEmpty();
 		}
 
 		public E next() {
 			if (!hasNext())
 				return null;
 
 			Node nextNode = nodeStack.pop();
 			if (nextNode.right != null)
 				nodeStack.push(nextNode.right);
 			if (nextNode.left != null)
 				nodeStack.push(nextNode.left);
 			return nextNode.elem;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException("remove");
 		}
 
 	}
 
 	public Iterator<E> iterator() {
 		return new DFSIterator(root);
 	}
 
 	public void depthFirstSearch(Visitor<E> visitor) {
 		dfs(root, visitor);
 	}
 
 	private void dfs(Node current, Visitor<E> visitor) {
 		if (current == null)
 			return;
 		visitor.visit(current.elem);
 		dfs(current.left, visitor);
 		dfs(current.right, visitor);
 	}
 
 	private static class QueryBox {
 		public final int x1;
 		public final int x2;
 		public final int upperY;
 
 		public QueryBox(int x1, int x2, int upperY) {
 			this.x1 = x1;
 			this.x2 = x2;
 			this.upperY = upperY;
 		}
 
 	}
 
 	public void clear() {
 		root = null;
 		nodeCount = 0;
 	}
 
 	public int size() {
 		return nodeCount;
 	}
 
 	/**
 	 * Retrieves elements contained in the specified range, (X:[x1, x2], Y:[ , upperY]). This query is useful for
 	 * answering the interval intersection problem.
 	 * 
 	 * @param x1
 	 * @param x2
 	 * @param upperY
 	 * @return elements contained in the range (X:[x1, x2], Y:[ , upperY])
 	 */
 	public List<E> rangeQuery(int x1, int x2, int upperY) {
 		ArrayList<E> result = new ArrayList<E>();
 		ResultCollector<E> resultCollector = new ResultCollector<E>();
 		rangeQuery_internal(root, new QueryBox(x1, x2, upperY), x1, x2, resultCollector);
 		return resultCollector.result;
 	}
 
 	public void rangeQuery(int x1, int x2, int upperY, ResultHandler<E> handler) {
 		rangeQuery_internal(root, new QueryBox(x1, x2, upperY), x1, x2, handler);
 	}
 
 	public static interface ResultHandler<E> {
 		public void handle(E elem);
 
 		public boolean toContinue();
 	}
 
 	private static class ResultCollector<E> implements ResultHandler<E> {
 
 		public List<E> result = new ArrayList<E>();
 
 		public void handle(E elem) {
 			result.add(elem);
 		}
 
 		public boolean toContinue() {
 			return true;
 		}
 
 	}
 
 	boolean rangeQuery_internal(Node currentNode, QueryBox queryBox, int rangeX1, int rangeX2, ResultHandler<E> resultHandler) {
 		boolean toContinue = resultHandler.toContinue();
		if (!toContinue || rangeX1 > rangeX2)
 			return false;
 
 		if (currentNode != null) {
 			if (currentNode.y <= queryBox.upperY) {
 				// the current node is within the y constraint
 				if (queryBox.x1 <= currentNode.x && currentNode.x <= queryBox.x2) {
 					// The current node is contained in the query box
 					resultHandler.handle(currentNode.elem);
 					toContinue = resultHandler.toContinue();
 				}
 
 				// search the descendant nodes
 				int middleX = currentNode.splitX;
 
 				// search the left tree
 				if (toContinue && queryBox.x1 < middleX) {
 					toContinue = rangeQuery_internal(currentNode.left, queryBox, rangeX1, middleX, resultHandler);
 				}
 
 				// search the right tree
 				if (toContinue && middleX <= queryBox.x2) {
 					toContinue = rangeQuery_internal(currentNode.right, queryBox, middleX, rangeX2, resultHandler);
 				}
 			}
 		}
 
 		return toContinue;
 
 	}
 
 	/**
 	 * Insert a new node
 	 * 
 	 * @param elem
 	 */
 	public void insert(E elem, int x, int y) {
 		root = insert_internal(root, new Node(elem, x, y), lowerBoundOfX, upperBoundOfX);
 	}
 
 	/**
 	 * Remove the specified node
 	 * 
 	 * @param elem
 	 * @return true if the specified element exists in the tree
 	 */
 	public boolean remove(E elem, int x, int y) {
 		int prevNumNodes = size();
 		root = remove_internal(root, new Node(elem, x, y), lowerBoundOfX, upperBoundOfX);
 		return prevNumNodes != size();
 	}
 
 	Node insert_internal(Node currentNode, Node insertNode, int lowerRangeOfX, int upperRangeOfX) {
 		if (currentNode == null) {
 			// empty leaf is found. Insert the new node here
 			currentNode = insertNode;
 			currentNode.splitX = (lowerRangeOfX + upperRangeOfX) / 2;
 			nodeCount++;
 		}
 		else {
 			if (insertNode.y < currentNode.y) {
 				currentNode.swap(insertNode);
 			}
 
 			if (insertNode.x < currentNode.splitX)
 				currentNode.left = insert_internal(currentNode.left, insertNode, lowerRangeOfX, currentNode.splitX);
 			else
 				currentNode.right = insert_internal(currentNode.right, insertNode, currentNode.splitX, upperRangeOfX);
 		}
 
 		return currentNode;
 	}
 
 	Node remove_internal(Node currentNode, Node removeTarget, int x_lower, int x_upper) {
 		if (currentNode == null) {
 			// no node to delete
 			return currentNode;
 		}
 
 		if (currentNode.elem.equals(removeTarget.elem)) {
 			// current node is the deletion target
 			if (currentNode.left != null) {
 				if (currentNode.right != null) {
 					if (currentNode.left.y < currentNode.right.y) {
 						// left node has lower Y than the right one
 						currentNode.replaceWith(currentNode.left);
 						currentNode.left = remove_internal(currentNode.left, currentNode.left, x_lower, x_upper);
 					}
 					else {
 						// right node has lower Y than the left one
 						currentNode.replaceWith(currentNode.right);
 						currentNode.right = remove_internal(currentNode.right, currentNode.right, x_lower, x_upper);
 					}
 				}
 				else {
 					// only the left subtree exists
 					currentNode.replaceWith(currentNode.left);
 					currentNode.left = remove_internal(currentNode.left, currentNode.left, x_lower, x_upper);
 				}
 
 			}
 			else {
 				if (currentNode.right != null) {
 					// only the right subtree exists
 					currentNode.replaceWith(currentNode.right);
 					currentNode.right = remove_internal(currentNode.right, currentNode.right, x_lower, x_upper);
 				}
 				else {
 					// no subtree exists, so delete the currentNode by returning null
 					nodeCount--;
 					return null;
 				}
 			}
 		}
 		else {
 			// node to be deleted exists in one of the subtrees
 			if (removeTarget.x < currentNode.splitX)
 				currentNode.left = remove_internal(currentNode.left, removeTarget, x_lower, currentNode.splitX);
 			else
 				currentNode.right = remove_internal(currentNode.right, removeTarget, currentNode.splitX, x_upper);
 		}
 
 		return currentNode;
 	}
 
 }
