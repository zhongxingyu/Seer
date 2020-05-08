 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package assignment2;
 
 import badm.BridgeHelper;
 import badm.Budget;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import org.junit.*;
 import org.workplicity.task.NetTask;
 import org.workplicity.util.Helper;
 import org.workplicity.worklet.WorkletContext;
 
 /**
  *
  * @author idontknow5691
  */
 public class Test3 {
 	
 	private static WorkletContext context = WorkletContext.getInstance();
 	private static Budget budget;
 	private static Integer id;
 
 	
 	public Test3() {
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
 	public void login(){
 		
 		try { 
 			// Set the store name since the default may be be not ours
                         NetTask.setStoreName("badm");
                         NetTask.setUrlBase("http://localhost:8080/netprevayle/task");
 
                         // Attempt the login
                         if(!Helper.login("admin", "gazelle", context))
 				fail("login failed");
 
 		} catch (Exception e) {
 			fail("failed with exception: " + e);
 		}
 	}
 	
 	@Test
 	public void createBudget(){
 		try{
 		budget = (Budget) BridgeHelper.getBudgetFactory().create();
 		
 		budget.setDescription("My Budget");
 		}catch(Exception e){
 			fail("cannot create budget" + e);
 		}
 	}
 	
 	@Test
 	public void syncBudget(){
 		try{
 			budget.setDescription("I'M A BUDGET!!!!!!!!");
 			budget.commit();
 		}catch(Exception e){
 			fail("cannot sync budget" + e);
 		}
 	}
 	
 	@Test
 	public void queryBudget(){
 		try{
 			budget.setDescription("I'M A BUDGET!!!!!!!!");
 			budget.setTotal(1337);
			id = budget.getId();
 			budget.commit();
 		}catch(Exception e){
 			fail("cannot sync budget" + e);
 		}
 		try{
 			Budget queriedBudget = Budget.find(id);
 			assertEquals(queriedBudget.getDescription(), "I'M A BUDGET!!!!!!!!");
 			System.out.println(queriedBudget.getDescription());
 		}catch(Exception e){
 			fail("Could not fetch the budget"+e);
 		}
 	}
 	
 }
