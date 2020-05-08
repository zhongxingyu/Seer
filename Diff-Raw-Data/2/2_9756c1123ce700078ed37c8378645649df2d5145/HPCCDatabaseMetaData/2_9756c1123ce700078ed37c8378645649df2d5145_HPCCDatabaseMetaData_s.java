 /*##############################################################################
 
 Copyright (C) 2011 HPCC Systems.
 
 All rights reserved. This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 ############################################################################## */
 
 package org.hpccsystems.jdbcdriver;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.RowIdLifetime;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.hpccsystems.jdbcdriver.HPCCJDBCUtils.EclTypes;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author rpastrana
  */
 
 public class HPCCDatabaseMetaData implements DatabaseMetaData
 {
     private HPCCQueries                 eclqueries;
     private HPCCLogicalFiles            dfufiles;
     private List<String>                targetclusters;
     private List<String>                querysets;
 
     public static final short           JDBCVerMajor             = 3;
     public static final short           JDBCVerMinor             = 0;
 
     private static String               HPCCBuildVersionFull     = "";
     @SuppressWarnings("unused")
     private static String               HPCCBuildType            = "";
     private static short                HPCCBuildMajor           = 0;
     private static float                HPCCBuildMinor           = 0;
 
     private boolean                     isHPCCMetaDataCached     = false;
     private boolean                     isDFUMetaDataCached      = false;
     private boolean                     isQuerySetMetaDataCached = false;
 
     private String                      serverAddress;
     private String                      targetcluster;
     private String                      queryset;
     private String                      basewseclwatchurl;
     private String                      basicAuth;
     private String                      UserName;
     private boolean                     lazyLoad;
     private int                         pageSize;
     private int                         pageOffset;
     private int                         connectTimoutMillis;
     private int                         readTimoutMillis;
 
     private DocumentBuilderFactory      dbf;
 
     final static String                 PROCEDURE_NAME           = "PROCEDURE_NAME";
     final static String                 TABLE_NAME               = "TABLE_NAME";
 
     private static final String         FILEFETCHTYPE_ALL             = "Logical Files and Superfiles";
     private static final String         FILEFETCHTYPE_LOGICAL_ONLY    = "Logical Files Only";
     private static final String         FILEFETCHTYPE_SUPER_ONLY      = "Superfiles Only";
     private static final String         FILEFETCHTYPE_NOT_IN_SUPER    = "Not in Superfiles";
 
     public HPCCDatabaseMetaData(Properties props)
     {
         super();
         this.serverAddress = props.getProperty("ServerAddress", HPCCDriver.SERVERADDRESSDEFAULT);
         this.targetcluster = props.getProperty("TargetCluster", HPCCDriver.CLUSTERDEFAULT);
         this.queryset = props.getProperty("QuerySet", HPCCDriver.QUERYSETDEFAULT);
 
         this.basewseclwatchurl = props.getProperty("WsECLWatchAddress", this.serverAddress) + ":" +
                              props.getProperty("WsECLWatchPort", HPCCDriver.WSECLWATCHPORTDEFAULT);
         this.UserName = props.getProperty("username", "");
         this.basicAuth = props.getProperty("BasicAuth",
                 HPCCConnection.createBasicAuth(this.UserName, props.getProperty("password", "")));
         this.lazyLoad = Boolean.parseBoolean(props.getProperty("LazyLoad", HPCCDriver.LAZYLOADDEFAULT));
         this.pageSize = HPCCJDBCUtils.stringToInt(
                 props.getProperty("PageSize"),
                 Integer.valueOf(HPCCDriver.FETCHPAGESIZEDEFAULT));
         this.pageOffset = HPCCJDBCUtils.stringToInt(
                 props.getProperty("PageOffset"),
                 Integer.valueOf(HPCCDriver.FETCHPAGEOFFSETDEFAULT));
         this.connectTimoutMillis = HPCCJDBCUtils.stringToInt(props.getProperty("ConnectTimeoutMilli"),
                 Integer.valueOf(HPCCDriver.CONNECTTIMEOUTMILDEFAULT));
         this.readTimoutMillis = HPCCJDBCUtils.stringToInt(props.getProperty("ReadTimeoutMilli"),
                 Integer.valueOf(HPCCDriver.READTIMEOUTMILDEFAULT));
 
         HPCCJDBCUtils.traceoutln(Level.INFO, "HPCCDatabaseMetaData ServerAddress: " + serverAddress
                 + " TargetCluster: " + targetcluster + " eclwatch: " + basewseclwatchurl);
 
         targetclusters = new ArrayList<String>();
         querysets = new ArrayList<String>();
         dfufiles = new HPCCLogicalFiles();
         eclqueries = new HPCCQueries();
 
         dbf = DocumentBuilderFactory.newInstance();
 
         if (!isHPCCMetaDataCached())
         {
             setHPCCMetaDataCached(CacheMetaData());
 
             if (targetclusters.size() > 0 && !targetclusters.contains(this.targetcluster))
             {
                 props.setProperty("TargetCluster", targetclusters.get(0));
                 HPCCJDBCUtils.traceoutln(Level.INFO, "Invalid cluster name found: "+ this.targetcluster+ ". using: "+targetclusters.get(0));
                 this.targetcluster = targetclusters.get(0);
             }
 
             if (querysets.size() > 0 && !querysets.contains(this.queryset))
             {
                 props.setProperty("QuerySet", querysets.get(0));
                 HPCCJDBCUtils.traceoutln(Level.INFO, "Invalid query set name found: "+ this.queryset+". using: "+querysets.get(0));
                 this.queryset = querysets.get(0);
             }
         }
         HPCCJDBCUtils.traceoutln(Level.INFO, "HPCCDatabaseMetaData initialized");
     }
 
     public boolean isDFUMetaDataCached()
     {
         return isDFUMetaDataCached;
     }
 
     public void setDFUMetaDataCached(boolean cached)
     {
         this.isDFUMetaDataCached = cached;
     }
 
     public boolean isQuerySetMetaDataCached()
     {
         return isQuerySetMetaDataCached;
     }
 
     public void setQuerySetMetaDataCached(boolean cached)
     {
         this.isQuerySetMetaDataCached = cached;
     }
 
     public boolean isHPCCMetaDataCached()
     {
         return isHPCCMetaDataCached;
     }
 
     public void setHPCCMetaDataCached(boolean isMetaDataCached)
     {
         this.isHPCCMetaDataCached = isMetaDataCached;
     }
 
     private boolean CacheMetaData()
     {
         boolean isSuccess = true;
 
         if (serverAddress == null || targetcluster == null)
             return false;
 
         isSuccess &= fetchHPCCInfo();
 
         isSuccess &= fetchClusterInfo();
 
         isSuccess &= fetchQuerysetsInfo();
 
         if (!lazyLoad)
         {
             setDFUMetaDataCached(fetchHPCCFilesInfo());
 
             if (isDFUMetaDataCached())
             {
                 HPCCJDBCUtils.traceoutln(Level.INFO, "Tables' Metadata fetched: ");
                 Enumeration<Object> em = dfufiles.getFiles();
                 while (em.hasMoreElements())
                 {
                     DFUFile file = (DFUFile) em.nextElement();
                     HPCCJDBCUtils.traceoutln(Level.INFO,"\t"+file.getClusterName()+"."+file.getFileName()+"("+file.getFullyQualifiedName()+")");
                 }
             }
 
             setQuerySetMetaDataCached(fetchHPCCQueriesInfo());
 
             if (isQuerySetMetaDataCached())
             {
                 HPCCJDBCUtils.traceoutln(Level.INFO,"Stored Procedures' Metadata fetched: ");
                 Enumeration<Object> em1 = eclqueries.getQueries();
                 while (em1.hasMoreElements())
                 {
                     HPCCQuery query = (HPCCQuery) em1.nextElement();
                     HPCCJDBCUtils.traceoutln(Level.INFO,"\t" + query.getQuerySet() + "::" + query.getName());
                 }
             }
         }
         else
             HPCCJDBCUtils.traceoutln(Level.INFO,
                     "HPCC file and published query info not pre-fetched (LazyLoad enabled)");
 
         if (!isSuccess)
            HPCCJDBCUtils.traceoutln(Level.SEVERE, "Could not query HPCC metadata check server address, cluster name, wsecl, and wseclwatch configuration.");
 
         return isSuccess;
     }
 
     private int registerFileDetails(Element node, DFUFile file)
     {
         if (file.isSuperFile())
             HPCCJDBCUtils.traceoutln(Level.INFO, "Found super file: " + file.getFullyQualifiedName());
 
         NodeList fileDetail = node.getElementsByTagName("FileDetail");
 
         if (fileDetail.getLength() > 0)
         {
             NodeList resultslist = fileDetail.item(0).getChildNodes(); // ECLResult nodes
 
             for (int i = 0; i < resultslist.getLength(); i++)
             {
                 Node currentfiledetail = resultslist.item(i);
 
                 String nodename = currentfiledetail.getNodeName();
 
                 if (nodename.equals("Ecl"))
                 {
                     file.setFileRecDef(currentfiledetail.getTextContent());
                 }
                 else if (nodename.equals("Filename"))
                 {
                     file.setFileName(currentfiledetail.getTextContent());
                 }
                 else if (nodename.equals("Format"))
                 {
                     file.setFormat(currentfiledetail.getTextContent());
                 }
                 else if (nodename.equals("Description"))
                 {
                     file.setDescription(currentfiledetail.getTextContent());
                 }
                 else if (file.isSuperFile() && nodename.equals("subfiles"))
                 {
                     NodeList subfilelist = currentfiledetail.getChildNodes();
                     for (int y = 0; y < subfilelist.getLength(); y++)
                     {
                         file.addSubfile(subfilelist.item(y).getTextContent());
                     }
                 }
             }
         }
         return 0;
     }
 
     public static int registerSchemaElements(Element node, HPCCQuery query)
     {
         NodeList results = node.getElementsByTagName("Results");
         if (results.getLength() > 0)
         {
             NodeList resultslist = results.item(0).getChildNodes(); // ECLResult nodes
 
             for (int i = 0; i < resultslist.getLength(); i++)
             {
                 Node currentresult = resultslist.item(i);
                 NodeList resultprops = currentresult.getChildNodes();
                 String tablename = "";
                 for (int j = 0; j < resultprops.getLength(); j++)
                 {
                     Node currentresultprop = resultprops.item(j);
                     if (currentresultprop.getNodeName().equals("Name"))
                     {
                         tablename = currentresultprop.getTextContent();
                         query.addResultDataset(tablename);
                     }
                     else if (currentresultprop.getNodeName().equals("ECLSchemas"))
                     {
                         NodeList schemaitems = currentresultprop.getChildNodes(); // ECLSchemaItem
                         String columname = "";
                         String columntype = "";
                         for (int x = 0; x < schemaitems.getLength(); x++)
                         {
                             NodeList schemaitemfields = schemaitems.item(x).getChildNodes();
 
                             for (int y = 0; y < schemaitemfields.getLength(); y++)
                             {
                                 Node elem = schemaitemfields.item(y);
                                 if (elem.getNodeName().equals("ColumnName"))
                                     columname = elem.getTextContent();
                                 else if (elem.getNodeName().equals("ColumnType"))
                                     columntype = elem.getTextContent();
                             }
 
                             HPCCColumnMetaData elemmeta = new HPCCColumnMetaData(columname.toUpperCase(), 0, java.sql.Types.OTHER);
                             elemmeta.setEclType(columntype);
                             elemmeta.setTableName(tablename);
                             elemmeta.setParamType(procedureColumnOut);
                             try
                             {
                                 query.addResultElement(elemmeta);
                             }
                             catch (Exception e)
                             {
                                 HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Could not add dataset element: "
                                         + tablename + ":" + elemmeta.getColumnName());
                             }
                         }
 
                     }
                 }
             }
         }
 
         NodeList variables = node.getElementsByTagName("Variables");
         if (variables.getLength() > 0)
         {
             NodeList resultslist = variables.item(0).getChildNodes(); // ECLResult  nodes
 
             for (int i = 0; i < resultslist.getLength(); i++)
             {
                 Node currentresult = resultslist.item(i);
                 NodeList resultprops = currentresult.getChildNodes();
                 String inParam = "";
                 for (int j = 0; j < resultprops.getLength(); j++)
                 {
                     Node currentresultprop = resultprops.item(j);
 
                     if (currentresultprop.getNodeName().equals("ECLSchemas"))
                     {
                         NodeList schemaitems = currentresultprop.getChildNodes(); // ECLSchemaItem
                         String columname = "";
                         String columntype = "";
                         for (int x = 0; x < schemaitems.getLength(); x++)
                         {
                             NodeList schemaitemfields = schemaitems.item(x).getChildNodes();
 
                             for (int y = 0; y < schemaitemfields.getLength(); y++)
                             {
                                 Node elem = schemaitemfields.item(y);
                                 if (elem.getNodeName().equals("ColumnName"))
                                     columname = elem.getTextContent();
                                 else if (elem.getNodeName().equals("ColumnType"))
                                     columntype = elem.getTextContent();
                             }
                             HPCCColumnMetaData elemmeta = new HPCCColumnMetaData(columname, i + 1,java.sql.Types.OTHER);
                             elemmeta.setEclType(columntype);
                             elemmeta.setTableName(query.getName());
                             elemmeta.setParamType(procedureColumnIn);
                             try
                             {
                                 query.addResultElement(elemmeta);
                             }
                             catch (Exception e)
                             {
                                 HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Could not add dataset element: "
                                         + inParam + ":" + elemmeta.getColumnName());
                             }
                         }
                     }
                 }
             }
         }
 
         return 0;
     }
 
     @Override
     public boolean allProceduresAreCallable() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData allProceduresAreCallable");
         return true;
     }
 
     @Override
     public boolean allTablesAreSelectable() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData allTablesAreSelectable");
         return false;
     }
 
     @Override
     public String getURL() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getURL");
         return "http://www.hpccsystems.com";
     }
 
     @Override
     public String getUserName() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getUserName");
         return UserName;
     }
 
     @Override
     public boolean isReadOnly() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData isReadOnly");
         return true;
     }
 
     @Override
     public boolean nullsAreSortedHigh() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData nullsAreSortedHigh");
         return false;
     }
 
     @Override
     public boolean nullsAreSortedLow() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData nullsAreSortedLow");
         return false;
     }
 
     @Override
     public boolean nullsAreSortedAtStart() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData nullsAreSortedAtStart");
         return false;
     }
 
     @Override
     public boolean nullsAreSortedAtEnd() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData nullsAreSortedAtEnd");
         return false;
     }
 
     @Override
     public String getDatabaseProductName() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDatabaseProductName: HPCC");
         // Some ODBC/JDBC bridges/clients do not like non-alpha chars.
         // Apparently Easysoft's ODBC/JDBC gateway crashes when "-" of " " is in
         // the name
         return "HPCC Systems";
     }
 
     @Override
     public String getDatabaseProductVersion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDatabaseProductVersion");
         // Some ODBC/JDBC bridges/clients do not like alpha chars.
         return HPCCBuildMajor + "." + HPCCBuildMinor;
     }
 
     @Override
     public String getDriverName() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDriverName");
         return "HPCC JDBC Driver";
     }
 
     @Override
     public String getDriverVersion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDriverVersion");
         return HPCCVersionTracker.HPCCMajor + "." + HPCCVersionTracker.HPCCMinor + "." + HPCCVersionTracker.HPCCPoint;
     }
 
     @Override
     public int getDriverMajorVersion()
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDriverMajorVersion");
         return HPCCVersionTracker.HPCCMajor;
     }
 
     @Override
     public int getDriverMinorVersion()
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDriverMinorVersion");
         return HPCCVersionTracker.HPCCMinor;
     }
 
     @Override
     public boolean usesLocalFiles() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData usesLocalFiles");
         return false;
     }
 
     @Override
     public boolean usesLocalFilePerTable() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData usesLocalFilePerTable");
         return false;
     }
 
     @Override
     public boolean supportsMixedCaseIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsMixedCaseIdentifiers");
         return false;
     }
 
     @Override
     public boolean storesUpperCaseIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData storesUpperCaseIdentifiers");
         return false;
     }
 
     @Override
     public boolean storesLowerCaseIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData storesLowerCaseIdentifiers");
         return false;
     }
 
     @Override
     public boolean storesMixedCaseIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData storesMixedCaseIdentifiers");
         return false;
     }
 
     @Override
     public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsMixedCaseQuotedIdentifiers");
         return false;
     }
 
     @Override
     public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData storesUpperCaseQuotedIdentifiers");
         return false;
     }
 
     @Override
     public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData storesLowerCaseQuotedIdentifiers");
         return false;
     }
 
     @Override
     public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData storesMixedCaseQuotedIdentifiers");
         return false;
     }
 
     @Override
     public String getIdentifierQuoteString() throws SQLException
     {
         String result = String.valueOf('"');
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getIdentifierQuoteString: " + result);
 
         // OpenLink seems to require a valid quote
         // Observed instances where Easysoft's ODBC/JDBC bridge crashed if driver returned anything but ""
         // The issue was resolved after receiving an update from Easysoft.
         return result;
     }
 
     @Override
     public String getSQLKeywords() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSQLKeywords");
         return "select from where AND call";
     }
 
     @Override
     public String getNumericFunctions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getNumericFunctions");
         return "";
     }
 
     @Override
     public String getStringFunctions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getStringFunctions");
         return "";
     }
 
     @Override
     public String getSystemFunctions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSystemFunctions");
         return "";
     }
 
     @Override
     public String getTimeDateFunctions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getTimeDateFunctions");
         return "";
     }
 
     @Override
     public String getSearchStringEscape() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSearchStringEscape");
         return "";
     }
 
     @Override
     public String getExtraNameCharacters() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getExtraNameCharacters");
         return "";
     }
 
     @Override
     public boolean supportsAlterTableWithAddColumn() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsAlterTableWithAddColumn");
         return false;
     }
 
     @Override
     public boolean supportsAlterTableWithDropColumn() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsAlterTableWithDropColumn");
         return false;
     }
 
     @Override
     public boolean supportsColumnAliasing() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsColumnAliasing");
         return false;
     }
 
     @Override
     public boolean nullPlusNonNullIsNull() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData nullPlusNonNullIsNull");
         return false;
     }
 
     @Override
     public boolean supportsConvert() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsConvert");
         return false;
     }
 
     @Override
     public boolean supportsConvert(int fromType, int toType) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsConvert");
         return false;
     }
 
     @Override
     public boolean supportsTableCorrelationNames() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsTableCorrelationNames");
         return false;
     }
 
     @Override
     public boolean supportsDifferentTableCorrelationNames() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsDifferentTableCorrelationNames");
         return false;
     }
 
     @Override
     public boolean supportsExpressionsInOrderBy() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsExpressionsInOrderBy");
         return false;
     }
 
     @Override
     public boolean supportsOrderByUnrelated() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsOrderByUnrelated");
         return false;
     }
 
     @Override
     public boolean supportsGroupBy() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsGroupBy");
         return true;
     }
 
     @Override
     public boolean supportsGroupByUnrelated() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsGroupByUnrelated");
         return false;
     }
 
     @Override
     public boolean supportsGroupByBeyondSelect() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsGroupByBeyondSelect");
         return false;
     }
 
     @Override
     public boolean supportsLikeEscapeClause() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsLikeEscapeClause");
         return false;
     }
 
     @Override
     public boolean supportsMultipleResultSets() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsMultipleResultSets");
         return false;
     }
 
     @Override
     public boolean supportsMultipleTransactions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsMultipleTransactions");
         return false;
     }
 
     @Override
     public boolean supportsNonNullableColumns() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsNonNullableColumns");
         return true;
     }
 
     @Override
     public boolean supportsMinimumSQLGrammar() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsMinimumSQLGrammar");
         return true;
     }
 
     @Override
     public boolean supportsCoreSQLGrammar() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCoreSQLGrammar");
         return true;
     }
 
     @Override
     public boolean supportsExtendedSQLGrammar() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsExtendedSQLGrammar");
         return false;
     }
 
     @Override
     public boolean supportsANSI92EntryLevelSQL() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsANSI92EntryLevelSQL");
         return true;
     }
 
     @Override
     public boolean supportsANSI92IntermediateSQL() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsANSI92IntermediateSQL");
         return true;
     }
 
     @Override
     public boolean supportsANSI92FullSQL() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsANSI92FullSQL");
         return true;
     }
 
     @Override
     public boolean supportsIntegrityEnhancementFacility() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsIntegrityEnhancementFacility");
         return false;
     }
 
     @Override
     public boolean supportsOuterJoins() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsOuterJoins");
         return true;
     }
 
     @Override
     public boolean supportsFullOuterJoins() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsFullOuterJoins");
         return true;
     }
 
     @Override
     public boolean supportsLimitedOuterJoins() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsLimitedOuterJoins");
         return true;
     }
 
     @Override
     public String getSchemaTerm() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSchemaTerm");
         return "Schema";
     }
 
     @Override
     public String getProcedureTerm() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getProcedureTerm");
         return "PublishedQuery";
     }
 
     @Override
     public String getCatalogTerm() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getCatalogTerm");
         return "Catalog";
     }
 
     @Override
     public boolean isCatalogAtStart() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData isCatalogAtStart");
         return true;
     }
 
     @Override
     public String getCatalogSeparator() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getCatalogSeparator");
         // Easysoft's 2_3_0 gateway seems to crash if return anything other than
         // ""
         // Will not report Catalog seperator since HPCC systems is lone catalog.
         return "";
     }
 
     @Override
     public boolean supportsSchemasInDataManipulation() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSchemasInDataManipulation");
         return false;
     }
 
     @Override
     public boolean supportsSchemasInProcedureCalls() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSchemasInProcedureCalls");
         return false;
     }
 
     @Override
     public boolean supportsSchemasInTableDefinitions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSchemasInTableDefinitions");
         return false;
     }
 
     @Override
     public boolean supportsSchemasInIndexDefinitions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSchemasInIndexDefinitions");
         return false;
     }
 
     @Override
     public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSchemasInPrivilegeDefinitions");
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInDataManipulation() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCatalogsInDataManipulation");
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInProcedureCalls() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCatalogsInProcedureCalls");
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInTableDefinitions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCatalogsInTableDefinitions");
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInIndexDefinitions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCatalogsInIndexDefinitions");
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCatalogsInPrivilegeDefinitions");
         return false;
     }
 
     @Override
     public boolean supportsPositionedDelete() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsPositionedDelete");
         return false;
     }
 
     @Override
     public boolean supportsPositionedUpdate() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsPositionedUpdate");
         return false;
     }
 
     @Override
     public boolean supportsSelectForUpdate() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSelectForUpdate");
         return false;
     }
 
     @Override
     public boolean supportsStoredProcedures() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsStoredProcedures");
         return true;
     }
 
     @Override
     public boolean supportsSubqueriesInComparisons() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSubqueriesInComparisons");
         return false;
     }
 
     @Override
     public boolean supportsSubqueriesInExists() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSubqueriesInExists");
         return false;
     }
 
     @Override
     public boolean supportsSubqueriesInIns() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSubqueriesInIns");
         return false;
     }
 
     @Override
     public boolean supportsSubqueriesInQuantifieds() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSubqueriesInQuantifieds");
         return false;
     }
 
     @Override
     public boolean supportsCorrelatedSubqueries() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsCorrelatedSubqueries");
         return false;
     }
 
     @Override
     public boolean supportsUnion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsUnion");
         return false;
     }
 
     @Override
     public boolean supportsUnionAll() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsUnionAll");
         return false;
     }
 
     @Override
     public boolean supportsOpenCursorsAcrossCommit() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsOpenCursorsAcrossCommit");
         return false;
     }
 
     @Override
     public boolean supportsOpenCursorsAcrossRollback() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsOpenCursorsAcrossRollback");
         return false;
     }
 
     @Override
     public boolean supportsOpenStatementsAcrossCommit() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsOpenStatementsAcrossCommit");
         return false;
     }
 
     @Override
     public boolean supportsOpenStatementsAcrossRollback() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsOpenStatementsAcrossRollback");
         return false;
     }
 
     @Override
     public int getMaxBinaryLiteralLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxBinaryLiteralLength");
         return 1024;
     }
 
     @Override
     public int getMaxCharLiteralLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxCharLiteralLength");
         return 1024;
     }
 
     @Override
     public int getMaxColumnNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxColumnNameLength");
         return 256;
     }
 
     @Override
     public int getMaxColumnsInGroupBy() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxColumnsInGroupBy");
         return 4;
     }
 
     @Override
     public int getMaxColumnsInIndex() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxColumnsInIndex");
         return 4;
     }
 
     @Override
     public int getMaxColumnsInOrderBy() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxColumnsInOrderBy");
         return 4;
     }
 
     @Override
     public int getMaxColumnsInSelect() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxColumnsInSelect");
         return 16;
     }
 
     @Override
     public int getMaxColumnsInTable() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxColumnsInTable");
         return 128;
     }
 
     @Override
     public int getMaxConnections() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxConnections");
         return 1024;
     }
 
     @Override
     public int getMaxCursorNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxCursorNameLength");
         return 50000;
     }
 
     @Override
     public int getMaxIndexLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxIndexLength");
         return 256;
     }
 
     @Override
     public int getMaxSchemaNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxSchemaNameLength");
         return 256;
     }
 
     @Override
     public int getMaxProcedureNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxProcedureNameLength");
         return 256;
     }
 
     @Override
     public int getMaxCatalogNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxCatalogNameLength");
         return 256;
     }
 
     @Override
     public int getMaxRowSize() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxRowSize");
         return 10000;
     }
 
     @Override
     public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData doesMaxRowSizeIncludeBlobs");
         return true;
     }
 
     @Override
     public int getMaxStatementLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxStatementLength");
         return 200;
     }
 
     @Override
     public int getMaxStatements() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxStatements");
         return 1;
     }
 
     @Override
     public int getMaxTableNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxTableNameLength 10240");
         return 1024;
     }
 
     @Override
     public int getMaxTablesInSelect() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxTablesInSelect");
         return 2;
     }
 
     @Override
     public int getMaxUserNameLength() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getMaxUserNameLength");
         return 20;
         // This is an LDAP limitation, in the future the ESP will expose a query
         // for this value
     }
 
     @Override
     public int getDefaultTransactionIsolation() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDefaultTransactionIsolation");
         return java.sql.Connection.TRANSACTION_NONE;
     }
 
     @Override
     public boolean supportsTransactions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsTransactions");
         return false;
     }
 
     @Override
     public boolean supportsTransactionIsolationLevel(int level) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsTransactionIsolationLevel");
         return false;
     }
 
     @Override
     public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST,
                 "HPCCDatabaseMetaData supportsDataDefinitionAndDataManipulationTransactions");
         return false;
     }
 
     @Override
     public boolean supportsDataManipulationTransactionsOnly() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsDataManipulationTransactionsOnly");
         return false;
     }
 
     @Override
     public boolean dataDefinitionCausesTransactionCommit() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData dataDefinitionCausesTransactionCommit");
         return false;
     }
 
     @Override
     public boolean dataDefinitionIgnoredInTransactions() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData dataDefinitionIgnoredInTransactions");
         return true;
     }
 
     @Override
     public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
             throws SQLException
     {
         List<List> procedures = new ArrayList<List>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         boolean allprocsearch = procedureNamePattern == null || procedureNamePattern.length() == 0
                 || procedureNamePattern.trim().equals("*") || procedureNamePattern.trim().equals("%");
 
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData GETPROCS catalog: " + catalog
                 + ", schemaPattern: " + schemaPattern + ", procedureNamePattern: " + procedureNamePattern);
 
         metacols.add(new HPCCColumnMetaData("PROCEDURE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PROCEDURE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData(PROCEDURE_NAME, 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("R1", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("R2", 5, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("R6", 6, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("REMARKS", 7, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PROCEDURE_TYPE", 8, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("SPECIFIC_NAME", 9, java.sql.Types.VARCHAR));
 
         if (allprocsearch)
         {
             if (!isQuerySetMetaDataCached())
                 setQuerySetMetaDataCached(fetchHPCCQueriesInfo());
 
             Enumeration<Object> aliases = eclqueries.getAliases();
             while (aliases.hasMoreElements())
             {
                 String queryalias = (String)aliases.nextElement();
                 HPCCQuery query = eclqueries.getQuerysetQuery(queryalias);
                 procedures.add(populateProcedureRow(query));
             }
         }
         else
         {
             procedures.add(populateProcedureRow(getHpccQuery(procedureNamePattern)));
         }
 
         return new HPCCResultSet(procedures, metacols, "Procedures");
     }
 
     private ArrayList populateProcedureRow(HPCCQuery query)
     {
         ArrayList rowValues = new ArrayList();
 
         if (query != null)
         {
             rowValues.add("");
             rowValues.add(query.getQuerySet());
             rowValues.add(query.getQuerySet() + "::" + query.getName());
             rowValues.add("");
             rowValues.add("");
             rowValues.add("");
 
             //The remarks string will display this query's actual ID, and its
             //input parameters in order.
 
             String remarks = query.getID() + "( ";
             int index = 0;
             int totalfields = query.getAllInFields().size();
             for (HPCCColumnMetaData field : query.getAllInFields())
             {
                 remarks += field.getColumnName();
                 if (index < totalfields -1)
                     remarks += ", ";
                 index++;
             }
             remarks += " )";
 
             rowValues.add(remarks);
             rowValues.add(procedureResultUnknown);
             rowValues.add(query.getID());
         }
 
         return rowValues;
     }
 
     @Override
     public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
             String columnNamePattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getProcedureColumns catalog: " + catalog
                 + ", schemaPattern: " + schemaPattern + ", procedureNamePattern: " + procedureNamePattern
                 + " columnanmepat: " + columnNamePattern);
 
         List<List> procedurecols = new ArrayList<List>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         boolean allcolumnsearch = columnNamePattern == null || columnNamePattern.length() == 0
                 || columnNamePattern.trim().equals("*") || columnNamePattern.trim().equals("%");
 
         metacols.add(new HPCCColumnMetaData("PROCEDURE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PROCEDURE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData(PROCEDURE_NAME, 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("COLUMN_NAME", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("COLUMN_TYPE", 5, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("DATA_TYPE", 6, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("TYPE_NAME", 7, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PRECISION", 8, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("LENGTH", 9, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("SCALE", 10, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("RADIX", 11, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("NULLABLE", 12, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("REMARKS", 13, java.sql.Types.VARCHAR));
 
         int coltype = java.sql.Types.NULL;
         ResultSet procs = getProcedures(catalog, schemaPattern, procedureNamePattern);
 
         while (procs.next())
         {
             HPCCQuery query = getHpccQuery(procs.getString(PROCEDURE_NAME));
 
             Iterator<HPCCColumnMetaData> queryfields = query.getAllFields().iterator();
 
             while (queryfields.hasNext())
             {
                 HPCCColumnMetaData col = (HPCCColumnMetaData) queryfields.next();
                 String fieldname = col.getColumnName();
                 if (!allcolumnsearch && !columnNamePattern.equalsIgnoreCase(fieldname))
                     continue;
                 coltype = col.getSqlType();
 
                 HPCCJDBCUtils.traceoutln(Level.FINEST, "Proc col Found: " + query.getName() + "." + fieldname + " of type: " + coltype
                         + "(" + HPCCJDBCUtils.convertSQLtype2JavaClassName(coltype) + ")");
 
                 ArrayList rowValues = new ArrayList();
                 procedurecols.add(rowValues);
 
                 /* 1 */rowValues.add(catalog);
                 /* 2 */rowValues.add(schemaPattern);
                 /* 3 */rowValues.add(query.getQuerySet() + "::" + query.getName());
                 /* 4 */rowValues.add(fieldname);
                 /* 5 */rowValues.add(col.getParamType());
                 /* 6 */rowValues.add(coltype);
                 /* 7 */rowValues.add(HPCCJDBCUtils.convertSQLtype2JavaClassName(coltype));
                 /* 8 */rowValues.add(0);
                 /* 9 */rowValues.add(0);
                 /* 10 */rowValues.add(0);
                 /* 11 */rowValues.add(1);
                 /* 12 */rowValues.add(procedureNoNulls);
                 /* 13 */rowValues.add(col.getParamType() == procedureColumnIn ? "Input param index: " + col.getIndex() + "." : "Output param.");
 
                 if (!allcolumnsearch)
                     break;
             }
         }
 
         return new HPCCResultSet(procedurecols, metacols, "ProcedureColumns");
     }
 
     @Override
     public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
             throws SQLException
     {
         /*
          * TABLE_CAT String => table catalog (may be null) TABLE_SCHEM String =>
          * table schema (may be null) TABLE_NAME String => table name TABLE_TYPE
          * String => table type. Typical types are "TABLE", "VIEW",
          * "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
          * "SYNONYM". REMARKS String => explanatory comment on the table
          * TYPE_CAT String => the types catalog (may be null) TYPE_SCHEM String
          * => the types schema (may be null) TYPE_NAME String => type name (may
          * be null) SELF_REFERENCING_COL_NAME String => name of the designated
          * "identifier" column of a typed table (may be null) REF_GENERATION
          * String => specifies how values in SELF_REFERENCING_COL_NAME are
          * created. Values are "SYSTEM", "USER", "DERIVED". (may be null)
          */
 
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData GETTABLES:");
         if (catalog != null)
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t Catalog: " + catalog);
         else
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t Catalog: null");
 
         if (schemaPattern != null)
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t schemaPattern: " + schemaPattern);
         else
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t schemaPattern: null");
 
         if (tableNamePattern != null)
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t tableNamePattern: " + tableNamePattern);
         else
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t tableNamePattern: null");
 
         if (types != null && types.length > 0)
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t types: " + types[0]);
         else
             HPCCJDBCUtils.traceoutln(Level.FINEST, "\t types: null");
 
         boolean alltablesearch = tableNamePattern == null || tableNamePattern.length() == 0
                 || tableNamePattern.trim().equals("*") || tableNamePattern.trim().equals("%");
 
         List<List<String>> tables = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TABLE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData(TABLE_NAME, 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_TYPE", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("REMARKS", 5, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TYPE_CAT", 6, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_CAT", 7, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TYPE_SCHEM", 8, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TYPE_NAME", 9, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("SELF_REFERENCING_COL_NAME", 10, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("REF_GENERATION", 11, java.sql.Types.VARCHAR));
 
         if (alltablesearch)
         {
             if (!isDFUMetaDataCached())
                 setDFUMetaDataCached(fetchHPCCFilesInfo());
 
             Enumeration<Object> files = dfufiles.getFiles();
             while (files.hasMoreElements())
             {
                 DFUFile file = (DFUFile) files.nextElement();
                 if (file.hasFileRecDef())
                     tables.add(populateTableInfo(file));
             }
         }
         else
         {
             DFUFile file = getDFUFile(tableNamePattern);
             if (file != null && file.hasFileRecDef())
                 tables.add(populateTableInfo(file));
         }
         return new HPCCResultSet(tables, metacols, "HPCC Tables");
     }
 
     private ArrayList<String> populateTableInfo(DFUFile table)
     {
         ArrayList<String> rowValues = new ArrayList<String>();
         if (table != null)
         {
             /* 1 */rowValues.add(HPCCJDBCUtils.HPCCCATALOGNAME);
             /* 2 */rowValues.add(null);
             /* 3 */rowValues.add(table.getFullyQualifiedName());
             /* 4 */rowValues.add("TABLE");
             /* 5 */rowValues.add("HPCC File "+ table.getDescription());
             /* 6 */rowValues.add("");
             /* 7 */rowValues.add("");
             /* 8 */rowValues.add("");
             /* 9 */rowValues.add("");
             /* 10 */rowValues.add("");
             /* 11 */rowValues.add("");
         }
         return rowValues;
     }
 
     @Override
     public ResultSet getSchemas() throws SQLException
     {
         if (!isDFUMetaDataCached())
             setDFUMetaDataCached(fetchHPCCFilesInfo());
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData GETSCHEMAS");
 
         List<List<String>> tables = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TABLE_SCHEM", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_CATALOG", 2, java.sql.Types.VARCHAR));
 
         return new HPCCResultSet(tables, metacols, "Schemas");
     }
 
     @Override
     public ResultSet getCatalogs() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getCatalogs");
 
         List<List<String>> catalogs = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TABLE_CAT", 1, java.sql.Types.VARCHAR));
 
         ArrayList<String> rowValues = new ArrayList<String>();
         catalogs.add(rowValues);
         rowValues.add("HPCC System");
 
         return new HPCCResultSet(catalogs, metacols, "Catalogs");
     }
 
     @Override
     public ResultSet getTableTypes() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getTableTypes");
 
         List<List<String>> tabletypes = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TABLE_TYPE", 1, java.sql.Types.VARCHAR));
 
         ArrayList<String> rowValues = new ArrayList<String>();
         tabletypes.add(rowValues);
         rowValues.add("TABLE");
 
         return new HPCCResultSet(tabletypes, metacols, "TableTypes");
     }
 
     public String[] getAllTableFields(String dbname, String tablename)
     {
         DFUFile file = getDFUFile(tablename);
 
         if (file != null)
             return file.getAllTableFieldsStringArray();
 
         return null;
     }
 
     @Override
     public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
             throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData GETCOLUMNS catalog: " + catalog + ", schemaPattern: " + schemaPattern
                 + ", tableNamePattern: " + tableNamePattern + ", columnNamePattern: " + columnNamePattern);
 
         boolean allfieldsearch = columnNamePattern == null || columnNamePattern.length() == 0
                 || columnNamePattern.trim().equals("*") || columnNamePattern.trim().equals("%");
 
         List<List<String>> columns = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TABLE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData(TABLE_NAME, 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("COLUMN_NAME", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("DATA_TYPE", 5, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("TYPE_NAME", 6, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("COLUMN_SIZE", 7, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("BUFFER_LENGTH", 8, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("DECIMAL_DIGITS", 9, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("NUM_PREC_RADIX", 10, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("NULLABLE", 11, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("REMARKS", 12, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("COLUMN_DEF", 13, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("SQL_DATA_TYPE", 14, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("SQL_DATETIME_SUB", 15, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("CHAR_OCTET_LENGTH", 16, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("ORDINAL_POSITION", 17, java.sql.Types.INTEGER));
         metacols.add(new HPCCColumnMetaData("IS_NULLABLE", 18, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("SCOPE_CATLOG", 19, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("SCOPE_SCHEMA", 20, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("SCOPE_TABLE", 21, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("SOURCE_DATA_TYPE", 22, java.sql.Types.SMALLINT));
 
         ResultSet tables = getTables(catalog, schemaPattern, tableNamePattern, null);
 
         while (tables.next())
         {
             DFUFile file = getDFUFile(tables.getString(TABLE_NAME));
 
             Enumeration<Object> e = file.getAllFields();
             while (e.hasMoreElements())
             {
                 HPCCColumnMetaData field = (HPCCColumnMetaData) e.nextElement();
                 String fieldname = field.getColumnName();
                 if (!allfieldsearch && !columnNamePattern.equalsIgnoreCase(fieldname))
                     continue;
 
                 int coltype = java.sql.Types.NULL;
                 coltype = field.getSqlType();
 
                 HPCCJDBCUtils.traceoutln(Level.FINEST, "Table col found: " + file.getFileName() + "." + fieldname + " of type: " + coltype
                         + "(" + HPCCJDBCUtils.convertSQLtype2JavaClassName(coltype) + ")");
 
                 ArrayList rowValues = new ArrayList();
                 columns.add(rowValues);
                 /* 1 */rowValues.add(catalog);
                 /* 2 */rowValues.add(schemaPattern);
                 /* 3 */rowValues.add(file.getFullyQualifiedName());
                 /* 4 */rowValues.add(fieldname);
                 /* 5 */rowValues.add(coltype);
                 /* 6 */rowValues.add(HPCCJDBCUtils.convertSQLtype2JavaClassName(coltype));
                 /* 7 */rowValues.add(0);
                 /* 8 */rowValues.add("null");
                 /* 9 */rowValues.add(0);
                 /* 10 */rowValues.add(0);
                 /* 11 */rowValues.add(1);
                 /* 12 */rowValues.add(file.isKeyFile() && file.getIdxFilePosField() != null
                         && file.getIdxFilePosField().equals(fieldname) ? "File Position Field" : "");
                 /* 13 */rowValues.add("");
                 /* 14 */rowValues.add(0);// unused
                 /* 15 */rowValues.add(0);
                 /* 16 */rowValues.add(0);
                 /* 17 */rowValues.add(field.getIndex()); // need to get index
                 /* 18 */rowValues.add("YES");
                 /* 19 */rowValues.add(coltype == java.sql.Types.REF ? null : "");
                 /* 20 */rowValues.add(coltype == java.sql.Types.REF ? null : "");
                 /* 21 */rowValues.add(coltype == java.sql.Types.REF ? null : "");
                 /* 22 */rowValues.add(coltype == java.sql.Types.REF ? null : "");
 
                 if (!allfieldsearch)
                     break;
             }
         }
 
         return new HPCCResultSet(columns, metacols, tableNamePattern +"'s columns");
     }
 
     @Override
     public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
             throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getColumnPrivileges");
         throw new UnsupportedOperationException("HPCCDBMetaData:  getColumnPrivileges Not  supported yet.");
     }
 
     @Override
     public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
             throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getTablePrivileges");
         throw new UnsupportedOperationException("HPCCDBMetaData: getTablePrivileges Not  supported yet.");
     }
 
     @Override
     public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
             throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getBestRowIdentifier");
         throw new UnsupportedOperationException("HPCCDBMetaData: getBestRowIdentifier Not  supported yet.");
     }
 
     @Override
     public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getVersionColumns");
         throw new UnsupportedOperationException("HPCCDBMetaData: getVersionColumns Not  supported yet.");
     }
 
     @Override
     public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getPrimaryKeys catalog: " + catalog + ", schema: " + schema + ", table: " + table);
 
         List<List<String>> importedkeys = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TABLE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("TABLE_NAME", 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("COLUMN_NAME", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("KEY_SEQ", 5, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("PK_NAME", 6, java.sql.Types.VARCHAR));
 
         return new HPCCResultSet(importedkeys, metacols, "PrimaryKeys");
     }
 
     @Override
     public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
     {
 
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getImportedKeys catalog: " + catalog + ", schema: " + schema  + ", table: " + table);
 
         List<List<String>> importedkeys = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("PKTABLE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PKTABLE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PKTABLE_NAME", 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PKCOLUMN_NAME", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKTABLE_CAT", 5, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKTABLE_SCHEM", 6, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKTABLE_NAME", 7, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKCOLUMN_NAME", 8, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("KEY_SEQ", 9, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("UPDATE_RULE", 10, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("DELETE_RULE", 11, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("FK_NAME", 12, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PK_NAME", 13, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("DEFERRABILITY", 14, java.sql.Types.SMALLINT));
 
         return new HPCCResultSet(importedkeys, metacols, "ImportedKeys");
     }
 
     @Override
     public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getExportedKeys catalog: " + catalog + ", schema: " + schema + ", table: " + table);
 
         List<List<String>> exportedkeys = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("PKTABLE_CAT", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PKTABLE_SCHEM", 2, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PKTABLE_NAME", 3, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PKCOLUMN_NAME", 4, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKTABLE_CAT", 5, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKTABLE_SCHEM", 6, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKTABLE_NAME", 7, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("FKCOLUMN_NAME", 8, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("KEY_SEQ", 9, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("UPDATE_RULE", 10, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("DELETE_RULE", 11, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("FK_NAME", 12, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("PK_NAME", 13, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("DEFERRABILITY", 14, java.sql.Types.SMALLINT));
 
         return new HPCCResultSet(exportedkeys, metacols, "ExportedKeys");
     }
 
     @Override
     public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
             String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getCrossReference");
         throw new UnsupportedOperationException("HPCCDBMetaData: getCrossReference Not  supported yet.");
     }
 
     @Override
     public ResultSet getTypeInfo() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData GETTYPEINFO");
 
         List<List<String>> types = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TYPE_NAME", 1, java.sql.Types.VARCHAR));
         metacols.add(new HPCCColumnMetaData("DATA_TYPE", 2, java.sql.Types.INTEGER));// SQL data type from java.sql.Types
         metacols.add(new HPCCColumnMetaData("PRECISION", 3, java.sql.Types.INTEGER));// maximum precision
         metacols.add(new HPCCColumnMetaData("LITERAL_PREFIX", 4, java.sql.Types.VARCHAR));// prefix used to quote a literal (may be null)
         metacols.add(new HPCCColumnMetaData("LITERAL_SUFFIX", 5, java.sql.Types.VARCHAR));// suffix used to quote a literal (may be null)
         metacols.add(new HPCCColumnMetaData("CREATE_PARAMS", 6, java.sql.Types.VARCHAR));// parameters  used  in  creating the  type (may be null)
         metacols.add(new HPCCColumnMetaData("NULLABLE", 7, java.sql.Types.SMALLINT));// can you use NULL for this type.
         metacols.add(new HPCCColumnMetaData("CASE_SENSITIVE", 8, java.sql.Types.BOOLEAN));
         metacols.add(new HPCCColumnMetaData("SEARCHABLE", 9, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("UNSIGNED_ATTRIBUTE", 10, java.sql.Types.BOOLEAN));
         metacols.add(new HPCCColumnMetaData("FIXED_PREC_SCALE", 11, java.sql.Types.BOOLEAN));// can it be a money value
         metacols.add(new HPCCColumnMetaData("AUTO_INCREMENT", 12, java.sql.Types.BOOLEAN));// can it be used for an auto-increment value
         metacols.add(new HPCCColumnMetaData("LOCAL_TYPE_NAME", 13, java.sql.Types.VARCHAR));// localized version of type name (may be null)
         metacols.add(new HPCCColumnMetaData("MINIMUM_SCALE", 14, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("MAXIMUM_SCALE", 15, java.sql.Types.SMALLINT));
         metacols.add(new HPCCColumnMetaData("SQL_DATA_TYPE", 16, java.sql.Types.INTEGER));// unused
         metacols.add(new HPCCColumnMetaData("SQL_DATETIME_SUB", 17, java.sql.Types.INTEGER));// unused
         metacols.add(new HPCCColumnMetaData("NUM_PREC_RADIX", 18, java.sql.Types.INTEGER));// unused
 
         for (EclTypes ecltype : EclTypes.values())
         {
             ArrayList rowValues = new ArrayList();
             types.add(rowValues);
 
             /* 1 */rowValues.add(String.valueOf(ecltype));
             /* 2 */rowValues.add(HPCCJDBCUtils.convertECLtypeCode2SQLtype(ecltype));
             /* 3 */rowValues.add(0);
             /* 4 */rowValues.add(null);
             /* 5 */rowValues.add(null);
             /* 6 */rowValues.add(null);
             /* 7 */rowValues.add(typeNullableUnknown);
             /* 8 */rowValues.add(false);
             /* 9 */rowValues.add(typePredNone);
             /* 10 */rowValues.add(false);
             /* 11 */rowValues.add(false);
             /* 12 */rowValues.add(false);
             /* 13 */rowValues.add(HPCCJDBCUtils.convertSQLtype2JavaClassName(HPCCJDBCUtils.convertECLtypeCode2SQLtype(ecltype)));
             /* 14 */rowValues.add(0);
             /* 15 */rowValues.add(0);
             /* 16 */rowValues.add(0);
             /* 17 */rowValues.add(0);
             /* 18 */rowValues.add(0);
         }
 
         return new HPCCResultSet(types, metacols, "Types");
     }
 
     @Override
     public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
             throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getIndexInfo");
         throw new UnsupportedOperationException("HPCCDBMetaData: Not  supported yet.");
     }
 
     @Override
     public boolean supportsResultSetType(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsResultSetType");
         return true;
     }
 
     @Override
     public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsResultSetConcurrency");
         return false;
     }
 
     @Override
     public boolean ownUpdatesAreVisible(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData ownUpdatesAreVisible");
         return false;
     }
 
     @Override
     public boolean ownDeletesAreVisible(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData ownDeletesAreVisible");
         return false;
     }
 
     @Override
     public boolean ownInsertsAreVisible(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData ownInsertsAreVisible");
         return false;
     }
 
     @Override
     public boolean othersUpdatesAreVisible(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData othersUpdatesAreVisible");
         return false;
     }
 
     @Override
     public boolean othersDeletesAreVisible(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData othersDeletesAreVisible");
         return false;
     }
 
     @Override
     public boolean othersInsertsAreVisible(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData othersInsertsAreVisible");
         return false;
     }
 
     @Override
     public boolean updatesAreDetected(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData updatesAreDetected");
         return false;
     }
 
     @Override
     public boolean deletesAreDetected(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData deletesAreDetected");
         return false;
     }
 
     @Override
     public boolean insertsAreDetected(int type) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData insertsAreDetected");
         return false;
     }
 
     @Override
     public boolean supportsBatchUpdates() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsBatchUpdates");
         return false;
     }
 
     @Override
     public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
             throws SQLException
     {
         /*
          * TYPE_CAT String => the type's catalog (may be null) TYPE_SCHEM String
          * => type's schema (may be null) TYPE_NAME String => type name
          * CLASS_NAME String => Java class name DATA_TYPE int => type value
          * defined in java.sql.Types. One of JAVA_OBJECT, STRUCT, or DISTINCT
          * REMARKS String => explanatory comment on the type BASE_TYPE short =>
          * type code of the source type of a DISTINCT type or the type that
          * implements the user-generated reference type of the
          * SELF_REFERENCING_COLUMN of a structured type as defined in
          * java.sql.Types (null if DATA_TYPE is not DISTINCT or not STRUCT with
          * REFERENCE_GENERATION = USER_DEFINED)
          *
          * Note: If the driver does not support UDTs, an empty result set is
          * returned.
          */
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getUDTs");
 
         List<List<String>> udts = new ArrayList<List<String>>();
         ArrayList<HPCCColumnMetaData> metacols = new ArrayList<HPCCColumnMetaData>();
 
         metacols.add(new HPCCColumnMetaData("TYPE_CAT", 1, java.sql.Types.VARCHAR)); // the type's catalog (may be  null)
         metacols.add(new HPCCColumnMetaData("TYPE_SCHEM", 2, java.sql.Types.VARCHAR)); // type's  schema  (may  be  null)
         metacols.add(new HPCCColumnMetaData("TYPE_NAME", 3, java.sql.Types.VARCHAR)); // type  name
         metacols.add(new HPCCColumnMetaData("CLASS_NAME", 4, java.sql.Types.VARCHAR)); // Java class name
         metacols.add(new HPCCColumnMetaData("DATA_TYPE", 5, java.sql.Types.INTEGER)); // type value defined in java.sql.Types.
                                                                                       // One of JAVA_OBJECT, STRUCT, or DISTINCT
         metacols.add(new HPCCColumnMetaData("REMARKS", 6, java.sql.Types.VARCHAR)); // explanatory comment on the type
         metacols.add(new HPCCColumnMetaData("BASE_TYPE", 7, java.sql.Types.SMALLINT));
 
         /*
          * for(EclTypes ecltype : EclTypes.values()) { ArrayList rowValues = new
          * ArrayList(); udts.add(rowValues);
          *
          * rowValues.add(String.valueOf(ecltype));
          * rowValues.add(convertECLtypeCode2SQLtype(ecltype.ordinal()));
          * rowValues.add(0); rowValues.add(null); rowValues.add(null);
          * rowValues.add(null); rowValues.add(typeNullableUnknown); }
          */
 
         return new HPCCResultSet(udts, metacols, "UDTs");
     }
 
     @Override
     public Connection getConnection() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getConnection");
         throw new UnsupportedOperationException("HPCCDBMetaData: getConnection Not  supported yet.");
     }
 
     @Override
     public boolean supportsSavepoints() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsSavepoints");
        throw new UnsupportedOperationException("HPCCDBMetaData: supportsSavepoints Not  supported yet.");
     }
 
     @Override
     public boolean supportsNamedParameters() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsNamedParameters");
         throw new UnsupportedOperationException("HPCCDBMetaData: supportsNamedParameters Not  supported yet.");
     }
 
     @Override
     public boolean supportsMultipleOpenResults() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsMultipleOpenResults");
         throw new UnsupportedOperationException("HPCCDBMetaData: supportsMultipleOpenResults Not  supported yet.");
     }
 
     @Override
     public boolean supportsGetGeneratedKeys() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsGetGeneratedKeys");
         throw new UnsupportedOperationException("HPCCDBMetaData: supportsGetGeneratedKeysNot  supported yet.");
     }
 
     @Override
     public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSuperTypes");
         throw new UnsupportedOperationException("HPCCDBMetaData: getSuperTypes Not  supported yet.");
     }
 
     @Override
     public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSuperTables");
         throw new UnsupportedOperationException("HPCCDBMetaData: getSuperTables Not  supported yet.");
     }
 
     @Override
     public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
             String attributeNamePattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getAttributes");
         throw new UnsupportedOperationException("HPCCDBMetaData: getAttributes Not  supported yet.");
     }
 
     @Override
     public boolean supportsResultSetHoldability(int holdability) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsResultSetHoldability");
         throw new UnsupportedOperationException(
                 "HPCCDBMetaData: supportsResultSetHoldability(int holdability) Not  supported yet.");
     }
 
     @Override
     public int getResultSetHoldability() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getResultSetHoldability");
         throw new UnsupportedOperationException("HPCCDBMetaData: getResultSetHoldability Not  supported yet.");
     }
 
     @Override
     public int getDatabaseMajorVersion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDatabaseMajorVersion");
         return HPCCBuildMajor;
     }
 
     @Override
     public int getDatabaseMinorVersion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getDatabaseMinorVersion");
         return (int) HPCCBuildMinor;
     }
 
     private boolean fetchHPCCFileColumnInfo(DFUFile file, DocumentBuilder db)
     {
         boolean isSuccess = true;
 
         if (basewseclwatchurl == null)
             return false;
 
         try
         {
             if (file.getFullyQualifiedName().length() > 0)
             {
                 String filedetailUrl = basewseclwatchurl + "/WsDfu/DFUInfo?Name="
                         + URLEncoder.encode(file.getFullyQualifiedName(), "UTF-8") + "&rawxml_";
 
                 // now request the schema for this file.
                 URL queryschema = HPCCJDBCUtils.makeURL(filedetailUrl);
                 HttpURLConnection queryschemaconnection = createHPCCESPConnection(queryschema);
 
                 InputStream schema = queryschemaconnection.getInputStream();
                 Document dom2 = db.parse(schema);
                 Element docElement = dom2.getDocumentElement();
 
                 // Get all pertinent detail info regarding this file
                 registerFileDetails(docElement, file);
 
                 // Add this file name to files structure
                 dfufiles.putFile(file.getFullyQualifiedName(), file);
             }
             else
                 HPCCJDBCUtils.traceoutln(Level.INFO, "Not fetching info for HPCC file: " + file.getFullyQualifiedName());
         }
         catch (Exception e)
         {
             isSuccess = false;
             e.printStackTrace();
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  e.getMessage());
         }
         return isSuccess;
     }
 
     private int parseDFULogicalFiles(InputStream xml, boolean setReportedFileCount)
     {
         int dfuFileParsedCount = 0;
 
         try
         {
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml);
 
             if (setReportedFileCount)
             {
                 try
                 {
                     NodeList NumFilesCount = dom.getElementsByTagName("NumFiles");
 
                     if (NumFilesCount.getLength() > 0)
                     {
                         dfufiles.setReportedFileCount(NumFilesCount.item(0).getTextContent());
                         HPCCJDBCUtils.traceoutln(Level.INFO, "Fetching " + pageSize + " files (tables) out of "
                                 + dfufiles.getReportedFileCount() + " reported files.");
                     }
                 }
                 catch (Exception e)
                 {
                     HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Could not determine total HPCC Logical file count");
                 }
             }
             NodeList querySetList = dom.getElementsByTagName("DFULogicalFiles");
 
             if (querySetList.getLength() > 0)
             {
                 NodeList queryList = querySetList.item(0).getChildNodes();
                 for (int i = 0; i < queryList.getLength(); i++)
                 {
                     Node currentNode = queryList.item(i);
                     if (currentNode.getNodeName().equals("DFULogicalFile"))
                     {
                         NodeList querysetquerychildren = currentNode.getChildNodes();
 
                         DFUFile file = new DFUFile();
                         String currentTextCont = "";
 
                         for (int j = 0; j < querysetquerychildren.getLength(); j++)
                         {
                             currentTextCont = querysetquerychildren.item(j).getTextContent();
                             if (!currentTextCont.equals(""))
                             {
                                 String NodeName = querysetquerychildren.item(j).getNodeName();
                                 if (NodeName.equals("Prefix"))
                                 {
                                     file.setPrefix(currentTextCont);
                                 }
                                 else if (NodeName.equals("ClusterName"))
                                 {
                                     file.setClusterName(currentTextCont);
                                 }
                                 else if (NodeName.equals("Name"))
                                 {
                                     file.setFullyQualifiedName(currentTextCont);
                                 }
                                 else if (NodeName.equals("Owner"))
                                 {
                                     file.setOwner(currentTextCont);
                                 }
                                 else if (NodeName.equals("Totalsize"))
                                 {
                                     file.setTotalSize(HPCCJDBCUtils.NUMFORMATTER.get().parse(currentTextCont).longValue());
                                 }
                                 else if (NodeName.equals("RecordCount"))
                                 {
                                     file.setRecordCount(HPCCJDBCUtils.NUMFORMATTER.get().parse(currentTextCont).longValue());
                                 }
                                 else if (NodeName.equals("LongSize"))
                                 {
                                     file.setLongSize(HPCCJDBCUtils.NUMFORMATTER.get().parse(currentTextCont).longValue());
                                 }
                                 else if (NodeName.equals("LongRecordCount"))
                                 {
                                     file.setLongRecordCount(Long.parseLong(currentTextCont));
                                 }
                                 else if (NodeName.equals("isSuperfile"))
                                 {
                                     file.setSuperFile(currentTextCont.equals("1"));
                                 }
                                 else if (NodeName.equals("isZipfile"))
                                 {
                                     file.setZipFile(currentTextCont.equals("1"));
                                 }
                                 else if (NodeName.equals("isDirectory"))
                                 {
                                     file.setDirectory(currentTextCont);
                                 }
                                 else if (NodeName.equals("IntSize"))
                                 {
                                     file.setIntSize(Integer.parseInt(currentTextCont));
                                 }
                                 else if (NodeName.equals("IntRecordCount"))
                                 {
                                     file.setIntRecordCount(Integer.parseInt(currentTextCont));
                                 }
                                 else if (NodeName.equals("FromRoxieCluster"))
                                 {
                                     file.setFromRoxieCluster(currentTextCont.equals("1"));
                                 }
                                 else if (NodeName.equals("IsKeyFile"))
                                 {
                                     file.setIsKeyFile(currentTextCont.equals("1"));
                                 }
                             }
                         }
 
                         if (file.getFullyQualifiedName().length() > 0)
                         {
                             if (file.getRecordCount() >= 0)
                             {
                                 if (fetchHPCCFileColumnInfo(file, db))
                                     dfuFileParsedCount++;
                             }
                             else
                                 HPCCJDBCUtils.traceoutln(Level.INFO, file.getFullyQualifiedName()
                                         + " does not appear to contain a valid record definition.");
                         }
                         else
                             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Found DFU file but could not determine name");
                     }
                 }
             }
         }
         catch (ParserConfigurationException e)
         {
             e.printStackTrace();
         }
         catch (SAXException e)
         {
             e.printStackTrace();
         }
         catch (ParseException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         return dfuFileParsedCount;
     }
 
     private boolean fetchHPCCFileInfo(String filename)
     {
         boolean isSuccess = false;
 
         if (basewseclwatchurl == null || filename.length() <= 0)
             return isSuccess;
 
         String urlString = "";
         try
         {
             urlString = basewseclwatchurl + "/WsDfu/DFUQuery?LogicalName=" + filename + "&rawxml_&filetype=" + FILEFETCHTYPE_ALL;
             URL dfuLogicalFilesURL = HPCCJDBCUtils.makeURL(urlString);
             HttpURLConnection dfulogfilesConn = createHPCCESPConnection(dfuLogicalFilesURL);
 
             HPCCJDBCUtils.traceoutln(Level.INFO, "Fetching file information: " + urlString);
             isSuccess = parseDFULogicalFiles(dfulogfilesConn.getInputStream(), false) > 0 ? true : false;
         }
         catch (SocketTimeoutException e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Warning: Connection to HPCC timed out while fetching: "
                     + filename);
         }
         catch (MalformedURLException e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Error fetching HPCC File: " + urlString);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
         return isSuccess;
     }
 
     private boolean fetchHPCCFilesInfo()
     {
         boolean isSuccess = false;
 
         if (isDFUMetaDataCached())
         {
             HPCCJDBCUtils.traceoutln(Level.INFO, "HPCC dfufile info already present (reconnect to force fetch)");
             return true;
         }
 
         if (basewseclwatchurl == null)
             return false;
 
         String urlString = "";
         try
         {
             urlString = basewseclwatchurl + "/WsDfu/DFUQuery?LogicalName=*&PageSize=" + pageSize + "&PageStartFrom=" + pageOffset + "&rawxml_&filetype=" + FILEFETCHTYPE_ALL;
             HPCCJDBCUtils.traceoutln(Level.INFO, "Fetching tables: " + urlString);
             URL dfuLogicalFilesURL = HPCCJDBCUtils.makeURL(urlString);
             HttpURLConnection dfulogfilesConn = createHPCCESPConnection(dfuLogicalFilesURL);
 
             isSuccess = parseDFULogicalFiles(dfulogfilesConn.getInputStream(), true) > 0 ? true : false;
 
             if (isSuccess)
             {
                 try
                 {
                     dfufiles.updateSuperFiles();
                 }
                 catch (Exception e)
                 {
                     HPCCJDBCUtils.traceoutln(Level.INFO, "WARNING: updating superfiles failed.");
                 }
             }
         }
         catch (SocketTimeoutException e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,
                     "Warning: Connection to HPCC timed out while fetching HPCC Files");
         }
         catch (MalformedURLException e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Error fetching HPCC Files: " + urlString);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         return isSuccess;
     }
 
     private int parseHPCCQuery(InputStream xml)
     {
         int hpccQueryParsedCount = 0;
 
         String querysetname = this.queryset;
 
         try
         {
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml);
 
             NodeList querySetNameNode = dom.getElementsByTagName("QuerySetName");
             if (querySetNameNode.getLength() > 0)
             {
                 querysetname = querySetNameNode.item(0).getTextContent();
             }
 
             NodeList querySetqueryList = dom.getElementsByTagName("QuerySetQuery");
 
             for (int i = 0; i < querySetqueryList.getLength() && i < this.pageSize; i++)
             {
                 NodeList querysetquerychildren = querySetqueryList.item(i).getChildNodes();
 
                 HPCCQuery query = new HPCCQuery();
                 query.setQueryset(querysetname);
 
                 for (int j = 0; j < querysetquerychildren.getLength(); j++)
                 {
                     String NodeName = querysetquerychildren.item(j).getNodeName();
                     if (NodeName.equals("Id"))
                     {
                         query.setID(querysetquerychildren.item(j).getTextContent());
                     }
                     else if (NodeName.equals("Name"))
                     {
                         query.setName(querysetquerychildren.item(j).getTextContent());
                     }
                     else if (NodeName.equals("Wuid"))
                     {
                         query.setWUID(querysetquerychildren.item(j).getTextContent());
                     }
                     else if (NodeName.equals("Dll"))
                     {
                         query.setDLL(querysetquerychildren.item(j).getTextContent());
                     }
                     else if (NodeName.equals("Suspended"))
                     {
                         query.setSuspended(Boolean.parseBoolean(querysetquerychildren.item(j).getTextContent()));
                     }
                 }
                 // for each QuerySetQuery found above, get all schema related info:
                 String queryinfourl = basewseclwatchurl
                         + "/WsWorkunits/WUInfo/WUInfoRequest?Wuid=" + query.getWUID() + "&IncludeExceptions=1"
                         + "&IncludeGraphs=0" + "&IncludeSourceFiles=0" + "&IncludeTimers=0" + "&IncludeDebugValues=0"
                         + "&IncludeApplicationValues=0" + "&IncludeWorkflows=0" + "&IncludeHelpers=0" + "&rawxml_";
 
                 // now request the schema for each hpcc query
                 URL queryschema = HPCCJDBCUtils.makeURL(queryinfourl);
                 HttpURLConnection queryschemaconnection = createHPCCESPConnection(queryschema);
 
                 InputStream schema = queryschemaconnection.getInputStream();
 
                 try
                 {
                     Document dom2 = db.parse(schema);
 
                     Element docElement = dom2.getDocumentElement();
 
                     // Get all pertinent info regarding this query (analogous to
                     // SQL Stored Procedure)
                     registerSchemaElements(docElement, query);
 
                     // Add this query name to queries structure
                     eclqueries.put(query);
                     hpccQueryParsedCount++;
                 }
                 catch (Exception e)
                 {
                     HPCCJDBCUtils.traceoutln(Level.SEVERE,
                             "Could not retreive Query info for: " + query.getID() + "(" + query.getWUID() + ")");
                 }
             }
 
             NodeList querySetAliases = dom.getElementsByTagName("QuerySetAlias");
             for (int i = 0; i < querySetAliases.getLength(); i++)
             {
                 String aliasid = null;
                 String aliasname = null;
 
                 NodeList querysetaliaschildren = querySetAliases.item(i).getChildNodes();
                 for (int j = 0; j < querysetaliaschildren.getLength(); j++)
                 {
                     String NodeName = querysetaliaschildren.item(j).getNodeName();
                     if (NodeName.equals("Id"))
                     {
                         aliasid = querysetaliaschildren.item(j).getTextContent();
                     }
                     else if (NodeName.equals("Name"))
                     {
                         aliasname = querysetaliaschildren.item(j).getTextContent();
                     }
                 }
 
                 if (aliasid != null && aliasname != null)
                 {
                     if (eclqueries.containsQueryName(querysetname, aliasid))
                         eclqueries.putAlias(querysetname, aliasname, aliasid);
                 }
             }
         }
         catch (Exception e)
         {
 
         }
 
         return hpccQueryParsedCount;
     }
 
     private boolean fetchHPCCQueryInfo(String queryset, String eclqueryname)
     {
         boolean isSuccess = false;
 
         if (basewseclwatchurl == null)
             return false;
 
         try
         {
             String urlString = basewseclwatchurl
                     + "/WsWorkunits/WUQuerysetDetails?QuerySetName=" + queryset + "&Filter=" + eclqueryname
                     + "&FilterType=Name" + "&rawxml_";
 
             URL querysetURL = new URL(urlString);
             HttpURLConnection querysetconnection = createHPCCESPConnection(querysetURL);
 
             InputStream xml = querysetconnection.getInputStream();
 
             isSuccess = parseHPCCQuery(xml) > 0 ? true : false;
 
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         return isSuccess;
     }
 
     private boolean fetchHPCCQueriesInfo()
     {
         boolean isSuccess = true;
 
         if (isQuerySetMetaDataCached())
         {
             HPCCJDBCUtils.traceoutln(Level.INFO, "HPCC query info already present (reconnect to force fetch)");
             return true;
         }
 
         if (basewseclwatchurl == null)
             return false;
 
         if (querysets.size() == 0)
             isSuccess &= fetchQuerysetsInfo();
 
         try
         {
             for (int z = 0; z < querysets.size(); z++)
             {
                 HPCCJDBCUtils.traceoutln(Level.INFO, "Fetching up to " + pageSize
                         + " Stored Procedures' Metadata from QuerySet " + querysets.get(z));
 
                 String urlString = basewseclwatchurl
                         + "/WsWorkunits/WUQuerysetDetails?QuerySetName=" + querysets.get(z) + "&rawxml_";
 
                 URL querysetURL = HPCCJDBCUtils.makeURL(urlString);
                 HttpURLConnection querysetconnection = createHPCCESPConnection(querysetURL);
 
                 InputStream xml = querysetconnection.getInputStream();
 
                 parseHPCCQuery(xml);
             }
         }
         catch (IOException e)
         {
             isSuccess = false;
             e.printStackTrace();
         }
         return isSuccess;
     }
 
     private boolean fetchQuerysetsInfo()
     {
         if (basewseclwatchurl == null)
             return false;
 
         if (querysets.size() > 0)
         {
             HPCCJDBCUtils.traceoutln(Level.INFO, "QuerySet info already present (reconnect to force fetch)");
             return true;
         }
 
         try
         {
             String urlString = basewseclwatchurl + "/WsWorkunits/WUQuerysets?rawxml_";
 
             URL clusterInfoURL = HPCCJDBCUtils.makeURL(urlString);
             HttpURLConnection clusterInfoConnection = createHPCCESPConnection(clusterInfoURL);
 
             InputStream xml = clusterInfoConnection.getInputStream();
 
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml);
 
             String querysetname;
             NodeList querysetsList = dom.getElementsByTagName("QuerySetName");
             for (int i = 0; i < querysetsList.getLength(); i++)
             {
                 querysetname = querysetsList.item(i).getTextContent();
                 if (querysetname.length() > 0)
                     querysets.add(querysetname);
             }
         }
         catch (Exception e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Could not fetch cluster information.");
             return false;
         }
         return true;
     }
 
     private boolean fetchClusterInfo()
     {
         if (basewseclwatchurl == null)
             return false;
 
         if (targetclusters.size() > 0)
         {
             HPCCJDBCUtils.traceoutln(Level.FINEST, "Cluster info already present (reconnect to force fetch)");
             return true;
         }
 
         try
         {
             String urlString = basewseclwatchurl
                     + "/WsTopology/TpTargetClusterQuery?Type=ROOT&rawxml_&ShowDetails=0";
 
             URL clusterInfoURL = HPCCJDBCUtils.makeURL(urlString);
             HttpURLConnection clusterInfoConnection = createHPCCESPConnection(clusterInfoURL);
 
             InputStream xml = clusterInfoConnection.getInputStream();
 
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml);
 
             NodeList clusterList = dom.getElementsByTagName("TpTargetCluster");
             for (int i = 0; i < clusterList.getLength(); i++)
             {
                 NodeList clusterElements = clusterList.item(i).getChildNodes();
                 for (int y = 0; y < clusterElements.getLength(); y++)
                 {
                     Node currNode = clusterElements.item(y);
                     if (currNode.getNodeName().equals("Name"))
                     {
                         targetclusters.add(currNode.getTextContent());
                         break;
                     }
                 }
             }
         }
         catch (Exception e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Could not fetch cluster information.");
             return false;
         }
 
         return true;
     }
 
     private boolean fetchHPCCInfo()
     {
         if (basewseclwatchurl == null)
             return false;
 
         try
         {
             String urlString = basewseclwatchurl + "/WsSMC/Activity?rawxml_";
 
             HPCCJDBCUtils.traceoutln(Level.INFO, "HPCCDatabaseMetaData Fetching HPCC INFO: " + urlString);
 
             URL querysetURL = new URL(urlString);
             HttpURLConnection querysetconnection = createHPCCESPConnection(querysetURL);
 
             InputStream xml = querysetconnection.getInputStream();
 
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml);
 
             NodeList activityList = dom.getElementsByTagName("ActivityResponse");
             if (activityList.getLength() > 0)
             {
                 NodeList queryList = activityList.item(0).getChildNodes();
                 for (int i = 0; i < queryList.getLength(); i++)
                 {
                     Node currentNode = queryList.item(i);
 
                     /* Get build block */
                     if (currentNode.getNodeName().equals("Build"))
                     {
                         Node buildNode = currentNode.getFirstChild();
 
                         if (buildNode != null && buildNode.getNodeType() == Node.TEXT_NODE)
                             HPCCBuildVersionFull = buildNode.getTextContent();
 
                         parseVersionString();
 
                         break;
                     }
                     /*
                      * Get whatever other element block
                      *
                      * else if
                      * (currentNode.getNodeName().equals("RoxieClusters"))
                      * REMOVE THE ABOVE BREAK!!!!!
                      */
                 }
             }
         }
         catch (Exception e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Could not fetch HPCC info.");
             HPCCJDBCUtils.traceoutln(Level.ALL,  e.getMessage());
             return false;
         }
 
         return true;
     }
 
     private void parseVersionString()
     {
         try
         {
             HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData Parsing HPCC version info: " + HPCCBuildVersionFull);
             HPCCBuildType = HPCCBuildVersionFull.substring(0, HPCCBuildVersionFull.lastIndexOf('_'));
 
             String numeric = HPCCBuildVersionFull.substring(HPCCBuildVersionFull.indexOf('_') + 1,
                     HPCCBuildVersionFull.indexOf('-'));
 
             HPCCBuildMajor = Short.parseShort(numeric.substring(0, numeric.indexOf('.')));
             HPCCBuildMinor = Float.parseFloat(numeric.substring(numeric.indexOf('.') + 1));
         }
         catch (Exception e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE,  "Error parsing HPCC version: " + e.getMessage());
         }
     }
 
     @Override
     public int getJDBCMajorVersion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getJDBCMajorVersion");
         return JDBCVerMajor;
     }
 
     @Override
     public int getJDBCMinorVersion() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getJDBCMinorVersion");
         return JDBCVerMinor;
     }
 
     @Override
     public int getSQLStateType() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSQLStateType");
         throw new UnsupportedOperationException("HPCCDBMetaData: getSQLStateType Not  supported yet.");
     }
 
     @Override
     public boolean locatorsUpdateCopy() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData locatorsUpdateCopy");
         throw new UnsupportedOperationException("HPCCDBMetaData: locatorsUpdateCopy Not  supported yet.");
     }
 
     @Override
     public boolean supportsStatementPooling() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData supportsStatementPooling");
         throw new UnsupportedOperationException("HPCCDBMetaData: supportsStatementPooling Not  supported yet.");
     }
 
     @Override
     public RowIdLifetime getRowIdLifetime() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData RowIdLifetime");
         throw new UnsupportedOperationException("HPCCDBMetaData: getRowIdLifetime Not  supported yet.");
     }
 
     @Override
     public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getSchemas");
         throw new UnsupportedOperationException("HPCCDBMetaData: getSchemas Not supported yet.");
     }
 
     @Override
     public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getConnection");
         throw new UnsupportedOperationException(
                 "HPCCDBMetaData:  supportsStoredFunctionsUsingCallSyntax Not supported yet.");
     }
 
     @Override
     public boolean autoCommitFailureClosesAllResultSets() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData autoCommitFailureClosesAllResultSets");
         throw new UnsupportedOperationException(
                 "HPCCDBMetaData: autoCommitFailureClosesAllResultSets Not  supported yet.");
     }
 
     @Override
     public ResultSet getClientInfoProperties() throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getClientInfoProperties");
         throw new UnsupportedOperationException("HPCCDBMetaData: getClientInfoProperties Not  supported yet.");
     }
 
     @Override
     public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getFunctions");
         throw new UnsupportedOperationException(
                 "HPCCDBMetaData: getFunctions(String catalog, String schemaPattern, String functionNamePattern) Not  supported yet.");
     }
 
     @Override
     public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
             String columnNamePattern) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData getFunctionColumns");
         throw new UnsupportedOperationException(
                 "HPCCDBMetaData: getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) Not  supported yet.");
     }
 
     @Override
     public <T> T unwrap(Class<T> iface) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData unwrap");
         throw new UnsupportedOperationException("HPCCDBMetaData: unwrap(Class<T> iface) Not  supported yet.");
     }
 
     @Override
     public boolean isWrapperFor(Class<?> iface) throws SQLException
     {
         HPCCJDBCUtils.traceoutln(Level.FINEST, "HPCCDatabaseMetaData isWrapperFor");
         throw new UnsupportedOperationException("HPCCDBMetaData: isWrapperFor(Class<?> iface) Not  supported yet.");
     }
 
     public String getBasicAuth()
     {
         return basicAuth;
     }
 
     public boolean tableExists(String clustername, String filename)
     {
         boolean found = dfufiles.containsFileName(filename);
 
         if (!found)
             found = fetchHPCCFileInfo(filename);
 
         return found;
     }
 
     public HPCCQuery getHpccQuery(String hpccqueryname)
     {
         HPCCQuery query = null;
         String querysetname;
         String queryname;
 
         String split[] = hpccqueryname.split("::", 2);
         if (split.length <= 1)
         {
             querysetname = this.queryset;
             queryname = hpccqueryname;
         }
         else
         {
             querysetname = split[0];
             queryname = split[1];
         }
 
         if (hpcclQueryExists(querysetname, queryname))
             query = eclqueries.getQuery(querysetname, queryname);
 
         return query;
     }
 
     public boolean hpcclQueryExists(String querysetname, String hpccqueryname)
     {
         boolean found = eclqueries.containsQueryName(querysetname, hpccqueryname);
 
         if (!found)
             found = fetchHPCCQueryInfo(querysetname, hpccqueryname);
 
         return found;
     }
 
     private boolean fetchSuperFileSubfile(DFUFile file)
     {
         boolean isSuccess = false;
 
         List<String> subfiles = file.getSubfiles();
         for (String subfilename : subfiles)
         {
             if (tableExists("", subfilename) && !isSuccess)
             {
                 DFUFile subfile = dfufiles.getFile(subfilename);
                 if (subfile.hasFileRecDef())
                 {
                     isSuccess = true;
                 }
                 else if (subfile.isSuperFile())
                 {
                     isSuccess = fetchSuperFileSubfile(subfile);
                 }
             }
 
             if (isSuccess) //no need to continue looking for more recdefs
                 break;
         }
         return isSuccess;
     }
 
     public DFUFile getDFUFile(String hpccfilename)
     {
         DFUFile file = null;
         if (tableExists("", hpccfilename))
         {
             file = dfufiles.getFile(hpccfilename);
             if (file.isSuperFile() && !file.hasFileRecDef())
             {
                 if (file.containsSubfiles())
                 {
                     if (fetchSuperFileSubfile(file))
                         dfufiles.updateSuperFile(hpccfilename);
                 }
             }
             if (file.isKeyFile() && !file.hasKeyedFieldInfoBeenSet())
             {
                 getKeyedFieldInfo(file);
             }
         }
 
         return file;
     }
 
     private void getKeyedFieldInfo(DFUFile file)
     {
         String openfiledetailUrl;
         try
         {
             openfiledetailUrl = basewseclwatchurl
                     + "/WsDfu/DFUSearchData?OpenLogicalName="
                     + URLEncoder.encode(file.getFullyQualifiedName(), "UTF-8") + "&Cluster="
                     + file.getClusterName() + "&RoxieSelections=0" + "&rawxml_";
             DocumentBuilder db = dbf.newDocumentBuilder();
 
             URL queryfiledata = HPCCJDBCUtils.makeURL(openfiledetailUrl);
             HttpURLConnection queryfiledataconnection = createHPCCESPConnection(queryfiledata);
 
             InputStream filesearchinfo = queryfiledataconnection.getInputStream();
             Document dom3 = db.parse(filesearchinfo);
 
             Element docElement2 = dom3.getDocumentElement();
 
             NodeList keyfiledetail = docElement2.getChildNodes();
             if (keyfiledetail.getLength() > 0)
             {
                 for (int k = 0; k < keyfiledetail.getLength(); k++)
                 {
                     Node currentnode = keyfiledetail.item(k);
                     if (currentnode.getNodeName().startsWith("DFUDataKeyedColumns"))
                     {
                         NodeList keyedColumns = currentnode.getChildNodes();
                         for (int fieldindex = 0; fieldindex < keyedColumns.getLength(); fieldindex++)
                         {
                             Node KeyedColumn = keyedColumns.item(fieldindex);
                             NodeList KeyedColumnFields = KeyedColumn.getChildNodes();
                             for (int q = 0; q < KeyedColumnFields.getLength(); q++)
                             {
                                 Node keyedcolumnfield = KeyedColumnFields.item(q);
                                 if (keyedcolumnfield.getNodeName().equals("ColumnLabel"))
                                 {
                                     file.addKeyedColumnInOrder(keyedcolumnfield.getTextContent());
                                     break;
                                 }
                                 // currently not interested in any other
                                 // field, if we need other field values, remove above break;
                             }
                         }
                     }
                     else if (currentnode.getNodeName().startsWith("DFUDataNonKeyedColumns"))
                     {
                         NodeList nonKeyedColumns = currentnode.getChildNodes();
 
                         for (int fieldindex = 0; fieldindex < nonKeyedColumns.getLength(); fieldindex++)
                         {
                             Node nonKeyedColumn = nonKeyedColumns.item(fieldindex);
                             NodeList nonKeyedColumnFields = nonKeyedColumn.getChildNodes();
                             for (int q = 0; q < nonKeyedColumnFields.getLength(); q++)
                             {
                                 Node nonkeyedcolumnfield = nonKeyedColumnFields.item(q);
                                 if (nonkeyedcolumnfield.getNodeName().equals("ColumnLabel"))
                                 {
                                     file.addNonKeyedColumnInOrder(nonkeyedcolumnfield.getTextContent());
                                     break;
                                 }
                                 // currently not interested in any other
                                 // field, if we need other field values, remove above break;
                             }
                         }
                     }
                 }
             }
         }
         catch (Exception e)
         {
             HPCCJDBCUtils.traceoutln(Level.SEVERE, "Error fetching Index file info: " + file.getFullyQualifiedName());
         }
     }
 
     protected HttpURLConnection createHPCCESPConnection(URL theurl) throws IOException
     {
         HttpURLConnection conn = (HttpURLConnection) theurl.openConnection();
         conn.setInstanceFollowRedirects(false);
         conn.setRequestProperty("Authorization", basicAuth);
         conn.setRequestMethod("GET");
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setConnectTimeout(connectTimoutMillis);
         conn.setReadTimeout(readTimoutMillis);
 
         return conn;
     }
 }
