 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.appbase.client;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  * The stack of visible windows. After the window stack has been validated
  * with a call to <code>validate()</code> each non-coplanar (i.e. "stacked") 
  * windows each has a unique zOrder. The top window has zOrder = 0, 
  * and zOrders increase from top to bottom. The zOrder of the bottom window is 
  * getNumStackedWindows() - 1.
  *
  * @author deronj
  */
 @ExperimentalAPI
 class WindowStack {
 
     /** 
      * The list of stacked windows. Windows in the list appear in top to bottom order.
      * After validate is called, each has a unique zOrder. The top window has zOrder = 0, 
      * and zOrders increase from top to bottom. The zOrder of the bottom window is 
      * getNumStackedWindows() - 1.
      */
     protected LinkedList<Window2D> stackedWindows = new LinkedList<Window2D>();
  
     /** 
      * The (unordered) list of coplanar windows. These have the zOrder of their parents.
      */
     protected LinkedList<Window2D> coplanarWindows = new LinkedList<Window2D>();
 
     /**
      * Deallocate resources.
      */
     public synchronized void cleanup() {
         stackedWindows.clear();
         coplanarWindows.clear();
     }
 
     /**
      * Add a new window to the stack. If the window is non-coplanar move it to the top of the stack.
      * @param window The window to be added.
      */
     public synchronized void add(Window2D window) {
         addNoValidate(window);
         validate();
     }
 
     /**
      * Same as the <code>add</code> method, but don't validate (i.e. recalculate the window Z orders)
      * afterward. Used during conventional window slave synchronization.
      * @param window The window to be added.
      */
     public synchronized void addNoValidate(Window2D window) {
         ensureNotInEitherList(window);
         if (window.isCoplanar()) {
             coplanarWindows.add(window);
         } else {
             stackedWindows.addFirst(window);
         }
      }
 
      /**
       * Remove the given window from the stack.
       * @param window The window to be removed.
       */
     public synchronized void remove(Window2D window) {
         ensureNotInEitherList(window);
         validate();
     }
 
     /** Make sure that the window isn't in either list. */
     private final void ensureNotInEitherList(Window2D window) {
         stackedWindows.remove(window);
         coplanarWindows.remove(window);
     }
 
     /** 
      * Tell the stack that the coplanar attribute of the given window has been updated. 
      * If the window is no longer coplanar it is placed immediately above its parent.
      * If it has no parent it is placed on top of the stack.
      */
     synchronized void coplanarUpdated(Window2D window) {
         coplanarUpdatedNoValidate(window);
         validate();
     }
 
     /** 
      * Same as the <code>coplanarUpdated</code> method, but don't validate afterward.
      * Used during conventional window slave synchronization.
      */
     synchronized void coplanarUpdatedNoValidate(Window2D window) {
         ensureNotInEitherList(window);
         if (window.isCoplanar()) {
             coplanarWindows.add(window);
         } else {
             Window2D parentWindow = window.getParent();
             addNoValidate(window);
             if (parentWindow != null) {
                 restackAbove(window, parentWindow);
             }
         }
     }
 
     /**
      * Returns the number of non-coplanar (i.e. "stacked") windows.
      */
     public synchronized int getNumStackedWindows () {
         return stackedWindows.size();
     }
 
     /**
      * Returns the number of coplanar windows.
      */
     public synchronized int getNumCoplanarWindows () {
         return coplanarWindows.size();
     }
 
 
     /**
      * Move the given window to the top of the stack. This is ignored for coplanar windows.
      * @param window The window to be moved.
      */
     public synchronized void restackToTop(Window2D window) {
         if (!window.isCoplanar()) {
             stackedWindows.remove(window);
             stackedWindows.addFirst(window);
             validate();
         }
     }
 
     /**
      * Move the given window to the bottom of the stack. This is ignored for coplanar windows.
      * @param window The window to be moved.
      */
     public synchronized void restackToBottom(Window2D window) {
         if (!window.isCoplanar()) {
             stackedWindows.remove(window);
             stackedWindows.addLast(window);
             validate();
         }
     }
 
     /**
      * Move the given window so that it is above the given sibling window in the stack. If sibling is null,
      * this is the same as restackToTop. This is ignored for coplanar windows.
      * @param window The window to be moved.
      */
     public synchronized void restackAbove(Window2D window, Window2D sibling) {
         if (!window.isCoplanar()) {
             if (sibling == null) {
                 restackToTop(window);
             } else {
                 int idx = stackedWindows.indexOf(sibling);
                 if (idx <= 0) {
                     stackedWindows.addFirst(window);
                 } else {
                     stackedWindows.add(idx, window);
                 }
             }
             validate();
         }
     }
 
     /**
      * Move the given window so that it is below the given sibling window in the stack. If sibling is null,
      * this is the same as restackToBottom. This is ignored for coplanar windows.
      * @param window The window to be moved.
      */
     public synchronized void restackBelow(Window2D window, Window2D sibling) {
         if (!window.isCoplanar()) {
             if (sibling == null) {
                 restackToBottom(window);
             } else {
                 int idx = stackedWindows.indexOf(sibling);
                 if (idx < 0 || idx >= stackedWindows.size()-1) {
                     stackedWindows.addLast(window);
                 } else {
                     stackedWindows.add(idx+1, window);
                 }
             }
             validate();
         }
     }
 
 
     /** 
      * Return the top window of the stack.
      */
     public synchronized Window2D getTop() {
         try {
             return stackedWindows.getFirst();
         } catch (NoSuchElementException ex) {
             return null;
         }
     }
 
     /** 
      * Return the bottom window of the stack.
      */
     public synchronized Window2D getBottom() {
         try {
             return stackedWindows.getLast();
         } catch (NoSuchElementException ex) {
             return null;
         }
     }
 
     /** 
      * Returns the window above the given window in the stack. Returns null if the 
      * given window is at the top of the stack.
      */
     public synchronized Window2D getAbove(Window2D window) {
         if (window == null) return null;
         if (window.isCoplanar()) {
             return getAbove(window.getParent());
         } else {
             int idx = stackedWindows.indexOf(window);
             if (idx <= 0) {
                 return null;
             } else {
                 return stackedWindows.get(idx-1);
             }
         }
     }
 
     /** 
      * Returns the window below the given window in the stack. Returns null if the 
      * given window is at the bottom of the stack.
      */
     public synchronized Window2D getBelow(Window2D window) {
         if (window == null) return null;
         if (window.isCoplanar()) {
             return getBelow(window.getParent());
         } else {
             int idx = stackedWindows.indexOf(window);
             if (idx < 0 || idx >= stackedWindows.size()-1) {
                 return null;
             } else {
                 return stackedWindows.get(idx+1);
             }
         }
     }
 
     /**
      * Recalculate the Z orders of all stacked windows based on their position in he
      * list of stacked windows. And recalculate the Z orders of the coplanar windows
      * based on the Z order of their parents. If a coplanar window doesn't have a parent
      * set its zOrder to 0.
      */
     private void validate () {
 
         // Traverse the stacked windows from top to bottom
         int zOrderNext = 0;
         for (Window2D window : stackedWindows) {
            // TODO: TEMP WORKAROUND: fix invalid texcoord updates gt: click left on File
            //window.setZOrder(zOrderNext);
             zOrderNext++;
         }
 
         // Traverse coplanar wins 
         for (Window2D window : coplanarWindows) {
             Window2D stackedParent = findFirstStackedParent(window);
             if (stackedParent == null) {
                 window.setZOrder(0);
             } else {
                 window.setZOrder(stackedParent.getZOrder());                
             }
         }
     }
 
     /** 
      * Searching upward in the given window's parent chain, return the first non-coplanar 
      * window encountered. 
      */
     private Window2D findFirstStackedParent (Window2D window) {
         Window2D parent = window.getParent();
         while (parent != null && parent.isCoplanar()) {
             parent = window.getParent();
         }
         return parent;
     }
 
     /**
      * Recalculate the stack positions of all non-coplanar windows in the stack based on the 
      * current Z order attributes of the windows. Used during conventional window 
      * slave synchronization.
      */
     public synchronized void restackFromZOrders () {
         try {
             Collections.sort(stackedWindows, new ComparatorImpl ());
         } catch (Exception ex) {
             RuntimeException re = new RuntimeException("Error during window zOrder sort");
             re.initCause(ex);
             throw re;
         }
     }
 
     private static class ComparatorImpl implements Comparator<Window2D> {
 
         public ComparatorImpl() {
         }
 
         public int compare(Window2D window1, Window2D window2) {
             int zOrder1 = window1.getZOrder();
             int zOrder2 = window2.getZOrder();
             if (zOrder1 < zOrder2) {
                 return -1;
             } else if (zOrder1 > zOrder2) {
                 return 1;
             } else {
                 return 0;
             }
         }
         /* TODO: really needed?
         public boolean equals(Object obj) {
         return false;
         }
          */
     }
 }
