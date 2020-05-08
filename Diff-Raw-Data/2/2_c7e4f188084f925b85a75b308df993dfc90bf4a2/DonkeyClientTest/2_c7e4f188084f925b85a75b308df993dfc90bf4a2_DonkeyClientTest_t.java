 package com.richitec.donkey.client;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class DonkeyClientTest {
 	
 	private static DonkeyClient client;
 	private static String baseUri = "http://127.0.0.1:8080/donkey/conference";
 	private static String appId = "26287092";
 	private static String appKey = "ud4872uu";
 	private static String requestId = "huuguanghui";
 	
 	private static String confId = "1234";
 	
 	@BeforeClass
 	public static void setUp(){
 		client = new DonkeyClient(baseUri, appId, appKey);
 	}
 
 	@Test
 	public void createNoControlConference(){
		DonkeyHttpResponse response = client.createNoControlConference(confId, null, null, requestId);
 		System.out.println(response.getEntityAsString());
 	}
 	
 	@Test
 	public void destroyConference(){
 		DonkeyHttpResponse response = client.destroyConference(confId, requestId);
 		System.out.println(response.getEntityAsString());
 	}
 	
 	@Test
 	public void testJSONArray() throws JSONException{
 		String jsonStr = "[\"abc\", \"123\",]";
 		JSONArray json = new JSONArray(jsonStr);
 		System.out.println(json.toString());
 	}
 }
