 package storage;
 
public class Node<T> {
 	private T data;
 	
 	public Node(T data) {
		if(this.data == null) {
 			throw new NullPointerException("The data to be stored was null");
 		}
 		this.data = data;
 	}
 	
 	public T getData() {
 		return this.data;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((data == null) ? 0 : data.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Node<?> other = (Node<?>) obj;
 		if (data == null) {
 			if (other.data != null) {
 				return false;
 			}
 		}
 		else if (!data.equals(other.data)) {
 			return false;
 		}
 		return true;
 	}
 }
