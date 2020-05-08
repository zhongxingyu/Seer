 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2004 Tim Pizey
  * 
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Tim Pizey (timp At paneris.org)
  *
  */
 
 package org.melati.poem.dbms;
 
 import java.sql.ResultSetMetaData;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 //import java.sql.Types;
 
 //import org.melati.poem.Table;
 
 import org.melati.poem.BooleanPoemType;
 import org.melati.poem.BinaryPoemType;
 import org.melati.poem.BigDecimalPoemType;
 import org.melati.poem.DoublePoemType;
 import org.melati.poem.PoemType;
 import org.melati.poem.SQLPoemType;
 //import org.melati.poem.BinaryPoemType;
 //import org.melati.poem.DoublePoemType;
 import org.melati.poem.IntegerPoemType;
 import org.melati.poem.LongPoemType;
 import org.melati.poem.StringPoemType;
 
 //import org.melati.poem.DuplicateKeySQLPoemException;
 //import org.melati.poem.NoSuchColumnPoemException;
 //import org.melati.poem.SeriousPoemException;
 //import org.melati.poem.SQLPoemException;
 import org.melati.poem.util.StringUtils;
 
 /**
  * A Driver for Oracle (http://www.oracle.com/).
  * 
  * @todo Testing required, code has been added to keep up with the interface 
  * without testing.
  **/
 public class Oracle extends AnsiStandard {
 
   /**
    * Oracle does not have a pleasant <code>TEXT</code> 
    * datatype, so we use an arbetary value in a 
    * <code>VARCHAR</code>.
    */
   public static int oracleTextHack = 266;
 
   /**
    * Constructor.
    */
   public Oracle() {
     setDriverClassName("oracle.jdbc.OracleDriver");
   }
 
   /**
    *  Get the user we are connected as and return that as the schema.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getSchema()
    * @see org.melati.poem.dbms.Dbms#getSchema()
    */
   public String getSchema() {
     return schema;
   }
   
   /**
    * We can.
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#canDropColumns()
    */
   public boolean canDropColumns() {
     return true;
   }
 
   /*
 
   public String preparedStatementPlaceholder(PoemType type) {
     if (type instanceof IntegerPoemType)
       return "CAST(? AS INT4)";
     else if (type instanceof LongPoemType)
       return "CAST(? AS INT8)";
     else if (type instanceof DoublePoemType)
       return "CAST(? AS FLOAT8)";
     else 
       return "?";
   }
 */
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getStringSqlDefinition(int)
    */
   public String getStringSqlDefinition(int size) throws SQLException {
     if (size < 0) { 
        return "VARCHAR(" + oracleTextHack + ")";
     }
        return super.getStringSqlDefinition(size);
   }
 
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getLongSqlDefinition()
    */
   public String getLongSqlDefinition() {
     return "NUMBER";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getSqlDefinition(java.lang.String)
    */
   public String getSqlDefinition(String sqlTypeName) {
     if (sqlTypeName.equals("BOOLEAN")) {
       return ("CHAR(1)");
     }
     return super.getSqlDefinition(sqlTypeName);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#sqlBooleanValueOfRaw(java.lang.Object)
    */
   public String sqlBooleanValueOfRaw(Object raw) {
     if (((Boolean)raw).booleanValue()) 
       return "1";
     else 
       return "0";
   }
 
   /**
    * Translates an Oracle Boolean into a Poem <code>BooleanPoemType</code>.
    */
   public static class OracleBooleanPoemType extends BooleanPoemType {
 
     /**
      * Constructor.
      * @param nullable nullability
      */
     public OracleBooleanPoemType(boolean nullable) {
        super(nullable);
      }
      
      protected Object _getRaw(ResultSet rs, int col) throws SQLException {
        synchronized (rs) {
          boolean v = rs.getBoolean(col);
          return rs.wasNull() ? null : (v ? Boolean.TRUE : Boolean.FALSE);
        }
      }
 
      protected void _setRaw(PreparedStatement ps, int col, Object bool) 
          throws SQLException {
        ps.setInt(col, ((Boolean) bool).booleanValue() ? 1 : 0);
      }
 
    }
 
    /**
     * Translates a Oracle String into a Poem <code>StringPoemType</code>.
     */ 
   public static class OracleStringPoemType extends StringPoemType {
 
     /**
      * Constructor.
      * @param nullable nullability
      * @param size size
      */
       public OracleStringPoemType(boolean nullable, int size) {
         super(nullable, size);
       }
 
       protected boolean _canRepresent(SQLPoemType other) {
         return (getSize() == oracleTextHack && 
                 ((StringPoemType)other).getSize() == -1)
                ||
                (getSize() >= ((StringPoemType)other).getSize());
       }
 
       /**
        * {@inheritDoc}
        * @see org.melati.poem.BasePoemType#canRepresent(PoemType)
        */
       public PoemType canRepresent(PoemType other) {
         return other instanceof StringPoemType &&
                _canRepresent((StringPoemType)other) &&
                !(!getNullable() && ((StringPoemType)other).getNullable()) ?
                  other : null;
       }
 
     }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getBinarySqlDefinition(int)
    */
   public String getBinarySqlDefinition(int size) throws SQLException {
     return "BLOB";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#unreservedName(java.lang.String)
    */
   public String unreservedName(String name) {
     if(name.equalsIgnoreCase("user")) name = "melati_" + name;
     if(name.equalsIgnoreCase("group")) name = "melati_" + name;
     return name.toUpperCase();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#melatiName(java.lang.String)
    */
   public String melatiName(String name) {
     if (name == null) return name;
     if(name.equalsIgnoreCase("melati_user")) name = "user";
     if(name.equalsIgnoreCase("melati_group")) name = "group";
     return name.toLowerCase();
   }
 
   /**
    * An Object Id <code>PoemType</code>.
    */
   /*
 
   public static class BlobPoemType extends IntegerPoemType {
       public BlobPoemType(boolean nullable) {
           super(Types.INTEGER, "BLOB", nullable);
       }
 
       protected boolean _canRepresent(SQLPoemType other) {
           return other instanceof BinaryPoemType;
       }
 
       public PoemType canRepresent(PoemType other) {
           return other instanceof BinaryPoemType &&
                  !(!getNullable() && 
        ((BinaryPoemType)other).getNullable()) ? other : null;
       }
   }
 */
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#canRepresent
    *          (org.melati.poem.PoemType, org.melati.poem.PoemType)
    */
   public PoemType canRepresent(PoemType storage, PoemType type) {
     if ((storage instanceof IntegerPoemType &&
         type instanceof BigDecimalPoemType) && 
        !(!storage.getNullable() && type.getNullable())){
       return type;
     }
     if ((storage instanceof IntegerPoemType &&
           type instanceof LongPoemType) && 
          !(!storage.getNullable() && type.getNullable())) {
         return type;
     } else {
       return storage.canRepresent(type);
     }
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#defaultPoemTypeOfColumnMetaData
    */
   public SQLPoemType defaultPoemTypeOfColumnMetaData(ResultSet md)
       throws SQLException {
     System.err.println("Type:"+md.getString("TYPE_NAME"));
 
     ResultSetMetaData rsmd = md.getMetaData();
     int cols = rsmd.getColumnCount();
     for (int i = 1; i <= cols; i++) {
       String table = rsmd.getTableName(i);
       System.err.println("table name: " + table);
       String column = rsmd.getColumnName(i);
       System.err.println("column name: " + column);
       int type = rsmd.getColumnType(i);
       System.err.println("type: " + type);
       String typeName = rsmd.getColumnTypeName(i);
       System.err.println("type Name: " + typeName);
       String className = rsmd.getColumnClassName(i);
       System.err.println("class Name: " + className);
       System.err.println("String val: " + md.getString(i));
       System.err.println("");
     }
 
     if(md.getString("TYPE_NAME").equals("VARCHAR2"))
       return 
           new OracleStringPoemType(md.getInt("NULLABLE")==
                                       DatabaseMetaData.columnNullable, 
                                   md.getInt("COLUMN_SIZE"));
     if(md.getString("TYPE_NAME").equals("CHAR"))
       return 
           new OracleBooleanPoemType(md.getInt("NULLABLE")==
                                       DatabaseMetaData.columnNullable);
     if(md.getString("TYPE_NAME").equals("BLOB"))
       return new BinaryPoemType(
                     md.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                     md.getInt("COLUMN_SIZE"));
     if(md.getString("TYPE_NAME").equals("FLOAT"))
       return new DoublePoemType(
                     md.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
     SQLPoemType t = 
       md.getString("TYPE_NAME").equals("NUMBER") ?
           new IntegerPoemType(md.getInt("NULLABLE") ==
                               DatabaseMetaData.columnNullable) :
           super.defaultPoemTypeOfColumnMetaData(md);
     System.err.println("SQLType:"+t);
     return t;
   }
   
   /**
   * NOTE This is NOT case insensitive,
    * Term2 has its quotes stripped.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#caseInsensitiveRegExpSQL
    * @see org.melati.poem.dbms.Dbms#caseInsensitiveRegExpSQL
    */
   public String caseInsensitiveRegExpSQL(String term1, String term2) {
     if (StringUtils.isQuoted(term2)) {
       term2 = term2.substring(1, term2.length() - 1);
     } 
     term2 = StringUtils.quoted(StringUtils.quoted(term2, '%'), '\'');
     
     return term1 + " LIKE " + term2;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getForeignKeyDefinition
    */
   public String getForeignKeyDefinition(String tableName, String fieldName, 
       String targetTableName, String targetTableFieldName, String fixName) {
     StringBuffer sb = new StringBuffer();
     sb.append(" ADD (CONSTRAINT FK_" + tableName + "_" + fieldName + 
         ") FOREIGN KEY (" + getQuotedName(fieldName) + ") REFERENCES " + 
         getQuotedName(targetTableName) + "(" + getQuotedName(targetTableFieldName) + ")");
     // Not currently implemented by Oracle, 
     // another reason for not using the DB to control these things
     //if (fixName.equals("prevent"))
     //  sb.append(" ON DELETE NO ACTION");
     if (fixName.equals("delete"))
       sb.append(" ON DELETE CASCADE");      
     if (fixName.equals("clear"))
       sb.append(" ON DELETE SET NULL");      
     return sb.toString();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getPrimaryKeyDefinition(java.lang.String)
    */
   public String getPrimaryKeyDefinition(String fieldName) {
     StringBuffer sb = new StringBuffer();
     sb.append(" ADD (CONSTRAINT PK_" + fieldName + 
        " PRIMARY KEY(" + getQuotedName(fieldName) + "))");
     return sb.toString();
   }
 
 }
