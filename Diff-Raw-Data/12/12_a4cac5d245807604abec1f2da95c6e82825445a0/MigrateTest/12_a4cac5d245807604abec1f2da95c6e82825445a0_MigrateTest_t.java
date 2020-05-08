 package com.sampullara.db;
 
 import junit.framework.TestCase;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 import javax.sql.DataSource;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * Test single and multithreaded migration
  * <p/>
  * User: sam
  * Date: Sep 8, 2007
  * Time: 2:55:02 PM
  */
 public class MigrateTest extends TestCase {
 
     public void testMigrationWithDataSource() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         String datasourceClassname = (String)p.get("datasourceClass");
         if (datasourceClassname != null) {
             String pkg = (String)p.get("package");
             int version = Integer.parseInt((String)(p.get("version")));
 
             try {
                 Class datasourceClass = Class.forName(datasourceClassname);
                 DataSource datasource = (DataSource)datasourceClass.newInstance();
                 Class[] types = new Class[] { String.class };
                 String url = (String)p.get("datasource.url");
                 if (url != null) {
                     Method setUrl = datasourceClass.getMethod("setURL",types);
                     setUrl.invoke(datasource,url);
                 }
 
                 String user = (String)p.get("datasource.user");
                 if (user != null)  {
                     Method setUser = datasourceClass.getMethod("setUser",types);
                     setUser.invoke(datasource,user);
                 }
 
                 String password = (String)p.get("datasource.password");
                 if (password != null) {
                     Method setPassword = datasourceClass.getMethod("setPassword",types);
                     setPassword.invoke(datasource,password);
                 }
 
 
                 String databaseName = (String)p.get("datasource.databaseName");
                 if (databaseName != null) {
                     Method setDatabaseName = datasourceClass.getMethod("setDatabaseName",types);
                     setDatabaseName.invoke(datasource,databaseName);
                 }
 
                 Migrate migrate = new Migrate(pkg,datasource,version);
                 //            Migrate migrate = new Migrate(
                 dropTable(migrate);
 
                 // Assert something needs to be done
                 assertTrue(migrate.needsMigrate());
 
                 // Do the migration
                 migrate.migrate();
 
                 // Assert nothing needs to be done
                 assertFalse(migrate.needsMigrate());
 
                 // Do it again
                 migrate.migrate();
 
             } catch (ClassNotFoundException e ) {
                 fail("could not find data source class: " + datasourceClassname);
             } catch (InstantiationException e) {
                 fail("could not instantiate data source class: " + datasourceClassname);
             } catch (IllegalAccessException e) {
                 fail("could not access data source class: " + datasourceClassname);
             } catch (NoSuchMethodException e) {
                 fail("failed to invoke setter on data source class: " + datasourceClassname);
             } catch (InvocationTargetException e) {
                 fail("datasource setter threw an exception: " + e);
             }
 
 
         } else {
             // no datasourceClass specified in the properties so ignore for this test
         }
     }
 
     public void testMigration() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         Migrate migrate = new Migrate(p);
         dropTable(migrate);
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Assert nothing needs to be done
         assertFalse(migrate.needsMigrate());
 
         // Do it again
         migrate.migrate();
     }
 
     private void dropTable(Migrate migrate) {
         try {
             Migrate.sqlScriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/bootstrap.sql");
         } catch (MigrationException me) {
             // Ignore if the drop table is unsuccessful
         }
     }
 
     public void testAutoMigration() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         p.put("version", "2");
 
         Migrate migrate = new Migrate(p);
         dropTable(migrate);
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Make sure it worked
         assertEquals(2, migrate.getDBVersion());
 
         // Now turn on auto and migrate
         p.remove("version");
         p.put("auto", "true");
         migrate = new Migrate(p);
         migrate.migrate();
 
         // Make sure it worked
         assertEquals(6, migrate.getDBVersion());
 
         // Assert nothing was done since we are auto migrating
         assertFalse(migrate.migrate());
     }
 
     public void testFullAutoMigration() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
        p.remove("version");
         p.put("auto", "true");
 
         Migrate migrate = new Migrate(p);
         dropTable(migrate);
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Make sure it worked
         assertEquals(6, migrate.getDBVersion());
 
         // Assert nothing was done since we are auto migrating
         assertFalse(migrate.migrate());
     }
 
     public void testFileMigration() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         p.put("auto", "true");
         p.put("package", "test/com/sampullara/test/migration");
         p.remove("version");
 
         Migrate migrate = new Migrate(p);
         dropTable(migrate);
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Make sure it worked
         assertEquals(6, migrate.getDBVersion());
 
         // Assert nothing was done since we are auto migrating
         assertFalse(migrate.migrate());
     }
 
     public void testMigrationCommandLine() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
 
         String[] commandline = new String[] {
                 "-url", p.getProperty("url", ""),
                 "-driver", p.getProperty("driver", ""),
                 "-user", p.getProperty("user", ""),
                 "-password", p.getProperty("password", ""),
                 "-version", p.getProperty("version", ""),
                 "-package", p.getProperty("package", ""),
         };
 
         Migrate migrate = new Migrate(commandline);
         dropTable(migrate);
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Assert nothing needs to be done
         assertFalse(migrate.needsMigrate());
 
         // Do it again
         migrate.migrate();
 
         // Make sure it worked
         assertEquals(6, migrate.getDBVersion());
     }
 
     public void testMigrationOutOfSync() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         Migrate migrate = new Migrate(p);
         dropTable(migrate);
 
         // Do the migration
         migrate.migrate();
 
         // Make sure it worked
         assertEquals(6, migrate.getDBVersion());
 
         // Get the database out of sync
         Migrate.sqlScriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/outofsync.sql");
 
         // Expect an exception
         try {
             migrate.migrate();
             fail("Migration succeeded and should have failed");
         } catch (MigrationException e) {
             // success
         }
     }
 
     private int migrations = 0;
 
     public void testMultithreaded() throws MigrationException, IOException, InterruptedException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         p.put("version", "1");
         Migrate migrate = new Migrate(p);
 
         // Bootstrap the database by removing the old version table
         dropTable(migrate);
 
         // Migrate to version 1
         migrate.migrate();
 
         final CyclicBarrier lock = new CyclicBarrier(3);
 
         Runnable run = new Runnable() {
             public void run() {
                 try {
                     Properties p = new Properties();
                     InputStream is =
                             Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
                     p.load(is);
                     Migrate migrate = new Migrate(p);
 
                     lock.await();
 
                     // We will actually migrate only once
                     if (migrate.migrate()) migrations++;
 
                     // Assert nothing needs to be done
                     assertFalse(migrate.needsMigrate());
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (MigrationException e) {
                     e.printStackTrace();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 } catch (BrokenBarrierException e) {
                     e.printStackTrace();
                 }
             }
         };
         Thread thread1 = new Thread(run);
         Thread thread2 = new Thread(run);
         Thread thread3 = new Thread(run);
         thread1.start();
         thread2.start();
         thread3.start();
         thread1.join();
         thread2.join();
         thread3.join();
         assertEquals(1, migrations);
 
         // Make sure it worked
         assertEquals(6, migrate.getDBVersion());
     }
 }
