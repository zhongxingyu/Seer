 package net.histos.java2as.core.meta;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.*;
 import java.util.Collection;
 
 /**
  * Models a Transfer Object property, either from a getter method or field.
  *
  * @author cliff.meyers
  */
 public class JavaProperty implements Property<Class<?>> {
 
 	//
 	// Fields
 	//
 
 	private String name;
 	private Class<?> type;
 	private Method getter;
 	private Field field;
 
 	//
 	// Constructors
 	//
 
 	public JavaProperty(Method getter) {
 		this.getter = getter;
		this.name = getter.getName().substring(3, 3).toLowerCase() + getter.getName().substring(4);
 		this.type = getter.getReturnType();
 	}
 
 	public JavaProperty(Field field) {
 		this.field = field;
 		this.name = field.getName();
 		this.type = field.getType();
 	}
 
 	//
 	// Public Methods
 	//
 
 
 	@Override
 	public String toString() {
 		return "JavaProperty{" +
 				type + " " +
 				name +
 				'}';
 	}
 
 	/**
 	 * @return Property's name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return The underlying Java type for this property
 	 */
 	public Class<?> getType() {
 		return type;
 	}
 
 	/**
 	 * @return True if the type is an Array or a java.util.Collection
 	 */
 	public boolean isArrayType() {
 		Class<?> javaType = getType();
 		return javaType.isArray() || Collection.class.isAssignableFrom(javaType);
 	}
 
 	/**
 	 * @return The type contained in the array type, or Object.class if untyped.
 	 */
 	public Class<?> getArrayElementType() {
 		Class<?> javaType = getType();
 		if (javaType.isArray())
 			return javaType.getComponentType();
 
 		Type type = null;
 		if (getter != null)
 			type = getter.getGenericReturnType();
 		else if (field != null)
 			type = field.getGenericType();
 
 		if (type instanceof ParameterizedType) {
 			Type paramType = ((ParameterizedType) type).getActualTypeArguments()[0];
 			// handles the case where paramType is java.lang.reflect.WildcardType
 			if (paramType instanceof WildcardType || paramType instanceof TypeVariable)
 				return Object.class;
 			else
 				return (Class<?>) paramType;
 		}
 
 		return Object.class;
 	}
 
 	/**
 	 * @return The annotations associated with the Field or getter Method for this property.
 	 */
 	public Annotation[] getAnnotations() {
 		if (getter != null)
 			return getter.getAnnotations();
 		else
 			return field.getAnnotations();
 	}
 }
