 package edu.northwestern.bioinformatics.studycalendar.utils.hibernate;
 
 import edu.nwu.bioinformatics.commons.ComparisonUtils;
 
 import org.hibernate.usertype.UserType;
 import org.hibernate.usertype.ParameterizedType;
 import org.hibernate.HibernateException;
 import org.apache.commons.logging.Log;
 
 import java.util.Properties;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import java.sql.Types;
 import java.io.Serializable;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 
 /**
  * A Hibernate UserType for subclasses of {@link edu.northwestern.bioinformatics.studycalendar.domain.AbstractControlledVocabularyObject}.
  * Required parameter:
  * <dl>
  *   <dt><code>enumClass</code></dt>
  *   <dd>Typesafe enumeration class of which this type instance will load instances</dd>
  * </dl>
  * Optional parameters:
  * <dl>
  *   <dt><code>factoryMethod</code></dt>
  *   <dd>The public static method to call to obtain an instance of the class from a database key.
  *       Default is <kbd>getById</kbd>.</dd>
  *   <dt><code>keyMethod</code></dt>
  *   <dd>The public method to call on an instance of the class to get the database key under
  *       which it should be stored.  Default is <kbd>getId</kbd>.</dd>
  * </dl>
  *
  * @author Rhett Sutphin
  */
 public class ControlledVocabularyObjectType implements UserType, ParameterizedType {
     protected static final String ENUM_CLASS_PARAM_KEY = "enumClass";
     protected static final String FACTORY_METHOD_PARAM_KEY = "factoryMethod";
     protected static final String KEY_METHOD_PARAM_KEY = "keyMethod";
 
     private static final String DEFAULT_FACTORY_METHOD_NAME = "getById";
     private static final String DEFAULT_KEY_METHOD_NAME = "getId";
 
     private static final Class[] NO_PARAMS = new Class[0];
 
     private Log log = HibernateTypeUtils.getLog(getClass());
 
     private Properties parameterValues;
 
     protected final Properties getParameterValues() {
         return parameterValues;
     }
 
     ////// IMPLEMENTATION of ParameterizedType
 
     public void setParameterValues(Properties parameters) {
         this.parameterValues = new Properties(createDefaults());
         if (parameters != null) {
             this.parameterValues.putAll(parameters);
         }
 
         // call various methods so that they have an opportunity to fail during initialization
         getEnumClass();
         getFactoryMethod();
         getKeyMethod();
     }
 
     private Properties createDefaults() {
         Properties defaults = new Properties();
         defaults.put(FACTORY_METHOD_PARAM_KEY, DEFAULT_FACTORY_METHOD_NAME);
         defaults.put(KEY_METHOD_PARAM_KEY, DEFAULT_KEY_METHOD_NAME);
         return defaults;
     }
 
     ////// IMPLEMENTATION OF UserType
 
     public final int[] sqlTypes() {
         return new int[] { Types.INTEGER };
     }
 
     public Class returnedClass() {
         return getEnumClass();
     }
 
     private Class getEnumClass() {
         if (getEnumClassName() == null) {
             throw new StudyCalendarError("required enumClass parameter not specified");
         }
         try {
             return Class.forName(getEnumClassName());
         } catch (ClassNotFoundException e) {
             throw new StudyCalendarError("enumClass " + getEnumClassName() + " does not exist", e);
         }
     }
 
     private String getEnumClassName() {
         return getParameterValues().getProperty(ENUM_CLASS_PARAM_KEY);
     }
 
     private Method getFactoryMethod() {
         return getParameterNamedMethod(FACTORY_METHOD_PARAM_KEY, new Class[] { Integer.TYPE });
     }
 
     private Method getKeyMethod() {
         return getParameterNamedMethod(KEY_METHOD_PARAM_KEY, NO_PARAMS);
     }
 
     private Method getParameterNamedMethod(String paramKey, Class[] parameterTypes) {
         String methodName = getParameterValues().getProperty(paramKey);
         try {
             return getEnumClass().getMethod(methodName, parameterTypes);
         } catch (NoSuchMethodException e) {
             throw new StudyCalendarError("enumClass " + getEnumClassName()
                 + " has no method named " + methodName, e);
         }
     }
 
     protected Object getKeyObject(ResultSet rs, String colname) throws SQLException {
        return rs.getInt(colname);
     }
 
     public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
         Object key = getKeyObject(rs, names[0]);
         Object value = null;
 
         if (key != null) {
             Method factoryMethod = getFactoryMethod();
             try {
                 value = factoryMethod.invoke(null, key);
             } catch (IllegalArgumentException iae) {
                 throw new StudyCalendarSystemException("Invocation of " + factoryMethod
                     + " with key=" + key + " (" + key.getClass().getName() + ") failed", iae);
             } catch (IllegalAccessException e) {
                 throw new StudyCalendarSystemException("Cannot access factoryMethod " + factoryMethod, e);
             } catch (InvocationTargetException e) {
                 throw new StudyCalendarSystemException("Invocation of " + factoryMethod + " failed", e);
             }
         }
 
         HibernateTypeUtils.logReturn(log, names[0], value);
         return value;
     }
 
     public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
         Method keyMethod = getKeyMethod();
         Object key = null;
         try {
             if (value != null) {
                 key = keyMethod.invoke(value, new Object[0]);
             }
         } catch (IllegalArgumentException iae) {
             throw new IllegalArgumentException("Could not call keyMethod " + keyMethod + " on value " + value, iae);
         } catch (IllegalAccessException e) {
             throw new StudyCalendarSystemException("Cannot access keyMethod " + keyMethod, e);
         } catch (InvocationTargetException e) {
             throw new StudyCalendarSystemException("Invocation of " + keyMethod + " failed", e);
         }
         HibernateTypeUtils.logBind(log, index, key);
         st.setObject(index, key, Types.INTEGER);
     }
 
     public Object deepCopy(Object value) throws HibernateException {
         return value;
     }
 
     public boolean isMutable() {
         return false;
     }
 
     public boolean equals(Object x, Object y) throws HibernateException {
         return ComparisonUtils.nullSafeEquals(x, y);
     }
 
     public int hashCode(Object x) throws HibernateException {
         return x == null ? 0 : x.hashCode();
     }
 
     public Serializable disassemble(Object value) throws HibernateException {
         return (Serializable) value;
     }
 
     public Object assemble(Serializable cached, Object owner) throws HibernateException {
         return cached;
     }
 
     public Object replace(Object original, Object target, Object owner) throws HibernateException {
         return original;
     }
 }
