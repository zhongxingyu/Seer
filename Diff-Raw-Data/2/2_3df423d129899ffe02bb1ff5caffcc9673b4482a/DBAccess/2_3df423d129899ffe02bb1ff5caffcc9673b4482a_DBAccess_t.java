 /*
 Dynamo Web Services is a web service project for administering LucidDB
 Copyright (C) 2010 Dynamo Business Intelligence Corporation
 
 This program is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation; either version 2 of the License, or (at your option)
 any later version approved by Dynamo Business Intelligence Corporation.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 package com.dynamobi.ws.util;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.sql.DataSource;
 
 
 import com.dynamobi.ws.domain.Column;
 import com.dynamobi.ws.domain.ColumnStats;
 import com.dynamobi.ws.domain.ForeignServer;
 import com.dynamobi.ws.domain.ForeignTable;
 import com.dynamobi.ws.domain.RelNode;
 import com.dynamobi.ws.domain.ShowPlanEntity;
 import com.dynamobi.ws.domain.SystemParameter;
 import com.dynamobi.ws.domain.TableDetails;
 
 import com.dynamobi.ws.domain.Wrapper;
 
 /**
  * Get Tables' info from database
  * 
  * @author Ray Zhang
  * @since Jan-12-2010
  *
  * @author Kevin Secretan
  * @since June-14-2010
  */
 public class DBAccess
 {
     public static String REGEX1 = "(\\w+)[\\(|:]";
     public static String REGEX2 = ":\\srowcount\\s=\\s(.+),\\scumulative\\scost\\s=\\s(.+)";
     public static String connection_catalog = "";
 
     public static DataSource connDataSource = null;
 
     private DBAccess()
     {
 
     }
 
     public static Connection getConnection()
         throws SQLException
     {
       return DB.getConnection();
     }
 
     public static void releaseConnection() {
       DB.releaseConnection();
     }
 
     public static SystemParameter findSystemParameterByName(String paramName)
         throws AppException
     {
 
         SystemParameter retVal = new SystemParameter();
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             conn = getConnection();
 
             ps = conn.prepareStatement("select param_name, param_value from localdb.sys_root.dba_system_parameters where param_name = ?");
             ps.setString(1, paramName);
             rs = ps.executeQuery();
 
             while (rs.next()) {
 
                 retVal.setParamName(rs.getString(1));
                 retVal.setParamValue(rs.getString(2));
                 break;
 
             }
 
         } catch (SQLException e) {
 
             e.printStackTrace();
             throw new AppException(
                 "Error Info: The connection was bad or Execute sql statment failed!");
         }  finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 
 
             } catch (SQLException ex) {
 
                 throw new AppException("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return retVal;
 
     }
 
     public static List<SystemParameter> getAllSystemParameters()
         throws AppException
     {
 
         List<SystemParameter> retVal = new ArrayList<SystemParameter>();
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             conn = getConnection();
 
             ps = conn.prepareStatement("select param_name, param_value from localdb.sys_root.dba_system_parameters");
             rs = ps.executeQuery();
 
             while (rs.next()) {
 
                 SystemParameter en = new SystemParameter();
                 en.setParamName(rs.getString(1));
                 en.setParamValue(rs.getString(2));
                 retVal.add(en);
 
             }
 
         }  catch (SQLException e) {
 
             e.printStackTrace();
             throw new AppException(
                 "Error Info: The connection was bad or Execute sql statment failed!");
         }  finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
         
             } catch (SQLException ex) {
 
                 throw new AppException("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return retVal;
 
     }
 
     public static boolean updateSystemParameter(
         String paramName,
         String paramValue)
         throws AppException
     {
 
         boolean retVal = false;
 
         Connection conn = null;
         PreparedStatement ps = null;
 
         try {
 
             conn = getConnection();
             String sql = "alter system set \"" + paramName + "\" = '"
                 + paramValue + "'";
             ps = conn.prepareStatement(sql);
             ps.execute();
 
             retVal = true;
 
         }  catch (SQLException e) {
 
             e.printStackTrace();
             throw new AppException(
                 "Error Info: The connection was bad or Execute sql statment failed!");
         } finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (ps != null) {
                     ps.close();
                 }
 
 
             } catch (SQLException ex) {
 
                 throw new AppException("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return retVal;
 
     }
 
     public static List<ColumnStats> getAllColumnStats()
         throws AppException
     {
 
         List<ColumnStats> retVal = new ArrayList<ColumnStats>();
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             conn = getConnection();
 
             ps = conn.prepareStatement("select catalog_name, schema_name, table_name, column_name, distinct_value_count, is_distinct_value_count_estimated, percent_sampled, sample_size from localdb.sys_root.dba_column_stats");
             rs = ps.executeQuery();
 
             while (rs.next()) {
 
                 ColumnStats en = new ColumnStats();
                 en.setCatalogName(rs.getString(1));
                 en.setSchemaName(rs.getString(2));
                 en.setTableName(rs.getString(3));
                 en.setColumnName(rs.getString(4));
                 en.setDistinctValueCount(rs.getLong(5));
                 en.setDistinctValueCountEstimated(rs.getBoolean(6));
                 en.setPercentSampled(rs.getDouble(7));
                 en.setSampleSize(rs.getLong(8));
                 retVal.add(en);
 
             }
 
         }  catch (SQLException e) {
 
             e.printStackTrace();
             throw new AppException(
                 "Error Info: The connection was bad or Execute sql statment failed!");
         }  finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
 
             } catch (SQLException ex) {
 
                 throw new AppException("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return retVal;
 
     }
 
     public static List<ColumnStats> findColumnStats(
         String catalogName,
         String schemaName,
         String tableName,
         String columnName)
         throws AppException
     {
 
         List<ColumnStats> retVal = new ArrayList<ColumnStats>();
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             conn = getConnection();
 
             StringBuffer sql = new StringBuffer();
 
             List<String> myConditions = new ArrayList<String>();
             sql.append("select catalog_name, schema_name, table_name, column_name, distinct_value_count, is_distinct_value_count_estimated, percent_sampled, sample_size from localdb.sys_root.dba_column_stats where ");
             if (catalogName != null && !catalogName.isEmpty()) {
                 myConditions.add("catalog_name = '" + catalogName + "'");
             }
 
             if (schemaName != null && !schemaName.isEmpty()) {
                 myConditions.add("schema_name = '" + schemaName + "'");
             }
 
             if (tableName != null && !tableName.isEmpty()) {
                 myConditions.add("table_name = '" + tableName + "'");
             }
 
             if (columnName != null && !columnName.isEmpty()) {
                 myConditions.add("column_name = '" + columnName + "'");
             }
 
             if (myConditions.size() > 1) {
 
                 int size = myConditions.size();
 
                 for (int i = 0; i < size; i++) {
 
                     if ((size - i) != 1)
                         sql.append(myConditions.get(i)).append(" and ");
                     else
                         sql.append(myConditions.get(i));
                 }
             }
             ps = conn.prepareStatement(sql.toString());
             rs = ps.executeQuery();
 
             while (rs.next()) {
 
                 ColumnStats en = new ColumnStats();
                 en.setCatalogName(rs.getString(1));
                 en.setSchemaName(rs.getString(2));
                 en.setTableName(rs.getString(3));
                 en.setColumnName(rs.getString(4));
                 en.setDistinctValueCount(rs.getLong(5));
                 en.setDistinctValueCountEstimated(rs.getBoolean(6));
                 en.setPercentSampled(rs.getDouble(7));
                 en.setSampleSize(rs.getLong(8));
                 retVal.add(en);
 
             }
 
         } catch (SQLException e) {
 
             e.printStackTrace();
             throw new AppException(
                 "Error Info: The connection was bad or Execute sql statment failed!");
         } finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
 
 
             } catch (SQLException ex) {
 
                 throw new AppException("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return retVal;
 
     }
 
     public static String getDBMetaData(String connection, String catalog)
         throws Exception
     {
 
         StringBuffer result = new StringBuffer();
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             conn = getConnection();
 
             ps = conn.prepareStatement("select "
                 + "case t.table_type "
                 + "when 'LOCAL TABLE' then 'Table' "
                 + "when 'FOREIGN TABLE' then 'Foreign_Table' "
                 + "when 'LOCAL VIEW' then 'View' "
                 + "else null "
                 + "end as ObjectType, "
                 + "s.schema_name, "
                 + "c.table_name AS Object, "
                 + "c.column_name AS ColumnName, "
                 + "c.ordinal_position AS ColumnOrder, "
                 + "c.\"PRECISION\" AS Length, "
                 + "c.is_nullable AS Nullable, "
                 + "c.datatype AS DataType "
                 + "from localdb.sys_root.dba_schemas s "
                 + "LEFT OUTER JOIN localdb.sys_root.dba_columns c ON c.schema_name = s.schema_name AND c.catalog_name = s.catalog_name "
                + "LEFT OUTER JOIN localdb.sys_root.dba_tables t ON t.table_name = c.table_name AND t.schema_name = c.schema_name AND t.catalog_name = c.catalog_name "
                 + "where s.catalog_name = ? "
                 + "order by schema_name,ObjectType,Object,ColumnOrder");
             ps.setString(1, catalog);
             rs = ps.executeQuery();
 
             /* This 'monstrosity' makes perfect sense if you don't think too hard about the data types,
              * here's a java-less sample:
              * meta_data =
              * {
              *  'schema_name' : 
              *                  {'table_type (either View or Table)' :
              *                          {  'table_name' :
              *                                   {'column_name' : 'column data (e.g. "(INTEGER, NULL)")',
              *                                   'column2_name' : 'column_data',
              *                                   'column3_name' : 'column_data' }
              *                            , 'table2_name' : {'column_name' : 'data'}
              *                           }
              *                    , 'possible second type' : {etc}
              *                  }
              *  , 'other_schema' : {etc}
              * }
              * 
              * This makes it super intuitive to build the XML structure looping through these,
              * again if you ignore the verbose data typing. Scroll down to the loops and check out
              * the xml if you're still confused.
              *
              * TODO: Admittedly we should probably be populating a
              * struct/object in domain with the data and not formatting
              * the XML directly here. Plus this is really bad XML design.
              */
 
             Map<String, Map<String, Map<String, Map<String, String>>> > meta_data = new LinkedHashMap<String, Map<String, Map<String, Map<String, String>>> >();
 
             while (rs.next()) {
                 int c = 1;
                 String type = rs.getString(c++);
                 String schemaName = rs.getString(c++);
                 if (schemaName != null)
                   schemaName = schemaName.replaceAll("\"","&quot;");
                 String name = rs.getString(c++);
                 if (name != null)
                   name = name.replaceAll("\"", "&quot;");
                 String col_name = rs.getString(c++);
                 if (col_name != null)
                   col_name = col_name.replaceAll("\"", "&quot;");
                 c++;
                 int col_len = rs.getInt(c++);
                 boolean is_null = rs.getBoolean(c++);
                 String data_type = rs.getString(c++);
 
                 if (!meta_data.containsKey(schemaName)) {
                   Map<String, Map<String, Map<String, String>> > new_schema = new LinkedHashMap<String, Map<String, Map<String, String>> >();
                   meta_data.put(schemaName, new_schema);
                 }
                 if (type != null && !meta_data.get(schemaName).containsKey(type)) {
                   Map<String, Map<String, String> > new_type = new LinkedHashMap<String, Map<String, String>>();
                   meta_data.get(schemaName).put(type, new_type);
                 }
                 if (name != null && !meta_data.get(schemaName).get(type).containsKey(name)) {
                   Map<String, String> new_table = new LinkedHashMap<String, String>();
                   meta_data.get(schemaName).get(type).put(name, new_table);
                 }
                 if (col_name != null && !meta_data.get(schemaName).get(type).get(name).containsKey(col_name)) {
                   String null_str = (is_null) ? "NULL" : "NOT NULL";
                   String col_info = "(" + data_type + ", " + null_str + ")";
                   meta_data.get(schemaName).get(type).get(name).put(col_name, col_info);
                 }
 
             }
 
             // Now build the XML to be returned to the client.
             result.append("<schemas label=\"Schemas\">\n"); // root node for our metadata
 
             for (Map.Entry<String, Map<String, Map<String, Map<String, String>>> > schema : meta_data.entrySet()) {
               result.append("<schema label=\"" + schema.getKey() + "\">\n");
               // Force some children to be added even if we didn't get them
               // in the query. (They should have a loadInfo defined..)
               Map<String, Map<String, String> > forced_type = new LinkedHashMap<String, Map<String, String>>();
               if (!meta_data.get(schema.getKey()).containsKey("Table"))
                   meta_data.get(schema.getKey()).put("Table", forced_type);
               if (!meta_data.get(schema.getKey()).containsKey("View"))
                   meta_data.get(schema.getKey()).put("View", forced_type);
               if (!meta_data.get(schema.getKey()).containsKey("Foreign_Table"))
                   meta_data.get(schema.getKey()).put("Foreign_Table", forced_type);
               if (!meta_data.get(schema.getKey()).containsKey("Function"))
                   meta_data.get(schema.getKey()).put("Function", forced_type);
               if (!meta_data.get(schema.getKey()).containsKey("Procedure"))
                   meta_data.get(schema.getKey()).put("Procedure", forced_type);
               if (!meta_data.get(schema.getKey()).containsKey("Jar"))
                   meta_data.get(schema.getKey()).put("Jar", forced_type);
 
               for (Map.Entry<String, Map<String, Map<String, String>>> type : schema.getValue().entrySet()) {
                 result.append("  <" + type.getKey().toLowerCase() + "s label=\"" + type.getKey().replace("_", " ") + "s\">\n");
                 for (Map.Entry<String, Map<String, String>> table : type.getValue().entrySet()) {
                   String table_data = "    <" + type.getKey().toLowerCase() + " label=\"" + table.getKey() + "\" sqlquery=\"SELECT ";
                   String column_data = "";
                   for (Map.Entry<String, String> column : table.getValue().entrySet()) {
                     column_data += "      <column column=\"" + column.getKey() + "\" label=\"" + column.getKey()
                       + " " + column.getValue() + "\" />\n";
                     table_data += "&quot;" + column.getKey() + "&quot;, ";
                   }
                   // chop off the last comma-space and finish the node
                   int last_ind = table_data.lastIndexOf(", ");
                   //if (last_ind != -1)
                     //table_data = table_data.substring(0, last_ind);
                   table_data += " FROM &quot;" + schema.getKey()
                     + "&quot;.&quot;" + table.getKey() + "&quot;\">\n";
                   result.append(table_data);
                   result.append(column_data);
                   result.append("    </" + type.getKey().toLowerCase() + ">\n"); // table
                 }
                 result.append("  </" + type.getKey().toLowerCase() + "s>\n"); // type
               }
               result.append("</schema>\n"); // schema
             }
 
             result.append("</schemas>\n"); // root node
 
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new Exception("Error Info: No expect error!");
         } finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
 
 
             } catch (SQLException ex) {
 
                 throw new Exception("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return result.toString();
     }
     
  public static List<ForeignServer> getForeignServers(String wrapper) throws AppException {
     	
     	List<ForeignServer> retVal = new ArrayList<ForeignServer>();
     	
     	Connection conn = null;
     	PreparedStatement ps;
     	ResultSet rs;
     	
     	 try {
 
              conn = getConnection();
 
              ps = conn.prepareStatement("select FOREIGN_WRAPPER_NAME, foreign_server_name "
             		 + " , remarks"
             		 + " from localdb.sys_root.dba_foreign_servers"
             		 + " where foreign_wrapper_name = ?");
              ps.setString(1, wrapper);
              rs = ps.executeQuery();
 
              while (rs.next()) {
 
                  ForeignServer fs = new ForeignServer();
                  fs.setWrapperName(rs.getString(1));
                  fs.setName(rs.getString(2));
                  fs.setDesc(rs.getString(3));
                  
                  retVal.add(fs);
 
              }
 
          }  catch (SQLException e) {
 
              e.printStackTrace();
              throw new AppException(
                  "Error Info: The connection was bad or Execute sql statment failed!");
          } finally {
            if (conn != null) {
              releaseConnection();
            }
          }
          
          return retVal;
          
     	
     }
     
     public static List<Wrapper> getWrappers() throws AppException {
     	
     	List<Wrapper> retVal = new ArrayList<Wrapper>();
     	
     	Connection conn = null;
     	PreparedStatement ps;
     	ResultSet rs;
     	
     	 try {
 
              conn = getConnection();
 
              ps = conn.prepareStatement("select FOREIGN_WRAPPER_NAME, LIBRARY "
             		 + "FROM localdb.SYS_ROOT.DBA_FOREIGN_WRAPPERS");
              rs = ps.executeQuery();
 
              while (rs.next()) {
 
                  Wrapper w = new Wrapper(rs.getString(1), rs.getString(2));
                  
                  retVal.add(w);
 
              }
 
          }  catch (SQLException e) {
 
              e.printStackTrace();
              throw new AppException(
                  "Error Info: The connection was bad or Execute sql statment failed!");
          } finally {
            if (conn != null) {
              releaseConnection();
            }
          }
          
          return retVal;
          
     	
     }
     
     public static ForeignTable createForeignTable ( ForeignTable ft) throws AppException {
     	
     	ForeignTable returnFT = null;
       Connection conn = null;
     	try {
 			conn = getConnection();
 			
 			StringBuffer sb = new StringBuffer();
 			// create foreign table catalog.schema.table
 			sb.append("CREATE FOREIGN TABLE \"" + ft.getCatalog() + "\"."
 						+ "\"" + ft.getSchema() + "\"."
 						+ "\"" + ft.getName() + "\"" + "\n");
 			sb.append("(\n");
 			
 			sb.append(")\n");
 			sb.append("server " );
 			
 
 		} catch (SQLException e) {
 			throw new AppException();
 		} finally {
       if (conn != null) {
         releaseConnection();
       }
     }
 		
 		return returnFT;
 		
 		
     	
     }
 
     public static String execSQL(
         String connection,
         String sqlquerytype,
         String raw_sql,
         int toomany)
     {
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         // Supports multiple queries by splitting at semicolons.
         List<String> all_results = new ArrayList<String>();
 
         for (String sql : raw_sql.split(";")) {
           sql = sql.trim();
 
         // Result Vars
         String datamap = "";
         String tablename = "";
         String edittable = "true";
         String executiontime = "0";
         String recordcount = "0";
         String datatables = "";
         String errormsg = "";
 
         try {
 
             conn = getConnection();
 
             String mySql = sql.toLowerCase();
             String sqlcmd = "";
             if (mySql.startsWith("select") || mySql.startsWith("explain")) {
                 sqlcmd = "SELECT";
             } else if (mySql.startsWith("insert")) {
                 sqlcmd = "INSERT";
             } else if (mySql.startsWith("update")) {
                 sqlcmd = "UPDATE";
             } else if (mySql.startsWith("delete")) {
                 sqlcmd = "DELETE";
             } else if (mySql.startsWith("call")) {
                 sqlcmd = "CALL";
             } else if (mySql.startsWith("create view")) {
                 sqlcmd = "CREATE VIEW";
             } else if (mySql.startsWith("drop view") || mySql.startsWith("drop table")) {
               sqlcmd = "DROP";
             }
 
 
             // TODO:Find the first operator
 
             StringBuffer result = new StringBuffer("");
             sql = sql.trim();
             if (sql.length() == 0) {
                 throw new Exception("Please submit a query");
             }
             if ("parse".equals(sqlquerytype)) {
                 // TODO:Open parseonly option. I don't know how to do in
                 // lucidDB
                 datamap = "Parse";
                 tablename = "";
                 edittable = "false";
                 executiontime = "0";
                 recordcount = "1";
                 datatables = "<NewDataSet><Table><Parse>The command(s) completed successfully.</Parse></Table></NewDataSet>";
                 // TODO:Close parseonly option. I don't know how to do in
                 // lucidDB
 
             } else if ("showplan".equals(sqlquerytype)) {
 
                 ps = conn.prepareStatement("explain plan including all attributes with implementation for "
                     + sql);
                 rs = ps.executeQuery();
                 tablename = rs.getMetaData().getTableName(1);
 
                 String tmp = "";
                 List<RelNode> nodes = new ArrayList<RelNode>();
                 int idx = 1;
 
                 while (rs.next()) {
 
                     RelNode node = new RelNode();
                     node.setCurrentId(idx);
                     String info = rs.getString(1);
                     if (info.contains(": rowcount =")) {
 
                         if (!tmp.equals(""))
                             info = tmp + info;
                         tmp = "";
 
                     } else {
 
                         tmp = tmp + info;
                         continue;
                     }
 
                     node.setNumOfSpaces((info.length() - (info.trim().length())));
                     ShowPlanEntity entity = new ShowPlanEntity();
                     info = info.trim();
 
                     Pattern pattern = Pattern.compile(REGEX1);
                     Matcher matcher = pattern.matcher(info);
 
                     if (matcher.find()) {
 
                         entity.setPhysicalOp(matcher.group(1));
                         entity.setLogicalOp(matcher.group(1));
                     } else {
 
                         throw new Exception(
                             "Error in showPlan: failed to parse the explain plan line: "
                                 + info);
                     }
 
                     pattern = Pattern.compile(REGEX2);
                     matcher = pattern.matcher(info);
 
                     if (matcher.find()) {
 
                         entity.setEstimateRows(matcher.group(1).trim());
                         entity.setTotalSubtreeCost(matcher.group(2).trim());
 
                     } else {
 
                         throw new Exception(
                             "Error in showPlan: failed to parse the explain plan line: "
                                 + info);
                     }
 
                     entity.setStmtText(info);
                     entity.setStmtId(1);
                     node.setShowPlanEntity(entity);
                     nodes.add(node);
                     idx++;
                 }
 
                 List<RelNode> output = buildRelationship(nodes);
 
                 StringBuffer sb = new StringBuffer();
 
                 sb.append("<showplan>");
 
                 for (RelNode node : output) {
 
                     sb.append("<Table>").append(
                         "<StmtText> <![CDATA[ "
                             + node.getShowPlanEntity().getStmtText()
                             + " ]]></StmtText>").append(
                         "<StmtId>" + node.getShowPlanEntity().getStmtId()
                             + "</StmtId>").append(
                         "<NodeId>" + node.getCurrentId() + "</NodeId>").append(
                         "<Parent>" + node.getParentId() + "</Parent>").append(
                         "<PhysicalOp>"
                             + node.getShowPlanEntity().getPhysicalOp()
                             + "</PhysicalOp>").append(
                         "<LogicalOp>" + node.getShowPlanEntity().getLogicalOp()
                             + "</LogicalOp>").append(
                         "<EstimateRows>"
                             + node.getShowPlanEntity().getEstimateRows()
                             + "</EstimateRows>").append(
                         "<TotalSubtreeCost>"
                             + node.getShowPlanEntity().getTotalSubtreeCost()
                             + "</TotalSubtreeCost>").append(
                         "<level>" + node.getLevel() + "</level>").append(
                         "</Table>");
                 }
                 sb.append("</showplan>");
                 datamap = "StmtText,StmtId,NodeId,Parent,PhysicalOp,LogicalOp,EstimateRows,TotalSubtreeCost";
                 datatables = sb.toString();
 
             } else if ("SELECT".equals(sqlcmd) )
             {
 
                 long start = System.currentTimeMillis();
 
                 ps = conn.prepareStatement(sql);
                 ps.setMaxRows(toomany);
 
                 rs = ps.executeQuery();
                 tablename = rs.getMetaData().getTableName(1);
                 long end = System.currentTimeMillis() - start;
 
                 result.append("<NewDataSet>");
                 String data = JDBCUtil.resultSetToXML(rs);
                 result.append(data);
                 result.append("</NewDataSet>");
 
                 int colCnt = rs.getMetaData().getColumnCount();
 
                 for (int i = 0; i < colCnt; i++) {
 
                     int index = i + 1;
                     datamap += rs.getMetaData().getColumnName(index).replaceAll(" ", "&nbsp;");
                     if (index < colCnt) {
                         datamap += ",";
                     }
                 }
                 executiontime = String.valueOf(end);
                 rs.last();
 
                 recordcount = String.valueOf(rs.getRow());
                 datatables = result.toString();
 
                 // TODO: include showplan when query.
                 if (sqlquerytype == "spnormal") {
 
                 }
 
             } else if ( "CALL".equals(sqlcmd)) {
             	long start = System.currentTimeMillis();
 
                 ps = conn.prepareStatement(sql);
 
 
                 ps.execute();
                 long end = System.currentTimeMillis() - start;
 
                 executiontime = String.valueOf(end);
                 datatables = "<NewDataSet><Table><" + sqlcmd + ">"
                 + "Command excuted successfully </" + sqlcmd
                 + "></Table></NewDataSet>";
             	
             } else if ("CREATE VIEW".equals(sqlcmd)) {
                 long start = System.currentTimeMillis();
                 ps = conn.prepareStatement(sql);
                 ps.execute();
                 long end = System.currentTimeMillis();
                 executiontime = String.valueOf(end - start);
                 datamap = "CREATE_VIEW";
                 datatables = "<NewDataSet><Table><CREATE_VIEW>"
                 + "Command excuted successfully</CREATE_VIEW>"
                 + "</Table></NewDataSet>";
 
             } else if ("DROP".equals(sqlcmd)) {
               long start = System.currentTimeMillis();
               ps = conn.prepareStatement(sql);
               ps.execute();
               long end = System.currentTimeMillis();
               executiontime = String.valueOf(end - start);
               datamap = "DROP";
               datatables = "<NewDataSet><Table><" + sqlcmd + ">"
                 + "Item Dropped Successfully</" + sqlcmd
                 + "></Table></NewDataSet>";
             } else {
 
                 long start = System.currentTimeMillis();
                 ps = conn.prepareStatement(sql);
                 recordcount = String.valueOf(ps.executeUpdate());
                 long end = System.currentTimeMillis() - start;
                 executiontime = String.valueOf(end);
                 datamap = "Query";
                 datatables = "<NewDataSet><Table><Query>"
                     + "Query Executed Successfully, " + 
                     recordcount + " row(s) affected.</Query"
                     + "></Table></NewDataSet>";
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
             errormsg = ex.getMessage();
             datamap = "Error";
             executiontime = "0";
             recordcount = "0";
             datatables = "<NewDataSet><Table><Error> <![CDATA[ Error Executing Query: "
                 + errormsg + "  ]]></Error></Table></NewDataSet>";
 
         } finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                
 
             } catch (SQLException ex) {
                 ex.printStackTrace();
                 errormsg = ex.getMessage();
                 datamap = "Error";
                 executiontime = "";
                 recordcount = "";
                 datatables = "<NewDataSet><Table><Error><![CDATA[Error Executing Query: "
                     + errormsg + "]]></Error></Table></NewDataSet>";
 
             }
         }
 
         all_results.add(formatResult(datamap, tablename, edittable,
               executiontime, recordcount, datatables));
         }
 
         StringBuilder retval = new StringBuilder();
         for (String result : all_results) {
           retval.append(result);
           retval.append("\n");
         }
         return retval.toString();
     }
 
     private static List<RelNode> buildRelationship(List<RelNode> input)
     {
 
         List<RelNode> ret = input;
         RelNode firstNode = null;
 
         for (int i = 0; i < ret.size(); i++) {
 
             RelNode node = ret.get(i);
             if (i == 0) {
 
                 node.setParentId(0);
                 node.setLevel(0);
                 firstNode = node;
 
             } else {
 
                 RelNode test = firstNode;
 
                 while (true) {
 
                     if (test.getChildrenIds() == null) {
 
                         if (test.getNumOfSpaces() < node.getNumOfSpaces()) {
 
                             node.setParentId(test.getCurrentId());
                             test.setChildrenIds(new ArrayList<Integer>());
                             test.getChildrenIds().add(node.getCurrentId());
                             node.setLevel(test.getLevel() + 1);
                             break;
                         }
                     } else {
 
                         int maxChild_id = test.getChildrenIds().get(
                             test.getChildrenIds().size() - 1);
                         RelNode selectedNode = ret.get(maxChild_id - 1);
 
                         if (selectedNode.getNumOfSpaces() < node.getNumOfSpaces())
                         {
 
                             test = selectedNode;
 
                         } else {
 
                             node.setParentId(test.getCurrentId());
                             test.getChildrenIds().add(node.getCurrentId());
                             node.setLevel(test.getLevel() + 1);
 
                             break;
                         }
 
                     }
                 }
 
             }
 
         }
         return ret;
 
     }
 
     public static String handleUpdate(
         String connection,
         String testsql,
         String sql,
         int toomany)
     {
         String result = "";
         String recordcount = "";
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             conn = getConnection();
             ps = conn.prepareStatement(testsql);
             rs = ps.executeQuery();
 
             rs.last();
             recordcount = String.valueOf(rs.getRow());
 
             if (!"1".equals(recordcount)) {
                 throw new Exception(
                     "Command affected more than one row, operation cancelled.");
             } else {
                 result = execSQL(connection, "normal", sql, toomany);
             }
         } catch (Exception ex) {
             String errormsg = ex.getMessage();
             String datatables = "<NewDataSet><Table><Error><![CDATA[Error Executing Query: "
                 + errormsg + "]]></Error></Table></NewDataSet>";
             result = formatResult("Error", "", "", "0", "", datatables);
         } finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
 
 
             } catch (SQLException ex) {
 
                 String errormsg = ex.getMessage();
                 String datatables = "<NewDataSet><Table><Error><![CDATA[Error Executing Query: "
                     + errormsg + "]]></Error></Table></NewDataSet>";
                 result = formatResult("Error", "", "", "0", "", datatables);
 
             }
         }
 
         return result;
     }
 
     private static String formatResult(
         String datamap,
         String tablename,
         String editable,
         String executiontime,
         String recordcount,
         String datatables)
     {
         return "<sqlquery><datamap>" + datamap + "</datamap><tablename>"
             + tablename + "</tablename><edittable>" + editable
             + "</edittable><executiontime>" + executiontime
             + "</executiontime><recordcount>" + recordcount + "</recordcount>"
             + datatables + "</sqlquery>";
     }
 
     public static ResultSet rawResultExec(String query) throws SQLException {
       Connection conn = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
 
       try {
         conn = getConnection();
         if (!connection_catalog.equals("")) {
           ps = conn.prepareStatement("SET CATALOG '" + connection_catalog + "'");
           ps.execute();
         }
         ps = conn.prepareStatement(query);
         ps.setMaxRows(0);
         if (ps.execute()) {
           rs = ps.getResultSet();
         }
       } catch (SQLException ex) {
         throw ex;
       } catch (Exception ex) {
         ex.printStackTrace();
       } finally {
         if (conn != null) {
           releaseConnection();
         }
       }
 
       return rs;
     }
     
 
 
     public static boolean postTableDetails(
         String catalogName,
         String schema,
         String table,
         TableDetails td)
         throws AppException
     {
         TableDetails retVal = td;
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             conn = getConnection();
 
             StringBuffer sb = new StringBuffer();
             sb.append("select count(1) from localdb.sys_root.dba_tables where ");
             sb.append(" TABLE_TYPE = 'LOCAL TABLE'");
             sb.append(" AND catalog_name = ? ");
             sb.append(" AND schema_name = ? ");
             sb.append(" AND table_name = ?");
             ps = conn.prepareStatement(sb.toString());
             ps.setString(1, catalogName);
             ps.setString(2, schema);
             ps.setString(3, table);
             rs = ps.executeQuery();
 
             long isExisting = 0;
             while (rs.next()) {
                 isExisting = rs.getLong(1);
             }
 
             if (isExisting == 0) {
 
                 schema = "\"" + schema + "\"";
                 table = "\"" + table + "\"";
 
                 sb = new StringBuffer();
                 sb.append("create table " + catalogName + "." + schema + "."
                     + table + " ( ");
                 sb.append(createTableSQL(retVal.column));
                 sb.append(" )");
                 ps = conn.prepareStatement(sb.toString());
                 ps.execute();
 
             } else {
 
                 ps = conn.prepareStatement("select dc.lineage_id, dc.column_name, dc.ordinal_position, dc.datatype,"
                     + "dc.\"PRECISION\", dc.dec_digits, dc.is_nullable, dc.remarks, dcs.distinct_value_count, dcs.is_distinct_value_count_estimated, "
                     + "dcs.last_analyze_time "
                     + "from localdb.sys_root.dba_columns dc left join localdb.sys_root.dba_column_stats dcs on dc.table_name "
                     + " = dcs.table_name and dc.schema_name = dcs.schema_name and dc.catalog_name = dcs.catalog_name "
                     + "and dc.column_name = dcs.column_name "
                     + "where dc.catalog_name = ? and dc.schema_name = ? "
                     + " and dc.table_name = ? order by  dc.ordinal_position");
                 ps.setString(1, catalogName);
                 ps.setString(2, schema);
                 ps.setString(3, table);
 
                 rs = ps.executeQuery();
 
                 if (schema.toLowerCase().equals(schema))
                   schema = "\"" + schema + "\"";
                 if (table.toLowerCase().equals(table))
                   table = "\"" + table + "\"";
 
                 List<Integer> index = new ArrayList<Integer>();
 
                 List<String> colNameFromDb = new ArrayList<String>();
 
                 while (rs.next()) {
 
                     String colName = rs.getString(2);
 
                     int matchCode = -1;
                     for (int i = 0; i < retVal.column.size(); i++) {
                         if (retVal.column.get(i).name.equals(colName)) {
                             matchCode = i;
                             break;
                         }
                     }
 
                     if (matchCode == -1) {
                         colNameFromDb.add(colName);
                     } else {
                         index.add(matchCode);
                     }
 
                 }
 
                 if (colNameFromDb.size() != 0) {
 
                     throw new AppException("Cannot remove columns");
                 }
 
                 List<Column> cols = retVal.column;
                 int sizeOfCols = cols.size();
 
                 for (int i = 0; i < sizeOfCols; i++) {
 
                     boolean match = false;
 
                     for (int j = 0; j < index.size(); j++) {
                         if (i == j) {
                             match = true;
                             break;
                         }
                     }
                     if (!match) {
 
                         ps = conn.prepareStatement("alter table " + catalogName
                             + "." + schema + "." + table + " add column "
                             + createColumnSQL(cols.get(i)));
                         ps.execute();
                     }
                 }
             }
 
         }  catch (SQLException e) {
 
             e.printStackTrace();
             throw new AppException(
                 "Error Info: The connection was bad or Execute sql statment failed!");
         }  finally {
 
             try {
 
                 if (conn != null) {
                   releaseConnection();
                 }
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
 
             } catch (SQLException ex) {
 
                 throw new AppException("Error Info: Release db resouce failed");
 
             }
 
         }
 
         return true;
     }
 
     private static String createTableSQL(List<Column> cols)
     {
 
         StringBuffer ret = new StringBuffer();
 
         int size = cols.size();
 
         int last_pos = size - 1;
         for (int i = 0; i < size; i++) {
 
             if (i == last_pos) {
 
                 ret.append(createColumnSQL(cols.get(i)));
 
             } else {
                 ret.append(createColumnSQL(cols.get(i)));
                 ret.append(",");
             }
         }
 
         return ret.toString();
     }
     
 
 
     private static String createColumnSQL(Column col)
     {
 
         StringBuffer ret = new StringBuffer();
         String dataType = col.datatype.toUpperCase();
 
         if (dataType.indexOf(Constants.CHAR_TYPE) != -1) {
 
             ret.append(col.name + " " + col.datatype + "(" + col.precision
                 + ")");
 
         } else if ((dataType.indexOf(Constants.DECIMAL_TYPE) != -1)
             || (dataType.indexOf(Constants.NUMERIC_TYPE) != -1))
         {
 
             ret.append(col.name + " " + col.datatype + "(" + col.precision
                 + ")");
 
         } else if (dataType.indexOf(Constants.BINARY_TYPE) != -1) {
 
             ret.append(col.name + " " + col.datatype + "(" + col.precision
                 + ")");
 
         } else {
 
             ret.append(col.name + " " + col.datatype);
         }
 
         if (col.default_value != null && !col.default_value.equals("")) {
           if (!isNumericType(col.datatype))
             ret.append(" DEFAULT '" + col.default_value + "'");
           else
             ret.append(" DEFAULT " + col.default_value);
         }
 
         if (!col.is_nullable) {
             ret.append(" not null");
         }
 
         return ret.toString();
 
     }
 
     private static boolean isNumericType(String type) {
       type = type.toUpperCase();
       if (type.indexOf("INT") != -1 || type.indexOf("DEC") != -1 ||
           type.indexOf("NUM") != -1 || type.indexOf("FLOAT") != -1 ||
           type.indexOf("REAL") != -1 || type.indexOf("DOUBLE") != -1) {
         return true;
       }
 
       return false;
     }
 
     private static String htmlEntities(String str) {
       String escaped = "";
       for (int i = 0; i < str.length(); i++) {
         String c = Character.toString(str.charAt(i));
         if (c.equals("<"))
           escaped += "&lt;";
         else if (c.equals(">"))
           escaped += "&gt;";
         else if (c.equals("\""))
           escaped += "&quot;";
         else if (c.equals("'"))
           escaped += "&apos;";
         else if (c.equals("&"))
           escaped += "&amp;";
         else
           escaped += c;
       }
       return escaped;
     }
     
     
 
 }
