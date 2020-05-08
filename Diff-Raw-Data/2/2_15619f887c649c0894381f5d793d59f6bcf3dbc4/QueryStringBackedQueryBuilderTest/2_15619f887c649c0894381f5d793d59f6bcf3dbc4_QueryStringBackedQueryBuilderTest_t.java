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
 
 package org.uriplay.query.content.parser;
 
 import static org.uriplay.content.criteria.Queries.after;
 import static org.uriplay.content.criteria.Queries.and;
 import static org.uriplay.content.criteria.Queries.before;
 import static org.uriplay.content.criteria.Queries.beginning;
 import static org.uriplay.content.criteria.Queries.equalTo;
 import static org.uriplay.content.criteria.Queries.greaterThan;
 import static org.uriplay.content.criteria.Queries.lessThan;
 import static org.uriplay.content.criteria.Queries.searchFor;
 import static org.uriplay.content.criteria.attribute.Attributes.BRAND_GENRE;
 import static org.uriplay.content.criteria.attribute.Attributes.BRAND_TAG;
 import static org.uriplay.content.criteria.attribute.Attributes.BRAND_TITLE;
 import static org.uriplay.content.criteria.attribute.Attributes.BROADCAST_TRANSMISSION_TIME;
 import static org.uriplay.content.criteria.attribute.Attributes.EPISODE_POSITION;
 import static org.uriplay.content.criteria.attribute.Attributes.ITEM_GENRE;
 import static org.uriplay.content.criteria.attribute.Attributes.ITEM_PUBLISHER;
 import static org.uriplay.content.criteria.attribute.Attributes.ITEM_TAG;
 import static org.uriplay.content.criteria.attribute.Attributes.ITEM_TITLE;
 import static org.uriplay.content.criteria.attribute.Attributes.LOCATION_AVAILABLE;
 import static org.uriplay.content.criteria.attribute.Attributes.LOCATION_TRANSPORT_TYPE;
 import static org.uriplay.content.criteria.attribute.Attributes.PLAYLIST_GENRE;
 import static org.uriplay.content.criteria.attribute.Attributes.PLAYLIST_TAG;
 import static org.uriplay.content.criteria.attribute.Attributes.PLAYLIST_TITLE;
 import static org.uriplay.content.criteria.attribute.Attributes.VERSION_DURATION;
 
 import java.util.Map;
 
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.joda.time.DateTime;
 import org.uriplay.content.criteria.ContentQuery;
 import org.uriplay.media.TransportType;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Playlist;
 
 import com.google.common.collect.Maps;
 
 public class QueryStringBackedQueryBuilderTest extends MockObjectTestCase {
 	
 	public void testTitleEquality() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		
 		params = Maps.newHashMap();
 		params.put("item.title", new String[] { "bob" });
 		check(params, searchFor(ITEM_TITLE, "bob"));
 		
 		params = Maps.newHashMap();
 		params.put("playlist.title", new String[] { "bob" });
 		check(params, searchFor(PLAYLIST_TITLE, "bob"));
 		
 		params = Maps.newHashMap();
 		params.put("brand.title", new String[] { "bob" });
 		check(params, searchFor(BRAND_TITLE, "bob"));
 		
 	}
 	
 	public void testTitleSearch() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title-search", new String[] { "est" });
 		check(params, searchFor(ITEM_TITLE, "est"));
 	}
 	
 	
 	public void testAvailability() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("location.available", new String[] { "true" });
 		check(params, equalTo(LOCATION_AVAILABLE, true));
 		
 		params = Maps.newHashMap();
 		params.put("location.available", new String[] { "false" });
 		check(params, equalTo(LOCATION_AVAILABLE, false));
 	}
 	
 	public void testGenreEquality() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 
 		params = Maps.newHashMap();
 		params.put("item.genre", new String[] { "bob" });
 		check(params, equalTo(ITEM_GENRE, "http://uriplay.org/genres/uriplay/bob"));
 		
 		params = Maps.newHashMap();
         params.put("genre", new String[] { "bob" });
         check(params, equalTo(ITEM_GENRE, "http://uriplay.org/genres/uriplay/bob"), Item.class);
 
 		params = Maps.newHashMap();
 		params.put("genre", new String[] { "bob" });
         check(params, equalTo(PLAYLIST_GENRE, "http://uriplay.org/genres/uriplay/bob"), Playlist.class);
         
         params = Maps.newHashMap();
         params.put("genre", new String[] { "bob" });
         check(params, equalTo(BRAND_GENRE, "http://uriplay.org/genres/uriplay/bob"), Brand.class);
 	}
 	
 	public void testTagEquality() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		
 		params = Maps.newHashMap();
 		params.put("item.tag", new String[] { "bob" });
 		check(params, equalTo(ITEM_TAG, "http://uriplay.org/tags/bob"));
 		
 		params = Maps.newHashMap();
         params.put("tag", new String[] { "bob" });
         check(params, equalTo(ITEM_TAG, "http://uriplay.org/tags/bob"), Item.class);
         
         params = Maps.newHashMap();
         params.put("tag", new String[] { "bob" });
         check(params, equalTo(PLAYLIST_TAG, "http://uriplay.org/tags/bob"), Playlist.class);
         
         params = Maps.newHashMap();
         params.put("tag", new String[] { "bob" });
         check(params, equalTo(BRAND_TAG, "http://uriplay.org/tags/bob"), Brand.class);
 	}
 	
 	public void testTitleBeginning() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title-beginning", new String[] { "a" });
 		check(params, beginning(ITEM_TITLE, "a"));
 	}
 	
 	public void testDurationEquals() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 
 		params = Maps.newHashMap();
 		params.put("version.duration", new String[] { "10" });
 		check(params, equalTo(VERSION_DURATION, 10));
 	}
 	
 	public void testTitleAndPublisherEqualityConjunction() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title", new String[] { "bob" });
 		params.put("item.publisher", new String[] { "bbc" });
 		check(params, and(equalTo(ITEM_PUBLISHER, "bbc"), searchFor(ITEM_TITLE, "bob")));
 	}
 
 	public void testTransportType() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
		params.put("location.transportType", new String[] { "link" });
 		check(params, equalTo(LOCATION_TRANSPORT_TYPE, TransportType.LINK));
 	}
 	
 	public void testTitleAndPublisherEqualityDisjunction() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.publisher", new String[] { "bbc,channel4" });
 		check(params, equalTo(ITEM_PUBLISHER, "bbc", "channel4"));
 	}
 	
 	public void testTitleContainsConjunction() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title-search", new String[] { "bob", "vic" });
 		check(params, and(searchFor(ITEM_TITLE, "bob"), searchFor(ITEM_TITLE, "vic")));
 	}
 	
 	public void testDurationGreaterThan() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("version.duration-greaterThan", new String[] {"10"});
 		check(params, greaterThan(VERSION_DURATION, 10));
 	}
 	
 	public void testDurationLessThan() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("version.duration-lessThan", new String[] {"10"});
 		check(params, lessThan(VERSION_DURATION, 10));
 	}
 	
 	public void testTransmittedAfter() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("broadcast.transmissionTime-after", new String[] {"101"});
 		check(params, after(BROADCAST_TRANSMISSION_TIME, new DateTime(101L)));
 	}
 	
 	public void testTransmittedBefore() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("broadcast.transmissionTime-before", new String[] {"101"});
 		check(params, before(BROADCAST_TRANSMISSION_TIME, new DateTime(101L)));
 	}
 	
 	public void testInvalidAttribute() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("foo", new String[] {"101"});
 		try {
 			new QueryStringBackedQueryBuilder().build(params, Item.class);
 			fail();
 		} catch (IllegalArgumentException e) {
 			// expected
 		}
 	}
 	
 	public void testUnknownOperator() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("title-haltingproblemsolver", new String[] { "est" });
 		try {
 			new QueryStringBackedQueryBuilder().build(params, Item.class);
 			fail("Exception should have been thrown");
 		} catch (IllegalArgumentException e){
 			assertTrue(e.getMessage().contains("Unknown operator"));
 		}
 	}
 	
 	public void testIgnoringParameters() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title", new String[] { "bob" });
 		params.put("foo", new String[] { "bob2" });
 		
 		ContentQuery query = 
 			new QueryStringBackedQueryBuilder()
 			.withIgnoreParams("foo")
 			.build(params, Item.class);
 		
 		assertEquals(searchFor(ITEM_TITLE, "bob"), query);
 	}
 	
 	public void testAliases() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		check(params, searchFor(ITEM_TITLE, "bob"), Item.class);
 		
 		params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		check(params, searchFor(BRAND_TITLE, "bob"), Brand.class);
 		
 		params = Maps.newHashMap();
 		params.put("available", new String[] { "true" });
 		check(params, equalTo(LOCATION_AVAILABLE, true), Item.class);
 		
 		params = Maps.newHashMap();
 		params.put("position", new String[] { "10" });
 		check(params, equalTo(EPISODE_POSITION, 10), Item.class);
 	}
 
 	private void check(Map<String, String[]> params, ContentQuery expected) {
 		check(params, expected, Description.class);
 	}
 	
 	private void check(Map<String, String[]> params, ContentQuery expected, Class<? extends Description> type) {
 		ContentQuery query = new QueryStringBackedQueryBuilder().build(params, type);
 		assertEquals(expected, query);
 	}
 }
