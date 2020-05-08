 /**
  * Copyright (c) 2010, Todd Ginsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    * Neither the name of Todd Ginsberg, or Gowalla nor the
  *      names of any contributors may be used to endorse or promote products
  *      derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  *  Also, please use this for Good and not Evil.  
  */
 package com.ginsberg.gowalla;
 
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.ginsberg.gowalla.dto.FullCategory;
 import com.ginsberg.gowalla.dto.FullSpot;
 import com.ginsberg.gowalla.dto.GeoPoint;
 import com.ginsberg.gowalla.dto.Item;
 import com.ginsberg.gowalla.dto.SimpleSpot;
 import com.ginsberg.gowalla.dto.SpotPhoto;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests against the live Gowalla server.
  *   
  * This accomplishes the following goals:
  * 1) Helps watch the API for changes, because they aren't announced.
  * 2) Tests full end-to-end.
  * 
  * @author Todd Ginsberg
  */
 public class GowallaTest {
 
 	public static final String UNIT_TEST_API_KEY = "f02109db617f41568610c20a748ef7de";
 	private Gowalla gowalla;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		gowalla = new Gowalla("UnitTests", UNIT_TEST_API_KEY);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 	
 	@Test
 	public void testCategory() throws Exception {
 		FullCategory c = gowalla.getCategory(88); // Find by id.
 		assertNotNull("Should have received a category", c);
 		assertEquals("Should have received correct category.", "Victorian", c.getName());
 		assertNotNull("Should have a subcat array", c.getSubcategories());
 		assertEquals("Subcats should be empty", 0, c.getSubcategories().size());
 		assertNotNull("Should have image url", c.getImageUrl());
 		assertNotNull("Should have small image url", c.getSmallImageUrl());
 		assertFalse("Should have an id", c.getId() == 0);
 		assertNotNull("Should have a url", c.getUrl());
 		assertNotNull("Should have a description", c.getDescription());
 		assertEquals("Should receive same category for identity search.", c, gowalla.getCategory(c));
 	}
 	
 	@Test
 	public void testCategories() throws Exception {
 		List<FullCategory> c = gowalla.getCategories();
 		assertNotNull("Should have received categories", c);
 		assertTrue("List of categories should be non-zero", c.size() > 0);
 	}
 
 	@Test
 	public void testSpot() throws Exception {
 		FullSpot s = gowalla.getSpot(11888);  // My favorite spot!
 		assertNotNull("Should have found the spot.");
 		assertEquals("Refind by identity should work.", s, gowalla.getSpot(s));
 		assertEquals("City", "Austin", s.getAddress().getLocality());
 		assertEquals("State", "TX", s.getAddress().getRegion());
 		assertNotNull("Categories should be present", s.getCategories());
 		assertEquals(1, s.getCategories().size());
 		assertNotNull(s.getDescription());
 		assertEquals(1, s.getWebsites().length);
 		assertNotNull(s.getCreatedAt());
 		assertNotNull(s.getCreator());
 		assertEquals(60, s.getCreator().getId());
 		assertFalse(s.getTop10().size() == 0);
 		assertFalse(s.getFounders().size() == 0);
 		assertFalse(s.isMerged());
 		assertTrue(s.canCheckIn(s.getGeoLocation()));
 		assertFalse(s.getItemsCount() == 0);
 		assertFalse(s.getUsersCount() == 0);
 		assertFalse(s.getCheckinsCount() == 0);
 		assertEquals("Sno-Beach South", s.getName());
 		assertEquals(11888, s.getId());
 		assertEquals("/spots/11888", s.getUrl());
 		assertNotNull(s.getImageUrl());
 	}
 	
 	@Test
 	public void testGetItemsAtSpot() throws Exception {
 		FullSpot s = gowalla.getSpot(11888);  
 		List<Item> items = gowalla.getItemsAtSpot(s);
 		assertEquals(s.getItemsCount(), items.size());
 		// Pick one...
 		Item i = items.get(0);
 		assertFalse(i.getId() == 0);
 		assertNotNull(i.getDeterminer());
 		assertNotNull(i.getImageUrl());
 		assertFalse(i.getIssueNumber() == 0);
 		assertNotNull(i.getName());
 		assertNotNull(i.getUrl());
 	}
 	
 	@Test
 	public void testGetItem() throws Exception {
 		Item i = gowalla.getItem(211082);
 		assertEquals(i, gowalla.getItem(i));
 		assertEquals("some", i.getDeterminer());
 		assertEquals(211082, i.getId());
 		assertNotNull(i.getImageUrl());
 		assertEquals(11, i.getIssueNumber());
 		assertNotNull("Venture Capital", i.getName());
 		assertNotNull("/items/211082", i.getUrl());
 	}
 	
 	@Test 
 	public void testFindSpots() throws Exception {
 		GeoPoint where = new GeoPoint("30.2590405833", "-97.75244235");
 		List<SimpleSpot> spots = gowalla.findSpotsNear(where, 1000);
 		// Should return in order.
 		assertNotNull(spots);
 		assertFalse(spots.size() == 0);
 		SimpleSpot s = spots.get(0);
 		assertEquals(11888, s.getId());
 		assertFalse(s.getItemsCount() == 0);
 		assertFalse(s.getUsersCount() == 0);
 		assertFalse(s.getCheckinsCount() == 0);
 		assertEquals("Sno-Beach South", s.getName());
 		assertEquals(11888, s.getId());
 		assertEquals("/spots/11888", s.getUrl());
 		assertNotNull(s.getImageUrl());
 	}
 	
 	@Test 
 	public void testFindSpotsWithPaging() throws Exception {
 		GeoPoint where = new GeoPoint("30.2590405833", "-97.75244235");
		SpotCriteria criteria = new SpotCriteria.Builder(where, 100).setNumberOfSpots(250).setPagingSupport(PagingSupport.PAGING_ALLOWED).build();
 		List<SimpleSpot> spots = gowalla.findSpots(criteria);
 		assertNotNull(spots);
 		assertEquals("Should have returned 250 spots near Sno-Beach.", 250, spots.size());
 	}
 	
 	@Test
 	public void testItemsInPack() throws Exception {
 		List<Item> items = gowalla.getItemsForUser("santa", ItemContext.PACK);
 		assertEquals("Santa shouldn't have less than 10 things.", 10, items.size());
 	}
 		
 	@Test
 	public void testItemsInVault() throws Exception {
 		List<Item> items = gowalla.getItemsForUser("santa", ItemContext.VAULT);
 		assertEquals("Santa account should have an empty vault.", 0, items.size());
 		items = gowalla.getItemsForUser("tginsberg", ItemContext.VAULT);
 		assertTrue("Todd should have an non-empty vault.",items.size() > 140);
 	}
 	
 	@Test
 	public void testItemsMissing() throws Exception {
 		List<Item> items = gowalla.getItemsForUser("santa", ItemContext.MISSING);
 		assertTrue("Santa account should have some missing items.", items.size() != 0);
 	}
 	
 	@Test
 	public void testSpotPhotos() throws Exception {
 		List<SpotPhoto> photos = gowalla.getSpotPhotos(11888);
 		for(SpotPhoto photo : photos) {
 			System.out.println(photo);
 			photo.getPhotos();
 		}
 	}
 }
