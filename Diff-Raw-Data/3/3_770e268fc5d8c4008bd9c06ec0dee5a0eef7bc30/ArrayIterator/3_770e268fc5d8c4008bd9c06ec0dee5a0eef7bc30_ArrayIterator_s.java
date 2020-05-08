 package br.com.rodrigosasaki.structures.iterator;
 
 import java.util.Iterator;
 
 /**
  * @author Rodrigo Sasaki
  */
 public class ArrayIterator<E> implements Iterator<E>{
 
 	private E[] elements;
 	private int index;
 	
 	public ArrayIterator(E[] elements){
 		this.elements = elements;
 		index = 0;
 	}
 
 	@Override
 	public boolean hasNext(){
 		return index < elements.length;
 	}
 
 	@Override
 	public E next(){
 		return elements[index++];
 	}
 
 	@Override
 	public void remove(){
 		throw new UnsupportedOperationException("Remove operation is not supported.");
 	}
 	
 }
