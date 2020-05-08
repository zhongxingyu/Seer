 package com.fpcms.common.util;
 
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.io.IOUtils;
 import org.apache.shiro.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class NetUtil {
 	private static Logger logger = LoggerFactory.getLogger(NetUtil.class);
 	public static MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
 	static HttpClient client = new HttpClient();
 	
 	static {
 		init();
 	}
 
 	public static void init() {
 		if (manager != null) {
 			manager.shutdown();
 			manager = null;
 		}
 		int connetctTimeout = 10000;
 		int soTimeout = 25000;
 		manager = new MultiThreadedHttpConnectionManager();
 		System.setProperty("apache.commons.httpclient.cookiespec","COMPATIBILITY");
 		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
 		params.setDefaultMaxConnectionsPerHost(50);
 		params.setMaxTotalConnections(1000);
 		params.setConnectionTimeout(connetctTimeout);
 		params.setSoTimeout(soTimeout);
 		manager.setParams(params);
 		manager.closeIdleConnections(15000);
 		
 		
 		client.setHttpConnectionManager(manager);
 		HttpClientParams cparams = new HttpClientParams();
 		cparams.setConnectionManagerTimeout(connetctTimeout);
 		cparams.setSoTimeout(soTimeout);
 		client.setParams(cparams);
 	}
 
 
 	public static String httpGet(String url) {
 		return httpGet(url,null);
 	}
 	
 	public static String httpGet(String url, String parameters) {
 		String finalUrl = getUrlWithParameters(url, parameters);
 		logger.info("httpGet:"+finalUrl+" parameters:"+parameters);
 		GetMethod method = new GetMethod(finalUrl);
 		method.setFollowRedirects(true);
 		method.setRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
 		method.setRequestHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
 		method.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		method.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 //		method.setRequestHeader("Accept-Encoding", "gzip, deflate");
 		try {
 			client.executeMethod(method);
 			InputStream is = method.getResponseBodyAsStream();
			String result = IOUtils.toString(is,"GBK");
			return result;
 		} catch (Exception e) {
 			throw new RuntimeException("读取url=[" + finalUrl + "]数据异常",e); 
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 
 	private static String getUrlWithParameters(String url, String parameters) {
 		String finalUrl = null;
 		if(url.contains("?")) {
 			finalUrl = url + "&" + encode(parameters);
 		}else {
 			finalUrl = url + "?" + encode(parameters);
 		}
 		return finalUrl;
 	}
 
 
 	private static String encode(String parameters) {
 		if(parameters == null) return null;
 		try {
 			return URLEncoder.encode(parameters, "UTF-8").replace("%3D", "=").replace("%26", "&");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
