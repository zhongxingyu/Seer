 package org.atlasapi.remotesite.hulu;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.query.uri.canonical.Canonicaliser;
 import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 import org.atlasapi.remotesite.html.HtmlNavigator;
 import org.jdom.Element;
 
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class HuluAllBrandsAdapter implements SiteSpecificAdapter<Playlist> {
 
     private static final String URL = "http://www.hulu.com/browse/alphabetical/episodes";
     private final SimpleHttpClient httpClient;
     private final SiteSpecificAdapter<Brand> brandAdapter;
     static final Log LOG = LogFactory.getLog(HuluAllBrandsAdapter.class);
     private ContentWriter contentStore;
     private final ExecutorService executor = Executors.newFixedThreadPool(2);
 
     public HuluAllBrandsAdapter() {
         this(HttpClients.screenScrapingClient(), new HuluBrandAdapter());
     }
 
     public HuluAllBrandsAdapter(SiteSpecificAdapter<Brand> brandAdapter) {
         this(HttpClients.screenScrapingClient(), brandAdapter);
     }
 
     public HuluAllBrandsAdapter(SimpleHttpClient httpClient, SiteSpecificAdapter<Brand> brandAdapter) {
         this.httpClient = httpClient;
         this.brandAdapter = brandAdapter;
     }
 
     public void setContentStore(ContentWriter contentStore) {
         this.contentStore = contentStore;
     }
 
     @Override
     public Playlist fetch(String uri) {
         try {
             LOG.info("Retrieving all Hulu brands");
 
             String content = null;
 
             for (int i = 0; i < 5; i++) {
                 try {
                     content = httpClient.getContentsOf(uri);
                     if (content != null) {
                         break;
                     }
                 } catch (HttpException e) {
                     LOG.warn("Error retrieving all hulu brands: " + uri + " attempt " + i + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
                 }
             }
 
             if (content != null) {
                 HtmlNavigator navigator = new HtmlNavigator(content);
 
                 List<Element> elements = navigator.allElementsMatching("//a[@rel='nofollow']");
                 for (Element element : elements) {
                     String brandUri = element.getAttributeValue("href");
                     if (brandAdapter.canFetch(brandUri)) {
                         if (contentStore != null) {
                             executor.execute(new BrandHydratingJob(brandUri));
                         }
                     }
                 }
             } else {
                 LOG.error("Unable to retrieve all hulu brands: " + uri);
             }
             
             // Returning empty playlist
             return new Playlist(URL, "hulu:all_brands");
         } catch (Exception e) {
             LOG.warn("Error retrieving all hulu brands: " + uri + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
             throw new FetchException("Unable to retrieve all hulu brands", e);
         }
     }
 
     class BrandHydratingJob implements Runnable {
 
         private final String uri;
 
         public BrandHydratingJob(String uri) {
             this.uri = uri;
         }
 
         public void run() {
             try {
                 Brand brand = brandAdapter.fetch(uri);
                 contentStore.createOrUpdatePlaylist(brand, true);
             } catch (Exception e) {
                LOG.warn("Error retrieving Hulu brand: " + uri + " while retrieving all brands with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
             }
         }
     }
 
     @Override
     public boolean canFetch(String uri) {
         return URL.equals(uri);
     }
 
     public static class HuluAllBrandsCanonicaliser implements Canonicaliser {
         @Override
         public String canonicalise(String uri) {
             return URL.equals(uri) ? uri : null;
         }
     }
 }
