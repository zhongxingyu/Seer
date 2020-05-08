 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.cidsclass;
 
 import org.apache.log4j.Logger;
 
 import org.openide.actions.DeleteAction;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.nodes.Node.Property;
 import org.openide.nodes.PropertySupport;
 import org.openide.nodes.Sheet;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CallableSystemAction;
 
 import java.awt.Image;
 
 import java.beans.PropertyEditor;
 
 import java.lang.reflect.InvocationTargetException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Action;
 
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
 import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
 import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
 import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
 import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
 import de.cismet.cids.abf.utilities.Refreshable;
 import de.cismet.cids.abf.utilities.windows.ErrorUtils;
 
 import de.cismet.cids.jpa.entity.cidsclass.Attribute;
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
 import de.cismet.cids.jpa.entity.permission.AttributePermission;
 import de.cismet.cids.jpa.entity.permission.Permission;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class CidsAttributeNode extends ProjectNode implements CidsClassContextCookie, CidsAttributeContextCookie {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(CidsAttributeNode.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Image attributeImage;
     private final transient Image attributeGrayImage;
     private final transient Image arrayBadge;
     private final transient Image foreignKeyBadge;
     private final transient Image foreignKeyBadBadge;
     private final transient Image primaryKeyBadge;
     private final transient Image indexBadge;
 
     private final transient PermissionResolver permResolve;
 
     private transient CidsClass cidsClass;
     private transient Attribute cidsAttribute;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CidsAttributeNode object.
      *
      * @param  cidsAttribute  DOCUMENT ME!
      * @param  cidsClass      DOCUMENT ME!
      * @param  project        DOCUMENT ME!
      */
     public CidsAttributeNode(final Attribute cidsAttribute,
             final CidsClass cidsClass,
             final DomainserverProject project) {
         super(Children.LEAF, project);
         this.cidsClass = cidsClass;
         this.cidsAttribute = cidsAttribute;
         setName(cidsAttribute.getFieldName());
         attributeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "attribute.png");               // NOI18N
         attributeGrayImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "attribute_gray.png");      // NOI18N
         arrayBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_array.png");                 // NOI18N
         foreignKeyBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_foreign_key.png");      // NOI18N
         foreignKeyBadBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_bad_oneToMany.png"); // NOI18N
         primaryKeyBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_key.png");              // NOI18N
         indexBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "badge_search.png");                // NOI18N
         permResolve = PermissionResolver.getInstance(project);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getIcon(final int i) {
         // extension attributes won't be badged
         if (cidsAttribute.isExtensionAttr()) {
             return attributeGrayImage;
         }
 
         Image ret = attributeImage;
         int count = 0;
         if (cidsAttribute.isIndexed()) {
             ret = ImageUtilities.mergeImages(ret, indexBadge, 16, 8);
             count++;
         }
         if (cidsClass.getPrimaryKeyField().equals(cidsAttribute.getFieldName())) {
             ret = ImageUtilities.mergeImages(ret, primaryKeyBadge, 16, 8);
             count++;
         }
         if (cidsAttribute.isForeignKey()) {
             final Image badge;
             if (cidsAttribute.getForeignKeyClass() < 0) {
                 final Integer refKey = Math.abs(cidsAttribute.getForeignKeyClass());
                 // TODO: this may slow down the EDT, we probably have to put it in a task and fire an icon change
                 // afterwards
                 final CidsClass ref = project.getCidsDataObjectBackend().getEntity(CidsClass.class, refKey);
 
                 if (isOneToManyBackrefPresent(ref)) {
                     badge = foreignKeyBadge;
                 } else {
                     badge = foreignKeyBadBadge;
                 }
             } else {
                 badge = foreignKeyBadge;
             }
 
             if (count == 0) {
                 ret = ImageUtilities.mergeImages(ret, badge, 16, 8);
             } else {
                 ret = ImageUtilities.mergeImages(ret, badge, 16, 0);
             }
             count++;
         }
         if (cidsAttribute.isArray()) {
             switch (count) {
                 case 0: {
                     ret = ImageUtilities.mergeImages(ret, arrayBadge, 16, 8);
                     break;
                 }
                 case 1: {
                     ret = ImageUtilities.mergeImages(ret, arrayBadge, 16, 0);
                     break;
                 }
                 default: {
                     ret = ImageUtilities.mergeImages(ret, arrayBadge, 8, 8);
                 }
             }
         }
 
         return ret;
     }
 
     @Override
     public String getHtmlDisplayName() {
         if ((cidsAttribute.isVisible() == null) || cidsAttribute.isVisible()) {
             return "<font color='!textText'>" + getName() + "</font>";      // NOI18N
         } else {
             return "<font color='!controlShadow'>" + getName() + "</font>"; // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fkClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isOneToManyBackrefPresent(final CidsClass fkClass) {
         boolean backrefSet = false;
         for (final Attribute a : fkClass.getAttributes()) {
             if (a.isForeignKey() && cidsClass.getId().equals(a.getForeignKeyClass())) {
                 backrefSet = true;
                 break;
             }
         }
 
         return backrefSet;
     }
 
     @Override
     protected Sheet createSheet() {
         final Sheet sheet = Sheet.createDefault();
         final Sheet.Set main = Sheet.createPropertiesSet();
         final Sheet.Set relations = Sheet.createPropertiesSet();
         final Sheet.Set classes = Sheet.createPropertiesSet();
         final Sheet.Set rightAttributes = Sheet.createPropertiesSet();
 
         try {
             // <editor-fold defaultstate="collapsed" desc=" Create Property: CidsAttrID ">
             final Property idProp = new PropertySupport.Reflection(
                     cidsAttribute,
                     Integer.class,
                     "getId", // NOI18N
                     null);
             idProp.setName(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().idProp.name")); // NOI18N
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
             final Property nameProp = new PropertySupport(
                     "name",                                                 // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().nameProp.attrName"), // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().nameProp.nameOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getName();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setName(object.toString());
                             project.getCidsDataObjectBackend().store(
                                 getCidsAttribute());
                             fireDisplayNameChange(null, object.toString());
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store cidsAttribute name", // NOI18N
                                 e);
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().nameProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };
             // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: FieldName ">
             final Property fieldnameProp = new PropertySupport(
                     "fieldname",                                                      // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().fieldnameProp.fieldName"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().fieldnameProp.fieldNameOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getFieldName();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setFieldName(object.toString());
                             project.getCidsDataObjectBackend().store(
                                 getCidsAttribute());
                             refreshInDiagram();
                             project.getLookup().lookup(SyncManagement.class).refresh();
                         } catch (final Exception e) {
                             LOG.error("could not store attribute fieldname", // NOI18N
                                 e);
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().fieldnameProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };
             // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: DefaultValue ">
             final Property defaultValueProp = new PropertySupport(
                     "defaultValue",                                                         // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().defaultValueProp.defaultValue"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().defaultValueProp.defaultValueOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getDefaultValue();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setDefaultValue(object.toString());
                             project.getCidsDataObjectBackend().store(
                                 cidsAttribute);
                             refreshInDiagram();
                             project.getLookup().lookup(SyncManagement.class).refresh();
                         } catch (final Exception e) {
                             LOG.error("could not store attr default value", // NOI18N
                                 e);
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().defaultValueProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
 
                     @Override
                     public boolean canWrite() {
                         return cidsAttribute.isOptional();
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Position ">
             final Property posProp = new PropertySupport(
                     "pos",                                                     // NOI18N
                     Integer.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().posProp.position"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().posProp.positionOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getPosition();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setPosition((Integer)object);
                             project.getCidsDataObjectBackend().store(
                                 cidsAttribute);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attr position", e);                                          // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().posProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                  // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Substitute ">
             final Property substituteProp = new PropertySupport(
                     "substitute",                                                    // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().substituteProp.replace"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().substituteProp.replaceOfAttr"), // NOI18N
                     true,
                     !cidsAttribute.isExtensionAttr()) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.isSubstitute();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setSubstitute((Boolean)object);
                             project.getCidsDataObjectBackend().store(
                                 cidsAttribute);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attr substitute", e);                                               // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().substituteProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
 
                     @Override
                     public boolean canWrite() {
                         return getCidsAttribute().isForeignKey();
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Optional ">
             final Property optionalProp = new PropertySupport(
                     "optional",                                                      // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().optionalProp.optional"),    // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().optionalProp.optionalTooltip"), // NOI18N
                     true,
                     !cidsAttribute.isExtensionAttr()) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.isOptional();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setOptional((Boolean)object);
                             project.getCidsDataObjectBackend().store(
                                 cidsAttribute);
                             refreshInDiagram();
                             project.getLookup().lookup(SyncManagement.class).refresh();
                         } catch (final Exception e) {
                             LOG.error("could not store attr optional", e);                                               // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().optionalProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                       // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Visible ">
             final Property visibleProp = new PropertySupport(
                     "visible",                                                     // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().visibleProp.visible"),    // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().visibleProp.visibleTooltip"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.isVisible();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setVisible((Boolean)object);
                             project.getCidsDataObjectBackend().store(
                                 cidsAttribute);
                             fireDisplayNameChange(null, object.toString());
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attr visible", e);                                               // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().visibleProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                      // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Indexed ">
             final Property indexedProp = new PropertySupport(
                     "indexed",                                                     // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().indexedProp.indexed"),    // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().indexedProp.indexedTooltip"), // NOI18N
                     true,
                     !cidsAttribute.isExtensionAttr()) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.isIndexed();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setIndexed((Boolean)object);
                             project.getCidsDataObjectBackend().store(
                                 cidsAttribute);
                             fireOpenedIconChange();
                             fireIconChange();
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attr indexed", e);                                               // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().indexedProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                      // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Extension Attribtue ">
             final Property extensionProp = new PropertySupport.Reflection(
                     cidsAttribute,
                     Boolean.class,
                     "isExtensionAttr", // NOI18N
                     null);
             extensionProp.setName(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().extensionProp.name")); // NOI18N
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Description ">
            final Property<String> descriptionProp = new PropertySupport<String>(
                     "description",                                                  // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().descriptionProp.description"), // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().descriptionProp.descOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return cidsAttribute.getDescription();
                     }
 
                     @Override
                    public void setValue(final String object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                            cidsAttribute.setDescription(object.toString());
                            project.getCidsDataObjectBackend().store(cidsAttribute);
                             refreshInDiagram();
                         } catch (final Exception e) {
                            LOG.error("could not store attr description", e); // NOI18N

                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().descriptionProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" PropertyGroup: Java Classes ">
             // <editor-fold defaultstate="collapsed" desc=" Create Property: toString ">
             final JavaClassPropertyEditor toStringPropertyEditor = new JavaClassPropertyEditor(
                     project,
                     JavaClass.Type.TO_STRING);
             final Property toStringProp = new PropertySupport(
                     "toString",                                                          // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().toStringProp.toString"),        // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().toStringProp.toStringClassOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getToString();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setToString((JavaClass)object);
                             project.getCidsDataObjectBackend().store(
                                 cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store cidsClass", e);                                                   // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().toStringProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
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
                     JavaClass.Type.SIMPLE_EDITOR);
             final Property editorProp = new PropertySupport(
                     "editor",                                                        // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().editorProp.editor"),        // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().editorProp.editorClassOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getEditor();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final CidsClass old = cidsClass;
                         try {
                             cidsAttribute.setEditor((JavaClass)object);
                             // TODO: why is the class object stored here???
                             // cannot understand this because the cidsclass is
                             // not modified -.-
                             cidsClass = project.getCidsDataObjectBackend().store(cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attr editor", e);                                               // NOI18N
                             cidsClass = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().editorProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return editorPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ComplexEditor ">
             final JavaClassPropertyEditor complexEditorPropertyEditor = new JavaClassPropertyEditor(
                     project,
                     JavaClass.Type.COMPLEX_EDITOR);
             final Property complexEditorProp = new PropertySupport(
                     "complexeditor",                                                          // NOI18N
                     JavaClass.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().complexEditorProp.complexEditor"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().complexEditorProp.complexEditorOfAttr"), // NOI18N
                     true,
                     true) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getComplexEditor();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final CidsClass old = cidsClass;
                         try {
                             cidsAttribute.setComplexEditor((JavaClass)object);
                             // TODO: why is the class object stored here???
                             // cannot understand this because the cidsclass is
                             // not modified -.-
                             cidsClass = project.getCidsDataObjectBackend().store(cidsClass);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attr complex editor", // NOI18N
                                 e);
                             cidsClass = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().complexEditorProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
 
                     @Override
                     public PropertyEditor getPropertyEditor() {
                         return complexEditorPropertyEditor;
                     }
                 }; // </editor-fold>
 
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" PropertyGroup: References ">
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Type ">
             final Property typeProp = new PropertySupport(
                     "type",                                                 // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().typeProp.type"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().typeProp.typeOfAttr"), // NOI18N
                     true,
                     false) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getType().getName();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         // read-only
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ForeignKeyReference ">
             final Property foreignKeyReferencesToProp = new PropertySupport(
                     "foreignKeyReferencesTo",                                                            // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().foreignKeyReferencesToProp.foreignKeyTable"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().foreignKeyReferencesToProp.foreignKeyTableOfAttr"), // NOI18N
                     true,
                     false) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         final Integer fkClass = cidsAttribute.getForeignKeyClass();
                         if (fkClass != null) {
                             // to support the 1:n facilities we use the absolute value of the fk
                             final CidsClass c = project.getCidsDataObjectBackend()
                                         .getEntity(CidsClass.class, Math.abs(fkClass));
                             if (c != null) {
                                 final StringBuilder sb = new StringBuilder(c.getName());
 
                                 if (fkClass < 0) {
                                     sb.append(" [1:N]");                                                                         // NOI18N
                                     if (!isOneToManyBackrefPresent(c)) {
                                         sb.append(NbBundle.getMessage(
                                                 CidsAttributeNode.class,
                                                 "CidsAttributeNode.createSheet().foreignKeyReferencesToProp.refBrokenPostfix")); // NOI18N
                                     }
                                 }
 
                                 return sb.toString();
                             }
                         }
                         return null;
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         // read-only
                     }
                 }; // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ArrayKey ">
             final Property arrayKeyProp = new PropertySupport(
                     "arraykey",                                                     // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().arrayKeyProp.arraykey"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().arrayKeyProp.arrayKeyOfAttr"), // NOI18N
                     true,
                     !cidsAttribute.isExtensionAttr()) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.getArrayKey();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setArrayKey((String)object);
                             project.getCidsDataObjectBackend().store(cidsAttribute);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attribute", e);                                                   // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().arrayKeyProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                       // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: ForeignKey ">
             final Property foreignKeyProp = new PropertySupport(
                     "foreignkey",                                                       // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().foreignKeyProp.foreignKey"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().foreignKeyProp.attrIsForeignKey"), // NOI18N
                     true,
                     !cidsAttribute.isExtensionAttr()) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.isForeignKey();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setForeignKey((Boolean)object);
                             project.getCidsDataObjectBackend().store(cidsAttribute);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attribute", e);                                                     // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().foreignKeyProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                         // </editor-fold>
 
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Array ">
             final Property arrayProp = new PropertySupport(
                     "array",                                                  // NOI18N
                     Boolean.class,
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().arrayProp.array"),   // NOI18N
                     NbBundle.getMessage(
                         CidsAttributeNode.class,
                         "CidsAttributeNode.createSheet().arrayProp.attrIsArray"), // NOI18N
                     true,
                     !cidsAttribute.isExtensionAttr()) {
 
                     @Override
                     public Object getValue() throws IllegalAccessException, InvocationTargetException {
                         return cidsAttribute.isArray();
                     }
 
                     @Override
                     public void setValue(final Object object) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final Attribute old = cidsAttribute;
                         try {
                             cidsAttribute.setArray((Boolean)object);
                             project.getCidsDataObjectBackend().store(cidsAttribute);
                             refreshInDiagram();
                         } catch (final Exception e) {
                             LOG.error("could not store attribute", e);                                                // NOI18N
                             cidsAttribute = old;
                             ErrorUtils.showErrorMessage(
                                 NbBundle.getMessage(
                                     CidsAttributeNode.class,
                                     "CidsAttributeNode.createSheet().arrayProp.setValue(Object).ErrorUtils.message"), // NOI18N
                                 e);
                         }
                     }
                 };                                                                                                    // </editor-fold>
 
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Rights ">
             final List<AttributePermission> allAttributePermissions = new ArrayList<AttributePermission>(
                     cidsAttribute.getAttributePermissions());
             for (final AttributePermission perm : allAttributePermissions) {
                 final UserGroup ug = perm.getUserGroup();
                 final String name;
                 if (ProjectUtils.isRemoteGroup(ug, project)) {
                     name = ug.getName() + "@" + ug.getDomain(); // NOI18N
                 } else {
                     name = ug.getName();
                 }
                 final Property attributePermissionProp = new PropertySupport(
                         "attributePerm"                         // NOI18N
                                 + perm.getId(),
                         String.class,
                         name,
                         "",                                     // NOI18N
                         true,
                         false) {
 
                         @Override
                         public Object getValue() throws IllegalAccessException, InvocationTargetException {
                             final Permission p = perm.getPermission();
                             String s = permResolve.getPermString(cidsAttribute, p).getPermissionString();
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
                 rightAttributes.put(attributePermissionProp);
             } // </editor-fold>
 
             main.setName("properties");        // NOI18N
             relations.setName("relations");    // NOI18N
             classes.setName("java");           // NOI18N
             rightAttributes.setName("rights"); // NOI18N
 
             main.setDisplayName(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().main.displayName"));            // NOI18N
             relations.setDisplayName(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().relations.displayName"));       // NOI18N
             classes.setDisplayName(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().classes.displayName"));         // NOI18N
             rightAttributes.setDisplayName(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().rightAttributes.displayName")); // NOI18N
 
             main.put(idProp);
             main.put(nameProp);
             main.put(fieldnameProp);
             main.put(defaultValueProp);
             main.put(posProp);
             main.put(descriptionProp);
             main.put(substituteProp);
             main.put(optionalProp);
             main.put(visibleProp);
             main.put(indexedProp);
             main.put(extensionProp);
 
             classes.put(toStringProp);
             classes.put(editorProp);
             classes.put(complexEditorProp);
 
             relations.put(foreignKeyProp);
             relations.put(foreignKeyReferencesToProp);
             relations.put(typeProp);
             relations.put(arrayProp);
             relations.put(arrayKeyProp);
 
             sheet.put(main);
             sheet.put(classes);
             sheet.put(relations);
 
             if (rightAttributes.getProperties().length > 0) {
                 sheet.put(rightAttributes);
             }
         } catch (final Exception ex) {
             LOG.error("could not create property sheet", ex);              // NOI18N
             ErrorUtils.showErrorMessage(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.createSheet().ErrorUtils.message"), // NOI18N
                 ex);
         }
         return sheet;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void refreshInDiagram() {
         final Node n = getParentNode();
         if (n instanceof CidsClassNode) {
             ((CidsClassNode)n).refreshInDiagram();
         }
     }
 
     @Override
     public Action[] getActions(final boolean b) {
         return new Action[] {
                 CallableSystemAction.get(AttributePermissionWizardAction.class),
                 null,
                 CallableSystemAction.get(DeleteAction.class)
             };
     }
 
     @Override
     public boolean canDestroy() {
         return true;
     }
 
     @Override
     public void destroy() {
         try {
             cidsClass.getAttributes().remove(cidsAttribute);
             project.getCidsDataObjectBackend().store(cidsClass);
             cidsAttribute = null;
 
             final Node p = getParentNode();
             if (p == null) {
                 throw new IllegalStateException("parent is not set: " + p);                              // NOI18N
             } else {
                 final Refreshable refreshable = p.getCookie(Refreshable.class);
                 if (refreshable == null) {
                     throw new IllegalStateException("parent does not provide Refreshable cookie: " + p); // NOI18N
                 } else {
                     refreshable.refresh();
                 }
             }
         } catch (final Exception e) {
             LOG.error("error during deletion", e);                                                       // NOI18N
             ErrorUtils.showErrorMessage(NbBundle.getMessage(
                     CidsAttributeNode.class,
                     "CidsAttributeNode.destroy().ErrorUtils.message"),                                   // NOI18N
                 e);
         }
         // we don't need to refresh the class management since it won't be affected by this change
         project.getLookup().lookup(SyncManagement.class).refresh();
     }
 
     @Override
     public void setCidsAttribute(final Attribute cidsAttribute) {
         this.cidsAttribute = cidsAttribute;
     }
 
     @Override
     public Attribute getCidsAttribute() {
         return cidsAttribute;
     }
 
     @Override
     public CidsClass getCidsClass() {
         return cidsClass;
     }
 }
