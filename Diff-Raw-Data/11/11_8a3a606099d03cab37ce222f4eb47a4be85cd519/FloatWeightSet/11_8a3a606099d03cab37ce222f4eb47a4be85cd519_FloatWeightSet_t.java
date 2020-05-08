 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package cm.generic;
 
 import java.util.*;
 
 /**
  * Provides the facility of efficient weighted random selection from a set
  * elements, each of whicn includes a characterizing floating point weight.
  * <p>
  * 
  * Works in cooperation w <code>FloatSetElement</code>s; requiring that
  * user object to keep an integer index slot, which only we should be
  * accessing. This impure oo style is an adaption to Java's lack of
  * multiple inheritance. 
  * <p>
  *
  * Gotta be careful to be thread safe.
  * Seems no operations can safely occur concurrently.
  **/
 public class FloatWeightSet
 {
    protected 	float			incrementalSums[];
    protected 	FloatSetElement		elements[];
 
    FloatSetElement	sentinel	= new FloatSetElement();
    
 /**
  * size of last used element in array
  */
    protected int		size; 
 /**
  * size of the array. (Some upper slots are likely unused.)
  */
    int			numSlots;
 
    static final int	TOO_SMALL_TO_QUICKSORT	= 10;
 
    public FloatWeightSet(int initialSize)
    {
       size		= 0;
       alloc(initialSize + 4);
       sentinel.weight	= 0.0f;
       insert(sentinel);
    }
    public FloatWeightSet()
    {
       this(4096);
    }
    private final void alloc(int allocSize)
    {
       incrementalSums	= new float[allocSize];
       elements		= new FloatSetElement[allocSize];
       numSlots		= allocSize;
    }
    public synchronized void insert(FloatSetElement el)
    {
       if (el == null)
 	 return;
       if (el.set != null)
       {
 	 System.out.println("tryed to double insert "+el+ " into "+ this);
 	 return;
       }
       if (size == numSlots)
       {	 // start housekeeping if we need more space
 	 int		allocSize	= 2 * size;
 	 System.out.println("FloatWeightSet.insert() alloc from " +
 			    size + " -> " + allocSize + " slots for "+ this);
 	 float		newSums[]	= new float[allocSize];;
 	 FloatSetElement newElements[]	= new FloatSetElement[allocSize];
 	 numSlots			= allocSize;
 	 // finish housekeeping
 	 System.arraycopy(incrementalSums,0, newSums, 0, 	size);
 	 System.arraycopy(elements,0,        newElements,0, 	size);
 	 incrementalSums		= newSums;
 	 elements			= newElements;
       }
       // start insert
       el.setSet(this);
       float weight			= el.getWeight();
       // finish insert
       if (size > 0)
 	 incrementalSums[size]		= incrementalSums[size - 1] + weight;
       elements[size]			= el;
       el.setIndex(size++);
    }
 /**
  * Delete an element from the set.
  * Perhaps recompute incremental sums for randomSelect() integrity.
  * Includes ensuring we cant pick el again.
  * 
  * @parm	The SetElement element to delete.
  * @param	recompute:
  * 			-1 for absolutely no recompute
  * 			 0 for recompute upwards from el
  * 			 1 for recompute all
  **/
    public synchronized void delete(FloatSetElement el, int recompute)
    {
       int index		= el.getIndex();
       if ((size == 0) || (index < 0))
 	 return;
       // ???!!!      return NaN;
       float finalWeight	= el.getWeight();
       int lastIndex	= --size;
       if (index != lastIndex)	// if not the last element
       {
 	 // swap the last element from down there
 	 FloatSetElement lastElement;
 	 // ??? this is a workaround for some horrendous bug that is
 	 // corrupting this data structure !!!
 	 while ((lastElement = elements[lastIndex]) == null)
 	 {
 	    size--;
 	    lastIndex--;
 	 }
 	 synchronized (lastElement)
 	 {
 	    elements[index]		= lastElement;
 	    elements[lastIndex]	= null;	// remove reference to allow gc
 	    lastElement.setIndex(index);
 	 }
 	 if (recompute != -1)
 	 {
 	    int recomputeIndex	= (recompute == 0) ? 0 : index;
 	    syncRecompute(recomputeIndex);
 	 }
       }
       el.deleteSkel();
    }
 /**
  * @return	True if the set has elements to pick.
  */
    public synchronized boolean syncRecompute()
    {
       return syncRecompute(0);
    }
    synchronized boolean syncRecompute(int index)
    {
 //      System.out.println(">>> FloatWeightSet.recompute() size="+size);
       // recompute sums above there
       float sum		= 0;
       if (index > 0)
 	 sum		= incrementalSums[index - 1];
       int beyond		= size;
       int i=0;
       try
       {
       for (i=index; i!=beyond; i++)
       {
 	 FloatSetElement element= elements[i];
 	 if (element == null)
 	 {
 	    String errorScene	="\nFloatWeightSet.recompute() error at i=" +
 	       i + " w beyond=" + beyond + " in " + this;
 	    System.out.println(errorScene + ".\nTrying to heal thyself.");
 	    FloatSetElement lastElement	= elements[--beyond];
 	    elements[i]			= lastElement;
 	    elements[beyond]	= null;	// remove reference in case of gc
 	    i--;		 // process the new one delete will swap down
 	    size--;
    	 }
 	 else
 	 {
 	    float weight		= element.getWeight();
 	    // ??? This kludge tries to avoid nasty errors.
 	    // Still it's kind of a bad idea, cause it hides dynamic
 	    // range problems that should be fixed elsewhere.
 	    if ((weight != Float.NaN) && (weight > 0) &&
 		(weight != Float.POSITIVE_INFINITY))
 	       sum		       += weight;
 	    incrementalSums[i]	= sum;
 	 }
       }
       } catch (Exception e)
       {
 	 String errorScene	="\nFloatWeightSet.recompute() error at i=" +
 	    i + " w beyond=" + beyond + " in " + this;
 	 System.out.println(errorScene);
 	 e.printStackTrace();
       }
       return (size > 1) && (sum > 3.0E-45f) && (sum != Float.NaN) &&
 	 (sum != Float.POSITIVE_INFINITY);
    }
 /**
  * gc() if necessary. Yield, then syncRecompute(). 
  * Then do weighted randomSelect(). 
  * After the selection, delete the item from the set.
  * 
  * @param	halfGcThreshold -- 
  * 		If size of the set >= 2 * halfGcThreshold, gc() down
  *		to halfGcThreshold.
  *		If 0, never gc.
  * 
  * @return	Selected	
  */
    public synchronized FloatSetElement randomSelect(int halfGcThreshold)
    {
       if ((halfGcThreshold > 0) && (size >= 2 * halfGcThreshold))
 	 gc(halfGcThreshold);
 //      Thread.yield(); // [very slow!]
       boolean ok	= syncRecompute();
 //      Thread.yield();
       Generic.sleep(10);
       FloatSetElement element	= null;
       if (ok)
 	 element	= randomSelect();
       if (element != null)
 	 element.delete(-1);
       return element;
    }
 /**
  * weighted randomSelect() with no recompute to update the data structure. 
  * Assumes caller is responsible for that updating via syncRecompute(...).
  */
    public synchronized FloatSetElement randomSelect()
    {
       if (size <= 1)		// degenerate case
 	 return null;
 
       // setup for binary search
       FloatSetElement result;
       int last	= size - 1;
       int first	= 1;		   // element 0 is the sentinel
 
       // use last sum as scale
       float	pick	= (float) (incrementalSums[last] * Math.random());
 //      System.out.println("randomSelect pick=" + pick);
       if (pick == 0)
       {
 	 System.out.println("randomSelect Auis vais! last="+last+
 			    " last sum=" + incrementalSums[last] + "\n"+
 			    this);
       }
       // do binary search. find the smallest incrementalSum larger than pick.
       while (true)
       {
 	 int thisSize = last - first + 1;
 	 if (thisSize == 1)
 	 {
 	    result	=  elements[first];
 	    break;
 	 }
 	 else
 	 {
 	    if (thisSize == 2)
 	    {
 	       if (pick <= incrementalSums[first])
 		  result	= elements[first];
 	       else
 		  result	= elements[last];
 	       break;
 	    }
 	    else
 	    {
 	       // !!! minus one here because of <= rule includes mid
 	       // when we recurse !!! subtle
 	       int   midIndex	= first + (thisSize - 1) / 2;
 	       float midSum	= incrementalSums[midIndex];
 	       if (pick == midSum)	// try to get out early - not needed!
 	       {
 		  result	= elements[midIndex];
 		  break;
 	       }
 	       else if (pick < midSum)	// check bottom half
 		  last	= midIndex;	// include mid cause <= rule
 	       else			// pick > midSum - check top half
 		  first	= midIndex + 1;
 	    }
 	 }
       }
 //     System.out.println("randomSelect() " + pick + " => " + result + " from:");
 //     System.out.println(this);
       return result;
    }
    ArrayList		maxArrayList;
    
 /**
  * @return	the maximum in the set. If there are ties, pick
  * randomly among them
  */
    public synchronized FloatSetElement maxSelect()
    {
       int size			= this.size;
      if (size <= 1)		// degenerate case
	 return null;

       if (maxArrayList == null)
       {
 	 int arrayListSize	= size / 4;
 	 if (arrayListSize > 1024)
 	    arrayListSize	= 1024;
 	 maxArrayList		= new ArrayList(arrayListSize);
       }
       else
 	 maxArrayList.clear();
       
       int maxIndex		= MathTools.random(size);
       FloatSetElement result	= elements[maxIndex];
       float maxWeight		= result.getWeight();
       for (int i=0; i<size; i++)
       {
 	 FloatSetElement thatElement	= elements[i];
 	 float thatWeight	= thatElement.getWeight();
 	 if (thatWeight > maxWeight)
 	 {
 	    result		= thatElement;
 	    maxWeight		= thatWeight;
 	    maxIndex		= i;
 	 }
 	 else if (thatWeight == maxWeight)
 	    maxArrayList.add(thatElement);
       }
       int numMax		= maxArrayList.size();
       if (numMax > 1)
 	 result			=
 	    (FloatSetElement) maxArrayList.get(MathTools.random(numMax));
       return result;
    }
 /**
  * Delete lowest-weighted elements.
  * @param	numToKeep -- size for the set after gc is done.
  */
    public synchronized void gc(int numToKeep)
    {
       if (size <= numToKeep)
 	 return;
       System.out.println("\n\n\nFloatWeightSet.gc("+numToKeep+") of " +
 			 size);
       
       //------------------ update weights ------------------//
       for (int i=0; i!=size; i++)
 	 elements[i].getWeight();
       //------------------ sort in inverse order ------------------//
 //      System.out.println("gc() after update: " + size);
       insertionSort(elements, size);
 //      System.out.println("gc() after sort: " + size);
       String before	= "gc(): " + size  + ", " + Memory.usage();
 
       //-------------- lowest weight elements are on top -------------//
       int wontGo	= 0;
       for (int i=1; i!=numToKeep; i++)
 	 elements[i].setIndex(i);  // renumber cref index
 //      System.out.println("gc() after renumber: " + size);
       int oldSize	= size;
       size	= numToKeep;
       for (int i=numToKeep; i!=oldSize; i++)
       {
 	 if (i >= elements.length)
 	    System.out.println("FloatWeightSet.SHOCK i="+i + " size="+size+
 			       " numSlots="+numSlots);
 	 FloatSetElement thatElement	= elements[i];
 	 if (thatElement != null)
 	 {
 	    elements[i]	= null;	   // its deleted or moved; null either way
 //	    thatElement.setSet(null); // make sure its treated as not here
 	    thatElement.setIndex(-1); // during gc() excursion
 	    if (thatElement.gc())  // ??? create recursive havoc ??? FIX THIS!
 	       thatElement.deleteSkel();
 	    else
 	       insert(thatElement); // put it back in the right place!
 	 }
       }
 //      System.gc();
 //      System.runFinalization();
 //      System.out.println(before + " -> " + size  + ", " + Memory.usage() +
 //			 ":\n" + this);
    }
 //      System.out.println("Before sort :\n"+ this);
       // sentinel always in position 0 -- avoid it!!!
 //      quickSort(elements, 1, size-1, false);
       // always do insertion sort, cause we leave short runs untouched
       // by quicksort, as per Sedgewick via Siegel and Cole
    public float mean()
    {
       float result;
       if (size == 0)
 	 result		= 1;
       else
 	 result		= incrementalSums[size - 1] / size;
       return result;
    }
    void insertionSort(FloatSetElement buffer[], int n)
    {
       sentinel.weight	= Float.POSITIVE_INFINITY;
       for (int i=2; i!=n; i++)
       {
 	 int current		= i;
 	 FloatSetElement toBeInserted	= buffer[current];
 	 float weightToInsert	= toBeInserted.weight;
 	 FloatSetElement below	= buffer[current - 1];
 	 while (weightToInsert	> below.weight)
 	 {
 	    buffer[current]	= below;
 	    below		= buffer[--current - 1];
 	 }
 	 buffer[current]	= toBeInserted;
       }
       n++;
       sentinel.weight	= 0;	   // dont influence randomSelect() ops
    }
    void quickSort(FloatSetElement buffer[],
 		  int lower, int upper, boolean printDebug)
    {
       if (lower >= upper)	   // ??? remove this condition !!!
 	 return;
       //------------------- choose a pivot ------------------------//
       // choose a sample pivot in the middle by swapping it to the bottom
       int pivot1		= (lower + upper) / 2;
       int pivot2		= (lower + pivot1) / 2;
       int pivot3		= (pivot1 + upper) / 2;
       float weight1		= buffer[pivot1].weight;
       float weight2		= buffer[pivot2].weight;
       float weight3		= buffer[pivot3].weight;
       int pivotIndex;
       if (weight1 < weight2)
       {
 	 if (weight1 < weight3)
 	 {			   // weight1 is lowest
 	    if (weight2 < weight3)
 	       pivotIndex	= pivot2;
 	    else
 	       pivotIndex	= pivot3;
 	 }
 	 else			   // weight3 is lowest
 	 {
 	    pivotIndex		= pivot1;
 	 }
       }
       else			   // weight2 < weight1
       {
 	 if (weight2 < weight3)
 	 {			   // weight2 is lowest
 	    if (weight1 < weight3)
 	       pivotIndex	= pivot1;
 	    else
 	       pivotIndex	= pivot3;
 	 }
 	 else			   // weight3 is lowest
 	    pivotIndex		= pivot2;
       }
       FloatSetElement pivot	= buffer[pivotIndex];
       //-- make sure pivot works w upper as a sentinel --//
       FloatSetElement tempL	= buffer[lower];
       FloatSetElement tempP	= buffer[pivotIndex];
 //      buffer[lower]	= temp2;   // delay, expecting extra swap
       buffer[pivotIndex]	= tempL;
 //      if (printDebug)
 //	 System.out.println("\ttrial pivotWeight="+buffer[pivotIndex]+
 //			    " pivotIndex="+pivotIndex);
       // make buffer[bottom] and buffer[top] be backwards, to offset
       // extra swap at start of do
       FloatSetElement tempU	= buffer[upper];
       // make sure the weight in upper (that is, the pivot)
       // is greater than the one in lower:
       // it's the original and moving to upper as sentinel.
       // in other words, that now lower element (that will again be
       // upper as sentinel) should be in proper relationship (here,
       // less than, cause we're doing a reverse sort) w the pivot.
       // pivot and temp lower = real upper get reversed in the 1st loop, below
       // nb: could use FloatSetElement.greaterThan() here and below;
       // prettier, but i suspect less efficient.
       if (tempP.weight > tempU.weight)
       {
 	 buffer[lower]	= tempU;
 	 buffer[upper]	= tempP;
 	 buffer[pivotIndex]	= tempL;
       }
       else  // didnt work. try to rectify.
       {
 	 if (tempP.weight > tempL.weight)
 	 {
 	    buffer[lower]	= tempL;
 	    buffer[upper]	= tempP;
 	    buffer[pivotIndex]	= tempU;
 	 }
 	 else
 	 {
 	    buffer[pivotIndex]	= tempL;
 	    buffer[lower]	= tempP;   // previously chosen pivot in lower
 	 }
       }
       //------------------- quickSort mccoy ------------------------//
       int bottom	= lower;
       int top		= upper;
       FloatSetElement pivotElement	= buffer[upper];
       float pivotWeight	= pivotElement.weight;	// soon to be lower!
       if (printDebug)
 	 System.out.println("quicksort lower="+lower+" " +"upper="+upper+
 			    " pivotWeight="+pivotWeight);
       do
       {
 	 // swap buffer[bottom], buffer[top]
 	 // note symmetric stopping conditions <=pivot, >=pivot
 	 FloatSetElement temp3	= buffer[bottom];
 	 buffer[bottom]	= buffer[top];
 	 buffer[top]	= temp3;
 
 	 FloatSetElement topElement;
 	 float topWeight;
 	 do	// find a top Element that needs swapping if there is one
 	 {
 	    topElement	= buffer[--top];
 	    topWeight	= topElement.weight;
 	 }
 	 while (topWeight < pivotWeight);
 	 FloatSetElement bottomElement;
 	 float bottomWeight;
 	 // find a bottom FloatSetElement that needs swapping if there is one
 	 do
 	 {
 	    bottomElement	= buffer[++bottom];
 	    bottomWeight	= bottomElement.weight;
 	 }
 	 while (bottomWeight > pivotWeight);
       } while (bottom < top);
       FloatSetElement temp4	= buffer[lower];
       buffer[lower]	= buffer[top];
       buffer[top]	= temp4;
 //      if (printDebug)
 //	 System.out.println("\ttop="+top);
       if (lower + TOO_SMALL_TO_QUICKSORT < top)
 	 quickSort(buffer, lower, top - 1, printDebug);
       if (top + TOO_SMALL_TO_QUICKSORT < upper)
 	 quickSort(buffer, top + 1, upper, printDebug);
    }
   
    // ------------------------ utilities ---------------------------- //
    //
    public int size()
    {
       return size - 1;		   // leave out the sentinel
    }
    public String toString()
    {
       String result	= size + " elements";
       if (!empty())
       {
 	 FloatSetElement element = elements[2];
 	 if (element != null)
 	    result	+= " of " + element.getClass();
       }
       result		+= ".\n";
 /*
       for (int i=0; i!=size; i++)
       {
 	 FloatSetElement element	= elements[i];
 	 result	+= element.index + "\t" + element.weight + "\t" +
 	    incrementalSums[element.index] + "\t" +
 	    element + "\n";
       }
 */
       return result;
    }
    public boolean empty()
    {
       return size <= 1;
    }
    public static void main(String a[])
    {
       FloatWeightSet set	= new FloatWeightSet();
       set.insert(new FloatSetElement(1.111f));
       set.insert(new FloatSetElement(12));
       for (int i=0; i!= 20; i++)
 	 set.insert(new FloatSetElement(-10000.33333f));
       set.insert(new FloatSetElement(1.111f));
       set.insert(new FloatSetElement(6.111f));
       set.insert(new FloatSetElement(33));
       set.syncRecompute();
       for (int i=0; i!= 10; i++)
 	 System.out.println(set.randomSelect());
    }
 }
