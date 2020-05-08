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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeoutException;
 
 import javax.inject.Inject;
 
 import models.ServerNode;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.ExecResponse;
 import org.jclouds.domain.LoginCredentials;
 import org.jclouds.logging.config.NullLoggingModule;
 import org.jclouds.openstack.nova.v2_0.NovaApi;
 import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
 import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
 import org.jclouds.openstack.nova.v2_0.features.ServerApi;
 import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
 import org.jclouds.rest.RestContext;
 import org.jclouds.ssh.SshClient;
 import org.jclouds.sshj.config.SshjSshClientModule;
 import org.jclouds.util.Strings2;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.cache.Cache;
 import server.ApplicationContext;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.ServerBootstrapper;
 import server.exceptions.ServerException;
 import utils.CloudifyUtils;
 import utils.Utils;
 import beans.api.ExecutorFactory;
 import beans.config.Conf;
 
 import com.google.common.net.HostAndPort;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 
 
 /**
  * This class manages a compute cloud provider by JCloud openstack nova infrastructure.
  * It provides ability to create/delete specific server with desired flavor configuration.
  * On each new server runs a bootstrap script that prepare machine for a server-pool, 
  * it includes a setup of firewall, JDK, cloudify installation and etc...
  * The bootstrap script can be found under ssh/bootstrap_machine.sh
  * 
  * @author Igor Goldenberg
  */
 public class ServerBootstrapperImpl implements ServerBootstrapper
 {
 
     private static Logger logger = LoggerFactory.getLogger( ServerBootstrapperImpl.class );
 	private ComputeService _compute;
 	private RestContext<NovaApi, NovaAsyncApi> _nova;
 
     @Inject
     private Conf conf;
 
     @Inject
     private ExecutorFactory executorFactory;
     
     @Inject
     private DeployManager deployManager;
 	
     public List<ServerNode> createServers( int numOfServers )
 	{
 		List<ServerNode> servers = new ArrayList<ServerNode>();
 		
 		for( int i=0; i< numOfServers; i++ )
 		{
 			ServerNode srvNode = null;
 	    	try {
 				srvNode =  createServerNode();
 				servers.add( srvNode );
 				
 				return servers;
 			} catch (Exception e) 
 			{
 				// failed to boostrap machine, nothing todo - let destroy :(
 				if ( srvNode != null ) {
 					destroyServer(srvNode.getNodeId());
                 }
 					
 				logger.error("Failed to bootstrap machine. ", e);
 			}
 		}
 		
 		return servers;
 	}
 	
     
 	public void destroyServer( String serverId )
 	{
         logger.info("destroying server {}", serverId );
 	   deleteServer(serverId);
 	}
 
     public void init(){
         String cloudProvider = conf.server.bootstrap.cloudProvider;
         String username = conf.server.bootstrap.username;
         String apiKey = conf.server.bootstrap.apiKey;
         logger.info( "initializing bootstrapper with [cloudProvider, username, apiKey]=[{},{},{}]", new Object[]{cloudProvider, username, apiKey} );
         ContextBuilder contextBuilder = ContextBuilder.newBuilder( cloudProvider );
         contextBuilder.credentials( username, apiKey );
         ComputeServiceContext context = contextBuilder.buildView( ComputeServiceContext.class );
         _compute = context.getComputeService();
         _nova = context.unwrap();
     }
 
 	
 	private ServerNode createServerNode() throws RunNodesException, TimeoutException
 	{
 		logger.info( "Starting to create new Server [imageId={}, flavorId={}]", conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId );
 		
 		ServerApi serverApi = _nova.getApi().getServerApiForZone(conf.server.bootstrap.zoneName);
 		CreateServerOptions serverOpts = new CreateServerOptions();
 		serverOpts.keyPairName( conf.server.bootstrap.keyPair );
 		serverOpts.securityGroupNames( conf.server.bootstrap.securityGroup );
 		
 		ServerCreated serverCreated = serverApi.create( conf.server.bootstrap.serverNamePrefix + System.currentTimeMillis(), conf.server.bootstrap.imageId , conf.server.bootstrap.flavorId, serverOpts);
 		blockUntilServerInState(serverCreated.getId(), Server.Status.ACTIVE, 1000, 5, serverApi);
 		Server server = serverApi.get(serverCreated.getId());
 
 		ServerNode serverNode = new ServerNode( server );
 		
 		logger.info("Server created, wait 10 seconds before starting to bootstrap machine: {}" ,  serverNode.getPublicIP() );
 		Utils.threadSleep(10000); // need for a network interfaces initialization
 		
 		// bootstrap machine: firewall, jvm, start cloudify
 		bootstrapMachine( serverNode );
 		
 		logger.info("Server created.{} " , server.getAddresses() );
 		
 		return serverNode;
 	}
 	
 
 	@Override
 	public ServerNode bootstrapCloud( ServerNode serverNode )  {
 		File cloudFolder = null;
 		try{
 			Cache.set( "output-" + serverNode.getNodeId(),  new StringBuilder());
 
 			logger.info("Creating cloud folder with specific user credentials. User: " + serverNode.getUserName() + ", api key: " + serverNode.getApiKey());
 			cloudFolder = CloudifyUtils.createCloudFolder( serverNode.getUserName(), serverNode.getApiKey() );
 
 			//Command line for bootstrapping remote cloud.
 			CommandLine cmdLine = new CommandLine(conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile() + Utils.getExecutableExt());
 			cmdLine.addArgument(cloudFolder.getName());
 
 			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
 			ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor(serverNode.getNodeId());
 			
 			logger.info("Executing command line: " + cmdLine);
 			bootstrapExecutor.execute(cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment() , resultHandler);
 			resultHandler.waitFor();
 
			String output = Utils.getCachedOutput(serverNode.getId());
 			if (resultHandler.getException() != null) {
 				if (output.contains("found existing management machines")) {
 					throw new RuntimeException("Found existing management machines. Process output was: " + output);
 				}
 				logger.info("Command execution ended with errors: " + output.toString());
 				throw new RuntimeException("Failed to bootstrap cloudify machine: " 
 						+ output, resultHandler.getException());
 			}
 
 			String publicIp = Utils.extractIpFromBootstrapOutput(output);
 			if (StringUtils.isEmpty(publicIp)) {
 				logger.warn("No public ip address found in bootstrap output. " + output);
 				throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output." 
 						+ output, resultHandler.getException() );
 			}
 			serverNode.setPublicIP(publicIp);
 			String privateKey = CloudifyUtils.getCloudPrivateKey(cloudFolder);
 			if (StringUtils.isEmpty(privateKey)) {
 				throw new RuntimeException( "Bootstrap failed. No pem file found in cloud directory." );
 			}
 			logger.info("Bootstrap cloud command ended successfully");
 
 			serverNode.setPrivateKey(privateKey);
 			serverNode.setRemote(true);
 
 			return serverNode;
 		} catch(Exception e) {
 			throw new RuntimeException("Unable to bootstrap cloud", e);
 		} finally {
 			if (cloudFolder != null)
 				FileUtils.deleteQuietly(cloudFolder);
 			serverNode.setStopped(true);
 		}
 	}
 	
 	private void deleteServer( String serverId )
 	{
 		ServerApi serverApi = _nova.getApi().getServerApiForZone( conf.server.bootstrap.zoneName );
 		serverApi.delete(serverId);
 		logger.info("Server id: {} was deleted.", serverId);
 	}
 	
 	
 	/**
 	 * Will block until the server is in the correct state.
 	 * 
 	 * @param serverId The id of the server to block on
 	 * @param status The status the server needs to reach before the method stops blocking
 	 * @param timeoutSeconds The maximum amount of time to block before throwing a TimeoutException
 	 * @param delaySeconds The amount of time between server status checks
 	 * @param serverApi The ServerApi used to do the checking
 	 * 
 	 * @throws TimeoutException If the server does not reach the status by timeoutSeconds
 	 */
 	private void blockUntilServerInState(String serverId, Status status,
 			int timeoutSeconds, int delaySeconds, ServerApi serverApi)
 			throws TimeoutException
 	{
 		int totalSeconds = 0;
 
 		while (totalSeconds < timeoutSeconds)
 		{
 			logger.info("Waiting for a server activation... Left timeout: {} sec", timeoutSeconds - totalSeconds);
 
 			Server server = serverApi.get(serverId);
 
 			if (server.getStatus().equals(status))
 				return;
 
 			Utils.threadSleep(delaySeconds * 1000);
 
 			totalSeconds += delaySeconds;
 		}
 
 		String message = String.format("Timed out at %d seconds waiting for server %s to reach status %s.",
 						timeoutSeconds, serverId, status);
 
 		throw new TimeoutException(message);
 	}
 	
 	private void bootstrapMachine( ServerNode server )
 	{
 		try
 		{
 			logger.info("Starting bootstrapping for server:{} " , server.getPublicIP() );
 
 			String script = FileUtils.readFileToString( conf.server.bootstrap.script );
 			ExecResponse response = runScriptOnNode( conf, server.getPublicIP(), script );
 			
 			logger.info("Bootstrap for server: {} finished successfully successfully. " +
                     "ExitStatus: {} \nOutput:  {}", new Object[]{server.getPublicIP(),
                     response.getExitStatus(),
                     response.getOutput()} );
 		}catch(Exception ex)
 		{
 			throw new ServerException("Failed to bootstrap cloudify machine: " + server.getPublicIP(), ex);
 		}
 	}
 	
 	static public ExecResponse runScriptOnNode( Conf conf, String serverIP, String script)
 			throws NumberFormatException, IOException
 	{
 		logger.info("Run ssh on server: {} script: {}" , serverIP, script );
         Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
 		SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
 		SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIP, conf.server.bootstrap.ssh.port ),
 				LoginCredentials.builder().user( conf.server.bootstrap.ssh.user )
 						.privateKey(Strings2.toStringAndClose(new FileInputStream( conf.server.bootstrap.ssh.privateKey ))).build());
         ExecResponse execResponse = null;
 		try
 		{
 			sshConnection.connect();
 			execResponse = sshConnection.exec(script);
 		 }finally 
 		 {
 			if (sshConnection != null)
 			   sshConnection.disconnect();
 		 }
 
 		return execResponse;
 	}
 	
 	/**
 	 * Always close your service when you're done with it.
 	 */
 	public void close()
 	{
          if (_compute != null)
 		{
 			_compute.getContext().close();
 		}
 	}
 
     public void setDeployManager(DeployManager deployManager) {
         this.deployManager = deployManager;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 }
