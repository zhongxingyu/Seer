 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 394801 - AddGraphicalRepresentation doesn't carry properties
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The class PropertyBag.
  */
 public class PropertyBag implements IPropertyBag {
 	private HashMap<Object, Object> propertyMap;
 
 	public Object getProperty(Object key) {
 		return getPropertyMap().get(key);
 	}
 
 	public Object putProperty(Object key, Object value) {
 		return getPropertyMap().put(key, value);
 	}
 
 	/**
 	 * @since 0.10
 	 */
 	public List<Object> getPropertyKeys() {
 		List<Object> result = new ArrayList<Object>();
 
		if (propertyMap != null) {
			Iterator<Object> iterator = propertyMap.keySet().iterator();
			while (iterator.hasNext()) {
				Object key = (Object) iterator.next();
				result.add(key);
			}
 		}
 
 		return result;
 	}
 
 	private HashMap<Object, Object> getPropertyMap() {
 		if (this.propertyMap == null) {
 			this.propertyMap = new HashMap<Object, Object>();
 		}
 		return this.propertyMap;
 	}
 
 }
