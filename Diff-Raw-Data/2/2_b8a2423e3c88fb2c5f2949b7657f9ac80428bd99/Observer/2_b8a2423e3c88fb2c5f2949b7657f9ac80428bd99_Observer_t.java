 package com.saleem_siddiqui.java.util;
 
 /**
  /**
  * A class can implement the <code>Observer</code> interface when it
  * wants to be informed of changes in observable objects.
  *
  * @author Saleem Siddiqui
  * @see     com.saleem_siddiqui.java.util.Observable
 */
 
 public interface Observer<E> {
     /**
      * This method is called whenever the observed object is changed. An
      * application calls an <tt>Observable</tt> object's
      * <code>notifyObservers</code> method to have all the object's
      * observers notified of the change.
      *
      * @param observable   the observable object.
      * @param event an argument passed to the <code>notifyObservers</code>
      *            method.
      */
     public void update(Observable observable, E event);
 }
