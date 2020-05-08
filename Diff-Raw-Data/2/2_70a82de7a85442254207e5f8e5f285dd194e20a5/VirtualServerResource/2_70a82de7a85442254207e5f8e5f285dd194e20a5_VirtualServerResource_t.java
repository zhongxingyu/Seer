  /**
  * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 package n3phele.factory.rest.impl;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.annotation.security.RolesAllowed;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import n3phele.factory.hpcloud.HPCloudCreateServerRequest;
 import n3phele.factory.hpcloud.HPCloudCredentials;
 import n3phele.factory.hpcloud.HPCloudManager;
 import n3phele.factory.model.ServiceModelDao;
 import n3phele.factory.strategy.DebugStrategy;
 import n3phele.factory.strategy.ZombieStrategy;
 import n3phele.service.core.NotFoundException;
 import n3phele.service.core.Resource;
 import n3phele.service.model.core.AbstractManager;
 import n3phele.service.model.core.ExecutionFactoryAssimilateRequest;
 import n3phele.service.model.core.VirtualServerStatus;
 import n3phele.service.model.core.BaseEntity;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.CreateVirtualServerResponse;
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.GenericModelDao;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.VirtualServer;
 
 import org.jclouds.compute.domain.internal.NodeMetadataImpl;
 import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
 import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.apphosting.api.DeadlineExceededException;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Work;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.Base64;
 
 /** EC2 Virtual Server Resource manages the lifecycle of virtual machines on Amazon EC2 (or compatible) clouds.
  * @author Nigel Cook
  * @author Cristina Scheibler
  * @author Alexandre Leites
  */
 @Path("/")
 public class VirtualServerResource {
 	private Client						client	= null;
 	private final static VirtualServerManager	manager = new VirtualServerManager();
 	
 	public final static String FACTORY_NAME	= "nova-factory";
 	final Logger logger = LoggerFactory.getLogger(VirtualServerResource.class);
 	
 	private ZombieStrategy zombieStrategy;
 	private DebugStrategy debugStrategy;
 	
 	public VirtualServerResource(ZombieStrategy zombieStrategy, DebugStrategy debugStrategy)
 	{
 		this.zombieStrategy = zombieStrategy;
 		this.debugStrategy = debugStrategy;
 	}
 	
 	public VirtualServerResource()
 	{
 		zombieStrategy = new ZombieStrategy();
 		debugStrategy = new DebugStrategy();
 	}
 
 	@Context
 	UriInfo uriInfo;
 	
 	@POST
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/accountTest")
 	public String accountTest(@DefaultValue("false") @FormParam("fix") Boolean fix, @FormParam("id") String id, @FormParam("secret") String secret, @FormParam("key") String key, @FormParam("location") URI location, @FormParam("locationId") String locationId, @FormParam("email") String email, @FormParam("firstName") String firstName, @FormParam("lastName") String lastName, @FormParam("securityGroup") String security_groups)
 	{
 		logger.info("accountTest with fix " + fix);
 		if (fix && 	(email == null || email.trim().length() == 0) 			||
 					(firstName == null || firstName.trim().length() == 0) 	||
 					(lastName == null || lastName.trim().length() == 0))
 			throw new IllegalArgumentException("email details must be supplied with option to fix");
 		
 		boolean resultKey = checkKey(key, id, secret, location, locationId);
 		if (!resultKey && fix)
 			resultKey = createKey(key, id, secret, location, email, firstName, lastName, locationId);
 		
 		boolean result = checkSecurityGroup(security_groups, id, secret, location, locationId);
 		if (!result && fix)
 			result = makeSecurityGroup(security_groups, id, secret, location, email, firstName, lastName, locationId);
 
 		String reply = "";
 		if (!resultKey)
 			reply = "KeyPair " + key + " does not exist"
 					+ (fix ? " and could not be created.\n" : "\n");
 		if (!result)
 			reply = "Security group " + security_groups + " does not exist"
 					+ (fix ? " and could not be created.\n" : "\n");
 		return reply;
 	}
 
 	
 
 	/** Collection of virtual servers managed by the n3phele.resource
 	 * @param summary True to return only a collection summary, else return the collection children
 	 * @return the collection
 	 * @see Collection
 	 * @See BaseEntity
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer")
 	public Collection<BaseEntity> list(@DefaultValue("false") @QueryParam("summary") Boolean summary)
 	{
 		logger.info("get entered with summary " + summary);
 
 		Collection<BaseEntity> result = getCollection().collection(summary);
 		return result;
 	}
 	
 	/** List of input parameters supported by the factory for VM creation
 	 * 
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/inputParameters")
 	public TypedParameter[] inputParameterList()
 	{
 		return inputParameters;
 	}
 	
 	/** List of output parameters supported by the factory for VM creation
 	 * 
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/outputParameters")
 	public TypedParameter[] outputParameterList()
 	{
 		return outputParameters;
 	}
 	
 
 	/** create one or more new virtual servers. When multiple virtual servers are created, the siblings field of the virtualServer
 	 * object contains the URIs of all created virtual servers including that virtual server itself. 
 	 * @param request vm request information
 	 * @return URI of the first created virtual server. 
 	 * @throws Exception
 	 * @see ExecutionFactoryCreateRequest
 	 * @see VirtualServer
 	 * 
 	 */
 	@POST
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer")
 	public Response create(ExecutionFactoryCreateRequest request) throws Exception, InvalidParameterException
 	{
 		HPCloudCreateServerRequest hpCloudRequest = fillWithRequest(request);
 		logger.info("Creating zombie");
 		/**
 		 * We're creating a temporary VirtualServer item to call createWithZombie method if we're creating just one machine.
 		 * If zombie is created with success, they'll just initialize the ArrayLists.
 		 */
 		Date epoch									= new Date();
 		List<URI> virtualMachinesRefs				= null;
 		ArrayList<String> siblings 					= null;
 		ArrayList<VirtualServer> virtualServerList	= null;				
 				
 		virtualServerList = createOneOrMoreVMs(request, hpCloudRequest, epoch);
 		
 		//get URIs for siblings and pass to VMs
 		//FIXME siblings have the same info as virtualMachineRefs, but are Strings. Maybe we can use only one of them.
 		siblings = new ArrayList<String>(virtualServerList.size());
 		for(VirtualServer virtualServer : virtualServerList)
 		{
 			siblings.add(virtualServer.getUri().toString());
 		}
 		
 		updateVMSiblings(siblings, virtualServerList);
 		
 		logger.info("Created new VirtualServer");
 		
 		virtualMachinesRefs = getMachineUris(virtualServerList);
 		return Response.created(virtualServerList.get(0).getUri()).entity(new CreateVirtualServerResponse(virtualMachinesRefs)).build();
 	}
 	
 	
 	@POST
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/assimilate")
 	public Response assimilate(ExecutionFactoryAssimilateRequest request)
 	{
 		logger.info("Assimilating VM");
 		HPCloudManager hpCloudManager = getNewHPCloudManager(request.accessKey, request.encryptedSecret);
 		request.ipaddress = request.ipaddress.trim();
 		logger.info("IP Adress: "+request.ipaddress);
 		logger.info("Location ID: "+request.locationId);		
 		
 		NodeMetadataImpl serverImpl = hpCloudManager.getServerByIP(request.locationId, request.ipaddress);
 		
 		if(serverImpl == null){
 			logger.warn("IP Adress not found on cloud");
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 				
 		String instanceId 	= serverImpl.getId();
 		String zone			= instanceId.substring(0, instanceId.indexOf("/"));
 		instanceId 			= instanceId.substring(instanceId.indexOf("/")+1);
 		logger.info("Server Instance ID: "+instanceId);
 	
 		
 		List<VirtualServer> list = getByInstanceId(instanceId);
 		if(list.size() > 0){
 			logger.warn("VM already exists on factory");
 			return Response.status(Response.Status.CONFLICT).build();
 		}
 		logger.info("Retrieved list");
 		/*
 		 * TODO: We're using a second request because NodeMetadataImpl doesn't return the creation date.
 		 * We need to refactory this.
 		 */
 		Server server = hpCloudManager.getServerById(zone, instanceId);
 		if(server == null){
 			logger.warn("Error retrieving VM from cloud");
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 		
 		
 		ArrayList<NameValue> params = new ArrayList<NameValue>();
 		
 		NameValue param = new NameValue();
 		param.setKey("locationId");
 		param.setValue(request.locationId);		
 		params.add(param);
 		
 		param = new NameValue();		
 		param.setKey("flavorRef");
 		param.setValue(server.getFlavor().getId());
 		params.add(param);
 		
 		
 		VirtualServer virtualServer = new VirtualServer(server.getName(), request.description, request.location, params, request.notification, request.accessKey, request.encryptedSecret, request.owner, request.idempotencyKey);
 		virtualServer.setCreated(server.getCreated());
 		virtualServer.setInstanceId(instanceId);
 		
 		for(NameValue nm: virtualServer.getParameters()){
 			logger.info("Key: "+nm.getKey()+", Value: "+nm.getValue());
 		}
 	
 		add(virtualServer);
 		
 		List<URI> virtualMachinesRefs = new ArrayList<URI>(1);
 		virtualMachinesRefs.add(virtualServer.getUri());
 		return Response.created(virtualServer.getUri()).entity(new CreateVirtualServerResponse(virtualMachinesRefs)).build();
 	}
 
 	protected void updateVMSiblings(ArrayList<String> siblings,
 			ArrayList<VirtualServer> virtualServerList) {
 		for(VirtualServer virtualServer : virtualServerList)
 		{
 			virtualServer.setSiblings(siblings);
 			logger.info("updating");
 			update(virtualServer);
 		}
 	}
 
 
 	protected ArrayList<VirtualServer> createOneOrMoreVMs(
 			ExecutionFactoryCreateRequest request,
 			HPCloudCreateServerRequest hpCloudRequest, Date epoch) {
 			
 		boolean createdFromZombie = false;
 		
 		ArrayList<VirtualServer> virtualServerList = null;
 		
 		if(hpCloudRequest.nodeCount == 1)
 		{
 			VirtualServer tempVirtualServer = new VirtualServer(request.name, request.description, request.location, request.parameters,
 					request.notification, request.accessKey, request.encryptedSecret,
 					request.owner, request.idempotencyKey);
 			
 			createdFromZombie = createWithZombie(tempVirtualServer);
 			if(createdFromZombie)
 			{
 				logger.info("Only one zombie virtualServer");
 				virtualServerList 	= new ArrayList<VirtualServer>(1);					
 				virtualServerList.	add(tempVirtualServer);			
 			}
 		}
 		
 		if( !createdFromZombie )
 		{
 			HPCloudManager hpCloudManager 			= getNewHPCloudManager(request.accessKey, request.encryptedSecret);
 			List<ServerCreated> resultServerList	= hpCloudManager.createServerRequest(hpCloudRequest);
 			virtualServerList 						= new ArrayList<VirtualServer>(resultServerList.size());
 			
 			for (ServerCreated server : resultServerList)
 			{
 				VirtualServer virtualServer = new VirtualServer(server.getName(), request.description, request.location,
 														request.parameters, request.notification, request.accessKey,
 														request.encryptedSecret, request.owner, request.idempotencyKey);
 				virtualServer.setCreated(epoch);
 				virtualServer.setInstanceId(server.getId());
 				logger.info("Created new VirtualServer: " + virtualServer.getUri());
 				add(virtualServer);
 				logger.info("Added new VirtualServer: " + virtualServer.getUri());
 				virtualServerList.add(virtualServer);
 			}
 		}
 		return virtualServerList;
 	}
 
 	protected List<URI> getMachineUris(ArrayList<VirtualServer> virtualServerList)
 	{
 		List<URI> Uris = new ArrayList<URI>(virtualServerList.size());
 		
 		for(VirtualServer virtualServer: virtualServerList)
 		{
 			Uris.add(virtualServer.getUri());
 		}
 		
 		return Uris;		
 	}
 
 	protected HPCloudCreateServerRequest fillWithRequest(
 			ExecutionFactoryCreateRequest request) throws InvalidParameterException {
 		int nodeCount = 1;		
 		logger.info("Creating hp cloud request");
 		HPCloudCreateServerRequest hpCloudRequest = new HPCloudCreateServerRequest();
 		
 		logger.info("Setting parameters");
 		for (NameValue parameter : request.parameters)
 		{
 			logger.info("Paramater: "+parameter.getKey()+" ,value: "+parameter.getValue());
 			if (parameter.getKey().equalsIgnoreCase("nodeCount"))
 			{
 				String value = parameter.getValue();
 				try
 				{
 					nodeCount = Integer.valueOf(value);
 					if (nodeCount <= 0)
 						nodeCount = 1;
 				} catch (Exception e){}
 			}
 			
 			if (parameter.getKey().equalsIgnoreCase("imageRef"))
 			{
 				hpCloudRequest.imageRef = parameter.getValue();
 			}
 
 			if (parameter.getKey().equalsIgnoreCase("flavorRef"))
 			{
 				hpCloudRequest.flavorRef = parameter.getValue();
 			}
 			
 			if (parameter.getKey().equalsIgnoreCase("security_groups"))
 			{
 				hpCloudRequest.security_groups = parameter.getValue();
 			}
 
 			if (parameter.getKey().equalsIgnoreCase("user_data"))
 			{
 				String data = parameter.getValue();
 				if( Base64.isBase64(data) )
 					hpCloudRequest.user_data = new String( Base64.decode(data) );
 				else
 					hpCloudRequest.user_data = data;
 			}
 			
 			if (parameter.getKey().equalsIgnoreCase("locationId"))
 			{
 				hpCloudRequest.locationId = parameter.getValue();
 			}
 			
 			if (parameter.getKey().equalsIgnoreCase("key_name"))
 			{
 				hpCloudRequest.keyName = parameter.getValue();
 			}
 		}
 		
 		if(hpCloudRequest.imageRef == null || hpCloudRequest.flavorRef == null ||
 				hpCloudRequest.security_groups == null || hpCloudRequest.locationId == null || hpCloudRequest.keyName == null)
 			throw new InvalidParameterException("Some parameters were not filled correctly");
 		
 		hpCloudRequest.nodeCount 	= nodeCount;
 		hpCloudRequest.serverName 	= request.name;
 		return hpCloudRequest;
 	}
 
 	/** Get details of a specific virtual server. This operation does a deep get, getting information from the cloud before
 	 * issuing a reply.
 	 * @param id the virtual server Id.
 	 * @return virtualServer object
 	 * @throws NotFoundException
 	 */
 	@GET
 	@Produces({ "application/json",
 				"application/vnd.com.n3phele.VirtualServer+json" })
 	@Path("virtualServer/{id}")
 	@RolesAllowed("authenticated")
 	public VirtualServer get(@PathParam("id") Long id) throws NotFoundException
 	{
 		logger.info("Getting VirtualServer with id "+id);
 		VirtualServer item = deepGet(id);
 
 		return item;
 	}
 
 	/** Kill the nominated virtual server
 	 * @param id the id of the virtual server to termination
 	 * @throws NotFoundException
 	 */
 	@DELETE
 	@Path("virtualServer/{id}")
 	@RolesAllowed("authenticated")
 	public void kill(@PathParam("id") Long id, @DefaultValue("false") @QueryParam("debug") boolean debug, @DefaultValue("false") @QueryParam("error") boolean error, @DefaultValue("false") @QueryParam("dbonly") boolean dbonly) throws NotFoundException
 	{
 		logger.info("[REST] DELETE was been called with id" + id + " debug = " + debug + " error = " + error);
 		VirtualServer virtualServer = null;
 		try
 		{
 			if(dbonly)
 			{
 				virtualServer = load(id);
 				if(virtualServer != null)
 					delete(virtualServer);
 			}
 			else
 			{
 				virtualServer = deepGet(id);
 				
 				if (error && !debug)
					terminate(virtualServer);
				else
 					softKill(virtualServer, error);
 			}
 		}
 		catch(Exception e)
 		{
 			if(!dbonly)
 			{
 				try
 				{
 					virtualServer = deepGet(id);
 					terminate(virtualServer);
 				}
 				catch(Exception ee)
 				{
 					if (virtualServer != null)
 						delete(virtualServer);
 				}
 			}
 		}
 	}
 	
 	
 	@GET
 	@Produces("text/plain")
 	@Path("total")
 	public String total()
 	{
 		String result;
 		Collection<VirtualServer> servers = getCollection();
 		result = Long.toString(servers.getTotal()) + "\n";
 		result += Calendar.getInstance().getTime().toString() + "\n";
 		return result;
 	}
 	
 	/** terminate a nominated virtual server
 	 * @param virtualServer virtual server to be terminated
 	 * @param error true that the server needs to be terminated, false it is a candidate for reuse
 	 */
 	protected void terminate(VirtualServer virtualServer)
 	{		
 		try
 		{
 			deleteInstance(virtualServer);
 		} catch (Exception e)
 		{
 			manager.delete(virtualServer);
 		}
 	}
 	
 	/** Kill a nominated virtual server, preserving as a zombie if it is suitable
 	 * @param virtualServer virtual server to be terminated
 	 * @param stop true only stop the server, false terminate the server
 	 */
 	protected void softKill(VirtualServer virtualServer, boolean error)
 	{		
 		try
 		{
 			if (!isZombieCandidate(virtualServer))
 				deleteInstance(virtualServer);
 			else
 			{
 				if (error)
 					makeDebug(virtualServer);
 				else
 					makeZombie(virtualServer);
 			}
 		} catch (Exception e)
 		{
 			manager.delete(virtualServer);
 		}
 	}
 
 	protected void makeZombie(VirtualServer virtualServer) throws Exception {
 		//FIXME too generic exception handling
 		String instanceId = virtualServer.getInstanceId();
 		try
 		{
 			zombieStrategy.makeZombie(virtualServer, this, getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey()));
 			logger.info("created zombie instance of instance " + instanceId);
 		}
 		catch(Exception e)
 		{
 			logger.error("makeZombie delete of instanceId " + instanceId, e);
 			virtualServer.setInstanceId(instanceId);
 			deleteInstance(virtualServer);
 			throw e;
 		}
 	}		
 	
 	private void makeDebug(VirtualServer virtualServer) throws Exception
 	{
 		String instanceId = virtualServer.getInstanceId();
 		//FIXME too generic exception handling
 		try
 		{
 			debugStrategy.makeDebug(virtualServer, this, getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey()));
 		} catch (Exception e)
 		{
 			logger.error("makeDebug delete of instanceId "	+ instanceId, e);
 			virtualServer.setInstanceId(instanceId);
 			deleteInstance(virtualServer);
 			throw e;
 		}
 	}
 	
 	private boolean isZombieCandidate(VirtualServer virtualServer)
 	{
 		boolean result = virtualServer != null
 				&& virtualServer.getInstanceId() != null
 				&& virtualServer.getInstanceId().length() > 0;
 
 		if (result)
 		{
 			if (virtualServer.getSiblings() != null	&& virtualServer.getSiblings().size() > 1)
 			{
 				logger.info("Server has " + virtualServer.getSiblings().size()	+ " siblings");
 				result = false;
 			}
 			
 			if (!virtualServer.getStatus().equals(VirtualServerStatus.running)) 
 			{
 				logger.info("Server is " + virtualServer.getStatus());
 				result = false;
 			}
 		} else
 		{
 			logger.info("Null server or instanceId");
 		}
 		logger.info("isZombieCandidate returning " + result);
 		return result;
 	}
 
 	/** Refresh the model. The refresh process walks the model and updates the state of virtual servers to reflect their
 	 * status in EC2. Change of state notifications will be issued as appropriate to the notification URL nominated in the create request.
 	 * @return summary of the virtualServer collection. The collection total size field, if negitive, denotes a partial refresh operation.
 	 */
 	@GET
 	@Path("admin/refresh")
 	@Produces("application/json")
 	@RolesAllowed("admin")
 	public Collection<BaseEntity> refresh()
 	{
 		long start = Calendar.getInstance().getTimeInMillis();
 
 		Collection<BaseEntity> result = refreshCollection();
 
 		logger.info(String.format("-----refresh-- %d ms processing %d items", (Calendar.getInstance().getTimeInMillis() - start), result.getTotal()));
 		return result;
 	}
 
 	protected VirtualServer deepGet(Long id) throws NotFoundException
 	{
 		VirtualServer virtualServer = load(id);
 		HPCloudManager hpCloudManager = getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey());
 		logger.info("Virtual Server retrieved: "+virtualServer.getInstanceId());
 		updateVirtualServer(virtualServer, hpCloudManager);
 		return virtualServer;
 	}
 	
 	/** Updates virtual server object and data store state
 	 * @param virtualServer object to update
 	 */
 	protected void updateVirtualServer(VirtualServer virtualServer, HPCloudManager hpCloudManager) throws IllegalArgumentException
 	{
 		String instanceId = virtualServer.getInstanceId();
 		boolean madeIntoZombie = virtualServer.isZombie();
 
 		if (madeIntoZombie)
 		{
 			if (updateStatus(virtualServer, VirtualServerStatus.terminated))
 				update(virtualServer);
 			
 			if (virtualServer.getStatus().equals("terminated"))
 			{
 				logger.info("Instance " + virtualServer.getName() + " terminated .. purging");
 				delete(virtualServer);
 				return;
 			}
 		} else if (instanceId != null && instanceId.length() > 0)
 		{
 			String locationId = getLocationId(virtualServer);
 			
 			Server s = hpCloudManager.getServerById(locationId, virtualServer.getInstanceId());
 			if (s != null)
 			{
 				VirtualServerStatus currentStatus = mapStatus(s);	
 				
 				/**
 				 * If the statuses are different, and the current cloud status
 				 * is ACTIVE (Running), we should update.
 				 */
 				if (!virtualServer.getStatus().equals(currentStatus) && currentStatus.equals(VirtualServerStatus.running)) 
 				{
 					Map<String, String> tags = new HashMap<String, String>();
 					tags.put("n3phele-name", virtualServer.getName());
 					tags.put("n3phele-factory", Resource.get("factoryName", FACTORY_NAME));
 					tags.put("n3phele-uri", virtualServer.getUri().toString());
 					hpCloudManager.putServerTags(virtualServer.getInstanceId(), locationId, tags);
 					virtualServer.setOutputParameters(HPCloudExtractor.extract(s));		
 				}
 				
 				String publicIP = "";
 				String privateIP = "";
 				ArrayList<NameValue> params = virtualServer.getOutputParameters();
 				if( params != null )
 				{
 					for(NameValue p : params)
 					{
 						if(p.getKey().equalsIgnoreCase("publicIpAddress" ))
 						{
 							publicIP = p.getValue();
 							logger.info("Name: "+p.getKey()+" ,Value: "+p.getValue());
 						}
 						if(p.getKey().equalsIgnoreCase("privateIpAddress" ))
 						{
 							privateIP = p.getValue();
 							logger.info("Name: "+p.getKey()+" ,Value: "+p.getValue());
 						}
 					}
 				}
 								
 				if(!(publicIP.equalsIgnoreCase(privateIP)))
 				{
 					logger.info("IP public is set, updating vs");
 					if (updateStatus(virtualServer, currentStatus))
 						update(virtualServer);
 				}
 
 				if (virtualServer.getStatus().equals(VirtualServerStatus.terminated))
 				{
 					logger.info("Instance " + virtualServer.getInstanceId() + " terminated .. purging");
 					delete(virtualServer);
 					return;
 				}
 			} else
 			{
 				logger.info("Instance " + virtualServer.getInstanceId() + " not found, assumed terminated .. purging");
 				delete(virtualServer);
 				return;
 			}
 		}
 	}
 	
 	private VirtualServerStatus mapStatus(Server server)
 	{	
 		if( server.getStatus().toString().equals("ACTIVE") )
 		{
 			return VirtualServerStatus.running;
 		}
 		else if(server.getStatus().toString().equals("BUILD")  || server.getStatus().toString().equals("REBUILD") ||
 				server.getStatus().toString().equals("REBOOT") || server.getStatus().toString().equals("HARD_REBOOT"))
 		{
 			return VirtualServerStatus.initializing;
 		}
 		else
 		{
 			return VirtualServerStatus.terminated;
 		}
 	}
 	
 	public String getLocationId(VirtualServer virtualServer)
 	{
 		ArrayList<NameValue> listParameters = virtualServer.getParameters();
 		String locationId = null;
 
 		for (NameValue parameter : listParameters)
 		{
 			if (parameter.getKey().equalsIgnoreCase("locationId"))
 			{
 				locationId = parameter.getValue();
 				break;
 			}
 		}
 		
 		return locationId;
 	}
 
 
 	protected void refreshVirtualServer(VirtualServer virtualServer)
 	{
 		if (virtualServer == null)
 			return;
 
 		HPCloudManager hpCloudManager = getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey());
 
 		String locationId = getLocationId(virtualServer);
 
 		Server s = hpCloudManager.getServerById(locationId, virtualServer.getInstanceId());
 		if (s != null)
 		{
 			VirtualServerStatus currentStatus = mapStatus(s);
 			virtualServer.setStatus(currentStatus);
 		} else
 		{
 			logger.info("Instance " + virtualServer.getInstanceId() + " not found, assumed terminated ..");
 			virtualServer.setStatus(VirtualServerStatus.terminated);
 		}
 	}
 	
 	/** Check if a zombie has expired and clean up if it has
 	 * @param virtualsServer virtual server
 	 * @return TRUE if zombie
 	 */
 	public boolean checkForZombieAndDebugExpiry(VirtualServer virtualServer)
 	{
 		/**
 		 * Double-checking if the VirtualServer is a zombie/debug machine.
 		 * We're checking the hpcloud tags AND the database name.
 		 */
 		boolean isDebugInstance = debugStrategy.isDebug(virtualServer);
 		boolean isZombieInstance = zombieStrategy.isZombie(virtualServer);
 		
 		if(isZombieInstance || isDebugInstance )
 		{
 			HPCloudManager hpCloudManager = getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey());
 			
 			if( virtualServer.getStatus().equals(VirtualServerStatus.terminated) )
 			{
 				logger.info("Found dead "+virtualServer.getName()+" with id "+virtualServer.getInstanceId()+" created "+virtualServer.getCreated());
 				manager.delete(virtualServer);
 				return true;
 			}
 			
 			long created	= virtualServer.getCreated().getTime();
 			long now 		= new Date().getTime();
 			long age 		= ((now - created)% (60*60*1000))/60000;
 			
 			if( (age > zombieStrategy.getMinutesExpirationTime() && (isDebugInstance || isZombieInstance) ) )
 			{
 				logger.info("Killing "+virtualServer.getName()+" with id "+virtualServer.getInstanceId()+" created "+virtualServer.getCreated() );
 				virtualServer.setName(isDebugInstance? "debug" : "zombie");
 				update(virtualServer);
 				
 				String locationId = getLocationId(virtualServer);
 				hpCloudManager.terminateNode(locationId, virtualServer.getInstanceId());
 			}
 		}
 		return false;
 	}
 
 	protected HPCloudManager getNewHPCloudManager(String acessKey, String encryptedKey)
 	{
 		HPCloudManager hpCloudManager = new HPCloudManager(getNewHPCredentials(acessKey, encryptedKey));
 		return hpCloudManager;
 	}
 	
 	private Collection<VirtualServer> getNonTerminatedServers()
 	{
 		return manager.getNotTerminatedMachines();	
 	}
 
 	private Collection<BaseEntity> refreshCollection()
 	{
 		Collection<VirtualServer> servers	= getNonTerminatedServers();
 		logger.info("Refreshing collection of non terminated virtual servers. Current size: " + servers.getElements().size());
 		
 		
 		Collection<BaseEntity> result 		= servers.collection(true);
 		HPCloudManager hpCloudManager 		= null;
 		String accessKey					= null;
 		String encryptedKey					= null;
 		
 		try {
 			for (VirtualServer virtualServer : servers.getElements()) {
 	
 					try {
 						if(virtualServer.getUri() != null && !checkForZombieAndDebugExpiry(virtualServer))
 						{
 							String accessKey2		= virtualServer.getAccessKey();
 							String encryptedKey2	= virtualServer.getEncryptedKey();
 							if(hpCloudManager == null || !accessKey.equalsIgnoreCase(accessKey2)  || !encryptedKey.equalsIgnoreCase(encryptedKey2))
 							{
 								logger.info("-------refreshCollection-- creating a new HPCloudManager");
 								hpCloudManager 	= getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey());
 								accessKey 		= accessKey2;
 								encryptedKey 	= encryptedKey2;
 							}
 							updateVirtualServer(virtualServer, hpCloudManager);
 						}
 					}
 					catch (Exception e) {
 						logger.warn( " refresh failed. Killing..",e);
 						try {
 							terminate(virtualServer);
 						} catch (Exception another) {
 							
 						} finally {
 							delete(virtualServer);
 						}
 					}
 			}
 		} catch (DeadlineExceededException deadline) {
 			return result;
 		}
 		return result;
 	}
 
 	public void deleteInstance(VirtualServer virtualServer) throws Exception
 	{
 		String instanceId = virtualServer.getInstanceId();
 		try
 		{
 			if (!virtualServer.getStatus().equals(VirtualServerStatus.terminated) && instanceId != null && instanceId.length() > 0)
 			{
 
 				HPCloudManager hpManager = getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey());
 
 				String locationId = getLocationId(virtualServer);
 
 				if (locationId == null)
 				{
 					logger.error("locationId is null, cannot delete instance "	+ virtualServer.getInstanceId(), new IllegalArgumentException("locationId: null"));
 					throw new IllegalArgumentException("locationId: null");
 				}
 
 				boolean isDeleted = hpManager.terminateNode(locationId, virtualServer.getInstanceId());
 
 				if (isDeleted)
 				{
 					logger.info("Instance " + virtualServer.getInstanceId() + "deleted");
 					if (updateStatus(virtualServer, VirtualServerStatus.terminated))
 						update(virtualServer);
 					
 				} else
 				{
 					logger.warn("Instance " + virtualServer.getInstanceId() + "could not be deleted");
 				}
 			}
 			else
 			{
 				if (updateStatus(virtualServer, VirtualServerStatus.terminated))
 					update(virtualServer);
 			}
 
 		} catch (Exception e)
 		{
 			logger.error("Cleanup delete of instanceId " + instanceId, e);
 			throw e;
 		}
 
 	}
 	
 	public boolean createWithZombie(VirtualServer virtualServer)
 	{
 		logger.info("Entered createWithZombie");
 		List<VirtualServer> zombies 	= getZombie();
 		HPCloudManager hpCloudManager 	= null;
 		String accessKey				= null;
 		String encryptedKey				= null;
 		if (zombies != null)
 		{
 			logger.info("Got " + zombies.size() + " Zombies ");
 			zombieCheck: for (VirtualServer zombieVirtualServer : zombies)
 			{
 				boolean locationMatch = zombieVirtualServer.getLocation().equals(virtualServer.getLocation());
 				boolean accessMatch = zombieVirtualServer.getAccessKey().equals(virtualServer.getAccessKey());
 				boolean secretMatch = zombieVirtualServer.getEncryptedKey().equals(virtualServer.getEncryptedKey());
 				logger.info(" Zombie " + zombieVirtualServer.getInstanceId() + " location "+ locationMatch + " access " + accessMatch + " secret "+ secretMatch);
 				
 				logger.info(" locationMatch: " + locationMatch + " accessMatch: " + accessMatch + " secretMatch: " + secretMatch);
 				if (locationMatch && accessMatch && secretMatch)
 				{
 					Map<String, String> zombieMap = zombieVirtualServer.getParametersMap();
 					for (NameValue parameter : virtualServer.getParameters())
 					{
 						if (!safeEquals(parameter.getValue(), zombieMap.get(parameter.getKey())))
 						{
 							logger.info("Mismatch on " + parameter.getKey() + " need "+ parameter.getValue() + " zombie "+ zombieMap.get(parameter.getKey()));
 							continue zombieCheck;
 						}
 					}
 					
 					final Long zombieId = zombieVirtualServer.getId();
 					boolean claimed = VirtualServerResource.dao.transact(new Work<Boolean>()
 					{
 							@Override
 							public Boolean run()
 							{
 								try{
 									VirtualServer zombie = VirtualServerResource.dao.get(zombieId);
 									zombie.setIdempotencyKey(new Date().toString());
 									VirtualServerResource.dao.add(zombie);
 									VirtualServerResource.dao.delete(zombie);
 								}
 								catch(Exception e){
 									return false;
 								}
 								return true;
 							}
 					 });
 					
 					if (claimed)
 					{
 						List<VirtualServer> leftOverZombies = getZombie();
 						if (leftOverZombies != null)
 							logger.info("Got " + leftOverZombies.size() + " zombies remaining");
 						else
 							logger.info("Got 0 Zombies remaining");
 						
 						logger.info("Claimed " + zombieVirtualServer.getInstanceId());
 						refreshVirtualServer(zombieVirtualServer);
 						
 						if( !zombieVirtualServer.getStatus().equals(VirtualServerStatus.running) )
 						{
 							terminate(zombieVirtualServer);
 							continue;
 						}
 						
 						//The instance object does not exist yet, add it
 						if(virtualServer.getId() == null)
 						{
 							add(virtualServer);
 						}						
 						
 						virtualServer.setInstanceId(zombieVirtualServer.getInstanceId());
 						virtualServer.setCreated(zombieVirtualServer.getCreated());
 						
 						String accessKey2		= virtualServer.getAccessKey();
 						String encryptedKey2	= virtualServer.getEncryptedKey();
 						if(hpCloudManager == null || !accessKey.equalsIgnoreCase(accessKey2)  || !encryptedKey.equalsIgnoreCase(encryptedKey2))
 						{
 							hpCloudManager 	= getNewHPCloudManager(virtualServer.getAccessKey(), virtualServer.getEncryptedKey());
 							accessKey 		= accessKey2;
 							encryptedKey 	= encryptedKey2;
 						}
 						
 						updateVirtualServer(virtualServer, hpCloudManager);
 						
 						if( virtualServer.getStatus().equals(VirtualServerStatus.running) )
 							return true;
 						else
 							continue; // There's no difference calling continue here, i think.
 						
 					} else
 					{
 						logger.warn("Zombie contention on " + zombieVirtualServer.getInstanceId());
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean safeEquals(String a, String b)
 	{
 		boolean emptyA = a == null || a.length() == 0;
 		boolean emptyB = b == null || b.length() == 0;
 		boolean aOrBNull = a == null || b == null;
 		
 		if (emptyA && emptyB)
 			return true;
 		if (aOrBNull)
 			return false;
 		return (a.equals(b));
 	}
 	
 	public boolean updateStatus(VirtualServer virtualServer, VirtualServerStatus newStatus)
 	{
 		VirtualServerStatus oldStatus = virtualServer.getStatus();
 		if (oldStatus.equals(newStatus))
 			return false;
 		virtualServer.setStatus(newStatus);
 		try
 		{
 			sendNotification(virtualServer, oldStatus, newStatus);
 		} catch (Exception e)
 		{
 			logger.info("SendNotification exception to <" + virtualServer.getNotification() + "> from " + virtualServer.getUri() + " old: " + oldStatus + " new: " + virtualServer.getStatus(), e);
 			if (oldStatus.equals(newStatus))
 			{
 				logger.warn("Cancelling SendNotification to <" + virtualServer.getNotification() + "> from " + virtualServer.getUri() + " old: " + oldStatus + " new: " + virtualServer.getStatus());
 			}
 			else
 			{
 				virtualServer.setStatus(newStatus);
 			}
 		}
 		return true;
 	}
 
 	
 	private void sendNotification(VirtualServer virtualServer, VirtualServerStatus oldStatus, VirtualServerStatus newStatus) throws Exception
 	{
 		URI notification = virtualServer.getNotification();
 		logger.info("SendNotification to <" + notification + "> from " + virtualServer.getUri() + " old: " + oldStatus + " new: " + virtualServer.getStatus());
 
 		if (notification == null)
 			return;
 
 		try{
 			if (client == null)
 			{
 				client = Client.create();
 			}
 			WebResource resource = client.resource(virtualServer.getNotification());
 	
 			ClientResponse response = resource.queryParam("source", virtualServer.getUri().toString()).queryParam("oldStatus", oldStatus.toString()).queryParam("newStatus", newStatus.toString()).type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
 			logger.info("Notificaion status " + response.getStatus());
 			if (response.getStatus() == 410)
 			{
 				logger.info("VM GONE .. killing " + virtualServer.getUri() + " silencing reporting to " + virtualServer.getNotification());
 				virtualServer.setNotification(null);
 				deleteInstance(virtualServer);
 			}
 		}catch(Exception e){
 			logger.warn(e.getMessage());
 		}
 	}
 
 	protected boolean checkKey(String key, String id, String secret, URI location, String locationId)
 	{
 		HPCloudManager hpCloudManager = getNewHPCloudManager(id, secret);
 
 		return hpCloudManager.checkKeyPair(key, locationId);
 	}
 
 	protected boolean createKey(String key, String id, String secret, URI location, String email, String firstName, String lastName, String locationId)
 	{
 		HPCloudManager hpCloudManager = getNewHPCloudManager(id, secret);
 		KeyPair newKey = hpCloudManager.createKeyPair(key, locationId);
 
 		if (newKey != null)
 		{
 			logger.info("Got " + newKey.toString());
 			sendNotificationEmail(newKey, email, firstName, lastName, location);
 			return true;
 		}
 
 		logger.error("Key pair couldn't be created");
 		return false;
 	}
 
 	public void sendNotificationEmail(KeyPair keyPair, String to, String firstName, String lastName, URI location)
 	{
 		try
 		{
 			StringBuilder subject = new StringBuilder();
 			StringBuilder body = new StringBuilder();
 			subject.append("Auto-generated keyPair \"");
 			subject.append(keyPair.getName());
 			subject.append("\"");
 			body.append(firstName);
 			body.append(",\n\nA keypair named \"");
 			body.append(keyPair.getName());
 			body.append("\" has been generated for you. \n\n");
 			body.append("Please keep this information secure as it allows access to the virtual machines");
 			body.append(" run on your behalf by n3phele on the cloud at ");
 			body.append(location.toString());
 			body.append(". To access the machines using ssh copy all of the lines");
 			body.append(" including -----BEGIN RSA PRIVATE KEY----- and -----END RSA PRIVATE KEY-----");
 			body.append(" into a file named ");
 			body.append(keyPair.getName());
 			body.append(".pem\n\n");
 			// TODO: check if private key is the same as key material
 			body.append(keyPair.getPrivateKey());
 			body.append("\n\nn3phele\n--\nhttps://n3phele.appspot.com\n\n");
 
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress("n3phele@gmail.com", "n3phele"));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, firstName
 					+ " " + lastName));
 			msg.setSubject(subject.toString());
 			msg.setText(body.toString());
 			Transport.send(msg);
 
 		} catch (AddressException e)
 		{
 			logger.error("Email to " + to, e);
 		} catch (MessagingException e)
 		{
 			logger.error("Email to " + to, e);
 		} catch (UnsupportedEncodingException e)
 		{
 			logger.error( "Email to " + to, e);
 		} catch (Exception e)
 		{
 			logger.error( "Email to " + to, e);
 		}
 	}
 
 	protected boolean checkSecurityGroup(String groupName, String id, String secret, URI location, String locationId)
 	{
 		HPCloudManager hpCloudManager = getNewHPCloudManager(id, secret);
 
 		return hpCloudManager.checkSecurityGroup(groupName, locationId);
 	}
 	
 	public void sendSecurityGroupNotificationEmail(String securityGroup, String to, String firstName, String lastName, URI location)
 	{
 		try
 		{
 			StringBuilder subject = new StringBuilder();
 			StringBuilder body = new StringBuilder();
 			subject.append("Auto-generated security group: \"");
 			subject.append(securityGroup);
 			subject.append("\"");
 			body.append(firstName);
 			body.append(",\n\nA security group named \"");
 			body.append(securityGroup);
 			body.append("\" has been generated for you. \n\n");
 			body.append("This is used as the default firewall for machines");
 			body.append(" run on your behalf on ");
 			body.append(location.toString());
 			body.append(".\n\nn3phele\n--\nhttps://n3phele.appspot.com\n\n");
 
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress("n3phele@gmail.com", "n3phele"));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, firstName
 					+ " " + lastName));
 			msg.setSubject(subject.toString());
 			msg.setText(body.toString());
 			Transport.send(msg);
 
 		} catch (AddressException e)
 		{
 			logger.error( "Email to " + to, e);
 		} catch (MessagingException e)
 		{
 			logger.error( "Email to " + to, e);
 		} catch (UnsupportedEncodingException e)
 		{
 			logger.error("Email to " + to, e);
 		} catch (Exception e)
 		{
 			logger.error("Email to " + to, e);
 		}
 
 	}
 	
 	protected boolean makeSecurityGroup(String groupName, String id, String secret, URI location, String to, String firstName, String lastName, String locationId)
 	{
 		HPCloudManager hpCloudManager = getNewHPCloudManager(id, secret);
 
 		SecurityGroup sg = hpCloudManager.createSecurityGroup(groupName, locationId);
 		sendSecurityGroupNotificationEmail(sg.getName(), to, firstName, lastName, location);
 
 		return true;
 	}	
 	
 	protected HPCloudCredentials getNewHPCredentials(String identity, String secretKey)
 	{
 		try
 		{
 			HPCloudCredentials credentials = new HPCloudCredentials(identity, secretKey);
 			return credentials;
 		}
 		catch (Exception e)
 		{
 			throw new WebApplicationException();
 		}
 	}
 
 	public static class VirtualServerManager extends AbstractManager<VirtualServer> {
 
 		@Override
 		protected URI myPath()
 		{
 			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8889/resources")).path("virtualServer").build();
 		}
 
 		@Override
 		public GenericModelDao<VirtualServer> itemDaoFactory() {
 			return new ServiceModelDao<VirtualServer>(VirtualServer.class);
 		}
 		
 		public void add(VirtualServer vs){
 			super.add(vs);
 		}
 		
 		public VirtualServer get(Long id){
 			return super.get(id);
 		}
 		
 		public VirtualServer get(URI uri){
 			return super.get(uri);
 		}
 		
 		public void update(VirtualServer vs){
 			super.update(vs);
 		}
 		
 		public void delete(VirtualServer vs){
 			super.delete(vs);
 		}
 		
 		public Collection<VirtualServer> getNotTerminatedMachines()
 		{			
 			java.util.Collection<VirtualServer> serversRunning = super.itemDao.collectionByProperty("status", VirtualServerStatus.running);
 			java.util.Collection<VirtualServer> serversInitializing = super.itemDao.collectionByProperty("status", VirtualServerStatus.initializing);
 			
 			ArrayList<VirtualServer> servers = new ArrayList<VirtualServer>();
 			servers.addAll(serversRunning);
 			servers.addAll(serversInitializing);
 			
 			return new Collection<VirtualServer>(super.itemDao.clazz.getSimpleName(), this.path, servers);
 		}
 		
 		public Collection<VirtualServer> getCollection(){
 			return super.getCollection();
 		}
 		
 	}
 
 	/**
 	 * Located a item from the persistent store based on the item id.
 	 * @param id
 	 * @return the item
 	 * @throws NotFoundException is the object does not exist
 	 */
 	public VirtualServer load(Long id) throws NotFoundException { return manager.get(id); }
 	/**
 	 * Locate a item from the persistent store based on the item name.
 	 * @param name
 	 * @return the item
 	 * @throws NotFoundException is the object does not exist
 	 *//*
 	public VirtualServer load(String name) throws NotFoundException { return manager.get(name); }*/
 	/**
 	 * Locate a item from the persistent store based on the item URI.
 	 * @param uri
 	 * @return the item
 	 * @throws NotFoundException is the object does not exist
 	 */
 	public VirtualServer load(URI uri) throws NotFoundException { return manager.get(uri); }
 	/** Add a new item to the persistent data store. The item will be updated with a unique key, as well
 	 * the item URI will be updated to include that defined unique team.
 	 * @param virtualServer to be added
 	 * @throws IllegalArgumentException for a null argument
 	 */
 	public void add(VirtualServer virtualServer) throws IllegalArgumentException { manager.add(virtualServer); }
 	/** Update a particular object in the persistent data store
 	 * @param virtualServer the virtualServer to update
 	 * @throws NotFoundException is the object does not exist 
 	 */
 	public void update(VirtualServer virtualServer) throws NotFoundException { manager.update(virtualServer); }
 	/**
 	 * Delete item from the persistent store
 	 * @param virtualServer to be deleted
 	 */
 	public void delete(VirtualServer virtualServer) { manager.delete(virtualServer); }
 	
 	/**
 	 * Collection of resources of a particular class in the persistent store. The will be extended
 	 * in the future to return the collection of resources accessible to a particular user.
 	 * @return the collection
 	 */
 	public Collection<VirtualServer> getCollection() {return manager.getCollection();}
 	
 	public List<Key<VirtualServer>> getCollectionKeys() {
 		
 		 final List<Key<VirtualServer>> result = VirtualServerResource.dao.transact(new Work<List<Key<VirtualServer>>>() {
              @Override
              public List<Key<VirtualServer>> run() {
             	 List<Key<VirtualServer>> resultTrans = VirtualServerResource.dao.itemDaoFactory().listKeys();
                   return resultTrans;
              }
 		 });
 		return result;
 	}
 	
 	public List<VirtualServer> getByIdempotencyKey(String key) {
 		 
 		final String keyTrans = key;
 		final List<VirtualServer> result = VirtualServerResource.dao.transact(new Work<List<VirtualServer>>() {
 			@Override
             public List<VirtualServer> run() {
 				List<VirtualServer> list = new ArrayList<VirtualServer>(VirtualServerResource.dao.itemDaoFactory().collectionByProperty("idempotencyKey", keyTrans));
             	return list;
             }
 		});
 		
 		return result;
 	}
 	
 	public List<VirtualServer> getByInstanceId(String instanceId)
 	{
 		logger.info("Getting vs list by instanceId...");
 		List<VirtualServer> list = new ArrayList<VirtualServer>(VirtualServerResource.dao.itemDaoFactory().collectionByProperty("instanceId", instanceId));
 		
 		return list;
 	}
 	
 	public List<VirtualServer> getZombie() { 
 		logger.info("Getting zombie list...");		 
 		 
 		 List<VirtualServer> list = new ArrayList<VirtualServer>(VirtualServerResource.dao.itemDaoFactory().collectionByProperty("name","zombie"));		 
 	
 		logger.info("Got zombie list");
 		return list;
 		
 	}
 	
 	public final static TypedParameter inputParameters[] =  {
 		new TypedParameter("flavorRef", "Specifies the virtual machine size. Valid Values: 100 (standard.xsmall), 101 (standard.small), 102 (standard.medium), 103 (standard.large), 104 (standard.xlarge), 105 (standard.2xlarge)", ParameterType.String,"", "100"),
 		new TypedParameter("imageRef", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", "75845"),
 		new TypedParameter("key_name", "Name of the SSH key  to be used for communication with the VM", ParameterType.String, "", "hpdefault"),
 		new TypedParameter("nodeCount", "Number of instances to launch.", ParameterType.Long, "", "1"),
 		new TypedParameter("locationId", "Unique ID of hpcloud zone. Valid Values: az-1.region-a.geo-1 | az-2.region-a.geo-1 | az-3.region-a.geo-1", ParameterType.String, null, "az-1.region-a.geo-1"),
 		new TypedParameter("security_groups", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", "default"),
 		new TypedParameter("user_data", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "value", "#!/bin/bash\necho n3phele agent injection... \nset -x\n wget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ec2-user ~/agent ~/sandbox' ec2-user\n")
 	};
 	
 	
 	public final static TypedParameter outputParameters[] =  {		
 		new TypedParameter("AccessIPv4", "IPv4 public server address", ParameterType.String, "", ""),
 		new TypedParameter("AccessIPv6", "IPv6 public server address", ParameterType.String, "", ""),
 		new TypedParameter("privateIpAddress", "Specifies the private IP address that is assigned to the instance.", ParameterType.String, "", ""),
 		new TypedParameter("publicIpAddress", "Specifies the public IP address of the instance.", ParameterType.String, "", ""),
 		new TypedParameter("Class", "Class of the server object", ParameterType.String, "", ""),
 		new TypedParameter("ConfigDrive", "Drive configuration of the server", ParameterType.String, "", ""),
 		new TypedParameter("Created", "Date when the server was created", ParameterType.String, "", ""),
 		new TypedParameter("DiskConfig", "Disk config attribute from the Disk Config Extension (alias OS-DCF)", ParameterType.String, "", ""),
 		new TypedParameter("Flavor", "Standard Instance type of the server", ParameterType.String, "", ""),
 		new TypedParameter("HostId", "Host identifier, or null if in Server.Status.BUILD", ParameterType.String, "", ""),
 		new TypedParameter("Id", "Id of the server", ParameterType.String, "", ""),
 		new TypedParameter("Image", "Image of the server", ParameterType.String, "", ""),
 		new TypedParameter("KeyName", "KeyName if extension is present and there is a value for this server", ParameterType.String, "", ""),
 		new TypedParameter("Links", "The links of the id address allocated to the new server", ParameterType.List, "", ""),
 		new TypedParameter("Name", "Name of the server", ParameterType.String, "", ""),
 		new TypedParameter("TenantId", "Group id of the server", ParameterType.String, "", ""),
 		new TypedParameter("Updated", "When the server was last updated", ParameterType.String, "", ""),
 		new TypedParameter("UserId", "User id of the server", ParameterType.String, "", ""),
 		new TypedParameter("UuId", "Unique server id", ParameterType.String, "", ""),
 		new TypedParameter("costPerHour", "Cost charged per hour", ParameterType.String, "", "")
 	};
 	
 	final public static VirtualServerManager dao = new VirtualServerManager();
 }
