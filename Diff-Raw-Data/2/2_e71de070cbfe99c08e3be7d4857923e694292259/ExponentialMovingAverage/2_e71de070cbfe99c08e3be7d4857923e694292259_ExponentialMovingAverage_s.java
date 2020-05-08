 /**
  * 
  */
 
 package com.zygon.trade.market.util;
 
 /**
  *
  * @author zygon
  */
 public class ExponentialMovingAverage extends MovingAverage {
 
     private final double alpha;
    private volatile Double lastValue = -1.0;
     
     public ExponentialMovingAverage(int maxValues) {
         super(maxValues);
         this.alpha = 2.0 / (maxValues + 1.0);
     }
     
     @Override
     public void add(double value) {
         double newValue = 0.0;
         
         if (this.lastValue == null) {
             this.lastValue = value;
             newValue = this.lastValue;
         } else {
             newValue = this.lastValue + (this.alpha * (value - this.lastValue));
             this.lastValue = newValue;
         }
         
         super.add(newValue);
     }
 }
