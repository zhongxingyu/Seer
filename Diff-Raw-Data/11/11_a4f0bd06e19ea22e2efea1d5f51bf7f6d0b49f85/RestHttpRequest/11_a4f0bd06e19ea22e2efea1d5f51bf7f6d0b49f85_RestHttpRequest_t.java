 package com.baixing.network.impl;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import android.text.TextUtils;
 import android.util.Log;
 
 /**
  * 
  * @author liuchong
  *
  * @date 2013-2-6
  */
 public abstract class RestHttpRequest extends BaseHttpRequest {
 
 	public static final String DEFAULT_CHARSET = "UTF-8";
 			
 	protected String charset;
 	protected Map<String, String> parameters;
 	
 	protected RestHttpRequest(String baseUrl, Map<String, String> parameters) {
 		this(baseUrl, parameters, DEFAULT_CHARSET);
 	}
 	
 	protected RestHttpRequest(String baseUrl, Map<String, String> parameters, String charset) {
 		super(baseUrl);
 		this.parameters = parameters;
 		this.charset = charset;
 	}
 	
 	protected String getFormatParams() {
 
 		Map<String, String> params = parameters;//.getParams();
 		
 		if (params == null || params.isEmpty()) {
 			return null;
 		}
 //		if (TextUtils.isEmpty(charset)) {
 //			charset = DEFAULT_CHARSET;
 //		}
 
 		StringBuilder query = new StringBuilder();
 //		Set<Entry<String, String>> entries = params.entrySet();
 		boolean hasParam = false;
 //		Set<String> keySet = params.keySet();
 		List<String> keyList = new ArrayList<String>(params.keySet());
 		Collections.sort(keyList);
 		
 		for (String key : keyList) {
 			String name = key;//entry.getKey();
 			String value = params.get(key);//entry.getValue();
 			// 忽略参数名或参数值为空的参数
 			if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
 				if (hasParam) {
 					query.append("&");
 				} else {
 					hasParam = true;
 				}
 
 				String encodedValue = "";//URLEncoder.encode(value);
 				try {
					encodedValue = URLEncoder.encode(value);//URLEncoder.encode(value, charset);//
				} catch (Throwable e) {
 					Log.w(TAG, "fail to encoded parameter [key, value]:[" + name + "," + value + "] with charset " + charset);
 				}
 				query.append(name).append("=").append(encodedValue);
 			}
 		}
 
 		return query.toString();
 	}
 	
 	public static String urlEncode(String str) {
 		return str.replaceAll(":", "%3A").replaceAll(" ", "%20")
 				.replaceAll("\\(", "%28").replaceAll("\\)", "%29")
 				.replaceAll("/", "%2F").replaceAll("\\+", "%20")
 				.replaceAll("\\*", "%2A").replaceAll("\\,", "%2C");
 	}
 
 	@Override
 	public String getContentType() {
 //		return "application/x-www-form-urlencoded;charset=" + charset;
 		return "application/x-www-form-urlencoded";
 	}
 }
