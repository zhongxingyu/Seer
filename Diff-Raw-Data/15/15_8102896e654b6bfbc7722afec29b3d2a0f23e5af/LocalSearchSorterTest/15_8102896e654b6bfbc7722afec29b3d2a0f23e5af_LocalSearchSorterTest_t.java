 package de.mh4j.examples.solver;
 
 import org.testng.annotations.Test;
 
 public class LocalSearchSorterTest {
     @Test
     public void testCreateInitialSolution() {
         LocalSearchSorter sorter = new LocalSearchSorter(10);
         sorter.createInitialSolution();
 
         /*
          * if the solver could create any solution without throwing an exception
          * this test succeeded. (Sorting class handles its validity by itself)
          */
     }
 }
