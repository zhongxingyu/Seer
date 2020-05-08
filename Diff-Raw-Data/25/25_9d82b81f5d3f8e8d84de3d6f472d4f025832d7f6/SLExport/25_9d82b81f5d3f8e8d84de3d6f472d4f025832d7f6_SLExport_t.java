 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.porcelain;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevObject.TYPE;
 import org.geogit.api.RevTree;
 import org.geogit.api.plumbing.FindTreeChild;
 import org.geogit.api.plumbing.ResolveTreeish;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.diff.DepthTreeIterator;
 import org.geogit.api.plumbing.diff.DepthTreeIterator.Strategy;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.geotools.plumbing.ExportOp;
 import org.geogit.geotools.plumbing.GeoToolsOpException;
 import org.geogit.geotools.plumbing.GeoToolsOpException.StatusCode;
 import org.geogit.storage.ObjectDatabase;
 import org.geotools.data.DataStore;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.simple.SimpleFeatureStore;
 import org.geotools.feature.NameImpl;
 import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.opengis.filter.Filter;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.base.Optional;
 
 /**
  * Exports features from a feature type into a SpatiaLite database.
  * 
  * @see ExportOp
  */
 @Parameters(commandNames = "export", commandDescription = "Export to SpatiaLite")
 public class SLExport extends AbstractSLCommand implements CLICommand {
 
     @Parameter(description = "<featureType path> <table>", arity = 2)
     public List<String> args;
 
     @Parameter(names = { "--overwrite", "-o" }, description = "Overwrite output table")
     public boolean overwrite;
 
     /**
      * Executes the export command using the provided options.
      * 
      * @param cli
      */
     @Override
     protected void runInternal(GeogitCLI cli) throws Exception {
         if (cli.getGeogit() == null) {
             cli.getConsole().println("Not a geogit repository: " + cli.getPlatform().pwd());
             return;
         }
 
         if (args.isEmpty()) {
             printUsage();
             return;
         }
 
         String featureTypeName = args.get(0);
         String tableName = args.get(1);
 
         DataStore dataStore = null;
         try {
             dataStore = getDataStore();
         } catch (ConnectException e) {
             cli.getConsole().println("Unable to connect using the specified database parameters.");
             cli.getConsole().flush();
             return;
         }
 
         if (!Arrays.asList(dataStore.getTypeNames()).contains(tableName)) {
             SimpleFeatureType featureType;
             try {
                 featureType = getFeatureType(featureTypeName, tableName, cli);
             } catch (GeoToolsOpException e) {
                 cli.getConsole().println("No features to export.");
                 return;
             }
             try {
                 dataStore.createSchema(featureType);
             } catch (IOException e) {
                 cli.getConsole().println("Cannot create new table in database");
                 return;
             }
         } else {
             if (!overwrite) {
                 cli.getConsole().println("The selected table already exists. Use -o to overwrite");
                 return;
             }
         }
 
         SimpleFeatureSource featureSource = dataStore.getFeatureSource(tableName);
         if (featureSource instanceof SimpleFeatureStore) {
             SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            if (overwrite) {
                featureStore.removeFeatures(Filter.INCLUDE);
            }
             cli.getGeogit().command(ExportOp.class).setFeatureTypeName(featureTypeName)
                     .setFeatureStore(featureStore).setProgressListener(cli.getProgressListener())
                     .call();
 
             cli.getConsole().println(featureTypeName + " exported successfully to " + tableName);
         } else {
             cli.getConsole().println("Can't write to the selected table");
             return;
         }
 
     }
 
     private SimpleFeatureType getFeatureType(String featureTypeName, String tableName, GeogitCLI cli) {
 
         checkArgument(featureTypeName != null, "No feature type name specified.");
         checkArgument(tableName != null && !tableName.isEmpty(), "No table name specified");
 
         final String refspec;
         if (featureTypeName.contains(":")) {
             refspec = featureTypeName;
         } else {
             refspec = "WORK_HEAD:" + featureTypeName;
         }
 
         checkArgument(refspec.endsWith(":") != true, "No feature type name specified.");
 
         final GeoGIT geogit = cli.getGeogit();
 
         Optional<ObjectId> rootTreeId = geogit.command(ResolveTreeish.class)
                 .setTreeish(refspec.split(":")[0]).call();
 
         checkState(rootTreeId.isPresent(), "Couldn't resolve '" + refspec + "' to a treeish object");
 
         RevTree rootTree = geogit.getRepository().getTree(rootTreeId.get());
         Optional<NodeRef> featureTypeTree = geogit.command(FindTreeChild.class)
                 .setChildPath(refspec.split(":")[1]).setParent(rootTree).setIndex(true).call();
 
         checkArgument(featureTypeTree.isPresent(), "pathspec '" + refspec.split(":")[1]
                 + "' did not match any valid path");
 
         Optional<RevObject> revObject = geogit.command(RevObjectParse.class).setRefSpec(refspec)
                 .call(RevObject.class);
 
         checkArgument(revObject.isPresent(), "Invalid reference: %s", refspec);
         checkArgument(revObject.get().getType() == TYPE.TREE, "%s did not resolve to a tree",
                 refspec);
 
         ObjectDatabase database = geogit.getRepository().getObjectDatabase();
 
         DepthTreeIterator iter = new DepthTreeIterator("", featureTypeTree.get().getMetadataId(),
                 (RevTree) revObject.get(), database, Strategy.FEATURES_ONLY);
 
         while (iter.hasNext()) {
             NodeRef nodeRef = iter.next();
             ObjectId metadataId = nodeRef.getMetadataId();
             revObject = geogit.command(RevObjectParse.class).setObjectId(metadataId).call();
             if (revObject.isPresent() && revObject.get() instanceof RevFeatureType) {
                 RevFeatureType revFeatureType = (RevFeatureType) revObject.get();
                 if (revFeatureType.type() instanceof SimpleFeatureType) {
                     SimpleFeatureType sft = (SimpleFeatureType) revFeatureType.type();
                     SimpleFeatureTypeImpl newSFT = new SimpleFeatureTypeImpl(
                             new NameImpl(tableName), sft.getAttributeDescriptors(),
                             sft.getGeometryDescriptor(), sft.isAbstract(), sft.getRestrictions(),
                             sft.getSuper(), sft.getDescription());
                     return newSFT;
                 }
             }
         }
 
         throw new GeoToolsOpException(StatusCode.NO_FEATURES_FOUND);
 
     }
 }
