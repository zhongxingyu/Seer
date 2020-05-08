 /*
  * Copyright (c) 2009 Zauber S.A. -- All rights reserved
  */
 package ar.com.zauber.labs.kraken.core.fetcher.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 
import org.apache.http.impl.client.DefaultHttpClient;
 import org.junit.Assert;
 import org.junit.Test;
 
 import ar.com.zauber.labs.kraken.core.fetcher.impl.ehcache.EhcacheURIFetcher;
import ar.com.zauber.labs.kraken.core.fetcher.impl.httpclient.HTTPClientURIFetcher;
 import ar.com.zauber.labs.kraken.fetcher.api.URIFetcher;
 
 /**
  * Tests para {@link EhcacheURIFetcher}
  * 
  * @author Francisco J. Gonzalez Costanzo
  * @since Nov 19, 2009
  */
 public class EhcacheURIFetcherTest {
     private static final Cache CACHE = new CacheManager(
             EhcacheURIFetcherTest.class.getResourceAsStream("ehcache.xml"))
             .getCache("kraken");
 
    private final URIFetcher fetcher;

    /** constructor */
    public EhcacheURIFetcherTest() {
        fetcher = new EhcacheURIFetcher(new HTTPClientURIFetcher(
                new DefaultHttpClient()), CACHE);
    }

     /** testCaching */
     @Test
     public final void testCaching() throws URISyntaxException {
         final URI uri = new URI("http://www.club.lanacion.com.ar");
 
         CACHE.remove(uri);
         Assert.assertNull(CACHE.get(uri));
 
         fetcher.fetch(uri);
         Assert.assertNotNull(CACHE.get(uri));
     }
 }
