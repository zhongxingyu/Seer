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
 
 import java.util.AbstractSet;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.NavigableSet;
 import java.util.NoSuchElementException;
 import java.util.SortedSet;
 
 /**
  * 
  * @author Semen Martynov
  * 
  *         Abstract class of binary search tree.
  * 
  * @param <E>
  *            The value stored in the tree nodes.
  */
 public abstract class BinarySearchTree<E extends Comparable<E>> extends
 		AbstractSet<E> implements NavigableSet<E> {
 
 	/**
 	 * 
 	 * @author Semen Martynov
 	 * 
 	 *         Node class from which the tree is constructed.
 	 * 
 	 */
 	protected class Node {
 		/**
 		 * Stored value.
 		 */
 		public final E value;
 
 		/**
 		 * Parent node.
 		 */
 		private Node parent = null;
 		/**
 		 * Left node.
 		 */
 		private Node left = null;
 		/**
 		 * Right node.
 		 */
 		private Node right = null;
 		/**
 		 * Height of the node in the tree.
 		 */
 		private int height = 1;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param value
 		 *            - stored value.
 		 */
 		Node(final E value) {
 			this.value = value;
 		}
 
 		/**
 		 * @return the parent node.
 		 */
 		public Node getParent() {
 			return parent;
 		}
 
 		/**
 		 * @return the left node.
 		 */
 		public Node getLeft() {
 			return left;
 		}
 
 		/**
 		 * @return the right node.
 		 */
 		public Node getRight() {
 			return right;
 		}
 
 		/**
 		 * @return the height.
 		 */
 		public int getHeight() {
 			return height;
 		}
 
 		/**
 		 * @param parent
 		 *            the parent node to set.
 		 */
 		public void setParent(Node parent) {
 			this.parent = parent;
 		}
 
 		/**
 		 * @param left
 		 *            the left node to set.
 		 */
 		public void setLeft(Node left) {
 			this.left = left;
 		}
 
 		/**
 		 * @param right
 		 *            the right node to set.
 		 */
 		public void setRight(Node right) {
 			this.right = right;
 		}
 
 		/**
 		 * @param height
 		 *            the height to set.
 		 */
 		public void setHeight(int height) {
 			this.height = height;
 		}
 	}
 
 	/**
 	 * The root of the tree.
 	 */
 	private Node root = null;
 
 	/**
 	 * Add value to the tree.
 	 * 
 	 * @param e
 	 *            stored value for a node which will be added to this tree.
 	 * 
 	 * @return true if node inserted, and false if not.
 	 */
 	@Override
 	public boolean add(E e) {
 		Node node = new Node(e);
 
 		if (root == null) {
 			root = node;
 		} else {
 			return insertNode(node, root);
 		}
 		return true;
 	}
 
 	/**
 	 * Returns the least element in this set greater than or equal to the given
 	 * element, or {@code null} if there is no such element.
 	 * 
 	 * @param e
 	 *            the value to match
 	 * @return the least element greater than or equal to {@code e}, or
 	 *         {@code null} if there is no such element
 	 * @throws ClassCastException
 	 *             if the specified element cannot be compared with the elements
 	 *             currently in the set
 	 * @throws NullPointerException
 	 *             if the specified element is null and this set does not permit
 	 *             null elements
 	 */
 	@Override
 	public E ceiling(E e) {
 		if (e == null) {
 			throw new NullPointerException("Parametr is null");
 		}
 
 		if (e.compareTo(first()) < 0 || e.compareTo(last()) > 0) {
 			return null;
 		}
 
 		Node current = minNode(root);
 		while (nextSetNode(current) != null && e.compareTo(current.value) > 0
 				&& e.compareTo(nextSetNode(current).value) >= 0) {
 			current = nextSetNode(current);
 		}
 
 		return e.compareTo(current.value) == 0 ? current.value
 				: nextSetNode(current).value;
 	}
 
 	/**
 	 * Returns the comparator used to order the elements in this set, or
 	 * <tt>null</tt> if this set uses the {@linkplain Comparable natural
 	 * ordering} of its elements.
 	 * 
 	 * @return the comparator used to order the elements in this set, or
 	 *         <tt>null</tt> if this set uses the natural ordering of its
 	 *         elements
 	 */
 	@Override
 	public Comparator<? super E> comparator() {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public Iterator<E> descendingIterator() {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public NavigableSet<E> descendingSet() {
 		return null;
 	}
 
 	/**
 	 * Returns the first (lowest) element currently in this set.
 	 * 
 	 * @return the first (lowest) element currently in this set
 	 * @throws NoSuchElementException
 	 *             if this set is empty
 	 */
 	@Override
 	public E first() {
 		if (root == null) {
 			throw new NoSuchElementException("Tree is empty");
 		}
 		return minNode(root).value;
 	}
 
 	/**
 	 * Returns the greatest element in this set less than or equal to the given
 	 * element, or {@code null} if there is no such element.
 	 * 
 	 * @param e
 	 *            the value to match
 	 * @return the greatest element less than or equal to {@code e}, or
 	 *         {@code null} if there is no such element
 	 * @throws ClassCastException
 	 *             if the specified element cannot be compared with the elements
 	 *             currently in the set
 	 * @throws NullPointerException
 	 *             if the specified element is null and this set does not permit
 	 *             null elements
 	 */
 	@Override
 	public E floor(E e) {
 		if (e == null) {
 			throw new NullPointerException("Parametr is null");
 		}
 
 		if (e.compareTo(first()) < 0 || e.compareTo(last()) > 0) {
 			return null;
 		}
 
 		Node current = minNode(root);
 		while (nextSetNode(current) != null && e.compareTo(current.value) > 0
 				&& e.compareTo(nextSetNode(current).value) >= 0) {
 			current = nextSetNode(current);
 		}
 
 		return current.value;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public SortedSet<E> headSet(E toElement) {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
 		return null;
 	}
 
 	/**
 	 * Returns the least element in this set strictly greater than the given
 	 * element, or {@code null} if there is no such element.
 	 * 
 	 * @param e
 	 *            the value to match
 	 * @return the least element greater than {@code e}, or {@code null} if
 	 *         there is no such element
 	 * @throws ClassCastException
 	 *             if the specified element cannot be compared with the elements
 	 *             currently in the set
 	 * @throws NullPointerException
 	 *             if the specified element is null and this set does not permit
 	 *             null elements
 	 */
 	@Override
 	public E higher(E e) {
 		if (e == null) {
 			throw new NullPointerException("Parametr is null");
 		}
 
 		if (e.compareTo(first()) < 0 || e.compareTo(last()) >= 0) {
 			return null;
 		}
 
 		Node current = minNode(root);
 		while (nextSetNode(current) != null && e.compareTo(current.value) > 0
 				&& e.compareTo(nextSetNode(current).value) >= 0) {
 			current = nextSetNode(current);
 		}
 
 		return nextSetNode(current).value;
 	}
 
 	/**
 	 * Find best place and insert node to the tree.
 	 * 
 	 * @param node
 	 *            to insert.
 	 * @param parent
 	 *            - point for bypass.
 	 * @return true if node inserted, and false if not.
 	 */
 	protected abstract boolean insertNode(Node node, Node parent);
 
 	/**
 	 * Returns an iterator over the elements contained in this collection.
 	 * 
 	 * @return an iterator over the elements contained in this collection
 	 */
 	@Override
 	public Iterator<E> iterator() {
 		Iterator<E> iterator = new Iterator<E>() {
 
 			/**
 			 * First point
 			 */
 			private Node current = minNode(root);
 
 			/**
 			 * Returns {@code true} if the iteration has more elements. (In
 			 * other words, returns {@code true} if {@link #next} would return
 			 * an element rather than throwing an exception.)
 			 * 
 			 * @return {@code true} if the iteration has more elements
 			 */
 			@Override
 			public boolean hasNext() {
 				return current != null;
 			}
 
 			/**
 			 * Returns the next element in the iteration.
 			 * 
 			 * @return the next element in the iteration
 			 * @throws NoSuchElementException
 			 *             if the iteration has no more elements
 			 */
 			@Override
 			public E next() {
 				if (current == null) {
 					throw new NoSuchElementException();
 				}
 				Node temp = current;
 				current = nextSetNode(current);
 				return temp.value;
 			}
 
 			/**
 			 * Not implemented!
 			 */
 			@Override
 			public void remove() {
 				// Do nothing!
 			}
 
 		};
 		return iterator;
 	}
 
 	/**
 	 * Returns the last (highest) element currently in this set.
 	 * 
 	 * @return the last (highest) element currently in this set
 	 * @throws NoSuchElementException
 	 *             if this set is empty
 	 */
 	@Override
 	public E last() {
 		if (root == null) {
 			throw new NoSuchElementException("Tree is empty");
 		}
 		return maxNode(root).value;
 	}
 
 	/**
 	 * Returns the greatest element in this set strictly less than the given
 	 * element, or {@code null} if there is no such element.
 	 * 
 	 * @param e
 	 *            the value to match
 	 * @return the greatest element less than {@code e}, or {@code null} if
 	 *         there is no such element
 	 * @throws ClassCastException
 	 *             if the specified element cannot be compared with the elements
 	 *             currently in the set
 	 * @throws NullPointerException
 	 *             if the specified element is null and this set does not permit
 	 *             null elements
 	 */
 	@Override
 	public E lower(E e) {
 		if (e == null) {
 			throw new NullPointerException("Parametr is null");
 		}
 
 		if (e.compareTo(first()) <= 0 || e.compareTo(last()) > 0) {
 			return null;
 		}
 
 		Node current = minNode(root);
 		while (nextSetNode(current) != null && e.compareTo(current.value) > 0
 				&& e.compareTo(nextSetNode(current).value) > 0) {
 			current = nextSetNode(current);
 		}
 
 		return current.value;
 	}
 
 	/**
 	 * Function finds node with maximum value in the subtree.
 	 * 
 	 * @param node
 	 *            which begins subtree.
 	 * @return node with maximum value.
 	 */
 	private Node maxNode(Node node) {
 		if (node.right == null) {
 			return node;
 		} else {
 			return maxNode(node.right);
 		}
 	}
 
 	/**
 	 * Function finds node with minimum value in the subtree.
 	 * 
 	 * @param node
 	 *            which begins subtree.
 	 * @return node with minimum value.
 	 */
 	private Node minNode(Node node) {
 		if (node.left == null) {
 			return node;
 		} else {
 			return minNode(node.left);
 		}
 	}
 
 	/**
 	 * Returns the next element in this collection
 	 * 
 	 * @param node
 	 *            element from which the following will be found.
 	 * @return the next element in this collection
 	 */
 	private Node nextSetNode(Node node) {
 		if (node.right != null) {
 			return minNode(node.right);
 		} else {
 			Node temp = node.parent;
 			while (temp != null && temp.right == node) {
 				node = temp;
 				temp = node.parent;
 			}
 			return temp;
 		}
 	}
 
 	/**
 	 * Returns the next element in this collection in order root - left - right.
 	 * 
 	 * @param node
 	 *            element from which the following will be found.
 	 * @return the next element in this collection in order root - left - right.
 	 */
 	private Node nextTreeNode(Node node) {
 		if (node.left != null) {
 			return node.left;
 		}
 
 		if (node.right != null) {
 			return node.right;
 		}
 
 		Node temp = node.parent;
		while (temp != null) {
			if (temp.left != null && temp.left == node && temp.right != null) {
				return temp.right;
			}

 			node = temp;
 			temp = node.parent;
 		}
 
		return temp;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public E pollFirst() {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public E pollLast() {
 		return null;
 	}
 
 	/**
 	 * Wrapper function for determine the number of nodes in the tree.
 	 * 
 	 * @return number of nodes in the tree.
 	 */
 	@Override
 	public int size() {
 		return getSize(root);
 	}
 
 	/**
 	 * Function of the recursive tree bypass and count of nodes.
 	 * 
 	 * @param node
 	 *            - start point.
 	 * @return - count of nodes.
 	 */
 	private int getSize(Node node) {
 		if (node == null) {
 			return 0;
 		} else {
 			return getSize(node.left) + getSize(node.right) + 1;
 		}
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
 			E toElement, boolean toInclusive) {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public SortedSet<E> subSet(E fromElement, E toElement) {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public SortedSet<E> tailSet(E fromElement) {
 		return null;
 	}
 
 	/**
 	 * Not implemented!
 	 * 
 	 * @return <tt>null</tt>
 	 */
 	@Override
 	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
 		return null;
 	}
 
 	/**
 	 * Returns an iterator over the elements contained in this collection in
 	 * order root - left - right.
 	 * 
 	 * @return an iterator over the elements contained in this collection in
 	 *         order root - left - right.
 	 */
 	public Iterator<E> treeIterator() {
 		Iterator<E> iterator = new Iterator<E>() {
 
 			/**
 			 * First point
 			 */
 			private Node current = root;
 
 			/**
 			 * Returns {@code true} if the iteration has more elements. (In
 			 * other words, returns {@code true} if {@link #next} would return
 			 * an element rather than throwing an exception.)
 			 * 
 			 * @return {@code true} if the iteration has more elements
 			 */
 			@Override
 			public boolean hasNext() {
 				return current != null;
 			}
 
 			/**
 			 * Returns the next element in the iteration.
 			 * 
 			 * @return the next element in the iteration
 			 * @throws NoSuchElementException
 			 *             if the iteration has no more elements
 			 */
 			@Override
 			public E next() {
 				if (current == null) {
 					throw new NoSuchElementException();
 				}
 				Node temp = current;
 				current = nextTreeNode(current);
 				return temp.value;
 			}
 
 			/**
 			 * Not implemented!
 			 */
 			@Override
 			public void remove() {
 				// Do nothing!
 			}
 
 		};
 		return iterator;
 	}
 
 	/**
 	 * Function of determination new root for a tree.
 	 * 
 	 * @param node
 	 *            new root for a tree.
 	 */
 	protected void setRoot(Node node) {
 		root = node;
 	}
 
 }
