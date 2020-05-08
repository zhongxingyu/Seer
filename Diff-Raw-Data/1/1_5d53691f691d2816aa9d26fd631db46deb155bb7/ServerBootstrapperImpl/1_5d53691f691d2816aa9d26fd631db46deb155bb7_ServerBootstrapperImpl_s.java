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
 
 import beans.api.ExecutorFactory;
 import beans.cloudify.CloudifyRestClient;
 import beans.cloudify.CloudifyRestResult;
 import beans.config.Conf;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Multimap;
 import com.google.common.net.HostAndPort;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import models.ServerNode;
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.ExecResponse;
 import org.jclouds.domain.LoginCredentials;
 import org.jclouds.logging.config.NullLoggingModule;
 import org.jclouds.openstack.nova.v2_0.NovaApi;
 import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
 import org.jclouds.openstack.nova.v2_0.domain.Address;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
 import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
 import org.jclouds.openstack.nova.v2_0.features.ServerApi;
 import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
 import org.jclouds.rest.AuthorizationException;
 import org.jclouds.rest.RestContext;
 import org.jclouds.ssh.SshClient;
 import org.jclouds.sshj.config.SshjSshClientModule;
 import org.jclouds.util.Strings2;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.i18n.Messages;
 import server.ApplicationContext;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.ServerBootstrapper;
 import server.exceptions.ServerException;
 import utils.CloudifyUtils;
 import utils.CollectionUtils;
 import utils.Utils;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicLong;
 
 
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
 
 	private NovaContext novaContext;
 
     private int retries = 2;
     private int bootstrapRetries = 2;
 
     @Inject
     private Conf conf;
 
     @Inject
     private ExecutorFactory executorFactory;
 
     // this is an incrementing ID starting from currentMilliTime..
     // resolves issue where we got "Server by this name already exists"
     private AtomicLong incNodeId = new AtomicLong(  System.currentTimeMillis() );
 
     @Inject
     private DeployManager deployManager;
 
     @Inject
     private CloudifyRestClient cloudifyRestClient;
 
     private Exception lastKnownException = null;
 
     public List<ServerNode> createServers( int numOfServers )
 	{
 		List<ServerNode> servers = new ArrayList<ServerNode>();
 		logger.info("creating {} new instances", numOfServers );
 		for( int i=0; i< numOfServers; i++ )
 		{
             ServerNode server = createServer();
             servers.add( server );
         }
 		return servers;
 	}
 
     public ServerNode createServer()
     {
 
         ServerNode serverNode = null;
         int i =0;
         for ( ; i < retries && serverNode == null ; i++){
             logger.info( "creating new server node, try #[{}]",i );
             serverNode = tryCreateServer();
         }
 
         if ( serverNode == null ){
             throw new RuntimeException( "unable to create new nodes!! printed last known exception here", lastKnownException );
         }
         return serverNode;
 
     }
 
     public ServerNode tryCreateServer(){
         ServerNode srvNode = null;
         try {
             srvNode = createServerNode();
             return srvNode;
         } catch ( Exception e ) {
             lastKnownException = e;
             // failed to boostrap machine, nothing to do - let destroy :(
             if ( srvNode != null ) {
                 destroyServer( srvNode );
                 logger.error( "Failed to create machine.", e );
             }
 
         }
         return null;
     }
 
     // guy - todo - need to ping machine first. If machine is down, we cannot validate - throw exception.
     // guy - todo - if machine is up we do the same thing we do today.
     @Override
     public BootstrapValidationResult validateBootstrap( ServerNode serverNode )
     {
         BootstrapValidationResult result = new BootstrapValidationResult();
         try{
             result.machineReachable = isMachineReachable( serverNode );
         }catch(Exception e){
             result.machineReachableException = e;
         }
 
         if ( result.machineReachable == Boolean.TRUE ){
             try{
                 result.managementVersion = cloudifyRestClient.getVersion( serverNode.getPublicIP() ).getVersion();
                 result.managementAvailable = true;
             }catch( Exception e ){
                 logger.debug( "got exception while checking management version",e );
                 result.managementAvailable = false;
             }
         }
         return result;
     }
 
     private boolean isMachineReachable( ServerNode serverNode ) throws Exception
     {
 //        logger.info( "pinging machine [{}]", serverNode );
 //        String publicIP = serverNode.getPublicIP();
 //        InetAddress byName = InetAddress.getByName( publicIP );
 //
 //        boolean reachable = byName.isReachable( 5000 );
 //        logger.info( "machine is reachable [{}]", reachable );
 //        return reachable;
         return true; // guy - there's a problem pinging machines.
     }
 
 
 	public void destroyServer( ServerNode serverNode)
 	{
        logger.info("destroying server {}", serverNode );
         if ( !StringUtils.isEmpty( serverNode.getNodeId() ) ){
             deleteServer( serverNode.getNodeId() );
         }
        serverNode.delete(  );
 	}
 
     /**
      * We encourage using conf.server.bootstrap.tags
      * This will tag all created machines.
      *
      * For example - use tag "managed-by-my-cloudify-widget-instance"
      *
      * Use a different confTags for each server instance you have - so you can tell which machine is in which pool.
        This function in turn should get all machines in the pool.
      * @return - all machines that contain all these tags.
      */
     public List<Server> getAllMachinesWithTag( NovaContext context){
         String confTags =  conf.server.bootstrap.tags;
         logger.info( "getting all machines with tag [{}]", confTags );
         List<Server> servers = new LinkedList<Server>();
         if ( StringUtils.isEmpty( confTags ) ){
             logger.info( "confTags is null, not finding all machines" );
             return servers;
         }
 
         else{ // get all servers with tags matching my configuration.
             return getAllMachinesWithPredicate( new ServerTagPredicate(), context );
         }
     }
 
     public List<Server> getAllMachinesWithPredicate( Predicate<Server> predicate, NovaContext context ){
         logger.info( "getting all machine by predicate [{}]", predicate );
         return ( List<Server> ) context.getApi().listInDetail().concat()
                             .filter( predicate )
                             .toImmutableList();
     }
 
 
     class ServerNamePrefixPredicate implements Predicate<Server>{
         String prefix = conf.server.cloudBootstrap.existingManagementMachinePrefix;
 
 
         @Override
         public boolean apply( Server server )
         {
             // return true iff server is not null, prefix is not empty and prefix is prefix of server.getName
             return server != null && !StringUtils.isEmpty( prefix ) && server.getName().indexOf( prefix ) == 0;
 
         }
 
         public String toString(){
              return String.format("name has prefix [%s]", prefix);
         }
     }
 
 
     class ServerTagPredicate implements Predicate<Server> {
 
         String confTags =  conf.server.bootstrap.tags;
         List<String> confTagsList = null;
 
         public ServerTagPredicate(){
             if ( !StringUtils.isEmpty( confTags )){
                 confTagsList = Arrays.asList( StringUtils.stripAll( confTags.split( "," ) ) );
             }
         }
 
         @Override
         public boolean apply( Server server )
         {
             if ( server == null ) {
                 return false;
             }
 
             Map<String, String> metadata = server.getMetadata();
             if ( !CollectionUtils.isEmpty( metadata ) && metadata.containsKey( "tags" ) ) {
                 String tags = metadata.get( "tags" );
                 if ( !StringUtils.isEmpty( tags ) && !CollectionUtils.isEmpty( confTagsList ) ) {
                     logger.info( "comparing tags [{}] with confTags [{}]", tags, confTags );
                     List<String> tagList = Arrays.asList( StringUtils.stripAll( tags.split( "," ) ) );
                     return CollectionUtils.isSubCollection( confTagsList, tagList );
                 }
             }
             return false;
         }
 
         @Override
         public String toString()
         {
             return String.format("has tags [%s]",confTags);
         }
     }
 
     @Override
     public List<ServerNode> recoverUnmonitoredMachines(){
         List<ServerNode> result = new ArrayList<ServerNode>(  );
         logger.info( "recovering all list machines" );
         List<Server> allMachinesWithTag = getAllMachinesWithTag( novaContext );
         logger.info( "found [{}] total machines with matching tags filtering lost", CollectionUtils.size( allMachinesWithTag )  );
         if ( !CollectionUtils.isEmpty( allMachinesWithTag )){
             for ( Server server : allMachinesWithTag ) {
                 ServerNode serverNode = ServerNode.getServerNode( server.getId() );
                 if ( serverNode == null ){
                     ServerNode newServerNode = new ServerNode( server );
                     logger.info( "found an unmonitored machine - I should add it to the DB [{}]", newServerNode );
                     result.add( newServerNode );
                 }
             }
         }
         return result;
     }
 
     public void init(){
         novaContext = new NovaContext(conf.server.bootstrap.cloudProvider,conf.server.bootstrap.api.project, conf.server.bootstrap.api.key, conf.server.bootstrap.api.secretKey, conf.server.bootstrap.zoneName, false  );
     }
 
     public static class NovaContext{
         ComputeServiceContext context;
         String zone;
 
         ServerApi api = null;
         RestContext<NovaApi, NovaAsyncApi> nova = null;
         ComputeService computeService = null;
 
         public NovaContext( String cloudProvider, String project, String key, String secretKey, String zone, boolean apiCredentials )
         {
             logger.info( "initializing bootstrapper with [cloudProvider, project, key, secretKey]=[{},{},{}]", new Object[]{cloudProvider, project, key, secretKey} );
             Properties overrides = new Properties();
             if ( apiCredentials ){
                 overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
             }
             context = ContextBuilder.newBuilder( cloudProvider ).credentials( project+":"+key, secretKey ).overrides( overrides ).buildView( ComputeServiceContext.class );
             this.zone = zone;
         }
 
         private RestContext<NovaApi, NovaAsyncApi> getNova(){
             if ( nova == null ){
                 nova  = context.unwrap(  );
             }
             return nova;
         }
 
         public ServerApi getApi(  ){
             if ( api == null ){
                 api = getNova().getApi().getServerApiForZone( zone );
             }
             return api;
         }
 
         public ComputeService getCompute(){
             if ( computeService == null ){
                 computeService = context.getComputeService();
             }
             return computeService;
         }
 
         public void close()
         {
             context.close();
         }
     }
 
 
 	private ServerNode createServerNode() throws RunNodesException, TimeoutException
 	{
 		logger.info( "Starting to create new Server [imageId={}, flavorId={}]", conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId );
 
 		ServerApi serverApi = novaContext.getApi();
 		CreateServerOptions serverOpts = new CreateServerOptions();
 
         Map<String,String> metadata = new HashMap<String, String>();
 
         List<String> tags = new LinkedList<String>();
         String hostname = null;
         try {
             java.net.InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException e) {
             logger.debug("unable to get hostname",e);
         }
 
         if ( hostname != null ){
             tags.add( hostname );
         }
 
         if ( !StringUtils.isEmpty(conf.server.bootstrap.tags) ){
             tags.add( conf.server.bootstrap.tags );
         }
         metadata.put("tags", StringUtils.join(tags, ","));
         serverOpts.metadata(metadata);
 		serverOpts.keyPairName( conf.server.bootstrap.keyPair );
 		serverOpts.securityGroupNames(conf.server.bootstrap.securityGroup);
 
 		ServerCreated serverCreated = serverApi.create( conf.server.bootstrap.serverNamePrefix + incNodeId.incrementAndGet(), conf.server.bootstrap.imageId , conf.server.bootstrap.flavorId, serverOpts);
 		blockUntilServerInState(serverCreated.getId(), Server.Status.ACTIVE, 1000, 5, serverApi);
 		Server server = serverApi.get(serverCreated.getId());
 
 		ServerNode serverNode = new ServerNode( server );
 
 		logger.info("Server created, wait 10 seconds before starting to bootstrap machine: {}" ,  serverNode.getPublicIP() );
 		Utils.threadSleep(10000); // need for a network interfaces initialization
 
 
         boolean bootstrapSuccess = false;
         Exception lastBootstrapException = null;
         for ( int i = 0; i < bootstrapRetries && !bootstrapSuccess ; i ++ ){
 		    // bootstrap machine: firewall, jvm, start cloudify
             logger.info( "bootstrapping machine try #[{}]", i );
 		    try{
                 bootstrapMachine( serverNode );
                 BootstrapValidationResult bootstrapValidationResult = validateBootstrap( serverNode );
                 if ( bootstrapValidationResult.isValid() ) {
                     bootstrapSuccess = true;
                 }else{
                     logger.info( "machine [{}] did not bootstrap successfully [{}] retrying", serverNode, bootstrapValidationResult );
                     logger.info( "rebuilding machine" );
                     try{
                         serverApi.rebuild( serverNode.getNodeId() );
                     }catch(RuntimeException e){
                         logger.error( "error while rebuilding machine [{}]", serverNode ,e );
                     }
                 }
             }catch(RuntimeException e){
                  lastBootstrapException = e;
             }
         }
 
         if ( !bootstrapSuccess ){
             logger.error( "unable to bootstrap machine", lastBootstrapException );
         }
 
 		logger.info("Server created.{} " , server.getAddresses() );
 
 		return serverNode;
 	}
 
 
     @Override
     public ServerNode bootstrapCloud( ServerNode serverNode )
     {
         serverNode.setRemote( true );
         // get existing management machine
 
         List<Server> existingManagementMachines = null;
         try{
             existingManagementMachines = getAllMachinesWithPredicate( new ServerNamePrefixPredicate(), new NovaContext( conf.server.cloudBootstrap.cloudProvider,serverNode.getProject(), serverNode.getKey(), serverNode.getSecretKey(),conf.server.cloudBootstrap.zoneName, true ) );
         }catch(Exception e){
             if ( ExceptionUtils.indexOfThrowable( e, AuthorizationException.class ) > 0 ){
                 serverNode.errorEvent( "Invalid Credentials" ).save(  );
                 return null;
             }
             logger.error( "unrecognized exception, assuming only existing algorithm failed. ",e );
         }
 
         logger.info( "found [{}] management machines", CollectionUtils.size( existingManagementMachines ) );
 
         if ( !CollectionUtils.isEmpty( existingManagementMachines ) ) {
 
             Server managementMachine = CollectionUtils.first( existingManagementMachines );
 
             // GUY - for some reason
 
             // ((Address )managementMachine.getAddresses().get("private").toArray()[1]).getAddr()
 
             String serverNodeIp = managementMachine.getAccessIPv4();
             if ( serverNodeIp == null ) {
                 Multimap<String, Address> addresses = managementMachine.getAddresses();
                 Collection<Map.Entry<String, Address>> entries = addresses.entries();
                 logger.info( "found machines, searching all IPs" );
                 for ( Map.Entry<String, Address> entry : entries ) {
                     logger.info( "looking at [{}]", entry );
                     Address address = entry.getValue();
                     address.getAddr();
                     try{
                     CloudifyRestResult.TestRest testRest = cloudifyRestClient.testRest( address.getAddr() );
                     if ( testRest.isSuccess() ) {
                         serverNodeIp = address.getAddr();
                         break;
                     }
                     }catch(Exception e){logger.info( "unable to reach management on [{}]. moving on ", address.getAddr() );}
                 }
             }
 
 
             if ( serverNodeIp  == null ){
                 serverNode.errorEvent( "Management machine exists but unreachable" ).save(  );
                 logger.info( "unable to reach management machine. stopping progress." );
                 return null;
             }
             logger.info( "using first machine  [{}] with ip [{}]", managementMachine, serverNodeIp );
             serverNode.infoEvent("Found management machine on :" + serverNodeIp ).save(  );
             serverNode.setPublicIP( serverNodeIp );
             serverNode.save(  );
             logger.info( "not searching for key - only needed for bootstrap" );
         } else {
             logger.info( "did not find an existing management machine, creating new machine" );
             createNewMachine( serverNode );
         }
         return serverNode;
     }
 
     private void createNewMachine( ServerNode serverNode )
     {
         File cloudFolder = null;
         ComputeServiceContext jCloudsContext = null;
         try {
             // no existing management machine - create new server
             String project = serverNode.getProject();
             String secretKey = serverNode.getSecretKey();
             String apiKey = serverNode.getKey();
             logger.info( "Creating cloud folder with specific user credentials. Project: [{}], api key: [{}]", project, apiKey );
             jCloudsContext = CloudifyUtils.createJcloudsContext( project, apiKey, secretKey );
             cloudFolder = CloudifyUtils.createCloudFolder( project, apiKey, secretKey, jCloudsContext );
             logger.info( "cloud folder is at [{}]", cloudFolder );
 
             logger.info( "Creating security group for user." );
             CloudifyUtils.createCloudifySecurityGroup( jCloudsContext );
 
             //Command line for bootstrapping remote cloud.
             CommandLine cmdLine = new CommandLine( conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile() );
             cmdLine.addArgument( cloudFolder.getName() );
 
             DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
             ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );
 
             logger.info( "Executing command line: " + cmdLine );
             bootstrapExecutor.execute( cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
             logger.info( "waiting for output" );
             resultHandler.waitFor();
             logger.info( "finished waiting , exit value is [{}]", resultHandler.getExitValue() );
 
 
             String output = Utils.getOrDefault( Utils.getCachedOutput( serverNode ), "" );
             if ( resultHandler.getException() != null ) {
                 logger.info( "we have exceptions, checking for known issues" );
                 if ( output.contains( "found existing management machines" ) ) {
                     logger.info( "found 'found existing management machines' - issuing cloudify already exists message" );
                     throw new ServerException( Messages.get( "cloudify.already.exists" ) );
                 }
                 logger.info( "Command execution ended with errors: {}", output );
                 throw new RuntimeException( "Failed to bootstrap cloudify machine: "
                         + output, resultHandler.getException() );
             }
 
             logger.info( "finished handling errors, extracting IP" );
             String publicIp = Utils.extractIpFromBootstrapOutput( output );
             if ( StringUtils.isEmpty( publicIp ) ) {
                 logger.warn( "No public ip address found in bootstrap output. " + output );
                 throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output."
                         + output, resultHandler.getException() );
             }
             logger.info( "ip is [{}], saving to serverNode", publicIp );
 
             String privateKey = CloudifyUtils.getCloudPrivateKey( cloudFolder );
             if ( StringUtils.isEmpty( privateKey ) ) {
                 throw new RuntimeException( "Bootstrap failed. No pem file found in cloud directory." );
             }
             logger.info( "found PEM string" );
             logger.info( "Bootstrap cloud command ended successfully" );
 
             logger.info( "updating server node with new info" );
             serverNode.setPublicIP( publicIp );
             serverNode.setPrivateKey( privateKey );
 
             serverNode.save();
             logger.info("server node updated and saved");
 		}catch(Exception e) {
             serverNode.errorEvent("Invalid Credentials").save();
 			throw new RuntimeException("Unable to bootstrap cloud", e);
 		} finally {
 			if (cloudFolder != null && conf.server.cloudBootstrap.removeCloudFolder ) {
 				FileUtils.deleteQuietly(cloudFolder);
 			}
 			if (jCloudsContext != null) {
 				jCloudsContext.close();
 			}
 			serverNode.setStopped(true);
 			
 		}
 	}
 
 	private void deleteServer( String serverId )
 	{
 		novaContext.getApi().delete( serverId );
 		logger.info("Server id: {} was terminated.", serverId);
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
 			logger.info("Starting bootstrapping for server:{} " , server );
 
 			String script = FileUtils.readFileToString( conf.server.bootstrap.script );
 			ExecResponse response = runScriptOnNode( conf, server.getPublicIP(), script );
 
             logger.info("script finished");
 			logger.info("Bootstrap for server: {} finished successfully successfully. " +
                     "ExitStatus: {} \nOutput:  {}", new Object[]{server,
                     response.getExitStatus(),
                     response.getOutput()} );
 		}catch(Exception ex)
 		{
             try{
                 destroyServer( server );
             }catch(Exception e){
                 logger.info("destroying server after failed bootstrap threw exception",e);
             }
 			throw new ServerException("Failed to bootstrap cloudify machine: " + server.toDebugString(), ex);
 		}
 	}
 
 	static public ExecResponse runScriptOnNode( Conf conf, String serverIP, String script)
 			throws NumberFormatException, IOException
 	{
 		logger.debug("Run ssh on server: {} script: {}" , serverIP, script );
         Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
 		SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
 		SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIP, conf.server.bootstrap.ssh.port ),
 				LoginCredentials.builder().user( conf.server.bootstrap.ssh.user )
 						.privateKey(Strings2.toStringAndClose(new FileInputStream( conf.server.bootstrap.ssh.privateKey ))).build());
         ExecResponse execResponse = null;
 		try
 		{
 			sshConnection.connect();
             logger.info("ssh connected, executing");
 			execResponse = sshConnection.exec(script);
             logger.info("finished execution");
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
          if ( novaContext != null)
 		{
 			novaContext.close();
 		}
 	}
 
     public void setDeployManager(DeployManager deployManager) {
         this.deployManager = deployManager;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 
     public void setRetries( int retries )
     {
         this.retries = retries;
     }
 
     public void setBootstrapRetries( int bootstrapRetries )
     {
         this.bootstrapRetries = bootstrapRetries;
     }
 
     public void setCloudifyRestClient( CloudifyRestClient cloudifyRestClient )
     {
         this.cloudifyRestClient = cloudifyRestClient;
     }
 }
