 package org.atlasapi.remotesite.seesaw;
 
 import java.util.Currency;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy.RevenueContract;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 
 import com.metabroadcast.common.currency.Price;
 
 public class SeesawBrandAdapterTest extends TestCase {
     SiteSpecificAdapter<Brand> adapter = new SeesawBrandAdapter(HttpClients.webserviceClient());
     
     public void testShouldGetBrand() {
         Brand galactica = adapter.fetch("http://www.seesaw.com/TV/Drama/b-28373-Battlestar-Galactica");
         assertEquals("Battlestar Galactica", galactica.getTitle());
         assertTrue(galactica.getItems().size() > 0);
         assertTrue(galactica.getGenres().contains("http://www.seesaw.com/TV/Drama"));
         Item firstItem = galactica.getItems().get(0);
         assertTrue(firstItem instanceof Episode);
         Episode firstEpisode = (Episode) firstItem;
         assertEquals("33", firstEpisode.getTitle());
         assertTrue(firstEpisode.getDescription().equalsIgnoreCase("Galactica is on the run and the crew is put to the test as sleep deprivation mixes with the already gloomy reality of dealing with a Cylon-apocalypse. The ship has 33 minutes to make a Jump, elude the Cylons and survive another day."));
         assertEquals(Integer.valueOf(1), firstEpisode.getSeriesNumber());
         assertEquals(Integer.valueOf(1), firstEpisode.getEpisodeNumber());
         
         assertTrue(firstItem.getVersions().size() > 0);
         Version firstVersion = firstItem.getVersions().iterator().next();
         assertEquals(Integer.valueOf(42 * 60), firstVersion.getPublishedDuration());
         
         assertTrue(firstVersion.getManifestedAs().size() > 0);
         Encoding firstEncoding = firstVersion.getManifestedAs().iterator().next();
         assertTrue(firstEncoding.getAvailableAt().size() > 0);
         Location firstLocation = firstEncoding.getAvailableAt().iterator().next();
         assertNotNull(firstLocation.getPolicy());
         assertEquals(RevenueContract.PAY_TO_RENT, firstLocation.getPolicy().getRevenueContract());
        assertEquals(new Price(Currency.getInstance("GBP"), 49), firstLocation.getPolicy().getPrice());
     }
     
     public void testShouldBeAbleToFetch() {
         assertTrue(adapter.canFetch("http://www.seesaw.com/TV/Factual/b-1167-Afghan-Star"));
         assertFalse(adapter.canFetch("http://www.seesaw.com/TV/Factual/p-1167-Afghan-Star"));
         assertFalse(adapter.canFetch("http://www.seesaw.com/AtoZ/A"));
     }
 }
