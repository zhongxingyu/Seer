 /**
  * Copyright (c) 2012 by Tyson Gern
  * Licensed under the MIT License 
  */
 
 import java.util.*;
 
 /**
  * This class stores an element of a Coxeter group of rank "rank" as a
  * signed permutation, oneLine.  The methods contained in this class
  * can preform elementary operations on the element.
  * @author Tyson Gern (tygern@gmail.com)
  */
 abstract class EvenElement extends Element {
 
     protected int countNeg() {
         int count = 0;
         
         for (int i = 1; i <= size; i++) {
            if (mapsTo(i) < 0) count ++;
         }
         
         return count;
     }
 
 }
