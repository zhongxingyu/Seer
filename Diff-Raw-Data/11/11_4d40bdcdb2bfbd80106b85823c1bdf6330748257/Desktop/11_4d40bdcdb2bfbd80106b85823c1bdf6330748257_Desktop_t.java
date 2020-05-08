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
 
 package com.quartercode.disconnected.sim.comp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlIDREF;
 import com.quartercode.disconnected.graphics.desktop.DesktopWidget;
 import com.quartercode.disconnected.graphics.desktop.Frame;
 
 /**
  * A desktop can hold and display windows.
  * This class holds the windows, another class in the graphics-package renders them.
  * 
  * @see Window
  */
 public class Desktop {
 
     @XmlIDREF
     @XmlAttribute
    private OperatingSystem          host;
     @XmlElementWrapper (name = "windows")
     @XmlElement (name = "window")
     private final List<Window>       windows       = new ArrayList<Window>();
     private final Set<DesktopWidget> pushReceivers = new HashSet<DesktopWidget>();
 
     /**
     * Creates a new empty desktop.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    public Desktop() {

    }

    /**
      * Creates a new desktop.
      * 
      * @param host The host operating system which uses this desktop.
      */
     public Desktop(OperatingSystem host) {
 
         this.host = host;
     }
 
     /**
      * Returns the host operating system which uses this desktop.
      * 
      * @return The host operating system which uses this desktop.
      */
     public OperatingSystem getHost() {
 
         return host;
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
     public void addWindow(Window window) {
 
         if (window.isClosed()) {
             throw new IllegalStateException("Can't add window: Window already closed");
         } else if (!windows.contains(window)) {
             windows.add(window);
 
             for (DesktopWidget pushReceiver : pushReceivers) {
                 pushReceiver.callAddWindow(window);
             }
         }
     }
 
     /**
      * Removes a window from the desktop.
      * The window wont be just minimized, it will be removed until it gets added again.
      * 
      * @param window The window to remove from the desktop.
      */
     public void removeWindow(Window window) {
 
         if (windows.contains(window)) {
             window.closed = true;
             windows.remove(window);
 
             for (DesktopWidget pushReceiver : pushReceivers) {
                 pushReceiver.callRemoveWindow(window);
             }
         }
     }
 
     /**
      * Adds a desktop widget push receiver to the desktop.
      * In this case, push receivers are graphical desktop widgets and render the windows of a desktop.
      * 
      * @param pushReceiver The desktop widget push receiver to add to the desktop.
      */
     public void addPushReceiver(DesktopWidget pushReceiver) {
 
         pushReceivers.add(pushReceiver);
     }
 
     /**
      * Removes a desktop widget push receiver from the desktop.
      * In this case, push receivers are graphical desktop widgets and render the windows of a desktop.
      * 
      * @param pushReceiver The desktop widget push receiver to remove from the desktop.
      */
     public void removePushReceiver(DesktopWidget pushReceiver) {
 
         pushReceivers.remove(pushReceiver);
     }
 
     /**
      * A window represents a frame widget as a lightweight wrapper.
      * Classes on the outside can change visible parameters of the window, like the name in the taskbar or the frame title.
      * 
      * @see Frame
      */
     public static class Window {
 
         private String  name;
         private String  title;
         private boolean closed;
 
         private Frame   frame;
 
         /**
          * Creates a new empty window.
          * This is only recommended for direct field access (e.g. for serialization).
          */
         public Window() {
 
         }
 
         /**
          * Creates a new window wrapping around the given frame.
          * 
          * @param frame The frame the new window wraps around.
          */
         public Window(Frame frame) {
 
             this.frame = frame;
             setVisible(true);
         }
 
         /**
          * Creates a new window with the given name and title wrapping around the given frame.
          * 
          * @param frame The frame the new window wraps around.
          * @param name The name for the window which will be displayed in the taskbar.
          * @param title The title for the window which will be displayed in the title bar of the frame.
          */
         public Window(Frame frame, String name, String title) {
 
             this(frame);
 
             setName(name);
             setTitle(title);
         }
 
         /**
          * Returns the name for the window which will be displayed in the taskbar.
          * 
          * @return The name for the window which will be displayed in the taskbar.
          */
         public String getName() {
 
             return name;
         }
 
         /**
          * Sets the name for the window to a new one.
          * The name will be displayed in the taskbar.
          * 
          * @param name The new name for the window.
          */
         public void setName(String name) {
 
             this.name = name;
             frame.setName(name);
         }
 
         /**
          * Returns the title for the window which will be displayed in the title bar of the frame.
          * 
          * @return The title for the window which will be displayed in the title bar of the frame.
          */
         public String getTitle() {
 
             return title;
         }
 
         /**
          * Sets the title for the window to a new one.
          * The title will be displayed in the title bar of the frame
          * 
          * @param title The new title for the window.
          */
         public void setTitle(String title) {
 
             this.title = title;
             frame.setTitle(title);
         }
 
         /**
          * Returns if the window is closed.
          * 
          * @return If the window is closed.
          */
         public boolean isClosed() {
 
             return closed;
         }
 
         /**
          * Returns the frame this window wraps around.
          * The frame object should only be used to add hooks.
          * 
          * @return The frame this window wraps around.
          */
         public Frame getFrame() {
 
             return frame;
         }
 
         /**
          * Returns the class the frame has.
          * This is only recommended for serialization.
          * 
          * @return the class the frame has.
          */
         public Class<? extends Frame> getFrameClass() {
 
             return frame.getClass();
         }
 
         /**
          * Sets the class for the frame to a new one and creates a new frame.
          * This is only recommended for serialization.
          * 
          * @param c The class for the new frame.
          * @throws InstantiationException Something goes wrong while creating the new frame object.
          * @throws IllegalAccessException The class for the new frame object is not accessable.
          */
         public void setFrameClass(Class<? extends Frame> c) throws InstantiationException, IllegalAccessException {
 
             frame = c.newInstance();
             if (name != null) {
                 frame.setName(name);
             }
             if (title != null) {
                 frame.setTitle(title);
             }
         }
 
         /**
          * Returns the visibility of the wrapped frame.
          * An invisible frame is equal to a minimized one.
          * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
          * 
          * @return The visibility of the wrapped frame.
          */
         public boolean isVisible() {
 
             return frame.isVisible();
         }
 
         /**
          * Changes the visibility of the wrapped frame.
          * Making a frame invisible is equal to minimizing it.
          * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
          * 
          * @param visible Determinates if the wrapped frame should be visible.
          */
         public void setVisible(boolean visible) {
 
             frame.setVisible(visible);
         }
 
     }
 
 }
