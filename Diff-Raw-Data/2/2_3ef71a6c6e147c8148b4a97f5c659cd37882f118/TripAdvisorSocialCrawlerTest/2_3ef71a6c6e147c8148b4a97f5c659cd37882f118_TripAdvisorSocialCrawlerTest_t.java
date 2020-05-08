 package org.social.networks.crawler;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.junit.Before;
 import org.junit.Test;
 import org.social.core.entity.domain.Messages;
 import org.social.core.network.crawler.SocialCrawler;
 import org.social.core.network.crawler.TripAdvisorSocialCrawler;
 
 public class TripAdvisorSocialCrawlerTest {
 
 	SocialCrawler jsoupCrawler;
 
 	@Before
 	public void setUp() throws Exception {
 		// http://www.tripadvisor.de/Restaurant_Review-g60763-d1846484-Reviews-Vapiano-New_York_City_New_York.html#REVIEWS
 		jsoupCrawler = new TripAdvisorSocialCrawler(new MockBaseCrawler(), "src/test/resources/",
 				"TripAdvisorTest_WithPagination.html");
 	}
 
 	@Test
 	public void testGetDocument() throws Exception {
 		assertNotNull(jsoupCrawler.getDocument());
 		assertEquals("Vapiano, New York - Bewertungen und Fotos - TripAdvisor", jsoupCrawler.getDocument().title());
 	}
 
 	@Test
 	public void testGetContainerOfReviewData() throws Exception {
 		Element body = jsoupCrawler.getDocument().body();
 		Elements reviewContainer = jsoupCrawler.getReviewDataContainer(body);
 
 		assertNotNull(reviewContainer);
 		assertTrue(reviewContainer.size() > 0);
 		assertEquals("deckB review_collection", reviewContainer.get(0).className());
 	}
 
 	@Test
 	public void testGetNextPageLinkFromPagination() throws Exception {
 		SocialCrawler secondJsoupCrawler = new TripAdvisorSocialCrawler(new MockBaseCrawler(), "src/test/resources/",
 				"TripAdvisorTest_WithPagination.html");
 
 		Element body = secondJsoupCrawler.getDocument().body();
 
 		String nextLink = secondJsoupCrawler.getNextPageFromPagination(body);
 
 		assertNotNull(nextLink);
 		assertEquals("/Restaurant_Review-g60763-d1846484-Reviews-or10-Vapiano-New_York_City_New_York.html",
 				nextLink);
 	}
 
 	@Test
 	public void testExtractReviewData() throws Exception {
 		Element body = jsoupCrawler.getDocument().body();
 		Elements reviewContainer = jsoupCrawler.getReviewDataContainer(body);
 
 		List<Messages> result = jsoupCrawler.extractReviewDataFromHtml(reviewContainer, jsoupCrawler.getDocument()
 				.head(), 1L);
 
 		assertTrue(result.size() >= 10);
 
 		assertTrue(result.get(0).getMessage().startsWith("U die Art der Pasta whlen"));
 		assertEquals("73kamla", result.get(0).getNetworkUser());
		assertEquals("n/a", result.get(0).getNetworkUserId());
 		assertEquals("en", result.get(0).getLanguage());
 		assertEquals("TRIPADVISOR", result.get(0).getNetworkId());
 		assertEquals(1, result.get(0).getCustomerId().longValue());
 		assertEquals("4", result.get(0).getNetworkUserRating());
 	}
 
 }
