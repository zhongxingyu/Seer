 package models;
 
 import com.google.gson.Gson;
 
 import java.util.List;
 
 public class AdsService {
 
     private final Gson GSON = new Gson();

     private static final StaticAdRepository repository = new StaticAdRepository();
 
     public String getAdsForSeller(int sellerId) {
         List<Integer> ads = repository.getAdsForSeller(sellerId);
         return GSON.toJson(ads);
     }
 
     public void addSellerAd(int sellerId, int adId) {
         repository.addAdForSeller(sellerId, adId);
     }
 
     public void removeSellerAd(int sellerId, int adId) {
         repository.removeAdForSeller(sellerId, adId);
     }
 }
