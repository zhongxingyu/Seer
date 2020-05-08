 package org.dbmigaret4j.migration;
 
 import org.jailsframework.database.IDatabase;
 import org.jailsframework.util.FileUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.tools.JavaCompiler;
 import javax.tools.ToolProvider;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import static junit.framework.Assert.assertEquals;
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: Apr 4, 2010
  *          Time: 12:08:43 AM
  */
 
 public class AbstractMigratableTest {
 
     private IMigratable migratable;
     IDatabase database = createMock(IDatabase.class);
 
     @Before
     public void setUp() {
         migratable = new AbstractMigratable() {
             @Override
             public String getMigrationPackage() {
                 return "migrations";
             }
 
             @Override
             public String getMigrationPath() {
                 return "dbmigrate4j/test/migrations";
             }
 
             @Override
             public IDatabase getDatabase() {
                 return database;
             }
 
             @Override
             public String getEnvironment() {
                 return "test";
             }
 
             @Override
             public String getMigrationsPropertiesFilePath() {
                 return "dbmigrate4j/test/versions.properties";
             }
 
             @Override
             public String getMigrationsClassPath() {
                 return "dbmigrate4j/test/";
             }
         };
         setDbVersionForCurrentEnvironment("0");
     }
 
     @After
     public void tearDown() {
         FileUtil.emptyDirRecursively(new File(migratable.getMigrationPath()), false);
        setDbVersionForCurrentEnvironment("0");
     }
 
     @Test
     public void shouldAddMigrations() {
         assertNotNull(migratable.addMigration("migration1"));
     }
 
     @Test
     public void shouldRunAllMigrations() {
         migratable.addMigration("migration1");
         Long finalDbVersion = migratable.addMigration("migration2");
         compileMigrations();
         expect(database.execute("")).andReturn(true).times(2);
         replay(database);
         assertEquals(finalDbVersion, migratable.migrate());
         verify(database);
     }
 
     @Test(expected = RuntimeException.class)
     public void shouldThrowExceptionWhenNoMigrationsPresent() {
         assertNotNull(migratable.migrate());
     }
 
     @Test(expected = RuntimeException.class)
     public void shouldThrowExceptionIfGivenMigrationVersionIsInvalid() {
         migratable.migrate(1236L);
     }
 
     @Test
     public void shouldMigrateDownTheDBToGivenMigrationWhenMigratedWithLowerVersion() {
         Long lowerVersion = migratable.addMigration("migration1");
         Long higherVersion = migratable.addMigration("migration2");
         compileMigrations();
         setDbVersionForCurrentEnvironment(higherVersion.toString());
         assertEquals(lowerVersion, migratable.migrate(lowerVersion));
     }
 
     @Test
     public void shouldMigrateUpTheDBToGivenMigrationWhenMigratedWithHigherVersion() {
         Long lowerVersion = migratable.addMigration("migration1");
         Long higherVersion = migratable.addMigration("migration2");
         compileMigrations();
         setDbVersionForCurrentEnvironment(lowerVersion.toString());
         assertEquals(higherVersion, migratable.migrate(higherVersion));
     }
 
     @Test
     public void shouldMigrateTheDBOnlyUpToGivenMigrationVersion() {
         Long firstMigrationVersion = migratable.addMigration("migration1");
         Long secondMigrationVersion = migratable.addMigration("migration2");
         migratable.addMigration("migration3");
         compileMigrations();
         setDbVersionForCurrentEnvironment(firstMigrationVersion.toString());
         assertEquals(secondMigrationVersion, migratable.migrate(secondMigrationVersion));
     }
 
     @Test(expected = RuntimeException.class)
     public void shouldThrowExceptionIfAlreadyMigratedToLatestVersion() {
         Long version = migratable.addMigration("migration2");
         compileMigrations();
         setDbVersionForCurrentEnvironment(version.toString());
         assertEquals(version, migratable.migrate(version));
     }
 
     private void compileMigrations() {
         File[] migrationClassFiles = new File(migratable.getMigrationPath()).listFiles();
 
         for (File migrationFile : migrationClassFiles) {
             if (migrationFile.getName().startsWith("Migration") && migrationFile.getName().endsWith(".java")) {
                 compile(migrationFile);
             }
         }
     }
 
     private void compile(File migrationFile) {
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
         if (compiler.run(null, null, null, migrationFile.getAbsolutePath()) != 0) {
             System.err.println("Could not compile file : " + migrationFile.getAbsolutePath());
             System.exit(0);
         }
     }
 
     private void setDbVersionForCurrentEnvironment(String version) {
         try {
             Properties properties = new Properties();
             File migrationPropertiesFile = new File(migratable.getMigrationsPropertiesFilePath());
             properties.load(new FileInputStream(migrationPropertiesFile));
             properties.setProperty(migratable.getEnvironment(), version);
             properties.store(new FileOutputStream(migrationPropertiesFile), null);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
