 package util;
 
 import com.ebay.sdk.*;
 import com.ebay.sdk.call.GetItemCall;
 import com.ebay.sdk.util.eBayUtil;
 import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
 import com.ebay.soap.eBLBaseComponents.ItemType;
 import com.ebay.services.finding.SearchItem;
 import java.util.*;
 import com.ebay.services.client.ClientConfig;
 import com.ebay.services.client.FindingServiceClientFactory;
 import com.ebay.services.finding.*;
 import com.ebay.services.finding.FindingServicePortType;
 
 import javax.xml.datatype.Duration;
 
 /**
  * This class is used for search items in ebay.
  * @author Admin
  *
  */
 public class SearchUtil {
     private GetItemCall gc;
     private FindingServicePortType serviceClient;
 
     /**
      * This constructor initial special configurations for sdk ebay API
      */
     public SearchUtil() {
         ClientConfig config = new ClientConfig();
         config.setApplicationId("TrackStu-c3e1-4e31-90e0-7ae139cd3650");
         this.serviceClient = FindingServiceClientFactory.getServiceClient(config);
         ApiContext apiContext = new ApiContext();
         ApiCredential cred = apiContext.getApiCredential();
 
         cred.seteBayToken("AgAAAA**AQAAAA**aAAAAA**l/nATg**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AEl4SnCJaKqAidj6x9nY+seQ**s00BAA**AAMAAA**oEMj7FN4qgzFdhM9ppIZUAndL2MDJxd3Q1HBBPHJPk4IH0ajpiEYrbXoAzpYZcUL0XyvU9f84EEK0EfqlXuOZu8g0o+ENxBr/UjaPFNcUpiRmwXA4ESVpKY0aB3V445a24yp9as+ljhDszsR1b6kmieYedFfZroyZ3FzlW1H4pfSOU0OCi5FxUQE52Qwv9bW91hFPJG5CzXskU3Cv4eYspjjSQSij5Jm2jaoiPOp/M4wLHw8Fi4p3n2X9znbpOco2qXgeisluHVbEIG+Qx7ODD6BZmi0aRt2wVW+00B72HpfWiFwsD9apico5TeYt5XUMtyMyloTRFTj0bp0A866yJNmWOQx8ny1DsDSHIpCr80E5aUZmHh2qc1JkSX/CfYBa6mO4qLndgSIP7fn3CxLQxGT4kXEGeJ3KuRPoD97wEhNGkoEY0OxvfVg++Bk7CUQnOkcSLVFEPIlPaZ/woUGpLD38MslXiEW4aoPfz6IBMxlka+jRqqr/HreC7RK47oeezdfMP5Z7dTZSHdmxXt2V7dN+IruYTIDe5bvFOtexiZUZ8QmAp2MkxlwLqaRb51AHdhp+jKr2Oc1OWdQf20H/WFdTqWfHPWVWwbnpsay8ais6FnMMbMS4xf5W2Yn4N3rhz6YH/Sxhvv2FHJ/zSJ7uwuEDCJ3yuj2kVzmdw3+Cmukml2RhGAAY4xZT3bk09pzO7X55fGUd+q2T0CfUBBPVoQH4Yxj6eoHmLa+NmBKsbaSr1NgcGl1BrPBHYWmrQk7");
         apiContext.setApiServerUrl("https://api.ebay.com/wsapi");
         GetItemCall gc = new GetItemCall(apiContext);
         DetailLevelCodeType[] detailLevels = new DetailLevelCodeType[] {
                 DetailLevelCodeType.RETURN_ALL,
                 DetailLevelCodeType.ITEM_RETURN_ATTRIBUTES,
                 DetailLevelCodeType.ITEM_RETURN_DESCRIPTION
         };
         gc.setDetailLevel(detailLevels);
         this.gc = gc;
     }
 
     /**
      * This method gets reference id by numbers item
      * @param itemNubmer
      * @return
      * @throws Exception
      */
     public Integer getReferenceID(String itemNubmer) throws Exception {
         ItemType item = gc.getItem(itemNubmer);
         String value = item.getProductListingDetails().getProductReferenceID();
         return Integer.valueOf(value);
     }
 
     /**
      * This method gets all items which relate to special product by reference id;
      * @param productId
      * @return
      */
     public List<SearchItem> getItemsByProductId(String productId) {
         FindItemsByProductRequest productRequest = new FindItemsByProductRequest();
         ProductId product = new ProductId();
         product.setType("ReferenceID");
         product.setValue(productId);
         productRequest.setProductId(product);
         FindItemsByProductResponse response = serviceClient.findItemsByProduct(productRequest);
         return response.getSearchResult().getItem();
     }
 
     /**
      * This methods gets all conditions from items.
      * @param items
      * @return
      */
     public static Set<String> getConditionsHeaders(List<SearchItem> items) {
         Set<String> conditions = new TreeSet<String>();
         for (SearchItem item : items) {
             conditions.add(item.getCondition().getConditionDisplayName());
         }
         return conditions;
     }
 
     /**
      * This method gets list type by special condition.
      * @param items
      * @param conditionItem
      * @return
      */
     public static Set<String> getListType(List<SearchItem> items, String conditionItem) {
         Set<String> listType = new TreeSet<String>();
         for (SearchItem item : items) {
             String condition = item.getCondition().getConditionDisplayName();
             if (condition.equals(conditionItem)) {
                 listType.add(item.getListingInfo().getListingType());
             }
         }
         return listType;
     }
 
     /**
      * This method gets items by special condition and list type; For example Used, Auction
      * if you want you can change all parameters. for example if you want to get items with only time : end soon. you need to add new params like Calendar and just check special properties items in loop
      * I mean that here is main idea that we can all items with relate to product, so we can make any select what we want.
      * @param items
      * @param conditionItem
      * @param listTypeItem
      * @return
      */
     public static List<SearchItem> getItemsByConditionAndListType(List<SearchItem> items, String conditionItem, String listTypeItem) {
         List<SearchItem> searchItems = new ArrayList<SearchItem>();
         for (SearchItem item : items) {
             String condition = item.getCondition().getConditionDisplayName();
             String listType = item.getListingInfo().getListingType();
             if (conditionItem != null) {
                 if (conditionItem.equals(condition)) {
                     if (listTypeItem != null) {
                         if (listTypeItem.equals(listType)) {
                             searchItems.add(item);
                         }
                     } else {
                         searchItems.add(item);
                     }
                 }
             } else {
                 searchItems.add(item);
             }
         }
         return searchItems;
     }
 
     private static ItemFilter buildFulter(ItemFilterType type, String value) {
         Filter filter = new Filter();
         filter.setName(type);
         filter.addValue(value);
         return filter;
     }
 
     public List<SearchItem> getItemsBySortedType(String productId, String condition, String listingType, SortOrderType order, String type, Calendar endTimeFrom, Calendar endTimeTo) {
         FindByProduct productRequest = new FindByProduct();
         PaginationInput pi = new PaginationInput();
         pi.setPageNumber(1);
         productRequest.setPaginationInput(pi);
         productRequest.setSortOrder(order);
         if (TextUtil.isNotNull(condition)) {
             productRequest.add(buildFulter(ItemFilterType.CONDITION, condition));
         }
         if (TextUtil.isNotNull(listingType)) {
             productRequest.add(buildFulter(ItemFilterType.LISTING_TYPE, listingType));
         }
 
         if (TextUtil.isNotNull(endTimeFrom) && Calendar.getInstance().compareTo(endTimeFrom) < 0) {
             productRequest.add(buildFulter(ItemFilterType.END_TIME_FROM, FormatterText.buildDate(endTimeFrom)));
         }
         if (TextUtil.isNotNull(endTimeTo) && Calendar.getInstance().compareTo(endTimeTo) < 0) {
             productRequest.add(buildFulter(ItemFilterType.END_TIME_TO, FormatterText.buildDate(endTimeTo)));
         }
 
         ProductId product = new ProductId();
         product.setType(type);
         product.setValue(productId);
         productRequest.setProductId(product);
         FindItemsByProductResponse response = serviceClient.findItemsByProduct(productRequest);
         SearchResult result = response.getSearchResult();
         if (result != null) {
             return getAllItems(productRequest, response, result.getItem());
         } else {
             return new ArrayList<SearchItem>();
         }
     }
 
     /**
      * This method gets all items, because find ebay api doesn't allow to get more then 100 items by one quire,
      * We get all items from all pages.
      * @param productRequest
      * @param response
      * @param items
      * @return
      */
     private List<SearchItem> getAllItems(FindByProduct productRequest, FindItemsByProductResponse response, List<SearchItem> items) {
         List<SearchItem> totalItems = new ArrayList<SearchItem>();
         PaginationOutput outPage = response.getPaginationOutput();
        totalItems.addAll(items);
         if (items.size() < outPage.getTotalEntries()) {
             for (int i=2;i!=outPage.getTotalPages()+1;++i) {
                 PaginationInput pi = new PaginationInput();
                 pi.setPageNumber(i);
                 productRequest.setPaginationInput(pi);
                 response = serviceClient.findItemsByProduct(productRequest);
                 SearchResult result = response.getSearchResult();
                 totalItems.addAll(result.getItem());
             }
         }
         return totalItems;
     }
 
     public static List<SearchItem> getGoldenItems(List<SearchItem> sortedItems) {       // after fixing the bug
         List<SearchItem> incorrectDuration = new ArrayList<SearchItem>();
         boolean check = false;   //checking for the moment the time has switched.
         if (!sortedItems.isEmpty()) {
             Duration previuosDuration = sortedItems.get(0).getSellingStatus().getTimeLeft();
             for (SearchItem item : sortedItems) {
                 Duration currectDuration = item.getSellingStatus().getTimeLeft();
                 if (currectDuration.isShorterThan(previuosDuration)) {
                     check = true;
                 }
                 if (check) {incorrectDuration.add(item); }
                 previuosDuration = currectDuration;
 
             }
         }
         return incorrectDuration;
     }
 }
