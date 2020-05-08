 package org.atlasapi.remotesite.itv;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 
 public class ItvMercuryBrandAdapterTest extends TestCase {
     private final SiteSpecificAdapter<Brand> adapter = new ItvMercuryBrandAdapter();
     
     public void testShouldGetBrand() throws Exception {
         String uri = "http://www.itv.com/itvplayer/video/?Filter=Emmerdale";
         Brand brand = adapter.fetch(uri);
         assertNotNull(brand);
         
         assertEquals(uri, brand.getCanonicalUri());
         assertEquals("itv:Emmerdale", brand.getCurie());
         assertFalse(brand.getGenres().isEmpty());
         assertNotNull(brand.getTitle());
         assertNotNull(brand.getDescription());
         assertFalse(brand.getItems().isEmpty());
         
         for (Item item: brand.getItems()) {
             assertNotNull(item.getTitle());
             assertNotNull(item.getDescription());
            assertFalse(item.getGenres().isEmpty());
             assertFalse(item.getVersions().isEmpty());
             
             for (Version version: item.getVersions()) {
                 assertFalse(version.getBroadcasts().isEmpty());
                 assertFalse(version.getManifestedAs().isEmpty());
                 
                 for (Broadcast broadcast: version.getBroadcasts()) {
                     assertNotNull(broadcast.getBroadcastOn());
                     assertNotNull(broadcast.getTransmissionTime());
                     assertNotNull(broadcast.getTransmissionEndTime());
                 }
                 
                 for (Encoding encoding: version.getManifestedAs()) {
                     assertFalse(encoding.getAvailableAt().isEmpty());
                     
                     for (Location location: encoding.getAvailableAt()) {
                         assertNotNull(location.getUri());
                         assertNotNull(location.getPolicy());
                         assertEquals(TransportType.LINK, location.getTransportType());
                     }
                 }
             }
         }
     }
     
     public void testShouldBeAbleToFetch() {
         assertTrue(adapter.canFetch("http://www.itv.com/itvplayer/video/?Filter=...Do%20the%20Funniest%20Things"));
         assertFalse(adapter.canFetch("http://www.itv.com/itvplayer/video/?Filter=1234"));
     }
 }
