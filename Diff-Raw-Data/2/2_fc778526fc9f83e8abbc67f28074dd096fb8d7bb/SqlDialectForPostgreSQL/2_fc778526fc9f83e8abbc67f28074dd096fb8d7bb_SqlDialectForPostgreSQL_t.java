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
  * PostgreSQL DBMS dialect information. 
  * 
  * @author rhodges
  */
 public class SqlDialectForPostgreSQL extends AbstractSqlDialect
 {
   /** Return the PostgreSQL driver. */
   public String getDriver()
   {
     return "org.postgresql.Driver";
   }
 
   /** Returns true if the JDBC URL looks like a PostgreSQL URL. */
   public boolean supportsJdbcUrl(String url)
   {
     return url.startsWith("jdbc:postgresql");
   }
   
   /** 
    * Postgres uses a "serial" datatype for autoincrement fields, hence we must
    * generate specifications for columns differently from the default. 
    */
   public String implementationColumnSpecification(Column col)
   {
     StringBuffer sb = new StringBuffer(); 
     
     // Name and type definition
     sb.append(col.getName());
     sb.append(" ");
     
     if (col.isAutoIncrement())
       sb.append("serial");
     else
       sb.append(this.implementationTypeName(col.getType()));
     
     // Length and precision if specified. 
     if (col.getLength() > 0)
     {
       sb.append("(").append(col.getLength());
       if (col.getPrecision() > 0)
       {
         sb.append(",").append(col.getPrecision());
       }
       sb.append(")"); 
     }
     
     // Primary key indicator. 
     if (col.isPrimaryKey())
     {
       sb.append(" primary key");
     }
     
     return sb.toString();
   }
   
   /** 
    * PostgreSQL uses "serial" datatype for autoincrement but does not have a
    * keyword 
    */
   public String implementationAutoIncrementKeyword()
   {
     // This should not be called or we will generate bad syntax. 
     throw new Error("Bug: PostgreSQL dialect should not use autoincrement keyword");
   }
   
   /** 
    * PostgreSQL requires a transaction to update blob and text types. 
    */
   public boolean implementationUpdateRequiresTransaction(int type)
   {
     switch (type)
     {
       case java.sql.Types.BLOB:
       case java.sql.Types.CLOB:
         return true;
       default:
         return false;
     }
   }
 
   /** Add support for specialized PostgreSQL BLOB/CLOB names. */
   public String implementationTypeName(int type)
   {
     switch (type)
     {
       case java.sql.Types.BLOB: 
         return "bytea";
       case java.sql.Types.CLOB:
         return "text";
      case java.sql.Types.BIGINT :
          return "bigint";
       default: 
         return super.implementationTypeName(type);
     }
   }
   
   public String implementationSpecificSuffix(Column c)
   {    
       return super.implementationSpecifcSuffix(c);     
   }
 
 }
