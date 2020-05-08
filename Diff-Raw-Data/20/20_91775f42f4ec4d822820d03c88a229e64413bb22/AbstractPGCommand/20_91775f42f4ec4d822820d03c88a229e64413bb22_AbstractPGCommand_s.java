 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.pg.cli;
 
 import java.io.Serializable;
 import java.net.ConnectException;
 import java.util.Map;
 
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geotools.data.AbstractDataStoreFactory;
 import org.geotools.data.DataStore;
 import org.geotools.data.postgis.PostgisNGDataStoreFactory;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.ParametersDelegate;
 import com.beust.jcommander.internal.Maps;
 
 /**
  * A template command; provides out of the box support for the --help argument so far.
  * 
  */
 public abstract class AbstractPGCommand implements CLICommand {
 
     @Parameter(names = "--help", help = true, hidden = true)
     public boolean help;
 
     @ParametersDelegate
     public PGCommonArgs commonArgs = new PGCommonArgs();
 
     public AbstractDataStoreFactory dataStoreFactory = new PostgisNGDataStoreFactory();
 
     /**
      * @param cli
      * @throws Exception
      * @see org.geogit.cli.CLICommand#run(org.geogit.cli.GeogitCLI)
      */
     @Override
     public void run(GeogitCLI cli) throws Exception {
         if (help) {
             JCommander jc = new JCommander(this);
             String commandName = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
             jc.setProgramName("geogit pg " + commandName);
             jc.usage();
             return;
         }
 
         runInternal(cli);
     }
 
     /**
     * Subclasses shall implement to do the real work, will not be called if the command was invoked with {@code --help}
      */
     protected abstract void runInternal(GeogitCLI cli) throws Exception;
 
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
 
         return dataStore;
     }
 }
