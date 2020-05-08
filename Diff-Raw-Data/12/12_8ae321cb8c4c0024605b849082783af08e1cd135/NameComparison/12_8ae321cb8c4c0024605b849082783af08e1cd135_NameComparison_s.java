 package com.sk.util;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.sk.parse.util.Parsers;
 import com.sk.util.navigate.DocNavigator;
 import com.sk.web.IOUtil;
 import com.sk.web.Request;
 import com.sk.web.Token;
 
 public class NameComparison {
 
 	private static final String TOKEN_NAME = "name_key";
 	private static final String API_NAME = "pipl";
 
 	private static final String LAST_NAME_KEY = "last";
 	private static final String FIRST_NAME_KEY = "first";
 
 	private static final String SEARCH_FORMAT_EXCLUDE_PATTERN = "[^A-Za-z]";
 
 	private static final String FIRST_NAME_BASE_URL = "http://api.pipl.com/name/v2/json/?first_name=%s";
 	private static final String RAW_NAME_BASE_URL = "http://api.pipl.com/name/v2/json/?raw_name=%s";
 
 	private static final long MINIMUM_SEPARATION_TIME = 20;
 
 	private long lastReadTime = 0;
 	private final Object readLock = new Object();
 
 	private final String key;
 
 	private final Map<String, Set<String>> firstNames = new HashMap<>();
 	private final Map<String, String[]> parsedNames = new HashMap<>();
 
 	private NameComparison(String key) {
 		this.key = key;
 	}
 
 	public Set<String> getRelatedFirstNames(String firstName) {
 		if (firstName == null)
 			return new HashSet<>();
 		firstName = format(firstName);
 		if (firstNames.containsKey(firstName)) {
 			return firstNames.get(firstName);
 		} else {
 			Set<String> ret = loadNamePossibilities(firstName);
 			firstNames.put(firstName, ret);
 			return ret;
 		}
 	}
 
 	public boolean isSameFirstName(String personA, String personB) {
 		Set<String> possA = new HashSet<>(getRelatedFirstNames(personA));
 		possA.retainAll(getRelatedFirstNames(personB));
 		return !possA.isEmpty();
 	}
 
 	public boolean isSameFullName(String[] personANames, String[] personBNames) {
 		if (!checkNameArrayFormat(personANames) || !checkNameArrayFormat(personBNames))
 			return false;
 		return isSame(personANames[1], personBNames[1]) && isSameFirstName(personANames[0], personBNames[0]);
 	}
 
 	public String[] parseName(String rawName) {
 		String formattedRawName = rawName.toLowerCase();
 		if (parsedNames.containsKey(formattedRawName)) {
 			return parsedNames.get(formattedRawName);
 		} else {
 			String[] parsedName = loadParsedName(formattedRawName);
 			parsedNames.put(formattedRawName, parsedName);
 			return parsedName;
 		}
 	}
 
 	private String[] loadParsedName(String formattedRawName) {
 		JsonObject names = getJsonForRawName(formattedRawName);
 		JsonObject name = names.get("name").getAsJsonObject();
 		if (name.has(FIRST_NAME_KEY) && name.has(LAST_NAME_KEY)) {
 			return new String[] { name.get(FIRST_NAME_KEY).getAsString(), name.get(LAST_NAME_KEY).getAsString() };
 		}
 		return null;
 	}
 
 	private JsonObject getJsonForRawName(String formattedRawName) {
 		JsonObject ret = getAndParseJson(RAW_NAME_BASE_URL, formattedRawName).getAsJsonObject();
 		return ret;
 	}
 
 	private JsonElement getAndParseJson(String base, String query) {
 		try {
 			String jsonString = getJsonString(base, query);
 			return Parsers.parseJSON(jsonString);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return new JsonObject();
 		}
 	}
 
 	private String getJsonString(String base, String query) throws IOException {
 		String encodedQuery = IOUtil.urlEncode(query);
 		Request request = new Request(String.format(base, encodedQuery));
 		request.addQuery("key", key);
 		synchronized (readLock) {
 			waitForRead();
 			return IOUtil.read(request);
 		}
 	}
 
 	private void waitForRead() {
 		long wait = MINIMUM_SEPARATION_TIME - (System.currentTimeMillis() - lastReadTime);
 		if (wait > 0)
 			try {
 				Thread.sleep(wait);
 			} catch (InterruptedException ignored) {
 				Thread.currentThread().interrupt();
 			}
 		lastReadTime = System.currentTimeMillis();
 	}
 
 	private boolean isSame(String lastName1, String lastName2) {
 		return format(lastName1).equals(format(lastName2));
 	}
 
 	private boolean checkNameArrayFormat(String[] names) {
 		return names != null && names.length == 2;
 	}
 
 	private Set<String> loadNamePossibilities(String name) {
		Set<String> ret = new HashSet<>();
 		JsonElement json = getAndParseJson(FIRST_NAME_BASE_URL, name);
 		for (DocNavigator navigator : firstNameNavigators) {
			ret.addAll(navigator.navigate(json));
 		}
		ret.add(name);
 		return ret;
 	}
 
 	public String format(String input) {
 		if (input == null)
 			return "";
 		else
 			return input.toLowerCase().replaceAll(SEARCH_FORMAT_EXCLUDE_PATTERN, "");
 	}
 
 	private static final DocNavigator[] firstNameNavigators = { new DocNavigator("full_names", FIRST_NAME_KEY),
 			new DocNavigator("nicknames", FIRST_NAME_KEY), new DocNavigator("spellings", FIRST_NAME_KEY) };
 
 	private static final LazyField<NameComparison> singleton = new LazyField<>(new Callable<NameComparison>() {
 		@Override
 		public NameComparison call() throws Exception {
 			Token token = ApiUtility.getNamedToken(API_NAME, TOKEN_NAME);
 			return new NameComparison(token.getKey());
 		}
 	});
 
 	public static NameComparison get() {
 		return singleton.get();
 	}
 
 }
