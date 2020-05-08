 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.connector;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 import com.gooddata.connector.backend.ConnectorBackend;
 import com.gooddata.connector.driver.Constants;
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.exception.ProcessingException;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.JdbcUtil;
 import com.gooddata.util.StringUtil;
 import com.gooddata.util.JdbcUtil.ResultSetHandler;
 
 /**
  * GoodData JDBC Connector
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class JdbcConnector extends AbstractConnector implements Connector {
 
     private static Logger l = Logger.getLogger(JdbcConnector.class);
 
     private String jdbcUrl;
     private String jdbcUsername;
     private String jdbcPassword;
     private String sqlQuery;
 
     /**
      * Creates a new JDBC connector
      * @param connectorBackend connector backend
      */
     protected JdbcConnector(ConnectorBackend connectorBackend) {
         super(connectorBackend);
     }
 
     /**
      * Creates a new JDBC connector
      * @param connectorBackend connector backend
      * @return a new instance of the JdbcConnector
      */
     public static JdbcConnector createConnector(ConnectorBackend connectorBackend) {
         return new JdbcConnector(connectorBackend);
     }
 
     /**
      * Saves a template of the config file
      * @param name new schema name
      * @param configFileName config file name
      * @param jdbcUsr JDBC username
      * @param jdbcPsw JDBC password
      * @param jdbcDriver JDBC driver class name
      * @param jdbcUrl JDBC url
      * @param query JDBC query
      * @throws IOException if there is a problem with writing the config file
      * @throws SQLException if there is a problem with the db
      */
     public static void saveConfigTemplate(String name, String configFileName, String jdbcUsr, String jdbcPsw,
                                   String jdbcDriver, String jdbcUrl,String query)
             throws IOException, SQLException {
         l.debug("Saving JDBC config template.");
         l.debug("Loading JDBC driver "+jdbcDriver);
         try {
             Class.forName(jdbcDriver).newInstance();
         } catch (InstantiationException e) {
             l.error("Can't load JDBC driver.", e);
         } catch (IllegalAccessException e) {
             l.error("Can't load JDBC driver.", e);
         } catch (ClassNotFoundException e) {
             l.error("Can't load JDBC driver.", e);
         }
         l.debug("JDBC driver "+jdbcDriver+" loaded.");
         SourceSchema s = SourceSchema.createSchema(name);
         Connection con = null;
         Statement st = null;
         ResultSet rs = null;
         ResultSetMetaData rsm;
         try {
             con = connect(jdbcUrl, jdbcUsr, jdbcPsw);
             st = con.createStatement();
             rs = JdbcUtil.executeQuery(st, query);
             rs.next();
             rsm = rs.getMetaData();
             int cnt = rsm.getColumnCount();
             for(int i=1; i <= cnt; i++) {
                 String cnm = StringUtil.formatShortName(rsm.getColumnName(i));
                 String cdsc = rsm.getColumnName(i);
                 String type = getColumnType(rsm.getColumnType(i));
                 SourceColumn column = new SourceColumn(cnm, type, cdsc);
                 if (SourceColumn.LDM_TYPE_DATE.equals(type)) {
                 	column.setFormat(Constants.DEFAULT_DATE_FMT_STRING);
                 }
                 s.addColumn(column);
                 
             }
             s.writeConfig(new File(configFileName));
         }
         finally {
             if(rs != null)
                 rs.close();
             if(st != null)
                 st.close();
             if(con != null && !con.isClosed())
                 con.close();
         }
         l.debug("Saved JDBC config template.");
     }
 
     /**
      * Determines the LDM type from the JDBC data type
      * @param jct jdbc data type id java.sql.Types)
      * @return the ldm data type
      */
     private static String getColumnType(int jct) {
         String type;
         switch (jct) {
             case Types.CHAR:
                 type = SourceColumn.LDM_TYPE_ATTRIBUTE;
                 break;
             case Types.VARCHAR:
                 type = SourceColumn.LDM_TYPE_ATTRIBUTE;
                 break;
             case Types.INTEGER:
                 type = SourceColumn.LDM_TYPE_ATTRIBUTE;
                 break;
             case Types.BIGINT:
                 type = SourceColumn.LDM_TYPE_ATTRIBUTE;
                 break;
             case Types.FLOAT:
                 type = SourceColumn.LDM_TYPE_FACT;
                 break;
             case Types.DOUBLE:
                 type = SourceColumn.LDM_TYPE_FACT;
                 break;
             case Types.DECIMAL:
                 type = SourceColumn.LDM_TYPE_FACT;
                 break;
             case Types.NUMERIC:
                 type = SourceColumn.LDM_TYPE_FACT;
                 break;
             case Types.DATE:
                 type = SourceColumn.LDM_TYPE_DATE;
                 break;
             case Types.TIMESTAMP:
                 type = SourceColumn.LDM_TYPE_DATE;
                 break;
             default:
                 type = SourceColumn.LDM_TYPE_ATTRIBUTE;
                 break;
         }
         return type;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void extract() throws IOException {
         Connection con = null;
         Statement s = null;
         ResultSet rs = null;
         try {
             con = connect();
             File dataFile = FileUtil.getTempFile();
            l.debug("Extracting JDBC data to file="+dataFile.getAbsolutePath());
             CSVWriter cw = new CSVWriter(new FileWriter(dataFile));
             s = con.createStatement();
             JdbcUtil.executeQuery(con, getSqlQuery(), new ResultSetCsvWriter(cw));
             l.debug("Finished retrieving JDBC data.");
             cw.flush();
             cw.close();
            l.debug("Making file="+dataFile.getAbsolutePath()+" writable.");
             FileUtil.makeWritable(dataFile);
            l.debug("Made file="+dataFile.getAbsolutePath()+" writable.");
             getConnectorBackend().extract(dataFile);
             FileUtil.recursiveDelete(dataFile);
         }
         catch (SQLException e) {
             l.debug("Error retrieving data from the JDBC source.", e);
             throw new InternalErrorException("Error retrieving data from the JDBC source.", e);
         }
         finally {
             try {
                 if (rs != null)
                     rs.close();
                 if (s != null)
                     s.close();
                 if (con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException e) {
                 l.error("Error closing JDBC connection.",e);
             }
         }
     }
 
     /**
      * Connects the DB
      * @param jdbcUrl JDBC url
      * @param usr JDBC username
      * @param psw JDBC password
      * @return JDBC connection
      * @throws SQLException in case of DB issues
      */
     private static Connection connect(String jdbcUrl, String usr, String psw) throws SQLException {
         return DriverManager.getConnection(jdbcUrl, usr, psw);
     }
 
     /**
      * {@inheritDoc}
      */
     public Connection connect() throws SQLException {
         return connect(getJdbcUrl(), getJdbcUsername(), getJdbcPassword());
     }
 
     /**
      * JDBC username getter
      * @return JDBC username
      */
     public String getJdbcUsername() {
         return jdbcUsername;
     }
 
     /**
      * JDBC username setter
      * @param jdbcUsername JDBC username
      */
     public void setJdbcUsername(String jdbcUsername) {
         this.jdbcUsername = jdbcUsername;
     }
 
     /**
      * JDBC password getter
      * @return JDBC password
      */
     public String getJdbcPassword() {
         return jdbcPassword;
     }
 
     /**
      * JDBC password setter
      * @param jdbcPassword JDBC password
      */
     public void setJdbcPassword(String jdbcPassword) {
         this.jdbcPassword = jdbcPassword;
     }
 
     /**
      * JDBC query getter
      * @return JDBC query
      */
     public String getSqlQuery() {
         return sqlQuery;
     }
 
     /**
      * JDBC query setter
      * @param sqlQuery JDBC query
      */
     public void setSqlQuery(String sqlQuery) {
         this.sqlQuery = sqlQuery;
     }
 
     /**
      * JDBC url getter
      * @return JDBC url
      */
     public String getJdbcUrl() {
         return jdbcUrl;
     }
 
     /**
      * JDBC url setter
      * @param jdbcUrl JDBC url
      */
     public void setJdbcUrl(String jdbcUrl) {
         this.jdbcUrl = jdbcUrl;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command "+c.getCommand());
         try {
             if(c.match("GenerateJdbcConfig")) {
                 generateJdbcConfig(c, cli, ctx);
             }
             else if(c.match("LoadJdbc")) {
                 loadJdbc(c, cli, ctx);
             }
             else {
                 l.debug("No match passing the command "+c.getCommand()+" further.");
                 return super.processCommand(c, cli, ctx);
             }
         }
         catch (SQLException e) {
             throw new ProcessingException(e);
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         l.debug("Processed command "+c.getCommand());
         return true;
     }
 
     /**
      * Loads the JDBC driver
      * @param drv JDBC driver class
      */
     private void loadDriver(String drv) {
         try {
             Class.forName(drv).newInstance();
         } catch (InstantiationException e) {
             l.error("Can't load JDBC driver.", e);
         } catch (IllegalAccessException e) {
             l.error("Can't load JDBC driver.", e);
         } catch (ClassNotFoundException e) {
             l.error("Can't load JDBC driver.", e);
         }
     }
 
     /**
      * Loads new JDBC data command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      * @throws SQLException in case of a DB issue
      */
     private void loadJdbc(Command c, CliParams p, ProcessingContext ctx) throws IOException, SQLException {
         String configFile = c.getParamMandatory("configFile");
         String usr = null;
         if(c.checkParam("username"))
             usr = c.getParam("username");
         String psw = null;
         if(c.checkParam("password"))
             psw = c.getParam("password");
         String drv = c.getParamMandatory("driver");
         String url = c.getParamMandatory("url");
         String q = c.getParamMandatory("query");
         loadDriver(drv);
         File conf = FileUtil.getFile(configFile);
         initSchema(conf.getAbsolutePath());
         setJdbcUsername(usr);
         setJdbcPassword(psw);
         setJdbcUrl(url);
         setSqlQuery(q);
         // sets the current connector
         ctx.setConnector(this);
         setProjectId(ctx);
     }
 
     /**
      * Generates the JDBC config command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      * @throws SQLException in case of a DB issue 
      */
     private void generateJdbcConfig(Command c, CliParams p, ProcessingContext ctx) throws IOException, SQLException {
         String configFile = c.getParamMandatory("configFile");
         String name = c.getParamMandatory("name");
         String usr = null;
         if(c.checkParam("username"))
             usr = c.getParam("username");
         String psw = null;
         if(c.checkParam("password"))
             psw = c.getParam("password");
         String drv = c.getParamMandatory("driver");
         String url = c.getParamMandatory("url");
         String query = c.getParamMandatory("query");
         loadDriver(drv);
         File cf = new File(configFile);
         JdbcConnector.saveConfigTemplate(name, cf.getAbsolutePath(), usr, psw, drv, url, query);
     }
 
     private static class ResultSetCsvWriter implements ResultSetHandler {
     	
     	private final CSVWriter cw;
     	
     	public ResultSetCsvWriter(CSVWriter cw) {
     		this.cw = cw;
 		}
     	
     	public void handle(ResultSet rs) throws SQLException {
     		final int length = rs.getMetaData().getColumnCount();
     		final String[] line = new String[length];
     		for (int i = 1; i <= length; i++) {
     			final int sqlType = rs.getMetaData().getColumnType(i);
     			final Object value = rs.getObject(i);
     			if (value == null)
     				line[i - 1] = "\\N";
     			else {
 	    			switch (sqlType) {
 	    				case Types.DATE:
 	    				case Types.TIMESTAMP:
 	    					Date date = (Date)value;
 	    					line[i - 1] = Constants.DEFAULT_DATE_FMT.format(date);
 	    					break;
 	    				default:
 	    					line[i - 1] = value.toString();
 	    			}
     			}
     		}
     		cw.writeNext(line);
     	}
     }
 }
