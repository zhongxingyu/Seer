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
 import net.madz.db.metadata.jdbc.JdbcSchemaMetaData;
 import net.madz.db.utils.LogUtils;
 import net.madz.db.utils.MessageConsts;
 
 public class DatabaseSchemaUtilsImpl implements DatabaseSchemaUtils {
 
     @Override
     public boolean databaseExists(String databaseName, boolean isCopy) {
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
                     rs = stmt.executeQuery("show databases");
                     while ( rs.next() ) {
                         String dbName = rs.getString("Database");
                         if ( databaseName.equals(dbName) ) {
                             result = true;
                         }
                     }
                     return result;
                 } catch (SQLException e) {
                     LogUtils.debug(this.getClass(), e.getMessage());
                 } finally {
                     try {
                         rs.close();
                     } catch (SQLException e) {
                         LogUtils.debug(this.getClass(), e.getMessage());
                     }
                 }
             } else {
                 // TODO [Jan 22, 2013][barry][Done]Close Resources Connection
                 // For non-mysql resource, if connection is created, which
                 // represents the database exists.
                 conn = DbConfigurationManagement.createConnection(databaseName, false);
                 result = true;
             }
             return result;
         } finally {
             try {
                 conn.close();
             } catch (final SQLException e) {
                 LogUtils.debug(this.getClass(), e.getMessage());
             }
         }
     }
 
     @Override
     public boolean compareDatabaseSchema(String sourceDatabaseName, String targetDatabaseName) throws SQLException {
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final AbsSchemaMetaDataParser sourceDbParser = DbOperatorFactoryImpl.getInstance().createSchemaParser(sourceDatabaseName, false);
         final AbsSchemaMetaDataParser targetDbParser = DbOperatorFactoryImpl.getInstance().createSchemaParser(targetDatabaseName, true);
         final JdbcSchemaMetaData sourceSchemaMetaData = sourceDbParser.parseSchemaMetaData();
         final JdbcSchemaMetaData targetSchemaMetaData = targetDbParser.parseSchemaMetaData();
         return sourceSchemaMetaData.equals(targetSchemaMetaData);
     }
 
     @Override
     public String cloneDatabaseSchema(String sourceDatabaseName, String targetDatabaseName) throws IllegalOperationException, SQLException, JAXBException {
         if ( !databaseExists(sourceDatabaseName, false) ) {
             // TODO [Jan 22, 2013][barry][Done] Should the text error message
             // distributed everywhere?
             throw new IllegalOperationException(MessageConsts.CONFIGURE_DATABASE_INFO);
         }
         if ( databaseExists(targetDatabaseName, true) ) {
             dropDatabase(targetDatabaseName);
         }
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final AbsSchemaMetaDataParser sourceDbParser = DbOperatorFactoryImpl.getInstance().createSchemaParser(sourceDatabaseName, false);
         final JdbcSchemaMetaData schemaMetaData = sourceDbParser.parseSchemaMetaData();
         // Upload schemaMetaData to to writer (local or remote)
         //
         final AbsDatabaseGenerator databaseGenerator = DbOperatorFactoryImpl.getInstance().createDatabaseGenerator(targetDatabaseName);
         final String databaseName = databaseGenerator.generateDatabase(schemaMetaData, targetDatabaseName);
         return databaseName;
     }
 
     @Override
     public boolean dropDatabase(String databaseName) throws JAXBException {
         if ( databaseExists(databaseName, true) ) {
             Connection conn = DbConfigurationManagement.createConnection(databaseName, true);
             Statement stmt;
             try {
                 stmt = conn.createStatement();
                 stmt.execute("drop database " + databaseName + ";");
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
 
     public static void main(String[] args) {
         DatabaseSchemaUtilsImpl impl = new DatabaseSchemaUtilsImpl();
         // boolean result = impl.compareDatabaseSchema("crmp", "crmp2");
         // System.out.println(result);
         try {
             impl.cloneDatabaseSchema("c3", "crmp2000");
         } catch (IllegalOperationException e) {
             e.printStackTrace();
         } catch (SQLException e) {
             e.printStackTrace();
         } catch (JAXBException e) {
             e.printStackTrace();
         }
     }
 }
