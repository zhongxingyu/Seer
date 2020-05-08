 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.graphics.desktop;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import com.quartercode.disconnected.graphics.component.GraphicsState;
 import de.matthiasmann.twl.BoxLayout;
 import de.matthiasmann.twl.BoxLayout.Direction;
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.DesktopArea;
 import de.matthiasmann.twl.Event;
 import de.matthiasmann.twl.GUI;
 
 /**
  * A desktop can display windows on his window area. Every window has a frame widget which gets rendered.
  * 
  * @see Window
  */
 public class Desktop extends GraphicsState {
 
     private final List<Window> windows = new ArrayList<Window>();
 
     public DesktopArea         windowArea;
     public BoxLayout           taskbar;
     public Button              launchButton;
 
     /**
      * Creates a new desktop and sets it up.
      */
     public Desktop() {
 
         super("/ui/desktop.xml");
 
         setTheme("");
 
         windowArea = new DesktopArea();
         windowArea.setTheme("");
         add(windowArea);
 
         taskbar = new BoxLayout(Direction.HORIZONTAL);
         taskbar.setSpacing(5);
         taskbar.setTheme("");
         add(taskbar);
 
         launchButton = new Button();
         launchButton.setTheme("/launch-button");
         launchButton.setText("Launch");
         launchButton.addCallback(new Runnable() {
 
             @Override
             public void run() {
 
                 // TODO: Display launch menu
             }
         });
         add(launchButton);
     }
 
     /**
      * Returns a list of all windows this desktop currently holds (all visible and invisible windows).
      * 
      * @return A list of all windows this desktop currently holds (all visible and invisible windows).
      */
     public List<Window> getWindows() {
 
         return Collections.unmodifiableList(windows);
     }
 
     /**
      * Adds a new window to the desktop.
      * The new window will be visible if the render frame is visible.
      * 
      * @param window The new window to add to the desktop.
      */
     public void addWindow(final Window window) {
 
         if (!windows.contains(window)) {
             window.getFrame().addCloseCallback(new Runnable() {
 
                 @Override
                 public void run() {
 
                     removeWindow(window);
                 }
             });
             window.getTaskbarButton().addCallback(Event.MOUSE_RBUTTON, new Runnable() {
 
                 @Override
                 public void run() {
 
                     removeWindow(window);
                 }
             });
             window.getTaskbarButton().addCallback(Event.MOUSE_LBUTTON, new Runnable() {
 
                 @Override
                 public void run() {
 
                    window.setVisible(!window.isVisible());
                 }
             });
 
             windows.add(window);
 
             windowArea.add(window.getFrame());
             windowArea.invalidateLayout();
 
             taskbar.add(window.getTaskbarButton());
             taskbar.invalidateLayout();
         }
     }
 
     /**
      * Removes a window from the desktop.
      * The window wont be just minimized, it will be removed until it gets added again.
      * 
      * @param window The window to remove from the desktop.
      */
     public void removeWindow(Window window) {
 
         windows.remove(window);
 
         windowArea.removeChild(window.getFrame());
         windowArea.invalidateLayout();
 
         taskbar.removeChild(window.getTaskbarButton());
         taskbar.invalidateLayout();
     }
 
     @Override
     protected void layout() {
 
         windowArea.setSize(getParent().getWidth(), getParent().getHeight());
 
         launchButton.adjustSize();
         launchButton.setPosition(10, getParent().getHeight() - launchButton.getHeight() - 5);
 
         taskbar.adjustSize();
         taskbar.setPosition(launchButton.getWidth() + 30, getParent().getHeight() - taskbar.getHeight() - 5);
     }
 
     @Override
     protected void afterAddToGUI(GUI gui) {
 
         super.afterAddToGUI(gui);
 
         setTheme("desktop");
         taskbar.validateLayout();
     }
 
 }
