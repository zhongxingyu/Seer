 package com.technophobia.substeps.database.runner;
 
 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Date;
 
 import org.junit.Assert;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A context to contain a statement being operated on
  * @author Alan Raison
  */
 public class DatabaseStatementContext {
     private static final Logger LOG = LoggerFactory.getLogger(DatabaseStatementContext.class);
     private PreparedStatement statement;
     private int argumentIndex = 1;
 
     public void setStatement(final PreparedStatement statement) {
 
         LOG.debug("setting new prepared statement");
 
        closeStatement();
         this.statement = statement;
     }
 
     public PreparedStatement getStatement() {
         return this.statement;
     }
 
     public void addStringParameter(final String value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             statement.setString(argumentIndex++, value);
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set string parameter on statement");
         }
     }
 
     public void addIntegerParameter(final Integer value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.INTEGER);
             } else {
                 statement.setInt(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set int parameter on statement");
         }
     }
 
     public void addBigDecimalParameter(final BigDecimal value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             statement.setBigDecimal(argumentIndex++, value);
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set big decimal parameter on statement");
         }
     }
 
     public void addBooleanParameter(final Boolean value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.BOOLEAN);
             } else {
                 statement.setBoolean(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set boolean parameter on statement");
         }
     }
 
     public void addByteParameter(final Byte value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.TINYINT);
             } else {
                 statement.setByte(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set byte parameter on statement");
         }
     }
 
     public void addBytesParameter(final byte[] value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             statement.setBytes(argumentIndex++, value);
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set bytes parameter on statement");
         }
     }
 
     public void addDateParameter(final Date value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.DATE);
             } else {
                 statement.setDate(argumentIndex++, new java.sql.Date(value.getTime()));
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set date parameter on statement");
         }
     }
 
     public void addDoubleParameter(final Double value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.DOUBLE);
             } else {
                 statement.setDouble(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set double parameter on statement");
         }
     }
 
     public void addFloatParameter(final Float value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.FLOAT);
             } else {
                 statement.setFloat(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set float parameter on statement");
         }
     }
 
     public void addLongParameter(final Long value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.BIGINT);
             } else {
                 statement.setLong(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set long parameter on statement");
         }
     }
 
     public void addShortParameter(final Short value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.SMALLINT);
             } else {
                 statement.setShort(argumentIndex++, value);
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set short parameter on statement");
         }
     }
 
     public void addTimeParameter(final Date value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.TIME);
             } else {
                 statement.setTime(argumentIndex++, new Time(value.getTime()));
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set time parameter on statement");
         }
     }
 
     public void addTimestampParameter(final Date value) {
         Assert.assertNotNull("Trying to add parameter to StatementContext before statement has been set", statement);
 
         try {
             if (value == null) {
                 statement.setNull(argumentIndex++, Types.TIMESTAMP);
             } else {
                 statement.setTimestamp(argumentIndex++, new Timestamp(value.getTime()));
             }
         } catch (SQLException e) {
             LOG.error(e.getMessage(), e);
             throw new AssertionError("Failed to set short parameter on statement");
         }
     }
 
     public void closeStatement() {
         argumentIndex = 1;
 
         if (statement != null) {
             LOG.debug("closing statement");
 
             try {
                 statement.close();
             } catch (SQLException e) {
                 LOG.warn("Error closing database statement", e);
             } finally {
                 statement = null;
             }
         }
     }
 }
