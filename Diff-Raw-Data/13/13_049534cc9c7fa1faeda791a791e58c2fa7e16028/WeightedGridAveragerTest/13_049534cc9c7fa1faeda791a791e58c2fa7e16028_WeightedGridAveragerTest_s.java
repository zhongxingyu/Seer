 /**
  * WeightedGridAverager.java - test for the WeightedGridAverager class
  */
 package jeffraytracer.rendering.supersampling;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import jeffraytracer.Vector3D;
 
 import org.junit.Test;
 
 /**
  * Test for the WeightedGridAverager class.
  * 
  * @author Jeffrey Finkelstein <jeffrey.finkelstein@gmail.com>
  * @since Spring 2011
  */
 public class WeightedGridAveragerTest {
 
   /**
    * Test method for
    * {@link jeffraytracer.rendering.supersampling.WeightedGridAverager#average(jeffraytracer.Vector3D[])}
    * .
    */
   @Test
   public void testAverage() {
 
     Vector3D[] colors = new Vector3D[4];
     colors[0] = new Vector3D(0.2, 0.2, 0.2);
     colors[1] = new Vector3D(0.3, 0.3, 0.3);
     colors[2] = new Vector3D(0.4, 0.4, 0.4);
     colors[3] = new Vector3D(0.5, 0.5, 0.5);
 
     double[] weights = { 1, 2, 2, 3 };
 
     final WeightedGridAverager a = new WeightedGridAverager();
     a.setGridSize(2);
     a.setWeights(weights);
     final Vector3D[] averages = a.average(colors);
     assertEquals(1, averages.length);
    assertTrue(averages[0].equalTo(new Vector3D(1.55, 1.55, 1.55)));
 
     Vector3D[] colors2 = new Vector3D[8];
    double[] weights2 = new double[8];
     System.arraycopy(colors, 0, colors2, 0, 4);
     System.arraycopy(colors, 0, colors2, 4, 4);
    System.arraycopy(weights, 0, weights2, 0, 4);
    System.arraycopy(weights, 0, weights2, 4, 4);
    a.setWeights(weights2);
     final Vector3D[] averages2 = a.average(colors2);
     assertEquals(2, averages2.length);
    assertTrue(averages2[0].equalTo(new Vector3D(1.55, 1.55, 1.55)));
    assertTrue(averages2[1].equalTo(new Vector3D(1.55, 1.55, 1.55)));
 
   }
 
 }
