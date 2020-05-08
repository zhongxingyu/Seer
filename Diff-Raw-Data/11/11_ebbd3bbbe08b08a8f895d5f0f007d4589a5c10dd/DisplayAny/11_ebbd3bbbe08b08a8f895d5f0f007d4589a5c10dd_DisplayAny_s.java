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
 
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 
 import com.webobjects.appserver.*;
 import com.webobjects.foundation.*;
 
 public class DisplayAny extends ExtDynamicElement {
 
 	public DisplayAny(String name, NSDictionary associations, WOElement template) {
 		super(name, associations, template);
 		
 		checkRequired(associations, "value");
 	}
 	
 	protected NSMutableDictionary associationsFromBindingsDict(NSDictionary bindings) {
 		NSMutableDictionary associations = new NSMutableDictionary();
 		if(bindings != null && bindings.count() > 0) {
 			WOAssociation valueBinding = (WOAssociation)bindingsDict.valueForKey("value");
 			Enumeration enu = bindings.keyEnumerator();
 			while (enu.hasMoreElements()) {
 				String key = (String) enu.nextElement();
 				Object value = bindings.valueForKey(key);
 				if(".".equals(value)) {
 					associations.takeValueForKey(valueBinding, key);
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '.') {
 					String keyPath = valueBinding.keyPath() + value;
 					associations.takeValueForKey(
 							WOAssociation.associationWithKeyPath(keyPath), key);
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '$') {
 					String keyPath = ((String)value).substring(1);
 					associations.takeValueForKey(
 							WOAssociation.associationWithKeyPath(keyPath), key);
 				} else if((value instanceof String) && 
 						((String)value).charAt(0) == '^') {
 					associations.takeValueForKey(
 							WOAssociation.associationWithKeyPath((String)value), key);
 				} else {
 					associations.takeValueForKey(WOAssociation.associationWithValue(value), key);
 				}
 			}
 		}
 		return associations;
 	}
 	
 	protected WOElement getPresenter(NSDictionary dict) {
 		String presenterName = (String)dict.valueForKey("presenter");
 		NSDictionary bindings = (NSDictionary)dict.valueForKey("presenterBindings");
 		// prepare main Element
 		NSMutableDictionary associations = associationsFromBindingsDict(bindings);
 		if(presenterName == null) {
 			presenterName = "WOString";
 			if(associations.valueForKey("value") == null) {
 				WOAssociation valueBinding = (WOAssociation)bindingsDict.valueForKey("value");
 				String path = (String)dict.valueForKey("titlePath");
 				if(path != null) {
 					path = valueBinding.keyPath() + '.' + path;
 					valueBinding = WOAssociation.associationWithKeyPath(path);
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
 			associations = associationsFromBindingsDict(bindings);
 			if(presenterName == null || Character.isLowerCase(presenterName.charAt(0))) {
 				if(associations.valueForKey("elementName") == null) {
 					if(presenterName == null)
 						presenterName = "span";
 					associations.takeValueForKey(
 							WOAssociation.associationWithValue(presenterName), "elementName");
 				}
 				presenterName = "WOGenericElement";
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
 				value = valueForBinding("value", aContext);
 				if(value == null)
 					return;
 				String path = (String)dict.valueForKey("titlePath");
 				if(path != null)
 					value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, path);
 				value = WOMessage.stringByEscapingHTMLString(value.toString());
 			} else {
 				value = null;
 			}
 		}
 		if(value != null) {
 			aResponse.appendContentString(value.toString());
 		} else {
 			WOElement presenter = getPresenter(dict);
 			presenter.appendToResponse(aResponse, aContext);
 		}
 	}
 	
 	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
 		NSDictionary dict = (NSDictionary)valueForBinding("dict", aContext);
 		if(dict != null && Various.boolForObject(dict.valueForKey("invokeAction"))) {
 			WOElement presenter = getPresenter(dict);
 			WOActionResults result = presenter.invokeAction(aRequest, aContext);
 			return result;
 		}
 		return super.invokeAction(aRequest, aContext);
 	}
 	
 	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
 		NSDictionary dict = (NSDictionary)valueForBinding("dict", aContext);
 		if(dict != null && Various.boolForObject(dict.valueForKey("takeValuesFromRequest"))) {
 			WOElement presenter = getPresenter(dict);
 			presenter.takeValuesFromRequest(aRequest, aContext);
 		}
 		super.takeValuesFromRequest(aRequest, aContext);
 	}
 	
     public static class ValueReader implements NSKeyValueCodingAdditions {
     	protected WOComponent page;
 
     	public static Object evaluateDict(NSDictionary dict, String objectPath, WOComponent page) {
 			Object tmp = dict.valueForKey("methodName");
 			if(tmp == null)
 				return dict;
 			String methodName = (String)evaluateValue(tmp,objectPath, page);
 			try {
 				Method method = (Method)dict.valueForKey("parsedMethod");
 				if(method == null) {
					tmp = dict.valueForKey("object");
					Object object = evaluateValue(tmp,objectPath,page);
 					Class aClass = (object == null) ? null : object.getClass();
 					if (aClass == null) {
 						tmp = dict.valueForKey("class");
 						aClass = (Class) evaluateValue(tmp, objectPath, page);
 						if (aClass == null) {
 							tmp = dict.valueForKey("className");
 							if(tmp == null)
 								return dict;
 							String className = (String) evaluateValue(tmp,objectPath, page);
 							aClass = Class.forName(className);
 						}
 					}
 					Class[] params = null;
 					NSArray paramNames = (NSArray)dict.valueForKey("paramClasses");
 					if(paramNames != null && paramNames.count() > 0) {
 						params = new Class[paramNames.count()];
 						for (int i = 0; i < params.length; i++) {
 							tmp = paramNames.objectAtIndex(i);
 							String className = (String) evaluateValue(tmp,objectPath, page);
 							params[i] = Class.forName(className);
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
 				}
 				Object[] values = null;
 				NSArray paramNames = (NSArray)dict.valueForKey("paramValues");
 				if(paramNames != null && paramNames.count() > 0) {
 					values = new Object[paramNames.count()];
 					for (int i = 0; i < values.length; i++) {
 						tmp = paramNames.objectAtIndex(i);
 						values[i] = evaluateValue(tmp,objectPath, page);
 					}
 				}
				Object result = method.invoke(values);
 //				tmp = dict.valueForKey("cacheResult");
 //				if(Various.boolForObject(tmp)) {
 //					dict.takeValueForKey(result, "cachedResult");
 //				}
 				return result;
 			} catch (Exception e) {
 				throw new NSForwardException(e,"Error parsing method for dict: " + dict);
 			}
     	}
 
     	public static Object evaluateValue(Object inPlist, String objectPath, WOComponent page) {
     		if (inPlist instanceof String) {
 				String keyPath = (String) inPlist;
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
 					keyPath = keyPath.substring(idx);
 					return NSKeyValueCodingAdditions.Utility.valueForKeyPath(binding, keyPath);
 				}
 				if(keyPath.charAt(0) == '.') {
 					if(keyPath.length() > 1)
 						return page.valueForKeyPath(objectPath + keyPath);
 					else
 						return page.valueForKey(objectPath);
 				}
 			}
     		if (inPlist instanceof NSDictionary) { // invoke method described in dict
     			NSDictionary dict = (NSDictionary)inPlist;
     			return evaluateDict(dict, objectPath, page);
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
