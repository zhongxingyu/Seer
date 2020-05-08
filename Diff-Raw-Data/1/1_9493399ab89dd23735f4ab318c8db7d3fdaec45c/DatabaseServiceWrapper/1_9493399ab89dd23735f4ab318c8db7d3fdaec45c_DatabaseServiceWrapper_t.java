 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.db.manager;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.amanzi.neo.db.manager.DatabaseManager.DatabaseAccessType;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.event.KernelEventHandler;
 import org.neo4j.graphdb.event.TransactionEventHandler;
 import org.neo4j.graphdb.index.IndexManager;
 
 /**
  * <p>
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class DatabaseServiceWrapper implements INeoDbService {
     private GraphDatabaseService realService;
     private final ReadWriteLock rwl = new ReentrantReadWriteLock();
     private final Lock r = rwl.readLock();
     private final Lock w = rwl.writeLock();
     private boolean canDelete = true;
     // TODO synchronize?
     private Set<Long> nodesTodelete = new HashSet<Long>();
     private Set<Long> relationshipTodelete = new HashSet<Long>();
 
     DatabaseServiceWrapper(GraphDatabaseService realService) {
         super();
         this.realService = realService;
     }
 
     void setRealService(DatabaseAccessType type, GraphDatabaseService realService) {
         w.lock();
         try {
             canDelete = type == DatabaseAccessType.EMBEDDED;
             this.realService = realService;
             if (canDelete) {
                 if (nodesTodelete.size() + relationshipTodelete.size() > 0) {
                     Transaction tx = realService.beginTx();
                     try {
                         for (long id : relationshipTodelete) {
                             try {
                                 realService.getRelationshipById(id).delete();
                             } catch (Exception e) {
                                 // TODO add handling
                                 e.printStackTrace();
                             }
                         }
                         relationshipTodelete.clear();
                         for (long id : nodesTodelete) {
                             try {
                                 realService.getNodeById(id).delete();
                             } catch (Exception e) {
                                 // TODO add handling
                                 e.printStackTrace();
                             }
                         }
                         nodesTodelete.clear();
                         tx.success();
                     } finally {
                         tx.finish();
                     }
                 }
             }
 
         } finally {
             w.unlock();
         }
     }
 
     @Override
     public Node createNode() {
         r.lock();
         try {
             return realService.createNode();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public Node getNodeById(long paramLong) {
         r.lock();
         try {
             return realService.getNodeById(paramLong);
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public Relationship getRelationshipById(long paramLong) {
         r.lock();
         try {
             return realService.getRelationshipById(paramLong);
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public Node getReferenceNode() {
         r.lock();
         try {
             return realService.getReferenceNode();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public Iterable<Node> getAllNodes() {
         r.lock();
         try {
             return realService.getAllNodes();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public Iterable<RelationshipType> getRelationshipTypes() {
         r.lock();
         try {
             return realService.getRelationshipTypes();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public void shutdown() {
         r.lock();
         try {
             realService.shutdown();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public boolean enableRemoteShell() {
         r.lock();
         try {
             return realService.enableRemoteShell();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public boolean enableRemoteShell(Map<String, Serializable> paramMap) {
         r.lock();
         try {
             return realService.enableRemoteShell(paramMap);
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public Transaction beginTx() {
         r.lock();
         try {
             return realService.beginTx();
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> paramTransactionEventHandler) {
         r.lock();
         try {
             return realService.registerTransactionEventHandler(paramTransactionEventHandler);
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> paramTransactionEventHandler) {
         r.lock();
         try {
             return realService.unregisterTransactionEventHandler(paramTransactionEventHandler);
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public KernelEventHandler registerKernelEventHandler(KernelEventHandler paramKernelEventHandler) {
         r.lock();
         try {
             return realService.registerKernelEventHandler(paramKernelEventHandler);
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler paramKernelEventHandler) {
         r.lock();
         try {
             return realService.unregisterKernelEventHandler(paramKernelEventHandler);
         } finally {
             r.unlock();
         }
     }
 
     void lockWrite() {
         w.lock();
     }
 
     void writeUnlock() {
         w.unlock();
     }
 
     @Override
     public void delete(Relationship relation) {
         r.lock();
         try {
             if (canDelete) {
                 relation.delete();
             } else {
                 relationshipTodelete.add(relation.getId());
             }
         } finally {
             r.unlock();
         }
     }
 
     @Override
     public void delete(Node node) {
         r.lock();
         try {
             if (canDelete) {
                 node.delete();
             } else {
                 nodesTodelete.add(node.getId());
             }
         } finally {
             r.unlock();
         }
     }
 
     public IndexManager index() {
         r.lock();
         try {
             return realService.index();
         } finally {
             r.unlock();
         }
     }
 
 }
