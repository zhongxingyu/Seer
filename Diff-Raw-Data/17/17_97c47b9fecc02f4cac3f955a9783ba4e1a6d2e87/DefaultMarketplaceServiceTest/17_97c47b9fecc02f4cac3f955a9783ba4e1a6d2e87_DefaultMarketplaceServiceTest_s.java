 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.mpc.tests.service;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.DefaultMarketplaceService;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.core.service.SearchResult;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 
 /**
  * @author David Green
  */
@SuppressWarnings("restriction")
 @RunWith(BlockJUnit4ClassRunner.class)
 public class DefaultMarketplaceServiceTest {
 
 	private DefaultMarketplaceService marketplaceService;
 
 	@Before
 	public void setUp() throws Exception {
 		marketplaceService = new DefaultMarketplaceService();
 		Map<String, String> requestMetaParameters = new HashMap<String, String>();
 		requestMetaParameters.put(DefaultMarketplaceService.META_PARAM_CLIENT, "org.eclipse.epp.mpc.tests");
 		marketplaceService.setRequestMetaParameters(requestMetaParameters);
 	}
 
 	@Test
 	public void listMarkets() throws CoreException {
 		List<Market> markets = marketplaceService.listMarkets(new NullProgressMonitor());
 		assertNotNull(markets);
 		assertFalse(markets.isEmpty());
 
 		for (Market market : markets) {
 			assertNotNull(market.getId());
 			assertNotNull(market.getUrl());
 			assertNotNull(market.getName());
 		}
 	}
 
 	@Test
 	public void getCategory() throws CoreException {
 		List<Market> markets = marketplaceService.listMarkets(new NullProgressMonitor());
 		assertNotNull(markets);
 		assertFalse(markets.isEmpty());
 
 		final String marketName = "Tools";
 
 		Market market = null;
 		for (Market m : markets) {
 			if (marketName.equals(m.getName())) {
 				market = m;
 				break;
 			}
 		}
 		assertNotNull("Expected market " + marketName, market);
 
 		assertFalse(market.getCategory().isEmpty());
 
 		final String categoryName = "Mylyn Connectors";
 
 		Category category = null;
 		for (Category c : market.getCategory()) {
 			if (categoryName.equals(c.getName())) {
 				category = c;
 				break;
 			}
 		}
 		assertNotNull("Expected category " + categoryName, category);
 
 		Category result = marketplaceService.getCategory(category, new NullProgressMonitor());
 		assertNotNull(result);
 
 		// FIXME: pending bug 302671
 		// assertEquals(category.getId(),result.getId());
 		assertEquals(category.getName(), result.getName());
 		assertEquals(category.getUrl(), result.getUrl());
 	}
 
 //	
 //	@Test
 //	public void testGetMarket() throws CoreException {
 ////		Failing due to bug 302670: REST API market response inconsistency
 ////		https://bugs.eclipse.org/bugs/show_bug.cgi?id=302670
 //		List<Market> markets = marketplaceService.listMarkets(new NullProgressMonitor());
 //		assertNotNull(markets);
 //		assertFalse(markets.isEmpty());
 //
 //		final String marketName = "Tools";
 //		
 //		Market market = null;
 //		for (Market m: markets) {
 //			if (marketName.equals(m.getName())) {
 //				market = m;
 //			}
 //		}
 //		assertNotNull("Expected market "+marketName,market);
 //		
 //		Market result = marketplaceService.getMarket(market, new NullProgressMonitor());
 //		
 //		assertEquals(market.getId(),result.getId());
 //		assertEquals(market.getName(),result.getName());
 //		assertEquals(market.getUrl(),result.getUrl());
 //	}
 //	
 	@Test
 	public void search() throws CoreException {
 		List<Market> markets = marketplaceService.listMarkets(new NullProgressMonitor());
 		assertTrue(!markets.isEmpty());
 
 		Market toolsMarket = null;
 		for (Market market : markets) {
 			if ("Tools".equals(market.getName())) {
 				toolsMarket = market;
 				break;
 			}
 		}
 		assertNotNull(toolsMarket);
 		Category mylynCategory = null;
 		for (Category category : toolsMarket.getCategory()) {
 			if ("Mylyn Connectors".equals(category.getName())) {
 				mylynCategory = category;
 				break;
 			}
 		}
 		assertNotNull(mylynCategory);
 
 		SearchResult result = marketplaceService.search(toolsMarket, mylynCategory, "WikiText",
 				new NullProgressMonitor());
 		assertNotNull(result);
 		assertNotNull(result.getNodes());
 		assertEquals(Integer.valueOf(1), result.getMatchCount());
 		assertEquals(1, result.getNodes().size());
 
 		Node node = result.getNodes().get(0);
 
 		assertTrue(node.getName().startsWith("Mylyn WikiText"));
 		assertEquals("1065", node.getId());
 	}
 
 	@Test
 	public void featured() throws CoreException {
 		SearchResult result = marketplaceService.featured(new NullProgressMonitor());
 		assertSearchResultSanity(result);
 	}
 
 	@Test
 	public void favorites() throws CoreException {
 		SearchResult result = marketplaceService.favorites(new NullProgressMonitor());
 		assertSearchResultSanity(result);
 	}
 
 	@Test
 	public void popular() throws CoreException {
 //	 NOTE: this test is failing until the following bug is fixed
 //			bug 303275: REST API popular returns count of 6 with 10 nodes returned
 		SearchResult result = marketplaceService.popular(new NullProgressMonitor());
 		assertSearchResultSanity(result);
 	}
 
 	@Test
 	public void recent() throws CoreException {
 		SearchResult result = marketplaceService.recent(new NullProgressMonitor());
 		assertSearchResultSanity(result);
 	}
 
 	protected void assertSearchResultSanity(SearchResult result) {
 		assertNotNull(result);
 		assertNotNull(result.getNodes());
 		assertNotNull(result.getMatchCount());
 		assertTrue(result.getMatchCount() >= result.getNodes().size());
 		assertTrue(result.getNodes().size() > 0);
 
 		Set<String> ids = new HashSet<String>();
 		for (Node node : result.getNodes()) {
 			assertNotNull(node.getId());
 			assertTrue(ids.add(node.getId()));
 			assertNotNull(node.getName());
 		}
 	}
 }
