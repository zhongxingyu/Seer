 package org.mitre.rhex;
 
 import edu.umd.cs.findbugs.annotations.NonNull;
 import org.apache.http.HttpResponse;
 import org.mitre.test.BaseTest;
 import org.mitre.test.TestException;
 import org.mitre.test.TestUnit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
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
  * Status Code: 200
  *
  * @author Jason Mathews, MITRE Corp.
  * Date: 2/20/12 10:45 AM
  */
 public class BaseUrlOptionsSecurityHeader extends BaseTest {
 
	private static final Logger log = LoggerFactory.getLogger(BaseUrlRootXmlPost.class);
 
 	public BaseUrlOptionsSecurityHeader() {
 		// forces BaseUrlOptions test to keep its HttpResponse object after it executes
 		setProperty(BaseUrlOptions.class, PROP_KEEP_RESPONSE_BOOL, Boolean.TRUE);
 	}
 
 	@NonNull
 	public String getId() {
 		return "6.2.5.2";
 	}
 
 	@Override
 	public boolean isRequired() {
 		return true; // MUST
 	}
 
 	@NonNull
 	public String getName() {
 		return "OPTIONS on HDR baseURL MUST return X-hdata-security HTTP header";
 	}
 
 	@NonNull
 	public List<Class<? extends TestUnit>> getDependencyClasses() {
 		return Collections.<Class<? extends TestUnit>> singletonList(BaseUrlOptions.class); // 6.2.5.1
 	}
 
 	public void execute() throws TestException {
 		// pre-conditions: for this test to be executed the prerequisite test BaseUrlOptions must have passed
 		// with 200 HTTP response
 		TestUnit baseTest = getDependency(BaseUrlOptions.class);
 		if (baseTest == null) {
 			// assertion failed: this should never be null
 			log.error("Failed to retrieve prerequisite test");
 			setStatus(StatusEnumType.SKIPPED, "Failed to retrieve prerequisite test");
 			return;
 		}
 		
 		HttpResponse response = ((BaseUrlOptions)baseTest).getResponse();
 		if (response == null) {
 			log.error("Failed to retrieve prerequisite test");
 			setStatus(StatusEnumType.SKIPPED, "Failed to retrieve prerequisite test results");
 			return;
 		}
 
 		if (response.getFirstHeader("X-hdata-security") == null) {
 			if (log.isDebugEnabled()) System.out.println("ERROR: Must set required X-hdata-security HTTP header in response");
 			setStatus(StatusEnumType.FAILED, "Must set required X-hdata-security HTTP header in response");
 			return;
 		}
 		/*
 		6.2.5 OPTIONS
 
 		All implementations MUST support OPTIONS on the baseURL of each HDR and return a status code of 200, along with
 		following HTTP headers:
 
 		 The X-hdata-security HTTP header defined in section of this specification. The security mechanisms defined at the
 		baseURL are applicable to all child resources, i.e. to the entire HDR.
 
 		X-hdata-security: http://openid.net/connect/
 
 		  An X-hdata-hcp HTTP header that contains a space separated list of the identifiers of the hData Content Profiles
 		supported by this implementation
 
 		 The X-hdata-extensions HTTP header contains a space separated list of the identifiers of the hData extensions
 		supported by this implementation independent of their presence in the root document at baseURL/root.xml (cf. section
 		2.2 in [1] describing the root document format for an explanation of the extensions in a root.xml)
 		*/
 		setStatus(StatusEnumType.SUCCESS);
 	}
 
 }
