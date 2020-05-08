 package net.sue445.s3tiger.internal;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Map;
 
 import org.junit.Test;
 import org.slim3.tester.AppEngineTestCase;
 import org.slim3.util.AppEngineUtil;
 
 public class AppEngineWebConfigUtilTest extends AppEngineTestCase{
 
 	@Test
 	public void getSystemProperties() {
 		if(AppEngineUtil.isServer()){
 			return;
 		}
 
 		Map<String, String> actual = AppEngineWebConfigUtil.getSystemProperties();
 
		assertThat(actual.size(), is(2));

		assertHasMapEntry(actual, "java.util.logging.config.file", "WEB-INF/classes/logging.properties");
		assertHasMapEntry(actual, "slim3.cipherGlobalKey", "1234567890123456");
	}
 
	private <K, V> void assertHasMapEntry(Map<K, V> actual, K key, V value) {
 		assertThat(actual.containsKey(key), is(true));
		assertThat(actual.get(key), is(value));
 	}
 
 }
