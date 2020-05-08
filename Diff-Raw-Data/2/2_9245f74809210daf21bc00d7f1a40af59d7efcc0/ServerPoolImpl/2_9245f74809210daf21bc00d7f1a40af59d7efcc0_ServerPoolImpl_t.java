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
 
 import java.util.List;
 
 import beans.config.Conf;
 
 import com.avaje.ebean.Ebean;
 import models.ServerNode;
 import models.WidgetInstance;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.*;
 import utils.CollectionUtils;
 
 import javax.inject.Inject;
 
 
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
 
 
 	@Override
     public void init()
 	{
 		logger.info( "Started to initialize ServerPool, cold-init={}", conf.server.pool.coldInit );
 		// get all available running servers
         List<ServerNode> servers = ServerNode.findByCriteria(new ServerNode.QueryConf().criteria().setRemote(false).done());
         if ( !CollectionUtils.isEmpty( servers )){
             for (ServerNode server : servers) {
 				if ( server.isBusy() )
 				{
                      logger.info( "Found a busy server, setting destruction: {}", server );
 				     expiredServerCollector.scheduleToDestroy(server);
 				}
 			}
 		}// for
 
 		// create new servers if need
 		if ( CollectionUtils.size( servers )  < conf.server.pool.minNode )
 		{
 			int serversToInit = conf.server.pool.minNode - CollectionUtils.size( servers );
 			logger.info( "ServerPool starting to initialize {} servers...", serversToInit );
             addNewServerToPool( serversToInit );
             // remove servers if we have too much
 		} else if ( CollectionUtils.size(servers) > conf.server.pool.maxNodes ){
             int i =0;
             for (ServerNode server : servers) {
                 if ( i >= conf.server.pool.maxNodes - CollectionUtils.size(servers) ){
                     break;
                 }
                 destroy( server.getNodeId() );
             }
         }
 
         // failed bootstraps.
         List<ServerNode> failedBootstraps = ServerNode.findByCriteria(new ServerNode.QueryConf().criteria().setServerIdIsNull(true).done());
        if (!CollectionUtils.isEmpty(failedBootstraps)) {
             logger.info("deleting failed bootstraps : " + failedBootstraps);
             Ebean.delete(failedBootstraps);
         }
     }
 	
 	/** @return a ServerNode from the pool, otherwise <code>null</code> if no free server available */
     @Override
 	synchronized public ServerNode get( long lifeExpectancy )
 	{
 		ServerNode freeServer = CollectionUtils.first(ServerNode.findByCriteria(new ServerNode.QueryConf().setMaxRows(1).criteria().setBusy(false).setRemote(false).done()));
 		if ( freeServer != null)
 		{    // guy : todo : need to lock this somehow
 			freeServer.setBusy(true);
 			freeServer.setExpirationTime( lifeExpectancy + System.currentTimeMillis() );
 			// schedule to destroy after time expiration 
 			// TODO when unlimited server will support uncomment this line if ( freeServer.isTimeLimited() )
 			expiredServerCollector.scheduleToDestroy(freeServer);
 		}
 
 		addNewServerToPool();
 
 		return freeServer;
 	}
 	
 	public void destroy(String serverId)
 	{
         if ( serverId == null ){
             return; // nothing to do.
         }
         logger.info("destroying server {}", serverId);
 		// when we move to Quarz just unregister from Cron
 		if ( ServerNode.getServerNode( serverId ) != null ){
 			addNewServerToPool();
         }
 
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
