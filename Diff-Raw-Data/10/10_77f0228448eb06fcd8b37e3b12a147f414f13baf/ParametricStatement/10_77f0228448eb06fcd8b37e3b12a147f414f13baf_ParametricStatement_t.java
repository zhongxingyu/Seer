 package org.xillium.data.persistence;
 
 import org.xillium.base.beans.Beans;
 import org.xillium.data.*;
 import java.sql.*;
 import java.util.*;
 import java.util.regex.*;
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 
 
 /**
  * A prepared SQL statement with named parameters. A parameter accepts null value if its name ends with '?'.
  *
  * TODO: remove nullable setting and checking, and let the database check column nullability
  */
 public class ParametricStatement {
     public static class Param {
         public static final int IN = 0x0001;
         public static final int OUT = 0x0002;
 
         public final boolean nullable;
         public final String name;
         public final int type;
         public final int direction;
 
         /**
          * Constructs a named formal parameter with name <code>n</code> and type <code>t</code>.
          * <p/>
          * A single-character prefix of either '-' or '+' before the name marks the parameter as either an OUT
          * or IN/OUT parameter, respectively.  Without any prefix, a parameter is regarded as an IN parameter.
          * <p/>
          * A single-character suffix of '?' after the name marks the parameter as accepting null values. Without
          * the suffix the parameter is non-null.
          * <p/>
          * Neither prefix or suffix is part of the actual parameter name.
          */
         public Param(String n, int t) {
             if (n.charAt(0) == '-') {
                 n = n.substring(1);
                 direction = OUT;
             } else if (n.charAt(0) == '+') {
                 n = n.substring(1);
                 direction = IN | OUT;
             } else {
                 direction = IN;
             }
             if (n.endsWith("?")) {
                 nullable = true;
                 name = n.substring(0, n.length()-1);
             } else {
                 nullable = false;
                 name = n;
             }
             type = t;
         }
 
         public String toString() {
             return name+",nullable:"+nullable+",type:"+type+",dir:"+direction;
         }
     };
 
     public ParametricStatement(Param[] parameters, String sql) throws IllegalArgumentException {
         _params = parameters;
         set(sql);
     }
 
     public ParametricStatement(String parameters) throws IllegalArgumentException {
         if (parameters != null && parameters.length() > 0) {
             String[] params = parameters.trim().split("\\s*,\\s*");
             _params = new Param[params.length];
             for (int i = 0; i < params.length; ++i) {
                 int colon = params[i].indexOf(':');
                 if (colon > 0) {
                     try {
                         int type = Integer.parseInt(params[i].substring(colon+1));
                         _params[i] = new Param(params[i].substring(0, colon), type);
                     } catch (NumberFormatException t) {
                         try {
                             int type = java.sql.Types.class.getField(params[i].substring(colon+1)).getInt(null);
                             _params[i] = new Param(params[i].substring(0, colon), type);
                         } catch (Exception x) {
                             throw new IllegalArgumentException("Parameter specification", x);
                         }
                     }
                 } else {
                     throw new IllegalArgumentException("Parameter specification: missing type in " + params[i]);
                 }
             }
         } else {
             _params = NoParams;
         }
     }
 
     public ParametricStatement() {
     }
 
     public ParametricStatement set(String sql) throws IllegalArgumentException {
     if (_params != null) {
         int count = 0;
         int qmark = sql.indexOf('?');
         while (qmark > 0) {
             ++count;
             qmark = sql.indexOf('?', qmark+1);
         }
         if (_params.length == count) {
             _sql = sql;
         } else {
             throw new IllegalArgumentException("Wrong number of parameters in '" + sql +'\'');
         }
     } else {
         List<Param> params = new ArrayList<Param>();
         Matcher matcher = PARAM_SYNTAX.matcher(sql);
         while (matcher.find()) {
             //System.err.println("[" + matcher.start() + ", " + matcher.end() + "] " + matcher.group());
             try {
                 params.add(new Param(matcher.group(1), java.sql.Types.class.getField(matcher.group(2)).getInt(null)));
             } catch (Exception x) {
                 throw new IllegalArgumentException("Parameter specification", x);
             }
         }
         _params = params.toArray(new Param[params.size()]);
         _sql = matcher.replaceAll("?");
     }
         return this;
     }
 
     public Param[] getParameters() {
         return _params;
     }
 
     public String getSQL() {
         return _sql;
     }
 
     protected <T extends PreparedStatement> T load(T statement, DataObject object) throws SQLException {
 //System.err.println("PreparedStatement: loading " + _sql);
         if (object != null && _params.length > 0) {
             Class<? extends DataObject> type = object.getClass();
 
             for (int i = 0; i < _params.length; ++i) {
                 if ((_params[i].direction & Param.IN) == 0) continue;
                 try {
                     Field field = Beans.getKnownField(type, _params[i].name);
                     Object value = field.get(object);
                     if (value != null) {
                         // NOTE: Class.isEnum() fails to return true if the field type is declared with a template parameter
                         if (Enum.class.isAssignableFrom(field.getType())) { // store as string or integer
                             if (Types.CHAR == _params[i].type || Types.VARCHAR == _params[i].type) {
                                 statement.setObject(i+1, value.toString(), _params[i].type);
                             } else {
                                 statement.setObject(i+1, ((Enum<?>)value).ordinal(), _params[i].type);
                             }
                         } else if (Calendar.class.isAssignableFrom(field.getType())) {
                             statement.setObject(i+1, new java.sql.Date(((Calendar)value).getTime().getTime()), _params[i].type);
                         } else {
                             statement.setObject(i+1, value, _params[i].type);
                         }
                     } else {
                         //throw new NoSuchFieldException(_params[i].name + ": null");
                         statement.setNull(i+1, _params[i].type);
                     }
                 } catch (NoSuchFieldException x) {
                     //if (_params[i].nullable) {
                     // LET database check the nullability of this column
                         statement.setNull(i+1, _params[i].type);
                     //} else {
                         //statement.close();
                         //throw new SQLException("Failed to retrieve non-nullable '" + _params[i].name + "' from DataObject (" + type.getName() + ')', x);
                     //}
                 } catch (Exception x) {
                     statement.close();
                     throw new SQLException("Exception in retrieval of '" + _params[i].name + "' from " + type.getName() + ": " + x.getMessage(), x);
                 }
             }
         }
         return statement;
     }
 
     /**
      * Executes an UPDATE or DELETE statement.
      *
      * @returns the number of rows affected
      */
     public int executeUpdate(Connection conn, DataObject object) throws SQLException {
         PreparedStatement statement = conn.prepareStatement(_sql);
         try {
             load(statement, object);
             return statement.executeUpdate();
         } finally {
             statement.close();
         }
     }
 
     /**
      * Executes a batch UPDATE or DELETE statement.
      *
      * @returns the number of rows affected
      */
     public int executeUpdate(Connection conn, DataObject[] objects) throws SQLException {
         PreparedStatement statement = conn.prepareStatement(_sql);
         try {
             for (DataObject object: objects) {
                 load(statement, object);
                 statement.addBatch();
             }
             int count = getAffectedRowCount(statement.executeBatch());
             return count;
         } finally {
             statement.close();
         }
     }
 
     /**
      * Executes a batch UPDATE or DELETE statement.
      *
      * @returns the number of rows affected
      */
     public int executeUpdate(Connection conn, Collection<? extends DataObject> objects) throws SQLException {
         PreparedStatement statement = conn.prepareStatement(_sql);
         try {
             for (DataObject object: objects) {
                 load(statement, object);
                 statement.addBatch();
             }
             int count = getAffectedRowCount(statement.executeBatch());
             return count;
         } finally {
             statement.close();
         }
     }
 
     /**
      * Executes an INSERT statement.
      *
      * @returns an array whose length indicates the number of rows inserted. If generatedKeys is true, the array
      *          contains the keys; otherwise the content of the array is not defined.
      */
     public long[] executeInsert(Connection conn, DataObject object, boolean generatedKeys) throws SQLException {
         PreparedStatement statement = conn.prepareStatement(_sql, generatedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
         try {
             load(statement, object);
             long[] keys = new long[statement.executeUpdate()];
             if (generatedKeys) {
                 ResultSet rs = statement.getGeneratedKeys();
                 for (int i = 0; rs.next(); ++i) {
                     keys[i] = rs.getLong(1);
                 }
                 rs.close();
             }
             return keys;
         } finally {
             statement.close();
         }
     }
 
     /**
      * Executes a batch INSERT statement.
      *
      * @returns the number of rows inserted.
      */
     public int executeInsert(Connection conn, DataObject[] objects) throws SQLException {
         PreparedStatement statement = conn.prepareStatement(_sql);
         try {
             for (DataObject object: objects) {
                 load(statement, object);
                 statement.addBatch();
             }
             return getAffectedRowCount(statement.executeBatch());
         } finally {
             statement.close();
         }
     }
 
     /**
      * Executes a batch INSERT statement.
      *
      * @returns the number of rows inserted.
      */
     public int executeInsert(Connection conn, Collection<DataObject> objects) throws SQLException {
         PreparedStatement statement = conn.prepareStatement(_sql);
         try {
             for (DataObject object: objects) {
                 load(statement, object);
                 statement.addBatch();
             }
             return getAffectedRowCount(statement.executeBatch());
         } finally {
             statement.close();
         }
     }
 
     /**
     * Executes a callable statement that performs updates.
      *
      * @returns the number of rows affected
      */
     public int executeProcedure(Connection conn, DataObject object) throws SQLException {
         CallableStatement statement = conn.prepareCall(_sql);
         try {
             for (int i = 0; i < _params.length; ++i) {
                 if ((_params[i].direction & Param.OUT) == 0) continue;
                 statement.registerOutParameter(i+1, _params[i].type);
             }
             load(statement, object);
            statement.execute();

            int result = statement.getUpdateCount();
 
             Class<? extends DataObject> type = object.getClass();
             for (int i = 0; i < _params.length; ++i) {
                 if ((_params[i].direction & Param.OUT) == 0) continue;
                 try {
                     Beans.setValue(object, Beans.getKnownField(type, _params[i].name), statement.getObject(i+1));
                 } catch (NoSuchFieldException x) {
                     // ignore
                 } catch (Exception x) {
                     throw new SQLException("Exception in storage of '" + _params[i].name + "' into DataObject (" + type.getName() + ')', x);
                 }
             }
            return result;
         } finally {
             statement.close();
         }
     }
 
     public void setTag(String t) {
         _tag = t;
     }
 
     public String getTag() {
         return _tag;
     }
 
     public StringBuilder print(StringBuilder sb) {
         sb.append('[');
         for (Param param: _params) {
             sb.append('<').append(param.name).append('>');
         }
         sb.append(']').append(_sql);
         return sb;
     }
 
     private static final Param[] NoParams = new Param[0];
     private static final Pattern PARAM_SYNTAX = Pattern.compile(":([-+]?\\w+\\??):(\\w+)");
     private /*final*/ Param[] _params;
     protected String _sql;
     protected String _tag;
 
     private static int getAffectedRowCount(int[] results) {
         int count = 0;
         for (int affected: results) {
             switch (affected) {
             case Statement.SUCCESS_NO_INFO:
                 count++;
                 break;
             case Statement.EXECUTE_FAILED:
                 break;
             default:
                 count += affected;
                 break;
             }
         }
         return count;
     }
 
 /*
     @SuppressWarnings("unchecked")
     protected void setValue(Object object, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
         if (value == null) {
             //if (Number.class.isAssignableFrom(field.getType())) {
                 //value = BigDecimal.ZERO;
             //} else return;
             return;
         }
 
         try {
             field.setAccessible(true);
             field.set(object, value);
         } catch (IllegalArgumentException x) {
             @SuppressWarnings("rawtypes")
             Class ftype = field.getType();
             if (value instanceof Number) {
                 // size of "value" bigger than that of "field"?
                 try {
                     Number number = (Number)value;
                     if (Double.TYPE == ftype || Double.class.isAssignableFrom(ftype)) {
                         field.set(object, number.doubleValue());
                     } else if (Float.TYPE == ftype || Float.class.isAssignableFrom(ftype)) {
                         field.set(object, number.floatValue());
                     } else if (Long.TYPE == ftype || Long.class.isAssignableFrom(ftype)) {
                         field.set(object, number.longValue());
                     } else if (Integer.TYPE == ftype || Integer.class.isAssignableFrom(ftype)) {
                         field.set(object, number.intValue());
                     } else if (Short.TYPE == ftype || Short.class.isAssignableFrom(ftype)) {
                         field.set(object, number.shortValue());
                     } else {
                         field.set(object, number.byteValue());
                     }
                 } catch (Throwable t) {
                     throw new IllegalArgumentException(t);
                 }
             } else if (value instanceof java.sql.Timestamp) {
                 try {
                     field.set(object, new java.sql.Date(((java.sql.Timestamp)value).getTime()));
                 } catch (Throwable t) {
                     throw new IllegalArgumentException(t);
                 }
             } else if ((value instanceof String) && Enum.class.isAssignableFrom(ftype)) {
                 try {
                     field.set(object, Enum.valueOf(ftype, (String)value));
                 } catch (Throwable t) {
                     throw new IllegalArgumentException(t);
                 }
             } else {
                 throw new IllegalArgumentException(x);
             }
         }
 
     }
 */
 }
