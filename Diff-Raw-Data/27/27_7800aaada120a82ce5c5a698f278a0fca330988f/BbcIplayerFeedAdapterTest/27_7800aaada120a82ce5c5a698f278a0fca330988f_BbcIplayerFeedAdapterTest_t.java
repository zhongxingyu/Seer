 /* Copyright 2009 British Broadcasting Corporation
    Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.bbc;
 
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.bbc.BbcIplayerFeedAdapter;
 import org.atlasapi.remotesite.synd.SyndicationSource;
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 
 import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  * @author John Ayres (john@metabroadcast.com)
  */
 public class BbcIplayerFeedAdapterTest extends MockObjectTestCase {
 
	private RemoteSiteClient<SyndFeed> feedClient;
	private ContentExtractor<SyndicationSource, Playlist> propertyExtractor;
	private BbcIplayerFeedAdapter adapter;
	private SyndFeed feed = new SyndFeedImpl();
	private SyndicationSource iplayerSource;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		feedClient = mock(RemoteSiteClient.class);
 		propertyExtractor = mock(ContentExtractor.class);
 		adapter = new BbcIplayerFeedAdapter(feedClient, propertyExtractor);
 		iplayerSource = new SyndicationSource(feed, "http://feeds.bbc.co.uk/iplayer/bbc_one/list");
 	}
 	
 	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
 		
 		checking(new Expectations() {{
 			one(feedClient).get("http://feeds.bbc.co.uk/iplayer/bbc_one/list"); will(returnValue(feed));
 			one(propertyExtractor).extract(iplayerSource);
 		}});
 		
 		adapter.fetch("http://feeds.bbc.co.uk/iplayer/bbc_one/list");
 	}
 	
 	public void testCanFetchResourcesForRssUris() throws Exception {
 		
 		assertTrue(adapter.canFetch("http://feeds.bbc.co.uk/iplayer/bbc_one/list"));
 		assertTrue(adapter.canFetch("http://feeds.bbc.co.uk/iplayer/atoz/a/list"));
 		assertTrue(adapter.canFetch("http://feeds.bbc.co.uk/iplayer/atoz/0-9/list"));
 		assertTrue(adapter.canFetch("http://feeds.bbc.co.uk/iplayer/popular/tv"));
 		assertFalse(adapter.canFetch("http://feeds.bbc.co.uk/iplayer/rubbish/tv"));
 		assertTrue(adapter.canFetch("http://feeds.bbc.co.uk/iplayer/bbc_news24/list"));
 		assertFalse(adapter.canFetch("http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml"));
 		assertFalse(adapter.canFetch("http://feeds.bbc.co.uk"));
 		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
 	}
 	
 	
 }
