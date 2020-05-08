 package com.qriosity.service;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.qriosity.mvc.model.SharedJsonItem;
 import com.qriosity.mvc.model.WedgieResponse;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  */
 @Service
 public class WedgiesService {
 
     private String url = "http://api.zappos.com/Product/styleId/[%s]?includes=[\"styles\"]&key=%s";
     private static final String zapposApiKey = "52ddafbe3ee659bad97fcce7c53592916a6bfd73";
 
 
     private static final String WEDGIE_QUESTION_TXT = " FASHION HACK WEEKEND TEST - Which awesome thing should I get?";
     private static final String WEDGIE_USER_ID = "5251a86de9a97802000004a2";
 
     @Autowired
     private CrazyCache crazyCache;
 
     public static final String WEDGIE_URL = "http://wedgies-api-production.herokuapp.com/question";
     // curl -X POST -d @question.json -H "Content-Type:application/json" "http://wedgies-api-production.herokuapp.com/question"
 
     public WedgieResponse createWedgie(List<String> styleIds) {
         WedgieResponse wedgieResponse = new WedgieResponse();
         String wedgieId = null;
 
         final List<SharedJsonItem> itemList = getProductDetails(styleIds);
         final JsonObject json = createJson(extractWedgieChoices(itemList));
         if (json != null) {
             System.out.println(json.toString());
             final String response = postToWedgie(json.toString());
             System.out.println("Wedgie response>> " + response);
 
             JsonParser parser = new JsonParser();
             JsonObject jsonObject = (JsonObject)parser.parse(response);
             wedgieId = jsonObject.get("_id") != null ? jsonObject.get("_id").getAsString() : null;
             wedgieResponse.setId(wedgieId);
             wedgieResponse.setText(jsonObject.get("text").getAsString());
         }
 
         return wedgieResponse;
     }
 
     private List<String> extractWedgieChoices(List<SharedJsonItem> jsonItems) {
         List<String> choices = new ArrayList<String>();
         for (int i = 0; i < jsonItems.size(); i++) {
             SharedJsonItem jsonItem = jsonItems.get(i);
             choices.add(jsonItem.getBrandName() + " - " + jsonItem.getProductName());
         }
         return choices;
     }
 
 
     private List<SharedJsonItem> getProductDetails(List<String> styleIds) {
         List<SharedJsonItem> sharedJsonItems = new ArrayList<SharedJsonItem>();
         for (int i = 0; i < styleIds.size(); i++) {
             String id = styleIds.get(i);
             final SharedJsonItem sharedJsonItem = crazyCache != null ? crazyCache.get(id) : null;
             if (sharedJsonItem != null) {
                 sharedJsonItems.add(sharedJsonItem);
             }
             else {
                 // cache miss; create a fake one
                 final SharedJsonItem item = new SharedJsonItem(id);
                 item.setProductName(id);
                 item.setBrandName(id);
 
                 sharedJsonItems.add(item);
             }
         }
         return sharedJsonItems;
     }
 
     private String postToWedgie(String json) {
         HttpClient httpclient = HttpClientBuilder.create().build();
         HttpPost httpPost = new HttpPost(WEDGIE_URL);
         httpPost.setHeader("Content-Type", "application/json");
 
 
         HttpResponse response = null;
 
         BufferedReader br = null;
         String vendorResponse = null;
         try {
             httpPost.setEntity(new StringEntity(json, "UTF-8"));
             response = httpclient.execute(httpPost);
             br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             vendorResponse = this.readerToString(br);
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         finally {
             if (br != null) {
                 try {
                     br.close();
                 }
                 catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         return vendorResponse;
     }
 
     private String readerToString(BufferedReader br) {
         final StringBuilder sb = new StringBuilder();
         String line;
         try {
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         return sb.toString();
     }
 
     private JsonObject createJson(List<String> choices) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("text", WEDGIE_QUESTION_TXT);
         jsonObject.addProperty("owner", WEDGIE_USER_ID);
 
         JsonArray jsonArray = new JsonArray();
         for (int i=0; i < choices.size(); i++) {
             JsonObject jsonChoice = new JsonObject();
            jsonChoice.addProperty("text", choices.get(i));
             jsonArray.add(jsonChoice);
         }
         jsonObject.add("choices", jsonArray);
         return jsonObject;
     }
 
     public static void main(String[] args) {
         WedgiesService service = new WedgiesService();
 
         service.createWedgie(Arrays.asList("Hello", "World"));
     }
 
 }
