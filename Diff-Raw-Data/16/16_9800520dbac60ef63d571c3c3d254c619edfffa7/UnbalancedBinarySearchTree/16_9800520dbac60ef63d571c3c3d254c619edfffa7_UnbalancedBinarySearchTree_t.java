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
  *         Class not balanced binary search tree.
  * 
  * @param <E>
  *            The value stored in the tree nodes.
  */
public class UnbalancedBinarySearchTree<E extends Comparable<? super E>> extends
 		BinarySearchTree<E> {
 
 	/**
 	 * Find best place and insert node to the tree, according to next rules: -
 	 * The left subtree of a node contains only nodes with keys less than the
 	 * node's key. - The right subtree of a node contains only nodes with keys
 	 * greater than the node's key. - The left and right subtree must each also
 	 * be a binary search tree. - There must be no duplicate nodes.
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
 				return true;
 			} else {
 				return insertNode(node, parent.getRight());
 			}
 		}
 
 		if (node.value.compareTo(parent.value) < 0) {
 			if (parent.getLeft() == null) {
 				node.setParent(parent);
 				parent.setLeft(node);
 				return true;
 			} else {
 				return insertNode(node, parent.getLeft());
 			}
 		}
 
 		// node.value.compareTo(parent.value) == 0
 		return false;
 	}
 
 }
