 /*
  * Created on Jul 25, 2006
  */
 package zz.utils;
 
 public class RingBuffer<T>
 {
 	private T[] itsBuffer;
 	private int itsSize;
 	private int itsInputIndex;
 	private int itsOutputIndex;
 	
 
 	public RingBuffer(int aCapacity)
 	{
 		itsBuffer = (T[])new Object[aCapacity];
 		itsSize = 0;
 		itsInputIndex = 0;
 		itsOutputIndex = 0;
 	}
 	
 	public boolean isFull()
 	{
 		return itsSize == itsBuffer.length;
 	}
 	
 	public boolean isEmpty()
 	{
 		return itsSize == 0;
 	}
 	
 	/**
 	 * Returns the number of elements stored in this buffer.
 	 */
 	public int getSize()
 	{
 		return itsSize;
 	}
 
 	public void add(T aObject)
 	{
 		if (isFull()) throw new IllegalStateException("Buffer is full");
 		itsBuffer[itsInputIndex] = aObject;
 		itsInputIndex = (itsInputIndex + 1) % itsBuffer.length;
 		itsSize++;
 	}
 	
 	public T remove()
 	{
 		if (isEmpty()) throw new IllegalStateException("Buffer is empty");
 		T theObject = itsBuffer[itsOutputIndex];
 		itsBuffer[itsOutputIndex] = null;
 		itsOutputIndex = (itsOutputIndex+1) % itsBuffer.length;
 		itsSize--;
 		
 		return theObject;
 	}
 	
 	/**
 	 * Returns the element at the specified index.
 	 * Index 0 is the last added object that has not been removed.
 	 */
 	public T get(int aIndex)
 	{
 		if (aIndex >= itsSize) throw new IndexOutOfBoundsException(""+aIndex+" >= "+itsSize);
 		return itsBuffer[(itsOutputIndex + aIndex) % itsBuffer.length];
 	}
 	
 	/**
 	 * Sets the element at the specified index.
 	 * Index 0 is the last added object that has not been removed.
 	 * @return The element that is being overwritten.
 	 */
 	public T set(int aIndex, T aObject)
 	{
 		if (aIndex >= itsSize) throw new IndexOutOfBoundsException(""+aIndex+" >= "+itsSize);
 		T theOld = itsBuffer[(itsOutputIndex + aIndex) % itsBuffer.length];
 		itsBuffer[(itsOutputIndex + aIndex) % itsBuffer.length] = aObject;
 		return theOld;
 	}
 }
