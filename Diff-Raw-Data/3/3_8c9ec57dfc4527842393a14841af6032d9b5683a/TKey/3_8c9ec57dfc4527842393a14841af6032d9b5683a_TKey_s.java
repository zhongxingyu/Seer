 package eu.europeana.uim.common;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Singleton typed field which implicitly defines the class of the value.
  * 
  * @param <T>
  *            the runtime type of the values for this field
  * @param <NS>
  *            the namespace (type) in which the field is defined
  * 
  * @author Andreas Juffinger <andreas.juffinger@kb.nl>
  * @since Dez 16, 2010
  */
 public final class TKey<NS, T> implements Comparable<TKey<NS, T>> {
     private static final long                  serialVersionUID = 1L;
 
     private final Class<T>                     type;
     private final String                       name;
     private final String                       full;
 
     private static Map<TKey<?, ?>, TKey<?, ?>> registry         = new HashMap<TKey<?, ?>, TKey<?, ?>>();
 
     private final Class<NS>                    namespace;
 
     /**
      * Private constructor to implement singleton.
      * 
      * @param namespace
      *            the namespace of the field
      * @param name
      *            the name of the field
      * @param type
      *            the runtime type of the field
      * 
      */
     private TKey(Class<NS> namespace, String name, Class<T> type) {
         if (namespace == null) { throw new IllegalArgumentException(
                 "Argument 'namespace' must not be null."); }
         if (name == null) { throw new IllegalArgumentException("Argument 'name' must not be null."); }
 
         this.namespace = namespace;
         this.name = name;
         this.type = type;
 
         this.full = namespace.getName() + "/" + name;
     }
 
     /**
      * Register a singleton field. The fields should be defined in a specific class within a central
      * configuration class.
      * 
      * <code>
      * class MyProjectFields {
      *     public static final Field<MyProjectFields, String> field0 = TKey.register(MyProjectFields.class, "field0", String.class);
      * }
      * </code>
      * 
      * @param <N>
      *            the type of the namespace
      * @param <T>
      *            the type of the value
      * 
      * @param namespace
      *            the namespace for the field
      * @param name
      *            the name of the field
      * @param type
      *            the type of the values which are defined by this field
      * 
      * @return the singleton instance for the field
      */
     @SuppressWarnings("unchecked")
     public static <N, T> TKey<N, T> register(Class<N> namespace, String name, Class<T> type) {
         if (namespace == null) { throw new IllegalArgumentException("Namespace must not be null."); }
         if (name == null) { throw new IllegalArgumentException("Name must not be null."); }
         if (type == null) { throw new IllegalArgumentException("Type must not be null."); }
 
         synchronized (registry) {
             TKey<N, T> field = new TKey<N, T>(namespace, name, type);
 
             TKey<?, ?> featureKey = registry.get(field);
             if (featureKey != null) {
                 if (!featureKey.getType().equals(type)) { throw new IllegalStateException(
                         "There is already a field registered with the name '" +
                                 featureKey.getName() + "', but it specifies the type '" +
                                 featureKey.getType().getName() + "' instead of '" + type.getName() +
                                 "'"); }
                 return (TKey<N, T>)featureKey;
             } else {
                 registry.put(field, field);
                 return field;
             }
         }
     }
 
     /**
      * @param <N>
      * @param <T>
      * @param namespace
      * @param name
      * @return the instance field of hte appropriate field
      */
     @SuppressWarnings("unchecked")
     public static <N, T> TKey<N, T> resolve(Class<N> namespace, String name) {
         if (namespace == null) { throw new IllegalArgumentException("Namespace must not be null."); }
         if (name == null) { throw new IllegalArgumentException("Name must not be null."); }
 
         synchronized (registry) {
             TKey<N, T> field = new TKey<N, T>(namespace, name, null);
 
             TKey<?, ?> featureKey = registry.get(field);
             if (featureKey != null) { return (TKey<N, T>)featureKey; }
         }
         return null;
     }
 
     /**
      * clears the static key registry.
      */
     public static void clear() {
         registry.clear();
     }
 
     /**
      * clears the static key registry.
      * 
      * @return the number of records in the registry.
      */
     public static int size() {
         return registry.size();
     }
 
     /**
      * @return the type
      */
     public Class<T> getType() {
         return type;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return the full name including namespace
      */
     public String getFullName() {
         return full;
     }
 
     /**
      * @return the namespace
      */
     public Class<NS> getNamespace() {
         return namespace;
     }
 
     @Override
     public String toString() {
         return getFullName() + "=" + type.getName() + ".class";
     }
 
     /**
      * @param string
      * @return the TKey instance represented by the string.
      */
     public static TKey<?, ?> fromString(String string) {
         try {
             String[] split = string.split("=");
             if (split.length == 1) {
                 // backward compatibility with full name only
                 String[] nn = split[0].split("/");
                 if (nn.length != 2) {
                     throw new IllegalArgumentException("Cannot split namespace and key for <" +
                                                        split[0] + ">");
                 } else {
                     // iterate available keys
                     for (TKey<?, ?> key : registry.keySet()) {
                         if (key.getNamespace().getName().equals(nn[0])) {
                             if (key.getName().equals(nn[1])) { return key; }
                         }
                     }
                     return null;
                 }
             } else if (split.length == 2) {
                 String[] nn = split[0].split("/");
                 if (nn.length != 2) {
                     throw new IllegalArgumentException("Cannot split namespace and key for <" +
                                                        split[0] + ">");
                 } else {
                     // iterate available keys
                     Class<?> namespace = null;
                     for (TKey<?, ?> key : registry.keySet()) {
                         if (key.getNamespace().getName().equals(nn[0])) {
                             namespace = key.getNamespace();
                             if (key.getName().equals(nn[1])) { return key; }
                         }
                     }
 
                     if (namespace == null) {
                         namespace = Class.forName(nn[0]);
                     }
 
                     Class<?> type = Class.forName(split[1]);
                     return TKey.register(namespace, nn[1], type);
                 }
             } else {
                 throw new IllegalArgumentException("Given string is not valid: <" + string + ">");
             }
         } catch (ClassNotFoundException e) {
             throw new IllegalStateException("Cannot find type or namespace class in classpath", e);
         }
     }
 
     /**
      * @param object
      * @return the cast of the object conform to this type
      */
     @SuppressWarnings("unchecked")
     public T toType(Object object) {
         return (T)object;
     }
 
     @Override
     public int compareTo(TKey<NS, T> o) {
         int nameDiff = name.compareTo(o.name);
         return nameDiff != 0 ? nameDiff : type.getName().compareTo(o.type.getName());
     }
 
     @Override
     public int hashCode() {
         final int prime = 37;
         int result = 1;
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
         return result;
     }
 
     @SuppressWarnings("rawtypes")
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (obj == null) return false;
         if (getClass() != obj.getClass()) return false;
         TKey other = (TKey)obj;
         if (name == null) {
             if (other.name != null) return false;
         } else if (!name.equals(other.name)) return false;
         if (namespace == null) {
             if (other.namespace != null) return false;
         } else if (!namespace.equals(other.namespace)) return false;
         return true;
     }
 }
