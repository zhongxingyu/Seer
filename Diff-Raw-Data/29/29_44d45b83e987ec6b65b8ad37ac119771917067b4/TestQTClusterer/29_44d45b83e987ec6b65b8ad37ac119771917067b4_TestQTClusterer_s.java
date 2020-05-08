 /*
  * Copyright (C) 2005-2007 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.cornell.med.icb.clustering;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * User: Fabien Campagne Date: Oct 2, 2005 Time: 6:59:35 PM To change this
  * template use File | Settings | File Templates.
  */
 public final class TestQTClusterer {
     /**
      * Used to log debug and informational messages.
      */
     private static final Logger LOGGER =
             Logger.getLogger(TestQTClusterer.class);
 
     /**
      * Default delta to use when comparing floating point values.
      */
     private static final double DELTA = 0.00001;
 
     /**
      * This test validates that each instance will be placed into it's own
      * cluster when there is no overlap between them.
      */
     @Test
     public void oneInstancePerCluster() {
         // put one instance in each cluster, total two instances
         final Clusterer clusterer = new QTClusterer(2);
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int instanceIndex,
                                            final int otherInstanceIndex) {
                         if (instanceIndex != otherInstanceIndex) {
                             return 100;
                         } else {
                             return 0;
                         }
                     }
                 };
 
         assertEquals(100d, distanceCalculator.distance(0, 1), DELTA);
         assertEquals(100d, distanceCalculator.distance(1, 0), DELTA);
         assertEquals(0d, distanceCalculator.distance(0, 0), DELTA);
         assertEquals(0d, distanceCalculator.distance(1, 1), DELTA);
 
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 2);
         assertNotNull(clusters);
         assertEquals(2, clusters.size());
         assertEquals(1, clusters.get(0).length);
         assertEquals(1, clusters.get(1).length);
         assertEquals(0, clusters.get(0)[0]);
         assertEquals(1, clusters.get(1)[0]);
 
     }
 
     @Test
     public void fourInstanceClusteringInOneCluster() {
         // put one instance in each cluster, total two instances
         final Clusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int i, final int j) {
                         // instances 0 and 1 belong to same cluster
                         if (i == 0 && j == 1 || i == 1 && j == 0) {
                             return 0;
                         } else {
                             return 10;
                         }
                     }
                 };
         // instances 0,1,2,3 go to cluster 1 (distance(0,1)=0; distance(2,0)=10<=threshold)
 
         assertEquals(0d, distanceCalculator.distance(0, 1), DELTA);
         assertEquals(0d, distanceCalculator.distance(1, 0), DELTA);
         assertEquals(10d, distanceCalculator.distance(0, 0), DELTA);
         assertEquals(10d, distanceCalculator.distance(1, 1), DELTA);
         assertEquals(10d, distanceCalculator.distance(0, 2), DELTA);
         assertEquals(10d, distanceCalculator.distance(2, 0), DELTA);
         assertEquals(10d, distanceCalculator.distance(2, 3), DELTA);
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 11);
         assertNotNull(clusters);
         assertEquals("Expected one cluster", 1, clusters.size());
         final int[] cluster = clusters.get(0);
         assertEquals("First cluster must have size 4", 4, cluster.length);
         assertTrue("Instance 0 in cluster 0", ArrayUtils.contains(cluster, 0));
         assertTrue("Instance 1 in cluster 0", ArrayUtils.contains(cluster, 1));
         assertTrue("Instance 2 in cluster 0", ArrayUtils.contains(cluster, 2));
         assertTrue("Instance 3 in cluster 0", ArrayUtils.contains(cluster, 3));
     }
 
     @Test
     public void fourInstanceClusteringInThreeClusters() {
         // put one instance in each cluster, total two instances
         final Clusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int i, final int j) {
                         // instances 0 and 1 belong to same cluster
                         if (i == 0 && j == 1 || i == 1 && j == 0) {
                             return 0;
                         } else {
                             return 11;
                         }
                     }
                 };
         assertEquals(0d, distanceCalculator.distance(0, 1), DELTA);
         assertEquals(0d, distanceCalculator.distance(1, 0), DELTA);
         assertEquals(11d, distanceCalculator.distance(0, 2), DELTA);
         assertEquals(11d, distanceCalculator.distance(2, 0), DELTA);
         assertEquals(11d, distanceCalculator.distance(2, 3), DELTA);
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 10);
         assertNotNull(clusters);
         assertEquals("Incorrect number of clusters", 3, clusters.size());
         assertEquals("First cluster must have size 2", 2,
                 clusters.get(0).length);
         assertEquals("Second cluster must have size 1", 1,
                 clusters.get(1).length);
         assertEquals("Third cluster must have size 1", 1,
                 clusters.get(2).length);
         assertEquals("Instance 0 in cluster 0", 0, clusters.get(0)[0]);
         assertEquals("Instance 1 in cluster 0", 1, clusters.get(0)[1]);
         assertEquals("Instance 2 in cluster 1", 2, clusters.get(1)[0]);
         assertEquals("Instance 3 in cluster 2", 3, clusters.get(2)[0]);
     }
 
     @Test
     public void fourInstanceClusteringInFourClusters() {
         // put one instance in each cluster, total two instances
         final QTClusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int i, final int j) {
                         // instances 0 and 1 belong to same cluster
                         if (i == 0 && j == 1 || i == 1 && j == 0) {
                             return 0;
                         } else {
                             return 10;
                         }
                     }
                 };
 
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 2);
         assertNotNull(clusters);
         assertEquals("Incorrect number of clusters", 3, clusters.size());
         assertEquals("First cluster must have size 2", 2,
                 clusters.get(0).length);
         assertEquals("Second cluster must have size 1", 1,
                 clusters.get(1).length);
         assertEquals("Third cluster must have size 1", 1,
                 clusters.get(2).length);
         assertEquals("Instance 0 in cluster 0", 0, clusters.get(0)[0]);
         assertEquals("Instance 1 in cluster 0", 1, clusters.get(0)[1]);
         assertEquals("Instance 2 in cluster 2", 2, clusters.get(1)[0]);
         assertEquals("Instance 3 in cluster 3", 3, clusters.get(2)[0]);
     }
 
     @Test
     public void zeroDistanceCalculator() {
         final Clusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int i, final int j) {
                         return 0;   // instances 0-3 belong to the same cluster
                     }
                 };
 
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 2);
         assertNotNull(clusters);
         assertEquals("Expected one cluster", 1, clusters.size());
         final int[] cluster = clusters.get(0);
         assertEquals("First cluster must have size 4", 4, cluster.length);
         assertTrue("Instance 0 in cluster 0", ArrayUtils.contains(cluster, 0));
         assertTrue("Instance 1 in cluster 0", ArrayUtils.contains(cluster, 1));
         assertTrue("Instance 2 in cluster 0", ArrayUtils.contains(cluster, 2));
         assertTrue("Instance 3 in cluster 0", ArrayUtils.contains(cluster, 3));
     }
 
     /**
      * This test validates that the clusterer will not throw any errors when
      * passed zero instances.
      */
     @Test
     public void zeroInstances() {
         final Clusterer clusterer = new QTClusterer(0);
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int instanceIndex,
                                            final int otherInstanceIndex) {
                         return Math.abs(instanceIndex - otherInstanceIndex);
                     }
                 };
 
         final List<int[]> result = clusterer.cluster(distanceCalculator, 0);
         assertNotNull(result);
         assertEquals(0, result.size());
     }
 
     /**
      * This test validates that the clusterer will not not allow a negative
      * instance count.
      */
     @Test(expected = IllegalArgumentException.class)
     public void illegalInstanceCount() {
         new QTClusterer(-1);
     }
 
     /**
      * This test validates that a dataset is clustered correctly using various
      * different values of thresholds.
      */
     @Test
     public void multipleThresholds() {
         // raw data to test
         final int[] data = {
                 1, 2, 3, 3, 2, 1, 42, 43, 4, 6
         };
 
         // list of expected results per threshold tested
         @SuppressWarnings("unchecked")
         final List<int[]>[] expectedResults = new List[6];
         // threshold = 0 ( each instance in it's own cluster )
         expectedResults[0] = new ArrayList<int[]>();
       
         // threshold = 0
         expectedResults[0] = new ArrayList<int[]>();
         expectedResults[0].add(new int[]{1, 1});
         expectedResults[0].add(new int[]{2, 2});
         expectedResults[0].add(new int[]{3, 3});
        expectedResults[0].add(new int[]{42});             
        expectedResults[0].add(new int[]{43});                   
         expectedResults[0].add(new int[]{4});
         expectedResults[0].add(new int[]{6});
 
         // threshold = 1
         expectedResults[1] = new ArrayList<int[]>();
         expectedResults[1].add(new int[]{1, 1, 2, 2});
         expectedResults[1].add(new int[]{3, 3, 4});
         expectedResults[1].add(new int[]{42, 43});
         expectedResults[1].add(new int[]{6});
 
         // threshold = 2
         expectedResults[2] = new ArrayList<int[]>();
         expectedResults[2].add(new int[]{1, 1, 2, 2, 3, 3});
         expectedResults[2].add(new int[]{42, 43});
         expectedResults[2].add(new int[]{4, 6});
 
         // threshold = 3
         expectedResults[3] = new ArrayList<int[]>();
         expectedResults[3].add(new int[]{1, 1, 2, 2, 3, 3, 4});
         expectedResults[3].add(new int[]{42, 43});
         expectedResults[3].add(new int[]{6});
 
         // threshold = 4 (same as 3)
         expectedResults[4] = new ArrayList<int[]>(expectedResults[3]);
 
         // threshold = 5
         expectedResults[5] = new ArrayList<int[]>();
         expectedResults[5].add(new int[]{1, 1, 2, 2, 3, 3, 4, 6});
         expectedResults[5].add(new int[]{42, 43});
 
 
         final Clusterer clusterer = new QTClusterer(data.length);
         // Distance function that deturns the difference between instances
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int i, final int j) {
                         return Math.abs(data[i] - data[j]);
                     }
                 };
 
         for (int i = 0; i <= 5; i++) {
             final List<int[]> clusters =
                     clusterer.cluster(distanceCalculator, i);
             assertNotNull("Cluster at threshold " + i, clusters);
 
             LOGGER.debug("Iterative clusters - threshold = " + i);
             final List<int[]> expectedCluster = expectedResults[i];
 
             int j = 0;
             for (final int[] cluster : clusters) {
                 // convert instance indexes from the cluster to data
                 final int[] result = new int[cluster.length];
                 for (int k = 0; k < result.length; k++) {
                     result[k] = data[cluster[k]];
                 }
                 LOGGER.debug(j + ":" + ArrayUtils.toString(result));
                 final int[] expectedResult = expectedCluster.get(j);
                 assertTrue("Cluster " + j + " with threshold " + i
                         + " does not match expected",
                         ArrayUtils.isEquals(expectedResult, result));
                 j++;
             }
         }
     }
 
     /**
     *
      */
     @Test
     public void clusterWordsInAString() {
         final String text = "Four score and seven years ago our fathers brought forth on this"
                 + " continent a new nation conceived in liberty and dedicated to the proposition"
                 + " that all men are created equal";
 
         final List<String[]> expectedResults = new ArrayList<String[]>();
         expectedResults.add(new String[] {"and","are","men","all","the","and","new","our","ago"});
         expectedResults.add(new String[] {"score","equal","forth","years","seven"});
         expectedResults.add(new String[] {"fathers","created","liberty","brought"});
         expectedResults.add(new String[] {"Four","that","this"});
         expectedResults.add(new String[] {"on","to","in"});
         expectedResults.add(new String[] {"continent","dedicated","conceived"});
         expectedResults.add(new String[] {"a"});
         expectedResults.add(new String[] {"nation"});
         expectedResults.add(new String[] {"proposition"});
 
         // break the text up into an array of indiviual words
         final String[] words = text.split(" ");
 
         // create a distance calculator that returns the difference in size between the two words
         final SimilarityDistanceCalculator distanceCalculator =
                 new MaxLinkageDistanceCalculator() {
                     @Override
                     public double distance(final int i, final int j) {
                         return Math.abs(words[i].length() - words[j].length());
                     }
                 };
 
         // and cluster the words into groups according to their size
         final Clusterer clusterer = new QTClusterer(words.length);
        final List<int[]> clusters = clusterer.cluster(distanceCalculator, 0.5);
 
         int j = 0;
         for (final int[] cluster : clusters) {
             // convert instance indexes from the cluster to source data
             final String[] result = new String[cluster.length];
             for (int k = 0; k < result.length; k++) {
                 result[k] = words[cluster[k]];
             }
             LOGGER.debug(ArrayUtils.toString(cluster));
             LOGGER.debug(ArrayUtils.toString(result));
             assertTrue("Cluster " + j + " does not match expected",
                     ArrayUtils.isEquals(expectedResults.get(j), result));
             j++;
         }
     }
 
 
     private interface Person {
     }
     private interface Place {
     }
     private interface Thing {
     }
 
     /**
      * Tests clustering with lists of object types.
      */
     @Test
     public void clusterObjectCollections() {
         final List<Object> peoplePlacesAndThings = new ArrayList<Object>();
         final Person tom = new Person() { };
         final Person dick = new Person() { };
         final Person harry = new Person() { };
 
         peoplePlacesAndThings.add(tom);
         peoplePlacesAndThings.add(dick);
         peoplePlacesAndThings.add(harry);
 
         final Place home = new Place() { };
         final Place work = new Place() { };
         final Place school = new Place() { };
 
         peoplePlacesAndThings.add(home);
         peoplePlacesAndThings.add(work);
         peoplePlacesAndThings.add(school);
 
         final Thing pencil = new Thing() { };
         final Thing pen = new Thing() { };
         final Thing paper = new Thing() { };
         final Thing stapler = new Thing() { };
 
         peoplePlacesAndThings.add(pencil);
         peoplePlacesAndThings.add(pen);
         peoplePlacesAndThings.add(paper);
         peoplePlacesAndThings.add(stapler);
 
         // put things in a random order just to make things interesting
         Collections.shuffle(peoplePlacesAndThings);
 
         final Clusterer clusterer = new QTClusterer(peoplePlacesAndThings.size());
         final List<int[]> clusters = clusterer.cluster(new MaxLinkageDistanceCalculator() {
             public double distance(final int i, final int j) {
                 final Object object1 = peoplePlacesAndThings.get(i);
                 final Object object2 = peoplePlacesAndThings.get(j);
                 if (object1 instanceof Person && object2 instanceof Person) {
                     return 0;
                 } else if (object1 instanceof Place && object2 instanceof Place) {
                     return 0;
                 } else if (object1 instanceof Thing && object2 instanceof Thing) {
                     return 0;
                 } else {
                     return 42;
                 }
             }
         }, 1.0f);
 
         assertNotNull("Cluster should not be null", clusters);
         assertEquals("There should be 3 clusters", 3, clusters.size());
 
         boolean peopleClustered = false;
         boolean placesClustered = false;
         boolean thingsClustered = false;
 
         for (final int[] cluster : clusters) {
             // check the type of the first, so we know what we're dealing with
             final Object object = peoplePlacesAndThings.get(cluster[0]);
             if (object instanceof Person) {
                 assertEquals("There should be 3 people", 3, cluster.length);
                 assertFalse("There appears to be more than one cluster of people", peopleClustered);
                 peopleClustered = true;
                 for (int i = 1; i < cluster.length; i++) {
                     final Object person = peoplePlacesAndThings.get(cluster[i]);
                     assertTrue("Cluster contains more than people", person instanceof Person);
                 }
             } else if (object instanceof Place) {
                 assertEquals("There should be 3 places", 3, cluster.length);
                 assertFalse("There appears to be more than one cluster of places", placesClustered);
                 placesClustered = true;
                 for (int i = 1; i < cluster.length; i++) {
                     final Object place = peoplePlacesAndThings.get(cluster[i]);
                     assertTrue("Cluster contains more than places", place instanceof Place);
                 }
             } else if (object instanceof Thing) {
                 assertEquals("There should be 4 things", 4, cluster.length);
                 assertFalse("There appears to be more than one cluster of things", thingsClustered);
                 thingsClustered = true;
                 for (int i = 1; i < cluster.length; i++) {
                     final Object thing = peoplePlacesAndThings.get(cluster[i]);
                     assertTrue("Cluster contains more than things", thing instanceof Thing);
                 }
             } else {
                 fail("Cluster contains an unknown object type: " + object.getClass().getName());
             }
         }
 
         assertTrue("People should have been clustered", peopleClustered);
         assertTrue("Places should have been clustered", placesClustered);
         assertTrue("Things should have been clustered", thingsClustered);
     }
 }
