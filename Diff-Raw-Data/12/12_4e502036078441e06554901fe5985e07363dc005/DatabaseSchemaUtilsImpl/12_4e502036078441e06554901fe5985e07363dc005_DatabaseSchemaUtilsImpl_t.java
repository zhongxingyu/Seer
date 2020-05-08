 package net.madz.db.core.impl;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.xml.bind.JAXBException;
 
 import net.madz.db.core.AbsDatabaseGenerator;
 import net.madz.db.core.AbsSchemaMetaDataParser;
 import net.madz.db.core.DatabaseSchemaUtils;
 import net.madz.db.core.IllegalOperationException;
 import net.madz.db.core.SchemaMetaDataComparator;
 import net.madz.db.core.meta.immutable.ColumnMetaData;
 import net.madz.db.core.meta.immutable.ForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.IndexMetaData;
 import net.madz.db.core.meta.immutable.SchemaMetaData;
 import net.madz.db.core.meta.immutable.TableMetaData;
 import net.madz.db.utils.LogUtils;
 import net.madz.db.utils.MessageConsts;
 
 public class DatabaseSchemaUtilsImpl<SMD extends SchemaMetaData<SMD, TMD, CMD, FMD, IMD>, TMD extends TableMetaData<SMD, TMD, CMD, FMD, IMD>, CMD extends ColumnMetaData<SMD, TMD, CMD, FMD, IMD>, FMD extends ForeignKeyMetaData<SMD, TMD, CMD, FMD, IMD>, IMD extends IndexMetaData<SMD, TMD, CMD, FMD, IMD>>
         implements DatabaseSchemaUtils {
 
     @Override
     public boolean databaseExists(String databaseName, boolean isCopy) throws SQLException {
         validateDatabaseName(databaseName);
         Connection conn = null;
         boolean result = false;
         try {
             if ( isCopy ) {
                 // TODO [Jan 22, 2013][barry][Done] Close Resources Connection
                 conn = DbConfigurationManagement.createConnection(databaseName, true);
                 ResultSet rs = null;
                 try {
                     Statement stmt = conn.createStatement();
                     // TODO [Jan 22, 2013][barry][Done] Close Resources
                     // ResultSet
                     rs = stmt.executeQuery("SHOW DATABASES");
                     while ( rs.next() ) {
                         String dbName = rs.getString("Database");
                         if ( databaseName.equals(dbName) ) {
                             result = true;
                         }
                     }
                     return result;
                 } catch (SQLException e) {
                     LogUtils.debug(this.getClass(), e.getMessage());
                     throw e;
                 } finally {
                     try {
                         if ( null != rs && !rs.isClosed() ) {
                             rs.close();
                         }
                     } catch (SQLException ignored) {
                         LogUtils.debug(this.getClass(), ignored.getMessage());
                     }
                 }
             } else {
                 // TODO [Jan 22, 2013][barry][Done]Close Resources Connection
                 // For non-mysql resource, if connection is created, which
                 // represents the database exists.
                 conn = DbConfigurationManagement.createConnection(databaseName, false);
                 result = true;
                return result;
             }
         } finally {
             try {
                 if ( null != conn && !conn.isClosed() ) {
                     conn.close();
                 }
             } catch (final SQLException e) {
                 LogUtils.debug(this.getClass(), e.getMessage());
             }
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public boolean compareDatabaseSchema(String sourceDatabaseName, String targetDatabaseName) throws SQLException {
        if ( !databaseExists(sourceDatabaseName, false) ) {
            return false;
        }
        if ( !databaseExists(targetDatabaseName, true) ) {
            return false;
        }
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final AbsSchemaMetaDataParser<SMD, TMD, CMD, FMD, IMD> sourceDbParser = DbOperatorFactoryImpl.getInstance().createSchemaParser(sourceDatabaseName,
                 false);
         final AbsSchemaMetaDataParser<SMD, TMD, CMD, FMD, IMD> targetDbParser = DbOperatorFactoryImpl.getInstance()
                 .createSchemaParser(targetDatabaseName, true);
         final SchemaMetaDataComparator databaseComparator = DbOperatorFactoryImpl.getInstance().createDatabaseComparator(sourceDatabaseName);
         final SMD sourceSchemaMetaData = sourceDbParser.parseSchemaMetaData();
         final SMD targetSchemaMetaData = targetDbParser.parseSchemaMetaData();
         return databaseComparator.compare(sourceSchemaMetaData, targetSchemaMetaData);
     }
 
     @Override
     public String cloneDatabaseSchema(String sourceDatabaseName, String targetDatabaseName) throws IllegalOperationException, SQLException, JAXBException {
         validateDatabaseName(sourceDatabaseName);
         if ( null == targetDatabaseName || 0 >= targetDatabaseName.length() ) {
             targetDatabaseName = sourceDatabaseName + "_copy";
         }
         if ( !databaseExists(sourceDatabaseName, false) ) {
             // TODO [Jan 22, 2013][barry][Done] Should the text error message
             // distributed everywhere?
             throw new IllegalOperationException(MessageConsts.CONFIGURE_DATABASE_INFO);
         }
         validateServerConfigurations(sourceDatabaseName);
         if ( databaseExists(targetDatabaseName, true) ) {
             dropDatabase(targetDatabaseName);
         }
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final AbsSchemaMetaDataParser<SMD, TMD, CMD, FMD, IMD> sourceDbParser = DbOperatorFactoryImpl.getInstance().createSchemaParser(sourceDatabaseName,
                 false);
         final SMD schemaMetaData = sourceDbParser.parseSchemaMetaData();
         // Upload schemaMetaData to to writer (local or remote)
         //
         final AbsDatabaseGenerator<SMD, TMD, CMD, FMD, IMD> databaseGenerator = DbOperatorFactoryImpl.getInstance().createDatabaseGenerator(targetDatabaseName);
         final String databaseName = databaseGenerator.generateDatabase(schemaMetaData, DbConfigurationManagement.createConnection(targetDatabaseName, true),
                 targetDatabaseName);
         DbConfigurationManagement.addDatabaseInfo(targetDatabaseName);
         return databaseName;
     }
 
     @Override
     public boolean dropDatabase(String databaseName) throws JAXBException, SQLException {
         validateDatabaseName(databaseName);
         if ( databaseExists(databaseName, true) ) {
             Connection conn = DbConfigurationManagement.createConnection(databaseName, true);
             Statement stmt;
             try {
                 stmt = conn.createStatement();
                 stmt.execute("DROP DATABASE " + databaseName + ";");
             } catch (SQLException ignored) {
                 // TODO [Jan 22, 2013][barry][Done] use log instead at least and
                 // declare ignore
                 LogUtils.debug(this.getClass(), ignored.getMessage());
                 return false;
             }
             DbConfigurationManagement.removeDatabaseInfo(databaseName);
             return true;
         }
         DbConfigurationManagement.removeDatabaseInfo(databaseName);
         return false;
     }
 
     public void validateDatabaseName(String databaseName) {
         if ( null == databaseName || 0 >= databaseName.trim().length() ) {
             throw new IllegalArgumentException(MessageConsts.DATABASE_NAME_SHOULD_NOT_BE_NULL);
         }
     }
 
     private void validateServerConfigurations(String databaseName) throws SQLException {
         Connection conn = null, conn2 = null;
         conn = DbConfigurationManagement.createConnection(databaseName, false);
         conn2 = DbConfigurationManagement.createConnection("", true);
         Statement stmt = null, stmt2 = null;
         ResultSet rs = null, rs2 = null;
         int one = 0, other = 0;
         try {
             stmt = conn.createStatement();
             stmt2 = conn2.createStatement();
             try {
                 rs = stmt.executeQuery("SHOW VARIABLES LIKE 'lower_case_table_names';");
                 rs2 = stmt2.executeQuery("SHOW VARIABLES LIKE 'lower_case_table_names';");
                 while ( rs.next() ) {
                     one = rs.getInt("Value");
                 }
                 while ( rs2.next() ) {
                     other = rs2.getInt("Value");
                 }
             } finally {
                 rs.close();
                 rs2.close();
             }
         } catch (SQLException e) {
             throw new IllegalStateException(e);
         }
         if ( one != other ) {
             throw new IllegalStateException(MessageConsts.LOWER_CASE_TABLE_NAMES_MUST_BE_SAME + "The value of source server is: " + one
                     + ", the value of target server is:" + other);
         }
     }
 }
