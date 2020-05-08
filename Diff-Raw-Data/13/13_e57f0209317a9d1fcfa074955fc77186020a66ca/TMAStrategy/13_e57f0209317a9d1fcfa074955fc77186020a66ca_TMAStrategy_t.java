 package com.rathesh.codejam2012.server.strategies;
 
 import java.util.List;
 
 public class TMAStrategy extends AbstractStrategy {
   protected SMAStrategy smaStrategy;
 
   public TMAStrategy(int N, boolean isFast) {
     super(N, isFast);
     smaStrategy = new SMAStrategy(N, isFast);
   }
 
   @Override
   public double update(double price) {
    
 
     smaStrategy.update(price);
     List<Double> smaAverages = smaStrategy.getAverages();
     double triangularAverage = 0;
    if (smaAverages.size() < N ) {
       for (int i = 0; i < smaAverages.size(); i++) {
         triangularAverage += smaAverages.get(i);
       }
      triangularAverage = triangularAverage / smaAverages.size();
     }
     else {
      for (int i = smaAverages.size() - N; i < smaAverages.size(); i++) {
         triangularAverage += smaAverages.get(i);
       }
      triangularAverage = triangularAverage / N;
     }
 
     this.averages.add(triangularAverage);
 
     return this.getAverage();
   }
 
 }
