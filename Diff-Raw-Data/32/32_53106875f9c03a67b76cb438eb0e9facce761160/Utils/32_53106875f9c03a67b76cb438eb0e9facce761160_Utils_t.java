 package eu.trentorise.smartcampus.ac.provider.utils;
 
import java.net.MalformedURLException;
import java.net.URL;

 import javax.annotation.PostConstruct;
 
 import org.owasp.esapi.ESAPI;
 import org.owasp.esapi.Encoder;
 import org.owasp.esapi.errors.IntrusionException;
 import org.owasp.esapi.errors.ValidationException;
 import org.owasp.validator.html.AntiSamy;
 import org.owasp.validator.html.Policy;
 import org.owasp.validator.html.PolicyException;
 import org.owasp.validator.html.ScanException;
 import org.springframework.stereotype.Service;
 
 @Service
 public class Utils {
 
 	private static final String POLICY_FILE = "antisamy-total.xml";
 
 	private Encoder encoder;
 	private AntiSamy sanitizer;
 
 	@PostConstruct
 	@SuppressWarnings("unused")
 	private void init() throws PolicyException {
 		encoder = ESAPI.encoder();
 		sanitizer = new AntiSamy(Policy.getInstance(getClass().getResource(
 				POLICY_FILE)));
 
 	}
 
 	public String sanitize(String s) throws ValidationException,
 			IntrusionException, ScanException, PolicyException {
 
 		s = encoder.canonicalize(s);
 		s = sanitizer.scan(s).getCleanHTML();
 		return s;
 	}

	public String retrieveDomain(String url) throws MalformedURLException {
		return retrieveDomain(new URL(url));
	}

	public String retrieveDomain(URL url) {
		return url.getHost();
	}
 }
