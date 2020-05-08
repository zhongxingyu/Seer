 package cs437.som.topology;
 
 import cs437.som.Dimension;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 public class ToroidalEuclideanSquareTest {
     private static final Dimension dimension = new Dimension(3, 3);
     private static final ToroidalEuclideanSquare grid = new ToroidalEuclideanSquare();
     private static final double MAX_DISTANCE = 0.001;
     private static final double ROOT_2 = 1.414;
 
     @BeforeMethod
     public void setUp() throws Exception {
         grid.setNeuronCount(dimension);
     }
 
     @Test
     public void testGridDistance() throws Exception {
         // Normal Euclidean operation
         assertEquals(grid.gridDistance(4, 4), 0, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 1), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 3), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 5), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 7), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 0), ROOT_2, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 2), ROOT_2, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 6), ROOT_2, MAX_DISTANCE);
         assertEquals(grid.gridDistance(4, 8), ROOT_2, MAX_DISTANCE);
 
         // Test toroidal distances
         assertEquals(grid.gridDistance(0, 0), 0, MAX_DISTANCE);
         assertEquals(grid.gridDistance(0, 1), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(0, 3), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(0, 2), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(0, 6), 1, MAX_DISTANCE);
 
         assertEquals(grid.gridDistance(8, 8), 0, MAX_DISTANCE);
         assertEquals(grid.gridDistance(8, 2), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(8, 5), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(8, 6), 1, MAX_DISTANCE);
         assertEquals(grid.gridDistance(8, 7), 1, MAX_DISTANCE);

     }
 }
