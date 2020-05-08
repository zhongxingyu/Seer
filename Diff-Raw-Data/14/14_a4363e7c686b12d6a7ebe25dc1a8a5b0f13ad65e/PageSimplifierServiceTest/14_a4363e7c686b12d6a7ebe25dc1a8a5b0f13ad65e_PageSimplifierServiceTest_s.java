 package mdettlaff.mobilemachine.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import mdettlaff.mobilemachine.repository.WebpageRepository;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class PageSimplifierServiceTest {
 
 	private PageSimplifierService service;
 
 	private WebpageRepository repository;
 	private HttpService httpService;
 
 	@Before
 	public void setUp() {
 		repository = mock(WebpageRepository.class);
 		httpService = mock(HttpService.class);
 		service = new PageSimplifierService(repository, httpService);
 	}
 
 	@Test
	public void testDownloadComics() throws Exception {
 		String url = "http://tvtropes.org";
 		// Test
 		String result = service.simplify(url, 0);
 		assertEquals("Hejka, <b>Zbyszek</b>", result);
 	}
 }
