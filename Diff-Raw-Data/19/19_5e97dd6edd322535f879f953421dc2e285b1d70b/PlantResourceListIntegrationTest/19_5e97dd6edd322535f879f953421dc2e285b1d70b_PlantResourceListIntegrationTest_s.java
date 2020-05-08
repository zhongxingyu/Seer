 package cs.ut.domain.rest;
 
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.ResponseEntity;
 import org.springframework.roo.addon.test.RooIntegrationTest;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.web.client.RestTemplate;
 
 import cs.ut.domain.HireRequestStatus;
 import cs.ut.domain.Plant;
 import cs.ut.domain.PurchaseOrder;
 import cs.ut.domain.PurchaseOrderUpdate;
 import cs.ut.security.Assignments;
 import cs.ut.security.Authorities;
 import cs.ut.security.Users;
 import cs.ut.util.RestHelper;
 
 @ContextConfiguration(locations = { "/META-INF/spring/applicationContext*.xml" })
 @RooIntegrationTest(entity = PlantResourceList.class)
 public class PlantResourceListIntegrationTest {
 
 
 	@Value("${webappurl}")
 	String webappurl;
 
 	Assignments assignments;
 	Authorities authorities;
 	
 	RestTemplate template;
 
 	@BeforeClass
 	public static void doStuff() {
 		removeStuff();
 		setUsers();
 	}
 	
 	@Before
 	public void  setUp(){
 		template = new RestTemplate();
 	}
 
 	private static void removeStuff() {
 		
 		List<PurchaseOrderUpdate> poU = PurchaseOrderUpdate.findAllPurchaseOrderUpdates();
 		for (PurchaseOrderUpdate purchasOrderUpdate : poU) {
 			purchasOrderUpdate.remove();
 		}
 		
 		List<PurchaseOrder> po = PurchaseOrder.findAllPurchaseOrders();
 		for (PurchaseOrder purchaseOrder : po) {
 			purchaseOrder.remove();
 		}
 		
 		List<Plant> plants = Plant.findAllPlants();
 		for (Plant plant : plants) {
 			plant.remove();
 		}
 		
 		List<Assignments> assignments = Assignments.findAllAssignmentses();
 		for (Assignments assignments2 : assignments) {
 			assignments2.remove();
 		}
 		
 		List<Users> allUsers = Users.findAllUserses();
 		for (Users users : allUsers) {
 			users.remove();
 		}
 		
 		List<Authorities> auth = Authorities.findAllAuthoritieses();
 		for (Authorities authorities : auth) {
 			authorities.remove();
 		}
 	}
 
 	private static void setUsers() {
 		Users user = new Users();
 		user.setEnabled(true);
 		user.setPassword("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
 		user.setUsername("user@rentit.com");
 		user.persist();
 
 		Users admin = new Users();
 		admin.setEnabled(true);
 		admin.setPassword("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
 		admin.setUsername("admin@rentit.com");
 		admin.persist();
 
 		Authorities authUser = new Authorities();
 		authUser.setAuthority("ROLE_USER");
 		authUser.persist();
 
 		Authorities authAdmin = new Authorities();
 		authAdmin.setAuthority("ROLE_ADMIN");
 		authAdmin.persist();
 
 		Assignments assignSiteEng = new Assignments();
 		assignSiteEng.setAuthority(authUser);
 		assignSiteEng.setUserRentit(user);
 		assignSiteEng.persist();
 
 		Assignments assignAdmin = new Assignments();
 		assignAdmin.setAuthority(authAdmin);
 		assignAdmin.setUserRentit(admin);
 		assignAdmin.persist();
 	}
 
 	private long createPlant(String plantName) {
 		Plant p = new Plant();
 		p.setName("PlantResourceListTruck");
 		p.setDescription("Dodge 2012");
 		p.setPricePerDay(new BigDecimal(400));
 		p.persist();
 		p.flush();
 		return p.getId();
 
 	}
 
 	private long createPlant() {
 		Plant p = new Plant();
 		p.setDescription("TestAvailable");
 		p.setName("Truck");
 		p.setPricePerDay(new BigDecimal(200));
 		p.persist();
 		return p.getId();
 	}
 
 	private long createPO(long plantId, Date startDate, Date endDate) {
 		PurchaseOrder po = new PurchaseOrder();
 		po.setEndDate(endDate);
 		po.setPlant(Plant.findPlant(plantId));
 		po.setStartDate(startDate);
 		po.setStatus(HireRequestStatus.OPEN);
 		po.setTotalCost(new BigDecimal(2));
 		po.persist();
 		return po.getId();
 	}
 
 	@Test
 	public void testGetPlants() {
 
 		createPlant("PlantResourceListTruck");
 		createPlant("PlantResourceListTruck2");
 
 		HttpEntity<String> requestEntity = new HttpEntity<String>(
 				RestHelper.getHeaders("user@rentit.com", "password"));
 		ResponseEntity<PlantResourceList> response = template.exchange(
 				webappurl + "/rest/plant/", HttpMethod.GET, requestEntity,
 				PlantResourceList.class);
 
 		assertTrue(response.getStatusCode().value() == 200);
 		PlantResourceList plantList = response.getBody();
 		assertTrue(plantList.getListOfPlantResources().size() > 1);
 	}
 
 	@Test
 	public void testGetAvailablePlants() {
 		long id = createPlant();
 		createPlant("PlantResourceListTruck");
 
 		HttpEntity<String> requestEntity = new HttpEntity<String>(
 				RestHelper.getHeaders("user@rentit.com", "password"));
 		
 		ResponseEntity<PlantResourceList> response = template.exchange(
 				webappurl + "/rest/plant/", HttpMethod.GET, requestEntity,
 				PlantResourceList.class);
 
 		assertTrue(response.getStatusCode().value() == 200);
 
 		PlantResourceList plantList = response.getBody();
 		long allPlantsSize = plantList.getListOfPlantResources().size();
 		// Then createPO for one plant and make sure it is not available
 		DateTime today = new DateTime().toDateMidnight().toDateTime();
 		DateTime tomorrow = today.plusDays(1);
 		createPO(id, today.toDate(), tomorrow.toDate());
 
		String startDateString = new SimpleDateFormat("MM-dd-yyyy").format(today
 				.toDate());
		String endDateString = new SimpleDateFormat("MM-dd-yyyy").format(tomorrow
 				.toDate());
 
 		ResponseEntity<PlantResourceList> response2 = template.exchange(
 				webappurl + "/rest/plant/" + "?startDate=" + startDateString
 						+ "&endDate=" + endDateString, HttpMethod.GET,
 				requestEntity, PlantResourceList.class);
 
 		assertTrue(response2.getStatusCode().value() == 200);
 
 		PlantResourceList plantListAvailable = response2.getBody();
 		assertTrue(plantListAvailable.getListOfPlantResources().size() == allPlantsSize - 1);
 
 
 	}
 }
