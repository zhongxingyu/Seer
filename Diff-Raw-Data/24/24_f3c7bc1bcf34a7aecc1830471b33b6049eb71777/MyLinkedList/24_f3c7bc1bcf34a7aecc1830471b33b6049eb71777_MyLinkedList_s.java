 package vilite;
 
 public class MyLinkedList implements ILinkedList {
 
 	private Node top;
 
 	public MyLinkedList() {
 		top = null;
 	}
 
 	@Override
 	public void add(String element) {
 
 		if(top == null) 
 			top = new Node(element, null);
 
 		else {
 			Node temp = top;
 
 			while(temp.getNext() != null) {
 				temp = (Node) temp.getNext();
 			}
 
 			temp.setNext((Node) new Node(element, null));
 		}
 	}
 
 	@Override
 	public void add(int index, String element) {
 		if(top == null) {
 			top = new Node(element, null);
 			return;
 		}
 
 		else {
 
 			if(index == 1) {
 				Node temp = new Node(element, top);
 				top = temp;
 
 				return;
 			}
 
 			else {
 				Node temp = top;
 
 				for(int i = 1; i < (index - 1); i++) {
 					if(temp.getNext() != null) {
 						temp = (Node) temp.getNext();
 					}
 					else {
 						this.add(element);
 
 						return;
 					}
 				}
 
 				temp.setNext((INode) new Node(element, (Node) temp.getNext()));
 				return;
 			}
 		}
 	}
 
 	@Override
 	public String remove(int index) {
 		//Remember to call isEmpty() before calling this because I will assume the list is not empty
 
 		//Case 1: Top element (index of 1)
 		if(index == 1) {
 			Node temp = top;
 			top = (Node) temp.getNext();
 			temp.setNext(null);
 		}
 
 		//Case 2: All other elements
 		else {
 
 			Node temp = top;
 
 			for(int i = 1; i < (index - 1); i++) {
 				if(temp.getNext() != null) {
 					temp = (Node) temp.getNext();
 				}

				temp.setNext(temp.getNext().getNext());
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public String get(int index) {
 		if(top == null)
 			return null;
 
 		if(index == 1)
 			return top.getData();
 
 		else {
 			Node temp = top;
 
 			for(int i = 1; i < index;) {
 				if(temp.getNext() != null) {
 					temp = (Node) temp.getNext();
 				}
 
 				i++;
 			}
 
 			return temp.getData();	
 		}
 	}
 
 	@Override
 	public boolean isEmpty() {
 
 		if(top == null)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public int size() {
 
 		Node temp = top;
 		int size = 0;
 
 		while(temp.getNext() != null) {
 			size++;
 		}
 
 		return size;
 	}
 
 	@Override
 	public void clear() {
 		top = null;
 	}
 }
