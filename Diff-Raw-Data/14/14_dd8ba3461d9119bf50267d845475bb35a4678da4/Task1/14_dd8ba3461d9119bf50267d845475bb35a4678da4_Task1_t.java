 package org.coursera.algs4.tasks.week1.unionfind;
 
 import org.coursera.algs4.QuickFindAlgorithm;
 
/**
 * Question 1 (seed = 481358)<br> 
 * Give the id[] array that results from the following sequence of 6 union operations on a set of 10 items using the quick-find algorithm.<br><br> 
 * 
 * 9-8 5-7 0-2 3-9 1-8 5-0 <br><br>
 * 
 * Recall: our quick-find convention for the union operation p-q is to change id[p] (and perhaps some other entries) but not id[q]. 
 * 
 * @author e.sokolov
 *
 */
 public class Task1 {
 
 	/**
	 * @param args - ignored
 	 */
 	public static void main(String[] args) {
 		QuickFindAlgorithm qf = new QuickFindAlgorithm(10);
 		qf.union(9, 8);
 		qf.union(5, 7);
 		qf.union(0, 2);
 		qf.union(3, 9);
 		qf.union(1, 8);
 		qf.union(5, 0);
 		System.out.println(qf);
 	}
 
 }
