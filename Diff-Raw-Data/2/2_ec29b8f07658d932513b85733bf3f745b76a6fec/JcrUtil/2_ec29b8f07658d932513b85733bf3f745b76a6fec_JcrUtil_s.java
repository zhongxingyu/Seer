 package org.mule.galaxy.impl.jcr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.ValueFormatException;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.version.VersionException;
 import javax.xml.namespace.QName;
 
 import org.apache.jackrabbit.value.BooleanValue;
 import org.apache.jackrabbit.value.DateValue;
 import org.apache.jackrabbit.value.DoubleValue;
 import org.apache.jackrabbit.value.LongValue;
 import org.apache.jackrabbit.value.StringValue;
 import org.mule.galaxy.Identifiable;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.SessionFactory;
 import org.springmodules.jcr.SessionFactoryUtils;
 
 public class JcrUtil {
 
     public static final String VALUE = "__value";
     public static final String TYPE = "__type";
     public static final String TYPE_SUFFIX = ".type";
     public static final String COMPONENT_TYPE = "__componentType";
     private static final String COMPONENT_TYPE_SUFFIX = ".componentType";
 
     public static Node getDateNode(Node parent, Calendar date) throws RepositoryException {
         String year = new Integer(date.get(Calendar.YEAR)).toString();
         
         String month = new Integer(date.get(Calendar.MONTH)).toString();
         String day = new Integer(date.get(Calendar.DAY_OF_MONTH)).toString();
         String hour = new Integer(date.get(Calendar.HOUR_OF_DAY)).toString();
         
         parent = JcrUtil.getOrCreate(parent, year);
         parent = JcrUtil.getOrCreate(parent, month);
         parent = JcrUtil.getOrCreate(parent, day);
         parent = JcrUtil.getOrCreate(parent, hour);
 
         return parent;
     }
 
     
     public static String escape(String name) {
         String ret = name.replace('/', ' ');
         ret = ret.replace(':', ' ');
         ret = ret.replace('[', ' ');
         ret = ret.replace(']', ' ');
         ret = ret.replace('*', ' ');
         ret = ret.replace('\'', ' ');
         ret = ret.replace('"', ' ');
         ret = ret.replace('|', ' ');
         ret = ret.trim();
         return ret;
     } 
     
     public static void safeSave(final Session session) throws RepositoryException {
         if (session.hasPendingChanges()) {
             session.save();
         }
     }
     
     /**
      * Convert a string to an XPath 2.0 string literal, suitable for inclusion in
      * a query. See JSR-170 spec v1.0, Sec. 6.6.4.9.
      * 
      * @param str
      *           Any string.
      * @return A valid XPath 2.0 string literal, including enclosing quotes.
      */
     public static String stringToXPathLiteralWithoutQuotes(String str) {
        // Single quotes needed for jcr:contains()
        return str.replaceAll("'", "''");
     }
     
     /**
      * Convert a string to an XPath 2.0 string literal, suitable for inclusion in
      * a query. See JSR-170 spec v1.0, Sec. 6.6.4.9.
      * 
      * @param str
      *           Any string.
      * @return A valid XPath 2.0 string literal, including enclosing quotes.
      */
     public static String stringToXPathLiteralWithQuotes(String str) {
        // Single quotes needed for jcr:contains()
        return "'" + str.replaceAll("'", "''") + "'";
     }
 
     /**
      * Convert a string to a JCR search expression literal, suitable for use in
      * jcr:contains() (inside XPath queries). The characters - and " have special
      * meaning, and may be escaped with a backslash to obtain their literal
      * value. See JSR-170 spec v1.0, Sec. 6.6.5.2.
      * 
      * @param str
      *           Any string.
      * @return A valid XPath 2.0 string literal suitable for use in
      *         jcr:contains(), including enclosing quotes.
      */
     public static String stringToJCRSearchExp(String str) {
        // Escape ' and \ everywhere, preceding them with \ except when \ appears
        // in one of the combinations \" or \-
        return stringToXPathLiteralWithQuotes(str.replaceAll("\\\\(?![-\" ])", "\\\\\\\\").replaceAll("'", "\\\\'"));
     }
     
     /** Recursively outputs the contents of the given node. */
     public static void dump(Node node) throws RepositoryException {
         // First output the node path
         System.out.println(node.getPath());
         // Skip the virtual (and large!) jcr:system subtree
         if (node.getName().equals("jcr:system")) {
             return;
         }
 
         // Then output the properties
         PropertyIterator properties = node.getProperties();
         while (properties.hasNext()) {
             Property property = properties.nextProperty();
             if (property.getDefinition().isMultiple()) {
                 // A multi-valued property, print all values
                 Value[] values = property.getValues();
                 for (int i = 0; i < values.length; i++) {
                     System.out.println(
                         property.getPath() + " = " + values[i].getString());
                 }
             } else {
                 // A single-valued property
                 System.out.println(
                     property.getPath() + " = " + property.getString());
             }
         }
 
         // Finally output all the child nodes recursively
         NodeIterator nodes = node.getNodes();
         while (nodes.hasNext()) {
             dump(nodes.nextNode());
         }
     }
 
     public static Node getOrCreate(Node node, String name) throws RepositoryException {
         return getOrCreate(node, name, null);
     }
     
     public static Node getOrCreate(Node node, String name, String nodeType) throws RepositoryException {
         Node child = null;
         try {
             child = node.getNode(name);
         } catch (PathNotFoundException e) {
         }
     
         if (child == null) {
             if (nodeType == null) {
                 child = node.addNode(name);
             } else {
                 child = node.addNode(name, nodeType);
             }
             child.addMixin("mix:referenceable");
         }
         return child;
     }
 
     public static void removeChildren(Node docTypesNode) 
         throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         for (NodeIterator itr = docTypesNode.getNodes(); itr.hasNext();) {
             itr.nextNode().remove();
         }
     }
 
     public static void removeChildren(Node node, String name)
         throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         for (NodeIterator itr = node.getNodes(); itr.hasNext();) {
             Node child = itr.nextNode();
             if (child.getName().equals(name))
                 child.remove();
         }
     }
 
     public static String getStringOrNull(Node node, String propName) {
         try {
             Value v = getValueOrNull(node, propName);   
             if (v != null) {
                 return v.getString();
             }
         } catch (ValueFormatException e) {
             throw new RuntimeException(e);
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }  
         
         return null;
     }
 
     public static Boolean getBooleanOrNull(Node node, String propName) {
         try {
             Value v = getValueOrNull(node, propName);   
             if (v != null) {
                 return v.getBoolean();
             }
         } catch (ValueFormatException e) {
             throw new RuntimeException(e);
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }  
         
         return null;
     }
 
     protected static Date getDateOrNull(Node node, String propName) {
         Calendar c = getCalendarOrNull(node, propName);
         
         if (c == null) return null;
         
         return c.getTime();
     }
     
     public static Calendar getCalendarOrNull(Node node, String propName) {
         try {
             Value v = getValueOrNull(node, propName);   
             if (v != null) {
                 return v.getDate();
             }
         } catch (ValueFormatException e) {
             throw new RuntimeException(e);
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }  
         
         return null;
     }
     
     public static Value getValueOrNull(Node node, String propName) throws PathNotFoundException, RepositoryException {
         Property p = null;
         try {
             p = node.getProperty(propName);
         } catch (PathNotFoundException e) {
             return null;
         }
         
         if (p == null) {
             return null;
         }
         
         return p.getValue();
     }
 
     @SuppressWarnings("unchecked")
     public static void setProperty(String name, Object value, Node n) throws RepositoryException {
         name = escape(name);
         
         if (value instanceof Map) {
             Map<String, String> c = (Map<String, String>) value;
             String keyName = name + ".keys";
             String valuesName = name + ".values";
             if (c.size() == 0) {
                 // clear the property
                 n.setProperty(keyName, (Value[]) null);
                 n.setProperty(valuesName, (Value[]) null);
                 return;
             }
             
             Value[] keys = new Value[c.size()];
             Value[] values = new Value[c.size()];
             int i = 0;
             
             for (Map.Entry<String, String> o : c.entrySet()) {
                 keys[i] = new StringValue(o.getKey());
                 if (!(o.getValue() instanceof String)) {
                     throw new IllegalArgumentException("Only supports Collections of String");
                 }
                 values[i] = new StringValue(o.getValue());
                 i++;
             }
 
             n.setProperty(keyName, keys);
             n.setProperty(valuesName, values);
             n.setProperty(name + TYPE_SUFFIX, Map.class.getName());
         } else  if (value instanceof Collection) {
             Collection<?> c = (Collection<?>) value;
             if (c.size() == 0) {
                 // clear the property
                 n.setProperty(name, (Value[]) null);
                 return;
             }
             
             String typeProp = name + TYPE_SUFFIX;
             if (c instanceof Set) {
                 n.setProperty(typeProp, Set.class.getName());
             } else {
                 n.setProperty(typeProp, Collection.class.getName());
             }
             
             Value[] values = new Value[c.size()];
             int i = 0;
             boolean setComponent = false;
             for (Object o : c) {
                 if (o instanceof Boolean) {
                     values[i] = new BooleanValue((Boolean) o);
                 } else if (o instanceof Calendar) {
                     values[i] = new DateValue((Calendar) o);
                 } else if (o instanceof Double) {
                     values[i] = new DoubleValue((Double) o);
                 } else if (o instanceof Long) {
                     values[i] = new LongValue((Long) o);
                 } else if (o instanceof Integer) {
                     values[i] = new LongValue(((Integer) o).longValue());
                 } else if (o instanceof String) {
                     values[i] = new StringValue((String) o);
                 } else {
                     setComponent = true;
                     values[i] = new StringValue(o.toString());
                 }
                 
                 i++;
             }
             
             if (setComponent) {
                 n.setProperty(name + COMPONENT_TYPE_SUFFIX, getComponentType(c));
             }
             
             n.setProperty(name, values);
         } else {
             if (value instanceof String) {
                 n.setProperty(name, value.toString());
             } else if (value instanceof Calendar) {
                 n.setProperty(name, (Calendar) value);
             } else if (value == null) {
                 n.setProperty(name, (String) null);
             } else if (value instanceof Identifiable) {
                 n.setProperty(name, ((Identifiable) value).getId());
             } else if (value instanceof Boolean) {
                 n.setProperty(name, (Boolean) value);
             } else if (value instanceof Double) {
                 n.setProperty(name, (Double) value);
             } else if (value instanceof Long) {
                 n.setProperty(name, (Long) value);
             } else {
                 n.setProperty(name, value.toString());
             }
         }
     }
 
     public static String getComponentType(Collection<?> c) {
         Iterator<?> itr = c.iterator();
         if (!itr.hasNext()) {
             return null;
         }
         
         return itr.next().getClass().getName();
     }
 
     @SuppressWarnings("unchecked")
     public static <T> T getProperty(String name, Node node) {
         name = escape(name);
         
         try {
             String typeProp = name + TYPE_SUFFIX;
             String type = getStringOrNull(node, typeProp);
             
             if (type == null) {
                 Property property = node.getProperty(name);
                 if (property == null || property.getDefinition().isMultiple()) {
                     return null;
                 }
                 
                 Value val = property.getValue();
                 if (val == null) {
                     return null;
                 }
 
                 switch (val.getType()) {
                 case PropertyType.STRING:
                     return (T) val.getString();
                 case PropertyType.BOOLEAN:
                     return (T) Boolean.valueOf(val.getBoolean());
                 case PropertyType.DATE:
                     return (T) val.getDate();
                 case PropertyType.DOUBLE:
                     return (T) Double.valueOf(val.getDouble());
                 case PropertyType.LONG:
                     return (T) Long.valueOf(val.getLong());
                 default:
                     return null;
                 }
             } else if (Map.class.getName().equals(type)) {
                 Property keys = node.getProperty(name + ".keys");
                 Property values = node.getProperty(name + ".values");
                 
                 Map<String,String> map = new HashMap<String, String>();
                 
                 Value[] keyV = keys.getValues();
                 Value[] valuesV = values.getValues();
                 
                 for (int i = 0; i < keyV.length; i++) {
                     map.put(keyV[i].getString(), valuesV[i].getString());
                 }
                 
                 return (T) map;
             }
 
             Property property = node.getProperty(name);
             if (property == null) {
                 return null;
             }
             
             Collection<Object> values = null;
             if (Set.class.getName().equals(type)) {
                 values = new HashSet<Object>();
             } else {
                 values = new ArrayList<Object>();
             }
             
             String component = JcrUtil.getStringOrNull(node, name + COMPONENT_TYPE_SUFFIX);
             Class componentCls = null;
             if (component != null) {
                 componentCls = JcrUtil.class.getClassLoader().loadClass(component);
             }
            
             for (Value val : property.getValues()) {
                 switch (val.getType()) {
                 case PropertyType.STRING:
                     if (componentCls != null && componentCls.equals(QName.class)) {
                         values.add(QName.valueOf(val.getString()));
                     } else {
                         values.add(val.getString());
                     }
                     break;
                 case PropertyType.BOOLEAN:
                     values.add(val.getBoolean());
                     break;
                 case PropertyType.DATE:
                     values.add(val.getDate());
                     break;
                 case PropertyType.DOUBLE:
                     values.add(val.getDouble());
                     break;
                 case PropertyType.LONG:
                     values.add(val.getLong());
                     break;
                 default:
                     return null;
                 }
             }
             return (T) values;
         } catch (PathNotFoundException e) {
             return null;
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Whether or not this class is a simple type which can be stored with the setProperty method.
      * @param cls
      * @return
      */
     public static boolean isSimpleType(Class<?> cls) {
        return Number.class.isAssignableFrom(cls) || cls == long.class || cls == double.class || cls == short.class
                 || cls == String.class
                 || cls == Calendar.class
                 || cls == Identifiable.class
                 || cls == Boolean.class || cls == boolean.class
                 || Collection.class.isAssignableFrom(cls)
                 || cls == Class.class
                 || cls == QName.class;
     }
     
     public static Object doInTransaction(SessionFactory sf, JcrCallback jcrCallback) throws IOException, RepositoryException {
         Session session = null;
         boolean participate = false;
         if (TransactionSynchronizationManager.hasResource(sf)) {
             // Do not modify the Session: just set the participate
             // flag.
             participate = true;
             session = SessionFactoryUtils.getSession(sf, false);
         } else {
             session = SessionFactoryUtils.getSession(sf, true);
             TransactionSynchronizationManager.bindResource(sf, sf.getSessionHolder(session));
         }
 
         try {
             return jcrCallback.doInJcr(session);
         } finally {
             if (!participate) {
                 TransactionSynchronizationManager.unbindResource(sf);
                 SessionFactoryUtils.releaseSession(session, sf);
             }
         }
     }
 
     public static Set<String> asSet(Value[] values) throws RepositoryException {
         Set<String> valset = new HashSet<String>();
         for (Value v : values) {
             valset.add(v.getString());
         }
         return valset;
     }
 }
