 package net.sue445.s3tiger.internal;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Map;
 
import net.sue445.s3tiger.internal.AppEngineWebConfigUtil;

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
 
		assertThat(actual.size(), is(1));
 
		String key = "java.util.logging.config.file";
 		assertThat(actual.containsKey(key), is(true));
		assertThat(actual.get(key), is("WEB-INF/classes/logging.properties"));
 	}
 
 }
