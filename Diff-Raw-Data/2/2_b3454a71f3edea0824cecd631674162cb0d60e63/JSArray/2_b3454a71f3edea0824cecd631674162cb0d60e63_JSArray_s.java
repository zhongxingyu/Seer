 /**
  * Copyright (C) 2012 Primitive Team <jpalka@gmail.com>
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
 package net.primitive.javascript.core.natives;
 
 import static net.primitive.javascript.core.Convertions.toNumber;
 
 import java.util.List;
 
 import net.primitive.javascript.core.Constructor;
 import net.primitive.javascript.core.Convertions;
 import net.primitive.javascript.core.Function;
 import net.primitive.javascript.core.Scope;
 import net.primitive.javascript.core.Scriptable;
 import net.primitive.javascript.core.ScriptableObject;
 import net.primitive.javascript.core.Undefined;
 
 /**
  * 
  * Implementation of JavaScript Array constructor
  * 
  * @author jpalka@gmail.com
  */
 public class JSArray extends ScriptableObject implements Function, Constructor {
 
 	private final StandardObjects standardObjects;
 
 	public JSArray(StandardObjects standardObjects) {
 		super("Function");
 		this.standardObjects = standardObjects;
 	}
 
 	/**
 	 * Array.prototype.pop()
 	 * 
 	 * @param thisObject
 	 * @param objects
 	 * @return
 	 */
 	public static Object pop(Scriptable thisObj, Object[] args) {
 		double len = toNumber(thisObj.get("length"));
 		Object element = Undefined.Value;
 		if (len > 0) {
 			String indx = Convertions.toString(len - 1);
 			element = thisObj.get(indx);
 			thisObj.delete(indx, true);
 			thisObj.put("length", indx);
 		}
 		return element;
 	}
 
 	/**
 	 * Array.prototype.push()
 	 * 
 	 * @param thisObject
 	 * @param objects
 	 * @return
 	 */
 	public static Object push(Scriptable thisObj, Object[] args) {
 		Object lenVal = thisObj.get("length");
 		int n = Convertions.toUInt32(lenVal);
 		Object[] items = args;
 		for (Object e : items) {
 			thisObj.put(Convertions.toString(n), e);
 			n++;
 			thisObj.put("length", n);
 		}
 		return n;
 	}
 
 	@Override
 	public Object call(Scope scope, Scriptable thisObj, Object[] args) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object[] bindParameters(Object[] actualParameters) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Scope getScope() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Scriptable construct(Scope scope, Object[] args) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object hasInstance(Object lvar) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<String> getParameterList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
