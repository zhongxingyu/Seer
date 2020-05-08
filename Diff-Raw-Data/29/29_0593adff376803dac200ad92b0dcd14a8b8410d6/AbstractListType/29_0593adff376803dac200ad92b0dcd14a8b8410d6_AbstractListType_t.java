 package de.flower.rmt.model.type;
 
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.HibernateException;
 import org.hibernate.type.descriptor.JdbcTypeNameMapper;
 import org.hibernate.usertype.UserType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.Serializable;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User type to store lists of objects in a single varchar.
  *
  * @param T type of list element
  * @author flowerrrr
  */
 public abstract class AbstractListType<T> implements UserType {
 
     private final static Logger log = LoggerFactory.getLogger(AbstractListType.class);
 
     private static final int[] SQL_TYPES = new int[]{Types.VARCHAR,};
 
     @Override
     public int[] sqlTypes() {
         return SQL_TYPES;
     }
 
     @Override
     public Class returnedClass() {
         return ArrayList.class;
     }
 
     @Override
     public boolean equals(final Object x, final Object y) throws HibernateException {
         if (x == y) {
             return true;
         }
         if (x == null || y == null) {
             return false;
         }
         List<T> dtx = (List<T>) x;
         List<T> dty = (List<T>) y;
 
         return dtx.equals(dty);
     }
 
     @Override
     public int hashCode(final Object x) throws HibernateException {
         return x.hashCode();
     }
 
     @Override
     public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
         String serialized = rs.getString(names[0]);
         if (log.isTraceEnabled()) {
             log.trace("found [" + serialized + "] as column [" + names[0] + "]");
         }
         return assemble(serialized, owner);
     }
 
     @Override
     public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
         String string = (String) disassemble(value);
         if (log.isTraceEnabled()) {
             log.trace("binding parameter [" + index + "] as [" + JdbcTypeNameMapper.getTypeName(SQL_TYPES[0]) + "] - " + string);
         }
         if (value == null) {
             st.setNull(index, SQL_TYPES[0]);
         } else {
             st.setString(index, string);
         }
     }
 
     @Override
     public Object deepCopy(final Object value) throws HibernateException {
         return (value == null) ? null : new ArrayList((List) value);
     }
 
     @Override
     public boolean isMutable() {
         return true;
     }
 
     @Override
     public Serializable disassemble(final Object value) throws HibernateException {
         return list2String((List<T>) value);
     }
 
     @Override
     public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
         return string2List((String) cached);
     }
 
     @Override
     public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
         return deepCopy(original);
     }
 
     public String list2String(final List<T> list) {
        if (list == null) {
            return null;
        }
        List<String> strings = new ArrayList<String>();
         for (T object : list) {
             strings.add(toString(object));
         }
         return StringUtils.join(strings, "|");
     }
 
     public List<T> string2List(final String string) {
         if (string == null) {
             return null;
         }
         String[] strings = StringUtils.split(string, "|");
         List<T> list = new ArrayList();
         for (String s : strings) {
             list.add(fromString(s));
         }
         return list;
     }
 
     /**
      * Subclasses should override these methods with marshalling/unmarshalling code.
      *
      * @param list
      * @return
      */
     public abstract String toString(T object);
 
     public abstract T fromString(String string);
 
     public static class StringListType extends AbstractListType<String> {
 
         @Override
         public String toString(final String object) {
             return object;
         }
 
         @Override
         public String fromString(final String string) {
             return string;
         }
     }
 }
