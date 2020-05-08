 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.pg.cli;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.api.RevFeature;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.repository.WorkingTree;
 import org.geogit.storage.hessian.GeoToolsRevFeature;
 import org.geogit.storage.hessian.GeoToolsRevFeatureType;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.postgis.PostgisNGDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.type.Name;
 import org.opengis.util.ProgressListener;
 
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.ParametersDelegate;
 import com.beust.jcommander.internal.Maps;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.common.collect.AbstractIterator;
 
 /**
  *
  */
 @Parameters(commandNames = "import", commandDescription = "Import PostGIS database")
 public class PGImport extends AbstractCommand implements CLICommand {
 
     @ParametersDelegate
     public PGImportArgs args = new PGImportArgs();
 
     @Override
     protected void runInternal(GeogitCLI cli) throws Exception {
         Preconditions.checkState(cli.getGeogit() != null, "Not a geogit repository: "
                 + cli.getPlatform().pwd());
 
        Preconditions.checkState(args.all == true && !args.table.isEmpty(),
                 "Specify --all or --table, both cannot be set.");
 
         try {
             doImport(cli);
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
 
     private void doImport(GeogitCLI cli) throws Exception {
         final ConsoleReader console = cli.getConsole();
         console.println("Importing database " + args.common.database);
 
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
            if (!args.all && !typeName.equals(args.table))
                 continue;
 
             WorkingTree workingTree = cli.getGeogit().getRepository().getWorkingTree();
 
             SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
             SimpleFeatureCollection features = featureSource.getFeatures();
 
             GeoToolsRevFeatureType revType = new GeoToolsRevFeatureType(featureSource.getSchema());
 
             String treePath = revType.getName().getLocalPart();
 
             final SimpleFeatureIterator featureIterator = features.features();
 
             Iterator<RevFeature> iterator = new AbstractIterator<RevFeature>() {
                 @Override
                 protected RevFeature computeNext() {
                     if (!featureIterator.hasNext()) {
                         return super.endOfData();
                     }
                     SimpleFeature feature = featureIterator.next();
                     return new GeoToolsRevFeature(feature);
                 }
             };
             ProgressListener progressListener = cli.getProgressListener();
             try {
                 Integer collectionSize = features.size();
                 workingTree
                         .insert(treePath, iterator, true, progressListener, null, collectionSize);
             } finally {
                 featureIterator.close();
             }
         }
     }
 }
