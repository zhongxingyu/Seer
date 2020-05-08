 package de.rallye.test;
 
 import static de.rallye.test.helper.HttpClient.apiCall;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Authenticator;
 import java.net.HttpURLConnection;
 import java.net.PasswordAuthentication;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.rallye.exceptions.DataException;
 import de.rallye.model.structures.Group;
 import de.rallye.test.db.MockDataAdapter;
 import de.rallye.test.helper.StartTestServer;
 
 public class GroupsTest {
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		StartTestServer.getServer(); //Start a server
 	}
 	
 
 	static ObjectMapper mapper = new ObjectMapper();
 	static MockDataAdapter data = MockDataAdapter.getInstance();
 	
 	@Test 
 	public void testGetGroups() throws IOException, DataException {
 		StartTestServer.getServer();
 		
 		HttpResponse c = apiCall("rallye/groups", 200);
 		List<Group> groups = mapper.readValue(c.getEntity().getContent(), new TypeReference<List<Group>>(){});
		assertEquals("Returned Groups should be equal to source",data.getGroups(true),groups);
 	}
 
 	@Test @Ignore
 	public void testGetMembers() {
 		fail("Not yet implemented");
 	}
 
 	@Test @Ignore
 	public void testGetGroupAvatar() {
 		fail("Not yet implemented");
 	}
 
 	@Test @Ignore
 	public void testGetUserInfo() {
 		fail("Not yet implemented");
 	}
 
 	@Test @Ignore
 	public void testGetPushSettings() {
 		fail("Not yet implemented");
 	}
 
 	@Test @Ignore
 	public void testSetPushConfig() {
 		fail("Not yet implemented");
 	}
 	
 
 	static DefaultHttpClient httpclient = new DefaultHttpClient();
 
 	@Test
 	public void testLogin() throws IOException {
 
 
 		Authenticator.setDefault(new Authenticator() {
 			@Override
 			protected PasswordAuthentication getPasswordAuthentication() {
 				String realm = getRequestingPrompt();
 				assertEquals("Should be right Realm","RallyeNewUser",realm);
 				return new PasswordAuthentication(String.valueOf(1), "test".toCharArray());
 			}
 		});
 		URL url = new URL("http://127.0.0.1:10111/rallye/groups/1");
 		HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
 		conn.setRequestMethod("PUT");
 		
 
 		conn.setDoOutput(true);
 		conn.addRequestProperty("Content-Type", "application/json");
 //		conn.setFixedLengthStreamingMode(post.length);
 		conn.getOutputStream().write(MockDataAdapter.validLogin.getBytes());
 		
 
 		int code = conn.getResponseCode();
 		
 		Authenticator.setDefault(null);
 		
 		try {
 		assertEquals("Code should be 200",200,code);
 		} catch (AssertionError e) {
 			System.err.println("This is the content:");
 			List<String> contents = IOUtils.readLines((InputStream)conn.getContent());
 			for (String line: contents) 
 				System.err.println(line);
 			throw e;
 		}
 		
 		
 	}
 
 	@Test @Ignore
 	public void testLogout() {
 		fail("Not yet implemented");
 	}
 
 }
