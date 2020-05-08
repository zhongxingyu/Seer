 package edu.msergey.jalg.exercises.ch5.ex51;
 
 public class BottomUpKnapsack extends BaseKnapsack {
     public BottomUpKnapsack(int capacity) {
         super(capacity);
     }
 
     @Override
     protected int evalHighestValue() {
         return evalHighestValue(0);
     }
 
     private int evalHighestValue(int capacity) {
        if (capacity > super.capacity) return super.knownHighestValues[super.capacity];
         if (super.knownHighestValues[capacity] != super.UNKNOWN_VALUE) return super.knownHighestValues[capacity];
 
         int localMaxValue = 0;
         for (int i = 0; i < super.ITEMS.length; ++i) {
             int capacityLeft = capacity - super.ITEMS[i].getSize();
             if (capacityLeft >= 0) {
                 int currentValue = evalHighestValue(capacityLeft) + super.ITEMS[i].getValue();
                 if (currentValue > localMaxValue) {
                     localMaxValue = currentValue;
                 }
             }
         }
         super.knownHighestValues[capacity] = localMaxValue;
 
         return evalHighestValue(capacity + 1);
     }
 }
