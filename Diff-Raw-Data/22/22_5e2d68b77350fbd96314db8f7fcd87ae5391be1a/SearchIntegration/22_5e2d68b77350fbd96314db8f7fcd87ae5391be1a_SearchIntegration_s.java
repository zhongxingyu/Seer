 /*
  * Licensed to Lolay, Inc. under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Lolay, Inc. licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://github.com/lolay/citygrid/raw/master/LICENSE
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package com.lolay.citygrid;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import junit.framework.TestCase;
 
 public class SearchIntegration extends TestCase {
 	private static final Log testLocationsLog = LogFactory.getLog(SearchIntegration.class.getName() + ".testLocations");
 	private static final Log testLocationsCorrectionLog = LogFactory.getLog(SearchIntegration.class.getName() + ".testLocationsCorrection");
 	private static final Log testEventsLog = LogFactory.getLog(SearchIntegration.class.getName() + ".testEvents");
 	private static final String baseUrl = "http://api.citygridmedia.com";
 	
 	public void testLocations() throws Exception {
 		Log log = testLocationsLog;
 		log.trace("ENTER");
 		SearchClient searchProxy = new ClientFactory(baseUrl).getSearch();
 		
 		SearchInvoker search = SearchInvoker.builder().publisher("acme")
 			.type(SearchType.RESTAURANT).where("90069").placement("junit").build();
 		SearchResults results = null;
 		try {
 			long start = System.currentTimeMillis();
 			results = search.locations(searchProxy);
 			long end = System.currentTimeMillis();
 			log.trace(String.format("Location search took %s ms", end - start));
 		} catch (WebApplicationException e) {
 			log(log, e);
 			fail();
 		}
 		assertNotNull(results);
 		assertNotNull(results.getTotalHits());
 		assertTrue(results.getTotalHits() > 0);
 		assertNull(results.getDidYouMean());
 		assertEquals((Integer) 1, results.getFirst());
 		assertEquals((Integer) 20, results.getLastHit());
 		assertNotNull(results.getUri());
 		assertNotNull(results.getRegions());
 		assertTrue(results.getRegions().size() > 0);
 		for (Region region : results.getRegions()) {
 			assertNotNull(region.getType());
 			assertNotNull(region.getLatitude());
 			assertNotNull(region.getLongitude());
 			assertNotNull(region.getDefaultRadius());
 		}
 		
 		assertNotNull(results.getLocations());
 		assertTrue(results.getLocations().size() > 0);
 		for (Location location : results.getLocations()) {
 			assertNotNull(location.getId());
 			assertNotNull(location.getFeatured());
 			assertNotNull(location.getName());
 			assertNotNull(location.getAddress());
 			assertNotNull(location.getLatitude());
 			assertNotNull(location.getLongitude());
 			assertNotNull(location.getProfile());
 			assertNotNull(location.getHasVideo());
 			assertNotNull(location.getHasOffers());
 			assertNotNull(location.getUserReviewCount());
 			assertNotNull(location.getSampleCategories());
 		}
 		
 		assertNull(results.getEngagements());
 		
 		assertNotNull(results.getHistograms());
 		assertTrue(results.getHistograms().size() > 0);
 		for (Histogram histogram : results.getHistograms()) {
 			assertNotNull(histogram.getName());
 			assertNotNull(histogram.getItems());
 			assertTrue(histogram.getItems().size() > 0);
 			for (HistogramItem item : histogram.getItems()) {
 				assertNotNull(item.getName());
 				assertNotNull(item.getCount());
 				assertTrue(item.getCount() > 0);
 				assertNotNull(item.getUri());
 			}
 		}
 	}
 	
 	public void testLocationsCorrection() throws Exception {
 		Log log = testLocationsCorrectionLog;
 		log.trace("ENTER");
 		SearchClient searchProxy = new ClientFactory(baseUrl).getSearch();
 		
 		SearchInvoker search = SearchInvoker.builder().publisher("acme")
 			.what("computr parts").where("90069").placement("junit").build();
 		SearchResults results = null;
 		try {
 			results = search.locations(searchProxy);
 		} catch (WebApplicationException e) {
 			log(log, e);
 			fail();
 		}
 		assertNotNull(results);
 		assertNotNull(results.getTotalHits());
 		assertEquals((Integer) 0, results.getTotalHits());
 		assertNotNull(results.getDidYouMean());
 		assertEquals((Integer) 1, results.getFirst());
 		assertEquals((Integer) 0, results.getLastHit());
 		assertNotNull(results.getUri());
 		assertNotNull(results.getRegions());
 		assertTrue(results.getRegions().size() > 0);
 		for (Region region : results.getRegions()) {
 			assertNotNull(region.getType());
 			assertNotNull(region.getLatitude());
 			assertNotNull(region.getLongitude());
 			assertNotNull(region.getDefaultRadius());
 		}
 	}
 	
 	public void testEvents() throws Exception {
 		Log log = testEventsLog;
 		log.trace("ENTER");
 		SearchClient searchProxy = new ClientFactory(baseUrl).getSearch();
 		
 		SearchInvoker search = SearchInvoker.builder().publisher("acme")
 			.type(SearchType.MOVIE).where("90069").placement("junit").build();
 		SearchResults results = null;
 		try {
 			long start = System.currentTimeMillis();
 			results = search.events(searchProxy);
 			long end = System.currentTimeMillis();
 			log.trace(String.format("Events search took %s ms", end - start));
 		} catch (WebApplicationException e) {
 			log(log, e);
 			fail();
 		}
 		assertNotNull(results);
 		assertNotNull(results.getTotalHits());
 		assertTrue(results.getTotalHits() > 0);
 		assertNull(results.getDidYouMean());
 		assertEquals((Integer) 1, results.getFirst());
 		assertNotNull(results.getLastHit());
 		assertTrue(results.getLastHit() > 0);
 		assertNotNull(results.getUri());
 		assertNotNull(results.getRegions());
 		assertTrue(results.getRegions().size() > 0);
 		for (Region region : results.getRegions()) {
 			assertNotNull(region.getType());
 			assertNotNull(region.getLatitude());
 			assertNotNull(region.getLongitude());
 			assertNotNull(region.getDefaultRadius());
 		}
 		
 		assertNull(results.getLocations());
 		
 		assertNotNull(results.getEngagements());
 		assertTrue(results.getEngagements().size() > 0);
 		for (Engagement engagement : results.getEngagements()) {
 			assertNotNull(engagement.getEvent());
 			Event event = engagement.getEvent();
 			assertNotNull(event.getId());
 			assertNotNull(event.getName());
 			assertNotNull(event.getPerformances());
 			assertNotNull(event.getUserReviewCount());
 			
 			Location location = engagement.getLocation();
 			assertNotNull(location.getId());
 			assertNotNull(location.getName());
 			assertNotNull(location.getAddress());
 			assertNotNull(location.getLatitude());
 			assertNotNull(location.getLongitude());
 		}
 		
 		assertNotNull(results.getHistograms());
 		assertTrue(results.getHistograms().size() > 0);
 		for (Histogram histogram : results.getHistograms()) {
 			assertNotNull(histogram.getName());
 			assertNotNull(histogram.getItems());
 			assertTrue(histogram.getItems().size() > 0);
 			for (HistogramItem item : histogram.getItems()) {
 				assertNotNull(item.getName());
 				assertNotNull(item.getCount());
 				assertTrue(item.getCount() > 0);
 				assertNotNull(item.getUri());
 			}
 		}
 	}
 	
 	private void log(Log log, WebApplicationException e) throws Exception {
 		Response response = e.getResponse();
 		log.error(response);
 	}
 }
