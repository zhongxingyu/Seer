 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 /*******************************************************************************
  *
  * Copyright (c)        :       EIG (Environmental Informatics Group)
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
  * Programmers          :       Pascal
  *
  * Project                      :       WuNDA 2
  * Filename             :
  * Version                      :       1.0
  * Purpose                      :
  * Created                      :       08.05.2000
  * History                      :
  *
  *******************************************************************************/
 //import java.net.URL;
 import Sirius.navigator.connection.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.plugin.interfaces.PluginMethod;
 import Sirius.navigator.plugin.ui.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.search.dynamic.SearchSearchTopicsDialogAction;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.ui.dialog.*;
 import Sirius.navigator.ui.embedded.*;
 import Sirius.navigator.ui.tree.*;
 import Sirius.navigator.ui.tree.editor.*;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.User;
 import Sirius.server.newuser.permission.PermissionHolder;
 import Sirius.server.newuser.permission.Policy;
 
 import org.apache.log4j.Logger;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import de.cismet.cids.navigator.utils.MetaTreeNodeVisualization;
 
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class MutablePopupMenu extends JPopupMenu {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(MutablePopupMenu.class);
     private static final ResourceManager resources = ResourceManager.getManager();
 
     //~ Instance fields --------------------------------------------------------
 
     protected PopupMenuItemsActionListener itemActionListener;
     // statische Men\u00FCeintr\u00E4ge
     protected JMenuItem searchItem;
     protected JMenuItem passwordItem;
     protected JMenuItem specialTreeItem;
     protected JMenuItem newNode;
     protected JMenuItem editNode;
     protected JMenuItem editObject;
     protected JMenuItem deleteNode;
     protected JMenuItem deleteObject;
     // statische Men\u00FCeintr\u00E4ge
     protected JMenuItem newObject;
     private final PluginMenuesMap pluginMenues = new PluginMenuesMap();
     private TreeEditorMenu treeEditorMenu = null;
 
     private MetaCatalogueTree currentTree = null;
     private TreeNodeEditor treeNodeEditor;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MutablePopupMenu object.
      */
     public MutablePopupMenu() {
         this.createDefaultMenues();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Initialisierungsmethode<br>
      * Wird nur von den Konstruktoren aufgerufen.
      */
     protected void createDefaultMenues() {
         itemActionListener = new PopupMenuItemsActionListener();
 
         if (PropertyManager.getManager().isEnableSearchDialog()) {
             searchItem = new JMenuItem(org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.searchItem.text"));              // NOI18N
             searchItem.setActionCommand("search");                         // NOI18N
             searchItem.addActionListener(itemActionListener);
             this.add(searchItem);
         } else {
             this.add(new JMenuItem(new SearchSearchTopicsDialogAction())); // NOI18N
         }
 
         specialTreeItem = new JMenuItem(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.specialTreeItem.text")); // NOI18N
         specialTreeItem.setActionCommand("treecommand");       // NOI18N
         specialTreeItem.addActionListener(itemActionListener);
         this.add(specialTreeItem);
 
         // Hier evtl showInMap hinzufuegen
 
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
 
     /**
      * DOCUMENT ME!
      */
     private void hideEditMenues() {
         newNode.setVisible(false);
         editNode.setVisible(false);
         editObject.setVisible(false);
         deleteNode.setVisible(false);
         editObject.setVisible(false);
         deleteObject.setVisible(false);
         newObject.setVisible(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  menu  DOCUMENT ME!
      */
     public void addPluginMenu(final EmbeddedMenu menu) {
         if (menu.getItemCount() > 0) {
             this.pluginMenues.add(menu);
         } else {
             logger.warn("menu '" + menu.getId() + "' does not contain any items, ignoring menu"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id  DOCUMENT ME!
      */
     public void removePluginMenu(final String id) {
         this.pluginMenues.remove(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  enabled  DOCUMENT ME!
      */
     public void setPluginMenuEnabled(final String id, final boolean enabled) {
         this.pluginMenues.setEnabled(id, enabled);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginMenuEnabled(final String id) {
         return this.pluginMenues.isEnabled(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginMenuAvailable(final String id) {
         return this.pluginMenues.isAvailable(id);
     }
 
     @Override
     public void show(final Component invoker, final int x, final int y) {
         super.show(invoker, x, y);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Innere Klasse zum verarbeiten des PopupMenuEvent.<br>
      * Wenn das PopupMenu sichtbar wird, werden anhand der im Baum selektierten Objekte und deren Attribute die
      * dynamischen Menues erzeugt.
      *
      * @version  $Revision$, $Date$
      */
     class DynamicPopupMenuListener implements PopupMenuListener {
 
         //~ Methods ------------------------------------------------------------
 
         // int[] classNodeIDs = null;
         // Method[] methodIDs = null;
 
         @Override
         public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
 //            if (metaCatalogueTree == null) {
             // metaCatalogueTree = ComponentRegistry.getRegistry().getCatalogueTree();
             currentTree = ComponentRegistry.getRegistry().getActiveCatalogue();
 //            }
 
             // edit mbrill: now private static final variable
 // if (resources == null) {
 // resources = ResourceManager.getManager();
 // }
 
             if (treeNodeEditor == null) {
                 treeNodeEditor = new TreeNodeEditor(ComponentRegistry.getRegistry().getMainWindow(), true);
             }
 
             logger.info("showing popup menu"); // NOI18N
             // lazily construct tree editor menues treeEditorMenu wird immer neu angelegt, damit auf unterschiedliche
             // Anforderungen des selected Nodes reagiert werden kann
 
             if ((treeEditorMenu == null) && PropertyManager.getManager().isEditable()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("creating new tree editor menu"); // NOI18N
                 }
 //                treeEditorMenu = new TreeEditorMenu(ComponentRegistry.getRegistry().getMainWindow(), ComponentRegistry.getRegistry().getCatalogueTree());
 //                addPluginMenu(treeEditorMenu);
                 final Node node = currentTree.getSelectedNode().getNode();
 //                if ((node.getId() != -1) && (node.getDynamicChildrenStatement() == null)) {
 //                    // Kein dynamischer Knoten, keine dynamischen Kinder
 //                    newNode.setVisible(true);
 //                    editNode.setVisible(true);
 //                    deleteNode.setVisible(true);
 //                    editObject.setVisible(true);
 //                } else
 //
                 if ((node instanceof MetaObjectNode) && (node.getId() == -1)) {
                     // DynamicObjectNode
 
                     // EditObject
                     editObject.setVisible(true);
 
                     // DeleteObject
                     deleteObject.setVisible(true);
                     final User u = SessionManager.getSession().getUser();
 
                     final boolean permission = ((MetaObjectNode)node).getObject()
                                 .getMetaClass()
                                 .getPermissions()
                                 .hasWritePermission(u.getUserGroup())
                                 && ((MetaObjectNode)node).getObject()
                                 .getBean()
                                 .hasObjectWritePermission(u);
 
                     editObject.setEnabled(permission);
                     deleteObject.setEnabled(permission);
                 } else if ((node instanceof MetaNode) && (node.getClassId() != -1)) {
                     final int classID = node.getClassId();
                     final String domain = node.getDomain();
                     try {
                         ((NewObjectMethod)newObject).init(classID, domain);
                         newObject.setVisible(true);
                     } catch (Exception ex) {
                         logger.error("Error when adding the NewObjectMethodMenuItem", ex); // NOI18N
                     }
                 }
             }
 
             // Text mit Unicode....
             // viel SPass beim /u0000DCbersetzen ;o)
             if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof SearchResultsTree) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("showing default search tree menues");          // NOI18N
                 }
                 specialTreeItem.setText(org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.specialTreeItem.deleteEntries.text")); // NOI18N
                 if (treeEditorMenu != null) {
                     setPluginMenuEnabled(treeEditorMenu.getId(), false);
                 }
             } else if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof MetaCatalogueTree) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("showing default catalogue menues");            // NOI18N
                 }
                 specialTreeItem.setText(org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.specialTreeItem.adoptInTree.text"));   // NOI18N
                 if (treeEditorMenu != null) {
                     setPluginMenuEnabled(treeEditorMenu.getId(), true);
                 }
             }
 
             // enable/disable menues
             if (logger.isDebugEnabled()) {
                 logger.debug("setting menues availability"); // NOI18N
             }
             MutablePopupMenu.this.pluginMenues.setAvailability(MethodManager.getManager().getMethodAvailability());
         }
 
         @Override
         public void popupMenuCanceled(final PopupMenuEvent e) {
         }
 
         @Override
         public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
             // entfernen des TreeEditorMenues bewirkt dass immer die richtigen menues angezeigt werden if
             // (treeEditorMenu != null) { removePluginMenu(treeEditorMenu.getId()); treeEditorMenu = null; }
 
             hideEditMenues();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class PopupMenuItemsActionListener implements ActionListener {
 
         //~ Instance fields ----------------------------------------------------
 
         Sirius.server.localserver.attribute.Attribute[] attrArray;
         DefaultMetaTreeNode[] mtnArray;
         String[] koordinatenKatalog = null;
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (e.getActionCommand().equals("search")) { // NOI18N
                 try {
                     // TODO select class nodes in searchtree
                     // MethodManager.getManager().showSearchDialog(true);
                     MethodManager.getManager().showSearchDialog();
                 } catch (Throwable t) {
                     logger.error("Error while processing searchmethod", t); // NOI18N
 
                     final ErrorDialog errorDialog = new ErrorDialog(
                             org.openide.util.NbBundle.getMessage(
                                 MutablePopupMenu.class,
                                 "MutablePopupMenu.PopupMenuItemsActionListener.actionPerformed(ActionEvent).errorDialog.errorMessage"), // NOI18N
                             t.toString(),
                             ErrorDialog.WARNING);
                     errorDialog.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
                     errorDialog.show();
                 }
             } else if (e.getActionCommand().equals("Passwort aendern")) { // NOI18N
                 MethodManager.getManager().showPasswordDialog();
             } else if (e.getActionCommand().equals("treecommand")) { // NOI18N
                 MethodManager.getManager().callSpecialTreeCommand();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class PluginMenuesMap extends AbstractEmbeddedComponentsMap {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PluginMenuesMap object.
          */
         private PluginMenuesMap() {
             Logger.getLogger(PluginMenuesMap.class);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void doAdd(final EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutablePopupMenu.this.add((EmbeddedMenu)component);
             } else {
                 this.logger.error("doAdd(): invalid object type '" + component.getClass().getName()
                             + "', 'Sirius.navigator.EmbeddedMenu' expected"); // NOI18N
             }
         }
 
         @Override
         protected void doRemove(final EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutablePopupMenu.this.remove((EmbeddedMenu)component);
             } else {
                 this.logger.error("doRemove(): invalid object type '" + component.getClass().getName()
                             + "', 'Sirius.navigator.EmbeddedMenu' expected"); // NOI18N
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  methodAvailability  DOCUMENT ME!
          */
         private void setAvailability(final MethodAvailability methodAvailability) {
             final Iterator iterator = this.getEmbeddedComponents();
             while (iterator.hasNext()) {
                 ((PluginMenu)iterator.next()).setAvailability(methodAvailability);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class NewNodeMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new NewNodeMethod object.
          */
         public NewNodeMethod() {
             super(MethodManager.PURE_NODE);
 
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.NewNodeMethod.title"));    // NOI18N
             this.setIcon(resources.getIcon("neuer_knoten.gif")); // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final DefaultMetaTreeNode selectedNode = currentTree.getSelectedNode();
 
             if (logger.isDebugEnabled()) {
                 logger.debug("NewNodeMethod: creating new node as parent of " + selectedNode); // NOI18N
             }
             if ((selectedNode != null) && selectedNode.isPureNode()) {
                 final String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
                 // Sirius.server.newuser.permission.Permission perm = SessionManager.getSession().getWritePermission();
 
                 if (selectedNode.getNode().getPermissions().hasPermission(key, PermissionHolder.WRITEPERMISSION)) {
                     // knoten aufklappen
                     if (!selectedNode.isLeaf() && !selectedNode.isExplored()) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("NewNodeMethod: parent node is not explored");   // NOI18N
                         }
                         try {
                             currentTree.expandPath(new TreePath(selectedNode.getPath()));
                             // selectedNode.explore();
                             // ((DefaultTreeModel)metaCatalogueTree.getModel()).nodeStructureChanged(selectedNode);
                         } catch (Exception exp) {
                             logger.error("could not explore node: " + selectedNode, exp); // NOI18N
                         }
                     }
 
                     treeNodeEditor.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
                     final DefaultMetaTreeNode metaTreeNode = treeNodeEditor.createTreeNode();
 
                     if (metaTreeNode != null) {
                         metaTreeNode.setNew(true);
                         // damit beim Aufklappen nicht die explore methode aufgerufen wird
                         metaTreeNode.setExplored(true);
 
                         // das endg\u00FCltige Hinzuf\u00FCgen erledigt der Attribut Editor
                         if (metaTreeNode.isObjectNode()) {
                             if (!ComponentRegistry.getRegistry().getAttributeEditor().isChanged()) {
                                 MethodManager.getManager().addTreeNode(currentTree, selectedNode, metaTreeNode);
 
                                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                                 ComponentRegistry.getRegistry()
                                         .getAttributeEditor()
                                         .setTreeNode(currentTree.getSelectionPath(), metaTreeNode);
                             } else {
                                 logger.warn("could not create new object node: edited object still unsaved");  // NOI18N
                                 JOptionPane.showMessageDialog(
                                     ComponentRegistry.getRegistry().getMainWindow(),
                                     org.openide.util.NbBundle.getMessage(
                                         MutablePopupMenu.class,
                                         "MutablePopupMenu.invoke().JOptionPane.NewObjectInfoMessage.message"), // NOI18N
                                     org.openide.util.NbBundle.getMessage(
                                         MutablePopupMenu.class,
                                         "MutablePopupMenu.invoke().JOptionPane.NewObjectInfoMessage.title"),   // NOI18N
                                     JOptionPane.INFORMATION_MESSAGE);
                             }
                         } else {
                             if (MethodManager.getManager().addNode(currentTree, selectedNode, metaTreeNode)) {
                                 MethodManager.getManager().addTreeNode(currentTree, selectedNode, metaTreeNode);
                                 metaTreeNode.setNew(false);
                             } else if (logger.isDebugEnabled()) {
                                 logger.warn("addNode failed, omitting addTreeNode");                           // NOI18N
                             }
                         }
                     }
                 } else {
                     logger.warn("no permission to create node");                                               // NOI18N
                     JOptionPane.showMessageDialog(
                         ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MutablePopupMenu.class,
                             "MutablePopupMenu.NewNodeMethod.invoke().nopermissonDialog.message"),              // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MutablePopupMenu.class,
                             "MutablePopupMenu.NewNodeMethod.invoke().nopermissonDialog.title"),                // NOI18N
                         JOptionPane.WARNING_MESSAGE);
                 }
             } else {
                 logger.warn("parent node '" + selectedNode + "' is no pure node");                             // NOI18N
                 JOptionPane.showMessageDialog(
                     ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.NewNodeMethod.invoke().nopurenodeDialog.message",
                         new Object[] { selectedNode }),                                                        // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.NewNodeMethod.invoke().nopurenodeDialog.title"),                     // NOI18N
                     JOptionPane.WARNING_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class NewObjectMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Instance fields ----------------------------------------------------
 
         int classID = -1;
         String domain = null;
         MetaClass metaClass = null;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new NewObjectMethod object.
          */
         public NewObjectMethod() {
             super(MethodManager.PURE_NODE);
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.NewObjectMethod.text"));   // NOI18N
             this.setIcon(resources.getIcon("neuer_knoten.gif")); // NOI18N
             // funzt eh nicht
             // this.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   classID  DOCUMENT ME!
          * @param   domain   DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         public void init(final int classID, final String domain) throws Exception {
             this.classID = classID;
             this.domain = domain;
             metaClass = SessionManager.getProxy().getMetaClass(classID, domain);
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.NewObjectMethod.text",
                     new Object[] { metaClass.getName() })); // NOI18N
         }
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final DefaultMetaTreeNode selectedNode = currentTree.getSelectedNode();
             if (metaClass.getPermissions().hasWritePermission(SessionManager.getSession().getUser().getUserGroup())) {
                 final MetaObject metaObject = metaClass.getEmptyInstance();
                 metaObject.setStatus(metaObject.NEW);
                 final MetaObjectNode MetaObjectNode = new MetaObjectNode(
                         -1,
                         SessionManager.getSession().getUser().getDomain(),
                         metaObject,
                         null,
                         null,
                         true,
                         Policy.createWIKIPolicy(),
                         -1,
                         null,
                         false);
                 final DefaultMetaTreeNode metaTreeNode = new ObjectTreeNode(MetaObjectNode);
                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                 ComponentRegistry.getRegistry()
                         .getAttributeEditor()
                         .setTreeNode(currentTree.getSelectionPath(), metaTreeNode);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class EditNodeMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new EditNodeMethod object.
          */
         public EditNodeMethod() {
             super(MethodManager.PURE_NODE + MethodManager.OBJECT_NODE + MethodManager.CLASS_NODE);
 
             this.pluginMethod = this;
 
             // XXX i18n
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.EditNodeMethod.text"));    // NOI18N
             this.setIcon(resources.getIcon("attr_edit_on.gif")); // NOI18N
             this.setAccelerator(KeyStroke.getKeyStroke("F2"));   // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final DefaultMetaTreeNode selectedNode = currentTree.getSelectedNode();
             if (MethodManager.getManager().checkPermission(selectedNode.getNode(), PermissionHolder.WRITEPERMISSION)) {
                 if ((selectedNode.getParent() != null) && !(selectedNode.getParent() instanceof RootTreeNode)) {
                     treeNodeEditor.editTreeNode(selectedNode);
                     if (selectedNode.isChanged()) {
                         MethodManager.getManager()
                                 .updateNode(currentTree, (DefaultMetaTreeNode)selectedNode.getParent(), selectedNode);
 
                         selectedNode.setChanged(false);
                         ((DefaultTreeModel)currentTree.getModel()).nodeChanged(selectedNode);
                     }
                 } else {
                     logger.warn("can not rename top node " + selectedNode); // NOI18N
                     // XXX dialog ...
                 }
             } else {
                 logger.warn("insufficient permission to edit node " + selectedNode); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class EditObjectMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new EditObjectMethod object.
          */
         public EditObjectMethod() {
             super(MethodManager.OBJECT_NODE);
 
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.EditObjectMethod.text"));       // NOI18N
             this.setIcon(resources.getIcon("objekt_bearbeiten.gif")); // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final DefaultMetaTreeNode selectedNode = currentTree.getSelectedNode();
 
             final MetaObjectNode mon = (MetaObjectNode)selectedNode.getNode();
 //             SessionManager.getSession().getConnection().getMetaObject()
 
 //            if()
             if (MethodManager.getManager().checkPermission(mon, PermissionHolder.WRITEPERMISSION)) {
                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                 ComponentRegistry.getRegistry()
                         .getAttributeEditor()
                         .setTreeNode(currentTree.getSelectionPath(), selectedNode);
             } else {
                 logger.warn("insufficient permission to edit node " + selectedNode); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class DeleteNodeMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DeleteNodeMethod object.
          */
         public DeleteNodeMethod() {
             super(MethodManager.PURE_NODE + MethodManager.CLASS_NODE + MethodManager.OBJECT_NODE);
 
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.DeleteNodeMethod.text"));     // NOI18N
             this.setIcon(resources.getIcon("knoten_loeschen.gif")); // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final DefaultMetaTreeNode selectedNode = currentTree.getSelectedNode();
             if (selectedNode != null) {
                 if (MethodManager.getManager().checkPermission(
                                 selectedNode.getNode(),
                                 PermissionHolder.WRITEPERMISSION)) {
                     final boolean deleted = MethodManager.getManager().deleteNode(currentTree, selectedNode);
                     if (deleted) {
                         try {
                             MetaTreeNodeVisualization.getInstance().removeVisualization(selectedNode);
 //                            CidsFeature cf = new CidsFeature((MetaObjectNode) selectedNode.getNode());
 //                            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(cf);
                         } catch (Exception e) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("Could not remove Node from map.", e); // NOI18N
                             }
                         }
                         ComponentRegistry.getRegistry().getDescriptionPane().clear();
                     }
                 } else {
                     logger.warn("insufficient permission to delete node: " + selectedNode); // NOI18N
                 }
             } else {
                 logger.warn("can not delete node, node is no leaf: " + selectedNode); // NOI18N
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.DeleteNodeMethod.invoke().deleteNodeMessage.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.DeleteNodeMethod.invoke().deleteNodeMessage.title"), // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class DeleteObjectMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         // TODO es wird noch deleteNode aufgerufen
 
         /**
          * Creates a new DeleteObjectMethod object.
          */
         public DeleteObjectMethod() {
             super(MethodManager.OBJECT_NODE);
 
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.DeleteObjectMethod.text"));   // NOI18N
             this.setIcon(resources.getIcon("knoten_loeschen.gif")); // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final DefaultMetaTreeNode selectedNode = currentTree.getSelectedNode();
             if (selectedNode != null) {
                 if (MethodManager.getManager().checkPermission(
                                 selectedNode.getNode(),
                                 PermissionHolder.WRITEPERMISSION)) {
                     final boolean deleted = MethodManager.getManager().deleteNode(currentTree, selectedNode);
 
                     if (deleted) {
                         try {
 //                            CidsFeature cf = new CidsFeature((MetaObjectNode) selectedNode.getNode());
 //                            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(cf);
                             MetaTreeNodeVisualization.getInstance().removeVisualization(selectedNode);
                         } catch (Exception e) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("Could not remove Node from map.", e); // NOI18N
                             }
                         }
                         ComponentRegistry.getRegistry().getDescriptionPane().clear();
                     }
                 } else {
                     logger.warn("insufficient permission to delete node: " + selectedNode); // NOI18N
                 }
             } else {
                 logger.warn("can not delete node, node is no leaf: " + selectedNode); // NOI18N
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.DeleteObjectMethod.invoke().deleteObjectMessage.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.DeleteObjectMethod.invoke().deleteObjectMessage.title"), // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class ExploreSubTreeMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ExploreSubTreeMethod object.
          */
         public ExploreSubTreeMethod() {
             super(Long.MAX_VALUE);
 
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.ExploreSubTreeMethod.text"));    // NOI18N
             this.setIcon(resources.getIcon("teilbaum_neu_laden.gif")); // NOI18N
             this.setAccelerator(KeyStroke.getKeyStroke("F5"));         // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
 
         @Override
         public void invoke() throws Exception {
             final TreePath selectionPath = currentTree.getSelectionPath();
             if ((selectionPath != null) && (selectionPath.getPath().length > 0)) {
                 final RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());
 
                ((DefaultTreeModel)currentTree.getModel()).setRoot(rootTreeNode);
                ((DefaultTreeModel)currentTree.getModel()).reload();
                currentTree.exploreSubtree(selectionPath);
             }
         }
     }
 }
