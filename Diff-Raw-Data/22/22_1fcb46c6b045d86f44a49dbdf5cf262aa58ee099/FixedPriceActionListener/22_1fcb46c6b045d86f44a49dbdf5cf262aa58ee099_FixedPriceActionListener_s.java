 package com.ebaytools.gui.linteners;
 
 import au.com.bytecode.opencsv.CSVWriter;
 import com.ebay.services.finding.SearchItem;
 import com.ebay.services.finding.SortOrderType;
 import com.ebaytools.gui.model.Data;
 import com.ebaytools.kernel.dao.ManagerDAO;
 import com.ebaytools.kernel.entity.FileSearching;
 import com.ebaytools.util.FileUtil;
 import com.ebaytools.util.FormatterText;
 import com.ebaytools.util.SearchUtil;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.StringWriter;
 import java.util.*;
 
 public class FixedPriceActionListener implements ActionListener {
     private static final Logger log = Logger.getLogger(FixedPriceActionListener.class);
 
     private Data data;
     private JFrame main;
 
     public FixedPriceActionListener(JFrame main, Data data) {
         this.main = main;
         this.data = data;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         List<FileSearching> files = ManagerDAO.getInstance().getFileSearchingDAO().getFileSearchingFixedPrice();
         for (FileSearching fileSearch : files) {
             List<String> loadId = new ArrayList<String>();
             File file = new File(fileSearch.getPath());
             if (file.exists()) {
                 List<String> data = LoadFileSearchItemActionListener.readFile(file);
                 loadId.addAll(data);
             } else {
                 log.error("File " + file.getPath() + " is not exist! ");
             }
             Map<String, SearchItem> relevantItems = new LinkedHashMap<String, SearchItem>(5);
             for (String reference : loadId) {
                 List<SearchItem> items = SearchUtil.getInstance().getItemsBySortedType(reference, "2000;2500", "FixedPrice", SortOrderType.CURRENT_PRICE_HIGHEST, "ReferenceID", null);
                 List<SearchItem> items3000 = SearchUtil.getInstance().getItemsBySortedType(reference, "3000", "FixedPrice", SortOrderType.CURRENT_PRICE_HIGHEST, "ReferenceID", null);
                 relevantItems.put("B", detectCheapestPrice(items));
                 relevantItems.put("C", detectCheapestPriceWithTopRate(items));
                 relevantItems.put("D", detectCheapestPrice(items3000));
                 relevantItems.put("E", detectCheapestPriceWithTopRate(items3000));
                 relevantItems.put("F", detectCheapestPriceWithShippingOverWorld(items3000));
 
                List<String[]> datas = new ArrayList<String[]>();
                datas.add(buildHeader());
                 for (Map.Entry<String, SearchItem> entry : relevantItems.entrySet()) {
                     SearchItem item = entry.getValue();
                     if (item == null) {
                         datas.add(new String[48]);
                     } else {
                         datas.add(buildField(item));
                     }
                 }
 
                StringWriter sw = new StringWriter();
                CSVWriter writer = new CSVWriter(sw);
                writer.writeAll(datas);
                File save = new File("FixedPrice_Ref_" + reference + "_time_"+Calendar.getInstance().getTimeInMillis()+".csv");
                FileUtil.save(save, sw.toString());
                data.getText().setText(data.getText().getText() + "\nFile was saved successful part : " + save.getAbsolutePath());
             }
         }
     }
 
     private String[] buildField(SearchItem item) {
         List<String> list = new ArrayList<String>();
         list.add(String.valueOf(item.isAutoPay()));
         list.add(item.getCharityId());
         list.add(item.getCompatibility());
         list.add(item.getCondition().getConditionDisplayName());
         list.add(item.getCountry());
         list.add(String.valueOf(item.getDistance()));
         list.add(item.getGalleryURL());
         list.add(String.valueOf(item.getGalleryPlusPictureURL()));
         list.add(item.getGlobalId());
         list.add(item.getItemId());
         list.add(String.valueOf(item.getListingInfo().isBestOfferEnabled()));
         list.add(String.valueOf(item.getListingInfo().isBuyItNowAvailable()));
         list.add(String.valueOf(item.getListingInfo().getBuyItNowPrice()));
         list.add(String.valueOf(item.getListingInfo().getConvertedBuyItNowPrice()));
         list.add(FormatterText.dateformatter.format(item.getListingInfo().getEndTime().getTime()));
         list.add(String.valueOf(item.getListingInfo().isGift()));
         list.add(item.getListingInfo().getListingType());
         list.add(FormatterText.dateformatter.format(item.getListingInfo().getStartTime().getTime()));
         list.add(item.getPrimaryCategory().getCategoryId());
         list.add(item.getPrimaryCategory().getCategoryName());
         list.add(item.getLocation());
         list.add(String.valueOf(item.getPaymentMethod()));
         list.add(item.getPostalCode());
         list.add(String.valueOf(item.getProductId()));
         list.add(String.valueOf(item.isReturnsAccepted()));
         list.add(item.getSecondaryCategory() != null ? item.getSecondaryCategory().getCategoryId() : "");
         list.add(item.getSecondaryCategory() != null ? item.getSecondaryCategory().getCategoryName() : "");
         list.add(item.getSellerInfo() != null ? item.getSellerInfo().getFeedbackRatingStar() : "");
         list.add(item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo().getFeedbackScore()) : "");
         list.add(item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo().getPositiveFeedbackPercent()) : "");
         list.add(item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo()) : "");
         list.add(item.getSellerInfo() != null ? String.valueOf(item.getSellerInfo().isTopRatedSeller()) : "");
         list.add(String.valueOf(item.getSellingStatus().getBidCount()));
         list.add(String.valueOf(item.getSellingStatus().getConvertedCurrentPrice()));
         list.add(String.valueOf(item.getSellingStatus().getCurrentPrice().getValue()));
         list.add(item.getSellingStatus().getSellingState());
         list.add(FormatterText.buildTime(item.getSellingStatus().getTimeLeft()));
         list.add(String.valueOf(item.getShippingInfo().isExpeditedShipping()));
         list.add(String.valueOf(item.getShippingInfo().getHandlingTime()));
         list.add(String.valueOf(item.getShippingInfo().isOneDayShippingAvailable()));
         list.add(FormatterText.getPrice(item.getShippingInfo().getShippingServiceCost()));
         list.add(item.getShippingInfo().getShippingType());
         list.add(String.valueOf(item.getShippingInfo().getShipToLocations()));
         list.add(item.getStoreInfo() != null ? item.getStoreInfo().getStoreName() : "");
         list.add(item.getStoreInfo() != null ? item.getStoreInfo().getStoreURL() : "");
         list.add(item.getSubtitle());
         list.add(item.getTitle());
         list.add(item.getViewItemURL());
         return list.toArray(new String[list.size()]);
     }
 
     private String[] buildHeader() {
         return new String[] {"autoPay",
                 "charityId",
                 "compatibility",
                 "conditionDisplayName",
                 "country",
                 "distance",
                 "galleryURL",
                 "galleryPlusPictureURL",
                 "globalId",
                 "itemId",
                 "bestOfferEnabled",
                 "buyItNowAvailable",
                 "buyItNowPrice",
                 "convertedBuyItNowPrice",
                 "endTime",
                 "gift",
                 "listingType",
                 "startTime",
                 "primaryCategoryId",
                 "primaryCategoryName",
                 "location",
                 "paymentMethod",
                 "postalCode",
                 "productId",
                 "returnsAccepted",
                 "secondaryCategoryId",
                 "secondaryCategoryName",
                 "feedbackRatingStar",
                 "feedbackScore",
                 "positiveFeedbackPercent",
                 "sellerUserName",
                 "topRatedSeller",
                 "bidCount",
                 "convertedCurrentPrice",
                 "currentPrice",
                 "sellingState",
                 "timeLeft",
                 "expeditedShipping",
                 "handlingTime",
                 "oneDayShippingAvailable",
                 "shippingServiceCost",
                 "shippingType",
                 "shipToLocations",
                 "storeName",
                 "storeURL",
                 "subtitle",
                 "title",
                 "viewItemURL"};
     }
 
     private SearchItem detectCheapestPrice(List<SearchItem> items) {
         if (!items.isEmpty()) {
             SearchItem cheapestPrice = items.get(0);
             for (SearchItem item : items) {
                 double temp = item.getSellingStatus().getCurrentPrice().getValue();
                 double min = cheapestPrice.getSellingStatus().getCurrentPrice().getValue();
                 if (temp < min) {
                     cheapestPrice = item;
                 }
             }
             return cheapestPrice;
         } else {
             return null;
         }
     }
 
     private SearchItem detectCheapestPriceWithTopRate(List<SearchItem> items) {
         if (!items.isEmpty()) {
             SearchItem cheapestPrice = items.get(0);
             for (SearchItem item : items) {
                 if (item.getSellerInfo().isTopRatedSeller()) {
                     double temp = item.getSellingStatus().getCurrentPrice().getValue();
                     double min = cheapestPrice.getSellingStatus().getCurrentPrice().getValue();
                     if (temp < min) {
                         cheapestPrice = item;
                     }
                 }
             }
             return cheapestPrice.getSellerInfo().isTopRatedSeller() ? cheapestPrice : null;
         } else {
             return null;
         }
     }
 
     private SearchItem detectCheapestPriceWithShippingOverWorld(List<SearchItem> items) {
         if (!items.isEmpty()) {
             SearchItem cheapestPrice = items.get(0);
             for (SearchItem item : items) {
                 if (item.getShippingInfo().getShipToLocations().contains("Worldwide")) {
                     double temp = item.getSellingStatus().getCurrentPrice().getValue();
                     double min = cheapestPrice.getSellingStatus().getCurrentPrice().getValue();
                     if (temp < min) {
                         cheapestPrice = item;
                     }
                 }
             }
             return cheapestPrice.getShippingInfo().getShipToLocations().contains("Worldwide") ? cheapestPrice : null;
         } else {
             return null;
         }
     }
 }
