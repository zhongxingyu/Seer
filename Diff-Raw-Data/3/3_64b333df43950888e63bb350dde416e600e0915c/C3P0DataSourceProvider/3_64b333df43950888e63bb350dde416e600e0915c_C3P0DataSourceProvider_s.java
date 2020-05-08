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
 package com.nesscomputing.jdbc;
 
 import java.lang.annotation.Annotation;
 import java.net.URI;
 import java.sql.SQLException;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.configuration.CombinedConfiguration;
 import org.apache.commons.configuration.ConfigurationConverter;
 import org.apache.commons.configuration.tree.OverrideCombiner;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 import com.google.inject.Binding;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 import com.google.inject.ProvisionException;
 import com.google.inject.TypeLiteral;
 import com.mchange.v2.c3p0.DataSources;
 import com.nesscomputing.config.Config;
 import com.nesscomputing.config.util.ImmutableConfiguration;
 import com.nesscomputing.lifecycle.LifecycleStage;
 import com.nesscomputing.lifecycle.guice.AbstractLifecycleProvider;
 import com.nesscomputing.lifecycle.guice.LifecycleAction;
 import com.nesscomputing.logging.Log;
 
 public class C3P0DataSourceProvider extends AbstractLifecycleProvider<DataSource> implements Provider<DataSource>
 {
     public static final String PREFIX = "ness.db.";
     public static final String DEFAULTS_PREFIX = "ness.db.defaults";
 
     private static final Log LOG = Log.findLog();
 
     private Config config;
 
     private Function<DataSource, DataSource> dataSourceWrapper = Functions.identity();
 
     private final String dbName;
     private URI uri = null;
     private Properties props = null;
     private final Annotation annotation;
     private final String propertiesPrefix;
 
     C3P0DataSourceProvider(final String dbName, final Annotation annotation)
     {
         this.dbName = dbName;
         this.annotation = annotation;
         this.propertiesPrefix = PREFIX + dbName;
 
         addAction(LifecycleStage.STOP_STAGE, new LifecycleAction<DataSource>() {
                 @Override
                 public void performAction(final DataSource dataSource)
                 {
                     LOG.info("Destroying datasource %s", dbName);
                     try {
                         DataSources.destroy(dataSource);
                     }
                     catch (SQLException e) {
                         LOG.error(e, "Could not destroy pool %s", dbName);
                     }
                 }
             });
     }
 
     @Inject(optional=true)
     void setUpConfig(final Config config)
     {
         this.config = config;
     }
 
     @Inject(optional = true)
     void injectDependencies(final Injector injector)
     {
         final Binding<Set<Function<DataSource, DataSource>>> datasourceBindings = injector.getExistingBinding(Key.get(new TypeLiteral<Set<Function<DataSource, DataSource>>> () { }, annotation));
         if (datasourceBindings != null) {
             for (Function<DataSource, DataSource> fn : datasourceBindings.getProvider().get()) {
                 dataSourceWrapper = Functions.compose(dataSourceWrapper, fn);
             }
         }
 
         final Binding<Properties> propertiesBinding = injector.getExistingBinding(Key.get(Properties.class, annotation));
         if (propertiesBinding != null) {
             props = propertiesBinding.getProvider().get();
         }
         final Binding<URI> uriBinding = injector.getExistingBinding(Key.get(URI.class, annotation));
         if (uriBinding != null) {
             uri = uriBinding.getProvider().get();
             Preconditions.checkArgument(uri != null, "the preset database URI must not be null!");
         }
 
     }
 
     @Override
     public DataSource internalGet()
     {
         try {
             DataSource pool = createC3P0Pool();
             DatabaseChecker.checkPool(pool);
             return dataSourceWrapper.apply(pool);
         } catch (SQLException e) {
             throw new ProvisionException(String.format("Could not start DB pool %s", dbName), e);
         }
     }
 
     private DataSource createC3P0Pool() throws SQLException
     {
         Preconditions.checkState(config != null, "Config object was never injected!");
 
         final DatabaseConfig databaseConfig;
 
         if (uri == null) {
             LOG.info("Creating datasource %s via C3P0", dbName);
             final DatabaseConfig dbConfig = config.getBean(DatabaseConfig.class, ImmutableMap.of("dbName", dbName));
             databaseConfig = dbConfig;
         }
         else {
             LOG.info("Using preset URI %s for %s via C3P0", uri, dbName);
             databaseConfig = new ImmutableDatabaseConfig(uri);
         }
 
         final Properties poolProps = getProperties("pool");
         LOG.info("Setting pool properties for %s to %s", dbName, poolProps);
 
         final Properties driverProps = getProperties("ds");
         LOG.info("Setting driver properties for %s to %s", dbName, driverProps);
 
         DataSource unpooledDataSource = DataSources.unpooledDataSource(databaseConfig.getDbUri().toString(), driverProps);
         DatabaseChecker.checkConnection(unpooledDataSource);
 
        final DataSource dataSource = DataSources.pooledDataSource(unpooledDataSource, poolProps);
        return dataSource;
     }
 
     private Properties getProperties(final String suffix)
     {
         final CombinedConfiguration cc = new CombinedConfiguration(new OverrideCombiner());
 
         if (props != null) {
             // Allow setting of internal defaults by using "ds.xxx" and "pool.xxx" if a properties
             // object is present.
             cc.addConfiguration(new ImmutableConfiguration(ConfigurationConverter.getConfiguration(props).subset(suffix)));
         }
 
         if (config != null) {
             cc.addConfiguration(config.getConfiguration(propertiesPrefix + "." + suffix));
             cc.addConfiguration(config.getConfiguration(DEFAULTS_PREFIX + "." + suffix));
         }
 
         return ConfigurationConverter.getProperties(cc);
     }
 }
