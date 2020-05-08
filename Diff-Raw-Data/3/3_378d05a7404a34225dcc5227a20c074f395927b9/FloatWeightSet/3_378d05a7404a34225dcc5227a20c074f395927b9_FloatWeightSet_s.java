 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package ecologylab.collections;
 
 import java.util.*;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.Generic;
 import ecologylab.generic.MathTools;
 import ecologylab.generic.ThreadMaster;
 
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
  * There are a bunch of synchronized methods to affect this.
  **/
 public class FloatWeightSet
 extends Debug implements BasicFloatSet
 {
 /**
  * An array representation of the members of the set.
  * Kind of like a java.util.ArrayList, but faster.
  */
    protected 	FloatSetElement		elements[];
 
 /**
  * Used to maintain a sequential set of areas by weight, in order
  * to implement fast weighted randomSelect().
  */
    protected 	float				incrementalSums[];
 
 /**
  * Used as a boundary condition for fast implementations of sort.
  */
    protected final FloatSetElement	SENTINEL	= new FloatSetElement();
    
 /**
  * This might pause us before we do an expensive operation.
  */
    ThreadMaster		threadMaster;
 
    static final int PRUNE_PRIORITY	= 1;
    
    /**
     * Sets should get pruned when their size is this much larger than
     * what the caller originally specified.
     */
    static final int PRUNE_LEVEL = 0;
    
    /**
     * This many extra slots will be allocated in the set, to enable insertions()
     * prior to a prune(), without reallocation.
     */
    protected final int	extraAllocation;
    
    /**
     * Allocate this many extra slots, to avoid needing to alloc due to synch
     * issues between insert & prune.
     */
    public static final int EXTRA_ALLOCATION = 256;
    
    /**
 	* size of last used element in array
 	*/
    protected int	size; 
    /**
 	* size of the array. (Some upper slots are likely unused.)
 	*/
    int				numSlots;
    
    /**
     * Maximum size to let this grow to, before needPrune() will return true.
     * That is, when the set grows larger than this, it should be prune()ed.
     */
    protected int	pruneSize;
 
    
    /**
     * Number of active elements contained in this floatWeightSet.
     */
    protected int	numActive;
    
 /**
  * When there is more than one maximum found by maxSelect(), this ArrayList
  * holds the tied elements. That this is not either a temporary, or
  * passed back as the result of the sort operation, is hackish.
  */
    protected ArrayList		maxArrayList;
 
    /**
  * For managing sort operations
  */   
    static final int			TOO_SMALL_TO_QUICKSORT	= 10;
 
 /**
  * Create a set, with data structures to support the stated initial size.
  * This set will not support weightedRandomSelect().
  */
    public FloatWeightSet(int initialSize)
    {
       this(initialSize, null, false);
    }
    public FloatWeightSet(int initialSize, boolean supportWeightedRandomSelect)
    {
       this(initialSize, null, supportWeightedRandomSelect);
    }
 /**
  * Create a set, with data structures to support the stated initial
  * size, and a threadMaster that may pause use before expensive operations.
  * This set will not support weightedRandomSelect().
  */
    public FloatWeightSet(int initialSize, ThreadMaster threadMaster)
    {
 	  this(initialSize, threadMaster, false);
    }
    public FloatWeightSet(int initialSize, ThreadMaster threadMaster, 
 						 boolean supportWeightedRandomSelect)
    {
 		  //this(initialSize, EXTRA_ALLOCATION, threadMaster, 
 		  this(initialSize, initialSize/2, threadMaster, 
 						 supportWeightedRandomSelect);
    }
    
 /**
  * Create a set, with data structures to support the stated initial
  * size, and a threadMaster that may pause use before expensive operations.
  * This set may support weightedRandomSelect().
  */
    public FloatWeightSet(int initialSize, int extraAllocation,
 						 ThreadMaster threadMaster, 
 						 boolean supportWeightedRandomSelect)
    {
       this.threadMaster	= threadMaster;
 	  this.extraAllocation		= extraAllocation;
 
       size		= 0;
       pruneSize	= initialSize + extraAllocation/2;
       
       alloc(initialSize + PRUNE_LEVEL + extraAllocation, supportWeightedRandomSelect);
       SENTINEL.weight	= - Float.MAX_VALUE;
       insert(SENTINEL);
       debug("constructed w numSlots=" + numSlots + " maxSize=" + pruneSize + " extraAllocation="+extraAllocation);
    }
    private final void alloc(int allocSize, boolean supportWeightedRandomSelect)
    {
       elements		= new FloatSetElement[allocSize];
       numSlots		= allocSize;
    	  if (supportWeightedRandomSelect)
    	  	incrementalSums	= new float[allocSize];
    }
    /**
     * Delete all the elements in the set, as fast as possible.
  * @param doRecycleElements TODO
     *
     */
    public synchronized void clear(boolean doRecycleElements)
    {
    		for (int i=0; i<size; i++)
    		{
    			FloatSetElement element	= elements[i];
    			elements[i]				= null;
    			if (doRecycleElements)
    			{
    				element.setIndex(-1); // during gc() excursion
    				element.recycle();
                 element.clearSynch();
    			}
    			element.clear();
    		}
    		size	= 0;
    		this.maxArrayListClear();
    }
    public synchronized void insert(FloatSetElement el)
    {
       if (el == null)
 		 return;
       if (el.set != null)
       {
 		 debug("ERROR: tryed to double insert "+el+ " into this.\nIgnored.");
 //		 return;
       }
       if (size == numSlots)
       {	 // start housekeeping if we need more space
 		 int		allocSize	= 2 * size;
 		 debug("insert() alloc from " + size + " -> " + allocSize + " slots");
 		 FloatSetElement newElements[]	= new FloatSetElement[allocSize];
 		 numSlots			= allocSize;
 		 // finish housekeeping
 		 System.arraycopy(elements,0,        newElements,0, 	size);
 		 elements			= newElements;
 		 if (incrementalSums != null)
 		 {
 			float		newSums[]	= new float[allocSize];;
 			System.arraycopy(incrementalSums,0, newSums, 0, 	size);
 			incrementalSums		= newSums;
 		 }
       }
       // start insert
       el.setSet(this);
       elements[size]			= el;
       el.setIndex(size++);
    }
    /**
 	* Delete an element from the set.
 	* Perhaps recompute incremental sums for randomSelect() integrity.
 	* Includes ensuring we cant pick el again.
 	* 
 	* @param el			The FloatSetElement element to delete.
 	* @param recompute	-1 for absolutely no recompute.
 	* 			 0 for recompute upwards from el.
 	* 			 1 for recompute all.
 	**/
    public synchronized void delete(FloatSetElement el, int recompute)
    {
       int index		= el.getIndex();
       if ((size == 0) || (index < 0))
 		 return;
       // ???!!!      return NaN;
 	  //      float finalWeight	= el.getWeight();
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
 		 if (recompute != NO_RECOMPUTE)
 		 {
 			int recomputeIndex	= (recompute == RECOMPUTE_ALL) ? 0 : index;
 			syncRecompute(recomputeIndex);
 		 }
       }
    }
    public boolean pruneIfNeeded()
    {
    	  int pruneSize	 = this.pruneSize;
    	  boolean result = isOversize(pruneSize);
    	  if (result)
    	  	 prune(pruneSize);
    	  return result;
    }
    /**
     * Return true if the size of this is greater than the threshold passed in.
     * 
     * @param sizeThreshold
     * @return
     */
    protected boolean isOversize(int sizeThreshold)
    {
       return (size >= sizeThreshold);
    }
 
    /**
     * Prune to the set's specified maxSize, if necessary, then do a maxSelect().
     * The reason for doing these operations together is because both require sorting.
     * 
     * @return	element in the set with the highest weight, or in case of a tie,
     * 			a random pick of those.
     */
    public synchronized FloatSetElement pruneAndMaxSelect()
    {
    	  return maxSelect(pruneSize);
    }
    /**
     * Prune to the specified desired size, if necessary, then do a maxSelect().
     * 
     * @param desiredSize
     * @return	element in the set with the highest weight, or in case of a tie,
     * 			a random pick of those.
     */
    public synchronized FloatSetElement maxSelect(int desiredSize)
    {
       if (isOversize(desiredSize))
 		 prune(desiredSize);
       Thread.yield();
       FloatSetElement result	= maxSelect();
       if (result == SENTINEL)
       {  // defensive programming
 		 debug("maxSelect() ERROR chose sentinel??????!!! size="+ size +" maxArrayListSize="+maxArrayList.size());
 		 Thread.dumpStack();
 		 if (size > 1)
 			 result	= elements[--size];
 		 else
 			 result	= null;
       }
       if (result != null)
 		 result.delete(NO_RECOMPUTE);
       return result;
    }
    /**
     * Clear the ArrayList of tied elements from the last maxSelect().
     * This method can be overridden to provide post-process before the actual clear.
     * <p/>
     * The clear() should be done as part of maxSelect() to avoid memory leaks.
     */
    protected void maxArrayListClear()
    {
 	   if (maxArrayList != null)
 		   maxArrayList.clear();
    }
    /**
 	* @return	the maximum in the set. If there are ties, pick
 	* randomly among them
 	*/
    public synchronized FloatSetElement maxSelect()
    {
 	   int size			= this.size;
 	   switch (size)
 	   {
 	   case 0:	// should never happen cause of sentinel
 	   case 1:	// degenerate case (look out for the sentinel!)
 		   return null;
 	   case 2:
 		   return elements[1];
 	   default:	// now, size >= 3!
 		   break;
 	   }
 	   
 	   if (maxArrayList == null)
 	   {
 		   int arrayListSize	= size / 8;
 		   if (arrayListSize > 1024)
 			   arrayListSize	= 1024;
 		   maxArrayList		= new ArrayList(arrayListSize);
 	   }
 	   else
 		   maxArrayList.clear();
 	   
 	   //count how many active elements we have in this set.
 	   numActive = 0;
 	   
 	   //set result in case there's only 1 element in the set.
 	   FloatSetElement result= SENTINEL;
 	   float maxWeight		= result.getWeight();
 	   
 	   for (int i=1; i<size; i++)
 	   {
 		   FloatSetElement thatElement	= elements[i];
 		   if (!thatElement.filteredOut())
 		   {
 			   numActive++;
 			   float thatWeight	= thatElement.getWeight();
 			   if (thatWeight > maxWeight)
 			   {
 				   maxArrayList.clear();
 				   result			= thatElement;
 				   maxWeight		= thatWeight;
 				   maxArrayList.add(thatElement);
 			   }
 			   else if (thatWeight == maxWeight)
 			   {
 				   maxArrayList.add(thatElement);
 			   }
 		   }
 /*		   else
 		   {
 			  // debug("MAX SELECT FILTERED " + thatElement);
 		   }
 */	   }
 	   
 	   if (result == SENTINEL)
 		   return null;
 	   
 	   int numMax		= maxArrayList.size();
 	   
 	   //if there are more than one in our set, there is a tie, so choose which to get!
 	   if (numMax > 1)
 		   result			=
 			   (FloatSetElement) maxArrayList.get(MathTools.random(numMax));
 	   //maxArrayListClear();
 	   
 	   return result;
    }
    
    /**
     * If there was a tie in the last maxSelect() calculation, the others will be here.
     * 
     * @return
     */
    public ArrayList tiedForMax()
    {
 	   return maxArrayList;
    }
    /**
 	* Delete lowest-weighted elements, in case this collection has grown too big.
 	* (After all, we can't have the INFINITELY LARGE collections we'd really like.)
 	* @param	numToKeep -- size for the set after pruning is done.
 	*/
    public synchronized void prune(int numToKeep)
    {
 	  int size = this.size;
       if (size <= numToKeep)
 		 return;
       long startTime	= System.currentTimeMillis();
       debug("prune("+ size +" > "+ numToKeep+")");
       if (threadMaster != null)
       {
 		 debug("pause threads and sleep briefly");
 		 threadMaster.pauseThreads();
 		 Generic.sleep(2000);
       }
       Thread currentThread	= Thread.currentThread();
       int priority		= currentThread.getPriority();
       if (PRUNE_PRIORITY < priority)
 		 currentThread.setPriority(PRUNE_PRIORITY);
       //------------------ update weights ------------------//
       for (int i=0; i<size; i++)
 		 elements[i].getWeight();
       //------------------ sort in inverse order ------------------//
 	  //      println("gc() after update: " + size);
 	  //      insertionSort(elements, size);
       quickSort(elements, 0, size-1, false);
 	  //      println("gc() after sort: " + size);
 
       //-------------- lowest weight elements are on top -------------//
       for (int i=1; i!=numToKeep; i++)
 		 elements[i].setIndex(i);  // renumber cref index
 	  //      println("gc() after renumber: " + size);
       int oldSize	= size;
       this.size	= numToKeep;
       for (int i=numToKeep; i<oldSize; i++)
       {
 		 if (i >= elements.length)
 			debug(".SHOCK i="+i + " size="+size+ " numSlots="+numSlots);
 		 FloatSetElement thatElement	= elements[i];
 		 if (thatElement != null)
 		 {
 			elements[i]	= null;	   // its deleted or moved; null either way
 			//	    thatElement.setSet(null); // make sure its treated as not here
 //			debug("recycling " + thatElement);
 			thatElement.setIndex(-1); // during gc() excursion
 			thatElement.deleteHook();
 			if (thatElement.recycle())  // ??? create recursive havoc ??? FIX THIS!
 			{
 			   thatElement.clearSynch();
 			}
 			else
 			{
 			   debug("WEIRD: re-inserting pruned element: " + thatElement);
 			   insert(thatElement); // put it back in the right place!
 			}
 		 }
       }
       long duration = System.currentTimeMillis() - startTime;
       if (PRUNE_PRIORITY < priority)
 		 currentThread.setPriority(priority);
       if (threadMaster != null)
       {
 		 debug("prune() unpause threads");
 		 threadMaster.unpauseThreads();
 		 //Generic.sleep(1000);
       }
       debug("prune() finished " + duration);
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
 	  {
 		 if (incrementalSums != null)
 			result		= incrementalSums[size - 1] / size;
 		 else
 		 {
 			double sum	= 0;
 			for (int i=0; i<size; i++)
 			   sum		+= elements[i].weight;
 			result		= (float) (sum / ((double) size));
 		 }
 	  }
       return result;
    }
    void insertionSort(FloatSetElement buffer[], int n)
    {
       SENTINEL.weight	= Float.POSITIVE_INFINITY;
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
       SENTINEL.weight	= 0;	   // dont influence randomSelect() ops
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
 	  /*
 		 for (int i=0; i!=size; i++)
 		 {
 		 FloatSetElement element	= elements[i];
 		 result	+= element.index + "\t" + element.weight + "\t" +
 		 incrementalSums[element.index] + "\t" +
 		 element + "\n";
 		 }
 		 */
       return shortString();
    }
    public String shortString()
    {
       return super.toString() + "[" + size() + "]";
    }
    /**
 	* Check to see if the set has any elements.
 	* @return
 	*/
    public boolean isEmpty()
    {
       return size <= 1;
    }
    /**
     * Get the ith element in the set.
     * 
     * @param i
     * @return
     */
    public FloatSetElement getElement(int i)
    {
    	  return elements[i];
    }
    /**
     * Get the last element in the set, or null if the set is empty.
     * 
     * @return
     */
    public FloatSetElement lastElement()
    {
    	  return (size == 0) ? null : elements[size - 1];
    }
    /**
     * Get the number of active elements in this set.
     */
    public int numActive()
    {
 	   return numActive;
    }
 
 
 ///////////////////// stuff for fast weighted randomSelect() ////////////////
    /**
 	* @return	True if the set has elements to pick.
 	*/
    public synchronized boolean syncRecompute()
    {
       return syncRecompute(0);
    }
 /**
  * Prepare underlying datastructures for a weighted random select operation,
  * by using current weights to form incremental sums.
  */
    synchronized boolean syncRecompute(int index)
    {
 	  if (incrementalSums == null)
 		 throw new RuntimeException("trying to prepare for weightedRandomSelect(), but this FloatWeightSet was not setup to support it.");
 	  // println(">>> FloatWeightSet.recompute() size="+size);
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
 			   String errorScene	="recompute() ERROR at i=" +
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
 		 String errorScene	="\recompute() ERROR at i=" +
 			i + " w beyond=" + beyond + " in " + this;
 		 debug(errorScene);
 		 e.printStackTrace();
       }
       return (size > 1) && (sum > 3.0E-45f) && (sum != Float.NaN) &&
 		 (sum != Float.POSITIVE_INFINITY);
    }
 
    /**
 	* prune() if necessary, then syncRecompute(). 
 	* Then do weighted randomSelect(). 
 	* After the selection, delete the item from the set.
 	* 
 	* @param	desiredSize -- 
 	* 		If size of the set >= 2 * desiredSize, gc() down
 	*		to desiredSize.
 	*		If 0, never gc.
 	* 
 	* @return	Selected	
 	*/
 
    /**
     * Prune to the set's specified maxSize, if necessary, then do a
     * weightedRandomSelect().
     * The reason for doing these operations together is because both require 
     * structural recomputation, specifically getting the current weights for
     * all elements.
     * 
     * @return	element in the set with the highest weight, or in case of a tie,
     * 			a random pick of those.
     */
    public synchronized FloatSetElement pruneAndWeightedRandomSelect()
    {
    	  return weightedRandomSelect(pruneSize);
    }
    /**
     * Prune to the specified desired size, if necessary, then do a 
     * weightedRandomSelect().
     * The reason for doing these operations together is because both require 
     * structural recomputation, specifically getting the current weights for
     * all elements.
     * 
     * @param desiredSize
     * @return	an element from the set.
     */
    public synchronized FloatSetElement weightedRandomSelect(int desiredSize)
    {
       if (isOversize(desiredSize))
 		 prune(desiredSize);
       boolean ok	= syncRecompute();
       Generic.sleep(10);
       FloatSetElement element	= null;
       if (ok)
 		 element	= weightedRandomSelect();
       if (element != null)
 		 element.delete(NO_RECOMPUTE);
       return element;
    }
    /**
 	* weighted randomSelect() with no recompute to update the data structure. 
 	* Assumes caller is responsible for that updating via syncRecompute(...).
 	*/
    public synchronized FloatSetElement weightedRandomSelect()
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
 		 println("randomSelect Auis vais! last="+last+
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
 	  //     debug("randomSelect() " + pick + " => " + result + " from:");
 	  //     println(this);
       return result;
    }
 
 
    public static void main(String a[])
    {
       FloatWeightSet set	= new FloatWeightSet(4096);
       set.insert(new FloatSetElement(1.111f));
       set.insert(new FloatSetElement(12));
       for (int i=0; i!= 20; i++)
 		 set.insert(new FloatSetElement(-10000.33333f));
       set.insert(new FloatSetElement(1.111f));
       set.insert(new FloatSetElement(6.111f));
       set.insert(new FloatSetElement(33));
       set.syncRecompute();
       for (int i=0; i!= 10; i++)
 		 println(set.weightedRandomSelect().toString());
    }
    
    //				Accessors for MediaSetState
    ////////////////////////////////////////////////////////
    public FloatSetElement[] elements()
    {
 	   return elements;
    }
    public void setElements(FloatSetElement[] newElements)
    {
 	   elements = newElements;
    }
    public float[] incrementalSums()
    {
 	   return incrementalSums;
    }
    public void setIncrementalSums(float[] newIncrementalSums)
    {
 	   incrementalSums = newIncrementalSums;
    }
 
    public int numSlots()
    {
 	   return numSlots;
    }
    public void setNumSlots(int newNumSlots)
    {
 	   numSlots = newNumSlots;
    }
    public void setSize(int newSize)
    {
 	   size = newSize;
    }
    public void initializeStructure(int size)
    {
 	   alloc(size, true);
    }
    
    /**
     * Set the maximum size this should grow to before pruning.
     * 
     * @param maxSize The maxSize to set.
     */
       public void setPruneSize(int maxSize)
       {
    	  this.pruneSize = maxSize;
       }
    /**
     * Get the maximum size this should grow to before pruning.
     */   
    public int getPruneSize()
    {
 	   return pruneSize;
    }
 }
