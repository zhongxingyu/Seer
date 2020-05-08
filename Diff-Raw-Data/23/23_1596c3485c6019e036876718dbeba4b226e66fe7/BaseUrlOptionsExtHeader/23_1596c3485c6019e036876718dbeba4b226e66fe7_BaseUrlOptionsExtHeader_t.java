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
  * @author Jason Mathews, MITRE Corp.
  * Date: 2/20/12 10:45 AM
  */
 public class BaseUrlOptionsExtHeader extends BaseTest {
 
	private static final Logger log = LoggerFactory.getLogger(BaseUrlOptionsExtHeader.class);
 
 	public BaseUrlOptionsExtHeader() {
 		// forces BaseUrlOptions test to keep its HttpResponse object after it executes
 		setProperty(BaseUrlOptions.class, PROP_KEEP_RESPONSE_BOOL, Boolean.TRUE);
 	}
 
 	@NonNull
 	public String getId() {
 		return "6.2.5.4";
 	}
 
 	@Override
 	public boolean isRequired() {
 		return true; // MUST
 	}
 
 	@NonNull
 	public String getName() {
 		return "OPTIONS on HDR baseURL MUST return X-hdata-extensions HTTP header";
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
 
 		if (response.getFirstHeader("X-hdata-extensions") == null) {
 			if (log.isDebugEnabled()) System.out.println("ERROR: Must set required X-hdata-extensions HTTP header in response");
 			setStatus(StatusEnumType.FAILED, "Must set required X-hdata-extensions HTTP header in response");
 			return;
 		}
 		/*
 		6.2.5 OPTIONS
 
 		All implementations MUST support OPTIONS on the baseURL of each HDR and return a status code of 200, along with
 		following HTTP headers:
 
 		 The X-hdata-security HTTP header defined in section of this specification. The security mechanisms defined at the
 		baseURL are applicable to all child resources, i.e. to the entire HDR.
 
 		  An X-hdata-hcp HTTP header that contains a space separated list of the identifiers of the hData Content Profiles
 		supported by this implementation
 
 		 The X-hdata-extensions HTTP header contains a space separated list of the identifiers of the hData extensions
 		supported by this implementation independent of their presence in the root document at baseURL/root.xml (cf. section
 		2.2 in [1] describing the root document format for an explanation of the extensions in a root.xml)
 		*/
 		setStatus(StatusEnumType.SUCCESS);
 	}
 
 }
