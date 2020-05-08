 package ru.alepar.minesweeper;
 
 public class SteppedOnABomb extends Exception {
 
     public SteppedOnABomb(Point p) {
        super(String.format("stepped on a bomdb at (%d, %d)", p.x, p.y));
     }
 
 }
