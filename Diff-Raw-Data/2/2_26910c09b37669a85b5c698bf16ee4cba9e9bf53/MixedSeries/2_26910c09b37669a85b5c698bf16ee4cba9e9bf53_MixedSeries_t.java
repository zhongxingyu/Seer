 package com.comsysto.insight.model.options.series.impl;
 
 import com.comsysto.insight.model.options.Point;
 import com.comsysto.insight.model.options.series.generic.AbstractSeries;
 
 import java.util.Collection;
 
 /**
  * Implementation of {@link AbstractSeries} which represents a mixed array of Numbers, Label/Y-Pairs, X/Y-Coordinates and Points
  * to be plotted on the chart.
  * <p/>
  * For more information read href="http://www.highcharts.com/ref/#series.
  * <p/>
  * Date: Feb 18, 2011 Time: 9:47:09 PM
  *
  * @author Mohammed El Batya
  */
 public class MixedSeries extends AbstractSeries<Object[]> {
 
 
     /**
      * {@link AbstractSeries#AbstractSeries()}
      */
     public MixedSeries() {
     }
 
     /**
      * {@link AbstractSeries#AbstractSeries(String)}
      */
     public MixedSeries(String pName) {
         super(pName);
     }
 
     /**
      * @param pMixedData
      * @return
      */
     public MixedSeries setData(Object[] pMixedData) {
         data = pMixedData;
         return this;
     }
 
 
     /**
      * Sets a two-dimensional array of Label/Y-Pairs to be plotted on the chart. The corresponding X-Values will be
      * automatically calculated by Highcharts, counting from 0 on.
      *
      * The second dimension of the array must have the length of 2, to represent Label/Y pairs. Additionally the first
      * field of the second dimension must be a String value representing the label. The second field must be an instance
      * of {@link Number}. If that is not the case, an {@link java.security.InvalidParameterException} will be thrown.
      *
      * @param pData a two-dimensional array of Label/Y-Pairs
      *
      * @return this object for convenient chaining, not a copy
      * @throws java.security.InvalidParameterException
      * @see LabeledNumberSeries
      */
 
     /**
      * Sets an collection with data points of different types to be drawn as a series on the chart.
      * <p/>
      * Only the following types are allowed to be in the collection,
      * otherwise an {@link IllegalArgumentException} will be thrown.
      * <p/>
      * - {@link Point}s
      * - {@link Number}s
      * - Array with length of 2 and containing a Number/Number pair
      * - Array with length of 2 and containing a String/Number pair
      *
      * @param pMixedData an collection with data points of different types to be drawn as a series on the chart
      * @return this object for convenient chaining, not a copy
      */
     public MixedSeries setData(Collection<?> pMixedData) {
 
         for (Object data : pMixedData) {
 
             if (!(isNumberOrPoint(data) || isValidArrayContainingAPoint(data))) {
                 throw new IllegalArgumentException("The mixed data series can only contain Numbers, Points, and Arrays which " +
                         "have the length of 2 and contain a Number-Number(X/Y) pair or a String-Number(Label/Y) pair.");
             }
 
         }
 
         return setData(pMixedData.toArray());
     }
 
     /**
      * Helper method which checks the given object for its type.
      * If it's {@link Number} or {@link Point}, {@code true} will be returned, otherwise {@code false}.
      *
      * @param pObject the object to be checked
      * @return {@code true} it the given object's type is {@link Number} or {@link Point},  otherwise {@code false}.
      */
     private boolean isNumberOrPoint(Object pObject) {
         return (pObject instanceof Number || pObject instanceof Point);
     }
 
     /**
      * Helper method which checks the given object for being an array containing a valid series point which can be used
      * within a mixed series.
      * <p/>
      * It is valid if it is an array with the length of 2 and containing a Number/Number-Pair or a String/Number-Pair.
      * <p/>
     * If valid, {@code true} will be returned, otherwise {@code false}.
      *
      * @param pObject the object to be checked
      * @return {@code true} if valid, otherwise {@code false}.
      */
     private boolean isValidArrayContainingAPoint(Object pObject) {
 
         boolean isValid;
 
         if (pObject instanceof Object[]) {
             Object[] array = (Object[]) pObject;
 
             if (array.length == 2) {
                 if (array[0] instanceof String && array[1] instanceof Number) {
                     isValid = true;
                 } else if (array[0] instanceof Number && array[1] instanceof Number) {
                     isValid = true;
                 } else {
                     isValid = false;
                 }
 
             } else {
                 isValid = false;
             }
 
         } else {
             isValid = false;
         }
 
         return isValid;
     }
 
 }
