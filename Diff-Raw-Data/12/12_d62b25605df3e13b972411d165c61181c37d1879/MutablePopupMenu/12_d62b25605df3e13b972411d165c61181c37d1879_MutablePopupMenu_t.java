 package Sirius.navigator.ui;
 
 /*******************************************************************************
  *
  * Copyright (c)	:	EIG (Environmental Informatics Group)
  * http://www.htw-saarland.de/eig
  * Prof. Dr. Reiner Guettler
  * Prof. Dr. Ralf Denzer
  *
  * HTWdS
  * Hochschule fuer Technik und Wirtschaft des Saarlandes
  * Goebenstr. 40
  * 66117 Saarbruecken
  * Germany
  *
  * Programmers		:	Pascal
  *
  * Project			:	WuNDA 2
  * Filename		:
  * Version			:	1.0
  * Purpose			:
  * Created			:	08.05.2000
  * History			:
  *
  *******************************************************************************/
 //import java.net.URL;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import org.apache.log4j.Logger;
 
 import Sirius.navigator.plugin.ui.*;
 import Sirius.navigator.connection.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.ui.embedded.*;
 import Sirius.navigator.ui.tree.*;
 import Sirius.navigator.ui.dialog.*;
 import Sirius.navigator.ui.tree.editor.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.plugin.interfaces.PluginMethod;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.permission.PermissionHolder;
 import Sirius.server.newuser.permission.Policy;
 import de.cismet.cids.utils.MetaTreeNodeVisualization;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 public class MutablePopupMenu extends JPopupMenu {
 
     private final static Logger logger = Logger.getLogger(MutablePopupMenu.class);
     private final PluginMenuesMap pluginMenues = new PluginMenuesMap();
     protected PopupMenuItemsActionListener itemActionListener;
     // statische Men\u00FCeintr\u00E4ge
     protected JMenuItem searchItem, passwordItem, specialTreeItem, newNode, editNode, editObject, deleteNode, deleteObject, newObject;
     private TreeEditorMenu treeEditorMenu = null;
 
     public MutablePopupMenu() {
         this.createDefaultMenues();
     }
 
     /**
      * Initialisierungsmethode<br>
      * Wird nur von den Konstruktoren aufgerufen
      */
     protected void createDefaultMenues() {
         itemActionListener = new PopupMenuItemsActionListener();
 
         searchItem = new JMenuItem("Suche");
         searchItem.setActionCommand("search");
         searchItem.addActionListener(itemActionListener);
         this.add(searchItem);
 
         specialTreeItem = new JMenuItem("");
         specialTreeItem.setActionCommand("treecommand");
         specialTreeItem.addActionListener(itemActionListener);
         this.add(specialTreeItem);
 
         //Hier evtl showInMap hinzufuegen
 
 
         this.add(new JSeparator(JSeparator.HORIZONTAL));
 
         this.addPopupMenuListener(new DynamicPopupMenuListener());
 
 //        JMenuItem edit = new JMenuItem();
 //        edit.setText(ResourceManager.getManager().getString("tree.editor.menu.name"));
 //        edit.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.icon")));
 //        this.add(edit);
 
         newNode = new NewNodeMethod();
         editNode = new EditNodeMethod();
         editObject = new EditObjectMethod();
         deleteNode = new DeleteNodeMethod();
         editObject = new EditObjectMethod();
         deleteObject = new DeleteObjectMethod();
         newObject = new NewObjectMethod();
 
         this.add(newNode);
         this.add(editNode);
         this.add(editObject);
         this.add(deleteNode);
         this.add(editObject);
         this.add(deleteObject);
         this.add(newObject);
         this.add(new ExploreSubTreeMethod());
 
         hideEditMenues();
     }
 
     private void hideEditMenues() {
         newNode.setVisible(false);
         editNode.setVisible(false);
         editObject.setVisible(false);
         deleteNode.setVisible(false);
         editObject.setVisible(false);
         deleteObject.setVisible(false);
         newObject.setVisible(false);
     }
 
     public void addPluginMenu(EmbeddedMenu menu) {
         if (menu.getItemCount() > 0) {
             this.pluginMenues.add(menu);
 
         } else if (logger.isDebugEnabled()) {
             logger.warn("menu '" + menu.getId() + "' does not contain any items, ignoring menu");
         }
     }
 
     public void removePluginMenu(String id) {
         this.pluginMenues.remove(id);
     }
 
     public void setPluginMenuEnabled(String id, boolean enabled) {
         this.pluginMenues.setEnabled(id, enabled);
     }
 
     public boolean isPluginMenuEnabled(String id) {
         return this.pluginMenues.isEnabled(id);
     }
 
     public boolean isPluginMenuAvailable(String id) {
         return this.pluginMenues.isAvailable(id);
     }
 
     // INNERE KLASSEN ZUM BEARBEITEN DER EREIGNISSE ============================
     /**
      * Innere Klasse zum verarbeiten des PopupMenuEvent.<br>
      * Wenn das PopupMenu sichtbar wird, werden anhand der im Baum selektierten
      * Objekte und deren Attribute die dynamischen Menues erzeugt.
      */
     class DynamicPopupMenuListener implements PopupMenuListener {
         //int[] classNodeIDs = null;
         //Method[] methodIDs = null;
 
         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
             if (metaCatalogueTree == null) {
                 metaCatalogueTree = ComponentRegistry.getRegistry().getCatalogueTree();
             }
 
             if (resources == null) {
                 resources = ResourceManager.getManager();
             }
 
             if (treeNodeEditor == null) {
                 treeNodeEditor = new TreeNodeEditor(ComponentRegistry.getRegistry().getMainWindow(), true);
             }
 
             logger.info("showing popup menu");
             // lazily construct tree editor menues
             //treeEditorMenu wird immer neu angelegt, damit auf unterschiedliche Anforderungen des selected Nodes reagiert werden kann
 
             if (treeEditorMenu == null && PropertyManager.getManager().isEditable()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("creating new tree editor menu");
                 }
 //                treeEditorMenu = new TreeEditorMenu(ComponentRegistry.getRegistry().getMainWindow(), ComponentRegistry.getRegistry().getCatalogueTree());
 //                addPluginMenu(treeEditorMenu);
                 Node node = metaCatalogueTree.getSelectedNode().getNode();
                 if (node.getId() != -1 && node.getDynamicChildrenStatement() == null) {
                     //Kein dynamischer Knoten, keine dynamischen Kinder
                     newNode.setVisible(true);
                     editNode.setVisible(true);
                     deleteNode.setVisible(true);
                     editObject.setVisible(true);
                 } else if (node instanceof MetaObjectNode && node.getId() == -1) {
                     //DynamicObjectNode
 
                     //EditObject
                     editObject.setVisible(true);
 
                     //DeleteObject
                     deleteObject.setVisible(true);
                 } else if (node instanceof MetaNode && node.getClassId() != -1) {
                     int classID = node.getClassId();
                     String domain = node.getDomain();
                     try {
                         ((NewObjectMethod) newObject).init(classID, domain);
                         newObject.setVisible(true);
                     } catch (Exception ex) {
                         logger.error("Error when adding the NewObjectMethodMenuItem", ex);
                     }
                 }
             }
 
             // Text mit Unicode....
             // viel SPass beim /u0000DCbersetzen ;o)
             if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof SearchResultsTree) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("showing default search tree menues");
                 }
                 specialTreeItem.setText(ResourceManager.getManager().getString("menu.popup.deleteEntries"));
                 if (treeEditorMenu != null) {
                     setPluginMenuEnabled(treeEditorMenu.getId(), false);
                 }
             } else if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof MetaCatalogueTree) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("showing default catalogue menues");
                 }
                 specialTreeItem.setText(ResourceManager.getManager().getString("menu.popup.adoptInTree"));
                 if (treeEditorMenu != null) {
                     setPluginMenuEnabled(treeEditorMenu.getId(), true);
                 }
             }
 
             // enable/disable menues
             if (logger.isDebugEnabled()) {
                 logger.debug("setting menues availability");
             }
             MutablePopupMenu.this.pluginMenues.setAvailability(MethodManager.getManager().getMethodAvailability());
         }
 
         public void popupMenuCanceled(PopupMenuEvent e) {
         }
 
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
             //entfernen des TreeEditorMenues bewirkt dass immer die richtigen menues angezeigt werden
 //            if (treeEditorMenu != null) {
 //                removePluginMenu(treeEditorMenu.getId());
 //                treeEditorMenu = null;
 //            }
 
             hideEditMenues();
 
         }
     }
 
     class PopupMenuItemsActionListener implements ActionListener {
 
         Sirius.server.localserver.attribute.Attribute[] attrArray;
         DefaultMetaTreeNode[] mtnArray;
         String[] koordinatenKatalog = null;
 
         public void actionPerformed(ActionEvent e) {
 
             if (e.getActionCommand().equals("search")) {
                 try {
                     // TODO select class nodes in searchtree
                     //MethodManager.getManager().showSearchDialog(true);
                     MethodManager.getManager().showSearchDialog();
                 } catch (Throwable t) {
                     logger.error("Fehler bei der Verarbeitung der Suchmethode", t);
 
                     ErrorDialog errorDialog = new ErrorDialog(ResourceManager.getManager().getString("menu.popup.searchError"), t.toString(), ErrorDialog.WARNING);
                     errorDialog.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
                     errorDialog.show();
                 }
             } else if (e.getActionCommand().equals("Passwort aendern")) {
                 MethodManager.getManager().showPasswordDialog();
             } else if (e.getActionCommand().equals("treecommand")) {
                 MethodManager.getManager().callSpecialTreeCommand();
             }
         }
     }
 
     public void show(Component invoker, int x, int y) {
         super.show(invoker, x, y);
     }
 
     private class PluginMenuesMap extends AbstractEmbeddedComponentsMap {
 
         private PluginMenuesMap() {
             Logger.getLogger(PluginMenuesMap.class);
         }
 
         protected void doAdd(EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutablePopupMenu.this.add((EmbeddedMenu) component);
             } else {
                 this.logger.error("doAdd(): invalid object type '" + component.getClass().getName() + "', 'Sirius.navigator.EmbeddedMenu' expected");
             }
         }
 
         protected void doRemove(EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutablePopupMenu.this.remove((EmbeddedMenu) component);
             } else {
                 this.logger.error("doRemove(): invalid object type '" + component.getClass().getName() + "', 'Sirius.navigator.EmbeddedMenu' expected");
             }
         }
 
         private void setAvailability(MethodAvailability methodAvailability) {
             Iterator iterator = this.getEmbeddedComponents();
             while (iterator.hasNext()) {
                 ((PluginMenu) iterator.next()).setAvailability(methodAvailability);
             }
         }
     }
 
     protected class NewNodeMethod extends PluginMenuItem implements PluginMethod {
 
         public NewNodeMethod() {
             super(MethodManager.PURE_NODE);
 
             this.pluginMethod = this;
 
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.new.name"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.new.icon")));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
             DefaultMetaTreeNode selectedNode = metaCatalogueTree.getSelectedNode();
 
             if (logger.isDebugEnabled()) {
                 logger.debug("NewNodeMethod: creating new node as parent of " + selectedNode);
             }
             if (selectedNode != null && selectedNode.isPureNode()) {
                 String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
                 //Sirius.server.newuser.permission.Permission perm = SessionManager.getSession().getWritePermission();
 
                 if (selectedNode.getNode().getPermissions().hasPermission(key, PermissionHolder.WRITEPERMISSION)) {
                     // knoten aufklappen
                     if (!selectedNode.isLeaf() && !selectedNode.isExplored()) {
                         if (logger.isDebugEnabled()) {
                             logger.warn("NewNodeMethod: parent node is not explored");
                         }
                         try {
                             metaCatalogueTree.expandPath(new TreePath(selectedNode.getPath()));
                             //selectedNode.explore();
                             //((DefaultTreeModel)metaCatalogueTree.getModel()).nodeStructureChanged(selectedNode);
                         } catch (Exception exp) {
                             logger.error("could not explore node: " + selectedNode, exp);
                         }
                     }
 
 
                     treeNodeEditor.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
                     DefaultMetaTreeNode metaTreeNode = treeNodeEditor.createTreeNode();
 
                     if (metaTreeNode != null) {
                         metaTreeNode.setNew(true);
                         // damit beim Aufklappen nicht die explore methode aufgerufen wird
                         metaTreeNode.setExplored(true);
 
                         // das endg\u00FCltige Hinzuf\u00FCgen erledigt der Attribut Editor
                         if (metaTreeNode.isObjectNode()) {
                             if (!ComponentRegistry.getRegistry().getAttributeEditor().isChanged()) {
                                 MethodManager.getManager().addTreeNode(metaCatalogueTree, selectedNode, metaTreeNode);
 
                                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                                 ComponentRegistry.getRegistry().getAttributeEditor().setTreeNode(metaCatalogueTree.getSelectionPath(), metaTreeNode);
                             } else {
                                 logger.warn("could not create new object node: edited object still unsaved");
                                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), "<html><p>Es kann kein neues Objekt erstellt werden, da z.Z. ein anderes Objekt bearbeitet wird.</p><p>Speichern sie das bearbeitete Objekt und versuchen sie es erneut.</p></html>", "Neues Objekt", JOptionPane.INFORMATION_MESSAGE);
                             }
                         } else {
                             if (MethodManager.getManager().addNode(metaCatalogueTree, selectedNode, metaTreeNode)) {
                                 MethodManager.getManager().addTreeNode(metaCatalogueTree, selectedNode, metaTreeNode);
                                 metaTreeNode.setNew(false);
                             } else if (logger.isDebugEnabled()) {
                                 logger.warn("addNode failed, omitting addTreeNode");
                             }
                         }
                     }
                 } else {
                     logger.warn("no permission to create node");
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), ResourceManager.getManager().getString("tree.editor.menu.new.nopermission"), ResourceManager.getManager().getString("tree.editor.menu.new.nopermission.title"), JOptionPane.WARNING_MESSAGE);
                 }
             } else {
                 logger.warn("parent node '" + selectedNode + "' is no pure node");
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), ResourceManager.getManager().getString("tree.editor.menu.new.nopurenode.1") + selectedNode + ResourceManager.getManager().getString("tree.editor.menu.new.nopurenode.2"), ResourceManager.getManager().getString("tree.editor.menu.new.nopurenode.title"), JOptionPane.WARNING_MESSAGE);
             }
         }
     }
 
     protected class NewObjectMethod extends PluginMenuItem implements PluginMethod {
 
         int classID = -1;
         String domain = null;
         MetaClass metaClass = null;
 
         public void init(int classID, String domain) throws Exception {
             this.classID = classID;
             this.domain = domain;
             metaClass = SessionManager.getProxy().getMetaClass(classID, domain);
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.newobject.name") + " (" + metaClass.getName() + ")");
         }
 
         public NewObjectMethod() {
             super(MethodManager.PURE_NODE);
             this.pluginMethod = this;
 
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.newobject.name"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.newobject.icon")));
             //funzt eh nicht
             //this.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
 
             DefaultMetaTreeNode selectedNode = metaCatalogueTree.getSelectedNode();
             if (metaClass.getPermissions().hasWritePermission(SessionManager.getSession().getUser().getUserGroup())) {
 
                 MetaObject MetaObject = metaClass.getEmptyInstance();
                 MetaObject.setStatus(MetaObject.NEW);
                 MetaObjectNode MetaObjectNode = new MetaObjectNode(-1, SessionManager.getSession().getUser().getDomain(), MetaObject, null, null, true, Policy.createWIKIPolicy(), -1, null, false);
                 DefaultMetaTreeNode metaTreeNode = new ObjectTreeNode(MetaObjectNode);
                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                 ComponentRegistry.getRegistry().getAttributeEditor().setTreeNode(metaCatalogueTree.getSelectionPath(), metaTreeNode);
             }
 
         }
     }
 
     protected class EditNodeMethod extends PluginMenuItem implements PluginMethod {
 
         public EditNodeMethod() {
             super(MethodManager.PURE_NODE + MethodManager.OBJECT_NODE + MethodManager.CLASS_NODE);
 
             this.pluginMethod = this;
 
             // XXX i18n
             this.setText(ResourceManager.getManager().getString("Sirius.navigator.ui.tree.editor.TreeEditorMenu.EditNodeMethod.Text"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.edit.icon")));
             this.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
             DefaultMetaTreeNode selectedNode = metaCatalogueTree.getSelectedNode();
             if (MethodManager.getManager().checkPermission(selectedNode.getNode(), PermissionHolder.WRITEPERMISSION)) {
                 if (selectedNode.getParent() != null && !(selectedNode.getParent() instanceof RootTreeNode)) {
                     treeNodeEditor.editTreeNode(selectedNode);
                     if (selectedNode.isChanged()) {
                         MethodManager.getManager().updateNode(metaCatalogueTree, (DefaultMetaTreeNode) selectedNode.getParent(), selectedNode);
 
                         selectedNode.setChanged(false);
                         ((DefaultTreeModel) metaCatalogueTree.getModel()).nodeChanged(selectedNode);
                     }
                 } else {
                     logger.warn("can not rename top node " + selectedNode);
                     // XXX dialog ...
                 }
             } else if (logger.isDebugEnabled()) {
                 logger.warn("insufficient permission to edit node " + selectedNode);
             }
         }
     }
 
     protected class EditObjectMethod extends PluginMenuItem implements PluginMethod {
 
         public EditObjectMethod() {
             super(MethodManager.OBJECT_NODE);
 
             this.pluginMethod = this;
 
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.edit.object.name"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.edit.object.icon")));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
             DefaultMetaTreeNode selectedNode = metaCatalogueTree.getSelectedNode();
 
             MetaObjectNode mon = (MetaObjectNode) selectedNode.getNode();
 //             SessionManager.getSession().getConnection().getMetaObject()
 
 //            if()
             if (MethodManager.getManager().checkPermission(mon, PermissionHolder.WRITEPERMISSION)) {
                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                 ComponentRegistry.getRegistry().getAttributeEditor().setTreeNode(metaCatalogueTree.getSelectionPath(), selectedNode);
             } else if (logger.isDebugEnabled()) {
                 logger.warn("insufficient permission to edit node " + selectedNode);
             }
         }
     }
 
     protected class DeleteNodeMethod extends PluginMenuItem implements PluginMethod {
 
         public DeleteNodeMethod() {
             super(MethodManager.PURE_NODE + MethodManager.CLASS_NODE + MethodManager.OBJECT_NODE);
 
             this.pluginMethod = this;
 
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.delete.name"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.delete.icon")));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
             DefaultMetaTreeNode selectedNode = metaCatalogueTree.getSelectedNode();
             if (selectedNode != null && selectedNode.isLeaf()) {
                 if (MethodManager.getManager().checkPermission(selectedNode.getNode(), PermissionHolder.WRITEPERMISSION)) {
                     boolean deleted = MethodManager.getManager().deleteNode(metaCatalogueTree, selectedNode);
                     if (deleted) {
                         try {
                             MetaTreeNodeVisualization.getInstance().removeVisualization(selectedNode);
 //                            CidsFeature cf = new CidsFeature((MetaObjectNode) selectedNode.getNode());
 //                            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(cf);
                         } catch (Exception e) {
                             logger.debug("Could not remove Node from map.", e);
                         }
                         ComponentRegistry.getRegistry().getDescriptionPane().clear();
                     }
                 } else if (logger.isDebugEnabled()) {
                     logger.warn("insufficient permission to delete node: " + selectedNode);
                 }
             } else {
                 logger.warn("can not delete node, node is no leaf: " + selectedNode);
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), "Dieser Knoten kann nicht gel\u00F6scht werden, da er noch Kindknoten enth\u00E4lt", "Dieser Knoten kann nicht gel\u00F6scht werden", JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     protected class DeleteObjectMethod extends PluginMenuItem implements PluginMethod {
         //TODO es wird noch deleteNode aufgerufen
 
         public DeleteObjectMethod() {
             super(MethodManager.OBJECT_NODE);
 
             this.pluginMethod = this;
 
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.deleteobject.name"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.deleteobject.icon")));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
             DefaultMetaTreeNode selectedNode = metaCatalogueTree.getSelectedNode();
             if (selectedNode != null && selectedNode.isLeaf()) {
                 if (MethodManager.getManager().checkPermission(selectedNode.getNode(), PermissionHolder.WRITEPERMISSION)) {
 
                     boolean deleted = MethodManager.getManager().deleteNode(metaCatalogueTree, selectedNode);
 
                     if (deleted) {
                         try {
 //                            CidsFeature cf = new CidsFeature((MetaObjectNode) selectedNode.getNode());
 //                            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(cf);
                             MetaTreeNodeVisualization.getInstance().removeVisualization(selectedNode);
                         } catch (Exception e) {
                             logger.debug("Could not remove Node from map.", e);
                         }
                         ComponentRegistry.getRegistry().getDescriptionPane().clear();
                     }
                 } else if (logger.isDebugEnabled()) {
                     logger.warn("insufficient permission to delete node: " + selectedNode);
                 }
             } else {
                 logger.warn("can not delete node, node is no leaf: " + selectedNode);
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), "Dieser Knoten kann nicht gel\u00F6scht werden, da er noch Kindknoten enth\u00E4lt", "Dieser Knoten kann nicht gel\u00F6scht werden", JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     protected class ExploreSubTreeMethod extends PluginMenuItem implements PluginMethod {
 
         public ExploreSubTreeMethod() {
             super(Long.MAX_VALUE);
 
             this.pluginMethod = this;
 
             this.setText(ResourceManager.getManager().getString("tree.editor.menu.reload.name"));
             this.setIcon(ResourceManager.getManager().getIcon(ResourceManager.getManager().getString("tree.editor.menu.reload.icon")));
             this.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
         }
 
         public String getId() {
             return this.getClass().getName();
         }
 
         public void invoke() throws Exception {
 
             final TreePath selectionPath = metaCatalogueTree.getSelectionPath();
            if (selectionPath != null && selectionPath.getPath().length > 0) {
                RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());
 
                ((DefaultTreeModel) metaCatalogueTree.getModel()).setRoot(rootTreeNode);
                ((DefaultTreeModel) metaCatalogueTree.getModel()).reload();
                metaCatalogueTree.exploreSubtree(selectionPath);
            }
         }
     }
     private MetaCatalogueTree metaCatalogueTree = null;
     private TreeNodeEditor treeNodeEditor;
     private ResourceManager resources;
 }
