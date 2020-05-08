 /*
  * Copyright (C) 2011 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.database.utils;
 
 import org.exoplatform.commons.utils.SecurityHelper;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 
 import java.security.PrivilegedExceptionAction;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.sql.DataSource;
 
 /**
  * This class provides JDBC tools
  * 
  * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
  * @version $Id$
  */
 /**
  * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
  * @version $Id: JDBCUtils.java 34360 2009-07-22 23:58:59Z tolusha $
  *
  */
 public class JDBCUtils
 {
    private static final Log LOG = ExoLogger.getLogger("exo.core.component.database.JDBCUtils");
 
    /**
     * Default SQL delimiter.
     */
    public static final String SQL_DELIMITER = ";";
 
    /**
     * SQL delimiter comment prefix.
     */
    public static final String SQL_DELIMITER_COMMENT_PREFIX = "/*$DELIMITER:";
 
    public static final String SQL_DELIMITER_COMMENT_SUFFIX = "*/";
 
    private JDBCUtils()
    {
    }
 
    /**
     * Indicates whether or not a given table exists
     * 
     * @param tableName 
     *          the name of the table to check
     * @param con 
     *          the connection to use
     * @return <code>true</code> if it exists, <code>false</code> otherwise
     */
    public static boolean tableExists(String tableName, Connection con)
    {
       Statement stmt = null;
       ResultSet trs = null;
       try
       {
          String dialect = DialectDetecter.detect(con.getMetaData());
          String query;
          if (dialect.startsWith(DialectConstants.DB_DIALECT_MYSQL) || dialect.startsWith(DialectConstants.DB_DIALECT_PGSQL))
          {
             query = "SELECT count(*) from (SELECT 1 FROM " + tableName + " LIMIT 1) T";
          }
          else if (dialect.startsWith(DialectConstants.DB_DIALECT_ORACLE))
          {
             query = "SELECT count(*) from (SELECT 1 FROM " + tableName + " WHERE ROWNUM = 1) T";
          }
          else if (dialect.startsWith(DialectConstants.DB_DIALECT_DB2) || dialect.startsWith(DialectConstants.DB_DIALECT_DERBY)
             || dialect.startsWith(DialectConstants.DB_DIALECT_INGRES))
          {
             query = "SELECT count(*) from (SELECT 1 FROM " + tableName + " FETCH FIRST 1 ROWS ONLY) T";
          }
          else if (dialect.startsWith(DialectConstants.DB_DIALECT_MSSQL))
          {
            query = "SELECT count(*) from (SELECT TOP (1) 1 FROM " + tableName + ") T";
          }
          else if (dialect.startsWith(DialectConstants.DB_DIALECT_SYBASE))
          {
             query = "SELECT count(*) from (SELECT TOP 1 1 FROM " + tableName + ") T";
          }
          else
          {
             query = "SELECT count(*) FROM " + tableName;
          }
          stmt = con.createStatement();
          trs = stmt.executeQuery(query);
          return trs.next();
       }
       catch (SQLException e)
       {
          if (LOG.isDebugEnabled())
          {
             LOG.debug("SQLException occurs while checking the table " + tableName, e);
          }
          return false;
       }
       finally
       {
          freeResources(trs, stmt, null);
       }
    }
 
    /**
     * Retrieves the full message from SQLException. 
     * 
     * @param exception
     *          SQLException which will be parsed
     */
    public static String getFullMessage(SQLException exception)
    {
       StringBuilder errorTrace = new StringBuilder(exception.getMessage());
 
       SQLException next = exception;
       while (next != null)
       {
          errorTrace.append("; ");
          errorTrace.append(next.getMessage());
 
          next = next.getNextException();
       }
 
       Throwable cause = exception.getCause();
 
       return errorTrace + (cause != null ? " (Cause: " + cause.getMessage() + ")" : "");
    }
 
    /**
     * Replace whitespace characters with space character.
     */
    public static String cleanWhitespaces(String string)
    {
       if (string != null)
       {
          char[] cc = string.toCharArray();
          for (int ci = cc.length - 1; ci > 0; ci--)
          {
             if (Character.isWhitespace(cc[ci]))
             {
                cc[ci] = ' ';
             }
          }
          return new String(cc);
       }
       return string;
    }
 
    /**
     * Split string resource with SQL Delimiter. Delimiter can be taken from resource
     * at the begining of the first line. It surrounded with {@link #SQL_DELIMITER_COMMENT_PREFIX}
     * and {@link #SQL_DELIMITER_COMMENT_SUFFIX}. Otherwise the default delimiter will 
     * be used {@link #SQL_DELIMITER}.
     */
    public static String[] splitWithSQLDelimiter(String resource)
    {
       if (resource.startsWith(SQL_DELIMITER_COMMENT_PREFIX))
       {
          try
          {
             String scripts = resource.substring(SQL_DELIMITER_COMMENT_PREFIX.length());
 
             int endOfDelimIndex = scripts.indexOf(SQL_DELIMITER_COMMENT_SUFFIX);
             String delim = scripts.substring(0, endOfDelimIndex).trim();
 
             scripts = scripts.substring(endOfDelimIndex + 2).trim();
             return scripts.split(delim);
          }
          catch (IndexOutOfBoundsException e)
          {
             LOG.warn("Error of parse SQL-script file. Invalid DELIMITER configuration. Valid format is '"
                + SQL_DELIMITER_COMMENT_PREFIX + "XXX*/' at begin of the SQL-script file, where XXX - DELIMITER string."
                + " Spaces will be trimed. ", e);
             LOG.info("Using DELIMITER:[" + SQL_DELIMITER + "]");
 
             return resource.split(SQL_DELIMITER);
          }
       }
       else
       {
          return resource.split(SQL_DELIMITER);
       }
    }
 
    /**
     * Returns appropriate blob type field for specific database. 
     */
    public static String getAppropriateBlobType(DataSource dataSource) throws SQLException
    {
       String dialect = resolveDialect(dataSource);
 
       if (dialect.startsWith(DialectConstants.DB_DIALECT_HSQLDB))
       {
          return "VARBINARY(65535)";
       }
       else if (dialect.startsWith(DialectConstants.DB_DIALECT_MYSQL))
       {
          return "LONGBLOB";
       }
       else if (dialect.startsWith(DialectConstants.DB_DIALECT_PGSQL))
       {
          return "bytea";
       }
       else if (dialect.startsWith(DialectConstants.DB_DIALECT_MSSQL))
       {
          return "VARBINARY(MAX)";
       }
       else if (dialect.startsWith(DialectConstants.DB_DIALECT_SYBASE))
       {
          return "IMAGE";
       }
       else if (dialect.startsWith(DialectConstants.DB_DIALECT_INGRES))
       {
          return "long byte";
       }
 
       return "BLOB";
    }
 
    /**
     * Returns appropriate timestamp type field for specific database. 
     */
    public static String getAppropriateTimestamp(DataSource dataSource) throws SQLException
    {
       String dialect = resolveDialect(dataSource);
 
       if (dialect.startsWith(DialectConstants.DB_DIALECT_ORACLE))
       {
          return "NUMBER(19, 0)";
       }
       
       return "BIGINT";
    }
 
    /**
     * Returns appropriate char type field for specific database. 
     */
    public static String getAppropriateCharType(DataSource dataSource) throws SQLException
    {
       String dialect = resolveDialect(dataSource);
 
       if (dialect.startsWith(DialectConstants.DB_DIALECT_ORACLE))
       {
          // Oracle suggests the use VARCHAR2 instead of VARCHAR while declaring data type.
          return "VARCHAR2(512)";
       }
 
       return "VARCHAR(512)";
    }
 
    /**
     * Returns dialect one of dialect {@link DialectConstants} based on {@link DataSource} name.  
     */
    public static String resolveDialect(final DataSource dataSource) throws SQLException
    {
       Connection jdbcConn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
       {
          public Connection run() throws Exception
          {
             return dataSource.getConnection();
          }
       });
 
       try
       {
          return DialectDetecter.detect(jdbcConn.getMetaData());
       }
       finally
       {
          freeResources(null, null, jdbcConn);
       }
    }
 
    /**
     * Closes database related resources.
     */
    public static void freeResources(ResultSet resultSet, Statement statement, Connection conn)
    {
       if (resultSet != null)
       {
          try
          {
             resultSet.close();
          }
          catch (SQLException e)
          {
             LOG.error(e.getMessage(), e);
          }
       }
 
       if (statement != null)
       {
          try
          {
             statement.close();
          }
          catch (SQLException e)
          {
             LOG.error(e.getMessage(), e);
          }
       }
 
       if (conn != null)
       {
          try
          {
             conn.close();
          }
          catch (SQLException e)
          {
             LOG.error(e.getMessage(), e);
          }
       }
    }
 
 }
