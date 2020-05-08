 package com.abudko.reseller.huuto.query.service.list.html.lekmer;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.QueryConstants;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 import com.abudko.reseller.huuto.query.service.list.AbstractQueryListService;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.reseller.huuto.query.service.list.html.HtmlListParser;
 
 @Component
 public class LekmerHtmlQueryListServiceImpl extends AbstractQueryListService {
 
     @Autowired
    @Qualifier("lekmerHtmlQueryItemServiceImpl")
     private QueryItemService queryItemService;
 
     @Autowired
     @Qualifier("lekmerHtmlListParser")
     private HtmlListParser htmlListParser;
 
     @Autowired
     private RestTemplate restTemplate;
 
     @Override
     public Collection<ListResponse> callAndParse(String query) throws URISyntaxException {
         Collection<ListResponse> queryAllResponses = new LinkedHashSet<ListResponse>();
 
         URI pagedURI = getPagedURI(query, 0);
         String responseList = restTemplate.getForObject(pagedURI, String.class);
         Collection<ListResponse> queryPageResponses = htmlListParser.parse(responseList);
 
         log.info(String.format("Called [%s], got [%d] responses", pagedURI, queryPageResponses.size()));
 
         queryAllResponses.addAll(queryPageResponses);
         return queryAllResponses;
     }
 
     private URI getPagedURI(String query, int page) throws URISyntaxException {
         StringBuilder sb = new StringBuilder(QueryConstants.LEKMER_HTML_SEARCH_URL);
         sb.append(query);
         URI uri = new URI(sb.toString());
 
         return uri;
     }
 
     @Override
     protected QueryItemService getQueryItemService() {
         return queryItemService;
     }
 }
