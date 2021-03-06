 /*
  * Scriptographer
  *
  * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
  *
  * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
  * All rights reserved.
  *
  * Please visit http://scriptographer.com/ for updates and contact.
  *
  * -- GPL LICENSE NOTICE --
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  * -- GPL LICENSE NOTICE --
  *
  * File created on 02.01.2005.
  *
  * $Id: UnsealedJavaObject.java 238 2007-02-16 01:09:06Z lehni $
  */
 
 package com.scratchdisk.script.rhino;
 
 import java.util.*;
 
 import org.mozilla.javascript.*;
 
 /**
  * @author lehni
  */
 public class ExtendedJavaObject extends NativeJavaObject {
 	HashMap properties;
 	ExtendedJavaClass classWrapper = null;
 	
 	/**
 	 * @param scope
 	 * @param javaObject
 	 * @param staticType
 	 */
 	public ExtendedJavaObject(Scriptable scope, Object javaObject,
 		Class staticType, boolean unsealed) {
 		super(scope, javaObject, staticType);
 		properties = unsealed ? new HashMap() : null;
 		classWrapper = staticType != null ?
 				ExtendedJavaClass.getClassWrapper(scope, staticType) : null;
 	}
 
 	public Scriptable getPrototype() {
 		Scriptable prototype = super.getPrototype();
 	    if (prototype == null && classWrapper != null) {
 	    	prototype = classWrapper.getInstancePrototype();
 	    }
 	    return prototype;
 	}
 
 	public Object get(String name, Scriptable start) {
 		// Properties need to come first, as they might override something
 		// defined in the underlying Java object
 		if (properties != null && properties.containsKey(name)) {
 			// See wether this object defines the property.
 			return properties.get(name);
 		} else {
 			// Careful: We cannot on members.has, as this does not
 			// check static fields the way members.get does...
 			Object res = members.get(this, name, javaObject, false);
 			if (res != null && res != Scriptable.NOT_FOUND) return res;
 			Scriptable prototype = getPrototype();
 			if (name.equals("prototype")) {
 				if (prototype == null) {
 					// If no prototype object was created it, produce it on the fly.
 					prototype = new NativeObject();
 					setPrototype(prototype);
 				}
 				return prototype;
 			} else if (prototype != null && prototype.has(name, start)) {
 				// If not, see wether the prototype maybe defines it.
 				// NativeJavaObject misses to do so:
 				return prototype.get(name, start);
 			}
 			return Scriptable.NOT_FOUND;
 			/*
 			// TODO: What does fieldAndMethods do? Is it needed?
 			else if (fieldAndMethods != null && fieldAndMethods.containsKey(name)) {
 				// Static field or method?
 				return fieldAndMethods.get(name);
 			}
 			*/
 		}
 	}
 	
 	public void put(String name, Scriptable start, Object value) {
 		RuntimeException exc = null;
         if (members.has(name, false)) {
 			try {
 		        // We could be asked to modify the value of a property in the
 		        // prototype. Since we can't add a property to a Java object,
 		        // we modify it in the prototype rather than copy it down.
 	            members.put(this, name, javaObject, value, false);
 				return; // done
 			} catch (RuntimeException e) {
 				exc = e;
 			}
 		}
 		// still here? let's try oter things
 		if (name.equals("prototype")) {
 			if (value instanceof Scriptable)
 				setPrototype((Scriptable) value);
 		} else if (properties != null) {
 			properties.put(name, value);
 		} else {
 			// if nothing worked, throw the error again
 			if (exc != null)
 				throw exc;
 		}
 	}
 	
 	public boolean has(String name, Scriptable start) {
 		return members.has(name, false) ||
 				properties != null && properties.containsKey(name);
 	}
 
 	public void delete(String name) {
 		if (properties != null)
 			properties.remove(name);
 	}
 	
 	public Object[] getIds() {
 		// concatenate the super classes ids array with the keySet from
 		// properties HashMap:
 		Object[] javaIds = super.getIds();
 		if (properties != null) {
 			int numProps = properties.size();
 			if (numProps == 0)
 				return javaIds;
 			Object[] ids = new Object[javaIds.length + numProps];
 			Collection propIds = properties.keySet();
 			propIds.toArray(ids);
 			System.arraycopy(javaIds, 0, ids, numProps, javaIds.length);
 			return ids;
 		} else {
 			return javaIds;
 		}
 	}
 }
