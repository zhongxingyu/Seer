 package com.technophobia.substeps.database.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import com.technophobia.substeps.database.runner.DatabaseSetupTearDown;
 import com.technophobia.substeps.database.runner.DatabaseSubstepsConfiguration;
 import com.technophobia.substeps.model.SubSteps;
 import com.technophobia.substeps.model.parameter.BooleanConverter;
 import com.technophobia.substeps.model.parameter.DoubleConverter;
 import com.technophobia.substeps.model.parameter.IntegerConverter;
 import com.technophobia.substeps.model.parameter.LongConverter;
 import org.junit.Assert;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Run named queries from a properties file
  */
 public class NamedSqlSubStepImplementations extends SQLSubStepImplementations {
     private static final Logger LOG = LoggerFactory.getLogger(NamedSqlSubStepImplementations.class);
 
     protected Properties properties = new Properties();
 
     public NamedSqlSubStepImplementations() {
 
         final String queryFileName = DatabaseSubstepsConfiguration.getNamedQueryPropertyFile();
 
         if (queryFileName != null) {
 
             LOG.debug("Loading queries from file {}", queryFileName);
 
             final InputStream queryFileStream = NamedSqlSubStepImplementations.class.getResourceAsStream(queryFileName);
 
             if (queryFileStream != null) {
                 try {
                     properties.load(queryFileStream);
                 } catch (IOException e) {
                     LOG.error(e.getMessage(), e);
                     throw new AssertionError("Failed to load database.query.file");
                 }
             }
 
         }
     }
 
     @SubSteps.Step("ExecuteNamedQuery \"([^\"]*)\"")
     public void executeNamedQuery(final String name) {
 
         String sql = lookupNamedQuery(name);
 
         LOG.debug("Running query {} ({})", name, sql);
 
         executeQuery(sql);
     }
 
     @SubSteps.Step("ExecuteNamedUpdate \"([^\"]*)\"")
     public void executeNamedUpdate(final String name) {
 
         String sql = lookupNamedQuery(name);
 
         LOG.debug("Executing update {} ({})", name, sql);
 
         executeUpdate(sql);
     }
 
     @SubSteps.Step("FetchNamedQuery \"([^\"]*)\"")
     public void fetchNamedQuery(final String name) {
 
         LOG.debug("fetching query \"{}\"", name);
 
         String sql = lookupNamedQuery(name);
 
         final Connection connection = DatabaseSetupTearDown.getConnectionContext().getConnection();
 
         try {
 
             DatabaseSetupTearDown.getStatementContext().setStatement(connection.prepareStatement(sql));
 
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Error creating query " + name + " (" + sql + ")");
         }
     }
 
     @SubSteps.Step("AddStringParameter value=\"([^\"]*)\"")
     public void addStringParameter(final String value) {
 
         LOG.debug("Adding String parameter {}", value);
 
         DatabaseSetupTearDown.getStatementContext().addStringParameter(value);
     }
 
     @SubSteps.Step("AddStringParameter value=null")
     public void addNullStringParameter() {
 
         LOG.debug("Adding null String parameter");
 
         DatabaseSetupTearDown.getStatementContext().addStringParameter(null);
     }
 
 
     @SubSteps.Step("AddIntegerParameter value=([0-9]*)")
     public void addIntegerParameter(@SubSteps.StepParameter(converter = IntegerConverter.class) final Integer value) {
 
        LOG.debug("Adding Integer parameter {}", value);
 
         DatabaseSetupTearDown.getStatementContext().addIntegerParameter(value);
     }
 
     @SubSteps.Step("AddIntegerParameter value=null")
     public void addNullIntegerParameter() {
 
         LOG.debug("Adding null Integer parameter");
 
         DatabaseSetupTearDown.getStatementContext().addIntegerParameter(null);
     }
 
     @SubSteps.Step("AddBooleanParameter value=(true|false)")
     public void addBooleanParameter(@SubSteps.StepParameter(converter = BooleanConverter.class) final Boolean value) {
 
         LOG.debug("Adding Boolean parameter {}", value);
 
         DatabaseSetupTearDown.getStatementContext().addBooleanParameter(value);
     }
 
     @SubSteps.Step("AddBooleanParameter value=null")
     public void addNullBooleanParameter() {
 
         LOG.debug("Adding null Boolean parameter");
 
         DatabaseSetupTearDown.getStatementContext().addBooleanParameter(null);
     }
 
     @SubSteps.Step("AddDoubleParameter value=([0-9,.]*)")
     public void addDoubleParameter(@SubSteps.StepParameter(converter = DoubleConverter.class) final Double value) {
 
         LOG.debug("Adding Double parameter {}", value);
 
         DatabaseSetupTearDown.getStatementContext().addDoubleParameter(value);
     }
 
     @SubSteps.Step("AddDoubleParameter value=null")
     public void addNullDoubleParameter() {
 
         LOG.debug("Adding null Double parameter");
 
         DatabaseSetupTearDown.getStatementContext().addDoubleParameter(null);
     }
 
     @SubSteps.Step("AddLongParameter value=([0-9,.]*)")
     public void addLongParameter(@SubSteps.StepParameter(converter = LongConverter.class) final Long value) {
 
         LOG.debug("Adding Long parameter {}", value);
 
         DatabaseSetupTearDown.getStatementContext().addLongParameter(value);
     }
 
     @SubSteps.Step("AddLongParameter value=null")
     public void addNullLongParameter() {
 
         LOG.debug("Adding null Long parameter");
 
         DatabaseSetupTearDown.getStatementContext().addLongParameter(null);
     }
 
     @SubSteps.Step("ExecuteQuery")
     public void executePreparedQuery() {
 
         final PreparedStatement statement = DatabaseSetupTearDown.getStatementContext().getStatement();
 
         LOG.debug("Executing prepared statement");
 
         try {
             DatabaseSetupTearDown.getExecutionContext().setQueryResult(statement.executeQuery());
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to execute prepared statement");
         } finally {
             DatabaseSetupTearDown.getStatementContext().closeStatement();
         }
     }
 
     @SubSteps.Step("ExecuteUpdate")
     public void executePreparedUpdate() {
 
         final PreparedStatement statement = DatabaseSetupTearDown.getStatementContext().getStatement();
 
         LOG.debug("Executing prepared update statement");
 
         try {
             statement.executeUpdate();
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to execute prepared update statement");
         } finally {
             DatabaseSetupTearDown.getStatementContext().closeStatement();
         }
     }
 
     private String lookupNamedQuery(final String name) {
 
         String sql = properties.getProperty(name);
 
         Assert.assertNotNull("No query found with name " + name, sql);
 
         return sql;
     }
 }
