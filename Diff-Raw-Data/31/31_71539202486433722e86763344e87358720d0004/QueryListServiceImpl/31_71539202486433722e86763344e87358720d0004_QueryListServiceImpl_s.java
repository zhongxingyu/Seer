 package com.abudko.reseller.huuto.query.service.list;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.filter.SearchResultFilter;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 import com.abudko.reseller.huuto.query.html.list.HtmlListParser;
 import com.abudko.reseller.huuto.query.html.list.ListResponse;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 import com.abudko.reseller.huuto.query.rules.PriceRules;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 @Component
 public class QueryListServiceImpl implements QueryListService {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Autowired
     private HtmlListParser htmlListParser;
 
     @Autowired
     private QueryItemService queryItemService;
 
     @Autowired
     private List<SearchResultFilter> searchResultFilters;
 
     @Autowired
     private RestTemplate restTemplate;
 
     @Autowired
     private PriceRules priceRules;
 
     @Override
     public Collection<ListResponse> search(String query, SearchParams searchParams)
             throws UnsupportedEncodingException, URISyntaxException, IllegalAccessException, InvocationTargetException,
             NoSuchMethodException {
         URI uri = new URI(query);
 
         Collection<ListResponse> queryResponses = callAndParse(uri);
 
         setNewPrice(queryResponses);
 
         validateImgBaseSrc(queryResponses);
 
         applySearchParamsTo(queryResponses, searchParams);
 
         Collection<ListResponse> filteredResponses = applyFilters(queryResponses, searchParams);
 
         return filteredResponses;
     }
 
     protected Collection<ListResponse> callAndParse(URI uri) {
         String responseList = restTemplate.getForObject(uri, String.class);
         Collection<ListResponse> queryResponses = htmlListParser.parse(responseList);
 
         return queryResponses;

     }
 
     private Collection<ListResponse> applyFilters(Collection<ListResponse> queryResponses, SearchParams searchParams) {
         for (SearchResultFilter filter : searchResultFilters) {
             queryResponses = filter.apply(queryResponses, searchParams);
         }
 
         return queryResponses;
     }
 
     private void setNewPrice(Collection<ListResponse> queryResponses) {
         for (ListResponse queryListResponse : queryResponses) {
             String newPrice = priceRules.calculateNew(queryListResponse.getFullPrice());
             queryListResponse.setNewPrice(newPrice);
         }
     }
 
     private void applySearchParamsTo(Collection<ListResponse> queryResponses, SearchParams searchParams) {
         for (ListResponse queryListResponse : queryResponses) {
             queryListResponse.setCondition(searchParams.getClassification());
         }
     }
 
     private void validateImgBaseSrc(Collection<ListResponse> queryResponses) {
         for (ListResponse queryListResponse : queryResponses) {
             if (queryListResponse.getImgBaseSrc() == null || queryListResponse.getImgBaseSrc().isEmpty()) {
                 ItemResponse item = queryItemService.extractItem(queryListResponse.getItemUrl());
                 String imgBaseSrc = item.getImgBaseSrc();
                 queryListResponse.setImgBaseSrc(imgBaseSrc);
             }
         }
     }
 }
