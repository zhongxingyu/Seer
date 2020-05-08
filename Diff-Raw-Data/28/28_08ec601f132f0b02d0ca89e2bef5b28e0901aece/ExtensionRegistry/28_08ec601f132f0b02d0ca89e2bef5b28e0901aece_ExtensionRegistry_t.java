 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.extensionpoint;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ExtensionRegistry {
 
 	public static final ExtensionRegistry INSTANCE = new ExtensionRegistry();
 	
 	private Map<String, ESConfigElement> configElements;
 
 	private ExtensionRegistry() {
 		configElements = new HashMap<String, ExtensionRegistry.ESConfigElement>();
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T get(String id, Class<T> clazz, T defaultInstance, boolean shouldSetDefault) {
 		
 		T extensionPointInstnace = getExtensionElement(id, clazz);
 		
 		if (extensionPointInstnace != null) {
 			return extensionPointInstnace;
 		}
 		
 		ESConfigElement configElement = configElements.get(id);
 		T t;		
 		
 		if (configElement != null) {
 			t = (T) configElement.get();	
 		} else {
 			t = defaultInstance;
 			if (shouldSetDefault) {
 				set(id, t);
 			}
 		}
 		
 		return t;
 	}
 	
 	public <T> T get(String id, Class<T> clazz) {
 		return get(id, clazz, null, false);
 	}
 	
 	public <T> void set(String id, T t) {
 		// TODO: if already present?
 		configElements.put(id, new ESConfigElement(id, t));
 	}
 	
 	private <T> T getExtensionElement(String id, Class<T> t) {
 		
 		int idx = id.lastIndexOf('.');
 		String extensionPointId = id.substring(0, idx);
 		String attributeName = id.substring(idx + 1, id.length());
 		
 		ESExtensionPoint extensionPoint = new ESExtensionPoint(extensionPointId);
 		
 		if (extensionPoint.getFirst() == null) {
 			return null;
 		}
 		
 		return extensionPoint.getFirst().getClass(attributeName, t);
 	}
 	
 	class ESConfigElement {
 		
 		private String id;
 		private Object t;
 
 		public ESConfigElement(String id) {
 			this.id = id;
 		}
 		
 		public ESConfigElement(String id, Object o) {
 			this.id = id;
 			t = o;
 		}
 
 		public Object get() {
 			return t;
 		}
 
 		public void set(Object t) {
 			this.t = t;
 		}
 	}
 
 	public void remove(String id) {
 		configElements.remove(id);
 	}
 }
