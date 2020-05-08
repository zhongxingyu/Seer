 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.geotools.porcelain;
 
 import java.io.Serializable;
 import java.net.ConnectException;
 import java.sql.Connection;
 import java.util.Map;
 
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geotools.data.AbstractDataStoreFactory;
 import org.geotools.data.DataStore;
 import org.geotools.data.postgis.PostgisNGDataStoreFactory;
 import org.geotools.jdbc.JDBCDataStore;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.ParametersDelegate;
 import com.beust.jcommander.internal.Maps;
 
 /**
  * A template for PostGIS commands; provides out of the box support for the --help argument so far.
  * 
  * @see CLICommand
  */
 public abstract class AbstractPGCommand implements CLICommand {
 
     /**
      * Flag for displaying help for the command.
      */
     @Parameter(names = "--help", help = true, hidden = true)
     public boolean help;
 
     /**
      * Common arguments for PostGIS commands.
      * 
      * @see PGCommonArgs
      */
     @ParametersDelegate
     public PGCommonArgs commonArgs = new PGCommonArgs();
 
     /**
      * Factory for constructing the data store.
      * 
      * @see PostgisNGDataStoreFactory
      */
     public AbstractDataStoreFactory dataStoreFactory = new PostgisNGDataStoreFactory();
 
     /**
      * Executes the command.
      * 
      * @param cli
      * @throws Exception
      * @see org.geogit.cli.CLICommand#run(org.geogit.cli.GeogitCLI)
      */
     @Override
     public void run(GeogitCLI cli) throws Exception {
         if (help) {
             printUsage();
             return;
         }
 
         runInternal(cli);
     }
 
     /**
      * Prints the correct usage of the geogit pg command.
      */
     protected void printUsage() {
         JCommander jc = new JCommander(this);
         String commandName = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
         jc.setProgramName("geogit pg " + commandName);
         jc.usage();
     }
 
     /**
      * Subclasses shall implement to do the real work, will not be called if the command was invoked
      * with {@code --help}
      */
     protected abstract void runInternal(GeogitCLI cli) throws Exception;
 
     /**
      * Constructs a new PostGIS data store using connection parameters from {@link PGCommonArgs}.
      * 
      * @return the constructed data store
      * @throws Exception
      * @see DataStore
      */
     protected DataStore getDataStore() throws Exception {
         Map<String, Serializable> params = Maps.newHashMap();
         params.put(PostgisNGDataStoreFactory.DBTYPE.key, "postgis");
         params.put(PostgisNGDataStoreFactory.HOST.key, commonArgs.host);
         params.put(PostgisNGDataStoreFactory.PORT.key, commonArgs.port.toString());
         params.put(PostgisNGDataStoreFactory.SCHEMA.key, commonArgs.schema);
         params.put(PostgisNGDataStoreFactory.DATABASE.key, commonArgs.database);
         params.put(PostgisNGDataStoreFactory.USER.key, commonArgs.username);
         params.put(PostgisNGDataStoreFactory.PASSWD.key, commonArgs.password);
 
         DataStore dataStore = dataStoreFactory.createDataStore(params);
 
         if (dataStore == null) {
             throw new ConnectException();
         }
 
         if (dataStore instanceof JDBCDataStore) {
             Connection con = null;
             try {
                 con = ((JDBCDataStore) dataStore).getDataSource().getConnection();
             } catch (Exception e) {
                 throw new ConnectException();
             }
 
             ((JDBCDataStore) dataStore).closeSafe(con);
         }
 
         return dataStore;
     }
 }
