 public class SimpleMapElement {
 	private Object key, value;
 	private SimpleMapElement next;
 	private SimpleMapElement prev;
 	
 	public SimpleMapElement(Object key, Object value) {
 		this.key = key;
 		this.value = value;
 	}
 	
 	public Object getKey() {
 		return key;
 	}
 	public void setKey(Object key) {
 		this.key = key;
 	}
 	public Object getValue() {
 		return value;
 	}
 	public void setValue(Object value) {
 		this.value = value;
 	}
 	public SimpleMapElement getNext() {
 		return next;
 	}
 	public void setNext(SimpleMapElement next) {
 		this.next = next;
 	}
 	public SimpleMapElement getPrev() {
 		return prev;
 	}
 	public void setPrev(SimpleMapElement prev) {
 		this.prev = prev;
 	}
}
 	
 	
