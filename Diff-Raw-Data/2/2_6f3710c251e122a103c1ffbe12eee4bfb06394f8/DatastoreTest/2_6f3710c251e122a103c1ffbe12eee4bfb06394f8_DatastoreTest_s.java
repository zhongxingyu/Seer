 package com.rambi;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.rambi.core.RambiScriptMachine;
 
 public class DatastoreTest {
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
 			new LocalDatastoreServiceTestConfig());
 
 	private String appConfig = "com/rambi/DatastoreTestConfig.js";
 
 	@Before
 	public void init() {
 		RambiScriptMachine.getInstance().init(appConfig);
 		helper.setUp();
 	}
 
 	@After
 	public void tearDown() {
 		helper.tearDown();
 	}
 
 	@Test
 	public void testDatastore() throws EntityNotFoundException {
 		// PUT
 		HttpServletRequest req = new RequestMock("mock/mock", "PUT") {
 
 			private Map<String, String> params = new HashMap<String, String>();
 			{
 				params.put("key", "5");
 			}
 
 			@Override
 			public String getParameter(String param) {
 				return params.get(param);
 			}
 		};
 
 		ResponseMock responseMock = new ResponseMock();
 		RambiScriptMachine.getInstance().executeHttpRequest(req, responseMock);
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		assertEquals("5", responseMock.getOutData());
 		Entity entity = service.get(KeyFactory.createKey("Kind", 5));
 		assertEquals("PUT - value", entity.getProperty("value"));
 		assertNotNull(entity);
 
 		// POST
 		req = new RequestMock("mock/mock", "POST");
 		responseMock = new ResponseMock();
 		RambiScriptMachine.getInstance().executeHttpRequest(req, responseMock);
 
 		final long id = Long.parseLong(responseMock.getOutData());
 		entity = service.get(KeyFactory.createKey("Kind", id));
 		assertNotNull(entity);
 		assertEquals("POST - value", entity.getProperty("value"));
 
 		// GET
 		req = new RequestMock("mock/mock", "GET") {
 
 			private Map<String, String> params = new HashMap<String, String>();
 			{
 				params.put("key", String.valueOf(id));
 				params.put("kind", "Kind");
 			}
 
 			@Override
 			public String getParameter(String param) {
 				return params.get(param);
 			}
 		};
 
 		responseMock = new ResponseMock();
 		RambiScriptMachine.getInstance().executeHttpRequest(req, responseMock);
 
 		JsonObject resp = (JsonObject) new JsonParser().parse(responseMock
 				.getOutData());
 		assertEquals("POST - value", resp.get("value").getAsString());
 	}
 }
