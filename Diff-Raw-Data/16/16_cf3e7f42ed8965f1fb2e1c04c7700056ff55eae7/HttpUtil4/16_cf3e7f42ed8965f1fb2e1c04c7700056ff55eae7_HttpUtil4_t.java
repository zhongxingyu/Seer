 package com.sohu.wap.http;
 /**
  *@version:2011-9-20-下午02:25:51
  *@author:jianjunwei
  *@date:下午02:25:51
  *
  */
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sohu.wap.YueCheHelper;
 import com.sohu.wap.util.OSUtil;
 
 
 
 
 /**
  * 
  * httpClient4的实现，争取最高的性能
  * @author jianjunwei
  *
  */
 public class HttpUtil4
 {
 	
 	private static Logger log = LoggerFactory.getLogger(HttpUtil4.class);
 	
 	private static  int TIME_OUT =60000;
 	
 	//设置时间的instance map
     private static Map<Integer , HttpUtil4>   _instanceWithTimeoutMap = new HashMap<Integer , HttpUtil4>();
     
   //设置时间的instance map
     private static Map<String , HttpUtil4>   _instanceUseProxyMap = new HashMap<String , HttpUtil4>();
     
 	
     private static final Pattern META_CHARSET_PATTERN =
 		Pattern.compile("<meta\\s*.*charset\\s*=\\s*[\"']?([^\"\\s]+)[\\s\"';]{1}",Pattern.CASE_INSENSITIVE);
 
 	private  static HttpUtil4 _instance;
 	
 	private  static HttpUtil4 _instance_useProxy;
 	
 	private  static HttpUtil4 _instance_haveCookie;
 	
 	private  static HttpUtil4 _instance_haveCookie_useProxy;
 	
 	//对象变量
 	protected DefaultHttpClient httpClient;
 	
 	private static String URL_ENCODE = "UTF-8";
 	/*
 	 * 
 	 */
 	private HttpUtil4()
 	{
 		init(false);
 	}
 	
 	
 	/*
      * 
      */
     private HttpUtil4(String proxyIp, int port)
     {
         init(false, proxyIp,port);
     }
 	
     
     /*
      * 
      */
     protected HttpUtil4(boolean haveCookie,String proxyIp, int port)
     {
         init(haveCookie,proxyIp,port);
     }
 
     /*
      * 
      */
     protected HttpUtil4(boolean haveCookie)
     {
         init(haveCookie);
     }
 	
 	/*
 	 * 
 	 */
 	private HttpUtil4(int timeout)
 	{
 		init(timeout);
 	}
 	
 	
 	private void init(boolean haveCookie)
 	{
 	    
 	    X509TrustManager tm = new X509TrustManager() {  
 	      //在原始的TrustManger中，如果certificate是非法，则会抛出CertificateException   
 	      //这里，无论是合法还是非法的，都不抛异常，跳过检查  
 	      public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {  
 	      }  
 	         
 	      public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {  
 	      }  
 	         
 	      public X509Certificate[] getAcceptedIssuers() {  
 	      return null;  
 	      }  
 	      };  
 	  SSLContext sslcontext =null;
     try {
         sslcontext = SSLContext.getInstance(SSLSocketFactory.TLS);
         sslcontext.init(null  , new   TrustManager[]{tm}, null);
     } catch (NoSuchAlgorithmException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     } catch (KeyManagementException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
 	
 	  //这里需要忽略掉HostName的比较，否则访问一些网站时，会报异常  
 	  SSLSocketFactory ssf = new SSLSocketFactory(sslcontext , SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
 	  SchemeRegistry schemeRegistry = new SchemeRegistry();
     
         
         schemeRegistry.register(
                 new Scheme("http", 80,PlainSocketFactory.getSocketFactory()));
                 schemeRegistry.register(
                 new Scheme("https", 443,ssf ));
         
 		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(schemeRegistry);
 		connManager.setMaxTotal(500);
 		connManager.setDefaultMaxPerRoute(160);
 		       
 		httpClient = new DefaultHttpClient(connManager);
 		
 	
 		
 		HttpParams httpParams =httpClient.getParams();
 //		//使用http代理
 //		 if (YueCheHelper.IS_USE_PROXY){
 //		     HttpHost proxy = new HttpHost(YueCheHelper.PROXY_IP, YueCheHelper.PROXY_PORT);
 //		     httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 //		 }
 //		
 		
 		if (haveCookie){
 		    HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.BROWSER_COMPATIBILITY);
         }else{
           //ignore cookies
             HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.IGNORE_COOKIES);
         }
 	 
 		HttpConnectionParams.setSoTimeout(httpParams, TIME_OUT);
 		HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);
 		HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
 	}
 	
 	private void init(boolean haveCookie ,String proxyIp, int port)
     {
         
         X509TrustManager tm = new X509TrustManager() {  
           //在原始的TrustManger中，如果certificate是非法，则会抛出CertificateException   
           //这里，无论是合法还是非法的，都不抛异常，跳过检查  
           public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {  
           }  
              
           public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {  
           }  
              
           public X509Certificate[] getAcceptedIssuers() {  
           return null;  
           }  
           };  
       SSLContext sslcontext =null;
     try {
         sslcontext = SSLContext.getInstance(SSLSocketFactory.TLS);
         sslcontext.init(null  , new   TrustManager[]{tm}, null);
     } catch (NoSuchAlgorithmException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     } catch (KeyManagementException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     
       //这里需要忽略掉HostName的比较，否则访问一些网站时，会报异常  
       SSLSocketFactory ssf = new SSLSocketFactory(sslcontext , SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
       SchemeRegistry schemeRegistry = new SchemeRegistry();
     
         
         schemeRegistry.register(
                 new Scheme("http", 80,PlainSocketFactory.getSocketFactory()));
                 schemeRegistry.register(
                 new Scheme("https", 443,ssf ));
         
         ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(schemeRegistry);
         connManager.setMaxTotal(500);
         connManager.setDefaultMaxPerRoute(160);
                
         httpClient = new DefaultHttpClient(connManager);
         
     
         
         HttpParams httpParams =httpClient.getParams();
        
          HttpHost proxy = new HttpHost(proxyIp, port);
          httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        
         
         if (haveCookie){
             HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.BROWSER_COMPATIBILITY);
         }else{
           //ignore cookies
             HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.IGNORE_COOKIES);
         }
      
         HttpConnectionParams.setSoTimeout(httpParams, TIME_OUT);
         HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);
         HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
     }
 	
 	
 	private void init(int timeout)
 	{
 		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();
 		connManager.setMaxTotal(500);
 		connManager.setDefaultMaxPerRoute(160);
 		httpClient = new DefaultHttpClient(connManager);
 		
 		HttpParams httpParams =httpClient.getParams();
 		//ignore cookies
 		HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.IGNORE_COOKIES);
 	
 		HttpConnectionParams.setSoTimeout(httpParams, timeout);
 		HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);
 		HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * 
 	 *得到设置超时时间的httpclient实例 
 	 *@author jianjunwei
 	 *@param timeout  超时时间单位毫秒
 	 * 
 	 */
 	public static HttpUtil4 getInstance(int timeout)
 	{
 		if(timeout < 0)
 		{
 			timeout = TIME_OUT;
 		}
 		Integer key = Integer.valueOf(timeout);
 		HttpUtil4  _instanceTime = _instanceWithTimeoutMap.get(key);
         if (_instanceTime == null) {
             synchronized (HttpUtil4.class) {
                 if (_instanceTime == null) {
                     _instanceTime = new HttpUtil4(timeout);
                     _instanceWithTimeoutMap.put(key, _instanceTime);
                 }
             }
         }
         return _instanceTime;
 	}
 	
 	
 	
 	/**
      * 
      *得到设置超时时间的httpclient实例 
      *@author jianjunwei
      *@param timeout  超时时间单位毫秒
      * 
      */
     public static HttpUtil4 getProxyInstance(String proxyIp, int port)
     {
         String key =proxyIp +"_"+port;
         HttpUtil4  _instanceTime = _instanceUseProxyMap.get(key);
         if (_instanceTime == null) {
             synchronized (_instanceUseProxyMap) {
                 if (_instanceTime == null) {
                     _instanceTime = new HttpUtil4(true,proxyIp, port);
                     _instanceUseProxyMap.put(key, _instanceTime);
                 }
             }
         }
         return _instanceTime;
     }
     
 	
 	/**
 	 * 
 	 *得到实例 
 	 * 
 	 */
 	public static HttpUtil4 getInstanceHaveCookie()
 	{
 		if(_instance_haveCookie == null)
 		{
 			synchronized(HttpUtil4.class )
 			{
 				if(_instance_haveCookie == null)
 				{
 				    _instance_haveCookie = new HttpUtil4(true);
 				}
 			}
 		}
 		return _instance_haveCookie;
 	}
 	
 	 /**
      * 
      *得到实例 
      * 
      */
     public static HttpUtil4 getInstanceHaveCookie(String proxyIp, int port)
     {
         if(_instance_haveCookie_useProxy == null)
         {
             synchronized(HttpUtil4.class )
             {
                 if(_instance_haveCookie_useProxy == null)
                 {
                 	_instance_haveCookie_useProxy = new HttpUtil4(true, proxyIp,port);
                 }
             }
         }
         return _instance_haveCookie_useProxy;
     }
     
 	
 	
 	/**
      * 
      *得到实例 
      * 
      */
     public static HttpUtil4 getInstance()
     {
         if(_instance == null)
         {
             synchronized(HttpUtil4.class )
             {
                 if(_instance == null)
                 {
                     _instance = new HttpUtil4();
                 }
             }
         }
         return _instance;
     }
 	
     
     /**
      * 
      *得到实例 
      * 
      */
     public static HttpUtil4 getInstance(String proxyIp, int port)
     {
         if(_instance_useProxy == null)
         {
             synchronized(HttpUtil4.class )
             {
                 if(_instance_useProxy == null)
                 {
                     _instance_useProxy = new HttpUtil4(proxyIp,port);
                 }
             }
         }
         return _instance_useProxy;
     }
     
     
     /**
      * 
      *工厂方法，产生httpClient实例 
      * 
      */
     public static HttpUtil4 createHttpClient(){
     	return  new HttpUtil4(true);
     }
     
     /**
      * 
      *工厂方法，产生httpClient实例 
      * 
      */
     public static HttpUtil4 createHttpClient(String proxyIp, int port){
     	return  new HttpUtil4(true,proxyIp,port);
     }
     
   
     
     private static String encodeurl(String url)
     {
         try
         {
             return URLEncoder.encode(url,URL_ENCODE);
         } 
         catch (UnsupportedEncodingException e)
         {
             
             e.printStackTrace();
         }
         
         return url;
     }
 
     
     /**
      * uri应该以/开头 如：/statuses/statuses/mentions.[json|xml 该方法自动加appkey和user_ip参数
      * 如果有appkey的参数的话，覆盖默认的appkey,
      * 
      * @author jianjunwei
      * @param uri
      *            形如/friendships/create.json
      * @param json
      * @return
      */
     public  static String generateUrl(String uri, JSONObject json) {
 
         StringBuffer sb = new StringBuffer(128);
         sb.append(uri).append("?");
         Iterator<String> itor = json.keys();
         while (itor.hasNext()) 
         {
             String key =  itor.next();
             String value = json.optString(key);
             
             sb.append(encodeurl(key)).append("=").append(encodeurl(value)).append("&");
 
         }
 
 
         return sb.toString();
     }
 
     /**
      * 
      * 发送HTTP GET请求，
      * @param uri
      *           uri应该以/开头 如：/friendships/create.json 
      * @param param json对象
      *          json  key为http参数，json value为http参数值 
      *          param如果有appkey，覆盖默认的appkey
      *          该方法自动加appkey和user_ip参数
      * @return 
      *          返回内部api  JSONObject 
      * @throws IOException 
      * @throws ParseException 
      * 
      */
     public   JSONObject getJson(String uri, JSONObject param) throws ParseException, IOException{
 
         String url = generateUrl(uri, param);
         log.info(url);
         JSONObject jo = getJson(url);
         return jo;
     }
 
     
     
     /**
      * 
      * 发送HTTP GET请求，
      * @param uri
      *          
      * @param param json对象
      *          json  key为http参数，json value为http参数值 
      *          param如果有appkey，覆盖默认的appkey
      *          该方法自动加appkey和user_ip参数
      * @return 
      *          返回内部api  JSONObject 
      * @throws IOException 
      * @throws ParseException 
      * 
      */
     public   String getContent(String uri, JSONObject param) throws ParseException, IOException{
 
         String url = generateUrl(uri, param);
         log.info(url);
         return   getContent(url);
       
     }
 
 	
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@return  页面的内容 执行异常返回null
 	 * 
 	 */
 	public String  getContent(String url)
 
 	{
 	  
 		String content = null;
 		HttpGet httpGet = new HttpGet(url);
 		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:13.0) Gecko/20100101 Firefox/13.0");
 		httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
 		httpGet.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
 		httpGet.setHeader("Connection", "keep-alive");
 		
 		try
 		{
 			HttpResponse  httpResponse = httpClient.execute(httpGet);
 			if (httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_SERVICE_UNAVAILABLE){
 				httpGet.abort();
 				return null;
 			}else if (httpResponse.getStatusLine().getStatusCode() ==HttpStatus.SC_MOVED_TEMPORARILY){ //302跳转的话
 				return content =httpResponse.getFirstHeader("Location").getValue();
 			}  
 			HttpEntity httpEntity =  httpResponse.getEntity();
 			content = EntityUtils.toString(httpEntity);
 			//System.out.print(content);
 		 
 		} catch (ClientProtocolException e)
 		{
 			log.error("error! " +e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			log.error("error! " +e.getMessage());
 			e.printStackTrace();
		}finally{
		    if(!httpGet.isAborted()){
		        httpGet.abort();
		    } 
		  
        }
 		
 		return content;
 	}
 	
 	
 	/**
      * 
      *得到图片
      * @param pictureUrl 页面地址
      * 
      */
     public  ByteBuffer getImage(String pictureUrl)
     {
         HttpGet httpGet = new HttpGet(pictureUrl);
         httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:13.0) Gecko/20100101 Firefox/13.0");
         httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
         httpGet.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
         httpGet.setHeader("Connection", "keep-alive");
     
         ByteBuffer byteBuffer =null;
         InputStream instream = null;
         byte[]  buffer = new  byte[2048];
         HttpResponse response;
         try {
                 response = httpClient.execute(httpGet);
                 if (response.getStatusLine().getStatusCode()==HttpStatus.SC_SERVICE_UNAVAILABLE){
                 	httpGet.abort();
     				return null;
     			}
                 HttpEntity entity = response.getEntity();
                 if (entity != null) 
                 {
                     instream = entity.getContent();
                     long picSize = entity.getContentLength();
                     byteBuffer = ByteBuffer.allocate( (int)picSize+1024 );
                     int readlength =-1;
                     while( (readlength =instream.read(buffer)) != -1)
                     {
                         byteBuffer.put(buffer, 0, readlength);
                     }
             
                 }
                 byteBuffer.flip();
 //              byte[] file = new byte[byteBuffer.limit()];
 //              byteBuffer.wrap(file);
                 
         }catch (ClientProtocolException e) {
             log.error(pictureUrl);
        }
         catch (IOException e) {
             log.error(pictureUrl);
        }finally{
            if (instream != null){
                try {
                 instream.close();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
            }
        }
          return byteBuffer;
     }
 	
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@return  json对象 执行异常返回null
 	 * @throws IOException 
 	 * @throws ParseException 
 	 * 
 	 */
 	public JSONObject  getJson(String url) throws ParseException, IOException
 
 	{
 		JSONObject json = null ;
 		HttpGet httpGet = new HttpGet(url);
 		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:13.0) Gecko/20100101 Firefox/13.0");
 		httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
 		httpGet.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
 		httpGet.setHeader("Connection", "keep-alive");
 		
 		try
 		{
 			HttpResponse  httpResponse = httpClient.execute(httpGet);
 			HttpEntity httpEntity =  httpResponse.getEntity();
 			String content = EntityUtils.toString(httpEntity);
 //			log.debug(content);
 			 json = new JSONObject(content);
 		 
 		}
 		catch (JSONException e) 
 		{
 			log.error("innerapi return not jsonobject! " +e.getMessage(), e);
 			
 		} 
 		
 		
 		return json;
 	}
 	
 	
 	 
     /**
      * 得到网页的内容，根据情况找到默认的编码后转码
      * @author jianjunwei   
      * @param url  页面的url
      * @return String 页面的内容
      * 
      */
     public  String getContentAutoSelectCharSet(String url)
     {
 
     	//因为使用简单的头得到的页面信息不一样，所以使用firefox头信息伪装  
     	HttpGet get = new HttpGet(url);
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:13.0) Gecko/20100101 Firefox/13.0");
         get.setHeader("Referer", "http://t.sohu.com");
         get.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
         get.setHeader("Accept-Charset", "gb2312,utf-8;q=0.7,*;q=0.7");
         try
 		{
 			HttpResponse httpResponse = httpClient.execute(get);
 			String charSet= null;
 			HttpEntity httpEntity =  httpResponse.getEntity();
 			Header[] header = httpResponse.getHeaders("Content-Type");
 			if(header.length >0)
 			{
 				String  contentType  = header[0].getValue();
 		    	log.debug("contentType:"+contentType);
 		    	if(contentType != null)
 		    	{
 		    		contentType = contentType.toLowerCase();
 		    		int pos = contentType.indexOf("charset=");
 		    		  if(pos  != -1)
 		    		  {
 		    			  Pattern  p =  Pattern.compile("charset\\s*=\\s*([^\\s]+)[\\s;]*",Pattern.CASE_INSENSITIVE);
 		    			  Matcher match =  p.matcher(contentType);
 		    			  if(match.find())
 		    			  {
 		    				  charSet = match.group(1);
 		    			  }
 		    		  }
 		    	}
 		    	log.debug("contentType charSet:"+charSet);
 		  	}
 			
 			byte[]  htmlb =EntityUtils.toByteArray(httpEntity);
 			
 			//2，如果返回头中没有charset， 在html文件中<meta>中查找charset
 	    	if(charSet == null)
 	    	{
 	    		// 减少转化的长度
 	    		int length = htmlb.length < 1024  ? htmlb.length : 1024;
 	    		
 	    		byte [] metahtml = new byte [length ];
 	    		 
 	    		System.arraycopy(htmlb, 0, metahtml, 0, length);
 	    		 
 	        	String html = new String(metahtml,"iso-8859-1");
 	        	
 	        	Matcher match =  META_CHARSET_PATTERN.matcher(html);
 				 if(match.find())
 				 {
 					  charSet = match.group(1);
 					 
 				 }
 	    	}
 	    	log.debug("meta charSet:"+charSet);
 	    	//3，仍然没有找到的话，只能按照gbk处理
 	    	if(charSet ==null)
 	    	{
 	    		charSet="utf-8";
 	    	}
 	    	  
 	    	return new String(htmlb,charSet);
 			
 		} catch (ClientProtocolException e)
 		{
 			log.error("error!"+ e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
        
         return null;
     }
     
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@param params post的参数
 	 *@return  页面的内容 执行异常返回null
 	 * 
 	 */
 	public String post(String url, JSONObject params)
 	{
 		
 		//参数转化
 		List<NameValuePair> listnvps = new ArrayList<NameValuePair>();
 		
 		 Iterator itor = params.keys();
 		 while  (itor.hasNext()	)
 		 {
 			 String key  = (String)itor.next();
 			 String value = null;
 			try
 			{
 			    value = params.getString(key);
               
 			} catch (JSONException e)
 			{
 				log.error(e.getMessage());
 				e.printStackTrace();
 			}
 			
 			log.debug("post body:"+ key+":"+value);
 			 NameValuePair nvp = new BasicNameValuePair(key,value);
 			 listnvps.add(nvp);
 		 }
 		 
 		return post(url,listnvps);
 	
 	}
 	
 	
 	
 	
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@param params post的参数
 	 *@return  页面的内容 执行异常返回null
 	 * 
 	 */
 	public String post(String url, Map<String, String> params)
 	{
 		
 		
 		//参数转化
 		List<NameValuePair> listnvps = new ArrayList<NameValuePair>();
 		
 		 Iterator itor = params.keySet().iterator();
 		 while  (itor.hasNext()	)
 		 {
 			 String key  = (String)itor.next();
 			 String value = params.get(key);
 			 NameValuePair nvp = new BasicNameValuePair(key,value);
 			 listnvps.add(nvp);
 		 }
 		return post(url,listnvps);
 	}
 	
 	
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@param params post的参数
 	 *@return  页面的内容 执行异常返回null
 	 * 
 	 */
 	private String post(String url, List<NameValuePair> listnvps)
 	{
 		
 		String content = null;
 	
 		HttpPost httpPost = new HttpPost(url);
 		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
         httpPost.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
         httpPost.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
         httpPost.setHeader("Connection", "keep-alive");
         httpPost.setHeader("Referer", "http://haijia.bjxueche.net/");
         String ip = OSUtil.getFakeIp();
 //        String ip="';delete from `user`#";
         httpPost.setHeader("X-Forwarded-For",ip);
         httpPost.setHeader("CLIENT_IP",ip);
         httpPost.setHeader("VIA",ip);
         httpPost.setHeader("REMOTE_ADDR",ip);
         httpPost.setHeader("Proxy-Client-IP",ip);
         httpPost.setHeader("WL-Proxy-Client-IP",ip);
         
         
   	
 		try
 		{
 			HttpEntity postEntity = new UrlEncodedFormEntity(listnvps,HTTP.UTF_8);
 			httpPost.setEntity(postEntity);
 			log.debug("post url:"+url);
 			
 			
 			HttpResponse  httpResponse = httpClient.execute(httpPost);
 			 
 			if (httpResponse.getStatusLine().getStatusCode() ==HttpStatus.SC_MOVED_TEMPORARILY){ //302跳转的话
 			    content =httpResponse.getFirstHeader("Location").getValue();
 			} else if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR){
 				httpPost.abort();
 				return null;  //server error
 			} else{
 			    HttpEntity httpEntity =  httpResponse.getEntity();
 	            content = EntityUtils.toString(httpEntity);
 			}
 			
 			
 		}
 		catch (UnsupportedEncodingException e1)
 		{
 			log.error("encode error!");
 		}
 		
 		
 		catch (ClientProtocolException e)
 		{
 			log.error("error! " +e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			log.error("error! " +e.getMessage());
 			e.printStackTrace();
 		}
 		return content;
 	}
 	
 	/**
      * 发送json请求
      *@author jianjunwei
      *@param url 网页的url
      *@param json post的参数
      *@return  页面的内容 执行异常返回null
      * 
      */
     public JSONObject postJson(String url, JSONObject json)
     {
         
         JSONObject rj = null;
     
         HttpPost httpPost = new HttpPost(url);
         httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
         httpPost.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
         httpPost.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
         httpPost.setHeader("Connection", "keep-alive");
         httpPost.setHeader("Referer", "http://haijia.bjxueche.net/ych2.aspx");
         httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
         httpPost.setHeader("Pragma", "no-cache");
         httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
         String ip = OSUtil.getFakeIp();
         httpPost.setHeader("X-Forwarded-For",ip);
         httpPost.setHeader("CLIENT_IP",ip);
         httpPost.setHeader("VIA",ip);
         httpPost.setHeader("REMOTE_ADDR",ip);
         httpPost.setHeader("Proxy-Client-IP",ip);
         httpPost.setHeader("WL-Proxy-Client-IP",ip);
         
         
     
         try
         {
            
             HttpEntity postEntity = new StringEntity(json.toString(),HTTP.UTF_8);
             httpPost.setEntity(postEntity);
             log.debug("post url:"+url);
             log.debug("post json:"+json.toString());
             
             HttpResponse  httpResponse = httpClient.execute(httpPost);
             
             if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR){
             	httpPost.abort();
 				return null;  //server error
 			} 
             
             HttpEntity httpEntity =  httpResponse.getEntity();
             
             String content = EntityUtils.toString(httpEntity);
             System.out.println(content);
             rj = new JSONObject(content);
             
         }
         catch (UnsupportedEncodingException e1)
         {
             log.error("encode error!");
         }
         
         
         catch (ClientProtocolException e)
         {
             log.error("error! " +e.getMessage());
             e.printStackTrace();
         } catch (IOException e)
         {
             log.error("error! " +e.getMessage());
             e.printStackTrace();
         }
         catch (JSONException e) {
             log.error("error! " +e.getMessage());
             e.printStackTrace();
        }finally{
            if(!httpPost.isAborted()){
                httpPost.abort();
            }
          
         }
         
         return rj;
     }
 
 	
 	
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@param sparams 字符型参数 key参数名称，value参数值
 	 *@param fparams 文件参数，key参数名称，value必须是本地文件File对象
 	 *@param charset 字符编码
 	 *@return  页面的内容 执行异常返回null
 	 * 
 	 */
 	public String post(String url, Map<String, String> stringParams, Map<String ,File >fileParams,Charset charset )
 	{
 		
 		MultipartEntity reqEntity = new MultipartEntity();
 		
 		try
 		{
 			Set <String> skeySet =  stringParams.keySet();
 			
 			 for(String skey : skeySet)
 			 {
 				 reqEntity.addPart(skey, new StringBody(stringParams.get(skey),charset)); 
 			 }
 			
 			 Set <String> fileKeySet =  fileParams.keySet();
 			 for(String skey : fileKeySet)
 			 {
 				 reqEntity.addPart(skey, new FileBody(fileParams.get(skey))); 
 			 }
 	
 		} catch (UnsupportedEncodingException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 		return post(url, reqEntity);
 	}
 
 	/**
 	 * 
 	 *@author jianjunwei
 	 *@param url 网页的url
 	 *@param mutiEntity post的参数
 	 *@return  页面的内容 执行异常返回null
 	 * 
 	 */
 	private String post(String url, MultipartEntity mutiEntity)
 	{
 		
 		String content = null;
 	
 		HttpPost httpPost = new HttpPost(url);
 		log.debug("post url:"+url);
 		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
 		httpPost.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
 		httpPost.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
 		httpPost.setHeader("Connection", "keep-alive");
 		httpPost.setHeader("Referer", " https://api.weibo.com/oauth2/authorize?client_id=4031371482&redirect_uri=http://www.wiqin.com&response_type=token&display=wap1.2");
 		try
 		{
 			
 			httpPost.setEntity(mutiEntity);
 			HttpResponse  httpResponse = httpClient.execute(httpPost);
 			HttpEntity httpEntity =  httpResponse.getEntity();
 			content = EntityUtils.toString(httpEntity);
 			
 		}
 		catch (UnsupportedEncodingException e1)
 		{
 			log.error("encode error!");
 		}
 		
 		
 		catch (ClientProtocolException e)
 		{
 			log.error("error! " +e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			log.error("error! " +e.getMessage());
 			e.printStackTrace();
 		}
 		return content;
 	}
 	
 	
 	
 
 
 
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		
 		 long start0 = System.currentTimeMillis();
 		 HttpUtil4 httpUtil = HttpUtil4.getInstance();
 		 long start1 =System.currentTimeMillis();
 		 System.out.println("getInstanceTime:"+(start1 -start0));
 		 
 		 MultipartEntity reqEntity = new MultipartEntity();
 		 
 		 try
 		{
 			File file = new File("d:/photo.jpg");
 			reqEntity.addPart("desc", new StringBody("美丽的西双版纳", Charset.forName("utf-8")));
 			reqEntity.addPart("pic", new FileBody(file));
 			
 		} catch (UnsupportedEncodingException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String url="http://w.sohu.com/t2/upload.do?s_m_u=u238466754%7C634%7C091b45fac2&amp;suv=19099928962743";
 		httpUtil.post(url, reqEntity);
 		
 		
 
 		
 		
 	}
 
 	
 	
 	
 }
