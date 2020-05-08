 package org.specksensor;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import edu.cmu.ri.createlab.persistence.DatabaseUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * <p>
  * <code>DatabaseDataSampleStore</code> handles storage and retrieval of {@link Speck.DataSample data samples}, storing
  * them in a local database.
  * </p>
  * <p>By default, if the database doesn't exist it will be created in a subdirectory of the user's home directory
  * (e.g. <code>~/CREATELab/Speck/Speck00343135321504100f17/database</code>).  This can be overridden by specifying a
  * different directory in the <code>derby.system.home</code> system property.
  * </p>
  * <p>
  * Much of this code is taken from the Apache Derby project's <a href="http://svn.apache.org/repos/asf/db/derby/code/trunk/java/demo/simple/SimpleApp.java">SimpleApp example</a>.
  * </p>
  *
  * @author Chris Bartley (bartley@cmu.edu)
  * @author Apache Derby
  */
 final class DatabaseDataSampleStore implements DataSampleStore
    {
    private static final Logger LOG = Logger.getLogger(DatabaseDataSampleStore.class);
    private static final Logger CONSOLE_LOG = Logger.getLogger("ConsoleLog");
 
    private static final String DERBY_SYSTEM_HOME_PROPERTY_KEY = "derby.system.home";
 
    private static final String DATABASE_NAME = "db";
 
    private static final String DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String PROTOCOL = "jdbc:derby:";
 
    private static final String STATEMENT_NAME_INSERT_SAMPLE = "insert_sample";
    private static final String STATEMENT_INSERT_SAMPLE = "INSERT INTO SpeckSamples (raw_particle_count, particle_count, temperature, humidity, sample_timestamp_utc_secs, download_timestamp_utc_millis) VALUES (?, ?, ?, ?, ?, ?)";
 
    private static final String STATEMENT_NAME_UPDATE_ALL_SAMPLES_HAVING_STATUS = "update_all_samples_having_status";
    private static final String STATEMENT_UPDATE_ALL_SAMPLES_HAVING_STATUS = "UPDATE SpeckSamples SET UPLOAD_STATUS = ? WHERE UPLOAD_STATUS = ?";
 
   private static final String STATEMENT_NAME_SELECT_SAMPLES_HAVING_STATUS = "select_samples_having_status";
   private static final String STATEMENT_SELECT_SAMPLES_HAVING_STATUS = "SELECT\n" +
                                                                         "   *\n" +
                                                                         "FROM\n" +
                                                                         "   (SELECT\n" +
                                                                         "       ROW_NUMBER()\n" +
                                                                         "       OVER () AS NUM_ROWS,\n" +
                                                                         "       SpeckSamples.id,\n" +
                                                                         "       SpeckSamples.SAMPLE_TIMESTAMP_UTC_SECS,\n" +
                                                                         "       SpeckSamples.RAW_PARTICLE_COUNT,\n" +
                                                                         "       SpeckSamples.PARTICLE_COUNT,\n" +
                                                                         "       SpeckSamples.TEMPERATURE,\n" +
                                                                         "       SpeckSamples.HUMIDITY\n" +
                                                                         "    FROM SpeckSamples\n" +
                                                                        "    WHERE SpeckSamples.UPLOAD_STATUS = ?) AS TEMP\n" +
                                                                         "WHERE NUM_ROWS <= ?\n";
 
    private static final String SQL_STATE_DUPLICATE_KEY = "23505";
    private static final int SQL_ERROR_CODE_DUPLICATE_KEY = 30000;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
    /**
     * Returns the details of a {@link SQLException} chain as a {@link String}. Details included are SQL State, Error
     * code, Exception message.
     */
    private static String getSqlExceptionAsString(final SQLException sqlException)
       {
       final StringBuilder s = new StringBuilder(LINE_SEPARATOR);
 
       // Unwraps the entire exception chain to unveil the real cause of the Exception.
       SQLException e = sqlException;
       while (e != null)
          {
          s.append("----- SQLException -----").append(LINE_SEPARATOR);
          s.append("  SQL State:  ").append(e.getSQLState()).append(LINE_SEPARATOR);
          s.append("  Error Code: ").append(e.getErrorCode()).append(LINE_SEPARATOR);
          s.append("  Message:    ").append(e.getMessage()).append(LINE_SEPARATOR);
          e = e.getNextException();
          }
 
       return s.toString();
       }
 
    private Connection connection = null;
    private final Map<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
    private boolean isShutDown = false;
    private final Lock lock = new ReentrantLock();
 
    DatabaseDataSampleStore(@NotNull final SpeckConfig speckConfig) throws InitializationException
       {
       lock.lock();  // block until condition holds
       try
          {
          // Define the Derby home directory.  We do this because the database will be created under the directory
          // specified by the derby.system.home system property.
          if (LOG.isInfoEnabled())
             {
             LOG.info("DatabaseDataSampleStore.DatabaseDataSampleStore(): Previous value of System.getProperty(" + DERBY_SYSTEM_HOME_PROPERTY_KEY + ") = [" + System.getProperty(DERBY_SYSTEM_HOME_PROPERTY_KEY) + "]");
             }
          final File dataDirectory = SpeckConstants.FilePaths.getDeviceDataDirectory(speckConfig);
          final File databaseParentDirectory = new File(dataDirectory, "database");
          final File databaseDirectory = new File(databaseParentDirectory, DATABASE_NAME);
          System.setProperty(DERBY_SYSTEM_HOME_PROPERTY_KEY, databaseParentDirectory.getAbsolutePath());
          if (LOG.isInfoEnabled())
             {
             LOG.info("DatabaseDataSampleStore.DatabaseDataSampleStore(): New value of System.getProperty(" + DERBY_SYSTEM_HOME_PROPERTY_KEY + ") = [" + System.getProperty(DERBY_SYSTEM_HOME_PROPERTY_KEY) + "]");
             }
 
          // Make sure the database directory parent exists
          //noinspection ResultOfMethodCallIgnored
          databaseParentDirectory.mkdirs();
          if (!databaseParentDirectory.isDirectory())
             {
             LOG.fatal("DatabaseDataSampleStore.DatabaseDataSampleStore(): Could not create the database directory.  Aborting.");
             System.exit(1);
             }
 
          if (LOG.isInfoEnabled())
             {
             LOG.info("DatabaseDataSampleStore.DatabaseDataSampleStore(): Database will be stored in directory [" + databaseDirectory.getAbsolutePath() + "]");
             }
 
          // Load the JDBC driver
          if (!loadDriver())
             {
             LOG.fatal("DatabaseDataSampleStore.DatabaseDataSampleStore(): Could not load the database driver.  Aborting.");
             System.exit(1);
             }
 
          // Define connection properties. Providing a user name and password is optional in the embedded framework, but,
          // by default, the schema APP will be used when no username is provided. Otherwise, the schema name is the same
          // as the user name.
          final Properties properties = new Properties();
          properties.put("user", "speck");
          properties.put("password", "speck");
 
          try
             {
             // This connection specifies create=true in the connection URL to cause the database to be created when connecting
             // for the first time. To remove the database, remove the directory derbyDB (the same as the database name) and
             // its contents.
             connection = DriverManager.getConnection(PROTOCOL + DATABASE_NAME + ";create=true", properties);
             }
          catch (SQLException e)
             {
             final String message = "SQLException while trying to create the database connection: " + getSqlExceptionAsString(e);
             LOG.error("DatabaseDataSampleStore.DatabaseDataSampleStore(): " + message, e);
             CONSOLE_LOG.error(message);
             }
 
          boolean wasSetupSuccessful = false;
          if (connection != null)
             {
             try
                {
                // We want transactions committed for us automatically. Autocommit is on by default in JDBC, but
                // there's no harm in making it explicit here.
                connection.setAutoCommit(true);
 
                // Creates tables, if necessary
                initializeDatabase(connection);
 
                // create prepared statements for insert and update
                preparedStatements.put(STATEMENT_NAME_INSERT_SAMPLE, connection.prepareStatement(STATEMENT_INSERT_SAMPLE));
                preparedStatements.put(STATEMENT_NAME_UPDATE_ALL_SAMPLES_HAVING_STATUS, connection.prepareStatement(STATEMENT_UPDATE_ALL_SAMPLES_HAVING_STATUS));
               preparedStatements.put(STATEMENT_NAME_SELECT_SAMPLES_HAVING_STATUS, connection.prepareStatement(STATEMENT_SELECT_SAMPLES_HAVING_STATUS));
 
                wasSetupSuccessful = true;
                }
             catch (SQLException e)
                {
                final String message = "SQLException while trying to configure or initialize the database: " + getSqlExceptionAsString(e);
                LOG.error("DatabaseDataSampleStore.DatabaseDataSampleStore(): " + message, e);
                CONSOLE_LOG.error(message);
                }
 
             if (LOG.isInfoEnabled() || CONSOLE_LOG.isInfoEnabled())
                {
                final String message = "Connected to and created database " + DATABASE_NAME;
                LOG.info("DatabaseDataSampleStore.DatabaseDataSampleStore(): " + message);
                CONSOLE_LOG.info(message);
                }
             }
 
          if (!wasSetupSuccessful)
             {
             throw new InitializationException("Failed to create, configure, or initialize the database");
             }
          }
       finally
          {
          lock.unlock();
          }
       }
 
    @Override
    @NotNull
    public SaveResult save(@NotNull final Speck.DataSample dataSample)
       {
       lock.lock();  // block until condition holds
       try
          {
          final PreparedStatement insertStatement = preparedStatements.get(STATEMENT_NAME_INSERT_SAMPLE);
 
          boolean isDuplicate = false;
          if (insertStatement != null)
             {
             try
                {
                insertStatement.setInt(1, dataSample.getRawParticleCount());
                insertStatement.setInt(2, dataSample.getParticleCount());
                insertStatement.setInt(3, dataSample.getTemperatureInTenthsOfADegreeF());
                insertStatement.setInt(4, dataSample.getHumidity());
                insertStatement.setInt(5, dataSample.getSampleTime());
                insertStatement.setLong(6, dataSample.getDownloadTime());
                insertStatement.executeUpdate();
 
                if (LOG.isDebugEnabled())
                   {
                   LOG.debug("DatabaseDataSampleStore.save(): Saved data sample [" + dataSample.getSampleTime() + "] to the database.");
                   }
                return SaveResult.SUCCESS;
                }
             catch (SQLException e)
                {
                if (e.getErrorCode() == SQL_ERROR_CODE_DUPLICATE_KEY && SQL_STATE_DUPLICATE_KEY.equals(e.getSQLState()))
                   {
                   LOG.error("DatabaseDataSampleStore.save(): Saved failed because a sample with timestamp [" + dataSample.getSampleTime() + "] already exists.  Duplicate sample timestamps are not allowed.");
                   isDuplicate = true;
                   }
                else
                   {
                   LOG.error("DatabaseDataSampleStore.save(): SQLException while trying to save data sample [" + dataSample.getSampleTime() + "] " + getSqlExceptionAsString(e));
                   }
                }
             }
          else
             {
             LOG.error("DatabaseDataSampleStore.save(): Save failed because no insert statement is defined!");
             }
 
          return (isDuplicate) ? SaveResult.FAILURE_DUPLICATE : SaveResult.FAILURE_ERROR;
          }
       finally
          {
          lock.unlock();
          }
       }
 
    @Override
    public void resetStateOfUploadingSamples()
       {
       lock.lock();  // block until condition holds
       try
          {
          final PreparedStatement updateStatement = preparedStatements.get(STATEMENT_NAME_UPDATE_ALL_SAMPLES_HAVING_STATUS);
          if (updateStatement != null)
             {
             try
                {
                updateStatement.setString(1, DataSampleUploadStatus.NOT_ATTEMPTED.getName());
                updateStatement.setString(2, DataSampleUploadStatus.IN_PROGRESS.getName());
                updateStatement.executeUpdate();
 
                if (LOG.isDebugEnabled())
                   {
                   LOG.debug("DatabaseDataSampleStore.resetStateOfUploadingSamples(): Reset all data sample with upload status of [" + DataSampleUploadStatus.IN_PROGRESS + "] to [" + DataSampleUploadStatus.NOT_ATTEMPTED + "].");
                   }
                }
             catch (SQLException e)
                {
                LOG.error("DatabaseDataSampleStore.save(): SQLException while trying to reset upload status of all [" + DataSampleUploadStatus.IN_PROGRESS + "] samples", e);
                }
             }
          else
             {
             LOG.error("DatabaseDataSampleStore.resetStateOfUploadingSamples(): Reset failed because no update statement is defined!");
             }
          }
       finally
          {
          lock.unlock();
          }
       }
 
    @NotNull
    @Override
    public DataSampleSet getDataSamplesToUpload(final int maxNumberRequested)
       {
       lock.lock();  // block until condition holds
       try
          {
          final SortedSet<Speck.DataSample> dataSamples = new TreeSet<Speck.DataSample>();
         final PreparedStatement selectStatement = preparedStatements.get(STATEMENT_NAME_SELECT_SAMPLES_HAVING_STATUS);
          if (selectStatement != null)
             {
             final int maxNumberToGet = (maxNumberRequested < 1) ? DataSampleSet.DEFAULT_SIZE : maxNumberRequested;
             try
                {
               selectStatement.setString(1, DataSampleUploadStatus.NOT_ATTEMPTED.getName());
               selectStatement.setInt(2, maxNumberToGet);
 
                final ResultSet resultSet = selectStatement.executeQuery();
 
                // Build up our DataSampleSet, but also build the List of IDs so we can create
                // a query to mark all these samples' upload state as IN_PROGRESS.
                final List<Integer> ids = new ArrayList<Integer>();
                while (resultSet.next())
                   {
                   final int id = resultSet.getInt(2);
                   ids.add(id);
                   dataSamples.add(new DataSample(id,                         // databaseId
                                                  resultSet.getInt(3),        // sampleTimeUtcSeconds
                                                  resultSet.getInt(4),        // rawParticleCount
                                                  resultSet.getInt(5),        // particleCount
                                                  resultSet.getInt(6),        // temperatureInTenthsOfDegreeF
                                                  resultSet.getInt(7)));      // humidity
                   }
 
                // if the update failed, then we should just return an empty DataSampleSet
                if (!markDataSamplesWithStatus(ids, DataSampleUploadStatus.IN_PROGRESS, null))
                   {
                   dataSamples.clear();
                   }
                }
             catch (SQLException e)
                {
                LOG.error("DatabaseDataSampleStore.getDataSamplesToUpload(): SQLException while trying to get data samples to upload", e);
                }
             }
          else
             {
             LOG.error("DatabaseDataSampleStore.resetStateOfUploadingSamples(): Reset failed because no update statement is defined!");
             }
 
          return new DataSampleSetImpl(dataSamples);
          }
       finally
          {
          lock.unlock();
          }
       }
 
    @Override
    public void markDataSamplesAsUploaded(@NotNull final DataSampleSet dataSampleSet, final long uploadTimestampUtcMillis)
       {
       lock.lock();  // block until condition holds
       try
          {
          markDataSamplesWithStatus(dataSampleSet, DataSampleUploadStatus.SUCCESS, uploadTimestampUtcMillis);
          }
       finally
          {
          lock.unlock();
          }
       }
 
    @Override
    public void markDataSamplesAsFailed(@NotNull final DataSampleSet dataSampleSet)
       {
       lock.lock();  // block until condition holds
       try
          {
          markDataSamplesWithStatus(dataSampleSet, DataSampleUploadStatus.FAILURE, null);
          }
       finally
          {
          lock.unlock();
          }
       }
 
    /**
     * Marks the given samples with the given status.  MUST be called from within a lock block. Returns <code>true</code>
     * upon success, <code>false</code> otherwise.
     */
    private boolean markDataSamplesWithStatus(@NotNull final DataSampleSet dataSampleSet, @NotNull final DataSampleUploadStatus status, @Nullable final Long timestampUtcMillis)
       {
       if (!dataSampleSet.isEmpty())
          {
          // build a list of the ids
          final List<Integer> ids = new ArrayList<Integer>();
          for (final Speck.DataSample sample : dataSampleSet.getDataSamples())
             {
             final Integer id = sample.getDatabaseId();
             if (id != null)
                {
                ids.add(id);
                }
             }
          return markDataSamplesWithStatus(ids, status, timestampUtcMillis);
          }
       return false;
       }
 
    /**
     * Marks the samples associated with the given IDs with the given status.  MUST be called from within a lock block.
     * Returns <code>true</code> upon success, <code>false</code> otherwise.
     */
    private boolean markDataSamplesWithStatus(@NotNull final List<Integer> dataSamplesIds, @NotNull final DataSampleUploadStatus status, @Nullable final Long timestampUtcMillis)
       {
       boolean wasSuccessful = false;
       if (!dataSamplesIds.isEmpty())
          {
          Statement updateStatement = null;
          try
             {
             final String updateSql = "UPDATE SpeckSamples " +
                                      "SET " +
                                      "   UPLOAD_STATUS='" + status.getName() + "', " +
                                      "   UPLOAD_TIMESTAMP_UTC_MILLIS=" + timestampUtcMillis + " " +
                                      "WHERE ID IN (" + StringUtils.join(dataSamplesIds, ",") + ")";
 
             updateStatement = connection.createStatement();
             updateStatement.executeUpdate(updateSql);
             wasSuccessful = true;
             }
          catch (SQLException e)
             {
             LOG.error("DatabaseDataSampleStore.markDataSamplesWithStatus(): SQLException while trying to mark data samples as " + status, e);
             }
          finally
             {
             closeStatement(updateStatement);
             }
          }
 
       if (LOG.isInfoEnabled() && !dataSamplesIds.isEmpty())
          {
          if (wasSuccessful)
             {
             LOG.info("DatabaseDataSampleStore.markDataSamplesWithStatus(): Marked [" + dataSamplesIds.size() + "] samples' upload status as " + status);
             }
          else
             {
             LOG.info("DatabaseDataSampleStore.markDataSamplesWithStatus(): Failed to mark [" + dataSamplesIds.size() + "] samples' upload status as " + status);
             }
          }
 
       return wasSuccessful;
       }
 
    public void shutdown()
       {
       lock.lock();  // block until condition holds
       try
          {
          if (!isShutDown)
             {
             LOG.debug("DatabaseDataSampleStore.shutdown(): Shutting down...");
             CONSOLE_LOG.info("Shutting down the database...");
             try
                {
                // the shutdown=true attribute shuts down Derby
                DriverManager.getConnection(PROTOCOL + ";shutdown=true");
                }
             catch (SQLException e)
                {
                if (((e.getErrorCode() == 50000) && ("XJ015".equals(e.getSQLState()))))
                   {
                   // we got the expected exception
                   LOG.info("DatabaseDataSampleStore.shutdown(): Derby shut down normally");
                   CONSOLE_LOG.info("Database shut down normally");
                   }
                else
                   {
                   // if the error code or SQLState is different, we have
                   // an unexpected exception (shutdown failed)
                   LOG.error("DatabaseDataSampleStore.shutdown(): Derby did not shut down normally: " + getSqlExceptionAsString(e), e);
                   CONSOLE_LOG.error("Database did not shut down normally: " + getSqlExceptionAsString(e));
                   }
                }
 
             // close the prepared statements
             for (final String statementName : preparedStatements.keySet())
                {
                if (!closeStatement(preparedStatements.get(statementName)))
                   {
                   LOG.error("DatabaseDataSampleStore.shutdown(): Failed to close the [" + statementName + "] statement.");
                   }
                }
             preparedStatements.clear();
 
             // close the connection
             try
                {
                if (connection != null)
                   {
                   connection.close();
                   connection = null;
                   LOG.info("DatabaseDataSampleStore.shutdown(): Connection closed normally");
                   }
                }
             catch (SQLException e)
                {
                LOG.error("DatabaseDataSampleStore.shutdown(): SQLException while closing the connection" + getSqlExceptionAsString(e));
                }
 
             isShutDown = true;
             }
          }
       finally
          {
          lock.unlock();
          }
       }
 
    /**
     * Loads the driver specified by {@link #DRIVER_NAME} and returns <code>true</code> upon success, <code>false</code>
     * otherwise.  Note that any static Derby system properties must be set before loading the driver in order for them
     * to have any effect.
     */
    private boolean loadDriver()
       {
         /*
          *  The JDBC driver is loaded by loading its class.
          *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
          *  be automatically loaded, making this code optional.
          *
          *  In an embedded environment, this will also start up the Derby
          *  engine (though not any databases), since it is not already
          *  running. In a client environment, the Derby engine is being run
          *  by the network server framework.
          *
          *  In an embedded environment, any static Derby system properties
          *  must be set before loading the driver to take effect.
          */
       try
          {
          Class.forName(DRIVER_NAME).newInstance();
          if (LOG.isInfoEnabled())
             {
             LOG.info("DatabaseDataSampleStore.loadDriver(): Loaded JDBC driver " + DRIVER_NAME);
             }
          return true;
          }
       catch (ClassNotFoundException e)
          {
          LOG.error("DatabaseDataSampleStore.loadDriver(): ClassNotFoundException while trying to load the JDBC driver " + DRIVER_NAME, e);
          }
       catch (InstantiationException e)
          {
          LOG.error("DatabaseDataSampleStore.loadDriver(): InstantiationException while trying to load the JDBC driver " + DRIVER_NAME, e);
          }
       catch (IllegalAccessException e)
          {
          LOG.error("DatabaseDataSampleStore.loadDriver(): IllegalAccessException while trying to load the JDBC driver " + DRIVER_NAME, e);
          }
       catch (Exception e)
          {
          LOG.error("DatabaseDataSampleStore.loadDriver(): Exception while trying to load the JDBC driver " + DRIVER_NAME, e);
          }
 
       return false;
       }
 
    private void initializeDatabase(@NotNull final Connection connection) throws SQLException
       {
       if (!DatabaseUtils.doesTableExist(connection, "SpeckSamples"))
          {
          Statement statement = null;
 
          try
             {
             LOG.debug("DatabaseDataSampleStore.initializeDatabase(): Creating table SpeckSamples...");
             statement = connection.createStatement();
             statement.execute("CREATE TABLE SpeckSamples (\n" +
                               "   id                            INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n" +
                               "   raw_particle_count            INTEGER     NOT NULL,\n" +
                               "   particle_count                INTEGER     NOT NULL,\n" +
                               "   temperature                   INTEGER     NOT NULL,\n" +
                               "   humidity                      INTEGER     NOT NULL,\n" +
                               "   sample_timestamp_utc_secs     INTEGER     NOT NULL,\n" +
                               "   download_timestamp_utc_millis BIGINT      NOT NULL,\n" +
                               "   upload_timestamp_utc_millis   BIGINT,\n" +
                               "   upload_status                 VARCHAR(13) NOT NULL DEFAULT 'not_attempted',\n" +
                               "   CONSTRAINT SpeckSamples_PrimaryKey PRIMARY KEY (id),\n" +
                               "   CONSTRAINT SpeckSamples_SampleTimestamp_Unique UNIQUE (sample_timestamp_utc_secs),\n" +
                               "   CONSTRAINT SpeckSamples_StatusContraint CHECK (upload_status IN\n" +
                               "                                                   ('not_attempted',\n" +
                               "                                                    'in_progress',\n" +
                               "                                                    'success',\n" +
                               "                                                    'failure'))\n" +
                               ")");
 
             LOG.debug("DatabaseDataSampleStore.initializeDatabase(): Creating indeces for table SpeckSamples...");
 
             statement.execute("CREATE INDEX SpeckSamples_RawParticleCount ON SpeckSamples (raw_particle_count)");
             statement.execute("CREATE INDEX SpeckSamples_ParticleCount ON SpeckSamples (particle_count)");
             statement.execute("CREATE INDEX SpeckSamples_Temperature ON SpeckSamples (temperature)");
             statement.execute("CREATE INDEX SpeckSamples_Humidity ON SpeckSamples (humidity)");
             statement.execute("CREATE INDEX SpeckSamples_DownloadTimestamp ON SpeckSamples (download_timestamp_utc_millis)");
             statement.execute("CREATE INDEX SpeckSamples_UploadTimestamp ON SpeckSamples (upload_timestamp_utc_millis)");
             statement.execute("CREATE INDEX SpeckSamples_UploadStatus ON SpeckSamples (upload_status)");
 
             LOG.debug("DatabaseDataSampleStore.initializeDatabase(): Database initialization complete!");
             }
          finally
             {
             closeStatement(statement);
             }
          }
       }
 
    private boolean closeStatement(@Nullable final Statement statement)
       {
       try
          {
          if (statement != null)
             {
             statement.close();
             return true;
             }
          }
       catch (SQLException e)
          {
          LOG.error("DatabaseDataSampleStore.closeStatement(): SQLException while trying to close the statment.  Oh well.", e);
          }
       return false;
       }
    }
