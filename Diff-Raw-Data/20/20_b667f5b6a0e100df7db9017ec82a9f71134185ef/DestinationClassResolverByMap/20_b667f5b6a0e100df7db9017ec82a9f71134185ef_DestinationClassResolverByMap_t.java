 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.nebulae2us.electron;
 
 import java.util.IdentityHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.nebulae2us.electron.util.IdentityEqualityComparator;
 import org.nebulae2us.electron.util.ImmutableMap;
 
 /**
  * @author Trung Phan
  *
  */
 public class DestinationClassResolverByMap implements DestinationClassResolver {
 
 	private final Map<Class<?>, Class<?>> associates;
 	
 	private final Map<Class<?>, Class<?>> inverseAssociates;
 	
 	public DestinationClassResolverByMap(Map<Class<?>, Class<?>> associates) {
 		this.associates = new ImmutableMap<Class<?>, Class<?>>(associates, IdentityEqualityComparator.getInstance());
 		Map<Class<?>, Class<?>> inverseAssociates = new IdentityHashMap<Class<?>, Class<?>>();
 		for (Entry<Class<?>, Class<?>> entry : associates.entrySet()) {
 			inverseAssociates.put(entry.getValue(), entry.getKey());
 		}
 		this.inverseAssociates = new ImmutableMap<Class<?>, Class<?>>(inverseAssociates, IdentityEqualityComparator.getInstance());
 	}
 	
 	public <T> Class<? extends T> getDestinationClass(Class<?> src, Class<T> expectedDest) {
 
 		Class<?> c = src;
 		while (c != null && c != Object.class) {
 			Class<?> candidateDest = associates.get(c);
 			if (candidateDest == null) {
 				candidateDest = inverseAssociates.get(c);
 			}
 			
 			if (candidateDest != null && expectedDest.isAssignableFrom(candidateDest)) {
 				return (Class<T>)candidateDest;
 			}
 			c = c.getSuperclass();
 		}

		if (expectedDest.isAssignableFrom(src)) {
			return (Class<T>)src;
		}
 		return expectedDest;
 	}
 
 }
