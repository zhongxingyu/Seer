 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ti.BasicContext;
 import org.eclipse.dltk.ti.DLTKTypeInferenceEngine;
 import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 public class RubyMixinClass implements IRubyMixinElement {
 
 	private final String key;
 	protected final RubyMixinModel model;
 	private final boolean module;
 
 	public RubyMixinClass(RubyMixinModel model, String key, boolean module) {
 		super();
 		this.model = model;
 		this.key = key;
 		this.module = module;
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public RubyMixinClass getInstanceClass() {
 		if (!isMeta())
 			return this;
 		String newkey = key + RubyMixin.INSTANCE_SUFFIX;
 		IRubyMixinElement r = model.createRubyElement(newkey);
 		if (r instanceof RubyMixinClass)
 			return (RubyMixinClass) r;
 		return null;
 	}
 
 	public RubyMixinClass getMetaclass() {
 		if (isMeta())
 			return this;
 		String metakey = key.substring(0, key
 				.indexOf(RubyMixin.INSTANCE_SUFFIX));
 		IRubyMixinElement r = model.createRubyElement(metakey);
 		if (r instanceof RubyMixinClass)
 			return (RubyMixinClass) r;
 		return null;
 	}
 
 	public boolean isMeta() {
 		return (!key.endsWith(RubyMixin.INSTANCE_SUFFIX))
 				&& (!key.endsWith(RubyMixin.VIRTUAL_SUFFIX));
 	}
 
 	public String getName() {
 		String name = key.substring(key.lastIndexOf(MixinModel.SEPARATOR));
 		int pos;
 		if ((pos = name.indexOf(RubyMixin.INSTANCE_SUFFIX)) != -1)
 			name = name.substring(0, pos);
 		return name;
 	}
 
 	public IType[] getSourceTypes() {
 		List result = new ArrayList();
 		IMixinElement mixinElement = model.getRawModel().get(key);
 		Object[] allObjects = mixinElement.getAllObjects();
 		for (int i = 0; i < allObjects.length; i++) {
 			RubyMixinElementInfo info = (RubyMixinElementInfo) allObjects[i];
 			if (info == null)
 				continue;
 			if (info.getKind() == RubyMixinElementInfo.K_CLASS ||
 					info.getKind() == RubyMixinElementInfo.K_MODULE) {
 				result.add (info.getObject());							
 			}
 		}
 		return (IType[]) result.toArray(new IType[result.size()]);
 	}
 
 	public RubyMixinClass getSuperclass() {
 		if (this.module)
 			return null; // no inheritance for modules
 		IMixinElement mixinElement = model.getRawModel().get(key);
 		if (mixinElement == null)
 			return null;
 		Object[] allObjects = mixinElement.getAllObjects();
 //		IType type = null;
 		for (int i = 0; i < allObjects.length; i++) {
 			RubyMixinElementInfo info = (RubyMixinElementInfo) allObjects[i];
 			if( info == null ) {
 				continue;
 			}
 //			if (info.getKind() == RubyMixinElementInfo.K_CLASS) {
 //				type = (IType) info.getObject();
 //				if (type == null)
 //					continue;
 //				String key = RubyModelUtils.evaluateSuperClass(type);
 //				if (key == null)
 //					continue;
 //				if (!this.isMeta())
 //					key = key + RubyMixin.INSTANCE_SUFFIX;
 //				RubyMixinClass s = (RubyMixinClass) model.createRubyElement(key);
 //				return s;				
 //			}
 			if (info.getKind() == RubyMixinElementInfo.K_SUPER) {
 				SuperclassReferenceInfo sinfo = (SuperclassReferenceInfo) info.getObject();
 				BasicContext c = new BasicContext(sinfo.getModule(), sinfo.getDecl());
 				ExpressionTypeGoal g = new ExpressionTypeGoal(c, sinfo.getNode());
 				DLTKTypeInferenceEngine engine = new DLTKTypeInferenceEngine();
 				IEvaluatedType type2 = engine.evaluateType(g, 500);
 				if (type2 instanceof RubyClassType) {
 					RubyClassType rubyClassType = (RubyClassType) type2;
 					String key =  rubyClassType.getModelKey();
 					if (!this.isMeta())
 						key = key + RubyMixin.INSTANCE_SUFFIX;
 					return (RubyMixinClass) model.createRubyElement(key);
 				}
 			}
 		}
 		String key;
 		if (this.isMeta())
 			key = "Class%";
 		else
 			key = "Object";
 		if (!this.isMeta())
 			key = key + RubyMixin.INSTANCE_SUFFIX;
 		RubyMixinClass s = (RubyMixinClass) model.createRubyElement(key);
 		return s;				
 	}
 	
 	public RubyMixinClass[] getIncluded () {
 		List result = new ArrayList ();
 		HashSet names = new HashSet ();
 		IMixinElement mixinElement = model.getRawModel().get(key);
 		if (mixinElement == null)
 			return new RubyMixinClass[0];
 		Object[] allObjects = mixinElement.getAllObjects();
 		for (int i = 0; i < allObjects.length; i++) {
 			RubyMixinElementInfo info = (RubyMixinElementInfo) allObjects[i];
 			if( info == null ) {
 				continue;
 			}
 			if(info.getKind() == RubyMixinElementInfo.K_INCLUDE) {
 				String inclKey = (String) info.getObject();
 				if (!names.contains(inclKey)) {
 					names.add(inclKey);
 					if (/*!this.isMeta() && */!inclKey.endsWith(RubyMixin.INSTANCE_SUFFIX))
 						inclKey += RubyMixin.INSTANCE_SUFFIX;
 					IRubyMixinElement element = model.createRubyElement(inclKey);
 					if (element instanceof RubyMixinClass)
 						result.add(element);
 				}
 			}
 		}		
 		return (RubyMixinClass[]) result.toArray(new RubyMixinClass[result.size()]);
 	}
 
 	public RubyMixinMethod[] findMethods(String prefix, boolean includeTopLevel) {
 		final List result = new ArrayList();
		final Set names = new HashSet(); // for overload checks
 		
 		IMixinElement mixinElement = model.getRawModel().get(key);
 		if (mixinElement == null)
 			return new RubyMixinMethod[0];
 		IMixinElement[] children = mixinElement.getChildren();
 		for (int i = 0; i < children.length; i++) {
 			if (children[i].getLastKeySegment().startsWith(prefix)) {
 				IRubyMixinElement element = model.createRubyElement(children[i]);
 				if (element instanceof RubyMixinMethod) {
						RubyMixinMethod rubyMixinMethod = (RubyMixinMethod) element;
 						result.add(element);
						names.add(rubyMixinMethod.getName());
 				}
 			}
 		}
 		
 		RubyMixinClass[] included = this.getIncluded();
 		for (int i = 0; i < included.length; i++) {
 			RubyMixinMethod[] methods = included[i].findMethods(prefix, includeTopLevel);
			for (int j = 0; j < methods.length; j++) {
				if (!names.contains(methods[j].getName())) {
					result.add(methods[j]);
					names.add(methods[j].getName());
				}
			}
 		}
 		
 		if (!this.key.endsWith(RubyMixin.VIRTUAL_SUFFIX)) {		
 			RubyMixinClass superclass = getSuperclass();
 			if (superclass != null) {
 				
 				if (!superclass.getKey().equals(key)) {
 					RubyMixinMethod[] methods = superclass.findMethods(prefix,
 							includeTopLevel);
					for (int j = 0; j < methods.length; j++) {
						if (!names.contains(methods[j].getName())) {
							result.add(methods[j]);
							names.add(methods[j].getName());
						}
					}
 				}
 			}
 		}
 
 		return (RubyMixinMethod[]) result.toArray(new RubyMixinMethod[result
 				.size()]);
 	}
 
 	public RubyMixinMethod getMethod(String name) {
 		String possibleKey = key + MixinModel.SEPARATOR + name;
 		IMixinElement mixinElement = model.getRawModel().get(possibleKey);
 		if (mixinElement != null) {
 			return (RubyMixinMethod) model.createRubyElement(mixinElement);
 		}
 		
 		RubyMixinClass[] included = this.getIncluded();
 		for (int i = 0; i < included.length; i++) {
 			RubyMixinMethod method = included[i].getMethod(name);
 			if (method != null)
 				return method;
 		}
 		
 		// search superclass
 		// if (!this.key.equals("Object") && !this.key.equals("Object%")) {
 		RubyMixinClass superclass = getSuperclass();
 		if (superclass != null) {
 			if (superclass.getKey().equals(key))
 				return null;
 			return superclass.getMethod(name);
 		}
 		// }
 		return null;
 	}
 
 	public RubyMixinClass[] getClasses() {
 		List result = new ArrayList();
 		IMixinElement mixinElement = model.getRawModel().get(key);
 		IMixinElement[] children = mixinElement.getChildren();
 		for (int i = 0; i < children.length; i++) {
 			IRubyMixinElement element = model.createRubyElement(children[i]);
 			if (element instanceof RubyMixinClass)
 				result.add(element);
 		}
 		return (RubyMixinClass[]) result.toArray(new RubyMixinClass[result
 				.size()]);
 	}
 
 	public RubyMixinVariable[] getFields() {
 		List result = new ArrayList();
 		IMixinElement mixinElement = model.getRawModel().get(key);
 		IMixinElement[] children = mixinElement.getChildren();
 		for (int i = 0; i < children.length; i++) {
 			IRubyMixinElement element = model.createRubyElement(children[i]);
 			if (element instanceof RubyMixinVariable)
 				result.add(element);
 		}
 		RubyMixinClass superclass = getSuperclass();
 		if (superclass != null && superclass.key != "Object" && superclass.key != "Object%") {
 			if (superclass.getKey().equals(key))
 				return null;
 			 RubyMixinVariable[] superFields = superclass.getFields();
 			 result.addAll(Arrays.asList(superFields));
 		}
 		
 		return (RubyMixinVariable[]) result
 				.toArray(new RubyMixinVariable[result.size()]);
 	}
 
 }
