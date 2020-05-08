 package net.m_kawato.tabletpos;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.SuppressLint;
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Globals extends Application {
     private static final String TAG = "Globals";
     private static final String SAVED_VALUES = "saved_values";
     private static final String KEY_LOADING_SHEET_NUMBER = "LoadingSheetNumber";
     private static final String KEY_TRANSACTION_ID = "TransactionId";  
     public static final String SDCARD_DIRNAME = "TabletPOSApp";
     public static final String IMAGE_DIRNAME = "images";
     public static final String PRODUCT_IMAGE_PREFIX = "product";
     public static final String PRODUCTS_FILENAME = "products.csv";
     public static final String PLACES_FILENAME = "places.csv";
     public static final String RECEIPT_PREFIX = "receipt";
    public static final String RECEIPT_SUFFIX = "xls";
     public boolean initialized = false; // true when Order is first called
     
     public String loadingSheetNumber;
     public long transactionId;
     public List<Product> products = new ArrayList<Product>();
     @SuppressLint("UseSparseArrays")
     private Map<Integer, Product> productMap = new HashMap<Integer, Product>(); // map[productId -> product]
     public List<String> categories; // list of category names
     public List<String> routes; // list of routeCode
     public Map<String, List<String>> places; // Map[routeCode -> list of placeCode]
     public Map<String, String> routeName; // Map[routeCode -> routeName]
     public Map<String, String> placeName; // Map[placeCode -> placeName]
 
     public Transaction transaction;
     public int selectedRoute;
     public int selectedPlace;
 
     public void initialize() {
         if (this.products != null) {
             for(Product p: this.products) {
                 p.orderItem = null;
             }
         }
         this.selectedRoute = 0;
         this.selectedPlace = 0;
     }
     
     public void addCategory(String category) {
         if (! this.categories.contains(category)) {
             this.categories.add(category);
         }
     }
 
     public void addProduct(Product product) {
         // Log.d(TAG, "addProduct: productId = " + product.productId);
         this.products.add(product);
         this.productMap.put(product.productId, product);
     }
 
     public Product getProduct(int productId) {
         return this.productMap.get(productId);
     }
 
     public void addPlace(String routeCode, String placeCode) {
         if (! this.routes.contains(routeCode)) {
             // Log.d(TAG, String.format("addPlace: add new route: %s", routeCode));
             this.routes.add(routeCode);
             this.places.put(routeCode, new ArrayList<String>());
         }
         //Log.d(TAG, String.format("addPlace: add new place: %s - %s", routeCode, placeCode));
         this.places.get(routeCode).add(placeCode);
     }
 
     public void addRouteName(String routeCode, String routeName) {
         if (this.routeName.containsKey(routeCode)) {
             if (! this.routeName.get(routeCode).equals(routeName)) {
                 Log.w(TAG, String.format("addRouteName: different place name for the same code (%s, %s)",
                         routeCode, routeName));
             }
         } else {
             /*
             Log.d(TAG, String.format("addRouteName: add new route name (%s,  %s)",
                     routeCode, routeName));
             */
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
             /*
             Log.d(TAG, String.format("addPlaceName: add new place name (%s,  %s)",
                     placeCode, placeName));
             */
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
         String value = preferences.getString(KEY_LOADING_SHEET_NUMBER, "0");
         this.loadingSheetNumber = value;
         Log.d(TAG, "loadLoadingSheetNumber: value = " + value);
     }
 
     public void setLoadingSheetNumber(String value) {
         this.loadingSheetNumber = value;
         SharedPreferences preferences = getSharedPreferences(SAVED_VALUES, Context.MODE_PRIVATE);
         Editor editor = preferences.edit();
         editor.putString(KEY_LOADING_SHEET_NUMBER, value);
         editor.commit();
 
         // delete existing records from order table in SQLite DB
         PosDbHelper dbHelper = new PosDbHelper(this);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         String deleteOrder = "DELETE FROM orders";
         db.execSQL(deleteOrder);
 
         Toast.makeText(this, "Loading sheet number has been recorded.", Toast.LENGTH_LONG).show();
     }
 
     public void loadTransactionId() {
         SharedPreferences preferences = getSharedPreferences(SAVED_VALUES, Context.MODE_PRIVATE);
         long value = preferences.getLong(KEY_TRANSACTION_ID, 0);
         this.transactionId = value;
         Log.d(TAG, "loadTransactionId: value = " + value);
     }
 
     public void incrTransactionId() {
         this.transactionId++;
         this.transaction = new Transaction(this, transactionId, null, null, new BigDecimal(0));
         SharedPreferences preferences = getSharedPreferences(SAVED_VALUES, Context.MODE_PRIVATE);
         Editor editor = preferences.edit();
         editor.putLong(KEY_TRANSACTION_ID, this.transactionId);
         editor.commit();
     }
 
     public void printRouteNames() {
         for (String routeCode: this.routes) {
             String routeName = this.routeName.get(routeCode);
             System.out.println(String.format("%s -> %s", routeCode, routeName));
         }
     }
 
     // Save loading/stock states to SQLite database
     public void saveLoading() {
         Log.d(TAG, "saveLoading");
 
         PosDbHelper dbHelper = new PosDbHelper(this);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
 
         db.execSQL("DELETE FROM loading");
         String insertLoading = "INSERT INTO loading (product_id, loaded, stock) VALUES (?, ?, ?)";
         for (Product p: this.products) {
             if (p.loaded) {
                 db.execSQL(insertLoading, new String[] {Integer.toString(p.productId), "1", Integer.toString(p.stock)});
             }
         }
     }
 
 }
