 package gov.nih.nci.iso21090.hibernate.usertype;
 
 import gov.nih.nci.iso21090.EntityNamePartQualifier;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.hibernate.MappingException;
 import org.hibernate.TypeMismatchException;
 import org.hibernate.usertype.UserType;
 
 import java.io.Serializable;
 
 /**
  * EntityPartQualifier UserType class maps the EntityPartQualifiers in the Java class to varchar in the database.
  * 
  * @author parmarv
  *
  */
 @SuppressWarnings("PMD.TooManyMethods")
 public class EntityPartNameQualifierUserType implements UserType {
 
     /**
      * SQL type for this usertype.
      */
     private static final int[] SQL_TYPES = {Types.VARCHAR};
 
     /**
      * @return returns SQL type for the column to which the UserType is mapped
      */
     @SuppressWarnings("PMD.MethodReturnsInternalArray")
     public int[] sqlTypes() {
         return SQL_TYPES;
     }
 
     /**
      * @return returns the class of the instance being managed by the UserType
      */
     public Class returnedClass() {
        return Set.class;
     }
 
     /**
      * @param x first object to compare
      * @param y second object to compare
      * @return comparison result
      * @throws HibernateException
      */
     @SuppressWarnings("PMD.CompareObjectsWithEquals")
     public boolean equals(Object x, Object y) {
         if (x == y) {
             return true;
         } else if (x == null || y == null) {
             return false;
         } else {
             return x.equals(y);
         }
     }
 
     /**
      * @param resultSet resultset object
      * @param names names of the columns in the resultset
      * @param owner parent object on which the value is to be set
      * @return returns Set<EntityPartQualifier> object 
      * @throws SQLException throws exception when error occurs in accessing the resultSet
      */
     public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) 
             throws SQLException {
         String strEntityPartQualifier = resultSet.getString(names[0]);
         
        if (strEntityPartQualifier == null) {
        	return null;
        }
        
        Set<EntityNamePartQualifier> entityNamePartyQualifierSet = new HashSet();
 
         String[] qualifiers = strEntityPartQualifier.split(",");

        for (String enpqVal : qualifiers){
        	try {
        		entityNamePartyQualifierSet.add(Enum.valueOf(EntityNamePartQualifier.class, enpqVal));
        	} catch (IllegalArgumentException e){
        		throw new TypeMismatchException(e);
         	}
 	    }
     
         return entityNamePartyQualifierSet;
     }
 
     /**
      * @param statement prepared statement object
      * @param value value being set in the statement
      * @param index location of the value in the statement
      * @throws SQLException throws exception when error occurs in accessing the statement
      */
     public void nullSafeSet(PreparedStatement statement, Object value, int index) 
             throws SQLException {
         if (value == null) {
             statement.setString(index, null);
         } else {
         	String temp="";
         	Set<EntityNamePartQualifier> ss = (Set<EntityNamePartQualifier>) value;
         	int count = ss.size();
         	for(EntityNamePartQualifier entityNamePartQualifier : ss){
         		temp = temp + entityNamePartQualifier.name();
         		if(count > 1){
        			temp = temp+",";
         		}
         		count--;
         	}
             statement.setString(index, temp);
         }
     }
 
     /**
      * @param value value being copied
      * @return copied value
      */
     public Object deepCopy(Object value) {
        return value;
     }
 
     /**
      * @return returns false
      */
     public boolean isMutable() {
         return false;
     }
 
     /**
      * @param arg0 object to be assembled
      * @param arg1 object to be assembled
      * @return returns assembled object
      */
     public Object assemble(Serializable arg0, Object arg1) {
         return (Serializable) deepCopy(arg0);
     }
 
     /**
      * @param arg0 object to be disassembled
      * @return returns disassembled object
      */
     public Serializable disassemble(Object arg0) {
         return (Serializable) deepCopy(arg0);
     }
 
     /**
      * @param arg0 object whose hashcode is to be determined 
      * @return returns 0
      */
     public int hashCode(Object arg0) {
         return 0;
     }
 
     /**
      * @param arg0 argument 0
      * @param arg1 argument 1
      * @param arg2 argument 2
      * @return returns arg0
      */
     public Object replace(Object arg0, Object arg1, Object arg2) {
         return arg0;
     }
 }
