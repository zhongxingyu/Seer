 package org.mattyo161.isql;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import java.sql.*;
 
 import javax.naming.*;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 import javax.servlet.*;
 
 import org.mattyo161.commons.cal.Cal;
 import org.mattyo161.commons.db.*;
 import org.mattyo161.commons.util.MyEnvironment;
 
 import com.sun.msv.util.Uri;
 
 @WebServlet("/Isql")
 public class Isql extends HttpServlet {
     
     /** Holds value of property publication. */
     private String publication;    
     private int tableCount = 0;
     private String nullString = "";
     private boolean showRowCount = false;
     
 
     public Isql() {
         super();
         // TODO Auto-generated constructor stub
         showRowCount = false;
         nullString = "";
         tableCount = 0;
     }
      public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
         throws IOException, ServletException
     {
          MyEnvironment env = MyEnvironment.getEnvironment();
          // find available Sources
          ArrayList envList = env.getKeys();
          Set sources = new TreeSet();
          for (Iterator i = envList.iterator(); i.hasNext(); ) {
              String currValue = (String) i.next();
              if (currValue.endsWith("_serverType")) {
                  sources.add(currValue.substring(0,currValue.indexOf("_serverType")));
              } else if (currValue.endsWith("_dburl")) {
                  sources.add(currValue.substring(0,currValue.indexOf("_dburl")));
              }
          }
          
          // Extract values from request
          String reqSource = request.getParameter("source");
          String reqType = request.getParameter("type");
          String reqServer = request.getParameter("server");
          String reqPort = request.getParameter("port");
          String reqUser = request.getParameter("user");
          String reqPassword = request.getParameter("password");
          String reqDb = request.getParameter("db");
          String reqSql = request.getParameter("sql");
          String reqDbUrl = request.getParameter("dburl");
          String reqFormat = request.getParameter("format");
          int reqRecStart = 1;
          int reqRecCount = 50;
          
          if (reqSource == null) reqSource = "";
          if (reqType == null) reqType = "";
          if (reqServer == null) reqServer = "";
          if (reqPort == null) reqPort = "";
          if (reqUser == null) reqUser = "";
          if (reqPassword == null) reqPassword = "";
          if (reqDb == null) reqDb = "";
          if (reqSql == null) reqSql = "";
          if (reqDbUrl == null) reqDbUrl = "";
          if (reqFormat == null) reqFormat = "";
          if (request.getParameter("recstart") != null) {
              reqRecStart = Integer.parseInt(request.getParameter("recstart"));
          }
          if (request.getParameter("reccount") != null) {
              reqRecCount = Integer.parseInt(request.getParameter("reccount"));
          }
          
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
         
         //print the HTML header </font>
         StringBuffer head = new StringBuffer();
         head.append("<head>")
         	.append("<title>Isql Tester</title>")
         	//.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/plugins/css/bootstrap.min.css\" />")
         	//.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/plugins/css/bootstrap-responsive.min.css\" />")
         	.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/css/styles.css\" />")
         	.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/plugins/datatables/css/jquery.dataTables.css\" />")
         	.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/plugins/scroller/css/dataTables.scroller.css\" />")
         	.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/plugins/tabletools/css/TableTools.css\" />")
         	.append("<script src=\"" + request.getContextPath() + "/plugins/js/modernizr-2.6.2-respond-1.1.0.min.js\"></script>")
         	.append("<script src=\"http://code.jquery.com/jquery-1.9.1.min.js\"></script>")
         	.append("<script src=\"http://code.jquery.com/jquery-migrate-1.1.1.min.js\"></script>")
         	.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/css/styles.css\" />")
         	.append("</head>")
         	;
         out.println("<html>");
         if (reqFormat.equals("")) {
         	this.nullString = "&lt;NULL&gt;";
         	this.showRowCount = true;
         	out.println(head.toString());
             out.println("<body bgcolor=\"white\">");
         } else {
         	out.println("<head></head>");
             out.println("<body>");
         }
         
 
         
         // If reqSource is not empty then we will setup this source and load the values
         // into the variables, it will also disable any of the other choices the user can
         // set the source to blank if they want to make modifications.
         DBConnection conn = null;
         String inputDisabled = "";
         
         if (!reqSource.equals("")) {
             if (env.get(reqSource + "_serverType") != null) {
                 reqType = env.get(reqSource + "_serverType");
                 reqServer = env.get(reqSource + "_server");
                 reqPort = env.get(reqSource + "_port");
                 reqUser = env.get(reqSource + "_user");
                 reqPassword = env.get(reqSource + "_password");
                 reqDb = env.get(reqSource + "_db");
                 inputDisabled = " readonly";
             }
             if (env.get(reqSource + "_dburl") != null) {
                 reqDbUrl = env.get(reqSource + "_dburl");
             }
         }
         
         // Lets create a basic form here.
         int leftWidth = 30;
 	    if (reqFormat.equals("")) {
 	        out.println("<form name=\"isql\" action=\"\" method=\"post\">");
 	        out.println("<table border=\"0\" width=\"90%\">");
 	            out.println("<tr>");
 	                out.println("<th width=\"" + leftWidth + "\" align=\"right\">Source:</th>");
 	                out.println("<td width=\"" + leftWidth + "\">");
 	                    out.println("<select name=\"source\">");
 	                        if (reqSource.equals("")) {
 	                             out.println("<option selected></option>");
 	                        } else {
 	                            out.println("<option></option>");
 	                        }
 	                        for (Iterator i = sources.iterator(); i.hasNext(); ) {
 	                            String currValue = (String) i.next();
 	                            if (currValue.equals(reqSource)) {
 	                                out.println("<option selected>" + currValue + "</option>");
 	                            } else {
 	                                out.println("<option>" + currValue + "</option>");
 	                            }
 	                        }
 	                    out.println("</select>");
 	                out.println("<td>");
 	            out.println("</tr>");
 	            out.println("<tr>");
 	                out.println("<th align=\"right\">DBUrl:</th>");
 	                out.println("<td colspan=\"5\" width=\"100%\"><input type=\"text\" name=\"dburl\" size=\"90%\" value=\"" + reqDbUrl + "\" /></td>");
 	            out.println("</tr>");
 	//            out.println("<!-- ");
 	//            out.println("<tr>");
 	//                out.println("<th>Type:</th>");
 	//                out.println("<td>");
 	//                    out.println("<select name=\"type\"" + inputDisabled + ">");
 	//                        String[] dbTypes = new String("mssql65,mssql7,mssql2000,mysql,sybase").split(",");
 	//                        for (int x = 0; x < dbTypes.length; x++) {
 	//                            if (reqType.equals(dbTypes[x])) {
 	//                                out.println("<option selected>" + dbTypes[x] + "</option>");
 	//                            } else {
 	//                                out.println("<option>" + dbTypes[x] + "</option>");
 	//                            }
 	//                        }
 	//                        out.println("");
 	//                    out.println("</select>");
 	//                out.println("</td>");
 	//                out.println("<th>Server:</th>");
 	//                out.println("<td><input type=\"text\" size=\"20\" name=\"server\" value=\"" + reqServer + "\"" + inputDisabled + ">");
 	//                out.println("<th>Port:</th>");
 	//                out.println("<td><input type=\"text\" size=\"5\" name=\"port\" value=\"" + reqPort + "\"" + inputDisabled + ">");
 	//            out.println("</tr>");
 	//            out.println("<tr>");
 	//                out.println("<th>User:</th>");
 	//                out.println("<td><input type=\"text\" size=\"10\" name=\"user\" value=\"" + reqUser + "\"" + inputDisabled + ">");
 	//                out.println("<th>Password:</th>");
 	//                out.println("<td><input type=\"text\" size=\"10\" name=\"password\" value=\"" + reqPassword + "\"" + inputDisabled + ">");
 	//                out.println("<th>Database:</th>");
 	//                out.println("<td><input type=\"text\" size=\"20\" name=\"db\" value=\"" + reqDb + "\"" + inputDisabled + ">");
 	//            out.println("</tr>");
 	//            out.println("-->");
 	            out.println("<tr>");
 	                out.println("<td valign=\"top\" align=\"right\"><b>SQL:</b><br /><a href=\"IsqlDocs.html\" target=\"IsqlDocs\">[Docs]</a></td>");
 	                out.println("<td colspan=\"5\">");
 	                    out.print("<textarea name=\"sql\" cols=\"80%\" rows=\"6\">");
 	                    out.print(reqSql);
 	                    out.println("</textarea>");
 	                out.println("</td>");
 	            out.println("</tr>");
 	            out.println("<tr>");
 	                out.println("<th width=\"" + leftWidth + "\">RecStart:</th>");
 	                out.println("<td width=\"" + leftWidth + "\"><input type=\"text\" size=\"10\" name=\"recstart\" value=\"" + reqRecStart + "\">");
 	                out.println("<th width=\"" + leftWidth + "\">RecCount:</th>");
 	                out.println("<td width=\"" + leftWidth + "\"><input type=\"text\" size=\"10\" name=\"reccount\" value=\"" + reqRecCount + "\">");
 	                out.println("<td width=\"" + leftWidth + "\">&nbsp</td>");
 	                out.println("<td width=\"" + leftWidth + "\">&nbsp</td>");
 	            out.println("</tr>");
 	            out.println("<tr><td colspan=\"6\"><center>");
 	                out.println("<input type=\"submit\">");
 	            out.println("</center></td></tr>");
 	         out.println("</table>");
 	        out.println("</form>");
 	        out.println("<hr>");
 	    }
         
         
             try {
                 if (!reqDbUrl.equals("")) {
                     //conn = ConnectionFactory.getConnection(reqDbUrl);
                     conn = new DBConnection(reqDbUrl);
                 } else if (!(reqType.equals("") || reqServer.equals("") || reqUser.equals(""))) {
                     //conn = ConnectionFactory.getConnection(reqType,
                     //        reqServer,
                     //        reqPort,
                     //        reqUser,
                     //        reqPassword,
                     //        reqDb);
                     conn = new DBConnection(reqType,
                             reqServer,
                             reqPort,
                             reqUser,
                             reqPassword,
                             reqDb);
                 } else {
                     out.println("<b>ERROR:</b> Missing Parameters<br>");
                     out.println("ServerType, Server and User must be entered in order to establish a connection.<br>");
                 }
                 if (conn != null) {
                     Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                   ResultSet.CONCUR_READ_ONLY);
                     try {
                     	stmt.setFetchSize(reqRecCount);
                     	stmt.setMaxRows(reqRecCount + 10);
                     } catch (Exception e) {
                     	System.out.println(e);
                     }
                     stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
                     if (!reqSql.equals("") && reqSql != null) {
                         String[] sqlCmds = reqSql.split(";\\s*([\\n\\r]|$)");
                         for (int line = 0; line < sqlCmds.length; line++) {
                         	Cal startSQL = new Cal();
                             reqSql = sqlCmds[line].trim();
                             out.println("<!-- " + reqSql + " -->");
                             
                             // lets see if we are working with a list
                             // the format is list <type> <srch>, <srch>, <srch>, <srch>
                             Matcher match = Pattern.compile("^list\\s+(\\w+)\\s*(.*)$",Pattern.CASE_INSENSITIVE).matcher(reqSql);
                             if (match.find()) {
                                 String listCmd = match.group(1).trim().toLowerCase();
                                 String listParams = match.group(2).trim();
                                 String[] listParamItems;
                                 ArrayList opts = new ArrayList();
                                 if (!listParams.equals("")) {
                                     listParamItems = listParams.split("\\s*,\\s*");
                                     for (int i = 0; i < listParamItems.length; i++) {
                                         if (listParamItems[i].trim().equalsIgnoreCase("null")) {
                                             opts.add(null);
                                         } else {
                                             opts.add(listParamItems[i].trim());
                                         }
                                     }
                                 }
                                 // Just incase there are not enough opts, fill the rest with nulls
                                 opts.add(null);
                                 opts.add(null);
                                 opts.add(null);
                                 opts.add(null);
                                 opts.add(null);
                                 opts.add(null);
                                 DatabaseMetaData dbMeta = conn.getMetaData();
                                 ResultSet rs = null;
                                 
                                 if (listCmd.startsWith("db")) {
                                     rs = dbMeta.getCatalogs();
                                 } else if (listCmd.startsWith("attr")) {
                                     rs = dbMeta.getAttributes((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), (String) opts.get(3));
                                 } else if (listCmd.startsWith("cat")) {
                                     rs = dbMeta.getCatalogs();
                                 } else if (listCmd.startsWith("bestrow")) {
                                     if (opts.get(3) == null) {
                                         opts.set(3, new Integer(DatabaseMetaData.bestRowSession));
                                     } else {
                                         // try to turn this into an integer
                                         try {
                                             opts.set(3, new Integer(Integer.parseInt( (String) opts.get(3) )));
                                         } catch (Exception e) {
                                             opts.set(3, new Integer(DatabaseMetaData.bestRowSession));
                                         }
                                     }
 
                                     if (opts.get(4) == null) {
                                         opts.set(4, new Boolean(true));
                                     } else {
                                         // try to turn this into an integer
                                         try {
                                             opts.set(4, new Boolean((String) opts.get(4)));
                                         } catch (Exception e) {
                                             opts.set(4, new Boolean(true));
                                         }
                                     }
                                     rs = dbMeta.getBestRowIdentifier((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), 
                                             	((Integer) opts.get(3)).intValue(), ((Boolean) opts.get(4)).booleanValue());
                                 } else if (listCmd.startsWith("columnpriv")) {
                                     rs = dbMeta.getColumnPrivileges((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), (String) opts.get(3));
                                 } else if (listCmd.startsWith("col")) {
                                     rs = dbMeta.getColumns((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), (String) opts.get(3));
                                 } else if (listCmd.startsWith("cross")) {
                                     rs = dbMeta.getCrossReference((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), 
                                             (String) opts.get(3), (String) opts.get(4), (String) opts.get(5));
                                 } else if (listCmd.startsWith("exp")) {
 	                                rs = dbMeta.getExportedKeys((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("imp")) {
 	                                rs = dbMeta.getImportedKeys((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("index")) {
                                     if (opts.get(3) == null) {
                                         opts.set(3, new Boolean(false));
                                     } else {
                                         // try to turn this into an integer
                                         try {
                                             opts.set(3, new Boolean((String) opts.get(3)));
                                         } catch (Exception e) {
                                             opts.set(3, new Boolean(false));
                                         }
                                     }
 
                                     if (opts.get(4) == null) {
                                         opts.set(4, new Boolean(true));
                                     } else {
                                         // try to turn this into an integer
                                         try {
                                             opts.set(4, new Boolean((String) opts.get(4)));
                                         } catch (Exception e) {
                                             opts.set(4, new Boolean(true));
                                         }
                                     }
                                     rs = dbMeta.getIndexInfo((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), 
                                             	((Boolean) opts.get(3)).booleanValue(), ((Boolean) opts.get(4)).booleanValue());
                                 } else if (listCmd.startsWith("procedurecolumns")) {
                                     rs = dbMeta. getProcedureColumns((String) opts.get(1), (String) opts.get(1), (String) opts.get(2), (String) opts.get(3));
                                 } else if (listCmd.startsWith("prim")) {
 	                                rs = dbMeta.getPrimaryKeys((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("proc")) {
                                     rs = dbMeta.getProcedures((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("schem")) {
                                     rs = dbMeta.getSchemas();
                                 } else if (listCmd.startsWith("supertypes")) {
                                     rs = dbMeta.getSuperTypes((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("supertables")) {
                                     rs = dbMeta.getSuperTables((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("tabletypes")) {
                                     rs = dbMeta.getTableTypes();
                                 } else if (listCmd.startsWith("tablepriv")) {
                                     rs = dbMeta.getTablePrivileges((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("table")) {
 	                                rs = dbMeta.getTables((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), null);
                                 } else if (listCmd.startsWith("type")) {
 	                                rs = dbMeta.getTypeInfo();
                                 } else if (listCmd.startsWith("ver")) {
 	                                rs = dbMeta.getVersionColumns((String) opts.get(0), (String) opts.get(1), (String) opts.get(2));
                                 } else if (listCmd.startsWith("udt")) {
 	                                rs = dbMeta.getUDTs((String) opts.get(0), (String) opts.get(1), (String) opts.get(2), null);
                                 }
                                 if (rs != null) {
                                     printResultSet(rs,reqRecCount, out);
                                 }
                             } else {
                                 if (reqSql.startsWith("tabledef")) {
 	                                String tableName = reqSql.substring(9);
 	                                out.println("<table class=\"datatable\" width=\"100%\"><tr><td width=\"100%\">");
 	                                out.println("Table Definition for '" + tableName + "'");
 	                                out.println("<pre>" + ((DBConnection)conn).getTableDef(null, tableName) + "</pre>");
 	                                out.println("</td></tr></table>");
 	                             } else if (reqSql.startsWith("fieldinfo")) {
 	                                String tableName = reqSql.substring(10);
 	                                org.mattyo161.commons.util.DataMap fieldInfo = ((DBConnection)conn).getFieldDataMap("citest", tableName);
 	                                Set keys = fieldInfo.getKeys();
 	                                out.println("<table class=\"datatable\" width=\"100%\"><tr><td width=\"100%\">");
 	                                out.println("Field Definition for '" + tableName + "'");
 	                                out.println("<pre>" + fieldInfo + "</pre>");
 	                                out.println("</td></tr></table>");
 	                            } else {
 	                                ResultSet rs = null;
 	                                if (false) {
 	                                	rs = stmt.executeQuery(reqSql);
 	                                    //rs.setFetchSize(reqRecCount);
 	                                    /* there needs to be a better way to do this
 	                                    rs.last();
 	                                    int numRows = rs.getRow();
 	                                     */
 	                                    int numRows = 0;
 	                                    int maxRows = reqRecCount;
 	                                    try {
 	                                        if (reqRecStart > 1) {
 	                                        rs.relative(reqRecStart - 1);
 	                                        }
 	                                    } catch (Exception e) {
 	                                        System.out.println(e.toString());
 	                                    //    rs.beforeFirst();
 	                                    }
 	                                    if (reqFormat.equals("")) {
 		                                    out.println("<p id=\"query\">" + reqSql + "</p>");
		                                    out.println("<p id=\"query-escape\">" + Uri.escapeDisallowedChars(reqSql) + "</p>");
 		                                    out.println("NumRows = " + NumberFormat.getInstance().format(numRows) + "</ br>");
 		                                    out.println("MaxRows = " + NumberFormat.getInstance().format(stmt.getMaxRows()) + "</ br>");
 		                                    // removed because it was causing sybase driver to hang not sure why but don't need.
 		                                    //out.println("RSHoldability = " + stmt.getResultSetHoldability() + "</ br>");
 		                                   out.println("RSConcurency = " + stmt.getResultSetConcurrency() + "</ br>");
 		                                   out.println("RSType = " + stmt.getResultSetType() + "</ br>");
 	                                    }
 	                                    printResultSet(rs, reqRecCount, out);
 	                                    rs.close();
 	                                } else {
 	                                    if (reqFormat.equals("")) {
 		                                    out.println("<p id=\"query\">" + reqSql + "</p>");
		                                    out.println("<p id=\"query-escape\">" + Uri.escapeDisallowedChars(reqSql) + "</p>");
 	                                    }
 	                                    // make sure that the sql begins with readonly code like select or sp_help
 	                                    if (Pattern.compile("^(select|sp_help)",Pattern.CASE_INSENSITIVE).matcher(reqSql).find()) {
 		                                    boolean moreResults = stmt.execute(reqSql);
 		                                    if (!moreResults) {
 		                                        moreResults = stmt.getMoreResults();
 		                                    }
 		                                    if (reqFormat.equals("")) {
 			                                    out.println("MaxRows = " + NumberFormat.getInstance().format(stmt.getMaxRows()) + "</ br>");
 			                                    // removed because it was causing sybase driver to hang not sure why but don't need.
 			                                    //out.println("RSHoldability = " + stmt.getResultSetHoldability() + "</ br>");
 			                                   out.println("RSConcurency = " + stmt.getResultSetConcurrency() + "</ br>");
 			                                   out.println("RSType = " + stmt.getResultSetType() + "</ br>");
 		                                    }
 		                                    while (moreResults) {
 		                                        rs = stmt.getResultSet();
 		                                        printResultSet(rs,reqRecCount, out);
 		                                        moreResults = stmt.getMoreResults();
 		                                    }
 	                                    } else {
 	                                        out.println("ERROR: this is not a valid Read-Only SQL statement, the statement must begin with select or sp_help");
 	                                    }
 	                                }
 	                            }
                             }
                             if (reqFormat.equals("")) {
                             	out.println("<pre>Query processed in " + (new Cal().diff(startSQL)) + " ms</pre>");
                             }
                         }
                     } else {
                         if (reqFormat.equals("")) {
                         	out.println("<p><center>&lt;sql statement is empty no data to return&gt;</center></p>");
                         }
                     }
                     stmt.close();
                 }
 
             } catch (SQLException eSql) {
                 out.println(eSql.toString());
                 System.out.println(eSql.toString());
                 eSql.printStackTrace();
             } catch (Exception e) {
                 out.println(e.toString());
                 System.out.println(e.toString());
                 e.printStackTrace();
             }
             if (conn != null) {
                 try {
                     DatabaseMetaData dbMeta = conn.getMetaData();
                     if (reqFormat.equals("")) {
 	                    out.println("<hr>");
 	                    out.println("<b>DataBase metaData</b><br>");
 	                    out.println("DriverName = <i>" + dbMeta.getDriverName() + "</i><br>");
 	                    out.println("DriverVersion = <i>" + dbMeta.getDriverVersion() + "</i><br>");
 	                    out.println("DatabaseProductName = <i>" + dbMeta.getDatabaseProductName() + "</i><br>");
 	                    out.println("DatabaseProductVersion = <i>" + dbMeta.getDatabaseProductVersion() + "</i><br>");
 	                    out.println("JDBC Driver = <i>" + conn.getJdbcDriver() + "</i><br>");
 	                    out.println("JDBC URL = <i>" + conn.getJdbcUrl() + "</i><br>");
                     }
                 } catch (Exception e) {
                     out.println(e.toString());
                     System.out.println(e.toString());
                     e.printStackTrace();
                 }
             }
 
         
              if (conn != null) {
                  System.out.println("Disconnecting...");
                 try {
                     conn.close();
                 } catch (SQLException eSql) {
                     out.println(eSql.toString());
                     System.out.println(eSql.toString());
                     eSql.printStackTrace();
                 }
             }
        
         StringBuffer dataTables = new StringBuffer();
         dataTables
         	.append("<script type=\"text/javascript\" src=\"" + request.getContextPath() + "/plugins/datatables/js/jquery.dataTables.min.js\"></script>")
 	        .append("<script type=\"text/javascript\" src=\"" + request.getContextPath() + "/plugins/tabletools/js/ZeroClipboard.js\"></script>")
 	        .append("<script type=\"text/javascript\" src=\"" + request.getContextPath() + "/plugins/tabletools/js/TableTools.js\"></script>")
 	        .append("<script type=\"text/javascript\" src=\"" + request.getContextPath() + "/plugins/scroller/js/dataTables.scroller.js\"></script>")
         	;
         if (true) {
         	dataTables
 	        	.append("		<script type=\"text/javascript\" charset=\"utf-8\">\n")
 	        	.append("			$(document).ready( function () {\n")
 	        	.append("				$('.datatable').dataTable( {\n")
 	        	.append("					\"sDom\": \"<'row-fluid'<'span6'T><'span6'f>r>tS<'row-fluid'<'span5'i><'span2 center'><'span5'>>\",\n")
 	        	.append("					\"sScrollY\": \"500px\",\n")
 	        	.append("					\"bDeferRender\": true,\n")
 	        	.append("					\"aaSorting\": [],\n")
 	        	.append("					\"oTableTools\": {\n")
 	        	.append("						\"sSwfPath\": \"").append(request.getContextPath()).append("/plugins/tabletools/swf/copy_csv_xls_pdf.swf\",\n")
 	        	.append("						\"aButtons\": [\n")
 	        	.append("							\"copy\",\n")
 	        	.append("							\"print\",\n")
 	        	.append("							{\n")
 	        	.append("								\"sExtends\":    \"collection\",\n")
 	        	.append("								\"sButtonText\": 'Save <span class=\"caret\" />',\n")
 	        	.append("								\"aButtons\":    [ \"csv\", \"xls\", \"pdf\" ]\n")
 	        	.append("							}\n")
 	        	.append("						]\n")
 	        	.append("					}\n")
 	        	.append("				} );\n")
 	        	.append("			} );\n")
 	        	.append("		</script>\n")
 	        	.append("\n");
         	// Add the copy pdf.swf file so that it loads, for some reason it does not work unless it is loaded.
         	
         }
         if (false) {
 	     	dataTables.append("<script type=\"text/javascript\" charset=\"utf-8\">\n")
 	     	.append("$(document).ready(function() {\n")
 	     		.append("$('.datatable').dataTable( {\n")
 		     		.append("\"oTableTools\": {\n")
 		     			.append("\"sSwfPath\": \"" + request.getContextPath() + "/plugins/tabletools/swf/copy_csv_xls_pdf.swf\",\n")
 		     		.append("}\n")
 		     	.append("} );")
 	     	.append("} );\n")
 	     	.append("</script>\n");
         }
         if (reqFormat.equals("")) {
         	out.println(dataTables.toString());
         }
         out.println("</body>");
         out.println("</html>");
         
     }
    private ArrayList getDbs(Connection conn) {
         ArrayList dbNames = new ArrayList();
         try {
             DatabaseMetaData dbMeta = conn.getMetaData();
             ResultSet rs = dbMeta.getCatalogs();
             while (rs.next()) {
                 dbNames.add(rs.getString("TABLE_CAT"));
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return dbNames;
     }
     
      private void printResultSet(ResultSet rs, int maxRows, PrintWriter out) {
     	this.tableCount++;
         ResultSetMetaData rsmd = null;
         try {
             rsmd = rs.getMetaData();
            out.println("<table id=\"isql-table-" + this.tableCount + "\" class=\"datatable\" border=\"1\">");
             int currRow = 0;
             int numCols = rsmd.getColumnCount();
             out.print("<thead><tr>");
             if (this.showRowCount) {
             	out.print("<th>Row#</th>");
             }
             for (int currCol = 1; currCol <= numCols; currCol++) {
                 out.print("<th>" + rsmd.getColumnName(currCol) + "</th>");
             }
             out.println("</tr></thead><tbody>");
             String currValue = null;
             while (rs.next() && currRow++ < maxRows)
             {
                 out.print("<tr>");
                 if (this.showRowCount) {
                 	out.print("<td>" + rs.getRow() + "</td>");
                 }
                 for (int currCol = 1; currCol <= numCols; currCol++) {
                     currValue = rs.getString(currCol);
                     if (currValue == null) {
                         currValue = this.nullString;
                     } else {
                         currValue = currValue.toString().trim().replaceAll("&","&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                     }
                     if (rs.wasNull()) {
                         currValue = this.nullString;
                     } else if (currValue.equals("")) {
                         currValue = "&#160;";
                     }
                     out.print("<td>" + currValue + "</td>");
                 }
                 out.println("</tr>");
             }
             out.println("</tbody></table>");
         } catch (Exception e) {
             out.println(e.toString());
             System.out.println("ERROR: in printResultSet");
             e.printStackTrace();
         }
         
          
      }
      
     public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
         throws IOException, ServletException
     {
         doGet(request, response);
     }    
     
     /** Getter for property publication.
      * @return Value of property publication.
      *
      */
     public String getPublication() {
         return this.publication;
     }
     
     /** Setter for property publication.
      * @param publication New value of property publication.
      *
      */
     public void setPublication(String publication) {
         this.publication = publication;
     }
     
 }
