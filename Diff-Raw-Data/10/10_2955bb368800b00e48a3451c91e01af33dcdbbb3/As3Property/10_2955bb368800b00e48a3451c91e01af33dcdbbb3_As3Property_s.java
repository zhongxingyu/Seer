 package net.histos.java2as.as3.transfer;
 
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.TypeHost;
 import net.histos.java2as.as3.As3Type;
 import net.histos.java2as.core.meta.DependencyKind;
 import net.histos.java2as.core.meta.JavaProperty;
 import net.histos.java2as.core.meta.Property;
 
 /**
  * Represents an ActionScript property
  */
 public class As3Property implements Property<As3Type> {
 
 	//
 	// Fields
 	//
 
 	private JavaProperty property;
 	private String name;
 	private As3Type type;
 	private String typeName;
 	private boolean arrayType;
 	private As3Type arrayElementType;
 	private As3Dependency dependency;
 	private boolean hasMetadata;
 	private String metadata;
 
 	//
 	// Constructors
 	//
 
 	/**
 	 * Constructs a simple property based off the backing Java property and the mapped ActionScript type.
 	 *
 	 * @param property Backing Java property
 	 * @param type     Mapped ActionScript type
 	 */
 	public As3Property(JavaProperty property, As3Type type) {
 		this(property, type, false, null);
 	}
 
 	/**
 	 * Constructs a simple or array-type property based off backing Java property and type information.
 	 *
 	 * @param property         Backing Java property.
 	 * @param type             Mapped ActionScript type.
 	 * @param arrayType        True if the property is an Array or ArrayCollection
 	 * @param arrayElementType If an array type, the type of element it holds.
 	 */
 	public As3Property(JavaProperty property, As3Type type, boolean arrayType, As3Type arrayElementType) {
 		this.property = property;
 		this.name = property.getName();
 		this.type = type;
 		this.typeName = type.getSimpleName();
 		this.arrayType = arrayType;
 		this.arrayElementType = arrayElementType;
 		this.dependency = new As3Dependency(DependencyKind.PROPERTY, type);
 	}
 
 	//
 	// Public Methods
 	//
 
 	/**
 	 * Turns on metadata display for this property.
 	 *
 	 * @param useArrayElementType True if the [ArrayElementType] metadata should be included
 	 */
 	public void enableMetadata(boolean useArrayElementType) {
 		if (isArrayType() && useArrayElementType) {
 			hasMetadata = true;
 			metadata = "[ArrayElementType(" + this.arrayElementType.getQualifiedName() + ")]";
 		}
 	}
 
 	@Override
 	public String toString() {
 		return name + ":" + type;
 	}
 
 	//
 	// Getters and Setters
 	//
 
 	public String getName() {
 		return property.getName();
 	}
 
 	public As3Type getType() {
 		return type;
 	}
 
 	public String getTypeName() {
 		return typeName;
 	}
 
 	public boolean isArrayType() {
 		return arrayType;
 	}
 
 	public As3Type getArrayElementType() {
 		return arrayElementType;
 	}
 
 	public As3Dependency getDependency() {
 		return dependency;
 	}
 
 	public boolean isHasMetadata() {
 		return hasMetadata;
 	}
 
 	public String getMetadata() {
 		return metadata;
 	}
 
 }
