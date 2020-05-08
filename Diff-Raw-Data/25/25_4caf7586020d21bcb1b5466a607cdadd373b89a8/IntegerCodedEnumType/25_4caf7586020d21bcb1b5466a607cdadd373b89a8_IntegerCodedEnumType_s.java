 package gov.nih.nci.cabig.ctms.tools.hibernate;
 
 import java.sql.Types;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * @author Rhett Sutphin
  */
 public class IntegerCodedEnumType extends CodedEnumType {
     @Override
     protected int codeSqlType() {
         return Types.INTEGER;
     }
 
     @Override
     @SuppressWarnings("RawUseOfParameterizedType")
     protected Class codeJavaType() {
         return Integer.TYPE;
     }
 
     @Override
     protected Object getKeyObject(ResultSet rs, String colname) throws SQLException {
        return rs.getObject(colname);
     }
 }
