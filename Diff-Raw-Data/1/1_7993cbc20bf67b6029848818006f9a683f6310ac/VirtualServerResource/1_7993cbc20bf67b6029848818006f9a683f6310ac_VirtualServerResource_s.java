 /**
  * @author Nigel Cook
  *
  * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
  
  package n3phele.factory.ec2;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
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
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import n3phele.factory.model.ServiceModelDao;
 import n3phele.security.EncryptedAWSCredentials;
 import n3phele.service.core.Resource;
 import n3phele.service.model.core.AbstractManager;
 import n3phele.service.model.core.BaseEntity;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.CreateVirtualServerResponse;
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.GenericModelDao;
 import n3phele.service.core.NotFoundException;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.VirtualServer;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.ClientConfiguration;
 import com.amazonaws.Protocol;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
 import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
 import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsResult;
 import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
 import com.amazonaws.services.ec2.model.CreateKeyPairResult;
 import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
 import com.amazonaws.services.ec2.model.CreateTagsRequest;
 import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
 import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
 import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
 import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
 import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
 import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceState;
 import com.amazonaws.services.ec2.model.InstanceStateName;
 import com.amazonaws.services.ec2.model.IpPermission;
 import com.amazonaws.services.ec2.model.KeyPair;
 import com.amazonaws.services.ec2.model.LaunchSpecification;
 import com.amazonaws.services.ec2.model.Placement;
 import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
 import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.SecurityGroup;
 import com.amazonaws.services.ec2.model.SpotInstanceRequest;
 import com.amazonaws.services.ec2.model.Tag;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 import com.amazonaws.services.ec2.model.TerminateInstancesResult;
 import com.amazonaws.services.ec2.model.UserIdGroupPair;
 import com.google.apphosting.api.DeadlineExceededException;
 import com.googlecode.objectify.Key;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /** EC2 Virtual Server Resource manages the lifecycle of virtual machines on Amazon EC2 (or compatible) clouds.
  * @author Nigel Cook
  *
  */
 @Path("/")
 public class VirtualServerResource {
 	private Client client = null;
 	private final static Logger log = Logger
 			.getLogger(VirtualServerResource.class.getName());
 	
 	private final VirtualServerManager manager;
 	public VirtualServerResource() {
 		manager = new VirtualServerManager();
 	}
 
 	@Context
 	UriInfo uriInfo;
 	
 	@GET
 	@RolesAllowed("authenticated")
 	@Path("dump")
 	public String dump(@QueryParam("id") String id,
 			@QueryParam("key") String key,
 			@DefaultValue("https://ec2.amazonaws.com") @QueryParam("location") String location) {
 		log.info("Id="+id+" key="+key);
 		ClientConfiguration clientConfiguration = new ClientConfiguration();
 
 		try {
 			clientConfiguration.setProtocol(Protocol.valueOf(URI.create(location).toURL()
 					.getProtocol().toUpperCase()));
 		} catch (MalformedURLException e) {
 			throw new WebApplicationException();
 		}
 		AmazonEC2Client client = new AmazonEC2Client(new BasicAWSCredentials(id, key),
 				clientConfiguration);
 		client.setEndpoint(location.toString());
 
 		DescribeKeyPairsResult result = client.describeKeyPairs();
 		log.info("Key pairs "+ result.getKeyPairs());
 
 		return(result.getKeyPairs().size()+" key pairs ");
 	}
 
 	
 	@POST
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/accountTest")
 	public String accountTest(
 			@DefaultValue("false") @FormParam("fix") Boolean fix,
 			@FormParam("id") String id,
 			@FormParam("secret") String secret,
 			@FormParam("key") String key,
 			@FormParam("location") URI location,
 			@FormParam("email") String email,
 			@FormParam("firstName") String firstName,
 			@FormParam("lastName") String lastName,
 			@FormParam("securityGroup") String securityGroup) {
 
 		log.info("accountTest with fix " + fix);
 		if(fix && (email == null || email.trim().length() == 0) || (firstName == null || firstName.trim().length()== 0)
 				|| (lastName == null || lastName.trim().length() == 0))
 			throw new IllegalArgumentException("email details must be supplied with option to fix");
 		boolean resultKey = checkKey(key, id, secret, location);
 		if(!resultKey && fix) {
 			resultKey = createKey(key, id, secret, location, email, firstName, lastName);
 		}
 		boolean result = checkSecurityGroup(securityGroup, id, secret, location);
 		if(!result && fix) {
 			result = makeSecurityGroup(securityGroup, id, secret, location, email, firstName, lastName);
 		}
 		
 		String reply = "";
 		if(!resultKey) 
 			reply = "KeyPair "+key+" does not exist"+(fix?" and could not be created.\n":"\n");
 		if(!result) 
 			reply = "Security group "+securityGroup+" does not exist"+(fix?" and could not be created.\n":"\n");
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
 	public Collection<BaseEntity> list(
 			@DefaultValue("false") @QueryParam("summary") Boolean summary) {
 
 		log.info("get entered with summary " + summary);
 
 		Collection<BaseEntity> result = getCollection()
 				.collection(summary);
 		return result;
 	}
 	
 	/** List of input parameters supported by the factory for VM creation
 	 * 
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/inputParameters")
 	public TypedParameter[] inputParameterList() {
 
 		return inputParameters;
 	}
 	
 	/** List of output parameters supported by the factory for VM creation
 	 * 
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/outputParameters")
 	public TypedParameter[] outputParameterList() {
 
 		return outputParameters;
 	}
 	
 	
 	
 
 	/** create one or more new virtual servers. When multiple virtual servers are created, the siblings field of the virtualServer
 	 * object contains the URIs of all created virtual servers including that virtual server itself. 
 	 * @param r vm request information
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
 	public Response create(ExecutionFactoryCreateRequest r) throws Exception {
 
 		if(r.location == null) {
 			r.location = URI.create("http://ec2.amazonaws.com");
 			log.warning("Assuming default location of "+r.location);
 		}
 		
 		List<VirtualServer> result = null;
 		if(r.idempotencyKey != null && r.idempotencyKey != "") {
 			result = getByIdempotencyKey(r.idempotencyKey);
 		}
 		
 		if(result != null && result.size() > 0) {
 			log.severe("Found existing entries for idempotency key "+r.idempotencyKey);
 		} else {
 			if("zombie".equalsIgnoreCase(r.name) || "debug".equalsIgnoreCase(r.name)) {
 				r.name = r.name.toUpperCase();
 			}
 			result = createVM(r.name, r.description, r.location,
 					r.parameters, r.notification, r.accessKey, r.encryptedSecret, r.owner, r.idempotencyKey);
 		}
 		List<URI> vmRefs = new ArrayList<URI>(result.size());
 
 		log.info("Created " + result.size()+" VMs");
 		for(VirtualServer v : result) {
 			log.info("VM is "+v.getUri());
 			vmRefs.add(v.getUri());
 		}
 		return Response.created(result.get(0).getUri()).entity(new CreateVirtualServerResponse(vmRefs)).build();
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
 	public VirtualServer get(@PathParam("id") Long id)
 			throws NotFoundException {
 
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
 	public void kill(@PathParam("id") Long id,
 			@DefaultValue("false") @QueryParam("debug") boolean debug,
 			@DefaultValue("false") @QueryParam("error") boolean error) throws NotFoundException {
 		VirtualServer virtualServer = null;
 		try {
 			virtualServer = deepGet(id);
 			if(error && !debug) {
 				terminate(virtualServer);
 			} else
 				softKill(virtualServer, error);
 		} catch (Exception e) {
 			try {
 				virtualServer = get(id);
 				terminate(virtualServer);
 			} catch (Exception ee) {
 				if(virtualServer != null)
 					delete(virtualServer);
 			}
 		}
 		
 	}
 	
 	
 	@GET
 	@Produces("text/plain")
 	@Path("total")
 	public String total() {
 		String result;
 		Collection<VirtualServer> servers = getCollection();
 		result=Long.toString(servers.getTotal())+"\n";
 		result += Calendar.getInstance().getTime().toString()+"\n";
 		return result;
 	}
 	
 	/** terminate a nominated virtual server
 	 * @param virtualServer virtual server to be terminated
 	 * @param error true that the server needs to be terminated, false it is a candidate for reuse
 	 */
 	protected void terminate(VirtualServer virtualServer) {;
 		
 		try {
 			deleteInstance(virtualServer, UUID.randomUUID(), 0);
 		} catch (Exception e) {
 			manager.delete(virtualServer); 
 		}
 	}
 	
 	/** Kill a nominated virtual server, preserving as a zombie if it is suitable
 	 * @param virtualServer virtual server to be terminated
 	 * @param stop true only stop the server, false terminate the server
 	 */
 	protected void softKill(VirtualServer virtualServer, boolean error) {;
 
 		try {
 			if(!isZombieCandidate(virtualServer))
 				deleteInstance(virtualServer, UUID.randomUUID(), 0);
 			else {
 				if(error)
 					makeDebug(virtualServer, UUID.randomUUID(), 0);
 				else
 					makeZombie(virtualServer, UUID.randomUUID(), 0);
 			}
 		} catch (Exception e) {
 			manager.delete(virtualServer); 
 		}
 	}
 	
 	private void makeZombie(VirtualServer item, UUID reference, int sequence) throws Exception {
 
 		String instanceId = item.getInstanceId();
 		try {
 			AmazonEC2 client = null;
 			client = getEC2Client(item.getAccessKey(),
 					item.getEncryptedKey(), item.getLocation());
 
 			try {
 				client.createTags(new CreateTagsRequest()
 						.withResources(instanceId)
 						.withTags(new Tag("Name", "zombie"),
 								new Tag("n3phele-factory", Resource.get("factoryName", "ec2Factory")),
 								new Tag("n3phele-uri", "")));
 			} catch (Exception ex) {
 				log.log(Level.WARNING, "Cant set tag for "
 						+ instanceId
 						+ " associated with " + item.getName(), ex);
 				// throw ex; // openstack ec2 cant do this for now. Just ignore.
 			}
 			
 			// client.rebootInstances(new RebootInstancesRequest().withInstanceIds(instanceId));		
 			item.setInstanceId(null);
 			item.setZombie(true);
 			updateStatus(item, "Terminated",  reference, sequence);
 			update(item);
 			/*
 			 * Create a new zombie virtualServer object, and then set item instance Id to null.
 			 * Update item.
 			 * Update status. 
 			 */
 			VirtualServer clone = new VirtualServer("zombie", item.getDescription(), item.getLocation(),
 					item.getParameters(), null, item.getAccessKey(), item.getEncryptedKey(), item.getOwner(), item.getIdempotencyKey());
 			clone.setCreated(item.getCreated());
 			clone.setInstanceId(instanceId);
 
 
 			//
 			// The add operation does two writes in order to fix the reference URI.
 			// This creates a race condition for a fetch based on name of zombie
 			// Similarly, refresh amd update could cause a race condition. Updates semantics
 			// need to be strengthened to fail if the object is not in the store, and the check and write wrapped in
 			// a transaction.
 			//
 			add(clone);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "makeZombie delete of instanceId " + instanceId, e);
 			deleteInstance(item, UUID.randomUUID(), 0);
 			throw e;
 		}
 
 	}
 	
 	private void makeDebug(VirtualServer item, UUID reference, int sequence) throws Exception {
 
 		String instanceId = item.getInstanceId();
 		try {
 			AmazonEC2 client = null;
 			client = getEC2Client(item.getAccessKey(),
 					item.getEncryptedKey(), item.getLocation());		
 			item.setInstanceId(null);
 			item.setZombie(true);
 			updateStatus(item, "Terminated",  reference, sequence);
 			update(item);
 
 			try {
 				client.createTags(new CreateTagsRequest()
 						.withResources(instanceId)
 						.withTags(new Tag("Name", "debug"),
 								new Tag("n3phele-factory", Resource.get("factoryName", "ec2Factory")),
 								new Tag("n3phele-uri", "")));
 			} catch (Exception ex) {
 				log.log(Level.WARNING, "Cant set tag for "
 						+ instanceId
 						+ " associated with " + item.getName(), ex);
 				throw ex;
 			}
 			/*
 			 * Create a new zombie virtualServer object, and then set item instance Id to null.
 			 * Update item.
 			 * Update status. 
 			 */
 			VirtualServer clone = new VirtualServer("debug", item.getDescription(), item.getLocation(),
 					item.getParameters(), null, item.getAccessKey(), item.getEncryptedKey(), item.getOwner(), item.getIdempotencyKey());
 			clone.setCreated(item.getCreated());
 			clone.setInstanceId(instanceId);
 
 			add(clone);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "makeDebug delete of instanceId " + instanceId,
 					e);
 			item.setInstanceId(instanceId);
 			deleteInstance(item, UUID.randomUUID(), 0);
 			throw e;
 		}
 	}
 	
 	private boolean isZombieCandidate(VirtualServer virtualServer) {
 		boolean result = virtualServer != null 
 				&& virtualServer.getInstanceId() != null && virtualServer.getInstanceId().length() > 0;
 
 		if(result) {
 			if(virtualServer.getSiblings() != null && virtualServer.getSiblings().size() > 1) {
 				log.info("Server has "+virtualServer.getSiblings().size()+" siblings");
 				result = false;
 			} else {
 				if(virtualServer.getSpotId() != null && virtualServer.getSpotId().length() != 0) {
 					log.info("Server is spot instance");
 					result = false;
 				} else if(!virtualServer.getStatus().equalsIgnoreCase(InstanceStateName.Running.toString())) {
 					log.info("Server is "+virtualServer.getStatus());
 					result = false;
 				}
 			}
 		} else {
 			log.info("Null server or instanceId");
 		}
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
 	public Collection<BaseEntity> refresh() {
 		
 		long start = Calendar.getInstance().getTimeInMillis();
 
 		Collection<BaseEntity> result = refreshCollection();
 		
 		log.info(String.format("-----refresh-- %d ms processing %d items", (Calendar.getInstance().getTimeInMillis()-start), result.getTotal()));
 		return result;
 	}
 
 	protected VirtualServer deepGet(Long id) throws NotFoundException {
 		VirtualServer s = load(id);
 		updateVirtualServer(s, UUID.randomUUID(), 0);
 		return s;
 
 	}
 	
 	/** Updates virtual server object and data store state
 	 * @param item object to update
 	 * @param reference reference UUID used for notifications
 	 * @param sequence notification sequence number
 	 */
 
 	private void updateVirtualServer(VirtualServer item, UUID reference, int sequence) throws IllegalArgumentException {
 		AmazonEC2Client client = null;
 		client = getEC2Client(item.getAccessKey(), item.getEncryptedKey(),
 				item.getLocation());
 		String instanceId = item.getInstanceId();
 		boolean madeIntoZombie = item.isZombie();
 		if (!madeIntoZombie && (instanceId == null || instanceId.length() == 0)) {
 			String spotId = item.getSpotId();
 			if(spotId == null || spotId.length()==0) {
 				if("Initializing".equals(item.getStatus())) {
 					// Update has been called on an item under construction -- ignore
 					log.warning("Ignoring partially formed item "+item.getUri());
 					return;
 				} else {
 					log.severe("Improper item "+item);
 				}
 			}
 
 			DescribeSpotInstanceRequestsResult update = null;
 			try {
 				update = client.describeSpotInstanceRequests(new DescribeSpotInstanceRequestsRequest()
 						.withSpotInstanceRequestIds(spotId));
 			} catch (AmazonServiceException e) {
 				log.log(Level.WARNING, "EC2 error "+e.getErrorCode(), e);
 				throw new WebApplicationException(e, Status.BAD_REQUEST);
 			}
 			instanceId = update.getSpotInstanceRequests().get(0).getInstanceId();
 			item.setInstanceId(instanceId);
 			//
 			//	After cleaning up an object, we leave it around with status == TERMINATED
 			// for one refresh so that others can see the state chance via polling.
 			// If we come across an object with state terminated, and the underlying object
 			// is canceled or closed, then this is the second time the item has been
 			// refreshed, so now we remove the object.
 			//
 			if((instanceId == null || instanceId.length()==0) // pure spot request
 			 && item.getStatus().equalsIgnoreCase(InstanceStateName.Terminated.toString()) 
 			 && (update.getSpotInstanceRequests().get(0).getState().equalsIgnoreCase("cancelled") ||
 					 update.getSpotInstanceRequests().get(0).getState().equals("closed"))) {
 				 delete(item);
 				 return;
 			 }
 			
 			updateStatus(item, update.getSpotInstanceRequests().get(0).getState(), reference, sequence);
 			update(item);
 			if(item.getStatus().equals("cancelled")) {
 				log.warning("Spot Instance " + item.getUri()+ " cancelled .. purging");
 				try {
 					deleteInstance(item, reference, sequence); //will set object to terminated
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "Failed to clean up cancelled spot instance", e);
 					updateStatus(item, InstanceStateName.Terminated.toString(), reference, sequence);
 					delete(item);
 					return;
 				}
 
 			}
 			if(item.getStatus().equals("closed") && ((item.getInstanceId() == null) || (item.getInstanceId().length() == 0))) {
 				log.warning("Spot Instance " + item.getUri()
 						+ " not fulfilled .. purging");
 				updateStatus(item, InstanceStateName.Terminated.toString(),  reference, sequence);
 				delete(item);
 				return;
 			}
 
 		}
 		if (madeIntoZombie) {
 			if (updateStatus(item, "terminated", reference, sequence))
 				update(item);
 			if (item.getStatus().equals("terminated")) {
 				log.warning("Instance " + item.getName() + " terminated .. purging");
 				delete(item);
 				return;
 			}
 			
 		} else if (instanceId != null && instanceId.length() > 0) {
 			DescribeInstancesResult update = client
 					.describeInstances(new DescribeInstancesRequest()
 							.withInstanceIds(item.getInstanceId()));
 			if (update.getReservations() != null
 					&& update.getReservations().size() > 0) {
 				Instance ec2Instance = update.getReservations().get(0)
 						.getInstances().get(0);
 				String newStatus = ec2Instance.getState().getName();
 
 				if (!item.getStatus().equalsIgnoreCase(
 						InstanceStateName.Running.toString())
 						&& InstanceStateName.Running.toString()
 								.equalsIgnoreCase(newStatus)) {
 					item.setOutputParameters(Extractor.extract(ec2Instance));
 					try {
 						client.createTags(new CreateTagsRequest()
 								.withResources(ec2Instance.getInstanceId())
 								.withTags(new Tag("Name", item.getName()),
 										new Tag("n3phele-factory", Resource.get("factoryName", "ec2Factory")),
 										new Tag("n3phele-uri", item.getUri().toString())));
 					} catch (Exception ex) {
 						log.log(Level.WARNING, "Cant set tag for "
 								+ ec2Instance.getInstanceId()
 								+ " associated with " + item.getName(), ex);
 					}
 				}
 
 				if (updateStatus(item, newStatus, reference, sequence))
 					update(item);
 				if (item.getStatus().equals("terminated")) {
 					log.warning("Instance " + ec2Instance.getInstanceId()	+ " terminated .. purging");
 					delete(item);
 					return;
 				}
 			} else {
 				log.warning("Instance " + item.getInstanceId()+ " not found, assumed terminated .. purging");
 				updateStatus(item, InstanceStateName.Terminated.toString(), reference, sequence);
 				delete(item);
 				return;
 			}
 		}
 	}
 	
 	private void refreshVirtualServer(VirtualServer item) {
 		if(item == null)
 			return;
 		AmazonEC2Client client = getEC2Client(item.getAccessKey(), item.getEncryptedKey(),
 					item.getLocation());
 		String instanceId = item.getInstanceId();
 		if (instanceId != null && instanceId.length() > 0) {
 			DescribeInstancesResult update = client
 					.describeInstances(new DescribeInstancesRequest()
 							.withInstanceIds(item.getInstanceId()));
 			if (update.getReservations() != null
 					&& update.getReservations().size() > 0) {
 				Instance ec2Instance = update.getReservations().get(0)
 						.getInstances().get(0);
 				String newStatus = ec2Instance.getState().getName();
 				item.setStatus(newStatus);
 
 			} else {
 				log.warning("Instance " + item.getInstanceId()
 						+ " not found, assumed terminated .. ");
 				item.setStatus(InstanceStateName.Terminated.toString());
 			}
 		}
 	}
 	
 	/** Check if a zombie has expired and clean up if it has
 	 * @param s virtual server
 	 * @return TRUE if zombie
 	 */
 	private boolean checkForZombieExpiry(VirtualServer s) {
 		boolean debugInstance = s.getName().equalsIgnoreCase("debug");
 		boolean zombieInstance = s.getName().equalsIgnoreCase("zombie");
 		if(zombieInstance || debugInstance ) {
 			refreshVirtualServer(s);
 			if(s.getStatus().equalsIgnoreCase(InstanceStateName.Terminated.toString())) {
 				log.info("Found dead "+s.getName()+" with id "+s.getInstanceId()+" created "+s.getCreated());
 				manager.delete(s);
 				return true;
 			}
 			long created = s.getCreated().getTime();
 			long now = new Date().getTime();
 			long age = ((now - created)% (60*60*1000))/60000; // minutes into hourly cycle
 			if(age > 55 || s.getName().equals("Zombie") || s.getName().equals("Debug") || !s.getStatus().equalsIgnoreCase(InstanceStateName.Running.toString())) {
 				log.info("Killing "+s.getName()+" with id "+s.getInstanceId()+" created "+s.getCreated());
 				s.setName(debugInstance? "Debug" : "Zombie");
 				update(s);
 				try {
 					AmazonEC2 client = null;
 					client = getEC2Client(s.getAccessKey(),
 							s.getEncryptedKey(), s.getLocation());
 					TerminateInstancesResult result = client
 							.terminateInstances((new TerminateInstancesRequest())
 									.withInstanceIds(s.getInstanceId()));
 					log.info("Terminated "+result.getTerminatingInstances().size());
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "Failed to delete zombie", e);
 				} 
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Collection<BaseEntity> refreshCollection() {
 		Collection<VirtualServer> servers = getCollection();
 		Collection<BaseEntity> result = servers.collection(true);
 		UUID reference = UUID.randomUUID();
 		int sequence = 0;
 		try {
 			for (VirtualServer s : servers.getElements()) {
 	
 					try {
 						if(s.getUri() != null && !checkForZombieExpiry(s))
 							updateVirtualServer(s, reference, sequence++);
 					} catch (AmazonClientException ignore) {
 						log.log(Level.WARNING, s.getUri()+" refresh failed. ignoring..",ignore);
 					} catch (Exception e) {
 
 						log.log(Level.WARNING, s.getUri()+" refresh failed. Killing..",e);
 						try {
 							terminate(s);
 						} catch (Exception another) {
 							
 						} finally {
 							delete(s);
 						}
 					}
 			}
 		} catch (DeadlineExceededException deadline) {
 			return result;
 		}
 		return result;
 	}
 
 	private void deleteInstance(VirtualServer item, UUID reference, int sequence) throws Exception {
 
 		String instanceId = item.getInstanceId();
 		try {
 			String spotId = item.getSpotId();
 			boolean isSpotImage = (spotId != null) && spotId.length() != 0;
 			if(isSpotImage) {
 				try {
 					cancelSpotVMRequest(item);
 				} catch (Exception e) {
 					// ignore
 				}
 			}
 
 			if (!item.getStatus().equalsIgnoreCase("Terminated")
 					&& instanceId != null && instanceId.length() > 0) {
 				AmazonEC2 client = null;
 				client = getEC2Client(item.getAccessKey(),
 						item.getEncryptedKey(), item.getLocation());
 				TerminateInstancesResult result = client
 						.terminateInstances((new TerminateInstancesRequest())
 								.withInstanceIds(instanceId));
 				if(result.getTerminatingInstances().size()==0) { // Openstack
 					log.warning("Termination returned "+result);
 					if (updateStatus(item, "Terminated",  reference, sequence)) {
 						update(item);
 					}
 				} else {
 					InstanceState now = result.getTerminatingInstances().get(0)
 							.getCurrentState();
 					InstanceState previous = result.getTerminatingInstances()
 							.get(0).getPreviousState();
 					log.warning("Deleting instanceId " + instanceId + " from "
 							+ previous + " now " + now);
 					if (updateStatus(item, now.getName(), reference, sequence)) {
 						update(item);
 					}
 				}
 			} else {
 				if (updateStatus(item, "Terminated",  reference, sequence)) {
 					update(item);
 				}
 			}	
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Cleanup delete of instanceId " + instanceId,
 					e);
 			throw e;
 		}
 
 	}
 
 	private void cancelSpotVMRequest(VirtualServer item) throws Exception {
 		String spotId = item.getSpotId();
 		try {
 			if (spotId != null && spotId.length() > 0) {
 				AmazonEC2Client client = null;
 				client = getEC2Client(item.getAccessKey(),
 						item.getEncryptedKey(), item.getLocation());
 				try {
 					if (item.getInstanceId() == null
 							|| item.getInstanceId().length() == 0) {
 						
 						DescribeSpotInstanceRequestsResult update = client
 								.describeSpotInstanceRequests(new DescribeSpotInstanceRequestsRequest()
 										.withSpotInstanceRequestIds(spotId));
 						String instanceId = update.getSpotInstanceRequests()
 								.get(0).getInstanceId();
 						item.setInstanceId(instanceId);
 						update(item);
 					}
 				} catch (Exception e) {
 					log.log(Level.WARNING, "Spot request " + spotId
 							+ " update failed ", e);
 				}
 
 				CancelSpotInstanceRequestsRequest request = new CancelSpotInstanceRequestsRequest()
 						.withSpotInstanceRequestIds(spotId);
 				CancelSpotInstanceRequestsResult result = client
 						.cancelSpotInstanceRequests(request);
 				log.info("Cancel spot request "
 						+ spotId
 						+ " status "
 						+ result.getCancelledSpotInstanceRequests().get(0)
 								.getState().toString());
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Cleanup delete of spot instance " + spotId,
 					e);
 			throw e;
 		}
 
 	}
 
 	private List<VirtualServer> createVM(String name, String description, URI location,
 			ArrayList<NameValue> params, URI notification,
 			String accessKey, String encryptedKey, URI owner, String idempotencyKey) throws Exception {
 		ArrayList<VirtualServer> vs = new ArrayList<VirtualServer>();
 		try {
 
 			boolean spotInstance = false;
 			int minCount = 1;
 			int maxCount = 1;
 			for (NameValue p : params) {
 				if (p.getKey().equalsIgnoreCase("spotPrice")) {
 					String value = p.getValue();
 					double d = 0.0;
 					try {
 						d = Double.valueOf(value);
 					} catch (Exception e) {
 						// fall through
 					}
 					if (d > 0.0) {
 						spotInstance = true;
 					}
 				} else if(p.getKey().equalsIgnoreCase("minCount")) {
 					String value = p.getValue();
 					try {
 						minCount = Integer.valueOf(value);
 						if(minCount <= 0) minCount = 1;
 					} catch (Exception e) {
 						
 					}
 				} else if(p.getKey().equalsIgnoreCase("maxCount")) {
 					String value = p.getValue();
 					try {
 						maxCount = Integer.valueOf(value);
 						if(maxCount <=0) maxCount = 1;
 					} catch (Exception e) {
 						
 					}
 				}
 			}
 			if(minCount > maxCount) {
 				maxCount = minCount;
 			}
 
 			Date epoch = new Date();
 			for(int i=0; i < maxCount; i++) {
 				VirtualServer item = new VirtualServer(name+(maxCount>1?Integer.toString(i):""), description, location,
 						params, notification, accessKey, encryptedKey, owner, idempotencyKey);
 				item.setCreated(epoch);
 				add(item);
 				vs.add(item);
 			}
 			int n;
 			if (spotInstance) {
 				n = addSpotInstance(vs);
 			} else {
 				n = addOnDemandInstance(vs);
 			}
 			if(n < vs.size()) {
 				for(VirtualServer v : vs.subList(n, vs.size())) {
 					delete(v);
 				}
 				while(n < vs.size()) {
 					vs.remove(n);
 				}
 			}
 			ArrayList<String> siblings = new ArrayList<String>(vs.size());
 			for(VirtualServer item : vs) {
 				if(item != null)
 					siblings.add(item.getUri().toString());
 			}
 			for(VirtualServer item : vs) {
 				if(item != null) {
 					item.setSiblings(siblings);
 					update(item);
 				}
 			}
 		} catch(Exception e) {
 			for(VirtualServer item : vs) {
 				if(item != null)
 					delete(item);
 			}
 			throw e;
 		}
 		
 		return vs;
 	}
 
 	private int addSpotInstance(List<VirtualServer> items) {
 
 		RequestSpotInstancesRequest sir = new RequestSpotInstancesRequest();
 
 
 		sir.setInstanceCount(items.size());
 		HashMap<String,String> map = items.get(0).getParametersMap();
 		Injector.inject(sir, map);
 		LaunchSpecification launchSpec = new LaunchSpecification();
 		Injector.inject(launchSpec, map);
 		if(map.containsKey("availabilityZone") || map.containsKey("groupName")) {
 			Placement p = new Placement();
 			if(map.containsKey("availabilityZone")) {
 				String availabilityZone = map.get("availabilityZone");
 				if(availabilityZone != null && !availabilityZone.equals("")) {
 					p.setAvailabilityZone(map.get("availabilityZone"));
 					launchSpec.setPlacement(p);
 				}
 			}
 			if(map.containsKey("groupName")) {
 				String groupName = map.get("groupName");
 				if(groupName != null && !groupName.equals("")) {
 					p.setGroupName(map.get("groupName"));
 					launchSpec.setPlacement(p);
 				}
 			}
 			
 		}
 		sir.setLaunchSpecification(launchSpec); 
 
 		AmazonEC2Client client = getEC2Client(items.get(0).getAccessKey(),
 				items.get(0).getEncryptedKey(), items.get(0).getLocation());
 		RequestSpotInstancesResult result = null;
 		try {
 			result = client.requestSpotInstances(sir);
 		} catch (AmazonServiceException e) {
 			log.log(Level.WARNING, "EC2 error "+e.getErrorCode(), e);
 			throw new WebApplicationException(e, Status.BAD_REQUEST);
 		}
 		int i = 0;
 		for (SpotInstanceRequest spot : result.getSpotInstanceRequests()) {
 			log.warning("Spot Instance["+i+"] id " + spot.getSpotInstanceRequestId());
 			items.get(i).setSpotId(spot.getSpotInstanceRequestId());
 			i++;
 		}
 
 		return result.getSpotInstanceRequests().size();
 	}
 	
 	private boolean createWithZombie(VirtualServer item) {
 		List<VirtualServer> zombies = getZombie();
 		if(zombies != null) {
 			log.info("Got "+zombies.size()+" Zombies ");
 			zombieCheck: for(VirtualServer s : zombies) {
 				boolean locationMatch = s.getLocation().equals(item.getLocation());
 				boolean accessMatch = s.getAccessKey().equals(item.getAccessKey());
 				boolean secretMatch = s.getEncryptedKey().equals(item.getEncryptedKey());
 				log.info(" Zombie "+s.getInstanceId()+" location "+locationMatch+
 						" access "+accessMatch+" secret "+secretMatch);
 				if(locationMatch && accessMatch && secretMatch) {
 					Map<String,String> map = s.getParametersMap();
 					for(NameValue x : item.getParameters()) {
 						if(!SafeEquals(x.getValue(), map.get(x.getKey()))) {
 							log.info("Mismatch on "+x.getKey()+" need "+x.getValue()+" zombie "+map.get(x.getKey()));
 							continue zombieCheck;
 						}
 					}
 					// zombie matches
 
 					GenericModelDao<VirtualServer> itemDaoTxn = null;
 					boolean claimed = false;
 					try {
 						itemDaoTxn = manager.itemDaoFactory(true);
 						VirtualServer zombie = itemDaoTxn.get(s.getId());
 							zombie.setIdempotencyKey(new Date().toString());
 							itemDaoTxn.put(zombie);
 							itemDaoTxn.delete(zombie);
 							itemDaoTxn.ofy().getTxn().commit();
 							claimed = true;
 					} catch (Exception e) {
 						log.log(Level.WARNING, "Zombie processing contention", e);
 					} finally {
 						if(itemDaoTxn.ofy().getTxn().isActive())
 							itemDaoTxn.ofy().getTxn().rollback();
 					}
 					if(claimed) {
 //						List<VirtualServer> leftOverZombies = getZombie();
 //						if(leftOverZombies != null) {
 //							log.info("Got "+leftOverZombies.size()+" zombies remaining");
 //						} else {
 //							log.info("Got 0 Zombies remaining");
 //						}
 						log.info("Claimed "+s.getInstanceId());
 						refreshVirtualServer(s);
 						if(!s.getStatus().equalsIgnoreCase(InstanceStateName.Running.toString())) {
 							terminate(s);
 							continue;
 						}
 						item.setInstanceId(s.getInstanceId());
 						item.setCreated(s.getCreated());
 						updateVirtualServer(item, UUID.randomUUID(), 0);
 						if(item.getStatus().equalsIgnoreCase(InstanceStateName.Running.toString())){
 							return true;
 						} else {
 							continue;	
 						}
 					} else {
 						log.warning("Zombie contention on "+s.getInstanceId());
 					}
 				}
 			}
 		}
 		
 		return false;
 	
 	}
 	
 	private boolean SafeEquals(String a, String b) {
 		if((a == null || a.length()==0) && (b==null || b.length()==0)) return true;
 		if(a == null || b == null) return false;
 		return(a.equals(b));
 	}
 
 	private int addOnDemandInstance(List<VirtualServer> items) {
 
 		RunInstancesRequest vs = new RunInstancesRequest();
 
 		vs.setMinCount(items.size());
 		vs.setMaxCount(items.size());
 		String token = items.get(0).getIdempotencyKey();
 	
 		if(token != null && token.length() > 64) {
 			token = token.substring(token.length()-64);
 		}
 		vs.setClientToken(token);
 		HashMap<String,String> map = items.get(0).getParametersMap();
 		Injector.inject(vs, map);
 		if(map.containsKey("availabilityZone") || map.containsKey("groupName")) {
 			Placement p = new Placement();
 			if(map.containsKey("availabilityZone")) {
 				String availabilityZone = map.get("availabilityZone");
 				if(availabilityZone != null && !availabilityZone.equals("")) {
 					p.setAvailabilityZone(map.get("availabilityZone"));
 					vs.setPlacement(p);
 				}
 			}
 			if(map.containsKey("groupName")) {
 				String groupName = map.get("groupName");
 				if(groupName != null && !groupName.equals("")) {
 					p.setGroupName(map.get("groupName"));
 					vs.setPlacement(p);
 				}
 			}
 			
 		}
 		if(items.size() == 1 && createWithZombie(items.get(0))) {
 			return 1; 
 		}
 		AmazonEC2Client client = getEC2Client(items.get(0).getAccessKey(),
 				items.get(0).getEncryptedKey(), items.get(0).getLocation());
 		RunInstancesResult result=null;
 		try {
 			result = client.runInstances(vs);
 		} catch (AmazonServiceException e) {
 			log.log(Level.WARNING, "EC2 error "+e.getErrorCode(), e);
 			throw new WebApplicationException(e, Status.BAD_REQUEST);
 		} catch (AmazonClientException e) {
 			log.log(Level.SEVERE, "EC2 AmazonClientException", e);
 			log.severe("Check for orphaned VMs");
 			try {
 				result = client.runInstances(vs);
 			} catch (Exception e2) {
 				log.log(Level.SEVERE, "EC2 AmazonClientException", e2);
 				throw new WebApplicationException(e, Status.BAD_REQUEST);
 			}
 		}
 		int i = 0;
 		for(Instance ec2Instance : result.getReservation().getInstances()) {
 			log.info("Create VM["+i+"] has Instance id " + ec2Instance.getInstanceId());
 			items.get(i).setInstanceId(ec2Instance.getInstanceId());
 			i++;
 		}
 
 		return result.getReservation().getInstances().size();
 	}
 
 	protected boolean updateStatus(VirtualServer s, String newStatus, UUID reference, int sequence) {
 		String oldStatus = s.getStatus();
 		newStatus = newStatus.toLowerCase();
 		if (oldStatus.equals(newStatus))
 			return false;
 		s.setStatus(newStatus);
 		try {
 			sendNotification(s, oldStatus.toLowerCase(), newStatus, reference.toString(), sequence);
 		} catch (Exception e) {
 			log.log(Level.INFO, "SendNotification exception to <"+s.getNotification()+"> from "+s.getUri()+" old: "+oldStatus+" new: "+s.getStatus(), e);
 			if(oldStatus.equals(newStatus.toUpperCase())) {
 				log.warning("Cancelling SendNotification to <"+s.getNotification()+"> from "+s.getUri()+" old: "+oldStatus+" new: "+s.getStatus());
 			} else {
 				s.setStatus(newStatus.toUpperCase());
 			}
 		}
 		return true;
 	}
 
 
 	private void sendNotification(VirtualServer s, String oldStatus,
 			String newStatus, String reference, int sequence) throws Exception {
 		URI notification = s.getNotification();
 		log.info("SendNotification to <"+notification+"> from "+s.getUri()+" old: "+oldStatus+" new: "+s.getStatus());
 
 		if(notification == null)
 			return;
 		
 		if(client == null) { 
 			client = Client.create();
 		}
 		WebResource resource = client.resource(s.getNotification());
 
 		ClientResponse response = resource.queryParam("source", s.getUri().toString())
 											.queryParam("oldStatus", oldStatus)
 											.queryParam("newStatus", newStatus)
 											.queryParam("reference", reference)
 											.queryParam("sequence", Integer.toString(sequence))
 											.type(MediaType.TEXT_PLAIN)
 											.get(ClientResponse.class);
 		log.info("Notificaion status "+response.getStatus());
 		if(response.getStatus() == 410) {
 			log.severe("VM GONE .. killing "+s.getUri()+" silencing reporting to "+s.getNotification());
 			s.setNotification(null);
 			deleteInstance(s, UUID.randomUUID(), 0);
 		}
 	}
 
 	private AmazonEC2Client getEC2Client(String accessKey,
 			String encryptedKey, URI location) {
 		AWSCredentials credentials = null;
 		try {
 			credentials = new EncryptedAWSCredentials(accessKey, encryptedKey);
 		} catch (UnsupportedEncodingException e) {
 			throw new WebApplicationException();
 		} catch (NoSuchAlgorithmException e) {
 			throw new WebApplicationException();
 		} catch (Exception e) {
 			throw new WebApplicationException();
 		}
 		ClientConfiguration clientConfiguration = new ClientConfiguration();
 
 		try {
 			clientConfiguration.setProtocol(Protocol.valueOf(location.toURL()
 					.getProtocol().toUpperCase()));
 		} catch (MalformedURLException e) {
 			throw new WebApplicationException();
 		}
 		AmazonEC2Client client = new AmazonEC2Client(credentials,
 				clientConfiguration);
 		client.setEndpoint(location.toString());
 		return client;
 	}
 
 	private boolean checkKey(String key, String id, String secret, URI location) {
 		AmazonEC2Client client = null;
 		client = getEC2Client(id, secret, location);
 		boolean found = true;
 		try {
 			DescribeKeyPairsResult response = client.describeKeyPairs(new DescribeKeyPairsRequest().withKeyNames("n3phele-"+key));
 			if(response.getKeyPairs() == null || response.getKeyPairs().isEmpty()) {
 				log.warning("No key pairs found");
 				found = false;
 			} else {
 				log.warning("Found "+response.getKeyPairs().size()+" "+response.getKeyPairs().toString());
 			}
 		} catch (Exception e) {
 			log.severe("Check security group exception "+e.getMessage());
 			found = false;
 		}
 		return found;
 	}
 	
 	private boolean createKey(String key, String id, String secret, URI location, String email, String firstName, String lastName) {
 		AmazonEC2Client client = null;
 		client = getEC2Client(id, secret, location);
 		boolean found = true;
 		try {
 			CreateKeyPairResult response = client.createKeyPair(new CreateKeyPairRequest().withKeyName("n3phele-"+key));
 			log.warning("Got "+response.getKeyPair().toString());
 			sendNotificationEmail(response.getKeyPair(), email, firstName, lastName, location);
 		} catch (Exception e) {
 			log.severe("Create key pair exception "+e.getMessage());
 			found = false;
 		}
 		return found;
 	}
 	
 	public void sendNotificationEmail(KeyPair keyPair, String to, String firstName, String lastName, URI location) {
 			try {
 				StringBuilder subject = new StringBuilder();
 				StringBuilder body = new StringBuilder();
 					subject.append("Auto-generated keyPair \"");
 					subject.append(keyPair.getKeyName());
 					subject.append("\"");
 					body.append(firstName);
 					body.append(",\n\nA keypair named \"");
 					body.append(keyPair.getKeyName());
 					body.append("\" has been generated for you. \n\n");
 					body.append("Please keep this information secure as it allows access to the virtual machines");
 					body.append(" run on your behalf by n3phele on the cloud at ");
 					body.append(location.toString());
 					body.append(". To access the machines using ssh copy all of the lines");
 					body.append(" including -----BEGIN RSA PRIVATE KEY----- and -----END RSA PRIVATE KEY-----");
 					body.append(" into a file named ");
 					body.append(keyPair.getKeyName());
 					body.append(".pem\n\n");
 					body.append(keyPair.getKeyMaterial());
 					body.append("\n\nn3phele\n--\nhttps://n3phele.appspot.com\n\n");
 					
 					Properties props = new Properties();
 					Session session = Session.getDefaultInstance(props, null);
 
 					Message msg = new MimeMessage(session);
 					msg.setFrom(new InternetAddress("n3phele@gmail.com", "n3phele"));
 					msg.addRecipient(Message.RecipientType.TO,
 							new InternetAddress(to, firstName
 									+ " " + lastName));
 					msg.setSubject(subject.toString());
 					msg.setText(body.toString());
 					Transport.send(msg);
 
 			} catch (AddressException e) {
 				log.log(Level.SEVERE,
 						"Email to " + to, e);
 			} catch (MessagingException e) {
 				log.log(Level.SEVERE,
 						"Email to " + to, e);
 			} catch (UnsupportedEncodingException e) {
 				log.log(Level.SEVERE,
 						"Email to " + to, e);
 			} catch (Exception e) {
 				log.log(Level.SEVERE,
 						"Email to " + to, e);
 			}
 	}
 	
 	private boolean checkSecurityGroup(String groupName, String id, String secret, URI location) {
 		AmazonEC2Client client = null;
 		client = getEC2Client(id, secret, location);
 		boolean found = true;
 		try {
 			DescribeSecurityGroupsResult response = client.describeSecurityGroups(new DescribeSecurityGroupsRequest().withGroupNames("n3phele-"+groupName));
 			if(response.getSecurityGroups() == null || response.getSecurityGroups().isEmpty()) {
 				log.warning("No groups found");
 				found = false;
 			} else {
 				log.warning("Found "+response.getSecurityGroups().size()+" "+response.getSecurityGroups().toString());
 			}
 		} catch (Exception e) {
 			log.severe("Check security group exception "+e.getMessage());
 			found = false;
 		}
 
 		return found;
 	}
 	
 	public void sendSecurityGroupNotificationEmail(String securityGroup, String to, String firstName, String lastName, URI location) {
 		try {
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
 				msg.addRecipient(Message.RecipientType.TO,
 						new InternetAddress(to, firstName
 								+ " " + lastName));
 				msg.setSubject(subject.toString());
 				msg.setText(body.toString());
 				Transport.send(msg);
 
 		} catch (AddressException e) {
 			log.log(Level.SEVERE,
 					"Email to " + to, e);
 		} catch (MessagingException e) {
 			log.log(Level.SEVERE,
 					"Email to " + to, e);
 		} catch (UnsupportedEncodingException e) {
 			log.log(Level.SEVERE,
 					"Email to " + to, e);
 		} catch (Exception e) {
 			log.log(Level.SEVERE,
 					"Email to " + to, e);
 		}
 
 
 }
 	
 //	private boolean makeSecurityGroup(String groupName, String id, String secret, URI location, String to, String firstName, String lastName) {
 //		AmazonEC2Client client = null;
 //		client = getEC2Client(id, secret, location);
 //		boolean found = true;
 //		try {
 //		client.createSecurityGroup(new CreateSecurityGroupRequest()
 //		.withGroupName("n3phele-"+groupName)
 //		.withDescription("n3phele "+groupName+" security group"));
 //		
 //		String ownerId = null;
 //		DescribeSecurityGroupsResult newGroupResult = client.describeSecurityGroups();
 //		for(SecurityGroup g : newGroupResult.getSecurityGroups()) {
 //			if(g.getGroupName().equals("n3phele-"+groupName)) {
 //				ownerId = g.getOwnerId();
 //			}
 //		}
 //		if(ownerId == null) return false;
 //		log.info("found ownerId of "+ownerId);
 //
 //		List<IpPermission> permissions = new ArrayList<IpPermission>();
 //
 //		
 //		UserIdGroupPair userIdGroupPairs = new UserIdGroupPair()
 //		.withUserId(ownerId)
 //		.withGroupName("n3phele-"+groupName);
 //		
 //		permissions.add(new IpPermission()
 //		.withIpProtocol("icmp")
 //		.withFromPort(-1)
 //		.withToPort(-1)
 //		.withUserIdGroupPairs(userIdGroupPairs));
 //		
 //		permissions.add(new IpPermission()
 //		.withIpProtocol("tcp")
 //		.withFromPort(1)
 //		.withToPort(65535)
 //		.withUserIdGroupPairs(userIdGroupPairs));
 //		
 //		permissions.add(new IpPermission()
 //		.withIpProtocol("udp")
 //		.withFromPort(1)
 //		.withToPort(65535)
 //		.withUserIdGroupPairs(userIdGroupPairs));
 //		
 //		permissions.add(new IpPermission()
 //		.withIpProtocol("tcp")
 //		.withFromPort(22)
 //		.withToPort(22)
 //		.withIpRanges("0.0.0.0/0"));
 //		
 //		permissions.add(new IpPermission()
 //		.withIpProtocol("tcp")
 //		.withFromPort(8887)
 //		.withToPort(8887)
 //		.withIpRanges("0.0.0.0/0"));
 //		
 //		
 //		client.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(
 //				"n3phele-"+groupName,
 //				permissions));
 //		sendSecurityGroupNotificationEmail("n3phele-"+groupName, to, firstName, lastName, location);
 //		} catch (Exception e) {
 //			log.log(Level.SEVERE, "Create security group "+groupName, e);
 //			found = false;
 //		}
 //		
 //		
 //		return found;
 //	}
 	private boolean makeSecurityGroup(String groupName, String id, String secret, URI location, String to, String firstName, String lastName) {
 		AmazonEC2Client client = null;
 		client = getEC2Client(id, secret, location);
 		boolean found = true;
 		boolean failed = false;
 		try {
 			client.createSecurityGroup(new CreateSecurityGroupRequest()
 			.withGroupName("n3phele-"+groupName)
 			.withDescription("n3phele "+groupName+" security group"));
 			
 			String ownerId = null;
 			DescribeSecurityGroupsResult newGroupResult = client.describeSecurityGroups();
 			for(SecurityGroup g : newGroupResult.getSecurityGroups()) {
 				if(g.getGroupName().equals("n3phele-"+groupName)) {
 					ownerId = g.getOwnerId();
 				}
 			}
 			if(ownerId == null) return false;
 			log.info("found ownerId of "+ownerId);
 			
 			log.info("adding ssh ports");
 			try {
 			client.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest()
 			.withGroupName("n3phele-"+groupName)
 			.withCidrIp("0.0.0.0/0")
 			.withIpProtocol("tcp")
 			.withFromPort(22)
 			.withToPort(22));
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Create security group "+groupName, e);
 				failed = true;
 			}
 			
 			log.info("adding agent ports");
 			try {
 			client.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest()
 			.withGroupName("n3phele-"+groupName)
 			.withCidrIp("0.0.0.0/0")
 			.withIpProtocol("tcp")
 			.withFromPort(8887)
 			.withToPort(8887));
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Create security group "+groupName, e);
 				failed = true;
 			}
 	
 			if(!failed) {
 				log.info("adding self access");
 	
 				try {
 					List<IpPermission> permissions = new ArrayList<IpPermission>();
 
 					
 					UserIdGroupPair userIdGroupPairs = new UserIdGroupPair()
 					.withUserId(ownerId)
 					.withGroupName("n3phele-"+groupName);
 					
 					permissions.add(new IpPermission()
 					.withIpProtocol("icmp")
 					.withFromPort(-1)
 					.withToPort(-1)
 					.withUserIdGroupPairs(userIdGroupPairs));
 					
 					permissions.add(new IpPermission()
 					.withIpProtocol("tcp")
 					.withFromPort(1)
 					.withToPort(65535)
 					.withUserIdGroupPairs(userIdGroupPairs));
 					
 					permissions.add(new IpPermission()
 					.withIpProtocol("udp")
 					.withFromPort(1)
 					.withToPort(65535)
 					.withUserIdGroupPairs(userIdGroupPairs));
 					
 					log.info("adding icmp/tcp/udp");
 					
 					client.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(
 							"n3phele-"+groupName,
 							permissions));
 				} catch (Exception e) {
 					log.log(Level.WARNING, "Error adding self access to group "+groupName, e);
 				}
 			}
 
 			
 			if(failed) {
 				client.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupName("n3phele-"+groupName));
 				found = false;
 			} else {
 				sendSecurityGroupNotificationEmail("n3phele-"+groupName, to, firstName, lastName, location);
 			}
 			
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Create security group "+groupName, e);
 			client.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupName("n3phele-"+groupName));
 			found = false;
 		}
 		return found;
 	}
 
 	private static class VirtualServerManager extends AbstractManager<VirtualServer> {
 		
 		@Override
 		protected URI myPath() {
 			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8889/resources")).path("virtualServer").build();
 		}
 
 		@Override
 		protected GenericModelDao<VirtualServer> itemDaoFactory(boolean transactional) {
 			return new ServiceModelDao<VirtualServer>(VirtualServer.class, transactional);
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
 	 */
 	public VirtualServer load(String name) throws NotFoundException { return manager.get(name); }
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
 
 		List<Key<VirtualServer>> result = null;
 		try { 
 			result = manager.itemDao().ofy().query(manager.itemDao().clazz).listKeys();
 		} catch (NotFoundException e) {
 		}
 		return result;
 
 	}
 	
 	public List<VirtualServer> getByIdempotencyKey(String key) { return manager.itemDao().ofy().query(VirtualServer.class).filter("idempotencyKey", key).list(); }
 	
 	public List<VirtualServer> getZombie() { return manager.itemDao().ofy().query(VirtualServer.class).filter("name", "zombie").list(); }
 
 	/*
 	 * Factory parameters
 	 */
 	
 	public final static TypedParameter inputParameters[] =  {
 		new TypedParameter("availabilityZone", "Specifies the placement constraints (Availability Zones) for launching the instances.", ParameterType.String, "", ""),
 		new TypedParameter("groupName", "Specifies the name of a placement group. Cannot be used for spot instances", ParameterType.String, "", ""),
 		new TypedParameter("availabilityZoneGroup", "If you specify the same Availability Zone group for all Spot Instance requests, all Spot Instances are launched in the same Availability Zone.", ParameterType.String, "", ""),
 		new TypedParameter("launchGroup", "The instance launch group. Launch groups are Spot Instances that launch together and terminate together", ParameterType.String, "", ""),
 		new TypedParameter("instanceInitiatedShutdownBehavior", "If an instance shutdown is initiated, this determines whether the instance stops or terminates. Valid Values: stop | terminate", ParameterType.String, "", ""),
 		new TypedParameter("instanceType", "specifies virtual machine size. Valid Values: t1.micro | m1.small | m1.large | m1.xlarge | m2.xlarge | m2.2xlarge | m2.4xlarge | c1.medium | c1.xlarge", ParameterType.String, "", ""),
 		new TypedParameter("imageId", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", ""),
 		new TypedParameter("keyName", "Name of the SSH key to be used for communication with the VM", ParameterType.String, "", ""),
 		new TypedParameter("minCount", "Minimum number of instances to launch. If the value is more than Amazon EC2 can launch, no instances are launched at all.", ParameterType.Long, "", "1"),
 		new TypedParameter("maxCount", "Maximum number of instances to launch. If the value is more than Amazon EC2 can launch, the largest possible number above minCount will be launched instead.", ParameterType.Long, "", "1"),
 		new TypedParameter("monitoring", "Specifies whether monitoring is enabled for the instance.", ParameterType.Boolean, "", "false"),
 		new TypedParameter("securityGroups", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", ""),
 		new TypedParameter("spotPrice", "Maximum hourly price for instance. If not specified then an on-demand instance is used", ParameterType.Double, "", ""),
 		new TypedParameter("userData", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "value", "default")
 	};
 	
 	public final static TypedParameter outputParameters[] =  {
 		new TypedParameter("amiLaunchIndex", "The AMI launch index, which can be used to find this instance within the launch group.", ParameterType.String, "", ""),
 		new TypedParameter("architecture", "The architecture of the image.", ParameterType.String, "", ""),
 		new TypedParameter("dnsName", "The public DNS name assigned to the instance. This DNS name is contactable from outside the Amazon EC2 network. This element remains empty until the instance enters a running state. ", ParameterType.String, "", ""),
 		new TypedParameter("imageId", "Specifies the name of a placement group.", ParameterType.String, "", ""),
 		new TypedParameter("instanceInitiatedShutdownBehavior", "If an instance shutdown is initiated, this determines whether the instance stops or terminates. Valid Values: stop | terminate", ParameterType.String, "", ""),
 		new TypedParameter("instanceType", "specifies virtual machine size. Valid Values: t1.micro | m1.small | m1.large | m1.xlarge | m2.xlarge | m2.2xlarge | m2.4xlarge | c1.medium | c1.xlarge", ParameterType.Long, "", ""),
 		new TypedParameter("imageId", "Image ID of the AMI used to launch the instance.", ParameterType.String, "", ""),
 		new TypedParameter("instanceId", "Unique ID of the instance launched.", ParameterType.String, "", ""),
 		new TypedParameter("instanceLifecycle", "Specifies whether this is a Spot Instance.", ParameterType.String, "", ""),
 		new TypedParameter("instanceState", "State of the instance. code: A 16-bit unsigned integer. The high byte is an opaque internal value and should be ignored. The low byte is set based on the state represented. Valid Values: 0 (pending) | 16 (running) | 32 (shutting-down) | 48 (terminated) | 64 (stopping) | 80 (stopped). name: Valid Values: pending | running | shutting-down | terminated | stopping | stopped ", ParameterType.String, "", ""),
 		new TypedParameter("instanceType", "specifies virtual machine size. Valid Values: t1.micro | m1.small | m1.large | m1.xlarge | m2.xlarge | m2.2xlarge | m2.4xlarge | c1.medium | c1.xlarge", ParameterType.String, "", ""),
 		new TypedParameter("publicIpAddress", "Specifies the public IP address of the instance.", ParameterType.String, "", ""),
 		new TypedParameter("kernelId", "Kernel associated with this instance.", ParameterType.String, "", ""),
 		new TypedParameter("keyName", "Name of the SSH key to be used for communication with the VM", ParameterType.String, "", ""),
 		new TypedParameter("launchTime", "The time the instance launched", ParameterType.String, "", ""),
 		new TypedParameter("monitoring", "Specifies whether monitoring is enabled for the instance. state: true | false", ParameterType.String, "", ""),
 		new TypedParameter("placement", "The location where the instance launched. availabilityZone: Availability Zone of the instance", ParameterType.String, "", ""),
 		new TypedParameter("platform", "The Platform of the instance (e.g., Windows).", ParameterType.String, "", ""),
 		new TypedParameter("privateDnsName", "The private DNS name assigned to the instance. This DNS name can only be used inside the Amazon EC2 network. This element remains empty until the instance enters a running state.", ParameterType.String, "", ""),
 		new TypedParameter("privateIpAddress", "Specifies the private IP address that is assigned to the instance.", ParameterType.String, "", ""),
 		new TypedParameter("productCodes", "Product codes attached to this instance.", ParameterType.String, "", ""),
 		new TypedParameter("ramdiskId", "RAM disk associated with this instance.", ParameterType.String, "", ""),
 		new TypedParameter("reason", "Reason for the most recent state transition. This might be an empty string.", ParameterType.String, "", ""),
 		new TypedParameter("rootDeviceName", "The root device name (e.g., /dev/sda1).", ParameterType.String, "", ""),
 		new TypedParameter("rootDeviceType", "The root device type used by the AMI. The AMI can use an Amazon EBS or instance store root device.", ParameterType.String, "", ""),
 		new TypedParameter("spotInstanceRequestId", "The ID of the Spot Instance request.", ParameterType.String, "", ""),
 		new TypedParameter("stateReason", "The reason for the state change. code: Reason code for the state change message: Message for the state change", ParameterType.String, "", ""),
 		new TypedParameter("subnetId", "Specifies the Amazon VPC subnet ID in which the instance is running.", ParameterType.String, "", ""),
 		new TypedParameter("virtualizationType", "Specifies the instance's virtualization type (paravirtual or hvm).", ParameterType.String, "", ""),
 		new TypedParameter("vpcId", "Specifies the Amazon VPC in which the instance is running.", ParameterType.String, "", "")
 	};
 }
