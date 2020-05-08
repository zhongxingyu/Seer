 /**
  * Copyright (C) 2006 NetMind Consulting Bt.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package hu.netmind.beankeeper.db.impl;
 
 import java.sql.*;
 import java.util.*;
 import hu.netmind.beankeeper.parser.*;
 import hu.netmind.beankeeper.transaction.*;
 import hu.netmind.beankeeper.db.*;
 import org.apache.log4j.Logger;
 
 /**
  * HSQL database implementation.
  * @author Brautigam Robert
  * @version CVS Revision: $Revision$
  */
 public class HSQLDatabaseImpl extends GenericDatabase implements Database
 {
    private static Logger logger = Logger.getLogger(HSQLDatabaseImpl.class);
    
    /**
     * Issue a checkpoint to the server. This works with embedded
     * and client-server case too, because it does not cause the
     * server to exit.
     */
    public void release()
    {
       Connection connection = getConnectionSource().getConnection();
       executeUpdate(connection,"checkpoint defrag");
       super.release();
    }
    
    /**
     * Get the limit component of statement, if it can be expressed in
     * the current database with simple statement part.
     * @param limits The limits to apply.
     */
    protected String getLimitStatement(String statement, Limits limits, List types)
    {
       StringBuffer result = new StringBuffer(statement);
       if ( limits.getLimit() > 0 )
          result.append(" limit "+limits.getLimit());
       if ( limits.getOffset() > 0 )
          result.append(" offset "+limits.getOffset());
       return result.toString();
    }
 
    /**
     * Get the class for an sql type.
     */
    protected String getSQLTypeName(int sqltype)
    {
       switch ( sqltype )
       {
          case Types.VARCHAR:
            return "varchar(1024)";
          case Types.BLOB:
          case Types.BINARY:
          case Types.VARBINARY:
             return "binary";
          default:
             return super.getSQLTypeName(sqltype);
       }
    }
 
    /**
     * Fix custom data types not supported by database.
     */
    protected int getTableAttributeType(ResultSet rs)
       throws SQLException
    {
       int columnType = rs.getInt("DATA_TYPE");
       // Recognize boolean type
       if ( (columnType == Types.DECIMAL) && (rs.getInt("COLUMN_SIZE")==1) )
          columnType = Types.BOOLEAN;
       return columnType;
    }
    
    /**
     * Get the data types of a given table.
     * @return A map of names with the sql type number as value.
     */
    protected HashMap getTableAttributeTypes(Connection connection,
          String tableName)
       throws SQLException
    {
       return super.getTableAttributeTypes(connection,tableName.toUpperCase());
    }
 
    /**
     * Transform functions for hsql.
     */
    protected List transformTerms(List terms)
    {
       for ( int i=0; i<terms.size(); i++ )
       {
          Object term = terms.get(i);
          if ( term instanceof ReferenceTerm )
          {
             Function function = ((ReferenceTerm) term).getFunction();
             if ( (function!=null) && (function instanceof MathematicalPostfixFunction) &&
                   (((MathematicalPostfixFunction)function).getFunction().equals(">>")) )
             {
                // Hsqldb does not support the bitshift operator, so we modify
                // the function
                MathematicalPostfixFunction mathFunction = (MathematicalPostfixFunction) function;
                ((ReferenceTerm)term).setFunction(
                   new MathematicalPostfixFunction("/",""+
                      (1L<<(Long.parseLong(mathFunction.getOperand()))) ));
             }
          }
       }
       return terms;
    }
 
    /**
     * Transform 'ilike' to upper case like.
     * @param expr The expression to possibly transform.
     * @return A transformed expression.
     */
    protected Expression transformExpression(Expression expr)
    {
       Expression result = new Expression(expr);
       result.clear();
       for ( int i=0; i<expr.size(); i++ )
       {
          Object item = expr.get(i);
          if ( "ilike".equals(item) )
          {
             // Here we need to upper() the argument before and after like
             Object arg = result.remove(result.size()-1);
             result.add("upper(");
             result.add(arg);
             result.add(")");
             result.add("like");
             result.add("upper(");
             result.add(expr.get(i+1));
             result.add(")");
             i++; // We used an argument
          } else {
             result.add(item);
          }
       }
       // Transform functions too
       transformTerms(result);
       return result;
    }
    
    /**
     * Get an unused index name.
     */
    protected String getCreateIndexName(Connection connection, String tableName,
          String field)
    {
       return super.getCreateIndexName(connection,tableName.toUpperCase(),field);
    }
 
    /**
     * Drop a column from a table. HSQL does not drop the column if
     * there are indexes to it, so first remove the indexes in question.
     * @param connection The connection object.
     * @param tableName The table to drop column from.
     * @param columnName The column to drop.
     */
    protected TransactionStatistics dropColumn(Connection connection, String tableName,
          String columnName)
    {
       TransactionStatistics stats = new TransactionStatistics();
       try
       {
          logger.debug("detemining indexes before dropping column: "+columnName);
          // Determine indexes for given column, and remove them mercilessly
          long startTime = System.currentTimeMillis();
          DatabaseMetaData dmd = connection.getMetaData();
          ResultSet rs = dmd.getIndexInfo(null,null,tableName.toUpperCase(),false,false);
          while ( rs.next() )
          {
             if ( logger.isDebugEnabled() )
                logger.debug("got index '"+rs.getString("INDEX_NAME")+"', it's column is: "+rs.getString("COLUMN_NAME"));
             // Got index, but only drop it, when it is about our petit column
             if ( columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME")) )
             {
                // It is about the column given, so drop it, for great justice
                executeUpdate(connection,"drop index "+rs.getString("INDEX_NAME"));
                stats.setSchemaCount(stats.getSchemaCount()+1);
             }
          }
          long endTime = System.currentTimeMillis();
          stats.setSchemaCount(stats.getSchemaCount()+1);
          stats.setSchemaTime(endTime-startTime);
       } catch ( SQLException e ) {
          logger.error("error while trying to remove indexes for column: "+tableName+"."+columnName,e);
       }
       // Now we can drop the column
       stats.add(super.dropColumn(connection,tableName,columnName));
       return stats;
    }
 
    /**
     * Get the create table statement before the attributes part.
     */
    protected String getCreateTableStatement(Connection connection, String tableName)
    {
       return "create cached table "+tableName;
    }
 
 }
 
 
 
