 package org.mitre.rhex;
 
 import edu.umd.cs.findbugs.annotations.NonNull;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpOptions;
 import org.mitre.test.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * 6.2.5 OPTIONS
  *
  * The OPTIONS operation on the baseURL is per [8], section 9.2, intended to return communications options to the clients.
  * Within the context of this specification, OPTIONS is used to indicate which security mechanisms are available for a given
  * baseURL and a list of hData content profiles supported by this implementation. All implementations MUST support
  * OPTIONS on the baseURL of each HDR and return a status code of 200, along with:
  * X-hdata-security, X-hdata-hcp, and X-hdata-extensions HTTP headers. <P>
  *
  * The server MAY include additional HTTP headers. The response SHOULD NOT include an HTTP body. The client
  * MUST NOT use the Max-Forward header when requesting the security mechanisms for a given HDR. <P>
  *
  * Implied: If there is no HDR at the base URL, the server SHOULD return a 404 - Not found status code.
  *
  * Status Code: 200, [404]
  *
  * @author Jason Mathews, MITRE Corp.
  * Date: 2/20/12 10:45 AM
  */
 public class BaseUrlOptionsNotFound extends BaseTest {
 
	private static final Logger log = LoggerFactory.getLogger(BaseUrlOptionsNotFound.class);
 
 	@NonNull
 	public String getId() {
 		return "6.2.5.7";
 	}
 
 	@Override
 	public boolean isRequired() {
 		return false; // implied
 	}
 
 	@NonNull
 	public String getName() {
 		return "OPTIONS operation on non-existent HDR baseURL SHOULD return 404";
 	}
 
 	@NonNull
 	public List<Class<? extends TestUnit>> getDependencyClasses() {
 		return Collections.emptyList();
 	}
 
 	public void execute() throws TestException {
 		final Context context = Loader.getInstance().getContext();
 		URI baseURL = context.getPropertyAsURI("invalidBaseURL");
 		// test pre-conditions
 		if (baseURL == null) {
 			setStatus(StatusEnumType.SKIPPED, "Failed to specify valid invalidBaseURL property in configuration");
 			return;
 		}
 		if (log.isDebugEnabled()) {
 			System.out.println("\nOPTIONS URL: " + baseURL);
 		}
 		HttpClient client = context.getHttpClient();
 		try {
 			HttpOptions req = new HttpOptions(baseURL);
 			HttpResponse response = context.executeRequest(client, req);
 			int code = response.getStatusLine().getStatusCode();
             boolean dumpBody = false;
             if (code != 404) {
 				if (log.isDebugEnabled()) dumpBody = true;
 				setStatus(StatusEnumType.FAILED, "Expected 404 HTTP status code but was: " + code);
 			} else {
 				setStatus(StatusEnumType.SUCCESS);
 			}
             dumpResponse(req, response, dumpBody);
 		} catch (IOException e) {
 			throw new TestException(e);
 		} finally {
 			client.getConnectionManager().shutdown();
 		}
 	}
 
 }
