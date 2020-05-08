 package com.sampullara.db;
 
 import junit.framework.TestCase;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.BrokenBarrierException;
 
 /**
  * Test single and multithreaded migration
  *
  * User: sam
  * Date: Sep 8, 2007
  * Time: 2:55:02 PM
  */
 public class MigrateTest extends TestCase {
 
     public void testMigration() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         Migrate migrate = new Migrate(p);
         Migrate.scriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/bootstrap.sql");
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Assert nothing needs to be done
         assertFalse(migrate.needsMigrate());
 
         // Do it again
         migrate.migrate();
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
         Migrate.scriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/bootstrap.sql");
 
         // Assert something needs to be done
         assertTrue(migrate.needsMigrate());
 
         // Do the migration
         migrate.migrate();
 
         // Assert nothing needs to be done
         assertFalse(migrate.needsMigrate());
 
         // Do it again
         migrate.migrate();
     }
 
     public void testMigrationOutOfSync() throws MigrationException, IOException {
         Properties p = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/test.properties");
         p.load(is);
         Migrate migrate = new Migrate(p);
 
         Migrate.scriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/bootstrap.sql");
 
         // Do the migration
         migrate.migrate();
 
         // Get the database out of sync
         Migrate.scriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/outofsync.sql");
 
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
         Migrate.scriptMigrator(migrate.getConnection(), "com/sampullara/test/migration/bootstrap.sql");
 
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
     }
 }
