 package utils;
 
 import path.Path;
 
 /**
  * Implementation of LinkedList
  * 
  * @author russ
  * 
  */
 public class LinkedList {
 
 	Node head;
 	Node tail;
 
 	public LinkedList() {
 		head = null;
 		tail = null;
 	}
 
 	/**
 	 * Updates the linked list returning an entity if it is time to spawn it and
 	 * null if we are doing nothing.
 	 */
 	public Path update(float delta) {
 		if (head != null) {
 			head.update(delta);
 			if (head.getDelay() < 0) {
 
 				Path path = head.getPath();
 				head = head.getNext();
 
 				return path;
 			}
 		}
 
 		return null;
 
 	}
 
 	public Node getCurrent() {
 		return head;
 	}
 
 	public void add(Node node) {
 		if (head == null)
 			head = tail = node;
 		else {
 			tail.setNext(node);
 			tail = node;
 		}
 
 	}
 
 	public void add(LinkedList list) {
 		if (list != null && list.tail != null)
 			if (head == null) {
 				head = list.head;
 				tail = list.tail;
 			} else {
				tail.setNext(list.head);
 				tail = list.tail;
 			}
 
 	}
 
 	public boolean isEmpty() {
 		return head == null;
 	}
 
 }
