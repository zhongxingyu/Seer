 package com.sk.api.impl;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.JsonObject;
 import com.pipl.api.data.containers.Record;
 import com.pipl.api.data.fields.Address;
 import com.pipl.api.data.fields.DOB;
 import com.pipl.api.data.fields.Education;
 import com.pipl.api.data.fields.Email;
 import com.pipl.api.data.fields.Job;
 import com.pipl.api.data.fields.Name;
 import com.pipl.api.data.fields.Phone;
 import com.pipl.api.data.fields.Username;
 import com.pipl.api.search.SearchAPIError;
 import com.pipl.api.search.SearchAPIRequest;
 import com.pipl.api.search.SearchAPIResponse;
 import com.sk.api.ApiUtility;
 import com.sk.impl.ScrapeController;
 import com.sk.util.PersonalData;
 import com.sk.util.parse.search.NameSearcher;
 
 public class PiplApiSearcher implements NameSearcher {
 
 	private final String key;
 	private final ThreadLocal<URL[]> urls = new ThreadLocal<>();
 	private final ThreadLocal<PersonalData[]> data = new ThreadLocal<>();
 
 	public PiplApiSearcher() {
 		JsonObject obj = ApiUtility.getTokensFor("PiplApi");
 		if (obj.has("client_key"))
 			key = obj.get("client_key").getAsString();
 		else
 			throw new RuntimeException("Failed to get key");
 	}
 
 	@Override
 	public boolean lookForName(String first, String last) throws IOException {
 		SearchAPIRequest req = new SearchAPIRequest.Builder().firstName(first).lastName(last).apiKey(key).build();
 		SearchAPIResponse resp;
 		try {
 			resp = req.send();
 		} catch (SearchAPIError | URISyntaxException e) {
 			return false;
 		}
 		ScrapeController scrape = ScrapeController.getController();
 		List<PersonalData> data = new ArrayList<>();
 		List<URL> url = new ArrayList<>();
 		for (Record possible : resp.getRecords()) {
 			PersonalData cur = new PersonalData("pipl");
 
 			if (possible.getPhones() != null) {
 				for (Phone ph : possible.getPhones()) {
 					cur.put(ph.getType().replaceAll("_", "-"), ph.getDisplay());
 				}
 			}
 			if (possible.getDobs() != null) {
 				for (DOB d : possible.getDobs()) {
 					cur.put("age", d.age() + "");
 					cur.put("dob", DateFormat.getDateInstance().format(d.getDateRange().middle()));
 					break;
 				}
 			}
 			grab(possible, cur, Email.class, new String[] { "Address" }, new String[] { "email" });
 			grab(possible, cur, Username.class, new String[] { "Content" }, new String[] { "username" });
 			grab(possible, cur, Job.class, new String[] { "Title", "Industry" }, new String[] { "job-title",
 					"industry" });
 			grab(possible, cur, Education.class, new String[] { "display" }, new String[] { "education" });
 			grab(possible, cur, Name.class, new String[] { "display", "First", "Last" }, new String[] { "name",
 					"first-name", "last-name" });
			grab(possible, cur, Address.class, new String[] { "display", "Country", "City", "House", "PoBox",
 					"Apartment", "Street", "State" }, new String[] { "address", "country", "city", "house",
 					"po-box", "apartment", "street", "state" });
 			if (scrape.isValid(possible.getSource().getUrl()))
 				url.add(new URL(possible.getSource().getUrl()));
 			data.add(cur);
 		}
 		urls.set(url.toArray(new URL[url.size()]));
 		this.data.set(data.toArray(new PersonalData[data.size()]));
 		return true;
 	}
 
 	private void grab(Record record, PersonalData store, Class<?> clazz, String[] methodNames,
 			String[] attributeNames) {
 		if (methodNames.length != attributeNames.length)
 			throw new IllegalArgumentException();
 		Class<?> recordClazz = Record.class;
 
 		Method source = null;
 		for (Method m : recordClazz.getMethods()) {
 			if (m.getName().toUpperCase().contains(clazz.getSimpleName().toUpperCase())
 					&& m.getParameterTypes().length == 0) {
 				source = m;
 				break;
 			}
 		}
 		if (source == null)
 			throw new IllegalArgumentException();
 		Method[] grabbers = new Method[methodNames.length];
 		StringBuilder[] builders = new StringBuilder[methodNames.length];
 		try {
 			for (int i = 0; i < methodNames.length; ++i) {
 				grabbers[i] = clazz.getDeclaredMethod(Character.isUpperCase(methodNames[i].charAt(0)) ? "get"
 						+ methodNames[i] : methodNames[i]);
 				builders[i] = new StringBuilder();
 			}
 		} catch (NoSuchMethodException ex) {
 			ex.printStackTrace();
 			throw new IllegalArgumentException();
 		}
 		try {
 			Object list = source.invoke(record);
 			if (list == null)
 				return;
 
 			for (Object item : (ArrayList<?>) list) {
 				for (int j = 0; j < methodNames.length; ++j) {
 					Object grabbed = grabbers[j].invoke(item);
 					if (grabbed != null)
 						builders[j].append(grabbed);
 					builders[j].append("|");
 				}
 			}
 		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException();
 		}
 		for (int i = 0; i < methodNames.length; ++i) {
 			if (!builders[i].toString().matches("[|]*")) {
 				store.put(attributeNames[i], builders[i].substring(0, builders[i].length() - 1));
 			}
 		}
 	}
 
 	@Override
 	public URL[] results() throws IllegalStateException {
 		URL[] ret = urls.get();
 		if (ret == null)
 			throw new IllegalStateException();
 		else
 			return ret;
 	}
 
 	@Override
 	public PersonalData[] getData() throws IllegalStateException {
 		PersonalData[] ret = data.get();
 		if (ret == null)
 			throw new IllegalStateException();
 		else
 			return ret;
 	}
 }
