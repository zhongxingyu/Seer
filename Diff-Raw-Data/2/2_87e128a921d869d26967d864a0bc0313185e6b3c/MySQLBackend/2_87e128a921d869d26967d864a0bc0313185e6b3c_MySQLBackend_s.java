 /**
  * Unframework Store Library
  *
  * Copyright 2011, Nick Matantsev
  * Dual-licensed under the MIT or GPL Version 2 licenses.
  */
 
 package store;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import javax.sql.DataSource;
 
 /**
  * Simple MySQL data store backend.
  */
 public class MySQLBackend implements Store.Backend {
     private final DataSource ds;
     private final Naming naming;
 
     public MySQLBackend(DataSource ds) {
         this(ds, new Naming());
     }
 
     public MySQLBackend(DataSource ds, Naming naming) {
         this.ds = ds;
         this.naming = naming;
     }
 
     public static class Naming {
         public String table(Class objectClass) {
             return objectClass.getSimpleName();
         }
 
         public String tableIdColumn(Class objectClass) {
             return "id";
         }
 
         public String tableColumn(Class objectClass, String field) {
             return field;
         }
     }
 
     private static String bt(String nativeName) {
         return nativeName.replace("`", "``");
     }
 
     public class IdentityImpl implements Store.Backend.Identity {
         private final String table;
         private final int rowId;
 
         private IdentityImpl(String table, int rowId) {
             this.table = table;
             this.rowId = rowId;
         }
 
         @Override
         public int hashCode() {
             return rowId;
         }
 
         @Override
         public boolean equals(Object obj) {
             if(obj instanceof IdentityImpl) {
                 IdentityImpl id = (IdentityImpl)obj;
                 return id.rowId == this.rowId && id.table.equals(this.table);
             }
 
             return false;
         }
     }
 
     public abstract class ColumnImpl implements Store.Backend.Column {
         protected final String table, column, idColumn;
 
         private ColumnImpl(Class objectClass, String field) {
             this.table = naming.table(objectClass);
             this.column = naming.tableColumn(objectClass, field).toString();
             this.idColumn = naming.tableIdColumn(objectClass);
         }
 
         abstract Object readFirstValue(ResultSet rs) throws SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
         abstract void setValue(PreparedStatement ps, int i, Object value) throws SQLException;
     }
 
     public Getter createGetter(Column pcol) {
         final ColumnImpl col = (ColumnImpl)pcol;
         final String sql = "select `" + bt(col.column) + "` from `" + bt(col.table) + "` where `" + bt(col.idColumn) + "` = ?";
 
         return new Getter() {
             public Object invoke(Identity pid) throws SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
                 IdentityImpl id = (IdentityImpl)pid;
 
                 Connection conn = ds.getConnection();
                 try {
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ps.setInt(1, id.rowId);
                     ps.execute();
 
                     ResultSet rs = ps.getResultSet();
                     if(!rs.next())
                         throw new RuntimeException("object ID not found"); // TODO: dedicated error
 
                     return col.readFirstValue(rs);
                 } finally {
                     conn.close();
                 }
             }
         };
     }
 
     public Setter createSetter(final Column[] cols) {
         final String sql;
         {
             StringBuffer sb = new StringBuffer();
             sb.append("update `").append(bt(((ColumnImpl)cols[0]).table)).append("` set ");
 
             boolean first = true;
             for(Column col: cols) {
                 sb.append(first ? "" : ", ");
                 sb.append("`").append(bt(((ColumnImpl)col).column)).append("` = ?");
                 first = false;
             }
 
             sb.append(" where `" + bt(((ColumnImpl)cols[0]).idColumn) + "` = ?");
 
             sql = sb.toString();
         }
 
         return new Setter() {
             public void invoke(Identity pid, Object[] args) throws SQLException {
                 IdentityImpl id = (IdentityImpl)pid;
 
                 Connection conn = ds.getConnection();
                 try {
                     CallableStatement cs = conn.prepareCall(sql);
                     for(int i = 0; i < cols.length; i++) {
                         ((ColumnImpl)cols[i]).setValue(cs, i + 1, args[i]);
                     }
                     cs.setInt(cols.length + 1, id.rowId);
                     cs.execute();
 
                     // TODO: verify num updated rows?
                 } finally {
                     conn.close();
                 }
             }
         };
     }
 
     public Finder createFinder(final Column[] cols) {
         return new Finder() {
             public Collection<Identity> invoke(Object[] args) throws Exception {
                 String table = ((ColumnImpl)cols[0]).table;
                 String idCol = ((ColumnImpl)cols[0]).idColumn;
 
                 final String sql;
                 {
                     StringBuffer sb = new StringBuffer();
                     sb.append("select `" + bt(idCol) + "` from `").append(bt(table)).append("` where ");
 
                     boolean first = true;
                     for(int i = 0; i < cols.length; i++) {
                         sb.append(first ? "" : " and ");
                        sb.append("`").append(bt(((ColumnImpl)cols[i]).column)).append(args[i] == null ? "is null" : "` = ?");
                         first = false;
                     }
 
                     sql = sb.toString();
                 }
 
                 Connection conn = ds.getConnection();
                 try {
                     PreparedStatement ps = conn.prepareStatement(sql);
                     int argIndex = 1;
                     for(int i = 0; i < cols.length; i++) {
                         // NULL arguments do not need to be set
                         if(args[i] == null)
                             continue;
 
                         ((ColumnImpl)cols[i]).setValue(ps, argIndex, args[i]);
                         argIndex++;
                     }
                     ps.execute();
 
                     ResultSet rs = ps.getResultSet();
 
                     ArrayList<Identity> result = new ArrayList<Identity>();
                     while(rs.next())
                         result.add(new IdentityImpl(table, rs.getInt(1)));
 
                     return result;
                 } finally {
                     conn.close();
                 }
             }
         };
     }
 
     public Identity createIdentity(Class objectClass) throws SQLException {
         final String table = naming.table(objectClass);
         final String idCol = naming.tableIdColumn(objectClass);
 
         Connection conn = ds.getConnection();
         try {
             CallableStatement cs = conn.prepareCall("insert into `" + bt(table) + "` (`" + bt(idCol) + "`) values (NULL)");
             cs.execute();
 
             ResultSet rs = cs.getGeneratedKeys();
             if(!rs.next())
                 throw new RuntimeException("no created ID returned"); // TODO: custom error
 
             return new IdentityImpl(table, rs.getInt(1));
         } finally {
             conn.close();
         }
     }
 
     public Identity intern(Class objectClass, String externalId) {
         int id = Integer.parseInt(externalId.toString()); // NOTE: triggering NPE explicitly
         return new IdentityImpl(naming.table(objectClass), id);
     }
 
     public String extern(Identity id) {
         return Integer.toString(((IdentityImpl)id).rowId);
     }
 
     public Column createColumn(Class objectClass, String field, final Class fieldType, boolean isIdentity) throws NoSuchMethodException {
         if(isIdentity) {
 
             // identities are always treated as ints
             return new ColumnImpl(objectClass, field) {
                 @Override
                 Object readFirstValue(ResultSet rs) throws SQLException {
                     return new IdentityImpl(table, rs.getInt(1));
                 }
 
                 @Override
                 void setValue(PreparedStatement ps, int i, Object value) throws SQLException {
                     ps.setInt(i, ((IdentityImpl)value).rowId);
                 }
             };
 
         } else if(fieldType == String.class) {
 
             // strings correspond to VARCHAR
             return new ColumnImpl(objectClass, field) {
                 @Override
                 Object readFirstValue(ResultSet rs) throws SQLException {
                     return rs.getString(1);
                 }
 
                 @Override
                 void setValue(PreparedStatement ps, int i, Object value) throws SQLException {
                     if(value == null)
                         ps.setNull(i, Types.VARCHAR);
                     else
                         ps.setString(i, (String)value);
                 }
             };
 
         } else if(fieldType == Integer.class) {
 
             // integers correspond to INT
             return new ColumnImpl(objectClass, field) {
                 @Override
                 Object readFirstValue(ResultSet rs) throws SQLException {
                     int r = rs.getInt(1);
                     return rs.wasNull() ? null : Integer.valueOf(r);
                 }
 
                 @Override
                 void setValue(PreparedStatement ps, int i, Object value) throws SQLException {
                     if(value == null)
                         ps.setNull(i, Types.INTEGER);
                     else
                         ps.setInt(i, (Integer)value);
                 }
             };
 
         } else if(fieldType == Date.class) {
 
             // dates are stored as BIGINT milliseconds since epoch
             return new ColumnImpl(objectClass, field) {
                 @Override
                 Object readFirstValue(ResultSet rs) throws SQLException {
                     long r = rs.getLong(1);
                     return rs.wasNull() ? null : new Date(r);
                 }
 
                 @Override
                 void setValue(PreparedStatement ps, int i, Object value) throws SQLException {
                     if(value == null)
                         ps.setNull(i, Types.BIGINT);
                     else
                         ps.setLong(i, ((Date)value).getTime());
                 }
             };
 
         } else if(fieldType.isEnum()) {
 
             // each enum is stored as VARCHAR of the value's simple name
             return new ColumnImpl(objectClass, field) {
                 @Override
                 Object readFirstValue(ResultSet rs) throws SQLException {
                     String name = rs.getString(1);
                     return name == null ? null : Enum.valueOf(fieldType, name);
                 }
 
                 @Override
                 void setValue(PreparedStatement ps, int i, Object value) throws SQLException {
                     if(value == null)
                         ps.setNull(i, Types.VARCHAR);
                     else
                         ps.setString(i, ((Enum)value).name());
                 }
             };
 
         } else {
 
             // generic values are stored as VARCHAR via their toString() method; when reading, constructor with a single String argument is called
             final Constructor ctor = fieldType.getConstructor(String.class);
             return new ColumnImpl(objectClass, field) {
                 @Override
                 Object readFirstValue(ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
                     String val = rs.getString(1);
                     return ctor.newInstance(val);
                 }
 
                 @Override
                 void setValue(PreparedStatement ps, int i, Object value) throws SQLException {
                     if(value == null)
                         ps.setNull(i, Types.VARCHAR);
                     else
                         ps.setString(i, value.toString());
                 }
             };
 
         }
     }
 }
