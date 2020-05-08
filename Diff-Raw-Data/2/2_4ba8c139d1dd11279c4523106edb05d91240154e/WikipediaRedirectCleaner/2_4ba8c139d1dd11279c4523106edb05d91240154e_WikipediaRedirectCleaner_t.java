 package com.sk.clean.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.sk.clean.DataCleaner;
 import com.sk.util.FieldBuilder;
 import com.sk.util.PersonalData;
 
 public class WikipediaRedirectCleaner implements DataCleaner {
 
 	private final String[] attr;
 
 	public WikipediaRedirectCleaner(String... attributeNames) {
 		this.attr = attributeNames;
 	}
 
 	@Override
 	public boolean clean(PersonalData in, PersonalData out) {
 		FieldBuilder builder = new FieldBuilder();
 		Map<String, String> allValues = new TreeMap<>();
 		for (String attributeName : attr) {
 			if (in.containsKey(attributeName)) {
 				for (String value : in.getAllValues(attributeName)) {
 					allValues.put(value, value);
 				}
 			}
 		}
 		StringBuilder titles = new StringBuilder();
 		Iterator<String> valueIterator = allValues.keySet().iterator();
 		Set<String> disambiguationTitles = new TreeSet<>();
 		Map<String, String> queryMap = new HashMap<>();
 		while (valueIterator.hasNext()) {
 			for (int i = 0; i < 50 && valueIterator.hasNext(); ++i) {
 				titles.append(valueIterator.next());
 				titles.append("|");
 			}
 			queryMap.put("titles", titles.substring(0, titles.length() - 1));
 			queryMap.put("prop", "pageprops");
 			queryMap.put("redirects", "");
 			JsonObject result = readWiki(queryMap);
 			JsonObject query = result.get("query").getAsJsonObject();
 			if (query.has("normalized")) {
 				for (JsonElement normalizedElement : query.get("normalized").getAsJsonArray()) {
 					JsonObject normal = normalizedElement.getAsJsonObject();
 					String from = normal.get("from").getAsString();
 					String to = normal.get("to").getAsString();
 					allValues.put(from, to);
 					allValues.put(to, to);
 				}
 			}
 			if (query.has("redirects")) {
 				for (JsonElement redirElement : query.get("redirects").getAsJsonArray()) {
 					JsonObject redir = redirElement.getAsJsonObject();
 					String from = redir.get("from").getAsString();
 					String to = redir.get("to").getAsString();
 					allValues.put(from, to);
 					allValues.put(to, to);
 				}
 			}
 			if (query.has("pages")) {
 				for (Entry<String, JsonElement> page : query.get("pages").getAsJsonObject().entrySet()) {
 					JsonObject pageObject = page.getValue().getAsJsonObject();
 					if (pageObject.has("pageprops")) {
 						JsonObject props = pageObject.get("pageprops").getAsJsonObject();
 						if (props.has("disambiguation"))
 							disambiguationTitles.add(pageObject.get("title").getAsString());
 					}
 					if (pageObject.has("missing")) {
						String from = allValues.get(pageObject.get("title").getAsString());
 						String to = GenericCleaner.cleanValue(from);
 						allValues.put(from, to);
 						allValues.put(to, to);
 					}
 				}
 			}
 		}
 		valueIterator = disambiguationTitles.iterator();
 
 		queryMap.clear();
 		queryMap.put("pllimit", "500");
 		queryMap.put("prop", "links");
 		while (valueIterator.hasNext()) {
 			FieldBuilder extendedTitleBuilder = new FieldBuilder();
 			for (int i = 0; i < 50 && valueIterator.hasNext(); ++i) {
 				titles.append(valueIterator.next());
 				titles.append("|");
 			}
 			queryMap.put("titles", titles.substring(0, titles.length() - 1));
 			queryMap.remove("plcontinue");
 			JsonObject result;
 			while (true) {
 				result = readWiki(queryMap);
 				for (Entry<String, JsonElement> pageEntry : result.get("query").getAsJsonObject().get("pages")
 						.getAsJsonObject().entrySet()) {
 					JsonObject page = pageEntry.getValue().getAsJsonObject();
 					String title = page.get("title").getAsString();
 					if (page.has("links")) {
 						for (JsonElement linkElement : page.get("links").getAsJsonArray()) {
 							JsonObject link = linkElement.getAsJsonObject();
 							if (link.get("ns").getAsInt() == 0) {
 								extendedTitleBuilder.put(link, "title", title);
 							}
 						}
 					}
 				}
 
 				if (result.has("query-continue")) {
 					queryMap.put("plcontinue", result.get("query-continue").getAsJsonObject().get("links")
 							.getAsJsonObject().get("plcontinue").getAsString());
 				} else {
 					break;
 				}
 			}
 			Map<String, String> extendedTitles = new HashMap<>();
 			extendedTitleBuilder.addTo(extendedTitles);
 			for (Entry<String, String> title : extendedTitles.entrySet()) {
 				allValues.put(title.getKey(), title.getValue());
 				allValues.put(title.getValue(), title.getValue());
 			}
 		}
 		for (String attributeName : attr) {
 			for (String value : in.getAllValues(attributeName)) {
 				String cur = value;
 				while (true) {
 					String next = allValues.get(cur);
 					if (next == null || next.equals(cur))
 						break;
 					cur = next;
 				}
 				builder.put(attributeName, cur);
 			}
 			in.remove(attributeName);
 		}
 		builder.addTo(out);
 		return !builder.isEmpty();
 	}
 
 	private static final String BASE = "http://en.wikipedia.org/w/api.php?format=json&action=query";
 	private static final JsonParser parser = new JsonParser();
 
 	private JsonObject readWiki(Map<String, String> query) {
 		try {
 			StringBuilder urlb = new StringBuilder(BASE);
 			for (Entry<String, String> param : query.entrySet()) {
 				urlb.append("&");
 				urlb.append(param.getKey());
 				urlb.append("=");
 				urlb.append(URLEncoder.encode(param.getValue(), "UTF-8"));
 			}
 			HttpURLConnection wikiConn = (HttpURLConnection) new URL(urlb.toString()).openConnection();
 			wikiConn.addRequestProperty("User-Agent", "WebfootprintingGrabber/1.0 (gtgrab@strikeskids.com)");
 			return parser.parse(new BufferedReader(new InputStreamReader(wikiConn.getInputStream())))
 					.getAsJsonObject();
 		} catch (IOException ex) {
 			return null;
 		}
 	}
 
 }
