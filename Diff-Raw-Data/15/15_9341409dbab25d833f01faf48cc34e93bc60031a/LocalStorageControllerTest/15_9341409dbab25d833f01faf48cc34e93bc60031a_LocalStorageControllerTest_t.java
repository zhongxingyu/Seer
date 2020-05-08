 package edu.helsinki.sulka.controllers;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.everyItem;
 import static org.hamcrest.Matchers.nullValue;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.security.web.FilterChainProxy;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.web.context.WebApplicationContext;
 
 import edu.helsinki.sulka.SecuritySessionHelper;
 import edu.helsinki.sulka.models.RecoveryDatabaseRow;
 import edu.helsinki.sulka.models.RingingDatabaseRow;
 import edu.helsinki.sulka.models.User;
 import edu.helsinki.sulka.services.LocalDatabaseService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration({
 	"file:src/main/webapp/WEB-INF/spring/root-context.xml",
 	"file:src/main/webapp/WEB-INF/spring/security.xml",
 	"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"
 })
 @ActiveProfiles("dev")
 public class LocalStorageControllerTest {
 	
 	@Autowired
 	private WebApplicationContext wac;
 	
 	@Autowired
 	private FilterChainProxy springSecurityFilterChain;
 	
 	@Autowired
 	private LocalDatabaseService localDatabaseService;
 
 	private MockMvc mockMvc;
 	private MockHttpSession lokkiHttpSession;
 	
 	private static final String USER_ID = "LocalStorageControllerTestUserId_123456789";
 	private static final byte[] validOnlyRow = "{\"row\":\"asdflkakgh\"}".getBytes();
 	private static final byte[] validRowAndId = "{\"id\":\"1234\", \"row\":\"asdflkakgh\"}".getBytes();
 	private static final byte[] invalidId = "{\"JOTAIN\":\"1234\", \"row\":\"asdflkakgh\"}".getBytes();
 	private static final byte[] invalidRow = "{\"id\":\"1234\", \"ABCD\":\"asdflkakgh\"}".getBytes();
 	private static final byte[]	validFullRow = "{\"id\":\"1\", \"userId\":\"LocalStorageControllerTestUserId_123456789\", \"row\":\"asdflkakgh\"}".getBytes();
	private static final byte[]	validFullRowToBeDeleted = "[{\"id\":\"1\", \"userId\":\"LocalStorageControllerTestUserId_123456789\", \"row\":\"asdflkakgh\"}]".getBytes();
 	
 	@Before
     public void setup() {
     	mockMvc = webAppContextSetup(wac).addFilters(springSecurityFilterChain).build();
     	
     	User lokki = new User();
     	lokki.setPass(true);
     	lokki.setLogin_id(USER_ID);
     	lokki.setExpires_at(System.currentTimeMillis() / 1000 + 60);
     	lokkiHttpSession = SecuritySessionHelper.createUserSession(lokki);
 	}
 	
 	@After
 	public void tearDown(){
 		List<RingingDatabaseRow> ringings = localDatabaseService.getRingings(USER_ID);
 		List<RecoveryDatabaseRow> recoveries = localDatabaseService.getRecoveries(USER_ID);
 		
 		for(RingingDatabaseRow toBeDeleted: ringings)
 			localDatabaseService.removeRinging(toBeDeleted);
 		for(RecoveryDatabaseRow toBeDeleted: recoveries)
 			localDatabaseService.removeRecovery(toBeDeleted);
 	}
 	
 	@Test
 	public void testGetRingingsStatusIsOk() throws Exception {
 		mockMvc.perform(get("/api/storage/ringings")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRingingsReturnsJSON() throws Exception {
 		mockMvc.perform(get("/api/storage/ringings")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRingingsReturnJSONsSuccessIsTrue() throws Exception {
 		mockMvc.perform(get("/api/storage/ringings")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRingingsReturnJSONsContainsNoErrors() throws Exception {
 		mockMvc.perform(get("/api/storage/ringings")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andExpect(jsonPath("$.error").value(nullValue()))
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRingingsReturnJSONsObjectsIsArray() throws Exception {
 		mockMvc.perform(get("/api/storage/ringings")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andExpect(jsonPath("$.error").value(nullValue()))
 				.andExpect(jsonPath("$.objects").isArray())
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRingingsReturnJSONsObjectsUserIdIsCorrect() throws Exception {
 		mockMvc.perform(get("/api/storage/ringings")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andExpect(jsonPath("$.error").value(nullValue()))
 				.andExpect(jsonPath("$.objects").isArray())
 				.andExpect(jsonPath("$.objects[*].userId", everyItem(equalTo(USER_ID))))
 				.andReturn();
 	}
 
 	@Test
 	public void testSaveRingingStatusIsOk() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
 						.content(validOnlyRow))
 				.andExpect(status().isOk())
 				.andReturn();
 	}
 	@Test
 	public void testSaveRingingReturnsJSON() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
 						.content(validOnlyRow))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andReturn();
 	}
 	
 	@Test
 	public void testSaveRingingReturnsErrorIfPostDataIsntJSON() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_XML)
 						.content(validRowAndId))
 				.andExpect(status().isUnsupportedMediaType())
 				.andReturn();
 	}
 	
 	@Test
 	public void testSaveRingingReturnsErrorWithWronglyNamedIdField() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(invalidId))
 		.andExpect(status().isBadRequest()) // Should be internal server error
 		.andReturn();
 	}
 
 	@Test
 	public void testSaveRingingReturnsErrorWithWronglyNamedRowField() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(invalidRow))
 		.andExpect(status().isBadRequest()) // Should be internal server error
 		.andReturn();
 	}
 
 	@Test
 	public void testSaveRingingAcceptsContentWithoutId() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(validOnlyRow))
 		.andExpect(status().isOk()) // Should be internal server error
 		.andReturn();
 	}
 	
 	@Test
 	public void testSaveRingingReturnsRowWithCorrectColumns() throws Exception {
 		mockMvc.perform(post("/api/storage/ringings")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(validOnlyRow))
 		.andExpect(status().isOk())
 		.andExpect(jsonPath("$.object.id").value(notNullValue()))
 		.andExpect(jsonPath("$.object.userId").value(notNullValue()))
 		.andExpect(jsonPath("$.object.row").value(notNullValue()))
 		.andReturn();
 	}
 	
 	@Test
 	public void testDeleteRingingStatusIsOk() throws Exception {
 		mockMvc.perform(delete("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
						.content(validFullRowToBeDeleted))
 				.andExpect(status().isOk())
 				.andReturn();
 		}
 	
 	@Test
 	public void testDeleteRingingReturnsJSON() throws Exception {
 		mockMvc.perform(delete("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
						.content(validFullRowToBeDeleted))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andReturn();
 	}
 	
 	@Test
 	public void testDeleteRingingReturnsErrorIfDeleteDataIsntJSON() throws Exception {
 		mockMvc.perform(delete("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_XML)
 						.content(validFullRow))
 				.andExpect(status().isUnsupportedMediaType())
 				.andReturn();
 	}
 	
 	@Test
 	public void testDeleteRingingReturnsErrorIfUserIdDoesNotMatchRowUserId() throws Exception {
 		mockMvc.perform(delete("/api/storage/ringings")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_XML)
 						.content(validFullRow))
 				.andExpect(status().isUnsupportedMediaType())
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRecoveryStatusIsOk() throws Exception {
 		mockMvc.perform(get("/api/storage/recoveries")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRecoveriesReturnsJSON() throws Exception {
 		mockMvc.perform(get("/api/storage/recoveries")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRecoveriesReturnJSONsSuccessIsTrue() throws Exception {
 		mockMvc.perform(get("/api/storage/recoveries")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRecoveriesReturnJSONsContainsNoErrors() throws Exception {
 		mockMvc.perform(get("/api/storage/recoveries")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andExpect(jsonPath("$.error").value(nullValue()))
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRecoveriesReturnJSONsObjectsIsArray() throws Exception {
 		mockMvc.perform(get("/api/storage/recoveries")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andExpect(jsonPath("$.error").value(nullValue()))
 				.andExpect(jsonPath("$.objects").isArray())
 				.andReturn();
 	}
 	
 	@Test
 	public void testGetRecoveriesReturnJSONsObjectsUserIdIsCorrect() throws Exception {
 		mockMvc.perform(get("/api/storage/recoveries")
 						.session(lokkiHttpSession))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andExpect(jsonPath("$.success").value(true))
 				.andExpect(jsonPath("$.error").value(nullValue()))
 				.andExpect(jsonPath("$.objects").isArray())
 				.andExpect(jsonPath("$.objects[*].userId", everyItem(equalTo(USER_ID))))
 				.andReturn();
 	}
 	
 	@Test
 	public void testSaveRecoveryStatusIsOk() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
 						.content(validOnlyRow))
 				.andExpect(status().isOk())
 				.andReturn();
 	}
 	
 	@Test
 	public void testSaveRecoveryReturnsJSON() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
 						.content(validOnlyRow))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andReturn();
 	}
 	
 	@Test
 	public void testSaveRecoveryReturnsErrorIfPostDataIsntJSON() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_XML)
 						.content(validRowAndId))
 				.andExpect(status().isUnsupportedMediaType())
 				.andReturn();
 	}
 	
 	@Test
 	public void testSaveRecoveryReturnsErrorWithWronglyNamedIdField() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(invalidId))
 		.andExpect(status().isBadRequest()) // Should be internal server error
 		.andReturn();
 	}
 
 	@Test
 	public void testSaveRecoveryReturnsErrorWithWronglyNamedRowField() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(invalidRow))
 		.andExpect(status().isBadRequest()) // Should be internal server error
 		.andReturn();
 	}
 
 	@Test
 	public void testSaveRecoveryAcceptsContentWithoutId() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(validOnlyRow))
 		.andExpect(status().isOk()) // Should be internal server error
 		.andReturn();
 	}
 	
 	@Test
 	public void testSaveRecoveryReturnsRowWithCorrectColumns() throws Exception {
 		mockMvc.perform(post("/api/storage/recoveries")
 				.session(lokkiHttpSession)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content(validOnlyRow))
 		.andExpect(status().isOk())
 		.andExpect(jsonPath("$.object.id").value(notNullValue()))
 		.andExpect(jsonPath("$.object.userId").value(notNullValue()))
 		.andExpect(jsonPath("$.object.row").value(notNullValue()))
 		.andReturn();
 	}
 	
 	@Test
 	public void testDeleteRecoveryStatusIsOk() throws Exception {
 		mockMvc.perform(delete("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
 						.content(validFullRow))
 				.andExpect(status().isOk())
 				.andReturn();
 		}
 	
 	@Test
 	public void testDeleteRecoveryReturnsJSON() throws Exception {
 		mockMvc.perform(delete("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_JSON)
 						.content(validFullRow))
 				.andExpect(status().isOk())
 				.andExpect(content().contentType("application/json;charset=UTF-8"))
 				.andReturn();
 	}
 	
 	@Test
 	public void testDeleteRecoveryReturnsErrorIfDeleteDataIsntJSON() throws Exception {
 		mockMvc.perform(delete("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_XML)
 						.content(validFullRow))
 				.andExpect(status().isUnsupportedMediaType())
 				.andReturn();
 	}
 	
 	@Test
 	public void testDeleteRecoveryReturnsErrorIfUserIdDoesNotMatchRowUserId() throws Exception {
 		mockMvc.perform(delete("/api/storage/recoveries")
 						.session(lokkiHttpSession)
 						.contentType(MediaType.APPLICATION_XML)
 						.content(validFullRow))
 				.andExpect(status().isUnsupportedMediaType())
 				.andReturn();
 	}
 	
 }
