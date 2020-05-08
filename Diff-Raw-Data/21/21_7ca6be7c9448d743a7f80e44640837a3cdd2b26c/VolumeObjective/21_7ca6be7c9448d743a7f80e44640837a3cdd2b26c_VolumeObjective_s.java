 
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
 
    public double getVolume(double account) {
         switch (this.modifier) {
             case PERCENT:
                return account * (this.value / 100);
         }
         
        throw new UnsupportedOperationException();
     }
     
     @Override
     public String toString() {
         return String.format("%d %s", this.modifier.name(), this.value);
     }
 }
