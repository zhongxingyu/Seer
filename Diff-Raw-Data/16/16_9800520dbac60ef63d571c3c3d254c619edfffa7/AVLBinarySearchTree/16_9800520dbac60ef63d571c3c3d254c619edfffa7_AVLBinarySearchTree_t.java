 /**
  * Copyright 2013 Semen A Martynov <semen.martynov@gmail.com>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package ru.spbau.martynov.task6;
 
 /**
  * 
  * @author Semen Martynov
  * 
  *         Class for self-balancing binary search tree (AVL).
  * 
  * @param <E>
  *            The value stored in the tree nodes.
  */
public class AVLBinarySearchTree<E extends Comparable<? super E>> extends
 		BinarySearchTree<E> {
 
 	/**
 	 * Find best place and insert node to the tree, according to next rules: -
 	 * The left subtree of a node contains only nodes with keys less than the
 	 * node's key. - The right subtree of a node contains only nodes with keys
 	 * greater than the node's key. - The left and right subtree must each also
 	 * be a binary search tree. - There must be no duplicate nodes. - At any
 	 * moment, the difference of heights of subtrees can't exceed 1.
 	 * 
 	 * @param node
 	 *            to insert.
 	 * @param parent
 	 *            - point for bypass.
 	 * @return true if node inserted, and false if not (if node already
 	 *         presented).
 	 */
 	@Override
 	protected boolean insertNode(Node node, Node parent) {
 		if (node.value.compareTo(parent.value) > 0) {
 			if (parent.getRight() == null) {
 				node.setParent(parent);
 				parent.setRight(node);
 				if (parent.getLeft() == null) {
 					balancing(parent);
 				}
 				return true;
 			} else {
 				return insertNode(node, parent.getRight());
 			}
 		}
 
 		if (node.value.compareTo(parent.value) < 0) {
 			if (parent.getLeft() == null) {
 				node.setParent(parent);
 				parent.setLeft(node);
 				if (parent.getRight() == null) {
 					balancing(parent);
 				}
 				return true;
 			} else {
 				return insertNode(node, parent.getLeft());
 			}
 		}
 
 		// node.value.compareTo(parent.value) == 0
 		return false;
 	}
 
 	/**
 	 * Function increases value of nodes height in a tree, and defines need of
 	 * turn, for maintenance of the balanced status.
 	 * 
 	 * @param node
 	 *            node in which balance is checked.
 	 */
 	private void balancing(Node node) {
 		// Let P be the root of the deepest unbalanced subtree, with R and L
 		// denoting the right and left children of P respectively.
 		while (node != null) {
 			int leftSubTreeHeight = 0;
 			if (node.getLeft() != null) {
 				leftSubTreeHeight = node.getLeft().getHeight();
 			}
 
 			int rightSubTreeHeight = 0;
 			if (node.getRight() != null) {
 				rightSubTreeHeight = node.getRight().getHeight();
 			}
 
 			node.setHeight(leftSubTreeHeight > rightSubTreeHeight ? leftSubTreeHeight + 1
 					: rightSubTreeHeight + 1);
 
 			// If the balance factor of P is -2 then the right subtree outweighs
 			// the left subtree of the given node, and the balance factor of the
 			// right child (R) must be checked. The left rotation with P as the
 			// root is necessary.
 			if (leftSubTreeHeight - rightSubTreeHeight == -2) {
 				// If the balance factor of R is +1, two different rotations are
 				// needed. The first rotation is a right rotation with R as the
 				// root. The second is a left rotation with P as the root
 				// (Right-Left case).
 				if (node.getRight().getRight() == null
 						|| node.getRight().getLeft() != null
 						&& node.getRight().getLeft().getHeight()
 								- node.getRight().getRight().getHeight() > 0) {
 					node = rightLeftCase(node);
 				} else
 				// If the balance factor of R is -1 (or in case of deletion also
 				// 0), a single left rotation (with P as the root) is needed
 				// (Right-Right case).
 				{
 					node = rightRightCase(node);
 				}
 			} else
 			// If the balance factor of P is 2, then the left subtree outweighs
 			// the right subtree of the given node, and the balance factor of
 			// the left child (L) must be checked. The right rotation with P as
 			// the root is necessary.
 			if (leftSubTreeHeight - rightSubTreeHeight == 2) {
 				// If the balance factor of L is -1, two different rotations are
 				// needed. The first rotation is a left rotation with L as the
 				// root. The second is a right rotation with P as the root
 				// (Left-Right case).
 				if (node.getLeft().getLeft() == null
 						|| node.getLeft().getRight() != null
 						&& node.getLeft().getLeft().getHeight()
 								- node.getLeft().getRight().getHeight() < 0) {
 					node = leftRightCase(node);
 				} else
 				// If the balance factor of L is +1 (or in case of deletion also
 				// 0), a single right rotation (with P as the root) is needed
 				// (Left-Left case).
 				{
 					node = leftLeftCase(node);
 				}
 			}
 
 			node = node.getParent();
 		}
 	}
 
 	/**
 	 * Two different rotations (right and left) for maintenance of the balanced
 	 * status.
 	 * 
 	 * @param node
 	 *            in which the balance is broken.
 	 * @return node with regenerated balance.
 	 */
 	private Node rightLeftCase(Node node) {
 		leftLeftCase(node.getRight());
 		return rightRightCase(node);
 	}
 
 	/**
 	 * Right rotations for maintenance of the balanced status.
 	 * 
 	 * @param node
 	 *            in which the balance is broken.
 	 * @return node with regenerated balance.
 	 */
 	private Node rightRightCase(Node node) {
 		Node temp = node.getRight();
 
 		// outweighs the left sub tree
 		node.setRight(temp.getLeft());
 		if (temp.getLeft() != null) {
 			temp.getLeft().setParent(node);
 		}
 
 		// notify parent
 		if (node.getParent() != null) {
 			if (node.getParent().getLeft() == node) {
 				node.getParent().setLeft(temp);
 			} else {
 				node.getParent().setRight(temp);
 			}
 		} else {
 			setRoot(temp);
 		}
 		temp.setParent(node.getParent());
 
 		// rotate
 		temp.setLeft(node);
 		node.setParent(temp);
 
 		// rebuild height
 		int leftSubTreeHeight = 0;
 		if (node.getLeft() != null) {
 			leftSubTreeHeight = node.getLeft().getHeight();
 		}
 
 		int rightSubTreeHeight = 0;
 		if (node.getRight() != null) {
 			rightSubTreeHeight = node.getRight().getHeight();
 		}
 
 		node.setHeight(leftSubTreeHeight > rightSubTreeHeight ? ++leftSubTreeHeight
 				: ++rightSubTreeHeight);
 
 		// rebuild height in temp (new root)
 		int leftTempSubTreeHeight = node.getHeight();
 		int rightTempSubTreeHeight = 0;
 		if (node.getRight() != null) {
 			rightTempSubTreeHeight = node.getRight().getHeight();
 		}
 
 		temp.setHeight(leftTempSubTreeHeight > rightTempSubTreeHeight ? ++leftTempSubTreeHeight
 				: ++rightTempSubTreeHeight);
 
 		return temp;
 	}
 
 	/**
 	 * Two different rotations (left and right) for maintenance of the balanced
 	 * status.
 	 * 
 	 * @param node
 	 *            in which the balance is broken.
 	 * @return node with regenerated balance.
 	 */
 	private Node leftRightCase(Node node) {
 		rightRightCase(node.getLeft());
 		return leftLeftCase(node);
 	}
 
 	/**
 	 * Right rotations for maintenance of the balanced status.
 	 * 
 	 * @param node
 	 *            in which the balance is broken.
 	 * @return node with regenerated balance.
 	 */
 	private Node leftLeftCase(Node node) {
 		Node temp = node.getLeft();
 
 		// outweighs the right sub tree
 		node.setLeft(temp.getRight());
 		if (temp.getRight() != null) {
 			temp.getRight().setParent(node);
 		}
 
 		// notify parent
 		if (node.getParent() != null) {
 			if (node.getParent().getLeft() == node) {
 				node.getParent().setLeft(temp);
 			} else {
 				node.getParent().setRight(temp);
 			}
 		} else {
 			setRoot(temp);
 		}
 		temp.setParent(node.getParent());
 
 		// rotate
 		temp.setRight(node);
 		node.setParent(temp);
 
 		// rebuild height in node
 		int leftNodeSubTreeHeight = 0;
 		if (node.getLeft() != null) {
 			leftNodeSubTreeHeight = node.getLeft().getHeight();
 		}
 
 		int rightNodeSubTreeHeight = 0;
 		if (node.getRight() != null) {
 			rightNodeSubTreeHeight = node.getRight().getHeight();
 		}
 
 		node.setHeight(leftNodeSubTreeHeight > rightNodeSubTreeHeight ? ++leftNodeSubTreeHeight
 				: ++rightNodeSubTreeHeight);
 
 		// rebuild height in temp (new root)
 		int leftTempSubTreeHeight = 0;
 		if (temp.getLeft() != null) {
 			leftTempSubTreeHeight = temp.getLeft().getHeight();
 		}
 
 		int rightTempSubTreeHeight = node.getHeight();
 
 		temp.setHeight(leftTempSubTreeHeight > rightTempSubTreeHeight ? ++leftTempSubTreeHeight
 				: ++rightTempSubTreeHeight);
 
 		return temp;
 	}
 }
