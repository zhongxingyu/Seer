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
 
 /**
  * @author Trung Phan
  *
  */
 public class DestinationClassResolverByAnnotation implements DestinationClassResolver {
 
 	public <T> Class<? extends T> getDestinationClass(Class<?> src, Class<T> expectedDest) {
 		
 		while (src != null && src != Object.class) {
 			if (src.getAnnotation(Builder.class) != null) {
 				Class<?> dest = src.getAnnotation(Builder.class).destination();
 				
 				if (expectedDest.isAssignableFrom(dest)) {
 					return (Class<? extends T>)dest;
 				}
 
 				break;
 			}
 			
 			src = src.getSuperclass();
 		}
 
 		return expectedDest;
 	}
 
 }
