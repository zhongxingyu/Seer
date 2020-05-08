 package com.carbonfive.db.migration;
 
 import com.carbonfive.jdbc.DatabaseType;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.*;
 
 import static org.apache.commons.collections.CollectionUtils.find;
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 /**
  * A MigrationResolver which leverages Spring's robust Resource loading mechanism, supporting 'file:', 'classpath:', and standard url format resources.
  * <p/>
  * Migration Location Examples: <ul> <li>classpath:/db/migrations/</li> <li>file:src/main/db/migrations/</li> <li>file:src/main/resources/db/migrations/</li>
  * </ul> All of the resources found in the migrations location which do not start with a '.' will be considered migrations.
  * <p/>
  * Configured out of the box with a SimpleVersionExtractor and the default resource pattern CLASSPATH_MIGRATIONS_SQL.
  *
  * @see Resource
  * @see VersionExtractor
  * @see MigrationFactory
  */
 public class ResourceMigrationResolver implements MigrationResolver {
     private static final String PATH_MIGRATIONS_SQL = "conf/migrations";
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private String migrationsLocation;
     private VersionExtractor versionExtractor;
     private MigrationFactory migrationFactory = new MigrationFactory();
 
     public ResourceMigrationResolver() {
         this(PATH_MIGRATIONS_SQL);
     }
 
     public ResourceMigrationResolver(String migrationsLocation) {
         this(migrationsLocation, new SimpleVersionExtractor());
     }
 
     public ResourceMigrationResolver(String migrationsLocation, VersionExtractor versionExtractor) {
         setMigrationsLocation(migrationsLocation);
         setVersionExtractor(versionExtractor);
     }
 
     public Set<Migration> resolve(DatabaseType dbType) {
         Set<Migration> migrations = new HashSet<Migration>();
 
         // Find all resources in the migrations location.
         File path = new File(migrationsLocation);
 
         List<Resource> resources = new ArrayList<Resource>();
 
        Collection<File> files = FileUtils.listFiles(path, new String[]{"*.sql"}, true);
         for (File file : files) {
             resources.add(new Resource(file));
         }
 
         if (resources.isEmpty()) {
             String message = "No migrations were found from path '" + path.getAbsolutePath() + "'.";
             logger.error(message);
             throw new MigrationException(message);
         }
 
         if (logger.isDebugEnabled()) {
             logger.debug("Found " + resources.size() + " resources: " + resources);
         }
 
         // Extract versions and create executable migrations for each resource.
         for (Resource resource : resources) {
             String version = versionExtractor.extractVersion(resource.getFilename());
             if (find(migrations, new Migration.MigrationVersionPredicate(version)) != null) {
                 String message = "Non-unique migration version.";
                 logger.error(message);
                 throw new MigrationException(message);
             }
             migrations.add(migrationFactory.create(version, resource));
         }
 
         return migrations;
     }
 
     public Set<Migration> resolve() {
         return resolve(DatabaseType.UNKNOWN);
     }
 
     protected String convertMigrationsLocation(String migrationsLocation, DatabaseType dbType) {
         String converted = migrationsLocation;
 
         if (!(isBlank(FilenameUtils.getName(converted)) || FilenameUtils.getName(converted).contains("*"))) {
             converted += "/";
         }
 
         if (!FilenameUtils.getName(converted).contains("*")) {
             converted += "*";
         }
 
         if (!(converted.startsWith("file:") || converted.startsWith("classpath:"))) {
             converted = "file:" + converted;
         }
 
         return converted;
     }
 
     public void setMigrationsLocation(String migrationsLocation) {
         this.migrationsLocation = migrationsLocation;
     }
 
     public void setVersionExtractor(VersionExtractor versionExtractor) {
         this.versionExtractor = versionExtractor;
     }
 
     public void setMigrationFactory(MigrationFactory migrationFactory) {
         this.migrationFactory = migrationFactory;
     }
 }
