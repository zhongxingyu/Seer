 package ch.hsr.objectCaching.util;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.hsr.objectCaching.dto.MethodCall;
 
 public class TestConcurrencyControl {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void testUpdateReadVersionOfClient() {
 		ConcurrencyControl concurrencyControl = new ConcurrencyControl();
 		MethodCall getBalanceMethod = new MethodCall();
 		getBalanceMethod.setMethodName("getBalance");
 		HashMap<Integer, Integer> writeMap = new HashMap<Integer, Integer>();
 		Integer objectID = 23;
 		Integer writeVersion = 11;
 		getBalanceMethod.setObjectID(objectID);
 		String clientIP = "123";
 		getBalanceMethod.setClientIp(clientIP);
 		writeMap.put(objectID , writeVersion );
 		concurrencyControl.setWriteMap(writeMap );
 		HashMap<String, Integer> readMap = new HashMap<String, Integer>();
 		concurrencyControl.setReadMap(readMap );
 		concurrencyControl.updateReadVersionOfClient(getBalanceMethod);
 		String versionKey = clientIP.concat(String.valueOf(objectID));
		assertEquals(writeVersion, readMap.get(versionKey));	
 	}
 
 }
