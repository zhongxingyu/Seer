 package org.gsoft.openserv.util.jpa;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Properties;
 
 import org.hibernate.HibernateException;
 import org.hibernate.engine.spi.SessionImplementor;
 import org.hibernate.usertype.ParameterizedType;
 import org.hibernate.usertype.UserType;
 
 public class GenericEnumUserType implements UserType, ParameterizedType {
 	private Class<?> clazz = null;
 
 	private static final int[] SQL_TYPES = { Types.BIGINT };
 
 	public int[] sqlTypes() {
 		return SQL_TYPES;
 	}
 
 	public Class<?> returnedClass() {
 		return clazz;
 	}
 
 	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImpl, Object owner)
 			throws HibernateException, SQLException {
 		Long id = resultSet.getLong(names[0]);
 		if (!resultSet.wasNull()) {
 			try {
 				Method valueOfMethod = this.clazz.getMethod("forID", new Class[] { Long.class });
 				return valueOfMethod.invoke(clazz, new Object[] { id });
 			} catch (final Exception e) {
 				throw new HibernateException(
 						"Exception while invoking forID method on enumeration class '" + this.clazz + "'", e);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void nullSafeSet(PreparedStatement preparedStatement, Object value,
			int index, SessionImplementor sessionImpl) throws SQLException {
 		if (null == value) {
 			preparedStatement.setNull(index, Types.BIGINT);
 		} else {
 			try {
 				Method identifierMethod = this.clazz.getMethod("getID", new Class[0]);
 				preparedStatement.setLong(index, (Long) identifierMethod.invoke(value, new Object[0]));
 			} catch (final Exception e) {
 				throw new HibernateException(
 						"Exception while invoking getID method on enumeration class '" + this.clazz + "'", e);
 			}
 		}
 	}
 
	public Object deepCopy(Object value){
 		return value;
 	}
 
 	public boolean isMutable() {
 		return false;
 	}
 
	public Object assemble(Serializable cached, Object owner){
 		return cached;
 	}
 
	public Serializable disassemble(Object value){
 		return (Serializable) value;
 	}
 
	public Object replace(Object original, Object target, Object owner){
 		return original;
 	}
 
	public int hashCode(Object x){
 		return x.hashCode();
 	}
 
	public boolean equals(Object x, Object y){
		if (x == y){
 			return true;
		}
		if (null == x || null == y){
 			return false;
		}
 		return x.equals(y);
 	}
 
 	@Override
 	public void setParameterValues(Properties parameters) {
         final String enumClassName = parameters.getProperty( "enumClass" );
         try {
             this.clazz = Class.forName( enumClassName ).asSubclass( Enum.class );
         }
         catch ( final ClassNotFoundException cfne ) {
             throw new HibernateException( "Enum class not found", cfne );
         }
 	}
 }
