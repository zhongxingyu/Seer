 package lab06;
 
 import java.lang.reflect.Array;
 import java.util.Comparator;
 
 public class MergeSorter implements Sorter {
 
 	@Override
 	public <E extends Comparable<? super E>> void sort(E[] data) {
 		//E[] tmpArray = new E [data.length];
 		//List<E>[] tmpArray = new List[data.length];
 		//tmpArray = (E[]) Array.newInstance(E[],data.length);
 		@SuppressWarnings("unchecked")
 		E[] tmpArray = (E[]) Array.newInstance(data[0].getClass(), data.length);
		//Object[] tmpArray = new Object [data.length];
 		mergeSort( data, tmpArray, 0, data.length - 1 );
 	}
 
 	@Override
 	public <E> void sort(E[] data, Comparator<? super E> comp) {
 		@SuppressWarnings("unchecked")
 		E[] tmpArray = (E[]) Array.newInstance(data[0].getClass(), data.length);
 		mergeSort( data, tmpArray, 0, data.length - 1, comp );
 	}
 
     private <E extends Comparable<? super E>> void mergeSort( E[] a, E[] tmpArray,
             int left, int right ) {
         if( left < right ) {
             int center = ( left + right ) / 2;
             mergeSort( a, tmpArray, left, center );
             mergeSort( a, tmpArray, center + 1, right );
             merge( a, tmpArray, left, center + 1, right );
         }
     }
     
     private <E> void mergeSort( E[] a, E[] tmpArray,
             int left, int right, Comparator<? super E> comp ) {
         if( left < right ) {
             int center = ( left + right ) / 2;
             mergeSort( a, tmpArray, left, center, comp);
             mergeSort( a, tmpArray, center + 1, right, comp);
             merge( a, tmpArray, left, center + 1, right, comp );
         }
     }
     
     
     private <E extends Comparable<? super E>> void merge( E [ ] a, E [ ] tmpArray,
             int leftPos, int rightPos, int rightEnd ) {
         int leftEnd = rightPos - 1;
         int tmpPos = leftPos;
         int numElements = rightEnd - leftPos + 1;
         
         // Main loop
         while( leftPos <= leftEnd && rightPos <= rightEnd )
             if( a[ leftPos ].compareTo( a[ rightPos ] ) <= 0 )
                 tmpArray[ tmpPos++ ] = a[ leftPos++ ];
             else
                 tmpArray[ tmpPos++ ] = a[ rightPos++ ];
         
         while( leftPos <= leftEnd )    // Copy rest of first half
             tmpArray[ tmpPos++ ] = a[ leftPos++ ];
         
         while( rightPos <= rightEnd )  // Copy rest of right half
             tmpArray[ tmpPos++ ] = a[ rightPos++ ];
         
         // Copy tmpArray back
         for( int i = 0; i < numElements; i++, rightEnd-- )
             a[ rightEnd ] = tmpArray[ rightEnd ];
     }
     
     private <E> void merge( E [ ] a, E [ ] tmpArray,
             int leftPos, int rightPos, int rightEnd, Comparator<? super E> comp ) {
         int leftEnd = rightPos - 1;
         int tmpPos = leftPos;
         int numElements = rightEnd - leftPos + 1;
         
         // Main loop
         while( leftPos <= leftEnd && rightPos <= rightEnd )
             if(comp.compare(a[leftPos], a[rightPos]) <= 0)
                 tmpArray[ tmpPos++ ] = a[ leftPos++ ];
             else
                 tmpArray[ tmpPos++ ] = a[ rightPos++ ];
         
         while( leftPos <= leftEnd )    // Copy rest of first half
             tmpArray[ tmpPos++ ] = a[ leftPos++ ];
         
         while( rightPos <= rightEnd )  // Copy rest of right half
             tmpArray[ tmpPos++ ] = a[ rightPos++ ];
         
         // Copy tmpArray back
         for( int i = 0; i < numElements; i++, rightEnd-- )
             a[ rightEnd ] = tmpArray[ rightEnd ];
     }
 	
 }
