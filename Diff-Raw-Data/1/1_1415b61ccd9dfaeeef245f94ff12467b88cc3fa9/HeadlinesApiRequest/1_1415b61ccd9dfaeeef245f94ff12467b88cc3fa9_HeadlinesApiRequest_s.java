 package com.infinitemule.espn.api.headlines;
 
 import java.util.Map;
 
 import com.infinitemule.espn.common.api.ApiRequest;
 import com.infinitemule.espn.common.api.ApiUrls.Headlines;
 import com.infinitemule.espn.common.api.City;
 
 public class HeadlinesApiRequest extends ApiRequest {
 
   private String type;
   
   private City city = null;
   
     
   public HeadlinesApiRequest news() {    
     setType(Headlines.news);
     return this;
   }
   
   public HeadlinesApiRequest headlines() {    
     setType(Headlines.headlines);
     return this;
   }
   
   public HeadlinesApiRequest topHeadlines() {    
     setType(Headlines.topHeadlines);
     return this;
   }
   
   public HeadlinesApiRequest forAllCities() {
     setMethod(Headlines.allCities + getType());
     return this;
   }
   
   public HeadlinesApiRequest forCity(City city) {
     setMethod(Headlines.byCity + getType());
     return this;
   }
 
   @Override
   public Map<String, String> getUrlParams() {
     
     Map<String, String> urlParams = super.getUrlParams();        
     
     if(isSpecified(getCity())) {
       urlParams.put(Headlines.Params.city, getCity().getId());
     }
     
     return urlParams;
     
   }
   
   @Override
   public Map<String, String> getQueryParams() {
     
     Map<String, String> queryParams = super.getQueryParams();
     
     queryParams.putAll(createPageableParams());
     
     return queryParams;
     
   }
 
   public String getType() {
     return type;
   }
     
   public void setType(String type) {
     this.type = type;  
   }
 
   public City getCity() {
     return city;
   }
 
   public void setCity(City city) {
     this.city = city;
   }
     
   
 }
