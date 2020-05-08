 package util;
 
 import com.ebay.services.finding.Amount;
 import com.ebay.services.finding.SearchItem;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class FormatterText {
     public final static SimpleDateFormat dateformatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
     private final static SimpleDateFormat dateForSearchDay = new SimpleDateFormat("yyyy-MM-dd");
     private final static SimpleDateFormat dateForSearchTime = new SimpleDateFormat("hh:mm:ss.SSS");
 
     public static String buildDate(Calendar cal) {
         return dateForSearchDay.format(cal.getTime()) + "T" + dateForSearchTime.format(cal.getTime()) + "Z";
     }
 
     /**
      * This method makes specail formats data for file
      * @param items
      * @param type
      * @param id
      * @return
      */
     public static String buildCsvFile(List<SearchItem> items, String type, String id, String typeSearch) {
         Map<String, List<SearchItem>> listByComdition = new LinkedHashMap<String, List<SearchItem>>(); //this map we use for sort our items by condition
         for (SearchItem item : items) {
             String condition = item.getCondition().getConditionDisplayName();
             List<SearchItem> conditionList = listByComdition.get(condition);
             if (conditionList == null) {
                 conditionList = new ArrayList<SearchItem>();
             }
             conditionList.add(item);
             listByComdition.put(condition, conditionList);
         }
         StringBuilder sb = new StringBuilder("Type search : " + typeSearch);
         sb.append(type).append(" (").append(id).append(")").append("\n\n");
         for (Map.Entry<String, List<SearchItem>> entry : listByComdition.entrySet()) {
             sb.append("Condition (").append(entry.getKey()).append(")").append("\n");
             for (SearchItem item : entry.getValue()) {
                 sb.append(item.getItemId()).append("\n");
             }
             sb.append("\n");
         }
         return sb.toString();
     }
 
 
     public static String formatForConsole(List<SearchItem> items, Map<String, Boolean> showOpts, String id, String type) {
         StringBuilder sb = new StringBuilder();
         sb.append(type).append(" (").append(id).append(")").append("\n\n");
         for (SearchItem item : items)  {
             for (Map.Entry<String, Boolean> entry : showOpts.entrySet()) {
                 if ("autoPay".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(String.valueOf(item.isAutoPay())).append("\n");
                 }
                 if ("charityId".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getCharityId()).append("\n");
                 }
                 if ("compatibility".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getCompatibility()).append("\n");
                 }
                 if ("conditionDisplayName".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getCondition().getConditionDisplayName()).append("\n");
                 }
                 if ("country".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getCountry()).append("\n");
                 }
                 if ("distance".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getDistance()).append("\n");
                 }
                 if ("galleryURL".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getGalleryURL()).append("\n");
                 }
                 if ("galleryPlusPictureURL".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getGalleryPlusPictureURL()).append("\n");
                 }
                 if ("globalId".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getGlobalId()).append("\n");
                 }
                 if ("itemId".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getItemId()).append("\n");
                 }
                 if ("bestOfferEnabled".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getListingInfo().isBestOfferEnabled()).append("\n");
                 }
                 if ("buyItNowAvailable".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getListingInfo().isBuyItNowAvailable()).append("\n");
                 }
                 if ("buyItNowPrice".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getListingInfo().getBuyItNowPrice()).append("\n");
                 }
                 if ("convertedBuyItNowPrice".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getListingInfo().getConvertedBuyItNowPrice()).append("\n");
                 }
                 if ("endTime".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(dateformatter.format(item.getListingInfo().getEndTime().getTime())).append("\n");
                 }
                 if ("gift".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getListingInfo().isGift()).append("\n");
                 }
                 if ("listingType".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getListingInfo().getListingType()).append("\n");
                 }
                 if ("startTime".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(dateformatter.format(item.getListingInfo().getStartTime().getTime())).append("\n");
                 }
                 if ("primaryCategoryId".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getPrimaryCategory().getCategoryId()).append("\n");
                 }
                 if ("primaryCategoryName".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getPrimaryCategory().getCategoryName()).append("\n");
                 }
                 if ("location".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getLocation()).append("\n");
                 }
                 if ("paymentMethod".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getPaymentMethod()).append("\n");
                 }
                 if ("postalCode".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getPostalCode()).append("\n");
                 }
                 if ("productId".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getProductId()).append("\n");
                 }
                 if ("returnsAccepted".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.isReturnsAccepted()).append("\n");
                 }
                 if ("secondaryCategoryId".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSecondaryCategory().getCategoryId()).append("\n");
                 }
                 if ("secondaryCategoryName".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSecondaryCategory().getCategoryName()).append("\n");
                 }
                 if ("feedbackRatingStar".equals(entry.getKey())) {
                    sb.append(entry.getKey()).append(" : ").append(item.getSellerInfo().getFeedbackRatingStar()).append("\n");
                 }
                 if ("feedbackScore".equals(entry.getKey())) {
                    sb.append(entry.getKey()).append(" : ").append(item.getSellerInfo().getFeedbackScore()).append("\n");
                 }
                 if ("positiveFeedbackPercent".equals(entry.getKey())) {
                    sb.append(entry.getKey()).append(" : ").append(item.getSellerInfo().getPositiveFeedbackPercent()).append("\n");
                 }
                 if ("sellerUserName".equals(entry.getKey())) {
                    sb.append(entry.getKey()).append(" : ").append(item.getSellerInfo().getSellerUserName()).append("\n");
                 }
                 if ("topRatedSeller".equals(entry.getKey())) {
                    sb.append(entry.getKey()).append(" : ").append(item.getSellerInfo().isTopRatedSeller()).append("\n");
                 }
                 if ("bidCount".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSellingStatus().getBidCount()).append("\n");
                 }
                 if ("convertedCurrentPrice".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSellingStatus().getConvertedCurrentPrice()).append("\n");
                 }
                 if ("currentPrice".equals(entry.getKey())) {
                     String value = String.valueOf(item.getSellingStatus().getCurrentPrice().getValue());
                     String rate = item.getSellingStatus().getCurrentPrice().getCurrencyId();
                     sb.append(entry.getKey()).append(" : ").append(buildPrice(item.getSellingStatus().getCurrentPrice())).append("\n");
                 }
                 if ("sellingState".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSellingStatus().getSellingState()).append("\n");
                 }
                 if ("timeLeft".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSellingStatus().getTimeLeft()).append("\n");
                 }
                 if ("expeditedShipping".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getShippingInfo().isExpeditedShipping()).append("\n");
                 }
                 if ("handlingTime".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getShippingInfo().getHandlingTime()).append("\n");
                 }
                 if ("oneDayShippingAvailable".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getShippingInfo().isOneDayShippingAvailable()).append("\n");
                 }
                 if ("shippingServiceCost".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(buildPrice(item.getShippingInfo().getShippingServiceCost())).append("\n");
                 }
                 if ("shippingType".equals(entry.getKey())) {
 
                     sb.append(entry.getKey()).append(" : ").append(item.getShippingInfo().getShippingType()).append("\n");
                 }
                 if ("shipToLocations".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getShippingInfo().getShipToLocations()).append("\n");
                 }
                 if ("storeName".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getStoreInfo().getStoreName()).append("\n");
                 }
                 if ("storeURL".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getStoreInfo().getStoreURL()).append("\n");
                 }
                 if ("subtitle".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getSubtitle()).append("\n");
                 }
                 if ("title".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getTitle()).append("\n");
                 }
                 if ("viewItemURL".equals(entry.getKey())) {
                     sb.append(entry.getKey()).append(" : ").append(item.getViewItemURL()).append("\n");
                 }
             }
             sb.append("\n");
         }
         return sb.toString();
     }
 
     private static String buildPrice(Amount amount) {
         if (amount != null) {
             String value = String.valueOf(amount.getValue());
             String rate = amount.getCurrencyId();
             return value + " " + rate;
         } else {
             return null;
         }
     }
 }
