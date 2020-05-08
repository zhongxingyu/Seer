 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.plumbing;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.FeatureBuilder;
 import org.geogit.api.NodeRef;
 import org.geogit.api.Ref;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevTree;
 import org.geogit.api.plumbing.LsTreeOp;
 import org.geogit.api.plumbing.LsTreeOp.Strategy;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.geotools.plumbing.GeoToolsOpException.StatusCode;
 import org.geotools.data.DataStore;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.feature.DecoratingFeature;
 import org.geotools.feature.NameImpl;
 import org.geotools.filter.identity.FeatureIdImpl;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.type.Name;
 import org.opengis.feature.type.PropertyDescriptor;
 import org.opengis.filter.identity.FeatureId;
 import org.opengis.util.ProgressListener;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.AbstractIterator;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 
 /**
  * Internal operation for importing tables from a GeoTools {@link DataStore}.
  * 
  * @see DataStore
  */
 public class ImportOp extends AbstractGeoGitOp<RevTree> {
 
     private boolean all = false;
 
     private String table = null;
 
     /**
      * The path to import the data into
      */
     private String destPath;
 
     private DataStore dataStore;
 
     /**
      * Whether to remove previous objects in the destination path, in case they exist
      * 
      */
     private boolean overwrite = true;
 
     /**
      * If true, it does not overwrite, and modifies the existing features to have the same feature
      * type as the imported table
      */
     private boolean alter;
 
     /**
      * Constructs a new {@code ImportOp} operation.
      */
     @Inject
     public ImportOp() {
     }
 
     /**
      * Executes the import operation using the parameters that have been specified. Features will be
      * added to the working tree, and a new working tree will be constructed. Either {@code all} or
      * {@code table}, but not both, must be set prior to the import process.
      * 
      * @return RevTree the new working tree
      */
     @SuppressWarnings("deprecation")
     @Override
     public RevTree call() {
         if (dataStore == null) {
             throw new GeoToolsOpException(StatusCode.DATASTORE_NOT_DEFINED);
         }
 
         if ((table == null || table.isEmpty()) && !(all)) {
             throw new GeoToolsOpException(StatusCode.TABLE_NOT_DEFINED);
         }
 
         if (table != null && !table.isEmpty() && all) {
             throw new GeoToolsOpException(StatusCode.ALL_AND_TABLE_DEFINED);
         }
 
         boolean foundTable = false;
 
         List<Name> typeNames;
         try {
             typeNames = dataStore.getNames();
         } catch (Exception e) {
             throw new GeoToolsOpException(StatusCode.UNABLE_TO_GET_NAMES);
         }
 
         if (typeNames.size() > 1 && alter && all) {
             throw new GeoToolsOpException(StatusCode.ALTER_AND_ALL_DEFINED);
         }
 
        if (alter) {
             overwrite = false;
         }
 
         getProgressListener().started();
         int tableCount = 0;
         if (destPath != null && !destPath.isEmpty()) {
             if (typeNames.size() > 1 && all) {
                 if (overwrite) {
                     // we delete the previous tree to honor the overwrite setting, but then turn it
                     // to false. Otherwise, each table imported will overwrite the previous ones and
                     // only the last one will be imported.
                     try {
                         this.getWorkTree().delete(new NameImpl(destPath));
                     } catch (Exception e) {
                         throw new GeoToolsOpException(e, StatusCode.UNABLE_TO_INSERT);
                     }
                     overwrite = false;
                 }
 
             }
         }
 
         for (Name typeName : typeNames) {
             tableCount++;
             if (!all && !table.equals(typeName.toString()))
                 continue;
 
             foundTable = true;
 
             String tableName = String.format("%-16s", typeName.getLocalPart());
             if (typeName.getLocalPart().length() > 16) {
                 tableName = tableName.substring(0, 13) + "...";
             }
             getProgressListener().setDescription(
                     "Importing " + tableName + " (" + (all ? tableCount : 1) + "/"
                             + (all ? typeNames.size() : 1) + ")... ");
 
             SimpleFeatureSource featureSource;
             SimpleFeatureCollection features;
             try {
                 featureSource = dataStore.getFeatureSource(typeName);
                 features = featureSource.getFeatures();
             } catch (Exception e) {
                 throw new GeoToolsOpException(StatusCode.UNABLE_TO_GET_FEATURES);
             }
 
             final RevFeatureType featureType = RevFeatureType.build(featureSource.getSchema());
 
             String path;
             if (destPath == null || destPath.isEmpty()) {
                 path = featureType.getName().getLocalPart();
             } else {
                 path = destPath;
             }
 
             NodeRef.checkValidPath(path);
 
             ProgressListener taskProgress = subProgress(100.f / (all ? typeNames.size() : 1f));
 
             String refspec = Ref.WORK_HEAD + ":" + path;
 
             if (overwrite) {
                 try {
                     this.getWorkTree().delete(new NameImpl(path));
                 } catch (Exception e) {
                     throw new GeoToolsOpException(e, StatusCode.UNABLE_TO_INSERT);
                 }
             }
 
             final SimpleFeatureIterator featureIterator = features.features();
             Iterator<Feature> iterator = new AbstractIterator<Feature>() {
                 @Override
                 protected Feature computeNext() {
                     if (!featureIterator.hasNext()) {
                         return super.endOfData();
                     }
                     return featureIterator.next();
                 }
             };
             final String fidPrefix = featureType.getName().getLocalPart() + ".";
             iterator = Iterators.transform(iterator, new FidReplacer(fidPrefix));
 
             Integer collectionSize = features.size();
             if (!alter) {
                 try {
                     if (iterator.hasNext()) {
                         getWorkTree().insert(path, iterator, taskProgress, null, collectionSize);
                     } else {
                         // No features
                         if (overwrite) {
                             getWorkTree().createTypeTree(path, featureType.type());
                         }
                     }
                 } catch (Exception e) {
                     throw new GeoToolsOpException(e, StatusCode.UNABLE_TO_INSERT);
                 } finally {
                     featureIterator.close();
                 }
             } else {
                 // first we modify the feature type and the existing features, if needed
                 this.getWorkTree().updateTypeTree(path, featureType.type());
 
                 Iterator<NodeRef> oldFeatures = command(LsTreeOp.class).setReference(refspec)
                         .setStrategy(Strategy.FEATURES_ONLY).call();
                 Iterator<Feature> transformedIterator = transformIterator(oldFeatures, featureType);
                 try {
                     Integer size = features.size();
                     getWorkTree().insert(path, transformedIterator, taskProgress, null, size);
                 } catch (Exception e) {
                     throw new GeoToolsOpException(StatusCode.UNABLE_TO_INSERT);
                 }
                 // then we add the new ones
                 getWorkTree().insert(path, iterator, taskProgress, null, collectionSize);
             }
 
         }
         if (!foundTable) {
             if (all) {
                 throw new GeoToolsOpException(StatusCode.NO_FEATURES_FOUND);
             } else {
                 throw new GeoToolsOpException(StatusCode.TABLE_NOT_FOUND);
             }
         }
         getProgressListener().progress(100.f);
         getProgressListener().complete();
         return getWorkTree().getTree();
     }
 
     private Iterator<Feature> transformIterator(Iterator<NodeRef> nodeIterator,
             final RevFeatureType newFeatureType) {
 
         Iterator<Feature> iterator = Iterators.transform(nodeIterator,
                 new Function<NodeRef, Feature>() {
                     @Override
                     public Feature apply(NodeRef node) {
                         return alter(node, newFeatureType);
                     }
 
                 });
 
         return iterator;
 
     }
 
     /**
      * Translates a feature pointed by a node from its original feature type to a given one, using
      * values from those attributes that exist in both original and destination feature type. New
      * attributes are populated with null values
      * 
      * @param node The node that points to the feature. No checking is performed to ensure the node
      *        points to a feature instead of other type
      * @param featureType the destination feature type
      * @return a feature with the passed feature type and data taken from the input feature
      */
     private Feature alter(NodeRef node, RevFeatureType featureType) {
         RevFeature oldFeature = command(RevObjectParse.class).setObjectId(node.objectId())
                 .call(RevFeature.class).get();
         RevFeatureType oldFeatureType;
         oldFeatureType = command(RevObjectParse.class).setObjectId(node.getMetadataId())
                 .call(RevFeatureType.class).get();
         ImmutableList<PropertyDescriptor> oldAttributes = oldFeatureType.sortedDescriptors();
         ImmutableList<PropertyDescriptor> newAttributes = featureType.sortedDescriptors();
         ImmutableList<Optional<Object>> oldValues = oldFeature.getValues();
         List<Optional<Object>> newValues = Lists.newArrayList();
         for (int i = 0; i < newAttributes.size(); i++) {
             int idx = oldAttributes.indexOf(newAttributes.get(i));
             if (idx != -1) {
                 Optional<Object> oldValue = oldValues.get(idx);
                 newValues.add(oldValue);
             } else {
                 newValues.add(Optional.absent());
             }
         }
         RevFeature newFeature = RevFeature.build(ImmutableList.copyOf(newValues));
         FeatureBuilder featureBuilder = new FeatureBuilder(featureType);
         Feature feature = featureBuilder.build(node.name(), newFeature);
         return feature;
     }
 
     /**
      * @param all if this is set, all tables from the data store will be imported
      * @return {@code this}
      */
     public ImportOp setAll(boolean all) {
         this.all = all;
         return this;
     }
 
     /**
      * @param table if this is set, only the specified table will be imported from the data store
      * @return {@code this}
      */
     public ImportOp setTable(String table) {
         this.table = table;
         return this;
     }
 
     /**
      * 
      * @param overwrite If this is true, existing features will be overwritten in case they exist
      *        and have the same path and Id than the features to import. If this is false, existing
      *        features will not be overwritten, and a safe import is performed, where only those
      *        features that do not already exists are added
      * @return {@code this}
      */
     public ImportOp setOverwrite(boolean overwrite) {
         this.overwrite = overwrite;
         return this;
     }
 
     /**
      * @param force if true, it will change the default feature type of the tree we are importing
      *        into and change all features under that tree to have that same feature type
      * @return {@code this}
      */
     public ImportOp setAlter(boolean alter) {
         this.alter = alter;
         return this;
     }
 
     /**
      * 
      * @param destPath the path to import to to. If not provided, it will be taken from the feature
      *        type of the table to import.
      * @return {@code this}
      */
     public ImportOp setDestinationPath(String destPath) {
         this.destPath = destPath;
         return this;
     }
 
     /**
      * @param dataStore the data store to use for the import process
      * @return {@code this}
      */
     public ImportOp setDataStore(DataStore dataStore) {
         this.dataStore = dataStore;
         return this;
     }
 
     /**
      * Replaces the default geotools fid by removing the specified fidPrefix prefix from it.
      * 
      */
     private static final class FidReplacer implements Function<Feature, Feature> {
 
         private String fidPrefix;
 
         public FidReplacer(String fidPrefix) {
             this.fidPrefix = fidPrefix;
         }
 
         @Override
         public Feature apply(final Feature input) {
             String fid = ((SimpleFeature) input).getID().substring(fidPrefix.length());
             return new FidOverrideFeature((SimpleFeature) input, fid);
         }
 
     }
 
     private static final class FidOverrideFeature extends DecoratingFeature {
 
         private String fid;
 
         public FidOverrideFeature(SimpleFeature delegate, String fid) {
             super(delegate);
             this.fid = fid;
         }
 
         @Override
         public String getID() {
             return fid;
         }
 
         @Override
         public FeatureId getIdentifier() {
             return new FeatureIdImpl(fid);
         }
     }
 }
