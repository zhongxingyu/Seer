 package test.model;
 
 import static org.easymock.EasyMock.createMock;
 import model.ProductContainerManager;
 import model.StorageUnit;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class StorageUnitTest {
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testValidStorageUnitName() {
		ProductContainerManager mockManager = createMock(ProductContainerManager.class);
 		new StorageUnit("Unit1", mockManager);
 	}
 }
