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
 package org.eclipse.dltk.javascript.internal.model.references;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.internal.javascript.typeinference.IReference;
 import org.eclipse.dltk.internal.javascript.typeinference.NativeArrayReference;
import org.eclipse.dltk.internal.javascript.typeinference.NativeBooleanReference;
import org.eclipse.dltk.internal.javascript.typeinference.NativeDateReference;
import org.eclipse.dltk.internal.javascript.typeinference.NativeNumberReference;
import org.eclipse.dltk.internal.javascript.typeinference.NativeStringReference;
import org.eclipse.dltk.internal.javascript.typeinference.NativeXMLReference;
 import org.eclipse.dltk.internal.javascript.typeinference.StandardSelfCompletingReference;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 public class ReferenceModelLoader {
 
 	private static ReferenceModelLoader instance = null;
 
 	public synchronized static ReferenceModelLoader getInstance() {
 		if (instance == null) {
 			instance = new ReferenceModelLoader();
 		}
 		return instance;
 	}
 
 	private final Resource resource;
 
 	private ReferenceModelLoader() {
 		registerFactories();
 		resource = loadResources();
 	}
 
 	private static Resource loadResources() {
 		final URI uri = URI.createPlatformPluginURI("/"
 				+ JavaScriptPlugin.PLUGIN_ID
 				+ "/resources/native-references.xml", true);
 		Resource resource = new XMIResourceImpl(uri);
 		try {
 			resource.load(null);
 		} catch (IOException e) {
 			JavaScriptPlugin.error(e);
 			if (!resource.isLoaded()) {
 				resource.getContents().clear();
 			}
 		}
 		return resource;
 	}
 
 	public Collection<IReference> getChildren(String typeName) {
 		for (EObject object : resource.getContents()) {
 			if (object instanceof Type) {
 				final Type type = (Type) object;
 				if (typeName.equals(type.getName())) {
 					return getChildren(type);
 				}
 			}
 		}
 		return Collections.emptyList();
 	}
 
 	private final Map<Type, List<IReference>> referencesByType = new IdentityHashMap<Type, List<IReference>>();
 
 	/**
 	 * @param type
 	 * @return
 	 */
 	private Collection<IReference> getChildren(Type type) {
 		List<IReference> list = referencesByType.get(type);
 		if (list != null) {
 			return list;
 		}
 		list = new ArrayList<IReference>();
 		for (Member member : type.getMembers()) {
 			if (member.getType() == null) {
 				JavaScriptPlugin.error("No type for " + type.getName() + "."
 						+ member.getName());
 				continue;
 			}
 			final Factory factory = factories.get(member.getType().getName());
 			if (factory == null) {
 				JavaScriptPlugin.error("No factory for " + type.getName() + "."
 						+ member.getName() + ":" + member.getType().getName());
 				continue;
 			}
 			final StandardSelfCompletingReference reference = factory
 					.create(member.getName());
 			reference.setProposalInfo(member.getDescription());
 			if (member instanceof Method) {
 				final Method method = (Method) member;
 				if (!method.getParameters().isEmpty()) {
 					final int paramCount = method.getParameters().size();
 					final String[] params = new String[paramCount];
 					for (int i = 0; i < paramCount; ++i) {
 						params[i] = method.getParameters().get(i).getName();
 					}
 					reference.setParameterNames(params);
 				}
 				reference.setFunctionRef();
 			}
 			list.add(reference);
 		}
 		list = Collections.unmodifiableList(list);
 		referencesByType.put(type, list);
 		return list;
 	}
 
 	private static interface Factory {
 		StandardSelfCompletingReference create(String name);
 	}
 
 	private final Map<String, Factory> factories = new HashMap<String, Factory>();
 
 	private void registerFactories() {
 		factories.put("Array", new Factory() {
 			public StandardSelfCompletingReference create(String name) {
 				return new NativeArrayReference(name);
 			}
 		});
 		factories.put("Boolean", new Factory() {
 			public StandardSelfCompletingReference create(String name) {
				return new NativeBooleanReference(name);
 			}
 		});
 		factories.put("Number", new Factory() {
 			public StandardSelfCompletingReference create(String name) {
				return new NativeNumberReference(name);
 			}
 		});
 		factories.put("String", new Factory() {
 			public StandardSelfCompletingReference create(String name) {
				return new NativeStringReference(name);
 			}
 		});
 		factories.put("XML", new Factory() {
 			public StandardSelfCompletingReference create(String name) {
				return new NativeXMLReference(name);
 			}
 		});
 		factories.put("Date", new Factory() {
 			public StandardSelfCompletingReference create(String name) {
				return new NativeDateReference(name);
 			}
 		});
 	}
 
 }
