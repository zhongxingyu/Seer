 package de.shop.util;
 
 import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
 import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
 import static com.jayway.restassured.config.HttpClientConfig.httpClientConfig;
 import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
 import static de.shop.util.TestConstants.BASEPATH;
 import static de.shop.util.TestConstants.BASEURI;
 import static de.shop.util.TestConstants.PORT;
 import static org.apache.http.client.params.ClientPNames.DEFAULT_HEADERS;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.json.Json;
 import javax.json.JsonBuilderFactory;
 import javax.json.JsonReaderFactory;
 
 import org.apache.http.Header;
 import org.apache.http.message.BasicHeader;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.shrinkwrap.api.Archive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 import com.jayway.restassured.RestAssured;
 
 public abstract class AbstractResourceTest {
	protected static final String USERNAME = "Loya@gmail.com";
	protected static final String PASSWORD = "1234";
	protected static final String USERNAME_ADMIN = "Allan@gmail.com";
	protected static final String PASSWORD_ADMIN = "1234";
 	protected static final String PASSWORD_FALSCH = "falsch";
 	
 	// Sprache und Zeichensatz
 	private static final String CLIENT_LANGUAGE = Locale.GERMAN.getLanguage();
 	private static final String UTF_8 = "UTF-8";
 	
 	private static JsonReaderFactory jsonReaderFactory;    // Fabrik zum Lesen bei GET-Requests
 	private static JsonBuilderFactory jsonBuilderFactory;  // Fabrik zum Schreiben bei POST- und PUT-Requests
 	
 	
 	@Deployment(name = ArchiveBuilder.TEST_WAR, testable = false)  // Die Tests laufen nicht im Container
 	protected static Archive<?> deployment() {
 		return ArchiveBuilder.getInstance().getArchive();
 	}
 	
 	
 	@BeforeClass
 	public static void init() {
 		jsonReaderFactory = Json.createReaderFactory(null);
 		jsonBuilderFactory = Json.createBuilderFactory(null);
 	}
 	
 	@Before
 	public void before() {
 		// Basispfad und Port
 		RestAssured.baseURI = BASEURI;
 		RestAssured.port = PORT;
 		RestAssured.basePath = BASEPATH;
 		
 		final List<Header> httpClientHeaders = new ArrayList<>();
 		httpClientHeaders.add(new BasicHeader("Accept-Language", CLIENT_LANGUAGE));
 		
 		// UTF-8 statt Latin-1
 		RestAssured.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset(UTF_8))
 				                        .decoderConfig(decoderConfig().defaultContentCharset(UTF_8))
 				                        .httpClient(httpClientConfig().setParam(DEFAULT_HEADERS, httpClientHeaders));
 	}
 	
 	
 	@After
 	public void reset() {
 		RestAssured.reset();
 	}
 
 
 	protected static JsonReaderFactory getJsonReaderFactory() {
 		return jsonReaderFactory;
 	}
 
 	protected static JsonBuilderFactory getJsonBuilderFactory() {
 		return jsonBuilderFactory;
 	}
 }
