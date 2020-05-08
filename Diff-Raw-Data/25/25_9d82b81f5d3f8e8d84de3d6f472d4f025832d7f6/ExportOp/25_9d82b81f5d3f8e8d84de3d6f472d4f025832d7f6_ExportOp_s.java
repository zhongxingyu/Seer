 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.plumbing;
 
 import java.io.IOException;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.FeatureBuilder;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevObject.TYPE;
 import org.geogit.api.RevTree;
 import org.geogit.api.plumbing.FindTreeChild;
 import org.geogit.api.plumbing.ResolveTreeish;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.diff.DepthTreeIterator;
 import org.geogit.api.plumbing.diff.DepthTreeIterator.Strategy;
 import org.geogit.geotools.plumbing.GeoToolsOpException.StatusCode;
 import org.geogit.storage.ObjectDatabase;
 import org.geogit.storage.StagingDatabase;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.Transaction;
 import org.geotools.data.simple.SimpleFeatureStore;
 import org.geotools.feature.DefaultFeatureCollection;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.type.FeatureType;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.inject.Inject;
 
 /**
  * Internal operation for creating a FeatureCollection from a tree content.
  * 
  */
 public class ExportOp extends AbstractGeoGitOp<SimpleFeatureStore> {
 
     private String featureTypeName;
 
     private Supplier<SimpleFeatureStore> featureStore;
 
     private ObjectDatabase database;
 
     private Function<Feature, Feature> function = Functions.identity();
 
     /**
      * Constructs a new export operation.
      */
     @Inject
     public ExportOp(StagingDatabase database) {
         this.database = database;
     }
 
     /**
      * Executes the export operation using the parameters that have been specified.
      * 
      * @return a FeatureCollection with the specified features
      */
     @SuppressWarnings("deprecation")
     @Override
     public SimpleFeatureStore call() {
 
         SimpleFeatureStore fs;
         try {
             fs = featureStore.get();
         } catch (Exception e) {
             throw new GeoToolsOpException(StatusCode.CANNOT_CREATE_FEATURESTORE);
         }
         if (fs == null) {
             throw new GeoToolsOpException(StatusCode.CANNOT_CREATE_FEATURESTORE);
         }
 
         final String refspec;
         if (featureTypeName.contains(":")) {
             refspec = featureTypeName;
         } else {
             refspec = "WORK_HEAD:" + featureTypeName;
         }
         // final String spec = refspec.substring(0, refspec.indexOf(':'));
         final String treePath = refspec.substring(refspec.indexOf(':') + 1);
 
         Optional<ObjectId> tree = command(ResolveTreeish.class).setTreeish(
                 refspec.substring(0, refspec.indexOf(':'))).call();
 
         Preconditions.checkArgument(tree.isPresent(), "Invalid tree spec: %s",
                 refspec.substring(0, refspec.indexOf(':')));
 
         Optional<RevTree> revTree = command(RevObjectParse.class).setObjectId(tree.get()).call(
                 RevTree.class);
 
         Preconditions.checkArgument(revTree.isPresent(), "Tree ref spec could not be resolved.");
 
         Optional<NodeRef> typeTreeRef = command(FindTreeChild.class).setIndex(true)
                 .setParent(revTree.get()).setChildPath(treePath).call();
 
         ObjectId parentMetadataId = null;
         if (typeTreeRef.isPresent()) {
             parentMetadataId = typeTreeRef.get().getMetadataId();
         }
 
         Optional<RevObject> revObject = command(RevObjectParse.class).setRefSpec(refspec).call(
                 RevObject.class);
 
         Preconditions.checkArgument(revObject.isPresent(), "Invalid reference: %s", refspec);
         Preconditions.checkArgument(revObject.get().getType() == TYPE.TREE,
                 "%s did not resolve to a tree", refspec);
 
         DepthTreeIterator iter = new DepthTreeIterator(treePath, parentMetadataId,
                 (RevTree) revObject.get(), database, Strategy.FEATURES_ONLY);
 
         // Create a FeatureCollection
 
         getProgressListener().started();
         getProgressListener().setDescription("Exporting " + featureTypeName + "... ");
         DefaultFeatureCollection features = new DefaultFeatureCollection();
 
         FeatureBuilder featureBuilder = null;
         FeatureType featureType = null;
         int i = 1;
         while (iter.hasNext()) {
             NodeRef nodeRef = iter.next();
             if (nodeRef.getType() == TYPE.FEATURE) {
                 if (featureBuilder == null) {
                     RevFeatureType revFeatureType = command(RevObjectParse.class)
                             .setObjectId(nodeRef.getMetadataId()).call(RevFeatureType.class).get();
                     featureType = revFeatureType.type();
                     featureBuilder = new FeatureBuilder(revFeatureType);
                 }
                 RevFeature revFeature = command(RevObjectParse.class)
                         .setObjectId(nodeRef.objectId()).call(RevFeature.class).get();
                Feature feature = featureBuilder.build(Integer.toString(i), revFeature);
                 Feature validFeature = function.apply(feature);
                 features.add((SimpleFeature) validFeature);
                 getProgressListener().progress((i * 100.f) / revTree.get().size());
                 i++;
             }
         }
 
         if (featureType == null) {
             throw new GeoToolsOpException(StatusCode.UNABLE_TO_GET_FEATURES);
         }
 
         // add the feature collection to the feature store
         final Transaction transaction = new DefaultTransaction("create");
         try {
             fs.setTransaction(transaction);
             try {
                 fs.addFeatures(features);
                 transaction.commit();
             } catch (final Exception e) {
                 transaction.rollback();
                 throw new GeoToolsOpException(StatusCode.UNABLE_TO_ADD);
             } finally {
                 features = null;
                 transaction.close();
             }
         } catch (IOException e) {
             throw new GeoToolsOpException(StatusCode.UNABLE_TO_ADD);
         }
 
         getProgressListener().complete();
 
         return fs;
 
     }
 
     /**
      * 
      * @param featureStore a supplier that resolves to the feature store to use for exporting
      * @return
      */
     public ExportOp setFeatureStore(Supplier<SimpleFeatureStore> featureStore) {
         this.featureStore = featureStore;
         return this;
     }
 
     /**
      * 
      * @param featureStore the feature store to use for exporting
      * @return
      */
     public ExportOp setFeatureStore(SimpleFeatureStore featureStore) {
         this.featureStore = Suppliers.ofInstance(featureStore);
         return this;
     }
 
     /**
      * @param featureType the name of the featureType to export. Supports the [refspec]:[path]
      *        syntax
      * @return {@code this}
      */
     public ExportOp setFeatureTypeName(String featureType) {
         this.featureTypeName = featureType;
         return this;
     }
 
     /**
      * Sets the function to use for creating a valid Feature that has the FeatureType of the output
      * FeatureStore, based on the actual FeatureType of the Features to export.
      * 
      * The Export operation assumes that the feature returned by this function are valid to be added
      * to the current FeatureSource, and, therefore, performs no checking of FeatureType matching.
      * It is up to the user performing the export to ensure that the function actually generates
      * valid features for the current FeatureStore.
      * 
      * If no function is explicitly set, and identity function is used, and Features are not
      * converted.
      * 
      * @param function
      * @return {@code this}
      */
     public ExportOp setFeatureTypeConversionFunction(Function<Feature, Feature> function) {
         this.function = function;
         return this;
     }
 }
