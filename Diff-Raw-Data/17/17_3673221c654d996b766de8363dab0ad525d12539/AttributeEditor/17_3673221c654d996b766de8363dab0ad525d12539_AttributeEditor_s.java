 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * AttributeEditor.java
  *
  * Created on 1. Juli 2004, 13:42
  */
 package Sirius.navigator.ui.attributes.editor;
 
 import Sirius.navigator.connection.*;
 import Sirius.navigator.exception.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.plugin.interfaces.EmbededControlBar;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.types.iterator.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.ui.*;
 import Sirius.navigator.ui.attributes.*;
 import Sirius.navigator.ui.attributes.editor.metaobject.*;
 import Sirius.navigator.ui.widget.*;
 
 import Sirius.server.localserver.attribute.Attribute;
 import Sirius.server.middleware.types.*;
 
 import org.apache.log4j.Logger;
 
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.awt.event.*;
 
 import java.net.*;
 
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import javax.swing.tree.*;
 
 import de.cismet.cids.editors.BeanInitializer;
 import de.cismet.cids.editors.EditorBeanInitializerStore;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class AttributeEditor extends javax.swing.JPanel implements EmbededControlBar {
 
     //~ Instance fields --------------------------------------------------------
 
     protected TreePath treePath;
     private final Logger logger = Logger.getLogger(this.getClass());
     private final ResourceManager resources = ResourceManager.getManager();
     private Object treeNode = null;
     private ComplexEditor editor = null;
     private Object commitBlocker = new Object();
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private Sirius.navigator.ui.attributes.AttributeTree attributeTree;
     private javax.swing.JButton cancelButton;
     private javax.swing.JButton commitButton;
     private javax.swing.JPanel controlBar;
     private javax.swing.JScrollPane editorScrollPane;
     private javax.swing.JPanel switchPanel;
     private Sirius.navigator.ui.widget.TitleBar titleBar;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form AttributeEditor.
      */
     public AttributeEditor() {
         initComponents();
 
         final ActionListener buttonListener = new ButtonListener();
         this.cancelButton.addActionListener(buttonListener);
         this.commitButton.addActionListener(buttonListener);
         // this.editButton.addActionListener(buttonListener);
         // this.pinButton.addActionListener(buttonListener);
 
         this.attributeTree.addTreeSelectionListener(new MetaObjectListener());
         this.attributeTree.setIgnoreInvisibleAttributes(false);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void setControlBarVisible(final boolean isVisible) {
         controlBar.setVisible(isVisible);
     }
 
     @Override
     public Vector<AbstractButton> getControlBarButtons() {
         final Vector<AbstractButton> buttons = new Vector<AbstractButton>();
         buttons.add(commitButton);
         buttons.add(cancelButton);
         return buttons;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  treePath  DOCUMENT ME!
      * @param  node      DOCUMENT ME!
      */
     public void setTreeNode(final TreePath treePath, final Object node) {
         this.treePath = treePath;
         setTreeNode(node);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  node  DOCUMENT ME!
      */
     public void setTreeNode(final Object node) {
         this.confirmEdit();
 
         this.attributeTree.setTreeNode(node);
 
         // wait for attribute thread
         synchronized (this.attributeTree) {
             try {
                 // 10 sec timeout
                 if (logger.isDebugEnabled()) {
                     logger.debug("waiting for attribute thread to finish"); // NOI18N
                 }
                 this.attributeTree.wait(10000);
             } catch (Throwable t) {
                 logger.error("thread synchronization failed", t);           // NOI18N
             }
         }
 
         this.treeNode = node;
 
         if ((this.attributeTree.getRootNode() != null)
                     && (this.attributeTree.getRootNode() instanceof ObjectAttributeNode)) {
             logger.info("setTreeNode(): initializing editor "); // NOI18N
 
             this.editor = new DefaultComplexMetaAttributeEditor();
             final ObjectAttributeNode rootNode = (ObjectAttributeNode)this.attributeTree.getRootNode();
             final MetaObject metaObject = rootNode.getMetaObject();
             final MetaAttributeEditorLocator mael = new MetaAttributeEditorLocator();
             try {
                 // HELL
                 if (mael.getEditor(metaObject) != null) {
                     editor = (ComplexEditor)mael.getEditor(metaObject);
                     if (logger.isDebugEnabled()) {
                         logger.debug("Editor :" + ((ObjectTreeNode)treeNode).getMetaClass().getComplexEditor()); // NOI18N
                     }
                 } else {
                     logger.warn("MetaAttributeEditorLocator returned null for object:" + metaObject);            // NOI18N
                 }
             } catch (Exception e) {
                 logger.info("setTreeNode(): initializing editor EXception", e);                                  // NOI18N
             }
 
             // TimEasy: hier wird das Innere des Editors erzeugt und in die Scrollpane gesetzt
             final Component editorComponent = this.editor.getEditorComponent(
                     null,
                     rootNode.getAttributeKey(),
                     metaObject);
             this.editorScrollPane.getViewport().setView(editorComponent);
 
             this.commitButton.setEnabled(true);
             this.cancelButton.setEnabled(true);
 
             this.titleBar.setTitle(org.openide.util.NbBundle.getMessage(
                     AttributeEditor.class,
                     "AttributeEditor.titleBar.title",
                     new Object[] { rootNode }));                                           // NOI18N
         } else if (logger.isDebugEnabled()) {
             logger.warn("setTreeNode(): node is null or not of type ObjectAttributeNode"); // NOI18N
             this.clear();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void clear() {
         this.editorScrollPane.getViewport().setView(null);
         this.editor = null;
         this.treeNode = null;
         this.attributeTree.clear();
 
         this.commitButton.setEnabled(false);
         this.cancelButton.setEnabled(false);
 
         this.titleBar.setTitle(org.openide.util.NbBundle.getMessage(
                 AttributeEditor.class,
                 "AttributeEditor.titleBar.title")); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Object getTreeNode() {
         return this.treeNode;
     }
 
     /**
      * DOCUMENT ME!
      */
    private void cancel() {
         if (editor != null) {
             editor.cancelEditing();
 
             if (logger.isDebugEnabled()) {
                 logger.debug("cancel() rejecting changes in node " + this.treeNode); // NOI18N
             }
             // this.clear();
 
             final ObjectTreeNode objectTreeNode = (ObjectTreeNode)this.treeNode;
 
             // neuer Knoten
             if (objectTreeNode.isNew()) {
                 MethodManager.getManager()
                         .deleteTreeNode(ComponentRegistry.getRegistry().getCatalogueTree(), objectTreeNode);
             }
 
             this.attributeTree.setTreeNode(null);
             this.treeNode = null;
             this.clear();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void commit() {
         synchronized (commitBlocker) {
             editor.stopEditing();
             if (logger.isInfoEnabled()) {
                 logger.info("commit() saving changes in node " + this.treeNode); // NOI18N
             }
             this.editor.setValueChanged(false);
 
             final ObjectTreeNode objectTreeNode = (ObjectTreeNode)this.getTreeNode();
 
             ComponentRegistry.getRegistry().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             CismetThreadPool.execute(new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             final MetaObject uneditedMetaObject = objectTreeNode.getMetaObject();
                             final MetaObject editedMetaObject = (MetaObject)AttributeEditor.this.editor.getValue();
                             MetaObject savedMetaObject = null;
                             objectTreeNode.setChanged(true);
 
                             // leere Attribute?
                             final String emptyAttributeName = MethodManager.getManager()
                                         .findEmptyAttributes(editedMetaObject);
                             if (emptyAttributeName == null) {
                                 try {
                                     // neuer Knoten
                                     if (objectTreeNode.isNew()) {
                                         Link link;
                                         final TreeNode parent = objectTreeNode.getParent();
                                         if (parent != null) {
                                             link = new Link(
                                                     ((DefaultMetaTreeNode)parent).getID(),
                                                     objectTreeNode.getDomain());
                                         } else {
                                             logger.warn("commit(): node '" + objectTreeNode + "' has no parent node'"); // NOI18N
                                             link = new Link(-1, objectTreeNode.getDomain());
                                         }
 
                                         if (logger.isInfoEnabled()) {
                                             logger.info("commit(): insert meta object: " + editedMetaObject.getName()); // NOI18N
                                         }
                                         savedMetaObject = SessionManager.getProxy()
                                                     .insertMetaObject(editedMetaObject, objectTreeNode.getDomain());
 
                                         // neues objekt zuweisen
                                         objectTreeNode.setMetaObject(savedMetaObject);
 
                                         if (logger.isInfoEnabled()) {
                                             logger.info("commit(): add node: " + objectTreeNode); // NOI18N
                                         }
                                         final Node node = SessionManager.getProxy()
                                                     .addNode(objectTreeNode.getNode(), link);
 
                                         // parent permissions zuweisen...
                                         node.setPermissions(
                                             ((DefaultMetaTreeNode)objectTreeNode.getParent()).getNode()
                                                         .getPermissions());
 
                                         objectTreeNode.setNode(node);
                                         objectTreeNode.setNew(false);
                                         objectTreeNode.setChanged(false);
 
                                         // Component editorComponent = this.editor.getEditorComponent(null,
                                         // this.attributeTree.getRootNode().getAttributeKey(), savedMetaObject);
                                         // this.editorScrollPane.getViewport().setView(editorComponent);
                                     } else {
                                         if (logger.isInfoEnabled()) {
                                             logger.info("commit(): update meta object: " + editedMetaObject.getName()); // NOI18N
                                         }
                                         SessionManager.getProxy()
                                                 .updateMetaObject(editedMetaObject, objectTreeNode.getDomain());
                                         savedMetaObject = editedMetaObject;
 
                                         // neues altes objekt zuweisen
                                         objectTreeNode.setMetaObject(savedMetaObject);
 
                                         objectTreeNode.setChanged(false);
                                     }
 
                                     SwingUtilities.invokeLater(new Runnable() {
 
                                             @Override
                                             public void run() {
                                                 // XXX event w\u00E4re besser ...
                                                 if (logger.isDebugEnabled()) {
                                                     logger.debug("invokeLater() performing GUI update"); // NOI18N
                                                 }
                                                 final AttributeViewer attributeViewer = ComponentRegistry.getRegistry()
                                                             .getAttributeViewer();
 
                                                 if (attributeViewer.getTreeNode()
                                                             == AttributeEditor.this.getTreeNode()) {
                                                     if (logger.isDebugEnabled()) {
                                                         logger.debug(
                                                             "commit() updating attribute viewer with new tree node"); // NOI18N
                                                     }
                                                     attributeViewer.setTreeNode(AttributeEditor.this.getTreeNode());
                                                 }
 
                                                 // XXX i18n
                                                 JOptionPane.showMessageDialog(
                                                     AttributeEditor.this,
                                                     org.openide.util.NbBundle.getMessage(
                                                         AttributeEditor.class,
                                                         "AttributeEditor.invokeLater().InfoMessage",
                                                         new Object[] { objectTreeNode }),           // NOI18N
                                                     org.openide.util.NbBundle.getMessage(
                                                         AttributeEditor.class,
                                                         "AttributeEditor.invokeLater().InfoTitle"), // NOI18N
                                                     JOptionPane.INFORMATION_MESSAGE);
 
                                                 // AttributeEditor.this.setTreeNodes(objectTreeNode);
                                                 AttributeEditor.this.clear();
 
                                                 ComponentRegistry.getRegistry()
                                                         .getMainWindow()
                                                         .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
                                                 try {
                                                     ComponentRegistry.getRegistry()
                                                             .getCatalogueTree()
                                                             .scrollPathToVisible(
                                                                 ComponentRegistry.getRegistry().getCatalogueTree()
                                                                     .getSelectionPath());
                                                     // hier k\u00F6nnte jetzt noch zu dem TAB gewechselt werden das vom
                                                     // User gew\u00FCnscht ist
                                                 } catch (Exception e) {
                                                     logger.warn("can not scroll to selected object.", e); // NOI18N
                                                 }
                                                 // ((MutableTreeNode)AttributeEditor.this.getTreeNode())
                                             }
                                         });
                                 } catch (Throwable t) {
                                     logger.error(
                                         "add / insert of meta object '"
                                                 + objectTreeNode.getMetaObject()
                                                 + "' failed",
                                         t);                                                               // NOI18N
                                     ExceptionManager.getManager()
                                             .showExceptionDialog(
                                                 ExceptionManager.WARNING,
                                                 org.openide.util.NbBundle.getMessage(
                                                     AttributeEditor.class,
                                                     "AttributeEditor.commit().insertError.title"),        // NOI18N
                                                 org.openide.util.NbBundle.getMessage(
                                                     AttributeEditor.class,
                                                     "AttributeEditor.commit().insertError.message"),
                                                 t);                                                       // NOI18N
                                     ComponentRegistry.getRegistry()
                                             .getMainWindow()
                                             .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                 }
                             } else {
                                 // XXX i18n
                                 JOptionPane.showMessageDialog(
                                     AttributeEditor.this,
                                     org.openide.util.NbBundle.getMessage(
                                         AttributeEditor.class,
                                         "AttributeEditor.commit().ErrorMessage",
                                         new Object[] { emptyAttributeName }),   // NOI18N
                                     org.openide.util.NbBundle.getMessage(
                                         AttributeEditor.class,
                                         "AttributeEditor.commit().ErrorTitle"), // NOI18N
                                     JOptionPane.WARNING_MESSAGE);
                                 ComponentRegistry.getRegistry()
                                         .getMainWindow()
                                         .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                             }
                         }
                     }, "commitEditThread"));                                    // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void confirmEdit() {
         if (this.isChanged()) {
             if (JOptionPane.YES_NO_OPTION
                         == JOptionPane.showOptionDialog(
                             AttributeEditor.this,
                             org.openide.util.NbBundle.getMessage(
                                 AttributeEditor.class,
                                 "AttributeEditor.confirmEdit().JOptionPane.message"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 AttributeEditor.class,
                                 "AttributeEditor.confirmEdit().JOptionPane.title"), // NOI18N
                             JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE,
                             null,
                             new String[] {
                                 org.openide.util.NbBundle.getMessage(
                                     AttributeEditor.class,
                                     "AttributeEditor.confirmEdit().JOptionPane.option1"), // NOI18N
                                 org.openide.util.NbBundle.getMessage(
                                     AttributeEditor.class,
                                     "AttributeEditor.confirmEdit().JOptionPane.option2")
                             },                                                // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 AttributeEditor.class,
                                 "AttributeEditor.confirmEdit().JOptionPane.option1"))) { // NOI18N
                 this.commit();
             } else {
                 this.cancel();
             }
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("confirmEdit(): no changes detected");           // NOI18N
             }
             this.cancel();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isChanged() {
         if (logger.isDebugEnabled()) {
             logger.debug("this.editor: " + this.editor);                                       // NOI18N
         }
         if (this.editor != null) {
             if (logger.isDebugEnabled()) {
                 logger.debug("this.editor.isValueChanged(): " + this.editor.isValueChanged()); // NOI18N
             }
         }
 
         if ((this.editor != null) && (this.editor.isValueChanged() || ((DefaultMetaTreeNode)this.treeNode).isNew())) {
             return true;
         }
 
         return false;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JToggleButton pinButton = new javax.swing.JToggleButton();
         final javax.swing.JToggleButton editButton = new javax.swing.JToggleButton();
         controlBar = new javax.swing.JPanel();
         titleBar = new Sirius.navigator.ui.widget.TitleBar();
         commitButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         switchPanel = new javax.swing.JPanel();
         final javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane();
         editorScrollPane = new javax.swing.JScrollPane();
         final javax.swing.JScrollPane treeScrollPane = new javax.swing.JScrollPane();
         attributeTree = new Sirius.navigator.ui.attributes.AttributeTree();
 
         pinButton.setIcon(resources.getIcon("attr_pin_off.gif"));                // NOI18N
         pinButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 AttributeEditor.class,
                 "AttributeEditor.pinButton.tooltip"));                           // NOI18N
         pinButton.setActionCommand("pin");                                       // NOI18N
         pinButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pinButton.setContentAreaFilled(false);
         pinButton.setFocusPainted(false);
         pinButton.setMaximumSize(new java.awt.Dimension(16, 16));
         pinButton.setMinimumSize(new java.awt.Dimension(16, 16));
         pinButton.setPreferredSize(new java.awt.Dimension(16, 16));
         pinButton.setRolloverIcon(resources.getIcon("attr_pin_off.gif"));        // NOI18N
         pinButton.setRolloverSelectedIcon(resources.getIcon("attr_pin_on.gif")); // NOI18N
         pinButton.setSelectedIcon(resources.getIcon("attr_pin_on.gif"));         // NOI18N
 
         editButton.setIcon(resources.getIcon("objekt_bearbeiten.gif"));                 // NOI18N
         editButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 AttributeEditor.class,
                 "AttributeEditor.editButton.tooltip"));                                 // NOI18N
         editButton.setActionCommand("edit");                                            // NOI18N
         editButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         editButton.setContentAreaFilled(false);
         editButton.setFocusPainted(false);
         editButton.setMaximumSize(new java.awt.Dimension(16, 16));
         editButton.setMinimumSize(new java.awt.Dimension(16, 16));
         editButton.setPreferredSize(new java.awt.Dimension(16, 16));
         editButton.setRolloverIcon(resources.getIcon("objekt_bearbeiten.gif"));         // NOI18N
         editButton.setRolloverSelectedIcon(resources.getIcon("objekt_bearbeiten.gif")); // NOI18N
         editButton.setSelectedIcon(resources.getIcon("objekt_bearbeiten.gif"));         // NOI18N
 
         setLayout(new java.awt.BorderLayout());
 
         controlBar.setLayout(new java.awt.GridBagLayout());
 
         titleBar.setIcon(resources.getIcon("floatingframe.gif")); // NOI18N
         titleBar.setTitle(org.openide.util.NbBundle.getMessage(
                 AttributeEditor.class,
                 "AttributeEditor.titleBar.title"));               // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         controlBar.add(titleBar, gridBagConstraints);
 
         commitButton.setIcon(resources.getIcon("save_objekt.gif"));         // NOI18N
         commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 AttributeEditor.class,
                 "AttributeEditor.commitButton.tooltip"));                   // NOI18N
         commitButton.setActionCommand("commit");                            // NOI18N
         commitButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         commitButton.setContentAreaFilled(false);
         commitButton.setEnabled(false);
         commitButton.setFocusPainted(false);
         commitButton.setMaximumSize(new java.awt.Dimension(16, 16));
         commitButton.setMinimumSize(new java.awt.Dimension(16, 16));
         commitButton.setPreferredSize(new java.awt.Dimension(16, 16));
         commitButton.setRolloverIcon(resources.getIcon("save_objekt.gif")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
         controlBar.add(commitButton, gridBagConstraints);
 
         cancelButton.setIcon(resources.getIcon("zurueck_objekt.gif"));         // NOI18N
         cancelButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 AttributeEditor.class,
                 "AttributeEditor.cancelButton.tooltip"));                      // NOI18N
         cancelButton.setActionCommand("cancel");                               // NOI18N
         cancelButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         cancelButton.setContentAreaFilled(false);
         cancelButton.setEnabled(false);
         cancelButton.setFocusPainted(false);
         cancelButton.setMaximumSize(new java.awt.Dimension(16, 16));
         cancelButton.setMinimumSize(new java.awt.Dimension(16, 16));
         cancelButton.setPreferredSize(new java.awt.Dimension(16, 16));
         cancelButton.setRolloverIcon(resources.getIcon("zurueck_objekt.gif")); // NOI18N
         controlBar.add(cancelButton, new java.awt.GridBagConstraints());
 
         add(controlBar, java.awt.BorderLayout.NORTH);
 
         switchPanel.setLayout(new java.awt.CardLayout());
 
         splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         splitPane.setResizeWeight(1.0);
         splitPane.setOneTouchExpandable(PropertyManager.getManager().isAdvancedLayout());
 
         editorScrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
         splitPane.setTopComponent(editorScrollPane);
 
         treeScrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
         treeScrollPane.setRequestFocusEnabled(false);
 
         attributeTree.setMaximumSize(null);
         attributeTree.setMinimumSize(new java.awt.Dimension(100, 50));
         attributeTree.setPreferredSize(null);
         treeScrollPane.setViewportView(attributeTree);
 
         splitPane.setBottomComponent(treeScrollPane);
 
         switchPanel.add(splitPane, "table"); // NOI18N
 
         add(switchPanel, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class MetaObjectListener implements TreeSelectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void valueChanged(final TreeSelectionEvent e) {
             if (AttributeEditor.this.editor != null) {
                 final LinkedList activeChildEditorTree = new LinkedList();
                 final Object[] objects = e.getPath().getPath();
                 for (int i = 0; i < objects.length; i++) {
                     if (objects[i] instanceof ObjectAttributeNode) {
                         activeChildEditorTree.addLast(((ObjectAttributeNode)objects[i]).getAttributeKey());
                     } else if (logger.isDebugEnabled()) {
                         logger.warn("valueChanged(): node '" + objects[i] + "' is no object tree node"); // NOI18N
                     }
                 }
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("valueChanged(): selection editor for selected object tree node"); // NOI18N
                 }
                 AttributeEditor.this.editor.setActiveChildEditorTree(activeChildEditorTree);
 
                 /*if(e.isAddedPath())
                  * { }else if(logger.isDebugEnabled())logger.debug("valueChanged(): ignoring selection event");*/
             } else if (logger.isDebugEnabled()) {
                 logger.warn("editor is null"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ButtonListener implements ActionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (logger.isDebugEnabled()) {
                 logger.debug("actionPerformed(): action command: " + e.getActionCommand()); // NOI18N
             }
             if (e.getActionCommand().equals("commit")) {                                    // NOI18N
                 // XXX
                 // Alle \u00C4nderungen im Objekt speichern:
                 editor.stopEditing();
 
                 if (isChanged()) {
                     final MetaObject editedMetaObject = (MetaObject)AttributeEditor.this.editor.getValue();
                     if (editedMetaObject.getBean().hasObjectWritePermission(SessionManager.getSession().getUser())) {
                         if (JOptionPane.YES_NO_OPTION
                                     == JOptionPane.showOptionDialog(
                                         AttributeEditor.this,
                                         org.openide.util.NbBundle.getMessage(
                                             AttributeEditor.class,
                                             "AttributeEditor.ButtonListener.JOptionPane.commit.message"), // NOI18N
                                         org.openide.util.NbBundle.getMessage(
                                             AttributeEditor.class,
                                             "AttributeEditor.ButtonListener.JOptionPane.commit.title"), // NOI18N
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         null,
                                         null)) {
                             commit();
                         }
                     } else {
                         JOptionPane.showMessageDialog(
                             AttributeEditor.this,
                             org.openide.util.NbBundle.getMessage(
                                 AttributeEditor.class,
                                 "AttributeEditor.ButtonListener.JOptionPane.noobjectpermission.message"),
                             org.openide.util.NbBundle.getMessage(
                                 AttributeEditor.class,
                                 "AttributeEditor.ButtonListener.JOptionPane.noobjectpermission.title"),
                             JOptionPane.WARNING_MESSAGE);
                         return;
                     }
                 } else {
                     AttributeEditor.this.clear();
                 }
             } else if (e.getActionCommand().equals("cancel")                                // NOI18N
                         && (JOptionPane.YES_NO_OPTION
                             == JOptionPane.showOptionDialog(
                                 AttributeEditor.this,
                                 org.openide.util.NbBundle.getMessage(
                                     AttributeEditor.class,
                                     "AttributeEditor.ButtonListener.JOptionPane.cancel.message"), // NOI18N
                                 org.openide.util.NbBundle.getMessage(
                                     AttributeEditor.class,
                                     "AttributeEditor.ButtonListener.JOptionPane.cancel.title"), // NOI18N
                                 JOptionPane.YES_NO_OPTION,
                                 JOptionPane.QUESTION_MESSAGE,
                                 null,
                                 null,
                                 null))) {
                 logger.error("unknown action command '" + e.getActionCommand() + "'");      // NOI18N
                 cancel();
             } else {
                 logger.error("unknown action command '" + e.getActionCommand() + "'");      // NOI18N
             }
             /*else if(e.getActionCommand().equals("edit"))
              * { confirmEdit(); DefaultMetaTreeNode node =
              * ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNode();
              * AttributeEditor.this.setTreeNodes(node); } else if(e.getActionCommand().equals("pin")) {
              * AttributeEditor.this.setUpdateEnabled(AttributeEditor.this.pinButton.isSelected()); }
              * /*if(e.getActionCommand().equals("edit")) { // ask to save or revert changes if(!editButton.isSelected())
              * {     confirmEdit(); }  commitButton.setEnabled(editButton.isSelected());
              * cancelButton.setEnabled(editButton.isSelected()); attributeTable.setEditable(editButton.isSelected()); }
              * else if(e.getActionCommand().equals("commit") && changed && JOptionPane.YES_NO_OPTION ==
              * JOptionPane.showOptionDialog(AttributeEditor.this,
              * resources.getString("attribute.viewer.commit.message"),
              * resources.getString("attribute.viewer.commit.tooltip"), JOptionPane.YES_NO_OPTION,
              * JOptionPane.QUESTION_MESSAGE, null, null, null)) { commit(); } else
              * if(e.getActionCommand().equals("cancel") && changed && JOptionPane.YES_NO_OPTION ==
              * JOptionPane.showOptionDialog(AttributeEditor.this,
              * resources.getString("attribute.viewer.cancel.message"),
              * resources.getString("attribute.viewer.cancel.tooltip"), JOptionPane.YES_NO_OPTION,
              * JOptionPane.QUESTION_MESSAGE, null, null, null)) { cancel(); } else
              * if(e.getActionCommand().equals("pin")) {
              * AttributeEditor.this.setUpdateEnabled(AttributeEditor.this.pinButton.isSelected());}*/
         }
     }
 }
