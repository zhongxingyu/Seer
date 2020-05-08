 package md.frolov.legume.client.elastic.api;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import md.frolov.legume.client.elastic.model.reply.Mapping;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 public class MappingsResponse extends ESResponse<Mapping>
 {
     private final static List<String> RESERVED_PROPERTIES =
             Lists.newArrayList("@type","@message","@source","@source_host","@source_path","@tags","@timestamp");
 
     /** type -> set of properties */
     private Map<String, Set<Property>> properties;
     public MappingsResponse(final Mapping mappings)
     {
         properties = Maps.newTreeMap();
 
         for (Map.Entry<String, Map<String, Mapping.TypeMapping>> node : mappings.getObj().entrySet())
         {
             //iterating through nodes
             visitNode(node.getValue());
         }
     }
 
     private void visitNode(Map<String,Mapping.TypeMapping> types) {
         for (Map.Entry<String, Mapping.TypeMapping> type : types.entrySet())
         {
             String typeName = type.getKey();
             final Set<Property> typeProperties;
             if(properties.containsKey(typeName)) {
                 typeProperties = properties.get(typeName);
             } else {
                 typeProperties = Sets.newTreeSet();
                 properties.put(typeName, typeProperties);
             }
 
             for (Map.Entry<String, Mapping.PropertyMapping> property : type.getValue().getProperties().entrySet())
             {
                 visitProperty("", typeProperties, property.getKey(), property.getValue());
             }
         }
     }
 
     private void visitProperty(final String base, final Set<Property> typeProperties, String propertyName, final Mapping.PropertyMapping value)
     {
         String fullName = base + propertyName;
         if(RESERVED_PROPERTIES.contains(fullName)) {
             return;
         }
 
        if(Boolean.TRUE.toString().equals(value.getDynamic()) && value.getProperties() != null) {
             for (Map.Entry<String, Mapping.PropertyMapping> dynProperty : value.getProperties().entrySet())
             {
                 visitProperty(fullName + ".", typeProperties, dynProperty.getKey(), dynProperty.getValue());
             }
         } else {
             typeProperties.add(new Property(fullName, propertyName, value.getType()));
         }
     }
 
     public Map<String, Set<Property>> getProperties()
     {
         return properties;
     }
 
     public static class Property implements Comparable<Property> {
         private final String fullName;
         private final String name;
         private final String type; //TODO enum
 
         public Property(final String fullName, final String name, final String type)
         {
             this.fullName = fullName;
             this.name = name;
             this.type = type;
         }
 
         public String getType()
         {
             return type;
         }
 
         public String getName()
         {
             return name;
         }
 
         public String getFullName()
         {
             return fullName;
         }
 
         @Override
         public int hashCode()
         {
             return fullName.hashCode();
         }
 
         @Override
         public boolean equals(final Object o)
         {
             return fullName.equals(((Property) o).fullName);
         }
 
         @Override
         public int compareTo(final Property property)
         {
             return fullName.compareTo(property.fullName);
         }
     }
 }
 
