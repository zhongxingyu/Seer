 package unit.feed.controller;
 
 import nmd.rss.collector.error.ServiceException;
 import nmd.rss.collector.feed.FeedHeader;
 import org.junit.Test;
 
 import java.util.List;
 import java.util.UUID;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 21.06.13
  */
 public class ControllerGetFeedHeadersTest extends AbstractControllerTest {
 
     @Test
     public void whenFeedsAddedThenTheyReturnInList() throws ServiceException {
         final UUID firstFeedId = addValidFirstRssFeed();
         final UUID secondFeedId = addValidSecondRssFeed();
 
         final List<FeedHeader> feeds = this.controlService.getFeedHeaders();
 
         assertEquals(2, feeds.size());
 
         assertEquals(firstFeedId, feeds.get(0).id);
         assertEquals(secondFeedId, feeds.get(1).id);
     }
 
     @Test
     public void whenFeedIdExistsThenItsHeaderReturns() throws ServiceException {
         final UUID firstFeedId = addValidFirstRssFeed();
 
         final FeedHeader loadedHeader = this.controlService.loadFeedHeader(firstFeedId);
        final FeedHeader expectedHeader = new FeedHeader(firstFeedId, VALID_FIRST_RSS_FEED_LINK, "3DNews - Daily Digital Digest: Новости Hardware", "Новости Hardware на 3DNews", "http://www.3dnews.ru/");
 
         assertEquals(expectedHeader, loadedHeader);
     }
 
     @Test(expected = ServiceException.class)
     public void whenFeedIdDoesNotExistThenExceptionThrows() throws ServiceException {
         this.controlService.loadFeedHeader(UUID.randomUUID());
     }
 
 }
