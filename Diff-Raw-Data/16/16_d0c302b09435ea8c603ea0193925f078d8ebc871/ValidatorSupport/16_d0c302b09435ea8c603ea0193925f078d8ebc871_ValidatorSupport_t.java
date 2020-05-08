 package jpaoletti.jpm.validator;
 
 import java.util.Properties;
 
 
 /**Helper class for {@link Validator}. Includes a DB object and a properties attribute to define
  * attributes in the configuration file.
  * <pre>
  * {@code
 * <!DOCTYPE entity SYSTEM "entity.dtd">
  * <operation>
  *     <validator class="SomeValidator">
  *         <properties>
  *             <property name="somename" value="somevalue" />
  *         </properties>
  *     </validator>
  * </operation>
  * }
  * </pre>
  * @author jpaoletti
  * */
 public abstract class ValidatorSupport implements Validator{
     /**The properties*/
     private Properties properties;
      
     /**Helper for a property
      * @param name Property name
      * @param def Default value
      * @return Property value*/
     public String get (String name, String def) {
         if (properties != null) {
             Object obj = properties.get (name);
             if (obj instanceof String)
                 return obj.toString();
         }
         return def;
     }
     
     /**Helper for an int property
      * @param name Property name
      * @return Property value as int*/
     public Integer getInt (String name) {
         try {
             return Integer.parseInt(get(name, "").trim());
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * @return the properties
      */
     public Properties getProperties() {
         return properties;
     }
 
     /**
      * @param properties the properties to set
      */
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 
 }
