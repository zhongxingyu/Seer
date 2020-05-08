 package com.abudko.reseller.huuto.query.service.list.html.huuto;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.QueryConstants;
 import com.abudko.reseller.huuto.query.rules.AbstractPriceRules;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 import com.abudko.reseller.huuto.query.service.list.AbstractQueryListService;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 
 @Component
 public class HuutoHtmlQueryListServiceImpl extends AbstractQueryListService {
 
     private static final int MAX_ITEMS_ON_PAGE = 50;
 
     private static final String PAGE_PARAM = "/page/";
     
     @Autowired
     @Qualifier("atomQueryItemServiceImpl")
     private QueryItemService queryItemService;
 
     @Autowired
     @Qualifier("huutoHtmlListParser")
     private HuutoHtmlListParser htmlListParser;
 
     @Autowired
     private RestTemplate restTemplate;
 
     @Override
     public Collection<ListResponse> callAndParse(String query) throws URISyntaxException {
         Collection<ListResponse> queryAllResponses = new LinkedHashSet<ListResponse>();
         boolean morePages = false;
 
         int page = 1;
         do {
             URI pagedURI = getPagedURI(query, page++);
             String responseList = restTemplate.getForObject(pagedURI, String.class);
             Collection<ListResponse> queryPageResponses = htmlListParser.parse(responseList);
 
             String info = String.format("Called [%s], got [%d] responses", pagedURI, queryPageResponses.size());
             log.info(info);
 
             queryAllResponses.addAll(queryPageResponses);
 
             morePages = morePages(queryPageResponses);
         } while (morePages);
 
         return queryAllResponses;
     }
 
     private URI getPagedURI(String query, int page) throws URISyntaxException {
         StringBuilder sb = new StringBuilder(QueryConstants.HUUTO_HTML_SEARCH_URL);
         sb.append(query);
         sb.append(PAGE_PARAM);
         sb.append(page);
         URI uri = new URI(sb.toString());
 
         return uri;
     }
 
     private boolean morePages(Collection<ListResponse> queryPageResponses) {
        return queryPageResponses.size() >= MAX_ITEMS_ON_PAGE;
     }
 
     @Override
     protected QueryItemService getQueryItemService() {
         return queryItemService;
     }
     
     @Override
     protected AbstractPriceRules getPriceRules() {
         return this.defaultPriceRules;
     }
 }
