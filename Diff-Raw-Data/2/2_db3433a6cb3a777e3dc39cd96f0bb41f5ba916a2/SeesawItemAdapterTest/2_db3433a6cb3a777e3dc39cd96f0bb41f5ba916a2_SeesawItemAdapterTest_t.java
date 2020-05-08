 package org.atlasapi.remotesite.seesaw;
 
 import java.util.Currency;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.media.entity.Policy.RevenueContract;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 
 import com.metabroadcast.common.currency.Price;
 
 public class SeesawItemAdapterTest extends TestCase {
     SiteSpecificAdapter<Episode> adapter = new SeesawItemAdapter(HttpClients.webserviceClient());
     
     public void testShouldGetProgram() {
         Episode afghanStar = adapter.fetch("http://www.seesaw.com/TV/Factual/p-1167-Afghan-Star");
         assertEquals("Afghan Star", afghanStar.getTitle());
         assertNotNull(afghanStar.getDescription());
         
         Episode southPark = adapter.fetch("http://www.seesaw.com/TV/Comedy/p-24525-Weight-Gain-4000");
         assertEquals("Weight Gain 4000", southPark.getTitle());
         assertEquals("http://www.seesaw.com/i/ccp/00000158/15894.jpg", southPark.getImage());
         
         assertTrue(southPark.getVersions().size() > 0);
         Version firstVersion = southPark.getVersions().iterator().next();
         assertEquals(Integer.valueOf(21 * 60), firstVersion.getPublishedDuration());
         
         assertTrue(firstVersion.getManifestedAs().size() > 0);
         Encoding firstEncoding = firstVersion.getManifestedAs().iterator().next();
         assertTrue(firstEncoding.getAvailableAt().size() > 0);
         Location firstLocation = firstEncoding.getAvailableAt().iterator().next();
         assertNotNull(firstLocation.getPolicy());
         assertEquals(RevenueContract.PAY_TO_RENT, firstLocation.getPolicy().getRevenueContract());
        assertEquals(new Price(Currency.getInstance("GBP"), 49), firstLocation.getPolicy().getPrice());
     }
 }
