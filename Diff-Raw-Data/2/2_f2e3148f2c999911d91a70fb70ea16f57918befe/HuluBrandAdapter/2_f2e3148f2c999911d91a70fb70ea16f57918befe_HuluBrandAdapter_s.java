 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.hulu;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.query.uri.canonical.Canonicaliser;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 import org.atlasapi.remotesite.html.HtmlNavigator;
 
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class HuluBrandAdapter implements SiteSpecificAdapter<Brand> {
 
     public static final String BASE_URI = "http://www.hulu.com/";
     private static final Pattern SUB_BRAND_PATTERN = Pattern.compile("(" + BASE_URI + ").+?\\/([a-z\\-]+).*");
     private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "[a-z\\-]+).*");
     private final SimpleHttpClient httpClient;
     private final ContentExtractor<HtmlNavigator, Brand> extractor;
     private SiteSpecificAdapter<Episode> episodeAdapter;
     static final Log LOG = LogFactory.getLog(HuluBrandAdapter.class);
 
     public HuluBrandAdapter() {
         this(HttpClients.screenScrapingClient(), new HuluBrandContentExtractor());
     }
 
     public HuluBrandAdapter(SimpleHttpClient httpClient, ContentExtractor<HtmlNavigator, Brand> extractor) {
         this.httpClient = httpClient;
         this.extractor = extractor;
     }
 
     @Override
     public Brand fetch(String uri) {
         LOG.info("Retrieving Hulu brand: " + uri + " with " + httpClient.getClass() + " : " + httpClient.toString());
         String content = getContent(uri);
         if (content != null) {
             HtmlNavigator navigator = new HtmlNavigator(content);
 
             Brand brand = extractor.extract(navigator);
             List<Item> episodes = Lists.newArrayList();
 
             if (episodeAdapter != null) {
                 for (Item item : brand.getItems()) {
                     try {
                         Episode episode = episodeAdapter.fetch(item.getCanonicalUri());
                         episode.setBrand(brand);
                         episodes.add(episode);
                     } catch (FetchException fe) {
                        LOG.warn("Failed to retrieve episode: " + item.getCanonicalUri() + " with message: " + fe.getMessage() + " with cause: " + fe.getCause().getMessage());
                     }
                 }
                 brand.setItems(episodes);
             }
 
             LOG.info("Retrieved Hulu brand: " + uri + " with " + brand.getItems().size() + " episodes");
             return brand;
         } else {
             throw new FetchException("Unable to retrieve brand from Hulu: " + uri + " after a number of attempts");
         }
     }
 
     @Override
     public boolean canFetch(String uri) {
         return Pattern.compile(BASE_URI + "[a-z\\-]+").matcher(uri).matches() && !uri.startsWith("http://www.hulu.com/browse");
     }
 
     public static class HuluBrandCanonicaliser implements Canonicaliser {
         @Override
         public String canonicalise(String uri) {
             if (uri.startsWith("http://www.hulu.com/watch") || uri.startsWith("http://www.hulu.com/feed")) {
                 return null;
             }
 
             Matcher matcher = SUB_BRAND_PATTERN.matcher(uri);
             if (matcher.matches()) {
                 return matcher.group(1) + matcher.group(2);
             }
 
             matcher = ALIAS_PATTERN.matcher(uri);
             if (matcher.matches()) {
                 return matcher.group(1);
             }
             return null;
         }
     }
 
     public void setEpisodeAdapter(SiteSpecificAdapter<Episode> episodeAdapter) {
         this.episodeAdapter = episodeAdapter;
     }
 
     private String getContent(String uri) {
         String content = null;
         for (int i = 0; i < 5; i++) {
             try {
                 content = httpClient.getContentsOf(uri);
                 if (content != null) {
                     break;
                 }
             } catch (HttpException e) {
                 LOG.warn("Error retrieving hulu brand: " + uri + " attempt " + i + " with message: " + e.getMessage());
             }
         }
         return content;
     }
 }
