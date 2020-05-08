 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.catalog;
 
 import org.apache.log4j.Logger;
 
 import org.hibernate.ObjectNotFoundException;
 
 import org.openide.actions.CopyAction;
 import org.openide.actions.CutAction;
 import org.openide.actions.DeleteAction;
 import org.openide.actions.PasteAction;
 import org.openide.nodes.AbstractNode;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.nodes.Node.Property;
 import org.openide.nodes.NodeTransfer;
 import org.openide.nodes.PropertySupport;
 import org.openide.nodes.Sheet;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 import org.openide.util.actions.CallableSystemAction;
 import org.openide.util.datatransfer.PasteType;
 import org.openide.windows.WindowManager;
 
 import java.awt.Image;
 import java.awt.datatransfer.Transferable;
 
 import java.beans.PropertyEditor;
 
 import java.io.IOException;
 
 import java.lang.reflect.InvocationTargetException;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.NoResultException;
 
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.plaf.metal.MetalIconFactory;
 
 import de.cismet.cids.abf.domainserver.RefreshAction;
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.PolicyPropertyEditor;
 import de.cismet.cids.abf.domainserver.project.ProjectChildren;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
 import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
 import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
 import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
 import de.cismet.cids.abf.utilities.Comparators;
 import de.cismet.cids.abf.utilities.Refreshable;
 import de.cismet.cids.abf.utilities.windows.ErrorUtils;
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.catalog.CatNode;
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
 import de.cismet.cids.jpa.entity.common.URL;
 import de.cismet.cids.jpa.entity.common.URLBase;
 import de.cismet.cids.jpa.entity.permission.NodePermission;
 import de.cismet.cids.jpa.entity.permission.Permission;
 import de.cismet.cids.jpa.entity.permission.Policy;
 
 import de.cismet.diff.db.DatabaseConnection;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  1.29
  */
 public class CatalogNode extends ProjectNode implements Refreshable, CatalogNodeContextCookie {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(CatalogNode.class);
 
     private static final Image IMAGE_OPEN;
     private static final Image IMAGE_CLOSED;
     private static final Image BADGE_ORG;
     private static final Image BADGE_CLASS;
     private static final Image BADGE_DYN;
     private static final Image BADGE_OBJ;
     private static final String NULL;
 
     static {
         Icon openIcon = UIManager.getIcon("Tree.openIcon"); // NOI18N
         // native GTK look and feel fix
         if (openIcon == null) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("GTK+ fix: using tree folder icon from metaliconfactory for opened icon"); // NOI18N
             }
             openIcon = MetalIconFactory.getTreeFolderIcon();
         }
 
         Icon closedIcon = UIManager.getIcon("Tree.closedIcon"); // NOI18N
         // native GTK look and feel fix
         if (closedIcon == null) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("GTK+ fix: using tree folder icon from metaliconfactory for closed icon");  // NOI18N
             }
             closedIcon = MetalIconFactory.getTreeFolderIcon();
         }
         IMAGE_OPEN = ImageUtilities.icon2Image(openIcon);
         IMAGE_CLOSED = ImageUtilities.icon2Image(closedIcon);
         BADGE_ORG = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_org.png");     // NOI18N
         BADGE_OBJ = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_object.png");  // NOI18N
         BADGE_DYN = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_dynamic.png"); // NOI18N
         BADGE_CLASS = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_class.png"); // NOI18N
         NULL = "null";                                                                                // NOI18N
     }
 
     //~ Instance fields --------------------------------------------------------
 
     transient CatNode catNode;
     transient Refreshable parent;
 
     private final transient PermissionResolver permResolve;
     private final RequestProcessor refreshProcessor;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CatalogNode object.
      *
      * @param  catNode  DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      * @param  parent   DOCUMENT ME!
      */
     public CatalogNode(final CatNode catNode, final DomainserverProject project, final Refreshable parent) {
         super(Children.LEAF, project);
         this.catNode = catNode;
         this.parent = parent;
         permResolve = PermissionResolver.getInstance(project);
         refreshProcessor = new RequestProcessor("refreshprocessor", 5); // NOI18N
         setDisplayName(catNode.getName());
         refresh();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getHtmlDisplayName() {
         return getDisplayName();
     }
 
     @Override
     public Action[] getActions(final boolean b) {
         return new Action[] {
                 CallableSystemAction.get(NewCatalogNodeWizardAction.class),
                 null,
                 CallableSystemAction.get(ModifyNodeRightsWizardAction.class),
                 null,
                 CallableSystemAction.get(CopyAction.class),
                 CallableSystemAction.get(CutAction.class),
                 CallableSystemAction.get(PasteAction.class),
                 CallableSystemAction.get(CreateLinkAction.class),
                 CallableSystemAction.get(InsertLinkAction.class),
                 null,
                 CallableSystemAction.get(DeleteAction.class),
                 null,
                 CallableSystemAction.get(RefreshAction.class)
             };
     }
 
     @Override
     public boolean canDestroy() {
         return catNode.getId() > 0;
     }
 
     @Override
     public boolean canCopy() {
         return true;
     }
 
     @Override
     public boolean canCut() {
         // cannot cut dynamically created nodes
         if (catNode.getId() == -1) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public void destroy() {
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("CatalogNode: destroy requested");                                          // NOI18N
             }
             if (parent instanceof CatalogNode) {
                 final CatalogNode parentNode = (CatalogNode)parent;
                 try {
                     if (project.getCidsDataObjectBackend().deleteNode(parentNode.catNode, catNode)) {
                         project.getLookup().lookup(CatalogManagement.class).destroyedNode(catNode, this);
                     } else {
                         project.getLookup().lookup(CatalogManagement.class).removedNode(catNode, this);
                     }
                 } catch (final NoResultException nre) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("possibly already deleted", nre);                                   // NOI18N
                     }
                     ErrorUtils.showErrorMessage(
                         NbBundle.getMessage(
                             CatalogNode.class,
                             "CatalogNode.destroy.ErrorUtils.nodeDeletionProbablyAlreadyDel.message"), // NOI18N
                         NbBundle.getMessage(
                             CatalogNode.class,
                             "CatalogNode.destroy.ErrorUtils.nodeDeletionProbablyAlreadyDel.title"),   // NOI18N
                         nre);
                 }
             } else {
                 project.getCidsDataObjectBackend().deleteRootNode(catNode);
                 project.getLookup().lookup(CatalogManagement.class).removedNode(catNode, this);
             }
             parent.refresh();
         } catch (final Exception e) {
             LOG.error("could not destroy node", e);                                                   // NOI18N
             ErrorUtils.showErrorMessage(NbBundle.getMessage(
                     CatalogNode.class,
                     "CatalogNode.destroy.ErrorUtils.duringNodeDeletion.message"),                     // NOI18N
                 e);
         }
     }
 
     @Override
     public final void refresh() {
         refreshProcessor.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     if (project.isConnected()) {
                         final CatNode parent = getParent();
                         if ((parent == null) || (parent.getDynamicChildren() == null)) {
                             catNode.setIsLeaf(project.getCidsDataObjectBackend().isLeaf(catNode, true));
                         } else {
                             catNode.setIsLeaf(catNode.getDynamicChildren() == null);
                         }
 
                         final Children c = getChildren();
                         if (catNode.isLeaf()
                                     && ((catNode.getDynamicChildren() == null)
                                         || NULL.equalsIgnoreCase(catNode.getDynamicChildren()))) {
                             setChildrenEDT(Children.LEAF);
                         } else if (c instanceof CatalogNodeChildren) {
                             if ((catNode.getDynamicChildren() == null)
                                         || NULL.equalsIgnoreCase(catNode.getDynamicChildren())) {
                                 ((ProjectChildren)c).refreshByNotify();
                             } else {
                                 setChildrenEDT(new DynamicCatalogNodeChildren(catNode, project));
                             }
                         } else if (c instanceof DynamicCatalogNodeChildren) {
                             if ((catNode.getDynamicChildren() == null)
                                         || NULL.equalsIgnoreCase(catNode.getDynamicChildren())) {
                                 setChildrenEDT(new CatalogNodeChildren(catNode, project));
                             } else {
                                 ((ProjectChildren)c).refreshByNotify();
                             }
                         } else if (c == Children.LEAF) {
                             if (catNode.getDynamicChildren() == null) {
                                 setChildrenEDT(new CatalogNodeChildren(catNode, project));
                             } else {
                                 setChildrenEDT(new DynamicCatalogNodeChildren(catNode, project));
                             }
                         }
                         // TODO: fire property change to display possibly changed rights
                         // firePropertySetsChange(null, getPropertySets());
                         // TODO: as long as i did not find out about the mechanism to fire
                         // an appropriate property change and/or how to register an
                         // appropriate listener at the right place if this is necessary,
                         // this workaround is acceptable
                         setSheet(createSheet());
                     } else {
                         setChildrenEDT(Children.LEAF);
                     }
                 }
             });
     }
 
     @Override
     public Sheet createSheet() {
         final Sheet sheet = Sheet.createDefault();
         final boolean mayWrite;
         if (parent instanceof CatalogNode) {
             final CatalogNode cn = (CatalogNode)parent;
             mayWrite = (cn.catNode.getDynamicChildren() == null);
         } else {
             mayWrite = true;
         }
         try {
             //J-
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeID ">
             final Property idProp = new PropertySupport.Reflection(catNode,
                     Integer.class, "getId", null);             // NOI18N
             idProp.setName(NbBundle.getMessage(
                     CatalogNode.class,
                     "CatalogNode.createSheet().idProp.name")); // NOI18N
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeName ">
             final Property nameProp = new PropertySupport(
                     "nodeName",                                       // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().nameProp.name"),   // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().nameProp.nameOfNode"), // NOI18N
                     true,
                     mayWrite) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return catNode.getName();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String name = object.toString();
                         final String oldName = catNode.getName();
                         try {
                             catNode.setName(name);
                             project.getCidsDataObjectBackend().store(catNode);
                             CatalogNode.this.setDisplayName(name);
                         } catch (final Exception ex) {
                             LOG.error("name could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().nameProp.setValue(Object).ErrorUtils.duringNameChange.message"), // NOI18N
                                 ex);
                             catNode.setName(oldName);
                         }
                     }
                 };                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeDescription ">
             final Property urlProp = new PropertySupport(
                     "nodeUrl",                                                 // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().urlProp.name"),             // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().urlProp.urlDescribingTheNode"), // NOI18N
                     true,
                     mayWrite) {
 
                     private NodeURLPropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         final URL url = catNode.getUrl();
                         return (url == null) ? URL.NO_DESCRIPTION : url;
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         if (!(object instanceof URL)) {
                             throw new IllegalArgumentException(
                                 "object must be of type URL");         // NOI18N
                         }
                         final URL url = (URL)object;
                         final URL oldURL = catNode.getUrl();
                         try {
                             catNode.setUrl((url.equals(URL.NO_DESCRIPTION)) ? null : url);
                             project.getCidsDataObjectBackend().store(catNode);
                         } catch (final Exception ex) {
                             LOG.error("url could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().urlProp.setValue(Object).ErrorUtils.duringURLchange.message"),// NOI18N
                                 ex);
                             catNode.setUrl(oldURL);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         if (editor == null) {
                             editor = new NodeURLPropertyEditor(project);
                         }
                         return editor;
                     }
                 }; // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeType ">
             final Property nodeTypeProp = new PropertySupport(
                     "nodeType",                                           // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().nodeTypeProp.nodeType"), // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().nodeTypeProp.typeOfNode"), // NOI18N
                     true,
                     mayWrite) {
 
                     private NodeTypePropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         final String type = catNode.getNodeType();
                         if (type.equals(CatNode.Type.CLASS.getType())) {
                             return NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().nodeTypeProp.nodeType.classNode");   // NOI18N
                         } else if (type.equals(CatNode.Type.OBJECT.getType())) {
                             return NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().nodeTypeProp.nodeType.objectNode");  // NOI18N
                         } else if (type.equals(CatNode.Type.ORG.getType())) {
                             return NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().nodeTypeProp.nodeType.orgNode");     // NOI18N
                         } else {
                             return NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().nodeTypeProp.nodeType.unknownType"); // NOI18N
                         }
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String oldType = catNode.getNodeType();
                         final String type = object.toString();
                         try {
                             catNode.setNodeType(type);
                             project.getCidsDataObjectBackend().store(catNode);
                             fireIconChange();
                         } catch (final Exception ex) {
                             LOG.error("type could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().nodeTypeProp.setValue(Object).ErrorUtils.duringTypeChange.message"),// NOI18N
                                 ex);
                             catNode.setNodeType(oldType);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         if (editor == null) {
                             editor = new NodeTypePropertyEditor();
                         }
                         return editor;
                     }
                 }; // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeIsRoot ">
             final Property rootProp = new PropertySupport(
                     "nodeIsRoot",                                         // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().rootProp.rootNode"),   // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().rootProp.isNodeRootNode"), // NOI18N
                     true,
                     mayWrite) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         if (catNode.getIsRoot() == null) {
                             return Boolean.FALSE;
                         }
                         return catNode.getIsRoot();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Boolean isRoot = (Boolean)object;
                         final Boolean oldIsRoot = catNode.getIsRoot();
                         try {
                             catNode.setIsRoot(isRoot);
                             project.getCidsDataObjectBackend().store(catNode);
                         } catch (final Exception ex) {
                             LOG.error("isRoot could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().rootProp.setValue(Object).ErrorUtils.duringRootFlagChange.message"),// NOI18N
                                 ex);
                             catNode.setIsRoot(oldIsRoot);
                         }
                     }
                 };                                                        // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodePolicy ">
             final Property policyProp = new PropertySupport(
                     "nodePolicy",                                          // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().policyProp.policy"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().policyProp.policyTooltip"), // NOI18N
                     true,
                     mayWrite) {
 
                     private PolicyPropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         Policy p = catNode.getPolicy();
                         if (p == null) {
                             final PermissionResolver.Result r = permResolve.getPermString(catNode, null);
                             if (r.getInheritanceString() == null) {
                                 // should never occur
                                 p = Policy.NO_POLICY;
                             } else {
                                 p = new Policy();
                                 p.setName("<" // NOI18N
                                             + r.getInheritanceString() + ">"); // NOI18N
                             }
                         }
                         return p;
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         if (!(object instanceof Policy)) {
                             throw new IllegalArgumentException(
                                 "object must be of type Policy");         // NOI18N
                         }
                         final Policy policy = (Policy)object;
                         final Policy oldPolicy = catNode.getPolicy();
                         try {
                             catNode.setPolicy((policy.getId() == null) ? null : policy);
                             project.getCidsDataObjectBackend().store(catNode);
                             refresh();
                         } catch (final Exception ex) {
                             LOG.error("policy could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().policyProp.setValue(Object).ErrorUtils.duringRightsChange.message"),// NOI18N
                                 ex);
                             catNode.setPolicy(oldPolicy);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         if (editor == null) {
                             editor = new PolicyPropertyEditor(project);
                         }
                         return editor;
                     }
                 }; // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeDerivePerm ">
             final Property derivePermProp = new PropertySupport(
                     "nodeDerivePerm",                                                         // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().derivePermProp.deriveRightsFromClass"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().derivePermProp.deriveRightsFromClassTooltip"), // NOI18N
                     true,
                     mayWrite) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         if (catNode.getDerivePermFromClass() == null) {
                             return Boolean.FALSE;
                         }
                         return catNode.getDerivePermFromClass();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Boolean derive = (Boolean)object;
                         final Boolean oldDerive = catNode.getDerivePermFromClass();
                         if ((derive != null) && (catNode.getCidsClass() == null)) {
                             final int answer = JOptionPane.showConfirmDialog(
                                     WindowManager.getDefault().getMainWindow(),
                                     NbBundle.getMessage(
                                         CatalogNode.class,
                                         "CatalogNode.createSheet().derivePermProp.setValue(Object).JOptionPane.message"), // NOI18N
                                     NbBundle.getMessage(
                                         CatalogNode.class,
                                         "CatalogNode.createSheet().derivePermProp.setValue(Object).JOptionPane.title"), // NOI18N
                                     JOptionPane.OK_CANCEL_OPTION,
                                     JOptionPane.WARNING_MESSAGE);
                             if (answer == JOptionPane.CANCEL_OPTION) {
                                 return;
                             }
                         }
                         try {
                             catNode.setDerivePermFromClass(derive);
                             project.getCidsDataObjectBackend().store(catNode);
                         } catch (final Exception ex) {
                             LOG.error("derivePerm could not be changed",                                                // NOI18N
                                 ex);
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().derivePermProp.setValue(Object).ErrorUtils.deriveFromClassFlagChange.message"), // NOI18N
                                 ex);
                             catNode.setDerivePermFromClass(oldDerive);
                         }
                     }
                 };                                                                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeIcon ">
             final Property iconProp = new PropertySupport(
                     "nodeIcon",                                        // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().iconProp.icon"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().iconProp.iconTooltip"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         final String icon = catNode.getIcon();
                         return (icon == null)
                             ? NbBundle.getMessage(
                                 CatalogNode.class,
                                 "CatalogNode.createSheet().iconProp.getValue().returnvalue.notIconBrackets") : icon;     // NOI18N
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String icon = object.toString();
                         final String oldIcon = catNode.getIcon();
                         try {
                             if (NULL.equals(icon) || "".equals(icon)    // NOI18N
                                         || NbBundle.getMessage(
                                             CatalogNode.class,
                                             "CatalogNode.createSheet().iconProp.getValue().returnvalue.notIconBrackets")// NOI18N
                                         .equals(icon))
                             {
                                 catNode.setIcon(null);
                             } else {
                                 catNode.setIcon(icon);
                             }
                             project.getCidsDataObjectBackend().store(catNode);
                             fireIconChange();
                             refresh();
                         } catch (final Exception ex) {
                             LOG.error("icon could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().iconProp.setValue(Object).ErrorUtils.duringIconChange.message"),// NOI18N
                                 ex);
                             catNode.setIcon(oldIcon);
                         }
                     }
                 };                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeIconFactory ">
             final Property factoryProp = new PropertySupport(
                     "nodeIconFactory",                                           // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().factoryProp.iconFactory"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().factoryProp.iconFactoryTooltip"), // NOI18N
                     true,
                     mayWrite) {
 
                     private JavaClassPropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return catNode.getIconFactory();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final JavaClass old = catNode.getIconFactory();
                         if (object == null) {
                             final int answer = JOptionPane.showConfirmDialog(
                                     WindowManager.getDefault().getMainWindow(),
                                     NbBundle.getMessage(
                                         CatalogNode.class,
                                         "CatalogNode.createSheet().factoryProp.setValue(Object).JOptionPane.message"), // NOI18N
                                     NbBundle.getMessage(
                                         CatalogNode.class,
                                         "CatalogNode.createSheet().factoryProp.setValue(Object).JOptionPane.title"), // NOI18N
                                     JOptionPane.YES_NO_OPTION,
                                     JOptionPane.QUESTION_MESSAGE);
                             if (answer == JOptionPane.NO_OPTION) {
                                 return;
                             }
                         }
                         try {
                             catNode.setIconFactory((JavaClass)object);
                             project.getCidsDataObjectBackend().store(catNode);
                             fireIconChange();
                         } catch (final Exception e) {
                             LOG.error("iconfactory could not be changed",                                            // NOI18N
                                 e);
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().factoryProp.setValue(Object).ErrorUtils.duringIconFactoryChange.message"),// NOI18N
                                 e);
                             catNode.setIconFactory(old);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         if (editor == null) {
                             editor = new JavaClassPropertyEditor(project, JavaClass.Type.UNKNOWN);
                         }
                         return editor;
                     }
                 }; // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeObjectSet ">
             final Map<String, String> objectInfo;
             if(CatNode.Type.OBJECT.getType().equals(catNode.getNodeType())){
                 objectInfo = project.getCidsDataObjectBackend().getSimpleObjectInformation(catNode);
             } else {
                 objectInfo = null;
             }
             final HashSet<Property> objectProps = new HashSet<Property>();
             if (objectInfo != null) {
                 for (final Iterator<Entry<String, String>> entries = objectInfo.entrySet().iterator();
                             entries.hasNext();) {
                     final Entry<String, String> entry = entries.next();
                     objectProps.add(new PropertySupport.ReadOnly(
                             "node"// NOI18N
                                     + entry.getKey(),
                             String.class,
                             entry.getKey(),
                             entry.getKey()) {
 
                             @Override
                             public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                 final String value = entry.getValue();
                                 if ((value == null) || NULL.equalsIgnoreCase(value)) {
                                     return NbBundle.getMessage(
                                             CatalogNode.class,
                                             "CatalogNode.createSheet().factoryProp.getValue().returnvalue.valueNotSetBrackets"); // NOI18N
                                 }
                                 return entry.getValue();
                             }
                         });
                 }
             }                                                                                                                    // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeClass ">
             final Property classProp = new PropertySupport(
                     "nodeClass",                                               // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().classProp.linkedClass"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().classProp.linkedClassTooltip"), // NOI18N
                     true,
                     mayWrite) {
 
                     private NodeClassPropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         final CidsClass cc = catNode.getCidsClass();
                         return (cc == null) ? CidsClass.NO_CLASS : cc;
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         if (!(object instanceof CidsClass)) {
                             throw new IllegalArgumentException(
                                 "object must be instanceof CidsClass");  // NOI18N
                         }
                         final CidsClass clazz = (CidsClass)object;
                         final CidsClass oldClass = catNode.getCidsClass();
                         try {
                             catNode.setCidsClass((clazz.equals(CidsClass.NO_CLASS)) ? null : clazz);
                             project.getCidsDataObjectBackend().store(catNode);
                         } catch (final Exception ex) {
                             LOG.error("class could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().classProp.setValue(Object).ErrorUtils.duringClassChange.message"), // NOI18N
                                 ex);
                             catNode.setCidsClass(oldClass);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         if (editor == null) {
                             editor = new NodeClassPropertyEditor(project);
                         }
                         return editor;
                     }
                 }; // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: NodePermissions ">
             final LinkedList<Property> rightProps = new LinkedList<Property>();
             if (catNode.getNodePermissions() != null) {
                 final List<NodePermission> perms = new ArrayList<NodePermission>(catNode.getNodePermissions());
                 for (final NodePermission perm : perms) {
                     rightProps.add(new PropertySupport(
                             "nodeRight"// NOI18N
                                     + perm.toString(),
                             String.class,
                             perm.getUserGroup().getName(),
                             NbBundle.getMessage(
                                 CatalogNode.class,
                                 "CatalogNode.createSheet().rightProp.aUsergroup"), // NOI18N
                             true,
                             false) {
 
                             @Override
                             public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                 final Permission p = perm.getPermission();
                                 String s = permResolve.getPermString(catNode, p).getPermissionString();
                                 if (s == null) {
                                     s = p.getKey();
                                 }
                                 return s;
                             }
 
                             @Override
                             public void setValue(final Object object) throws IllegalAccessException,
                                 IllegalArgumentException,
                                 InvocationTargetException {
                                 // not needed
                             }
                         });
                 }
             } // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: DynamicChildrenSQL ">
             final Property dynaChildrenSQL = new PropertySupport(
                     "nodeDynaChildrenSQL",                                        // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().dynaChildrenSQL.sqlQuery"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().dynaChildrenSQL.sqlQueryTooltip"), // NOI18N
                     true,
                     mayWrite) {
 
                     private PropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return catNode.getDynamicChildren();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String sql = object.toString();
                         final String oldSql = catNode.getDynamicChildren();
                         try {
                             if (NULL.equals(sql) || "".equals(sql))      // NOI18N
                             {
                                 catNode.setDynamicChildren(null);
                             } else {
                                 catNode.setDynamicChildren(sql);
                             }
                             project.getCidsDataObjectBackend().store(catNode);
                             fireIconChange();
                             refresh();
                         } catch (final Exception ex) {
                             LOG.error("dynamic children statement could" // NOI18N
                                         + " not be changed", ex);        // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().dynaChildrenSQL.setValue(Object).ErrorUtils.duringDynChildrenChange.message"),// NOI18N
                                 ex);
                             catNode.setDynamicChildren(oldSql);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         if (editor == null) {
                             editor = new DynamicChildrenPropertyEditor();
                         }
                         return editor;
                     }
                 }; // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: DynamicChildrenSQLSort ">
             final Property dynaChildrenSQLSort = new PropertySupport(
                     "nodeDynaChildrenSQLSort",                                       // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().dynaChildrenSQLSort.sqlSort"),    // NOI18N
                     NbBundle.getMessage(
                         CatalogNode.class,
                         "CatalogNode.createSheet().dynaChildrenSQLSort.sqlSortTooltip"), // NOI18N
                     true,
                     mayWrite) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         if (catNode.getSqlSort() == null) {
                             return Boolean.FALSE;
                         }
                         return catNode.getSqlSort();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Boolean sqlSort = (Boolean)object;
                         final Boolean oldSqlSort = catNode.getSqlSort();
                         try {
                             catNode.setSqlSort(sqlSort);
                             project.getCidsDataObjectBackend().store(catNode);
                         } catch (final Exception ex) {
                             LOG.error("SQLSort could not be changed", ex); // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CatalogNode.class,
                                     "CatalogNode.createSheet().dynaChildrenSQL.setValue(Object).ErrorUtils.duringSqlSortChange.message"),// NOI18N
                                 ex);
                             catNode.setSqlSort(oldSqlSort);
                         }
                     }
                 };                                                         // </editor-fold>
             //J+
 
             final Sheet.Set main = Sheet.createPropertiesSet();
             final Sheet.Set clazz = Sheet.createPropertiesSet();
             final Sheet.Set object = Sheet.createPropertiesSet();
             final Sheet.Set rights = Sheet.createPropertiesSet();
             final Sheet.Set dynaChildren = Sheet.createPropertiesSet();
             main.setName("nodeProps");                                                                                   // NOI18N
             clazz.setName("nodeClassProps");                                                                             // NOI18N
             object.setName("nodeObjectProps");                                                                           // NOI18N
             rights.setName("nodeRights");                                                                                // NOI18N
             dynaChildren.setName("nodeDynaChildrenProps");                                                               // NOI18N
             main.setDisplayName(NbBundle.getMessage(CatalogNode.class, "CatalogNode.createSheet().main.displayName"));   // NOI18N
             clazz.setDisplayName(NbBundle.getMessage(CatalogNode.class, "CatalogNode.createSheet().clazz.displayName")); // NOI18N
             object.setDisplayName(NbBundle.getMessage(
                     CatalogNode.class,
                     "CatalogNode.createSheet().object.displayName"));                                                    // NOI18N
             rights.setDisplayName(NbBundle.getMessage(
                     CatalogNode.class,
                     "CatalogNode.createSheet().rights.displayName"));                                                    // NOI18N
             dynaChildren.setDisplayName(NbBundle.getMessage(
                     CatalogNode.class,
                     "CatalogNode.createSheet().dynaChildren.displayName"));                                              // NOI18N
             main.put(idProp);
             main.put(nameProp);
             main.put(urlProp);
             main.put(nodeTypeProp);
             main.put(rootProp);
             main.put(policyProp);
             main.put(derivePermProp);
             main.put(iconProp);
             // main.put(factoryProp);
             clazz.put(classProp);
             for (final Iterator<Property> props = objectProps.iterator(); props.hasNext();) {
                 object.put(props.next());
             }
             for (final Iterator<Property> props = rightProps.iterator(); props.hasNext();) {
                 rights.put(props.next());
             }
             sheet.put(main);
             sheet.put(clazz);
             sheet.put(object);
             sheet.put(rights);
             dynaChildren.put(dynaChildrenSQL);
             dynaChildren.put(dynaChildrenSQLSort);
             sheet.put(dynaChildren);
         } catch (final Exception ex) {
             LOG.error("could not create property sheet", ex);                               // NOI18N
             ErrorUtils.showErrorMessage(NbBundle.getMessage(
                     CatalogNode.class,
                     "CatalogNode.createSheet().ErrorUtils.duringPropViewCreation.message"), // NOI18N
                 ex);
         }
         return sheet;
     }
 
     // TODO: use iconfactory if set or get icon from icon resource
     @Override
     public Image getIcon(final int i) {
         if (catNode.getDynamicChildren() != null) {
             return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_DYN, 0, 0);
         } else if ("N".equalsIgnoreCase(catNode.getNodeType()))    // NOI18N
         {
             return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_ORG, 0, 0);
         } else if ("C".equalsIgnoreCase(catNode.getNodeType()))    // NOI18N
         {
             return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_CLASS, 0, 0);
         } else if ("O".equalsIgnoreCase(catNode.getNodeType()))    // NOI18N
         {
             return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_OBJ, 0, 0);
         } else if ("none".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
         {
             return null;
         } else {
             return IMAGE_CLOSED;
         }
     }
 
     @Override
     public Image getOpenedIcon(final int i) {
         if (catNode.getDynamicChildren() != null) {
             return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_DYN, 0, 0);
         } else if ("N".equalsIgnoreCase(catNode.getNodeType()))    // NOI18N
         {
             return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_ORG, 0, 0);
         } else if ("C".equalsIgnoreCase(catNode.getNodeType()))    // NOI18N
         {
             return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_CLASS, 0, 0);
         } else if ("O".equalsIgnoreCase(catNode.getNodeType()))    // NOI18N
         {
             return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_OBJ, 0, 0);
         } else if ("none".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
         {
             return null;
         } else {
             return IMAGE_OPEN;
         }
     }
 
     @Override
     protected void createPasteTypes(final Transferable transferable, final List list) {
         super.createPasteTypes(transferable, list);
         int mode = NodeTransfer.COPY;
         Node node = NodeTransfer.node(transferable, mode);
         if (!(node instanceof CatalogNode)) {
             mode = NodeTransfer.MOVE;
             node = NodeTransfer.node(transferable, mode);
             if (!(node instanceof CatalogNode)) {
                 return;
             }
         }
 
         //J-
         final CatalogNode pasteNode = (CatalogNode)node;
         // one cannot add children to object nodes
         if (!catNode.getNodeType().equals(CatNode.Type.OBJECT.getType())
                 // one can only perform copy & paste/dnd within a project
                 && pasteNode.project.getProjectDirectory().equals(project.getProjectDirectory())
                 // one cannot paste a node in itself
                 // !pasteNode.equals(this) &&
                 // one cannot paste a node where it already is
                 // !(pasteNode.getParent() != null && pasteNode.getParent().equals(this.catNode))
                 // one cannot insert if this is dynamic node
                 && (catNode.getDynamicChildren() == null))
                 // this restriction must not be present
                 // one cannot insert dynamically created nodes
                 // && (pasteNode.getParent() == null || pasteNode.getParent().getDynamicChildren() == null))
         {
             list.add(new CatalogPasteType(pasteNode, mode));
         }
         //J+
     }
 
     @Override
     public CatNode getCatNode() {
         return catNode;
     }
 
     @Override
     public void setCatNode(final CatNode catNode) {
         this.catNode = catNode;
     }
 
     @Override
     public CatNode getParent() {
         return (parent instanceof CatalogNode) ? ((CatalogNode)parent).catNode : null;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class CatalogPasteType extends PasteType {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient CatalogNode node;
         private final transient int mode;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new CatalogPasteType object.
          *
          * @param  node  DOCUMENT ME!
          * @param  mode  DOCUMENT ME!
          */
         public CatalogPasteType(final CatalogNode node, final int mode) {
             this.node = node;
             this.mode = mode;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Transferable paste() throws IOException {
             if (node.catNode.getId() == -1) {
                 if (SwingUtilities.isEventDispatchThread()) {
                     final int answer = JOptionPane.showOptionDialog(
                             WindowManager.getDefault().getMainWindow(),
                             NbBundle.getMessage(CatalogPasteType.class, "CatalogNode.paste().JOptionPane.message"), // NOI18N
                             NbBundle.getMessage(CatalogPasteType.class, "CatalogNode.paste().JOptionPane.title"),  // NOI18N
                             JOptionPane.YES_NO_OPTION,
                             JOptionPane.WARNING_MESSAGE,
                             null,
                             new Object[] {
                                 NbBundle.getMessage(
                                     CatalogPasteType.class,
                                     "CatalogNode.paste().JOptionPane.yesOption"),                                  // NOI18N
                                 NbBundle.getMessage(CatalogNode.class, "CatalogNode.paste().JOptionPane.noOption") // NOI18N
                             },
                             NbBundle.getMessage(CatalogPasteType.class, "CatalogNode.paste().JOptionPane.noOption")); // NOI18N
                     if (JOptionPane.YES_OPTION != answer) {
                         return null;
                     }
                 } else {
                     final AnswerRunner answerRunner = new AnswerRunner();
                     try {
                         SwingUtilities.invokeAndWait(answerRunner);
                     } catch (final Exception ex) {
                         LOG.warn("could not complete answerrunner", ex);
                     }
                     if (JOptionPane.YES_OPTION != answerRunner.getAnswer()) {
                         return null;
                     }
                 }
             }
             final CatNode parent;
             if (node.parent instanceof CatalogNode) {
                 parent = ((CatalogNode)node.parent).catNode;
             } else {
                 parent = null;
             }
             final Backend backend = project.getCidsDataObjectBackend();
             if (mode == NodeTransfer.COPY) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("copyNode");                                                                         // NOI18N
                 }
                 backend.copyNode(parent, catNode, node.catNode);
             } else {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("moveNode");                                                                         // NOI18N
                 }
                 backend.moveNode(parent, catNode, node.catNode);
                 node.parent.refresh();
             }
             refresh();
             node.refresh();
             return null;
         }
 
         //~ Inner Classes ------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @version  $Revision$, $Date$
          */
         private final class AnswerRunner implements Runnable {
 
             //~ Instance fields ------------------------------------------------
 
             private int answer;
 
             //~ Methods --------------------------------------------------------
 
             @Override
             public void run() {
                 answer = JOptionPane.showOptionDialog(
                         WindowManager.getDefault().getMainWindow(),
                         NbBundle.getMessage(CatalogPasteType.class, "CatalogNode.paste().JOptionPane.message"), // NOI18N
                         NbBundle.getMessage(CatalogPasteType.class, "CatalogNode.paste().JOptionPane.title"),  // NOI18N
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.WARNING_MESSAGE,
                         null,
                         new Object[] {
                             NbBundle.getMessage(
                                 CatalogPasteType.class,
                                 "CatalogNode.paste().JOptionPane.yesOption"),                                  // NOI18N
                             NbBundle.getMessage(CatalogNode.class, "CatalogNode.paste().JOptionPane.noOption") // NOI18N
                         },
                         NbBundle.getMessage(CatalogPasteType.class, "CatalogNode.paste().JOptionPane.noOption")); // NOI18N
             }
 
             /**
              * DOCUMENT ME!
              *
              * @return  DOCUMENT ME!
              */
             int getAnswer() {
                 return answer;
             }
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class CatalogNodeChildren extends ProjectChildren {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(CatalogNodeChildren.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient CatNode catNode;
     private final transient CatalogManagement catalogManagement;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CatalogNodeChildren object.
      *
      * @param  node     DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      */
     public CatalogNodeChildren(final CatNode node, final DomainserverProject project) {
         super(project);
         this.catNode = node;
         catalogManagement = project.getLookup().lookup(CatalogManagement.class);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected Node[] createUserNodes(final Object key) {
         if (key instanceof CatNode) {
             final CatNode node = (CatNode)key;
             final CatalogNode cn = new CatalogNode(node, project, (Refreshable)getNode());
             catalogManagement.addOpenNode(node, cn);
             return new Node[] { cn };
         } else if (key instanceof Node) {
             return new Node[] { (Node)key };
         } else {
             return new Node[] {};
         }
     }
 
     @Override
     protected void threadedNotify() throws IOException {
         try {
             final List<CatNode> l = project.getCidsDataObjectBackend().getNodeChildren(catNode);
             Collections.sort(l, new Comparators.CatNodes());
             setKeysEDT(l);
         } catch (final ObjectNotFoundException ex) {
             final CatNode node = new CatNode();
             node.setName(NbBundle.getMessage(
                     CatalogNodeChildren.class,
                     "CatalogNode.ChildrenBuilder.threadedNotify().node.errorName"));                                   // NOI18N
             node.setNodeType("none");                                                                                  // NOI18N
             node.setIsLeaf(true);
             node.setIsRoot(false);
             node.setId(-1);
             if (catNode.getCidsClass() == null) {
                 ErrorUtils.showErrorMessage(
                     NbBundle.getMessage(
                         CatalogNodeChildren.class,
                         "CatalogNode.ChildrenBuilder.threadedNotify().ErrorUtils.dataInconsistencyCheckDB.message"),   // NOI18N
                     ex);
             } else {
                 ErrorUtils.showErrorMessage(
                     NbBundle.getMessage(
                         CatalogNodeChildren.class,
                         "CatalogNode.ChildrenBuilder.threadedNotify().ErrorUtils.dataInconsistencyCheckTable.message", // NOI18N
                         catNode.getCidsClass().getTableName()),
                     ex);
             }
             LOG.error("data inconsistency", ex);                                                                       // NOI18N
             setKeysEDT(new Object[] { node });
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class DynamicCatalogNodeChildren extends ProjectChildren {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(DynamicCatalogNodeChildren.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient CatNode parentNode;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DynamicCatalogNodeChildren object.
      *
      * @param  node     DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      */
     public DynamicCatalogNodeChildren(final CatNode node, final DomainserverProject project) {
         super(project);
         this.parentNode = node;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected Node[] createUserNodes(final Object o) {
         if (o instanceof CatNode) {
             final CatNode catNode = (CatNode)o;
             return new Node[] { new CatalogNode(catNode, project, (Refreshable)getNode()) };
         } else {
             final AbstractNode node = new AbstractNode(Children.LEAF);
             node.setName(o.toString());
             return new Node[] { node };
         }
     }
 
     @Override
     protected void threadedNotify() throws IOException {
         Connection con = null;
         try {
             con = DatabaseConnection.getConnection(project.getRuntimeProps());
         } catch (final SQLException ex) {
             LOG.error("could not connect to database", ex);                                 // NOI18N
             setKeysEDT(
                 new Object[] {
                     NbBundle.getMessage(
                         DynamicCatalogNodeChildren.class,
                         "CatalogNode.DynamicCatalogNodeChildren.addNotify().duringConToDB") // NOI18N
                 });
 
             return;
         }
 
         ResultSet set = null;
         try {
             set = con.createStatement().executeQuery(parentNode.getDynamicChildren());
         } catch (final SQLException ex) {
             LOG.error("could not fetch resultset", ex);                                         // NOI18N
             setKeysEDT(
                 new Object[] {
                     NbBundle.getMessage(
                         DynamicCatalogNodeChildren.class,
                         "CatalogNode.DynamicCatalogNodeChildren.addNotify().queryUnsuccessful") // NOI18N
                 });
             refresh();
 
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeConnection(con);
 
             return;
         }
 
         try {
             final List<CatNode> catNodes = new LinkedList<CatNode>();
             final HashMap<Integer, CidsClass> classCache = new HashMap<Integer, CidsClass>();
             while (set.next()) {
                 final CatNode c = new CatNode();
                 c.setName(set.getString("name"));                                                   // NOI18N
                 c.setDynamicChildren(set.getString("dynamic_children"));                            // NOI18N
                 c.setSqlSort(set.getBoolean("sql_sort"));                                           // NOI18N
                 try {
                     c.setId(set.getInt("id"));                                                      // NOI18N
                 } catch (final SQLException ex) {
                     LOG.warn("id could not be set", ex);                                            // NOI18N
                 }
                 try {
                     c.setObjectId(set.getInt("object_id"));                                         // NOI18N
                 } catch (final SQLException ex) {
                     LOG.warn("object_id could not be set", ex);                                     // NOI18N
                 }
                 try {
                     c.setNodeType(set.getString("node_type"));                                      // NOI18N
                 } catch (final SQLException ex) {
                     LOG.warn("node_type could not be set", ex);                                     // NOI18N
                 }
                 try {
                     final int classId = set.getInt("class_id");                                     // NOI18N
                     final CidsClass clazz;
                     if (classCache.containsKey(classId)) {
                         clazz = classCache.get(classId);
                     } else {
                         clazz = project.getCidsDataObjectBackend().getEntity(CidsClass.class, classId);
                         classCache.put(classId, clazz);
                     }
                     c.setCidsClass(clazz);
                 } catch (final NoResultException ex) {
                     LOG.warn("cidsclass could not be set", ex);                                     // NOI18N
                 } catch (final SQLException ex) {
                     LOG.warn("cidsclass could not be set", ex);                                     // NOI18N
                 }
                 try {
                     final URL url = getURL(set.getString("url"));                                   // NOI18N
                     c.setUrl(url);
                 } catch (final SQLException ex) {
                     LOG.warn("url could not be set", ex);                                           // NOI18N
                 }
                 c.setIsLeaf(true);
                 catNodes.add(c);
             }
             if ((parentNode.getSqlSort() == null) || !parentNode.getSqlSort()) {
                 Collections.sort(catNodes, new Comparators.CatNodes());
             }
             setKeysEDT(catNodes);
         } catch (final SQLException ex) {
             LOG.error("could not evaluate resultset", ex);                                          // NOI18N
             setKeysEDT(
                 new Object[] {
                     NbBundle.getMessage(
                         DynamicCatalogNodeChildren.class,
                         "CatalogNode.DynamicCatalogNodeChildren.addNotify().queryResultEvaluation") // NOI18N
                 });
         } finally {
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeConnection(con);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   s  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private URL getURL(final String s) {
        final URL url = new URL();
        url.setObjectName(s);
        final URLBase base = new URLBase();
        base.setPath("");           // NOI18N
        base.setProtocolPrefix(""); // NOI18N
        base.setServer("");         // NOI18N
        url.setUrlbase(base);
        return url;
     }
 }
