 package edu.northwestern.bioinformatics.studycalendar.test.osgi;
 
 import org.osgi.service.metatype.AttributeDefinition;
 
 import java.util.Map;
 import java.util.LinkedHashMap;
 
 /**
  * @author Rhett Sutphin
  */
 public class PscMockAttributeDefinition implements AttributeDefinition {
     private String id, name, description;
     private String[] defaultValue;
     private int cardinality, type;
     private Map<String, String> options;
 
     public PscMockAttributeDefinition(int type, String id, String name, String description) {
         this.type = type;
         this.id = id;
         this.name = name;
         this.description = description;
         this.cardinality = 0;
         this.defaultValue = null;
         this.options = new LinkedHashMap<String, String>();
     }
 
     public PscMockAttributeDefinition(int type, String id) {
         this(type, id, null, null);
     }
 
     public String getID() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public PscMockAttributeDefinition setCardinality(int c) {
         this.cardinality = c;
         return this;
     }
 
     public PscMockAttributeDefinition setCardinality(CollectionType collection, Integer count) {
         if (collection == CollectionType.SINGLE_VALUE) {
             cardinality = 0;
         } else if (collection == CollectionType.VECTOR) {
             cardinality = count == null ? Integer.MIN_VALUE : count * -1;
         } else {
             cardinality = count == null ? Integer.MAX_VALUE : count;
         }
         return this;
     }
 
     public int getCardinality() {
         return cardinality;
     }
 
     public PscMockAttributeDefinition setType(int t) {
         this.type = t;
         return this;
     }
 
     public int getType() {
         return type;
     }
 
     public PscMockAttributeDefinition addOption(String label, String value) {
         this.options.put(label, value);
         return this;
     }
 
     public String[] getOptionValues() {
        return this.options.values().toArray(new String[this.options.size()]);
     }
 
     public String[] getOptionLabels() {
        return this.options.keySet().toArray(new String[this.options.size()]);
     }
 
     public String[] getDefaultValue() {
         return defaultValue;
     }
 
     public PscMockAttributeDefinition setDefaultValue(String[] val) {
         this.defaultValue = val;
         return this;
     }
 
     public String validate(String s) {
          return null;
     }
 
     public static enum CollectionType {
         SINGLE_VALUE, ARRAY, VECTOR
     }
 }
