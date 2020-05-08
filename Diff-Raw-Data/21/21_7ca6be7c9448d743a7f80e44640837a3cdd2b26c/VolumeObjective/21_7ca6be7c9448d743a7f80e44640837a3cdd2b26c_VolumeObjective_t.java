 
 package com.zygon.trade.trade;
 
 /**
  *
  * @author zygon
  */
 public class VolumeObjective {
 
     public static enum Modifier {
         PERCENT
     }
     
     private final Modifier modifier;
     private final double value;
 
     public VolumeObjective(Modifier modifier, double value) {
         this.modifier = modifier;
         this.value = value;
     }
 
     public Modifier getModifier() {
         return this.modifier;
     }
 
     public double getValue() {
         return this.value;
     }
 
    public double getVolume(double account, double currentPrice) {
        
        double capital = 0.0;
        
         switch (this.modifier) {
             case PERCENT:
                capital = account * (this.value / 100);
         }
         
        return capital / currentPrice;
     }
     
     @Override
     public String toString() {
         return String.format("%d %s", this.modifier.name(), this.value);
     }
 }
