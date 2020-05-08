 /*
  * Kajona Language File Editor Gui
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 
 package de.mulchprod.kajona.languageeditor.gui;
 
 import de.mulchprod.kajona.languageeditor.core.textfile.ILanguageFileSet;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textfile;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textfile.KeyNotUniqueException;
 import de.mulchprod.kajona.languageeditor.gui.TextAreas.TextAreaManager;
 import de.mulchprod.kajona.languageeditor.gui.coreconnector.CoreConnector;
 import de.mulchprod.kajona.languageeditor.gui.coreconnector.CoreLogListener;
 import de.mulchprod.kajona.languageeditor.gui.tree.GuiTreeNode;
 import de.mulchprod.kajona.languageeditor.gui.tree.GuiTreeSelectionListener;
 import de.mulchprod.kajona.languageeditor.gui.tree.KeyTreeCellRenderer;
 import de.mulchprod.kajona.languageeditor.gui.tree.NodeType;
 import de.mulchprod.kajona.languageeditor.gui.tree.TreeNodeManager;
 import java.awt.Component;
 import java.awt.Toolkit;
 import java.util.EventObject;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import org.jdesktop.application.Application.ExitListener;
 
 /**
  * The application's main frame.
  */
 public class KajonaLanguageEditorGuiView extends FrameView {
 
     private GuiTreeNode lastSelectedGuiTreeNode;
     private TreePath lastSelectedPath;
 
 
     public KajonaLanguageEditorGuiView(SingleFrameApplication app) {
         super(app);
 
         initComponents();
 
         
        
        
         
         getApplication().addExitListener(new ExitListener() {
 
             public boolean canExit(EventObject arg0) {
                 //any files left open for edit?
                 if(CoreConnector.getInstance().needToSave()) {
                     int dialogResult = JOptionPane.showConfirmDialog(mainPanel, "There are unsaved changes. Really exit?", "Confirmation", JOptionPane.YES_NO_OPTION);
                     if(dialogResult == JOptionPane.YES_OPTION)
                         return true;
                     else
                         return false;
                 }
 
                 return true;
             }
 
             public void willExit(EventObject arg0) {
                 
             }
         });
 
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     //progressBar.setVisible(true);
                     //progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                 } else if ("message".equals(propertyName)) {
                     String text = (String)(evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer)(evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);
                 }
             }
         });
 
 
         //create the logging-listener once
         CoreConnector.getInstance(CoreLogListener.getInstance());
 
         initInternalProperties();
 
 
         getFrame().setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("favicon.png")));
         
     }
 
     private void startWorkingIcon(String message) {
         if (!busyIconTimer.isRunning()) {
             statusAnimationLabel.setIcon(busyIcons[0]);
             busyIconIndex = 0;
             busyIconTimer.start();
         }
         statusMessageLabel.setText(message);
         //messageTimer.restart();
     }
 
     private void stopWorkingIcon() {
         busyIconTimer.stop();
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
         progressBar.setValue(0);
         statusMessageLabel.setText("");
     }
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = KajonaLanguageEditorGuiApp.getApplication().getMainFrame();
             aboutBox = new KajonaLanguageEditorGuiAboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         KajonaLanguageEditorGuiApp.getApplication().show(aboutBox);
     }
 
     private void initInternalProperties() {
         //firePropertyChange(new PropertyChangeEvent(this, "started", "1", "2"));
         statusAnimationLabel.setIcon(busyIcons[0]);
         busyIconIndex = 0;
         busyIconTimer.start();
         statusMessageLabel.setText("Initializing Gui");
 
         ((DefaultTreeModel)langTree.getModel()).setRoot(new DefaultMutableTreeNode(""));
 
         //try to fetch a core-connector
         if(CoreConnector.getInstance().isBitSetupError()) {
             JOptionPane.showMessageDialog(mainPanel, "Please set project properties (File -> Project properties)", "Error",  JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         //Tree-setup
         TreeNodeManager.getInstance().resetCacheSets();
         CoreConnector.getInstance().initLangTree(langTree);
         langTree.addTreeSelectionListener(new GuiTreeSelectionListener(langTree));
 
         //adminTree.setRootVisible(false);
         //portalTree.setRootVisible(false);
 
         //TextArea-Setup
         TextAreaManager textManager = TextAreaManager.getInstance();
         textManager.setScrollContainer(textValuePanel);
         textManager.setLanguages(CoreConnector.getInstance().getLanguages());
         textManager.resetPanel();
 
         busyIconTimer.stop();
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
         progressBar.setValue(0);
         statusMessageLabel.setText("");
 
        
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         jToolBar1 = new javax.swing.JToolBar();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jSplitPane1 = new javax.swing.JSplitPane();
         jScrollPane1 = new javax.swing.JScrollPane();
         textValuePanel = new javax.swing.JPanel();
         adminTreeScrollPane = new javax.swing.JScrollPane();
         langTree = new javax.swing.JTree();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         saveMenuItem = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         createLangMenuItem = new javax.swing.JMenuItem();
         propsMenuItem = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         consoleMenuItem = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JSeparator();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
         nodePopupMenu = new javax.swing.JPopupMenu();
         newItem = new javax.swing.JMenuItem();
         newLangItem = new javax.swing.JMenuItem();
         pasteKey = new javax.swing.JMenuItem();
         leafPopupMenu = new javax.swing.JPopupMenu();
         newItem2 = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JSeparator();
         renameItem = new javax.swing.JMenuItem();
         deleteItem = new javax.swing.JMenuItem();
         copyKey = new javax.swing.JMenuItem();
 
         mainPanel.setName("mainPanel"); // NOI18N
 
         jToolBar1.setRollover(true);
         jToolBar1.setName("jToolBar1"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(KajonaLanguageEditorGuiView.class, this);
         jButton1.setAction(actionMap.get("preferencesAction")); // NOI18N
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(KajonaLanguageEditorGuiView.class);
         jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
         jButton1.setFocusable(false);
         jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton1.setName("jButton1"); // NOI18N
         jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(jButton1);
 
         jButton2.setAction(actionMap.get("saveToCoreAction")); // NOI18N
         jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
         jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
         jButton2.setFocusable(false);
         jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton2.setName("jButton2"); // NOI18N
         jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(jButton2);
 
         jSplitPane1.setBorder(null);
         jSplitPane1.setDividerLocation(160);
         jSplitPane1.setName("jSplitPane1"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         textValuePanel.setName("textValuePanel"); // NOI18N
 
         javax.swing.GroupLayout textValuePanelLayout = new javax.swing.GroupLayout(textValuePanel);
         textValuePanel.setLayout(textValuePanelLayout);
         textValuePanelLayout.setHorizontalGroup(
             textValuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 569, Short.MAX_VALUE)
         );
         textValuePanelLayout.setVerticalGroup(
             textValuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 253, Short.MAX_VALUE)
         );
 
         jScrollPane1.setViewportView(textValuePanel);
 
         jSplitPane1.setRightComponent(jScrollPane1);
 
         adminTreeScrollPane.setName("adminTreeScrollPane"); // NOI18N
 
         langTree.setCellRenderer(new KeyTreeCellRenderer());
         langTree.setName("langTree"); // NOI18N
         langTree.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 treeMousePressedHandler(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 treeMousePressedHandler(evt);
             }
         });
         adminTreeScrollPane.setViewportView(langTree);
 
         jSplitPane1.setLeftComponent(adminTreeScrollPane);
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
             .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
         );
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setAction(actionMap.get("createNewLanguageAction")); // NOI18N
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         saveMenuItem.setAction(actionMap.get("saveToCoreAction")); // NOI18N
         saveMenuItem.setIcon(resourceMap.getIcon("saveMenuItem.icon")); // NOI18N
         saveMenuItem.setText(resourceMap.getString("saveMenuItem.text")); // NOI18N
         saveMenuItem.setName("saveMenuItem"); // NOI18N
         fileMenu.add(saveMenuItem);
 
         jSeparator1.setName("jSeparator1"); // NOI18N
         fileMenu.add(jSeparator1);
 
         createLangMenuItem.setAction(actionMap.get("createNewLanguageAction")); // NOI18N
         createLangMenuItem.setText(resourceMap.getString("createLangMenuItem.text")); // NOI18N
         createLangMenuItem.setName("createLangMenuItem"); // NOI18N
         fileMenu.add(createLangMenuItem);
 
         propsMenuItem.setAction(actionMap.get("preferencesAction")); // NOI18N
         propsMenuItem.setIcon(resourceMap.getIcon("propsMenuItem.icon")); // NOI18N
         propsMenuItem.setText(resourceMap.getString("propsMenuItem.text")); // NOI18N
         propsMenuItem.setName("propsMenuItem"); // NOI18N
         fileMenu.add(propsMenuItem);
 
         jSeparator2.setName("jSeparator2"); // NOI18N
         fileMenu.add(jSeparator2);
 
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         consoleMenuItem.setAction(actionMap.get("showConsoleViewAction")); // NOI18N
         consoleMenuItem.setText(resourceMap.getString("consoleMenuItem.text")); // NOI18N
         consoleMenuItem.setName("consoleMenuItem"); // NOI18N
         helpMenu.add(consoleMenuItem);
 
         jSeparator3.setName("jSeparator3"); // NOI18N
         helpMenu.add(jSeparator3);
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         menuBar.add(helpMenu);
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(statusMessageLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 462, Short.MAX_VALUE)
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(statusAnimationLabel)
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(statusMessageLabel)
                     .addComponent(statusAnimationLabel)
                     .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(3, 3, 3))
         );
 
         nodePopupMenu.setName("nodePopupMenu"); // NOI18N
 
         newItem.setAction(actionMap.get("newLanguageKey")); // NOI18N
         newItem.setText(resourceMap.getString("newItem.text")); // NOI18N
         newItem.setName("newItem"); // NOI18N
         nodePopupMenu.add(newItem);
 
         newLangItem.setAction(actionMap.get("createNewLanguageForFileAction")); // NOI18N
         newLangItem.setText(resourceMap.getString("newLangItem.text")); // NOI18N
         newLangItem.setName("newLangItem"); // NOI18N
         nodePopupMenu.add(newLangItem);
 
         pasteKey.setAction(actionMap.get("pasteKeyAction")); // NOI18N
         pasteKey.setText(resourceMap.getString("Paste Key and Values.text")); // NOI18N
         pasteKey.setName("Paste Key and Values"); // NOI18N
         nodePopupMenu.add(pasteKey);
 
         leafPopupMenu.setName("leafPopupMenu"); // NOI18N
 
         newItem2.setAction(actionMap.get("newLanguageKey")); // NOI18N
         newItem2.setText(resourceMap.getString("newItem2.text")); // NOI18N
         newItem2.setName("newItem2"); // NOI18N
         leafPopupMenu.add(newItem2);
 
         jSeparator4.setName("jSeparator4"); // NOI18N
         leafPopupMenu.add(jSeparator4);
 
         renameItem.setAction(actionMap.get("renameKeyAction")); // NOI18N
         renameItem.setText(resourceMap.getString("renameItem.text")); // NOI18N
         renameItem.setName("renameItem"); // NOI18N
         leafPopupMenu.add(renameItem);
 
         deleteItem.setAction(actionMap.get("deleteKeyAction")); // NOI18N
         deleteItem.setText(resourceMap.getString("deleteItem.text")); // NOI18N
         deleteItem.setName("deleteItem"); // NOI18N
         leafPopupMenu.add(deleteItem);
 
         copyKey.setAction(actionMap.get("copyKeyAction")); // NOI18N
         copyKey.setText(resourceMap.getString("copyKey.text")); // NOI18N
         copyKey.setName("copyKey"); // NOI18N
         leafPopupMenu.add(copyKey);
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     private void treeMousePressedHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressedHandler
         if(evt.isPopupTrigger()) {
             if(evt.getSource() instanceof JTree) {
                 JTree tree = (JTree)evt.getSource();
 
                 TreePath path = tree.getClosestPathForLocation(evt.getX(), evt.getY());
                 tree.setSelectionPath(path);
                 if(path.getLastPathComponent() instanceof GuiTreeNode) {
                     GuiTreeNode node = (GuiTreeNode)path.getLastPathComponent();
                     this.lastSelectedGuiTreeNode = node;
                     this.lastSelectedPath = path;
                     if(node.getReferencingTreeNode().getType() == NodeType.MODULEPART) {
                         nodePopupMenu.show((Component)evt.getSource(), evt.getX(), evt.getY());
                     }
                     else if(node.getReferencingTreeNode().getType() == NodeType.KEY) {
                         leafPopupMenu.show((Component)evt.getSource(), evt.getX(), evt.getY());
                     }
                 }
             }
         }
 
     }//GEN-LAST:event_treeMousePressedHandler
 
     @Action
     public void preferencesAction() {
         KajonaLanguageEditorGuiPrefDialog.main(CoreConnector.getInstance());
     }
 
     @Action
     public Task saveToCoreAction() {
         return new SaveToCoreActionTask(getApplication());
     }
 
     private class SaveToCoreActionTask extends org.jdesktop.application.Task<Object, Void> {
         SaveToCoreActionTask(org.jdesktop.application.Application app) {
             // Runs on the EDT.  Copy GUI state that
             // doInBackground() depends on from parameters
             // to SaveToCoreActionTask fields, here.
             super(app);
             lastSelectedGuiTreeNode = (GuiTreeNode)langTree.getLastSelectedPathComponent();
             startWorkingIcon("Saving files...");
             
         }
         @Override protected Object doInBackground() {
             // Your Task's code here.  This method runs
             // on a background thread, so don't reference
             // the Swing GUI from here.
 
             final WorkingDialog dialog = new WorkingDialog(getFrame(), true);
                 java.awt.EventQueue.invokeLater(new Runnable() {
                     public void run() {
                         /*dialog = new WorkingDialog(new javax.swing.JFrame(), true);
                         dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                             public void windowClosing(java.awt.event.WindowEvent e) {
                                 System.exit(0);
                             }
                         });*/
                         dialog.setVisible(true);
                         CoreConnector.getInstance().saveProjectfiles();
                     }
                 });
 
                 
                 dialog.setVisible(false);
                 dialog.dispose();
                 
 
             //save old selection-path
             
             
             return null;  // return your result
         }
         @Override protected void succeeded(Object result) {
             // Runs on the EDT.  Update the GUI based on
             // the result computed by doInBackground().
             initInternalProperties();
 
             JTree tree = null;
             if(lastSelectedGuiTreeNode != null) {
                 ILanguageFileSet fileSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
 
                 tree = langTree;
 
                 TreePath path = new TreePath(tree.getModel().getRoot());
                 Object parentNode = tree.getModel().getRoot();
                 for (int i = 0; i < tree.getModel().getChildCount(parentNode); i++) {
                     GuiTreeNode node = (GuiTreeNode) tree.getModel().getChild(parentNode, i);
                     if (fileSet.getModule().equals(node.getReferencingTreeNode().getNodeName())) {
                         path = path.pathByAddingChild((DefaultMutableTreeNode) node);
                         parentNode = node;
                     }
                 }
 
                 for (int i = 0; i < tree.getModel().getChildCount(parentNode); i++) {
                     GuiTreeNode node = (GuiTreeNode) tree.getModel().getChild(parentNode, i);
                     if (fileSet.getModulePart().equals(node.getReferencingTreeNode().getNodeName())) {
                         path = path.pathByAddingChild((DefaultMutableTreeNode) node);
                         parentNode = node;
                     }
                 }
 
                 for (int i = 0; i < tree.getModel().getChildCount(parentNode); i++) {
                     GuiTreeNode node = (GuiTreeNode) tree.getModel().getChild(parentNode, i);
                     if (lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName().equals(node.getReferencingTreeNode().getNodeName())) {
                         path = path.pathByAddingChild((DefaultMutableTreeNode) node);
                         parentNode = node;
                     }
                 }
 
                 tree.scrollPathToVisible(path);
                 tree.setSelectionPath(path);
             }
 
             stopWorkingIcon();
         }
     }
 
     @Action
     public void newLanguageKey() {
         String newValue = JOptionPane.showInputDialog("Name of new key, no whitespaces allowed:", "");
         if(newValue != null && newValue.trim().length() > 0) {
             ILanguageFileSet fileSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
             try {
                 fileSet.createKeyValue(newValue);
             } catch (KeyNotUniqueException ex) {
                 JOptionPane.showMessageDialog(mainPanel, "Key already existing", "Error",  JOptionPane.ERROR_MESSAGE);
                 return;
             }
             //reinit
             initInternalProperties();
 
             JTree tree = null;
             tree = langTree;
 
             newValue = Textfile.replaceChars(newValue);
 
             TreePath path = new TreePath(  tree.getModel().getRoot()  );
             Object parentNode = tree.getModel().getRoot();
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(fileSet.getModule().equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(fileSet.getModulePart().equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(newValue.equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             tree.scrollPathToVisible(path);
             tree.setSelectionPath(path);
         }
     }
 
     @Action
     public void renameKeyAction() {
         String newValue = JOptionPane.showInputDialog("Name of new key, no whitespaces allowed:", lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName());
         if(newValue != null && newValue.trim().length() > 0 && (!newValue.equals(lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName()))) {
 
 
             ILanguageFileSet fileSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
             try {
                 fileSet.updateKeyValue(lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName(), newValue);
             } catch (KeyNotUniqueException ex) {
                 JOptionPane.showMessageDialog(mainPanel, "Key already existing", "Error",  JOptionPane.ERROR_MESSAGE);
                 return;
                 //Logger.getLogger(KajonaLanguageEditorGuiView.class.getName()).log(Level.SEVERE, null, ex);
             }
             //reinit
             initInternalProperties();
 
             JTree tree = langTree;
 
             newValue = Textfile.replaceChars(newValue);
 
             TreePath path = new TreePath(  tree.getModel().getRoot()  );
             Object parentNode = tree.getModel().getRoot();
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(fileSet.getModule().equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(fileSet.getModulePart().equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(newValue.equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
             
             tree.scrollPathToVisible(path);
             tree.setSelectionPath(path);
         }
     }
 
     @Action
     public void deleteKeyAction() {
         int returnCode = JOptionPane.showConfirmDialog(mainPanel, "Really delete key \""+this.lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName()+"\" ?", "Confirmation", JOptionPane.YES_NO_OPTION);
         if(returnCode == JOptionPane.OK_OPTION) {
             
             ILanguageFileSet referencedSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
             referencedSet.deleteKeyValue(this.lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName());
             //reinit the trees
             initInternalProperties();
 
             JTree tree = langTree;
 
             TreePath path = new TreePath(  tree.getModel().getRoot()  );
             Object parentNode = tree.getModel().getRoot();
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(referencedSet.getModule().equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                 GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                 if(referencedSet.getModulePart().equals(node.getReferencingTreeNode().getNodeName())) {
                     path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                     parentNode = node;
                 }
             }
 
             tree.scrollPathToVisible(path);
             tree.expandPath(path);
         }
     }
 
     @Action
     public void copyKeyAction() {
         ILanguageFileSet referencedSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
         String selectedKey = this.lastSelectedGuiTreeNode.getReferencingTreeNode().getNodeName();
         //pass value to backend
         CoreConnector.getInstance().copyKey(referencedSet, selectedKey);
     }
 
     @Action
     public void pasteKeyAction() {
         ILanguageFileSet referencedSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
         //pass value to backend
         if(!CoreConnector.getInstance().pasteKey(referencedSet)) {
             JOptionPane.showMessageDialog(mainPanel, "No key to paste selected", "Error",  JOptionPane.ERROR_MESSAGE);
         }
 
         //reinit
         initInternalProperties();
 
         JTree tree = langTree;
 
 
         TreePath path = new TreePath(  tree.getModel().getRoot()  );
         Object parentNode = tree.getModel().getRoot();
         for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
             GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
             if(referencedSet.getModule().equals(node.getReferencingTreeNode().getNodeName())) {
                 path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                 parentNode = node;
             }
         }
 
         for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
             GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
             if(referencedSet.getModulePart().equals(node.getReferencingTreeNode().getNodeName())) {
                 path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                 parentNode = node;
             }
         }
 
         for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
             GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
             if(CoreConnector.getInstance().getKeyMarkedToCopy().equals(node.getReferencingTreeNode().getNodeName())) {
                 path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                 parentNode = node;
             }
         }
 
         tree.scrollPathToVisible(path);
         tree.setSelectionPath(path);
     }
 
     @Action
     public Task showConsoleViewAction() {
         return new ShowConsoleViewActionTask(getApplication());
     }
 
     private class ShowConsoleViewActionTask extends org.jdesktop.application.Task<Object, Void> {
         ShowConsoleViewActionTask(org.jdesktop.application.Application app) {
             // Runs on the EDT.  Copy GUI state that
             // doInBackground() depends on from parameters
             // to ShowConsoleViewActionTask fields, here.
             super(app);
 
         }
         @Override protected Object doInBackground() {
             // Your Task's code here.  This method runs
             // on a background thread, so don't reference
             // the Swing GUI from here.
             KajonaLanguageEditorGuiConsoleView.main(new String[0]);
             return null;  // return your result
         }
         @Override protected void succeeded(Object result) {
             // Runs on the EDT.  Update the GUI based on
             // the result computed by doInBackground().
         }
     }
 
     @Action
     public Task createNewLanguageAction() {
         return new CreateNewLanguageActionTask(getApplication());
 
     }
 
     private class CreateNewLanguageActionTask extends org.jdesktop.application.Task<Object, Void> {
         CreateNewLanguageActionTask(org.jdesktop.application.Application app) {
             super(app);
             startWorkingIcon("Creating filesets...");
         }
         @Override protected Object doInBackground() {
             String newLanguage  = JOptionPane.showInputDialog("Abbreviation of new language, 2 chars:");
             if(newLanguage != null && newLanguage.length() == 2) {
                 final WorkingDialog dialog = new WorkingDialog(getFrame(), true);
                 java.awt.EventQueue.invokeLater(new Runnable() {
                     public void run() {
                         dialog.setVisible(true);
                     }
                 });
 
                 CoreConnector.getInstance().createNewLanguage(newLanguage);
                 dialog.setVisible(false);
                 dialog.dispose();
                 
             }
             else if(newLanguage != null && newLanguage.length() > 0)
                 JOptionPane.showMessageDialog(mainPanel, "Length of language-name not exactly 2 chars", "Error",  JOptionPane.ERROR_MESSAGE);
             return null;
         }
         @Override protected void succeeded(Object result) {
             // Runs on the EDT.  Update the GUI based on
             // the result computed by doInBackground().
             initInternalProperties();
             stopWorkingIcon();
         }
     }
 
     @Action
     public void createNewLanguageForFileAction() {
         String newLanguage  = JOptionPane.showInputDialog("Abbreviation of new language, 2 chars:");
             if(newLanguage != null && newLanguage.length() == 2) {
                 ILanguageFileSet fileSet = lastSelectedGuiTreeNode.getReferencingTreeNode().getReferencingObject();
 
                 fileSet.createNewFileForNewLanguage(newLanguage);
 
                 //reinit
                 initInternalProperties();
 
                 JTree tree = langTree;
       
                 TreePath path = new TreePath(  tree.getModel().getRoot()  );
                 Object parentNode = tree.getModel().getRoot();
                 for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                     GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                     if(fileSet.getModule().equals(node.getReferencingTreeNode().getNodeName())) {
                         path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                         parentNode = node;
                     }
                 }
 
                 for(int i=0; i < tree.getModel().getChildCount(parentNode); i++) {
                     GuiTreeNode node = (GuiTreeNode)tree.getModel().getChild(parentNode, i);
                     if(fileSet.getModulePart().equals(node.getReferencingTreeNode().getNodeName())) {
                         path = path.pathByAddingChild((DefaultMutableTreeNode)node);
                         parentNode = node;
                     }
                 }
 
                 
 
                 tree.scrollPathToVisible(path);
                 tree.setSelectionPath(path);
 
 
 
                 
                 //CoreConnector.getInstance().createNewLanguage(newLanguage);
             }
             else if(newLanguage != null && newLanguage.length() > 0)
                 JOptionPane.showMessageDialog(mainPanel, "Length of language-name not exactly 2 chars", "Error",  JOptionPane.ERROR_MESSAGE);
     }
 
     
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane adminTreeScrollPane;
     private javax.swing.JMenuItem consoleMenuItem;
     private javax.swing.JMenuItem copyKey;
     private javax.swing.JMenuItem createLangMenuItem;
     private javax.swing.JMenuItem deleteItem;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JTree langTree;
     private javax.swing.JPopupMenu leafPopupMenu;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenuItem newItem;
     private javax.swing.JMenuItem newItem2;
     private javax.swing.JMenuItem newLangItem;
     private javax.swing.JPopupMenu nodePopupMenu;
     private javax.swing.JMenuItem pasteKey;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JMenuItem propsMenuItem;
     private javax.swing.JMenuItem renameItem;
     private javax.swing.JMenuItem saveMenuItem;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     private javax.swing.JPanel textValuePanel;
     // End of variables declaration//GEN-END:variables
 
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
 
     private JDialog aboutBox;
 }
