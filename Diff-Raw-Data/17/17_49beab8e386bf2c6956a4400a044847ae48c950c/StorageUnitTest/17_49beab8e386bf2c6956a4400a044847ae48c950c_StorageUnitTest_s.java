 package test.model;
 
 import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;
 import model.ProductContainerManager;
 import model.StorageUnit;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class StorageUnitTest {
	private ProductContainerManager mockManager;

 	@Before
 	public void setUp() throws Exception {
		mockManager = createMock(ProductContainerManager.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
	public void testEqualStorageNames() {

		StorageUnit unit1 = new StorageUnit("Unit1", mockManager);
		StorageUnit unit2 = new StorageUnit("Unit1", mockManager);
		assertTrue(unit1.equals(unit2));
	}

	@Test
 	public void testValidStorageUnitName() {
 		new StorageUnit("Unit1", mockManager);
 	}
 }
