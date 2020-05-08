 package com.owfg.facade.ws.StoreManagement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: tomnightingale
  * Date: 11-05-10
  * Time: 3:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Factory {
     private static final String names[] = {
             "Vancouver",
             "Burnaby",
             "Richmond"
     };
 
     private static final String bannerNames[] = {
             "Banner 1",
             "Banner 2",
             "Banner 3"
     };
 
     ArrayList<Store> stores;
     ArrayList<Banner> banners;
 
     private int currentStore = 0;
     private int currentBanner = 0;
 
     Factory() {
        stores = new ArrayList<Store>();
        banners = new ArrayList<Banner>();

         Store store;
        Banner banner;

         while ((store = createStore()) != null) {
             stores.add(store);
         }

        while ((banner = createBanner()) != null) {
            banners.add(banner);
        }
     }
 
     public List<Store> getStores() {
         return stores;
     }
 
     public List<Banner> getBanners() {
         return banners;
     }
 
     public StoreManagementInfo getStoreManagementInfo(long storeid, String upc) {
         StoreManagementInfo info = new StoreManagementInfo();
 
         info.setStoreId(storeid);
         info.setBalanceOnHand(10d);
         info.setForecast(10d);
         info.setInTransit(10d);
         info.setItemDescription("A really cool item.");
         info.setMinimum(5l);
         info.setOnOrder(20d);
         info.setPack(4);
         info.setPromotion("This is a promotion.");
         info.setRegularPrice(20d);
         info.setUpc(upc);
 
         return info;
     }
 
     private Store createStore() {
         if (currentStore >= names.length) {
             return null;
         }
 
         Store store = new Store();
         store.setBannerId(currentStore);
         store.setStoreId(currentStore);
         store.setStoreName(names[currentStore]);
         currentStore++;
 
         return store;
     }
 
     private Banner createBanner() {
         if (currentBanner >= bannerNames.length) {
             return null;
         }
 
         Banner banner = new Banner();
         banner.setBannerId(currentBanner);
         banner.setBannerName(bannerNames[currentBanner]);
         currentBanner++;
 
         return banner;
     }
 }
