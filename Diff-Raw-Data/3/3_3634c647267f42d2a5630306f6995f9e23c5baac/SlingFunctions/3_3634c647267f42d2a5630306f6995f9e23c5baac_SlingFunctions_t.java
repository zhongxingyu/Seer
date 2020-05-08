 /*
  * Copyright 2012 - Six Dimensions
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.sixdimensions.wcm.cq.cqex.functions;
 
 import org.apache.sling.api.resource.Resource;
 import org.apache.sling.api.resource.ValueMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tldgen.annotations.Function;
 
 /**
  * Class for Sling EL functions.
  * 
  * @author dklco
  */
 public class SlingFunctions {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(SlingFunctions.class);
 
 	/**
 	 * Retrieves a property from the ValueMap allowing for default values.
 	 * 
 	 * @param properties
 	 *            the ValueMap from which to retrieve the value
 	 * @param key
 	 *            the key for the value to retrieve
 	 * @param defaultValue
 	 *            the default value, if no value is retrieved, this value is
 	 *            returned
	 * @return the value of the property matching the key or the default value
	 *         if no valid value is retrieved
 	 */
 	@Function(example = "${cqex:getProperty(properties, key, defaultValue)}")
 	public static Object getProperty(final ValueMap properties,
 			final String key, final Object defaultValue) {
 		log.trace("getProperty");
 		Object value = null;
 		if (defaultValue != null) {
 			value = properties.get(key, defaultValue);
 		} else {
 			value = properties.get(key);
 		}
 		return value;
 	}
 
 	/**
 	 * Checks to see if there is a child resource at the specified path.
 	 * 
 	 * @param resource
 	 *            the resource to check the children thereof
 	 * @param path
 	 *            the path of the child to check
 	 * @return true if there is a child at the specified path, false otherwise
 	 */
 	@Function(example = "${cqex:hasChild(resource,path)}")
 	public static boolean hasChild(final Resource resource, final String path) {
 		log.trace("hasChild");
 		return resource.getChild(path) != null;
 	}
 
 	/**
 	 * Do not invoke the default constructor.
 	 */
 	protected SlingFunctions() {
 		// prevents calls from subclass
 		throw new UnsupportedOperationException();
 	}
 }
