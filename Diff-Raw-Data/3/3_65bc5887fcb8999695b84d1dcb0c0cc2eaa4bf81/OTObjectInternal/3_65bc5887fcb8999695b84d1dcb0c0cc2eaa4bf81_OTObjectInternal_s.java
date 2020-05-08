 package org.concord.otrunk;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectInterface;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.framework.otrunk.otcore.OTClassProperty;
 import org.concord.framework.otrunk.otcore.OTType;
 import org.concord.otrunk.datamodel.BlobResource;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.view.OTViewerHelper;
 
 public class OTObjectInternal implements OTObjectInterface
 {
 	public final static boolean traceListeners = OTViewerHelper.getBooleanProp(
             OTViewerHelper.TRACE_LISTENERS_PROP, false);
 
 	protected OTObjectServiceImpl objectService;
 	
     Vector changeListeners = new Vector();
 
     /**
      * This is for debugging purposes it contains a mapping from
      * the weak reference object to the toString of the listener it
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
 
 	protected OTDataObject dataObject;
 	protected OTClass otClass;
 	
 	private String changeEventSourceInstanceID;
 
 	private Class schemaInterface;
 
 	private int hashCode;
 
 	
 	public OTObjectInternal(OTDataObject dataObject, OTObjectServiceImpl objectService, OTClass otClass)
     {
 		this.objectService = objectService;
     	this.dataObject = dataObject;
     	
 		String str = getOTClassName() + "@" +  getGlobalId();
 		hashCode = str.hashCode();
 
 		this.otClass = otClass;
     }	
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#getOTObjectService()
      */
     public OTObjectService getOTObjectService()
     {
         return objectService;    	
     }
     
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#setEventSource(org.concord.framework.otrunk.OTObject)
      */
     public void setEventSource(OTObject src)
     {
     	changeEventSource = src;
     	changeEventSourceInstanceID = Integer.toHexString(System.identityHashCode(changeEventSource));
     }
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#setDoNotifyListeners(boolean)
      */
 	public void setDoNotifyChangeListeners(boolean doNotify)
 	{
 	    doNotifyListeners = doNotify;
 	}
 	
     /* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#notifyOTChange(java.lang.String, java.lang.String, java.lang.Object)
      */
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
     		if(traceListeners && !(listener instanceof TraceListener)){
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
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#addOTChangeListener(org.concord.framework.otrunk.OTChangeListener)
      */
 	public void addOTChangeListener(OTChangeListener changeListener)
     {
 		if(changeListener == null){
 			throw new IllegalArgumentException("changeListener cannot be null");
 		}
 		
 	    WeakReference listenerRef = new WeakReference(changeListener);
 	    changeListeners.add(listenerRef);
 	    
 	    // debugging instrumentation
 	    // ignore instances of the tracelistener
 	    if(traceListeners &&
 	    		!(changeListener instanceof TraceListener)){
 	    	System.out.println("addOTChangeListener(obj:" + changeEventSource + ","); 
 	    	System.out.println("   listener:" + changeListener+")");
 
 	    	if(changeListenerLabels == null){
 	    		changeListenerLabels = new HashMap();
 	    		changeListenerLabels.put(listenerRef, "" + changeListener);
 	    	}
 	    }
 	    hasListeners = true;
     }
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#removeOTChangeListener(org.concord.framework.otrunk.OTChangeListener)
      */
 	public void removeOTChangeListener(OTChangeListener changeListener)
 	{
 	    if(traceListeners){
 	    	System.out.println("removeOTChangeListener(obj:" + changeEventSource + ",");
 	    	System.out.println("   listener:" + changeListener);
 	    }
 
 	    // param OTChangeListener listener		    
 	    for(int i=0; i<changeListeners.size(); i++) {
 	        WeakReference ref = (WeakReference)changeListeners.get(i);
 	        if(changeListener == ref.get()) {
 	            changeListeners.remove(i);
 
 	            return;
 	        }
 	    }
 	    if(changeListeners.size() == 0) {
 	    	hasListeners = false;
 	    }
 	}
 
     public OTObject getOTObject(OTID childID) throws Exception
     {
     	return objectService.getOTObject(childID);
     }
 
     /* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#setResource(java.lang.String, java.lang.Object)
      */
     public boolean setResource(String name, Object value)
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
 		
 		// Check to see if it is equal before we go further
 	    Object oldValue = getResourceValue(name);
 	    if(oldValue != null && oldValue.equals(value)){
 	        return false;
 	    }
 
 		// setResource should only return true if the dataObject was 
 		// actually changed with this call
 		if(setResourceInternal(name, value)){
 			notifyOTChange(name, OTChangeEvent.OP_SET, value);			
 		}
 		
 		return true;
 	}
    
     public OTID getGlobalId()
     {
     	return dataObject.getGlobalId();
     }
 
 	public boolean isResourceSet(String resourceName)
     {		
 		OTClassProperty property = otClass().getProperty(resourceName);
 		return otIsSet(property);
     }
 
 	public Object getResource(String resourceName, Class returnType)
 	throws Exception
 	{
         // If this class is the one handling the overlays then this call
         // would be substituted by one that goes through all of the overlayed data objects.
 	    Object resourceValue = getResourceValue(resourceName);
 	    
 	    // we can't rely on the returnType here because it could be an
 	    // interface that isn't in the ot package
 	    if(resourceValue instanceof OTID){
 	        OTObject object;
 	        try {
 	            if(resourceValue == null) {
 	                return null;
 	            }
 	            OTID objId = (OTID)resourceValue;
 	            
 	            object = getOTObject(objId);
 	            
 	            if(object != null){
 	            	if(!returnType.isAssignableFrom(object.getClass())){
 	            		System.err.println("Error: Type Mismatch");
 	            		System.err.println("  value: " + object);
 	            		System.err.println("  parentObject: " + schemaInterface.toString());
 	            		System.err.println("  resourceName: " + resourceName);
 	        	        System.err.println("  expected type is: " + returnType);
 	            		return null;
 	            	}
 	            }
 	            
 	            return object;
 	        } catch (Exception e)
 	        {
 	            e.printStackTrace();
 	        }		
 	        
 	        return null;
 	        
 	    } else if(OTResourceMap.class.isAssignableFrom(returnType)) {
 	        try {
 	        	return getResourceMap(resourceName);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;
 	    } else if(OTObjectMap.class.isAssignableFrom(returnType)) {
 	        try {
 	        	return getObjectMap(resourceName);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;				
 	    } else if(OTResourceList.class.isAssignableFrom(returnType)) {
 	        try {
 	        	return getResourceList(resourceName);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;				
 	    } else if(OTObjectList.class.isAssignableFrom(returnType)) {
 	        try {					
 	        	return getObjectList(resourceName);
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
 	    } else if(resourceValue == null && 
 	    		(returnType == String.class || returnType.isPrimitive())) {
 	        try {
 	            Field defaultField = schemaInterface.getField("DEFAULT_" + resourceName);
 	            if(defaultField != null) {
 	                return defaultField.get(null);
 	            }
 	        } catch (NoSuchFieldException e) {
 	        	// It is normal to have undefined strings so we shouldn't throw an
 	        	// exception in that case.
 	        	if(returnType != String.class){
 	        		throw new RuntimeException("No default value set for \"" + resourceName + "\" " +
 	        				"in class: " + schemaInterface);
 	        	}
 	        }
 	    }
 	    
 	    if(resourceValue == null) return null;
 	    
 	    if(!returnType.isInstance(resourceValue) &&
 	            !returnType.isPrimitive()){
 	        System.err.println("invalid resource value for: " + resourceName);
 	        System.err.println("  object type: " + schemaInterface.toString());
 	        System.err.println("  resourceValue is: " + resourceValue.getClass());
 	        System.err.println("  expected type is: " + returnType);
 	        return null;
 	    }
 	    
 	    return resourceValue;	    
 	}
 
 	public Object getResourceValue(String resourceName)
     {
     	return dataObject.getResource(resourceName);
     }
 
 	public OTResourceMap getResourceMap(String resourceName)
     {
         OTDataMap map = (OTDataMap)dataObject.getResourceCollection(
                 resourceName, OTDataMap.class);
         return new OTResourceMapImpl(resourceName, map, this);
     }
 
 	public OTObjectMap getObjectMap(String resourceName)
     {
     	OTDataMap map = (OTDataMap)dataObject.getResourceCollection(
     			resourceName, OTDataMap.class);
     	return new OTObjectMapImpl(resourceName, map, this);
     }
 
 	public OTResourceList getResourceList(String resourceName)
     {
         OTDataList list = (OTDataList)dataObject.getResourceCollection(
                 resourceName, OTDataList.class);
         return new OTResourceListImpl(resourceName, list, this);
     }
 
 	public OTObjectList getObjectList(String resourceName)
     {
     	OTDataList list = (OTDataList)dataObject.getResourceCollection(
                 resourceName, OTDataList.class);
         return new OTObjectListImpl(resourceName, list, this);
     }
 
 	public boolean setResourceInternal(String name, Object value)
     {
     	return dataObject.setResource(name, value);
     }
 
 	public String getOTClassName()
     {
     	return OTrunkImpl.getClassName(dataObject);
     }
 
 	public String internalToString()
 	{
 		String otObjectIDStr = "";
 		if(changeEventSourceInstanceID != null){
 			otObjectIDStr = "@" + changeEventSourceInstanceID;
 		}
 		return getOTClassName() + "#" +  getGlobalId() + otObjectIDStr;
 	}
 	
 	public int internalHashCode()
 	{
 		return hashCode;
 	}
 	
 	public boolean internalEquals(Object other)
 	{
 		if(!(other instanceof OTObject)){
 			return false;
 		}
 		
 		if(changeEventSource == other) {
 			return true;
 		}
 		
 		if(((OTObject)other).getGlobalId().equals(getGlobalId())) {
 			System.err.println("compared two ot objects with the same ID but different instances");
 			return true;
 		}
 		return false;
 	}
 	
 	protected void finalize()
 	throws Throwable
 	{
 		if(OTViewerHelper.isTrace()){
 			System.out.println("finalizing object: " + internalToString());
 		}
 		if(traceListeners){
 			if(changeListeners.size() != 0){
 				// Check for the case where there is just the TraceListener
 				if(changeListeners.size() == 1){
 					WeakReference ref = (WeakReference)changeListeners.get(0);
 					Object listener = ref.get();
 					if(listener instanceof TraceListener){
 						// don't print anything here
 						return;
 					}
 				}
 
 				System.out.println("listeners on finalized object: " + internalToString());
 				for(int i=0; i<changeListeners.size(); i++){
 					WeakReference ref = (WeakReference)changeListeners.get(i);
 					Object listener = ref.get();
 					if(listener instanceof TraceListener){
 						// skip the trace listener
 						continue;
 					}
 					System.out.println("  " + listener);
 				}
 			} 
 		}
 	}
 
 	public OTClass otClass()
     {
 		return otClass;
     }
 
 	public String getName()
     {
 		return (String) getResourceValue("name");
     }
 
 	public void setName(String name)
     {
 		setResource("name", name);
     }
 	public void init()
     {
 		// do nothing on init.
     }
 
 	public String getLocalId()
     {		
 		throw new UnsupportedOperationException("should not be called");
     }
 
 	public void setSchemaInterface(Class schemaInterface)
     {
 		this.schemaInterface = schemaInterface;
     }
 
 	public Object otGet(OTClassProperty property)
     {
 		String name = property.getName();
 		OTType type = property.getType();
 		Class returnClass = type.getInstanceClass();
 		
 		try {
 	        return getResource(name, returnClass);
         } catch (Exception e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
         return null;
     }
 
 	public boolean otIsSet(OTClassProperty property)
     {
         Object resourceValue = dataObject.getResource(property.getName());
         return resourceValue != null;
     }
 
 	public void otSet(OTClassProperty property, Object newValue)
     {
 		// FIXME should probably do some type checking here
 		setResource(property.getName(), newValue);
     }
 
 	public void otUnSet(OTClassProperty property)
     {
 		throw new UnsupportedOperationException("not implemented yet");
     }
 }
