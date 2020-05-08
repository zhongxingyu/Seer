 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.migratory.mojo.database;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.skife.jdbi.v2.DBI;
 
 import com.nesscomputing.logging.Log;
 import com.nesscomputing.migratory.Migratory;
 import com.nesscomputing.migratory.MigratoryException;
 import com.nesscomputing.migratory.migration.MigrationPlan;
 import com.nesscomputing.migratory.mojo.database.util.DBIConfig;
 import com.nesscomputing.migratory.mojo.database.util.MojoLocator;
 
 
 /**
  * Maven goal that upgrades all databases.
  *
  * @aggregator true
  * @requiresProject false
  * @goal upgrade
  */
 public class DatabaseUpgradeMojo extends AbstractDatabaseMojo
 {
     private static final Log CONSOLE = Log.forName("console");
 
     /**
      * Describes the migrations for this database.
      *
      * all -> all databases, latest version. Default.
      * follow Migrate follow database to latest version.
      * oauth=oauth@4  : explicit version for a personality on a given db (all others are ignored)
      * ness_test=oauth@4/prefs@2 Migrate two personalities of a given database.
      * follow=follow@4,prefs=prefs@7  Migrate two different databases.
      * follow=follow Migrate follow personality in the follow database to latest version
      *
      * @parameter expression="${migrations}"
      */
     private String migrations = "all";
 
     @Override
     protected void doExecute() throws Exception
     {
         final boolean permission = config.getBoolean(getPropertyName("permission.upgrade-db"), false);
         if (!permission) {
             throw new MojoExecutionException("No permission to run this task!");
         }
 
         final Map<String, String> databases = extractDatabases(migrations);
 
         for (Map.Entry<String, String> database : databases.entrySet()) {
             final String databaseName = database.getKey();
 
             final DBIConfig databaseConfig = getDBIConfigFor(databaseName);
             final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
             final DBI dbi = getDBIFor(databaseName);
 
             try {
                 final MigrationPlan rootMigrationPlan  = createMigrationPlan(database);
                 if (!rootMigrationPlan.isEmpty()) {
                     CONSOLE.info("Migrating %s ...", databaseName);
 
                     Migratory migratory = new Migratory(migratoryConfig, dbi, rootDbDbi);
                     migratory.addLocator(new MojoLocator(migratory, manifestUrl));
                     migratory.dbMigrate(rootMigrationPlan, optionList);
                 }
             }
             catch (MigratoryException me) {
                CONSOLE.warnDebug(me, "While creating '%s': %s, Reason: %s", databaseName, me.getMessage(), me.getReason());
             }
             CONSOLE.info("... done");
         }
     }
 
     protected Map<String, String> extractDatabases(final String migrations)throws MojoExecutionException
     {
         String [] migrationNames = StringUtils.stripAll(StringUtils.split(migrations, ","));
 
         final List<String> availableDatabases = getAvailableDatabases();
 
         if (migrationNames == null) {
             return  Collections.<String, String>emptyMap();
         }
 
         final Map<String, String> databases = Maps.newHashMap();
 
         if (migrationNames.length == 1 && migrationNames[0].equalsIgnoreCase("all")) {
             migrationNames = availableDatabases.toArray(new String[availableDatabases.size()]);
         }
 
         for (String migration : migrationNames) {
             final String [] migrationFields = StringUtils.stripAll(StringUtils.split(migration, "="));
 
             if (migrationFields == null || migrationFields.length < 1 || migrationFields.length > 2) {
                 throw new MojoExecutionException("Migration " + migration + " is invalid.");
             }
 
             if (!availableDatabases.contains(migrationFields[0])) {
                 throw new MojoExecutionException("Database " + migrationFields[0] + " is unknown!");
             }
 
             databases.put(migrationFields[0], (migrationFields.length == 1 ? null : migrationFields[1]));
         }
 
         return databases;
     }
 
     protected MigrationPlan createMigrationPlan(final Map.Entry<String, String> database) throws MojoExecutionException
     {
         final Map<String, MigrationInformation> availableMigrations = getAvailableMigrations(database.getKey());
 
         final MigrationPlan migrationPlan = new MigrationPlan();
 
         // Do we have any special migrations given?
         final String migrations = database.getValue();
         if (StringUtils.isEmpty(migrations)) {
             for (MigrationInformation availableMigration : availableMigrations.values()) {
                 migrationPlan.addMigration(availableMigration.getName(), Integer.MAX_VALUE, availableMigration.getPriority());
             }
 
             return migrationPlan; // No
         }
 
         final String [] migrationNames = StringUtils.stripAll(StringUtils.split(migrations, "/"));
 
         for (String migrationName : migrationNames) {
             final String [] migrationFields = StringUtils.stripAll(StringUtils.split(migrationName, "@"));
 
             if (migrationFields == null || migrationFields.length < 1 || migrationFields.length > 2) {
                 throw new MojoExecutionException("Migration " + migrationName + " is invalid.");
             }
 
             int targetVersion = migrationFields.length == 2 ? Integer.parseInt(migrationFields[1], 10) : Integer.MAX_VALUE;
 
             MigrationInformation migrationInformation = availableMigrations.get(migrationFields[0]);
 
             if (migrationInformation == null) {
                 throw new MojoExecutionException("Migration " + migrationName + " is unknown!");
             }
 
             migrationPlan.addMigration(migrationInformation.getName(), targetVersion, migrationInformation.getPriority());
         }
 
         return migrationPlan;
     }
 }
 
