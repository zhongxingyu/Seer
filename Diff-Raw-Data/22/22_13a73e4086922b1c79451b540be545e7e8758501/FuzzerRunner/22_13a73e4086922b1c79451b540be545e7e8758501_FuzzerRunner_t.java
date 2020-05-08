 package fuzzer;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
import java.util.logging.Level;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 
 import site.Site;
 
 import attackSurface.AttackSurfaceDiscoverer;
 
 public class FuzzerRunner {
 	private Fuzzer fuzzer;
 	private AttackSurfaceDiscoverer attackSurfaceDiscoverer;
 	private final String userNamesFilePath;
 	private final String passwordsFilePath;
 	
 	
 	public FuzzerRunner(String[] args) throws IOException {
 		String targetURL = args[0];
 		int timeDelay = Integer.parseInt(args[1]);
 		String sensitiveDataFilePath = args[2];
 		String maliciousInputFilePath = args[3];
 		String pageGuessFilePath = args[4];
 		userNamesFilePath = args[5];
 		passwordsFilePath = args[6];
 		
 		WebClient webClient = new TimeDelayWebClient(timeDelay, sensitiveDataFilePath);
 		webClient.setPrintContentOnFailingStatusCode(false);
 		
 		fuzzer = new Fuzzer(webClient, sensitiveDataFilePath, maliciousInputFilePath, userNamesFilePath, passwordsFilePath);
 		attackSurfaceDiscoverer = new AttackSurfaceDiscoverer(webClient, targetURL, pageGuessFilePath);
 	}
 	
 	public void run() throws MalformedURLException, IOException {
 		Site site = attackSurfaceDiscoverer.discoverAttackSurface(userNamesFilePath, passwordsFilePath);
 		attackSurfaceDiscoverer.reportAttackSurface(site);
 		fuzzer.fuzz(site);		
 		
 	}
 	
 	/**
 	 * @param args
 	 *            0- target url
 	 *            1- time delay (can be 0)
 	 *            2- path to sensitive data file
 	 *            3- path to malicious inputs
 	 *            4- path to page guesses
 	 *            5- path to username guesses (0 to use standard auth info)
 	 *            6- path to password guesses (0 to use standard auth info) ***NOTE: args[5]=0 <==> args[6]==0
 	 */			  
 	public static void main(String[] args) throws MalformedURLException,
 			IOException {
		
		//Disable reporting of irrelevant/non-applicable errors and warnings
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http").setLevel(Level.OFF);		
		
 		FuzzerRunner fuzzerRunner = new FuzzerRunner(args);
 		fuzzerRunner.run();
 	}
 	
 	
 	
 }
