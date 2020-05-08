 package org.social.network.crawler;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.List;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.junit.Before;
 import org.junit.Test;
 import org.social.SocialITCase;
 import org.social.core.entity.domain.Messages;
 import org.social.core.network.crawler.OpenTableSocialCrawler;
 import org.social.core.network.crawler.SocialCrawler;
 import org.social.core.util.UtilDateTime;
 
 public class SocialOpenTableCrawlerTest extends SocialITCase {
 
 	SocialCrawler jsoupCrawler;
 	private Document doc = null;
 
 	@Before
 	public void setUp() throws Exception {
		jsoupCrawler = new OpenTableSocialCrawler("http://reviews.opentable.com",
				"/0938/41533/reviews.htm");
 		doc = jsoupCrawler.getDocument();
 	}
 
 	@Test
 	public void testIfMessageDateIsYounger() throws Exception {
 		Calendar calendar = Calendar.getInstance();
 
 		Timestamp messageNetworkTs = UtilDateTime.nowTimestamp();
 		Timestamp lastNetworkAccess = UtilDateTime.nowTimestamp();
 
 		boolean younger = jsoupCrawler.isMessageYoungerThanLastNetworkAccess(messageNetworkTs, lastNetworkAccess);
 		assertFalse(younger);
 
 		messageNetworkTs = UtilDateTime.toTimestamp("3/24/2012");
 
 		calendar.set(2012, 7 - 1, 26, 11, 3, 0);
 		lastNetworkAccess = new Timestamp(calendar.getTimeInMillis());
 
 		younger = jsoupCrawler.isMessageYoungerThanLastNetworkAccess(messageNetworkTs, lastNetworkAccess);
 		assertTrue(younger);
 
 		messageNetworkTs = UtilDateTime.toTimestamp("7/26/2012");
 
 		calendar.set(2012, 7 - 1, 26, 11, 3, 0);
 		lastNetworkAccess = new Timestamp(calendar.getTimeInMillis());
 
 		younger = jsoupCrawler.isMessageYoungerThanLastNetworkAccess(messageNetworkTs, lastNetworkAccess);
 		assertFalse(younger);
 	}
 
 	@Test
 	public void testGetDocument() throws Exception {
 		assertNotNull(doc);
		assertEquals("Wolfgang's Steak House - 54th Street Reviews - Rated by OpenTable Diners", doc.title());
 	}
 
 	@Test
 	public void testGetContainerOfReviewData() throws Exception {
 		Element body = doc.body();
 		Elements reviewContainer = jsoupCrawler.getReviewDataContainer(body);
 
 		assertNotNull(reviewContainer);
 		assertTrue(reviewContainer.size() > 0);
		assertEquals("BVRRSDisplayContentBody", reviewContainer.get(0).className());
 	}
 
 	@Test
 	public void testGetNextPageLinkFromPagination() throws Exception {
 		Element body = doc.body();
 
 		String nextLink = jsoupCrawler.getNextPageFromPagination(body);
 
 		assertNotNull(nextLink);
		assertEquals("/0938/41533/reviews.htm?page=2", nextLink);
 	}
 
 	@Test
 	public void testExtractReviewData() throws Exception {
 		Element body = doc.body();
 		Elements reviewContainer = jsoupCrawler.getReviewDataContainer(body);
 
 		List<Messages> result = jsoupCrawler.extractReviewDataFromHtml(reviewContainer, doc.head(), 1L);
 
 		assertTrue(result.size() >= 10);
 
 		// assertTrue(result.get(0).getMessage().startsWith("Even though Vapiano is one of my favorite places"));
 		// assertEquals("Dorie L.", result.get(0).getNetworkUser());
		// assertEquals("H9QzuPZn_wWlx0NYStSO6Q",
		// result.get(0).getNetworkUserId());
		assertEquals("en-US", result.get(0).getLanguage());
		assertEquals("OPENTABLE", result.get(0).getNetworkId());
 		assertEquals(1, result.get(0).getCustomerId().longValue());
 		// assertEquals("4.0", result.get(0).getNetworkUserRating());
 	}
 
 }
