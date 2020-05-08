 package org.atlasapi.media.entity;
 
 import static org.junit.Assert.assertEquals;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class ContainerTest {
     private final Episode item1 = new Episode("item1", "item1", Publisher.BBC);
     private final Episode item1duplicate = new Episode("item1", "item1", Publisher.BBC);
     private final Episode item1noupdated = new Episode("item1", "item1", Publisher.BBC);
     private final Episode item2 = new Episode("item2", "item2", Publisher.BBC);
     private final DateTime now = new DateTime(DateTimeZones.UTC);
     
     @Before
     public void setUp() {
         item1.setLastUpdated(now);
         item1.setEpisodeNumber(1);
         item1.setSeriesNumber(1);
         item1duplicate.setLastUpdated(now.minusHours(1));
         item1duplicate.setEpisodeNumber(1);
         item1duplicate.setSeriesNumber(1);
         item2.setLastUpdated(now);
         item2.setEpisodeNumber(2);
         item2.setSeriesNumber(1);
     }
     
     @Test
     public void containerShouldHaveDeDupedItemsInCorrectOrder() {
         Container<Episode> container = new Container<Episode>();
        container.setContentsByResolvingChildRefs(ImmutableList.of(item2, item1noupdated, item1duplicate, item1));
         
         assertEquals(ImmutableList.of(item1, item2), container.getContents());
     }
 }
