 /**
  * Copyright (C) 2013 Sebastian Kürten.
  */
 package de.topobyte.adt.avltree;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.SortedSet;
 
 public class Tree<T extends Comparable<T>> implements Set<T>, SortedSet<T>,
 		List<T>
 {
 
 	private Node<T> root = null;
 
 	/*
 	 * Public API
 	 */
 
 	/**
 	 * Insert the specified element into the tree.
 	 */
 	public boolean insertElement(T e)
 	{
 		Node<T> n = insert(root, e);
 		if (n == null) {
 			return false;
 		}
 		root = n;
 		return true;
 	}
 
 	/**
 	 * 
 	 * Remove the specified element from the tree if present.
 	 * 
 	 * @return <tt>true</tt> if the element was found and removed.
 	 */
 	public boolean removeElement(T e)
 	{
 		TreePath<T> n = findNodePath(e);
 		if (n == null) {
 			return false;
 		}
 		remove(n);
 		return true;
 	}
 
 	/**
 	 * Removes all elements from the tree.
 	 */
 	@Override
 	public void clear()
 	{
 		root = null;
 	}
 
 	/**
 	 * Returns <tt>true</tt> if this tree contains no elements.
 	 * 
 	 * @return <tt>true</tt> if this tree contains no elements.
 	 */
 	@Override
 	public boolean isEmpty()
 	{
 		return root == null;
 	}
 
 	/**
 	 * Get the height of the tree.
 	 * 
 	 * @return the height of the tree.
 	 */
 	public int getHeight()
 	{
 		return height(root);
 	}
 
 	/**
 	 * Get the number of elements stored in this tree.
 	 * 
 	 * @return the number of elements.
 	 */
 	@Override
 	public int size()
 	{
 		if (root == null) {
 			return 0;
 		}
 		return root.getSize();
 	}
 
 	/**
 	 * Test whether this tree stores the specified element.
 	 * 
 	 * @return <tt>true</tt> if the specified element is already present in the
 	 *         tree.
 	 */
 	public boolean contains(T e)
 	{
 		return getElement(findNode(e)) != null;
 	}
 
 	/**
 	 * Get the element at position <tt>index</tt>
 	 */
 	public T getElement(int index)
 	{
 		return getElement(findIndex(index));
 	}
 
 	/**
 	 * Find the smallest element stored in this tree.
 	 * 
 	 * @return the smallest element in this tree or null if this tree does not
 	 *         contain any elements.
 	 */
 	public T findMin()
 	{
 		return getElement(findMin(root));
 	}
 
 	/**
 	 * Find the biggest element stored in this tree.
 	 * 
 	 * @return the biggest element in this tree or null if this tree does not
 	 *         contain any elements.
 	 */
 	public T findMax()
 	{
 		return getElement(findMax(root));
 	}
 
 	/**
 	 * Get all elements stored in this tree as a list.
 	 * 
 	 * @return a list containing all elements stored in this tree.
 	 */
 	public List<T> elementsAsList()
 	{
 		List<T> list = new ArrayList<T>();
 		if (root != null) {
 			collectElements(list, root);
 		}
 		return list;
 	}
 
 	/*
 	 * Internal methods
 	 */
 
 	/**
 	 * Insert the specified element into the tree <tt>n</tt>.
 	 * 
 	 * @return the new root of the subtree or null if the element was not
 	 *         inserted because it has already been present.
 	 */
 	private Node<T> insert(Node<T> n, T e)
 	{
 		if (n == null) {
 			// Simplest case (empty subtree): just add as root
 			n = new Node<T>(e, null, null);
 		} else if (e.compareTo(n.getElement()) < 0) {
 			// The element is smaller than n, recurse into left subtree
 			Node<T> ins = insert(n.getLeft(), e);
 			if (ins == null) {
 				return null;
 			} else {
 				n.setLeft(ins);
 				n = ensureBalanced(n);
 			}
 		} else if (e.compareTo(n.getElement()) > 0) {
 			// The element is bigger than n, recurse into right subtree
 			Node<T> ins = insert(n.getRight(), e);
 			if (ins == null) {
 				return null;
 			} else {
 				n.setRight(ins);
 				n = ensureBalanced(n);
 			}
 		} else {
 			// the element is already present, ignore duplicates
 			return null;
 		}
 		// update height and size information
 		n.setHeight(max(height(n.getLeft()), height(n.getRight())) + 1);
 		n.calculateSize();
 		return n;
 	}
 
 	/**
 	 * Remove the element stored at the specified path.
 	 */
 	private void remove(TreePath<T> path)
 	{
 		// The variable path will point to the node to remove
 
 		// The target node is a leaf or has only one child. It can be
 		// removed by removal or replacing it with its child.
 		if (path.getTarget().getNode().getSize() > 2) {
 			// The target node has two children. We replace it by its
 			// predecessor and remove the predecessor's former node instead.
 
 			// Remember where path pointed
 			TreePathNode<T> target = path.getTarget();
 
 			// Find the path to the predecessor.
 			findPredecessor(path);
 
 			// Get the predecessor node
 			Node<T> pred = path.getTarget().getNode();
 
 			// Move predecessor value into the removed node
 			target.getNode().setElement(pred.getElement());
 		}
 		// Otherwise path already points to the node
 
 		// The node pointed to with path has either one or no subtree at all
 		Node<T> sub = path.getTarget().getNode().getLeft();
 		if (sub == null) {
 			sub = path.getTarget().getNode().getRight();
 		}
 
 		if (sub == null) {
 			// If it does not have a subtree, just delete the reference to the
 			// node
 			if (path.getTarget().getParent() == null) {
 				// If this was the root node, clear the tree
 				root = null;
 			} else {
 				// Otherwise just update the parent node's pointer
 				TreePathNode<T> rem = path.getTarget();
 				if (rem.getDirection() == Direction.Left) {
 					rem.getParent().setLeft(null);
 				} else if (rem.getDirection() == Direction.Right) {
 					rem.getParent().setRight(null);
 				}
 			}
 		} else {
 			// If it does have a subtree
 			if (path.getTarget().getParent() == null) {
 				// If this was the root node, set new root
 				root = sub;
 			} else {
 				// Replace pointer from parent node
 				if (path.getTarget().getDirection() == Direction.Left) {
 					path.getTarget().getParent().setLeft(sub);
 				} else if (path.getTarget().getDirection() == Direction.Right) {
 					path.getTarget().getParent().setRight(sub);
 				}
 			}
 		}
 
 		// Traverse tree path and update height and size information
 		for (int i = path.getLength() - 2; i >= 0; i--) {
 			TreePathNode<T> nr = path.get(i);
 			Node<T> n = nr.getNode();
 
 			n.setHeight(max(height(n.getLeft()), height(n.getRight())) + 1);
 			n.calculateSize();
 		}
 
 		// Traverse tree path and balance tree if necessary
 		// TODO: use better stop criterion
 		for (int i = path.getLength() - 2; i >= 0; i--) {
 			TreePathNode<T> nr = path.get(i);
 			Node<T> n = nr.getNode();
 
 			if (i == 0) {
 				root = ensureBalanced(n);
 				ensureSizeAndHeight(root);
 			} else {
 				TreePathNode<T> parent = path.get(i - 1);
 				if (nr.getDirection() == Direction.Left) {
 					Node<T> b = ensureBalanced(n);
 					ensureSizeAndHeight(b);
 					parent.getNode().setLeft(b);
 				} else if (nr.getDirection() == Direction.Right) {
 					Node<T> b = ensureBalanced(n);
 					ensureSizeAndHeight(b);
 					parent.getNode().setRight(b);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Methods for finding nodes or paths
 	 */
 
 	/**
 	 * Find the node with the smallest element within the subtree <tt>n</tt>
 	 */
 	private Node<T> findMin(Node<T> n)
 	{
 		if (n == null) {
 			return n;
 		}
 
 		while (n.getLeft() != null) {
 			n = n.getLeft();
 		}
 		return n;
 	}
 
 	/**
 	 * Find the node with the biggest element within the subtree <tt>n</tt>
 	 */
 	private Node<T> findMax(Node<T> n)
 	{
 		if (n == null) {
 			return n;
 		}
 
 		while (n.getRight() != null) {
 			n = n.getRight();
 		}
 		return n;
 	}
 
 	/**
 	 * Find the node that stores the specified element or null if that element
 	 * is not currently stored in the tree.
 	 */
 	private Node<T> findNode(T e)
 	{
 		Node<T> n = root;
 		while (n != null) {
 			if (e.compareTo(n.getElement()) < 0) {
 				n = n.getLeft();
 			} else if (e.compareTo(n.getElement()) > 0) {
 				n = n.getRight();
 			} else {
 				return n;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Find the path to the node that stores the specified element or null if
 	 * that element is not currently stored in the tree.
 	 */
 	private TreePath<T> findNodePath(T e)
 	{
 		TreePath<T> path = new TreePath<T>();
 
 		Node<T> parent = null;
 		Direction dir = null;
 		Node<T> n = root;
 
 		while (n != null) {
 			path.add(parent, dir, n);
 			if (e.compareTo(n.getElement()) < 0) {
 				parent = n;
 				dir = Direction.Left;
 				n = n.getLeft();
 			} else if (e.compareTo(n.getElement()) > 0) {
 				parent = n;
 				dir = Direction.Right;
 				n = n.getRight();
 			} else {
 				return path;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Find the index'th element within the ordered sequence of elements.
 	 */
 	private Node<T> findIndex(int index)
 	{
 		if (root == null) {
 			return null;
 		}
 		return findIndex(root, index);
 	}
 
 	/**
 	 * Find the index'th element within the ordered sequence of elements
 	 * represented by this subtree.
 	 */
 	private Node<T> findIndex(Node<T> n, int index)
 	{
 		int size = n.getSize();
 		if (index >= size) {
 			return null;
 		}
 		Node<T> left = n.getLeft();
 		Node<T> right = n.getRight();
 		if (left == null) {
 			if (index == 0) {
 				return n;
 			} else {
 				return findIndex(right, index - 1);
 			}
 		} else {
 			if (index < left.getSize()) {
 				return findIndex(left, index);
 			} else if (index == left.getSize()) {
 				return n;
 			} else {
 				return findIndex(right, index - left.getSize() - 1);
 			}
 		}
 	}
 
 	/**
 	 * Find the index'th element within the ordered sequence of elements
 	 * represented by this subtree.
 	 */
 	private TreePath<T> findIndexPath(int index)
 	{
 		int size = size();
 		if (index >= size) {
 			return null;
 		}
 
 		TreePath<T> path = new TreePath<T>();
 
 		Node<T> p = null;
 		Node<T> n = root;
 		Direction d = null;
 		int idx = index;
 
 		while (true) {
 			path.add(p, d, n);
 			Node<T> left = n.getLeft();
 			Node<T> right = n.getRight();
 			if (left == null) {
 				if (idx == 0) {
 					break;
 				} else {
 					idx = idx - 1;
 					p = n;
 					n = right;
 					d = Direction.Right;
 				}
 			} else {
 				if (idx < left.getSize()) {
 					p = n;
 					n = left;
 					d = Direction.Left;
 				} else if (idx == left.getSize()) {
 					break;
 				} else {
 					idx = idx - left.getSize() - 1;
 					p = n;
 					n = right;
 					d = Direction.Right;
 				}
 			}
 		}
 
 		return path;
 	}
 
 	/**
 	 * Find the path to the predecessor of the node specified by <tt>path</tt>.
 	 * This method actually modifies the supplied path.
 	 */
 	TreePath<T> findPredecessor(TreePath<T> path)
 	{
 		if (path.getTarget().getNode().getLeft() == null) {
 			// The node does not have descendants, thus walk upwards to the next
 			// smaller element
 			while (path.getTarget().getDirection() == Direction.Left) {
 				path.removeLastNode();
 			}
 			path.removeLastNode();
 			return path;
 		} else {
 			// The node has descendants, thus walk downwards to the next smaller
 			// element. The path goes one node to the left...
 			Node<T> left = path.getTarget().getNode().getLeft();
 			path.add(path.getTarget().getNode(), Direction.Left, left);
 
 			// ...and then always to the right as long as possible
 			Node<T> i = left;
 			while (i.getRight() != null) {
 				path.add(i, Direction.Right, i.getRight());
 				i = i.getRight();
 			}
 			return path;
 		}
 	}
 
 	/**
 	 * Find the path to the successor of the node specified by <tt>path</tt>.
 	 * This method actually modifies the supplied path.
 	 */
 	TreePath<T> findSuccessor(TreePath<T> path)
 	{
 		if (path.getTarget().getNode().getRight() == null) {
 			// The node does not have descendants, thus walk upwards to the next
 			// bigger element
 			while (path.getTarget().getDirection() == Direction.Right) {
 				path.removeLastNode();
 			}
 			path.removeLastNode();
 			return path;
 		} else {
 			// The node has descendants, thus walk downwards to the next smaller
 			// element. The path goes one node to the right...
 			Node<T> right = path.getTarget().getNode().getRight();
 			path.add(path.getTarget().getNode(), Direction.Right, right);
 
 			// ...and then always to the left as long as possible
 			Node<T> i = right;
 			while (i.getLeft() != null) {
 				path.add(i, Direction.Left, i.getLeft());
 				i = i.getLeft();
 			}
 			return path;
 		}
 	}
 
 	/**
 	 * Find the path to the node with the smallest element.
 	 */
 	TreePath<T> findMinPath()
 	{
 		TreePath<T> path = new TreePath<T>();
 
 		Node<T> parent = null;
 		Direction dir = null;
 		Node<T> n = root;
 
 		while (n != null) {
 			path.add(parent, dir, n);
 			parent = n;
 			dir = Direction.Left;
 			n = n.getLeft();
 		}
 
 		return path;
 	}
 
 	/*
 	 * Balancing
 	 */
 
 	/**
 	 * Perform balancing via rotation for <tt>n</tt> if necessary and return the
 	 * new root node of the subtree.
 	 */
 	private Node<T> ensureBalanced(Node<T> n)
 	{
 		int balance = height(n.getLeft()) - height(n.getRight());
 		if (balance == 2) {
 			Node<T> l = n.getLeft();
 			if (height(l.getRight()) <= height(l.getLeft())) {
 				n = rotateRight(n);
 			} else {
 				n = rotateLeftRight(n);
 			}
 		} else if (balance == -2) {
 			Node<T> r = n.getRight();
 			if (height(r.getLeft()) <= height(r.getRight())) {
 				n = rotateLeft(n);
 			} else {
 				n = rotateRightLeft(n);
 			}
 		}
 		return n;
 	}
 
 	private void ensureSizeAndHeight(Node<T> n)
 	{
 		n.setHeight(max(height(n.getLeft()), height(n.getRight())) + 1);
 		n.calculateSize();
 	}
 
 	/*
 	 * Rotation methods
 	 */
 
 	/**
 	 * Single left rotation (with right child)
 	 * 
 	 * @return new root
 	 */
 	private Node<T> rotateLeft(Node<T> n)
 	{
 		Node<T> r = n.getRight();
 		n.setRight(r.getLeft());
 		r.setLeft(n);
 		n.setHeight(max(height(n.getLeft()), height(n.getRight())) + 1);
 		r.setHeight(max(height(r.getRight()), n.getHeight()) + 1);
 		n.calculateSize();
 		r.calculateSize();
 		return r;
 	}
 
 	/**
 	 * Single right rotation (with left child)
 	 * 
 	 * @return new root
 	 */
 	private Node<T> rotateRight(Node<T> n)
 	{
 		Node<T> l = n.getLeft();
 		n.setLeft(l.getRight());
 		l.setRight(n);
 		n.setHeight(max(height(n.getLeft()), height(n.getRight())) + 1);
 		l.setHeight(max(height(l.getLeft()), n.getHeight()) + 1);
 		n.calculateSize();
 		l.calculateSize();
 		return l;
 	}
 
 	/**
 	 * Double rotation: left then right.
 	 * 
 	 * @return new root
 	 */
 	private Node<T> rotateLeftRight(Node<T> n)
 	{
 		n.setLeft(rotateLeft(n.getLeft()));
 		return rotateRight(n);
 	}
 
 	/**
 	 * Double rotation: right then left.
 	 * 
 	 * @return new root
 	 */
 	private Node<T> rotateRightLeft(Node<T> n)
 	{
 		n.setRight(rotateRight(n.getRight()));
 		return rotateLeft(n);
 	}
 
 	/*
 	 * Misc methods
 	 */
 
 	private int height(Node<T> n)
 	{
 		return n == null ? 0 : n.getHeight();
 	}
 
 	private T getElement(Node<T> n)
 	{
 		return n == null ? null : n.getElement();
 	}
 
 	private void collectElements(List<T> list, Node<T> n)
 	{
 		if (n.getLeft() != null) {
 			collectElements(list, n.getLeft());
 		}
 		list.add(getElement(n));
 		if (n.getRight() != null) {
 			collectElements(list, n.getRight());
 		}
 	}
 
 	/*
 	 * Helper methods
 	 */
 
 	private int max(int a, int b)
 	{
 		return a > b ? a : b;
 	}
 
 	/*
 	 * temporary debug stuff
 	 */
 
 	/**
 	 * Create a printable representation of the tree.
 	 */
 	public String toFoldedString()
 	{
 		StringBuffer strb = new StringBuffer();
 		if (root != null) {
 			toFoldedString(strb, root);
 		}
 		return strb.toString();
 	}
 
 	private void toFoldedString(StringBuffer strb, Node<T> n)
 	{
 		strb.append(n.getElement());
 		if (n.getLeft() != null || n.getRight() != null) {
 			strb.append("[");
 			if (n.getLeft() != null) {
 				toFoldedString(strb, n.getLeft());
 			}
 			strb.append("]");
 			strb.append("[");
 			if (n.getRight() != null) {
 				toFoldedString(strb, n.getRight());
 			}
 			strb.append("]");
 		}
 	}
 
 	/**
 	 * Check whether the tree is balanced.
 	 */
	public boolean checkBalanced()
 	{
 		if (root == null) {
 			return true;
 		}
 		return checkBalanced(root);
 	}
 
 	private boolean checkBalanced(Node<T> n)
 	{
 		int b = height(n.getLeft()) - height(n.getRight());
 		if (b < -1 || b > 1) {
 			return false;
 		}
 		if (n.getLeft() != null) {
 			if (!checkBalanced(n.getLeft())) {
 				return false;
 			}
 		}
 		if (n.getRight() != null) {
 			if (!checkBalanced(n.getRight())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/*
 	 * Set implementation
 	 */
 
 	@Override
 	public boolean add(T element)
 	{
 		return insertElement(element);
 	}
 
 	public boolean addAll(Collection<? extends T> collection)
 	{
 		boolean modified = false;
 		Iterator<? extends T> it = collection.iterator();
 		while (it.hasNext()) {
 			if (add(it.next())) {
 				modified = true;
 			}
 		}
 		return modified;
 	}
 
 	@Override
 	public boolean contains(Object object)
 	{
 		return contains((T) object);
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> collection)
 	{
 		Iterator<?> it = collection.iterator();
 		while (it.hasNext()) {
 			if (!contains(it.next())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public Iterator<T> iterator()
 	{
 		return new TreeIterator<T>(this);
 	}
 
 	@Override
 	public boolean remove(Object object)
 	{
 		return remove((T) object);
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> collection)
 	{
 		boolean modified = false;
 		Iterator<?> it = collection.iterator();
 		while (it.hasNext()) {
 			if (remove(it.next())) {
 				modified = true;
 			}
 		}
 		return modified;
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> collection)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Object[] toArray()
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public <S> S[] toArray(S[] a)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	/*
 	 * SortedSet implementation
 	 */
 
 	@Override
 	public Comparator<? super T> comparator()
 	{
 		return null;
 	}
 
 	@Override
 	public T first()
 	{
 		T min = findMin();
 		if (min == null) {
 			throw new NoSuchElementException();
 		}
 		return min;
 	}
 
 	@Override
 	public T last()
 	{
 		T max = findMax();
 		if (max == null) {
 			throw new NoSuchElementException();
 		}
 		return max;
 	}
 
 	@Override
 	public SortedSet<T> headSet(T toElement)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public SortedSet<T> tailSet(T fromElement)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public SortedSet<T> subSet(T fromElement, T toElement)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	/*
 	 * List implementation
 	 */
 
 	@Override
 	public void add(int index, T element)
 	{
 		// this method is in contradiction with the automatic sorting of the
 		// tree
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean addAll(int index, Collection<? extends T> c)
 	{
 		// this method is in contradiction with the automatic sorting of the
 		// tree
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public T set(int index, T element)
 	{
 		// this method is in contradiction with the automatic sorting of the
 		// tree
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public T get(int index)
 	{
 		return getElement(index);
 	}
 
 	@Override
 	public T remove(int index)
 	{
 		TreePath<T> n = findIndexPath(index);
 		if (n == null) {
 			throw new IndexOutOfBoundsException();
 		}
 		T element = getElement(n.getTarget().getNode());
 		remove(n);
 		return element;
 	}
 
 	@Override
 	public int indexOf(Object o)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int lastIndexOf(Object o)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ListIterator<T> listIterator()
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ListIterator<T> listIterator(int index)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public List<T> subList(int fromIndex, int toIndex)
 	{
 		// TODO: implement this
 		throw new UnsupportedOperationException();
 	}
 
 }
