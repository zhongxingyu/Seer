 package net.argius.stew.ui.window;
 
 import static java.awt.event.KeyEvent.VK_ESCAPE;
 import static javax.swing.JOptionPane.*;
 import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
 import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
 import static net.argius.stew.ui.window.ConnectorMapEditDialog.ActionKey.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import net.argius.stew.*;
 
 final class ConnectorMapEditDialog extends JDialog implements ChangeListener, AnyActionListener {
 
     enum ActionKey {
         addNew, modify, rename, remove, up, down, submit, cancel
     }
 
     private static final ResourceManager res = ResourceManager.getInstance(ConnectorMapEditDialog.class);
 
     private final ConnectorMap connectorMap;
     private final JList idList;
     private final DefaultListModel listModel;
 
     ConnectorMapEditDialog(JFrame owner, Environment env) {
         // [instance]
         super(owner);
         final DefaultListModel listModel = new DefaultListModel();
         this.connectorMap = new ConnectorMap(env.getConnectorMap());
         this.idList = new JList(listModel);
         this.listModel = listModel;
         setTitle(res.get("title"));
         setResizable(false);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         FlexiblePanel p = new FlexiblePanel();
         p.c.anchor = GridBagConstraints.CENTER;
         p.c.insets = new Insets(8, 12, 8, 0);
         add(p);
         // [components]
         // List
         final JList idList = this.idList;
         idList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         for (ConnectorEntry entry : ConnectorEntry.toList(connectorMap.values())) {
             listModel.addElement(entry);
         }
         JScrollPane pane = new JScrollPane(idList,
                                            VERTICAL_SCROLLBAR_ALWAYS,
                                            HORIZONTAL_SCROLLBAR_NEVER);
         pane.setWheelScrollingEnabled(true);
         idList.addMouseListener(new IdListMouseListener());
         p.addComponent(pane, false);
         // Button 1
         JPanel p1 = new JPanel(new GridLayout(6, 1, 4, 2));
         p1.add(createJButton(addNew));
         p1.add(createJButton(modify));
         p1.add(createJButton(rename));
         p1.add(createJButton(remove));
         p1.add(createJButton(up));
         p1.add(createJButton(down));
         p.c.gridwidth = GridBagConstraints.REMAINDER;
         p.c.insets = new Insets(8, 32, 8, 32);
         p.addComponent(p1, true);
         // Button 2
         JPanel p2 = new JPanel(new GridLayout(1, 2, 16, 8));
         p2.add(createJButton(submit));
         p2.add(createJButton(cancel));
         p.c.gridwidth = GridBagConstraints.REMAINDER;
         p.c.fill = GridBagConstraints.NONE;
         p.addComponent(p2, false);
         // [events]
         AnyAction aa = new AnyAction(rootPane);
         // ESC key
         aa.bind(this, true, cancel, KeyStroke.getKeyStroke(VK_ESCAPE, 0));
     }
 
     private final class IdListMouseListener extends MouseAdapter {
         @Override
         public void mouseClicked(MouseEvent e) {
             if (e.getClickCount() % 2 == 0) {
                 ConnectorEntry entry = (ConnectorEntry)idList.getSelectedValue();
                 openConnectorEditDialog(entry.getConnector());
             }
         }
     }
 
     @Override
     protected void processWindowEvent(WindowEvent e) {
         super.processWindowEvent(e);
         if (e.getID() == WindowEvent.WINDOW_CLOSING) {
             anyActionPerformed(new AnyActionEvent(this, cancel));
         }
     }
 
     @Override
     public void anyActionPerformed(AnyActionEvent ev) {
         if (ev.isAnyOf(addNew)) {
             final String id = showInputDialog(this, res.get("i.input-new-connector-id"));
             if (id == null) {
                 return;
             }
             if (connectorMap.containsKey(id)) {
                 final String message = res.get("e.id-already-exists", id);
                 showMessageDialog(this, message, null, ERROR_MESSAGE);
             } else {
                Connector connector;
                try {
                    connector = new Connector(id, new Properties());
                } catch (IllegalArgumentException ex) {
                    showMessageDialog(this, ex.getMessage(), null, ERROR_MESSAGE);
                    return;
                }
                openConnectorEditDialog(connector);
             }
         } else if (ev.isAnyOf(modify)) {
             ConnectorEntry entry = (ConnectorEntry)idList.getSelectedValue();
             if (entry != null) {
                 openConnectorEditDialog(entry.getConnector());
             }
         } else if (ev.isAnyOf(rename)) {
             Object o = idList.getSelectedValue();
             if (o == null) {
                 return;
             }
             ConnectorEntry entry = (ConnectorEntry)o;
             final String newId = showInputDialog(this,
                                                  res.get("i.input-new-connector-id"),
                                                  entry.getId());
             if (newId == null || newId.equals(entry.getId())) {
                 return;
             }
             connectorMap.remove(entry);
             connectorMap.put(newId, entry.getConnector());
             DefaultListModel m = (DefaultListModel)idList.getModel();
            Connector newConnector;
            try {
                newConnector = new Connector(newId, entry.getConnector());
            } catch (IllegalArgumentException ex) {
                showMessageDialog(this, ex.getMessage(), null, ERROR_MESSAGE);
                return;
            }
             m.set(m.indexOf(entry), new ConnectorEntry(newId, newConnector));
             idList.repaint();
         } else if (ev.isAnyOf(remove)) {
             if (showConfirmDialog(this, res.get("i.confirm-remove"), "", OK_CANCEL_OPTION) != OK_OPTION) {
                 return;
             }
             ConnectorEntry selected = (ConnectorEntry)idList.getSelectedValue();
             connectorMap.remove(selected.getId());
             DefaultListModel m = (DefaultListModel)idList.getModel();
             m.removeElement(selected);
         } else if (ev.isAnyOf(up)) {
             shiftSelectedElementUpward();
         } else if (ev.isAnyOf(down)) {
             shiftSelectedElementDownward();
         } else if (ev.isAnyOf(submit)) {
             requestClose(true);
         } else if (ev.isAnyOf(cancel)) {
             requestClose(false);
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent e) {
         Object source = e.getSource();
         if (source instanceof Connector) {
             Connector connector = (Connector)source;
             final String id = connector.getId();
             connectorMap.setConnector(id, connector);
             ConnectorEntry changed = new ConnectorEntry(id, connector);
             final int index = listModel.indexOf(changed);
             if (index >= 0) {
                 listModel.set(index, changed);
                 idList.setSelectedIndex(index);
             } else {
                 listModel.add(0, changed);
                 idList.setSelectedIndex(0);
             }
         }
     }
 
     private JButton createJButton(Object cmdObject) {
         final String cmd = String.valueOf(cmdObject);
         JButton button = new JButton(res.get("button." + cmd));
         button.setActionCommand(cmd);
         button.addActionListener(new AnyAction(this));
         return button;
     }
 
     private void openConnectorEditDialog(Connector connector) {
         ConnectorEditDialog dialog = new ConnectorEditDialog(this, connector);
         dialog.addChangeListener(this);
         dialog.setModal(true);
         dialog.setLocationRelativeTo(getParent());
         dialog.setSize(dialog.getPreferredSize());
         dialog.setVisible(true);
     }
 
     private void shiftSelectedElementUpward() {
         final int index = idList.getSelectedIndex();
         if (index == 0) {
             return;
         }
         final int newIndex = index - 1;
         swap(listModel, index, newIndex);
         idList.setSelectedIndex(newIndex);
         idList.ensureIndexIsVisible(newIndex);
     }
 
     private void shiftSelectedElementDownward() {
         final int index = idList.getSelectedIndex();
         final int size = listModel.getSize();
         if (index == size - 1) {
             return;
         }
         final int newIndex = index + 1;
         swap(listModel, index, newIndex);
         idList.setSelectedIndex(newIndex);
         idList.ensureIndexIsVisible(newIndex);
     }
 
     private static void swap(DefaultListModel listModel, int index1, int index2) {
         Object o = listModel.get(index1);
         listModel.set(index1, listModel.get(index2));
         listModel.set(index2, o);
     }
 
     private void requestClose(boolean withSaving) {
         if (withSaving) {
             if (showConfirmDialog(this, res.get("i.confirm-save"), "", YES_NO_OPTION) != YES_OPTION) {
                 return;
             }
             ConnectorMap m = new ConnectorMap();
             for (Object o : listModel.toArray()) {
                 ConnectorEntry entry = (ConnectorEntry)o;
                 final String id = entry.getId();
                 m.setConnector(id, connectorMap.getConnector(id));
             }
             try {
                 ConnectorConfiguration.save(m);
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
             connectorMap.clear();
             connectorMap.putAll(m);
         } else {
             ConnectorMap fileContent;
             try {
                 fileContent = ConnectorConfiguration.load();
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
             if (!connectorMap.equals(fileContent)) {
                 if (showConfirmDialog(this, res.get("i.confirm-without-save"), "", OK_CANCEL_OPTION) != OK_OPTION) {
                     return;
                 }
             }
         }
         dispose();
     }
 
 }
