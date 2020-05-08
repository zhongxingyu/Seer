 /*
  * AttributeEditor.java
  *
  * Created on 1. Juli 2004, 13:42
  */
 package de.cismet.cids.editors;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.DescriptionPane;
 import Sirius.navigator.ui.attributes.AttributeViewer;
 import Sirius.navigator.ui.attributes.editor.AttributeEditor;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.StaticDebuggingTools;
 import de.cismet.tools.gui.ComponentWrapper;
 import de.cismet.tools.gui.WrappedComponent;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Vector;
 import java.util.logging.Level;
 import javax.swing.AbstractButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 import javax.swing.Timer;
 import javax.swing.tree.DefaultTreeModel;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author  pascal
  */
 public class NavigatorAttributeEditorGui extends AttributeEditor {
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private Object treeNode = null;
     private MetaObject backupObject = null;
     private MetaObject editorObject = null;
     private final ResourceManager resources;
     private JComponent wrappedWaitingPanel;
 
     /** Creates new form AttributeEditor */
     public NavigatorAttributeEditorGui() {
         this.resources = ResourceManager.getManager();
         initComponents();
         if (!StaticDebuggingTools.checkHomeForFile("cidsNavigatorGuiHiddenDebugControls")) {
             panDebug.setVisible(false);
         }
         ComponentWrapper cw = CidsObjectEditorFactory.getInstance().getComponentWrapper();
         if (cw != null) {
             wrappedWaitingPanel = (JComponent) cw.wrapComponent(lblEditorCreation);
         }
 
         //Nur fuer die Uebergangsphase sollange noch von AttributeEditor geerbt wird
         ActionListener[] alr = commitButton.getActionListeners();
         for (ActionListener al : alr) {
             commitButton.removeActionListener(al);
         }
         alr = cancelButton.getActionListeners();
         for (ActionListener al : alr) {
             cancelButton.removeActionListener(al);
         }
 
         commitButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (backupObject != null && !(backupObject.propertyEquals(editorObject)) || backupObject == null) {
                     int answer = JOptionPane.showConfirmDialog(NavigatorAttributeEditorGui.this, "Wollen Sie die gemachten Änderungen speichern?", "Speichern", JOptionPane.YES_NO_CANCEL_OPTION);
                     if (answer == JOptionPane.YES_OPTION) {
                         saveIt();
 
 
                     } else if (answer == JOptionPane.CANCEL_OPTION) {
                     } else {
                         reloadFromDB();
                         clear();
                     }
                 } else {
                     clear();
                 }
             }
         });
 
         cancelButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (backupObject != null && !(backupObject.propertyEquals(editorObject)) || backupObject == null) {
                     int answer = JOptionPane.showConfirmDialog(NavigatorAttributeEditorGui.this, "Sind Sie sicher dass Sie den Editor beenden wollen?", "Editor beenden ?", JOptionPane.YES_NO_OPTION);
                     if (answer == JOptionPane.YES_OPTION) {
                         reloadFromDB();
                         clear();
                     }
                 } else {
                     clear();
                 }
 
             }
         });
     }
 
     @Override
     public void setControlBarVisible(boolean isVisible) {
         controlBar.setVisible(isVisible);
     }
 
     @Override
     public Vector<AbstractButton> getControlBarButtons() {
         Vector<AbstractButton> buttons = new Vector<AbstractButton>();
         buttons.add(commitButton);
         buttons.add(cancelButton);
         buttons.add(copyButton);
         buttons.add(pasteButton);
         return buttons;
     }
 
     private void refreshAttributeTable() {
         AttributeViewer viewer = ComponentRegistry.getRegistry().getAttributeViewer();
         Object node = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNode();
         viewer.setTreeNode(node);
     }
 
     private void refreshTree() {
         if (treePath != null) {
             try {
                 MetaCatalogueTree metaCatalogueTree = ComponentRegistry.getRegistry().getCatalogueTree();
 
                 RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());
 
                 ((DefaultTreeModel) metaCatalogueTree.getModel()).setRoot(rootTreeNode);
                 ((DefaultTreeModel) metaCatalogueTree.getModel()).reload();
 
                 metaCatalogueTree.exploreSubtree(treePath);
             } catch (Exception e) {
                 log.error("Error when refreshing Tree", e);
             }
         }
     }
 
     private void createBackup(MetaObject orig) {
         try {
             int oid = orig.getID();
             int cid = orig.getMetaClass().getID();
             String domain = orig.getDomain();
             User user = SessionManager.getSession().getUser();
             backupObject = SessionManager.getConnection().getMetaObject(user, oid, cid, domain);
         } catch (Exception e) {
             log.error("Error during Backupcreation. Cannot detect whether the objects is changed.", e);
         }
     }
 
     private void reloadFromDB() {
         try {
             ObjectTreeNode otn = (ObjectTreeNode) treeNode;
             int oid = otn.getMetaObject().getID();
 
             if (oid == -1) {
                 return;
             }
 
             int cid = otn.getMetaObject().getMetaClass().getID();
             String domain = otn.getMetaObject().getDomain();
             User user = SessionManager.getSession().getUser();
 
 
             MetaObject reloaded = SessionManager.getConnection().getMetaObject(user, oid, cid, domain);
             reloaded.setAllClasses();
             log.debug("Reloaded MO:" + reloaded.getDebugString());
             otn.setMetaObject(reloaded);
 
             refreshAttributeTable();
         } catch (Exception e) {
             log.error("Error durig reload from DB.", e);
             ErrorInfo ei = new ErrorInfo("Fehler beim Refresh.",
                     "Das Objekt konnte nicht wieder vom Server geladen werden. Die aktuelle Objektversion ist nicht gültig!",
                     null,
                     null,
                     e,
                     Level.SEVERE,
                     null);
             JXErrorPane.showDialog(this, ei);
         }
     }
 
     private void saveIt() {
         ObjectTreeNode otn = (ObjectTreeNode) treeNode;
         MetaObject mo = otn.getMetaObject();
         try {
             mo.getBean().persist();
             refreshTree();
             JOptionPane jop = new JOptionPane("Objekt wurde gespeichert.", JOptionPane.INFORMATION_MESSAGE);
 
             final JDialog dialog = jop.createDialog(NavigatorAttributeEditorGui.this, "Speichern erfolgreich");
 
             Timer t = new Timer(900, new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     dialog.setVisible(false);
                     dialog.dispose();
 
                 }
             });
             t.setRepeats(false);
             t.start();
             dialog.setVisible(true);
             clear();
         } catch (Exception ex) {
             log.error("Fehler beim Speichern", ex);
             ErrorInfo ei = new ErrorInfo("Fehler", "Beim Speichern des Objektes ist ein Fehler aufgetreten.", null, null, ex, Level.SEVERE, null);
             JXErrorPane.showDialog(NavigatorAttributeEditorGui.this, ei);
         }
         clear();
     }
 
     @Override
     public void setTreeNode(Object node) {
         if (treeNode != null && editorObject != null && backupObject != null) {
             if (!editorObject.propertyEquals(backupObject)) {
                 int answer = JOptionPane.showConfirmDialog(NavigatorAttributeEditorGui.this, "Wollen Sie die gemachten Änderungen speichern, bevor Sie ein neues Objekt editieren?", "Objekt ist noch nicht gespeichert.", JOptionPane.YES_NO_CANCEL_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     saveIt();
                 } else if (answer == JOptionPane.NO_OPTION) {
                     reloadFromDB();
                 } else { //Cancel
                     return;
                 }
             }
         }
         final AttributeViewer viewer = ComponentRegistry.getRegistry().getAttributeViewer();
         final MetaCatalogueTree tree = ComponentRegistry.getRegistry().getActiveCatalogue();
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 scpEditor.getViewport().removeAll();
                 scpEditor.getViewport().setView(wrappedWaitingPanel);
                 NavigatorAttributeEditorGui.this.revalidate();
             }
         });
         treeNode = node;
         if (treeNode instanceof ObjectTreeNode) {
//            final DescriptionPane desc = ComponentRegistry.getRegistry().getDescriptionPane();
             CismetThreadPool.execute(new SwingWorker<JComponent, Void>() {
 
                 @Override
                 protected JComponent doInBackground() throws Exception {
                     ObjectTreeNode otn = (ObjectTreeNode) treeNode;
                     editorObject = otn.getMetaObject();
                     createBackup(editorObject);
 
                     editorObject.getBean().addPropertyChangeListener(new PropertyChangeListener() {
 
                         @Override
                         public void propertyChange(PropertyChangeEvent evt) {
                             viewer.repaint();
                             tree.repaint();
                             //commitButton.setEnabled(!backupObject.propertyEquals(mo)); //vielleicht einfach zuviel des guten
 
                         }
                     });
 //                    try {
 //                        EditorBeanInitializerStore.getInstance().initialize(editorObject.getBean());
 //                    } catch (Exception ex) {
 //                        log.error("Exception while initializing Bean with template values!", ex);
 //                    }
                     JComponent ed = CidsObjectEditorFactory.getInstance().getEditor(editorObject);
                     return ed;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         JComponent ed = get();
                         log.debug("editor:" + ed);
                         scpEditor.getViewport().removeAll();
                         scpEditor.getViewport().setView(ed);
                         if (ed instanceof WrappedComponent) {
                            ed = ((WrappedComponent) ed).getOriginalComponent();
                        }
                        if (ed instanceof CidsBeanStore) {
                            currentBeanStore = (CidsBeanStore) ed;
                            currentInitializer = EditorBeanInitializerStore.getInstance().getInitializer(editorObject.getMetaClass());
                            cancelButton.setEnabled(true);
                            commitButton.setEnabled(true);
                            copyButton.setEnabled(true);
                            if (currentInitializer != null) {
                                //enable editor attribute paste only for new MOs
                                boolean isNewBean = currentBeanStore.getCidsBean().getMetaObject().getStatus() == MetaObject.NEW;
                                pasteButton.setEnabled(isNewBean);
                             }
                         }
                         NavigatorAttributeEditorGui.this.revalidate();
                         log.debug("editor added");
                     } catch (Exception e) {
                         ErrorInfo ei = new ErrorInfo("Fehler", "Beim Erzeugen des Editors ist ein Fehler aufgetreten.", null, null, e, Level.SEVERE, null);
                         log.error("Fehler beim Darstellen des Editors" + ei.getState() + editorObject.getDebugString() + "\n", e);
                         JXErrorPane.showDialog(NavigatorAttributeEditorGui.this, ei);
                         clear();
                     }
                 }
             });
 
         } else {
             log.warn("Uebergebener Treenode ist keine ObjectTreeMode, sondern: " + node.getClass());
         }
     }
     private BeanInitializer currentInitializer = null;
     private CidsBeanStore currentBeanStore = null;
 
     @Override
     protected void clear() {
         currentInitializer = null;
         currentBeanStore = null;
         scpEditor.getViewport().removeAll();
         commitButton.setEnabled(false);
         cancelButton.setEnabled(false);
         copyButton.setEnabled(false);
         pasteButton.setEnabled(false);
         revalidate();
         repaint();
         ComponentRegistry.getRegistry().getAttributeViewer().repaint();
         this.treeNode = null;
         backupObject = null;
         editorObject = null;
     }
 
     public Object getTreeNode() {
         return treeNode;
     }
 
     public boolean isChanged() {
 
         return true;
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
         editorScrollPane = new javax.swing.JScrollPane();
         lblEditorCreation = new javax.swing.JLabel();
         controlBar = new javax.swing.JPanel();
         commitButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         copyButton = new javax.swing.JButton();
         pasteButton = new javax.swing.JButton();
         switchPanel = new javax.swing.JPanel();
         scpEditor = new javax.swing.JScrollPane();
         panDebug = new javax.swing.JPanel();
         jButton1 = new javax.swing.JButton();
 
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
 
         editorScrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
 
         lblEditorCreation.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblEditorCreation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Sirius/navigator/resource/img/load.png"))); // NOI18N
 
         controlBar.setLayout(new java.awt.GridBagLayout());
 
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
 
         copyButton.setIcon(resources.getIcon(resources.getString("attribute.viewer.copy.icon")));
         copyButton.setToolTipText(resources.getString("attribute.viewer.copy.tooltip"));
         copyButton.setActionCommand("cancel");
         copyButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         copyButton.setContentAreaFilled(false);
         copyButton.setEnabled(false);
         copyButton.setFocusPainted(false);
         copyButton.setMaximumSize(new java.awt.Dimension(16, 16));
         copyButton.setMinimumSize(new java.awt.Dimension(16, 16));
         copyButton.setPreferredSize(new java.awt.Dimension(16, 16));
         copyButton.setRolloverIcon(resources.getIcon(resources.getString("attribute.viewer.copy.icon.rollover")));
         copyButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 copyButtonActionPerformed(evt);
             }
         });
         controlBar.add(copyButton, new java.awt.GridBagConstraints());
 
         pasteButton.setIcon(resources.getIcon(resources.getString("attribute.viewer.paste.icon")));
         pasteButton.setToolTipText(resources.getString("attribute.viewer.paste.tooltip"));
         pasteButton.setActionCommand("paste");
         pasteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pasteButton.setContentAreaFilled(false);
         pasteButton.setEnabled(false);
         pasteButton.setFocusPainted(false);
         pasteButton.setMaximumSize(new java.awt.Dimension(16, 16));
         pasteButton.setMinimumSize(new java.awt.Dimension(16, 16));
         pasteButton.setPreferredSize(new java.awt.Dimension(16, 16));
         pasteButton.setRolloverIcon(resources.getIcon(resources.getString("attribute.viewer.paste.icon.rollover")));
         pasteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pasteButtonActionPerformed(evt);
             }
         });
         controlBar.add(pasteButton, new java.awt.GridBagConstraints());
 
         add(controlBar, java.awt.BorderLayout.NORTH);
 
         switchPanel.setLayout(new java.awt.BorderLayout());
         switchPanel.add(scpEditor, java.awt.BorderLayout.CENTER);
 
         panDebug.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
 
         jButton1.setText("log MetaObject");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
         panDebug.add(jButton1);
 
         switchPanel.add(panDebug, java.awt.BorderLayout.NORTH);
 
         add(switchPanel, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         if (getTreeNode() != null && getTreeNode() instanceof ObjectTreeNode) {
             MetaObject mo = ((ObjectTreeNode) getTreeNode()).getMetaObject();
             log.fatal("Current MetaObject:" + mo.getDebugString());
             EditorBeanInitializerStore.getInstance().registerInitializer(mo.getMetaClass(), new DefaultBeanInitializer(mo.getBean()));
         }
 
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
         if (currentBeanStore != null) {
             CidsBean bean = currentBeanStore.getCidsBean();
             boolean isNewBean = bean.getMetaObject().getStatus() == MetaObject.NEW;
             if (currentBeanStore instanceof BeanInitializerProvider) {
                 BeanInitializerProvider beanInitProvider = (BeanInitializerProvider) currentBeanStore;
                 currentInitializer = beanInitProvider.getBeanInitializer();
                 if (currentInitializer != null) {
                     EditorBeanInitializerStore.getInstance().registerInitializer(bean.getMetaObject().getMetaClass(), currentInitializer);
                 } else {
                     log.error("BeanInitializerProvider delivers null as initializer.");
                 }
             } else {
                 currentInitializer = new DefaultBeanInitializer(bean);
                 EditorBeanInitializerStore.getInstance().registerInitializer(bean.getMetaObject().getMetaClass(), currentInitializer);
             }
             //enable editor attribute paste only for new MOs
             pasteButton.setEnabled(currentInitializer != null && isNewBean);
         }
     }//GEN-LAST:event_copyButtonActionPerformed
 
     private void pasteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteButtonActionPerformed
         if (currentBeanStore != null) {
             CidsBean bean = currentBeanStore.getCidsBean();
             try {
                 EditorBeanInitializerStore.getInstance().initialize(bean);
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
     }//GEN-LAST:event_pasteButtonActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton cancelButton;
     private javax.swing.JButton commitButton;
     private javax.swing.JPanel controlBar;
     private javax.swing.JButton copyButton;
     private javax.swing.JScrollPane editorScrollPane;
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel lblEditorCreation;
     private javax.swing.JPanel panDebug;
     private javax.swing.JButton pasteButton;
     private javax.swing.JScrollPane scpEditor;
     private javax.swing.JPanel switchPanel;
     // End of variables declaration//GEN-END:variables
 }
 
