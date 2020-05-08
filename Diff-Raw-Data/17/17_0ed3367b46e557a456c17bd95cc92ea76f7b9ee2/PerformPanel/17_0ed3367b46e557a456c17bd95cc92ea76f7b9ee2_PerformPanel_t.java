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
 
 package com.dmdirc.addons.ui_swing.dialogs.serversetting;
 
 import com.dmdirc.Server;
 import com.dmdirc.actions.Action;
 import com.dmdirc.actions.wrappers.PerformWrapper;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Perform panel.
  */
 public final class PerformPanel extends JPanel implements ActionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
 
     /** Parent server. */
     private final Server server;
 
     /** The action used for server performs. */
     private Action serverAction;
     /** The action used for network performs. */
     private Action networkAction;
     /** The action used for profile server performs. */
     private Action profileServerAction;
     /** The action used for profile network performs. */
     private Action profileNetworkAction;
 
     /** The server perform string. */
     private String serverPerform;
     /** The network perform string. */
     private String networkPerform;
     /** The profile's server perform string. */
     private String profileServerPerform;
     /** The profile's network perform string. */
     private String profileNetworkPerform;
 
     /** Network/server combo box. */
     private JComboBox target;
 
     /** Perform text area. */
     private JTextArea textarea;
 
     /** Active perform. */
     private int activePerform = 0;
 
     /**
      * Creates a new instance of IgnoreList.
      *
      * @param server Parent server
      */
     public PerformPanel(final Server server) {
         super();
         this.server = server;
 
         this.setOpaque(UIUtilities.getTabbedPaneOpaque());
         initComponents();
         addListeners();
         loadPerforms();
         populatePerform();
     }
 
     /** Initialises teh components. */
     private void initComponents() {
         setLayout(new MigLayout("fill"));
 
         final DefaultComboBoxModel model = new DefaultComboBoxModel();
         target = new JComboBox(model);
 
         model.addElement("Network perform (" + server.getNetwork()
                 + ") Any profile");
         model.addElement("Network perform (" + server.getNetwork()
                 + ") This profile (" + server.getProfile().getName() + ")");
        model.addElement("Server perform (" + server.getAddress()
                 + ") Any profile");
        model.addElement("Server perform (" + server.getAddress()
                 + ") This profile (" + server.getProfile().getName() + ")");
 
         add(target, "growx, pushx, wrap");
 
         textarea = new JTextArea();
         textarea.setColumns(40);
 
         add(new JScrollPane(textarea), "grow, push");
     }
 
     /** Adds listeners to the components. */
     private void addListeners() {
         target.addActionListener(this);
     }
 
 
     /** Loads the perform actions. */
     private void loadPerforms() {
         serverAction = PerformWrapper.getPerformWrapper().
                getActionForServer(server.getAddress());
         networkAction = PerformWrapper.getPerformWrapper().
                 getActionForNetwork(server.getNetwork());
         profileServerAction = PerformWrapper.getPerformWrapper().
                getActionForServer(server.getAddress(), server.getProfile().getName());
         profileNetworkAction = PerformWrapper.getPerformWrapper().
                 getActionForNetwork(server.getNetwork(), server.getProfile().getName());
 
         if (serverAction == null) {
             serverPerform = "";
         } else {
             serverPerform = implode(serverAction.getResponse());
         }
 
         if (networkAction == null) {
             networkPerform = "";
         } else {
             networkPerform = implode(networkAction.getResponse());
         }
 
         if (profileServerAction == null) {
             profileServerPerform = "";
         } else {
             profileServerPerform = implode(profileServerAction.getResponse());
         }
 
         if (profileNetworkAction == null) {
             profileNetworkPerform = "";
         } else {
             profileNetworkPerform = implode(profileNetworkAction.getResponse());
         }
     }
 
     /**
      * Implodes the specified string array, joining each line with a LF.
      *
      * @param lines The lines to be joined together
      * @return A string containing each element of lines, separated by a LF.
      */
     private String implode(final String[] lines) {
         final StringBuilder res = new StringBuilder();
 
         for (String line : lines) {
             res.append('\n');
             res.append(line);
         }
 
         return res.length() == 0 ? "" : res.substring(1);
     }
 
     /** Populates the perform text area. */
     private void populatePerform() {
         switch (target.getSelectedIndex()) {
             case 0:
                 textarea.setText(networkPerform);
                 break;
             case 1:
                 textarea.setText(profileNetworkPerform);
                 break;
             case 2:
                 textarea.setText(serverPerform);
                 break;
             case 3:
                 textarea.setText(profileServerPerform);
                 break;
             default:
                 break;
         }
     }
 
     /** Stores the text currently in the textarea into the perform strings. */
     private void storeText() {
         switch (activePerform) {
             case 0:
                 networkPerform = textarea.getText();
                 break;
             case 1:
                 profileNetworkPerform = textarea.getText();
                 break;
             case 2:
                 serverPerform = textarea.getText();
                 break;
             case 3:
                 profileServerPerform = textarea.getText();
                 break;
             default:
                 break;
         }
     }
 
     /** Saves the performs. */
     public void savePerforms() {
         storeText();
 
         if (!serverPerform.isEmpty()) {
             if (serverAction == null) {
                 serverAction = PerformWrapper.getPerformWrapper().
                        createActionForServer(server.getAddress());
             }
             serverAction.setResponse(serverPerform.split("\n"));
             serverAction.save();
         }
 
         if (!networkPerform.isEmpty()) {
             if (networkAction == null) {
                 networkAction = PerformWrapper.getPerformWrapper().
                         createActionForNetwork(server.getNetwork());
             }
             networkAction.setResponse(networkPerform.split("\n"));
             networkAction.save();
         }
 
         if (!profileNetworkPerform.isEmpty()) {
             if (profileNetworkAction == null) {
                 profileNetworkAction = PerformWrapper.getPerformWrapper().
                         createActionForNetwork(server.getNetwork(),
                         server.getProfile().getName());
             }
             profileNetworkAction.setResponse(profileNetworkPerform.split("\n"));
             profileNetworkAction.save();
         }
 
         if (!profileServerPerform.isEmpty()) {
             if (profileServerAction == null) {
                 profileServerAction = PerformWrapper.getPerformWrapper().
                        createActionForServer(server.getAddress(),
                         server.getProfile().getName());
             }
             profileServerAction.setResponse(profileServerPerform.split("\n"));
             profileServerAction.save();
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         storeText();
         activePerform = target.getSelectedIndex();
         populatePerform();
     }
 
 }
