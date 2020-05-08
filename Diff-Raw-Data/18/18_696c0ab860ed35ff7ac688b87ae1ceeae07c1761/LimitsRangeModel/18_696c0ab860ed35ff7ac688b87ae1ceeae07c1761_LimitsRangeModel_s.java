 /*
  * #%L
  * Cyni Sample Gui implementaion
  * $Id:$
  * $HeadURL:$
  * %%
  * Copyright (C) 2006 - 2013 The Cytoscape Consortium
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.cytoscape.cyniSampleGuiAlgorithm.internal;
 
 
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 
 public class LimitsRangeModel implements BoundedRangeModel {
     protected ChangeEvent changeEvent = null;
     protected EventListenerList listenerList = new EventListenerList();
 
     protected int maximum = 10000;
     protected int minimum = 0;
     protected int extent = 0;
     protected double value = 0.0;
     protected double limit = 1.0;
     protected boolean isAdjusting = false;
 
     public LimitsRangeModel() {
     }
 
     public double getLimit() {
         return limit;
     }
 
     public void setLimit(double limit) {
         this.limit = limit;
         fireStateChanged();
     }
 
     public int getMaximum() {
         return maximum;
     }
 
     public void setMaximum(int newMaximum) {
         setRangeProperties(value, extent, minimum, newMaximum, isAdjusting);
     }
 
     public int getMinimum() {
         return (int)minimum;
     }
 
     public void setMinimum(int newMinimum) {
     	minimum = newMinimum;
     	setRangeProperties(value, extent, minimum, maximum, isAdjusting);
     }
 
     public int getValue() {
         return (int)getDoubleValue();
     }
 
     public void setValue(int newValue) {
         setDoubleValue((double)newValue);
     }
 
     public double getDoubleValue() {
         return value;
     }
 
     public void setDoubleValue(double newValue) {
         setRangeProperties(newValue, extent, minimum, maximum, isAdjusting);
     }
 
     public int getExtent() {
         return (int)extent;
     }
 
     public void setExtent(int newExtent) {
         //Do nothing.
     }
 
     public boolean getValueIsAdjusting() {
         return isAdjusting;
     }
 
     public void setValueIsAdjusting(boolean b) {
         setRangeProperties(value, extent, minimum, maximum, b);
     }
 
     public void setRangeProperties(int newValue,
                                    int newExtent,
                                    int newMin,
                                    int newMax,
                                    boolean newAdjusting) {
         setRangeProperties((double)newValue,
                            newExtent,
                            newMin,
                            newMax,
                            newAdjusting);
     }
 
     public void setRangeProperties(double newValue,
                                    int unusedExtent,
                                    int unusedMin,
                                    int newMax,
                                    boolean newAdjusting) {
         if (newMax <= minimum) {
             newMax = minimum + 1;
         }
         if (Math.round(newValue) > newMax) { 
             newValue = newMax;
         }
 
         boolean changeOccurred = false;
         if (newValue != value) {
             value = newValue;
             changeOccurred = true;
         }
         if (newMax != maximum) {
             maximum = newMax;
             changeOccurred = true;
         }
         if (newAdjusting != isAdjusting) {
             maximum = newMax;
             isAdjusting = newAdjusting;
             changeOccurred = true;
         }
 
         if (changeOccurred) {
             fireStateChanged();
         }
     }
 
     public void addChangeListener(ChangeListener l) {
         listenerList.add(ChangeListener.class, l);
     }
 
     public void removeChangeListener(ChangeListener l) {
         listenerList.remove(ChangeListener.class, l);
     }
 
     protected void fireStateChanged() {
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -=2 ) {
             if (listeners[i] == ChangeListener.class) {
                 if (changeEvent == null) {
                     changeEvent = new ChangeEvent(this);
                 }
                 ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
             }
         }
     }
 }
