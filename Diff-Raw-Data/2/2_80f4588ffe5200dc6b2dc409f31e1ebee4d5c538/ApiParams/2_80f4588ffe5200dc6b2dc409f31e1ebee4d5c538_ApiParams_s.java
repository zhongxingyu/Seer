 //liuweili@baixing.com
 package com.baixing.network.api;
 
 import java.io.Serializable;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.baixing.network.NetworkUtil;
 
 /**
  * 
  * @history: by liuchong add <code>addParams</code> for primitive types.
  */
 public class ApiParams implements Serializable {
 	
 	private static final long serialVersionUID = 6811845003931804312L;
 	
 	public static final String KEY_APIKEY = "api_key";
 	public static final String KEY_UDID = "udid";
 	public static final String KEY_VERSION = "version";
 	public static final String KEY_TIMESTAMP = "timestamp";
 	public static final String KEY_CHANNEL = "channel";
 	public static final String KEY_USERID = "userId";
 	public static final String KEY_CITY = "city";
 	public static final String KEY_ACCESSTOKEN = "access_token";
 	
 	private Map<String,String> params=new HashMap<String,String>();
 	public boolean useCache = false;
 	public boolean zipRequest = false;
 	
 	/**
 	 * 添加业务参数
 	 * @param key
 	 * @param value
 	 */
 	public void addParam(String key,String value){
 		addParameter(key, value);
 	}
 	
 	public boolean hasParam(String key) {
 		return params.containsKey(key);
 	}
 	
 	public void addAll(Map<String, String> all){
 		params.putAll(all);
 	}
 	
 	private void addParameter(String key, Object value)
 	{
 		if (value != null)
 		{
 			params.put(key, value.toString());
 		}
 	}
 	
 	public void addParam(String key, int value)
 	{
 		params.put(key, value + "");
 	}
 	
 	public void addParam(String key, double value)
 	{
 		params.put(key, value + "");
 	}
 	
 	public void addParam(String key, long value)
 	{
 		params.put(key, value + "");
 	}
 	
 	public void addParam(String key, float value)
 	{
 		params.put(key, value + "");
 	}
 	
 	public void addParam(String key, short value)
 	{
 		params.put(key, value + "");
 	}
 	
 	public void addParam(String key, boolean value)
 	{
 		this.addParameter(key, Boolean.valueOf(value));
 	}
 	
 	/**
 	 * 获取已添加的业务参数
 	 * @param key
 	 * @return
 	 */
 	public String getParam(String key){
 		return params.get(key);
 	}
 	/**
 	 * 删除已添加的业务参数
 	 * @param key
 	 */
 	public void removeParam(String key){
 		params.remove(key);
 	}
 	
 	public Map<String, String> getParams() {
 		return params;
 	}
 	public void setParams(Map<String, String> params) {
 		this.params = params;
 	}
 	
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		Map<String, String> map = this.params;
 		if (map != null) {
 			Set<Entry<String, String>> set = map.entrySet();
 			for (Entry<String, String> entry : set) {
 				sb.append(entry.getKey() + ":" + entry.getValue());
 				sb.append('\n');
 			}
 		}
 		
 		return sb.toString();
 	}
 	
 	public String toUrlParams()
 	{
 		StringBuffer buf = new StringBuffer();
 		Iterator<String> keys = params.keySet().iterator();
 		while (keys.hasNext())
 		{
 			String key = keys.next();
 			buf.append(key).append("=").append(URLEncoder.encode(params.get(key))).append("&");
 		}
 		
 		if (buf.length() > 0) {
 			buf.deleteCharAt(buf.length()-1);
 		}
 		
 		return buf.toString();
 	}
 	
 	/**
 	 * Set authentication information to request parameters.
 	 * 
 	 * @param account user account used to login.
 	 * @param password password of the specified account.
 	 */
 	public void appendAuthInfo(String account, String password) {
 		this.addParam("mobile", account);
 		this.addParam("userToken", generateUsertoken(password));
 	}
 	
 	static private String generateUsertoken(String password) {
 		String password1 = NetworkUtil.getMD5(password.trim());
		password1 += password;
 		return NetworkUtil.getMD5(password1);
 	}
 }
