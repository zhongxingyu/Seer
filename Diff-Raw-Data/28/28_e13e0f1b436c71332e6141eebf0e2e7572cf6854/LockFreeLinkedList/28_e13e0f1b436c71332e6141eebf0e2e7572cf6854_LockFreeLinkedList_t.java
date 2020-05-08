 package lobstre.chlist;
 
 import java.util.Comparator;
 import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
 
 import lobstre.chlist.LockFreeLinkedList.Node;
 import lobstre.chlist.util.Pair;
 
 public class LockFreeLinkedList {
 	private final Node head;
 	private final Comparator<Object> comparator;
 
 	public LockFreeLinkedList () {
 		this.comparator = new Comparator<Object> () {
 			@Override
 			public int compare (Object o1, Object o2) {
 				if (o1 == MINUS_INFINITE_KEY || o2 == PLUS_INFINITE_KEY) {
 					return -1;
 				}
 				if (o1 == PLUS_INFINITE_KEY || o2 == MINUS_INFINITE_KEY) {
 					return +1;
 				}
 				
 				final Integer i1 = (Integer) o1;
 				final Integer i2 = (Integer) o2;
 				return i1.compareTo (i2);
 			}
 		};
 		final Node tail = new Node (PLUS_INFINITE_KEY, PLUS_INFINITE_KEY, null);
 		this.head = new Node (MINUS_INFINITE_KEY, MINUS_INFINITE_KEY, tail);
 	}
 
 	/**
 	 * Searches for a node with the supplied key
 	 * 
 	 * @param k
 	 *            the key instance
 	 * @return a {@link Node} instance
 	 */
 	public Node search (final int k) {
 		final Pair<Node, Node> curAndNext =
 				searchCurrentAndNextFrom (Integer.valueOf (k), head);
 		final Node current = curAndNext.getFirst ();
 		if (0 == this.comparator.compare (current.key, k)) {
 			return current;
 		} else {
 			return null;
 		}
 	}
 
 	public Node delete (final int k) {
 		final Pair<Node, Node> search = searchPrevAndCurrentFrom (
 				Integer.valueOf (k),
 				this.head);
 		Node prevNode = search.getFirst ();
 		final Node delNode = search.getSecond ();
 	
 		if (0 != this.comparator.compare (delNode.key, k)) {
 			// k is not found in the list.
 			return null;
 		}
 	
 		final Pair<Node, Boolean> tryFlag = tryFlag (prevNode, delNode);
 		prevNode = tryFlag.getFirst ();
 		if (null != prevNode) {
 			helpFlagged (prevNode, delNode);
 		}
 		return tryFlag.getSecond ().booleanValue () ? delNode : null;
 	}
 
 	public Node insert (final int k, final Object v) {
 		Pair<Node, Node> search = 
 				searchCurrentAndNextFrom (Integer.valueOf (k), head);
 		Node prevNode = search.getFirst ();
 		Node nextNode = search.getSecond ();
 		
 		if (prevNode.key.equals (k)) {
 			return null;
 		}
 		
 		for (;;) {
 			final Node newNode = new Node (k, v, nextNode);
 			final NextLink prevNext = prevNode.next ();
 			if (prevNext.flag == true) {
 				// If predecessor is flagged : help deletion to complete
 				helpFlagged (prevNode, prevNext.node);
 			} else {
 				final NextLink failed = prevNode.compareAndSetNext (
 					nextNode, false, false, 
 					newNode, false, false);
 				if (failed == null) {
 					// Success
 					return newNode;
 				} else {
 					// Failure
 					if (failed.mark == false && failed.flag == true) {
 						// Failure due to flagging : 
 						// Help complete deletion
 						helpFlagged (prevNode, failed.node);
 					}
 					// Possibly a failure due to marking 
 					// Help complete deletion: traverse the backlink chain
 					while (prevNode.next.mark == true) {
 						prevNode = prevNode.backlink;
 					}
 				}
 			}
 			
 			// Search again from prevNode
 			search = searchCurrentAndNextFrom (k, prevNode);
 			prevNode = search.getFirst ();
 			nextNode = search.getSecond ();
 			if (prevNode.key.equals (k)) {
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Finds two consecutive nodes n1 and n2 such that n1.key <= k < n2.key.
 	 * 
 	 * @param k
 	 *            the key instance
 	 * @param currNode
 	 *            the current node to start searching for
 	 * @return a {@link Pair} of two {@link Node} instances
 	 */
 	private Pair<Node, Node> searchCurrentAndNextFrom (
 			final Object k,
 			final Node currNode) {
 		return searchFromInternal (k, currNode, LOWER_OR_EQUAL);
 	}
 
 	/**
 	 * Finds two consecutive nodes n1 and n2 such that n1.key < k <= n2.key.
 	 * 
 	 * @param k
 	 *            the key instance
 	 * @param currNode
 	 *            the previous node to start searching for
 	 * @return a {@link Pair} of two {@link Node} instances
 	 */
 	private Pair<Node, Node> searchPrevAndCurrentFrom (
 			final Object k,
 			final Node currNode) {
 		return searchFromInternal (k, currNode, STRICTLY_LOWER);
 	}
 
 	/**
 	 * Attempts to mark and physically delete node delNode, which is the
 	 * successor of the flagged node prevNode.
 	 * 
 	 * @param prevNode
 	 *            the previous node
 	 * @param delNode
 	 *            the candidate node for deletion
 	 */
 	private void helpFlagged (
 			final Node prevNode,
 			final Node delNode) {
 		delNode.backlink = prevNode;
 		if (!delNode.next ().mark) {
 			tryMark (delNode);
 		}
 		helpMarked (prevNode, delNode);
 	}
 
 	private void tryMark (final Node delNode) {
 		do {
 			final Node nextNode = delNode.next ().node;
 			final NextLink failedLink = delNode.compareAndSetNext (
 					nextNode, false, false,
 					nextNode, true, false);
			if (null != failedLink && 
					failedLink.mark == false && 
					failedLink.flag == true) {
 				helpFlagged (delNode, failedLink.node);
 			}
 		} while (delNode.next ().mark != true);
 	}
 
 	private Pair<Node, Boolean> tryFlag (Node prevNode,
 			final Node targetNode) {
 		for (;;) {
 			final NextLink next = prevNode.next ();
 			if (next.node == targetNode &&
 					!next.mark &&
 					next.flag) {
 				// Predecessor is already flagged
 				return new Pair<Node, Boolean> (
 						prevNode, Boolean.TRUE);
 			}
 			// Flagging attempt
 			final NextLink failed = prevNode.compareAndSetNext (
 					targetNode, false, false,
 					targetNode, false, true);
 			if (null == failed) {
 				// Successful flaging : report the success
 				return new Pair<Node, Boolean> (
 						prevNode, Boolean.TRUE);
 			}
 			if (failed.node == targetNode &&
 					failed.mark == false &&
 					failed.flag == true) {
 				// Failure due to a concurrent operation
 				// Report the failure, return a pointer to prev node.
 				return new Pair<Node, Boolean> (
 						prevNode, Boolean.FALSE);
 			}
 			while (prevNode.next ().mark) {
 				// Possibly a failure due to marking,
 				// Traverse a chain of backlinks to reach an unmarked node.
 				prevNode = prevNode.backlink;
 			}
 			// Search again
 			final Pair<Node, Node> search = searchPrevAndCurrentFrom (
 					targetNode.key,
 					prevNode);
 			prevNode = search.getFirst ();
 			final Node delNode = search.getSecond ();
 			if (delNode != targetNode) {
 				// targetNode got deleted
 				// report the failure, return no pointer.
 				return new Pair<Node, Boolean> (
 						null, Boolean.FALSE);
 			}
 		}
 	}
 
 	private Pair<Node, Node> searchFromInternal (
 			final Object k,
 			Node currNode,
 			final Comparison c) {
 		Node nextNode = currNode.next ().node;
 		while (c.apply (this.comparator.compare (nextNode.key, k), 0)) {
 			// Ensure that either nextNode is unmarked,
 			// or both currNode and nextNode are mark
 			// and currNode was mark earlier.
 			while (nextNode.next ().mark
 					&& (!currNode.next ().mark || currNode.next ().node != nextNode)) {
 				if (currNode == nextNode) {
 					helpMarked (currNode, nextNode);
 				}
 				nextNode = currNode.next ().node;
 			}
 			if (c.apply (this.comparator.compare (nextNode.key, k), 0)) {
 				currNode = nextNode;
 				nextNode = currNode.next ().node;
 			}
 		}
 		return new Pair<Node, Node> (currNode, nextNode);
 	}
 
 	private void helpMarked (final Node prevNode, final Node delNode) {
 		// Attempts to physically delete the marked
 		// node delNode and unflag prevNode.
 		final Node nextNode = delNode.next ().node;
 		prevNode.compareAndSetNext (
 				delNode, false, true,
 				nextNode, false, false);
 	}
 
 	static class Node {
 		public Node (final Object key, final Object value, Node nextNode) {
 			this.key = key;
 			this.value = value;
 			this.next = new NextLink (nextNode, false, false);
 			this.backlink = null;
 		}
 
 		public Object getValue () {
 			return value;
 		}
 
 		public NextLink next () {
 			return NEXT_UPDATER.get (this);
 		}
 
 		/**
 		 * CAS the next pointer
 		 *
 		 * @param expectedNode
 		 *            the expected node
 		 * @param expectedMark
 		 *            the expected mark
 		 * @param expectedFlag
 		 *            the expected flag
 		 * @param replacementNode
 		 *            the replacement node
 		 * @param replacementMark
 		 *            the replacement mark
 		 * @param replacementFlag
 		 *            the replacement flag
 		 * @return null if CAS worked, or the previous value if CAS failed.
 		 */
 		public NextLink compareAndSetNext (final Node expectedNode,
 				final boolean expectedMark, final boolean expectedFlag,
 				final Node replacementNode,
 				final boolean replacementMark, final boolean replacementFlag) {
 			final NextLink currentLink = next ();
 			if (currentLink.node == expectedNode
 					&& currentLink.mark == expectedMark
 					&& currentLink.flag == expectedFlag) {
 				final NextLink update = new NextLink (
 						replacementNode, replacementMark, replacementFlag);
 				final boolean set = NEXT_UPDATER.compareAndSet (
 						this, currentLink, update);
 				return set ? null : currentLink;
 			} else {
 				return currentLink;
 			}
 		}
 
 		final Object key;
 		final Object value;
 		volatile NextLink next;
 		volatile Node backlink;
 
 		static final AtomicReferenceFieldUpdater<Node, NextLink> NEXT_UPDATER =
 				AtomicReferenceFieldUpdater
 						.newUpdater (Node.class, NextLink.class, "next");
 	}
 
 	static class NextLink {
 		public NextLink (
 				final Node node,
 				final boolean mark,
 				final boolean flag) {
 			this.node = node;
 			this.mark = mark;
 			this.flag = flag;
 		}
 
 		final Node node;
 		final boolean mark;
 		final boolean flag;
 	}
 
 	public interface Comparison {
 		public boolean apply (int a, int b);
 	}
 
 	public static Comparison LOWER_OR_EQUAL = new Comparison () {
 		@Override
 		public boolean apply (final int a, final int b) {
 			return a <= b;
 		}
 	};
 	public static Comparison STRICTLY_LOWER = new Comparison () {
 		@Override
 		public boolean apply (final int a, final int b) {
 			return a < b;
 		}
 	};
 	private static final Object MINUS_INFINITE_KEY = new Object ();
 	private static final Object PLUS_INFINITE_KEY = new Object ();
 }
