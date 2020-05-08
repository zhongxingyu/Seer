 /**
  *         PlasmaSDO™ License
  * 
  * This is a community release of PlasmaSDO™, a dual-license 
  * Service Data Object (SDO) 2.1 implementation. 
  * This particular copy of the software is released under the 
  * version 2 of the GNU General Public License. PlasmaSDO™ was developed by 
  * TerraMeta Software, Inc.
  * 
  * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
  * 
  * General License information can be found below.
  * 
  * This distribution may include materials developed by third
  * parties. For license and attribution notices for these
  * materials, please refer to the documentation that accompanies
  * this distribution (see the "Licenses for Third-Party Components"
  * appendix) or view the online documentation at 
  * <http://plasma-sdo.org/licenses/>.
  *  
  */
 package org.plasma.sdo.core;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.modeldriven.fuml.repository.Class_;
 import org.plasma.config.PlasmaConfig;
 import org.plasma.config.PropertyBinding;
 import org.plasma.config.TypeBinding;
 import org.plasma.sdo.Alias;
 import org.plasma.sdo.Concurrent;
 import org.plasma.sdo.DataFlavor;
 import org.plasma.sdo.DataType;
 import org.plasma.sdo.Derivation;
 import org.plasma.sdo.EnumerationConstraint;
 import org.plasma.sdo.Key;
 import org.plasma.sdo.PlasmaDataObjectException;
 import org.plasma.sdo.PlasmaProperty;
 import org.plasma.sdo.PlasmaType;
 import org.plasma.sdo.Sort;
 import org.plasma.sdo.Temporal;
 import org.plasma.sdo.UniqueConstraint;
 import org.plasma.sdo.ValueConstraint;
 import org.plasma.sdo.ValueSetConstraint;
 import org.plasma.sdo.XmlProperty;
 import org.plasma.sdo.helper.PlasmaTypeHelper;
 import org.plasma.sdo.profile.ConcurrencyType;
 import org.plasma.sdo.profile.ConcurrentDataFlavor;
 import org.plasma.sdo.profile.KeyType;
 import org.plasma.sdo.profile.XmlNodeType;
 import org.plasma.sdo.repository.Comment;
 import org.plasma.sdo.repository.Enumeration;
 
 import commonj.sdo.DataObject;
 import commonj.sdo.Property;
 import commonj.sdo.Type;
 import fUML.Syntax.Classes.Kernel.VisibilityKind;
 
 /**
  * A representation of a Property in the {@link Type type} of a
  * {@link DataObject data object}.
  */
 public class CoreProperty implements PlasmaProperty {
 
     private CoreType type;
     private org.plasma.sdo.repository.Property property;
     /** name attribute to support instance properties w/no repository element */
     private String name;
     private QName qname;
     private int qnameHashCode;
     // cached names, used/valued conditionally based on binding customizations
     // FIXME: cache these as instance properties
     private String physicalName;
     private String localName; 
 
     private CoreType containingType;
     
     // TODO: extend PlasmaDataObject ??
     private Map<Property, Object> instanceProperties;
     
     
     @SuppressWarnings("unused")
     private CoreProperty() {
     }
 
     private CoreProperty(String name, CoreType type, CoreType containingType) {
     	this.type = type;
         if (this.type == null)
         	throw new IllegalArgumentException("expected type argument");
         if (!this.type.isDataType())
             throw new IllegalArgumentException("expected datatype not, "
                     + this.type.getName());  
         this.name = name;
         if (this.name == null)
         	throw new IllegalArgumentException("expected name argument");
         if (containingType != null) {
 	        PropertyBinding binding = PlasmaConfig.getInstance().findPropertyBinding(containingType.getURI(), 
 	        		containingType.getName(), name);
 	        if (binding != null) {
 	        	if (binding.getLogicalName() != null && binding.getLogicalName().trim().length() > 0)
 	        	    this.name = binding.getLogicalName().trim();
 	        	if (binding.getPhysicalName() != null && binding.getPhysicalName().trim().length() > 0)
 	        	    this.physicalName = binding.getPhysicalName().trim();
 	        	if (binding.getLocalName() != null && binding.getLocalName().trim().length() > 0)
 	        	    this.localName = binding.getLocalName().trim();
 	        }
 	        this.containingType = containingType;
 	        this.qname = new QName(this.containingType.getName(), this.name);
 	        this.qnameHashCode = this.qname.hashCode();
         }
         else {
 	        this.qname = new QName(this.name);        	
 	        this.qnameHashCode = this.qname.hashCode();
         }
     }
 
     public CoreProperty(CoreType type, 
     		org.plasma.sdo.repository.Property property, 
     		CoreType containingType) {
         this.type = type;
         if (this.type == null)
         	throw new IllegalArgumentException("expected type argument");
         this.property = property;
         this.name = this.property.getName();
         if (this.name == null || this.name.trim().length() == 0) {
             if (this.type.isDataType())
                 throw new IllegalArgumentException("expected reference (non-datatype) property type not, "
                     + this.type.getName());  
         	this.name = getDerivedName(this.type); 
         }
         PropertyBinding binding = PlasmaConfig.getInstance().findPropertyBinding(containingType.getURI(), 
         		containingType.getName(), name);
         if (binding != null) {
         	if (binding.getLogicalName() != null && binding.getLogicalName().trim().length() > 0)
         	    this.name = binding.getLogicalName().trim();
         	if (binding.getPhysicalName() != null && binding.getPhysicalName().trim().length() > 0)
         	    this.physicalName = binding.getPhysicalName().trim();
         	if (binding.getLocalName() != null && binding.getLocalName().trim().length() > 0)
         	    this.localName = binding.getLocalName().trim();
         }
         this.containingType = containingType;
         this.qname = new QName(this.containingType.getName(), this.name);
         this.qnameHashCode = this.qname.hashCode();
        
         // add instance properties
         instanceProperties = new HashMap<Property, Object>();
         
         // descr
         if (this.property.getComments() != null) {
             instanceProperties.put(CoreProperty.INSTANCE_PROPERTY_OBJECT_DESCRIPTION, 
             		this.property.getComments());            
         }        
         
         // visibility
         VisibilityKind visibility = VisibilityKind.public_;
         if (this.property.getVisibility() != null)
         	visibility = this.property.getVisibility();
         instanceProperties.put(CoreProperty.INSTANCE_PROPERTY_OBJECT_VISIBILITY, 
         		visibility);  
         
         // can be null for some reference properties
         String physicalName = this.property.findPhysicalName();
         if (physicalName != null)
             this.instanceProperties.put(CoreProperty.INSTANCE_PROPERTY_STRING_PHYSICAL_NAME, 
                 physicalName);                     
        
         
         if (this.property.getIsUnique())	
             instanceProperties.put(INSTANCE_PROPERTY_BOOLEAN_ISUNIQUE, new Boolean(true));
 
         if (this.property.isDataType()) {
         
         	Long max = this.property.getMaxLength();
         	if (max != null)
                 instanceProperties.put(INSTANCE_PROPERTY_INT_MAXLENGTH, max);
                     
         }
     }
     
     public int hashCode() {
     	return this.qnameHashCode;
     }
     
     public boolean equals(Object other) {
     	if (other != null) {
     		CoreProperty otherType = (CoreProperty)other;
     		return this.qnameHashCode == otherType.hashCode();
     	}
     	return false;	
     }
     
     private String getDerivedName(PlasmaType type) {
     	return getDerivedName(type.getName());  	
     }
     
     private String getDerivedName(String typeName) {
     	return typeName.substring(0, 1).toLowerCase() 
 	        + typeName.substring(1);     	
     }    
     
     public static PlasmaProperty createInstanceProperty(String name, DataType dataType) {
         
         CoreType type = (CoreType)PlasmaTypeHelper.INSTANCE.getType(
                 PlasmaConfig.getInstance().getSDO().getDefaultNamespace().getUri(), 
                 dataType.name());
         
         PlasmaProperty property = new CoreProperty(
                 name, 
                 type,
                 null); // instance property for a property has no containing type
         
         return property;
     }
 
     /**
      * Returns the name of the Property.
      * @return the Property name.
      */
     public String getName() {
         return this.name;
     }
     
 	public DataFlavor getDataFlavor() {
 		if (!this.getType().isDataType())
 			throw new UnsupportedOperationException("property "
 				+ this.getContainingType().getURI() + "#" 
 				+ this.getContainingType().getName() + "." + this.getName()
 				+ " is not a data type property");
 		DataType dataType = DataType.valueOf(this.getType().getName());
 		return DataFlavor.fromDataType(dataType);
 	}
 	    
 	@SuppressWarnings("unchecked")
 	public List<Comment> getDescription() {
 		return (List<Comment>)this.get(
 	    		CoreProperty.INSTANCE_PROPERTY_OBJECT_DESCRIPTION);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public String getDescriptionText() {
 		StringBuilder buf = new StringBuilder();
 		List<Comment> list = (List<Comment>)this.get(
 	    		PlasmaProperty.INSTANCE_PROPERTY_OBJECT_DESCRIPTION);
 	    if (list != null)
 		    for (Comment comment : list)
 		    	buf.append(comment.getBody());
 		return buf.toString();
 	}
 
 	public String getPhysicalName() {
     	if (this.physicalName != null) {
     		return this.physicalName;
     	}
     	else {
 		    Alias alias = this.property.findAlias();
 		    if (alias != null && alias.getPhysicalName() != null)
 			    return alias.getPhysicalName();
     	}
 		return null;
 	}
     /**
      * Returns the logical name of this Property as a byte array which may be cached
      * or lazily cached on demand. 
      * <p>
      * Helps support {@link org.plasma.sdo.access.DataAccessService Data Access Services} for sparse, 
      * distributed "cloud" data stores typically storing lexicographically 
      * ordered row and column keys as uninterpreted arrays of bytes. Fast dynamic
      * construction of such keys is important as such services may necessarily construct
      * unique composite keys based in part on qualified or unqualified logical or physical
      * type names. 
      * </p>
      * @return the logical name alias as a byte array.
      */
     public byte[] getNameBytes() {
     	byte[] result = (byte[])this.instanceProperties.get(PlasmaProperty.INSTANCE_PROPERTY_BYTES_NAME_BYTES);
         if (result == null) {
         	result = this.getName().getBytes(
         		Charset.forName( CoreConstants.UTF8_ENCODING ) );
 			this.instanceProperties.put(
 				PlasmaProperty.INSTANCE_PROPERTY_BYTES_NAME_BYTES, result);
         }
         return result;
     }
 	
     /**
      * Returns the physical name alias of this Property as a byte array which may be cached
      * or lazily cached on demand, or null if no physical alias name exists. 
      * <p>
      * Helps support {@link org.plasma.sdo.access.DataAccessService Data Access Services} for sparse, 
      * distributed "cloud" data stores typically storing lexicographically 
      * ordered row and column keys as uninterpreted arrays of bytes. Fast dynamic
      * construction of such keys is important as such services may necessarily construct
      * unique composite keys based in part on qualified or unqualified logical or physical
      * type names. 
      * </p>
      * @return the physical name alias of this Property as a byte array, or null if no physical alias name exists.
      */
     public byte[] getPhysicalNameBytes() {
     	byte[] result = (byte[])this.instanceProperties.get(PlasmaProperty.INSTANCE_PROPERTY_BYTES_PHYSICAL_NAME_BYTES);
         if (result == null) {
         	String name = this.getPhysicalName();
         	if (name != null) {
 	        	result = name.getBytes(
 	        		Charset.forName( CoreConstants.UTF8_ENCODING ) );
 				this.instanceProperties.put(
 					PlasmaProperty.INSTANCE_PROPERTY_BYTES_PHYSICAL_NAME_BYTES, result);
         	}
         }
         return result;
     }
     
     public String getLocalName() {
     	if (this.localName != null) {
     		return this.localName;
     	}
     	else {
 		    Alias alias = this.property.findAlias();
 		    if (alias != null && alias.getLocalName() != null)
 			    return alias.getLocalName();
     	}
     
     	return this.name;
     }
 	
     /**
      * Returns the local name of this Property as a byte array which may be cached
      * or lazily cached on demand. 
      * <p>
      * Helps support {@link org.plasma.sdo.access.DataAccessService Data Access Services} for sparse, 
      * distributed "cloud" data stores typically storing lexicographically 
      * ordered row and column keys as uninterpreted arrays of bytes. Fast dynamic
      * construction of such keys is important as such services may necessarily construct
      * unique composite keys based in part on qualified or unqualified logical or physical
      * type names. 
      * </p>
      * @return the local name of this Property as a byte array
      */
     public byte[] getLocalNameBytes() {
     	byte[] result = (byte[])this.instanceProperties.get(PlasmaProperty.INSTANCE_PROPERTY_BYTES_LOCAL_NAME_BYTES);
         if (result == null) {
         	result = this.getLocalName().getBytes(
 	        	Charset.forName( CoreConstants.UTF8_ENCODING ) );
 			this.instanceProperties.put(
 				PlasmaProperty.INSTANCE_PROPERTY_BYTES_LOCAL_NAME_BYTES, result);
         }
         return result;
     }
     
     public Alias getAlias() {
     	return this.property.findAlias();
     }   
 
     public Key getKey() {
 		return this.property.findKey();
 	}
     
     public boolean isKey() {
     	Key key = this.property.findKey();
     	if (key == null)
     		return false;
     	else
     	    return true;
     }
     
     public boolean isKey(KeyType keyType) {
     	Key key = this.property.findKey();
     	if (key == null)
     		return false;
     	return key.getType().ordinal() == keyType.ordinal();
     }
     
 	@Override
 	public PlasmaProperty getKeySupplier() {
    	Key key = this.property.findKey();
    	if (key == null) {
            org.modeldriven.fuml.repository.Property supplierProperty = this.property.findKeySupplier();
            if (supplierProperty != null) {
            	return lookup(supplierProperty);
            }
            else
            	return null;
		}
		return null;
 	}
     
     public Concurrent getConcurrent() {
 		return this.property.findConcurrent();
 	}
     
     public boolean isConcurrent() {
     	Concurrent concurrent = this.property.findConcurrent();
     	return concurrent != null;
     }
     
     public boolean isConcurrent(ConcurrencyType type, ConcurrentDataFlavor dataFlavor) {
     	Concurrent concurrent = this.property.findConcurrent();
     	if (concurrent == null)
     		return false;    	
     	return concurrent.getType().ordinal() == type.ordinal() &&
     	    concurrent.getDataFlavor().ordinal() == dataFlavor.ordinal();
     }
        
     public Temporal getTemporal() {
         if (!property.isDataType())
             throw new IllegalArgumentException("property " 
                 + this.getType().getURI() + "#" + this.getType().getName() + "." + 
                 this.getName() + " is not a datatype property");        
 		return this.property.findTemporal();
 	}
 
     public EnumerationConstraint getEnumerationConstraint() {
         if (!property.isDataType())
             throw new IllegalArgumentException("property " 
                 + this.getType().getURI() + "#" + this.getType().getName() + "." + 
                 this.getName() + " is not a datatype property");        
 		return this.property.findEnumerationConstraint();
 	}
     
 
     public ValueSetConstraint getValueSetConstraint() {
         if (!property.isDataType())
             throw new IllegalArgumentException("property " 
                 + this.getType().getURI() + "#" + this.getType().getName() + "." + 
                 this.getName() + " is not a datatype property");        
 		return this.property.findValueSetConstraint();
 	}
     
     public ValueConstraint getValueConstraint() {
         if (!property.isDataType())
             throw new IllegalArgumentException("property " 
                 + this.getType().getURI() + "#" + this.getType().getName() + "." + 
                 this.getName() + " is not a datatype property");        
 		return this.property.findValueConstraint();
 	}
     
     public Sort getSort() {
 		return this.property.findSort();
 	}
     
     public UniqueConstraint getUniqueConstraint() {
 		return this.property.findUniqueConstraint();
 	}    
     
     public XmlProperty getXmlProperty() {
         if (!property.isDataType())
             throw new IllegalArgumentException("property " 
                 + this.getType().getURI() + "#" + this.getType().getName() + "." + 
                 this.getName() + " is not a datatype property");        
 		return this.property.findXmlProperty();
     }
 
     public Enumeration getRestriction() {
         if (!property.isDataType())
             throw new IllegalArgumentException("property " 
                 + this.getType().getURI() + "#" + this.getType().getName() + "." + 
                 this.getName() + " is not a datatype property");        
 		return this.property.getRestriction();
 	}
 	
 	/**
 	 * Returns the maximum length allowed this property, or -1 if no
 	 * maximum length is defined. 
 	 * @return the maximum length or -1 if no
 	 * maximum length is defined
 	 */
 	public long getMaxLength() {
 		Long maxlen = (Long)this.get(
 				CoreProperty.INSTANCE_PROPERTY_INT_MAXLENGTH);
 		if (maxlen != null)
 		    return maxlen.longValue();
 		else
 			return -1;
 	}
 		
 	public boolean isXMLAttribute() {
 		
 		if (!this.getType().isDataType())
 			return false;
 		
 		XmlProperty xmlPropery = getXmlProperty();
 		if (xmlPropery != null)
 			return xmlPropery.getNodeType().ordinal() == XmlNodeType.attribute.ordinal();
 		
     	DataType datatype = DataType.valueOf(this.getType().getName());
     	
         switch (datatype) {
         case Boolean:   
         case Byte:      
         case Character: 
         case DateTime:  
         case Date:      
         case Day:       
         case Decimal:   
         case Duration:  
         case Float:     
         case Double:    
         case Int:       
         case Integer:   
         case Long:      
         case Month:     
         case MonthDay:  
         case Short:     
         case Time:      
         case URI:       
         case Year:      
         case YearMonth: 
         case YearMonthDay: 
         	return true;
         case Bytes:     
         case Object: 
         	return false;
         case String:    
         case Strings: 
         	if (this.getEnumerationConstraint() != null)
         	    return true;
         	if (this.getValueConstraint() != null)
         	    return true;
         	else if (this.getMaxLength() == -1 || this.getMaxLength() > 20)
         		return false;
         	else 
         		return false;
         default:
             throw new IllegalArgumentException("unknown datatype, "
                     + datatype.toString());
         }
     }
 
     /**
      * Returns the value of the specified instance property of this Property.
      * @param property one of the properties returned by {@link #getInstanceProperties()}.
      * @return the value of the specified property.
      * @see DataObject#get(Property)
      * 
      * 3.6.7 Property Instance Properties
      * Property instances can themselves include open content, that is, extended metadata in the
      * form of instance properties. The list of such extensions is available by calling
      * Property.getInstanceProperties(). The values of these properties are available by calling
      * Property.get(Property). For more details, see Section 3.5.6.
      * 
      */
     public Object get(Property property) {
         Object result = null;
         if (instanceProperties != null) {
             result = instanceProperties.get(property);
             if (result == null) // hashed by name ?
                 result = instanceProperties.get(property.getName());
         }
         return result;
     }
 
     public List getInstanceProperties() {
         
         List<Property> result = new ArrayList<Property>();
         if (instanceProperties != null) {
             Iterator<Property> iter = instanceProperties.keySet().iterator();
             while (iter.hasNext())
                 result.add(iter.next());
         }
 
         return result;
     }
     
     /**
      * Returns a list of alias names for this Property.
      * @return a list of alias names for this Property.
      */
     public List<String> getAliasNames() {
         List<String> aliasList = new ArrayList<String>();
         Alias alias = this.property.findAlias();
         if (alias != null) {
         	if (alias.getPhysicalName() != null)
                 aliasList.add(alias.getPhysicalName()); 
         	if (alias.getLocalName() != null)
                 aliasList.add(alias.getLocalName()); 
         	if (alias.getBusinessName() != null)
                 aliasList.add(alias.getBusinessName()); 
         }
         return aliasList;
     }
 
     /**
      * Returns the default value this Property will have in a {@link DataObject data object} where the Property hasn't been set.
      * @return the default value.
      */
     public Object getDefault() {
         return this.property.findPropertyDefault();
     }
 
     public Property getOpposite() {
         if (!this.getType().isDataType()) {
             
             org.modeldriven.fuml.repository.Property oppositeProperty = this.property.getOpposite();
             if (oppositeProperty != null) {
             	return lookup(oppositeProperty);
             }
             else
             	return null;
             
         } else
             throw new PlasmaDataObjectException("cannot return opposite property for datatype property "
                 + this.getType().getName() + "." + this.getName());
     }
 
     /**
      * Returns the type of the Property.
      * @return the Property type.
      */
     public Type getType() {
         return this.type;
     }
 
     /**
      * Returns whether the Property is containment, i.e., whether it represents
      * by-value composition.
      * 
      * 3.1.6 Containment ...The tree structure is created using containment
      * references which start at the root DataObject. The root DataObject refers
      * to other DataObjects, which can refer to further DataObjects. Each
      * DataObject in the data graph, except for the root DataObject, must have a
      * containment reference from another node in the tree. Each DataObject in
      * the graph keeps track of the location of its containment reference. ... A
      * container DataObject is one that contains other DataObjects. A DataObject
      * can have a maximum of one container DataObject. If a DataObject has no
      * container, it is considered to be a root DataObject.
      * 
      * In the case of a reference, a Property may be either a containment or non-containment
      * reference. In EMOF, the term containment reference is called composite. In XML,
      * elements with complex types are mapped to containment properties.
      * 
      * S.C. indicates how to emit XML entities in terms of containment. E.g.
      * in UML, Package.packagedElement is a containment reference property.
      * Activity.edge is a reference property.
      * 
      * @return <code>true</code> if the Property is containment.
      */
     public boolean isContainment() {
         
         // Since since graph queries allow result graphs
         // to be described and composed in any number of ways
         // across a particular model, every reference property
         // should (potentially) be a containment property. Therefore
         // we return true here for every reference property.
         return !this.property.isDataType();
     }
 
     /**
      * Returns the containing type of this Property.
      * @return the Property's containing type.
      * @see Type#getProperties()
      */
     public Type getContainingType() {
         return this.containingType;
     }
 
     /**
      * Returns whether the Property is many-valued.
      * @return <code>true</code> if the Property is many-valued.
      */
     public boolean isMany() {
     	
         return this.property.isMany();
     }
 
     /**
      * Returns whether or not instances of this property can be set to null. The effect of 
      * calling set(null) on a non-nullable property is not specified by SDO.
      * @return true if this property is nullable.
      */
     public boolean isNullable() {
         return this.property.isNullable();
     }
 
     /**
      * Returns whether or not this is an open content Property.
      * @return true if this property is an open content Property.
      */
     public boolean isOpenContent() {
         // TODO Auto-generated method stub
         return false;
     }
 
     /**
      * Returns true if values for this Property cannot be modified using the SDO APIs.
      * When true, DataObject.set(Property property, Object value) throws an exception.
      * Values may change due to other factors, such as services operating on DataObjects.
      * @return true if values for this Property cannot be modified.
      */
     public boolean isReadOnly() {
         
         // For a delete() operation on data-object, the spec demands we unset all non-readonly
         // properties, but services need pri-keys and references to determine what
         // to delete in an underlying data store!! This implies that pri-key data properties MUST 
     	// always be read-only. Unfortunately many data models are quite liberal
     	// with setting many columns as pri-key(s), so assuming all these are
     	// read only is a bad assumption. 
         
         // In general for persistence capable objects, we don't want users to modify pri-key
         // properties, but whether these are truly read only "should" be defined
         // in the model itself, but users will omit this for sure.    
     	
     	// Service implementors assembling a graph as a result of a query need to set read-only
     	// data as well as reference properties. For data properties there is an easy bypass
     	// mechanism, but for reference properties, it is critical that implentors use the
     	// SDO APIs to link data objects as much is accomplished under the covers in terms of
     	// graph edges and the like. Is there an extension API targeted for service implemetors
     	// that does not check read-only on a set operation (??)
         //return this.property.getIsReadonly() || (this.property.isDataType() && this.isKey(KeyType.primary));
         
         return this.property.getIsReadonly();
     }
     
     public String toString() {
     	if (this.containingType != null)
     	    return this.containingType.toString() + "." + this.getName();
     	else
     		return this.getName();    		
     }
 
 	@Override
 	public Derivation getDerivation() {
 		return this.property.findDerivation();
 	}
 
 	@Override
 	public PlasmaProperty getDerivationSupplier() {
 		
 		Derivation derivation = this.property.findDerivation();
 		if (derivation != null) {
             org.modeldriven.fuml.repository.Property supplierProperty = this.property.findDerivationSupplier();
             if (supplierProperty != null) {
             	return lookup(supplierProperty);
             }
             else
             	return null;
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Finds a mapped SDO property adhering to any type or property name binding customizations.
 	 * @param repoProperty the repository property
 	 * @return a mapped SDO property adhering to any type or property name binding customizations.
 	 */
 	private PlasmaProperty lookup(org.modeldriven.fuml.repository.Property repoProperty) {
         Class_ repoClass = repoProperty.getClass_();
         String repoClassNamespaceURI = this.property.getNamespaceURI(
                 repoClass);
         String repoTypeName = repoClass.getName();
 
         TypeBinding binding = PlasmaConfig.getInstance().findTypeBinding(repoClassNamespaceURI, repoClass.getName());
         if (binding != null) {
         	if (binding.getLogicalName() != null && binding.getLogicalName().trim().length() > 0)
         		repoTypeName = binding.getLogicalName().trim();
         }
         Type repoType = PlasmaTypeHelper.INSTANCE.getType(repoClassNamespaceURI, 
         		repoTypeName);
         String repoPropertyName = repoProperty.getName();
         
         // lookup any property binding customization
         PropertyBinding propertyBinding = PlasmaConfig.getInstance().findPropertyBinding(repoClassNamespaceURI, 
         		repoClass.getName(), repoProperty.getName());
         if (propertyBinding != null) {
         	if (propertyBinding.getLogicalName() != null && propertyBinding.getLogicalName().trim().length() > 0)
         		repoPropertyName = propertyBinding.getLogicalName().trim();
         }
         
         if (repoPropertyName != null && repoPropertyName.trim().length() > 0) {
             return (PlasmaProperty)repoType.getProperty(repoPropertyName);
         }
         else {
         	repoPropertyName = this.getDerivedName(repoProperty.getType().getName());
             return (PlasmaProperty)repoType.getProperty(repoPropertyName);
         }		
 	}
 
 	
 
 }
