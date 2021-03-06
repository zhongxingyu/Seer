 /**
  * Copyright (C) 2010 Olafur Gauti Gudmundsson
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
 
 package com.google.code.morphia;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import com.google.code.morphia.MappedClass.MappedField;
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.PostLoad;
 import com.google.code.morphia.annotations.PreLoad;
 import com.google.code.morphia.annotations.PrePersist;
 import com.google.code.morphia.annotations.Property;
 import com.google.code.morphia.annotations.Reference;
 import com.google.code.morphia.utils.Key;
 import com.google.code.morphia.utils.ReflectionUtils;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBRef;
 import com.mongodb.ObjectId;
 
 /**
  *
  * @author Olafur Gauti Gudmundsson
  * @author Scott Hernandez
  */
 @SuppressWarnings("unchecked")
 public class Mapper {
     private static final Logger logger = Logger.getLogger(Mapper.class.getName());
 
 	private static final String CLASS_NAME_KEY = "className";
 	
 	public static final String ID_KEY = "_id";
 	public static final String IGNORED_FIELDNAME = ".";
 
     /** Set of classes that have been validated for mapping by this mapper */
     private final ConcurrentHashMap<String,MappedClass> mappedClasses = new ConcurrentHashMap<String, MappedClass>();
     
     private final ThreadLocal<Map<String, Object>> entityCache = new ThreadLocal<Map<String, Object>>();
 
 
     Mapper() {
     }
 
     boolean isMapped(Class c) {
         return mappedClasses.containsKey(c.getName());
     }
 
     void addMappedClass(Class c) {
     	MappedClass mc = new MappedClass(c);
     	mc.validate();
         mappedClasses.put(c.getName(), mc);
     }
 
     MappedClass addMappedClass(MappedClass mc) {
     	mc.validate();
         mappedClasses.put(mc.clazz.getName(), mc);
         return mc;
     }
 
     Map<String, MappedClass> getMappedClasses() {
         return mappedClasses;
     }
 
     /** Gets the mapped class for the object (type). If it isn't mapped, create a new class and cache it (without validating).*/
     public MappedClass getMappedClass(Object obj) {
 		if (obj == null) return null;
 		Class type = (obj instanceof Class) ? (Class)obj : obj.getClass();
 		MappedClass mc = mappedClasses.get(type.getName());
 		if (mc == null) {
 			//no validation
 			mc = new MappedClass(type);
 			this.mappedClasses.put(mc.clazz.getName(), mc);
 		}
 		return mc;
 	}
 
     void clearHistory() {
         entityCache.remove();
     }
 
     public String getCollectionName(Object object) {
     	if (object instanceof Class) return getCollectionName((Class) object);
     	
     	MappedClass mc = getMappedClass(object);
         return mc.defCollName;
     }
     
 	public String getCollectionName(Class clazz) {
 	  	MappedClass mc = getMappedClass(clazz);
     	return mc.defCollName;
     }
 
     private String getId(Object entity) {
         try {
             return (String)getMappedClass(entity).idField.get(entity);
         } catch ( IllegalAccessException iae ) {
             throw new RuntimeException(iae);
         }
     }
 
     /**
      * Updates the {@code @Id} and {@code @CollectionName} fields.
      * @param entity The object to update
      * @param dbId Value to update with; null means skip
      * @param dbNs Value to update with; null or empty means skip
      */
 	void updateKeyInfo(Object entity, Object dbId, String dbNs) {
 		MappedClass mc = getMappedClass(entity);
 		
 		//update id field, if there.
 		if (mc.idField != null && dbId != null) {
 			try {
 				Object value = mc.idField.get(entity);
 				if ( value != null ) {
 					//The entity already had an id set. Check to make sure it hasn't changed. That would be unexpected, and could indicate a bad state.
 			    	if (!dbId.equals(value))
 			    		throw new RuntimeException("id mismatch: " + value + " != " + dbId + " for " + entity.getClass().getSimpleName());
 				} else {
 					//set the id field with the "new" value
 					if (dbId instanceof ObjectId && mc.idField.getType().isAssignableFrom(String.class)) {
 						dbId = dbId.toString();
 					}
 		    		mc.idField.set(entity, dbId);
 				}
 
 			} catch (Exception e) {
 				if (e.getClass().equals(RuntimeException.class)) throw (RuntimeException)e;
 
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
     Class getClassForName(String className, Class defaultClass) {
     	if (mappedClasses.containsKey(className)) return mappedClasses.get(className).clazz;
         try {
             Class c = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
             return c;
         } catch ( ClassNotFoundException ex ) {
             return defaultClass;
         }
     }
 
     protected Object createEntityInstanceForDbObject( Class entityClass, BasicDBObject dbObject ) {
         // see if there is a className value
         String className = (String) dbObject.get(CLASS_NAME_KEY);
         Class c = entityClass;
         if ( className != null ) {
         	//try to Class.forName(className) as defined in the dbObject first, otherwise return the entityClass
             c = getClassForName(className, entityClass);
         }
         return createInstance(c);
     }
 
     /** Gets a no-arg constructor and calls it via reflection.*/
     protected Object createInstance(Class type) {
         try {
         	//allows private/protected constructors
 	        Constructor constructor = type.getDeclaredConstructor();
 	        constructor.setAccessible(true);
 	        return constructor.newInstance();
         } catch (Exception e) {throw new RuntimeException(e);}
     }
 
     /** creates an instance of testType (if it isn't Object.class or null) or fallbackType */
     protected Object notObjInst(Class fallbackType, Class testType) {
     	if (testType != null && testType != Object.class) return createInstance(testType);
     	return createInstance(fallbackType);
     }
     
     Object fromDBObject(Class entityClass, BasicDBObject dbObject) {
        entityCache.set(new HashMap<String, Object>());
         
         Object entity = createEntityInstanceForDbObject(entityClass, dbObject);
         
         mapDBObjectToEntity(dbObject, entity);
 
         entityCache.remove();
         return entity;
     }
 
     DBObject toDBObject( Object entity ) {
     	BasicDBObject dbObject = new BasicDBObject();
     	try {
 	        dbObject.put(CLASS_NAME_KEY, entity.getClass().getCanonicalName());
 	
 	        MappedClass mc = getMappedClass(entity);
             
 	        dbObject = (BasicDBObject) mc.callLifecycleMethods(PrePersist.class, entity, dbObject);
 	        for (MappedField mf : mc.persistenceFields) {
 	            Field field = mf.field;
 	
 	            field.setAccessible(true);
 	
 	            if ( mf.hasAnnotation(Id.class) ) {
 	                Object value = field.get(entity);
 	                if ( value != null ) {
 	                    dbObject.put(ID_KEY, asObjectIdMaybe(value));
 	                }
 	            } else if ( mf.hasAnnotation(Reference.class) ) {
 	                mapReferencesToDBObject(entity, mf, dbObject);
 	            } else  if (mf.hasAnnotation(Embedded.class)){
 	                mapEmbeddedToDBObject(entity, mf, dbObject);
 	            } else if (mf.isMongoTypeCompatible()) {
 	            	mapValuesToDBObject(entity, mf, dbObject);
 	            } else {
 	            	logger.warning("Ignoring field: " + field.getName() + " [" + field.getType().getSimpleName() + "]");
 	            }
 	        }	        
         } catch (Exception e) {throw new RuntimeException(e);}
         return dbObject;
 
     }
 
     void mapReferencesToDBObject( Object entity, MappedField mf, BasicDBObject dbObject) {
     	try {
 	    	Reference refAnn = (Reference)mf.getAnnotation(Reference.class);
 	        String name = mf.name;
 	
 	        Object fieldValue = mf.field.get(entity);
 	        
 	        if (mf.isMap()) {
 	            Map<Object,Object> map = (Map<Object,Object>) fieldValue;
 	            if ( map != null && map.size() > 0) {
 	                Map values = (Map)notObjInst(HashMap.class, (refAnn == null) ? null : refAnn.concreteClass());
 
 	                for ( Map.Entry<Object,Object> entry : map.entrySet() ) {
 	                    values.put(entry.getKey(), new DBRef(null, getCollectionName(entry.getValue()), asObjectIdMaybe(getId(entry.getValue()))));
 	                }
 	                if (values.size() > 0) dbObject.put(name, values);
 	            }
 	    	} else if (mf.isMultipleValues()) {
 	    		if (fieldValue != null) {
 	                List values = new ArrayList();
 
 		            if (mf.field.getType().isArray()) {
 			            for (Object o : (Object[])fieldValue) {
 		                    values.add(new DBRef(null, getCollectionName(o), asObjectIdMaybe(getId(o))));
 		                }
 		            } else {
 			            for (Object o : (Iterable)fieldValue) {
 		                    values.add(new DBRef(null, getCollectionName(o), asObjectIdMaybe(getId(o))));
 		                }		            	
 		            }
 		            
 	                if (values.size() > 0) dbObject.put(name, values);
 	            }
 	        } else {
 	            if ( fieldValue != null ) {
 	                dbObject.put(name, new DBRef(null, getCollectionName(fieldValue), asObjectIdMaybe(getId(fieldValue))));
 	            }
 	        }
         } catch (Exception e) {throw new RuntimeException(e);}
     }
 
     void mapEmbeddedToDBObject( Object entity, MappedField mf, BasicDBObject dbObject ) {
         String name = mf.name;
 
         Object fieldValue = null;
 		try {
 			fieldValue = mf.field.get(entity);
         } catch (Exception e) {throw new RuntimeException(e);}
 
 
 	    if (mf.isMap()) {
 	        Map<String, Object> map = (Map<String, Object>) fieldValue;
 	        if ( map != null ) {
 	            BasicDBObject values = new BasicDBObject();
 	            for ( Map.Entry<String,Object> entry : map.entrySet() ) {
 	                values.put(entry.getKey(), toDBObject(entry.getValue()));
 	            }
 	            if (values.size() > 0) dbObject.put(name, values);
 	        }
 	
 	    } else if (mf.isMultipleValues()) {
             Iterable coll = (Iterable)fieldValue;
             if ( coll != null ) {
                 List values = new ArrayList();
                 for ( Object o : coll ) {
                     values.add(toDBObject(o));
                 }
                 if (values.size()>0) dbObject.put(name, values);
             }
         } else {
         	DBObject dbObj = fieldValue == null ? null : toDBObject(fieldValue);
             if ( dbObj != null && dbObj.keySet().size() > 0) dbObject.put(name, dbObj);
         }
     }
 
     void mapValuesToDBObject( Object entity, MappedField mf, BasicDBObject dbObject ) {
         try {
 	    	String name = mf.name;
 	        Class fieldType = mf.field.getType();
 	        Object fieldValue = mf.field.get(entity);
 	
 	        //sets and list are stored in mongodb as ArrayLists
 	        if (mf.isMap()) {
 	            Map<Object,Object> map = (Map<Object,Object>) mf.field.get(entity);
 	            if (map != null && map.size() > 0) {
 	                Map mapForDb = new HashMap();
 	                for ( Map.Entry<Object,Object> entry : map.entrySet() ) {
 	                	mapForDb.put(entry.getKey(), objectToValue(entry.getValue()));
 	                }
 	                dbObject.put(name, mapForDb);
 	            }
 	        } else if (mf.isMultipleValues()) {
 	        	Class paramClass = mf.subType;
 	            if (fieldValue != null) {
 	            	Iterable iterableValues = null;
 	
 	            	if (fieldType.isArray()) {
 	            		Object[] objects = null;
 	            		try {
 	            			objects = (Object[]) fieldValue;
 	            		} catch (ClassCastException e) {
 	                		//store the primitive array without making it into a list.
 	            			if (Array.getLength(fieldValue) == 0) return;
 	            			dbObject.put(name, fieldValue);
 	            			return;
 	            		}
 	            		//convert array into arraylist
 	            		iterableValues = new ArrayList(objects.length);
 	            		for(Object obj :objects)
 	            			((ArrayList)iterableValues).add(obj);
 	            	} else {
 	            		//cast value to a common interface
 	            		iterableValues = (Iterable) fieldValue;
 	            	}
 	        	
 	        		//cast value to a common interface
 	        		List values = new ArrayList();
 	                
 	            	if ( paramClass != null ) {
 	                    for ( Object o : iterableValues )
 	                    	values.add(objectToValue(paramClass, o));
 	                } else {
 	                    for ( Object o : iterableValues )
 	                    	values.add(objectToValue(o));
 	                }
 	        		if (values.size() > 0) dbObject.put(name, values);
 	            }
 	        
 	        } else {
 	        	Object val = objectToValue(fieldValue);
 	            if ( val != null ) {
 	            	dbObject.put(name, val);
 	            }
 	        }
         } catch (Exception e) {throw new RuntimeException(e);}
     }
 
     Object mapDBObjectToEntity( BasicDBObject dbObject, Object entity ) {
         // check the history key (a key is the namespace + id)
         String cacheKey = (!dbObject.containsField(ID_KEY)) ? null : "[" + dbObject.getString(ID_KEY) + "]";
         if (entityCache.get() == null) {
             entityCache.set(new HashMap<String, Object>());
         }
         if ( cacheKey != null ) {
             if (entityCache.get().containsKey(cacheKey)) {
                 return entityCache.get().get(cacheKey);
             } else {
                 entityCache.get().put(cacheKey, entity);
             }
         }
 
         MappedClass mc = getMappedClass(entity);
 
         dbObject = (BasicDBObject) mc.callLifecycleMethods(PreLoad.class, entity, dbObject);
         try {
 	        for (MappedField mf : mc.persistenceFields) {
 	            Field field = mf.field;
 	            field.setAccessible(true);
 	
 	            if ( mf.hasAnnotation(Id.class) ) {
 	                if ( dbObject.get(ID_KEY) != null ) {
 	                    field.set(entity, objectFromValue(field.getType(), dbObject, ID_KEY));
 	                }
 	
 	            } else if ( mf.hasAnnotation(Reference.class) ) {
 	                mapReferencesFromDBObject(dbObject, mf, entity);
 	
 	            } else if ( mf.hasAnnotation(Embedded.class) ) {
 	                mapEmbeddedFromDBObject(dbObject, mf, entity);
 	                
 	            } else if ( mf.hasAnnotation(Property.class) || mf.isMongoTypeCompatible()) {
 	            	mapValuesFromDBObject(dbObject, mf, entity);
 	            } else {
 	            	logger.warning("Ignoring field: " + field.getName() + " [" + field.getType().getSimpleName() + "]");
 	            }
 	        }
         } catch (Exception e) {throw new RuntimeException(e);}
 
         mc.callLifecycleMethods(PostLoad.class, entity, dbObject);
         return entity;
     }
 
     void mapValuesFromDBObject( BasicDBObject dbObject, MappedField mf, Object entity ) {
         Property propAnnotation = (Property)mf.getAnnotation(Property.class);
         String name = mf.name;
         try {
 	        Class fieldType = mf.field.getType();
 	        
 	        if (mf.isMap()) {
 		        if ( dbObject.containsField(name) ) {
 		            Map<Object,Object> map = (Map<Object,Object>) dbObject.get(name);
 	                Map values = (Map)notObjInst(HashMap.class, (propAnnotation == null) ? null : propAnnotation.concreteClass());
 		            for ( Map.Entry<Object,Object> entry : map.entrySet() ) {
 		            	values.put(entry.getKey(), objectFromValue(fieldType, entry.getValue()));
 		            }
 		            mf.field.set(entity, values);
 		        }
 	    	}else if (mf.isMultipleValues()) {
 	            boolean bSet = ReflectionUtils.implementsInterface(fieldType, Set.class);
 	
 	            if ( dbObject.containsField(name) ) {
 	                Class subtype = mf.subType;
 	                
 	                //for byte[] don't treat it as a multiple values.
 	                if (subtype == byte.class && fieldType.isArray()) {
 	                	mf.field.set(entity, dbObject.get(name));
 	                	return;
 	                }
 	                //List and Sets are stored as List in mongodb
 	                List list = (List) dbObject.get(name);
 	                
 	                if ( subtype != null ) {
 	                    //map back to the java datatype (List/Set/Array[])
 	                    Collection values;
 	                    
 	                    if (!bSet)
 	    	                values = (List)notObjInst(ArrayList.class, (propAnnotation == null) ? null : propAnnotation.concreteClass());
 	                    else
 	    	                values = (Set)notObjInst(HashSet.class, (propAnnotation == null) ? null : propAnnotation.concreteClass());
 	                    
 	                    if (subtype == Locale.class) {
 	                        for ( Object o : list )
 	                            values.add(parseLocale((String)o));
 	                    } else if (subtype == Key.class) {
 	                        for ( Object o : list )
 	                            values.add(new Key((DBRef)o));
 	                    } else if (subtype.isEnum()) {
 	                        for ( Object o : list )
 	                            values.add(Enum.valueOf(subtype, (String)o));
 	                    } else {
 	                        for ( Object o : list ) 
 	                            values.add(o);
 	                    }
 	                    if (fieldType.isArray()) {
 	                    	Object exampleArray = Array.newInstance(subtype, 1);
 	                    	
 	                    	if (subtype == Long.class) {
 	                    		Object[] array = ((ArrayList)values).toArray((Object[]) exampleArray);
 	                    		mf.field.set(entity, array);
 	                    	}
 	                    }
 	                    else
 	                    	mf.field.set(entity, values);
 	                } else {
 	                	mf.field.set(entity, list);
 	                }
 	            }
 	        } else {
 	            if ( dbObject.containsField(name) ) {
 	            	mf.field.set(entity, objectFromValue(fieldType, dbObject, name));
 	            }
 	        }
     	} catch (Exception e) {throw new RuntimeException(e);}
     }
 
 	void mapEmbeddedFromDBObject( BasicDBObject dbObject, MappedField mf, Object entity ) {
         Embedded embeddedAnn = (Embedded)mf.getAnnotation(Embedded.class);
         String name = mf.name;
 
         Class fieldType = mf.field.getType();
         try {
 	        if (mf.isMap()) {
 	            Class docObjClass = ReflectionUtils.getParameterizedClass(mf.field, 1);
 	            Map map = (Map)notObjInst(HashMap.class, (embeddedAnn == null) ? null : embeddedAnn.concreteClass());
 
 	            if ( dbObject.containsField(name) ) {
 	                BasicDBObject value = (BasicDBObject) dbObject.get(name);
 	                for ( Map.Entry entry : value.entrySet() ) {
 	                    Object docObj = createEntityInstanceForDbObject(docObjClass, (BasicDBObject)entry.getValue());
 	                    docObj = mapDBObjectToEntity((BasicDBObject)entry.getValue(), docObj);
 	                    map.put(entry.getKey(), docObj);
 	                }
 	            }
 	            mf.field.set(entity, map);
 	        } else if (mf.isMultipleValues()) {
 	            boolean bList = ReflectionUtils.implementsInterface(fieldType, List.class);
 	
 	        	// multiple documents in a List
 	            Class docObjClass = mf.subType;
 	            Collection docs = (Collection)notObjInst((bList) ? ArrayList.class : HashSet.class, embeddedAnn.concreteClass());
 	
 	            if ( dbObject.containsField(name) ) {
 	                Object value = dbObject.get(name);
 	                if ( value instanceof List ) {
 	                    List refList = (List) value;
 	                    for ( Object docDbObject : refList ) {
 	                        Object docObj = createEntityInstanceForDbObject(docObjClass, (BasicDBObject)docDbObject);
 	                        docObj = mapDBObjectToEntity((BasicDBObject)docDbObject, docObj);
 	                        docs.add(docObj);
 	                    }
 	                } else {
 	                    BasicDBObject docDbObject = (BasicDBObject) dbObject.get(name);
 	                    Object docObj = createEntityInstanceForDbObject(docObjClass, docDbObject);
 	                    docObj = mapDBObjectToEntity(docDbObject, docObj);
 	                    docs.add(docObj);
 	                }
 	            }
 	            mf.field.set(entity, docs);
 	        }  else {
 	            // single document
 	            Class docObjClass = fieldType;
 	            if ( dbObject.containsField(name) ) {
 	                BasicDBObject docDbObject = (BasicDBObject) dbObject.get(name);
 	                Object refObj = createEntityInstanceForDbObject(docObjClass, docDbObject);
 	                refObj = mapDBObjectToEntity(docDbObject, refObj);
 	                mf.field.set(entity, refObj);
 	            }
 	        }
         } catch (Exception e) {throw new RuntimeException(e);}
     }
 
     void mapReferencesFromDBObject( BasicDBObject dbObject, MappedField mf, Object entity ) {
         Reference refAnn = (Reference)mf.getAnnotation(Reference.class);
         String name = mf.name;
 
         
         Class fieldType = mf.field.getType();
 
     	try {
 	    	if (mf.isMap()) {
 	            Class referenceObjClass = ReflectionUtils.getParameterizedClass(mf.field, 1);
 	            Map map = (Map)notObjInst(HashMap.class, (refAnn == null) ? null : refAnn.concreteClass());
 
 	            if ( dbObject.containsField(name) ) {
 	                BasicDBObject value = (BasicDBObject) dbObject.get(name);
 	                for ( Map.Entry entry : value.entrySet() ) {
 	                    DBRef dbRef = (DBRef) entry.getValue();
 	                    BasicDBObject refDbObject = (BasicDBObject) dbRef.fetch();
 
 	                    Object refObj = createEntityInstanceForDbObject(referenceObjClass, refDbObject);
 	                    refObj = mapDBObjectToEntity(refDbObject, refObj);
 	                    map.put(entry.getKey(), refObj);
 	                }
 	            }
 	            mf.field.set(entity, map);
 	            
 	        } else if (mf.isMultipleValues()) {
 	            boolean bSet = ReflectionUtils.implementsInterface(fieldType, Set.class);
 	
 	            // multiple references in a List
 	            Class referenceObjClass = mf.subType;
 	            Collection references = (Collection) notObjInst((!bSet) ? ArrayList.class : HashSet.class, refAnn.concreteClass());
 	        	
 	            if ( dbObject.containsField(name) ) {
 	                Object value = dbObject.get(name);
 	                if ( value instanceof List ) {
 	                    List refList = (List) value;
 	                    for ( Object dbRefObj : refList ) {
 	                        DBRef dbRef = (DBRef) dbRefObj;
 	                        BasicDBObject refDbObject = (BasicDBObject) dbRef.fetch();
 	
 	                        Object refObj = createEntityInstanceForDbObject(referenceObjClass, refDbObject);
 	                        refObj = mapDBObjectToEntity(refDbObject, refObj);
 	                        references.add(refObj);
 	                    }
 	                } else {
 	                    DBRef dbRef = (DBRef) dbObject.get(name);
 	                    BasicDBObject refDbObject = (BasicDBObject) dbRef.fetch();
 	                    Object refObj = createEntityInstanceForDbObject(referenceObjClass, refDbObject);
 	                    refObj = mapDBObjectToEntity(refDbObject, refObj);
 	                    references.add(refObj);
 	                }
 	            }
 	            
 	            mf.field.set(entity, references);
 	        } else {
 	            // single reference
 	            Class referenceObjClass = fieldType;
 	            if ( dbObject.containsField(name) ) {
 	                DBRef dbRef = (DBRef) dbObject.get(name);
 	                BasicDBObject refDbObject = (BasicDBObject) dbRef.fetch();
 	
 	                Object refObj = createEntityInstanceForDbObject(referenceObjClass, refDbObject);
 	                refObj = mapDBObjectToEntity(refDbObject, refObj);
 	                mf.field.set(entity, refObj);
 	            }
 	        }
         } catch (Exception e) {throw new RuntimeException(e);}
     }
     
     private static Locale parseLocale(String localeString) {
         if (localeString != null && localeString.length() > 0) {
             StringTokenizer st = new StringTokenizer(localeString, "_");
             String language = st.hasMoreElements() ? st.nextToken() : Locale.getDefault().getLanguage();
             String country = st.hasMoreElements() ? st.nextToken() : "";
             String variant = st.hasMoreElements() ? st.nextToken() : "";
             return new Locale(language, country, variant);
         }
         return null;
     }
     
     /** turns the object intto an ObjectId if it is/should-be one */
 	public static Object asObjectIdMaybe(Object id) {
 		if ((id instanceof String) && ObjectId.isValid((String)id)) return new ObjectId((String)id);
 		return id;
 	}
 	
     /** Converts known types from mongodb -> java. Really it just converts enums and locales from strings */
     public static Object objectFromValue( Class javaType, BasicDBObject dbObject, String name ) {
     	return objectFromValue(javaType, dbObject.get(name));
     }
 
     protected static Object objectFromValue( Class javaType, Object val) {
         if (javaType == String.class) {
             return val.toString();
         } else if (javaType == Character.class || javaType == char.class) {
             return val.toString().charAt(0);
         } else if (javaType == Integer.class || javaType == int.class) {
             return ((Number)val).intValue();
         } else if (javaType == Long.class || javaType == long.class) {
             return ((Number)val).longValue();
         } else if (javaType == Byte.class || javaType == byte.class) {
            	Object dbValue = val;
         	if (dbValue instanceof Byte) return dbValue;
         	else if (dbValue instanceof Double) return ((Double)dbValue).byteValue();
         	else if (dbValue instanceof Integer) return ((Integer)dbValue).byteValue();
         	String sVal = val.toString();
             return Byte.parseByte(sVal);
         } else if (javaType == Short.class || javaType == short.class) {
            	Object dbValue = val;
         	if (dbValue instanceof Short) return dbValue;
         	else if (dbValue instanceof Double) return ((Double)dbValue).shortValue();
         	else if (dbValue instanceof Integer) return ((Integer)dbValue).shortValue();
         	String sVal = val.toString();
             return Short.parseShort(sVal);
         } else if (javaType == Float.class || javaType == float.class) {
         	Object dbValue = val;
         	if (dbValue instanceof Double) return ((Double)dbValue).floatValue();
         	String sVal = val.toString();
             return Float.parseFloat(sVal);
         } else if (javaType == Locale.class) {
             return parseLocale(val.toString());
         } else if (javaType.isEnum()) {
             return Enum.valueOf(javaType, val.toString());
         } else if (javaType == Key.class) {
             return new Key((DBRef)val);
         }
         return val;
     }
 
     /** Converts known types from java -> mongodb. Really it just converts enums and locales to strings */
     public Object objectToValue(Class javaType, Object obj) {
     	if (obj == null) return null;
     	if (javaType == null) javaType = obj.getClass();
 
     	if ( javaType.isEnum() ) {
             return ((Enum) obj).name();
         } else if ( javaType == Locale.class ) {
           	return ((Locale) obj).toString();
         } else if ( javaType == char.class ||  javaType == Character.class ) {
         	return ((Character)obj).toString();
         } else if ( javaType == Key.class ) {
           	return ((Key) obj).toRef(this);
         } else {
             return obj;
         }
     	
     }
     
     /** Converts known types from java -> mongodb. Really it just converts enums and locales to strings */
     public Object objectToValue(Object obj) {
     	if (obj == null) return null;
     	return objectToValue(obj.getClass(), obj);
     }
 }
