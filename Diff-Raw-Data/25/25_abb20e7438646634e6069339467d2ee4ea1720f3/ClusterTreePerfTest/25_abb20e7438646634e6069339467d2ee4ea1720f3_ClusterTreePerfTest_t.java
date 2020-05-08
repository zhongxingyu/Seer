 /*
  * Copyright © 2012 Karel Rank All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *  Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  *  Redistributions in binary form must reproduce the above copyright notice, this
  *   list of conditions and the following disclaimer in the documentation and/or
  *   other materials provided with the distribution.
  *  Neither the name of Karel Rank nor the names of its contributors may be used to
  *   endorse or promote products derived from this software without specific prior
  *   written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package cz.rank.vsfs.mindex;
 
 import cz.rank.vsfs.mindex.util.Generators;
 import cz.rank.vsfs.mindex.util.PerfLogger;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Karel Rank
  */
 public class ClusterTreePerfTest {
     private static final Logger logger = LoggerFactory.getLogger(ClusterTreePerfTest.class);
     private static final int[] PIVOTS_COUNT = {10,
                                                20,
                                                30,
                                                50,
                                                100,
                                                200,
                                                400};
     private static final int[] CLUSTER_MAX_LEVEL = {2,
                                                     3,
                                                     4,
                                                     5,
                                                     10};
     private static final double[] RANGES = {0.01,
                                             0.05,
                                             0.15,
                                             0.3,
                                             0.5,
                                             1.0};
    public static final String MINDEX_REFERENCE_FILE = "mindex.reference.file";
     private final List<Vector> objects = new ArrayList<>();
     private double maximumDistance;
 
     @BeforeClass
     public void loadReferenceDataAndWarmUp() throws IOException {
        Path referenceDataPath = new File(System.getProperty(MINDEX_REFERENCE_FILE)).toPath();
 
        logger.info("Reading reference data from: " + referenceDataPath);
 
        final List<String> lines = Files.readAllLines(referenceDataPath, Charset.defaultCharset());
        logger.info("Read " + lines.size() + " lines");
        for (String line : lines) {
             parseLineAndCreateVector(line);
         }
 
         calculateMaximumDistance();
         warmUp();
     }
 
     private void calculateMaximumDistance() {
         logger.info("Calculating maximum distance...");
         maximumDistance = new MaximumDistance<>(objects).calculate();
         logger.info("Maximum distance is " + maximumDistance);
 
     }
 
     private void warmUp() {
         final Slf4JStopWatch stopWatch = new Slf4JStopWatch("WARM UP", PerfLogger.LOGGER);
         performTest(new TestParams(500, 3, 1, 0.15d), 1, stopWatch);
     }
 
     private void parseLineAndCreateVector(String line) {
         // Skip empty lines
         if (line.trim().isEmpty()) {
             return;
         }
 
         final String[] values = line.split(" ");
 
         objects.add(new Vector(convertStringsIntoDoubles(values)));
     }
 
     private List<Double> convertStringsIntoDoubles(String[] values) {
         List<Double> doubleValues = new ArrayList<>(values.length);
         for (String value : values) {
             doubleValues.add(Double.valueOf(value));
         }
 
         return doubleValues;
     }
 
     @DataProvider(name = "clusterTreeParams")
     public Object[][] clusterTreeParams() {
         List<TestParams[]> params = new ArrayList<>();
 
         for (Integer dimension : PIVOTS_COUNT) {
             for (Integer objectsCount : CLUSTER_MAX_LEVEL) {
                 for (Double range : RANGES) {
                     params.add(new TestParams[]{new TestParams(dimension, objectsCount, 20, range)});
                 }
             }
         }
         return params.toArray(new Object[params.size()][1]);
     }
 
     @Test(dataProvider = "clusterTreeParams")
     public void testClusterTree(TestParams params) {
         final Slf4JStopWatch stopWatch = new Slf4JStopWatch(params.toString(), PerfLogger.LOGGER);
         for (int i = 1; i < params.invocations + 1; i++) {
             performTest(params, i, stopWatch);
         }
     }
 
     private void performTest(TestParams params, int invocation, StopWatch stopWatch) {
 
         List<Pivot<Vector>> pivots = Generators.createPivots(objects.subList(0, params.pivotsCount));
 
         ClusterTree<Vector> clusterTree = new ClusterTree<>(params.clusterMaxLevel, 100, pivots, maximumDistance);
        stopWatch.start(params + ".build", Integer.toString(invocation));
         clusterTree.build();
        stopWatch.stop(params + ".build", Integer.toString(invocation));
 
         int emptyResults = 0;
        stopWatch.start(params + ".rangeQuery", Integer.toString(invocation));
         for (Vector queryObject : objects.subList(params.pivotsCount, params.pivotsCount + 1000)) {
             Collection<Vector> foundObjects = clusterTree.rangeQuery(queryObject, params.range);
 
             // Avoid dead code
             if (foundObjects.isEmpty()) {
                 emptyResults++;
             }
         }
        stopWatch.stop(params + ".rangeQuery", Integer.toString(invocation));
 
         logger.info("Empty results {}", emptyResults);
     }
 
     private static class TestParams {
         private final int pivotsCount;
         private final int clusterMaxLevel;
         private final int invocations;
         private final double range;
 
         public TestParams(int pivotsCount, int clusterMaxLevel, int invocations, double range) {
             this.pivotsCount = pivotsCount;
             this.clusterMaxLevel = clusterMaxLevel;
             this.invocations = invocations;
             this.range = range;
         }
 
         @Override
         public String toString() {
             final StringBuilder sb = new StringBuilder();
             sb.append("{pivotsCount=").append(pivotsCount);
             sb.append(", clusterMaxLevel=").append(clusterMaxLevel);
             sb.append(", range=").append(range);
             sb.append('}');
             return sb.toString();
         }
     }
 }
