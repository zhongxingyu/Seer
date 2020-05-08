 // package alda.sort;
 
 // import java.util.List;
 import java.util.*;
 
 /**
  * Detta är en av algoritmerna du ska implementera själv. Algoritmen ska vara
  * quicksort, och pivotvärdet ska väljas slumpmässigt, det som på
  * sidan 266 i boken förklaras vara "a safe maneuver".
  * 
  * För information om quicksort generellt: se kursboken sidan 264 och framåt.
  * Tänk dock på att implementationen i boken jobbar på arrayer, men den här
  * jobbar på en lista av godtycklig typ och att den använder en annan
  * pivotvalsstrategi vilket kräver att man skriver om den lite grann.
  */
 public class QuickSorterRandom<T extends Comparable<? super T>> extends Sorter<T> {
 
 	private static final int CUTOFF = 10;
 
 	private void insertionSort(List<T> l, int left, int right) {
 		int j;
 		for (int p = left + 1; p <= right; p++) {
 			T tmp = l.get(p);
 			for (j = p; j > left && tmp.compareTo(l.get(j - 1)) < 0; j--) {
 				l.set(j, l.get(j - 1));
 			}
 			l.set(j, tmp);
 		}
 	}
 
 	private void quicksort(List<T> l, int left, int right) {
 		if (left + CUTOFF <= right) {
 			T pivot = random(l, left, right);
 
 			int i = left-1;
 			int j = right+1;
 
//			System.out.println(i);
//			System.out.println(j);
//			System.out.println();

 			for (;;) {
 				while (l.get(++i).compareTo(pivot) < 0) {
 				}
 				while (l.get(--j).compareTo(pivot) > 0) {
 				}
 				if (i < j)
 					swap(l, i, j);
 				else
 					break;
 			}
 
 			swap(l, i, right); // restore pivot
 
			quicksort(l, left, i - 1); // sort small elements
			quicksort(l, i + 1, right); // sort large elements
 		} else 
 		{
 			insertionSort(l, left, right);
 		}
 	}
 
 	private T random(List<T> l, int left, int right) 
 	{
 		Random rnd = new Random();
		int pivoNr = rnd.nextInt(right-left)+left;
 
 		swap(l, pivoNr, right);
 		return l.get(right);
 	}
 
 	@Override
 	protected void doSort(List<T> l) 
 	{
 		quicksort(l, 0, l.size() - 1);
 	}
 
 //	private static void copy(List l, int index1, int index2) {
 //		l.set(index1, l.get(index2));
 //	}
 
 	@SuppressWarnings("unchecked")
 	public static void main (String[]args)
 	{
 		List<Integer> myList = new ArrayList<Integer>();
 
 		Random r = new Random();
 		int maxNoDuplicates = (myList.size() / 100); 
 		int count = 0;
 
//		for(int e = 0; e<100; e++)
//			myList.add(r.nextInt(20000));
//
 //		do {
 //			count++;
 //			copy(myList, r.nextInt(myList.size()), r.nextInt(myList.size()));
 //		} while (count < maxNoDuplicates);
 
 		QuickSorterRandom rn = new QuickSorterRandom();
 		rn.doSort(myList);
 	}
 }
