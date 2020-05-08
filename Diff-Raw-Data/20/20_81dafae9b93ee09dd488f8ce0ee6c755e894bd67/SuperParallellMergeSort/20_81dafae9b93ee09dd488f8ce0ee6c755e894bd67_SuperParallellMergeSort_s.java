 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.karevoll.sorting.algorithms;
 
 import java.util.concurrent.Executor;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import no.karevoll.sorting.App;
 import no.karevoll.sorting.SortingAlgorithm;
 import no.karevoll.sorting.memory.Element;
 import no.karevoll.sorting.memory.MemoryArray;
 import no.karevoll.sorting.memory.MemoryManager;
 import no.karevoll.sorting.memory.MemorySlice;
 import no.karevoll.sorting.parallell.Counter;
 
 /**
  * 
  * @author berengal
  */
 public class SuperParallellMergeSort implements SortingAlgorithm {
 
     Executor executor;
     MemorySlice input, scratchMemory;
     Object done;
 
     @Override
     public void sort(MemoryArray input, MemoryManager memoryManager) {
 	this.input = new MemorySlice(input);
 	this.scratchMemory = new MemorySlice(memoryManager.allocate(input
 		.getSize(), "Scratch memory"));
 	executor = new ThreadPoolExecutor(App.THREAD_POOL_SIZE,
 		App.THREAD_POOL_SIZE, 0, TimeUnit.SECONDS,
 		new LinkedBlockingQueue<Runnable>());
 	done = new Object();
 	executor.execute(new Sorter(new MemorySlice(input), new MemorySlice(
 		scratchMemory), new Counter(1), new Runnable() {
 
 	    @Override
 	    public void run() {
 		synchronized (done) {
 		    done.notifyAll();
 		}
 	    }
 
 	}));
 	synchronized (done) {
 	    try {
 		done.wait();
 	    } catch (InterruptedException ex) {
 		ex.printStackTrace();
 	    }
 	}
	memoryManager.free(scratchMemory);
 	new MemorySlice(input).markSorted();
     }
 
     private class Sorter implements Runnable {
 
 	private final MemorySlice input, scratch;
 	private final Counter counter;
 	private final Runnable afterSort;
 
 	public Sorter(MemorySlice input, MemorySlice scratch, Counter counter,
 		Runnable afterSort) {
 	    this.input = input;
 	    this.scratch = scratch;
 	    this.counter = counter;
 	    this.afterSort = afterSort;
 	}
 
 	@Override
 	public void run() {
 	    if (input.getSize() <= 1) {
 		if (counter.dec()) {
 		    afterSort.run();
 		}
 		return;
 	    }
 
 	    int middle = input.getSize() / 2;
 	    Counter childCounter = new Counter(2);
 
 	    MemorySlice firstSlice = input.sliceLeft(middle);
 	    MemorySlice secondSlice = input.sliceRight(middle);
 
 	    MemorySlice firstScratch = scratch.sliceLeft(middle);
 	    MemorySlice secondScratch = scratch.sliceRight(middle);
 
 	    Runnable childrenDone = new Merger(firstSlice, secondSlice,
 		    scratch, new Counter(1), new Runnable() {
 
 			@Override
 			public void run() {
 			    moveScratchToInput();
 			    if (counter.dec()) {
 				executor.execute(afterSort);
 			    }
 			}
 
 		    });
 
 	    executor.execute(new Sorter(firstSlice, firstScratch, childCounter,
 		    childrenDone));
 	    executor.execute(new Sorter(secondSlice, secondScratch,
 		    childCounter, childrenDone));
 	}
 
 	private void moveScratchToInput() {
 	    for (int i = 0; i < input.getSize(); i++) {
 		input.insert(scratch.remove(i), i);
 	    }
 	}
 
     }
 
     class Merger implements Runnable {
 
 	private final MemorySlice firstList, secondList, scratch;
 	private final Counter counter;
 	private final Runnable afterMerge;
 
 	public Merger(MemorySlice firstList, MemorySlice secondList,
 		MemorySlice scratch, Counter counter, Runnable afterMerge) {
 	    this.firstList = firstList;
 	    this.secondList = secondList;
 	    this.scratch = scratch;
 	    this.counter = counter;
 	    this.afterMerge = afterMerge;
 	}
 
 	@Override
 	public void run() {
 	    if (firstList.getSize() < 10 || secondList.getSize() < 10) {
 		serialMerge();
 	    } else {
 		parallellMerge();
 	    }
 	}
 
 	private void serialMerge() {
 	    int i = 0, j = 0;
 
 	    while (i < firstList.getSize() && j < secondList.getSize()) {
 		Element first = firstList.read(i);
 		Element second = secondList.read(j);
 		if (first.compareTo(second) < 0) {
 		    scratch.insert(firstList.remove(i), i + j);
 		    i++;
 		} else {
 		    scratch.insert(secondList.remove(j), i + j);
 		    j++;
 		}
 	    }
 
 	    while (i < firstList.getSize()) {
 		scratch.insert(firstList.remove(i), i + j);
 		i++;
 	    }
 	    while (j < secondList.getSize()) {
 		scratch.insert(secondList.remove(j), i + j);
 		j++;
 	    }
 
 	    if (counter.dec()) {
 		executor.execute(afterMerge);
 	    }
 	}
 
 	private void parallellMerge() {
 	    int firstMiddle = firstList.getSize() / 2;
 	    int secondMiddle = binarySearch(secondList, firstList
 		    .read(firstMiddle));
 	    MemorySlice lowerFirstList = firstList.sliceLeft(firstMiddle);
 	    MemorySlice higherFirstList = firstList.sliceRight(firstMiddle);
 	    MemorySlice lowerSecondList = secondList.sliceLeft(secondMiddle);
 	    MemorySlice higherSecondList = secondList.sliceRight(secondMiddle);
 	    int lowerScratchSize = lowerFirstList.getSize()
 		    + lowerSecondList.getSize();
 	    MemorySlice lowerScratch = scratch.sliceLeft(lowerScratchSize);
 	    MemorySlice higherScratch = scratch.sliceRight(lowerScratchSize);
 
 	    Runnable childrenDone = new Runnable() {
 
 		@Override
 		public void run() {
 		    if (counter.dec()) {
 			executor.execute(afterMerge);
 		    }
 		}
 
 	    };
 	    Counter mergeCounter = new Counter(2);
 	    executor.execute(new Merger(lowerFirstList, lowerSecondList,
 		    lowerScratch, mergeCounter, childrenDone));
 	    executor.execute(new Merger(higherFirstList, higherSecondList,
 		    higherScratch, mergeCounter, childrenDone));
 	}
 
 	private int binarySearch(MemorySlice array, Element find) {
 	    int start = 0, end = array.getSize();
	    while (end - start > 1) {
 		int middle = ((end - start) / 2) + start;
 		int cmp = array.read(middle).compareTo(find);
 		switch (cmp) {
 		case 0:
 		    return middle;
 		case -1:
 		    start = middle + 1;
 		    break;
 		case 1:
		    end = middle;
 		    break;
 		}
 	    }
	    if (start < array.getSize() - 1
		    && array.read(start).compareTo(find) == -1) {
		return start + 1;
	    } else if (start >= array.getSize()) {
		return array.getSize() - 1;
	    } else {
		return start;
 	    }
 	}
 
     }
 }
