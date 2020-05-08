 package org.jbpm.db.hibernate;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import org.hibernate.type.StringType;
 import org.jbpm.JbpmException;
 
 public class StringMax extends StringType implements org.hibernate.usertype.ParameterizedType {
 
   private static final long serialVersionUID = 1L;
   
   int length = 4000;
 
  public void set(PreparedStatement st, Object value, int index) throws SQLException {
     String string = (String)value;
     if ( (value!=null)
          && (string.length()>length)
        ) {
       value = string.substring(0, length);
     }
     super.set(st, value, index);
   }
 
   public void setParameterValues(Properties parameters) {
     if ( (parameters!=null)
          && (parameters.containsKey("length"))
        ){
       try {
         length = Integer.parseInt(parameters.getProperty("length"));
       } catch (NumberFormatException e) {
         throw new JbpmException("hibernate column type 'string_max' can't parse value '"+parameters.getProperty("length")+"' as a max length.  default is 4000.", e);
       }
     }
   }
 }
