 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import beans.config.Conf;
 
 import com.avaje.ebean.Ebean;
 import models.ServerNode;
 import org.apache.commons.collections.Predicate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.*;
 import utils.CollectionUtils;
 
 import javax.inject.Inject;
 import javax.persistence.OptimisticLockException;
 
 
 /**
  * This class manages a server pool of available/busy bootstrapped machines(servers).
  * A server pool initialize with configured minimum/maximum number of servers. 
  * The get() method returns a bootstrap server instance.
  * On init() a server-pool deletes an expires or orphans servers. 
  * It also provides ability to startup with a cold init which deletes a running server. See {@link server.Config} class.
  * 
  * @author Igor Goldenberg
  * @see ServerBootstrapperImpl
  */
 public class ServerPoolImpl implements ServerPool
 {
 
     private static Logger logger = LoggerFactory.getLogger( ServerPoolImpl.class );
     @Inject
     private ServerBootstrapper serverBootstrapper;
 
     @Inject
     private ExpiredServersCollector expiredServerCollector;
 
     @Inject
     private Conf conf;
 
 
     private static Predicate nonRemotePredicate = new Predicate() {
         @Override
         public boolean evaluate( Object o )
         {
             return !((ServerNode)o).isRemote();
         }
     };
     private static Predicate busyServerPredicate = new Predicate() {
         @Override
         public boolean evaluate(Object o) {
             return ((ServerNode)o).isBusy() && nonRemotePredicate.evaluate( o );
         }
     };
 
     private static Predicate nonBusyServerPredicate = new Predicate() {
         @Override
         public boolean evaluate(Object o) {
             return !((ServerNode)o).isBusy() && nonRemotePredicate.evaluate( o );
         }
     };
 
     private static Predicate failedBootstrapsPredicate = new Predicate() {
         @Override
         public boolean evaluate(Object o) {
             return ((ServerNode)o).getNodeId() == null;
         }
     };
 
     /**
      *
      * @param pool - the pool we need to clean
      * @return - a clean pool
      */
     private List<ServerNode> cleanPool( Collection<ServerNode> pool ){
 
         if ( CollectionUtils.isEmpty( pool )){
             return new LinkedList<ServerNode>(  );
         }
 
         List<ServerNode> cleanPool = new LinkedList<ServerNode>(  );
         for ( ServerNode serverNode : pool ) {
             BootstrapValidationResult bootstrapValidationResult = serverBootstrapper.validateBootstrap( serverNode );
             if ( !bootstrapValidationResult.getResult() ){
                 if ( bootstrapValidationResult.testCompleted() ){
                     logger.info( "found a bad bootstrap on server [{}] I should destroy this server..", serverNode );
                     destroy( serverNode.getNodeId() );
                 }else{
                    logger.error("unable to complete bootstrap test on [{}], nothing to do", bootstrapValidationResult );
                 }
             }else{
                 logger.info( "Found a working management server [{}], adding to clean pool", serverNode );
                 cleanPool.add( serverNode );
             }
         }
         return cleanPool;
     }
 
 	@Override
     public void init()
 	{
 		logger.info( "Started to initialize ServerPool, cold-init={}", conf.server.pool.coldInit );
 		// get all available running servers
         List<ServerNode> servers = ServerNode.all();
 
         Collection<ServerNode> busyServer = CollectionUtils.select( servers, busyServerPredicate );
         logger.info("I found {} busy servers", CollectionUtils.size(busyServer));
         if ( !CollectionUtils.isEmpty( busyServer )){
             for (ServerNode server : busyServer) {
 				     expiredServerCollector.scheduleToDestroy(server);
 			}
 		}// for
 
         Collection<ServerNode> availableServer = CollectionUtils.select( servers, nonBusyServerPredicate );
         availableServer = cleanPool( availableServer );
         logger.info(" I have {} available server, I need a minimum of {} and maximum of {}", new Object[]{ CollectionUtils.size(availableServer), conf.server.pool.minNode, conf.server.pool.maxNodes} );
 		// create new servers if need
 		if ( CollectionUtils.size( availableServer )  < conf.server.pool.minNode )
 		{
 			int serversToInit = conf.server.pool.minNode - CollectionUtils.size( availableServer );
             logger.info("creating {} new Servers", serversToInit);
             logger.info( "ServerPool starting to initialize {} servers...", serversToInit );
             addNewServerToPool( serversToInit );
             // remove servers if we have too much
 		} else if ( CollectionUtils.size(availableServer) > conf.server.pool.maxNodes ){
             int i =0;
             int serversToDelete = CollectionUtils.size(availableServer) - conf.server.pool.maxNodes ;
             logger.info("deleting {} servers",serversToDelete);
             for (ServerNode server : availableServer) {
                 if ( i >= serversToDelete){
                     break;
                 }
                 i++;
                 destroy( server.getNodeId() );
             }
         }
 
         // failed bootstraps.
         Collection<ServerNode> failedBootstraps =CollectionUtils.select( servers,  failedBootstrapsPredicate );
         if (!CollectionUtils.isEmpty(failedBootstraps)) {
             logger.info("deleting {} failed bootstraps : {}", CollectionUtils.size( failedBootstraps) ,failedBootstraps);
             Ebean.delete(failedBootstraps);
         }
     }
 
     public ServerNodesPoolStats getStats(){
         ServerNodesPoolStats stats = new ServerNodesPoolStats();
 
         List<ServerNode> all = ServerNode.all();
         stats.all = CollectionUtils.size( all );
         stats.nonRemote = CollectionUtils.size(  CollectionUtils.select( all, nonRemotePredicate ) );
         stats.busyServers = CollectionUtils.size( CollectionUtils.select( all, busyServerPredicate ) );
         stats.nonBusyServers = CollectionUtils.size( CollectionUtils.select( all, nonBusyServerPredicate ) );
         stats.minLimit = conf.server.pool.minNode;
         stats.maxLimit = conf.server.pool.maxNodes;
         return stats;
     }
 	
 	/** @return a ServerNode from the pool, otherwise <code>null</code> if no free server available */
     @Override
 	synchronized public ServerNode get( long lifeExpectancy )
 	{
         logger.info( "getting a server node with lifeExpectancy [{}]", lifeExpectancy );
 
         List<ServerNode> freeServers = null;
         freeServers = ServerNode.findByCriteria( new ServerNode.QueryConf().setMaxRows( 10 ).criteria().setBusy( false ).setRemote( false ).done() );
         ServerNode selectedServer = null;
         if ( !CollectionUtils.isEmpty( freeServers )){
             for ( ServerNode freeServer : freeServers ) {
                 if ( tryToGetFreeServer( freeServer, lifeExpectancy )){
                     logger.info( "successfully got a free server [{}]", freeServer );
                     selectedServer = freeServer;
                 }
             }
         }else{
             logger.info( "freeServers is empty, adding a new server. pool status is [{}]", getStats() );
         }
 
 		addNewServerToPool();
 
 		return selectedServer;
 	}
 
     /**
      * <p>
      * This method checks the serverNode is accessible.<br/>
      * If the serverNode is accessible it will try to mark it as "busy" which will effectively
      * get it out of the pool.<br/>
      * </p>
      *
      * @param serverNode - the server node we are trying to get
      * @return - true iff successfully got the serverNode
      */
     private boolean tryToGetFreeServer( ServerNode serverNode, long lifeExpectancy )
     {
         try{
             BootstrapValidationResult result = serverBootstrapper.validateBootstrap( serverNode );
             if ( result.getResult() ) {
                 serverNode.setBusy( true );
                 serverNode.setExpirationTime( lifeExpectancy + System.currentTimeMillis() );
                 serverNode.save(); // optimistic locking
                 // schedule to destroy after time expiration
                 // TODO when unlimited server will support uncomment this line if ( serverNode.isTimeLimited() )
                 expiredServerCollector.scheduleToDestroy( serverNode );
                 return true;
             } else {
                 logger.info( "serverNode[{}] has an invalid bootstrap", result );
             }
         }catch(OptimisticLockException e){
             logger.info( "server [{}] already caught by another thread, could not get it", serverNode );
         }
         return false;
     }
 
     public void destroy(String serverId)
 	{
         if ( serverId == null ){
             return; // nothing to do.
         }
         logger.info("destroying server {}", serverId);
 
         // guy - removing "addNewServerToPool" - this is the destroy function, not create function.
         // guy - removing WidgetInstance.delete since we cascade removal
         // guy - removing ServerNode.delete, since we established it does not exist.
         serverBootstrapper.destroyServer( serverId );
 	}
 
     private void addNewServerToPool( int number ){
         for (int i = 0; i < number; i++) {
             addNewServerToPool();
         }
     }
 
     private boolean isPoolSaturated(){
         return ServerNode.count() >= conf.server.pool.maxNodes;
     }
 	
 	void addNewServerToPool()
 	{
         logger.info( "adding new server to the pool" );
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					List<ServerNode> servers = serverBootstrapper.createServers( 1 );
 					for( ServerNode srv :  servers ){
 						srv.save();
                     }
 				} catch (Exception e)
 				{
 					logger.error("ServerPool failed to create a new server node", e);
 				}
 			}
 		}).start();
 	}
 
     public void setServerBootstrapper(server.ServerBootstrapper serverBootstrapper) {
         this.serverBootstrapper = serverBootstrapper;
     }
 
     public void setExpiredServerCollector(ExpiredServersCollector expiredServerCollector) {
         this.expiredServerCollector = expiredServerCollector;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 }
