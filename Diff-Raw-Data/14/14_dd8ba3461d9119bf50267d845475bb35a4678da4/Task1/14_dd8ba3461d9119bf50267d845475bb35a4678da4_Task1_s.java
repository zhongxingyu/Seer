 package org.coursera.algs4.tasks.week1.unionfind;
 
 import org.coursera.algs4.QuickFindAlgorithm;
 
 public class Task1 {
 
 	/**
	 * @param args
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
