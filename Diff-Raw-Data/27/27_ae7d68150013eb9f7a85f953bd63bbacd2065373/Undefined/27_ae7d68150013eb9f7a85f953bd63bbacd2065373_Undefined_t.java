 /**
  * Copyright (C) 2011 Primitive Team <jpalka@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.primitive.javascript.core;
 
 import java.util.Enumeration;
 import java.util.Iterator;
import java.util.Map;
 import java.util.Map.Entry;
 
public class Undefined implements Scriptable {
 
 	public static final Undefined Value = new Undefined();
 
 	private Undefined() {
 	}
 
 	@Override
 	public Scriptable getPrototype() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setPrototype(Scriptable prototype) {
 		// TODO Auto-generated method stub

 	}
 
 	@Override
 	public String getClassname() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isExtensible() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean hasProperty(String propertyName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Object get(String propertyName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public PropertyDescriptor getOwnProperty(String propertyName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public PropertyDescriptor getProperty(String propertyName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean canPut(String propertyName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void put(String propertyName, Object value) {
 		// TODO Auto-generated method stub

 	}
 
 	@Override
	public boolean defineOwnProperty(String propertyName, PropertyDescriptor propertyDescriptor, boolean failureHandling) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean delete(String propertyName, boolean failureHandling) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Object getDefaultValue(Class<?> hint) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public Enumeration<String> enumeration() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public Map<String, PropertyDescriptor> getOwnProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	@Override
	public void setExtensible(boolean b) {
		// TODO Auto-generated method stub

	}

 }
