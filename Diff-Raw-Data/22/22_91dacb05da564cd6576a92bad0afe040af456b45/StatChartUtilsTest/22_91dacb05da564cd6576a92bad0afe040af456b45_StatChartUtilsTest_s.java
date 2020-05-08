 package com.ziodyne.sometrpg.screens.stats;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.badlogic.gdx.math.Vector2;
 import com.google.common.collect.Lists;
 
 public class StatChartUtilsTest {
   
   private static float epsilon = 0.00001f;
 
   static void assertEpsilonEquals(Vector2 expected, Vector2 actual, float epsilon) {
     if (!expected.epsilonEquals(actual, epsilon)) {
       fail();
     }
   }
   
   @Test
   public void test() {
     int radius = 5;
     List<Float> scalingFactors = Lists.newArrayList(0.5f, 0.5f);
     List<Vector2> vectors = StatChartUtils.getScaledChartVertices(scalingFactors, radius);
     assertEquals(scalingFactors.size(), vectors.size());
 
     
     Vector2 expectedFirst = new Vector2(0, 1).mul(radius*0.5f);
     Vector2 expectedSecond = new Vector2(0, -1).mul(radius*0.5f);
     
     Vector2 actualFirst = vectors.get(0);
     Vector2 actualSecond = vectors.get(1);
     
     assertEpsilonEquals(expectedFirst, actualFirst, epsilon);
     assertEpsilonEquals(expectedSecond, actualSecond, epsilon);
   }
   
   @Test
   public void testUnitCircleSampling() {
     int nSamples = 2;
     List<Vector2> samples = StatChartUtils.uniformSampleUnitCircle(nSamples);
     
     assertEquals(2, samples.size());
     
     Vector2 first = samples.get(0);
     Vector2 second = samples.get(1);
     
     Vector2 correctFirst = new Vector2(0, 1);
     Vector2 correctSecond = new Vector2(0, -1);
     
     assertEpsilonEquals(correctFirst, first, epsilon);
     assertEpsilonEquals(correctSecond, second, epsilon);
   }
 
 }
