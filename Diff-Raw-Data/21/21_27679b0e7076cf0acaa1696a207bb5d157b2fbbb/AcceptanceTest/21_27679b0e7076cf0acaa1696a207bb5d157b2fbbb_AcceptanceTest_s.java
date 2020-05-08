 package it.polimi.chansonnier.test;
 
 import java.io.IOException;
 
 import org.xml.sax.SAXException;
 
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 
 import junit.framework.TestCase;
 
 public abstract class AcceptanceTest extends TestCase {
 	protected WebResponse addVideoLink(String link) throws Exception {
 		WebConversation wc = new WebConversation();
 		PostMethodWebRequest	req = new PostMethodWebRequest( "http://localhost:8080/chansonnier/add" );
 		req.setParameter("link", link);
 		WebResponse resp = wc.getResponse(req);
 		return resp;
 	}
 
 	protected void assertWebPageContains(WebRequest req, String text,
 			int timeout) throws InterruptedException, IOException, SAXException {
 				int tries = timeout / 10000;
 				for (int i = 0; i < tries; i++) {
 			        System.out.println("Try " + i + " for text ''" + text + "'");
 					Thread.sleep(10000);
 					WebConversation wc = new WebConversation();
 					WebResponse   resp = wc.getResponse( req );
 					System.out.println(resp.getText());
 					if (resp.getText().contains(text)) {
 						assertTrue(true);
 						return;
 					}
 				}
				fail("After " + timeout + "milliseconds of waiting, the web page does not contain the prescribed text.");
 			}
 
}
