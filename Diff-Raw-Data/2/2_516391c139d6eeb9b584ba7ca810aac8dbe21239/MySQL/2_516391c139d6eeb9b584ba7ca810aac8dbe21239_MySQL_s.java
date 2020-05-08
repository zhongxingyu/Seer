 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2002 Peter Kehl
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
  *
  */
 
 
 package org.melati.poem.dbms;
 
 import java.util.Enumeration;
 import java.sql.SQLException;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import org.melati.poem.SizedAtomPoemType;
 import org.melati.poem.Table;
 import org.melati.poem.Column;
 import org.melati.poem.PoemType;
 import org.melati.poem.SQLPoemType;
 import org.melati.poem.IntegerPoemType;
 import org.melati.poem.BinaryPoemType;
 import org.melati.poem.BooleanPoemType;
 import org.melati.poem.StringPoemType;
 import org.melati.poem.ParsingPoemException;
 import org.melati.poem.SQLPoemException;
 import org.melati.poem.util.StringUtils;
 
  /**
   * A Driver for MySQL.
   * See http://www.mysql.com.
   *
   * <b>Notes</b>
   * <ol>
   * <li>
   *  Use JDBC URL of type jdbc:mysql://[host][:port]/dbname[?param=value[...]]
   *  ie. the simpliest one has 3 slashes: jdbc:mysql:///melatitest
   * </li>
   * <li>
   *  Don't use asterix * for password, leave it empty (end of line), as:
   * <pre>
   *  org.Melati.LogicalDatabase.melatitest.pass=
   * </pre>
   * 
   * or use explicit username and password and 
   * <pre>
   *   GRANT ALL PRIVILEGES ON dbname
   *  TO username@localhost IDENTIFIED BY 'password';
   * </pre> 
   * </li>
   * <li>
   * If you want to use double quotes to delimit table and column names then 
   * start MySQL in ANSI mode and modify getQuotedName(String name).
   * </li>
   * <li>   
   * Start MySQL with transactioned tables as default. InnoDB is stable,
   *  BDB nearly stable.
   *  <code>getConnection</code> now returns a <code>Connection</code> 
   *  with <code>autocommit</code> turned off through JDBC.
   * 
   *  BDB tables of MySQL-Max 3.23.49 don't support full transactions
   *  - they lock whole table instead, until commit/rollback is called.
   *  According to MySQL 4.0.2-alpha doc, interface between MySQL and
   *  BDB tables is still improved.
   *
   *  As I tested MySQL-Max 3.23.49, InnoDB has correct transactions,
   *  however database size must be specified & reserved in advance
   *  in one file, that is share by all InnoDB tables.
   *  Set in /etc/my.cnf by line like:
   *     innodb_data_file_path=ibdata1:30M
   * <pre>
   *  run
   *  safe_mysqld --user=mysql --ansi --default-table-type=InnoDB
   * </pre>
   * After it created and initialised dB file /var/lib/mysql/ibdata1
   * of 30MB, it creates 2 own log files  /var/lib/mysql/ib_logfile0
   * and ib_logfile1, both of size 5MB.
   * <br/>
   * The table type is currently hardcoded in <tt>getCreateTableOptions</tt>.
   *
   * </li>
   * <li>
   *  <tt>boolean</tt> type works (both applications melatitest and contacts).
   *  Because MySQL returns metainfo about BOOL as TINYINT.
   * </li>
   * </ol>
   */
 public class MySQL extends AnsiStandard {
 
   /** Size of indexes. */
   public static final int indexSize = 30;
   /** Size of MySQL text fields. */
   public static final int mysqlTextSize = 65535;
 
   /** Constructor - sets driver. */
   public MySQL() {
     setDriverClassName("org.gjt.mm.mysql.Driver");
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#createTableOptionsSql()
    */
   public String createTableOptionsSql() {
     return " TYPE='InnoDB' ";
   }
 
 /**
   * Retrieve an SQL type keyword used by the DBMS 
   * for the given Melati type name.
   *
   * @param sqlTypeName the Melati internal type name
   * @return this dbms specific type keyword
   */
   public String getSqlDefinition(String sqlTypeName) {
     if(sqlTypeName.equals("BOOLEAN")) return "BOOL"; 
     return super.getSqlDefinition(sqlTypeName);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getStringSqlDefinition(int)
    */
   public String getStringSqlDefinition(int size) throws SQLException {
     if (size < 0) { 
       return "TEXT";
     }
     return super.getStringSqlDefinition(size); //VARCHAR(size) is OK
   }
 
   /**
    * Ignores size.
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getBinarySqlDefinition(int)
    */
   public String getBinarySqlDefinition(int size) {
     return "BLOB"; 
   }
 
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#getQuotedName(java.lang.String)
    */
   public String getQuotedName(String name) {
     return unreservedName(name);
   }
 
  /**
   * Translates a MySQL String into a Poem <code>StringPoemType</code>.
   */ 
   public static class MySQLStringPoemType extends StringPoemType {
 
     /**
      * Constructor.
      * @param nullable whether nullable
      * @param size size
      */
     public MySQLStringPoemType(boolean nullable, int size) {
       super(nullable, size);
     }
 
     //_assertValidRow(Object) is defined OK in StringPoemType
 
     //MySQL returns metadata info size 65535 for TEXT type
     protected boolean _canRepresent(SQLPoemType other) {
       return
              sqlTypeCode() == other.sqlTypeCode() &&
              other instanceof StringPoemType &&
              (getSize()<0 || getSize()==mysqlTextSize ||
              getSize()>=((StringPoemType)other).getSize());
     }
 
     /**
      * Looks like this is unnecessary.
      * {@inheritDoc}
      * @see org.melati.poem.BasePoemType#canRepresent(PoemType)
      */
     /*
     public PoemType canRepresent(PoemType other) {
       return other instanceof StringPoemType &&
              _canRepresent((StringPoemType)other) &&
              !(!getNullable() && ((StringPoemType)other).getNullable()) ?
                other : null;
     }
      */
     /**
      * {@inheritDoc}
      * @see org.melati.poem.SizedAtomPoemType#withSize(int)
      */
     public SizedAtomPoemType withSize(int newSize) {
       if (newSize==mysqlTextSize)
         return super.withSize(-1);
       return super.withSize(newSize);
     }
   }
 
  /**
   * Translates a MySQL Boolean into a Poem <code>BooleanType</code>.
   */ 
   public static class MySQLBooleanPoemType extends BooleanPoemType {
     /**
      * Constructor.
      * @param nullable whether nullable
      */
     public MySQLBooleanPoemType(boolean nullable) {
       super(nullable);
     }
 
     protected Object _getRaw(ResultSet rs, int col) throws SQLException {
       synchronized (rs) {
         int i = rs.getInt(col);
           return rs.wasNull() ? null :
             (i==1 ? Boolean.TRUE : Boolean.FALSE);
       }
     }
 /*
     protected Object _getRaw(ResultSet rs, int col) throws SQLException {
       synchronized (rs) {
         String v = rs.getString(col);
         return rs.wasNull() ? null :
          (v.equals("t") ? Boolean.TRUE : Boolean.FALSE);
       }
     }
 */
 
     protected void _setRaw(PreparedStatement ps, int col, Object bool)
         throws SQLException {
       ps.setInt(col, ((Boolean)bool).booleanValue() ? 1 : 0);
     }
     /*
     protected void _setRaw(PreparedStatement ps, int col, Object bool)
         throws SQLException {
       if (bool instanceof Boolean && bool == Boolean.TRUE) 
         ps.setString(col, "t");
       else
         ps.setString(col, "f");
     }
     */ 
         
     /**
      * We could use original method from BooleanPoemType,
      * it too recognizes 0/1.
      * {@inheritDoc}
      * @see org.melati.poem.BooleanPoemType#_rawOfString(java.lang.String)
      */
     protected Object _rawOfString(String rawString)
         throws ParsingPoemException {
       rawString = rawString.trim();
       switch (rawString.charAt(0)) {
         case '1': return Boolean.TRUE;
         case '0': return Boolean.FALSE;
         default: throw new ParsingPoemException(this, rawString);
       }
     }
   }
 
  /**
   * Translates a MySQL Blob into a Poem <code>IntegerPoemType</code>.
   */ 
   public static class BlobPoemType extends BinaryPoemType {
  
    /**
     * Constructor.
     * @param nullable whether nullable
     * @param size size
     */
     public BlobPoemType(boolean nullable, int size) {
       super(nullable, size);
     }
 
     protected boolean _canRepresent(SQLPoemType other) {
       return other instanceof BinaryPoemType;
     }
 
    /**
     * {@inheritDoc}
     * @see org.melati.poem.BasePoemType#canRepresent(PoemType)
     */
     public PoemType<?> canRepresent(PoemType other) {
       return other instanceof BinaryPoemType &&
           !(!getNullable() && ((BinaryPoemType)other).getNullable()) ?
                        other : null;
     }
   }
 
  /**
   * {@inheritDoc}
   * @see org.melati.poem.dbms.AnsiStandard#canRepresent(org.melati.poem.PoemType, org.melati.poem.PoemType)
   */
   public PoemType<?> canRepresent(PoemType<?> storage, PoemType<?> type) {
     if (storage instanceof IntegerPoemType &&
         type instanceof BooleanPoemType
         && !(!storage.getNullable() && type.getNullable())  // Nullable may represent not nullable
     ) {
       return type;
     } else {
       return storage.canRepresent(type);
     }
   }
 
  /**
   * {@inheritDoc}
   * @see org.melati.poem.dbms.AnsiStandard#
   *          defaultPoemTypeOfColumnMetaData(java.sql.ResultSet)
   */
   public SQLPoemType defaultPoemTypeOfColumnMetaData(ResultSet md)
       throws SQLException {
     boolean nullable = md.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
     String typeName = md.getString("TYPE_NAME");
     if(typeName.equals("blob"))
       return new BlobPoemType(nullable, md.getInt("COLUMN_SIZE"));
     else if(typeName.equals("text"))
       return new MySQLStringPoemType(nullable, md.getInt("COLUMN_SIZE"));
     else if(typeName.equals("smallint"))
       return new IntegerPoemType(nullable);
     else if(typeName.equals("set"))
      return new IntegerPoemType(nullable);
     
     else if(typeName.equals("char"))
       return new StringPoemType(nullable, md.getInt("COLUMN_SIZE"));
     // MySQL:BOOL --> MySQL:TINYINT --> Melati:boolean backward mapping
     else if(typeName.equals("tinyint"))
       return new MySQLBooleanPoemType(nullable);
     else
       return super.defaultPoemTypeOfColumnMetaData(md);
   }
 
 
  /**
   * {@inheritDoc}
   * @see org.melati.poem.dbms.AnsiStandard#exceptionForUpdate
   */
   public SQLPoemException exceptionForUpdate(
         Table table, String sql, boolean insert, SQLException e) {
 
     String m = e.getMessage();
 
     // MySQL's duplicate key (or any unique field) message is:
     // "ERROR 1062: Duplicate entry '106' for key 1"
     //  Duplicate index value      <--|           |
     //                                            |
     //  Which 'index' (unique field) it is, in <--|
     //  order as table was defined, starting from 1.
 
     if (m != null &&
         m.indexOf("1062") >= 0) {
 
   // It's not simple as in Postgres. This duplicated 'index' is one
   // of possibly more unique columns. That involves searching for its
   // column. For error "Duplicate entry '106' for key 4"
   // we search 4th unique field = we loop over columns, skip first 3 that
   // are unique and return 4th unique.
 
       try { //Try parsing error message.
 
         int preIndex, postIndex; //Places of apostrophes around index value
         int preColumn; //Place of "key ", which is in front of column number
     
         preIndex= m.indexOf('\'');
         postIndex= m.lastIndexOf('\'');
         preColumn= m.indexOf("key ");
   
         String indexValue= m.substring(preIndex+1, postIndex);
         String indexColumn= m.substring(preColumn+4);
 
         System.err.println("Duplicated value " + indexValue +
               " of " + indexColumn + "th unique field."); 
   
         int indexNum= Integer.parseInt(indexColumn);
         Column column= table.troidColumn(); //Just to satisfy compiler.
          //At the end, it will (should) be our column anyway.
       
         for(Enumeration<Column> columns = table.columns(); columns.hasMoreElements();) {
           column= columns.nextElement();
           if(column.getUnique() && (--indexNum == 0))
             break; //We found it!
         }
         //Now, it's found & indexNum==0.
         if(indexNum==0)
           return new DuplicateKeySQLPoemException(column, sql, insert, e);
       } catch(NumberFormatException f) {
           throw new RuntimeException(
               "Number format exception parsing dbms error.");  
       }
       return new DuplicateKeySQLPoemException(table, sql, insert, e);
     }
     return super.exceptionForUpdate(table, sql, insert, e);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#unreservedName(java.lang.String)
    */
   public String unreservedName(String name) {
     if(name.equalsIgnoreCase("group")) name = "poem_" + name;
     if(name.equalsIgnoreCase("precision")) name = "poem_" + name;
     if(name.equalsIgnoreCase("unique")) name = "poem_" + name;
     return name;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#melatiName(java.lang.String)
    */
   public String melatiName(String name) {
     if (name == null) return name;
     if(name.equalsIgnoreCase("poem_group")) name = "group";
     if(name.equalsIgnoreCase("poem_precision")) name = "precision";
     if(name.equalsIgnoreCase("poem_unique")) name = "unique";
     return name;
   }
 
  /**
   * MySQL requires TEXT and BLOB field indices to have an 
   * explicit length, 30 should be fine.
   *
   * @return a snippet of sql to insert into an SQL statement.
   */
   public String getIndexLength(Column column) {
     PoemType<?> t = column.getType();
     if (t instanceof StringPoemType && 
         ((StringPoemType)t).getSize() < 0) return "(" + indexSize + ")";
     if (t instanceof BinaryPoemType) return "(" + indexSize + ")";
     return "";
   }  
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#givesCapabilitySQL
    */
   public String givesCapabilitySQL(Integer userTroid, String capabilityExpr) {
     return
         "SELECT groupmembership.* " + 
         "FROM groupmembership LEFT JOIN groupcapability " +
         "ON groupmembership." + getQuotedName("group") +
         " =  groupcapability." + getQuotedName("group") + " " +
         "WHERE " + getQuotedName("user") + " = " + userTroid + " " +
         "AND groupcapability." + getQuotedName("group") + " IS NOT NULL " +
         "AND capability = " + capabilityExpr;
   }
 
 
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.AnsiStandard#
    * caseInsensitiveRegExpSQL(java.lang.String, java.lang.String)
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
    * @see org.melati.poem.dbms.AnsiStandard#
    * alterColumnNotNullableSQL(java.lang.String, org.melati.poem.Column)
    */
   public String alterColumnNotNullableSQL(String tableName, Column column) {
     return "ALTER TABLE " + getQuotedName(tableName) +
     " CHANGE " + getQuotedName(column.getName()) + " " + getQuotedName(column.getName()) +
     " " + 
     column.getSQLType().sqlDefinition(this);
   }
   
   
 }
