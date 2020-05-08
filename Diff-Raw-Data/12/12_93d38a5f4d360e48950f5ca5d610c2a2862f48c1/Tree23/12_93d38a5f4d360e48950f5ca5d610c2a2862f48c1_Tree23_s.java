 package suite.immutable;
 
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.List;
 
 import suite.util.FunUtil;
 import suite.util.FunUtil.Source;
 import suite.util.Util;
 
 public class Tree23<T> implements Iterable<T>, ImmutableTree<T> {
 
 	private int maxSize = 4;
 	private int halfSize = maxSize / 2;
 
 	private Node root;
 	private Comparator<T> comparator;
 
 	private class Node {
 		private List<Slot> slots;
 
 		public Node(List<Slot> slots) {
 			this.slots = slots;
 		}
 	}
 
 	/**
 	 * Node would be null in leaves. Pivot stores the leaf value.
 	 * 
 	 * Pivot would be null at the maximum side of a tree. It represents the
 	 * guarding key.
 	 */
 	private class Slot {
 		private Node node;
 		private T pivot;
 
 		public Slot(Node node) {
 			this(node, getLastPivot(node.slots));
 		}
 
 		public Slot(Node node, T pivot) {
 			this.node = node;
 			this.pivot = pivot;
 		}
 	}
 
 	public Tree23(Comparator<T> comparator) {
 		this.root = new Node(Arrays.asList(new Slot(null, null)));
 		this.comparator = comparator;
 	}
 
 	private Tree23(Comparator<T> comparator, Node root) {
 		this.root = root;
 		this.comparator = comparator;
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return FunUtil.iterator(new Source<T>() {
 			private Deque<List<Slot>> stack = new ArrayDeque<>();
 			{
 				stack.push(root.slots);
 			}
 
 			public T source() {
 				List<Slot> slots = Collections.emptyList();

 				while (!stack.isEmpty() && (slots = stack.pop()).isEmpty())
 					;

 				return push(slots);
 			}
 
 			private T push(List<Slot> slots) {
 				T t;
 
 				if (!slots.isEmpty()) {
 					Slot slot0 = slots.get(0);
 					Node node = slot0.node;
 					stack.push(Util.sublist(slots, 1, 0));

					if (node != null)
						t = push(node.slots);
					else
						t = slot0.pivot;
 				} else
 					t = null;
 
 				return t;
 			}
 		});
 	}
 
 	public T find(T t) {
 		Node node = root;
 		Slot slot = null;
 		int c = 1;
 
 		while (node != null) {
 			List<Slot> slots = node.slots;
 			int size = slots.size();
 			int i = 0;
 
 			while (i < size && (c = compare((slot = slots.get(i)).pivot, t)) < 0)
 				i++;
 
 			node = slot.node;
 		}
 
 		return c == 0 ? slot.pivot : null;
 	}
 
 	public Tree23<T> add(T t) {
 		return add(t, false);
 	}
 
 	/**
 	 * Replaces a value with another. Mainly for dictionary cases to replace
 	 * stored value for the same key.
 	 * 
 	 * Asserts comparator.compare(<original-value>, t) == 0.
 	 */
 	public Tree23<T> replace(T t) {
 		return add(t, true);
 	}
 
 	public Tree23<T> remove(T t) {
 		return new Tree23<T>(comparator, createRootNode(remove(root.slots, t)));
 	}
 
 	private Tree23<T> add(T t, boolean isReplace) {
 		return new Tree23<T>(comparator, createRootNode(add(root.slots, t, isReplace)));
 	}
 
 	private List<Slot> add(List<Slot> slots0, T t, boolean isReplace) {
 		Slot slot = null;
 		int i = 0, c = 1;
 
 		// Finds appropriate slot
 		while ((c = compare((slot = slots0.get(i)).pivot, t)) < 0)
 			i++;
 
 		// Adds the node into it
 		List<Slot> replaceSlots;
 
 		if (slot.node != null)
 			replaceSlots = add(slot.node.slots, t, isReplace);
 		else if (c != 0)
 			replaceSlots = Arrays.asList(new Slot(null, t), slot);
 		else if (isReplace)
 			replaceSlots = Arrays.asList(new Slot(null, t));
 		else
 			throw new RuntimeException("Duplicate node " + t);
 
 		List<Slot> slots1 = Util.add(Util.sublist(slots0, 0, i), replaceSlots, Util.sublist(slots0, i + 1, 0));
 		List<Slot> slots2;
 
 		// Checks if need to split
 		if (slots1.size() < maxSize)
 			slots2 = Arrays.asList(slot(slots1));
 		else { // Splits into two if reached maximum number of nodes
 			List<Slot> leftSlots = Util.sublist(slots1, 0, halfSize);
 			List<Slot> rightSlots = Util.sublist(slots1, halfSize, 0);
 			slots2 = Arrays.asList(slot(leftSlots), slot(rightSlots));
 		}
 
 		return slots2;
 	}
 
 	private List<Slot> remove(List<Slot> slots0, T t) {
 		int size = slots0.size();
 		Slot slot = null;
 		int i = 0, c = 1;
 
 		// Finds appropriate slot
 		while (i < size && (c = compare((slot = slots0.get(i)).pivot, t)) < 0)
 			i++;
 
 		// Removes the node from it
 		int s0 = i, s1 = i + 1;
 		List<Slot> replaceSlots;
 
 		if (c >= 0)
 			if (slot.node != null) {
 				List<Slot> slot1 = remove(slot.node.slots, t);
 
 				// Merges with a neighbor if reached minimum number of nodes
 				if (slot1.size() < halfSize)
 					if (s0 > 0)
 						replaceSlots = merge(slots0.get(--s0).node.slots, slot1);
 					else if (s1 < size)
 						replaceSlots = merge(slot1, slots0.get(s1++).node.slots);
 					else
 						replaceSlots = Arrays.asList(slot(slot1));
 				else
 					replaceSlots = Arrays.asList(slot(slot1));
 			} else
 				replaceSlots = Collections.emptyList();
 		else
 			throw new RuntimeException("Node not found " + t);
 
 		return Util.add(Util.sublist(slots0, 0, s0), replaceSlots, Util.sublist(slots0, s1, 0));
 	}
 
 	private List<Slot> merge(List<Slot> slots0, List<Slot> slots1) {
 		List<Slot> merged;
 
 		if (slots0.size() + slots1.size() >= maxSize) {
 			List<Slot> leftSlots, rightSlots;
 
 			if (slots0.size() > halfSize) {
 				leftSlots = Util.sublist(slots0, 0, -1);
 				rightSlots = Util.add(Arrays.asList(Util.at(slots0, -1)), slots1);
 			} else {
 				leftSlots = Util.add(slots0, Arrays.asList(Util.at(slots1, 0)));
 				rightSlots = Util.sublist(slots1, 1, 0);
 			}
 
 			merged = Arrays.asList(slot(leftSlots), slot(rightSlots));
 		} else
 			merged = Arrays.asList(slot(Util.add(slots0, slots1)));
 
 		return merged;
 	}
 
 	private Node createRootNode(List<Slot> slots) {
 		Node node, node1;
 
 		if (slots.size() == 1 && (node1 = slots.get(0).node) != null)
 			node = node1;
 		else
 			node = new Node(slots);
 
 		return node;
 	}
 
 	private T getLastPivot(List<Slot> slots) {
 		return slots.get(slots.size() - 1).pivot;
 	}
 
 	private Slot slot(List<Slot> slots) {
 		return new Slot(new Node(slots));
 	}
 
 	private int compare(T t0, T t1) {
 		boolean b0 = t0 != null;
 		boolean b1 = t1 != null;
 
 		if (b0 && b1)
 			return comparator.compare(t0, t1);
 		else
 			return b0 ? -1 : b1 ? 1 : 0;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		dump(sb, root, "");
 		return sb.toString();
 	}
 
 	private void dump(StringBuilder sb, Node node, String indent) {
 		if (node != null)
 			for (Slot slot : node.slots) {
 				dump(sb, slot.node, indent + "  ");
 				sb.append(indent + slot.pivot + "\n");
 			}
 	}
 
 }
