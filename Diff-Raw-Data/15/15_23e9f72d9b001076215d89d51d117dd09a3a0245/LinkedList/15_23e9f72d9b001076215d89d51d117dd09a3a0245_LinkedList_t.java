 
 import java.util.Iterator;
 
 public class LinkedList<T> implements Iterable<T>{
 	private Node<T> firstNode;
 	private int size;
 	private int toDeleteCount;	
 	 
 	public LinkedList(){
 		this.size=0;
 		this.toDeleteCount=0;
 	}
 
 	public boolean isEmpty(){
 		return size==0;
 	}
 
 	public int size(){
 		return this.size;
 	}
 
 	public void checkIndex(int index){
 		if(index<0||index>=size){
 			throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
 		}
 	}
 
 	public T get(int index){
 		checkIndex(index);		
 		Node<T> current= this.firstNode;
		while(current.isRemoveable()){
			current=current.next();			
		}
 		int i=0;		
 		while(i<index){
 			if(!current.next().isRemoveable()){
 				i++;
 			}
 			current=current.next();
 		}
 		return current.getElement();
 	}
 
 	public int indexOf(T element){
 		int index= 0;
 		for(T t: this){
 			if(t.equals(element)){
 				return index;
 			}
 			index++;
 		}
 		return -1;
 	}
 
 	public T remove (int index){
 		checkIndex(index);
         	int i=0;
 		Node<T> currentNode= this.firstNode;		
 		while(currentNode.isRemoveable()){
 			currentNode=currentNode.next();			
 		}
 		while(i<index){
 			if(!currentNode.isRemoveable()){
 				i++;
 			}
 			currentNode=currentNode.next();
 		}
 		currentNode.setRemoveable(true);
 		this.toDeleteCount++;
 		this.size--;
 
 		T element = currentNode.getElement();
 		if(this.toDeleteCount>=this.size){
 			lazyDeletion();
 		}
 
 		return element;
 	}
 
 	public void lazyDeletion(){
 		if(size==0){
 			this.firstNode.setNext(null);
 			return;
 		}		
 		while(firstNode.isRemoveable()){
 			firstNode=firstNode.next();
 			this.toDeleteCount--;
 		}
 		Node<T> current= firstNode;	
 		while(this.toDeleteCount>0){
 			Node<T> next= current.next();
 			while(next!=null&&next.isRemoveable()){
 				next=next.next();
 				current.setNext(next);
 				this.toDeleteCount--;
 			}			
 			current=current.next();
 		}
 	}
 
 	public void add(T element){
 		add(this.size,element);
 	}
 
 	public void add(int index,T element){
 		if(index<0||index>size){
 			throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
 		}
 		Node<T> nod= new Node<>(element);		
 
 		if(index==0){
 			nod.setNext(firstNode);
 			this.firstNode=nod;
 		}else{
 			int i=0;
 			Node<T> currentNode= firstNode;
 			while(i<index-1){						
 				if(!currentNode.isRemoveable()){
 					i++;
 				}				
 				currentNode=currentNode.next();
 			}
 			nod.setNext(currentNode.next());
 			currentNode.setNext(nod);					
 		}
 		this.size++;
 	}
 
 	public Iterator<T> iterator(){
 		return new LinkedListIterator();
 	}
 
 	private class LinkedListIterator implements Iterator<T>{
 		private int nextPos;
 		private Node<T> nextNode;
 		
 		public LinkedListIterator(){
 			this.nextPos=0;
 			this.nextNode= firstNode;
 		}
 
 		@Override
 		public boolean hasNext(){
 			return nextPos<size;
 		}
 		@Override
 		public T next(){
 			while(nextNode.isRemoveable()){
 				nextNode=nextNode.next();
 			}
 			T t= nextNode.getElement();			
 			nextNode=nextNode.next();
 			nextPos++;
 			return t;
 		}
 		@Override
 		public void remove(){
 			System.out.println("Not Supported remove method");
 		}		
 
 	}
 
 
 	public static void main(String[] args){
 		LinkedList<String> test= new LinkedList<>();
 		System.out.println("is Empty: "+test.isEmpty());
 
 		test.add("a");
 		test.add("b");
 		test.add("c");
 		test.add("d");
 		test.add("e");
 		test.add("f");
 		test.add("g");
 		test.add("h");
 		test.add("i");
 		test.add("j");
 		
		test.remove(0);
 		for(String s: test){
 			System.out.println(s);
 		}
 		System.out.println("is Empty: "+test.isEmpty());
 		System.out.println("pos 6: "+test.get(6));
 	}
 }
