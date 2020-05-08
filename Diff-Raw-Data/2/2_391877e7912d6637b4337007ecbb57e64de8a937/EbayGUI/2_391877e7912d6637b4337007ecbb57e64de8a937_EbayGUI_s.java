 package com.ebaytools.gui.panel;
 
import com.ebaytools.gui.linteners.OpenProductIDDialogListener;
import com.ebaytools.gui.linteners.OpenShowProductDialogListener;
 import com.ebaytools.gui.model.Data;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.swing.*;
 
 /**
  * This is main frame. There we can show all com.ebaytools.gui components.
  * @author Admin
  *
  */
 public class EbayGUI extends JFrame {
     private EbayGUI gui;
     private Data data;
 
     public EbayGUI(Data data) throws HeadlessException {
         this.data = data;
     }
 
     public void init() {
         this.gui = this;
         gui.setTitle("Ebay tools");
         gui.setResizable(false);
         gui.add(new SearchPanel(gui, data), BorderLayout.CENTER);
         gui.setSize(1000, 700);
         centre(this);
         gui.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         gui.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 closePerform(gui);
             }
         });
     }
 
     public static void centre(Window w) {
         Dimension us = w.getSize();
         Dimension them = Toolkit.getDefaultToolkit().getScreenSize();
         int newX = (them.width - us.width) / 2;
         int newY = (them.height - us.height) / 2;
         w.setLocation(newX, newY);
 
     }
 
     public static void closePerform(Frame gui) {
         gui.setVisible(false);
         gui.dispose();
         System.exit(0);
     }
 
     /**
      * This map consists from options which we can see, if i's needed I can add new options
      */
     public final static Map<String, Boolean> fields = new LinkedHashMap<String, Boolean>();
     static {
         fields.put("autoPay", false);
         fields.put("charityId", false);
         fields.put("compatibility", false);
         fields.put("conditionDisplayName", false);
         fields.put("country", false);
         fields.put("distance", false);
         fields.put("galleryInfoContainer", false);
         fields.put("galleryURL", false);
         fields.put("globalId", false);
         fields.put("itemId", true);
         fields.put("+listingInfo", false);
         fields.put("--bestOfferEnabled", false);
         fields.put("--buyItNowAvailable", false);
         fields.put("--buyItNowPrice", false);
         fields.put("--convertedBuyItNowPrice", false);
         fields.put("--endTime", false);
         fields.put("--gift", false);
         fields.put("--listingType", false);
         fields.put("--startTime", false);
         fields.put("+primaryCategory", false);
         fields.put("--primaryCategoryId", false);
         fields.put("--primaryCategoryName", false);
         fields.put("+secondaryCategory", false);
         fields.put("--secondaryCategoryId", false);
         fields.put("--secondaryCategoryName", false);
         fields.put("location", false);
         fields.put("paymentMethod", false);
         fields.put("postalCode", false);
         fields.put("productId", false);
         fields.put("returnsAccepted", false);
         fields.put("+sellerInfo", false);
         fields.put("--feedbackRatingStar", false);
         fields.put("--feedbackScore", false);
         fields.put("--positiveFeedbackPercent", false);
         fields.put("--sellerUserName", false);
         fields.put("--topRatedSeller", false);
         fields.put("+sellingStatus", false);
         fields.put("--bidCount", false);
         fields.put("--convertedCurrentPrice", false);
         fields.put("--currentPrice", false);
         fields.put("--sellingState", false);
         fields.put("--timeLeft", false);
         fields.put("+shippingInfo", false);
         fields.put("--expeditedShipping", false);
         fields.put("--handlingTime", false);
         fields.put("--oneDayShippingAvailable", false);
         fields.put("--shippingServiceCost", false);
         fields.put("--shippingType", false);
         fields.put("--shipToLocations", false);
         fields.put("+storeInfo", false);
         fields.put("--storeName", false);
         fields.put("--storeURL", false);
         fields.put("subtitle", false);
         fields.put("title", false);
         fields.put("viewItemURL", false);
         fields.put("description", false);
     }
 }
