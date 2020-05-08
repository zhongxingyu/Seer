 package it.polimi.chansonnier.test;
 
 import java.io.IOException;
 
 import org.xml.sax.SAXException;
 
 import junit.framework.TestCase;
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 
 public class AddSongTest extends TestCase {
 	public void testNothing() throws Exception {
 		WebConversation wc = new WebConversation();
 		WebRequest     req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/add" );
 		WebResponse   resp = wc.getResponse( req );
 		assertTrue(resp.getText().contains("Hello from AddServlet"));
 	}
 	
 	public void _testGivenAYouTubeLinkAddsTheRelatedSongToTheIndex() throws Exception {
 		WebResponse resp = addVideoLink("http://www.youtube.com/watch?v=e8w7f0ShtIM");
 		// TODO insert redirect
 		assertTrue(resp.getText().contains("Success"));
 		WebRequest     req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/last" );
 		assertWebPageContains(req, "Beautiful Day", 300000);
 	}
 	
 	public void testGivenAnAddedYouTubeLinkTheSongIsSearchable() throws Exception {
 		String link = "http://www.youtube.com/watch?v=5tK7-OuYfJc";
 		addVideoLink(link);
		// TODO: avoid all errors "index does not exist in data dictionary [test_index]"
 		WebRequest req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/search" );
 		req.setParameter("lyrics", "I walk alone");
 		assertWebPageContains(req, "<Source>youtube</Source><Key>" + link + "</Key>", 300000);
 	}
 	
 	private WebResponse addVideoLink(String link) throws Exception {
 		WebConversation wc = new WebConversation();
 		PostMethodWebRequest	req = new PostMethodWebRequest( "http://localhost:8080/chansonnier/add" );
 		req.setParameter("link", link);
 		WebResponse resp = wc.getResponse(req);
 		return resp;
 	}
 	
 	private void assertWebPageContains(WebRequest req, String text, int timeout) throws InterruptedException, IOException, SAXException {
 		int tries = timeout / 10000;
 		for (int i = 0; i < tries; i++) {
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
