 package edu.ibs.core.operation.logic;
 
 import edu.ibs.core.controller.SpecifiedJpaController;
 import edu.ibs.core.entity.Account;
 import edu.ibs.core.entity.User;
 import org.junit.*;
 
 /**
  *
  * @author vadim
  */
 public class CommonServiceTest {
 
 	private final CommonService service = new CommonService();
 	private final SpecifiedJpaController controller = SpecifiedJpaController.instance();
 
 	public CommonServiceTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void registerTest() {
 		String email = "vadim.martos@gmail.com";
 		Account acc = null;
 		try {
 			Assert.assertTrue(service.isFree(email));
 			acc = service.register(email, "12314");
 			Assert.assertFalse(service.isFree(email));
 			Account acc1 = controller.getUserAccount(acc.getEmail(), acc.getPassword());
 			Assert.assertEquals(acc, acc1);
 		} finally {
 			if (acc != null) {
 				controller.delete(acc.getClass(), acc.getId());
 			}
 		}
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void registerInvalidEmailTest() {
 		String email = "vadim.martos.gmail.com";
 		Account acc = null;
 		try {
 			Assert.assertFalse(service.isValid(email));
 			Assert.assertFalse(service.isFree(email));
 			acc = service.register(email, "pass");
 			Assert.assertFalse(service.isFree(email));
 			Assert.assertNull(acc);
 			Account acc1 = controller.getUserAccount(acc.getEmail(), acc.getPassword());
 			Assert.assertNull(acc1);
 		} finally {
 			if (acc != null) {
 				controller.delete(acc.getClass(), acc.getId());
 			}
 		}
 	}
 
 	@Test
 	public void loginTest() {
 		String email = "vadim.martos@gmail.com", pass = "pass";
 		Account acc = null;
 		try {
 			acc = service.register(email, pass);
 			Account acc2 = service.login(email, pass);
 			Assert.assertEquals(acc, acc2);
 			Account acc3 = service.login(email, pass + 1);
 			Assert.assertNull(acc3);
 			acc3 = service.login(email + 1, pass);
 			Assert.assertNull(acc3);
 		} finally {
 			if (acc != null) {
 				controller.delete(acc.getClass(), acc.getId());
 			}
 		}
 	}
 
 	@Test
 	public void updateAccountTest() {
 		String email = "vadim.martos@gmail.com", pass = "ad";
 		Account acc = null;
 		User u = null;
 		try {
 			acc = service.register(email, pass);
 			Assert.assertNull(acc.getUser());
 			u = new User("Vadim", "Martos", "AB1953782");
 			acc.setUser(u);
 			service.update(acc);
 			Account acc1 = service.login(email, pass);
 			Assert.assertEquals(u, acc1.getUser());
 			u = acc1.getUser();
 			String addr = "addr";
 			u.setAddress(addr);
			service.update(acc1);
 			acc = service.login(email, pass);
 			Assert.assertNotNull(acc.getUser());
 			Assert.assertNotNull(acc.getUser().getAddress());
 			Assert.assertEquals(addr, acc.getUser().getAddress());
 		} finally {
 			if (u != null && u.getId() != 0) {
 				controller.delete(u.getClass(), u.getId());
 			}
 			if (acc != null) {
 				controller.delete(acc.getClass(), acc.getId());
 			}
 		}
 	}
 }
