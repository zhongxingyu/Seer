 package net.sue445.s3tiger.inner;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Map;
 
 import org.junit.Test;
 import org.slim3.tester.AppEngineTestCase;
 
 public class AppEngineWebConfigUtilTest extends AppEngineTestCase{
 
 	@Test
 	public void getSystemProperties() {
 		Map<String, String> actual = AppEngineWebConfigUtil.getSystemProperties();
 
 		assertThat(actual.size(), is(1));
 
 		String key = "java.util.logging.config.file";
 		assertThat(actual.containsKey(key), is(true));
 		assertThat(actual.get(key), is("WEB-INF/classes/logging.properties"));
 	}
 
 }
