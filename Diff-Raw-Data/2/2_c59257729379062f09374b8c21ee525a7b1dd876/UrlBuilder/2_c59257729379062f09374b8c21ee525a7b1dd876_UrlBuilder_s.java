 package com.tk.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 
 public class UrlBuilder {
 
 	private String url;
 	private Map<String, String> queries;
 	private UrlBuilder(String url) {
 		this.url = url;
 		this.queries = new HashMap<String, String>();
 	}
 
 	public static UrlBuilder valueOf(String url) {
 		return new UrlBuilder(url);
 	}
 	
 	public static UrlBuilder valueOf(String... urls) {
 		return new UrlBuilder(StringUtils.join(urls));
 	}
	public String buld() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(url);
 		
 		if (!queries.isEmpty()) {
 			sb.append("?").append(buildQuery(queries));
 		}
 		return sb.toString();
 	}
 	private String buildQuery(Map<String, String> queries) {
 		List<String> list = new ArrayList<String>();
 		for (String key: queries.keySet()) {
 			list.add(key + "=" + queries.get(key));
 		}
 		return StringUtils.join(list, "&");
 	}
 	
 	public UrlBuilder add(String key, String value) {
 		if (StringUtils.isNotEmpty(value)) {
 		    queries.put(key, StringEscapeUtils.escapeHtml(value));
 		}
 		return this;
 	}
 	
 	public UrlBuilder add(String key, Object value) {
 		if (value != null) {
 		    add(key, value.toString());
 		}
 		return this;
 	}
 	public UrlBuilder add(String key, int value) {
 		add(key, String.valueOf(value));
 		return this;
 	}
 	public UrlBuilder add(String key, long value) {
 		add(key, String.valueOf(value));
 		return this;
 	}
 }
