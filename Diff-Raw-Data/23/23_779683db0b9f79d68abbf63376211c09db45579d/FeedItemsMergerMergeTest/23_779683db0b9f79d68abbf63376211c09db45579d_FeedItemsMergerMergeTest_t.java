 package feed.merger;
 
 import nmd.rss.collector.feed.FeedItem;
 import nmd.rss.collector.feed.FeedItemsMergeReport;
 import nmd.rss.collector.feed.FeedItemsMerger;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 29.04.13
  */
 public class FeedItemsMergerMergeTest {
 
     private static final int REALLY_BIG_FEED = 1000;
     private static final int REALLY_SMALL_FEED = 1;
 
     private static final FeedItem OLD_FIRST = new FeedItem("oldFirstTitle", "oldFirstDescription", "oldFirstLink", new Date(48));
     private static final FeedItem OLD_SECOND = new FeedItem("oldSecondTitle", "oldSecondDescription", "oldSecondLink", new Date(50));
 
     private static final FeedItem YOUNG_FIRST = new FeedItem("youngFirstTitle", "youngFirstDescription", "youngFirstLink", new Date(58));
     private static final FeedItem YOUNG_SECOND = new FeedItem("youngSecondTitle", "youngSecondDescription", "youngSecondLink", new Date(60));
     private static final FeedItem OLD_SECOND_DUPLICATE = new FeedItem("oldSecondTitle", "oldSecondDescription", "oldSecondLink", new Date(52));
 
     private List<FeedItem> olds;
     private List<FeedItem> youngs;
 
     @Before
     public void before() {
         this.olds = new ArrayList<>();
         this.olds.add(OLD_FIRST);
         this.olds.add(OLD_SECOND);
 
         this.youngs = new ArrayList<>();
         this.youngs.add(YOUNG_FIRST);
         this.youngs.add(YOUNG_SECOND);
     }
 
     @Test
     public void allNewFeedsAdded() {
         final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_BIG_FEED);
 
         assertEquals(2, report.added.size());
         assertEquals(YOUNG_FIRST, report.added.get(0));
         assertEquals(YOUNG_SECOND, report.added.get(1));
     }
 
     @Test
     public void anyOfOldFeedsDidNotRemove() {
         final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_BIG_FEED);
 
         assertEquals(2, report.retained.size());
         assertEquals(OLD_FIRST, report.retained.get(0));
         assertEquals(OLD_SECOND, report.retained.get(1));
 
         assertTrue(report.removed.isEmpty());
     }
 
     @Test
     public void newItemWithSameLinkAsOldItemAdded() {
         this.youngs.add(OLD_SECOND_DUPLICATE);
 
         final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_BIG_FEED);
 
         assertEquals(3, report.added.size());
 
         assertEquals(OLD_SECOND_DUPLICATE, report.added.get(0));
         assertEquals(YOUNG_FIRST, report.added.get(1));
         assertEquals(YOUNG_SECOND, report.added.get(2));
     }
 
     @Test
     public void oldItemWithSameLinkAsNewItemRemoved() {
         this.youngs.add(OLD_SECOND_DUPLICATE);
 
         final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_BIG_FEED);
 
         assertEquals(1, report.removed.size());
 
         assertEquals(OLD_SECOND, report.removed.get(0));
     }
 
     @Test
     public void ifItemsIdenticalThenMergeNoNeeds() throws Exception {
         this.youngs.clear();
         this.youngs.add(OLD_FIRST);
         this.youngs.add(OLD_SECOND);
 
         final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_BIG_FEED);
 
         assertTrue(report.added.isEmpty());
         assertTrue(report.removed.isEmpty());
 
         assertEquals(2, report.retained.size());
         assertTrue(report.retained.contains(OLD_FIRST));
         assertTrue(report.retained.contains(OLD_SECOND));
     }
 
     @Test
     public void whenCountOfItemsMoreThanMaximumThenYoungestItemsAdded() throws Exception {
         this.olds.clear();
 
         final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_SMALL_FEED);
 
         assertTrue(report.removed.isEmpty());
         assertTrue(report.retained.isEmpty());
 
         assertEquals(1, report.added.size());
         assertEquals(YOUNG_SECOND, report.added.get(0));
     }
 
     @Test
     public void whenCountOfItemsMoreThanMaximumAndThereAreNoNewItemsThenFeedNoChanged() throws Exception {
         this.olds.clear();
 
         final FeedItemsMergeReport firstReport = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_SMALL_FEED);
 
         this.olds.add(firstReport.added.get(0));
 
         final FeedItemsMergeReport secondReport = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_SMALL_FEED);
 
         assertTrue(secondReport.added.isEmpty());
         assertTrue(secondReport.removed.isEmpty());
 
         assertEquals(1, secondReport.retained.size());
         assertEquals(YOUNG_SECOND, secondReport.retained.get(0));
     }
 
    @Test
    public void whenNoYoungsThenNothingChanges() throws Exception {
        this.youngs.clear();

        final FeedItemsMergeReport report = FeedItemsMerger.merge(this.olds, this.youngs, REALLY_BIG_FEED);

        assertTrue(report.added.isEmpty());
        assertTrue(report.removed.isEmpty());
        assertEquals(this.olds.size(), report.retained.size());
    }
 }
