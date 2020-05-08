 package sim.structures;
 
 import java.awt.Rectangle;
 import java.util.Vector;
 
 import sim.gui.elements.GuiElement;
 import sim.gui.elements.GuiList;
 
 /**
  * A class representing a linked list. 
  * The attributes, doubly or singly linked and circular or non-circular 
  * are set in the constructor.
  *
  */
 public class LinkedList {
 	private GuiList gui;
 	private Vector<Node> v = new Vector<Node>();
 	
 	public GuiList getGuiElement(){
 		return gui;
 	}
 	/**
 	 * Constructor. 
 	 * @param bounds The size of the graphical element.
 	 * @param doublyLinked Determines whether nodes will link both forwards and backwards.
 	 * @param circular Determines if the last node will link to the first of the list (and vice verse if the list if doubly-linked)
 	 */
 	public LinkedList(Rectangle bounds, boolean doublyLinked, boolean circular){
 
 		Node dummy = new Node("dummy");
 		
 		dummy.setNext(dummy);
 		dummy.setPrevious(dummy);
 		dummy.setAnimated(false);
 		v.add(dummy);
 		gui = new GuiList(bounds,v, doublyLinked, circular);
 	}
 	/**
 	 * Adds a new node to the start of the list.
 	 * @param value The object (most likely a string of text) that this node is to contain.
 	 */
 	public void addFirst(Object value){
 		insertAfterElement(v.elementAt(0), value);
 	}
 	/**
 	 * Adds a new node to the end of the list.
 	 * @param value The object (most likely a string of text) that this node is to contain.
 	 */
 	public void addLast(Object value){
 		insertAfterElement(v.elementAt(v.size()-1), value);
 	}
 	/**
 	 * Adds a new node before a node in the list.
 	 * @param element The element that the new node will be placed before.
 	 * @param value The object (most likely a string of text) that this node is to contain.
 	 */
 	public void insertBeforeElement(Node element, Object value){
 		Node n = new Node(value);
 		n.setPrevious(element.getPrevious());
 		n.setNext(element);
 		
 		element.getPrevious().setNext(n);
 		
 		v.insertElementAt(n, v.indexOf(element));
 		
 		element.setPrevious(n);
 		gui.getAnimation().start();
 		gui.validate();
 		gui.repaint();
 	}
 	/**
 	 * Adds a new node after a node in the list.
 	 * @param element The element that the new node will be placed after.
 	 * @param value The object (most likely a string of text) that this node is to contain.
 	 */
 	public void insertAfterElement(Node element, Object value){
 		Node n = new Node(value);
 		
 		n.setPrevious(element);
 		n.setNext(element.getNext());
 		
 		element.getNext().setPrevious(n);
 		
 		v.insertElementAt(n, v.indexOf(element)+1);
 
 		element.setNext(n);
 
 		gui.getAnimation().start();
 		gui.validate();
 		gui.repaint();
 	}
 	
 	/**
 	 * The Node class is an inner class of {@link SingleLinkedList} that contains 
 	 * information about where an element is in relationship to other elements in a list
 	 */
 	public class Node{
 	// Class variables
 		private Object 	value;
 		private Node 	next;
 		private Node 	previous;
 		private boolean animated;
 		
 	// Getters and setters
 		public Node getNext() {
 			return next;
 		}
 
 		public void setNext(Node next) {
 			this.next = next;
 		}
 
 		public Node getPrevious() {
 			return previous;
 		}
 
 		public void setPrevious(Node previous) {
 			this.previous = previous;
 		}
 		public boolean isAnimated() {
 			return animated;
 		}
 
 		public void setAnimated(boolean animated) {
 			this.animated = animated;
 		}
 		public Object getValue() {
 			return value;
 		}
 
 		public void setValue(Object value) {
 			this.value = value;
 		}
 	
 	// Class constructor
 		public Node(Object value){
 			this.previous 	= null;
 			this.next 		= null;
 			this.value		= value;
 			animated = true;
 		}
 	}
 }
