 package org.apache.felix.ipojo.everest.impl;
 
 import org.apache.felix.ipojo.everest.services.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A default implementation of default 'root' resource. Root resources are the resource representing a domain and
  * tracked by the everest core.
  */
 public abstract class AbstractResourceManager extends DefaultResource {
 
     private final String name;
     private final String description;
 
     public AbstractResourceManager(String name) {
         this(name, null);
     }
 
     public AbstractResourceManager(String name, String description) {
         super(Path.SEPARATOR + name);
 
         if (name == null) {
             throw new NullPointerException("Name cannot be null");
         }
         this.name = name;
 
         if (description == null) {
             this.description = name;
         } else {
             this.description = description;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public ResourceMetadata getMetadata() {
         return new ImmutableResourceMetadata.Builder()
                 .set("name", getName())
                 .set("description", getDescription())
                 .build();
     }
 
     /**
      * Extracts the direct children and add a {@literal GET} relation to them.
      *
      * @return a list of relations
      */
     public List<Relation> getRelations() {
         List<Relation> relations = new ArrayList<Relation>();
         for (Resource resource : getResources()) {
            int size = getCanonicalPath().getCount();
             String name = resource.getCanonicalPath().getElements()[size];
             relations.add(new DefaultRelation(resource.getCanonicalPath(), Action.GET, "everest:" + name,
                     "Get " + name));
         }
         return relations;
     }
 
 }
