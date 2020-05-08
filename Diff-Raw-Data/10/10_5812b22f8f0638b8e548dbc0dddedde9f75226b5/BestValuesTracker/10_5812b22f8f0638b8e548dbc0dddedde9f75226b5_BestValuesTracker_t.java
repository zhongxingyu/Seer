 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
  *
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  *
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  *
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package es.csic.iiia.maxsum.util;
 
 import es.csic.iiia.maxsum.MaxOperator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utitlity class to compute the two best objects among a couple of them.
  *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class BestValuesTracker<T> {
     private static final Logger LOG = Logger.getLogger(BestValuesTracker.class.getName());
 
     private MaxOperator operator;
     private final double[] values;
     private final Object[] objects;
     private int count = 0;
 
     /**
      * Build a new tracker of best values.
      *
      * @param operator maximization operator to use.
      */
     public BestValuesTracker(MaxOperator operator) {
         values = new double[2];
         objects = new Object[2];
         this.operator = operator;
     }
 
     /**
      * Cleanup all values tracked until now.
      */
     public void reset() {
         LOG.finest("Tracking start");
         values[0] = values[1] = operator.getWorstValue();
         objects[0] = null; objects[1] = null;
         count = 0;
     }
 
     /**
      * Get the best value among tracked elements which are *not* the given one.
      *
      * @param element element to exclude from the maximization.
      * @return value of the best element which is *not* the given one.
      */
     public double getComplementary(T element) {
        return element != objects[0] ? values[0] : values[1];
     }
 
     /**
      * Get the element with a best value between all tracked ones.
      *
      * @return element with best value.
      */
     public T getBest() {
         return (T)objects[0];
     }
 
     /**
      * Get the value of the best element between all tracked ones. Alternatively, this is the
      * maximum (according to our operator) between all tracked values.
      *
      * @see MaxOperator
      * @return best value (maximum) between all tracked ones.
      */
     public double getBestValue() {
         return values[0];
     }
 
     /**
      * Track an element and its associated value.
      *
      * @param element element to track.
      * @param value value of this element.
      */
     public void track(T element, double value) {
         count++;
 
         LOG.log(Level.FINEST, "BestValues tracking {0}", value);
 
         if (operator.max(value, values[0]) == value) {
             values[1]  = values[0];     values[0]  = value;
             objects[1] = objects[0];    objects[0] = element;
             return;
         }
 
         if (operator.max(value, values[1]) == value) {
             values[1]  = value;
             objects[1] = element;
         }
 
     }
 
     @Override
     public String toString() {
         return "Best(" + values[0] + "," + values[1] + ")[" + count + "]";
     }
 
 }
