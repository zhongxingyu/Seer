 package dk.braintrust.os.logger;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.couchbase.client.CouchbaseClient;
 
 
 public class TestCouchBaseLogAppender {
 	static Logger log = Logger.getLogger(TestCouchBaseLogAppender.class);
 	private static String hosts = "localhost";
 	private static int port = 8092;
 	private static String password = "";
 	private static String defaultMetadataBucket = "default";
 	private static CouchbaseClient client = null;
 	private static List<URI> uris = new LinkedList<URI>();
 	
 	@BeforeClass
 	public static void testSetup() throws IOException {
 		//uris.add(URI.create("http://" + hosts + ":" + port + "/pools"));
 		//client = new CouchbaseClient(uris, defaultMetadataBucket, password); 
 	}
 	
 	@Test
 	public void testErrorLogger() {
		log.error("This is an error");
		log.error("This is an error with a stack!", new StackOverflowError("Craaaap"));
 	}
 	
 	@Test
	void testInfoLogger() {
		log.info("This is a info message");
		log.info("This is a info message with a embedded message", new Throwable("Throw some tomatoes after couchbase!"));
 	}
 	
 }
