 package datatypes;
 
 public class SingleObjectHolder<E> {
	public int key;
 	public E value;
 	
	public SingleObjectHolder(int key, E value) {
 		this.key = key;
 		this.value = value;
 	}
 }
