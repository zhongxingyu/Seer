 package com.sk.impl2;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.sk.api.NameComparison;
 import com.sk.parse.Extractor;
 import com.sk.parse.OuterLoader;
 import com.sk.parse.PagingLoader;
 import com.sk.parse.Parsers;
 import com.sk.web.IOUtil;
 import com.sk.web.Request;
 
 public class GooglePlusApiLoader extends OuterLoader {
 
 	private static final String URL = "https://www.googleapis.com/plus/v1/people?query=%s%%20%s&"
			+ "pageToken=%s&fields=items(id,displayName),nextPageToken&maxResults=50";
 	private static final String NEXT_PAGE_KEY = "nextPageToken";
 
 	static final String SITE_KEY = "g+";
 
 	private final String first, last, names[], url;
 	private JsonObject json;
 
 	public GooglePlusApiLoader(String first, String last) {
 		this(new String[] { first, last }, "");
 	}
 
 	private GooglePlusApiLoader(String[] names, String token) {
 		this.first = names[0];
 		this.last = names[1];
 		this.names = names;
 		this.url = String.format(URL, IOUtil.urlEncode(first), IOUtil.urlEncode(last), token);
 	}
 
 	@Override
 	protected List<Extractor> getExtractors() {
 		List<Extractor> ret = new ArrayList<>();
		boolean stop = true;
 		for (JsonElement personElement : getPeople()) {
 			JsonObject person = personElement.getAsJsonObject();
 			if (!checkName(person)) {
				stop = true;
 				break;
 			}
			stop = false;
 			String id = person.get("id").getAsString();
 			try {
 				ret.add(new GooglePlusPersonLoader(id));
 			} catch (MalformedURLException e) {
 			}
 		}
		stopPaging.set(stop);
 		return ret;
 	}
 
 	@Override
 	protected PagingLoader createNextPage() {
 		init();
 		if (!json.has(NEXT_PAGE_KEY))
 			return null;
		String nextToken = json.get(NEXT_PAGE_KEY).getAsString();
 		if (stopPaging.get())
 			return null;
 		return new GooglePlusApiLoader(names, nextToken);
 	}
 
 	@Override
 	protected Request getRequest() {
 		try {
 			Request request = new Request(url, "GET");
 			request.addQuery("key", ApiUtility.getAccessToken(SITE_KEY).getKey());
 			return request;
 		} catch (MalformedURLException ignored) {
 			return null;
 		}
 	}
 
 	@Override
 	protected void parse(URL source, String data) {
 		json = Parsers.parseJSON(data).getAsJsonObject();
 	}
 
 	@Override
 	protected boolean loadStopSearching() {
		for (JsonElement personElement : getPeople()) {
			if (!checkName(personElement.getAsJsonObject()))
				return true;
		}
		return false;
 	}
 
 	private boolean checkName(JsonObject person) {
 		NameComparison nameUtil = NameComparison.get();
 		String displayName = person.get("displayName").getAsString();
 		String[] parsedNames = nameUtil.parseName(displayName);
 		return nameUtil.isSameName(names, parsedNames);
 	}
 
 	private JsonArray getPeople() {
 		init();
 		if (json.has("items"))
 			return json.get("items").getAsJsonArray();
 		else
 			return new JsonArray();
 	}
 
 }
