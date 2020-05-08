 package au.com.miskinhill.web.feeds;
 
 import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.sun.jersey.api.client.Client;
 import org.junit.Test;
 
 import au.com.miskinhill.AbstractWebIntegrationTest;
 import au.com.miskinhill.schema.sitemaps.Url;
 import au.com.miskinhill.schema.sitemaps.Urlset;
 
 public class SitemapWebIntegrationTest extends AbstractWebIntegrationTest {
     
     @Test
     public void shouldBeParseable() {
         Urlset sitemap = Client.create().resource(BASE).path("/feeds/sitemap").get(Urlset.class);
         assertTrue(sitemap.getUrls().size() >= 1228);
    }
    
    @Test
    public void shouldNotContainDuplicates() {
        Urlset sitemap = Client.create().resource(BASE).path("/feeds/sitemap").get(Urlset.class);
        Set<String> locs = new HashSet<String>();
        for (Url url: sitemap.getUrls()) {
            assertFalse("Found duplicate " + url, locs.contains(url.getLoc()));
            locs.add(url.getLoc());
        }
     }
 
 }
