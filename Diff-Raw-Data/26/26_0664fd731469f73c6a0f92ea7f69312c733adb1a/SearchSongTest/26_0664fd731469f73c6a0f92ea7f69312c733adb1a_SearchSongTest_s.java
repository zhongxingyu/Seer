 /****************************************************************************
  * Copyright (c) 2010 Giorgio Sironi. All rights reserved.
  * This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this 
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  ****************************************************************************/
 package it.polimi.chansonnier.test;
 
 import it.polimi.chansonnier.fixtures.Fixtures;
 
 import java.io.InputStream;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebRequest;
 
 public class SearchSongTest extends AcceptanceTest {	
 	public void testGivenAnAddedYouTubeLinkTheSongIsSearchable() throws Exception {
 		InputStream beautifulDayFlv = Fixtures.class.getResourceAsStream("beautifulday.flv");
 		fixtureManager.addSong("http://www.youtube.com/watch?v=e8w7f0ShtIM", beautifulDayFlv, "U2 - Beautiful Day (with Lyrics)");
 		
 		WebRequest     req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/last" );
 		assertWebPageContains(req, "http://www.youtube.com/watch?v=e8w7f0ShtIM", 20000);
 		Thread.sleep(10000);
 		
 		selenium.open("/chansonnier/index.html");
 		wrapped.verifyTrue(selenium.isTextPresent("Beautiful Day"));
 		selenium.click("link=happiness");
        selenium.waitForPageToLoad("50000");
 		wrapped.verifyTrue(selenium.isTextPresent("(x) emotion:happiness"));
 		wrapped.verifyTrue(selenium.isTextPresent("The heart is a bloom"));
     }
 }
