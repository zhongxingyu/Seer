 package com.max.algs.ds.skip_list;
 
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ThreadLocalRandom;
 
 
 /**
  * 
  * Skip list implementation.
  * 
  * Don't allow to store NULL elements.
  * 
  */
 public class SkipListSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable{
 	
 
 	private static final long serialVersionUID = -9160359921559960550L;
 	
 	// Probability of adding element from level L(i) to L(i+1) is equals 1/4.	
 	private static final int PROBABILITY_FACTOR = 4; 
 	private static final Random RAND = ThreadLocalRandom.current();
 	
 	private int size;	
 	private final Node<E> head = Node.<E>createHead(0);
 	
 	private final Comparator<E> comparator;
 		
 	
 	public SkipListSet(){		
 		super();		
 		this.comparator = null;
 	}
 	
 	@Override
 	public Iterator<E> iterator() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 	public SkipListSet(Comparator<E> comparator ){		
 		super();		
 		this.comparator = comparator;
 	}
 	
 	protected int maxLevel(){
 		return head.level;
 	}
 	
 	
 	@Override
 	public void clear(){
 		head.forward.clear();
 		size = 0;	
 	}
 	
 	/*
 	 
 	 Delete(list, searchKey)
 		local update[1..MaxLevel]
 		x := list→header
 		
 		for i := list→level downto 1 do
 			while x→forward[i]→key < searchKey do
 				x := x→forward[i]
 			update[i] := x
 			
 		x := x→forward[1]
 		
 		if x→key = searchKey then
 			for i := 1 to list→level do
 				if update[i]→forward[i] ≠ x then break
 					update[i]→forward[i] := x→forward[i]
 				
 			free(x)
 			
 			while list→level > 1 and list→header→forward[list→level] = NIL do
 				list→level := list→level – 1
 	  
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean remove(Object obj){
 		
 		E valueToDelete = (E)obj;
 		
 		//TODO: implement this
 		return false;
 	}
 	
 	
 	/*
 	 
 	Insert(list, searchKey, newValue)
 		local update[1..MaxLevel]
 		x := list→header
 		
 		for i := list→level downto 1 do
 			while x→forward[i]→key < searchKey do
 				x := x→forward[i]				
 			-- x→key < searchKey ≤ x→forward[i]→key
 			update[i] := x
 			
 		x := x→forward[1]
 		
 		if x→key = searchKey then x→value := newValue
 		else
 			lvl := randomLevel()
 			
 			if lvl > list→level then
 				for i := list→level + 1 to lvl do
 					update[i] := list→header
 				list→level := lvl
 				
 			x := makeNode(lvl, searchKey, value)
 			
 			for i := 1 to level do
 				x→forward[i] := update[i]→forward[i]
 				update[i]→forward[i] := x
 	  
 	 */
 	public boolean add(E newValue){
 		
 		if( newValue == null ){
 			throw new IllegalArgumentException("NULL 'value' passed to 'add'");
 		}		
 		
 		if( isEmpty() ){
 			head.forward.add( 0, new Node<>(newValue) );
 			++size;
 			return true;
 		}
 		
 		List<Node<E>> update = new ArrayList<>( head.level );
 		
 		for( int i = 0; i <= head.level; i++ ){
 			update.add(new Node<E>());
 		}
 		
 		Node<E> cur = head;
 				
 		for(int i = head.level; i != -1; i--){
 			
 			while(  cur.forward.size() > i && cmp(cur.forward.get(i).value, newValue ) < 0 ){
 				cur = cur.forward.get(i);
 			}
 			//-- x→key < searchKey ≤ x→forward[i]→key
 			update.set(i, cur);
 		}
 			
 		if( ! cur.forward.isEmpty() ){
 			cur = cur.forward.get(0);
 		}
 				
 		if( cmp(cur.value, newValue) == 0 ){
 			return false;
 		}
 		else {			
 
 			int newLevel = randomLevel( head.level + 1);
 			
 			// add new level
 			if( newLevel > head.level ){
 				for( int i = head.level + 1; i <= newLevel; i++ ){
 					update.add(i, head);
 				}
 				head.level += 1;
 				
 			}			
 				
 			Node<E> newNode = new Node<>(newValue, newLevel);
 			
 			// update path
 			for( int i = 0; i < newLevel+1; i++){
 				
 				Node<E> nodeToUpdate = update.get(i);
 				
 				if( nodeToUpdate.forward.size() > i ){
 					newNode.forward.add(i, nodeToUpdate.forward.get(i));
 					nodeToUpdate.forward.set(i, newNode);
 				}				
 				else {
 					nodeToUpdate.forward.add(i, newNode);
 				}
 			}
 		}
 		
 		++size;
 		return true;
 		
 	}
 	
 	
 	/*	 
 	 Search(list, searchKey)
 		x := list→header
 		-- loop invariant: x→key < searchKey
 		
 		for i := list→level downto 1 do
 			while x→forward[i]→key < searchKey do
 				x := x→forward[i]
 				
 		-- x→key < searchKey ≤ x→forward[1]→key
 		
 		x := x→forward[1]
 		if x→key = searchKey then return x→value
 		else return failure
 			  
 	 */
 	@SuppressWarnings("unchecked")
 	@Override	
 	public boolean contains( Object obj ){
 		
 		E searchValue = (E)obj;
 		
 		if( isEmpty() || searchValue == null ){
 			return false;
 		}
 		
 		Node<E> cur = head;
 		
 		for( int i = head.level; i != -1; i-- ){			
 			while( cur.forward.size() > i && cmp( cur.forward.get(i).value, searchValue ) < 0 ){			
 				cur = cur.forward.get(i);
 			}
 		}
 		
 		// this guard clause can be removed in future
 		if( cur.forward.isEmpty()  ){
 			return false;
 		}
 		
 		cur = cur.forward.get(0);
 		
 		if( cmp( cur.value, searchValue) == 0 ){
 			return true;
 		}
 		
 		return false;
 	}
 	
 		
 	@Override
 	public int size(){
 		return size;
 	}
 	
 	@Override
 	public boolean isEmpty(){
 		return size == 0;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private int cmp(E value1, E value2){
 		if( comparator == null ){
 			
 			if( !(value1 instanceof Comparable) ){
 				throw new IllegalArgumentException("'" + value1.getClass() + "' doesn't implement Comparable interface");
 			}
 			
 			return ((Comparable<E>)value1).compareTo(value2);
 		}
 		return comparator.compare(value1, value2 );
 	}
 	
 	/*
 	 * Generate level, at which valoue should be inserted.
 	 */
 	private int randomLevel(int maxLevel){
 		
 		int level = 0;		
 		
 		while( RAND.nextInt(PROBABILITY_FACTOR) == 0 && level < maxLevel ){
 			++level;
 		}		
 		
 		return level;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public SkipListSet<E> clone() {
 		
 		SkipListSet<E> copy = null;
 		
 		try{
 			copy = (SkipListSet<E>)super.clone();
 			// TODO: copy internal state
 		}
 		catch( CloneNotSupportedException ex ){
			throw new InternalError( "'" + this.getClass().getName() + "' doens't implement Cloneable", ex);
 		}
 		
 		return copy;
 	}
 	
 	/**
 	 * Serialize object state
 	 */
 	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
 		s.defaultWriteObject();
 		//TODO:
     }
 
 	/**
 	* Reconstruct object state from serialized binary stream.
 	*/
 	private void readObject(java.io.ObjectInputStream s)  throws java.io.IOException, ClassNotFoundException {
 		s.defaultReadObject();		
 		// TODO:
 	}
 	
 	
 	
 	@SuppressWarnings("unused")
 	private static final class Node<U> {
 		
 	
 		U value;
 		boolean head;
 		
 		int level;
 		List<Node<U>> forward = new ArrayList<>();
 		
 		
 		static <V> Node<V> createHead(int level){
 			Node<V> node = new Node<V>();
 			node.head = true;
 			return node;
 		}		
 		
 		Node(U value) {
 			super();
 			this.value = value;
 		}
 		
 		Node(U newValue, int lvl ){
 			value = newValue;
 			level = lvl;
 			forward = new ArrayList<>( lvl );
 		}
 
 
 		boolean isHead(){
 			return head;
 		}
 
 		public Node() {
 			super();
 		}
 		
 		@Override
 		public String toString(){
 			if( head ){
 				return "HEAD, level: " + level;
 			}
 			
 			return String.valueOf("value: " + value + ", level: " + level);
 		}
 		
 	}
 
 }
