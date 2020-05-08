 package se751.team13.quicksort;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 public class QuickSorterTask<T extends Comparable<? super T>> extends
 		QuickSorter implements Runnable {
 	private final List<T> unsorted;
 	private List<T> sortedList;
 	private CyclicBarrier barrier;
 
 	public QuickSorterTask(List<T> unsorted) {
 		this.unsorted = unsorted;
 	}
 
 	public QuickSorterTask(List<T> unsorted, CyclicBarrier barrier) {
 		this.unsorted = unsorted;
 		this.barrier = barrier;
 	}
 
 	@Override
 	public void run() {
 		sortedList = super.sort(unsorted);
 		if (barrier == null)
 			return;
 
 		try {
 			barrier.await();
 		} catch (InterruptedException | BrokenBarrierException e) {
 			return;
 		}
 	}
 
 	public int getSize() {
 		return sortedList.size();
 	}
 
 	public List<T> getSamples(int processors) {
 		if (sortedList == null) {
 			throw new NotSortedException("Not sorted");
 		}
 
 		List<T> samples = new ArrayList<T>();
 
 		for (int i = 0; i < processors; i++) {
 			samples.add(sortedList.get(i * sortedList.size() / (processors)));
 		}
 
 		return samples;
 	}
 
 	public List<List<T>> getSections(List<T> points) {
 		if (sortedList == null) {
 			throw new NotSortedException("Not sorted");
 		}
 		List<List<T>> sections = new ArrayList<List<T>>();
 
 		int currentPointIndex = 0;
 		int from = 0;
 		T point = points.get(currentPointIndex);
		boolean hasFreeElements = false; 	
 		for (int i = 0; i < sortedList.size(); i++) {
			if (sortedList.get(i).compareTo(point) == -1) {
				hasFreeElements = true;
			}else if(sortedList.get(i).compareTo(point) == 1) {
				hasFreeElements = false;
 				sections.add(new ArrayList<T>(sortedList.subList(from, i)));
 				from = i;
 				currentPointIndex += 1;
 				if (currentPointIndex >= points.size()) {
 					sections.add(new ArrayList<T>(sortedList.subList(from,
 							sortedList.size())));
 					break;
 				}
 
 				point = points.get(currentPointIndex);
 			}
 		}
		if(hasFreeElements){
			sections.add(new ArrayList<T>(sortedList.subList(from, sortedList.size()))); // Fix inorder bug
		}
 		int sum = 0;
 		for (List<T> a : sections) {
 			for (T b : a) {
 				sum++;
 			}
 		}
 		return sections;
 	}
 
 	public List<T> getSortedList() {
 		return sortedList;
 	}
 }
