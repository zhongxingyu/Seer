 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.events.SwingEventBus;
 import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
 import com.dmdirc.events.ServerInviteExpiredEvent;
 import com.dmdirc.events.ServerInviteReceivedEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.InviteManager;
 import com.dmdirc.addons.ui_swing.components.IconManager;
 
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.util.List;
 import java.util.Optional;
 
 import javax.inject.Inject;
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * A status bar component to show invites to the user and enable them to accept or dismiss them.
  */
 public class InviteLabel extends StatusbarPopupPanel<JLabel> {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Invite popup menu. */
     private final JPopupMenu menu;
     /** Dismiss invites menu item. */
     private final JMenuItem dismiss;
     /** Accept invites menu item. */
     private final JMenuItem accept;
     /** Parent window that will own popup windows. */
     private final Window parentWindow;
     /** The client event bus to use for invite events. */
     private final DMDircMBassador eventBus;
     /** The swing event bus to use for selection events. */
     private final SwingEventBus swingEventBus;
     /** Active connection. */
     private Optional<Connection> activeConnection;
 
     @Inject
     public InviteLabel(final DMDircMBassador eventBus, final IconManager iconManager,
             final MainFrame mainFrame, final SwingEventBus swingEventBus) {
         super(new JLabel());
 
         this.parentWindow = mainFrame;
         this.eventBus = eventBus;
         this.swingEventBus = swingEventBus;
         this.activeConnection = Optional.empty();
 
         setBorder(BorderFactory.createEtchedBorder());
         label.setIcon(iconManager.getIcon("invite"));
 
         menu = new JPopupMenu();
         dismiss = new JMenuItem("Dismiss all invites");
         dismiss.addActionListener(e -> activeConnection.map(Connection::getInviteManager)
                 .ifPresent(InviteManager::removeInvites));
         accept = new JMenuItem("Accept all invites");
         accept.addActionListener(e -> activeConnection.map(Connection::getInviteManager)
                 .ifPresent(InviteManager::acceptInvites));
     }
 
     /**
      * Initialises the invite label, adding appropriate listeners.
      */
     public void init() {
         swingEventBus.subscribe(this);
         eventBus.subscribe(this);
         update();
     }
 
     @Override
     protected StatusbarPopupWindow getWindow() {
         return new InvitePopup(this, activeConnection, parentWindow);
     }
 
     /**
      * Populates the menu.
      */
     private void popuplateMenu() {
         menu.removeAll();
 
         activeConnection
                 .map(Connection::getInviteManager)
                 .map(InviteManager::getInvites)
                 .ifPresent(invites -> invites.stream()
                         .map(InviteAction::new)
                         .map(JMenuItem::new)
                         .map(menu::add));
         menu.add(new JSeparator());
         menu.add(accept);
         menu.add(dismiss);
     }
 
     /**
      * Updates the invite label for the currently active server.
      */
     private void update() {
         if (activeConnection
                 .map(Connection::getInviteManager)
                 .map(InviteManager::getInvites)
                 .map(List::isEmpty).orElse(true)) {
             setVisible(false);
             closeDialog();
         } else {
             refreshDialog();
             setVisible(true);
         }
     }
 
     @Handler(invocation = EdtHandlerInvocation.class)
     public void handleInviteReceived(final ServerInviteReceivedEvent event) {
         update();
     }
 
     @Handler(invocation = EdtHandlerInvocation.class)
     public void handleInviteExpired(final ServerInviteExpiredEvent event) {
         update();
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         mouseClicked(e);
         popuplateMenu();
         if (menu.getComponentCount() > 0) {
             menu.show(this, e.getX(), e.getY());
         }
     }
 
     @Handler(invocation = EdtHandlerInvocation.class)
     public void selectionChanged(final SwingWindowSelectedEvent event) {
         if (event.getWindow().isPresent()) {
             activeConnection = event.getWindow().get().getContainer().getConnection();
         } else {
            activeConnection = Optional.empty();
         }
         update();
     }
 }
