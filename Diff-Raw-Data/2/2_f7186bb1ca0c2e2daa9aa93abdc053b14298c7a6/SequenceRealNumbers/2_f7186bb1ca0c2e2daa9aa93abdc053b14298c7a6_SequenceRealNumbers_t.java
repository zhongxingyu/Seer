 package com.github.ivanshchitov.sequencerealnumbers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * The sequence real numbers class.
  *
  * @author Ivan Shchitov
  */
 public class SequenceRealNumbers {
     /**
      * Sequence real numbers.
      */
     private final ArrayList<Double> sequence;
 
     /**
      * Constructor.
      */
     public SequenceRealNumbers() {
         sequence = new ArrayList<Double>();
     }
 
     /**
      * Adds number in sequence.
      *
      * @param number real number
      */
     public void add(double number) {
         sequence.add(number);
     }
 
     /**
      * Returns maximum of sequence.
      *
      * @return maximum of sequence
      */
     public double getMaximum() {
         double maximum = sequence.get(0);
         for (int i = 0; i < sequence.size(); i++) {
             if (Double.compare(sequence.get(i), maximum) > 0) {
                 maximum = sequence.get(i);
             }
         }
         return maximum;
     }
 
     /**
      * Returns minimum of sequence.
      *
      * @return minimum of sequence
      */
     public double getMinimum() {
         double minimum = sequence.get(0);
         for (int i = 0; i < sequence.size(); i++) {
             if (Double.compare(sequence.get(i), minimum) < 0) {
                 minimum = sequence.get(i);
             }
         }
         return minimum;
     }
 
     /**
      * Returns average value of sequence.
      *
      * @return average value of sequence
      */
     public double getAverage() {
        double sumSequence = 0;
         for (int i = 0; i < sequence.size(); i++) {
             sumSequence += sequence.get(i);
         }
         return (double) sumSequence / sequence.size();
     }
 
     /**
      * Returns median of sequence.
      *
      * @return median of sequence
      */
     public double getMedian() {
         Collections.sort(sequence);
         // При четном количестве элементов этот код верен, т. к.
         // отсчет элементов с нуля, поэтому и обращаемся к правому
         // элементу из двух центральных.
         double median = sequence.get(sequence.size() / 2);
         if (sequence.size() % 2 == 0) {
             median = (median + sequence.get(sequence.size() / 2 - 1)) / 2;
         }
         return median;
     }
 
     /**
      * Returns size of the sequence.
      *
      * @return size of the sequence
      */
     public int getSize() {
         return sequence.size();
     }
 
     /**
      * Returns sequence element at index.
      *
      * @param index element index of sequence
      * @return sequence element
      * @throws ArrayIndexOutOfBoundsException if index is not valid
      */
     public double getElement(int index) throws ArrayIndexOutOfBoundsException {
             return sequence.get(index);
     }
 
     /**
      * Prints sequence elements in string.
      *
      * @return string with sequence real numbers
      */
     public String printSequence() {
         String sequence = "Sequence: |";
         for (int i = 0; i < getSize(); i++) {
             sequence += getElement(i) + "|";
         }
         return sequence;
     }
 
     /**
      * Returns mods of sequence as String.
      *
      * @return mods of sequence
      */
     public String getMods() {
         Collections.sort(sequence);
         String mods = "Mods: \n" + sequence.get(0) + "| ";
         for (int i = 0; i < getSize(); i++) {
             if(i == 0) {
                 mods += "#";
                 continue;
             }
             if (Double.compare(getElement(i), getElement(i - 1)) == 0) {
                 mods += "#";
             } else {
                 mods += "\n" + sequence.get(i) + "| #";
             }
         }
         return mods;
     }
 }
