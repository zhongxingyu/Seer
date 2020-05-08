 package com.nesscomputing.migratory.mojo.database;
 
 import static java.lang.String.format;
 
 import java.io.File;
 import java.io.StringReader;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.configuration.CombinedConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.configuration.SystemConfiguration;
 import org.apache.commons.configuration.tree.OverrideCombiner;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.skife.config.CommonsConfigSource;
 import org.skife.config.ConfigurationObjectFactory;
 import org.skife.jdbi.v2.DBI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.nesscomputing.migratory.MigratoryConfig;
 import com.nesscomputing.migratory.MigratoryOption;
 import com.nesscomputing.migratory.loader.FileLoader;
 import com.nesscomputing.migratory.loader.HttpLoader;
 import com.nesscomputing.migratory.loader.JarLoader;
 import com.nesscomputing.migratory.loader.LoaderManager;
 import com.nesscomputing.migratory.mojo.database.util.DBIConfig;
 import com.nesscomputing.migratory.mojo.database.util.InitialConfig;
 import com.pyx4j.log4j.MavenLogAppender;
 
 public abstract class AbstractDatabaseMojo extends AbstractMojo
 {
     private static final String [] MUST_EXIST = new String [] { "default.base", "default.root_url", "default.root_user", "default.root_password", "default.user", "default.password" };
     private static final String [] MUST_NOT_BE_EMPTY = new String [] { "default.base", "default.root_url", "default.root_user", "default.user" };
 
     public static final String MIGRATORY_PROPERTIES_FILE = ".migratory.properties";
 
 
     private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabaseMojo.class);
 
     private ConfigurationObjectFactory factory;
 
     protected DBIConfig rootDBIConfig;
     protected Configuration config;
     protected InitialConfig initialConfig;
     protected MigratoryConfig migratoryConfig;
 
     protected LoaderManager loaderManager;
 
     protected MigratoryOption [] optionList;
 
     private void stateCheck()
         throws MojoExecutionException
     {
         StringBuilder sb = new StringBuilder();
         sb.append(rootDBIConfig == null ? "rootDBIConfig is null, " : "");
         sb.append(config == null ? "config is null, " : "");
         sb.append(initialConfig == null ? "initialConfig is null, " : "");
         sb.append(migratoryConfig == null ? "migratoryConfig is null, " : "");
         sb.append(loaderManager == null ? "loaderManager is null, " : "");
         sb.append(optionList == null ? "optionList is null, " : "");
 
         if (sb.length() > 0) {
             throw new MojoExecutionException(format("Internal error (%s), refusing to run mojo !", sb));
         }
     }
 
     /**
      * @parameter expression="${manifest.url}"
      */
     protected String manifestUrl = null;
 
     /**
      * @parameter expression="${manifest.name}"
      */
     private String manifestName = null;
 
     /**
      * @parameter expression="${options}"
      */
     private String options;
 
     @Override
     public final void execute() throws MojoExecutionException, MojoFailureException
     {
         MavenLogAppender.startPluginLog(this);
 
         try {
             // Load the default manifest information.
             //
             final CombinedConfiguration config = new CombinedConfiguration(new OverrideCombiner());
             // everything can be overridden by system properties.
             config.addConfiguration(new SystemConfiguration(), "systemProperties");
 
             final String userHome = System.getProperty("user.home");
             if (userHome != null) {
                 final File propertyFile = new File(userHome, MIGRATORY_PROPERTIES_FILE);
                 if (propertyFile.exists() && propertyFile.canRead() && propertyFile.isFile()) {
                     config.addConfiguration(new PropertiesConfiguration(propertyFile));
                 }
             }
 
             final ConfigurationObjectFactory initialConfigFactory = new ConfigurationObjectFactory(new CommonsConfigSource(config));
 
             // Load the initial config from the local config file
             this.initialConfig = initialConfigFactory.build(InitialConfig.class);
 
 
             if (this.manifestUrl == null) {
                 this.manifestUrl = initialConfig.getManifestUrl();
             }
 
             if (this.manifestName == null) {
                 this.manifestName = initialConfig.getManifestName();
             }
 
             LOG.debug("Manifest URL:      {}", manifestUrl);
             LOG.debug("Manifest Name:     {}", manifestName);
 
             this.optionList = parseOptions(options);
 
             final StringBuilder location = new StringBuilder(manifestUrl);
             if (!this.manifestUrl.endsWith("/")) {
                 location.append("/");
             }
 
             // After here, the manifestUrl is guaranteed to have a / at the end!
             this.manifestUrl = location.toString();
 
             location.append(manifestName);
             location.append(".manifest");
 
             LOG.debug("Manifest Location: {}", location);
 
             final MigratoryConfig initialMigratoryConfig = initialConfigFactory.build(MigratoryConfig.class);
             final LoaderManager initialLoaderManager = createLoaderManager(initialMigratoryConfig);
             final String contents = initialLoaderManager.loadFile(URI.create(location.toString()));
 
             if (contents == null) {
                 throw new MojoExecutionException(format("Could not load manifest '%s' from '%s'", manifestName, manifestUrl));
             }
 
             //
             // Now add the contents of the manifest file to the configuration creating the
             // final configuration for building the sql migration sets.
             //
             final PropertiesConfiguration pc = new PropertiesConfiguration();
             pc.load(new StringReader(contents));
             config.addConfiguration(pc);
 
             if (!validateConfiguration(config)) {
                 throw new MojoExecutionException(format("Manifest '%s' is not valid. Refusing to execute!", manifestName));
             }
 
             this.config = config;
            final ConfigurationObjectFactory factory = new ConfigurationObjectFactory(new CommonsConfigSource(config));
             this.migratoryConfig = factory.build(MigratoryConfig.class);
             this.loaderManager = createLoaderManager(migratoryConfig);
 
             LOG.debug("Configuration: {}", this.config);
 
             this.rootDBIConfig = getDBIConfig(getPropertyName("default.root_"));
 
             stateCheck();
 
             doExecute();
         }
         catch (Exception e) {
             Throwables.propagateIfInstanceOf(e, MojoExecutionException.class);
 
             LOG.error("While executing Mojo {}: {}", this.getClass().getSimpleName(), e);
             throw new MojoExecutionException("Failure:" ,e);
         }
         finally {
             MavenLogAppender.endPluginLog(this);
         }
     }
 
     /**
      * Executes this mojo.
      */
     protected abstract void doExecute() throws Exception;
 
     protected String getPropertyName(final String name)
     {
         return initialConfig.getDefaultPropertyPrefix() + "." + name;
     }
 
     private DBIConfig getDBIConfig(final String dbiName)
     {
         return factory.buildWithReplacements(DBIConfig.class, ImmutableMap.of("_dbi_name", dbiName,
                                                                               "_prefix", initialConfig.getDefaultPropertyPrefix()));
     }
 
     protected DBIConfig getDBIConfigFor(final String database)
     {
         final DBIConfig baseConfig = getDBIConfig(getPropertyName(format("db.%s.", database)));
         final String dbUrl = (baseConfig.getDBUrl() != null)
             ? baseConfig.getDBUrl()
             : String.format(config.getString(getPropertyName("default.base")), database);
 
         return new DBIConfig() {
             @Override
             public String getDBDriverClass() {
                 return baseConfig.getDBDriverClass();
             }
 
             @Override
             public String getDBUser() {
                 return baseConfig.getDBUser();
             }
 
             @Override
             public String getDBPassword() {
                 return baseConfig.getDBPassword();
             }
 
             @Override
             public String getDBUrl() {
                 return dbUrl;
             }
 
             @Override
             public String getDBTablespace() {
                 return baseConfig.getDBTablespace();
             }
         };
     }
 
     protected DBI getDBIFor(final String database) throws Exception
     {
         return getDBIFor(getDBIConfigFor(database));
     }
 
     protected DBI getDBIFor(final DBIConfig dbiConfig) throws Exception
     {
         if (dbiConfig.getDBDriverClass() != null) {
             Class.forName(dbiConfig.getDBDriverClass());
         }
 
         return new DBI(dbiConfig.getDBUrl(), dbiConfig.getDBUser(), dbiConfig.getDBPassword());
     }
 
     protected List<String> expandDatabaseList(final String databases) throws MojoExecutionException
     {
         final String [] databaseNames = StringUtils.stripAll(StringUtils.split(databases, ","));
         if (databaseNames == null) {
             return  Collections.<String>emptyList();
         }
 
         final List<String> availableDatabases = getAvailableDatabases();
 
         if (databaseNames.length == 1 && databaseNames[0].equalsIgnoreCase("all")) {
             return availableDatabases;
         }
         else {
             for (String database : databaseNames) {
                 if (!availableDatabases.contains(database)) {
                     throw new MojoExecutionException("Database " + database + " is unknown!");
                 }
             }
 
             return Arrays.asList(databaseNames);
         }
     }
 
     protected List<String> getAvailableDatabases()
     {
         final List<String> databaseList = Lists.newArrayList();
 
         final Configuration dbConfig = config.subset(getPropertyName("db"));
 
         for (Iterator<?> it = dbConfig.getKeys(); it.hasNext(); ) {
             final String key = (String) it.next();
             if (key.contains(".")) {
                 continue;
             }
             databaseList.add(key);
         }
         return databaseList;
     }
 
     protected MigratoryOption [] parseOptions(final String options)
     {
         final String [] optionList = StringUtils.stripAll(StringUtils.split(options, ","));
 
         if (optionList == null) {
             return new MigratoryOption[0];
         }
 
         final MigratoryOption [] migratoryOptions = new MigratoryOption[optionList.length];
         for (int i = 0 ; i < optionList.length; i++) {
             migratoryOptions[i] = MigratoryOption.valueOf(optionList[i].toUpperCase(Locale.ENGLISH));
         }
 
         LOG.debug("Parsed {} into {}", options, migratoryOptions);
         return migratoryOptions;
     }
 
 
     protected Map<String, MigrationInformation> getAvailableMigrations(final String database) throws MojoExecutionException
     {
         final Map<String, MigrationInformation> availableMigrations = Maps.newHashMap();
 
         addMigrations(getPropertyName("db." + database), availableMigrations);
         addMigrations(getPropertyName("default.personalities"), availableMigrations);
 
         return availableMigrations;
     }
 
     protected void addMigrations(final String property, final Map<String, MigrationInformation> availableMigrations) throws MojoExecutionException
     {
         final String [] personalities = StringUtils.stripAll(config.getStringArray(property));
         for (String personality : personalities) {
             final String [] personalityParts = StringUtils.stripAll(StringUtils.split(personality, ":"));
 
             if (personalityParts == null || personalityParts.length < 1 || personalityParts.length > 2) {
                 throw new MojoExecutionException("Personality " + personality + " is invalid.");
             }
 
             if (personalityParts.length == 1) {
                 availableMigrations.put(personalityParts[0], new MigrationInformation(personalityParts[0], 0));
             }
             else {
                 availableMigrations.put(personalityParts[0], new MigrationInformation(personalityParts[0], Integer.parseInt(personalityParts[1], 10)));
             }
         }
     }
 
     protected static class MigrationInformation
     {
         private final String name;
         private final int priority;
 
         public MigrationInformation(final String name, final int priority)
         {
             this.name = name;
             this.priority = priority;
         }
 
         public String getName()
         {
             return name;
         }
 
         public int getPriority()
         {
             return priority;
         }
     }
 
     private boolean validateConfiguration(Configuration config) throws MojoExecutionException
     {
         boolean valid = true;
         for (String mustExist : MUST_EXIST) {
             final String propertyName = getPropertyName(mustExist);
             if (!config.containsKey(propertyName)) {
                 LOG.error("The required property '{}' does not exist in the manifest.", propertyName);
                 valid = false;
             }
         }
 
         for (String mustNotBeEmpty : MUST_NOT_BE_EMPTY) {
             final String propertyName = getPropertyName(mustNotBeEmpty);
             if (StringUtils.isBlank(config.getString(propertyName, null))) {
                 LOG.error("The property '{}' must not be empty.", propertyName);
                 valid = false;
             }
         }
 
         final String defaultBase = config.getString(getPropertyName("default.base"), "");
         if (defaultBase.indexOf("%s") == -1 || defaultBase.indexOf("%s") != defaultBase.lastIndexOf("%s")) {
             LOG.error("The 'config.default.base' property must contain exactly one '%s' place holder!");
             valid = false;
         }
 
         return valid;
     }
 
     private LoaderManager createLoaderManager(final MigratoryConfig migratoryConfig)
     {
         final LoaderManager loaderManager = new LoaderManager();
         loaderManager.addLoader(new FileLoader(Charsets.UTF_8));
         loaderManager.addLoader(new JarLoader(Charsets.UTF_8));
         loaderManager.addLoader(new HttpLoader(migratoryConfig));
 
         return loaderManager;
     }
 }
