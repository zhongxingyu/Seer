 package org.nolat.rsircbot.data;
 
 import java.util.HashMap;
 
 import org.nolat.rsircbot.data.json.ItemData;
 import org.nolat.rsircbot.tools.URLReader;
 
 import com.google.gson.Gson;
 
 public class ItemSearch {
     private static final String baseUrl = "http://forums.zybez.net/pages/2007-price-guide-api?term=";
 
     private String item;
 
     ItemData[] results;
 
     public ItemSearch(String item) throws LookupException {
        this.item = item.replaceAll(" ", "_");
         refreshData();
     }
 
     public void refreshData() throws LookupException {
         String jsonData = null;
         try {
             jsonData = URLReader.readUrl(baseUrl + item);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (jsonData != null) {
             Gson gson = new Gson();
             if (jsonData.contains("\"error\"")) {
                 HashMap<String, String> result = gson.fromJson(jsonData, HashMap.class);
                 throw new LookupException("Server Error: " + result.get("error"));
             } else {
                 results = gson.fromJson(jsonData, ItemData[].class);
             }
         } else {
             throw new LookupException("Failed to retrieve data from server");
         }
     }
 
     /**
      * Gets the ItemData for the returned item if there is only one item. <br>
      * Or if one of the items has an exact name match with the query
      * 
      * @return the ItemData for the matched item, null if there is more than one
      */
     public ItemData getMatchedItem() {
         if (results.length > 1) {
             for (ItemData result : results) {
                 if (result.getName().replaceAll(" ", "_").toLowerCase().equalsIgnoreCase(item)) {
                     return result;
                 }
             }
         } else {
             return results[0];
         }
         return null;
     }
 
     public ItemData[] getResults() {
         return results;
     }
 
     public String getSuggestionString() {
         StringBuffer buf = new StringBuffer("{");
         for (int i = 0; i < results.length; i++) {
             buf.append(results[i].getName() + (i == results.length - 1 ? "" : ", "));
         }
         buf.append("}");
         return buf.toString();
     }
 }
