 package ph.hatch.ddd.oe;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import ph.hatch.ddd.domain.annotations.DomainEntity;
 
 import java.lang.reflect.Constructor;
 import java.util.*;
 
 public class ObjectExplorer {
 
     ObjectRegistry objectRegistry;
     ObjectRepository objectRepository;
 
     private String dateFormat = "MM/dd/yyyy";
     private boolean areNullsIncluded = false;
 
     private Gson gson;
 
     public ObjectExplorer(ObjectRegistry objectRegistry, ObjectRepository objectRepository) {
 
         this.objectRegistry = objectRegistry;
         this.objectRepository = objectRepository;
 
     }
 
     public void setDateFormat(String dateFormat) {
         this.dateFormat = dateFormat;
     }
 
     public void includeNulls(Boolean includeNulls) {
         this.areNullsIncluded = includeNulls;
     }
 
     private Object flattenHashMap(Map<String, Object> field) {
 
         // applicable only to single field Maps that we use for Entity Identity Objects
         // this assumes that all Entity Identity objects  will only have one field (and they should)
         if(field.keySet().size() == 1) {
 
             String key = (String) field.keySet().toArray()[0];
 
             Object firstField = field.get(key);
 
             if(firstField instanceof Map) {
                 flattenHashMap((Map) firstField);
             } else {
                 return firstField;
             }
         } else {
             return field;
         }
 
         return field;
     }
 
     private Map flattenFields(Map<String, Object> fatMap) {
 
         for(String key : fatMap.keySet()) {
 
             Object keyValue = fatMap.get(key);
 
             if(keyValue instanceof Map) {
                 fatMap.put(key, flattenHashMap((Map) keyValue));
             }
 
             if(keyValue instanceof Collection) {
                 //System.out.println(keyValue);
             }
         }
 
         return fatMap;
     }
 
     private Map expandHierarchy(Object object, Map<String, Object> base, Boolean includeMeta, HashSet visited) {
 
         ObjectMeta meta = objectRegistry.getMetaForClass(object.getClass());
 
         //Gson gson = new GsonBuilder().serializeNulls().setDateFormat(dateFormat).create();
 
         // TODO : deeply nested stuff here, refactor by breaking down
         for(String key : base.keySet()) {
 
             ObjectMeta fieldMeta = meta.getFieldMeta(key);
 
             String classForIdentity = objectRegistry.getClassForEntityIdentityField(fieldMeta.getClassName());
 
             // check if the field is an entity identity, if it is we expand using the entity identity
             if(classForIdentity != null) {
 
                 // only expand for identities that are not for this parent
                 //if(!classForIdentity.equalsIgnoreCase(object.getClass().getCanonicalName())) {
                 Boolean isInstance = false;
                 try {
                     isInstance = Class.forName(classForIdentity).isAssignableFrom(object.getClass());
                 } catch(Exception e) {
 
                 }
 
                 //TODO: bug here if subclass
                 //if(!classForIdentity.equalsIgnoreCase(object.getClass().getCanonicalName())) {
                 if(!isInstance) {
 
                     // get the object type of the entity
                     String entityIdentityClassName = fieldMeta.getClassName();
                     String entityClassName = objectRegistry.getClassForEntityIdentityField(entityIdentityClassName);
 
                     // prevent circular-referencing children from getting reloaded
                    if(visited.contains(entityIdentityClassName)) {
                        //flattenFields(base);
                     } else {
                         visited.add(entityIdentityClassName);
                     }
 
                     // if we found an entity class, it means that the elements of the collection are entity identities
                     if(entityClassName != null) {
 
                         //System.out.println("class for collection entry " + entityClassName);
 
                         Collection items;
 
                         // if this is is a collection, get the collection,
                         // otherwise, just fake a collection so we only have one routine for both
                         if(fieldMeta.isCollection) {
                             items = (Collection) base.get(key);
                         } else {
                             items = new HashSet();
                             items.add(base.get(key));
                         }
 
                         Set newEntries = new HashSet();
 
                         // we are sure that what we have are Maps since we JSONed them in
                         for(Map item : (Collection<Map>) items) {
 
                             //System.out.println(item);
 
                             // there should only be one entry
                             if(item != null && item.keySet().size() == 1) {
 
                                 String idValue = (String) item.values().toArray()[0];
                                 //System.out.println(idValue);
 
                                 try {
 
                                     Class<?> cl = Class.forName(entityIdentityClassName);
                                     Constructor<?> cons = cl.getConstructor(String.class);
 
                                     Object o = cons.newInstance(idValue);
 
                                     // TODO: this is terribly inefficient since we have to hit the database for every element
                                     // of the collection, consider doing an "IN" query
                                     Object result = null;
                                     result = objectRepository.load((Class<DomainEntity>) Class.forName(entityClassName), o);
 
                                     String objectName = Class.forName(entityClassName).getSimpleName();
 
                                     // only create a new entry if we found a matching record
                                     if(result != null) {
 
                                         Map newEntryMap = gson.fromJson(gson.toJson(result), Map.class);
                                         newEntryMap = expandHierarchy(result, newEntryMap, includeMeta, visited);
 
                                         // remove the entity identity after exploding it
                                         visited.remove(entityIdentityClassName);
 
                                         Map newEntry = flattenFields(newEntryMap);
 
                                         Map innerElement = new HashMap();
                                         innerElement.put(objectName, newEntry);
 
                                         //newEntries.add(newEntry);
                                         newEntries.add(innerElement);
 
                                     }
 
                                 } catch(Exception e) {
 
                                     e.printStackTrace();
 
                                 }
                             }
                         }
 
                         // if this was a collection, then put the new entries, otherwise,
                         // put the single entry that we updated
                         if(fieldMeta.isCollection) {
                             if(newEntries.size() > 0) {
                                 base.put(key, newEntries);
                             }
                         } else {
                             if(newEntries.size() > 0) {
                                 base.put(key, newEntries.toArray()[0]);
                             }
                         }
                     }
                 }
             }
         }
 
         return flattenFields(base);
 
     }
 
 
     private Map buildMap(Object object) {
 
         Gson gson = new Gson();
 
         if(object != null) {
             Map me = gson.fromJson(gson.toJson(object), Map.class);
             return me;
         }
 
         return null;
 
     }
 
     public void exploreStructure(Object object) {
 
     }
 
     public Map explore(Object object, Boolean includeMeta) {
 
         if(object != null) {
 
             GsonBuilder builder = new GsonBuilder();
 
             if(areNullsIncluded) {
                 builder.serializeNulls();
             }
 
             builder.setDateFormat(dateFormat);
 
             gson = builder.create();
 
             HashSet<String> visited = new HashSet();
 
             Map me = gson.fromJson(gson.toJson(object), Map.class);
             Map expanded = expandHierarchy(object, me, includeMeta, visited);
 
             return expanded;
         }
 
         return null;
     }
 
     public Map explore(Object object) {
 
         return explore(object, false);
 
     }
 
 }
