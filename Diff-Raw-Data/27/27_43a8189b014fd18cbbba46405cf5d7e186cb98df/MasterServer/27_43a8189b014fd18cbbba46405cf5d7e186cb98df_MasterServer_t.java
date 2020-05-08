 /**
 * @Author Chris Card, Steven Rupert
 * CSCI 565 project 1
 * This file defines the Master server methods
 */
 
 package Server;
 
 import Domain.Article;
import Domain.NotFound404Exception;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.rmi.RemoteException;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class MasterServer extends Node implements Master
 {
     private static Logger log = LogManager.getLogger();
 
     private final ExecutorService executorService;
     private final List<Slave> nodes;
 
     /**
      * Constructs a new MasterServer instance.
      * @param databaseName local database file name to use.
      */
 	public MasterServer(String databaseName)
     {
         super(databaseName);
 
         nodes = Collections.synchronizedList(new ArrayList<Slave>());
         // caution, leaks incomplete "this" reference, but I think we should be fine here
         nodes.add(this);
 
         // Controls concurrent bord cast of replications to the quorum
         executorService = Executors.newCachedThreadPool(
                 new ThreadFactoryBuilder()
                         .setNameFormat("master-worker-%s")
                         .build());
     }
 
 	public int post(Article input) throws RemoteException
     {
         //creates a new article with a unique id
         final int id;
         try
         {
             id = articleStore.generateKey();
         } catch (Exception e)
         {
             log.error("Couldn't write key", e);
             throw new RuntimeException("Couldn't generate key " + e.getMessage());
         }
 
         final Article article = input.setId(id);
 
         runOnNodes(nodes.size() / 2 + 1, new Task()
         {
             @Override
             public void run(Slave node) throws Exception
             {
                 node.replicateWrite(article);
             }
         },"<POST>");
 
         log.info("Posted article {}", id);
 
         return id;
     }
 
 	public List<Article> getArticles() throws RemoteException
     {
         // strategy: collect all returned articles from the nodes into a set,
         // nodes will return all articles they know about, so we'll get the union
         // of all the known articles after all the nodes we wait for are done.
         // initialized to articles the master node (us) knows.
         final SortedSet<Article> articles =
                 Collections.synchronizedSortedSet(Sets.<Article>newTreeSet());
 
         runOnNodes((int) Math.ceil((nodes.size()) / 2.), new Task()
         {
             @Override
             public void run(Slave node) throws Exception
             {
                 articles.addAll(node.getLocalArticles());
             }
         },"<LIST>");
 
         log.info("Listed {} articles.", articles.size());
         return Lists.newArrayList(articles);
     }
 
 	public Article choose(final int id) throws RemoteException
     {
         // article will be the final returned article from the slaves
         // using an array so other threads can modify
         final Article[] article = new Article[1];
 
         runOnNodes((int) Math.ceil((nodes.size()) / 2.), new Task()
         {
             @Override
             public void run(Slave node) throws Exception
             {
                 article[0] = node.getLocalArticle(id);
             }
         },"<CHOOSE>");
 
         if (article[0] == null)
         {
             log.info("Article {} not found in cluster", id);
             throw new RemoteException("404 not found");
         }
         else
         {
             log.info("Retrieved article {} from cluster.", id);
             return article[0];
         }
     }
 
     public void registerSlaveNode(Slave slave, String identifier) throws RemoteException
     {
         nodes.add(slave);
         log.info("Slave {} registered. Cluster now contains {} nodes.", identifier, nodes.size());
     }
 
     private void runOnNodes(int minSuccesses, final Task task,String method) throws RemoteException
     {
         long start = System.nanoTime();
         final CountDownLatch latch = new CountDownLatch(minSuccesses);
         for (final Slave node : nodes)
         {
             executorService.submit(new Runnable()
             {
                 @Override
                 public void run()
                 {
                     try
                     {
                         task.run(node);
                         latch.countDown();
                    }
                    catch (NotFound404Exception e)
                    {

                    }
                    catch (Exception e)
                     {
                         log.error("cluster task failed!", e);
                     }
                 }
             });
         }
         try
         {
             boolean succeeded = latch.await(5, TimeUnit.SECONDS);
             if (succeeded)
             {
                 log.info("Method {} task took {} ms",method, (System.nanoTime() - start) / 1000000.);
             } else {
                 throw new RemoteException("cluster task timed out!");
             }
         } catch (InterruptedException ignored) { }
     }
 
     private static interface Task {
         void run(Slave node) throws Exception;
     }
 }
