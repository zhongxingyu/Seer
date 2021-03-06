 /*
  * Copyright 2009 JBoss, a divison Red Hat, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jboss.errai.tools.monitoring;
 
 import org.jboss.errai.bus.client.api.MessageCallback;
 import org.jboss.errai.bus.client.api.base.RuleDelegateMessageCallback;
 import org.jboss.errai.bus.client.framework.MessageBus;
 import org.jboss.errai.bus.server.ServerMessageBus;
 import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeSelectionModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.*;
 import java.util.List;
 
 import static javax.swing.SwingUtilities.invokeLater;
 
 public class ServerMonitorPanel implements Attachable {
     private MainMonitorGUI mainMonitorGUI;
     private MessageBus messageBus;
     private String busId;
 
     private JList busServices;
     private JTree serviceExplorer;
 
     private final DefaultListModel busServicesModel;
 
     private JPanel rootPanel;
 
     private String currentlySelectedService;
 
     private ServerLogPanel logPanel;
 
     private ActivityProcessor processor;
 
     private Map<String, ServiceActivityMonitor> monitors = new HashMap<String, ServiceActivityMonitor>();
 
     public ServerMonitorPanel(MainMonitorGUI gui, MessageBus bus, String busId) {
         this.mainMonitorGUI = gui;
         this.messageBus = bus;
         this.busId = busId;
 
         rootPanel = new JPanel();
         rootPanel.setLayout(new BorderLayout());
 
         JButton monitorButton = new JButton("Monitor...");
         JButton activityConsoleButton = new JButton("Activity Console");
 
         busServices = new JList();
         busServices.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         busServices.setModel(busServicesModel = new DefaultListModel());
         busServices.setCellRenderer(new ServicesListCellRender());
 
         JScrollPane pane;
        rootPanel.add(pane = new JScrollPane(busServices), BorderLayout.WEST);
        pane.setPreferredSize(new Dimension(200, 0));
 
         serviceExplorer = new JTree();
        rootPanel.add(new JScrollPane(serviceExplorer), BorderLayout.CENTER);
 
         JPanel southPanel = new JPanel();
         southPanel.setLayout(new BorderLayout());
         rootPanel.add(southPanel, BorderLayout.SOUTH);
 
         southPanel.add(activityConsoleButton, BorderLayout.WEST);
         southPanel.add(monitorButton, BorderLayout.EAST);
 
         busServices.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 currentlySelectedService = getCurrentServiceSelection();
                 generateServiceExplorer();
             }
         });
 
         busServices.addMouseListener(new MouseListener() {
             long lastClick;
 
             public void mouseClicked(MouseEvent e) {
                 switch (e.getClickCount()) {
                     case 1:
                         lastClick = System.currentTimeMillis();
                         break;
                     case 2:
                         if (!e.isConsumed() && (System.currentTimeMillis() - lastClick < 500)) {
                             e.consume();
                             openActivityMonitor();
                         }
                 }
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         monitorButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 openActivityMonitor();
             }
         });
 
         activityConsoleButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 openServerLog();
             }
         });
 
         DefaultTreeModel model = (DefaultTreeModel) serviceExplorer.getModel();
         ((DefaultMutableTreeNode) serviceExplorer.getModel().getRoot()).removeAllChildren();
         serviceExplorer.setRootVisible(false);
 
         serviceExplorer.setCellRenderer(new MonitorTreeCellRenderer());
 
         serviceExplorer.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 
         model.reload();
     }
 
     public void attach(ActivityProcessor proc) {
         this.processor = proc;
     }
 
     private void openActivityMonitor() {
         if (monitors.containsKey(getCurrentServiceSelection())) {
             monitors.get(currentlySelectedService).toFront();
         } else {
             ServiceActivityMonitor sam = new ServiceActivityMonitor(this, busId, currentlySelectedService);
             sam.attach(processor);
             monitors.put(currentlySelectedService, sam);
         }
     }
 
     private void openServerLog() {
         if (this.logPanel != null && this.logPanel.isVisible()) {
             return;
         }
         this.logPanel = new ServerLogPanel(mainMonitorGUI);
         this.logPanel.attach(processor);
     }
 
     void stopMonitor(String service) {
         monitors.remove(service);
     }
 
     private String getCurrentServiceSelection() {
         return String.valueOf(busServicesModel.get(busServices.getSelectedIndex()));
     }
 
     public void addServiceName(final String serviceName) {
         synchronized (busServicesModel) {
             if (busServicesModel.contains(serviceName)) return;
 
             invokeLater(new Runnable() {
                 public void run() {
                     busServicesModel.addElement(serviceName);
                 }
             });
         }
     }
 
     public void removeServiceName(final String serviceName) {
         synchronized (busServicesModel) {
             if (!busServicesModel.contains(serviceName)) return;
 
             invokeLater(new Runnable() {
                 public void run() {
                     busServicesModel.removeElement(serviceName);
                 }
             });
         }
     }
 
     public ServiceActivityMonitor getMonitor(String monitor) {
         return monitors.get(monitor);
     }
 
     public JPanel getPanel() {
         return rootPanel;
     }
 
     private void generateServiceExplorer() {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) serviceExplorer.getModel().getRoot();
 
         node.setUserObject(new JLabel(currentlySelectedService, getSwIcon("service.png"), SwingConstants.LEFT));
         node.removeAllChildren();
 
         serviceExplorer.setRootVisible(true);
 
         DefaultTreeModel model = (DefaultTreeModel) serviceExplorer.getModel();
 
 
         if (messageBus instanceof ServerMessageBus) {
             // this is the serverside bus.
             ServerMessageBus smb = (ServerMessageBus) messageBus;
             List<MessageCallback> receivers = smb.getReceivers(currentlySelectedService);
 
             DefaultMutableTreeNode receiversNode
                     = new DefaultMutableTreeNode("Receivers (" + receivers.size() + ")", true);
 
             for (MessageCallback mc : receivers) {
                 receiversNode.add(new DefaultMutableTreeNode(mc.getClass().getName()));
 
                 if (mc instanceof RuleDelegateMessageCallback) {
                     RuleDelegateMessageCallback ruleDelegate = (RuleDelegateMessageCallback) mc;
                     DefaultMutableTreeNode securityNode =
                             new DefaultMutableTreeNode("Security");
 
                     if (ruleDelegate.getRoutingRule() instanceof RolesRequiredRule) {
                         RolesRequiredRule rule = (RolesRequiredRule) ruleDelegate.getRoutingRule();
 
                         DefaultMutableTreeNode rolesNode =
                                 new DefaultMutableTreeNode(rule.getRoles().isEmpty() ? "Requires Authentication" : "Roles Required");
 
                         for (Object o : rule.getRoles()) {
                             //     DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(String.valueOf(o));
 
                             rolesNode.add(createIconEntry("key.png", String.valueOf(o)));
                         }
 
                         securityNode.add(rolesNode);
                     }
 
                     node.add(securityNode);
                 }
             }
 
             node.add(receiversNode);
         }
 
         model.reload();
     }
 
     public MainMonitorGUI getMainMonitorGUI() {
         return mainMonitorGUI;
     }
 
     private Icon getSwIcon(String name) {
         return new ImageIcon(this.getClass().getClassLoader().getResource(name));
     }
 
     private MutableTreeNode createIconEntry(String icon, String name) {
         return new DefaultMutableTreeNode(new JLabel(name, getSwIcon(icon), SwingConstants.LEFT));
     }
 
     private static Set<String> builtInServices = new HashSet<String>();
 
     static {
         builtInServices.add("ServerBus");
         builtInServices.add("AuthorizationService");
         builtInServices.add("AuthenticationService");
         builtInServices.add("ServerEchoService");
 
         builtInServices.add("ClientBus");
         builtInServices.add("ClientBusErrors");
     }
 
     public class ServicesListCellRender extends DefaultListCellRenderer {
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             String v = String.valueOf(value);
             if (v.endsWith(":RPC")) {
                 setIcon(getSwIcon("database_connect.png"));
             } else {
                 setIcon(builtInServices.contains(v) ? getSwIcon("database_key.png") : getSwIcon("database.png"));
             }
             setToolTipText(v);
             return this;
         }
     }
 }
