 package org.eclipse.stem.core.math;
 
 
 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 /**
  * Utility primarily used by stochastic models
  * This technique makes a random pick from a binomial distribution.
  * The complex part of this operationn is to compute the binomial coefficient
  * efficiently (see for example: http://en.wikipedia.org/wiki/Binomial_distribution)
  * In order to do the computation for large N (large S) we compute the log of the binomial coefficent
  * so we do a sum (instead of a factorial product) and then exponentiate the result. 
  */
 public class BinomialDistributionUtil {
 
 	 /**
 	  * Returns the random pick from a binomial dist
 	  * given 0<=rndVar<=1
 	  * This sums the binomial dist and retruns the k value with that integrated probability
 	  * @param p The probability
 	  * @param n N, (e.g. susceptible)	   
 	  * @param rndVAR Random number
 	  * @return K Random pick
 	  **/ 
 	 public static int fastPickFromBinomialDist(double p, int n, double rndVAR){
 		 double Bsum = 0.0;
 		 // compute this once
 		 double lnFactorialN = lnFactorial(n);
 		 double lnFactorialK = 0.0;
 		 double lnFactorialN_K = lnFactorialN;
 		 int n_k = n;
 		 for(int k = 0; k <= n; k ++) {
 			 
 			 if(k>=1) {
 				 // count up
 				 lnFactorialK += Math.log((double)k);
 			 
 				 // count down for n-k
 				 lnFactorialN_K -= Math.log((double)n_k);
 				 n_k -= 1;
 			 }
 			 // do the entire computation in log space
 			 double logB = lnFactorialN - lnFactorialK - lnFactorialN_K;
 			 // now instead of multplying by p^k * (1-p)^n-k
 			 // we add
 			 // logB + k*log(p) + (n-k)*log(1-p);
 			 logB += (k*Math.log(p))  + ( (n-k) * Math.log (1-p) );
 			 // NOW we take the exponent
 			 Bsum += Math.exp(logB);
 			 if (Bsum >= rndVAR) return k;
 		 }
 		 return n;
 	 }
 	 
 	   
 	 /**
 	  * compute the log(n!)
 	  * @param n
 	  * @return log(n!)
 	  */
 	   static double lnFactorial(int n) {
 	      double retVal = 0.0;
 	
 	      for (int i = 1; i <= n; i++) {
 	    	  retVal += Math.log((double)i);
 	      }
 	       
 	      return retVal;
 	   }
 }
