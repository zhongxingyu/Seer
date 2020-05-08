 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.framemanager.windowmenu;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.SelectionListener;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
 import com.dmdirc.events.FrameIconChangedEvent;
 import com.dmdirc.events.FrameNameChangedEvent;
 import com.dmdirc.ui.messages.Styliser;
 
 import com.google.common.base.Optional;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 import javax.swing.SwingUtilities;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Action representing a frame.
  */
 public class FrameContainerMenuItem extends JMenuItem implements ActionListener, SelectionListener,
         FrameContainerMenuInterface {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Active frame manager. */
     private final ActiveFrameManager activeFrameManager;
     /** Wrapped frame. */
     private final FrameContainer frame;
     /** Swing window. */
     private final TextFrame window;
     /** Parent window menu frame manager. */
     private final WindowMenuFrameManager manager;
 
     /**
      * Instantiates a new FrameContainer menu item wrapping the specified frame.
      *
      * @param activeFrameManager The active window manager
      * @param frame              Wrapped frame
      * @param window             The window this menu item corresponds to.
      * @param manager            Parent window menu frame manager.
      */
     public FrameContainerMenuItem(
             final ActiveFrameManager activeFrameManager,
             final FrameContainer frame,
             final TextFrame window,
             final WindowMenuFrameManager manager) {
         super(frame.getName(), frame.getIconManager().getIcon(frame.getIcon()));
 
         this.activeFrameManager = activeFrameManager;
         this.frame = frame;
         this.window = window;
         this.manager = manager;
 
         addActionListener(this);
     }
 
     @Handler
     public void iconChanged(final FrameIconChangedEvent event) {
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                if (frame != null && window != null && frame.equals(window.getContainer())) {
                     setIcon(window.getIconManager().getIcon(event.getIcon()));
                 }
             }
         });
     }
 
     @Handler
     public void nameChanged(final FrameNameChangedEvent event) {
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                if (frame != null && window != null && frame.equals(window.getContainer())) {
                     setText(Styliser.stipControlCodes(event.getName()));
                 }
             }
         });
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         activeFrameManager.setActiveFrame(window);
     }
 
     @Override
     public void selectionChanged(final TextFrame window) {
         if (frame.equals(window.getContainer())) {
             setFont(getFont().deriveFont(Font.BOLD));
             final Optional<FrameContainer> parentWindow = window.getContainer().getParent();
             if (parentWindow.isPresent()) {
                 manager.parentSelection(parentWindow.get());
             }
         } else {
             setFont(getFont().deriveFont(Font.PLAIN));
         }
     }
 
     @Override
     public FrameContainer getFrame() {
         return frame;
     }
 
 }
