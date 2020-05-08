 package com.socialcomputing.wps.server.planDictionnary.connectors.datastore;
 
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 /**
  * 
  * @author Franck Valetas <franck.valetas@social-computing.com>
  * @author Jonathan Dray <jonathan.dray@social-computing.com>
  * 
  *         Representation of some data with a String identifier and a Map of
  *         parameters
  * 
  */
 public abstract class Data {
     protected final String m_Id;
     protected final Hashtable<String, Object> m_Properties;
 
     public Data(String id) {
         this.m_Id = id;
         this.m_Properties = new Hashtable<String, Object>();
         addProperty("id", id);
     }
 
     /**
      * Add a property to this element
      * 
      * @param name
      *            name of the property
      * @param value
      *            property's value
      */
     public void addProperty(String name, Object value) {
         if( value != null)
             this.m_Properties.put(name, value);
     }
 
     /**
      * Add multiple properties to this element
      * 
      * @param properties
      *            a map of properties with keys and values
      */
     public void addProperties(Map<String, ? extends Object> properties) {
         this.m_Properties.putAll(properties);
     }
 
     public String getId() {
         return this.m_Id;
     }
 
     public Hashtable<String, Object> getProperties() {
         return this.m_Properties;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder().append(this.m_Id).append(" {");
 
         // Adding list of properties to the list of displayed values
         for (Entry<String, Object> property : this.m_Properties.entrySet()) {
             sb.append(property.getKey()).append(" => ");
             Object value = property.getValue();
             if (value instanceof String[]) {
                 String[] new_name = (String[]) value;
 
                 sb.append("[");
                 for (String s : new_name) {
                     sb.append(s).append(", ");
                 }
                 sb.append("]");
 
             }
             else {
                 sb.append(property.getValue());
             }
             sb.append(",");
         }
 
         sb.append("}");
         return sb.toString();
     }
     
     public StringBuilder toJson( StringBuilder sb) {
         boolean first = true;
         for (Entry<String, Object> p : this.m_Properties.entrySet()) {
             if( first) first = false;
             else sb.append(',');
             sb.append( "\"").append(Data.toJson(p.getKey())).append("\":");
             Data.toJson(sb, p.getValue());
             sb.append("");
         }
         return sb;
     }
     
     public static StringBuilder toJson( StringBuilder sb, Object o) {
         if( o instanceof String) {
             sb.append('\"').append( Data.toJson( ( String) o)).append('\"');
         }
         else if( o instanceof Integer) {
             sb.append( String.valueOf( ( Integer) o));
         }
         else if( o instanceof Long) {
             sb.append( String.valueOf( ( Long) o));
         }
         else if( o instanceof Object[]) {
             boolean first = true;
             sb.append( '[');
             for( Object s : (Object[]) o) {
                 if( first) first = false;
                 else sb.append(',');
                 Data.toJson( sb, s);
             }
             sb.append( ']');
         }
         else {
             Data.toJson( sb, o.toString());
         }
         return sb;
     }
     
     public static String toJson( String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\\n").replace("\r", "\\\r").replace("\t", "\\\t");
     }
     
 }
