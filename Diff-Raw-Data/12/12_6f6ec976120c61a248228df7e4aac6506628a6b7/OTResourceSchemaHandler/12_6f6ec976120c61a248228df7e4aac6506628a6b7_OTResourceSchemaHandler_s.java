 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Mar 21, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.otrunk;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.otrunk.datamodel.BlobResource;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.view.OTViewerHelper;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OTResourceSchemaHandler extends OTInvocationHandler
 {
 	public final static boolean traceListeners =
 		Boolean.getBoolean(OTViewerHelper.TRACE_LISTENERS_PROP);
 	
 	Class schemaInterface = null;
     OTrunkImpl db;
     OTObjectService objectService;
     
     Vector changeListeners = new Vector();
 
     /**
      * This is for debugging purposes it contains a mapping from
      * the weakreference object to the toString of the listener it
      * referenced.  This way when the listener is gc'd we can printout
      * its "label" (toString value).
      */
     Map changeListenerLabels;
     
     /**
      * This can be used by a user of an object to turn off the listening
      * 
      */
     protected boolean doNotifyListeners = true;
     
     /**
      * An internal variable to speed up skipping of the listener notification
      */
     protected boolean hasListeners = false;
     protected OTChangeEvent changeEvent;
     protected OTObject changeEventSource;
     
     /**
      * @param dataObject
      * @param db
      */
     public OTResourceSchemaHandler(OTDataObject dataObject, OTrunkImpl db, 
             OTObjectService objectService, Class schemaInterface)
     {
         super(dataObject);
         this.schemaInterface = schemaInterface; 
         this.db = db;
         this.objectService = objectService;
         // TODO Auto-generated constructor stub
     }
 
     public void setEventSource(OTObject src)
     {
     	changeEventSource = src;
     }
     
 	public Object handleGet(String resourceName, Class returnType, Class proxyClass)
 	throws Exception
 	{
 	    // Handle the globalId specially
 	    if(resourceName.equals("globalId")) {
 	        return dataObject.getGlobalId();
 	    }
 	    
         if(resourceName.equals("oTObjectService")) {
             return objectService;
         }
         
 	    Object resourceValue = dataObject.getResource(resourceName);
 	    
 	    // we can't rely on the returnType here because it could be an
 	    // interface that isn't in the ot package
 	    if(resourceValue instanceof OTID){
 	        OTObject object;
 	        try {
 	            if(resourceValue == null) {
 	                return null;
 	            }
 	            OTID objId = (OTID)resourceValue;
 	            
 	            object = (OTObject)objectService.getOTObject(objId);
 	            
 	            return object;
 	        } catch (Exception e)
 	        {
 	            e.printStackTrace();
 	        }		
 	        
 	        return null;
 	        
 	    } else if(OTResourceMap.class.isAssignableFrom(returnType)) {
 	        try {					
 	            OTDataMap map = (OTDataMap)dataObject.getResourceCollection(
 	                    resourceName, OTDataMap.class);
 	            return new OTResourceMapImpl(resourceName, map, this);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;
 	    } else if(OTObjectMap.class.isAssignableFrom(returnType)) {
 	        try {					
 	            OTDataMap map = (OTDataMap)dataObject.getResourceCollection(
 	                    resourceName, OTDataMap.class);
 	            return new OTObjectMapImpl(resourceName, map, this, objectService);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;				
 	    } else if(OTResourceList.class.isAssignableFrom(returnType)) {
 	        try {	        	
 	            OTDataList list = (OTDataList)dataObject.getResourceCollection(
 	                    resourceName, OTDataList.class);
 	            return new OTResourceListImpl(resourceName, list, this);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;				
 	    } else if(OTObjectList.class.isAssignableFrom(returnType)) {
 	        try {					
 	        	OTDataList list = (OTDataList)dataObject.getResourceCollection(
 	                    resourceName, OTDataList.class);
 	            return new OTObjectListImpl(resourceName, list, this, 
 	            		objectService);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;	
 	    } else if(resourceValue instanceof BlobResource) {
 	    	BlobResource blob = (BlobResource)resourceValue;
 	    	if(returnType == byte[].class){
 	    		return blob.getBytes();
 	    	} else if(returnType == URL.class){
 	    		return blob.getBlobURL();
 	    	}
 	    } else if(resourceValue == null && returnType.isPrimitive()) {
 	        try {
 	            Field defaultField = proxyClass.getField("DEFAULT_" + resourceName);
 	            if(defaultField != null) {
 	                return defaultField.get(null);
 	            }
 	        } catch (NoSuchFieldException e) {
 	            throw new RuntimeException("No default value set for \"" + resourceName + "\" " +
 		                "in class: " + schemaInterface);
 	        }
 	    }
 	    
 	    if(resourceValue == null) return null;
 	    
 	    if(!returnType.isInstance(resourceValue) &&
 	            !returnType.isPrimitive()){
 	        System.err.println("invalid resource value for: " + resourceName);
 	        System.err.println("  object type: " + 
 	        		OTrunkImpl.getClassName(dataObject));
 	        System.err.println("  resourceValue is: " + resourceValue.getClass());
 	        System.err.println("  expected type is: " + returnType);
 	        return null;
 	    }
 	    
 	    return resourceValue;
 	    
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
 	 */
 	public Object invoke(Object proxy, Method method, Object[] args)
 		throws Throwable
 	{
 		String methodName = method.getName();
 
 		if(methodName.equals("addOTChangeListener")) {
 		    // param OTChangeListener listener
 		    
 		    // should check to see if this listener is already
 		    // added
 		    WeakReference listenerRef = new WeakReference(args[0]);
 		    changeListeners.add(listenerRef);
 		    
 		    // debugging instrumentation
 		    // ignore instances of the tracelistener
 		    if(traceListeners &&
 		    		!(args[0] instanceof TraceListener)){
 		    	System.out.println("addOTChangeListener(obj:" + proxy + 
 		    			", listener:" + args[0]);
 		    	
 		    	if(changeListenerLabels == null){
 		    		changeListenerLabels = new HashMap();
 		    		changeListenerLabels.put(listenerRef, "" + args[0]);
 		    	}
 		    }
 		    hasListeners = true;
 		    return null;
 		}
 		
 		if(methodName.equals("removeOTChangeListener")) {
 		    if(traceListeners){
 		    	System.out.println("removeOTChangeListener(obj:" + proxy + 
 		    			", listener:" + args[0]);
 		    }
 
 		    // param OTChangeListener listener		    
 		    for(int i=0; i<changeListeners.size(); i++) {
 		        WeakReference ref = (WeakReference)changeListeners.get(i);
 		        if(args[0] == ref.get()) {
 		            changeListeners.remove(i);
 
 		            return null;
 		        }
 		    }
 		    if(changeListeners.size() == 0) {
 		    	hasListeners = false;
 		    }
 		    return null;
 		}
 		
 		if(methodName.equals("setDoNotifyChangeListeners")) {
 		    setDoNotifyListeners(((Boolean)args[0]).booleanValue());
 		    return null;
 		}
 
 		if(methodName.equals("notifyOTChange")) {
 			// FIXME
 		    notifyOTChange(null, null, null);
 		    return null;
 		}
 
 
 		if(methodName.equals("isResourceSet")) {
 		    String resourceName = (String)args[0];
 		    Object resourceValue = dataObject.getResource(resourceName);
 		    return Boolean.valueOf(resourceValue != null);
         }  else if(methodName.startsWith("is")) {
             String resourceName = getResourceName(2, methodName); 
             Class returnType = method.getReturnType();
             Class proxyClass = proxy.getClass();
             return handleGet(resourceName, returnType, proxyClass);            
 		} else if(methodName.startsWith("get")) {
 			String resourceName = getResourceName(3, methodName); 
 			Class returnType = method.getReturnType();
 			Class proxyClass = proxy.getClass();
 			return handleGet(resourceName, returnType, proxyClass);
 		} else if(methodName.startsWith("add")) {
 			// String resourceName = getResourceName(3, methodName);
             (new Exception("Don't handle add yet")).printStackTrace();
             return null;
 		} else if(methodName.startsWith("removeAll")) {
             (new Exception("Don't handle removeAll yet")).printStackTrace();
             return null;
 		} else if(methodName.equals("toString")) {
 			return OTrunkImpl.getClassName(dataObject) + "@" +  dataObject.getGlobalId();
 		} else if(methodName.equals("hashCode")) {
 			String str = OTrunkImpl.getClassName(dataObject) + "@" +  dataObject.getGlobalId();
 			Integer integer = new Integer(str.hashCode()); 
 			return integer;
 		} else if(methodName.equals("equals")) {
 			Object other = args[0];
 			if(!(other instanceof OTObject)){
 				return Boolean.FALSE;
 			}
 			
 			if(proxy == other) {
 				return Boolean.TRUE;
 			}
 			
 			if(((OTObject)other).getGlobalId().equals(dataObject.getGlobalId())) {
 				System.err.println("compared two ot objects with the same ID but different instances");
 				return Boolean.TRUE;
 			}
 			return Boolean.FALSE;
 
 		} else if(methodName.startsWith("set")){
 			String resourceName = getResourceName(3, methodName); 
 			Object resourceValue = args[0];
 			
 			setResource(resourceName, resourceValue);
 		} else if(methodName.equals("copyInto")) {
             return copyInto(args[0]);
         } else {
 		    System.err.println("Unknown method \"" + methodName + "\" called on " + proxy.getClass());
 		}
 		return null;
 	}
     
 	protected boolean setResource(String name, Object value)
 	{
 		if(value instanceof OTObject) {
 			OTObject child = (OTObject)value;
 			OTID childId = child.getGlobalId();
 			value = childId;
 		} else if(value instanceof byte[]) {
 			value = new BlobResource((byte[])value);
 		} else if(value instanceof URL){
 			value = new BlobResource((URL)value);
 		}
 		
 		// setResource should only return true if the dataObject was 
 		// actually changed with this call
 		if(dataObject.setResource(name, value)){
 			notifyOTChange(name, OTChangeEvent.OP_SET, value);			
 		}
 		
 		return true;
 	}
 
 	public void setDoNotifyListeners(boolean doNotify)
 	{
 	    doNotifyListeners = doNotify;
 	}
 	
     public void notifyOTChange(String property, String operation, 
     		Object value)
     {
     	if(!doNotifyListeners || !hasListeners){
     		return;
     	}
     	
         Vector toBeRemoved = null;
         
         if(changeEvent == null) {
         	changeEvent = new OTChangeEvent(changeEventSource);
         }
         
         changeEvent.setProperty(property);
         changeEvent.setOperation(operation);
         changeEvent.setValue(value);
         
         for(int i=0;i<changeListeners.size(); i++){
             WeakReference ref = (WeakReference)changeListeners.get(i);
             Object listener = ref.get();
             if(traceListeners){
             	System.out.println("sending stateChanged " + changeEvent.getDescription() +
             			" to " + listener);
             }
             if(listener != null) {
                 ((OTChangeListener)listener).stateChanged(changeEvent);
             } else {
                 // the listener was gc'd so lets mark it to be removed
                 if(toBeRemoved == null) {
                     toBeRemoved = new Vector();
                 }
                 if(traceListeners){
                 	System.out.println("otChangeListener garbage collected:" +
                 			changeListenerLabels.get(ref));
                 }
                 toBeRemoved.add(ref);
             }
         }
 
         // clear the value so it doesn't remain around and 
         // so it can be garbage collected
         changeEvent.setValue(null);
 
         if(toBeRemoved != null) {
             for(int i=0; i<toBeRemoved.size(); i++) {
                 changeListeners.remove(toBeRemoved.get(i));
             }
             if(changeListeners.size() == 0){
             	hasListeners = false;
             }
         }
     }
 
 }
