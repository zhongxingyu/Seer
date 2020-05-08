 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 import Sirius.navigator.NavigatorConcurrency;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.method.MethodAvailability;
 import Sirius.navigator.method.MethodManager;
 import Sirius.navigator.plugin.interfaces.PluginMethod;
 import Sirius.navigator.plugin.ui.PluginMenu;
 import Sirius.navigator.plugin.ui.PluginMenuItem;
 import Sirius.navigator.resource.PropertyManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.search.dynamic.SearchSearchTopicsDialogAction;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.ui.dialog.ErrorDialog;
 import Sirius.navigator.ui.embedded.AbstractEmbeddedComponentsMap;
 import Sirius.navigator.ui.embedded.EmbeddedComponent;
 import Sirius.navigator.ui.embedded.EmbeddedMenu;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 import Sirius.navigator.ui.tree.SearchResultsTree;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.User;
 import Sirius.server.newuser.permission.PermissionHolder;
 import Sirius.server.newuser.permission.Policy;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.MenuElement;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import de.cismet.cids.navigator.utils.MetaTreeNodeVisualization;
 
 import de.cismet.cids.utils.interfaces.CidsBeanAction;
 
 import de.cismet.ext.CExtContext;
 import de.cismet.ext.CExtManager;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class MutablePopupMenu extends JPopupMenu {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(MutablePopupMenu.class);
     private static final ResourceManager resources = ResourceManager.getManager();
 
     //~ Instance fields --------------------------------------------------------
 
     private final PopupMenuItemsActionListener itemActionListener;
 
     private final JMenuItem specialTreeItem;
     private final JMenuItem newObject;
 
     private final PluginMenuesMap pluginMenues;
 
     // no sync needed, this shall be EDT-only class
     private MetaCatalogueTree currentTree;
 
     // we use this executor to limit the duration of the extension lookup (mscholl)
     private final ExecutorService extensionExecutor;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MutablePopupMenu object.
      */
     // TODO: default actions should be replaced by commons node actions
     public MutablePopupMenu() {
         extensionExecutor = Executors.newSingleThreadExecutor(NavigatorConcurrency.createThreadFactory(
                     "popup-menu-lookup"));
         pluginMenues = new PluginMenuesMap();
         itemActionListener = new PopupMenuItemsActionListener();
 
         if (PropertyManager.getManager().isEnableSearchDialog()) {
             final JMenuItem searchItem = new JMenuItem(org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.searchItem.text"));              // NOI18N
             searchItem.setActionCommand("search");                         // NOI18N
             searchItem.addActionListener(WeakListeners.create(ActionListener.class, itemActionListener, searchItem));
             this.add(searchItem);
         } else {
             this.add(new JMenuItem(new SearchSearchTopicsDialogAction())); // NOI18N
         }
 
         specialTreeItem = new JMenuItem(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.specialTreeItem.text")); // NOI18N
         specialTreeItem.setActionCommand("treecommand");       // NOI18N
         specialTreeItem.addActionListener(WeakListeners.create(
                 ActionListener.class,
                 itemActionListener,
                 specialTreeItem));
         this.add(specialTreeItem);
 
         this.add(new JSeparator(JSeparator.HORIZONTAL));
 
         this.addPopupMenuListener(new DynamicPopupMenuListener());
 
         newObject = new NewObjectMethod();
 
         this.add(new EditObjectMethod());
         this.add(new DeleteObjectMethod());
         this.add(newObject);
         this.add(new ExploreSubTreeMethod());
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  menu  DOCUMENT ME!
      */
     public void addPluginMenu(final EmbeddedMenu menu) {
         if (menu.getItemCount() > 0) {
             this.pluginMenues.add(menu);
         } else {
             LOG.warn("menu '" + menu.getId() + "' does not contain any items, ignoring menu"); // NOI18N
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
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Innere Klasse zum verarbeiten des PopupMenuEvent.<br>
      * Wenn das PopupMenu sichtbar wird, werden anhand der im Baum selektierten Objekte und deren Attribute die
      * dynamischen Menues erzeugt.
      *
      * @version  $Revision$, $Date$
      */
     private class DynamicPopupMenuListener implements PopupMenuListener {
 
         //~ Instance fields ----------------------------------------------------
 
         private final Set<Component> lookupItems = new HashSet<Component>();
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
             currentTree = ComponentRegistry.getRegistry().getActiveCatalogue();
 
             if (LOG.isInfoEnabled()) {
                 LOG.info("showing popup menu"); // NOI18N
             }
 
             boolean permission = false;
             if (PropertyManager.getManager().isEditable()) {
                 permission = true;
             }
 
             final DefaultMetaTreeNode[] selectedNodes = currentTree.getSelectedNodesArray();
             long availability = MethodManager.NONE;
             Boolean dynamicObjectNode = null;
             Boolean classNode = null;
 
             final Collection<MetaObject> ctxMetaObjects = new ArrayList<MetaObject>(selectedNodes.length);
             for (final DefaultMetaTreeNode tmp : selectedNodes) {
                 final Node node = tmp.getNode();
                 availability = availability | MethodManager.PURE_NODE;
 
                 // we have to load the metaobjects of the nodes for the dynamically created context menu (mscholl)
                 final MetaObject mo;
                 if (tmp instanceof ObjectTreeNode) {
                     mo = ((ObjectTreeNode)tmp).getMetaObject(true);
                     ctxMetaObjects.add(mo);
                 } else {
                     mo = null;
                 }
 
                 if ((node instanceof MetaObjectNode) && (node.getId() == -1)) {
                     // DynamicObjectNode
                     if (dynamicObjectNode == null) {
                         dynamicObjectNode = true;
                     }
                     classNode = false;
                     final User u = SessionManager.getSession().getUser();
 
                     // if we reach this piece of code then the code above already loaded the metaobject, because
                     // MetaObjectNode implies ObjectTreeNode, however, trust is good, control is better :) (mscholl)
                     assert mo != null : "the metaobject has not been loaded yet"; // NOI18N
 
                     permission = permission
                                 && mo.getMetaClass().getPermissions().hasWritePermission(u.getUserGroup())
                                 && mo.getBean().hasObjectWritePermission(u);
                 } else if ((node instanceof MetaNode) && (node.getClassId() != -1)) {
                     if (classNode == null) {
                         classNode = true;
                     }
                     dynamicObjectNode = false;
 
                     final User u = SessionManager.getSession().getUser();
                     permission = permission && ((MetaNode)node).getPermissions().hasWritePermission(u.getUserGroup());
 
                     try {
                         final int classID = node.getClassId();
                         final String domain = node.getDomain();
                         ((NewObjectMethod)newObject).init(classID, domain);
                     } catch (final Exception ex) {
                         LOG.error("Error when adding the NewObjectMethodMenuItem", ex); // NOI18N
                     }
                 }
             }
 
             if ((classNode != null) && dynamicObjectNode) {
                 availability += MethodManager.OBJECT_NODE;
             }
 
             if ((classNode != null) && classNode) {
                 availability += MethodManager.CLASS_NODE;
             }
 
             if (permission) {
                 availability += MethodManager.WRITE;
             }
 
             if ((selectedNodes != null) && (selectedNodes.length > 1)) {
                 availability += MethodManager.MULTIPLE;
             } else if ((selectedNodes != null) && (selectedNodes.length == 1)) {
                 availability += MethodManager.SINGLE;
             }
 
             if ((availability & (MethodManager.CLASS_NODE + MethodManager.MULTIPLE)) != 0) {
                 availability += MethodManager.CLASS_MULTIPLE;
             }
 
             final MenuElement[] mes = MutablePopupMenu.this.getSubElements();
 
             if (mes != null) {
                 for (final MenuElement me : mes) {
                     if (me instanceof PluginMenuItem) {
                         final long avail = ((PluginMenuItem)me).getAvailability();
 
                         ((PluginMenuItem)me).setVisible((avail & availability) == avail);
                     }
                 }
             }
 
             if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof SearchResultsTree) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("showing default search tree menues");             // NOI18N
                 }
                 specialTreeItem.setText(org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.specialTreeItem.deleteEntries.text")); // NOI18N
             } else if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof MetaCatalogueTree) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("showing default catalogue menues");               // NOI18N
                 }
                 specialTreeItem.setText(org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.specialTreeItem.adoptInTree.text"));   // NOI18N
             }
 
             // enable/disable menues
             if (LOG.isDebugEnabled()) {
                 LOG.debug("setting menues availability"); // NOI18N
             }
             // Die Plugin Menues werden nicht mehr verwendet
             MutablePopupMenu.this.pluginMenues.setAvailability(MethodManager.getManager().getMethodAvailability());
 
             // add CidsBeanActions dynamically (mscholl)
             if (!ctxMetaObjects.isEmpty()) {
                 final Collection<CidsBeanAction> additionalActions = new ArrayList<CidsBeanAction>(0);
                 final Runnable extensionRunner = new Runnable() {
 
                         @Override
                         public void run() {
                             final Collection<? extends CidsBeanAction> extensions = CExtManager.getInstance()
                                         .getExtensions(
                                             CidsBeanAction.class,
                                             new CExtContext(CExtContext.CTX_REFERENCE, ctxMetaObjects));
                             additionalActions.addAll(extensions);
                         }
                     };
 
                 final Future extensionFuture = extensionExecutor.submit(extensionRunner);
                 try {
                     extensionFuture.get(300, TimeUnit.MILLISECONDS);
 
                     if (!additionalActions.isEmpty()) {
                         final JSeparator separator = new JSeparator();
                         lookupItems.add(separator);
                         MutablePopupMenu.this.add(separator);
 
                         for (final CidsBeanAction action : additionalActions) {
                             final JMenuItem item = new JMenuItem(action);
                             lookupItems.add(item);
                             MutablePopupMenu.this.add(item);
                         }
                     }
                 } catch (final Exception ex) {
                     LOG.warn("cannot add actions from extensions", ex); // NOI18N
                 }
             }
         }
 
         @Override
         public void popupMenuCanceled(final PopupMenuEvent e) {
             removeLookupItems();
         }
 
         @Override
         public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
             removeLookupItems();
         }
 
         /**
          * DOCUMENT ME!
          */
         private void removeLookupItems() {
             for (final Component c : lookupItems) {
                 MutablePopupMenu.this.remove(c);
             }
 
             lookupItems.clear();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class PopupMenuItemsActionListener implements ActionListener {
 
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
                     MethodManager.getManager().showSearchDialog();
                 } catch (final Exception ex) {
                     LOG.error("Error while processing searchmethod", ex); // NOI18N
 
                     final ErrorDialog errorDialog = new ErrorDialog(
                             org.openide.util.NbBundle.getMessage(
                                 MutablePopupMenu.class,
                                 "MutablePopupMenu.PopupMenuItemsActionListener.actionPerformed(ActionEvent).errorDialog.errorMessage"), // NOI18N
                             ex.toString(),
                             ErrorDialog.WARNING);
                     StaticSwingTools.showDialog(errorDialog);
                 }
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
     private class NewObjectMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Instance fields ----------------------------------------------------
 
         int classID = -1;
         String domain = null;
         MetaClass metaClass = null;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new NewObjectMethod object.
          */
         public NewObjectMethod() {
             super(MethodManager.CLASS_NODE + MethodManager.SINGLE + MethodManager.WRITE);
             this.pluginMethod = this;
 
             this.setText(org.openide.util.NbBundle.getMessage(
                     MutablePopupMenu.class,
                     "MutablePopupMenu.NewObjectMethod.text"));   // NOI18N
             this.setIcon(resources.getIcon("neuer_knoten.gif")); // NOI18N
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
                 metaObject.setStatus(MetaObject.NEW);
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
     private class EditObjectMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new EditObjectMethod object.
          */
         public EditObjectMethod() {
             super(MethodManager.OBJECT_NODE + MethodManager.SINGLE);
 
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
 
             if (MethodManager.getManager().checkPermission(mon, PermissionHolder.WRITEPERMISSION)) {
                 ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
                 ComponentRegistry.getRegistry()
                         .getAttributeEditor()
                         .setTreeNode(currentTree.getSelectionPath(), selectedNode);
             } else {
                 LOG.warn("insufficient permission to edit node " + selectedNode); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class DeleteObjectMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         // TODO es wird noch deleteNode aufgerufen
 
         /**
          * Creates a new DeleteObjectMethod object.
          */
         public DeleteObjectMethod() {
             super(MethodManager.OBJECT_NODE + MethodManager.WRITE);
 
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
             final DefaultMetaTreeNode[] selectedNodes = currentTree.getSelectedNodesArray();
 
             if ((selectedNodes != null) && (selectedNodes.length > 0)) {
                 for (final DefaultMetaTreeNode tmp : selectedNodes) {
                     if (!MethodManager.getManager().checkPermission(
                                     tmp.getNode(),
                                     PermissionHolder.WRITEPERMISSION)) {
                         LOG.warn("insufficient permission to delete node: " + tmp); // NOI18N
 
                         return;
                     }
                 }
 
                 if (selectedNodes.length > 1) {
                     final int option = JOptionPane.showOptionDialog(
                             ComponentRegistry.getRegistry().getMainWindow(),
                             org.openide.util.NbBundle.getMessage(
                                 MutablePopupMenu.class,
                                 "MutablePopupMenu.DeleteObjectMethod.invoke().JOptionPane.message",
                                 new Object[] { String.valueOf(selectedNodes.length) }),                        // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MutablePopupMenu.class,
                                 "MutablePopupMenu.DeleteObjectMethod.invoke().JOptionPane.title"),             // NOI18N
                             JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE,
                             null,
                             new String[] {
                                 org.openide.util.NbBundle.getMessage(
                                     MutablePopupMenu.class,
                                     "MutablePopupMenu.DeleteObjectMethod.invoke().JOptionPane.option.commit"), // NOI18N
                                 org.openide.util.NbBundle.getMessage(
                                     MutablePopupMenu.class,
                                     "MutablePopupMenu.DeleteObjectMethod.invoke().JOptionPane.option.cancel")
                             },                                                                                 // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MutablePopupMenu.class,
                                 "MutablePopupMenu.DeleteObjectMethod.invoke().JOptionPane.option.cancel"));
                     if (option != JOptionPane.YES_OPTION) {
                         return;
                     }
                 }
 
                 boolean deleted = false;
                 for (final DefaultMetaTreeNode tmp : selectedNodes) {
                     final boolean deletedSingleNode = MethodManager.getManager()
                                 .deleteNode(currentTree, tmp, (selectedNodes.length == 1));
                     deleted = deleted | deletedSingleNode;
 
                     if (deletedSingleNode) {
                         try {
                             MetaTreeNodeVisualization.getInstance().removeVisualization(tmp);
                         } catch (final Exception e) {
                             LOG.warn("Could not remove Node from map.", e); // NOI18N
                         }
                     }
                 }
 
                 if (deleted) {
                     ComponentRegistry.getRegistry().getDescriptionPane().clear();
                 }
             } else {
                 LOG.warn("cannot delete node, because there is no node selected.");                  // NOI18N
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.DeleteObjectMethod.invoke().deleteObjectMessage.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MutablePopupMenu.class,
                         "MutablePopupMenu.DeleteObjectMethod.invoke().deleteObjectMessage.title"),   // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ExploreSubTreeMethod extends PluginMenuItem implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ExploreSubTreeMethod object.
          */
         public ExploreSubTreeMethod() {
             super(MethodManager.NONE);
 
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
 
                 final Runnable r = new Runnable() {
 
                         @Override
                         public void run() {
                             ((DefaultTreeModel)currentTree.getModel()).setRoot(rootTreeNode);
                             ((DefaultTreeModel)currentTree.getModel()).reload();
                             currentTree.exploreSubtree(selectionPath);
                         }
                     };
 
                 if (EventQueue.isDispatchThread()) {
                     r.run();
                 } else {
                     EventQueue.invokeLater(r);
                 }
             }
         }
     }
 }
