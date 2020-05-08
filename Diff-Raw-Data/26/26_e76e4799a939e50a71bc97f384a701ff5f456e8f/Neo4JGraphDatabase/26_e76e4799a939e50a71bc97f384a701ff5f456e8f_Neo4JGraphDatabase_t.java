 package org.geogit.storage;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import org.geogit.api.ObjectId;
 import org.geogit.api.Platform;
 import org.geogit.api.plumbing.ResolveGeogitDir;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.index.Index;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.inject.Inject;
 
 public class Neo4JGraphDatabase extends AbstractGraphDatabase {
 
     GraphDatabaseService graphDB = null;
 
     private String dbPath = null;
 
     private static Map<String, GraphDatabaseService> databaseServices = new HashMap<String, GraphDatabaseService>();
 
     private final Platform platform;
 
     private enum CommitRelationshipTypes implements RelationshipType {
        TOROOT, PARENT
     }
 
     /**
      * Constructs a new {@code Neo4JGraphDatabase} using the given platform.
      * 
      * @param platform the platform to use.
      */
     @Inject
     public Neo4JGraphDatabase(final Platform platform) {
         this.platform = platform;
     }
 
     @Override
     public void open() {
         if (isOpen()) {
             return;
         }
         URL envHome = new ResolveGeogitDir(platform).call();
         if (envHome == null) {
             throw new IllegalStateException("Not inside a geogit directory");
         }
         if (!"file".equals(envHome.getProtocol())) {
             throw new UnsupportedOperationException(
                     "This Graph Database works only against file system repositories. "
                             + "Repository location: " + envHome.toExternalForm());
         }
         File repoDir;
         try {
             repoDir = new File(envHome.toURI());
         } catch (URISyntaxException e) {
             throw Throwables.propagate(e);
         }
         File graph = new File(repoDir, "graph");
         if (!graph.exists() && !graph.mkdir()) {
             throw new IllegalStateException("Cannot create graph directory '"
                     + graph.getAbsolutePath() + "'");
         }
 
         dbPath = graph.getAbsolutePath() + "/graphDB.db";
 
         if (databaseServices.containsKey(dbPath)) {
             graphDB = databaseServices.get(dbPath);
         } else {
             graphDB = getGraphDatabase(dbPath);
             databaseServices.put(dbPath, graphDB);
             registerShutdownHook(graphDB, dbPath);
         }
 
     }
 
     protected GraphDatabaseService getGraphDatabase(String dbPath) {
         return new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
     }
 
     private static void registerShutdownHook(final GraphDatabaseService graphDB, final String dbPath) {
         // Registers a shutdown hook for the Neo4j instance so that it
         // shuts down nicely when the VM exits (even if you "Ctrl-C" the
         // running example before it's completed)
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 graphDB.shutdown();
                 databaseServices.remove(dbPath);
             }
         });
     }
 
     @Override
     public boolean isOpen() {
         return graphDB != null;
     }
 
     @Override
     public void close() {
         if (isOpen()) {
             graphDB.shutdown();
             databaseServices.remove(dbPath);
             graphDB = null;
         }
 
     }
 
     @Override
     public boolean exists(ObjectId commitId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
         return node != null;
     }
 
     @Override
     public ImmutableList<ObjectId> getParents(ObjectId commitId) throws IllegalArgumentException {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
 
         Builder<ObjectId> listBuilder = new ImmutableList.Builder<ObjectId>();
 
         for (Relationship parent : node.getRelationships(CommitRelationshipTypes.PARENT)) {
             Node parentNode = parent.getEndNode();
             if (parentNode.getId() != node.getId()) {
                 listBuilder.add(ObjectId.valueOf((String) parentNode.getProperty("id")));
             }
         }
         return listBuilder.build();
     }
 
     @Override
     public boolean put(ObjectId commitId, ImmutableList<ObjectId> parentIds) {
         Transaction tx = graphDB.beginTx();
 
         Node commitNode = null;
         try {
             // See if it already exists
             commitNode = getOrAddNode(commitId);
 
            if (parentIds.isEmpty()) {
                if (!commitNode.getRelationships(CommitRelationshipTypes.TOROOT).iterator()
                        .hasNext()) {
                    // Attach this node to the root node
                    commitNode.createRelationshipTo(graphDB.getNodeById(0),
                            CommitRelationshipTypes.TOROOT);
                }
            }

            if (!commitNode.getRelationships(CommitRelationshipTypes.PARENT).iterator().hasNext()) {
                // Don't make relationships if they have been created already
                for (ObjectId parent : parentIds) {
                    Node parentNode = getOrAddNode(parent);
                    commitNode.createRelationshipTo(parentNode, CommitRelationshipTypes.PARENT);
                }
             }
 
             tx.success();
         } catch (Exception e) {
             tx.failure();
         } finally {
             tx.finish();
         }
 
         return true;
     }
 
     /**
      * Gets a node or adds it if it doesn't exist. Note, this must be called within a
      * {@link Transaction}.
      * 
      * @param commitId
      * @return
      */
     private Node getOrAddNode(ObjectId commitId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
         if (node == null) {
             node = graphDB.createNode();
             node.setProperty("id", commitId.toString());
             idIndex.add(node, "id", commitId.toString());
         }
         return node;
     }
 
     @Override
     public Optional<ObjectId> lowestCommonAncestor(ObjectId leftId, ObjectId rightId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
 
         Set<Node> leftSet = new HashSet<Node>();
         Set<Node> rightSet = new HashSet<Node>();
 
         Queue<Node> leftQueue = new LinkedList<Node>();
         leftQueue.add(idIndex.get("id", leftId.toString()).getSingle());
         Queue<Node> rightQueue = new LinkedList<Node>();
         rightQueue.add(idIndex.get("id", rightId.toString()).getSingle());
         List<Node> potentialCommonAncestors = new LinkedList<Node>();
         while (!leftQueue.isEmpty() || !rightQueue.isEmpty()) {
             if (!leftQueue.isEmpty()) {
                 Node commit = leftQueue.poll();
                 if (processCommit(commit, leftQueue, leftSet, rightQueue, rightSet)) {
                     potentialCommonAncestors.add(commit);
                 }
             }
             if (!rightQueue.isEmpty()) {
                 Node commit = rightQueue.poll();
                 if (processCommit(commit, rightQueue, rightSet, leftQueue, leftSet)) {
                     potentialCommonAncestors.add(commit);
                 }
             }
         }
         verifyAncestors(potentialCommonAncestors, leftSet, rightSet);
 
         Optional<ObjectId> ancestor = Optional.absent();
         if (potentialCommonAncestors.size() > 0) {
             ancestor = Optional.of(ObjectId.valueOf((String) potentialCommonAncestors.get(0)
                     .getProperty("id")));
         }
         return ancestor;
     }
 
     private boolean processCommit(Node commit, Queue<Node> myQueue, Set<Node> mySet,
             Queue<Node> theirQueue, Set<Node> theirSet) {
         if (!mySet.contains(commit)) {
             mySet.add(commit);
             if (theirSet.contains(commit)) {
                 // found a common ancestor
                 stopAncestryPath(commit, theirQueue, theirSet);
 
                 return true;
             }
             for (Relationship parent : commit.getRelationships(CommitRelationshipTypes.PARENT)) {
                 if (parent.getEndNode().getId() != commit.getId()) {
                     myQueue.add(parent.getEndNode());
                 }
             }
         }
         return false;
     }
 
     private void stopAncestryPath(Node commit, Queue<Node> theirQueue, Set<Node> theirSet) {
         Queue<Node> ancestorQueue = new LinkedList<Node>();
         ancestorQueue.add(commit);
         while (!ancestorQueue.isEmpty()) {
             Node ancestor = ancestorQueue.poll();
             for (Relationship parent : ancestor.getRelationships(CommitRelationshipTypes.PARENT)) {
                 Node parentNode = parent.getEndNode();
                 if (parentNode.getId() != ancestor.getId()) {
                     if (theirSet.contains(parentNode)) {
                         ancestorQueue.add(parentNode);
                     } else if (theirQueue.contains(parentNode)) {
                         theirQueue.remove(parentNode);
                     }
                 }
             }
         }
     }
 
     private void verifyAncestors(List<Node> potentialCommonAncestors, Set<Node> leftSet,
             Set<Node> rightSet) {
         Queue<Node> ancestorQueue = new LinkedList<Node>();
         List<Node> falseAncestors = new LinkedList<Node>();
         for (Node n : potentialCommonAncestors) {
             if (falseAncestors.contains(n)) {
                 continue;
             }
             ancestorQueue.add(n);
             while (!ancestorQueue.isEmpty()) {
                 Node ancestor = ancestorQueue.poll();
                 for (Relationship parent : ancestor
                         .getRelationships(CommitRelationshipTypes.PARENT)) {
                     Node parentNode = parent.getEndNode();
                     if (parentNode.getId() != ancestor.getId()) {
                         if (leftSet.contains(parentNode) || rightSet.contains(parentNode)) {
                             ancestorQueue.add(parentNode);
                             if (potentialCommonAncestors.contains(parentNode)) {
                                 falseAncestors.add(parentNode);
                             }
                         }
                     }
                 }
             }
         }
         potentialCommonAncestors.removeAll(falseAncestors);
     }
 }
