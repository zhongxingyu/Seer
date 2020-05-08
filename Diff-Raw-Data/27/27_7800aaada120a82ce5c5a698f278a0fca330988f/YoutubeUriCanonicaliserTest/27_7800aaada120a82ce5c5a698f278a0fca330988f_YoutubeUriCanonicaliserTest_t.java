 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.youtube;
 
 import org.jmock.integration.junit3.MockObjectTestCase;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class YoutubeUriCanonicaliserTest extends MockObjectTestCase {
 
	private YoutubeUriCanonicaliser canonicaliser;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		canonicaliser = new YoutubeUriCanonicaliser();
 	}
 
 	public void testThatTheAdapterCanExtractVideoIdFromYoutubeUri() throws Exception {
 		check("http://www.youtube.com/watch?v=uOH0o2DQDco&feature=channel", "uOH0o2DQDco");
 		check("http://www.youtube.com/watch?v=xyCNqsbVPYM&feature=autoshare_twitter", "xyCNqsbVPYM");
 	}
 
 	public void testCanGenerateCurieForUri() throws Exception {
 		assertEquals("yt:uOH0o2DQDco", YoutubeUriCanonicaliser.curieFor("http://www.youtube.com/watch?v=uOH0o2DQDco&feature=channel"));
 		assertEquals("yt:uOH0o2DQDco", YoutubeUriCanonicaliser.curieFor("http://www.youtube.com/watch?v=uOH0o2DQDco"));
 	}
 
 	private void check(String alternate, final String expected) {
 		final String canonicalUri = "http://www.youtube.com/watch?v=" + expected;
 		assertEquals(canonicalUri, canonicaliser.canonicalise(alternate));
 	}
 }
