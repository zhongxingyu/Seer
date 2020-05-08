 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.pg.cli;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.ResourceInfo;
 import org.geotools.data.postgis.PostgisNGDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureSource;
 
 import org.opengis.feature.type.Name;
 
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.ParametersDelegate;
 import com.beust.jcommander.internal.Maps;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 
 /**
  *
  */
 @Parameters(commandNames = "describe", commandDescription = "Describe a PostGIS table")
 public class PGDescribe extends AbstractCommand implements CLICommand {
 
     @ParametersDelegate
     public PGDescribeArgs args = new PGDescribeArgs();
 
     @Override
     protected void runInternal(GeogitCLI cli) throws Exception {
         Preconditions.checkState(cli.getGeogit() != null, "Not a geogit repository: "
                 + cli.getPlatform().pwd());
 
         try {
             doDescribe(cli);
         } catch (Exception e) {
             throw Throwables.propagate(e);
         } finally {
             try {
                 cli.getConsole().flush();
             } catch (IOException e) {
                 throw Throwables.propagate(e);
             }
         }
     }
 
     private void doDescribe(GeogitCLI cli) throws Exception {
         final ConsoleReader console = cli.getConsole();
         console.println("Fetching table...");
 
         Map<String, Serializable> params = Maps.newHashMap();
         params.put(PostgisNGDataStoreFactory.DBTYPE.key, "postgis");
         params.put(PostgisNGDataStoreFactory.HOST.key, args.common.host);
         params.put(PostgisNGDataStoreFactory.PORT.key, args.common.port.toString());
         params.put(PostgisNGDataStoreFactory.SCHEMA.key, args.common.schema);
         params.put(PostgisNGDataStoreFactory.DATABASE.key, args.common.database);
         params.put(PostgisNGDataStoreFactory.USER.key, args.common.username);
         params.put(PostgisNGDataStoreFactory.PASSWD.key, args.common.password);
 
         DataStore dataStore = new PostgisNGDataStoreFactory().createDataStore(params);
 
         List<Name> typeNames = dataStore.getNames();
         for (Name typeName : typeNames) {
            if (!args.table.equals(typeName.toString()))
                 continue;
 
             SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
             ResourceInfo info = featureSource.getInfo();
             console.println("Name : " + info.getName());
             // TODO : Print table property names/types
 
         }
     }
 }
