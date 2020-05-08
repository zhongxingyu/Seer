 package org.simpledao;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.beans.PropertyDescriptor;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: jumiller
  * Date: Feb 24, 2011
  * Time: 6:54:09 AM
  */
 //todo: insert byte array blob
 //todo: handle clob in insert
 public class SimpleDAO<T>
 {
     private static final Log log = LogFactory.getLog( SimpleDAO.class );
     private static final Log sqlLog = LogFactory.getLog( "SQL" );
 
     public void simpleInsert( T bean ) throws SQLException
     {
         SimpleDBConnection dbc = new SimpleDBConnection();
         Connection con = null;
         try
         {
             con = dbc.getDBConnection();
             simpleInsert( con, bean, getBeanDescriptor(bean) );
         }
         finally
         {
             dbc.closeDBConnection(con);
         }
     }
 
     /**
      * Insert data into the database based on columns introspected from the bean
      * @param con Connection object used to communicate with the database (JDBC)
      * @param bean SimpleBean derived class that has the approprate getters/setters
      * @throws SQLException catch-all
      * @see SimpleBean
      */
     public void simpleInsert( Connection con, T bean ) throws SQLException
     {
         simpleInsert( con, bean, getBeanDescriptor(bean) );
     }
 
 
     /**
      * Creates and executes a SQL INSERT statement against the passed in Connection object.
      * The columns to be inserted along with their values are ascertained from the passed in
      * SimpleBean derived class and the associated Map of bean proeprties.
      * @param con Connection object used to communicate with the database (JDBC)
      * @param bean SimpleBean derived class that has the approprate getters/setters
      * @param description A description of the bean
      * @throws SQLException catch-all
      * @see SimpleBean
      */
     public void simpleInsert( Connection con, T bean, BeanDescriptor description ) throws SQLException
     {
         //ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
 
         //String sql = buildInsertSQL( bean, description, bindVariables);
         //if ( sqlLog.isDebugEnabled() ) { sqlLog.debug("simpleInsert SQL:" + sql); }
 /*
         try
         {
 */
             //todo: refactor this back
             PreparedStatement ps = buildInsertStatement(bean, description, con);
 //            PreparedStatement ps = con.prepareStatement( sql );
             //Utils.bindVariables( ps, bindVariables);
             ps.executeUpdate();
             ps.close();
 /*
         }
         catch ( SQLException e )
         {
             log.error(e);
             throw new SQLException ("A database occurred while inserting: " + e.getMessage(), e);
         }
 */
     }
 
     public T simpleSelect( T criteria ) throws SQLException
     {
 
         SimpleDBConnection dbc = new SimpleDBConnection();
         Connection con = null;
         try
         {
             con = dbc.getDBConnection();
             return simpleSelect( con, criteria);
         }
         finally
         {
             dbc.closeDBConnection( con );
         }
     }
 
     public T simpleSelect( Connection con, T criteria) throws SQLException
     {
         if ( log.isDebugEnabled() ) { log.debug("Get the beans properties");}
 
 /*
         Map<String,ColumnDefinition> properties = null;
         if ( criteria instanceof SimpleBean)
             properties = ((SimpleBean)criteria).describe();
         else
             properties = ReflectionUtils.getBeanPropertyDBColumnMap(criteria );
 
         return simpleSelect( con, criteria, properties );
 */
         return simpleSelect(con, criteria, getBeanDescriptor(criteria));
     }
 
 
     public T simpleSelect( Connection con, T criteria, BeanDescriptor descriptor ) throws SQLException
 //    public T simpleSelect( Connection con, T criteria, Map<String,ColumnDefinition> properties ) throws SQLException
 	{
 		if ( log.isDebugEnabled() ) { log.debug("call simpleSelectList and get the first bean");}
 
 		ArrayList<T> beanList = simpleSelectList(con, criteria, descriptor);
 //		ArrayList<T> beanList = simpleSelectList(con, criteria, properties);
 		if ( beanList.size() > 0 )
 		{
 			return beanList.get(0);
 		}
 		else
 		{
 			return null;
 		}
 	}
 
     public ArrayList<T> simpleSelectList( T criteria ) throws SQLException
     {
         SimpleDBConnection dbc = new SimpleDBConnection();
         Connection con = null;
         try
         {
             con = dbc.getDBConnection();
             return simpleSelectList( con, criteria);
         }
         finally
         {
             dbc.closeDBConnection(con);
         }
     }
 
     public ArrayList<T> simpleSelectList( Connection con, T criteria) throws SQLException
     {
 /*
         Map<String,ColumnDefinition> properties = null;
         if ( criteria instanceof SimpleBean)
             properties = ((SimpleBean)criteria).describe();
         else
             properties = ReflectionUtils.getBeanPropertyDBColumnMap(criteria );
         return simpleSelectList( con, criteria, properties );
 */
         return simpleSelectList( con, criteria, getBeanDescriptor(criteria));
     }
 
     public ArrayList<T> simpleSelectList( Connection con, T bean, BeanDescriptor descriptor ) throws SQLException
     {
         ArrayList<T> beanList = new ArrayList<T>();
         //ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
 //        Map<String,ColumnDefinition> columns = Utils.getColumnMapFromProps( properties );
         Map<String,String> columnPropertyMap = Utils.getColumnPropertyMap( descriptor.getPropertyMap());
 
         // get SQL statement
         //String sql = buildSelectSQL( bean, columns, bindVariables );
         //if ( sqlLog.isDebugEnabled() ) { sqlLog.debug("simpleSelectList SQL:" + sql); }
 
         //try
         //{
             //PreparedStatement ps = con.prepareStatement( sql );
             //Utils.bindVariables( ps, bindVariables);
             PreparedStatement ps = buildSelectStatement( bean, descriptor, con );
             ResultSet rs = ps.executeQuery();
 
 			HashMap<String,Object> props = new HashMap<String,Object>();
 
             ResultSetMetaData metaData = rs.getMetaData();
 
             int columnCount = metaData.getColumnCount();
 
             while ( rs.next() )
             {
                 for ( int i = 1; i <= columnCount ; i++)
                 {
                     if ( columnPropertyMap.containsKey(metaData.getColumnName((i))))
                     {
                         if ( metaData.getColumnType(i) == Types.BLOB )
                         {
                             if ( log.isDebugEnabled() ) { log.debug("simpleSelectList - column # '" + i + " is a BLOB");}
 
                             Blob blob = rs.getBlob( metaData.getColumnName(i) );
 
                             ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                             BufferedInputStream bis = new BufferedInputStream( blob.getBinaryStream() );
 
                             byte[] buffer = new byte[1024];
                             int curByte;
                             try
                             {
                                 while ( ( curByte = bis.read( buffer, 0, buffer.length ) ) != -1 )
                                 {
                                     baos.write( buffer, 0, curByte );
                                 }
                             }
                             catch (IOException e)
                             {
                                 log.error(e);
                                 throw new RuntimeException("Unable to read the blob from the database",e);
                             }
                             //props.put( Utils.getCamelCaseColumnName( metaData.getColumnName(i) ), baos.toByteArray() );
                             if ( log.isDebugEnabled() ) { log.debug("simpleSelectList - write BLOB to bean'" );}
 
                             props.put( columnPropertyMap.get( metaData.getColumnName(i)), rs.getString(i) );
                         }
                         else if ( metaData.getColumnType(i) == Types.DATE )
                         {
                             if ( log.isDebugEnabled() ) { log.debug("simpleSelectList - column # '" + i + "' is a DATE");}
                             props.put( columnPropertyMap.get( metaData.getColumnName(i)), rs.getTimestamp(i) );
                         }
                         else if ( metaData.getColumnType(i) == Types.TIME )
                         {
                             if ( log.isDebugEnabled() ) { log.debug("simpleSelectList - column # '" + i + "' is a TIME");}
                             props.put( columnPropertyMap.get( metaData.getColumnName(i)), rs.getTime(i) );
                         }
                         else if ( metaData.getColumnType(i) == Types.TIMESTAMP )
                         {
                             if ( log.isDebugEnabled() ) { log.debug("simpleSelectList - column # '" + i + "' is a TIMESTAMP");}
                             props.put( columnPropertyMap.get( metaData.getColumnName(i)), rs.getTimestamp(i) );
                         }
                         else
                         {
                             if ( log.isDebugEnabled() ) { log.debug("simpleSelectList - column # '" + i + "' is not special");}
                             //props.put( Utils.getCamelCaseColumnName( metaData.getColumnName(i) ), rs.getString(i) );
                             props.put( columnPropertyMap.get( metaData.getColumnName(i)), rs.getString(i) );
                         }
                     }
 
 				}
 
                 // create the return bean
                 //Type pt = bean.getClass().getGenericSuperclass();
                 T newBean = null;
                 try
                 {
                     newBean = (T)bean.getClass().newInstance();
                 }
                 catch (Exception e)
                 {
                     log.error(e);
                     throw new RuntimeException("Unable to instantiate the new Object",e);
                 }
                 //T newBean = (T)((Class)pt).newInstance();
                 //T newBean = (T)((Class)((ParameterizedType)pt).getActualTypeArguments()[0]).newInstance();
 
 /*
                 if ( newBean instanceof SimpleBean)
                     ((SimpleBean)newBean).populate( props );
 */
                 ReflectionUtils.populateBean(newBean,props);
                 beanList.add( newBean );
             }
             ps.close();
         //}
 /*
         catch ( SQLException e )
         {
             log.error(e);
             throw new SQLException("A database error occurred while selecting the data: " + e.getMessage() ,e);
         }
 */
         return beanList;
     }
 
     public void simpleUpdate( T bean ) throws SQLException
     {
         SimpleDBConnection dbc = new SimpleDBConnection();
         Connection con = null;
         try
         {
             con = dbc.getDBConnection();
             simpleUpdate( con, bean, getBeanDescriptor(bean) );
         }
         finally
         {
             dbc.closeDBConnection(con);
         }
     }
 
     /**
      * Introspects the passed SimpleBean for columns that need to be updated.  The
      * resulting Map of columns is then passed to the simpleUpdate method that
      * accepts a Map parameter.
      *
      * @param con Connection object used to communicate with the database (JDBC) and run the UPDATE
      * @param bean  SimpleBean derived class with getters and setters that holds the values to update.
      * This method introspects the passed SimpleBean for columns to update
      * @throws SQLException catch-all
      * @see SimpleBean
      */
     public void simpleUpdate( Connection con, T bean ) throws SQLException
     {
         simpleUpdate( con, bean , getBeanDescriptor(bean) );
     }
 
     /**
      * Creates and runs a SQL UPDATE statement against the passed database connection object.
      * The columns to be updated along with their values are ascertained from the passed in
      * SimpleBean derived class and Map of columns.
      *
      * @param con Connection object used to communicate with the database (JDBC) and run the UPDATE
      * @param bean  SimpleBean derived class with getters and setters that holds the values to update
      * @param description A Map of SimpleBean properties that will be used as columns to update
      * @throws SQLException catch-all
      * @see SimpleBean
      */
     public void simpleUpdate( Connection con, T bean,BeanDescriptor description ) throws SQLException
     {
         ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
 
 //        String sql = buildUpdateSQL( bean, description, bindVariables);
 
 //        try
 //        {
             //todo: refactor this back
             PreparedStatement ps = buildUpdateStatement(bean, description, con);
 //            PreparedStatement ps = con.prepareStatement( sql );
 //            Utils.bindVariables( ps, bindVariables);
             ps.executeUpdate();
             ps.close();
 /*
         }
         catch ( SQLException e )
         {
             log.error(e);
             throw new SQLException("A database error occurred while saving: " + e.getMessage() ,e);
         }
 */
     }
 
     public void simpleDelete( T bean ) throws SQLException
     {
         SimpleDBConnection dbc = new SimpleDBConnection();
         Connection con = null;
         try
         {
             con = dbc.getDBConnection();
             simpleDelete( con, bean, getBeanDescriptor(bean) );
         }
         finally
         {
             dbc.closeDBConnection(con);
         }
     }
 
     /**
      *
      * @param con Connection object used to run the DELETE statement against
      * @param bean SimpleBean derived class used to determine the table name and WHERE clause
      * @throws SQLException catch-all
      * @see SimpleBean
      */
     public void simpleDelete( Connection con, T bean ) throws SQLException
     {
         simpleDelete( con, bean , getBeanDescriptor(bean) );
     }
 
 
     /**
      * Creates and executes a SQL DELETE statement against the Connection parameter.
      * The table name is determined from the SimpleBean derived class and the WHERE clause
      * is asertained from the Map of SimpleBean properties.
      *
      * @param con Connection object used to run the DELETE statement against
      * @param bean SimpleBean derived class used to determine the table name and WHERE clause
      * @param description Map of SimpleBean properties used to determine WHERE clause
      * @throws SQLException catch-all
      */
     public void simpleDelete( Connection con, T bean, BeanDescriptor description ) throws SQLException
     {
 //        ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
 
 //        String sql = buildDeleteSQL( bean, description, bindVariables);
 
 //        try
 //        {
             //todo: refactor this back
             PreparedStatement ps = buildDeleteStatement(bean, description, con);
 //            PreparedStatement ps = con.prepareStatement( sql );
             //Utils.bindVariables( ps, bindVariables);
             ps.executeUpdate();
             ps.close();
 /*
         }
         catch ( SQLException e )
         {
             log.error(e);
             throw new SQLException("A database error occurred while deleting." ,e);
         }
 */
     }
 
     //-----------------------PRIVATE METHODS---------------------------------
 
     private PreparedStatement buildInsertStatement(T bean, BeanDescriptor description, Connection con ) throws SQLException
     {
         ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
         StringBuffer sql = new StringBuffer("INSERT INTO " );
         StringBuffer valuesSQL = new StringBuffer(" ) VALUES ( ");
         int propCount = 0;
 
         sql.append( description.getTable() );
         sql.append( " ( " );
 
         for (String property : description.getPropertyMap().keySet())
         {
             String column = description.getPropertyMap().get(property).getName();
             // if the database column is not already specified (unlikely), then determine it
             if (column == null || "".equals(column))
             {
                 column = Utils.getPropertyDBName(property);
             }
 
             PropertyDescriptor pd = null;
             Object value = null;
             try
             {
                 pd = PropertyUtils.getPropertyDescriptor( bean, property);
                 value = PropertyUtils.getProperty ( bean, property);
             }
             catch (Exception e)
             {
                 log.error(e);
                 throw new RuntimeException("Unable to get the bean property named '" + property + "'",e);
             }
 
 //            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, property);
 //            Object value = PropertyUtils.getProperty(bean, property);
 
             Class type = pd.getPropertyType();
 
             //todo: replace this with ReflectionUtils.isPropertyNull()
             if (value == null ||
 			  (type == Integer.class || "int".equals(type.getName())) && ((Integer) value < 0) ||
 			  ( type == Double.class || "double".equals( type.getName() ) ) && ((Double) value < 0.0d))
             {
                 continue;
             }
 
             if (propCount > 0)
             {
                 sql.append(", ");
                 valuesSQL.append(", ");
             }
             sql.append(column);
             valuesSQL.append("?");
 
             propCount++;
             bindVariables.add(new BoundVariable(propCount, column, type, value));
         }
         sql.append( valuesSQL );
         sql.append( " )");
 
         if ( sqlLog.isDebugEnabled() ) { sqlLog.debug("buildInsertStatement SQL:" + sql); }
 
 /*
         PreparedStatement ps = con.prepareStatement( sql.toString() );
         Utils.bindVariables(ps, bindVariables);
         return ps;
 */
 //        return sql.toString();
         return Utils.prepareStatement(con, sql.toString(), bindVariables);
     }
 
     private PreparedStatement buildSelectStatement( T bean, BeanDescriptor descriptor, Connection con) throws SQLException
     {
         String sql= "";
 
         ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
 /*
         if ( getBeanDBTableName(bean).toUpperCase().contains("SELECT ") &&
                 getBeanDBTableName(bean).toUpperCase().contains("FROM "))
 */
         if ( descriptor.getTable().toUpperCase().contains("SELECT ") &&
                 descriptor.getTable().toUpperCase().contains("FROM "))
         {
             sql = descriptor.getTable();
 //            sql = getBeanDBTableName(bean);
             String loopsql = descriptor.getTable();
 //            String loopsql = getBeanDBTableName(bean);
             int paramCount = 0;
             while ( loopsql.indexOf("@") > -1)
             {
                 String param;
                 int paramStart = loopsql.indexOf("@") + 1;
                 int paramEnd = loopsql.indexOf(" ", paramStart);
                 if ( paramEnd > paramStart)
                 {
                     param = loopsql.substring(paramStart, paramEnd );
                 }
                 else
                 {
                     param = loopsql.substring(paramStart);
                 }
                 PropertyDescriptor pd = null;
                 Object value = null;
                 try
                 {
                     pd = PropertyUtils.getPropertyDescriptor( bean, param);
                     value = PropertyUtils.getProperty ( bean, param);
                 }
                 catch (Exception e)
                 {
                     log.error(e);
                     throw new RuntimeException("Unable to get the bean property named '" + param + "'",e);
                 }
                 paramCount ++;
                 bindVariables.add( new BoundVariable( paramCount, descriptor.getPropertyMap().get(param).getName(), pd.getPropertyType(), value));
 //                bindVariables.add( new BoundVariable( paramCount, Utils.getPropertyDBName(param), pd.getPropertyType(), value));
                 sql = sql.replace("@" + param, "?");
                 loopsql = loopsql.substring( loopsql.indexOf("@" + param) + param.length());
             }
         }
         else
         {
 
             StringBuffer selectSQL = new StringBuffer( "SELECT ");
             StringBuffer whereSQL = new StringBuffer(" FROM " );
             StringBuffer orderSQL = new StringBuffer("");
 
             whereSQL.append( descriptor.getTable() );
 //            whereSQL.append( getBeanDBTableName(bean) );
 
 //            Iterator iter = props.keySet().iterator();
 
             int colCount = 0;
             int whereCount = 0;
 //            int orderCount = 0;
 
             for (String property : descriptor.getPropertyMap().keySet())
 //            while ( iter.hasNext() )
             {
 //                String property = (String) iter.next();
                 String column = descriptor.getPropertyMap().get(property).getName();
 //                String column = props.get(property);
 
                 if ( log.isDebugEnabled()) { log.debug("buildSelectStatement - get property '" + property + "' for column '" + column + "'");}
                 
                 PropertyDescriptor pd = null;
                 Object value = null;
                 try
                 {
                     pd = PropertyUtils.getPropertyDescriptor( bean, property );
                     value = PropertyUtils.getProperty ( bean, property );
                 }
                 catch (Exception e)
                 {
                     throw new RuntimeException("Unable to get the property '" + property + "'",e);
                 }
                 Class type = pd.getPropertyType();
 
                 if ( colCount > 0 )
                 {
                     selectSQL.append(", " );
                 }
                 selectSQL.append( column );
                 colCount++;
 
                 //todo: deal with nullable properties
                 if ( Utils.isPropertyNull( type, value ) )
                 {
                     //continue;
                 }
                 else
                 {
                     if ( whereCount == 0 )
                     {
                         whereSQL.append( " WHERE " );
                     }
                     else
                     {
                         whereSQL.append(" AND " );
                     }
                     whereSQL.append( column );
                     if ( value.toString().indexOf("%") > -1 )
                     {
                         whereSQL.append( " LIKE ? " );
                     }
                     else
                     {
                         whereSQL.append( " = ? " );
                     }
                     whereCount ++;
                     bindVariables.add( new BoundVariable( whereCount, column, type, value));
                 }
 
             }
 
             if ( descriptor.getOrderedColumns() != null && descriptor.getOrderedColumns().size() > 0 )
             {
                 //todo: this might need to look at the property<->column map to make sure this column is included
                 for ( int i = 1; i <= descriptor.getOrderedColumns().size(); i ++ )
                 {
                     SortedColumn sc = descriptor.getOrderedColumns().get(i);
                     if ( i == 1 )
                         orderSQL.append(" ORDER BY ");
                     else
                         orderSQL.append(", ");
                     orderSQL.append( sc.getName() );
                     if ( sc.getSortOrder() == SortOrder.DESCENDING)
                         orderSQL.append(" DESC");
                 }
             }
             selectSQL.append ( whereSQL );
             selectSQL.append( orderSQL );
             sql = selectSQL.toString();
         }
 /*
         PreparedStatement stmt = con.prepareStatement(sql);
         Utils.bindVariables(stmt, bindVariables);
         return stmt;
 */
         return Utils.prepareStatement(con, sql, bindVariables);
     }
 
     private PreparedStatement buildUpdateStatement( T bean, BeanDescriptor description, Connection con) throws SQLException
     {
         ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
         StringBuilder sql = new StringBuilder( "UPDATE " );
         StringBuilder whereSQL = new StringBuilder(" WHERE ");
         ArrayList<BoundVariable> keyBindVariables = new ArrayList<BoundVariable>();
 
         int columnCount = 0;
         int keyCount = 0;
 
         sql.append( description.getTable()  );
 
         sql.append( " SET " );
 
         String[] keys = description.getUpdateKeys();
 
         for (String property : description.getPropertyMap().keySet())
         {
             ColumnDefinition def = description.getPropertyMap().get(property);
             String column = def.getName();
             if (column == null || "".equals(column))
             {
                 column = Utils.getPropertyDBName(property);
             }
 //            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, property);
 //            Object value = PropertyUtils.getProperty(bean, property);
 
             PropertyDescriptor pd = null;
             Object value = null;
             try
             {
                 pd = PropertyUtils.getPropertyDescriptor( bean, property );
                 value = PropertyUtils.getProperty ( bean, property );
             }
             catch (Exception e)
             {
                 throw new RuntimeException("Unable to get the property '" + property + "'",e);
             }
 
             if ( isColumnAKey( keys,column ))
             {
                 //if ( ( (Integer) value).intValue()  < 1 )
                 if (Utils.isPropertyNull(pd.getPropertyType(), value))
                 {
                     throw new SimpleDAOException("Key may not have a null/0 value;");
                 }
                 if (keyCount > 0)
                 {
                     whereSQL.append(" AND ");
                 }
                 whereSQL.append(column);
                 whereSQL.append(" = ?");
 
                 //todo: this count can go away, just use the size of the key BB list
                 keyCount++;
                 keyBindVariables.add( new BoundVariable( keyCount, column, pd.getPropertyType(), value ) );
             }
             else
             {
                 Class type = pd.getPropertyType();
                 StringBuilder col = new StringBuilder();
                 if (columnCount > 0 || col.length() > 0)
                 {
                     col.append(", ");
                 }
                 col.append(column);
                 col.append(" = ");
 
 //				if (value == null ||
 //				  (type == Integer.class || "int".equals(type.getName())) && ((Integer) value < 0) ||
 //				  ( type == Double.class || "double".equals( type.getName() ) ) && ((Double) value < 0.0d))
                 if ( Utils.isPropertyNull( type, value) )
 				{
                     if ( def.isNullable() )
                     {
                        col.append("NULL");
                         //todo: no need to increment count, it's not going to be a bound var
                         //columnCount++;
                     }
                     else
                     {
                         continue;
                     }
 				}
                 else
                 {
                     col.append("?");
                     columnCount++;
                     bindVariables.add(new BoundVariable(columnCount, column, type, value));
                 }
                 sql.append(col);
             }
         }
 
         // add the keys to the bind variable list
         for ( BoundVariable bv : keyBindVariables)
         {
             bindVariables.add( new BoundVariable(columnCount + bv.getPosition(), bv.getName(), bv.getType(), bv.getValue() ) );
         }
 
         sql.append( whereSQL );
 //        return sql.toString();
         if ( sqlLog.isDebugEnabled() ) { sqlLog.debug("buildUpdateStatement SQL:" + sql); }
 /*
         PreparedStatement stmt = con.prepareStatement(sql.toString());
         Utils.bindVariables(stmt, bindVariables);
         return stmt;
 */
         return Utils.prepareStatement(con, sql.toString(), bindVariables);
     }
 
     private PreparedStatement buildDeleteStatement( T bean, BeanDescriptor description,Connection con ) throws SQLException
     {
         ArrayList<BoundVariable> bindVariables = new ArrayList<BoundVariable>();
         StringBuffer sql = new StringBuffer( "DELETE ");
 
         sql.append( description.getTable() );
         sql.append( " WHERE " );
 
 //        Iterator iter = properties.keySet().iterator();
 
         int colCount = 0;
 /*
         while ( iter.hasNext() )
         {
 */
         for (String property : description.getPropertyMap().keySet())
         {
             String column = description.getPropertyMap().get( property ).getName();
 //            if ( column == null || "".equals( column ) )
 //            {
 //                column = Utils.getPropertyDBName( property );
 //            }
 //            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( bean, property );
 //            Object value = PropertyUtils.getProperty ( bean, property );
             PropertyDescriptor pd = null;
             Object value = null;
             try
             {
                 pd = PropertyUtils.getPropertyDescriptor( bean, property );
                 value = PropertyUtils.getProperty ( bean, property );
             }
             catch (Exception e)
             {
                 throw new RuntimeException("Unable to get the property '" + property + "'",e);
             }
             Class type = pd.getPropertyType();
 
 /*
             if ( value != null )
             {
                 if  ( ( type == Integer.class || "int".equals( type.getName() ) ) &&  ( ((Integer)value).intValue()  < 0 ) )
                 {
                     continue;
                 }
 */
 /*
             if ( value == null ||
                  ( ( type == Integer.class || "int".equals( type.getName() ) ) &&  ( ((Integer)value).intValue()  < 0 ) ) )
 */
             if ( !Utils.isPropertyNull( type, value ) )
             {
 
                 if ( colCount > 0 )
                 {
                     sql.append(" AND ");
                 }
                 sql.append( column );
                 sql.append( " = ? " );
                 colCount ++;
                 bindVariables.add( new BoundVariable( colCount, column, type, value ));
             }
         }
         //return sql.toString();
         if ( sqlLog.isDebugEnabled() ) { sqlLog.debug("buildDeleteStatement SQL:" + sql); }
 //        PreparedStatement stmt = con.prepareStatement(sql.toString());
 //        Utils.bindVariables(stmt, bindVariables);
 //        return stmt;
         return Utils.prepareStatement(con, sql.toString(), bindVariables);
     }
 
 /*
     private String buildSelectSQL( T bean, Map<String,ColumnDefinition> columns, ArrayList<BoundVariable> bindVariables ) throws SQLException
     {
         if ( getBeanDBTableName(bean).toUpperCase().contains("SELECT ") &&
                 getBeanDBTableName(bean).toUpperCase().contains("FROM "))
         {
             String sql = getBeanDBTableName(bean);
             String loopsql = getBeanDBTableName(bean);
             int paramCount = 0;
             while ( loopsql.indexOf("@") > -1)
             {
                 String param;
                 int paramStart = loopsql.indexOf("@") + 1;
                 int paramEnd = loopsql.indexOf(" ", paramStart);
                 if ( paramEnd > paramStart)
                 {
                     param = loopsql.substring(paramStart, paramEnd - paramStart);
                 }
                 else
                 {
                     param = loopsql.substring(paramStart);
                 }
                 PropertyDescriptor pd = null;
                 Object value = null;
                 try
                 {
                     pd = PropertyUtils.getPropertyDescriptor( bean, param);
                     value = PropertyUtils.getProperty ( bean, param);
                 }
                 catch (Exception e)
                 {
                     log.error(e);
                     throw new RuntimeException("Unable to get the bean property named '" + param + "'",e);
                 }
                 paramCount ++;
                 bindVariables.add( new BoundVariable( paramCount, Utils.getPropertyDBName(param), pd.getPropertyType(), value));
                 sql = sql.replace("@" + param, "?");
                 loopsql = loopsql.substring( loopsql.indexOf(param) + param.length());
             }
             return sql;
         }
 
         StringBuffer selectSQL = new StringBuffer( "SELECT ");
         StringBuffer whereSQL = new StringBuffer(" FROM " );
         StringBuffer orderSQL = new StringBuffer("");
 
         whereSQL.append( getBeanDBTableName(bean) );
 
         Iterator iter = columns.keySet().iterator();
 
         int colCount = 0;
         int whereCount = 0;
         int orderCount = 0;
         while ( iter.hasNext() )
         {
             String column = (String) iter.next();
             String property = columns.get(column);
             Map <Integer,SortedColumn> orderBy = getBeanOrderBy(bean);
             if ( orderBy != null && orderBy.get(property) != null )
             {
                 if ( orderCount == 0 )
                 {
                     orderSQL.append( " ORDER BY ");
                 }
                 else
                 {
                     orderSQL.append(", ");
                 }
                 orderSQL.append(column);
 //                if ( !orderBy.get(property))
 //                {
 //                    orderSQL.append(" DESC");
 //                }
                 orderCount++;
             }
 
             PropertyDescriptor pd = null;
             Object value = null;
             try
             {
                 pd = PropertyUtils.getPropertyDescriptor( bean, property );
                 value = PropertyUtils.getProperty ( bean, property );
             }
             catch (Exception e)
             {
                 throw new RuntimeException("Unable to get the property '" + property + "'",e);
             }
             Class type = pd.getPropertyType();
 
             if ( colCount > 0 )
             {
                 selectSQL.append(", " );
             }
             selectSQL.append( column );
             colCount++;
 
             // check to see if the value of this column is null
             // if so, add it to the SELECT list
             // if not, add it to WHERE clause
 //            if ( value == null ||
 //                 ( ( type == Integer.class || "int".equals( type.getName() ) ) &&  ( ((Integer)value).intValue()  < 0 ) ) )
             if ( Utils.isPropertyNull( type, value ) )
             {
                 //continue;
             }
             else
             {
                 if ( whereCount == 0 )
                 {
                     whereSQL.append( " WHERE " );
                 }
                 else
                 {
                     whereSQL.append(" AND " );
                 }
                 whereSQL.append( column );
                 if ( value.toString().indexOf("%") > -1 )
 				{
 					whereSQL.append( " LIKE ? " );
 				}
 				else
 				{
 					whereSQL.append( " = ? " );
 				}
                 whereCount ++;
                 bindVariables.add( new BoundVariable( whereCount, column, type, value));
             }
 //                if ( colCount > 0 )
 //                {
 //                    selectSQL.append(", " );
 //                }
 //                selectSQL.append( Utils.getPropertyDBName( column ) );
 //                colCount++;
 //            }
 //            else
 //            {
         }
         selectSQL.append ( whereSQL );
         selectSQL.append( orderSQL);
         return selectSQL.toString();
     }
 */
     private BeanDescriptor getBeanDescriptor( T bean )
     {
         if ( bean instanceof SimpleBean)
             return ((SimpleBean)bean).describe();
         else
             return ReflectionUtils.describeBean(bean);
     }
 
     private boolean isColumnAKey( String[] keys, String column)
     {
         for ( String key : keys )
         {
             if ( key.equalsIgnoreCase( column ))
             {
 				if ( log.isDebugEnabled()) { log.debug("Key column found: " + column) ;}
 				return true;
             }
         }
         return false;
     }
     /*
     private String getBeanDBTableName(T bean )
     {
         //ParameterizedType pt = (ParameterizedType)bean.getClass().getGenericSuperclass();
         //T newBean = (T)((Class)pt.getActualTypeArguments()[0]).newInstance();
         if ( bean instanceof SimpleBean)
             return ((SimpleBean)bean).getDBTableName();
         else
         {
             return ReflectionUtils.inferBeanDBTableName(bean);
         }
     }
 
     private String[] getBeanDBUpdateKeys(T bean)
     {
         if ( bean instanceof SimpleBean)
             return ((SimpleBean)bean).getDBPrimaryKey();
         else
             return ReflectionUtils.inferBeanDBUpdateKeys(bean);
     }
 
     private Map<Integer, SortedColumn> getBeanOrderBy( T bean )
     {
 
 //        ParameterizedType pt = (ParameterizedType)bean.getClass().getGenericSuperclass();
 //        T newBean = (T)((Class)pt.getActualTypeArguments()[0]).newInstance();
 //        if ( newBean instanceof SimpleBean)
  //           return ((SimpleBean)newBean).getDBOrderByProps();
 
         if ( bean instanceof SimpleBean)
             return ((SimpleBean)bean).getDBOrderBy();
         else
             return ReflectionUtils.getBeanDBOrderBy(bean);
     }
     */
 
 }
