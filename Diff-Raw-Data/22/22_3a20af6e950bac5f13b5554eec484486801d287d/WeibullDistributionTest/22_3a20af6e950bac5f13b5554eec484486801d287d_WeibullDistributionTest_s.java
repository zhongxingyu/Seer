 /**
  * This file is part of sina.
  *
  * sina is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * sina is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with sina.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mashti.sina.distribution;
 
 import DistLib.weibull;
 import java.util.Random;
 import org.junit.Assert;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mashti.sina.distribution.ProbabilityDistribution.HALF;
 import static org.mashti.sina.distribution.ProbabilityDistribution.ONE;
 
 /**
  * Tests {@link WeibullDistribution}.
  *
  * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
  */
 public class WeibullDistributionTest {
 
    public static final double DELTA = 0.0d;
     //@formatter:off
     private static final double[][] TEST_INPUT_OUTPUT = {
             {0.8842, 852456, 905853.2908924981, 457, 2.4778507908955887E-6, 0.0012815021029850548, 0.457, 487985.81582461746, 563184.9037453446, Double.NaN, -1.8566473246120316, 1.0545908823268892E12}, {1.5, 50, 45.13726464754669, 11, 0.012691650910201193, 0.09804364473659022, 0.11, 11.929016803104716, 39.160988438732566, 24.037492838456807, -2.4427852784427633, 939.2257120348302}
     };
     //@formatter:on
 
     /** Tests {@link WeibullDistribution#WeibullDistribution(Number, Number)}. */
     @Test
     public void testWeibullDistribution() {
 
         final Number[][] bad_args = {{Double.NEGATIVE_INFINITY, -Double.MAX_VALUE}, {-Double.MAX_VALUE, -Double.MAX_VALUE}, {-Double.MAX_VALUE, Double.NEGATIVE_INFINITY}, {0.0, -0.0}, {-1.0, -0.0}, {-1.0, -0.0}, {Double.NaN, Double.NaN}, {+0.0, Double.NaN}};
         for (final Number[] bad_arg : bad_args) {
             try {
                 new WeibullDistribution(bad_arg[0], bad_arg[1]);
                 fail();
             }
             catch (final IllegalArgumentException e) {
                 continue;
             }
         }
 
         final Number[][] good_args = {{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}, {Double.MAX_VALUE, Double.MAX_VALUE}, {ONE, ONE}, {HALF, HALF}, {ONE / HALF, ONE}};
         for (final Number[] good_arg : good_args) {
             final Number shape = good_arg[0];
             final Number scale = good_arg[1];
             final WeibullDistribution test_dist = new WeibullDistribution(shape, scale);
             Assert.assertEquals(test_dist.getShape().doubleValue(), shape);
             Assert.assertEquals(test_dist.getScale().doubleValue(), scale);
         }
     }
 
     /** Tests {@link WeibullDistribution#byMean(Number, Number)}. */
     @Test
     public void testByMeanNumber() {
 
         ////@formatter:off
         final Number[][] bad_args = {
                 {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY}, {-Double.MAX_VALUE, -Double.MAX_VALUE}, {-Double.MAX_VALUE, Double.NEGATIVE_INFINITY}, {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE}, {0.0, -1}, {Double.NaN, +0.0}, {-1.0, Double.NaN}, {Double.NaN, Double.NaN}
         };
         ////@formatter:on
         for (final Number[] bad_arg : bad_args) {
             try {
                 WeibullDistribution.byMean(bad_arg[0], bad_arg[1]);
                 fail();
             }
             catch (final IllegalArgumentException e) {
                 continue;
             }
         }
 
         ////@formatter:off
         final Number[][] good_args = {
                 {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}, {Double.MAX_VALUE, Double.POSITIVE_INFINITY}, {1.0, 1000.0}, {1.0, 1000.0}
         };
         ////@formatter:on
         for (final Number[] good_arg : good_args) {
             final Number shape = good_arg[0];
             final Number mean = good_arg[1];
             final WeibullDistribution test_dist = WeibullDistribution.byMean(shape, mean);
             Assert.assertEquals(test_dist.mean(), mean);
             Assert.assertEquals(test_dist.getShape(), shape);
         }
     }
 
     /** Tests rate, mean probability, cumulative, quantile, median,mode, skewness and variance. */
     @Test
     public void testCalculations() {
 
         for (final double[] args : TEST_INPUT_OUTPUT) {
             int i = 0;
             final double shape = args[i++];
             final double scale = args[i++];
             final WeibullDistribution distribution = new WeibullDistribution(shape, scale);
             Assert.assertEquals(distribution.getShape().doubleValue(), shape, DELTA);
             Assert.assertEquals(distribution.getScale().doubleValue(), scale, DELTA);
             Assert.assertEquals(distribution.mean().doubleValue(), args[i++], DELTA);
             final double x = args[i++];
             Assert.assertEquals(distribution.density(x).doubleValue(), args[i++], DELTA);
             Assert.assertEquals(distribution.cumulative(x).doubleValue(), args[i++], DELTA);
             Assert.assertEquals(distribution.quantile(args[i++]).doubleValue(), args[i++], DELTA);
             Assert.assertEquals(distribution.median().doubleValue(), args[i++], DELTA);
             Assert.assertEquals(distribution.mode().doubleValue(), args[i++], DELTA);
             Assert.assertEquals(distribution.skewness().doubleValue(), args[i++], DELTA);
             Assert.assertEquals(distribution.variance().doubleValue(), args[i++], DELTA);
         }
     }
 
     @Test
     public void testProbability() throws Exception {
 
         Random random = new Random(654654);
         for (int i = 0; i < 10000; i++) {
             final double v1 = StrictMath.abs(random.nextDouble() * random.nextInt());
             final double v2 = StrictMath.abs(random.nextDouble() * random.nextInt());
             final WeibullDistribution tt = new WeibullDistribution(v1, v2);
             final int i1 = Math.abs(random.nextInt());
 
             final org.apache.commons.math3.distribution.WeibullDistribution tDistribution = new org.apache.commons.math3.distribution.WeibullDistribution(v1, v2);
             final double actual = tDistribution.density(i1);
             final double probability = tt.density(i1).doubleValue();
             final double density = weibull.density(i1, v1, v2);
            assertEquals(actual, probability, 1.0e-10);
            assertEquals(actual, density, 1.0e-10);
 
             final float v = random.nextFloat();
             assertEquals(tDistribution.inverseCumulativeProbability(v), weibull.quantile(v, v1, v2), 1.0e-5);
             assertEquals(tDistribution.inverseCumulativeProbability(v), tt.quantile(v).doubleValue(), 1.0e-5);
         }
     }
 }
