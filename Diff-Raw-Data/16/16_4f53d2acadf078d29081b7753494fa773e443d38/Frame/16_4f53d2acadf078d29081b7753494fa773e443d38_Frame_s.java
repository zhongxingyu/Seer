 package de.winkler.bowling;
 
 public class Frame {
 
     private int firstThrow = -1;
     private int secondThrow = -1;
 
     public Frame() {   
     }
 
     public void roll(int pins) {
         if (firstThrow == -1) {
             firstThrow(pins);
         } else if (secondThrow != -1) {
             throw new IllegalStateException("Only two rolls per frame!");
         } else {
             secondThrow(pins);
         }
     }
 
     private void firstThrow(int pins) {
         firstThrow = pins;
     }
 
     private void secondThrow(int pins) {
         if (firstThrow == 10) {
             throw new IllegalStateException();
         }
         int lastPins = 10 - firstThrow;
 
         if (pins > lastPins) {
             throw new IllegalStateException();
         }
         secondThrow = pins;
     }
 
     public int getFirstThrow() {
         return firstThrow;
     }
 
     public int getSecondThrow() {
         return secondThrow;
     }
 
     public boolean isStrike() {
         return firstThrow == 10;
     }
 
     public boolean isSpare() {
         return ((firstThrow + secondThrow) == 10);
     }
 
     public boolean isMiss() {
         return ((firstThrow + secondThrow) == 0);
     }
 
 }
