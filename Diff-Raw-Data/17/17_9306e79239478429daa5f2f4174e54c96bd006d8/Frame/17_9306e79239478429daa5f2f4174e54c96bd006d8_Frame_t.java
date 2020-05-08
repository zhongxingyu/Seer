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
 
 import org.apache.commons.lang.Validate;
 import com.quartercode.disconnected.graphics.component.MultiactionButton;
 import com.quartercode.disconnected.sim.comp.Desktop;
 import com.quartercode.disconnected.sim.comp.Desktop.Window;
 import de.matthiasmann.twl.ResizableFrame;
 import de.matthiasmann.twl.Widget;
 
 /**
  * A frame is the displayed component of a desktop window.
  * The frame widget renders the visible part of the frame onto the window area.
  * 
  * @see Window
  */
 public class Frame extends ResizableFrame {
 
     private final MultiactionButton taskbarButton;
 
     /**
      * Creates and prepares a new frame.
      */
     public Frame() {
 
         setTheme("frame");
 
         taskbarButton = new MultiactionButton();
         setVisible(true);
     }
 
     /**
      * Returns the name of the frame.
      * The name will be displayed in the taskbar.
      * 
      * @return The name of the frame.
      */
     public String getName() {
 
         return taskbarButton.getText();
     }
 
     /**
      * Sets the name of the frame to a new one.
      * The name will be displayed in the taskbar.
      * 
      * @param name The new name of the frame.
      */
     public void setName(String name) {
 
         taskbarButton.setText(name);
     }
 
     /**
      * Returns the title of the frame.
      * The title will be displayed in the title bar of the frame
      * 
      * @return The title of the frame.
      */
     @Override
     public String getTitle() {
 
         return super.getTitle();
     }
 
     /**
      * Sets the title of the frame to a new one.
      * The title will be displayed in the title bar of the frame
      * 
      * @param title The new title of the frame.
      */
     @Override
     public void setTitle(String title) {
 
         super.setTitle(title);
     }
 
     /**
      * Changes the visibility of the frame.
      * Making a frame invisible is equal to minimizing it.
      * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
      * 
      * @param visible Determinates if the frame should be visible.
      */
     @Override
     public void setVisible(boolean visible) {
 
         if (visible != isVisible()) {
             super.setVisible(visible);
 
             if (visible) {
                 requestKeyboardFocus();
             }
         }
 
         taskbarButton.setTheme(visible ? "taskbar-button-active" : "taskbar-button-inactive");
         taskbarButton.reapplyTheme();
     }
 
     /**
      * Returns the taskbar button which represents the frame.
      * The taskbar button object should only be used to add hooks.
      * 
      * @return the taskbar button which represents the frame.
      */
     protected MultiactionButton getTaskbarButton() {
 
         return taskbarButton;
     }
 
     /**
      * Sets the center of the frame to the given relative coordinates (0 - 1).
      * 
      * @param x The new relative x center of the frame (0 - 1).
      * @param y The new relative y center of the frame (0 - 1).
      */
     public void center(float x, float y) {
 
         Validate.isTrue(x >= 0 && x <= 1, "Relative x coordinate must be in range 0 <= x <= 1: " + x);
         Validate.isTrue(y >= 0 && y <= 1, "Relative y coordinate must be in range 0 <= y <= 1: " + y);
 
         Widget parent = getParent();
         if (parent != null) {
             setPosition(parent.getInnerX() + (int) ( (parent.getInnerWidth() - getWidth()) * x), parent.getInnerY() + (int) ( (parent.getInnerHeight() - getHeight()) * y));
         }
     }
 
 }
