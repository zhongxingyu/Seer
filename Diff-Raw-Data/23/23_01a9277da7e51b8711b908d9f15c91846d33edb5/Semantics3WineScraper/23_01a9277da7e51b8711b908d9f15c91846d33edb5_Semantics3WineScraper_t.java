 package com.selesse.android.winedb.winescraper.impl;
 
import android.os.Build;
import android.util.Base64;
 import android.util.Log;
 import com.google.common.collect.Lists;
 import com.google.common.io.CharStreams;
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.selesse.android.winedb.database.Wine;
 import com.selesse.android.winedb.priv.Semantics3Key;
 import com.selesse.android.winedb.winescraper.WineScraper;
 
import javax.net.ssl.HttpsURLConnection;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.List;
 
 public class Semantics3WineScraper implements WineScraper {
     private static final String TAG = Semantics3WineScraper.class.getSimpleName();
     private String url;
     private List<Exception> errors;
 
     public Semantics3WineScraper(String barcode) {
         errors = Lists.newArrayList();
        // TODO: "prod" mode (1k hits/day) requires OATH; "test" mode (100 hits/day) is simpler for now
        url = "https://api.semantics3.com/test/v1/products?q=";
         try {
             String request = URLEncoder.encode("{\"upc\":\"" + barcode + "\"}", "utf-8");
             url += request;
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public List<Wine> scrape() {
         List<Wine> scrapedWines = Lists.newArrayList();
         try {
             URL url = new URL(getQueryUrl());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
             connection.setRequestProperty("api_key", Semantics3Key.getKey());

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                String errorResponse = CharStreams.toString(new InputStreamReader(connection.getErrorStream()));

                throw new IOException("Error querying: got response code " + responseCode +
                        " from server with message " + errorResponse);
            }
             String jsonResponse = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
             Log.i(TAG, "Semantics3 JSON response was: \n" + jsonResponse);
 
             Gson gson = new Gson();
             Semantics3Response response = gson.fromJson(jsonResponse, Semantics3Response.class);
             if (response.getResultsSize() > 0) {
                 scrapedWines = response.convertResponsesToWineList();
             }
         } catch (JsonSyntaxException e) {
             Log.e(TAG, "JsonSyntaxException thrown: " + e.toString());
             errors.add(e);
         } catch (IOException e) {
             Log.e(TAG, "IOException thrown: " + e.toString());
             errors.add(e);
         }
 
         return scrapedWines;
     }
 
     @Override
     public String getQueryUrl() {
         return url;
     }
 
     @Override
     public List<Exception> getErrors() {
         return errors;
     }
 
     @Override
     public String getSourceName() { return "Semantics3"; }
 }
