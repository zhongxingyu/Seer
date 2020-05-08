 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2010 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.mediation.task;
 
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.mediation.Record;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import org.apache.log4j.Logger;
 
 import java.sql.*;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This plug-in saves mediation errors to a JDBC database by generating insert statements
  * matching the set of {@link PricingField} objects from the mediation process. This effectively
  * preserves the original CDR record along with error states for later review.
  *
  * This class requires that a database table be created with columns matching the field
  * names from the mediation format XML definition. jBilling does not create this table, it must
  * be created by the end user when installing and configuring this plug-in.
  * 
  * Plug-in parameters:
  *
  *      url             mandatory parameter, url for JDBC connection to database,
  *                          i.e. jdbc:postgresql://localhost:5432/jbilling_test
  *
  *      driver          JDBC driver class for connection to DB, defaults to 'org.postgresql.Driver'
  *      username        username for database, defaults to 'SA'
  *      password        password for database, defaults to a blank string ("")
  *      table_name      table name for saving records, defaults to 'mediation_errors'
  *      errors_column   column name for saving error codes, defaults to 'error_message'
  *      retry_column    column name for saving flag of reprocessing, defaults to 'should_retry'
  *
  * @author Alexander Aksenov
  * @since 31.01.2010
  */
 public class SaveToJDBCMediationErrorHandler extends PluggableTask
         implements IMediationErrorHandler {
 
     private static final Logger log = Logger.getLogger(SaveToJDBCMediationErrorHandler.class);
 
     // plug-in parameters
     // mandatory parameter, url with host, port, database, etc
     protected final static String PARAM_DATABASE_URL = "url";
     // optional, may be used default values
     protected final static String PARAM_DRIVER = "driver";
     protected final static String PARAM_DATABASE_USERNAME = "username";
     protected final static String PARAM_DATABASE_PASSWORD = "password";
     protected final static String PARAM_TABLE_NAME = "table_name";
     protected final static String PARAM_ERRORS_COLUMN_NAME = "errors_column";
     protected final static String PARAM_RETRY_COLUMN_NAME = "retry_column";
 
     // defaults
     public static final String DRIVER_DEFAULT = "org.postgresql.Driver";
     public static final String DATABASE_USERNAME_DEFAULT = "SA";
     public static final String DATABASE_PASSWORD_DEFAULT = "";
 
     public static final String TABLE_NAME_DEFAULT = "mediation_errors";
     public static final String ERRORS_COLUMN_NAME_DEFAULT = "error_message";
     public static final String RETRY_COLUMN_NAME_DEFAULT = "should_retry";
 
 
     public void process(Record record, List<String> errors, Date processingTime) throws TaskException {
         log.debug("Perform saving errors to database ");
 
         Connection connection = null;
         try {
             connection = getConnection();
 
             List<String> columnNames = new LinkedList<String>();
             for (PricingField field : record.getFields()) {
                 columnNames.add(escapedKeywordsColumnName(field.getName()));
             }
             columnNames.add(getParameter(PARAM_ERRORS_COLUMN_NAME, ERRORS_COLUMN_NAME_DEFAULT));
             columnNames.add(getParameter(PARAM_RETRY_COLUMN_NAME, RETRY_COLUMN_NAME_DEFAULT));
 
             StringBuilder query = new StringBuilder("insert into ");
             query.append(getParameter(PARAM_TABLE_NAME, TABLE_NAME_DEFAULT));
             query.append("(");
             query.append(com.sapienter.jbilling.server.util.Util.join(columnNames, ", "));
             query.append(") values (");
             query.append(com.sapienter.jbilling.server.util.Util.join(Collections.nCopies(columnNames.size(), "?"), ", "));
             query.append(")");
 
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString());
 
             int index = 1;
             for (PricingField field : record.getFields()) {
                 switch (field.getType()) {
                     case STRING:
                         preparedStatement.setString(index, field.getStrValue());
                         break;
                     case INTEGER:
                         preparedStatement.setInt(index, field.getIntValue());
                         break;
                     case DECIMAL:
                         preparedStatement.setDouble(index, field.getDoubleValue());
                         break;
                     case DATE:
                         preparedStatement.setTimestamp(index, new Timestamp(field.getDateValue().getTime()));
                         break;
                 }
                 index++;
             }
             // errors column
             preparedStatement.setString(index, com.sapienter.jbilling.server.util.Util.join(errors, " "));
             index++;
             // retry column
             preparedStatement.setBoolean(index, false);
 
             // save data
             preparedStatement.executeUpdate();
 
         } catch (SQLException e) {
             log.error("Saving errors to database failed", e);
             throw new TaskException(e);
         } catch (ClassNotFoundException e) {
             log.error("Saving errors to database failed, incorrect configuration", e);
             throw new TaskException(e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     log.error(e);
                 }
             }
         }
     }
 
 
     protected Connection getConnection() throws SQLException, ClassNotFoundException, TaskException {
         String driver = getParameter(PARAM_DRIVER, DRIVER_DEFAULT);
         Object url = parameters.get(PARAM_DATABASE_URL);
         if (url == null) {
             throw new TaskException("Error, expected mandatory parameter databae_url");
         }
         String username = getParameter(PARAM_DATABASE_USERNAME, DATABASE_USERNAME_DEFAULT);
         String password = getParameter(PARAM_DATABASE_PASSWORD, DATABASE_PASSWORD_DEFAULT);
 
         // create connection
         Class.forName(driver); // load driver
         return DriverManager.getConnection((String) url, username, password);
     }
 
 
     protected String getParameter(String key, String defaultValue) {
         Object value = parameters.get(key);
         return value != null ? (String) value : defaultValue;
     }
 
    // TODO: escape keywords differ for different databases
     protected String escapedKeywordsColumnName(String columnName) {
        return "\"" + columnName + "\"";
     }
}
