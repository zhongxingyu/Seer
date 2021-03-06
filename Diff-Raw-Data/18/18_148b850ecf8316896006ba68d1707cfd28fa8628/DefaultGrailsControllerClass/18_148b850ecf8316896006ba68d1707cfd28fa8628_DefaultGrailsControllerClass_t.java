 /*
  * Copyright 2004-2005 the original author or authors.
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
 package org.codehaus.groovy.grails.commons;
 
 import groovy.lang.Closure;
 import groovy.lang.GroovyObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.codehaus.groovy.grails.scaffolding.GrailsScaffolder;
 import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
 
 import java.beans.PropertyDescriptor;
 import java.util.*;
 
 /**
  * 
  * 
  * @author Steven Devijver
  * @since Jul 2, 2005
  */
 public class DefaultGrailsControllerClass extends AbstractInjectableGrailsClass
 		implements GrailsControllerClass {
 
     public static final String CONTROLLER = "Controller";
 
     private static final String SLASH = "/";
     private static final String VIEW = "View";
     private static final String DEFAULT_CLOSURE_PROPERTY = "defaultAction";
 	private static final String SCAFFOLDING_PROPERTY = "scaffold";
 	private static final String ALLOWED_HTTP_METHODS_PROPERTY = "allowedMethods";
 
 	private static final String EXCEPT = "except";
 	private static final String ONLY = "only";
 	private static final String ACTION = "action";
 
 	
 
 	private Map uri2viewMap = null;
 	private Map uri2closureMap = null;
 	private Map viewNames = null;
 	private String[] uris = null;
     private String uri;
 
     private boolean scaffolding;
     private Class scaffoldedClass;
 
 
     public DefaultGrailsControllerClass(Class clazz) {
         super(clazz, CONTROLLER);
         this.uri = SLASH + (StringUtils.isNotBlank(getPackageName()) ? getPackageName().replace('.', '/')  + SLASH : "" ) + WordUtils.uncapitalize(getName());
         String defaultActionName = (String)getPropertyOrStaticPropertyOrFieldValue(DEFAULT_CLOSURE_PROPERTY, String.class);
         if(defaultActionName == null) {
             defaultActionName = INDEX_ACTION;
         }
         this.scaffoldedClass = (Class)getPropertyOrStaticPropertyOrFieldValue(SCAFFOLDING_PROPERTY, Class.class);
         if(this.scaffoldedClass != null) {
             this.scaffolding = true;
         }
         else {
         	if(getReference().isReadableProperty(SCAFFOLDING_PROPERTY)) {
             	Object tmp = getReference().getPropertyValue(SCAFFOLDING_PROPERTY);
             	if(tmp instanceof Boolean) {
                     this.scaffolding = ((Boolean)tmp).booleanValue();        		
             	}        		
         	}
         }
 
         Collection closureNames = new ArrayList();
         this.uri2viewMap = new HashMap();
         this.uri2closureMap = new HashMap();
         this.viewNames = new HashMap();
 
         if(this.scaffolding) {
             this.uri2closureMap.put(uri, defaultActionName);
             this.uri2closureMap.put(uri + SLASH, defaultActionName);
             this.uri2viewMap.put(uri + SLASH, uri + SLASH + defaultActionName);
             this.uri2viewMap.put(uri,uri + SLASH +  defaultActionName);
             this.viewNames.put( defaultActionName, uri + SLASH + defaultActionName );
             
             for(int i = 0; i < GrailsScaffolder.ACTION_NAMES.length;i++) {
                 closureNames.add(GrailsScaffolder.ACTION_NAMES[i]);
                 String viewName = (String)getPropertyOrStaticPropertyOrFieldValue(GrailsScaffolder.ACTION_NAMES[i] + VIEW, String.class);
                 // if no explicity view name is specified just use action name
                 if(viewName == null) {
                     viewName =GrailsScaffolder.ACTION_NAMES[i];
                 }
                 String tmpUri = uri + SLASH + GrailsScaffolder.ACTION_NAMES[i];
                 String viewUri = uri + SLASH + viewName;
                 if (StringUtils.isNotBlank(viewName)) {
                     this.uri2viewMap.put(tmpUri, viewUri);
                     this.viewNames.put( GrailsScaffolder.ACTION_NAMES[i], viewUri );
                 }
                 this.uri2closureMap.put(tmpUri, GrailsScaffolder.ACTION_NAMES[i]);
                 this.uri2closureMap.put(tmpUri + SLASH +"**", GrailsScaffolder.ACTION_NAMES[i]);
             }
         }
 
         PropertyDescriptor[] propertyDescriptors = getReference().getPropertyDescriptors();
         for (int i = 0; i < propertyDescriptors.length; i++) {
             PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
             Closure closure = (Closure)getPropertyOrStaticPropertyOrFieldValue(propertyDescriptor.getName(), Closure.class);
             if (closure != null) {
                 closureNames.add(propertyDescriptor.getName());
                 String closureName = propertyDescriptor.getName();
                 String viewName = (String)getPropertyOrStaticPropertyOrFieldValue(propertyDescriptor.getName() + VIEW, String.class);
                 // if no explicity view name is specified just use property name
                 if(viewName == null) {
                     viewName = propertyDescriptor.getName();
                 }
 
                 String tmpUri = uri + SLASH + propertyDescriptor.getName();
                 String viewUri = uri + SLASH + viewName;
 
                 uri2closureMap.put(tmpUri,closureName);
                 uri2closureMap.put(tmpUri + SLASH + "**",closureName);
                 if (StringUtils.isNotBlank(viewName)) {
                     this.uri2viewMap.put(tmpUri, viewUri);
                     this.viewNames.put( closureName, viewUri );
                 }
             }
         }
 
         if (getReference().isReadableProperty(defaultActionName)) {
             this.uri2closureMap.put(uri, defaultActionName);
             this.uri2closureMap.put(uri + SLASH, defaultActionName);
             this.uri2viewMap.put(uri + SLASH, uri + SLASH + defaultActionName);
             this.uri2viewMap.put(uri,uri + SLASH +  defaultActionName);
             this.viewNames.put( defaultActionName, uri + SLASH + defaultActionName );
         }
 
         if (closureNames.size() == 1) {
             String closureName = ((String)closureNames.iterator().next());
             this.uri2closureMap.put(uri, closureName);
             this.uri2closureMap.put(uri + SLASH, closureName);
             if (!this.uri2viewMap.isEmpty()) {
                 this.uri2viewMap.put(uri, this.uri2viewMap.values().iterator().next());
             }
         }
 
         this.uris  = (String[])this.uri2closureMap.keySet().toArray(new String[this.uri2closureMap.keySet().size()]);
     }
 
 	public String[] getURIs() {
 		return this.uris;
 	}
 
 	public boolean mapsToURI(String uri) {
 		for (int i = 0; i < uris.length; i++) {
 			if (uris[i].equals(uri)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public String getViewByURI(String uri) {
 		return (String)this.uri2viewMap.get(uri);
 	}
 	
 	public String getClosurePropertyName(String uri) {
 		return (String)this.uri2closureMap.get(uri);
 	}
 
 	public String getViewByName(String viewName) {
         if(this.viewNames.containsKey(viewName)) {
             return (String)this.viewNames.get(viewName);
         }
         else {
              return this.uri + SLASH + viewName;
         }
     }
 
 	public boolean isScaffolding() {
 		return this.scaffolding;
 	}
 
     public Class getScaffoldedClass() {
         return this.scaffoldedClass;
     }
 
     /**
      * @param scaffolding The scaffolding to set.
      */
     public void setScaffolding(boolean scaffolding) {
         this.scaffolding = scaffolding;
     }
 
 	public boolean isInterceptedBefore(GroovyObject controller, String action) {
 		final Map controllerProperties = DefaultGroovyMethods.getProperties(controller);
 		return isIntercepted(controllerProperties.get(BEFORE_INTERCEPTOR),action);
 	}
 
 	private boolean isIntercepted(Object bip, String action) {
 		if(bip instanceof Map) {
 			Map bipMap = (Map)bip;
 			if(bipMap.containsKey(EXCEPT)) {
 				Object excepts = bipMap.get(EXCEPT);
 				if(excepts instanceof String) {
 					if(!excepts.equals(action))
 						return true;							
 				}
 				else if(excepts instanceof List) {
 					if(!((List)excepts).contains(action))
 						return true;
 				}
 			}
 			else if(bipMap.containsKey(ONLY)) {
 				Object onlys = bipMap.get(ONLY);
 				if(onlys instanceof String) {
 					if(onlys.equals(action))
 						return true;
 				}
 				else if(onlys instanceof List) {
 					if(((List)onlys).contains(action))
 						return true;
 				}
 			}
 		}
 		else if(bip instanceof Closure) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isHttpMethodAllowedForAction(GroovyObject controller, String httpMethod, String actionName) {
 		boolean isAllowed = true;
 		final Map controllerProperties = DefaultGroovyMethods.getProperties(controller);
 		Object methodRestrictionsProperty = controllerProperties.get(ALLOWED_HTTP_METHODS_PROPERTY);
 		if(methodRestrictionsProperty instanceof Map) {
 			Map map = (Map)methodRestrictionsProperty;
 			if(map.containsKey(actionName)) {
 				Object value = map.get(actionName);
 				if(value instanceof List) {
 					List listOfMethods = (List) value;
 					isAllowed = listOfMethods.contains(httpMethod);
 				} else if(value instanceof String) {
 					isAllowed = value.equals(httpMethod);
 				}
 			}
 		}
 		return isAllowed;
 	}
 
 	public boolean isInterceptedAfter(GroovyObject controller, String action) {
 		final Map controllerProperties = DefaultGroovyMethods.getProperties(controller);
 		return isIntercepted(controllerProperties.get(AFTER_INTERCEPTOR),action);
 	}
 
 	public Closure getBeforeInterceptor(GroovyObject controller) {
        if(getReference().isReadableProperty(BEFORE_INTERCEPTOR)) {
            return getInterceptor(controller.getProperty(BEFORE_INTERCEPTOR));
        }
        return null;
 	}
 
 	public Closure getAfterInterceptor(GroovyObject controller) {
        if(getReference().isReadableProperty(AFTER_INTERCEPTOR)) {
            return getInterceptor(controller.getProperty(AFTER_INTERCEPTOR));
        }
        return null;
    }
 
 	private Closure getInterceptor(Object ip) {
 		if(ip instanceof Map) {
 			Map ipMap = (Map)ip;
 			if(ipMap.containsKey(ACTION)) {
 				return (Closure)ipMap.get(ACTION);
 			}
 		}
 		else if(ip instanceof Closure) {
 			return (Closure)ip;
 		}
 		return null;
 	}
 }
