 // DisplayAny.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.logging.Logger;
 
 import com.webobjects.appserver.*;
 import com.webobjects.eocontrol.EOEnterpriseObject;
 import com.webobjects.foundation.*;
 
 public class DisplayAny extends ExtDynamicElement {
 
 	public DisplayAny(String name, NSDictionary associations, WOElement template) {
 		super(name, associations, template);
 //		checkRequired(associations, "value");
 	}
 	
 	protected NSMutableDictionary associationsFromBindingsDict(
 			NSDictionary bindings, Object dictValue) {
 		NSMutableDictionary associations = new NSMutableDictionary();
 		if(bindings != null && bindings.count() > 0) {
 			WOAssociation valueBinding = (WOAssociation)bindingsDict.valueForKey("value");
 			if(dictValue != null)
 				valueBinding = new DictAssociation(valueBinding, dictValue);
 			Enumeration enu = bindings.keyEnumerator();
 			while (enu.hasMoreElements()) {
 				String key = (String) enu.nextElement();
 				Object value = bindings.valueForKey(key);
 				WOAssociation as = null;
 				if(".".equals(value)) {
 					as = valueBinding;
 					//associations.takeValueForKey(valueBinding, key);
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '\'') {
 					String keyPath = ((String)value).substring(1);
 					as = WOAssociation.associationWithValue(keyPath);
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '.') {
 					if(valueBinding.isValueConstant()) {
 						Object bind = valueBinding.valueInComponent(null);
 						if(bind != null)
 							bind = NSKeyValueCodingAdditions.Utility.valueForKeyPath(bind,
 									((String)value).substring(1));
 						as = WOAssociation.associationWithValue(bind);
 					} else {
 						String keyPath = valueBinding.keyPath() + value;
 						as = WOAssociation.associationWithKeyPath(keyPath);
 					}
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '$') {
 					String keyPath = ((String)value).substring(1);
 					as = WOAssociation.associationWithKeyPath(keyPath);
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '^') {
 					String keyPath = ((String)value).substring(1);
 					int idx = keyPath.indexOf('.'); 
 					if(idx > 0) {
 						as = (WOAssociation)bindingsDict.valueForKey(keyPath.substring(0,idx));
 						keyPath = keyPath.substring(idx);
 						if(as.isValueConstant()) {
 							Object bind = as.valueInComponent(null);
 							bind = NSKeyValueCodingAdditions.Utility.valueForKeyPath(bind,
 									keyPath.substring(1));
 							as = WOAssociation.associationWithValue(bind);
 						} else {
 							as = WOAssociation.associationWithKeyPath(as.keyPath() + keyPath);
 						}
 					} else
 						as = (WOAssociation)bindingsDict.valueForKey(keyPath);
 				} else {
 					as = WOAssociation.associationWithValue(value);
 				}
 				associations.takeValueForKey(as, key);
 			}
 		}
 		return associations;
 	}
 	
 	protected WOElement getPresenter(NSDictionary dict) {
 		String presenterName = (String)dict.valueForKey("presenter");
 		Object dictValue = dict.valueForKey("value");
 		NSDictionary bindings = (NSDictionary)dict.valueForKey("presenterBindings");
 		// prepare main Element
 		NSMutableDictionary associations = associationsFromBindingsDict(bindings, dictValue);
 		if(presenterName == null) {
 			presenterName = "WOString";
 			if(associations.valueForKey("value") == null) {
 				WOAssociation valueBinding = (WOAssociation)bindingsDict.valueForKey("value");
 				String path = (String)dict.valueForKey("titlePath");
 				if(path != null) {
 					if(valueBinding.isValueConstant()) {
 						Object value = valueBinding.valueInComponent(null);
 						value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, path);
 						valueBinding = WOAssociation.associationWithValue(value);
 					} else {
 						path = valueBinding.keyPath() + '.' + path;
 						valueBinding = WOAssociation.associationWithKeyPath(path);
 					}
 				} else {
 					if(dictValue != null) 
 						valueBinding = new DictAssociation(valueBinding,dictValue);
 				}
 				associations.takeValueForKey(valueBinding, "value");
 			}
 		}
 		WOElement presenter = WOApplication.application().dynamicElementWithName(
 				presenterName, associations, children, null);
 
 		// make wrapper
 		presenterName = (String)dict.valueForKey("wrapper");
 		bindings = (NSDictionary)dict.valueForKey("wrapperBindings");
 		if(presenterName != null || (bindings != null && bindings.count() > 0)) {
 			associations = associationsFromBindingsDict(bindings, dictValue);
 			if(presenterName == null || Character.isLowerCase(presenterName.charAt(0))) {
 				if(associations.valueForKey("elementName") == null) {
 					if(presenterName == null)
 						presenterName = "span";
 					associations.takeValueForKey(
 							WOAssociation.associationWithValue(presenterName), "elementName");
 				}
 				presenterName = "WOGenericContainer";
 			}
 			presenter = WOApplication.application().dynamicElementWithName(
 					presenterName, associations, presenter, null);
 		}
 		return presenter;
 	}
 
 	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
 		NSDictionary dict = (NSDictionary)valueForBinding("dict", aContext);
 		Object value = null;
 		if(dict == null || dict.count() == 0) {
 			value = valueForBinding("value", aContext);
 		} else {
 			value = dict.valueForKey("presenter");
 			if(value == null)
 				value = dict.valueForKey("presenterBindings");
 			if(value == null)
 				value = dict.valueForKey("wrapper");
 			if(value == null)
 				value = dict.valueForKey("wrapperBindings");
 			
 			if(value == null) {
 				value = dict.valueForKey("value");
 				if(value != null)
 					value = ValueReader.evaluateValue(value, 
 							valueForBinding("value", aContext), aContext.component());
 				else
 					value = valueForBinding("value", aContext);
 				if(value == null) {
 					value = valueForBinding("valueWhenEmpty", aContext);
 					if(value != null) {
 						aResponse.appendContentString(value.toString());
 					} else {
 						value = dict.valueForKey("valueWhenEmpty");
 						if(value != null)
 							aResponse.appendContentString(value.toString());
 					}
 					return;
 				}
 				String path = (String)dict.valueForKey("titlePath");
 				if(path != null)
 					value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, path);
 				path = (String)dict.valueForKey("format");
 				if(path != null) {
 					value = formatObject(path, value);
 				} else if(Various.boolForObject(dict.valueForKey("condFormat"))) {
 					if(value instanceof EOEnterpriseObject) {
 						path = ((EOEnterpriseObject)value).entityName();
 					} else {
 						path = value.getClass().getName();
 						int dot = path.lastIndexOf('.');
 						if(dot > 0)
 							path = path.substring(dot +1);
 					}
 					path = (String)dict.valueForKey("format" + path);
 					if(path != null)
 						value = formatObject(path, value);
 				}
 				path = (value == null)?"":value.toString();
 				value = dict.valueForKey("escapeHTML");
 				if(value == null || Various.boolForObject(value))
 					value = WOMessage.stringByEscapingHTMLString(path);
 				else
 					value = path;
 			} else {
 				value = null;
 			}
 		}
 		if(value != null) {
 			aResponse.appendContentString(value.toString());
 		} else {
 			if(dict == null) {
 				value = valueForBinding("valueWhenEmpty", aContext);
 				if(value != null)
 					aResponse.appendContentString(value.toString());
 				return;
 			}
 			WOElement presenter = getPresenter(dict);
 			if(presenter == null) {
 				value = valueForBinding("valueWhenEmpty", aContext);
 				if(value != null)
 					aResponse.appendContentString(value.toString());
 				return;
 			}
 			String prID = presenterID(dict);
 			if(prID != null)
 				aContext.appendElementIDComponent(prID);
 			presenter.appendToResponse(aResponse, aContext);
 			if(prID != null)
 				aContext.deleteLastElementIDComponent();
 		}
 	}
 	
 	protected static String presenterID(NSDictionary dict) {
 		String wrID = (String)dict.valueForKey("wrapper");
 		String prID = (String)dict.valueForKey("presenter");
 		if(wrID != null)
 			wrID = wrID.replace('.', '_');
 		if(prID != null) {
 			prID = prID.replace('.', '_');
 			if(wrID != null)
 				return wrID + "__" + prID;
 			else
 				return prID;
 		} else {
 			return wrID;
 		}
 	}
 	
 	public static String formatObject(String format, Object obj) {
 		int idx = format.indexOf("%[");
 		int end = -1;
 		NSMutableArray<String> keyset = new NSMutableArray<String>();
 		StringBuilder buf = new StringBuilder();
 		while (idx >= 0) {
 			buf.append(format.substring(end +1, idx +1));
 			end = format.indexOf(']',idx);
 			if(end < 0) {
 				idx = format.indexOf("%[",idx+2);
 				continue;
 			}
 			String sub = format.substring(idx + 2, end);
 			if(format.charAt(end +1) == '$') {
 				int keyidx = keyset.indexOf(sub);
 				if(keyidx < 0) {
 					keyset.addObject(sub);
 					buf.append(keyset.count());
 				} else {
 					buf.append(keyidx +1);
 				}
 			} else {
 				keyset.addObject(sub);
 			}
 			idx = format.indexOf("%[",end);
 		}
 		if(end < format.length() -1)
 			buf.append(format.substring(end +1));
 		Object[] keys = keyset.toArray();
 		for (int i = 0; i < keys.length; i++) {
 			String key = (String)keys[i];
 			keys[i] = NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, key);
 		}
 		return String.format(buf.toString(), keys);
 	}
 	
 	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
 		NSDictionary dict = (NSDictionary)valueForBinding("dict", aContext);
 		if(dict != null && Various.boolForObject(dict.valueForKey("invokeAction"))) {
 			String prID = presenterID(dict);
 			if(prID != null)
 				aContext.appendElementIDComponent(prID);
 			String pageName = (String)dict.valueForKey("nextPage");
 			WOActionResults result = null;
 			if(pageName != null && aContext.senderID().equals(aContext.elementID())) {
 				result = WOApplication.application().pageWithName(pageName, aContext);
 				NSDictionary pageParams = (NSDictionary)dict.valueForKey("pageParams");
 				if(pageParams != null && pageParams.count() > 0) {
 					Enumeration enu = pageParams.keyEnumerator();
 					while (enu.hasMoreElements()) {
 						String key = (String)enu.nextElement();
 						Object param = pageParams.valueForKey(key);
 						if(param instanceof String) {
 							String str = (String)param;
 							if(str.charAt(0) == '^')
 								param = valueForBinding(str.substring(1), aContext);
 							else
 								param = ValueReader.evaluateValue(param,
 										valueForBinding("value", aContext), aContext.page());
 						}
 						((WOComponent)result).takeValueForKey(param, key);
 					}
 				}
 			} else {
 				WOElement presenter = getPresenter(dict);
 				if(presenter != null)
 					result = presenter.invokeAction(aRequest, aContext);
 			}
 			if(prID != null)
 				aContext.deleteLastElementIDComponent();
 			return result;
 		}
 		return super.invokeAction(aRequest, aContext);
 	}
 	
 	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
 		NSDictionary dict = (NSDictionary)valueForBinding("dict", aContext);
 		if(dict != null && Various.boolForObject(dict.valueForKey("takeValuesFromRequest"))) {
 			WOElement presenter = getPresenter(dict);
 			if(presenter != null) {
 				String prID = presenterID(dict);
 				if(prID != null)
 					aContext.appendElementIDComponent(prID);
 				presenter.takeValuesFromRequest(aRequest, aContext);
 				if(prID != null)
 					aContext.deleteLastElementIDComponent();
 			}
 		}
 		super.takeValuesFromRequest(aRequest, aContext);
 	}
 	
 	public static class DictAssociation extends WOAssociation {
 		
 		protected Object dict;
 		WOAssociation sup;
 		
 		public DictAssociation(WOAssociation parent,Object dictValue) {
 			super();
 			dict = dictValue;
 			sup = parent;
 		}
 
 		public Object valueInComponent(WOComponent aComponent) {
 			Object value = sup.valueInComponent(aComponent);
 			return ValueReader.evaluateValue(dict, value, aComponent);
 		}
 		public boolean isValueSettable() {
 			return false;
 		}
 		public boolean isValueConstant() {
 			return sup.isValueConstant();
 		}
 		public boolean isValueSettableInComponent(WOComponent aComponent) {
 			return sup.isValueSettableInComponent(aComponent);
 		}
 		public boolean isValueConstantInComponent(WOComponent aComponent) {
 			return sup.isValueConstantInComponent(aComponent);
 		}
 		public String bindingInComponent(WOComponent arg0) {
 			return sup.bindingInComponent(arg0);
 		}
 		public String keyPath() {
 			return sup.keyPath();
 		}
 		public void setDebugEnabledForBinding(String aBindingName,
                 String aDeclarationName,String aDeclarationType) {
 			sup.setDebugEnabledForBinding(aBindingName, aDeclarationName, aDeclarationType);
 		}
 		public void setValue(Object aValue, WOComponent aComponent) {
 			throw new UnsupportedOperationException("Calculated value can not be set");
 		}
 	}
 	
     public static class ValueReader implements NSKeyValueCodingAdditions {
     	protected WOComponent page;
 
     	public static Object evaluateDict(NSDictionary dict, 
     			Object refObject, WOComponent page) {
 			Object tmp = dict.valueForKey("dict");
 			if(tmp instanceof NSDictionary) {
 				NSDictionary values = (NSDictionary)tmp;
 				tmp = dict.valueForKey("key");
 				if(tmp != null) {
 					tmp = evaluateValue(tmp,refObject, page);
 				}
 				if(tmp == null) {
 					if(refObject == null)
 						return null;
 					tmp = refObject;
 				}
 				String key = tmp.toString();
 				Object result = values.valueForKey(key);
 				return result;
 			}
 			tmp = dict.valueForKey("methodName");
 			if(tmp == null)
 				return dict;
 			if(refObject == null && Various.boolForObject(dict.valueForKey("nullNull")))
 				return null;
 			String methodName = (String)evaluateValue(tmp,refObject, page);
 			NSMutableDictionary resultCache = (NSMutableDictionary)dict
 					.objectForKey("resultCache");
 			if(resultCache != null) {
 				if(refObject instanceof String) {
 					try {
 						tmp = page.valueForKeyPath((String)refObject);
 					} catch (UnknownKeyException e) {
 						tmp = refObject;
 					}
 				} else {
 					tmp = refObject;
 				}
 				if(tmp != null) {
 					Object res = resultCache.objectForKey(tmp);
 					if(res != null) {
 						if(res instanceof EOEnterpriseObject &&
 								((EOEnterpriseObject)res).editingContext() == null)
 							resultCache.removeObjectForKey(tmp);
 						else
 							return res;
 					}
 				}
 			}
 			try {
 				tmp = dict.valueForKey("object");
 				Object object = evaluateValue(tmp,refObject,page);
 				Method method = (Method)dict.valueForKey("parsedMethod");
 				Class[] params = null;
 				if(method == null) {
 					Class aClass = (object == null) ? null : object.getClass();
 					if (aClass == null) {
 						tmp = dict.valueForKey("class");
 						aClass = (Class) evaluateValue(tmp, refObject, page);
 						if (aClass == null) {
 							tmp = dict.valueForKey("className");
 							if(tmp == null)
 								return dict;
 							String className = (String) evaluateValue(tmp,refObject, page);
 							aClass = Class.forName(className);
 						}
 					}
 					NSArray paramNames = (NSArray)dict.valueForKey("paramClasses");
 					if(paramNames != null && paramNames.count() > 0) {
 						params = new Class[paramNames.count()];
 						for (int i = 0; i < params.length; i++) {
 							tmp = paramNames.objectAtIndex(i);
 							String className = (String) evaluateValue(tmp,refObject, page);
 							params[i] = primitiveClassForName(className);
 						}
 					}
 
 					method = aClass.getMethod(methodName, params);
 					tmp = dict.valueForKey("cacheMethod");
 					if(Various.boolForObject(tmp)) {
 						try {
 							dict.takeValueForKey(method, "parsedMethod");
 						} catch (Exception e) {
 							// Could not cache...
 						}
 					}
 				} else {
 					params = method.getParameterTypes();
 				}
 				Object[] values = null;
 				NSArray paramNames = (NSArray)dict.valueForKey("paramValues");
 				if(paramNames != null && paramNames.count() > 0) {
 					values = new Object[paramNames.count()];
 					for (int i = 0; i < values.length; i++) {
 						tmp = paramNames.objectAtIndex(i);
 						values[i] = coerceToClass(
 								evaluateValue(tmp,refObject, page),params[i]);
 					}
 				}
 				Object result = method.invoke(object,values);
 				tmp = dict.valueForKey("cacheResult");
 				if(Various.boolForObject(tmp)) {
 					if(refObject instanceof String) {
 						try {
 							tmp = page.valueForKeyPath((String)refObject);
 						} catch (UnknownKeyException e) {
 							tmp = refObject;
 						}
 					} else {
 						tmp = refObject;
 					}
 					if(tmp == null)
 						return result;
 					if(resultCache != null) {
 						if(result == null)
 							resultCache.removeObjectForKey(tmp);
 						else
 							resultCache.setObjectForKey(result, tmp);
 					} else if(result != null) {
 						resultCache = new NSMutableDictionary();
 						dict.takeValueForKey(resultCache, "resultCache");
 					}
 
 				}
 				return result;
 			} catch (Exception e) {
 				throw new NSForwardException(e,"Error parsing method for dict: " + dict);
 			}
     	}
     	
     	public static Class primitiveClassForName(String className) 
     									throws ClassNotFoundException {
 			if(className.equals("int"))
 				return Integer.TYPE;
 			else if (className.equals("boolean"))
 				return Boolean.TYPE;
 			else if (className.equals("byte"))
 				return Byte.TYPE;
 			else if (className.equals("char"))
 				return Character.TYPE;
 			else if (className.equals("short"))
 				return Short.TYPE;
 			else if (className.equals("long"))
 				return Long.TYPE;
 			else if (className.equals("float"))
 				return Float.TYPE;
 			else if (className.equals("double"))
 				return Double.TYPE;
 			else
 				return Class.forName(className);
     	}
     	
     	public static Object coerceToClass(Object obj, Class cl) throws Exception{
     		if(obj == null || cl.isInstance(obj))
     			return obj;

     		if(cl == Integer.TYPE || cl == Integer.class) {
     			if(obj instanceof Number)
     				return new Integer(((Number)obj).intValue());
     			else if (obj instanceof String)
     				return new Integer((String)obj);
     		} else if(cl == Character.TYPE || cl == Character.class) {
     			return new Character(obj.toString().charAt(0));
     		} else if(cl == Boolean.TYPE || cl == Boolean.class) {
     			return new Boolean(Various.boolForObject(obj));
    		} else if(Number.class.isAssignableFrom(cl) || cl.isPrimitive()) {
    			Constructor cn = cl.getConstructor(String.class);
    			return cn.newInstance(obj.toString());
     		} else if(cl == Long.TYPE || cl == Long.class) {
     			if(obj instanceof Number)
     				return new Long(((Number)obj).longValue());
     			else if (obj instanceof String)
     				return new Long((String)obj);
     		} else if(cl == Double.TYPE || cl == Double.class) {
     			if(obj instanceof Number)
     				return new Double(((Number)obj).doubleValue());
     			else if (obj instanceof String)
     				return new Double((String)obj);
     		} else if(cl == Float.TYPE || cl == Float.class) {
     			if(obj instanceof Number)
     				return new Float(((Number)obj).floatValue());
     			else if (obj instanceof String)
     				return new Float((String)obj);
     		} else if(cl == Short.TYPE || cl == Short.class) {
     			if(obj instanceof Number)
     				return new Short(((Number)obj).shortValue());
     			else if (obj instanceof String)
     				return new Short((String)obj);
     		} else if(cl == Byte.TYPE || cl == Byte.class) {
     			if(obj instanceof Number)
     				return new Byte(((Number)obj).byteValue());
     			else if (obj instanceof String)
     				return new Byte((String)obj);
     		}
     		throw new IllegalArgumentException("Could not coerce argument of type" +
     				obj.getClass().getName() + " to required type" + cl.getName());
     	}
     	
     	public static void clearResultCache(NSMutableDictionary dict, 
     			Object onObject, boolean recursive) {
     		if(dict == null || dict.count() == 0)
     			return;
     		NSMutableDictionary resultCache = (NSMutableDictionary)dict.valueForKey(
     				"resultCache");
     		if(resultCache != null) {
     			if(onObject == null)
     				dict.removeObjectForKey("resultCache");
     			else
     				resultCache.removeObjectForKey(onObject);
     		}
     		if(recursive) {
     			Enumeration enu = dict.keyEnumerator();
     			while (enu.hasMoreElements()) {
 					Object key = enu.nextElement();
 					Object value = dict.objectForKey(key);
 					if(value instanceof NSMutableDictionary)
 						clearResultCache((NSMutableDictionary)value,onObject,recursive);
 				}
     			NSArray subParams = (NSArray)dict.valueForKey("subParams");
     			if(subParams != null && subParams.count() > 0) {
     				enu = subParams.objectEnumerator();
     				while (enu.hasMoreElements()) {
 						NSMutableDictionary param = (NSMutableDictionary) enu.nextElement();
 						clearResultCache(param,onObject,recursive);
 					}
     			}
     		}
     	}
 
     	public static Object evaluateValue(Object inPlist, Object refObject, WOComponent page) {
     		if (inPlist instanceof String) {
 				String keyPath = (String) inPlist;
 				if(keyPath.length() == 0 || keyPath.equalsIgnoreCase("null"))
 					return null;
 				if(keyPath.charAt(0) == '\'') {
 					return keyPath.substring(1);
 				}
 				if(keyPath.charAt(0) == '$') {
 					if(keyPath.length() > 1)
 						return page.valueForKeyPath(keyPath.substring(1));
 					else
 						return page;
 				}
 				if(keyPath.charAt(0) == '^') {
 					keyPath = keyPath.substring(1);
 					int idx = keyPath.indexOf('.');
 					if(idx < 0)
 						return page.valueForBinding(keyPath);
 					Object binding = page.valueForBinding(keyPath.substring(0,idx));
 					keyPath = keyPath.substring(idx +1);
 					return NSKeyValueCodingAdditions.Utility.valueForKeyPath(binding, keyPath);
 				}
 				if(keyPath.charAt(0) == '.') {
 					if(refObject instanceof String) {
 						try {
 							Object tmp = page.valueForKeyPath((String) refObject);
 							refObject = tmp;
 						} catch (UnknownKeyException e) {
 							;
 						}
 					}
 					if(keyPath.length() > 1 && refObject != null) {
 						try {
 							return NSKeyValueCodingAdditions.Utility.
 									valueForKeyPath(refObject, keyPath.substring(1));
 						} catch (NSKeyValueCoding.UnknownKeyException e) {
 							Logger.getLogger("rujel.reusables").log(WOLogLevel.WARNING,
 									"Erroneous dispayDict",new Object[] {page,e});
 						}
 					}
 						return refObject;
 				}
 			}
     		if (inPlist instanceof NSDictionary) { // invoke method described in dict
     			NSDictionary dict = (NSDictionary)inPlist;
     			return evaluateDict(dict, refObject, page);
     		}
     		return inPlist;    		
     	}
 
     	public ValueReader (WOComponent page) {
     		this.page = page;
     	}
     	
     	public void setPage(WOComponent page) {
     		this.page = page;
     	}
     	    	
     	public Object valueForKeyPath(String path) {
     		int idx = path.indexOf('.');
     		if(idx < 0)
     			throw new UnsupportedOperationException("Path required");
     		String referName = path.substring(0, idx);
     		path = path.substring(idx + 1);
     		Object inPlist = page.valueForKeyPath(path);
     		return evaluateValue(inPlist, referName, page);
     	}
 
     	public void takeValueForKeyPath(Object arg0, String arg1) {
     		throw new UnsupportedOperationException("Read only");								
     	}
     	public void takeValueForKey(Object arg0, String arg1) {
     		throw new UnsupportedOperationException("Read only");				
     	}
     	public Object valueForKey(String arg0) {
     		throw new UnsupportedOperationException("Path required");				
     	}
     }
 }
