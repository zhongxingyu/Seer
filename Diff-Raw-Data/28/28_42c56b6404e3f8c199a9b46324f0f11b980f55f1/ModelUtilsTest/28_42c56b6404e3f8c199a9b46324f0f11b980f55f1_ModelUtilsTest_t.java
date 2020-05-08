 package com.pangratz.oeffinpc.model;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import junit.framework.TestCase;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 
 public class ModelUtilsTest extends TestCase {
 
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 	private ModelUtils modelUtils = null;
 	private PersistenceManager pm;
 
 	public void testAddNetworkPlanAndEntries() {
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		Long linzKey = modelUtils.storeNetworkPlan(linz);
 
 		NetworkPlanEntry schumpeter = createNetworkPlanEntry(linzKey, "Schumpeterstrasse", "123", 100, 200);
 		Long schumpeterKey = modelUtils.storeNetworkPlanEntry(schumpeter);
 		assertNotNull(schumpeterKey);
 		NetworkPlanEntry insertedSchumpeter = modelUtils.getNetworkPlanEntry(schumpeterKey);
 		assertNotNull(insertedSchumpeter);
 		assertEquals(schumpeterKey, insertedSchumpeter.getKey());
 		assertEquals(linzKey, insertedSchumpeter.getNetworkPlanKey());
 		assertEquals("Schumpeterstrasse", insertedSchumpeter.getName());
 		assertEquals("123", insertedSchumpeter.getStationId());
 		assertEquals(100, insertedSchumpeter.getX());
 		assertEquals(200, insertedSchumpeter.getY());
 
 		NetworkPlanEntry hauptbahnhof = createNetworkPlanEntry(linzKey, "Hauptbahnhof", "456", 189, 567);
 		Long hauptbahnhofKey = modelUtils.storeNetworkPlanEntry(hauptbahnhof);
 		assertNotNull(hauptbahnhofKey);
 		NetworkPlanEntry insertedHauptbahnhof = modelUtils.getNetworkPlanEntry(hauptbahnhofKey);
 		assertNotNull(insertedHauptbahnhof);
 		assertEquals(hauptbahnhofKey, insertedHauptbahnhof.getKey());
 		assertEquals(linzKey, insertedHauptbahnhof.getNetworkPlanKey());
 		assertEquals("Hauptbahnhof", insertedHauptbahnhof.getName());
 		assertEquals("456", insertedHauptbahnhof.getStationId());
 		assertEquals(189, insertedHauptbahnhof.getX());
 		assertEquals(567, insertedHauptbahnhof.getY());
 
 		List<NetworkPlanEntry> networkPlanEntries = modelUtils.getNetworkPlanEntries(linzKey);
 		assertNotNull(networkPlanEntries);
 		assertEquals(2, networkPlanEntries.size());
 
 		NetworkPlanEntry hauptbahnhofEntry = networkPlanEntries.get(0);
 		assertEquals(hauptbahnhof.getName(), hauptbahnhofEntry.getName());
 
 		NetworkPlanEntry schumpeterEntry = networkPlanEntries.get(1);
 		assertEquals(schumpeter.getName(), schumpeterEntry.getName());
 	}
 
 	public void testAddTwoNetworkPlans() {
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		Long linzKey = modelUtils.storeNetworkPlan(linz);
 
 		NetworkPlan bonn = createNetworkPlan("bonn", "bonn", "http://oeffi.schildbach.de/plans/bonn.png");
 		Long bonnKey = modelUtils.storeNetworkPlan(bonn);
 
 		assertFalse(linzKey.equals(bonnKey));
 	}
 
 	public void testEditNetworkPlan() {
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		modelUtils.storeNetworkPlan(linz);
 
 		linz.setImageUrl("http://oeffi.schildbach.de/plans/linz_bw.png");
 		Long key = modelUtils.storeNetworkPlan(linz);
 
 		NetworkPlan insertedNetworkPlan = pm.getObjectById(NetworkPlan.class, key);
 		assertNotNull(insertedNetworkPlan);
 		assertEquals("http://oeffi.schildbach.de/plans/linz_bw.png", insertedNetworkPlan.getImageUrl());
 		assertEquals("linz", insertedNetworkPlan.getNetworkId());
 		assertEquals("linz", insertedNetworkPlan.getPlanId());
 	}
 
 	public void testEditNetworkPlanEntry() {
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		Long linzKey = modelUtils.storeNetworkPlan(linz);
 
 		NetworkPlanEntry schumpeter = createNetworkPlanEntry(linzKey, "Schumpeterstrasse", "123", 100, 200);
 		Long schumpeterKey = modelUtils.storeNetworkPlanEntry(schumpeter);
 
 		NetworkPlanEntry editedSchumpeter = createNetworkPlanEntry(linzKey, "Schumpeterstr.", "321", 111, 222);
 		editedSchumpeter.setKey(schumpeterKey);
 		Long editedSchumpeterKey = modelUtils.storeNetworkPlanEntry(editedSchumpeter);
 
 		assertEquals(schumpeterKey, editedSchumpeterKey);
 		NetworkPlanEntry insertedSchumpeter = modelUtils.getNetworkPlanEntry(schumpeterKey);
 		assertNotNull(insertedSchumpeter);
 		assertEquals("Schumpeterstr.", insertedSchumpeter.getName());
 		assertEquals("321", insertedSchumpeter.getStationId());
 		assertEquals(111, insertedSchumpeter.getX());
 		assertEquals(222, insertedSchumpeter.getY());
 	}
 
 	public void testGetInvalidNetworkPlan() {
 		assertNull(modelUtils.getNetworkPlan(null));
 		assertNull(modelUtils.getNetworkPlan(1L));
 	}
 
 	public void testGetInvalidNetworkPlanEntry() {
 		assertNull(modelUtils.getNetworkPlanEntry(null));
 		assertNull(modelUtils.getNetworkPlanEntry(1L));
 	}
 
 	public void testGetNetworkPlan() {
 		NetworkPlan bonn = createNetworkPlan("bonn", "bonn", "http://oeffi.schildbach.de/plans/bonn.png");
 		modelUtils.storeNetworkPlan(bonn);
 
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		Long linzKey = modelUtils.storeNetworkPlan(linz);
 
 		NetworkPlan insertedNetworkPlan = modelUtils.getNetworkPlan(linzKey);
 		assertNotNull(insertedNetworkPlan);
 		assertEquals("linz", insertedNetworkPlan.getNetworkId());
 		assertEquals("linz", insertedNetworkPlan.getPlanId());
 		assertEquals("http://oeffi.schildbach.de/plans/linz.png", insertedNetworkPlan.getImageUrl());
 		assertEquals(linzKey, insertedNetworkPlan.getKey());
 	}
 
	public void testGetNetworkPlanEntries() {
		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
		Long linzKey = modelUtils.storeNetworkPlan(linz);

		modelUtils.storeNetworkPlanEntry(createNetworkPlanEntry(linzKey, "Hauptbahnhof", "111", 100, 200));
		modelUtils.storeNetworkPlanEntry(createNetworkPlanEntry(linzKey, "Schumpeterstrasse", "222", 111, 2222));
		modelUtils.storeNetworkPlanEntry(createNetworkPlanEntry(linzKey, "Rudolfstrasse", "333", 333, 444));

		NetworkPlan bonn = createNetworkPlan("bonn", "bonn", "http://oeffi.schildbach.de/plans/bonn.png");
		Long bonnKey = modelUtils.storeNetworkPlan(bonn);
		modelUtils.storeNetworkPlanEntry(createNetworkPlanEntry(bonnKey, "Bonn Station 1", "1", 1, 1));
		modelUtils.storeNetworkPlanEntry(createNetworkPlanEntry(bonnKey, "Bonn Station 2", "2", 2, 2));

		List<NetworkPlanEntry> linzNetworkPlanEntries = modelUtils.getNetworkPlanEntries(linzKey);
		assertNotNull(linzNetworkPlanEntries);
		assertEquals(3, linzNetworkPlanEntries.size());

		List<NetworkPlanEntry> bonnNetworkPlanEntries = modelUtils.getNetworkPlanEntries(bonnKey);
		assertNotNull(bonnNetworkPlanEntries);
		assertEquals(2, bonnNetworkPlanEntries.size());
	}

 	public void testGetNetworkPlans() {
 		List<NetworkPlan> networkPlans = modelUtils.getNetworkPlans();
 		assertEquals(0, networkPlans.size());
 
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		modelUtils.storeNetworkPlan(linz);
 		assertEquals(1, modelUtils.getNetworkPlans().size());
 
 		NetworkPlan bonn = createNetworkPlan("bonn", "bonn", "http://oeffi.schildbach.de/plans/bonn.png");
 		modelUtils.storeNetworkPlan(bonn);
 		assertEquals(2, modelUtils.getNetworkPlans().size());
 	}
 
 	public void testInsertNewNetworkPlan() {
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		Long key = modelUtils.storeNetworkPlan(linz);
 		assertNotNull(key);
 
 		NetworkPlan insertedNetworkPlan = pm.getObjectById(NetworkPlan.class, key);
 		assertNotNull(insertedNetworkPlan);
 		assertEquals("http://oeffi.schildbach.de/plans/linz.png", insertedNetworkPlan.getImageUrl());
 		assertEquals("linz", insertedNetworkPlan.getNetworkId());
 		assertEquals("linz", insertedNetworkPlan.getPlanId());
 	}
 
 	public void testRemoveNetworkPlanEntry() {
 		NetworkPlan linz = createNetworkPlan("linz", "linz", "http://oeffi.schildbach.de/plans/linz.png");
 		Long linzKey = modelUtils.storeNetworkPlan(linz);
 
 		NetworkPlanEntry schumpeter = createNetworkPlanEntry(linzKey, "Schumpeterstrasse", "123", 100, 200);
 		Long schumpeterKey = modelUtils.storeNetworkPlanEntry(schumpeter);
 
 		modelUtils.removeNetworkPlanEntry(schumpeterKey);
 
 		Query query = pm.newQuery(NetworkPlanEntry.class, "key == keyParam");
 		Collection<Object> entries = (Collection<Object>) query.execute(schumpeterKey);
 		assertNotNull(entries);
 		assertEquals(0, entries.size());
 	}
 
 	private NetworkPlan createNetworkPlan(String networkId, String planId, String imageUrl) {
 		NetworkPlan networkPlan = new NetworkPlan();
 		networkPlan.setNetworkId(networkId);
 		networkPlan.setPlanId(planId);
 		networkPlan.setImageUrl(imageUrl);
 		return networkPlan;
 	}
 
 	private NetworkPlanEntry createNetworkPlanEntry(Long networkPlanId, String name, String stationId, int x, int y) {
 		NetworkPlanEntry npe = new NetworkPlanEntry();
 		npe.setNetworkPlanKey(networkPlanId);
 		npe.setName(name);
 		npe.setStationId(stationId);
 		npe.setX(x);
 		npe.setY(y);
 		return npe;
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		helper.setUp();
 		pm = PMF.get().getPersistenceManager();
 		modelUtils = ModelUtils.getInstance();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		helper.tearDown();
 		pm.close();
 		modelUtils = null;
 	}
 
 }
