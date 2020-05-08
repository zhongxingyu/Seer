 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.repository;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Nullable;
 import javax.xml.namespace.QName;
 
 import org.geogit.api.MutableTree;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.SpatialRef;
 import org.geogit.api.RevObject.TYPE;
 import org.geogit.api.RevTree;
 import org.geogit.api.TreeVisitor;
 import org.geogit.api.plumbing.DiffWorkTree;
 import org.geogit.api.plumbing.FindOrCreateSubtree;
 import org.geogit.api.plumbing.FindTreeChild;
 import org.geogit.api.plumbing.ResolveTreeish;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.WriteBack;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.storage.ObjectSerialisingFactory;
 import org.geogit.storage.ObjectWriter;
 import org.geogit.storage.StagingDatabase;
 import org.opengis.filter.Filter;
 import org.opengis.geometry.BoundingBox;
 import org.opengis.util.ProgressListener;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 
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
  * @param the actual type representing a Feature
  * @param the actual type representing a FeatureType
  * @author Gabriel Roldan
  * @see Repository
  */
 public class WorkingTree {
 
     @Inject
     private StagingDatabase indexDatabase;
 
     @Inject
     private Repository repository;
 
     @Inject
     private ObjectSerialisingFactory serialFactory;
 
     /**
      * Updates the WORK_HEAD ref to the specified tree.
      * 
      * @param newTree
      */
     private void updateWorkHead(ObjectId newTree) {
         repository.command(UpdateRef.class).setName(Ref.WORK_HEAD).setNewValue(newTree).call();
     }
 
     /**
      * @return the tree represented by WORK_HEAD. If there is no tree set at WORK_HEAD, it will
      *         return the HEAD tree (no unstaged changes).
      */
     public RevTree getTree() {
         Optional<ObjectId> workTreeId = repository.command(ResolveTreeish.class)
                 .setTreeish(Ref.WORK_HEAD).call();
         final RevTree workTree;
         if (!workTreeId.isPresent() || workTreeId.get().isNull()) {
             // Work tree was not resolved, update it to the head.
             RevTree headTree = repository.getOrCreateHeadTree();
             updateWorkHead(headTree.getId());
             workTree = headTree;
 
         } else {
             workTree = repository.command(RevObjectParse.class).setObjectId(workTreeId.get())
                     .call(RevTree.class).or(RevTree.NULL);
         }
         return workTree;
     }
 
     /**
      * @return a supplier for the working tree.
      */
     private Supplier<MutableTree> getTreeSupplier() {
         Supplier<MutableTree> supplier = new Supplier<MutableTree>() {
             @Override
             public MutableTree get() {
                 return getTree().mutable();
             }
         };
         return Suppliers.memoize(supplier);
     }
 
     /**
      * Deletes a single feature from the working tree and updates the WORK_HEAD ref.
      * 
      * @param path
      * @param featureId
      * @return true if the object was found and deleted, false otherwise
      */
     public boolean delete(final String path, final String featureId) {
         MutableTree parentTree = repository.command(FindOrCreateSubtree.class).setIndex(true)
                 .setParent(Suppliers.ofInstance(Optional.of(getTree()))).setChildPath(path).call()
                 .mutable();
 
         String featurePath = NodeRef.appendChild(path, featureId);
         Optional<NodeRef> ref = findUnstaged(featurePath);
         if (ref.isPresent()) {
             parentTree.remove(ref.get().getPath());
         }
 
         ObjectId newTree = repository.command(WriteBack.class).setAncestor(getTreeSupplier())
                 .setChildPath(path).setIndex(true).setTree(parentTree).call();
 
         updateWorkHead(newTree);
 
         return ref.isPresent();
     }
 
     /**
      * Deletes a collection of features of the same type from the working tree and updates the
      * WORK_HEAD ref.
      * 
      * @param typeName
      * @param filter - currently unused
      * @param affectedFeatures
      * @throws Exception
      */
     public void delete(final QName typeName, final Filter filter,
             final Iterator<RevFeature> affectedFeatures) throws Exception {
 
         MutableTree parentTree = repository.command(FindOrCreateSubtree.class)
                 .setParent(Suppliers.ofInstance(Optional.of(getTree()))).setIndex(true)
                 .setChildPath(typeName.getLocalPart()).call().mutable();
 
         String fid;
         String featurePath;
 
         while (affectedFeatures.hasNext()) {
             fid = affectedFeatures.next().getFeatureId();
             featurePath = NodeRef.appendChild(typeName.getLocalPart(), fid);
             Optional<NodeRef> ref = findUnstaged(featurePath);
             if (ref.isPresent()) {
                 parentTree.remove(ref.get().getPath());
             }
         }
 
         ObjectId newTree = repository.command(WriteBack.class).setAncestor(getTree().mutable())
                 .setChildPath(typeName.getLocalPart()).setIndex(true).setTree(parentTree).call();
 
         updateWorkHead(newTree);
     }
 
     /**
      * Deletes a feature type from the working tree and updates the WORK_HEAD ref.
      * 
      * @param typeName
      * @throws Exception
      */
     public void delete(final QName typeName) throws Exception {
         // Not implemented
         throw new UnsupportedOperationException("not yet implemented");
     }
 
     /**
      * Insert a single feature into the working tree and updates the WORK_HEAD ref.
      * 
      * @param parentTreePath
      * @param feature
      */
     public NodeRef insert(final String parentTreePath, final RevFeature feature) {
         NodeRef ref = putInDatabase(parentTreePath, feature);
         MutableTree parentTree = repository.command(FindOrCreateSubtree.class).setIndex(true)
                 .setParent(Suppliers.ofInstance(Optional.of(getTree())))
                 .setChildPath(parentTreePath).call().mutable();
 
         parentTree.put(ref);
 
         ObjectId newTree = repository.command(WriteBack.class).setAncestor(getTreeSupplier())
                 .setChildPath(parentTreePath).setIndex(true).setTree(parentTree).call();
 
         updateWorkHead(newTree);
         return ref;
     }
 
     /**
      * Inserts a collection of features into the working tree and updates the WORK_HEAD ref.
      * 
      * @param treePath
      * @param features
      * @param forceUseProvidedFID - currently unused
      * @param listener
      * @param insertedTarget
      * @param collectionSize
      * @throws Exception
      */
     public void insert(final String treePath, Iterator<RevFeature> features,
             boolean forceUseProvidedFID, ProgressListener listener,
             @Nullable List<NodeRef> insertedTarget, @Nullable Integer collectionSize)
             throws Exception {
 
         checkArgument(collectionSize == null || collectionSize.intValue() > -1);
 
         final Integer size = collectionSize == null || collectionSize.intValue() < 1 ? null
                 : collectionSize.intValue();
 
         MutableTree parentTree = repository.command(FindOrCreateSubtree.class).setIndex(true)
                 .setParent(Suppliers.ofInstance(Optional.of(getTree()))).setChildPath(treePath)
                 .call().mutable();
 
         putInDatabase(treePath, features, listener, size, insertedTarget, parentTree);
 
         ObjectId newTree = repository.command(WriteBack.class).setAncestor(getTreeSupplier())
                 .setChildPath(treePath).setIndex(true).setTree(parentTree).call();
 
         updateWorkHead(newTree);
     }
 
     /**
      * Updates a collection of features in the working tree and updates the WORK_HEAD ref.
      * 
      * @param treePath
      * @param features
      * @param listener
      * @param collectionSize
      * @throws Exception
      */
     public void update(final String treePath, final Iterator<RevFeature> features,
             final ProgressListener listener, @Nullable final Integer collectionSize)
             throws Exception {
 
         checkArgument(collectionSize == null || collectionSize.intValue() > -1);
 
         final Integer size = collectionSize == null || collectionSize.intValue() < 1 ? null
                 : collectionSize.intValue();
 
         insert(treePath, features, false, listener, null, size);
     }
 
     /**
      * Determines if a specific feature type is versioned (existing in the main repository).
      * 
      * @param typeName
      * @return true if the feature type is versioned, false otherwise.
      */
     public boolean hasRoot(final QName typeName) {
         String localPart = typeName.getLocalPart();
         Optional<NodeRef> typeNameTreeRef = repository.command(FindTreeChild.class)
                 .setChildPath(localPart).call();
         return typeNameTreeRef.isPresent();
     }
 
     /**
      * @param pathFilter
      * @return an iterator for all of the differences between the work tree and the index based on
      *         the path filter.
      */
     public Iterator<DiffEntry> getUnstaged(final @Nullable String pathFilter) {
         Iterator<DiffEntry> unstaged = repository.command(DiffWorkTree.class).setFilter(pathFilter)
                 .call();
         return unstaged;
     }
 
     /**
      * @param pathFilter
      * @return the number differences between the work tree and the index based on the path filter.
      */
     public int countUnstaged(final @Nullable String pathFilter) {
         Iterator<DiffEntry> unstaged = getUnstaged(pathFilter);
         int count = 0;
         while (unstaged.hasNext()) {
             count++;
             unstaged.next();
         }
         return count;
     }
 
     /**
      * @param path
      * @return the NodeRef for the feature at the specified path if it exists in the work tree,
      *         otherwise Optional.absent()
      */
     public Optional<NodeRef> findUnstaged(final String path) {
         Optional<NodeRef> entry = repository.command(FindTreeChild.class).setIndex(true)
                 .setParent(getTree()).setChildPath(path).call();
         return entry;
     }
 
     /**
      * Adds a single feature to the staging database.
      * 
      * @param path
      * @return the NodeRef for the inserted feature
      */
     private NodeRef putInDatabase(final String parentTreePath, final RevFeature feature) {
         NodeRef.checkValidPath(parentTreePath);
         checkNotNull(feature);
 
         final ObjectWriter<?> featureWriter = serialFactory.createFeatureWriter(feature);
 
         final RevFeatureType featureType = feature.getFeatureType();
         final ObjectWriter<RevFeatureType> featureTypeWriter = serialFactory
                 .createFeatureTypeWriter(featureType);
 
         final BoundingBox bounds = feature.getBounds();
         final String nodePath = NodeRef.appendChild(parentTreePath, feature.getFeatureId());
 
         final ObjectId objectId = indexDatabase.put(featureWriter);
         final ObjectId metadataId;
         if (featureType.getId().isNull()) {
             metadataId = indexDatabase.put(featureTypeWriter);
         } else {
             metadataId = featureType.getId();
         }
 
         NodeRef newObject;
         if (bounds == null) {
             newObject = new NodeRef(nodePath, objectId, metadataId, TYPE.FEATURE);
         } else {
             newObject = new SpatialRef(nodePath, objectId, metadataId, TYPE.FEATURE, bounds);
         }
 
         return newObject;
     }
 
     /**
      * Adds a collection of features to the staging database.
      * 
      * @param parentTreepath
      * @param objects
      * @param progress
      * @param size
      * @param target
      * @throws Exception
      */
     private void putInDatabase(final String parentTreePath, final Iterator<RevFeature> objects,
             final ProgressListener progress, final @Nullable Integer size,
             @Nullable final List<NodeRef> target, MutableTree parentTree) throws Exception {
 
         checkNotNull(objects);
         checkNotNull(progress);
         checkNotNull(parentTree);
 
         RevFeature revFeature;
         int count = 0;
 
         progress.started();
         while (objects.hasNext()) {
             count++;
             if (progress.isCanceled()) {
                 return;
             }
             if (size != null) {
                 progress.progress((float) (count * 100) / size.intValue());
             }
 
             revFeature = objects.next();
            NodeRef objectRef = insert(parentTreePath, revFeature);
 
             parentTree.put(objectRef);
 
             if (target != null) {
                 target.add(objectRef);
             }
         }
         progress.complete();
     }
 
     /**
      * @return
      */
     public List<QName> getFeatureTypeNames() {
         // List<QName> names = new ArrayList<QName>();
         RevTree root = getTree();
 
         final List<QName> typeNames = Lists.newLinkedList();
         if (root != null) {
             root.accept(new TreeVisitor() {
 
                 @Override
                 public boolean visitSubTree(int bucket, ObjectId treeId) {
                     return false;
                 }
 
                 @Override
                 public boolean visitEntry(NodeRef ref) {
                     if (TYPE.TREE.equals(ref.getType())) {
                         typeNames.add(new QName(ref.getPath()));
                         // if (!ref.getMetadataId().isNull()) {
                         // ObjectId metadataId = ref.getMetadataId();
                         // ObjectSerialisingFactory serialFactory;
                         // serialFactory = repository.getSerializationFactory();
                         // ObjectReader<RevFeatureType> typeReader = serialFactory
                         // .createFeatureTypeReader();
                         // RevFeatureType type = indexDatabase.get(metadataId, typeReader);
                         // typeNames.add(type.getName());
                         // }
                         return true;
                     } else {
                         return false;
                     }
                 }
             });
         }
         return typeNames;
     }
 
     // public RevTree getHeadVersion(final QName typeName) {
     // final String featureTreePath = path(typeName, null);
     // Optional<NodeRef> typeTreeRef = repository.getRootTreeChild(featureTreePath);
     // RevTree typeTree;
     // if (typeTreeRef.isPresent()) {
     // typeTree = repository.getTree(typeTreeRef.get().getObjectId());
     // } else {
     // typeTree = repository.newTree();
     // }
     // return typeTree;
     // }
     //
     // public RevTree getStagedVersion(final QName typeName) {
     //
     // RevTree typeTree = getHeadVersion(typeName);
     //
     // String path = path(typeName, null);
     // StagingDatabase database = index.getDatabase();
     // final int stagedCount = database.countStaged(path);
     // if (stagedCount == 0) {
     // return typeTree;
     // }
     // return new DiffTree(typeTree, path, index);
     // }
     //
     // private static class DiffTree implements RevTree {
     //
     // private final RevTree typeTree;
     //
     // private final Map<String, NodeRef> inserts = new HashMap<String, NodeRef>();
     //
     // private final Map<String, NodeRef> updates = new HashMap<String, NodeRef>();
     //
     // private final Set<String> deletes = new HashSet<String>();
     //
     // public DiffTree(final RevTree typeTree, final String basePath, final StagingArea index) {
     // this.typeTree = typeTree;
     //
     // Iterator<NodeRef> staged = index.getDatabase().getStaged(basePath);
     // NodeRef entry;
     // String fid;
     // while (staged.hasNext()) {
     // entry = staged.next();
     // switch (entry.changeType()) {
     // case ADDED:
     // fid = fid(entry.newPath());
     // inserts.put(fid, entry.getNewObject());
     // break;
     // case REMOVED:
     // fid = fid(entry.oldPath());
     // deletes.add(fid);
     // break;
     // case MODIFIED:
     // fid = fid(entry.newPath());
     // updates.put(fid, entry.getNewObject());
     // break;
     // default:
     // throw new IllegalStateException();
     // }
     // }
     // }
     //
     // /**
     // * Extracts the feature id (last path step) from a full path
     // */
     // private String fid(String featurePath) {
     // int idx = featurePath.lastIndexOf(NodeRef.PATH_SEPARATOR);
     // return featurePath.substring(idx);
     // }
     //
     // @Override
     // public TYPE getType() {
     // return TYPE.TREE;
     // }
     //
     // @Override
     // public ObjectId getId() {
     // return null;
     // }
     //
     // @Override
     // public boolean isNormalized() {
     // return false;
     // }
     //
     // @Override
     // public MutableTree mutable() {
     // throw new UnsupportedOperationException();
     // }
     //
     // @Override
     // public Optional<NodeRef> get(final String fid) {
     // NodeRef ref = inserts.get(fid);
     // if (ref == null) {
     // ref = updates.get(fid);
     // if (ref == null) {
     // return this.typeTree.get(fid);
     // }
     // }
     // return Optional.of(ref);
     // }
     //
     // @Override
     // public void accept(TreeVisitor visitor) {
     // throw new UnsupportedOperationException();
     // }
     //
     // @Override
     // public BigInteger size() {
     // BigInteger size = typeTree.size();
     // if (inserts.size() > 0) {
     // size = size.add(BigInteger.valueOf(inserts.size()));
     // }
     // if (deletes.size() > 0) {
     // size = size.subtract(BigInteger.valueOf(deletes.size()));
     // }
     // return size;
     // }
     //
     // @Override
     // public Iterator<NodeRef> iterator(Predicate<NodeRef> filter) {
     // Iterator<NodeRef> current = typeTree.iterator(null);
     //
     // current = Iterators.filter(current, new Predicate<NodeRef>() {
     // @Override
     // public boolean apply(NodeRef input) {
     // boolean returnIt = !deletes.contains(input.getPath());
     // return returnIt;
     // }
     // });
     // current = Iterators.transform(current, new Function<NodeRef, NodeRef>() {
     // @Override
     // public NodeRef apply(NodeRef input) {
     // NodeRef update = updates.get(input.getPath());
     // return update == null ? input : update;
     // }
     // });
     //
     // Iterator<NodeRef> inserted = inserts.values().iterator();
     // if (filter != null) {
     // inserted = Iterators.filter(inserted, filter);
     // current = Iterators.filter(current, filter);
     // }
     //
     // Iterator<NodeRef> diffed = Iterators.concat(inserted, current);
     // return diffed;
     // }
     //
     // }
 }
