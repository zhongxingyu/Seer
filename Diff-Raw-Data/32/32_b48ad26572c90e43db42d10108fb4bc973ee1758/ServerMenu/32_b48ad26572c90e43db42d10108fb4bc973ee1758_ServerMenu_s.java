 /*
  * Copyright (c) 2006-2013 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.menubar;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.Server;
 import com.dmdirc.ServerState;
 import com.dmdirc.addons.ui_swing.Apple;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.dialogs.NewServerDialog;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 
 /**
  * A menu providing server related commands to the menu bar.
  */
 @Singleton
 public class ServerMenu extends JMenu implements ActionListener,
         MenuListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Swing controller. */
     private final SwingController controller;
     /** Main frame. */
     private final MainFrame mainFrame;
     /** Menu items which can be enabled/disabled. */
     private JMenuItem ssd, disconnect;
 
     /**
      * Creates a new Server menu.
      *
      * @param controller Parent swing controller
      * @param mainFrame Parent main frame
      */
     @Inject
     public ServerMenu(
             final SwingController controller,
             final MainFrame mainFrame) {
         super("Server");
         this.controller = controller;
         this.mainFrame = mainFrame;
         setMnemonic('s');
         addMenuListener(this);
         initServerMenu();
         menuSelected(null);
     }
 
     /**
      * Initialises the server menu.
      */
     private void initServerMenu() {
         JMenuItem menuItem = new JMenuItem();
         menuItem.setText("New Server...");
         menuItem.setMnemonic('n');
         menuItem.setActionCommand("NewServer");
         menuItem.addActionListener(this);
         add(menuItem);
 
         disconnect = new JMenuItem();
         disconnect.setText("Disconnect");
         disconnect.setMnemonic('d');
         disconnect.setActionCommand("Disconnect");
         disconnect.addActionListener(this);
         add(disconnect);
 
         ssd = new JMenuItem();
         ssd.setMnemonic('s');
         ssd.setText("Server settings");
         ssd.setActionCommand("ServerSettings");
         ssd.addActionListener(this);
         add(ssd);
 
         if (!Apple.isAppleUI()) {
             menuItem = new JMenuItem();
             menuItem.setText("Exit");
             menuItem.setMnemonic('x');
             menuItem.setActionCommand("Exit");
             menuItem.addActionListener(this);
             add(menuItem);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void actionPerformed(final ActionEvent e) {
         switch (e.getActionCommand()) {
             case "NewServer":
                 controller.showDialog(NewServerDialog.class);
                 break;
             case "Exit":
                 mainFrame.quit();
                 break;
             case "ServerSettings":
                 controller.showServerSettingsDialog(
                         (Server) mainFrame.getActiveFrame().getContainer().getConnection());
                 break;
             case "Disconnect":
                 mainFrame.getActiveFrame().getContainer().getConnection().disconnect();
                 break;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public final void menuSelected(final MenuEvent e) {
         final TextFrame activeFrame = mainFrame.getActiveFrame();
         final FrameContainer activeWindow = activeFrame == null ? null
                 : activeFrame.getContainer();
 
        ssd.setEnabled(activeWindow != null && activeWindow
                .getConnection() != null && activeWindow.getConnection().getState()
                == ServerState.CONNECTED);
        disconnect.setEnabled(activeWindow != null && activeWindow
                .getConnection() != null && activeWindow.getConnection().getState()
                == ServerState.CONNECTED);
     }
 
     /** {@inheritDoc} */
     @Override
     public void menuDeselected(final MenuEvent e) {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     public void menuCanceled(final MenuEvent e) {
         // Do nothing
     }
 }
