 package process;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import vindsiden.VindsidenHttpClient;
 import configuration.Configuration;
 
 public class WeatherFetcherTest {
 
 	private WeatherFetcher fetcher;
 	
 	@Mock
 	private VindsidenHttpClient vindsidenHttpClientMock;
 	
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		fetcher = new WeatherFetcher(Configuration.getConfiguration());
 		fetcher.setHttpClient(vindsidenHttpClientMock);
 	}
 	
 	@Test
 	public void testExecution() throws Exception {		
 		fetcher.execute();
 		
 	}
 
 }
