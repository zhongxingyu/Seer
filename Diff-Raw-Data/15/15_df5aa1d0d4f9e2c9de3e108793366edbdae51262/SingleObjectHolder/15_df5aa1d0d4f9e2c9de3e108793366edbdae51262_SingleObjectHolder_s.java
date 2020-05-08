 package datatypes;
 
 public class SingleObjectHolder<E> {
	public String key;
 	public E value;
 	
	public SingleObjectHolder(String key, E value) {
 		this.key = key;
 		this.value = value;
 	}
 }
