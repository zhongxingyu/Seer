 package org.atlasapi.media.entity;
 
 import static org.junit.Assert.assertEquals;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class BrandTest {
     private Brand brand = new Brand("brand", "brand", Publisher.BBC);
     private Series old = new Series("series", "series", Publisher.BBC);
     private Series recent = new Series("series", "series", Publisher.BBC);
     
     private Episode e1 = new Episode("e1", "e1", Publisher.BBC);
     private Episode e2 = new Episode("e2", "e2", Publisher.BBC);
 
     @Test
     public void shouldIncludeSeriesThatIsMostRecentlyUpdated() {
         DateTime now = new DateTime(DateTimeZones.UTC);
         DateTime older = now.minusHours(1);
         
         old.setLastUpdated(older);
        old.setContentsByResolvingChildRefs(ImmutableList.of(e1));
         recent.setLastUpdated(now);
        recent.setContentsByResolvingChildRefs(ImmutableList.of(e2));
        brand.setContentsByResolvingChildRefs(ImmutableList.of(e1, e2));
         
         assertEquals(now, Iterables.getOnlyElement(brand.getSeries()).getLastUpdated());
     }
 }
