 package com.ebaytools.util;
 
 import com.ebay.services.finding.Amount;
 import com.ebay.services.finding.SearchItem;
 import com.ebaytools.kernel.dao.ManagerDAO;
 import com.ebaytools.kernel.entity.Item;
 import com.ebaytools.kernel.entity.ItemProperties;
 
 import javax.xml.datatype.Duration;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class FormatterText {
     public final static SimpleDateFormat dateformatter = new SimpleDateFormat("kk:mm:ss dd.MM.yyyy");
     private final static SimpleDateFormat dateForSearchDay = new SimpleDateFormat("yyyy-MM-dd");
     private final static SimpleDateFormat dateForSearchTime = new SimpleDateFormat("hh:mm:ss.SSS");
     public final static SimpleDateFormat dateForAverage = new SimpleDateFormat("dd.MM.yyyy EEE");
 
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
 
     public static String formatShowFields(List<Item> items, List<String> showFields) {
         StringBuilder sb = new StringBuilder();
         for (Item item : items) {
             buildSingleItem(sb, item, showFields);
         }
         sb.append("Total items : ").append(items.size()).append("\n");
         return sb.toString();
     }
     
     public static void buildSingleItem(final StringBuilder sb, final Item item, final List<String> showFields) {
         Map<Fields, ItemProperties> fields = Fields.buildProperties(item.getProperties());
         if (showFields.contains(Fields.REF.getKey())) {
             sb.append(Fields.REF.getKey()).append(" ").append(item.getProduct().getReferenceId()).append("\n");
         }
         if (showFields.contains(Fields.SOLD.getKey())) {
             sb.append(Fields.SOLD.getKey()).append(" ").append(item.getNameStatus()).append("\n");
         }
         if (showFields.contains(Fields.EBAY_ITEM_ID.getKey())) {
             sb.append(Fields.EBAY_ITEM_ID.getKey()).append(" ").append(item.getEbayItemId()).append("\n");
         }
         if (showFields.contains(Fields.AUCTION_PRICE.getKey())) {
             sb.append(Fields.AUCTION_PRICE.getKey()).append(" ").append(fields.get(Fields.AUCTION_PRICE).getValue()).append("\n");
         }
         if (showFields.contains(Fields.CONDITIONS.getKey())) {
             sb.append(Fields.CONDITIONS.getKey()).append(" ").append(fields.get(Fields.CONDITIONS).getValue()).append("\n");
         }
         if (showFields.contains(Fields.SHIPPING_COST.getKey())) {
             sb.append(Fields.SHIPPING_COST.getKey()).append(" ").append(fields.get(Fields.SHIPPING_COST).getValue()).append("\n");
         }
         if (showFields.contains(Fields.TOTAL_COST.getKey())) {
             sb.append(Fields.TOTAL_COST.getKey()).append(" ").append(fields.get(Fields.TOTAL_COST).getValue()).append("\n");
         }
         if (showFields.contains(Fields.AUCTION_STATUS.getKey())) {
             sb.append(Fields.AUCTION_STATUS.getKey()).append(" ").append(fields.get(Fields.AUCTION_STATUS).getValue()).append("\n");
         }
         if (showFields.contains(Fields.CLOSE_DATE.getKey())) {
             sb.append(Fields.CLOSE_DATE.getKey()).append(" ").append(dateformatter.format(item.getCloseDate().getTime())).append("\n");
         }
         if (showFields.contains(Fields.TOTAL_BID.getKey())) {
             sb.append(Fields.TOTAL_BID.getKey()).append(" ").append(item.getTotalBid()).append("\n");
         }
         if (showFields.contains(Fields.GOLDEN.getKey())) {
             sb.append(Fields.GOLDEN.getKey()).append(" ").append(item.getGolden()).append("\n");
         }
         if (showFields.contains(Fields.POSITIVE_FEEDBACK_PERCENT.getKey()) && fields.get(Fields.POSITIVE_FEEDBACK_PERCENT) != null) {
             sb.append(Fields.POSITIVE_FEEDBACK_PERCENT.getKey()).append(" ").append(fields.get(Fields.POSITIVE_FEEDBACK_PERCENT).getValue()).append("\n");
         }
         if (showFields.contains(Fields.FEEDBACK_SCORE.getKey()) && fields.get(Fields.FEEDBACK_SCORE) != null) {
             sb.append(Fields.FEEDBACK_SCORE.getKey()).append(" ").append(fields.get(Fields.FEEDBACK_SCORE).getValue()).append("\n");
         }
         if (showFields.contains(Fields.TOP_RATED_SELLER.getKey()) && fields.get(Fields.TOP_RATED_SELLER) != null) {
             sb.append(Fields.TOP_RATED_SELLER.getKey()).append(" ").append(fields.get(Fields.TOP_RATED_SELLER).getValue()).append("\n");
         }
         if (showFields.contains(Fields.SHIP_TO_LOCATION.getKey()) && fields.get(Fields.SHIP_TO_LOCATION) != null) {
             sb.append(Fields.SHIP_TO_LOCATION.getKey()).append(" ").append(fields.get(Fields.SHIP_TO_LOCATION).getValue()).append("\n");
         }
         if (showFields.contains(Fields.HANDLING_TIME.getKey()) && fields.get(Fields.HANDLING_TIME) != null) {
             sb.append(Fields.HANDLING_TIME.getKey()).append(" ").append(fields.get(Fields.HANDLING_TIME).getValue()).append("\n");
         }
         if (showFields.contains(Fields.LISTING_TYPE.getKey()) && fields.get(Fields.LISTING_TYPE) != null) {
             sb.append(Fields.LISTING_TYPE.getKey()).append(" ").append(fields.get(Fields.LISTING_TYPE).getValue()).append("\n");
         }
         sb.append("\n");
     }
     
     /**
      * This method prints result searching with system setting
      * @param items Map searching result
      * @param id product id
      * @param type type product
      * @return formatted text
      */
     public static String formatForConsole(Map<SearchItem, Boolean> items, String id, String type) {
         StringBuilder sb = new StringBuilder();
         sb.append(type).append(" (").append(id).append(")").append("\n\n");
         List<String> opts = ManagerDAO.getInstance().getSystemSettingDAO().getChooseOptsValue();
         for (Map.Entry<SearchItem, Boolean> entry : items.entrySet())  {
              SearchItem item = entry.getKey();
              buildOneItem(sb, item, opts, entry.getValue());
         }
         return sb.toString();
     }
     
     public static void buildOneItem(final StringBuilder sb, SearchItem item, List<String> opts, Boolean golden) {
         for (String valueOpt : opts) {
             if ("autoPay".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(String.valueOf(item.isAutoPay())).append("\n");
             }
             if ("charityId".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getCharityId()).append("\n");
             }
             if ("compatibility".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getCompatibility()).append("\n");
             }
             if ("conditionDisplayName".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getCondition().getConditionDisplayName()).append("\n");
             }
             if ("country".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getCountry()).append("\n");
             }
             if ("distance".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getDistance()).append("\n");
             }
             if ("galleryURL".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getGalleryURL()).append("\n");
             }
             if ("galleryPlusPictureURL".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getGalleryPlusPictureURL()).append("\n");
             }
             if ("globalId".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getGlobalId()).append("\n");
             }
             if ("itemId".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getItemId()).append("\n");
             }
             if ("bestOfferEnabled".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getListingInfo().isBestOfferEnabled()).append("\n");
             }
             if ("buyItNowAvailable".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getListingInfo().isBuyItNowAvailable()).append("\n");
             }
             if ("buyItNowPrice".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getListingInfo().getBuyItNowPrice()).append("\n");
             }
             if ("convertedBuyItNowPrice".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getListingInfo().getConvertedBuyItNowPrice()).append("\n");
             }
             if ("endTime".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(dateformatter.format(item.getListingInfo().getEndTime().getTime())).append("\n");
             }
             if ("gift".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getListingInfo().isGift()).append("\n");
             }
             if ("listingType".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getListingInfo().getListingType()).append("\n");
             }
             if ("startTime".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(dateformatter.format(item.getListingInfo().getStartTime().getTime())).append("\n");
             }
             if ("primaryCategoryId".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getPrimaryCategory().getCategoryId()).append("\n");
             }
             if ("primaryCategoryName".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getPrimaryCategory().getCategoryName()).append("\n");
             }
             if ("location".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getLocation()).append("\n");
             }
             if ("paymentMethod".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getPaymentMethod()).append("\n");
             }
             if ("postalCode".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getPostalCode()).append("\n");
             }
             if ("productId".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getProductId().getValue()).append("\n");
             }
             if ("returnsAccepted".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.isReturnsAccepted()).append("\n");
             }
             if ("secondaryCategoryId".equals(valueOpt) && item.getSecondaryCategory() != null) {
                 sb.append(valueOpt).append(" : ").append(item.getSecondaryCategory().getCategoryId()).append("\n");
             }
             if ("secondaryCategoryName".equals(valueOpt) && item.getSecondaryCategory() != null) {
                 sb.append(valueOpt).append(" : ").append(item.getSecondaryCategory().getCategoryName()).append("\n");
             }
             if ("feedbackRatingStar".equals(valueOpt)) {
                 String value = item.getSellerInfo() != null ? item.getSellerInfo().getFeedbackRatingStar() : "";
                 sb.append(valueOpt).append(" : ").append(value).append("\n");
             }
             if ("feedbackScore".equals(valueOpt)) {
                 String value = item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo().getFeedbackScore()) : "";
                 sb.append(valueOpt).append(" : ").append(value).append("\n");
             }
             if ("positiveFeedbackPercent".equals(valueOpt)) {
                 String value = item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo().getPositiveFeedbackPercent()) : "";
                 sb.append(valueOpt).append(" : ").append(value).append("\n");
             }
             if ("sellerUserName".equals(valueOpt)) {
                 String value = item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo()) : "";
                 sb.append(valueOpt).append(" : ").append(value).append("\n");
             }
             if ("topRatedSeller".equals(valueOpt)) {
                 String value = item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo().isTopRatedSeller()) : "";
                 sb.append(valueOpt).append(" : ").append(value).append("\n");
             }
             if ("bidCount".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getSellingStatus().getBidCount()).append("\n");
             }
             if ("convertedCurrentPrice".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getSellingStatus().getConvertedCurrentPrice()).append("\n");
             }
             if ("currentPrice".equals(valueOpt)) {
                 String value = String.valueOf(item.getSellingStatus().getCurrentPrice().getValue());
                 String rate = item.getSellingStatus().getCurrentPrice().getCurrencyId();
                 sb.append(valueOpt).append(" : ").append(getPrice(item.getSellingStatus().getCurrentPrice())).append("\n");
             }
             if ("sellingState".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getSellingStatus().getSellingState()).append("\n");
             }
             if ("timeLeft".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(buildTime(item.getSellingStatus().getTimeLeft())).append("\n");
             }
             if ("expeditedShipping".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getShippingInfo().isExpeditedShipping()).append("\n");
             }
             if ("handlingTime".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getShippingInfo().getHandlingTime()).append("\n");
             }
             if ("oneDayShippingAvailable".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getShippingInfo().isOneDayShippingAvailable()).append("\n");
             }
             if ("shippingServiceCost".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(getPrice(item.getShippingInfo().getShippingServiceCost())).append("\n");
             }
             if ("shippingType".equals(valueOpt)) {
 
                 sb.append(valueOpt).append(" : ").append(item.getShippingInfo().getShippingType()).append("\n");
             }
             if ("shipToLocations".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getShippingInfo().getShipToLocations()).append("\n");
             }
             if ("storeName".equals(valueOpt) && item.getStoreInfo() != null) {
                 sb.append(valueOpt).append(" : ").append(item.getStoreInfo().getStoreName()).append("\n");
             }
             if ("storeURL".equals(valueOpt) && item.getStoreInfo() != null) {
                 sb.append(valueOpt).append(" : ").append(item.getStoreInfo().getStoreURL()).append("\n");
             }
             if ("subtitle".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getSubtitle()).append("\n");
             }
             if ("title".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getTitle()).append("\n");
             }
             if ("viewItemURL".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(item.getViewItemURL()).append("\n");
             }
             if ("description".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(SearchUtil.getInstance().getProductByItemNumber(item.getItemId()).getDescription()).append("\n");
             }
             if ("golden".equals(valueOpt)) {
                 sb.append(valueOpt).append(" : ").append(String.valueOf(golden)).append("\n");
             }
         }
         sb.append("\n");
     }
 
     public static String buildTime(Duration duration) {
         StringBuilder sb = new StringBuilder();
         sb.append(duration.getDays()).append(" days, ").append(duration.getHours()).append(" hours, ").append(duration.getMinutes()).append(" minutes, ").append(duration.getSeconds()).append(" seconds.");
         return sb.toString();
     }
     
     public static String addAmount(Amount shipping, Amount price) {
         if (shipping != null && price != null) {
             return String.valueOf(shipping.getValue() + price.getValue());
         } else {
             return null;
         }        
     }
 
     public static String getPrice(Amount amount) {
         if (amount != null) {
             return String.valueOf(amount.getValue());
         } else {
             return null;
         }
     }
 
     public static String getCurrency(Amount amount) {
         if (amount != null) {
             return amount.getCurrencyId();
         } else {
             return null;
         }
     }
 }
