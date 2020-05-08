 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.osd;
 
 import com.dmdirc.config.IdentityManager;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * Class to manage OSD Windows.
  *
  * @author Simon
  * @since 0.6.3
  */
 public class OsdManager {
 
     /** The Plugin that owns this OSD Manager. */
     private final OsdPlugin plugin;
 
     /** List of OSD Windows. */
     private final List<OsdWindow> windowList = new ArrayList<OsdWindow>();
 
     /** List of messages to be queued. */
     private final Queue<String> windowQueue = new LinkedList<String>();
 
     /** The spacing between the windows. */
     private static final int WINDOW_GAP = 5;
 
     /**
      * Create a new OSD Manager.
      *
      * @param plugin The plugin that owns this OSD Manager.
      */
     public OsdManager(final OsdPlugin plugin) {
         this.plugin = plugin;
     }
 
     /**
      * Add messages to the queue and call displayWindows.
      *
      * @param message Message to be displayed.
      */
     public void showWindow(final String message) {
         windowQueue.add(message);
         displayWindows();
     }
 
     /**
      * Displays as many windows as appropriate.
      */
     private synchronized void displayWindows() {
         final Integer maxWindows = IdentityManager.getGlobalConfig().getOptionInt(
                 plugin.getDomain(), "maxWindows");
 
         String nextItem;
 
         while ((maxWindows == null || getWindowCount() < maxWindows)
                 && (nextItem = windowQueue.poll()) != null) {
             displayWindow(nextItem);
         }
     }
 
     /**
      * Create a new OSD window with "message".
      *
      * @param message Text to display in the OSD window.
      */
     private void displayWindow(final String message) {
         OsdWindow currentWindow = new OsdWindow(message, false,
                 IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                 "locationX"), getYPosition(), plugin, this);

         windowList.add(currentWindow);
     }
 
     /**
      * Destroy the given OSD Window and check if the Queue has items, if so
      * Display them.
      *
      * @param window The window that we are destroying.
      */
     public void closeWindow(final OsdWindow window) {
         windowList.remove(window);
         window.dispose();
 
         displayWindows();
     }
 
     /**
      * Destroy all OSD Windows.
      */
     public void closeAll() {
         for (OsdWindow window : new ArrayList<OsdWindow>(windowList)) {
             window.setVisible(false);
             closeWindow(window);
         }
     }
 
     /**
      * Get the list of current OSDWindows.
      *
      * @return a List of all currently open OSDWindows.
      */
     public List<OsdWindow> getWindowList() {
         return new ArrayList<OsdWindow>(windowList);
     }
 
     /**
      * Get the count of open windows.
      *
      * @return Current number of OSD Windows open.
      */
     public int getWindowCount() {
         return windowList.size();
     }
 
     /**
      * Get the Y position for the next window.
      *
      * @return the Y position for the next window.
      */
    public int getYPosition() {
         final String policy = IdentityManager.getGlobalConfig().getOption(
                 plugin.getDomain(), "newbehaviour");
         int y = IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                 "locationY");
 
         if ("down".equals(policy)) {
             // Place our new window below old windows
             for (OsdWindow window : new ArrayList<OsdWindow>(getWindowList())) {
                 if (window.isVisible()) {
                     y = Math.max(y, window.getY() + window.getHeight() + WINDOW_GAP);
                 }
             }
         } else if ("up".equals(policy)) {
             // Place our new window above old windows
             for (OsdWindow window : new ArrayList<OsdWindow>(getWindowList())) {
                 if (window.isVisible()) {
                     y = Math.min(y, window.getY() - window.getHeight() - WINDOW_GAP);
                 }
             }
         } else if ("close".equals(policy)) {
             // Close existing windows and use their place
             closeAll();
         }
 
         return y;
     }
 }
