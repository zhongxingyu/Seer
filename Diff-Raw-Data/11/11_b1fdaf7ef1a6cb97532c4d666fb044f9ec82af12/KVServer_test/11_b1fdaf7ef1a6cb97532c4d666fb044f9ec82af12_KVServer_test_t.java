 package edu.berkeley.cs162;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class KVServer_test {
 
 	@Test
 	public void KVServerTest() {
 		KVServer kvServer = new KVServer(5, 5);
 		try{
 			kvServer.put(null, "12");
 			assertTrue(false); //should throw exception
 		} catch(Exception e) {
 			assertTrue(true);
 		}
 		try{
 			kvServer.put("here", null);
 			assertTrue(false); //should throw exception
 		} catch(Exception e) {
 			assertTrue(true);
 		}
 		try{
 			kvServer.get(null);
 			assertTrue(false); //should throw exception
 		} catch(Exception e) {
 			assertTrue(true);
 		}
 		try{
 			kvServer.get("telp");
 			assertTrue(false); //should throw exception
 		} catch(Exception e) {
 			assertTrue(true);
 		}
 		try {
			kvServer.put("here", "12");
 			assertEquals(kvServer.dataStore.get("here"), "12");
 			assertEquals(kvServer.dataCache.get("here"), "12");
 			kvServer.dataCache.del("here");
 			assertEquals(kvServer.dataCache.get("here"), null);
 			String result = kvServer.get("here");
 			assertEquals(kvServer.dataCache.get("here"), "12");
 			assertEquals(result, "12");
 			kvServer.del("here");
 		} catch(Exception e) {
 			fail(e.getMessage());
 		}
 		try {
 			kvServer.get("here");
 			fail("Should throw exception");
 		} catch(Exception e) {
 			assertTrue(true);
 		}
 		
 		
 		
 		
 	}
 }
