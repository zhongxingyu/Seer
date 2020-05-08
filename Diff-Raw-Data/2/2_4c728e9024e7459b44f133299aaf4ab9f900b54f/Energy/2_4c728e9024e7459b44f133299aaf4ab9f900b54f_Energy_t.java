 package com.volumetricpixels.mcsquared.api.energy;
 
public abstract class Energy<T extends Energy<?>> implements Comparable<T> {
 
     protected final float energy;
     
     public Energy(float energy) {
         if (energy < 0) {
             throw new IllegalArgumentException("Energy cannot be negative!");
         }
         this.energy = energy;
     }
     
     public float getValue() {
         return energy;
     }
     
     public abstract T add(T other);
     
     public abstract T substract(T other);
     
     public abstract T multiply(T other);
     
     public abstract T divide(T other);
     
     public abstract T split(int size);
     
     public abstract T newEmpty();
     
     public boolean isEmpty() {
         return energy <= 0;
     }
     
     public int compareTo(T other) {
         return (int) (energy - other.energy);
     }
 }
