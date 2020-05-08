 package n3phele.factory.test.units;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.ws.rs.core.Response;
 
 import n3phele.factory.hpcloud.HPCloudCreateServerRequest;
 import n3phele.factory.hpcloud.HPCloudManager;
 import n3phele.factory.rest.impl.VirtualServerResource;
 import n3phele.factory.strategy.DebugStrategy;
 import n3phele.factory.strategy.ZombieStrategy;
 import n3phele.service.core.NotFoundException;
 import n3phele.service.model.core.BaseEntity;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.CreateVirtualServerResponse;
 import n3phele.service.model.core.ExecutionFactoryAssimilateRequest;
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.VirtualServer;
 import n3phele.service.model.core.VirtualServerStatus;
 
 import org.jclouds.compute.domain.Hardware;
 import org.jclouds.compute.domain.OperatingSystem;
 import org.jclouds.compute.domain.internal.NodeMetadataImpl;
 import org.jclouds.domain.Location;
 import org.jclouds.domain.LoginCredentials;
 import org.jclouds.openstack.nova.v2_0.domain.Address;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
 import org.jclouds.openstack.nova.v2_0.domain.ServerExtendedAttributes;
 import org.jclouds.openstack.nova.v2_0.domain.ServerExtendedStatus;
 import org.jclouds.openstack.v2_0.domain.Link;
 import org.jclouds.openstack.v2_0.domain.Resource;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.reflect.Whitebox;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 public class VirtualServerResourceTest
 {
 
 	private final LocalServiceTestHelper	helper	= new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalTaskQueueTestConfig().setDisableAutoTaskExecution(false).setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class));
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception
 	{
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception
 	{
 		helper.setUp();
 	}
 
 	@After
 	public void tearDown()
 	{
 		helper.tearDown();
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void accountTestThrowExceptionWhenEmptyParametersWithFix()
 	{
 		VirtualServerResource resource = new VirtualServerResource();
 
 		URI uri = null;
 		try
 		{
 			uri = new URI("http://localhost/");
 		}
 		catch (URISyntaxException e)
 		{
 			fail();
 		}
 
 		resource.accountTest(true, "1", "", "", uri, "", "", "", "", "");
 		fail("exception was expected");
 	}
 
 	@Test
 	public void accountTestOnCheckedKeyAndGroup()
 	{
 		// Create test resources mocking calls to remote API
 		VirtualServerResource resource = new VirtualServerResource() {
 			@Override
 			protected boolean checkSecurityGroup(String groupName, String id, String secret, URI location, String locationId)
 			{
 				return true;
 			}
 
 			@Override
 			protected boolean checkKey(String key, String id, String secret, URI location, String locationId)
 			{
 				return true;
 			}
 		};
 
 		URI uri = null;
 		try
 		{
 			uri = new URI("http://localhost/");
 		}
 		catch (URISyntaxException e)
 		{
 			fail();
 		}
 
 		String result = resource.accountTest(true, "1", "secret", "key", uri, "locationId", "email", "firstName", "lastName", "securityGroup");
 		assertEquals("", result);
 	}
 
 	@Test
 	public void accountTestOnBadKeyAndGroup()
 	{
 		// Create test resources mocking calls to remote API
 
 		VirtualServerResource resource = new VirtualServerResource() {
 
 			@Override
 			protected boolean checkSecurityGroup(String groupName, String id, String secret, URI location, String locationId)
 			{
 				return false;
 			}
 
 			@Override
 			protected boolean checkKey(String key, String id, String secret, URI location, String locationId)
 			{
 				return false;
 			}
 
 			// FIXME how to verify if this function was really called?
 			@Override
 			protected boolean makeSecurityGroup(String groupName, String id, String secret, URI location, String to, String firstName, String lastName, String locationId)
 			{
 				return true;
 			}
 
 			@Override
 			protected boolean createKey(String key, String id, String secret, URI location, String email, String firstName, String lastName, String locationId)
 			{
 				return true;
 			}
 
 		};
 
 		URI uri = null;
 		try
 		{
 			uri = new URI("http://localhost/");
 		}
 		catch (URISyntaxException e)
 		{
 			fail();
 		}
 
 		String result = resource.accountTest(true, "1", "secret", "key", uri, "locationId", "email", "firstName", "lastName", "securityGroup");
 		assertEquals("", result);
 	}
 
 	@Test
 	public void virtualServerListTest()
 	{
 
 		VirtualServerTestDAO manager = new VirtualServerTestDAO();
 
 		// Add a virtual server object to database
 		VirtualServer vs = Utils.createFakeDataVirtualServer();
 		manager.add(vs);
 
 		// Test list method return from resource
 		VirtualServerResource resource = new VirtualServerResource();
 		Collection<BaseEntity> collection = resource.list(false);
 
 		// Verify if returned the virtual server from database
 		assertEquals(1, collection.getElements().size());
 	}
 
 	@Test
 	public void virtualServerGetTest()
 	{
 		VirtualServerTestDAO manager = new VirtualServerTestDAO();
 
 		// Add a virtual server object to database
 		VirtualServer vs = Utils.createFakeDataVirtualServer();
 		long id = 1111l;
 		vs.setId(id);
 		manager.add(vs);
 
 		// Test get method return from resource
 		VirtualServerResource resource = new VirtualServerResource() {
 			// Do nothing when trying to update reference throw remote call
 			@Override
 			protected void updateVirtualServer(VirtualServer item, HPCloudManager hpCloudManager)
 					throws IllegalArgumentException
 			{
 
 			}
 		};
 		VirtualServer virtualServer = resource.get(id);
 
 		// Verify if returned the virtual server from database
 		assertEquals(vs.getId(), virtualServer.getId());
 		assertEquals(vs.getLocation(), virtualServer.getLocation());
 		assertEquals(vs.getAccessKey(), virtualServer.getAccessKey());
 		assertEquals(vs.getEncryptedKey(), virtualServer.getEncryptedKey());
 		assertEquals(vs.getInstanceId(), virtualServer.getInstanceId());
 	}
 
 	@Test(expected = NotFoundException.class)
 	public void virtualServerKillCallsSoftKillVMWhenNoErrorTest()
 	{
 
 		final VirtualServerTestDAO manager = new VirtualServerTestDAO();
 
 		// Add a virtual server object to database
 		VirtualServer vs = Utils.createFakeDataVirtualServer();
 		long id = 1111l;
 		vs.setId(id);
 		manager.add(vs);
 
 		VirtualServerResource resource = new VirtualServerResource() {
 			// Do nothing when trying to update reference throw remote call
 			@Override
 			protected void updateVirtualServer(VirtualServer item, HPCloudManager hpCloudManager)
 					throws IllegalArgumentException
 			{
 
 			}
 
 			@Override
 			protected void softKill(VirtualServer virtualServer, boolean error)
 			{
 				manager.delete(virtualServer);
 			}
 		};
 
 		// Send error as false
 		resource.kill(vs.getId(), false, false);
 
 		// If virtual server was deleted, this method throws an exception
 		VirtualServer virtualServer = manager.get(vs.getId());
 	}
 
 	@Test(expected = NotFoundException.class)
 	public void virtualServerKillCallsTerminateVMWhenErrorTest()
 	{
 
 		final VirtualServerTestDAO manager = new VirtualServerTestDAO();
 
 		// Add a virtual server object to database
 		VirtualServer vs = Utils.createFakeDataVirtualServer();
 		long id = 1111l;
 		vs.setId(id);
 		manager.add(vs);
 
 		VirtualServerResource resource = new VirtualServerResource() {
 			// Do nothing when trying to update reference throw remote call
 			@Override
 			protected void updateVirtualServer(VirtualServer item, HPCloudManager hpCloudManager)
 					throws IllegalArgumentException
 			{
 
 			}
 
 			@Override
 			protected void terminate(VirtualServer virtualServer)
 			{
 				manager.delete(virtualServer);
 			}
 		};
 
 		// Send error as true
 		resource.kill(vs.getId(), false, true);
 
 		// If virtual server was deleted, this method throws an exception
 		VirtualServer virtualServer = manager.get(vs.getId());
 	}
 
 	@Test
 	public void createWithZombieTest() throws Exception
 	{
 		final VirtualServerTestDAO manager = new VirtualServerTestDAO();
 		final List<VirtualServer> list = new ArrayList<VirtualServer>();
 
 		VirtualServer vs1 = new VirtualServer("zombie", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		VirtualServer vs2 = new VirtualServer("zombie", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		VirtualServer vs3 = new VirtualServer("zombie", "desc", new URI("http://something.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs1.setStatus(VirtualServerStatus.running);
 		vs2.setStatus(VirtualServerStatus.running);
 		vs3.setStatus(VirtualServerStatus.running);
 
 		list.add(vs1);
 		manager.add(vs1);
 
		final HPCloudManager cloudManager = Mockito.mock(HPCloudManager.class);
		VirtualServerResource virtualServerResource = PowerMockito.spy(new VirtualServerResource()
		{
			protected HPCloudManager getNewHPCloudManager(String acessKey, String encryptedKey)
			{
				return cloudManager;				
			}
		});

 		PowerMockito.doNothing().when(virtualServerResource, "refreshVirtualServer", Mockito.any());
		PowerMockito.doNothing().when(virtualServerResource, "updateVirtualServer", Mockito.any(), Mockito.any());
 		PowerMockito.when(virtualServerResource, "getZombie").thenReturn(list);
 
 		assertEquals(true, Whitebox.invokeMethod(virtualServerResource, "createWithZombie", vs2));
 		assertEquals(false, Whitebox.invokeMethod(virtualServerResource, "createWithZombie", vs3));
 	}
 
 	@Test
 	public void isZombieCandidateTest() throws Exception
 	{
 		VirtualServerResource virtualServerResource = PowerMockito.spy(new VirtualServerResource());
 
 		// a non empty list of siblings
 		ArrayList<String> siblings = new ArrayList<String>();
 		siblings.add("http://location1.com");
 		siblings.add("http://location2.com");
 
 		// vs1 is running and has siblings empty
 		VirtualServer vs1 = new VirtualServer("zombificable0", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs1.setStatus(VirtualServerStatus.running);
 		vs1.setInstanceId("InstanceID01");
 		vs1.setSiblings(new ArrayList<String>());
 
 		// vs2 is terminated and has siblings
 		VirtualServer vs2 = new VirtualServer("unzombificable0", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs2.setStatus(VirtualServerStatus.terminated);
 		vs2.setInstanceId("InstanceID02");
 		vs2.setSiblings(siblings);
 
 		// vs3 is running and has siblings
 		VirtualServer vs3 = new VirtualServer("unzombificable1", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs3.setStatus(VirtualServerStatus.running);
 		vs3.setInstanceId("InstanceID03");
 		vs3.setSiblings(siblings);
 
 		// vs4 is running and has siblings in default value
 		VirtualServer vs4 = new VirtualServer("zombificable1", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs4.setStatus(VirtualServerStatus.running);
 		vs4.setInstanceId("InstanceID04");
 
 		assertEquals("vs1 should be zimbificable", true, Whitebox.invokeMethod(virtualServerResource, "isZombieCandidate", vs1));
 		assertEquals("vs2 should not be zimbificable", false, Whitebox.invokeMethod(virtualServerResource, "isZombieCandidate", vs2));
 		assertEquals("vs3 should not be zimbificable", false, Whitebox.invokeMethod(virtualServerResource, "isZombieCandidate", vs3));
 		assertEquals("vs4 should be zimbificable", true, Whitebox.invokeMethod(virtualServerResource, "isZombieCandidate", vs4));
 	}
 
 	@Test
 	public void checkForZombieExpiryTest() throws Exception
 	{
 		HPCloudManager manager = PowerMockito.mock(HPCloudManager.class);
 
 		VirtualServerResource virtualServerResource = PowerMockito.spy(new VirtualServerResource());
 		PowerMockito.doNothing().when(virtualServerResource, "update", Mockito.any());
 		PowerMockito.doReturn(manager).when(virtualServerResource, "getNewHPCloudManager", Mockito.any(), Mockito.any());
 
 		Date now = new Date();
 
 		// vs1 is a zombie expired
 		VirtualServer vs1 = new VirtualServer("zombie", "desc01", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs1.setStatus(VirtualServerStatus.terminated);
 		vs1.setInstanceId("instance01");
 		vs1.setCreated(now);
 		vs1.setId(01l);
 
 		// vs2 is a debug that is expired
 		VirtualServer vs2 = new VirtualServer("debug", "desc02", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs2.setStatus(VirtualServerStatus.terminated);
 		vs2.setInstanceId("instance02");
 		vs2.setCreated(now);
 		vs2.setId(02l);
 
 		// vs3 is a process that is expired
 		VirtualServer vs3 = new VirtualServer("process", "desc03", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs3.setStatus(VirtualServerStatus.terminated);
 		vs3.setInstanceId("instance03");
 		vs3.setCreated(now);
 		vs3.setId(03l);
 
 		// vs4 is a zombie that is not expired
 		VirtualServer vs4 = new VirtualServer("zombie", "desc05", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs4.setStatus(VirtualServerStatus.running);
 		vs4.setInstanceId("instance04");
 		vs4.setCreated(now);
 		vs4.setId(04l);
 
 		// vs5 is a debug that is not expired
 		VirtualServer vs5 = new VirtualServer("debug", "desc05", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs5.setStatus(VirtualServerStatus.running);
 		vs5.setCreated(now);
 		vs5.setId(05l);
 
 		// vs6 is a process that is not expired
 		VirtualServer vs6 = new VirtualServer("process", "desc", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs6.setStatus(VirtualServerStatus.running);
 		vs6.setCreated(now);
 		vs6.setId(06l);
 
 		// vs7 is a zombie that is expired
 		VirtualServer vs7 = new VirtualServer("zombi3", "desc04", new URI("http://location.com"), new ArrayList<NameValue>(), new URI("http://notification.com"), "accessKey", "encryptedSecret", new URI("http://owner.com"), "idempotencyKey");
 		vs7.setStatus(VirtualServerStatus.terminated);
 		vs7.setInstanceId("instance07");
 		vs7.setCreated(now);
 		ArrayList<NameValue> p = new ArrayList<NameValue>();
 		p.add(new NameValue("n3phele-behavior", "zombie"));
 		vs7.setParameters(p);
 		vs7.setId(07l);
 
 		assertEquals("vs1 should be a expired zombie", true, virtualServerResource.checkForZombieAndDebugExpiry(vs1));
 		assertEquals("vs2 should be a expired debug", true, virtualServerResource.checkForZombieAndDebugExpiry(vs2));
 		assertEquals("vs3 should not be a expired zombie or debug", false, virtualServerResource.checkForZombieAndDebugExpiry(vs3));
 		assertEquals("vs4 should not be a expired zombie or debug", false, virtualServerResource.checkForZombieAndDebugExpiry(vs4));
 		assertEquals("vs5 should not be a expired zombie or debug", false, virtualServerResource.checkForZombieAndDebugExpiry(vs5));
 		assertEquals("vs6 should not be a expired zombie or debug", false, virtualServerResource.checkForZombieAndDebugExpiry(vs6));
 		assertEquals("vs7 should be a expired zombie", true, virtualServerResource.checkForZombieAndDebugExpiry(vs7));
 	}
 
 	@Test
 	public void returnUrisOfTwoCreatedVMs() throws InvalidParameterException,
 			Exception
 	{
 
 		final VirtualServer v1 = Utils.createFakeDataVirtualServer();
 		v1.setUri(new URI("http://localhost/server/1"));
 
 		final VirtualServer v2 = Utils.createFakeDataVirtualServer();
 		v2.setUri(new URI("http://localhost/server/2"));
 
 		VirtualServerResource resource = new VirtualServerResource() {
 			protected ArrayList<VirtualServer> createOneOrMoreVMs(ExecutionFactoryCreateRequest request, HPCloudCreateServerRequest hpCloudRequest, Date epoch)
 			{
 				ArrayList<VirtualServer> vsList = new ArrayList<VirtualServer>();
 				vsList.add(v1);
 				vsList.add(v2);
 				return vsList;
 			}
 
 			// Do not execute database update operation
 			protected void updateVMSiblings(ArrayList<String> siblings, ArrayList<VirtualServer> virtualServerList)
 			{
 
 			}
 		};
 
 		ExecutionFactoryCreateRequest request = new ExecutionFactoryCreateRequest();
 		request.parameters = new ArrayList<NameValue>();
 		NameValue nodeCount = new NameValue();
 		nodeCount.setKey("nodeCount");
 		nodeCount.setValue("1");
 		request.parameters.add(nodeCount);
 
 		Response response = resource.create(request);
 
 		CreateVirtualServerResponse createResponse = (CreateVirtualServerResponse) response.getEntity();
 		assertEquals(2, createResponse.vmList.length);
 		assertEquals(v1.getUri(), createResponse.vmList[0]);
 		assertEquals(v2.getUri(), createResponse.vmList[1]);
 	}
 
 	@Test
 	public void returnUrisOfAssimilateVMs() throws Exception
 	{
 		final Multimap<String, Address> map = ArrayListMultimap.create();
 		
 		class ServerExension extends Server
 		{
 			public ServerExension()
 			{
 				super("test", "test", null, "test", "test", "test", new Date(), new Date(), "test", "", "", Status.ACTIVE, Mockito.mock(Resource.class), Mockito.mock(Resource.class), "key", "", map, new HashMap<String, String>(), null, null, "");
 			}
 		}
 
 		class StringCollection implements Iterable<String>
 		{
 			private Vector<String>	strings;
 
 			public StringCollection()
 			{
 				strings = new Vector<String>();
 			}
 
 			public void add(String game)
 			{
 				strings.add(game);
 			}
 
 			@Override
 			public Iterator<String> iterator()
 			{
 				return strings.iterator();
 			}
 		}
 
 		HPCloudManager cloudManager = PowerMockito.mock(HPCloudManager.class);
 		NodeMetadataImpl node = new NodeMetadataImpl("1579239", "test", "az-1.region-a.geo-1/1579239", Mockito.mock(Location.class), new URI("http://location.com"), new HashMap<String, String>(), new HashSet<String>(), "group", Mockito.mock(Hardware.class), "imageId", Mockito.mock(OperatingSystem.class), org.jclouds.compute.domain.NodeMetadata.Status.RUNNING, "RUNNING", 0, new StringCollection(), new StringCollection(), Mockito.mock(LoginCredentials.class), "test");
 
 		Server server = new ServerExension();
 
 		PowerMockito.when(cloudManager, "getServerByIP", Mockito.any(), Mockito.any()).thenReturn(node);
 		PowerMockito.when(cloudManager, "getServerById", Mockito.any(), Mockito.any()).thenReturn(server);
 
 		VirtualServerResource resource = PowerMockito.spy(new VirtualServerResource());
 		PowerMockito.doReturn(cloudManager).when(resource, "getNewHPCloudManager", Mockito.any(), Mockito.any());
 
 		ExecutionFactoryAssimilateRequest request = new ExecutionFactoryAssimilateRequest();
 		request.location = new URI("http://localhost");
 		request.notification = new URI("http://localhost");
 		request.owner = new URI("http://localhost");
 		request.accessKey = "";
 		request.description = "description";
 		request.encryptedSecret = "";
 		request.idempotencyKey = "";
 		request.name = "vm_name";
 		request.locationId = "az-1.region-a.geo-1";
 		request.ipaddress = "127.0.0.1";
 
 		Response response = resource.assimilate(request);
 
 		CreateVirtualServerResponse createResponse = (CreateVirtualServerResponse) response.getEntity();
 		assertEquals(1, createResponse.vmList.length);
 		
 		response = resource.assimilate(request);
 		assertEquals(409, response.getStatus());
 	}
 
 	@Test
 	public void checkZombieVirtualServerExpiryTest() throws Exception
 	{
 		ZombieStrategy zombieStrategy = new ZombieStrategy();
 		zombieStrategy.setMinutesExpirationTime(5);
 
 		final HPCloudManager cloudManager = Mockito.mock(HPCloudManager.class);
 		VirtualServerResource virtualServerResource = new VirtualServerResource(zombieStrategy, new DebugStrategy()) {
 			protected HPCloudManager getNewHPCloudManager(String acessKey, String encryptedKey)
 			{
 				return cloudManager;
 			}
 		};
 
 		VirtualServer virtualServer = Utils.createFakeDataVirtualServer();
 		virtualServer.setInstanceId("1");
 		virtualServer.setName("zombie");
 		virtualServer.setStatus(VirtualServerStatus.running);
 		ArrayList<NameValue> parameters = new ArrayList<NameValue>();
 		parameters.add(new NameValue("n3phele-behavior", "zombie"));
 		virtualServer.setParameters(parameters);
 		Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.MINUTE, -6);
 		virtualServer.setCreated(calendar.getTime());
 
 		virtualServerResource.checkForZombieAndDebugExpiry(virtualServer);
 
 		Mockito.verify(cloudManager).terminateNode(Mockito.anyString(), Mockito.eq(virtualServer.getInstanceId()));
 	}
 
 	@Test
 	public void checkZombieVirtualServerNonExpiryTest() throws Exception
 	{
 		ZombieStrategy zombieStrategy = new ZombieStrategy();
 		zombieStrategy.setMinutesExpirationTime(5);
 
 		final HPCloudManager cloudManager = Mockito.mock(HPCloudManager.class);
 		VirtualServerResource virtualServerResource = new VirtualServerResource(zombieStrategy, new DebugStrategy()) {
 			protected HPCloudManager getNewHPCloudManager(String acessKey, String encryptedKey)
 			{
 				return cloudManager;
 			}
 		};
 
 		VirtualServer virtualServer = Utils.createFakeDataVirtualServer();
 		virtualServer.setInstanceId("1");
 		virtualServer.setName("zombie");
 		virtualServer.setStatus(VirtualServerStatus.running);
 		ArrayList<NameValue> parameters = new ArrayList<NameValue>();
 		parameters.add(new NameValue("n3phele-behavior", "zombie"));
 		virtualServer.setParameters(parameters);
 		Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.MINUTE, -4);
 		virtualServer.setCreated(calendar.getTime());
 
 		virtualServerResource.checkForZombieAndDebugExpiry(virtualServer);
 
 		Mockito.verify(cloudManager, Mockito.times(0)).terminateNode(Mockito.anyString(), Mockito.eq(virtualServer.getInstanceId()));
 	}
 
 }
