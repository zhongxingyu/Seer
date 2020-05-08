 /*
  * Copyright (c) 2011-2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.openinfinity.core.util;
 
 /**
  * Interface for handling <code>java.util.Map</code> entries by utilizing callbacks.
  * 
  * @author Ilkka Leinonen
  * @version 1.0.0
 * @since 1.3.0
  */
 public interface MapElementCallback<K, V> {
 	
 	/**
 	 * Callbacks each entry of <code>java.util.Map</code>.
 	 *
 	 * @param callbackObject Represents type safe entry of <code>java.util.Map</code>.
 	 */
 	void callback(K key, V value);
 
 }
