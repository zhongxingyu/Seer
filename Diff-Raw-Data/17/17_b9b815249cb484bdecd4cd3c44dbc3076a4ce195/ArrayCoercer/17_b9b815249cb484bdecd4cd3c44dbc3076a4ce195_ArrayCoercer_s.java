 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils.coercion.coercers;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.oobium.utils.Base64;
 import org.oobium.utils.coercion.TypeCoercer;
 import org.oobium.utils.json.JsonUtils;
 
 public class ArrayCoercer extends AbstractCoercer {
 
 	public Object coerce(Object object, Class<?> toType) {
 		Class<?> componentType = toType.getComponentType();
 		
 		if(object.getClass().isArray()) {
 			int len = Array.getLength(object);
 			Object array = Array.newInstance(componentType, len);
 			for(int i = 0; i < len; i++) {
 				Array.set(array, i, TypeCoercer.coerce(Array.get(object, i), componentType));
 			}
 			return array;
 		}
 		
 		if(object instanceof String) {
 			String s = (String) object;
 			if(componentType == byte.class) {
 				if(s.startsWith("/Base64(") && s.endsWith(")/")) {
 					return Base64.decode(s.substring(8, s.length()-2));
 				}
 				return s.getBytes();
 			}
 			object = JsonUtils.toList(s);
 		}
 		
 		if(object instanceof Collection) {
 			Collection<?> collection = (Collection<?>) object;
 			Object array = Array.newInstance(componentType, collection.size());
 			Iterator<?> iter = collection.iterator();
 			for(int i = 0; i < collection.size(); i++) {
 				Array.set(array, i, TypeCoercer.coerce(iter.next(), componentType));
 			}
 			return array;
 		}
 		
 		if(object instanceof Iterable<?>) {
 			List<Object> list = new ArrayList<Object>();
 			for(Object o : (Iterable<?>) object) {
 				list.add(o);
 			}
 			Object array = Array.newInstance(componentType, list.size());
 			for(int i = 0; i < list.size(); i++) {
 				Array.set(array, i, TypeCoercer.coerce(list.get(i), componentType));
 			}
 			return array;
 		}
 		
 		Object array = Array.newInstance(componentType, 1);
 		Array.set(array, 0, TypeCoercer.coerce(object, componentType));
 		return array;
 	}
 
 	@Override
 	public Object coerceNull() {
 		return null;
 	}
 
 	@Override
 	public Class<?> getType() {
 		return Object[].class;
 	}
 	
 	@Override
 	public boolean handleSubTypes() {
 		return true;
 	}
 
 }
