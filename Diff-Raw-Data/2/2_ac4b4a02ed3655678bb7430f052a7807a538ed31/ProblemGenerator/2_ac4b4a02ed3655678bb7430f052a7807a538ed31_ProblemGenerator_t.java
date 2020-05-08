 package com.akavrt.csp.cutgen;
 
 import com.akavrt.csp.Order;
 import com.akavrt.csp.Problem;
 
 /**
  * <p>Java port of the CUTGEN1 - problem generator for one-dimensional single stock size cutting
  * stock problem (1D SSSCSP) developed by Gau and Wascher (1995).</p>
  *
  * <p>This is a direct port to Java of the original CUTGEN1 procedure written in Fortran. The
  * source code for the latter one is available at ESICUP
  * <a href="http://paginas.fe.up.pt/~esicup">website</a>.</p>
  *
  * <p>For more detailed explanation refer to the original EJOR paper:</p>
  *
  * <p>Gau, T., and Wascher, G., 1995, CUTGEN1 - a problem generator for the one-dimensional
  * cutting stock problem. European Journal of Operational Research, 84, 572-579.</p>
  *
  * @author Victor Balabanov <akavrt@gmail.com>
  */
 public class ProblemGenerator {
     private final PseudoRandom rGen;
     private final ProblemDescriptors descriptors;
 
     /**
      * <p>Creates an instance of problem generator. Instances of the random number generator and
      * problem descriptors provided as a parameters.</p>
      *
      * @param random      Already initialized instance of the random number generator.
      * @param descriptors Descriptors of the class of the problem instances to be generated.
      */
     public ProblemGenerator(PseudoRandom random, ProblemDescriptors descriptors) {
         this.rGen = random;
         this.descriptors = descriptors;
     }
 
     /**
      * <p>Generates problem instance with predefined characteristics.</p>
      *
      * <p>As stated in Gau and Wascher (1995), the actual size of the problem instance doesn't
      * necessarily equals to ProblemDescriptors.getSize(), because the possibility exists that
      * identical order lengths may be generated. Demands of identical order lengths are summed
      * up.</p>
      *
      * @return Problem instance.
      */
     public Problem nextProblem() {
         int[] lengths = generateLengths();
         int[] demands = generateDemands();
 
         return merge(lengths, demands);
     }
 
     private int[] generateLengths() {
         int[] result = new int[descriptors.getSize()];
 
         double lb = descriptors.getOrderLengthLowerBound();
         double ub = descriptors.getOrderLengthUpperBound();
        for (int i = 0; i < result.length; i++) {
             double rValue = rGen.nextDouble();
             double length = (lb + (ub - lb) * rValue) * descriptors.getStockLength() + rValue;
 
             result[i] = (int) length;
         }
 
         Utils.descendingSort(result);
 
         return result;
     }
 
     private int[] generateDemands() {
         int[] result = new int[descriptors.getSize()];
 
         double sum = 0;
         double[] rands = new double[descriptors.getSize()];
         for (int i = 0; i < result.length; i++) {
             rands[i] = rGen.nextDouble();
             sum += rands[i];
         }
 
         int totalDemand = descriptors.getAverageDemand() * descriptors.getSize();
         int rest = totalDemand;
         for (int i = 0; i < result.length - 1; i++) {
             double demand = totalDemand * rands[i] / sum + 0.5;
             result[i] = Math.max(1, (int) demand);
 
             rest -= result[i];
         }
 
         result[result.length - 1] = Math.max(1, rest);
 
         return result;
     }
 
     /**
      * <p>Used to identify orders with identical lengths and sum up the corresponding demands.</p>
      */
     private Problem merge(int[] lengths, int[] demands) {
         Problem problem = new Problem(descriptors.getStockLength());
         for (int i = 0; i < lengths.length; i++) {
             if (i == lengths.length - 1 || lengths[i] != lengths[i + 1]) {
                 Order order = new Order(lengths[i], demands[i]);
                 problem.addOrder(order);
             } else {
                 demands[i + 1] += demands[i];
             }
         }
 
         return problem;
     }
 
 }
