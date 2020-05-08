 package com.felthat.testnimble;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import junit.framework.Assert;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.http.HttpStatus;
 
 import com.felthat.nimble.graph.NimbleMapGraph;
 import com.felthat.nimble.rest.NimbleRequest;
 import com.felthat.nimble.rest.NimbleResponse;
 
 
 public class RestServiceIT {
 
 	private static HttpClient client;
 	private static ObjectMapper mapper;
 	private static String JETTY_PORT = "9090";
 	private static String urlRestNimble = "http://localhost:" + JETTY_PORT + "/test-nimble/rest/nimble";
 	
 	@BeforeClass
 	public static void setUpStatics(){
 		
 		mapper = new ObjectMapper();
 	}
 		
 	@Before
 	public void setup(){
 		client = new HttpClient();
 	}
 
 	@Test
 	public void makeSureItIsRunning() throws HttpException, IOException {
 		NimbleMapGraph graph = new NimbleMapGraph();
 		graph.put("name", "Duncan");
 		PutMethod putRequest = makePut(makeNimbleRequest(graph),urlRestNimble);
 		client.executeMethod(putRequest);
 
 		GetMethod request = new GetMethod(urlRestNimble);
 		int response = client.executeMethod(request);
 		Assert.assertEquals(HttpStatus.OK, HttpStatus.valueOf(response));
 	}
 	
 	@Test
 	public void makeSureGraphNotFound() throws HttpException, IOException {
 		GetMethod request = new GetMethod(urlRestNimble);
 		int response = client.executeMethod(request);
 		Assert.assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(response));
 	}
 	
 	
 	@Test
 	public void performPut() throws HttpException, IOException {
 		NimbleMapGraph graph = new NimbleMapGraph();
 		graph.put("name", "Duncan");
 		PutMethod putRequest = makePut(makeNimbleRequest(graph),urlRestNimble);
 		int response = client.executeMethod(putRequest);
 		System.out.println(putRequest.getResponseBodyAsString());
 		Assert.assertEquals(HttpStatus.CREATED.value(), response);
 	}
 	
 	
 	@Test
 	public void updateSingleField() throws HttpException, IOException {
 		NimbleMapGraph graph = new NimbleMapGraph();
 		graph.put("user/name", "Duncan");
 		graph.put("user/email", "duncan.atkinson@nimble.com");
 		graph.put("user/favorites/color", "green");
 		PutMethod putRequest = makePut(makeNimbleRequest(graph),urlRestNimble);
 		client.executeMethod(putRequest);
 		
 		NimbleMapGraph graphUpdate = new NimbleMapGraph();
 		graphUpdate.put("/","blue");
 		putRequest = makePut(makeNimbleRequest(graphUpdate),urlRestNimble + "/user/favorites/color");
 		int status = client.executeMethod(putRequest);
 		assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(status));
 		
 		GetMethod getMethod = new GetMethod(urlRestNimble);
 		client.executeMethod(getMethod);
 		String responseString = getMethod.getResponseBodyAsString();
 		System.out.println(responseString);
 		
 		NimbleResponse nimbleResponse2 = mapper.readValue(responseString, NimbleResponse.class);
 		NimbleMapGraph updatedGraph = new NimbleMapGraph(nimbleResponse2.getData());
 		
 		assertEquals("Duncan", updatedGraph.getValue("user/name"));
 		assertEquals("duncan.atkinson@nimble.com", updatedGraph.getValue("user/email"));
 		assertEquals("blue", updatedGraph.getValue("user/favorites/color"));
 		System.out.println(graph);
 	}
 	
 	@Test
 	public void makeSureUrlIsCorrect() throws HttpException, IOException {
 		GetMethod request = new GetMethod(urlRestNimble);
 		client.executeMethod(request);
 		String responseString = request.getResponseBodyAsString();
 		NimbleResponse nimbleResponse = mapper.readValue(responseString, NimbleResponse.class);
 		assertEquals(1,nimbleResponse.getData().keySet().size());
 		String url = (String) nimbleResponse.getData().get("url");
 		
 		request = new GetMethod(url);
 		client.executeMethod(request);
 		responseString = request.getResponseBodyAsString();
 		NimbleResponse nimbleResponse2 = mapper.readValue(responseString, NimbleResponse.class);
 		assertEquals(nimbleResponse,nimbleResponse2);
 	}
 	
 	@Test
 	public void performPutThenGet() throws HttpException, IOException {
 		NimbleMapGraph graph = new NimbleMapGraph();
 		String namePath = "user/name";
 		String emailPath = "user/email";
 		String postcodePath = "user/address/postcode";
 		String townPath = "user/address/town";
 		String countyPath = "user/address/county";
 		
 		graph.put(namePath, "Duncan");
 		graph.put(emailPath, "duncan.atkinson@nimble.com");
 		graph.put(postcodePath, "RG21 4EA");
 		graph.put(townPath, "Basingstoke");
 		graph.put(countyPath, "Hampshire");
 		
 		PutMethod putRequest = makePut(makeNimbleRequest(graph),urlRestNimble);
 		int response = client.executeMethod(putRequest);
 		assertEquals(HttpStatus.valueOf(response), HttpStatus.CREATED);
 		
 		GetMethod request = new GetMethod(urlRestNimble);
 		client.executeMethod(request);
 		String responseString = request.getResponseBodyAsString();
 		NimbleResponse nimbleResponse = mapper.readValue(responseString, NimbleResponse.class);
 		NimbleMapGraph graph2 = new NimbleMapGraph();
 		graph2.setGraphObject(nimbleResponse.getData());
 		
 		assertGraphsEqual(graph, namePath, graph2);
 		assertGraphsEqual(graph, emailPath, graph2);
 		assertGraphsEqual(graph, postcodePath, graph2);
 		assertGraphsEqual(graph, townPath, graph2);
 		assertGraphsEqual(graph, countyPath, graph2);
 	}
 	
 	
 	@Test
 	public void performPutThenGetUrls() throws HttpException, IOException {
 		NimbleMapGraph graph = new NimbleMapGraph();
 		String namePath = "user/name";
 		String emailPath = "user/email";
 		
 		graph.put(namePath, "Duncan");
 		graph.put(emailPath, "duncan.atkinson@nimble.com");
 		
 		PutMethod putRequest = makePut(makeNimbleRequest(graph),urlRestNimble);
 		client.executeMethod(putRequest);
 		
 		GetMethod request = new GetMethod(urlRestNimble);
 		client.executeMethod(request);
 		String responseString = request.getResponseBodyAsString();
 		NimbleResponse nimbleResponse = mapper.readValue(responseString, NimbleResponse.class);
 		NimbleMapGraph graph2 = new NimbleMapGraph();
 		graph2.setGraphObject(nimbleResponse.getData());
 		
 		assertGraphsEqual(graph, namePath, graph2);
 		assertGraphsEqual(graph, emailPath, graph2);
 	}
 
 
 
 
 	private void assertGraphsEqual(NimbleMapGraph graph, String namePath, NimbleMapGraph graph2) {
 		assertEquals(graph.getValue(namePath),graph2.getValue(namePath));
 	}
 
 	private PutMethod makePut(Object nimbleRequest, String uri) throws IOException, JsonGenerationException,
 			JsonMappingException, UnsupportedEncodingException {
 		PutMethod putRequest = new PutMethod(uri);
 		String json = mapper.writeValueAsString(nimbleRequest);
 		RequestEntity requestEntity = new  StringRequestEntity(json,"application/json","UTF-8");
 		putRequest.setRequestEntity(requestEntity);
 		return putRequest;
 	}
 	
 	private NimbleRequest makeNimbleRequest(NimbleMapGraph graph) {
 		NimbleRequest nimbleRequest = new NimbleRequest();
 		return nimbleRequest;
 	}
 	
 }
