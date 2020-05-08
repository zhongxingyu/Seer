 package org.mule.galaxy.impl.jcr.onm;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.mule.galaxy.impl.jcr.CollectionPersister;
 
 /**
  * 
  */
 public class PersisterManager {
     private Map<String, FieldPersister> persisters = new HashMap<String, FieldPersister>();
     private Map<String, ClassPersister> classPersisters = new HashMap<String, ClassPersister>();
     private FieldPersister defaultPersister = new DefaultPersister();
     private EnumPersister enumPersister = new EnumPersister();
     
     public PersisterManager() {
         super();
         
         persisters.put(Set.class.getName(), new CollectionPersister(HashSet.class, this));
         persisters.put(Class.class.getName(), new ClassFieldPersister());
     }
 
     public FieldPersister getPersister(Class<?> c) {
         FieldPersister p = persisters.get(c.getName());
         if (p == null) {
             if (Enum.class.isAssignableFrom(c)) {
                p = persisters.put(c.getName(), enumPersister);
             }
         }
         if (p == null) {
             return defaultPersister;
         }
         return p;
     }
 
     public Map<String, FieldPersister> getPersisters() {
         return persisters;
     }
 
     public void setPersisters(Map<String, FieldPersister> persisters) {
         // Working around some weird spring bug...
         this.persisters.putAll(persisters);
     }
 
     public FieldPersister getDefaultPersister() {
         return defaultPersister;
     }
 
     public void setDefaultPersister(FieldPersister defaultPersister) {
         this.defaultPersister = defaultPersister;
     }
 
     public Map<String, ClassPersister> getClassPersisters() {
         return classPersisters;
     }
     
     public void setClassPersisters(Map<String, ClassPersister> classPersister) {
         this.classPersisters = classPersister;
     }
     
 }
