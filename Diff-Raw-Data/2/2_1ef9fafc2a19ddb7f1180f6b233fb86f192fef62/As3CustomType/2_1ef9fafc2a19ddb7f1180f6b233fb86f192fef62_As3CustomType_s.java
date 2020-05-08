 package net.histos.java2as.as3;
 
 /**
  * Represents a custom AS3 type that is not a built-in.
  *
  * @author cliff.meyers
  */
 public class As3CustomType implements As3Type {
 
 	//
 	// Fields
 	//
 
 	/**
 	 * Backing Java class for this custom type.
 	 */
 	private Class<?> clazz;
 
 	/**
 	 * Fully-qualified name for this type.
 	 */
 	private String qualifiedName;
 
 	/**
 	 * Short name for this type.
 	 */
 	private String simpleName;
 
 	//
 	// Constructors
 	//
 
 	public As3CustomType(Class<?> clazz) {
 		this.clazz = clazz;
 		this.qualifiedName = clazz.getName();
 		this.simpleName = clazz.getSimpleName();
 	}
 
 	public As3CustomType(String qualifiedName) {
 		this.qualifiedName = qualifiedName;
 		this.simpleName = qualifiedName.substring(qualifiedName.indexOf(".") + 1);
 	}
 
 	//
 	// Public Methods
 	//
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null) return false;
 		if (!(o instanceof As3CustomType)) return false;
 		As3CustomType that = (As3CustomType) o;
		return qualifiedName.equals(that.getQualifiedName().equals(o));
 	}
 
 	@Override
 	public int hashCode() {
 		return qualifiedName.hashCode();
 	}
 
 	//
 	// Getters and Setters
 	//
 
 	public boolean isCustomType() {
 		return true;
 	}
 
 	public String getQualifiedName() {
 		return qualifiedName;
 	}
 
 	public String getSimpleName() {
 		return simpleName;
 	}
 
 }
