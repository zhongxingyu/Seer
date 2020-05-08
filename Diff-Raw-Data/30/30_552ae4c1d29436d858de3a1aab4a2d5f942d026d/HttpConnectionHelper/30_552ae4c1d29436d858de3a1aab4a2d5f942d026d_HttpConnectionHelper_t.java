 package com.storassa.android.scuolasci;
 
 import java.io.BufferedInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.http.client.HttpClient;
 
 public class HttpConnectionHelper {
    static HttpConnectionHelper helper;
    HttpURLConnection connection;
    HttpClient httpClient;
    CookieManager cookieManager;
    String[] result  = new String[3];
    boolean infoAvailable;
    Feature[] features;
    String cookie;
    ExecutorService exec;
 
    private HttpConnectionHelper() {
       cookieManager = new CookieManager();
       CookieHandler.setDefault(cookieManager);
    }
 
    public static HttpConnectionHelper getHelper() {
       if (helper == null)
          helper = new HttpConnectionHelper();
 
       return helper;
    }
 
    public void openConnection(final HttpResultCallable callable,
          String username, String password) {
 
       
       features = new Feature[MAX_FEATURES];
 
       String urlParameters = "task=signin&accion=signin&textBoxUsername=";
       urlParameters += username;
       urlParameters += "&textBoxPassword=";
       urlParameters += password + "&buttonSubmit=";
       final String params = urlParameters;
       final URL url;
       try {
          url = new URL(
                "http://www.scuolascilimone.com/it/area-riservata/access/signin");
       } catch (MalformedURLException e1) {
          e1.printStackTrace();
          throw new RuntimeException(e1);
       }
 
       exec = Executors.newCachedThreadPool();
       exec.execute(new Runnable() {
 
          @Override
          public void run() {
             try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                      "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", ""
                      + Integer.toString(params.getBytes().length));
                connection.setUseCaches(false);
 
                DataOutputStream wr = new DataOutputStream(connection
                      .getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();
                connection.connect();
                BufferedInputStream in = new BufferedInputStream(connection
                      .getInputStream());
                int responseCode = connection.getResponseCode();
 
                // if the credentials are not correct, return code = 200
                if (responseCode == 200) {
                   result[0] = "Incorrect";
                   result[1] = null;
                   callable.resultAvailable(Request.LOGIN, result, null);
                } else {
 
                   // if the credentials are correct get the redirections
                   while (responseCode == 303) {
 
                      String urlString = connection.getHeaderField("Location");
                      URL redirectedUrl = new URL(urlString);
                      connection = (HttpURLConnection) redirectedUrl
                            .openConnection();
                      connection.setDoOutput(true);
                      connection.setDoInput(true);
                      connection.setInstanceFollowRedirects(false);
                      connection.setRequestMethod("GET");
                      connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                      connection.setRequestProperty("charset", "utf-8");
                      connection.setUseCaches(false);
 
                      connection.connect();
                      responseCode = connection.getResponseCode();
                   }
 
                   final int finalResponseCode = responseCode;
 
                   in = new BufferedInputStream(connection.getInputStream());
 
                   java.util.Scanner s = new java.util.Scanner(in)
                         .useDelimiter("\\A");
 
                   StringBuilder builder = new StringBuilder();
                   while (s.hasNext())
                      builder.append(s.next());
 
                   result[0] = Integer.toString(finalResponseCode);
                   result[1] = builder.toString();
 
                   int count = 1;
                   String[] featureString = getAvailBtn(result[1]);
                   result[2] = featureString[0];
                   
                   for (String f : featureString) {
                      if (f != null) {
                         if (f.equals("Racing Team"))
                            features[count++] = Feature.RACING_TEAM;
                         else if (f.equals("Scuderia"))
                            features[count++] = Feature.SCUDERIA;
                         else if (f.equals("Maestri"))
                            features[count++] = Feature.INSTRUCTOR;
                        else if (f.equals("Saranno Campioni"))
                           features[count++] = Feature.CAMPIONI;
                      }
                   }
                   
                   callable.resultAvailable(Request.LOGIN, result, features);
                   infoAvailable = true;
                }
 
             } catch (IOException e) {
                callable.resultAvailable(Request.LOGIN, null, features);
                ;
             } finally {
                // connection.disconnect();
             }
 
          }
       });
    }
 
    public void openGenericConnection(final Request request,
          final HttpResultCallable callable, final URL localUrl) {
 
       exec = Executors.newCachedThreadPool();
       exec.execute(new Runnable() {
 
          @Override
          public void run() {
             try {
                HttpURLConnection genericConnection = (HttpURLConnection) localUrl
                      .openConnection();
 
                // Create a response handler
                genericConnection.setDoOutput(true);
                genericConnection.setDoInput(true);
                genericConnection.setRequestMethod("GET");
                genericConnection.setRequestProperty("Content-Type",
                      "application/x-www-form-urlencoded");
                genericConnection.setRequestProperty("charset", "utf-8");
                genericConnection.setUseCaches(false);
                genericConnection.connect();
 
                int finalResponseCode = genericConnection.getResponseCode();
 
                BufferedInputStream in = new BufferedInputStream(
                      genericConnection.getInputStream());
                in = new BufferedInputStream(genericConnection.getInputStream());
 
                java.util.Scanner s = new java.util.Scanner(in)
                      .useDelimiter("\\A");
 
                StringBuilder builder = new StringBuilder();
                while (s.hasNext())
                   builder.append(s.next());
 
                result[0] = Integer.toString(finalResponseCode);
                result[1] = builder.toString();
 
                callable.resultAvailable(request, result, null);
 
             } catch (IOException e) {
                callable.resultAvailable(Request.LOGIN, null, features);
                ;
             } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources;
             }
          }
       });
    }
 
    public HttpClient getGenericClient() {
       return httpClient;
    }
 
    public Feature[] getFeature() {
       return features;
    }
 
    public boolean infoAvailable() {
       return infoAvailable;
    }
 
    private String[] getAvailBtn(String response) {
       String temp = response;
       
       String[] result = new String[MAX_FEATURES];
       int index = 1, start, end;
       boolean endOfString = false;
 
       while (!endOfString) {
          start = temp.indexOf("txt18");
          temp = temp.substring(start + 1);
 
          end = temp.indexOf("<");
          result[index++] = temp.substring(6, end);
 
          if (temp.indexOf("p>") < temp.indexOf("txt18")
                || (temp.indexOf("txt18") == -1)) {
             endOfString = true;
          }
       }
       
       start = temp.indexOf("Nome:");
       temp = temp.substring(start + 1);
       start = temp.indexOf("strong>");
       temp = temp.substring(start);
       end = temp.indexOf("<");
       result[0] = temp.substring(7, end);
 
       return result;
    }
 
    private final static int MAX_FEATURES = 10;
 }
