 /*
  * AttributeEditor.java
  *
  * Created on 1. Juli 2004, 13:42
  */
 
 package Sirius.navigator.ui.attributes.editor;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.datatransfer.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import java.net.*;
 import javax.swing.border.*;
 import javax.swing.tree.*;
 
 import org.apache.log4j.Logger;
 
 import Sirius.server.localserver.attribute.Attribute;
 import Sirius.server.middleware.types.*;
 import Sirius.navigator.types.iterator.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.ui.widget.*;
 import Sirius.navigator.ui.attributes.*;
 import Sirius.navigator.ui.*;
 import Sirius.navigator.exception.*;
 import Sirius.navigator.ui.attributes.editor.*;
 import Sirius.navigator.ui.attributes.editor.metaobject.*;
 import Sirius.navigator.connection.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.plugin.interfaces.EmbededControlBar;
 import de.cismet.tools.CismetThreadPool;
 import java.beans.Beans;
 
 /**
  *
  * @author  pascal
  */
 public class AttributeEditor extends javax.swing.JPanel implements EmbededControlBar {
     private final Logger logger = Logger.getLogger(this.getClass());
     private static final ResourceBundle I18N = ResourceBundle.getBundle("Sirius/navigator/resource/i18n/resources");
     
    private final ResourceManager resources = ResourceManager.getManager();;
     private Object treeNode = null;
     
     private ComplexEditor editor = null;
 
     protected TreePath treePath;
 
     /** Creates new form AttributeEditor */
     public AttributeEditor() {
         
         initComponents();
 
         if(!Beans.isDesignTime()) {
 
             ActionListener buttonListener = new ButtonListener();
             this.cancelButton.addActionListener(buttonListener);
             this.commitButton.addActionListener(buttonListener);
             //this.editButton.addActionListener(buttonListener);
             //this.pinButton.addActionListener(buttonListener);
 
             this.attributeTree.addTreeSelectionListener(new MetaObjectListener());
             this.attributeTree.setIgnoreInvisibleAttributes(false);
         }
         
     }
 
     public void setControlBarVisible(boolean isVisible) {
         controlBar.setVisible(isVisible);
     }
 
     public Vector<AbstractButton> getControlBarButtons() {
         Vector<AbstractButton> buttons = new Vector<AbstractButton>();
         buttons.add(commitButton);
         buttons.add(cancelButton);
         return buttons;
     }
     
 
     public void setTreeNode(TreePath treePath,Object node) {
         this.treePath=treePath;
         setTreeNode(node);
     }
     
     public void setTreeNode(Object node) {
         this.confirmEdit();
         
         this.attributeTree.setTreeNode(node);
         
         // wait for attribute thread
         synchronized(this.attributeTree) {
             try {
                 // 10 sec timeout
                 if(logger.isDebugEnabled())logger.debug("waiting for attribute thread to finish");
                 this.attributeTree.wait(10000);
             } catch(Throwable t) {
                 logger.error("thread synchronization failed", t);
             }
         }
         
         this.treeNode = node;
         
         if(this.attributeTree.getRootNode() != null && this.attributeTree.getRootNode() instanceof ObjectAttributeNode) {
             logger.info("setTreeNode(): initializing editor ");
             
             this.editor = new DefaultComplexMetaAttributeEditor();
             ObjectAttributeNode rootNode = (ObjectAttributeNode)this.attributeTree.getRootNode();
             MetaObject metaObject = rootNode.getMetaObject();
             
             MetaAttributeEditorLocator mael=new MetaAttributeEditorLocator();
             try {
                 //HELL
                 if (mael.getEditor(metaObject)!=null){
                     editor=(ComplexEditor)mael.getEditor(metaObject);
                     logger.debug("Editor :"+((ObjectTreeNode)treeNode).getMetaClass().getComplexEditor());
                 } else {
                     logger.warn("MetaAttributeEditorLocator returned null for object:"+metaObject);
                 }
                 
             } catch (Exception e) {
                 logger.info("setTreeNode(): initializing editor EXception", e);
             }
             
             //TimEasy: hier wird das Innere des Editors erzeugt und in die Scrollpane gesetzt
             Component editorComponent = this.editor.getEditorComponent(null, rootNode.getAttributeKey(), metaObject);
             this.editorScrollPane.getViewport().setView(editorComponent);
             
             this.commitButton.setEnabled(true);
             this.cancelButton.setEnabled(true);
             
             this.titleBar.setTitle(I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.titleBar.title")
                     + " (" + rootNode + ")");
         } else if(logger.isDebugEnabled()) {
             logger.warn("setTreeNode(): node is null or not of type ObjectAttributeNode");
             this.clear();
         }
         
     }
     
     protected void clear() {
         this.editorScrollPane.getViewport().setView(null);
         this.editor = null;
         this.treeNode = null;
         this.attributeTree.clear();
         
         this.commitButton.setEnabled(false);
         this.cancelButton.setEnabled(false);
         
         this.titleBar.setTitle(I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.titleBar.title"));
     }
     
     public Object getTreeNode() {
         return this.treeNode;
     }
     
     private void cancel() {
         if(editor != null) {
             editor.cancelEditing();
             
             if(logger.isDebugEnabled())logger.debug("cancel() rejecting changes in node " + this.treeNode);
             //this.clear();
             
             ObjectTreeNode objectTreeNode = (ObjectTreeNode)this.treeNode;
             
             // neuer Knoten
             if(objectTreeNode.isNew()) {
                 MethodManager.getManager().deleteTreeNode(ComponentRegistry.getRegistry().getCatalogueTree(), objectTreeNode);
             }
             
             this.attributeTree.setTreeNode(null);
             this.treeNode = null;
             this.clear();
         }
     }
     
     private Object commitBlocker=new Object();
     private void commit() {
         synchronized(commitBlocker) {
             editor.stopEditing();
             
             logger.info("commit() saving changes in node " + this.treeNode);
             this.editor.setValueChanged(false);
             
             final ObjectTreeNode objectTreeNode = (ObjectTreeNode)this.getTreeNode();
             
             ComponentRegistry.getRegistry().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             CismetThreadPool.execute(new Thread(new Runnable() {
                 public void run() {
                     MetaObject uneditedMetaObject = objectTreeNode.getMetaObject();
                     MetaObject editedMetaObject = (MetaObject)AttributeEditor.this.editor.getValue();
                     MetaObject savedMetaObject = null;
                     objectTreeNode.setChanged(true);
                     
                     // leere Attribute?
                     String emptyAttributeName = MethodManager.getManager().findEmptyAttributes(editedMetaObject);
                     if(emptyAttributeName == null) {
                         try {
                             // neuer Knoten
                             if(objectTreeNode.isNew()) {
                                 Link link;
                                 TreeNode parent = objectTreeNode.getParent();
                                 if(parent != null) {
                                     link = new Link(((DefaultMetaTreeNode)parent).getID(), objectTreeNode.getDomain());
                                 } else {
                                     logger.warn("commit(): node '" + objectTreeNode + "' has no parent node'");
                                     link = new Link(-1, objectTreeNode.getDomain());
                                 }
                                 
                                 logger.info("commit(): insert meta object: " + editedMetaObject.getName());
                                 savedMetaObject = SessionManager.getProxy().insertMetaObject(editedMetaObject, objectTreeNode.getDomain());
                                 
                                 // neues objekt zuweisen
                                 objectTreeNode.setMetaObject(savedMetaObject);
                                 
                                 logger.info("commit(): add node: " + objectTreeNode);
                                 Node node = SessionManager.getProxy().addNode(objectTreeNode.getNode(), link);
                                 
                                 // parent permissions zuweisen...
                                 node.setPermissions(((DefaultMetaTreeNode)objectTreeNode.getParent()).getNode().getPermissions());
                                 
                                 objectTreeNode.setNode(node);
                                 objectTreeNode.setNew(false);
                                 objectTreeNode.setChanged(false);
                                 
                                 //Component editorComponent = this.editor.getEditorComponent(null, this.attributeTree.getRootNode().getAttributeKey(), savedMetaObject);
                                 //this.editorScrollPane.getViewport().setView(editorComponent);
                             }
                             
                             else {
                                 logger.info("commit(): update meta object: " + editedMetaObject.getName());
                                 SessionManager.getProxy().updateMetaObject(editedMetaObject, objectTreeNode.getDomain());
                                 savedMetaObject = editedMetaObject;
                                 
                                 // neues altes objekt zuweisen
                                 objectTreeNode.setMetaObject(savedMetaObject);
                                 
                                 objectTreeNode.setChanged(false);
                             }
                             
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     // XXX event w\u00E4re besser ...
                                     if(logger.isDebugEnabled())logger.debug("invokeLater() performing GUI update");
                                     AttributeViewer attributeViewer = ComponentRegistry.getRegistry().getAttributeViewer();
                                     
                                     if(attributeViewer.getTreeNode() == AttributeEditor.this.getTreeNode()) {
                                         if(logger.isDebugEnabled())logger.debug("commit() updating attribute viewer with new tree node");
                                         attributeViewer.setTreeNode(AttributeEditor.this.getTreeNode());
                                     }
                                     
                                     //XXX i18n
                                     JOptionPane.showMessageDialog(AttributeEditor.this,
                                             I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.invokeLater().InfoMessage1") +
                                             objectTreeNode +
                                             I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.invokeLater().InfoMessage2"),
                                             I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.invokeLater().InfoTitle"),
                                             JOptionPane.INFORMATION_MESSAGE);
                                     
                                     //AttributeEditor.this.setTreeNodes(objectTreeNode);
                                     AttributeEditor.this.clear();
                                     
                                     ComponentRegistry.getRegistry().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                     
                                     try {
                                         ComponentRegistry.getRegistry().getCatalogueTree().scrollPathToVisible(ComponentRegistry.getRegistry().getCatalogueTree().getSelectionPath());
                                         //hier k\u00F6nnte jetzt noch zu dem TAB gewechselt werden das vom User gew\u00FCnscht ist
                                     } catch (Exception e) {
                                         logger.warn("can not scroll to selected object.",e);
                                     }
                                     //((MutableTreeNode)AttributeEditor.this.getTreeNode())
                                 }
                             });
                         } catch(Throwable t) {
                             logger.error("add / insert of meta object '" + objectTreeNode.getMetaObject() + "' failed", t);
                             ExceptionManager.getManager().showExceptionDialog(
                                     ExceptionManager.WARNING,
                                     I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.commit().insertError.title"),
                                     I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.commit().insertError.message"), t);
                             ComponentRegistry.getRegistry().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                         }
                     } else {
                         //XXX i18n
                         JOptionPane.showMessageDialog(AttributeEditor.this,
                                 I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.commit().ErrorMessage1") + emptyAttributeName +
                                 I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.commit().ErrorMessage2"),
                                 I18N.getString("Sirius.navigator.ui.attributes.editorSirius.navigator.ui.attributes.editor.AttributeEditor.commit().ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                         ComponentRegistry.getRegistry().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     }
                 }
             }, "commitEditThread"));
         }
     }
     
     /**
      *
      */
     private void confirmEdit() {
         if(this.isChanged()) {
             if(JOptionPane.YES_NO_OPTION == JOptionPane.showOptionDialog(
                     AttributeEditor.this,
                     I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.confirmEdit().JOptionPane.message"),
                     I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.confirmEdit().JOptionPane.title"),
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                     null, new String[]
             {I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.confirmEdit().JOptionPane.option1"),
              I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.confirmEdit().JOptionPane.option2")},
              I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.confirmEdit().JOptionPane.option1"))) {
                 this.commit();
             } else {
                 this.cancel();
             }
         } else {
             if(logger.isDebugEnabled())logger.debug("confirmEdit(): no changes detected");
             this.cancel();
         }
     }
     
     public boolean isChanged() {
         logger.debug("this.editor: " + this.editor);
         if(this.editor != null)logger.debug("this.editor.isValueChanged(): " + this.editor.isValueChanged());
         
         if(this.editor != null && (this.editor.isValueChanged() || ((DefaultMetaTreeNode)this.treeNode).isNew())) {
             return true;
         }
         
         return false;
     }
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         javax.swing.JToggleButton pinButton = new javax.swing.JToggleButton();
         javax.swing.JToggleButton editButton = new javax.swing.JToggleButton();
         controlBar = new javax.swing.JPanel();
         titleBar = new Sirius.navigator.ui.widget.TitleBar();
         commitButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         switchPanel = new javax.swing.JPanel();
         javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane();
         editorScrollPane = new javax.swing.JScrollPane();
         javax.swing.JScrollPane treeScrollPane = new javax.swing.JScrollPane();
         attributeTree = new Sirius.navigator.ui.attributes.AttributeTree();
 
         pinButton.setIcon(resources.getIcon(resources.getString("attribute.viewer.pin.icon")));
         pinButton.setToolTipText(resources.getString("attribute.viewer.pin.tooltip"));
         pinButton.setActionCommand("pin");
         pinButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pinButton.setContentAreaFilled(false);
         pinButton.setFocusPainted(false);
         pinButton.setMaximumSize(new java.awt.Dimension(16, 16));
         pinButton.setMinimumSize(new java.awt.Dimension(16, 16));
         pinButton.setPreferredSize(new java.awt.Dimension(16, 16));
         pinButton.setRolloverIcon(resources.getIcon(resources.getString("attribute.viewer.pin.icon.rollover")));
         pinButton.setRolloverSelectedIcon(resources.getIcon(resources.getString("attribute.viewer.pin.icon.selected.rollover")));
         pinButton.setSelectedIcon(resources.getIcon(resources.getString("attribute.viewer.pin.icon.selected")));
 
         editButton.setIcon(resources.getIcon(resources.getString("attribute.viewer.edit.icon")));
         editButton.setToolTipText(resources.getString("attribute.viewer.edit.tooltip"));
         editButton.setActionCommand("edit");
         editButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         editButton.setContentAreaFilled(false);
         editButton.setFocusPainted(false);
         editButton.setMaximumSize(new java.awt.Dimension(16, 16));
         editButton.setMinimumSize(new java.awt.Dimension(16, 16));
         editButton.setPreferredSize(new java.awt.Dimension(16, 16));
         editButton.setRolloverIcon(resources.getIcon(resources.getString("attribute.viewer.edit.icon.rollover")));
         editButton.setRolloverSelectedIcon(resources.getIcon(resources.getString("attribute.viewer.edit.icon.selected.rollover")));
         editButton.setSelectedIcon(resources.getIcon(resources.getString("attribute.viewer.edit.icon.selected")));
 
         setLayout(new java.awt.BorderLayout());
 
         controlBar.setLayout(new java.awt.GridBagLayout());
 
         titleBar.setIcon(resources.getIcon("floatingframe.gif"));
         titleBar.setTitle(resources.getString("attribute.editor.title"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         controlBar.add(titleBar, gridBagConstraints);
 
         commitButton.setIcon(resources.getIcon(resources.getString("attribute.viewer.commit.icon")));
         commitButton.setToolTipText(resources.getString("attribute.viewer.commit.tooltip"));
         commitButton.setActionCommand("commit");
         commitButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         commitButton.setContentAreaFilled(false);
         commitButton.setEnabled(false);
         commitButton.setFocusPainted(false);
         commitButton.setMaximumSize(new java.awt.Dimension(16, 16));
         commitButton.setMinimumSize(new java.awt.Dimension(16, 16));
         commitButton.setPreferredSize(new java.awt.Dimension(16, 16));
         commitButton.setRolloverIcon(resources.getIcon(resources.getString("attribute.viewer.commit.icon.rollover")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
         controlBar.add(commitButton, gridBagConstraints);
 
         cancelButton.setIcon(resources.getIcon(resources.getString("attribute.viewer.cancel.icon")));
         cancelButton.setToolTipText(resources.getString("attribute.viewer.cancel.tooltip"));
         cancelButton.setActionCommand("cancel");
         cancelButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         cancelButton.setContentAreaFilled(false);
         cancelButton.setEnabled(false);
         cancelButton.setFocusPainted(false);
         cancelButton.setMaximumSize(new java.awt.Dimension(16, 16));
         cancelButton.setMinimumSize(new java.awt.Dimension(16, 16));
         cancelButton.setPreferredSize(new java.awt.Dimension(16, 16));
         cancelButton.setRolloverIcon(resources.getIcon(resources.getString("attribute.viewer.cancel.icon.rollover")));
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
 
         switchPanel.add(splitPane, "table");
 
         add(switchPanel, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private Sirius.navigator.ui.attributes.AttributeTree attributeTree;
     private javax.swing.JButton cancelButton;
     private javax.swing.JButton commitButton;
     private javax.swing.JPanel controlBar;
     private javax.swing.JScrollPane editorScrollPane;
     private javax.swing.JPanel switchPanel;
     private Sirius.navigator.ui.widget.TitleBar titleBar;
     // End of variables declaration//GEN-END:variables
     
     
     
     private class MetaObjectListener implements TreeSelectionListener {
         public void valueChanged(TreeSelectionEvent e) {
             if(AttributeEditor.this.editor != null) {
                 LinkedList activeChildEditorTree = new LinkedList();
                 Object[] objects = e.getPath().getPath();
                 for (int i = 0; i < objects.length; i++) {
                     if(objects[i] instanceof ObjectAttributeNode) {
                         activeChildEditorTree.addLast(((ObjectAttributeNode)objects[i]).getAttributeKey());
                     } else if(logger.isDebugEnabled())logger.warn("valueChanged(): node '" + objects[i] + "' is no object tree node");
                 }
                 
                 if(logger.isDebugEnabled())logger.debug("valueChanged(): selection editor for selected object tree node");
                 AttributeEditor.this.editor.setActiveChildEditorTree(activeChildEditorTree);
                 
                 /*if(e.isAddedPath())
                 {
                  
                 }
                 else if(logger.isDebugEnabled())logger.debug("valueChanged(): ignoring selection event");*/
             } else if(logger.isDebugEnabled()) {
                 logger.warn("editor is null");
             }
         }
     }
     
     private class ButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             if(logger.isDebugEnabled())logger.debug("actionPerformed(): action command: " + e.getActionCommand());
             if(e.getActionCommand().equals("commit")) {
                 // XXX
                 // Alle \u00C4nderungen im Objekt speichern:
                 editor.stopEditing();
                 
                 if(isChanged() &&
                         JOptionPane.YES_NO_OPTION == JOptionPane.showOptionDialog(
                         AttributeEditor.this,
                         I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.ButtonListener.JOptionPane.commit.message"),
                         I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.ButtonListener.JOptionPane.commit.title"),
                         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                         null, null, null)) {
                     
                     commit();
                 } else {
                     AttributeEditor.this.clear();
                 }
                 
             } else if(e.getActionCommand().equals("cancel") &&
                     JOptionPane.YES_NO_OPTION == JOptionPane.showOptionDialog(
                     AttributeEditor.this,
                     I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.ButtonListener.JOptionPane.cancel.message"),
                     I18N.getString("Sirius.navigator.ui.attributes.editor.AttributeEditor.ButtonListener.JOptionPane.cancel.title"),
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                 logger.error("unknown action command '" + e.getActionCommand() + "'");
                 cancel();
             } else {
                 logger.error("unknown action command '" + e.getActionCommand() + "'");
             }
             /*else if(e.getActionCommand().equals("edit"))
             {
                 confirmEdit();
                 DefaultMetaTreeNode node = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNode();
                 AttributeEditor.this.setTreeNodes(node);
             }
             else if(e.getActionCommand().equals("pin"))
             {
                 AttributeEditor.this.setUpdateEnabled(AttributeEditor.this.pinButton.isSelected());
             }
             /*if(e.getActionCommand().equals("edit"))
             {
                 // ask to save or revert changes
                 if(!editButton.isSelected())
                 {
                     confirmEdit();
                 }
              
                 commitButton.setEnabled(editButton.isSelected());
                 cancelButton.setEnabled(editButton.isSelected());
                 attributeTable.setEditable(editButton.isSelected());
             }
             else if(e.getActionCommand().equals("commit") &&
             changed &&
             JOptionPane.YES_NO_OPTION == JOptionPane.showOptionDialog(AttributeEditor.this, resources.getString("attribute.viewer.commit.message"), resources.getString("attribute.viewer.commit.tooltip"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null))
             {
                 commit();
             }
             else if(e.getActionCommand().equals("cancel") &&
             changed &&
             JOptionPane.YES_NO_OPTION == JOptionPane.showOptionDialog(AttributeEditor.this, resources.getString("attribute.viewer.cancel.message"), resources.getString("attribute.viewer.cancel.tooltip"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null))
             {
                 cancel();
             }
             else if(e.getActionCommand().equals("pin"))
             {
                 AttributeEditor.this.setUpdateEnabled(AttributeEditor.this.pinButton.isSelected());
             }*/
         }
     }
     
 }
 
