 package info.joaolopes;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  *
  * @author João Lopes <mail at joaolopes.info>
  */
 public class HttpWrapper {
     String url;
     public static enum Action{
         GET, POST, PUT, DELETE
     }
     
     public static enum Type{
         Normal, Json, Ajax, Json_Ajax
     }
     
     public CookieContainer cookies;
     private String Json;
     private Type requestType;
     private Map<String, String> parameters;
     private Action requestAction;
     private String host;
     private String referer;
     
     public HttpWrapper(){
         cookies = new CookieContainer();
         requestType = Type.Normal;
         parameters = new HashMap<>();
         requestAction = Action.GET;
         Json = "{}";
         host = "";
         referer = "";
     }
     
     public void setReferer(String referer){
         this.referer = referer;
     }
     
     public String getReferer(){
         return referer;
     }
     
     public void setHost(String host){
         this.host = host;
     }
     
     public String getHost(){
         return host;
     }
     
     public void addParameter(String name, String value){
         parameters.put(name, value);
     }
     
     public void removeParameter(String name){
         parameters.remove(name);
     }
     
     public void clearParameters(){
         parameters.clear();
     }
     
     public void setType(Type requestType){
         this.requestType = requestType;
     }
     
     public Type getType(){
         return requestType;
     }
     
     public void setJson(String Json){
         this.Json = Json;
     }
     
     public String getJson(){
         return Json;
     }
     
     public void setAction(Action requestAction){
         this.requestAction = requestAction;
     }
     
     public Action getAction(){
         return requestAction;
     }
     
     public String request() throws UnsupportedEncodingException, MalformedURLException, IOException{
         if(requestType == Type.Json || requestType == Type.Json_Ajax){
             String _json = Json;
             Json = "{}";
             
             return HttpWrapper.request(url, _json, requestType, requestAction, host, referer, cookies);
         }
         String params = getParametersString();
         clearParameters();
         
         return HttpWrapper.request(url, params, requestType, requestAction, host, referer, cookies);
     }
     
     private String getParametersString() throws UnsupportedEncodingException{
         String params = "";
         Iterator it = parameters.entrySet().iterator();
         while(it.hasNext()){
             Map.Entry entry = (Map.Entry) it.next();
             params = params.concat(URLEncoder.encode((String)entry.getKey(), "UTF-8") + "=" + URLEncoder.encode((String)entry.getValue(), "UTF-8"));
             it.remove();
             if(it.hasNext()) params = params.concat("&");
         }
         return params;
     }
     
     public void clearCookies(){
         cookies.clear();
     }
     
     public void setUrl(String url) throws MalformedURLException{
         this.url = url;
     }
     
     public void setUrl(URL url){
         this.url = url.toString();
     }
     
     public String getUrl(){
         return this.url;
     }
     
     
     public static String request(String url) throws MalformedURLException, IOException{
         return request(url, "");
     }
     public static String request(String url,  String data) throws MalformedURLException, IOException{
         return request(url, data, Type.Normal);
     }
     public static String request(String url,  String data, Type type) throws MalformedURLException, IOException{
         return request(url, data, type, Action.GET);
     }
     public static String request(String url,  String data, Type type, Action action) throws MalformedURLException, IOException{
         return request(url, data, type, action, "");
     }
     public static String request(String url,  String data, Type type, Action action, String host) throws MalformedURLException, IOException{
         return request(url, data, type, action, host, "");
     }
     public static String request(String url,  String data, Type type, Action action, String host, String referer) throws MalformedURLException, IOException{
         return request(url, data, type, action, host, referer, null);
     }
     public static String request(String url, String data, Type type, Action action, String host, String referer, CookieContainer cookieContainer) throws MalformedURLException, IOException{
         String resultLines = "";
         URL _url;
         URLConnection urlConnection;
         OutputStreamWriter outStream = null;
         BufferedReader inStream;
         String body = data;
         
         // Create connection
         if (action == Action.GET || action == Action.DELETE) {
             if(body.length() > 0){
                 url += "?" + body;
             }
         }
         _url = new URL(url);
         urlConnection = _url.openConnection();
         if(action == Action.GET){
             ((HttpURLConnection)urlConnection).setRequestMethod("GET");
             urlConnection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
         }
         if(action == Action.POST){
             ((HttpURLConnection)urlConnection).setRequestMethod("POST");
             urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
         }
         if(action == Action.PUT){
             ((HttpURLConnection)urlConnection).setRequestMethod("PUT");
             urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
         }
         if(action == Action.DELETE){
             ((HttpURLConnection)urlConnection).setRequestMethod("DELETE");
             urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
         }
         System.setProperty("http.agent", "");
         urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
         if(!host.isEmpty()){
             urlConnection.setRequestProperty("Host", host);
         }
         
         if(!referer.isEmpty()){
             urlConnection.setRequestProperty("Referer", referer);
         }
         
         urlConnection.setDoInput(true);
         urlConnection.setUseCaches(false);
         
         if(cookieContainer != null){
             urlConnection.setRequestProperty("Cookie", cookieContainer.getHeaderCookies(_url.getHost()));
         }
         
         if(type == Type.Json || type == Type.Json_Ajax){
             urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
         }
         if (type == Type.Ajax || type == Type.Json_Ajax) {
             urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
             urlConnection.setRequestProperty("X-Prototype-Version", "1.7");
         }
         
         if(action == Action.POST || action == Action.PUT){
             urlConnection.setDoOutput(true);
             urlConnection.setRequestProperty("Content-Length", ""+ body.length());
             outStream = new OutputStreamWriter(urlConnection.getOutputStream());
             outStream.write(body);
             outStream.flush();
         }
         
         
         inStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
         String buffer;
         while((buffer = inStream.readLine()) != null) {
             resultLines = resultLines.concat(buffer + "\n");
         }
        resultLines = resultLines.substring(0, resultLines.length() - 1);
        
         inStream.close();
         if(outStream != null){
             outStream.close();
         }
         
         if(cookieContainer != null){
             String headerName;
             for (int i=1; (headerName = urlConnection.getHeaderFieldKey(i))!=null; i++) {
                 if (headerName.equals("Set-Cookie")) {                  
                     String cookie = urlConnection.getHeaderField(i);
                     if(!cookie.contains("domain=")){
                         cookie = cookie.concat("; domain=" + _url.getHost());
                     }
                     cookieContainer.addCookie(cookie);
                 }
             }
         }
         
         return resultLines;
     }
 }
