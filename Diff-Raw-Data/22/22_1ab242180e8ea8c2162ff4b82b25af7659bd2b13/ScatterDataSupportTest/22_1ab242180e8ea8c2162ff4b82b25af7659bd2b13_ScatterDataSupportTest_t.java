 package ch.opentrainingcenter.core.charts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.opentrainingcenter.transfer.ITrackPointProperty;
 import ch.opentrainingcenter.transfer.ITraining;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class ScatterDataSupportTest {
 
     private ITraining training;
     private List<ITrackPointProperty> trackpoints;
 
     @Before
     public void setUp() {
         training = mock(ITraining.class);
         trackpoints = new ArrayList<>();
         when(training.getTrackPoints()).thenReturn(trackpoints);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testNullPopulateHeartData() {
         ScatterDataSupport.populateHeartData(null);
     }
 
     public void testEmptyPopulateHeartData() {
         final float[][] result = ScatterDataSupport.populateHeartData(training);
         assertNotNull(result);
         assertTrue(result.length == 0);
     }
 
     @Test
     public void testSameValuePopulateHeartData() {
         trackpoints.add(createHeartBeat(180));
         trackpoints.add(createHeartBeat(180));
         trackpoints.add(createHeartBeat(180));
         final float[][] result = ScatterDataSupport.populateHeartData(training);
 
         assertEquals(2, result.length);
         assertEquals(3, result[1][0], 0.00001);
     }
 
     @Test
     public void testDifferentValuePopulateHeartData() {
         trackpoints.add(createHeartBeat(180));
         trackpoints.add(createHeartBeat(181));
         trackpoints.add(createHeartBeat(182));
         trackpoints.add(createHeartBeat(182));
         final float[][] result = ScatterDataSupport.populateHeartData(training);
 
         assertEquals(2, result.length);
         assertEquals(1, result[1][0], 0.00001);
        assertEquals(1, result[1][1], 0.00001);
        assertEquals(2, result[1][2], 0.00001);
     }
 
     @Test
     public void testDifferentIgnore_0_ValuePopulateHeartData() {
         trackpoints.add(createHeartBeat(180));
         trackpoints.add(createHeartBeat(181));
         trackpoints.add(createHeartBeat(182));
         trackpoints.add(createHeartBeat(182));
         trackpoints.add(createHeartBeat(0));
         final float[][] result = ScatterDataSupport.populateHeartData(training);
 
         assertEquals(2, result.length);
         assertEquals(1, result[1][0], 0.00001);
        assertEquals(1, result[1][1], 0.00001);
        assertEquals(2, result[1][2], 0.00001);
     }
 
     private ITrackPointProperty createHeartBeat(final int bpm) {
         final ITrackPointProperty pointA = mock(ITrackPointProperty.class);
         when(pointA.getHeartBeat()).thenReturn(bpm);
         return pointA;
     }
 
 }
