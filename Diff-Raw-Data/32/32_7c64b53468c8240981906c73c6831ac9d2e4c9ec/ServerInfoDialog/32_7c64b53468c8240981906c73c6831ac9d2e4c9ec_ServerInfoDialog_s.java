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
 
 package com.dmdirc.addons.lagdisplay;
 
 import com.dmdirc.ServerState;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPanel;
 import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPopupWindow;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
 
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 /**
  * Shows information about all connected servers.
  */
 public class ServerInfoDialog extends StatusbarPopupWindow {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 3;
     /** The lag display manager. */
     protected final LagDisplayManager manager;
    /** Swing main frame. */
    private final MainFrame mainFrame;
     /** Server manager to retrieve servers from. */
     private final ConnectionManager connectionManager;
 
     /**
      * Creates a new ServerInfoDialog.
      *
      * @param manager       The {@link LagDisplayManager} we're using for info
      * @param parent        The {@link JPanel} to use for positioning
      * @param mainFrame     The frame that will own this dialog.
      * @param connectionManager The manager to use to iterate servers.
      */
     public ServerInfoDialog(
             final LagDisplayManager manager,
             final StatusbarPanel<JLabel> parent,
             final MainFrame mainFrame,
             final ConnectionManager connectionManager) {
         super(parent, mainFrame);
 
         this.manager = manager;
        this.mainFrame = mainFrame;
         this.connectionManager = connectionManager;
     }
 
     @Override
     protected void initContent(final JPanel panel) {
         final List<Connection> connections = connectionManager.getConnections();
 
         if (connections.isEmpty()) {
             panel.add(new JLabel("No open servers."));
         } else {
             if (manager.shouldShowGraph()) {
                panel.add(new PingHistoryPanel(manager, mainFrame), "span, grow, wrap");
                 panel.add(new JSeparator(), "span, grow, wrap");
             }
 
             for (Connection connection : connections) {
                 panel.add(new JLabel(connection.getAddress()));
                 panel.add(new JLabel(
                         connection.getState() == ServerState.CONNECTED
                                 ? connection.getNetwork() : "---",
                         SwingConstants.CENTER), "grow");
                 panel.add(new JLabel(connection.getState() == ServerState.CONNECTED
                         ? manager.getTime(connection) : "---", SwingConstants.RIGHT), "grow, wrap");
             }
         }
     }
 
 }
