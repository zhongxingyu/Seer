 
 package com.lukekorth.android_http;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.integralblue.httpresponsecache.HttpResponseCache;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 public class HttpHelper {
 
     public static final int CACHE = -1;
     public static final int NO_CACHE = 0;
     public static final int VALIDATE_CACHE = 1;
 
     private static final String TAG = "android-http";
     private static final String UAS = "android-http";
     private static boolean DEBUG_HTTP;
 
     private Context mContext;
     private static Gson gson = null;
 
     private SharedPreferences mPrefs;
 
     private int connectTimeout = 10 * 1000; // 10 seconds in milliseconds
     private int readTimeout = 60 * 1000; // 60 seconds in milliseconds
 
     private String cookies = null;
     private List<NameValuePair> additionalHeaderFields;
 
     public HttpHelper(Context context) {
         Init(context, (long) 10); // 10 MiB
     }
 
     public HttpHelper(Context context, long size) {
         Init(context, size);
     }
 
     private void Init(Context context, long size) {
         mContext = context;
 
         DEBUG_HTTP = IsDebug();
 
         try {
             HttpResponseCache.install(new File(context.getCacheDir(),
                     "http"), size * 1024 * 1024);
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG,
                         "IOException occured while getting cache dir " + e);
         }
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "Android-Http Initialized");
             Log.d(TAG, "Requests: "
                     + HttpResponseCache.getInstalled().getRequestCount() +
                     " Network Requests: "
                     + HttpResponseCache.getInstalled().getNetworkCount() +
                     " Cache served requests: "
                     + HttpResponseCache.getInstalled().getHitCount());
         }
 
         mPrefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
 
         additionalHeaderFields = new ArrayList<NameValuePair>();
     }
 
     public void setCookies(List<NameValuePair> nameValuePairs) {
         for (NameValuePair pair : nameValuePairs) {
             try {
                 cookies += pair.getName() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8")
                         + "; ";
             } catch (UnsupportedEncodingException e) {
                 if (DEBUG_HTTP)
                     Log.w(TAG, "UnsupportedEncodingException while url encoding cookie " + e);
             }
         }
 
         cookies = cookies.substring(0, cookies.length() - 2);
     }
 
     public void clearHeaders() {
         additionalHeaderFields.clear();
     }
 
     public void setHeaderField(String name, String value) {
         additionalHeaderFields.add(new BasicNameValuePair(name, value));
     }
 
     public void flush() {
         if (DEBUG_HTTP)
             Log.d(TAG, "Flushing cache to disk...");
 
         HttpResponseCache cache = HttpResponseCache.getInstalled();
         if (cache != null) {
             cache.flush();
             if (DEBUG_HTTP)
                 Log.d(TAG, "Cache was flushed to disk successfully");
         } else {
             if (DEBUG_HTTP)
                Log.d(TAG, "Cache was null and was nto flushed to disk");
         }
     }
 
     public void delete() {
         if (DEBUG_HTTP)
             Log.d(TAG, "Deleting cache...");
 
         HttpResponseCache cache = HttpResponseCache.getInstalled();
         try {
             cache.delete();
 
             if (DEBUG_HTTP)
                 Log.d(TAG, "Cache deleted successfully");
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "IOException while deleting cache. " + e);
         }
 
         mPrefs.edit().clear().commit();
     }
 
     public void setConnectTimeout(int seconds) {
         connectTimeout = seconds * 1000;
     }
 
     public void setReadTimeout(int seconds) {
         readTimeout = seconds * 1000;
     }
 
     public String get(String url) {
         return get(url, CACHE);
     }
 
     public String get(String url, int cache) {
         HttpURLConnection urlConnection = null;
         String response = null;
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "url: " + url);
         }
 
         try {
             urlConnection = (HttpURLConnection) new URL(url).openConnection();
 
             urlConnection.setConnectTimeout(connectTimeout);
             urlConnection.setReadTimeout(readTimeout);
             urlConnection.setRequestProperty("User-Agent", UAS);
 
             if (cookies != null)
                 urlConnection.setRequestProperty("Cookie", cookies);
 
             if (additionalHeaderFields.size() > 0) {
                 for (NameValuePair pair : additionalHeaderFields) {
                     try {
                         urlConnection.setRequestProperty(pair.getName(),
                                 URLEncoder.encode(pair.getValue(), "UTF-8"));
                     } catch (UnsupportedEncodingException e) {
                         if (DEBUG_HTTP)
                             Log.w(TAG,
                                     "UnsupportedEncodingException while url encoding additional header field "
                                             + e);
                     }
                 }
             }
 
             if (cache == NO_CACHE) {
                 urlConnection.addRequestProperty("Cache-Control", "no-cache");
             } else if (cache == VALIDATE_CACHE) {
                 String etag = mPrefs.getString(url, null);
 
                 if (etag != null)
                     urlConnection.addRequestProperty("If-None-Match", etag);
             }
 
             int responseCode = urlConnection.getResponseCode();
             if (responseCode == 304) {
                 if (DEBUG_HTTP)
                     Log.d(TAG,
                             "Server responded with 304 not modified, attempting to load from cache");
 
                 response = getCached(url);
 
                 if (response == null) {
                     if (DEBUG_HTTP)
                         Log.d(TAG, "Response was not in the cache, fetching from server");
 
                     response = get(url);
                 }
             } else
                 response = readStream(urlConnection.getInputStream());
 
             if (response == null)
                 response = getCached(url);
 
             mPrefs.edit().putString(url, urlConnection.getHeaderField("ETag")).commit();
 
             if (DEBUG_HTTP) {
                 Log.d(TAG, "response code: " + responseCode);
                 Log.d(TAG, "response payload: " + response);
             }
         } catch (MalformedURLException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "MalformedURLException occured while parsing url " + e);
         } catch (IOException e) {
             if (DEBUG_HTTP) {
                 response = readStream(urlConnection.getErrorStream());
                 Log.d(TAG, "error response: " + response);
 
                 Log.w(TAG,
                         "IOException occured while trying to open connection or getting input stream. "
                                 + e);
             }
         } finally {
             urlConnection.disconnect();
         }
 
         return response;
     }
 
     public String get(String url, List<NameValuePair> nameValuePairs) {
         return get(url, nameValuePairs, CACHE);
     }
 
     public String get(String url, List<NameValuePair> nameValuePairs, int cache) {
         url = url + "?" + encodeParameters(nameValuePairs);
 
         if (DEBUG_HTTP)
             Log.d(TAG, "query string: " + nameValuePairs.toString());
 
         return get(url, cache);
     }
 
     @SuppressWarnings("rawtypes")
     public <T> T get(String url, Class type) {
         return get(url, type, CACHE);
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T get(String url, Class type, int cache) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(get(url, cache), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     @SuppressWarnings("rawtypes")
     public <T> T get(String url, List<NameValuePair> nameValuePairs, Class type) {
         return get(url, nameValuePairs, type, CACHE);
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T get(String url, List<NameValuePair> nameValuePairs, Class type, int cache) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(get(url, nameValuePairs, cache), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     public String getCached(String url) {
         HttpURLConnection urlConnection = null;
         String response = null;
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "url: " + url);
         }
 
         try {
             urlConnection = (HttpURLConnection) new URL(url).openConnection();
             urlConnection.addRequestProperty("Cache-Control", "only-if-cached");
 
             InputStream in = new BufferedInputStream(urlConnection.getInputStream());
             response = readStream(in);
 
             if (DEBUG_HTTP)
                 Log.d(TAG, "cache payload: " + response);
         } catch (FileNotFoundException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "The requested resource was not cached");
 
             response = null;
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG,
                         "IOException occured while trying to open connection or getting input stream. "
                                 + e);
         } finally {
             urlConnection.disconnect();
         }
 
         return response;
     }
 
     public String getCached(String url, List<NameValuePair> nameValuePairs) {
         if (nameValuePairs != null && nameValuePairs.size() > 0)
             url = url + "?" + encodeParameters(nameValuePairs);
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "Attempting to load directly from cache, return null if not cached");
             Log.d(TAG, "query string: " + nameValuePairs.toString());
         }
 
         return getCached(url);
     }
 
     @SuppressWarnings({
             "rawtypes", "unchecked"
     })
     public <T> T getCached(String url, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(getCached(url, new ArrayList<NameValuePair>()), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     @SuppressWarnings({
             "rawtypes", "unchecked"
     })
     public <T> T getCached(String url, List<NameValuePair> nameValuePairs, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(getCached(url, nameValuePairs), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     public Bitmap getBitmap(String url) {
         return getBitmap(url, CACHE);
     }
 
     public Bitmap getBitmap(String url, int cache) {
         HttpURLConnection urlConnection = null;
         Bitmap response = null;
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "url: " + url);
         }
 
         try {
             urlConnection = (HttpURLConnection) new URL(url).openConnection();
 
             urlConnection.setConnectTimeout(connectTimeout);
             urlConnection.setReadTimeout(readTimeout);
             urlConnection.setRequestProperty("User-Agent", UAS);
             urlConnection.setDoInput(true);
 
             if (cookies != null)
                 urlConnection.setRequestProperty("Cookie", cookies);
 
             if (additionalHeaderFields.size() > 0) {
                 for (NameValuePair pair : additionalHeaderFields) {
                     try {
                         urlConnection.setRequestProperty(pair.getName(),
                                 URLEncoder.encode(pair.getValue(), "UTF-8"));
                     } catch (UnsupportedEncodingException e) {
                         if (DEBUG_HTTP)
                             Log.w(TAG,
                                     "UnsupportedEncodingException while url encoding additional header field "
                                             + e);
                     }
                 }
             }
 
             if (cache == NO_CACHE) {
                 urlConnection.addRequestProperty("Cache-Control", "no-cache");
             } else if (cache == VALIDATE_CACHE) {
                 urlConnection.addRequestProperty("Cache-Control", "max-age=0");
             }
 
             response = BitmapFactory.decodeStream(urlConnection.getInputStream());
 
             if (DEBUG_HTTP) {
                 Log.d(TAG, "response code: " + urlConnection.getResponseCode());
             }
         } catch (MalformedURLException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "MalformedURLException occured while parsing url " + e);
         } catch (IOException e) {
             if (DEBUG_HTTP) {
                 Log.w(TAG,
                         "IOException occured while trying to open connection or getting input stream. "
                                 + e);
             }
         } finally {
             urlConnection.disconnect();
         }
 
         return response;
     }
 
     public String post(String url) {
         return post(url, "");
     }
 
     public String post(String url, String params) {
         HttpURLConnection urlConnection = null;
         String response = null;
         String urlParameters = params;
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "url: " + url);
             Log.d(TAG, "query string: " + params);
         }
 
         try {
             urlConnection = (HttpURLConnection) new URL(url).openConnection();
 
             urlConnection.setDoOutput(true);
             urlConnection.setConnectTimeout(connectTimeout);
             urlConnection.setReadTimeout(readTimeout);
             urlConnection.setRequestProperty("User-Agent", UAS);
 
             if (cookies != null)
                 urlConnection.setRequestProperty("Cookie", cookies);
 
             if (additionalHeaderFields.size() > 0) {
                 for (NameValuePair pair : additionalHeaderFields) {
                     try {
                         urlConnection.setRequestProperty(pair.getName(),
                                 URLEncoder.encode(pair.getValue(), "UTF-8"));
                     } catch (UnsupportedEncodingException e) {
                         if (DEBUG_HTTP)
                             Log.w(TAG,
                                     "UnsupportedEncodingException while url encoding additional header field "
                                             + e);
                     }
                 }
             }
 
             urlConnection.addRequestProperty("Cache-Control", "no-cache");
 
             urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             urlConnection.setRequestProperty("Content-Length",
                     Integer.toString(urlParameters.getBytes().length));
 
             OutputStream output = null;
             try {
                 output = urlConnection.getOutputStream();
                 output.write(urlParameters.getBytes("UTF-8"));
             } catch (IOException e) {
                 Log.w(TAG, "IOException occured while trying to get output stream. " + e);
             } finally {
                 if (output != null) {
                     try {
                         output.close();
                     } catch (IOException e) {
                         Log.w(TAG, "IOException occured while trying to close output stream. " + e);
                     }
                 }
             }
 
             response = readStream(urlConnection.getInputStream());
 
             if (DEBUG_HTTP) {
                 Log.d(TAG, "response code: " + urlConnection.getResponseCode());
                 Log.d(TAG, "headers: " + urlConnection.getHeaderFields().toString());
                 Log.d(TAG, "response payload: " + response);
             }
         } catch (MalformedURLException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "MalformedURLException occured while parsing url " + e);
         } catch (IOException e) {
             if (DEBUG_HTTP) {
                 response = readStream(urlConnection.getErrorStream());
                 Log.d(TAG, "error response: " + response);
 
                 Log.w(TAG,
                         "IOException occured while trying to open connection or getting input stream. "
                                 + e);
             }
         } finally {
             urlConnection.disconnect();
         }
 
         return response;
     }
 
     public String post(String url, List<NameValuePair> nameValuePairs) {
         return post(url, encodeParameters(nameValuePairs));
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T post(String url, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(post(url, new String()), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T post(String url, String params, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(post(url, params), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T post(String url, List<NameValuePair> nameValuePairs, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         try {
             return (T) gson.fromJson(post(url, nameValuePairs), type);
         } catch (JsonSyntaxException e) {
             if (DEBUG_HTTP)
                 Log.d(TAG, "Error while parsing json: " + e);
 
             return null;
         }
     }
 
     public String uploadImage(String url, String imagePath, ProgressCallback callback) {
         // Delimiters for the upload
         // following:
         // http://reecon.wordpress.com/2010/04/25/uploading-files-to-http-server-using-post-android-sdk/
         final String lineEnd = "\r\n";
         final String twoHyphens = "--";
         final String boundary = "*****";
 
         int bytesRead, bytesAvailable, bufferSize;
         byte[] buffer;
         int maxBufferSize = 1 * 1024 * 1024;
 
         HttpURLConnection urlConnection = null;
         String response = null;
 
         DataOutputStream outputStream = null;
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "url: " + url);
         }
 
         try {
             FileInputStream fileInputStream = new FileInputStream(new File(imagePath));
 
             urlConnection = (HttpURLConnection) new URL(url).openConnection();
 
             urlConnection.setDoInput(true);
             urlConnection.setDoOutput(true);
             urlConnection.setRequestProperty("User-Agent", UAS);
             urlConnection.addRequestProperty("Cache-Control", "no-cache");
             urlConnection.setRequestMethod("POST");
             urlConnection.setRequestProperty("Connection", "Keep-Alive");
 
             urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                     + boundary);
             urlConnection.setRequestProperty("Content-Disposition",
                     "attachment; name=\"uploadedfile\";filename=\""
                             + imagePath + "\"" + lineEnd);
 
             if (cookies != null)
                 urlConnection.setRequestProperty("Cookie", cookies);
 
             if (additionalHeaderFields.size() > 0) {
                 for (NameValuePair pair : additionalHeaderFields) {
                     try {
                         urlConnection.setRequestProperty(pair.getName(),
                                 URLEncoder.encode(pair.getValue(), "UTF-8"));
                     } catch (UnsupportedEncodingException e) {
                         if (DEBUG_HTTP)
                             Log.w(TAG,
                                     "UnsupportedEncodingException while url encoding additional header field "
                                             + e);
                     }
                 }
             }
 
             outputStream = new DataOutputStream(urlConnection.getOutputStream());
 
             bytesAvailable = fileInputStream.available();
             int max = bytesAvailable;
             bufferSize = Math.min(bytesAvailable, maxBufferSize);
             buffer = new byte[bufferSize];
 
             // Read file
             bytesRead = fileInputStream.read(buffer, 0, bufferSize);
             int progress = bytesRead;
 
             while (bytesRead > 0) {
                 if (DEBUG_HTTP) {
                     Log.d(TAG, "upload progress: " + (int) (((double) progress / max) * 100) + "/"
                             + 100);
                 }
 
                 if (callback != null)
                     callback.MakeProgress((int) (((double) progress / max) * 100));
 
                 outputStream.write(buffer, 0, bufferSize);
                 bytesAvailable = fileInputStream.available();
                 bufferSize = Math.min(bytesAvailable, maxBufferSize);
                 bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                 progress += bytesRead;
             }
 
             outputStream.writeBytes(lineEnd);
             outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 
             response = readStream(urlConnection.getInputStream());
         } catch (FileNotFoundException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "FileNotFoundException occured while fetching the image " + e);
         } catch (MalformedURLException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "MalformedURLException occured while parsing url " + e);
         } catch (IOException e) {
             if (DEBUG_HTTP) {
                 response = readStream(urlConnection.getErrorStream());
                 Log.d(TAG, "error response: " + response);
 
                 Log.w(TAG,
                         "IOException occured while trying to open connection or getting input stream. "
                                 + e);
             }
         } finally {
             urlConnection.disconnect();
         }
 
         return response;
     }
 
     /* Public helper methods */
     public static Gson getGson() {
         if (gson == null)
             gson = new Gson();
 
         return gson;
     }
 
     public static String encodeParameters(List<NameValuePair> nameValuePairs) {
         String parameters = "";
 
         for (NameValuePair pair : nameValuePairs) {
             try {
                 parameters += pair.getName() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8")
                         + "&";
             } catch (UnsupportedEncodingException e) {
                 if (DEBUG_HTTP)
                     Log.w(TAG, "UnsupportedEncodingException while url encoding parameters " + e);
             }
         }
 
         return parameters.substring(0, parameters.length() - 1);
     }
 
     /* Private helper methods */
     private String readStream(InputStream in) {
         InputStreamReader is = null;
         BufferedReader br = null;
         StringBuilder sb = null;
 
         try {
             is = new InputStreamReader(in);
             br = new BufferedReader(is);
             sb = new StringBuilder();
 
             String read = br.readLine();
 
             while (read != null) {
                 sb.append(read);
                 read = br.readLine();
             }
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "IOException occured while reading response from server " + e);
         } catch (NullPointerException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "A NullPointerException occured while reading response from server " + e);
         }
 
         try {
             in.close();
             is.close();
             br.close();
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "IOException occured while closing streams " + e);
         } catch (NullPointerException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "A NullPointerException occured while closing streams " + e);
         }
 
         if (sb != null)
             return sb.toString();
         else
             return null;
     }
 
     // http://izvornikod.com/Blog/tabid/82/EntryId/13/How-to-check-if-your-android-application-is-running-in-debug-or-release-mode.aspx
     private boolean IsDebug() {
         boolean debuggable = false;
 
         PackageManager pm = mContext.getPackageManager();
         try {
             ApplicationInfo appinfo = pm.getApplicationInfo(mContext.getPackageName(), 0);
             debuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
         } catch (NameNotFoundException e) {
             /* debuggable variable will remain false */
             Log.w(TAG, "NameNotFoundException, DEBUG_HTTP will remain false  " + e);
         }
 
         return debuggable;
     }
 
 }
