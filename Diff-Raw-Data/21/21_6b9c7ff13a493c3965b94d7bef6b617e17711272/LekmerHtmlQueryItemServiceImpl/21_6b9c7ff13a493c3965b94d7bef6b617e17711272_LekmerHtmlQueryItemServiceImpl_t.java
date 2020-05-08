 package com.abudko.reseller.huuto.query.service.item.html.lekmer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.service.item.AbstractQueryItemService;
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 
 @Component
 public class LekmerHtmlQueryItemServiceImpl extends AbstractQueryItemService {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Autowired
     private LekmerHtmlItemParser htmlItemParser;
 
     @Autowired
     private RestTemplate restTemplate;
 
     @Override
     protected ItemResponse callAndParse(String itemId) {
         String itemUrl = constructFullItemUrl(itemId);
 
         log.info(String.format("Quering item: %s", itemUrl));
 
         String html = restTemplate.getForObject(itemUrl, String.class);
         ItemResponse itemResponse = parseResponseFromHtml(html);
         itemResponse.setItemUrl(itemUrl);
 
         return itemResponse;
     }
 
     private String constructFullItemUrl(String itemId) {
        return itemId;
     }
 
     private ItemResponse parseResponseFromHtml(String html) {
         ItemResponse itemResponse = htmlItemParser.parse(html);
         return itemResponse;
     }
     
     protected String extractIdFromUrl(String urlSuffix) {
         return null;
     }
 }
