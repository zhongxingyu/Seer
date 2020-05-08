 /**
  *
  * @desc: Http Read Tool
  * 
  */
 package com.sohu.wuhan;
 
 import java.net.Proxy;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import android.os.Handler;
 
 import com.sohu.wuhan.Constant.Error;
 
 
 /**
 * @author Leon
  *
  */
 public class HttpRead implements IReadable, INetwork {
 	private static HttpRead ins = new HttpRead();
 	
 	public static HttpRead instance() {
 		return ins;
 	}
 	
 	private boolean bInit = false;
 	
 	private String domain = Constant.DOMAIN;
 	private String accessToken 	= "";
 	private DirectUrl durl 		= null;
 	private AsyncTask async 	= null;
 	private String encoding		= "UTF-8";
 	
 	protected HttpRead() {}
 	
 	public boolean init(String __token) { 
 		if (bInit) return true;
 		if (null == __token) return false;
 		
 		accessToken = __token; 
 		
 		try {
 			this.setDomain(null);
 			
 			durl 	= 	new DirectUrl();
 			async	=	new AsyncTask(durl);
 			
 			async.start();
 		} catch (Exception e) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public Error getError() { return durl.error; }
 		
 	/* ================================= INetwork interface ====================================== */
 	
 	@Override
 	public boolean setDomain(String domain) {
 		if (null == domain || domain.equalsIgnoreCase("")) {
 			domain = Constant.DOMAIN;
 		} else 
 			this.domain = domain;
 		
 		return true;
 	}
 
 	@Override
 	public String getDomain() {
 		return domain;
 	}
 	
 	public void setProxy(Proxy __proxy) {
 		durl.setProxy(__proxy);
 	}
 	
 	public void delProxy() { durl.delProxy(); }
 	
 	public boolean isProxy() { return durl.isProxy(); } 
 	
 	public String getEncoding() { return encoding; }
 	
 	public void setEncoding(String __encoding) { encoding = __encoding; }
 	
 	/* ================================= End INetwork interface ====================================== */
 	
 	
 	/* (non-Javadoc)
 	 * @see com.sohu.wuhan.IReadable#createArticle(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public Error createArticle(String __url) {
 		String content = null;
 		
 		if (null == __url) return Error.ParamNull;
 		
 		try {
 			content = URLEncoder.encode("access_token", encoding) + "=" + URLEncoder.encode(accessToken, encoding); 
 			content += "&" + URLEncoder.encode("url", encoding) + "=" + URLEncoder.encode(__url, encoding);
 		} catch (Exception e) {
 			return Error.Encode;
 		}
 		
 		String url = domain + Constant.ARTICLE_CREATE;
 		
 		String output = durl.call_url(url, Constant.POST, content);
 				
 		if (-1 != output.indexOf("success")) {
 			return Error.OK;
 		}
 		
 		return durl.error;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sohu.wuhan.IReadable#readArticle(java.lang.String)
 	 */
 	@Override
 	public String readArticle(String key) {
 		String url = domain + Constant.ARTICLE_READ + key + ".xml/?access_token=" + accessToken;
 		return durl.call_url(url, null, null);
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see com.sohu.wuhan.IReadable#deleteArticle(java.lang.String)
 	 */
 	@Override
 	public Error deleteArticle(String key) {
 		String content = null;
 		
 		if (null == key) return Error.ParamNull;
 		
 		try {
 			content = URLEncoder.encode("access_token", encoding) + "=" + URLEncoder.encode(accessToken, encoding); 
 		} catch (Exception e) {
 			return Error.Url;
 		}
 		
 		String url = domain + Constant.ARTICLE_DELETE + key + ".xml/";
 		durl.call_url(url, Constant.POST, content);
 		return Error.OK;
 	}
 	
 	@Override
 	public Error updateArticle(String key, Hashtable<String, String> args) {
 		String content = null;
 		
 		if (null == key || null == args) return Error.ParamNull;
 		
 		try {
 			content = URLEncoder.encode("access_token", encoding) + "=" + URLEncoder.encode(accessToken, encoding); 
 			Enumeration<String> ee = args.keys();
 			while (ee.hasMoreElements()) {
 				String option = ee.nextElement();
 				content += "&" + URLEncoder.encode(option, encoding) + "=" + URLEncoder.encode(args.get(option), encoding);
 			}
 		} catch (Exception e) {
 			return Error.Encode;
 		}
 		
 		String url = domain + Constant.ARTICLE_UPDATE + key + ".xml/";
 		
 		String output = durl.call_url(url, Constant.POST, content);
 				
 		if (-1 != output.indexOf("success")) {
 			return Error.OK;
 		}
 		
 		return durl.error;
 	}
 	
 	@Override
 	public String probeArticle() {
 		String url = domain + Constant.ARTICLE_PROBE + accessToken;
 		return durl.call_url(url, null, null);
 	}
 	
 	//====================================== Asynchronous call =========================================================//
 	
 	public void cancel() {
 		async.cancel();
 	}
 	
 	@Override
 	public boolean asyncCreateArticle(String __url, Handler __handler) {
 		String content = null;
 		
 		if (null == __url) return false;
 		
 		try {
 			content = URLEncoder.encode("access_token", encoding) + "=" + URLEncoder.encode(accessToken, encoding); 
 			content += "&" + URLEncoder.encode("url", encoding) + "=" + URLEncoder.encode(__url, encoding);
 		} catch (Exception e) {
 			return false;
 		}
 		
 		String url_arc = domain + Constant.ARTICLE_CREATE;
 		async.postTask(new Task(url_arc, Constant.POST, content, __handler));
 		return true;
 	}
 
 	@Override
 	public boolean asyncProbeArticle(Handler __handler) {
 		String url_arp = domain + Constant.ARTICLE_PROBE + accessToken;
 		
 		async.postTask(new Task(url_arp, null, null, __handler));
 		return true;
 	}
 
 	@Override
 	public boolean asyncReadArticle(String key, Handler __handler) {
 		String url = domain + Constant.ARTICLE_READ + key + ".xml/?access_token=" + accessToken;
 		async.postTask(new Task(url, null, null, __handler));
 		return true;
 	}
 
 	@Override
 	public boolean asyncUpdateArticle(String key,
 			Hashtable<String, String> args, Handler __handler) {
 		
 		String content = null;
 		
 		if (null == key || null == args) return false;
 		
 		try {
 			content = URLEncoder.encode("access_token", encoding) + "=" + URLEncoder.encode(accessToken, encoding); 
 			Enumeration<String> ee = args.keys();
 			while (ee.hasMoreElements()) {
 				String option = ee.nextElement();
 				content += "&" + URLEncoder.encode(option, encoding) + "=" + URLEncoder.encode(args.get(option), encoding);
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		
 		String url = domain + Constant.ARTICLE_UPDATE + key + ".xml/";
 		async.postTask(new Task(url, Constant.POST, content, __handler));
 		return true;
 	}
 
 	@Override
 	public boolean asyncDeleteArticle(String key, Handler __handler) {
 		String content = null;
 		
 		if (null == key) return false;
 		
 		try {
 			content = URLEncoder.encode("access_token", encoding) + "=" + URLEncoder.encode(accessToken, encoding); 
 		} catch (Exception e) {
 			return false;
 		}
 		
 		String url = domain + Constant.ARTICLE_DELETE + key + ".xml/";
 		async.postTask(new Task(url, Constant.POST, content, __handler));
 		return true;
 	}
 }
 
 
 
 
