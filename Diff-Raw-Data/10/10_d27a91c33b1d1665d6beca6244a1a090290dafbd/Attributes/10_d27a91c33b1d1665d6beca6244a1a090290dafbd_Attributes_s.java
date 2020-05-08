 /*
  * Copyright 2011 Tomas Schlosser
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
 package org.anadix.html;
 
 import java.util.Collection;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Class representing set of element's attributes. Contains both the attributes
  * contained in tag and contained in style.
  *
  * @author tomason
  * @version $Id: $
  */
 public class Attributes {
 	private final Properties attributes;
 
 	/**
 	 * Constructor
 	 *
 	 * @param attributes list of key - value pairs
 	 */
 	public Attributes(Properties attributes) {
 		if (attributes == null) {
 			throw new NullPointerException("attributes can't be null");
 		}
 		this.attributes = new Properties();
 
 		for (String key : attributes.stringPropertyNames()) {
 			this.attributes.setProperty(key, attributes.getProperty(key));
 		}
 
 		if (hasAttribute("style")) {
 			Properties style = parseStyle(getAttribute("style"));
 			for (String key : style.stringPropertyNames()) {
 				this.attributes.setProperty(key, style.getProperty(key));
 			}
 		}
 	}
 
 	/**
 	 * Gets the attribute by name
 	 *
 	 * @param key name of the attribute to get
 	 * @return value of the attribute or null if attribute is not defined
 	 */
 	public String getAttribute(String key) {
 		return attributes.getProperty(key);
 	}
 
 	/**
 	 * Gets the attribute by name
 	 *
 	 * @param key name of the attribute to get
 	 * @param defaultValue default value to use if attribute is not defined
 	 * @return value of the attribute or defaultValue if attribute is not defined
 	 */
 	public String getAttribute(String key, String defaultValue) {
 		return attributes.getProperty(key, defaultValue);
 	}
 
 	/**
 	 * Determines whether attribute with given name is defined
 	 *
 	 * @param key name of the attribute
 	 * @return true if attribute is defined false otherwise
 	 */
 	public boolean hasAttribute(String key) {
 		return attributes.containsKey(key);
 	}
 
 	/**
	 * Gets names of all attributes 
 	 *
 	 * @return Collection of attribute names
 	 */
 	public Collection<String> getAvaliableAttributes() {
 		return attributes.stringPropertyNames();
 	}
 
 	/**
 	 * Parses the style into list of key - value pairs
 	 *
 	 * @param style String representation of style
 	 * @return list of key - value pairs
 	 */
 	public static Properties parseStyle(String style) {
 		Properties result = new Properties();
 
 		style = style.replace("\r", "").replace("\n", "").replace("\t", "");
 		Pattern p = Pattern.compile("[^:;]+:([^;\"]|\"[^\"]+\")*;?");
 		Matcher m = p.matcher(style);
 
 		while (m.find()) {
 			String prop = m.group().trim();
 			if (prop.endsWith(";")) {
 				prop = prop.substring(0, prop.length() - 1);
 			}
 
 			String[] split = prop.split(":");
 			result.setProperty(split[0].trim(), split[1].trim());
 		}
 
 		return result;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toString() {
 		return attributes.toString();
 	}
 }
 
