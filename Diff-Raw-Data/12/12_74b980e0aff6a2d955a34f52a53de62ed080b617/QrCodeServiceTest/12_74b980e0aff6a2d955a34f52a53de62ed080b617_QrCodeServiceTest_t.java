 package org.offlike.server.service;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.UnsupportedEncodingException;
 
 import org.junit.Test;
 import org.offlike.server.service.UrlBuilder;
 
 public class QrCodeServiceTest {
 
 	@Test
 	public void parseUrl() throws UnsupportedEncodingException {
 		String parseUrl = UrlBuilder.createEncodedLikeURL("ab4711f","Save the whales");
		assertEquals("http%3A%2F%2Fofflike.herokuapp.com%2Flike%2Fab4711f%3Fcampaign_name%3DSave%2Bthe%2Bwhales", parseUrl);
		//assertEquals("http%3A%2F%2Fofflike.herokuapp.com%2Flike%2Fab4711f?campaign_name=Save+the+whales", parseUrl);
 	}
 	
 }
