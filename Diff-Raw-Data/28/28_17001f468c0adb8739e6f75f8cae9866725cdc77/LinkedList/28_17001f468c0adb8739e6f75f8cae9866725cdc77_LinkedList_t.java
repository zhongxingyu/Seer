 package util;
 
 import java.util.ListIterator;
 import java.util.Iterator;
 import java.util.AbstractSequentialList;
 import java.util.NoSuchElementException;
 
 
 /**
  * A list.
  * @see AbstractSequentialList
  */
public class LinkedList<T> extends AbstractSequentialList<T> {
 
 	/**
 	 * Head node of the list.
 	 */
 	private Node head = null;
 
 	/**
 	 * The size of the list
 	 */
 	private int size = 0;
 
 	/**
 	 * Default constructor - initialize an empty list.
 	 */
 	public LinkedList() {
 		
 	}
 	
 	/**
 	 * Returns the size of the list.
 	 * @override
 	 * @return the size of the list
 	 */
 	public int size() {
 		return size;
 	}
 
 
 	/**
 	 * One node of the linked list.  Little more than a struct.
 	 * mini/private/inner class with only private members.
 	 */
 	private class Node {
 		private Node next = null;
 
 		private T item = null;
 
 		private Node(T item) {
 			this.item = item;
 		}
 	}
 
 	/**
 	 * Returns a generic iterator over the list, starting at 
 	 * the first element
 	 *
 	 * @return an iterator...
 	 */
 	public Iterator<T> iterator() {
 		return listIterator();
 	}
 
 
 	/**
 	 * Seek to start and return a list iterator.
 	 * @see ListIterator
 	 * @override
 	 *
 	 * @param start the index of the element to be returned by the first call to next()
 	 *
 	 * @return an Iterator over the list.
 	 */
 	public ListIterator<T> listIterator(int start) throws IndexOutOfBoundsException {  
 		
 		if ( start < 0 || start > size )
 			throw new IndexOutOfBoundsException("Invalid start index: " +start);
 
 		//the  list iterator expects current 
 		//to be null when starting at the beginning
 		Node cur = null;
 
 		//seek to the requested index...
 		for (int i = 0; i < start; ++i) {
 			//the list is assumed to be in a sane state at this point, 
 			//so there shouldnt' be any null pointers or anything
 		
 			cur = (cur == null) ? head : cur.next;
 		}
 
 		//start - 1 because next() should 
 		//return the element with index start.
 		return listIterator(start - 1, cur);
 	}
 
 	/**
 	 * This method actually creates the list iterator. The entire body is an anonymous class
 	 * 
 	 * Per AbstractSequentialList, this is the functional part of the entire list.
 	 */
 	private ListIterator<T> listIterator(final int startIdx, final Node startNode) { return new ListIterator<T>() {
 	
 		private Node current = startNode;
 		private int idx = startIdx;
 	
 		/**
 		 * Adds an item to the list after the current item.
 		 * @override
 		 */
 		public void add (T item) {
 
 			//PREVIOUS
 			//CURRENT
 			//NEW NODE
 			//NEXT
 
 			Node node = new Node(item);
 
 			//if at the beginning of the list...
 			if (current == null) {
 				node.next = head;
 				head = node;
 
 			} else {
 				node.next = current.next;
 				current.next = node;
 			}
 
 			++idx;
 			current = node;
 			++size;
 
 		}
 
 		/**
 		 * Returns the index of the element that will be returned by next(), or
 		 * size if thereis no next element.
 		 *
 		 * @return index of next element
 		 */
 		public int nextIndex() {
 			//idx will never advance past size-1, so returns size
 			//when at the end.
 			return idx + 1;
 		}
 
 		/**
 		 * Determines if there is an element before the ... current one.
 		 */
 		public boolean hasPrevious() {
 			return idx > 0;
 		}
 
 		/**
 		 * Returns the "current" item (the one previously returned by next())
 		 * and moves the interal pointer (current) back
 		 */
 		public T previous() throws NoSuchElementException {
 
 			if (!hasPrevious()) 
 				throw new NoSuchElementException("NO previous element. Try using hasPrevious()...");
 
 			idx--;
 
 			Node tmp = current;
 
 			current = head;
 
 			//find the node BEFORE tmp
 			while (current.next != tmp && current.next != null) 
 				current = current.next;
 
 			//should NEVER happen (I think? Maybe after a remove(). Should check on that.)
 			if ( current == null || current.next != tmp )
 				throw new NoSuchElementException("The current item isn't in the list. Something has gone terribly wrong?");
 
 			return tmp.item;
 		}
 
 		/**
 		 * Returns the index of the element that would be returned by previous
 		 * or -1 if there is no previous...
 		 */
 		public int previousIndex() {
 			//in theory, idx CAN become -1
 			//(-1 is the default state, so calling previous enough times should return it to -1)
 			return (idx > 0) ? idx - 1 : -1;
 		}
 
 
 		/**
 		 * Return the next item, or throw nosuchelementexception
 		 */
 		public T next() throws NoSuchElementException {
 
 			//this is the first call to next.
 			//(current will never get set to null while iterating)
 			if ( current == null ) {
 
 				if (head == null) 
 					throw new NoSuchElementException("No Items in the list...");
 
 				current = head;
 
 				idx++;
 
 				return head.item;
 	
 			//if there is a next item, throw it
 			} else if (current.next != null) {
 
 				T item = current.next.item;
 
 				current = current.next;
 
 				idx++;
 			
 				return item;
 
 			//there was no call to hasNext()
 			//or the output of hasNext() was ignored.
 			} else {
 				throw new NoSuchElementException("At end of list.");
 			}
 		}
 
 		/**
 		 * Returns true if iteration has more elements.
 		 *
 		 * @return true if next() will return a valid item
 		 */
 		public boolean hasNext() {
 
 			if ( current != null) {
 				return !(current.next == null);
 
 			} else {
 				return head != null;
 			}
 		}
 
 		/**
 		 * Removes the last element returned by next() from the collection.
 		 * Throws an exception if the list is empty or next() has not been called.
 		 */
 		public void remove() throws IllegalStateException {
 
 			//ensure that next() has been called.
 			if (current == null || head == null)
 				throw new IllegalStateException("No item selected.");
 
 			//handle head specially because if head is removed
 			//we need to set a new head. (a headless list is bad)
 			if (current == head) {
 				head = current.next;
 				--size;
 				--idx;
 				current = null;
 				return;
 			}
 
 			//item before the one being removed
 			Node index = head;
 
 			while (index.next != current && index.next != null)
 				index = index.next;
 
 			//This probably shouldn't ever happen
 			//if current isn't in the collection and also not null
 			//then next() has a BIG problem.
 			if ( index == null )
 				throw new IllegalStateException("Element is not in the collection." );
 
 
 			//remove the item in the linked list by skipping over it.
 			index.next = current.next;
 
 			//this isn't really necessary, but it's more clear
 			current = index;
 
 			--size;
 			--idx;
 		}
 
 		/** 
 		 * @TODO This conforms to ListIterator
 		 */
 		public void set(T item) throws ClassCastException, IllegalStateException  {
 	
 			if ( current == null )
 				throw new IllegalStateException("Cannot call set() before next()");
 
 			current.item = item;
 		}
 
 	};} //end Iterator
 }
