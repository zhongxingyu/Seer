 package com.albaniliu.chuangxindemo.util;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.util.Log;
 
 public class HTTPClient {
 	public static int CONNECT_TIME_OUT = 50000;
 	public static int READ_TIME_OUT = 30000;
 	private HttpURLConnection mHttpConnection = null;
 	private static String TAG = "HTTPClient";
 	
 	public static String URL_INDEX;
 	public static HttpGet URL_REQUEST;
 	public static String HOST;
 	public static String COVER_INDEX_PREFIX;
 	
 	public static String URL_ABOUTPAGE;
 	
 	public static Map<String, String> urlMap;
 	
 	static {
 		URL_INDEX = "http://184.105.176.95/app/DemoCenter/Api/albumlist.vdi";
 		URL_REQUEST = new HttpGet(URL_INDEX);
 		HOST = "http://184.105.176.95";
 		COVER_INDEX_PREFIX = "http://184.105.176.95";
 		
 	}
 	
 	public static URLConnection getJSONHttpConnection(String strURL)
 			throws SocketTimeoutException, MalformedURLException, IOException {
 		Log.v(TAG, "getHttpConnection");
 		URL url = new URL(strURL);
 		HttpURLConnection httpURLConnection = (HttpURLConnection) url
 				.openConnection();
 		httpURLConnection.setConnectTimeout(CONNECT_TIME_OUT);
 		httpURLConnection.setReadTimeout(READ_TIME_OUT);
 		httpURLConnection.setAllowUserInteraction(false);
 		httpURLConnection.setInstanceFollowRedirects(true);
 		httpURLConnection.setRequestMethod("GET");
 		Log.v(TAG, "connection OK");
 		return httpURLConnection;
 	}
 	
 	public static URLConnection getHttpConnection(String strURL)
 			throws SocketTimeoutException, MalformedURLException, IOException {
 		Log.v(TAG, "getHttpConnection");
 		URL url = new URL(strURL);
 		HttpURLConnection httpURLConnection = (HttpURLConnection) url
 				.openConnection();
 		httpURLConnection.setConnectTimeout(CONNECT_TIME_OUT);
 		httpURLConnection.setReadTimeout(READ_TIME_OUT);
 		httpURLConnection.setRequestMethod("GET");
 		Log.v(TAG, "connection OK");
 		return httpURLConnection;
 	}
 	
 	/**
 	 * 根据Url得到InputStream 先查找本地是否存储有从该Url取得的JSON如果存在直接读取，
 	 * 如果不存在从Url取得JOSN文件并存储到指定位置
 	 * 
 	 * @param url
 	 * @returnhou
 	 * @throws SocketTimeoutException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public static JSONArray getJSONArrayFromUrl(String url)
 			throws SocketTimeoutException, MalformedURLException, IOException,
 			JSONException {
 		String result = "";
 		InputStream inputStream = null;
 		ByteArrayOutputStream bao = null;
 		HttpURLConnection urlConn_sourceRecommend = null;
 		int retry  = 3;
 		while (retry-- > 0) {
 			try {
 				urlConn_sourceRecommend = (HttpURLConnection)getJSONHttpConnection(url);
 				urlConn_sourceRecommend.connect();
 				inputStream = urlConn_sourceRecommend.getInputStream();
 				bao = new ByteArrayOutputStream();
 				byte[] temp = new byte[1024];
 				int count = 0;
 				while ((count = inputStream.read(temp)) >= 0) {
 					bao.write(temp, 0, count);
 				}
 				result = new String(bao.toByteArray());
 				Log.v(url, result);
 				break;
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (inputStream != null) 
 					inputStream.close();
 				if (bao != null) {
 					bao.close();
 				}
 				urlConn_sourceRecommend.disconnect();
 			}
 		}
 		return new JSONArray(result);
 		
 //		String result = doConnect(new HttpGet(url));
 //		return new JSONArray(result);
 	}
 	
 	public static boolean getStreamFromUrl(String url, String path)
             throws SocketTimeoutException, MalformedURLException, IOException,
             JSONException {
         // 注释掉从文件取结果，这个功能没有要求
         InputStream inputStream = null;
         BufferedOutputStream bao = null;
         int retry = 3;
         while (retry-- > 0) {
         	try {
 		        URLConnection urlConn_sourceRecommend = getJSONHttpConnection(url);
 		        urlConn_sourceRecommend.connect();
 		        inputStream = urlConn_sourceRecommend.getInputStream();
 		        String tmpFileName = path + ".tmp";
 		        bao = new BufferedOutputStream(new FileOutputStream(tmpFileName));
 		        byte[] temp = new byte[1024];
 		        int count = 0;
 		        while ((count = inputStream.read(temp)) >= 0) {
 		            bao.write(temp, 0, count);
 		        }
 		        File file = new File(path);
 		        File tmpFile = new File(tmpFileName);
 		    	tmpFile.renameTo(file);
 		    	break;
         	} catch (Exception e) {
         		e.printStackTrace();
         	} finally {
         		if (inputStream != null) {
         			inputStream.close();
         		}
         		if (bao != null) {
         			bao.close();
         		}
         	}
         }
         return true;
     }
 
 	protected static InputStream getStreamFromNetwork(URI imageUri) throws IOException {
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet httpRequest = new HttpGet(imageUri.toString());
         HttpResponse response = httpClient.execute(httpRequest);
         HttpEntity entity = response.getEntity();
         BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
         return bufHttpEntity.getContent();
     }
 	
 	private static BasicHttpParams mHttpParams;
 	private static DefaultHttpClient mHttpClient;
 	protected static String doConnect(HttpUriRequest jsonUri) {
 	    mHttpParams = new BasicHttpParams();
         HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(mHttpParams, "UTF-8");
         HttpConnectionParams.setConnectionTimeout(mHttpParams, 10000);
         HttpConnectionParams.setSoTimeout(mHttpParams, 10000);
         // 加入代理
         mHttpClient = new DefaultHttpClient(mHttpParams);
         // 重试一次
         mHttpClient.setHttpRequestRetryHandler(
                 new DefaultHttpRequestRetryHandler(2, true));
         HttpResponse httpResponse = null;
         try {
             httpResponse = mHttpClient.execute(jsonUri);
             return getContent(httpResponse);
         } catch (ClientProtocolException e) {
         	Log.v(TAG, jsonUri.toString());
             Log.e(TAG, "", e);
         } catch (IOException e) {
         	Log.v(TAG, jsonUri.getURI().getPath());
             Log.e(TAG, "", e);
         }
         return null;
 	}
 	
 	public static String getContent(HttpResponse response) throws IOException {
         if (response == null)
             return null;
         boolean isGzip = false;
         Header[] headers = response.getHeaders("Content-Encoding");
         if (headers != null && headers.length > 0) {
             final int size = headers.length;
             for (int i = 0; i < size; ++i) {
                 final Header header = headers[i];
                 if (header.getValue().toLowerCase().indexOf("gzip") > -1) {
                     isGzip = true;
                     break;
                 }
             }
 
             if (isGzip) {
                 final InputStream is = response.getEntity().getContent();
                 GZIPInputStream gis = new GZIPInputStream(is);
                 BufferedReader br = new BufferedReader(new InputStreamReader(
                         gis));
                 StringBuffer sb = new StringBuffer();
                 String line = null;
                 while ((line = br.readLine()) != null) {
                     sb.append(line);
                 }
                 gis.close();
                 return sb.toString();
             }
         }
         String ret = null;
         try {
             ret = EntityUtils.toString(response.getEntity());
         } catch (OutOfMemoryError e) {
         }
         return ret;
     }
 }
