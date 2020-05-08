 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.jpa.entity.cidsclass;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 import java.io.Serializable;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import de.cismet.cids.jpa.entity.common.CommonEntity;
 import de.cismet.cids.jpa.entity.common.PermissionAwareEntity;
 import de.cismet.cids.jpa.entity.permission.AbstractPermission;
 import de.cismet.cids.jpa.entity.permission.AttributePermission;
 import de.cismet.cids.jpa.entity.permission.Policy;
 
 import de.cismet.tools.Equals;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 @Entity
 @Table(name = "cs_attr")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class Attribute extends CommonEntity implements Serializable, PermissionAwareEntity {
 
     //~ Instance fields --------------------------------------------------------
 
     @Id
     @SequenceGenerator(
         name = "cs_attr_sequence",
         sequenceName = "cs_attr_sequence",
         allocationSize = 1
     )
     @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "cs_attr_sequence"
     )
     @Column(name = "id")
     private Integer id;
 
     @ManyToOne(
         optional = false,
         fetch = FetchType.EAGER
     )
     @JoinColumn(
         name = "class_id",
         nullable = false
     )
     @Fetch(FetchMode.SELECT)
     private CidsClass cidsClass;
 
     @ManyToOne(
         optional = false,
         fetch = FetchType.EAGER
     )
     @JoinColumn(
         name = "type_id",
         nullable = false
     )
     @Fetch(FetchMode.SELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private Type type;
 
     @Column(name = "name")
     private String name;
 
     @Column(name = "field_name")
     private String fieldName;
 
     @Column(name = "foreign_key")
     private Boolean foreignKey;
 
     @Column(name = "substitute")
     private Boolean substitute;
 
     @Column(name = "foreign_key_references_to")
     private Integer foreignKeyClass;
 
     @Column(name = "descr")
     private String description;
 
     @Column(name = "visible")
     private Boolean visible;
 
     @Column(name = "indexed")
     private Boolean indexed;
 
     @Column(name = "isarray")
     private Boolean array;
 
     @Column(name = "array_key")
     private String arrayKey;
 
     @ManyToOne(
         optional = true,
         fetch = FetchType.EAGER
     )
     @JoinColumn(
         name = "editor",
         nullable = true
     )
     @Fetch(FetchMode.SELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private JavaClass editor;
 
     @ManyToOne(
         optional = true,
         fetch = FetchType.EAGER
     )
     @JoinColumn(
         name = "tostring",
         nullable = true
     )
     @Fetch(FetchMode.SELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private JavaClass toString;
 
     @ManyToOne(
         optional = true,
         fetch = FetchType.EAGER
     )
     @JoinColumn(
         name = "complex_editor",
         nullable = true
     )
     @Fetch(FetchMode.SELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private JavaClass complexEditor;
 
     @Column(name = "optional")
     private Boolean optional;
 
     @Column(name = "default_value")
     private String defaultValue;
 
     /** depricated private JavaClass fromString;. */
 
     @Column(name = "pos")
     private Integer position;
 
     @Column(name = "precision")
     private Integer precision;
 
     @Column(name = "scale")
     private Integer scale;
 
     @OneToMany(
         cascade = CascadeType.ALL,
         fetch = FetchType.EAGER,
         mappedBy = "attribute",
         orphanRemoval = true
     )
     @Fetch(FetchMode.SELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private Set<AttributePermission> attributePermissions;
 
     @Column(name = "extension_attr")
     private Boolean extensionAttr;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new Attribute object.
      */
     public Attribute() {
         attributePermissions = new HashSet<AttributePermission>();
         // defaults initialisation to prevent not-null constraint errors
         visible = true;
         foreignKey = false;
         substitute = false;
         indexed = false;
         array = false;
         optional = true;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Integer getId() {
         return id;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setId(final Integer id) {
         this.id = id;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public CidsClass getCidsClass() {
         return cidsClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsClass  DOCUMENT ME!
      */
     public void setCidsClass(final CidsClass cidsClass) {
         this.cidsClass = cidsClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Type getType() {
         return type;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  type  DOCUMENT ME!
      */
     public void setType(final Type type) {
         this.type = type;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getName() {
         return name;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  name  DOCUMENT ME!
      */
     public void setName(final String name) {
         this.name = name;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getFieldName() {
         return fieldName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  fieldName  DOCUMENT ME!
      */
     public void setFieldName(final String fieldName) {
         this.fieldName = fieldName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isForeignKey() {
         return foreignKey;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  foreignKey  DOCUMENT ME!
      */
     public void setForeignKey(final Boolean foreignKey) {
         this.foreignKey = foreignKey;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isSubstitute() {
         return substitute;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  substitute  DOCUMENT ME!
      */
     public void setSubstitute(final Boolean substitute) {
         this.substitute = substitute;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Integer getForeignKeyClass() {
         return foreignKeyClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  foreignKeyClass  DOCUMENT ME!
      */
     public void setForeignKeyClass(final Integer foreignKeyClass) {
         this.foreignKeyClass = foreignKeyClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  description  DOCUMENT ME!
      */
     public void setDescription(final String description) {
         this.description = description;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isVisible() {
         return visible;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  visible  DOCUMENT ME!
      */
     public void setVisible(final Boolean visible) {
         this.visible = visible;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isIndexed() {
         return indexed;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  indexed  DOCUMENT ME!
      */
     public void setIndexed(final Boolean indexed) {
         this.indexed = indexed;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isArray() {
         return array;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  array  DOCUMENT ME!
      */
     public void setArray(final Boolean array) {
         this.array = array;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getArrayKey() {
         return arrayKey;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  arrayKey  DOCUMENT ME!
      */
     public void setArrayKey(final String arrayKey) {
         this.arrayKey = arrayKey;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JavaClass getEditor() {
         return editor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  editor  DOCUMENT ME!
      */
     public void setEditor(final JavaClass editor) {
         this.editor = editor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JavaClass getToString() {
         return toString;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  toString  DOCUMENT ME!
      */
     public void setToString(final JavaClass toString) {
         this.toString = toString;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JavaClass getComplexEditor() {
         return complexEditor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  complexEditor  DOCUMENT ME!
      */
     public void setComplexEditor(final JavaClass complexEditor) {
         this.complexEditor = complexEditor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isOptional() {
         return optional;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  optional  DOCUMENT ME!
      */
     public void setOptional(final Boolean optional) {
         this.optional = optional;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDefaultValue() {
         return defaultValue;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  defaultValue  DOCUMENT ME!
      */
     public void setDefaultValue(final String defaultValue) {
         this.defaultValue = defaultValue;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Integer getPosition() {
         return position;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  position  DOCUMENT ME!
      */
     public void setPosition(final Integer position) {
         this.position = position;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Integer getPrecision() {
         return precision;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  precision  DOCUMENT ME!
      */
     public void setPrecision(final Integer precision) {
         this.precision = precision;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Integer getScale() {
         return scale;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  scale  DOCUMENT ME!
      */
     public void setScale(final Integer scale) {
         this.scale = scale;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Set<AttributePermission> getAttributePermissions() {
         return attributePermissions;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  attributePermissions  DOCUMENT ME!
      */
     public void setAttributePermissions(final Set<AttributePermission> attributePermissions) {
         this.attributePermissions = attributePermissions;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return getName() + "(" + getId() + ")"; // NOI18N
     }
 
     @Override
     public Set<? extends AbstractPermission> getPermissions() {
         return getAttributePermissions();
     }
 
     @Override
     public Policy getPolicy() {
         return getCidsClass().getAttributePolicy();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Boolean isExtensionAttr() {
         return extensionAttr;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  extensionAttr  DOCUMENT ME!
      */
     public void setExtensionAttr(final Boolean extensionAttr) {
         this.extensionAttr = extensionAttr;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (!super.equals(o)) {
             return false;
         }
 
         // we ignore two operations for equals comparison because otherwise it would cause a StackOverFlow
         return Equals.beanDeepEqual(this, o, "getCidsClass", "getType");
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = (43 * hash) + ((this.getId() != null) ? this.getId().hashCode() : 0);
         // NOTE: we cannot use the cids class' own hashcode implementation, because this causes hashCode invokation
         // cycles which in turn will cause an exception "org.hibernate.LazyInitializationException: illegal access to
         // loading collection"
         final CidsClass c = this.getCidsClass();
         hash = (43 * hash) + ((c != null) ? ((c.getId() != null) ? c.getId().hashCode() : 0) : 0);
         hash = (43 * hash) + ((this.getType() != null) ? this.getType().hashCode() : 0);
         hash = (43 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
         hash = (43 * hash) + ((this.getFieldName() != null) ? this.getFieldName().hashCode() : 0);
         hash = (43 * hash) + ((this.isForeignKey() != null) ? this.isForeignKey().hashCode() : 0);
         hash = (43 * hash) + ((this.isSubstitute() != null) ? this.isSubstitute().hashCode() : 0);
         hash = (43 * hash) + ((this.getForeignKeyClass() != null) ? this.getForeignKeyClass().hashCode() : 0);
         hash = (43 * hash) + ((this.getDescription() != null) ? this.getDescription().hashCode() : 0);
         hash = (43 * hash) + ((this.isVisible() != null) ? this.isVisible().hashCode() : 0);
         hash = (43 * hash) + ((this.isIndexed() != null) ? this.isIndexed().hashCode() : 0);
         hash = (43 * hash) + ((this.isArray() != null) ? this.isArray().hashCode() : 0);
         hash = (43 * hash) + ((this.getArrayKey() != null) ? this.getArrayKey().hashCode() : 0);
         hash = (43 * hash) + ((this.getEditor() != null) ? this.getEditor().hashCode() : 0);
         hash = (43 * hash) + ((this.getToString() != null) ? this.getToString().hashCode() : 0);
         hash = (43 * hash) + ((this.getComplexEditor() != null) ? this.getComplexEditor().hashCode() : 0);
         hash = (43 * hash) + ((this.isOptional() != null) ? this.isOptional().hashCode() : 0);
         hash = (43 * hash) + ((this.getDefaultValue() != null) ? this.getDefaultValue().hashCode() : 0);
         hash = (43 * hash) + ((this.getPosition() != null) ? this.getPosition().hashCode() : 0);
         hash = (43 * hash) + ((this.getPrecision() != null) ? this.getPrecision().hashCode() : 0);
         hash = (43 * hash) + ((this.getScale() != null) ? this.getScale().hashCode() : 0);
         hash = (43 * hash) + ((this.getAttributePermissions() != null) ? this.getAttributePermissions().hashCode() : 0);
         hash = (43 * hash) + ((this.isExtensionAttr() != null) ? this.isExtensionAttr().hashCode() : 0);
 
         return hash;
     }
 }
