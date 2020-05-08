 /**
  * 
  */
 package be.boeboe.scapsync.rest.stats;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import be.boeboe.scapsync.rest.interfaces.IScapSyncStats;
 
 /**
  * Rest Implementation of a ScapSync Statistics
  * 
  * Example json:
  * { "chart" : { 
  *      "data" : {
  *         "rows" : [ { "c" : [ { "v" : "Configurations" }, { "v" : 11241 } ]
  *                 }, { "c" : [ { "v" : "Products" }, { "v" : 64265 } ]
  *                 }, { "c" : [ { "v" : "Vulnerabilities" }, { "v" : 54333 } ]
  *                 }, { "c" : [ { "v" : "Weaknesses" }, { "v" : 909 } ]
  *                 } ],
  *         "cols" : [ { "type" : "string", 
  *                      "id" : "type", 
  *                      "label" : "Content Type"
  *                 }, {
  *                      "type" : "number",
  *                      "id" : "records",
  *                      "label" : "Number Of Records"
  *                 } ]
  *               }
  *              ...
  *             }
  * }
  * @author boeboe
  */
 public class ScapSyncStatsRest implements IScapSyncStats {
   private static final String CHART = "chart";
   private static final String DATA = "data";
   private static final String ROWS = "rows";
   private static final String COUNT = "c";
   private static final String VALUE = "v";
   private static final String CONFIGURATIONS = "Configurations";
   private static final String PRODUCTS = "Products";
   private static final String VULNERABILITIES = "Vulnerabilities";
   private static final String WEAKNESSES = "Weaknesses";
 
   private int fConfigurations = 0;
   private int fProducts = 0;
   private int fVulnerabilities = 0;
   private int fWeaknesses = 0;
   Map<String,Integer> fStatistics = new HashMap<String,Integer>();
   
 
   public ScapSyncStatsRest(JSONObject scapSyncStatsRest) {
     super();
     try {
       JSONArray rows = scapSyncStatsRest.getJSONObject(CHART).getJSONObject(DATA).getJSONArray(ROWS);
 
       for (int i = 0 ; i < rows.length(); i++) {
         JSONArray row = rows.getJSONObject(i).getJSONArray(COUNT);
         String scope = row.getJSONObject(0).getString(VALUE);
         
         if (scope.equals(CONFIGURATIONS)) {
           fConfigurations = row.getJSONObject(1).getInt(VALUE);
         } else if (scope.equals(PRODUCTS)) {
           fProducts = row.getJSONObject(1).getInt(VALUE);
         } else if (scope.equals(VULNERABILITIES)) {
           fVulnerabilities = row.getJSONObject(1).getInt(VALUE);
         } else if (scope.equals(WEAKNESSES)) {
           fWeaknesses = row.getJSONObject(1).getInt(VALUE);
         } 
       }
      
      fConfigurations = scapSyncStatsRest.getInt(CONFIGURATIONS);
      fProducts = scapSyncStatsRest.getInt(PRODUCTS);
      fVulnerabilities = scapSyncStatsRest.getInt(VULNERABILITIES);
      fWeaknesses = scapSyncStatsRest.getInt(WEAKNESSES);
      
       fStatistics.put(CONFIGURATIONS, fConfigurations);
       fStatistics.put(PRODUCTS, fProducts);
       fStatistics.put(VULNERABILITIES, fVulnerabilities);
       fStatistics.put(WEAKNESSES, fWeaknesses);
     } catch (JSONException e) {
       e.printStackTrace();
     }
   }
   
   /**
    * @see be.boeboe.scapsync.rest.interfaces.IScapSyncStats#getAmountConfigurations()
    */
   @Override
   public int getAmountConfigurations() {
     return fConfigurations;
   }
 
   /**
    * @see be.boeboe.scapsync.rest.interfaces.IScapSyncStats#getAmountProducts()
    */
   @Override
   public int getAmountProducts() {
     return fProducts;
   }
 
   /**
    * @see be.boeboe.scapsync.rest.interfaces.IScapSyncStats#getAmountVulnerabilities()
    */
   @Override
   public int getAmountVulnerabilities() {
     return fVulnerabilities;
   }
 
   /**
    * @see be.boeboe.scapsync.rest.interfaces.IScapSyncStats#getAmountWeaknesses()
    */
   @Override
   public int getAmountWeaknesses() {
     return fWeaknesses;
   }
 
   /**
    * @see be.boeboe.scapsync.rest.interfaces.IScapSyncStats#getStatistics()
    */
   @Override
   public Map<String,Integer> getStatistics() {
     return fStatistics;
   }
 }
