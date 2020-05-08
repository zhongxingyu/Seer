 package org.pillarone.riskanalytics.domain.utils.math.distribution;
 
 import org.apache.commons.lang.NotImplementedException;
 import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
 import umontreal.iro.lecuyer.probdist.DiscreteDistribution;
 import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;
 import umontreal.iro.lecuyer.probdist.Distribution;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author jessika.walter (at) intuitive-collaboration (dot) com
  */
 public class
         TruncatedDistribution implements Distribution {
 
     Distribution distribution;
     double a;
     double b;
     double cdfLeftBoundary;
 
 
     public TruncatedDistribution(Distribution distribution, double a, double b) {
         this.distribution = distribution;
         this.a = a;
         this.b = b;
         this.cdfLeftBoundary = deriveCdfLeftBoundary();
     }
 
     public TruncatedDistribution(ContinuousDistribution distribution, double a, double b) {
         this.distribution = distribution;
         this.a = a;
         this.b = b;
         this.cdfLeftBoundary = distribution.cdf(a);
         if (cdfLeftBoundary == 1 || (distribution.cdf(b) - cdfLeftBoundary) <= 1E-8) {
            throw new IllegalArgumentException("TruncatedDistribution.nonNormalizeableSpace");
         }
     }
 
     public TruncatedDistribution(DiscreteDistribution distribution, double a, double b) {
         this.distribution = distribution;
         this.a = a;
         this.b = b;
         double[] params = distribution.getParams();
         int n = (int) params[0];
         List<Double> obs = new ArrayList<Double>(n + 1);
         for (int i = 0; i < n; i++) {
             obs.add(params[i + 1]);
         }
         obs.add(a);
         Collections.sort(obs);
         int index = obs.indexOf(a);
         this.cdfLeftBoundary = index == 0 ? 0 : distribution.cdf(obs.get(index - 1));
         if (cdfLeftBoundary == 1 || (distribution.cdf(b) - cdfLeftBoundary) <= 1E-8) {
            throw new IllegalArgumentException("TruncatedDistribution.nonNormalizeableSpace");
         }
     }
 
     public TruncatedDistribution(DiscreteDistributionInt distribution, double a, double b) {
         this.distribution = distribution;
         this.a = a;
         this.b = b;
         this.cdfLeftBoundary = a == 0 ? 0 : distribution.cdf(Math.ceil(a - 1));
         if (cdfLeftBoundary == 1 || (distribution.cdf(b) - cdfLeftBoundary) <= 1E-8) {
            throw new IllegalArgumentException("TruncatedDistribution.nonNormalizeableSpace");
         }
     }
 
     public double cdf(double x) {
         if (x < a) {
             return 0;
         }
         else if (x >= b) {
             return 1;
         }
         else {
             return (distribution.cdf(x) - cdfLeftBoundary) / (distribution.cdf(b) - cdfLeftBoundary);
         }
     }
 
     public double barF(double x) {
         return 1d - cdf(x);
     }
 
     public double inverseF(double y) {
         return distribution.inverseF(y * (distribution.cdf(b) - cdfLeftBoundary) + cdfLeftBoundary);
     }
 
 
     public double density(double x) {
         if (!(distribution instanceof ContinuousDistribution)) {
            throw new IllegalArgumentException("TruncatedDistribution.densityForContinuousDistributionOnly");
         }
         if (x < a || x > b)
             return 0d;
         return ((ContinuousDistribution) distribution).density(x) / (distribution.cdf(b) - cdfLeftBoundary);
     }
 
     public double prob(int x) {
         if (distribution instanceof ContinuousDistribution) {
            throw new IllegalArgumentException("TruncatedDistribution.probForDiscreteDistributionsOnly");
         }
         if (x < a || x > b) return 0d;
         if (distribution instanceof DiscreteDistribution)
             return ((DiscreteDistribution) distribution).prob(x) / (distribution.cdf(b) - cdfLeftBoundary);
         return ((DiscreteDistributionInt) distribution).prob(x) / (distribution.cdf(b) - cdfLeftBoundary);
     }
 
     public double getMean() {
        throw new NotImplementedException("TruncatedDistribution.noImplementationOfGetMean");
     }
 
     public double getVariance() {
        throw new NotImplementedException("TruncatedDistribution.noImplementationOfGetVariance");
     }
 
     public double getStandardDeviation() {
        throw new NotImplementedException("TruncatedDistribution.noImplementationOfGetStandardDeviation");
     }
 
     // todo(jwa): implement getParams() depending of instance of distribution
     // implement prob, respectively density
 
     public double[] getParams() {
         return new double[0];  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private double deriveCdfLeftBoundary(){
         double cdfLeftBoundary = 0;
         if (distribution instanceof ContinuousDistribution) {
             cdfLeftBoundary = distribution.cdf(a);
         }
         else if (distribution instanceof DiscreteDistribution) {
             double[] params = distribution.getParams();
             int n = (int) params[0];
             List<Double> obs = new ArrayList<Double>(n + 1);
             for (int i = 0; i < n; i++) {
                 obs.add(params[i + 1]);
             }
             obs.add(a);
             Collections.sort(obs);
             int index = obs.indexOf(a);
             cdfLeftBoundary = index == 0 ? 0 : distribution.cdf(obs.get(index - 1));
         }
         else if (distribution instanceof DiscreteDistributionInt) {
             cdfLeftBoundary = a == 0 ? 0 : distribution.cdf(Math.ceil(a - 1));
         }
         if (cdfLeftBoundary == 1 || (distribution.cdf(b) - cdfLeftBoundary) <= 1E-8) {
            throw new IllegalArgumentException("TruncatedDistribution.nonNormalizeableSpace");
         }
         return cdfLeftBoundary;
     }
 }
