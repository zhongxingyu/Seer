 /*
  *      Copyright (C) 2005-2012 Team XBMC
  *      http://xbmc.org
  *
  *  This Program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2, or (at your option)
  *  any later version.
  *
  *  This Program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with XBMC Remote; see the file license.  If not, write to
  *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
  *  http://www.gnu.org/copyleft/gpl.html
  *
  */
 package org.xbmc.android.jsonrpc.generator.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xbmc.android.jsonrpc.generator.introspect.Method;
 import org.xbmc.android.jsonrpc.generator.introspect.Param;
 import org.xbmc.android.jsonrpc.generator.introspect.Property;
 import org.xbmc.android.jsonrpc.generator.introspect.Type;
 import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
 import org.xbmc.android.jsonrpc.generator.model.JavaClass;
 import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
 import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
 import org.xbmc.android.jsonrpc.generator.model.JavaParameter;
 import org.xbmc.android.jsonrpc.generator.model.Namespace;
 
 /**
  * Produces a {@link JavaClass} for a given {@link Method}.
  * 
  * @author freezy <freezy@xbmc.org>
  */
 public class MethodController {
 
 	private final String name;
 	private final String apiType;
 	private final Method method;
 	
 	private final static String RESULT_CLASS_SUFFIX = "Result";
 	private final static List<String> META_RETURN_PROPS = new ArrayList<String>();
 	
 	static {
 		META_RETURN_PROPS.add("limits");
 	}
 	
 	/**
 	 * Creates a new method controller.
 	 * @param apiType Full name of the method, e.g.: <tt>Addons.ExecuteAddon</tt>
 	 * @param method Unserialized JSON object
 	 */
 	public MethodController(String apiType, Method method) {
 		if (method == null) {
 			throw new IllegalArgumentException("Method parameter must not be null.");
 		}
 		this.method = method;
 		this.apiType = apiType;
 		this.name = apiType.contains(".") ? apiType.substring(apiType.indexOf(".") + 1) : apiType;
 	}
 	
 	/**
 	 * Registers a method.
 	 * @param packageName 
 	 */
 	public Namespace register(String packageName, String classSuffix) {
 		
 		final Namespace ns = Namespace.getMethod(apiType, packageName, classSuffix);
 		ns.addClass(getClass(ns, name));
 		
 		return ns;
 	}
 	
 	/**
 	 * Creates the agnostic {@link JavaClass} object.
 	 * 
 	 * @param className Name of the class (retrieved from parent key)
 	 * @return Class object
 	 */
 	public JavaMethod getClass(Namespace namespace, String methodName) {
 		
 		final JavaMethod klass = new JavaMethod(namespace, name, apiType);
 		
 //		final List<JavaConstructor> constructors = new ArrayList<JavaConstructor>();
 		final JavaConstructor jc = new JavaConstructor(klass);
 		
 		// parameters
 		for (Param p : method.getParams()) {
 			
 			if (p.isEnum()) {
 			} else if (p.isArray() && p.getItems().isEnum()) {
 			} else {
 				final TypeWrapper tr = p.getType();
 				if (tr == null || !tr.isList()) {
 					final PropertyController pc = new PropertyController(name, p);
 					final JavaClass type = pc.getClass(namespace, p.getName(), klass);
 					final JavaParameter jp = new JavaParameter(p.getName(), type);
 					jp.setDescription(p.getDescription());
 					jc.addParameter(jp);
					if (type.isInner()) {
						klass.linkInnerType(type);
					}
 				}
 			}
 
 			
 /*			final Property obj = p.obj();
 			final Property itemsObj = obj.getItems() != null ? obj.getItems().obj() : null;
 			
 			if (obj.isEnum()) {
 				
 				final PropertyController pc = new PropertyController(name, p);
 				final JavaEnum e = pc.getEnum(p.getName());
 				jc.addParameter(new JavaParameter(p.getName(), e));
 				
 			} else if (obj.isArray() && obj.getItems().isEnum()) {
 			} else if (itemsObj != null && itemsObj.isEnum()) {
 			} else {
 				final TypeWrapper tr = p.getType();
 				if (tr == null || !tr.isList()) {
 					final PropertyController pc = new PropertyController(name, p);
 					final JavaClass type = pc.getClass(namespace, p.getName(), klass);
 					jc.addParameter(new JavaParameter(p.getName(), type));
 					
 					System.out.println("T " + apiType + "." + p.getName() + ": " + type);
 				}
 			}*/
 		}
 
 //		constructors.add(jc);
 		klass.addConstructor(jc);
 
 
 		// return type
 		final TypeWrapper tw = method.getReturns();
 		if (tw.isObject()) {
 			
 			final Type type = tw.getObj();
 			
 			// result type is either native, array, a type reference...
 			if (type.isNative() || type.isRef() || type.isArray()) {
 				
 				final String name = klass.getName() + RESULT_CLASS_SUFFIX;
 				final PropertyController returnTypeController = new PropertyController(null, method.getReturns().getObj());
 				final JavaClass returnType = returnTypeController.getClass(namespace, name, klass);
 				
 				if (returnType.isTypeArray()) {
 					klass.linkInnerType(returnType.getArrayType());
 				}
 				
 				klass.setReturnType(returnType);
 
 			// ...or an object	
 			} else if (type.isObjectDefinition()) {
 				/* 
 				 * In case of an object, there are two scenarios. Either the
 				 * the object is a "meta" object, meaning it contains meta data
 				 * such as the limits of the result. In this case there is
 				 * one non-meta property where the data is stored. Example:
 				 *  "returns" : {
 				 *  	"properties" : {
 				 *  		"limits" : { "$ref" : "List.LimitsReturned", "required" : true },
 				 *  		"sources" : { "$ref" : "List.Items.Sources", "required" : true }
 				 *  	},
 				 *  	"type" : "object"
 				 *  }
 				 *  If there is more than one "non-meta" property, we assume
 				 *  it's a full-fledged object definition which will result in
 				 *  an inner class:
 				 *   "returns" : {
 				 *   	"properties" : {
 				 *   		"details" : { "description" : "Tran...", "required" : true, "type" : "any" },
 				 *   		"mode" : { "description" : "Dir...", "enums" : ["redirect", "direct"], "required" : true, "type" : "string" },
 				 *   		"protocol" : { "enums" : ["http"], "required" : true, "type" : "string" }
 				 *   	},
 				 *   	"type" : "object"
 				 *   }
 				 */
 				if (!type.hasProperties() && !type.hasAdditionalProperties()) {
 					throw new IllegalStateException("Definition is object but no props defined. That's seriously weird.");
 				}
 				
 				if (type.hasProperties()) {
 					
 					// go through props and compare and count.
 					String potentialResultPropName = null; // the non-meta prop in case there is only one.
 					int nonMetaProps = 0;
 					for (String propName : type.getProperties().keySet()) {
 						if (!META_RETURN_PROPS.contains(propName)) {
 							potentialResultPropName = propName;
 							nonMetaProps++;
 						}
 					}
 					
 					// first case described above: data is wrapped into a meta object.
 					if (nonMetaProps == 1) {
 						final Property prop = type.getProperties().get(potentialResultPropName);
 						if (!prop.isRef() && !prop.isNative() && !prop.isArray()) {
 							throw new IllegalStateException("Return type is expected to be either reference, native or array");
 						}
 						
 						final PropertyController returnTypeController = new PropertyController(null, prop);
 						klass.setReturnType(returnTypeController.getClass(namespace, null, klass));
 						klass.setReturnProperty(potentialResultPropName);
 					
 					// second case: full object definition. we suffix the class name with RESULT_CLASS_SUFFIX	
 					} else {
 						final String name = klass.getName() + RESULT_CLASS_SUFFIX;
 						final PropertyController returnTypeController = new PropertyController(null, type);
 						final JavaClass returnType = returnTypeController.getClass(namespace, name, klass);
 						klass.setReturnType(returnType);
 						klass.getInnerTypes().add(returnType);
 					}
 				} else {
 					throw new UnsupportedOperationException("Naked objects with only additional attributes are not yet supported.");
 				}
 				
 			} else {
 				throw new IllegalStateException("Result type is expected to be a reference, native or an object.");
 			}
 			
 		} else {
 			throw new RuntimeException("Expected return type is an object with properties.");
 		}
 		
 		// description
 		klass.setDescription(method.getDescription());
 		
 		return klass;
 	}
 }
