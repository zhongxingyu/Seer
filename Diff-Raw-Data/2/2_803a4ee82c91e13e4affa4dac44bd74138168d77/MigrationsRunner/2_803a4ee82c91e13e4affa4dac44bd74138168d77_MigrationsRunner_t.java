 package com.objectstyle.db.migration.kit;
 
 import com.carbonfive.db.jdbc.DatabaseType;
 import com.carbonfive.db.migration.DriverManagerMigrationManager;
 import com.carbonfive.db.migration.MigrationResolver;
 import com.carbonfive.db.migration.ResourceMigrationResolver;
 
 public class MigrationsRunner {
     private static final String[] DEFAULT_MIGRATION_PATHS = {"/db/migrations"};
 
     public static void main(String[] args) {
         DriverManagerMigrationManager migrationManager = createMigrationManager();
         for (String path : getMigrationPaths()) {
             MigrationResolver migrationResolver = new ResourceMigrationResolver(path);
             migrationManager.setMigrationResolver(migrationResolver);
             migrationManager.migrate();
         }
     }
 
     private static DriverManagerMigrationManager createMigrationManager() {
         String driver = System.getProperty("driver");
         String url = System.getProperty("url");
         String username = System.getProperty("username");
         String password = System.getProperty("password");
         return new DriverManagerMigrationManager(driver, url, username, password, DatabaseType.MYSQL);
     }
 
     private static String[] getMigrationPaths() {
         String[] migrationPaths = DEFAULT_MIGRATION_PATHS;
         String migrationPathsStr = System.getProperty("paths");
        if (migrationPathsStr != null) {
             migrationPaths = migrationPathsStr.split(":");
             for (int i = 0; i < migrationPaths.length; i++) {
                 String path = "classpath:" + migrationPaths[i].trim();
                 migrationPaths[i] = path;
             }
         }
         return migrationPaths;
     }
 }
