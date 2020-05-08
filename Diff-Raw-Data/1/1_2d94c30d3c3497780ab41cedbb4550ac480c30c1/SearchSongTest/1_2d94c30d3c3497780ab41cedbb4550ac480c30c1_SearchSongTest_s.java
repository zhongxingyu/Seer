 /****************************************************************************
  * Copyright (c) 2010 Giorgio Sironi. All rights reserved.
  * This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this 
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  ****************************************************************************/
 package it.polimi.chansonnier.test;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebRequest;
 
 import it.polimi.chansonnier.fixtures.Fixtures;
 
 import java.io.File;
 import java.io.InputStream;
 
 //import org.eclipse.osgi.framework.adaptor.core.DefaultClassLoader;
 
 public class SearchSongTest extends AcceptanceTest {	
 	public void testGivenAnAddedYouTubeLinkTheSongIsSearchable() throws Exception {
 		InputStream beautifulDayFlv = Fixtures.class.getResourceAsStream("beautifulday.flv");
 		fixtureManager.addSong("http://www.youtube.com/watch?v=e8w7f0ShtIM", beautifulDayFlv, "U2 - Beautiful Day (with Lyrics)");
 		Thread.sleep(5000);
 		//WebResponse resp = addVideoLink("http://www.youtube.com/watch?v=e8w7f0ShtIM");
 		// TODO insert redirect
 		//assertTrue(resp.getText().contains("Success"));
 		
 		WebRequest     req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/last" );
 		// TODO: avoid all errors "index does not exist in data dictionary [test_index]"
 		assertWebPageContains(req, "http://www.youtube.com/watch?v=e8w7f0ShtIM", 20000);
 		
 		selenium.open("/chansonnier/index.html");
		Thread.sleep(2000);
 		wrapped.verifyTrue(selenium.isTextPresent("Beautiful Day"));
 		/*
 		req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/index.html" );
 		WebConversation conn = new WebConversation();
 		WebResponse resp = conn.getResponse(req);
 		for (HTMLElement elem: resp.getElementsByTagName("h2")) {
 			System.out.println(elem.getText());
 		}
 		*/
         // TODO: make it case insensitive
 //        assertSongsListContainsSongTitle(res, "Beautiful Day");
 //		assertSongsListContainsSongArtist(res, "U2");
 //		assertSongsListContainsSongLyrics(res, "The heart is a bloom");
 //		assertSongsListContainsSongEmotion(res, "happiness");
 
 /*
         String hero = "http://www.youtube.com/watch?v=owTmJrtD7g8";
 		addVideoLink(hero);
 		req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/search" );
 		req.setParameter("lyrics", "Would you dance");
 		WebResponse response = assertWebPageContains(req, hero, 300000);
 		assertSongsListContainsSongTitle(response, "Hero");
 		assertSongsListContainsSongArtist(response, "Enrique Iglesias");
 		assertSongsListContainsSongLyrics(response, "if I asked you to dance?");
 		assertSongsListContainsSongImage(response, "<img src=\"attachment?name=Image1&id=" + hero + "\" />");
         assertWebPageNotContains(response, "Beautiful Day");
 
         // XML format
 		req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/search" );
 		req.setParameter("lyrics", "Would you dance");
 		req.setParameter("format", "xml");
 		WebConversation wc = new WebConversation();
 		resp = wc.getResponse( req );
         System.out.println(resp.getText());
  */
 //        NodeList songs = (NodeList) xpath.evaluate("//song", dom, XPathConstants.NODESET);
 //        assertEquals(1, songs.getLength());
 //        String title = (String) xpath.evaluate("//song/title/text()", dom, XPathConstants.STRING);
 //        assertEquals("Hero", title);
 //        String artist = (String) xpath.evaluate("//song/artist/text()", dom, XPathConstants.STRING);
 //        assertEquals("Enrique Iglesias", artist);
 //        String emotion = (String) xpath.evaluate("//song/emotion/text()", dom, XPathConstants.STRING);
 //        assertEquals("anger", emotion);
 //        String lyrics = (String) xpath.evaluate("//songs/song/lyrics/text()", dom, XPathConstants.STRING);
 //        assertTrue(lyrics.contains("if I asked you to dance"));
     }
 }
