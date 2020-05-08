 package com.clouway.jobex.client.navigation;
 
 import com.google.gwt.place.shared.Place;
 import com.google.inject.Inject;
 
 import java.util.Map;
 
 /**
  * @author Adelin Ghanayem adelin.ghanaem@clouway.com
  */
 public class MenuItemMapperImpl implements MenuItemMapper {
 
 
   Map<String, Place> placeMap;
 
   @Inject
   public MenuItemMapperImpl(Map<String, Place> placeMap) {
     this.placeMap = placeMap;
   }
 
 
   public Place getPlace(String placeName) {
    return placeMap.get(placeName);
   }
 
   public void addPlace(String placeName, Place place) {
     placeMap.put(placeName, place);
   }
 }
