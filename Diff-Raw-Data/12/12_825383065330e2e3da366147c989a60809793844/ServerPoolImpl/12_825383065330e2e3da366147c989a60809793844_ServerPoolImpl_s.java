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
         List<ServerNode> servers = ServerNode.all();
         if ( !CollectionUtils.isEmpty( servers )){
             for (ServerNode server : servers) {
 				if ( server.isBusy() )
 				{
 
                      logger.info( "Found a busy server, setting destruction: {}", server );
 				     expiredServerCollector.scheduleToDestroy(server);
 
 				}
 				else
 				   logger.info( "Found a free bootstrapped server, add to a server pool: {}", server );
 			}
 		}// for
 
 		// create new servers if need
 		if ( CollectionUtils.size( servers )  < conf.server.pool.minNode )
 		{
 			int serversToInit = conf.server.pool.minNode - CollectionUtils.size( servers );
 			logger.info( "ServerPool starting to initialize {} servers...", serversToInit );
             addNewServerToPool( serversToInit );
 
 		}
 	}
 	
 	/** @return a ServerNode from the pool, otherwise <code>null</code> if no free server available */
     @Override
 	synchronized public ServerNode get( long lifeExpectancy )
 	{
 		ServerNode freeServer = ServerNode.getFreeServer();
 		if ( freeServer != null)
 		{
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
         logger.info("destroying server {}", serverId);
 		// when we move to Quarz just unregister from Cron
 		if ( ServerNode.getServerNode( serverId ) != null ){
 			addNewServerToPool();
         }
 
 		WidgetInstance.deleteByInstanceId(serverId);
 		ServerNode.deleteServer( serverId );
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
 					
 					for( ServerNode srv :  servers )
 						srv.save();
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
