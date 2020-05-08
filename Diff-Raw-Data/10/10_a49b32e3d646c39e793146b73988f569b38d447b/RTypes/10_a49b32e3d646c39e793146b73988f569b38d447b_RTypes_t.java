 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.typeinfo;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.RegistryFactory;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.dltk.javascript.core.Types;
 import org.eclipse.dltk.javascript.internal.core.ThreadTypeSystemImpl;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 
 /**
  * Static utility methods pertaining to {@code IRType} instances.
  */
 public class RTypes {
 
 	private RTypes() {
 	}
 
 	private static IRType initRType(IRType defaultValue) {
 		final IExtensionRegistry registry = RegistryFactory.getRegistry();
 		if (registry != null) { // if running under OSGI
 			final String name = defaultValue.getClass().getSimpleName();
 			final IConfigurationElement[] elements = registry
 					.getConfigurationElementsFor(TypeInfoManager.EXT_POINT);
 			for (IConfigurationElement element : elements) {
 				if ("runtimeType".equals(element.getName())
 						&& name.equals(element.getAttribute("name"))) {
 					try {
 						return (IRType) element
 								.createExecutableExtension("class");
 					} catch (Exception e) {
 						JavaScriptPlugin.error(e);
 					}
 				}
 			}
 		}
 		return defaultValue;
 	}
 
 	private static final IRType UNDEFINED_TYPE = new Undefined();
 
 	static class Undefined extends RType {
 		public String getName() {
 			return ITypeNames.UNDEFINED;
 		}
 
 		public TypeCompatibility isAssignableFrom(IRType type) {
 			return TypeCompatibility.valueOf(type == this);
 		}
 	}
 
 	/**
 	 * Returns the instance of the special <b>undefined</b> type.
 	 */
 	public static IRType undefined() {
 		return UNDEFINED_TYPE;
 	}
 
 	private static final IRType ANY_TYPE = initRType(new Any());
 
 	static class Any extends RType {
 		public String getName() {
 			return "Any";
 		}
 
 		@Override
 		public TypeCompatibility isAssignableFrom(IRType type) {
 			return TypeCompatibility.TRUE;
 		}
 
 		@Override
 		public boolean isExtensible() {
 			return true;
 		}
 	}
 
 	/**
 	 * Returns the instance of the logical <b>Any</b> type.
 	 * 
 	 * @return
 	 */
 	public static IRType any() {
 		return ANY_TYPE;
 	}
 
 	private static final IRType NONE_TYPE = new None();
 
 	static class None extends RType {
 		public String getName() {
 			return "None";
 		}
 
 		@Override
 		public TypeCompatibility isAssignableFrom(IRType type) {
 			return TypeCompatibility.TRUE;
 		}
 
 		@Override
 		public boolean isExtensible() {
 			return true;
 		}
 	}
 
 	/**
 	 * Returns the instance of the logical <b>None</b> type (which is used as a
 	 * placeholder if generic type parameters (e.g. array item type) are not
 	 * known/specified).
 	 */
 	public static IRType none() {
 		return NONE_TYPE;
 	}
 
 	/**
 	 * Returns the instance of the <b>empty array literal</b>.
 	 */
 	public static IRArrayType arrayOf() {
 		return arrayOf(EMPTY_ARRAY_ITEM_TYPE);
 	}
 
 	static final IRType EMPTY_ARRAY_ITEM_TYPE = new EmptyArrayItem();
 
 	static class EmptyArrayItem extends RType {
 		public String getName() {
 			return "empty";
 		}
 
 		@Override
 		public TypeCompatibility isAssignableFrom(IRType type) {
 			return TypeCompatibility.TRUE;
 		}
 
 		@Override
 		public boolean isExtensible() {
 			return true;
 		}
 	}
 
 	public static IRType simple(Type type) {
 		if (Types.ARRAY == type) {
 			return arrayOf(none());
 		} else {
 			return type.toRType(null);
 		}
 	}
 
 	public static IRClassType classType(Type type) {
 		return new RClassType(type);
 	}
 
 	public static IRMapType mapOf(final IRType keyType, final IRType valueType) {
 		return new RMapType(keyType, valueType);
 	}
 
 	public static IRType recordType(ITypeSystem typeSystem,
 			Collection<Member> members) {
 		return new RRecordType(typeSystem, members);
 	}
 
 	public static IRType functionType(List<IRParameter> parameters,
 			IRType returnType) {
 		return new RFunctionType(parameters, returnType);
 	}
 
 	public static IRType union(Collection<IRType> targets) {
 		return new RUnionType(targets);
 	}
 
 	/**
 	 * Creates new instance of the array type with the specified itemType.
 	 */
 	public static IRArrayType arrayOf(final IRType itemType) {
		return new RArrayType(itemType != null ? itemType : none());
 	}
 
 	public static IRArrayType arrayOf(ITypeSystem typeSystem,
 			final IRType itemType) {
 		return new RArrayType(typeSystem, itemType);
 	}
 
 	public static IRType create(JSType type) {
 		ITypeSystem current = ITypeSystem.CURRENT.get();
 		if (current == null) {
 			current = ThreadTypeSystemImpl.DELEGATING_TYPE_SYSTEM;
 		}
 		return create(current, type);
 	}
 
 	public static IRType create(ITypeSystem context, JSType type) {
 		if (type == null) {
 			return null;
 		}
 		final IRType result = type.toRType(context);
 		if (result != null) {
 			return result;
 		}
 		for (IRTypeFactory factory : TypeInfoManager.getRTypeFactories()) {
 			final IRType runtimeType = factory.create(context, type);
 			if (runtimeType != null) {
 				return runtimeType;
 			}
 		}
 		throw new IllegalArgumentException("Unsupported type "
 				+ type.getClass().getName());
 	}
 
 }
