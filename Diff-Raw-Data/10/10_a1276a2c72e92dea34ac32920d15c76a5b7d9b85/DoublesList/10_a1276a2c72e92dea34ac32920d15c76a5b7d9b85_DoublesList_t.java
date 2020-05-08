 package doublesLibrary;
 
 public class DoublesList {
 
 	private double[] list;
 	private int size;
 
 	/* Constructor: Make a new ArrayList */
 	public DoublesList() {
 		size = 0;
 		list = new double[16];
 	}
 
 	/* Return a copy of the list as an array. Owner: Giorgos */
 	public double[] toArray() {
 		double[] ret = new double[ size ];
 
 		for(int i = 0; i < size; i++)
 			ret[i] = list[i];
 
 		return ret;
 	}
 
 	/* Re-allocate the array with double the size. Owner: Giorgos */
 	private void expand() {
 		double[] newList = new double[ list.length * 2 ];
 
 		for(int i = 0; i < list.length; i++)
 			newList[i] = list[i];
 		
 		list = newList;
 	}
 
 	/* Inset a new value at the specified index. Owner: Giorgos */
 	public void insert(int index, double value) throws IllegalArgumentException {
 		if(index < 0)
 			throw new IllegalArgumentException("Attempted to insert at a negative position: " + index);
 
 		if(index > size )
 			throw new IllegalArgumentException("Attempted to insert at a non-existent position: " + index);
 
 		/* Do I need a bigger array? */
 		if( list.length == size )
 			expand();
 
 		/* No values to displace */
 		if( index == size ) {
 			list[size] = value;
 			size++;
 		}
 		/* Need to shift values to the right */
 		else {
 			double prev = list[index];
 			list[index] = value;
 
 			for(int i = index+1; i < size; i++) {
 				double temp = list[i];
 				list[i] = prev;
 				prev = temp;
 			}
 			list[size] = prev;
 			size++;
 		}
 
 	}
 
	/* Add value to all elements after index i. Owner: bitzesmichail */
 	public void addNumAfter(int index, double value) throws IllegalArgumentException {
 		if(index < 0)
 			throw new IllegalArgumentException("Attempted to insert at a negative position: " + index);
 
 		if(index > size )
 			throw new IllegalArgumentException("Attempted to insert at a non-existent position: " + index);
 
 		for(int i = index+1; i < size; i++)	{
 			list[i] += value;
 		}
 
 	}
 
	/* Substract value from all elements after index i and remove zero-value elements. Owner: bitzesmichail */
 	public void subtractNumAfter(int index, double value) throws IllegalArgumentException {
 		if(index < 0)
 			throw new IllegalArgumentException("Attempted to insert at a negative position: " + index);
 
 		if(index > size )
 			throw new IllegalArgumentException("Attempted to insert at a non-existent position: " + index);
 
 		for(int i = index+1; i < size; i++)	{
 			list[i] -= value;
 		}
 
 		/* create a new list, copy all the elements before index(including the index, 
 		but only the non-zero elements after the index */
 		double[] newList = new double[ list.length ];
 
 		for(int i = 0; i <= index; i++) {
 			newList[i] = list[i];
 		}
 		int newListSize = index + 1;
 
 		for(int i = index+1; i < size; i++) {
 			if(list[i] != 0.0) {
 				newList[newListSize] = list[i];
 				newListSize++;
 			}
 		}
 
 		list = newList;
 		size = newListSize;
 	}
 
 }
 
 
 
  
