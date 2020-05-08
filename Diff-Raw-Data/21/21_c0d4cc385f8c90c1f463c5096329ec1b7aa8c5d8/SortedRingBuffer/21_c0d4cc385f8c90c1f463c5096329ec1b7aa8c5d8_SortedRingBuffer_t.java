 /*
  * Created on Jul 25, 2006
  */
 package zz.utils;
 
 import java.util.Comparator;
 
 
 /**
  * A ring buffer that sorts its elements according to a {@link Comparator}.
  * @author gpothier
  */
 public class SortedRingBuffer<T> extends RingBuffer<T>
 {
 	private Comparator<T> itsComparator;
 
 	public SortedRingBuffer(int aCapacity, Comparator<T> aComparator)
 	{
 		super(aCapacity);
 		itsComparator = aComparator;
 	}
 	
 	@Override
 	public void add(T aObject)
 	{
 		super.add(null);
 		
 		int i = getSize()-1;
 		while(i > 0)
 		{
 			T theOtherObject = get(i-1);
 			if (itsComparator.compare(theOtherObject, aObject) <= 0) break;
 			else set(i, theOtherObject);
 			
			i--;
 		}
		if (i == 0) System.err.println("SortedRingBuffer: warning, got back to the beginning of buffer.");
 		
 		set(i, aObject);
 	}
 }
