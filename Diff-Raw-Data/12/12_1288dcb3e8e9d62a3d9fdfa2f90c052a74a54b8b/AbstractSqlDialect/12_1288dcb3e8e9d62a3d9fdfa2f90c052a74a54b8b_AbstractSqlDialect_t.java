 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.benchmark.db;
 
 /**
  * Superclass for SqlDialect implementations that takes care of most select
  * 
  * @author rhodges
  */
 public abstract class AbstractSqlDialect implements SqlDialect
 {
   /** Every implememtation must implement at least this method. */
   public abstract boolean supportsJdbcUrl(String url);
 
   /**
    * Implements a generic create table that should work for all DBMS.
    */
   public String getCreateTable(Table t)
   {
     // Header
     StringBuffer sb = new StringBuffer();
     sb.append("create table ");
     sb.append(t.getName());
     sb.append(" (");
 
     // Column specifications.
     Column[] cols = t.getColumns();
     for (int i = 0; i < cols.length; i++)
     {
       // Add intervening comma for 2nd and greater col. definitions.
       if (i > 0)
         sb.append(", ");
 
       Column col = cols[i];
       sb.append(implementationColumnSpecification(col));
     }
 
     sb.append(")");
     String sql = sb.toString();
     return sql;
   }
 
   /**
    * Implements a generic create index that should work for all DBMS.
    */
   public String getCreateIndex(Table t, Column c)
   {
     StringBuffer sb = new StringBuffer();
     sb.append("create index ");
     sb.append(t.getName());
     sb.append("_");
     sb.append(c.getName());
     sb.append("_idx on ");
     sb.append(t.getName());
     sb.append(" (");
     sb.append(c.getName());
     sb.append(")");
 
     String sql = sb.toString();
     return sql;
   }
 
   /**
    * Loads a generic column type specification that works for databases other
    * than PostgreSQL, which uses a type to handle autoincrement columns.
    */
   public String implementationColumnSpecification(Column col)
   {
     StringBuffer sb = new StringBuffer();
 
     // Name and type definition
     sb.append(col.getName());
     sb.append(" ");
     sb.append(this.implementationTypeName(col.getType()));
 
     // Length and precision if required for this type.
     if (implementationTypeNeedsLength(col.getType()))
     {
       sb.append("(").append(col.getLength());
       if (implementationTypeNeedsPrecision(col.getType())
           && col.getPrecision() > 0)
       {
         sb.append(",").append(col.getPrecision());
       }
       sb.append(")");
     }
 
     // Auto-increment indicator.
     if (col.isAutoIncrement())
     {
       sb.append(" ").append(implementationAutoIncrementKeyword());
     }
 
     // Primary key indicator.
     if (col.isPrimaryKey())
     {
       sb.append(" primary key");
     }
 
     return sb.toString();
   }
 
   /** Provides a generic table DELETE implementation that works for all DBMS. */
   public String getDeleteAll(Table t)
   {
     return "delete from " + t.getName();
   }
 
   /** Provides a generic implementation that works for all DBMS. */
   public String getDeleteByKey(Table t)
   {
     Column pkeyCol = t.getPrimaryKey();
     if (pkeyCol == null)
       throw new RuntimeException("No primary key found on table definition: "
           + t.toString());
 
     return "delete from " + t.getName() + " where " + pkeyCol.getName()
         + " = ?";
   }
 
   /**
    * Like getDeleteByKey(Table t) but allows defining which column is used as
    * key
    */
   public String getDeleteByKey(Table t, Column keyColumn)
   {
     return "delete from " + t.getName() + " where " + keyColumn.getName()
         + " = ?";
   }
 
   /**
    * Like getDeleteByKey(Table t) but allows defining which column is used as
    * key
    */
   public String getDeleteByKeyLike(Table t, Column keyColumn)
   {
     String sql = "delete from " + t.getName() + " where " + keyColumn.getName()
         + " LIKE ?";
     return sql;
   }
 
   /** Provides a generic DROP TABLE that works for all DBMS. */
   public String getDropTable(Table t)
   {
     return "drop table " + t.getName();
   }
 
   /**
    * Provides a generic INSERT that works for and DBMS that supports
    * autoincrement columns.
    */
   public String getInsert(Table t)
   {
     // Start of INSERT command.
     StringBuffer sb = new StringBuffer();
     sb.append("insert into ");
     sb.append(t.getName());
 
     // Column list.
     sb.append(" (");
     Column[] columns = t.getColumns();
     int nonKeyCount = 0;
     for (int i = 0; i < columns.length; i++)
     {
       if (!columns[i].isAutoIncrement())
       {
         if (nonKeyCount++ > 0)
           sb.append(", ");
         sb.append(columns[i].getName());
       }
     }
     sb.append(")");
 
     // Parameter list.
     sb.append(" values(");
     for (int i = 0; i < nonKeyCount; i++)
     {
       if (i > 0)
         sb.append(", ");
       sb.append("?");
     }
     sb.append(")");
 
     return sb.toString();
   }
 
   /** Returns an UPDATE statement that updates a single record by key value. */
   public String getUpdateByKey(Table t)
   {
     return getUpdateByKey(t, t.getPrimaryKey());
   }
 
   /**
    * Like getUpdateByKey(Table t) but allows defining which column is used as
    * key
    */
   public String getUpdateByKey(Table t, Column keyColumn)
   {
     // Start of UPDATE command.
     StringBuffer sb = new StringBuffer();
     sb.append("update ");
     sb.append(t.getName());
 
     // Column list.
     Column[] columns = t.getColumns();
     int nonKeyCount = 0;
     for (int i = 0; i < columns.length; i++)
     {
       if (!columns[i].isPrimaryKey())
       {
         if (nonKeyCount++ > 0)
           sb.append(",");
         else
           sb.append(" set ");
 
         sb.append(columns[i].getName());
         sb.append(" = ?");
       }
     }
 
     // Where clause.
     sb.append(" where ");
     sb.append(keyColumn.getName());
     sb.append(" = ?");
 
     return sb.toString();
   }
   /**
    * Like getUpdateByKey(Table t, Column keyColumn) but uses like instead of =
    */
   public String getUpdateByKeyLike(Table t, Column keyColumn){
     String sql= getUpdateByKey(t, keyColumn);
     return sql.substring(0, sql.length()-3) +" LIKE ?";
   }
 
   /** Returns a generic SELECT for all columns and rows. */
   public String getSelectAll(Table t)
   {
     return "select * from " + t.getName();
   }
 
   /** Returns a generic SELECT for all columns and rows in a sorted, deterministic way. */
   public String getSelectAllSorted(Table t)
   {
     Column cols[] = t.getColumns();
     int ncols = cols.length;
     int i = 0;
     int usedColumns = 0;
    String SQL = "select * from " + t.getName();
     
    String orderBy = "";
     for (i = 0; i < ncols; i++)
     {
         Column col = cols[i];
         /* do not sort blobs or clobs */
         if (col.getType() == java.sql.Types.BLOB ||
             col.getType() == java.sql.Types.CLOB) continue;
         usedColumns++;
        if (usedColumns > 1) orderBy += ", ";
        orderBy += (i + 1);
     }
    return SQL + (orderBy.length() > 0? " order by " + orderBy:"");
   }
 
   /**
    * Return all rows of the cross product select of the table on itself. This
    * scans and returns N x N rows where N is the table row count.
    */
   public String getSelectCrossProduct(Table table)
   {
     StringBuffer sb = new StringBuffer();
 
     // Generate the join SQL statement.
     sb.append("select * from ");
     sb.append(table.getName());
     sb.append(" t0, ");
     sb.append(table.getName());
     sb.append(" t1");
 
     String sql = sb.toString();
     return sql;
   }
 
   /**
    * Return count of rows of the cross product select of the table on itself.
    * This scans N x N rows where N is the table row count and returns a single
    * row.
    */
   public String getSelectCrossProductCount(Table table)
   {
     StringBuffer sb = new StringBuffer();
 
     // Generate the join SQL statement.
     sb.append("select count(*) from ");
     sb.append(table.getName());
     sb.append(" t0, ");
     sb.append(table.getName());
     sb.append(" t1");
 
     String sql = sb.toString();
     return sql;
   }
 
   /**
    * Returns a SELECT statement to fetch a query that performs an aggregate over
    * a set of of two more more tables joined in a cross product and high and low
    * key values, which must be supplied as prepared statement parameters.
    */
   public String getSelectCrossProductCount(Table[] tables)
   {
     StringBuffer sb = new StringBuffer();
 
     // Generate the join SQL statement.
     sb.append("select count(*) from ").append(tables[0].getName()).append(
         " t0 ");
     for (int i = 1; i < tables.length; i++)
     {
       sb.append(" join ");
       sb.append(tables[i].getName());
       sb.append(" t").append(i);
       sb.append(" on t0.").append(tables[0].getPrimaryKey().getName());
       sb.append(" = t").append(i).append(".").append(
           tables[i].getPrimaryKey().getName());
     }
 
     // Select key values from the first table.
     sb.append(" where t0.").append(tables[0].getPrimaryKey().getName());
     sb.append(" between ? and ?");
 
     String sql = sb.toString();
     return sql;
   }
 
   /**
    * Provides a generic SELECT that selects all columns where the indicated
    * columns matches.
    */
   public String getSelectByColumn(Table t, Column c)
   {
     return "select * from " + t.getName() + " where " + c.getName() + " = ?";
   }
 
   /**
    * Provides a generic select that works for all DBMS that support limit
    * clauses.
    */
   public String getSelectByColumnWithLimit(Table t, Column c, int limit)
   {
     String base = getSelectByColumn(t, c);
     if (this.implementationSupportsLimitClause())
       return base + " limit " + limit;
     else
       return base;
   }
 
   /** Provides a generic SELECT that works for any table with one PKEY column. */
   public String getSelectByKey(Table t)
   {
     Column pkeyCol = t.getPrimaryKey();
     return getSelectByColumn(t, pkeyCol);
   }
 
   /** Provides general default for most database. */
   public boolean implementationSupportsLimitClause()
   {
     return true;
   }
 
   /**
    * Provides general default that works for databases that don't do wierd BLOB
    * or other datatype handling.
    */
   public boolean implementationUpdateRequiresTransaction(int type)
   {
     return false;
   }
 
   /** Returns the generic autoincrement keyword. */
   public String implementationAutoIncrementKeyword()
   {
     return "auto_increment";
   }
 
   /** Translates JDBC types to standard SQL type names. */
   public String implementationTypeName(int type)
   {
     switch (type)
     {
       case java.sql.Types.BIT :
         return "bit";
       case java.sql.Types.BLOB :
         return "blob";
       case java.sql.Types.BOOLEAN :
         return "boolean";
       case java.sql.Types.CHAR :
         return "char";
       case java.sql.Types.CLOB :
         return "clob";
       case java.sql.Types.DATE :
         return "date";
       case java.sql.Types.DECIMAL :
         return "decimal";
       case java.sql.Types.DOUBLE :
         return "double";
       case java.sql.Types.FLOAT :
         return "float";
       case java.sql.Types.INTEGER :
         return "integer";
       case java.sql.Types.SMALLINT :
         return "smallint";
       case java.sql.Types.TIME :
         return "time";
       case java.sql.Types.TIMESTAMP :
         return "timestamp";
       case java.sql.Types.VARCHAR :
         return "varchar";
       case AdditionalTypes.TIMESTAMPLOCAL:
         return "timestamp with local time zone";
       default :
         throw new IllegalArgumentException("Unsupported JDBC type value: "
             + type);
     }
   }
 
   /**
    * Returns true if this type requires a length specification.
    */
   public boolean implementationTypeNeedsLength(int type)
   {
     switch (type)
     {
       case java.sql.Types.BLOB :
       case java.sql.Types.BOOLEAN :
       case java.sql.Types.CLOB :
       case java.sql.Types.DATE :
       case java.sql.Types.DOUBLE :
       case java.sql.Types.FLOAT :
       case java.sql.Types.INTEGER :
       case java.sql.Types.SMALLINT :
       case java.sql.Types.TIME :
       case java.sql.Types.TIMESTAMP :
       case AdditionalTypes.TIMESTAMPLOCAL :
         return false;
         
       case java.sql.Types.BIT :
       case java.sql.Types.CHAR :
       case java.sql.Types.DECIMAL :
       case java.sql.Types.VARCHAR :
         return true;
 
       default :
         throw new IllegalArgumentException("Unsupported JDBC type value: "
             + type);
     }
   }
 
   /**
    * Returns true if this type requires a precision specification.
    */
   public boolean implementationTypeNeedsPrecision(int type)
   {
     switch (type)
     {
       case java.sql.Types.BIT :
       case java.sql.Types.BLOB :
       case java.sql.Types.BOOLEAN :
       case java.sql.Types.CHAR :
       case java.sql.Types.CLOB :
       case java.sql.Types.DATE :
       case java.sql.Types.DOUBLE :
       case java.sql.Types.FLOAT :
       case java.sql.Types.INTEGER :
       case java.sql.Types.SMALLINT :
       case java.sql.Types.TIME :
       case java.sql.Types.TIMESTAMP :
       case java.sql.Types.VARCHAR :
       case AdditionalTypes.TIMESTAMPLOCAL :
         return false;
 
       case java.sql.Types.DECIMAL :
         return true;
 
       default :
         throw new IllegalArgumentException("Unsupported JDBC type value: "
             + type);
     }
   }
 
   /**
    * Default implementation hands back fetch size as is.
    */
   public int implementationConvertFetchSize(int fetchSize)
   {
     if (fetchSize < 0)
       return 0;
     else
       return fetchSize;
   }
 }
