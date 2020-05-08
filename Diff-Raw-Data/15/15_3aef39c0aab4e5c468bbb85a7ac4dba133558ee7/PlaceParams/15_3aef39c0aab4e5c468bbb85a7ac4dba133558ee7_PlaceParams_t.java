 package io.github.axxiss.places.request;
 
 import com.loopj.android.http.RequestParams;
 
 import io.github.axxiss.places.PlacesSettings;
 import io.github.axxiss.places.enums.Params;
 import io.github.axxiss.places.enums.PlaceType;
 import io.github.axxiss.places.enums.RankBy;
 
 /**
  * @author Axxiss
  */
 public class PlaceParams extends RequestParams {
     private static final String TAG = "PlaceParams";
 
     private String mUrl;
 
     public PlaceParams() {
         put(Params.KEY.getValue(), PlacesSettings.getInstance().getApiKey());
         put(Params.SENSOR.getValue(), "true");
     }
 
     protected void put(String key, boolean value) {
         put(key, String.valueOf(value));
     }
 
     protected void put(String key, int value) {
         put(key, String.valueOf(value));
     }
 
     protected String getUrl() {
         return mUrl;
     }
 
     protected void setUrl(final String url) {
         mUrl = url;
     }
 
     public PlaceParams setRankBy(RankBy rankBy) {
         put(Params.RANK_BY.getValue(), rankBy.getvalue());
         return this;
     }
 
     public PlaceParams setRadius(int radius) {
         if (radius > 0) {
             put(Params.RADIUS.getValue(), radius);
         } else {
             setRankBy(RankBy.DISTANCE);
         }
         return this;
     }
 
     public PlaceParams setLocation(final double lat, final double lng) {
         String location = String.format("%s,%s", String.valueOf(lat), String.valueOf(lng));
         put(Params.LOCATION.getValue(), location);
         return this;
     }
 
     public PlaceParams setQuery(String query) {
         if (query != null) {
             put(Params.QUERY.getValue(), query);
         }
         return this;
     }
 
     public PlaceParams setTypes(PlaceType[] places) {
         final String divider = "|";

        StringBuilder types = new StringBuilder();
        
         for (PlaceType place : places) {
            types.append(place.getValue());
            types.append(divider);
         }
        
        put(Params.TYPES.getValue(), types.substring(0, types.length() - 1));
         return this;
     }
 
     public PlaceParams setLanguage(String langCode) {
         put(Params.LANGUAGE.getValue(), langCode);
 
         return this;
     }
 
     public PlaceParams setKeyword(String keyword) {
         put(Params.KEYWORD.getValue(), keyword);
         return this;
     }
 
     public PlaceParams setReference(String reference) {
         put(Params.REFERENCE.getValue(), reference);
         return this;
     }
 
     public PlaceParams setPhotoReference(String photoReference) {
         put(Params.PHOTO_REFERENCE.getValue(), photoReference);
         return this;
     }
 }
