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
 import org.social.core.network.crawler.SocialCrawler;
 import org.social.core.network.crawler.YelpSocialCrawler;
 import org.social.core.util.UtilDateTime;
 
 public class SocialYelpCrawlerTest extends SocialITCase {
 
 	SocialCrawler jsoupCrawler;
 	private Document doc = null;
 
 	@Before
 	public void setUp() throws Exception {
		jsoupCrawler = new YelpSocialCrawler("https://www.yelp.com", "/biz/vapiano-new-york-2?sort_by=date_desc");
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
 		assertEquals("Vapiano - Greenwich Village - New York, NY", doc.title());
 	}
 
 	@Test
 	public void testGetContainerOfReviewData() throws Exception {
 		Element body = doc.body();
 		Elements reviewContainer = jsoupCrawler.getReviewDataContainer(body);
 
 		assertNotNull(reviewContainer);
 		assertTrue(reviewContainer.size() > 1);
 		assertEquals("media-block-no-margin clearfix media-block", reviewContainer.get(0).className());
 	}
 
 	@Test
 	public void testGetSitePagination() throws Exception {
 		Element body = doc.body();
 		Element paginationElement = jsoupCrawler.getPaginationCurrentSelectedData(body);
 
 		assertNotNull(paginationElement);
 		assertEquals("highlight2", paginationElement.className());
 		assertEquals("1", paginationElement.text());
 	}
 
 	@Test
 	public void testGetNextPageLinkFromPagination() throws Exception {
 		Element body = doc.body();
 
 		String nextLink = jsoupCrawler.getNextPageFromPagination(body);
 
 		assertNotNull(nextLink);
 		assertEquals("/biz/vapiano-new-york-2?sort_by=date_desc&start=40", nextLink);
 	}
 
 	@Test
 	public void testExtractReviewData() throws Exception {
 		Element body = doc.body();
 		Elements reviewContainer = jsoupCrawler.getReviewDataContainer(body);
 
 		List<Messages> result = jsoupCrawler.extractReviewDataFromHtml(reviewContainer, doc.head(), 1L);
 
 		assertTrue(result.size() >= 40);
 
 		// assertTrue(result.get(0).getMessage().startsWith("Even though Vapiano is one of my favorite places"));
 		// assertEquals("Dorie L.", result.get(0).getNetworkUser());
 		// assertEquals("H9QzuPZn_wWlx0NYStSO6Q", result.get(0).getNetworkUserId());
 		assertEquals("en", result.get(0).getLanguage());
 		assertEquals("YELP", result.get(0).getNetworkId());
 		assertEquals(1, result.get(0).getCustomerId().longValue());
 		// assertEquals("4.0", result.get(0).getNetworkUserRating());
 	}
 
 }
