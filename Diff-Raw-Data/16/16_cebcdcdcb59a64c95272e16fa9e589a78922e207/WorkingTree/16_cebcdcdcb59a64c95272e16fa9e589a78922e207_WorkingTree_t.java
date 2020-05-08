 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 package org.geogit.repository;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.CommandLocator;
 import org.geogit.api.FeatureBuilder;
 import org.geogit.api.Node;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureBuilder;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevObject.TYPE;
 import org.geogit.api.RevTree;
 import org.geogit.api.RevTreeBuilder;
 import org.geogit.api.data.FindFeatureTypeTrees;
 import org.geogit.api.plumbing.DiffCount;
 import org.geogit.api.plumbing.DiffWorkTree;
 import org.geogit.api.plumbing.FindOrCreateSubtree;
 import org.geogit.api.plumbing.FindTreeChild;
 import org.geogit.api.plumbing.LsTreeOp;
 import org.geogit.api.plumbing.LsTreeOp.Strategy;
 import org.geogit.api.plumbing.ResolveTreeish;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.WriteBack;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.storage.StagingDatabase;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.feature.Feature;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.feature.type.Name;
 import org.opengis.filter.Filter;
 import org.opengis.geometry.BoundingBox;
 import org.opengis.util.ProgressListener;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 import com.google.common.collect.PeekingIterator;
 import com.google.inject.Inject;
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * A working tree is the collection of Features for a single FeatureType in GeoServer that has a
  * repository associated with it (and hence is subject of synchronization).
  * <p>
  * It represents the set of Features tracked by some kind of geospatial data repository (like the
  * GeoServer Catalog). It is essentially a "tree" with various roots and only one level of nesting,
  * since the FeatureTypes held in this working tree are the equivalents of files in a git working
  * tree.
  * </p>
  * <p>
  * <ul>
  * <li>A WorkingTree represents the current working copy of the versioned feature types
  * <li>A WorkingTree has a Repository
  * <li>A Repository holds commits and branches
  * <li>You perform work on the working tree (insert/delete/update features)
  * <li>Then you commit to the current Repository's branch
  * <li>You can checkout a different branch from the Repository and the working tree will be updated
  * to reflect the state of that branch
  * </ul>
  * 
  * @see Repository
  */
 public class WorkingTree {
 
     private StagingDatabase indexDatabase;
 
     private CommandLocator commandLocator;
 
     @Inject
     public WorkingTree(final StagingDatabase indexDb, final CommandLocator commandLocator) {
         Preconditions.checkNotNull(indexDb);
         Preconditions.checkNotNull(commandLocator);
         this.indexDatabase = indexDb;
         this.commandLocator = commandLocator;
     }
 
     /**
      * Updates the WORK_HEAD ref to the specified tree.
      * 
      * @param newTree the tree to be set as the new WORK_HEAD
      */
     public void updateWorkHead(ObjectId newTree) {
 
         commandLocator.command(UpdateRef.class).setName(Ref.WORK_HEAD).setNewValue(newTree).call();
     }
 
     /**
      * @return the tree represented by WORK_HEAD. If there is no tree set at WORK_HEAD, it will
      *         return the HEAD tree (no unstaged changes).
      */
     public RevTree getTree() {
         Optional<ObjectId> workTreeId = commandLocator.command(ResolveTreeish.class)
                 .setTreeish(Ref.WORK_HEAD).call();
         final RevTree workTree;
         if (!workTreeId.isPresent() || workTreeId.get().isNull()) {
             // Work tree was not resolved, update it to the head.
             Optional<ObjectId> headTreeId = commandLocator.command(ResolveTreeish.class)
                     .setTreeish(Ref.HEAD).call();
             final RevTree headTree;
             if (!headTreeId.isPresent() || headTreeId.get().isNull()) {
                 headTree = RevTree.EMPTY;
             } else {
                 headTree = commandLocator.command(RevObjectParse.class)
                         .setObjectId(headTreeId.get()).call(RevTree.class).get();
             }
             updateWorkHead(headTree.getId());
             workTree = headTree;
         } else {
             workTree = commandLocator.command(RevObjectParse.class).setObjectId(workTreeId.get())
                     .call(RevTree.class).or(RevTree.EMPTY);
         }
         Preconditions.checkState(workTree != null);
         return workTree;
     }
 
     /**
      * @return a supplier for the working tree.
      */
     private Supplier<RevTreeBuilder> getTreeSupplier() {
         Supplier<RevTreeBuilder> supplier = new Supplier<RevTreeBuilder>() {
             @Override
             public RevTreeBuilder get() {
                 return getTree().builder(indexDatabase);
             }
         };
         return Suppliers.memoize(supplier);
     }
 
     /**
      * Deletes a single feature from the working tree and updates the WORK_HEAD ref.
      * 
      * @param path the path of the feature
      * @param featureId the id of the feature
      * @return true if the object was found and deleted, false otherwise
      */
     public boolean delete(final String path, final String featureId) {
         Optional<NodeRef> typeTreeRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(getTree()).setChildPath(path).call();
 
         ObjectId metadataId = null;
         if (typeTreeRef.isPresent()) {
             metadataId = typeTreeRef.get().getMetadataId();
         }
 
         RevTreeBuilder parentTree = commandLocator.command(FindOrCreateSubtree.class)
                 .setIndex(true).setParent(Suppliers.ofInstance(Optional.of(getTree())))
                 .setChildPath(path).call().builder(indexDatabase);
 
         String featurePath = NodeRef.appendChild(path, featureId);
         Optional<Node> node = findUnstaged(featurePath);
         if (node.isPresent()) {
             parentTree.remove(node.get().getName());
         }
 
         ObjectId newTree = commandLocator.command(WriteBack.class).setAncestor(getTreeSupplier())
                 .setChildPath(path).setToIndex(true).setMetadataId(metadataId)
                 .setTree(parentTree.build()).call();
 
         updateWorkHead(newTree);
 
         return node.isPresent();
     }
 
     /**
      * Deletes a tree and the features it contains from the working tree and updates the WORK_HEAD
      * ref.
      * 
      * @param path the path to the tree to delete
      * @throws Exception
      */
     public void delete(final String path) {
 
         final String parentPath = NodeRef.parentPath(path);
         final String childName = NodeRef.nodeFromPath(path);
 
         final RevTree workHead = getTree();
 
         RevTree parent;
         RevTreeBuilder parentBuilder;
         ObjectId parentMetadataId = ObjectId.NULL;
         if (parentPath.isEmpty()) {
             parent = workHead;
             parentBuilder = workHead.builder(indexDatabase);
         } else {
             Optional<NodeRef> parentRef = commandLocator.command(FindTreeChild.class)
                     .setParent(workHead).setChildPath(parentPath).setIndex(true).call();
             if (!parentRef.isPresent()) {
                 return;
             }
 
             parentMetadataId = parentRef.get().getMetadataId();
             parent = commandLocator.command(RevObjectParse.class)
                     .setObjectId(parentRef.get().objectId()).call(RevTree.class).get();
             parentBuilder = parent.builder(indexDatabase);
         }
         RevTree newParent = parentBuilder.remove(childName).build();
         indexDatabase.put(newParent);
         if (parent.getId().equals(newParent.getId())) {
             return;// nothing changed
         }
 
         ObjectId newWorkHead;
         if (parentPath.isEmpty()) {
             newWorkHead = newParent.getId();
         } else {
             newWorkHead = commandLocator.command(WriteBack.class).setToIndex(true)
                     .setAncestor(workHead.builder(indexDatabase)).setChildPath(parentPath)
                     .setTree(newParent).setMetadataId(parentMetadataId).call();
         }
         updateWorkHead(newWorkHead);
     }
 
     /**
      * Deletes a collection of features of the same type from the working tree and updates the
      * WORK_HEAD ref.
      * 
      * @param typeName feature type
      * @param filter - currently unused
      * @param affectedFeatures features to remove
      * @throws Exception
      */
     public void delete(final Name typeName, final Filter filter,
             final Iterator<Feature> affectedFeatures) throws Exception {
 
         Optional<NodeRef> typeTreeRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(getTree()).setChildPath(typeName.getLocalPart()).call();
 
         ObjectId parentMetadataId = null;
         if (typeTreeRef.isPresent()) {
             parentMetadataId = typeTreeRef.get().getMetadataId();
         }
 
         RevTreeBuilder parentTree = commandLocator.command(FindOrCreateSubtree.class)
                 .setParent(Suppliers.ofInstance(Optional.of(getTree()))).setIndex(true)
                 .setChildPath(typeName.getLocalPart()).call().builder(indexDatabase);
 
         String fid;
         String featurePath;
 
         while (affectedFeatures.hasNext()) {
             fid = affectedFeatures.next().getIdentifier().getID();
             featurePath = NodeRef.appendChild(typeName.getLocalPart(), fid);
             Optional<Node> ref = findUnstaged(featurePath);
             if (ref.isPresent()) {
                 parentTree.remove(ref.get().getName());
             }
         }
 
         ObjectId newTree = commandLocator.command(WriteBack.class)
                 .setAncestor(getTree().builder(indexDatabase)).setMetadataId(parentMetadataId)
                 .setChildPath(typeName.getLocalPart()).setToIndex(true).setTree(parentTree.build())
                 .call();
 
         updateWorkHead(newTree);
     }
 
     /**
      * Deletes a feature type from the working tree and updates the WORK_HEAD ref.
      * 
      * @param typeName feature type to remove
      * @throws Exception
      */
     public void delete(final Name typeName) throws Exception {
         checkNotNull(typeName);
 
         RevTreeBuilder workRoot = getTree().builder(indexDatabase);
 
         final String treePath = typeName.getLocalPart();
         if (workRoot.get(treePath).isPresent()) {
             workRoot.remove(treePath);
             RevTree newRoot = workRoot.build();
             indexDatabase.put(newRoot);
             updateWorkHead(newRoot.getId());
         }
     }
 
     /**
      * 
      * @param features the features to delete
      */
     public void delete(Iterator<String> features) {
         Map<String, RevTreeBuilder> parents = Maps.newHashMap();
 
         final RevTree currentWorkHead = getTree();
         while (features.hasNext()) {
             String featurePath = features.next();
             // System.err.println("removing " + feature);
             String parentPath = NodeRef.parentPath(featurePath);
             RevTreeBuilder parentTree;
             if (parents.containsKey(parentPath)) {
                 parentTree = parents.get(parentPath);
             } else {
                 parentTree = commandLocator.command(FindOrCreateSubtree.class).setIndex(true)
                         .setParent(Suppliers.ofInstance(Optional.of(currentWorkHead)))
                         .setChildPath(parentPath).call().builder(indexDatabase);
                 parents.put(parentPath, parentTree);
             }
             String featureName = NodeRef.nodeFromPath(featurePath);
             parentTree.remove(featureName);
         }
         ObjectId newTree = null;
         for (Map.Entry<String, RevTreeBuilder> entry : parents.entrySet()) {
             String path = entry.getKey();
 
             RevTreeBuilder parentTree = entry.getValue();
             RevTree newTypeTree = parentTree.build();
 
             ObjectId metadataId = null;
             Optional<NodeRef> currentTreeRef = commandLocator.command(FindTreeChild.class)
                     .setIndex(true).setParent(currentWorkHead).setChildPath(path).call();
             if (currentTreeRef.isPresent()) {
                 metadataId = currentTreeRef.get().getMetadataId();
             }
             newTree = commandLocator.command(WriteBack.class).setAncestor(getTreeSupplier())
                     .setChildPath(path).setToIndex(true).setTree(newTypeTree)
                     .setMetadataId(metadataId).call();
             updateWorkHead(newTree);
         }
     }
 
     public NodeRef createTypeTree(final String treePath, final FeatureType featureType) {
 
         final RevTree workHead = getTree();
         Optional<NodeRef> typeTreeRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(workHead).setChildPath(treePath).call();
         Preconditions
                 .checkArgument(!typeTreeRef.isPresent(), "Tree already exists at %s", treePath);
 
         final RevFeatureType revType = RevFeatureType.build(featureType);
         indexDatabase.put(revType);
 
         final ObjectId metadataId = revType.getId();
         final RevTree newTree = new RevTreeBuilder(indexDatabase).build();
 
         ObjectId newWorkHeadId = commandLocator.command(WriteBack.class).setToIndex(true)
                 .setAncestor(workHead.builder(indexDatabase)).setChildPath(treePath)
                 .setTree(newTree).setMetadataId(metadataId).call();
         updateWorkHead(newWorkHeadId);
 
         return commandLocator.command(FindTreeChild.class).setIndex(true).setParent(getTree())
                 .setChildPath(treePath).call().get();
     }
 
     /**
      * Insert a single feature into the working tree and updates the WORK_HEAD ref.
      * 
      * @param parentTreePath path of the parent tree to insert the feature into
      * @param feature the feature to insert
      */
     public Node insert(final String parentTreePath, final Feature feature) {
 
         final FeatureType featureType = feature.getType();
 
         NodeRef treeRef;
 
         Optional<NodeRef> typeTreeRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(getTree()).setChildPath(parentTreePath).call();
 
         ObjectId metadataId;
         if (typeTreeRef.isPresent()) {
             treeRef = typeTreeRef.get();
             RevFeatureType newFeatureType = RevFeatureType.build(featureType);
             metadataId = newFeatureType.getId().equals(treeRef.getMetadataId()) ? ObjectId.NULL
                     : newFeatureType.getId();
             if (!newFeatureType.getId().equals(treeRef.getMetadataId())) {
                 indexDatabase.put(newFeatureType);
             }
         } else {
             treeRef = createTypeTree(parentTreePath, featureType);
             metadataId = ObjectId.NULL;// treeRef.getMetadataId();
         }
 
         // ObjectId metadataId = treeRef.getMetadataId();
         final Node node = putInDatabase(feature, metadataId);
 
         RevTreeBuilder parentTree = commandLocator.command(FindOrCreateSubtree.class)
                 .setIndex(true).setParent(Suppliers.ofInstance(Optional.of(getTree())))
                 .setChildPath(parentTreePath).call().builder(indexDatabase);
 
         parentTree.put(node);
         final ObjectId treeMetadataId = treeRef.getMetadataId();
 
         ObjectId newTree = commandLocator.command(WriteBack.class).setAncestor(getTreeSupplier())
                 .setChildPath(parentTreePath).setToIndex(true).setTree(parentTree.build())
                 .setMetadataId(treeMetadataId).call();
 
         updateWorkHead(newTree);
 
         final String featurePath = NodeRef.appendChild(parentTreePath, node.getName());
         Optional<NodeRef> featureRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(getTree()).setChildPath(featurePath).call();
         return featureRef.get().getNode();
     }
 
     /**
      * Inserts a collection of features into the working tree and updates the WORK_HEAD ref.
      * 
      * @param treePath the path of the tree to insert the features into
      * @param features the features to insert
      * @param listener a {@link ProgressListener} for the current process
      * @param insertedTarget if provided, inserted features will be added to this list
      * @param collectionSize number of features to add
      * @throws Exception
      */
     public void insert(final String treePath, Iterator<? extends Feature> features,
             final ProgressListener listener, @Nullable final List<Node> insertedTarget,
             @Nullable final Integer collectionSize) {
 
         checkArgument(collectionSize == null || collectionSize.intValue() > -1);
 
         final NodeRef treeRef;
         {
             Optional<NodeRef> typeTreeRef = commandLocator.command(FindTreeChild.class)
                     .setIndex(true).setParent(getTree()).setChildPath(treePath).call();
 
             if (typeTreeRef.isPresent()) {
                 treeRef = typeTreeRef.get();
             } else {
                 Preconditions.checkArgument(features.hasNext(),
                         "Can't create new FeatureType tree %s as no features were provided, "
                                 + "try using createTypeTree() first", treePath);
 
                 features = Iterators.peekingIterator(features);
 
                 FeatureType featureType = ((PeekingIterator<Feature>) features).peek().getType();
                 treeRef = createTypeTree(treePath, featureType);
             }
         }
 
        final ObjectId defaultMetadataId = treeRef.getMetadataId();
        final Map<Name, ObjectId> revFeatureTypes = Maps.newHashMap();
 
         final RevTreeBuilder typeTreeBuilder = commandLocator.command(FindOrCreateSubtree.class)
                 .setIndex(true).setParent(Suppliers.ofInstance(Optional.of(getTree())))
                 .setChildPath(treePath).call().builder(indexDatabase);
 
         Iterator<RevObject> objects = Iterators.transform(features,
                 new Function<Feature, RevObject>() {
 
                     private RevFeatureBuilder builder = new RevFeatureBuilder();
 
                     private int count;
 
                     @Override
                     public RevFeature apply(Feature feature) {
                         final RevFeature revFeature = builder.build(feature);
                        FeatureType featureType = feature.getType();
                        ObjectId revFeatureTypeId = revFeatureTypes.get(featureType.getName());

                        if (null == revFeatureTypeId) {
                            RevFeatureType newFeatureType = RevFeatureType.build(featureType);
                            revFeatureTypeId = newFeatureType.getId();
                            indexDatabase.put(newFeatureType);
                            revFeatureTypes.put(feature.getType().getName(), revFeatureTypeId);
                        }

                        ObjectId metadataId = defaultMetadataId.equals(revFeatureTypeId) ? ObjectId.NULL
                                : revFeatureTypeId;
                         Node node = createNode(metadataId, feature, revFeature);
 
                         if (insertedTarget != null) {
                             insertedTarget.add(node);
                         }
                         typeTreeBuilder.put(node);
 
                         count++;
                         if (collectionSize != null) {
                             listener.progress((float) (count * 100) / collectionSize.intValue());
                         }
                         return revFeature;
                     }
 
                 });
 
         // System.err.println("\n inserting rev features...");
         // Stopwatch sw = new Stopwatch().start();
         listener.started();
         indexDatabase.putAll(objects);
         listener.complete();
         // sw.stop();
         // System.err.printf("\n%d features inserted in %s", collectionSize, sw);
 
         // System.err.println("\nBuilding final tree...");
         // sw.reset().start();
         RevTree newFeatureTree = typeTreeBuilder.build();
         indexDatabase.put(newFeatureTree);
         // sw.stop();
         // System.err.println("\nfinal tree built in " + sw);
 
         ObjectId newTree = commandLocator.command(WriteBack.class).setAncestor(getTreeSupplier())
                 .setChildPath(treePath).setMetadataId(treeRef.getMetadataId()).setToIndex(true)
                 .setTree(newFeatureTree).call();
 
         updateWorkHead(newTree);
     }
 
     private Node createNode(final ObjectId metadataId, Feature feature, final RevFeature revFeature) {
         final String name;
         final ObjectId oid;
         final Envelope env;
         name = feature.getIdentifier().getID();
         BoundingBox bounds = feature.getBounds();
         if (bounds instanceof ReferencedEnvelope) {
             env = (Envelope) bounds;
         } else if (bounds != null) {
             env = new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(),
                     bounds.getMaxY());
         } else {
             env = null;
         }
         oid = revFeature.getId();
         Node node = Node.create(name, oid, metadataId, TYPE.FEATURE, env);
         return node;
     }
 
     /**
      * Updates a collection of features in the working tree and updates the WORK_HEAD ref.
      * 
      * @param treePath the path of the tree to insert the features into
      * @param features the features to insert
      * @param listener a {@link ProgressListener} for the current process
      * @param collectionSize number of features to add
      * @throws Exception
      */
     public void update(final String treePath, final Iterator<Feature> features,
             final ProgressListener listener, @Nullable final Integer collectionSize)
             throws Exception {
 
         checkArgument(collectionSize == null || collectionSize.intValue() > -1);
 
         final Integer size = collectionSize == null || collectionSize.intValue() < 1 ? null
                 : collectionSize.intValue();
 
         insert(treePath, features, listener, null, size);
     }
 
     /**
      * Determines if a specific feature type is versioned (existing in the main repository).
      * 
      * @param typeName feature type to check
      * @return true if the feature type is versioned, false otherwise.
      */
     public boolean hasRoot(final Name typeName) {
         String localPart = typeName.getLocalPart();
 
         Optional<NodeRef> typeNameTreeRef = commandLocator.command(FindTreeChild.class)
                 .setIndex(true).setChildPath(localPart).call();
 
         return typeNameTreeRef.isPresent();
     }
 
     /**
      * @param pathFilter if specified, only changes that match the filter will be returned
      * @return an iterator for all of the differences between the work tree and the index based on
      *         the path filter.
      */
     public Iterator<DiffEntry> getUnstaged(final @Nullable String pathFilter) {
         Iterator<DiffEntry> unstaged = commandLocator.command(DiffWorkTree.class)
                 .setFilter(pathFilter).setReportTrees(true).call();
         return unstaged;
     }
 
     /**
      * @param pathFilter if specified, only changes that match the filter will be counted
      * @return the number differences between the work tree and the index based on the path filter.
      */
     public long countUnstaged(final @Nullable String pathFilter) {
         Long count = commandLocator.command(DiffCount.class).setOldVersion(Ref.STAGE_HEAD)
                 .setNewVersion(Ref.WORK_HEAD).addFilter(pathFilter).call();
         return count.longValue();
     }
 
     /**
      * @param path finds a {@link Node} for the feature at the given path in the index
      * @return the Node for the feature at the specified path if it exists in the work tree,
      *         otherwise Optional.absent()
      */
     public Optional<Node> findUnstaged(final String path) {
         Optional<NodeRef> nodeRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(getTree()).setChildPath(path).call();
         if (nodeRef.isPresent()) {
             return Optional.of(nodeRef.get().getNode());
         } else {
             return Optional.absent();
         }
     }
 
     /**
      * Adds a single feature to the staging database.
      * 
      * @param feature the feature to add
      * @param metadataId
      * @return the Node for the inserted feature
      */
     private Node putInDatabase(final Feature feature, final ObjectId metadataId) {
 
         checkNotNull(feature);
         checkNotNull(metadataId);
 
         final RevFeature newFeature = new RevFeatureBuilder().build(feature);
         final ObjectId objectId = newFeature.getId();
         final Envelope bounds = (ReferencedEnvelope) feature.getBounds();
         final String nodeName = feature.getIdentifier().getID();
 
         indexDatabase.put(newFeature);
 
         Node newObject = Node.create(nodeName, objectId, metadataId, TYPE.FEATURE, bounds);
         return newObject;
     }
 
     /**
      * Adds a collection of features to the staging database.
      * 
      * @param parentTreepath path of features
      * @param objects the features to insert
      * @param progress the {@link ProgressListener} for this process
      * @param size number of features to add
      * @param target if specified, created {@link Node}s will be added to the list
      * @param defaultMetadataId
      */
     private void putInDatabase(final String parentTreePath,
             final Iterator<? extends Feature> objects, final ProgressListener progress,
             final @Nullable Integer size, @Nullable final List<Node> target,
             final RevTreeBuilder parentTree, ObjectId defaultMetadataId) {
 
         checkNotNull(objects);
         checkNotNull(progress);
         checkNotNull(parentTree);
 
         Feature feature;
         int count = 0;
 
         progress.started();
 
         Map<Name, ObjectId> revFeatureTypes = Maps.newHashMap();
 
         while (objects.hasNext()) {
             count++;
             if (progress.isCanceled()) {
                 return;
             }
             if (size != null) {
                 progress.progress((float) (count * 100) / size.intValue());
             }
 
             feature = objects.next();
 
             final FeatureType featureType = feature.getType();
             ObjectId revFeatureTypeId = revFeatureTypes.get(featureType.getName());
 
             if (null == revFeatureTypeId) {
                 RevFeatureType newFeatureType = RevFeatureType.build(featureType);
 
                 revFeatureTypeId = newFeatureType.getId();
 
                 indexDatabase.put(newFeatureType);
                 revFeatureTypes.put(featureType.getName(), revFeatureTypeId);
             }
 
             final Node objectRef = putInDatabase(feature,
                     defaultMetadataId.equals(revFeatureTypeId) ? ObjectId.NULL : revFeatureTypeId);
             parentTree.put(objectRef);
             if (target != null) {
                 target.add(objectRef);
             }
         }
 
         progress.complete();
     }
 
     /**
      * @return a list of all the feature type names in the working tree
      * @see FindFeatureTypeTrees
      */
     public List<NodeRef> getFeatureTypeTrees() {
 
         List<NodeRef> typeTrees = commandLocator.command(FindFeatureTypeTrees.class)
                 .setRootTreeRef(Ref.WORK_HEAD).call();
         return typeTrees;
     }
 
     /**
      * Updates the definition of a Feature type associated as default feature type to a given path.
      * It also modifies the metadataId associated to features under the passed path, which used the
      * previous default feature type.
      * 
      * @param path the path
      * @param featureType the new feature type definition to set as default for the passed path
      */
     public NodeRef updateTypeTree(final String treePath, final FeatureType featureType) {
 
         // TODO: This is not the optimal way of doing this. A better solution should be found.
 
         final RevTree workHead = getTree();
         Optional<NodeRef> typeTreeRef = commandLocator.command(FindTreeChild.class).setIndex(true)
                 .setParent(workHead).setChildPath(treePath).call();
         Preconditions.checkArgument(typeTreeRef.isPresent(), "Tree does not exist: %s", treePath);
 
         Iterator<NodeRef> iter = commandLocator.command(LsTreeOp.class).setReference(treePath)
                 .setStrategy(Strategy.DEPTHFIRST_ONLY_FEATURES).call();
 
         final RevFeatureType revType = RevFeatureType.build(featureType);
         indexDatabase.put(revType);
 
         final ObjectId metadataId = revType.getId();
         RevTreeBuilder treeBuilder = new RevTreeBuilder(indexDatabase);
 
         final RevTree newTree = treeBuilder.build();
         ObjectId newWorkHeadId = commandLocator.command(WriteBack.class).setToIndex(true)
                 .setAncestor(workHead.builder(indexDatabase)).setChildPath(treePath)
                 .setTree(newTree).setMetadataId(metadataId).call();
         updateWorkHead(newWorkHeadId);
 
         Map<ObjectId, FeatureBuilder> featureBuilders = Maps.newHashMap();
         while (iter.hasNext()) {
             NodeRef noderef = iter.next();
             RevFeature feature = commandLocator.command(RevObjectParse.class)
                     .setObjectId(noderef.objectId()).call(RevFeature.class).get();
             if (!featureBuilders.containsKey(noderef.getMetadataId())) {
                 RevFeatureType ft = commandLocator.command(RevObjectParse.class)
                         .setObjectId(noderef.getMetadataId()).call(RevFeatureType.class).get();
                 featureBuilders.put(noderef.getMetadataId(), new FeatureBuilder(ft));
             }
             FeatureBuilder fb = featureBuilders.get(noderef.getMetadataId());
             String parentPath = NodeRef.parentPath(NodeRef.appendChild(treePath, noderef.path()));
             insert(parentPath, fb.build(noderef.getNode().getName(), feature));
         }
 
         return commandLocator.command(FindTreeChild.class).setIndex(true).setParent(getTree())
                 .setChildPath(treePath).call().get();
 
     }
 }
