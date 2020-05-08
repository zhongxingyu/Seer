 package com.abudko.reseller.huuto.query.service.item.atom;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.service.item.AbstractQueryItemService;
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.sun.syndication.feed.atom.Feed;
 
 @Component
 public class AtomQueryItemServiceImpl extends AbstractQueryItemService {
 
     private static final String API_HOST = "http://api.huuto.net/somt/0.9/items/";
 
     @Autowired
     @Qualifier("restTemplateAtom")
     private RestTemplate restTemplate;
     
     @Autowired
     private AtomXmlItemParser atomXmlItemParser;
 
     @Override
     public ItemResponse callAndParse(String urlSuffix) {
         String itemUrl = constructFullItemUrl(urlSuffix);
 
         log.info(String.format("Quering item: %s", itemUrl));
 
         Feed atomXmlResponse = restTemplate.getForObject(itemUrl, Feed.class);
 
         ItemResponse itemResponse = atomXmlItemParser.parse(atomXmlResponse);
         itemResponse.setItemUrl(itemUrl);
 
         return itemResponse;
     }
 
     private String constructFullItemUrl(String urlSuffix) {
         String id = extractIdFromUrl(urlSuffix);
         StringBuilder sb = new StringBuilder(API_HOST);
         sb.append(id);
 
         return sb.toString();
     }
     
     protected String extractIdFromUrl(String urlSuffix) {
        int index = urlSuffix.lastIndexOf("/");
        return urlSuffix.substring(index + 1);
     }
 }
