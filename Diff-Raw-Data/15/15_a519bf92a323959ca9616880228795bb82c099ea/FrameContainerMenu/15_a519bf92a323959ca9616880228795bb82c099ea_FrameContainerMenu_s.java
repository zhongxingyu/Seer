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
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenu;
 import javax.swing.SwingUtilities;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Frame container JMenu.
  */
 public class FrameContainerMenu extends JMenu implements ActionListener, SelectionListener,
         FrameContainerMenuInterface {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Active frame manager. */
     private final ActiveFrameManager activeFrameManager;
     /** The swing frame. */
     private final TextFrame window;
     /** Wrapped frame. */
     private final FrameContainer frame;
 
     /**
      * Instantiates a new FrameContainer menu item wrapping the specified frame.
      *
      * @param activeFrameManager The active window manager
      * @param globalConfig       The config to read settings from
      * @param domain             The domain to read settings from
      * @param window             The swing window being wrapped
      * @param frame              Wrapped frame
      */
     public FrameContainerMenu(
             final ActiveFrameManager activeFrameManager,
             final AggregateConfigProvider globalConfig,
             final String domain,
             final TextFrame window,
             final FrameContainer frame) {
         super(frame.getName());
 
         this.activeFrameManager = activeFrameManager;
         this.window = window;
         this.frame = frame;
 
         setIcon(frame.getIconManager().getIcon(frame.getIcon()));
         new WindowMenuScroller(this, globalConfig, domain, 0);
 
         addActionListener(this);
     }
 
     @Handler
     public void iconChanged(final FrameIconChangedEvent event) {
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                if ((frame != null && window != null) && frame.equals(window)) {
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
                if ((frame != null && window != null) && frame.equals(window)) {
                     setText(event.getName());
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
         } else {
             setFont(getFont().deriveFont(Font.PLAIN));
         }
     }
 
     /**
      * Informs this menu one of its children is selected.
      */
     protected void childSelected() {
         setFont(getFont().deriveFont(Font.ITALIC));
     }
 
     @Override
     public FrameContainer getFrame() {
         return frame;
     }
 
 }
