 package eu.dm2e.ws.grafeo.gom;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 
 import eu.dm2e.utils.PojoUtils;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.GValue;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.annotations.Namespaces;
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.annotations.RDFId;
 import eu.dm2e.ws.grafeo.annotations.RDFProperty;
 
 /**
  * The object mapper contains all functionality for the serialization and deserialization
  * of objects (POJOS) as RDF. The object mapper contains a cache to avoid circles
  * in the object graph, i.e., the object mapper should be reused as long as the same
  * instances of objects are used.
  *
  * @author Konstantin Baierer
  * @author Kai Eckert
  *
  */
 public class ObjectMapper {
 	
     Logger log = Logger.getLogger(getClass().getName());
     
     /**
      * Backreference to the Grafeo this ObjectMapper belongs to.
      */
     Grafeo grafeo;
 	
 	/**
      * The object cache to avoid circles in the object hierarchy while adding objects.
      */
     private Map<Object,GResource> objectCache = new HashMap<>();
     
     /**
      * The object cache to avoid circles in the object hierarchy while retrieving objects.
      */
     private Map<String,Object> uriCache = new HashMap<>();
 
     /**
      * The object mapper is always connected to a Grafeo where the
      * mapped objects are stored.
      *
      * @param grafeo
      */
     public ObjectMapper(Grafeo grafeo) {
         this.grafeo = grafeo;
     }
 
     /**
      * Serialized an object to RDF statements and adds the statements
      * to the internal Grafeo.
      *
      * @param object
      * @return
      */
     public GResource addObject(Object object) {
     	// Cache nested objects
     	if (objectCache.keySet().contains(object))
     		return objectCache.get(object);
     	
     	log.fine("Adding object " + object);
         setAnnotatedNamespaces(object);
         GResource targetResource = getGResource(object);
 
         String type = object.getClass().getAnnotation(RDFClass.class).value();
         log.fine("New Resource: " + targetResource + " a " + type + ".");
         targetResource.set(NS.RDF.PROP_TYPE, grafeo.resource(type));
         
         for (Field field : PojoUtils.getAllFields(object.getClass())) {
             if (!field.isAnnotationPresent(RDFProperty.class)) continue;
             log.finest("Field: " + field.getName() + " instanceof " + field.getType());
             String property = field.getAnnotation(RDFProperty.class).value();
             Object value;
             try {
                 value = PropertyUtils.getProperty(object, field.getName());
                 if (null == value) continue;
             } catch (NoSuchMethodException e) {
                 log.severe(object.getClass().getName() +": No getter/setters for " + field.getName() + " property: " + e);
                 continue;
             } catch (InvocationTargetException | IllegalAccessException e) {
                 throw new RuntimeException("An exception occurred: " + e, e);
             }
 
             // TODO make this more flexible
             if (isAnnotatedObject(value)) {
                 serializeAnnotatedObject(targetResource, property, value);
             } else if (value instanceof java.util.Set) { 
             	serializeSet(targetResource, property, field, value);
             }
 			else if (value instanceof java.util.List) {
             	serializeList(targetResource, property, field, value);
             }
 			else if (value instanceof java.net.URI){
                 serializeURI(targetResource, property, value);
             } else {
                 serializeLiteral(targetResource, property, value);
             }
         }
         return targetResource;
     }
     
     /**
      * Deserializes an object from the Grafeo.
      * 
      * @param T The class of the desired object
      * @param uri The URI of the resource that is deserialized
      * @param <T>
      * @return An object of class T.
      * 
      * @see ObjectMapper#getObject(Class, GResource)
      */
     public <T> T getObject(Class T, URI uri) {
         return getObject(T, grafeo.resource(uri));
     }
 
     /**
      * Deserializes an object from the Grafeo.
      * 
      * @param T The class of the desired object
      * @param resStr The URI of the resource that is deserialized
      * @param <T>
      * @return An object of class T.
      */
    public <T> T getObject(Class<T> T, String resStr) {
         return getObject(T, grafeo.resource(resStr));
     }
 
     /**
      * Deserializes an object from the Grafeo based on the URI of an existing GResource.
      * The existing GResource is not changed or updated!
      * 
      * @param T The class of the desired object
      * @param targetResource An existing GResource used as URI
      * @param <T>
      * @return An object of class T.
      */
     public <T> T getObject(Class T, GResource targetResource) {
         if (targetResource == null) throw new RuntimeException("Trying to get an object for an undefined resource.");
         String uri;
         if (targetResource.isAnon())
         	uri = targetResource.getAnonId();
         else
         	uri = grafeo.expand(targetResource.getUri());
         
         // Cache
         if (uriCache.containsKey(uri)) {
             log.fine("Cache contains: " + T + " for " + uri);
             return (T) uriCache.get(uri);
         }
 
         // the built object
         T targetObject;
         try {
 	        log.fine("Getting object of class " + T + " for URI " + uri);
             targetObject = (T) T.newInstance();
             uriCache.put(uri, targetObject);
             log.fine("Added to cache: " + T + " for " + uri);
         } catch (InstantiationException | IllegalAccessException | SecurityException e) {
             throw new RuntimeException("An exception occurred instantiating class " + T + " for URI " + uri + ". "  + e + "\n" + ExceptionUtils.getStackTrace(e));
         }
 
         // iterate fields in the class definition
         for (Field field : PojoUtils.getAllFields(targetObject.getClass())) {
             log.finest("Field: " + field.getName() + " instanceof " + field.getType());
 
             // if it's a RDF property field
             if (field.isAnnotationPresent(RDFProperty.class)) {
 
                 // the property this field represents
                 String prop = grafeo.expand(field.getAnnotation(RDFProperty.class).value());
 
                 // TODO make this more flexible
                 
                 if (field.getType().isAssignableFrom(java.util.Set.class)) {
                     deserializeSet(targetResource, targetObject, field, prop);
                 }
                 else if (field.getType().isAssignableFrom(java.util.List.class)) {
 	                deserializeList(targetResource, targetObject, field, prop);
                 }
                 else if (field.getType().isAssignableFrom(java.net.URI.class)){
                 	deserializeURI(targetResource, targetObject, field, prop);
                 }
                 else {
                     deserializeLiteral(targetResource, targetObject, field, prop);
                 }
 
                 // RDFId fields with a prefix
             } else if (field.isAnnotationPresent(RDFId.class) && !targetResource.isAnon()) {
                 String prefix = field.getAnnotation(RDFId.class).prefix();
                 try {
                     String id = uri.replace(prefix, "");
                     Object o = grafeo.literal(id).getTypedValue(field.getType());
                     PropertyUtils.setProperty(targetObject, field.getName(), o);
                 } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
             }
         }
         setAnnotatedNamespaces(targetObject);
         return targetObject;
     }
     
 	/**				
 	 * a URI is used, which is usually done to reference resources
 	 * that are not mapped or for resources that are mapped but
 	 * should not get instantiated immediately or the full
 	 * serialization is not desired .
 	 * 
 	 * @param result
 	 * @param property
 	 * @param value
 	 */
 	private void serializeURI(GResource result, String property, Object value) {
 		result.set(property, grafeo.resource((URI) value));
 	}
 
 	/** 
 	 * nested annotated object 
 	 * 
 	 * @param targetResource
 	 * @param property
 	 * @param value
 	 */
 	private void serializeAnnotatedObject(GResource targetResource, String property, Object value) {
 		addObject(value);
 		targetResource.set(property, getGResource(value));
 	}
 
 	/**
 	 * Serializes Unordered list / Set
 	 * 
 	 * @param targetResource
 	 * @param property
 	 * @param value
 	 */
 	private void serializeSet(GResource targetResource, String property, Field field, Object value) {
 		log.fine("Adding All objects from the Set "  + value);
 		Iterable valueIterable = (Iterable) value;
 		for (Object setItem : valueIterable) {
 			if (setItem.getClass().isAssignableFrom(java.net.URI.class)) {
 				serializeURI(targetResource, property, setItem);
 			} else if ( isAnnotatedObject(setItem)
 						&&
 						field.getAnnotation(RDFProperty.class).serializeAsURI()
 					  ){
 				GResource itemContentRes = getGResource(setItem);
 				if (itemContentRes.isAnon()) {
 					log.warning("This element of " + property + " has no id: " + setItem);
 					continue;
 				}
 				serializeURI(targetResource, property, URI.create(itemContentRes.getUri()));
 			} else if (isAnnotatedObject(setItem)) {
 				// nested object
 				log.finest("Adding set element " + setItem + " to grafeo");
 				GResource setItemRes = addObject(setItem);
 				log.finest("Added item as '" + setItemRes + "' to grafeo");
 				log.finest("Asserting relation " + property + " between " + targetResource + " and " + setItemRes);
 				targetResource.set(property, setItemRes);
 			//TODO lists/set
 			} else {
 				log.finest("Asserting relation " + property + " between " + targetResource + " and " + setItem);
 				serializeLiteral(targetResource, property, setItem);
 			}
 		}
 	}
 	
 	/**
 	 * Serializes an ordered List / Array
 	 * TODO buggy
 	 * 
 	 * @param targetResource
 	 * @param value
 	 */
 	private void serializeList(GResource targetResource, String property, Field field, Object value) {
 		
 		
 		// Stop if the list has already been serialized
 		if (null != targetResource.get(property)) {
 			log.fine(targetResource + " has " + property + " already set to " + value);
 			return;
 		}
 		
 		Class<?> subtypeClass = PojoUtils.subtypeClassOfGenericField(field);
 		
 		// get actual list
 		List valueList = (List) value;
 		
 		// Don't serialize empty lists
 		if (valueList.size() == 0) {
 			return;
 		}
 		
 		// Attach a blank node as list with the property
 		GResource listResource = grafeo.createBlank();
 		listResource.set(NS.RDF.PROP_TYPE, grafeo.resource(NS.CO.CLASS_LIST));
 		targetResource.set(property, listResource);
 		
 		// list size
 		listResource.set(NS.CO.PROP_SIZE, grafeo.literal(valueList.size()));
 		
 		// store backref to previous list resource so we can link them with co:nextItem
 		GResource previousItemResource = null;
 		
 		for (int i = 0; i < valueList.size(); i++) {
 			
 			// create item resource
 			GResource itemResource = grafeo.createBlank(); 
 			itemResource.set(NS.RDF.PROP_TYPE, grafeo.resource(NS.CO.CLASS_ITEM));
 			
 			// connect the list item to the list
 			if (i == 0) {
 				listResource.set(NS.CO.PROP_FIRST_ITEM, itemResource);
 			} else if (i == valueList.size() - 1) {
 				listResource.set(NS.CO.PROP_LAST_ITEM, itemResource);
 			} else {
 				listResource.set(NS.CO.PROP_ITEM, itemResource);
 			}
 			
 			// set co:nextItem on previous
 			if (null != previousItemResource) {
 				previousItemResource.set(NS.CO.PROP_NEXT_ITEM, itemResource);
 			}
 			previousItemResource = itemResource;
 			
 			
 			// set item index
 			itemResource.set(NS.CO.PROP_INDEX, grafeo.literal(i));
 			
 			// Add the itemcontent
 			Object itemContent = valueList.get(i);
 			if (subtypeClass.isAssignableFrom(java.net.URI.class)) {
 				serializeURI(itemResource, NS.CO.PROP_ITEM_CONTENT, itemContent);
 			} else if ( isAnnotatedObject(itemContent)
 						&&
 						field.getAnnotation(RDFProperty.class).serializeAsURI()
 				  ){
 				GResource itemContentRes = getGResource(itemResource);
 				if (itemContentRes.isAnon()) {
 					log.warning("This element of " + property + " has no id: " + itemContent);
 					continue;
 				}
 				serializeURI(itemResource, NS.CO.PROP_ITEM_CONTENT, URI.create(itemContentRes.getUri()));
 			} else if (isAnnotatedObject(itemContent)) {
 				serializeAnnotatedObject(itemResource, NS.CO.PROP_ITEM_CONTENT, itemContent);
 			} else {
 				serializeLiteral(itemResource, NS.CO.PROP_ITEM_CONTENT, itemContent);
 				
 			}
 		}
 	}
 
 	/**
 	 * Serializes a plain value.
 	 * 
 	 * @param targetResource
 	 * @param property
 	 * @param value
 	 */
 	private void serializeLiteral(GResource targetResource, String property, Object value) {
 		targetResource.set(property, grafeo.literal(value));
 	}
 
 	/**
 	 * Deserializes a Set.
 	 * 
 	 * @param objectResource
 	 * @param targetObject
 	 * @param field
 	 * @param prop
 	 */
 	private <T> void deserializeSet(GResource objectResource, T targetObject, Field field, String prop) {
 		log.fine(field.getName() + " is a SET.");
 		Class<?> subtypeClass = PojoUtils.subtypeClassOfGenericField(field);
 		Set propSet = new HashSet();
 		Set<GValue> propValues = objectResource.getAll(prop);
 		for (GValue thisValue : propValues) {
 
 		    // Sets can be composed of literals ...
 			if (isAnnotatedObject(thisValue)) {
 		        // TODO infinite recursion on doubly-linked resources? Is that fixed by caching?
 		        Object nestedObject = getObject(subtypeClass, (GResource) thisValue);
 		        propSet.add(nestedObject);
 		        log.fine("Added resource value: " + thisValue.resource());
 			} else if (subtypeClass.isAssignableFrom(java.net.URI.class)){
 				propSet.add(URI.create(thisValue.resource().getUri()));
 				// TODO lists/sets
 			} else {
 		        Object thisValueTyped = thisValue.getTypedValue(subtypeClass);
 		        propSet.add(thisValueTyped);
 		        log.fine("Added literal value: " + thisValue.toString());
 			}
 		}
 
 		// store the property set
 		try {
 		    PropertyUtils.setProperty(targetObject, field.getName(), propSet);
 		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
 		    throw new RuntimeException("An exception occurred: " + e, e);
 		}
 	}
 
 	/**
 	 * Deserializes a List.
 	 * 
 	 * @param objectResource The Resource representing the object to deserialize to
 	 * @param targetObject  The object to de-serialize to
 	 * @param field The field in question
 	 * @param prop The RDFProperty that bridges this field to RDF
 	 */
 	private <T> void deserializeList(GResource objectResource, T targetObject, Field field, String prop) {
 		log.fine(field.getName() + " is a LIST.");
 		
 //		// Skip de-serialization if there is already one statement with this property
 //		// in the graph (this is a HACK). This prevents grafeo from promiscuously
 //		// aggregating blank nodes.
 //		if (this.grafeo.containsTriple(objectResource, prop, null)) {
 //			log.info("Skipping further list deserialization.");
 //		}
 		
 		Class<?> subtypeClass = PojoUtils.subtypeClassOfGenericField(field);
 		
 		// Find the co:List
 		GResource listResource;
 		try {
 			listResource = objectResource.get(prop).resource();
 		} catch (Exception e) {
 			log.severe("No List: " + e);
 			return;
 		}
 		
 		// Find the co:firstItem
 		GResource currentItem;
 		try {
 			currentItem = listResource.get(NS.CO.PROP_FIRST_ITEM).resource();
 		} catch (Exception e) {
 			log.severe("No firstItem: " + e);
 			return;
 		}
 		log.fine("co:firstItem : " + currentItem);
 		
 		// Cache for the list items to prevent cyclic references and other recursions
 		Set<GResource> listItemCache = new HashSet<>();
 		
 		// Stop flag to stop list recursion
 		boolean listIsFinished = false;
 		
 		// The final array
 		ArrayList propArray = new ArrayList();
 		
 		// loop through the list
 		while (! listIsFinished) {
 			
 			// Sanity check
 			if (listItemCache.contains(currentItem))
 				throw new RuntimeException("This item has already been processed - smells like cyclic list!");
 			else 
 				listItemCache.add(currentItem);
 			
 			GValue contentValue = currentItem.get(NS.CO.PROP_ITEM_CONTENT);
 			if (null == contentValue) {
 				throw new RuntimeException("This item has no item content!");
 			}
 			
 			// if its a literal
 			if (contentValue.isLiteral()) {
 				log.fine("Adding literal item '" + contentValue + " instanceof  " + subtypeClass);
 				propArray.add(contentValue.literal().getTypedValue(subtypeClass));
 			}
 			// TODO nested lists/sets
 			else {
 				log.fine("Recursively Adding annotated object '" + contentValue);
 				propArray.add(getObject(subtypeClass, contentValue.resource()));
 			}
 			
 			// recurse
 			try {
 				currentItem = currentItem.get(NS.CO.PROP_NEXT_ITEM).resource();
 			} catch (NullPointerException e) {
 				listIsFinished = true;
 			}
 		}
 		
 		try{
 		    PropertyUtils.setProperty(targetObject, field.getName(), propArray);
 		} catch (InvocationTargetException | NoSuchMethodException  | IllegalAccessException e) {
 		    throw new RuntimeException("An exception occurred: " + e, e);
 		}
 	}
 	
 	protected <T> void deserializeURI(GResource targetResource, T targetObject, Field field, String prop) {
 		log.fine(field.getName() + " is a fascinating " + field.getType());
 		GValue propValue = targetResource.get(prop);
 		if (propValue != null && ! propValue.isLiteral() && ! propValue.resource().isAnon()) {
 			URI theUri = URI.create(propValue.resource().getUri());
 			try {
 				PropertyUtils.setProperty(targetObject, field.getName(), theUri);
 			} catch (NoSuchMethodException e) {
 				log.severe(targetObject.getClass().getName() +": No getter/setters for " + field.getName() + " property: " + e);
 				return;
 			} catch (InvocationTargetException | IllegalAccessException e) {
 				throw new RuntimeException( "An exception occurred for type: "
 						+ targetObject.getClass()
 						+ " Make sure the Pojo has valid getters/setters for all RDFProperty fields. "
 						+ e, e);
 			}
 		}
 	}
 	
 	/**
 	 * Deserialize a literal
 	 * 
 	 * @param targetResource The RDF Resource to which the literal is linked
 	 * @param targetObject The Java object to add to
 	 * @param field The Java field relating to this literal
 	 * @param prop The RDF property linking to the literal
 	 */
 	protected <T> void deserializeLiteral(GResource targetResource, T targetObject, Field field, String prop) {
 		log.fine(field.getName() + " is a boring " + field.getType());
 		try {
 		    GValue propValue = targetResource.get(prop);
 		    if (null == propValue) {
 		    	return;
 		    }
 		    log.fine("Property " + prop + " : " + propValue);
 		    PropertyUtils.setProperty(targetObject, field.getName(), propValue.getTypedValue(field.getType()));
 		} catch (NoSuchMethodException e) {
 			log.severe(targetObject.getClass().getName() +": No getter/setters for " + field.getName() + " property: " + e);
 			return;
 		} catch (InvocationTargetException | IllegalAccessException e) {
 			throw new RuntimeException( "An exception occurred for type: "
 					+ targetObject.getClass()
 					+ " Make sure the Pojo has valid getters/setters for all RDFProperty fields. "
 					+ e, e);
 		}
 	}
 
     /**
      * Determines whether a class is annotated with Grafeo annotations, in particular whether it has the RDFClass annotation.
      * 
      * @param object The object to check
      * @return true if the object has the RDFClass annotation, false otherwise
      * 
      * @see RDFClass
      */
     private boolean isAnnotatedObject(Object object) {
         return object.getClass().isAnnotationPresent(RDFClass.class);
     }
 
     /**
      * Get the GResource for an object, either by its ID field (denoted by the RDFId annotation) or a blank node.
      * 
      * @param object The object for which to get the GResource
      * @return The object id as a GResource or a blank node
      * 
      * @see RDFId
      */
     private GResource getGResource(Object object) {
         String uri = null;
         log.fine("Getting GResource for object " + object);
         if (objectCache.containsKey(object)) {
         	return objectCache.get(object);
         }
 
         for (Field field : PojoUtils.getAllFields(object.getClass())) {
             log.finest("Field: " + field.getName() + " instanceof " + field.getType());
             if (field.isAnnotationPresent(RDFId.class)) {
                 try {
                     Object id = PropertyUtils.getProperty(object, field.getName());
                     if (!(null == id) && !("0".equals(id.toString())) ) {
 	                    // prepend prefix
 	                    uri = field.getAnnotation(RDFId.class).prefix() + id.toString();
                     }
                 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
             }
         }
         if (uri==null) {
         	// TODO BUG this must be unique per object
         	objectCache.put(object, grafeo.createBlank());
         } else {
             uri = grafeo.expand(uri);
         	objectCache.put(object, grafeo.resource(uri));
         }
         return objectCache.get(object);
 
     }
 
     /**
      * Adds the namespace prefixes defined by an object using the Namespaces annotation to the Grafeo.
      * 
      * @param object The object from which to add namespaces
      * 
      * @see Namespaces
      */
     private void setAnnotatedNamespaces(Object object) {
         String key = null;
         Namespaces annotation = object.getClass().getAnnotation(Namespaces.class);
         if (annotation == null) return;
         for (String s : annotation.value()) {
             if (key == null) {
                 key = s;
             } else {
                 grafeo.setNamespace(key, s);
                 key = null;
             }
         }
 
     }
 }
