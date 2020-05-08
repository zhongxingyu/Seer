 /**
  * 
  */
 package passiveobjects;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author lxmonk
  * 
  */
 public class WarehouseImplTest {
 
 	Warehouse warehouse;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		Logger logger= Logger.getLogger("noLogger");
		this.warehouse = new WarehouseImpl(logger);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for
 	 * {@link passiveobjects.WarehouseImpl#addResource(passiveobjects.Resource, int)}
 	 * .
 	 */
 	@Test
 	public void testAddResource() {
 		int rand = (int) Math.round(Math.random() * 100);
 		Resource hammer = new Resource("Hammer", rand);
		int amountBefore = this.warehouse.queryResource(hammer);
		this.warehouse.addResource(hammer);
		int amountAfter = this.warehouse.queryResource(hammer);
 		assertEquals(amountBefore + rand, amountAfter);
 	}
 
 	/**
 	 * Test method for
 	 * {@link passiveobjects.WarehouseImpl#queryResource(passiveobjects.Resource)}
 	 * .
 	 */
 	@Test
 	public void testQueryResource() {
 		/* "Simple queries are never tested." */
 	}
 
 	/**
 	 * Test method for
 	 * {@link passiveobjects.WarehouseImpl#resourcesAvailable(java.util.List)}.
 	 */
 	@Test
 	public void testResourcesAvailable() {
 		String randStr = UUID.randomUUID().toString();
 		int randInt = (int) Math.round(Math.random() * 10) + 1;
 		Resource resource1 = new Resource(randStr, randInt);
 		Resource resource2 = new Resource(randStr + "2", randInt + 2);
 		// resources to make the method fail:
		Resource resource3 = new Resource(randStr+ "NOT_EXISTANT", randInt + 3);
 		Resource resource4 = new Resource(randStr + "NOT_EXISTANT", randInt + 2);
 		List<Resource> resourseList = new ArrayList<Resource>();
 		List<Resource> failedresourseList1 = new ArrayList<Resource>();
 		List<Resource> failedresourseList2 = new ArrayList<Resource>();
 
 		resourseList.add(resource1);
 		resourseList.add(resource2);
 		warehouse.addResource(resource1);
 		warehouse.addResource(resource2);
 
 		assertEquals(true, warehouse.resourcesAvailable(resourseList));
 
 		failedresourseList1.add(resource3);
 		failedresourseList2.add(resource4);
 		assertEquals(false, warehouse.resourcesAvailable(failedresourseList1));
 		assertEquals(false, warehouse.resourcesAvailable(failedresourseList2));
 	}
 
 	/**
 	 * Test method for
 	 * {@link passiveobjects.WarehouseImpl#returnResources(java.util.List)}.
 	 */
 	@Test
 	public void testReturnResources() {
 		String randStr = UUID.randomUUID().toString();
 		int randInt = (int) Math.round(Math.random() * 10) + 1;
 		Resource resource1 = new Resource(randStr, randInt);
 		Resource resource2 = new Resource(randStr + "2", randInt + 2);
 		warehouse.addResource(resource1);
 		warehouse.addResource(resource2);
 		List<Resource> resourceList = new ArrayList<Resource>();
 		resourceList.add(new Resource(randStr));
 		resourceList.add(new Resource(randStr + "2"));
 		warehouse.returnResources(resourceList,"Test");
 		assertEquals(randInt + 1, warehouse.queryResource(resource1));
 		assertEquals(randInt + 3, warehouse.queryResource(resource2));
 	}
 
 	/**
 	 * Test method for
 	 * {@link passiveobjects.WarehouseImpl#takeResources(java.util.List)}.
 	 * @throws InterruptedException 
 	 */
 	@Test
 	public void testGetResources() throws InterruptedException {
 		String randStr = UUID.randomUUID().toString();
 		int randInt = (int) Math.round(Math.random() * 10) + 1;
 		Resource resource1 = new Resource(randStr, randInt);
 		Resource resource2 = new Resource(randStr + "2", randInt + 2);
 		warehouse.addResource(resource1);
 		warehouse.addResource(resource2);
 		List<Resource> resourceList = new ArrayList<Resource>();
 		resourceList.add(new Resource(randStr));
 		resourceList.add(new Resource(randStr + "2"));
 		Task task = new TaskImpl(randStr + "8", new ManagerSpecialization(
 				randStr + "24"), new WorkerSpecialty(randStr + randStr),
 				randInt, resourceList);
 		warehouse.getResources(task,"Test");
 		assertEquals(randInt - 1, warehouse.queryResource(resource1));
 		assertEquals(randInt + 1, warehouse.queryResource(resource2));
 	}
 }
