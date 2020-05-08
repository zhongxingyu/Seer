 /**
  * Copyright (c) 2012 by Tyson Gern
  * Licensed under the MIT License 
  */
 
 import java.util.*;
 
 /**
  * This class stores an element of a Coxeter group of type D and rank
  * "size" as a signed permutation, oneLine.  The methods contained in
  * this class can preform elementary operations on the element.
  * @author Tyson Gern (tygern@gmail.com)
  */
 class TypeD extends EvenElement{
 
     /**
      * This constructs an element from a signed permutation.
      * @param input The signed permutation
      */
     public TypeD(int[] input) {
         size = input.length;
         rank = input.length;
         oneLine = new int[size];
         int sign = 1;
 
         boolean[] number = new boolean[size];
         for (int i = 0; i < size; i++) number[i] = false;
         
         for(int i = 0; i < size; i++) {
             if (Math.abs(input[i]) > size || input[i] == 0 || number[Math.abs(input[i]) - 1]) {
                 throw new IllegalArgumentException("Invalid permutation");
             }
             oneLine[i] = input[i];
             number[Math.abs(input[i]) - 1] = true;
             if (oneLine[i] < 0) {
                 sign *= -1;
             }
         }
         if (sign == -1) {
             throw new IllegalArgumentException("Invalid type");
         }
     }
     
     /**
      * This constructs the identity element of a particular rank.
      * @param rank The rank of the element
      */
     protected TypeD(int rank) {
         this.rank = rank;
         this.size = rank;
         oneLine = new int[size];
         for (int i = 0; i < size; i++) {
             oneLine[i] = i + 1;
         }
     }
 
     /**
      * This method gets the inverse
      * @return The inverse of element
      */
     public TypeD findInverse() {
         return new TypeD(this.invertPermutation());
     }
 
     /**
      * This method multiplies the element on the right by a generator,
      * s.
      * @param s The generator
      * @return Nothing
      */
     private TypeD rightMultiplyS(int s) {
         if (s <= rank && s >= 1) {
             int sign = 1;
 
             if (s == 1) {
                 sign = -1;
                 s = 2;
             }
             
             switchPlaces(s-2, s-1, sign);
         }
         return this;
     }
 
     /**
      * This method multiplies the element on the left by a generator,
      * s.
      * @param s The generator
      * @return Nothing
      */
     private TypeD leftMultiplyS(int s) {
         if (s <= rank && s >= 1) {
             int sign = 1;
 
             if (s == 1) {
                 sign = -1;
                 s = 2;
             }
 
             switchValues(s - 1, s, sign);
         }
 
         return this;
     }
 
     /**
      * This method tells if an element has a reduced expression ending
      * in two noncommution generators.
      * @return true if the element is right bad
      */
     private boolean isRightBad() {
        if (-1 * oneLine[1] > oneLine[0] && -1 * oneLine[0] > oneLine[2]) return false; // 13
        if (oneLine[1] > oneLine[2] && -1 * oneLine[2] > oneLine[0]) return false; // 31
         
        for(int j = 0; j < size - 3; j++) {
             if (oneLine[j] > oneLine[j + 1] && oneLine[j + 1] > oneLine[j + 2]) return false; //321
             if (oneLine[j] > oneLine[j + 2] && oneLine[j + 2] > oneLine[j + 1]) return false; //312
             if (oneLine[j + 1] > oneLine[j] && oneLine[j] > oneLine[j + 2]) return false; //231
         }
 
         return true;
     }
 
     /**
      * This method tells if an element has a reduced expression
      * beginning in two noncommution generators.
      * @return true if the element is left bad
      */
     private boolean isLeftBad() {
         return findInverse().isRightBad();
     }
 
     /**
      * This method tells if an element has a reduced expression
      * beginning or ending in two noncommution generators, or if the
      * element is a product of commuting generators.
      * @return true if the element is bad
      */
     public boolean isBad() {
         if (commutingGenerators()) {
             return false;
         }
         return (isRightBad() && isLeftBad());
     }
 
     /**
      * This method tells if an element is a product of commuting
      * generators.
      * @return true if the element is a product of commuting
      * generators
      */
     private boolean commutingGenerators() {
         int j = 0;
         if (oneLine[0] == 1) {
             j = 1;
         }
         else if (oneLine[0] == -1) {
             if (oneLine[1] != -2) {
                 return false;
             }
             j = 2;
         }
         else if (oneLine[0] == 2) {
             if (oneLine[1] != 1) {
                 return false;
             }
             j = 2;
         }
         else if (oneLine[0] == -2) {
             if (oneLine[1] != -1) {
                 return false;
             }
             j = 2;
         }
         else {
             return false;
         }
         
         while (j < size - 2) {
             if (oneLine[j] > j + 2) {
                 return false;
             }
             else if (oneLine[j] == j + 1) {
                 j += 1;
             }
             else { // oneLine[j] = j + 2
                 if (oneLine[j + 1] != j + 1) return false;
                 j += 2;
             }
         }
             
         return true;
     }
     
     /**
      * This method gets the length of the element
      * @return the length of the element.
      */
     public int length() {
         return countInv(1) + countInv(-1);
     }
 
     /**
      * This method returns true if s is in the right descent set of
      * the element, false otherwise.
      * @return true is s is a descent of the element
      */
     protected boolean isRightDescent(int s) {
         if (s == 1) {
             return (-1 * oneLine[1] > oneLine[0]);
         }
         if (s >= 2 && s <= rank) {
             return (oneLine[s - 2] > oneLine[s - 1]);
         }
         return false;
     }
 
     /**
      * This method creates a TypeDExpression reduced expression from a
      * signed permutation.
      * @return a reduced expression
      */
     public TypeDExpression findRE() {
         ArrayList<Integer> generator = new ArrayList<Integer> ();
         TypeD permutation = new TypeD(oneLine);
         
         while (permutation.length() != 0) {
             for (int i = permutation.size; i >= 1; i--) {
                 if (permutation.isRightDescent(i)) {
                     generator.add(i);
                     permutation.rightMultiplyS(i);
                 }
             }
         }
 
         int length = generator.size();
         int[] genArray = new int[length];
         for (int i = 0; i < length; i++) {
             genArray[length - 1 - i] = generator.get(i).intValue();
         }
 
         TypeDExpression redExp = new TypeDExpression(genArray, permutation.rank);
         return redExp;
         
     }
 
     /**
      * This method multiplies a signed permutation on the right by
      * another signed permutation.
      * @param the other element
      * @return the product of this and other
      */
     public TypeD rightMultiply(TypeD other) {
         if (!compatible(other)) return null;
         return new TypeD(rightMultiplyPerm(other));
     }
 
     /**
      * This method multiplies a signed permutation on the left by
      * another signed permutation.
      * @param the other element
      * @return the product of this and other
      */
     public TypeD leftMultiply(TypeD other) {
         return other.rightMultiply(this);
     }
 }
