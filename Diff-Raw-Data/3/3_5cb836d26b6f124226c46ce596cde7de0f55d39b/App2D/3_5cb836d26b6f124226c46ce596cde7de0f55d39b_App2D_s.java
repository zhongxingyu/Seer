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
 
 import com.jme.math.Vector2f;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.InternalAPI;
 import org.jdesktop.wonderland.modules.appbase.client.cell.view.View2DCellFactory;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DSet;
 
 /**
  * The generic 2D application superclass. All 2D apps in Wonderland have this
  * root class.
  * <br><br>
  * 2D apps provide a window stack in which to arrange their visible windows.
  * This stack is a list. The top window in the stack is first in the list.
  * The bottom window is the last in the list. 
  * <br><br>
  * The stack position index of the top window is N-1, where N is the number of windows. 
  * The stack position of the bottom window is 0.
  *
  * In order to display this app you must add a displayer to it. Once added, as windows of the
  * app become visible they will be displayed in the displayer.
  *
  * @author deronj
  */
 @ExperimentalAPI
 public abstract class App2D {
 
     private static final Logger logger = Logger.getLogger(App2D.class.getName());
 
     /** All of the apps which have been created by this client. */
     private static LinkedList<App2D> apps = new LinkedList<App2D>();
 
     /** The global default appbase View2DCell factory.*/
     private static View2DCellFactory view2DCellFactory;
 
     /** The window stack for this app */
     private WindowStack stack = new WindowStack();
 
     /** The world size of pixels */
     protected Vector2f pixelScale;
 
     /** The primary window of this app. */
     private Window2D primaryWindow;
 
     /** The list of all windows created by this app */
     protected LinkedList<Window2D> windows = new LinkedList<Window2D>();
 
     /** The control arbiter for this app. */
     protected ControlArb controlArb;
 
     /** The focus entity of the app. */
     protected Entity focusEntity;
 
     /** The name of the app. */
     private String name;
 
     /** The set of all views of the windows of this app. */
     private View2DSet viewSet = new View2DSet();
 
     // Register the appbase shutdown hook
     static {
         Runtime.getRuntime().addShutdownHook(new Thread("App Base Shutdown Hook") {
             public void run() { App2D.shutdown(); }
         });
     }
 
     /**
      * Set the default View2DCell factory to be used for all apps in this client. (Called by 
      * AppClientPlugin.initialize).
      */
     static void setView2DCellFactory (View2DCellFactory vFactory) {
         view2DCellFactory = vFactory;
     }
 
     /** Returns the default View2DCell factory. */
     public static View2DCellFactory getView2DCellFactory () {
         return view2DCellFactory;
     }
 
     /** Returns whether the app is running in a SAS Provider. */
     public static boolean isInSas () {
         return getView2DCellFactory() == null;
     }
 
     /**
      * Create a new instance of App2D with a default name..
      *
      * @param controlArb The control arbiter to use. Must be non-null.
      * @param pixelScale The size of the window pixels in world coordinates.
      */
     public App2D(ControlArb controlArb, Vector2f pixelScale) {
         this(null, controlArb, pixelScale);
     }
 
     /**
      * Create a new instance of App2D with the given name.
      *
      * @param name The name of the app.
      * @param controlArb The control arbiter to use. Must be non-null.
      * @param pixelScale The size of the window pixels in world coordinates.
      */
     public App2D(String name, ControlArb controlArb, Vector2f pixelScale) {
         this.name = name;
         if (controlArb == null) {
             throw new RuntimeException("controlArb argument must be non-null.");
         }
         this.controlArb = controlArb;
         this.pixelScale = pixelScale;
         focusEntity = new Entity("App focus entity for app " + getName());
 
         synchronized(apps) {
             apps.add(this);
         }
     }
 
     /**
      * Deallocate resources.
      */
     public void cleanup() {
         viewSet.cleanup();
         if (controlArb != null) {
             controlArb.cleanup();
             controlArb = null;
         }
         stack.cleanup();
         LinkedList<Window2D> toRemoveList = (LinkedList<Window2D>) windows.clone();
         for (Window2D window : toRemoveList) {
             window.cleanup();
         }
         windows.clear();
         toRemoveList.clear();
         pixelScale = null;
     }
 
     /** 
      * Returns the pixel scale 
      */
     public Vector2f getPixelScale() {
         return new Vector2f(pixelScale);
     }
 
     WindowStack getWindowStack () {
         return stack;
     }
 
     /**
      * Add a new displayer to this app. This displays all existing windows of the
      * app in the displayer.
      */
     public void addDisplayer (View2DDisplayer displayer) {
         viewSet.add(displayer);
     }
 
     /**
      * Remove a displayer to this app.
      */
     public void removeDisplayer (View2DDisplayer displayer) {
         viewSet.remove(displayer);
     }
 
     /**
      * Returns an iterator over all the displayers of this app.
      */
     public Iterator<View2DDisplayer> getDisplayers() {
         return viewSet.getDisplayers();
     }
     /**
      * Add a window to this app. 
      * @param window The window to add.
      */
     public synchronized void addWindow(Window2D window) {
         windows.add(window);
         viewSet.add(window);
     }
 
     /**
      * Remove the given window from this app.
      * @param window The window to remove.
      */
     public void removeWindow(Window2D window) {
         viewSet.remove(window);
         windows.remove(window);
         if (window == primaryWindow) {
             setPrimaryWindow(null);
         }
     }
 
     /**
      * Returns the number of windows in this app.
      */
     public int getNumWindows () {
         return windows.size();
     }
 
     /**
      * Specify the primary window. This also reparents existing secondaries to this new primary.
      */
     public void setPrimaryWindow (Window2D primaryWindow) {
         if (this.primaryWindow == primaryWindow) return;
 
         /* Make current primary window secondary
          TODO: not yet supported
         if (this.primaryWindow != null) {
             this.primaryWindow.setType(Window2D.Type.SECONDARY);
         }
         */
 
         this.primaryWindow = primaryWindow;
         logger.info("set primary window to " + primaryWindow);
 
         for (Window2D window : windows) {
             if (window.getType() == Window2D.Type.SECONDARY) {
                 window.setParent(primaryWindow);
             }
         }
     }
 
     /**
      * Returns the primary window.
      */
     public Window2D getPrimaryWindow () {
         return primaryWindow;
     }
 
     /**
      * Recalculate the stack order based on the desired Z orders of the windows in the stack.
      * Used during slave synchronization of conventional apps.
      */
     public void updateSlaveWindows () {
         stack.restackFromDesiredZOrders();
         changedStackAllWindows();
     }
 
     /**
      * Tell all windows that their stack order may have changed.
      */
     private void changedStackAllWindows () {
         for (Window2D window : windows) {
             window.changedStack();
         }
     }
 
     /**
      * Tell all non-coplanar windows (except the argument window) that their stack order may have changed.
      */
     public void changedStackAllWindowsExcept (Window2D windowExcept) {
         for (Window2D window : windows) {
             if (window != windowExcept) {
                 if (!window.isCoplanar()) {
                     window.changedStack();
                 }
             }
         }
     }
 
     /**
      * Returns the focus entity of the app.
      */
     @InternalAPI
     public Entity getFocusEntity () {
         return focusEntity;
     }
 
     /**
      * Returns the Control Arbiter for this app.
      * If this is null the app supports fine-grained control swapping.
      * That is, the app accepts user events from different users equally
      * on a first-come first-served basis.
      */
     public ControlArb getControlArb() {
         return controlArb;
     }
 
     /**
      * Returns an iterator over all the windows of this app.
      */
     public Iterator<Window2D> getWindows() {
         return windows.iterator();
     }
 
     /**
      * Returns the name of the app.
      */
     public String getName () {
         if (name == null) {
             return "(Unnamed App)";
         } else {
             return name;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public String toString () {
         return getName();
     }
 
     /** Executed by the JVM shutdown process. */
     private static void shutdown () {
         logger.warning("Shutting down app base...");
 
         // Note: I tried to run this in a synchronized block, but it hung.
        for (App2D app : apps) {
             logger.warning("Shutting down app " + app);
             app.cleanup();
         }
         logger.warning("Done shutting down apps.");
 
         apps.clear();
         logger.warning("Done shutting down app base.");
     }
 }
