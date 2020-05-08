 package net.anyflow.menton;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
import io.netty.handler.codec.http.HttpResponseStatus;
 import io.netty.handler.codec.http.QueryStringDecoder;
 
 import java.net.URI;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 
 import net.anyflow.menton.http.HttpClient;
import net.anyflow.menton.http.HttpResponse;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author anyflow
  */
 public class UtilityTest {
 
 	@SuppressWarnings("unused")
 	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UtilityTest.class);
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testQueryStringDecoderParameter() throws Exception {
 
 		String queryString = "/asdfasd/qwerew";
 		Map<String, List<String>> parameters = (new QueryStringDecoder(queryString)).parameters();
 
 		assertThat(parameters, is(not(nullValue())));
 	}
 	
 	@Test
 	public void testURIport() throws Exception {
 
 		String uriString = "http://10.0.0.1/getporttest";
 		HttpClient client = new HttpClient(uriString);
 
 		assertThat(client.httpRequest().uri().getPort(), is(80));
 	}
 }
