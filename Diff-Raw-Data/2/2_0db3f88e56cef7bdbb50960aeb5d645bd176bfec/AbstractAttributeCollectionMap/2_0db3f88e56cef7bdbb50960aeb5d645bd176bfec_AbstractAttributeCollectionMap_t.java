 package http.attribute;
 
 import http.util.AbstractMap;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import static http.util.Asserts.assertNotNull;
 import static http.util.Checks.isNotEmpty;
 import static http.util.Checks.isNull;
 
 /**
  * This is a map that can contain multiple {@link Attribute}'s and it's sub classes for each key. It also provides
  * convenience constructors and methods for adding {@code Attribute} instances.
  *
  * @author Karl Bennett
  */
 public abstract class AbstractAttributeCollectionMap<A extends Attribute, C extends Collection<A>> extends
         AbstractMap<String, C> implements AttributeCollectionMap<A, C> {
 
     /**
      * Create a new {@code AbstractAttributeCollectionMap} with the supplied backing map.
      *
      * @param backingMap the map to be used internally for all the default {@link Map} methods.
      */
     public AbstractAttributeCollectionMap(Map<String, C> backingMap) {
         super(backingMap);
     }
 
     /**
      * Create a new {@code AbstractAttributeCollectionMap} that uses a {@link HashMap} as it's backing map.
      */
     public AbstractAttributeCollectionMap() {
 
         this(new HashMap<String, C>());
     }
 
     /**
      * Create a new {@code AbstractAttributeCollectionMap} with the supplied backing map and that is populated from the
      * supplied {@code AbstractAttributeCollectionMap}.
      *
      * @param backingMap the map to be used internally for all the default {@link Map} methods.
      * @param attributes the map that will be copied to produce this map.
      */
     public AbstractAttributeCollectionMap(Map<String, C> backingMap, AbstractAttributeCollectionMap<A, C> attributes) {
 
         this(backingMap);
 
         assertNotNull("attributes", attributes);
 
         putAll(attributes);
     }
 
     /**
      * Create a new {@code AbstractAttributeCollectionMap} as a copy of the supplied
      * {@code AbstractAttributeCollectionMap}. This is not a deep copy so will point to the same attribute instances
      * that are contained in the supplied map.
      *
      * @param attributes the map that will be copied to produce this map.
      */
     public AbstractAttributeCollectionMap(AbstractAttributeCollectionMap<A, C> attributes) {
 
         this(new HashMap<String, C>(), attributes);
     }
 
     /**
      * Create a new {@code AbstractAttributeCollectionMap} with the supplied backing map and that is populated with the
      * attributes in the supplied {@link Collection}.
      *
      * @param backingMap the map to be used internally for all the default {@link Map} methods.
      * @param attributes the attributes that will be contained in the new map.
      */
     public AbstractAttributeCollectionMap(Map<String, C> backingMap, Collection<A> attributes) {
 
         this(backingMap);
 
         assertNotNull("attributes", attributes);

        addAll(attributes);
     }
 
     /**
      * Create a new {@code AbstractAttributeCollectionMap} that uses a {@link HashMap} as it's backing map and that is
      * populated with the attributes in the supplied {@link Collection}.
      *
      * @param attributes the attributes that will be contained in the new map.
      */
     public AbstractAttributeCollectionMap(Collection<A> attributes) {
 
         this(new HashMap<String, C>(), attributes);
     }
 
 
     /**
      * Override this method to provide a new instance of the {@link Collection} type that will be used to holed the
      * multiple attribute instances.
      *
      * @return the new {@code Collection} instance that will be used to hold the commonly named attributes.
      */
     protected abstract C newCollection();
 
 
     /**
      * Add a new attribute to the map using the attributes name for the key. If an entry for this attribute already
      * exists it will be added to the {@link Collection} mapped to that attributes name.
      *
      * @param attribute the attribute to add.
      * @return true if adding the attribute modified the map, otherwise false.
      */
     public boolean add(A attribute) {
 
         assertNotNull("attribute", attribute);
 
         // Try and get any attributes with the same name as the attribute provided.
         C attributes = get(attribute.getName());
 
         if (isNull(attributes)) attributes = newCollection();
 
         boolean mutated = attributes.add(attribute);
 
         put(attribute.getName(), attributes);
 
         return mutated;
     }
 
     /**
      * Add all the supplied attributes to the map using the attributes names as the keys. If an entry for any of the
      * attributes already exists they will be added to the {@link Collection} mapped to the existing name key.
      *
      * @param attributes the attributes to add.
      * @return true if adding the attributes modified the map, otherwise false.
      */
     public boolean addAll(Collection<A> attributes) {
 
         assertNotNull("attributes", attributes);
 
         boolean mutated = false;
 
         for (A attribute : attributes) {
 
             if (add(attribute)) mutated = true;
         }
 
         return mutated;
     }
 
     /**
      * Remove the supplied attribute from the map. If removing this attribute causes the collection of attributes that
      * relate to the attributes name to become empty then the entire entry will be removed.
      *
      * @param attribute the attribute to remove.
      * @return true if an attribute was removed from the map, otherwise false.
      */
     public boolean remove(A attribute) {
 
         C attributes = get(attribute.getName());
 
         if (isNull(attributes)) return false;
 
         boolean mutated = attributes.remove(attribute);
 
         // If there are no more instances left for the supplied header type then remove the entry completely.
         if (isNotEmpty(attributes)) put(attribute.getName(), attributes);
         else remove(attribute.getName());
 
         return mutated;
     }
 
     /**
      * Remove all the supplied attributes from the map. If removing any of these attributes causes the collection of
      * attributes that relate to the name key to become empty then the entire entry will be removed.
      *
      * @param attributes the attributes to remove.
      * @return true if an attribute was removed from the map, otherwise false.
      */
     public boolean removeAll(Collection<A> attributes) {
 
         assertNotNull("attributes", attributes);
 
         boolean mutated = false;
 
         for (A attribute : attributes) {
 
             if (remove(attribute)) mutated = true;
         }
 
         return mutated;
     }
 }
