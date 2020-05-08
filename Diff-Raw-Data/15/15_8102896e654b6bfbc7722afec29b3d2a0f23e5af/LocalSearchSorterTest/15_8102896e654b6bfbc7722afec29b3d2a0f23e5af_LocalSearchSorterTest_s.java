 package de.mh4j.examples.solver;
 
 import org.testng.annotations.Test;
 
import de.mh4j.examples.Sorting;

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

    @Test
    public void testCreateRandomNeighbor() {
        LocalSearchSorter sorter = new LocalSearchSorter(10);
        // invoke step to initialize the solver
        sorter.step();

        Sorting initialSorting = sorter.getCurrentSolution();
        Sorting neighbor = sorter.createRandomNeighbor();

        assert initialSorting.equals(neighbor) == false;
    }
 }
