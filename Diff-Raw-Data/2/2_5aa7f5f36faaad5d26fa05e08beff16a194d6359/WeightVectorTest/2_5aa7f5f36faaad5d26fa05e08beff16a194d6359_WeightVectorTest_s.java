 package myorg.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.Assert.*;
 
 import static org.junit.Assert.assertEquals;
 
 import myorg.io.WeightVector;
 
 public class WeightVectorTest {
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
  
     @Before
     public void setUp() throws Exception {
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testToString() throws IOException {
 
         int dim = 16 * 1024;
         double epsilon = 1e-5;
         Random random = new Random();
 
         WeightVector w1 = new WeightVector(dim);
 
         for (int i = 0; i < 1000; i++) {
             int k = Math.abs(random.nextInt()) % dim;
             float v = random.nextFloat();
             w1.setValue(k, v);
         }
 
         WeightVector w2 = new WeightVector(w1.toString());
 
        assertEquals(w1.getDimensions(), w2.getDimensions());
 
         for (int i = 0; i < w2.getDimensions(); i++) {
             assertEquals(w1.getValue(i), w2.getValue(i), epsilon);
         }
     }
 
     @Test
     public void testScale() throws IOException {
 
         int dim = 16 * 1024;
         double epsilon = 1e-5;
         Random random = new Random();
 
         WeightVector w1 = new WeightVector(dim);
         WeightVector w2 = new WeightVector(dim);
         WeightVector w3 = new WeightVector(dim);
         float scaleFactor = random.nextFloat();
 
         for (int i = 0; i < 1000; i++) {
             int k = Math.abs(random.nextInt()) % dim;
             float v = random.nextFloat();
             w1.setValue(k, v);
             w2.setValue(k, v * scaleFactor);
             w3.setValue(k, v);
         }
 
         w3.scale(scaleFactor);
 
         assertEquals(w1.getDimensions(), w2.getDimensions());
 
         for (int i = 0; i < w2.getDimensions(); i++) {
             assertEquals(w1.getValue(i) * scaleFactor, w2.getValue(i), epsilon);
             assertEquals(w1.getValue(i) * scaleFactor, w3.getValue(i), epsilon);
         }
     }
 
     @Test
     public void testAddVector() throws IOException {
 
         int dim1 = 16 * 1024;
         int dim2 = 32 * 1024;
         double epsilon = 1e-5;
         Random random = new Random();
 
         WeightVector w1 = new WeightVector(dim1);
         WeightVector w2 = new WeightVector(dim1);
         WeightVector w3 = new WeightVector(dim2);
 
         for (int i = 0; i < 1000; i++) {
             int k = Math.abs(random.nextInt()) % dim1;
             float v = random.nextFloat();
             w1.setValue(k, v);
 
             k = Math.abs(random.nextInt()) % dim1;
             v = random.nextFloat();
             w2.setValue(k, v);
 
             k = Math.abs(random.nextInt()) % dim2;
             v = random.nextFloat();
             w3.setValue(k, v);
         }
 
         WeightVector w1c = new WeightVector(w1.toString());
         WeightVector w2c = new WeightVector(w2.toString());
 
         float xScale = random.nextFloat(); 
         w1.addVector(w2, xScale);
 
         assertEquals(dim1, w1.getDimensions());
 
         for (int i = 0; i < w1.getDimensions(); i++) {
             float v = w1c.getValue(i) + xScale * w2.getValue(i);
             assertEquals(w1.getValue(i), v, epsilon);
         }
 
         assertEquals(dim1, w2.getDimensions());
 
         w2.addVector(w3, xScale);
 
         assertEquals(dim2, w2.getDimensions());
 
         for (int i = 0; i < w2.getDimensions(); i++) {
             float v = xScale * w3.getValue(i);
             if (i < w2c.getDimensions()) {
                 v += w2c.getValue(i);
             }
             assertEquals(w2.getValue(i), v, epsilon);
         }
 
     }
 }
 
 
