 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.cidsclass;
 
 import org.apache.log4j.Logger;
 
 import org.openide.ErrorManager;
 import org.openide.actions.DeleteAction;
 import org.openide.nodes.Index;
 import org.openide.nodes.Node;
 import org.openide.nodes.Node.Property;
 import org.openide.nodes.PropertySupport;
 import org.openide.nodes.Sheet;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CallableSystemAction;
 import org.openide.windows.TopComponent;
 import org.openide.windows.WindowManager;
 
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import java.beans.PropertyEditor;
 
 import java.io.IOException;
 
 import java.lang.reflect.InvocationTargetException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.Action;
 import javax.swing.JOptionPane;
 import javax.swing.event.ChangeListener;
 
 import de.cismet.cids.abf.domainserver.RefreshAction;
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.PolicyPropertyEditor;
 import de.cismet.cids.abf.domainserver.project.ProjectChildren;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
 import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassDiagramTopComponent;
 import de.cismet.cids.abf.domainserver.project.icons.IconPropertyEditor;
 import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
 import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
 import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
 import de.cismet.cids.abf.domainserver.project.nodes.TypeManagement;
 import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
 import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
 import de.cismet.cids.abf.utilities.Refreshable;
 import de.cismet.cids.abf.utilities.windows.ErrorUtils;
 
 import de.cismet.cids.jpa.entity.cidsclass.Attribute;
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
 import de.cismet.cids.jpa.entity.cidsclass.Icon;
 import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
 import de.cismet.cids.jpa.entity.permission.ClassPermission;
 import de.cismet.cids.jpa.entity.permission.Permission;
 import de.cismet.cids.jpa.entity.permission.Policy;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  1.30
  */
 public final class CidsClassNode extends ProjectNode implements Refreshable, CidsClassContextCookie {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(CidsClassNode.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Image defaultClassImage;
     private final transient PermissionResolver permResolve;
 
     // TODO: cannot be final because is new assigned when class is stored,
     // can possibly be refactored as after storage the children will be
     // refreshed and newly loaded
     private transient CidsClass cidsClass;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CidsClassNode object.
      *
      * @param  cidsClass  DOCUMENT ME!
      * @param  project    DOCUMENT ME!
      */
     public CidsClassNode(final CidsClass cidsClass, final DomainserverProject project) {
         super(new CidsClassNodeChildren(cidsClass, project), project);
         this.cidsClass = cidsClass;
         defaultClassImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "class.png"); // NOI18N
         permResolve = PermissionResolver.getInstance(project);
         getCookieSet().add((CidsClassNodeChildren)getChildren());
         getCookieSet().add(this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getIcon(final int i) {
         if (cidsClass.getClassIcon() == null) {
             return defaultClassImage;
         }
         final Image ic = ProjectUtils.getImageForIconAndProject(cidsClass.getClassIcon(), project);
         if (ic == null) {
             return defaultClassImage;
         } else {
             final Image image = ic.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
             final BufferedImage result = new BufferedImage(10, 10,
                     BufferedImage.TYPE_INT_ARGB);
             final Graphics2D g = result.createGraphics();
             g.drawImage(image, 0, 0, null);
             g.dispose();
             final Image badged = ImageUtilities.mergeImages(
                     defaultClassImage,
                     result,
                     11,
                     7);
             return badged;
         }
     }
 
     @Override
     public Image getOpenedIcon(final int i) {
         return getIcon(i);
     }
 
     @Override
     public String getDisplayName() {
         return cidsClass.getTableName();
     }
 
     @Override
     protected Sheet createSheet() {
         final Sheet sheet = Sheet.createDefault();
         final Sheet.Set main = Sheet.createPropertiesSet();
         final Sheet.Set icons = Sheet.createPropertiesSet();
         final Sheet.Set classes = Sheet.createPropertiesSet();
         final Sheet.Set classAttributes = Sheet.createPropertiesSet();
         final Sheet.Set rightAttributes = Sheet.createPropertiesSet();
         final String storeError = "could not store class: "; // NOI18N
         try {
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ID ">
             final Property<Integer> idProp = new PropertySupport.Reflection<Integer>(
                     cidsClass,
                     Integer.class,
                     "getId", // NOI18N
                     null);
             idProp.setName(NbBundle.getMessage(
                     CidsClassNode.class,
                     "CidsClassNode.createSheet().idProp.name")); // NOI18N
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
             final Property<String> nameProp = new PropertySupport<String>(
                     "name",                                              // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().nameProp.classname"), // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().nameProp.nameOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getName();
                     }
 
                     @Override
                     public void setValue(final String object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String old = cidsClass.getName();
                         try {
                             cidsClass.setName(object.toString());
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             fireDisplayNameChange(null, object.toString());
                             refresh();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setName(old);
                         }
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Tablename ">
             final Property<String> tablenameProp = new PropertySupport<String>(
                     "tablename",                                              // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().tablenameProp.tablename"), // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().tablenameProp.nameOfTable"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getTableName();
                     }
 
                     @Override
                     public void setValue(final String object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String old = cidsClass.getTableName();
                         try {
                             cidsClass.setTableName(object.toString());
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refresh();
                             project.getLookup().lookup(SyncManagement.class).refresh();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setTableName(old);
                         }
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: PrimaryKeyField ">
             final Property<String> primaryKeyFieldProp = new PropertySupport<String>(
                     "primaryKeyField",                                                            // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().primaryKeyFieldProp.fieldnameOfPrimKEy"),    // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().primaryKeyFieldProp.fieldnameOfPrimKeyOfTable"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getPrimaryKeyField();
                     }
 
                     @Override
                     public void setValue(final String object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String old = cidsClass.getPrimaryKeyField();
                         try {
                             cidsClass.setPrimaryKeyField(object.toString());
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                             project.getLookup().lookup(SyncManagement.class).refresh();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setPrimaryKeyField(old);
                         }
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Indexed ">
             final Property indexedProp = new PropertySupport(
                     "indexed",                                                     // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().indexedProp.inSearchIndex"),  // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().indexedProp.addedToSearchIndex"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.isIndexed();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Boolean old = cidsClass.isIndexed();
                         try {
                             fireIconChange();
                             cidsClass.setIndexed((Boolean)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setIndexed(old);
                         }
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Arraylink ">
             final Property arraylinkProp = new PropertySupport(
                     "arrayLink",                                                    // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().arraylinkProp.arrayclass"),    // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().arraylinkProp.classIsArrayclass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.isArrayLink();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Boolean old = cidsClass.isArrayLink();
                         try {
                             fireIconChange();
                             cidsClass.setArrayLink((Boolean)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setArrayLink(old);
                         }
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Description ">
             final Property descriptionProp = new PropertySupport(
                     "description",                                                     // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().descriptionProp.description"),    // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().descriptionProp.descriptionOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         final String val = cidsClass.getDescription();
                         return (val == null) ? "" : val; // NOI18N
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String old = cidsClass.getDescription();
                         try {
                             cidsClass.setDescription(object.toString());
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setDescription(old);
                         }
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ClassPolicy ">
             final Property policyProp = new PropertySupport(
                     "classPolicy",                                                     // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().policyProp.classPolicy"),         // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().policyProp.basicPolicyOnClassLevel"), // NOI18N
                     true,
                     true) {
 
                     private PolicyPropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         Policy p = cidsClass.getPolicy();
                         if (p == null) {
                             final PermissionResolver.Result r = permResolve.getPermString(cidsClass, null);
                             if (r.getInheritanceString() == null) {
                                 // should never occur
                                 p = Policy.NO_POLICY;
                             } else {
                                 p = new Policy();
                                 p.setName("<" // NOI18N
                                             + r.getInheritanceString()
                                             + ">"); // NOI18N
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
                                 "object must be of type Policy");                                                  // NOI18N
                         }
                         final Policy policy = (Policy)object;
                         final Policy oldPolicy = cidsClass.getPolicy();
                         try {
                             cidsClass.setPolicy((policy.getId() == null) ? null : policy);
                             project.getCidsDataObjectBackend().store(cidsClass);
                             refresh();
                             firePropertySetsChange(null, getPropertySets());
                         } catch (final Exception ex) {
                             LOG.error("policy could not be changed", ex);                                          // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsClassNode.class,
                                     "CidsClassNode.createSheet().policyProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 ex);
                             cidsClass.setPolicy(oldPolicy);
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
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: AttributePolicy ">
             final Property attrPolicyProp = new PropertySupport(
                     "attrPolicy",                                                          // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().attrPolicyProp.attrPolicy"),          // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().attrPolicyProp.basicPolicyForRightEval"), // NOI18N
                     true,
                     true) {
 
                     private PolicyPropertyEditor editor;
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         Policy p = cidsClass.getAttributePolicy();
                         if (p == null) {
                             final Set<Attribute> attr = cidsClass.getAttributes();
                             if (attr.isEmpty()) {
                                 p = Policy.NO_POLICY;
                             } else {
                                 final PermissionResolver.Result r = permResolve.getPermString(attr.iterator().next(),
                                         null);
                                 if (r.getInheritanceString() == null) {
                                     // should never occur
                                     p = Policy.NO_POLICY;
                                 } else {
                                     p = new Policy();
                                     p.setName("<" // NOI18N
                                                 + r.getInheritanceString()
                                                 + ">"); // NOI18N
                                 }
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
                                 "object must be of type Policy");                                                      // NOI18N
                         }
                         final Policy policy = (Policy)object;
                         final Policy oldPolicy = cidsClass.getAttributePolicy();
                         try {
                             cidsClass.setAttributePolicy((policy.getId() == null) ? null : policy);
                             project.getCidsDataObjectBackend().store(cidsClass);
                             refreshChildren();
                         } catch (final Exception ex) {
                             LOG.error("policy could not be changed", ex);                                              // NOI18N
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsClassNode.class,
                                     "CidsClassNode.createSheet().attrPolicyProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 ex);
                             cidsClass.setAttributePolicy(oldPolicy);
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
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ClassIcon ">
             final IconPropertyEditor iconPropertyEditor = new IconPropertyEditor(project);
             final Property classIconProp = new PropertySupport(
                     "classIcon",                                              // NOI18N
                     Icon.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().classIconProp.classIcon"), // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().classIconProp.iconOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getClassIcon();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Icon old = cidsClass.getClassIcon();
                         try {
                             cidsClass.setClassIcon((Icon)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             fireOpenedIconChange();
                             fireIconChange();
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setClassIcon(old);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return iconPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ObjectIcon ">
             final Property objectIconProp = new PropertySupport(
                     "objectIcon",                                              // NOI18N
                     Icon.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().objectIconProp.objectIcon"), // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().objectIconProp.iconOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getObjectIcon();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Icon old = cidsClass.getObjectIcon();
                         try {
                             cidsClass.setObjectIcon((Icon)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setObjectIcon(old);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return iconPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ToString ">
             final JavaClassPropertyEditor toStringPropertyEditor = new JavaClassPropertyEditor(
                     project,
                     JavaClass.Type.TO_STRING);
             final Property toStringProp = new PropertySupport(
                     "toString",                                                       // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().toStringProp.toString"),         // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().toStringProp.toStringClassOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getToString();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final JavaClass old = cidsClass.getToString();
                         if (object == null) {
                             final int answer = JOptionPane.showConfirmDialog(
                                     WindowManager.getDefault().getMainWindow(),
                                     NbBundle.getMessage(
                                         CidsClassNode.class,
                                         "CidsClassNode.setValue(Object).JOptionPane.message"), // NOI18N
                                     NbBundle.getMessage(
                                         CidsClassNode.class,
                                         "CidsClassNode.setValue(Object).JOptionPane.title"), // NOI18N
                                     JOptionPane.YES_NO_OPTION,
                                     JOptionPane.QUESTION_MESSAGE);
                             if (answer == JOptionPane.NO_OPTION) {
                                 return;
                             }
                         }
                         try {
                             cidsClass.setToString((JavaClass)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setToString(old);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return toStringPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Editor ">
             final JavaClassPropertyEditor editorPropertyEditor = new JavaClassPropertyEditor(
                     project,
                     JavaClass.Type.SIMPLE_EDITOR,
                     JavaClass.Type.COMPLEX_EDITOR);
             final Property editorProp = new PropertySupport(
                     "editor",                                                     // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().editorProp.editor"),         // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().editorProp.editorClassOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getEditor();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final JavaClass old = cidsClass.getEditor();
                         try {
                             cidsClass.setEditor((JavaClass)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setEditor(old);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return editorPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Renderer ">
             final JavaClassPropertyEditor rendererPropertyEditor = new JavaClassPropertyEditor(
                     project,
                     JavaClass.Type.RENDERER);
             final Property rendererProp = new PropertySupport(
                     "rendererProp",                                                   // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().rendererProp.renderer"),         // NOI18N
                     NbBundle.getMessage(
                         CidsClassNode.class,
                         "CidsClassNode.createSheet().rendererProp.rendererClassOfClass"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsClass.getRenderer();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final JavaClass old = cidsClass.getRenderer();
                         try {
                             cidsClass.setRenderer((JavaClass)object);
                             cidsClass = project.getCidsDataObjectBackend().store(
                                     cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error(storeError + cidsClass.getName(), e);
                             ErrorManager.getDefault().notify(e);
                             cidsClass.setRenderer(old);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return rendererPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Properties: ClassAttribute ">
             final List<ClassAttribute> allClassAttributes = new ArrayList<ClassAttribute>(
                     cidsClass.getClassAttributes());
             for (final ClassAttribute classAttr : allClassAttributes) {
                 final Property classAttrProp = new PropertySupport(
                         "classAttrProp"
                                 + classAttr.getAttrKey(),                              // NOI18N
                         String.class,
                         classAttr.getAttrKey(),
                         NbBundle.getMessage(
                                     CidsClassNode.class,
                                     "CidsClassNode.createSheet().classAttrProp.classAttr") // NOI18N
                                 + classAttr.getAttrKey(),
                         true,
                         true) {
 
                         @Override
                         public Object getValue() throws IllegalAccessException, InvocationTargetException {
                             return classAttr.getAttrValue();
                         }
 
                         @Override
                         public void setValue(final Object object) throws IllegalAccessException,
                             IllegalArgumentException,
                             InvocationTargetException {
                             final String old = classAttr.getAttrValue();
                             try {
                                 classAttr.setAttrValue(object.toString());
                                 project.getCidsDataObjectBackend().store(classAttr);
                                 refreshInDiagram();
                             } catch (final Exception e) {
                                 LOG.error("could not store class attribute", // NOI18N
                                     e);
                                 ErrorManager.getDefault().notify(e);
                                 classAttr.setAttrValue(old);
                             }
                         }
                     };
                 classAttributes.put(classAttrProp);
             }                         // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Properties: ClassPermission ">
             final List<ClassPermission> allClassPermissions = new ArrayList<ClassPermission>(
                     cidsClass.getClassPermissions());
             for (final ClassPermission perm : allClassPermissions) {
                 final String ug;
                 if (ProjectUtils.isRemoteGroup(perm.getUserGroup(), project)) {
                     ug = perm.getUserGroup().getName()
                                 + "@"           // NOI18N
                                 + perm.getUserGroup().getDomain();
                 } else {
                     ug = perm.getUserGroup().getName();
                 }
                 final Property classPermissionProp = new PropertySupport(
                         "classPerm"
                                 + perm.getId(), // NOI18N
                         String.class,
                         ug,
                         "",                     // NOI18N
                         true,
                         false) {
 
                         @Override
                         public Object getValue() throws IllegalAccessException, InvocationTargetException {
                             final Permission p = perm.getPermission();
                             String s = permResolve.getPermString(cidsClass, p).getPermissionString();
                             if (s == null) {
                                 s = p.getKey();
                             }
                             return s;
                         }
 
                         @Override
                         public void setValue(final Object object) throws IllegalAccessException,
                             IllegalArgumentException,
                             InvocationTargetException {
                             // read-only
                         }
                     };
                 rightAttributes.put(classPermissionProp);
             }                                                                    // </editor-fold>
             main.setName("properties");                                          // NOI18N
             icons.setName("icons");                                              // NOI18N
             classes.setName("java");                                             // NOI18N
             classAttributes.setName("classattributes");                          // NOI18N
             rightAttributes.setName("rights");                                   // NOI18N
             main.setDisplayName(NbBundle.getMessage(
                     CidsClassNode.class,
                     "CidsClassNode.createSheet().main.displayName"));            // NOI18N
             icons.setDisplayName(NbBundle.getMessage(
                     CidsClassNode.class,
                     "CidsClassNode.createSheet().icons.displayName"));           // NOI18N
             classes.setDisplayName(NbBundle.getMessage(
                     CidsClassNode.class,
                     "CidsClassNode.createSheet().classes.displayName"));         // NOI18N
             classAttributes.setDisplayName(NbBundle.getMessage(
                     CidsClassNode.class,
                     "CidsClassNode.createSheet().classAttributes.displayName")); // NOI18N
             rightAttributes.setDisplayName(NbBundle.getMessage(
                     CidsClassNode.class,
                     "CidsClassNode.createSheet().rightAttributes.displayName")); // NOI18N
             main.put(idProp);
             main.put(nameProp);
             main.put(tablenameProp);
             main.put(primaryKeyFieldProp);
             main.put(indexedProp);
             main.put(arraylinkProp);
             main.put(descriptionProp);
             main.put(policyProp);
             main.put(attrPolicyProp);
             icons.put(classIconProp);
             icons.put(objectIconProp);
             classes.put(toStringProp);
             classes.put(editorProp);
             classes.put(rendererProp);
             sheet.put(main);
             sheet.put(icons);
             sheet.put(classes);
             if (classAttributes.getProperties().length > 0) {
                 sheet.put(classAttributes);
             }
             if (rightAttributes.getProperties().length > 0) {
                 sheet.put(rightAttributes);
             }
         } catch (final Exception ex) {
             LOG.warn("error during sheet creation", ex);                         // NOI18N
             ErrorManager.getDefault().notify(ex);
         }
         return sheet;
     }
 
     @Override
     public CidsClass getCidsClass() {
         return cidsClass;
     }
 
     @Override
     public Action[] getActions(final boolean b) {
         return new Action[] {
                 CallableSystemAction.get(RefreshAction.class),
                 null,
                 CallableSystemAction.get(EditCidsClassWizardAction.class),
                 CallableSystemAction.get(EditRightsWizardAction.class),
                 CallableSystemAction.get(CheckRightsAction.class),
                 null,
                 CallableSystemAction.get(
                     AddClassWithRelationsToCurrentDiagramAction.class),
                 CallableSystemAction.get(AddClassToCurrentDiagramAction.class),
                 null,
                 CallableSystemAction.get(
                     CreateDiagramFromClassWithRelationsAction.class),
                 CallableSystemAction.get(CreateDiagramFromClassAction.class),
                 null,
                 CallableSystemAction.get(DeleteAction.class),
                null,
                CallableSystemAction.get(ExportClassesAction.class),
                 null,
                 CallableSystemAction.get(IndexAction.class)
             };
     }
 
     @Override
     public boolean canDestroy() {
         return true;
     }
 
     @Override
     public void destroy() {
         if (project.getCidsDataObjectBackend().stillReferenced(cidsClass.getType())) {
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         JOptionPane.showMessageDialog(
                             WindowManager.getDefault().getMainWindow(),
                             NbBundle.getMessage(
                                 CidsClassNode.class,
                                 "CidsClassNode.destroy().JOptionPane.message"), // NOI18N
                             NbBundle.getMessage(
                                 CidsClassNode.class,
                                 "CidsClassNode.destroy().JOptionPane.title"), // NOI18N
                             JOptionPane.INFORMATION_MESSAGE);
                     }
                 });
             return;
         }
         try {
             project.getDiffAccessor().putDropAction(cidsClass.getTableName());
             project.getCidsDataObjectBackend().delete(cidsClass);
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         ClassDiagramTopComponent.classDestroyed(cidsClass);
                         project.getLookup().lookup(ClassManagement.class).refresh();
                         project.getLookup().lookup(TypeManagement.class).refresh();
                         project.getLookup().lookup(SyncManagement.class).refresh();
                     }
                 });
         } catch (final Exception e) {
             LOG.error("error during deletion", e); // NOI18N
             ErrorManager.getDefault().notify(e);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void refreshChildren() {
         ((CidsClassNodeChildren)getChildren()).setCidsClass(cidsClass);
     }
 
     /**
      * DOCUMENT ME!
      */
     void refreshInDiagram() {
         final Set opened = TopComponent.getRegistry().getOpened();
         for (final Object o : opened) {
             if (o instanceof ClassDiagramTopComponent) {
                 ((ClassDiagramTopComponent)o).refreshClassWidget(this);
             }
         }
     }
 
     @Override
     public void refresh() {
         cidsClass = project.getCidsDataObjectBackend().getEntity(CidsClass.class, cidsClass.getId());
         refreshChildren();
         refreshInDiagram();
         fireIconChange();
         fireOpenedIconChange();
         // as long as the props will not be refreshed...
         setSheet(createSheet());
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class CidsClassNodeChildren extends ProjectChildren implements Index {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(CidsClassNodeChildren.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Index indexSupport;
     private transient CidsClass cidsClass;
     private transient boolean doReorder;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CidsClassNodeChildren object.
      *
      * @param  cidsClass  DOCUMENT ME!
      * @param  project    DOCUMENT ME!
      */
     public CidsClassNodeChildren(final CidsClass cidsClass, final DomainserverProject project) {
         super(project);
         this.cidsClass = cidsClass;
         this.doReorder = false;
         indexSupport = new IndexSupport();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsClass  DOCUMENT ME!
      */
     void setCidsClass(final CidsClass cidsClass) {
         this.cidsClass = cidsClass;
         refreshByNotify();
     }
 
     @Override
     public void removeChangeListener(final ChangeListener changeListener) {
         indexSupport.removeChangeListener(changeListener);
     }
 
     @Override
     public void addChangeListener(final ChangeListener changeListener) {
         indexSupport.addChangeListener(changeListener);
     }
 
     @Override
     public void reorder(final int[] i) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("REORDER"); // NOI18N
         }
         indexSupport.reorder(i);
     }
 
     @Override
     public void moveUp(final int i) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("moveUp"); // NOI18N
         }
         indexSupport.moveUp(i);
     }
 
     @Override
     public void moveDown(final int i) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("moveDown"); // NOI18N
         }
         indexSupport.moveDown(i);
     }
 
     @Override
     public int indexOf(final Node node) {
         final int ret = indexSupport.indexOf(node);
         if (LOG.isDebugEnabled()) {
             LOG.debug("indexof = " + ret); // NOI18N
         }
         return ret;
     }
 
     @Override
     public void reorder() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("reorder"); // NOI18N
         }
         indexSupport.reorder();
     }
 
     @Override
     public void move(final int from, final int to) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("move"); // NOI18N
         }
         indexSupport.move(from, to);
     }
 
     @Override
     public void exchange(final int x, final int y) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("exchange"); // NOI18N
         }
         indexSupport.exchange(x, y);
     }
 
     @Override
     protected Node[] createUserNodes(final Object o) {
         if (o instanceof Attribute) {
             return new Node[] { new CidsAttributeNode((Attribute)o, cidsClass, project) };
         } else {
             return new Node[] {};
         }
     }
 
     @Override
     protected void threadedNotify() throws IOException {
         try {
             final LinkedList<Attribute> children = new LinkedList(cidsClass.getAttributes());
             doReorder = false;
             final Comparator comp = new Comparator() {
 
                     @Override
                     public int compare(final Object o1, final Object o2) {
                         final Attribute a1 = (Attribute)o1;
                         final Attribute a2 = (Attribute)o2;
 
                         if ((a1.getPosition() != null) && (a2.getPosition() != null)) {
                             final int comp = a1.getPosition().compareTo(a2.getPosition());
 
                             if (comp == 0) {
                                 doReorder = true;
                                 return a1.getName().compareTo(a2.getName());
                             } else {
                                 return comp;
                             }
                         } else if ((a1.getPosition() == null) && (a2.getPosition() != null)) {
                             doReorder = true;
                             return 1;
                         } else if ((a1.getPosition() != null) && (a2.getPosition() == null)) {
                             doReorder = true;
                             return -1;
                         } else {
                             doReorder = true;
                             return a1.getName().compareTo(a2.getName());
                         }
                     }
 
                     @Override
                     public boolean equals(final Object obj) {
                         return false;
                     }
                 };
 
             Collections.sort(children, comp);
             try {
                 if (doReorder) {
                     for (int i = 0; i < children.size(); ++i) {
                         final Attribute a = children.get(i);
                         if ((a.getPosition() == null) || !a.getPosition().equals(i)) {
                             a.setPosition(i);
                             project.getCidsDataObjectBackend().store(a);
                         }
                     }
                     Collections.sort(children, comp);
                 }
             } catch (final Exception e) {
                 LOG.warn("error during reorder", e); // NOI18N
                 ErrorManager.getDefault().notify(e);
             }
 
             setKeysEDT(children);
         } catch (final Exception ex) {
             LOG.error("could not create children", ex); // NOI18N
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class IndexSupport extends Index.Support {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void reorder(final int[] perm) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("EMPTY REORDER"); // NOI18N
             }
         }
 
         @Override
         public int getNodesCount() {
             return CidsClassNodeChildren.this.getNodesCount();
         }
 
         @Override
         public Node[] getNodes() {
             return CidsClassNodeChildren.this.getNodes();
         }
     }
 }
