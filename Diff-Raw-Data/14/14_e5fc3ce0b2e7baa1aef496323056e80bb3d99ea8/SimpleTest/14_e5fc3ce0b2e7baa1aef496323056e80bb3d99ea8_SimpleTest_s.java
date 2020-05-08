 package com.sumilux.idme.sdk;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertNotNull;
 import org.junit.Test;
 
 import com.sumilux.ssi.client.Idme;
 import com.sumilux.ssi.client.IdmeException;
 import com.sumilux.ssi.client.json.JSONException;
 import com.sumilux.ssi.client.json.JSONObject;
 
 
 public class SimpleTest {
 
 	@Test
 	public void testIsMatchAppNameAndSecret() {
 		String token = "dummy";
 		try {
 			Idme idme = new Idme(token);
 			assertTrue(idme.isMatchAppNameAndSecret("SSISample", "9ccf50d91d8c4f0cbb4742131e5fe13a"));
 		} catch (IdmeException e) {
 			e.printStackTrace();
 			assertTrue(false);
 		}
 	}
 	
 	@Test
 	public void TestAllocateTestUser() {
 		try {
 			String token = Idme.allocateTestUser();
 			Idme idme = new Idme(token);
 			JSONObject jo = idme.getUserProfile();
 			assertNotNull(jo);
 			try {
				assertTrue(jo.getJSONArray("emails").length() > 0);
 			} catch (JSONException e) {
 				assertTrue(false);
 				e.printStackTrace();
 			}
 			System.out.println(jo);
 		} catch (IdmeException e) {
 			e.printStackTrace();
 			assertTrue(false);
 		}
 	}
 }
