 package com.mycompany.app;
 /**HW-3, CS271
  * @author Mark Johnson
  *SinglyLinkedList Class
  */
 
import cs271.hw.liststackqueue.interfaces.SimpleList;
 
 public class SinglyLinkedList<T> implements SimpleList<T> {
 
 	private ElementHolder<T> head, tail;
 	private static int size = 0;
 
 	/**Add method adds a new element to a Singly-linked list;
 	 * New elements are added to the end of the list;
 	 * @return void;
 	 */
 	public void add(T data) {
 
 		ElementHolder<T> newElementHolder = new ElementHolder<T>();
 		newElementHolder.data=data;
 		if(size==0){
 			head=newElementHolder;
 			newElementHolder.next=null;
 			newElementHolder.previous=null;
 			tail=head;
 			size++;
 		}
 
 		else {
 			tail.next=newElementHolder;
 			newElementHolder.previous=tail;
 			tail=newElementHolder;
 			newElementHolder.next=null;
 			size++;
 			}
 	}
 
 	/**Accessor method;
 	 * @return the data field from the element at index "index";
 	 * returns null if "index" is out of the bounds of the list;
 	 */
 	public T get(int index) {
 		ElementHolder<T>current=new ElementHolder<T>();
 		int count = 0;
 		if((index<0)||(index>(size-1))||(head==null))return null;
 
 		for(current=head; count!=index; current=current.next){
 			count++;
 		}
 		return current.data;
 	}
 
 	/**Mutator method;
 	 * Removes the element at index "index";
 	 * @return the data field from the element at index "index";
 	 * returns null if "index" is out of the bounds of the list;
 	 */
 @Override
 	public T remove(int index) {
 		ElementHolder<T>myCurrentElementHolder=new ElementHolder<T>();
 		ElementHolder<T>myReturnValue=new ElementHolder<T>();
 		myCurrentElementHolder=head;
 
 		if((head==null)||(index<0)||(index>(size-1)))return null;
 
 		else if(index==0){
 			ElementHolder<T>LastElement=new ElementHolder<T>();
 			LastElement=head;
 			head = head.next;
 			size--;
 			return LastElement.data;
 			}
 
 		else if(index==(size-1)){
 			myCurrentElementHolder=tail;
 			myReturnValue=tail;
 			myCurrentElementHolder.previous.next=null;
 			tail=myCurrentElementHolder.previous;
 			size--;
 			return myReturnValue.data;
 
 		}
 
 		else{
 			for(int i=0; i<(index-1); i++){
 				myCurrentElementHolder=myCurrentElementHolder.next;
 			}
 
 			myReturnValue=myCurrentElementHolder.next;
 			myCurrentElementHolder.next=myCurrentElementHolder.next.next;
 			}
 		size --;
 		return myReturnValue.data;
 		}
 
 /**Default Constructor method;
  * Constructs a default Singly-linked list;
  * @return a default Singly-linked list;
  */
 	public SinglyLinkedList(){
 		head = null;
 		tail = null;
 		size = 0;
 	}
 
 	/**Accessor method;
 	 * Checks the state of a Singly-linked list  object to see if it is empty;
 	 * @return "true" if the list is empty and false otherwise;
 	 */
 	public boolean isEmpty() {
 		return head == null;
 
 	}
 
 	/**
 	 * @return the size, an int, of a Singly-linked list object;
 	 * I return the field "size" in order to have running time of O(1) for methods in
 	 * classes SimpleStackImpl and SimpleQueueImpl; I have "commented out" the original
 	 * method, which runs at O(n);
 	 */
 	public int size() {
 		return size;
 		//int count = 0;
 
 		//for(ElementHolder<T>current = head; current!=null; current=current.next)
 		//	count++;
 		//return count;
 	}
 
 	/**Nested Class, ElementHolder<T>;
 	 * Holds the elements in a Singly-linked list;
 	 * Added extra field, "previous", in order to make it run at O(1), worst
 	 * time; The "previous" field makes it possible to access the tail and
 	 * the element before the tail without iterating through the whole list;
 	 * Implementation of previous field makes the SinglyLinkedList class into a
 	 * doubly linked list class;
 	 */
 	protected class ElementHolder<T> {
 		protected T data;
 		protected ElementHolder<T>next;
 		protected ElementHolder<T>previous;
 	}
 }
