 package uk.ac.susx.mlcl.erl.tac.eval;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Assume;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import uk.ac.susx.mlcl.erl.reduce.Reducers;
 import uk.ac.susx.mlcl.erl.test.AbstractTest;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
 import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
 import static org.junit.Assert.*;
 
 
 interface Config {
 
     long[][] getContingencies();
 
     String[] getLabels();
 
     int getSize();
 
     long[][][] getAllVsOnes();
 
 }
 
 @RunWith(Parameterized.class)
 public class ConfusionMatrixTest extends AbstractTest implements Config {
 
     private static final Log LOG = LogFactory.getLog(ConfusionMatrixTest.class);
     private static final double EPSILON = 0.0000000001;
     private final Config config;
 
     public ConfusionMatrixTest(Config config) {
         this.config = config;
     }
 
     @Parameterized.Parameters(name = "{index}: {0}")
     public static Collection<Object[]> data() {
         ImmutableList.Builder<Object[]> data = ImmutableList.builder();
         data.add(new Object[]{new Config() {
             @Override
             public long[][] getContingencies() {
                 return new long[][]{
                         {5, 3, 0},
                         {2, 3, 1},
                         {0, 2, 11}
                 };
             }
 
             @Override
             public String[] getLabels() {
                 return new String[]{"Cat", "Dog", "Rabbit"};
             }
 
             @Override
             public int getSize() {
                 return 3;
             }
 
             @Override
             public long[][][] getAllVsOnes() {
                 return new long[][][]{
                         {
                                 {5, 3},
                                 {2, 17},
                         },
                         {
                                 {3, 3},
                                 {5, 16},
                         },
                         {
                                 {11, 2},
                                 {1, 13}
                         },
                 };
             }
 
 
         }});
         data.add(new Object[]{new Config() {
             @Override
             public long[][] getContingencies() {
                 return new long[][]{
                         {15, 10, 100},
                         {10, 15, 10},
                         {0, 100, 1000}
                 };
             }
 
             @Override
             public String[] getLabels() {
                 return new String[]{"Pos", "Neg", "Neutral"};
             }
 
             @Override
             public int getSize() {
                 return 3;
             }
 
             @Override
             public long[][][] getAllVsOnes() {
                 return new long[][][]{
                         {
                                 {15, 110},
                                 {10, 1125},
                         },
                         {
                                 {15, 20},
                                 {110, 1115},
                         },
                         {
                                 {1000, 100},
                                 {110, 50}
                         },
                 };
             }
 
         }});
         data.add(new Object[]{new Config() {
             @Override
             public long[][] getContingencies() {
                 return new long[][]{
                         {0, 0, 0, 0},
                         {0, 0, 0, 0},
                         {0, 0, 0, 0},
                         {0, 0, 0, 0}
                 };
             }
 
             @Override
             public String[] getLabels() {
                 return new String[]{"A", "B", "C", "D"};
             }
 
             @Override
             public int getSize() {
                 return 4;
             }
 
             @Override
             public long[][][] getAllVsOnes() {
                 return new long[][][]{
                         {
                                 {0, 0, 0},
                                 {0, 0, 0},
                                 {0, 0, 0},
                         },
                         {
                                 {0, 0, 0},
                                 {0, 0, 0},
                                 {0, 0, 0},
                         },
                         {
                                 {0, 0, 0},
                                 {0, 0, 0},
                                 {0, 0, 0},
                         },
                         {
                                 {0, 0, 0},
                                 {0, 0, 0},
                                 {0, 0, 0},
                         },
 
                 };
             }
 
         }});
         data.add(new Object[]{new Config() {
             @Override
             public long[][] getContingencies() {
                 return new long[][]{
                         {0, 10000},
                         {10000000, 0},
                 };
             }
 
             @Override
             public String[] getLabels() {
                 return new String[]{"X", "Y"};
             }
 
             @Override
             public int getSize() {
                 return 2;
             }
 
             @Override
             public long[][][] getAllVsOnes() {
                 return new long[][][]{
                         {
                                 {0, 10000},
                                 {10000000, 0},
                         },
                         {
                                 {0, 10000000},
                                 {10000, 0},
                         },
                 };
             }
 
         }});
         data.add(new Object[]{new Config() {
             @Override
             public long[][] getContingencies() {
                 return new long[][]{
                         {1},
                 };
             }
 
             @Override
             public String[] getLabels() {
                 return new String[]{"X"};
             }
 
             @Override
             public int getSize() {
                 return 1;
             }
 
             @Override
             public long[][][] getAllVsOnes() {
                 return new long[][][]{
                         {
                                 {1, 0},
                                 {0, 0},
                         },
                         {
                                 {0, 0},
                                 {0, 1},
                         },
                 };
             }
 
         }});
         data.add(new Object[]{new Config() {
             @Override
             public long[][] getContingencies() {
                 return new long[][]{};
             }
 
             @Override
             public String[] getLabels() {
                 return new String[]{};
             }
 
             @Override
             public int getSize() {
                 return 0;
             }
 
             @Override
             public long[][][] getAllVsOnes() {
                 return new long[][][]{
                 };
             }
 
         }});
         return data.build();
     }
 
     @Override
     public long[][] getContingencies() {
         return config.getContingencies();
     }
 
     @Override
     public String[] getLabels() {
         return config.getLabels();
     }
 
     @Override
     public int getSize() {
         return config.getSize();
     }
 
     @Override
     public long[][][] getAllVsOnes() {
         return config.getAllVsOnes();
     }
 
     public ConfusionMatrix<String> getInstance() {
         return ConcreteLongArrayConfusionMatrix.newInstance(getContingencies(), getLabels());
     }
 
     @Test
     public void testSize() {
         final ConfusionMatrix<String> mat = getInstance();
         assertEquals("size", getSize(), mat.size());
     }
 
     @Test
     public void testGetCount() {
         final ConfusionMatrix<String> mat = getInstance();
         for (int y = 0; y < getSize(); y++) {
             for (int x = 0; x < getSize(); x++) {
                 long expectedCount = getContingencies()[y][x];
                 long actualCount = mat.getCount(getLabels()[y], getLabels()[x]);
                 assertEquals(String.format("getCount(%s,%s)", getLabels()[y], getLabels()[x]),
                         expectedCount, actualCount);
                 assertThat(actualCount, is(greaterThanOrEqualTo(0l)));
                 assertThat(actualCount, is(lessThanOrEqualTo(mat.getGrandTotal())));
             }
         }
     }
 
     @Test
     public void testGetLabels() {
         final ConfusionMatrix<String> mat = getInstance();
         final List<String> actualLabels = mat.getLabels();
         assertEquals("getLabels", Lists.newArrayList(getLabels()), actualLabels);
     }
 
     @Test
     public void testGetTrueCount() {
         final ConfusionMatrix<String> mat = getInstance();
 
         long expectedTrueCount = 0;
         for (int i = 0; i < getSize(); i++)
             expectedTrueCount += getContingencies()[i][i];
         final long actualTrueCount = mat.getTrueCount();
 
         assertEquals("getTrueCount", expectedTrueCount, actualTrueCount);
         assertThat(actualTrueCount, is(greaterThanOrEqualTo(0l)));
         assertThat(actualTrueCount, is(lessThanOrEqualTo(mat.getGrandTotal())));
     }
 
     @Test
     public void testGetFalseCount() {
         final ConfusionMatrix<String> mat = getInstance();
 
         long expectedFalseCount = 0;
         for (int y = 0; y < getSize(); y++) {
             for (int x = 0; x < getSize(); x++)
                 if (y != x)
                     expectedFalseCount += getContingencies()[y][x];
         }
         final long actualFalseCount = mat.getFalseCount();
 
         assertEquals("getFalseCount", expectedFalseCount, actualFalseCount);
         assertThat(actualFalseCount, is(greaterThanOrEqualTo(0l)));
         assertThat(actualFalseCount, is(lessThanOrEqualTo(mat.getGrandTotal())));
     }
 
     @Test
     public void testGetTrueCountFor() {
         final ConfusionMatrix<String> mat = getInstance();
 
         for (int i = 0; i < getSize(); i++) {
             final String label = getLabels()[i];
             final long expectedTrueCount = getContingencies()[i][i];
             final long actualTrueCount = mat.getTrueCountFor(label);
             assertEquals(String.format("getTrueCountFor(%s)", label),
                     expectedTrueCount, actualTrueCount);
             assertThat(actualTrueCount, is(greaterThanOrEqualTo(0l)));
             assertThat(actualTrueCount, is(lessThanOrEqualTo(mat.getTrueCount())));
         }
     }
 
     @Test
     public void testGetFalseCountFor() {
         final ConfusionMatrix<String> mat = getInstance();
 
         for (int x = 0; x < getSize(); x++) {
             final String label = getLabels()[x];
             long expectedFalseCount = 0;
             for (int y = 0; y < getSize(); y++) {
                 if (x != y) {
                     expectedFalseCount += getContingencies()[y][x];
                 }
             }
             final long actualFalseCount = mat.getFalseCountFor(label);
             assertEquals(String.format("getFalseCountFor(%s)", label),
                     expectedFalseCount, actualFalseCount);
             assertThat(actualFalseCount, is(greaterThanOrEqualTo(0l)));
             assertThat(actualFalseCount, is(lessThanOrEqualTo(mat.getFalseCount())));
         }
     }
 
     @Test
     public void getGrandTotal() {
         final ConfusionMatrix<String> mat = getInstance();
 
         long expectedGrandTotal = 0;
         for (int x = 0; x < getSize(); x++)
             for (int y = 0; y < getSize(); y++)
                 expectedGrandTotal += getContingencies()[y][x];
 
         final long actualGrandTotal = mat.getGrandTotal();
         assertEquals("getGrandTotal", expectedGrandTotal, actualGrandTotal);
         assertThat(actualGrandTotal, is(greaterThanOrEqualTo(0l)));
     }
 
     @Test
     public void testGetActualCountFor() {
         final ConfusionMatrix<String> mat = getInstance();
 
         for (int y = 0; y < getSize(); y++) {
             final String label = getLabels()[y];
 
             long expected = 0;
             for (int x = 0; x < getSize(); x++)
                 expected += getContingencies()[y][x];
 
             long actualActualCount = mat.getActualCountFor(label);
             assertEquals(String.format("getActualCountFor(%s)", label),
                     expected, actualActualCount);
             assertThat(actualActualCount, is(greaterThanOrEqualTo(0l)));
             assertThat(actualActualCount, is(lessThanOrEqualTo(mat.getGrandTotal())));
         }
     }
 
     @Test
     public void testGetPredictedCountFor() {
         final ConfusionMatrix<String> mat = getInstance();
 
         for (int x = 0; x < getSize(); x++) {
             final String label = getLabels()[x];
 
             long expected = 0;
             for (int y = 0; y < getSize(); y++)
                 expected += getContingencies()[y][x];
 
             long actualPredictedCount = mat.getPredictedCountFor(label);
             assertEquals(String.format("getPredictedCountFor(%s)", label), expected, actualPredictedCount);
             assertThat(actualPredictedCount, is(greaterThanOrEqualTo(0l)));
             assertThat(actualPredictedCount, is(lessThanOrEqualTo(mat.getGrandTotal())));
         }
     }
 
     @Test
     public void testAccuracy() {
         final ConfusionMatrix<String> mat = getInstance();
 
         long nom = 0, denom = 0;
         for (int y = 0; y < getSize(); y++) {
             nom += getContingencies()[y][y];
             for (int x = 0; x < getSize(); x++)
                 denom += getContingencies()[y][x];
         }
         final double expectedAccuracy = nom == 0 ? 0 : nom / (double) denom;
         final double actualAccuracy = mat.getAccuracy();
 
         assertEquals("getAccuracy", expectedAccuracy, actualAccuracy, EPSILON);
         assertThat(actualAccuracy, is(greaterThanOrEqualTo(0d)));
         assertThat(actualAccuracy, is(lessThanOrEqualTo(1d)));
     }
 
     @Test
     public void testPrecision() {
         final ConfusionMatrix<String> mat = getInstance();
 
         for (int x = 0; x < getSize(); x++) {
             final String label = getLabels()[x];
             long tp = getContingencies()[x][x];
             long fp = 0;
             for (int y = 0; y < getSize(); y++) {
                 if (x != y)
                     fp += getContingencies()[y][x];
             }
             double expectedPrecision = tp == 0 ? 0 : (double) tp / (double) (tp + fp);
             double actualPrecision = mat.getPredictiveValueFor(label);
             LOG.debug(String.format("Precision(%s) = %d/(%<d+%d) = %f", label, tp, fp, actualPrecision));
             assertEquals(String.format("precision of %s", label), expectedPrecision, actualPrecision, EPSILON);
             assertThat(actualPrecision, is(greaterThanOrEqualTo(0d)));
             assertThat(actualPrecision, is(lessThanOrEqualTo(1d)));
         }
     }
 
     @Test
     public void testRecall() {
         final ConfusionMatrix<String> mat = getInstance();
 
         for (int y = 0; y < getSize(); y++) {
             final String label = getLabels()[y];
             long tp = getContingencies()[y][y];
             long fn = 0;
             for (int x = 0; x < getSize(); x++) {
                 if (y != x)
                     fn += getContingencies()[y][x];
             }
             double expectedRecall = tp == 0 ? 0 : (double) tp / (double) (tp + fn);
             double actualRecall = mat.getTrueRateFor(label);
             LOG.debug(String.format("Recall(%s) = %d/(%<d+%d) = %f", label, tp, fn, actualRecall));
             assertEquals(String.format("recall of %s", label), expectedRecall, actualRecall, EPSILON);
             assertThat(actualRecall, is(greaterThanOrEqualTo(0d)));
             assertThat(actualRecall, is(lessThanOrEqualTo(1d)));
         }
     }
 
     @Test
     public void testFScore() {
         final double[] betas = {0, 0.1, 0.5, 1, 2, 10, Double.POSITIVE_INFINITY};
         final ConfusionMatrix<String> mat = getInstance();
 
         for (double beta : betas) {
             for (int i = 0; i < getSize(); i++) {
                 final String label = getLabels()[i];
                 long tp = getContingencies()[i][i];
                 long fp = 0;
                 long fn = 0;
                 for (int y = 0; y < getSize(); y++) {
                     if (i != y)
                         fp += getContingencies()[y][i];
                 }
                 for (int x = 0; x < getSize(); x++) {
                     if (i != x)
                         fn += getContingencies()[i][x];
                 }
 
                 double precision = tp == 0 ? 0 : (double) tp / (double) (tp + fp);
                 double recall = tp == 0 ? 0 : (double) tp / (double) (tp + fn);
                 final double expectedF1Score;
                 if (beta == 0) {
                     expectedF1Score = precision;
                 } else if (beta == Double.POSITIVE_INFINITY) {
                     expectedF1Score = recall;
                 } else if (precision == 0 || recall == 0) {
                     expectedF1Score = 0;
                 } else {
                     double betaSquared = Math.pow(beta, 2);
                     expectedF1Score = (1 + betaSquared) * precision * recall / (betaSquared * precision + recall);
                 }
 
                 double actualFScore = mat.getFScoreFor(label, beta);
                 LOG.debug(String.format("F(%s;ß=%.2f) = %f", label, beta, actualFScore));
                 assertEquals(String.format("F(%s;ß=%.2f) = %f", label, beta, actualFScore),
                         expectedF1Score, actualFScore, EPSILON);
 
                 // irrespective of beta the fscore should be be in the range precision to recall (or reversed)
                 assertThat(String.format("F(%s;ß=%.2f) < min(p,r)", label, beta),
                         actualFScore, is(greaterThanOrEqualTo(Math.min(precision, recall))));
                 assertThat(String.format("F(%s;ß=%.2f) > max(p,r)", label, beta),
                         actualFScore, is(lessThanOrEqualTo(Math.max(precision, recall))));
             }
         }
     }
 
     @Test
     public void testGetTableString() {
         final ConfusionMatrix<String> mat = getInstance();
         final String tableString = mat.getTableString();
         LOG.info(tableString);
 
         assertEquals("table size", getSize() + 1, tableString.split("\n").length);
 
         // Reparse the whole table string and test each element matches the input data.
         final String[] rows = tableString.split("\n");
         for (int y = 0; y <= getSize(); y++) {
             final String row = rows[y].trim();
             final String[] cols = row.split("\\s+");
             LOG.debug(Arrays.toString(cols));
             if (y == 0) {
                 for (int x = 0; x < getSize(); x++) {
                     assertEquals("Column label mismatch for column " + x, getLabels()[x], cols[x]);
                 }
             } else {
                 assertEquals("Row label mismatch for row " + y, getLabels()[y - 1], cols[0]);
                 for (int x = 1; x <= getSize(); x++) {
                     assertEquals("Result value mismatch for cell " + y + "," + x,
                             getContingencies()[y - 1][x - 1], Long.parseLong(cols[x]));
                 }
 
             }
         }
     }
 
     @Test
     public void testAppendStats() {
         final ConfusionMatrix<String> mat = getInstance();
         final String statsString = mat.getStatsString();
         LOG.info(statsString);
         assertNotNull("statsString", statsString);
     }
 
     @Test
     public void testAppendStatsFor() {
         final ConfusionMatrix<String> mat = getInstance();
         for (String label : getLabels()) {
             final String statsString = mat.getStatsStringFor(label);
             LOG.info(String.format("Stats for %s:%n%s", label, statsString));
             assertNotNull("statsString", statsString);
         }
     }
 
     @Test
     public void testAllVsOne() {
         final ConfusionMatrix<String> mat = getInstance();
 
         Assume.assumeThat("All-vs-one binary matrices don't make sense for matrices smaller than 2",
                 mat.size(), is(greaterThanOrEqualTo(2)));
 
         final double beta = 1.0;
         for (int i = 0; i < getLabels().length; i++) {
             final String label = getLabels()[i];
             long[][] av1 = getAllVsOnes()[i];
 
            final BinaryConfusionMatrix<String> binmat = mat.mapAllVersusOne(label, Reducers.Doubles.sum());
             LOG.debug(String.format("Stats for %s%n%s%s",
                     label,
                     binmat.getTableString(),
                     binmat.getStatsString()));
 
             assertEquals(av1[0][0], binmat.getTruePositiveCount());
             assertEquals(av1[1][0], binmat.getFalsePositiveCount());
             assertEquals(av1[1][1], binmat.getTrueNegativeCount());
             assertEquals(av1[0][1], binmat.getFalseNegativeCount());
 
             assertEquals(mat.getGrandTotal(), binmat.getGrandTotal());
             assertEquals(mat.getTrueCountFor(label), binmat.getTruePositiveCount());
             assertEquals(mat.getFalseCountFor(label), binmat.getFalsePositiveCount());
 
             assertEquals(mat.getTrueRateFor(label), binmat.getTruePositiveRate(), EPSILON);
             assertEquals(mat.getPredictiveValueFor(label), binmat.getPositivePredictiveValue(), EPSILON);
 
             assertEquals("fscore for label", mat.getFScoreFor(label, beta), binmat.getFScoreFor(label, beta), EPSILON);
             assertEquals("positive fscore", mat.getFScoreFor(label, beta), binmat.getPositiveFScore(beta), EPSILON);
         }
     }
 
 }
