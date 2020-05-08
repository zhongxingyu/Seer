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
 
 package org.atlasapi.query.content.parser;
 
 
 import static org.atlasapi.content.criteria.ContentQueryBuilder.query;
 import static org.atlasapi.content.criteria.attribute.Attributes.*;
 
 import java.util.Map;
 
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.content.criteria.ContentQueryBuilder;
 import org.atlasapi.content.criteria.attribute.Attributes;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
 import org.atlasapi.query.content.parser.WebProfileDefaultQueryAttributesSetter;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class QueryStringBackedQueryBuilderTest extends MockObjectTestCase {
 	
 	private final QueryStringBackedQueryBuilder builder = new QueryStringBackedQueryBuilder();
 	
 	public void testTitleEquality() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		
 		params = Maps.newHashMap();
 		params.put("item.title", new String[] { "bob" });
 		check(params, query().searchFor(ITEM_TITLE, "bob"));
 		
 		params = Maps.newHashMap();
 		params.put("playlist.title", new String[] { "bob" });
 		check(params, query().searchFor(PLAYLIST_TITLE, "bob"));
 		
 		params = Maps.newHashMap();
 		params.put("brand.title", new String[] { "bob" });
 		check(params, query().searchFor(BRAND_TITLE, "bob"));
 		
 	}
 	
 	public void testAvailableCountrySearch() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		
 		params = Maps.newHashMap();
 		params.put("availableCountries", new String[] { "gb" });
 		check(params, query().equalTo(POLICY_AVAILABLE_COUNTRY, Countries.GB.code(), Countries.ALL.code()));
 		
 
 		params = Maps.newHashMap();
 		params.put("availableCountries", new String[] { "all" });
 		check(params, query().equalTo(POLICY_AVAILABLE_COUNTRY, Countries.ALL.code()));
 	}
 	
 	public void testTitleSearch() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title-search", new String[] { "est" });
 		check(params, query().searchFor(ITEM_TITLE, "est"));
 	}
 	
 	
 	public void testAvailability() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("location.available", new String[] { "true" });
 		check(params, query().equalTo(LOCATION_AVAILABLE, true));
 		
 		params = Maps.newHashMap();
 		params.put("location.available", new String[] { "false" });
 		check(params, query().equalTo(LOCATION_AVAILABLE, false));
 	
 	}
 	
 	public void testGenreEquality() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 
 		params = Maps.newHashMap();
 		params.put("item.genre", new String[] { "bob" });
 		check(params, query().equalTo(ITEM_GENRE, "http://ref.atlasapi.org/genres/atlas/bob"));
 		
 		params = Maps.newHashMap();
         params.put("genre", new String[] { "bob" });
         check(params, query().equalTo(ITEM_GENRE, "http://ref.atlasapi.org/genres/atlas/bob"), Item.class);
 
 		params = Maps.newHashMap();
 		params.put("genre", new String[] { "bob" });
         check(params, query().equalTo(PLAYLIST_GENRE, "http://ref.atlasapi.org/genres/atlas/bob"), Playlist.class);
         
         params = Maps.newHashMap();
         params.put("genre", new String[] { "bob" });
         check(params, query().equalTo(BRAND_GENRE, "http://ref.atlasapi.org/genres/atlas/bob"), Brand.class);
 	}
 	
 	public void testTagEquality() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		
 		params = Maps.newHashMap();
 		params.put("item.tag", new String[] { "bob" });
 		check(params, query().equalTo(ITEM_TAG, "http://ref.atlasapi.org/tags/bob"));
 		
 		params = Maps.newHashMap();
         params.put("tag", new String[] { "bob" });
         check(params, query().equalTo(ITEM_TAG, "http://ref.atlasapi.org/tags/bob"), Item.class);
         
         params = Maps.newHashMap();
         params.put("tag", new String[] { "bob" });
         check(params, query().equalTo(PLAYLIST_TAG, "http://ref.atlasapi.org/tags/bob"), Playlist.class);
         
         params = Maps.newHashMap();
         params.put("tag", new String[] { "bob" });
         check(params, query().equalTo(BRAND_TAG, "http://ref.atlasapi.org/tags/bob"), Brand.class);
 	}
 	
 	public void testTitleBeginning() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title-beginning", new String[] { "a" });
 		check(params, query().beginning(ITEM_TITLE, "a"));
 	}
 	
 	public void testDurationEquals() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 
 		params = Maps.newHashMap();
 		params.put("version.duration", new String[] { "10" });
 		check(params, query().equalTo(VERSION_DURATION, 10));
 	}
 	
 	public void testTitleAndPublisherEqualityConjunction() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title", new String[] { "bob" });
 		params.put("item.publisher", new String[] { "bbc" });
 		check(params, query().equalTo(ITEM_PUBLISHER, "bbc").searchFor(ITEM_TITLE, "bob"));
 	}
 
 	public void testTransportType() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("location.transportType", new String[] { "link" });
 		check(params, query().equalTo(LOCATION_TRANSPORT_TYPE, TransportType.LINK));
 	}
 	
 	public void testTitleAndPublisherEqualityDisjunction() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.publisher", new String[] { "bbc,channel4" });
 		check(params, query().equalTo(ITEM_PUBLISHER, "bbc", "channel4"));
 	}
 	
 	public void testTitleContainsConjunction() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("item.title-search", new String[] { "bob", "vic" });
 		check(params, query().searchFor(ITEM_TITLE, "bob").searchFor(ITEM_TITLE, "vic"));
 	}
 	
 	public void testDurationGreaterThan() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("version.duration-greaterThan", new String[] {"10"});
 		check(params, query().greaterThan(VERSION_DURATION, 10));
 	}
 	
 	public void testDurationLessThan() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("version.duration-lessThan", new String[] {"10"});
 		check(params, query().lessThan(VERSION_DURATION, 10));
 	}
 	
 	public void testTransmittedAfter() {
 		Map<String, String[]> params = Maps.newHashMap();
		params.put("broadcast.transmissionTime-after", new String[] {"101"});
		check(params, query().after(BROADCAST_TRANSMISSION_TIME, new DateTime(101L, DateTimeZones.UTC)));
 	}
 	
 	public void testTransmittedBefore() {
 		Map<String, String[]> params = Maps.newHashMap();
		params.put("broadcast.transmissionTime-before", new String[] {"101"});
		check(params, query().before(BROADCAST_TRANSMISSION_TIME, new DateTime(101L, DateTimeZones.UTC)));
 	}
 	
 	public void testTransmissionTimeEquals() throws Exception {
		DateTime when = new DateTime(1010101, DateTimeZones.UTC);
 		Map<String, String[]> params = Maps.newHashMap();
		params.put("broadcast.transmissionTime", new String[] { String.valueOf(when.getMillis()) });
 		check(params, query().before(BROADCAST_TRANSMISSION_TIME, when.plusSeconds(1)).after(BROADCAST_TRANSMISSION_END_TIME, when));
 	}
 	
 	public void testInvalidAttribute() {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("foo", new String[] {"101"});
 		try {
 			builder.build(params, Item.class);
 			fail();
 		} catch (IllegalArgumentException e) {
 			// expected
 		}
 	}
 	
 	public void testUnknownOperator() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("title-haltingproblemsolver", new String[] { "est" });
 		try {
 			builder.build(params, Item.class);
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
 			builder
 			.withIgnoreParams("foo")
 			.build(params, Item.class);
 		
 		assertEquals(query().searchFor(ITEM_TITLE, "bob").build(), query);
 	}
 	
 	public void testBroadcastQuery() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("transmissionTime-after", new String[] { "10101" });
 		
 		new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter());
		check(params, query().after(Attributes.BROADCAST_TRANSMISSION_TIME, new DateTime(10101, DateTimeZones.UTC)), Item.class);
 	}
 	
 	public void testAliases() throws Exception {
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		check(params, query().searchFor(ITEM_TITLE, "bob"), Item.class);
 		
 		params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		check(params, query().searchFor(BRAND_TITLE, "bob"), Brand.class);
 		
 		params = Maps.newHashMap();
 		params.put("available", new String[] { "true" });
 		check(params, query().equalTo(LOCATION_AVAILABLE, true), Item.class);
 		
 		params = Maps.newHashMap();
 		params.put("position", new String[] { "10" });
 		check(params, query().equalTo(EPISODE_POSITION, 10), Item.class);
 	}
 	
 	public void testDefaultSetting() throws Exception {
 		QueryStringBackedQueryBuilder builderWithDefaults = new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter());
 
 		Map<String, String[]> params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		
 		assertEquals(query().searchFor(ITEM_TITLE, "bob").equalTo(LOCATION_AVAILABLE, true).build(), builderWithDefaults.build(params, Item.class));
 	
 		params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		params.put("available", new String[] { "false" });
 		
 		assertEquals(query().searchFor(ITEM_TITLE, "bob").equalTo(LOCATION_AVAILABLE, false).build(), builderWithDefaults.build(params, Item.class));
 
 		params = Maps.newHashMap();
 		params.put("title", new String[] { "bob" });
 		params.put("available", new String[] { "any" });
 		
 		assertEquals(query().searchFor(ITEM_TITLE, "bob").build(), builderWithDefaults.build(params, Item.class));
 	}
 
 	private void check(Map<String, String[]> params, ContentQueryBuilder expected) {
 		check(params, expected, Content.class);
 	}
 	
 	private void check(Map<String, String[]> params, ContentQueryBuilder expected, Class<? extends Content> type) {
 		ContentQuery query = builder.build(params, type);
 		assertEquals(expected.build(), query);
 	}
 }
