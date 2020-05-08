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
        return rs.getInt(colname);
        // The above behavior is incorrect -- it returns 0 for NULL, and so it results in
        // incorrect values when an enum uses the code 0.  (All other enums will be fine.)
        // Unfortunately, caAERS has several misdefined tables which use character columns
        // for integer enum codes, meaning that the correct solution (below) breaks for
        // them.  TODO: resolve this with caAERS team.  -- RMS20080220
        // return rs.getObject(colname);
     }
 }
