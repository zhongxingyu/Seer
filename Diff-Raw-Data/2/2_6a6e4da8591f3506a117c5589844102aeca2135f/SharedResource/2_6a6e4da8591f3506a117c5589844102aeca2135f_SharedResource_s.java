 /*-
  * Copyright 2010 Wyona
  */
 
 package org.wyona.yanel.resources.konakart.shared;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.map.Realm;
 import org.wyona.yanel.servlet.security.impl.DefaultWebAuthenticatorImpl;
 import org.wyona.yanel.servlet.IdentityMap;
 import org.wyona.yanel.servlet.YanelServlet;
 import org.wyona.yarep.core.Repository;
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.lang.Integer;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.servlet.http.HttpSession;
 import java.util.Map;
 import java.util.HashMap;
 
 import com.konakart.app.Basket;
 import com.konakart.app.EngineConfig;
 import com.konakart.app.KKEng;
 import com.konakart.app.ProductSearch;
 import com.konakart.app.CustomerRegistration;
 
 import com.konakart.appif.BasketIf;
 import com.konakart.appif.KKEngIf;
 import com.konakart.appif.CategoryIf;
 import com.konakart.appif.LanguageIf;
 import com.konakart.appif.CountryIf;
 import com.konakart.appif.ManufacturerIf;
 import com.konakart.appif.ProductIf;
 import com.konakart.appif.ProductsIf;
 import com.konakart.appif.ProductSearchIf;
 import com.konakart.appif.ProductsIf;
 import com.konakart.appif.OrderIf;
 import com.konakart.appif.ShippingQuoteIf;
 import com.konakart.appif.CustomerRegistrationIf;
 import com.konakart.appif.CustomerIf;
 import com.konakart.appif.CreateOrderOptionsIf;
 import com.konakart.appif.EngineConfigIf;
 import com.konakart.util.KKConstants;
 import com.konakart.app.CreateOrderOptions;
 import com.konakart.app.EngineConfig;
 import com.konakart.app.ShippingQuote;
 import com.konakart.app.KKWSEng;
 
 /**
  * Shared functions.
  */
 public class SharedResource extends Resource {
     public static String KONAKART_SESSION_ID = "konakart-session-id";
     public static String KONAKART_DEFAULT_STOREID = "store1";
 
     private static Logger log = Logger.getLogger(SharedResource.class);
 
     private KKEngIf kkEngine;                   // Info: Default store engine
     private Map<String, KKEngIf> kkEngineMap;   // Info: Engines for other stores
 
     /**
      * Constructor.
      */
     public SharedResource() throws Exception {
         kkEngineMap = new HashMap(); 
         kkEngine = getKonakartEngineImpl(KONAKART_DEFAULT_STOREID);
     }
 
     /**
      * Get language ID.
      * @param lang Language, e.g. 'de', 'fr'
      * @return KonaKart language ID, e.g. 1, 2
      */
     public int getLanguageId(String lang) throws Exception {
        log.warn("DEBUG: Try to get ID for language: " + lang);
         LanguageIf kkLang = null;
         try {
             kkLang = getKonakartEngineImpl().getLanguagePerCode(lang);
         } catch(Exception e) {
             log.error(e, e);
         }
 
         int kkLangInt = -1;
         if (kkLang != null) {
             kkLangInt = kkLang.getId();
         } else {
             log.warn("Could not get ID for language: " + lang);
         }
 
         return kkLangInt;
     }
 
     /**
      * Get KonaKart engine implementation for default store.
      */
     public KKEngIf getKonakartEngineImpl() {
         return kkEngine;
     }
 
     /**
      * Get KonaKart engine implementation for a specific store.
      */
     public KKEngIf getKonakartEngineImpl(String storeId) throws Exception {
         // We only instantiate the the KonaKart engine once,
         // and then store it in a map for re-use later
         // (since we'll most likely need it more than once)
         if(!kkEngineMap.containsKey(storeId)) {
             EngineConfigIf conf = new EngineConfig();
             conf.setStoreId(storeId);
             kkEngine = new KKWSEng(conf);
             kkEngineMap.put(storeId, kkEngine);
         }
 
         return kkEngineMap.get(storeId);
     }
 
     /**
      * Get customer ID for an anyonymous user or signed-in user.
      * @param session The HTTP servlet session.
      */
     public int getCustomerId(HttpSession session) throws Exception {
         String konakartSessionId = getSessionId(session);
         if (konakartSessionId != null && konakartSessionId.length() > 0) {
             return getKonakartEngineImpl().getCustomer(konakartSessionId).getId();
         } else {
             return getTemporaryCustomerId(session);
         }
     }
 
     /**
      * Get temporary customer ID for an anyonymous user.
      * @param session The HTTP servlet session.
      */
     public int getTemporaryCustomerId(HttpSession session) throws Exception {
         String KONAKART_TMP_CUSTOMER_ID = "konakart-tmp-customer-id";
         Integer customerIdInt = (Integer) session.getAttribute(KONAKART_TMP_CUSTOMER_ID);
 
         if (customerIdInt != null) {
             return customerIdInt.intValue();
         } else {
             int customerId = getKonakartEngineImpl().getTempCustomerId();
             session.setAttribute(KONAKART_TMP_CUSTOMER_ID, new Integer(customerId));
             return customerId;
         }
     }
 
     /**
      * Get KonaKart session ID.
      * @param session The HTTP servlet session.
      */
     public String getSessionId(HttpSession session) throws Exception {
         String sessionId = (String) session.getAttribute(KONAKART_SESSION_ID);
 
         if (sessionId != null) {
             // We have to make sure the session hasn't expired, or otherwise
             // we'll get exceptions everytime we try to use it! Konakart doesn't
             // have a function to check this, so this is what we do...
             try {
                 // According to the docs, getCustomer() only throws an exception
                 // if the session is expired so this should be a reliable check.
                 getKonakartEngineImpl().getCustomer(sessionId);
             } catch(Exception e) {
                 // Now we need to generate a new sessionId
                 session.removeValue(KONAKART_SESSION_ID);
                 sessionId = null;
             }
         }
 
         return sessionId;
     }
 
     /**
      * Get customer object associated with session.
      */
     public CustomerIf getCustomer(String sessionId, int customerId) throws Exception {
         if (customerId > 0) {
             CustomerIf customer = getKonakartEngineImpl().getCustomer(sessionId);
             return customer;
         } else {
             // Not signed in
             return null;
         }
     }
 
     /**
      * Get item units (Flaschen/Stueck/Pieces/...).
      * If you set abbrev to true, it will return the short
      * (abbreviated version), e.g. Fl instead of Flaschen.
      */
     public String getItemUnits(ProductIf product, String languageCode, boolean abbrev) {
         // TODO: This is a very specific/customized function.
         // Should be replaced it with a more generic mechanism.
         if(product == null) return "";
 
         String model = product.getManufacturerName();
         if(model != null && model.length() > 0) {
             // If it's a wine...
             if(model.contains("wein")
             || model.contains("Wein")) {
                 if("fr".equalsIgnoreCase(languageCode)) {
                     if(abbrev) {
                         return "bout.";
                     } else {
                         return "Bouteilles";
                     }
                 } else {
                     if(abbrev) {
                         return "Fl.";
                     } else {
                         return "Flaschen";
                     }
                 }
             }
 
             // If it's a gift basket...
             if(model.contains("geschenk")
             || model.contains("Geschenk")) {
                 if("fr".equalsIgnoreCase(languageCode)) {
                     if(abbrev) {
                         return "pces";
                     } else {
                         return "Quantité";
                     }
                 } else {
                     if(abbrev) {
                         return "St.";
                     } else {
                         return "Anzahl";
                     }
                 }
             }
         }
 
         return "";
     }
 
     /**
      * Perform login.
      */
     public String login(String username, String password, Realm realm, HttpSession session) throws Exception {
         // INFO: Yanel authentication 
         String currentYanelUserId = null;
         org.wyona.security.core.api.Identity identity = YanelServlet.getIdentity(session, realm);
         if (identity != null) {
             currentYanelUserId = identity.getUsername();
         }
         if(currentYanelUserId == null) {
             if(DefaultWebAuthenticatorImpl.authenticate(username, password, realm, session)) { // TODO: replace by getRealm().getWebAuthenticator().doAuthenticate() ...
                 log.info("Yanel authentication successful for user: " + username);
             } else {
                 log.warn("Yanel authentication failed for user: " + username);
             }
         } else {
             log.warn(
                 "Alread signed into Yanel as '" + currentYanelUserId + 
                 "', hence do not authenticate at Yanel as '" + username + 
                 "', whereas please note that user probably has different " +
                 "IDs for Yanel and KonaKart.");
         }
 
         // INFO: Konakart authentication
         try { 
             String id = getKonakartEngineImpl().login(username, password);
             if(id != null) {
                 // OPTIONAL: Transfer shopping cart from temporary negative 
                 // customer ID to session of signed-in user to make it persistent.
                 // E.g; use margeBaskets in KKEngIf interface; see also documentation at
                 // http://www.konakart.com/javadoc/server/com/konakart/appif/KKEngIf.html
                 session.setAttribute(KONAKART_SESSION_ID, id);
             }
             return id;
         } catch(Exception e) {
             return null;
         }
     }
 
     /**
      * Perform logout.
      */
     public void logout(Realm realm, HttpSession session) throws Exception {
         // INFO: Yanel logout
         IdentityMap identityMap = (IdentityMap) session.getAttribute(YanelServlet.IDENTITY_MAP_KEY);
 
         if(identityMap != null && identityMap.containsKey(realm.getID())) {
             log.debug("Logout from realm: " + realm.getID());
             identityMap.remove(realm.getID());
         }
 
         // INFO: KonaKart logout
         String sessionId = getSessionId(session);
         if(sessionId != null) getKonakartEngineImpl().logout(sessionId);
         session.removeValue(KONAKART_SESSION_ID);
     }
 
     /**
      * Calculate the shipping cost for a basket.
      * TODO: Make this function more generic.
      */
     public ShippingQuoteIf getShippingCost(BasketIf[] items, String sessionId, int languageId) throws Exception {
         // We calculate the shipping cost ourselves in this function,
         // because Konakart has certain limitations as to what you can do.
         CreateOrderOptionsIf orderOptions = new CreateOrderOptions();
         orderOptions.setUseDefaultCustomer(sessionId == null);
 
         ShippingQuoteIf shipping = new ShippingQuote();
 
         // And now we calculate the shipping cost...
         // Shiping is diffrent for wine and for gift baskets.
         // In order to find out whether a product is a wine or
         // a gift basket, we use the getModel() function.
         int bottles = 0, baskets = 0;
         BigDecimal bottles_price = new BigDecimal("0");
         BigDecimal baskets_price = new BigDecimal("0");
 
         // Some wines also have "Mengenrabatt". We need to know how many.
         int bottles_rebate = 0;
         BigDecimal bottles_rebate_price = new BigDecimal("0");
 
         for(BasketIf item : items) {
             String model = item.getProduct().getManufacturerName();
             if(model != null && model.length() > 0) {
                 // If it's a wine...
                 if(model.contains("wein")
                 || model.contains("Wein")) {
                     log.debug("It's a wine");
                     bottles = bottles + item.getQuantity();
                     // Multiply quantity with product price, add to total
                     bottles_price = bottles_price.add(item.getFinalPriceExTax());
                     // Rebate/discount?
                     if(model.contains("mit")) {
                         bottles_rebate = bottles_rebate + item.getQuantity();
                         bottles_rebate_price = bottles_rebate_price.add(item.getFinalPriceExTax());
                     }
                     continue;
                 }
 
                 // If it's a gift basket...
                 if(model.contains("geschenk")
                 || model.contains("Geschenk")) {
                     log.debug("It's a basket");
                     baskets = baskets + item.getQuantity();
                     // Multiply quantity with product price, add to total
                     baskets_price = baskets_price.add(item.getFinalPriceExTax());
                     continue;
                 }
             }
 
             // If we end up here, we have an unknown/invalid product.
             // I guess we'll just have to skip it then (what else to do?).
             log.debug("It's unknown");
         }
 
         BigDecimal zero = new BigDecimal("0");
         BigDecimal bottles_shipping_price = null;
         BigDecimal baskets_shipping_price = null;
         BigDecimal bottles_total_rebate = null;
 
         // Reset the shipping cost to zero
         shipping.setCost(zero);
         shipping.setHandlingCost(zero);
         shipping.setTax(zero);
         shipping.setTotalExTax(zero);
         shipping.setTotalIncTax(zero);
 
         // Shipping for wine
         if(bottles != 0) {
             bottles_shipping_price = new BigDecimal("0");
             if(bottles_price.compareTo(new BigDecimal("1000")) < 0) {  
                 if(bottles > 0 && bottles < 13) {
                     bottles_shipping_price = new BigDecimal("15");
                 } else if(bottles > 12 && bottles < 24) {
                     bottles_shipping_price = new BigDecimal("30");
                 }
             }
         }
 
         if(bottles_shipping_price != null) {
             try {
                 shipping.setCustom1(bottles_shipping_price.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
             } catch(Exception e) {
                 shipping.setCustom1(bottles_shipping_price.toString());
             }
         } else {
             shipping.setCustom1("");
         }
 
         // Shipping for baskets
         if(baskets != 0) {
             baskets_shipping_price = new BigDecimal("0");
             if(baskets_price.compareTo(new BigDecimal("1000")) < 0) {
                 baskets_shipping_price = new BigDecimal("30");
             }
         }
 
         if(baskets_shipping_price != null) {
             try {
                 shipping.setCustom2(baskets_shipping_price.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
             } catch(Exception e) {
                 shipping.setCustom2(baskets_shipping_price.toString());
             }
         } else {
             shipping.setCustom2("");
         }
 
         // Rebate for wine
         if(bottles_rebate >= 96) {
             bottles_total_rebate = bottles_rebate_price.multiply(new BigDecimal("-0.10"));
         } else if(bottles_rebate >= 48) {
             bottles_total_rebate = bottles_rebate_price.multiply(new BigDecimal("-0.05"));
         } else if(bottles_rebate >= 24) {
             bottles_total_rebate = bottles_rebate_price.multiply(new BigDecimal("-0.03"));
         } else {
             bottles_total_rebate = new BigDecimal("0");
         }
 
         // Round up to the next 0.05 unit
         // Yes, this is ugly, but I didn't find a better way to do it
         BigDecimal unit = new BigDecimal("0.05");
         bottles_total_rebate = bottles_total_rebate.divide(unit).setScale(0, BigDecimal.ROUND_HALF_UP).multiply(unit);
         
         // And convert to string...
         shipping.setCustom3(bottles_total_rebate.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
 
         // Total shipping
         BigDecimal total;
         if(bottles_shipping_price != null && baskets_shipping_price != null) { 
             total = bottles_shipping_price.add(baskets_shipping_price);
         } else if(bottles_shipping_price != null) {
             total = bottles_shipping_price;
         } else if(baskets_shipping_price != null) {
             total = baskets_shipping_price;
         } else {
             total = new BigDecimal("0");
         }
 
         shipping.setTotalExTax(total);
         shipping.setTotalIncTax(bottles_total_rebate);
 
         // Name of shipping method
         shipping.setResponseText("Frais de port");
         shipping.setTitle("Lieferkosten");
 
         return shipping;
     }
 
     /**
      * Check whether KonaKart is online/offline
      *
      * @param realm Realm which contains online/offline flag
      *
      * @return False if offline and true if online
      */
     public boolean isKKOnline(Realm realm) {
         // First we check for the existence of a file
         // in the default repository, this allows us to
         // let the shop appear to be offline for maintenance
 
         String offlinePath = "/go-offline";
 
         try {
             Repository repo = realm.getRepository();
     
             if(repo.existsNode(offlinePath)) {
                 log.warn("KonaKart offline flag exists: '" + offlinePath  + "' (" + repo + ")");
                 return false;
             }
         } catch(Exception e) {
             log.warn(e,e);
             return false;
         }
         
         try {
             // We use getStore to check if Konakart is online
             // because getStore has no side effects, it just
             // return some configuration parameters.
             getKonakartEngineImpl().getStore();
         } catch(Exception e) {
             // If getStore fails, we assume Konakart is down.
             log.warn(e.getMessage());
             log.warn("KonaKart seems to be offline, but no '" + offlinePath + "' flag set. Check status of KonaKart and corresponding DB!");
             return false;
         }
 
         return true;
     }
 }
