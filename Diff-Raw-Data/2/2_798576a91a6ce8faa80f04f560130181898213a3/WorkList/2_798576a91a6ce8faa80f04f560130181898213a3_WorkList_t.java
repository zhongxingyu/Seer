 package worklists;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import net.sourceforge.interval.ia_math.RealInterval;
 
 import choosers.Chooser;
 import core.Box;
 
 
 public abstract class WorkList {
 	protected Collection<Box> collection;
 	protected Screener screener;
 	protected Chooser chooser;
 	private int maxSize = -1;
 	private int avgSize = -1;
 	private int boxAdded = -1;
 	
 	private static final int sizeThreshold = 20;
 	private static final int updatesThreshold = 10;
 	private static final long memoryThreshold = 1 * 1024*1024; // 1 Mb;
 
 	boolean _dbg_InitialAreaAddedCorrectly = false;
 	
 	private static final boolean logging = false;
 
 
 	/*
 	 * Has a protected constructor only
 	 */
 	protected WorkList(Collection<Box> collectionToUse, Box area) {
 		/*
 		 *  child has to instantiate particular collection
 		 *  sorted and unsorted list uses different collections
 		 */ 
 		assert(collectionToUse != null);
 		collection = collectionToUse;
 		chooser = null;
 
 		/*
 		assert(area != null); // previously we allowed creation of a worklist w/o search area
 								// now it is forbidden because we treats initial search area
 								// in a different way than the rest of the boxes (see @addSearchArea@).
 		// IT WAS really inconvenient and now we do allow this again*/ 
 		double limit = Double.MAX_VALUE; 
 		screener = new Screener(limit);
 		addSearchArea(area);
 	}
 	/*
 	 * Screening by derivative will throw away any box on which the derivative 
 	 * doesn't contain 0. Because a point could be a minimum or a maximum if 
 	 * the derivative is equal to zero in this point OR this is a border point. 
 	 * Consider the following case:
 	 * f(x) = x, min_{0<x<1}(f) = f(0), but f'(x) != 0.
 	 * BUT instead of checking if a box contains borders we just add all ages to the
 	 * working list from the very beginning! Much simple and less code: )  
 	 */
 	protected void addAreaAndAllEges(Box area) {
 		assert(collection.size() == 0); 
 		assert(area != null);
 		addChecked(area); // Otherwise add() will call addAllEges() that calls add()
 							// actually now it don't. Nevertheless this is no point in use screener on initial area
 							// especially because usually the function value is not set.
 		final int dim = area.getDimension();
 		for (int i = 0; i < dim; i++) {
 			Box edge1 = area.clone();
 			Box edge2 = area.clone();
 			RealInterval side = area.getInterval(i);
 			edge1.setInterval(i, side.lo());
 			edge2.setInterval(i, side.hi());
 			this.addChecked(edge1); // now add() doesn't screen out by derivatives boxes
 			this.addChecked(edge2); //   with any side with width = 0. nevertheless there is 
 									//   no point to use add() here because function values are not set
 		}
 	}
 	public void addSearchArea(Box area) {
 		if (area == null)
 			return;
 		// we do this only for initial search area
 		assert(collection.size() == 0);
 		_dbg_InitialAreaAddedCorrectly = true;
 		addAreaAndAllEges(area);
 	}
 /*	
 	public void addSearchArea(Box[] area) {
 		throw new RuntimeException("NOT IMPL.");
 		// for (Box b : area) {
 		//	addAllEges(b);
 		// }
 		// check and test it!
 	}
 */	
 /*
 	@Override
 	protected void finalize() {
 		if (debug){
 			System.out.println("_____________________");
 			System.out.println("WorkList statistiks:");
 			System.out.println("  Max size of the list was " + maxSize);
 			System.out.println("  Average size of the list " + avgSize);
 			System.out.println("  Num of boxes inserted    " + boxAdded);
 			System.out.println("  Attempts to GC the list  " + attemptsToClean);
 			System.out.println("  Successfully cleaned     " + listCleanedTimes);
 			System.out.println("----------------------");
 		}
 	}
 */
 	public void setChooser(Chooser c) {
 		chooser = c;
 	}
 	
 	/*
 	 * this method indeed adds box to the list.
 	 * add(Box) and add(Box[]) lock the list, perform
 	 * checks with Screener and call this method for
 	 * actual adding. Is the list sorted or not depends on 
 	 * implementation of this method.
 	 */
 	protected abstract void addChecked(Box box);
 	
 	/*
 	 * this method returns a box with the lowest
 	 * low border - so called the leading box.
 	 * The method is used in getOptimumValue()
 	 * Also can be used in some strategies to 
 	 * split the most suspicious box.
 	 * We do not know anything about box order
 	 * so we have to ask a child
 	 * that implements the actual behavior 
 	 */
 	public abstract Box getLeadingBox();
 	
 	/*
 	 * extracts next box. which box will be extracted depends on
 	 * what Chooser is used.
 	 */
 	public final Box extractNext() {
 		assert (_dbg_InitialAreaAddedCorrectly);
 		if (collection.size() < 1) {
 			return null;
 		}
 		Box b;
 		do {
 			b = chooser.extractNext();
 			// cnt++;
 		} while (b != null && screener.checkByValue(b) == false); // we do not clean the list all the time but we do not return bad boxes
 		// TODO: if(cnt>CNT && b!=null) cleanList();															
 		return b;
 	}
 	
 	/*
 	 * adds a bunch of boxes.
 	 * current implementation just calls
 	 * @add(Box)@ for each element.
 	 * @add(Box)@ performs all the checks
 	 */
 	public final void add(Box[] newBoxes) {
 		for (Box b : newBoxes)
 			add(b); // add(Box) locks the list
 	}
 
 	/*
 	 * check input boxes with screener
 	 * and calls add_checked()
 	 */
 	public final void add(Box box) {
 		// do not use here something like "if(collection.size() == 0)" 
 		// because @addAllEges@ calls @add@ and the size is still equal to zero.
 		if (screener.checkPassed(box)) {
 			if (logging) System.out.println("     -> Box " + box.getFunctionValue() + " PASSED the screening.");
 			if(collection.size() == 0) { // this is the first box -- 
 				addAreaAndAllEges(box); 		// -- has to take care about border points
 										// ...
 										// actually it could be not the very first box
 										// but the list could be cleaned out.
 										// anyway lets play safe and do not add one more flag
 										// for this and trigger it in @clearAll@ and so on..
 			} else
 				addChecked(box);
 		} else {
 			if (logging) System.out.println("     -> Box " + box.getFunctionValue() + " FAILD the screening.");
 		}
 	}
 	/*
 	 * receives a new set of boxes and their estimation of the optimum
 	 * used by ParallelAlgorithm
 	 */
 	public void add(Box[] newBoxes, double minLo, double minHi) {
 		if (minLo > screener.getLowBoundMaxValue())
 			return; 					// they hasn't passed screening
 		if (this.size() == 0 || minHi < this.getOptimumValue().lo()) { // getOptimumValue() could be expensive on unsorted lists!
 			// we are an empty list or 		we haven't passed the screening
 			this.clearAll(minHi);
 			collection.addAll(Arrays.asList(newBoxes));
 		}
 		this.probeNewLowBoundMaxValueAndDoNotClean(minHi);
 		this.add(newBoxes);
 	}
 	public final Box extract(int n) {
 		Box b = extractInternal(n);
 		return b;
 	}
 	
 	protected Box extractInternal(int n) {
 		// default implementation
 		// we do not have a random access in Collection interface
 		// so, we have to use iterators.
 		// derived worklist can override this function for
 		// more effective work
 		Iterator<Box> it = collection.iterator();
 		Box b = null;
 		for (int i = 0; i < n-1; i++)
 			it.next();
 		b = it.next();
 		boolean success = remove(b);
 		assert(success);
 		return b;
 	}
 
 	public final boolean remove(Box toRemove) {
 		return collection.remove(toRemove);
 	}
 	public final int size() {
 		int i = collection.size();
 		return i;
 	}
 	
 	public final RealInterval getOptimumValue() {
 		if (collection.size() == 0)
 			return null;
 		double hiBorder = screener.getLowBoundMaxValue();
 		double loBorder = getLeadingBox().getFunctionValue().lo();
 		return new RealInterval(loBorder, hiBorder);
 	}
 	public final Box[] getOptimumArea() {
 		assert(collection.size() > 0);
 		removeRejectedBoxes();
 		assert(collection.size() > 0);
 		
 		//ArrayList<Box> opt = (ArrayList<Box>)list.clone();
 		Box[] opt = collection.toArray(new Box[collection.size()]);
 		
 		
 		// TODO: combine near intervals into one if possible!
 //		IntervalMerger merger = new IntervalMerger(opt);
 //		opt = merger.merge();
 		return opt;
 	}
 
 	
 	public void probeNewLowBoundMaxValue(double possibleNewMax) {
 		if (screener.probeNewLimit(possibleNewMax) )
 			if(isWorthScreening())
 				removeRejectedBoxes();
 	}
 	public void probeNewLowBoundMaxValueAndClean(double possibleNewMax) {
 		if (screener.probeNewLimit(possibleNewMax) )
 			removeRejectedBoxes();
 	}
 	public void probeNewLowBoundMaxValueAndDoNotClean(double possibleNewMax) {
 		screener.probeNewLimit(possibleNewMax);
 	}
 
 	/*
 	 * removes ALL boxes and reset screening-by-value threshold
 	 */
 	public void clearAll() {
 		clearAll(Double.MAX_VALUE);
 	}
 	private void clearAll(double threshold) {
 		collection.clear();
 		screener = new Screener(threshold);
 	}
 	public int removeRejectedBoxes() {
 		double threshold = screener.getLowBoundMaxValue();
 		int was = collection.size();
 		int removed = removeRejected2(threshold);
 		screener.resetStatistics();
 		
		if (logging) System.out.println("WorkList:  -- Cleaned. Was: " + was + ", removed: " + removed + 
 				", now: " + collection.size());
 		return removed;
 	}
 	// first variant of list cleaning.
 	// Do not call this function manually! Use @removeRejectedBoxes()@ instead
 	@SuppressWarnings("unused")
 	private int removeRejected1(double valueLimit) {	
 		int removedCount = 0;
 
 		Iterator<Box> it = collection.iterator(); Box b;
 		while(it.hasNext()) {
 	    	b = it.next();
 			if (b.getFunctionValue().lo() > valueLimit) {
 	    		it.remove();
 	    		removedCount++;
 	    	}
 	    }
 		
 		return removedCount;
 	}
 	// second variant of list cleaning implementation
 	// Do not call this function manually! Use @removeRejectedBoxes()@ instead
 	@SuppressWarnings("unused")
 	private int removeRejected2(double valueLimit) {	
 		int removedCount = 0;
 
 		HashSet<Box> toRemove = new HashSet<Box>();
 		for(Box b : collection) {
 	    	if (b.getFunctionValue().lo() > valueLimit) {
 	    		toRemove.add(b);
 				removedCount++;
 	    	}
 	    }
 		collection.removeAll(toRemove);
 		return removedCount;
 	}
 	// third variant of list cleaning
 	// Do not call this function manually! Use @removeRejectedBoxes()@ instead
 	@SuppressWarnings("unused")
 	private int removeRejected3(double valueLimit) {
 		int removedCount = 0;
 		// WorkList will use the old collection!
 		ArrayList<Box> newCollection = new ArrayList<Box>();
 		for(Box b : collection) {
 	    	if (b.getFunctionValue().lo() < valueLimit) {
 	    		newCollection.add(b);
 	    	}
 	    }
 		removedCount = collection.size() - newCollection.size();
 		collection = newCollection;
 		return removedCount;
 	}
 	private boolean isWorthScreening() {
 		long usedMem = Runtime.getRuntime().totalMemory();
 		if (usedMem > memoryThreshold)
 			return true;
 		if (this.size() < sizeThreshold)
 			return false;
 		if (screener.getValueLimitUpdatesCount() > updatesThreshold)
 			return true;
 //		if (screener.getLowBoundMaxValueLimitDelta() > collection.iterator().next().getFunctionValue().wid()/10) return true;
 		// some other heuristics 
 		//if() return true;
 		return false;			
 	}
 	
 	
 	public final void getWorkFrom(WorkList otherWL) {
 		System.out.println("WorkList::getWorkFrom() {{{");
 		double threshold = Math.min(this.getLowBoundMaxValue(), otherWL.getLowBoundMaxValue() );
 		assert(threshold <= this.getLowBoundMaxValue());
 		clearAll(threshold);
 		
 		Collection<Box> oCol = otherWL.collection;
 		Box b;
 		int otherSize = otherWL.size();
 		for (int i = 0; i < otherSize/2; i++) {
 			Iterator<Box> oI = oCol.iterator();
 			b = oI.next(); 
 			oI.remove();
 			collection.add(b);
 		}
 	
 		System.out.println("WorkList::getWorkFrom() otherWL has size " + 
 				otherSize + ", now this WL has size " + collection.size());
 		System.out.println("WorkList::getWorkFrom() }}}");
 	}
 	public double getLowBoundMaxValue() {
 		return screener.getLowBoundMaxValue();
 	}
 	public void switchOff1DerivativeCheck() {
 		screener.switchOff1DerivativeCheck();
 	}
 }
