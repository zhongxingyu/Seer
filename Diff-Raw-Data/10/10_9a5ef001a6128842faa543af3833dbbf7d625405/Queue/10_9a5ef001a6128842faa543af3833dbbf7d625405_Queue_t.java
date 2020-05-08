 package br.com.rodrigosasaki.structures.queue;
 
 import java.util.Iterator;
 
 import br.com.rodrigosasaki.structures.iterator.ArrayIterator;
 
 /**
  * @author Rodrigo Sasaki
  */
 public class Queue<E> implements Iterable<E>{
 
 	private static final int INITIAL_CAPACITY = 10;
 
 	private E[] elements;
 	private int head;
 	private int tail;
 	
 	public Queue(){
 		this(INITIAL_CAPACITY);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Queue(int size){
 		elements = (E[]) new Object[size];
 		head = 0;
 		tail = 0;
 	}
 
 	public void enqueue(E element){
 		if (arrayFull()){
 			growArray(elements.length * 2);
 		} else if (tailAtArrayEnd()){
 			tail = 0;
 		}
 		elements[tail++] = element;
 	}
 
 	public E dequeue(){
 		if(elements.length > INITIAL_CAPACITY && arrayLessThanQuarter()){
 			shrinkArray(elements.length / 2);
 		} else if (headAtArrayEnd()){
 			head = 0;
 		}
 		E item = elements[head];
 		elements[head++] = null;
 		organizeHeadAndTail();
 		return item;
 	}
 
 	private void organizeHeadAndTail(){
 		if(head == tail){
 			head = 0;
 			tail = 0;
 		}
 	}
 
 	private boolean arrayLessThanQuarter(){
 		return size() < elements.length / 4;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void shrinkArray(int length){
 		E tempArray[] = (E[]) new Object[length];
 		if(head < tail){
 			System.arraycopy(elements, head, tempArray, 0, size());
 		}else{
 			System.arraycopy(elements, head, tempArray, 0, elements.length - head);
 			System.arraycopy(elements, 0, tempArray, elements.length - head, tail + 1);
 		}
 		elements = tempArray;
 		tail = (length / 2) - 1;
 		head = 0;
 	}
 
 	private boolean arrayFull(){
 		return size() == elements.length;
 	}
 	
 	private boolean tailAtArrayEnd(){
 		return tail == elements.length;
 	}
 	
 	private boolean headAtArrayEnd(){
 		return head == elements.length;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void growArray(int length){
 		E tempArray[] = (E[]) new Object[length];
 		System.arraycopy(elements, head, tempArray, 0, elements.length - head);
 		System.arraycopy(elements, 0, tempArray, elements.length - head, head);
 		elements = tempArray;
 		head = 0;
 		tail = length / 2;
 	}
 
 	public int size(){
 		int size = 0;
 		if (tail > head){
 			return tail - head;
 		} else if (tail < head){
 			return (elements.length - head) + tail;
 		}
 		return size;
 	}
 
 	public boolean isEmpty(){
 		return size() == 0;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public Iterator<E> iterator(){
		E tempArray[] = (E[]) new Object[size()];
		if(head < tail){
			System.arraycopy(elements, head, tempArray, 0, size());
		}else{
			System.arraycopy(elements, head, tempArray, 0, elements.length - head);
			System.arraycopy(elements, 0, tempArray, elements.length - head, tail + 1);
		}
 		return new ArrayIterator<E>(tempArray);
 	}
 
 }
