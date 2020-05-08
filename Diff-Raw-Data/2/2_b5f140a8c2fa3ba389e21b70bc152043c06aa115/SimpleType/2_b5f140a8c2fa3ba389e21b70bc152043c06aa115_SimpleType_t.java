 /*
  * Copyright (c) 2011 Colin Benner
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the
  * Software without restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
  * Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
  * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package de.uni_siegen.informatik.bs.alvic;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * SimpleType describes a built-in type like 'Integer' or 'Boolean' as well as
  * types provided by plug-ins, such as 'Graph' or 'Vertex'. It does not describe
  * the type of a function or an array.
  * 
  * @author Colin Benner
  */
 public class SimpleType implements Type {
 	/**
 	 * List of all simple types that have been used until now.
 	 */
 	private static Map<String, SimpleType> types = new HashMap<String, SimpleType>();
 
 	/**
 	 * If this type has a type parameter it is stored in this attribute.
 	 */
 	private Type argument = null;
 
 	/**
 	 * The type's name.
 	 */
 	private String name = null;
 
 	/**
 	 * The type this is derived from.
 	 */
 	private SimpleType parent = null;
 
 	/**
 	 * This stores which member an object of the type represented by 'this' has
 	 * and what their types are.
 	 */
 	private List<Member> members;
 
 	/**
 	 * This is somehow needed to run the parser tests but is not used for
 	 * anything else.
 	 */
 	private SimpleType() {
 	}
 
 	/**
 	 * This type matches any object. It is analogous to Java's "Object", except
 	 * that in Java there are types not derived from any class, and in Alvis
 	 * there are not.
 	 */
 	public static final SimpleType wildcard = new SimpleType("Object", null);
 
 	/**
 	 * @param name
 	 *	    the name of the type
 	 * @param argument
 	 *	    the Java generics type argument if there is one, null
 	 *	    otherwise
 	 */
 	private SimpleType(String name, Type argument) {
 		this.name = name;
 		this.argument = argument;
 	}
 
 	/**
 	 * Create non-generic type with the given name.
 	 * 
 	 * @param name
 	 *	    The name of the type that is to be created.
 	 * @return SimpleType object representing the type with that name.
 	 */
 	public static SimpleType create(String name) {
 		return create(name, null);
 	}
 
 	/**
 	 * This creates a new SimpleType with the given name and type argument if
 	 * there is one.
 	 * 
 	 * @param name
 	 *	    the type name
 	 * @param typeArgument
 	 *	    the generics argument
 	 * @return a SimpleType object representing 'name<typeArgument>'
 	 */
 	public static SimpleType create(String name, Type typeArgument) {
 		name = name.replaceAll(".*\\.PC", "");
 		String type = (null == typeArgument ? name : name + "<" + typeArgument
 				+ ">");
 
 		if (types.containsKey(type))
 			return types.get(type);
 
 		SimpleType ret = new SimpleType(name, typeArgument);
 		types.put(type, ret);
 		setMembers(ret); // This has to be done after adding the type, otherwise
 							// we would run into an infinite loop
 		return ret;
 	}
 
 	/**
 	 * This method is used to set the 'members' Map of the SimpleType given. We
 	 * need to do this, so we have a reference of which members a certain object
 	 * has.
 	 * 
 	 * @param t
 	 *	    the type that is initialized.
 	 */
 	@SuppressWarnings("unchecked")
 	private static void setMembers(SimpleType t) {
 		t.members = new ArrayList<Member>();
 		Object obj = Compiler.getInstance().getObject(t.name);
 		if (null == obj)
 			return;
 		Class<? extends Object> c = obj.getClass();
 
 		try {
 			Collection<String> attributes = (Collection<String>) c.getMethod(
 					"getMembers").invoke(obj);
 			Collection<String> methods = (Collection<String>) c.getMethod(
 					"getMethods").invoke(obj);
 
 			/*
 			 * FIXME This might need to be changed to allow using members in alvis for which there
 			 * is no corresponding member in the corresponding Javaa class.
 			 * In that case you will have to check whether the correct getters/setters exist and
 			 * use their types to find out what type the attribute has.
 			 */
 			for (Field field : c.getDeclaredFields())
 				if (attributes.contains(field.getName()))
 					t.members.add(Member.attribute(t, obj, field));
 
 			for (Method m : c.getMethods())
 				if (methods.contains(m.getName()))
 					t.members.add(Member.method(t, m));
 
 			if (!c.getName().replaceAll(".*\\.PC", "").equals("Object"))
 				t.parent = SimpleType.create(c.getSuperclass().getName());
 			else
 				t.parent = null;
 			// TODO decide on what to do with the exceptions
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return the name of this type.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the type this was derived from if this is not PCObject, null
 	 *	 otherwise.
 	 */
 	public SimpleType getParent() {
 		return parent;
 	}
 
 	/**
 	 * This indicates whether the class represented by this Type object has a
 	 * member with the given name;
 	 * 
 	 * @param name
 	 *	    The name that is supposed to be checked.
 	 * @return whether there is a member of this name.
 	 */
 	public boolean hasMember(String name) {
 		return 0 != getMember(name).size();
 	}
 
 	/**
 	 * Return the type of a member.
 	 * 
 	 * @param name
 	 *	    The name of the member.
 	 * @return the type of the member
 	 */
 	public List<Type> getMember(String name) {
 		List<Type> result = new ArrayList<Type>();
 		for (Member m : members)
 			if (name.equals(m.getName()))
 				result.add(m.getType());
 		return result;
 	}
 
 	/**
 	 * Get the generics type argument.
 	 * 
 	 * @return null if this is not a generic type, its type argument otherwise.
 	 */
 	public Type getTypeArgument() {
 		return argument;
 	}
 
 	/**
 	 * Return whether an object of the type represented by this object can be
 	 * used where something of a given type is expected.
 	 * 
 	 * @param other
 	 *          the expected type
 	 * @return whether this type is a subtype of other
 	 */
 	public boolean matches(Type other) {
 		if (!(other instanceof SimpleType))
 			return false;
 
 		if (this.equals(other))
 			return true;
 
 		if (this.name.equals(((SimpleType) other).getName()))
 			return (null == argument && null == ((SimpleType) other)
 					.getTypeArgument())
 					|| argument.equals(other.getTypeArgument());
 
 		for (SimpleType tmp = parent; tmp != null; tmp = tmp.getParent()) {
 			if (tmp.equals(other))
 				return true;
 		}
 
 		return false;
 	}
 
 	public boolean equals(Type other) {
 		return other.toString().equals(name);
 	}
 
 	@Override
 	public String toString() {
 		return null == argument ? name : argument + " " + name;
 	}
 
 	/**
 	 * Get the type that will be used in the generated source code to represent
 	 * this type.
 	 * 
 	 * @return the name of the class that actually provides this type.
 	 */
 	public String getJavaType() {
		return "PC" + (null == argument ? name : name + "<" + argument.getJavaType() + ">");
 	}
 }
