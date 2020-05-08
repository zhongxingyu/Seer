 package trumpet.maven;
 
 import io.trumpet.migratory.Migratory;
 import io.trumpet.migratory.MigratoryConfig;
 import io.trumpet.migratory.MigratoryException;
 import io.trumpet.migratory.MigratoryOption;
 
 import java.util.List;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import org.skife.jdbi.v2.exceptions.DBIException;
 import org.skife.jdbi.v2.tweak.HandleCallback;
 import org.skife.jdbi.v2.tweak.StatementLocator;
 import org.skife.jdbi.v2.util.IntegerMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import trumpet.maven.util.DBIConfig;
 import trumpet.maven.util.TemplatingStatementLocator;
 
 
 /**
  * Maven goal that creates all databases.
  *
  * @aggregator true
  * @requiresProject false
  * @goal create
  */
 public class CreateMojo extends AbstractDatabaseMojo
 {
     private static final Logger LOG = LoggerFactory.getLogger(CreateMojo.class);
 
     /**
      * @parameter expression="${databases}"
      * @required
      */
     private String databases;
 
     @Override
     protected void doExecute() throws Exception
     {
         final List<String> databaseList = expandDatabaseList(databases);
 
         final boolean permission = config.getBoolean("trumpet.permission.create-db", false);
         if (!permission) {
             throw new MojoExecutionException("No permission to run this task!");
         }
 
         final DBI rootDbi = getDBIFor(rootDBIConfig);
 
         final StatementLocator statementLocator = new TemplatingStatementLocator("/sql/", loaderManager);
         rootDbi.setStatementLocator(statementLocator);
 
 
         for (final String database : databaseList) {
             final DBIConfig databaseConfig = getDBIConfigFor(database);
             final String user = databaseConfig.getDBUser();
 
             if (MigratoryOption.containsOption(MigratoryOption.DRY_RUN, optionList)) {
                 LOG.info("Dry run for database {} activated!", database);
             }
             else {
                 // User and Database creation runs as root user connected to the root db
                 try {
                     boolean userExists = rootDbi.withHandle(new HandleCallback<Boolean>() {
                         @Override
                         public Boolean withHandle(final Handle handle) {
                             return handle.createQuery("#mojo:detect_user")
                             .bind("user", user)
                             .map(IntegerMapper.FIRST)
                             .first() != 0;
                         }
                     });
 
                     if (userExists) {
                         LOG.trace("... User {} already exists ...", user);
                     }
                     else {
                         LOG.info("... creating User {} ...", user);
 
                         rootDbi.withHandle(new HandleCallback<Void>() {
                             @Override
                             public Void withHandle(final Handle handle) {
                                 handle.createStatement("#mojo:create_user")
                                 .define("user", user)
                                 .define("password", databaseConfig.getDBPassword())
                                 .execute();
                                 return null;
                             }
                         });
                     }
 
                     boolean databaseExists = rootDbi.withHandle(new HandleCallback<Boolean>() {
                         @Override
                         public Boolean withHandle(final Handle handle) {
                             return handle.createQuery("#mojo:detect_database")
                             .bind("database", database)
                             .map(IntegerMapper.FIRST)
                             .first() != 0;
                         }
                     });
 
 
                     if (databaseExists) {
                         LOG.info("... Database {} already exists ...", database);
                     }
                     else {
                         LOG.info("... creating Database {}...", database);
 
                        String tablespaceName = databaseConfig.getDBTablespace();
 
                        if (tablespaceName != null) {
                             boolean tablespaceExists = rootDbi.withHandle(new HandleCallback<Boolean>() {
                                 @Override
                                 public Boolean withHandle(final Handle handle) {
                                     return handle.createQuery("#mojo:detect_tablespace")
                                    .bind("database", database)
                                     .map(IntegerMapper.FIRST)
                                     .first() != 0;
                                 }
                             });
 
                            if (!tablespaceExists) {
                                LOG.warn("Tablespace '" + tablespaceName + "' does not exist, falling back to default!");
                                tablespaceName = null;
                             }
                         }

                        final String tablespace = tablespaceName;
 
                         rootDbi.withHandle(new HandleCallback<Void>() {
                             @Override
                             public Void withHandle(final Handle handle) {
                                 handle.createStatement("#mojo:create_database")
                                 .define("database", database)
                                 .define("owner", databaseConfig.getDBUser())
                                 .define("tablespace", tablespace)
                                 .execute();
                                 return null;
                             }
                         });
                     }
                 }
                 catch (DBIException de) {
                     LOG.warn("While creating {}: {}", database, de);
                 }
 
                 try {
                     // Language creation runs as root user, but connected to the actual database.
                     final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
                     rootDbDbi.setStatementLocator(statementLocator);
 
                     boolean languageExists = rootDbDbi.withHandle(new HandleCallback<Boolean>() {
                         @Override
                         public Boolean withHandle(final Handle handle) {
                             return handle.createQuery("#mojo:detect_language")
                             .map(IntegerMapper.FIRST)
                             .first() != 0;
                         }
                     });
 
 
                     if (languageExists) {
                         LOG.trace("Language plpgsql exists");
                     }
                     else {
                         LOG.info("... creating plpgsql language...");
 
                         rootDbDbi.withHandle(new HandleCallback<Void>() {
                             @Override
                             public Void withHandle(final Handle handle) {
                                 handle.createStatement("#mojo:create_language")
                                 .execute();
                                 return null;
                             }
                         });
                     }
                 }
                 catch (DBIException de) {
                     LOG.warn("While creating {}: {}", database, de);
                 }
 
                 try {
                     LOG.info("... initializing metadata ...");
 
                     // Finally metadata is created as the database owner connected to the database.
 
                     final DBI dbi = getDBIFor(database);
                     final MigratoryConfig config = factory.build(MigratoryConfig.class);
 
                     Migratory migratory = new Migratory(config, dbi);
                     migratory.dbInit();
                 }
                 catch (DBIException de) {
                     LOG.warn("While creating {}: {}", database, de);
                 }
                 catch (MigratoryException me) {
                     LOG.warn("While creating {}: {}", database, me);
                 }
             }
             LOG.info("... done");
         }
     }
 }
