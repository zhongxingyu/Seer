 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.data;
 
 import java.io.IOException;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.CommandLocator;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevTree;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.repository.WorkingTree;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.data.Transaction;
 import org.geotools.data.store.ContentEntry;
 import org.geotools.data.store.ContentFeatureSource;
 import org.geotools.data.store.ContentState;
 import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.filter.visitor.SimplifyingFilterVisitor;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.feature.FeatureVisitor;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.Name;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 /**
  *
  */
 @SuppressWarnings("unchecked")
 public class GeogitFeatureSource extends ContentFeatureSource {
 
     /**
      * <b>Precondition</b>: {@code entry.getDataStore() instanceof GeoGitDataStore}
      * 
      * @param entry
      */
     public GeogitFeatureSource(ContentEntry entry) {
         this(entry, (Query) null);
     }
 
     /**
      * <b>Precondition</b>: {@code entry.getDataStore() instanceof GeoGitDataStore}
      * 
      * @param entry
      * @param query optional "definition query" making this feature source a "view"
      */
     public GeogitFeatureSource(ContentEntry entry, @Nullable Query query) {
         super(entry, query);
         Preconditions.checkArgument(entry.getDataStore() instanceof GeoGitDataStore);
     }
 
     /**
      * Adds the {@link Hints#FEATURE_DETACHED} hint to the supported hints so the renderer doesn't
      * clone the geometries
      */
     @Override
     protected void addHints(Set<Hints.Key> hints) {
         hints.add(Hints.FEATURE_DETACHED);
     }
 
     @Override
     protected boolean canFilter() {
         return true;
     }
 
     @Override
     protected boolean canSort() {
         return false;
     }
 
     @Override
     protected boolean canRetype() {
         return false;
     }
 
     @Override
     protected boolean canLimit() {
         return false;
     }
 
     @Override
     protected boolean canOffset() {
         return false;
     }
 
     /**
      * @return {@code true}
      */
     @Override
     protected boolean canTransact() {
         return true;
     }
 
     @Override
     protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException {
         return false;
     }
 
     @Override
     public GeoGitDataStore getDataStore() {
         return (GeoGitDataStore) super.getDataStore();
     }
 
     @Override
     public ContentState getState() {
         return super.getState();
     }
 
     /**
      * Overrides {@link ContentFeatureSource#getName()} to restore back the original meaning of
      * {@link FeatureSource#getName()}
      */
     @Override
     public Name getName() {
         return getEntry().getName();
     }
 
     @Override
     protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
         final Filter filter = (Filter) query.getFilter().accept(new SimplifyingFilterVisitor(),
                 null);
         final CoordinateReferenceSystem crs = getSchema().getCoordinateReferenceSystem();
         if (Filter.INCLUDE.equals(filter)) {
             NodeRef typeRef = getDataStore().findTypeRef(getName(), getTransaction());
             ReferencedEnvelope bounds = new ReferencedEnvelope(crs);
             typeRef.getNode().expand(bounds);
             return bounds;
         }
         // TODO optimize, please
         FeatureReader<SimpleFeatureType, SimpleFeature> features = getReader(query);
         ReferencedEnvelope bounds = new ReferencedEnvelope(crs);
         try {
             while (features.hasNext()) {
                 bounds.expandToInclude((ReferencedEnvelope) features.next().getBounds());
             }
         } finally {
             features.close();
         }
         return bounds;
     }
 
     @Override
     protected int getCountInternal(Query query) throws IOException {
         final Filter filter = (Filter) query.getFilter().accept(new SimplifyingFilterVisitor(),
                 null);
         if (Filter.EXCLUDE.equals(filter)) {
             return 0;
         }
 
         int size;
         if (Filter.INCLUDE.equals(filter)) {
             RevTree tree = getTypeTree();
             size = (int) tree.size();
             return size;
         }
 
         // TODO optimize, please
         FeatureReader<SimpleFeatureType, SimpleFeature> features = getReader(query);
         int count = 0;
         try {
             while (features.hasNext()) {
                 features.next();
                 count++;
             }
         } finally {
             features.close();
         }
 
         return count;
     }
 
     @Override
     protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query)
             throws IOException {
 
         final Filter filter = (Filter) query.getFilter().accept(new SimplifyingFilterVisitor(),
                 null);
 
         GeogitFeatureReader<SimpleFeatureType, SimpleFeature> nativeReader;
 
         final SimpleFeatureType schema = getSchema();
         final NodeRef typeRef = getTypeRef();
         final String configuredBranch = getDataStore().getConfiguredBranch();
 
         final CommandLocator commandLocator = getCommandLocator();
         nativeReader = new GeogitFeatureReader<SimpleFeatureType, SimpleFeature>(commandLocator,
                 schema, typeRef, filter, configuredBranch);
 
         return nativeReader;
     }
 
     @Override
     protected SimpleFeatureType buildFeatureType() throws IOException {
 
         final NodeRef typeRef = getTypeRef();
         final String treePath = typeRef.path();
         final ObjectId metadataId = typeRef.getMetadataId();
 
         CommandLocator commandLocator = getCommandLocator();
         Optional<RevFeatureType> revType = commandLocator.command(RevObjectParse.class)
                 .setObjectId(metadataId).call(RevFeatureType.class);
         if (revType.isPresent()) {
             SimpleFeatureType featureType = (SimpleFeatureType) revType.get().type();
            Name name = featureType.getName();
            Name assignedName = getEntry().getName();
            if (!assignedName.equals(name)) {
                SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                builder.init(featureType);
                builder.setName(assignedName);
                featureType = builder.buildFeatureType();
            }
             return featureType;
         }
 
         throw new IllegalStateException(String.format("Feature type for tree %s not found",
                 treePath));
     }
 
     CommandLocator getCommandLocator() {
         CommandLocator commandLocator = getDataStore().getCommandLocator(getTransaction());
         return commandLocator;
     }
 
     String getTypeTreePath() {
         NodeRef typeRef = getTypeRef();
         String path = typeRef.path();
         return path;
     }
 
     /**
      * @return
      */
     NodeRef getTypeRef() {
         GeoGitDataStore dataStore = getDataStore();
         Name name = getName();
         Transaction transaction = getTransaction();
         return dataStore.findTypeRef(name, transaction);
     }
 
     /**
      * @return
      */
     RevTree getTypeTree() {
         String refSpec = Ref.WORK_HEAD + ":" + getTypeTreePath();
         CommandLocator commandLocator = getCommandLocator();
         Optional<RevTree> ref = commandLocator.command(RevObjectParse.class).setRefSpec(refSpec)
                 .call(RevTree.class);
         Preconditions.checkState(ref.isPresent(), "Ref %s not found on working tree", refSpec);
         return ref.get();
     }
 
     /**
      * @return
      */
     WorkingTree getWorkingTree() {
         Transaction transaction = getTransaction();
         GeoGitDataStore dataStore = getDataStore();
         return dataStore.getWorkingTree(transaction);
     }
 }
