 /*
  * EuropeanaConnection.java - europeana4j
  * (C) 2011 Digibis S.L.
  */
 
 package eu.hack4europe.europeana4j;
 
 import android.util.Log;
 import com.google.gson.Gson;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.URLEncoder;
 
 
 /**
  * The EuropeanaConnection class is the main access point to the Europeana API,
  * allowing to make querys and obtain the results in different formats.
  *
  * @author Andr�s Viedma Pel�ez
  */
 public class EuropeanaConnection {
 
     private String apiKey;
     private String europeanaUri = "http://api.europeana.eu/api/opensearch.json";
 
     private HttpConnector http = new HttpConnector();
 
 
     public EuropeanaConnection(String apiKey) {
         this.apiKey = apiKey;
     }
 
     public EuropeanaConnection() {
     }
 
     public EuropeanaResults search(EuropeanaQuery search, long maxResults)
             throws IOException {
         int pag = 1;
         EuropeanaResults res = this.searchPage(search, pag);
         while ((res.getItemCount() < maxResults) && (res.getItemCount() < res.getTotalResults())) {
             pag++;
             EuropeanaResults res2 = this.searchPage(search, pag);
             res.acumular(res2);
         }
         return res;
     }
 
 
     public EuropeanaResults searchPage(EuropeanaQuery search, long startPage)
             throws IOException {
         String json = this.searchJsonPage(search, startPage);
 
         // Load results object from JSON
         Gson gson = new Gson();
 
         return gson.fromJson(json, EuropeanaResults.class);
     }
 
     public String searchJsonPage(EuropeanaQuery search, long startPage)
             throws IOException {
         String cadenaBusq = search.getQueryString();
         String url = this.europeanaUri + "?searchTerms=" + URLEncoder.encode(cadenaBusq, "UTF-8");
         url += "&startPage=" + startPage;
         url += "&wskey=" + this.apiKey;
 
         // Execute Europeana API request
         String jSON = this.getJSONResult(url);
 
         // Workaround to Bug in Europeana JSON response: quotes not escaped
         StringBuilder builder = new StringBuilder();
         BufferedReader inJson = new BufferedReader(new StringReader(jSON));
         String jsonLine = inJson.readLine();
         while (jsonLine != null) {
             int iSep = jsonLine.indexOf("\": \"");
             if (iSep < 0) {
                 builder.append(jsonLine);
             } else {
                 boolean end = false;
                 while ((!jsonLine.endsWith("\"") && !jsonLine.endsWith("\","))
                         || jsonLine.endsWith("\": \"")) {
                     String nextLine = inJson.readLine();
                     end = (nextLine == null);
                     if (!end) jsonLine += nextLine;
                 }
 
                 builder.append(jsonLine.substring(0, iSep));
                 builder.append("\": \"");
                 int iLastQuote = jsonLine.lastIndexOf("\"");
                 String fieldValue = jsonLine.substring(iSep + 4, iLastQuote);
                fieldValue = fieldValue.replace("\"", "\\\"");
                 builder.append(fieldValue);
                 builder.append(jsonLine.substring(iLastQuote));
             }
 
             jsonLine = inJson.readLine();
         }
         jSON = builder.toString();
 
         // Namespaces are removed
         jSON = jSON.replace("europeana:", "");
         jSON = jSON.replace("enrichment:", "");
 
         return jSON;
     }
 
 
     private String getJSONResult(String url)
             throws IOException {
         Log.i("europeana", "Call to Europeana API: " + url);
 
         return http.getURLContent(url);
     }
 
 
     public String getEuropeanaUri() {
         return europeanaUri;
     }
 
     public void setEuropeanaUri(String europeanaUri) {
         this.europeanaUri = europeanaUri;
     }
 
     private String readStream(InputStream is) {
         try {
             ByteArrayOutputStream bo = new ByteArrayOutputStream();
             int i = is.read();
             while (i != -1) {
                 bo.write(i);
                 i = is.read();
             }
             return bo.toString();
         } catch (IOException e) {
             return "";
         }
     }
 }
