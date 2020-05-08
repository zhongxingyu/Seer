 package storage;
 
public class Node<T> implements java.lang.Comparable {
 	private T data;
 	
 	public Node(T data) {
		if(data == null) {
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

	/**
	 * Returns -1 if object types don't match.
	 * Returns 0 if Node data is equal.
	 * Returns 1 if Node data is not equal.
	 */
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof Node) {
			Node n = (Node) arg0;
			
			if (this.getData().equals(n.getData())) {
				return 0;
			} else {
				return 1;
			}
		}
		else {
			return -1;
		}
	}
 }
