 package edu.helsinki.sulka.controllers;
 
 import static org.hamcrest.Matchers.anyOf;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.everyItem;
 import static org.hamcrest.Matchers.greaterThanOrEqualTo;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.security.web.FilterChainProxy;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.web.context.WebApplicationContext;
 
 import edu.helsinki.sulka.SecuritySessionHelper;
 import edu.helsinki.sulka.models.User;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration({
 	"file:src/main/webapp/WEB-INF/spring/root-context.xml",
 	"file:src/main/webapp/WEB-INF/spring/security.xml",
 	"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"
 })
 public class RowsControllerTest {
     @Autowired
     private WebApplicationContext wac;
     
 	@Autowired
 	private FilterChainProxy springSecurityFilterChain;
 
     private MockMvc mockMvc;
     private MockHttpSession lokkiHttpSession;
     private MockHttpSession testUserHttpSession;
 
     private static final int LOKKI_ID = 846;
     private static final int TEST_ID = 10020;
     
     @Before
     public void setup() {
     	mockMvc = webAppContextSetup(wac).addFilters(springSecurityFilterChain).build();
     	
     	User lokki = new User();
     	lokki.setPass(true);
     	lokki.setLogin_id(Integer.toString(LOKKI_ID));
     	lokki.setExpires_at(System.currentTimeMillis() / 1000 + 60);
     	lokkiHttpSession = SecuritySessionHelper.createUserSession(lokki);
     	
     	User testUser = new User();
     	testUser.setLogin_id(Integer.toString(TEST_ID));
     	testUser.setPass(true);
     	testUser.setExpires_at(System.currentTimeMillis() / 1000 + 60);
     	testUserHttpSession = SecuritySessionHelper.createUserSession(testUser);
     }
 
     @SuppressWarnings("unchecked")
 	@Test
     public void testAll() throws Exception {
     	/* These tests need to run as Heikki Lokki */
     	mockMvc.perform(get("/api/rows").session(lokkiHttpSession))
     		.andExpect(status().isBadRequest())
     		.andExpect(content().contentType("application/json;charset=UTF-8"))
     		.andExpect(jsonPath("$.success").value(false))
     		.andExpect(jsonPath("$.error").value(notNullValue()))
     		.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=ESPOO").session(lokkiHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(7))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(LOKKI_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("ESPOO"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=VANTAA").session(lokkiHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].person", everyItem(equalTo(Integer.toString(LOKKI_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("VANTAA"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=VANTAA&municipality=ESPOO").session(lokkiHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(LOKKI_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("VANTAA"), equalTo("ESPOO")))))
 			.andReturn();
     	
     	/* These need to run as test user */
     	mockMvc.perform(get("/api/rows?municipality=INKOO").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(7))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("INKOO"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("MHAMN"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=13.6.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=7.6.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=07.6.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=7.06.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=07.06.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=6.13.1974").session(testUserHttpSession))
 			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/json;charset=UTF-8"))
    		.andExpect(jsonPath("$.success").value(false))
    		.andExpect(jsonPath("$.error").value(notNullValue()))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=1.1.1970&endDate=1.1.2013").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=01.01.1970&endDate=20.1.2013").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=01.01.1970&endDate=1.20.2013").session(testUserHttpSession))
 			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/json;charset=UTF-8"))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error").value(notNullValue()))
 			.andReturn();
     	mockMvc.perform(get("/api/rows?municipality=MHAMN&municipality=INKOO&startDate=01.01.1970&endDate=20.01.2013").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
 	}
     
     @SuppressWarnings("unchecked")
 	@Test
     public void testRingings() throws Exception {
     	/* These tests need to run as Heikki Lokki */
     	mockMvc.perform(get("/api/rows/ringings").session(lokkiHttpSession))
     		.andExpect(status().isBadRequest())
     		.andExpect(content().contentType("application/json;charset=UTF-8"))
     		.andExpect(jsonPath("$.success").value(false))
     		.andExpect(jsonPath("$.error").value(notNullValue()))
     		.andReturn();
     	mockMvc.perform(get("/api/rows/ringings?municipality=ESPOO").session(lokkiHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(7))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(LOKKI_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("ESPOO"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/ringings?municipality=VANTAA").session(lokkiHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].person", everyItem(equalTo(Integer.toString(LOKKI_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("VANTAA"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/ringings?municipality=VANTAA&municipality=ESPOO").session(lokkiHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(LOKKI_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("VANTAA"), equalTo("ESPOO")))))
 			.andReturn();
 	}
     
     @SuppressWarnings("unchecked")
 	@Test
     public void testRecoveries() throws Exception {
     	mockMvc.perform(get("/api/rows/recoveries?municipality=INKOO").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(7))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("INKOO"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(equalTo("MHAMN"))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=13.6.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=7.6.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=07.6.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=7.06.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=07.06.1974").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=6.13.1974").session(testUserHttpSession))
 			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/json;charset=UTF-8"))
    		.andExpect(jsonPath("$.success").value(false))
    		.andExpect(jsonPath("$.error").value(notNullValue()))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=1.1.1970&endDate=1.1.2013").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=01.01.1970&endDate=20.1.2013").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=01.01.1970&endDate=1.20.2013").session(testUserHttpSession))
 			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/json;charset=UTF-8"))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error").value(notNullValue()))
 			.andReturn();
     	mockMvc.perform(get("/api/rows/recoveries?municipality=MHAMN&municipality=INKOO&startDate=01.01.1970&endDate=20.01.2013").session(testUserHttpSession))
 			.andExpect(status().isOk())
 			.andExpect(content().contentType("application/json;charset=UTF-8"))
 			.andExpect(jsonPath("$.success").value(true))
 			.andExpect(jsonPath("$.error").value(nullValue()))
 			.andExpect(jsonPath("$.objects").isArray())
 			.andExpect(jsonPath("$.objects", hasSize(greaterThanOrEqualTo(2))))
 			.andExpect(jsonPath("$.objects[*].ringer", everyItem(equalTo(Integer.toString(TEST_ID)))))
 			.andExpect(jsonPath("$.objects[*].municipality", everyItem(anyOf(equalTo("MHAMN"), equalTo("INKOO")))))
 			.andReturn();
 	}
 }
