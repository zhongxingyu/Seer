 package net.m_kawato.tabletpos;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Globals extends Application {
     private static final String TAG = "Globals";
     private static final String SAVED_VALUES = "saved_values";
     private static final String KEY_LOADING_SHEET_NUMBER = "LoadingSheetNumber";
     public static final String SDCARD_DIRNAME = "TabletPOSApp";
     public static final String PRODUCTS_FILENAME = "products.csv";
     public static final String PLACES_FILENAME = "places.csv";
    public static final String RECEIPT_PREFIX = "receipt";
    public static final String RECEIPT_SUFFIX = "xls";
 
     public String loadingSheetNumber;
     public List<Product> products;
     public List<String> categories;
     public List<String> routes; // list of routeCode
     public Map<String, List<String>> places; // Map[routeCode -> list of placeCode]
     public Map<String, String> routeName; // Map[routeCode -> routeName]
     public Map<String, String> placeName; // Map[placeCode -> placeName]
     
     public List<Product> orderItems;
     public List<Map<String, Object>> orderItemList;
     public int selectedRoute;
     public int selectedPlace;
     public BigDecimal totalAmount;
     public BigDecimal creditAmount;
 
     public void initialize() {
         if (this.products != null) {
             for(Product p: this.products) {
                 p.quantity = 0;
                 p.quantityBox = 0;
             }
         }
         this.orderItems = new ArrayList<Product>();
         this.orderItemList = new ArrayList<Map<String, Object>>();
         this.selectedRoute = 0;
         this.selectedPlace = 0;
         this.totalAmount = new BigDecimal(0);
         this.creditAmount = new BigDecimal(0);
     }
     
     public void addCategory(String category) {
         if (! this.categories.contains(category)) {
             this.categories.add(category);
         }
     }
 
     public void addPlace(String routeCode, String placeCode) {
         if (! this.routes.contains(routeCode)) {
             Log.d(TAG, String.format("addPlace: add new route: %s", routeCode));
             this.routes.add(routeCode);
             this.places.put(routeCode, new ArrayList<String>());
         }
         Log.d(TAG, String.format("addPlace: add new place: %s - %s", routeCode, placeCode));
         this.places.get(routeCode).add(placeCode);
     }
 
     public void addRouteName(String routeCode, String routeName) {
         if (this.routeName.containsKey(routeCode)) {
             if (! this.routeName.get(routeCode).equals(routeName)) {
                 Log.w(TAG, String.format("addRouteName: different place name for the same code (%s, %s)",
                         routeCode, routeName));
             }
         } else {
             Log.d(TAG, String.format("addRouteName: add new route name (%s,  %s)",
                     routeCode, routeName));
             this.routeName.put(routeCode, routeName);
         }
     }
 
     public void addPlaceName(String placeCode, String placeName) {
         if (this.placeName.containsKey(placeCode)) {
             if (! this.placeName.get(placeCode).equals(placeName)) {
                 Log.w(TAG, String.format("addPlaceName: different place name for the same code (%s, %s)",
                     placeCode, placeName));
             }
         } else {
             Log.d(TAG, String.format("addPlaceName: add new place name (%s,  %s)",
                     placeCode, placeName));
             this.placeName.put(placeCode, placeName);
         }
     }
 
     public String getSelectedRouteCode() {
         return this.routes.get(this.selectedRoute);
     }
 
     public String getSelectedPlaceCode() {
         String routeCode = getSelectedRouteCode();
         return this.places.get(routeCode).get(this.selectedPlace);
     }
 
     public void loadLoadingSheetNumber() {
         SharedPreferences preferences = getSharedPreferences(SAVED_VALUES, Context.MODE_PRIVATE);
         String value = preferences.getString(KEY_LOADING_SHEET_NUMBER, null);
         if (value == null) {
             value = "0";
         }
         this.loadingSheetNumber = value;
         Log.d(TAG, "loadLoadingSheetNumber: value = " + value);
     }
 
     public void setLoadingSheetNumber(String value) {
         this.loadingSheetNumber = value;
         SharedPreferences preferences = getSharedPreferences(SAVED_VALUES, Context.MODE_PRIVATE);
         Editor editor = preferences.edit();
         editor.putString(KEY_LOADING_SHEET_NUMBER, value);
         editor.commit();
         Toast.makeText(this, "Loading sheet number has been recorded.", Toast.LENGTH_LONG).show();
     }
     
     public void printRouteNames() {
         for (String routeCode: this.routes) {
             String routeName = this.routeName.get(routeCode);
             System.out.println(String.format("%s -> %s", routeCode, routeName));
         }
     }
 }
