 package edu.calpoly.similarity;
 
public class EditDistanceComparison implements ComparisonMetric
 {
     /**
      *     * 0 = 100% different
      *         * 1 = 100% the same
      **/
     public double compare(String code1, String code2)
     {
         EditDistance ed = new EditDistance();
         return (1000.0 - ed.Solve(code1,code2))/1000.0;
     }
 
     
 }
 
