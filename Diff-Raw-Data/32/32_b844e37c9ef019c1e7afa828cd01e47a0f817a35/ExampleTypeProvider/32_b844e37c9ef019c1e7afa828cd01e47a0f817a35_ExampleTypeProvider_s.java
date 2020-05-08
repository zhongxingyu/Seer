 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core.tests.typeinference;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInfoContext;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.ITypeProvider;
 import org.eclipse.dltk.javascript.typeinfo.TypeMode;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.emf.common.util.EList;
 
 @SuppressWarnings("nls")
 public class ExampleTypeProvider implements ITypeProvider {
 
 	static final String TYPE_SERVICE = "ExampleService";
 	private static final String TYPE_RESPONSE = "ExampleResponse";
 
 	private static final String TYPE_SERVICE2 = "ExampleService2";
 
 	static final String TYPE_EXAMPLE_FORMS = "ExampleForms";
 
 	static final String TYPE_GENERIC_ARRAY_METHOD = "ExampleArrayMethod";
 
 	public boolean initialize(ITypeInfoContext context) {
 		return true;
 	}
 
 	public Type getType(ITypeInfoContext context, TypeMode mode, String typeName) {
		if (TYPE_SERVICE.equals(typeName)) {
 			Type type = TypeInfoModelFactory.eINSTANCE.createType();
 			type.setName(typeName);
 			type.setKind(TypeKind.JAVA);
 
 			Method method1 = TypeInfoModelFactory.eINSTANCE.createMethod();
 			method1.setName("execute");
 			method1.setType(context.getTypeRef(TYPE_RESPONSE));
 			type.getMembers().add(method1);
 
 			Method method2 = TypeInfoModelFactory.eINSTANCE.createMethod();
 			method2.setName("executeCompatible");
 			method2.setType(context.getTypeRef(TYPE_RESPONSE));
 			method2.setDeprecated(true);
 			type.getMembers().add(method2);
 
 			Property prop1 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop1.setName("name");
 			prop1.setType(context.getTypeRef(ITypeNames.STRING));
 			type.getMembers().add(prop1);
 
 			Property prop2 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop2.setName("nameCompatible");
 			prop2.setType(context.getTypeRef(ITypeNames.STRING));
 			prop2.setDeprecated(true);
 			type.getMembers().add(prop2);
 
 			{
 				Method method5 = TypeInfoModelFactory.eINSTANCE.createMethod();
 				method5.setName("run");
 				method5.setDescription("description for run(language,code)");
 				Parameter p = TypeInfoModelFactory.eINSTANCE.createParameter();
 				p.setName("language");
 				p.setType(context.getTypeRef(ITypeNames.STRING));
 				method5.getParameters().add(p);
 				p = TypeInfoModelFactory.eINSTANCE.createParameter();
 				p.setName("code");
 				p.setType(context.getTypeRef(ITypeNames.STRING));
 				method5.getParameters().add(p);
 				type.getMembers().add(method5);
 			}
 			{
 				Method method3 = TypeInfoModelFactory.eINSTANCE.createMethod();
 				method3.setName("run");
 				method3.setDescription("description for run()");
 				type.getMembers().add(method3);
 			}
 			{
 				Method method4 = TypeInfoModelFactory.eINSTANCE.createMethod();
 				method4.setName("run");
 				method4.setDescription("description for run(code)");
 				Parameter p = TypeInfoModelFactory.eINSTANCE.createParameter();
 				p.setName("code");
 				p.setType(context.getTypeRef(ITypeNames.STRING));
 				method4.getParameters().add(p);
 				type.getMembers().add(method4);
 			}
 
 			context.markInvariant(type);
 
 			return type;
 		} else if (TYPE_RESPONSE.equals(typeName)) {
 			Type type = TypeInfoModelFactory.eINSTANCE.createType();
 			type.setName(typeName);
 			type.setKind(TypeKind.JAVA);
 
 			Property prop1 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop1.setName("status");
 			prop1.setType(context.getTypeRef(ITypeNames.NUMBER));
 			type.getMembers().add(prop1);
 
 			Property prop2 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop2.setName("service");
 			prop2.setType(context.getTypeRef(TYPE_SERVICE));
 			type.getMembers().add(prop2);
 
 			context.markInvariant(type);
 
 			return type;
 		} else if (TYPE_SERVICE2.equals(typeName)) {
 			Type type = TypeInfoModelFactory.eINSTANCE.createType();
 			type.setName(typeName);
 			type.setKind(TypeKind.JAVA);
 			type.setDeprecated(true);
 
 			Method method1 = TypeInfoModelFactory.eINSTANCE.createMethod();
 			method1.setName("execute");
 			method1.setType(context.getTypeRef(TYPE_RESPONSE + "Garbage"));
 			type.getMembers().add(method1);
 
 			Property prop1 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop1.setName("name");
 			prop1.setType(TypeUtil.ref(ITypeNames.STRING));
 			type.getMembers().add(prop1);
 
 			return type;
 		} else if (TYPE_EXAMPLE_FORMS.equals(typeName)) {
 			Type type = TypeInfoModelFactory.eINSTANCE.createType();
 			type.setName(typeName);
 			type.setKind(TypeKind.JAVA);
 
 			Property prop1 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop1.setName("service");
 			prop1.setType(context.getTypeRef(TYPE_SERVICE));
 			type.getMembers().add(prop1);
 
 			Property prop2 = TypeInfoModelFactory.eINSTANCE.createProperty();
 			prop2.setName("deprecatedName");
 			prop2.setType(TypeUtil.ref(ITypeNames.STRING));
 			prop2.setDeprecated(true);
 			type.getMembers().add(prop2);
 
 			return type;
 		} else if (TYPE_GENERIC_ARRAY_METHOD.equals(typeName)) {
 			Type type = TypeInfoModelFactory.eINSTANCE.createType();
 			type.setName(typeName);
 			type.setKind(TypeKind.JAVA);
 
 			Property property = TypeInfoModelFactory.eINSTANCE.createProperty();
 			property.setName("genericArrayProperty");
 			property.setType(TypeUtil.arrayOf(TypeUtil.ref(ITypeNames.STRING)));
 			type.getMembers().add(property);
 
 			Method method1 = TypeInfoModelFactory.eINSTANCE.createMethod();
 			method1.setName("execute");
 			method1.setType(TypeUtil.arrayOf(TypeUtil.ref(ITypeNames.STRING)));
 			type.getMembers().add(method1);
 			return type;
 		} else if (typeName.startsWith("Packages.")) {
 			String name = typeName.substring("Packages.".length());
 			try {
 				Class<?> clz = Class.forName(name, false, Thread
 						.currentThread().getContextClassLoader());
 				return getClassType(clz, name, context);
 			} catch (ClassNotFoundException e) {
 			}
 		}
 		return null;
 	}
 
 	private static Type getClassType(Class<?> clz, String name,
 			ITypeInfoContext context) {
 		Type type = TypeInfoModelFactory.eINSTANCE.createType();
 		type.setName(name);
 		type.setKind(TypeKind.JAVA);
 
 		java.lang.reflect.Method[] methods = clz.getMethods();
 		Field[] fields = clz.getFields();
 
 		EList<Member> members = type.getMembers();
 
 		for (Field field : fields) {
 			Property property = TypeInfoModelFactory.eINSTANCE.createProperty();
 			property.setName(field.getName());
 			Class<?> fieldType = field.getType();
 			if (fieldType != null)
 				property.setType(TypeUtil.ref(context.getKnownType("Packages."
 						+ fieldType.getName(), null)));
 			if (Modifier.isStatic(field.getModifiers())) {
 				property.setStatic(true);
 			}
 			members.add(property);
 		}
 		for (java.lang.reflect.Method method : methods) {
 			org.eclipse.dltk.javascript.typeinfo.model.Method m = TypeInfoModelFactory.eINSTANCE
 					.createMethod();
 			m.setName(method.getName());
 			Class<?> methodType = method.getReturnType();
 			if (methodType != null)
 				m.setType(TypeUtil.ref(context.getKnownType("Packages."
 						+ methodType.getName(), null)));
 
 			EList<Parameter> parameters = m.getParameters();
 			Class<?>[] parameterTypes = method.getParameterTypes();
 			for (int i = 0; i < parameterTypes.length; i++) {
 				Parameter parameter = TypeInfoModelFactory.eINSTANCE
 						.createParameter();
 				parameter.setName(parameterTypes[i].getSimpleName() + " arg"
 						+ i);
 				parameter.setType(TypeUtil.ref(context.getKnownType("Packages."
 						+ parameterTypes[i].getName(), null)));
 				parameters.add(parameter);
 			}
 			if (Modifier.isStatic(method.getModifiers())) {
 				m.setStatic(true);
 			}
 			members.add(m);
 		}
 		return type;
 	}
 
 	public Set<String> listTypes(ITypeInfoContext context, TypeMode mode,
 			String prefix) {
 		final Set<String> result = new HashSet<String>();
 		final String[] names = new String[] { TYPE_SERVICE, TYPE_SERVICE2,
 				TYPE_RESPONSE, TYPE_EXAMPLE_FORMS, TYPE_GENERIC_ARRAY_METHOD };
 		for (String name : names) {
 			if (CharOperation.prefixEquals(prefix, name)) {
 				result.add(name);
 			}
 		}
 		return result;
 	}
 
 }
