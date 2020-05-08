 package com.carbonfive.db.migration;
 
 import com.carbonfive.db.jdbc.DatabaseType;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 
 import java.io.IOException;
 import java.util.*;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;
 
 /**
  * A MigrationResolver which leverages Spring's robust Resource loading mechanism, supporting 'file:', 'classpath:', and standard url format resources.
  * <p/>
  * Migration Location Examples: <ul> <li>classpath:/db/migrations/</li> <li>file:src/main/db/migrations/</li> <li>file:src/main/resources/db/migrations/</li>
  * </ul> All of the resources found in the migrations location which do not start with a '.' will be considered migrations.
  * <p/>
  * Configured out of the box with a SimpleVersionExtractor and the default resource pattern CLASSPATH_MIGRATIONS_SQL.
  *
  * @see Resource
  * @see PathMatchingResourcePatternResolver
  * @see VersionExtractor
  * @see MigrationFactory
  */
 public class ResourceMigrationResolver implements MigrationResolver
 {
     private static final String CLASSPATH_MIGRATIONS_SQL = "classpath:/db/migrations/";
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private String migrationsLocation;
     private VersionExtractor versionExtractor;
     private MigrationFactory migrationFactory = new MigrationFactory();
 
     public ResourceMigrationResolver()
     {
         this(CLASSPATH_MIGRATIONS_SQL);
     }
 
     public ResourceMigrationResolver(String migrationsLocation)
     {
         this(migrationsLocation, new SimpleVersionExtractor());
     }
 
     public ResourceMigrationResolver(String migrationsLocation, VersionExtractor versionExtractor)
     {
         setMigrationsLocation(migrationsLocation);
         setVersionExtractor(versionExtractor);
     }
 
     public Set<Migration> resolve(DatabaseType dbType)
     {
         Set<Migration> migrations = new HashSet<Migration>();
 
         // Find all resources in the migrations location.
         String convertedMigrationsLocation = convertMigrationsLocation(migrationsLocation);
 
         PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
         List<Resource> resources;
         try
         {
             resources = new ArrayList<Resource>(Arrays.asList(patternResolver.getResources(convertedMigrationsLocation)));
         }
         catch (IOException e)
         {
             throw new MigrationException(e);
         }
 
         // Remove resources starting with a '.' (e.g. .svn, .cvs, etc)
         CollectionUtils.filter(resources, new Predicate()
         {
             public boolean evaluate(Object object)
             {
                 try
                 {
                    return (((Resource) object).isReadable() && !((Resource) object).getFilename().startsWith("."));
                 }
                catch (Exception e)
                 {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Exception while filtering resource.", e);
                    }
                     return false;
                 }
             }
         });
 
         if (resources.isEmpty())
         {
             String message = "No migrations were found using resource pattern '" + migrationsLocation + "'. Terminating migration.";
             logger.error(message);
             throw new MigrationException(message);
         }
 
         if (logger.isDebugEnabled())
         {
             logger.debug("Found " + resources.size() + " resources: " + collectionToCommaDelimitedString(resources));
         }
 
         // Extract versions and create executable migrations for each resource.
         for (Resource resource : resources)
         {
             String version = versionExtractor.extractVersion(resource.getFilename());
             migrations.add(migrationFactory.create(version, resource));
         }
 
         return migrations;
     }
 
     public Set<Migration> resolve()
     {
         return resolve(DatabaseType.UNKNOWN);
     }
 
     String convertMigrationsLocation(String migrationsLocation)
     {
         String converted = migrationsLocation;
 
         if (!(isBlank(FilenameUtils.getName(converted)) || FilenameUtils.getName(converted).contains("*")))
         {
             converted += "/";
         }
 
         if (!FilenameUtils.getName(converted).contains("*"))
         {
             converted += "*";
         }
 
         if (!(converted.startsWith("file:") || converted.startsWith("classpath:")))
         {
             converted = "file:" + converted;
         }
 
         return converted;
     }
 
     public void setMigrationsLocation(String migrationsLocation)
     {
         this.migrationsLocation = migrationsLocation;
     }
 
     public void setVersionExtractor(VersionExtractor versionExtractor)
     {
         this.versionExtractor = versionExtractor;
     }
 
     public void setMigrationFactory(MigrationFactory migrationFactory)
     {
         this.migrationFactory = migrationFactory;
     }
 }
