 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.persistence;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 
 /**
 * Properties file persistence delegate implementation.
 * <p>
 * The persistence delegates reads and writes a simple grouped properties file format.
  */
 public abstract class AbstractPropertiesPersistenceDelegate extends ExecutableExtension implements IPersistenceDelegate {
 
 	public final static String CORE_SECTION_NAME = "core"; //$NON-NLS-1$
 
 	/**
 	 * Constructor.
 	 */
 	public AbstractPropertiesPersistenceDelegate() {
 		super();
 	}
 
 	/**
 	 * Convert the given context to map.
	 * 
 	 * @param context The context. Must not be <code>null</code>.
 	 * @return Map representing the context.
	 * 
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	protected Map<String, Object> toMap(final Object context) throws IOException {
 		Map<String, Object> result = new HashMap<String,Object>();
 
 		Map<String,Object> attrs = null;
 		if (context instanceof Map) {
 			attrs = (Map<String, Object>)context;
 		}
 		else if (context instanceof IPropertiesContainer) {
 			IPropertiesContainer container = (IPropertiesContainer)context;
 			attrs = new HashMap<String,Object>(container.getProperties());
 		}
 
 		if (attrs != null) {
 			for (String key : attrs.keySet()) {
 				if (!key.endsWith(".transient")) { //$NON-NLS-1$
 					result.put(key, attrs.get(key));
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Convert a map into the needed context object.
	 * 
 	 * @param map The map representing the context. Must not be <code>null</code>.
 	 * @param context The context to put the map values in or <code>null</code>.
 	 * @return The context object.
	 * 
 	 * @throws IOException
 	 */
 	protected Object fromMap(Map<String,Object> map, Object context) throws IOException {
 		if (context == null || Map.class.equals(context.getClass())) {
 			return map;
 		}
 		else if (context instanceof Map) {
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			Map<String,Object> newMap = new HashMap<String, Object>((Map)context);
 			newMap.putAll(map);
 			return newMap;
 		}
 		else if (IPropertiesContainer.class.equals(context.getClass())) {
 			IPropertiesContainer container = new PropertiesContainer();
 			container.setProperties(map);
 
 			return container;
 		}
 		else if (context instanceof IPropertiesContainer) {
 			IPropertiesContainer container = (IPropertiesContainer)context;
 			container.setProperties(map);
 
 			return container;
 		}
 
 		return null;
 	}
 }
