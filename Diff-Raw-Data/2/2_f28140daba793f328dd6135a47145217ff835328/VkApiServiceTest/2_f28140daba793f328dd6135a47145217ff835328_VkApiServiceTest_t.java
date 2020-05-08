 package com.couple.services.external;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.StringEntity;
 import org.junit.Test;
 
 import com.couple.services.external.User.Sex;
 
 public class VkApiServiceTest {
 	private final VkApiService service = new VkApiService("http://fake.vk-api.com");
 	
 	private HttpClient buildClientWithResponse(String jsonResponse) throws IOException {
 		HttpResponse httpResponse = mock(HttpResponse.class);
 		when(httpResponse.getEntity()).thenReturn(new StringEntity(jsonResponse, "UTF-8"));
 		
 		HttpClient client = mock(HttpClient.class);
 		when(client.execute(any(HttpGet.class))).thenReturn(httpResponse);
 		
 		return client;
 	}
 	
 	@Test
 	public void shouldReturnNullIfUserNotFound() throws IOException {
 		service.setHttpClient(buildClientWithResponse("{}"));
 		
 		User user = service.getUser("");
 		
 		assertNull(user);
 	}
 	
 	@Test
	public void shouldReadUserFromResponse() throws IOException {
 		String response = "{" +
 			"\"response\": [" +
 				"{" +
 					"\"uid\": \"12345\"," +
 					"\"first_name\": \"Test\"," +
 					"\"last_name\": \"User\"," +
 					"\"sex\": 2," +
 					"\"photo\":\"http://vk.com/small.jpg\"" +
 				"}" +
 			"]" +
 		"}";
 		
 		service.setHttpClient(buildClientWithResponse(response));
 		
 		User user = service.getUser("");
 		
 		assertNotNull(user);
 		assertEquals("12345", user.getId());
 		assertEquals("Test", user.getFirstName());
 		assertEquals("User", user.getLastName());
 		assertEquals(Sex.MALE, user.getSex());
 		assertEquals("http://vk.com/small.jpg", user.getSmallPhotoUrl());
 	}
 }
